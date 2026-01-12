package com.uyscuti.social.network.api.response.allFeedRepostsPost

import com.uyscuti.social.network.api.response.allFeedRepostsPost.Avatar
import java.io.Serializable

data class RepostedUser(
    val _id: String,
    val avatar: Avatar,
    val createdAt: String,
    val email: String,
    val updatedAt: String,
    val username: String,
    val url: String,
    val type: String
) : Serializable