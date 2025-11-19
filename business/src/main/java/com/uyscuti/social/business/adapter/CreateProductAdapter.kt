package com.uyscuti.social.business.adapter

import android.app.Activity
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator
import com.uyscuti.social.business.model.Catalogue
import com.uyscuti.social.business.R


class CreateProductAdapter(private var context: Activity) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var nameError = false
    private var priceError = false
    private var descriptionError = false

    private var nameEditText: EditText? = null
    private var priceEditText: EditText? = null
    private var descriptionEditText: EditText? = null
    private var imageUri: Uri? = null

    private var name: String? = null
    private var price: String? = null
    private var description: String? = null


    companion object {
        private const val VIEW_TYPE_PRODUCT = 1
        private const val VIEW_TYPE_PRODUCT_IMAGE = 2
        private const val VIEW_SAVE_BUTTON_PRODUCT = 3
    }


    private val images = arrayListOf<String>()

    private var pagerPosition = 0
    private var showIndicator = false

    private var videoUri: Uri? = null

    init {
        images.add("")
    }

    var onInvokeCatalogue: ((Catalogue) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_PRODUCT -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.product_item_catalogue, parent, false)
                ProductViewHolder(view)
            }

            VIEW_TYPE_PRODUCT_IMAGE -> {
                val view =
                    LayoutInflater.from(context).inflate(R.layout.product_image_item, parent, false)
                ProductImageViewHolder(view)
            }

            VIEW_SAVE_BUTTON_PRODUCT -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.save_button_item_catalogue, parent, false)
                SaveButtonViewHolder(view)
            }

            else -> {
                throw IllegalArgumentException("Invalid view type")
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ProductViewHolder) {

            nameEditText = holder.itemName
            priceEditText = holder.itemPrice
            descriptionEditText = holder.description

            if (nameError) {
                holder.itemName.error = "Required"
            }
            holder.itemName.setText(name)

            nameEditText!!.setText(name)
            priceEditText!!.setText(price)
            descriptionEditText!!.setText(description)

            if (priceError) {
                holder.itemPrice.error = "Required"
            }

            if (descriptionError) {
                holder.description.error = "Required"

            }

        } else if (holder is ProductImageViewHolder) {
            val viewPager: ViewPager2 = holder.viewPager
            val dotsIndicator: WormDotsIndicator = holder.dotsIndicator

            holder.dotsIndicator.visibility = if (showIndicator) View.VISIBLE else View.GONE

            viewPager.adapter = AddMediaPagerAdapter(images, context)
            viewPager.setCurrentItem(pagerPosition, false)
            dotsIndicator.attachTo(viewPager)

//            if (videoUri != null){
//                val duration = getVideoDuration(videoUri!!)
//                if (duration > maxVideoDuration) {
//                    Toast.makeText(context, "Video duration is too long", Toast.LENGTH_SHORT).show()
//                    return
//                }
//            }

        } else if (holder is SaveButtonViewHolder) {
            val saveButton: Button = holder.saveButton
            saveButton.setOnClickListener {
                validateFields()
                onInvokeCatalogue?.invoke(
                    Catalogue(
                        System.currentTimeMillis().toString(),
                        nameEditText!!.text.toString(),
                        descriptionEditText!!.text.toString(),
                        priceEditText!!.text.toString(),
                        images
                    )
                )
                context.finish()
                Toast.makeText(context, "You have created a catalogue", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun addImage(image: String) {
        Log.d("CreateProduct", "addImage: $image")
        images.removeLast()
        images.add(image)
        images.add("")
        showIndicator = true
        pagerPosition = images.indexOf(image)
        notifyItemChanged(0)
    }


    fun addVideo(video: String) {

        Log.d("CreateProduct", "addVideo: $video")

//        images.removeLast()
        images.add(0, video)
        showIndicator = true
        pagerPosition = images.indexOf(video)
        notifyItemChanged(0)
    }


    private fun validateFields() {
        nameError = nameEditText?.text.toString().isEmpty()
        priceError = priceEditText?.text.toString().isEmpty()
        descriptionError = descriptionEditText?.text.toString().isEmpty()

        name = nameEditText?.text.toString()
        price = priceEditText?.text.toString()
        description = descriptionEditText?.text.toString()


        if (nameError || priceError || descriptionError || images.isEmpty() || images[0].isEmpty() || videoUri == null) {
            notifyItemChanged(1)
            return
        }


        var videoThumbnail: String? = null
        val imageUrls = arrayListOf<String>()
        images.forEach {
            if (it.isNotEmpty()) {
                imageUrls.add(it)

            }

        }
//        if (videoUri != null) {
//
//            val getVideoDuration = getVideoDuration(videoUri!!)
//            val maxVideoDuration = 30 * 1000
//            if (getVideoDuration > maxVideoDuration) {
//                videoThumbnail = videoUri.toString()
//                Toast.makeText(
//                    context,
//                    "Video duration must be less than 30 seconds",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        }
        if (videoUri != null) {
            return
        }
        if (imageUrls.isEmpty()) {
            Toast.makeText(context, "Please add media", Toast.LENGTH_SHORT).show()
            return

        }

        val id = System.currentTimeMillis().toString()
        onInvokeCatalogue?.invoke(Catalogue(id, name!!, description!!, price!!, imageUrls))

    }


    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> VIEW_TYPE_PRODUCT_IMAGE
            1 -> VIEW_TYPE_PRODUCT
            else -> VIEW_SAVE_BUTTON_PRODUCT
        }

    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Initialize views for DetailsHolder if needed

        val itemName: EditText = itemView.findViewById(R.id.name)
        val itemPrice: EditText = itemView.findViewById(R.id.price_text)
        val description: EditText = itemView.findViewById(R.id.description)

    }

    inner class ProductImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Initialize views for ImagesHolder if needed
        val viewPager: ViewPager2 = itemView.findViewById(R.id.viewPager)
        val dotsIndicator: WormDotsIndicator = itemView.findViewById(R.id.worm_dots_indicator)

    }


    inner class SaveButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Initialize views for EditButtonHolder if needed
        val saveButton: Button = itemView.findViewById(R.id.shopNowButton)

    }


    override fun getItemCount(): Int {
        return 3

    }

    private fun getVideoDuration(videoUri: Uri): Long {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, videoUri)
        val durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        return durationString?.toLongOrNull() ?: 0
    }

}


