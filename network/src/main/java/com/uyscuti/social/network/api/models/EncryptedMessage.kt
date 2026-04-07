package com.uyscuti.social.network.api.models

data class EncryptedMessage(
    val encryptedContent: String,
    val iv: String,
    val ephemeralPublicKey: String = "",
    val messageType: Int = 1
)