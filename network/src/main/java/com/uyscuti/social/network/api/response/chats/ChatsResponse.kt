package com.uyscuti.social.network.api.response.chats

import com.uyscuti.social.network.api.models.Chat

data class ChatsResponse(
    val data: List<Chat>,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)
