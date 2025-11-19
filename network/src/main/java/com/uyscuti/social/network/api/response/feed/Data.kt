package com.uyscuti.social.network.api.response.feed

data class Data(
    val __v: Int,
    val _id: String,
    val author: Author,
    val comments: Int,
    val content: String,
    val createdAt: String,
    val files: List<Image>,
    val isBookmarked: Boolean,
    val isLiked: Boolean,
    val likes: Int,
    val tags: List<String>,
    val updatedAt: String
)