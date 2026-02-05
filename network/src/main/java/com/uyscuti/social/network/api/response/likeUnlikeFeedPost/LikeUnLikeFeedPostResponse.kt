package com.uyscuti.social.network.api.response.likeUnlikeFeedPost

data class LikeUnLikeFeedPostResponse(
    val data: LikeUnLikeFeedPostData,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)

data class LikeUnLikeFeedPostData(
    val isLiked: Boolean,
    val likeCount: Int,
    val likedByUserIds: List<String>,
    val likeId: String? = null,
    val postId: String? = null
)