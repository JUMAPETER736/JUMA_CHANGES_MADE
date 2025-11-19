package com.uyscuti.social.business.retro.model

import com.uyscuti.social.business.retro.model.Avatar
import com.uyscuti.social.network.api.response.getrepostsPostsoriginal.Account
import java.io.Serializable
import java.util.Date

data class User(
    val account: Account? = null,
    val _id: String,
    val avatar: Avatar,
    val email: String,
    val isEmailVerified: Boolean,
    val role: String,
    val username: String,
    val lastseen: Date
): Serializable