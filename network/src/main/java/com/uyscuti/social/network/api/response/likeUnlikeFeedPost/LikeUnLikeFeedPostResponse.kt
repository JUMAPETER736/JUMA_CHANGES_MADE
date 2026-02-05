package com.uyscuti.social.network.api.response.likeUnlikeFeedPost
import com.uyscuti.social.network.api.response.posts.Post

data class LikeUnLikeFeedPostResponse(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)
