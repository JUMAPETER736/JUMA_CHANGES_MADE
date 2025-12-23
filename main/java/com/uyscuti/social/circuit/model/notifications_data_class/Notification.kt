package com.uyscuti.social.circuit.model.notifications_data_class

import android.util.Log
import com.uyscuti.social.circuit.model.notifications_data_class.INotification



data class Notification(
    override val name: String,
    override val notificationMessage: String,
    override val link: String = "",
    override val notificationTime: String,
    override val avatar: String,
    override val _id: String,
    override val owner: String,
    override var isRead: Boolean = false




) : INotification {
    override fun handleNotification() {
        // Implementation for handling like notifications
        Log.d("NotificationHandler", "Handling Like Notification")
    }
}

