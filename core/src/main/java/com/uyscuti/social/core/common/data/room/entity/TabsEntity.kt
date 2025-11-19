package com.uyscuti.social.core.common.data.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "tabs")
data class TabsEntity(
    @PrimaryKey val id: String,
    val name: String,
    var unreadCount: Int
)
