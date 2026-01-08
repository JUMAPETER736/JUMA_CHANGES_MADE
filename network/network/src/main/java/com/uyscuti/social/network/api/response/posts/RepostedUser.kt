package com.uyscuti.social.network.api.response.posts

data class RepostedUser(
    val _id: String,
    val avatar: Avatar,
    val bio: String,
    val coverImage: CoverImage,
    val createdAt: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val owner: String,
    val updatedAt: String,
    val username: String
)