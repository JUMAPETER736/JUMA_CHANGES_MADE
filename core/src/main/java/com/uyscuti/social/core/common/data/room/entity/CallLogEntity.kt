package com.uyscuti.social.core.common.data.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_log")
data class CallLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val callerName: String,
    val createdAt: Long,
    val callDuration: Long,
    val callType: String,
    val callStatus: String,
    val callerAvatar: String,
    val callerId: String,
    val isVideoCall: Boolean,
    var isSelected: Boolean
)
