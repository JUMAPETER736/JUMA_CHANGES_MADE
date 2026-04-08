package com.uyscuti.social.network.api.response.comment

import com.uyscuti.social.network.api.models.Comment

data class CommentLocationResponse(
    val success: Boolean,
    val `data`: CommentLocationData,
    val message: String
)


data class CommentLocationData(
    val comments: List<Comment>,
    val totalComments: Int,
    val limit: Int,
    val page: Int,
    val totalPages: Int,
    val serialNumberStartFrom: Int,
    val hasPrevPage: Boolean,
    val hasNextPage: Boolean,
    val prevPage: Int?,
    val nextPage: Int?,
    val location: CommentLocation
)


data class CommentLocation(
    val commentId: String,
    val pageNumber: Int,
    val positionInPage: Int,
    val isReply: Boolean,
    val parentCommentId: String?,
    val parentPageNumber: Int?
)
