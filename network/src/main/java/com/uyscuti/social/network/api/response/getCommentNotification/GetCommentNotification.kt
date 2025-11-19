package com.uyscuti.social.network.api.response.getCommentNotification

import com.uyscuti.social.network.api.response.getCommentNotification.Data

data class GetCommentNotification(
    val `data`: List<Data>,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)