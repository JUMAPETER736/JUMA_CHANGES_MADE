package com.uyscuti.social.circuit.model.feed.multiple_files

import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.File
import java.io.Serializable

@Parcelize
data class FeedMultipleDocumentsDataClass(
    var uri: Uri? = null,
    var filename: String = "",
    var numberOfPages: String = "",
    var documentType: String = "",
    var fileSize: String = "",
    var documentThumbnailFilePath: Bitmap? = null,
    var pdfFilePath: String = "",
    var uriFile: File? = null

): Parcelable, Serializable
