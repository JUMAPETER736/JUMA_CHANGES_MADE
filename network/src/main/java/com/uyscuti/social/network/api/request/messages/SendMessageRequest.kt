package com.uyscuti.social.network.api.request.messages

data class SendMessageRequest(
    val content: String?,
    val attachments: String?
)