package com.uyscuti.social.network.api.request.profile

data class EditMyProfile(
    val bio: String,
    val countryCode: String,
    val dob: String,
    val firstName: String,
    val lastName: String,
    val location: String,
    val phoneNumber: String
)