package com.uyscuti.sharedmodule.utils

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoPlayerManager @Inject constructor() {

    private var player: ExoPlayer? = null
    private var playerView: PlayerView? = null

    fun initialize(view: PlayerView) {
        playerView = view
    }

    fun loadVideo(
        url: String,
        onReady: () -> Unit,
        onError: (String) -> Unit
    ) {
        playerView?.let { view ->
            player = ExoPlayer.Builder(view.context).build().apply {
                view.player = this

                val mediaItem = MediaItem.fromUri(url)
                setMediaItem(mediaItem)
                prepare()

                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        when (state) {
                            Player.STATE_READY -> onReady()
                            Player.STATE_ENDED -> seekTo(0)
                        }
                    }

                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        onError(error.message ?: "Unknown error")
                    }
                })

                playWhenReady = true
            }
        }
    }

    fun play() {
        player?.play()
    }

    fun pause() {
        player?.pause()
    }

    fun seekTo(positionMs: Long) {
        player?.seekTo(positionMs)
    }

    fun getCurrentProgress(): Int {
        val duration = player?.duration ?: 0
        val position = player?.currentPosition ?: 0
        return if (duration > 0) ((position * 100) / duration).toInt() else 0
    }

    fun release() {
        player?.release()
        player = null
    }
}