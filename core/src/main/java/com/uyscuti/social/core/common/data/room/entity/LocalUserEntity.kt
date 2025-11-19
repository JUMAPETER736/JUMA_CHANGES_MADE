package com.uyscuti.social.core.common.data.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "local_user")
data class LocalUserEntity (
    @PrimaryKey val _id: String,
    val avatar: String,
    val username: String,
    val email: String,
    val role: String,
    val loginType: String,
    val isEmailVerified: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val accessToken: String,
    val refreshToken: String
    )