package com.uyscuti.social.circuit.adapter.feed.multiple_files

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.uyscuti.social.circuit.interfaces.feedinterfaces.FeedTextViewFragmentInterface
import com.uyscuti.social.circuit.model.feed.multiple_files.MixedFeedUploadDataClass
import com.uyscuti.social.circuit.User_Interface.fragments.feed.feedviewfragments.FeedRepostFilesFragment

class FeedRepostFilesViewPagerAdapter(
    fragmentActivity: FragmentActivity, private val feedPost: com.uyscuti.social.network.api.response.posts.OriginalPost
) :
    FragmentStateAdapter(fragmentActivity), FeedTextViewFragmentInterface {
    var fragment = FeedRepostFilesFragment()
    private var feedPostPosition = -1

    fun setFeedPostPosition(feedPostPosition: Int) {
        this.feedPostPosition = feedPostPosition
    }
    override fun getItemCount(): Int = feedPost.files.size

    override fun createFragment(position: Int): Fragment {
        fragment = FeedRepostFilesFragment()

        fragment.arguments = Bundle().apply {
            putInt("position",position)
            putInt("feedPostPosition",feedPostPosition)
            putSerializable("feedPost", feedPost)
        }
        return fragment
    }



    override fun backPressedFromFeedTextViewFragment() {
        Log.d("VideoPagerAdapter", "backPressedFromFeedTextViewFragment: listening in video fragment")
        fragment.backPressedFromFeedTextViewFragment()
    }

    override fun onCommentClickFromFeedTextViewFragment(
        position: Int, data:com.uyscuti.social.network.api.response.posts.Post) {


    }

    override fun onLikeUnLikeFeedFromFeedTextViewFragment(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {

    }

    override fun onFeedFavoriteClickFromFeedTextViewFragment(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {

    }

    override fun onMoreOptionsClickFromFeedTextViewFragment(position: Int, data:com.uyscuti.social.network.api.response.posts.Post) {

    }

    override fun finishedPlayingVideo(position: Int) { }
    override fun onRePostClickFromFeedTextViewFragment(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.Post
    ) {
        TODO("Not yet implemented")
    }

    override fun onFullScreenClicked(data: MixedFeedUploadDataClass) {
        TODO("Not yet implemented")
    }

    override fun onMediaClick(data: MixedFeedUploadDataClass) {
        TODO("Not yet implemented")
    }

    override fun onMediaPrepared(mp: MediaPlayer) {
        TODO("Not yet implemented")
    }

    override fun onMediaError() {
        TODO("Not yet implemented")
    }
}