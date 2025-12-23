package com.uyscuti.social.business.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.uyscuti.social.business.EditCatalogueActivity
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator
import com.uyscuti.social.business.model.Catalogue
import com.uyscuti.social.business.R


class ViewProductAdapter(private val context: Activity, private var catalogue: Catalogue) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var fullScreenPagerAdapter: MediaPagerAdapter
    private lateinit var player: ExoPlayer


    companion object {
        private const val VIEW_TYPE_IMAGES = 1
        private const val VIEW_TYPE_DETAILS = 2
        private const val VIEW_TYPE_EDIT_BUTTON = 3
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_IMAGES -> {
                val view =
                    LayoutInflater.from(context).inflate(R.layout.item_product_media, parent, false)
                ImagesHolder(view)
            }

            VIEW_TYPE_DETAILS -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.item_product_details, parent, false)
                DetailsHolder(view)
            }

            VIEW_TYPE_EDIT_BUTTON -> {
                val view =
                    LayoutInflater.from(context).inflate(R.layout.item_edit_button, parent, false)
                EditButtonHolder(view)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    fun updateCatalogue(updatedCatalogue: Catalogue) {
        this.catalogue = updatedCatalogue
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // Bind data based on the view holder type if needed
        if (holder is ImagesHolder) {
            // Bind data for ImagesHolder if needed
            val imagesHolder = holder as ImagesHolder
            var thumbnail: String? = null
            val imageList = arrayListOf<String>()


            for (image in catalogue.images) {
                imageList.add(image)
                if (isVideoUrl(image)) {
                    thumbnail = image
                }
            }


            fullScreenPagerAdapter = MediaPagerAdapter(imageList, context, thumbnail)

            imagesHolder.viewPager.adapter = fullScreenPagerAdapter
            imagesHolder.viewPager.offscreenPageLimit = 10

            imagesHolder.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {

                    if (position == 0) {
                        fullScreenPagerAdapter.resumePlayer()
                    } else {
                        fullScreenPagerAdapter.pausePlayer()

                    }

                }
            })

            if (catalogue.images.size < 2){
                imagesHolder.dotsIndicator.visibility = View.GONE
            }


            imagesHolder.dotsIndicator.attachTo(imagesHolder.viewPager)

        } else if (holder is DetailsHolder) {
            // Bind data for DetailsHolder if needed
            val detailsHolder = holder as DetailsHolder
            detailsHolder.productTitle.text = catalogue.name
            detailsHolder.productDescription.text = catalogue.description
            detailsHolder.productPrice.text = "MK " + catalogue.price.toString()


        } else if (holder is EditButtonHolder) {
            // Bind data for EditButtonHolder if needed
            val editButtonHolder = holder as EditButtonHolder

            editButtonHolder.editButton.setOnClickListener {
                val intent = Intent(context, EditCatalogueActivity::class.java)
                intent.putExtra("catalogue", catalogue)
                context.startActivityForResult(intent, 123)

            }
        }
    }

// video player functions
    fun releasePlayer() {
        fullScreenPagerAdapter.releasePlayer()
    }

    fun pausePlayer() {
        fullScreenPagerAdapter.pausePlayer()
    }

    private fun isVideoUrl(url: String): Boolean {
        val videoExtensions = listOf(".mp4", ".mkv", ".webm", ".avi", ".mov")
        return videoExtensions.any { url.endsWith(it, ignoreCase = true) }
    }


    override fun getItemCount(): Int {
        // Return the total number of items in the RecyclerView
        return 3 // Assuming there are 3 types of view holders
    }

    override fun getItemViewType(position: Int): Int {
        // Return the view type based on the position
        return when (position) {
            0 -> VIEW_TYPE_IMAGES
            1 -> VIEW_TYPE_DETAILS
            2 -> VIEW_TYPE_EDIT_BUTTON
            else -> throw IllegalArgumentException("Invalid position")
        }
    }

    inner class ImagesHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Initialize views for ImagesHolder if needed
        val viewPager: ViewPager2 = itemView.findViewById(R.id.viewPager)
        val dotsIndicator: WormDotsIndicator = itemView.findViewById(R.id.worm_dots_indicator)
    }

    inner class DetailsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Initialize views for DetailsHolder if needed
        val productTitle: TextView = itemView.findViewById(R.id.view_name_product)
        val productDescription: TextView = itemView.findViewById(R.id.view_description_product)
        val productPrice: TextView = itemView.findViewById(R.id.view_price_product)


    }

    inner class EditButtonHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Initialize views for EditButtonHolder if needed
        val editButton: Button = itemView.findViewById(R.id.edit_button)


    }


}
