package com.uyscuti.social.network.api.response.profile.followersList

data class UserOtherFollowersResponse(
    val `data`: List<Data>,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)