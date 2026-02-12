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
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.FragmentFeedRepostVideoViewBinding
import com.uyscuti.sharedmodule.feed_demo.VideoPagerAdapter
import com.uyscuti.sharedmodule.interfaces.feedinterfaces.FeedTextViewFragmentInterface


// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


private const val TAG = "FeedRepostVideoViewFragment"

class FeedRepostVideoViewFragment : Fragment() , PlayFeedVideoInterface {

    companion object {

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
    private lateinit var backPressedCallback: OnBackPressedCallback
    private var feedTextViewFragmentInterface: FeedTextViewFragmentInterface? = null

    var adapter: FeedRepostMultipleAudioAdapter? = null
    private var adapter2: VideoPagerAdapter? = null
    var isPaused = false

    var videoUrl = ""
    var owner = ""
    private lateinit var binding: FragmentFeedRepostVideoViewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            data = (it.getSerializable("data") as com.uyscuti.social.network.api.response.allFeedRepostsPost.OriginalPost?)!!
            position = it.getInt("position")
        }
    }

    @SuppressLint("CheckResult")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        activity?.window?.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.black)
        binding = FragmentFeedRepostVideoViewBinding.inflate(inflater, container, false)
        binding.toolbar.backIcon.setOnClickListener {
            if (feedTextViewFragmentInterface != null) {
                feedTextViewFragmentInterface?.backPressedFromFeedTextViewFragment()
                adapter2?.backPressedFromFeedTextViewFragment()

            }
        }
        Log.d(TAG, "onCreateView: data content ${data.content}")
        if (data.content == "") {
            binding.feedTextContent.text = ""
        } else {
            binding.feedTextContent.text = data.content
        }

        if (data.tags.isEmpty()) {
            binding.tags.visibility = View.GONE
        } else {
            binding.tags.visibility = View.VISIBLE
            val formattedTags = data.tags.joinToString(" ") { "#$it" }

            binding.tags.text = formattedTags
        }
        val videoList: MutableList<String> = mutableListOf()
        if (data.files.isNotEmpty()) {
            for (image in data.files) {
                Log.d(TAG, "render: images ${image.url}")
                videoList.add(image.url)
            }
        } else {
            Log.d(TAG, "render: data files is empty")
        }
        videoUrl = data.files[0].url

        adapter2 = VideoPagerAdapter(requireActivity(), videoList)

        binding.viewPager.adapter = adapter2

        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        if (data.author.isEmpty()){
            Glide.with(this)
                .load(data.author[0].account.avatar.url)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .placeholder(R.drawable.flash21)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.toolbar.feedProfilePic)

        }else{

            Glide.with(this)
                .load(R.drawable.flash21)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .placeholder(R.drawable.flash21)

        }


        return binding.root
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

    override fun onDestroy() {
        super.onDestroy()

    }

    fun setListener(listener: FeedTextViewFragmentInterface) {
        feedTextViewFragmentInterface = listener
    }

    @OptIn(UnstableApi::class)
   override fun onPlayClickListener(
        videoUrl: String,
        playerView: PlayerView,
        playImageView: ImageView,
        seekBars: SeekBar,
        currentDuration: TextView
    ) {
    }
}