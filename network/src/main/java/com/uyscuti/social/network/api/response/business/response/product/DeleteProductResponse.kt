package com.uyscuti.social.network.api.response.business.response.product

data class DeleteProductResponse(
    val `data`: Any,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)