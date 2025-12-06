package com.uyscuti.social.circuit.adapter.feed

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.uyscuti.social.circuit.R

class FeedMultipleImagesRecyclerViewAdapter(

    private val imageList: List<String>?) : RecyclerView.Adapter<FeedMultipleImagesRecyclerViewAdapter.ViewHolder>() {
    private var onMultipleImagesClickListener: OnMultipleImagesClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.feed_multiple_images_item, parent, false)
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
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val imageView2: ImageView = itemView.findViewById(R.id.imageView2)
        private val cardView: CardView = itemView.findViewById(R.id.cardView)
        private val countTextView: TextView = itemView.findViewById(R.id.textView)

        @SuppressLint("ResourceAsColor", "SetTextI18n")
        fun bind(imageItem: String) {

            cardView.setOnClickListener {
                if (onMultipleImagesClickListener != null) {

                    onMultipleImagesClickListener?.multipleImagesClickListener()
                }
            }
            if(imageList?.size!! <= 2) {
                Log.d("TAG", "bind: more")
            }
            else if(imageList.size == 3) {

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

            }
            else if(imageList.size > 4) {
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
            }
            else {
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


            Glide.with(imageView.context)
                .load(imageItem)
                .placeholder(R.drawable.profilepic2)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView)
            // You can also add click listeners or other bindings here if needed
        }
    }

}
interface OnMultipleImagesClickListener {
    fun multipleImagesClickListener()
    abstract fun SetOnPaginationListener(onPaginationListener: FeedPaginatedAdapter.OnPaginationListener)
    fun updateItem(i: kotlin.Int, post: com.uyscuti.social.network.api.response.posts.Post)
}