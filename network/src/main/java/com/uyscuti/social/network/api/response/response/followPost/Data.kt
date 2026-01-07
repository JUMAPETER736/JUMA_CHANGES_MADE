package com.uyscuti.social.network.api.response.followPost

import com.uyscuti.social.network.api.response.getrepostsPostsoriginal.Post

data class Data(
    val followedPosts: List<Post>,
    val hasNextPage: Boolean,
    val hasPrevPage: Boolean,
    val limit: Int,
    val nextPage: Any,
    val page: Int,
    val prevPage: Any,
    val serialNumberStartFrom: Int,
    val totalFollowedPosts: Int,
    val totalPages: Int
)