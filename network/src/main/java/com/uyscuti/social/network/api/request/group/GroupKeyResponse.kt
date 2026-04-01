package com.uyscuti.social.network.api.request.group

data class GroupKeyResponse(
    val success: Boolean,
    val message: String?,
    val data: GroupKeyData?
)