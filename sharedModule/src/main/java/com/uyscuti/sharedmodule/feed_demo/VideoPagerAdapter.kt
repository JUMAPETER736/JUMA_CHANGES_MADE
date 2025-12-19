package com.uyscuti.sharedmodule.feed_demo

import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.uyscuti.sharedmodule.interfaces.feedinterfaces.FeedTextViewFragmentInterface
import com.uyscuti.social.network.api.response.allFeedRepostsPost.Post

class VideoPagerAdapter(fragmentActivity: FragmentActivity, private val videoUrls: List<String>) :
    FragmentStateAdapter(fragmentActivity), FeedTextViewFragmentInterface {

    private lateinit var backPressedCallback: OnBackPressedCallback
    private var feedTextViewFragmentInterface: FeedTextViewFragmentInterface? = null

    var fragment = VideoFragment()

    override fun getItemCount(): Int = videoUrls.size

    override fun createFragment(position: Int): Fragment {
        fragment = VideoFragment()
        fragment.arguments = Bundle().apply {
            putString("videoUri", videoUrls[position])
        }
        return fragment
    }



    override fun backPressedFromFeedTextViewFragment() {
        Log.d("VideoPagerAdapter", "backPressedFromFeedTextViewFragment: listening in video fragment")
        fragment.backPressedFromFeedTextViewFragment()
    }

    override fun onCommentClickFromFeedTextViewFragment(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {

    }

    override fun onLikeUnLikeFeedFromFeedTextViewFragment(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {

    }

    override fun onFeedFavoriteClickFromFeedTextViewFragment(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {

    }

    override fun onMoreOptionsClickFromFeedTextViewFragment(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {

    }

    override fun finishedPlayingVideo(position: Int) {

    }

    override fun onRePostClickFromFeedTextViewFragment(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.Post
    ) {
        TODO("Not yet implemented")
    }
}
