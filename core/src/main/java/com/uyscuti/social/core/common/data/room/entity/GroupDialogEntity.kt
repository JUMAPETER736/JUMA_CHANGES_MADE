package com.uyscuti.social.core.common.data.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.uyscuti.social.core.common.data.room.converters.MessageConverter
import com.uyscuti.social.core.common.data.room.converters.UserConverter


@Entity(tableName = "group_dialogs")
@TypeConverters(UserConverter::class, MessageConverter::class)
data class GroupDialogEntity (
    @PrimaryKey
    val id: String,
    val adminId: String,
    val adminName: String,
    val dialogPhoto: String,
    val dialogName: String,
    val users: List<UserEntity>,
    var lastMessage: MessageEntity,
    var unreadCount: Int,
    val createdAt: Long,
    val updatedAt: Long
)