package com.uyscuti.social.network.api.response.business.response.product


data class AddProductResponse(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)