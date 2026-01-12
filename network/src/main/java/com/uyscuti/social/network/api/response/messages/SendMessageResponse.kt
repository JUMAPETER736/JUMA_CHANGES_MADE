package com.uyscuti.social.network.api.response.messages

import com.uyscuti.social.network.api.models.Message

data class SendMessageResponse(
    val `data`: Message,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)