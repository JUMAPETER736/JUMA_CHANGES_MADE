package com.uyscuti.social.network.api.response.comment.allcomments

import com.uyscuti.social.network.api.response.comment.allcomments.Comment

data class Data(
    val comments: List<Comment>,
    val hasNextPage: Boolean,
    val hasPrevPage: Boolean,
    val limit: Int,
    val nextPage: Int,
    val page: Int,
    val prevPage: Any,
    val serialNumberStartFrom: Int,
    val totalComments: Int,
    val totalPages: Int
//    val totalReplies: Int? = 0
)