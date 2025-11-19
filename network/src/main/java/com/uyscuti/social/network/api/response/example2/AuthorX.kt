package com.uyscuti.social.network.api.response.example2

import java.io.Serializable

data class AuthorX(
    val _id: String,
    val avatar: Avatar,
    val email: String,
    val username: String
): Serializable