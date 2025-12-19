package com.uyscuti.sharedmodule.adapter.feed.feed.multiple_files

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
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
import com.uyscuti.sharedmodule.R
import com.uyscuti.sharedmodule.utils.waveformseekbar.WaveformSeekBar
import com.uyscuti.social.network.api.response.allFeedRepostsPost.OriginalPost

private const val VIEW_TYPE_IMAGE_FEED = 0
private const val VIEW_TYPE_AUDIO_FEED = 1
private const val VIEW_TYPE_VIDEO_FEED = 2
private const val VIEW_TYPE_DOCUMENT_FEED = 3
private const val VIEW_TYPE_COMBINATION_OF_MULTIPLE_FILES = 4

private const val TAG = "FeedRepostFilesAdapter"

class FeedRepostViewFileAdapter(
    private val feedPost: com.uyscuti.social.network.api.response.posts.OriginalPost,
    ) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    internal var onMultipleFilesClickListener: OnMultipleFilesClickListener? = null


    fun setOnMultipleFilesClickListener(listener: OnMultipleFilesClickListener) {
        onMultipleFilesClickListener = listener
    }

    interface OnMultipleFilesClickListener {
        fun multipleFileClickListener(
            position: Int, files: List<com.uyscuti.social.network.api.response.posts.File>,
            fileIds: List<String>)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {

            VIEW_TYPE_IMAGE_FEED -> {

                val itemView = inflater.inflate(
                    R.layout.feed_multiple_images_view_item, parent, false
                )
                FeedRepostImagesOnly(itemView)
            }

            VIEW_TYPE_AUDIO_FEED -> {

                val itemView = inflater.inflate(
                    R.layout.feed_multiple_audios_view_item, parent, false
                )
                FeedRepostAudiosOnly(itemView)
            }

            VIEW_TYPE_VIDEO_FEED -> {

                val itemView = inflater.inflate(
                    R.layout.feed_multiple_videos_view_item, parent, false
                )
                FeedRepostVideosOnly(itemView)
            }

            VIEW_TYPE_DOCUMENT_FEED -> {

                val itemView =
                    inflater.inflate(
                        R.layout.feed_multiple_documents_view_item, parent, false
                    )
                FeedRepostDocumentsOnly(itemView)
            }

            VIEW_TYPE_COMBINATION_OF_MULTIPLE_FILES -> {

                val itemView =
                    inflater.inflate(
                        R.layout.feed_multiple_combination_of_files_view_item, parent, false
                    )
                FeedRepostCombinationOfMultipleFiles(itemView)
            }


            else -> throw IllegalArgumentException("Invalid view type")
        }
    }


    inner class FeedRepostImagesOnly(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val materialCardView: MaterialCardView =
            itemView.findViewById(R.id.materialCardView)
        private val countTextView: TextView = itemView.findViewById(R.id.textView)
        private val imageView2: ImageView = itemView.findViewById(R.id.imageView2)

        fun Int.dpToPx(context: Context): Int {
            return (this * context.resources.displayMetrics.density).toInt()
        }

        // Adaptive height functions
        private fun getAdaptiveHeights(screenHeight: Int): Pair<Int, Int> {
            val minHeight = (screenHeight * 0.12).toInt() // 12% of screen height
            val maxHeight = (screenHeight * 0.35).toInt() // 35% of screen height
            return Pair(minHeight, maxHeight)
        }

        private fun getConstrainedHeight(desiredHeight: Int, minHeight: Int, maxHeight: Int): Int {
            return desiredHeight.coerceIn(minHeight, maxHeight)
        }

        @SuppressLint("SetTextI18n")
        fun onBind(data: com.uyscuti.social.network.api.response.posts.OriginalPost) {
            Log.d(TAG, "image feed $absoluteAdapterPosition item count $itemCount")

            val context = itemView.context
            val displayMetrics = context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels
            val margin = 4.dpToPx(context)
            val spaceBetweenRows = 4.dpToPx(context) // Spacing between rows

            // Get adaptive height bounds
            val (minHeight, maxHeight) = getAdaptiveHeights(screenHeight)

            val fileIdToFind = data.fileIds[absoluteAdapterPosition]
            val file = data.files.find { it.fileId == fileIdToFind }
            val imageUrl = file?.url ?: data.files.getOrNull(absoluteAdapterPosition)?.url ?: ""

            val fileSize = itemCount
            Log.d(TAG, "image getItemCount: $fileSize $imageUrl")

            // Handle click on the entire item
            itemView.setOnClickListener {
                onMultipleFilesClickListener?.multipleFileClickListener(
                    absoluteAdapterPosition,
                    data.files,
                    data.fileIds as List<String>
                )
            }

            val layoutParams = materialCardView.layoutParams as ViewGroup.MarginLayoutParams

            when {
                fileSize <= 1 -> {
                    // Full-width single image with adaptive height
                    val desiredHeight = (maxHeight * 0.75).toInt()
                    layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                    layoutParams.height = getConstrainedHeight(desiredHeight, minHeight, maxHeight)
                    layoutParams.leftMargin = 0
                    layoutParams.rightMargin = 0
                    layoutParams.topMargin = 0
                    layoutParams.bottomMargin = 0

                    imageView.adjustViewBounds = true
                    Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.imageplaceholder) // Updated to use imageplaceholder from XML
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imageView)
                }

                fileSize == 2 -> {
                    val desiredHeight = (maxHeight * 0.75).toInt()
                    layoutParams.width = screenWidth / 2
                    layoutParams.height = getConstrainedHeight(desiredHeight, minHeight, maxHeight)
                    layoutParams.topMargin = 0 // No top margin for top row items
                    layoutParams.bottomMargin = 0 // No bottom margin for bottom row items

                    // Handle left and right margins for columns
                    val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
                    layoutParams.leftMargin = if (isLeftColumn) 0 else spaceBetweenRows
                    layoutParams.rightMargin = if (isLeftColumn) spaceBetweenRows else 0

                    materialCardView.radius = 8.dpToPx(context).toFloat()

                    Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.imageplaceholder)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imageView)
                }

                fileSize == 3 -> {
                    when (absoluteAdapterPosition) {
                        0 -> {
                            // Full width top item with adaptive height
                            val desiredHeight = (maxHeight * 0.75).toInt()
                            layoutParams.width = screenWidth
                            layoutParams.height = getConstrainedHeight(desiredHeight, minHeight, maxHeight)
                            layoutParams.leftMargin = 0
                            layoutParams.rightMargin = 0
                            layoutParams.topMargin = 0 // No top margin
                            layoutParams.bottomMargin = margin

                            // Handle top and bottom margins
                            layoutParams.topMargin =
                                if (absoluteAdapterPosition < 2) 0 else spaceBetweenRows
                            layoutParams.bottomMargin = if (absoluteAdapterPosition >= 2) 0 else 0

                            if (layoutParams is StaggeredGridLayoutManager.LayoutParams) {
                                layoutParams.isFullSpan = true
                            }
                        }

                        1 -> {
                            // Left half with adaptive height
                            val desiredHeight = (maxHeight * 0.65).toInt()
                            layoutParams.width = screenWidth / 2
                            layoutParams.height = getConstrainedHeight(desiredHeight, minHeight, maxHeight)
                            layoutParams.leftMargin = 0
                            layoutParams.rightMargin = (spaceBetweenRows / 2)
                            layoutParams.topMargin = margin // No top margin
                            layoutParams.bottomMargin = 0 // No bottom margin
                        }

                        2 -> {
                            // Right half with adaptive height
                            val desiredHeight = (maxHeight * 0.65).toInt()
                            layoutParams.width = screenWidth / 2
                            layoutParams.height = getConstrainedHeight(desiredHeight, minHeight, maxHeight)
                            layoutParams.leftMargin = (spaceBetweenRows / 2)
                            layoutParams.rightMargin = 0
                            layoutParams.topMargin = margin // No top margin
                            layoutParams.bottomMargin = 0 // No bottom margin
                        }
                    }

                    materialCardView.radius = 8.dpToPx(context).toFloat()

                    Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.imageplaceholder)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imageView)
                }

                fileSize == 4 -> {
                    // Square items in 2x2 grid with adaptive sizing
                    val desiredHeight = (maxHeight * 0.6).toInt()
                    val adaptiveGridHeight = getConstrainedHeight(desiredHeight, minHeight, maxHeight)

                    // Ensure square aspect ratio by using the smaller dimension
                    val availableWidth = screenWidth / 2
                    val gridSize = minOf(adaptiveGridHeight, availableWidth)

                    layoutParams.width = screenWidth / 2
                    layoutParams.height = gridSize

                    // Handle top and bottom margins
                    layoutParams.topMargin =
                        if (absoluteAdapterPosition < 2) 0 else spaceBetweenRows
                    layoutParams.bottomMargin = if (absoluteAdapterPosition >= 2) 0 else 0

                    // Handle left and right margins for columns
                    val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
                    layoutParams.leftMargin = if (isLeftColumn) 0 else (spaceBetweenRows / 2)
                    layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows / 2) else 0

                    materialCardView.radius = 8.dpToPx(context).toFloat()

                    Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.imageplaceholder)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imageView)
                }

                fileSize > 4 -> {
                    if (absoluteAdapterPosition >= 4) {
                        // Hide extra items beyond the first 4
                        itemView.visibility = View.GONE
                        layoutParams.width = 0
                        layoutParams.height = 0
                        itemView.layoutParams = layoutParams
                        return
                    }

                    // Show 2x2 grid layout for first 4 items with adaptive sizing
                    itemView.visibility = View.VISIBLE

                    // Calculate adaptive grid size
                    val desiredHeight = (maxHeight * 0.6).toInt()
                    val adaptiveGridHeight = getConstrainedHeight(desiredHeight, minHeight, maxHeight)

                    // Ensure square aspect ratio by using the smaller dimension
                    val availableWidth = screenWidth / 2
                    val gridSize = minOf(adaptiveGridHeight, availableWidth)

                    layoutParams.width = screenWidth / 2
                    layoutParams.height = gridSize

                    // Vertical spacing between top and bottom row
                    val isTopRow = absoluteAdapterPosition < 2
                    layoutParams.topMargin = if (isTopRow) 0 else spaceBetweenRows
                    layoutParams.bottomMargin = 0

                    // Horizontal spacing between left and right columns
                    val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
                    layoutParams.leftMargin = if (isLeftColumn) 0 else (spaceBetweenRows)
                    layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows) else 0

                    itemView.layoutParams = layoutParams

                    // Reset CardView padding and margins
                    materialCardView.setContentPadding(0, 0, 0, 0)
                    val cardLayoutParams =
                        materialCardView.layoutParams as ViewGroup.MarginLayoutParams
                    materialCardView.layoutParams = cardLayoutParams

                    // Rounded corners
                    materialCardView.radius = 8.dpToPx(context).toFloat()

                    // Show "+N" overlay on 4th item
                    if (absoluteAdapterPosition == 3) {
                        countTextView.visibility = View.VISIBLE
                        countTextView.text = "+${fileSize - 4}"
                        countTextView.textSize = 32f
                        countTextView.setPadding(
                            12,
                            4,
                            12,
                            4
                        ) // Optional: padding for better appearance

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

                    Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.imageplaceholder)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imageView)
                }
            }

            materialCardView.layoutParams = layoutParams
        }
    }

    inner class FeedRepostAudiosOnly(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val audioDurationTextView: TextView = itemView.findViewById(R.id.audioDuration)
        private val materialCardView: MaterialCardView = itemView.findViewById(R.id.materialCardView)
        private val artworkLayout: LinearLayout = itemView.findViewById(R.id.artworkLayout)
        private val countTextView: TextView = itemView.findViewById(R.id.textView)
        private val imageView2: ImageView = itemView.findViewById(R.id.imageView2)
        private val artworkImageView: ImageView = itemView.findViewById(R.id.artworkImageView)
        private val seekBar: SeekBar = itemView.findViewById(R.id.seekBar)
        private val waveSeekBar: WaveformSeekBar = itemView.findViewById(R.id.waveSeekBar)

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

        @SuppressLint("SetTextI18n")
        fun onBind(data: com.uyscuti.social.network.api.response.posts.OriginalPost) {
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

            // Reset views
            imageView2.visibility = View.GONE
            countTextView.visibility = View.GONE
            itemView.visibility = View.VISIBLE

            // Set duration
            val durationItem = data.duration?.find { it.fileId == fileIdToFind }
            if (!durationItem?.duration.isNullOrEmpty()) {
                audioDurationTextView.text = durationItem?.duration
                audioDurationTextView.visibility = View.VISIBLE
            } else {
                audioDurationTextView.visibility = View.GONE
            }

            // Determine media type
            val fileName = data.fileNames?.find { it.fileId == fileIdToFind }?.fileName ?: ""

            when {
                fileName.endsWith(".mp3", true) ||
                        fileName.endsWith(".m4a", true) ||
                        fileName.endsWith(".aac", true) -> {
                    artworkImageView.visibility = View.VISIBLE
                    seekBar.visibility = View.GONE
                    waveSeekBar.visibility = View.GONE
                    artworkLayout.visibility = View.GONE
                }

                fileName.endsWith(".ogg", true) ||
                        fileName.endsWith(".wav", true) ||
                        fileName.endsWith(".flac", true) ||
                        fileName.endsWith(".amr", true) ||
                        fileName.endsWith(".3gp", true) ||
                        fileName.endsWith(".opus", true) -> {
                    artworkLayout.visibility = View.VISIBLE
                    seekBar.visibility = View.GONE
                    waveSeekBar.visibility = View.GONE
                }

                else -> {
                    artworkImageView.setImageResource(R.drawable.music_placeholder)
                    artworkImageView.visibility = View.VISIBLE
                    artworkLayout.visibility = View.GONE
                    seekBar.visibility = View.GONE
                    waveSeekBar.visibility = View.GONE
                }
            }

            // Layout adjustment based on file count
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
                    layoutParams.leftMargin = if (isLeft) 0 else spaceBetweenRows
                    layoutParams.rightMargin = if (isLeft) spaceBetweenRows else 0
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
                            // Use adaptive height for smaller audio items (slightly smaller than main)
                            layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.65).toInt())
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
                    val preferredHeight = getConstrainedHeight(context, (maxHeight * 0.6).toInt())
                    layoutParams.height = preferredHeight
                    val isLeft = absoluteAdapterPosition % 2 == 0
                    layoutParams.leftMargin = if (isLeft) 0 else (spaceBetweenRows / 2)
                    layoutParams.rightMargin = if (isLeft) (spaceBetweenRows / 2) else 0
                    layoutParams.topMargin = if (absoluteAdapterPosition < 2) 0 else spaceBetweenRows
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
                    val preferredHeight = getConstrainedHeight(context, (maxHeight * 0.6).toInt())
                    layoutParams.height = preferredHeight
                    val isLeft = absoluteAdapterPosition % 2 == 0
                    layoutParams.leftMargin = if (isLeft) 0 else (spaceBetweenRows)
                    layoutParams.rightMargin = if (isLeft) (spaceBetweenRows) else 0
                    layoutParams.topMargin = if (absoluteAdapterPosition < 2) 0 else spaceBetweenRows

                    if (absoluteAdapterPosition == 3) {
                        countTextView.visibility = View.VISIBLE
                        countTextView.text = "+${itemCount - 4}"
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

            materialCardView.layoutParams = layoutParams

            itemView.setOnClickListener {
                onMultipleFilesClickListener?.multipleFileClickListener(
                    absoluteAdapterPosition,
                    data.files,
                    data.fileIds as List<String>
                )
            }
        }
    }

    inner class FeedRepostVideosOnly(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun Int.dpToPx(context: Context): Int {
            return (this * context.resources.displayMetrics.density).toInt()
        }

        private val feedThumbnail: ImageView = itemView.findViewById(R.id.feedThumbnail)
        private val feedVideoDurationTextView: TextView =
            itemView.findViewById(R.id.feedVideoDurationTextView)
        private val cardView: CardView = itemView.findViewById(R.id.cardView)
        private val countTextView: TextView = itemView.findViewById(R.id.countTextView)
        private val imageView2: ImageView = itemView.findViewById(R.id.imageView2)

        // Adaptive height functions
        private fun getAdaptiveHeights(screenHeight: Int): Pair<Int, Int> {
            val minHeight = (screenHeight * 0.12).toInt() // 12% of screen height
            val maxHeight = (screenHeight * 0.35).toInt() // 35% of screen height
            return Pair(minHeight, maxHeight)
        }

        private fun getConstrainedHeight(desiredHeight: Int, minHeight: Int, maxHeight: Int): Int {
            return desiredHeight.coerceIn(minHeight, maxHeight)
        }

        @SuppressLint("SetTextI18n")
        fun onBind(data: com.uyscuti.social.network.api.response.posts.OriginalPost) {
            Log.d(TAG, "onBind: file type doc $absoluteAdapterPosition item count $itemCount")

            val fileIdToFind = data.fileIds[absoluteAdapterPosition]

            itemView.setOnClickListener {
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
            val screenHeight = displayMetrics.heightPixels
            val margin = 4.dpToPx(context)
            val spaceBetweenRows = 4.dpToPx(context)
            val sideMargin = 0 // No side margin to ensure items touch screen edges

            // Get adaptive height bounds
            val (minHeight, maxHeight) = getAdaptiveHeights(screenHeight)

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
            cardView.radius = 8.dpToPx(context).toFloat()

            when {
                fileSize <= 1 -> {
                    val desiredHeight = (maxHeight * 0.75).toInt()
                    layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                    layoutParams.height = getConstrainedHeight(desiredHeight, minHeight, maxHeight)
                    layoutParams.leftMargin = 0
                    layoutParams.rightMargin = 0
                    layoutParams.topMargin = 0
                    layoutParams.bottomMargin = 0
                }

                fileSize == 2 -> {
                    val desiredHeight = (maxHeight * 0.6).toInt()
                    layoutParams.width = screenWidth / 2
                    layoutParams.height = getConstrainedHeight(desiredHeight, minHeight, maxHeight)
                    layoutParams.topMargin = 0
                    layoutParams.bottomMargin = 0

                    // Handle left and right margins for columns
                    val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
                    layoutParams.leftMargin = if (isLeftColumn) 0 else (spaceBetweenRows)
                    layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows) else 0
                }

                fileSize == 3 -> {
                    val spanLayout = cardView.layoutParams as? StaggeredGridLayoutManager.LayoutParams

                    if (absoluteAdapterPosition == 0) {
                        // First item spans the full width
                        val desiredHeight = (maxHeight * 0.6).toInt()
                        spanLayout?.isFullSpan = true
                        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                        layoutParams.height = getConstrainedHeight(desiredHeight, minHeight, maxHeight)
                        layoutParams.leftMargin = 0
                        layoutParams.rightMargin = 0
                        layoutParams.topMargin = 0
                        layoutParams.bottomMargin = margin
                    } else {
                        // Smaller items
                        val desiredHeight = (maxHeight * 0.6).toInt()
                        spanLayout?.isFullSpan = false
                        layoutParams.width = (screenWidth - margin - (2 * sideMargin)) / 2
                        layoutParams.height = getConstrainedHeight(desiredHeight, minHeight, maxHeight)

                        layoutParams.topMargin = 0
                        layoutParams.bottomMargin = 0

                        if (absoluteAdapterPosition == 1) { // Bottom-left item
                            layoutParams.leftMargin = 0
                            layoutParams.rightMargin = (spaceBetweenRows / 2)
                        } else if (absoluteAdapterPosition == 2) { // Bottom-right item
                            layoutParams.leftMargin = (spaceBetweenRows / 2)
                            layoutParams.rightMargin = 0
                        }
                    }

                    if (absoluteAdapterPosition == 2) {
                        // Bottom-right file spans the full width
                        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                        layoutParams.leftMargin = 0
                        layoutParams.rightMargin = 0
                        layoutParams.bottomMargin = 0
                    }

                    cardView.layoutParams = layoutParams
                }

                fileSize == 4 -> {
                    // Calculate adaptive grid size
                    val desiredHeight = (maxHeight * 0.6).toInt()
                    val adaptiveGridHeight = getConstrainedHeight(desiredHeight, minHeight, maxHeight)

                    // Ensure square aspect ratio by using the smaller dimension
                    val availableWidth = screenWidth / 2
                    val gridSize = minOf(adaptiveGridHeight, availableWidth)

                    layoutParams.width = screenWidth / 2
                    layoutParams.height = gridSize

                    // Only add margin between items, not on screen edges
                    val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
                    layoutParams.leftMargin = if (isLeftColumn) 0 else (spaceBetweenRows / 2)
                    layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows / 2) else 0

                    // Add space between the top row and bottom row
                    if (absoluteAdapterPosition < 2) { // Top row
                        layoutParams.topMargin = 0
                        layoutParams.bottomMargin = (margin / 2)
                    } else { // Bottom row
                        layoutParams.topMargin = (margin / 2)
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

                    // Calculate adaptive grid size
                    val desiredHeight = (maxHeight * 0.6).toInt()
                    val adaptiveGridHeight = getConstrainedHeight(desiredHeight, minHeight, maxHeight)

                    // Ensure square aspect ratio by using the smaller dimension
                    val availableWidth = screenWidth / 2
                    val gridSize = minOf(adaptiveGridHeight, availableWidth)

                    layoutParams.width = screenWidth / 2
                    layoutParams.height = gridSize

                    // Horizontal margins: space only between items, not edges
                    val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
                    layoutParams.leftMargin = if (isLeftColumn) 0 else (spaceBetweenRows)
                    layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows) else 0

                    // Vertical spacing between top and bottom row
                    layoutParams.topMargin = if (absoluteAdapterPosition < 2) 0 else (spaceBetweenRows / 2)
                    layoutParams.bottomMargin = if (absoluteAdapterPosition < 2) (spaceBetweenRows / 2) else 0

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
                        countTextView.setPadding(12, 4, 12, 4)

                        // Create rounded dimmed background
                        val background = GradientDrawable().apply {
                            shape = GradientDrawable.RECTANGLE
                            cornerRadius = 16f
                            setColor(Color.parseColor("#80000000"))
                        }
                        countTextView.background = background
                    } else {
                        countTextView.visibility = View.GONE
                        countTextView.setPadding(0, 0, 0, 0)
                        countTextView.background = null
                    }
                }
            }

            cardView.layoutParams = layoutParams
        }
    }

    inner class FeedRepostDocumentsOnly(itemView: View) : RecyclerView.ViewHolder(itemView) {

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

        @SuppressLint("SetTextI18n", "UseKtx")
        fun onBind(data: com.uyscuti.social.network.api.response.posts.OriginalPost) {

            val sideMargin = 2.dpToPx(itemView.context)
            val context = itemView.context // Define context properly

            Log.d(TAG, "onBind: file type doc $absoluteAdapterPosition item count $itemCount")

            itemView.setOnClickListener {
                onMultipleFilesClickListener?.multipleFileClickListener(
                    absoluteAdapterPosition,
                    data.files,
                    data.fileIds as List<String>
                )
            }

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

                when {

                    fileSize == 1 -> {

                        Log.d(TAG, "bind: file size 1")

                        val topMargin = (-8).dpToPx(context)
                        // Use 85% of max height for single document
                        val adaptiveHeight = getConstrainedHeight(context, (maxHeight * 0.85).toInt())

                        val containerParams = documentContainer.layoutParams as ViewGroup.MarginLayoutParams
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

                        // Create a new ImageView for single document view
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

                        // Add image view to the center container
                        centerContainer.addView(singleImageView)

                        // Create an overlay layout for fileTypeIcon
                        val overlayLayout = FrameLayout(context).apply {
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }

                        // Configure fileTypeIcon
                        (fileTypeIcon.parent as? ViewGroup)?.removeView(fileTypeIcon)
                        fileTypeIcon.layoutParams = FrameLayout.LayoutParams(
                            20.dpToPx(context),
                            20.dpToPx(context),
                            Gravity.TOP or Gravity.START
                        ).apply {
                            setMargins(8.dpToPx(context), 8.dpToPx(context), 0, 0)
                        }

                        // Add fileTypeIcon to the overlay
                        overlayLayout.addView(fileTypeIcon)

                        // Add the overlay on top of the center container
                        centerContainer.addView(overlayLayout)

                        // Add complete center container to documentContainer
                        documentContainer.addView(centerContainer)

                        // Filter and load thumbnail into singleImageView
                        val thumbnails = data.thumbnail.filter { thumb ->
                            data.fileIds.contains(thumb.fileId)
                        }

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
                        val adaptiveHeight = getConstrainedHeight(context, (maxHeight * 0.7).toInt())

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
                        val adaptiveHeight = getConstrainedHeight(context, (maxHeight * 0.7).toInt())

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

                                    // Create the "+N" TextView
                                    val textView = TextView(context).apply {
                                        text = plusCountText
                                        setTextColor(Color.WHITE)
                                        textSize = 32f
                                        gravity = Gravity.CENTER
                                    }

                                    // Add TextView to the container
                                    overlayContainer.addView(textView)

                                    // Add container to the image wrapper
                                    imageWrapper.addView(overlayContainer)

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

    inner class FeedRepostCombinationOfMultipleFiles(itemView: View) : RecyclerView.ViewHolder(itemView) {

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

        @SuppressLint("SetTextI18n")


        fun onBind(data: com.uyscuti.social.network.api.response.posts.OriginalPost) {

            val context = itemView.context
            val displayMetrics = context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val SpaceBetweenRows = 4.dpToPx(context) // Space between cards
            val cardHeight = 300.dpToPx(context) // Fixing the height to 300dp

            val fileIdToFind = data.fileIds[absoluteAdapterPosition]
            val file = data.files.find { it.fileId == fileIdToFind }
            val fileUrl = file?.url ?: data.files.getOrNull(absoluteAdapterPosition)?.url ?: ""
            val mimeType = data.fileTypes.getOrNull(absoluteAdapterPosition)?.fileType ?: ""


            val durationItem = data.duration?.find { it.fileId == fileIdToFind }

            feedVideoDurationTextView.text = durationItem?.duration


            val fileSize = data.files.size

            itemView.setOnClickListener {
                onMultipleFilesClickListener?.multipleFileClickListener(
                    absoluteAdapterPosition,
                    data.files,
                    data.fileIds as List<String>
                )
            }

            val layoutParams = materialCardView.layoutParams as ViewGroup.MarginLayoutParams

            // Set default visibility for media controls
            playButton.visibility = View.GONE
            feedVideoImageView.visibility = View.GONE
            feedVideoDurationTextView.visibility = View.VISIBLE
            imageView2.visibility = View.GONE
            countTextView.visibility = View.GONE

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

                // Rest of your code remains the same...
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

            is FeedRepostImagesOnly -> {
                holder.onBind(feedPost)
            }

            is FeedRepostAudiosOnly -> {
                holder.onBind(feedPost)
            }

            is FeedRepostVideosOnly -> {
                holder.onBind(feedPost)
            }

            is FeedRepostDocumentsOnly -> {
                holder.onBind(feedPost)
            }

            is FeedRepostCombinationOfMultipleFiles -> {
                holder.onBind(feedPost)
            }

        }
    }

    override fun getItemViewType(position: Int): Int {


        return when (feedPost.fileTypes.get(position).fileType) {

            "image" -> {
                VIEW_TYPE_IMAGE_FEED
            }

            "audio" -> {
                VIEW_TYPE_AUDIO_FEED
            }

            "video" -> {
                VIEW_TYPE_VIDEO_FEED
            }

            "doc", "pdf" -> {
                VIEW_TYPE_DOCUMENT_FEED
            }

            "image", "audio", "video", "doc", "pdf" -> {
                VIEW_TYPE_COMBINATION_OF_MULTIPLE_FILES
            }

            else -> {

                Log.d(TAG, "getItemViewType: unknown type")
            }

        }
    }


    override fun getItemCount(): Int {
        return feedPost.files.size
    }


}


