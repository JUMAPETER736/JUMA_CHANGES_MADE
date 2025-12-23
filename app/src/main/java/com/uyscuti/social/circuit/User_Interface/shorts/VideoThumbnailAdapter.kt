package com.uyscuti.social.circuit.User_Interface.shorts

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.compose.ui.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.circuit.R
import com.bumptech.glide.Glide

class VideoThumbnailAdapter(
    private val thumbnails: List<Bitmap>,
    private val clickListener: ThumbnailClickListener
) : RecyclerView.Adapter<VideoThumbnailAdapter.VideoThumbnailViewHolder>() {

    private var selectedPosition: Int = -1 // Track selected thumbnail



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoThumbnailViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video_thumbnail, parent, false)
        return VideoThumbnailViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoThumbnailViewHolder, @SuppressLint("RecyclerView") position: Int) {
        if (thumbnails.isEmpty()) {
            Log.w("VideoThumbnailAdapter", "No thumbnails available to bind")
            holder.thumbnailImageView.setImageResource(R.drawable.flash) // Use a placeholder
            holder.selectionBorder.visibility = View.GONE
            holder.selectionOverlay.visibility = View.GONE
            return
        }

        val thumbnail = thumbnails[position]

        // Load the thumbnail image with Glide for better performance
        Glide.with(holder.itemView.context)
            .load(thumbnail)
            .centerCrop()
            .error(R.drawable.flash) // Fallback placeholder
            .into(holder.thumbnailImageView)

        // Show selection border/overlay only for the selected position
        holder.selectionBorder.visibility = if (position == selectedPosition) View.VISIBLE else View.GONE
        holder.selectionOverlay.visibility = if (position == selectedPosition) View.VISIBLE else View.GONE

        // Set click listener
        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = position // Update selected position

            // Notify only the affected items for better performance
            if (previousPosition != -1) {
                notifyItemChanged(previousPosition) // Remove selection from previous item
            }
            notifyItemChanged(position) // Add selection to current item

            clickListener.onThumbnailClick(thumbnail)
        }
    }

    override fun getItemCount(): Int = thumbnails.size

    class VideoThumbnailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnailImageView: ImageView = itemView.findViewById(R.id.thumbnailImageView)
        val selectionBorder: View = itemView.findViewById(R.id.selectionBorder)
        val selectionOverlay: View = itemView.findViewById(R.id.selectionOverlay)
    }

    interface ThumbnailClickListener {
        fun onThumbnailClick(thumbnail: Bitmap)
        fun setBackgroundColor(color: Color)
    }
}