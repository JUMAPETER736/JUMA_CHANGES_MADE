package com.uyscuti.social.network.api.request.posts

data class Account(
    val _id: String,
    val avatar: Avatar,
    val email: String,
    val username: String
)