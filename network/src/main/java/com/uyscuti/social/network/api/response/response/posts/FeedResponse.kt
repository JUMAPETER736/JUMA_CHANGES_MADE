package com.uyscuti.social.network.api.response.posts

data class FeedResponse(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)