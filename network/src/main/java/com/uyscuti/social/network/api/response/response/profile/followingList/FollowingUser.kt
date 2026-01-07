package com.uyscuti.social.network.api.response.profile.followingList

data class FollowingUser(
    val _id: String,
    val username: String,
    val email: String,
    val avatar: com.uyscuti.social.network.api.models.Avatar?,
    val isEmailVerified: Boolean,
    val firstName: String?,
    val lastName: String?,
    val fullName: String?,
    val bio: String?,
    val dob: String?,
    val location: String?,
    val countryCode: String?,
    val phoneNumber: String?,
    val coverImage: CoverImage?,
    val followsBack: Boolean,
    val followedAt: String
)