package com.uyscuti.social.network.api.request.group

import com.uyscuti.social.network.api.request.group.Avatar

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