package com.uyscuti.social.circuit

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.ExoDatabaseProvider
import androidx.media3.datasource.DefaultDataSourceFactory
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkInfo
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.uyscuti.sharedmodule.FlashApplication
import com.uyscuti.sharedmodule.User_Interfaces.OtherUserProfile.OtherUserProfileAccount
import com.uyscuti.sharedmodule.adapter.CommentsRecyclerViewAdapter
import com.uyscuti.sharedmodule.adapter.OnViewRepliesClickListener
import com.uyscuti.sharedmodule.adapter.notifications.AdPaginatedAdapter
import com.uyscuti.sharedmodule.data.model.shortsmodels.OtherUsersProfile
import com.uyscuti.sharedmodule.media.CameraActivity
import com.uyscuti.sharedmodule.model.AudioPlayerHandler
import com.uyscuti.sharedmodule.model.CommentAudioPlayerHandler
import com.uyscuti.sharedmodule.model.PauseShort
import com.uyscuti.sharedmodule.model.ShortsFavoriteUnFavorite
import com.uyscuti.sharedmodule.model.ShortsLikeUnLike
import com.uyscuti.sharedmodule.ui.GifActivity
import com.uyscuti.sharedmodule.uploads.AudioActivity
import com.uyscuti.sharedmodule.uploads.DocumentsActivity
import com.uyscuti.sharedmodule.uploads.ImagesActivity
import com.uyscuti.sharedmodule.uploads.VideosActivity
import com.uyscuti.sharedmodule.utils.AndroidUtil.showToast
import com.uyscuti.sharedmodule.utils.AudioDurationHelper
import com.uyscuti.sharedmodule.utils.AudioDurationHelper.getFormattedDuration
import com.uyscuti.sharedmodule.utils.PathUtil
import com.uyscuti.sharedmodule.utils.Timer
import com.uyscuti.sharedmodule.utils.TrimVideoUtils
import com.uyscuti.sharedmodule.utils.VideoPlayerManager
import com.uyscuti.sharedmodule.utils.WaveFormExtractor
import com.uyscuti.sharedmodule.utils.audiomixer.AudioMixer
import com.uyscuti.sharedmodule.utils.audiomixer.input.GeneralAudioInput
import com.uyscuti.sharedmodule.utils.deleteFiles
import com.uyscuti.sharedmodule.utils.fileType
import com.uyscuti.sharedmodule.utils.formatFileSize
import com.uyscuti.sharedmodule.utils.generateRandomId
import com.uyscuti.sharedmodule.utils.getFileNameFromLocalPath
import com.uyscuti.sharedmodule.utils.getOutputFilePath
import com.uyscuti.sharedmodule.utils.isFileSizeGreaterThan2MB
import com.uyscuti.sharedmodule.utils.videodownload.DownloadStateManager
import com.uyscuti.sharedmodule.utils.videodownload.VideoDownloadManager
import com.uyscuti.sharedmodule.utils.waveformseekbar.SeekBarOnProgressChanged
import com.uyscuti.sharedmodule.utils.waveformseekbar.WaveformSeekBar
import com.uyscuti.sharedmodule.viewmodels.comments.ShotPostViewModel
import com.uyscuti.social.business.CatalogueDetailsActivity
import com.uyscuti.social.chatsuit.messages.CommentsInput
import com.uyscuti.social.circuit.databinding.ActivityPostDetails2Binding
import com.uyscuti.social.circuit.databinding.BottomDialogForShareBinding
import com.uyscuti.social.core.util.NetworkUtil
import com.uyscuti.social.network.api.models.Comment
import com.uyscuti.social.network.api.response.business.response.post.Post
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.uyscuti.social.network.api.response.post.Data
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage
import com.vanniktech.emoji.EmojiPopup
import id.zelory.compressor.Compressor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.hwpf.HWPFDocument
import org.apache.poi.hwpf.usermodel.Range
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.UUID
import kotlin.properties.Delegates


private const val TAG = "ShotsDetailsActivity"

