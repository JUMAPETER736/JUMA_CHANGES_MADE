package com.uyscuti.sharedmodule.model.notifications_data_class


data class NotificationByDay(
    val day: String,
    val type:String,
    val notification: List<INotification>
)
