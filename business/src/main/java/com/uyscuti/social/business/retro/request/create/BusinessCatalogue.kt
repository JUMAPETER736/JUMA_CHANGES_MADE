package com.uyscuti.social.business.retro.request.create

data class BusinessCatalogue(
    val description: String,
    val features: List<String>,
    val itemName: String
) {
}