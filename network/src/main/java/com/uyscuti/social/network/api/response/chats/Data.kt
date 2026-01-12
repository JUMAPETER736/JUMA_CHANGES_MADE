package com.uyscuti.social.network.api.response.chats

data class Data(
    val __v: Int,
    val _id: String,
    val admin: String,
    val createdAt: String,
    val isGroupChat: Boolean,
    val name: String,
    val participants: List<Participant>,
    val updatedAt: String
)