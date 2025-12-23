package com.uyscuti.social.circuit.feed_demo

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.adapter.feed.multiple_files.MultipleFeedFilesPagerAdapter
import com.uyscuti.social.circuit.databinding.ActivityFullScreenAnyFileBinding
import com.uyscuti.social.circuit.model.feed.FeedMultipleImages
import com.uyscuti.social.circuit.model.feed.multiple_files.FeedMultipleAudios
import com.uyscuti.social.circuit.model.feed.multiple_files.FeedMultipleDocumentsDataClass
import com.uyscuti.social.circuit.model.feed.multiple_files.FeedMultipleVideos
import com.uyscuti.social.circuit.model.feed.multiple_files.MixedFeedUploadDataClass
import com.uyscuti.social.circuit.utils.AudioDurationHelper.getVideoDuration
import com.uyscuti.social.circuit.utils.generateRandomId
import java.io.File
import kotlin.math.max
import androidx.core.net.toUri

class AnyFileFullScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullScreenAnyFileBinding
    private var mediaPagerAdapter: MultipleFeedFilesPagerAdapter? = null
    private lateinit var viewPager: ViewPager2
    private lateinit var videoDurationText: TextView
    private lateinit var closeButton: ImageView
    private lateinit var fullScreenPlayPauseButton: ImageView
    private lateinit var audioIcon: ImageView
    private lateinit var bottomShortsVideoProgressSeekBar: SeekBar
    private var timerHandler: Handler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null
    private var playPauseHideRunnable: Runnable? = null
    private var isTimerRunning = false
    private var isPlaying = true
    private var totalDuration: Long = 0
    private var currentPosition: Long = 0
    private var isUserSeeking = false
    private var isActivityReady = false
    private lateinit var removeButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullScreenAnyFileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        viewPager = binding.viewPager

        val mediaUrls = intent.getStringArrayListExtra("imageUrls") ?: ArrayList()
        val startPosition = intent.getIntExtra("position", 0)
        val videoThumbnails = intent.getStringArrayListExtra("videoThumbnails")

        Log.d(TAG, "onCreate: mediaUrls count=${mediaUrls.size}, startPosition=$startPosition")

        if (mediaUrls.isEmpty()) {
            Log.e(TAG, "No media URLs provided")
            finish()
            return
        }

        // Handle all content through media viewer (including documents)
        setupMediaViewer(mediaUrls, startPosition, videoThumbnails)
    }

    // Update the setupMediaViewer function to include documents
    private fun setupMediaViewer(
        allUrls: List<String>,
        startPosition: Int,
        videoThumbnails: List<String>?
    ) {
        // Include documents in the filtered list - DON'T filter them out
        val mediaOnlyUrls = allUrls.filter { url ->
            getFileType(url) in listOf("image", "video", "audio", "audio_vn", "document")
        }

        if (mediaOnlyUrls.isEmpty()) {
            Toast.makeText(this, "No files to display", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Calculate adjusted start position for media list
        var adjustedStartPosition = 0
        val originalClickedUrl = if (startPosition < allUrls.size) allUrls[startPosition] else null

        if (originalClickedUrl != null) {
            // Find the position in the filtered list
            adjustedStartPosition = mediaOnlyUrls.indexOf(originalClickedUrl)
            if (adjustedStartPosition == -1) adjustedStartPosition = 0
        }

        Log.d(TAG, "setupMediaViewer: mediaOnlyUrls count=${mediaOnlyUrls.size}, adjustedStartPosition=$adjustedStartPosition")

        // Create media items for ViewPager
        val mediaItems = mediaOnlyUrls.mapIndexed { index, url ->
            val fileType = getFileType(url)
            val originalIndex = allUrls.indexOf(url)

            MixedFeedUploadDataClass(
                images = if (fileType == "image") {
                    FeedMultipleImages(imagePath = url, compressedImagePath = "")
                } else null,

                videos = if (fileType == "video") {
                    val thumbnailPath = videoThumbnails?.getOrNull(originalIndex)
                    FeedMultipleVideos(
                        videoPath = url,
                        thumbnail = thumbnailPath?.let { path ->
                            try {
                                Glide.with(this).asBitmap().load(File(path)).submit().get()
                            } catch (e: Exception) {
                                Log.e(TAG, "Error loading thumbnail: ${e.message}")
                                null
                            }
                        },
                        videoDuration = try {
                            getVideoDuration(url).toString()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error getting video duration: ${e.message}")
                            "0"
                        },
                        fileName = url.substringAfterLast("/"),
                        videoUri = Uri.parse(url).toString()
                    )
                } else null,

                audios = if (fileType == "audio" || fileType == "audio_vn") {
                    FeedMultipleAudios(audioPath = url)
                } else null,

                documents = if (fileType == "document") {
                    FeedMultipleDocumentsDataClass(
                        uri = Uri.parse(url),
                        filename = url.substringAfterLast("/"),
                        documentType = getFileType(url),
                        pdfFilePath = url,
                        uriFile = File(url)
                    )
                } else null,

                fileId = generateRandomId(),
                fileTypes = fileType
            )
        }

        if (mediaItems.isEmpty()) {
            Log.e(TAG, "No valid media items created")
            finish()
            return
        }

        // Setup ViewPager
        mediaPagerAdapter = MultipleFeedFilesPagerAdapter(this, isFullScreen = true).apply {
            setMixedFeedUploadDataClass(mediaItems)
        }

        binding.viewPager.adapter = mediaPagerAdapter
        binding.viewPager.setCurrentItem(adjustedStartPosition, false)

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val currentItem = mediaPagerAdapter?.getItem(position)
                Log.d(TAG, "onPageSelected: position=$position, fileId=${currentItem?.fileId}, fileType=${currentItem?.fileTypes}")
                if (isActivityReady && !isDestroyed && !isFinishing) {
                    try {
                        pauseCurrentlyPlayingMedia()
                        checkCurrentMediaType()
                    } catch (e: Exception) {
                        Log.e(TAG, "Page change error: ${e.message}")
                    }
                }
            }
        })

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupUI()
        setupVideoControls()
        setupCloseButton()
        setupTapGesture()
        setupRemoveButton()

        isActivityReady = true

        // Start playing if it's media
        binding.viewPager.post {
            if (!isDestroyed && !isFinishing) {
                checkCurrentMediaType()
            }
        }
    }

    private fun getFileType(url: String): String {
        return when {
            url.matches(Regex(".*\\.(jpg|jpeg|png|gif|bmp|webp)$", RegexOption.IGNORE_CASE)) -> "image"
            url.matches(Regex(".*\\.(mp4|avi|mov|mkv|webm|3gp)$", RegexOption.IGNORE_CASE)) -> "video"
            url.matches(Regex(".*\\.(mp3|wav|flac|aac|m4a)$", RegexOption.IGNORE_CASE)) -> "audio"
            url.matches(Regex(".*\\.(ogg)$", RegexOption.IGNORE_CASE)) -> "audio_vn"
            url.matches(Regex(".*\\.(pdf|doc|docx|xls|xlsx|ppt|pptx|txt|csv|rtf|zip|rar)$", RegexOption.IGNORE_CASE)) -> "document"
            else -> "unknown"
        }
    }

    // Add this method to get document thumbnail:
    private fun getDocumentThumbnail(
        documentUrl: String,
        allUrls: List<String>,
        videoThumbnails: List<String>?
    ): String? {
        val documentIndex = allUrls.indexOf(documentUrl)
        return if (documentIndex != -1 && videoThumbnails != null && documentIndex < videoThumbnails.size) {
            videoThumbnails[documentIndex]
        } else {
            null
        }
    }

    // Replace your openDocument method with this:
    private fun openDocumentWithExternalApp(documentUrl: String) {
        Log.d(TAG, "openDocumentWithExternalApp: Attempting to open $documentUrl")

        val file = File(documentUrl)
        val uri = if (file.exists()) {
            FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )
        } else {
            Uri.parse(documentUrl)
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, getMimeType(documentUrl))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            // Create chooser to let user select app
            val chooser = Intent.createChooser(intent, "Open document with...")
            startActivity(chooser)
            Log.d(TAG, "openDocumentWithExternalApp: Successfully launched chooser for $documentUrl")
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "openDocumentWithExternalApp: No app found to open document: ${e.message}")
            Toast.makeText(this, "No app found to open this document", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "openDocumentWithExternalApp: Error opening document: ${e.message}")
            Toast.makeText(this, "Error opening document: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Add this helper method for MIME types:
    private fun getMimeType(url: String): String {
        return when {
            url.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
            url.endsWith(".doc", ignoreCase = true) -> "application/msword"
            url.endsWith(".docx", ignoreCase = true) -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            url.endsWith(".xls", ignoreCase = true) -> "application/vnd.ms-excel"
            url.endsWith(".xlsx", ignoreCase = true) -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            url.endsWith(".ppt", ignoreCase = true) -> "application/vnd.ms-powerpoint"
            url.endsWith(".pptx", ignoreCase = true) -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            url.endsWith(".txt", ignoreCase = true) -> "text/plain"
            else -> "*/*"
        }
    }

    @SuppressLint("UseKtx")
    private fun openDocument(documentUrl: String) {
        Log.d(TAG, "openDocument: Attempting to open $documentUrl")
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            startActivity(intent)
            Log.d(TAG, "openDocument: Successfully launched intent for $documentUrl")
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "openDocument: No app found to open document: ${e.message}")
            Toast.makeText(this, "No app found to open this document", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "openDocument: Error opening document: ${e.message}")
            Toast.makeText(this, "Error opening document: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("DefaultLocale")
    private fun updateDurationDisplay() {
        val remainingTime = maxOf(0, totalDuration - currentPosition)
        val seconds = (remainingTime / 1000) % 60
        val minutes = (remainingTime / (1000 * 60)) % 60
        val timeString = String.format("-%02d:%02d", minutes, seconds)
        videoDurationText.text = timeString
    }

    private fun startTimer() {
        if (isTimerRunning) return
        isTimerRunning = true
        timerRunnable = object : Runnable {
            override fun run() {
                if (!isDestroyed && !isFinishing && isTimerRunning && mediaPagerAdapter != null) {
                    try {
                        val currentItem = mediaPagerAdapter?.getItem(viewPager.currentItem)
                        if (currentItem?.fileTypes in listOf("video", "audio", "audio_vn")) {
                            currentPosition = when (currentItem?.fileTypes) {
                                "video" -> mediaPagerAdapter?.getCurrentVideoPosition(viewPager.currentItem) ?: 0L
                                "audio", "audio_vn" -> mediaPagerAdapter?.getCurrentAudioPosition(viewPager.currentItem) ?: 0L
                                else -> 0L
                            }

                            totalDuration = when (currentItem?.fileTypes) {
                                "video" -> mediaPagerAdapter?.getVideoDuration(viewPager.currentItem) ?: 0L
                                "audio", "audio_vn" -> mediaPagerAdapter?.getAudioDuration(viewPager.currentItem) ?: 0L
                                else -> 0L
                            }

                            if (totalDuration > 0 && !isUserSeeking) {
                                val progress = ((currentPosition * 100L) / totalDuration).toInt()
                                bottomShortsVideoProgressSeekBar.progress = progress
                                updateDurationDisplay()
                            }
                        }
                        if (isTimerRunning) {
                            timerHandler.postDelayed(this, 100)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Timer error: ${e.message}")
                    }
                }
            }
        }
        timerRunnable?.let { timerHandler.post(it) }
    }

    private fun setupVideoAudioControls() {
        videoDurationText.visibility = View.VISIBLE
        bottomShortsVideoProgressSeekBar.visibility = View.VISIBLE
        isPlaying = true
        mediaPagerAdapter?.getItem(viewPager.currentItem)?.let { item ->
            when (item.fileTypes) {
                "video" -> {
                    audioIcon.visibility = View.GONE
                    mediaPagerAdapter?.playVideo(viewPager.currentItem)
                    Log.d(TAG, "setupVideoAudioControls: Playing video for fileId=${item.fileId}")
                }
                "audio" -> {
                    audioIcon.setImageResource(R.drawable.ic_audio_white_icon)
                    audioIcon.visibility = View.VISIBLE
                    mediaPagerAdapter?.playAudio(viewPager.currentItem)
                    Log.d(TAG, "setupVideoAudioControls: Playing audio for fileId=${item.fileId}")
                }
                "audio_vn" -> {
                    audioIcon.setImageResource(R.drawable.ic_audio_white_icon)
                    audioIcon.visibility = View.VISIBLE
                    audioIcon.scaleX = 2.0f
                    audioIcon.scaleY = 2.0f
                    audioIcon.parent?.let { parent ->
                        if (parent is View) {
                            parent.setBackgroundColor(Color.BLACK)
                        }
                    }
                    mediaPagerAdapter?.playAudio(viewPager.currentItem)
                    Log.d(TAG, "setupVideoAudioControls: Playing audio_vn for fileId=${item.fileId}")
                }
            }
        }
    }

    private fun setupTapGesture() {
        viewPager.setOnClickListener {
            val currentItem = mediaPagerAdapter?.getItem(viewPager.currentItem)
            when (currentItem?.fileTypes) {
                "video", "audio", "audio_vn" -> {
                    if (isPlaying) {
                        pauseCurrentlyPlayingMedia()
                        showPauseIcon()
                    } else {
                        playCurrentlyPlayingMedia()
                        showPlayIcon()
                    }
                }
                "document" -> {
                    // Handle document tap - open with external app
                    currentItem.documents?.let { doc ->
                        openDocumentWithExternalApp(doc.pdfFilePath)
                    }
                }
            }
        }
    }

    private fun showPauseIcon() {
        fullScreenPlayPauseButton.setImageResource(R.drawable.baseline_pause_white_24)
        showPlayPauseButton()
    }

    private fun showPlayIcon() {
        fullScreenPlayPauseButton.setImageResource(R.drawable.baseline_play_arrow_24)
        showPlayPauseButton()
    }

    private fun showPlayPauseButton() {
        fullScreenPlayPauseButton.visibility = View.VISIBLE
        fullScreenPlayPauseButton.alpha = 1.0f
        playPauseHideRunnable?.let { timerHandler.removeCallbacks(it) }
        playPauseHideRunnable = Runnable {
            if (!isDestroyed && !isFinishing) {
                fullScreenPlayPauseButton.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction {
                        if (!isDestroyed && !isFinishing) {
                            fullScreenPlayPauseButton.visibility = View.GONE
                        }
                    }
                    .start()
            }
        }
        timerHandler.postDelayed(playPauseHideRunnable!!, 5000)
    }

    private fun setupUI() {
        videoDurationText = binding.videoDurationText
        fullScreenPlayPauseButton = binding.fullScreenPlayPauseButton
        audioIcon = binding.audioIcon
        bottomShortsVideoProgressSeekBar = binding.bottomShortsVideoProgressSeekBar
        viewPager = binding.viewPager

        removeButton = binding.removeButton

        val mediaItems = mediaPagerAdapter?.getAllItems() ?: emptyList()
        val dotsIndicator: DotsIndicator = binding.wormDotsIndicator
        if (mediaItems.size < 2) {
            dotsIndicator.visibility = View.GONE
        } else {
            dotsIndicator.attachTo(viewPager)
        }
    }

    private fun checkCurrentMediaType() {
        val currentItem = mediaPagerAdapter?.getItem(viewPager.currentItem)
        Log.d(TAG, "checkCurrentMediaType: currentItem=${currentItem?.fileTypes}")
        when (currentItem?.fileTypes) {
            "video", "audio", "audio_vn" -> {
                setupVideoAudioControls()
                startTimer()
            }
            "document" -> {
                // Documents don't need playback controls
                audioIcon.visibility = View.GONE
                hideVideoControls()
                stopTimer()
            }
            else -> {
                audioIcon.visibility = View.GONE
                hideVideoControls()
                stopTimer()
            }
        }
    }

    private fun setupVideoControls() {
        bottomShortsVideoProgressSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val currentItem = mediaPagerAdapter?.getItem(viewPager.currentItem)
                    if (currentItem?.fileTypes in listOf("video", "audio", "audio_vn")) {
                        totalDuration = when (currentItem?.fileTypes) {
                            "video" -> mediaPagerAdapter?.getVideoDuration(viewPager.currentItem) ?: 0L
                            "audio", "audio_vn" -> mediaPagerAdapter?.getAudioDuration(viewPager.currentItem) ?: 0L
                            else -> 0L
                        }

                        if (totalDuration > 0) {
                            val newPosition = (totalDuration * progress.toLong() / 100L)
                            currentPosition = newPosition
                            updateDurationDisplay()

                            when (currentItem?.fileTypes) {
                                "video" -> mediaPagerAdapter?.seekVideo(viewPager.currentItem, newPosition)
                                "audio", "audio_vn" -> mediaPagerAdapter?.seekAudio(viewPager.currentItem, newPosition)
                            }
                        }
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = false
            }
        })

        fullScreenPlayPauseButton.setOnClickListener {
            togglePlayPause()
        }
    }

    private fun hideVideoControls() {
        videoDurationText.visibility = View.GONE
        bottomShortsVideoProgressSeekBar.visibility = View.GONE
        fullScreenPlayPauseButton.visibility = View.GONE
    }

    private fun togglePlayPause() {
        if (isPlaying) {
            pauseCurrentlyPlayingMedia()
            showPauseIcon()
        } else {
            playCurrentlyPlayingMedia()
            showPlayIcon()
        }
    }

    private fun playCurrentlyPlayingMedia() {
        val currentItem = mediaPagerAdapter?.getItem(viewPager.currentItem)
        if (currentItem?.fileTypes in listOf("video", "audio", "audio_vn")) {
            isPlaying = true
            when (currentItem?.fileTypes) {
                "video" -> mediaPagerAdapter?.playVideo(viewPager.currentItem)
                "audio", "audio_vn" -> mediaPagerAdapter?.playAudio(viewPager.currentItem)
            }
            Log.d(TAG, "playCurrentlyPlayingMedia: Playing ${currentItem?.fileTypes} for fileId=${currentItem?.fileId}")
            startTimer()
        }
    }

    private fun pauseCurrentlyPlayingMedia() {
        val currentItem = mediaPagerAdapter?.getItem(viewPager.currentItem)
        if (currentItem?.fileTypes in listOf("video", "audio", "audio_vn")) {
            isPlaying = false
            when (currentItem?.fileTypes) {
                "video" -> mediaPagerAdapter?.pauseVideo(viewPager.currentItem)
                "audio", "audio_vn" -> mediaPagerAdapter?.pauseAudio(viewPager.currentItem)
            }
            Log.d(TAG, "pauseCurrentlyPlayingMedia: Pausing ${currentItem?.fileTypes} for fileId=${currentItem?.fileId}")
            stopTimer()
        }
    }

    private fun setupCloseButton() {
        closeButton = binding.closeButton
        closeButton.setOnClickListener {
            Log.d(TAG, "Close button clicked")
            finish()
        }
    }

    private fun setupRemoveButton() {
        removeButton.setOnClickListener {
            Log.d(TAG, "Remove button clicked")
            removeCurrentItem()
        }
    }

    private fun removeCurrentItem() {
        val currentPosition = viewPager.currentItem
        val adapter = mediaPagerAdapter ?: return

        if (adapter.itemCount <= 1) {
            // If this is the last item, close the activity
            finish()
            return
        }

        try {
            // Pause current media before removing
            pauseCurrentlyPlayingMedia()
            stopTimer()

            // Remove item from adapter
            adapter.removeItem(currentPosition)

            // Update dots indicator visibility
            val dotsIndicator: DotsIndicator = binding.wormDotsIndicator
            if (adapter.itemCount < 2) {
                dotsIndicator.visibility = View.GONE
            }

            // Adjust current position if needed
            val newPosition = if (currentPosition >= adapter.itemCount) {
                adapter.itemCount - 1
            } else {
                currentPosition
            }

            // Set new current item and check media type
            viewPager.setCurrentItem(newPosition, false)
            viewPager.post {
                if (!isDestroyed && !isFinishing && isActivityReady) {
                    checkCurrentMediaType()
                }
            }

            Log.d(TAG, "removeCurrentItem: Removed item at position $currentPosition, new position: $newPosition, remaining items: ${adapter.itemCount}")

        } catch (e: Exception) {
            Log.e(TAG, "removeCurrentItem error: ${e.message}")
            Toast.makeText(this, "Error removing item", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopTimer() {
        isTimerRunning = false
        timerRunnable?.let { timerHandler.removeCallbacks(it) }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause called")
        try {
            pauseCurrentlyPlayingMedia()
        } catch (e: Exception) {
            Log.e(TAG, "onPause error: ${e.message}")
        }
        stopTimer()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called")
        viewPager.postDelayed({
            if (!isDestroyed && !isFinishing && isActivityReady) {
                try {
                    val currentItem = mediaPagerAdapter?.getItem(viewPager.currentItem)
                    if (currentItem?.fileTypes in listOf("video", "audio", "audio_vn")) {
                        playCurrentlyPlayingMedia()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "onResume error: ${e.message}")
                }
            }
        }, 100)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy called")
        isActivityReady = false
        try {
            mediaPagerAdapter?.releaseAllPlayers()
        } catch (e: Exception) {
            Log.e(TAG, "onDestroy error: ${e.message}")
        }
        stopTimer()
        playPauseHideRunnable?.let { timerHandler.removeCallbacks(it) }
    }

    companion object {
        private const val TAG = "AnyFileFullScreenActivity"
    }
}