package com.uyscuti.social.network.api.request.posts

import com.uyscuti.social.network.api.request.posts.Data

data class PostRequest(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)