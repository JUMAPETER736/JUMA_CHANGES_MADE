package com.uyscuti.social.network.api.response.userprofileshorts

import com.uyscuti.social.network.api.response.userprofileshorts.Data

data class UserProfileShortsResponse(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)