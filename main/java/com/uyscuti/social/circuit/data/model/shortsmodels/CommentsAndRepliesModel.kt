package com.uyscuti.social.circuit.data.model.shortsmodels


data class CommentsAndRepliesModel(
    val mainComment: Comment,
    val commentReply:
    List<com.uyscuti.social.network.api.response.commentreply.allreplies.Comment>?,
)
