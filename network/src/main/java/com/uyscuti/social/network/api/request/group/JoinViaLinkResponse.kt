package com.uyscuti.social.network.api.request.group

data class JoinViaLinkResponse(
    val statusCode: Int,
    val success: Boolean,
    val message: String?,
    val data: GroupChatDetail?        // full chat object after joining
)
