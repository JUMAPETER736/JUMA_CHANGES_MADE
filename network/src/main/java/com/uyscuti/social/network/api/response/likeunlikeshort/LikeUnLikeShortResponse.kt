package com.uyscuti.social.network.api.response.likeunlikeshort

import com.uyscuti.social.network.api.response.likeunlikeshort.Data

data class LikeUnLikeShortResponse(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)


data class LikeData(
    val isLiked: Boolean,
    val likeCount: Int,
    val likedByUserIds: List<String>,
    val likeId: String? = null,
    val postId: String? = null,
    val commentId: String? = null,
    val commentReplyId: String? = null
)