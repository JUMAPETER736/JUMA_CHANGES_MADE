package com.uyscuti.social.network.api.response.notification

import com.uyscuti.social.network.api.response.notification.Data


data class GetMyNotifications(
    val `data`: List<Data>,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)