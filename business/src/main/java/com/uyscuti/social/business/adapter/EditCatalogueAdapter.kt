package com.uyscuti.social.business.adapter

import android.app.Activity
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator
import com.uyscuti.social.business.model.Catalogue
import com.uyscuti.social.business.R

import java.util.concurrent.TimeUnit

// Create a duration for a toast message to appear for 3seconds
val duration = Toast.LENGTH_LONG

class EditCatalogueAdapter(private var context: Activity, private val catalogue: Catalogue) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var nameError = false
    private var priceError = false
    private var descriptionError = false

    private var pagerPosition = 0
    private var showIndicator = false

    private var images = arrayListOf<String>()
    private var videoUrl = ""


    private var nameEditText: EditText? = null
    private var priceEditText: EditText? = null
    private var descriptionEditText: EditText? = null

    private var name: String? = null
    private var price: String? = null
    private var description: String? = null

    var onInvokeCatalogue: ((Catalogue) -> Unit)? = null


    private lateinit var editMediaPagerAdapter: EditMediaPagerAdapter

    private var thumbnail: String? = null



    companion object {
        private const val EDIT_MEDIA_CATALOGUE = 1
        private const val EDIT_CATALOGUE_DETAILS = 2
        private const val UPDATE_CATALOGUE_BUTTON = 3
    }

    init {

        name = catalogue.name
        price = catalogue.price
        description = catalogue.description

        for (image in catalogue.images) {

            if (isVideoUrl(image)) {
                images.add(0, image)
                thumbnail = image
            } else {
                images.add(image)
            }
            showIndicator = true
        }
        images.add("")

    }

    private fun isVideoUrl(url: String): Boolean {
        val videoExtensions = listOf(".mp4", ".mkv", ".webm", ".avi", ".mov")
        return videoExtensions.any { url.endsWith(it, ignoreCase = true) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            EDIT_MEDIA_CATALOGUE -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.edit_catalogue_media, parent, false)
                EditMediaCatalogueViewHolder(view)
            }

            EDIT_CATALOGUE_DETAILS -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.edit_catalogue_details, parent, false)
                EditCatalogueDetailsViewHolder(view)
            }

            UPDATE_CATALOGUE_BUTTON -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.edit_catalogue_button, parent, false)
                UpdateCatalogueButtonViewHolder(view)
            }

            else -> {
                throw IllegalArgumentException("Invalid view type")
            }
        }
    }

    inner class EditMediaCatalogueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val viewPager: ViewPager2 = itemView.findViewById(R.id.viewPager)
        val dotsIndicator: WormDotsIndicator = itemView.findViewById(R.id.worm_dots_indicator_two)
    }

    inner class EditCatalogueDetailsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName: EditText = itemView.findViewById(R.id.name)
        val itemPrice: EditText = itemView.findViewById(R.id.price_text)
        val description: EditText = itemView.findViewById(R.id.description)

    }

    inner class UpdateCatalogueButtonViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val updateButton: Button = itemView.findViewById(R.id.upDateButton)
    }

    inner class EditCatalogueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> EDIT_MEDIA_CATALOGUE
            1 -> EDIT_CATALOGUE_DETAILS
            else -> UPDATE_CATALOGUE_BUTTON
        }
    }

    override fun getItemCount(): Int {


        return 3

    }

    fun releasePlayer() {
        editMediaPagerAdapter.releasePlayer()
    }

    fun pausePlayer() {
        editMediaPagerAdapter.pausePlayer()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is EditMediaCatalogueViewHolder) {
            val viewPager: ViewPager2 = holder.viewPager
            val dotsIndicator: WormDotsIndicator = holder.dotsIndicator
            showIndicator = false
            dotsIndicator.visibility = View.VISIBLE

            editMediaPagerAdapter = EditMediaPagerAdapter(images, context, thumbnail)

            viewPager.adapter = editMediaPagerAdapter
            viewPager.setCurrentItem(pagerPosition, false)
            dotsIndicator.attachTo(viewPager)

        } else if (holder is UpdateCatalogueButtonViewHolder) {
            val updateButton: Button = holder.updateButton
            updateButton.setOnClickListener {

                if (validateFields()) {
                    if (images.isEmpty()){
                        Toast.makeText(context, "Please add image or video", TimeUnit.SECONDS.toMillis(3).toInt()).show()
                        return@setOnClickListener

                    }
                    if (images.isNotEmpty()) {
                        onInvokeCatalogue?.invoke(
                            Catalogue(catalogue.id, name!!, description!!, price!!, images!!)
                        )
                        Toast.makeText(context, "Catalogue has been updated", TimeUnit.SECONDS.toMillis(3).toInt()
                        ).show()
                        context.finish()

                    } else {
                        Toast.makeText(
                            context,
                            "Please add image or video",
                            TimeUnit.SECONDS.toMillis(3).toInt()).show()

                    }

                }
            }
        } else if (holder is EditCatalogueDetailsViewHolder) {
            nameEditText = holder.itemName
            priceEditText = holder.itemPrice
            descriptionEditText = holder.description

            if (nameError) {
                holder.itemName.error = "Required"
            }
            if (priceError) {
                holder.itemPrice.error = "Required"
            }

            if (descriptionError) {
                holder.description.error = "Required"
            }

            nameEditText?.setOnClickListener {
                if (nameEditText?.text.toString().isEmpty()) {
                    holder.itemName.error = "Required"
                }
            }
            priceEditText?.setOnClickListener {
                if (priceEditText?.text.toString().isEmpty()) {
                    holder.itemPrice.error = "Required"
                }
            }
            descriptionEditText?.setOnClickListener {
                if (descriptionEditText?.text.toString().isEmpty()) {
                    holder.description.error = "Required"
                }
            }
            nameEditText?.setText(name)
            priceEditText?.setText(price)
            descriptionEditText?.setText(description)
        }
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun addImage(image: String) {

        images.removeLast()
        images.add(image)
        images.add("")
        showIndicator = true
        pagerPosition = images.indexOf(image)

        notifyItemRemoved(0)
    }

    fun addVideo(video: String) {
        if (images.isNotEmpty()) {
            if (isVideoUrl(images[0])) {
                images.removeFirst()
            }
        }
        images.add(0, video)
        showIndicator = true
        pagerPosition = images.indexOf(video)
        notifyItemRemoved(0)

    }

    fun replaceVideo(video: String) {
        try {
            val position = editMediaPagerAdapter.getSelectedPosition()

            if (position == -1) {
                addVideo(video)

            } else {
                images[position] = video
                showIndicator = true
                pagerPosition = position
                notifyItemChanged(position)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun replaceImage(image: String) {
        try {
            val position = editMediaPagerAdapter.getSelectedPosition()

            Log.d("ReplaceImage", "replace image at position: $position image:$image")

            if (position == -1) {
                addImage(image)
            } else {
                images[position] = image
                showIndicator = true
                pagerPosition = position
                notifyItemChanged(0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun validateFields(): Boolean {
        nameError = nameEditText?.text.toString().isEmpty()
        priceError = priceEditText?.text.toString().isEmpty()
        descriptionError = descriptionEditText?.text.toString().isEmpty()


        name = nameEditText?.text.toString()
        price = priceEditText?.text.toString()
        description = descriptionEditText?.text.toString()


        if (nameError || priceError || descriptionError || images.isEmpty() || videoUrl.isEmpty()) {
            notifyItemChanged(1)

        }

        images.removeLast()

//        val imageUrl = arrayListOf<String>()
//        images.forEach {
//            if (it.isNotEmpty()) {
//                imageUrl.add(it)
//            }
//        }
////
////
////        val id = System.currentTimeMillis().toString()
////        onInvokeCatalogue?.invoke(Catalogue(id, name!!, description!!, price!!, imageUrl))

        return true
    }

}



