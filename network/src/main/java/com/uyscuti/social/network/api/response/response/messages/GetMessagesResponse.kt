package com.uyscuti.social.network.api.response.messages

import com.uyscuti.social.network.api.models.Message

data class GetMessagesResponse(
    val `data`: List<Message>,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)
