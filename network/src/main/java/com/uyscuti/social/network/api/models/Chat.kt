package com.uyscuti.social.network.api.models

data class Chat(
    val admin: String?,
    val createdAt: String,
    val isGroupChat: Boolean,
    val lastMessage: Message?,
    val name: String,
    val participants: List<User>,
    val updatedAt: String,
    val _id: String
)
