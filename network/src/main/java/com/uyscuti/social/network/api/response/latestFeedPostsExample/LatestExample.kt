package com.uyscuti.social.network.api.response.latestFeedPostsExample

import com.uyscuti.social.network.api.response.latestFeedPostsExample.Data

data class LatestExample(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)