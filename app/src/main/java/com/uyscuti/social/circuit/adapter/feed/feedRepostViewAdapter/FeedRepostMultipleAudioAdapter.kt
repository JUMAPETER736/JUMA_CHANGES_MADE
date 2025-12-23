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
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.circuit.adapter.feed.multiple_files.PlayFeedAudioInterface
import com.uyscuti.social.circuit.utils.commaSeparatedStringToList
import com.uyscuti.social.circuit.R

private const val TAG = "FeedRepostMultipleAudioAdapter"

class FeedRepostMultipleAudioAdapter (
    private var context: Context,
    private var images: List<String>,
    private var playFeedAudioInterface: PlayFeedAudioInterface,):
    RecyclerView.Adapter<FeedRepostMultipleAudioAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val playPauseButton: ImageView = itemView.findViewById(R.id.playPauseButton)
        //        private val videoItemLayout: ConstraintLayout = itemView.findViewById(R.id.videoItemLayout)
        val audioDuration: TextView = itemView.findViewById(R.id.audioDuration)
        val leftAudioDuration: TextView = itemView.findViewById(R.id.leftAudioDuration)
        val seekBar: SeekBar = itemView.findViewById(R.id.seekBar)

    }

    private var data: com.uyscuti.social.network.api.response.allFeedRepostsPost.OriginalPost?= null
    fun setAudioData(data: com.uyscuti.social.network.api.response.allFeedRepostsPost.OriginalPost) {
        this.data = data
    }

    fun refreshAudio(position: Int)
    {
        notifyItemChanged(position)
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Pager2ViewHolder {
        return Pager2ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.multiple_feed_audios_item, parent, false)
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: Pager2ViewHolder,
        position: Int
    ) {



        if(data != null) {
            val durationString = data?.duration?.let { commaSeparatedStringToList(it.toString()) }
            Log.d(TAG, "onBindViewHolder: duration string $durationString")
            holder.audioDuration.text = durationString?.get(position) ?: "00:00"
        }else {
            holder.audioDuration.text = "00:00"
        }
        val audioUrl = data?.files?.get(position)?.url


        holder.playPauseButton.setOnClickListener {

            Log.d(TAG, "onBindViewHolder: play audio button clicked")
            if (audioUrl != null) {
                playFeedAudioInterface.onAudioPlayClickListener(audioUrl, holder.playPauseButton, holder.seekBar, holder.leftAudioDuration)

            }
        }

        holder.playPauseButton.setOnClickListener {
            if (audioUrl != null) {
                playFeedAudioInterface.onAudioPlayClickListener(audioUrl, holder.playPauseButton, holder.seekBar, holder.leftAudioDuration)

            }else{

                Log.d(TAG, "onBindViewHolder: audio url is null")

            }
        }
    }
    override fun getItemCount(): Int {
        return images.size
    }
}
