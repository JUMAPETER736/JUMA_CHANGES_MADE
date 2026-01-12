package com.uyscuti.social.network.api.response.gif.allgifs

import com.uyscuti.social.network.api.response.comment.allcomments.CommentFiles

data class GifModel(
    val fileType:String,
    val gifs: MutableList<CommentFiles>
)
