package com.uyscuti.social.network.api.response.business.response.post

data class ProductPost(
    val `data`: List<Post>,
    val message: String,
    val success: Boolean
)