package com.uyscuti.social.network.api.response.getPostRepostTrial

import com.uyscuti.social.network.api.response.getPostRepostTrial.Post

data class Posts(
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