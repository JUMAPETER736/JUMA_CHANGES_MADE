package com.uyscuti.social.circuit.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

fun getRealPathFromUri(context: Context, uri: Uri): String? {
    val projection = arrayOf(MediaStore.Images.Media.DATA)
    context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
        val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        if (cursor.moveToFirst()) {
            return cursor.getString(columnIndex)
        }
    }
    return null
}

fun getFilePathFromContentUri(context: Context, uri: Uri): String? {
    val contentResolver: ContentResolver = context.contentResolver
    val file = File.createTempFile("temp", null, context.cacheDir)
    try {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return file.absolutePath
    } catch (e: IOException) {
        e.printStackTrace()
        return null
    }
}

fun getFilePathFromUri(uri: Uri): String? {
    return if (uri.scheme == "file") {
        uri.path
    } else {
        null
    }
}
