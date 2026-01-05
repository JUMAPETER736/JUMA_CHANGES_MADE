package com.uyscuti.social.network.api.response.business.response.get

data class BusinessCatalogue(
    val _id: String,
    val description: String,
    val features: List<String>,
    val itemName: String
)