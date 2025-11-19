package com.uyscuti.social.network.api.response.notification

import com.uyscuti.social.network.api.response.notification.Avatar


data class Sender(
    val _id: String,
    val avatar: Avatar,
    val email: String,
    val username: String
)