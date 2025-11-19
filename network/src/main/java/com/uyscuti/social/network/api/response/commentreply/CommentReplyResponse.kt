package com.uyscuti.social.network.api.response.commentreply

data class CommentReplyResponse(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)