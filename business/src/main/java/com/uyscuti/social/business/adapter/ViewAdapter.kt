package com.uyscuti.social.business.adapter


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uyscuti.social.business.FullScreenImageActivity
import com.uyscuti.social.business.R


class ViewAdapter(private val context: Activity, private val imageUris: ArrayList<Uri>) :
    RecyclerView.Adapter<ViewAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.catalogue_image_view, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUri = imageUris[position]

        Glide.with(context)
            .load(imageUri.toString())
            .placeholder(R.drawable.baseline_camera_alt_24)
            .centerCrop()
            .into(holder.imageView)

        Log.d("ViewAdapter", "position: $position imageUri: $imageUri")

//        holder.imageView.setImageURI(imageUri)
//        holder.imageView.visibility = View.VISIBLE

        holder.imageView.setOnClickListener{
//            val intent = Intent(context, FullScreenImageActivity::class.java).apply {
//                putExtra("imageUrl", imageUri.toString())
//            }
//            context.startActivity(intent)

            val imagePaths = ArrayList<String>()
            for (i in imageUris) {
                imagePaths.add(i.toString())
            }

            val intent = Intent(context, FullScreenImageActivity::class.java)
            intent.putStringArrayListExtra("imageUrls",imagePaths)
            context.startActivity(intent)
        }
    }


    fun setImageUris(imageUris: MutableList<Uri>) {
        this.imageUris.addAll(imageUris)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return imageUris.size
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.view_catalogue_image)

    }


}
