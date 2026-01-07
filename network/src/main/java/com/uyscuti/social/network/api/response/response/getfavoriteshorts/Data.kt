package com.uyscuti.social.network.api.response.getfavoriteshorts

import com.uyscuti.social.network.api.response.getfavoriteshorts.BookmarkedPost

data class Data(
    val bookmarkedPosts: List<BookmarkedPost>,
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