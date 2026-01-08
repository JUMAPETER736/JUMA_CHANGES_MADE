package com.uyscuti.social.network.api.request.group

import com.uyscuti.social.network.api.models.Message
import com.uyscuti.social.network.api.models.User

data class GroupChat(
    val admin: String?,
    val createdAt: String,
    val isGroupChat: Boolean,
    val lastMessage: Message?,
    val name: String,
    val participants: List<User>,
    val updatedAt: String,
    val _id: String
)
