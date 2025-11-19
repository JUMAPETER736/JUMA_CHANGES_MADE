package com.uyscuti.social.network.api.ai

data class Data(
    val __v: Int,
    val _id: String,
    val attachments: List<Attachment>,
    val chat: String,
    val content: String,
    val createdAt: String,
    val sender: Sender,
    val updatedAt: String
)