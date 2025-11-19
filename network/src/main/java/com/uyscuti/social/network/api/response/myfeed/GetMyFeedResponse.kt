package com.uyscuti.social.network.api.response.myfeed

import com.uyscuti.social.network.api.response.myfeed.Data

data class GetMyFeedResponse(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)

