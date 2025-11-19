package com.uyscuti.social.network.api.request.business.product

data class AddProductRequest(
    val description: String,
    val features: List<String>,
    val itemName: String,
    val price: String
)