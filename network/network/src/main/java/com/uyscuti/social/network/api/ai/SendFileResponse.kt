package com.uyscuti.social.network.api.ai

import com.uyscuti.social.network.api.ai.Data

data class SendFileResponse(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)