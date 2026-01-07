package com.uyscuti.social.network.api.response.getallshorts

data class GetAllShortsResponse(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)