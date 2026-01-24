package com.uyscuti.social.network.api.response.profile.followingList

data class OtherUserFollowingResponse(
    val `data`: List<Data>,
    val message: String,
    val statusCode: Int,
    val success: Boolean
)



data class BaseResponse(
    val success: Boolean,
    val message: String,
    val statusCode: Int
)

data class UserDetails(
    val _id: String,
    val username: String,
    val email: String,
    val avatar: Avatar?,
    val coverImage: CoverImage?,
    val firstName: String?,
    val lastName: String?,
    val bio: String?,
    val dob: String?,
    val location: String?,
    val countryCode: String?,
    val phoneNumber: String?,
    val profileId: String?,
    val createdAt: String,
    val updatedAt: String
)


// Close Friends
data class CloseFriendsListResponse(
    val success: Boolean,
    val message: String,
    val statusCode: Int,
    val data: List<CloseFriendItem>
)

data class CloseFriendItem(
    val _id: String,
    val user: UserDetails?,
    val addedAt: String
)

data class CloseFriendStatusResponse(
    val success: Boolean,
    val message: String,
    val statusCode: Int,
    val data: CloseFriendStatus
)

data class CloseFriendStatus(
    val isCloseFriend: Boolean
)

// Muted Posts
data class MutedPostsListResponse(
    val success: Boolean,
    val message: String,
    val statusCode: Int,
    val data: List<MutedPostsItem>
)

data class MutedPostsItem(
    val _id: String,
    val user: UserDetails?,
    val mutedAt: String
)

data class MutedPostsStatusResponse(
    val success: Boolean,
    val message: String,
    val statusCode: Int,
    val data: MutedPostsStatus
)

data class MutedPostsStatus(
    val isPostsMuted: Boolean
)

// Muted Stories
data class MutedStoriesListResponse(
    val success: Boolean,
    val message: String,
    val statusCode: Int,
    val data: List<MutedStoriesItem>
)

data class MutedStoriesItem(
    val _id: String,
    val user: UserDetails?,
    val mutedAt: String
)

data class MutedStoriesStatusResponse(
    val success: Boolean,
    val message: String,
    val statusCode: Int,
    val data: MutedStoriesStatus
)

data class MutedStoriesStatus(
    val isStoriesMuted: Boolean
)

// Favorites
data class FavoritesListResponse(
    val success: Boolean,
    val message: String,
    val statusCode: Int,
    val data: List<FavoriteItem>
)

data class FavoriteItem(
    val _id: String,
    val user: UserDetails?,
    val addedAt: String
)

data class FavoriteStatusResponse(
    val success: Boolean,
    val message: String,
    val statusCode: Int,
    val data: FavoriteStatus
)

data class FavoriteStatus(
    val isFavorite: Boolean
)

// Restricted
data class RestrictedListResponse(
    val success: Boolean,
    val message: String,
    val statusCode: Int,
    val data: List<RestrictedItem>
)

data class RestrictedItem(
    val _id: String,
    val user: UserDetails?,
    val restrictedAt: String
)

data class RestrictedStatusResponse(
    val success: Boolean,
    val message: String,
    val statusCode: Int,
    val data: RestrictedStatus
)

data class RestrictedStatus(
    val isRestricted: Boolean
)


