package com.uyscuti.social.business.retro.response.login

import com.uyscuti.social.business.retro.response.login.IData

data class LoginResponse(
    val `data`: IData,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)