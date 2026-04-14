package com.uyscuti.sharedmodule.utils.videodownload


import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.*
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit

class VideoDownloadManager(private val context: Context) {

    private val workManager = WorkManager.getInstance(context)

    fun downloadVideo(
        videoUrl: String,
        postId: String,
        videoTitle: String
    ): UUID {
        return startDownload(videoUrl, postId, videoTitle, 0L)
    }

    fun resumeDownload(postId: String, resumeFromByte: Long) {
        // Get the existing work info to retrieve video URL and title
        val workInfos = workManager.getWorkInfosByTag("download_$postId").get()

        // We need to get the original video URL and title
        // These should be stored somewhere accessible, or we can get them from the last work request
        val resumeFile = File(context.filesDir, "download_${postId}_resume")

        if (resumeFile.exists()) {
            // If we have resume info, we should also store the video URL and title
            // For now, we'll need to get this from the application context or pass it differently
            // A better approach is to store this info as well

            // You should store videoUrl and videoTitle when first starting the download
            // For now, let's try to get it from work info if available
            val videoUrl = if (workInfos.isNotEmpty()) {
                workInfos.first().outputData.getString(VideoDownloadWorker.KEY_VIDEO_URL)
            } else {
                null
            }

            val videoTitle = if (workInfos.isNotEmpty()) {
                workInfos.first().outputData.getString(VideoDownloadWorker.KEY_VIDEO_TITLE)
            } else {
                "Video"
            }

            if (videoUrl != null) {
                startDownload(videoUrl, postId, videoTitle ?: "Video", resumeFromByte)
            }
        }
    }


}