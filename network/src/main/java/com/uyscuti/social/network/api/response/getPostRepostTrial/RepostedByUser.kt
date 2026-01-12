package com.uyscuti.social.network.api.response.getPostRepostTrial

import com.uyscuti.social.network.api.response.getPostRepostTrial.AvatarX

data class RepostedByUser(
    val _id: String,
    val avatar: AvatarX,
    val username: String
)