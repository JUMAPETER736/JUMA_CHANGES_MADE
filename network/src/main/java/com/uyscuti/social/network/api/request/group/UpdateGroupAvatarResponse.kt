package com.uyscuti.social.network.api.request.group

data class UpdateGroupAvatarResponse(
    val statusCode: Int,
    val success: Boolean,
    val message: String?,
    val data: UpdateAvatarResponseData?
)