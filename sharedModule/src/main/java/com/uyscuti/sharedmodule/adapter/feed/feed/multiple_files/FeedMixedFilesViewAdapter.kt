package com.uyscuti.sharedmodule.adapter.feed.feed.multiple_files

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.card.MaterialCardView
import com.uyscuti.sharedmodule.utils.waveformseekbar.WaveformSeekBar
import com.uyscuti.sharedmodule.R
import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.uyscuti.social.network.api.response.getrepostsPostsoriginal.File
import androidx.core.graphics.toColorInt
import com.uyscuti.sharedmodule.ui.fragments.feed.feedviewfragments.feedRepost.PostItem
import com.uyscuti.sharedmodule.ui.fragments.feed.feedviewfragments.feedRepost.Tapped_Files_In_The_Container_View_Fragment


private const val VIEW_TYPE_IMAGE_FEED = 0
private const val VIEW_TYPE_AUDIO_FEED = 1
private const val VIEW_TYPE_VIDEO_FEED = 2
private const val VIEW_TYPE_DOCUMENT_FEED = 3
private const val VIEW_TYPE_DEFAULT = 4
private const val VIEW_TYPE_COMBINATION_OF_MULTIPLE_FILES = 5


private const val TAG = "FeedMixedFilesViewAdapter"


