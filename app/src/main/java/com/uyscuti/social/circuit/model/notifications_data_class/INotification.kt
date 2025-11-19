package com.uyscuti.social.circuit.model.notifications_data_class

interface INotification {
    val name: String
    val notificationMessage: String
    val link: String
    val notificationTime: String
    val avatar: String
    val _id: String
    val owner: String
    var isRead: Boolean // Add this property

    fun handleNotification()
}
