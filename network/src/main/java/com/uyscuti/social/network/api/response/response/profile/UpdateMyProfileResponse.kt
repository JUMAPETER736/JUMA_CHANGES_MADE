package com.uyscuti.social.network.api.response.profile

import com.uyscuti.social.network.api.response.profile.Data

data class UpdateMyProfileResponse(
    val `data`: List<Data>,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)