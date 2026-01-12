package com.uyscuti.social.network.api.response.feed.getallfeed

import android.graphics.Bitmap
import java.io.Serializable

data class FeedThumbnail(
    val _id: String,
    val thumbnailUrl: String,
    val thumbnailLocalPath: String,
    val fileId: String = "",
    var fileThumbnail: Bitmap? = null
): Serializable
