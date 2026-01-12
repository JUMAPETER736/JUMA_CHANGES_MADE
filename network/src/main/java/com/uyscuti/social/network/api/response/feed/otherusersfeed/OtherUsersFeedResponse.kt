package com.uyscuti.social.network.api.response.feed.otherusersfeed

import com.uyscuti.social.network.api.response.feed.otherusersfeed.Data

data class OtherUsersFeedResponse(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)