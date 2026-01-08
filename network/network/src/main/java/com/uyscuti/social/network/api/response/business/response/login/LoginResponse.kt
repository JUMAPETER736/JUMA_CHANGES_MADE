package com.uyscuti.social.network.api.response.business.response.login

import com.uyscuti.social.network.api.response.business.response.login.IData

data class LoginResponse(
    val `data`: IData,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)