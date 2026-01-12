package com.uyscuti.social.network.api.response.commentreply.allreplies

import java.io.Serializable

data class Account(
    val _id: String,
    val avatar: Avatar,
    val email: String,
    val username: String
):Serializable