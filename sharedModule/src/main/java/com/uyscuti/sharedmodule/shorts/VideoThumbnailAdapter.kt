package com.uyscuti.sharedmodule.shorts

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.sharedmodule.R

class VideoThumbnailAdapter(private val thumbnails: List<Bitmap>,   private val clickListener: ThumbnailClickListener) :
    RecyclerView.Adapter<VideoThumbnailAdapter.VideoThumbnailViewHolder>() {

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
            clickListener.onThumbnailClick(thumbnail)
        }
    }

    override fun getItemCount(): Int {
        return thumbnails.size
    }

    class VideoThumbnailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnailImageView: ImageView = itemView.findViewById(R.id.thumbnailImageView)
    }

    interface ThumbnailClickListener {
        fun onThumbnailClick(thumbnail: Bitmap)
    }

}