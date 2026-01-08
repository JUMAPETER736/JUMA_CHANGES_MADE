package com.uyscuti.social.network.api.request.business.create

data class BusinessCatalogue(
    val description: String,
    val features: List<String>,
    val itemName: String
) {
}