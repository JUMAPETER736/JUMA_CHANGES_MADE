package com.uyscuti.social.network.api.request.group

data class SingleGroupChatResponse(
    val statusCode: Int,
    val success: Boolean,
    val message: String?,
    val data: GroupChatDetail?
)