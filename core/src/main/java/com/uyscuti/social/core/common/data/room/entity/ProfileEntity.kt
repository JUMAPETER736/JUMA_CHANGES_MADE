package com.uyscuti.social.core.common.data.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.uyscuti.social.core.common.data.room.converters.AccountConverter
import com.uyscuti.social.core.common.data.room.converters.CoverImageConverter
import com.uyscuti.social.network.api.response.getmyprofile.Account
import com.uyscuti.social.network.api.response.getmyprofile.CoverImage


@Entity(tableName = "profile")
@TypeConverters(AccountConverter::class, CoverImageConverter::class)
data class ProfileEntity(
    val __v: Int,
    @PrimaryKey
    val _id: String,
    val account: Account?,
    val bio: String?,
    val countryCode: String?,
    val coverImage: CoverImage?,
    val createdAt: String?,
    val dob: String?,
    val firstName: String,
    val followersCount: Int?,
    val followingCount: Int?,
    val isFollowing: Boolean?,
    val lastName: String,
    val location: String?,
    val owner: String?,
    val phoneNumber: String?,
    val updatedAt: String?
)
