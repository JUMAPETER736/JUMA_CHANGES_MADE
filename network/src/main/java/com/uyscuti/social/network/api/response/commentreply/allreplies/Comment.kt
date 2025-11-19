package com.uyscuti.social.network.api.response.commentreply.allreplies

import com.uyscuti.social.network.api.response.comment.allcomments.CommentFiles
import com.uyscuti.social.network.api.response.commentreply.allreplies.Author
import java.io.Serializable

data class Comment(
    var __v: Int,
    var _id: String,
    val author: Author?,
    val content: String,
    val contentType: String = "",
    val createdAt: String,
    var isLiked: Boolean,
    var likes: Int,
    val commentId: String,
    val updatedAt: String,
    val audios: MutableList<CommentFiles> = mutableListOf(),
    val images: MutableList<CommentFiles> = mutableListOf(),
    val videos: MutableList<CommentFiles> = mutableListOf(),
    val docs: MutableList<CommentFiles> = mutableListOf(),
    val thumbnail: MutableList<CommentFiles> = mutableListOf(),
    val gifs: String? =  "",
    var progress: Float = 0f,
    var isPlaying: Boolean = false,
    var secondWaveProgress:Float = 0f,
    var duration: String = "00:00",
    var numberOfPages: String? = "0",
    var fileSize: String? = "0B",
    var fileType: String? = "unknown",
    var fileName: String? = "unknown",
    var uploadId: String? = ""
    ): Serializable