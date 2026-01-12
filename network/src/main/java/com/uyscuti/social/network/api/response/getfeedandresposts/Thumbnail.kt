package com.uyscuti.social.network.api.response.getfeedandresposts

import java.io.Serializable

data class Thumbnail(
    val _id: String,
    val fileId: String,
    val thumbnailLocalPath: String,
    val thumbnailUrl: String


):Serializable