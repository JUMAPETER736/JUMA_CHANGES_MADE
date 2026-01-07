package com.uyscuti.social.network.api.response.feed.deletefeed

import com.uyscuti.social.network.api.response.feed.deletefeed.Data

data class DeleteFeedResponse(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)