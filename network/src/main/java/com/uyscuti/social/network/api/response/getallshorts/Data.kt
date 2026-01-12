package com.uyscuti.social.network.api.response.getallshorts

import java.io.Serializable

data class Data(
    val hasNextPage: Boolean,
    val hasPrevPage: Boolean,
    val limit: Int,
    val nextPage: Int,
    val page: Int,
    val posts: List<Post>,
//    val followList: List<FollowListItem>,
    val prevPage: Any,
    val serialNumberStartFrom: Int,
    val totalPages: Int,
    val totalPosts: Int
):Serializable