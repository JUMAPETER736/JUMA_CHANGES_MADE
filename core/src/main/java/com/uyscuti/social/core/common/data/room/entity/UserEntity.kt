package com.uyscuti.social.core.common.data.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val avatar: String,
    val lastSeen: Date,
    val online: Boolean
)