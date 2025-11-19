package com.uyscuti.social.network.api.response.getUnifiedNotification

data class Data(
    val _id: String,
    val avatar: String,
    val createdAt: String,
    val `data`: DataX,
    val message: String,
    val owner: String,
    val read: Boolean,
    val sender: Sender,
    val type: String
)