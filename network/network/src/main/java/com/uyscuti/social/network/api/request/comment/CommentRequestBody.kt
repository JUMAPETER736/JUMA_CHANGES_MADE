package com.uyscuti.social.network.api.request.comment

data class CommentRequestBody(
    val content: String,
    val contentType: String,
    val localUpdateId: String
)
