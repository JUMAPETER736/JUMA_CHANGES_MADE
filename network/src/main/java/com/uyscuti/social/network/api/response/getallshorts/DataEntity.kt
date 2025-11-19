package com.uyscuti.social.network.api.response.getallshorts

data class DataEntity (
    val `posts`: Data,
    val followList: List<FollowListItem>,
)
