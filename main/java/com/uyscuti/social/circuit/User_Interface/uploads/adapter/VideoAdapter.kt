package com.uyscuti.social.circuit.User_Interface.uploads.adapter

import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uyscuti.social.circuit.R
import java.io.File

class VideoAdapter(private val videoList: List<String>, private val onItemClick: (String) -> Unit) :
    RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val videoThumbnail: ImageView = itemView.findViewById(R.id.videoThumbnail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.video_item_layout, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val videoPath = videoList[position]

        Glide.with(holder.itemView.context)
            .load(Uri.fromFile(File(videoPath)))
            .placeholder(R.drawable.videoplaceholder)
            .error(R.drawable.videoplaceholder)
            .centerCrop()
            .into(holder.videoThumbnail)

        val durationTextView: TextView = holder.itemView.findViewById(R.id.durationTextView)
        val sizeTextView: TextView = holder.itemView.findViewById(R.id.sizeTextView)

//        Log.d("VideoPath", videoPath)

        try {
            val mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(videoPath)

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
        val size = File(videoPath).length()

//        durationTextView.text = formatDuration(duration?.toLong() ?: 0)
        sizeTextView.text = formatSize(size)

        holder.itemView.setOnClickListener {
            onItemClick(videoPath)
        }
    }

    private fun formatDuration(durationMillis: Long): String {
        val durationSeconds = durationMillis / 1000
        val minutes = durationSeconds / 60
        val seconds = durationSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

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
