package com.uyscuti.social.network.api.response.business.response.post.comment

data class DataX(
    val comments: List<Comment>,
    val hasNextPage: Boolean,
    val hasPrevPage: Boolean,
    val limit: Int,
    val nextPage: Any,
    val page: Int,
    val prevPage: Any,
    val serialNumberStartFrom: Int,
    val totalComments: Int,
    val totalPages: Int
)