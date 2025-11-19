package com.uyscuti.social.network.api.response.feed.getallfeed

data class Data(
    val followList: List<Follow> = emptyList(),
    val posts: Posts
)