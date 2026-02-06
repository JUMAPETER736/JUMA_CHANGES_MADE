package com.uyscuti.social.network.api.response.repostFeedPosts

data class RepostUnRepostFeedPostResponse(
    val success: Boolean,
    val message: String,
    val repostCount: Int,
    val isReposted: Boolean
)