package com.uyscuti.social.network.api.response.profile.followingList

data class Data(
    val _id: String,
    val avatar: Avatar,
    val bio: String,
    val countryCode: String,
    val coverImage: CoverImage,
    val dob: String,
    val email: String,
    val firstName: String,
    val followedAt: String,
    val followsBack: Boolean,
    val fullName: String,
    val isEmailVerified: Boolean,
    val lastName: String,
    val location: String,
    val phoneNumber: String,
    val username: String
)