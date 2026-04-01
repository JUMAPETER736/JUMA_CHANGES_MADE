package com.uyscuti.social.network.api.request.group

data class ChangeRoleResponse(
    val statusCode: Int,
    val success: Boolean,
    val message: String?,
    val data: GroupChatDetail?        // full updated chat object
)