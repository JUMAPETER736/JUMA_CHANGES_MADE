package com.uyscuti.sharedmodule.interfaces.feedinterfaces

import com.uyscuti.social.core.common.data.room.entity.UserShortsEntity
import com.uyscuti.social.network.api.response.allFeedRepostsPost.OriginalPost
import com.uyscuti.social.network.api.response.posts.Post


interface FeedTextViewFragmentInterface {

    fun backPressedFromFeedTextViewFragment()
    fun onCommentClickFromFeedTextViewFragment(position: Int, data: Post)
    fun onLikeUnLikeFeedFromFeedTextViewFragment(position: Int, data: Post)
    fun onFeedFavoriteClickFromFeedTextViewFragment(position: Int, data: Post)
    fun onMoreOptionsClickFromFeedTextViewFragment(position: Int, data: Post)
    fun finishedPlayingVideo (position: Int)
    fun onRePostClickFromFeedTextViewFragment(position: Int, data: Post)
}

interface FeedRepostOptionsInterface{
    fun onRepostShareOptions(position: Int, data: OriginalPost)
}

interface ToggleFeedFloatingActionButton {
    fun hideFloatingActionButton()
    fun displayFloatingActionButton()
}

interface OnShortThumbnailClickListener {
    fun onShortClick(
        shortsProfile: ArrayList<UserShortsEntity>,
        userShortEntity: UserShortsEntity,
    )
}