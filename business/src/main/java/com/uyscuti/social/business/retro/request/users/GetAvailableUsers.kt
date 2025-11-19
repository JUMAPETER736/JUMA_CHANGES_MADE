package com.uyscuti.social.business.retro.request.users

import com.uyscuti.social.business.retro.request.users.Data

data class GetAvailableUsers(
    val `data`: List<Data>,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)