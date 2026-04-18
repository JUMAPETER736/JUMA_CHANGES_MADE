package com.uyscuti.social.circuit.ui.fragments.feed.feedRepostViewFragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.uyscuti.sharedmodule.adapter.feed.feed.feedRepostViewAdapter.FeedRepostMultipleAudioAdapter
import com.uyscuti.sharedmodule.adapter.feed.feed.multiple_files.PlayFeedVideoInterface
import com.uyscuti.sharedmodule.databinding.FragmentFeedRepostVideoViewBinding
import com.uyscuti.social.circuit.R

import com.uyscuti.sharedmodule.feed_demo.VideoPagerAdapter
import com.uyscuti.sharedmodule.interfaces.feedinterfaces.FeedTextViewFragmentInterface

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FeedRepostVideoViewFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
private const val TAG = "FeedRepostVideoViewFragment"

class FeedRepostVideoViewFragment : Fragment() , PlayFeedVideoInterface {

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FeedRepostVideoViewFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FeedRepostVideoViewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private var param1: String? = null
    private var param2: String? = null
    private lateinit var data: com.uyscuti.social.network.api.response.allFeedRepostsPost.OriginalPost
    private var position = 0
    private var videoPlayingPosition = -1;
    private lateinit var backPressedCallback: OnBackPressedCallback
    private var feedTextViewFragmentInterface: FeedTextViewFragmentInterface? = null

    var adapter: FeedRepostMultipleAudioAdapter? = null
    private var adapter2: VideoPagerAdapter? = null

    private var isUserSeeking = false
    private var currentDuration: TextView? = null


    var isPaused = false

    var videoUrl = ""
    var owner = ""
    private lateinit var binding: FragmentFeedRepostVideoViewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            data =
                (it.getSerializable("data") as com.uyscuti.social.network.api.response.allFeedRepostsPost.OriginalPost?)!!
            position = it.getInt("position")
        }
    }



    override fun onResume() {
        Log.d(TAG, "onResume: ")
        super.onResume()
        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle back press

                if (feedTextViewFragmentInterface != null) {
                    feedTextViewFragmentInterface?.backPressedFromFeedTextViewFragment()

                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }

}