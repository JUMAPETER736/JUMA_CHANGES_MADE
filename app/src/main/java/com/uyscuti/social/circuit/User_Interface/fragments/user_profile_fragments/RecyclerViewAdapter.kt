package com.uyscuti.social.circuit.User_Interface.fragments.user_profile_fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.uyscuti.social.circuit.R
import com.uyscuti.social.core.common.data.room.entity.UserShortsEntity

class RecyclerViewAdapter: PagingDataAdapter<UserShortsEntity, RecyclerViewAdapter.MyViewHolder>(
    DiffUtilCallBack()
) {

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.bind(getItem(position)!!)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context).inflate(R.layout.shorts_user_profile_item, parent, false)

        return MyViewHolder(inflater)
    }

    class MyViewHolder(view: View): RecyclerView.ViewHolder(view) {

        val imageView: ImageView = view.findViewById(R.id.thumbnailImageView)
//        val tvName: TextView = view.findViewById(R.id.tvName)
//        val tvDesc: TextView = view.findViewById(R.id.tvDesc)

        fun bind(data: UserShortsEntity) {
//            tvName.text = data.name
//            tvDesc.text = data.species

//            Glide.with(imageView)
//                .load(data.image)
//                .circleCrop()
//                .into(imageView)
            Glide.with(imageView.context)
                .load(data.thumbnail[0].thumbnailUrl)
                .apply(RequestOptions.centerCropTransform())
                .placeholder(R.drawable.flash21)
//                    .diskCacheStrategy(
//                        DiskCacheStrategy.ALL
//                    )
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(imageView)
        }
    }

    class DiffUtilCallBack: DiffUtil.ItemCallback<UserShortsEntity>() {
        override fun areItemsTheSame(oldItem: UserShortsEntity, newItem: UserShortsEntity): Boolean {
            return oldItem._id == newItem._id
        }

        override fun areContentsTheSame(oldItem: UserShortsEntity, newItem: UserShortsEntity): Boolean {
            return oldItem._id == newItem._id
                    && oldItem.images[0]._id == newItem.images[0]._id
        }

    }

}