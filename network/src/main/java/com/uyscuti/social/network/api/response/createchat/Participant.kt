package com.uyscuti.social.network.api.response.createchat

import com.uyscuti.social.network.api.response.createchat.Avatar


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