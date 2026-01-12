package com.uyscuti.social.network.api.response.getfeedreposts

import com.uyscuti.social.network.api.response.getfeedreposts.Data

data class GetFeedAndReposts(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)