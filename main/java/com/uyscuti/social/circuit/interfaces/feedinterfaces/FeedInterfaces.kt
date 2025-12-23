package com.uyscuti.social.circuit.interfaces.feedinterfaces

import android.media.MediaPlayer
import com.uyscuti.social.circuit.model.feed.multiple_files.MixedFeedUploadDataClass
import com.uyscuti.social.core.common.data.room.entity.UserShortsEntity


interface FeedTextViewFragmentInterface {

    fun backPressedFromFeedTextViewFragment()
    fun onCommentClickFromFeedTextViewFragment(position: Int, data: com.uyscuti.social.network.api.response.posts.Post)
    fun onLikeUnLikeFeedFromFeedTextViewFragment(position: Int, data:com.uyscuti.social.network.api.response.posts.Post)
    fun onFeedFavoriteClickFromFeedTextViewFragment(position: Int, data:com.uyscuti.social.network.api.response.posts.Post)
    fun onMoreOptionsClickFromFeedTextViewFragment(position: Int, data:com.uyscuti.social.network.api.response.posts.Post)
    fun finishedPlayingVideo (position: Int)
    fun onRePostClickFromFeedTextViewFragment(position: Int, data:com.uyscuti.social.network.api.response.posts.Post)
    fun onFullScreenClicked(data: MixedFeedUploadDataClass)
    fun onMediaClick(data: MixedFeedUploadDataClass)
    fun onMediaPrepared(mp: MediaPlayer)
    fun onMediaError()
}

interface FeedRepostOptionsInterface{
    fun onRepostShareOptions(position: Int, data:com.uyscuti.social.network.api.response.posts.OriginalPost)
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