package com.uyscuti.social.network.api.response.business.response.create

data class BusinessCatalogue(
    val _id: String,
    val description: String,
    val features: List<String>,
    val itemName: String
)