package com.uyscuti.sharedmodule.model.business

import com.uyscuti.social.network.api.response.getrepostsPostsoriginal.Account
import com.uyscuti.social.network.api.response.getrepostsPostsoriginal.Avatar
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