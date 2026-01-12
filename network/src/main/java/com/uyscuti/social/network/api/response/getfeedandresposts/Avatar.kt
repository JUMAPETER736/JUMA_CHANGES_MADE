package com.uyscuti.social.network.api.response.getfeedandresposts

import java.io.Serializable

data class Avatar(
    val _id: String,
    val localPath: String,
    val url: String
):Serializable