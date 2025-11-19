package com.uyscuti.social.network.api.response.getallshorts

import java.io.Serializable

data class Image(
    val _id: String,
    val localPath: String,
    val url: String
): Serializable