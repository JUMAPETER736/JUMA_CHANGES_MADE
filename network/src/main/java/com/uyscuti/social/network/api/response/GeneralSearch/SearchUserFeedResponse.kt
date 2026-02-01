package com.uyscuti.social.network.api.response.GeneralSearch

import com.uyscuti.social.network.api.response.posts.Author
import com.uyscuti.social.network.api.response.posts.Avatar
import com.uyscuti.social.network.api.response.posts.Post



data class SearchUserFeedData(
    val posts: List<Post>?,
    val totalPosts: Int?,
    val limit: Int?,
    val page: Int?,
    val totalPages: Int?,
    val hasNextPage: Boolean?,
    val hasPrevPage: Boolean?,
    val matchingUsers: List<MatchingUser>?,
    val searchedUsername: String?,
    val debug: DebugInfo?
)

data class DebugInfo(
    val userIdsSearched: Int?,
    val socialProfileIdsSearched: Int?,
    val rawPostCount: Int?,
    val postsReturnedAfterAggregation: Int?,
    val currentPage: Int?,
    val totalPages: Int?
)


data class MatchingUser(
    val _id: String,
    val username: String,
    val email: String?,
    val avatar: Avatar?,
    val firstName: String?,
    val lastName: String?,
    val bio: String?,
    val location: String?,
    val countryCode: String?,
    val phoneNumber: String?,
    val createdAt: String?,
    val updatedAt: String?
)

data class SearchDataWrapper(
    val posts: com.uyscuti.social.network.api.response.getallshorts.Data,
    val followList: List<FollowListItem>
)

data class FollowListItem(
    val followersId: String,
    val isFollowing: Boolean
)


data class DataX(
    val posts: List<Post>?,
    val totalPosts: Int?,
    val limit: Int?,
    val page: Int?,
    val totalPages: Int?,
    val hasNextPage: Boolean?,
    val hasPrevPage: Boolean?
)


data class Post(
    val _id: String,
    val content: String,
    val fileId: String,
    val feedShortsBusinessId: String,
    val tags: List<String>,
    val images: List<Image>,
    val thumbnail: List<Thumbnail>,
    val author: Author,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int,
    val comments: Int,
    val likes: Int,
    val isLiked: Boolean,
    val isBookmarked: Boolean
)

data class Image(
    val url: String,
    val localPath: String,
    val _id: String
)

data class Thumbnail(
    val thumbnailUrl: String,
    val thumbnailLocalPath: String,
    val _id: String
)

data class Author(
    val _id: String,
    val coverImage: CoverImage,
    val firstName: String,
    val lastName: String,
    val bio: String,
    val dob: String,
    val location: String,
    val countryCode: String,
    val phoneNumber: String,
    val owner: String,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int,
    val account: Account,
    val authorId: List<String>
)

data class CoverImage(
    val url: String,
    val localPath: String,
    val _id: String
)

data class Account(
    val _id: String,
    val avatar: Avatar,
    val username: String,
    val email: String
)

data class Avatar(
    val url: String,
    val localPath: String,
    val _id: String
)

data class Follow(
    val followersId: String,
    val isFollowing: Boolean
)


