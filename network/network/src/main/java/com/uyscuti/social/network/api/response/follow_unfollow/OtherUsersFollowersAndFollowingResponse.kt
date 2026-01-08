package com.uyscuti.social.network.api.response.follow_unfollow

data class OtherUsersFollowersAndFollowingResponse(
    val success: Boolean,
    val message: String,
    val data: FollowStatusData
)


