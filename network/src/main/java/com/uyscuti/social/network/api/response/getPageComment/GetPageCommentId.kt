package com.uyscuti.social.network.api.response.getPageComment

import com.uyscuti.social.network.api.response.getPageComment.Data

data class GetPageCommentId(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)