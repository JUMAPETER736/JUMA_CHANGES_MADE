package com.uyscuti.social.network.api.request.profile

data class UpdateSocialProfileRequest(
    val firstName: String?,
    val lastName: String?,
    val bio: String?,
    val dob: String?,
    val location: String?,
    val phoneNumber: String?,
    val countryCode: String?
)
