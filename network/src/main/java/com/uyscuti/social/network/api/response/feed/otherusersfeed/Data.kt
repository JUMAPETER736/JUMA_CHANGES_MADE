package com.uyscuti.social.network.api.response.feed.otherusersfeed

data class Data(
    val hasNextPage: Boolean,
    val hasPrevPage: Boolean,
    val limit: Int,
    val nextPage: Int,
    val page: Int,
    val posts: List<Post>,
    val prevPage: Any,
    val serialNumberStartFrom: Int,
    val totalPages: Int,
    val totalPosts: Int
)