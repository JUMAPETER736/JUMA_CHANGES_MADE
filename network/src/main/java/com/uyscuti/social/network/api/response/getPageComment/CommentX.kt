package com.uyscuti.social.network.api.response.getPageComment

import com.uyscuti.social.network.api.response.comment.allcomments.CommentFiles

import com.uyscuti.social.network.api.response.commentreply.allreplies.Comment
import com.uyscuti.social.network.api.response.comment.allcomments.Author

data class CommentX(
    val __v: Int,
    val _id: String,
    val audios: MutableList<CommentFiles> = mutableListOf(),
    val author: Author?,
    val content: String,
    val contentType: String,
    val createdAt: String,
    val docs: MutableList<CommentFiles> = mutableListOf(),
    val duration: String,
    val fileName: String,
    val fileType: String,
    val images: MutableList<CommentFiles> = mutableListOf(),
    val isLiked: Boolean,
    val likes: Int,
    val localUpdateId: String,
    val postId: String,
    val replies: MutableList<Comment> = mutableListOf(),
    val replyCount: Int,
    val thumbnail: MutableList<CommentFiles> = mutableListOf(),
    val updatedAt: String,
    val videos: MutableList<CommentFiles> = mutableListOf()
)