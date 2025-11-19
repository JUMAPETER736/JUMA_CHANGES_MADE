package com.uyscuti.social.core.pushnotifications.socket.chatsocket.social

import com.uyscuti.social.network.api.models.Avatar

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
)
