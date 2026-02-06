package com.uyscuti.social.network.api.response.repostFeedPosts

data class RepostUnRepostFeedPostResponse(
    val statusCode: Int,
    val data: RepostData,
    val message: String,
    val success: Boolean
)

data class RepostData(
    val isReposted: Boolean,
    val repostCount: Int,
    val repostedByUserIds: List<String>,
    val repostId: String? = null
)