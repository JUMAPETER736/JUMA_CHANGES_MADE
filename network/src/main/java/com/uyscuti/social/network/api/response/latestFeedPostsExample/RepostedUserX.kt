package com.uyscuti.social.network.api.response.latestFeedPostsExample

import com.uyscuti.social.network.api.response.latestFeedPostsExample.Avatar

data class RepostedUserX(
    val _id: String,
    val avatar: Avatar,
    val createdAt: String,
    val email: String,
    val updatedAt: String,
    val username: String
)