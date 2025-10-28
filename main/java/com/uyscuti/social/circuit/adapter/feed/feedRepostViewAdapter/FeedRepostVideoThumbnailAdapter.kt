package com.uyscuti.social.circuit.adapter.feed.feedRepostViewAdapter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.circuit.model.feed.multiple_files.FeedMultipleVideos
import com.uyscuti.social.circuit.R

class FeedRepostVideoThumbnailAdapter(
    private val thumbnails: List<Bitmap>,
                                      private val clickListener: ThumbnailClickListener
) :    RecyclerView.Adapter<FeedRepostVideoThumbnailAdapter.VideoThumbnailViewHolder>() {

    private var videoDetails: FeedMultipleVideos? = null
    fun setVideoDetails(videoDetails: FeedMultipleVideos) {
        this.videoDetails = videoDetails
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoThumbnailViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video_thumbnail, parent, false)
        return VideoThumbnailViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoThumbnailViewHolder, position: Int) {
        val thumbnail = thumbnails[position]
        holder.thumbnailImageView.setImageBitmap(thumbnail)
        holder.itemView.setOnClickListener {
            // Handle thumbnail click by calling the callback
            if (videoDetails != null) {
                clickListener.onThumbnailClick(thumbnail, videoDetails = videoDetails!!)
            }

        }
    }

    override fun getItemCount(): Int {
        return thumbnails.size
    }

    inner class VideoThumbnailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnailImageView: ImageView = itemView.findViewById(R.id.thumbnailImageView)
    }

    interface ThumbnailClickListener {
        fun onThumbnailClick(thumbnail: Bitmap, videoDetails: FeedMultipleVideos)
    }

}