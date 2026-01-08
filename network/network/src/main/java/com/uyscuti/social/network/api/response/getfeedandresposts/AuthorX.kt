package com.uyscuti.social.network.api.response.getfeedandresposts

import java.io.Serializable

data class AuthorX(
    val __v: Int,
    val _id: String,
    val account: List<Account>,
    val bio: String,
    val countryCode: String,
    val coverImage: CoverImageX,
    val createdAt: String,
    val dob: Any,
    val firstName: String,
    val lastName: String,
    val location: String,
    val owner: String,
    val phoneNumber: String,
    val updatedAt: String
):Serializable