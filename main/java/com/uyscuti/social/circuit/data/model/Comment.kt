package com.uyscuti.social.circuit.data.model
import com.uyscuti.social.network.api.response.comment.allcomments.Author
import com.uyscuti.social.network.api.response.comment.allcomments.CommentFiles
import java.io.Serializable

data class Comment(
    var __v: Int,
    var _id: String,
    val author: Author?,
    val content: String,
    val createdAt: String,
    var isLiked: Boolean,
    var likes: Int,
    val postId: String,
    val updatedAt: String,
    val replyCount: Int,
    val replies: MutableList<com.uyscuti.social.network.api.response.commentreply.allreplies.Comment> = mutableListOf(),
    var isRepliesVisible: Boolean = false,
    var hasNextPage: Boolean = true,
    var pageNumber: Int = 1,
    val contentType: String = "",
    val audios: MutableList<CommentFiles> = mutableListOf(),
    val images: MutableList<CommentFiles> = mutableListOf(),
    val videos: MutableList<CommentFiles> = mutableListOf(),
    val docs: MutableList<CommentFiles> = mutableListOf(),
    val thumbnail: MutableList<CommentFiles> = mutableListOf(),
    val gifs: String = "",
    var progress: Float = 0f,
    var isPlaying: Boolean = false,
    var isReplyPlaying: Boolean = false,
    val localUpdateId: String,
    var replyCountVisible: Boolean = true,
    var duration: String = "00:00",
    var numberOfPages: String = "0",
    var fileSize: String = "0B",
    var fileType: String = "unknown",
    var fileName: String = "unknown",
    var uploadId: String? = ""

):Serializable