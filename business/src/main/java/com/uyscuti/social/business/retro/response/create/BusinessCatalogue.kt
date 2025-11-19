package com.uyscuti.social.business.retro.response.create

data class BusinessCatalogue(
    val _id: String,
    val description: String,
    val features: List<String>,
    val itemName: String
)