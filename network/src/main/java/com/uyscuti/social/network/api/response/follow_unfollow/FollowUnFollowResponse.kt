package com.uyscuti.social.network.api.response.follow_unfollow

import com.uyscuti.social.network.api.response.follow_unfollow.Data
import com.uyscuti.social.network.api.response.posts.Avatar

data class FollowUnFollowResponse(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)


data class BlockUnblockResponse(
    val data: BlockData?,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)

data class BlockData(
    val blocked: Boolean
)


data class BlockedUsersResponse(
    val success: Boolean,
    val message: String,
    val data: List<BlockedUserData>?
)

data class BlockedUserData(
    val _id: String,
    val username: String,
    val avatar: Avatar?,
    val email: String,
    val isEmailVerified: Boolean,
    val firstName: String,
    val lastName: String,
    val bio: String,
    val blockedAt: String
)