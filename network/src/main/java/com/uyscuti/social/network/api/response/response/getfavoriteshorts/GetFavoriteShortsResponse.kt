package com.uyscuti.social.network.api.response.getfavoriteshorts

import com.uyscuti.social.network.api.response.getfavoriteshorts.Data

data class GetFavoriteShortsResponse(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)