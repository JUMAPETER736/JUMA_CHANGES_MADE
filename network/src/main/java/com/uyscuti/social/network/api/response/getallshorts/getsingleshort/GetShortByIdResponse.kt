package com.uyscuti.social.network.api.response.getallshorts.getsingleshort

import com.uyscuti.social.network.api.response.getallshorts.getsingleshort.Data

data class GetShortByIdResponse(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)