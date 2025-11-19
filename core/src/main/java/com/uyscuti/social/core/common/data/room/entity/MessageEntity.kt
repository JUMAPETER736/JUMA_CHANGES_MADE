package com.uyscuti.social.core.common.data.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    var chatId: String,
    val userId: String,
    val userName: String,
    val user: UserEntity,
    val text: String,
    val createdAt: Long,
    var imageUrl: String?,
    var voiceUrl: String?,
    var voiceDuration: Int,
    var status: String,
    var videoUrl: String?,
    var audioUrl: String?,
    var docUrl: String?,
    var fileSize: Long,
    var deleted: Boolean = false
    )
