package com.uyscuti.sharedmodule.media

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class ImagePickerManager(private val activity: AppCompatActivity) {

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private var onImageSelected: ((List<Pair<Uri, String?>>) -> Unit)? = null
    private var isMultiSelect = false

    fun initialize(onImageSelected: (List<Pair<Uri, String?>>) -> Unit) {
        this.onImageSelected = onImageSelected
        imagePickerLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            handleImagePickerResult(result)
        }
    }

    fun launchImagePicker(allowMultiple: Boolean = false) {
        isMultiSelect = allowMultiple
        val intent = createImagePickerIntent(allowMultiple)
        imagePickerLauncher.launch(intent)
    }

    private fun createImagePickerIntent(allowMultiple: Boolean): Intent {
        return Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "image/jpeg",
                "image/png",
                "image/webp",
                "image/gif",
                "image/*"
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

    private fun handleImagePickerResult(result: ActivityResult) {
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val data = result.data
            val images = mutableListOf<Pair<Uri, String?>>()

            if (isMultiSelect && data?.clipData != null) {
                // Multiple images selected
                val clipData = data.clipData!!
                for (i in 0 until clipData.itemCount) {
                    val uri = clipData.getItemAt(i).uri
                    val fileName = getImageFileName(uri)
                    images.add(Pair(uri, fileName))
                }
            } else if (data?.data != null) {
                // Single image selected
                val uri = data.data!!
                val fileName = getImageFileName(uri)
                images.add(Pair(uri, fileName))
            }

            onImageSelected?.invoke(images)
        } else {
            // User cancelled
            onImageSelected?.invoke(emptyList())
        }
    }

    private fun getImageFileName(uri: Uri): String? {
        return when (uri.scheme) {
            "content" -> {
                val cursor = activity.contentResolver.query(
                    uri, arrayOf(MediaStore.Images.Media.DISPLAY_NAME),
                    null, null, null
                )
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
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
}