@UnstableApi
@AndroidEntryPoint
class PostDetailsActivity2 : AppCompatActivity(),
    OnViewRepliesClickListener,
    CommentsInput.InputListener,
    CommentsInput.EmojiListener,
    CommentsInput.VoiceListener,
    CommentsInput.GifListener,
    CommentsInput.AttachmentsListener,
    Timer.OnTimeTickListener {

    private lateinit var binding: ActivityPostDetails2Binding

    @Inject
    lateinit var videoPlayerManager: VideoPlayerManager

    @Inject
    lateinit var retrofitInstance: RetrofitInstance

    @Inject
    lateinit var localStorage: LocalStorage

    private val shotPostViewModel: ShotPostViewModel by viewModels()

    @Inject
    lateinit var downloadStateManager: DownloadStateManager

    private lateinit var downloadManager: VideoDownloadManager

    private var currentDownloadWorkId: UUID? = null
    private var isActivityVisible = false

    // Permission request code
    private val NOTIFICATION_PERMISSION_CODE = 1001
    private val STORAGE_PERMISSION_CODE = 1002

    // Intent extras
    private var postId: String = ""
    private var commentId: String = ""
    private var showComments: Boolean = false
    private var isLoadingForTarget = false
    private var targetCommentId: String = ""

    // State variables
    private var isLiked = false
    private var isFavorited = false
    private var isFollowing = false
    private var isPlaying = true
    private var currentPost: Data? = null

    private var commentAdapter: CommentsRecyclerViewAdapter? = null
    private lateinit var commentRecyclerView: RecyclerView

    private lateinit var gifsPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var audioPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var videoPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var docsPickerLauncher: ActivityResultLauncher<Intent>


    private lateinit var inputMethodManager: InputMethodManager
    private lateinit var emojiPopup: EmojiPopup

    private var emojiShowing = false

    private var isReply = false

    private var commentToAddReplies: Comment? = null
    private var commentPosition = 0

    private var exoPlayer: ExoPlayer? = null

    private val recordedAudioFiles = mutableListOf<String>()

    private var mediaRecorder: MediaRecorder? = null


    private var player: MediaPlayer? = null
    private val waveHandler = Handler()

    private lateinit var outputFile: String

    private var outputVnFile: String = ""

    private lateinit var amplitudes: ArrayList<Float>

    private var amps = 0

    var wasPaused = false
    var sending = false
    var firstTimeSendVn = false

    private var isRecording = false
    private var isPaused = false
    private var isAudioVNPlaying = false
    private var isAudioVNPaused = false

    private var mixingCompleted = false

    private var isVnResuming = false


    var vnRecordAudioPlaying = false
    var vnRecordProgress = 0
    var isOnRecordDurationOnPause = false

    private var currentHandler: Handler? = null
    var seekBarProgress = 0f
    var waveProgress = 0f
    private var wavePosition = -1
    private var seekPosition = -1
    private var position: Int = 0
    var maxDuration = 0L

    private var simpleCache: SimpleCache? = FlashApplication.cache


    private lateinit var audioDurationTVCount: TextView
    private lateinit var audioFormWave: WaveformSeekBar

    private lateinit var audioSeekBar: SeekBar

    private lateinit var httpDataSourceFactory: HttpDataSource.Factory
    private lateinit var defaultDataSourceFactory: DefaultDataSourceFactory
    private lateinit var cacheDataSourceFactory: CacheDataSource.Factory


    private var isReplyVnPlaying = false
    private var isVnAudioToPlay = false
    var isDurationOnPause = false
    private var currentCommentAudioPosition = RecyclerView.NO_POSITION
    private var currentCommentAudioPath = ""


    private lateinit var timer: Timer

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetails2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        EventBus.getDefault().register(this)

        setupStatusBar()
        downloadManager = VideoDownloadManager(this)

        extractIntentExtras()
        setupViews()
        setupInputManager()
        setupClickListeners()
        observeDownloadState()
        loadPostData()
        handleActionEvents()
        registerGifPickerLauncher()
        registerImagePicker()
        registerVideoPickerLauncher()
        registerDocPicker()
        registerAudioPickerLauncher()


        if (showComments) {
            initCommentAdapter()
            toggleCommentBottomSheet()
            setCommentAdapterPagination()
            loadToTargetComment(targetCommentId)
        }
    }

    private fun observeDownloadState() {
        // Observe global download states
        downloadStateManager.downloadStates.observe(this) { states ->
            val downloadState = states[postId]
            downloadState?.let { state ->
                updateDownloadButton(state)
            } ?: run {
                // No download for this post
                binding.downloadBtn.reset()
            }
        }

        // Check if download is already in progress when activity starts
        val existingState = downloadStateManager.getDownloadState(postId)
        existingState?.let { state ->
            updateDownloadButton(state)
        }
    }

    private fun updateDownloadButton(state: DownloadStateManager.DownloadState) {
        when (state.status) {
            DownloadStateManager.Status.DOWNLOADING -> {
                if (state.progress == 0) {
                    binding.downloadBtn.setDownloading(true)
                } else {
                    binding.downloadBtn.setProgress(state.progress)
                }
            }
            DownloadStateManager.Status.PAUSED -> {
                binding.downloadBtn.setPaused(state.progress)
            }
            DownloadStateManager.Status.COMPLETED -> {
                binding.downloadBtn.setCompleted()
                binding.downloadBtn.postDelayed({
                    downloadStateManager.removeDownload(postId)
                }, 2000)
            }
            DownloadStateManager.Status.FAILED, DownloadStateManager.Status.CANCELLED -> {
                binding.downloadBtn.reset()
                downloadStateManager.removeDownload(postId)
            }
            DownloadStateManager.Status.IDLE -> {
                binding.downloadBtn.reset()
            }
        }
    }

    private fun downloadVideo() {
        val post = currentPost
        if (post == null) {
            showError("No video to download")
            return
        }

        // Check if already downloading
        if (downloadStateManager.isDownloading(postId)) {
            showError("Download already in progress")
            return
        }

        // Check and request permissions first
        if (!hasRequiredPermissions()) {
            requestDownloadPermissions()
            return
        }

        startDownload(post)
    }

    private fun startDownload(post: Data) {
        lifecycleScope.launch {
            try {
                // Start download
                val workId = downloadManager.downloadVideo(
                    videoUrl = post.images[0].url,
                    postId = postId,
                    videoTitle = "Video_${System.currentTimeMillis()}"
                )

                currentDownloadWorkId = workId

                // Observe download progress from WorkManager
                downloadManager.getDownloadProgress(workId).observe(this@PostDetailsActivity2) { workInfo ->
                    when (workInfo?.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            handleDownloadSuccess()
                        }
                        WorkInfo.State.FAILED, WorkInfo.State.CANCELLED -> {
                            handleDownloadFailure(workInfo.state)
                        }
                        else -> {
                            // State is being managed by DownloadStateManager
                        }
                    }
                }

                showSuccess("Download started")

            } catch (e: Exception) {
                showError("Failed to start download: ${e.message}")
            }
        }
    }

    private fun handleDownloadSuccess() {
        lifecycleScope.launch {
            if (isActivityVisible) {
                // Activity is visible - increment UI count and send to server
                val currentCount = binding.downloadCounts.text.toString().toIntOrNull() ?: 0
                binding.downloadCounts.text = formatCount(currentCount + 1)

                // Send to server
                try {
                    //  postRepository.incrementDownloadCount(postId)
                } catch (e: Exception) {
                    // Silently fail - count already updated locally
                }
            } else {
                // Activity is not visible - just send to server
                try {
                    //  postRepository.incrementDownloadCount(postId)
                } catch (e: Exception) {
                    // Silently fail or retry later
                }
            }
        }
    }

    private fun handleDownloadFailure(state: WorkInfo.State) {
        if (isActivityVisible) {
            val message = when (state) {
                WorkInfo.State.CANCELLED -> "Download cancelled"
                else -> "Download failed"
            }
            showError(message)
        }
    }


    private fun hasRequiredPermissions(): Boolean {
        // Check notification permission for Android 13+
        val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        // Check storage permission based on Android version
        val hasStoragePermission = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                // Android 10+ uses scoped storage, no permission needed
                true
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                // Android 6-9 needs WRITE_EXTERNAL_STORAGE
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED
            }
            else -> true
        }

        return hasNotificationPermission && hasStoragePermission
    }

    private fun requestDownloadPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
                android.content.pm.PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Request storage permission for Android 6-9
        if (Build.VERSION.SDK_INT in Build.VERSION_CODES.M until Build.VERSION_CODES.Q) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                android.content.pm.PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissions(permissionsToRequest.toTypedArray(), NOTIFICATION_PERMISSION_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            NOTIFICATION_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    // Permissions granted, start download
                    currentPost?.let { startDownload(it) }
                } else {
                    showError("Permissions are required to download videos")
                }
            }
        }
    }



    private fun loadToTargetComment(commentId: String) {
        isLoadingForTarget = true
        showShimmer()

        lifecycleScope.launch {
            val commentLocation =
                shotPostViewModel.locateShotComment(postId, commentId)
            if (commentLocation != null) {
                // check if it is a reply
                if (commentLocation.location.parentCommentId != null) {
                    loadPagesToTarget(
                        commentLocation.location.parentPageNumber ?: 1,
                        commentLocation.location.parentCommentId!!,
                        commentLocation.comments
                    )


                } else {
                    // top level comment
                    loadPagesToTarget(
                        commentLocation.location.pageNumber,
                        commentId,
                        commentLocation.comments
                    )
                }
            }
        }
    }

    private suspend fun loadPagesToTarget(
        targetPage: Int,
        commentId: String,
        comments: List<Comment>
    ) {
        commentAdapter!!.setmCurrentPage(targetPage)

        commentAdapter?.submitItems(comments)

        commentRecyclerView.post {
            // hideShimmer()
            scrollToComment(commentId, targetPage)
            if (comments.isEmpty()) {
                updateUI(true)
            } else {
                updateUI(false)
            }
            isLoadingForTarget = false
        }
    }

    private fun scrollToComment(commentId: String, currentPage: Int) {
        val position = commentAdapter?.findCommentPosition(commentId)

        if (position != -1) {
            val layoutManager = commentRecyclerView.layoutManager as LinearLayoutManager

            // Scroll to position with offset
            layoutManager.scrollToPositionWithOffset(position!!, commentRecyclerView.height / 3)

            commentRecyclerView.postDelayed({
                highlightComment(position, currentPage)
            }, 300)
        } else {
            // showError("Comment not available")
        }
    }

    private fun highlightComment(position: Int, currentPage: Int) {
        commentAdapter?.setHighlightedPosition(position)
        commentRecyclerView.postDelayed({
            commentAdapter?.clearHighlight()

            if (!isLoadingForTarget) {
                commentAdapter?.setmCurrentPage(currentPage)
            }
        }, 4000)
        hideShimmer()
    }


    private fun registerAudioPickerLauncher() {
        audioPickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

                val data = result.data
                val audioPath = data?.getStringExtra("audio_url")
                val uriString = data?.getStringExtra("aUri")
                val caption = data?.getStringExtra("caption") ?: ""

                if (audioPath != null) {
                    Log.d("AudioPicker", "File path: $audioPath")
                    val durationString = getFormattedDuration(audioPath)
                    val fileName = getFileNameFromLocalPath(audioPath)

                    Log.d("AudioPicker", "File name: $fileName")
                    Log.d("AudioPicker", "durationString: $durationString")
//                        Log.d("AudioPicker", "reverseDurationString: $reverseDurationString")
                    val file = File(audioPath)

                    var fileSizeInBytes by Delegates.notNull<Long>()
                    var fileSizeInKB by Delegates.notNull<Long>()
                    var fileSizeInMB by Delegates.notNull<Long>()


                    fileSizeInBytes = file.length()
                    fileSizeInKB = fileSizeInBytes / 1024
                    fileSizeInMB = fileSizeInKB / 1024

                    if (isReply) {
                        uploadAudioComment(
                            file.absolutePath,
                            caption,
                            isReply1 = isReply,
                            fileType = file.extension
                        )
                    } else {
                        Log.d("AudioPicker", "Calling upload audio comment")
                        uploadAudioComment(
                            file.absolutePath,
                            caption,
                            isReply1 = isReply,
                            fileType = file.extension
                        )
                    }

                }
            }

    }

    private fun registerDocPicker() {
        docsPickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val data = result.data
                    // Process the selected image data
                    val docPath = data?.getStringExtra("doc_url")
                    val caption = data?.getStringExtra("caption") ?: ""

                    Log.d(TAG, "Path: $docPath caption: $caption")

                    handleDocumentUri(getContentUriFromFilePath(this, docPath!!)!!, caption)
                }
            }
    }

    private fun registerVideoPickerLauncher() {
        // Register the launcher in onCreate
        videoPickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

                if (result.resultCode == RESULT_OK) {
                    val data = result.data

                    val videoPath = data?.getStringExtra("video_url")
                    val uriString = data?.getStringExtra("vUri")
                    val vUri = Uri.parse(uriString)
                    val caption = data?.getStringExtra("caption") ?: ""

                    val uri = Uri.parse(videoPath)

                    if (videoPath != null) {
                        Log.d("VideoPicker", "File path: $videoPath")
                        val durationString = getFormattedDuration(videoPath)
                        val file = File(videoPath)
                        Log.d("VideoPicker", "File path durationString: $durationString")

                        if (file.exists()) {
                            val fileSizeInBytes = file.length()
                            val fileSizeInKB = fileSizeInBytes / 1024
                            val fileSizeInMB = fileSizeInKB / 1024

                            val fileSizeInGB = fileSizeInMB / 1024 // Conversion from MB to GB

                            Log.d("VideoPicker", "File size: $fileSizeInMB MB")

                            if (fileSizeInGB.toInt() == 1) {
                                showToast(this, "File size too large")
                            } else if (fileSizeInMB > 10) {
                                Log.d("VideoPicker", "File size: greater than $fileSizeInMB MB")
                                if (isReply) {
                                    uploadVideoComment(videoPath, caption, isReply)
                                } else {
                                    uploadVideoComment(videoPath, caption)
                                }
                            } else {
                                Log.d("VideoPicker", "File size: less than $fileSizeInMB MB")
                                if (isReply) {
                                    uploadVideoComment(videoPath, caption, isReply)
                                } else {
                                    uploadVideoComment(videoPath, caption)
                                }
                            }
                        }
                    }


                }
            }

    }

    private fun registerImagePicker() {
        imagePickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    // Handle image selection result here
                    val data = result.data
                    // Process the selected image data
                    val imagePath = data?.getStringExtra("image_url")
                    val caption = data?.getStringExtra("caption") ?: ""

                    val filePath = PathUtil.getPath(
                        this,
                        imagePath!!.toUri()
                    ) // Use the utility class to get the real file path
                    Log.d("PhotoPicker", "File path: $filePath")
                    Log.d("PhotoPicker", "File path: $isReply")

                    val file = filePath?.let { File(it) }
                    if (file?.exists() == true) {
                        lifecycleScope.launch {
                            val compressedImageFile =
                                Compressor.compress(this@PostDetailsActivity2, file)
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

                            if (!isReply) {
                                uploadImageComment(
                                    compressedImageFile.absolutePath,
                                    caption,
                                    isReply
                                )
                            } else {
                                uploadImageComment(
                                    compressedImageFile.absolutePath,
                                    caption,
                                    isReply
                                )
                            }
                        }
                    }

                }
            }
    }

    private fun registerGifPickerLauncher() {
        gifsPickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                val data = result.data
                val gifUri = data?.getStringExtra("gifUri")
                Log.d(TAG, "Gif Uri $gifUri")

                if (gifUri!!.isNotEmpty()) {

                    val localUpdateId = generateRandomId()

                    if (isReply) {
                        shotPostViewModel.addCommentReply(
                            commentId,
                            contentType = "gif",
                            localUpdateId = localUpdateId,
                            gif = gifUri,
                            isReply = isReply
                        )
                        isReply = false
                    } else {
                        shotPostViewModel.addComment(
                            postId,
                            contentType = "gif",
                            localUpdateId = localUpdateId,
                            gif = gifUri
                        )
                    }

                }

            }
    }

    private fun setupInputManager() {
        emojiPopup = EmojiPopup(binding.motionLayout, binding.input.inputEditText)

        binding.input.setInputListener(this)
        binding.input.setAttachmentsListener(this)
        binding.input.setVoiceListener(this)
        binding.input.setEmojiListener(this)
        binding.input.setGifListener(this)
    }

    private fun initEmojiView() {
        if (emojiShowing) {
            emojiPopup.dismiss()
            // Show keyboard after a slight delay to ensure smooth transition
            binding.input.inputEditText?.postDelayed({
                inputMethodManager.showSoftInput(
                    binding.input.inputEditText, InputMethodManager.SHOW_IMPLICIT
                )
            }, 50)
            emojiShowing = false
        } else {
            // Hide keyboard first
            inputMethodManager.hideSoftInputFromWindow(
                binding.input.inputEditText?.windowToken, 0
            )
            // Show emoji popup after a slight delay
            binding.input.inputEditText?.postDelayed({
                emojiPopup.toggle()
            }, 50)
            emojiShowing = true
        }
    }

    private fun onGoBack() {
        val callback = object : OnBackPressedCallback(true) {

            override fun handleOnBackPressed() {
                if (binding.motionLayout.isVisible) {
                    toggleCommentBottomSheet()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun getContentUriFromFilePath(context: Context, filePath: String): Uri? {
        val file = File(filePath)
        val projection = arrayOf(MediaStore.Files.FileColumns._ID)
        val selection = "${MediaStore.Files.FileColumns.DATA}=?"
        val selectionArgs = arrayOf(file.absolutePath)

        context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id =
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
                return ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id)
            }
        }
        return null
    }

    private fun getFileNameWithExtension(uri: Uri): String {
        var fileName = "document_${System.currentTimeMillis()}"

        // Try to get the original filename
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst()) {
                fileName = cursor.getString(nameIndex) ?: fileName
            }
        }

        // If filename doesn't have an extension, get it from MIME type
        if (!fileName.contains(".")) {
            val mimeType = contentResolver.getType(uri)
            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
            if (extension != null) {
                fileName = "${fileName}.${extension}"
            }
        }

        return fileName
    }

    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val fileName = getFileNameWithExtension(uri)
            val tempFile = File(cacheDir, fileName)

            inputStream?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            // Debug: Check if file exists and has content
            Log.d("FileDebug", "File path: ${tempFile.absolutePath}")
            Log.d("FileDebug", "File exists: ${tempFile.exists()}")
            Log.d("FileDebug", "File size: ${tempFile.length()} bytes")

            if (tempFile.exists() && tempFile.length() > 0) {
                tempFile
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun handleDocumentUri(uri: Uri, caption: String) {
        val file = getFileFromUri(uri)

        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            cursor.moveToFirst()
            val fileName = cursor.getString(nameIndex)
            val fileSize = cursor.getLong(sizeIndex)
//            val numberOfPages = getNumberOfPagesFromUri(this, uri)
            var numberOfPages = 0
            val formattedFileSize = formatFileSize(fileSize)

            val fileSizes = isFileSizeGreaterThan2MB(fileSize)
            val documentType = fileType(fileName)
            Log.d("handleDocumentUri", ": $fileName")
            Log.d("handleDocumentUri", "uri $uri")
            Log.d("handleDocumentUri", "formattedFileSize $formattedFileSize")
            Log.d("handleDocumentUri", "Document type $documentType")

            numberOfPages = when (documentType) {
                "doc" -> {
                    getNumberOfPagesFromUriForDoc(uri)
                }

                "docx", "pptx" -> {
                    getNumberOfPagesFromUriForDocx(uri)
                }

                "xlsx", "xls" -> {
                    getNumberOfSheetsFromUri(uri)
                }

                else -> {
                    getNumberOfPagesFromUriForPDF(this, uri)
                }
            }


            Log.d("handleDocumentUri", "File path: ${file?.absolutePath}")

            if (fileSizes) {
                if (!isReply) {
                    Log.d("handleDocumentUri", "handleDocumentUri for main document")

                    uploadDocumentComment(
                        file?.absolutePath!!,
                        caption,
                        numberOfPages,
                        formattedFileSize,
                        documentType,
                        fileName,
                        isReply
                    )
                } else {
                    Log.d("handleDocumentUri", "This is for document reply")
                    uploadDocumentComment(
                        file?.absolutePath!!,
                        caption,
                        numberOfPages,
                        formattedFileSize,
                        documentType,
                        fileName,
                        isReply
                    )
                }

            } else {

                if (!isReply) {
                    Log.d("handleDocumentUri", "handleDocumentUri for main document")
                    uploadDocumentComment(
                        file?.absolutePath!!,
                        caption,
                        numberOfPages,
                        formattedFileSize,
                        documentType,
                        fileName,
                        isReply
                    )
                } else {
                    Log.d("handleDocumentUri", "This is for document reply")
                    uploadDocumentComment(
                        file?.absolutePath!!,
                        caption,
                        numberOfPages,
                        formattedFileSize,
                        documentType,
                        fileName,
                        isReply
                    )
                }
            }

        }
    }

    private fun getNumberOfPagesFromUriForDoc(uri: Uri): Int {
        var numberOfPages = 0
        val inputStream: InputStream = contentResolver.openInputStream(uri) ?: return 0
        val hwpfDocument = HWPFDocument(inputStream)
        val range = hwpfDocument.range

        // Count the paragraphs within the range
        val paragraphs = Range(range.startOffset, range.endOffset, hwpfDocument).numParagraphs()
        numberOfPages = paragraphs

        hwpfDocument.close()
        inputStream.close()

        return numberOfPages

    }

    private fun getNumberOfPagesFromUriForPDF(context: Context, uri: Uri): Int {
        var inputStream: InputStream? = null
        var numberOfPages = 0
        try {
            inputStream = contentResolver.openInputStream(uri)
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

    private fun getNumberOfSheetsFromUri(uri: Uri): Int {
        try {
            var numberOfPages = 0
            val inputStream = contentResolver.openInputStream(uri)
            val mimeType = contentResolver.getType(uri)

            val workbook = when {
                // .xlsx (newer format)
                mimeType == "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" ||
                        uri.path?.endsWith(".xlsx", ignoreCase = true) == true -> {
                    XSSFWorkbook(inputStream)
                }
                // .xls (older format)
                mimeType == "application/vnd.ms-excel" ||
                        uri.path?.endsWith(".xls", ignoreCase = true) == true -> {
                    HSSFWorkbook(inputStream)
                }

                else -> null
            }

            workbook?.let {
                numberOfPages = it.numberOfSheets
                it.close()
            }
            inputStream?.close()

            return numberOfPages
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }

    private fun getNumberOfPagesFromUriForDocx(uri: Uri): Int {
        var numberOfPages = 0
        val inputStream: InputStream = contentResolver.openInputStream(uri) ?: return 0
        val xwpfDocument = XWPFDocument(inputStream)

        // Count the paragraphs or sections in the document
        numberOfPages = xwpfDocument.paragraphs.size

        xwpfDocument.close()
        inputStream.close()

        return numberOfPages

    }

    private fun uploadImageComment(
        imageFilePathToUpload: String,
        caption: String = "",
        isReply1: Boolean
    ) {

        Log.d("uploadImageComment", "uploadImageComment: $imageFilePathToUpload")
        Log.d("uploadImageComment", "uploadImageComment: isReply is $isReply")

        val file = File(imageFilePathToUpload)

        val localUpdateId = generateRandomId()

        if (file.exists()) {
            if (isReply) {
                shotPostViewModel.addCommentReply(
                    commentId,
                    content = caption,
                    contentType = "image",
                    localUpdateId = localUpdateId,
                    file = file,
                    isReply = isReply1
                )
                isReply = false
            } else {
                shotPostViewModel.addComment(
                    postId,
                    content = caption,
                    contentType = "image",
                    localUpdateId = localUpdateId,
                    file = file
                )
            }
        }

    }

    private fun uploadVideoComment(
        videoFilePathToUpload: String,
        caption: String,
        isReply1: Boolean = false
    ) {
        Log.d("uploadVideoComment", "uploadVideoComment: $videoFilePathToUpload")

        val file = File(videoFilePathToUpload)

        val localUpdateId = generateRandomId()

        if (file.exists()) {
            if (isReply) {
                shotPostViewModel.addCommentReply(
                    commentId,
                    content = caption,
                    contentType = "video",
                    localUpdateId = localUpdateId,
                    file = file,
                    isReply = isReply1
                )
                isReply = false
            } else {
                shotPostViewModel.addComment(
                    postId,
                    content = caption,
                    contentType = "video",
                    localUpdateId = localUpdateId,
                    file = file
                )
            }
        }

    }

    private fun uploadDocumentComment(
        documentFilePathToUpload: String,
        caption: String,
        numberOfPages: Int,
        fileSize: String,
        fileType: String,
        fileName: String,
        isReply1: Boolean
    ) {

        val file = File(documentFilePathToUpload)

        val localUpdateId = generateRandomId()
        Log.d("UploadingDocument", "File exist: ${file.exists()}")
        if (file.exists()) {
            Log.d("UploadingDocument", "Upload document called")

            if (isReply) {
                shotPostViewModel.addCommentReply(
                    commentId,
                    content = caption,
                    file = file,
                    contentType = "docs",
                    localUpdateId = localUpdateId,
                    numberOfPages = numberOfPages,
                    fileType = fileType,
                    fileName = fileName,
                    fileSize = fileSize,
                    isReply = isReply1
                )

                isReply = false
            } else {
                shotPostViewModel.addComment(
                    postId,
                    content = caption,
                    file = file,
                    contentType = "docs",
                    localUpdateId = localUpdateId,
                    numberOfPages = numberOfPages,
                    fileType = fileType,
                    fileName = fileName,
                    fileSize = fileSize
                )
            }

        }

    }

    private fun uploadAudioComment(
        audio: String,
        caption: String = "",
        contentType: String = "audio",
        isReply1: Boolean,
        fileType: String
    ) {
        val localUpdateId = generateRandomId()
        val file = File(audio)

        if (file.exists()) {
            if (isReply) {
                shotPostViewModel.addCommentReply(
                    commentId,
                    content = caption,
                    file = file,
                    contentType = contentType,
                    localUpdateId = localUpdateId,
                    isReply = isReply1,
                    fileType = fileType
                )

                isReply = false
            } else {
                shotPostViewModel.addComment(
                    postId,
                    content = caption,
                    file = file,
                    contentType = contentType,
                    localUpdateId = localUpdateId,
                    fileType = fileType
                )
            }

        }
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("SetTextI18n")
    private fun handleActionEvents() {


        binding.click.setOnClickListener {
            hideKeyboard(binding.input.inputEditText)
            toggleCommentBottomSheet()
        }

        binding.deleteVN.setOnClickListener {

            if (mediaRecorder != null) {
                Log.d(TAG, "onCreate: media recorder not null")
            } else {
                Log.d(TAG, "onCreate: media recorder null")
            }
            lifecycleScope.launch(Dispatchers.Main) {
                delay(500)
                deleteRecording()
                binding.sendVN.isClickable = true
            }
            if (player?.isPlaying == true) {
                stopPlayingVn()
            }

            binding.VnLayout.visibility = View.GONE
        }

        binding.sendVN.setOnClickListener {
            sending = true
            CoroutineScope(Dispatchers.Main).launch {
                if (!wasPaused) {
                    timer.stop()
                    mediaRecorder?.apply {
                        stop()
                        release()
                    }
                    mediaRecorder = null
                    Log.d("SendVN", "When sending vn was paused was false")
                    mixVN() // Execute mixVN asynchronously
                }

                lifecycleScope.launch(Dispatchers.Main) {
                    delay(500)
                    stopRecordingAndSendVn()
                }

            }
        }

        binding.recordVN.setOnClickListener {
            when {
                isPaused -> resumeRecordingVn()
                isRecording -> pauseRecordingVn()
                else -> {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        startRecordingVn()
                    } else {
                        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }

                }
            }
        }

        onGoBack()
    }

    private fun hideKeyboard(view: View) {
        val imm = inputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }


    private fun setupStatusBar() {
        window.apply {
            // Set status bar color to black (API 21+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                statusBarColor = Color.BLACK
            }

            // Set status bar icons to light/white color for visibility on black background
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // API 23+ - Ensure icons are white (light) on black background
                @Suppress("DEPRECATION")
                var flags = decorView.systemUiVisibility
                // Remove the LIGHT_STATUS_BAR flag to make icons white
                flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                decorView.systemUiVisibility = flags
            }
            // For API < 23, status bar icons are always white by default
        }
    }

    private fun extractIntentExtras() {
        postId = intent.getStringExtra("post_id") ?: ""
        targetCommentId = intent.getStringExtra("comment_id") ?: ""
        showComments = intent.getBooleanExtra("showComments", false)
    }

    private fun setupViews() {
        // Initialize video player
        videoPlayerManager.initialize(binding.videoView)

        // Set initial visibility
        binding.progressBar.isVisible = true
        binding.btnPlayPause.isVisible = false
        binding.videoLayout.isVisible = false

        inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        timer = Timer(this)
        audioDurationTVCount = TextView(this)
        audioFormWave = WaveformSeekBar(this)
        audioSeekBar = SeekBar(this)


        // Setup SeekBar
        binding.bottomShortsVideoProgressSeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                if (fromUser) {
                    videoPlayerManager.seekTo(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupClickListeners() {
        // Play/Pause functionality
        binding.shortsViewPager.setOnClickListener {
            togglePlayPause()
        }

        binding.videoView.setOnClickListener {
            togglePlayPause()
        }

        // Like button
        binding.btnLike.setOnClickListener {
            toggleLike()
        }

        // Comments
        binding.commentsParentLayout.setOnClickListener {
            openComments()
        }

        // Favorite/Bookmark
        binding.favorite.setOnClickListener {
            toggleFavorite()
        }

        // Share button
        binding.shareBtn.setOnClickListener {
            sharePost()
        }

        // Download button
        binding.downloadBtn.setOnClickListener {
            downloadVideo()
        }

        // Profile image click
        binding.profileImageForShort.setOnClickListener {
            openUserProfile()
        }

        // Username click
        binding.shortUsername.setOnClickListener {
            openUserProfile()
        }

        // Follow button
        binding.followButton.setOnClickListener {
            toggleFollow()
        }

        // Read more/less caption
        binding.tvReadMoreLess.setOnClickListener {
            toggleCaptionExpansion()
        }
    }

    private fun loadPostData() {

        binding.progressBar.isVisible = true

        lifecycleScope.launch {
            try {

                val response = shotPostViewModel.getShotPost(postId)
                if (response != null) {
                    binding.progressBar.isVisible = false
                    binding.videoLayout.isVisible = true
                    val shot = response
                    updateUI(shot)
                    Log.d("ShotDetails", "Details: $response")
                } else {
                    binding.progressBar.isVisible = false
                    binding.showError.isVisible = true
                    Log.d("ShotDetails", "Details: $response")
                }

            } catch (e: Exception) {
                binding.progressBar.isVisible = false
                binding.showError.isVisible = true
            }
        }
    }

    private fun updateUI(post: Data) {
        currentPost = post

        binding.followButton.isVisible = post.author.owner != localStorage.getUserId()

        // Update username
        binding.shortUsername.text = post.author.account.username

        // Update caption
        binding.tvReadMoreLess.text = post.content

        // Update counts
        binding.likeCount.text = formatCount(post.likes)
        binding.commentsCount.text = formatCount(post.comments)
        binding.favoriteCounts.text = formatCount(post.bookmarks)
//        binding.shareCount.text = formatCount(post.shareCount)
//        binding.downloadCounts.text = formatCount(post.downloadCount)

        // Update states
        isLiked = post.isLiked
        isFavorited = post.isBookmarked
        isFollowing = post.isFollowing

        updateLikeButton()
        updateFavoriteButton()
        updateFollowButton()

        // Load profile image
        Glide.with(this)
            .load(post.author.account.avatar.url)
            .placeholder(R.drawable.baseline_person_outline_24)
            .circleCrop()
            .into(binding.profileImageForShort)

        // Load video thumbnail
        Glide.with(this)
            .load(post.thumbnail.first().thumbnailUrl)
            .into(binding.videoThumbnail)

        // Load and play video
        loadVideo(post.images.first().url)
        observeViewModel(currentPost!!)
    }

    private fun loadVideo(videoUrl: String) {
        videoPlayerManager.loadVideo(
            url = videoUrl,
            onReady = {
                binding.progressBar.isVisible = false
                binding.videoThumbnail.isVisible = false
                startVideoPlayback()
            },
            onError = { error ->
                binding.progressBar.isVisible = false
                showError("Video loading failed: $error")
            }
        )
    }

    private fun startVideoPlayback() {
        videoPlayerManager.play()
        isPlaying = true
        updatePlayPauseButton()

        // Start updating seek bar
        updateSeekBar()
    }

    private fun updateSeekBar() {
        lifecycleScope.launch {
            while (isPlaying) {
                val progress = videoPlayerManager.getCurrentProgress()
                binding.bottomShortsVideoProgressSeekBar.progress = progress
                kotlinx.coroutines.delay(100)
            }
        }
    }

    private fun togglePlayPause() {
        if (isPlaying) {
            videoPlayerManager.pause()
            isPlaying = false
            binding.btnPlayPause.isVisible = false
        } else {
            videoPlayerManager.play()
            isPlaying = true
            binding.btnPlayPause.isVisible = false
            updateSeekBar()
        }
        updatePlayPauseButton()
    }

    private fun updatePlayPauseButton() {
        binding.btnPlayPause.setImageResource(
            if (isPlaying) R.drawable.baseline_pause_black
            else R.drawable.baseline_play_black // You'll need to add this drawable
        )
    }

    private fun toggleLike() {
        isLiked = !isLiked
        updateLikeButton()

        lifecycleScope.launch {
            try {

                EventBus.getDefault().post(ShortsLikeUnLike(postId))

                val currentCount = binding.likeCount.text.toString().toIntOrNull() ?: 0
                binding.likeCount.text = formatCount(currentCount + if (isLiked) 1 else -1)

            } catch (e: Exception) {
                // Revert on error
                isLiked = !isLiked
                updateLikeButton()
                showError("Failed to update like")
            }
        }
    }

    private fun updateLikeButton() {
        if (isLiked) {
            binding.btnLike.setImageResource(R.drawable.filled_favorite_like) // You'll need this
            binding.btnLike.tag = "filled"
        } else {
            binding.btnLike.setImageResource(R.drawable.favorite_svgrepo_com)
            binding.btnLike.tag = "unfilled"
        }
    }

    private fun toggleFavorite() {
        isFavorited = !isFavorited
        updateFavoriteButton()

        lifecycleScope.launch {
            try {
                EventBus.getDefault().post(ShortsFavoriteUnFavorite(postId))
                val currentCount = binding.favoriteCounts.text.toString().toIntOrNull() ?: 0
                binding.favoriteCounts.text = formatCount(currentCount + if (isFavorited) 1 else -1)
            } catch (e: Exception) {
                isFavorited = !isFavorited
                updateFavoriteButton()
                showError("Failed to update favorite")
            }
        }
    }

    private fun updateFavoriteButton() {
        if (isFavorited) {
            binding.favorite.setImageResource(R.drawable.filled_favorite) // Add this
            binding.favorite.tag = "filled"
        } else {
            binding.favorite.setImageResource(R.drawable.favorite_svgrepo_com__1_)
            binding.favorite.tag = "unfilled"
        }
    }

    private fun toggleFollow() {
        isFollowing = !isFollowing
        updateFollowButton()
        binding.followButton.isEnabled = false

        lifecycleScope.launch {
            try {
                val response =
                    retrofitInstance.apiService.followUnFollow(currentPost!!.author.owner)
                if (response.isSuccessful) {
                    binding.followButton.isEnabled = true
                } else {
                    isFollowing = !isFollowing
                    updateFollowButton()
                    showError("Failed to update follow status")
                }

            } catch (e: Exception) {
                isFollowing = !isFollowing
                updateFollowButton()
                showError("Failed to update follow status")
            }
        }
    }

    private fun updateFollowButton() {
        if (isFollowing) {
            binding.followButton.text =
                resources.getString(com.uyscuti.social.business.R.string.following)
            binding.followButton.setBackgroundResource(R.drawable.shorts_following_button)
        } else {
            binding.followButton.text =
                resources.getString(com.uyscuti.social.business.R.string.follow)
            binding.followButton.setBackgroundResource(R.drawable.shorts_follow_button_border)
        }
    }

    private fun initCommentAdapter() {
        commentAdapter = CommentsRecyclerViewAdapter(this, this)
        commentAdapter?.setDefaultRecyclerView(this, R.id.recyclerView)

        commentRecyclerView = binding.recyclerView

        commentRecyclerView.itemAnimator = null
    }

    private fun toggleCommentBottomSheet() {
        val currentVisibility = binding.motionLayout.visibility

        if (currentVisibility == View.VISIBLE) {
            binding.motionLayout.visibility = View.GONE
            binding.VnLayout.visibility = View.GONE

            binding.replyToLayout.visibility = View.GONE
            binding.input.inputEditText.setText("")
            binding.placeholderLayout.visibility = View.GONE

            deleteRecording()
            stopPlayingVn()
            commentAudioStop()
            stopWaveRunnable()
            stopRecordWaveRunnable()
            exoPlayer?.release()

        } else {
            binding.motionLayout.visibility = View.VISIBLE
            binding.motionLayout.transitionToStart()
        }

    }

    private fun setCommentAdapterPagination() {
        commentAdapter!!.setOnPaginationListener(object : AdPaginatedAdapter.OnPaginationListener {

            override fun onCurrentPage(page: Int) {
                Log.d(TAG, "currentPage: page number $page")
            }

            override fun onNextPage(page: Int) {
                lifecycleScope.launch(Dispatchers.Main) {
                    Log.d(TAG, "onNextPage: page number $page")
                    getShotsComments(page)
                }
            }

            override fun onFinish() {
                Log.d(TAG, "finished: page number")
            }
        })
    }

    private fun getShotsComments(page: Int) {

        lifecycleScope.launch(Dispatchers.IO) {

            withContext(Dispatchers.Main) {
                if (page == 1) {
                    showShimmer()
                } else {
                    showProgressBar()
                }
            }

            try {

                val commentsWithReplies = loadComment(page)
                withContext(Dispatchers.Main) {

                    if (page == 1) {
                        hideShimmer()
                    } else {
                        hideProgressBar()
                    }

                    commentAdapter!!.submitItems(commentsWithReplies)
                    if (commentsWithReplies.isEmpty()) {
                        updateUI(true)
                    } else {
                        updateUI(false)
                    }
                }

            } catch (e: Exception) {
                lifecycleScope.launch {

                    if (page == 1) {
                        hideShimmer()
                    } else {
                        hideProgressBar()
                    }
                }
                e.printStackTrace()
            }

        }

    }

    private suspend fun loadComment(page: Int): List<Comment> {
        val response = shotPostViewModel.getShotPostComments(postId, page)
        Log.d(TAG, "Comments: $response")
        return response
    }

    private fun loadInitialComments() {
        lifecycleScope.launch(Dispatchers.Main) {
            getShotsComments(commentAdapter!!.startPage)
        }
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }


    private fun updateUI(dataEmpty: Boolean) {
        if (dataEmpty) {
            commentRecyclerView.visibility = View.GONE
            binding.placeholderLayout.visibility = View.VISIBLE
        } else {
            binding.placeholderLayout.visibility = View.GONE
            commentRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun showShimmer() {
        binding.shimmerLayout.startShimmerAnimation()
        binding.shimmerLayout.visibility = View.VISIBLE
    }

    private fun hideShimmer() {
        binding.shimmerLayout.stopShimmerAnimation()
        binding.shimmerLayout.visibility = View.GONE
    }

    private fun openComments() {
        initCommentAdapter()
        toggleCommentBottomSheet()
        setCommentAdapterPagination()
        loadInitialComments()
    }

    private fun sharePost() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val shareBinding = BottomDialogForShareBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(shareBinding.root)

        // Prepare share content
        val shareText = "Check out this video on Flash!\n" +
                "By: ${currentPost!!.author.account.username}\n" +
                "${currentPost!!.content}"
        val videoUrl = currentPost!!.images.firstOrNull()?.url
        val fullShareText = if (videoUrl != null) "$shareText\n$videoUrl" else shareText

        // Setup share buttons
        shareBinding.btnWhatsApp.setOnClickListener {
            shareToWhatsApp(this, fullShareText)
            bottomSheetDialog.dismiss()
        }

        shareBinding.btnSMS.setOnClickListener {
            shareViaSMS(this, fullShareText)
            bottomSheetDialog.dismiss()
        }

        shareBinding.btnInstagram.setOnClickListener {
            shareToInstagram(this, fullShareText)
            bottomSheetDialog.dismiss()
        }

        shareBinding.btnMessenger.setOnClickListener {
            shareToMessenger(this, fullShareText)
            bottomSheetDialog.dismiss()
        }

        shareBinding.btnFacebook.setOnClickListener {
            shareToFacebook(this, fullShareText)
            bottomSheetDialog.dismiss()
        }

        shareBinding.btnTelegram.setOnClickListener {
            shareToTelegram(this, fullShareText)
            bottomSheetDialog.dismiss()
        }

        // Setup action buttons
        shareBinding.btnReport.setOnClickListener {
            Toast.makeText(this, "Report functionality", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }

        shareBinding.btnNotInterested.setOnClickListener {
            Toast.makeText(this, "Not interested", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }

        shareBinding.btnSaveVideo.setOnClickListener {
            Toast.makeText(this, "Save video functionality", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }

        shareBinding.btnDuet.setOnClickListener {
            Toast.makeText(this, "Duet functionality", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }

        shareBinding.btnReact.setOnClickListener {
            Toast.makeText(this, "React functionality", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }

        shareBinding.btnAddToFavorites.setOnClickListener {
            Toast.makeText(this, "Add to favorites", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }

        // Setup cancel button
        shareBinding.btnCancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun shareToWhatsApp(context: Context, text: String) {
        val packages = listOf(
            "com.whatsapp",
            "com.whatsapp.w4b"  // WhatsApp Business
        )
        shareToApp(context, text, packages, "WhatsApp")
    }

    private fun shareViaSMS(context: Context, text: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = "smsto:".toUri()
                putExtra("sms_body", text)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "SMS app not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareToInstagram(context: Context, text: String) {
        val packages = listOf(
            "com.instagram.android"
        )
        shareToApp(context, text, packages, "Instagram")
    }

    private fun shareToFacebook(context: Context, text: String) {
        val packages = listOf(
            "com.facebook.katana",
            "com.facebook.lite"
        )
        shareToApp(context, text, packages, "Facebook")
    }

    private fun shareToMessenger(context: Context, text: String) {
        try {
            // Try Messenger URI scheme first
            val messengerIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("fb-messenger://share/?link=${Uri.encode(text)}")
            }

            if (messengerIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(messengerIntent)
                return
            }

            // Fallback to standard share with specific package
            val packages = listOf("com.facebook.orca", "com.facebook.mlite")
            for (packageName in packages) {
                try {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        setPackage(packageName)
                        putExtra(Intent.EXTRA_TEXT, text)
                    }

                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                        return
                    }
                } catch (e: Exception) {
                    continue
                }
            }

            Toast.makeText(context, "Messenger not installed", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Messenger share error", e)
            Toast.makeText(context, "Messenger not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareToTelegram(context: Context, text: String) {
        try {
            // Try Telegram URI scheme first
            val telegramIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("tg://msg?text=${Uri.encode(text)}")
            }

            if (telegramIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(telegramIntent)
                return
            }

            // Fallback to web share URL
            val webIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://t.me/share/url?url=${Uri.encode(text)}")
            }

            if (webIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(webIntent)
                return
            }

            // Last fallback - standard share with specific packages
            val packages = listOf(
                "org.telegram.messenger",
                "org.telegram.messenger.web",
                "org.thunderdog.challegram"
            )

            for (packageName in packages) {
                try {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        setPackage(packageName)
                        putExtra(Intent.EXTRA_TEXT, text)
                    }

                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                        return
                    }
                } catch (e: Exception) {
                    continue
                }
            }

            Toast.makeText(context, "Telegram not installed", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Telegram share error", e)
            Toast.makeText(context, "Telegram not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareToApp(
        context: Context,
        text: String,
        packages: List<String>,
        appName: String
    ) {
        try {
            for (packageName in packages) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    setPackage(packageName)
                    putExtra(Intent.EXTRA_TEXT, text)
                }

                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                    return
                }
            }

            // If none of the specific packages work, show toast
            Toast.makeText(context, "$appName not installed", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "$appName not available", Toast.LENGTH_SHORT).show()
        }
    }

    @OptIn(UnstableApi::class)
    private fun openUserProfile() {
        val otherUsersProfile = OtherUsersProfile(
            currentPost!!.author.account.username, currentPost!!.author.account.username,
            currentPost!!.author.account.avatar.url, currentPost!!.author.owner
        )

        OtherUserProfileAccount.open(
            this,
            otherUsersProfile,
            currentPost!!.author.account.avatar.url,
            currentPost!!.author.owner
        )
    }

    private fun toggleCaptionExpansion() {
        // TODO: Implement expand/collapse caption functionality
        // You can use a custom TextView or library for this
    }

    private fun formatCount(count: Int): String {
        return when {
            count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
            count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
            else -> count.toString()
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onPause() {
        super.onPause()
        videoPlayerManager.pause()
        isPlaying = false
    }

    override fun onResume() {
        super.onResume()
        if (!isPlaying) {
            videoPlayerManager.play()
            isPlaying = true
            updateSeekBar()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        videoPlayerManager.release()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun likeUnlikeShot(event: ShortsLikeUnLike) {
        val TAG = "likeUnLikeShort"

        Log.d(TAG, "likeUnlikeShot ${event.postId}")

        lifecycleScope.launch {
            try {
                if (NetworkUtil.isConnected(this@PostDetailsActivity2)) {
                    val response = retrofitInstance.apiService.likeUnLikeShort(event.postId)
                    if (response.isSuccessful) {
                        Log.d(TAG, "Shot like successfully")
                    } else {
                        Log.d(TAG, "Error: ${response.message()}")
                    }
                } else {
                    showToast(this@PostDetailsActivity2, "Failed to connect try again...")
                }
            } catch (e: HttpException) {
                Log.d(TAG, "Http Exception ${e.message}")
            } catch (e: IOException) {
                Log.d(TAG, "IOException ${e.message}")
            }
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun favoriteUnFavoriteShort(event: ShortsFavoriteUnFavorite) {
        var TAG = "favoriteUnFavoriteShort"

        lifecycleScope.launch {
            try {
                if (NetworkUtil.isConnected(this@PostDetailsActivity2)) {
                    val response = retrofitInstance.apiService.favoriteShort(event.postId)
                    if (response.isSuccessful) {
                        Log.d(TAG, "Shot has been bookmarked")
                    } else {
                        Log.d(TAG, "Error: ${response.message()}")
                    }
                } else {
                    showToast(this@PostDetailsActivity2, "Failed to connect try again...")
                }

            } catch (e: HttpException) {
                Log.d(TAG, "Http Exception ${e.message}")
                runOnUiThread {
                    showToast(this@PostDetailsActivity2, "Failed to connect try again...")
                }
            } catch (e: IOException) {
                Log.d(TAG, "IOException ${e.message}")
            }
        }
    }

    private fun observeViewModel(post: Data) {

        shotPostViewModel.commentLiveData.observe(this) { commentState ->

            if (commentState.isReply) {
                processReplyComments(commentState.comment)
            } else {
                commentAdapter!!.submitItem(commentState.comment, 0)
                var commentCount = post.comments
                ++commentCount
                binding.commentsCount.text = commentCount.toString()
                post.comments = commentCount
                if (commentAdapter!!.itemCount == 1) {
                    updateUI(false)
                }
            }
        }
    }

    private fun processReplyComments(comment: Comment) {

        if (comment.contentType == "text") {
            val newReply = com.uyscuti.social.network.api.response.commentreply.allreplies.Comment(
                __v = comment.__v,
                _id = comment._id,
                author = getReliesAuthor(),
                content = comment.content!!,
                contentType = comment.contentType,
                createdAt = comment.createdAt,
                isLiked = false,
                likes = 0,
                commentId = commentId,
                updatedAt = comment.updatedAt,
            )

            commentToAddReplies?.replies?.add(0, newReply)

        } else if (comment.contentType == "image") {
            val newReply = com.uyscuti.social.network.api.response.commentreply.allreplies.Comment(
                __v = comment.__v,
                _id = comment._id,
                author = getReliesAuthor(),
                contentType = comment.contentType,
                createdAt = comment.createdAt,
                isLiked = false,
                likes = 0,
                commentId = commentId,
                updatedAt = comment.updatedAt,
                images = comment.images
            )

            commentToAddReplies?.replies?.add(0, newReply)

        } else if (comment.contentType == "gif") {
            val newReply = com.uyscuti.social.network.api.response.commentreply.allreplies.Comment(
                __v = comment.__v,
                _id = comment._id,
                author = getReliesAuthor(),
                contentType = comment.contentType,
                createdAt = comment.createdAt,
                isLiked = comment.isLiked,
                likes = comment.likes,
                commentId = commentId,
                updatedAt = comment.updatedAt,
                gifs = comment.gifs
            )

            commentToAddReplies?.replies?.add(0, newReply)

        } else if (comment.contentType == "video") {
            val newReply = com.uyscuti.social.network.api.response.commentreply.allreplies.Comment(
                __v = comment.__v,
                _id = comment._id,
                author = getReliesAuthor(),
                contentType = comment.contentType,
                createdAt = comment.createdAt,
                isLiked = comment.isLiked,
                likes = comment.likes,
                commentId = commentId,
                updatedAt = comment.updatedAt,
                duration = comment.duration,
                videos = comment.videos
            )

            commentToAddReplies?.replies?.add(0, newReply)
        } else if (comment.contentType == "audio") {
            val newReply = com.uyscuti.social.network.api.response.commentreply.allreplies.Comment(
                __v = comment.__v,
                _id = comment._id,
                author = getReliesAuthor(),
                contentType = comment.contentType,
                createdAt = comment.createdAt,
                isLiked = comment.isLiked,
                likes = comment.likes,
                commentId = commentId,
                updatedAt = comment.updatedAt,
                duration = comment.duration,
                audios = comment.audios,
                fileSize = comment.fileSize,
                fileType = comment.fileType,
                fileName = comment.fileName
            )

            commentToAddReplies?.replies?.add(0, newReply)
        } else if (comment.contentType == "docs") {
            val newReply = com.uyscuti.social.network.api.response.commentreply.allreplies.Comment(
                __v = comment.__v,
                _id = comment._id,
                author = getReliesAuthor(),
                contentType = comment.contentType,
                createdAt = comment.createdAt,
                isLiked = comment.isLiked,
                likes = comment.likes,
                commentId = commentId,
                updatedAt = comment.updatedAt,
                docs = comment.docs,
                fileSize = comment.fileSize,
                fileType = comment.fileType,
                fileName = comment.fileName,
                numberOfPages = comment.numberOfPages
            )

            commentToAddReplies?.replies?.add(0, newReply)
        }


        val replyCount = commentToAddReplies?.replyCount?.plus(1)
        commentToAddReplies?.replyCount = replyCount!!
        commentAdapter?.updateItem(commentPosition, commentToAddReplies)
    }

    private fun getReliesAuthor(): com.uyscuti.social.network.api.response.commentreply.allreplies.Author {
        val localSettings = getSharedPreferences("LocalSettings", MODE_PRIVATE)
        val profilePic = localSettings.getString("profile_pic", "").toString()

        val avatar = com.uyscuti.social.network.api.response.commentreply.allreplies.Avatar(
            "", "", url = profilePic
        )

        val account = com.uyscuti.social.network.api.response.commentreply.allreplies.Account(
            _id = "", avatar = avatar, "", LocalStorage.getInstance(this).getUsername()
        )
        val author =
            com.uyscuti.social.network.api.response.commentreply.allreplies.Author(
                _id = "21", account = account, firstName = "", lastName = ""
            )

        return author
    }

    private fun openImagePicker() {
        val intent = Intent(this, ImagesActivity::class.java)
        imagePickerLauncher.launch(intent)
    }

    private fun openDocPickerLauncher() {
        val intent = Intent(this, DocumentsActivity::class.java)
        docsPickerLauncher.launch(intent)
    }

    @SuppressLint("InflateParams")
    private fun showAttachmentDialog() {
        val dialog = BottomSheetDialog(this)

        val view = LayoutInflater.from(this).inflate(
            com.uyscuti.social.business.R.layout.file_upload_dialog,
            null
        )

        val video = view.findViewById<LinearLayout>(com.uyscuti.social.business.R.id.upload_video)
        val audio = view.findViewById<LinearLayout>(com.uyscuti.social.business.R.id.upload_audio)
        val image = view.findViewById<LinearLayout>(com.uyscuti.social.business.R.id.upload_image)
        val camera = view.findViewById<LinearLayout>(com.uyscuti.social.business.R.id.open_camera)
        val doc = view.findViewById<LinearLayout>(com.uyscuti.social.business.R.id.upload_doc)
        val location =
            view.findViewById<LinearLayout>(com.uyscuti.social.business.R.id.share_location)

        image!!.setOnClickListener {
            openImagePicker()
            dialog.dismiss()
        }

        video!!.setOnClickListener {
            val intent = Intent(this, VideosActivity::class.java)
            videoPickerLauncher.launch(intent)
            dialog.dismiss()
        }

        audio!!.setOnClickListener {
            val intent = Intent(this, AudioActivity::class.java)
            audioPickerLauncher.launch(intent)
            dialog.dismiss()
        }

        doc?.setOnClickListener {
            openDocPickerLauncher()
            dialog.dismiss()
        }

        camera!!.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            cameraLauncher.launch(intent)
            dialog.dismiss()
        }

        location?.visibility = View.INVISIBLE
        dialog.setContentView(view)
        dialog.show()

    }

    private fun updateRecordWaveProgress(progress: Float) {

        CoroutineScope(Dispatchers.Main).launch {
            binding.wave.progress = progress
//            currentComment?.progress = progress
            Log.d("updateWaveProgress", "updateWaveProgress: $progress")
        }
    }

    private val onRecordWaveRunnable = object : Runnable {
        override fun run() {
            try {
                if (!isOnRecordDurationOnPause) {
                    val currentPosition = player?.currentPosition?.toFloat()!!
                    updateRecordWaveProgress(currentPosition)
                }
                waveHandler.postDelayed(this, 20)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("Exception", "run: ${e.message}")
            }

        }
    }

    private val waveRunnable = object : Runnable {
        override fun run() {
//            Log.d("isDurationOnPause" , " in comment audio runnable isDurationOnPause is $isDurationOnPause")
            if (!isDurationOnPause) {
                val currentPosition = exoPlayer?.currentPosition?.toFloat()!!

                Log.d("ExoPlayerPosition", "Current Position: $currentPosition")

                waveProgress = currentPosition
                if (isReplyVnPlaying) {
                    commentAdapter!!.updateReplyWaveProgress(currentPosition, audioFormWave)
                } else {
                    commentAdapter!!.updateWaveProgress(currentPosition, wavePosition)
                }
                audioDurationTVCount.text = String.format(
                    "%s",
                    TrimVideoUtils.stringForTime(currentPosition)
                )
            }
            waveHandler.postDelayed(this, 20)
        }
    }

    private fun startWaveRunnable() {
        try {
            waveHandler.removeCallbacks(waveRunnable)
            waveHandler.post(waveRunnable)
            isDurationOnPause = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startRecordWaveRunnable() {
        try {
            waveHandler.removeCallbacks(onRecordWaveRunnable)
            waveHandler.post(onRecordWaveRunnable)
            isOnRecordDurationOnPause = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopRecordWaveRunnable() {
        try {
            waveHandler.removeCallbacks(onRecordWaveRunnable)
            isOnRecordDurationOnPause = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopPlayingVn() {
        binding.playVnAudioBtn.setImageResource(com.uyscuti.social.business.R.drawable.play_svgrepo_com)
        player?.release()
        player = null
        isAudioVNPlaying = false
        vnRecordAudioPlaying = false
        isOnRecordDurationOnPause = false
        stopRecordWaveRunnable()
        binding.wave.progress = 0F
        vnRecordProgress = 0
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun resumeRecordingVn() {
        if (isPaused) {
            isVnResuming = true
            startRecordingVn() // Start a new recording session, appending to the previous file
            binding.waveForm.visibility = View.VISIBLE
            binding.timerTv.visibility = View.VISIBLE
            binding.playAudioLayout.visibility = View.GONE
            binding.playVnAudioBtn.setImageResource(com.uyscuti.social.business.R.drawable.play_svgrepo_com)
            binding.recordVN.setImageResource(com.uyscuti.social.business.R.drawable.baseline_pause_black)
        }

    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun pauseRecordingVn() {
        val TAG = "pauseRecording"
//        firstTimeSendVn = true
        if (isRecording && !isPaused) {

            try {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
                mediaRecorder = null
            } catch (e: Exception) {
                Log.d(TAG, " failed to stop media recorder: $e")
                e.printStackTrace()
            }

            isPaused = true
            timer.pause() // Pause the recording timer
            binding.timerTv.visibility = View.INVISIBLE
            binding.waveForm.visibility = View.GONE
            binding.playAudioLayout.visibility = View.VISIBLE
            binding.playVnAudioBtn.setImageResource(com.uyscuti.social.business.R.drawable.play_svgrepo_com)
            binding.recordVN.setImageResource(com.uyscuti.social.business.R.drawable.mic_2)


            Log.d(TAG, "pauseRecording: list of recordings  size: ${recordedAudioFiles.size}")
            Log.d(TAG, "pauseRecording: list of recordings $recordedAudioFiles")

            mixVN()
        }
    }

    private fun startPlayingVn(vnAudio: String) {
        binding.playVnAudioBtn.setImageResource(com.uyscuti.social.business.R.drawable.baseline_pause_white_24)
        EventBus.getDefault().post(PauseShort(true))
//        player?.reset()
        isAudioVNPlaying = true
        vnRecordAudioPlaying = true

        isOnRecordDurationOnPause = false
        startRecordWaveRunnable()
        if (isAudioVNPaused) {
//            progressAnim.resume()
            Log.d("startPlaying", "(isAudioVNPaused)->vnRecordProgress $vnRecordProgress")

            if (vnRecordProgress != 0) {
                player?.seekTo(vnRecordProgress)
            }
            player?.start()
        } else {

            player = MediaPlayer().apply {
                try {
                    setDataSource(vnAudio)
//                inputStream.close()
                    prepare()
                    Log.d("startPlaying", "vnRecordProgress $vnRecordProgress")
                    if (vnRecordProgress != 0) {
                        player?.seekTo(vnRecordProgress)
                    }
                    start()
                    setOnCompletionListener {
                        // Playback completed, restart playback
                        isAudioVNPaused = false
                        stopPlayingVn()
                    }
                } catch (e: IOException) {
                    Log.e("MediaRecorder", "prepare() failed")
                }
            }

        }
    }

    private fun pauseVn(progress: Int) {
        Log.d("pauseVn", "vnRecordProgress $vnRecordProgress..... progress $progress")

        player?.pause()
        player?.seekTo(progress)
        isAudioVNPlaying = false
        isAudioVNPaused = true
        isOnRecordDurationOnPause = true

//        progressAnim.pause()
        binding.playVnAudioBtn.setImageResource(com.uyscuti.social.business.R.drawable.play_svgrepo_com)
    }

    @SuppressLint("DefaultLocale")
    private fun inflateWave(outputVN: String) {

//        outputVnFile = outputVN

        val TAG = "inflateWave"
        Log.d("playVnAudioBtn", "inflateWave: outputvn $outputVN")

        binding.wave.visibility = View.VISIBLE
        binding.playerTimerTv.visibility = View.VISIBLE
        Log.d(TAG, "render: does not start with http")
        //                audioDuration = 100L
        val file = File(outputVN)
        Log.d(TAG, "render: file $outputVN exists: ${file.exists()}")
        val locaAudioDuration = AudioDurationHelper.getLocalAudioDuration(outputVN)
        if (locaAudioDuration != null) {
            // Duration is available, do something with it
            //                    println("Audio duration: ${duration}ms")
            val minutes = (locaAudioDuration / 1000) / 60
            val seconds = (locaAudioDuration / 1000) % 60
            //                println("Audio duration: $minutes minutes $seconds seconds")
            binding.thirdTimerTv.text = String.format("%02d:%02d", minutes, seconds)
        } else {
            // File does not exist or error retrieving duration
//            println("Unable to retrieve audio duration.")
            Log.e(TAG, "render: failed to retrieve audio duration")

        }


        //                Log.d(TAG, "render: file $audioUrl can execute: ${file.canExecute()}")

//        binding.wave.setSampleFrom(audioFile)
        CoroutineScope(Dispatchers.IO).launch {
            WaveFormExtractor.getSampleFrom(applicationContext, outputVN) {

                CoroutineScope(Dispatchers.Main).launch {
//                    binding.wave.progress = 0F
//                    binding.wave.progress = currentItem.progress

                    if (locaAudioDuration != null) {
                        binding.wave.maxProgress = locaAudioDuration.toFloat()
                    }
                    binding.wave.setSampleFrom(it)

                    binding.wave.onProgressChanged = object : SeekBarOnProgressChanged {
                        override fun onProgressChanged(
                            waveformSeekBar: WaveformSeekBar,
                            progress: Float,
                            fromUser: Boolean
                        ) {
//                                    wave.progress = progress
                            binding.secondTimerTv.text = String.format(
                                "%s",
                                TrimVideoUtils.stringForTime(progress)
                            )

//                            currentItem.progress = progress

                            if (fromUser) {
                                if (vnRecordAudioPlaying) {
                                    pauseVn(progress = progress.toInt())
                                } else {
                                    vnRecordProgress = progress.toInt()
                                    Log.d("FromUser", "Scroll to this $progress")
                                }

                            }
                        }

                        override fun onRelease(event: MotionEvent?, progress: Float) {
                            if (outputVN.isNotEmpty()) {
//                                inflateWave(outputVN)
                                if (vnRecordAudioPlaying) {
                                    Log.d(
                                        "onRelease",
                                        "vnRecordAudioPlaying $isAudioVNPlaying progress $progress"
                                    )
                                    vnRecordProgress = progress.toInt()
                                    startPlayingVn(outputVN)
                                } else {
                                    Log.d("onRelease", "Start playing from this progress $progress")
                                    vnRecordProgress = progress.toInt()
                                }

                            } else {
                                Log.d("onRelease", "output vn is empty")
                            }
                        }
                    }
                }
            }
        }

    }

    private fun mixVN() {
        val TAG = "mixVN"
        try {
            wasPaused = true
            Log.d(TAG, "pauseRecording: outputFile: $outputVnFile")

            val audioMixer = AudioMixer(outputVnFile)

            for (input in recordedAudioFiles) {
                val ai = GeneralAudioInput(input)
                audioMixer.addDataSource(ai)
            }
            audioMixer.mixingType = AudioMixer.MixingType.SEQUENTIAL

            audioMixer.setProcessingListener(object : AudioMixer.ProcessingListener {
                override fun onProgress(progress: Double) {
                    // Not used in this example, but you can handle progress updates if needed
                }

                override fun onEnd() {
                    runOnUiThread {
                        audioMixer.release()
                        mixingCompleted = true // Set the flag to indicate mixing is completed
                        // Additional code as needed
                        val file = File(outputVnFile)
                        Log.d(TAG, "onEnd: output vn file exists ${file.exists()}")
                        Log.d(TAG, "onEnd: media muxed success")

                        inflateWave(outputVnFile)

                        binding.playVnAudioBtn.setOnClickListener {
                            Log.d("playVnAudioBtn", "onEnd: play vn button clicked")
                            when {
                                !isAudioVNPlaying -> {
                                    binding.playVnAudioBtn.setImageResource(com.uyscuti.social.business.R.drawable.baseline_pause_black)
                                    Log.d(
                                        "playVnAudioBtn",
                                        "play vn"
                                    )
                                    startPlayingVn(outputVnFile)
                                }

                                else -> {
                                    Log.d(
                                        "playVnAudioBtn",
                                        "pause VN"
                                    )
                                    binding.playVnAudioBtn.setImageResource(com.uyscuti.social.business.R.drawable.play_svgrepo_com)
                                    vnRecordAudioPlaying = true
                                    pauseVn(vnRecordProgress)
                                }
                            }
                        }
                    }
                }
            })

            try {
                audioMixer.start()
                audioMixer.processAsync()
            } catch (e: IOException) {
                audioMixer.release()
                e.printStackTrace()
                Log.d(TAG, "pauseRecording: exception 1 $e")
                Log.d(TAG, "pauseRecording: exception 1 ${e.message}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, "pauseRecording: exception 2 $e")
            Log.d(TAG, "pauseRecording: exception 2 ${e.message}")
        }
    }

    private fun startRecordingVn() {

        Log.d("StartRecording", "startRecoding vn called")
        try {

            if (player?.isPlaying == true) {
                stopPlayingVn()
            }

            binding.playerTimerTv.visibility = View.GONE
            outputFile = getOutputFilePath("rec")
            outputVnFile = getOutputFilePath("mix")
            wasPaused = false

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setOutputFile(outputFile)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

//                setAudioSource(MediaRecorder.AudioSource.MIC)
//                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
//                setOutputFile(outputFile)
//                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

                prepare()
                start()
            }

            isRecording = true
            isPaused = false
            isVnResuming = false
            binding.recordVN.setImageResource(com.uyscuti.social.business.R.drawable.baseline_pause_white_24)
            binding.sendVN.setBackgroundResource(com.uyscuti.social.business.R.drawable.ic_ripple)
            binding.deleteVN.setBackgroundResource(com.uyscuti.social.business.R.drawable.ic_ripple)
            timer.start()

            binding.deleteVN.isClickable = true
            binding.sendVN.isClickable = true
            recordedAudioFiles.add(outputFile)

            Log.d("VNFile", outputFile)

        } catch (e: Exception) {
            Log.d("VNFile", "Failed to record audio properly")
            e.printStackTrace()
        }
    }

    private fun deleteRecording() {

        val TAG = "Recording"

        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            isPaused = false
            isAudioVNPlaying = false

            binding.timerTv.text = "00:00.00"
            binding.secondTimerTv.visibility = View.GONE
            binding.thirdTimerTv.visibility = View.GONE
//            binding.recordVN.setImageResource(R.drawable.baseline_pause_24)
            binding.recordVN.setImageResource(com.uyscuti.social.business.R.drawable.mic_2)


            binding.sendVN.setBackgroundResource(com.uyscuti.social.business.R.drawable.ic_ripple_disabled)
            binding.sendVN.isClickable = false

            amplitudes = binding.waveForm.clear()
            amps = 0
            timer.stop()
            Log.d("TAG", "deleteRecording: recorded files size ${recordedAudioFiles.size}")
            deleteVn()
//            if()
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle exceptions as needed
        }
    }

    private fun deleteVn() {
        recordedAudioFiles.clear()
//        if (recordedAudioFiles.isNotEmpty()) {
        val isDeleted = deleteFiles(recordedAudioFiles)
        var outputVnFileList = mutableListOf<String>()
        outputVnFileList.add(outputVnFile)
        val deleteMixVn = deleteFiles(outputVnFileList)
        if (isDeleted) {
            Log.d(TAG, "File record deleted successfully")
        } else {
            println("Failed to delete file.")
        }

        if (deleteMixVn) {
            Log.d(TAG, "File mix vn deleted successfully")
        } else {
            println("Failed to delete file.")
        }
//        }
    }

    private fun stopRecordingAndSendVn() {

        try {

            if (mediaRecorder != null) {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
                mediaRecorder = null
            }
            isRecording = false
            isPaused = false

            binding.timerTv.text = "00:00.00"
            binding.recordVN.setImageResource(com.uyscuti.social.business.R.drawable.ic_mic_on)
            binding.sendVN.setBackgroundResource(com.uyscuti.social.business.R.drawable.ic_ripple_disabled)
            binding.sendVN.isClickable = false

            amplitudes = binding.waveForm.clear()
            amps = 0
            timer.stop()
            if (player?.isPlaying == true) {
                stopPlayingVn()
            }
            binding.VnLayout.visibility = View.GONE

            // Add any UI changes or notifications indicating recording has stopped
            binding.secondTimerTv.text = " 00:00"
            binding.thirdTimerTv.text = "00:00"
            binding.thirdTimerTv.visibility = View.GONE
            binding.secondTimerTv.visibility = View.GONE
            binding.replyToLayout.visibility = View.GONE


            if (!isReply) {

                if (recordedAudioFiles.size != 1) {
                    uploadAudioComment(outputVnFile, isReply1 = isReply, fileType = "vnAudio")
                } else {
                    uploadAudioComment(outputVnFile, isReply1 = isReply, fileType = "vnAudio")
                }
            } else {
                if (recordedAudioFiles.size != 1) {
                    uploadAudioComment(outputVnFile, isReply1 = isReply, fileType = "vnAudio")
                } else {
                    uploadAudioComment(outputVnFile, isReply1 = isReply, fileType = "vnAudio")
                }
            }


        } catch (e: Exception) {
            e.printStackTrace()
            // Handle exceptions as needed
        }
    }


    private fun commentAudioStartPlaying(
        audio: String,
        audioPlayPauseBtn: ImageView,
        progress: Float,
        position: Int
    ) {

        EventBus.getDefault().post(PauseShort(true))
        isDurationOnPause = false

        if (isVnAudioToPlay) {
            startWaveRunnable()
        }

        audioPlayPauseBtn.setImageResource(com.uyscuti.social.business.R.drawable.baseline_pause_black)

        try {
            val file = File(audio)

            if (file.exists()) {
                // Local file playback
                val fileUrl = Uri.fromFile(file)
                exoPlayer = ExoPlayer.Builder(this).build()

                Log.d("commentAudioStartPlaying", "commentAudioStartPlaying: Local file $fileUrl")

                val localFileUri = Uri.parse(fileUrl.toString())
                val mediaItem = MediaItem.fromUri(localFileUri)
                exoPlayer!!.setMediaItem(mediaItem)
            } else {
                // Server file playback
                Log.d("commentAudioStartPlaying", "commentAudioStartPlaying: server file $audio")

                val audioUri = Uri.parse(audio)
                Log.d("commentAudioStartPlaying", "audioUri $audioUri")
                val mediaItem = MediaItem.fromUri(audioUri)

                // Try playing with cache first
                try {
                    exoPlayer = buildExoPlayerWithCache(mediaItem)
                    Log.d("commentAudioStartPlaying", "Using cached playback")
                } catch (cacheException: Exception) {
                    Log.e(
                        "commentAudioStartPlaying",
                        "Cache error, clearing and retrying",
                        cacheException
                    )

                    // Clear corrupted cache
                    clearExoPlayerCache()

                    // Retry with fresh cache
                    try {
                        exoPlayer = buildExoPlayerWithCache(mediaItem)
                        Log.d("commentAudioStartPlaying", "Cache cleared, using fresh cache")
                    } catch (retryException: Exception) {
                        Log.e(
                            "commentAudioStartPlaying",
                            "Cache still failing, playing directly from server",
                            retryException
                        )

                        // Fallback to direct server playback without cache
                        exoPlayer = buildExoPlayerWithoutCache(mediaItem)
                        Log.d(
                            "commentAudioStartPlaying",
                            "Playing directly from server without cache"
                        )
                    }
                }
            }

            exoPlayer!!.prepare()
            exoPlayer!!.seekTo(progress.toLong())
            exoPlayer!!.playWhenReady = true
            exoPlayer!!.repeatMode = Player.REPEAT_MODE_OFF
            exoPlayer!!.addListener(playbackStateListener())
            exoPlayer!!.addListener(object : Player.Listener {
                @Deprecated("Deprecated in Java")
                override fun onPlayerStateChanged(
                    playWhenReady: Boolean,
                    playbackState: Int
                ) {
                    if (playbackState == Player.STATE_READY && exoPlayer!!.duration != C.TIME_UNSET) {
                        // Player ready
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    error.printStackTrace()

                    // Check if error is cache-related
                    if (isCacheError(error)) {
                        Log.e("commentAudioStartPlaying", "Cache error detected during playback")
                        clearExoPlayerCache()

                        // Retry without cache
                        try {
                            exoPlayer?.release()
                            exoPlayer = buildExoPlayerWithoutCache(MediaItem.fromUri(audio))
                            exoPlayer!!.prepare()
                            exoPlayer!!.seekTo(progress.toLong())
                            exoPlayer!!.playWhenReady = true
                            exoPlayer!!.addListener(playbackStateListener())
                            Log.d("commentAudioStartPlaying", "Retrying playback without cache")
                        } catch (e: Exception) {
                            showToast(this@PostDetailsActivity2, "Can't play this audio")
                        }
                    } else {
                        showToast(this@PostDetailsActivity2, "Can't play this audio")
                    }
                }
            })

            if (isReplyVnPlaying) {
                val handler = Handler()
                handler.postDelayed({
                    commentAdapter?.refreshMainComment(position)
                }, 200)
            }

        } catch (e: Exception) {
            Log.d("commentAudioStartPlaying", "commentAudioStartPlaying: error: ${e.message}")
            e.printStackTrace()
            showToast(this, "Error playing audio")
        }
    }

    // Helper method to build ExoPlayer with cache
    private fun buildExoPlayerWithCache(mediaItem: MediaItem): ExoPlayer {
        httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)

        defaultDataSourceFactory = DefaultDataSourceFactory(
            this, httpDataSourceFactory
        )

        cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(simpleCache!!)
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        val mediaSourceFactory: MediaSource.Factory =
            DefaultMediaSourceFactory(this)
                .setDataSourceFactory(cacheDataSourceFactory)

        val player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()

        val mediaSource = ProgressiveMediaSource.Factory(cacheDataSourceFactory)
            .createMediaSource(mediaItem)

        player.setMediaSource(mediaSource)
        return player
    }

    // Helper method to build ExoPlayer without cache (direct server playback)
    private fun buildExoPlayerWithoutCache(mediaItem: MediaItem): ExoPlayer {
        val directHttpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(30000)
            .setReadTimeoutMs(30000)

        val player = ExoPlayer.Builder(this).build()

        val mediaSource = ProgressiveMediaSource.Factory(directHttpDataSourceFactory)
            .createMediaSource(mediaItem)

        player.setMediaSource(mediaSource)
        return player
    }

    // Helper method to check if error is cache-related
    private fun isCacheError(error: PlaybackException): Boolean {
        val cause = error.cause
        return cause is IllegalStateException ||
                cause?.cause is IllegalStateException ||
                error.message?.contains("cache", ignoreCase = true) == true ||
                cause?.message?.contains("SimpleCache", ignoreCase = true) == true
    }

    // Helper method to clear ExoPlayer cache
    private fun clearExoPlayerCache() {
        try {
            simpleCache?.release()

            val exoPlayerCacheDir = File(cacheDir, "exoplayer")
            if (exoPlayerCacheDir.exists()) {
                exoPlayerCacheDir.deleteRecursively()
                Log.d("clearExoPlayerCache", "Cache cleared successfully")
            }

            // Reinitialize cache
            val leastRecentlyUsedCacheEvictor =
                LeastRecentlyUsedCacheEvictor(1024 * 1024 * 1024) // 1GB
            val exoDatabaseProvider = ExoDatabaseProvider(this)
            exoPlayerCacheDir.mkdirs()
            simpleCache =
                SimpleCache(exoPlayerCacheDir, leastRecentlyUsedCacheEvictor, exoDatabaseProvider)

            Log.d("clearExoPlayerCache", "Cache reinitialized")
        } catch (e: Exception) {
            Log.e("clearExoPlayerCache", "Error clearing cache", e)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun audioWave(event: AudioPlayerHandler) {

        val TAG = "audioWave"

        audioFormWave = event.audioWave
//        event.audioWave.setSampleFrom(event.audioPath)
        audioDurationTVCount = event.leftDuration
        wavePosition = event.position
        Log.d(TAG, "audioWave: position $wavePosition ")


    }

    private fun commentAudioStop() {
        Log.d(
            "TAG",
            "commentAudioStop: Comment audio completed playing player is playing ${player?.isPlaying}"
        )

        Log.d("isDurationOnPause", " in comment audio stop isDurationOnPause is $isDurationOnPause")

        Log.d("commentAudioStop", "commentAudioStop: was reply playing $isReplyVnPlaying")

        if (isVnAudioToPlay) {
            if (::audioFormWave.isInitialized) {
                audioFormWave.progress = 0f
            }
            commentAdapter?.setSecondWaveFormProgress(0f, currentCommentAudioPosition)
            commentAdapter?.setReplySecondWaveFormProgress(0f, currentCommentAudioPosition)
        } else {
            commentAdapter?.setSecondSeekBarProgress(0f, currentCommentAudioPosition)
            commentAdapter?.setReplySecondSeekBarProgress(0f, currentCommentAudioPosition)
        }


        currentCommentAudioPosition = RecyclerView.NO_POSITION
        currentCommentAudioPath = ""
        commentAdapter?.resetAudioPlay()

        exoPlayer?.let { exoPlayer ->
            if (exoPlayer.isPlaying) {
                exoPlayer.stop()
            }
        }
    }


    private fun initializeSeekBar(exoPlayer: ExoPlayer) {
        audioSeekBar.max = exoPlayer.duration.toInt()
// Remove callbacks from the current handler, if any
        currentHandler?.removeCallbacksAndMessages(currentHandler)
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable {
            override fun run() {
                try {
                    Log.d(
                        "initializeSeekBar",
                        "Position $currentCommentAudioPosition is reply $isReplyVnPlaying"
                    )
                    if (!isVnAudioToPlay && exoPlayer.isPlaying) {

                        exoPlayer.let {
                            if (isReplyVnPlaying) {
                                commentAdapter!!.updateReplySeekBarProgress(
                                    it.currentPosition.toFloat(),
                                    audioSeekBar
                                )
                            } else {

                                CoroutineScope(Dispatchers.Main).launch {
                                    audioSeekBar.progress = it.currentPosition.toInt()
                                    seekBarProgress = it.currentPosition.toFloat()
                                    commentAdapter!!.setSecondSeekBarProgress(
                                        seekBarProgress,
                                        currentCommentAudioPosition
                                    )
                                    audioDurationTVCount.text = String.format(
                                        "%s",
                                        TrimVideoUtils.stringForTime(it.currentPosition.toFloat())
                                    )
                                }

                            }

                            handler.postDelayed(this, 1000)

                        }
                    }
                } catch (e: Exception) {
                    audioSeekBar.progress = 0
                    e.printStackTrace()
                }
            }
        }, 0)
        // Set the new handler as the current handler
        currentHandler = handler
    }


    private fun stopWaveRunnable() {
        try {
            waveHandler.removeCallbacks(waveRunnable)
            isDurationOnPause = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun commentAudioPause(audioPlayPauseBtn: ImageView, isReply: Boolean) {
        Log.d("TAG", "commentAudioPause: is Reply $isReply")
        isDurationOnPause = true

        Log.d(
            "isDurationOnPause",
            " in comment audio pause isDurationOnPause is $isDurationOnPause"
        )

        audioPlayPauseBtn.setImageResource(com.uyscuti.social.business.R.drawable.play_svgrepo_com)
        commentAdapter!!.updatePlaybackButton(
            currentCommentAudioPosition,
            isReply,
            audioPlayPauseBtn
        )
        exoPlayer?.pause()
    }

    private fun playbackStateListener() = object : Player.Listener {
        @SuppressLint("SetTextI18n")
        override fun onPlaybackStateChanged(state: Int) {
            when (state) {
                ExoPlayer.STATE_ENDED -> {
//                     The video playback ended. Move to the next video if available.
                    Log.d(
                        "playbackStateListener",
                        "commentAudioStartPlaying: comment audio completed"
                    )
//                    audioPlayPauseBtn.setImageResource(R.drawable.play_svgrepo_com)
                    if (isVnAudioToPlay) {
                        if (::audioDurationTVCount.isInitialized) {
                            audioDurationTVCount.text = "00:00"
                            commentAdapter?.updateReplyWaveProgress(0f, audioFormWave)
                            if (isReplyVnPlaying) {
                                Log.d("isReplyVnPlaying", "isReplyVnPlaying $isReplyVnPlaying")
                                val handler = Handler()

                                handler.postDelayed({
                                    commentAdapter?.refreshMainComment(position)
                                }, 200)
                            } else {
                                Log.d("isReplyVnPlaying", "isReplyVnPlaying $isReplyVnPlaying")
                            }
                        }
                    }

                    if (isVnAudioToPlay) {
                        audioFormWave.progress = 0f
                    } else {
                        if (::audioDurationTVCount.isInitialized) {
                            audioDurationTVCount.text = "00:00"
                        }
                        audioSeekBar.progress = 0
                        commentAdapter?.refreshAudioComment(currentCommentAudioPosition)
                    }

                    Log.d(
                        "audioSeekBar",
                        "currentCommentAudioPosition $currentCommentAudioPosition"
                    )

                    commentAdapter?.refreshMainComment(position)
                    commentAdapter?.changePlayingStatus()
//                    adapter?.resetWaveForm()
//                    adapter?.notifyDataSetChanged()
                    if (isVnAudioToPlay) {
                        stopWaveRunnable()

                    }
                    commentAudioStop()
                }
                // Add other cases if needed
                Player.STATE_BUFFERING -> {

                }

                Player.STATE_IDLE -> {
                }

                Player.STATE_READY -> {
                    if (!isVnAudioToPlay) {
                        exoPlayer?.let { initializeSeekBar(it) }
                    }
                    Log.d("TAG", "STATE_READY")
//                    startUpdatingSeekBar()
//                    shortsAdapter.setSeekBarProgress(exoPlayer!!.currentPosition.toInt())

                }

                else -> {
                    Log.d("TAG", "STOP SEEK BAR")
                    // Stop updating seek bar in other states
//                    stopUpdatingSeekBar()
                }
            }
        }

        override fun onIsPlayingChanged(isVideoPlaying: Boolean) {
//        super.onIsPlayingChanged(isPlaying)

        }

        override fun onEvents(player: Player, events: Player.Events) {
//        super.onEvents(player, events)
            if (events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED) ||
                events.contains(Player.EVENT_IS_PLAYING_CHANGED)
            ) {

//                progressBar.visibility = View.GONE
            }

            if (events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION)
            ) {
//                player.seekTo(5000L)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun commentAudioSeekBar(event: CommentAudioPlayerHandler) {
        val TAG = "commentAudioSeekBar"
        audioSeekBar = event.audioSeekBar
//        event.audioWave.setSampleFrom(event.audioPath)
        audioDurationTVCount = event.leftDuration
        seekPosition = event.position
        maxDuration = event.maxDuration
        Log.d(TAG, "commentAudioSeekBar: position $wavePosition ")
    }

    override fun onViewRepliesClick(
        data: Comment,
        repliesRecyclerView: RecyclerView,
        position: Int
    ) {

    }

    override fun onViewRepliesClick(
        data: Comment,
        position: Int,
        commentRepliesTV: TextView,
        hideCommentReplies: TextView,
        repliesRecyclerView: RecyclerView,
        isRepliesVisible: Boolean,
        page: Int
    ) {
        lifecycleScope.launch {

            if (data.hasNextPage) {

                withContext(Dispatchers.Main) {
                    commentRepliesTV.text = "Loading..."
                }


                if (commentRepliesTV.text.equals("Loading...")) {

                    withContext(Dispatchers.Main) {
                        hideCommentReplies.visibility = View.GONE
                    }

                    withContext(Dispatchers.Main) {
                        commentRepliesTV.visibility = View.GONE
                        hideCommentReplies.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    override fun toggleAudioPlayer(
        audioPlayPauseBtn: ImageView,
        audioToPlayPath: String,
        position: Int,
        isReply: Boolean,
        progress: Float,
        isSeeking: Boolean,
        seekTo: Boolean,
        isVnAudio: Boolean
    ) {
        isReplyVnPlaying = isReply
        isVnAudioToPlay = isVnAudio

        wavePosition = position
        currentCommentAudioPosition = position

        if (currentCommentAudioPath == audioToPlayPath) {

            if (seekTo) {
                Log.d("SeekTo", "Seek to $progress")
                EventBus.getDefault().post(PauseShort(true))
                isDurationOnPause = false
                exoPlayer?.seekTo(progress.toLong())
                exoPlayer?.play()
            } else if (isSeeking) {
                Log.d("toggleAudioPlayer", "user is seeking so i paused the audio")
                exoPlayer?.pause()
            } else if (exoPlayer?.isPlaying == true) {
                Log.d(
                    "toggleAudioPlayer",
                    "toggleAudioPlayer: current player is playing then pause"
                )

                if (isVnAudio) {
                    Log.d("waveProgress", "toggleAudioPlayer: $waveProgress")

                    commentAdapter?.setReplySecondWaveFormProgress(waveProgress, position)
                    commentAdapter?.setSecondWaveFormProgress(waveProgress, position)
                } else {
                    //for seek bar
                    commentAdapter?.setSecondSeekBarProgress(seekBarProgress, position)
                    commentAdapter?.setReplySecondSeekBarProgress(seekBarProgress, position)
                }
                exoPlayer?.pause()
                isDurationOnPause = true

            } else {
                Log.d(
                    "toggleAudioPlayer",
                    "toggleAudioPlayer: current player is not playing then play"
                )
                EventBus.getDefault().post(PauseShort(true))
                isDurationOnPause = false
                exoPlayer?.seekTo(progress.toLong())
                exoPlayer?.play()
            }
        } else {

            if (exoPlayer?.isPlaying == true) {
                Log.d("toggleAudioPlayer", "toggleAudioPlayer: in else player is playing")
                commentAudioPause(audioPlayPauseBtn, isReply)
            }

            commentAudioStartPlaying(audioToPlayPath, audioPlayPauseBtn, progress, position)
            currentCommentAudioPath = audioToPlayPath
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onReplyButtonClick(
        position: Int,
        data: Comment,
        isMainComment: Boolean
    ) {

        commentToAddReplies = data
        commentPosition = commentAdapter!!.findCommentPosition(data._id)
        var username = ""
        isReply = true

        if (isMainComment) {
            username = data.author!!.account.username
            binding.replyToLayout.visibility = View.VISIBLE

            binding.replyToTextView.text = "Replying to $username"
            commentId = data._id
        } else {

            username = data.replies[position].author!!.account.username

            binding.replyToLayout.visibility = View.VISIBLE

            binding.replyToTextView.text = "Replying to $username"
            commentId = data.replies[position]._id
        }

        binding.input.inputEditText.setText("@$username")
        binding.input.inputEditText.setSelection(binding.input.inputEditText.text!!.length)

        binding.exitReply.setOnClickListener {
            binding.replyToLayout.visibility = View.GONE
            binding.input.inputEditText.setText("")
            isReply = false
        }
    }

    override fun likeUnLikeComment(
        position: Int,
        data: Comment
    ) {
        if (com.uyscuti.sharedmodule.utils.NetworkUtil.isConnected(this)) {
            val updatedComment = if (data.isLiked) {
                data.copy(
                    likes = data.likes + 1,
                )
            } else {
                data.copy(
                    likes = data.likes - 1,
                )
            }
            commentAdapter?.updateItem(position, updatedComment)
            shotPostViewModel.likeUnlikeShotComment(data._id)
        } else {
            showToast(this, "Like failed. No internet access.")
        }
    }

    override fun likeUnlikeCommentReply(
        replyPosition: Int,
        replyData: com.uyscuti.social.network.api.response.commentreply.allreplies.Comment,
        mainCommentPosition: Int,
        mainComment: Comment
    ) {
        if (com.uyscuti.sharedmodule.utils.NetworkUtil.isConnected(this)) {
            if (replyData.isLiked) {
                replyData.copy(
                    likes = replyData.likes + 1
                )
            } else {
                replyData.copy(
                    likes = replyData.likes - 1
                )
            }
            mainComment.replies[replyPosition] = replyData

            commentAdapter?.updateItem(mainCommentPosition, mainComment)
            shotPostViewModel.likeUnlikeShotCommentReplies(replyData._id)

        } else {
            showToast(this, "Like failed. No internet access.")
        }

    }

    override fun onSubmit(input: CharSequence?): Boolean {
        hideKeyboard(binding.input.inputEditText)
        val localUpdateId = generateRandomId()

        if (!isReply) {
            shotPostViewModel.addComment(
                postId,
                input.toString(),
                "text",
                localUpdateId
            )

        } else {
            shotPostViewModel.addCommentReply(
                commentId,
                input.toString(),
                "text",
                localUpdateId,
                isReply = isReply
            )

            isReply = false
        }


        return true
    }

    override fun onAddEmoji() {
        initEmojiView()
    }

    override fun onAddVoiceNote() {
        binding.VnLayout.visibility = View.VISIBLE
        binding.playAudioLayout.visibility = View.GONE
        binding.waveForm.visibility = View.VISIBLE
        binding.timerTv.visibility = View.VISIBLE
    }

    override fun onAddGif() {
        val intent = Intent(this, GifActivity::class.java)
        gifsPickerLauncher.launch(intent)
    }

    override fun onAddAttachments() {
        showAttachmentDialog()
    }

    override fun onTimerTick(duration: String) {
        binding.timerTv.text = duration

        var amplitude = mediaRecorder!!.maxAmplitude.toFloat()
        amplitude = if (amplitude > 0) amplitude else 130f

        binding.waveForm.addAmplitude(amplitude)
    }

}








