package com.uyscuti.social.business.retro.response.catalogue


data class Data(
    val __v: Int,
    val _id: String,
    val businessProfile: String,
    val createdAt: String,
    val owner: String,
    val products: List<Product>,
    val updatedAt: String
)