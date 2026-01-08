package com.uyscuti.social.network.api.request.business.catalogue


data class Data(
    val __v: Int,
    val _id: String,
    val businessProfile: String,
    val createdAt: String,
    val owner: String,
    val products: List<Product>,
    val updatedAt: String
)