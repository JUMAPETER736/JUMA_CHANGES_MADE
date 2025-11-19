package com.uyscuti.social.network.api.response.feed.getallfeed

import java.io.Serializable

data class Account(
    val _id: String,
    val avatar: Avatar,
    val email: String,
    val username: String
): Serializable