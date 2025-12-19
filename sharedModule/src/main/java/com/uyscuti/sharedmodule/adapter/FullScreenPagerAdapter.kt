package com.uyscuti.sharedmodule.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uyscuti.sharedmodule.R

class FullScreenPagerAdapter(private val mediaUrls: List<String>, private val context: Context) :
    RecyclerView.Adapter<FullScreenPagerAdapter.ViewHolder>() {

    private lateinit var player: ExoPlayer



    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: io.getstream.photoview.PhotoView = itemView.findViewById(R.id.imageView)
        val playerView: PlayerView = itemView.findViewById(R.id.playerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_view_pager, parent, false)
        return ViewHolder(view)
    }

    fun releasePlayer() {
        if (::player.isInitialized) {
            player.stop()
            player.release()
        }
    }

    fun pausePlayer() {
        if (::player.isInitialized) {
            player.pause()
        }
    }

    fun playPlayer() {
        if (::player.isInitialized) {
            player.play()
        }
    }

    @OptIn(UnstableApi::class)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mediaUrl = mediaUrls[position]

        if (isVideoUrl(mediaUrl)) {

            holder.imageView.visibility = View.GONE
            holder.playerView.visibility = View.VISIBLE

            player = ExoPlayer.Builder(context).build()
            holder.playerView.player = player

            val mediaItem = MediaItem.fromUri(mediaUrl)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.playWhenReady = false

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

    override fun getItemCount(): Int {
        return mediaUrls.size
    }

    private fun isVideoUrl(url: String): Boolean {
        val videoExtensions = listOf(".mp4", ".mkv", ".webm", ".avi", ".mov")
        return videoExtensions.any { url.endsWith(it, ignoreCase = true) }
    }
}