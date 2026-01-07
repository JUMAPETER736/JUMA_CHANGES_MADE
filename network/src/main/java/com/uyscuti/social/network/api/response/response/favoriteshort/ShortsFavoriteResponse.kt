package com.uyscuti.social.network.api.response.favoriteshort

import com.uyscuti.social.network.api.response.favoriteshort.Data

data class ShortsFavoriteResponse(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)