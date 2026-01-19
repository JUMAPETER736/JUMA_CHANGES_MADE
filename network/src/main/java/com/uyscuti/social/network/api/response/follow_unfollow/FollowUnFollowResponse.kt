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


data class AllBlockedUsersResponse(
    val statusCode: Int,
    val data: BlockedUsersData,
    val message: String,
    val success: Boolean
)

data class BlockedUsersData(
    val blockedUsers: List<BlockedUserItem>,
    val totalBlocked: Int,
    val limit: Int,
    val page: Int,
    val totalPages: Int,
    val pagingCounter: Int,
    val hasPrevPage: Boolean,
    val hasNextPage: Boolean,
    val prevPage: Int?,
    val nextPage: Int?
)

data class BlockedUserItem(
    val _id: String,
    val blockedAt: String,
    val user: BlockedUserDetails
)

data class BlockedUserDetails(
    val _id: String,
    val username: String,
    val email: String,
    val avatar: Avatar?,
    val firstName: String,
    val lastName: String
)

data class Avatar(
    val _id: String,
    val url: String,
    val localPath: String
)