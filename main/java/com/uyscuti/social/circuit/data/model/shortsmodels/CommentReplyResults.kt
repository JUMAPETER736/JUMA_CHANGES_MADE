package com.uyscuti.social.circuit.data.model.shortsmodels

import com.uyscuti.social.network.api.response.comment.allcomments.Author
import com.uyscuti.social.network.api.response.commentreply.allreplies.Comment

data class CommentReplyResults(
    val comments: MutableList<Comment>,
    val hasNextPage: Boolean,
    val pageNumber: Int
    // Add more properties as needed
)