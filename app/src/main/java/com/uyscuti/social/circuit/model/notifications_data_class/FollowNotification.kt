package com.uyscuti.social.circuit.model.notifications_data_class

import android.util.Log

data class FollowNotification(
    override val name: String,
    override val notificationMessage: String,
    override val link: String ="follow",
    override val notificationTime: String,
    override val avatar: String,
    override val _id: String,
    override val owner: String,
    override var isRead: Boolean = false,
    var followId: String // The ID of the user who started following
) : INotification {
    override fun handleNotification() {
        // Implementation for handling follow notifications
        Log.d("NotificationHandler", "Handling Follow Notification")
    }
}


