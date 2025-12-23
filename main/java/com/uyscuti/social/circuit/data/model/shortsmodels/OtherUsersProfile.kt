package com.uyscuti.social.circuit.data.model.shortsmodels

import java.io.Serializable
import java.util.Date

data class OtherUsersProfile(
    val name: String,
    val username: String,
    val profilePic: String,
    val userId: String,
    val isVerified: Boolean,

    // Additional commonly needed fields
    val bio: String? = null,
    val linkInBio: String? = null,
    val isCreator: Boolean = false,
    val isTrending: Boolean = false,
    val isFollowing: Boolean = false,
    val isPrivate: Boolean = false,
    val followersCount: Long = 0,
    val followingCount: Long = 0,
    val postsCount: Long = 0,
    val shortsCount: Long = 0,
    val videosCount: Long = 0,
    val isOnline: Boolean = false,
    val lastSeen: Date? = null,
    val joinedDate: Date? = null,
    val location: String? = null,
    val website: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val dateOfBirth: Date? = null,
    val gender: String? = null,
    val accountType: String = "user",
    val isBlocked: Boolean = false,
    val isMuted: Boolean = false,
    val badgeType: String? = null,
    val level: Int = 1,
    val reputation: Long = 0,
    val coverPhoto: String? = null,
    val theme: String? = null,
    val language: String? = null,
    val timezone: String? = null,
    val notificationsEnabled: Boolean = true,
    val privacySettings: Map<String, Any>? = null,
    val socialLinks: Map<String, String>? = null,
    val achievements: List<String>? = null,
    val interests: List<String>? = null,
    val categories: List<String>? = null

) : Serializable