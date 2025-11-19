package com.uyscuti.social.network.api.ai

import com.uyscuti.social.network.api.response.allFeedRepostsPost.Avatar

data class Sender(
    val _id: String,
    val avatar: Avatar,
    val email: String,
    val username: String
)