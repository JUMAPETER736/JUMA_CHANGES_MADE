package com.uyscuti.social.network.api.response.posts

data class Bookmark(
    val __v: Int,
    val _id: String,
    val bookmarkedBy: String,
    val createdAt: String,
    val postId: String,
    val updatedAt: String
)