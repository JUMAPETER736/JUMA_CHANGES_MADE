package com.uyscuti.social.network.api.request.feed

data class FeedTextUploadRequestBody(
    val content: String,
    val contentType: String,
    val tags: MutableList<String> = mutableListOf()

)
