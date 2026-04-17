package com.uyscuti.social.circuit.ui.fragments.feed.feedRepostViewFragments

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.uyscuti.sharedmodule.adapter.feed.feed.feedRepostViewAdapter.FeedRepostMultipleAudioAdapter
import com.uyscuti.sharedmodule.adapter.feed.feed.feedRepostViewAdapter.PlayFeedAudioInterface
import com.uyscuti.sharedmodule.interfaces.feedinterfaces.FeedTextViewFragmentInterface
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.FragmentFeedRepostAudioViewBinding
import com.uyscuti.social.network.api.response.allFeedRepostsPost.OriginalPost
import java.io.IOException
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FeedRepostAudioViewFragment.newInstance] factory method to
 * create an instance of this fragment.
 */private const val TAG = "FeedRepostAudioViewFragment"

class FeedRepostAudioViewFragment() : Fragment(), PlayFeedAudioInterface {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var data: OriginalPost
    private var position = 0
    private lateinit var backPressedCallback: OnBackPressedCallback
    private var feedTextViewFragmentInterface: FeedTextViewFragmentInterface? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioPlayingPosition = -1
    private var previousPosition = -1
    private var seekBar: SeekBar? = null
    private var currentDuration: TextView? = null
    private var pausePlayButton: ImageView? = null
    private lateinit var binding: FragmentFeedRepostAudioViewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            data = (it.getSerializable("data") as OriginalPost?)!!
            position = it.getInt("position")

        }
    }


    private var handler: Handler? = null
    private var isPlaying = false
    private lateinit var updateSeekBarRunnable: Runnable
    private var adapter: FeedRepostMultipleAudioAdapter? = null

    @SuppressLint("SuspiciousIndentation")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFeedRepostAudioViewBinding.inflate(inflater, container, false)

        // Inflate the layout for this fragment
        /**this code has been uncommented**/
        binding.playPauseButton.setOnClickListener {
            Log.d(TAG, "onCreateView: play button clicked ${mediaPlayer?.isPlaying}")
            Toast.makeText(requireContext(), "playbutton", Toast.LENGTH_SHORT).show()

        }

        activity?.window?.navigationBarColor =
            ContextCompat.getColor(requireContext(), R.color.black)
        if (data.content == "") {
            /**
             * this code has been added soon
             */
            binding.feedTextContent.text = ""
        } else {
            binding.feedTextContent.text = data.content
        }
        binding.audioDuration.text = "0:00"
        binding.audioDuration.text = data.duration.toString()
        if (data.tags.isEmpty()) {
            binding.tags.visibility = View.GONE
        } else {
            binding.tags.visibility = View.VISIBLE
            val formattedTags = data.tags.joinToString(" ") { "#$it" }

            binding.tags.text = formattedTags
        }

        mediaPlayer?.release()
        Glide.with(this)
            .load(data.author[0].account.avatar.url)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .placeholder(R.drawable.profilepic2)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.toolbar.feedProfilePic)

        binding.re.setOnClickListener {


        }
        binding.toolbar.backIcon.setOnClickListener {

            if (feedTextViewFragmentInterface != null) {
                feedTextViewFragmentInterface?.backPressedFromFeedTextViewFragment()
            }
        }
        binding.commentButtonIcon.setOnClickListener {

        }


        binding.toolbar.username.text = data.author[0].account.username

        binding.moreOptions.setOnClickListener {

        }

        val audioList: ArrayList<String> = ArrayList()
        if (data.files.isNotEmpty()) {
            for (audio in data.files) {
//                Log.d(TAG, "render: images ${audio.url}")
                audioList.add(audio.url)
            }
        } else {
            Log.d(TAG, "render: data files is empty")
        }

        adapter = FeedRepostMultipleAudioAdapter(
            requireContext(),
            audioList,
            this)

        binding.viewPager.adapter = adapter

        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.viewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                // This method will be invoked when the ViewPager2 is scrolled, but not necessarily settled (user is still swiping)
            }

            override fun onPageSelected(position: Int) {
                // This method will be invoked when a new page becomes selected.
                Log.d("ViewPager2", "Page selected: $position previous position")

                audioPlayingPosition = position
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    adapter?.refreshAudio(previousPosition)
                }, 500)
                if (mediaPlayer == null) {
                    Log.d("ViewPager2", "onPageSelected: mediaPlayer is null")
                    // newly added
                    setupMediaPlayer(data.files[position].url, binding.seekbar, binding.playPauseButton, binding.audioDuration)

                } else {
                    Log.d("ViewPager2", "onPageSelected: player not null")
                    mediaPlayer?.apply {

                        releaseMediaPlayer()
                        this@FeedRepostAudioViewFragment.isPlaying = false
                        seekBar?.let { currentDuration?.let { it1 -> resetUI(seekBar = it, it1) } }

                        adapter?.refreshAudio(audioPlayingPosition)
                        pausePlayButton?.setImageResource(R.drawable.play_svgrepo_com)
                        pause()
                        stop()
                        release()
//                      removeListener(playbackStateListener)
                    }
                    mediaPlayer = null
                }
                previousPosition = position

            }

            override fun onPageScrollStateChanged(state: Int) {
                // Called when the scroll state changes:
                // SCROLL_STATE_IDLE, SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING
                when (state) {
                    ViewPager2.SCROLL_STATE_IDLE -> {
                        // The pager is in an idle, settled state.
//                        Log.d("ViewPager2", "Page selected: SCROLL_STATE_IDLE")
                    }

                    ViewPager2.SCROLL_STATE_DRAGGING -> {
                        // The user is dragging the pager.
//                        Log.d("ViewPager2", "Page selected: SCROLL_STATE_DRAGGING")
                    }

//                    ViewPager2.
                    ViewPager2.SCROLL_STATE_SETTLING -> {
                        // The pager is settling to a final position.
//                        Log.d("ViewPager2", "Page selected: SCROLL_STATE_SETTLING")
                    }
                }
            }
        })
        adapter?.setAudioData(data)
        // Setup CircleIndicator for ViewPager2
//        val indicator = findViewById<CircleIndicator3>(R.id.circleIndicator)
        binding.circleIndicator.setViewPager(binding.viewPager)

        binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    Log.d("TAG", "onProgressChanged: progress $progress")
                    mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Not needed for your case
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Not needed for your case
            }
        })
        return binding.root
        /**this code has been uncommented ends here**/


        return binding.root
    }

}