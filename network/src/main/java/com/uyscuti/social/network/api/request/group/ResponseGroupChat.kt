package com.uyscuti.social.network.api.request.group

import com.uyscuti.social.network.api.request.group.GroupChat

data class ResponseGroupChat(
    val `data`: GroupChat,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)