package com.uyscuti.social.circuit.model.feed.multiple_files

import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class FeedMultipleVideos(
    val videoPath: String,
    val videoDuration: String,
    val fileName: String,
    val videoUri: String,
    var thumbnail: Bitmap?,

):Parcelable, Serializable