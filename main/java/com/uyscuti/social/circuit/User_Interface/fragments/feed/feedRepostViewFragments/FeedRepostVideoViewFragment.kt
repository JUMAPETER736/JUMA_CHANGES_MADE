package com.uyscuti.social.circuit.User_Interface.fragments.feed.feedRepostViewFragments

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
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.adapter.feed.feedRepostViewAdapter.FeedRepostMultipleAudioAdapter
import com.uyscuti.social.circuit.adapter.feed.multiple_files.PlayFeedVideoInterface
import com.uyscuti.social.circuit.databinding.FragmentFeedRepostVideoViewBinding
import com.uyscuti.social.circuit.feed_demo.VideoPagerAdapter
import com.uyscuti.social.circuit.interfaces.feedinterfaces.FeedTextViewFragmentInterface

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
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var data: com.uyscuti.social.network.api.response.allFeedRepostsPost.OriginalPost
    private var position = 0
    private var videoPlayingPosition = -1;
    private lateinit var backPressedCallback: OnBackPressedCallback
    private var feedTextViewFragmentInterface: FeedTextViewFragmentInterface? = null

    var adapter: FeedRepostMultipleAudioAdapter? = null
    private var adapter2: VideoPagerAdapter? = null

    //    private var isPlaying = false
    private var isUserSeeking = false
    private var currentDuration: TextView? = null

    //    private var reply: Boolean = false
//
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
//                if (player != null) {
//                    player?.removeListener(playbackStateListener)
//                    player?.stop()
//                    player?.release()
//                    player?.clearVideoSurface()
//                }
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
        var previousPosition: Int = -1
//        val videoUrls = listOf("url1", "url2", "url3")
//        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
//        viewPager.adapter = VideoPagerAdapter(this, videoUrls)
        adapter2 = VideoPagerAdapter(requireActivity(), videoList)
//        feedTextViewFragmentInterface?.let { adapter2!!.setFeedTextViewFragmentInterface(it) }

//        adapter = MultipleFeedVideosAdapter(requireContext(), videoList, this)
        binding.viewPager.adapter = adapter2
        // Get the RecyclerView from ViewPager2
//        val recyclerView: RecyclerView = binding.viewPager.getChildAt(0) as RecyclerView
//
//// Set the ItemAnimator to null to disable animations
//        recyclerView.itemAnimator = null
        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
//        binding.viewPager.registerOnPageChangeCallback(object :
//            ViewPager2.OnPageChangeCallback() {
//
        if (data.author.isEmpty()){
            Glide.with(this)
                .load(data.author[0].account.avatar.url)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .placeholder(R.drawable.profilepic2)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.toolbar.feedProfilePic)

        }else{

            Glide.with(this)
                .load(R.drawable.profilepic2)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .placeholder(R.drawable.profilepic2)

        }


        return binding.root
    }

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

    override fun onResume() {
        Log.d(TAG, "onResume: ")
        super.onResume()
        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle back press
//                navigateBack()
                if (feedTextViewFragmentInterface != null) {
                    feedTextViewFragmentInterface?.backPressedFromFeedTextViewFragment()
//                    if (player != null) {
//                        player?.removeListener(playbackStateListener)
//                        player?.stop()
//                        player?.release()
//                    }
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
//        player?.removeListener(playbackStateListener)
//        player?.release()
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