package com.uyscuti.social.network.api.response.comment.allcomments

import java.io.Serializable

data class Account(
    var _id: String,
    var avatar: Avatar,
    val email: String,
    var username: String
): Serializable