package com.uyscuti.social.network.api.response.shorts

import com.uyscuti.social.network.api.response.shorts.Data

data class ShortsUploadResponse(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)