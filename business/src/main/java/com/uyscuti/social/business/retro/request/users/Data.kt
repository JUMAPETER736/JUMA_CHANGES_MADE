package com.uyscuti.social.business.retro.request.users

import com.uyscuti.social.business.retro.request.users.Avatar

data class Data(
    val __v: Int,
    val _id: String,
    val avatar: Avatar,
    val createdAt: String,
    val email: String,
    val emailVerificationExpiry: String,
    val emailVerificationToken: String,
    val isEmailVerified: Boolean,
    val lastSeen: String,
    val loginType: String,
    val password: String,
    val refreshToken: String,
    val role: String,
    val updatedAt: String,
    val username: String
)