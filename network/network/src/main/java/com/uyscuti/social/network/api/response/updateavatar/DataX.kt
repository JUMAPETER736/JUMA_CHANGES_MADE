package com.uyscuti.social.network.api.response.updateavatar

import com.uyscuti.social.network.api.response.updateavatar.AvatarX

data class DataX(
    val __v: Int,
    val _id: String,
    val avatar: AvatarX,
    val createdAt: String,
    val email: String,
    val emailVerificationExpiry: String,
    val emailVerificationToken: String,
    val isEmailVerified: Boolean,
    val loginType: String,
    val password: String,
    val refreshToken: String,
    val role: String,
    val updatedAt: String,
    val username: String
)