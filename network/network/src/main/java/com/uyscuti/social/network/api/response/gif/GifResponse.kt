package com.uyscuti.social.network.api.response.gif

import com.uyscuti.social.network.api.response.gif.Data


data class GifResponse(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)