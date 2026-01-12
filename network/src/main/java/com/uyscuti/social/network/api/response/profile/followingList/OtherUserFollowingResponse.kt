package com.uyscuti.social.network.api.response.profile.followingList

data class OtherUserFollowingResponse(
    val `data`: List<Data>,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)