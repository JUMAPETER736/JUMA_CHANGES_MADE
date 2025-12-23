package com.uyscuti.social.network.api.response.getfeedreposts.more_feed_data_classes

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

data class RefreshFeedData(val position: Int, val booleanValue: Boolean)

@Parcelize
data class FeedMultipleImages(
    var imagePath: String = "",
    var compressedImagePath: String = ""
): Parcelable, Serializable

@Parcelize
data class Duration(
    var fileId:String,
    var duration: String
): Parcelable, Serializable

@Parcelize
data class NumberOfPages(
    var fileId: String,
    var numberOfPage: String
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