package com.uyscuti.social.network.api.response.chats

import com.uyscuti.social.network.api.response.chats.Data

data class FetchChatResponse(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)