package com.uyscuti.social.business.adapter

import android.app.Activity
import android.content.Intent
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
import com.uyscuti.social.business.FullScreenImageActivity
import com.uyscuti.social.business.R

class MediaPagerAdapter(
    private val mediaUrls: ArrayList<String>,
    private val context: Activity,
    private val videoThumbnail: String? = null
) :
    RecyclerView.Adapter<MediaPagerAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val playerView: PlayerView = itemView.findViewById(R.id.playerView)
        val playButton: ImageView = itemView.findViewById(R.id.playButton)
    }

    private lateinit var player: ExoPlayer

    private var paused = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_view_pager, parent, false)
        return ViewHolder(view)
    }

    @OptIn(UnstableApi::class)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mediaUrl = mediaUrls[position]


        if (isVideoUrl(mediaUrl)) {

            Glide.with(context).load(videoThumbnail).centerCrop().into(holder.imageView)

            holder.playButton.visibility = View.VISIBLE
            holder.playerView.visibility = View.GONE
            holder.imageView.visibility = View.VISIBLE

            holder.playButton.setOnClickListener {
                holder.playButton.visibility = View.GONE
                holder.imageView.visibility = View.GONE
                holder.playerView.visibility = View.VISIBLE
                player = ExoPlayer.Builder(context).build()
                holder.playerView.player = player

                val mediaItem = MediaItem.fromUri(mediaUrl)
                player.setMediaItem(mediaItem)
                player.prepare()
                player.playWhenReady = true

                holder.playerView.useController = true
                holder.playerView.setShowFastForwardButton(false)
                holder.playerView.setShowRewindButton(false)
                holder.playerView.setShowNextButton(false)
                holder.playerView.setShowPreviousButton(false)
            }


        } else {

            Glide.with(context).load(mediaUrl).centerCrop().into(holder.imageView)
            holder.playButton.visibility = View.GONE
            holder.playerView.visibility = View.GONE
            holder.imageView.visibility = View.VISIBLE

            Glide.with(context)
                .load(mediaUrl)
                .centerCrop()
                .into(holder.imageView)

            holder.imageView.setOnClickListener {
                pickMedia()
                // Handle image click event
                val imagePaths = ArrayList<String>()
                for (url in mediaUrls) {
                    imagePaths.add(url)
                }

                val intent = Intent(context, FullScreenImageActivity::class.java)
                intent.putStringArrayListExtra("imageUrls", imagePaths)
                intent.putExtra("position", position)
                context.startActivity(intent)
            }
        }

    }

    private fun pickMedia() {

    }

    fun releasePlayer() {
        if (::player.isInitialized) {
            player.stop()
            player.release()
            player.clearVideoSurface()
        }

    }
    fun pausePlayer() {
        if (::player.isInitialized) {
            player.pause()
            paused = true
        }
    }
    fun resumePlayer() {
        if (::player.isInitialized && paused) {
            player.play()
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
