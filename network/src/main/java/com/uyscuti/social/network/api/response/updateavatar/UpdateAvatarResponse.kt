package com.uyscuti.social.network.api.response.updateavatar

import com.uyscuti.social.network.api.response.updateavatar.DataX

data class UpdateAvatarResponse(
    val `data`: DataX,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)