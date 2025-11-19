package com.uyscuti.social.core.common.data.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "follow_table")

data class FollowUnFollowEntity (
    @PrimaryKey val userId: String,
    var isFollowing: Boolean,
    var isButtonVisible: Boolean = true
)