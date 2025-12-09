package com.uyscuti.social.circuit.User_Interface.fragments.feed

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.PorterDuff
import android.graphics.pdf.PdfRenderer
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.MediaStore.Audio
import android.provider.OpenableColumns
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setPadding
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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.uyscuti.social.circuit.adapter.feed.MultipleImagesListener
import com.uyscuti.social.circuit.adapter.feed.multiple_files.DocumentListenerInterface
import com.uyscuti.social.circuit.adapter.feed.multiple_files.FeedVideoThumbnailAdapter
import com.uyscuti.social.circuit.adapter.feed.multiple_files.MixedFilesUploadAdapter
import com.uyscuti.social.circuit.adapter.feed.multiple_files.MultipleAudiosListener
import com.uyscuti.social.circuit.adapter.feed.multiple_files.MultipleFeedAudioAdapter
import com.uyscuti.social.circuit.adapter.feed.multiple_files.MultipleFeedDocAdapter
import com.uyscuti.social.circuit.adapter.feed.multiple_files.MultipleFeedFilesPagerAdapter
import com.uyscuti.social.circuit.adapter.feed.multiple_files.MultipleSelectedFeedVideoAdapter
import com.uyscuti.social.circuit.adapter.feed.multiple_files.MultipleVideosListener
import com.uyscuti.social.circuit.adapter.feed.multiple_files.UriTypeAdapter
import com.uyscuti.social.circuit.eventbus.FeedUploadResponseEvent
import com.uyscuti.social.circuit.model.ProgressEvent
import com.uyscuti.social.circuit.model.feed.FeedMultipleImages
import com.uyscuti.social.circuit.model.feed.multiple_files.FeedMultipleAudios
import com.uyscuti.social.circuit.model.feed.multiple_files.FeedMultipleDocumentsDataClass
import com.uyscuti.social.circuit.model.feed.multiple_files.FeedMultipleVideos
import com.uyscuti.social.circuit.model.feed.multiple_files.MixedFeedUploadDataClass
import com.uyscuti.social.circuit.model.feed.multiple_files.MultipleAudios
import com.uyscuti.social.circuit.User_Interface.feed.FeedUploadWorker
import com.uyscuti.social.circuit.User_Interface.shorts.ShortsUploadWorker
import com.uyscuti.social.circuit.User_Interface.shorts.UniqueIdGenerator
import com.uyscuti.social.circuit.User_Interface.shorts.VideoUtils
import com.uyscuti.social.circuit.User_Interface.shorts.getFileSize
import com.uyscuti.social.circuit.User_Interface.uploads.CameraActivity
import com.uyscuti.social.circuit.User_Interface.uploads.feed_uploads.FeedAudioActivity
import com.uyscuti.social.circuit.User_Interface.uploads.feed_uploads.FeedSelectVideoActivity
import com.uyscuti.social.circuit.utils.AudioDurationHelper.reverseFormattedDuration
import com.uyscuti.social.circuit.utils.PathUtil
import com.uyscuti.social.circuit.utils.audio_compressor.AudioCompressorWithProgress
import com.uyscuti.social.circuit.utils.feedutils.ThumbnailUtil
import com.uyscuti.social.circuit.utils.feedutils.feedRemoveTextStartingWithHash
import com.uyscuti.social.circuit.utils.fileType
import com.uyscuti.social.circuit.utils.generateRandomFileName
import com.uyscuti.social.circuit.utils.generateRandomId
import com.uyscuti.social.circuit.utils.getFileNameFromLocalPath
import com.uyscuti.social.circuit.utils.isFileSizeGreaterThan2MB
import com.uyscuti.social.circuit.utils.listToCommaSeparatedString
import com.uyscuti.social.circuit.utils.uriToFile2
import com.uyscuti.social.circuit.viewmodels.feed.FeedUploadViewModel
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.adapter.feed.FeedAdapter
import com.uyscuti.social.circuit.adapter.feed.multiple_files.getNumberOfPagesFromUriForDoc
import com.uyscuti.social.circuit.adapter.feed.multiple_files.getNumberOfPagesFromUriForDocx
import com.uyscuti.social.circuit.adapter.feed.multiple_files.getNumberOfPagesFromUriForPDF
import com.uyscuti.social.circuit.adapter.feed.multiple_files.retrieveFirstPageAndSaveAsImage
import com.uyscuti.social.circuit.adapter.feed.multiple_files.retrieveFirstPageAsBitmap
import com.uyscuti.social.circuit.databinding.ActivityUploadFeeedBinding
import com.uyscuti.social.circuit.User_Interface.shorts.UploadShortsActivity.Companion.LOCATION_PERMISSION_REQUEST_CODE
import com.uyscuti.social.compressor.CompressionListener
import com.uyscuti.social.compressor.VideoCompressor
import com.uyscuti.social.compressor.VideoQuality
import com.uyscuti.social.compressor.config.Configuration
import com.uyscuti.social.compressor.config.SaveLocation
import com.uyscuti.social.compressor.config.SharedStorageConfiguration
import com.uyscuti.social.network.api.response.posts.Post
import dagger.hilt.android.AndroidEntryPoint
import id.zelory.compressor.Compressor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import me.relex.circleindicator.CircleIndicator3
import org.apache.poi.hwpf.HWPFDocument
import org.apache.poi.hwpf.usermodel.Range
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import android.media.MediaPlayer
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.uyscuti.social.circuit.User_Interface.shorts.UploadShortsActivity
import kotlin.text.isNotEmpty
import androidx.core.widget.NestedScrollView
import kotlinx.coroutines.Job
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import com.uyscuti.social.circuit.feed_demo.AnyFileFullScreenActivity
//import com.uyscuti.social.network.api.response.posts.Post


private val TAG = "UploadFeedActivity"

interface AudioUploadListener {
    fun onAudioSelected(audio: Audio)
    fun onAudioRemoved(position: Int)
    // Add any other methods the adapter needs to call
}

