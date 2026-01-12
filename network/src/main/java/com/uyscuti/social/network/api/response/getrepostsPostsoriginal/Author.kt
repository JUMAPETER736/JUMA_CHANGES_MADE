package com.uyscuti.social.network.api.response.getrepostsPostsoriginal

import java.io.Serializable

data class Author(
    val __v: Int,
    val _id: String,
    val account: Account,
    val bio: String,
    val countryCode: String,
    val coverImage: CoverImage,
    val createdAt: String,
    val dob: String,
    val avatar: Avatar?,
    val firstName: String,
    val lastName: String,
    val location: String,
    val owner: String,
    val phoneNumber: String,
    val updatedAt: String,
    val profilePicture: String?,
    val profilePicUrl: String?,
    val username: String?,
):Serializable