package com.uyscuti.social.network.api.request.messages

data class SendMessageRequest(
    val content: String? = null,
    val encryptedContent: String? = null,
    val iv: String? = null,
    val ephemeralPublicKey: String? = null,
    val messageType: Int = 1
)