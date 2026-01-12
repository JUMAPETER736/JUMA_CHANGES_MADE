package com.uyscuti.social.network.api.response.follow_unfollow

import com.uyscuti.social.network.api.response.follow_unfollow.Data

data class FollowUnFollowResponse(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)