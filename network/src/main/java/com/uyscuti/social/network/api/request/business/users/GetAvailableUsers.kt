package com.uyscuti.social.network.api.request.business.users

import com.uyscuti.social.network.api.request.business.users.Data

data class GetAvailableUsers(
    val `data`: List<Data>,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)