package com.uyscuti.social.network.api.response.otherusersprofile

import java.io.Serializable

data class Account(
    val _id: String,
    val avatar: Avatar,
    val email: String,
    val isEmailVerified: Boolean,
    val username: String
) : Serializable