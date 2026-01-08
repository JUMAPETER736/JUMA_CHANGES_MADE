package com.uyscuti.social.network.api.response.googleloginresponse

import com.uyscuti.social.network.api.models.User

data class GoogleLoginResponse(
    val success: Boolean,
    val user: User?, // Include user details if needed
    val accessToken: String?, // Include an access token if you use token-based authentication
    val errorMessage: String? // Include an error message if there's an issue
)
