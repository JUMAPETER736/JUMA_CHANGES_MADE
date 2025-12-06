package com.uyscuti.social.circuit.adapter.feed

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.uyscuti.social.circuit.R
import java.io.File

private const val TAG = ""

class MultipleFeedImagesAdapter(

    private var context: Context,
    private var images: List<String>,
    private var multipleImagesListener: Fragment,
    private var type: String = "image"
) :
    RecyclerView.Adapter<MultipleFeedImagesAdapter.Pager2ViewHolder>() {
    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val images: ImageView = itemView.findViewById(R.id.img)

        init {
            images.setOnClickListener {
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
                .inflate(R.layout.multiple_feed_images_item, parent, false)
        )
    }

    override fun onBindViewHolder(
        holder: Pager2ViewHolder,
        position: Int
    ) {

        if (type == "image") {
            Glide.with(context)
                .load(images[position])
                .into(holder.images)
        } else {
            Glide.with(context)
                .load(Uri.fromFile(File(images[position])))
                .into(holder.images)
        }
        holder.images.setOnClickListener {
            multipleImagesListener.onImageClick()
        }
    }
    override fun getItemCount(): Int {
        return images.size
    }
}

private fun Fragment.onImageClick() {
    TODO("Not yet implemented")
}

interface MultipleImagesListener {
    fun onImageClick()
}