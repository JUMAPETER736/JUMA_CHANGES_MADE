package com.uyscuti.social.network.api.response.commentreply.allreplies

data class CommentReply(
    val __v: Int,
    val _id: String,
    val author: Author?,
    val content: String,
    val createdAt: String,
    val isLiked: Boolean,
    val likes: Int,
    val postId: String,
    val updatedAt: String,
    val replies: List<CommentReply>,
    var isExpanded: Boolean = false
)