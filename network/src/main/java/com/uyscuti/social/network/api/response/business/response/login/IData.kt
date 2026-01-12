package com.uyscuti.social.network.api.response.business.response.login

import com.uyscuti.social.network.api.models.business.User

data class IData(
    val accessToken: String,
    val refreshToken: String,
    val user: User
)