@AndroidEntryPoint
class UploadFeedActivity : AppCompatActivity(), FeedVideoThumbnailAdapter.ThumbnailClickListener,
    AudioCompressorWithProgress.ProgressListener, MultipleImagesListener, MultipleAudiosListener,
    DocumentListenerInterface,
    MultipleVideosListener,
    AudioUploadListener{

    override fun onAudioSelected(audio: Audio) {
        // Handle audio selection
    }

    override fun onAudioRemoved(position: Int) {
        // Handle audio removal
    }

    private var videoUri: Uri? = null
    private lateinit var binding: ActivityUploadFeeedBinding
    private lateinit var attachmentFile: CardView
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var audioPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var videoPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var imagePickLauncher: ActivityResultLauncher<Intent>
    private lateinit var documentPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var feedUploadViewModel: FeedUploadViewModel

    // Full screen views
    private lateinit var fullScreenFileContainer: FrameLayout
    private lateinit var fullScreenFileView: VideoView  // This is correct - matches XML id
    private lateinit var fullScreenAnyFileView: ImageView

    private lateinit var closeFullScreenButton: ImageView
    private lateinit var fullScreenPlayPauseButton: ImageView
    private lateinit var bottomFeedVideoProgressSeekBar: SeekBar
    private lateinit var videoDurationText: TextView

    // Main content views
    private lateinit var forUploadingFileThumbNail: CardView
    private lateinit var uploadedFileThumbNail: ImageView
    private lateinit var videoView: VideoView
    private lateinit var viewPager: ViewPager2
    private lateinit var circleIndicator: CircleIndicator3
    private lateinit var playButton: ImageView
    private lateinit var pauseButton: ImageView

    // Other views
    private var toolbar: Toolbar? = null
    private lateinit var cancelButton: ImageView
    private lateinit var moreButton: ImageView
    private lateinit var titleTextView: TextView
    private lateinit var selectCoverText: TextView
    private lateinit var recyclerView2: RecyclerView
    private lateinit var interactionsBox: MaterialCardView
    private lateinit var saveChanges: TextView
    private lateinit var editTextText: EditText
    private lateinit var iconLayout: LinearLayout
    private lateinit var tagPeopleLayout: LinearLayout
    private lateinit var topicsLayout: LinearLayout
    private lateinit var locationLayout: LinearLayout
    private lateinit var addMoreFeedLayout: LinearLayout
    private lateinit var buttonsLayout: LinearLayout
    private lateinit var draftButton: Button
    private lateinit var postButton: Button
    private lateinit var viewModel: FeedUploadViewModel
    // Add these properties to your class
    private lateinit var blinkingIconView: ImageView
    private val blinkingIconHandler = Handler(Looper.getMainLooper())

    // Data variables
    val tags: MutableList<String> = mutableListOf()
    val audioPathList: MutableList<String> = mutableListOf()
    var text = ""
    var fileType: String = ""
    private var durationString = ""
    private var audioDurationStringList: MutableList<String> = mutableListOf()
    private var videoUris: MutableList<Uri> = mutableListOf()
    private var videoPaths: MutableList<String> = mutableListOf()
    private var isThumbnailClicked = false
    private var thumbnail: Bitmap? = null
    private var thumbnails: MutableList<Bitmap> = mutableListOf()
    private var imagesList = mutableListOf<String>()
    private var audiosList = mutableListOf<MultipleAudios>()
    private var videosList = mutableListOf<FeedMultipleVideos>()
    private var documentUriListToUpload: MutableList<String> = mutableListOf()
    private var documentFileNamesToUpload: MutableList<String> = mutableListOf()
    private var documentNumberOfPagesToUpload: MutableList<String> = mutableListOf()
    private var documentTypesToUpload: MutableList<String> = mutableListOf()
    private var documentThumbnailsToUpload: MutableList<String> = mutableListOf()
    private var documentsList: MutableList<FeedMultipleDocumentsDataClass> = mutableListOf()
    private var permissionGranted = false
    private var compressedImageFile: File? = null
    private var compressedImageFiles: MutableList<File> = mutableListOf()
    private var uploadWorkRequest: OneTimeWorkRequest? = null
    private var audioPath = ""
    private val toCompressUris = mutableListOf<Uri>()
    private var fileName = ""
    private var docType = ""
    private var docFilePath = ""
    private var numberOfPages = ""
    private var vnFilePath: String? = null
    private var vnDurationString: String? = null
    private var vnFileName: String? = null
    private var isMultipleImages = false
    private lateinit var multipleAudioAdapter: MultipleFeedAudioAdapter
    private lateinit var multipleDocsAdapter: MultipleFeedDocAdapter
    private lateinit var multipleSelectedFeedVideoAdapter: MultipleSelectedFeedVideoAdapter
    private lateinit var mixedFilesUploadAdapter: MixedFilesUploadAdapter
    private var addMoreFeedFiles = true
    private var multipleFeedFilesPagerAdapter: MultipleFeedFilesPagerAdapter? = null
    private val selectedPeople = mutableSetOf<String>()
    private val selectedTopics = mutableSetOf<String>()
    private var selectedLocation: String? = null
    private lateinit var feedRecyclerView: RecyclerView
    private lateinit var feedAdapter: FeedAdapter
    private lateinit var allPosts: List<Post>
    private lateinit var filterIndicator: TextView
    private lateinit var topicsChipsContainer: LinearLayout
    private lateinit var locationBadge: TextView
    private lateinit var tagPeopleText: TextView
    private lateinit var topicsText: TextView
    private lateinit var locationText: TextView
    private var shouldRestoreUIOnBack = false
    private val originalUIState = mutableMapOf<View, Int>()
    private var videoCurrentPosition = 0
    private var wasVideoPlaying = false
    private var isThumbnailSelected = false
    private val thumbnailCache: MutableMap<String, Bitmap> = mutableMapOf()
    private val thumbnailExtractionJobs: MutableMap<Int, Job> = mutableMapOf()
    private lateinit var fullScreenViewPager: ViewPager2
    private lateinit var fullScreenIndicator: CircleIndicator3

    private lateinit var clickableOverlay: View
    private lateinit var mainContentContainer: NestedScrollView

    // Media player state management
    private var mediaPlayer: MediaPlayer? = null
    private var isMediaPlayerReady = false
    private var currentMediaType: String? = null
    private var mediaCurrentPosition = 0
    private var wasMediaPlaying = false
    private var isInFullScreen = false

    private val documentUris = mutableListOf<Uri>()

    private lateinit var adapter: MultipleFeedFilesPagerAdapter

    private var mediaViewPagerAdapter: MediaViewPagerAdapter? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.READ_MEDIA_IMAGES
    )
    private val REQUEST_CODE = 2024

    private var currentItemPosition = 0

    private lateinit var actualFileClicked: View // Add this at the top of your activity


    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("SetTextI18n", "ClickableViewAccessibility", "UseKtx", "CutPasteId")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)


    fun Bitmap?.isNullOrEmpty(): Boolean {
        return this == null || this.isRecycled
    }

    private fun initializeRemoveFeature() {
        setupRemoveButton()
        setupViewPagerListener()
    }

    private fun setupRemoveButton() {
        val removeButton = findViewById<ImageView>(R.id.removeButton)
        removeButton.setOnClickListener {
            removeCurrentItem()
        }
    }

    private fun removeCurrentItem() {
        val mixedFeedFiles = feedUploadViewModel.getMixedFeedUploadDataClass()

        if (mixedFeedFiles.isNotEmpty() && currentItemPosition < mixedFeedFiles.size) {
            // Remove item from ViewModel
            feedUploadViewModel.removeMixedFeedUploadDataClass(currentItemPosition)

            // Update the adapter
            multipleFeedFilesPagerAdapter?.let { adapter ->
                val updatedList = feedUploadViewModel.getMixedFeedUploadDataClass()
                adapter.setMixedFeedUploadDataClass(updatedList)
                adapter.notifyDataSetChanged()
            }

            // Update UI based on remaining items
            if (feedUploadViewModel.getMixedFeedUploadDataClass().isEmpty()) {
                // Hide the entire thumbnail container if no items left
                binding.forUploadingFileThumbNail.visibility = View.GONE
                binding.selectCoverText.visibility = View.GONE
                binding.recyclerView2.visibility = View.GONE
            } else {
                // Adjust current position if needed
                val remainingSize = feedUploadViewModel.getMixedFeedUploadDataClass().size
                if (currentItemPosition >= remainingSize) {
                    currentItemPosition = remainingSize - 1
                }

                // Update ViewPager to show correct item
                binding.viewPager.setCurrentItem(currentItemPosition, false)

                // Update circle indicator
                binding.circleIndicator.setViewPager(binding.viewPager)
            }

            // Update media adapter if it exists
            mediaViewPagerAdapter?.let { adapter ->
                val updatedList = feedUploadViewModel.getMixedFeedUploadDataClass()
                adapter.updateMediaItems(updatedList)
            }

            // Update file type icon and play buttons
            setupPlayButton()
        }
    }

    private fun setupViewPagerListener() {
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentItemPosition = position
                setupPlayButton() // This will update UI for current item
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUploadFeeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playButton = binding.playButton
        pauseButton = binding.pauseButton

        initializeViews()
        setupClickListeners()
        setupPlayButton()
        setupUI()
        setupMediaAreaClickable()
        initializeActivity()
        initializeRemoveFeature()


        documentsList = mutableListOf()

        adapter = MultipleFeedFilesPagerAdapter(
            this,

            isFullScreen = true
        )
        viewModel = ViewModelProvider(this)[FeedUploadViewModel::class.java]

        setupAdapter()

        // Initialize only the essential views needed for the activity
        viewPager = findViewById(R.id.viewPager)

        feedUploadViewModel = ViewModelProvider(this)[FeedUploadViewModel::class.java]

        multipleFeedFilesPagerAdapter = MultipleFeedFilesPagerAdapter(
            this,
            isFullScreen = true
        )
        binding.viewPager.adapter = multipleFeedFilesPagerAdapter

        // Remove all full-screen related initializations and listeners
        // Let the Fragment handle its own full-screen functionality

        binding.tagPeopleLayout.setOnClickListener {
            showTagPeopleDialog()
        }

        binding.topicsLayout.setOnClickListener {
            showTopicsDialog()
        }

        binding.locationLayout.setOnClickListener {
            showLocationPicker()
        }

        binding.moreButton.setOnClickListener { view ->
            showOptionsMenu(view)
        }

        binding.actualFileClicked.setOnClickListener { view ->

        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right,
                systemBars.bottom)
            insets
        }

        vnFilePath = intent?.getStringExtra("vnFilePath").toString()
        vnDurationString = intent?.getStringExtra("vnDurationString").toString()
        vnFileName = intent?.getStringExtra("vnFileName").toString()

        mixedFilesUploadAdapter = MixedFilesUploadAdapter(
            this, this, this,
            multipleDocumentsListener = null
        )
        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        Log.d(TAG, "onCreate: vnFilePath - $vnFilePath duration $vnDurationString")

        if (vnFilePath != "null") {
            fileType = "mixed_files"
            setAddMoreFeedVisible()
            binding.uploadedFileThumbNail.setImageResource(R.drawable.baseline_headphones_24)
            val tintColor = getColor(R.color.black)
            binding.recyclerView2.visibility = View.INVISIBLE
            binding.uploadedFileThumbNail.setColorFilter(tintColor,
                PorterDuff.Mode.SRC_ATOP)
            feedUploadViewModel.setText(
                "File name: $vnFileName \nDuration: $vnDurationString")

            multipleFeedFilesPagerAdapter = MultipleFeedFilesPagerAdapter(
                this,

                isFullScreen = true
            )
            binding.viewPager.adapter = multipleFeedFilesPagerAdapter

            feedUploadViewModel.addMixedFeedUploadDataClass(
                MixedFeedUploadDataClass(
                    audios = FeedMultipleAudios(
                        duration = vnDurationString!!,
                        audioPath = vnFilePath!!,
                        filename = vnFileName!!
                    ), fileTypes = "audio"
                )
            )

            val mixedFeedFiles = feedUploadViewModel.getMixedFeedUploadDataClass()
            multipleFeedFilesPagerAdapter?.setMixedFeedUploadDataClass(mixedFeedFiles)

            val indicator = findViewById<CircleIndicator3>(R.id.circleIndicator)
            indicator.setViewPager(binding.viewPager)

            binding.recyclerView2.visibility = View.INVISIBLE
            binding.forUploadingFileThumbNail.visibility = View.VISIBLE
            binding.uploadedFileThumbNail.visibility = View.GONE
            binding.selectCoverText.visibility = View.VISIBLE
        } else {
            Log.d(TAG, "onCreate: vn path is null")
        }

        feedUploadViewModel.displayText.observe(this) { text ->
            Log.d(TAG, "onCreate: text $text")
            binding.selectCoverText.text = text
        }

        cancelShortsUpload()
        backFromShortsUpload()

        permissionGranted = ActivityCompat.checkSelfPermission(
            this, permissions[0]) == PackageManager.PERMISSION_GRANTED

        if (!permissionGranted) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
        }

        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                fileType = "mixed_files"
                val imagePath = result.data?.getStringExtra("image_url")
                Log.d(TAG, "Selected image path from camera: $imagePath")
                imagePath?.toUri()
                if (imagePath != null) {
                    setAddMoreFeedVisible()
                    val file = File(imagePath)
                    if (file.exists()) {
                        lifecycleScope.launch {
                            val compressedImageFile = Compressor.compress(
                                this@UploadFeedActivity, file)
                            Log.d(TAG,
                                "cameraLauncher: compressedImageFile absolutePath: " +
                                        "${compressedImageFile.absolutePath}")
                            val fileSizeInBytes = compressedImageFile.length()
                            val fileSizeInKB = fileSizeInBytes / 1024
                            val fileSizeInMB = fileSizeInKB / 1024
                            this@UploadFeedActivity.compressedImageFile = compressedImageFile
                            Log.d(TAG,
                                "cameraLauncher: compressedImageFile size " +
                                        "$fileSizeInKB KB, " +
                                        "$fileSizeInMB MB addMoreFeedFiles $addMoreFeedFiles")

                            feedUploadViewModel.addMixedFeedUploadDataClass(
                                MixedFeedUploadDataClass(
                                    images = FeedMultipleImages(
                                        imagePath = imagePath,
                                        compressedImagePath = compressedImageFile.absolutePath),
                                    fileTypes = "image"
                                )
                            )

                            if (addMoreFeedFiles) {
                                multipleFeedFilesPagerAdapter = MultipleFeedFilesPagerAdapter(
                                    this@UploadFeedActivity,
                                            isFullScreen = true
                                )
                                binding.viewPager.adapter = multipleFeedFilesPagerAdapter
                                val mixedFeedFiles =
                                    feedUploadViewModel.getMixedFeedUploadDataClass()
                                multipleFeedFilesPagerAdapter?.setMixedFeedUploadDataClass(
                                    mixedFeedFiles)
                                binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
                                binding.viewPager.currentItem = mixedFeedFiles.size - 1
                            } else {
                                Glide.with(this@UploadFeedActivity).load(
                                    compressedImageFile).into(
                                    binding.uploadedFileThumbNail)
                                updateFileUIVisibility()
                            }

                            feedUploadViewModel.setText("")
                            binding.recyclerView2.visibility = View.INVISIBLE
                            binding.uploadedFileThumbNail.colorFilter = null
                            binding.uploadedFileThumbNail.setPadding(0)
                        }
                    }
                }
            }
        }

        attachmentFile = findViewById(R.id.forUploadingFileThumbNail)

        imagePickLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                setAddMoreFeedVisible()
                val data = result.data
                if (data != null && data.data != null) {
                    val selectedImageUri: Uri = data.data!!
                    isThumbnailClicked = true
                    val bitmap = loadBitmapFromUri(this, selectedImageUri)
                    if (bitmap != null) {
                        binding.uploadedFileThumbNail.colorFilter = null
                        binding.uploadedFileThumbNail.setPadding(0)
                        Glide.with(this).load(bitmap).into(binding.uploadedFileThumbNail)
                        thumbnail = bitmap
                        playButton.visibility = View.GONE
                        pauseButton.visibility = View.GONE

                        lifecycleScope.launch {
                            val file = File(selectedImageUri.path ?: return@launch)
                            if (file.exists()) {
                                val compressedImageFile = Compressor.compress(this@UploadFeedActivity, file)
                                feedUploadViewModel.addMixedFeedUploadDataClass(
                                    MixedFeedUploadDataClass(
                                        images = FeedMultipleImages(
                                            imagePath = selectedImageUri.toString(),
                                            compressedImagePath = compressedImageFile.absolutePath
                                        ),
                                        fileTypes = "image"
                                    )
                                )
                                if (addMoreFeedFiles) {
                                    val mixedFeedFiles = feedUploadViewModel.getMixedFeedUploadDataClass()
                                    multipleFeedFilesPagerAdapter?.setMixedFeedUploadDataClass(mixedFeedFiles)
                                    binding.viewPager.adapter = multipleFeedFilesPagerAdapter
                                    binding.viewPager.currentItem = mixedFeedFiles.size - 1
                                }
                            }
                        }
                    } else {
                        updateFileUIVisibility()
                        Log.e(TAG, "Bitmap is null for URI: $selectedImageUri")
                    }
                }

                setupMediaAdapter()
            }
        }

        audioPickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                fileType = "mixed_files"
                setAddMoreFeedVisible()
                binding.selectCoverText.visibility = View.VISIBLE
                val data = result.data ?: return@registerForActivityResult
                val audioPath = data.getStringArrayListExtra("audio_url") ?: return@registerForActivityResult
                val feedMultipleAudios: MutableList<FeedMultipleAudios> = mutableListOf()

                if (!addMoreFeedFiles) {
                    audiosList.clear()
                    audioPathList.clear()
                    audioDurationStringList.clear()
                }

                for (audioFilePath in audioPath) {
                    val durationString = getFormattedDuration(audioFilePath)
                    val fileName = getFileNameFromLocalPath(audioFilePath)
                    audioDurationStringList.add(durationString)
                    audioPathList.add(audioFilePath)
                    audiosList.add(MultipleAudios(audioFilePath, durationString, fileName))
                    feedMultipleAudios.add(FeedMultipleAudios(durationString, audioFilePath, fileName))
                }

                if (addMoreFeedFiles) {
                    multipleFeedFilesPagerAdapter = MultipleFeedFilesPagerAdapter(
                        this,

                        isFullScreen = true
                    )
                    binding.viewPager.adapter = multipleFeedFilesPagerAdapter
                    for (audio in feedMultipleAudios) {
                        feedUploadViewModel.addMixedFeedUploadDataClass(
                            MixedFeedUploadDataClass(audios = audio, fileTypes = "audio")
                        )
                    }
                    val mixedFeedFiles = feedUploadViewModel.getMixedFeedUploadDataClass()
                    multipleFeedFilesPagerAdapter?.setMixedFeedUploadDataClass(mixedFeedFiles)
                } else {
                    multipleAudioAdapter = MultipleFeedAudioAdapter(this, audiosList, this)
                    binding.viewPager.adapter = multipleAudioAdapter
                    for (audio in feedMultipleAudios) {
                        feedUploadViewModel.addMixedFeedUploadDataClass(
                            MixedFeedUploadDataClass(audios = audio, fileTypes = "audio")
                        )
                    }
                }
                updateFileUIVisibility()
                setupViewPager()
                setupMediaAdapter()
            }
        }

        videoPickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
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
                    Log.d(TAG, "feedUploadViewModel: mixedFilesCount = ${feedUploadViewModel.mixedFilesCount}")
                    for (uri in uriString) {
                        val vUri = uri.toUri()
                        videoUri.add(vUri)
                        Log.d(TAG, "onCreate: Uri $uri videoUri $videoUri vUri $vUri")
                    }
                    for (i in videoPaths.indices) {
                        val videoPath = videoPaths[i]
                        val videoPathUri = videoUri[i]
                        Log.d(TAG, "Video Path: $videoPath")
                        val durationString = getFormattedDuration(videoPath)
                        val fileName = getFileNameFromLocalPath(videoPath)
                        val videoThumbnail = getFirstFrameAsThumbnail(videoPathUri)
                        Log.d(TAG, "onCreate videoThumbnail: $videoThumbnail")
                        val videoItem = FeedMultipleVideos(videoPath, durationString, fileName, videoPathUri.toString(), videoThumbnail)
                        videosList.add(videoItem)
                        newVideosList.add(videoItem)
                    }
                }

                Log.d(TAG, "onCreate: videoUri.size ${videoUri.size} videoPathList.size ${videoPathList.size}")
                videoUris.addAll(videoUri)
                this.videoPaths.addAll(videoPathList)

                val arrayList: ArrayList<FeedMultipleVideos> = ArrayList(videosList)
                if (addMoreFeedFiles) {
                    multipleFeedFilesPagerAdapter = MultipleFeedFilesPagerAdapter(
                        this,

                        isFullScreen = true
                    )
                    binding.viewPager.adapter = multipleFeedFilesPagerAdapter
                    for (video in newVideosList) {
                        feedUploadViewModel.addMixedFeedUploadDataClass(
                            MixedFeedUploadDataClass(videos = video, fileTypes = "video")
                        )
                    }
                    val mixedFeedFiles = feedUploadViewModel.getMixedFeedUploadDataClass()
                    multipleFeedFilesPagerAdapter?.setMixedFeedUploadDataClass(mixedFeedFiles)
                } else {
                    for (video in newVideosList) {
                        feedUploadViewModel.addMixedFeedUploadDataClass(
                            MixedFeedUploadDataClass(videos = video, fileTypes = "video")
                        )
                    }
                    multipleSelectedFeedVideoAdapter = MultipleSelectedFeedVideoAdapter(this, arrayList, this)
                    binding.viewPager.adapter = multipleSelectedFeedVideoAdapter
                }

                binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
                updateFileUIVisibility()
                setupMediaAdapter()
            }
        }


        documentPickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->

            if (result.resultCode == RESULT_OK) {
                setAddMoreFeedVisible()
                val newDocumentsList: MutableList<FeedMultipleDocumentsDataClass> = mutableListOf()
                fileType = "mixed_files"
                val data = result.data ?: return@registerForActivityResult

                if (!addMoreFeedFiles) {
                    documentsList.clear()
                }

                try {
                    if (data.clipData != null) {
                        // Multiple documents selected
                        val count = data.clipData!!.itemCount
                        Log.d(TAG, "Selected $count documents")
                        feedUploadViewModel.mixedFilesCount += count

                        for (i in 0 until count) {
                            val uri = data.clipData!!.getItemAt(i).uri
                            documentUris.add(uri)

                            // Process each document individually
                            val documentData = handleDocumentUriToUploadReturn(uri)

                            if (documentData.filename != "Error Loading Document") {
                                // Generate thumbnail for EACH document and store it permanently
                                Log.d(TAG, "Generating thumbnail for document $i: ${documentData.filename}")
                                val documentThumbnail = generateDocumentThumbnail(documentData.uriFile, documentData)

                                // CRITICAL: Ensure thumbnail is properly assigned and persisted
                                documentData.documentThumbnailFilePath = documentThumbnail

                                // Add to both lists to ensure persistence
                                documentsList.add(documentData)
                                newDocumentsList.add(documentData)

                                Log.d(TAG, "Document $i processed with thumbnail: ${documentThumbnail != null} for ${documentData.filename}")
                            } else {
                                Log.e(TAG, "Failed to process document at index $i")
                            }
                        }
                    } else if (data.data != null) {
                        // Single document selected
                        val uri = data.data!!
                        feedUploadViewModel.mixedFilesCount += 1

                        documentUris.add(uri)
                        val documentData = handleDocumentUriToUploadReturn(uri)

                        if (documentData.filename != "Error Loading Document") {
                            Log.d(TAG, "Generating thumbnail for single document: ${documentData.filename}")
                            val documentThumbnail = generateDocumentThumbnail(documentData.uriFile, documentData)

                            // CRITICAL: Store thumbnail permanently in the object
                            documentData.documentThumbnailFilePath = documentThumbnail

                            documentsList.add(documentData)
                            newDocumentsList.add(documentData)

                            Log.d(TAG, "Single document processed with thumbnail: ${documentThumbnail != null} for ${documentData.filename}")
                        } else {
                            Log.e(TAG, "Failed to process single document")
                        }
                    }

                    if (newDocumentsList.isNotEmpty()) {
                        // Ensure all documents have thumbnails before proceeding


                        // Log thumbnail status for verification
                        documentsList.forEachIndexed { index, doc ->
                            Log.d(TAG, "Final check - Document $index (${doc.filename}) has thumbnail: ${doc.documentThumbnailFilePath != null}")
                        }

                        // FIXED: Clear ViewModel before adding new documents to prevent duplication
                        if (!addMoreFeedFiles) {
                            feedUploadViewModel.clearMixedFeedUploadDataClass()
                        }

                        // Add to ViewModel with thumbnails intact
                        for (doc in newDocumentsList) {
                            feedUploadViewModel.addMixedFeedUploadDataClass(
                                MixedFeedUploadDataClass(
                                    documents = doc, // This doc already has thumbnail stored
                                    fileTypes = doc.documentType
                                )
                            )
                        }

                        // FIXED: Always use MultipleFeedFilesPagerAdapter for consistent behavior
                        multipleFeedFilesPagerAdapter = MultipleFeedFilesPagerAdapter(
                            this,

                            isFullScreen = true
                        )
                        binding.viewPager.adapter = multipleFeedFilesPagerAdapter

                        val mixedFeedFiles = feedUploadViewModel.getMixedFeedUploadDataClass()
                        multipleFeedFilesPagerAdapter?.setMixedFeedUploadDataClass(mixedFeedFiles)

                        ensureDocumentThumbnailPersistence()

                        Log.d(TAG, "Set up MultipleFeedFilesPagerAdapter with ${mixedFeedFiles.size} documents")

                        // Verify all thumbnails are properly set
                        multipleFeedFilesPagerAdapter?.verifyThumbnailIntegrity()

                        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
                        updateFileUIVisibility()

                        // FIXED: Show preview for the first document and setup page navigation
                        if (documentsList.isNotEmpty()) {
                            showDocumentPreview(documentsList[0])

                            binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                                override fun onPageSelected(position: Int) {
                                    super.onPageSelected(position)

                                    Log.d(TAG, "ViewPager page changed to: $position")

                                    // Get document from the local documentsList instead of ViewModel to preserve thumbnails
                                    if (position < documentsList.size) {
                                        val documentData = documentsList[position]

                                        // Ensure the document has a thumbnail before showing preview
                                        if (documentData.documentThumbnailFilePath == null) {
                                            Log.w(TAG, "Document ${documentData.filename} missing thumbnail on page change, regenerating...")
                                            val thumbnail = generateDocumentThumbnail(documentData.uriFile, documentData)
                                            documentData.documentThumbnailFilePath = thumbnail

                                            // Also update the ViewModel copy
                                            val mixedFeedFiles = feedUploadViewModel.getMixedFeedUploadDataClass()
                                            if (position < mixedFeedFiles.size) {
                                                mixedFeedFiles[position].documents?.documentThumbnailFilePath = thumbnail
                                            }
                                        }

                                        showDocumentPreview(documentData)
                                        forceRefreshDocumentUI()

                                        Log.d(TAG, "ViewPager page changed to: $position, showing: ${documentData.filename} with thumbnail: ${documentData.documentThumbnailFilePath != null}")
                                    } else {
                                        Log.w(TAG, "ViewPager position $position out of bounds for documentsList size: ${documentsList.size}")
                                    }
                                }
                            })

                            // Make sure ViewPager2 shows all pages properly
                            binding.viewPager.offscreenPageLimit = 1
                            Log.d(TAG, "Setup ViewPager2 for ${documentsList.size} documents with page navigation")
                        }

                        Log.d(TAG, "Successfully processed ${newDocumentsList.size} documents with persistent thumbnails")
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Error processing selected documents", e)
                    runOnUiThread {
                        Toast.makeText(this, "Error processing selected documents", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Log.d(TAG, "Document picker was cancelled or failed")
            }

            setupMediaAdapter()
        }



        binding.postButton.setOnClickListener {
            val caption = binding.editTextText.text.toString()
            feedUploadViewModel.getMixedFeedUploadDataClass()
            val resultIntent = Intent()
            val gson = createGson()

            if (fileType == "mixed_files") {
                val mixedFeedFiles = feedUploadViewModel.getMixedFeedUploadDataClass()
                Log.d(TAG, "onCreate: send mixed files $mixedFeedFiles")
                if (text.isNotEmpty()) {
                    val tagsArray = ArrayList<String>()
                    tagsArray.add(tags.toString())
                    val uploadDataJson = gson.toJson(mixedFeedFiles)
                    resultIntent.putExtra("mixedFiles", uploadDataJson)
                    resultIntent.putExtra("caption", text)
                    resultIntent.putExtra("tags", tags.toString())
                    resultIntent.putExtra("contentType", "mixed_files")
                    setResult(RESULT_OK, resultIntent)

                    uploadMixedFeed(mixedFeedFiles, text, tags)
                    finish()

                } else {
                    val tagsArray = ArrayList<String>()
                    tagsArray.add(tags.toString())
                    val uploadDataJson = gson.toJson(mixedFeedFiles)
                    resultIntent.putExtra("mixedFiles", uploadDataJson)
                    resultIntent.putExtra("caption", caption)
                    resultIntent.putExtra("tags", tags.toString())
                    resultIntent.putExtra("contentType", "mixed_files")
                    setResult(RESULT_OK, resultIntent)

                    uploadMixedFeed(mixedFeedFiles, caption, tags)

                    finish()
                }
            } else {
                Log.d(TAG, "onCreate: Lets upload some text")
                resultIntent.putExtra("tags", tags.toString())
                resultIntent.putExtra("contentType", "text")
                if (text.isNotEmpty()) {
                    resultIntent.putExtra("caption", text)
                    feedUploadViewModel.uploadTextFeed(
                        text, "text", tags,
                        onSuccess = { data ->
                            Log.d(TAG, "Data received: $data")
                            EventBus.getDefault().post(FeedUploadResponseEvent(data._id))
                            Log.i(TAG, "onCreate: after event bus")
                        },
                        onError = { errorMessage ->
                            Log.e(TAG, "Error occurred: $errorMessage")
                        }
                    )
                } else {
                    resultIntent.putExtra("caption", caption)
                    feedUploadViewModel.uploadTextFeed(
                        caption, "text", tags,
                        onSuccess = { data ->
                            Log.d(TAG, "Data received: $data")
                            EventBus.getDefault().post(FeedUploadResponseEvent(data._id))
                            Log.i(TAG, "onCreate: after event bus2")
                        },
                        onError = { errorMessage ->
                            Log.e(TAG, "Error occurred: $errorMessage")
                        }
                    )
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }

        binding.cancelButton.setOnClickListener {
            cleanupResources()
            finish()
        }


    }

    private fun initializeViews() {

        actualFileClicked = findViewById(R.id.actualFileClicked)
        iconLayout = findViewById(R.id.iconLayout)
        forUploadingFileThumbNail = findViewById(R.id.forUploadingFileThumbNail)
        interactionsBox = findViewById(R.id.interactionsBox)
        toolbar = findViewById(R.id.toolbar)
        cancelButton = findViewById(R.id.cancelButton)
        moreButton = findViewById(R.id.moreButton)
        titleTextView = findViewById(R.id.titleTextView)
        uploadedFileThumbNail = findViewById(R.id.uploadedFileThumbNail)
        viewPager = findViewById(R.id.viewPager)
        circleIndicator = findViewById(R.id.circleIndicator)
        selectCoverText = findViewById(R.id.selectCoverText)
        recyclerView2 = findViewById(R.id.recyclerView2)
        saveChanges = findViewById(R.id.saveChanges)
        editTextText = findViewById(R.id.editTextText)
        filterIndicator = findViewById(R.id.filterIndicator)
        topicsChipsContainer = findViewById(R.id.topicsChipsContainer)
        locationBadge = findViewById(R.id.locationBadge)
        tagPeopleLayout = findViewById(R.id.tagPeopleLayout)
        topicsLayout = findViewById(R.id.topics_layout)
        locationLayout = findViewById(R.id.location_layout)
        addMoreFeedLayout = findViewById(R.id.addMoreFeed)
        tagPeopleText = findViewById(R.id.tagPeopleText)
        topicsText = findViewById(R.id.topicsText)
        locationText = findViewById(R.id.locationText)
        buttonsLayout = findViewById(R.id.buttonsLayout)
        draftButton = findViewById(R.id.draftButton)
        postButton = findViewById(R.id.postButton)
        feedRecyclerView = findViewById(R.id.feedRecyclerView)
        fullScreenAnyFileView = findViewById(R.id.fullScreenAnyFileView)
        bottomFeedVideoProgressSeekBar = findViewById(R.id.bottomFeedVideoProgressSeekBar)
        videoDurationText = findViewById(R.id.videoDurationText)
        videoView = findViewById(R.id.videoView)
        clickableOverlay = findViewById(R.id.clickableOverlay)
        mainContentContainer = findViewById(R.id.mainContentContainer)
        fullScreenFileContainer = findViewById(R.id.fullScreenFileContainer)
        fullScreenFileView = findViewById(R.id.fullScreenFileView)
        closeFullScreenButton = findViewById(R.id.closeFullScreenButton)
        fullScreenPlayPauseButton = findViewById(R.id.fullScreenPlayPauseButton)
        fullScreenViewPager = findViewById(R.id.fullScreenViewPager)
        fullScreenIndicator = findViewById(R.id.fullScreenIndicator)
        playButton = findViewById(R.id.playButton)
        pauseButton = findViewById(R.id.pauseButton)
    }

    private fun setupPlayButton() {
        Log.d(TAG, "setupPlayButton: videoUri=$videoUri, fileType=$fileType")

        val mixedFeedUploadData =
            multipleFeedFilesPagerAdapter?.getItem(binding.viewPager.currentItem)
        val detectedFileType = when {
            mixedFeedUploadData?.fileTypes == "video" ||
                    mixedFeedUploadData?.videos != null -> "video"
            mixedFeedUploadData?.fileTypes == "audio" ||
                    mixedFeedUploadData?.audios != null -> "audio"
            mixedFeedUploadData?.fileTypes == "image" ||
                    mixedFeedUploadData?.images != null -> "image"
            mixedFeedUploadData?.fileTypes == "document" ||
                    mixedFeedUploadData?.documents != null -> "document"
            else -> fileType ?: "unknown"
        }

        Log.d(TAG, "setupPlayButton: Detected file type: $detectedFileType")

        // Only show play button for video and audio files
        val showPlayButton = detectedFileType == "audio" || detectedFileType == "video"
        binding.playButton.visibility = if (showPlayButton) View.VISIBLE else View.GONE

        configureUIForFileType(detectedFileType)

        Log.d(TAG,
            "setupPlayButton: Configuration complete for fileType=$detectedFileType")
    }

    inner class MediaViewPagerAdapter(
        private val context: Context,
        private var mediaItems: MutableList<MixedFeedUploadDataClass>, // Changed from Uri to your data class
        private val onMediaRemoved: (Int, Int) -> Unit,
        private val feedUploadViewModel: FeedUploadViewModel,
    ) : RecyclerView.Adapter<MediaViewPagerAdapter.MediaViewHolder>() {

        // Add method to update data
        fun updateMediaItems(newItems: List<MixedFeedUploadDataClass>) {
            mediaItems.clear()
            mediaItems.addAll(newItems)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_media_display, parent, false
            )
            return MediaViewHolder(view)
        }

        override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
            if (position < mediaItems.size) {
                holder.bind(mediaItems[position])
            }
        }

        override fun getItemCount(): Int = mediaItems.size

        inner class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val mediaImageView: ImageView = itemView.findViewById(R.id.mediaImageView)
            private val playButton: ImageView = itemView.findViewById(R.id.playButton)
            private val fileTypeIcon: ImageView = itemView.findViewById(R.id.fileTypeIcon)
            private val removeButton: ImageView = itemView.findViewById(R.id.removeButton)
            private val videoInfoContainer: LinearLayout = itemView.findViewById(R.id.videoInfoContainer)
            private val videoDuration: TextView = itemView.findViewById(R.id.videoDuration)

            fun bind(mediaItem: MixedFeedUploadDataClass) {
                playButton.background = null

                // Reset visibility
                playButton.visibility = View.GONE
                fileTypeIcon.visibility = View.GONE
                videoInfoContainer.visibility = View.GONE
                videoDuration.visibility = View.GONE

                when (mediaItem.fileTypes) {
                    "image" -> handleImageFile(mediaItem.images)
                    "video" -> handleVideoFile(mediaItem.videos)
                    "audio" -> handleAudioFile(mediaItem.audios)
                    "document" -> handleDocumentFile(mediaItem.documents)
                    else -> {
                        Log.w("MediaAdapter", "Unknown file type: ${mediaItem.fileTypes}")
                        handleUnknownFile()
                    }
                }

                removeButton.setOnClickListener {

                    removeMediaFile(adapterPosition)
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION && position < mediaItems.size) {
                        // Remove from ViewModel
                        feedUploadViewModel.removeMixedFeedUploadDataClass(position)

                        // Update local data
                        mediaItems.removeAt(position)
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, mediaItems.size)

                        // Notify callback
                        onMediaRemoved(position, mediaItems.size)
                    }
                }


            }

            private fun handleImageFile(image: FeedMultipleImages?) {
                image ?: return

                playButton.visibility = View.GONE
                fileTypeIcon.visibility = View.GONE
                videoInfoContainer.visibility = View.GONE

                mediaImageView.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                mediaImageView.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                mediaImageView.scaleType = ImageView.ScaleType.CENTER_CROP

                val imageSource = if (!image.compressedImagePath.isNullOrEmpty()) {
                    File(image.compressedImagePath)
                } else {
                    // Convert Uri to file path if it's a file URI
                    val uri = Uri.parse(image.imagePath)
                    if (uri.scheme == "file") {
                        File(uri.path ?: "")
                    } else {
                        // For content URIs or other schemes, pass the URI directly to Glide
                        uri
                    }
                }

                Glide.with(context)
                    .load(imageSource)
                    .centerCrop()
                    .placeholder(R.drawable.imageplaceholder)
                    .error(R.drawable.imageplaceholder)
                    .into(mediaImageView)
            }

            private fun handleVideoFile(video: FeedMultipleVideos?) {
                video ?: return

                playButton.visibility = View.VISIBLE
                fileTypeIcon.visibility = View.GONE
                videoInfoContainer.visibility = View.VISIBLE
                videoDuration.visibility = View.VISIBLE
                videoDuration.text = video.videoDuration

                mediaImageView.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                mediaImageView.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                mediaImageView.scaleType = ImageView.ScaleType.CENTER_CROP

            }

            private fun handleAudioFile(audio: FeedMultipleAudios?) {
                audio ?: return

                playButton.visibility = View.VISIBLE
                fileTypeIcon.visibility = View.GONE
                videoInfoContainer.visibility = View.VISIBLE
                videoDuration.visibility = View.VISIBLE
                videoDuration.text = audio.duration

                loadAudioThumbnail(audio.filename)
            }

            private fun handleDocumentFile(document: FeedMultipleDocumentsDataClass?) {
                document ?: return

                playButton.visibility = View.GONE
                fileTypeIcon.visibility = View.VISIBLE
                videoInfoContainer.visibility = View.GONE

                mediaImageView.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                mediaImageView.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                mediaImageView.scaleType = ImageView.ScaleType.CENTER_CROP

                // Use stored thumbnail bitmap if available
                if (document.documentThumbnailFilePath != null) {
                    // Load the bitmap directly since documentThumbnailFilePath is a Bitmap
                    Glide.with(context)
                        .load(document.documentThumbnailFilePath) // This is a Bitmap
                        .centerCrop()
                        .placeholder(getDocumentIcon(document.documentType))
                        .error(getDocumentIcon(document.documentType))
                        .into(mediaImageView)
                } else if (document.uriFile != null && document.uriFile!!.exists()) {
                    // Try to load from the file if available
                    Glide.with(context)
                        .load(document.uriFile)
                        .centerCrop()
                        .placeholder(getDocumentIcon(document.documentType))
                        .error(getDocumentIcon(document.documentType))
                        .into(mediaImageView)
                } else if (document.uri != null) {
                    // Try to load from the URI if available
                    Glide.with(context)
                        .load(document.uri)
                        .centerCrop()
                        .placeholder(getDocumentIcon(document.documentType))
                        .error(getDocumentIcon(document.documentType))
                        .into(mediaImageView)
                } else {
                    // Fallback to default icon if no thumbnail/file/uri is available
                    mediaImageView.setImageResource(getDocumentIcon(document.documentType))
                }

                fileTypeIcon.setImageResource(getDocumentIcon(document.documentType))
            }

            private fun handleUnknownFile() {
                playButton.visibility = View.GONE
                fileTypeIcon.visibility = View.VISIBLE
                videoInfoContainer.visibility = View.GONE
                fileTypeIcon.setImageResource(R.drawable.flash21)
                mediaImageView.setImageResource(R.drawable.flash21)
            }

            private fun loadAudioThumbnail(fileName: String) {
                mediaImageView.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                mediaImageView.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

                // Set audio-specific background and icon
                mediaImageView.setBackgroundColor(Color.parseColor("#616161"))
                mediaImageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
                val drawable = ContextCompat.getDrawable(context, R.drawable.ic_audio_white_large_icon)?.mutate()
                drawable?.let {
                    it.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                    mediaImageView.setImageDrawable(it)
                }
            }

            private fun getDocumentIcon(documentType: String?): Int {
                return when (documentType?.lowercase()) {
                    "pdf" -> R.drawable.pdf_icon
                    "doc", "docx" -> R.drawable.word_icon
                    "xls", "xlsx" -> R.drawable.excel_icon
                    "ppt", "pptx" -> R.drawable.powerpoint_icon
                    "txt" -> R.drawable.text_icon
                    else -> R.drawable.flash21
                }
            }


            private fun removeMediaFile(position: Int) {
                if (position >= 0 && position < mediaItems.size) {
                    mediaItems.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, mediaItems.size)
                    onMediaRemoved(position, mediaItems.size)
                }
            }

        }


    }

    private fun setupMediaAdapter() {
        val mixedFeedFiles = feedUploadViewModel.getMixedFeedUploadDataClass()

        mediaViewPagerAdapter = MediaViewPagerAdapter(
            this, // context
            mixedFeedFiles.toMutableList(), // mediaItems
            { position, remainingCount -> // onMediaRemoved lambda - this should be third
                // Handle media removal
                if (remainingCount == 0) {
                    updateFileUIVisibility()
                }

                // Update main ViewPager adapter
                multipleFeedFilesPagerAdapter?.let { adapter ->
                    val updatedList = feedUploadViewModel.getMixedFeedUploadDataClass()
                    adapter.setMixedFeedUploadDataClass(updatedList)
                    adapter.notifyDataSetChanged()
                }
            },
            feedUploadViewModel // feedUploadViewModel - this should be fourth
        )

        binding.recyclerView2.adapter = mediaViewPagerAdapter
    }



