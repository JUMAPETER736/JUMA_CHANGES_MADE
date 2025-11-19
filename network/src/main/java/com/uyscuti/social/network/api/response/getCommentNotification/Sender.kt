package com.uyscuti.social.network.api.response.getCommentNotification

import com.uyscuti.social.network.api.response.getCommentNotification.Avatar

data class Sender(
    val _id: String,
    val avatar: Avatar,
    val email: String,
    val username: String
)