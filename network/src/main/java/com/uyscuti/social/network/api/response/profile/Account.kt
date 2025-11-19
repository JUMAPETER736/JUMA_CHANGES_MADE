package com.uyscuti.social.network.api.response.profile

data class Account(
    val _id: String,
    val avatar: Avatar,
    val email: String,
    val isEmailVerified: Boolean,
    val loginType: String,
    val role: String,
    val username: String
)