package com.uyscuti.social.network.api.response.backApiResponse

import com.uyscuti.social.network.api.response.backApiResponse.Avatar

data class OriginalPostReposter(
    val _id: String,
    val avatar: Avatar,
    val createdAt: String,
    val email: String,
    val updatedAt: String,
    val username: String
)