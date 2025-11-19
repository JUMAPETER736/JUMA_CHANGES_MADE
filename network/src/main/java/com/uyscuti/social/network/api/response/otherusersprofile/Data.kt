package com.uyscuti.social.network.api.response.otherusersprofile
import com.uyscuti.social.network.api.response.otherusersprofile.Account
import com.uyscuti.social.network.api.response.otherusersprofile.CoverImage

data class Data(
    val __v: Int,
    val _id: String,
    val account: Account,
    val bio: String,
    val countryCode: String,
    val coverImage: CoverImage,
    val createdAt: String,
    val dob: String,
    val firstName: String,
    val followersCount: Int,
    val followingCount: Int,
    val isFollowing: Boolean,
    val lastName: String,
    val location: String,
    val owner: String,
    val phoneNumber: String,
    val updatedAt: String
)