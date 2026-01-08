package com.uyscuti.social.network.api.response.feed

import com.uyscuti.social.network.api.response.feed.Data

data class FeedUploadResponse(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)