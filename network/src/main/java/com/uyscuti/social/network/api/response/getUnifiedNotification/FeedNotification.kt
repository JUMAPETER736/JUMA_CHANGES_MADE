package com.uyscuti.social.network.api.response.getUnifiedNotification

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

data class FeedNotification(
    val _id: String,
    val avatar: String,
    val createdAt: String,
    val `data`: DataX,
    val message: String,
    val owner: String,
    val read: Boolean,
    val sender: Sender,
    val type: String
) {
    // Convert MongoDB date string to timestamp
    fun getTimestamp(): Long {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            format.timeZone = TimeZone.getTimeZone("UTC")
            format.parse(createdAt)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}