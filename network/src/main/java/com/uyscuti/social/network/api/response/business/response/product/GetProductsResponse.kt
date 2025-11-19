package com.uyscuti.social.network.api.response.business.response.product

import com.uyscuti.social.network.api.response.business.response.product.DataX

data class GetProductsResponse(
    val `data`: List<DataX>,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)