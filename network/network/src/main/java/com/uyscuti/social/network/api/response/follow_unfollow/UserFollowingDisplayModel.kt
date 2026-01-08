import com.uyscuti.social.network.api.models.User
import com.uyscuti.social.network.api.models.Avatar as ModelAvatar
import com.uyscuti.social.network.api.response.profile.followingList.Avatar as ApiAvatar
import com.uyscuti.social.network.api.response.profile.followingList.Data
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class UserFollowingDisplayModel(
    val _id: String,
    val avatar: ModelAvatar?,
    val email: String,
    val isEmailVerified: Boolean,
    val role: String,
    val username: String,
    val lastseen: Date,
    var isFollowing: Boolean = false,
    val firstName: String = "",
    val lastName: String = "",
    val isVerified: Boolean = false,
    val isOnline: Boolean = false,
    val hasActiveStory: Boolean = false,
    val mutualConnectionsCount: Int = 0,
    val isSuggested: Boolean = false
) {
    // Constructor from your existing User model
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
    val fullName: String get() = if (firstName.isNotEmpty() && lastName.isNotEmpty()) "$firstName $lastName" else username

    companion object {
        private fun isUserOnline(lastSeen: Date): Boolean {
            val currentTime = System.currentTimeMillis()
            val lastSeenTime = lastSeen.time
            val fiveMinutesInMillis = 5 * 60 * 1000 // 5 minutes
            return (currentTime - lastSeenTime) <= fiveMinutesInMillis
        }

        // Factory method to create from API Data response (original)
        fun fromFollowingUser(data: Data): UserFollowingDisplayModel {
            return UserFollowingDisplayModel(
                _id = data._id,
                avatar = convertAvatar(data.avatar),
                email = data.email,
                isEmailVerified = data.isEmailVerified,
                role = "user",
                username = data.username,
                lastseen = parseDate(data.followedAt) ?: Date(),
                isFollowing = true,
                firstName = data.firstName.takeIf { it.isNotBlank() } ?: data.username.split("_").firstOrNull() ?: data.username,
                lastName = data.lastName.takeIf { it.isNotBlank() } ?: data.username.split("_").drop(1).joinToString(" "),
                isVerified = data.isEmailVerified,
                isOnline = false,
                hasActiveStory = false,
                mutualConnectionsCount = 0,
                isSuggested = false
            )
        }

        // Overloaded factory method with explicit isFollowing parameter
        fun fromFollowingUser(data: Data, isFollowing: Boolean): UserFollowingDisplayModel {
            return UserFollowingDisplayModel(
                _id = data._id,
                avatar = convertAvatar(data.avatar),
                email = data.email,
                isEmailVerified = data.isEmailVerified,
                role = "user",
                username = data.username,
                lastseen = parseDate(data.followedAt) ?: Date(),
                isFollowing = isFollowing,
                firstName = data.firstName.takeIf { it.isNotBlank() } ?: data.username.split("_").firstOrNull() ?: data.username,
                lastName = data.lastName.takeIf { it.isNotBlank() } ?: data.username.split("_").drop(1).joinToString(" "),
                isVerified = data.isEmailVerified,
                isOnline = false,
                hasActiveStory = false,
                mutualConnectionsCount = 0,
                isSuggested = false
            )
        }

        // Convert API Avatar to models.Avatar (assuming identical structure)
        private fun convertAvatar(apiAvatar: ApiAvatar?): ModelAvatar? {
            return apiAvatar?.let {
                ModelAvatar(
                    _id = it._id,
                    localPath = it.localPath,
                    url = it.url
                )
            }
        }

        private fun parseDate(dateString: String?): Date? {
            if (dateString == null) return null
            return try {
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                format.timeZone = TimeZone.getTimeZone("UTC")
                format.parse(dateString)
            } catch (e: Exception) {
                null
            }
        }
    }

}