package com.uyscuti.social.circuit.model.notifications_data_class

import android.util.Log

data class FriendSuggestionReq(
    val ownerId: String,
    val suggestedUserId: String,
    override val name: String,
    override val notificationMessage: String,
    override val link: String,
    override val notificationTime: String,
    override val avatar: String,
    override val _id: String,
    override val owner: String,
    override var isRead: Boolean = false,

    ): INotification {
    override fun handleNotification() {
        // Implementation for handling comment notifications
        Log.d("NotificationHandler", "Handling Comment Notification")
    }
}