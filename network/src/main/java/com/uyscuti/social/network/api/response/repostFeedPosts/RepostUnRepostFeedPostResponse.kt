package com.uyscuti.social.network.api.response.repostFeedPosts

data class RepostUnRepostFeedPostResponse(
    val data: RepostUnRepostData,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)

data class RepostUnRepostData(
    val isReposted: Boolean,
    val repostCount: Int,
    val repostedByUserIds: List<String>,
    val repostId: String? = null,
    val postId: String? = null,
    val repostedBy: String? = null,
    val repostedAt: String? = null
)