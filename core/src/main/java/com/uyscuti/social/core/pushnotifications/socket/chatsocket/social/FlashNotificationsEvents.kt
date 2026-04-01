package com.uyscuti.social.core.pushnotifications.socket.chatsocket.social

data class FlashNotificationsEvents(
    val name: String,
    val notificationMessage: String,
    val link: String,
    val type: String,
    val notificationTime: String,
    val avatar :String = "",
    val _id : String,
    val owner : String,
    val isRead : Boolean = false,
    val postId : String,
    val commentId:String,
    val noteFor: String = ""
)