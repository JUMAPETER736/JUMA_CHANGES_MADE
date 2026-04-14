package com.uyscuti.sharedmodule.utils.videodownload


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import android.content.pm.ServiceInfo
import android.media.MediaScannerConnection
import kotlinx.coroutines.delay

class VideoDownloadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface DownloadStateManagerEntryPoint {
        fun downloadStateManager(): DownloadStateManager
    }

    private val downloadStateManager: DownloadStateManager by lazy {
        val appContext = applicationContext.applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(
            appContext,
            DownloadStateManagerEntryPoint::class.java
        )
        entryPoint.downloadStateManager()
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val notificationId = inputData.getString(KEY_POST_ID).hashCode()

    @Volatile
    private var isPaused = false

    companion object {
        const val KEY_VIDEO_URL = "video_url"
        const val KEY_POST_ID = "post_id"
        const val KEY_VIDEO_TITLE = "video_title"
        const val KEY_RESUME_FROM_BYTE = "resume_from_byte"
        const val CHANNEL_ID = "video_downloads"
        const val CHANNEL_NAME = "Video Downloads"
        const val PROGRESS = "progress"
        const val DOWNLOAD_STATUS = "download_status"
        const val BYTES_DOWNLOADED = "bytes_downloaded"
        const val STATUS_SUCCESS = "success"
        const val STATUS_FAILED = "failed"
        const val STATUS_CANCELLED = "cancelled"
        const val STATUS_PAUSED = "paused"

        const val ACTION_CANCEL = "com.uyscuti.social.circuit.ACTION_CANCEL_DOWNLOAD"
        const val ACTION_PAUSE = "com.uyscuti.social.circuit.ACTION_PAUSE_DOWNLOAD"
        const val ACTION_RESUME = "com.uyscuti.social.circuit.ACTION_RESUME_DOWNLOAD"

        const val PAUSE_CHECK_FILE_SUFFIX = "_pause"
        private const val ALBUM_NAME = "FlashShorts"

    }


    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val videoUrl = inputData.getString(KEY_VIDEO_URL) ?: return@withContext Result.failure()
        val postId = inputData.getString(KEY_POST_ID) ?: return@withContext Result.failure()
        val videoTitle = inputData.getString(KEY_VIDEO_TITLE) ?: "Video"
        val resumeFromByte = inputData.getLong(KEY_RESUME_FROM_BYTE, 0L)

        createNotificationChannel()

        // Clear pause file at start to prevent immediate pause
        deletePauseCheckFile(postId)

        try {
            // Update global state
            downloadStateManager.startDownload(postId, id)

            // Set foreground to keep worker running
            setForeground(createForegroundInfo(0, videoTitle, false))

            val result = downloadVideo(videoUrl, postId, videoTitle, resumeFromByte)

            when (result) {
                DownloadResult.SUCCESS -> {
                    downloadStateManager.setCompleted(postId)
                    deletePauseCheckFile(postId)
                    deleteResumeInfo(postId)
                    showCompletionNotification(videoTitle, true)
                    Result.success(
                        workDataOf(
                            DOWNLOAD_STATUS to STATUS_SUCCESS,
                            KEY_POST_ID to postId
                        )
                    )
                }

                DownloadResult.PAUSED -> {
                    // Get progress from resume info (more reliable)
                    val resumeInfo = getResumeInfo(postId)
                    val currentProgress = resumeInfo?.progress ?: 0

                    downloadStateManager.setPaused(postId)

                    // Show the paused notification BEFORE the worker ends
                    showPausedNotification(videoTitle, postId, currentProgress)

                    // Give the notification manager time to post the notification
                    delay(1000) // Increased delay

                    Result.failure(
                        workDataOf(
                            DOWNLOAD_STATUS to STATUS_PAUSED,
                            KEY_POST_ID to postId
                        )
                    )
                }

                DownloadResult.FAILED -> {
                    downloadStateManager.setFailed(postId)
                    deletePauseCheckFile(postId)
                    deleteResumeInfo(postId)
                    showCompletionNotification(videoTitle, false)
                    Result.failure(
                        workDataOf(
                            DOWNLOAD_STATUS to STATUS_FAILED,
                            KEY_POST_ID to postId
                        )
                    )
                }
            }
        } catch (e: Exception) {
            if (isStopped) {
                downloadStateManager.setCancelled(postId)
                deletePauseCheckFile(postId)
                deleteResumeInfo(postId)
                deletePartialFile(postId)
                showCompletionNotification(videoTitle, false, "Download cancelled")
                Result.failure(
                    workDataOf(
                        DOWNLOAD_STATUS to STATUS_CANCELLED,
                        KEY_POST_ID to postId
                    )
                )
            } else {
                downloadStateManager.setFailed(postId)
                deletePauseCheckFile(postId)
                deleteResumeInfo(postId)
                showCompletionNotification(videoTitle, false, "Download failed: ${e.message}")
                Result.failure(
                    workDataOf(
                        DOWNLOAD_STATUS to STATUS_FAILED,
                        KEY_POST_ID to postId
                    )
                )
            }
        }
    }

    private enum class DownloadResult {
        SUCCESS, PAUSED, FAILED
    }

}