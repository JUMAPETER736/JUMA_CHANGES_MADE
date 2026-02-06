package com.uyscuti.social.network.api.response.shareFeedPosts

data class FeedPostShareResponse(
    val `data`: FeedPostShareData,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)

data class FeedPostShareData(
    val postId: String,
    val isShared: Boolean,
    val shareCount: Int,
    val sharedBy: String? = null,
    val sharedAt: String? = null
)