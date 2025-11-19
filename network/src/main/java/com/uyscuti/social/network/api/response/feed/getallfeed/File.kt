package com.uyscuti.social.network.api.response.feed.getallfeed

import java.io.Serializable

data class File(
    val _id: String,
    val localPath: String,
    val url: String,
    val fileId: String = ""
): Serializable