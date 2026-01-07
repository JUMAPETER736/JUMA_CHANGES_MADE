package com.uyscuti.social.network.api.response.feed.getallfeed

import java.io.Serializable

data class CoverImage(
    val _id: String,
    val localPath: String,
    val url: String
): Serializable