// Remove all fullscreen-related variables and methods, keep only these essential parts:

    private fun setupClickListeners() {
        // Non-media click listeners (keep existing)
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

        addMoreFeedLayout.setOnClickListener {
            val mixedFeedFiles = feedUploadViewModel.getMixedFeedUploadDataClass()
            if (mixedFeedFiles.size > 10) {
                Toast.makeText(this, "Select 10 files only", Toast.LENGTH_SHORT).show()
            } else {
                Log.d(TAG, "addMoreFeed clicked")
                addMoreFeedFiles = true
                showAttachmentDialog()
            }
        }

        interactionsBox.setOnClickListener {
            ImagePicker.with(this).cropSquare().compress(512).maxResultSize(512, 512)
                .createIntent { intent: Intent ->
                    imagePickLauncher.launch(intent)
                }
        }
    }

    // Simplified media area setup - only launches AnyFileFullScreenActivity
    @SuppressLint("ClickableViewAccessibility")
    private fun setupMediaAreaClickable() {
        binding.actualFileClicked.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    val currentPosition = binding.viewPager.currentItem
                    Log.d("UploadFeedActivity", "Media area touch released - launching fullscreen")
                    launchFullScreenActivity(currentPosition)
                    true
                }
                else -> false
            }
        }

        binding.actualFileClicked.setOnClickListener { view ->
            val currentPosition = binding.viewPager.currentItem
            Log.d("UploadFeedActivity", "Media File Area clicked - launching fullscreen")
            launchFullScreenActivity(currentPosition)
        }
    }

    // Launch AnyFileFullScreenActivity for all file types
    private fun launchFullScreenActivity(startPosition: Int) {
        try {
            val allUrls = getAllMediaUrls()
            val videoThumbnails = getAllVideoThumbnails()

            if (allUrls.isNotEmpty()) {
                val intent = Intent(this, AnyFileFullScreenActivity::class.java).apply {
                    putStringArrayListExtra("imageUrls", ArrayList(allUrls))
                    putExtra("position", startPosition)
                    if (videoThumbnails.isNotEmpty()) {
                        putStringArrayListExtra("videoThumbnails", ArrayList(videoThumbnails))
                    }
                }
                startActivity(intent)
                Log.d("UploadFeedActivity", "Launched AnyFileFullScreenActivity with ${allUrls.size} items, startPosition: $startPosition")
            } else {
                Log.e("UploadFeedActivity", "No media URLs to display")
                Toast.makeText(this, "No media to display", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("UploadFeedActivity", "Error launching full screen activity: ${e.message}")
            Toast.makeText(this, "Error opening full screen view", Toast.LENGTH_SHORT).show()
        }
    }

    // Get all media URLs for AnyFileFullScreenActivity
    private fun getAllMediaUrls(): List<String> {
        val urls = mutableListOf<String>()
        try {
            multipleFeedFilesPagerAdapter?.getAllItems()?.forEach { item ->
                when {
                    item.images != null -> urls.add(item.images!!.imagePath)
                    item.videos != null -> urls.add(item.videos!!.videoPath)
                    item.audios != null -> urls.add(item.audios!!.audioPath)
                    item.documents != null -> urls.add(item.documents!!.pdfFilePath)
                }
            }
            Log.d("UploadFeedActivity", "getAllMediaUrls: Found ${urls.size} URLs")
        } catch (e: Exception) {
            Log.e("UploadFeedActivity", "Error getting media URLs: ${e.message}")
        }
        return urls
    }

    // Get thumbnails for videos/documents
    private fun getAllVideoThumbnails(): List<String> {
        val thumbnails = mutableListOf<String>()
        try {
            multipleFeedFilesPagerAdapter?.getAllItems()?.forEach { item ->
                when {
                    item.videos?.thumbnail != null -> {
                        thumbnails.add((item.videos!!.thumbnail ?: "") as String)
                    }
                    item.documents != null -> {
                        // For documents, add empty string or document thumbnail path if available
                        thumbnails.add("")
                    }
                    else -> thumbnails.add("")
                }
            }
            Log.d("UploadFeedActivity", "getAllVideoThumbnails: Found ${thumbnails.size} thumbnails")
        } catch (e: Exception) {
            Log.e("UploadFeedActivity", "Error getting thumbnails: ${e.message}")
        }
        return thumbnails
    }

    // Keep existing ViewPager setup for normal viewing (not fullscreen)
    @SuppressLint("ClickableViewAccessibility")
    private fun setupViewPager() {
        binding.viewPager.apply {
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                @SuppressLint("SetTextI18n")
                override fun onPageSelected(position: Int) {
                    Log.d(TAG, "Page selected: $position")
                    binding.selectCoverText.visibility = View.VISIBLE
                    val currentItem = multipleFeedFilesPagerAdapter?.getItem(position)
                    handlePageSelection(currentItem, position)
                }
            })
        }

        val indicator = findViewById<CircleIndicator3>(R.id.circleIndicator)
        indicator.setViewPager(binding.viewPager)
    }

    // Keep existing page selection handling for normal viewing
    @SuppressLint("SetTextI18n")
    private fun handlePageSelection(currentItem: MixedFeedUploadDataClass?, position: Int) {
        currentItem?.let { item ->
            when (item.fileTypes) {
                "document" -> handleDocumentPage(item, position)
                "image" -> handleImagePage(item, position)
                "video" -> handleVideoPage(item, position)
                "audio" -> handleAudioPage(item, position)
            }
        }
    }

    // Keep existing page handlers for normal viewing (remove fullscreen parts)
    private fun handleDocumentPage(item: MixedFeedUploadDataClass, position: Int) {
        val docDetails = if (!addMoreFeedFiles) {
            multipleFeedFilesPagerAdapter?.getItem(position)?.documents
        } else {
            multipleFeedFilesPagerAdapter?.getDocumentDetails(position)
        }

        Handler(Looper.getMainLooper()).postDelayed({
            docDetails?.let { details ->
                binding.selectCoverText.text = "File name: ${details.filename}\nPages: ${details.numberOfPages}"
                binding.uploadedFileThumbNail.colorFilter = null
                binding.uploadedFileThumbNail.setPadding(0, 0, 0, 0)

                if (details.documentThumbnailFilePath != null && !details.documentThumbnailFilePath!!.isRecycled) {
                    try {
                        Glide.with(this@UploadFeedActivity)
                            .load(details.documentThumbnailFilePath)
                            .placeholder(R.drawable.documents)
                            .error(R.drawable.documents)
                            .centerCrop()
                            .into(binding.uploadedFileThumbNail)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error loading document thumbnail", e)
                        binding.uploadedFileThumbNail.setImageResource(R.drawable.documents)
                    }
                } else {
                    binding.uploadedFileThumbNail.setImageResource(R.drawable.documents)
                }

                configureUIForDocument()
            }
        }, 100)
    }

    private fun handleImagePage(item: MixedFeedUploadDataClass, position: Int) {
        val imageDetails = item.images
        Handler(Looper.getMainLooper()).postDelayed({
            imageDetails?.let { details ->
                binding.selectCoverText.text = "File name: ${File(details.imagePath).name}"
                binding.uploadedFileThumbNail.colorFilter = null
                binding.uploadedFileThumbNail.setPadding(0, 0, 0, 0)

                val imagePath = details.compressedImagePath.takeIf { it.isNotEmpty() } ?: details.imagePath
                Glide.with(this@UploadFeedActivity)
                    .load(File(imagePath))
                    .placeholder(R.drawable.flash21)
                    .error(R.drawable.flash21)
                    .centerCrop()
                    .into(binding.uploadedFileThumbNail)

                configureUIForImage()
            }
        }, 100)
    }

    @SuppressLint("SetTextI18n")
    private fun handleVideoPage(item: MixedFeedUploadDataClass, position: Int) {
        val videoDetails = if (!addMoreFeedFiles) {
            multipleSelectedFeedVideoAdapter.getVideoDetails(position)
        } else {
            multipleFeedFilesPagerAdapter?.getVideoDetails(position)
        }

        Handler(Looper.getMainLooper()).postDelayed({
            videoDetails?.let { details ->
                binding.selectCoverText.text = "File name: ${details.fileName}\nDuration: ${details.videoDuration}"
                configureUIForVideo()

                lifecycleScope.launch(Dispatchers.IO) {
                    val videoUri = if (!addMoreFeedFiles) {
                        details.videoUri.toUri()
                    } else {
                        Uri.parse(details.videoUri)
                    }
                    val videoThumbnails = extractThumbnailsFromVideos(videoUri)
                    withContext(Dispatchers.Main) {
                        setupRecyclerView(videoThumbnails, details)
                    }
                }
            }
        }, 100)
    }

    private fun handleAudioPage(item: MixedFeedUploadDataClass, position: Int) {
        val audioDetails = if (!addMoreFeedFiles) {
            multipleAudioAdapter.getAudioDetails(position)
        } else {
            multipleFeedFilesPagerAdapter?.getAudioDetails(position)
        }

        Handler(Looper.getMainLooper()).postDelayed({
            audioDetails?.let { details ->
                val audioData = details as? FeedMultipleAudios
                val displayText = if (audioData != null) {
                    "File name: ${audioData.filename}\nDuration: ${audioData.duration}"
                } else {
                    "Audio file loaded"
                }

                binding.selectCoverText.text = displayText
                configureUIForAudio()
                Log.d(TAG, "Audio loaded: ${audioData?.filename ?: "Unknown"}")
            } ?: run {
                Log.w(TAG, "Audio details not found for position: $position")
                binding.selectCoverText.text = "Audio file not available"
                configureUIForAudio()
            }
        }, 100)
    }

    // Keep UI configuration methods (simplified)
    private fun configureUIForDocument() {
        binding.apply {
            uploadedFileThumbNail.visibility = View.VISIBLE
            videoView.visibility = View.GONE
            playButton.visibility = View.GONE
            pauseButton.visibility = View.GONE
            uploadedFileThumbNail.isClickable = true
            uploadedFileThumbNail.isFocusable = true
        }
    }

    private fun configureUIForImage() {
        binding.apply {
            uploadedFileThumbNail.visibility = View.VISIBLE
            videoView.visibility = View.GONE
            playButton.visibility = View.GONE
            pauseButton.visibility = View.GONE
        }
    }

    private fun configureUIForVideo() {
        binding.apply {
            playButton.visibility = View.VISIBLE
            pauseButton.visibility = View.GONE
            uploadedFileThumbNail.visibility = View.GONE
            videoView.visibility = View.GONE
        }
    }

    private fun configureUIForAudio() {
        binding.apply {
            playButton.visibility = View.VISIBLE
            pauseButton.visibility = View.GONE
            uploadedFileThumbNail.visibility = View.GONE
            videoView.visibility = View.GONE
            uploadedFileThumbNail.isClickable = true
            uploadedFileThumbNail.isFocusable = true
        }
    }

    // Simplified MediaPlayer management (only for non-fullscreen audio/video preview)
    private fun initializeMediaPlayer() {
        try {
            releaseMediaPlayer()
            mediaPlayer = MediaPlayer().apply {
                setOnPreparedListener {
                    isMediaPlayerReady = true
                    Log.d(TAG, "MediaPlayer prepared and ready")
                }
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                    isMediaPlayerReady = false
                    handleMediaPlayerError(what, extra)
                    true
                }
                setOnCompletionListener {
                    Log.d(TAG, "MediaPlayer playback completed")
                    isMediaPlayerReady = false
                }
            }
            Log.d(TAG, "MediaPlayer initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize MediaPlayer", e)
            mediaPlayer = null
            isMediaPlayerReady = false
        }
    }

    private fun handleMediaPlayerError(what: Int, extra: Int) {
        when (what) {
            MediaPlayer.MEDIA_ERROR_UNKNOWN -> {
                Log.e(TAG, "Unknown media error, resetting MediaPlayer")
                recoverMediaPlayer()
            }
            MediaPlayer.MEDIA_ERROR_SERVER_DIED -> {
                Log.e(TAG, "MediaPlayer server died, reinitializing")
                initializeMediaPlayer()
            }
            else -> {
                Log.e(TAG, "Unhandled MediaPlayer error, releasing")
                releaseMediaPlayer()
            }
        }
    }

    private fun recoverMediaPlayer() {
        try {
            mediaPlayer?.reset()
            isMediaPlayerReady = false
            currentMediaType = null
            Log.d(TAG, "MediaPlayer reset successfully")
        } catch (e: Exception) {
            Log.e(TAG, "MediaPlayer reset failed, releasing", e)
            releaseMediaPlayer()
        }
    }

    private fun stopMediaPlayerSafely() {
        try {
            mediaPlayer?.let { player ->
                when {
                    isMediaPlayerReady && player.isPlaying -> {
                        player.pause()
                        Log.d(TAG, "MediaPlayer paused successfully")
                    }
                    isMediaPlayerReady && !player.isPlaying -> {
                        Log.d(TAG, "MediaPlayer already paused")
                    }
                    else -> {
                        Log.d(TAG, "MediaPlayer not ready, skipping pause")
                    }
                }
            }
        } catch (e: IllegalStateException) {
            Log.w(TAG, "MediaPlayer pause failed due to invalid state", e)
            recoverMediaPlayer()
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during MediaPlayer pause", e)
            recoverMediaPlayer()
        }
    }

    private fun releaseMediaPlayer() {
        try {
            mediaPlayer?.release()
            Log.d(TAG, "MediaPlayer released")
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing MediaPlayer", e)
        } finally {
            mediaPlayer = null
            isMediaPlayerReady = false
            currentMediaType = null
        }
    }

    override fun onPause() {
        super.onPause()
        stopMediaPlayerSafely()
        viewModel.preserveDocumentThumbnails()
    }

    override fun onStop() {
        super.onStop()
        stopMediaPlayerSafely()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayer()
    }



    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onResume() {
        super.onResume()
        // Restore document thumbnails when activity resumes
        restoreDocumentThumbnails(adapter, viewModel)
    }


        // Method to safely set data to adapter while preserving document thumbnails
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private fun setAdapterDataSafely(
            adapter: MultipleFeedFilesPagerAdapter,
            viewModel: FeedUploadViewModel,
            data: List<MixedFeedUploadDataClass>
        ) {
            Log.d("UploadFeedActivity", "Setting adapter data safely with ${data.size} items")

            // First preserve thumbnails in ViewModel
            viewModel.preserveDocumentThumbnails()

            // Verify and restore thumbnails in the data before setting
            val dataWithThumbnails = data.map { mixedData ->
                mixedData.documents?.let { doc ->
                    if (doc.documentThumbnailFilePath == null) { // Check for null Bitmap instead of isNullOrEmpty()
                        // Get the thumbnail path from ViewModel and convert to Bitmap
                        viewModel.getDocumentThumbnail(doc.filename)?.let { thumbnailPath ->
                            // Convert the file path to Bitmap
                            val bitmap = BitmapFactory.decodeFile(thumbnailPath)
                            doc.documentThumbnailFilePath = bitmap
                            Log.d("UploadFeedActivity", "Restored thumbnail for: ${doc.filename}")
                        }
                    } else {
                        Log.d("UploadFeedActivity", "Document ${doc.filename} already has thumbnail")
                    }
                }
                mixedData
            }

            // Set the data with preserved thumbnails
            adapter.setMixedFeedUploadDataClass(dataWithThumbnails)

            // Verify integrity after setting
            adapter.verifyThumbnailIntegrity()

            // Also verify in ViewModel
            val allThumbnailsPresent = viewModel.verifyDocumentThumbnails()
            if (!allThumbnailsPresent) {
                Log.w("UploadFeedActivity", "Some document thumbnails are still missing after setting data")
            } else {
                Log.d("UploadFeedActivity", "All document thumbnails verified successfully")
            }
        }

        // Method to preserve thumbnails before any operation that might reset data
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private fun preserveDocumentThumbnailsBeforeOperation() {
            Log.d("UploadFeedActivity", "Preserving document thumbnails before operation")

            val allData = adapter.getAllItems()
            allData.forEach { mixedData ->
                mixedData.documents?.let { doc ->
                    if (!doc.documentThumbnailFilePath.isNullOrEmpty()) {
                        Log.d("UploadFeedActivity", "Preserving thumbnail for: ${doc.filename} -> ${doc.documentThumbnailFilePath}")
                    }
                }
            }

            // Also preserve in ViewModel
            viewModel.preserveDocumentThumbnails()
        }

        // Method to restore document thumbnails
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private fun restoreDocumentThumbnails(
            adapter: MultipleFeedFilesPagerAdapter,
            viewModel: FeedUploadViewModel
        ) {
            Log.d("UploadFeedActivity", "Restoring document thumbnails")

            val currentData = adapter.getAllItems().toMutableList()
            var thumbnailsRestored = false

            currentData.forEachIndexed { index, mixedData ->
                mixedData.documents?.let { doc ->
                    if (doc.documentThumbnailFilePath.isNullOrEmpty()) {
                        // Try to restore from ViewModel cache
                        viewModel.getDocumentThumbnail(doc.filename)?.let { cachedThumbnail ->
                            val bitmap = BitmapFactory.decodeFile(cachedThumbnail)
                            doc.documentThumbnailFilePath = bitmap
                            thumbnailsRestored = true

                            Log.d("UploadFeedActivity", "Restored thumbnail for document: ${doc.filename}")
                        }
                    }
                }
            }

            // If we restored any thumbnails, update the adapter
            if (thumbnailsRestored) {
                adapter.setMixedFeedUploadDataClass(currentData)
                Log.d("UploadFeedActivity", "Updated adapter with restored thumbnails")
            }
        }

        // Safe method to perform operations that might affect adapter data
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private fun performSafeAdapterOperation(operation: () -> Unit) {
            Log.d("UploadFeedActivity", "Performing safe adapter operation")

            // Preserve thumbnails before operation
            preserveDocumentThumbnailsBeforeOperation()

            // Perform the operation
            operation()

            // Restore thumbnails after operation
            restoreDocumentThumbnails(adapter, viewModel)
        }


        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private fun setupAdapter() {
            // When setting data to adapter, use the safe method
            val mixedData = viewModel.getMixedFeedUploadDataClass()
            setAdapterDataSafely(adapter, viewModel, mixedData)
        }



    private fun initializeActivity() {

        initializeMediaPlayer()
    }

    private fun configureUIForFileType(fileType: String) {

        with(binding) {
            when (fileType) {
                "image" -> {
                    videoView.visibility = View.GONE
                    uploadedFileThumbNail.visibility = View.VISIBLE
                    Log.d(TAG, "configureUIForFileType: Setting up image UI")
                }
                "video" -> {
                    videoView.visibility = View.VISIBLE
                    uploadedFileThumbNail.visibility = View.GONE
                    videoView.setVideoURI(videoUri)
                    binding.playButton.visibility = View.VISIBLE
                    Log.d(TAG, "configureUIForFileType: Setting up video UI")
                }
                "audio" -> {
                    videoView.visibility = View.GONE
                    uploadedFileThumbNail.visibility = View.VISIBLE
                    binding.playButton.visibility = View.VISIBLE
                    Log.d(TAG, "configureUIForFileType: Setting up audio UI")
                }
                "document" -> {
                    videoView.visibility = View.GONE
                    uploadedFileThumbNail.visibility = View.VISIBLE
                    Log.d(TAG, "configureUIForFileType: Setting up document UI")
                }
                else -> {
                    Log.w(TAG, "configureUIForFileType: Unknown file type: $fileType")
                    videoView.visibility = View.GONE
                    uploadedFileThumbNail.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun exitFullScreen() {
        // Pause any ongoing media playback
        mediaPlayer?.pause()
        binding.fullScreenFileView.pause()

        // Restore UI state
        binding.fullScreenFileContainer.visibility = View.GONE
        binding.fullScreenViewPager.visibility = View.GONE
        binding.fullScreenIndicator.visibility = View.GONE
        binding.fullScreenFileView.visibility = View.GONE
        binding.fullScreenAnyFileView.visibility = View.GONE
        binding.bottomFeedVideoProgressSeekBar.visibility = View.GONE
        binding.videoDurationText.visibility = View.GONE
        binding.fullScreenPlayPauseButton.visibility = View.GONE

        restoreOriginalUIState()
        showSystemUI()
        isInFullScreen = false
    }

    private fun showBlinkingIcon(iconRes: Int) {
        if (!::blinkingIconView.isInitialized) {
            blinkingIconView = ImageView(this).apply {
                layoutParams = ViewGroup.LayoutParams(
                    120.dpToPx(),
                    120.dpToPx()
                )
                scaleType = ImageView.ScaleType.CENTER
                setPadding(24, 24, 24, 24)
            }
        }

        blinkingIconHandler.removeCallbacksAndMessages(null)
        blinkingIconView.setImageResource(iconRes)
        blinkingIconView.visibility = View.VISIBLE

        if (blinkingIconView.parent == null) {
            binding.fullScreenFileContainer.addView(blinkingIconView)
        }

        blinkingIconView.post {
            val containerWidth = binding.fullScreenFileContainer.width
            val containerHeight = binding.fullScreenFileContainer.height
            val iconWidth = blinkingIconView.width
            val iconHeight = blinkingIconView.height

            blinkingIconView.x = (containerWidth - iconWidth) / 2f
            blinkingIconView.y = (containerHeight - iconHeight) / 2f
        }

        blinkingIconHandler.postDelayed({
            blinkingIconView.visibility = View.GONE
        }, 3000)
    }

    private fun updateSeekBarAndDuration() {
        multipleFeedFilesPagerAdapter?.getItem(fullScreenViewPager.currentItem)?.let { item ->
            when (item.fileTypes) {
                "video" -> {
                    val currentPosition = fullScreenFileView.currentPosition
                    val duration = fullScreenFileView.duration
                    if (duration > 0) {
                        binding.bottomFeedVideoProgressSeekBar.max = duration
                        binding.bottomFeedVideoProgressSeekBar.progress = currentPosition

                    }
                    if (fullScreenFileView.isPlaying && isInFullScreen) {
                        Handler(Looper.getMainLooper()).postDelayed({ updateSeekBarAndDuration() }, 100)
                    }
                }
                "audio" -> {
                    val currentPosition = mediaPlayer?.currentPosition ?: 0
                    val duration = mediaPlayer?.duration ?: 0
                    if (duration > 0) {
                        binding.bottomFeedVideoProgressSeekBar.max = duration
                        binding.bottomFeedVideoProgressSeekBar.progress = currentPosition

                    }
                    if (mediaPlayer?.isPlaying == true && isInFullScreen) {
                        Handler(Looper.getMainLooper()).postDelayed({ updateSeekBarAndDuration() }, 100)
                    }
                }
            }
        }
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

    private fun hideNonFullScreenElements() {
        binding.toolbar.visibility = View.GONE
        binding.mainContentContainer.visibility = View.GONE
        binding.buttonsLayout.visibility = View.GONE
    }

    private fun storeOriginalUIState() {
        originalUIState.clear()
        originalUIState[binding.viewPager] = binding.viewPager.visibility
        originalUIState[binding.uploadedFileThumbNail] = binding.uploadedFileThumbNail.visibility
        originalUIState[binding.forUploadingFileThumbNail] = binding.forUploadingFileThumbNail.visibility
        originalUIState[binding.playButton] = binding.playButton.visibility
        originalUIState[binding.pauseButton] = binding.pauseButton.visibility
        originalUIState[binding.selectCoverText] = binding.selectCoverText.visibility
        originalUIState[binding.recyclerView2] = binding.recyclerView2.visibility
        originalUIState[binding.interactionsBox] = binding.interactionsBox.visibility
        originalUIState[binding.editTextText] = binding.editTextText.visibility
        originalUIState[binding.iconLayout] = binding.iconLayout.visibility
        originalUIState[binding.buttonsLayout] = binding.buttonsLayout.visibility
        originalUIState[binding.toolbar] = binding.toolbar.visibility
        originalUIState[binding.mainContentContainer] = binding.mainContentContainer.visibility
        originalUIState[binding.tagPeopleLayout] = binding.tagPeopleLayout.visibility
        originalUIState[binding.topicsLayout] = binding.topicsLayout.visibility
        originalUIState[binding.locationLayout] = binding.locationLayout.visibility
        originalUIState[binding.addMoreFeed] = binding.addMoreFeed.visibility
    }

    private fun restoreOriginalUIState() {
        originalUIState.forEach { (view, visibility) -> view.visibility = visibility }
        binding.root.requestLayout()
    }


    @SuppressLint("UseKtx")
    private fun String.toUri(): Uri {
        return try {
            Uri.parse(this)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing URI: $this", e)
            Uri.EMPTY
        }
    }

    @Deprecated("This method has been deprecated in favor of using the\n " +
            "   {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n   " +
            "   The OnBackPressedDispatcher controls how back button events are dispatched\n   " +
            "   to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        if (isInFullScreen) {
            exitFullScreen()
        } else {
            super.onBackPressed()
        }
    }


    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    private fun initializeVideoUri() {
        if (videoUri == null || videoUri == Uri.EMPTY) {
            val currentItem = multipleFeedFilesPagerAdapter?.getItem(binding.viewPager.currentItem)
            if (currentItem?.fileTypes == "video") {
                currentItem.videos?.videoUri?.let { uriString ->
                    videoUri = uriString.toUri()
                    Log.d(TAG, "Initialized videoUri from current item: $videoUri")
                }
            }
        }
    }


    private fun setupUI() {

        initializeVideoUri()
        setupPlayButton()

    }

    private fun updateFileUIVisibility() {
        val hasFiles = feedUploadViewModel.getMixedFeedUploadDataClass().isNotEmpty() || vnFilePath != "null"
        Log.d(TAG, "updateFileUIVisibility: hasFiles=$hasFiles")
        binding.forUploadingFileThumbNail.visibility = if (hasFiles) View.VISIBLE else View.GONE
        binding.selectCoverText.visibility = if (hasFiles) View.VISIBLE else View.GONE
        binding.recyclerView2.visibility = if (hasFiles) View.VISIBLE else View.GONE
        binding.viewPager.visibility = if (hasFiles) View.VISIBLE else View.GONE
    }

    private fun cleanupResources() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading bitmap from URI: ${e.message}")
            null
        }
    }

    override fun saveBitmapToCache2(context: Context, bitmap: Bitmap): String {
        return com.uyscuti.social.circuit.adapter.feed.multiple_files.saveBitmapToCache(context, bitmap) ?: ""
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

    fun openDocFilePicker(mimeType: String) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mimeType
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        documentPickerLauncher.launch(intent)
    }

    private fun determineDocumentType(mimeType: String, fileName: String): String {
        return when {
            mimeType.contains("pdf") -> "pdf"
            mimeType.contains("msword") || mimeType.contains("wordprocessingml") -> "word"
            mimeType.contains("excel") || mimeType.contains("spreadsheetml") -> "excel"
            mimeType.contains("powerpoint") || mimeType.contains("presentationml") -> "powerpoint"
            mimeType.contains("text/plain") -> "text"
            mimeType.contains("text/csv") -> "csv"
            mimeType.contains("application/rtf") -> "rtf"
            mimeType.contains("zip") -> "zip"
            mimeType.contains("rar") -> "rar"
            fileName.lowercase().endsWith(".pdf") -> "pdf"
            fileName.lowercase().endsWith(".doc") || fileName.lowercase().endsWith(".docx") -> "word"
            fileName.lowercase().endsWith(".xls") || fileName.lowercase().endsWith(".xlsx") -> "excel"
            fileName.lowercase().endsWith(".ppt") || fileName.lowercase().endsWith(".pptx") -> "powerpoint"
            fileName.lowercase().endsWith(".txt") -> "text"
            fileName.lowercase().endsWith(".csv") -> "csv"
            fileName.lowercase().endsWith(".rtf") -> "rtf"
            fileName.lowercase().endsWith(".zip") -> "zip"
            fileName.lowercase().endsWith(".rar") -> "rar"
            else -> "document"
        }
    }

    private fun createLocalCopyOfDocument(uri: Uri, fileName: String): File? {
        return try {
            val documentsDir = File(filesDir, "temp_documents")
            if (!documentsDir.exists()) {
                documentsDir.mkdirs()
            }

            val localFile = File(documentsDir, fileName)

            contentResolver.openInputStream(uri)?.use { inputStream ->
                localFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            Log.d(TAG, "Created local copy: ${localFile.absolutePath}")
            localFile
        } catch (e: Exception) {
            Log.e(TAG, "Error creating local copy of document", e)
            null
        }
    }

    private fun getDocumentPageCount(file: File?, documentType: String): String {
        if (file == null || !file.exists()) return "0"

        return try {
            when (documentType) {
                "pdf" -> {
                    getPdfPageCount(file)
                }
                "word" -> {
                    "1" // Could be enhanced to actually count Word pages
                }
                else -> "1"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting page count for ${file.name}", e)
            "1"
        }
    }

    private fun getPdfPageCount(file: File): String {
        return try {
            val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val pdfRenderer = PdfRenderer(fileDescriptor)
            val pageCount = pdfRenderer.pageCount
            pdfRenderer.close()
            fileDescriptor.close()
            pageCount.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting PDF page count", e)
            "1"
        }
    }



    override fun onThumbnailClick(thumbnail: Bitmap, videoDetails: FeedMultipleVideos) {

        this.thumbnail = thumbnail
        this.isThumbnailSelected = true

        // Use fullScreenAnyFileView (ImageView) for thumbnails
        Glide.with(this)
            .load(thumbnail)
            .into(binding.fullScreenAnyFileView)

        // Show the ImageView and hide the VideoView for thumbnail display
        binding.fullScreenAnyFileView.visibility = View.VISIBLE
        binding.fullScreenFileView.visibility = View.GONE

        binding.playButton.visibility = View.VISIBLE
        binding.recyclerView2.visibility = View.VISIBLE
        binding.selectCoverText.visibility = View.VISIBLE
    }

    private fun ensureDocumentThumbnailPersistence() {
        // Sync thumbnails from local documentsList to ViewModel
        val mixedFeedFiles = feedUploadViewModel.getMixedFeedUploadDataClass()

        documentsList.forEachIndexed { index, localDoc ->
            if (index < mixedFeedFiles.size && localDoc.documentThumbnailFilePath != null) {
                val viewModelDoc = mixedFeedFiles[index].documents
                if (viewModelDoc != null && viewModelDoc.documentThumbnailFilePath == null) {
                    viewModelDoc.documentThumbnailFilePath = localDoc.documentThumbnailFilePath
                    Log.d(TAG, "Synced thumbnail for ${localDoc.filename} from local to ViewModel")
                }
            }
        }

        // Update the adapter with synchronized data
        multipleFeedFilesPagerAdapter?.setMixedFeedUploadDataClass(mixedFeedFiles)
    }

    private fun generatePdfThumbnail(file: File?): Bitmap? {
        var fileDescriptor: ParcelFileDescriptor? = null
        var pdfRenderer: PdfRenderer? = null
        var page: PdfRenderer.Page? = null

        return try {
            fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            pdfRenderer = PdfRenderer(fileDescriptor)

            if (pdfRenderer.pageCount > 0) {
                page = pdfRenderer.openPage(0)

                val scale = 0.5f
                val width = (page.width * scale).toInt()
                val height = (page.height * scale).toInt()
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawColor(Color.WHITE)

                val rect = Rect(0, 0, width, height)
                page.render(bitmap, rect, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                Log.d(TAG, "Successfully generated PDF thumbnail for: ${file?.name}")
                bitmap
            } else {
                Log.w(TAG, "PDF has no pages: ${file?.name}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating PDF thumbnail for ${file?.name}", e)
            null
        } finally {
            try {
                page?.close()
                pdfRenderer?.close()
                fileDescriptor?.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error closing PDF resources", e)
            }
        }
    }

    private fun generateTextThumbnail(file: File?): Bitmap? {
        return try {
            val text = file?.readText(Charsets.UTF_8)?.take(500)
            if (text?.isEmpty() ?: null == true) return null

            // Use screen-based sizing like PDF
            val displayMetrics = resources.displayMetrics
            val scale = 0.5f
            val baseWidth = (displayMetrics.widthPixels * scale).toInt()
            val baseHeight = (displayMetrics.heightPixels * scale * 0.6f).toInt() // Adjust ratio for text documents

            val width = minOf(baseWidth, 400) // Cap at reasonable size
            val height = minOf(baseHeight, 500)

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // White background
            canvas.drawColor(Color.WHITE)

            val paint = Paint().apply {
                isAntiAlias = true
                color = Color.BLACK
                textSize = 14f * displayMetrics.density / 2.5f // Scale with density
                typeface = Typeface.MONOSPACE
            }

            val padding = 20f * displayMetrics.density / 2.5f
            val lineHeight = 18f * displayMetrics.density / 2.5f
            var yPosition = padding + lineHeight

            // Calculate characters per line based on width
            val availableWidth = width - (padding * 2)
            val charWidth = paint.measureText("M")
            val maxCharsPerLine = (availableWidth / charWidth).toInt()

            val lines = text?.chunked(maxCharsPerLine)
            val maxLines = ((height - padding * 2) / lineHeight).toInt()

            for (line in lines?.take(maxLines)!!) {
                if (yPosition > height - padding) break
                canvas.drawText(line, padding, yPosition, paint)
                yPosition += lineHeight
            }

            // Add border
            val borderPaint = Paint().apply {
                style = Paint.Style.STROKE
                strokeWidth = 2f
                color = Color.GRAY
            }
            canvas.drawRect(2f, 2f, width.toFloat() - 2f, height.toFloat() - 2f, borderPaint)

            Log.d(TAG, "Successfully generated text thumbnail for: ${file?.name}")
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Error generating text thumbnail for ${file?.name}", e)
            null
        }
    }

    private fun generateWordThumbnail(file: File?): Bitmap? {
        return try {
            // Use screen-based sizing like PDF
            val displayMetrics = resources.displayMetrics
            val scale = 0.5f
            val baseWidth = (displayMetrics.widthPixels * scale).toInt()
            val baseHeight = (displayMetrics.heightPixels * scale * 0.7f).toInt()

            val width = minOf(baseWidth, 400)
            val height = minOf(baseHeight, 550)

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Word document background
            canvas.drawColor(Color.WHITE)

            // Create Word-like header
            val headerPaint = Paint().apply {
                color = Color.parseColor("#2B579A")
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, width.toFloat(), 40f * displayMetrics.density / 2.5f, headerPaint)

            // Add Word icon simulation
            val iconPaint = Paint().apply {
                isAntiAlias = true
                color = Color.WHITE
                textSize = 20f * displayMetrics.density / 2.5f
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.LEFT
            }
            canvas.drawText("W", 10f, 28f * displayMetrics.density / 2.5f, iconPaint)

            // Simulate document content with lines
            val contentPaint = Paint().apply {
                color = Color.BLACK
                strokeWidth = 1.5f
                style = Paint.Style.STROKE
            }

            val startY = 60f * displayMetrics.density / 2.5f
            val lineSpacing = 20f * displayMetrics.density / 2.5f
            val leftMargin = 30f * displayMetrics.density / 2.5f
            val rightMargin = width - 30f * displayMetrics.density / 2.5f

            // Draw simulated text lines
            for (i in 0 until 15) {
                val y = startY + (i * lineSpacing)
                if (y > height - 30f) break

                val lineEnd = if (i % 4 == 3) rightMargin * 0.7f else rightMargin
                canvas.drawLine(leftMargin, y, lineEnd, y, contentPaint)
            }

            // Add border
            val borderPaint = Paint().apply {
                style = Paint.Style.STROKE
                strokeWidth = 2f
                color = Color.parseColor("#2B579A")
            }
            canvas.drawRect(2f, 2f, width.toFloat() - 2f, height.toFloat() - 2f, borderPaint)

            Log.d(TAG, "Successfully generated Word thumbnail for: ${file?.name}")
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Error generating Word thumbnail for ${file?.name}", e)
            null
        }
    }

    private fun generateExcelThumbnail(file: File?): Bitmap? {
        return try {
            // Use screen-based sizing like PDF
            val displayMetrics = resources.displayMetrics
            val scale = 0.5f
            val baseWidth = (displayMetrics.widthPixels * scale).toInt()
            val baseHeight = (displayMetrics.heightPixels * scale * 0.7f).toInt()

            val width = minOf(baseWidth, 400)
            val height = minOf(baseHeight, 550)

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Excel background
            canvas.drawColor(Color.WHITE)

            // Excel header
            val headerPaint = Paint().apply {
                color = Color.parseColor("#217346")
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, width.toFloat(), 40f * displayMetrics.density / 2.5f, headerPaint)

            // Excel icon
            val iconPaint = Paint().apply {
                isAntiAlias = true
                color = Color.WHITE
                textSize = 20f * displayMetrics.density / 2.5f
                typeface = Typeface.DEFAULT_BOLD
            }
            canvas.drawText("X", 10f, 28f * displayMetrics.density / 2.5f, iconPaint)

            // Draw grid
            val gridPaint = Paint().apply {
                color = Color.GRAY
                strokeWidth = 1f
                style = Paint.Style.STROKE
            }

            val startY = 50f * displayMetrics.density / 2.5f
            val cellWidth = width / 5f
            val cellHeight = 25f * displayMetrics.density / 2.5f

            // Draw horizontal lines
            for (i in 0..18) {
                val y = startY + (i * cellHeight)
                if (y > height) break
                canvas.drawLine(0f, y, width.toFloat(), y, gridPaint)
            }

            // Draw vertical lines
            for (i in 0..5) {
                val x = i * cellWidth
                canvas.drawLine(x, startY, x, height.toFloat(), gridPaint)
            }

            // Fill some cells with data simulation
            val dataPaint = Paint().apply {
                color = Color.parseColor("#E8F5E8")
                style = Paint.Style.FILL
            }

            // Fill random cells
            for (row in 1..3) {
                for (col in 1..4) {
                    if ((row + col) % 3 == 0) {
                        val left = col * cellWidth
                        val top = startY + (row * cellHeight)
                        val right = left + cellWidth
                        val bottom = top + cellHeight
                        canvas.drawRect(left, top, right, bottom, dataPaint)
                    }
                }
            }

            // Border
            val borderPaint = Paint().apply {
                style = Paint.Style.STROKE
                strokeWidth = 2f
                color = Color.parseColor("#217346")
            }
            canvas.drawRect(2f, 2f, width.toFloat() - 2f, height.toFloat() - 2f, borderPaint)

            Log.d(TAG, "Successfully generated Excel thumbnail for: ${file?.name}")
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Error generating Excel thumbnail for ${file?.name}", e)
            null
        }
    }

    private fun generatePowerPointThumbnail(file: File?): Bitmap? {
        return try {
            // Use screen-based sizing like PDF
            val displayMetrics = resources.displayMetrics
            val scale = 0.5f
            val baseWidth = (displayMetrics.widthPixels * scale).toInt()
            val baseHeight = (displayMetrics.heightPixels * scale * 0.7f).toInt()

            val width = minOf(baseWidth, 400)
            val height = minOf(baseHeight, 550)

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // PowerPoint background
            canvas.drawColor(Color.WHITE)

            // PowerPoint header
            val headerPaint = Paint().apply {
                color = Color.parseColor("#D24726")
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, width.toFloat(), 40f * displayMetrics.density / 2.5f, headerPaint)

            // PowerPoint icon
            val iconPaint = Paint().apply {
                isAntiAlias = true
                color = Color.WHITE
                textSize = 20f * displayMetrics.density / 2.5f
                typeface = Typeface.DEFAULT_BOLD
            }
            canvas.drawText("P", 10f, 28f * displayMetrics.density / 2.5f, iconPaint)

            // Simulate slide layout
            val slidePaint = Paint().apply {
                color = Color.parseColor("#F5F5F5")
                style = Paint.Style.FILL
            }

            val slideMargin = 30f * displayMetrics.density / 2.5f
            val slideTop = 60f * displayMetrics.density / 2.5f
            val slideBottom = height - 30f * displayMetrics.density / 2.5f

            canvas.drawRect(slideMargin, slideTop, width - slideMargin, slideBottom, slidePaint)

            // Title area
            val titlePaint = Paint().apply {
                color = Color.parseColor("#E0E0E0")
                style = Paint.Style.FILL
            }
            canvas.drawRect(
                slideMargin + 20f,
                slideTop + 20f,
                width - slideMargin - 20f,
                slideTop + 60f * displayMetrics.density / 2.5f,
                titlePaint
            )

            // Content areas
            val contentPaint = Paint().apply {
                color = Color.parseColor("#F0F0F0")
                style = Paint.Style.FILL
            }

            // Left content box
            canvas.drawRect(
                slideMargin + 20f,
                slideTop + 80f * displayMetrics.density / 2.5f,
                width / 2f - 10f,
                slideBottom - 20f,
                contentPaint
            )

            // Right content box
            canvas.drawRect(
                width / 2f + 10f,
                slideTop + 80f * displayMetrics.density / 2.5f,
                width - slideMargin - 20f,
                slideBottom - 20f,
                contentPaint
            )

            // Border
            val borderPaint = Paint().apply {
                style = Paint.Style.STROKE
                strokeWidth = 2f
                color = Color.parseColor("#D24726")
            }
            canvas.drawRect(2f, 2f, width.toFloat() - 2f, height.toFloat() - 2f, borderPaint)

            Log.d(TAG, "Successfully generated PowerPoint thumbnail for: ${file?.name}")
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Error generating PowerPoint thumbnail for ${file?.name}", e)
            null
        }
    }

    private fun generateCsvThumbnail(file: File?): Bitmap? {

        return try {
            // Use screen-based sizing like PDF
            val displayMetrics = resources.displayMetrics
            val scale = 0.5f
            val baseWidth = (displayMetrics.widthPixels * scale).toInt()
            val baseHeight = (displayMetrics.heightPixels * scale * 0.7f).toInt()

            val width = minOf(baseWidth, 400)
            val height = minOf(baseHeight, 550)

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // CSV background
            canvas.drawColor(Color.WHITE)

            // Read CSV content
            val csvContent = file?.readText(Charsets.UTF_8)?.take(1000)
            val lines = csvContent?.split("\n")?.take(15)

            if ((lines?.isNotEmpty() ?: null) == true) {
                // Draw CSV table
                val cellPaint = Paint().apply {
                    color = Color.BLACK
                    strokeWidth = 1f
                    style = Paint.Style.STROKE
                }

                val textPaint = Paint().apply {
                    isAntiAlias = true
                    color = Color.BLACK
                    textSize = 10f * displayMetrics.density / 2.5f
                    typeface = Typeface.MONOSPACE
                }

                val headerPaint = Paint().apply {
                    color = Color.parseColor("#E8F4FD")
                    style = Paint.Style.FILL
                }

                val cellHeight = 25f * displayMetrics.density / 2.5f
                val maxColumns = 4
                val cellWidth = width / maxColumns.toFloat()

                lines?.forEachIndexed { rowIndex, line ->
                    val cells = line.split(",").take(maxColumns)
                    val y = rowIndex * cellHeight

                    if (y > height - cellHeight) return@forEachIndexed

                    // Header background
                    if (rowIndex == 0) {
                        canvas.drawRect(0f, y, width.toFloat(), y + cellHeight, headerPaint)
                    }

                    cells.forEachIndexed { colIndex, cell ->
                        val x = colIndex * cellWidth

                        // Draw cell border
                        canvas.drawRect(x, y, x + cellWidth, y + cellHeight, cellPaint)

                        // Draw cell text
                        val trimmedCell = cell.trim().take(8)
                        if (trimmedCell.isNotEmpty()) {
                            canvas.drawText(
                                trimmedCell,
                                x + 5f,
                                y + cellHeight - 5f,
                                textPaint
                            )
                        }
                    }
                }
            }

            // Border
            val borderPaint = Paint().apply {
                style = Paint.Style.STROKE
                strokeWidth = 2f
                color = Color.parseColor("#4CAF50")
            }
            canvas.drawRect(2f, 2f, width.toFloat() - 2f, height.toFloat() - 2f, borderPaint)

            Log.d(TAG, "Successfully generated CSV thumbnail for: ${file?.name}")
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Error generating CSV thumbnail for ${file?.name}", e)
            null
        }
    }

    private fun createDocumentTypeIcon(documentType: String, fileName: String): Bitmap {
        // Use screen-based sizing like PDF
        val displayMetrics = resources.displayMetrics
        val scale = 0.5f
        val baseSize = (displayMetrics.widthPixels * scale * 0.8f).toInt()
        val size = minOf(baseSize, 300)

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background color based on document type
        val backgroundColor = when (documentType.lowercase()) {
            "pdf" -> Color.parseColor("#FF6B6B")
            "word" -> Color.parseColor("#4ECDC4")
            "excel" -> Color.parseColor("#45B7D1")
            "powerpoint" -> Color.parseColor("#FFA07A")
            "text", "csv" -> Color.parseColor("#98D8C8")
            else -> Color.parseColor("#F7DC6F")
        }

        canvas.drawColor(backgroundColor)

        // Paint for text
        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = 32f * displayMetrics.density / 2.5f
            typeface = Typeface.DEFAULT_BOLD
            setShadowLayer(2f, 0f, 2f, Color.BLACK)
        }

        // Draw document type text
        val typeText = documentType.uppercase()
        val centerX = size / 2f
        val centerY = size / 2f - 10f

        canvas.drawText(typeText, centerX, centerY, paint)

        // Draw file extension if available
        val extension = fileName.substringAfterLast(".", "").uppercase()
        if (extension.isNotEmpty() && extension != typeText) {
            paint.textSize = 20f * displayMetrics.density / 2.5f
            canvas.drawText(".$extension", centerX, centerY + 40f * displayMetrics.density / 2.5f, paint)
        }

        // Draw border
        val borderPaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 6f
            color = Color.WHITE
        }
        canvas.drawRect(3f, 3f, size.toFloat() - 3f, size.toFloat() - 3f, borderPaint)

        Log.d(TAG, "Created document type icon for: $documentType")
        return bitmap
    }

    private fun generateDocumentThumbnail(file: File?, documentData: FeedMultipleDocumentsDataClass): Bitmap? {
        return when (documentData.documentType.lowercase()) {
            "pdf" -> generatePdfThumbnail(file)
            "word" -> generateWordThumbnail(file)
            "excel" -> generateExcelThumbnail(file)
            "powerpoint" -> generatePowerPointThumbnail(file)
            "text" -> generateTextThumbnail(file)
            "csv" -> generateCsvThumbnail(file)
            else -> createDocumentTypeIcon(documentData.documentType, documentData.filename)
        }
    }

    private fun showDocumentPreview(documentData: FeedMultipleDocumentsDataClass) {
        try {
            Log.d(TAG, "Showing preview for: ${documentData.filename}, type: ${documentData.documentType}")

            // Ensure the ImageView is visible first
            binding.uploadedFileThumbNail.visibility = View.VISIBLE
            binding.fullScreenAnyFileView.visibility = View.VISIBLE

            // Clear any existing styling
            binding.uploadedFileThumbNail.colorFilter = null
            binding.uploadedFileThumbNail.setPadding(0, 0, 0, 0)
            binding.uploadedFileThumbNail.scaleType = ImageView.ScaleType.CENTER_CROP

            // Also ensure fullScreenAnyFileView is properly configured
            binding.fullScreenAnyFileView.colorFilter = null
            binding.fullScreenAnyFileView.setPadding(0, 0, 0, 0)
            binding.fullScreenAnyFileView.scaleType = ImageView.ScaleType.CENTER_CROP

            // Always try to show the generated thumbnail first
            if (documentData.documentThumbnailFilePath != null) {
                Log.d(TAG, "Setting Document Thumbnail Generated Bitmap for: ${documentData.filename}")

                // Set the thumbnail to both ImageViews to ensure it's visible
                binding.uploadedFileThumbNail.setImageBitmap(documentData.documentThumbnailFilePath)
                binding.fullScreenAnyFileView.setImageBitmap(documentData.documentThumbnailFilePath)

                // Make sure the correct view is visible
                binding.uploadedFileThumbNail.visibility = View.VISIBLE
                binding.fullScreenAnyFileView.visibility = View.VISIBLE
                binding.fullScreenFileView.visibility = View.GONE // Hide video view

                Log.d(TAG, "Thumbnail bitmap set successfully for: ${documentData.filename}")
            } else {
                Log.w(TAG, "No thumbnail available for ${documentData.filename}, generating new thumbnail")
                // Try to generate thumbnail on demand
                val file = documentData.uriFile
                if (file != null && file.exists()) {
                    val thumbnail = generateDocumentThumbnail(file, documentData)
                    if (thumbnail != null) {
                        // Set to both ImageViews
                        binding.uploadedFileThumbNail.setImageBitmap(thumbnail)
                        binding.fullScreenAnyFileView.setImageBitmap(thumbnail)

                        // Update the data class with the generated thumbnail
                        documentData.documentThumbnailFilePath = thumbnail

                        // Make sure views are visible
                        binding.uploadedFileThumbNail.visibility = View.VISIBLE
                        binding.fullScreenAnyFileView.visibility = View.VISIBLE
                        binding.fullScreenFileView.visibility = View.GONE

                        Log.d(TAG, "Generated and set new thumbnail for: ${documentData.filename}")
                    } else {
                        // Final fallback to icon
                        setFallbackIcon(documentData)
                    }
                } else {
                    // Final fallback to icon when no file exists
                    setFallbackIcon(documentData)
                }
            }

            // Hide media controls for documents
            binding.playButton.visibility = View.GONE
            binding.pauseButton.visibility = View.GONE

            // Show document information with filename and pages
            val documentInfo = buildString {
                val nameWithoutExtension = documentData.filename.substringBeforeLast(".")

                if (documentData.numberOfPages.isNotEmpty() && documentData.numberOfPages != "0" && documentData.numberOfPages != "1") {
                    append("$nameWithoutExtension • ${documentData.numberOfPages} pages")
                } else {
                    append(nameWithoutExtension)
                }
            }

            feedUploadViewModel.setText(documentInfo)
            Log.d(TAG, "Document preview setup complete for: ${documentData.filename}")

        } catch (e: Exception) {
            Log.e(TAG, "Error showing document preview for: ${documentData.filename}", e)
            // Final fallback
            setFallbackIcon(documentData)
            val fallbackName = documentData.filename.substringBeforeLast(".")
            feedUploadViewModel.setText(fallbackName)
        }
    }

    private fun setFallbackIcon(documentData: FeedMultipleDocumentsDataClass) {
        Log.d(TAG, "Setting fallback icon for: ${documentData.filename}")

        val fallbackBitmap = createDocumentTypeIcon(documentData.documentType, documentData.filename)

        // Set to both ImageViews
        binding.uploadedFileThumbNail.setImageBitmap(fallbackBitmap)
        binding.fullScreenAnyFileView.setImageBitmap(fallbackBitmap)

        // Ensure proper visibility
        binding.uploadedFileThumbNail.visibility = View.VISIBLE
        binding.fullScreenAnyFileView.visibility = View.VISIBLE
        binding.fullScreenFileView.visibility = View.GONE

        // Store the fallback as thumbnail
        documentData.documentThumbnailFilePath = fallbackBitmap
    }

    // Also add this method to force refresh the UI after setting thumbnails
    private fun forceRefreshDocumentUI() {
        runOnUiThread {
            binding.uploadedFileThumbNail.invalidate()
            binding.fullScreenAnyFileView.invalidate()
            binding.viewPager.invalidate()
        }
    }

    override fun handleDocumentUriToUploadReturn(uri: Uri): FeedMultipleDocumentsDataClass {
        return try {
            // Take persistable permission for the URI
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            val cursor = contentResolver.query(uri, null, null, null, null)
            var displayName = "Unknown Document"
            var size = 0L
            var mimeType = contentResolver.getType(uri) ?: "application/octet-stream"

            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = it.getColumnIndexOrThrow(OpenableColumns.SIZE)
                    displayName = it.getString(nameIndex) ?: "Unknown Document"
                    size = it.getLong(sizeIndex)
                }
            }

            // Determine document type from MIME type or file extension
            val documentType = determineDocumentType(mimeType, displayName)

            // Create a local copy of the document for reliable access
            val localFile = createLocalCopyOfDocument(uri, displayName)

            Log.d(TAG, "Document processed - Name: $displayName, Size: $size, Type: $documentType, MIME: $mimeType")

            // Create document data class first (without thumbnail)
            val documentData = FeedMultipleDocumentsDataClass(
                uri = uri,
                filename = displayName,
                numberOfPages = getDocumentPageCount(localFile, documentType),
                documentType = documentType,
                fileSize = formatFileSize(size),
                documentThumbnailFilePath = null, // Will be set immediately below
                pdfFilePath = localFile?.absolutePath ?: "",
                uriFile = localFile
            )

            // Generate thumbnail immediately after creating the data object
            val thumbnail = generateDocumentThumbnail(localFile, documentData)
            documentData.documentThumbnailFilePath = thumbnail

            Log.d(TAG, "Document URI handling complete for: $displayName, thumbnail: ${thumbnail != null}")

            documentData

        } catch (e: Exception) {
            Log.e(TAG, "Error handling document URI: $uri", e)
            // Return a default document object with error info
            FeedMultipleDocumentsDataClass(
                uri = uri,
                filename = "Error Loading Document",
                numberOfPages = "0",
                documentType = "unknown",
                fileSize = "Unknown",
                documentThumbnailFilePath = null,
                pdfFilePath = "",
                uriFile = null
            )
        }
    }

    @SuppressLint("DefaultLocale")
    private fun formatFileSize(sizeInBytes: Long): String {
        val kb = sizeInBytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            sizeInBytes < 1024 -> "$sizeInBytes B"
            kb < 1024 -> String.format("%.2f KB", kb)
            mb < 1024 -> String.format("%.2f MB", mb)
            else -> String.format("%.2f GB", gb)
        }
    }

    override fun showAttachmentDialog() {

        val dialog = BottomSheetDialog(this)

        dialog.setContentView(R.layout.shorts_and_all_feed_file_upload_bottom_dialog)

        val video = dialog.findViewById<LinearLayout>(R.id.upload_video)
        val audio = dialog.findViewById<LinearLayout>(R.id.upload_audio)
        val image = dialog.findViewById<LinearLayout>(R.id.upload_image)
        val camera = dialog.findViewById<LinearLayout>(R.id.open_camera)
        val doc = dialog.findViewById<LinearLayout>(R.id.upload_document)
        val location = dialog.findViewById<LinearLayout>(R.id.share_location)
        val vnRecord = dialog.findViewById<LinearLayout>(R.id.vnRecord)

        val dialogView = dialog.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet)
        dialogView?.startAnimation(
            AnimationUtils.loadAnimation(this, R.anim.slide_up))

        val selectableItemBackground = TypedValue()
        image?.context?.theme?.resolveAttribute(
            android.R.attr.selectableItemBackground,
            selectableItemBackground,
            true)

        image?.setBackgroundResource(
            selectableItemBackground.resourceId)
        video?.context?.theme?.resolveAttribute(
            android.R.attr.selectableItemBackground,
            selectableItemBackground
            , true)

        video?.setBackgroundResource(selectableItemBackground.resourceId)
        audio?.context?.theme?.resolveAttribute(
            android.R.attr.selectableItemBackground,
            selectableItemBackground,
            true)

        audio?.setBackgroundResource(selectableItemBackground.resourceId)
        camera?.context?.theme?.resolveAttribute(
            android.R.attr.selectableItemBackground,
            selectableItemBackground,
            true)

        camera?.setBackgroundResource(selectableItemBackground.resourceId)
        doc?.context?.theme?.resolveAttribute(
            android.R.attr.selectableItemBackground,
            selectableItemBackground, true)

        doc?.setBackgroundResource(selectableItemBackground.resourceId)
        location?.context?.theme?.resolveAttribute(
            android.R.attr.selectableItemBackground,
            selectableItemBackground,
            true)

        location?.setBackgroundResource(selectableItemBackground.resourceId)
        vnRecord?.context?.theme?.resolveAttribute(
            android.R.attr.selectableItemBackground,
            selectableItemBackground,
            true)
        vnRecord?.setBackgroundResource(selectableItemBackground.resourceId)

        image?.setOnClickListener {
            Log.d("SelectImage", "Image selector button clicked")
            pickMultipleMedia.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly))
            dialog.dismiss()
        }

        video?.setOnClickListener {
            val intent = Intent(this@UploadFeedActivity,
                FeedSelectVideoActivity::class.java)
            videoPickerLauncher.launch(intent)
            dialog.dismiss()
        }

        audio?.setOnClickListener {
            val intent = Intent(this@UploadFeedActivity,
                FeedAudioActivity::class.java)
            audioPickerLauncher.launch(intent)

            dialog.dismiss()
        }

        doc?.setOnClickListener {

            openDocFilePicker("application/*")

            dialog.dismiss()
        }

        camera?.setOnClickListener {
            val intent = Intent(this@UploadFeedActivity,
                CameraActivity::class.java)
            cameraLauncher.launch(intent)
            dialog.dismiss()
        }

        location?.visibility = View.GONE
        vnRecord?.visibility = View.INVISIBLE
        vnRecord?.setOnClickListener {
            dialog.dismiss()
        }
        location?.setOnClickListener { }
        dialog.show()
    }

    private fun CoroutineScope.extractThumbnailsFromVideos(uri: Uri): List<Bitmap> {
        val thumbnails = mutableListOf<Bitmap>()
        var retriever: MediaMetadataRetriever? = null
        try {
            retriever = MediaMetadataRetriever()
            retriever.setDataSource(this@UploadFeedActivity, uri)
            val durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val duration = durationString?.toLongOrNull() ?: 0L
            val numberOfThumbnails = 9
            if (duration > 0) {
                for (i in 0 until numberOfThumbnails) {
                    try {
                        val timePosition = if (i == 0) 500L else {
                            val remainingDuration = duration - 500
                            500 + (remainingDuration * i) / (numberOfThumbnails - 1)
                        }
                        val bitmap = retriever.getFrameAtTime(timePosition * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                        bitmap?.let { thumbnails.add(it) }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error extracting frame at position $i for video: $uri", e)
                    }
                }
            }
            retriever.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up MediaMetadataRetriever for video: $uri", e)
        } finally {
            retriever?.release()
        }
        if (thumbnails.isEmpty()) {
            try {
                retriever = MediaMetadataRetriever()
                retriever.setDataSource(this@UploadFeedActivity, uri)
                val bitmap = retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST)
                bitmap?.let { thumbnails.add(it) }
                retriever.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error creating fallback thumbnail for video: $uri", e)
            }
        }
        return thumbnails
    }

    private fun CoroutineScope.setupRecyclerView(thumbnails: List<Bitmap>,
                                                 videos: FeedMultipleVideos) {
        val adapter = FeedVideoThumbnailAdapter(thumbnails, this@UploadFeedActivity)
        binding.recyclerView2.layoutManager = LinearLayoutManager(this@UploadFeedActivity, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerView2.adapter = adapter
        binding.recyclerView2.visibility = View.VISIBLE
    }
    @SuppressLint("DefaultLocale")
    private fun getFormattedDuration(path: String): String {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(path)
            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0
            retriever.release()
            val minutes = (durationMs / 1000) / 60
            val seconds = (durationMs / 1000) % 60
            String.format("%02d:%02d", minutes, seconds)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting duration for path: $path", e)
            "00:00"
        }
    }

    private fun createGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(Uri::class.java, UriTypeAdapter())
            .create()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun uploadMixedFeed(
        mixedFiles: MutableList<MixedFeedUploadDataClass>,
        content: String,
        tags: MutableList<String>,
    ) {

        val gson = createGson()
        val compressionCount = 0
        val feedShortsBusinessId: String = generateRandomId()
        val audioListToUpload: MutableList<MixedFeedUploadDataClass> = mutableListOf()
        val compressedAudioListToUpload: MutableList<MixedFeedUploadDataClass> = mutableListOf()
        val itemsToRemove = mutableListOf<MixedFeedUploadDataClass>()


        lifecycleScope.launch(Dispatchers.IO) {

            for (mixedFile in mixedFiles) {
                if (mixedFile.audios != null) {
                    audioListToUpload.add(mixedFile)
                    itemsToRemove.add(mixedFile)
                }
            }


            for (video in mixedFiles) {
                if (video.videos != null) {

                    val thumbnailFile =
                        video.videos!!.thumbnail?.let { saveBitmapToFile(
                            it,
                            applicationContext,
                            generateRandomFileName()
                        ) }
                    val thumbnailFilePath = thumbnailFile?.absolutePath
                    if (thumbnailFilePath != null) {
                        val caption = ""
                        compressShorts(
                            path = video.videos!!.videoPath,
                            caption = caption,
                            thumbnailFilePath = thumbnailFilePath.toString(),
                            fileId = video.fileId,
                            feedShortsBusinessId = feedShortsBusinessId
                        )
                    }
                }
            }

            if (itemsToRemove.isNotEmpty()) {
                mixedFiles.removeAll(itemsToRemove)
            }


            if (audioListToUpload.isNotEmpty()) {

                for (audioList in audioListToUpload) {
                    val audioPath = audioList.audios?.audioPath
                    val filename = audioList.audios?.filename
                    val duration = audioList.audios?.duration

                    if (audioPath != null) {
                        val outputFileName = "compressed_audio${System.currentTimeMillis()}.mp3"
                        Log.d(TAG, "uploadMixedFeed: 1")
                        val outputFilePath = File(cacheDir, outputFileName)
                        Log.d(TAG, "uploadMixedFeed: 2")
                        val ffmpegCompressor = AudioCompressorWithProgress()
                        Log.d(TAG, "uploadMixedFeed: 3")
                        val audioDu = reverseFormattedDuration(audioList.audios!!.duration)

                        val audioFile = File(audioPath)
                        val fileSizeInBytes = audioFile.length()
                        val fileSizeInKB = fileSizeInBytes / 1024
                        val fileSizeInMB = fileSizeInKB / 1024

                        if (fileSizeInMB > 2) {
                            // Compress large files
                            val isCompressionSuccessful = ffmpegCompressor.compress(
                                audioPath,
                                outputFilePath.absolutePath,
                                audioDu,
                                this@UploadFeedActivity
                            )

                            if (isCompressionSuccessful) {
                                // Add compressed audio to the list
                                compressedAudioListToUpload.add(
                                    MixedFeedUploadDataClass(
                                        audios = FeedMultipleAudios(
                                            duration = duration!!,
                                            filename = filename!!,
                                            audioPath = outputFilePath.absolutePath
                                        ),
                                        fileTypes = "audio",
                                        fileId = audioList.fileId // IMPORTANT: Include fileId
                                    )
                                )
                            } else {
                                // Compression failed, use original
                                Log.w(TAG, "Audio compression failed for ${filename}, using original")
                                compressedAudioListToUpload.add(
                                    MixedFeedUploadDataClass(
                                        audios = audioList.audios,
                                        fileTypes = "audio",
                                        fileId = audioList.fileId
                                    )
                                )
                            }
                        } else {
                            // File is small enough, use original
                            compressedAudioListToUpload.add(
                                MixedFeedUploadDataClass(
                                    audios = audioList.audios,
                                    fileTypes = "audio",
                                    fileId = audioList.fileId
                                )
                            )
                        }
                    }
                }
            }



            if (compressedAudioListToUpload.isNotEmpty()) {
                for (audios in compressedAudioListToUpload) {
                    mixedFiles.add(audios)
                }
            }


            for (video in mixedFiles) {
                Log.d(TAG, "uploadMixedFeed video thumbnail: ${video.videos?.thumbnail}")
            }


            for (document in mixedFiles) {
                if (document.documents != null) {

                    Log.d(TAG, "uploadMixedFeed document: ${document.documents?.documentType}")
                }
            }

            val uploadDataJson = gson.toJson(mixedFiles)

            val inputData = Data.Builder()
                .putString("upload_data", uploadDataJson)
                .putString(FeedUploadWorker.CAPTION, content)
                .putString(FeedUploadWorker.FEED_SHORTS_BUSINESS_ID, feedShortsBusinessId)
                .putString(FeedUploadWorker.CONTENT_TYPE, "mixed_files")
                .putStringArray(FeedUploadWorker.TAGS, tags.toTypedArray())
                .build()




            try {
                GlobalScope.launch(Dispatchers.IO) {
                    Log.d(TAG, "uploadVideoFeed: step 3")


                    uploadWorkRequest = OneTimeWorkRequestBuilder<FeedUploadWorker>()
                        .setInputData(inputData)
                        .build()


                    val workManager = WorkManager.getInstance(applicationContext)

                    Log.d("Upload", "Enqueuing upload work request...")
                    workManager.enqueue(uploadWorkRequest!!)


                }
            } catch (e: Exception) {
                Log.e(TAG, "uploadVideoFeed: error because ${e.message}")
                e.printStackTrace()
            }
        }


    }

    @SuppressLint("SetTextI18n")
    private fun compressShorts(
        path: String,
        caption: String,
        fileId: String,
        feedShortsBusinessId: String,
        thumbnailFilePath: String
    ) {
        val uploadWorkRequest: OneTimeWorkRequest = OneTimeWorkRequestBuilder<ShortsUploadWorker>()
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
        workManager.enqueue(uploadWorkRequest)
        // Inside compressShorts function, after enqueueing the work request

        lifecycleScope.launch(Dispatchers.Main) {
            Log.d("Progress", "Progress ...scope")


            workManager = WorkManager.getInstance(applicationContext)
            workManager.getWorkInfoByIdLiveData(uploadWorkRequest.id)
                .observe(this@UploadFeedActivity) { workInfo ->
                    Log.d("Progress", "Observer triggered!")
                    if (workInfo != null) {
                        val progress =
                            workInfo.progress.getInt(ShortsUploadWorker.Progress, 0)
                        // Update your UI with the progress value
                        Log.d("Progress", "Progress $progress")
                    } else {
                        Log.d("Progress", "Work info is null")
                    }

                    if (workInfo.state == WorkInfo.State.RUNNING) {
                        // Access progress here
                        Log.d("Progress", "Running")
                    }
                    if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                        // Access progress here
                        Log.d("Progress", "SUCCEEDED")
                    }
                    if (workInfo.state == WorkInfo.State.ENQUEUED) {
                        // Access progress here
                        Log.d("Progress", "ENQUEUED")
                    }
                    if (workInfo.state == WorkInfo.State.BLOCKED) {
                        // Access progress here
                        Log.d("Progress", "BLOCKED")
                    }

                    if (workInfo.state == WorkInfo.State.CANCELLED) {
                        // Access progress here
                        Log.d("Progress", "CANCELLED")
                    }

                }
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

    private val pickMultipleMedia =
        registerForActivityResult(
            ActivityResultContracts.PickMultipleVisualMedia(
                10)) { uris ->
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
                                    Compressor.compress(this@UploadFeedActivity, file)
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
                                        this@UploadFeedActivity,

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

                                this@UploadFeedActivity.fileType = "mixed_files"

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
                                        binding.forUploadingFileThumbNail.visibility = View.VISIBLE
                                        binding.uploadedFileThumbNail.visibility = View.GONE
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
                                binding.forUploadingFileThumbNail.visibility = View.VISIBLE
                                binding.uploadedFileThumbNail.visibility = View.GONE
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
        Log.d(TAG, "inside setFirstFrameAsThumbnail: ")
        try {
            for (videoUri in videoUris) {
                val firstFrame: Bitmap? = VideoUtils.getFirstFrame(this, videoUri)
                if (firstFrame != null) {
                    Log.d(TAG, "setFirstFrameAsThumbnail: thumbnail not null")

                    thumbnails.add(firstFrame)
                } else {
                    Log.d(TAG, "setFirstFrameAsThumbnail: thumbnail is null")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "setFirstFrameAsThumbnail: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun uploadVNAudioFeed(
        filePath: String,
        content: String,
        contentType: String,
        tags: MutableList<String>,
        durationString: String

    ) {
        if (contentType == "vn") {
            lifecycleScope.launch(Dispatchers.IO) {
                Log.d("AudioPicker", "AudioPicker: Compression successful ")

                uploadWorkRequest =
                    OneTimeWorkRequestBuilder<FeedUploadWorker>()
                        .setInputData(
                            Data.Builder()
                                .putString(
                                    FeedUploadWorker.EXTRA_FILE_PATH,
                                    filePath
                                )
                                .putString(FeedUploadWorker.CAPTION, content)
                                .putString(FeedUploadWorker.DURATION, durationString)
                                .putString(FeedUploadWorker.CONTENT_TYPE, contentType)
                                .putStringArray(FeedUploadWorker.TAGS, tags.toTypedArray())
                                .build()
                        )
                        .build()

                val workManager = WorkManager.getInstance(applicationContext)

                Log.d("Upload", "Enqueuing upload work request...")
                workManager.enqueue(uploadWorkRequest!!)

                lifecycleScope.launch(Dispatchers.Main) {
                    Log.d("Progress", "Progress ...scope")
                    val workManager = WorkManager.getInstance(applicationContext)
                    workManager.getWorkInfoByIdLiveData(uploadWorkRequest!!.id)
                        .observe(this@UploadFeedActivity) { workInfo ->
                            Log.d("Progress", "Observer triggered!")
                            if (workInfo != null) {
                                val progress =
                                    workInfo.progress.getInt(FeedUploadWorker.Progress, 0)
                                // Update your UI with the progress value
                                Log.d("Progress", "Progress $progress")
                            } else {
                                Log.d("Progress", "Work info is null")
                            }
                            if (workInfo.state == WorkInfo.State.RUNNING) {
                                // Access progress here
                                Log.d("Progress", "Running")
                            }
                            if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                                // Access progress here
                                Log.d("Progress", "SUCCEEDED")
                            }
                            if (workInfo.state == WorkInfo.State.ENQUEUED) {
                                // Access progress here
                                Log.d("Progress", "ENQUEUED")
                            }
                            if (workInfo.state == WorkInfo.State.BLOCKED) {
                                // Access progress here
                                Log.d("Progress", "BLOCKED")
                            }
                            if (workInfo.state == WorkInfo.State.CANCELLED) {
                                // Access progress here
                                Log.d("Progress", "CANCELLED")
                            }
                        }
                }

            }

        } else {
            Log.d(TAG, "uploadAudioFeed: ")
            val outputFileName =
                "compressed_audio${System.currentTimeMillis()}.mp3" // Example output file name
            val outputFilePath = File(cacheDir, outputFileName)

            lifecycleScope.launch(Dispatchers.IO) {

                try {
                    val ffmpegCompressor = AudioCompressorWithProgress()
                    // Start compression in a coroutine scope
                    val audioDu = reverseFormattedDuration(durationString)
                    val isCompressionSuccessful = ffmpegCompressor.compress(
                        filePath,
                        outputFilePath.absolutePath,
                        audioDu, this@UploadFeedActivity
                    )
                    Log.d(
                        TAG,
                        "uploadAudioFeed: $isCompressionSuccessful outputFilePath.absolutePath ${outputFilePath.absolutePath}"
                    )
                    if (isCompressionSuccessful) {
                        Log.d("AudioPicker", "AudioPicker: Compression successful ")

                        uploadWorkRequest =
                            OneTimeWorkRequestBuilder<FeedUploadWorker>()
                                .setInputData(
                                    Data.Builder()
                                        .putString(
                                            FeedUploadWorker.EXTRA_FILE_PATH,
                                            outputFilePath.absolutePath
                                        )
                                        .putString(FeedUploadWorker.CAPTION, content)
                                        .putString(FeedUploadWorker.DURATION, durationString)
                                        .putString(FeedUploadWorker.CONTENT_TYPE, contentType)
                                        .putStringArray(FeedUploadWorker.TAGS, tags.toTypedArray())
                                        .build()
                                )
                                .build()

                        val workManager = WorkManager.getInstance(applicationContext)

                        Log.d("Upload", "Enqueuing upload work request...")
                        workManager.enqueue(uploadWorkRequest!!)

                        lifecycleScope.launch(Dispatchers.Main) {
                            Log.d("Progress", "Progress ...scope")

                            val workManager = WorkManager.getInstance(applicationContext)
                            workManager.getWorkInfoByIdLiveData(uploadWorkRequest!!.id)
                                .observe(this@UploadFeedActivity) { workInfo ->
                                    Log.d("Progress", "Observer triggered!")
                                    if (workInfo != null) {
                                        val progress =
                                            workInfo.progress.getInt(FeedUploadWorker.Progress, 0)
                                        // Update your UI with the progress value
                                        Log.d("Progress", "Progress $progress")
                                    } else {
                                        Log.d("Progress", "Work info is null")
                                    }

                                    if (workInfo.state == WorkInfo.State.RUNNING) {
                                        // Access progress here
                                        Log.d("Progress", "Running")
                                    }
                                    if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                                        // Access progress here
                                        Log.d("Progress", "SUCCEEDED")
                                    }
                                    if (workInfo.state == WorkInfo.State.ENQUEUED) {
                                        // Access progress here
                                        Log.d("Progress", "ENQUEUED")
                                    }
                                    if (workInfo.state == WorkInfo.State.BLOCKED) {
                                        // Access progress here
                                        Log.d("Progress", "BLOCKED")
                                    }

                                    if (workInfo.state == WorkInfo.State.CANCELLED) {
                                        // Access progress here
                                        Log.d("Progress", "CANCELLED")
                                    }

                                }
                        }
                    }

                } catch (e: Exception) {
                    Log.d(TAG, "uploadAudioFeed: error ${e.message}")
                    e.printStackTrace()
                }

                runBlocking {


                }
            }


        }


        fun uploadAudioFeed(
            audiosList: MutableList<String>,
            content: String,
            contentType: String,
            tags: MutableList<String>,
        ) {
            val TAG = "uploadAudioFeed"
            Log.d(TAG, "uploadAudioFeed: ")


            val compressedAudioPaths: MutableList<String> = mutableListOf()
            lifecycleScope.launch(Dispatchers.IO) {

                val audioListToUpload: MutableList<String> = mutableListOf()
                try {
                    for (audioPath in audiosList) {
                        Log.d(TAG, "uploadAudioFeed audioPath: $audioPath")
                        val outputFileName =
                            "compressed_audio${System.currentTimeMillis()}.mp3" // Example output file name
                        val outputFilePath = File(cacheDir, outputFileName)
                        val ffmpegCompressor = AudioCompressorWithProgress()
                        // Start compression in a coroutine scope
                        Log.d(
                            TAG,
                            "uploadAudioFeed: multipleAudioAdapter.getAudioDuration(audioPath) : ${
                                multipleAudioAdapter.getAudioDuration(audioPath)
                            }"
                        )
                        val audioDu =
                            reverseFormattedDuration(multipleAudioAdapter.getAudioDuration(audioPath))

                        val audioFile = File(audioPath)
                        val fileSizeInBytes = audioFile.length()
                        val fileSizeInKB = fileSizeInBytes / 1024
                        val fileSizeInMB = fileSizeInKB / 1024
                        if (fileSizeInMB > 2) {
                            val isCompressionSuccessful = ffmpegCompressor.compress(
                                audioPath,
                                outputFilePath.absolutePath,
                                audioDu, this@UploadFeedActivity
                            )
                            Log.d(
                                TAG,
                                "uploadAudioFeed outputFileName: ${outputFilePath.absolutePath}"
                            )
                            if (isCompressionSuccessful) {
                                audioListToUpload.add(outputFilePath.absolutePath)
                                Log.d(TAG, "uploadAudioFeed: compression successful")
                            } else {
                                Log.d(TAG, "uploadAudioFeed: compression not successful")
                            }
                        } else {
                            audioListToUpload.add(audioPath)
                        }

                    }
                    Log.d(TAG, "uploadAudioFeed: total files to upload : ${audioListToUpload.size}")
                    for (audio in audioListToUpload) {
                        val audioFile = File(audioPath)
                        val fileSizeInBytes = audioFile.length()
                        val fileSizeInKB = fileSizeInBytes / 1024
                        val fileSizeInMB = fileSizeInKB / 1024
                        Log.d(TAG, "uploadAudioFeed: audio :fileSizeInKB: $fileSizeInKB $audio ")

                    }
                    val audDuration = listToCommaSeparatedString(audioDurationStringList)
                    uploadWorkRequest =
                        OneTimeWorkRequestBuilder<FeedUploadWorker>()
                            .setInputData(
                                Data.Builder()
                                    .putStringArray(
                                        FeedUploadWorker.MULTIPLE_AUDIOS,
                                        audioListToUpload.toTypedArray()
                                    )
                                    .putString(FeedUploadWorker.CAPTION, content)
                                    .putString(FeedUploadWorker.DURATION, audDuration)
                                    .putString(FeedUploadWorker.CONTENT_TYPE, contentType)
                                    .putStringArray(FeedUploadWorker.TAGS, tags.toTypedArray())
                                    .build()
                            )
                            .build()

                    var workManager = WorkManager.getInstance(applicationContext)

                    workManager.enqueue(uploadWorkRequest!!)
                    lifecycleScope.launch(Dispatchers.Main) {
                        Log.d("Progress", "Progress ...scope")

                        workManager = WorkManager.getInstance(applicationContext)
                        workManager.getWorkInfoByIdLiveData(uploadWorkRequest!!.id)
                            .observe(this@UploadFeedActivity) { workInfo ->
                                Log.d("Progress", "Observer triggered!")
                                if (workInfo != null) {
                                    val progress =
                                        workInfo.progress.getInt(FeedUploadWorker.Progress, 0)
                                    // Update your UI with the progress value
                                    Log.d("Progress", "Progress $progress")
                                } else {
                                    Log.d("Progress", "Work info is null")
                                }

                                if (workInfo.state == WorkInfo.State.RUNNING) {
                                    // Access progress here
                                    Log.d("Progress", "Running")
                                }
                                if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                                    // Access progress here
                                    Log.d("Progress", "SUCCEEDED")
                                }
                                if (workInfo.state == WorkInfo.State.ENQUEUED) {
                                    // Access progress here
                                    Log.d("Progress", "ENQUEUED")
                                }
                                if (workInfo.state == WorkInfo.State.BLOCKED) {
                                    // Access progress here
                                    Log.d("Progress", "BLOCKED")
                                }

                                if (workInfo.state == WorkInfo.State.CANCELLED) {
                                    // Access progress here
                                    Log.d("Progress", "CANCELLED")
                                }

                            }
                    }

                } catch (e: Exception) {
                    Log.d(TAG, "uploadAudioFeed: error ${e.message}")
                    e.printStackTrace()
                }

                runBlocking {


                }
            }


        }




        @OptIn(DelicateCoroutinesApi::class)
        fun uploadVideoFeed(
            filePath: MutableList<String>,
            content: String,
            tags: MutableList<String>,
            durationString: String
        ) {
            val TAG = "uploadVideoFeed"

            Log.d(
                TAG,
                "uploadVideoFeed: file path size ${filePath.size} duration String $durationString"
            )
            setFirstFrameAsThumbnail()
            Log.d(TAG, "uploadVideoFeed: step 1")
            val thumbnailFilePaths: ArrayList<String> = arrayListOf()
            for (i in thumbnails) {
                Log.i(TAG, "uploadVideoFeed: thumbnails: $i")
                val thumbnailFile =
                    saveBitmapToFile(i, applicationContext, generateRandomFileName())
                val thumbnailFilePath = thumbnailFile.absolutePath
                thumbnailFilePaths.add(thumbnailFilePath)
            }
            Log.d(TAG, "uploadVideoFeed: step 2")


            try {
                GlobalScope.launch(Dispatchers.IO) {
                    Log.d(TAG, "uploadVideoFeed: step 3")

                    Log.d(TAG, "uploadVideoFeed: thumbnailFilePath $thumbnailFilePaths")
                    uploadWorkRequest =
                        OneTimeWorkRequestBuilder<FeedUploadWorker>()
                            .setInputData(
                                Data.Builder()
                                    .putStringArray(
                                        FeedUploadWorker.MULTIPLE_VIDEOS,
                                        filePath.toTypedArray()
                                    )
                                    .putString(FeedUploadWorker.CAPTION, content)
                                    .putString(FeedUploadWorker.DURATION, durationString)
                                    .putString(FeedUploadWorker.CONTENT_TYPE, "video")
                                    .putStringArray(FeedUploadWorker.TAGS, tags.toTypedArray())
                                    .putStringArray(
                                        FeedUploadWorker.MULTIPLE_THUMBNAILS,
                                        thumbnailFilePaths.toTypedArray()
                                    )
                                    .build()
                            )
                            .build()

                    val workManager = WorkManager.getInstance(applicationContext)

                    Log.d("Upload", "Enqueuing upload work request...")
                    workManager.enqueue(uploadWorkRequest!!)

                    lifecycleScope.launch(Dispatchers.Main) {
                        Log.d("Progress", "Progress ...scope")

                        val workManager = WorkManager.getInstance(applicationContext)
                        workManager.getWorkInfoByIdLiveData(uploadWorkRequest!!.id)
                            .observe(this@UploadFeedActivity) { workInfo ->
                                Log.d("Progress", "Observer triggered!")
                                if (workInfo != null) {
                                    val progress =
                                        workInfo.progress.getInt(FeedUploadWorker.Progress, 0)
                                    // Update your UI with the progress value
                                    Log.d("Progress", "Progress $progress")
                                } else {
                                    Log.d("Progress", "Work info is null")
                                }

                                if (workInfo.state == WorkInfo.State.RUNNING) {
                                    // Access progress here
                                    Log.d("Progress", "Running")
                                }
                                if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                                    // Access progress here
                                    Log.d("Progress", "SUCCEEDED")
                                }
                                if (workInfo.state == WorkInfo.State.ENQUEUED) {
                                    // Access progress here
                                    Log.d("Progress", "ENQUEUED")
                                }
                                if (workInfo.state == WorkInfo.State.BLOCKED) {
                                    // Access progress here
                                    Log.d("Progress", "BLOCKED")
                                }

                                if (workInfo.state == WorkInfo.State.CANCELLED) {
                                    // Access progress here
                                    Log.d("Progress", "CANCELLED")
                                }

                            }
                    }

                }
            } catch (e: Exception) {
                Log.e(TAG, "uploadVideoFeed: error because ${e.message}")
                e.printStackTrace()
            }


            Log.d(TAG, "uploadVideoFeed: step 4")
        }



        fun uploadImageFeed(filePath: String, content: String, tags: MutableList<String>) {
            uploadWorkRequest =
                OneTimeWorkRequestBuilder<FeedUploadWorker>()
                    .setInputData(
                        Data.Builder()
                            .putString(FeedUploadWorker.EXTRA_FILE_PATH, filePath)
                            .putString(FeedUploadWorker.CAPTION, content)
                            .putString(FeedUploadWorker.CONTENT_TYPE, "image")
                            .putString(FeedUploadWorker.DURATION, "")
                            .putStringArray(FeedUploadWorker.TAGS, tags.toTypedArray())
                            .build()
                    )
                    .build()

            val workManager = WorkManager.getInstance(applicationContext)

            Log.d("Upload", "Enqueuing upload work request...")
            workManager.enqueue(uploadWorkRequest!!)

            lifecycleScope.launch(Dispatchers.Main) {
                Log.d("Progress", "Progress ...scope")

                val workManager = WorkManager.getInstance(applicationContext)
                workManager.getWorkInfoByIdLiveData(uploadWorkRequest!!.id)
                    .observe(this@UploadFeedActivity) { workInfo ->
                        Log.d("Progress", "Observer triggered!")
                        if (workInfo != null) {
                            val progress =
                                workInfo.progress.getInt(FeedUploadWorker.Progress, 0)
                            // Update your UI with the progress value
                            Log.d("Progress", "Progress $progress")
                        } else {
                            Log.d("Progress", "Work info is null")
                        }

                        if (workInfo.state == WorkInfo.State.RUNNING) {
                            // Access progress here
                            Log.d("Progress", "Running")
                        }
                        if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                            // Access progress here
                            Log.d("Progress", "SUCCEEDED")
                        }
                        if (workInfo.state == WorkInfo.State.ENQUEUED) {
                            // Access progress here
                            Log.d("Progress", "ENQUEUED")
                        }
                        if (workInfo.state == WorkInfo.State.BLOCKED) {
                            // Access progress here
                            Log.d("Progress", "BLOCKED")
                        }

                        if (workInfo.state == WorkInfo.State.CANCELLED) {
                            // Access progress here
                            Log.d("Progress", "CANCELLED")
                        }

                    }
            }

        }

        fun uploadMultipleImageFeed(
            compressedImageFiles: MutableList<File>,
            content: String,
            tags: MutableList<String>
        ) {
            Log.d(
                TAG,
                "uploadMultipleImageFeed: 'upload multiple files size: ${compressedImageFiles.size}"
            )
// Assuming compressedImageFiles is your list of compressed files
            val multipleImagesList: MutableList<String> = mutableListOf()
            for (compressedFile in compressedImageFiles) {
                val compressedFilePath = compressedFile.absolutePath
                multipleImagesList.add(compressedFilePath)
                Log.d("CompressedFilePath", "Compressed file path: $compressedFilePath")
                // Use compressedFilePath as needed, e.g., to display, upload, etc.
            }

            uploadWorkRequest =
                OneTimeWorkRequestBuilder<FeedUploadWorker>()
                    .setInputData(
                        Data.Builder()
                            .putStringArray(
                                FeedUploadWorker.MULTIPLE_IMAGES,
                                multipleImagesList.toTypedArray()
                            )
                            .putString(FeedUploadWorker.CAPTION, content)
                            .putString(FeedUploadWorker.CONTENT_TYPE, "multiple_images")
                            .putString(FeedUploadWorker.DURATION, "")
                            .putStringArray(FeedUploadWorker.TAGS, tags.toTypedArray())
                            .build()
                    )
                    .build()

            val workManager = WorkManager.getInstance(applicationContext)

            Log.d("Upload", "Enqueuing upload work request...")
            workManager.enqueue(uploadWorkRequest!!)

            lifecycleScope.launch(Dispatchers.Main) {
                Log.d("Progress", "Progress ...scope")

                val workManager = WorkManager.getInstance(applicationContext)
                workManager.getWorkInfoByIdLiveData(uploadWorkRequest!!.id)
                    .observe(this@UploadFeedActivity) { workInfo ->
                        Log.d("Progress", "Observer triggered!")
                        if (workInfo != null) {
                            val progress =
                                workInfo.progress.getInt(FeedUploadWorker.Progress, 0)
                            // Update your UI with the progress value
                            Log.d("Progress", "Progress $progress")
                        } else {
                            Log.d("Progress", "Work info is null")
                        }

                        if (workInfo.state == WorkInfo.State.RUNNING) {
                            // Access progress here
                            Log.d("Progress", "Running")
                        }
                        if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                            // Access progress here
                            Log.d("Progress", "SUCCEEDED")
                        }
                        if (workInfo.state == WorkInfo.State.ENQUEUED) {
                            // Access progress here
                            Log.d("Progress", "ENQUEUED")
                        }
                        if (workInfo.state == WorkInfo.State.BLOCKED) {
                            // Access progress here
                            Log.d("Progress", "BLOCKED")
                        }

                        if (workInfo.state == WorkInfo.State.CANCELLED) {
                            // Access progress here
                            Log.d("Progress", "CANCELLED")
                        }

                    }
            }

        }

        suspend fun extractThumbnailsFromVideos(context: Context, videoUri: Uri): List<Bitmap> {
            val thumbnails = mutableListOf<Bitmap>()
            val retriever = MediaMetadataRetriever()

            try {
                retriever.setDataSource(context, videoUri)

                val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val duration = durationStr?.toLongOrNull() ?: 0L

                // Extract 3 thumbnails evenly spaced
                val frameTimes = listOf(0L, duration / 2, duration - 1000)

                for (time in frameTimes) {
                    val bitmap = retriever.getFrameAtTime(
                        time * 1000, MediaMetadataRetriever.OPTION_CLOSEST)
                    if (bitmap != null) {
                        thumbnails.add(bitmap)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                retriever.release()
            }

            return thumbnails
        }


        fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<out String>, grantResults: IntArray
        ) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (requestCode == REQUEST_CODE) {
                permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
            }

        }


        fun saveBitmapToFile(bitmap: Bitmap, context: Context): File {
            val fileDir = File(context.filesDir, "thumbnails")
            if (!fileDir.exists()) {
                fileDir.mkdirs()
            }
            val fileName = "thumbnail.png"

            val file = File(fileDir, fileName)
            try {
                val stream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                stream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return file
        }

        fun saveBitmapToFile(bitmap: Bitmap, context: Context, fileName: String): File {
            // Create a directory for the thumbnails if it doesn't already exist
            val fileDir = File(context.filesDir, "thumbnails")
            if (!fileDir.exists()) {
                fileDir.mkdirs()
            }

            // Create a File object with the specified file name
            val file = File(fileDir, fileName)

            try {
                // Write the bitmap to the file
                val stream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                stream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return file
        }

        suspend fun extractThumbnail(videoUrl: Uri): List<Bitmap>? {
            return try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(this, videoUrl)

                // Get the duration of the video in milliseconds
                val durationMs =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        ?.toLong()
                        ?: 0
                // Set the frame interval to 1000ms (1 second)
                val frameIntervalMs = 1000L
                val thumbnails = mutableListOf<Bitmap>()

                // Iterate through each second and retrieve the frame
                for (timeMs in 0 until durationMs step frameIntervalMs) {
                    val bitmap: Bitmap? = retriever.getFrameAtTime(
                        timeMs * 1000,
                        MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                    )
                    bitmap?.let { thumbnails.add(it) }
                }
                // Release the MediaMetadataRetriever
                retriever.release()
                thumbnails
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        fun loadBitmapFromUri(uri: Uri): Bitmap {
            return if (Build.VERSION.SDK_INT < 28) {
                // For versions before Android 9 (API level 28)
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            } else {
                // For Android 9 (API level 28) and above
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            }
        }


        @SuppressLint("SetTextI18n")
        fun handleDocumentUriToUpload(uri: Uri) {
            // Handle the selected document URI here
            // For example, you can retrieve the file name
            documentUriListToUpload.add(uri.toString())
            contentResolver.query(uri, null, null,
                null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                cursor.moveToFirst()
                val fileName = cursor.getString(nameIndex)
                val fileSize = cursor.getLong(sizeIndex)

                var numberOfPages = 0
                val formattedFileSize = formatFileSize(fileSize)
                fileType = "mixed_files"

                val fileSizes = isFileSizeGreaterThan2MB(fileSize)
                val documentType = fileType(fileName)
                Log.d("handleDocumentUri", ": $fileName")
                Log.d("handleDocumentUri", "uri $uri")
                Log.d("handleDocumentUri", "formattedFileSize $formattedFileSize")

                binding.recyclerView2.visibility = View.INVISIBLE

                numberOfPages = when (documentType) {
                    "doc" -> {
                        getNumberOfPagesFromUriForDoc(this, uri)
                    }

                    "docx", "xlsx", "pptx" -> {
                        getNumberOfPagesFromUriForDocx(this, uri)
                    }

                    else -> {
                        getNumberOfPagesFromUriForPDF(this, uri)
                    }
                }


                documentFileNamesToUpload.add(fileName)
                documentNumberOfPagesToUpload.add(numberOfPages.toString())
                documentTypesToUpload.add(documentType)
                if (documentType == "pdf") {
                    retrieveFirstPageAndSaveAsImage(this, uri)
                }

            }
        }

        fun handleDocumentUriToUploadReturn(uri: Uri): FeedMultipleDocumentsDataClass {
            // Handle the selected document URI here
            // For example, you can retrieve the file name
            documentUriListToUpload.add(uri.toString())
            var numberOfPages = 0
            var formattedFileSize: String = ""
            var documentType: String = ""
            var fileName: String = ""
            var bitmapDocument: Bitmap? = null
            var pdfFile: File? = null
            var pdfFilePath = ""
            contentResolver.query(uri, null, null,
                null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                cursor.moveToFirst()
                fileName = cursor.getString(nameIndex)
                val fileSize = cursor.getLong(sizeIndex)


                formattedFileSize = formatFileSize(fileSize)
                fileType = "mixed_files"

                val fileSizes = isFileSizeGreaterThan2MB(fileSize)
                documentType = fileType(fileName)
                pdfFile = uriToFile2(this, uri, documentType)
                if (documentType == "pdf") {
                    lifecycleScope.launch(Dispatchers.IO) {
                        bitmapDocument = retrieveFirstPageAsBitmap(this@UploadFeedActivity, uri)
                    }


                }

                if (pdfFile != null) {
                    pdfFilePath = pdfFile!!.absolutePath
                }
                Log.d("handleDocumentUri", ": $fileName")
                Log.d("handleDocumentUri", "uri $uri")
                Log.d("handleDocumentUri", "formattedFileSize $formattedFileSize")

                binding.recyclerView2.visibility = View.INVISIBLE

                numberOfPages = when (documentType) {
                    "doc" -> {
                        getNumberOfPagesFromUriForDoc(this, uri)
                    }

                    "docx", "xlsx", "pptx" -> {
                        getNumberOfPagesFromUriForDocx(this, uri)
                    }

                    else -> {
                        getNumberOfPagesFromUriForPDF(this, uri)
                    }
                }


                documentFileNamesToUpload.add(fileName)
                documentNumberOfPagesToUpload.add(numberOfPages.toString())
                documentTypesToUpload.add(documentType)


            }
            if (bitmapDocument != null) {
                return FeedMultipleDocumentsDataClass(
                    uri = uri,
                    filename = fileName,
                    numberOfPages = numberOfPages.toString(),
                    documentType = documentType,
                    fileSize = formattedFileSize,
                    documentThumbnailFilePath = bitmapDocument,
                    pdfFilePath = pdfFilePath
                )
            } else {
                val drawable = ContextCompat.getDrawable(this, R.drawable.documents)

                val thumbnailWidth = 100 // Set your desired thumbnail width
                val thumbnailHeight = 100 // Set your desired thumbnail height
                val thumbnail = drawable?.let {
                    ThumbnailUtil.drawableToThumbnail(this, it, thumbnailWidth, thumbnailHeight)
                }

                return FeedMultipleDocumentsDataClass(
                    uri = uri,
                    filename = fileName,
                    numberOfPages = numberOfPages.toString(),
                    documentType = documentType,
                    fileSize = formattedFileSize,
                    documentThumbnailFilePath = thumbnail,
                    pdfFilePath = pdfFilePath
                )
            }

        }

        @SuppressLint("SetTextI18n")
        fun handleDocumentUri(uri: Uri) {
            // Handle the selected document URI here
            // For example, you can retrieve the file name
            contentResolver.query(uri, null, null,
                null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                cursor.moveToFirst()
                val fileName = cursor.getString(nameIndex)
                val fileSize = cursor.getLong(sizeIndex)

                binding.uploadedFileThumbNail.setImageResource(R.drawable.documents)

                var numberOfPages = 0
                val formattedFileSize = formatFileSize(fileSize)
                fileType = "mixed_files"

                val fileSizes = isFileSizeGreaterThan2MB(fileSize)
                val documentType = fileType(fileName)
                Log.d("handleDocumentUri", ": $fileName")
                Log.d("handleDocumentUri", "uri $uri")
                Log.d("handleDocumentUri", "formattedFileSize $formattedFileSize")

                binding.recyclerView2.visibility = View.INVISIBLE

                numberOfPages = when (documentType) {
                    "doc" -> {
                        getNumberOfPagesFromUriForDoc(this, uri)
                    }

                    "docx", "xlsx", "pptx" -> {
                        getNumberOfPagesFromUriForDocx(this, uri)
                    }

                    else -> {
                        getNumberOfPagesFromUriForPDF(this, uri)
                    }
                }

                binding.uploadedFileThumbNail.setPadding(0)
                binding.uploadedFileThumbNail.colorFilter = null
                feedUploadViewModel.setText(
                    "File name: $fileName \nFile size: $formattedFileSize \nDocument Type: $documentType \n$numberOfPages pages")

                this.numberOfPages = numberOfPages.toString()
                this.fileName = fileName
                this.docType = documentType
                this.docFilePath = uri.toString()

            }
        }

        fun retrieveFirstPageAsBitmap(context: Context, uri: Uri): Bitmap? {
            val contentResolver = context.contentResolver

            Log.i(TAG, "retrieveFirstPageAsBitmap: converting to bitmap")
            return try {
                // Open a ParcelFileDescriptor from the URI
                val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")

                parcelFileDescriptor?.use { pfd ->
                    // Create a PdfRenderer from the ParcelFileDescriptor
                    val pdfRenderer = PdfRenderer(pfd)

                    // Open the first page
                    val page = pdfRenderer.openPage(0)

                    // Create a bitmap of the page
                    val bitmap =
                        Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)

                    // Render the page content into the bitmap
                    page.render(bitmap, null,
                        null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                    // Close the page and the PdfRenderer
                    page.close()
                    pdfRenderer.close()

                    val bitmapFilePath = saveBitmapToCache2(context, bitmap)
                    bitmap
                }
            } catch (e: Exception) {
                Log.e(TAG, "retrieveFirstPageAsBitmap: error retrieving bitmap")
                e.printStackTrace()
                null
            }
        }

        fun retrieveFirstPageAndSaveAsImage(context: Context, uri: Uri) {
            val contentResolver = context.contentResolver

            Log.i(TAG, "retrieveFirstPageAndSaveAsImage: save to bitmap")
            try {
                // Open a ParcelFileDescriptor from the URI
                val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")

                parcelFileDescriptor?.use { pfd ->
                    // Create a PdfRenderer from the ParcelFileDescriptor
                    val pdfRenderer = PdfRenderer(pfd)

                    // Open the first page
                    val page = pdfRenderer.openPage(0)

                    // Create a bitmap of the page
                    val bitmap =
                        Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)

                    // Render the page content into the bitmap
                    page.render(bitmap, null,
                        null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                    // Close the page and the PdfRenderer
                    page.close()
                    pdfRenderer.close()

                    // Save bitmap to cache directory
                    saveBitmapToCache(context, bitmap)
                }
            } catch (e: Exception) {
                Log.e(TAG, "retrieveFirstPageAndSaveAsImage: not saved to bitmap")
                e.printStackTrace()
            }
        }

        // Function to save bitmap to cache directory
        fun saveBitmapToCache(context: Context, bitmap: Bitmap) {
            val cacheDir = context.cacheDir // Get the cache directory


            val file = File(cacheDir, generateRandomFileName())

            try {
                // Write the bitmap data to the file
                FileOutputStream(file).use { fos ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                }
                // Bitmap saved successfully
                Log.d(TAG, "Bitmap saved to cache directory: ${file.absolutePath}")
                documentThumbnailsToUpload.add(file.absolutePath)
            } catch (e: IOException) {
                Log.e(TAG, "saveBitmapToCache: not saved to bitmap")
                e.printStackTrace()
            }
        }

        fun saveBitmapToCache2(context: Context, bitmap: Bitmap): String {
            val cacheDir = context.cacheDir // Get the cache directory

            // Create a file in the cache directory

            val file = File(cacheDir, generateRandomFileName())

            try {
                // Write the bitmap data to the file
                FileOutputStream(file).use { fos ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                }
                // Bitmap saved successfully
                Log.d(TAG, "Bitmap saved to cache directory: ${file.absolutePath}")
                return file.absolutePath

            } catch (e: IOException) {
                Log.e(TAG, "saveBitmapToCache: not saved to bitmap")
                e.printStackTrace()
                return ""
            }
        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)

        fun showAttachmentDialog() {
            val dialog = BottomSheetDialog(this)

            dialog.setContentView(R.layout.shorts_and_all_feed_file_upload_bottom_dialog)

            val video = dialog.findViewById<LinearLayout>(R.id.upload_video)
            val audio = dialog.findViewById<LinearLayout>(R.id.upload_audio)
            val image = dialog.findViewById<LinearLayout>(R.id.upload_image)
            val camera = dialog.findViewById<LinearLayout>(R.id.open_camera)
            val doc = dialog.findViewById<LinearLayout>(R.id.upload_document)
            val location = dialog.findViewById<LinearLayout>(R.id.share_location)
            val vnRecord = dialog.findViewById<LinearLayout>(R.id.vnRecord)
            // Apply animation to the dialog's view
            val dialogView =
                dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            dialogView?.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.slide_up))


            val selectableItemBackground = TypedValue()
            image?.context?.theme?.resolveAttribute(
                android.R.attr.selectableItemBackground, selectableItemBackground, true
            )
            image?.setBackgroundResource(selectableItemBackground.resourceId)


            video?.context?.theme?.resolveAttribute(
                android.R.attr.selectableItemBackground,
                selectableItemBackground, true
            )
            video?.setBackgroundResource(selectableItemBackground.resourceId)


            audio?.context?.theme?.resolveAttribute(
                android.R.attr.selectableItemBackground,
                selectableItemBackground, true
            )
            audio?.setBackgroundResource(selectableItemBackground.resourceId)


            camera?.context?.theme?.resolveAttribute(
                android.R.attr.selectableItemBackground,
                selectableItemBackground, true
            )
            camera?.setBackgroundResource(selectableItemBackground.resourceId)


            doc?.context?.theme?.resolveAttribute(
                android.R.attr.selectableItemBackground,
                selectableItemBackground, true
            )
            doc?.setBackgroundResource(selectableItemBackground.resourceId)

            location?.context?.theme?.resolveAttribute(
                android.R.attr.selectableItemBackground,
                selectableItemBackground, true
            )
            location?.setBackgroundResource(selectableItemBackground.resourceId)

            vnRecord?.context?.theme?.resolveAttribute(
                android.R.attr.selectableItemBackground,
                selectableItemBackground, true
            )
            vnRecord?.setBackgroundResource(selectableItemBackground.resourceId)

            image?.setOnClickListener {
                Log.d("SelectImage", "Image selector button clicked")

                pickMultipleMedia.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly))
                dialog.dismiss()

            }

            video?.setOnClickListener {
//            val intent = Intent(this@ChatActivity, DisplayVideosActivity::class.java)
//            val intent = Intent(this@UploadFeeedActivity, VideosActivity::class.java)
                val intent = Intent(this@UploadFeedActivity,
                    FeedSelectVideoActivity::class.java)
//            dialog.dismiss()
                videoPickerLauncher.launch(intent)
                dialog.dismiss()

            }

            audio?.setOnClickListener {
                val intent = Intent(this@UploadFeedActivity,
                    FeedAudioActivity::class.java)
                dialog.dismiss()
                audioPickerLauncher.launch(intent)

            }

            doc?.setOnClickListener {
                openFilePicker("image/*")
                dialog.dismiss()
            }
            camera?.setOnClickListener {
                val intent = Intent(this@UploadFeedActivity,
                    CameraActivity::class.java)
                cameraLauncher.launch(intent)
                dialog.dismiss()
            }

            location?.visibility = View.GONE
            vnRecord?.visibility = View.INVISIBLE
            vnRecord?.setOnClickListener {

                dialog.dismiss()
            }
            location?.setOnClickListener {
            }
            dialog.show()
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

        fun checkPermissionAndSelectFiles() {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
             ///   selectFiles()
            } else {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        fun selectFiles() {
            filePickerLauncher.launch(arrayOf("*/*"))
        }

        fun getNumberOfPagesFromUriForPDF(context: Context, uri: Uri): Int {
            var inputStream: InputStream? = null
            var numberOfPages = 0
            try {
                inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val document = PDDocument.load(inputStream)
                    numberOfPages = document.numberOfPages
                    document.close()
                }
            } catch (e: Exception) {
                // Handle exceptions
                Log.e("getNumberOfPagesFromUri", "getNumberOfPagesFromUri ex $e")
                e.printStackTrace()
            } finally {
                inputStream?.close()
            }
            return numberOfPages
        }

        fun getNumberOfPagesFromUriForDoc(uri: Uri): Int {
            var numberOfPages = 0
            val inputStream: InputStream = contentResolver.openInputStream(uri) ?: return 0
            val hwpfDocument = HWPFDocument(inputStream)
            val range = hwpfDocument.range

            // Count the paragraphs within the range
            val paragraphs = Range(range.startOffset,
                range.endOffset, hwpfDocument).numParagraphs()
            numberOfPages = paragraphs

            hwpfDocument.close()
            inputStream.close()

            return numberOfPages

        }

        fun getNumberOfPagesFromUriForDocx(uri: Uri): Int {
            var numberOfPages = 0
            val inputStream: InputStream = contentResolver.openInputStream(uri) ?: return 0
            val xwpfDocument = XWPFDocument(inputStream)

            // Count the paragraphs or sections in the document
            numberOfPages = xwpfDocument.paragraphs.size

            xwpfDocument.close()
            inputStream.close()

            return numberOfPages

        }


        var caption = ""

        @Deprecated("Deprecated in Java")
        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)

            if (requestCode == UploadShortsActivity.REQUEST_TOPICS_ACTIVITY) {
                if (resultCode == RESULT_OK) {

                    val selectedSubtopics = data?.getStringArrayListExtra("selectedSubtopics")

                    val formattedSubtopics = selectedSubtopics?.joinToString(" ") { "#$it" }
                    // Get the current text from the EditText
                    val currentText = binding.editTextText.text?.toString() ?: ""


                    selectedSubtopics?.let { subtopics ->
                        for (subtopic in subtopics) {
                            tags.add(subtopic)
                        }
                    }


                    val finalText = feedRemoveTextStartingWithHash(currentText)


                    text = finalText

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
            } else if (requestCode == 120 && resultCode == RESULT_OK) {
                setAddMoreFeedVisible()
                if (!addMoreFeedFiles) {
                    documentsList.clear()
                }
                val feedMultipleDocuments: MutableList<FeedMultipleDocumentsDataClass> =
                    mutableListOf()
                data?.let { dataZ ->
                    if (dataZ.clipData != null) {
                        // Multiple files selected
                        val count = dataZ.clipData!!.itemCount
                        for (i in 0 until count) {
                            val uri = dataZ.clipData!!.getItemAt(i).uri
                            Log.d("FilePicker", "Selected URI: $uri")
                            // Perform operations with each URI
                            val documentData = handleDocumentUriToUploadReturn(uri)
                            documentsList.add(documentData)

                            feedMultipleDocuments.add(handleDocumentUriToUploadReturn(uri))
                        }
                    } else {
                        // Single file selected
                        val uri = data.data
                        if (uri != null) {
                            Log.d("FilePicker", "Selected URI: $uri")
                            // Perform operations with the single URI
                            val documentData = handleDocumentUriToUploadReturn(uri)
                            documentsList.add(documentData)

                            feedMultipleDocuments.add(handleDocumentUriToUploadReturn(uri))
                        }
                    }
                }

                if (addMoreFeedFiles) {

                    Log.d(TAG, "step 1: ")
                    multipleFeedFilesPagerAdapter = MultipleFeedFilesPagerAdapter(
                        this,

                        isFullScreen = true
                    )
                    Log.d(TAG, "step 2: ")
                    binding.viewPager.adapter = multipleFeedFilesPagerAdapter
                    for (doc in feedMultipleDocuments) {
                        Log.d("addMoreFeedFiles", "onCreate: audio to upload $doc.")
                        feedUploadViewModel.addMixedFeedUploadDataClass(
                            MixedFeedUploadDataClass(

                                documents = doc, fileTypes = doc.documentType
                            )
                        )
                    }
                    Log.d(TAG, "step 3: ")
                    val mixedFeedFiles = feedUploadViewModel.getMixedFeedUploadDataClass()
                    Log.d(TAG, "step 4: ")
                    multipleFeedFilesPagerAdapter?.setMixedFeedUploadDataClass(
                        mixedFeedFiles
                    )
                    Log.d(TAG, "step 5: ")

                } else {
                    // Create a separate URI list for the adapter
                    val documentUris = documentsList.map { it.uri }.filterNotNull().toMutableList()

//                    multipleDocsAdapter = MultipleFeedDocAdapter(
//                        this,
//                        documentUris,
//                        this)

                    binding.viewPager.adapter = multipleDocsAdapter
                }


                binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
                binding.viewPager.registerOnPageChangeCallback(object :
                    ViewPager2.OnPageChangeCallback() {
                    override fun onPageScrolled(
                        position: Int,
                        positionOffset: Float,
                        positionOffsetPixels: Int
                    ) {
                        // This method will be invoked when the ViewPager2 is scrolled, but not necessarily settled (user is still swiping)
                    }

                    @SuppressLint("SetTextI18n")
                    override fun onPageSelected(position: Int) {
                        // This method will be invoked when a new page becomes selected.
                        // You can perform actions here based on the selected page position.
                        Log.d("ViewPager2", "Page selected: $position")
                        if (!addMoreFeedFiles) {
                            val documentUri = multipleDocsAdapter.getDocumentUri(position)
                            Log.d("ViewPager2", "Page selected documentUri: $documentUri")

                            lifecycleScope.launch {
                                delay(500)
                                handleDocumentUri(documentUri)
                            }
                        } else {
                            Log.d(TAG, "onPageSelected: ")

                            val documentDetails =
                                multipleFeedFilesPagerAdapter?.getDocumentDetails(position)
                            Log.d(
                                "ViewPager2",
                                "onPageSelected: get document details $documentDetails"
                            )
                            if (documentDetails == null) {
//
                            } else {
                                binding.selectCoverText.visibility = View.VISIBLE
                            }
                            val handler = Handler(Looper.getMainLooper())
                            handler.postDelayed({
                                if (documentDetails != null) {
                                    binding.uploadedFileThumbNail.setPadding(0)
                                    binding.uploadedFileThumbNail.colorFilter = null
                                    binding.selectCoverText.text =
                                        "File name: ${documentDetails.filename} \nFile size: ${documentDetails.fileSize} \nDocument Type: ${documentDetails.documentType} \n${documentDetails.numberOfPages} pages"
//                                    feedUploadViewModel.setText("File name: ${documentDetails.filename} \nFile size: ${documentDetails.fileSize} \nDocument Type: ${documentDetails.documentType} \n${documentDetails.numberOfPages} pages")
                                }
                            }, 500)
                        }


                    }

                    override fun onPageScrollStateChanged(state: Int) {
                        // Called when the scroll state changes:
                        // SCROLL_STATE_IDLE, SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING
                        when (state) {
                            ViewPager2.SCROLL_STATE_IDLE -> {
                                // The pager is in an idle, settled state.
                                Log.d("ViewPager2", "Page selected: SCROLL_STATE_IDLE")
                            }

                            ViewPager2.SCROLL_STATE_DRAGGING -> {
                                // The user is dragging the pager.
                                Log.d("ViewPager2", "Page selected: SCROLL_STATE_DRAGGING")
                            }

                            ViewPager2.SCROLL_STATE_SETTLING -> {
                                // The pager is settling to a final position.
                                Log.d("ViewPager2", "Page selected: SCROLL_STATE_SETTLING")
                            }
                        }
                    }
                })
                // Setup CircleIndicator for ViewPager2
                val indicator = findViewById<CircleIndicator3>(R.id.circleIndicator)
                indicator.setViewPager(binding.viewPager)

                // Ensure visibility settings are correct
                binding.recyclerView2.visibility = View.INVISIBLE
                binding.forUploadingFileThumbNail.visibility = View.VISIBLE
                binding.uploadedFileThumbNail.visibility = View.GONE
                binding.selectCoverText.visibility = View.VISIBLE
            }
        }

        fun cancelShortsUpload() {
            binding.cancelButton.setOnClickListener {
                finish()
            }
        }

