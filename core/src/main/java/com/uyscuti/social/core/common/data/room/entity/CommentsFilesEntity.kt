package com.uyscuti.social.core.common.data.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comment_files_table")
data class CommentsFilesEntity (
    @PrimaryKey val id: String,
    val localPath: String,
    val url: String,
    val isReply: Int = 0,
    val localUpdateId: String,
    val duration: String = "00:00",
    val fileName: String = "",
    val fileSize: String = "",
    val fileType: String = "",
    val numberOfPages: Int = 0,
    val content: String = "",
    val isFeedComment:Boolean = false,
    val parentPosition: Int = 0,
    var uploadId: String? = ""
)