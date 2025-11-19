package com.uyscuti.social.business.retro.request.users

data class LiveLocationInfo(
    val accuracy: String,
    val latitude: String,
    val longitude: String,
    val range: String
)