package com.uyscuti.social.network.api.response.allFeedRepostsPost

import java.io.Serializable

data class Avatar(
    val _id: String,
    val localPath: String,
    val url: String,
    val type: String
) : Serializable