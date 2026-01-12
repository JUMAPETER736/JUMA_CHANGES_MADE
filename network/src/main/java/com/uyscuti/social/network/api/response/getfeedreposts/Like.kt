package com.uyscuti.social.network.api.response.getfeedreposts

data class Like(
    val __v: Int,
    val _id: String,
    val commentId: Any,
    val commentReplyId: Any,
    val createdAt: String,
    val isRetweet: Boolean,
    val likedBy: String,
    val originalPostId: Any,
    val postId: String,
    val retweetCount: Int,
    val updatedAt: String
)