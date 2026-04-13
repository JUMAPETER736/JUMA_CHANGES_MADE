package com.uyscuti.social.network.api.response.business.response.post

data class BusinessPost(
    val `data`: Data,
    val currentPage: Int,
    val totalPages: Int,
    val hasNextPage: Boolean
)

