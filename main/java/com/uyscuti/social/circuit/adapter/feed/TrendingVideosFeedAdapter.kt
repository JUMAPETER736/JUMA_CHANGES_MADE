package com.uyscuti.social.circuit.adapter.feed

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

import com.uyscuti.social.circuit.R
import com.uyscuti.social.core.common.data.room.entity.ShortsEntity

class TrendingVideosFeedAdapter(
    private var videos: MutableList<ShortsEntity>
) : RecyclerView.Adapter<TrendingVideosFeedAdapter.VideoViewHolder>() {

    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnail: ImageView = itemView.findViewById(R.id.ivThumbnail)
        val duration: TextView = itemView.findViewById(R.id.tvDuration)
        val title: TextView = itemView.findViewById(R.id.tvVideoTitle)
        val creator: TextView = itemView.findViewById(R.id.tvCreatorName)
        val views: TextView = itemView.findViewById(R.id.tvViews)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trending_video, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = videos[position]

        holder.duration.text = video.updatedAt
        holder.title.text = video.content
        holder.creator.text = video.author.account.username
        holder.views.text = video.createdAt

        // Load thumbnail using Glide
        Glide.with(holder.itemView.context)
            .load(video.thumbnail.firstOrNull()?.thumbnailUrl) // Use first thumbnail if available
            .placeholder(R.drawable.imageplaceholder)
            .into(holder.thumbnail)

        holder.itemView.setOnClickListener {
            // Handle item click if needed
        }
    }

    override fun getItemCount() = videos.size

    // Update adapter's data and refresh the list
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newVideos: List<ShortsEntity>) {
        videos.clear()
        videos.addAll(newVideos)
        notifyDataSetChanged()
    }
}
