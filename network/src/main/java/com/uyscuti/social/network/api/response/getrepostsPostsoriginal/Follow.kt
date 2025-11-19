package com.uyscuti.social.network.api.response.getrepostsPostsoriginal

import java.io.Serializable

data class Follow(
    val followersId: String,
    val isFollowing: Boolean
):Serializable