class FeedMixedFilesViewAdapter(

    private val feedPost:com.uyscuti.social.network.api.response.posts.Post) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>()  {
    private var onMultipleFilesClickListener: OnMultipleFilesClickListener? = null

    fun setOnMultipleFilesClickListener(l: OnMultipleFilesClickListener) {
        onMultipleFilesClickListener = l
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {

            VIEW_TYPE_IMAGE_FEED -> {
                val itemView = inflater.inflate(
                    R.layout.feed_multiple_images_view_item, parent, false)
                FeedImagesOnly(itemView)
            }

            VIEW_TYPE_AUDIO_FEED -> {
                val itemView = inflater.inflate(
                    R.layout.feed_multiple_audios_view_item, parent, false)
                FeedAudiosOnly(itemView)
            }

            VIEW_TYPE_VIDEO_FEED -> {
                val itemView = inflater.inflate(
                    R.layout.feed_multiple_videos_view_item, parent, false)
                FeedVideosOnly(itemView)
            }

            VIEW_TYPE_DOCUMENT_FEED -> {
                val itemView = inflater.inflate(
                    R.layout.feed_multiple_documents_view_item, parent, false)
                FeedDocumentsOnly(itemView)
            }

            VIEW_TYPE_COMBINATION_OF_MULTIPLE_FILES -> {
                val itemView = inflater.inflate(
                    R.layout.feed_multiple_combination_of_files_view_item, parent, false)
                FeedCombinationOfMultipleFiles(itemView)
            }



            else -> throw IllegalArgumentException("Invalid view type")
        }
    }



    inner class FeedImagesOnly(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val materialCardView: MaterialCardView =
            itemView.findViewById(R.id.materialCardView)
        private val countTextView: TextView = itemView.findViewById(R.id.textView)
        private val imageView2: ImageView = itemView.findViewById(R.id.imageView2)

        fun Int.dpToPx(context: Context): Int {
            return (this * context.resources.displayMetrics.density).toInt()
        }

        // Helper function to calculate adaptive heights based on screen size
        private fun getAdaptiveHeights(context: Context): Pair<Int, Int> {
            val displayMetrics = context.resources.displayMetrics
            val screenHeight = displayMetrics.heightPixels

            // Calculate min and max heights as percentages of screen height
            val minHeight = (screenHeight * 0.15).toInt() // 15% of screen height
            val maxHeight = (screenHeight * 0.4).toInt()  // 40% of screen height

            return Pair(minHeight, maxHeight)
        }

        // Helper function to get a constrained height within min/max bounds
        private fun getConstrainedHeight(context: Context, preferredHeight: Int): Int {
            val (minHeight, maxHeight) = getAdaptiveHeights(context)
            return preferredHeight.coerceIn(minHeight, maxHeight)
        }

        // Helper function to get AppCompatActivity from context
        private fun getActivityFromContext(context: Context): AppCompatActivity? {
            return when (context) {
                is AppCompatActivity -> context
                is ContextWrapper -> getActivityFromContext(context.baseContext)
                else -> null
            }
        }

        private fun navigateToTappedFilesFragment(
            context: Context,
            currentIndex: Int,
            files: List<com.uyscuti.social.network.api.response.posts.File>,
            fileIds: List<String>
        ) {
            val activity = getActivityFromContext(context)
            if (activity != null) {
                // Hide AppBar (Toolbar) if available
                activity.findViewById<View>(R.id.topBar)?.visibility = View.GONE

                // Hide Bottom Navigation if available
                activity.findViewById<View>(R.id.bottomNavigationView)?.visibility = View.GONE

                val fragment = Tapped_Files_In_The_Container_View_Fragment()

                val bundle = Bundle().apply {
                    putInt("current_index", currentIndex)
                    putInt("total_files", files.size)

                    val fileUrls = ArrayList<String>()
                    files.forEach { file -> fileUrls.add(file.url) }
                    putStringArrayList("file_urls", fileUrls)
                    putStringArrayList("file_ids", ArrayList(fileIds))

                    val postItems = ArrayList<PostItem>()
                    files.forEachIndexed { index, file ->
                        val postItem = PostItem(
                            audioUrl = file.url,
                            audioThumbnailUrl = null,
                            videoUrl = file.url,
                            videoThumbnailUrl = null,
                            postId = fileIds.getOrNull(index) ?: "file_$index",
                            data = "Post data for file $index",
                            files = arrayListOf(file.url)
                        )
                        postItems.add(postItem)
                    }
                    putParcelableArrayList("post_list", postItems)
                    putString("post_id", fileIds.getOrNull(currentIndex) ?: "file_$currentIndex")
                }

                fragment.arguments = bundle

                activity.supportFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                    .replace(R.id.frame_layout, fragment)
                    .addToBackStack("tapped_files_view")
                    .commit()

                Log.d(TAG, "Navigated to Tapped_Files_In_The_Container_View with ${files.size} files, starting at index $currentIndex")
            } else {
                Log.e(TAG, "Activity is null, cannot navigate to fragment")
            }
        }

        @SuppressLint("SetTextI18n")
        fun onBind(data: com.uyscuti.social.network.api.response.posts.Post) {
            Log.d(TAG, "image feed $absoluteAdapterPosition item count $itemCount")

            val context = itemView.context
            val displayMetrics = context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val margin = 4.dpToPx(context)
            val spaceBetweenRows = 4.dpToPx(context)

            // Get adaptive heights
            val (minHeight, maxHeight) = getAdaptiveHeights(context)

            val fileIdToFind = data.fileIds[absoluteAdapterPosition]
            val file = data.files.find { it.fileId == fileIdToFind }
            val imageUrl = file?.url ?: data.files.getOrNull(absoluteAdapterPosition)?.url ?: ""

            val fileSize = itemCount
            Log.d(TAG, "image getItemCount: $fileSize $imageUrl")

            // Updated click listener to navigate to fragment
            itemView.setOnClickListener {
                // Navigate to the fragment instead of calling the old listener
                navigateToTappedFilesFragment(
                    context,
                    absoluteAdapterPosition,
                    data.files,
                    data.fileIds as List<String>
                )

                // Optional: Still call the original listener if you need it for other purposes
                onMultipleFilesClickListener?.multipleFileClickListener(
                    absoluteAdapterPosition,
                    data.files,
                    data.fileIds as List<String>
                )
            }

            // Also add click listener to the image itself for better UX
            imageView.setOnClickListener {
                navigateToTappedFilesFragment(
                    context,
                    absoluteAdapterPosition,
                    data.files,
                    data.fileIds as List<String>
                )
            }

            // Add click listener to the card view as well
            materialCardView.setOnClickListener {
                navigateToTappedFilesFragment(
                    context,
                    absoluteAdapterPosition,
                    data.files,
                    data.fileIds as List<String>
                )
            }

            val layoutParams = materialCardView.layoutParams as ViewGroup.MarginLayoutParams

            when {

                fileSize <= 1 -> {

                    Glide.with(context)
                        .asBitmap()
                        .load(imageUrl)
                        .placeholder(R.drawable.flash21)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(object : CustomTarget<Bitmap>() {

                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                val imageWidth = resource.width
                                val imageHeight = resource.height

                                val aspectRatio = imageHeight.toFloat() / imageWidth.toFloat()
                                val screenWidth = Resources.getSystem().displayMetrics.widthPixels

                                val calculatedHeight = (screenWidth * aspectRatio).toInt()

                                // Avoid zooming: never let it be larger than the original image height
                                val limitedHeight = minOf(calculatedHeight, imageHeight)

                                // Constrain height between min and max bounds
                                val finalHeight = getConstrainedHeight(context, limitedHeight)

                                val layoutParams = imageView.layoutParams as ViewGroup.MarginLayoutParams
                                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                                layoutParams.height = finalHeight
                                layoutParams.setMargins(0, 0, 0, 0)

                                imageView.layoutParams = layoutParams
                                imageView.setImageBitmap(resource)
                                imageView.adjustViewBounds = true
                                imageView.scaleType = ImageView.ScaleType.FIT_CENTER
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                                // Handle placeholder cleanup if needed
                            }
                        })
                }

                fileSize == 2 -> {
                    layoutParams.width = screenWidth / 2
                    // Use adaptive height instead of fixed 300dp
                    layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.75).toInt())
                    layoutParams.topMargin = 0
                    layoutParams.bottomMargin = 0

                    val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
                    layoutParams.leftMargin = if (isLeftColumn) 0 else spaceBetweenRows
                    layoutParams.rightMargin = if (isLeftColumn) spaceBetweenRows else 0

                    materialCardView.radius = 8.dpToPx(context).toFloat()
                }

                fileSize == 3 -> {
                    when (absoluteAdapterPosition) {
                        0 -> {
                            layoutParams.width = screenWidth
                            // Use adaptive height for the main large image
                            layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.75).toInt())
                            layoutParams.leftMargin = 0
                            layoutParams.rightMargin = 0
                            layoutParams.topMargin = 0
                            layoutParams.bottomMargin = (margin/2)

                            if (layoutParams is StaggeredGridLayoutManager.LayoutParams) {
                                layoutParams.isFullSpan = true
                            }
                        }

                        1, 2 -> {
                            layoutParams.width = screenWidth / 2
                            // Use adaptive height for smaller images
                            layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.75).toInt())

                            if (absoluteAdapterPosition == 1) {
                                layoutParams.leftMargin = 0
                                layoutParams.rightMargin = (spaceBetweenRows/2)
                            } else {
                                layoutParams.leftMargin = (spaceBetweenRows/2)
                                layoutParams.rightMargin = 0
                            }

                            layoutParams.topMargin = (margin/2)
                            layoutParams.bottomMargin = 0
                        }
                    }

                    materialCardView.radius = 8.dpToPx(context).toFloat()
                }

                fileSize == 4 -> {
                    layoutParams.width = screenWidth / 2
                    // Make height adaptive but maintain square-ish aspect ratio
                    val preferredSquareHeight = screenWidth / 2
                    layoutParams.height = getConstrainedHeight(context, preferredSquareHeight)

                    layoutParams.topMargin = if (absoluteAdapterPosition < 2) 0 else spaceBetweenRows
                    layoutParams.bottomMargin = if (absoluteAdapterPosition >= 2) 0 else 0

                    val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
                    layoutParams.leftMargin = if (isLeftColumn) 0 else (spaceBetweenRows/2)
                    layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows/2) else 0

                    materialCardView.radius = 8.dpToPx(context).toFloat()
                }

                fileSize > 4 -> {

                    if (absoluteAdapterPosition >= 4) {
                        itemView.visibility = View.GONE
                        layoutParams.width = 0
                        layoutParams.height = 0
                        itemView.layoutParams = layoutParams
                        return
                    }

                    itemView.visibility = View.VISIBLE

                    layoutParams.width = screenWidth / 2
                    val preferredSquareHeight = screenWidth / 2
                    layoutParams.height = getConstrainedHeight(context, preferredSquareHeight)

                    val isTopRow = absoluteAdapterPosition < 2
                    layoutParams.topMargin = if (isTopRow) 0 else spaceBetweenRows
                    layoutParams.bottomMargin = 0

                    // ✅ FIX: Explicit spacing calculation for 2x2 grid
                    // Since we only show positions 0,1,2,3, use these directly for spacing
                    when (absoluteAdapterPosition) {
                        0 -> { // Top-left
                            layoutParams.leftMargin = 0
                            layoutParams.rightMargin = spaceBetweenRows / 2
                        }
                        1 -> { // Top-right
                            layoutParams.leftMargin = spaceBetweenRows / 2
                            layoutParams.rightMargin = 0
                        }
                        2 -> { // Bottom-left
                            layoutParams.leftMargin = 0
                            layoutParams.rightMargin = spaceBetweenRows / 2
                        }
                        3 -> { // Bottom-right
                            layoutParams.leftMargin = spaceBetweenRows / 2
                            layoutParams.rightMargin = 0
                        }

                    }

                    itemView.layoutParams = layoutParams

                    materialCardView.setContentPadding(0, 0, 0, 0)
                    val cardLayoutParams = materialCardView.layoutParams as ViewGroup.MarginLayoutParams
                    materialCardView.layoutParams = cardLayoutParams
                    materialCardView.radius = 8.dpToPx(context).toFloat()

                    if (absoluteAdapterPosition == 3) {
                        countTextView.visibility = View.VISIBLE
                        countTextView.text = "+${fileSize - 4}"
                        countTextView.textSize = 32f
                        countTextView.setPadding(12, 4, 12, 4)
                        countTextView.background = GradientDrawable().apply {
                            shape = GradientDrawable.RECTANGLE
                            cornerRadius = 16f
                            setColor(Color.parseColor("#80000000"))
                        }
                    } else {
                        countTextView.visibility = View.GONE
                        countTextView.setPadding(0, 0, 0, 0)
                        countTextView.background = null
                    }
                }

            }

            materialCardView.layoutParams = layoutParams

            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.flash21)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop() // Use centerCrop for better image fitting
                .into(imageView)
        }
    }

    inner class FeedAudiosOnly(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val materialCardView: MaterialCardView = itemView.findViewById(R.id.materialCardView)
        private val artworkLayout: LinearLayout = itemView.findViewById(R.id.artworkLayout)
        private val countTextView: TextView = itemView.findViewById(R.id.textView)
        private val imageView2: ImageView = itemView.findViewById(R.id.imageView2)
        private val artworkImageView: ImageView = itemView.findViewById(R.id.artworkImageView)
        private val seekBar: SeekBar = itemView.findViewById(R.id.seekBar)
        private val waveSeekBar: WaveformSeekBar = itemView.findViewById(R.id.waveSeekBar)
        private var currentPostData: com.uyscuti.social.network.api.response.posts.Post? = null
        private val audioDurationTextView: TextView = itemView.findViewById(R.id.audioDuration)

        fun Int.dpToPx(context: Context): Int {
            return (this * context.resources.displayMetrics.density).toInt()
        }

        // Helper function to calculate adaptive heights based on screen size
        private fun getAdaptiveHeights(context: Context): Pair<Int, Int> {
            val displayMetrics = context.resources.displayMetrics
            val screenHeight = displayMetrics.heightPixels

            // Calculate min and max heights as percentages of screen height
            // For audio items, we can use slightly smaller heights than images
            val minHeight = (screenHeight * 0.12).toInt() // 12% of screen height
            val maxHeight = (screenHeight * 0.35).toInt() // 35% of screen height

            return Pair(minHeight, maxHeight)
        }

        // Helper function to get a constrained height within min/max bounds
        private fun getConstrainedHeight(context: Context, preferredHeight: Int): Int {
            val (minHeight, maxHeight) = getAdaptiveHeights(context)
            return preferredHeight.coerceIn(minHeight, maxHeight)
        }

        // Helper function to get AppCompatActivity from context
        private fun getActivityFromContext(context: Context): AppCompatActivity? {
            return when (context) {
                is AppCompatActivity -> context
                is ContextWrapper -> getActivityFromContext(context.baseContext)
                else -> null
            }
        }

        private fun navigateToTappedFilesFragment(
            context: Context,
            currentIndex: Int,
            files: List<com.uyscuti.social.network.api.response.posts.File>,
            fileIds: List<String>
        ) {
            val activity = getActivityFromContext(context)
            if (activity != null) {
                // Create the fragment instance
                val fragment = Tapped_Files_In_The_Container_View_Fragment()

                // Create bundle to pass data to the fragment
                val bundle = Bundle().apply {
                    putInt("current_index", currentIndex)
                    putInt("total_files", files.size)

                    // Convert files to ArrayList of URLs for easy passing
                    val fileUrls = ArrayList<String>()
                    files.forEach { file ->
                        fileUrls.add(file.url)
                    }
                    putStringArrayList("file_urls", fileUrls)
                    putStringArrayList("file_ids", ArrayList(fileIds))

                    // Create PostItem list for the ViewPager with audio-specific data
                    val postItems = ArrayList<PostItem>()
                    files.forEachIndexed { index, file ->
                        val fileId = fileIds.getOrNull(index)
                        val durationItem = currentPostData?.duration?.find { it.fileId == fileId }
                        val fileName = currentPostData?.fileNames?.find { it.fileId == fileId }?.fileName ?: ""

                        val postItem = PostItem(
                            audioUrl = file.url,
                            audioThumbnailUrl = null,
                            videoUrl = null,
                            videoThumbnailUrl = null,
                            postId = fileId ?: "audio_file_$index",
                            data = "Audio file: $fileName",
                            files = arrayListOf(file.url)
                        )
                        postItems.add(postItem)
                    }
                    putParcelableArrayList("post_list", postItems)

                    // Set a default post ID
                    putString("post_id", fileIds.getOrNull(currentIndex) ?: "audio_file_$currentIndex")

                    // Add audio-specific metadata
                    putString("media_type", "audio")
                }

                fragment.arguments = bundle

                Log.d(TAG, "Navigated to Tapped_Files_In_The_Container_View with ${files.size} " +
                        "audio files, starting at index $currentIndex")

                try {
                    // Navigate to the fragment with animation
                    activity.supportFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left,
                        )
                        .replace(R.id.frame_layout, fragment)
                        .addToBackStack("tapped_audio_files_view")
                        .commit()
                } catch (e: Exception){
                    Log.e(TAG, e.printStackTrace().toString())
                }

            } else {
                Log.e(TAG, "Activity is null, cannot navigate to fragment")
            }
        }

        @SuppressLint("SetTextI18n", "UseKtx")
        fun onBind(data: com.uyscuti.social.network.api.response.posts.Post) {

            this.currentPostData = data
            val context = itemView.context
            val fileIdToFind = data.fileIds[absoluteAdapterPosition]
            val displayMetrics = context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val margin = 4.dpToPx(context)
            val spaceBetweenRows = 4.dpToPx(context)

            // Get adaptive heights
            val (minHeight, maxHeight) = getAdaptiveHeights(context)

            val layoutParams = materialCardView.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.setMargins(0, 0, 0, 0)

            // Reset all views first
            imageView2.visibility = View.GONE
            countTextView.visibility = View.GONE
            itemView.visibility = View.VISIBLE
            artworkLayout.visibility = View.VISIBLE // Make sure artwork layout is visible for audios
            artworkImageView.visibility = View.VISIBLE

            // Set default artwork for audio files
            artworkImageView.setImageResource(R.drawable.music_placeholder)

            // Hide seek bars by default (they'll be shown in the detailed view)
            seekBar.visibility = View.GONE
            waveSeekBar.visibility = View.GONE

            val durationItem = data.duration?.find { it.fileId == fileIdToFind }
            audioDurationTextView.text = durationItem?.duration ?: ""
            audioDurationTextView.visibility = if (!durationItem?.duration.isNullOrEmpty()) View.VISIBLE else View.GONE

            val fileName = data.fileNames?.find { it.fileId == fileIdToFind }?.fileName ?: ""

            // Handle different audio formats
            when {
                fileName.endsWith(".mp3", true) ||
                        fileName.endsWith(".m4a", true) -> {
                    // Show artwork for common audio formats with music placeholder
                    artworkImageView.setImageResource(R.drawable.music_placeholder)
                    artworkImageView.visibility = View.VISIBLE
                    artworkLayout.visibility = View.VISIBLE
                }
                fileName.endsWith(".ogg", true) ||
                        fileName.endsWith(".aac", true) ||
                        fileName.endsWith(".wav", true) ||
                        fileName.endsWith(".flac", true) ||
                        fileName.endsWith(".amr", true) ||
                        fileName.endsWith(".3gp", true) ||
                        fileName.endsWith(".opus", true) -> {
                    // Show mic icon with styling
                    artworkImageView.setImageResource(R.drawable.mic_6)
                    artworkImageView.setPadding(64, 64, 64, 64)
                    artworkImageView.setBackgroundColor("#616161".toColorInt())
                    artworkLayout.visibility = View.VISIBLE
                    artworkImageView.visibility = View.VISIBLE

                }
                else -> {
                    // Default case - show artwork with music placeholder
                    artworkImageView.setImageResource(R.drawable.music_placeholder)
                    artworkImageView.visibility = View.VISIBLE
                    artworkLayout.visibility = View.VISIBLE
                }
            }

            // Layout configuration based on item count
            val itemCount = data.files.size

            when {
                itemCount <= 1 -> {
                    layoutParams.width = screenWidth
                    // Use adaptive height instead of fixed 300dp
                    layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.75).toInt())
                    materialCardView.radius = 8.dpToPx(context).toFloat()
                }
                itemCount == 2 -> {
                    layoutParams.width = screenWidth / 2
                    // Use adaptive height instead of fixed 300dp
                    layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.75).toInt())
                    val isLeft = absoluteAdapterPosition % 2 == 0
                    layoutParams.leftMargin = if (isLeft) 0 else (spaceBetweenRows)
                    layoutParams.rightMargin = if (isLeft) (spaceBetweenRows) else 0
                    materialCardView.radius = 8.dpToPx(context).toFloat()
                }
                itemCount == 3 -> {
                    when (absoluteAdapterPosition) {
                        0 -> {
                            layoutParams.width = screenWidth
                            // Use adaptive height for the main large audio item
                            layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.75).toInt())
                            if (layoutParams is StaggeredGridLayoutManager.LayoutParams) {
                                layoutParams.isFullSpan = true
                            }
                            layoutParams.bottomMargin = margin / 2
                        }
                        1, 2 -> {
                            layoutParams.width = screenWidth / 2
                            // Use adaptive height for smaller audio items
                            layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.75).toInt())
                            val isLeft = absoluteAdapterPosition == 1
                            layoutParams.leftMargin = if (isLeft) 0 else (spaceBetweenRows / 2)
                            layoutParams.rightMargin = if (isLeft) (spaceBetweenRows / 2) else 0
                            layoutParams.topMargin = margin / 2
                        }
                    }
                }

                itemCount == 4 -> {
                    if (absoluteAdapterPosition >= 4) {
                        itemView.visibility = View.GONE
                        layoutParams.width = 0
                        layoutParams.height = 0
                        itemView.layoutParams = layoutParams
                        return
                    }

                    layoutParams.width = screenWidth / 2
                    // Make height adaptive but maintain reasonable proportions for audio
                    val preferredHeight = getConstrainedHeight(context, (maxHeight * 0.75).toInt())
                    layoutParams.height = preferredHeight

                    val isLeft = absoluteAdapterPosition % 2 == 0
                    layoutParams.leftMargin = if (isLeft) 0 else (spaceBetweenRows / 2)
                    layoutParams.rightMargin = if (isLeft) (spaceBetweenRows / 2) else 0
                    layoutParams.topMargin = if (absoluteAdapterPosition < 2) 0 else (spaceBetweenRows)

                    countTextView.visibility = View.GONE
                    countTextView.setPadding(0, 0, 0, 0)
                    countTextView.background = null
                }

                itemCount > 4 -> {
                    if (absoluteAdapterPosition >= 4) {
                        itemView.visibility = View.GONE
                        layoutParams.width = 0
                        layoutParams.height = 0
                        itemView.layoutParams = layoutParams
                        return
                    }

                    layoutParams.width = screenWidth / 2
                    // Make height adaptive but maintain reasonable proportions for audio
                    val preferredHeight = getConstrainedHeight(context, (maxHeight * 0.75).toInt())
                    layoutParams.height = preferredHeight

                    val isLeft = absoluteAdapterPosition % 2 == 0
                    layoutParams.leftMargin = if (isLeft) 0 else (spaceBetweenRows / 2)
                    layoutParams.rightMargin = if (isLeft) (spaceBetweenRows / 2) else 0
                    layoutParams.topMargin = if (absoluteAdapterPosition < 2) 0 else (spaceBetweenRows)

                    if (absoluteAdapterPosition == 3) {
                        countTextView.visibility = View.VISIBLE
                        countTextView.text = "+${itemCount - 4}"
                        countTextView.textSize = 32f
                        countTextView.setPadding(12, 4, 12, 4)
                        countTextView.background = GradientDrawable().apply {
                            shape = GradientDrawable.RECTANGLE
                            cornerRadius = 16f
                            setColor(Color.parseColor("#80000000"))
                        }
                    } else {
                        countTextView.visibility = View.GONE
                        countTextView.setPadding(0, 0, 0, 0)
                        countTextView.background = null
                    }
                }

            }

            materialCardView.layoutParams = layoutParams

            // Updated click listeners to navigate to fragment
            itemView.setOnClickListener {
                // Navigate to the fragment instead of calling the old listener
                navigateToTappedFilesFragment(
                    context,
                    absoluteAdapterPosition,
                    data.files,
                    data.fileIds as List<String>
                )

                // Optional: Still call the original listener if you need it for other purposes
                onMultipleFilesClickListener?.multipleFileClickListener(
                    absoluteAdapterPosition,
                    data.files,
                    data.fileIds as List<String>
                )
            }

            // Add click listener to the artwork image for better UX
            artworkImageView.setOnClickListener {
                navigateToTappedFilesFragment(
                    context,
                    absoluteAdapterPosition,
                    data.files,
                    data.fileIds as List<String>
                )
            }

            // Add click listener to the artwork layout
            artworkLayout.setOnClickListener {
                navigateToTappedFilesFragment(
                    context,
                    absoluteAdapterPosition,
                    data.files,
                    data.fileIds as List<String>
                )
            }

            // Add click listener to the card view as well
            materialCardView.setOnClickListener {
                navigateToTappedFilesFragment(
                    context,
                    absoluteAdapterPosition,
                    data.files,
                    data.fileIds as List<String>
                )
            }

            // Add click listener to the count text view (for "more files" indicator)
            countTextView.setOnClickListener {
                navigateToTappedFilesFragment(
                    context,
                    absoluteAdapterPosition,
                    data.files,
                    data.fileIds as List<String>
                )
            }

            // Add click listener to the duration text view
            audioDurationTextView.setOnClickListener {
                navigateToTappedFilesFragment(
                    context,
                    absoluteAdapterPosition,
                    data.files,
                    data.fileIds as List<String>
                )
            }
        }
    }

    inner class FeedVideosOnly(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun Int.dpToPx(context: Context): Int {
            return (this * context.resources.displayMetrics.density).toInt()
        }

        private val feedThumbnail: ImageView = itemView.findViewById(R.id.feedThumbnail)
        private val feedVideoDurationTextView: TextView =
            itemView.findViewById(R.id.feedVideoDurationTextView)
        private val cardView: CardView = itemView.findViewById(R.id.cardView)
        private val countTextView: TextView = itemView.findViewById(R.id.countTextView)
        private val imageView2: ImageView = itemView.findViewById(R.id.imageView2)

        // Helper function to get adaptive heights based on screen size
        private fun getAdaptiveHeights(context: Context): Pair<Int, Int> {
            val displayMetrics = context.resources.displayMetrics
            val screenHeight = displayMetrics.heightPixels

            // For videos: min = 18% of screen height, max = 45% of screen height
            val minHeight = (screenHeight * 0.18).toInt()
            val maxHeight = (screenHeight * 0.45).toInt()

            return Pair(minHeight, maxHeight)
        }

        // Helper function to constrain height within min/max bounds
        private fun getConstrainedHeight(context: Context, targetHeight: Int): Int {
            val (minHeight, maxHeight) = getAdaptiveHeights(context)
            return targetHeight.coerceIn(minHeight, maxHeight)
        }

        // Helper function to get AppCompatActivity from context
        private fun getActivityFromContext(context: Context): AppCompatActivity? {
            return when (context) {
                is AppCompatActivity -> context
                is ContextWrapper -> getActivityFromContext(context.baseContext)
                else -> null
            }
        }

        private fun navigateToTappedFilesFragment(
            context: Context,
            currentIndex: Int,
            files: List<com.uyscuti.social.network.api.response.posts.File>,
            fileIds: List<String>
        ) {
            val activity = getActivityFromContext(context)
            if (activity != null) {
                // Create the fragment instance
                val fragment = Tapped_Files_In_The_Container_View_Fragment()

                // Create bundle to pass data to the fragment
                val bundle = Bundle().apply {
                    putInt("current_index", currentIndex)
                    putInt("total_files", files.size)

                    // Convert files to ArrayList of URLs for easy passing
                    val fileUrls = ArrayList<String>()
                    files.forEach { file ->
                        fileUrls.add(file.url)
                    }
                    putStringArrayList("file_urls", fileUrls)
                    putStringArrayList("file_ids", ArrayList(fileIds))

                    // **ADD THIS: Create PostItem list for the ViewPager**
                    val postItems = ArrayList<PostItem>()
                    files.forEachIndexed { index, file ->
                        val postItem = PostItem(
                            audioUrl = file.url,
                            audioThumbnailUrl = null,
                            videoUrl = file.url, // or null if it's not a video
                            videoThumbnailUrl = null,
                            postId = fileIds.getOrNull(index) ?: "file_$index",
                            data = "Post data for file $index",
                            files = arrayListOf(file.url) // Pass the URL
                        )
                        postItems.add(postItem)
                    }
                    putParcelableArrayList("post_list", postItems)

                    // Set a default post ID
                    putString("post_id", fileIds.getOrNull(currentIndex) ?: "file_$currentIndex")
                }

                fragment.arguments = bundle

                // Navigate to the fragment with animation
                activity.supportFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                    )
                    .replace(R.id.frame_layout, fragment)
                    .addToBackStack("tapped_files_view")
                    .commit()

                Log.d(TAG, "Navigated to Tapped_Files_In_The_Container_View with ${files.size} " +
                        "files, starting at index $currentIndex")
            } else {
                Log.e(TAG, "Activity is null, cannot navigate to fragment")
            }
        }

        @SuppressLint("SetTextI18n")
        fun onBind(data: com.uyscuti.social.network.api.response.posts.Post) {
            Log.d(TAG, "onBind: file type doc $absoluteAdapterPosition item count $itemCount")

            val fileIdToFind = data.fileIds[absoluteAdapterPosition]

            itemView.setOnClickListener {
                navigateToTappedFilesFragment(
                    itemView.context,
                    absoluteAdapterPosition,
                    data.files,
                    data.fileIds as List<String>
                )
                onMultipleFilesClickListener?.multipleFileClickListener(
                    absoluteAdapterPosition,
                    data.files,
                    data.fileIds as List<String>
                )
            }

            val durationItem = data.duration?.find { it.fileId == fileIdToFind }
            val thumbnail = data.thumbnail.find { it.fileId == fileIdToFind }

            feedVideoDurationTextView.text = durationItem?.duration

            val fileSize = itemCount

            val context = itemView.context
            val displayMetrics = context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val margin = 4.dpToPx(context)
            val spaceBetweenRows = 4.dpToPx(context)
            val sideMargin = 0 // No side margin to ensure items touch screen edges

            // Get adaptive heights
            val (minHeight, maxHeight) = getAdaptiveHeights(context)

            if (thumbnail != null) {
                Glide.with(context)
                    .load(thumbnail.thumbnailUrl)
                    .placeholder(R.drawable.flash21)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(feedThumbnail)
            } else {
                Glide.with(context)
                    .load(R.drawable.videoplaceholder)
                    .placeholder(R.drawable.flash21)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(feedThumbnail)
            }

            val layoutParams = cardView.layoutParams as ViewGroup.MarginLayoutParams

            // Apply rounded corners to the CardView
            cardView.radius = 8.dpToPx(context).toFloat() // Adjust the radius as needed

            // Updated click listener to navigate to fragment
            itemView.setOnClickListener {
                // Navigate to the fragment instead of calling the old listener
                navigateToTappedFilesFragment(
                    context,
                    absoluteAdapterPosition,
                    data.files,
                    data.fileIds as List<String>
                )

                // Optional: Still call the original listener if you need it for other purposes
                onMultipleFilesClickListener?.multipleFileClickListener(
                    absoluteAdapterPosition,
                    data.files,
                    data.fileIds as List<String>
                )
            }

            // Also add click listener to the image itself for better UX
            feedThumbnail.setOnClickListener {
                navigateToTappedFilesFragment(
                    context,
                    absoluteAdapterPosition,
                    data.files,
                    data.fileIds as List<String>
                )
            }

            // Add click listener to the video duration text view
            feedVideoDurationTextView.setOnClickListener {
                navigateToTappedFilesFragment(
                    context,
                    absoluteAdapterPosition,
                    data.files,
                    data.fileIds as List<String>
                )
            }

            // Also add click listener to the count text view for better UX
            countTextView.setOnClickListener {
                navigateToTappedFilesFragment(
                    context,
                    absoluteAdapterPosition,
                    data.files,
                    data.fileIds as List<String>
                )
            }

            // Add click listener to the card view as well
            cardView.setOnClickListener {
                navigateToTappedFilesFragment(
                    context,
                    absoluteAdapterPosition,
                    data.files,
                    data.fileIds as List<String>
                )
            }

            // Add click listener to imageView2 as well
            imageView2.setOnClickListener {
                navigateToTappedFilesFragment(
                    context,
                    absoluteAdapterPosition,
                    data.files,
                    data.fileIds as List<String>
                )
            }

            when {
                fileSize <= 1 -> {
                    layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                    // Use 90% of max height for single video
                    layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.75).toInt())
                    layoutParams.leftMargin = 0
                    layoutParams.rightMargin = 0
                    layoutParams.topMargin = 0
                    layoutParams.bottomMargin = 0
                }

                fileSize == 2 -> {
                    layoutParams.width = screenWidth / 2
                    // Use 80% of max height for two videos
                    layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.6).toInt())
                    layoutParams.topMargin = 0 // No top margin for top row items
                    layoutParams.bottomMargin = 0 // No bottom margin for bottom row items

                    // Handle left and right margins for columns
                    val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
                    layoutParams.leftMargin = if (isLeftColumn) 0 else (spaceBetweenRows)
                    layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows) else 0
                }

                fileSize == 3 -> {
                    val spanLayout = cardView.layoutParams as? StaggeredGridLayoutManager.LayoutParams

                    if (absoluteAdapterPosition == 0) {
                        // First item spans the full width
                        spanLayout?.isFullSpan = true
                        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                        // Use 75% of max height for main video in 3-layout
                        layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.6).toInt())
                        layoutParams.leftMargin = 0
                        layoutParams.rightMargin = 0
                        layoutParams.topMargin = 0
                        layoutParams.bottomMargin = margin // No bottom margin for the top item
                    } else {
                        spanLayout?.isFullSpan = false
                        layoutParams.width = (screenWidth - margin - (2 * sideMargin)) / 2
                        // Use 65% of max height for smaller videos in 3-layout
                        layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.6).toInt())

                        layoutParams.topMargin = 0 // Consistent top margin for all
                        layoutParams.bottomMargin = 0 // Remove bottom margin for bottom row items

                        if (absoluteAdapterPosition == 1) { // Bottom-left item
                            layoutParams.leftMargin = 0 // Touch the left screen edge
                            layoutParams.rightMargin = (spaceBetweenRows / 2)
                        } else if (absoluteAdapterPosition == 2) { // Bottom-right item
                            layoutParams.leftMargin = (spaceBetweenRows / 2)
                            layoutParams.rightMargin = 0 // Touch the right edge
                        }
                    }

                    if (absoluteAdapterPosition == 2) {
                        // Bottom-right file spans the full width
                        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                        layoutParams.leftMargin = 0
                        layoutParams.rightMargin = 0
                        layoutParams.bottomMargin = 0 // No bottom margin for the last item
                    }

                    cardView.layoutParams = layoutParams
                }

                fileSize == 4 -> {
                    // Square items in 2x2 grid, but constrained by adaptive height
                    val idealSquareSize = screenWidth / 2 // Exact half width
                    val constrainedHeight = getConstrainedHeight(context, idealSquareSize)

                    layoutParams.width = screenWidth / 2
                    layoutParams.height = constrainedHeight

                    // Only add margin between items, not on screen edges
                    val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
                    layoutParams.leftMargin = if (isLeftColumn) 0 else (spaceBetweenRows/2)
                    layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows/2) else 0

                    // Add space between the top row and bottom row
                    if (absoluteAdapterPosition < 2) { // Top row
                        layoutParams.topMargin = 0
                        layoutParams.bottomMargin = (margin / 2) // Space between top and bottom rows
                    } else { // Bottom row
                        layoutParams.topMargin = (margin/2)
                        layoutParams.bottomMargin = 0
                    }
                }

                fileSize > 4 -> {
                    if (absoluteAdapterPosition >= 4) {
                        // Hide extra files beyond the first 4
                        itemView.visibility = View.GONE
                        layoutParams.width = 0
                        layoutParams.height = 0
                        itemView.layoutParams = layoutParams
                        return
                    }

                    // Show item
                    itemView.visibility = View.VISIBLE

                    // Set item size as square in 2x2 grid, but constrained by adaptive height
                    val idealSquareSize = screenWidth / 2
                    val constrainedHeight = getConstrainedHeight(context, idealSquareSize)

                    layoutParams.width = screenWidth / 2
                    layoutParams.height = constrainedHeight

                    // Horizontal margins: space only between items, not edges
                    val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
                    layoutParams.leftMargin = if (isLeftColumn) 0 else (spaceBetweenRows)
                    layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows) else 0

                    // Vertical spacing between top and bottom row using spaceBetweenRows
                    layoutParams.topMargin = if (absoluteAdapterPosition < 2) 0 else (spaceBetweenRows /2)
                    layoutParams.bottomMargin = if (absoluteAdapterPosition < 2) (spaceBetweenRows /2) else 0

                    // Apply layout params to outer item and cardView
                    itemView.layoutParams = layoutParams
                    cardView.layoutParams = layoutParams

                    // Ensure no internal padding or margin inside card
                    cardView.setContentPadding(0, 0, 0, 0)
                    val cardMargins = cardView.layoutParams as ViewGroup.MarginLayoutParams
                    cardView.layoutParams = cardMargins

                    // Overlay "+N" for the last visible item
                    if (absoluteAdapterPosition == 3) {
                        countTextView.visibility = View.VISIBLE
                        countTextView.text = "+${fileSize - 4}"
                        countTextView.textSize = 32f
                        countTextView.setPadding(12, 4, 12, 4) // Optional: padding for better appearance

                        // Create rounded dimmed background
                        val background = GradientDrawable().apply {
                            shape = GradientDrawable.RECTANGLE
                            cornerRadius = 16f // Rounded corners
                            setColor(Color.parseColor("#80000000")) // Semi-transparent black
                        }
                        countTextView.background = background
                    } else {
                        countTextView.visibility = View.GONE
                        countTextView.setPadding(0, 0, 0, 0) // Reset padding if needed
                        countTextView.background = null // Clear background
                    }
                }
            }

            cardView.layoutParams = layoutParams
        }
    }

    inner class FeedDocumentsOnly(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun Int.dpToPx(context: Context): Int {
            return (this * context.resources.displayMetrics.density).toInt()
        }

        private var tag = "FeedDocument"
        private val pdfImageView: ImageView = itemView.findViewById(R.id.pdfImageView)
        private val documentContainer: CardView = itemView.findViewById(R.id.documentContainer)
        private val fileTypeIcon : ImageView = itemView.findViewById(R.id.fileTypeIcon)

        // Helper function to get adaptive heights based on screen size
        private fun getAdaptiveHeights(context: Context): Pair<Int, Int> {
            val displayMetrics = context.resources.displayMetrics
            val screenHeight = displayMetrics.heightPixels

            // For documents: min = 15% of screen height, max = 38% of screen height
            val minHeight = (screenHeight * 0.15).toInt()
            val maxHeight = (screenHeight * 0.38).toInt()

            return Pair(minHeight, maxHeight)
        }

        // Helper function to constrain height within min/max bounds
        private fun getConstrainedHeight(context: Context, targetHeight: Int): Int {
            val (minHeight, maxHeight) = getAdaptiveHeights(context)
            return targetHeight.coerceIn(minHeight, maxHeight)
        }

        //A Helper function to get AppCompatActivity from context
        private fun getActivityFromContext(context: Context): AppCompatActivity? {
            return when (context) {
                is AppCompatActivity -> context
                is ContextWrapper -> getActivityFromContext(context.baseContext)
                else -> null
            }
        }

        private fun navigateToTappedFilesFragment(
            context: Context,
            currentIndex: Int,
            files: List<com.uyscuti.social.network.api.response.posts.File>,
            fileIds: List<String>
        ) {
            val activity = getActivityFromContext(context)
            if (activity != null) {
                // Creating the fragment instance
                val fragment = Tapped_Files_In_The_Container_View_Fragment()

                // Creating bundle to pass data to the fragment
                val bundle = Bundle().apply {
                    putInt("current_index", currentIndex)
                    putInt("total_files", files.size)

                    // Converting files to ArrayList of URLs for easy passing
                    val fileUrls = ArrayList<String>()
                    files.forEach { file ->
                        fileUrls.add(file.url)
                    }
                    putStringArrayList("file_urls", fileUrls)
                    putStringArrayList("file_ids", ArrayList(fileIds))

                    // Creating PostItem list for the ViewPager**
                    val postItems = ArrayList<PostItem>()
                    files.forEachIndexed { index, file ->
                        val postItem = PostItem(
                            audioUrl = file.url, // or null if it's not a video
                            audioThumbnailUrl = null,
                            videoUrl = file.url, // or null if it's not a video
                            videoThumbnailUrl = null,
                            postId = fileIds.getOrNull(index) ?: "file_$index",
                            data = "Post data for file $index",
                            files = arrayListOf(file.url) // Pass the URL
                        )
                        postItems.add(postItem)
                    }
                    putParcelableArrayList("post_list", postItems)

                    // Set a default post ID
                    putString("post_id", fileIds.getOrNull(currentIndex) ?: "file_$currentIndex")
                }

                fragment.arguments = bundle

                // Navigating to the fragment with animation
                activity.supportFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                    )
                    .replace(R.id.frame_layout, fragment)
                    .addToBackStack("tapped_files_view")
                    .commit()

                Log.d(TAG, "Navigated to Tapped_Files_In_The_Container_View with ${files.size} " +
                        "files, starting at index $currentIndex")
            } else {
                Log.e(TAG, "Activity is null, cannot navigate to fragment")
            }
        }

        @SuppressLint("SetTextI18n", "UseKtx")
        fun onBind(data: com.uyscuti.social.network.api.response.posts.Post) {

            val sideMargin = 2.dpToPx(itemView.context)
            val context = itemView.context // Fix: Define context properly

            Log.d(TAG, "onBind: file type doc $absoluteAdapterPosition item count $itemCount")

            val fileIdToFind = data.fileIds[absoluteAdapterPosition]
            val documentType = data.fileTypes?.find { it.fileId == fileIdToFind }

            val fileSize = itemCount

            // Get adaptive heights
            val (minHeight, maxHeight) = getAdaptiveHeights(context)

            // Set the file type icon (e.g., PDF, DOCX, PPTX)
            if (documentType != null) {

                val fileExtension = documentType.fileType

                fileTypeIcon.setImageResource(
                    when (fileExtension) {
                        "pdf" -> R.drawable.pdf_icon
                        "doc", "docx" -> R.drawable.word_icon
                        "ppt", "pptx" -> R.drawable.powerpoint_icon
                        "xls", "xlsx" -> R.drawable.excel_icon
                        "txt" -> R.drawable.text_icon
                        "rtf" -> R.drawable.text_icon
                        "odt" -> R.drawable.word_icon
                        "csv" -> R.drawable.excel_icon
                        else ->  R.drawable.text_icon
                    }
                )
                fileTypeIcon.visibility = View.VISIBLE
            }

            if (documentType != null) {

                // Handle PDF files
                if (documentType.fileType == "pdf") {
                    val thumbnail = data.thumbnail.find { it.fileId == fileIdToFind }
                    pdfImageView.scaleType = ImageView.ScaleType.CENTER_INSIDE

                    if (thumbnail != null) {
                        Glide.with(context)
                            .load(thumbnail.thumbnailUrl)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(pdfImageView)
                    }

                    pdfImageView.visibility = View.VISIBLE
                } else if (documentType.fileType == "docx" || documentType.fileType == "pptx") {
                    val thumbnail = data.thumbnail.find { it.fileId == fileIdToFind }
                    pdfImageView.scaleType = ImageView.ScaleType.CENTER_INSIDE

                    Log.d(tag, "onBind: Documents File type is not pdf")
                    Glide.with(context)
                        .load(thumbnail?.thumbnailUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(pdfImageView)
                    pdfImageView.visibility = View.VISIBLE
                }

                // COMPREHENSIVE CLICK HANDLING - Similar to video version
                // Updated click listener to navigate to fragment
                itemView.setOnClickListener {
                    navigateToTappedFilesFragment(
                        context,
                        absoluteAdapterPosition,
                        data.files,
                        data.fileIds as List<String>
                    )

                    // Optional: Still call the original listener if you need it for other purposes
                    onMultipleFilesClickListener?.multipleFileClickListener(
                        absoluteAdapterPosition,
                        data.files,
                        data.fileIds as List<String>
                    )
                }

                // Also add click listener to the document image itself for better UX
                pdfImageView.setOnClickListener {
                    navigateToTappedFilesFragment(
                        context,
                        absoluteAdapterPosition,
                        data.files,
                        data.fileIds as List<String>
                    )
                }

                // Add click listener to the document container (CardView)
                documentContainer.setOnClickListener {
                    navigateToTappedFilesFragment(
                        context,
                        absoluteAdapterPosition,
                        data.files,
                        data.fileIds as List<String>
                    )
                }

                // Add click listener to the file type icon
                fileTypeIcon.setOnClickListener {
                    navigateToTappedFilesFragment(
                        context,
                        absoluteAdapterPosition,
                        data.files,
                        data.fileIds as List<String>
                    )
                }

                when {

                    fileSize == 1 -> {

                        Log.d(TAG, "bind: file size 1")

                        val topMargin = (-8).dpToPx(context)
                        // Use 85% of max height for single document
                        val adaptiveHeight = getConstrainedHeight(context, (maxHeight * 0.85).toInt())

                        val containerParams =
                            documentContainer.layoutParams as ViewGroup.MarginLayoutParams
                        containerParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                        containerParams.height = adaptiveHeight
                        containerParams.setMargins(0, topMargin, 0, 0)
                        documentContainer.layoutParams = containerParams
                        documentContainer.setBackgroundColor(Color.BLACK)

                        // Clear container
                        documentContainer.removeAllViews()

                        val centerContainer = FrameLayout(context).apply {
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            setPadding(0, 0, 0, 0)
                            setBackgroundColor(Color.rgb(160, 160, 160))
                        }

                        // Create a new ImageView for single document view to avoid parent conflicts
                        val singleImageView = ImageView(context).apply {
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                Gravity.CENTER
                            ).apply {
                                height = adaptiveHeight
                            }
                            scaleType = ImageView.ScaleType.FIT_CENTER
                        }

                        // Add click listener to the single image view
                        singleImageView.setOnClickListener {
                            navigateToTappedFilesFragment(
                                context,
                                absoluteAdapterPosition,
                                data.files,
                                data.fileIds as List<String>
                            )
                        }

                        // Add image view to the center container
                        centerContainer.addView(singleImageView)

                        // Create an overlay for the fileTypeIcon
                        val overlayLayout = FrameLayout(context).apply {
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }

                        // Configur the fileTypeIcon for the overlay
                        (fileTypeIcon.parent as? ViewGroup)?.removeView(fileTypeIcon)
                        fileTypeIcon.layoutParams = FrameLayout.LayoutParams(
                            20.dpToPx(context), // Width in dp
                            20.dpToPx(context), // Height in dp
                            Gravity.TOP or Gravity.START // Top-left corner
                        ).apply {
                            setMargins(8.dpToPx(context),
                                8.dpToPx(context), 0, 0) // Optional: add slight margin from top/left
                        }

                        // Re-adding click listener to fileTypeIcon after re-adding to layout
                        fileTypeIcon.setOnClickListener {
                            navigateToTappedFilesFragment(
                                context,
                                absoluteAdapterPosition,
                                data.files,
                                data.fileIds as List<String>
                            )
                        }

                        // Adding fileTypeIcon to the overlay
                        overlayLayout.addView(fileTypeIcon)

                        // Adding click listener to the overlay layout
                        overlayLayout.setOnClickListener {
                            navigateToTappedFilesFragment(
                                context,
                                absoluteAdapterPosition,
                                data.files,
                                data.fileIds as List<String>
                            )
                        }

                        // Adding the overlay on top of the center container
                        centerContainer.addView(overlayLayout)

                        // Add click listener to the center container
                        centerContainer.setOnClickListener {
                            navigateToTappedFilesFragment(
                                context,
                                absoluteAdapterPosition,
                                data.files,
                                data.fileIds as List<String>
                            )
                        }

                        // Adding the center container to the document container
                        documentContainer.addView(centerContainer)

                        val thumbnails = data.thumbnail.filter { thumb ->
                            data.fileIds.contains(thumb.fileId)
                        }

                        // Loading the image thumbnail into singleImageView
                        thumbnails.getOrNull(0)?.let { thumb ->
                            Glide.with(context)
                                .load(thumb.thumbnailUrl)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(singleImageView)
                        }
                    }

                    fileSize == 2 -> {

                        Log.d(TAG, "onBind: Document file size == 2")

                        val cardView = itemView.findViewById<CardView>(R.id.documentContainer)
                        val imageView = itemView.findViewById<ImageView>(R.id.pdfImageView)

                        // Use 75% of max height for two documents
                        val adaptiveHeight = getConstrainedHeight(context, (maxHeight * 0.70).toInt())

                        // Layout params for CardView
                        val cardLayoutParams = cardView.layoutParams as ViewGroup.MarginLayoutParams
                        cardLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                        cardLayoutParams.height = adaptiveHeight // Set adaptive height for both CardViews

                        // Reset all margins
                        cardLayoutParams.topMargin = 0
                        cardLayoutParams.bottomMargin = 0

                        when (absoluteAdapterPosition) {
                            0 -> {
                                // First item: Touch left edge
                                cardLayoutParams.setMargins(0, 0, sideMargin, 0)
                            }
                            1 -> {
                                // Second item: Touch right edge
                                cardLayoutParams.setMargins(sideMargin, 0, 0, 0)
                            }
                        }

                        cardView.layoutParams = cardLayoutParams

                        // Match ImageView height to the CardView's height
                        val imageLayoutParams = imageView.layoutParams as ViewGroup.MarginLayoutParams
                        imageLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                        imageLayoutParams.height = cardLayoutParams.height // Match the height of the CardView
                        imageLayoutParams.topMargin = 0
                        imageLayoutParams.bottomMargin = 0
                        imageView.layoutParams = imageLayoutParams

                        // Ensure image scales properly
                        imageView.scaleType = ImageView.ScaleType.FIT_XY
                    }

                    fileSize >= 3 -> {

                        Log.d(TAG, "onBind: Document file size >= 3")

                        // Hide additional items (index 2 and beyond)
                        if (absoluteAdapterPosition >= 2) {
                            Log.d(TAG, "onBind: position >= 2, hiding item view")
                            itemView.visibility = View.GONE
                            itemView.layoutParams = RecyclerView.LayoutParams(0, 0) // Prevent item from taking space
                            return
                        }

                        val cardView = itemView.findViewById<CardView>(R.id.documentContainer)
                        val imageView = itemView.findViewById<ImageView>(R.id.pdfImageView)

                        // Use 70% of max height for multiple documents
                        val adaptiveHeight = getConstrainedHeight(context, (maxHeight * 0.70).toInt())

                        // Match CardView to parent with adaptive height
                        val cardLayoutParams = cardView.layoutParams as ViewGroup.MarginLayoutParams
                        cardLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                        cardLayoutParams.height = adaptiveHeight // Adaptive height for all CardViews

                        when (absoluteAdapterPosition) {
                            0 -> {
                                // First item: Touch left edge, gap on right
                                cardLayoutParams.setMargins(0, 0, sideMargin, 0)
                            }
                            1 -> {
                                // Second item: Gap on left, touch right edge
                                cardLayoutParams.setMargins(sideMargin, 0, 0, 0)
                            }
                        }

                        cardView.layoutParams = cardLayoutParams

                        // Set the ImageView height to match the CardView
                        val imageLayoutParams = imageView.layoutParams as ViewGroup.MarginLayoutParams
                        imageLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                        imageLayoutParams.height = cardLayoutParams.height // Match the height of the CardView
                        imageView.layoutParams = imageLayoutParams

                        // Ensure the image scales properly
                        imageView.scaleType = ImageView.ScaleType.FIT_XY

                        // Handle different cases for position 0 and position 1 (additional files)
                        when (absoluteAdapterPosition) {

                            0 -> {

                                Log.d(TAG, "onBind: position 0 for document with additional files")

                                // Remove any previously added overlays (from recycled views)
                                val parent = imageView.parent as ViewGroup

                                parent.findViewWithTag<View>("overlay_tag")?.let {
                                    parent.removeView(it)
                                }
                                parent.findViewWithTag<View>("text_overlay_container")?.let {
                                    parent.removeView(it)
                                }
                            }

                            1 -> {

                                Log.d(TAG, "onBind: position 1 for document with additional files")

                                val remainingFilesCount = fileSize - 2
                                val plusCountText = "+$remainingFilesCount"

                                val parent = imageView.parent as ViewGroup

                                // Add overlay only if not already added
                                if (parent.findViewWithTag<View>("overlay_tag") == null) {
                                    // Create a FrameLayout to wrap the ImageView + Overlay + Text
                                    val imageWrapper = FrameLayout(context).apply {
                                        layoutParams = imageView.layoutParams
                                    }

                                    // Remove the ImageView from its parent and re-add in wrapper
                                    val index = parent.indexOfChild(imageView)
                                    parent.removeView(imageView)
                                    imageWrapper.addView(imageView)

                                    // Create the container for the dim effect around the "+N" count text
                                    val overlayContainer = FrameLayout(context).apply {
                                        // Create rounded dimmed background
                                        background = GradientDrawable().apply {
                                            shape = GradientDrawable.RECTANGLE
                                            cornerRadius = 16f // Adjust the radius as needed
                                            setColor(Color.parseColor("#80000000")) // Semi-transparent black
                                        }

                                        tag = "overlay_tag"

                                        layoutParams = FrameLayout.LayoutParams(
                                            FrameLayout.LayoutParams.WRAP_CONTENT,
                                            FrameLayout.LayoutParams.WRAP_CONTENT
                                        ).apply {
                                            gravity = Gravity.BOTTOM or Gravity.END
                                            marginEnd = 8
                                            bottomMargin = 8
                                        }

                                        setPadding(12, 4, 12, 4) // Padding around the text
                                    }

                                    // Add click listener to the overlay container
                                    overlayContainer.setOnClickListener {
                                        navigateToTappedFilesFragment(
                                            context,
                                            absoluteAdapterPosition,
                                            data.files,
                                            data.fileIds as List<String>
                                        )
                                    }

                                    // Create the "+N" TextView
                                    val textView = TextView(context).apply {
                                        text = plusCountText
                                        setTextColor(Color.WHITE)
                                        textSize = 32f
                                        gravity = Gravity.CENTER
                                    }

                                    // Add click listener to the text view
                                    textView.setOnClickListener {
                                        navigateToTappedFilesFragment(
                                            context,
                                            absoluteAdapterPosition,
                                            data.files,
                                            data.fileIds as List<String>
                                        )
                                    }

                                    // Add TextView to the container
                                    overlayContainer.addView(textView)

                                    // Add container to the image wrapper
                                    imageWrapper.addView(overlayContainer)

                                    // Add click listener to the image wrapper
                                    imageWrapper.setOnClickListener {
                                        navigateToTappedFilesFragment(
                                            context,
                                            absoluteAdapterPosition,
                                            data.files,
                                            data.fileIds as List<String>
                                        )
                                    }

                                    // Add everything back to the original parent
                                    parent.addView(imageWrapper, index)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    inner class FeedCombinationOfMultipleFiles(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val materialCardView: MaterialCardView = itemView.findViewById(R.id.materialCardView)
        private val countTextView: TextView = itemView.findViewById(R.id.textView)
        private val imageView2: ImageView = itemView.findViewById(R.id.imageViewOverlay)
        private val fileTypeIcon: ImageView = itemView.findViewById(R.id.fileTypeIcon)
        private val playButton: ImageView = itemView.findViewById(R.id.playButton)
        private val feedVideoImageView: ImageView = itemView.findViewById(R.id.feedVideoImageView)
        private val feedVideoDurationTextView: TextView = itemView.findViewById(R.id.feedVideoDurationTextView)


        fun Int.dpToPx(context: Context): Int {
            return (this * context.resources.displayMetrics.density).toInt()
        }

        // Helper function to calculate responsive height based on screen size
        private fun calculateResponsiveHeight(context: Context, fileSize: Int): Int {
            val displayMetrics = context.resources.displayMetrics
            val screenHeight = displayMetrics.heightPixels
            val screenWidth = displayMetrics.widthPixels

            return when {
                // For tablets (assuming width > 600dp)
                screenWidth > 600.dpToPx(context) -> {
                    when (fileSize) {
                        1 -> (screenHeight * 0.45).toInt() // 45% of screen height
                        2, 4 -> (screenHeight * 0.25).toInt() // 25% of screen height
                        3 -> if (absoluteAdapterPosition == 0) (screenHeight * 0.3).toInt() else (screenHeight * 0.2).toInt()
                        else -> (screenHeight * 0.25).toInt()
                    }
                }
                // For large phones (height > 2000px)
                screenHeight > 2000 -> {
                    when (fileSize) {
                        1 -> (screenHeight * 0.4).toInt() // 40% of screen height
                        2, 4 -> (screenHeight * 0.22).toInt() // 22% of screen height
                        3 -> if (absoluteAdapterPosition == 0) (screenHeight * 0.25).toInt() else (screenHeight * 0.18).toInt()
                        else -> (screenHeight * 0.22).toInt()
                    }
                }
                // For medium phones (height > 1500px)
                screenHeight > 1500 -> {
                    when (fileSize) {
                        1 -> (screenHeight * 0.35).toInt() // 35% of screen height
                        2, 4 -> (screenHeight * 0.2).toInt() // 20% of screen height
                        3 -> if (absoluteAdapterPosition == 0) (screenHeight * 0.22).toInt() else (screenHeight * 0.18).toInt()
                        else -> (screenHeight * 0.2).toInt()
                    }
                }
                // For smaller phones
                else -> {
                    when (fileSize) {
                        1 -> (screenHeight * 0.4).toInt() // 30% of screen height
                        2, 4 -> (screenHeight * 0.2).toInt() // 18% of screen height
                        3 -> if (absoluteAdapterPosition == 0) (screenHeight * 0.2).toInt() else (screenHeight * 0.18).toInt()
                        else -> (screenHeight * 0.2).toInt()
                    }
                }
            }
        }

        // Helper function to get AppCompatActivity from context
        private fun getActivityFromContext(context: Context): AppCompatActivity? {
            return when (context) {
                is AppCompatActivity -> context
                is ContextWrapper -> getActivityFromContext(context.baseContext)
                else -> null
            }
        }

        private fun navigateToTappedFilesFragment(
            context: Context,
            currentIndex: Int,
            files: List<com.uyscuti.social.network.api.response.posts.File>,
            fileIds: List<String>
        ) {
            val activity = getActivityFromContext(context)
            if (activity != null) {
                // Create the fragment instance
                val fragment = Tapped_Files_In_The_Container_View_Fragment()

                // Create bundle to pass data to the fragment
                val bundle = Bundle().apply {
                    putInt("current_index", currentIndex)
                    putInt("total_files", files.size)

                    // Convert files to ArrayList of URLs for easy passing
                    val fileUrls = ArrayList<String>()
                    files.forEach { file ->
                        fileUrls.add(file.url)
                    }
                    putStringArrayList("file_urls", fileUrls)
                    putStringArrayList("file_ids", ArrayList(fileIds))

                    // **ADD THIS: Create PostItem list for the ViewPager**
                    val postItems = ArrayList<PostItem>()
                    files.forEachIndexed { index, file ->
                        val postItem = PostItem(
                            audioUrl = file.url, // or null if it's not a video
                            audioThumbnailUrl = null,
                            videoUrl = file.url, // or null if it's not a video
                            videoThumbnailUrl = null,
                            postId = fileIds.getOrNull(index) ?: "file_$index",
                            data = "Post data for file $index",
                            files = arrayListOf(file.url) // Pass the URL
                        )
                        postItems.add(postItem)
                    }
                    putParcelableArrayList("post_list", postItems)

                    // Set a default post ID
                    putString("post_id", fileIds.getOrNull(currentIndex) ?: "file_$currentIndex")
                }

                fragment.arguments = bundle

                // Navigate to the fragment with animation
                activity.supportFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                    )
                    .replace(R.id.frame_layout, fragment)
                    .addToBackStack("tapped_files_view")
                    .commit()

                Log.d(TAG, "Navigated to Tapped_Files_In_The_Container_View with ${files.size} " +
                        "files, starting at index $currentIndex")
            } else {
                Log.e(TAG, "Activity is null, cannot navigate to fragment")
            }
        }

        fun onBind(data: com.uyscuti.social.network.api.response.posts.Post) {

            val context = itemView.context
            val displayMetrics = context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val SpaceBetweenRows = 4.dpToPx(context) // Space between cards

            // Calculate responsive height based on screen size and file count
            val cardHeight = calculateResponsiveHeight(context, data.files.size)

            val fileIdToFind = data.fileIds[absoluteAdapterPosition]
            val file = data.files.find { it.fileId == fileIdToFind }
            val fileUrl = file?.url ?: data.files.getOrNull(absoluteAdapterPosition)?.url ?: ""
            val mimeType = data.fileTypes.getOrNull(absoluteAdapterPosition)?.fileType ?: ""
            val durationItem = data.duration?.find { it.fileId == fileIdToFind }
            feedVideoDurationTextView.text = durationItem?.duration


            val fileSize = data.files.size

            itemView.setOnClickListener {
                onMultipleFilesClickListener?.multipleFileClickListener(
                    absoluteAdapterPosition, // current image index
                    data.files,              // all files in the post
                    data.fileIds as List<String>            // file IDs
                )
            }

            val layoutParams = materialCardView.layoutParams as ViewGroup.MarginLayoutParams

            // Set default visibility for media controls
            playButton.visibility = View.GONE
            feedVideoImageView.visibility = View.GONE
            feedVideoDurationTextView.visibility = View.VISIBLE
            imageView2.visibility = View.GONE
            countTextView.visibility = View.GONE

            // Updated click listener to navigate to fragment
            itemView.setOnClickListener {
                // Navigate to the fragment instead of calling the old listener
                navigateToTappedFilesFragment(
                    context,
                    absoluteAdapterPosition,
                    data.files,
                    data.fileIds as List<String>
                )

                // Optional: Still call the original listener if you need it for other purposes
                onMultipleFilesClickListener?.multipleFileClickListener(
                    absoluteAdapterPosition,
                    data.files,
                    data.fileIds as List<String>
                )
            }

            // Also add click listener to the image itself for better UX
            countTextView.setOnClickListener {
                navigateToTappedFilesFragment(
                    context,
                    absoluteAdapterPosition,
                    data.files,
                    data.fileIds as List<String>
                )
            }

            // Add click listener to the card view as well
            imageView.setOnClickListener {
                navigateToTappedFilesFragment(
                    context,
                    absoluteAdapterPosition,
                    data.files,
                    data.fileIds as List<String>
                )
            }

            // Add click listener to the card view as well
            imageView2.setOnClickListener {
                navigateToTappedFilesFragment(
                    context,
                    absoluteAdapterPosition,
                    data.files,
                    data.fileIds as List<String>
                )
            }

            // Add click listener to the card view as well
            materialCardView.setOnClickListener {
                navigateToTappedFilesFragment(
                    context,
                    absoluteAdapterPosition,
                    data.files,
                    data.fileIds as List<String>
                )
            }

            // Add click listener to the card view as well
            feedVideoImageView.setOnClickListener {
                navigateToTappedFilesFragment(
                    context,
                    absoluteAdapterPosition,
                    data.files,
                    data.fileIds as List<String>
                )
            }

            // Determine the file preview based on MIME type
            when {

                mimeType.startsWith("image") -> {
                    loadImage(fileUrl)
                    fileTypeIcon.visibility = View.GONE
                }

                mimeType.startsWith("video") -> {
                    loadVideoThumbnail(fileUrl)
                    fileTypeIcon.visibility = View.VISIBLE
                    playButton.visibility = View.VISIBLE
                    feedVideoImageView.visibility = View.VISIBLE
                }

                mimeType.startsWith("audio") -> {
                    fileTypeIcon.setImageResource(R.drawable.ic_audio)
                    fileTypeIcon.visibility = View.VISIBLE
                    playButton.visibility = View.VISIBLE
                }

                mimeType.contains("pdf") || mimeType.contains("docx")
                        || mimeType.contains("pptx") || mimeType.contains("xlsx")
                        || mimeType.contains("ppt") || mimeType.contains("xls")
                        || mimeType.contains("xls") || mimeType.contains("txt")
                        || mimeType.contains("rtf") || mimeType.contains("odt")
                        || mimeType.contains("csv")-> {

                    // Load the first page (thumbnail) of the document
                    val thumbnail = data.thumbnail.find { it.fileId == fileIdToFind }
                    imageView.scaleType = ImageView.ScaleType.FIT_XY // Ensure the thumbnail fills the width

                    if (thumbnail != null) {
                        Glide.with(itemView.context)
                            .load(thumbnail.thumbnailUrl)
                            .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache the image
                            .into(imageView)
                    }

                    fileTypeIcon.setImageResource(
                        when {
                            mimeType.contains("pdf") -> R.drawable.pdf_icon
                            mimeType.contains("docx")  -> R.drawable.word_icon
                            mimeType.contains("pptx") -> R.drawable.powerpoint_icon
                            mimeType.contains("xlsx") -> R.drawable.excel_icon
                            mimeType.contains("ppt") -> R.drawable.powerpoint_icon
                            mimeType.contains("xls") -> R.drawable.excel_icon
                            mimeType.contains("txt") -> R.drawable.text_icon
                            mimeType.contains("rtf") -> R.drawable.text_icon
                            mimeType.contains("odt") -> R.drawable.word_icon
                            mimeType.contains( "csv") -> R.drawable.excel_icon
                            else -> R.drawable.text_icon
                        }
                    )
                    fileTypeIcon.visibility = View.VISIBLE
                    imageView.visibility = View.VISIBLE
                }
                else -> {
                    imageView.setImageResource(R.drawable.feed_mixed_image_view_rounded_corners)
                    fileTypeIcon.visibility = View.GONE
                }
            }

            // Layout logic for spacing and alignment
            when {
                fileSize == 2 -> {
                    Log.d(TAG, "onBind: Document file size == 2")

                    // Set layout width to half of screen minus half the margin to prevent overflow
                    layoutParams.width = (screenWidth / 2)
                    layoutParams.height = cardHeight

                    layoutParams.topMargin = 0
                    layoutParams.bottomMargin = 0

                    when (absoluteAdapterPosition) {
                        0 -> {
                            // First item: Touches left screen edge
                            layoutParams.leftMargin = 0
                            layoutParams.rightMargin = (SpaceBetweenRows /2)
                        }
                        1 -> {
                            // Second item: Touches right screen edge
                            layoutParams.leftMargin = (SpaceBetweenRows /2)
                            layoutParams.rightMargin = 0
                        }
                    }

                    // Apply layout params back
                    itemView.layoutParams = layoutParams

                    // Optional: adjust ImageView inside itemView if needed
                    val imageView = itemView.findViewById<ImageView>(R.id.pdfImageView)
                    val imageLayoutParams = imageView.layoutParams as ViewGroup.MarginLayoutParams
                    imageLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                    imageLayoutParams.height = cardHeight
                    imageLayoutParams.topMargin = 0
                    imageLayoutParams.bottomMargin = 0
                    imageView.layoutParams = imageLayoutParams

                    imageView.scaleType = ImageView.ScaleType.FIT_XY
                }

                fileSize == 3 -> {
                    if (absoluteAdapterPosition == 0) {
                        // Full-width item at top
                        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                        layoutParams.height = cardHeight
                        layoutParams.setMargins(0, 0, 0, SpaceBetweenRows) // Space below it
                    } else {
                        // Items 1 and 2 in a row
                        layoutParams.width = (screenWidth / 2)
                        layoutParams.height = cardHeight

                        layoutParams.topMargin = (SpaceBetweenRows)
                        layoutParams.bottomMargin = 0

                        when (absoluteAdapterPosition) {
                            1 -> {
                                // Left item: Touch left screen edge
                                layoutParams.leftMargin = 0
                                layoutParams.rightMargin = (SpaceBetweenRows /2)
                            }
                            2 -> {
                                // Right item: Touch right screen edge
                                layoutParams.leftMargin = (SpaceBetweenRows /2)
                                layoutParams.rightMargin = 0
                            }
                        }
                    }

                    // Apply updated layout
                    itemView.layoutParams = layoutParams

                    // Optional: if using ImageView inside, match height & width
                    val imageView = itemView.findViewById<ImageView>(R.id.pdfImageView)
                    val imageLayoutParams = imageView.layoutParams as ViewGroup.MarginLayoutParams
                    imageLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                    imageLayoutParams.height = cardHeight
                    imageLayoutParams.topMargin = 0
                    imageLayoutParams.bottomMargin = 0
                    imageView.layoutParams = imageLayoutParams

                    imageView.scaleType = ImageView.ScaleType.FIT_XY
                }

                fileSize == 4 -> {
                    layoutParams.width = screenWidth / 2
                    layoutParams.height = cardHeight

                    val isLeftColumn = (absoluteAdapterPosition % 2 == 0)

                    // Horizontal margins: ensure left and right items touch screen edges
                    layoutParams.leftMargin = if (isLeftColumn) 0 else (SpaceBetweenRows /2)
                    layoutParams.rightMargin = if (isLeftColumn) (SpaceBetweenRows /2) else 0

                    // Vertical spacing between rows
                    layoutParams.topMargin = if (absoluteAdapterPosition < 2) 0 else SpaceBetweenRows
                    layoutParams.bottomMargin = 0

                    itemView.layoutParams = layoutParams

                    // Optional: Ensure inner image stretches fully
                    val imageView = itemView.findViewById<ImageView>(R.id.pdfImageView)
                    val imageLayoutParams = imageView.layoutParams as ViewGroup.MarginLayoutParams
                    imageLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                    imageLayoutParams.height = cardHeight
                    imageLayoutParams.topMargin = 0
                    imageLayoutParams.bottomMargin = 0
                    imageView.layoutParams = imageLayoutParams

                    imageView.scaleType = ImageView.ScaleType.FIT_XY
                }

                fileSize > 4 -> {
                    if (absoluteAdapterPosition == 3) {
                        imageView2.visibility = View.VISIBLE
                        countTextView.visibility = View.VISIBLE

                        // Set the "+N" text
                        countTextView.text = "+${fileSize - 4}"
                        countTextView.textSize = 32f
                        countTextView.setTextColor(Color.WHITE)
                        countTextView.setTypeface(null, Typeface.NORMAL)

                        if (absoluteAdapterPosition == 3) {
                            countTextView.visibility = View.VISIBLE
                            countTextView.text = "+${fileSize - 4}"
                            countTextView.textSize = 32f
                            countTextView.setPadding(12, 4, 12, 4) // Optional: padding for better appearance

                            // Create rounded dimmed background
                            val background = GradientDrawable().apply {
                                shape = GradientDrawable.RECTANGLE
                                cornerRadius = 16f // Rounded corners
                                setColor(Color.parseColor("#80000000")) // Semi-transparent black
                            }
                            countTextView.background = background

                        } else {
                            countTextView.visibility = View.GONE
                            countTextView.setPadding(0, 0, 0, 0) // Reset padding if needed
                            countTextView.background = null // Clear background
                        }

                        val marginInDp = 8 // adjust as needed
                        val scale = countTextView.context.resources.displayMetrics.density
                        val marginInPx = (marginInDp * scale + 0.5f).toInt()

                        // Place the countTextView at the bottom-right corner
                        val params = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            gravity = Gravity.BOTTOM or Gravity.END
                            marginEnd = marginInPx
                            bottomMargin = marginInPx
                        }

                        countTextView.layoutParams = params

                        // Optional: center the overlay image fully
                        val imageParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                        )
                        imageView2.layoutParams = imageParams
                    }

                    else if (absoluteAdapterPosition >= 4) {
                        // Hide extra items beyond the fourth
                        itemView.visibility = View.GONE
                        layoutParams.width = 0
                        layoutParams.height = 0
                        itemView.layoutParams = layoutParams
                        return
                    } else {
                        imageView2.visibility = View.GONE
                        countTextView.visibility = View.GONE
                    }

                    // Ensure 2-column layout touches screen edges
                    layoutParams.width = screenWidth / 2
                    layoutParams.height = cardHeight

                    val isLeftColumn = (absoluteAdapterPosition % 2 == 0)

                    // Vertical spacing between rows
                    layoutParams.topMargin = if (absoluteAdapterPosition < 2) 0 else (SpaceBetweenRows)
                    layoutParams.bottomMargin = 0

                    layoutParams.leftMargin = if (isLeftColumn) 0 else (SpaceBetweenRows /2)
                    layoutParams.rightMargin = if (isLeftColumn) (SpaceBetweenRows /2) else 0
                    layoutParams.topMargin = if (absoluteAdapterPosition < 2) 0 else SpaceBetweenRows
                    layoutParams.bottomMargin = 0

                    itemView.layoutParams = layoutParams
                }
            }

            materialCardView.layoutParams = layoutParams
            materialCardView.setContentPadding(0, 0, 0, 0)

            // Ensure the content inside fills the MaterialCardView
            imageView.layoutParams.height = layoutParams.height
            imageView.layoutParams.width = layoutParams.width
        }

        private fun loadImage(url: String) {
            Glide.with(itemView.context)
                .load(url)
                .placeholder(R.drawable.flash21)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView)
        }

        private fun loadVideoThumbnail(url: String) {
            Glide.with(itemView.context)
                .asBitmap()
                .load(url)
                // .placeholder(R.drawable.video_icon)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {

            is FeedImagesOnly -> {
                holder.onBind(feedPost)
            }

            is FeedAudiosOnly -> {
                holder.onBind(feedPost)
            }

            is FeedVideosOnly -> {
                holder.onBind(feedPost)
            }

            is FeedDocumentsOnly -> {
                holder.onBind(feedPost)
            }

            is FeedCombinationOfMultipleFiles -> {
                holder.onBind(feedPost)
            }

        }
    }

    override fun getItemViewType(position: Int): Int {

        if (feedPost.fileTypes.isEmpty() || position >= feedPost.fileTypes.size) {

            Log.e(TAG, "getItemViewType: Invalid position $position, size: ${feedPost.fileTypes.size}")
            return VIEW_TYPE_DEFAULT // Fallback view type for invalid position
        }

        // Collect all unique file types in the post
        val fileTypes = feedPost.fileTypes.map {

            when {
                it.fileType.startsWith("image") -> "image"
                it.fileType.startsWith("video") -> "video"
                it.fileType.startsWith("audio") -> "audio"
                it.fileType.contains("pdf", true) ||
                        it.fileType.contains("doc", true) ||
                        it.fileType.contains("msword", true) -> "document"
                else -> "unknown"
            }
        }.toSet() // Convert to Set to remove duplicates

        // Check if there is a combination of file types
        return when {
            fileTypes.size > 1 -> {
                // More than one file type indicates a combination of multiple file types
                VIEW_TYPE_COMBINATION_OF_MULTIPLE_FILES
            }

            fileTypes.contains("image") -> {
                VIEW_TYPE_IMAGE_FEED
            }

            fileTypes.contains("video") -> {
                VIEW_TYPE_VIDEO_FEED
            }

            fileTypes.contains("audio") -> {
                VIEW_TYPE_AUDIO_FEED
            }

            fileTypes.contains("document") -> {
                VIEW_TYPE_DOCUMENT_FEED
            }

            else -> {
                Log.d(TAG, "getItemViewType: unknown type")
                VIEW_TYPE_DEFAULT // Fallback view type for unknown file type
            }
        }
    }

    override fun getItemCount(): Int {
        return feedPost.files.size
    }


}


private fun ImageView.addView(view: TextView) {}

interface OnMultipleFilesClickListener {
    fun multipleFileClickListener(
        currentIndex: Int,
        files: List<com.uyscuti.social.network.api.response.posts.File>,
        fileIds: List<String>
    )

}