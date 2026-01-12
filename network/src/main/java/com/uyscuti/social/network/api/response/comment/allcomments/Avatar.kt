package com.uyscuti.social.network.api.response.comment.allcomments

import java.io.Serializable

data class Avatar(
    val _id: String,
    val localPath: String,
    val url: String
) : Serializable
