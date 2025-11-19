package com.uyscuti.social.network.api.response.getCommentNotification

data class Data(
    val __v: Int,
    val _id: String,
    val avatar: String,
    val createdAt: String,
    val message: String,
    val owner: String,
    val postId: String,
    val commentId:String?,
    val read: Boolean,
    val sender: Sender,
    val updatedAt: String,
//    val colorState: String

)