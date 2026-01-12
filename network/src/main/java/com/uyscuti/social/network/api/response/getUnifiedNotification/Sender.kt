package com.uyscuti.social.network.api.response.getUnifiedNotification

import com.uyscuti.social.network.api.response.getUnifiedNotification.Avatar

data class Sender(
    val _id: String,
    val avatar: Avatar,
    val email: String,
    val username: String
)