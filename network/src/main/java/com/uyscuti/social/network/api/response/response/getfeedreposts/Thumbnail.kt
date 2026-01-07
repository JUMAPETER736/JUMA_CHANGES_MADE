package com.uyscuti.social.network.api.response.getfeedreposts

import android.graphics.Bitmap
import java.io.Serializable

data class Thumbnail(
    val _id: String,
    val fileId: String,
    val thumbnailLocalPath: String,
    val thumbnailUrl: String,
    var fileThumbnail: Bitmap? = null
): Serializable