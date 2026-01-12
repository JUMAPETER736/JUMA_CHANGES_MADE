package com.uyscuti.social.network.api.response.getallshorts

import java.io.Serializable

data class Thumbnail(
    val _id: String,
    val thumbnailLocalPath: String,
    val thumbnailUrl: String
): Serializable
