package com.uyscuti.sharedmodule.eventbus

import com.uyscuti.social.network.api.response.posts.Post


data class FeedFavoriteClick(
    val position: Int,
    val data: Post
)

data class FromOtherUsersFeedFavoriteClick(
    val position: Int,
    val data: Post
)

data class FromOtherUsersFeedCommentClick(
    val position: Int,
    val data: Post
)


data class FeedLikeClick(
    val position: Int,
    val data: Post
)

class HideFeedFloatingActionButton
class ShowFeedFloatingActionButton(bool: Boolean)

data class FromFavoriteFragmentFeedFavoriteClick(
    val position: Int,
    val data: Post
)

data class FromFavoriteFragmentFeedLikeClick(
    val position: Int,
    val data: Post
)

data class FeedUploadResponseEvent(
    val id: String
)

class FeedFavoriteFollowUpdate(
    val userId: String,
    val isFollowing: Boolean
)

class InformOtherUsersFeedProfileFragment(
    val userId: String,
    val isFollowing: Boolean
)

class AllFeedUpdateLike(
    val position: Int,
    val data: Post
)

class InformShortsFragment
class InformShortsFragment2(
    val userId: String,
    val isFollowing: Boolean
)

class InformFeedFragment(
    val userId: String,
    val isFollowing: Boolean
)

data class RepostSuccessEvent(
    val post: com.uyscuti.social.network.api.response.posts.Post
)