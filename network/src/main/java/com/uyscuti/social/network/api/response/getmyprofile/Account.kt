package com.uyscuti.social.network.api.response.getmyprofile

data class Account(
    val _id: String,
    val avatar: Avatar,
    val email: String,
    val isEmailVerified: Boolean,
    val username: String
)