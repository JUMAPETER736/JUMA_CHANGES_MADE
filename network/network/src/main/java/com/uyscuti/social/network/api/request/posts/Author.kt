package com.uyscuti.social.network.api.request.posts

data class Author(
    val __v: Int,
    val _id: String,
    val account: Account,
    val bio: String,
    val countryCode: String,
    val coverImage: CoverImage,
    val createdAt: String,
    val avatar: Avatar?,
    val dob: String,
    val firstName: String,
    val lastName: String,
    val location: String,
    val owner: String,
    val phoneNumber: String,
    val updatedAt: String
    
)