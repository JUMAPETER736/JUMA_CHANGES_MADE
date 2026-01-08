package com.uyscuti.social.network.api.response.business.response.post.comment

import com.uyscuti.social.network.api.response.comment.allcomments.CommentFiles

data class Data(
    val __v: Int,
    val _id: String,
    val audios: MutableList<CommentFiles> = mutableListOf(),
    val author: String,
    val content: String,
    val contentType: String,
    val createdAt: String,
    val docs: MutableList<CommentFiles> = mutableListOf(),
    val images: MutableList<CommentFiles> = mutableListOf(),
    val localUpdateId: String,
    val gifs: String,
    val postId: String,
    val thumbnail: MutableList<CommentFiles> = mutableListOf(),
    val updatedAt: String,
    val videos: MutableList<CommentFiles> = mutableListOf()
)