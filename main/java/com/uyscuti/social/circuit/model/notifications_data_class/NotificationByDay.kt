package com.uyscuti.social.circuit.model.notifications_data_class

import com.uyscuti.social.circuit.model.notifications_data_class.INotification

data class NotificationByDay(
    val day: String,
    val type:String,
    val notification: List<INotification>
)
