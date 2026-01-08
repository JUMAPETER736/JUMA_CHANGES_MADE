package com.uyscuti.social.network.api.response.login

import com.uyscuti.social.network.api.models.User

data class IData(
    val accessToken: String,
    val refreshToken: String,
    val user: User
)