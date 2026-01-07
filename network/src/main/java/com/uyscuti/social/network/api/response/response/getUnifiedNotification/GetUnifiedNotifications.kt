package com.uyscuti.social.network.api.response.getUnifiedNotification

import com.uyscuti.social.network.api.response.getUnifiedNotification.Data

data class GetUnifiedNotifications(
    val `data`: List<Data>,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)