package com.uyscuti.social.network.api.response.posts

import java.io.Serializable



data class OriginalUser(
    val _id: String,
    val avatar: Avatar,
    val username: String,
    val email: String,
    val createdAt: String,
    val updatedAt: String,
    val coverImage: CoverImage,
    val firstName: String,
    val lastName: String,
    val bio: String,
    val owner: String
): Serializable

