package com.uyscuti.social.circuit.model.feed

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

data class RefreshFeedData(val position: Int, val booleanValue: Boolean)

@Parcelize
data class FeedMultipleImages(
    var imagePath: String = "",
    var compressedImagePath:String = ""
): Parcelable, Serializable

@Parcelize
data class Duration(
    var fileId:String,
    var duration: String
): Parcelable, Serializable

@Parcelize
data class NumberOfPages(
    var fileId: String,
    var numberOfPages: String
): Parcelable, Serializable

@Parcelize
data class FileType (
    var fileId: String,
    var fileType: String
): Parcelable, Serializable

@Parcelize
data class FileName(
    var fileId: String,
    var fileName: String
): Parcelable, Serializable

@Parcelize
data class FileSize (
    var fileId: String,
    var fileSize: String
): Parcelable, Serializable

@Parcelize
data class Thumbnail (
    var fileId: String,
    var thumbnail: String
): Parcelable, Serializable

data class ThumbnailWithString(
    val bitmap: Bitmap,
    val fileName: String // This string can represent any metadata or description you need
)

data class SetAllFragmentScrollPosition(
    var setPosition: Boolean = false,
    var allFragmentFeedPosition: Int = -1
)
