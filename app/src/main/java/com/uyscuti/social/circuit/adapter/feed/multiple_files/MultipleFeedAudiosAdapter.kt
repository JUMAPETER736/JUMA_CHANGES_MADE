package com.uyscuti.social.circuit.adapter.feed.multiple_files

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
import com.uyscuti.social.circuit.utils.commaSeparatedStringToList
import com.uyscuti.social.circuit.R

private const val TAG = "MultipleFeedAudiosAdapter"
class MultipleFeedAudiosAdapter(
    private var context: Context,
    private var images: List<String>,
    private var playFeedAudioInterface: PlayFeedAudioInterface,


    ) :
    RecyclerView.Adapter<MultipleFeedAudiosAdapter.Pager2ViewHolder>() {
    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val playPauseButton: ImageView = itemView.findViewById(R.id.playPauseButton)

        val audioDuration: TextView = itemView.findViewById(R.id.audioDuration)
        val leftAudioDuration: TextView = itemView.findViewById(R.id.leftAudioDuration)
        val seekBar: SeekBar = itemView.findViewById(R.id.seekBar)
        val artworkVn : ImageView = itemView.findViewById(R.id.artworkVn)
        val artworkImageView : ImageView = itemView.findViewById(R.id.artworkImageView)


    }

    private var data: com.uyscuti.social.network.api.response.posts.Post?= null
    fun setAudioData(data: com.uyscuti.social.network.api.response.posts.Post) {
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
                playFeedAudioInterface.onAudioPlayClickListener(
                    audioUrl, holder.playPauseButton, holder.seekBar, holder.leftAudioDuration)
            }
        }
        // Find the file name using fileId
        val fileIdToFind = data?.files?.get(position)?.fileId
        val fileName = data?.fileNames?.find { it.fileId == fileIdToFind }?.fileName
        Log.d(TAG, "onBindViewHolder: file name $fileName")

        fileName?.let {
            when {
                it.endsWith(".mp3", ignoreCase = true) -> {
                    holder.artworkImageView.visibility = View.VISIBLE // Replace with your mp3 image
                }

                it.endsWith(".ogg", ignoreCase = true) -> {
                    holder.artworkVn.visibility = View.VISIBLE // Replace with your ogg image
                }

                it.endsWith(".wav", ignoreCase = true) -> {
                    holder.artworkImageView.visibility = View.VISIBLE // Replace with your wav image
                }
                it.endsWith(".m4a", ignoreCase = true) -> {
                    holder.artworkImageView.visibility = View.VISIBLE // Replace with your m4a image
                }
                it.endsWith(".flac", ignoreCase = true) -> {
                    holder.artworkImageView.visibility = View.VISIBLE // Replace with your flac image
                }
                it.endsWith(".aac", ignoreCase = true) -> {
                    holder.artworkImageView.visibility = View.VISIBLE // Replace with your aac image
                }
                it.endsWith(".wma", ignoreCase = true) -> {
                    holder.artworkImageView.visibility = View.VISIBLE // Replace with your wma image
                }



                else -> {
                    holder.artworkImageView.setImageResource(R.drawable.music_placeholder) // Default image for other formats
                }
            }
        }


        holder.playPauseButton.setOnClickListener {
            if (audioUrl != null) {
                playFeedAudioInterface.onAudioPlayClickListener(
                    audioUrl, holder.playPauseButton, holder.seekBar, holder.leftAudioDuration)

            }else{

                Log.d(TAG, "onBindViewHolder: audio url is null")

            }
        }
    }
    override fun getItemCount(): Int {
        return images.size
    }
}
interface PlayFeedAudioInterface {

    fun onAudioPlayClickListener (
        audioUrl: String,
        playImageView: ImageView,
        seekBar: SeekBar,
        currentDuration: TextView
    )
}