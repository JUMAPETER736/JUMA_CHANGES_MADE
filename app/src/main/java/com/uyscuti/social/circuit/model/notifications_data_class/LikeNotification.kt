package com.uyscuti.social.circuit.model.notifications_data_class

import android.util.Log
import com.uyscuti.social.circuit.model.notifications_data_class.INotification

data class LikeNotification(
    override val name: String,
    override val notificationMessage: String,
    override val link: String = "onLiked",
    override val notificationTime: String,
    override val avatar: String,
    override val _id: String,
    override val owner: String,
    override var isRead: Boolean = false,
    var postId:String,




) : INotification {
    override fun handleNotification() {
        // Implementation for handling like notifications
        Log.d("NotificationHandler", "Handling Like Notification")
    }
}