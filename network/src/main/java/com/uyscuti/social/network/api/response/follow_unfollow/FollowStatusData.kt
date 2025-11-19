package com.uyscuti.social.network.api.response.follow_unfollow

data class FollowStatusData(
    val isFollowing: Boolean,
    val isFollowedBy: Boolean,
    val isBlocked: Boolean,
    val isBlockedBy: Boolean,
    val mutualFollowersCount: Int? = null,
    val relationshipStatus: String? = null
)