package com.uyscuti.social.circuit.adapter.feed.feedRepostViewAdapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uyscuti.social.circuit.utils.commaSeparatedStringToList
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.adapter.feed.multiple_files.PlayFeedVideoInterface
import com.uyscuti.social.network.api.response.allFeedRepostsPost.Post

private const val TAG = "FeedRepostMultipleVideoAdapter"

class FeedRepostMultipleVideoAdapter(
    private var context: Context,
    private var images: List<String>,
    private var playFeedVideoInterface: PlayFeedVideoInterface,
    private var type: String = "image"
) :
    RecyclerView.Adapter<FeedRepostMultipleVideoAdapter.Pager2ViewHolder>() {

        private var videoThumbnail = ""

        inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val images: ImageView = itemView.findViewById(R.id.imageView)
            val playImageView: ImageView = itemView.findViewById(R.id.playImageView)
            val videoItemLayout: ConstraintLayout = itemView.findViewById(R.id.videoItemLayout)
            val finalDuration: TextView = itemView.findViewById(R.id.finalDuration)
            val currentDuration: TextView = itemView.findViewById(R.id.currentDuration)
            val playerView: PlayerView = itemView.findViewById(R.id.player_view)
            val seekBar: SeekBar = itemView.findViewById(R.id.seekBar)

            init {
                videoItemLayout.setOnClickListener {
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
                    .inflate(R.layout.multiple_feed_videos_item, parent, false)
            )
        }

        private var data: Post? = null
        fun setVideoData(data: Post) {
            this.data = data
        }

        fun refreshVideo(position: Int) {
            Log.d(TAG, "refreshVideo: ")
            notifyItemChanged(position)
        }
        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(
            holder: Pager2ViewHolder,
            position: Int
        ) {

//        holder.images.setImageResource(images[position])
            if (data != null) {
                val durationString = data?.duration?.let { commaSeparatedStringToList(it.toString()) }
                holder.finalDuration.text = durationString?.get(position) ?: "00:00"
                Glide.with(context)
                    .load(data?.thumbnail?.get(position)?.thumbnailUrl)
                    .into(holder.images)

                videoThumbnail = data?.thumbnail?.get(position)?.thumbnailUrl.toString()
            } else {
                holder.finalDuration.text = "00:00"
            }


            holder.itemView.setOnClickListener {
                Log.d(TAG, "onBindViewHolder: holder.itemView clicked")
                val videoUrl = data?.files?.get(position)?.url
                holder.images.visibility = View.GONE
                holder.seekBar.visibility = View.VISIBLE
                holder.playImageView.visibility = View.GONE
                holder.videoItemLayout.visibility = View.VISIBLE
                if (videoUrl != null) {
                    Log.d(TAG, "onBindViewHolder: seekBar.isVisible: ${holder.seekBar.isVisible}")
                    playFeedVideoInterface.onPlayClickListener(
                        videoUrl,
                        holder.playerView,
                        holder.playImageView,
                        holder.seekBar,
                        holder.currentDuration
                    )
                }
            }
        }

        override fun getItemCount(): Int {
            return images.size
        }

        fun getVideoThumbnail(): String {
            return videoThumbnail
        }
    }

    interface PlayFeedVideoInterface {
        fun onPlayClickListener(
            videoUrl: String,
            playerView: PlayerView,
            playImageView: ImageView,
            seekBars: SeekBar,
            currentDuration: TextView
        )
}