package com.uyscuti.social.business.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog

object ImagePicker {

    private const val REQUEST_CODE_IMAGE_PICKER = 100
    private const val REQUEST_CODE_VIDEO_PICKER = 158
    private const val REQUEST_CODE_IMAGE_PICKER_CATALOGUE = 110

    fun pickMedia(activity: Activity) {
        val options = arrayOf("Pick Image", "Pick Video")

        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Select Media")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> {
                    // Pick Image
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activity.startActivityForResult(intent, REQUEST_CODE_IMAGE_PICKER)
                }
                1 -> {
                    // Pick Video
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                    activity.startActivityForResult(intent, REQUEST_CODE_VIDEO_PICKER)
                }
            }
        }
        builder.show()
    }

    fun pickCatalogImage(activity: Activity){
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(intent, REQUEST_CODE_IMAGE_PICKER_CATALOGUE)
    }

//    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Uri? {
//        if (requestCode == REQUEST_CODE_IMAGE_PICKER && resultCode == Activity.RESULT_OK) {
//            return data?.data
//        }else if (requestCode == REQUEST_CODE_IMAGE_PICKER_CATALOGUE && resultCode == Activity.RESULT_OK){
//            return data?.data
//        }
//        return null
//    }
fun setupImageViewClickListener(imageView: ImageView, context: Context) {
    imageView.setOnClickListener {

    }
}


    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Pair<Int, Uri?> {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_IMAGE_PICKER, REQUEST_CODE_IMAGE_PICKER_CATALOGUE, REQUEST_CODE_VIDEO_PICKER -> {
                    val uri = data?.data
                    return Pair(requestCode, uri)
                }
            }
        }
        return Pair(-1, null)
    }

}



