package com.uyscuti.social.circuit.adapter.feed.multiple_files

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Outline
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.feedRepost.Tapped_Files_In_The_Container_View_Fragment
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.utils.waveformseekbar.WaveformSeekBar
import com.uyscuti.social.network.api.response.posts.OriginalPost
import com.uyscuti.social.network.api.response.posts.File
import com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.feedRepost.PostItem
import com.uyscuti.social.network.api.response.posts.Duration
import com.uyscuti.social.network.api.response.posts.FileType
import com.uyscuti.social.network.api.response.posts.ThumbnailX
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import androidx.core.graphics.toColorInt


private const val VIEW_TYPE_IMAGE_FEED = 0
private const val VIEW_TYPE_AUDIO_FEED = 1
private const val VIEW_TYPE_VIDEO_FEED = 2
private const val VIEW_TYPE_DOCUMENT_FEED = 3
private const val VIEW_TYPE_COMBINATION_OF_MULTIPLE_FILES = 4

private const val TAG = "FeedRepostFilesAdapter"

class FeedRepostViewFileAdapter(

    private val images: List<String>,
    private val feedPost: OriginalPost,



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
                    R.layout.feed_multiple_images_only_view_item, parent, false
                )
                FeedRepostImagesOnly(itemView)
            }

            VIEW_TYPE_AUDIO_FEED -> {

                val itemView = inflater.inflate(
                    R.layout.feed_multiple_audios_only_view_item, parent, false
                )
                FeedRepostAudiosOnly(itemView)
            }

            VIEW_TYPE_VIDEO_FEED -> {

                val itemView = inflater.inflate(
                    R.layout.feed_multiple_videos_only_view_item, parent, false
                )
                FeedRepostVideosOnly(itemView)
            }

            VIEW_TYPE_DOCUMENT_FEED -> {

                val itemView =
                    inflater.inflate(
                        R.layout.feed_multiple_documents_only_view_item, parent, false
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
        private val materialCardView: MaterialCardView = itemView.findViewById(R.id.materialCardView)
        private val countTextView: TextView = itemView.findViewById(R.id.textView)
        private val imageView2: ImageView = itemView.findViewById(R.id.imageView2)

        fun Int.dpToPx(context: Context): Int {
            return (this * context.resources.displayMetrics.density).toInt()
        }

        private fun getAdaptiveHeights(screenHeight: Int): Pair<Int, Int> {
            val minHeight = (screenHeight * 0.15).toInt()
            val maxHeight = (screenHeight * 0.4).toInt()
            return Pair(minHeight, maxHeight)
        }

        private fun getConstrainedHeight(desiredHeight: Int, minHeight: Int, maxHeight: Int): Int {
            return desiredHeight.coerceIn(minHeight, maxHeight)
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

                Log.d(TAG, "Navigated to Tapped_Files_In_The_Container_View with " +
                        "${files.size} files, starting at index $currentIndex")
            } else {
                Log.e(TAG, "Activity is null, cannot navigate to fragment")
            }
        }

        @SuppressLint("SetTextI18n")
        fun onBind(data: OriginalPost) {
            Log.d(TAG, "image feed $absoluteAdapterPosition item count $itemCount")

            val context = itemView.context
            val displayMetrics = context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels
            val SpaceBetweenRows = 2.dpToPx(context)

            val (minHeight, maxHeight) = getAdaptiveHeights(screenHeight)
            val availableWidth = ((screenWidth - SpaceBetweenRows * 1) / 2 * 0.95f).toInt()

            val fileIdToFind = data.fileIds[absoluteAdapterPosition]
            val file = data.files.find { it.fileId == fileIdToFind }
            val imageUrl = file?.url ?: data.files.getOrNull(absoluteAdapterPosition)?.url ?: ""

            val fileSize = itemCount
            Log.d(TAG, "image getItemCount: $fileSize $imageUrl")

            materialCardView.setCardBackgroundColor(Color.TRANSPARENT)
            materialCardView.cardElevation = 0f

            val clickListener = View.OnClickListener {
                navigateToTappedFilesFragment(
                    context,
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

            itemView.setOnClickListener(clickListener)
            imageView.setOnClickListener(clickListener)
            materialCardView.setOnClickListener(clickListener)

            val layoutParams = if (materialCardView.layoutParams != null) {
                materialCardView.layoutParams as ViewGroup.MarginLayoutParams
            } else {
                ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            imageView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP

            when {

                fileSize <= 1 -> {

                    layoutParams.width = screenWidth - (SpaceBetweenRows * 2)
                    layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    resetMargins(layoutParams)
                    layoutParams.leftMargin = SpaceBetweenRows
                    layoutParams.rightMargin = SpaceBetweenRows

                    imageView.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    imageView.adjustViewBounds = true
                    imageView.scaleType = ImageView.ScaleType.FIT_CENTER

                    loadImage(context, imageUrl)

                }

                fileSize == 2 -> {
                    val desiredHeight = (maxHeight * 0.6).toInt()
                    layoutParams.width = availableWidth
                    layoutParams.height = getConstrainedHeight(desiredHeight, minHeight, maxHeight)
                    resetMargins(layoutParams)

                    when (absoluteAdapterPosition) {
                        0 -> layoutParams.rightMargin = (SpaceBetweenRows/2)
                        1 -> layoutParams.leftMargin =  (SpaceBetweenRows/2)
                    }

                    loadImage(context, imageUrl)
                }

                fileSize == 3 -> {
                    val largeImageHeight = getConstrainedHeight((maxHeight * 0.6).toInt(), minHeight, maxHeight)
                    val smallImageHeight = largeImageHeight / 2

                    when (absoluteAdapterPosition) {
                        0 -> {
                            // IMAGE 0: Left side, full height
                            layoutParams.width = availableWidth
                            layoutParams.height = largeImageHeight

                            resetMargins(layoutParams)
                            layoutParams.rightMargin =  (SpaceBetweenRows/2)
                        }

                        1, 2 -> {
                            // IMAGES 1 & 2: Right stacked images, half the height of image 0
                            layoutParams.width = availableWidth
                            layoutParams.height = smallImageHeight

                            resetMargins(layoutParams)
                            layoutParams.leftMargin =  (SpaceBetweenRows/2)

                            if (absoluteAdapterPosition == 1) {
                                layoutParams.bottomMargin = SpaceBetweenRows
                            } else {
                                layoutParams.topMargin = SpaceBetweenRows
                            }
                        }
                    }

                    // Ensure items don’t span full width
                    if (layoutParams is StaggeredGridLayoutManager.LayoutParams) {
                        layoutParams.isFullSpan = false
                    }

                    loadImage(context, imageUrl)
                }

                fileSize == 4 -> {
                    val desiredHeight = (maxHeight * 0.6).toInt()
                    val uniformHeight = getConstrainedHeight(desiredHeight, minHeight, maxHeight)

                    layoutParams.width = availableWidth
                    layoutParams.height = uniformHeight
                    resetMargins(layoutParams)

                    when (absoluteAdapterPosition) {
                        0 -> {
                            layoutParams.rightMargin = (SpaceBetweenRows/2)
                            layoutParams.bottomMargin = SpaceBetweenRows
                        }
                        1 -> {
                            layoutParams.leftMargin = (SpaceBetweenRows/2)
                            layoutParams.bottomMargin = SpaceBetweenRows
                        }
                        2 -> {
                            layoutParams.rightMargin = (SpaceBetweenRows/2)
                            layoutParams.topMargin = SpaceBetweenRows
                        }
                        3 -> {
                            layoutParams.leftMargin = (SpaceBetweenRows/2)
                            layoutParams.topMargin = SpaceBetweenRows
                        }
                    }

                    loadImage(context, imageUrl)
                }

                fileSize > 4 -> {
                    if (absoluteAdapterPosition >= 4) {
                        itemView.visibility = View.GONE
                        layoutParams.width = 0
                        layoutParams.height = 0
                        materialCardView.layoutParams = layoutParams
                        return
                    }

                    itemView.visibility = View.VISIBLE
                    val desiredHeight = (maxHeight * 0.6).toInt()
                    val uniformHeight = getConstrainedHeight(desiredHeight, minHeight, maxHeight)

                    layoutParams.width = availableWidth
                    layoutParams.height = uniformHeight
                    resetMargins(layoutParams)

                    when (absoluteAdapterPosition) {
                        0 -> {
                            layoutParams.rightMargin = (SpaceBetweenRows/2)
                            layoutParams.bottomMargin = SpaceBetweenRows
                        }
                        1 -> {
                            layoutParams.leftMargin = (SpaceBetweenRows/2)
                            layoutParams.bottomMargin = SpaceBetweenRows
                        }
                        2 -> {
                            layoutParams.rightMargin = (SpaceBetweenRows/2)
                            layoutParams.topMargin = SpaceBetweenRows
                        }
                        3 -> {
                            layoutParams.leftMargin = (SpaceBetweenRows/2)
                            layoutParams.topMargin = SpaceBetweenRows
                        }
                    }

                    if (absoluteAdapterPosition == 3) {
                        countTextView.visibility = View.VISIBLE
                        countTextView.text = "+${fileSize - 4}"
                        countTextView.textSize = 32f
                        countTextView.setPadding(12, 4, 12, 4)

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

                    loadImage(context, imageUrl)
                }
            }

            materialCardView.layoutParams = layoutParams
        }

        private fun resetMargins(layoutParams: ViewGroup.MarginLayoutParams) {
            layoutParams.leftMargin = 0
            layoutParams.rightMargin = 0
            layoutParams.topMargin = 0
            layoutParams.bottomMargin = 0
        }

        private fun loadImage(context: Context, imageUrl: String) {
            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.imageplaceholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView)
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
        private val artworkVn: ShapeableImageView = itemView.findViewById(R.id.artworkVn)

        fun Int.dpToPx(context: Context): Int {
            return (this * context.resources.displayMetrics.density).toInt()
        }

        private fun getAdaptiveHeights(context: Context): Pair<Int, Int> {
            val displayMetrics = context.resources.displayMetrics
            val screenHeight = displayMetrics.heightPixels
            val minHeight = (screenHeight * 0.18).toInt()
            val maxHeight = (screenHeight * 0.45).toInt()
            return Pair(minHeight, maxHeight)
        }

        private fun getConstrainedHeight(context: Context, preferredHeight: Int): Int {
            val (minHeight, maxHeight) = getAdaptiveHeights(context)
            return preferredHeight.coerceIn(minHeight, maxHeight)
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
                        val fileId = fileIds.getOrNull(index)
                        val fileName = data?.fileNames?.find { it.fileId == fileId }?.fileName ?: ""
                        val postItem = PostItem(
                            audioUrl = file.url,
                            audioThumbnailUrl = null,
                            videoUrl = null,
                            videoThumbnailUrl = null,
                            postId = fileId ?: "audio_file_$index",
                            data = "Audio file: $fileName",
                            files = arrayListOf(file.url),
                            fileType = "audio"
                        )
                        postItems.add(postItem)
                    }
                    putParcelableArrayList("post_list", postItems)
                    putString("post_id", fileIds.getOrNull(currentIndex) ?: "audio_file_$currentIndex")
                    putString("media_type", "audio")
                }

                fragment.arguments = bundle

                activity.supportFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                    .replace(R.id.frame_layout, fragment)
                    .addToBackStack("tapped_audio_files_view")
                    .commit()

                Log.d(TAG, "Navigated to Tapped_Files_In_The_Container_View with ${files.size} audio files, starting at index $currentIndex")
            } else {
                Log.e(TAG, "Activity is null, cannot navigate to fragment")
            }
        }

        private var data: OriginalPost? = null

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @SuppressLint("SetTextI18n")
        fun onBind(data: OriginalPost) {
            this.data = data
            val context = itemView.context
            val fileIdToFind = data.fileIds[absoluteAdapterPosition]
            val displayMetrics = context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val SpaceBetweenRows = 2.dpToPx(context)

            val (minHeight, maxHeight) = getAdaptiveHeights(context)
            val availableWidth = ((screenWidth - SpaceBetweenRows * 1) / 2 * 0.95f).toInt()
            val fullWidth = (screenWidth - SpaceBetweenRows * 2) // Full width for single items

            val layoutParams = if (materialCardView.layoutParams != null) {
                materialCardView.layoutParams as ViewGroup.MarginLayoutParams
            } else {
                ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            resetMargins(layoutParams)
            imageView2.visibility = View.GONE
            countTextView.visibility = View.GONE
            itemView.visibility = View.VISIBLE
            seekBar.visibility = View.GONE
            waveSeekBar.visibility = View.GONE

            val durationItem = data.duration?.find { it.fileId == fileIdToFind }
            if (!durationItem?.duration.isNullOrEmpty()) {
                audioDurationTextView.text = durationItem?.duration
                audioDurationTextView.visibility = View.VISIBLE
            } else {
                audioDurationTextView.visibility = View.GONE
            }

            val fileName = data.fileNames?.find { it.fileId == fileIdToFind }?.fileName ?: ""

            val clickListener = View.OnClickListener {
                navigateToTappedFilesFragment(
                    context,
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

            itemView.setOnClickListener(clickListener)
            artworkImageView.setOnClickListener(clickListener)
            materialCardView.setOnClickListener(clickListener)
            artworkLayout.setOnClickListener(clickListener)
            countTextView.setOnClickListener(clickListener)
            audioDurationTextView.setOnClickListener(clickListener)

            configureAudioUI(fileName)

            val itemCount = data.files.size

            when {
                itemCount <= 1 -> {
                    // Make single items cover full width
                    layoutParams.width = fullWidth
                    layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.6).toInt())
                    layoutParams.leftMargin = SpaceBetweenRows
                    layoutParams.rightMargin = SpaceBetweenRows
                    materialCardView.radius = 8.dpToPx(context).toFloat()
                }

                itemCount == 2 -> {
                    layoutParams.width = availableWidth
                    layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.6).toInt())
                    materialCardView.radius = 8.dpToPx(context).toFloat()

                    when (absoluteAdapterPosition) {
                        0 -> layoutParams.rightMargin = (SpaceBetweenRows/2)
                        1 -> layoutParams.leftMargin = (SpaceBetweenRows/2)
                    }
                }

                itemCount == 3 -> {
                    // Calculate total height for position 0
                    val desiredHeight = (maxHeight * 0.6).toInt()
                    val constrainedHeight = getConstrainedHeight(context, desiredHeight)

                    // Compute half-height for items 1 and 2 with spacing in mind
                    val halfHeight = (constrainedHeight - SpaceBetweenRows) / 2

                    when (absoluteAdapterPosition) {
                        0 -> {
                            layoutParams.width = availableWidth
                            layoutParams.height = constrainedHeight
                            layoutParams.rightMargin = (SpaceBetweenRows/2)
                            layoutParams.leftMargin = 0
                            layoutParams.topMargin = 0
                            layoutParams.bottomMargin = 0
                        }

                        1 -> {
                            layoutParams.width = availableWidth
                            layoutParams.height = halfHeight
                            layoutParams.leftMargin = (SpaceBetweenRows/2)
                            layoutParams.rightMargin = 0
                            layoutParams.topMargin = 0
                            layoutParams.bottomMargin = SpaceBetweenRows
                        }

                        2 -> {
                            layoutParams.width = availableWidth
                            layoutParams.height = halfHeight
                            layoutParams.leftMargin = (SpaceBetweenRows/2)
                            layoutParams.rightMargin = 0
                            layoutParams.topMargin = SpaceBetweenRows
                            layoutParams.bottomMargin = 0
                        }
                    }

                    materialCardView.radius = 8.dpToPx(context).toFloat()
                }

                itemCount == 4 -> {
                    val preferredHeight = getConstrainedHeight(context, (maxHeight * 0.6).toInt())

                    layoutParams.width = availableWidth
                    layoutParams.height = preferredHeight
                    materialCardView.radius = 6.dpToPx(context).toFloat()

                    when (absoluteAdapterPosition) {
                        0 -> {
                            layoutParams.rightMargin = (SpaceBetweenRows/2)
                            layoutParams.bottomMargin = SpaceBetweenRows
                        }
                        1 -> {
                            layoutParams.leftMargin = (SpaceBetweenRows/2)
                            layoutParams.bottomMargin = SpaceBetweenRows
                        }
                        2 -> {
                            layoutParams.rightMargin = (SpaceBetweenRows/2)
                            layoutParams.topMargin = SpaceBetweenRows
                        }
                        3 -> {
                            layoutParams.leftMargin = (SpaceBetweenRows/2)
                            layoutParams.topMargin = SpaceBetweenRows
                        }
                    }
                }

                itemCount > 4 -> {
                    if (absoluteAdapterPosition >= 4) {
                        itemView.visibility = View.GONE
                        layoutParams.width = 0
                        layoutParams.height = 0
                        materialCardView.layoutParams = layoutParams
                        return
                    }

                    itemView.visibility = View.VISIBLE
                    val preferredHeight = getConstrainedHeight(context, (maxHeight * 0.6).toInt())

                    layoutParams.width = availableWidth
                    layoutParams.height = preferredHeight
                    materialCardView.radius = 8.dpToPx(context).toFloat()

                    when (absoluteAdapterPosition) {
                        0 -> {
                            layoutParams.rightMargin = (SpaceBetweenRows/2)
                            layoutParams.bottomMargin = SpaceBetweenRows
                        }
                        1 -> {
                            layoutParams.leftMargin = (SpaceBetweenRows/2)
                            layoutParams.bottomMargin = SpaceBetweenRows
                        }
                        2 -> {
                            layoutParams.rightMargin = (SpaceBetweenRows/2)
                            layoutParams.topMargin = SpaceBetweenRows
                        }
                        3 -> {
                            layoutParams.leftMargin = (SpaceBetweenRows/2)
                            layoutParams.topMargin = SpaceBetweenRows
                        }
                    }

                    if (absoluteAdapterPosition == 3) {
                        countTextView.visibility = View.VISIBLE
                        countTextView.text = "+${itemCount - 4}"
                        countTextView.textSize = 32f
                        countTextView.setPadding(12, 4, 12, 4)

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

            materialCardView.layoutParams = layoutParams
        }


        private fun configureAudioUI(fileName: String) {
            val context = itemView.context

            // Reset all views first
            seekBar.visibility = View.GONE
            waveSeekBar.visibility = View.GONE

            // Handle different audio formats - EXACTLY like the first code
            when {
                fileName.endsWith(".mp3", true) ||
                        fileName.endsWith(".wav", true) -> {
                    // Show artwork for common audio formats with music placeholder
                    materialCardView.setCardBackgroundColor(Color.WHITE) // Set white for music files

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

                    val artworkLayoutWrapper = itemView.findViewById<MaterialCardView>(R.id.artworkLayoutWrapper)
                    artworkLayoutWrapper?.visibility = View.VISIBLE
                    artworkLayoutWrapper?.setCardBackgroundColor(Color.parseColor("#616161"))

                    // Original approach if not using wrapper:
                    artworkLayout.visibility = View.VISIBLE
                    artworkLayout.setBackgroundColor(Color.parseColor("#616161")) // Match dark_gray

                    // Ensure the mic icon (artworkVn) is properly configured with corner radius
                    artworkVn.setImageResource(R.drawable.ic_audio_white_icon)
                    artworkVn.visibility = View.VISIBLE

                    // Make sure the layout parameters are correct for centering
                    val layoutParams = artworkVn.layoutParams
                    if (layoutParams != null) {
                        layoutParams.width = 120.dpToPx(context)
                        layoutParams.height = 270.dpToPx(context)
                        artworkVn.layoutParams = layoutParams
                    }
                }

                else -> {
                    // Default case - show artwork with music placeholder
                    materialCardView.setCardBackgroundColor(Color.WHITE)

                    // Show main artwork image with music icon
                    artworkImageView.setImageResource(R.drawable.music_icon)
                    artworkImageView.visibility = View.VISIBLE
                    artworkImageView.scaleType = ImageView.ScaleType.CENTER_CROP

                    // Hide the artworkLayout
                    artworkLayout.visibility = View.GONE
                }
            }

            // Find the audioDurationLayout and keep it visible
            val audioDurationLayout = itemView.findViewById<LinearLayout>(R.id.audioDurationLayout)
            audioDurationLayout?.visibility = View.VISIBLE
        }

        private fun addPlayIconOverlay(targetImageView: ImageView) {
            val context = targetImageView.context
            val parent = targetImageView.parent as? ViewGroup ?: return

            // Remove any existing play icon overlays to avoid duplicates
            for (i in parent.childCount - 1 downTo 0) {
                val child = parent.getChildAt(i)
                if (child.tag == "play_icon_overlay_image") {
                    parent.removeView(child)
                }
            }

            // Create a FrameLayout to overlay on top of the image
            val overlayContainer = FrameLayout(context)
            val containerParams = when (parent) {
                is FrameLayout -> FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                is LinearLayout -> LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                else -> ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            overlayContainer.layoutParams = containerParams
            overlayContainer.tag = "play_icon_overlay_image"

            // Create a play icon overlay
            val playIcon = ImageView(context)
            playIcon.setImageResource(R.drawable.play_button_filled)
            playIcon.setColorFilter(Color.WHITE)
            val playIconSize = 48.dpToPx(context)
            val playLayoutParams = FrameLayout.LayoutParams(playIconSize, playIconSize)
            playLayoutParams.gravity = Gravity.CENTER
            playIcon.layoutParams = playLayoutParams

            // Add semi-transparent background to the play icon for better visibility
            val playBackground = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#80000000")) // Semi-transparent black
            }
            playIcon.background = playBackground
            playIcon.setPadding(12, 12, 12, 12)

            // Add the play icon to the overlay container
            overlayContainer.addView(playIcon)

            // Add the overlay container to the parent
            parent.addView(overlayContainer)
        }

        private fun addPlayIconOverlayToLayout(targetLayout: LinearLayout) {
            val context = targetLayout.context

            // Remove any existing play icon overlays to avoid duplicates
            for (i in targetLayout.childCount - 1 downTo 0) {
                val child = targetLayout.getChildAt(i)
                if (child.tag == "play_icon_overlay") {
                    targetLayout.removeView(child)
                }
            }

            // Create a FrameLayout to hold both the audio icon and play button
            val overlayContainer = FrameLayout(context)
            val containerParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            containerParams.gravity = Gravity.CENTER
            overlayContainer.layoutParams = containerParams

            // Create a play icon overlay
            val playIcon = ImageView(context)
            playIcon.setImageResource(R.drawable.play_button_filled)
            playIcon.setColorFilter(Color.WHITE)
            val playIconSize = 48.dpToPx(context)
            val playLayoutParams = FrameLayout.LayoutParams(playIconSize, playIconSize)
            playLayoutParams.gravity = Gravity.CENTER
            playIcon.layoutParams = playLayoutParams

            // Add semi-transparent background to the play icon for better visibility
            val playBackground = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#80000000")) // Semi-transparent black
            }
            playIcon.background = playBackground
            playIcon.setPadding(12, 12, 12, 12)
            playIcon.tag = "play_icon_overlay"

            // Add the play icon to the overlay container
            overlayContainer.addView(playIcon)

            // Add the overlay container to the target layout
            targetLayout.addView(overlayContainer)
        }

        private fun resetMargins(layoutParams: ViewGroup.MarginLayoutParams) {
            layoutParams.leftMargin = 0
            layoutParams.rightMargin = 0
            layoutParams.topMargin = 0
            layoutParams.bottomMargin = 0
        }

    }


    inner class FeedRepostVideosOnly(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun Int.dpToPx(context: Context): Int {
            return (this * context.resources.displayMetrics.density).toInt()
        }

        private val feedThumbnail: ImageView = itemView.findViewById(R.id.feedThumbnail)
        private val feedVideoDurationTextView: TextView = itemView.findViewById(R.id.feedVideoDurationTextView)
        private val cardView: CardView = itemView.findViewById(R.id.cardView)
        private val countTextView: TextView = itemView.findViewById(R.id.countTextView)
        private val imageView2: ImageView = itemView.findViewById(R.id.imageView2)

        private fun getAdaptiveHeights(screenHeight: Int): Pair<Int, Int> {
            val minHeight = (screenHeight * 0.18).toInt()
            val maxHeight = (screenHeight * 0.45).toInt()
            return Pair(minHeight, maxHeight)
        }

        private fun getConstrainedHeight(desiredHeight: Int, minHeight: Int, maxHeight: Int): Int {
            return desiredHeight.coerceIn(minHeight, maxHeight)
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

                Log.d(TAG, "Navigated to Tapped_Files_In_The_Container_View with" +
                        " ${files.size} files, starting at index $currentIndex")
            } else {
                Log.e(TAG, "Activity is null, cannot navigate to fragment")
            }
        }

        @SuppressLint("SetTextI18n", "UseKtx")
        fun onBind(data: OriginalPost) {
            Log.d(TAG, "onBind: file type Video $absoluteAdapterPosition item count $itemCount")

            val fileIdToFind = data.fileIds[absoluteAdapterPosition]
            val durationItem = data.duration.find { it.fileId == fileIdToFind }
            val thumbnail = data.thumbnail.find { it.fileId == fileIdToFind }

            feedVideoDurationTextView.text = durationItem?.duration

            val fileSize = itemCount

            val context = itemView.context
            val displayMetrics = context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels
            val margin = 2.dpToPx(context)
            val spaceBetweenRows = 2.dpToPx(context)

            val (minHeight, maxHeight) = getAdaptiveHeights(screenHeight)
            val availableWidth = ((screenWidth - spaceBetweenRows * 1) / 2 * 0.95f).toInt()

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
            cardView.radius = 8.dpToPx(context).toFloat()

            itemView.setOnClickListener {
                navigateToTappedFilesFragment(
                    context,
                    absoluteAdapterPosition,
                    data.files,
                    data.fileIds
                )

                onMultipleFilesClickListener?.multipleFileClickListener(
                    absoluteAdapterPosition,
                    data.files,
                    data.fileIds
                )
            }

            feedThumbnail.setOnClickListener {
                navigateToTappedFilesFragment(
                    context,
                    absoluteAdapterPosition,
                    data.files,
                    data.fileIds
                )
            }

            cardView.setOnClickListener {
                navigateToTappedFilesFragment(
                    context,
                    absoluteAdapterPosition,
                    data.files,
                    data.fileIds
                )
            }

            when {

                fileSize <= 1 -> {
                    val desiredHeight = (maxHeight * 0.6).toInt()
                    layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                    layoutParams.height = getConstrainedHeight(desiredHeight, minHeight, maxHeight)
                    layoutParams.leftMargin = spaceBetweenRows
                    layoutParams.rightMargin = spaceBetweenRows
                    layoutParams.topMargin = 0
                    layoutParams.bottomMargin = 0
                }

                fileSize == 2 -> {
                    val desiredHeight = (maxHeight * 0.6).toInt()
                    layoutParams.width = availableWidth
                    layoutParams.height = getConstrainedHeight(desiredHeight, minHeight, maxHeight)
                    layoutParams.topMargin = 0
                    layoutParams.bottomMargin = 0

                    val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
                    layoutParams.leftMargin = if (isLeftColumn) (spaceBetweenRows/2) else (spaceBetweenRows * 2)
                    layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows * 2) else (spaceBetweenRows/2)
                }

                fileSize == 3 -> {
                    val spanLayout = cardView.layoutParams as? StaggeredGridLayoutManager.LayoutParams

                    // Step 1: Calculate full height for position 0
                    val desiredHeight = (maxHeight * 0.6).toInt()
                    val constrainedHeight = getConstrainedHeight(desiredHeight, minHeight, maxHeight)

                    // Step 2: Calculate half height for positions 1 and 2, minus spacing adjustment
                    val halfHeight = (constrainedHeight - spaceBetweenRows) / 2

                    when (absoluteAdapterPosition) {
                        0 -> {
                            spanLayout?.isFullSpan = false
                            layoutParams.width = availableWidth
                            layoutParams.height = constrainedHeight

                            layoutParams.leftMargin = (spaceBetweenRows/2)
                            layoutParams.rightMargin = (spaceBetweenRows/2)
                            layoutParams.topMargin = 0
                            layoutParams.bottomMargin = 0
                        }

                        1 -> {
                            spanLayout?.isFullSpan = false
                            layoutParams.width = availableWidth
                            layoutParams.height = halfHeight

                            layoutParams.leftMargin = (spaceBetweenRows/2)
                            layoutParams.rightMargin = (spaceBetweenRows/2)
                            layoutParams.topMargin = 0
                            layoutParams.bottomMargin = (spaceBetweenRows)
                        }

                        2 -> {
                            spanLayout?.isFullSpan = false
                            layoutParams.width = availableWidth
                            layoutParams.height = halfHeight

                            layoutParams.leftMargin =  (spaceBetweenRows/2)
                            layoutParams.rightMargin = (spaceBetweenRows/2)
                            layoutParams.topMargin = spaceBetweenRows
                            layoutParams.bottomMargin = 0
                        }
                    }

                    cardView.layoutParams = layoutParams
                }


                fileSize == 4 -> {
                    val desiredHeight = (maxHeight * 0.6).toInt()
                    val adaptiveGridHeight = getConstrainedHeight(desiredHeight, minHeight, maxHeight)

                    layoutParams.width = availableWidth
                    layoutParams.height = adaptiveGridHeight

                    val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
                    layoutParams.leftMargin = if (isLeftColumn) (spaceBetweenRows) else (spaceBetweenRows * 2)
                    layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows * 2) else (spaceBetweenRows)

                    layoutParams.topMargin = if (absoluteAdapterPosition < 2) spaceBetweenRows else spaceBetweenRows
                    layoutParams.bottomMargin = spaceBetweenRows
                }

                else -> {
                    if (absoluteAdapterPosition >= 4) {
                        itemView.visibility = View.GONE
                        layoutParams.width = 0
                        layoutParams.height = 0
                        itemView.layoutParams = layoutParams
                        return
                    }

                    itemView.visibility = View.VISIBLE
                    val desiredHeight = (maxHeight * 0.6).toInt()
                    val adaptiveGridHeight = getConstrainedHeight(desiredHeight, minHeight, maxHeight)

                    layoutParams.width = availableWidth
                    layoutParams.height = adaptiveGridHeight

                    val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
                    layoutParams.leftMargin = if (isLeftColumn) (spaceBetweenRows) else (spaceBetweenRows * 2)
                    layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows * 2) else (spaceBetweenRows)
                    layoutParams.topMargin = if (absoluteAdapterPosition < 2) spaceBetweenRows else spaceBetweenRows
                    layoutParams.bottomMargin = spaceBetweenRows

                    if (absoluteAdapterPosition == 3) {
                        countTextView.visibility = View.VISIBLE
                        countTextView.text = "+${fileSize - 4}"
                        countTextView.textSize = 32f
                        countTextView.setPadding(12, 4, 12, 4)

                        val background = GradientDrawable().apply {
                            shape = GradientDrawable.RECTANGLE
                            cornerRadius = 16f
                            setColor("#80000000".toColorInt())
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
            val minHeight = (screenHeight * 0.20).toInt()
            val maxHeight = (screenHeight * 0.45).toInt()

            return Pair(minHeight, maxHeight)
        }

        // Helper function to constrain height within min/max bounds
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

        // Add the navigation function
        private fun navigateToTappedFilesFragment(
            context: Context,
            currentIndex: Int,
            files: List<File>,
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

        @SuppressLint("SetTextI18n", "UseKtx")
        fun onBind(data: OriginalPost) {

            val sideMargin = 2.dpToPx(itemView.context)
            val context = itemView.context // Define context properly

            Log.d(TAG, "onBind: file type Document $absoluteAdapterPosition item count $itemCount")

            val fileIdToFind = data.fileIds[absoluteAdapterPosition]
            val documentType = data.fileTypes.find { it.fileId == fileIdToFind }

            val fileSize = itemCount

            // Get adaptive heights
            val (minHeight, maxHeight) = getAdaptiveHeights(context)

            // Replace the existing click listener with this:
            itemView.setOnClickListener {
                // Navigate to the fragment
                navigateToTappedFilesFragment(
                    context,
                    absoluteAdapterPosition,
                    data.files,
                    data.fileIds
                )

                // Optional: Still call the original listener if needed
                onMultipleFilesClickListener?.multipleFileClickListener(
                    absoluteAdapterPosition,
                    data.files,
                    data.fileIds
                )
            }

            // Add click listener to the document image
            pdfImageView.setOnClickListener {
                navigateToTappedFilesFragment(
                    context,
                    absoluteAdapterPosition,
                    data.files,
                    data.fileIds
                )
            }

            // Add click listener to the document container
            documentContainer.setOnClickListener {
                navigateToTappedFilesFragment(
                    context,
                    absoluteAdapterPosition,
                    data.files,
                    data.fileIds
                )
            }

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
                        val adaptiveHeight = getConstrainedHeight(context, (maxHeight * 0.75).toInt())

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
                        val adaptiveHeight = getConstrainedHeight(context, (maxHeight * 0.65).toInt())

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
                        val adaptiveHeight = getConstrainedHeight(context, (maxHeight * 0.65).toInt())

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
        private val countTextView: TextView = itemView.findViewById(R.id.countTextView)
        private val imageView2: ImageView = itemView.findViewById(R.id.imageViewOverlay)
        private val fileTypeIcon: ImageView = itemView.findViewById(R.id.fileTypeIcon)
        private val playButton: ImageView = itemView.findViewById(R.id.playButton)
        private val feedVideoImageView: ImageView = itemView.findViewById(R.id.feedVideoImageView)
        private val feedVideoDurationTextView: TextView = itemView.findViewById(R.id.feedVideoDurationTextView)

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

        private fun getActivityFromContext(context: Context): AppCompatActivity? {
            return when (context) {
                is AppCompatActivity -> context
                is ContextWrapper -> getActivityFromContext(context.baseContext)
                else -> null
            }
        }

        // SIMPLIFIED FILE DETECTION - SAME AS FIRST CLASS
        private fun isDocument(mimeType: String): Boolean {
            return mimeType.contains("pdf") || mimeType.contains("docx") ||
                    mimeType.contains("pptx") || mimeType.contains("xlsx") ||
                    mimeType.contains("ppt") || mimeType.contains("xls") ||
                    mimeType.contains("txt") || mimeType.contains("rtf") ||
                    mimeType.contains("odt") || mimeType.contains("csv")
        }

        private fun getCorrectFileIndex(
            data: OriginalPost,
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
            files: List<File>,
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
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                    .replace(R.id.frame_layout, fragment)
                    .addToBackStack("tapped_files_view")
                    .commit()
            }
        }

        @SuppressLint("SetTextI18n", "UseKtx")
        fun onBind(data: OriginalPost) {
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

            playButton.visibility = View.GONE
            feedVideoImageView.visibility = View.GONE
            feedVideoDurationTextView.visibility = View.VISIBLE
            imageView2.visibility = View.GONE
            countTextView.visibility = View.GONE

            itemView.setOnClickListener {
                navigateToTappedFilesFragment(context, actualFileIndex, data.files, data.fileIds as List<String>)
            }
            imageView.setOnClickListener {
                navigateToTappedFilesFragment(context, actualFileIndex, data.files, data.fileIds as List<String>)
            }
            materialCardView.setOnClickListener {
                navigateToTappedFilesFragment(context, actualFileIndex, data.files, data.fileIds as List<String>)
            }
            countTextView.setOnClickListener {
                navigateToTappedFilesFragment(context, actualFileIndex, data.files, data.fileIds as List<String>)
            }
            imageView2.setOnClickListener {
                navigateToTappedFilesFragment(context, actualFileIndex, data.files, data.fileIds as List<String>)
            }
            feedVideoImageView.setOnClickListener {
                navigateToTappedFilesFragment(context, actualFileIndex, data.files, data.fileIds as List<String>)
            }

            val layoutParams = materialCardView.layoutParams as ViewGroup.MarginLayoutParams

            when {
                fileSize == 2 -> {
                    layoutParams.width = screenWidth / 2
                    layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.75).toInt())
                    layoutParams.topMargin = 0
                    layoutParams.bottomMargin = 0
                    val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
                    layoutParams.leftMargin = if (isLeftColumn) 0 else (spaceBetweenRows / 2)
                    layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows / 2) else 0
                    loadFileContent(fileUrl, mimeType, data, fileIdToFind.toString(), context)
                }

                fileSize == 3 -> {
                    val documentCount = data.fileTypes.count { isDocument(it.fileType) }

                    if (documentCount == 2) {
                        val documentIndices = data.fileTypes.mapIndexed { index, fileType ->
                            if (isDocument(fileType.fileType)) index else -1
                        }.filter { it != -1 }

                        val actualFileIndex = when (absoluteAdapterPosition) {
                            0 -> documentIndices[0]
                            1 -> documentIndices[1]
                            2 -> data.fileTypes.indices.find { !documentIndices.contains(it) } ?: 0
                            else -> absoluteAdapterPosition
                        }

                        val fileIdToFind = data.fileIds[actualFileIndex]
                        val file = data.files.find { it.fileId == fileIdToFind }
                        val fileUrl = file?.url ?: data.files.getOrNull(actualFileIndex)?.url ?: ""
                        val mimeType = data.fileTypes.getOrNull(actualFileIndex)?.fileType ?: ""
                        val durationItem = data.duration?.find { it.fileId == fileIdToFind }
                        feedVideoDurationTextView.text = durationItem?.duration

                        layoutParams.width = screenWidth / 2
                        val baseFileHeight = getConstrainedHeight(context, (maxHeight * 0.8).toInt())
                        layoutParams.height = baseFileHeight

                        when (absoluteAdapterPosition) {
                            0 -> {
                                layoutParams.leftMargin = 0
                                layoutParams.rightMargin = (spaceBetweenRows/2)
                                layoutParams.topMargin = 0
                                layoutParams.bottomMargin = 0
                            }
                            1 -> {
                                layoutParams.leftMargin = (spaceBetweenRows/2)
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
                            loadFileContent(fileUrl, mimeType, data, fileIdToFind.toString(), context)
                        }
                    } else {
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
                        loadFileContent(fileUrl, mimeType, data, fileIdToFind.toString(), context)
                    }
                }

                fileSize == 4 -> {
                    layoutParams.width = screenWidth / 2
                    layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.75).toInt())
                    layoutParams.topMargin = if (absoluteAdapterPosition < 2) 0 else spaceBetweenRows
                    layoutParams.bottomMargin = if (absoluteAdapterPosition >= 2) 0 else 0
                    val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
                    layoutParams.leftMargin = if (isLeftColumn) 0 else (spaceBetweenRows / 2)
                    layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows / 2) else 0
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
            data: OriginalPost,
            fileIdToFind: String,
            context: Context
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


