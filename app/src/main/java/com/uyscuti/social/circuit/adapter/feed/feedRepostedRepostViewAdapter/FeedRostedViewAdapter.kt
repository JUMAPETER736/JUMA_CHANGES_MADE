package com.uyscuti.social.circuit.adapter.feed.feedRepostedRepostViewAdapter

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.uyscuti.social.circuit.interfaces.feedinterfaces.FeedTextViewFragmentInterface
import com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.FeedMixedFilesFragment
import com.uyscuti.social.circuit.model.feed.multiple_files.MixedFeedUploadDataClass
import com.uyscuti.social.network.api.response.posts.Post

class FeedRostedViewAdapter (fragmentActivity: FragmentActivity,
   private val feedPost: com.uyscuti.social.network.api.response.posts.OriginalPost
) :
    FragmentStateAdapter(fragmentActivity), FeedTextViewFragmentInterface {


    var fragment = FeedMixedFilesFragment()
    private var feedPostPosition = -1

    fun setFeedPostPosition(feedPostPosition: Int) {
        this.feedPostPosition = feedPostPosition
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }


    override fun createFragment(position: Int): Fragment {
        fragment = FeedMixedFilesFragment()

        fragment.arguments = Bundle().apply {
            putInt("position",position)
            putInt("feedPostPosition",feedPostPosition)

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
    override fun onRePostClickFromFeedTextViewFragment(position: Int, data: Post) {
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
