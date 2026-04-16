package com.uyscuti.social.network.api.response.getUnifiedNotification

data class DataX(
    val postId: String,
    val `for`: String,
    val commentId: String? = null,
    val commentReplyId: String? = null
)