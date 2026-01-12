package com.uyscuti.social.network.api.response.commentreply.allreplies

data class AllCommentReplies(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)