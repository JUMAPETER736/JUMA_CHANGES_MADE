package com.uyscuti.social.circuit.feed_demo

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.pdf.PdfRenderer
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.chrisbanes.photoview.PhotoView
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.adapter.feed.multiple_files.MultipleFeedFilesPagerAdapter
import com.uyscuti.social.circuit.databinding.FragmentMixedFeedFilesUploadBinding
import com.uyscuti.social.circuit.interfaces.feedinterfaces.FeedTextViewFragmentInterface
import com.uyscuti.social.circuit.model.feed.FeedMultipleImages
import com.uyscuti.social.circuit.model.feed.multiple_files.FeedMultipleVideos
import com.uyscuti.social.circuit.model.feed.multiple_files.MixedFeedUploadDataClass
import com.uyscuti.social.circuit.model.feed.multiple_files.FeedMultipleDocumentsDataClass
import id.zelory.compressor.Compressor
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min

private const val TAG = "MixedFeedFilesUploadFragment"

class MixedFeedFilesUploadFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(mixedFeedUploadDataClass: MixedFeedUploadDataClass, isFullScreen: Boolean = false) =
            MixedFeedFilesUploadFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("mixedFeedUploadDataClass", mixedFeedUploadDataClass)
                    putBoolean("isFullScreen", isFullScreen)
                }
            }
    }

    private var _binding: FragmentMixedFeedFilesUploadBinding? = null
    private val binding get() = _binding!!
    private var mixedFeedUploadDataClass: MixedFeedUploadDataClass? = null
    private var feedTextViewFragmentInterface: FeedTextViewFragmentInterface? = null
    private var compressedImageFile: File? = null
    private var anyFileMediaPagerAdapter: AnyFileMediaPagerAdapter? = null

    private var isFullScreen = false

    // Add these properties at the top of the class after existing properties
    private var audioMediaPlayer: MediaPlayer? = null
    private var videoMediaPlayer: MediaPlayer? = null
    private var isAudioPlaying = false
    private var isVideoPlaying = false

    private var allMediaItems: List<MixedFeedUploadDataClass> = emptyList()
    private var currentPosition: Int = 0

    // Zoom and gesture detection properties
    private var scaleGestureDetector: ScaleGestureDetector? = null
    private var gestureDetector: GestureDetector? = null
    private var scaleFactor = 1.0f
    private var focusX = 0f
    private var focusY = 0f
    private var isZoomed = false

    // UPDATED: Remove unused properties and add new ones
    private var minScaleFactor = 1.0f  // This will be the fit-to-screen scale
    private var baseScaleFactor = 1.0f  // The scale factor when image first fits screen

    // For pan gesture when zoomed
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var posX = 0f
    private var posY = 0f

    private var originalImageWidth = 0f
    private var originalImageHeight = 0f
    private var displayedImageWidth = 0f
    private var displayedImageHeight = 0f
    private var maxAllowedScale = 3.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mixedFeedUploadDataClass = it.getParcelable("mixedFeedUploadDataClass")
            isFullScreen = it.getBoolean("isFullScreen")
        }
        Log.d(TAG,
            "onCreate: fileId=${mixedFeedUploadDataClass?.fileId}," +
                    " fileType=${mixedFeedUploadDataClass?.fileTypes}")
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMixedFeedFilesUploadBinding.inflate(inflater,
            container, false)

        if (isFullScreen) {
            setupZoomGestures()
        }

        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupZoomGestures() {
        // Setup scale gesture detector for pinch-to-zoom
        scaleGestureDetector = ScaleGestureDetector(requireContext(),
            ScaleGestureListener())

        // Setup gesture detector for double-tap
        gestureDetector = GestureDetector(
            requireContext(),
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    Log.d(TAG, "onDoubleTap: Double tap detected at (${e.x}, ${e.y})")
                    handleDoubleTap(e.x, e.y)
                    return true
                }

                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    handleSingleTap()
                    return true
                }
            })

        // Set touch listener on the root view
        binding.root.setOnTouchListener { view, event ->
            var handled = false

            scaleGestureDetector?.onTouchEvent(event)?.let { scaleHandled ->
                handled = scaleHandled
            }

            gestureDetector?.onTouchEvent(event)?.let { gestureHandled ->
                handled = handled || gestureHandled
            }

            // UPDATED: Check against minScaleFactor instead of just isZoomed
            if (scaleFactor > minScaleFactor && !scaleGestureDetector!!.isInProgress) {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        lastTouchX = event.x
                        lastTouchY = event.y
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = event.x - lastTouchX
                        val dy = event.y - lastTouchY

                        posX += dx
                        posY += dy

                        limitPanning()
                        applyTransformation()

                        lastTouchX = event.x
                        lastTouchY = event.y
                        handled = true
                    }
                }
            }

            handled
        }
    }

    // UPDATED: Add minimum scale factor calculation
    private fun calculateMaxAllowedScale() {
        mixedFeedUploadDataClass?.let { data ->
            when (data.fileTypes) {
                "image" -> {
                    calculateImageMaxScale()
                }
                "document" -> {
                    calculateDocumentMaxScale()
                }
            }
        }
    }

    private fun calculateImageMaxScale() {
        // FIXED: Check if binding is still valid
        if (_binding == null || !isAdded) {
            Log.w(TAG, "calculateImageMaxScale: Fragment not in valid state, skipping")
            return
        }

        try {
            val drawable = binding.feedUploadImageView.drawable
            if (drawable != null) {
                // Get original image dimensions
                originalImageWidth = drawable.intrinsicWidth.toFloat()
                originalImageHeight = drawable.intrinsicHeight.toFloat()

                // Get ImageView dimensions
                val viewWidth = binding.feedUploadImageView.width.toFloat()
                val viewHeight = binding.feedUploadImageView.height.toFloat()

                if (viewWidth > 0 && viewHeight > 0 && originalImageWidth > 0 && originalImageHeight > 0) {
                    // Calculate how the image fits in the view (FIT_CENTER behavior)
                    val scaleX = viewWidth / originalImageWidth
                    val scaleY = viewHeight / originalImageHeight
                    val fitCenterScale = min(scaleX, scaleY) // This ensures the entire image is visible

                    if (isFullScreen) {
                        // UPDATED: For fullscreen, minScaleFactor is the fit-to-screen scale
                        // This ensures the full image is always visible with black bars if needed
                        minScaleFactor = fitCenterScale
                        baseScaleFactor = fitCenterScale

                        // Calculate displayed image dimensions at fit-to-screen scale
                        displayedImageWidth = originalImageWidth * fitCenterScale
                        displayedImageHeight = originalImageHeight * fitCenterScale

                        // Maximum scale should allow reasonable zoom from the fit-to-screen base
                        maxAllowedScale = fitCenterScale * 4.0f // Allow 4x zoom from fit-to-screen

                    } else {
                        // For non-fullscreen (thumbnail view), keep original behavior
                        baseScaleFactor = fitCenterScale
                        minScaleFactor = 1.0f  // Keep this at 1.0f for thumbnail view

                        // Calculate displayed image dimensions
                        displayedImageWidth = originalImageWidth * fitCenterScale
                        displayedImageHeight = originalImageHeight * fitCenterScale

                        maxAllowedScale = max(3.0f, 1.0f / fitCenterScale)
                    }

                    Log.d(TAG, "calculateImageMaxScale: Original(${originalImageWidth}x${originalImageHeight}), " +
                            "View(${viewWidth}x${viewHeight}), " +
                            "FitCenterScale($fitCenterScale), " +
                            "BaseScale($baseScaleFactor), " +
                            "MinScale($minScaleFactor), " +
                            "MaxScale($maxAllowedScale), " +
                            "IsFullScreen($isFullScreen)")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "calculateImageMaxScale: Error calculating max scale", e)
            minScaleFactor = if (isFullScreen) 0.5f else 1.0f
            maxAllowedScale = 3.0f // Fallback
        }
    }

    private fun calculateDocumentMaxScale() {
        // UPDATED: For documents, prevent zooming below fit-to-screen
        minScaleFactor = 1.0f
        maxAllowedScale = 3.0f
        Log.d(TAG, "calculateDocumentMaxScale: Set scale range $minScaleFactor to $maxAllowedScale for document")
    }

    private fun handleDoubleTap(x: Float, y: Float) {
        if (!isFullScreen) return

        mixedFeedUploadDataClass?.let { data ->
            when (data.fileTypes) {
                "image" -> {
                    if (isZoomed) {
                        resetZoom()
                    } else {
                        // Calculate max scale first
                        calculateMaxAllowedScale()
                        // Use a reasonable zoom level above minimum
                        val targetScale = min(2.5f, maxAllowedScale * 0.6f)
                        zoomToPoint(x, y, targetScale)
                    }
                }
                "document" -> {
                    if (isZoomed) {
                        resetZoom()
                    } else {
                        calculateMaxAllowedScale()
                        val targetScale = min(2.0f, maxAllowedScale * 0.5f)
                        zoomToPoint(x, y, targetScale)
                    }
                }
                "video" -> {
                    Log.d(TAG, "handleDoubleTap: Video double-tap - could implement video controls")
                }
                "audio" -> {
                    Log.d(TAG, "handleDoubleTap: Audio double-tap - could toggle repeat mode")
                }
            }
        }
    }

    inner class ScaleGestureListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            if (!isFullScreen) return false

            mixedFeedUploadDataClass?.let { data ->
                when (data.fileTypes) {
                    "image", "document" -> {
                        val oldScaleFactor = scaleFactor
                        scaleFactor *= detector.scaleFactor

                        // Calculate max scale on first zoom attempt
                        if (oldScaleFactor == 1.0f) {
                            calculateMaxAllowedScale()
                        }

                        // CRITICAL FIX: Constrain scale factor to never go below minScaleFactor (fit-to-screen)
                        scaleFactor = max(minScaleFactor, min(scaleFactor, maxAllowedScale))

                        // Update focus point
                        focusX = detector.focusX - binding.feedUploadImageView.width / 2f
                        focusY = detector.focusY - binding.feedUploadImageView.height / 2f

                        // Update isZoomed state - zoomed means above the minimum (fit-to-screen) scale
                        isZoomed = scaleFactor > minScaleFactor

                        applyTransformation()

                        Log.d(TAG, "onScale: Scale factor: $scaleFactor (min: $minScaleFactor, max: $maxAllowedScale)")
                        return true
                    }
                }
            }
            return false
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            return isFullScreen && (mixedFeedUploadDataClass?.fileTypes in listOf("image", "document"))
        }
    }

    private fun limitPanning() {
        // Only allow panning when zoomed above fit-to-screen
        if (scaleFactor <= minScaleFactor) {
            posX = 0f
            posY = 0f
            return
        }

        val viewWidth = binding.feedUploadImageView.width.toFloat()
        val viewHeight = binding.feedUploadImageView.height.toFloat()

        // Use displayed image dimensions for more accurate panning limits
        val scaledWidth = if (displayedImageWidth > 0) displayedImageWidth * scaleFactor else viewWidth * scaleFactor
        val scaledHeight = if (displayedImageHeight > 0) displayedImageHeight * scaleFactor else viewHeight * scaleFactor

        val maxX = max(0f, (scaledWidth - viewWidth) / 2)
        val maxY = max(0f, (scaledHeight - viewHeight) / 2)

        posX = max(-maxX, min(maxX, posX))
        posY = max(-maxY, min(maxY, posY))
    }

    private fun handleSingleTap() {
        mixedFeedUploadDataClass?.let { data ->
            Log.d(TAG, "handleSingleTap: Tapped fileId=${data.fileId}, fileType=${data.fileTypes}")
            launchFullScreenActivityWithAllMedia()
        }
    }
    private fun zoomToPoint(x: Float, y: Float, targetScale: Float) {
        // Ensure target scale is within bounds
        val constrainedScale = max(minScaleFactor, min(targetScale, maxAllowedScale))

        scaleFactor = constrainedScale

        // Calculate focus point relative to view center
        val viewWidth = binding.feedUploadImageView.width.toFloat()
        val viewHeight = binding.feedUploadImageView.height.toFloat()

        focusX = x - viewWidth / 2
        focusY = y - viewHeight / 2

        // Calculate position to center the zoom on the tap point
        posX = -focusX * (scaleFactor - 1)
        posY = -focusY * (scaleFactor - 1)

        limitPanning()
        applyTransformation()

        isZoomed = scaleFactor > minScaleFactor
        Log.d(TAG, "zoomToPoint: Zoomed to ${scaleFactor}x at ($x, $y)")
    }

    private fun resetZoom() {
        // FIXED: Check if binding is still valid
        if (_binding == null || !isAdded) {
            Log.w(TAG, "resetZoom: Fragment not in valid state, skipping")
            return
        }

        if (isFullScreen) {
            // In fullscreen, reset to fit-to-screen scale (which shows full image with black bars)
            scaleFactor = minScaleFactor
            posX = 0f
            posY = 0f
            focusX = 0f
            focusY = 0f
            isZoomed = false

            // Use FIT_CENTER to show the full image with black bars if needed
            binding.feedUploadImageView.scaleType = ImageView.ScaleType.FIT_CENTER
            binding.feedUploadImageView.imageMatrix = null

            Log.d(TAG, "resetZoom: Reset to fit-to-screen with scale factor $scaleFactor")
        } else {
            // In thumbnail view, reset to fit-to-screen
            scaleFactor = minScaleFactor
            posX = 0f
            posY = 0f
            focusX = 0f
            focusY = 0f
            isZoomed = false

            applyTransformation()
            Log.d(TAG, "resetZoom: Thumbnail reset to scale factor $scaleFactor")
        }
    }

    private fun onImageLoaded() {
        // FIXED: Check if binding is still valid
        if (_binding == null || !isAdded) {
            Log.w(TAG, "onImageLoaded: Fragment not in valid state, skipping")
            return
        }

        // Call this after image is loaded to set up proper scale limits
        binding.feedUploadImageView.post {
            // Double-check binding is still valid after post
            if (_binding == null || !isAdded) {
                Log.w(TAG, "onImageLoaded: Fragment not in valid state after post, skipping")
                return@post
            }

            calculateMaxAllowedScale()

            if (isFullScreen) {
                // UPDATED: For fullscreen, start with fit-to-screen scale
                // This ensures the image is fully visible with black bars if needed
                scaleFactor = minScaleFactor
                posX = 0f
                posY = 0f
                focusX = 0f
                focusY = 0f
                isZoomed = false // Start unzoomed

                // Apply the transformation to ensure proper display
                applyTransformation()
                Log.d(TAG, "onImageLoaded: Fullscreen - set to fit-to-screen scale: $scaleFactor")
            } else {
                // For thumbnail view, reset to fit-to-screen
                resetZoom()
            }
        }
    }
    private fun applyTransformation() {
        // FIXED: Check if binding is still valid
        if (!isFullScreen || _binding == null || !isAdded) return

        // Only apply matrix transformation when zoomed
        if (scaleFactor > minScaleFactor) {
            val matrix = Matrix()
            matrix.postScale(
                scaleFactor,
                scaleFactor,
                binding.feedUploadImageView.width / 2f,
                binding.feedUploadImageView.height / 2f
            )
            matrix.postTranslate(posX, posY)

            binding.feedUploadImageView.imageMatrix = matrix
            binding.feedUploadImageView.scaleType = ImageView.ScaleType.MATRIX
        } else {
            // When not zoomed, use FIT_CENTER to show full image with black bars
            binding.feedUploadImageView.scaleType = ImageView.ScaleType.FIT_CENTER
            binding.feedUploadImageView.imageMatrix = null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mixedFeedUploadDataClass?.let { data ->
            bindData(data)
            if (!isFullScreen) {
                setupClickListener(data)
            }
        } ?: run {
            Log.e(TAG, "onViewCreated: mixedFeedUploadDataClass is null")
            setupDefaultUI()
        }
    }

    private fun setupClickListener(data: MixedFeedUploadDataClass) {
        binding.root.setOnClickListener {
            Log.d(TAG,
                "setupClickListener: Tapped fileId=${data.fileId}, " +
                        "fileType=${data.fileTypes}")
            launchFullScreenActivityWithAllMedia()
        }
    }

    private fun showDocumentOpenOptions(data: MixedFeedUploadDataClass) {
        data.documents?.let { docData ->
            if (docData.pdfFilePath.isNotEmpty()) {
                val file = File(docData.pdfFilePath)
                if (file.exists()) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW)
                        val uri = androidx.core.content.FileProvider.getUriForFile(
                            requireContext(),
                            "${requireContext().packageName}.fileprovider",
                            file
                        )
                        intent.setDataAndType(uri,
                            getMimeType(file.extension))
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                        // Create chooser to show all available apps
                        val chooser = Intent.createChooser(intent,
                            "Open document with...")

                        if (chooser.resolveActivity(
                                requireContext().packageManager) != null) {
                            startActivity(chooser)
                            Log.d(TAG,
                                "showDocumentOpenOptions:" +
                                        " Opened document chooser for fileId=${data.fileId}")
                        } else {
                            // Show toast that no app is available
                            android.widget.Toast.makeText(
                                requireContext(),
                                "No app available to open this document",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                            Log.w(TAG,
                                "showDocumentOpenOptions:" +
                                        " No app available to handle document")
                        }
                    } catch (e: IllegalArgumentException) {
                        Log.e(TAG,
                            "showDocumentOpenOptions:" +
                                    " FileProvider path not configured for ${file.absolutePath}",
                            e)

                        // Fallback: Copy file to external cache and try again
                        try {
                            val externalCacheDir = requireContext().externalCacheDir
                            if (externalCacheDir != null) {
                                val tempFile = File(externalCacheDir, file.name)
                                file.copyTo(tempFile, overwrite = true)

                                val fallbackUri = androidx.core.content.FileProvider.getUriForFile(
                                    requireContext(),
                                    "${requireContext().packageName}.fileprovider",
                                    tempFile
                                )

                                val fallbackIntent = Intent(Intent.ACTION_VIEW)
                                fallbackIntent.setDataAndType(fallbackUri,
                                    getMimeType(file.extension))
                                fallbackIntent.addFlags(
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION)

                                val fallbackChooser = Intent.createChooser(
                                    fallbackIntent, "Open document with...")
                                startActivity(fallbackChooser)
                                Log.d(TAG,
                                    "showDocumentOpenOptions: Opened with fallback method")
                            } else {
                                throw Exception("External cache directory not available")
                            }
                        } catch (fallbackException: Exception) {
                            Log.e(TAG,
                                "showDocumentOpenOptions: Fallback method failed",
                                fallbackException)
                            android.widget.Toast.makeText(
                                requireContext(),
                                "Error opening document: ${fallbackException.message}",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG,
                            "showDocumentOpenOptions: " +
                                    "Error opening document for fileId=${data.fileId}", e)
                        android.widget.Toast.makeText(
                            requireContext(),
                            "Error opening document",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Log.w(TAG,
                        "showDocumentOpenOptions:" +
                                " Document file does not exist for fileId=${data.fileId}")
                    android.widget.Toast.makeText(
                        requireContext(),
                        "Document file not found",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Log.w(TAG,
                    "showDocumentOpenOptions:" +
                            " No valid document path for fileId=${data.fileId}")
            }
        }
    }

    fun setAllMediaItems(allItems: List<MixedFeedUploadDataClass>, position: Int) {
        this.allMediaItems = allItems
        this.currentPosition = position
        Log.d(TAG, "setAllMediaItems: Set ${allItems.size} items, current position: $position")
    }

    private fun bindData(data: MixedFeedUploadDataClass) {
        when (data.fileTypes) {
            "image" -> setupImageUI(data)
            "video" -> setupVideoUI(data)
            "audio" -> setupAudioUI(data)
            "document" -> setupDocumentUI(data)
            else -> setupDefaultUI()
        }
    }

    private fun setupImageUI(data: MixedFeedUploadDataClass) {
        binding.videoView.visibility = View.GONE
        binding.audioIcon.visibility = View.GONE
        binding.feedUploadImageView.visibility = View.VISIBLE
        binding.viewPager.visibility = View.GONE

        data.images?.let { imageData ->
            val imagePath = imageData.compressedImagePath.takeIf { it.isNotEmpty() } ?: imageData.imagePath
            if (imagePath.isNotEmpty()) {
                // Use the safer loading method
                loadImageSafely(imagePath, data)
            } else {
                Log.w(TAG, "setupImageUI: Empty image path for fileId=${data.fileId}")
                if (_binding != null && isAdded) {
                    binding.feedUploadImageView.setImageResource(R.drawable.flash21)
                }
            }

            // UPDATED: Set appropriate ScaleType based on mode
            binding.feedUploadImageView.scaleType = if (isFullScreen) {
                // For fullscreen, use CENTER_CROP to fill the entire width
                ImageView.ScaleType.CENTER_CROP
            } else {
                // For thumbnails, use CENTER_CROP to fill the view
                ImageView.ScaleType.CENTER_CROP
            }

            // UPDATED: Configure layout for fullscreen to take full width
            if (isFullScreen) {
                // Remove background color to avoid black bars
                binding.feedUploadImageView.setBackgroundColor(android.graphics.Color.TRANSPARENT)

                // Ensure the ImageView takes full screen with proper layout params
                val layoutParams = binding.feedUploadImageView.layoutParams
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                binding.feedUploadImageView.layoutParams = layoutParams

                // Additional configuration for ViewPager context
                binding.feedUploadImageView.adjustViewBounds = false
                binding.feedUploadImageView.cropToPadding = false
            } else {
                // Clear background for thumbnails
                binding.feedUploadImageView.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }

            if (imageData.compressedImagePath.isEmpty()) {
                val file = File(imageData.imagePath)
                lifecycleScope.launch {
                    compressedImageFile = Compressor.compress(requireContext(), file)
                    Log.d(TAG, "Compressed image path: ${compressedImageFile?.absolutePath} for fileId=${data.fileId}")
                    data.images?.compressedImagePath = compressedImageFile?.absolutePath ?: ""
                }
            }
        } ?: run {
            Log.w(TAG, "setupImageUI: No image data for fileId=${data.fileId}")
            binding.feedUploadImageView.setImageResource(R.drawable.flash21)
        }
    }



    private fun setupAudioUI(data: MixedFeedUploadDataClass) {
        binding.videoView.visibility = View.GONE
        binding.feedUploadImageView.visibility = View.VISIBLE
        binding.audioIcon.visibility = View.VISIBLE
        binding.viewPager.visibility = View.GONE

        data.audios?.audioPath?.let { audioPath ->
            val albumArt = getAlbumArt(audioPath)
            if (albumArt != null) {
                Log.d(TAG, "setupAudioUI: Album art not null for fileId=${data.fileId}")
                Glide.with(requireContext())
                    .load(albumArt)
                    .placeholder(R.drawable.flash21)
                    .error(R.drawable.music_icon)
                    .into(binding.feedUploadImageView)
            } else {
                Log.d(TAG, "setupAudioUI: Album art null for fileId=${data.fileId}")
                Glide.with(requireContext())
                    .load(R.drawable.music_icon)
                    .placeholder(R.drawable.flash21)
                    .error(R.drawable.flash21)
                    .into(binding.feedUploadImageView)
            }
        } ?: run {
            Log.w(TAG, "setupAudioUI: No audio data for fileId=${data.fileId}")
            binding.feedUploadImageView.setImageResource(R.drawable.music_icon)
        }
    }

    private fun setupDocumentUI(data: MixedFeedUploadDataClass) {
        binding.videoView.visibility = View.GONE
        binding.audioIcon.visibility = View.GONE
        binding.feedUploadImageView.visibility = View.VISIBLE
        binding.viewPager.visibility = View.GONE

        data.documents?.let { docData ->
            if (docData.documentThumbnailFilePath != null) {
                // Load existing Bitmap thumbnail
                Glide.with(requireContext())
                    .load(docData.documentThumbnailFilePath)
                    .placeholder(R.drawable.documents)
                    .error(R.drawable.documents)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .fitCenter()
                    .into(binding.feedUploadImageView)
                Log.d(TAG, "setupDocumentUI: Loaded existing thumbnail for fileId=${data.fileId}")
            } else if (docData.pdfFilePath.isNotEmpty()) {
                // Show loading state first
                binding.feedUploadImageView.setImageResource(R.drawable.documents)

                // Generate thumbnail for PDF
                lifecycleScope.launch {
                    val thumbnailFile = generatePdfThumbnail(docData.pdfFilePath, data.fileId)
                    if (thumbnailFile != null && isAdded && context != null) {
                        // Load the generated thumbnail file on main thread
                        Glide.with(requireContext())
                            .load(thumbnailFile)
                            .placeholder(R.drawable.documents)
                            .error(R.drawable.documents)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .fitCenter()
                            .into(binding.feedUploadImageView)

                        // Update documentThumbnailFilePath
                        try {
                            val bitmap = android.graphics.BitmapFactory.decodeFile(thumbnailFile.absolutePath)
                            if (bitmap != null) {
                                docData.documentThumbnailFilePath = bitmap
                                Log.d(TAG, "setupDocumentUI: Generated and loaded PDF thumbnail for fileId=${data.fileId}, path=${thumbnailFile.absolutePath}")
                            } else {
                                Log.w(TAG, "setupDocumentUI: Failed to decode bitmap for fileId=${data.fileId}")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "setupDocumentUI: Error decoding bitmap for fileId=${data.fileId}", e)
                        }
                    } else {
                        if (isAdded && context != null) {
                            Log.w(TAG, "setupDocumentUI: Failed to generate thumbnail for fileId=${data.fileId}")
                            binding.feedUploadImageView.setImageResource(R.drawable.documents)
                        }
                    }
                }
            } else {
                Log.w(TAG, "setupDocumentUI: No thumbnail or valid PDF path for fileId=${data.fileId}")
                binding.feedUploadImageView.setImageResource(R.drawable.documents)
            }
        } ?: run {
            Log.w(TAG, "setupDocumentUI: No document data for fileId=${data.fileId}")
            binding.feedUploadImageView.setImageResource(R.drawable.documents)
        }

        binding.feedUploadImageView.scaleType = ImageView.ScaleType.CENTER_CROP
    }

    private fun getMimeType(extension: String): String {
        return when (extension.lowercase()) {
            "pdf" -> "application/pdf"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "ppt" -> "application/vnd.ms-powerpoint"
            "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            "txt" -> "text/plain"
            "rtf" -> "application/rtf"
            else -> "application/octet-stream" // Generic binary file type
        }
    }

    private fun setupVideoUI(data: MixedFeedUploadDataClass) {
        if (isFullScreen) {
            // For fullscreen, show VideoView instead of thumbnail
            binding.videoView.visibility = View.VISIBLE
            binding.feedUploadImageView.visibility = View.GONE
            binding.audioIcon.visibility = View.GONE
            binding.viewPager.visibility = View.GONE

            data.videos?.videoPath?.let { videoPath ->
                binding.videoView.setVideoPath(videoPath)
                binding.videoView.setOnPreparedListener { mediaPlayer ->
                    mediaPlayer.isLooping = true
                    mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
                }
                // Don't auto-start, let user control
            }
        } else {
            // For non-fullscreen, show thumbnail
            binding.videoView.visibility = View.GONE
            binding.feedUploadImageView.visibility = View.VISIBLE
            binding.audioIcon.visibility = View.GONE
            binding.viewPager.visibility = View.GONE

            data.videos?.let { video ->
                if (video.thumbnail != null) {
                    Glide.with(requireContext())
                        .load(video.thumbnail)
                        .placeholder(R.drawable.flash21)
                        .error(R.drawable.flash21)
                        .into(binding.feedUploadImageView)
                } else {
                    Glide.with(requireContext())
                        .load(Uri.fromFile(File(video.videoPath ?: "")))
                        .placeholder(R.drawable.flash21)
                        .error(R.drawable.flash21)
                        .into(binding.feedUploadImageView)
                }
            }
        }
    }

    private fun loadImageSafely(imagePath: String, data: MixedFeedUploadDataClass) {
        // Only start Glide request if fragment is in valid state
        if (_binding == null || !isAdded) {
            Log.w(TAG, "loadImageSafely: Fragment not in valid state, skipping Glide request")
            return
        }

        try {
            Glide.with(this) // Use 'this' (fragment) instead of requireContext() for better lifecycle handling
                .load(File(imagePath))
                .placeholder(R.drawable.flash21)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.flash21)
                .apply {
                    if (isFullScreen) {
                        // For fullscreen, use centerCrop to fill the entire width without black bars
                        centerCrop()
                    } else {
                        centerCrop()
                    }
                }
                .listener(object : com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable> {
                    override fun onLoadFailed(
                        e: com.bumptech.glide.load.engine.GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.e(TAG, "loadImageSafely: Failed to load image for fileId=${data.fileId}", e)
                        return false
                    }

                    override fun onResourceReady(
                        resource: android.graphics.drawable.Drawable?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>?,
                        dataSource: com.bumptech.glide.load.DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        // Remove the zoom setup for full-width display
                        // The image should now fill the entire width
                        return false
                    }
                })
                .into(binding.feedUploadImageView)
        } catch (e: Exception) {
            Log.e(TAG, "loadImageSafely: Exception loading image for fileId=${data.fileId}", e)
            if (_binding != null && isAdded) {
                binding.feedUploadImageView.setImageResource(R.drawable.flash21)
            }
        }
    }


    override fun onDestroyView() {
        // Clear Glide requests before destroying view
        if (_binding != null) {
            try {
                Glide.with(this).clear(binding.feedUploadImageView)
            } catch (e: Exception) {
                Log.e(TAG, "onDestroyView: Error clearing Glide requests", e)
            }
        }

        super.onDestroyView()
        releasePlayer() // Release media players first
        anyFileMediaPagerAdapter?.releasePlayer()

        if (_binding != null) {
            binding.videoView.stopPlayback()
        }
        _binding = null
    }

    fun playVideo() {
        if (isFullScreen && binding.videoView.visibility == View.VISIBLE) {
            // Use VideoView for fullscreen
            if (!binding.videoView.isPlaying) {
                binding.videoView.start()
                isVideoPlaying = true
                Log.d(TAG, "playVideo: Started VideoView playback")
            }
        } else {
            // Keep your existing MediaPlayer logic for audio-only or background
            mixedFeedUploadDataClass?.videos?.videoPath?.let { videoPath ->
                try {
                    if (videoMediaPlayer == null) {
                        videoMediaPlayer = MediaPlayer().apply {
                            setDataSource(videoPath)
                            isLooping = true
                            prepareAsync()
                            setOnPreparedListener {
                                start()
                                isVideoPlaying = true
                                Log.d(TAG, "playVideo: Started MediaPlayer audio")
                            }
                        }
                    } else {
                        videoMediaPlayer?.start()
                        isVideoPlaying = true
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "playVideo: Error", e)
                }
            }
        }
    }

    fun pauseVideo() {
        if (isFullScreen && binding.videoView.visibility == View.VISIBLE) {
            if (binding.videoView.isPlaying) {
                binding.videoView.pause()
                isVideoPlaying = false
                Log.d(TAG, "pauseVideo: Paused VideoView")
            }
        } else {
            videoMediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                    isVideoPlaying = false
                }
            }
        }
    }

    fun seekVideo(position: Long) {
        if (isFullScreen && binding.videoView.visibility == View.VISIBLE) {
            binding.videoView.seekTo(position.toInt())
            Log.d(TAG, "seekVideo: VideoView seeked to $position")
        } else {
            videoMediaPlayer?.seekTo(position.toInt())
            Log.d(TAG, "seekVideo: MediaPlayer seeked to $position")
        }
    }

    fun getCurrentVideoPosition(): Long {
        return if (isFullScreen && binding.videoView.visibility == View.VISIBLE) {
            binding.videoView.currentPosition.toLong()
        } else {
            videoMediaPlayer?.currentPosition?.toLong() ?: 0L
        }
    }

    fun getVideoDuration(): Long {
        return if (isFullScreen && binding.videoView.visibility == View.VISIBLE) {
            binding.videoView.duration.toLong()
        } else {
            videoMediaPlayer?.duration?.toLong() ?: 0L
        }
    }

    private fun generatePdfThumbnail(pdfPath: String, fileId: String?): File? {
        return try {
            val file = File(pdfPath)
            if (!file.exists()) {
                Log.w(TAG, "generatePdfThumbnail: PDF file does not exist at path=$pdfPath")
                return null
            }

            val parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val pdfRenderer = PdfRenderer(parcelFileDescriptor)

            // Render the first page
            val page = pdfRenderer.openPage(0)
            val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

            // Save the bitmap to a temporary file
            val outputDir = requireContext().cacheDir
            val outputFile = File.createTempFile("pdf_thumbnail_${fileId}", ".png", outputDir)
            FileOutputStream(outputFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }

            // Clean up
            page.close()
            pdfRenderer.close()
            parcelFileDescriptor.close()

            outputFile
        } catch (e: Exception) {
            Log.e(TAG, "generatePdfThumbnail: Error generating thumbnail for pdfPath=$pdfPath", e)
            null
        }
    }

    private fun setupDefaultUI() {
        binding.videoView.visibility = View.GONE
        binding.audioIcon.visibility = View.GONE
        binding.feedUploadImageView.visibility = View.VISIBLE
        binding.viewPager.visibility = View.GONE
        // binding.feedUploadImageView.setImageResource(R.drawable.flash21)
        Log.d(TAG, "setupDefaultUI: Default UI set")
    }

    private fun launchFullScreenActivityWithAllMedia() {
        val mediaUrls = ArrayList<String>()
        val videoThumbnails = ArrayList<String?>()

        val itemsToProcess = if (allMediaItems.isNotEmpty()) allMediaItems else listOfNotNull(mixedFeedUploadDataClass)

        itemsToProcess.forEachIndexed { index, data ->
            when {
                data.fileTypes == "image" -> {
                    data.images?.let { image ->
                        val imagePath = image.compressedImagePath.takeIf { it.isNotEmpty() } ?: image.imagePath
                        if (imagePath.isNotEmpty()) {
                            mediaUrls.add(image.imagePath)
                            videoThumbnails.add(null)
                        } else {
                            Log.w(TAG,
                                "Navigation to the  Full Screen Activity With All Media types: Empty image path for fileId=${data.fileId}")
                        }
                    }
                }
                data.fileTypes == "video" -> {
                    data.videos?.let { video ->
                        val videoPath = video.videoPath
                        if (!videoPath.isNullOrEmpty()) {
                            mediaUrls.add(videoPath)
                            videoThumbnails.add(video.thumbnail?.toString())
                        } else {
                            Log.w(TAG,
                                "Navigation to the  Full Screen Activity With All Media types: Empty video path for fileId=${data.fileId}")
                        }
                    }
                }
                data.fileTypes == "audio" -> {
                    data.audios?.let { audio ->
                        val audioPath = audio.audioPath
                        if (!audioPath.isNullOrEmpty()) {
                            mediaUrls.add(audioPath)
                            videoThumbnails.add(getAlbumArt(audioPath)?.toString())
                        } else {
                            Log.w(TAG,
                                "Navigation to the  Full Screen Activity With All Media types: Empty audio path for fileId=${data.fileId}")
                        }
                    }
                }
                data.fileTypes == "pdf" -> {
                    data.documents?.let { document ->
                        val docPath = document.pdfFilePath.takeIf { it.isNotEmpty() }
                        if (docPath != null) {
                            mediaUrls.add(docPath)
                            val thumbnailPath = document.documentThumbnailFilePath?.let { bitmap ->
                                saveBitmapToTempFile(bitmap, data.fileId)?.absolutePath
                            }
                            videoThumbnails.add(thumbnailPath)
                            Log.d(TAG,
                                "Navigation to the  Full Screen Activity With All Media types: Added document path=$docPath, thumbnail=$thumbnailPath for fileId=${data.fileId}")
                        } else {
                            Log.w(TAG,
                                "Navigation to the  Full Screen Activity With All Media types: Empty document path for fileId=${data.fileId}")
                        }
                    }
                }
                else -> {
                    Log.w(TAG,
                        "Navigation to the  Full Screen Activity With All Media types: Sorry, File Unknown file type for fileId=${data.fileId}: ${data.fileTypes}")
                }
            }
        }

        if (mediaUrls.isNotEmpty()) {
            val intent = Intent(requireContext(), AnyFileFullScreenActivity::class.java)
            intent.putStringArrayListExtra("imageUrls", mediaUrls)
            intent.putExtra("position", currentPosition.coerceIn(0, mediaUrls.size - 1))
            intent.putStringArrayListExtra("videoThumbnails", videoThumbnails)
            startActivity(intent)

            Log.d(TAG,
                "Navigation to the  Full Screen Activity With All Media types: Launched with ${mediaUrls.size} media items, starting at position $currentPosition")
        } else {
            Log.w(TAG,
                "Navigation to the  Full Screen Activity With All Media types: No valid media URLs found")
        }
    }

    private fun showInlineMedia(data: MixedFeedUploadDataClass) {
        val mediaUrls = ArrayList<String>()
        val videoThumbnails = ArrayList<String?>()

        val itemsToProcess = if (allMediaItems.isNotEmpty()) allMediaItems else listOfNotNull(mixedFeedUploadDataClass)

        itemsToProcess.forEachIndexed { index, item ->
            when (item.fileTypes) {
                "image" -> {
                    item.images?.let { image ->
                        val imagePath = image.compressedImagePath.takeIf { it.isNotEmpty() } ?: image.imagePath
                        if (imagePath.isNotEmpty()) {
                            mediaUrls.add(imagePath)
                            videoThumbnails.add(null)
                        } else {
                            Log.w(TAG, "Showing Tapped Files In line Media: Empty image path for fileId=${item.fileId}")
                        }
                    }
                }
                "video" -> {
                    item.videos?.let { video ->
                        val videoPath = video.videoPath
                        if (!videoPath.isNullOrEmpty()) {
                            mediaUrls.add(videoPath)
                            videoThumbnails.add(video.thumbnail?.toString())
                        } else {
                            Log.w(TAG, "Showing Tapped Files In line Media: Empty video path for fileId=${item.fileId}")
                        }
                    }
                }
                "audio" -> {
                    item.audios?.let { audio ->
                        val audioPath = audio.audioPath
                        if (!audioPath.isNullOrEmpty()) {
                            mediaUrls.add(audioPath)
                            videoThumbnails.add(getAlbumArt(audioPath)?.toString())
                        } else {
                            Log.w(TAG, "Showing Tapped Files In line Media: Empty audio path for fileId=${item.fileId}")
                        }
                    }
                }
                "document" -> {
                    item.documents?.let { document ->
                        val docPath = document.pdfFilePath.takeIf { it.isNotEmpty() }
                        if (docPath != null) {
                            mediaUrls.add(docPath)
                            if (document.documentThumbnailFilePath != null) {
                                val tempFile = saveBitmapToTempFile(document.documentThumbnailFilePath!!, item.fileId)
                                videoThumbnails.add(tempFile?.absolutePath)
                            } else {
                                videoThumbnails.add(null)
                            }
                        } else {
                            Log.w(TAG, "Showing Tapped Files In line Media: Empty document path for fileId=${item.fileId}")
                        }
                    }
                }
                else -> {
                    Log.w(TAG, "Showing Tapped Files In line Media: Unknown file type for fileId=${item.fileId}: ${item.fileTypes}")
                }
            }
        }

        if (mediaUrls.isNotEmpty()) {
            binding.feedUploadImageView.visibility = View.GONE
            binding.videoView.visibility = View.GONE
            binding.audioIcon.visibility = View.GONE
            binding.viewPager.visibility = View.VISIBLE

            anyFileMediaPagerAdapter = AnyFileMediaPagerAdapter(
                mediaUrls,
                requireActivity(),
                null,
                getAllMediaUrls(),
                currentPosition
            )
            binding.viewPager.adapter = anyFileMediaPagerAdapter
            binding.viewPager.setCurrentItem(currentPosition.coerceIn(0, mediaUrls.size - 1), false)

            Log.d(TAG, "Showing Tapped Files In line Media: Displaying ${mediaUrls.size} media items inline, starting at position $currentPosition")
        } else {
            Log.w(TAG, "Showing Tapped Files In line Media: No valid media URLs found")
            setupDefaultUI()
        }
    }

    private fun saveBitmapToTempFile(bitmap: Bitmap, fileId: String?): File? {
        return try {
            val outputDir = requireContext().cacheDir
            val outputFile = File.createTempFile("doc_thumbnail_${fileId}", ".png", outputDir)
            FileOutputStream(outputFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
            outputFile
        } catch (e: Exception) {
            Log.e(TAG, "saveBitmapToTempFile: Error saving bitmap for fileId=$fileId", e)
            null
        }
    }

    private fun getAllMediaUrls(): ArrayList<String>? {
        if (allMediaItems.isEmpty()) return null

        val allUrls = ArrayList<String>()
        allMediaItems.forEach { data ->
            when (data.fileTypes) {
                "image" -> {
                    data.images?.let {
                        val imagePath = it.compressedImagePath.takeIf { path -> path.isNotEmpty() } ?: it.imagePath
                        if (imagePath.isNotEmpty()) allUrls.add(imagePath)
                    }
                }
                "video" -> {
                    data.videos?.let {
                        if (!it.videoPath.isNullOrEmpty()) allUrls.add(it.videoPath)
                    }
                }
                "audio" -> {
                    data.audios?.let {
                        if (!it.audioPath.isNullOrEmpty()) allUrls.add(it.audioPath)
                    }
                }
                "document" -> {
                    data.documents?.let {
                        if (it.pdfFilePath.isNotEmpty()) allUrls.add(it.pdfFilePath)
                    }
                }
            }
        }
        return allUrls
    }

    fun updateVideo(newVideo: FeedMultipleVideos) {
        mixedFeedUploadDataClass?.let {
            it.videos = newVideo
            bindData(it)
            Log.d(TAG, "updateVideo: Updated video data for fileId=${it.fileId}, thumbnail=${newVideo.thumbnail}")
        }
    }

    fun setFeedTextViewFragmentInterface(listener: FeedTextViewFragmentInterface?) {
        this.feedTextViewFragmentInterface = listener
        Log.d(TAG, "setFeedTextViewFragmentInterface: Listener Setting...")
    }

    private fun getAlbumArt(audioPath: String): Uri? {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        return try {
            mediaMetadataRetriever.setDataSource(audioPath)
            val albumArtBytes = mediaMetadataRetriever.embeddedPicture
            if (albumArtBytes != null) {
                val albumArtFile = saveAlbumArt(albumArtBytes)
                Uri.fromFile(albumArtFile)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "getAlbumArt: Error retrieving album art for audioPath=$audioPath", e)
            null
        } finally {
            mediaMetadataRetriever.release()
        }
    }

    private fun saveAlbumArt(albumArtBytes: ByteArray): File {
        val outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val outputFile = File.createTempFile("album_art_${mixedFeedUploadDataClass?.fileId}", ".jpg", outputDir)
        FileOutputStream(outputFile).use { outputStream ->
            outputStream.write(albumArtBytes)
        }
        return outputFile
    }



    fun getCurrentAudioPosition(): Long {
        return audioMediaPlayer?.currentPosition?.toLong() ?: 0L
    }

    fun getAudioDuration(): Long {
        return audioMediaPlayer?.duration?.toLong() ?: run {
            // Fallback: get duration from audio file
            mixedFeedUploadDataClass?.audios?.let { audioData ->
                try {
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(audioData.audioPath)
                    val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                    retriever.release()
                    duration
                } catch (e: Exception) {
                    Log.e(TAG, "getAudioDuration: Error getting duration", e)
                    0L
                }
            } ?: 0L
        }
    }

    fun formatDuration(milliseconds: Long): String {
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / (1000 * 60)) % 60
        val hours = milliseconds / (1000 * 60 * 60)

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    fun getRemainingDuration(currentPosition: Long, totalDuration: Long): String {
        val remaining = if (totalDuration > currentPosition) {
            totalDuration - currentPosition
        } else {
            0L
        }
        return formatDuration(remaining)
    }

    fun setVideoLooping(shouldLoop: Boolean) {
        videoMediaPlayer?.isLooping = shouldLoop
        Log.d(TAG, "setVideoLooping: Set to $shouldLoop")
    }

    fun setAudioLooping(shouldLoop: Boolean) {
        audioMediaPlayer?.isLooping = shouldLoop
        Log.d(TAG, "setAudioLooping: Set to $shouldLoop")
    }

    fun playAudio() {
        mixedFeedUploadDataClass?.audios?.audioPath?.let { audioPath ->
            try {
                if (audioMediaPlayer == null) {
                    audioMediaPlayer = MediaPlayer().apply {
                        setDataSource(audioPath)
                        isLooping = true  // This is already there
                        prepareAsync()
                        setOnPreparedListener {
                            start()
                            isAudioPlaying = true
                            Log.d(TAG, "playAudio: Started audio playback with looping enabled")
                        }
                        setOnCompletionListener { // Add this listener for auto-restart
                            if (isLooping) {
                                start() // Restart the audio
                                Log.d(TAG, "playAudio: Auto-restarted audio after completion")
                            } else {
                                isAudioPlaying = false
                                Log.d(TAG, "playAudio: Audio completed without loop")
                            }
                        }
                        setOnErrorListener { _, what, extra ->
                            Log.e(TAG, "playAudio: MediaPlayer error - what: $what, extra: $extra")
                            false
                        }
                    }
                } else {
                    audioMediaPlayer?.start()
                    isAudioPlaying = true
                    Log.d(TAG, "playAudio: Resumed audio playback")
                }
            } catch (e: Exception) {
                Log.e(TAG, "playAudio: Error starting audio", e)
            }
        }
    }

    fun pauseAudio() {
        audioMediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                isAudioPlaying = false
                Log.d(TAG, "pauseAudio: Paused audio playback")
            }
        }
    }

    fun seekAudio(position: Long) {
        audioMediaPlayer?.let {
            try {
                it.seekTo(position.toInt())
                Log.d(TAG, "seekAudio: Seeked to position $position")
            } catch (e: Exception) {
                Log.e(TAG, "seekAudio: Error seeking audio", e)
            }
        }
    }

    fun releasePlayer() {
        try {
            audioMediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
                audioMediaPlayer = null
                isAudioPlaying = false
            }

            videoMediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
                videoMediaPlayer = null
                isVideoPlaying = false
            }

            Log.d(TAG, "releasePlayer: Released all media players")
        } catch (e: Exception) {
            Log.e(TAG, "releasePlayer: Error releasing players", e)
        }
    }

    override fun onPause() {
        super.onPause()
        // Pause all media when fragment goes to background
        pauseAudio()
        pauseVideo()
        anyFileMediaPagerAdapter?.pausePlayer()
    }

    override fun onResume() {
        super.onResume()
        // Don't auto-resume, let user control playback
        anyFileMediaPagerAdapter?.pausePlayer()
    }
}