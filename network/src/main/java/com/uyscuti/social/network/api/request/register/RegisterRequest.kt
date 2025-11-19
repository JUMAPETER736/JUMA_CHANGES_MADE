package com.uyscuti.social.network.api.request.register

data class RegisterRequest(
    val email: String,
    val password: String,
    val username: String
)
