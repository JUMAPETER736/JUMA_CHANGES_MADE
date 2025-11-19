package com.uyscuti.social.network.api.response.favoritefeed

import com.uyscuti.social.network.api.response.allFeedRepostsPost.Post

data class Data(
    val bookmarkedPosts: List<Post>,
    val hasNextPage: Boolean,
    val hasPrevPage: Boolean,
    val limit: Int,
    val nextPage: Any,
    val page: Int,
    val prevPage: Any,
    val serialNumberStartFrom: Int,
    val totalBookmarkedPosts: Int,
    val totalPages: Int
)