package com.uyscuti.social.network.api.response.getPageComment

import com.uyscuti.social.network.api.response.getPageComment.Account
import java.io.Serializable

data class Author(
    val _id: String,
    val account: Account,
    val firstName: String,
    val lastName: String
): Serializable