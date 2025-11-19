package com.uyscuti.social.network.api.response.followPost

data class GetFollowedPost(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)