package com.uyscuti.social.network.api.models

import java.io.Serializable

data class Message(
    val _id: String,
    val sender: User,
    val content: String?,
    val chat: String,
    val attachments: List<Attachment>?,
    val createdAt: String,
    val updatedAt: String,
    val isEncrypted: Boolean? = false,
    val encryptedContent: String? = null,
    val iv: String? = null,
    val ephemeralPublicKey: String? = null,
    val messageType: Int? = 1
) : Serializable