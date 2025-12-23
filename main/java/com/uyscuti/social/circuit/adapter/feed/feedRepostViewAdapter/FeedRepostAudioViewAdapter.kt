package com.uyscuti.social.circuit.adapter.feed.feedRepostViewAdapter

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.circuit.adapter.feed.OnMultipleImagesClickListener

import com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.FeedAudioViewFragment
import com.uyscuti.social.circuit.utils.commaSeparatedStringToList
import com.uyscuti.social.circuit.R
import org.greenrobot.eventbus.EventBus


private const val TAG = "FeedAudiosViewAdapter"
private var mediaPlayer: MediaPlayer? = null
private var isPlaying = false
@SuppressLint("StaticFieldLeak")
private var pausePlayButton: ImageView? = null

class FeedRepostAudioViewAdapter (private val imageList: List<String>?, val context: Context) :
    RecyclerView.Adapter<FeedRepostAudioViewAdapter.ViewHolder>() {


        private var onMultipleImagesClickListener: OnMultipleImagesClickListener? = null
        fun setOnMultipleImagesClickListener(l: OnMultipleImagesClickListener) {
            onMultipleImagesClickListener = l
        }

        private var data: com.uyscuti.social.network.api.response.getfeedandresposts.Post? = null
        fun setData(data: com.uyscuti.social.network.api.response.getfeedandresposts.Post) {
            this.data = data
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.feed_view_multiple_audios_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val imageItem = imageList?.get(position)
            if (imageItem != null) {
                holder.bind(imageItem)
            }
        }

        override fun getItemCount(): Int {
            return imageList?.size ?: -1
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            //        private val artworkImageView: ImageView = itemView.findViewById(R.id.artworkImageView)
            private val imageView2: ImageView = itemView.findViewById(R.id.imageView2)
            private val cardView: CardView = itemView.findViewById(R.id.cardView)
            private val countTextView: TextView = itemView.findViewById(R.id.textView)
            private val audioDuration: TextView = itemView.findViewById(R.id.audioDuration)
            private val audioPlayButton : ImageView = itemView.findViewById(R.id.playAudio)
            @SuppressLint("ResourceAsColor", "SetTextI18n")
            fun bind(imageItem: String) {
//            imageView.setImageResource()
                if(data != null) {
                    Log.d(TAG, "bind: audio data duration ${data!!.duration}")
                    if(data!!.duration?.isNotEmpty() == true) {
                        val duration = data!!.duration?.let { commaSeparatedStringToList(it.toString()) }
                        audioDuration.text = duration?.get(absoluteAdapterPosition)
                    } else {
                        audioDuration.text = "00:00"
                    }
                }
                imageView2.setOnClickListener {
                    Log.d(TAG, "bind: clicked audio")
                }

                audioPlayButton.setOnClickListener {
                    Log.d(TAG, "bind: clicked play button")
                    Toast.makeText(itemView.context, "clicked play button", Toast.LENGTH_SHORT).show()
                    EventBus.getDefault().post(FeedAudioViewFragment())

                    if (isPlaying) {
                        mediaPlayer?.pause()
                        isPlaying = false
                        pausePlayButton?.setImageResource(R.drawable.play_svgrepo_com)
                    } else {
                        mediaPlayer?.start()
                        isPlaying = true
                        pausePlayButton?.setImageResource(R.drawable.baseline_pause_white_24)
                    }
                }

                cardView.setOnClickListener {
                    if (onMultipleImagesClickListener != null) {

                        onMultipleImagesClickListener?.multipleImagesClickListener()
                    }
                }

                if (imageList?.size!! <= 2) {
                    Log.d("TAG", "bind: more")
                } else if (imageList.size == 3) {

                    val layoutParams = cardView.layoutParams
                    val newWidthInPixels = 320  // for example, 300 pixels
                    val newHeightInPixels = 180 // for example, 200 pixels

                    when (absoluteAdapterPosition) {
                        0 -> {
                            layoutParams.width = newWidthInPixels
                            layoutParams.height = newHeightInPixels
                            cardView.layoutParams = layoutParams
                        }

                        1 -> {
                            layoutParams.width = newWidthInPixels
                            layoutParams.height = newHeightInPixels
                            cardView.layoutParams = layoutParams
                        }

                        2 -> {
                            layoutParams.width = 640
                            layoutParams.height = newHeightInPixels
                            cardView.layoutParams = layoutParams
                        }
                        // Add more cases as needed
                        else -> {
                            layoutParams.width = newWidthInPixels
                            layoutParams.height = newHeightInPixels
                            cardView.layoutParams = layoutParams
                        }
                    }

                } else if (imageList.size > 4) {
                    Log.d("TAG", "bind: size list > 4")
                    val layoutParams = cardView.layoutParams
                    val newWidthInPixels = 320  // for example, 300 pixels
                    val newHeightInPixels = 185 // for example, 200 pixels

                    when (absoluteAdapterPosition) {
                        0 -> {
                            layoutParams.width = newWidthInPixels
                            layoutParams.height = newHeightInPixels
                            cardView.layoutParams = layoutParams
                        }

                        1 -> {
                            layoutParams.width = newWidthInPixels
                            layoutParams.height = newHeightInPixels
                            cardView.layoutParams = layoutParams
                        }

                        2 -> {
                            layoutParams.width = newWidthInPixels
                            layoutParams.height = newHeightInPixels
                            cardView.layoutParams = layoutParams
                        }

                        3 -> {
                            Log.d("TAG", "bind: load 4th")

                            imageView2.visibility = View.VISIBLE
                            countTextView.visibility = View.VISIBLE

                            countTextView.text = "+ ${imageList.size - 4}"
                            layoutParams.width = newWidthInPixels
                            layoutParams.height = newHeightInPixels
                            cardView.layoutParams = layoutParams
                        }
                        // Add more cases as needed
                        else -> {
                            layoutParams.width = newWidthInPixels
                            layoutParams.height = newHeightInPixels
                            cardView.layoutParams = layoutParams
                        }
                    }
                } else {
                    // Specify new width and height programmatically
                    val newWidthInPixels = 320  // for example, 300 pixels
                    val newHeightInPixels = 180 // for example, 200 pixels

                    // Get the existing layout parameters of the ImageView
                    val params = cardView.layoutParams


                    // Change the width and height to the specified sizes
                    params.width = newWidthInPixels
                    params.height = newHeightInPixels


                    // Apply the changes to the ImageView
                    cardView.layoutParams = params
                }




                // You can also add click listeners or other bindings here if needed
            }
        }

    }