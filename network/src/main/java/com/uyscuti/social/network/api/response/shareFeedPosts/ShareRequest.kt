package com.uyscuti.social.network.api.response.shareFeedPosts


data class ShareRequest(
    val comment: String? = null,
    val files: List<String>? = null,
    val tags: List<String>? = null
)