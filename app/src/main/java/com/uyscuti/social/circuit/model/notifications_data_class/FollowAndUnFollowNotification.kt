package com.uyscuti.social.circuit.model.notifications_data_class

import android.util.Log



data class UnfollowNotification(
    override val name: String,
    override val notificationMessage: String,
    override val link: String = "unfollowed",
    override val notificationTime: String,
    override val avatar: String,
    override val _id: String,
    override val owner: String,
    override var isRead: Boolean = false,
    var unfollowId: String // The ID of the user who stopped following
//    override var colorState: String

) : INotification {
    override fun handleNotification() {
        // Implementation for handling unfollow notifications
        Log.d("NotificationHandler", "Handling Unfollow Notification")
    }
}
