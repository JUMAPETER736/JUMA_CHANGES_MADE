package com.uyscuti.sharedmodule.adapter.feed.feed.multiple_files

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.FeedMixedFilesFragment
import com.uyscuti.sharedmodule.interfaces.feedinterfaces.FeedTextViewFragmentInterface

class  FeedMixedFilesViewPagerAdapter(

    fragmentActivity: FragmentActivity,
    private val feedPost: com.uyscuti.social.network.api.response.posts.Post) :
    FragmentStateAdapter(fragmentActivity), FeedTextViewFragmentInterface {


    var fragment = FeedMixedFilesFragment()
    private var feedPostPosition = -1

    fun setFeedPostPosition(feedPostPosition: Int) {
        this.feedPostPosition = feedPostPosition
    }
    override fun getItemCount(): Int = feedPost.files.size

    override fun createFragment(position: Int): Fragment {
        fragment = FeedMixedFilesFragment()

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

    override fun onCommentClickFromFeedTextViewFragment(position: Int, data:com.uyscuti.social.network.api.response.posts.Post) {


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
}
