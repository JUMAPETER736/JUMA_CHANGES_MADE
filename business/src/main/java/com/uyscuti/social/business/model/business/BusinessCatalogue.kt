package com.uyscuti.social.business.model.business

import java.io.Serializable

data class BusinessCatalogue(
    val id: String,
    val name: String,
    val description: String,
    val price: String,
    val images: List<String>,
    val owner: String,
    val createdAt: String,
): Serializable {
}
