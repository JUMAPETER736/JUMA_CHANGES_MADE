

package com.uyscuti.social.circuit.adapter.feed.multiple_files
import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.graphics.Color
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
import com.uyscuti.social.circuit.utils.waveformseekbar.WaveformSeekBar
import com.uyscuti.social.circuit.R
import android.graphics.Bitmap
import android.graphics.Outline
import android.os.Bundle
import android.view.ViewOutlineProvider
import android.widget.ImageButton
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.feedRepost.PostItem
import com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.feedRepost.Tapped_Files_In_The_Container_View_Fragment
import androidx.core.graphics.toColorInt
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.imageview.ShapeableImageView
import com.uyscuti.social.network.api.response.posts.File
import com.uyscuti.social.network.api.response.posts.AuthorX
import com.uyscuti.social.network.api.response.posts.FileType
import com.uyscuti.social.network.api.response.posts.OriginalPost
import com.uyscuti.social.network.api.response.posts.Post
import com.uyscuti.social.network.api.response.posts.ThumbnailX
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


private const val VIEW_TYPE_IMAGE_FEED = 0
private const val VIEW_TYPE_AUDIO_FEED = 1
private const val VIEW_TYPE_VIDEO_FEED = 2
private const val VIEW_TYPE_DOCUMENT_FEED = 3
private const val VIEW_TYPE_DEFAULT = 4
private const val VIEW_TYPE_COMBINATION_OF_MULTIPLE_FILES = 5
private const val VIEW_TYPE_VOICE_NOTE = 6

private const val TAG = "FeedMixedFilesViewAdapter"


