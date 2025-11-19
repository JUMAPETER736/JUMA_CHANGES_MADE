package com.uyscuti.social.network.api.request.FaceBookLogIn

data class FacebookLoginRequest(
    val accessToken: String,
    val facebookId: String?,
    val name: String?,
    val email: String?,
    val pictureUrl: String?
)