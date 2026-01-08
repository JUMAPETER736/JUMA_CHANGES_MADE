package com.uyscuti.social.network.api.response.chats

import com.uyscuti.social.network.api.response.chats.Avatar

data class Participant(
    val __v: Int,
    val _id: String,
    val avatar: Avatar,
    val createdAt: String,
    val email: String,
    val isEmailVerified: Boolean,
    val loginType: String,
    val role: String,
    val updatedAt: String,
    val username: String
)