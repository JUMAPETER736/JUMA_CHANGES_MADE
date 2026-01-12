package com.uyscuti.social.network.api.response.getfeedreposts

data class Comment(
    val __v: Int,
    val _id: String,
    val audios: List<Any>,
    val author: String,
    val content: String,
    val createdAt: String,
    val docs: List<Any>,
    val images: List<Any>,
    val postId: String,
    val thumbnail: List<Any>,
    val updatedAt: String,
    val videos: List<Any>
)