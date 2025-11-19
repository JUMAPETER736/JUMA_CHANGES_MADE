package com.uyscuti.social.network.api.response.otherusersprofileshorts

import com.uyscuti.social.network.api.response.otherusersprofileshorts.Data

data class OtherUsersProfileShortsResponse(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)