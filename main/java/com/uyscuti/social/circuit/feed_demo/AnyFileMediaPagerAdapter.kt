package com.uyscuti.social.circuit.feed_demo

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.uyscuti.social.circuit.R
import java.util.concurrent.TimeUnit

class AnyFileMediaPagerAdapter(
    private val mediaUrls: List<String>,
    private val context: Context,
    private val videoThumbnail: String?,
    private val allMediaUrls: ArrayList<String>?,
    private var currentPosition: Int
) : RecyclerView.Adapter<AnyFileMediaPagerAdapter.MediaViewHolder>() {

    private val players = mutableMapOf<Int, ExoPlayer>()
    private var progressUpdateCallback: ((Int, Int, String) -> Unit)? = null
    private var currentItemType: String? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_view_pager, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val mediaUrl = mediaUrls[position]
        currentItemType = when {
            mediaUrl.endsWith(".mp4") || mediaUrl.endsWith(".mkv") -> "video/mp4"
            mediaUrl.endsWith(".mp3") || mediaUrl.endsWith(".wav") || mediaUrl.endsWith(".flac") -> "audio/mp3"
            mediaUrl.endsWith(".ogg") -> "audio/ogg"
            else -> "image/jpeg"
        }

        if (isVideoOrAudio(currentItemType)) {
            holder.imageView.visibility = View.GONE
            holder.playerView.visibility = View.VISIBLE
            setupPlayer(holder.playerView, mediaUrl, position)
        } else {
            holder.playerView.visibility = View.GONE
            holder.imageView.visibility = View.GONE
//            Glide.with(context)
//                .load(mediaUrl)
//                .placeholder(R.drawable.flash21)
//                .error(R.drawable.flash21)
//                .into(holder.imageView)
        }
    }

    override fun getItemCount(): Int = mediaUrls.size

    inner class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.mediaImageView)
        val playerView: PlayerView = itemView.findViewById(R.id.mediaPlayerView)
    }

    private fun setupPlayer(playerView: PlayerView, mediaUrl: String, position: Int) {
        players[position]?.release()
        val player = ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(mediaUrl))
            prepare()
            playWhenReady = position.toLong() == currentPosition // Auto-play if current
            repeatMode = Player.REPEAT_MODE_ONE // Enable looping
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        val duration = duration
                        val position = currentPosition
                        val progress = if (duration > 0) ((position * 100) / duration).toInt() else 0
                        val timeText = formatTime(duration - position)
                        progressUpdateCallback?.invoke(currentPosition.toInt(), progress, timeText)
                    } else if (playbackState == Player.STATE_ENDED) {
                        if (position.toLong() == currentPosition) {
                            seekTo(0)
                            playWhenReady = true // Auto-play after ending
                        }
                    }
                }
            })
        }
        players[position] = player
        playerView.player = player
    }

    @SuppressLint("DefaultLocale")
    private fun formatTime(ms: Long): String {
        return String.format(
            "-%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(ms),
            TimeUnit.MILLISECONDS.toSeconds(ms) % 60
        )
    }

    fun pausePlayer() {
        players[currentPosition]?.pause()
    }

    fun seekTo(positionMs: Long) {
        players[currentPosition]?.seekTo(positionMs)
    }

    fun releasePlayer() {
        players.values.forEach { it.release() }
        players.clear()
    }

    fun setCurrentPosition(position: Int) {
        if (position != currentPosition) {
            players[currentPosition]?.pause()
            currentPosition = position
            players[currentPosition]?.playWhenReady = true
        }
    }

    private fun isVideoOrAudio(itemType: String?): Boolean {
        return itemType?.let { it.startsWith("video/") || it.startsWith("audio/") } ?: false
    }
}