package com.uyscuti.social.circuit.adapter.feed.multiple_files

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uyscuti.social.circuit.model.feed.multiple_files.FeedMultipleVideos
import com.uyscuti.social.circuit.R
import java.io.File

private const val TAG = "MultipleSelectedFeedVideoAdapter"

class MultipleSelectedFeedVideoAdapter(
    private var context: Context,
    private var videos: ArrayList<FeedMultipleVideos>,
    private var multipleVideosListener: MultipleVideosListener,
) :
    RecyclerView.Adapter<MultipleSelectedFeedVideoAdapter.Pager2ViewHolder>() {
    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val thumbnail: ImageView = itemView.findViewById(R.id.img)


        init {
            thumbnail.setOnClickListener {
                val position = absoluteAdapterPosition
                Log.d(TAG, "${position + 1}: ")
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Pager2ViewHolder {
        return Pager2ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.multiple_feed_images_item, parent, false)
        )
    }

    fun getVideoDetails(position: Int): FeedMultipleVideos {
        return videos[position]
    }

    fun getVideoDetails(): ArrayList<FeedMultipleVideos> {
        return videos
    }

    fun updateSelectedVideo(position: Int, feedVideo: FeedMultipleVideos) {
        videos[position] = feedVideo
        notifyItemChanged(position)
    }

    override fun onBindViewHolder(
        holder: Pager2ViewHolder,
        position: Int
    ) {

        val videoData = videos[position]


        multipleVideosListener.onVideoDisplay(videos[position])
        if (videoData.thumbnail != null) {
            Glide.with(context)
                .load(videoData.thumbnail)
                .into(holder.thumbnail)
        } else {
            Glide.with(context)
                .load(Uri.fromFile(File(videos[position].videoPath)))
                .into(holder.thumbnail)
        }


        holder.thumbnail.setOnClickListener {
            multipleVideosListener.onVideoClick()
        }
    }

    override fun getItemCount(): Int {
        return videos.size
    }


}

interface MultipleVideosListener {
    fun onVideoClick()
    fun onVideoDisplay(details: FeedMultipleVideos)
}