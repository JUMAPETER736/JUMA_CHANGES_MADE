package com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments

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
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.uyscuti.sharedmodule.R
import com.uyscuti.sharedmodule.adapter.feed.feed.multiple_files.MultipleFeedAudiosAdapter
import com.uyscuti.sharedmodule.adapter.feed.feed.multiple_files.PlayFeedAudioInterface
import com.uyscuti.sharedmodule.databinding.FragmentFeedAudioViewBinding
import com.uyscuti.sharedmodule.interfaces.feedinterfaces.FeedTextViewFragmentInterface
import com.uyscuti.sharedmodule.model.FeedCommentClicked
import org.greenrobot.eventbus.EventBus
import com.uyscuti.social.network.api.response.posts.Post
import java.io.IOException
import java.util.Locale


// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FeedAudioViewFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
private const val TAG = "FeedAudioViewFragment"

class FeedAudioViewFragment : Fragment(), PlayFeedAudioInterface {

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FeedAudioViewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private var param1: String? = null
    private var param2: String? = null
    private lateinit var data: Post
    private var position = 0
    private lateinit var backPressedCallback: OnBackPressedCallback
    private var feedTextViewFragmentInterface: FeedTextViewFragmentInterface? = null

    private lateinit var binding: FragmentFeedAudioViewBinding
    private var mediaPlayer: MediaPlayer? = null
    private var audioPlayingPosition = -1
    private var previousPosition = -1
    private var seekBar: SeekBar? = null
    private var currentDuration: TextView? = null
    private var pausePlayButton: ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            position = it.getInt("position")
            data = (it.getSerializable("data") as  Post?)!!
        }
    }

    private var handler: Handler? = null
    private var isPlaying = false
    private lateinit var updateSeekBarRunnable: Runnable
    private var adapter: MultipleFeedAudiosAdapter? = null

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        /**this code has been uncommented**/
        binding.playPauseButton.setOnClickListener {
            Log.d(TAG, "onCreateView: play button clicked ${mediaPlayer?.isPlaying}")
            Toast.makeText(requireContext(), "playbutton", Toast.LENGTH_SHORT).show()

        }

        binding = FragmentFeedAudioViewBinding.inflate(layoutInflater, container, false)
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
            .load(data.author!!.account.avatar.url)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .placeholder(R.drawable.profilepic2)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.toolbar.feedProfilePic)

        binding.re.setOnClickListener {


        }
        binding.toolbar.backIcon.setOnClickListener {

            releaseMediaPlayer()

            if (feedTextViewFragmentInterface != null) {
                feedTextViewFragmentInterface?.backPressedFromFeedTextViewFragment()
            }
        }
        binding.comment.setOnClickListener {
            feedTextViewFragmentInterface?.onCommentClickFromFeedTextViewFragment(position, data)

            EventBus.getDefault().post(FeedCommentClicked(position, data))
        }


        binding.toolbar.username.text = data.author!!.account.username
        if (data.likes <= 0) {
            binding.likesCount.text = "0"
        } else {
            binding.likesCount.text = data.likes.toString()
        }

        if (data.isLiked) {
            binding.like.setImageResource(R.drawable.filled_favorite_like)
        } else {
            binding.like.setImageResource(R.drawable.like_svgrepo_com)
        }
        binding.like.setOnClickListener {
            data.isLiked = !data.isLiked
            if (data.isLiked) {
                Log.d(TAG, "onCreateView: data likes ${data.likes}")
                binding.likesCount.text = data.likes.toString()
                if (data.likes < 0) {
                    binding.likesCount.text = "0"
                } else {
                    binding.likesCount.text = (data.likes + 1).toString()
                }
                binding.like.setImageResource(R.drawable.filled_favorite_like)
                YoYo.with(Techniques.Tada)
                    .duration(700)
                    .repeat(1)
                    .playOn(binding.like)
            } else {
                Log.d(TAG, "onCreateView: data likes ${data.likes}")
                binding.likesCount.text = data.likes.toString()
                if (data.likes <= 0) {
                    binding.likesCount.text = "0"
                } else {
                    binding.likesCount.text = (data.likes - 1).toString()
                }
                binding.like.setImageResource(R.drawable.like_svgrepo_com)
                    YoYo.with(Techniques.Tada)
                    .duration(700)
                    .repeat(1)
                    .playOn(binding.like)
            }

            feedTextViewFragmentInterface?.onLikeUnLikeFeedFromFeedTextViewFragment(position, data)

        }
        if (data.isBookmarked) {
            binding.fav.setImageResource(R.drawable.filled_favorite)
        } else {
            binding.fav.setImageResource(R.drawable.favorite_svgrepo_com__1_)
        }
        binding.moreOptions.setOnClickListener {
            feedTextViewFragmentInterface?.onMoreOptionsClickFromFeedTextViewFragment(
                position,data
            )
        }
        binding.fav.setOnClickListener {
            data.isBookmarked = !data.isBookmarked
            feedTextViewFragmentInterface?.onFeedFavoriteClickFromFeedTextViewFragment(
                position,
                data
            )
            if (data.isBookmarked) {
                binding.fav.setImageResource(R.drawable.filled_favorite)
                YoYo.with(Techniques.Tada)
                    .duration(700)
                    .repeat(1)
                    .playOn(binding.fav)
            } else {
                binding.fav.setImageResource(R.drawable.favorite_svgrepo_com__1_)
                YoYo.with(Techniques.Tada)
                    .duration(700)
                    .repeat(1)
                    .playOn(binding.fav)
            }
        }
        binding.re.setOnClickListener {
            data.isReposted = !data.isReposted

            Toast.makeText(requireContext(),"REPOST IS CLICKED ", Toast.LENGTH_SHORT).show()
            if (data.isReposted){

                YoYo.with(Techniques.Tada)
                    .duration(700)
                    .repeat(1)
                    .playOn(binding.re)
            }else{
                YoYo.with(Techniques.Tada)
                    .duration(700)
                    .repeat(1)
                    .playOn(binding.re)
            }

        }
        val audioList: MutableList<String> = mutableListOf()
        if (data.files.isNotEmpty()) {
            for (audio in data.files) {

                audioList.add(audio.url)
            }
        } else {
            Log.d(TAG, "render: data files is empty")
        }

        adapter = MultipleFeedAudiosAdapter(requireContext(), audioList, this@FeedAudioViewFragment)
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
                        this@FeedAudioViewFragment.isPlaying = false
                        seekBar?.let { currentDuration?.let { it1 -> resetUI(seekBar = it, it1) } }

                        adapter?.refreshAudio(audioPlayingPosition)
                        pausePlayButton?.setImageResource(R.drawable.play_svgrepo_com)
                        pause()
                        stop()
                        release()

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

                    }

                    ViewPager2.SCROLL_STATE_DRAGGING -> {
                        // The user is dragging the pager.

                    }


                    ViewPager2.SCROLL_STATE_SETTLING -> {
                        // The pager is settling to a final position.

                    }
                }
            }
        })
        adapter?.setAudioData(data)
        // Setup CircleIndicator for ViewPager2

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
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                this@FeedAudioViewFragment.isPlaying = true
                pausePlayButton.setImageResource(R.drawable.baseline_pause_white_24)
                handler = Handler(Looper.getMainLooper())
                try {
                    updateSeekBarRunnable = object : Runnable {
                        override fun run() {
                            try {
                                this@FeedAudioViewFragment.seekBar?.progress =
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
                this@FeedAudioViewFragment.isPlaying = false
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


    }



    override fun onResume() {
        Log.d(TAG, "onResume: ")
        super.onResume()
        handler?.removeCallbacks(updateSeekBarRunnable) // Remove seekbar update runnable callbacks
        handler = null
        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle back press

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


    override fun onAudioPlayClickListener(
        audioUrl: String,
        playImageView: ImageView,
        seekBar: SeekBar,
        currentDuration: TextView
    ) {
        Log.d(TAG, "onAudioPlayClickListener: start playing audio")

        this.pausePlayButton = playImageView
        this.seekBar = seekBar
        this.currentDuration = currentDuration
        setupMediaPlayer(audioUrl, seekBar, playImageView, currentDuration)
        setupPlaybackControls(playImageView, seekBar)

    }
}