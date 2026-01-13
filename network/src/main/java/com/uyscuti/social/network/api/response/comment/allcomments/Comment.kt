package com.uyscuti.social.network.api.response.comment.allcomments

import com.uyscuti.social.network.api.response.comment.allcomments.Author
import com.uyscuti.social.network.api.response.comment.allcomments.CommentFiles

data class Comment(
    val __v: Int,
    var _id: String,
    var author: Author?,
    var content: String,
    val contentType: String = "",
    var createdAt: String,
    var isLiked: Boolean,
    var likes: Int,
    val postId: String,
    val updatedAt: String,
    var replyCount: Int,
    val audios: MutableList<CommentFiles> = mutableListOf(),
    val images: MutableList<CommentFiles> = mutableListOf(),
    val videos: MutableList<CommentFiles> = mutableListOf(),
    val docs: MutableList<CommentFiles> = mutableListOf(),
    val thumbnail: MutableList<CommentFiles> = mutableListOf(),
    val gifs: String? = "",
    val duration: String? = "00:00",
    var numberOfPages: String? = "0",
    var fileSize: String? = "0B",
    var fileType: String? = "unknown",
    var fileName: String? = "unknown"
)