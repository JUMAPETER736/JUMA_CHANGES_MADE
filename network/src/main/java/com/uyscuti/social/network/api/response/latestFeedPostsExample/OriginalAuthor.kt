package com.uyscuti.social.network.api.response.latestFeedPostsExample

import com.uyscuti.social.network.api.response.latestFeedPostsExample.Account
import com.uyscuti.social.network.api.response.latestFeedPostsExample.CoverImage

data class OriginalAuthor(
    val __v: Int,
    val _id: String,
    val account: Account,
    val bio: String,
    val countryCode: String,
    val coverImage: CoverImage,
    val createdAt: String,
    val dob: String,
    val firstName: String,
    val lastName: String,
    val location: String,
    val owner: String,
    val phoneNumber: String,
    val updatedAt: String
)