package com.uyscuti.social.network.api.response.comment

import com.uyscuti.social.network.api.response.comment.Data

data class ShortCommentResponse(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)