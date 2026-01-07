package com.uyscuti.social.network.api.response.backApiResponse

data class BackUpApiResponseOfFeed(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)