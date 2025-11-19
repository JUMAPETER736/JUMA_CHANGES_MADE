package com.uyscuti.social.core.common.data.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "notifications")
data class NotificationEntity (
    val name : String,
    val notificationMessage: String,
    val link: String,
    val avatar: String,
    val notificationTime: String,
    val owner: String,
    val isRead : Boolean = false,
//    val colorState: String,
    @PrimaryKey val _id : String,

)