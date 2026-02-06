package com.uyscuti.social.network.api.response.shareFeedPosts

data class ShareUnShareFeedPostResponse(
    val data: ShareUnShareData,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)

data class ShareUnShareData(
    val isShared: Boolean,
    val shareCount: Int,
    val sharedByUserIds: List<String>,
    val shareId: String? = null,
    val postId: String? = null,
    val sharedBy: String? = null,
    val sharedAt: String? = null
)