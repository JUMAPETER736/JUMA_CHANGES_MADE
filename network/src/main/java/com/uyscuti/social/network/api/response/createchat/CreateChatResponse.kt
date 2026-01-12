package com.uyscuti.social.network.api.response.createchat

data class CreateChatResponse(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)