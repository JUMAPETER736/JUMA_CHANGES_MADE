package com.uyscuti.social.network.api.response.otherusersprofile

import com.uyscuti.social.network.api.response.otherusersprofile.Data

data class OtherUsersProfileResponse(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)