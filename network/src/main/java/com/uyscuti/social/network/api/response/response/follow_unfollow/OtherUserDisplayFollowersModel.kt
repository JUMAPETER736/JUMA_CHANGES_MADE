package com.uyscuti.social.network.api.response.follow_unfollow

import com.uyscuti.social.network.api.models.User
import com.uyscuti.social.network.api.response.profile.followersList.Data
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class OtherUserDisplayFollowersModel(
    val _id: String,
    val avatar: com.uyscuti.social.network.api.models.Avatar?,
    val email: String,
    val isEmailVerified: Boolean,
    val role: String = "",
    val username: String,
    val lastseen: Date = Date(),
    val bio: String = "",
    var isFollowing: Boolean = false,
    val firstName: String = "",
    val lastName: String = "",
    val isVerified: Boolean = false,
    val isOnline: Boolean = false,
    val hasActiveStory: Boolean = false,
    val mutualConnectionsCount: Int = 0,
    val isSuggested: Boolean = false,
    val location: String = "",
    val followedAt: String = ""
) {
    constructor(user: User, isFollowing: Boolean = false) : this(
        _id = user._id,
        avatar = user.avatar,
        email = user.email,
        isEmailVerified = user.isEmailVerified,
        role = user.role,
        username = user.username,
        lastseen = user.lastseen,
        isFollowing = isFollowing,
        firstName = user.username.split("_").firstOrNull() ?: user.username,
        lastName = user.username.split("_").drop(1).joinToString(" "),
        isVerified = user.isEmailVerified,
        isOnline = isUserOnline(user.lastseen),
        hasActiveStory = false,
        mutualConnectionsCount = 0,
        isSuggested = false
    )

    val id: String get() = _id
    val fullName: String get() = if (firstName.isNotEmpty() && lastName.isNotEmpty()) {
        "$firstName $lastName"
    } else {
        username
    }

    companion object {
        private fun isUserOnline(lastSeen: Date): Boolean {
            val currentTime = System.currentTimeMillis()
            val lastSeenTime = lastSeen.time
            val fiveMinutesInMillis = 5 * 60 * 1000
            return (currentTime - lastSeenTime) <= fiveMinutesInMillis
        }

        private fun parseDate(dateString: String): Date {
            return try {
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                format.parse(dateString) ?: Date()
            } catch (e: Exception) {
                Date()
            }
        }

        fun fromApiData(data: Data, isFollowing: Boolean = false): OtherUserDisplayFollowersModel {
            val avatarModel = data.avatar?.let {
                com.uyscuti.social.network.api.models.Avatar(
                    _id = it._id,
                    url = it.url,
                    localPath = it.localPath
                )
            }

            val lastSeenDate = parseDate(data.followedAt)

            return OtherUserDisplayFollowersModel(
                _id = data._id,
                avatar = avatarModel,
                email = data.email,
                isEmailVerified = data.isEmailVerified,
                role = "",
                username = data.username,
                lastseen = lastSeenDate,
                bio = data.bio,
                isFollowing = isFollowing,
                firstName = data.firstName,
                lastName = data.lastName,
                isVerified = data.isEmailVerified,
                isOnline = isUserOnline(lastSeenDate),
                hasActiveStory = false,
                mutualConnectionsCount = 0,
                isSuggested = false,
                location = data.location,
                followedAt = data.followedAt
            )
        }
    }
}