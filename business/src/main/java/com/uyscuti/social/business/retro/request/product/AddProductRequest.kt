package com.uyscuti.social.business.retro.request.product

data class AddProductRequest(
    val description: String,
    val features: List<String>,
    val itemName: String,
    val price: String
)