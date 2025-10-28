package com.uyscuti.social.circuit.User_Interface.shorts

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Audio
import android.provider.OpenableColumns
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.adapter.ShortsAdapter
import com.uyscuti.social.circuit.adapter.feed.FeedAdapter
import com.uyscuti.social.circuit.adapter.feed.multiple_files.FeedVideoThumbnailAdapter
import com.uyscuti.social.circuit.adapter.feed.multiple_files.MultipleFeedAudioAdapter
import com.uyscuti.social.circuit.adapter.feed.multiple_files.MultipleFeedFilesPagerAdapter
import com.uyscuti.social.circuit.adapter.feed.multiple_files.MultipleSelectedFeedVideoAdapter
import com.uyscuti.social.circuit.adapter.feed.multiple_files.UriTypeAdapter
import com.uyscuti.social.circuit.adapter.feed.multiple_files.saveBitmapToCache
import com.uyscuti.social.circuit.model.feed.multiple_files.FeedMultipleVideos
import com.uyscuti.social.circuit.model.feed.multiple_files.MixedFeedUploadDataClass
import com.uyscuti.social.circuit.User_Interface.feed.FeedUploadWorker
import com.uyscuti.social.circuit.utils.AudioDurationHelper.getFormattedDuration
import com.uyscuti.social.circuit.utils.generateRandomId
import com.uyscuti.social.circuit.utils.getFileNameFromLocalPath
import com.uyscuti.social.circuit.utils.getFilePathFromContentUri
import com.uyscuti.social.circuit.utils.getFilePathFromUri
import com.uyscuti.social.circuit.utils.getFileSizeFromUri
import com.uyscuti.social.circuit.utils.getRealPathFromUri
import com.uyscuti.social.circuit.viewmodels.feed.GetFeedViewModel
import com.uyscuti.social.circuit.databinding.ActivityUploadShortsBinding
import com.uyscuti.social.circuit.model.feed.FeedMultipleImages
import com.uyscuti.social.circuit.model.feed.multiple_files.FeedMultipleDocumentsDataClass
import com.uyscuti.social.circuit.model.feed.multiple_files.MultipleAudios
import com.uyscuti.social.circuit.utils.PathUtil
import com.uyscuti.social.circuit.viewmodels.feed.FeedUploadViewModel
import com.uyscuti.social.compressor.CompressionListener
import com.uyscuti.social.compressor.VideoCompressor
import com.uyscuti.social.compressor.VideoQuality
import com.uyscuti.social.compressor.config.Configuration
import com.uyscuti.social.compressor.config.SaveLocation
import com.uyscuti.social.compressor.config.SharedStorageConfiguration
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.AndroidEntryPoint
import id.zelory.compressor.Compressor
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.relex.circleindicator.CircleIndicator3
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import android.widget.Button
import androidx.cardview.widget.CardView
import com.google.android.material.card.MaterialCardView
import com.uyscuti.social.network.api.response.allFeedRepostsPost.Post
import android.Manifest
import android.animation.ValueAnimator
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.VideoView
import androidx.appcompat.widget.Toolbar
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.scale
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat


private const val TAG = "UploadShortsActivity"

interface AudioUploadListener {
    fun onAudioSelected(audio: Audio)
    fun onAudioRemoved(position: Int)

}

