package com.uyscuti.social.network.api.response.createchat

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