//        fun backFromShortsUpload() {
//            binding.backButton.setOnClickListener {
//                finish()
//            }
//        }


        fun onDestroy() {

            super.onDestroy()

        }

        @SuppressLint("SetTextI18n")
        fun compressFeedVideo(
            onSuccess: (String) -> Unit,
            onCompletion: () -> Unit
        ) {

            val uniqueId = UniqueIdGenerator.generateUniqueId()
            Log.d("progress id", uniqueId)
            Log.d("toCompressUris", "toCompressUris $toCompressUris")

            lifecycleScope.launch {
                VideoCompressor.start(
                    context = applicationContext,
                    toCompressUris,
                    isStreamable = true,
                    sharedStorageConfiguration = SharedStorageConfiguration(
                        saveAt = SaveLocation.movies,
                        subFolderName = "flash_feed_video_compresses"
                    ),

                    configureWith = Configuration(
                        quality = VideoQuality.MEDIUM,

                        videoNames = toCompressUris.map { uri -> uri.pathSegments.last() },

                        isMinBitrateCheckEnabled = false,
                    ),

                    listener = object : CompressionListener {
                        override fun onProgress(index: Int, percent: Float) {

                            //Update UI
                            if (percent <= 100) {
                                Log.d("Compress", "Progress: $percent")
                                EventBus.getDefault()
                                    .post(ProgressEvent("uniqueIdVideo", percent.toInt()))

                            }
                        }

                        override fun onStart(index: Int) {
                        }

                        override fun onSuccess(index: Int, size: Long, path: String?) {
                            Log.d(
                                "compressFeedVideo",
                                "compressFeedVideo file size: ${getFileSize(size)}"
                            )
                            Log.d("compressFeedVideo", "compressFeedVideo path: $path")

                            if (path != null) {
                                onSuccess(path)
                            }
                            toCompressUris.clear()

                            onCompletion()
                        }

                        override fun onFailure(index: Int, failureMessage: String) {
                            Log.wtf("Compress", failureMessage)
                        }

                        override fun onCancelled(index: Int) {
                            Log.wtf("Compress", "compression has been cancelled")
                            // make UI changes, cleanup, etc
                        }

                    },

                    )
            }
        }


        fun onProgress(progress: Int) {
            Log.d(TAG, "Progress: $progress%")
            EventBus.getDefault().post(ProgressEvent("uniqueIdAudio", progress))
        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        fun onImageClick() {
            showAttachmentDialog()
        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        fun onAudioClick() {
            showAttachmentDialog()
        }

        @SuppressLint("SetTextI18n")
        fun onAudioDisplay(details: MultipleAudios) {
            Log.d(TAG, "onAudioDisplay details: $details")

        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        fun onVideoClick() {
            showAttachmentDialog()

        }

        fun setupRecyclerView(
            videoThumbnails: List<Bitmap>,
            videoDetails: FeedMultipleVideos
        ) {
            val layoutManager = LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false)


            val adapter = FeedVideoThumbnailAdapter(videoThumbnails, this)
            adapter.setVideoDetails(videoDetails)
            binding.recyclerView2.visibility = View.VISIBLE
            binding.recyclerView2.layoutManager = layoutManager
            binding.recyclerView2.adapter = adapter
        }

        fun getAllVideos(
            videosList: MutableList<MixedFeedUploadDataClass>):
                ArrayList<FeedMultipleVideos> {

            return videosList
                .mapNotNull { it.videos } // Filter out null values and map to FeedMultipleVideos
                .let { ArrayList(it) } // Convert the List to ArrayList
        }

        fun onThumbnailClick(thumbnail: Bitmap, videoDetails: FeedMultipleVideos) {
            Log.d(TAG, "onThumbnailClick: ")
            var allVideos: ArrayList<FeedMultipleVideos> = arrayListOf()


            var feedAllVideos = feedUploadViewModel.getMixedFeedUploadDataClass()
            if (!addMoreFeedFiles) {
                allVideos = multipleSelectedFeedVideoAdapter.getVideoDetails()
            } else {

                allVideos = getAllVideos(feedAllVideos)
            }



            videoDetails.thumbnail = thumbnail
            val videoToUpdate = allVideos.indexOfFirst { it.videoUri == videoDetails.videoUri }


            if (!addMoreFeedFiles) {
                multipleSelectedFeedVideoAdapter.updateSelectedVideo(
                    videoToUpdate, videoDetails)
            } else {
                Log.d(
                    TAG,
                    "onThumbnailClick: update selected videoToUpdate " +
                            "$videoToUpdate, video details $videoDetails"
                )
                val feedVideoToUpdate =
                    feedAllVideos.indexOfFirst {
                        (it.videos?.videoUri ?: "") == videoDetails.videoUri
                    }
                val feedVideoToUpdates =
                    feedAllVideos.find { (it.videos?.videoUri ?: "") == videoDetails.videoUri }
                if (feedVideoToUpdates != null) {
                    feedVideoToUpdates.videos = videoDetails
                }
                if (feedVideoToUpdates != null) {
                    feedUploadViewModel.updateMixedFeedUploadDataClass(
                        feedVideoToUpdate,
                        feedVideoToUpdates
                    )
                }
                multipleFeedFilesPagerAdapter?.updateSelectedVideo(
                    videoToUpdate, videoDetails)
            }
            this.thumbnail = thumbnail
        }

        fun onVideoDisplay(details: FeedMultipleVideos) {
        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        fun onDocumentClickListener() {
            showAttachmentDialog()
        }
    }



    override fun onThumbnailLongClick(
        thumbnail: Bitmap,
        videoDetails: FeedMultipleVideos,
        position: Int
    ) {
        // Show options like share, download, save, etc.
       // showVideoOptionsMenu(videoDetails, position)
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
        Toast.makeText(this, "Link copied to clipboard",
            Toast.LENGTH_SHORT).show()
    }

    private fun handleSavePost() {
        // Save/unsave post
        Toast.makeText(this, "Post saved", Toast.LENGTH_SHORT).show()
    }

    private fun handleNotInterested() {
        // Mark as not interested
        Toast.makeText(this, "We'll show you fewer posts like this",
            Toast.LENGTH_SHORT).show()
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
            .setMultiChoiceItems(
                topicsList,
                selectedItems) { _, which, isChecked ->
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
            .setMultiChoiceItems(
                peopleList,
                selectedItems) { _, which, isChecked ->
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
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
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
                Toast.makeText(
                    this, "Please enable GPS",
                    Toast.LENGTH_SHORT).show()
                return
            }

            val locationListener = object : LocationListener {
                @SuppressLint("DefaultLocale")
                override fun onLocationChanged(location: Location) {
                    selectedLocation = "Current Location (${String.format(
                        "%.4f", 
                        location.latitude)}, " +
                            "${String.format("%.4f", 
                                location.longitude)})"
                    updateLocationUI()
                    applyLocationFilter()
                    locationManager.removeUpdates(this)
                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {
                    Toast.makeText(this@UploadFeedActivity,
                        "GPS disabled", Toast.LENGTH_SHORT).show()
                }
            }

            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0, 0f, locationListener)

            // Add timeout to prevent indefinite waiting
            Handler(Looper.getMainLooper()).postDelayed({
                locationManager.removeUpdates(locationListener)
            }, 10000) // 10 seconds timeout

        } catch (e: Exception) {
            Toast.makeText(this, "Unable to get location",
                Toast.LENGTH_SHORT).show()
            Log.e("LocationError", "Error getting location", e)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation()
                } else {
                    Toast.makeText(this, "Location permission required",
                        Toast.LENGTH_SHORT).show()
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
            textView.setTextColor(ContextCompat.getColor(this,
                R.color.colorPrimary))
        } else {
            textView.text = "Tag people"
            textView.setTextColor(ContextCompat.getColor(this,
                android.R.color.black))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateTopicsUI() {
        val textView = topicsLayout.findViewById<TextView>(R.id.topicsText)
        if (selectedTopics.isNotEmpty()) {
            textView.text = "Topics (${selectedTopics.size})"
            textView.setTextColor(ContextCompat.getColor(this,
                R.color.colorPrimary))
        } else {
            textView.text = "Add topics"
            textView.setTextColor(ContextCompat.getColor(this,
                android.R.color.black))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateLocationUI() {
        val textView = locationLayout.findViewById<TextView>(R.id.locationText)
        if (selectedLocation != null) {
            textView.text = "Location ✓"
            textView.setTextColor(ContextCompat.getColor(this,
                R.color.colorPrimary))
        } else {
            textView.text = "Add location"
            textView.setTextColor(ContextCompat.getColor(this,
                android.R.color.black))
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
            //val filteredPosts = filterPostsByLocation(location)
            //updateFeedWithFilteredPosts(filteredPosts)

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
//                    "Friends" -> post.isFromFriend
//                    "Family" -> post.isFromFamily
//                    "Followers" -> post.isFromFollower
//                    "Mutual Friends" -> post.isFromMutualFriend
//                    "Close Friends" -> post.isFromCloseFriend
//                    "Celebrities" -> post.isFromCelebrity
//                    "Influencers" -> post.isFromInfluencer
                    else -> true
                }
            }
        }
    }

    private fun filterPostsByTopics(topics: List<String>): List<Post> {
        // Filter posts based on selected topics
        return allPosts.filter { post ->
            topics.any { topic ->
                post.tags.contains(topic.lowercase()
                )
//                        || post.description.contains(topic, ignoreCase = true)
            }
        }
    }

//    private fun filterPostsByLocation(location: String): List<Post> {
//        // Filter posts based on location
//        return allPosts.filter { post ->
//
//            post.location?.contains(location, ignoreCase = true) == true ||
//                    isNearLocation(post.latitude, post.longitude, location)
//        }
//    }

    private fun isNearLocation(lat: Double?, lon: Double?, targetLocation: String): Boolean {
        // Implement proximity check logic
        if (lat == null || lon == null) return false


        return true
    }

    private fun updateFeedWithFilteredPosts(posts: List<Post>) {
        // Update RecyclerView with filtered posts
        feedAdapter.updatePosts(posts)

        // Show filter indicator
        showActiveFiltersIndicator()

        // Smooth scroll to top
        feedRecyclerView.smoothScrollToPosition(0)
    }

//    private fun updateFeedWithFilteredPosts(posts: List<Post>) {
//        // Update RecyclerView with filtered posts
//        feedAdapter.updatePosts(posts)
//
//        // Show filter indicator
//        showActiveFiltersIndicator()
//
//        // Smooth scroll to top
//        feedRecyclerView.smoothScrollToPosition(0)
//    }

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

    override fun cancelShortsUpload() {

    }


    fun saveBitmapToFile(bitmap: Bitmap, context: Context, fileName: String): File {
        val file = File(context.cacheDir, "$fileName.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file
    }

    fun saveBitmapToCache(
        context: Context,
        bitmap: Bitmap
    ) {
        TODO("Not yet implemented")
    }

    override fun backFromShortsUpload() {
    }

    override fun onProgress(progress: Int) {
        TODO("Not yet implemented")
    }

    override fun onImageClick() {
        val currentPosition = binding.viewPager.currentItem
        val currentItem = multipleFeedFilesPagerAdapter?.getItem(currentPosition)
        if (currentItem?.fileTypes == "image") {
            Log.d(TAG, "Image clicked at position: $currentPosition")
            //showFullScreenViewForAnyFileType(currentItem)
        } else {
            Log.w(TAG, "onImageClick: Current item is not an image or null")
        }
    }

    override fun onAudioClick() {
        val currentPosition = binding.viewPager.currentItem
        val currentItem = multipleFeedFilesPagerAdapter?.getItem(currentPosition)
        if (currentItem?.fileTypes == "audio") {
            Log.d(TAG, "Audio clicked at position: $currentPosition")
           // showFullScreenViewForAnyFileType(currentItem)
        } else {
            Log.w(TAG, "onAudioClick: Current item is not an audio or null")
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onAudioDisplay(details: MultipleAudios) {
        Log.d(TAG,
            "Displaying audio details: fileName=${details.fileName}, " +
                    "duration=${details.audioDuration}")
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({

            binding.selectCoverText.text =
                "File name: ${details.fileName} \nDuration: ${details.audioDuration}"
            binding.uploadedFileThumbNail.setImageResource(R.drawable.baseline_headphones_24)

            binding.uploadedFileThumbNail.setColorFilter(
                ContextCompat.getColor(this, R.color.black),
                PorterDuff.Mode.SRC_ATOP
            )

            binding.uploadedFileThumbNail.setPadding(0)
            binding.playButton.visibility = View.VISIBLE
            binding.pauseButton.visibility = View.GONE
            binding.selectCoverText.visibility = View.VISIBLE
            binding.recyclerView2.visibility = View.INVISIBLE
            updateFileUIVisibility()
        }, 500)
    }

    override fun onDocumentClickListener(position: Int, documentData: FeedMultipleDocumentsDataClass) {
        Log.d(TAG, "Document clicked at position $position: ${documentData.filename}")
        Log.d(TAG, "Clicked document has thumbnail: ${documentData.documentThumbnailFilePath != null}")

        // Show the preview for the clicked document
        showDocumentPreview(documentData)

        // You can add additional click handling logic here
    }

    override fun onDocumentClickListener() {
        val currentPosition = binding.viewPager.currentItem
        val currentItem = multipleFeedFilesPagerAdapter?.getItem(currentPosition)
        if (currentItem?.fileTypes?.startsWith("application/") == true) {
            Log.d(TAG, "Document clicked at position: $currentPosition")
           // showFullScreenViewForAnyFileType(currentItem)
        } else {
            Log.w(TAG, "onDocumentClickListener: Current item is not a document or null")
        }
    }

    override fun onVideoClick() {
        val currentPosition = binding.viewPager.currentItem
        val currentItem = multipleFeedFilesPagerAdapter?.getItem(currentPosition)
        if (currentItem?.fileTypes == "video") {
            Log.d(TAG, "Video clicked at position: $currentPosition")
           // showFullScreenViewForAnyFileType(currentItem)
        } else {
            Log.w(TAG, "onVideoClick: Current item is not a video or null")
        }
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onVideoDisplay(details: FeedMultipleVideos) {
        Log.d(TAG, "Displaying video details: fileName=${details.fileName}, " +
                "duration=${details.videoDuration}")
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            binding.selectCoverText.text = "File name: ${details.fileName}" +
                    " \nDuration: ${details.videoDuration}"
            binding.playButton.visibility = View.VISIBLE
            binding.pauseButton.visibility = View.GONE
            binding.selectCoverText.visibility = View.VISIBLE
            binding.uploadedFileThumbNail.colorFilter = null
            binding.uploadedFileThumbNail.setPadding(0)
            details.thumbnail?.let { bitmap ->
                Glide.with(this@UploadFeedActivity).load(bitmap).into(
                    binding.uploadedFileThumbNail)
            } ?: run {
                Glide.with(this@UploadFeedActivity).load(
                    R.drawable.flash21).into(binding.uploadedFileThumbNail)
            }
            lifecycleScope.launch(Dispatchers.IO) {
                val videoThumbnails = extractThumbnailsFromVideos(details.videoUri.toUri())
                withContext(Dispatchers.Main) {
                    setupRecyclerView(videoThumbnails, details)
                }
            }
            updateFileUIVisibility()

        }, 500)
    }

    private fun FeedUploadViewModel.clearMixedFeedUploadDataClass() {
        // This now calls the method in the ViewModel that preserves thumbnails
        this.clearMixedFeedUploadDataClass()
        Log.d("FeedUploadViewModel", "Cleared mixed feed upload data")
    }

}




