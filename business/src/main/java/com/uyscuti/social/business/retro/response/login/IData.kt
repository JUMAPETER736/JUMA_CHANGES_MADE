package com.uyscuti.social.business.retro.response.login

import com.uyscuti.social.business.retro.model.User

data class IData(
    val accessToken: String,
    val refreshToken: String,
    val user: User
)