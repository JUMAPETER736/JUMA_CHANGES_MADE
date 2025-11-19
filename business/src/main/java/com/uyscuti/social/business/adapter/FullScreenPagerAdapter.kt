

package com.uyscuti.social.business.adapter


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uyscuti.social.business.R


class FullScreenPagerAdapter(
    private val mediaUrls: List<String>,
    private val context: Context
) : RecyclerView.Adapter<FullScreenPagerAdapter.ViewHolder>() {

    private val players = mutableMapOf<Int, ExoPlayer>()
    private var currentPosition = 0

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val playerView: PlayerView = itemView.findViewById(R.id.playerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_view_pager, parent, false)
        return ViewHolder(view)
    }

    @OptIn(UnstableApi::class)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mediaUrl = mediaUrls[position]

        if (isVideoUrl(mediaUrl)) {
            holder.imageView.visibility = View.GONE
            holder.playerView.visibility = View.VISIBLE

            // Create player for this position if it doesn't exist
            if (!players.containsKey(position)) {
                val player = ExoPlayer.Builder(context).build()
                val mediaItem = MediaItem.fromUri(mediaUrl)
                player.setMediaItem(mediaItem)
                player.prepare()
                player.playWhenReady = false
                players[position] = player
            }

            val player = players[position]
            holder.playerView.player = player

            holder.playerView.setShowFastForwardButton(false)
            holder.playerView.setShowRewindButton(false)
            holder.playerView.setShowNextButton(false)
            holder.playerView.setShowPreviousButton(false)
            holder.playerView.useController = true

        } else {
            holder.imageView.visibility = View.VISIBLE
            holder.playerView.visibility = View.GONE

            Glide.with(context).load(mediaUrl).fitCenter().into(holder.imageView)
        }
    }

    fun setCurrentPosition(position: Int) {
        // Pause all players
        pauseAllPlayers()

        currentPosition = position

        // Play current position if it's a video
        if (position < mediaUrls.size && isVideoUrl(mediaUrls[position])) {
            players[position]?.play()
        }
    }

    private fun pauseAllPlayers() {
        players.values.forEach { it.pause() }
    }

    fun releasePlayer() {
        players.values.forEach { player ->
            player.stop()
            player.release()
        }
        players.clear()
    }

    fun pausePlayer() {
        players[currentPosition]?.pause()
    }

    fun playPlayer() {
        if (currentPosition < mediaUrls.size && isVideoUrl(mediaUrls[currentPosition])) {
            players[currentPosition]?.play()
        }
    }

    override fun getItemCount(): Int = mediaUrls.size

    private fun isVideoUrl(url: String): Boolean {
        val videoExtensions = listOf(".mp4", ".mkv", ".webm", ".avi", ".mov")
        return videoExtensions.any { url.endsWith(it, ignoreCase = true) }
    }
}