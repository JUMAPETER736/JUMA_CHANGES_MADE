package com.uyscuti.social.network.api.request.group

data class GroupChatsResponse(
    val statusCode: Int,
    val success: Boolean,
    val message: String?,
    val data: List<GroupChatDetail>?
)