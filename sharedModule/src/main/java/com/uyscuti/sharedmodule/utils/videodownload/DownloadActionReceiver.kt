package com.uyscuti.sharedmodule.utils.videodownload

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.work.WorkManager
import java.io.File

class DownloadActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val postId = intent.getStringExtra(VideoDownloadWorker.KEY_POST_ID) ?: return

        when (intent.action) {
            VideoDownloadWorker.ACTION_CANCEL -> {
                handleCancel(context, postId)
            }
            VideoDownloadWorker.ACTION_PAUSE -> {
                handlePause(context, postId)
            }
            VideoDownloadWorker.ACTION_RESUME -> {
                handleResume(context, postId)
            }
        }
    }

    private fun handleResume(context: Context, postId: String) {
        // Dismiss the paused notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = postId.hashCode()
        notificationManager.cancel(notificationId + 1000)

        // Delete pause check file FIRST
        val pauseFile = File(context.filesDir, "download_${postId}_pause")
        if (pauseFile.exists()) {
            pauseFile.delete()
        }

        // Get resume information
        val resumeFile = File(context.filesDir, "download_${postId}_resume")
        if (resumeFile.exists()) {
            try {
                val lines = resumeFile.readLines()
                if (lines.size >= 5) {
                    val resumeFromByte = lines[1].toLongOrNull() ?: 0L
                    val videoUrl = lines[2]
                    val videoTitle = lines[3]
                    val progress = lines[4].toIntOrNull() ?: 0

                    android.util.Log.d("VideoDownload", "Resuming download for $postId from byte $resumeFromByte (${progress}%)")

                    // Small delay to ensure pause file deletion is processed
                    Thread.sleep(100)

                    // Resume the download
                    val downloadManager = VideoDownloadManager(context)
                    downloadManager.startDownload(videoUrl, postId, videoTitle, resumeFromByte)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun handleCancel(context: Context, postId: String) {
        // Cancel the work
        val tag = "download_$postId"
        WorkManager.getInstance(context).cancelAllWorkByTag(tag)

        // Delete pause check file
        val pauseFile = File(context.filesDir, "download_${postId}_pause")
        if (pauseFile.exists()) {
            pauseFile.delete()
        }

        // Delete resume info file
        val resumeFile = File(context.filesDir, "download_${postId}_resume")
        if (resumeFile.exists()) {
            try {
                val lines = resumeFile.readLines()
                if (lines.isNotEmpty()) {
                    val uri = Uri.parse(lines[0])
                    context.contentResolver.delete(uri, null, null)
                }
            } catch (e: Exception) {
                // Ignore
            }
            resumeFile.delete()
        }

        // Dismiss both the foreground and paused notifications
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = postId.hashCode()
        notificationManager.cancel(notificationId) // Foreground notification
        notificationManager.cancel(notificationId + 1000) // Paused notification
    }

    private fun handlePause(context: Context, postId: String) {
        // Create pause check file with a small delay to ensure worker sees it
        val pauseFile = File(context.filesDir, "download_${postId}${VideoDownloadWorker.PAUSE_CHECK_FILE_SUFFIX}")
        try {
            if (!pauseFile.exists()) {
                pauseFile.createNewFile()
            }
            // Write a timestamp to verify it's a fresh pause request
            pauseFile.writeText(System.currentTimeMillis().toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}