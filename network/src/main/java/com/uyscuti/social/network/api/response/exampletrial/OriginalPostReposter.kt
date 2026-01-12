package com.uyscuti.social.network.api.response.exampletrial

import com.uyscuti.social.network.api.response.exampletrial.Avatar

data class OriginalPostReposter(
    val _id: String,
    val avatar: Avatar,
    val createdAt: String,
    val email: String,
    val updatedAt: String,
    val username: String
)