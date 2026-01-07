package com.uyscuti.social.network.api.response.commentreply.allreplies

import com.uyscuti.social.network.api.response.commentreply.allreplies.Comment

data class Data(
    val comments: MutableList<Comment>,
    val hasNextPage: Boolean,
    val hasPrevPage: Boolean,
    val limit: Int,
    val nextPage: Int,
    val page: Int,
    val prevPage: Any,
    val serialNumberStartFrom: Int,
    val totalReplyComments: Int,
    val totalPages: Int
)