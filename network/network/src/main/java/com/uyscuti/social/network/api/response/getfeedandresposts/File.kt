package com.uyscuti.social.network.api.response.getfeedandresposts

import java.io.Serializable

data class File(
    val _id: String,
    val fileId: String,
    val localPath: String,
    val url: String
):Serializable