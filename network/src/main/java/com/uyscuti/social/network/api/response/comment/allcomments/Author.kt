package com.uyscuti.social.network.api.response.comment.allcomments

import com.uyscuti.social.network.api.response.comment.allcomments.Account
import java.io.Serializable

data class Author(
    val _id: String,
    var account: Account,
    var firstName: String,
    var lastName: String,
    var avatar: Avatar? = null,
): Serializable