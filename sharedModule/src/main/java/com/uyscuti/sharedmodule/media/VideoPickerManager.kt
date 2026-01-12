package com.uyscuti.sharedmodule.media

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import android.util.Log

class VideoPickerManager(private val activity: AppCompatActivity) {

    private lateinit var videoPickerLauncher: ActivityResultLauncher<Intent>
    private var onVideoSelected: ((List<Pair<Uri, String?>>) -> Unit)? = null
    private var isMultiSelect = false

    fun initialize(onVideoSelected: (List<Pair<Uri, String?>>) -> Unit) {
        this.onVideoSelected = onVideoSelected
        videoPickerLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            handleVideoPickerResult(result)
        }
    }

    fun launchVideoPicker(allowMultiple: Boolean = false) {
        isMultiSelect = allowMultiple
        val intent = createVideoPickerIntent(allowMultiple)
        videoPickerLauncher.launch(intent)
    }

    private fun createVideoPickerIntent(allowMultiple: Boolean): Intent {
        return Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "video/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "video/mp4",
                "video/mpeg",
                "video/quicktime",
                "video/x-msvideo",
                "video/x-matroska",
                "video/webm",
                "video/*"
            ))
            addCategory(Intent.CATEGORY_OPENABLE)

            // Enable multiple selection
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultiple)

            // Only show local files
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            }
        }
    }

    private fun handleVideoPickerResult(result: ActivityResult) {
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val data = result.data
            val videos = mutableListOf<Pair<Uri, String?>>()

            if (isMultiSelect && data?.clipData != null) {
                // Multiple videos selected
                val clipData = data.clipData!!
                for (i in 0 until clipData.itemCount) {
                    val uri = clipData.getItemAt(i).uri
                    val fileName = getVideoFileName(uri)
                    val duration = getVideoDuration(uri)
                    val displayName = if (duration > 0) "$fileName ($duration ms)" else fileName
                    videos.add(Pair(uri, displayName))
                }
            } else if (data?.data != null) {
                // Single video selected
                val uri = data.data!!
                val fileName = getVideoFileName(uri)
                val duration = getVideoDuration(uri)
                val displayName = if (duration > 0) "$fileName ($duration ms)" else fileName
                videos.add(Pair(uri, displayName))
            }

            onVideoSelected?.invoke(videos)
        } else {
            // User cancelled
            onVideoSelected?.invoke(emptyList())
        }
    }

    private fun getVideoFileName(uri: Uri): String? {
        return when (uri.scheme) {
            "content" -> {
                val cursor = activity.contentResolver.query(
                    uri, arrayOf(MediaStore.Video.Media.DISPLAY_NAME),
                    null, null, null
                )
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)
                        it.getString(nameIndex)
                    } else null
                }
            }
            "file" -> {
                uri.lastPathSegment
            }
            else -> uri.lastPathSegment
        }
    }

    private fun getVideoDuration(uri: Uri): Long {
        return try {
            val cursor = activity.contentResolver.query(
                uri, arrayOf(MediaStore.Video.Media.DURATION),
                null, null, null
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    val durationIndex = it.getColumnIndex(MediaStore.Video.Media.DURATION)
                    it.getLong(durationIndex)
                } else 0L
            } ?: 0L
        } catch (e: Exception) {
            Log.e("VideoPicker", "Error getting video duration", e)
            0L
        }
    }
}
