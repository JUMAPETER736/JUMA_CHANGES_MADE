package com.uyscuti.social.network.api.response.GeneralSearch

import com.uyscuti.social.network.api.response.posts.Post

data class SearchUserFeedResponse(
    val statusCode: Int,
    val data: SearchUserFeedWrapper?,
    val message: String,
    val success: Boolean
)

data class SearchUserFeedWrapper(
    val data: SearchUserFeedData?
)



data class GeneralSearchResponse(
    val statusCode: Int,
    val data: GeneralSearchWrapper?,
    val message: String,
    val success: Boolean
)

data class GeneralSearchWrapper(
    val data: GeneralSearchData?,
    val searchQuery: String?,
    val filter: String?,
    val matchingUsers: List<MatchingUser>?,
    val totalResults: Int?
)

data class GeneralSearchData(
    val posts: List<Post>?,
    val totalPosts: Int?,
    val limit: Int?,
    val page: Int?,
    val totalPages: Int?,
    val serialNumberStartFrom: Int?,
    val hasNextPage: Boolean?,
    val hasPrevPage: Boolean?,
    val prevPage: Int?,
    val nextPage: Int?
)

