package com.uyscuti.social.network.api.request.group

data class BulkKeysResponse(
    val success: Boolean,
    val message: String?,
    val data: BulkKeyData?
)