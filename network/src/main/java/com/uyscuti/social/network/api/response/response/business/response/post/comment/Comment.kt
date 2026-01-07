package com.uyscuti.social.network.api.response.business.response.post.comment

import com.uyscuti.social.network.api.response.comment.allcomments.Author
import com.uyscuti.social.network.api.response.comment.allcomments.CommentFiles
import com.uyscuti.social.network.api.response.commentreply.allreplies.Comment

//data class Comment(
//    val __v: Int,
//    val _id: String,
//    val audios: MutableList<CommentFiles> = mutableListOf(),
//    val author: Author,
//    val content: String,
//    val contentType: String,
//    val createdAt: String,
//    val docs: MutableList<CommentFiles> = mutableListOf(),
//    val images: MutableList<CommentFiles> = mutableListOf(),
//    val isLiked: Boolean,
//    val likes: Int,
//    val localUpdateId: String,
//    val postId: String,
//    val replies: MutableList<Comment> = mutableListOf(),
//    val replyCount: Int,
//    val thumbnail: MutableList<CommentFiles> = mutableListOf(),
//    val updatedAt: String,
//    val videos: MutableList<CommentFiles> = mutableListOf(),
//    var duration: String? = "00:00",
//    var numberOfPages: String? = "0",
//    var fileSize: String? = "0B",
//    var fileType: String? = "unknown",
//    var fileName: String? = "unknown",
//    var uploadId: String? = "",
//    var replyCountVisible: Boolean = true,
//    val gifs: String? = "",
//    var progress: Float = 0f,
//    var isPlaying: Boolean = false,
//    var isReplyPlaying: Boolean = false,
//    var isRepliesVisible: Boolean = false,
//)