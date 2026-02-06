package com.uyscuti.social.network.api.response.repostFeedPosts

data class RepostUnRepostFeedPostResponse(
    val `data`: FeedRepostData,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)

data class FeedRepostData(
    val postId: String,
    val isReposted: Boolean,
    val repostCount: Int,
    val repostedBy: String? = null,
    val repostedAt: String? = null
)
