package com.uyscuti.social.circuit.User_Interface.media

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.uyscuti.social.circuit.data.model.Comment
import com.uyscuti.social.circuit.utils.formatDuration
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.ActivityCommentVideoPlayerBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

class CommentVideoPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommentVideoPlayerBinding
    private var player: ExoPlayer? = null
    private var isPlaying = false
    private var isUserSeeking = false
    private var reply:Boolean = false

    private val playbackStateListener: Player.Listener = playbackStateListener()
    private fun updateSeekBar() {
        player?.let { player ->
            if (!isUserSeeking) {
                val currentPosition = player.currentPosition.toInt()
                binding.seekBar.progress = currentPosition
            }
        }
    }

    var videoUrl = ""
    var owner = ""
    private var position by Delegates.notNull<Int>()
    private var data: Comment? = null
    private var currentReplyComment: com.uyscuti.social.network.api.response.commentreply.allreplies.Comment? =
        null
    private var updateReplyLikes:Boolean = false
    private var updateLike:Boolean = false


    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        val windowInsetsController =
//            WindowCompat.getInsetsController(window, window.decorView)
//
//        windowInsetsController.systemBarsBehavior =
//            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//
//        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { view, windowInsets ->
//
//            if (windowInsets.isVisible(WindowInsetsCompat.Type.statusBars())) {
//                windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())
//            }
//
//            ViewCompat.onApplyWindowInsets(view, windowInsets)
//        }
        binding = ActivityCommentVideoPlayerBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

//        val videoUrl = intent.
        data = intent?.extras?.getSerializable("data") as Comment?
        position = intent?.getIntExtra("position", 0)!!
        val updateReplyLike = intent?.getBooleanExtra("updateReplyLike", false)
        currentReplyComment =
            intent?.extras?.getSerializable("currentItem") as com.uyscuti.social.network.api.response.commentreply.allreplies.Comment?

        videoUrl = intent.getStringExtra("videoUrl").toString()
        owner = intent.getStringExtra("owner").toString()