@AndroidEntryPoint
class UploadShortsActivity : AppCompatActivity(), VideoThumbnailAdapter.ThumbnailClickListener,
    AudioUploadListener{

    companion object {
        internal const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        internal const val REQUEST_TOPICS_ACTIVITY = 1003
        const val EXTRA_VIDEO_URI = "extra_video_uri"

    }




    override fun onAudioSelected(audio: Audio) {
        // Handle audio selection
    }

    override fun onAudioRemoved(position: Int) {
        // Handle audio removal
    }


    private lateinit var binding: ActivityUploadShortsBinding

    @Inject
    lateinit var retrofitIns: RetrofitInstance



    private lateinit var feedUploadViewModel: FeedUploadViewModel
    private val getFeedViewModel: GetFeedViewModel by viewModels()


    private lateinit var toolbar: Toolbar
    private lateinit var titleTextView: TextView
    private lateinit var editTextText: EditText
    private lateinit var viewPager: ViewPager2
    private lateinit var circleIndicator: CircleIndicator3
    private lateinit var recyclerView2: RecyclerView
    private lateinit var interactionsBox: MaterialCardView



    private lateinit var draftButton: Button
    private lateinit var postButton: Button
    private lateinit var cancelButton: ImageView
    private lateinit var moreButton: ImageView
    private lateinit var saveChanges: TextView
    private lateinit var buttonsLayout: LinearLayout



    private lateinit var topicsLayout: LinearLayout
    private lateinit var locationLayout: LinearLayout
    private lateinit var iconLayout: LinearLayout
    private lateinit var tagPeopleLayout: LinearLayout
    private lateinit var addMoreFeedLayout: LinearLayout


    private lateinit var shortThumbNail: ImageView
    private lateinit var shortVideoThumbNail: CardView
    private lateinit var selectCoverText: TextView


    private lateinit var filterIndicator: TextView
    private lateinit var topicsChipsContainer: LinearLayout
    private lateinit var locationBadge: TextView
    private lateinit var topicsText: TextView
    private lateinit var locationText: TextView
    private lateinit var tagPeopleText: TextView


    private var imagePickLauncher: ActivityResultLauncher<Intent>? = null
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var audioPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var videoPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var documentPickerLauncher: ActivityResultLauncher<Intent>


    private var shortsAdapter: ShortsAdapter? = null
    private lateinit var feedAdapter: FeedAdapter
    private lateinit var multipleAudioAdapter: MultipleFeedAudioAdapter
    private var multipleFeedFilesPagerAdapter: MultipleFeedFilesPagerAdapter? = null
    private lateinit var multipleSelectedFeedVideoAdapter: MultipleSelectedFeedVideoAdapter


    private lateinit var feedRecyclerView: RecyclerView
    private lateinit var allPosts: List<Post>


    private lateinit var videoUri: Uri
    private var videoUris: MutableList<Uri> = mutableListOf()
    private var videoPaths: MutableList<String> = mutableListOf()
    private var videosList = mutableListOf<FeedMultipleVideos>()

    private var uris: MutableList<Uri> = mutableListOf()
    private var compressedImageFiles: MutableList<File> = mutableListOf()
    private lateinit var thumbnail: Bitmap
    private var imagesList = mutableListOf<String>()
    private var isMultipleImages = false

    // Thumbnail Management
    private var isThumbnailSelected = false
    private var isThumbnailClicked = false
    private val thumbnailCache = mutableMapOf<String, Bitmap>()
    private val thumbnailExtractionJobs = mutableMapOf<Int, Job>()


    private var audioDurationStringList: MutableList<String> = mutableListOf()
    private val audioPathList: MutableList<String> = mutableListOf()
    private var audiosList = mutableListOf<MultipleAudios>()

    private var documentsList = ArrayList<Uri>()



    private lateinit var caption: String
    var fileType: String = ""
    private var durationString = ""
    val tags: MutableList<String> = mutableListOf()
    var text = ""
    private var addMoreFeedFiles = true
    private var progressLineAnimator: ValueAnimator? = null
    private var progressLineView: View? = null


    private val selectedPeople = mutableSetOf<String>()
    private val selectedTopics = mutableSetOf<String>()
    private var selectedLocation: String? = null


    private var uploadWorkRequest: OneTimeWorkRequest? = null


    private lateinit var fullScreenVideoContainer: FrameLayout
    private lateinit var fullScreenVideoView: VideoView
    private lateinit var fullScreenPlayPauseButton: ImageView
    private lateinit var closeFullScreenButton: ImageView
    private lateinit var playButton: ImageView
    private lateinit var pauseButton: ImageView
    private lateinit var videoView: VideoView



    private val originalUIState = mutableMapOf<View, Int>()
    private var wasVideoPlaying = false
    private var videoCurrentPosition = 0
    private var isInFullScreen = false

    // Add this new variable to track if we need to restore UI state
    private var shouldRestoreUIOnBack = false


    private lateinit var videoClickableOverlay: View
    private lateinit var fullScreenControlsOverlay: RelativeLayout
    private lateinit var bottomShortsVideoProgressSeekBar: SeekBar
    private lateinit var videoDurationText: TextView
    private lateinit var thumbnailProgressBar: ProgressBar
    private lateinit var recyclerViewContainer: RelativeLayout


    @SuppressLint("UseKtx")
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivityUploadShortsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.tagPeopleLayout.setOnClickListener {
            showTagPeopleDialog()
        }

        binding.topicsLayout.setOnClickListener {
            showTopicsDialog()
        }

        binding.locationLayout.setOnClickListener {
            showLocationPicker()
        }


        fun updateUploadProgress(progress: Int) {
            // Get the SeekBar from the current ViewHolder in the adapter
            val uploadSeekBar = shortsAdapter?.getCurrentViewHolderUploadSeekBar()
            uploadSeekBar?.let {
                it.progress = progress
                it.visibility = if (progress > 0) View.VISIBLE else View.GONE
            } ?: run {
                Log.w("UploadShortsActivity", "Upload SeekBar not available for progress update")
            }
        }

        fun updateUploadProgressForPosition(position: Int, progress: Int) {
            val uploadSeekBar = shortsAdapter?.getViewHolderUploadSeekBar(position)
            uploadSeekBar?.let {
                it.progress = progress
                it.visibility = if (progress > 0) View.VISIBLE else View.GONE
            } ?: run {
                Log.w("UploadShortsActivity", "Upload SeekBar not available for position $position")
            }
        }

        setContentView(binding.root)

        feedUploadViewModel = ViewModelProvider(this)[FeedUploadViewModel::class.java]

        //EventBus.getDefault().register(this)

        // Enable the Up button for back navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        videoUri = intent.getParcelableExtra(EXTRA_VIDEO_URI)!!

        binding.moreButton.setOnClickListener { view ->
            showOptionsMenu(view)
        }

        binding.addMoreFeed.setOnClickListener {

        }

        videoPickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                Log.d("VideoDebug", "onActivityResult callback triggered")
                if (result.resultCode == RESULT_OK) {
                    setAddMoreFeedVisible()
                    val newVideosList: MutableList<FeedMultipleVideos> = mutableListOf()
                    fileType = "mixed_files"
                    val data = result.data

                    if (!addMoreFeedFiles) {
                        videosList.clear()
                    }

                    val videoPaths = data?.getStringArrayListExtra("video_url")
                    val uriString = data?.getStringArrayListExtra("vUri")

                    val videoUri: MutableList<Uri> = mutableListOf()
                    val videoPathList: MutableList<String> = mutableListOf()
                    if (uriString != null && videoPaths != null) {

                        feedUploadViewModel.mixedFilesCount += videoPaths.size
                        Log.d(
                            TAG,
                            "feedUploadViewModel:  mixedFilesCount = ${feedUploadViewModel.mixedFilesCount}"
                        )
                        for (uri in uriString) {
                            val vUri = uri.toUri()
                            videoUri.add(vUri)

                            Log.d(
                                "videoThumbnail",
                                "onCreate: Uri $uri videoUri $videoUri vUri $vUri"
                            )
                        }
                        for (i in videoPaths.indices) {
                            val videoPath = videoPaths[i]
                            val videoPathUri = videoUri[i]

                            Log.d("videoThumbnail", "Video Path: $videoPath")

                            // Assuming videoPathList and videosList are your data holders
                            videoPathList.add(videoPath)

                            // Get additional information
                            val durationString = getFormattedDuration(videoPath)
                            val fileName = getFileNameFromLocalPath(videoPath)

                            val videoThumbnail = getFirstFrameAsThumbnail(videoUri[i])
                            Log.d("videoThumbnail", "onCreate videoThumbnail: $videoThumbnail")
                            val videoItem = FeedMultipleVideos(
                                videoPath,
                                durationString,
                                fileName,
                                videoPathUri.toString(),
                                videoThumbnail,
                            )
                            videosList.add(videoItem)
                            newVideosList.add(videoItem)
                        }
                    }

                    Log.d(
                        TAG,
                        "onCreate: videoUri.size ${videoUri.size} videoPathList.size ${videoPathList.size}"
                    )
                    videoUris.addAll(videoUri)
                    this.videoPaths.addAll(videoPathList)

                    val arrayList: ArrayList<FeedMultipleVideos> = ArrayList(videosList)
                    if (addMoreFeedFiles) {
                        multipleFeedFilesPagerAdapter = MultipleFeedFilesPagerAdapter(
                            this,
                            isFullScreen = true,

                        )
                        binding.viewPager.adapter = multipleFeedFilesPagerAdapter

                        for (video in newVideosList) {
                            feedUploadViewModel.addMixedFeedUploadDataClass(
                                MixedFeedUploadDataClass(
                                    videos = video, fileTypes = "video"
                                )
                            )
                        }

                        val mixedFeedFiles = feedUploadViewModel.getMixedFeedUploadDataClass()
                        multipleFeedFilesPagerAdapter?.setMixedFeedUploadDataClass(
                            mixedFeedFiles
                        )
                    } else {
                        for (video in newVideosList) {
                            feedUploadViewModel.addMixedFeedUploadDataClass(
                                MixedFeedUploadDataClass(
                                    videos = video, fileTypes = "video"
                                )
                            )
                        }
                        // Initialize the adapter with proper parameters
//                        multipleSelectedFeedVideoAdapter = MultipleSelectedFeedVideoAdapter(
//                            this, arrayList, this
//                        )
                        binding.viewPager.adapter = multipleSelectedFeedVideoAdapter
                    }

                    binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
                    binding.viewPager.registerOnPageChangeCallback(object :
                        ViewPager2.OnPageChangeCallback() {
                        override fun onPageScrolled(
                            position: Int,
                            positionOffset: Float,
                            positionOffsetPixels: Int
                        ) {
                            // Implementation can be added here if needed
                        }

                        @SuppressLint("SetTextI18n")
                        override fun onPageSelected(position: Int) {
                            // This method will be invoked when a new page becomes selected.
                            Log.d("ViewPager2", "Page selected: $position")
                            binding.selectCoverText.visibility = View.VISIBLE
                            if (!addMoreFeedFiles) {
                                val videoDetails = multipleSelectedFeedVideoAdapter.getVideoDetails(position)
                                Log.d("ViewPager2", "Page selected videoDetails: $videoDetails")
                                val handler = Handler(Looper.getMainLooper())
                                handler.postDelayed({
                                    Log.d(
                                        "ViewPager2",
                                        "File name: ${videoDetails.fileName} Duration: ${videoDetails.videoDuration}"
                                    )
                                    binding.selectCoverText.text =
                                        "File name: ${videoDetails.fileName} \nDuration: ${videoDetails.videoDuration}"
                                }, 500)

                                lifecycleScope.launch(Dispatchers.IO) {
                                    val videoThumbnails = extractThumbnailsFromVideos(videoDetails.videoUri.toUri())

                                    // Switch to the main thread to update the RecyclerView
                                    withContext(Dispatchers.Main) {
                                        setupRecyclerView(videoThumbnails, videoDetails)
                                    }
                                }
                            } else {
                                val videoDetails = multipleFeedFilesPagerAdapter?.getVideoDetails(position)
                                Log.d("ViewPager2", "Page selected videoDetails: $videoDetails")
                                binding.selectCoverText.visibility = View.VISIBLE

                                if (videoDetails != null) {
                                    binding.selectCoverText.visibility = View.VISIBLE
                                    val handler = Handler(Looper.getMainLooper())
                                    handler.postDelayed({
                                        binding.selectCoverText.text =
                                            "File name: ${videoDetails.fileName} \nDuration: ${videoDetails.videoDuration}"
                                    }, 500)

                                    lifecycleScope.launch(Dispatchers.IO) {
                                        val videoThumbnails = extractThumbnailsFromVideos(videoDetails.videoUri.toUri())

                                        // Switch to the main thread to update the RecyclerView
                                        withContext(Dispatchers.Main) {
                                            binding.selectCoverText.visibility = View.VISIBLE
                                            setupRecyclerView(videoThumbnails, videoDetails)
                                        }
                                    }
                                }
                            }
                        }

                        override fun onPageScrollStateChanged(state: Int) {
                            // Called when the scroll state changes:
                            // SCROLL_STATE_IDLE, SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING
                            when (state) {
                                ViewPager2.SCROLL_STATE_IDLE -> {
                                    // The pager is in an idle, settled state.
                                }

                                ViewPager2.SCROLL_STATE_DRAGGING -> {
                                    // The user is dragging the pager.
                                }

                                ViewPager2.SCROLL_STATE_SETTLING -> {
                                    // The pager is settling to a final position.
                                }
                            }
                        }
                    })

                    // Setup CircleIndicator for ViewPager2
                    val indicator = findViewById<CircleIndicator3>(R.id.circleIndicator)
                    indicator.setViewPager(binding.viewPager)

                    // Ensure visibility settings are correct
                    binding.recyclerView2.visibility = View.VISIBLE
                    binding.shortVideoThumbNail.visibility = View.VISIBLE
                    binding.shortThumbNail.visibility = View.GONE
                    binding.selectCoverText.visibility = View.VISIBLE
                }
            }

        // Display the video using Glide
        videoUri.let {
            Glide.with(this@UploadShortsActivity)
                .load(it)
                .into(binding.shortThumbNail)
        }

        binding.postButton.setOnClickListener {
            if (isThumbnailClicked) {
                // Execute function for when thumbnail is clicked
                uploadThumbnail()
            } else {
                // Execute another function when thumbnail is not clicked
                setFirstFrameAsThumbnail()
            }
        }

        binding.topicsLayout.setOnClickListener {
            val intent = Intent(this@UploadShortsActivity, TopicsActivity::class.java)
            startActivityForResult(intent, REQUEST_TOPICS_ACTIVITY)
        }

        cancelShortsUpload()

        GlobalScope.launch(Dispatchers.IO) {
            val videoThumbnails = extractThumbnailsFromVideos()

            // Switch to the main thread to update the RecyclerView
            withContext(Dispatchers.Main) {
                setupRecyclerView(videoThumbnails)
            }
        }

        binding.interactionsBox.setOnClickListener {
            ImagePicker.with(this).cropSquare().compress(512).maxResultSize(512, 512)
                .createIntent { intent: Intent ->
                    imagePickLauncher?.launch(intent)
                }
        }

        // Enhanced onPageSelected callback with proper caching
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                Log.d("ViewPager2", "Page selected: $position")
                binding.selectCoverText.visibility = View.VISIBLE

                // Cancel any ongoing thumbnail extraction for previous positions
                thumbnailExtractionJobs.values.forEach { it.cancel() }
                thumbnailExtractionJobs.clear()

                if (!addMoreFeedFiles) {
                    val videoDetails = multipleSelectedFeedVideoAdapter.getVideoDetails(position)
                    handleVideoSelection(videoDetails, position)
                } else {
                    val videoDetails = multipleFeedFilesPagerAdapter?.getVideoDetails(position)
                    if (videoDetails != null) {
                        handleVideoSelection(videoDetails, position)
                    }
                }
            }
        })

        initializeViews()
        setupClickListeners()
        setupPlayButton()
    }

    private fun initializeViews() {
        // Toolbar elements
        toolbar = findViewById(R.id.toolbar)
        cancelButton = findViewById(R.id.cancelButton)
        titleTextView = findViewById(R.id.titleTextView)
        moreButton = findViewById(R.id.moreButton)

        // Video thumbnail section
        shortVideoThumbNail = findViewById(R.id.shortVideoThumbNail)
        shortThumbNail = findViewById(R.id.shortThumbNail)
        viewPager = findViewById(R.id.viewPager)
        circleIndicator = findViewById(R.id.circleIndicator)
        playButton = findViewById(R.id.playButton)
        pauseButton = findViewById(R.id.pauseButton)
        videoView = findViewById(R.id.videoView)

        // Full-screen video container
        fullScreenVideoContainer = findViewById(R.id.fullScreenVideoContainer)
        fullScreenVideoView = findViewById(R.id.fullScreenVideoView)
        fullScreenPlayPauseButton = findViewById(R.id.fullScreenPlayPauseButton)
        closeFullScreenButton = findViewById(R.id.closeFullScreenButton)

        // Select cover section
        selectCoverText = findViewById(R.id.selectCoverText)
        recyclerView2 = findViewById(R.id.recyclerView2)
        interactionsBox = findViewById(R.id.interactionsBox)
        saveChanges = findViewById(R.id.saveChanges)

        // Caption input
        editTextText = findViewById(R.id.editTextText)

        // Main container
        iconLayout = findViewById(R.id.iconLayout)

        // Filter and chips
        filterIndicator = findViewById(R.id.filterIndicator)
        topicsChipsContainer = findViewById(R.id.topicsChipsContainer)
        locationBadge = findViewById(R.id.locationBadge)

        // Clickable options
        tagPeopleLayout = findViewById(R.id.tagPeopleLayout)
        topicsLayout = findViewById(R.id.topics_layout)
        locationLayout = findViewById(R.id.location_layout)
        addMoreFeedLayout = findViewById(R.id.addMoreFeed)

        // Text views inside the options
        topicsText = findViewById(R.id.topicsText)

        // RecyclerView
        feedRecyclerView = findViewById(R.id.feedRecyclerView)

        // Bottom buttons
        buttonsLayout = findViewById(R.id.buttonsLayout)
        draftButton = findViewById(R.id.draftButton)
        postButton = findViewById(R.id.postButton)

        videoClickableOverlay = findViewById(R.id.videoClickableOverlay)
        fullScreenControlsOverlay = findViewById(R.id.fullScreenControlsOverlay)
        bottomShortsVideoProgressSeekBar = findViewById(R.id.bottomShortsVideoProgressSeekBar)
        videoDurationText = findViewById(R.id.videoDurationText)
        thumbnailProgressBar = findViewById(R.id.thumbnailProgressBar)
        recyclerViewContainer = findViewById(R.id.recyclerViewContainer)


    }

    private fun setupClickListeners() {
        tagPeopleLayout.setOnClickListener {
            Log.d("FilterDebug", "Tag people clicked")
            showTagPeopleDialog()
        }
        topicsLayout.setOnClickListener {
            Log.d("FilterDebug", "Topics clicked")
            showTopicsDialog()
        }
        locationLayout.setOnClickListener {
            Log.d("FilterDebug", "Location clicked")
            showLocationPicker()
        }

    }

    private fun cancelShortsUpload() {
        binding.cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun setupPlayButton() {

        Log.d(TAG, "setupPlayButton: videoUri=$videoUri, fileType=$fileType")
        playButton.setImageResource(R.drawable.baseline_play_arrow_24)

        val isVideo = if (::videoUri.isInitialized && videoUri != Uri.EMPTY) {
            isVideoFile(videoUri).also { Log.d(TAG, "isVideoFile result: $it") }
        } else {
            (fileType == "video").also { Log.d(TAG, "fileType check: $it") }
        }

        binding.playButton.visibility = if (isVideo) View.VISIBLE else View.GONE
        Log.d(TAG, "playButton visibility set to: ${if (isVideo) "VISIBLE" else "GONE"}")

        // Create a common click listener for all video-related views
        val videoClickListener = View.OnClickListener {
            if (::videoUri.isInitialized && videoUri != Uri.EMPTY && isVideo) {
                Log.d(TAG, "Video area clicked, playing video: $videoUri")
                playVideoInFullScreen(videoUri)
            } else {
                Log.e(TAG, "Cannot play video: videoUri=$videoUri, isVideo=$isVideo")
                Toast.makeText(this, "Cannot play video", Toast.LENGTH_SHORT).show()
            }
        }

        // Apply the click listener to all video-related views
        binding.playButton.setOnClickListener(videoClickListener)
        binding.shortVideoThumbNail.setOnClickListener(videoClickListener)
        binding.shortThumbNail.setOnClickListener(videoClickListener)
        binding.videoView.setOnClickListener(videoClickListener)
        binding.viewPager.setOnClickListener(videoClickListener)

        // Add the new clickable overlay
        videoClickableOverlay.setOnClickListener(videoClickListener)

        // If you want the entire video container to be clickable
        binding.shortVideoThumbNail.setOnClickListener(videoClickListener)
    }

    fun playVideoInFullScreen(uri: Uri) {

        if (!isVideoFile(uri)) {
            Toast.makeText(this, "File is not a video", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            isInFullScreen = true
            shouldRestoreUIOnBack = true

            // Store current UI state
            storeOriginalUIState()

            hideSystemUI()

            // Save current video state
            videoCurrentPosition = try {
                binding.videoView.currentPosition
            } catch (e: Exception) { 0 }

            wasVideoPlaying = try {
                binding.videoView.isPlaying
            } catch (e: Exception) { false }

            // Pause normal video
            try { binding.videoView.pause() } catch (e: Exception) {}

            // Hide normal UI
            hideNonFullScreenElementsInstant()

            // Show fullscreen UI
            fullScreenVideoContainer.visibility = View.VISIBLE
            fullScreenPlayPauseButton.visibility = View.GONE
            binding.bottomShortsVideoProgressSeekBar.visibility = View.VISIBLE
            binding.videoDurationText.visibility = View.VISIBLE

            binding.bottomShortsVideoProgressSeekBar.progress = 0

            fullScreenVideoView.setVideoURI(uri)
            fullScreenVideoView.setOnPreparedListener { mediaPlayer ->

                mediaPlayer.isLooping = true

                // Get video dimensions
                val videoWidth = mediaPlayer.videoWidth
                val videoHeight = mediaPlayer.videoHeight

                val videoProportion = videoWidth.toFloat() / videoHeight.toFloat()

                val screenWidth = fullScreenVideoContainer.width.toFloat()
                val screenHeight = fullScreenVideoContainer.height.toFloat()

                val screenProportion = screenWidth / screenHeight

                val lp = fullScreenVideoView.layoutParams

                if (videoProportion > screenProportion) {
                    // Landscape video -> fit width, black bars top & bottom
                    lp.width = ViewGroup.LayoutParams.MATCH_PARENT
                    lp.height = (screenWidth / videoProportion).toInt()
                } else {
                    // Portrait video -> fit height, black bars sides
                    lp.width = (screenHeight * videoProportion).toInt()
                    lp.height = ViewGroup.LayoutParams.MATCH_PARENT
                }

                fullScreenVideoView.layoutParams = lp

                // Resume previous position
                if (videoCurrentPosition > 0) {
                    fullScreenVideoView.seekTo(videoCurrentPosition)
                }

                fullScreenVideoView.start()
                updateSeekBarAndDuration()
            }

            fullScreenVideoView.setOnErrorListener { _, what, extra ->
                Log.e("PlayVideo", "Error playing video: what=$what, extra=$extra")
                exitFullScreenInstant()
                Toast.makeText(this, "Error playing video", Toast.LENGTH_SHORT).show()
                true
            }

            // Tap to play/pause
            fullScreenVideoView.setOnClickListener {
                if (fullScreenVideoView.isPlaying) {
                    fullScreenVideoView.pause()
                    showBlinkingIcon(R.drawable.baseline_play_arrow_24)
                    binding.bottomShortsVideoProgressSeekBar.visibility = View.VISIBLE
                    binding.videoDurationText.visibility = View.VISIBLE
                } else {
                    fullScreenVideoView.start()
                    showBlinkingIcon(R.drawable.baseline_pause_white_24)
                    binding.bottomShortsVideoProgressSeekBar.visibility = View.VISIBLE
                    binding.videoDurationText.visibility = View.VISIBLE
                    updateSeekBarAndDuration()
                }
            }

            // Close fullscreen button
            binding.closeFullScreenButton.setOnClickListener {
                exitFullScreenInstant()
            }

            // Seekbar listener
            binding.bottomShortsVideoProgressSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        val duration = fullScreenVideoView.duration
                        if (duration > 0) {
                            val seekPosition = (progress * duration) / 100
                            fullScreenVideoView.seekTo(seekPosition)
                            updateVideoDurationText(seekPosition, duration)
                        }
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    if (fullScreenVideoView.isPlaying) {
                        fullScreenVideoView.pause()
                        showBlinkingIcon(R.drawable.baseline_play_arrow_24)
                    }
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    fullScreenVideoView.start()
                    showBlinkingIcon(R.drawable.baseline_pause_white_24)
                    updateSeekBarAndDuration()
                }
            })
        } catch (e: Exception) {
            Log.e("PlayVideo", "Error setting up video: ${e.message}")
            exitFullScreenInstant()
            Toast.makeText(this, "Cannot play video", Toast.LENGTH_SHORT).show()
        }
    }

    private fun storeOriginalUIState() {

        originalUIState.clear()

            // Store visibility of all relevant UI elements
            originalUIState[binding.shortVideoThumbNail] = binding.shortVideoThumbNail.visibility
            originalUIState[binding.viewPager] = binding.viewPager.visibility
            originalUIState[binding.playButton] = binding.playButton.visibility
            originalUIState[binding.pauseButton] = binding.pauseButton.visibility
            originalUIState[binding.selectCoverText] = binding.selectCoverText.visibility
            originalUIState[binding.recyclerView2] = binding.recyclerView2.visibility
            originalUIState[binding.interactionsBox] = binding.interactionsBox.visibility
            originalUIState[binding.editTextText] = binding.editTextText.visibility
            originalUIState[binding.iconLayout] = binding.iconLayout.visibility
            originalUIState[binding.buttonsLayout] = binding.buttonsLayout.visibility
            originalUIState[binding.toolbar] = binding.toolbar.visibility
            originalUIState[binding.circleIndicator] = binding.circleIndicator.visibility
            originalUIState[binding.topicsChipsContainer] = binding.topicsChipsContainer.visibility
            originalUIState[binding.locationBadge] = binding.locationBadge.visibility
            originalUIState[binding.tagPeopleLayout] = binding.tagPeopleLayout.visibility
            originalUIState[binding.topicsLayout] = binding.topicsLayout.visibility
            originalUIState[binding.locationLayout] = binding.locationLayout.visibility
            originalUIState[binding.addMoreFeed] = binding.addMoreFeed.visibility
            originalUIState[binding.draftButton] = binding.draftButton.visibility
            originalUIState[binding.postButton] = binding.postButton.visibility
            originalUIState[binding.shortThumbNail] = binding.shortThumbNail.visibility


    }

    private fun hideNonFullScreenElementsInstant() {
        // Hide elements with no animation or delay
        binding.shortVideoThumbNail.visibility = View.GONE
        binding.viewPager.visibility = View.GONE
        binding.playButton.visibility = View.GONE
        binding.pauseButton.visibility = View.GONE
        binding.selectCoverText.visibility = View.GONE
        binding.recyclerView2.visibility = View.GONE
        binding.interactionsBox.visibility = View.GONE
        binding.editTextText.visibility = View.GONE
        binding.iconLayout.visibility = View.GONE
        binding.buttonsLayout.visibility = View.GONE
        binding.toolbar.visibility = View.GONE
        binding.circleIndicator.visibility = View.GONE
        binding.topicsChipsContainer.visibility = View.GONE
        binding.locationBadge.visibility = View.GONE
        binding.tagPeopleLayout.visibility = View.GONE
        binding.topicsLayout.visibility = View.GONE
        binding.locationLayout.visibility = View.GONE
        binding.addMoreFeed.visibility = View.GONE
        binding.draftButton.visibility = View.GONE
        binding.postButton.visibility = View.GONE
        binding.shortThumbNail.visibility = View.GONE

        Log.d(TAG, "Non-fullscreen elements hidden")
    }

    private fun restoreOriginalUIState() {
        Log.d(TAG, "Restoring original UI state with ${originalUIState.size} elements")

        // Restore all stored UI elements to their original visibility
        originalUIState.forEach { (view, visibility) ->
            try {
                view.visibility = visibility
                Log.d(TAG, "Restored ${view.javaClass.simpleName} to visibility: $visibility")
            } catch (e: Exception) {
                Log.e(TAG, "Error restoring view visibility: ${e.message}")
            }
        }

        // Ensure thumbnail and play button are correctly set based on content
        if (::videoUri.isInitialized && isVideoFile(videoUri)) {
            binding.shortThumbNail.visibility = originalUIState[binding.shortThumbNail] ?: View.VISIBLE
            binding.playButton.visibility = originalUIState[binding.playButton] ?: View.VISIBLE
            binding.shortVideoThumbNail.visibility = originalUIState[binding.shortVideoThumbNail] ?: View.VISIBLE
            Glide.with(this@UploadShortsActivity)
                .load(videoUri)
                .into(binding.shortThumbNail)
        }

        // Restore ViewPager adapter and position
        binding.viewPager.adapter = if (addMoreFeedFiles) {
            multipleFeedFilesPagerAdapter
        } else if (fileType == "video") {
            multipleSelectedFeedVideoAdapter
        } else {
            multipleAudioAdapter
        }

        // Restore CircleIndicator
        val indicator = findViewById<CircleIndicator3>(R.id.circleIndicator)
        indicator.setViewPager(binding.viewPager)

        // Force a layout pass to ensure changes are applied
        binding.root.post {
            binding.root.requestLayout()
            binding.root.invalidate()
        }
    }

    private fun exitFullScreenInstant() {
        Log.d(TAG, "exitFullScreenInstant called")

        try {
            isInFullScreen = false
            shouldRestoreUIOnBack = false

            showSystemUI()

            // Get current position from fullscreen video
            val currentFullScreenPosition = try {
                if (fullScreenVideoView.isPlaying) fullScreenVideoView.currentPosition else 0
            } catch (e: Exception) { 0 }

            // Stop fullscreen video immediately - no delays
            try {
                fullScreenVideoView.stopPlayback()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping fullscreen video: ${e.message}")
            }

            // Hide full screen elements instantly
            fullScreenVideoContainer.visibility = View.GONE
            binding.bottomShortsVideoProgressSeekBar.visibility = View.GONE
            binding.videoDurationText.visibility = View.GONE
            fullScreenPlayPauseButton.visibility = View.GONE
            binding.closeFullScreenButton.visibility = View.GONE

            // Use a handler to ensure UI operations happen on the main thread
            Handler(Looper.getMainLooper()).post {
                // Restore UI to EXACT original state instantly
                restoreOriginalUIState()

                // Clear the stored state after restoration
                originalUIState.clear()
                Log.d(TAG, "Original UI state cleared")

                // Restore normal video view state
                restoreNormalVideoView(currentFullScreenPosition)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in exitFullScreenInstant: ${e.message}")
            // Force clear state even if there's an error
            originalUIState.clear()
            shouldRestoreUIOnBack = false
            isInFullScreen = false
            // Fallback to restore basic UI
            restoreOriginalUIState()
        }
    }

    private fun updateSeekBarAndDuration() {
        val currentPosition = fullScreenVideoView.currentPosition
        val duration = fullScreenVideoView.duration

        if (duration > 0) {
            val progress = (currentPosition * 100) / duration
            binding.bottomShortsVideoProgressSeekBar.progress = progress
            updateVideoDurationText(currentPosition, duration)
        }

        // Continue updating if video is playing and still in fullscreen
        if (fullScreenVideoView.isPlaying && isInFullScreen) {
            Handler(Looper.getMainLooper()).postDelayed({
                updateSeekBarAndDuration()
            }, 100)
        }
    }

    @SuppressLint("DefaultLocale")
    private fun updateVideoDurationText(currentPosition: Int, duration: Int) {
        val remainingTimeMs = duration - currentPosition
        val remainingSeconds = remainingTimeMs / 1000
        val remainingFormatted = String.format("%d:%02d", remainingSeconds / 60, remainingSeconds % 60)
        binding.videoDurationText.text = remainingFormatted
    }

    private fun restoreNormalVideoView(currentFullScreenPosition: Int) {
        // If there was a video playing before, restore it
        if (::videoUri.isInitialized && videoUri != Uri.EMPTY) {
            try {
                binding.videoView.let { normalVideoView ->
                    Log.d(TAG, "Restoring normal video view")

                    // Set up the video URI
                    normalVideoView.setVideoURI(videoUri)

                    normalVideoView.setOnPreparedListener { mediaPlayer ->
                        // Determine which position to restore to
                        val positionToRestore = if (currentFullScreenPosition > 0) {
                            currentFullScreenPosition
                        } else {
                            videoCurrentPosition
                        }

                        // Set the position
                        if (positionToRestore > 0) {
                            normalVideoView.seekTo(positionToRestore)
                        }

                        // Resume playback if it was playing before
                        if (wasVideoPlaying) {
                            normalVideoView.start()
                            // Update play/pause button state
                            binding.playButton.visibility = View.GONE
                            binding.pauseButton.visibility = View.VISIBLE
                            Log.d(TAG, "Video resumed playing")
                        } else {
                            // Show play button if video was paused
                            binding.playButton.visibility = View.VISIBLE
                            binding.pauseButton.visibility = View.GONE
                            Log.d(TAG, "Video restored in paused state")
                        }
                    }

                    normalVideoView.setOnErrorListener { _, what, extra ->
                        Log.e("RestoreVideo", "Error restoring video: what=$what, extra=$extra")
                        // Reset to default state on error
                        binding.playButton.visibility = View.VISIBLE
                        binding.pauseButton.visibility = View.GONE
                        true
                    }
                }
            } catch (e: Exception) {
                Log.e("RestoreVideo", "Exception restoring video: ${e.message}")
                // Reset to default state on exception
                binding.playButton.visibility = View.VISIBLE
                binding.pauseButton.visibility = View.GONE
            }
        }
    }

    // Modified onBackPressed method to handle UI restoration
    @Deprecated("This method has been deprecated in favor of using the OnBackPressedDispatcher")
    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed called - isInFullScreen: $isInFullScreen, shouldRestoreUIOnBack: $shouldRestoreUIOnBack")

        if (isInFullScreen) {
            exitFullScreenInstant()
        } else {
            // If we need to restore UI state (coming back from fullscreen interaction)
            if (shouldRestoreUIOnBack && originalUIState.isNotEmpty()) {
                restoreOriginalUIState()
                Handler(Looper.getMainLooper()).postDelayed({
                    originalUIState.clear()
                    shouldRestoreUIOnBack = false
                }, 100)
            }
            super.onBackPressed()
        }
    }

    private fun showBlinkingIcon(iconResId: Int) {

        fullScreenPlayPauseButton.setImageResource(iconResId)
        fullScreenPlayPauseButton.visibility = View.VISIBLE

        val fadeOutAnimation = AlphaAnimation(1.0f, 0.0f).apply {
            duration = 3000
            repeatCount = 0
        }

        fadeOutAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                fullScreenPlayPauseButton.visibility = View.GONE
            }
            override fun onAnimationRepeat(animation: Animation?) {}
        })

        fullScreenPlayPauseButton.startAnimation(fadeOutAnimation)
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun showSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, window.decorView).show(WindowInsetsCompat.Type.systemBars())
    }

    @SuppressLint("SetTextI18n")
    private fun handleVideoSelection(videoDetails: FeedMultipleVideos, position: Int) {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            binding.selectCoverText.text =
                "File name: ${videoDetails.fileName} \nDuration: ${videoDetails.videoDuration}"
            binding.selectCoverText.visibility = View.VISIBLE
            binding.recyclerView2.visibility = View.VISIBLE
            binding.playButton.visibility = View.VISIBLE
        }, 100)

        val cacheKey = videoDetails.videoUri
        if (thumbnailCache.containsKey(cacheKey)) {
            val cachedThumbnails = thumbnailCache[cacheKey]
            if (cachedThumbnails != null) {
                setupRecyclerView(listOf(cachedThumbnails), videoDetails)
                updateVideoThumbnail(cachedThumbnails, position)
                binding.playButton.visibility = View.VISIBLE
                binding.recyclerView2.visibility = View.VISIBLE
            }
        } else {
            binding.thumbnailProgressBar.visibility = View.VISIBLE
            val job = lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val videoThumbnails = extractThumbnailsFromVideos(videoDetails.videoUri.toUri())
                    if (videoThumbnails.isNotEmpty()) {
                        thumbnailCache[cacheKey] = videoThumbnails[0]
                    }
                    withContext(Dispatchers.Main) {
                        binding.thumbnailProgressBar.visibility = View.GONE
                        if (binding.viewPager.currentItem == position) {
                            setupRecyclerView(videoThumbnails, videoDetails)
                            updateVideoThumbnail(videoThumbnails[0], position)
                            binding.playButton.visibility = View.VISIBLE
                            binding.recyclerView2.visibility = View.VISIBLE
                            binding.selectCoverText.visibility = View.VISIBLE
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        binding.thumbnailProgressBar.visibility = View.GONE
                    }
                    Log.e("ThumbnailExtraction", "Error extracting thumbnails", e)
                }
            }
            thumbnailExtractionJobs[position] = job
        }
    }

    private fun updateVideoThumbnail(thumbnail: Bitmap, position: Int) {
        Glide.with(this@UploadShortsActivity)
            .load(thumbnail)
            .centerCrop()
            .into(binding.shortThumbNail)
        binding.playButton.visibility = View.VISIBLE
        binding.recyclerView2.visibility = View.VISIBLE
        binding.selectCoverText.visibility = View.VISIBLE
    }

    private fun setupRecyclerView(thumbnails: List<Bitmap>, videoDetails: FeedMultipleVideos) {
        if (thumbnails.isEmpty()) {
            Log.w("RecyclerView", "No thumbnails available for video: ${videoDetails.fileName}")
            binding.recyclerView2.visibility = View.GONE
            binding.playButton.visibility = View.VISIBLE
            binding.selectCoverText.visibility = View.VISIBLE
            return
        }

        binding.recyclerView2.visibility = View.VISIBLE
        binding.selectCoverText.visibility = View.VISIBLE
        binding.playButton.visibility = View.VISIBLE

        val thumbnailAdapter = FeedVideoThumbnailAdapter(
            thumbnails = thumbnails,
            clickListener = object : FeedVideoThumbnailAdapter.ThumbnailClickListener {
                override fun onThumbnailClick(thumbnail: Bitmap, videoDetails: FeedMultipleVideos) {
                    this@UploadShortsActivity.thumbnail = thumbnail
                    this@UploadShortsActivity.isThumbnailSelected = true
                    Glide.with(this@UploadShortsActivity)
                        .load(thumbnail)
                        .into(binding.shortThumbNail)
                    binding.playButton.visibility = View.VISIBLE
                    binding.recyclerView2.visibility = View.VISIBLE
                    binding.selectCoverText.visibility = View.VISIBLE
                }

                override fun onThumbnailLongClick(thumbnail: Bitmap, videoDetails: FeedMultipleVideos, position: Int) {}
                override fun showAttachmentDialog() {}
                override fun backFromShortsUpload() {}
                override fun cancelShortsUpload() {}
                override fun saveBitmapToCache2(context: Context, bitmap: Bitmap): String {
                    return saveBitmapToCache(context, bitmap) ?: ""
                }
                override fun saveBitmapToFile(bitmap: Bitmap, context: Context): File {
                    val cacheDir = context.cacheDir
                    val file = File(cacheDir, "thumbnail_${System.currentTimeMillis()}.png")
                    try {
                        FileOutputStream(file).use { outputStream ->
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                        }
                    } catch (e: Exception) {
                        Log.e("UploadShortsActivity", "Error saving bitmap to file: ${e.message}")
                    }
                    return file
                }
                override fun handleDocumentUriToUploadReturn(uri: Uri): FeedMultipleDocumentsDataClass {
                    return FeedMultipleDocumentsDataClass()
                }
            }
        )

        thumbnailAdapter.setVideoDetails(videoDetails)
        val layoutManager = LinearLayoutManager(
            this@UploadShortsActivity, LinearLayoutManager.HORIZONTAL, false
        )
        binding.recyclerView2.layoutManager = layoutManager
        binding.recyclerView2.adapter = thumbnailAdapter

        binding.recyclerView2.post {
            val itemCount = thumbnails.size
            val recyclerWidth = binding.recyclerView2.width
            val paddingHorizontal = binding.recyclerView2.paddingStart + binding.recyclerView2.paddingEnd
            val availableWidth = recyclerWidth - paddingHorizontal
            if (itemCount > 0 && availableWidth > 0) {
                val itemWidth = availableWidth / itemCount
                thumbnailAdapter.setItemWidth(itemWidth)
                Log.d("RecyclerView", "Setting item width: $itemWidth for $itemCount items, available width: $availableWidth")
            }
        }
    }

    private fun handleFileFullScreen() {
        when {
            // Video files
            ::videoUri.isInitialized && isVideoFile(videoUri) -> {
                playVideoInFullScreen(videoUri)
            }

            // Fallback - try to determine from videoUri
            ::videoUri.isInitialized -> {
                val mimeType = contentResolver.getType(videoUri)
                when {
                    mimeType?.startsWith("video/") == true -> playVideoInFullScreen(videoUri)

                }
            }
            else -> {
                Toast.makeText(this, "No file available to display", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isVideoFile(uri: Uri): Boolean {
        val mimeType = contentResolver.getType(uri)
        return mimeType?.startsWith("video/") == true ||
                uri.toString().endsWith(".mp4", ignoreCase = true) ||
                uri.toString().endsWith(".mov", ignoreCase = true) ||
                uri.toString().endsWith(".avi", ignoreCase = true) ||
                uri.toString().endsWith(".3gp", ignoreCase = true)
    }

    private val progressUpdateHandler = Handler(Looper.getMainLooper())
    private val progressUpdateRunnable = object : Runnable {
        override fun run() {
            if (binding.videoView.isPlaying) {

                progressUpdateHandler.postDelayed(this, 16) // 60fps updates
            }
        }
    }


    override fun onPause() {
        super.onPause()
        if (fullScreenVideoView.isPlaying) {
            fullScreenVideoView.stopPlayback()
        }
    }

    @SuppressLint("UseKtx")
    override fun onResume() {
        super.onResume()
        // No auto-resume - user must manually start video in full screen
    }

    private suspend fun extractThumbnailsFromVideos(uri: Uri): List<Bitmap> {
        return withContext(Dispatchers.IO) {
            val thumbnails = mutableListOf<Bitmap>()
            var retriever: MediaMetadataRetriever? = null

            try {
                retriever = MediaMetadataRetriever()
                retriever.setDataSource(this@UploadShortsActivity, uri)

                // Get video duration and dimensions
                val durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val duration = durationString?.toLongOrNull() ?: 0L

                val widthString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                val heightString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                val videoWidth = widthString?.toIntOrNull() ?: 0
                val videoHeight = heightString?.toIntOrNull() ?: 0

                // WhatsApp uses about 8-10 thumbnails for the timeline
                val numberOfThumbnails = 8

                if (duration > 0) {
                    for (i in 0 until numberOfThumbnails) {
                        try {
                            // Calculate time position for each thumbnail
                            val timePosition = if (i == 0) {
                                // First thumbnail at 500ms to avoid black frames
                                500L
                            } else {
                                // Evenly distribute remaining thumbnails
                                val remainingDuration = duration - 500
                                500 + (remainingDuration * i) / (numberOfThumbnails - 1)
                            }

                            val bitmap = retriever.getFrameAtTime(
                                timePosition * 1000,
                                MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                            )

                            bitmap?.let {
                                val processedBitmap = createStyleThumbnail(it, videoWidth, videoHeight)
                                thumbnails.add(processedBitmap)
                            }
                        } catch (e: Exception) {
                            Log.e("ThumbnailExtraction", "Error extracting frame at position $i", e)
                        }
                    }
                }

                retriever.release()
            } catch (e: Exception) {
                Log.e("ThumbnailExtraction", "Error setting up MediaMetadataRetriever", e)
            } finally {
                retriever?.release()
            }

            // If no thumbnails were extracted, try to get the first frame
            if (thumbnails.isEmpty()) {
                try {
                    val bitmap = ThumbnailUtils.createVideoThumbnail(
                        getRealPathFromURI(uri) ?: "",
                        MediaStore.Images.Thumbnails.MINI_KIND
                    )
                    bitmap?.let {
                        val processedBitmap = createStyleThumbnail(it, it.width, it.height)
                        thumbnails.add(processedBitmap)
                    }
                } catch (e: Exception) {
                    Log.e("ThumbnailExtraction", "Error creating video thumbnail", e)
                }
            }

            thumbnails
        }
    }

    @SuppressLint("UseKtx")
    private fun createStyleThumbnail(bitmap: Bitmap, videoWidth: Int, videoHeight: Int): Bitmap {
        val targetSize = 60 // WhatsApp timeline thumbnail size

        // Calculate crop dimensions to maintain aspect ratio
        val cropSize = minOf(bitmap.width, bitmap.height)
        val x = (bitmap.width - cropSize) / 2
        val y = (bitmap.height - cropSize) / 2

        // Create square crop
        val squareBitmap = Bitmap.createBitmap(bitmap, x, y, cropSize, cropSize)

        // Scale to target size
        val scaledBitmap = squareBitmap.scale(targetSize, targetSize)

        // Clean up intermediate bitmap if different from original
        if (squareBitmap != bitmap) {
            squareBitmap.recycle()
        }

        return scaledBitmap
    }

    private suspend fun extractThumbnailsFromVideos(): List<Bitmap> {
        return if (::videoUri.isInitialized) {
            extractThumbnailsFromVideos(videoUri)
        } else {
            emptyList()
        }
    }

    private fun setupRecyclerView(videoThumbnails: List<Bitmap>) {
        // Create a basic FeedMultipleVideos object with default values
        val defaultVideoDetails = FeedMultipleVideos(
            videoPath = "",
            videoDuration = "00:00",
            fileName = "video.mp4",
            videoUri = Uri.EMPTY.toString(),
            thumbnail = if (videoThumbnails.isNotEmpty()) videoThumbnails[0] else null
        )

        val adapter = FeedVideoThumbnailAdapter(
            thumbnails = videoThumbnails,
            clickListener = object : FeedVideoThumbnailAdapter.ThumbnailClickListener {
                override fun onThumbnailClick(thumbnail: Bitmap, videoDetails: FeedMultipleVideos) {
                    this@UploadShortsActivity.thumbnail = thumbnail
                    this@UploadShortsActivity.isThumbnailSelected = true

                    Glide.with(this@UploadShortsActivity)
                        .load(thumbnail)
                        .into(binding.shortThumbNail)
                }

                override fun onThumbnailLongClick(thumbnail: Bitmap, videoDetails: FeedMultipleVideos, position: Int) {
                    // Handle long click if needed
                }

                override fun showAttachmentDialog() {
                    // Implement if needed
                }

                override fun backFromShortsUpload() {
                    // Implement if needed
                }

                override fun cancelShortsUpload() {
                    // Implement if needed
                }

                override fun saveBitmapToCache2(context: Context, bitmap: Bitmap): String {
                    return saveBitmapToCache(context, bitmap) ?: ""
                }

                override fun saveBitmapToFile(bitmap: Bitmap, context: Context): File {
                    val cacheDir = context.cacheDir
                    val file = File(cacheDir, "thumbnail_${System.currentTimeMillis()}.png")

                    try {
                        FileOutputStream(file).use { outputStream ->
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                        }
                    } catch (e: Exception) {
                        Log.e("UploadShortsActivity", "Error saving bitmap to file: ${e.message}")
                    }

                    return file
                }

                override fun handleDocumentUriToUploadReturn(uri: Uri): FeedMultipleDocumentsDataClass {
                    return FeedMultipleDocumentsDataClass()
                }
            }
        )

        // Set video details on adapter
        adapter.setVideoDetails(defaultVideoDetails)

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerView2.layoutManager = layoutManager
        binding.recyclerView2.adapter = adapter

        // Calculate and set item width to fill the entire width
        binding.recyclerView2.post {
            val itemCount = videoThumbnails.size
            val recyclerWidth = binding.recyclerView2.width
            val paddingHorizontal = binding.recyclerView2.paddingStart + binding.recyclerView2.paddingEnd
            val availableWidth = recyclerWidth - paddingHorizontal

            if (itemCount > 0 && availableWidth > 0) {
                val itemWidth = availableWidth / itemCount

                // Set the calculated width on the adapter
                adapter.setItemWidth(itemWidth)

                Log.d("RecyclerView", "Setting item width: $itemWidth for $itemCount items, available width: $availableWidth")
            }
        }
    }

    private fun getRealPathFromURI(uri: Uri): String? {
        return try {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                val columnIndex = it.getColumnIndex(MediaStore.Video.Media.DATA)
                if (columnIndex != -1 && it.moveToFirst()) {
                    it.getString(columnIndex)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("PathResolver", "Error getting real path from URI", e)
            null
        }
    }

    private fun loadBitmapFromUri(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT < 28) {
            // For versions before Android 9 (API level 28)
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        } else {
            // For Android 9 (API level 28) and above
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        }
    }

    private fun getFirstFrameAsThumbnail(videoUri: Uri): Bitmap? {
        Log.d("getFirstFrameAsThumbnail", "getFirstFrameAsThumbnail: ")
        return try {
            val firstFrame: Bitmap? = VideoUtils.getFirstFrame(this, videoUri)
            if (firstFrame != null) {
                Log.d(TAG, "getFirstFrameAsThumbnail: thumbnail not null")
                return firstFrame // Return the first non-null frame immediately
            } else {
                Log.d(TAG, "getFirstFrameAsThumbnail: thumbnail is null")
            }
            // No frames were found; return null
            Log.d(TAG, "getFirstFrameAsThumbnail: No valid thumbnails found")
            null
        } catch (e: Exception) {
            Log.e(TAG, "getFirstFrameAsThumbnail: ${e.message}")
            e.printStackTrace()
            null // Return null in case of an exception
        }
    }

    private fun setFirstFrameAsThumbnail() {
        val firstFrame: Bitmap? = VideoUtils.getFirstFrame(this, videoUri)
        if (firstFrame != null) {
            thumbnail = firstFrame
        }
        caption = binding.editTextText.text.toString().trim()
        uploadShorts(videoUri, caption)
        val resultIntent = Intent()
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    override fun onThumbnailClick(thumbnail: Bitmap) {

        isThumbnailClicked = true
        Glide.with(this)
            .load(thumbnail)
            .into(binding.shortThumbNail)


        this.thumbnail = thumbnail

    }

    override fun setBackgroundColor(color: Color) {
        TODO("Not yet implemented")
    }

    fun saveBitmapToFile(bitmap: Bitmap, context: Context): File {
        // Get the directory for the app's private pictures directory.
        val fileDir = File(context.filesDir, "thumbnails")

        // Create the directory if it doesn't exist
        if (!fileDir.exists()) {
            fileDir.mkdirs()
        }

        // Create a unique filename for the thumbnail
//        val fileName = "thumbnail_${System.currentTimeMillis()}.png"
        val fileName = "thumbnail.png"

        // Create the file object
        val file = File(fileDir, fileName)

        try {
            // Save the bitmap to the file
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return file
    }

    private fun uploadThumbnail() {
        caption = binding.editTextText.text.toString().trim()
        uploadShorts(videoUri, caption)
        val resultIntent = Intent()
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun clearThumbnailCache() {
        thumbnailCache.clear()
        thumbnailExtractionJobs.values.forEach { it.cancel() }
        thumbnailExtractionJobs.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        clearThumbnailCache()
        binding.videoView.stopPlayback()

        // Clean up progress line view
        progressLineView?.let { view ->
            val parent = view.parent as? ViewGroup
            parent?.removeView(view)
        }
        progressLineView = null
    }

    private val pickMultipleMedia =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(10)) { uris ->
            // Callback is invoked after the user selects media items or closes the
            // photo picker.
            setAddMoreFeedVisible()
            val newImagesList: MutableList<String> = mutableListOf()
            val newCompressedImageFiles: MutableList<File> = mutableListOf()
            if (uris.isNotEmpty()) {
                if (!addMoreFeedFiles) {
                    imagesList.clear()
                }
                if (uris.isNotEmpty()) {
                    Log.d(TAG, "selected more than 1 image: ${uris.size}")
                    isMultipleImages = true

                    feedUploadViewModel.mixedFilesCount += uris.size

                    Log.d(
                        TAG,
                        "feedUploadViewModel:  mixedFilesCount = ${feedUploadViewModel.mixedFilesCount}"
                    )
                    for (uri in uris) {
                        val filePath = PathUtil.getPath(
                            this,
                            uri
                        ) // Use the utility class to get the real file path
                        Log.d("PhotoPicker", "File path: $filePath")
                        Log.d("PhotoPicker", "Selected image path from camera: $uri")
                        if (filePath != null) {
                            imagesList.add(filePath)
                            newImagesList.add(filePath)
                            val file = File(filePath)
                            lifecycleScope.launch {
                                val compressedImageFile =
                                    Compressor.compress(this@UploadShortsActivity, file)
                                Log.d(
                                    "PhotoPicker",
                                    "PhotoPicker: compressedImageFile absolutePath: ${compressedImageFile.absolutePath}"
                                )


                                val fileSizeInBytes = compressedImageFile.length()
                                val fileSizeInKB = fileSizeInBytes / 1024
                                val fileSizeInMB = fileSizeInKB / 1024

                                Log.d(
                                    "PhotoPicker",
                                    "PhotoPicker: compressedImageFile size $fileSizeInKB KB, $fileSizeInMB MB"
                                )
                                compressedImageFiles.add(compressedImageFile)
                                newCompressedImageFiles.add(compressedImageFile)

                                multipleFeedFilesPagerAdapter =
                                    MultipleFeedFilesPagerAdapter(
                                        this@UploadShortsActivity,

                                        isFullScreen = true
                                    )
                                binding.viewPager.adapter = multipleFeedFilesPagerAdapter
                                val compressedImagePath =
                                    compressedImageFile.absolutePath
                                val fileType = File(compressedImagePath).extension
                                Log.d(
                                    "newCompressedImageFiles",
                                    "newCompressedImageFiles: extension: $fileType file path: $compressedImagePath"
                                )

                                feedUploadViewModel.addMixedFeedUploadDataClass(
                                    MixedFeedUploadDataClass(
                                        images = FeedMultipleImages(
                                            imagePath = filePath,
                                            compressedImagePath = compressedImagePath
                                        ), fileTypes = "image"
                                    )
                                )


                                val mixedFeedFiles =
                                    feedUploadViewModel.getMixedFeedUploadDataClass()

                                multipleFeedFilesPagerAdapter?.setMixedFeedUploadDataClass(
                                    mixedFeedFiles
                                )

                                this@UploadShortsActivity.fileType = "mixed_files"

                                binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

                                // Setup CircleIndicator for ViewPager2
                                val indicator = findViewById<CircleIndicator3>(R.id.circleIndicator)
                                indicator.setViewPager(binding.viewPager)
                                binding.viewPager.registerOnPageChangeCallback(object :
                                    ViewPager2.OnPageChangeCallback() {
                                    override fun onPageScrolled(
                                        position: Int,
                                        positionOffset: Float,
                                        positionOffsetPixels: Int
                                    ) {
                                        // This method will be invoked when the ViewPager2 is scrolled, but not necessarily settled (user is still swiping)
                                    }

                                    override fun onPageSelected(position: Int) {
                                        // This method will be invoked when a new page becomes selected.
                                        // You can perform actions here based on the selected page position.
                                        Log.d("ViewPager2", "onPageSelected: $position")

                                        binding.recyclerView2.visibility = View.INVISIBLE
                                        binding.shortVideoThumbNail.visibility = View.VISIBLE
                                        binding.shortThumbNail.visibility = View.GONE
                                        binding.selectCoverText.visibility = View.VISIBLE
                                        binding.selectCoverText.text = ""

                                    }

                                    override fun onPageScrollStateChanged(state: Int) {
                                        // Called when the scroll state changes:
                                        // SCROLL_STATE_IDLE, SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING
                                        when (state) {
                                            ViewPager2.SCROLL_STATE_IDLE -> {
                                                // The pager is in an idle, settled state.

                                            }

                                            ViewPager2.SCROLL_STATE_DRAGGING -> {
                                                // The user is dragging the pager.

                                            }

                                            ViewPager2.SCROLL_STATE_SETTLING -> {
                                                // The pager is settling to a final position.

                                            }
                                        }
                                    }
                                })
                                // Ensure visibility settings are correct
                                binding.recyclerView2.visibility = View.INVISIBLE
                                binding.shortVideoThumbNail.visibility = View.VISIBLE
                                binding.shortThumbNail.visibility = View.GONE
                                binding.selectCoverText.visibility = View.VISIBLE
                                binding.selectCoverText.text = ""
                            }
                        }

                    }

                }
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }


    private fun setAddMoreFeedVisible() {
        binding.addMoreFeed.visibility = View.VISIBLE


    }

    private fun showOptionsMenu(anchor: View) {
        val popup = PopupMenu(this, anchor, Gravity.END)

        // Apply custom style and force show icons (optional)
        try {
            val fieldPopup = PopupMenu::class.java.getDeclaredField("mPopup")
            fieldPopup.isAccessible = true
            val menuPopupWindow = fieldPopup.get(popup)
            // Note: MenuPopupWindow.setForceShowIcon() might not be available in all versions
            // You may need to handle this differently based on your target SDK
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Inflate the menu
        popup.menuInflater.inflate(R.menu.post_options_menu, popup.menu)

        // Set menu item click listener
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_report -> {
                    handleReportPost()
                    true
                }
                R.id.menu_block_user -> {
                    handleBlockUser()
                    true
                }
                R.id.menu_mute_user -> {
                    handleMuteUser()
                    true
                }
                R.id.menu_copy_link -> {
                    handleCopyLink()
                    true
                }
                R.id.menu_save_post -> {
                    handleSavePost()
                    true
                }
                R.id.menu_not_interested -> {
                    handleNotInterested()
                    true
                }
                else -> false
            }
        }

        // Show the popup menu
        popup.show()
    }

    private fun handleReportPost() {
        // Show report dialog or navigate to report screen
        Toast.makeText(this, "Report post", Toast.LENGTH_SHORT).show()
    }

    private fun handleBlockUser() {
        // Show confirmation dialog and block user
        Toast.makeText(this, "User blocked", Toast.LENGTH_SHORT).show()
    }

    private fun handleMuteUser() {
        // Show confirmation dialog and mute user
        Toast.makeText(this, "User muted", Toast.LENGTH_SHORT).show()
    }

    private fun handleCopyLink() {
        // Copy post link to clipboard
        val clipboard = this.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Post Link", "https://example.com/post/123")
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun handleSavePost() {
        // Save/unsave post
        Toast.makeText(this, "Post saved", Toast.LENGTH_SHORT).show()
    }

    private fun handleNotInterested() {
        // Mark as not interested
        Toast.makeText(this, "We'll show you fewer posts like this", Toast.LENGTH_SHORT).show()
    }


    val filePickerLauncher =
        registerForActivityResult(
            ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            // Handle selected files URIs here
            for (uri in uris) {
                // Process each selected file URI
            }
        }

    val permissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {

            } else {
                Toast.makeText(
                    this,
                    "Permission denied. Cannot select files.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


    fun openFilePicker(mimeType: String) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mimeType
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(intent, 120)
    }

    private fun copyFileToAppDirectory(uri: Uri): String? {
        return try {
            val fileName = getFileNameFromUri(uri) ?: "document_${System.currentTimeMillis()}"
            val destinationFile = File(filesDir, fileName)

            contentResolver.openInputStream(uri)?.use { inputStream ->
                destinationFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            destinationFile.absolutePath
        } catch (e: Exception) {
            Log.e("DocumentPicker", "Error copying file", e)
            null
        }
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        return try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        cursor.getString(displayNameIndex)
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("DocumentPicker", "Error getting file name", e)
            null
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_TOPICS_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                // Handle the result when TopicsActivity returns RESULT_OK
                // You can use data to retrieve any additional information passed back
                // For example, val resultValue = data?.getStringExtra("keyName")
                val selectedSubtopics = data?.getStringArrayListExtra("selectedSubtopics")
//                Log.d("selectedSubtopics", selectedSubtopics.toString())
//                binding.editTextText.setText(selectedSubtopics.toString())

                val formattedSubtopics = selectedSubtopics?.joinToString(" ") { "#$it" }
                // Get the current text from the EditText
                val currentText = binding.editTextText.text?.toString() ?: ""


                // Set the formatted subtopics to the EditText
//                binding.editTextText.setText(formattedSubtopics)
                val updatedText = if (currentText.isEmpty()) {
                    formattedSubtopics ?: ""
                } else {
                    "$currentText \n\n$formattedSubtopics"
                }

                // Set the updated text to the EditText
                binding.editTextText.setText(updatedText)

            } else {
                // Handle other result codes if needed
            }
        }
    }

    private fun uploadShorts(videoUri: Uri, caption: String) {
        uris.add(videoUri)

        // Get the SeekBar and Cancel Button from the current ViewHolder in the adapter
        val uploadTopSeekBar = shortsAdapter?.getCurrentViewHolderUploadSeekBar()
        val uploadCancelButton = shortsAdapter?.getCurrentViewHolderUploadCancelButton()

        uploadTopSeekBar?.let { seekBar ->
            seekBar.visibility = View.VISIBLE
            seekBar.progress = 0
            Log.d("UploadShorts", "SeekBar found and initialized")
        } ?: run {
            Log.e("UploadShorts", "SeekBar not found - adapter or current ViewHolder may be null")
        }

        uploadCancelButton?.let { cancelButton ->
            cancelButton.visibility = View.VISIBLE
            cancelButton.setOnClickListener {
                cancelUpload()
            }
            Log.d("UploadShorts", "Cancel button found and initialized")
        } ?: run {
            Log.e("UploadShorts", "Cancel button not found - adapter or current ViewHolder may be null")
        }

        // 1. compress shorts
        compressShorts()
        initializeShortsAdapter()
    }

    private fun cancelUpload() {
        // Cancel the upload work request
        uploadWorkRequest?.let { workRequest ->
            WorkManager.getInstance(applicationContext).cancelWorkById(workRequest.id)
            Log.d("UploadShorts", "Upload work cancelled")
        }

        // Hide progress bar and cancel button through adapter
        shortsAdapter?.hideCurrentViewHolderUploadProgress()

        Toast.makeText(this, "Upload cancelled", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("SetTextI18n")
    private fun compressShorts() {
        val uniqueId = UniqueIdGenerator.generateUniqueId()
        Log.d("progress id", uniqueId)

        val uri = uris[0]
        val uriFileSize = getFileSizeFromUri(this, uri)

        val fileSizeInKB = uriFileSize?.div(1024)
        val fileSizeInMB = fileSizeInKB?.div(1024)
        val fileSizeInGb = fileSizeInMB?.div(1024)

        Log.d("uriFileSize", "uri.scheme ${uri.scheme} compressShorts:uriFileSize: $uriFileSize  fileSizeInKB $fileSizeInKB fileSizeInMB $fileSizeInMB fileSizeInGb $fileSizeInGb")

        val filePath = when (uri.scheme) {
            "content" -> getRealPathFromUri(this, uri) ?: getFilePathFromContentUri(this, uri)
            "file" -> getFilePathFromUri(uri)
            else -> null
        }

        Log.d("uriFileSize", "compressShorts: file path $filePath ")

        if (fileSizeInMB != null) {
            Log.d("uriFileSize", "compressShorts: file size in mb $fileSizeInMB")
            if (fileSizeInGb != null) {
                Log.d("uriFileSize", "compressShorts: file size in mb $fileSizeInGb")
                if(fileSizeInGb > 1) {
                    Toast.makeText(this, "File too large", Toast.LENGTH_LONG).show()
                    shortsAdapter?.hideCurrentViewHolderUploadProgress()
                } else if(fileSizeInMB <= 10) {
                    Log.d("uriFileSize", "compressShorts: less than 10mb ")
                    // No compression needed, go directly to upload
                    shortsAdapter?.updateCurrentViewHolderUploadProgress(50) // Show 50% for file preparation

                    val thumbnailFile = saveBitmapToFile(thumbnail, applicationContext)
                    val thumbnailFilePath = thumbnailFile.absolutePath

                    val fileId:String = generateRandomId()
                    val feedShortsBusinessId:String = generateRandomId()
                    val durationString = filePath?.let { getFormattedDuration(it) }
                    val fileName = filePath?.let { getFileNameFromLocalPath(it) }
                    val mixedFeedUploadDataClass: MutableList<MixedFeedUploadDataClass> =
                        mutableListOf()
                    mixedFeedUploadDataClass.add(
                        MixedFeedUploadDataClass(
                            videos = FeedMultipleVideos(
                                videoPath = filePath!!,
                                videoDuration = durationString ?: "00:00",
                                fileName = fileName!!,
                                videoUri = uris[0].toString(),
                                thumbnail = thumbnail
                            ), fileTypes = "video", fileId = fileId
                        )
                    )
                    val words = caption.split("\\s+".toRegex())

                    val topics = mutableListOf<String>()
                    val nonTags = mutableListOf<String>()

                    for (word in words) {
                        if (word.startsWith("#")) {
                            topics.add(word.substring(1))
                        } else {
                            nonTags.add(word)
                        }
                    }
                    val tagS = mutableListOf<String>()
                    val content = nonTags.joinToString(" ")
                    val tagsString = topics.joinToString(", ")

                    val tags = if (tagsString.isNotEmpty()) {
                        tagsString.split(",").map { it.trim() }
                    } else {
                        tagS
                    }

                    uploadMixedFeed(mixedFeedUploadDataClass, content, tags.toMutableList(),feedShortsBusinessId)

                    uploadWorkRequest =
                        OneTimeWorkRequestBuilder<ShortsUploadWorker>()
                            .setInputData(
                                Data.Builder()
                                    .putString(ShortsUploadWorker.EXTRA_FILE_PATH, filePath)
                                    .putString(ShortsUploadWorker.CAPTION, caption)
                                    .putString(ShortsUploadWorker.FILE_ID, fileId)
                                    .putString(ShortsUploadWorker.FEED_SHORTS_BUSINESS_ID, feedShortsBusinessId)
                                    .putString(ShortsUploadWorker.THUMBNAIL, thumbnailFilePath)
                                    .build()
                            )
                            .build()

                    var workManager = WorkManager.getInstance(applicationContext)
                    Log.d("Upload", "Enqueuing upload work request...")
                    workManager.enqueue(uploadWorkRequest!!)

                    lifecycleScope.launch(Dispatchers.Main) {
                        Log.d("Progress", "Progress ...scope")
                        workManager = WorkManager.getInstance(applicationContext)
                        workManager.getWorkInfoByIdLiveData(uploadWorkRequest!!.id)
                            .observe(this@UploadShortsActivity) { workInfo ->
                                Log.d("Progress", "Observer triggered!")
                                if (workInfo != null) {
                                    val progress = workInfo.progress.getInt(ShortsUploadWorker.Progress, 0)
                                    // Update the seekbar with upload progress (50% + half of upload progress)
                                    val totalProgress = 50 + (progress / 2)

                                    // Update progress through adapter
                                    shortsAdapter?.updateCurrentViewHolderUploadProgress(totalProgress)

                                    Log.d("Progress", "Progress $progress, Total: $totalProgress")
                                } else {
                                    Log.d("Progress", "Work info is null")
                                }

                                when (workInfo.state) {
                                    WorkInfo.State.RUNNING -> {
                                        Log.d("Progress", "Running")
                                    }
                                    WorkInfo.State.SUCCEEDED -> {
                                        Log.d("Progress", "SUCCEEDED")
                                        shortsAdapter?.updateCurrentViewHolderUploadProgress(100)
                                        // Hide progress bar and cancel button after a delay
                                        Handler(Looper.getMainLooper()).postDelayed({
                                            shortsAdapter?.hideCurrentViewHolderUploadProgress()
                                        }, 1000)
                                    }
                                    WorkInfo.State.ENQUEUED -> {
                                        Log.d("Progress", "ENQUEUED")
                                    }
                                    WorkInfo.State.BLOCKED -> {
                                        Log.d("Progress", "BLOCKED")
                                    }
                                    WorkInfo.State.CANCELLED -> {
                                        Log.d("Progress", "CANCELLED")
                                        shortsAdapter?.hideCurrentViewHolderUploadProgress()
                                    }
                                    WorkInfo.State.FAILED -> {
                                        Log.d("Progress", "FAILED")
                                        shortsAdapter?.hideCurrentViewHolderUploadProgress()
                                    }
                                }
                            }
                    }
                }
                else {
                    Log.d("uriFileSize", "compressShorts: greater than 10mb ")
                    lifecycleScope.launch {
                        VideoCompressor.start(
                            context = applicationContext,
                            uris,
                            isStreamable = true,
                            sharedStorageConfiguration = SharedStorageConfiguration(
                                saveAt = SaveLocation.movies,
                                subFolderName = "flash_shorts"
                            ),
                            configureWith = Configuration(
                                quality = VideoQuality.MEDIUM,
                                videoNames = uris.map { uri -> uri.pathSegments.last() },
                                isMinBitrateCheckEnabled = false,
                            ),
                            listener = object : CompressionListener {
                                override fun onProgress(index: Int, percent: Float) {
                                    // Update seekbar with compression progress (0-50%)
                                    if (percent <= 100) {
                                        val compressionProgress = (percent / 2).toInt() // 0-50%
                                        runOnUiThread {
                                            shortsAdapter?.updateCurrentViewHolderUploadProgress(compressionProgress)
                                        }
                                        Log.d("Compress", "Compression Progress: $percent%, SeekBar: $compressionProgress%")
                                    }
                                }

                                override fun onStart(index: Int) {
                                    Log.d("Compress", "short compress started")
                                    runOnUiThread {
                                        shortsAdapter?.updateCurrentViewHolderUploadProgress(0)
                                    }
                                }

                                override fun onSuccess(index: Int, size: Long, path: String?) {
                                    Log.d("Compress", "short compress successful")
                                    Log.d("Compress", "short file size: ${getFileSize(size)}")
                                    Log.d("Compress", "short path: $path")

                                    // Update seekbar to 50% after compression completes
                                    runOnUiThread {
                                        shortsAdapter?.updateCurrentViewHolderUploadProgress(50)
                                    }

                                    val thumbnailFile = saveBitmapToFile(thumbnail, applicationContext)
                                    val thumbnailFilePath = thumbnailFile.absolutePath

                                    val fileId:String = generateRandomId()
                                    val feedShortsBusinessId:String = generateRandomId()
                                    val durationString = path?.let { getFormattedDuration(it) }
                                    val fileName = path?.let { getFileNameFromLocalPath(it) }
                                    val mixedFeedUploadDataClass: MutableList<MixedFeedUploadDataClass> =
                                        mutableListOf()
                                    mixedFeedUploadDataClass.add(
                                        MixedFeedUploadDataClass(
                                            videos = FeedMultipleVideos(
                                                videoPath = path!!,
                                                videoDuration = durationString ?: "00:00",
                                                fileName = fileName!!,
                                                videoUri = uris[0].toString(),
                                                thumbnail = thumbnail
                                            ), fileTypes = "video", fileId = fileId
                                        )
                                    )
                                    val words = caption.split("\\s+".toRegex())

                                    val topics = mutableListOf<String>()
                                    val nonTags = mutableListOf<String>()

                                    for (word in words) {
                                        if (word.startsWith("#")) {
                                            topics.add(word.substring(1))
                                        } else {
                                            nonTags.add(word)
                                        }
                                    }
                                    val tagS = mutableListOf<String>()
                                    val content = nonTags.joinToString(" ")
                                    val tagsString = topics.joinToString(", ")

                                    val tags = if (tagsString.isNotEmpty()) {
                                        tagsString.split(",").map { it.trim() }
                                    } else {
                                        tagS
                                    }

                                    uploadMixedFeed(mixedFeedUploadDataClass, content, tags.toMutableList(),feedShortsBusinessId)

                                    uploadWorkRequest =
                                        OneTimeWorkRequestBuilder<ShortsUploadWorker>()
                                            .setInputData(
                                                Data.Builder()
                                                    .putString(ShortsUploadWorker.EXTRA_FILE_PATH, path)
                                                    .putString(ShortsUploadWorker.CAPTION, caption)
                                                    .putString(ShortsUploadWorker.FILE_ID, fileId)
                                                    .putString(ShortsUploadWorker.FEED_SHORTS_BUSINESS_ID, feedShortsBusinessId)
                                                    .putString(ShortsUploadWorker.THUMBNAIL, thumbnailFilePath)
                                                    .build()
                                            )
                                            .build()

                                    var workManager = WorkManager.getInstance(applicationContext)
                                    Log.d("Upload", "Enqueuing upload work request...")
                                    workManager.enqueue(uploadWorkRequest!!)

                                    lifecycleScope.launch(Dispatchers.Main) {
                                        Log.d("Progress", "Progress ...scope")
                                        workManager = WorkManager.getInstance(applicationContext)
                                        workManager.getWorkInfoByIdLiveData(uploadWorkRequest!!.id)
                                            .observe(this@UploadShortsActivity) { workInfo ->
                                                Log.d("Progress", "Observer triggered!")
                                                if (workInfo != null) {
                                                    val progress = workInfo.progress.getInt(ShortsUploadWorker.Progress, 0)
                                                    // Update the seekbar with upload progress (50% + half of upload progress)
                                                    val totalProgress = 50 + (progress / 2)

                                                    // Update progress through adapter
                                                    shortsAdapter?.updateCurrentViewHolderUploadProgress(totalProgress)

                                                    Log.d("Progress", "Upload Progress $progress%, Total: $totalProgress%")
                                                } else {
                                                    Log.d("Progress", "Work info is null")
                                                }

                                                when (workInfo.state) {
                                                    WorkInfo.State.RUNNING -> {
                                                        Log.d("Progress", "Running")
                                                    }
                                                    WorkInfo.State.SUCCEEDED -> {
                                                        Log.d("Progress", "SUCCEEDED")
                                                        shortsAdapter?.updateCurrentViewHolderUploadProgress(100)
                                                        // Hide progress bar and cancel button after a delay
                                                        Handler(Looper.getMainLooper()).postDelayed({
                                                            shortsAdapter?.hideCurrentViewHolderUploadProgress()
                                                        }, 1000)
                                                    }
                                                    WorkInfo.State.ENQUEUED -> {
                                                        Log.d("Progress", "ENQUEUED")
                                                    }
                                                    WorkInfo.State.BLOCKED -> {
                                                        Log.d("Progress", "BLOCKED")
                                                    }
                                                    WorkInfo.State.CANCELLED -> {
                                                        Log.d("Progress", "CANCELLED")
                                                        shortsAdapter?.hideCurrentViewHolderUploadProgress()
                                                    }
                                                    WorkInfo.State.FAILED -> {
                                                        Log.d("Progress", "FAILED")
                                                        shortsAdapter?.hideCurrentViewHolderUploadProgress()
                                                    }
                                                }
                                            }
                                    }
                                }

                                override fun onFailure(index: Int, failureMessage: String) {
                                    Log.wtf("failureMessage", failureMessage)
                                    runOnUiThread {
                                        shortsAdapter?.hideCurrentViewHolderUploadProgress()
                                    }
                                }

                                override fun onCancelled(index: Int) {
                                    Log.wtf("TAG", "compression has been cancelled")
                                    runOnUiThread {
                                        shortsAdapter?.hideCurrentViewHolderUploadProgress()
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }
    }

    private fun showTopicsDialog() {
        val topicsList = arrayOf(
            "Dance", "Comedy", "Music", "Sports", "Fashion", "Food", "Travel",
            "Gaming", "Education", "DIY", "Beauty", "Fitness", "Pets", "Art",
            "Technology", "News", "Lifestyle", "Entertainment"
        )

        val selectedItems = BooleanArray(topicsList.size)

        AlertDialog.Builder(this)
            .setTitle("Select Topics")
            .setMultiChoiceItems(topicsList, selectedItems) { _, which, isChecked ->
                if (isChecked) {
                    selectedTopics.add(topicsList[which])
                } else {
                    selectedTopics.remove(topicsList[which])
                }
            }
            .setPositiveButton("Done") { dialog, _ ->
                updateTopicsUI()
                applyTopicsFilter()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showTagPeopleDialog() {
        val peopleList = arrayOf(
            "Friends", "Family", "Colleagues", "Followers", "Mutual Friends",
            "Close Friends", "Acquaintances", "Celebrities", "Influencers"
        )

        val selectedItems = BooleanArray(peopleList.size)

        // Pre-select already selected items
        peopleList.forEachIndexed { index, person ->
            selectedItems[index] = selectedPeople.contains(person)
        }

        AlertDialog.Builder(this)
            .setTitle("Tag People")
            .setMultiChoiceItems(peopleList, selectedItems) { _, which, isChecked ->
                if (isChecked) {
                    selectedPeople.add(peopleList[which])
                } else {
                    selectedPeople.remove(peopleList[which])
                }
            }
            .setPositiveButton("Done") { dialog, _ ->
                updateTagPeopleUI()
                applyPeopleFilter()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showLocationPicker() {
        val locations = arrayOf(
            "Current Location", "Nearby", "City Center", "Popular Places",
            "Restaurants", "Parks", "Shopping Centers", "Entertainment Venues",
            "Schools", "Gyms", "Cafes", "Custom Location"
        )

        AlertDialog.Builder(this)
            .setTitle("Add Location")
            .setItems(locations) { dialog, which ->
                when (which) {
                    0 -> getCurrentLocation()
                    1 -> getNearbyLocations()
                    locations.size - 1 -> showCustomLocationDialog()
                    else -> {
                        selectedLocation = locations[which]
                        updateLocationUI()
                        applyLocationFilter()
                    }
                }
                dialog.dismiss()
            }
            .show()
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        // Check location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        try {
            val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

            // Check if GPS is enabled
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(this, "Please enable GPS", Toast.LENGTH_SHORT).show()
                return
            }

            val locationListener = object : LocationListener {
                @SuppressLint("DefaultLocale")
                override fun onLocationChanged(location: Location) {
                    selectedLocation = "Current Location (" +
                            "${String.format("%.4f", location.latitude)}, " +
                            "${String.format("%.4f", location.longitude)})"
                    updateLocationUI()
                    applyLocationFilter()
                    locationManager.removeUpdates(this)
                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {
                    Toast.makeText(this@UploadShortsActivity, "GPS disabled", Toast.LENGTH_SHORT).show()
                }
            }

            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                0f,
                locationListener)


            // Add timeout to prevent indefinite waiting
            Handler(Looper.getMainLooper()).postDelayed({
                locationManager.removeUpdates(locationListener)
            }, 10000) // 10 seconds timeout

        } catch (e: Exception) {
            Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show()
            Log.e("LocationError", "Error getting location", e)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation()
                } else {
                    Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showCustomLocationDialog() {
        val input = EditText(this)
        input.hint = "Enter location name"

        AlertDialog.Builder(this)
            .setTitle("Custom Location")
            .setView(input)
            .setPositiveButton("Add") { dialog, _ ->
                val customLocation = input.text.toString().trim()
                if (customLocation.isNotEmpty()) {
                    selectedLocation = customLocation
                    updateLocationUI()
                    applyLocationFilter()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun getNearbyLocations() {
        // Simulate getting nearby locations
        val nearbyPlaces = arrayOf(
            "Coffee Shop - 0.2 km", "Restaurant - 0.5 km", "Park - 0.8 km",
            "Mall - 1.2 km", "Gym - 1.5 km", "Cinema - 2.0 km"
        )

        AlertDialog.Builder(this)
            .setTitle("Nearby Locations")
            .setItems(nearbyPlaces) { dialog, which ->
                selectedLocation = nearbyPlaces[which]
                updateLocationUI()
                applyLocationFilter()
                dialog.dismiss()
            }
            .show()
    }

    @SuppressLint("SetTextI18n")
    private fun updateTagPeopleUI() {
        val textView = tagPeopleLayout.findViewById<TextView>(R.id.tagPeopleText)
        if (selectedPeople.isNotEmpty()) {
            textView.text = "Tagged (${selectedPeople.size})"
            textView.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
        } else {
            textView.text = "Tag people"
            textView.setTextColor(ContextCompat.getColor(this, android.R.color.black))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateTopicsUI() {
        val textView = topicsLayout.findViewById<TextView>(R.id.topicsText)
        if (selectedTopics.isNotEmpty()) {
            textView.text = "Topics (${selectedTopics.size})"
            textView.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
        } else {
            textView.text = "Add topics"
            textView.setTextColor(ContextCompat.getColor(this, android.R.color.black))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateLocationUI() {
        val textView = locationLayout.findViewById<TextView>(R.id.locationText)
        if (selectedLocation != null) {
            textView.text = "Location ✓"
            textView.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
        } else {
            textView.text = "Add location"
            textView.setTextColor(ContextCompat.getColor(this, android.R.color.black))
        }
    }

    private fun applyPeopleFilter() {

        val filteredPosts = filterPostsByPeople(selectedPeople as List<String>)
        updateFeedWithFilteredPosts(filteredPosts)

        // Analytics tracking
        trackFilterUsage("people", selectedPeople.size)
    }

    private fun applyTopicsFilter() {
        // Apply topics filter to feed
        val filteredPosts = filterPostsByTopics(selectedTopics as List<String>)
        updateFeedWithFilteredPosts(filteredPosts)

        // Show topics chips
        showTopicsChips()

        // Analytics tracking
        trackFilterUsage("topics", selectedTopics.size)
    }

    private fun applyLocationFilter() {
        // Apply location filter to feed
        selectedLocation?.let { location ->
            val filteredPosts = filterPostsByLocation(location)
            updateFeedWithFilteredPosts(filteredPosts)

            // Show location badge
            showLocationBadge(location)

            // Analytics tracking
            trackFilterUsage("location", 1)
        }
    }

    private fun filterPostsByPeople(people: List<String>): List<Post> {
        // Filter posts based on selected people categories
        return allPosts.filter { post ->
            people.any { person ->
                when (person) {
                    "Friends" -> post.isFromFriend
                    "Family" -> post.isFromFamily
                    "Followers" -> post.isFromFollower
                    "Mutual Friends" -> post.isFromMutualFriend
                    "Close Friends" -> post.isFromCloseFriend
                    "Celebrities" -> post.isFromCelebrity
                    "Influencers" -> post.isFromInfluencer
                    else -> true
                }
            }
        }
    }

    private fun filterPostsByTopics(topics: List<String>): List<Post> {
        // Filter posts based on selected topics
        return allPosts.filter { post ->
            topics.any { topic ->
                post.tags.contains(topic.lowercase()) ||
                        post.description.contains(topic, ignoreCase = true)
            }
        }
    }

    private fun filterPostsByLocation(location: String): List<Post> {
        // Filter posts based on location
        return allPosts.filter { post ->
            post.location?.contains(location, ignoreCase = true) == true ||
                    isNearLocation(post.latitude, post.longitude, location)
        }
    }

    private fun isNearLocation(lat: Double?, lon: Double?, targetLocation: String): Boolean {
        // Implement proximity check logic
        if (lat == null || lon == null) return false

        // Calculate distance and return true if within certain radius
        // This would typically use Google Maps or similar service
        return true // Simplified for example
    }

    private fun updateFeedWithFilteredPosts(posts: List<Post>) {
        // Update RecyclerView with filtered posts
//        feedAdapter.updatePosts(posts)

        // Show filter indicator
        showActiveFiltersIndicator()

        // Smooth scroll to top
        feedRecyclerView.smoothScrollToPosition(0)
    }

    private fun showTopicsChips() {
        // Show selected topics as chips at top of feed
        val chipsContainer = findViewById<LinearLayout>(R.id.topicsChipsContainer)
        chipsContainer.removeAllViews()

        selectedTopics.forEach { topic ->
            val chip = createTopicChip(topic)
            chipsContainer.addView(chip)
        }

        chipsContainer.visibility = if (selectedTopics.isNotEmpty()) View.VISIBLE else View.GONE
    }

    @SuppressLint("InflateParams")
    private fun createTopicChip(topic: String): View {
        val chip = layoutInflater.inflate(R.layout.topic_chip, null)
        val textView = chip.findViewById<TextView>(R.id.chipText)
        val closeButton = chip.findViewById<ImageView>(R.id.chipClose)

        textView.text = topic
        closeButton.setOnClickListener {
            selectedTopics.remove(topic)
            showTopicsChips()
            applyTopicsFilter()
        }

        return chip
    }

    @SuppressLint("SetTextI18n")
    private fun showLocationBadge(location: String) {
        val locationBadge = findViewById<TextView>(R.id.locationBadge)
        locationBadge.text = "📍 $location"
        locationBadge.visibility = View.VISIBLE

        locationBadge.setOnClickListener {
            selectedLocation = null
            updateLocationUI()
            locationBadge.visibility = View.GONE
            applyLocationFilter()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showActiveFiltersIndicator() {
        val filtersCount = selectedPeople.size + selectedTopics.size +
                (if (selectedLocation != null) 1 else 0)

        val filterIndicator = findViewById<TextView>(R.id.filterIndicator)
        if (filtersCount > 0) {
            filterIndicator.text = "Active Filters: $filtersCount"
            filterIndicator.visibility = View.VISIBLE
        } else {
            filterIndicator.visibility = View.GONE
        }
    }

    private fun trackFilterUsage(filterType: String, count: Int) {
        // Analytics tracking
        val params = Bundle().apply {
            putString("filter_type", filterType)
            putInt("filter_count", count)
        }
        // FirebaseAnalytics.getInstance(this).logEvent("filter_applied", params)
    }

    private fun initializeShortsAdapter() {
        // Make sure your shortsAdapter is properly initialized before calling upload methods
        if (shortsAdapter == null) {
            // Initialize your adapter here with proper parameters
            // shortsAdapter = ShortsAdapter(...)
            Log.e("UploadShorts", "ShortsAdapter is not initialized")
        }
    }

    private fun createGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(Uri::class.java, UriTypeAdapter())
            .create()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun uploadMixedFeed(
        mixedFiles: List<MixedFeedUploadDataClass>,
        content: String,
        tags: MutableList<String>,
        feedShortsBusinessId: String
    ) {


        var uploadWorkRequest: OneTimeWorkRequest? = null
        val gson = createGson()
        val tag = "uploadMixedFeed"
        val uploadDataJson = gson.toJson(mixedFiles)
        Log.d(tag, "all feed size: ${getFeedViewModel.getAllFeedData().size}")

        val inputData = Data.Builder()
            .putString("upload_data", uploadDataJson)
            .putString(FeedUploadWorker.CAPTION, content)
            .putString(FeedUploadWorker.FEED_SHORTS_BUSINESS_ID, feedShortsBusinessId)
            .putString(FeedUploadWorker.CONTENT_TYPE, "mixed_files")
            .putStringArray(FeedUploadWorker.TAGS, tags.toTypedArray())
            .build()

        try {
            GlobalScope.launch(Dispatchers.IO) {

                uploadWorkRequest = OneTimeWorkRequestBuilder<FeedUploadWorker>()
                    .setInputData(inputData)
                    .build()


                val workManager = WorkManager.getInstance(applicationContext)


                workManager.enqueue(uploadWorkRequest)

            }
        } catch (e: Exception) {
            Log.e(tag, "uploadVideoFeed: error because ${e.message}")
            e.printStackTrace()
        }
    }


}




