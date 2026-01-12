package com.uyscuti.social.network.api.response.getallshorts

import com.uyscuti.social.network.api.response.getallshorts.Data
import com.uyscuti.social.network.api.response.getallshorts.FollowListItem

data class ResponseData(
    val posts: Data,
    val followList: List<FollowListItem>
)
