package com.uyscuti.social.business.retro.response.get

data class BusinessCatalogue(
    val _id: String,
    val description: String,
    val features: List<String>,
    val itemName: String
)