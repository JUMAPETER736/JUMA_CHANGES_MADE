package com.uyscuti.social.network.api.response.allFeedRepostsPost

data class MediaFile(
    val _id: String,
    val mineType: FileType,
    val name: String?,
    val size: Long?,
    val url: String,
    val type: String
)