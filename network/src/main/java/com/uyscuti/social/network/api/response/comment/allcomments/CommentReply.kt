package com.uyscuti.social.network.api.response.comment.allcomments

data class CommentReply(
    val __v: Int,
    val _id: String,
    val author: Author?,
    val content: String,
    val createdAt: String,
    val isLiked: Boolean,
    val likes: Int,
    val commentId: String,
    val updatedAt: String,
    val replies: List<CommentReply>,
    var isExpanded: Boolean = false
)