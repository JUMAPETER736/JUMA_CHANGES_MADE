package com.uyscuti.social.circuit.User_Interface.uploads.feed_uploads.adapter

import android.annotation.SuppressLint
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uyscuti.social.circuit.User_Interface.uploads.feed_uploads.models.SelectFeedVideoDataClass
import com.uyscuti.social.circuit.R
import java.io.File

private const val TAG =""
class SelectFeedVideoAdapter(
    private val videoList: List<SelectFeedVideoDataClass>,
    private val onSelectedFilesCount: SelectedFilesCount,
    private val onItemClick: (String) -> Unit
) :
    RecyclerView.Adapter<SelectFeedVideoAdapter.VideoViewHolder>() {
    private var videosSelected: MutableList<SelectFeedVideoDataClass> = mutableListOf()
    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val videoThumbnail: ImageView = itemView.findViewById(R.id.videoThumbnail)
        val videoItemLayout: FrameLayout = itemView.findViewById(R.id.videoItemLayout)
        val videosThumbnail: ImageView = itemView.findViewById(R.id.videosThumbnail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.select_feed_video_adapter_layout_item, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val videoPath = videoList[position]

        Glide.with(holder.itemView.context)
            .load(Uri.fromFile(File(videoPath.videoList)))
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .centerCrop()
            .into(holder.videoThumbnail)
        if(videoPath.isSelected) {
//            holder.videoThumbnail.setBackgroundColor(holder.videoThumbnail.context.resources.getColor(R.color.black))
            holder.videosThumbnail.visibility = View.VISIBLE
        }else {
            holder.videosThumbnail.visibility = View.INVISIBLE
        }
        val durationTextView: TextView = holder.itemView.findViewById(R.id.durationTextView)
        val sizeTextView: TextView = holder.itemView.findViewById(R.id.sizeTextView)

//        Log.d("VideoPath", videoPath)

        try {
            val mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(videoPath.videoList)

            val duration =
                mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)

            durationTextView.text = formatDuration(duration?.toLong() ?: 0)


        } catch (e: Exception) {
            Log.d("VideoPath", "Error: ${e.message}")

            e.printStackTrace()
        }

//        val mediaMetadataRetriever = MediaMetadataRetriever()
//        mediaMetadataRetriever.setDataSource(videoPath)
//
//        val duration = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val size = File(videoPath.videoList).length()

//        durationTextView.text = formatDuration(duration?.toLong() ?: 0)
        sizeTextView.text = formatSize(size)

//        holder.itemView.setOnClickListener {
//            onItemClick(videoPath.videoList)
//            setMultipleSelection(position)
//        }

        holder.videoItemLayout.setOnClickListener {
            onItemClick(videoPath.videoList)
            setMultipleSelection(position)
        }
    }
    fun getSelectedVideos() : MutableList<SelectFeedVideoDataClass> {
        return videosSelected
    }
    private fun setMultipleSelection(adapterPosition: Int) {
        val video = videoList[adapterPosition]

        video.isSelected = !video.isSelected

        if (video.isSelected) {
            videosSelected.add(video)
        } else {
            videosSelected.remove(video)
        }

        Log.d(TAG, "setMultipleSelection: audiosSelected size ${videosSelected.size}")
        onSelectedFilesCount.onFilesCount(videosSelected.size)
        notifyItemChanged(adapterPosition)
    }
    @SuppressLint("DefaultLocale")
    private fun formatDuration(durationMillis: Long): String {
        val durationSeconds = durationMillis / 1000
        val minutes = durationSeconds / 60
        val seconds = durationSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    @SuppressLint("DefaultLocale")
    private fun formatSize(sizeBytes: Long): String {
        val sizeKb = sizeBytes / 1024
        return if (sizeKb < 1024) {
            "$sizeKb KB"
        } else {
            String.format("%.2f MB", sizeKb / 1024.0)
        }
    }


    override fun getItemCount(): Int {
        return videoList.size
    }
}