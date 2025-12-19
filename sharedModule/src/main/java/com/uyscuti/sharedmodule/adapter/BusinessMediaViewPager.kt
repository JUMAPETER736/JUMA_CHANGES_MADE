package com.uyscuti.sharedmodule.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.uyscuti.sharedmodule.R
import java.util.concurrent.TimeUnit

class BusinessMediaViewPager(
    private val context: Context,
    private var urlList: List<String>,
    private val onItemClicked: (Int) -> Unit
): RecyclerView.Adapter<BusinessMediaViewPager.BusinessMediaViewPagerViewHolder>() {

    companion object {
        private const val ITEM_MARGIN = 2 // dp
        private const val MIN_ITEM_HEIGHT_DP = 120 // dp
        private const val MAX_ITEM_HEIGHT_PERCENT = 0.35f // 35% of screen height
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BusinessMediaViewPagerViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.business_catalogue_view_pager,parent,false)
        return BusinessMediaViewPagerViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: BusinessMediaViewPagerViewHolder,
        position: Int
    ) {
        holder.bind(urlList.get(position), position)
    }

    override fun getItemCount(): Int {
        return if (urlList.size > 4) 4 else urlList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateMediaUrl(newMediaUrl: List<String>) {
        urlList = newMediaUrl
        notifyDataSetChanged()
    }

    inner class BusinessMediaViewPagerViewHolder(itemView: View):
            RecyclerView.ViewHolder(itemView) {

        private val ivMedia: ImageView = itemView.findViewById(R.id.iv_media)
        private val vvMedia: VideoView = itemView.findViewById(R.id.vv_media)
        private val ivVideoThumbnail: ImageView = itemView.findViewById(R.id.iv_video_thumbnail)
        private val ivPlayButton: ImageView = itemView.findViewById(R.id.iv_play_button)
        private val llVideoControls: LinearLayout = itemView.findViewById(R.id.ll_video_controls)
        private val ivPlayPause: ImageView = itemView.findViewById(R.id.iv_play_pause)
        private val sbProgress: SeekBar = itemView.findViewById(R.id.sb_progress)
        private val tvDuration: TextView = itemView.findViewById(R.id.tv_duration)
        private val ivMute: ImageView = itemView.findViewById(R.id.iv_mute)
        private val tvVideoIndicator: TextView = itemView.findViewById(R.id.tv_video_indicator)
        private val pbLoading: ProgressBar = itemView.findViewById(R.id.pb_loading)
        private val llErrorState: LinearLayout = itemView.findViewById(R.id.ll_error_state)
        private val tvRetry: TextView = itemView.findViewById(R.id.tv_retry)






        // Video state
        private var isPlaying = false
        private var isMuted = false
        private val handler = Handler(Looper.getMainLooper())
        private val updateProgressRunnable = object : Runnable {
            override fun run() {
                updateProgress()
                handler.postDelayed(this, 100)
            }
        }



        fun bind(mediaUrl: String, position: Int) {

            // resetting all media views
            resetViews()

            applyResponsiveLayout(position)


            // check if url contains a video
            val isVideo = isVideoUrl(mediaUrl)

            if (isVideo) {
                setupVideoView(mediaUrl)
            } else {
                setupImageView(mediaUrl)
            }

            setupClickListeners(mediaUrl,position)



        }

        private fun resetViews() {
            ivMedia.visibility = View.GONE
            vvMedia.visibility = View.GONE
            ivVideoThumbnail.visibility = View.GONE
            ivPlayButton.visibility = View.GONE
            llVideoControls.visibility = View.GONE
            tvVideoIndicator.visibility = View.GONE
            pbLoading.visibility = View.GONE
            llErrorState.visibility = View.GONE

            handler.removeCallbacks(updateProgressRunnable)
        }

        private fun applyResponsiveLayout(position: Int) {
            val context = itemView.context
            val displayMetrics = context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels
            val itemCount = urlList.size
            val position = absoluteAdapterPosition
            val marginPx = ITEM_MARGIN.dpToPx(context)

            // Calculate available width (accounting for margins)
            val availableWidth = screenWidth - (2 * marginPx)

            // Calculate dimensions based on item count
            val dimensions = calculateItemDimensions(
                itemCount = itemCount,
                position = position,
                availableWidth = availableWidth,
                screenHeight = screenHeight,
                context = context
            )

            // Apply calculated dimensions
            val layoutParams = itemView.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.width = dimensions.width
            layoutParams.height = dimensions.height

            // Set margins
           // layoutParams.setMargins(marginPx, marginPx, marginPx, marginPx)

            if(urlList.size >= 2) {

                if(urlList.size == 3) {
                    if (position == 0) {
                        layoutParams.setMargins(0, 0, marginPx, 0)
                    } else if (position == 1) {
                        layoutParams.setMargins(marginPx, 0, 0, marginPx)
                    } else if (position == 2) {
                        layoutParams.setMargins(marginPx, marginPx, 0, 0)
                    }
                } else if(urlList.size == 2) {
                    if (position == 0) {
                        layoutParams.setMargins(0, 0, marginPx, 0)
                    } else if (position == 1) {
                        layoutParams.setMargins(marginPx, 0, 0, 0)
                    }
                }

                else {

                    if (position == 0) {
                        layoutParams.setMargins(0, 0, marginPx, marginPx)
                    } else if (position == 1) {
                        layoutParams.setMargins(marginPx, 0, 0, marginPx)
                    } else if (position == 2) {
                        layoutParams.setMargins(0, marginPx, marginPx, 0)
                    } else if (position == 3) {
                        layoutParams.setMargins(marginPx, marginPx, 0, 0)
                    }
                }

            } else {
                layoutParams.setMargins(0, 0, 0, 0)
            }

            val background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadii = floatArrayOf(
                    16f, 16f, // Top-left corner (x, y radius)
                    16f, 16f, // Top-right corner (x, y radius)
                    0f, 0f,   // Bottom-right corner (x, y radius)
                    0f, 0f    // Bottom-left corner (x, y radius)
                )
                // setColor(Color.parseColor("#80000000")) // Semi-transparent black
            }

            itemView.background = background
            itemView.layoutParams = layoutParams
        }

        private fun calculateItemDimensions(
            itemCount: Int,
            position: Int,
            availableWidth: Int,
            screenHeight: Int,
            context: Context
        ): ItemDimensions {
            val marginPx = ITEM_MARGIN.dpToPx(context)
            val minHeightPx = MIN_ITEM_HEIGHT_DP.dpToPx(context)
            val maxHeightPx = (screenHeight * MAX_ITEM_HEIGHT_PERCENT).toInt()

            return when (itemCount) {
                1 -> {
                    // Single item - use full width, responsive height
                    val height = calculateSingleItemHeight(screenHeight, context)
                    ItemDimensions(availableWidth, height)
                }

                2 -> {
                    // Two items - equal height and width
                    val itemWidth = (availableWidth - marginPx) / 2
                    val itemHeight = calculateEqualItemHeight(screenHeight, context, 2)
                    ItemDimensions(itemWidth, itemHeight)
                }

                3 -> {
                    // Three items - first item spans full width, others split remaining height
                    if (position == 0) {
                        // First item spans full width
                        val height = calculateSpanningItemHeight(screenHeight, context)
                        val itemWidth = (availableWidth -marginPx) / 2
                        ItemDimensions(itemWidth, height)
                    } else {
                        // Other two items split the remaining space
                        val height = calculateSplitItemHeight(screenHeight, context)
                        val itemWidth = (availableWidth - marginPx) / 2
                        ItemDimensions(itemWidth, height)
                    }
                }

                4 -> {
                    // Four items - 2x2 grid layout
                    val itemWidth = (availableWidth - marginPx ) /2
                    val itemHeight = calculateEqualItemHeight(screenHeight, context, 4)
                    ItemDimensions(itemWidth, itemHeight)
                }

                else -> {
                    // More than 4 items - use uniform sizing
                    val itemWidth = (availableWidth - marginPx) / 2
                    val itemHeight = calculateEqualItemHeight(screenHeight, context, 4)
                    ItemDimensions(itemWidth, itemHeight)
                }
            }.let { dimensions ->
                // Ensure dimensions are within acceptable bounds
                ItemDimensions(
                    width = dimensions.width.coerceAtLeast(100), // Minimum width
                    height = dimensions.height.coerceIn(minHeightPx, maxHeightPx)
                )
            }
        }

        private fun Int.dpToPx(): Float {
            return this * context.resources.displayMetrics.density
        }

        private fun calculateSingleItemHeight(screenHeight: Int, context: Context): Int {
            return when {
                isTablet(context) -> (screenHeight * 0.45).toInt()
                screenHeight > 2000 -> (screenHeight * 0.4).toInt()
                screenHeight > 1500 -> (screenHeight * 0.35).toInt()
                else -> (screenHeight * 0.35).toInt()
            }
        }

        private fun calculateEqualItemHeight(screenHeight: Int, context: Context, itemCount: Int): Int {
            val baseHeight = when {
                isTablet(context) -> (screenHeight * 0.5).toInt()
                screenHeight > 2000 -> (screenHeight * 0.45).toInt()
                screenHeight > 1500 -> (screenHeight * 0.4).toInt()
                else -> (screenHeight * 0.4).toInt()
            }

            val marginPx = ITEM_MARGIN.dpToPx(context)

            // For 2 and 4 items, calculate height to create equal width/height ratio
            return when (itemCount) {
                2 -> {
                    // For 2 items side by side, use half the base height
                    val totalMargins = marginPx * 3 // top, middle, bottom
                    (baseHeight - totalMargins) / 2
                }
                4 -> {
                    // For 4 items in 2x2 grid, use same height as 2-item case for consistency
                    val totalMargins = marginPx * 3 // top, middle, bottom (2 rows)
                    (baseHeight - totalMargins) / 2
                }
                else -> {
                    // Fallback for other cases
                    val totalMargins = marginPx * (itemCount + 1)
                    (baseHeight - totalMargins) / itemCount
                }
            }
        }

        private fun calculateSpanningItemHeight(screenHeight: Int, context: Context): Int {
            return when {
                isTablet(context) -> (screenHeight * 0.3).toInt()
                screenHeight > 2000 -> (screenHeight * 0.25).toInt()
                screenHeight > 1500 -> (screenHeight * 0.22).toInt()
                else -> (screenHeight * 0.22).toInt()
            }
        }

        private fun calculateSplitItemHeight(screenHeight: Int, context: Context): Int {
            return when {
                isTablet(context) -> (screenHeight * 0.15).toInt()
                screenHeight > 2000 -> (screenHeight * 0.13).toInt()
                screenHeight > 1500 -> (screenHeight * 0.11).toInt()
                else -> (screenHeight * 0.11).toInt()
            }
        }

        private fun calculateUniformItemHeight(screenHeight: Int, context: Context, itemCount: Int): Int {
            val baseHeight = when {
                isTablet(context) -> (screenHeight * 0.6).toInt()
                screenHeight > 2000 -> (screenHeight * 0.5).toInt()
                screenHeight > 1500 -> (screenHeight * 0.45).toInt()
                else -> (screenHeight * 0.45).toInt()
            }

            // Divide available height by number of items, accounting for margins
            val marginPx = ITEM_MARGIN.dpToPx(context)
            val totalMargins = marginPx * (itemCount + 1)
            return (baseHeight - totalMargins) / itemCount
        }

        private fun isTablet(context: Context): Boolean {
            val displayMetrics = context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            return screenWidth > 600.dpToPx(context)
        }

        private fun updateProgress() {
            if (vvMedia.isPlaying) {
                val currentPosition = vvMedia.currentPosition
                sbProgress.progress = currentPosition
            }
        }

        private fun isVideoUrl(url: String): Boolean {
            val videoExtensions = listOf("mp4", "avi", "mov", "wmv", "flv", "webm", "mkv")
            val extension = url.substringAfterLast('.', "").lowercase()
            return videoExtensions.contains(extension)
        }

        private fun setupImageView(mediaItem: String) {
            ivMedia.visibility = View.VISIBLE
            pbLoading.visibility = View.VISIBLE

            Glide.with(context)
                .load(mediaItem)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(ivMedia)

            pbLoading.visibility = View.GONE
        }

        private fun setupVideoView(mediaItem: String) {
            tvVideoIndicator.visibility = View.VISIBLE
            ivVideoThumbnail.visibility = View.VISIBLE
            ivPlayButton.visibility = View.VISIBLE
           // pbLoading.visibility = View.VISIBLE

            // Load video thumbnail
            loadVideoThumbnail(mediaItem)

            // Setup VideoView
            vvMedia.setVideoURI(Uri.parse(mediaItem))
            vvMedia.setOnPreparedListener { mediaPlayer ->
                pbLoading.visibility = View.GONE
                mediaPlayer.isLooping = true
                mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)

                // Set duration
                val duration = mediaPlayer.duration
                tvDuration.text = formatDuration(duration)
                sbProgress.max = duration

                // Handle mute state
                if (isMuted) {
                    mediaPlayer.setVolume(0f, 0f)
                }
            }

            vvMedia.setOnErrorListener { _, _, _ ->
                pbLoading.visibility = View.GONE
                showErrorState()
                true
            }

            vvMedia.setOnCompletionListener {
                isPlaying = false
                updatePlayPauseButton()
                handler.removeCallbacks(updateProgressRunnable)
            }
        }

        private fun updatePlayPauseButton() {
            ivPlayPause.setImageResource(
                if (isPlaying) R.drawable.baseline_pause_black else R.drawable.ic_play
            )
            ivPlayButton.visibility = if (isPlaying) View.GONE else View.VISIBLE
        }

        private fun showErrorState() {
            llErrorState.visibility = View.VISIBLE
            ivVideoThumbnail.visibility = View.GONE
            ivPlayButton.visibility = View.GONE
        }


        private fun loadVideoThumbnail(videoUrl: String) {
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(videoUrl, HashMap<String, String>())
                val bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                retriever.release()

                if (bitmap != null) {
                    ivVideoThumbnail.setImageBitmap(bitmap)
                } else {
                    // Fallback to Glide for thumbnail
                    Glide.with(context)
                        .load(videoUrl)
                        .centerCrop()
                        .into(ivVideoThumbnail)
                }
            } catch (e: Exception) {
                // Fallback to Glide
                Glide.with(context)
                    .load(videoUrl)
                    .centerCrop()
                    .into(ivVideoThumbnail)
            }
        }

        private fun formatDuration(duration: Int): String {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(duration.toLong())
            val seconds = TimeUnit.MILLISECONDS.toSeconds(duration.toLong()) % 60
            return String.format("%02d:%02d", minutes, seconds)
        }

        private fun updateMuteButton() {
            ivMute.setImageResource(
                if (isMuted) R.drawable.ic_volume_off else R.drawable.ic_volume_up
            )
        }

        private fun updateVideoViewVisibility() {
            if (isPlaying) {
                vvMedia.visibility = View.VISIBLE
                ivVideoThumbnail.visibility = View.GONE
            } else {
                vvMedia.visibility = View.GONE
                ivVideoThumbnail.visibility = View.VISIBLE
            }
        }

        private fun showVideoControls() {
            llVideoControls.visibility = View.VISIBLE
            // Auto-hide controls after 3 seconds
            handler.postDelayed({
                if (isPlaying) {
                    llVideoControls.visibility = View.GONE
                }
            }, 3000)
        }

        fun onPause() {
            if (isPlaying) {
                vvMedia.pause()
                handler.removeCallbacks(updateProgressRunnable)
            }
        }

        fun onResume() {
            if (isPlaying) {
                vvMedia.start()
                handler.post(updateProgressRunnable)
            }
        }

        private fun setupClickListeners(mediaItem: String, position: Int) {

            ivVideoThumbnail.setOnClickListener { onItemClicked(position) }

            ivMedia.setOnClickListener { onItemClicked(position) }

           // ivPlayPause.setOnClickListener { onItemClicked(position) }

            ivPlayButton.setOnClickListener { onItemClicked(position) }


            if (isVideoUrl(mediaItem)) {

                // ivVideoThumbnail.setOnClickListener { togglePlayPause() }
                vvMedia.setOnClickListener {
                    llVideoControls.visibility = if (llVideoControls.visibility == View.VISIBLE)
                        View.GONE else View.VISIBLE
                }

                ivMute.setOnClickListener { toggleMute() }

                sbProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            vvMedia.seekTo(progress)
                        }
                    }
                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                })

                tvRetry.setOnClickListener {
                    llErrorState.visibility = View.GONE
                    pbLoading.visibility = View.VISIBLE
                    vvMedia.setVideoURI(Uri.parse(mediaItem))
                    vvMedia.start()
                }
            } else {
               // ivMedia.setOnClickListener { onVideoClick(mediaItem) }
            }
        }

        private fun togglePlayPause() {
            if (isPlaying) {
                vvMedia.pause()
                handler.removeCallbacks(updateProgressRunnable)
            } else {
                vvMedia.start()
                handler.post(updateProgressRunnable)
                showVideoControls()
            }
            isPlaying = !isPlaying
            updatePlayPauseButton()
            updateVideoViewVisibility()
        }

        private fun toggleMute() {
            isMuted = !isMuted
            if (vvMedia.isPlaying) {
                val mediaPlayer = vvMedia.tag as? MediaPlayer
                mediaPlayer?.setVolume(if (isMuted) 0f else 1f, if (isMuted) 0f else 1f)
            }
            updateMuteButton()
        }
    }

    fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }

    // Data class for item dimensions
    data class ItemDimensions(
        val width: Int,
        val height: Int
    )
}