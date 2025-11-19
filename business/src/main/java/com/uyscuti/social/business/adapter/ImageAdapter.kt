package com.uyscuti.social.business.adapter

import android.app.Activity
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator
import com.uyscuti.social.business.R

class ImageAdapter(
    private val context: Activity,
    private val imageUris: ArrayList<Uri>,
    private val onLastImageClicked: (Any?, Any?) -> Unit
) :
    RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_viewer, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUri = imageUris[position]


        if (imageUri != Uri.EMPTY) {
//            Glide.with(context)
//                .load(imageUri)
//                .centerCrop()
//                .into(holder.imageView)
        }

        if (position == imageUris.size - 1) {
//            holder.imageView.setOnClickListener {
//                onLastImageClicked.invoke()
//
//            }

        } else {
//            holder.imageView.setOnClickListener{
////                val intent = Intent(context, FullScreenImageActivity::class.java).apply {
////                    putExtra("imageUrl", imageUri.toString())
////                }
////                context.startActivity(intent)
//
//                val imagePaths = ArrayList<String>()
//                for (i in imageUris) {
//                    imagePaths.add(i.toString())
//                }
//
//                val intent = Intent(context, FullScreenImageActivity::class.java)
//                intent.putStringArrayListExtra("imageUrls",imagePaths)
//                context.startActivity(intent)
//
//
//            }
        }
    }

    override fun getItemCount(): Int {
        return imageUris.size
    }

    fun setImageUris(imageUris: MutableList<Uri>) {
//        this.imageUris.clear()
        this.imageUris.removeLast()
        this.imageUris.addAll(imageUris)
        this.imageUris.add(Uri.EMPTY)
        notifyDataSetChanged()
    }

    fun getImageUris(): List<Uri> {

        return imageUris
    }


    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //        val imageView: ImageView = itemView.findViewById(R.id.productImageView)
        val viewPager: ViewPager2 = itemView.findViewById(R.id.viewPager)
        val dotsIndicator: WormDotsIndicator = itemView.findViewById(R.id.worm_dots_indicator)


    }
}

