package com.uyscuti.social.business.retro.response.catalogue

data class Product(
    val __v: Int,
    val _id: String,
    val catalogue: String,
    val createdAt: String,
    val description: String,
    val features: List<String>,
    val images: List<String>,
    val itemName: String,
    val owner: String,
    val updatedAt: String
)