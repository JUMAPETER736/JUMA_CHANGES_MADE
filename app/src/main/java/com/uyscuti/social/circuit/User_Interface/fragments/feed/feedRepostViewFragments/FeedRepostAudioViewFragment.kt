package com.uyscuti.social.circuit.User_Interface.fragments.feed.feedRepostViewFragments

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
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.adapter.feed.feedRepostViewAdapter.FeedRepostMultipleAudioAdapter
import com.uyscuti.social.circuit.adapter.feed.multiple_files.PlayFeedAudioInterface
import com.uyscuti.social.circuit.databinding.FragmentFeedRepostAudioViewBinding
import com.uyscuti.social.circuit.interfaces.feedinterfaces.FeedTextViewFragmentInterface
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

class FeedRepostAudioViewFragment : Fragment(), PlayFeedAudioInterface {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var data:  com.uyscuti.social.network.api.response.allFeedRepostsPost.OriginalPost
    private var position = 0
    private lateinit var backPressedCallback: OnBackPressedCallback
    private var feedTextViewFragmentInterface: FeedTextViewFragmentInterface? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioPlayingPosition = -1
    private var previousPosition = -1
    private var seekBar: SeekBar? = null
    private var currentDuration: TextView? = null
    private var pausePlayButton: ImageView? = null
    private lateinit var binding : FragmentFeedRepostAudioViewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            data = (it.getSerializable("data") as  com.uyscuti.social.network.api.response.allFeedRepostsPost.OriginalPost?)!!
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

            val audioList: MutableList<String> = mutableListOf()
            if (data.files.isNotEmpty()) {
                for (audio in data.files) {
//                Log.d(TAG, "render: images ${audio.url}")
                    audioList.add(audio.url)
                }
            } else {
                Log.d(TAG, "render: data files is empty")
            }

            adapter = FeedRepostMultipleAudioAdapter(requireContext(), audioList, this@FeedRepostAudioViewFragment)
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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FeedRepostAudioViewFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FeedRepostAudioViewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun setupMediaPlayer(
        audioUrl: String,
        seekBar: SeekBar,
        pausePlayButton: ImageView,
        currentDuration: TextView
    ) {
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            try {
                setDataSource(audioUrl) // Assuming url is valid
                prepareAsync()
            } catch (e: IOException) {
                Log.e(TAG, "Failed to prepare MediaPlayer", e)
            }

            setOnPreparedListener {
                seekBar.max = mediaPlayer?.duration!!
                mediaPlayer?.start()
                this@FeedRepostAudioViewFragment.isPlaying = true
                pausePlayButton.setImageResource(R.drawable.baseline_pause_white_24)
                handler = Handler(Looper.getMainLooper())
                try {
                    updateSeekBarRunnable = object : Runnable {
                        override fun run() {
                            try {
                                this@FeedRepostAudioViewFragment.seekBar?.progress =
                                    mediaPlayer!!.currentPosition
                                updateCounterTextView(mediaPlayer!!.currentPosition, currentDuration)
                                handler!!.postDelayed(this, 1000) // Update seekbar every second
                            }catch (e: Exception){
                                Log.e(TAG, "run: ${e.message}", )
                                e.printStackTrace()
                            }
                        }
                    }
                    handler!!.postDelayed(updateSeekBarRunnable, 0)
                } catch (e: Exception) {
                    Log.e(TAG, "setupMediaPlayer: ${e.message}")
                    e.printStackTrace()
                }
            }

            setOnCompletionListener {
                // Handle completion of audio playback if needed
                releaseMediaPlayer()
                this@FeedRepostAudioViewFragment.isPlaying = false
                resetUI(seekBar, currentDuration)
                adapter?.refreshAudio(audioPlayingPosition)
                pausePlayButton.setImageResource(R.drawable.play_svgrepo_com)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun resetUI(seekBar: SeekBar, currentDuration: TextView) {
        // Reset SeekBar and counter TextView
        seekBar.progress = 0
        currentDuration.text = "0:00"
    }

    private fun updateCounterTextView(currentPosition: Int, currentDuration: TextView) {
        val minutes = currentPosition / 1000 / 60
        val seconds = (currentPosition / 1000) % 60
        val timeString = String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
        currentDuration.text = timeString
    }

    private fun setupPlaybackControls(pausePlayButton: ImageView, seekBar: SeekBar) {
        mediaPlayer?.start()

        pausePlayButton.setOnClickListener {
            Log.d("FeedAudioViewFragment", "setupPlaybackControls: play button clicked")
            if (isPlaying) {
                mediaPlayer?.pause()
                isPlaying = false
                pausePlayButton.setImageResource(R.drawable.play_svgrepo_com)
            } else {
                mediaPlayer?.start()
                isPlaying = true
                pausePlayButton.setImageResource(R.drawable.baseline_pause_white_24)
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
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
    }

    private fun releaseMediaPlayer() {
        handler?.removeCallbacks(updateSeekBarRunnable)
        handler = null
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null

//        mediaPlayer?.stop()
//        mediaPlayer?.release()
    }

    override fun onResume() {
        Log.d(TAG, "onResume: ")
        super.onResume()
        handler?.removeCallbacks(updateSeekBarRunnable) // Remove seekbar update runnable callbacks
        handler = null
        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle back press
//                navigateBack()
                if (mediaPlayer?.isPlaying == true) {
                    mediaPlayer!!.stop()
                }
                mediaPlayer?.release() // Release the MediaPlayer when the fragment is destroyed

                if (feedTextViewFragmentInterface != null) {
                    feedTextViewFragmentInterface?.backPressedFromFeedTextViewFragment()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }

    override fun onPause() {
        super.onPause()
        releaseMediaPlayer()
//        if(mediaPlayer?.isPlaying == true) {
//            mediaPlayer!!.pause()
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release() // Release the MediaPlayer when the fragment is destroyed
        handler?.removeCallbacks(updateSeekBarRunnable) // Remove seekbar update runnable callbacks
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer!!.stop()
        }
        handler = null
    }

    fun setListener(listener: FeedTextViewFragmentInterface) {
        feedTextViewFragmentInterface = listener
    }

    //    var isPlaying = false
    override fun onAudioPlayClickListener(
        audioUrl: String,
        playImageView: ImageView,
        seekBar: SeekBar,
        currentDuration: TextView
    ) {
        Log.d(TAG, "onAudioPlayClickListener: start playing audio")
//        isPlaying = true
        this.pausePlayButton = playImageView
        this.seekBar = seekBar
        this.currentDuration = currentDuration
        setupMediaPlayer(audioUrl, seekBar, playImageView, currentDuration)
        setupPlaybackControls(playImageView, seekBar)

    }

}