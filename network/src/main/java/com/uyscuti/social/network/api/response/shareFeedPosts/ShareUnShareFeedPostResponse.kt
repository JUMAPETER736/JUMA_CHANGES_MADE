package com.uyscuti.social.network.api.response.shareFeedPosts

data class ShareUnShareFeedPostResponse(
    val success: Boolean,
    val message: String,
    val shareCount: Int,
    val isShared: Boolean
)