package com.uyscuti.social.network.api.response.favoritefeed

import com.uyscuti.social.network.api.response.favoritefeed.Data

data class FeedFavoriteResponse(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)