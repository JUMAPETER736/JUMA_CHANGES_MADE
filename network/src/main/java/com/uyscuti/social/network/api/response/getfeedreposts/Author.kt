package com.uyscuti.social.network.api.response.getfeedreposts

data class Author(
    val __v: Int,
    val _id: String,
    val account: Account,
    /** the account at first it was a list **/
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