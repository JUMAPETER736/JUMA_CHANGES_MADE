package com.uyscuti.social.network.api.response.users

import com.uyscuti.social.network.api.models.User

data class UsersResponse(
    val data: List<User>,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)