class FeedMixedFilesViewAdapter(

    private val feedPost:com.uyscuti.social.network.api.response.posts.Post) :

    RecyclerView.Adapter<RecyclerView.ViewHolder>()  {
    private var onMultipleFilesClickListener: OnMultipleFilesClickListener? = null

    fun setOnMultipleFilesClickListener(l: OnMultipleFilesClickListener?) {
        onMultipleFilesClickListener = l
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {

            VIEW_TYPE_IMAGE_FEED -> {
                val itemView = inflater.inflate(
                    R.layout.feed_multiple_images_only_view_item, parent, false)
                FeedImagesOnly(itemView)
            }

            VIEW_TYPE_VOICE_NOTE -> {
                val itemView = inflater.inflate(
                    R.layout.feed_multiple_audios_only_view_item, parent, false)
                FeedAudiosOnly(itemView)
            }

            VIEW_TYPE_AUDIO_FEED -> {
                val itemView = inflater.inflate(
                    R.layout.feed_multiple_audios_only_view_item, parent, false)
                FeedAudiosOnly(itemView)
            }

            VIEW_TYPE_VIDEO_FEED -> {
                val itemView = inflater.inflate(
                    R.layout.feed_multiple_videos_only_view_item, parent, false)
                FeedVideosOnly(itemView)
            }

            VIEW_TYPE_DOCUMENT_FEED -> {
                val itemView = inflater.inflate(
                    R.layout.feed_multiple_documents_only_view_item, parent, false)
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
            val minHeight = (screenHeight * 0.12).toInt() // 12% of screen height
            val maxHeight = (screenHeight * 0.35).toInt()

            return Pair(minHeight, maxHeight)
        }

        // Helper function to get a constrained height within min/max bounds
        private fun getConstrainedHeight(context: Context, preferredHeight: Int): Int {
            val (minHeight, maxHeight) = getAdaptiveHeights(context)
            return preferredHeight.coerceIn(minHeight, maxHeight)
        }

        // Helper function to configure MaterialCardView with proper corner radius
        private fun setupCardViewCorners(context: Context) {
            val cornerRadius = 8.dpToPx(context).toFloat()

            // Set the corner radius
            materialCardView.radius = cornerRadius

            // Ensure card is clipped to bounds to show rounded corners
            materialCardView.clipToOutline = true
            materialCardView.clipChildren = true

            // Remove any elevation that might interfere with corners
            materialCardView.cardElevation = 0f
            materialCardView.maxCardElevation = 0f

            // Set stroke width to 0 to avoid border issues
            materialCardView.strokeWidth = 0

            // Ensure content padding doesn't interfere
            materialCardView.setContentPadding(0, 0, 0, 0)

            // Set background color
            materialCardView.setCardBackgroundColor(Color.WHITE)

            // Configure ImageView to respect the card's rounded corners
            imageView.clipToOutline = true
            imageView.outlineProvider = ViewOutlineProvider.BACKGROUND
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
            files: ArrayList<File>,
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

            // Setup card view corners first
            setupCardViewCorners(context)

            itemView.setBackgroundColor(Color.TRANSPARENT)

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

                                // Limit image width to screen width and calculate height based on actual aspect ratio
                                val displayWidth = screenWidth
                                val displayHeight = (displayWidth * aspectRatio).toInt()

                                val layoutParams = imageView.layoutParams as ViewGroup.MarginLayoutParams
                                layoutParams.width = displayWidth
                                layoutParams.height = displayHeight
                                layoutParams.setMargins(0, 0, 0, 0)

                                imageView.layoutParams = layoutParams
                                imageView.setImageBitmap(resource)
                                imageView.adjustViewBounds = true

                                // Automatically choose CENTER_CROP or FIT_CENTER based on image shape
                                imageView.scaleType = if (aspectRatio > 1.2f) {
                                    // Portrait image
                                    ImageView.ScaleType.CENTER_CROP
                                } else {
                                    // Landscape image or near-square
                                    ImageView.ScaleType.CENTER_CROP
                                }
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                                // Optionally handle placeholder cleanup
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
                    layoutParams.leftMargin = if (isLeftColumn) 0 else spaceBetweenRows /2
                    layoutParams.rightMargin = if (isLeftColumn) spaceBetweenRows /2 else 0
                }

                fileSize == 3 -> {
                    when (absoluteAdapterPosition) {
                        0 -> {
                            // First image takes left half with FULL height
                            layoutParams.width = screenWidth / 2
                            layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.75).toInt())
                            layoutParams.leftMargin = 0
                            layoutParams.rightMargin = (spaceBetweenRows/2)
                            layoutParams.topMargin = 0
                            layoutParams.bottomMargin = 0

                            if (layoutParams is StaggeredGridLayoutManager.LayoutParams) {
                                layoutParams.isFullSpan = false
                            }
                        }

                        1, 2 -> {
                            // Second and third images stack vertically on the right side
                            layoutParams.width = screenWidth / 2
                            // Each takes half the FULL height (so together they equal position 0's height)
                            layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.75).toInt()) /2

                            layoutParams.leftMargin = (spaceBetweenRows/2)
                            layoutParams.rightMargin = 0

                            if (absoluteAdapterPosition == 1) {
                                // Top right image
                                layoutParams.topMargin = 0
                                layoutParams.bottomMargin = (spaceBetweenRows/2)
                            } else {
                                // Bottom right image (position 2)
                                layoutParams.topMargin = (spaceBetweenRows/2)
                                layoutParams.bottomMargin = 0
                            }
                        }
                    }
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
                }

                fileSize == 5 -> {
                    if (absoluteAdapterPosition >= 4) {
                        // Hide anything beyond the first 4 items
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

                    // 🟢 Default spacing
                    val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
                    layoutParams.leftMargin = if (isLeftColumn) 0 else (spaceBetweenRows /2)
                    layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows /2) else 0
                    layoutParams.topMargin = if (absoluteAdapterPosition < 2) 0 else spaceBetweenRows / 2
                    layoutParams.bottomMargin = if (absoluteAdapterPosition < 2) spaceBetweenRows / 2 else 0



                    itemView.layoutParams = layoutParams

                    // 🟣 Show +X overlay only on 4th item
                    if (absoluteAdapterPosition == 3) {
                        countTextView.visibility = View.VISIBLE
                        countTextView.text = "+${fileSize - 4}"
                        countTextView.textSize = 32f
                        countTextView.setPadding(12, 4, 12, 4)
                        countTextView.background = GradientDrawable().apply {
                            shape = GradientDrawable.RECTANGLE
                            cornerRadius = 16f
                            setColor("#80000000".toColorInt())
                        }
                    } else {
                        countTextView.visibility = View.GONE
                        countTextView.setPadding(0, 0, 0, 0)
                        countTextView.background = null
                    }
                }

                fileSize > 4 -> {
                    if (absoluteAdapterPosition >= 4) {
                        // Hide anything beyond the first 4 items
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

                    // 🟢 Default spacing
                    val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
                    layoutParams.leftMargin = if (isLeftColumn) 0 else spaceBetweenRows
                    layoutParams.rightMargin = if (isLeftColumn) spaceBetweenRows else 0
                    layoutParams.topMargin = if (absoluteAdapterPosition < 2) 0 else spaceBetweenRows / 2
                    layoutParams.bottomMargin = if (absoluteAdapterPosition < 2) spaceBetweenRows / 2 else 0



                    itemView.layoutParams = layoutParams

                    // 🟣 Show +X overlay only on 4th item
                    if (absoluteAdapterPosition == 3) {
                        countTextView.visibility = View.VISIBLE
                        countTextView.text = "+${fileSize - 4}"
                        countTextView.textSize = 32f
                        countTextView.setPadding(12, 4, 12, 4)
                        countTextView.background = GradientDrawable().apply {
                            shape = GradientDrawable.RECTANGLE
                            cornerRadius = 16f
                            setColor("#80000000".toColorInt())
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
        private val artworkVn: ShapeableImageView = itemView.findViewById(R.id.artworkVn)
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
            files: ArrayList<File>,
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

        fun onBind(data: Post) {

            itemView.background = null
            // Set the rounded background to itemView

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

            // Hide seek bars by default
            seekBar.visibility = View.GONE
            waveSeekBar.visibility = View.GONE

            val durationItem = data.duration?.find { it.fileId == fileIdToFind }
            audioDurationTextView.text = durationItem?.duration ?: ""
            audioDurationTextView.visibility = if (!durationItem?.duration.isNullOrEmpty()) View.VISIBLE else View.GONE

            val fileName = data.fileNames?.find { it.fileId == fileIdToFind }?.fileName ?: ""

            // Handle different audio formats - UPDATE CARDVIEW BACKGROUND COLORS HERE
            // Replace the entire when block (lines handling audio formats)
            when {
                fileName.endsWith(".mp3", true) ||
                        fileName.endsWith(".wav", true) -> {
                    // Show artwork for common audio formats with music placeholder
                    materialCardView.setCardBackgroundColor(Color.WHITE)

                    // Show main artwork image with music icon
                    artworkImageView.setImageResource(R.drawable.music_icon)
                    artworkImageView.visibility = View.VISIBLE
                    artworkImageView.scaleType = ImageView.ScaleType.CENTER_CROP

                    // Hide the artworkLayout (mic icon layout)
                    artworkLayout.visibility = View.GONE
                }

                fileName.endsWith(".ogg", true) ||
                        fileName.endsWith(".aac", true) ||
                        fileName.endsWith(".m4a", true) ||
                        fileName.endsWith(".flac", true) ||
                        fileName.endsWith(".amr", true) ||
                        fileName.endsWith(".3gp", true) ||
                        fileName.endsWith(".opus", true) -> {
                    // Set gray background for voice note formats
                    materialCardView.setCardBackgroundColor(Color.parseColor("#616161"))

                    // Hide the main artwork image
                    artworkImageView.visibility = View.GONE

                    val artworkLayoutWrapper = itemView.findViewById<com.google.android.material.card.MaterialCardView>(R.id.artworkLayoutWrapper)
                    artworkLayoutWrapper?.visibility = View.VISIBLE
                    artworkLayoutWrapper?.setCardBackgroundColor(Color.parseColor("#616161"))

                    artworkLayout.visibility = View.VISIBLE
                    artworkLayout.setBackgroundColor(Color.parseColor("#616161"))

                    // Show microphone icon for voice notes
                    artworkVn.setImageResource(R.drawable.ic_audio_white_icon)
                    artworkVn.visibility = View.VISIBLE

                    val layoutParams = artworkVn.layoutParams
                    if (layoutParams != null) {
                        layoutParams.width = 120.dpToPx(context)
                        layoutParams.height = 270.dpToPx(context)
                        artworkVn.layoutParams = layoutParams
                    }
                }

                else -> {
                    // Set gray background for unknown audio formats
                    materialCardView.setCardBackgroundColor(Color.parseColor("#616161"))

                    // Hide the main artwork image
                    artworkImageView.visibility = View.GONE

                    val artworkLayoutWrapper = itemView.findViewById<com.google.android.material.card.MaterialCardView>(R.id.artworkLayoutWrapper)
                    artworkLayoutWrapper?.visibility = View.VISIBLE
                    artworkLayoutWrapper?.setCardBackgroundColor(Color.parseColor("#616161"))

                    artworkLayout.visibility = View.VISIBLE
                    artworkLayout.setBackgroundColor(Color.parseColor("#616161"))

                    // Show generic audio icon for unknown audio types
                    artworkVn.setImageResource(R.drawable.ic_audio_white_icon)
                    artworkVn.visibility = View.VISIBLE

                    val layoutParams = artworkVn.layoutParams
                    if (layoutParams != null) {
                        layoutParams.width = 120.dpToPx(context)
                        layoutParams.height = 270.dpToPx(context)
                        artworkVn.layoutParams = layoutParams
                    }
                }
            }

            // Find the audioDurationLayout and keep it visible
            val audioDurationLayout = itemView.findViewById<LinearLayout>(R.id.audioDurationLayout)
            audioDurationLayout?.visibility = View.VISIBLE

            // Layout configuration based on item count
            val itemCount = data.files.size

            when {
                itemCount <= 1 -> {
                    layoutParams.width = screenWidth
                    // Use adaptive height instead of fixed 300dp
                    layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.75).toInt())
                }
                itemCount == 2 -> {
                    layoutParams.width = screenWidth / 2
                    // Use adaptive height instead of fixed 300dp
                    layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.75).toInt())
                    val isLeft = absoluteAdapterPosition % 2 == 0
                    layoutParams.leftMargin = if (isLeft) 0 else (spaceBetweenRows /2)
                    layoutParams.rightMargin = if (isLeft) (spaceBetweenRows /2) else 0
                }

                itemCount == 3 -> {
                    when (absoluteAdapterPosition) {
                        0 -> {
                            // First audio item takes left half with full height
                            layoutParams.width = screenWidth / 2

                            // Calculate the total height that the right side will occupy
                            val singleAudioHeight = 135.dpToPx(context)
                            val totalRightSideHeight = (singleAudioHeight * 2) + (spaceBetweenRows / 2)
                            layoutParams.height = totalRightSideHeight

                            layoutParams.leftMargin = 0
                            layoutParams.rightMargin = (spaceBetweenRows/2)
                            layoutParams.topMargin = 0
                            layoutParams.bottomMargin = 0

                            if (layoutParams is StaggeredGridLayoutManager.LayoutParams) {
                                layoutParams.isFullSpan = false
                            }
                        }

                        1, 2 -> {
                            // Second and third audio items stack vertically on the right side
                            layoutParams.width = screenWidth / 2

                            // Each takes half the total height (matching position 0's height)
                            val singleAudioHeight = 135.dpToPx(context)
                            val totalHeight = (singleAudioHeight * 2) + (spaceBetweenRows / 2)
                            layoutParams.height = (totalHeight - (spaceBetweenRows / 2)) / 2

                            layoutParams.leftMargin = (spaceBetweenRows/2)
                            layoutParams.rightMargin = 0

                            if (absoluteAdapterPosition == 1) {
                                // Top right audio item
                                layoutParams.topMargin = 0
                                layoutParams.bottomMargin = (spaceBetweenRows/2)
                            } else {
                                // Bottom right audio item (position 2)
                                layoutParams.topMargin = (spaceBetweenRows/2)
                                layoutParams.bottomMargin = 0
                            }
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

                itemCount == 5 -> {
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
                    layoutParams.leftMargin = if (isLeft) 0 else (spaceBetweenRows /2)
                    layoutParams.rightMargin = if (isLeft) (spaceBetweenRows /2) else 0
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
                    layoutParams.leftMargin = if (isLeft) 0 else (spaceBetweenRows)
                    layoutParams.rightMargin = if (isLeft) (spaceBetweenRows) else 0
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

        // Extension function to convert dp to px (add this if not already present)
        fun Float.dpToPx(context: Context): Int {
            return (this * context.resources.displayMetrics.density).toInt()
        }



    }

    inner class FeedVideosOnly(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun Int.dpToPx(context: Context): Int {
            return (this * context.resources.displayMetrics.density).toInt()
        }

        private var preloadJob: Job? = null
        private val feedThumbnail: ImageView = itemView.findViewById(R.id.feedThumbnail)
        private val feedVideoDurationTextView: TextView =
            itemView.findViewById(R.id.feedVideoDurationTextView)
        private val cardView: CardView = itemView.findViewById(R.id.cardView)
        private val countTextView: TextView = itemView.findViewById(R.id.countTextView)
        private val imageView2: ImageView = itemView.findViewById(R.id.imageView2)

        // Add this method to preload video
        private fun preloadVideo(context: Context, videoUrl: String) {
            preloadJob?.cancel() // Cancel any existing preload job

            preloadJob = CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Preload with Glide's video frame loading
                    Glide.with(context)
                        .asFile()
                        .load(videoUrl)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .preload()

                    Log.d(TAG, "Preloaded video: $videoUrl")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to preload video: ${e.message}")
                }
            }
        }

        // Add this method to preload multiple videos
        private fun preloadMultipleVideos(context: Context, files: ArrayList<File>, startIndex: Int) {
            CoroutineScope(Dispatchers.IO).launch {
                // Preload current video and next 2 videos
                val videosToPreload = minOf(3, files.size - startIndex)

                for (i in 0 until videosToPreload) {
                    val index = startIndex + i
                    if (index < files.size) {
                        val file = files[index]
                        try {
                            Glide.with(context)
                                .asFile()
                                .load(file.url)
                                .diskCacheStrategy(DiskCacheStrategy.DATA)
                                .preload()

                            Log.d(TAG, "Preloaded video at index $index: ${file.url}")

                            // Small delay between preloads to avoid overwhelming
                            delay(100)
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to preload video at index $index: ${e.message}")
                        }
                    }
                }
            }
        }

        // Helper function to get adaptive heights based on screen size
        private fun getAdaptiveHeights(context: Context): Pair<Int, Int> {
            val displayMetrics = context.resources.displayMetrics
            val screenHeight = displayMetrics.heightPixels


            val minHeight = (screenHeight * 0.12).toInt() // 12% of screen height
            val maxHeight = (screenHeight * 0.35).toInt()
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
            files: ArrayList<File>,
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
            Log.d(TAG, "onBind: file type Video $absoluteAdapterPosition item count $itemCount")

            cardView.setCardBackgroundColor(Color.WHITE)
            itemView.setBackgroundColor(Color.TRANSPARENT)
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
            cardView.radius = 12.dpToPx(context).toFloat()
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
                    layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.75).toInt())
                    layoutParams.topMargin = 0 // No top margin for top row items
                    layoutParams.bottomMargin = 0 // No bottom margin for bottom row items

                    // Handle left and right margins for columns
                    val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
                    layoutParams.leftMargin = if (isLeftColumn) 0 else (spaceBetweenRows /2)
                    layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows/2) else 0
                }

                fileSize == 3 -> {
                    val spanLayout = cardView.layoutParams as? StaggeredGridLayoutManager.LayoutParams

                    when (absoluteAdapterPosition) {
                        0 -> {
                            // First video takes left half with full calculated height
                            spanLayout?.isFullSpan = false
                            layoutParams.width = screenWidth / 2

                            // Calculate the total height that will match the right side combined
                            val baseVideoHeight = getConstrainedHeight(context, (maxHeight * 0.65).toInt())
                            val rightSideItemHeight = baseVideoHeight / 2
                            val totalRightSideHeight = (rightSideItemHeight * 2) + (spaceBetweenRows / 2)
                            layoutParams.height = totalRightSideHeight

                            layoutParams.leftMargin = 0
                            layoutParams.rightMargin = (spaceBetweenRows / 2)
                            layoutParams.topMargin = 0
                            layoutParams.bottomMargin = 0
                        }

                        1, 2 -> {
                            // Second and third videos stack vertically on the right side
                            spanLayout?.isFullSpan = false
                            layoutParams.width = screenWidth / 2

                            // Each takes half the height to match position 0's total height
                            val baseVideoHeight = getConstrainedHeight(context, (maxHeight * 0.65).toInt())
                            val totalHeight = baseVideoHeight + (spaceBetweenRows / 2)
                            layoutParams.height = (totalHeight - (spaceBetweenRows / 2)) / 2

                            layoutParams.leftMargin = (spaceBetweenRows / 2)
                            layoutParams.rightMargin = 0

                            if (absoluteAdapterPosition == 1) {
                                // Top-right video
                                layoutParams.topMargin = 0
                                layoutParams.bottomMargin = (spaceBetweenRows / 2)
                            } else {
                                // Bottom-right video (position 2)
                                layoutParams.topMargin = (spaceBetweenRows / 2)
                                layoutParams.bottomMargin = 0
                            }
                        }
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
                        countTextView.setPadding(12, 4, 12, 4)

                        // Create rounded dimmed background
                        val background = GradientDrawable().apply {
                            shape = GradientDrawable.RECTANGLE
                            cornerRadius = 16f // Rounded corners
                            setColor(Color.parseColor("#80000000"))
                        }
                        countTextView.background = background
                    } else {
                        countTextView.visibility = View.GONE
                        countTextView.setPadding(0, 0, 0, 0)
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

        private val tag = "FeedDocument"
        private val pdfImageView: ImageView = itemView.findViewById(R.id.pdfImageView)
        private val documentContainer: CardView = itemView.findViewById(R.id.documentContainer)
        private val fileTypeIcon: ImageView = itemView.findViewById(R.id.fileTypeIcon)
        private val overlayImageView: ImageView = itemView.findViewById(R.id.imageView2)
        private val countTextView: TextView = itemView.findViewById(R.id.countTextView)

        private fun getAdaptiveHeights(context: Context): Pair<Int, Int> {
            val displayMetrics = context.resources.displayMetrics
            val screenHeight = displayMetrics.heightPixels
            val minHeight = (screenHeight * 0.15).toInt()
            val maxHeight = (screenHeight * 0.38).toInt()
            return Pair(minHeight, maxHeight)
        }

        private fun getConstrainedHeight(context: Context, targetHeight: Int): Int {
            val (minHeight, maxHeight) = getAdaptiveHeights(context)
            return targetHeight.coerceIn(minHeight, maxHeight)
        }

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
            files: List<File>,
            fileIds: List<String>
        ) {
            val activity = getActivityFromContext(context)
            if (activity != null) {
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
                Log.d(
                    tag, "Navigated to Tapped_Files_In_The_Container_View with ${files.size} " +
                            "files, starting at index $currentIndex"
                )
            } else {
                Log.e(tag, "Activity is null, cannot navigate to fragment")
            }
        }

        private fun showPlusMoreOverlay(count: Int) {
            overlayImageView.visibility = View.VISIBLE
            countTextView.visibility = View.VISIBLE
            countTextView.text = "+$count"

            // Style the count text
            countTextView.textSize = 32f
            countTextView.setPadding(
                12.dpToPx(itemView.context),
                4.dpToPx(itemView.context),
                12.dpToPx(itemView.context),
                4.dpToPx(itemView.context)
            )
            countTextView.setTextColor(Color.WHITE)
            countTextView.gravity = Gravity.CENTER
            countTextView.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16f
                setColor("#80000000".toColorInt())
            }

            Log.d(tag, "Showing overlay with count: +$count")
        }

        private fun hidePlusMoreOverlay() {
            overlayImageView.visibility = View.GONE
            countTextView.visibility = View.GONE
        }

        fun onBind(data: Post) {
            var actualFiles: List<File> = data.files ?: emptyList()
            var actualFileIds: List<String> = data.fileIds as? List<String> ?: emptyList()
            var actualFileTypes: List<FileType> = data.fileTypes ?: emptyList()
            var actualThumbnails: List<ThumbnailX> = data.thumbnail ?: emptyList()

            if (actualFiles.isEmpty() && data.originalPost != null && data.originalPost.isNotEmpty()) {
                val originalPost: OriginalPost = data.originalPost[0]
                actualFiles = originalPost.files ?: emptyList()
                actualFileIds = (originalPost.fileIds ?: emptyList()) as List<String>
                actualFileTypes = originalPost.fileTypes ?: emptyList()
                actualThumbnails = originalPost.thumbnail ?: emptyList()
            }

            // Reset view state
            itemView.visibility = View.VISIBLE
            pdfImageView.setImageDrawable(null)
            fileTypeIcon.setImageDrawable(null)
            fileTypeIcon.visibility = View.GONE
            hidePlusMoreOverlay()

            val position = absoluteAdapterPosition
            Log.d(tag, "onBind: position=$position, fileSize=${actualFiles.size}")

            if (actualFiles.isEmpty() || actualFileIds.isEmpty()) {
                Log.e(tag, "No files or fileIds available")
                itemView.visibility = View.GONE
                itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
                return
            }

            if (position < 0 || position >= actualFileIds.size) {
                Log.e(tag, "Invalid absoluteAdapterPosition: $position")
                itemView.visibility = View.GONE
                itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
                return
            }

            val context = itemView.context
            val sideMargin = 2.dpToPx(context)
            val fileIdToFind = actualFileIds[position]
            val documentType = actualFileTypes.find { it.fileId == fileIdToFind }
            val fileSize = actualFiles.size
            val (minHeight, maxHeight) = getAdaptiveHeights(context)

            // Set file type icon
            val fallbackDrawable = when (documentType?.fileType) {
                "pdf" -> R.drawable.pdf_icon
                "doc", "docx" -> R.drawable.word_icon
                "ppt", "pptx" -> R.drawable.powerpoint_icon
                "xls", "xlsx" -> R.drawable.excel_icon
                "txt" -> R.drawable.text_icon
                "rtf" -> R.drawable.text_icon
                "odt" -> R.drawable.word_icon
                "csv" -> R.drawable.excel_icon
                else -> R.drawable.text_icon
            }

            fileTypeIcon.setImageResource(fallbackDrawable)
            fileTypeIcon.visibility = View.VISIBLE

            // Load thumbnail
            val thumbnail = actualThumbnails.find { it.fileId == fileIdToFind }
            pdfImageView.visibility = View.VISIBLE
            if (thumbnail != null && !thumbnail.thumbnailUrl.isNullOrEmpty()) {
                Glide.with(context)
                    .load(thumbnail.thumbnailUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(fallbackDrawable)
                    .error(fallbackDrawable)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: com.bumptech.glide.request.target.Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            Log.e(tag, "Glide failed to load thumbnail for fileId=$fileIdToFind")
                            pdfImageView.setImageResource(fallbackDrawable)
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable>,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.d(tag, "Glide loaded thumbnail for fileId=$fileIdToFind")
                            return false
                        }
                    })
                    .into(pdfImageView)
            } else {
                Log.w(tag, "No thumbnail found for fileId=$fileIdToFind")
                pdfImageView.setImageResource(fallbackDrawable)
            }

            // Set click listeners
            val clickListener = View.OnClickListener {
                navigateToTappedFilesFragment(context, position, actualFiles, actualFileIds)
            }
            itemView.setOnClickListener(clickListener)
            pdfImageView.setOnClickListener(clickListener)
            documentContainer.setOnClickListener(clickListener)
            fileTypeIcon.setOnClickListener(clickListener)

            // Apply layout based on file size
            when {
                fileSize == 1 -> {
                    val adaptiveHeight = getConstrainedHeight(context, (maxHeight * 0.85).toInt())
                    val containerParams = documentContainer.layoutParams as ViewGroup.MarginLayoutParams
                    containerParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                    containerParams.height = adaptiveHeight
                    containerParams.setMargins(0, (-8).dpToPx(context), 0, 0)
                    documentContainer.layoutParams = containerParams

                    val imageLayoutParams = pdfImageView.layoutParams
                    imageLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                    imageLayoutParams.height = adaptiveHeight
                    pdfImageView.layoutParams = imageLayoutParams
                    pdfImageView.scaleType = ImageView.ScaleType.FIT_CENTER
                }

                fileSize == 2 -> {
                    val adaptiveHeight = getConstrainedHeight(context, (maxHeight * 0.70).toInt())
                    val containerParams = documentContainer.layoutParams as ViewGroup.MarginLayoutParams
                    containerParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                    containerParams.height = adaptiveHeight
                    when (position) {
                        0 -> containerParams.setMargins(0, 0, sideMargin, 0)
                        1 -> containerParams.setMargins(sideMargin, 0, 0, 0)
                    }
                    documentContainer.layoutParams = containerParams

                    val imageLayoutParams = pdfImageView.layoutParams
                    imageLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                    imageLayoutParams.height = adaptiveHeight
                    pdfImageView.layoutParams = imageLayoutParams
                    pdfImageView.scaleType = ImageView.ScaleType.FIT_XY
                }

                fileSize >= 3 -> {
                    // Hide items at position 2 and beyond
                    if (position >= 2) {
                        Log.d(tag, "Hiding item at position $position")
                        itemView.visibility = View.GONE
                        itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
                        return
                    }

                    val adaptiveHeight = getConstrainedHeight(context, (maxHeight * 0.70).toInt())
                    val containerParams = documentContainer.layoutParams as ViewGroup.MarginLayoutParams
                    containerParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                    containerParams.height = adaptiveHeight

                    when (position) {
                        0 -> {
                            containerParams.setMargins(0, 0, sideMargin, 0)
                            Log.d(tag, "Position 0: No overlay")
                        }
                        1 -> {
                            containerParams.setMargins(sideMargin, 0, 0, 0)
                            val remainingCount = fileSize - 2
                            showPlusMoreOverlay(remainingCount)
                            Log.d(tag, "Position 1: Showing overlay +$remainingCount")
                        }
                    }
                    documentContainer.layoutParams = containerParams

                    val imageLayoutParams = pdfImageView.layoutParams
                    imageLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                    imageLayoutParams.height = adaptiveHeight
                    pdfImageView.layoutParams = imageLayoutParams
                    pdfImageView.scaleType = ImageView.ScaleType.FIT_XY
                }
            }
        }
    }

    inner class FeedCombinationOfMultipleFiles(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val materialCardView: MaterialCardView = itemView.findViewById(R.id.materialCardView)
        private val countTextView: TextView = itemView.findViewById(R.id.countTextView)
        private val imageView2: ImageView = itemView.findViewById(R.id.imageViewOverlay)
        private val fileTypeIcon: ImageView = itemView.findViewById(R.id.fileTypeIcon)
        private val playButton: ImageView = itemView.findViewById(R.id.playButton)
        private val feedVideoImageView: ImageView = itemView.findViewById(R.id.feedVideoImageView)
        private val feedVideoDurationTextView: TextView = itemView.findViewById(R.id.feedVideoDurationTextView)

        fun Int.dpToPx(context: Context): Int {
            return (this * context.resources.displayMetrics.density).toInt()
        }

        // Helper function to calculate adaptive heights based on screen size
        private fun getAdaptiveHeights(context: Context): Pair<Int, Int> {
            val displayMetrics = context.resources.displayMetrics
            val screenHeight = displayMetrics.heightPixels

            val minHeight = (screenHeight * 0.12).toInt()
            val maxHeight = (screenHeight * 0.35).toInt()

            return Pair(minHeight, maxHeight)
        }

        // Helper function to get a constrained height within min/max bounds
        private fun getConstrainedHeight(context: Context, preferredHeight: Int): Int {
            val (minHeight, maxHeight) = getAdaptiveHeights(context)
            return preferredHeight.coerceIn(minHeight, maxHeight)
        }

        // Helper function to setup consistent +N count styling
        private fun setupCountTextViewStyling(context: Context, countText: String) {
            countTextView.visibility = View.VISIBLE
            countTextView.text = countText
            countTextView.textSize = 32f
            countTextView.setTextColor(Color.WHITE)
            countTextView.setPadding(12, 4, 12, 4)

            val background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16f
                setColor(Color.parseColor("#80000000"))
            }
            countTextView.background = background

            when (val params = countTextView.layoutParams) {
                is ConstraintLayout.LayoutParams -> {
                    params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                    params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                    params.marginEnd = 8.dpToPx(context) // Matches XML layout_marginEnd="8dp"
                    params.bottomMargin = 8.dpToPx(context) // Matches XML layout_marginBottom="8dp"
                    countTextView.layoutParams = params
                }
                is FrameLayout.LayoutParams -> {
                    params.gravity = Gravity.BOTTOM or Gravity.END
                    params.marginEnd = 8.dpToPx(context) // Matches XML layout_marginEnd="8dp"
                    params.bottomMargin = 8.dpToPx(context) // Matches XML layout_marginBottom="8dp"
                    countTextView.layoutParams = params
                }
                is ViewGroup.MarginLayoutParams -> {
                    params.marginEnd = 8.dpToPx(context) // Matches XML layout_marginEnd="8dp"
                    params.bottomMargin = 8.dpToPx(context) // Matches XML layout_marginBottom="8dp"
                    countTextView.layoutParams = params
                }
            }
        }

        // Helper function to configure MaterialCardView with proper corner radius for ALL elements
        private fun setupCardViewCorners(context: Context) {
            val cornerRadius = 8.dpToPx(context).toFloat()

            materialCardView.radius = cornerRadius
            materialCardView.clipToOutline = true
            materialCardView.clipChildren = true
            materialCardView.cardElevation = 0f
            materialCardView.maxCardElevation = 0f
            materialCardView.strokeWidth = 0
            materialCardView.setContentPadding(0, 0, 0, 0)
            materialCardView.useCompatPadding = false
            materialCardView.setCardBackgroundColor(Color.WHITE)

            imageView.clipToOutline = true
            imageView.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
                }
            }
            val imageLayoutParams = imageView.layoutParams as FrameLayout.LayoutParams
            imageLayoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT
            imageLayoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT
            imageLayoutParams.setMargins(0, 0, 0, 0)
            imageView.layoutParams = imageLayoutParams

            imageView2.clipToOutline = true
            imageView2.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
                }
            }

            fileTypeIcon.clipToOutline = true
            fileTypeIcon.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
                }
            }

            playButton.clipToOutline = true
            playButton.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
                }
            }

            feedVideoImageView.clipToOutline = true
            feedVideoImageView.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
                }
            }

            countTextView.clipToOutline = true
            countTextView.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
                }
            }

            feedVideoDurationTextView.clipToOutline = true
            feedVideoDurationTextView.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
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

        // Helper function to check if a file is a document
        private fun isDocument(mimeType: String): Boolean {
            return mimeType.contains("pdf") || mimeType.contains("docx") ||
                    mimeType.contains("pptx") || mimeType.contains("xlsx") ||
                    mimeType.contains("ppt") || mimeType.contains("xls") ||
                    mimeType.contains("txt") || mimeType.contains("rtf") ||
                    mimeType.contains("odt") || mimeType.contains("csv")
        }

        // Helper function to get the correct file index for fileSize == 3
        private fun getCorrectFileIndex(
            data: com.uyscuti.social.network.api.response.posts.Post,
            currentPosition: Int
        ): Int {
            if (data.files.size != 3) return currentPosition

            var documentIndex = -1
            data.fileTypes.forEachIndexed { index, fileType ->
                if (isDocument(fileType.fileType)) {
                    documentIndex = index
                    return@forEachIndexed
                }
            }

            if (documentIndex == -1) return currentPosition

            return when (currentPosition) {
                0 -> documentIndex
                1 -> if (documentIndex == 0) 1 else if (documentIndex == 1) 0 else 1
                2 -> if (documentIndex == 2) 1 else 2
                else -> currentPosition
            }
        }

        private fun navigateToTappedFilesFragment(
            context: Context,
            currentIndex: Int,
            files: ArrayList<File>,
            fileIds: List<String>
        ) {
            val activity = getActivityFromContext(context)
            if (activity != null) {
                activity.findViewById<View>(R.id.topBar)?.visibility = View.GONE
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
            }
        }

        @SuppressLint("SetTextI18n", "UseKtx")
        fun onBind(data: Post) {
            val context = itemView.context

            setupCardViewCorners(context)

            itemView.setBackgroundColor(Color.TRANSPARENT)

            val displayMetrics = context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val margin = 4.dpToPx(context)
            val spaceBetweenRows = 4.dpToPx(context)

            val (minHeight, maxHeight) = getAdaptiveHeights(context)

            // Check if this is the special case: 3 files with 2 documents
            val isSpecial3FileCase = data.files.size == 3 && data.fileTypes.count { isDocument(it.fileType) } == 2

            // Get the actual file index based on the scenario
            val actualFileIndex = if (isSpecial3FileCase) {
                // For 2-document case, map positions directly to document indices
                val documentIndices = data.fileTypes.mapIndexed { index, fileType ->
                    if (isDocument(fileType.fileType)) index else -1
                }.filter { it != -1 }

                when (absoluteAdapterPosition) {
                    0 -> documentIndices[0]
                    1 -> documentIndices[1]
                    else -> documentIndices[0] // Fallback
                }
            } else if (data.files.size == 3) {
                getCorrectFileIndex(data, absoluteAdapterPosition)
            } else {
                absoluteAdapterPosition
            }

            // Determine effective file size for layout logic
            val fileSize = if (isSpecial3FileCase) {
                2 // Treat as 2-file layout
            } else {
                itemCount
            }

            val fileIdToFind = data.fileIds[actualFileIndex]
            val file = data.files.find { it.fileId == fileIdToFind }
            val fileUrl = file?.url ?: data.files.getOrNull(actualFileIndex)?.url ?: ""
            val mimeType = data.fileTypes.getOrNull(actualFileIndex)?.fileType ?: ""
            val durationItem = data.duration?.find { it.fileId == fileIdToFind }
            feedVideoDurationTextView.text = durationItem?.duration


            playButton.visibility = View.GONE
            feedVideoImageView.visibility = View.GONE
            feedVideoDurationTextView.visibility = View.VISIBLE
            imageView2.visibility = View.GONE
            countTextView.visibility = View.GONE

            itemView.setOnClickListener {
                navigateToTappedFilesFragment(
                    context,
                    actualFileIndex,
                    data.files,
                    data.fileIds as List<String>
                )
            }

            imageView.setOnClickListener {
                navigateToTappedFilesFragment(
                    context,
                    actualFileIndex,
                    data.files,
                    data.fileIds as List<String>
                )
            }

            materialCardView.setOnClickListener {
                navigateToTappedFilesFragment(
                    context,
                    actualFileIndex,
                    data.files,
                    data.fileIds as List<String>
                )
            }

            countTextView.setOnClickListener {
                navigateToTappedFilesFragment(
                    context,
                    actualFileIndex,
                    data.files,
                    data.fileIds as List<String>
                )
            }

            imageView2.setOnClickListener {
                navigateToTappedFilesFragment(
                    context,
                    actualFileIndex,
                    data.files,
                    data.fileIds as List<String>
                )
            }

            feedVideoImageView.setOnClickListener {
                navigateToTappedFilesFragment(
                    context,
                    actualFileIndex,
                    data.files,
                    data.fileIds as List<String>
                )
            }

            val layoutParams = materialCardView.layoutParams as ViewGroup.MarginLayoutParams

            when {
                fileSize == 2 -> {
                    layoutParams.width = screenWidth / 2
                    layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.75).toInt())
                    layoutParams.topMargin = 0
                    layoutParams.bottomMargin = 0

                    val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
                    layoutParams.leftMargin = if (isLeftColumn) 0 else (spaceBetweenRows/2)
                    layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows/2) else 0

                    loadFileContent(fileUrl, mimeType, data, fileIdToFind.toString(), context)
                }

                fileSize == 3 -> {
                    // Normal 3-file cases (NOT the 2-document case)
                    when (absoluteAdapterPosition) {
                        0 -> {
                            layoutParams.width = screenWidth / 2
                            val baseFileHeight = getConstrainedHeight(context, (maxHeight * 0.8).toInt())
                            val rightSideItemHeight = baseFileHeight / 2
                            val totalRightSideHeight = (rightSideItemHeight * 2) + (spaceBetweenRows / 2)
                            layoutParams.height = totalRightSideHeight
                            layoutParams.leftMargin = 0
                            layoutParams.rightMargin = (spaceBetweenRows/2)
                            layoutParams.topMargin = 0
                            layoutParams.bottomMargin = 0
                        }
                        1 -> {
                            layoutParams.width = screenWidth / 2
                            val baseFileHeight = getConstrainedHeight(context, (maxHeight * 0.8).toInt())
                            val totalHeight = baseFileHeight + (spaceBetweenRows / 2)
                            layoutParams.height = (totalHeight - (spaceBetweenRows / 2)) / 2
                            layoutParams.leftMargin = (spaceBetweenRows/2)
                            layoutParams.rightMargin = 0
                            layoutParams.topMargin = 0
                            layoutParams.bottomMargin = 0
                        }
                        2 -> {
                            layoutParams.width = screenWidth / 2
                            val baseFileHeight = getConstrainedHeight(context, (maxHeight * 0.8).toInt())
                            val totalHeight = baseFileHeight + (spaceBetweenRows / 2)
                            layoutParams.height = (totalHeight - (spaceBetweenRows / 2)) / 2
                            layoutParams.leftMargin = (spaceBetweenRows/2)
                            layoutParams.rightMargin = 0
                            layoutParams.topMargin = (spaceBetweenRows/2)
                            layoutParams.bottomMargin = 0
                        }
                    }
                    itemView.visibility = View.VISIBLE
                    loadFileContent(fileUrl, mimeType, data, fileIdToFind.toString(), context)
                }

                fileSize == 4 -> {
                    layoutParams.width = screenWidth / 2
                    val preferredSquareHeight = screenWidth / 2
                    layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.75).toInt())

                    layoutParams.topMargin = if (absoluteAdapterPosition < 2) 0 else spaceBetweenRows
                    layoutParams.bottomMargin = if (absoluteAdapterPosition >= 2) 0 else 0

                    val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
                    layoutParams.leftMargin = if (isLeftColumn) 0 else (spaceBetweenRows/2)
                    layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows/2) else 0

                    loadFileContent(fileUrl, mimeType, data, fileIdToFind.toString(), context)
                }

                fileSize == 5 -> {
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
                    layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.75).toInt())

                    val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
                    layoutParams.leftMargin = if (isLeftColumn) 0 else (spaceBetweenRows /2)
                    layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows /2) else 0
                    layoutParams.topMargin = if (absoluteAdapterPosition < 2) 0 else spaceBetweenRows
                    layoutParams.bottomMargin = if (absoluteAdapterPosition < 2) spaceBetweenRows / 2 else 0

                    itemView.layoutParams = layoutParams

                    if (absoluteAdapterPosition == 3) {
                        setupCountTextViewStyling(context, "+${fileSize - 4}")
                    } else {
                        countTextView.visibility = View.GONE
                        countTextView.setPadding(0, 0, 0, 0)
                        countTextView.background = null
                    }

                    loadFileContent(fileUrl, mimeType, data, fileIdToFind.toString(), context)
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
                    layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.75).toInt())

                    val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
                    layoutParams.leftMargin = if (isLeftColumn) 0 else spaceBetweenRows
                    layoutParams.rightMargin = if (isLeftColumn) spaceBetweenRows else 0
                    layoutParams.topMargin = if (absoluteAdapterPosition < 2) 0 else spaceBetweenRows / 2
                    layoutParams.bottomMargin = if (absoluteAdapterPosition < 2) spaceBetweenRows / 2 else 0

                    itemView.layoutParams = layoutParams

                    if (absoluteAdapterPosition == 3) {
                        setupCountTextViewStyling(context, "+${fileSize - 4}")
                    } else {
                        countTextView.visibility = View.GONE
                        countTextView.setPadding(0, 0, 0, 0)
                        countTextView.background = null
                    }

                    loadFileContent(fileUrl, mimeType, data, fileIdToFind.toString(), context)
                }
            }

            materialCardView.layoutParams = layoutParams
        }

        private fun loadFileContent(
            fileUrl: String,
            mimeType: String,
            data: com.uyscuti.social.network.api.response.posts.Post,
            fileIdToFind: String,
            context: Context,
            fitImage: Boolean = false
        ) {
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

                    val params = fileTypeIcon.layoutParams as FrameLayout.LayoutParams
                    params.gravity = Gravity.BOTTOM or Gravity.START
                    params.marginStart = 8.dpToPx(context)
                    params.bottomMargin = 8.dpToPx(context)
                    fileTypeIcon.layoutParams = params
                }

                mimeType.startsWith("audio") -> {
                    imageView.setImageResource(R.drawable.music_icon)
                    imageView.visibility = View.VISIBLE
                    imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                    playButton.visibility = View.GONE
                    fileTypeIcon.visibility = View.VISIBLE
                    fileTypeIcon.setImageResource(R.drawable.ic_audio_white_icon)

                    val params = fileTypeIcon.layoutParams as FrameLayout.LayoutParams
                    params.gravity = Gravity.BOTTOM or Gravity.START
                    params.marginStart = 8.dpToPx(context)
                    params.bottomMargin = 8.dpToPx(context)
                    fileTypeIcon.layoutParams = params
                }

                mimeType.contains("pdf") || mimeType.contains("docx") ||
                        mimeType.contains("pptx") || mimeType.contains("xlsx") ||
                        mimeType.contains("ppt") || mimeType.contains("xls") ||
                        mimeType.contains("txt") || mimeType.contains("rtf") ||
                        mimeType.contains("odt") || mimeType.contains("csv") -> {

                    val thumbnail = data.thumbnail.find { it.fileId == fileIdToFind }
                    imageView.scaleType = ImageView.ScaleType.CENTER_CROP

                    if (thumbnail != null) {
                        Glide.with(itemView.context)
                            .load(thumbnail.thumbnailUrl)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop()
                            .into(imageView)
                    }

                    fileTypeIcon.setImageResource(
                        when {
                            mimeType.contains("pdf") -> R.drawable.pdf_icon
                            mimeType.contains("docx") -> R.drawable.word_icon
                            mimeType.contains("pptx") -> R.drawable.powerpoint_icon
                            mimeType.contains("xlsx") -> R.drawable.excel_icon
                            mimeType.contains("ppt") -> R.drawable.powerpoint_icon
                            mimeType.contains("xls") -> R.drawable.excel_icon
                            mimeType.contains("txt") -> R.drawable.text_icon
                            mimeType.contains("rtf") -> R.drawable.text_icon
                            mimeType.contains("odt") -> R.drawable.word_icon
                            mimeType.contains("csv") -> R.drawable.excel_icon
                            else -> R.drawable.text_icon
                        }
                    )
                    fileTypeIcon.visibility = View.VISIBLE
                    imageView.visibility = View.VISIBLE
                }
                else -> {
                    imageView.setImageResource(R.drawable.feed_mixed_image_view_rounded_corners)
                    imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                    fileTypeIcon.visibility = View.GONE
                }
            }
        }

        private fun loadImage(url: String) {
            Glide.with(itemView.context)
                .load(url)
                .placeholder(R.drawable.flash21)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(imageView)

            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.clipToOutline = true
        }

        private fun loadVideoThumbnail(url: String) {
            Glide.with(itemView.context)
                .asBitmap()
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(imageView)

            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.clipToOutline = true
        }
    }

    inner class FeedNewPostWithRepostInsideFilesPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val TAG = "FeedRepostedWithNewFilesPostViewHolder"

        // UI Elements - New Post Section (Top)
        private val userProfileImage: ImageView = itemView.findViewById(R.id.userProfileImage)
        private val repostedUserName: TextView = itemView.findViewById(R.id.repostedUserName)
        private val tvUserHandle: TextView = itemView.findViewById(R.id.tvUserHandle)
        private val dateTimeCreate: TextView = itemView.findViewById(R.id.date_time_create)
        private val followButton: AppCompatButton = itemView.findViewById(R.id.followButton)
        private val moreOptionsButton: ImageButton = itemView.findViewById(R.id.moreOptions)

        // Main clickable containers
        private val repostContainer: LinearLayout = itemView.findViewById(R.id.repostContainer)
        private val originalPostContainer: LinearLayout = itemView.findViewById(R.id.originalPostContainer)
        private val quotedPostCard: CardView = itemView.findViewById(R.id.quotedPostCard)

        // New Post Content Section (Top)
        private val tvPostTag: TextView = itemView.findViewById(R.id.tvPostTag)
        private val userComment: TextView = itemView.findViewById(R.id.userComment)
        private val tvHashtags: TextView = itemView.findViewById(R.id.tvHashtags)

        // New Post Media Section (Top)
        private val newPostMediaCard: CardView = itemView.findViewById(R.id.newPostMediaCard)
        private val newPostImage: ImageView = itemView.findViewById(R.id.newPostImage)
        private val newPostMultipleMediaContainer: ConstraintLayout = itemView.findViewById(R.id.newPostMultipleMediaContainer)
        private val newPostMediaRecyclerView: RecyclerView = itemView.findViewById(R.id.newPostMediaRecyclerView)

        // Original Post Media (for backward compatibility)
        private val mixedFilesCardViews: CardView = itemView.findViewById(R.id.mixedFilesCardViews)
        private val originalFeedImages: ImageView = itemView.findViewById(R.id.originalFeedImages)
        private val multipleAudiosContainers: ConstraintLayout = itemView.findViewById(R.id.multipleAudiosContainers)
        private val recyclerViews: RecyclerView = itemView.findViewById(R.id.recyclerViews)

        // Quoted/Original Post Section (Bottom)
        private val originalPosterProfileImage: ImageView = itemView.findViewById(R.id.originalPosterProfileImage)
        private val originalPosterName: TextView = itemView.findViewById(R.id.originalPosterName)
        private val tvQuotedUserHandle: TextView = itemView.findViewById(R.id.tvQuotedUserHandle)
        private val originalPostText: TextView = itemView.findViewById(R.id.originalPostText)
        private val tvQuotedHashtags: TextView = itemView.findViewById(R.id.tvQuotedHashtags)

        // Quoted Post Media
        private val mixedFilesCardView: CardView = itemView.findViewById(R.id.mixedFilesCardView)
        private val originalFeedImage: ImageView = itemView.findViewById(R.id.originalFeedImage)
        private val multipleAudiosContainer: ConstraintLayout = itemView.findViewById(R.id.multipleAudiosContainer)
        private val recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerView)
        private val ivQuotedPostImage: ImageView = itemView.findViewById(R.id.ivQuotedPostImage)

        // Interaction Buttons
        private val likeSection: LinearLayout = itemView.findViewById(R.id.likeLayout)
        private val likeButton: ImageView = itemView.findViewById(R.id.likeButtonIcon)
        private val likesCount: TextView = itemView.findViewById(R.id.likesCount)

        private val commentSection: LinearLayout = itemView.findViewById(R.id.commentLayout)
        private val commentButton: ImageView = itemView.findViewById(R.id.commentButtonIcon)
        private val feedCommentsCount: TextView = itemView.findViewById(R.id.commentCount)

        private val favoriteSection: LinearLayout = itemView.findViewById(R.id.favoriteSection)
        private val favoriteButton: ImageView = itemView.findViewById(R.id.favoritesButton)
        private val favCount: TextView = itemView.findViewById(R.id.favoriteCounts)

        private val repostSection: LinearLayout = itemView.findViewById(R.id.repostPost)
        private val repostPost: ImageView = itemView.findViewById(R.id.repostPost)
        private val repostCountTextView: TextView = itemView.findViewById(R.id.repostCount)

        private val shareSection: LinearLayout = itemView.findViewById(R.id.shareButtonIcon)
        private val shareImageView: ImageView = itemView.findViewById(R.id.shareButtonIcon)
        private val shareCountTextView: TextView = itemView.findViewById(R.id.shareCount)

        // Additional UI elements
        private val feedMixedFilesContainer: CardView = itemView.findViewById(R.id.feedMixedFilesContainer)
        private val bottomDivider: View = itemView.findViewById(R.id.bottomDivider)
        private val interactionButtonsCard: LinearLayout = itemView.findViewById(R.id.interactionButtonsCard)

        // State variables - ID management
        private var currentPostId: String = ""
        private var currentAuthorId: String = ""
        private var originalPostId: String = ""
        private var originalAuthorId: String = ""
        private var repostedUserId: String = ""

        // Other state variables
        private var isFollowed = false
        private var totalMixedComments = 0
        private var serverCommentCount = 0
        private var loadedCommentCount = 0
        private var currentPost: Post? = null
        private var totalMixedLikesCounts = 0
        private var totalMixedBookMarkCounts = 0
        private var totalMixedShareCounts = 0
        private var totalMixedRePostCounts = 0
        private var postClicked = false

        // For media adapter - requires these views
        private val materialCardView: CardView by lazy { itemView.findViewById(R.id.materialCardView) }
        private val imageView: ImageView by lazy { itemView.findViewById(R.id.imageView) }
        private val imageView2: ImageView by lazy { itemView.findViewById(R.id.imageView2) }
        private val fileTypeIcon: ImageView by lazy { itemView.findViewById(R.id.fileTypeIcon) }
        private val playButton: ImageView by lazy { itemView.findViewById(R.id.playButton) }
        private val feedVideoImageView: ImageView by lazy { itemView.findViewById(R.id.feedVideoImageView) }
        private val countTextView: TextView by lazy { itemView.findViewById(R.id.countTextView) }
        private val feedVideoDurationTextView: TextView by lazy { itemView.findViewById(R.id.feedVideoDurationTextView) }

        fun Int.dpToPx(context: Context): Int {
            return (this * context.resources.displayMetrics.density).toInt()
        }

        private fun getAdaptiveHeights(context: Context): Pair<Int, Int> {
            val displayMetrics = context.resources.displayMetrics
            val screenHeight = displayMetrics.heightPixels
            val minHeight = (screenHeight * 0.12).toInt()
            val maxHeight = (screenHeight * 0.35).toInt()
            return Pair(minHeight, maxHeight)
        }

        private fun getConstrainedHeight(context: Context, preferredHeight: Int): Int {
            val (minHeight, maxHeight) = getAdaptiveHeights(context)
            return preferredHeight.coerceIn(minHeight, maxHeight)
        }

        private fun setupCountTextViewStyling(context: Context, countText: String) {
            countTextView.visibility = View.VISIBLE
            countTextView.text = countText
            countTextView.textSize = 32f
            countTextView.setTextColor(Color.WHITE)
            countTextView.setPadding(12, 4, 12, 4)

            val background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16f
                setColor(Color.parseColor("#80000000"))
            }
            countTextView.background = background

            when (val params = countTextView.layoutParams) {
                is ConstraintLayout.LayoutParams -> {
                    params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                    params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                    params.marginEnd = 8.dpToPx(context)
                    params.bottomMargin = 8.dpToPx(context)
                    countTextView.layoutParams = params
                }
                is FrameLayout.LayoutParams -> {
                    params.gravity = Gravity.BOTTOM or Gravity.END
                    params.marginEnd = 8.dpToPx(context)
                    params.bottomMargin = 8.dpToPx(context)
                    countTextView.layoutParams = params
                }
                is ViewGroup.MarginLayoutParams -> {
                    params.marginEnd = 8.dpToPx(context)
                    params.bottomMargin = 8.dpToPx(context)
                    countTextView.layoutParams = params
                }
            }
        }

        private fun setupCardViewCorners(context: Context) {
            val cornerRadius = 8.dpToPx(context).toFloat()

            materialCardView.radius = cornerRadius
            materialCardView.clipToOutline = true
            materialCardView.clipChildren = true
            materialCardView.cardElevation = 0f
            materialCardView.maxCardElevation = 0f

            materialCardView.setContentPadding(0, 0, 0, 0)
            materialCardView.useCompatPadding = false
            materialCardView.setCardBackgroundColor(Color.WHITE)

            val views = listOf(imageView, imageView2, fileTypeIcon, playButton, feedVideoImageView, countTextView, feedVideoDurationTextView)
            views.forEach { view ->
                view.clipToOutline = true
                view.outlineProvider = object : ViewOutlineProvider() {
                    override fun getOutline(view: View, outline: Outline) {
                        outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
                    }
                }
            }

            val imageLayoutParams = imageView.layoutParams as FrameLayout.LayoutParams
            imageLayoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT
            imageLayoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT
            imageLayoutParams.setMargins(0, 0, 0, 0)
            imageView.layoutParams = imageLayoutParams
        }

        private fun getActivityFromContext(context: Context): AppCompatActivity? {
            return when (context) {
                is AppCompatActivity -> context
                is ContextWrapper -> getActivityFromContext(context.baseContext)
                else -> null
            }
        }

        private fun isDocument(mimeType: String): Boolean {
            return mimeType.contains("pdf") || mimeType.contains("docx") ||
                    mimeType.contains("pptx") || mimeType.contains("xlsx") ||
                    mimeType.contains("ppt") || mimeType.contains("xls") ||
                    mimeType.contains("txt") || mimeType.contains("rtf") ||
                    mimeType.contains("odt") || mimeType.contains("csv")
        }

        private fun getCorrectFileIndex(
            data: com.uyscuti.social.network.api.response.posts.Post,
            currentPosition: Int
        ): Int {
            if (data.files.size != 3) return currentPosition

            var documentIndex = -1
            data.fileTypes.forEachIndexed { index, fileType ->
                if (isDocument(fileType.fileType)) {
                    documentIndex = index
                    return@forEachIndexed
                }
            }

            if (documentIndex == -1) return currentPosition

            return when (currentPosition) {
                0 -> documentIndex
                1 -> if (documentIndex == 0) 1 else if (documentIndex == 1) 0 else 1
                2 -> if (documentIndex == 2) 1 else 2
                else -> currentPosition
            }
        }

        private fun navigateToTappedFilesFragment(
            context: Context,
            currentIndex: Int,
            files: ArrayList<File>,
            fileIds: List<String>
        ) {
            val activity = getActivityFromContext(context)
            if (activity != null) {
                activity.findViewById<View>(R.id.topBar)?.visibility = View.GONE
                activity.findViewById<View>(R.id.bottomNavigationView)?.visibility = View.GONE

                val fragment = Tapped_Files_In_The_Container_View_Fragment()

                val bundle = Bundle().apply {
                    putInt("current_index", currentIndex)
                    putInt("total_files", files.size)

                    // Pass relevant IDs
                    putString("current_post_id", currentPostId)
                    putString("current_author_id", currentAuthorId)
                    putString("original_post_id", originalPostId)
                    putString("original_author_id", originalAuthorId)

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
            }
        }

        @OptIn(UnstableApi::class)
        @SuppressLint("SetTextI18n", "SuspiciousIndentation")
        fun render(data: Post) {
            Log.d(TAG, "render: feed data $data")

            // Store current post reference and extract all IDs
            currentPost = data
            extractAndStoreIds(data)

            totalMixedComments = data.comments
            totalMixedLikesCounts = data.likes
            totalMixedBookMarkCounts = data.bookmarkCount
            totalMixedShareCounts = data.shareCount
            totalMixedRePostCounts = data.repostCount


            setupNewPostMediaFiles(data)
            setupOriginalPostContent(data)
            setupEngagementButtons(data)
            setupProfileClickListeners(data)
            setupFollowButton()
            setupPostClickListeners(data)
            ensurePostClickability(data)
            setupInteractionButtonsClickPrevention()
        }

        private fun extractAndStoreIds(data: Post) {
            // Extract main post ID
            currentPostId = data._id

            // Extract reposter/author IDs
            repostedUserId = data.repostedUser?._id ?: ""
            currentAuthorId = data.author?.account?._id ?: repostedUserId

            // Extract original post IDs if this is a repost
            if (data.originalPost.isNotEmpty()) {
                val originalPostData = data.originalPost[0]
                originalPostId = originalPostData._id

                // Get original author ID
                originalAuthorId = when {
                    originalPostData.originalPostReposter.isNotEmpty() -> {
                        // Note: originalPostReposter is List<Any?>, so we need to cast if it contains actual reposter objects
                        // This might need adjustment based on your actual data structure
                        val reposter = originalPostData.originalPostReposter[0]
                        if (reposter is AuthorX) reposter._id else ""
                    }
                    else -> originalPostData.author._id
                }
            }

            Log.d(TAG, "IDs extracted - Post: $currentPostId, Author: $currentAuthorId, " +
                    "Reposter: $repostedUserId, Original Post: $originalPostId, Original Author: $originalAuthorId")
        }

        @SuppressLint("SetTextI18n", "UseKtx")
        fun onBind(data: com.uyscuti.social.network.api.response.posts.Post) {
            val context = itemView.context

            setupCardViewCorners(context)
            itemView.setBackgroundColor(Color.TRANSPARENT)

            val displayMetrics = context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val spaceBetweenRows = 4.dpToPx(context)
            val (minHeight, maxHeight) = getAdaptiveHeights(context)

            val actualFileIndex = if (data.files.size == 3) {
                getCorrectFileIndex(data, absoluteAdapterPosition)
            } else {
                absoluteAdapterPosition
            }

            val fileIdToFind = data.fileIds[actualFileIndex]
            val file = data.files.find { it.fileId == fileIdToFind }
            val fileUrl = file?.url ?: data.files.getOrNull(actualFileIndex)?.url ?: ""
            val mimeType = data.fileTypes.getOrNull(actualFileIndex)?.fileType ?: ""
            val durationItem = data.duration?.find { it.fileId == fileIdToFind }
            feedVideoDurationTextView.text = durationItem?.duration

            val fileSize = itemCount

            // Reset visibility
            playButton.visibility = View.GONE
            feedVideoImageView.visibility = View.GONE
            feedVideoDurationTextView.visibility = View.VISIBLE
            imageView2.visibility = View.GONE
            countTextView.visibility = View.GONE

            // Setup click listeners with proper ID passing
            setupFileItemClickListeners(context, actualFileIndex, data)

            val layoutParams = materialCardView.layoutParams as ViewGroup.MarginLayoutParams

            when {
                fileSize == 2 -> {
                    setupTwoFileLayout(layoutParams, screenWidth, spaceBetweenRows, context, maxHeight)
                    loadFileContent(fileUrl, mimeType, data, fileIdToFind.toString(), context)
                }
                fileSize == 3 -> {
                    setupThreeFileLayout(layoutParams, screenWidth, spaceBetweenRows,
                        context, maxHeight, data, actualFileIndex,
                        fileIdToFind.toString(), fileUrl, mimeType)
                }
                fileSize == 4 -> {
                    setupFourFileLayout(layoutParams, screenWidth, spaceBetweenRows, context, maxHeight)
                    loadFileContent(fileUrl, mimeType, data, fileIdToFind.toString(), context)
                }
                fileSize == 5 -> {
                    setupFiveFileLayout(layoutParams, screenWidth, spaceBetweenRows, context, maxHeight, fileSize)
                    if (absoluteAdapterPosition < 4) {
                        loadFileContent(fileUrl, mimeType, data, fileIdToFind.toString(), context)
                    }
                }
                fileSize > 4 -> {
                    setupMoreThanFourFileLayout(layoutParams, screenWidth, spaceBetweenRows, context, maxHeight, fileSize)
                    if (absoluteAdapterPosition < 4) {
                        loadFileContent(fileUrl, mimeType, data, fileIdToFind.toString(), context)
                    }
                }
            }

            materialCardView.layoutParams = layoutParams
        }

        private fun setupFileItemClickListeners(
            context: Context,
            actualFileIndex: Int,
            data: com.uyscuti.social.network.api.response.posts.Post
        ) {
            val clickListener = View.OnClickListener {
                navigateToTappedFilesFragment(
                    context,
                    actualFileIndex,
                    data.files,
                    data.fileIds as List<String>
                )
            }

            // Apply the same click listener to all relevant views
            listOf(itemView, imageView, materialCardView, countTextView, imageView2, feedVideoImageView)
                .forEach { it.setOnClickListener(clickListener) }
        }

        private fun setupTwoFileLayout(
            layoutParams: ViewGroup.MarginLayoutParams,
            screenWidth: Int,
            spaceBetweenRows: Int,
            context: Context,
            maxHeight: Int
        ) {
            layoutParams.width = screenWidth / 2
            layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.75).toInt())
            layoutParams.topMargin = 0
            layoutParams.bottomMargin = 0

            val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
            layoutParams.leftMargin = if (isLeftColumn) 0 else (spaceBetweenRows / 2)
            layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows / 2) else 0
        }

        private fun setupThreeFileLayout(
            layoutParams: ViewGroup.MarginLayoutParams,
            screenWidth: Int,
            spaceBetweenRows: Int,
            context: Context,
            maxHeight: Int,
            data: com.uyscuti.social.network.api.response.posts.Post,
            actualFileIndex: Int,
            fileIdToFind: String,
            fileUrl: String,
            mimeType: String
        ) {
            val documentCount = data.fileTypes.count { isDocument(it.fileType) }

            if (documentCount == 2) {
                setupThreeFileLayoutWithTwoDocuments(layoutParams, screenWidth, spaceBetweenRows, context, maxHeight, data, actualFileIndex, fileIdToFind, fileUrl, mimeType)
            } else {
                setupThreeFileLayoutStandard(layoutParams, screenWidth, spaceBetweenRows, context, maxHeight, fileUrl, mimeType, data, fileIdToFind.toString())
            }
        }

        private fun setupThreeFileLayoutWithTwoDocuments(
            layoutParams: ViewGroup.MarginLayoutParams,
            screenWidth: Int,
            spaceBetweenRows: Int,
            context: Context,
            maxHeight: Int,
            data: com.uyscuti.social.network.api.response.posts.Post,
            actualFileIndex: Int,
            fileIdToFind: String,
            fileUrl: String,
            mimeType: String
        ) {
            val documentIndices = data.fileTypes.mapIndexed { index, fileType ->
                if (isDocument(fileType.fileType)) index else -1
            }.filter { it != -1 }

            val correctedActualFileIndex = when (absoluteAdapterPosition) {
                0 -> documentIndices[0]
                1 -> documentIndices[1]
                2 -> data.fileTypes.indices.find { !documentIndices.contains(it) } ?: 0
                else -> actualFileIndex
            }

            val correctedFileIdToFind = data.fileIds[correctedActualFileIndex]
            val file = data.files.find { it.fileId == correctedFileIdToFind }
            val correctedFileUrl = file?.url ?: data.files.getOrNull(correctedActualFileIndex)?.url ?: ""
            val correctedMimeType = data.fileTypes.getOrNull(correctedActualFileIndex)?.fileType ?: ""
            val durationItem = data.duration?.find { it.fileId == correctedFileIdToFind }
            feedVideoDurationTextView.text = durationItem?.duration

            layoutParams.width = screenWidth / 2
            val baseFileHeight = getConstrainedHeight(context, (maxHeight * 0.8).toInt())
            layoutParams.height = baseFileHeight

            when (absoluteAdapterPosition) {
                0 -> {
                    layoutParams.leftMargin = 0
                    layoutParams.rightMargin = (spaceBetweenRows / 2)
                    layoutParams.topMargin = 0
                    layoutParams.bottomMargin = 0
                }
                1 -> {
                    layoutParams.leftMargin = (spaceBetweenRows / 2)
                    layoutParams.rightMargin = 0
                    layoutParams.topMargin = 0
                    layoutParams.bottomMargin = 0
                    setupCountTextViewStyling(context, "+1")
                }
                2 -> {
                    itemView.visibility = View.GONE
                    layoutParams.width = 0
                    layoutParams.height = 0
                    itemView.layoutParams = layoutParams
                    return
                }
            }

            if (absoluteAdapterPosition != 2) {
                itemView.visibility = View.VISIBLE
                loadFileContent(correctedFileUrl, correctedMimeType, data, correctedFileIdToFind.toString(), context)
            }
        }

        private fun setupThreeFileLayoutStandard(
            layoutParams: ViewGroup.MarginLayoutParams,
            screenWidth: Int,
            spaceBetweenRows: Int,
            context: Context,
            maxHeight: Int,
            fileUrl: String,
            mimeType: String,
            data: com.uyscuti.social.network.api.response.posts.Post,
            fileIdToFind: String
        ) {
            when (absoluteAdapterPosition) {
                0 -> {
                    layoutParams.width = screenWidth / 2
                    val baseFileHeight = getConstrainedHeight(context, (maxHeight * 0.8).toInt())
                    val rightSideItemHeight = baseFileHeight / 2
                    val totalRightSideHeight = (rightSideItemHeight * 2) + (spaceBetweenRows / 2)
                    layoutParams.height = totalRightSideHeight
                    layoutParams.leftMargin = 0
                    layoutParams.rightMargin = (spaceBetweenRows / 2)
                    layoutParams.topMargin = 0
                    layoutParams.bottomMargin = 0
                }
                1 -> {
                    layoutParams.width = screenWidth / 2
                    val baseFileHeight = getConstrainedHeight(context, (maxHeight * 0.8).toInt())
                    val totalHeight = baseFileHeight + (spaceBetweenRows / 2)
                    layoutParams.height = (totalHeight - (spaceBetweenRows / 2)) / 2
                    layoutParams.leftMargin = (spaceBetweenRows / 2)
                    layoutParams.rightMargin = 0
                    layoutParams.topMargin = 0
                    layoutParams.bottomMargin = 0
                }
                2 -> {
                    layoutParams.width = screenWidth / 2
                    val baseFileHeight = getConstrainedHeight(context, (maxHeight * 0.8).toInt())
                    val totalHeight = baseFileHeight + (spaceBetweenRows / 2)
                    layoutParams.height = (totalHeight - (spaceBetweenRows / 2)) / 2
                    layoutParams.leftMargin = (spaceBetweenRows / 2)
                    layoutParams.rightMargin = 0
                    layoutParams.topMargin = (spaceBetweenRows / 2)
                    layoutParams.bottomMargin = 0
                }
            }
            loadFileContent(fileUrl, mimeType, data, fileIdToFind, context)
        }

        private fun setupFourFileLayout(
            layoutParams: ViewGroup.MarginLayoutParams,
            screenWidth: Int,
            spaceBetweenRows: Int,
            context: Context,
            maxHeight: Int
        ) {
            layoutParams.width = screenWidth / 2
            layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.75).toInt())

            layoutParams.topMargin = if (absoluteAdapterPosition < 2) 0 else spaceBetweenRows
            layoutParams.bottomMargin = 0

            val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
            layoutParams.leftMargin = if (isLeftColumn) 0 else (spaceBetweenRows / 2)
            layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows / 2) else 0
        }

        private fun setupFiveFileLayout(
            layoutParams: ViewGroup.MarginLayoutParams,
            screenWidth: Int,
            spaceBetweenRows: Int,
            context: Context,
            maxHeight: Int,
            fileSize: Int
        ) {
            if (absoluteAdapterPosition >= 4) {
                itemView.visibility = View.GONE
                layoutParams.width = 0
                layoutParams.height = 0
                itemView.layoutParams = layoutParams
                return
            }

            itemView.visibility = View.VISIBLE
            layoutParams.width = screenWidth / 2
            layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.75).toInt())

            val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
            layoutParams.leftMargin = if (isLeftColumn) 0 else (spaceBetweenRows / 2)
            layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows / 2) else 0
            layoutParams.topMargin = if (absoluteAdapterPosition < 2) 0 else spaceBetweenRows
            layoutParams.bottomMargin = if (absoluteAdapterPosition < 2) spaceBetweenRows / 2 else 0

            itemView.layoutParams = layoutParams

            if (absoluteAdapterPosition == 3) {
                setupCountTextViewStyling(context, "+${fileSize - 4}")
            } else {
                countTextView.visibility = View.GONE
                countTextView.setPadding(0, 0, 0, 0)
                countTextView.background = null
            }
        }

        private fun setupMoreThanFourFileLayout(
            layoutParams: ViewGroup.MarginLayoutParams,
            screenWidth: Int,
            spaceBetweenRows: Int,
            context: Context,
            maxHeight: Int,
            fileSize: Int
        ) {
            if (absoluteAdapterPosition >= 4) {
                itemView.visibility = View.GONE
                layoutParams.width = 0
                layoutParams.height = 0
                itemView.layoutParams = layoutParams
                return
            }

            itemView.visibility = View.VISIBLE
            layoutParams.width = screenWidth / 2
            layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.75).toInt())

            val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
            layoutParams.leftMargin = if (isLeftColumn) 0 else spaceBetweenRows / 2
            layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows / 2) else 0
            layoutParams.topMargin = if (absoluteAdapterPosition < 2) 0 else spaceBetweenRows
            layoutParams.bottomMargin = if (absoluteAdapterPosition < 2) spaceBetweenRows / 2 else 0

            itemView.layoutParams = layoutParams

            if (absoluteAdapterPosition == 3) {
                setupCountTextViewStyling(context, "+${fileSize - 4}")
            } else {
                countTextView.visibility = View.GONE
                countTextView.setPadding(0, 0, 0, 0)
                countTextView.background = null
            }
        }

        private fun loadFileContent(
            fileUrl: String,
            mimeType: String,
            data: com.uyscuti.social.network.api.response.posts.Post,
            fileId: String,
            context: Context
        ) {
            when {
                mimeType.contains("video") -> {
                    loadVideoContent(fileUrl, context)
                }
                mimeType.contains("image") -> {
                    loadImageContent(fileUrl, context)
                }
                mimeType.contains("audio") -> {
                    loadAudioContent(fileUrl, context)
                }
                isDocument(mimeType) -> {
                    loadDocumentContent(fileUrl, mimeType, context)
                }
                else -> {
                    loadImageContent(fileUrl, context) // Default fallback
                }
            }
        }

        private fun loadVideoContent(fileUrl: String, context: Context) {
            playButton.visibility = View.VISIBLE
            feedVideoImageView.visibility = View.VISIBLE
            feedVideoDurationTextView.visibility = View.VISIBLE

            // Load video thumbnail using Glide
            Glide.with(context)
                .load(fileUrl)
                .placeholder(R.drawable.videoplaceholder)
                .error(R.drawable.videoplaceholder)
                .centerCrop()
                .into(feedVideoImageView)

            imageView.visibility = View.GONE
            imageView2.visibility = View.GONE
        }

        private fun loadImageContent(fileUrl: String, context: Context) {
            imageView.visibility = View.VISIBLE
            playButton.visibility = View.GONE
            feedVideoImageView.visibility = View.GONE
            feedVideoDurationTextView.visibility = View.GONE
            imageView2.visibility = View.GONE

            // Load image using Glide
            Glide.with(context)
                .load(fileUrl)
                .placeholder(R.drawable.imageplaceholder)
                .error(R.drawable.imageplaceholder)
                .centerCrop()
                .into(imageView)
        }

        private fun loadAudioContent(fileUrl: String, context: Context) {
            imageView2.visibility = View.VISIBLE
            playButton.visibility = View.VISIBLE
            imageView.visibility = View.GONE
            feedVideoImageView.visibility = View.GONE
            feedVideoDurationTextView.visibility = View.VISIBLE

            // Set audio placeholder
            imageView2.setImageResource(R.drawable.music_icon)
        }

        private fun loadDocumentContent(fileUrl: String, mimeType: String, context: Context) {
            imageView2.visibility = View.VISIBLE
            fileTypeIcon.visibility = View.VISIBLE
            imageView.visibility = View.GONE
            playButton.visibility = View.GONE
            feedVideoImageView.visibility = View.GONE
            feedVideoDurationTextView.visibility = View.GONE

            // Set document icon based on type
            val iconRes = when {
                mimeType.contains("pdf") -> R.drawable.pdf_icon
                mimeType.contains("docx") || mimeType.contains("doc") -> R.drawable.word_icon
                mimeType.contains("pptx") || mimeType.contains("ppt") -> R.drawable.powerpoint_icon
                mimeType.contains("xlsx") || mimeType.contains("xls") -> R.drawable.excel_icon
                mimeType.contains("txt") -> R.drawable.text_icon
                else -> R.drawable.documents
            }


            fileTypeIcon.setImageResource(iconRes)
        }



        private fun setupNewPostMediaFiles(data: Post) {
            // Handle new post media files if they exist
            if (data.files.isNotEmpty()) {
                newPostMediaCard.visibility = View.VISIBLE
                setupNewPostMediaRecyclerView(data)
            } else {
                newPostMediaCard.visibility = View.GONE
            }
        }

        private fun setupNewPostMediaRecyclerView(data: Post) {
            // Setup RecyclerView for new post media
            newPostMediaRecyclerView.layoutManager = GridLayoutManager(itemView.context, 2)
            // Set adapter for media files
            // This would typically use a separate adapter for media files
        }


        private fun setupOriginalPostContent(data: Post) {
            if (data.originalPost?.isNotEmpty() == true) {
                val originalPost = data.originalPost[0]
                quotedPostCard.visibility = View.VISIBLE


                setupOriginalPostMedia(originalPost)
            } else {
                quotedPostCard.visibility = View.GONE
            }
        }


        private fun setupOriginalPostMedia(originalPost: com.uyscuti.social.network.api.response.posts.OriginalPost) {
            // Handle original post media files
            if (originalPost.files.isNotEmpty()) {
                // Show appropriate media container based on content type
                // This is a simplified version - you'd need to implement based on your requirements
                mixedFilesCardView.visibility = View.VISIBLE

                // Load first image as preview
                val firstFile = originalPost.files[0]
                Glide.with(itemView.context)
                    .load(firstFile.url)
                    .placeholder(R.drawable.imageplaceholder)
                    .error(R.drawable.imageplaceholder)
                    .centerCrop()
                    .into(originalFeedImage)
            } else {
                mixedFilesCardView.visibility = View.GONE
            }
        }

        private fun setupEngagementButtons(data: Post) {
            // Update like button state and count
            updateLikeButton(data.isLiked)
            likesCount.text = formatCount(totalMixedLikesCounts)

            // Update comment count
            feedCommentsCount.text = formatCount(totalMixedComments)

            // Update bookmark button state and count
            updateBookmarkButton(data.isBookmarked)
            favCount.text = formatCount(totalMixedBookMarkCounts)

            // Update repost button state and count
            updateRepostButton(data.isReposted)
            repostCountTextView.text = formatCount(totalMixedRePostCounts)

            // Update share count
            shareCountTextView.text = formatCount(totalMixedShareCounts)
        }

        private fun updateLikeButton(isLiked: Boolean) {
            if (isLiked) {
                likeButton.setImageResource(R.drawable.heart_svgrepo_com)

            } else {
                likeButton.setImageResource(com.uyscuti.social.business.R.drawable.ic_heart)

            }
        }

        private fun updateBookmarkButton(isBookmarked: Boolean) {
            if (isBookmarked) {
                favoriteButton.setImageResource(R.drawable.favorite_svgrepo_com__1_)

            } else {
                favoriteButton.setImageResource(R.drawable.filled_favorite)

            }
        }

        private fun updateRepostButton(isReposted: Boolean) {
            if (isReposted) {
                repostPost.setImageResource(R.drawable.retweet)

            } else {
                repostPost.setImageResource(R.drawable.retweet)

            }
        }

        private fun setupProfileClickListeners(data: Post) {
            val profileClickListener = View.OnClickListener {
                // Navigate to user profile
                navigateToUserProfile(data.repostedUser?._id ?: data.author?.account?._id ?: "")
            }

            userProfileImage.setOnClickListener(profileClickListener)
            repostedUserName.setOnClickListener(profileClickListener)

            // Original poster profile click
            if (data.originalPost?.isNotEmpty() == true) {
                val originalProfileClickListener = View.OnClickListener {
                    navigateToUserProfile(originalAuthorId)
                }

                originalPosterProfileImage.setOnClickListener(originalProfileClickListener)
                originalPosterName.setOnClickListener(originalProfileClickListener)
            }
        }

        private fun setupFollowButton() {
            followButton.setOnClickListener {
                // Handle follow/unfollow logic
                handleFollowAction()
            }

            // Update follow button text based on follow status
            followButton.text = if (isFollowed) "Following" else "Follow"
        }

        private fun setupPostClickListeners(data: Post) {
            val postClickListener = View.OnClickListener {
                if (!postClicked) {
                    navigateToPostDetail(data)
                }
            }

            // Apply to main post areas but not interaction buttons
            repostContainer.setOnClickListener(postClickListener)
            originalPostContainer.setOnClickListener(postClickListener)
            userComment.setOnClickListener(postClickListener)
            originalPostText.setOnClickListener(postClickListener)
        }

        private fun ensurePostClickability(data: Post) {
            // Ensure the main containers are clickable
            repostContainer.isClickable = true
            originalPostContainer.isClickable = true
            quotedPostCard.isClickable = true
        }

        private fun setupInteractionButtonsClickPrevention() {
            // Prevent post click when interaction buttons are tapped
            val preventPostClickListener = View.OnClickListener { postClicked = true }

            likeSection.setOnClickListener { handleLikeAction() }
            commentSection.setOnClickListener { handleCommentAction() }
            favoriteSection.setOnClickListener { handleBookmarkAction() }
            repostSection.setOnClickListener { handleRepostAction() }
            shareSection.setOnClickListener { handleShareAction() }

            // Reset postClicked flag after a delay
            itemView.postDelayed({ postClicked = false }, 300)
        }

        // Action handlers
        private fun handleLikeAction() {
            // Implement like/unlike logic
        }

        private fun handleCommentAction() {
            // Navigate to comments or open comment dialog
        }

        private fun handleBookmarkAction() {
            // Implement bookmark/unbookmark logic
        }

        private fun handleRepostAction() {
            // Implement repost logic
        }

        private fun handleShareAction() {
            // Implement share functionality
        }

        private fun handleFollowAction() {
            // Implement follow/unfollow logic
            isFollowed = !isFollowed
            followButton.text = if (isFollowed) "Following" else "Follow"
        }

        // Navigation methods
        private fun navigateToUserProfile(userId: String) {
            if (userId.isNotEmpty()) {
                // Navigate to user profile fragment/activity
            }
        }

        private fun navigateToPostDetail(data: Post) {
            // Navigate to post detail fragment/activity
        }

        // Utility methods
        private fun formatDate(dateString: String?): String {
            // Implement date formatting logic
            return dateString ?: ""
        }

        private fun formatCount(count: Int): String {
            return when {
                count < 1000 -> count.toString()
                count < 1000000 -> String.format("%.1fK", count / 1000.0)
                else -> String.format("%.1fM", count / 1000000.0)
            }
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

            is FeedNewPostWithRepostInsideFilesPostViewHolder -> {
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
                it.fileType.startsWith("vn") -> "vn"
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

            fileTypes.contains("vn") -> {
                VIEW_TYPE_VOICE_NOTE
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