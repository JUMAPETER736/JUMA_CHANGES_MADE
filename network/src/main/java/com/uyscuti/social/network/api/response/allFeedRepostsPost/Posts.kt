package com.uyscuti.social.network.api.response.allFeedRepostsPost

import com.uyscuti.social.network.api.response.allFeedRepostsPost.Post

data class Posts(
    val hasNextPage: Boolean,
    val hasPrevPage: Boolean,
    val limit: Int,
    val nextPage: Any,
    val page: Int,
    val posts: List<Post>,
    val prevPage: Any,
    val serialNumberStartFrom: Int,
    val totalPages: Int,
    val totalPosts: Int
)