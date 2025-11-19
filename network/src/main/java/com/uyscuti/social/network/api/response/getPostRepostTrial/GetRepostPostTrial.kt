package com.uyscuti.social.network.api.response.getPostRepostTrial

import com.uyscuti.social.network.api.response.getPostRepostTrial.Data

data class GetRepostPostTrial(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)