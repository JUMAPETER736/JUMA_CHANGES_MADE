package com.uyscuti.social.network.api.response.comment.allcomments

data class AllShortComments(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)