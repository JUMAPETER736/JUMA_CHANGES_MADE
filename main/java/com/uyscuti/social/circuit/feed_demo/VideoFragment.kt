package com.uyscuti.social.circuit.feed_demo

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.activity.OnBackPressedCallback
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.uyscuti.social.circuit.interfaces.feedinterfaces.FeedTextViewFragmentInterface
import com.uyscuti.social.circuit.utils.formatDuration
import com.uyscuti.social.circuit.databinding.FragmentVideoBinding
import com.uyscuti.social.circuit.model.feed.multiple_files.MixedFeedUploadDataClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "VideoFragment"

class VideoFragment : Fragment(), FeedTextViewFragmentInterface {

    private lateinit var player: ExoPlayer

    //    private late init var playerView: PlayerView
    private var isUserSeeking = false
    private lateinit var binding: FragmentVideoBinding

    private lateinit var backPressedCallback: OnBackPressedCallback
    private var feedTextViewFragmentInterface: FeedTextViewFragmentInterface? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVideoBinding.inflate(layoutInflater, container, false)
//        playerView = findViewById(R.id.playerView)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializePlayer()
        binding.videoItemLayout.setOnClickListener {
            if (player.isPlaying) {
                player.pause()
                binding.playImageView.visibility = View.VISIBLE
            } else {
                player.play()
                binding.playImageView.visibility = View.GONE
            }
        }
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(requireContext()).build()
        binding.playerView.useController = false
        binding.playerView.player = player
        val videoUri = arguments?.getString("videoUri")
        val mediaItem = videoUri?.let { MediaItem.fromUri(it) }
      if (mediaItem != null) {
            player.setMediaItem(mediaItem)
        }
        player.prepare()
        player.repeatMode = Player.REPEAT_MODE_OFF
        player.addListener(playbackStateListener)
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    player.seekTo(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")
        player.release()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: ")
        binding.playImageView.visibility = View.VISIBLE
        player.pause()
    }

    private var updateSeekBarJob: Job? = null

    private fun startUpdatingSeekBar() {
        updateSeekBarJob = CoroutineScope(Dispatchers.Main).launch {
            while (true) {


                updateSeekBar()
                delay(50) // Update seek bar every second (adjust as needed)
                val currentPositionMs = player.currentPosition ?: 0

                // Format current playback position into "mm:ss" format
                val formattedPosition = formatDuration(currentPositionMs)

                // Update TextView with formatted playback position
                binding.currentDuration.text = formattedPosition

            }
        }
    }

    private fun updateSeekBar() {
//        Log.d(TAG, "updateSeekBar: ")
        player.let { player ->
            if (!isUserSeeking) {
                val currentPosition = player.currentPosition.toInt()
                binding.seekBar.progress = currentPosition
            }
        }
    }

    private fun stopUpdatingSeekBar() {
        updateSeekBarJob?.cancel()
    }

    private val playbackStateListener: Player.Listener = playbackStateListener()
    private fun playbackStateListener() = object : Player.Listener {
        @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
        override fun onPlaybackStateChanged(state: Int) {
            when (state) {
                ExoPlayer.STATE_ENDED -> {
                    Log.d(TAG, "onPlaybackStateChanged: playing video end")
                    binding.playImageView.visibility = View.VISIBLE

                    player.pause()
                    player.seekTo(0)
                    binding.currentDuration.text = "00:00"
                    initializePlayer()

                }

                Player.STATE_BUFFERING -> {

                }

                Player.STATE_IDLE -> {
                }

                Player.STATE_READY -> {

                    Log.d("TAG", "STATE_READY")
                    val durationMs: Long = player.duration
                    binding.seekBar.max = player.duration.toInt()
                    val formattedDuration = formatDuration(durationMs)
                    binding.finalDuration.text = formattedDuration
                    startUpdatingSeekBar()
                }

                else -> {
                    Log.d("TAG", "STOP SEEK BAR")
                    stopUpdatingSeekBar()
                }
            }
        }


        override fun onIsPlayingChanged(isVideoPlaying: Boolean) {

        }

        override fun onEvents(player: Player, events: Player.Events) {

        }
    }

    companion object {
        fun newInstance(videoUri: String) = VideoFragment().apply {
            arguments = Bundle().apply {
                putString("videoUri", videoUri)
            }
        }
    }



    override fun backPressedFromFeedTextViewFragment() {
        Log.d(TAG, "backPressedFromFeedTextViewFragment: listening in video fragment")
        player.release()
        player.removeListener(playbackStateListener)
    }

    override fun onCommentClickFromFeedTextViewFragment(
        position: Int, data:com.uyscuti.social.network.api.response.posts.Post) {

    }

    override fun onLikeUnLikeFeedFromFeedTextViewFragment(
        position: Int, data:com.uyscuti.social.network.api.response.posts.Post) {

    }

    override fun onFeedFavoriteClickFromFeedTextViewFragment(
        position: Int, data:com.uyscuti.social.network.api.response.posts.Post) {

    }

    override fun onMoreOptionsClickFromFeedTextViewFragment(
        position: Int, data:com.uyscuti.social.network.api.response.posts.Post) {

    }

    override fun finishedPlayingVideo(position: Int) {

    }

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
