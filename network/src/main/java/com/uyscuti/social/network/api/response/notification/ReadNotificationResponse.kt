package com.uyscuti.social.network.api.response.notification

import com.uyscuti.social.network.api.response.notification.DataX

data class ReadNotificationResponse(
    val `data`: DataX,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)