//        if (data != null) {
//            videoUrl = data!!.videos[0].url
//            owner = data!!.author?.account?.username ?: "Username"
//        }
        binding.toolbar.apply {
            username.text = owner
            backIcon.setOnClickListener {
                onReturn()
            }
            replyIcon.setOnClickListener {
                reply = true
                onReturn()
            }
            if (updateReplyLike == true) {
                if (currentReplyComment?.isLiked == true) {
                    likeIcon.setImageResource(R.drawable.filled_favorite_like)
                } else {
                    likeIcon.setImageResource(R.drawable.like_svgrepo_com_white)
                }
            } else {
                if (data!!.isLiked) {
                    likeIcon.setImageResource(R.drawable.filled_favorite_like)
                } else {
                    likeIcon.setImageResource(R.drawable.like_svgrepo_com_white)
                }
            }

            likeIcon.setOnClickListener {
                if (updateReplyLike == true){
//                    updateReplyLike = true
                    updateReplyLikes = true
                    updateLike = false
                    currentReplyComment?.isLiked = !currentReplyComment?.isLiked!!
                    if (currentReplyComment!!.isLiked) {
                        likeIcon.setImageResource(R.drawable.filled_favorite_like)
                        YoYo.with(Techniques.Tada)
                            .duration(700)
                            .repeat(1)
                            .playOn(likeIcon)
                    } else {
                        likeIcon.setImageResource(R.drawable.like_svgrepo_com_white)
                        YoYo.with(Techniques.Tada)
                            .duration(700)
                            .repeat(1)
                            .playOn(likeIcon)
                    }
                }else{
                    data!!.isLiked = !data!!.isLiked
                    updateReplyLikes = false
                    updateLike = true
                    if (data!!.isLiked) {
                        likeIcon.setImageResource(R.drawable.filled_favorite_like)
                        YoYo.with(Techniques.Tada)
                            .duration(700)
                            .repeat(1)
                            .playOn(likeIcon)
                    } else {
                        likeIcon.setImageResource(R.drawable.like_svgrepo_com_white)
                        YoYo.with(Techniques.Tada)
                            .duration(700)
                            .repeat(1)
                            .playOn(likeIcon)
                    }
                }
            }

        }
        player = ExoPlayer.Builder(this).build()

        binding.playerView.player = player

        // Disable default controls
        binding.playerView.useController = false
        val mediaItem = MediaItem.fromUri(videoUrl)

        player!!.setMediaItem(mediaItem)
        player!!.prepare()
        player!!.play()
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    player?.seekTo(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        player?.repeatMode = Player.REPEAT_MODE_ONE
        player?.addListener(playbackStateListener)

        player?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                super.onPlaybackStateChanged(state)
                if (state == Player.STATE_READY) {
                    binding.seekBar.max = player?.duration?.toInt()!!
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onPositionDiscontinuity(reason: Int) {
                updateSeekBar()
            }

        })

        binding.playerView.setOnClickListener {
            isPlaying = !isPlaying
            if (isPlaying) {
                binding.imageView.visibility = View.VISIBLE
                binding.imageView.setImageResource(R.drawable.comment_play_button)
                player?.pause()
            } else {
                binding.imageView.visibility = View.GONE
                player?.play()
            }

        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle the back button press here
                Log.d("onBackPressed", "Back button pressed")
                onReturn()
            }
        }

        // Add the callback to the back button dispatcher
        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun onReturn() {

        Log.d("onReturn", "data is liked ${data?.isLiked}")
        Log.d("onReturn", "data is liked ${currentReplyComment?.isLiked}")
        val resultIntent = Intent()
        resultIntent.putExtra("data", data)
        resultIntent.putExtra("reply", reply)
        resultIntent.putExtra("updateReplyLikes", updateReplyLikes)
        resultIntent.putExtra("updateLike", updateLike)
        resultIntent.putExtra("currentReplyComment", currentReplyComment)
        resultIntent.putExtra("position", position)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.removeListener(playbackStateListener)
        player?.release()
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onResume() {
        super.onResume()
        player?.play()
    }

    private fun playbackStateListener() = object : Player.Listener {
        @SuppressLint("SetTextI18n")
        override fun onPlaybackStateChanged(state: Int) {
            when (state) {
                ExoPlayer.STATE_ENDED -> {
                }

                Player.STATE_BUFFERING -> {

                }

                Player.STATE_IDLE -> {
                }

                Player.STATE_READY -> {

                    Log.d("TAG", "STATE_READY")
                    val durationMs: Long? = player?.duration

                    if (durationMs != null) {
                        val formattedDuration = formatDuration(durationMs)
                        binding.finalDuration.text = formattedDuration
                    } else {
                        binding.finalDuration.text = "Duration not available"
                    }
                    startUpdatingSeekBar()
                }

                else -> {
                    Log.d("TAG", "STOP SEEK BAR")
                    stopUpdatingSeekBar()
                }
            }
        }

        private var updateSeekBarJob: Job? = null

        private fun startUpdatingSeekBar() {
            updateSeekBarJob = CoroutineScope(Dispatchers.Main).launch {
                while (true) {

                    updateSeekBar()
                    delay(50) // Update seek bar every second (adjust as needed)
                    val currentPositionMs = player?.currentPosition ?: 0

                    // Format current playback position into "mm:ss" format
                    val formattedPosition = formatDuration(currentPositionMs)

                    // Update TextView with formatted playback position
                    binding.currentDuration.text = formattedPosition

                }
            }
        }

        private fun stopUpdatingSeekBar() {
            updateSeekBarJob?.cancel()
        }

        override fun onIsPlayingChanged(isVideoPlaying: Boolean) {
//        super.onIsPlayingChanged(isPlaying)

        }

        override fun onEvents(player: Player, events: Player.Events) {
//        super.onEvents(player, events)
//            if (events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED) ||
//                events.contains(Player.EVENT_IS_PLAYING_CHANGED)
//            ) {
//
////                progressBar.visibility = View.GONE
//            }
//
//            if (events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION)
//            ) {
////                player.seekTo(5000L)
//            }
        }
    }

}