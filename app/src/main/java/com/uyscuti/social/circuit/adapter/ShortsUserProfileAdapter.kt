package com.uyscuti.social.circuit.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
//import com.softrunapps.paginatedrecyclerview.PaginatedAdapter
import com.uyscuti.social.circuit.R
import com.uyscuti.social.core.common.data.room.entity.UserShortsEntity


class ShortsUserProfileAdapter(
    private val clickListener: ThumbnailClickListener) :
    PaginatedAdapter<UserShortsEntity, ShortsUserProfileAdapter.ViewHolder>() {

    interface ThumbnailClickListener {
        fun onUserProfileShortClick(shortsEntity: UserShortsEntity)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.shorts_user_profile_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.render(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView = itemView.findViewById(R.id.thumbnailImageView)


        val count = 0
        fun render(data: UserShortsEntity) {


            Glide.with(imageView.context)
                .load(data.thumbnail[0].thumbnailUrl)
                .apply(RequestOptions.centerCropTransform())
                .placeholder(R.drawable.flash21)

                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(imageView)

            Log.d("Thumbnail", "render: ${data.thumbnail[0].thumbnailUrl} ${count+1}")
            imageView.setOnClickListener {
                clickListener.onUserProfileShortClick(data)
            }


        }

    }
}

