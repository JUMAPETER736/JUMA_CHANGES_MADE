package com.uyscuti.social.network.api.response.getPageComment

import com.uyscuti.social.network.api.response.getPageComment.Comment
import com.uyscuti.social.network.api.response.getPageComment.CommentX

data class Data(
    val comment: Comment,
    val comments: List<CommentX>,
    val pageNumber: Int
)