package com.uyscuti.social.network.api.response.getfeedandresposts

import com.uyscuti.social.network.api.response.getfeedandresposts.Post

data class Posts(
    val posts: List<Post>,
    val totalPosts: Int,
    val limit: Int,
    val page: Int,
    val totalPages: Int,
    val serialNumberStartFrom: Int,
    val hasPrevPage: Boolean,
    val hasNextPage: Boolean,
    val prevPage: Any,
    val nextPage: Any,
)