package com.uyscuti.social.core.common.data.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shorts_follow_list")
data class ShortsEntityFollowList(
    @PrimaryKey val followersId: String, // Change the type as needed
    var isFollowing: Boolean
)
