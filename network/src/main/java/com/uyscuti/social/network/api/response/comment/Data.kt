package com.uyscuti.social.network.api.response.comment

data class Data(
    val __v: Int,
    val _id: String,
    val author: String,
    val content: String,
    val createdAt: String,
    val postId: String,
    val updatedAt: String,
    val localUpdateId: String = ""
)