package com.uyscuti.social.core.common.data.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comments_table")
data class ShortCommentEntity(
    @PrimaryKey val postId: String,
    val content: String,
    val localUpdateId: String,
    val isFeedComment: Boolean = false
)
