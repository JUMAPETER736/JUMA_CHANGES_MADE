package com.uyscuti.social.network.api.response.getallshorts

data class ResponseData(
    val posts: Data,
    val followList: List<FollowListItem>
)
