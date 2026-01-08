package com.uyscuti.social.network.api.response.feed.getallfeed

import com.uyscuti.social.network.api.response.feed.getallfeed.Data

data class GetAllFeedResponse(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)