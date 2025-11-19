package com.uyscuti.social.network.api.request.comment

data class GifCommentRequestBody(
    val content: String,
    val contentType: String,
    val localUpdateId: String,
    val gifs: String
)
