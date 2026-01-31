package com.uyscuti.sharedmodule.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uyscuti.sharedmodule.FullScreenImageActivity
import com.uyscuti.sharedmodule.R
import io.getstream.photoview.PhotoView

class MediaPagerAdapter(

    private val mediaUrls: ArrayList<String>,
    private val context: Activity
) :
    RecyclerView.Adapter<MediaPagerAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: PhotoView = itemView.findViewById(R.id.imageView)
        val playerView: PlayerView = itemView.findViewById(R.id.playerView)
        val playButton: ImageView = itemView.findViewById(R.id.playButton)
    }

    private lateinit var player: ExoPlayer

    private var paused = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_view_pager, parent, false)
        return ViewHolder(view)
    }

    @OptIn(UnstableApi::class)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mediaUrl = mediaUrls[position]


        if (isVideoUrl(mediaUrl)) {

            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(mediaUrl, HashMap<String, String>())
                val bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                retriever.release()

                if (bitmap != null) {
                    holder.imageView.setImageBitmap(bitmap)
                } else {
                    // Fallback to Glide for thumbnail
                    Glide.with(context)
                        .load(mediaUrl)
                        .centerCrop()
                        .into(holder.imageView)
                }
            } catch (e: Exception) {
                // Fallback to Glide
                Glide.with(context)
                    .load(mediaUrl)
                    .centerCrop()
                    .into(holder.imageView)
            }

            holder.playButton.visibility = View.VISIBLE
            holder.playerView.visibility = View.GONE
            holder.imageView.visibility = View.VISIBLE

            holder.imageView.setOnClickListener {

                val intent = Intent(context, FullScreenImageActivity::class.java)
                intent.putStringArrayListExtra("imageUrls", mediaUrls)
                intent.putExtra("position", position)
                context.startActivity(intent)

            }

            holder.playButton.setOnClickListener {

                val intent = Intent(context, FullScreenImageActivity::class.java)
                intent.putStringArrayListExtra("imageUrls", mediaUrls)
                intent.putExtra("position", position)
                context.startActivity(intent)
            }


        } else {

            Glide.with(context).load(mediaUrl).centerCrop().into(holder.imageView)

            holder.playButton.visibility = View.GONE
            holder.playerView.visibility = View.GONE
            holder.imageView.visibility = View.VISIBLE



            holder.imageView.setOnClickListener {
                val intent = Intent(context, FullScreenImageActivity::class.java)
                intent.putStringArrayListExtra("imageUrls", mediaUrls)
                intent.putExtra("position", position)
                context.startActivity(intent)
            }
        }

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
    fun isVideoUrl(url: String): Boolean {
        val videoExtensions = listOf(".mp4", ".mkv", ".webm", ".avi", ".mov")
        return videoExtensions.any { url.endsWith(it, ignoreCase = true) }
    }

    fun addImage(imageUri: Uri) {
        if (mediaUrls.isEmpty()) {
            mediaUrls.add(imageUri.toString())
            notifyItemInserted(0)
        } else if(mediaUrls.size == 1) {
            if (isVideoUrl(mediaUrls[0])) {
                mediaUrls.add(imageUri.toString())
                notifyItemInserted(1)
            } else {
                mediaUrls[0] = imageUri.toString()
                notifyItemChanged(0)
            }
        } else if(mediaUrls.size == 2) {
            mediaUrls[1] = imageUri.toString()
            notifyItemChanged(1)
        }

    }

    fun addVideo(videoUri: Uri?) {
        if(mediaUrls.isEmpty()) {
            mediaUrls.add(videoUri.toString())
            notifyItemInserted(0)
        } else if(mediaUrls.size == 1) {
            if(isVideoUrl(mediaUrls[0])){
                mediaUrls[0] = videoUri.toString()
                notifyItemChanged(0)
            } else {
                mediaUrls.add(0,videoUri.toString())
                notifyItemInserted(0)
            }
        } else if(mediaUrls.size == 2) {
            mediaUrls[0] = videoUri.toString()
            notifyItemChanged(0)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addMediaUrl(uri: Uri) {
        mediaUrls.add(uri.toString())
        notifyDataSetChanged()
    }
}