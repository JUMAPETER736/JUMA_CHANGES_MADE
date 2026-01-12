package com.uyscuti.social.business.model

import java.io.Serializable
import java.util.Date

data class User(
    val _id: String,
    val avatar: String? = null,
    val email: String,
    val isEmailVerified: Boolean,
    val role: String,
    val username: String,
    val lastseen: Date
):Serializable