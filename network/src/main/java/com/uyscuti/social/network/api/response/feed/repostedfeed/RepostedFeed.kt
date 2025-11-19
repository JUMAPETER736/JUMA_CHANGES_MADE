package com.uyscuti.social.network.api.response.feed.repostedfeed

data class RepostedFeed(
    val `data`: Data,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)