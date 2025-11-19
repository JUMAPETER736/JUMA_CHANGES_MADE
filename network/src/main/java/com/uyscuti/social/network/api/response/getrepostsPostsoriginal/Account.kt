package com.uyscuti.social.network.api.response.getrepostsPostsoriginal

import java.io.Serializable

data class Account(

    val _id: String,
    val avatar: Avatar,
    val createdAt: String,
    val email: String,
    val updatedAt: String,
    val username: String
):Serializable