package com.uyscuti.social.circuit.adapter.gif

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.uyscuti.social.circuit.R
import com.uyscuti.social.network.api.response.gif.allgifs.GifModel

class GifAdapter(private val clickListener: GifClickListener) :
    GifPaginatedAdapter<GifModel, GifAdapter.ViewHolder>() {

    interface GifClickListener {
        fun onUserGifClick(gifEntity: GifModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.gif_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.render(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var imageView: ImageView

        init {
            imageView = itemView.findViewById(R.id.gifImageView)
        }

        val count = 0
        fun render(data: GifModel) {

            Glide.with(imageView.context)
                .load(data.gifs[0].url)
                .apply(RequestOptions.centerCropTransform())
                .placeholder(R.drawable.flash21)

                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(imageView)

            Log.d("Thumbnail", "render: ${data.gifs[0].url} ${count + 1}")
            imageView.setOnClickListener {
                clickListener.onUserGifClick(data)
            }

        }
    }
}

