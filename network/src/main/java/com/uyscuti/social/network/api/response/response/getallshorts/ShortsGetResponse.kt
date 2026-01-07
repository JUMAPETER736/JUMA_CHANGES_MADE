package com.uyscuti.social.network.api.response.getallshorts

data class ShortsGetResponse(
    val `data`: DataEntity,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)
