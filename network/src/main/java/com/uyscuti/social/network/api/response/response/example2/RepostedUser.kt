package com.uyscuti.social.network.api.response.example2

import com.uyscuti.social.network.api.response.example2.Avatar

data class RepostedUser(
    val _id: String,
    val avatar: Avatar,
    val email: String,
    val username: String
)