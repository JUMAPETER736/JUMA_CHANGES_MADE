package com.uyscuti.social.network.api.response.GeneralSearch

import com.uyscuti.social.network.api.response.posts.Post

data class SearchUserFeedResponse(
    val statusCode: Int,
    val data: SearchUserFeedWrapper?,  // Changed: Wrapped in another data object
    val message: String,
    val success: Boolean
)

data class SearchUserFeedWrapper(
    val data: SearchUserFeedData?  // Added: This matches the API's nested structure
)

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

data class MatchingUser(
    val _id: String,
    val username: String,
    val email: String
)

data class DebugInfo(
    val userIdsSearched: Int?,
    val socialProfileIdsSearched: Int?,
    val rawPostCount: Int?,
    val postsReturnedAfterAggregation: Int?,
    val currentPage: Int?,
    val totalPages: Int?
)