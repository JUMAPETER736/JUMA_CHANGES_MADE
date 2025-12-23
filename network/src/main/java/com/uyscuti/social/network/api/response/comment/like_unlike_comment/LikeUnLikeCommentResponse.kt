package com.uyscuti.social.network.api.response.comment.like_unlike_comment



data class LikeUnLikeCommentResponse(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)