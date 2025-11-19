package com.uyscuti.social.network.api.response

import com.uyscuti.social.network.api.response.Data

data class MainResponse(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)