package com.uyscuti.social.network.api.models

import com.uyscuti.social.network.api.models.Avatar
import java.io.Serializable
import java.util.Date

data class User(
    val _id: String,
    val avatar: Avatar,
    val email: String,
    val isEmailVerified: Boolean,
    val role: String,
    val username: String,
    val lastseen: Date
): Serializable