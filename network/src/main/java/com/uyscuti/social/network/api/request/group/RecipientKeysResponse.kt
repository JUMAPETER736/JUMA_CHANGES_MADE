package com.uyscuti.social.network.api.request.group

data class RecipientKeysResponse(
    val statusCode: Int,
    val success: Boolean,
    val message: String?,
    val data: RecipientKeyData?
)