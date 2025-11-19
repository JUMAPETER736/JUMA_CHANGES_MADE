package com.uyscuti.social.core.common.data.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comments_reply_table")
data class ShortCommentReply(
    @PrimaryKey val commentId: String,
    val content: String,
    val localUpdateId: String,
    val isFeedCommentReply: Boolean = false
)
