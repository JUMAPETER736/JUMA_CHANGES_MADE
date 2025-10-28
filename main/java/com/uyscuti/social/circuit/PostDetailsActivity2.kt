package com.uyscuti.social.circuit

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.AnimationDrawable
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.util.Log
import android.util.TypedValue
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSourceFactory
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.tom_roush.pdfbox.pdmodel.PDDocument

import com.uyscuti.social.circuit.adapter.OnClickListeners
import com.uyscuti.social.circuit.adapter.OnCommentsClickListener

import com.uyscuti.social.circuit.calls.viewmodel.CallViewModel

import com.uyscuti.social.circuit.data.model.shortsmodels.CommentReplyResults
import com.uyscuti.social.circuit.model.AudioPlayerHandler
import com.uyscuti.social.circuit.model.CleanCache
import com.uyscuti.social.circuit.model.CommentAudioPlayerHandler
import com.uyscuti.social.circuit.model.LikeCommentReply
import com.uyscuti.social.circuit.model.PausePlayEvent
import com.uyscuti.social.circuit.model.PauseShort
import com.uyscuti.social.circuit.model.ShortAdapterNotifyDatasetChanged
import com.uyscuti.social.circuit.model.ShortsFavoriteUnFavorite
import com.uyscuti.social.circuit.model.ShortsLikeUnLike
import com.uyscuti.social.circuit.model.ShortsLikeUnLike2
import com.uyscuti.social.circuit.model.ShortsViewModel
import com.uyscuti.social.circuit.model.ToggleReplyToTextView
import com.uyscuti.social.circuit.model.UserProfileShortsViewModel
import com.uyscuti.social.circuit.presentation.DialogViewModel
import com.uyscuti.social.circuit.presentation.GroupDialogViewModel
import com.uyscuti.social.circuit.presentation.MessageViewModel
import com.uyscuti.social.circuit.service.VideoPreLoadingService
import com.uyscuti.social.circuit.User_Interface.OtherImportantProfileThings.GifActivity
import com.uyscuti.social.circuit.User_Interface.Log_In_And_Register.LoginActivity
import com.uyscuti.social.circuit.User_Interface.OtherImportantProfileThings.SearchShortActivity
import com.uyscuti.social.circuit.User_Interface.OtherImportantProfileThings.SettingsActivity
import com.uyscuti.social.circuit.User_Interface.shorts.UniqueIdGenerator
import com.uyscuti.social.circuit.User_Interface.shorts.getFileSize
import com.uyscuti.social.circuit.User_Interface.uploads.AudioActivity
import com.uyscuti.social.circuit.User_Interface.uploads.CameraActivity
import com.uyscuti.social.circuit.User_Interface.uploads.DocumentsActivity
import com.uyscuti.social.circuit.User_Interface.uploads.VideosActivity
import com.uyscuti.social.circuit.utils.AudioDurationHelper
import com.uyscuti.social.circuit.utils.AudioDurationHelper.getFormattedDuration
import com.uyscuti.social.circuit.utils.AudioDurationHelper.reverseFormattedDuration
import com.uyscuti.social.circuit.utils.COMMENT_VIDEO_CODE
import com.uyscuti.social.circuit.utils.Constants
import com.uyscuti.social.circuit.utils.GIF_CODE
import com.uyscuti.social.circuit.utils.PathUtil
import com.uyscuti.social.circuit.utils.R_CODE
import com.uyscuti.social.circuit.utils.Timer
import com.uyscuti.social.circuit.utils.TrimVideoUtils
import com.uyscuti.social.circuit.utils.WaveFormExtractor
import com.uyscuti.social.circuit.utils.audio_compressor.FFMPEG_AudioCompressor
import com.uyscuti.social.circuit.utils.createMultipartBody
import com.uyscuti.social.circuit.utils.deleteFiled
import com.uyscuti.social.circuit.utils.deleteFiles
import com.uyscuti.social.circuit.utils.extractThumbnailFromVideo
import com.uyscuti.social.circuit.utils.fileType
import com.uyscuti.social.circuit.utils.formatFileSize
import com.uyscuti.social.circuit.utils.generateRandomId
import com.uyscuti.social.circuit.utils.getFileNameFromLocalPath
import com.uyscuti.social.circuit.utils.getOutputFilePath
import com.uyscuti.social.circuit.utils.isFileExists
import com.uyscuti.social.circuit.utils.isFileSizeGreaterThan2MB
import com.uyscuti.social.circuit.utils.waveformseekbar.SeekBarOnProgressChanged
import com.uyscuti.social.circuit.utils.waveformseekbar.WaveformSeekBar
import com.uyscuti.social.circuit.viewmodels.NotificationCountViewModel
import com.uyscuti.social.circuit.viewmodels.comments.CommentsViewModel
import com.uyscuti.social.circuit.viewmodels.comments.RoomCommentFilesViewModel
import com.uyscuti.social.circuit.viewmodels.comments.RoomCommentReplyViewModel
import com.uyscuti.social.circuit.viewmodels.comments.RoomCommentsViewModel
import com.uyscuti.social.chatsuit.messages.CommentsInput
import com.uyscuti.social.circuit.adapter.CommentsRecyclerViewAdapter
import com.uyscuti.social.circuit.adapter.OnViewRepliesClickListener
import com.uyscuti.social.circuit.adapter.notifications.AdPaginatedAdapter
import com.uyscuti.social.circuit.data.model.Comment
import com.uyscuti.social.circuit.data.model.Message
import com.uyscuti.social.circuit.data.model.User
import com.uyscuti.social.circuit.databinding.ActivityPostDetails2Binding
import com.uyscuti.social.circuit.utils.AndroidUtil.showToast
import com.uyscuti.social.circuit.utils.audiomixer.AudioMixer
import com.uyscuti.social.circuit.utils.audiomixer.input.GeneralAudioInput
import com.uyscuti.social.circuit.viewmodels.comments.ShortCommentReplyViewModel
import com.uyscuti.social.circuit.viewmodels.comments.ShortCommentsViewModel
import com.uyscuti.social.compressor.VideoCompressor
import com.uyscuti.social.compressor.VideoQuality
import com.uyscuti.social.compressor.config.Configuration
import com.uyscuti.social.compressor.config.SharedStorageConfiguration
import com.uyscuti.social.core.common.data.room.entity.CommentsFilesEntity
import com.uyscuti.social.core.common.data.room.entity.ShortsEntity
import com.uyscuti.social.core.common.data.room.entity.UserShortsEntity
import com.uyscuti.social.core.common.data.room.repository.ProfileRepository
import com.uyscuti.social.compressor.CompressionListener
import com.uyscuti.social.compressor.config.*
import com.uyscuti.social.network.api.response.comment.allcomments.Account
import com.uyscuti.social.network.api.response.comment.allcomments.Author
import com.uyscuti.social.network.api.response.comment.allcomments.Avatar
import com.uyscuti.social.network.api.response.comment.allcomments.CommentFiles
import com.uyscuti.social.network.api.response.post.GetPostById
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.twitter.TwitterEmojiProvider
import com.vanniktech.ui.Color

import id.zelory.compressor.Compressor
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.poi.hwpf.HWPFDocument
import org.apache.poi.hwpf.usermodel.Range
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.HttpException

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Collections
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.abs
import kotlin.properties.Delegates
import kotlin.random.Random

import com.uyscuti.social.medialoader.MediaLoader
import com.uyscuti.social.medialoader.DefaultConfigFactory
import com.uyscuti.social.medialoader.DownloadManager
import com.uyscuti.social.medialoader.MediaLoaderConfig
import com.uyscuti.social.medialoader.data.file.naming.Md5FileNameCreator

import com.uyscuti.social.core.common.data.room.entity.ShortCommentEntity
import com.uyscuti.social.core.common.data.room.entity.ShortCommentReply

@UnstableApi
@AndroidEntryPoint
class PostDetailsActivity2 : AppCompatActivity(),
    CommentsInput.EmojiListener, OnCommentsClickListener,
    CommentsInput.VoiceListener, CommentsInput.GifListener, CommentsInput.InputListener,
    CommentsInput.AttachmentsListener, Timer.OnTimeTickListener, OnViewRepliesClickListener,
    OnClickListeners {
    @Inject
    lateinit var retrofitInterface: RetrofitInstance
    lateinit var localStorage: LocalStorage
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private lateinit var commentViewModel: CommentsViewModel
    private lateinit var playerView: PlayerView
    private lateinit var context: Context
    private lateinit var binding: ActivityPostDetails2Binding
    private lateinit var clickListeners: OnClickListeners

    //    private late init var BottomSheet1Binding: BottomSheet1Binding
    private var adapter: CommentsRecyclerViewAdapter? = null
    private var commentCount by Delegates.notNull<Int>()
    private var currentCommentAudioPath = ""
    private var currentCommentAudioPosition = RecyclerView.NO_POSITION
    private var isReplyVnPlaying = false
    private var isVnAudioToPlay = false

    var isDurationOnPause = false
    var isOnRecordDurationOnPause = false
    var waveProgress = 0f
    var seekBarProgress = 0f

    private val waveHandler = Handler()
    var vnRecordAudioPlaying = false
    private var isAudioVNPlaying = false
    var vnRecordProgress = 0
    private var position: Int = 0
    private lateinit var shortsViewModel: ShortsViewModel
    private lateinit var postId: String
    private lateinit var commentId: String
    private var isReply = false
    private lateinit var roomCommentReplyViewModel: RoomCommentReplyViewModel
    private var data: Comment? = null
    private lateinit var commentsReplyViewModel: ShortCommentReplyViewModel
    private lateinit var shortsCommentViewModel: RoomCommentsViewModel
    private var listOfReplies = mutableListOf<Comment>()
    private var shortToComment: ShortsEntity? = null
    private lateinit var commentsViewModel: ShortCommentsViewModel
    private lateinit var commentsRecyclerViewAdapter: CommentsRecyclerViewAdapter
    private var commentIdToNavigate: String? = null
    private lateinit var commentFilesViewModel: RoomCommentFilesViewModel
    private lateinit var notificationCountViewModel: NotificationCountViewModel
    private var emojiShowing = false
    private lateinit var emojiPopup: EmojiPopup
    private lateinit var inputMethodManager: InputMethodManager

    //    private lateinit var onClickListeners: OnClickListeners
    private val playbackStateListener: Player.Listener = playbackStateListener()
    private val shortPlaybackStateListener: Player.Listener = shortPlaybackStateListener()


    //    private lateinit var audioDurationTVCount: TextView
    private lateinit var audioFormWave: WaveformSeekBar
    private lateinit var audioSeekBar: SeekBar
    private var seekPosition = -1
    private var wavePosition = -1
    private val recordedAudioFiles = mutableListOf<String>()
    private var mediaRecorder: MediaRecorder? = null
    private lateinit var amplitudes: ArrayList<Float>
    private var amps = 0
    private var outputVnFile: String = ""
    private lateinit var outputFile: String
    private var permissionGranted = false
    private var permissionGranted2 = false
    private var permissionGranted3 = false
    private var isVnResuming = false
    private val IMAGES_REQUEST_CODE = 2023
    private val REQUEST_CODE = 2024
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var audioPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var videoPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var docsPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var shortPlayer: ExoPlayer
    private var videoUrl: String? = null
    private var gifUrlType = ""
    private val PREFS_NAME = "LocalSettings"
    private lateinit var httpDataSourceFactory: HttpDataSource.Factory
    private lateinit var defaultDataSourceFactory: DefaultDataSourceFactory
    private lateinit var cacheDataSourceFactory: CacheDataSource.Factory
    private val simpleCache: SimpleCache = FlashApplication.cache
    private var player: MediaPlayer? = null
    private val requestCode = 2024
    private val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 12
    private lateinit var downloadProgressBarLayout: LinearLayout
    private var wifiAnimation: AnimationDrawable? = null
    private lateinit var shortsDownloadImageView: ImageView
    private lateinit var shortsDownloadProgressBar: ProgressBar

    private var commentColor: Color = Color.WHITE
    private var totalLikes = 0
    private var totalFavorites = 0
    private lateinit var onClickListeners: OnClickListeners
    private var isUserSeeking = false
    private var isUserShortSeeking = false
    private lateinit var shortSeekBar: SeekBar
    private lateinit var myProfileRepository: ProfileRepository
    private val callViewModel: CallViewModel by viewModels()
    private val messageViewModel: MessageViewModel by viewModels()
    private val dialogViewModel: DialogViewModel by viewModels()
    private val groupDialogViewModel: GroupDialogViewModel by viewModels()
    private val userProfileShortsViewModel: UserProfileShortsViewModel by viewModels()
    private var currentlyHighlightedIndex: Int = -1

    private val getDocumentContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                Log.d("getDocument", "document is being fetched")
                result.data?.data?.let { uri ->
                    // Handle the selected document URI
                    handleDocumentUri(uri)
                }
            }
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.READ_MEDIA_IMAGES
    )

    private var isRecording = false
    private var isPaused = false
    private var isAudioVNPaused = false
    private lateinit var timer: Timer

    private val comments = mutableListOf<Comment>()

    private fun updateRecordWaveProgress(progress: Float) {
        CoroutineScope(Dispatchers.Main).launch {
            binding.wave.progress = progress
//            currentComment?.progress = progress
            Log.d("updateWaveProgress", "updateWaveProgress: $progress")
        }
    }
//    private val showComments = intent.getBooleanExtra("show_comments", false)

    companion object {
        const val EXTRA_POST_ID = "post_id"
        const val EXTRA_COMMENT_ID = "comment_id"
        const val EXTRA_COMMENT = "show_comments"
        const val EXTRA_COMMENT_REPLY_ID = "comment_reply_id"
        private const val COMMENT_SECTION = 1
    }

    private lateinit var commentsRecyclerView: RecyclerView

    @OptIn(UnstableApi::class)
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        binding = ActivityPostDetails2Binding.inflate(layoutInflater)
        setContentView(binding.root)
//        val bottomSheet1Binding = BottomSheet1Binding.inflate(layoutInflater)
//        binding.root.addView(BottomSheet1Binding.root)
        permissionGranted = ActivityCompat.checkSelfPermission(
            this, permissions[0]
        ) == PackageManager.PERMISSION_GRANTED

        permissionGranted2 = ActivityCompat.checkSelfPermission(
            this, permissions[1]
        ) == PackageManager.PERMISSION_GRANTED

        permissionGranted3 = ActivityCompat.checkSelfPermission(
            this, permissions[2]
        ) == PackageManager.PERMISSION_GRANTED

        if (!permissionGranted2) {
            ActivityCompat.requestPermissions(this, permissions, READ_EXTERNAL_STORAGE_REQUEST_CODE)
        }
        if (!permissionGranted) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
        }

        if (!permissionGranted3) {
            ActivityCompat.requestPermissions(this, permissions, IMAGES_REQUEST_CODE)
        }

//        setContentView(R.layout.activity_post_details2)
        notificationCountViewModel = ViewModelProvider(this)[NotificationCountViewModel::class.java]

        commentViewModel = ViewModelProvider(this)[CommentsViewModel::class.java]
        roomCommentReplyViewModel = ViewModelProvider(this)[RoomCommentReplyViewModel::class.java]
        commentsReplyViewModel = ViewModelProvider(this)[ShortCommentReplyViewModel::class.java]
        commentsViewModel = ViewModelProvider(this)[ShortCommentsViewModel::class.java]
        shortsCommentViewModel = ViewModelProvider(this)[RoomCommentsViewModel::class.java]
        commentFilesViewModel = ViewModelProvider(this)[RoomCommentFilesViewModel::class.java]
        shortsViewModel = ViewModelProvider(this)[ShortsViewModel::class.java]
        settings = getSharedPreferences(PREFS_NAME, 0)
        val accessToken = settings.getString("token", "").toString()
        timer = Timer(this)
        docsPickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    result.data?.data?.also { uri ->
                        handleDocumentUri(uri)
                    }
                }
            }
        val commentId = intent.getStringExtra(EXTRA_COMMENT_ID)
        val postId = intent.getStringExtra(EXTRA_POST_ID)
        val commentIdToNavigate = intent.getStringExtra(EXTRA_COMMENT_ID)
        val showComments = intent.getBooleanExtra(EXTRA_COMMENT, false)


        commentsRecyclerView = binding.recyclerView
        commentsRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        commentsRecyclerViewAdapter = CommentsRecyclerViewAdapter(this, this@PostDetailsActivity2)
        commentsRecyclerView.adapter = commentsRecyclerViewAdapter
        Log.d(
            "PostDetails",
            "Received postId: $postId and commentIdToNavigate: $commentIdToNavigate from intent"
        )

        if (showComments) {
            if (postId != null) {
                // Fetch and display comments if show_comments is true
                fetchPostDetails(postId)
            } else {
                Log.d("showCComment", "ShowComments :$postId")
            }
        } else {
            // Handle the case where comments should not be displayed
            Log.d("PostDetailsActivity2", "Comments are not displayed.")
        }
        if (postId != null) {
//            fetchPostDetails(postId)
            Log.d("ApiService", "Received commentId $commentId")
            if (commentId != null) {
                getPageComment(commentId)
            } else {
                Log.d("ApiService", "commentId is null")
            }
            if (commentIdToNavigate != null) {
                Log.d("CommentNavigation", "Navigating to commentId: $commentIdToNavigate")
                if (commentId != null) {
                    scrollToComment(commentId)
                }
            } else {
                Log.d(
                    "CommentNavigation",
                    "No commentIdToNavigate provided; skipping comment navigation"
                )
            }
            toggleMotionLayoutVisibility()
        } else {
            Log.d("PostDetailActivity", "Post ID is missing")
            Log.d("PostDetailActivity", "CommentId is missing")
        }
        val menuSearch: MenuItem? = binding.toolbar.menu.findItem(R.id.menu_search)
        val settingsMenuItem: MenuItem? = binding.toolbar.menu.findItem(R.id.menu_setting)
        val logoutMenuItem: MenuItem? = binding.toolbar.menu.findItem(R.id.logout)

        menuSearch?.setOnMenuItemClickListener {
            val intent = Intent(this, SearchShortActivity::class.java)
            startActivity(intent)
            true
        }
        settingsMenuItem?.setOnMenuItemClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            true
        }
        logoutMenuItem?.setOnMenuItemClickListener {
            showLogoutConfirmationDialog()
            true
        }
//        shortSeekBar.progress = 0
        shortSeekBar = binding.shortsSeekBar
        playerView = binding.videoView
        shortPlayer = ExoPlayer.Builder(this).build()
        playerView.useController = false
        playerView.player = exoPlayer
        downloadProgressBarLayout = binding.downloadProgressBarLayout
        onClickListeners = this
        commentsRecyclerViewAdapter = CommentsRecyclerViewAdapter(this, this@PostDetailsActivity2)

        shortsDownloadImageView = binding.shortsDownloadImageView

        val shortsViewPager = binding.shortsViewPager
//        toggleMotionLayoutVisibility()
//        downloadProgressBarLayout = binding.downloadProgressBarLayout
        shortsDownloadImageView = binding.shortsDownloadImageView
        shortsDownloadProgressBar = binding.shortsDownloadProgressBar
        val playerView = binding.videoView
        playerView.setOnClickListener {
            Log.d("player view", "player view button is working")
            togglesPausePlay()
        }
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.commentsParentLayout.setOnClickListener {
            toggleMotionLayoutVisibility()
            postId?.let {
                onCommentsClick(postId)
            } ?: run {
                Log.d("commentsUploading", "commentsUploading: null")
            }
            binding.motionLayout.visibility = View.VISIBLE
        }
        audioPickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    // Handle image selection result here
                    val data = result.data
                    // Process the selected image data
                    val audioPath = data?.getStringExtra("audio_url")
//                    val aUri = data?.getStringExtra("aUri")
                    val uriString = data?.getStringExtra("aUri")
                    val aUri = Uri.parse(uriString)
//                    val vUri = Uri.parse(uriString)
                    if (audioPath != null) {
                        Log.d("AudioPicker", "File path: $audioPath")
                        Log.d("AudioPicker", "File path: $isReply")
                        val durationString = getFormattedDuration(audioPath)
                        val fileName = getFileNameFromLocalPath(audioPath)
                        val reverseDurationString = reverseFormattedDuration(durationString)
                        Log.d("AudioPicker", "File path: $audioPath")
                        Log.d("AudioPicker", "File name: $fileName")
                        Log.d("AudioPicker", "durationString: $durationString")
                        Log.d("AudioPicker", "reverseDurationString: $reverseDurationString")
                        val file = File(audioPath)
                        if (isReply) {
//                                    uploadReplyVideoComment(videoPath, durationString, true)
                            uploadReplyVnComment(
                                audioPath,
                                fileName,
                                durationString,
                                "mAudio",
                                true
                            )
                        } else {
//                                   uploadVideoComment(videoPath, durationString, true)
                            uploadVnComment(
                                audioPath,
                                fileName,
                                durationString,
                                "mAudio",
                                true
                            )
                        }
//                        val inputFilePath = "path_to_your_input_audio_file"
//                        val outputFilePath = "path_to_your_output_compressed_audio_file"
                        val outputFileName =
                            "compressed_audio_${System.currentTimeMillis()}.mp3" // Example output file name
                        val outputFilePath = File(cacheDir, outputFileName)

                        lifecycleScope.launch(Dispatchers.IO) {
                            val compressor = FFMPEG_AudioCompressor()
                            val isCompressionSuccessful =
                                compressor.compress(audioPath, outputFilePath.absolutePath)

//                            val compressedFile = compressAudio(audioPath, outputFilePath.absolutePath)
                            if (isCompressionSuccessful) {
                                Log.d("AudioPicker", "AudioPicker: Compression successful ")

                                val fileSizeInBytes = outputFilePath.length()
                                val fileSizeInKB = fileSizeInBytes / 1024
                                val fileSizeInMB = fileSizeInKB / 1024
                                Log.d(
                                    "AudioPicker",
                                    "File size: $fileSizeInKB KB,  $fileSizeInMB MB"
                                )

                                val fileSizeInGB = fileSizeInMB / 1024 // Conversion from MB to GB
//                            Log.d("VideoPicker", "File size: $fileSizeInGB GB")
                                withContext(Dispatchers.Main) {
                                    if (!isReply) {
                                        uploadVnComment(
                                            vnToUpload = outputFilePath.absolutePath,
                                            fileName = fileName,
                                            durationString = durationString,
                                            fileType = "mAudio",
                                            update = true
                                        )
                                    } else {
                                        uploadReplyVnComment(
                                            outputFilePath.absolutePath,
                                            fileName,
                                            durationString,
                                            fileType = "mAudio",
                                            update = true
                                        )
                                    }
                                }

                            } else {
                                Log.d("AudioPicker", "AudioPicker: Compression not successful")
                            }
                        }
                    } else {
                        Log.d("AudioPicker", "File path: $audioPath")
                    }
                }
            }
        cameraLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    // Handle image selection result here
                    val data = result.data
                    // Process the selected image data
                    val imagePath = data?.getStringExtra("image_url")
                    Log.d(
                        "cameraLauncher", "Selected image path from camera: $imagePath"
                    )
                    val imageUri = Uri.parse(imagePath)
                    Log.d(
                        "cameraLauncher", "Selected image path from camera: $imageUri"
                    )
                    if (imagePath != null) {
                        val file = File(imagePath)
                        if (file.exists()) {
                            lifecycleScope.launch {
                                val compressedImageFile =
                                    Compressor.compress(this@PostDetailsActivity2, file)
                                Log.d(
                                    "cameraLauncher",
                                    "cameraLauncher: compressedImageFile absolutePath: ${compressedImageFile.absolutePath}"
                                )
                                val fileSizeInBytes = compressedImageFile.length()
                                val fileSizeInKB = fileSizeInBytes / 1024
                                val fileSizeInMB = fileSizeInKB / 1024

                                Log.d(
                                    "cameraLauncher",
                                    "cameraLauncher: compressedImageFile size $fileSizeInKB KB, $fileSizeInMB MB"
                                )
                                if (!isReply) {
                                    uploadImageComment(compressedImageFile.absolutePath)
                                } else {
                                    uploadReplyImageComment(compressedImageFile.absolutePath)
                                }
                            }
                        }
                    }
                }
            }
        docsPickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    // Handle image selection result here
                    val data = result.data
                    // Process the selected image data
                    val docPath = data?.getStringExtra("doc_url")

//                    Log.d("Document Results", "Picked Document : $docPath")

                    if (docPath != null) {

                        Log.d("ChatActivityDocPath", "Selected Document path: $docPath")

                        val docFileName =
                            "files/${System.currentTimeMillis()}.jpg" // Change the file name as needed
                        val user = User("0", "You", "test", true, Date())
                        val messageId = "Doc_${Random.nextInt()}"

                        val date = Date(System.currentTimeMillis())

                        val message = Message(
                            messageId, user, // Set user ID as needed
                            null, date
                        )

                        val audioUrl = Uri.parse(docPath)
                        val file = File(docPath)
                        if (file.exists()) {

                            Log.d("Document File", "Document File Exists : $file")
                            val absolutePath = file.absolutePath
//                            Log.d(TAG, "image absolute path $absolutePath")
                            val fileUri = Uri.fromFile(file)
                            val fileUrl = fileUri.toString()
                        }
                    }
                }
            }
        videoPickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                Log.d("VideoDebug", "onActivityResult callback triggered")
                if (result.resultCode == RESULT_OK) {
                    val data = result.data
                    val videoPath = data?.getStringExtra("video_url")
                    val uriString = data?.getStringExtra("vUri")
                    val vUri = Uri.parse(uriString)
                    val uri = Uri.parse(videoPath)

                    if (videoPath != null) {
                        Log.d("VideoPicker", "File path: $videoPath")
                        Log.d("VideoPicker", "File path: $isReply")
                        val durationString = getFormattedDuration(videoPath)
                        Log.d("VideoPicker", "File path durationString: $durationString")

                        val file = File(videoPath)
                        if (file.exists()) {
                            val fileSizeInBytes = file.length()
                            val fileSizeInKB = fileSizeInBytes / 1024
                            val fileSizeInMB = fileSizeInKB / 1024
                            Log.d("VideoPicker", "File size: $fileSizeInMB MB")

                            val fileSizeInGB = fileSizeInMB / 1024 // Conversion from MB to GB
//                            Log.d("VideoPicker", "File size: $fileSizeInGB GB")

                            if (fileSizeInGB.toInt() == 1) {
                                showToast(this, "File size too large")
                            } else if (fileSizeInMB > 10) {
                                Log.d("VideoPicker", "File size: greater than $fileSizeInMB MB")
                                Log.d("VideoPicker", "Uri $uri")
                                Log.d("VideoPicker", "v Uri $vUri")
                                if (isReply) {
                                    uploadReplyVideoComment(videoPath, durationString, true)
                                } else {
                                    uploadVideoComment(videoPath, durationString, true)
                                }
                                if (vUri != null) {
                                    toCompressUris.add(vUri)
                                }
                                compressShorts(durationString, fileType = "video")
                            } else {
                                Log.d("VideoPicker", "File size: less than $fileSizeInMB MB")
                                if (!isReply) {
                                    uploadVideoComment(videoPath, durationString)
                                } else {
                                    uploadReplyVideoComment(videoPath, durationString)
                                }
                            }
                        } else {
                            Log.d("VideoPicker", "File does not exists ")
                        }
//                        toCompressUris
                    } else {
                        Log.d("PhotoPicker", "No media selected")
                    }
                }
            }
        imagePickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    // Handle image selection result here
                    val data = result.data
                    // Process the selected image data
                    val imagePath = data?.getStringExtra("image_url")
                    Log.d("ImagePath", "Img path $imagePath")
                }
            }
        installTwitter()
        addComment()
        addCommentVN()
        addCommentReply()
        addCommentFileReply()
        addImageComment()
        addVideoComment()
        addDocumentComment()
        addGifComment()
        observeCommentRepliesToRefresh()
        observeMainCommentToRefresh()
        // Back press handling
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentVisibility = binding.motionLayout.visibility

                if (currentVisibility == View.VISIBLE) {
                    toggleMotionLayoutVisibility()


                } else {
                    finish()
                }
            }
        }
        onBackPressedDispatcher.addCallback(
            this, onBackPressedCallback
        )
        initializeCommentsBottomSheet()
//
        shortSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Update playback position when user drags the SeekBar
                if (fromUser) {
                    shortPlayer.seekTo(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // User starts seeking
                isUserSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // User stops seeking
                isUserSeeking = false
            }
        })


        binding.VNLinearLayout.setOnClickListener {
            Log.d(TAG, "onCreate: vn linear layout touched")
        }
//        audioRecorderClickListeners()
        if (binding.motionLayout.visibility == View.GONE) {
            binding.VNLayout.visibility = View.GONE
        } else {
            Log.d(TAG, "onCreate: vn linear layout touched")

        }
        binding.recordVN.setOnClickListener {
            when {
                isPaused -> resumeRecording()
                isRecording -> pauseRecording()
                else -> Log.d("recordVN", "onCreate: else in vn record btn on click")
//                else->startRecording()
            }
        }
        binding.deleteVN.setOnClickListener {
            if (mediaRecorder != null) {
//                timer.stop()
                Log.d(TAG, "onCreate: media recorder not null")
            } else {
                Log.d(TAG, "onCreate: media recorder null")
            }
            lifecycleScope.launch(Dispatchers.Main) {
                delay(500)
                deleteRecording()
            }
            if (player?.isPlaying == true) {
                stopPlaying()
            }
            binding.VNLayout.visibility = View.GONE
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
                    timer.stop()
                    delay(500)
                    stopRecording()
                }
//                stopRecording() // Stop recording after mixVN finishes executing or immediately if wasPaused is true
            }
        }
    }

    @Deprecated("Deprecated in Java")

    override fun onBackPressed() {
        super.onBackPressed()
        if (isRecording) {
            Log.d("stopRecording", "vn deleted")
            stopRecording()

//            onTimerTick(0.toString())
        }
    }

    var wasPaused = false
    var sending = false
    private var mixingCompleted = false // Define a flag to track if mixing is completed

    private fun updateSeekBar() {
        Log.d("UpdateSeekBar", "audio is working")
        exoPlayer.let { player ->
            audioSeekBar.max = maxDuration.toInt()
            audioSeekBar.progress = position
//            currentHandler?.postDelayed(this ,1000)
            if (!isUserSeeking) {
                val currentPosition = player.currentPosition.toInt()
                audioSeekBar.progress = currentPosition
            } else {
                Log.d("UpdateSeekBar", "not working")
            }
        }
    }

    private fun updateShortSeekBar() {
        Log.d("UpdateSeekBar", "video is working")
        shortPlayer.let { player ->
            shortSeekBar.max = maxDuration.toInt()
            shortSeekBar.progress = position
//            currentHandler?.postDelayed(this ,1000)
            if (!isUserSeeking) {
                val currentPosition = player.currentPosition.toInt()
                shortSeekBar.progress = currentPosition
            } else {
                Log.d("UpdateSeekBar", "not working")
            }
        }
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Logout")
        builder.setMessage("Are you sure you want to logout?")
        builder.setPositiveButton("Yes") { dialog, which ->
            // Handle logout here
            performLogout()
        }
        builder.setNegativeButton("No") { dialog, which ->
            // Dismiss the dialog
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun performLogout() {
        CoroutineScope(Dispatchers.IO).launch {
            //delete data from local db
            deleteUserProfile()
            //clear shared prefs
            LocalStorage.getInstance(this@PostDetailsActivity2).clear()
            LocalStorage.getInstance(this@PostDetailsActivity2).clearToken()
            settings.edit().clear().apply()
            callViewModel.clearAll()
            messageViewModel.clearAll()
            dialogViewModel.clearAll()
            groupDialogViewModel.clearAll()
        }
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        startActivity(intent)
    }

    suspend fun deleteUserProfile() {
        myProfileRepository.deleteMyProfile()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun pauseRecording() {
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
            binding.playVnAudioBtn.setImageResource(R.drawable.play_svgrepo_com)
            binding.recordVN.setImageResource(com.uyscuti.social.call.R.drawable.ic_mic_on)
            Log.d(TAG, "pauseRecording: list of recordings  size: ${recordedAudioFiles.size}")
            Log.d(TAG, "pauseRecording: list of recordings $recordedAudioFiles")
            mixVN()
        }

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onAddAttachments() {
        showAttachmentDialog()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun showAttachmentDialog() {
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(R.layout.shorts_and_all_feed_file_upload_bottom_dialog)
        val video = dialog.findViewById<LinearLayout>(R.id.upload_video)
        val audio = dialog.findViewById<LinearLayout>(R.id.upload_audio)
        val image = dialog.findViewById<LinearLayout>(R.id.upload_image)
        val camera = dialog.findViewById<LinearLayout>(R.id.open_camera)
        val doc = dialog.findViewById<LinearLayout>(R.id.upload_document)
        val location = dialog.findViewById<LinearLayout>(R.id.share_location)
        // Apply animation to the dialog's view
        val dialogView =
            dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        dialogView?.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up))

        val selectableItemBackground = TypedValue()
        image?.context?.theme?.resolveAttribute(
            android.R.attr.selectableItemBackground, selectableItemBackground, true
        )
        image?.setBackgroundResource(selectableItemBackground.resourceId)


        video?.context?.theme?.resolveAttribute(
            android.R.attr.selectableItemBackground, selectableItemBackground, true
        )
        video?.setBackgroundResource(selectableItemBackground.resourceId)

        audio?.context?.theme?.resolveAttribute(
            android.R.attr.selectableItemBackground, selectableItemBackground, true
        )
        audio?.setBackgroundResource(selectableItemBackground.resourceId)

        camera?.context?.theme?.resolveAttribute(
            android.R.attr.selectableItemBackground, selectableItemBackground, true
        )
        camera?.setBackgroundResource(selectableItemBackground.resourceId)

        doc?.context?.theme?.resolveAttribute(
            android.R.attr.selectableItemBackground, selectableItemBackground, true
        )
        doc?.setBackgroundResource(selectableItemBackground.resourceId)

        location?.context?.theme?.resolveAttribute(
            android.R.attr.selectableItemBackground, selectableItemBackground, true
        )
        location?.setBackgroundResource(selectableItemBackground.resourceId)

        image?.setOnClickListener {
            Log.d("SelectImage", "Image selector button clicked")
            pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            dialog.dismiss()
        }

        video?.setOnClickListener {
//            val intent = Intent(this@ChatActivity, DisplayVideosActivity::class.java)
            val intent = Intent(this@PostDetailsActivity2, VideosActivity::class.java)
            dialog.dismiss()
            videoPickerLauncher.launch(intent)
        }

        audio?.setOnClickListener {
            val intent = Intent(this@PostDetailsActivity2, AudioActivity::class.java)
            audioPickerLauncher.launch(intent)

            dialog.dismiss()
        }
        doc?.setOnClickListener {
            val intent = Intent(this@PostDetailsActivity2, DocumentsActivity::class.java)
            getDocumentContent.launch(intent)
            openFilePicker()
            dialog.dismiss()
        }
        camera?.setOnClickListener {
            val intent = Intent(this@PostDetailsActivity2, CameraActivity::class.java)
//            startActivity(intent)
            cameraLauncher.launch(intent)
            dialog.dismiss()
        }
        location?.visibility = View.INVISIBLE
        location?.setOnClickListener {
        }
        dialog.show()
    }

    private val pickMultipleMedia =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(2)) { uris ->
            // Callback is invoked after the user selects media items or closes the
            // photo picker.
            if (uris.isNotEmpty()) {
                for (uri in uris) {
                    val filePath = PathUtil.getPath(
                        this,
                        uri
                    ) // Use the utility class to get the real file path
                    Log.d("PhotoPicker", "File path: $filePath")
                    Log.d("PhotoPicker", "File path: $isReply")
                    Log.d(
                        "PhotoPicker", "Selected image path from camera: $uri"
                    )

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
                                uploadImageComment(compressedImageFile.absolutePath)
                            } else {
                                uploadReplyImageComment(compressedImageFile.absolutePath)
                            }
                        }
                    }
                }

            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private fun uploadReplyImageComment(
        vnToUpload: String,
        placeholder: Boolean = false,
        update: Boolean = false
    ) {
        Log.d("uploadReplyImageComment", "uploadReplyImageComment: $vnToUpload")
        Log.d("uploadReplyImageComment", "uploadReplyImageComment: isReply is $isReply")


        val localUpdateId = generateRandomId()
        val profilePic2 = settings.getString("profile_pic", "").toString()
        val avatar = com.uyscuti.social.network.api.response.commentreply.allreplies.Avatar(
            "", "", url = profilePic2
        )
        val account = com.uyscuti.social.network.api.response.commentreply.allreplies.Account(
            _id = "", avatar = avatar, "", LocalStorage.getInstance(this).getUsername()
        )


        val commentReplyAuthor = com.uyscuti.social.network.api.response.commentreply.allreplies.Author(
            _id = "21", account = account, firstName = "", lastName = ""
        )

        Log.d("uploadReplyImageComment", "uploadReplyImageComment: handle reply to a comment")
//        isReply = false

//        val newCommentReplyEntity = CommentsFilesEntity(commentId, vnToUpload, vnToUpload, isReply = 1)

        //if it clash on upload un comment the line below//
        if (!placeholder) {
            val newCommentReplyEntity =
                CommentsFilesEntity(
                    postId,
                    "image",
                    vnToUpload,
                    isReply = 1,
                    localUpdateId,
                    content = binding.input.inputEditText.text.toString()
                )
            commentFilesViewModel.insertCommentFile(newCommentReplyEntity)
            Log.d(
                "uploadReplyImageComment",
                "uploadReplyImageComment: inserted comment $newCommentReplyEntity"
            )
        }

        val mongoDbTimeStamp = generateMongoDBTimestamp()
        val imageFile = CommentFiles(_id = "", url = vnToUpload, localPath = "image")

        val newReply = com.uyscuti.social.network.api.response.commentreply.allreplies.Comment(
            __v = data!!.__v,
            _id = "commentId",
            author = commentReplyAuthor,
            content = binding.input.inputEditText.text.toString(),
            createdAt = mongoDbTimeStamp,
            isLiked = false,
            likes = 0,
            commentId = commentId,
            updatedAt = mongoDbTimeStamp,
            images = mutableListOf(imageFile),
//            audios = mutableListOf(vnFile),
            contentType = "image"
        )

        val replyCount = data!!.replyCount + 1
        val commentWithReplies = Comment(
            __v = data!!.__v,
            _id = data!!._id,
            author = data!!.author,
            content = data!!.content,
            createdAt = data!!.createdAt,
            isLiked = data!!.isLiked,
            likes = data!!.likes,
            postId = data!!.postId,
            updatedAt = data!!.updatedAt,
            replyCount = replyCount,
//                replies = data!!.replies
            replies = data?.replies?.toMutableList()?.apply {
                // Assuming newReply is the new reply you want to add
                add(0, newReply)
            } ?: mutableListOf(),
            isRepliesVisible = true,
            images = data?.images ?: mutableListOf(),
            audios = mutableListOf(),
            docs = mutableListOf(),
            gifs = "",
            thumbnail = mutableListOf(),
            videos = mutableListOf(),
            contentType = data?.contentType ?: "image",
            isPlaying = data?.isPlaying ?: false,
            localUpdateId = localUpdateId,
            replyCountVisible = false,
            numberOfPages = data?.numberOfPages ?: "",
            fileSize = data?.fileSize ?: "",
            isReplyPlaying = data?.isReplyPlaying ?: false,
            progress = data?.progress ?: 0f,
            fileType = data?.fileType ?: ""
        )

        updateReplyPosition = position
        if (update) {
            isReply = false
            Log.d("uploadVideoComment", "updatePosition: $updatePosition")
            updateAdapter(commentWithReplies, updateReplyPosition)
            updateReplyPosition = -1
        }
        if (!update) {
            listOfReplies.add(commentWithReplies)
            Log.d(
                "uploadReplyImageComment",
                "uploadReplyImageComment: comment id = data is? $commentId = ${data!!._id} on position $position"
            )
            Log.d(
                "uploadReplyImageComment",
                "uploadReplyImageComment: comment id = data is? $commentId = ${data!!._id} on position $position"
            )
            updateAdapter(commentWithReplies, position)
        }

        binding.input.inputEditText.setText("")
        binding.replyToLayout.visibility = View.GONE
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun uploadImageComment(
        imageFilePathToUpload: String,
        placeholder: Boolean = false,
        update: Boolean = false
    ) {
        Log.d("uploadImageComment", "uploadImageComment: $imageFilePathToUpload")
        Log.d("uploadImageComment", "uploadImageComment: isReply is $isReply")

        val mongoDbTimeStamp = generateMongoDBTimestamp()

        val file = File(imageFilePathToUpload)

        val localUpdateId = generateRandomId()
        if (file.exists()) {
            Log.d("uploadImageComment", "File exists, creating comment.......")
            val profilePic2 = settings.getString("profile_pic", "").toString()
            val avatar = Avatar("", "", url = profilePic2)
            val account =
                Account(_id = "", avatar = avatar, "", LocalStorage.getInstance(this).getUsername())
            val author = Author(
                _id = "12", account = account, firstName = "", lastName = "",
                avatar = TODO()
            )
            val imageFile = CommentFiles(
                _id = "124",
                url = imageFilePathToUpload,
                localPath = imageFilePathToUpload
            )
            val comment = Comment(
                __v = 1,
                _id = adapter!!.itemCount.toString(),
                author = author,
                content = "",
                createdAt = mongoDbTimeStamp,
                isLiked = false,
                likes = 0,
                postId = postId,
                updatedAt = mongoDbTimeStamp,
                replyCount = 0,
                images = mutableListOf(imageFile),
                audios = mutableListOf(),
                docs = mutableListOf(),
                gifs = "",
                thumbnail = mutableListOf(),
                videos = mutableListOf(),
                contentType = "image",
                isPlaying = data?.isPlaying ?: false,
                progress = data?.progress ?: 0f,
                localUpdateId = localUpdateId
            )

            if (!placeholder) {
                val newCommentEntity =
                    CommentsFilesEntity(
                        postId,
                        "image",
                        imageFilePathToUpload,
                        isReply = 0,
                        localUpdateId
                    )
                commentFilesViewModel.insertCommentFile(newCommentEntity)
                Log.d(
                    "uploadImageComment",
                    "uploadImageComment: inserted comment $newCommentEntity"
                )
            }

            if (update) {
                Log.d("uploadVideoComment", "updatePosition: $updatePosition")
                updateAdapter(comment, updatePosition)
                updatePosition = -1
            }

            Log.d("uploadImageComment", "uploadImageComment: comment $comment")
//        adapter.submitItems(listOf(comment) )
//            adapter!!.submitItem(comment, (adapter?.itemCount?.minus(1)!!))
//            adapter!!.submitItem(commentsAndRepliesModel, adapter!!.itemCount)

            recordedAudioFiles.clear()
            if (!update) {
                listOfReplies.add(comment)

                adapter!!.submitItem(comment, adapter!!.itemCount)
//            addCommentVN()
                shortToComment = shortsViewModel.mutableShortsList.find { it._id == postId }


                if (shortToComment != null) {
                    shortToComment!!.comments += 1
                    Log.d(
                        "uploadImageComment",
                        "uploadImageComment: count before ${shortToComment!!.comments}"
                    )
                    // Update the count in the mutableShortsList
                    // Update the count in the mutableShortsList
                    shortsViewModel.mutableShortsList.forEach { short ->
                        if (short._id == postId) {
                            short.comments = shortToComment!!.comments
                        }
                    }
                    val newShortToComment =
                        shortsViewModel.mutableShortsList.find { it._id == postId }
                    Log.d(
                        "uploadImageComment",
                        "onSubmit: count after ${newShortToComment!!.comments}"
                    )

                    EventBus.getDefault().post(ShortAdapterNotifyDatasetChanged())
                }
            }
        } else {
            Log.e(TAG, "File does not exist")
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun resumeRecording() {
        if (isPaused) {
            isVnResuming = true
            startRecording() // Start a new recording session, appending to the previous file
            binding.waveForm.visibility = View.VISIBLE
            binding.timerTv.visibility = View.VISIBLE
            binding.playAudioLayout.visibility = View.GONE
            binding.playVnAudioBtn.setImageResource(R.drawable.play_svgrepo_com)
            binding.recordVN.setImageResource(R.drawable.baseline_pause_black)
        }
    }

    private fun stopRecording() {
        val TAG = "StopRecording"
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
            binding.recordVN.setImageResource(com.uyscuti.social.call.R.drawable.ic_mic_on)
            binding.sendVN.setBackgroundResource(R.drawable.ic_ripple_disabled)
            binding.sendVN.isClickable = false
            amplitudes = binding.waveForm.clear()
            amps = 0
            timer.stop()
            if (player?.isPlaying == true) {
                stopPlaying()
            }
            binding.VNLayout.visibility = View.GONE
            // Add any UI changes or notifications indicating recording has stopped
            Log.d(TAG, "stopRecording: isReply is $isReply")
            binding.replyToLayout.visibility = View.GONE

            val file = File(outputVnFile)
            val file2 = File(outputFile)
            Log.d(TAG, "vn file exists outputVnFile: $outputVnFile")
            Log.d(TAG, "vn file2 exists: $outputFile")
            Log.d(TAG, "vn file exists: ${file.exists()}")
            Log.d(TAG, "vn file2 exists: ${file2.exists()}")

            if (!isReply) {
                Log.d("firstTimeSendVn", "firstTimeSendVn: ${recordedAudioFiles.size}")

                if (recordedAudioFiles.size != 1) {
                    val durationString = getFormattedDuration(outputVnFile)
                    val fileName = getFileNameFromLocalPath(outputVnFile)
                    Log.d("AudioPicker", "File path: $outputVnFile")
                    Log.d("AudioPicker", "File name: $fileName")
                    Log.d("AudioPicker", "durationString: $durationString")
                    uploadVnComment(outputVnFile, fileName, durationString, "vnAudio")
//                    firstTimeSendVn = false
                } else {
                    val durationString = getFormattedDuration(outputFile)
                    val fileName = getFileNameFromLocalPath(outputFile)
                    Log.d("AudioPicker", "File path: $outputFile")
                    Log.d("AudioPicker", "File name: $fileName")
                    Log.d("AudioPicker", "durationString: $durationString")
                    uploadVnComment(outputFile, fileName, durationString, "vnAudio")
                }
            } else {
                Log.d("firstTimeSendVn", "firstTimeSendVn: ${recordedAudioFiles.size}")
                if (recordedAudioFiles.size != 1) {
                    val durationString = getFormattedDuration(outputVnFile)
                    val reverseDurationString = reverseFormattedDuration(durationString)
                    val fileName = getFileNameFromLocalPath(outputVnFile)
                    Log.d("AudioPicker", "File path: $outputVnFile")
                    Log.d("AudioPicker", "File name: $fileName")
                    Log.d("AudioPicker", "durationString: $durationString")
                    Log.d("AudioPicker", "reverseDurationString: $reverseDurationString")
                    uploadReplyVnComment(outputVnFile, fileName, durationString, "vnAudio")
//                    firstTimeSendVn = false
                } else {
                    val durationString = getFormattedDuration(outputFile)
                    val fileName = getFileNameFromLocalPath(outputFile)
                    Log.d("AudioPicker", "File path: $outputFile")
                    Log.d("AudioPicker", "File name: $fileName")
                    Log.d("AudioPicker", "durationString: $durationString")
                    uploadReplyVnComment(outputFile, fileName, durationString, "vnAudio")
                }
            }


        } catch (e: Exception) {
            e.printStackTrace()
            // Handle exceptions as needed
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/*"
//            type = "*/*"
//            type = "images/*" // Set MIME type to select all types of documents
        }
        getDocumentContent.launch(intent)
    }

    private lateinit var settings: SharedPreferences

    @RequiresApi(Build.VERSION_CODES.O)
    private fun uploadReplyVnComment(
        vnToUpload: String,
        fileName: String,
        durationString: String,
        fileType: String,
        placeholder: Boolean = false, update: Boolean = false
    ) {

        val localUpdateId = generateRandomId()
        Log.d("uploadReplyVnComment", "uploadVnComment: $vnToUpload")
        Log.d("uploadReplyVnComment", "stopRecording: isReply is $isReply")


        val profilePic2 = settings.getString("profile_pic", "").toString()
        val avatar = com.uyscuti.social.network.api.response.commentreply.allreplies.Avatar(
            "", "", url = profilePic2
        )
        val account = com.uyscuti.social.network.api.response.commentreply.allreplies.Account(
            _id = "", avatar = avatar, "", LocalStorage.getInstance(this).getUsername()
        )


        val commentReplyAuthor = com.uyscuti.social.network.api.response.commentreply.allreplies.Author(
            _id = "21", account = account, firstName = "", lastName = ""
        )

        Log.d(TAG, "onSubmit: handle reply to a comment")
        isReply = false

//        val newCommentReplyEntity = CommentsFilesEntity(commentId, vnToUpload, vnToUpload, isReply = 1)

        //if it clash on upload un comment the line below//
        if (!placeholder) {
            val newCommentReplyEntity =
                CommentsFilesEntity(
                    postId, "audio", vnToUpload, isReply = 1, localUpdateId,
                    fileName = fileName, fileType = fileType, duration = durationString,
                    content = binding.input.inputEditText.text.toString()
                )
            commentFilesViewModel.insertCommentFile(newCommentReplyEntity)

            Log.d(TAG, "onSubmit: inserted comment $newCommentReplyEntity")
        }

        val mongoDbTimeStamp = generateMongoDBTimestamp()
        val vnFile = CommentFiles(_id = "", url = vnToUpload, localPath = vnToUpload)

        val newReply = com.uyscuti.social.network.api.response.commentreply.allreplies.Comment(
            __v = data!!.__v,
            _id = "commentId",
            author = commentReplyAuthor,
            content = binding.input.inputEditText.text.toString(),
            createdAt = mongoDbTimeStamp,
            isLiked = false,
            likes = 0,
            commentId = commentId,
            updatedAt = mongoDbTimeStamp,
            audios = mutableListOf(vnFile),
            contentType = "audio",
            fileType = fileType,
            fileName = fileName,
            duration = durationString
        )

        val replyCount = data!!.replyCount + 1
        val commentWithReplies = Comment(
            __v = data!!.__v,
            _id = data!!._id,
            author = data!!.author,
//            content = data!!.author?.account?.username!!,
            content = data!!.content,
            createdAt = data!!.createdAt,
            isLiked = data!!.isLiked,
            likes = data!!.likes,
            postId = data!!.postId,
            updatedAt = data!!.updatedAt,
            replyCount = replyCount,
//                replies = data!!.replies
            replies = data?.replies?.toMutableList()?.apply {
                // Assuming newReply is the new reply you want to add
                add(0, newReply)
            } ?: mutableListOf(),
            isRepliesVisible = true,
            images = mutableListOf(),
            audios = data?.audios ?: mutableListOf(),
            docs = mutableListOf(),
            gifs = data?.gifs ?: "",
            thumbnail = mutableListOf(),
            videos = mutableListOf(),
            contentType = data?.contentType ?: "audio",
            isPlaying = data?.isPlaying ?: false,
            localUpdateId = localUpdateId,
            replyCountVisible = false,
            fileName = data!!.fileName,
            fileType = data!!.fileType,
            duration = data!!.duration,
            progress = data?.progress ?: 0f,
            isReplyPlaying = data?.isReplyPlaying ?: false,
            fileSize = data?.fileSize ?: "",
            numberOfPages = data?.numberOfPages ?: ""
        )

        updateReplyPosition = position
        if (update) {
            isReply = false
            Log.d("uploadVideoComment", "updatePosition: $updatePosition")
            updateAdapter(commentWithReplies, updateReplyPosition)
            updateReplyPosition = -1
        }

        if (!update) {
            listOfReplies.add(commentWithReplies)
            Log.d(
                TAG,
                "onSubmit: comment id = data is? $commentId = ${data!!._id} on position $position"
            )
            Log.d(
                TAG,
                "onSubmit: comment id = data is? $commentId = ${data!!._id} on position $position"
            )
            updateAdapter(commentWithReplies, position)
        }

        binding.input.inputEditText.setText("")
        binding.replyToLayout.visibility = View.GONE
    }

    private var updateReplyPosition = -1
    private fun uploadVnComment(
        vnToUpload: String,
        fileName: String,
        durationString: String,
        fileType: String,
        placeholder: Boolean = false, update: Boolean = false
    ) {
        Log.d("uploadVnComment", "uploadVnComment: $vnToUpload")
        Log.d("uploadVnComment", "stopRecording: isReply is $isReply")
        Log.d("uploadVnComment", "stopRecording: duration is $durationString")

        val mongoDbTimeStamp = generateMongoDBTimestamp()

        val localUpdateId = generateRandomId()
        val file = File(vnToUpload)

        if (file.exists()) {
            Log.d(TAG, "File exists, creating comment.......")
            val profilePic2 = settings.getString("profile_pic", "").toString()
            val avatar = Avatar("", "", url = profilePic2)
            val account =
                Account(_id = "", avatar = avatar, "", LocalStorage.getInstance(this).getUsername())
            val author = Author(
                _id = "12", account = account, firstName = "", lastName = "",
                avatar = TODO()
            )
            val vnFile = CommentFiles(_id = "124", url = vnToUpload, localPath = vnToUpload)
            val comment = Comment(
                __v = 1,
                _id = adapter!!.itemCount.toString(),
                author = author,
                content = "",
                createdAt = mongoDbTimeStamp,
                isLiked = false,
                likes = 0,
                postId = postId,
                updatedAt = mongoDbTimeStamp,
                replyCount = 0,
                images = mutableListOf(),
                audios = mutableListOf(vnFile),
                docs = mutableListOf(),
                gifs = "",
                thumbnail = mutableListOf(),
                videos = mutableListOf(),
                contentType = "audio",
                isPlaying = data?.isPlaying ?: false,
                progress = data?.progress ?: 0f,
                localUpdateId = localUpdateId,
                fileName = fileName,
                duration = durationString,
                fileType = fileType
            )

            if (!placeholder) {
                val newCommentEntity =
                    CommentsFilesEntity(
                        postId, "audio", vnToUpload, isReply = 0, localUpdateId,
                        fileName = fileName, duration = durationString, fileType = fileType
                    )
                commentFilesViewModel.insertCommentFile(newCommentEntity)
                Log.d(TAG, "uploadVnComment: inserted comment $newCommentEntity")
            }
            if (update) {
                Log.d("uploadVideoComment", "updatePosition: $updatePosition")
                updateAdapter(comment, updatePosition)
                updatePosition = -1
            }
            Log.d(TAG, "uploadVnComment: comment $comment")
            recordedAudioFiles.clear()

            if (!update) {
                listOfReplies.add(comment)
                adapter!!.submitItem(comment, adapter!!.itemCount)
//            addCommentVN()
                shortToComment = shortsViewModel.mutableShortsList.find { it._id == postId }
                if (shortToComment != null) {
                    Log.d(TAG, "uploadVnComment: count before ${shortToComment!!.comments}")

                    shortToComment!!.comments += 1
                    // Update the count in the mutableShortsList
                    // Update the count in the mutableShortsList
                    shortsViewModel.mutableShortsList.forEach { short ->
                        if (short._id == postId) {
                            short.comments = shortToComment!!.comments
                        }
                    }
                    val newShortToComment =
                        shortsViewModel.mutableShortsList.find { it._id == postId }
                    Log.d(TAG, "onSubmit: count after ${newShortToComment!!.comments}")

                    EventBus.getDefault().post(ShortAdapterNotifyDatasetChanged())
                }
            }

        } else {
            Log.e(TAG, "File does not exist")
        }
    }

    private var updatePosition = -1

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
                                    binding.playVnAudioBtn.setImageResource(R.drawable.baseline_pause_black)
                                    Log.d(
                                        "playVnAudioBtn",
                                        "play vn"
                                    )
                                    startPlaying(outputVnFile)
                                }

                                else -> {
                                    Log.d(
                                        "playVnAudioBtn",
                                        "pause VN"
                                    )
                                    binding.playVnAudioBtn.setImageResource(R.drawable.play_svgrepo_com)
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

//            binding.timerTv.text = "00:00.00"
            binding.recordVN.setImageResource(R.drawable.baseline_pause_white_24)
            binding.recordVN.setImageResource(com.uyscuti.social.call.R.drawable.ic_mic_on)

            binding.deleteVN.setBackgroundResource(R.drawable.ic_ripple_disabled)
            binding.deleteVN.isClickable = false
            binding.sendVN.setBackgroundResource(R.drawable.ic_ripple_disabled)
            binding.sendVN.isClickable = false

            amplitudes = binding.waveForm.clear()
            amps = 0

            timer.stop()
            Log.d("TAG", "deleteRecording: recorded files size ${recordedAudioFiles.size}")
            deleteVn()

//            if()
            // Add any UI changes or notifications indicating recording has stopped
//            showSaveConfirmationDialog(outputFile)
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle exceptions as needed
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun cleanCache(event: CleanCache) {


        val TAG = "CleanCache"

        Log.d(TAG, "cleanCache: inside clean cache bus in main activity")
        initMediaLoader()
        try {
            Log.d(TAG, "cleanCache: inside clean cache bus in main activity try download")

            DownloadManager.getInstance(applicationContext).cleanCacheDir()
//            MediaLoader.getInstance(this).
        } catch (e: IOException) {
            Toast.makeText(this@PostDetailsActivity2, "Error clean cache", Toast.LENGTH_LONG).show()
        }
    }

    private fun initMediaLoader() {
        Log.d("CleanCache", "cleanCache: initMediaLoader")

        val mediaLoaderConfig: MediaLoaderConfig = MediaLoaderConfig.Builder(this)
            .cacheRootDir(DefaultConfigFactory.createCacheRootDir(this))
            .cacheFileNameGenerator(Md5FileNameCreator())
            .maxCacheFilesCount(100)
            .maxCacheFilesSize(100 * 1024 * 1024)
            .maxCacheFileTimeLimit(15 * 24 * 60 * 60)
            .downloadThreadPoolSize(3)
            .downloadThreadPriority(Thread.NORM_PRIORITY)
            .build()
        MediaLoader.getInstance(this).init(mediaLoaderConfig)
    }

    private fun deleteVn() {
        recordedAudioFiles.clear()
//        if (recordedAudioFiles.isNotEmpty()) {
        val isDeleted = deleteFiles(recordedAudioFiles)
        val outputVnFileList = mutableListOf<String>()
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
    }


    private fun pauseVn(progress: Int) {
        Log.d("pauseVn", "vnRecordProgress $vnRecordProgress..... progress $progress")

        player?.pause()
        player?.seekTo(progress)
        isAudioVNPlaying = false
        isAudioVNPaused = true
        isOnRecordDurationOnPause = true

//        progressAnim.pause()
        binding.playVnAudioBtn.setImageResource(R.drawable.play_svgrepo_com)
    }

    private fun startPlaying(vnAudio: String) {
        binding.playVnAudioBtn.setImageResource(R.drawable.baseline_pause_white_24)
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
                        stopPlaying()
                    }
                } catch (e: IOException) {
                    Log.e("MediaRecorder", "prepare() failed")
                }
            }
        }
    }

    private fun startRecordWaveRunnable() {
        try {
            Log.d(
                "isDurationOnPause",
                " in comment audio start wave isDurationOnPause is $isOnRecordDurationOnPause"
            )
            waveHandler.removeCallbacks(onRecordWaveRunnable)
            waveHandler.post(onRecordWaveRunnable)
            isOnRecordDurationOnPause = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    @SuppressLint("DefaultLocale")
    private fun inflateWave(outputVN: String) {

//        outputVnFile = outputVN

        val TAG = "inflateWave"
        Log.d("playVnAudioBtn", "inflateWave: outputvn $outputVN")

        val audioFile = File(outputVN)
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
                                    startPlaying(outputVN)
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
//        binding.wave.setRawData(audioFile.readBytes()) { progressAnim.start() }
    }

    private val READ_EXTERNAL_STORAGE_REQUEST_CODE = 101 // Any integer value


    private fun installTwitter() {
        EmojiManager.install(TwitterEmojiProvider())
    }

    private fun addCommentReply() {
        val TAG = "addCommentReply"
        Log.d(TAG, "addCommentReply: inside")
//        Log.d(TAG, "addCommentReply: comment id $commentId")
//        commentsReplyViewModel.commentReply(commentId, content)

        if (isInternetAvailable(this)) {

            roomCommentReplyViewModel.allCommentReplies.observe(this) {

                if (it.isNotEmpty()) {
                    Log.d(TAG, "addComment: comments in room count is ${it.size}")
                    commentsReplyViewModel.commentReply(
                        it[0].commentId,
                        it[0].content,
                        it[0].localUpdateId
                    )
                    roomCommentReplyViewModel.viewModelScope.launch {
                        val isDeleted =
                            roomCommentReplyViewModel.deleteCommentReplyById(it[0].commentId)
                        if (isDeleted) {
                            // Deletion was successful, update UI or perform other actions
                            Log.d(TAG, "Follow deleted successfully.")
                        } else {
                            // Deletion was not successful, handle accordingly
                            Log.d(TAG, "Failed to delete follow.")
                        }
                    }

                } else {
                    Log.d(TAG, "onSubmit: Room database has no comments")
                }
            }
        } else {
            Log.d(TAG, "addComment: no internet connection")
        }
    }

    private fun addCommentVN() {
        val TAG = "addCommentVN"
        Log.d("addCommentReply", "addComment: is reply $isReply")

//        commentsViewModel.commentAudio(postId, "", "audio", filePart, video, image, docs, gif, thumbnail)

        if (isInternetAvailable(this)) {

            commentFilesViewModel.allCommentFiles.observe(this) {

//                if(it.isNotEmpty()) {
//
//                }
                Log.d(TAG, "Comments observed size:${it.size}")
//
                if (it.isNotEmpty()) {

                    for (i in it) {
                        val file = File(i.url)

                        if (i.localPath == "audio" && i.isReply == 0) {
                            Log.d("FileSend", "Local path is audio")
                            Log.d(TAG, "url ${i.url}")
                            if (file.exists()) {
                                val requestFile =
                                    file.asRequestBody("multipart/form-data".toMediaTypeOrNull())

// Create MultipartBody.Part instance from RequestBody
                                val filePart =
                                    MultipartBody.Part.createFormData(
                                        "audio",
                                        file.name,
                                        requestFile
                                    )
                                val video =
                                    MultipartBody.Part.createFormData(
                                        "video",
                                        file.name,
                                        requestFile
                                    )
                                val image =
                                    MultipartBody.Part.createFormData(
                                        "image",
                                        file.name,
                                        requestFile
                                    )
                                val docs =
                                    MultipartBody.Part.createFormData(
                                        "docs",
                                        file.name,
                                        requestFile
                                    )
                                val gif =
                                    MultipartBody.Part.createFormData("gif", file.name, requestFile)
                                val thumbnail =
                                    MultipartBody.Part.createFormData(
                                        "thumbnail",
                                        file.name,
                                        requestFile
                                    )

                                Log.d(TAG, "addComment: comments in room count is ${it.size}")
                                commentsViewModel.commentAudio(
                                    postId,
                                    "",
                                    "audio",
                                    filePart,
                                    video,
                                    image,
                                    docs,
                                    gif,
                                    thumbnail,
                                    i.localUpdateId,
                                    fileName = i.fileName,
                                    fileType = i.fileType,
                                    duration = i.duration,
                                    isFeedComment = i.isFeedComment

                                )
//                                { data ->
//                                    Log.d(
//                                        "OnSuccess",
//                                        "OnSuccess: addCommentVN id: ${data._id} parent position ${it[0].parentPosition}"
//                                    )
//
//                                    val commentPosition = adapter?.getPositionByUploadId(i.uploadId)
//
//                                    val comment = commentPosition?.let { it1 ->
//                                        adapter?.getComment(
//                                            it1
//                                        )
//                                    }
//                                    Log.d(
//                                        TAG,
//                                        "addCommentVN: comment get successful  $comment"
//                                    )
//                                    if (comment != null) {
//                                        comment._id = data._id
//                                    }
//                                    adapter?.notifyItemChanged(commentPosition!!)
//                                }
//                    commentsViewModel.comment(it[0].postId, it[0].content, "text")
                                commentFilesViewModel.viewModelScope.launch {
                                    val isDeleted = commentFilesViewModel.deleteCommentById(i.id)
                                    if (isDeleted) {
                                        // Deletion was successful, update UI or perform other actions
                                        Log.d(TAG, "addCommentVN deleted successfully.")
                                        recordedAudioFiles.clear()

                                    } else {
                                        // Deletion was not successful, handle accordingly
                                        Log.d(TAG, "Failed to delete addCommentVN.")
                                    }
                                }
                            } else {
                                Log.d(TAG, "File does not exist")
                                commentFilesViewModel.viewModelScope.launch {
                                    val isDeleted = commentFilesViewModel.deleteCommentById(i.id)
                                    if (isDeleted) {
                                        // Deletion was successful, update UI or perform other actions
                                        Log.d(TAG, "vn deleted successfully.")
                                        recordedAudioFiles.clear()
                                    } else {
                                        // Deletion was not successful, handle accordingly
                                        Log.d(TAG, "Failed to delete follow.")
                                    }
                                }
                            }
                        } else {
                            Log.d("FileSend", "i.isReply: ${i.isReply} send reply audio")
                        }

                    }


                } else {
                    Log.d(TAG, "onSubmit: Room database has no comments")
                }
            }


        } else {
            Log.d(TAG, "addComment: no internet connection")
        }

    }

    @SuppressLint("DefaultLocale")
    private fun addCommentFileReply() {
        val TAG = "addCommentReply"
        Log.d(TAG, "addCommentReply: inside ")

        if (isInternetAvailable(this)) {

            commentFilesViewModel.allCommentReplyFiles.observe(this) {

                if (it.isNotEmpty()) {
                    val file = File(it[0].url)
                    val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                    val filePart =
                        MultipartBody.Part.createFormData("audio", file.name, requestFile)
                    val video = MultipartBody.Part.createFormData("video", file.name, requestFile)
                    val image = MultipartBody.Part.createFormData("image", file.name, requestFile)
                    val docs = MultipartBody.Part.createFormData("docs", file.name, requestFile)
                    val gif = MultipartBody.Part.createFormData("gif", file.name, requestFile)
                    val thumbnail =
                        MultipartBody.Part.createFormData("thumbnail", file.name, requestFile)
                    Log.d(
                        "LocalPath",
                        "url ${it[0].url} local path ${it[0].localPath} ${it[0].isReply}"
                    )
                    if (it[0].localPath == "image") {
                        Log.d("LocalPath", "is set to image")
                        if (::commentId.isInitialized) {
                            commentsReplyViewModel.commentReply(
                                commentId,
                                it[0].content,
                                "image",
                                filePart,
                                video,
                                docs,
                                "",
                                thumbnail,
                                image,
                                it[0].localUpdateId,
                                "00:00",
                                numberOfPages = "",
                                fileName = "",
                                fileType = "",
                                fileSize = "",
                                isFeedCommentReply = it[0].isFeedComment
                            ) {

                            }
                        } else {
                            Log.e("CommentId", "Comment Id not initialized ")
                        }
                    } else if (it[0].localPath == "video") {
                        Log.d("LocalPath", "is set to video $")

                        val durationString = getFormattedDuration(it[0].url)
                        Log.d("durationString", "durationString $durationString")
                        val (success, bitmapThumbnail) = extractThumbnailFromVideo(it[0].url)
                        if (success) {
                            // Thumbnail extraction successful, use the 'thumbnail' Bitmap
                            Log.d("ThumbnailExtract", "ThumbnailExtract successful")
                        } else {
                            // Thumbnail extraction failed
                            Log.d("ThumbnailExtract", "ThumbnailExtract failed")
                        }
                        val outputStream = ByteArrayOutputStream()
//                            // Compress the bitmap to a byte array
                        bitmapThumbnail?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
//                            // Convert the byte array to a RequestBody
                        val requestBody = outputStream.toByteArray()
                            .toRequestBody(
                                "image/*".toMediaTypeOrNull(),
                                0, outputStream.size()
                            )

                        val videoThumbnail = MultipartBody.Part.createFormData(
                            "thumbnail",
                            "thumbnail.png",
                            requestBody
                        )

                        val videos =
                            MultipartBody.Part.createFormData("video", file.name, requestFile)
                        if (::commentId.isInitialized) {
                            commentsReplyViewModel.commentReply(
                                commentId,
                                it[0].content,
                                "video",
                                audio = filePart,
                                video = videos,
                                docs = docs,
                                gif = "",
                                thumbnail = videoThumbnail,
                                image = image,
                                localUpdateId = it[0].localUpdateId,
                                duration = durationString,
                                numberOfPages = "",
                                fileName = "",
                                fileType = "",
                                fileSize = "",
                                isFeedCommentReply = it[0].isFeedComment
                            ) { data ->
                                val deleted = deleteFile(it[0].url)
                                if (deleted) {
                                    Log.d(" onSuccess()", " onSuccess(): deleted successful")
                                } else {
                                    Log.d(" onSuccess()", " onSuccess(): failed to delete")
                                }
                            }
                        } else {
                            Log.e("CommentId", "Comment Id not initialized ")
                        }
                    } else if (it[0].localPath == "docs") {
                        val replyDocs = createMultipartBody(this, it[0].url.toUri(), "docs")
                        Log.d("LocalPath", "is set to docs")
                        if (::commentId.isInitialized) {
                            commentsReplyViewModel.commentReply(
                                commentId,
                                it[0].content,
                                "docs",
                                audio = filePart,
                                video = video,
                                docs = replyDocs!!,
                                gif = "",
                                thumbnail = thumbnail,
                                image = image,
                                localUpdateId = it[0].localUpdateId,
                                duration = "00:00",
                                numberOfPages = it[0].numberOfPages.toString(),
                                fileName = it[0].fileName,
                                fileType = it[0].fileType,
                                fileSize = it[0].fileSize,
                                isFeedCommentReply = it[0].isFeedComment
                            ) {

                            }
                        } else {
                            Log.e("CommentId", "Comment Id not initialized ")
                        }
                    } else if (it[0].localPath == "gif") {
//                        val replyGif = createMultipartBody(this, it[0].url.toUri(), "gif")
                        Log.d("LocalPath", "is set to gif")
                        if (::commentId.isInitialized) {
                            commentsReplyViewModel.commentReply(
                                commentId,
                                it[0].content,
                                "gif",
                                audio = filePart,
                                video = video,
                                docs = docs,
                                gif = it[0].url,
                                thumbnail = thumbnail,
                                image = image,
                                localUpdateId = it[0].localUpdateId,
                                duration = "00:00",
                                numberOfPages = "",
                                fileName = "",
                                fileType = "",
                                fileSize = "",
                                isFeedCommentReply = it[0].isFeedComment
                            ) {

                            }
                        } else {
                            Log.e("CommentId", "Comment Id not initialized ")
                        }
                    } else {
                        Log.d("LocalPath", "is set to audio duration ${it[0].duration}")

                        if (::commentId.isInitialized) {
                            commentsReplyViewModel.commentReply(
                                commentId = commentId,
                                content = it[0].content,
                                contentType = "audio",
                                audio = filePart,
                                video = video,
                                image = image,
                                docs = docs,
                                gif = "",
                                thumbnail = thumbnail,
                                localUpdateId = it[0].localUpdateId,
                                duration = it[0].duration,
                                numberOfPages = "",
                                fileName = it[0].fileName,
                                fileType = it[0].fileType,
                                fileSize = "",
                                isFeedCommentReply = it[0].isFeedComment
                            ) { data ->
                                Log.d(
                                    "OnSuccess",
                                    "OnSuccess: addCommentFileReply id: ${data._id} parent position ${it[0].parentPosition}"
                                )

                                val comment = adapter?.getComment(it[0].parentPosition)

                                Log.d(TAG, "addCommentFileReply: comment get successful  $comment")
                                val replyToUpdate = comment?.replies?.find { reply ->
                                    Log.d(
                                        TAG,
                                        "addCommentFileReply: ids it[0].uploadId  ${it[0].uploadId} reply.uploadId  ${reply.uploadId}"
                                    )
                                    reply.uploadId == it[0].uploadId
                                }
                                replyToUpdate?._id = data._id
                                adapter?.notifyItemChanged(it[0].parentPosition)
//                                adapter?.updateItem(it[0].parentPosition, comment)
                            }
                        } else {
                            Log.e("CommentId", "Comment Id not initialized ")
                        }

                    }

                    commentFilesViewModel.viewModelScope.launch {
                        val isDeleted = commentFilesViewModel.deleteCommentById(it[0].id)
                        if (isDeleted) {
                            // Deletion was successful, update UI or perform other actions
                            Log.d(TAG, "File in local database deleted successfully.")
                        } else {
                            // Deletion was not successful, handle accordingly
                            Log.d(TAG, "Failed to delete the file in local db.")
                        }
                    }

                } else {
                    Log.d(TAG, "onSubmit: Room database has no comments")
                }
            }


        } else {
            Log.d(TAG, "addComment: no internet connection")
        }

    }

    private fun addImageComment() {
        val TAG = "addImageComment"
        Log.d("addImageComment", "addImageComment: is reply $isReply")

//        commentsViewModel.commentAudio(postId, "", "audio", filePart, video, image, docs, gif, thumbnail)

        if (isInternetAvailable(this)) {

            commentFilesViewModel.allCommentFiles.observe(this) {

                Log.d(TAG, "Comments observed size:${it.size}")
//
                if (it.isNotEmpty()) {

                    for (i in it) {
                        val file = File(i.url)

                        if (i.localPath == "image") {
                            Log.d("LocalPath", "Local path is image")
                            Log.d(TAG, "url ${i.url} type ${i.localPath}")
                            if (file.exists()) {
                                val requestFile =
                                    file.asRequestBody("multipart/form-data".toMediaTypeOrNull())

                                val filePart =
                                    MultipartBody.Part.createFormData(
                                        "audio",
                                        file.name,
                                        requestFile
                                    )
                                val video =
                                    MultipartBody.Part.createFormData(
                                        "video",
                                        file.name,
                                        requestFile
                                    )
                                val image =
                                    MultipartBody.Part.createFormData(
                                        "image",
                                        file.name,
                                        requestFile
                                    )
                                val docs =
                                    MultipartBody.Part.createFormData(
                                        "docs",
                                        file.name,
                                        requestFile
                                    )
                                val gif =
                                    MultipartBody.Part.createFormData("gif", file.name, requestFile)
                                val thumbnail =
                                    MultipartBody.Part.createFormData(
                                        "thumbnail",
                                        file.name,
                                        requestFile
                                    )

                                Log.d(TAG, "addComment: comments in room count is ${it.size}")
                                commentsViewModel.commentImage(
                                    postId,
                                    "",
                                    "image",
                                    filePart,
                                    video,
                                    thumbnail,
                                    gif,
                                    docs,
                                    image,
                                    i.localUpdateId,
                                    i.isFeedComment
                                )
                                commentFilesViewModel.viewModelScope.launch {
                                    val isDeleted = commentFilesViewModel.deleteCommentById(i.id)
                                    if (isDeleted) {
                                        // Deletion was successful, update UI or perform other actions
                                        Log.d(TAG, "addImageComment deleted successfully.")
                                    } else {
                                        // Deletion was not successful, handle accordingly
                                        Log.d(TAG, "Failed to delete follow.")
                                    }
                                }
                            } else {
                                Log.d(TAG, "File does not exist")
                                commentFilesViewModel.viewModelScope.launch {
                                    val isDeleted = commentFilesViewModel.deleteCommentById(i.id)
                                    if (isDeleted) {
                                        // Deletion was successful, update UI or perform other actions
                                        Log.d(TAG, "Follow deleted successfully.")
                                        recordedAudioFiles.clear()
                                    } else {
                                        // Deletion was not successful, handle accordingly
                                        Log.d(TAG, "Failed to delete follow.")
                                    }
                                }
                            }
                        }
//                        else if(i.localPath == "video"){
//                            Log.d("LocalPath","ready to upload video")
//                            Log.d("LocalPath", "url ${i.url} type ${i.localPath}")
//                        }

                    }


                } else {
                    Log.d(TAG, "onSubmit: Room database has no comments")
                }
            }


        } else {
            Log.d(TAG, "addComment: no internet connection")
        }

    }

    private fun addDocumentComment() {
        val TAG = "addDocumentComment"
        Log.d("addDocumentComment", "addDocumentComment: is reply $isReply")

//        commentsViewModel.commentAudio(postId, "", "audio", filePart, video, image, docs, gif, thumbnail)

        if (isInternetAvailable(this)) {

            commentFilesViewModel.allCommentFiles.observe(this) {

                Log.d(TAG, "Comments observed size:${it.size}")
//
                if (it.isNotEmpty()) {

                    for (i in it) {
                        val file = File(i.url)

                        if (i.localPath == "docs") {
                            Log.d("LocalPath", "Local path is docs")
                            Log.d(TAG, "url ${i.url} type ${i.localPath}")
                            if (isFileExists(this, i.url.toUri())) {
                                val requestFile =
                                    file.asRequestBody("multipart/form-data".toMediaTypeOrNull())

                                val filePart =
                                    MultipartBody.Part.createFormData(
                                        "audio",
                                        file.name,
                                        requestFile
                                    )
                                val video =
                                    MultipartBody.Part.createFormData(
                                        "video",
                                        file.name,
                                        requestFile
                                    )
                                val image =
                                    MultipartBody.Part.createFormData(
                                        "image",
                                        file.name,
                                        requestFile
                                    )
//                                val docs =
//                                    MultipartBody.Part.createFormData(
//                                        "docs",
//                                        file.name,
//                                        requestFile
//                                    )
                                Log.d(
                                    "addDocumentComment",
                                    "addDocumentComment: uri ${i.url.toUri()} ::i.url:: ${i.url}"
                                )
                                val docs =
                                    createMultipartBody(this, i.url.toUri(), "docs", i.fileType)
                                val gif =
                                    MultipartBody.Part.createFormData("gif", file.name, requestFile)
                                val thumbnail =
                                    MultipartBody.Part.createFormData(
                                        "thumbnail",
                                        file.name,
                                        requestFile
                                    )

                                Log.d(TAG, "addComment: comments in room count is ${it.size}")
                                commentsViewModel.addComment(
                                    postId,
                                    "",
                                    "docs",
                                    filePart,
                                    video,
                                    thumbnail,
                                    "",
                                    docs!!,
                                    image,
                                    i.localUpdateId,
                                    "",
                                    fileName = i.fileName,
                                    numberOfPages = i.numberOfPages.toString(),
                                    fileSize = i.fileSize,
                                    fileType = i.fileType,
                                    isFeedComment = i.isFeedComment
                                ) {

                                }
                                commentFilesViewModel.viewModelScope.launch {
                                    val isDeleted = commentFilesViewModel.deleteCommentById(i.id)
                                    if (isDeleted) {
                                        // Deletion was successful, update UI or perform other actions
                                        Log.d(TAG, "addImageComment deleted successfully.")
                                    } else {
                                        // Deletion was not successful, handle accordingly
                                        Log.d(TAG, "Failed to delete follow.")
                                    }
                                }
                            } else {
                                Log.d(TAG, "File does not exist")
                                commentFilesViewModel.viewModelScope.launch {
                                    val isDeleted = commentFilesViewModel.deleteCommentById(i.id)
                                    if (isDeleted) {
                                        // Deletion was successful, update UI or perform other actions
                                        Log.d(TAG, "Follow deleted successfully.")
                                        recordedAudioFiles.clear()
                                    } else {
                                        // Deletion was not successful, handle accordingly
                                        Log.d(TAG, "Failed to delete follow.")
                                    }
                                }
                            }
                        }
//                        else if(i.localPath == "video"){
//                            Log.d("LocalPath","ready to upload video")
//                            Log.d("LocalPath", "url ${i.url} type ${i.localPath}")
//                        }

                    }


                } else {
                    Log.d(TAG, "onSubmit: Room database has no comments")
                }
            }


        } else {
            Log.d(TAG, "addComment: no internet connection")
        }

    }

    private fun addGifComment() {
        val TAG = "addGifComment"
        Log.d("addGifComment", "addGifComment: is reply $isReply")

//        commentsViewModel.commentAudio(postId, "", "audio", filePart, video, image, docs, gif, thumbnail)

        if (isInternetAvailable(this)) {

            commentFilesViewModel.allCommentFiles.observe(this) {

                Log.d(TAG, "Comments observed size:${it.size}")
//
                if (it.isNotEmpty()) {

                    for (i in it) {
                        val file = File(i.url)

                        if (i.localPath == "gif") {
                            Log.d("LocalPath", "Local path is gif")
                            Log.d(TAG, "url ${i.url} type ${i.localPath}")
                            if (i.url.isNotEmpty()) {
                                val requestFile =
                                    file.asRequestBody("multipart/form-data".toMediaTypeOrNull())

                                val filePart =
                                    MultipartBody.Part.createFormData(
                                        "audio",
                                        file.name,
                                        requestFile
                                    )
                                val video =
                                    MultipartBody.Part.createFormData(
                                        "video",
                                        file.name,
                                        requestFile
                                    )
                                val image =
                                    MultipartBody.Part.createFormData(
                                        "image",
                                        file.name,
                                        requestFile
                                    )
                                val docs =
                                    MultipartBody.Part.createFormData(
                                        "docs",
                                        file.name,
                                        requestFile
                                    )
//                                val gif = createMultipartBody(this, i.url.toUri(), "gif")
//                                val gif =
//                                    MultipartBody.Part.createFormData("gif", file.name, requestFile)
                                val thumbnail =
                                    MultipartBody.Part.createFormData(
                                        "thumbnail",
                                        file.name,
                                        requestFile
                                    )

                                Log.d(TAG, "addComment: comments in room count is ${it.size}")
                                commentsViewModel.addComment(
                                    postId,
                                    "",
                                    "gif",
                                    filePart,
                                    video,
                                    thumbnail,
                                    gifs = i.url,
                                    docs,
                                    image,
                                    i.localUpdateId,
                                    "",
                                    fileName = i.fileName,
                                    numberOfPages = i.numberOfPages.toString(),
                                    fileSize = i.fileSize,
                                    fileType = i.fileType,
                                    isFeedComment = i.isFeedComment
                                ) {

                                }
                                commentFilesViewModel.viewModelScope.launch {
                                    val isDeleted = commentFilesViewModel.deleteCommentById(i.id)
                                    if (isDeleted) {
                                        // Deletion was successful, update UI or perform other actions
                                        Log.d(TAG, "addImageComment deleted successfully.")
                                    } else {
                                        // Deletion was not successful, handle accordingly
                                        Log.d(TAG, "Failed to delete follow.")
                                    }
                                }
                            } else {
                                Log.d(TAG, "File does not exist")
                                commentFilesViewModel.viewModelScope.launch {
                                    val isDeleted = commentFilesViewModel.deleteCommentById(i.id)
                                    if (isDeleted) {
                                        // Deletion was successful, update UI or perform other actions
                                        Log.d(TAG, "Follow deleted successfully.")
                                        recordedAudioFiles.clear()
                                    } else {
                                        // Deletion was not successful, handle accordingly
                                        Log.d(TAG, "Failed to delete follow.")
                                    }
                                }
                            }
                        }
//                        else if(i.localPath == "video"){
//                            Log.d("LocalPath","ready to upload video")
//                            Log.d("LocalPath", "url ${i.url} type ${i.localPath}")
//                        }

                    }


                } else {
                    Log.d(TAG, "onSubmit: Room database has no comments")
                }
            }


        } else {
            Log.d(TAG, "addComment: no internet connection")
        }

    }
    private fun observeCommentRepliesToRefresh() {
        commentsReplyViewModel.getReplyCommentsLiveData().observe(this) { data ->
            // Handle the response data here
            for (i in listOfReplies) {
//                i._id
                Log.d("observeCommentRepliesToRefresh", "list of replies id ${i.localUpdateId}")
            }

            Log.d("observeCommentRepliesToRefresh", "list of replies size ${listOfReplies.size}")
            Log.d("observeCommentRepliesToRefresh", data.toString())
        }

    }

    @SuppressLint("DefaultLocale")
    private fun addVideoComment() {
        val TAG = "addVideoComment"
        Log.d("addVideoComment", "addVideoComment: is reply $isReply")

        if (isInternetAvailable(this)) {

            commentFilesViewModel.allCommentFiles.observe(this) {

                Log.d(TAG, "Comments observed size:${it.size}")
//
                if (it.isNotEmpty()) {

                    for (i in it) {
                        val file = File(i.url)

                        if (i.localPath == "video") {
                            Log.d("addVideoComment", "Local path is video duration ${i.duration}")

                            Log.d(TAG, "url ${i.url} type ${i.localPath} file name: ${file.name}")

                            val externalStorageDir = Environment.getExternalStorageDirectory()
                            val fullPath = File(externalStorageDir, i.url)

//                            Log.d(TAG, "Full path $fullPath")
//                            val audioDuration = AudioDurationHelper.getLocalAudioDuration(i.url)!!
//                            val minutes = (audioDuration / 1000) / 60
//                            val seconds = (audioDuration / 1000) % 60
//
//                            val durationString = String.format("%02d:%02d", minutes, seconds)
                            val (success, bitmapThumbnail) = extractThumbnailFromVideo(i.url)
                            if (success) {
                                // Thumbnail extraction successful, use the 'thumbnail' Bitmap
                                Log.d("ThumbnailExtract", "ThumbnailExtract successful")
                            } else {
                                // Thumbnail extraction failed
                                Log.d("ThumbnailExtract", "ThumbnailExtract failed")
                            }
                            val outputStream = ByteArrayOutputStream()
//                            // Compress the bitmap to a byte array
                            bitmapThumbnail?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
//                            // Convert the byte array to a RequestBody
                            val requestBody = outputStream.toByteArray()
                                .toRequestBody(
                                    "image/*".toMediaTypeOrNull(),
                                    0, outputStream.size()
                                )

                            val thumbnail = MultipartBody.Part.createFormData(
                                "thumbnail",
                                "thumbnail.png",
                                requestBody
                            )
                            if (file.exists()) {
                                val requestFile =
                                    file.asRequestBody("multipart/form-data".toMediaTypeOrNull())

                                val filePart =
                                    MultipartBody.Part.createFormData(
                                        "audio",
                                        file.name,
                                        requestFile
                                    )
                                val video =
                                    MultipartBody.Part.createFormData(
                                        "video",
                                        file.name,
                                        requestFile
                                    )
                                val image =
                                    MultipartBody.Part.createFormData(
                                        "image",
                                        file.name,
                                        requestFile
                                    )
                                val docs =
                                    MultipartBody.Part.createFormData(
                                        "docs",
                                        file.name,
                                        requestFile
                                    )
                                val gif =
                                    MultipartBody.Part.createFormData("gif", file.name, requestFile)
//                               val thumbnail =  MultipartBody.Part.createFormData(
//                                    "thumbnail",
//                                    file.name,
//                                    requestFile
//                                )

                                Log.d(TAG, "addVideoComment: comments in room count is ${it.size}")
                                if (::postId.isInitialized) {
                                    commentsViewModel.addComment(
                                        postId,
                                        "",
                                        "video",
                                        filePart,
                                        video,
                                        thumbnail,
                                        "",
                                        docs,
                                        image,
                                        i.localUpdateId,
                                        duration = i.duration,
                                        numberOfPages = "",
                                        fileName = "",
                                        fileType = "",
                                        fileSize = "",
                                        isFeedComment = i.isFeedComment
                                    ) {
                                        Log.d(" onSuccess()", " onSuccess(): upload successful")
//                                    deleteFile(i.url) addCommentFileReply
                                        val deleted = deleteFiled(i.url)
                                        if (deleted) {
                                            Log.d(
                                                " onSuccess()",
                                                " onSuccess(): deleted successful"
                                            )
                                        } else {
                                            Log.d(" onSuccess()", " onSuccess(): failed to delete")
                                        }
                                    }
                                }

                                commentFilesViewModel.viewModelScope.launch {
                                    val isDeleted = commentFilesViewModel.deleteCommentById(i.id)
                                    if (isDeleted) {
                                        // Deletion was successful, update UI or perform other actions
                                        Log.d(TAG, "addVideoComment deleted successfully.")
                                    } else {
                                        // Deletion was not successful, handle accordingly
                                        Log.d(TAG, "Failed to delete VideoComment.")
                                    }
                                }
                            } else {
                                Log.d(TAG, "File does not exist")
                                commentFilesViewModel.viewModelScope.launch {
                                    val isDeleted = commentFilesViewModel.deleteCommentById(i.id)
                                    if (isDeleted) {
                                        // Deletion was successful, update UI or perform other actions
                                        Log.d(TAG, "addVideoComment deleted successfully.")
                                        recordedAudioFiles.clear()
                                    } else {
                                        // Deletion was not successful, handle accordingly
                                        Log.d(TAG, "Failed to delete addVideoComment.")
                                    }
                                }
                            }
                        }
//                        else if(i.localPath == "video"){
//                            Log.d("LocalPath","ready to upload video")
//                            Log.d("LocalPath", "url ${i.url} type ${i.localPath}")
//                        }

                    }


                } else {
                    Log.d(TAG, "addVideoComment: Room database has no comments")
                }
            }


        } else {
            Log.d(TAG, "addVideoComment: no internet connection")
        }

    }

    private fun observeMainCommentToRefresh() {
        commentsViewModel.commentsObserver().observe(this) { data ->
            // Handle the response data here
            for (mainComment in listOfReplies) {
//                i._id
                Log.d(
                    "UpdateReplyData",
                    "list of replies id ${mainComment.localUpdateId} position ${mainComment._id}"
                )
                if (mainComment.localUpdateId == data.localUpdateId) {
                    Log.d(
                        "UpdateReplyData",
                        "We have an equal to update on position ${mainComment._id}"
                    )
                    val position: Int = mainComment._id.toInt()
                    mainComment._id = data._id
                    updateAdapter(mainComment, position)
                }
            }

        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun openUserProfileShortsPlayerFragment(event: ShortsLikeUnLike2) {
        val TAG = "likeUnLikeShort"

        Log.d(TAG, "openUserProfileShortsPlayerFragment: ")
        lifecycleScope.launch {
            try {
                val response = retrofitInterface.apiService.likeUnLikeShort(event.userId)
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d(
                        TAG, "likeUnLikeShort ${responseBody?.data!!.isLiked}"
                    )
                } else {
                    Log.d(TAG, "Error: ${response.message()}")
//                requireActivity().runOnUiThread {
//                    showToast(response.message())
//                }
                }
            } catch (e: HttpException) {
                Log.d(TAG, "Http Exception ${e.message}")
//            requireActivity().runOnUiThread {
//                showToast("Failed to connect try again...")
//            }
            } catch (e: IOException) {
                Log.d(TAG, "IOException ${e.message}")
//            requireActivity().runOnUiThread {
//                showToast("Failed to connect try again...")
//            }
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun getPageComment(commentId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = retrofitInterface.apiService.getPageComment(commentId)
                if (response.isSuccessful) {
                    val notifications = response.body()?.data
                    Log.d("ApiService", "getCommentByPage: $notifications")
                    withContext(Dispatchers.Main) {
                        adapter = CommentsRecyclerViewAdapter(
                            this@PostDetailsActivity2,
                            this@PostDetailsActivity2
                        )
                        binding.recyclerView.layoutManager =
                            LinearLayoutManager(
                                this@PostDetailsActivity2,
                                LinearLayoutManager.VERTICAL,
                                false
                            )
                        binding.recyclerView.adapter = adapter
                        binding.recyclerView.itemAnimator = null
                        adapter?.setDefaultRecyclerView(
                            this@PostDetailsActivity2,

                            R.id.recyclerView

                        )
                        if (adapter?.itemCount == 0) {
                            Log.d("GetPageCommentId", "adapter is empty")
                        }
                        toggleMotionLayoutVisibility()
                        notifications?.let {
                            val commentList =
                                arrayListOf<Comment>()
                            val comments = Comment(
                                __v = it.comment.__v,
                                _id = it.comment._id,
                                author = it.comment.author,
                                content = it.comment.content,
                                createdAt = it.comment.createdAt,
                                isLiked = it.comment.isLiked,
                                likes = it.comment.likes,
                                postId = it.comment.postId,
                                updatedAt = it.comment.updatedAt,
                                replyCount = it.comment.replyCount,
                                replies = it.comment.replies.toMutableList(),
                                images = it.comment.images,
                                audios = it.comment.audios,
                                docs = it.comment.docs,
                                gifs = data?.gifs ?: "",
                                thumbnail = it.comment.thumbnail,
                                videos = it.comment.videos,
                                contentType = it.comment.contentType,
                                localUpdateId = "",
                                duration = "00:00",
                                fileName = "unknown",
                                fileSize = "unknown",
                                fileType = "unknown",
                                numberOfPages = "0"
                            )
                            it.comments.map { commentX ->
                                Log.d("ApiService", "getCommentByPage: $commentX")
                                val comment = Comment(
                                    __v = commentX.__v,
                                    _id = commentX._id,
                                    author = commentX.author,
                                    content = commentX.content,
                                    createdAt = commentX.createdAt,
                                    isLiked = commentX.isLiked,
                                    likes = commentX.likes,
                                    postId = commentX.postId,
                                    updatedAt = commentX.updatedAt,
                                    replyCount = commentX.replyCount,
                                    replies = commentX.replies.toMutableList(),
                                    images = commentX.images,
                                    audios = commentX.audios,
                                    docs = commentX.docs,
                                    gifs = data?.gifs ?: "",
                                    thumbnail = commentX.thumbnail,
                                    videos = commentX.videos,
                                    contentType = commentX.contentType,
                                    localUpdateId = "",
                                    duration = "00:00",
                                    fileName = "unknown",
                                    fileSize = "unknown",
                                    fileType = "unknown",
                                    numberOfPages = "0"
                                )
                                commentList.add(comment)
                            }
                            adapter!!.submitItems(commentList)
                                /**this is the code used mainly */
                                val highlightedIndex = commentList.indexOfLast { it._id == commentId }
                                if (highlightedIndex != -1) {
                                Log.d("HighLight", "(1)highlight $highlightedIndex currentlyHighlightedIndex $currentlyHighlightedIndex")
                                /**Check if the index is different from the currently highlighted index*/
                                if (highlightedIndex != currentlyHighlightedIndex) {
                                    /**Update the currently highlighted index*/
                                    Log.d("HighLight", "(2)highlight $highlightedIndex currentlyHighlightedIndex $currentlyHighlightedIndex")

                                    currentlyHighlightedIndex = highlightedIndex

                                    commentsRecyclerView.post {
                                        var viewHolder =
                                        commentsRecyclerView.findViewHolderForAdapterPosition(
                                                currentlyHighlightedIndex
                                            )
                                        viewHolder?.itemView?.setBackgroundResource(
                                            R.color.white
                                        )
                                    clearPreviousHighlights()

                                        commentsRecyclerView.scrollToPosition(highlightedIndex)

                                        /**Highlight the comment after a short delay*/
                                        commentsRecyclerView.postDelayed({
                                            Log.d("HighLight", "getPageComment: inside highlight 1")
                                            viewHolder =
                                                commentsRecyclerView.findViewHolderForAdapterPosition(
                                                    highlightedIndex
                                                )
                                            viewHolder?.itemView?.setBackgroundColor(
                                                ContextCompat.getColor(
                                                    this@PostDetailsActivity2,
                                                    R.color.bluejeans
                                                )
                                            )
                                            /**Optional: Revert the color after a few seconds*/
                                            viewHolder?.itemView?.postDelayed({
                                                Log.d(
                                                    "HighLight",
                                                    "getPageComment: inside highlight 2"
                                                )
                                                viewHolder!!.itemView.setBackgroundColor(
                                                    ContextCompat.getColor(
                                                        this@PostDetailsActivity2,
                                                        R.color.white
                                                    )
                                                )
                                                currentlyHighlightedIndex = -1
                                            }, 5000)
                                        }, 200)
                                    }
                                } else {
                                    val viewHolder = commentsRecyclerView.findViewHolderForAdapterPosition(
                                            highlightedIndex
                                        )
                                    Log.d("HighLight", "highlightedIndex is +1")
                                    viewHolder!!.itemView.setBackgroundColor(
                                        ContextCompat.getColor(
                                            this@PostDetailsActivity2,
                                            R.color.white
                                        )
                                    )
                                }
                            } else {
                                Log.d("HighLight", "comment not found")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d("ApiService", "getCommentByPage: ${e.message}")
            }
        }
    }


    private fun clearPreviousHighlights() {
        // Loop through the visible items and reset their background color
        Log.d("HighLight", "clearPreviousHighlights")
        val layoutManager = binding.recyclerView.layoutManager as? LinearLayoutManager
        val firstVisiblePosition = layoutManager?.findFirstVisibleItemPosition() ?: 0
        val lastVisiblePosition = layoutManager?.findLastVisibleItemPosition() ?: 0

        for (i in firstVisiblePosition..lastVisiblePosition) {
            Log.d("HighLight", "clearPreviousHighlights $i")
            val viewHolder = binding.recyclerView.findViewHolderForAdapterPosition(i)
            viewHolder?.itemView?.setBackgroundColor(
                ContextCompat.getColor(
                    this@PostDetailsActivity2,
                    R.color.white
                )
            )
        }
    }
    private fun fetchPostDetails(postId: String) {
        coroutineScope.launch {
            Log.d("PostDetails", "Fetching details for postId: $postId")
            withContext(Dispatchers.IO) {
                try {
                    Log.d("PostDetails", "Making network request for postId: $postId")
                    val response = retrofitInterface.apiService.getPostById(postId)
                    if (response.isSuccessful) {
                        Log.d("PostDetails", "Network request successful for postId: $postId")
//                        "Shorts List in page $postId ${response.body()!!}"
                        Log.d("PostDetails", "Fetched post post: ${response.body()!!.data}")
                        response.body()?.let { post ->
                            Log.d("PostDetails", "Post details retrieved: $response")
                            val commentNotificationResponse =
                                response                            /* Obtain this from response or adapt your response model */
                            withContext(Dispatchers.Main) {
                                updateNotification(post)
                                commentIdToNavigate?.let {
                                    Log.d(
                                        "CommentNavigation",
                                        "Successfully navigated to commentId: $commentId"
                                    )
//                                    scrollToComment(it)
                                }
                            }
                        } ?: Log.d("PostDetails", "Response body is null for postId: $postId")
                    } else {
                        Log.e("PostDetails", "Error fetching post: ${response.errorBody()}")
                    }
                } catch (e: Exception) {
                    Log.e("PostDetails", "Failure fetching post", e)
                }
            }
        }
    }
    private fun fetchCommentsDetails(comments: List<Comment>) {
        coroutineScope.launch {
            Log.d("commentDetails", "Fetching details for postId: $comments")
            withContext(Dispatchers.IO) {
                try {
                    Log.d("commentDetails", "Making network request for postId: $comments")

                    val response = retrofitInterface.apiService.getPostById(comments.toString())

                    if (response.isSuccessful) {
                        Log.d("commentDetails", "Network request successful for postId: $comments")
//                        "Shorts List in page $postId ${response.body()!!}"
                        Log.d("commentDetails", "Fetched post post: ${response.body()!!.data}")

                        response.body()?.let { post ->
                            Log.d("commentDetails", "Post details retrieved: $response")
                            val commentNotificationResponse =
                                response                            /* Obtain this from response or adapt your response model */
                            withContext(Dispatchers.Main) {
                                updateNotification(post)

//                                val comments = post.data.comments // Assuming your post data contains a list of comments
                                commentIdToNavigate?.let {
                                    Log.d("positionDetails", "position is working ")

                                }
                            }
                        } ?: Log.d("PostDetails", "Response body is null for postId: $comments")
                    } else {
                        Log.e("PostDetails", "Error fetching post: ${response.errorBody()}")
                    }
                } catch (e: Exception) {
                    Log.e("PostDetails", "Failure fetching post", e)
                }
            }
        }
    }
    @SuppressLint("NotifyDataSetChanged")
//    private fun updateComments(comments: List<com.uyscuti.social.circuit.data.model.Comment>) {
//        commentsRecyclerViewAdapter.submitList(comments) // Assuming you are using ListAdapter
//        commentsRecyclerViewAdapter = CommentsRecyclerViewAdapter(this, this@PostDetailsActivity2)
//        commentsRecyclerViewAdapter.notifyDataSetChanged()
//    }
    private fun scrollToComment(commentId: String) {
        // This method will not throw an exception now, since commentsRecyclerView is initialized
//        val position = commentsRecyclerViewAdapter.getCommentPositionById(commentId)
        Log.d("ScrollToComment", "scrollToComment called with commentId: $commentId")

        if (position != null) {
            Log.d("ScrollToComment", "Position found: $position")

            (commentsRecyclerView.layoutManager as LinearLayoutManager)
                .scrollToPositionWithOffset(position, 0)
        }else{
            Log.d("ScrollToComment", "Position found: $position")

        }
    }
    private fun observeComments() {
        val TAG = "observeComments"
        commentsViewModel.commentsLiveData.observe(this) { it ->
            Log.d(TAG, "observeComments comments size: ${it.size}")
//            val commentsWithReplies = it.find{it.}
            val commentsWithReplies = it.filter { it.replyCount > 0 }
            Log.d(TAG, "observeComments comments with replies size: ${commentsWithReplies.size}")

        }
    }

    fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    fun showShimmer() {
        binding.shimmerLayout.startShimmerAnimation()
        binding.shimmerLayout.visibility = View.VISIBLE
    }

    fun hideShimmer() {
        binding.shimmerLayout.stopShimmerAnimation()
        binding.shimmerLayout.visibility = View.GONE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }

    private var vnList = ArrayList<String>()
    private fun startPreLoadingService() {
        Log.d("VNCache", "Preloading called")
        val preloadingServiceIntent =
            Intent(this, VideoPreLoadingService::class.java)
        preloadingServiceIntent.putStringArrayListExtra(Constants.VIDEO_LIST, vnList)
        startService(preloadingServiceIntent)
    }

    private suspend fun commentReplyLikeUnLike(commentReplyId: String): Boolean {
        val TAG = "commentReplyLikeUnLike"
        try {
            val response = retrofitInterface.apiService.likeUnLikeCommentReply(commentReplyId)
            return if (response.isSuccessful) {
                val responseBody = response.body()
                val isLiked = responseBody?.data?.isLiked ?: false
                Log.d(TAG, "commentReplyLikeUnLike $isLiked")
                isLiked
            } else {
                Log.d(TAG, " commentReplyLikeUnLike Error: ${response.message()}")
                Log.d(TAG, " commentReplyLikeUnLike Error: ${response.body()}")
                Log.d(TAG, " commentReplyLikeUnLike Error: ${response.errorBody()}")
                Log.d(TAG, " commentReplyLikeUnLike Error: ${response.code()}")
                false
            }
        } catch (e: HttpException) {
            Log.d(TAG, "Http Exception ${e.message}")
            withContext(Dispatchers.Main) {
//                showToast(this@MainActivity, "Check Internet Connection")
            }
            return false
        } catch (e: IOException) {
            Log.d(TAG, "IOException ${e.message}")
            return false
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }


    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    private fun handleDocumentUri(uri: Uri) {
        // Handle the selected document URI here
        // For example, you can retrieve the file name
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

            numberOfPages = when (documentType) {
                "doc" -> {
                    getNumberOfPagesFromUriForDoc(uri)
                }

                "docx", "xlsx", "pptx" -> {
                    getNumberOfPagesFromUriForDocx(uri)
                }

                else -> {
                    getNumberOfPagesFromUriForPDF(this, uri)
                }
            }
            if (fileSizes) {
                if (!isReply) {
                    Log.d("handleDocumentUri", "handleDocumentUri for main document")
                    uploadDocumentComment(
                        uri.toString(),
                        numberOfPages,
                        formattedFileSize,
                        documentType,
                        fileName, placeholder = true
                    )
                } else {
                    Log.d("handleDocumentUri", "This is for document reply")
                    uploadReplyDocumentComment(
                        uri.toString(),
                        numberOfPages,
                        formattedFileSize,
                        documentType,
                        fileName, placeholder = true
                    )
                }
//                                if (vUri != null) {
                toCompressUris.add(uri)
//                                }
                compressShorts(
                    "",
                    fileType = "doc",
                    fileName = fileName,
                    numberOfPages = numberOfPages,
                    documentType = documentType,
                    formattedFileSize = formattedFileSize
                )
            } else {

                if (!isReply) {
                    Log.d("handleDocumentUri", "handleDocumentUri for main document")
                    uploadDocumentComment(
                        uri.toString(),
                        numberOfPages,
                        formattedFileSize,
                        documentType,
                        fileName
                    )
                } else {
                    Log.d("handleDocumentUri", "This is for document reply")
                    uploadReplyDocumentComment(
                        uri.toString(),
                        numberOfPages,
                        formattedFileSize,
                        documentType,
                        fileName
                    )
                }
            }

        }
    }

    private fun getNumberOfPagesFromUriForPDF(context: Context, uri: Uri): Int {
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

    private fun getNumberOfPagesFromUri(context: Context, uri: Uri): Int {
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
            e.printStackTrace()
        } finally {
            inputStream?.close()
        }
        return numberOfPages
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun uploadDocumentComment(
        documentFilePathToUpload: String,
        numberOfPages: Int,
        fileSize: String,
        fileType: String,
        fileName: String, placeholder: Boolean = false, update: Boolean = false
    ) {
        Log.d("uploadDocumentComment", "uploadDocumentComment: $documentFilePathToUpload")
        Log.d(
            "uploadDocumentComment",
            "uploadDocumentComment to uri: ${documentFilePathToUpload.toUri()}"
        )
        Log.d("uploadDocumentComment", "uploadDocumentComment: isReply is $isReply")

        val mongoDbTimeStamp = generateMongoDBTimestamp()

        val file = File(documentFilePathToUpload)

        val localUpdateId = generateRandomId()
        if (isFileExists(this, documentFilePathToUpload.toUri())) {
            Log.d("uploadDocumentComment", "File exists, creating comment.......")
            val profilePic2 = settings.getString("profile_pic", "").toString()
            val avatar = Avatar("", "", url = profilePic2)
            val account =
                Account(_id = "", avatar = avatar, "", LocalStorage.getInstance(this).getUsername())
            val author = Author(
                _id = "12", account = account, firstName = "", lastName = "",
                avatar = TODO()
            )
            val documentFile = CommentFiles(
                _id = "124",
                url = documentFilePathToUpload,
                localPath = documentFilePathToUpload
            )
            val comment = Comment(
                __v = 1,
                _id = adapter!!.itemCount.toString(),
                author = author,
                content = "",
                createdAt = mongoDbTimeStamp,
                isLiked = false,
                likes = 0,
                postId = postId,
                updatedAt = mongoDbTimeStamp,
                replyCount = 0,
                images = mutableListOf(),
                audios = mutableListOf(),
                docs = mutableListOf(documentFile),
                gifs = "",
                thumbnail = mutableListOf(),
                videos = mutableListOf(),
                contentType = "docs",
                isPlaying = data?.isPlaying ?: false,
                progress = data?.progress ?: 0f,
                localUpdateId = localUpdateId,
                numberOfPages = numberOfPages.toString(),
                fileSize = fileSize,
                fileType = fileType,
                fileName = fileName
            )


            if (!placeholder) {
                val newCommentEntity =
                    CommentsFilesEntity(
                        postId,
                        "docs",
                        documentFilePathToUpload,
                        isReply = 0,
                        localUpdateId,
                        numberOfPages = numberOfPages,
                        fileSize = fileSize,
                        fileType = fileType,
                        fileName = fileName
                    )
                commentFilesViewModel.insertCommentFile(newCommentEntity)
                Log.d(
                    "uploadDocumentComment",
                    "uploadDocumentComment: inserted comment $newCommentEntity"
                )
            }

            if (update) {
                Log.d("uploadVideoComment", "updatePosition: $updatePosition")
                updateAdapter(comment, updatePosition)
                updatePosition = -1
            }

            Log.d("uploadDocumentComment", "uploadDocumentComment: comment $comment")
//        adapter.submitItems(listOf(comment) )
//            adapter!!.submitItem(comment, (adapter?.itemCount?.minus(1)!!))
//            adapter!!.submitItem(commentsAndRepliesModel, adapter!!.itemCount)

            recordedAudioFiles.clear()
            if (!update) {
                listOfReplies.add(comment)

                adapter!!.submitItem(comment, adapter!!.itemCount)
//            addCommentVN()
                shortToComment = shortsViewModel.mutableShortsList.find { it._id == postId }
                Log.d(
                    "uploadDocumentComment",
                    "uploadDocumentComment: count before ${shortToComment!!.comments}"
                )

                if (shortToComment != null) {
                    shortToComment!!.comments += 1

                    // Update the count in the mutableShortsList
                    // Update the count in the mutableShortsList
                    shortsViewModel.mutableShortsList.forEach { short ->
                        if (short._id == postId) {
                            short.comments = shortToComment!!.comments
                        }
                    }
                    val newShortToComment =
                        shortsViewModel.mutableShortsList.find { it._id == postId }
                    Log.d(
                        "uploadDocumentComment",
                        "onSubmit: count after ${newShortToComment!!.comments}"
                    )

                    EventBus.getDefault().post(ShortAdapterNotifyDatasetChanged())
                }
            }

        } else {
            Log.e(TAG, "File does not exist")
        }


    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private fun uploadReplyDocumentComment(
        documentFilePathToUpload: String,
        numberOfPages: Int,
        fileSize: String,
        fileType: String,
        fileName: String, placeholder: Boolean = false, update: Boolean = false
    ) {
        Log.d("uploadReplyDocumentComment", "uploadReplyDocumentComment: $documentFilePathToUpload")
        Log.d("uploadReplyDocumentComment", "uploadReplyDocumentComment: isReply is $isReply")


        val localUpdateId = generateRandomId()
        val profilePic2 = settings.getString("profile_pic", "").toString()
        val avatar = com.uyscuti.social.network.api.response.commentreply.allreplies.Avatar(
            "", "", url = profilePic2
        )
        val account = com.uyscuti.social.network.api.response.commentreply.allreplies.Account(
            _id = "", avatar = avatar, "", LocalStorage.getInstance(this).getUsername()
        )


        val commentReplyAuthor = com.uyscuti.social.network.api.response.commentreply.allreplies.Author(
            _id = "21", account = account, firstName = "", lastName = ""
        )

        Log.d("uploadReplyDocumentComment", "uploadReplyDocumentComment: handle reply to a comment")
//        isReply = false

        if (!placeholder) {
            val newCommentReplyEntity =
                CommentsFilesEntity(
                    postId,
                    "docs",
                    documentFilePathToUpload,
                    isReply = 1,
                    localUpdateId,
                    fileSize = fileSize,
                    fileName = fileName,
                    numberOfPages = numberOfPages,
                    fileType = fileType,
                    content = binding.input.inputEditText.text.toString()
                )
            commentFilesViewModel.insertCommentFile(newCommentReplyEntity)

            Log.d(
                "uploadReplyDocumentComment",
                "uploadReplyDocumentComment: inserted comment $newCommentReplyEntity"
            )
        }


        val mongoDbTimeStamp = generateMongoDBTimestamp()
        val documentReplyFile =
            CommentFiles(_id = "", url = documentFilePathToUpload, localPath = "docs")

        val newReply = com.uyscuti.social.network.api.response.commentreply.allreplies.Comment(
            __v = data!!.__v,
            _id = "commentId",
            author = commentReplyAuthor,
            content = binding.input.inputEditText.text.toString(),
            createdAt = mongoDbTimeStamp,
            isLiked = false,
            likes = 0,
            commentId = commentId,
            updatedAt = mongoDbTimeStamp,
            docs = mutableListOf(documentReplyFile),
//            audios = mutableListOf(vnFile),
            contentType = "docs",
            fileName = fileName,
            fileSize = fileSize,
            fileType = fileType,
            numberOfPages = numberOfPages.toString()
        )

        val replyCount = data!!.replyCount + 1
        val commentWithReplies = Comment(
            __v = data!!.__v,
            _id = data!!._id,
            author = data!!.author,
            content = data!!.content,
            createdAt = data!!.createdAt,
            isLiked = data!!.isLiked,
            likes = data!!.likes,
            postId = data!!.postId,
            updatedAt = data!!.updatedAt,
            replyCount = replyCount,
//                replies = data!!.replies
            replies = data?.replies?.toMutableList()?.apply {
                // Assuming newReply is the new reply you want to add
                add(0, newReply)
            } ?: mutableListOf(),
            isRepliesVisible = true,
            images = mutableListOf(),
            audios = mutableListOf(),
            docs = data?.docs ?: mutableListOf(),
            gifs = data?.gifs ?: "",
            thumbnail = mutableListOf(),
            videos = mutableListOf(),
            contentType = data?.contentType ?: "docs",
            isPlaying = data?.isPlaying ?: false,
            localUpdateId = localUpdateId,
            replyCountVisible = false,
            numberOfPages = data?.numberOfPages ?: "",
            fileType = data?.fileType ?: "",
            fileName = data?.fileName ?: "",
            fileSize = data?.fileSize ?: "",
            duration = data?.duration ?: "00:00",
            isReplyPlaying = data?.isReplyPlaying ?: false,
            progress = data?.progress ?: 0f,
        )

        updateReplyPosition = position
        if (update) {
            isReply = false
            Log.d("uploadVideoComment", "updatePosition: $updatePosition")
            updateAdapter(commentWithReplies, updateReplyPosition)
            updateReplyPosition = -1
        }
        if (!update) {
            listOfReplies.add(commentWithReplies)
            Log.d(
                "uploadReplyDocumentComment",
                "uploadReplyDocumentComment: comment id = data is? $commentId = ${data!!._id} on position $position"
            )
            Log.d(
                "uploadReplyDocumentComment",
                "uploadReplyDocumentComment: comment id = data is? $commentId = ${data!!._id} on position $position"
            )
            updateAdapter(commentWithReplies, position)
        }

        binding.input.inputEditText.setText("")
        binding.replyToLayout.visibility = View.GONE
    }

    private val toCompressUris = mutableListOf<Uri>()

    @SuppressLint("SetTextI18n")
    private fun compressShorts(
        durationString: String,
        fileName: String = "",
        audioType: String = "",
        fileType: String,
        numberOfPages: Int = 0,
        formattedFileSize: String = "",
        documentType: String = ""

    ) {
//        binding.mainContents.visibility = View.VISIBLE
        val uniqueId = UniqueIdGenerator.generateUniqueId()
        Log.d("progress id", uniqueId)

        lifecycleScope.launch {
            VideoCompressor.start(
                context = applicationContext,
                toCompressUris,
                isStreamable = true,
                sharedStorageConfiguration = SharedStorageConfiguration(
                    saveAt = SaveLocation.movies,
                    subFolderName = "flash_comments_compresses"
                ),
//                appSpecificStorageConfiguration = AppSpecificStorageConfiguration(
//
//                ),
                configureWith = Configuration(
                    quality = VideoQuality.MEDIUM,
//                    videoNames = uris.map { uri -> uri.pathSegments.last() },
                    videoNames = toCompressUris.map { uri -> uri.pathSegments.last() },
//                    videoNames = listOf("compressed_short"),
                    isMinBitrateCheckEnabled = false,
                ),

                listener = object : CompressionListener {
                    override fun onProgress(index: Int, percent: Float) {

                        //Update UI
                        if (percent <= 100) {
                            Log.d("Compress", "Progress: $percent")
//                            EventBus.getDefault().post(ProgressEvent(uniqueId, percent.toInt()))

                        }
                    }

                    override fun onStart(index: Int) {


                    }

                    override fun onSuccess(index: Int, size: Long, path: String?) {

                        Log.d("Compress", "comment compress successful is reply $isReply")
                        Log.d("Compress", "comment file size: ${getFileSize(size)}")
                        Log.d("Compress", "comment path: $path")
                        if (path != null) {
                            runOnUiThread {
                                if (fileType == "video") {
                                    if (!isReply) {
                                        uploadVideoComment(path, durationString, update = true)
                                    } else {
                                        uploadReplyVideoComment(path, durationString, update = true)
                                    }
                                }

                            }

                        } else {
                            Log.d("", "compress path is null")
                        }

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

    private fun uploadVideoComment(
        videoFilePathToUpload: String, durationString: String,
        placeholder: Boolean = false, update: Boolean = false
    ) {
        Log.d("uploadVideoComment", "uploadVideoComment: $videoFilePathToUpload")
        Log.d(
            "uploadVideoComment",
            "uploadVideoComment: isReply is $isReply duration string $durationString"
        )

        val mongoDbTimeStamp = generateMongoDBTimestamp()

        val file = File(videoFilePathToUpload)

        val localUpdateId = generateRandomId()
        if (file.exists()) {
            Log.d("uploadVideoComment", "File exists, creating comment.......")
            val profilePic2 = settings.getString("profile_pic", "").toString()
            val avatar = Avatar("", "", url = profilePic2)
            val account =
                Account(_id = "", avatar = avatar, "", LocalStorage.getInstance(this).getUsername())
            val author = Author(
                _id = "12", account = account, firstName = "", lastName = "",
                avatar = TODO()
            )
            val videoFile = CommentFiles(
                _id = localUpdateId,
                url = videoFilePathToUpload,
                localPath = videoFilePathToUpload
            )
            val comment = Comment(
                __v = 1,
                _id = adapter!!.itemCount.toString(),
                author = author,
                content = "",
                createdAt = mongoDbTimeStamp,
                isLiked = false,
                likes = 0,
                postId = postId,
                updatedAt = mongoDbTimeStamp,
                replyCount = 0,
                images = mutableListOf(),
                audios = mutableListOf(),
                docs = mutableListOf(),
                gifs = "",
                thumbnail = mutableListOf(),
                videos = mutableListOf(videoFile),
                contentType = "video",
                isPlaying = data?.isPlaying ?: false,
                progress = data?.progress ?: 0f,
                localUpdateId = localUpdateId,
                duration = durationString
            )

            if (!placeholder) {
                val newCommentEntity =
                    CommentsFilesEntity(
                        postId,
                        "video",
                        videoFilePathToUpload,
                        isReply = 0,
                        localUpdateId,
                        duration = durationString
                    )
                commentFilesViewModel.insertCommentFile(newCommentEntity)
                Log.d(
                    "uploadVideoComment",
                    "uploadVideoComment: inserted comment $newCommentEntity"
                )

            }

            if (update) {
                Log.d("uploadVideoComment", "updatePosition: $updatePosition")
                updateAdapter(comment, updatePosition)
                updatePosition = -1
            }

            Log.d("uploadVideoComment", "uploadVideoComment: comment $comment")

            recordedAudioFiles.clear()
            if (!update) {
                listOfReplies.add(comment)

                runOnUiThread {
                    updatePosition = adapter!!.itemCount
                    adapter!!.submitItem(comment, adapter!!.itemCount)
                }

                //            addCommentVN()
                shortToComment = shortsViewModel.mutableShortsList.find { it._id == postId }


                if (shortToComment != null) {
                    Log.d(
                        "uploadVideoComment",
                        "uploadVideoComment: count before ${shortToComment!!.comments}"
                    )
                    shortToComment!!.comments += 1

                    shortsViewModel.mutableShortsList.forEach { short ->
                        if (short._id == postId) {
                            short.comments = shortToComment!!.comments
                        }
                    }
                    val newShortToComment =
                        shortsViewModel.mutableShortsList.find { it._id == postId }
                    Log.d(
                        "uploadVideoComment",
                        "onSubmit: count after ${newShortToComment!!.comments}"
                    )

                    EventBus.getDefault().post(ShortAdapterNotifyDatasetChanged())
                }
            }
        } else {
            Log.e(TAG, "File does not exist")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private fun uploadReplyVideoComment(
        videoToUpload: String, durationString: String,
        placeholder: Boolean = false, update: Boolean = false
    ) {
        Log.d("uploadReplyImageComment", "uploadReplyImageComment: $videoToUpload")
        Log.d("uploadReplyImageComment", "uploadReplyImageComment: isReply is $isReply")
        val localUpdateId = generateRandomId()
        val profilePic2 = settings.getString("profile_pic", "").toString()
        val avatar = com.uyscuti.social.network.api.response.commentreply.allreplies.Avatar(
            "", "", url = profilePic2
        )
        val account = com.uyscuti.social.network.api.response.commentreply.allreplies.Account(
            _id = "", avatar = avatar, "", LocalStorage.getInstance(this).getUsername()
        )
        val commentReplyAuthor = com.uyscuti.social.network.api.response.commentreply.allreplies.Author(
            _id = "21", account = account, firstName = "", lastName = ""
        )
        Log.d("uploadReplyImageComment", "uploadReplyImageComment: handle reply to a comment")
        //if it clash on upload un comment the line below//
        if (!placeholder) {
            val newCommentReplyEntity =
                CommentsFilesEntity(
                    postId,
                    "video",
                    videoToUpload,
                    isReply = 1,
                    localUpdateId,
                    duration = durationString,
                    content = binding.input.inputEditText.text.toString()
                )
            commentFilesViewModel.insertCommentFile(newCommentReplyEntity)

            Log.d(
                "uploadReplyImageComment",
                "uploadReplyImageComment: inserted comment $newCommentReplyEntity"
            )
        }

        lifecycleScope.launch {

        }
        val mongoDbTimeStamp = generateMongoDBTimestamp()
        val videoFile = CommentFiles(_id = "", url = videoToUpload, localPath = "video")

        val newReply = com.uyscuti.social.network.api.response.commentreply.allreplies.Comment(
            __v = data!!.__v,
            _id = "commentId",
            author = commentReplyAuthor,
            content = binding.input.inputEditText.text.toString(),
            createdAt = mongoDbTimeStamp,
            isLiked = false,
            likes = 0,
            commentId = commentId,
            updatedAt = mongoDbTimeStamp,
            videos = mutableListOf(videoFile),
            contentType = "video",
            duration = durationString
        )

        val replyCount = data!!.replyCount + 1
        val commentWithReplies = Comment(
            __v = data!!.__v,
            _id = data!!._id,
            author = data!!.author,
            content = data!!.content,
            createdAt = data!!.createdAt,
            isLiked = data!!.isLiked,
            likes = data!!.likes,
            postId = data!!.postId,
            updatedAt = data!!.updatedAt,
            replyCount = replyCount,
//                replies = data!!.replies
            replies = data?.replies?.toMutableList()?.apply {
                // Assuming newReply is the new reply you want to add
                add(0, newReply)
            } ?: mutableListOf(),
            isRepliesVisible = true,
            images = mutableListOf(),
            audios = mutableListOf(),
            docs = mutableListOf(),
            gifs = data?.gifs ?: "",
            thumbnail = mutableListOf(),
            videos = data?.videos ?: mutableListOf(),
            contentType = data?.contentType ?: "video",
            isPlaying = data?.isPlaying ?: false,
            localUpdateId = localUpdateId,
            replyCountVisible = false,
            duration = data?.duration ?: "00: 00",
            numberOfPages = data?.numberOfPages ?: "",
            fileSize = data?.fileSize ?: "",
            fileName = data?.fileName ?: "",
            fileType = data?.fileType ?: "",
            isReplyPlaying = data?.isReplyPlaying ?: false,
            progress = data?.progress ?: 0f
        )


        updateReplyPosition = position
        if (update) {
            isReply = false
            Log.d("uploadVideoComment", "updatePosition: $updatePosition")
            updateAdapter(commentWithReplies, updateReplyPosition)
            updateReplyPosition = -1
        }
        if (!update) {

            updateAdapter(commentWithReplies, position)
            listOfReplies.add(commentWithReplies)
            Log.d(
                "uploadReplyImageComment",
                "uploadReplyImageComment: comment id = data is? $commentId = ${data!!._id} on position $position"
            )
            Log.d(
                "uploadReplyImageComment",
                "uploadReplyImageComment: comment id = data is? $commentId = ${data!!._id} on position $position"
            )
        }
        binding.input.inputEditText.setText("")
        binding.replyToLayout.visibility = View.GONE
    }


    private fun likeCommentReplyFromViewsActivity(event: LikeCommentReply) {

        val TAG = "likeCommentReplyFromViewsActivity"

        Log.d(
            "likeCommentReplyFromViewsActivity",
            "likeCommentReplyFromViewsActivity: is liked count is ${event.commentReply.isLiked}"
        )

        val itemToUpdate = event.comment.replies.find { it._id == event.commentReply._id }
        itemToUpdate!!.isLiked = event.commentReply.isLiked
        if (event.commentReply.isLiked) {
            itemToUpdate.likes += 1

        } else {
            itemToUpdate.likes -= 1
        }

        if (event.commentReply._id == itemToUpdate._id) {
            Log.d(TAG, "likeCommentReplyFromViewsActivity: ids are equal")
        } else {
            Log.d(TAG, "likeCommentReplyFromViewsActivity: ids not equal")
        }


        Log.d(
            "likeCommentReplyFromViewsActivity",
            "likeCommentReplyFromViewsActivity: is liked count is ${event.commentReply}"
        )
        adapter?.updateItem(event.position, event.comment)

        if (isInternetAvailable(this)) {
            Log.d(
                TAG,
                "likeCommentReplyFromViewsActivity: item to update id ${itemToUpdate._id} and comment reply id ${event.commentReply._id}"
            )
            lifecycleScope.launch {
                val result = commentReplyLikeUnLike(itemToUpdate._id)
                Log.d(TAG, "likeCommentReplyFromViewsActivity server result: $result")

            }
        } else {
            Log.d(TAG, "likeCommentReplyFromViewsActivity: cant like offline")
        }
    }


    private fun allShortComments(page: Int) {
        val TAG = "allShortComments"
        lifecycleScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                if (page == 1) {
                    showShimmer()
                } else {
                    showProgressBar()
                }
            }
            try {
                val commentsWithReplies = commentViewModel.fetchShortComments(postId, page)
                Log.d("commentsWithReplies", "allShortComments: size ${commentsWithReplies.size}")
                withContext(Dispatchers.Main) {

                    if (page == 1) {
                        hideShimmer()
                    } else {
                        hideProgressBar()
                    }

                    commentsWithReplies
                        .filter { commentAudioCache ->
                            commentAudioCache.audios.isNotEmpty() // Filter out comments without any audios
                        }
                        .map { commentAudioCache ->
                            vnList.add(commentAudioCache.audios[0].url)
                        }


                    commentsWithReplies
                        .flatMap { commentReplies ->
                            commentReplies.replies.filter { commentRepliesCache ->
                                commentRepliesCache.audios.isNotEmpty() // Filter out replies without any audios
                            }.map { commentRepliesCache ->
                                commentRepliesCache.audios[0].url
                            }
                        }
                        .forEach { url ->
                            vnList.add(url)
                        }

                    startPreLoadingService()
                    adapter!!.submitItems(commentsWithReplies)
                }

            } catch (e: Exception) {
                Log.e("UserProfileShortsViewModel", "Exception: ${e.message}")
                lifecycleScope.launch {
//                    hideShimmer()
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

    private fun generateSampleData(count: Int): List<Comment> {
        val itemList = mutableListOf<Comment>()
        for (i in 1..count) {
            //itemList.add(Comment("Item $i"))
        }
        return itemList
    }

    private fun onDownLoadClick(url: String, fileLocation: String) {
        Log.d(
            "Download",
            "OnDownload $url  \nto path : $fileLocation"
        )

        val permissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, permissions, requestCode)
        } else {
            // You have permission, proceed with your file operations

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                // Check if the permission is not granted
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // Request the permission
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        WRITE_EXTERNAL_STORAGE_REQUEST_CODE
                    )
                } else {
                    // Permission already granted, proceed with your code
//                                    downloadFile(mUrl)
//                    downld(url, fileLocation)
                    download(url, fileLocation)
                }

//                                downlod(url, progressbar, fileDisplay, fileLocation, message)
            } else {
                download(url, fileLocation)
            }
        }
    }

    @kotlin.OptIn(DelicateCoroutinesApi::class)
    private fun download(
        mUrl: String,
        fileLocation: String,
    ) {
        Log.d("Download", "directory path - $fileLocation")

        if (mUrl.startsWith("/storage/") || mUrl.startsWith("/storage/")) {

            Log.d("Download", "Cannot download a local file")
            return
        }
        //STORAGE_FOLDER += fileLocation
        val STORAGE_FOLDER = "/Download/Flash/$fileLocation"
//        val fileName = mUrl.split("/").last()
        val fileName = generateUniqueFileName(mUrl)
        val storageDirectory =
            Environment.getExternalStorageDirectory().toString() + STORAGE_FOLDER + "/$fileName"
        Log.d("Download", "directory path - $storageDirectory")
        val file = File(Environment.getExternalStorageDirectory().toString() + STORAGE_FOLDER)
        if (!file.exists()) {
            file.mkdirs()
        }
        GlobalScope.launch(Dispatchers.IO) {
            val url = URL(mUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept-Encoding", "identity")
            connection.connect()
            try {
                if (connection.responseCode in 200..299) {
                    val fileSize = connection.contentLength
                    val inputStream = connection.inputStream
                    val outputStream = FileOutputStream(storageDirectory)
                    var bytesCopied: Long = 0
                    val buffer = ByteArray(1024)
                    var bytes = inputStream.read(buffer)
                    while (bytes >= 0) {
                        bytesCopied += bytes
                        val downloadProgress =
                            (bytesCopied.toFloat() / fileSize.toFloat() * 100).toInt()
                        runOnUiThread {
                            //progressbar.visibility = View.VISIBLE
//                            progressbar.progress = downloadProgress
//                        progressCountTv.text = "$downloadProgress%"
                            binding.downloadProgressBarLayout.visibility = View.VISIBLE
                            shortsDownloadImageView.setBackgroundResource(R.drawable.shorts_download_animation)
                            wifiAnimation =
                                shortsDownloadImageView.background as AnimationDrawable
                            wifiAnimation!!.start()
                            Log.d("Download", "Progress $downloadProgress")
                            binding.shortsDownloadProgressBar.progress = downloadProgress
                        }
                        outputStream.write(buffer, 0, bytes)
                        bytes = inputStream.read(buffer)
                    }
                    // progressbar.visibility = View.GONE
                    //progressCountTv.visibility = View.GONE
                    runOnUiThread {
                        // Update the UI components here
//                        progressbar.visibility = View.GONE

//                        coreChatSocketClient.sendDownLoadedEvent(myId,message.id)

                        Log.d("Download", "File Downloaded : $storageDirectory")
                        binding.downloadProgressBarLayout.visibility = View.GONE

                        wifiAnimation!!.stop()

                        // Show notification
                        showNotification(
                            this@PostDetailsActivity2,
                            "Download Complete",
                            "File downloaded successfully.",
                            generateRandomNotificationId(),
                            storageDirectory
                        )
                        val downloadedFile = File(storageDirectory)


//                    progressCountTv.visibility = View.GONE
                        //videoHolder.progressCountTv.visibility = View.VISIBLE

                    }

                    outputStream.close()
                    inputStream.close()
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            this@PostDetailsActivity2,
                            "Not successful",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("DownloadFailed", e.message.toString())

                e.printStackTrace()
                runOnUiThread {
//                    progressbar.visibility = View.GONE
                    binding.downloadProgressBarLayout.visibility = View.GONE
                }
            }
        }
    }

    private fun generateRandomNotificationId(): Int {
        val randomUUID = UUID.randomUUID()
        return abs(randomUUID.hashCode())
    }

    private fun showNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int,
        fileLocation: String
    ) {
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        // Create a Notification Channel for Android Oreo and above
        val channelId = "channel_id"
        val channel = NotificationChannel(
            channelId,
            "Channel Name",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = "Channel Description"
        channel.enableLights(true)
        channel.lightColor = android.graphics.Color.BLUE
        notificationManager.createNotificationChannel(channel)
        // Create an Intent to view the video file
        val fileUri = Uri.parse(fileLocation)
        val intent = Intent(Intent.ACTION_VIEW, fileUri)
        intent.setDataAndType(fileUri, "video/*") // Set the MIME type for videos

        // Create a PendingIntent to be triggered when the notification is clicked
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Create the notification
        val builder = NotificationCompat.Builder(context, "channel_id")
            .setSmallIcon(R.drawable.flash21) // Set your notification icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent) // Set the PendingIntent

        // Show the notification with a unique ID
        notificationManager.notify(notificationId, builder.build())
    }

    private fun generateUniqueFileName(originalUrl: String): String {
        val timestamp =
            SimpleDateFormat("yyyy_MM_dd_HHmmss", Locale.getDefault()).format(Date())
        val originalFileName = originalUrl.split("/").last()
        val fileExtension = MimeTypeMap.getFileExtensionFromUrl(originalFileName)
        val randomString = UUID.randomUUID().toString().substring(0, 8)
        return "$timestamp-$randomString.$fileExtension"
    }


    private fun onCommentsClick(postId: String) {
        val TAG = "allShortComments"
        this.postId = postId
        commentCount = 0
        Log.d("showBottomSheet", "showBottomSheet: inside show bottom sheet")
        val items = generateSampleData(50)
        Log.d("showBottomSheet", "showBottomSheet: postId $postId")
        adapter = CommentsRecyclerViewAdapter(this, this@PostDetailsActivity2)
        binding.recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.itemAnimator = null
        adapter?.setDefaultRecyclerView(this, R.id.recyclerView)

        if (adapter?.itemCount == 0) {
//            adapter.
        }
        toggleMotionLayoutVisibility()
        adapter!!.setOnPaginationListener(object : AdPaginatedAdapter.OnPaginationListener {
            override fun onCurrentPage(page: Int) {
//                Toast.makeText(requireContext(), "Page $page loaded!", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "currentPage: page number $page")

            }

            override fun onNextPage(page: Int) {
                lifecycleScope.launch(Dispatchers.Main) {
//                    loadMoreShorts(page)
                    Log.d(TAG, "onNextPage: page number $page")
                    allShortComments(page)
                }
            }

            override fun onFinish() {
//
//                commentIdToNavigate?.let { commentId ->
//                    Log.d("position", "position working")
                Log.d(TAG, "finished: page number")
//                    scrollToComment(commentId)

            }
        })

        lifecycleScope.launch(Dispatchers.Main) {
            allShortComments(adapter!!.startPage)
//            allCommentReplies(adapter!!.startPage)
        }
        observeComments()
    }

    @SuppressLint("SetTextI18n")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun toggleReplyToTextView(event: ToggleReplyToTextView) {

        val TAG = "toggleReplyToTextView"
        isReply = true
        commentId = event.comment._id
        data = event.comment
        position = event.position
        Log.d(
            TAG,
            "toggleReplyToTextView: comment id $commentId data comment id ${data!!._id} comment position $position"
        )
        Log.d(TAG, "toggleReplyToTextView: data ${event.comment}")
        var username = event.comment.author!!.account.username

        binding.replyToLayout.visibility = View.VISIBLE
        binding.replyToTextView.text = "Replying to $username"
        binding.exitReply.setOnClickListener {
            binding.replyToLayout.visibility = View.GONE
            binding.input.inputEditText.setText("")
            isReply = false
        }
        binding.input.inputEditText.setText("@$username")
        binding.input.inputEditText.setSelection(binding.input.inputEditText.text!!.length)
    }


    @SuppressLint("SetTextI18n")
    private fun toggleReplyFromViewsActivity(
        data: Comment,
        pos: Int
    ) {
        val TAG = "toggleReplyToTextView"
        isReply = true
        commentId = data._id
        this.data = data
        position = pos
        Log.d(
            TAG,
            "toggleReplyToTextView: comment id $commentId data comment id ${data._id} comment position $position"
        )
//        Log.d(TAG, "toggleReplyToTextView: data ${event.comment}")
        val username = data.author!!.account.username

        binding.replyToLayout.visibility = View.VISIBLE

        binding.replyToTextView.text = "Replying to $username"
        binding.exitReply.setOnClickListener {
            binding.replyToLayout.visibility = View.GONE
            binding.input.inputEditText.setText("")
            isReply = false
        }

        binding.input.inputEditText.setText("@$username ")
        binding.input.inputEditText.setSelection(binding.input.inputEditText.text!!.length)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == R_CODE && resultCode == RESULT_OK && data != null) {
            // Handle the result from the adapter
            // You can extract data from the Intent if needed
            val modifiedData =
                data.getSerializableExtra("data") as Comment
            val currentReplyData =
                data.getSerializableExtra("currentReplyComment") as com.uyscuti.social.network.api.response.commentreply.allreplies.Comment?

            val position = data.getIntExtra("position", 0)
            val reply = data.getBooleanExtra("reply", false)
            val updateReplyLikes = data.getBooleanExtra("updateReplyLikes", false)
            val updateLike = data.getBooleanExtra("updateLike", false)

////            Log.d("onActivityResult", "data $data")
//            Log.d("onActivityResult", "reply data is liked ${currentReplyData?.isLiked}")
//            Log.d("onActivityResult", "position $position")
//            Log.d("onActivityResult", "reply $reply")
//            Log.d("onActivityResult", "updateReplyLike $updateReplyLikes")
            if (reply) {
                toggleReplyFromViewsActivity(modifiedData, position)
            }
            if (updateLike) {
                likeUnLikeCommentFromViewsActivity(position, modifiedData)
            }

            if (updateReplyLikes) {
                val likeCommentReply = LikeCommentReply(currentReplyData!!, modifiedData, position)
                likeCommentReplyFromViewsActivity(likeCommentReply)
            }

//            EventBus.getDefault().post(ToggleReplyToTextView(modifiedData, position))

        } else if (requestCode == COMMENT_VIDEO_CODE && resultCode == RESULT_OK && data != null) {
            // Handle the result from the adapter
            // You can extract data from the Intent if needed
            val modifiedData =
                data.getSerializableExtra("data") as Comment
            val currentReplyData =
                data.getSerializableExtra("currentReplyComment") as com.uyscuti.social.network.api.response.commentreply.allreplies.Comment?

            val position = data.getIntExtra("position", 0)
            val reply = data.getBooleanExtra("reply", false)
            val updateReplyLikes = data.getBooleanExtra("updateReplyLikes", false)
            val updateLike = data.getBooleanExtra("updateLike", false)

            if (reply) {
                toggleReplyFromViewsActivity(modifiedData, position)
            }
            if (updateLike) {
                likeUnLikeCommentFromViewsActivity(position, modifiedData)
            }

            if (updateReplyLikes) {
                val likeCommentReply = LikeCommentReply(currentReplyData!!, modifiedData, position)
                likeCommentReplyFromViewsActivity(likeCommentReply)
            }

//            EventBus.getDefault().post(ToggleReplyToTextView(modifiedData, position))

        } else if (requestCode == GIF_CODE && resultCode == RESULT_OK && data != null) {

            val gifUri = data.getStringExtra("gifUri")
            Log.d("onActivityResult", "gifUri: ${gifUri.toString()}")
            if (!isReply) {
                uploadGifComment(gifUri.toString())
            } else {
                uploadGifReplyComment(gifUri.toString())
            }


        } else {
            Log.d("onActivityResult", "onActivityResult failed")
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun likeCommentReply(event: LikeCommentReply) {

        val TAG = "likeCommentReply"

        Log.d("ReplyCommentAdapter", "likeCommentReply: inside like comment reply event bus ")
        Log.d(
            "ReplyCommentAdapter",
            "likeCommentReply: inside like comment reply event bus data.replies.size ${event.comment.replies.size}"
        )

        val itemToUpdate = event.comment.replies.find { it._id == event.commentReply._id }

        if (event.commentReply.isLiked) {
            itemToUpdate!!.likes += 1
        } else {
            itemToUpdate!!.likes -= 1
        }

        if (event.commentReply._id == itemToUpdate?._id) {
            Log.d(TAG, "likeCommentReply: ids are equal")
        } else {
            Log.d(TAG, "likeCommentReply: ids not equal")
        }

        Log.d(
            "CommentsRecyclerViewAdapter",
            "likeCommentReply: likes count is ${event.commentReply.likes}"
        )
        adapter?.updateItem(event.position, event.comment)

        if (isInternetAvailable(this)) {
//            Log.d(TAG, "likeUnLikeComment: internet is available")
            Log.d(
                TAG,
                "likeUnLikeComment: item to update id ${itemToUpdate?._id} and comment reply id ${event.commentReply._id}"
            )
//            Log.d(TAG, "likeUnLikeComment: internet is available")
            lifecycleScope.launch {
                val result = commentReplyLikeUnLike(itemToUpdate!!._id)
                Log.d(TAG, "likeUnLikeComment server result: $result")

                if (result) {
                }
            }
        } else {
            Log.d(TAG, "likeUnLikeComment: cant like offline")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun uploadGifComment(gifFilePathToUpload: String) {
        Log.d("uploadGifComment", "uploadGifComment: $gifFilePathToUpload")
        Log.d("uploadGifComment", "uploadGifComment: isReply is $isReply")

        val mongoDbTimeStamp = generateMongoDBTimestamp()

        val file = File(gifFilePathToUpload)

        val localUpdateId = generateRandomId()
        if (gifFilePathToUpload.isNotEmpty()) {
            Log.d("uploadGifComment", "File exists, creating comment.......")
            val profilePic2 = settings.getString("profile_pic", "").toString()
            val avatar = Avatar("", "", url = profilePic2)
            val account =
                Account(_id = "", avatar = avatar, "", LocalStorage.getInstance(this).getUsername())
            val author = Author(
                _id = "12", account = account, firstName = "", lastName = "",
                avatar = TODO()
            )
//            val gifFile = CommentFiles(
//                _id = "124",
//                url = gifFilePathToUpload,
//                localPath = gifFilePathToUpload
//            )
            val comment = Comment(
                __v = 1,
                _id = adapter!!.itemCount.toString(),
                author = author,
                content = "",
                createdAt = mongoDbTimeStamp,
                isLiked = false,
                likes = 0,
                postId = postId,
                updatedAt = mongoDbTimeStamp,
                replyCount = 0,
                images = mutableListOf(),
                audios = mutableListOf(),
                docs = mutableListOf(),
                gifs = gifFilePathToUpload,
                thumbnail = mutableListOf(),
                videos = mutableListOf(),
                contentType = "gif",
                isPlaying = data?.isPlaying ?: false,
                progress = data?.progress ?: 0f,
                localUpdateId = localUpdateId
            )
            val newCommentEntity =
                CommentsFilesEntity(
                    postId,
                    "gif",
                    gifFilePathToUpload,
                    isReply = 0,
                    localUpdateId,
                )
            commentFilesViewModel.insertCommentFile(newCommentEntity)
            Log.d("uploadGifComment", "uploadGifComment: inserted comment $newCommentEntity")

            Log.d("uploadGifComment", "uploadGifComment: comment $comment")
            listOfReplies.add(comment)
//            recordedAudioFiles.clear()
            adapter!!.submitItem(comment, adapter!!.itemCount)
            shortToComment = shortsViewModel.mutableShortsList.find { it._id == postId }
            if (shortToComment != null) {
                shortToComment!!.comments += 1
                Log.d(
                    "uploadGifComment",
                    "uploadGifComment: count before ${shortToComment!!.comments}"
                )
                shortsViewModel.mutableShortsList.forEach { short ->
                    if (short._id == postId) {
                        short.comments = shortToComment!!.comments
                    }
                }
                val newShortToComment = shortsViewModel.mutableShortsList.find { it._id == postId }
                Log.d("uploadGifComment", "onSubmit: count after ${newShortToComment!!.comments}")

                EventBus.getDefault().post(ShortAdapterNotifyDatasetChanged())
            }
        } else {
//            Log.e( "File does not exist")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private fun uploadGifReplyComment(gifToUpload: String) {
        Log.d("uploadGifImageComment", "uploadGifImageComment: $gifToUpload")
        Log.d("uploadGifImageComment", "uploadGifImageComment: isReply is $isReply")


        val localUpdateId = generateRandomId()
        val profilePic2 = settings.getString("profile_pic", "").toString()
        val avatar = com.uyscuti.social.network.api.response.commentreply.allreplies.Avatar(
            "", "", url = profilePic2
        )
        val account = com.uyscuti.social.network.api.response.commentreply.allreplies.Account(
            _id = "", avatar = avatar, "", LocalStorage.getInstance(this).getUsername()
        )


        val commentReplyAuthor = com.uyscuti.social.network.api.response.commentreply.allreplies.Author(
            _id = "21", account = account, firstName = "", lastName = ""
        )

        Log.d("uploadGifImageComment", "uploadGifImageComment: handle reply to a comment")
        isReply = false
//        val newCommentReplyEntity = CommentsFilesEntity(commentId, vnToUpload, vnToUpload, isReply = 1)
        //if it clash on upload un comment the line below//
        val newCommentReplyEntity =
            CommentsFilesEntity(
                postId,
                "gif",
                gifToUpload,
                isReply = 1,
                localUpdateId,
                content = binding.input.inputEditText.text.toString()
            )
        commentFilesViewModel.insertCommentFile(newCommentReplyEntity)

        Log.d(
            "uploadGifImageComment",
            "uploadGifImageComment: inserted comment $newCommentReplyEntity"
        )
        lifecycleScope.launch {
//                allCommentReplies2(1, commentId)
        }
        val mongoDbTimeStamp = generateMongoDBTimestamp()
        val gifFile = CommentFiles(_id = "", url = gifToUpload, localPath = "gif")

        val newReply = com.uyscuti.social.network.api.response.commentreply.allreplies.Comment(
            __v = data!!.__v,
            _id = "commentId",
            author = commentReplyAuthor,
            content = binding.input.inputEditText.text.toString(),
            createdAt = mongoDbTimeStamp,
            isLiked = false,
            likes = 0,
            commentId = commentId,
            updatedAt = mongoDbTimeStamp,
            gifs = gifToUpload,
//            audios = mutableListOf(vnFile),
            contentType = "gif"
        )

        val replyCount = data!!.replyCount + 1
        val commentWithReplies = Comment(
            __v = data!!.__v,
            _id = data!!._id,
            author = data!!.author,
            content = data!!.content,
            createdAt = data!!.createdAt,
            isLiked = data!!.isLiked,
            likes = data!!.likes,
            postId = data!!.postId,
            updatedAt = data!!.updatedAt,
            replyCount = replyCount,
//                replies = data!!.replies
            replies = data?.replies?.toMutableList()?.apply {
                // Assuming newReply is the new reply you want to add
                add(0, newReply)
            } ?: mutableListOf(),
            isRepliesVisible = true,
            images = data?.images ?: mutableListOf(),
            audios = data?.audios ?: mutableListOf(),
            docs = data?.docs ?: mutableListOf(),
            gifs = data?.gifs ?: "",
            thumbnail = data?.thumbnail ?: mutableListOf(),
            videos = data?.videos ?: mutableListOf(),
            contentType = data?.contentType ?: "gif",
            isPlaying = data?.isPlaying ?: false,
            localUpdateId = localUpdateId,
            replyCountVisible = false,
            progress = data?.progress ?: 0f,
            isReplyPlaying = data?.isReplyPlaying ?: false,
            duration = data?.duration ?: "00:00",
            fileType = data?.fileType ?: "",
            fileName = data?.fileName ?: "",
            fileSize = data?.fileSize ?: "",
            numberOfPages = data?.numberOfPages ?: ""
        )

        listOfReplies.add(commentWithReplies)
        Log.d(
            "uploadGifImageComment",
            "uploadGifImageComment: comment id = data is? $commentId = ${data!!._id} on position $position"
        )
        Log.d(
            "uploadGifImageComment",
            "uploadGifImageComment: comment id = data is? $commentId = ${data!!._id} on position $position"
        )
        updateAdapter(commentWithReplies, position)
        binding.input.inputEditText.setText("")
        binding.replyToLayout.visibility = View.GONE
    }


    private fun likeUnLikeCommentFromViewsActivity(
        position: Int, data: Comment
    ) {
        val TAG = "likeUnLikeCommentFromViewsActivity"

        Log.d(
            "likeUnLikeCommentFromViewsActivity",
            "likeUnLikeComment: data.isLiked ${data.isLiked} position $position"
        )
//        var updatedComment : com.uyscuti.social.circuit.data.model.Comment? = null
        val updatedComment = if (data.isLiked) {
            data.copy(
                likes = data.likes + 1,
            )
        } else {
            data.copy(
                likes = data.likes - 1,
            )
        }
        Log.d(
            "likeUnLikeCommentFromViewsActivity",
            "likeUnLikeComment: likes count is ${data.likes}"
        )
        adapter?.updateItem(position, updatedComment)

        if (isInternetAvailable(this)) {
            Log.d(TAG, "likeUnLikeCommentFromViewsActivity: internet is available")
//            Log.d(TAG, "likeUnLikeComment: internet is available")
            lifecycleScope.launch {
                val result = commentLikeUnLike(data._id)
                Log.d(TAG, "likeUnLikeCommentFromViewsActivity server result: $result")

                if (result) {
                }
            }
        } else {
            Log.d(TAG, "likeUnLikeCommentFromViewsActivity: cant like offline")
        }

    }

    private val selectGifLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let { uri ->
                    val fileName = getFileName(uri)
                    if (fileName?.endsWith(".gif") == true) {
                        // Handle the selected GIF file here
                        // For example, you can display it in an ImageView
                        // val inputStream = contentResolver.openInputStream(uri)
                        // val gifBitmap = BitmapFactory.decodeStream(inputStream)
                        // imageView.setImageBitmap(gifBitmap)
                        Log.d(
                            "selectGifLauncher",
                            "selectGifLauncher: is reply $isReply gifUrlType $gifUrlType "
                        )
//                        uploadGifToServer(uri.toString(), gifUrlType)
//                        if (!isReply) {
//                            uploadGifComment(uri.toString(), gifUrlType)
//                        } else {
////                        uploadGifComment(uri.toString(), "happy")
//                            uploadGifReplyComment(uri.toString(), gifUrlType)
//                        }
//                        if (dialogDismissed) {
//
//                        } else {
//                            Log.d("selectGifLauncher", "selectGifLauncher: dialog dismissed $dialogDismissed")
//                        }

//                        customDialog.dismiss()
                    } else {
                        // Not a GIF file, handle error or inform the user
                        Log.d("selectGifLauncher", "selectGifLauncher: Selected file is not GIF")

                    }
                }
            }
        }


    private suspend fun allCommentRepliesOnce(
        page: Int, commentId: String
    )
            : CommentReplyResults {
        val TAG = "allCommentReplies"
        try {
            var hasNextPage: Boolean
            val pageNumber = page + 1
            val comments: MutableList<com.uyscuti.social.network.api.response.commentreply.allreplies.Comment> =
                mutableListOf()
            withContext(Dispatchers.IO) {
                // Handle UI-related tasks if needed
                val response =
                    retrofitInterface.apiService.getCommentReplies(commentId, page.toString())
                val responseBody = response.body()

//               val  comments = responseBody?.data?.comments ?: emptyList()
                responseBody?.data?.comments?.let { comments.addAll(it) }
                hasNextPage = responseBody?.data?.hasNextPage ?: false
//                pageNumber = responseBody!!.data.page
                Log.d(TAG, "allCommentRepliesOnce: has next page $hasNextPage")
                val uniqueCommentsList = comments.distinctBy { it._id }

                val filteredNewItems = uniqueCommentsList.filter { newItem ->
                    commentsReplyViewModel.commentsReplyMutableList.none { existingItem ->
                        existingItem._id == newItem._id
                    }
                }

                withContext(Dispatchers.Main) {
                    if (page == 1) {
                        commentsReplyViewModel.commentsReplyMutableList.clear()
                    }
                    commentsReplyViewModel.commentsReplyMutableList.addAll(filteredNewItems)

//                    Log.d(TAG, "allCommentReplies: $comments")
//                    Log.d(
//                        TAG,
//                        "allShortComments: total comments for this post: ${filteredNewItems.size}"
//                    )
                    Log.d(
                        TAG, "allShortComments: total comments for this post: ${comments.size}"
                    )
                }
                for (i in comments) {
                    Log.d(TAG, "All comments images ${i.images}")
                }
//                Log.d(TAG, "Comments $comments")
            }
            return CommentReplyResults(comments, hasNextPage, pageNumber)
//            return CommentReplyResults(commentsReplyViewModel.commentsReplyMutableList, hasNextPage, pageNumber)
        } catch (e: Exception) {
            Log.e("UserProfileShortsViewModel", "Exception: ${e.message}")
            lifecycleScope.launch {
                // Handle UI-related tasks if needed
                Toast.makeText(
                    this@PostDetailsActivity2, e.message, Toast.LENGTH_LONG
                ).show()
            }
            e.printStackTrace()
        }
        return CommentReplyResults(Collections.emptyList(), false, page)
    }


    @SuppressLint("CheckResult", "SuspiciousIndentation")
    private fun updateNotification(post: GetPostById) {
        shortPlayer = ExoPlayer.Builder(this).build()
        playerView.player = shortPlayer
        shortSeekBar = binding.shortsSeekBar
        videoUrl = post.data.images[0].url
        val mediaItem = MediaItem.fromUri(videoUrl!!)
        shortPlayer.setMediaItem(mediaItem)
        shortPlayer.prepare()
        Log.e("PostDetails", "Invalid video URL: $videoUrl")
        shortPlayer.playWhenReady = true
        shortPlayer.repeatMode = Player.REPEAT_MODE_ONE


//        exoPlayer.addListener(playbackStateListener)
        shortPlayer.addListener(shortPlaybackStateListener)
        exoPlayer.removeListener(playbackStateListener)

        binding.shortUsername.text = post.data.author.account.username
        binding.tvReadMoreLess.text = post.data.content
        binding.likeCount.text = post.data.likes.toString()
        binding.commentsCount.text = post.data.comments.toString()
        binding.favoriteCounts.text = post.data.isBookmarked.toString()

        val isFavorite = post.data.isBookmarked
        if (isFavorite) {
            totalFavorites += 1
            // Set the liked drawable
            YoYo.with(Techniques.Tada)
                .duration(700)
                .repeat(1)
                .playOn(binding.favorite)
            binding.favorite.setImageResource(R.drawable.filled_favorite)
        } else {

            totalFavorites -= 1
            // Set the unliked drawable
            YoYo.with(Techniques.Tada)
                .duration(700)
                .repeat(1)
                .pivotX(2.6F)
                .playOn(binding.favorite)
            binding.favorite.setImageResource(R.drawable.favorite_svgrepo_com__1_)
        }
        val isLiked = post.data.isLiked
        if (isLiked) {
            totalLikes += 1

            // Set the liked drawable
            binding.btnLike.setImageResource(R.drawable.filled_favorite_like)
            YoYo.with(Techniques.Tada)
                .duration(700)
                .repeat(1)
                .pivotX(2.6F)
                .playOn(binding.btnLike)
        } else {
            // Set the unliked drawable
            totalLikes -= 1
            data?.isLiked ?: false
            binding.btnLike.setImageResource(R.drawable.favorite_svgrepo_com)
            YoYo.with(Techniques.Tada)
                .duration(700)
                .repeat(1)
                .playOn(binding.btnLike)
        }
        ////username also declared
        binding.shortUsername.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("fragment", "profile")
            startActivity(intent)
        }
        //username
        val profileViewImage = binding.profileImageView
        Glide.with(binding.profileImageView.context)
            .load(post.data.author.account.avatar.url)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .apply(RequestOptions.placeholderOf(R.drawable.flash21))
            .into(profileViewImage)

        binding.profileImageView.setOnClickListener {
            Log.d("profile", "clicked on image")
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("fragment", "profile")
            startActivity(intent)
        }
        binding.downloadBtn.setOnClickListener {
            if (videoUrl == null) {
                throw RuntimeException("video data is null")
            }
//            onDownLoadClick(videoUrl!!,"flashShorts")

            Log.d("downloadBtn", "Download : $videoUrl")
//            downloadProgressBarLayout.visibility = View.VISIBLE
            shortsDownloadImageView.visibility = View.VISIBLE
            shortsDownloadProgressBar.visibility = View.VISIBLE
//            download(videoUrl!!,"flashShorts")
            onClickListeners.onDownloadClick(videoUrl!!, "flashShorts")
            Log.d("downloadBtn", "Download is working")

        }

        binding.btnLike.setOnClickListener {
            handleLikeClick(postId, binding.likeCount, binding.btnLike, post)
            Log.d("btnLike", "clicked on like")
            EventBus.getDefault().post(ShortsLikeUnLike(postId, isLiked))

        }
        binding.shareBtn.setOnClickListener {

        }
        binding.favorite.setOnClickListener {
            handleFavoriteClick(postId, binding.favorite, post)
        }

    }

    override fun onSeekBarChanged(progress: Int) {
//        TODO("Not yet implemented")
    }


    override fun onDownloadClick(url: String, fileLocation: String) {
        Log.d(
            "Download",
            "OnDownload $url  \nto path : $fileLocation"
        )

        val permissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, permissions, requestCode)
        } else {
            // You have permission, proceed with your file operations

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                // Check if the permission is not granted
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // Request the permission
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        WRITE_EXTERNAL_STORAGE_REQUEST_CODE
                    )
                } else {

                    download(url, fileLocation)
                }

//                                downlod(url, progressbar, fileDisplay, fileLocation, message)
            } else {
                download(url, fileLocation)
            }
        }

    }

    override fun onShareClick(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onUploadCancelClick() {
        TODO("Not yet implemented")
    }

    private fun handleLikeClick(
        postId: String,
        likeCount: TextView,
        btnLike: ImageButton,
        shortsEntity: GetPostById
    ) {
        Log.d("handleLikeClick", "handleLikeClick: before ${shortsViewModel.isLiked}")
        shortsViewModel.isLiked = !shortsViewModel.isLiked
        Log.d("handleLikeClick", "handleLikeClick: after ! ${shortsViewModel.isLiked}")
        EventBus.getDefault().post(ShortsLikeUnLike2(postId))

        if (!shortsEntity.data.isLiked) {

//                shortsViewModel.totalLikes += 1
            shortsEntity.data.likes += 1
            likeCount.text = shortsEntity.data.likes.toString()

            btnLike.setImageResource(R.drawable.filled_favorite_like)
            YoYo.with(Techniques.Tada)
                .duration(700)
                .repeat(1)
                .playOn(btnLike)

            shortsEntity.data.isLiked = true
            shortsEntity.data.likes += shortsViewModel.totalLikes

            val myShorts = userProfileShortsViewModel.mutableShortsList.find { it._id == postId }
            var myFavoriteShorts =
                userProfileShortsViewModel.mutableFavoriteShortsList.find { it._id == postId }

            if (myShorts != null) {
                Log.d("handleLikeClick", "handleLikeClick: short found id: ${myShorts._id}")
                myShorts.isLiked = true
                myShorts.likes += 1
            } else {
                Log.d("handleLikeClick", "handleLikeClick: short not found")
            }
            if (myFavoriteShorts != null) {
                Log.d("handleLikeClick", "handleLikeClick: short found id: ${myFavoriteShorts._id}")
                myFavoriteShorts.isLiked = true
                myFavoriteShorts.likes += 1
            } else {
                Log.d("handleLikeClick", "handleLikeClick: short not found")
            }
            shortsViewModel.isLiked = true
        } else {
            shortsEntity.data.likes -= 1
            likeCount.text = shortsEntity.data.likes.toString()
            var myShorts = userProfileShortsViewModel.mutableShortsList.find { it._id == postId }
            var myFavoriteShorts =
                userProfileShortsViewModel.mutableFavoriteShortsList.find { it._id == postId }

            if (myShorts != null) {
                Log.d("handleLikeClick", "handleLikeClick: short found id: ${myShorts._id}")
                myShorts.isLiked = false
                myShorts.likes -= 1
            } else {
                Log.d("handleLikeClick", "handleLikeClick: short not found")
            }
            if (myFavoriteShorts != null) {
                Log.d("handleLikeClick", "handleLikeClick: short found id: ${myFavoriteShorts._id}")
                myFavoriteShorts.isLiked = false
                myFavoriteShorts.likes -= 1
            } else {
                Log.d("handleLikeClick", "handleLikeClick: short not found")
            }
            btnLike.setImageResource(R.drawable.favorite_svgrepo_com)
            shortsEntity.data.isLiked = false
            shortsEntity.data.likes += shortsViewModel.totalLikes
            shortsViewModel.isLiked = false
            YoYo.with(Techniques.Tada)
                .duration(700)
                .repeat(1)
                .playOn(btnLike)
        }
    }

    private fun handleFavoriteClick(postId: String, button: ImageView, shortsEntity: GetPostById) {

        val TAG = "handleFavoriteClick"
        shortsViewModel.isFavorite = !shortsViewModel.isFavorite
        EventBus.getDefault().post(ShortsFavoriteUnFavorite(postId))

        if (!shortsEntity.data.isBookmarked) {
            button.setImageResource(R.drawable.filled_favorite)
            YoYo.with(Techniques.Tada)
                .duration(700)
                .repeat(1)
                .playOn(button)
            shortsEntity.data.isBookmarked = true
            shortsEntity.data.likes += shortsViewModel.totalLikes

            shortsViewModel.isFavorite = true

            var myShorts = userProfileShortsViewModel.mutableShortsList.find { it._id == postId }
            var myFavoriteShorts =
                userProfileShortsViewModel.mutableFavoriteShortsList.find { it._id == postId }

            if (myShorts != null) {
                Log.d("handleLikeClick", "handleLikeClick: short found id: ${myShorts._id}")
                myShorts.isBookmarked = true
            } else {
                Log.d("handleLikeClick", "handleLikeClick: short not found")
            }
            if (myFavoriteShorts != null) {
                myFavoriteShorts.isBookmarked = true
            }
//            val convertedShort = shortsEntityToUserShortsEntity(postId, shortsEn)
//            userProfileShortsViewModel.mutableFavoriteShortsList.add(0, convertedShort)
        } else {
            button.setImageResource(R.drawable.favorite_svgrepo_com__1_)
            shortsEntity.data.isBookmarked = false
            shortsEntity.data.likes += shortsViewModel.totalLikes
            shortsViewModel.isFavorite = false

            var myShorts = userProfileShortsViewModel.mutableShortsList.find { it._id == postId }
            var myFavoriteShorts =
                userProfileShortsViewModel.mutableFavoriteShortsList.find { it._id == postId }

            if (myShorts != null) {
                Log.d("handleLikeClick", "handleLikeClick: short found id: ${myShorts._id}")
                myShorts.isBookmarked = false
            } else {
                Log.d("handleLikeClick", "handleLikeClick: short not found")
            }

            if (myFavoriteShorts != null) {
                Log.d("handleLikeClick", "handleLikeClick: short found id: ${myFavoriteShorts._id}")
                myFavoriteShorts.isBookmarked = false
//                val userShortsEntity = shortsEntityToUserShortsEntity(shortsEntity)
                Log.d(
                    TAG,
                    "handleFavoriteClick: mutableFavoriteShortsList size before ${userProfileShortsViewModel.mutableFavoriteShortsList.size}"
                )
//                userProfileShortsViewModel.mutableFavoriteShortsList.add(userShortsEntity)
                // Remove the item if it exists in the list
                userProfileShortsViewModel.mutableFavoriteShortsList.removeIf { it._id == postId }
//                myFavoriteShorts.f
                Log.d(
                    TAG,
                    "handleFavoriteClick: mutableFavoriteShortsList size after ${userProfileShortsViewModel.mutableFavoriteShortsList.size}"
                )

            } else {
                Log.d("handleLikeClick", "handleLikeClick: short not found")
            }

            YoYo.with(Techniques.Tada)
                .duration(700)
                .repeat(1)
                .playOn(button)
        }
    }

    private fun shortsEntityToUserShortsEntity(serverResponseItem: ShortsEntity): UserShortsEntity {
        return UserShortsEntity(
            __v = serverResponseItem.__v,
            _id = serverResponseItem._id,
            content = serverResponseItem.content,
            author = serverResponseItem.author,
            comments = serverResponseItem.comments,
            createdAt = serverResponseItem.createdAt,
            images = serverResponseItem.images,
            isBookmarked = serverResponseItem.isBookmarked,
            isLiked = serverResponseItem.isLiked,
            likes = serverResponseItem.likes,
            tags = serverResponseItem.tags,
            updatedAt = serverResponseItem.updatedAt,
            thumbnail = serverResponseItem.thumbnail,
            // map other properties...
        )
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }

    override fun onPause() {
        super.onPause()
        if (::exoPlayer.isInitialized) {
            if (player?.isPlaying == true) {
                player?.pause()
            }
            if (exoPlayer.isPlaying == true) {
                exoPlayer.stop()
            }
        }
//        if(mediaRecorder?.)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPlaying()
        stopRecording()
        commentAudioStop()
        stopWaveRunnable()
        stopRecordWaveRunnable()
        MediaLoader.getInstance(this).destroy()
        exoPlayer.removeListener(playbackStateListener)
        shortPlayer.removeListener(shortPlaybackStateListener)

//        shortPlayer.removeListener(shortPlaybackStateListener)
// have added a short player here to remove when back pressed
        if (::shortPlayer.isInitialized) {
            Log.d("DestroyDetails", "onDestroy: shortPlayer initialized")
            if (shortPlayer.isPlaying) {
                Log.d("DestroyDetails", "onDestroy: shortPlayer is playing, stopping now")
                shortPlayer.stop()
            } else {
                Log.d("DestroyDetails", "onDestroy: shortPlayer is not playing")
            }
            shortPlayer.release()
            Log.d("DestroyDetails", "onDestroy: shortPlayer released")
        } else {
            Log.d("DestroyDetails", "onDestroy: shortPlayer not initialized")
        }

        Log.d("DestroyDetails", "onDestroy: player playing: ${shortPlayer.isPlaying}")
        if (::exoPlayer.isInitialized) {
            if (exoPlayer.isPlaying) {
                exoPlayer.stop()
            }

            exoPlayer.release()
        }
        Log.d("DestroyDetails", "onDestroy: player playing: ${exoPlayer.isPlaying}")
    }

    private fun stopWaveRunnable() {
        try {
            waveHandler.removeCallbacks(waveRunnable)
            isDurationOnPause = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
//    private val waveRunnable = Runnable {
////                    TODO("Not yet implemented")
//    }

    private lateinit var audioDurationTVCount: TextView
    private val waveRunnable = object : Runnable {
        override fun run() {
            Log.d(
                "isDurationOnPause",
                " in comment audio runnable isDurationOnPause is $isDurationOnPause"
            )

            if (!isDurationOnPause) {
                val currentPosition = exoPlayer.currentPosition?.toFloat()!!

                waveProgress = currentPosition
                if (isReplyVnPlaying) {
                    adapter!!.updateReplyWaveProgress(currentPosition, audioFormWave)
                } else {
                    adapter!!.updateWaveProgress(currentPosition, wavePosition)

                }
                audioDurationTVCount.text = String.format(
                    "%s",
                    TrimVideoUtils.stringForTime(currentPosition)
                )
            }
            waveHandler.postDelayed(this, 20)
        }
    }

    @SuppressLint("SuspiciousIndentation")
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
            adapter?.setSecondWaveFormProgress(0f, currentCommentAudioPosition)
            adapter?.setReplySecondWaveFormProgress(0f, currentCommentAudioPosition)
        } else {
            adapter?.setSecondSeekBarProgress(0f, currentCommentAudioPosition)
            adapter?.setReplySecondSeekBarProgress(0f, currentCommentAudioPosition)
        }
//        if (isReplyVnPlaying) {
//        } else {
//        }
        currentCommentAudioPosition = RecyclerView.NO_POSITION
        currentCommentAudioPath = ""
        adapter?.resetAudioPlay()
//        player?.let { mediaPlayer ->
//            if (mediaPlayer.isPlaying) {
//                mediaPlayer.stop()
//            }
//            mediaPlayer.release()
//        }
//        player = null
        exoPlayer = ExoPlayer.Builder(this).build()
//        playerView.player = exoPlayer
        exoPlayer.prepare()
        exoPlayer?.let { exoPlayer ->
            if (exoPlayer.isPlaying) {
                exoPlayer.stop()
            }
            exoPlayer.release()
        }
    }


    override fun onSubmit(input: CharSequence?): Boolean {
        val TAG = "onSubmit"
        hideKeyboard(binding.input.inputEditText)
        val localUpdateId = generateRandomId()
        if (!isReply) {
            val mongoDbTimeStamp = generateMongoDBTimestamp()

            val profilePic2 = settings.getString("profile_pic", "").toString()
            val avatar = Avatar("", "", url = profilePic2)
            val account =
                Account(_id = "", avatar = avatar, "", LocalStorage.getInstance(this).getUsername())
            val author = Author(
                _id = "12", account = account, firstName = "", lastName = "",
                avatar = TODO()
            )
            val comment = Comment(
                __v = 1,
                _id = adapter!!.itemCount.toString(),
                author = author,
                content = input.toString(),
                createdAt = mongoDbTimeStamp,
                isLiked = false,
                likes = 0,
                postId = postId,
                updatedAt = mongoDbTimeStamp,
                replyCount = 0,
                images = mutableListOf(),
                audios = mutableListOf(),
                docs = mutableListOf(),
                gifs = "",
                thumbnail = mutableListOf(),
                videos = mutableListOf(),
                contentType = "text",
                isPlaying = data?.isPlaying ?: false,
                localUpdateId = localUpdateId
            )

            val newCommentEntity =
                ShortCommentEntity(postId, input.toString(), localUpdateId = localUpdateId)
            shortsCommentViewModel.insertComment(newCommentEntity)
            Log.d(TAG, "onSubmit: inserted comment $newCommentEntity")

            listOfReplies.add(comment)

            Log.d(TAG, "onSubmit: comment $comment")
//        adapter.submitItems(listOf(comment) )
//            adapter!!.submitItem(comment, (adapter?.itemCount?.minus(1)!!))
//            adapter!!.submitItem(commentsAndRepliesModel, adapter!!.itemCount)

            adapter!!.submitItem(comment, adapter!!.itemCount)
            shortToComment = shortsViewModel.mutableShortsList.find { it._id == postId }

            if (shortToComment != null) {
                shortToComment!!.comments += 1
                Log.d(TAG, "onSubmit: count before ${shortToComment!!.comments}")
                // Update the count in the mutableShortsList
                // Update the count in the mutableShortsList
                shortsViewModel.mutableShortsList.forEach { short ->
                    if (short._id == postId) {
                        short.comments = shortToComment!!.comments
                    }
                }
                val newShortToComment = shortsViewModel.mutableShortsList.find { it._id == postId }
                Log.d(TAG, "onSubmit: count after ${newShortToComment!!.comments}")

                EventBus.getDefault().post(ShortAdapterNotifyDatasetChanged())
            }
        } else {

            val profilePic2 = settings.getString("profile_pic", "").toString()
            val avatar = com.uyscuti.social.network.api.response.commentreply.allreplies.Avatar(
                "", "", url = profilePic2
            )
            val account = com.uyscuti.social.network.api.response.commentreply.allreplies.Account(
                _id = "", avatar = avatar, "", LocalStorage.getInstance(this).getUsername()
            )


            val commentReplyAuthor = com.uyscuti.social.network.api.response.commentreply.allreplies.Author(
                _id = "21", account = account, firstName = "", lastName = ""
            )

            Log.d(TAG, "onSubmit: handle reply to a comment")
            isReply = false
            val newCommentReplyEntity =
                ShortCommentReply(commentId, input.toString(), localUpdateId)
            roomCommentReplyViewModel.insertCommentReply(newCommentReplyEntity)
            Log.d(TAG, "onSubmit: inserted comment $newCommentReplyEntity")
            lifecycleScope.launch {
//                allCommentReplies2(1, commentId)
            }
            val mongoDbTimeStamp = generateMongoDBTimestamp()
            val newReply = com.uyscuti.social.network.api.response.commentreply.allreplies.Comment(
                __v = data!!.__v,
                _id = "commentId",
                author = commentReplyAuthor,
                content = input.toString(),
                createdAt = mongoDbTimeStamp,
                isLiked = false,
                likes = 0,
                commentId = commentId,
                updatedAt = mongoDbTimeStamp,
            )

            val replyCount = data!!.replyCount + 1
            val commentWithReplies =
                Comment(
                    __v = data!!.__v,
                    _id = data!!._id,
                    author = data!!.author,
                    content = data!!.content,
                    createdAt = data!!.createdAt,
                    isLiked = data!!.isLiked,
                    likes = data!!.likes,
                    postId = data!!.postId,
                    updatedAt = data!!.updatedAt,
                    replyCount = replyCount,
//                replies = data!!.replies
                    replies = data?.replies?.toMutableList()?.apply {
                        // Assuming newReply is the new reply you want to add
                        add(0, newReply)
                    } ?: mutableListOf(),
                    isRepliesVisible = true,
                    images = data?.images ?: mutableListOf(),
                    audios = data?.audios ?: mutableListOf(),
                    docs = mutableListOf(),
                    gifs = "",
                    thumbnail = mutableListOf(),
                    videos = data?.images ?: mutableListOf(),
                    contentType = data?.contentType ?: "text",
                    isPlaying = data?.isPlaying ?: false,
                    localUpdateId = localUpdateId,
                    replyCountVisible = false
                )
//            val updatedComment = commentWithReplies.copy(replies = commentReplies.toMutableList(), isRepliesVisible = isRepliesVisible)

            listOfReplies.add(commentWithReplies)

            Log.d(
                TAG,
                "onSubmit: comment id = data is? $commentId = ${data!!._id} on position $position"
            )
            Log.d(
                TAG,
                "onSubmit: comment id = data is? $commentId = ${data!!._id} on position $position"
            )
            updateAdapter(commentWithReplies, position)
//            addCommentReply(input.toString())
        }
        binding.replyToLayout.visibility = View.GONE
        return true
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onAddVoiceNote() {
        val TAG = "onAddVoiceNote1"
        Log.d(TAG, "onAddVoiceNote: start VN clicked")
        binding.VNLayout.visibility = View.VISIBLE
        binding.playAudioLayout.visibility = View.GONE
        binding.waveForm.visibility = View.VISIBLE
        binding.timerTv.visibility = View.VISIBLE
        startRecording()
        EventBus.getDefault().post(PauseShort(true))
    }

    private fun stopPlaying() {
        binding.playVnAudioBtn.setImageResource(R.drawable.play_svgrepo_com)
        player?.release()
        player = null
        isAudioVNPlaying = false
        vnRecordAudioPlaying = false
        isOnRecordDurationOnPause = false
        stopRecordWaveRunnable()
        binding.wave.progress = 0F
        vnRecordProgress = 0
    }

    private fun stopRecordWaveRunnable() {
        try {
            waveHandler.removeCallbacks(onRecordWaveRunnable)
            isOnRecordDurationOnPause = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val onRecordWaveRunnable = object : Runnable {
        override fun run() {
//            Log.d("isDurationOnPause" , " in comment audio runnable isDurationOnPause is $isDurationOnPause")
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

    private fun startWaveRunnable() {
        try {
            Log.d(
                "isDurationOnPause",
                " in comment audio start wave isDurationOnPause is $isDurationOnPause"
            )
            Log.d("StartWave", "Start waves")
            waveHandler.removeCallbacks(waveRunnable)
            waveHandler.post(waveRunnable)
            isDurationOnPause = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private var currentHandler: Handler? = null
    private fun initializeSeekBar(exoPlayer: ExoPlayer) {
        audioSeekBar.max = exoPlayer.duration.toInt()
// Remove callbacks from the current handler, if any
        currentHandler?.removeCallbacksAndMessages(currentHandler)
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable {
            override fun run() {
                try {

                    if (!isVnAudioToPlay && exoPlayer.isPlaying) {
                        Log.d(
                            "initializeSeekBar",
                            "Position $currentCommentAudioPosition is reply $isReplyVnPlaying"
                        )
                        exoPlayer.let {
                            if (isReplyVnPlaying) {
                                adapter!!.updateReplySeekBarProgress(
                                    it.currentPosition.toFloat(),
                                    audioSeekBar
                                )

//                                audioSeekBar.progress = it.currentPosition.toInt()
//                                seekBarProgress = it.currentPosition.toFloat()
//                                audioDurationTVCount.text = String.format(
//                                    "%s",
//                                    TrimVideoUtils.stringForTime(it.currentPosition.toFloat())
//                                )
                            } else {
//                                adapter!!.updateWaveProgress(currentPosition, wavePosition)
                                audioSeekBar.progress = it.currentPosition.toInt()
                                seekBarProgress = it.currentPosition.toFloat()
                                audioDurationTVCount.text = String.format(
                                    "%s",
                                    TrimVideoUtils.stringForTime(it.currentPosition.toFloat())
                                )
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

    private fun initializeShortSeekBar(shortPlayer: ExoPlayer) {
//        shortSeekBar.max = shortPlayer.duration.toInt()
        Log.d("ShortSeekBar", "Short SeekBar initialized")
//        audioSeekBar.max = exoPlayer.duration.toInt()
// Remove callbacks from the current handler, if any
        currentHandler?.removeCallbacksAndMessages(currentHandler)
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable {
            override fun run() {
                try {
//                    updating shortseekbar is added here
                    if (shortPlayer.isPlaying) {
                        val currentPosition = shortPlayer.currentPosition.toFloat()
                        Log.d("ShortSeekBar", "Position $currentPosition")
                        shortSeekBar.max = shortPlayer.duration.toInt()
                        shortSeekBar.progress = currentPosition.toInt()
                        Log.d(
                            "initializeSeekBar",
                            "Position $currentCommentAudioPosition is reply $isReplyVnPlaying"
                        )
                        shortPlayer.let {
                            handler.postDelayed(this, 1000)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }, 0)
        // Set the new handler as the current handler
        currentHandler = handler
    }

    var maxDuration = 0L

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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun audioWave(event: AudioPlayerHandler) {
        val TAG = "audioWave"
        audioFormWave = event.audioWave
//        event.audioWave.setSampleFrom(event.audioPath)
        audioDurationTVCount = event.leftDuration
        wavePosition = event.position
        Log.d(TAG, "audioWave: position $wavePosition ")
    }

    private fun shortPlaybackStateListener() = object : Player.Listener {
        @SuppressLint("SetTextI18n")
        override fun onPlaybackStateChanged(state: Int) {
            when (state) {
                ExoPlayer.STATE_ENDED -> {
//                     The video playback ended. Move to the next video if available.
                    Log.d(
                        "playbackStateListener",
                        "commentAudioStartPlaying: comment audio completed"
                    )

                }
                // Add other cases if needed
                Player.STATE_BUFFERING -> {
                }

                Player.STATE_IDLE -> {
                }

                Player.STATE_READY -> {
                    Log.d("TAG", "STATE_READY_VIDEO")
//                    have added the code that initialises shortseekbar
                    shortPlayer.let {
                        initializeShortSeekBar(it)
                    }
                    Log.d("TAG", "STATE_READY_VIDEO")
//                    startUpdatingShortSeekBar()
//                    updateShortSeekBar()
                }

                else -> {
                    Log.d("TAG", "STOP SEEK BAR_VIDEO")
                    // Stop updating seek bar in other states
                    stopUpdatingShortSeekBar()
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
                            adapter?.updateReplyWaveProgress(0f, audioFormWave)
                            if (isReplyVnPlaying) {
                                Log.d("isReplyVnPlaying", "isReplyVnPlaying $isReplyVnPlaying")
                                val handler = Handler()

                                handler.postDelayed({
                                    adapter?.refreshMainComment(position)
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
                    }
                    Log.d(
                        "audioSeekBar",
                        "currentCommentAudioPosition $currentCommentAudioPosition"
                    )

                    adapter?.refreshMainComment(position)
                    adapter?.changePlayingStatus()
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
                        Log.d("TAG", "STATE_READY")
                        exoPlayer.let {
                            initializeSeekBar(it)
                        }
                    }
                    Log.d("TAG", "STATE_READY")
                    startUpdatingSeekBar()
//                    shortsAdapter.setSeekBarProgress(exoPlayer!!.currentPosition.toInt())

                }

                else -> {
                    Log.d("TAG", "STOP SEEK BAR")
                    // Stop updating seek bar in other states
                    stopUpdatingSeekBar()
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

    private var updateSeekBarJob: Job? = null

    private fun startUpdatingSeekBar() {
        updateSeekBarJob = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                // Update seek bar based on current playback position
                exoPlayer.let { player ->
                    // Update seek bar based on current playback position
                    //                        shortsAdapter.setSeekBarProgress(20)
                }
                updateSeekBar()
                delay(50) // Update seek bar every second (adjust as needed)
            }
        }
    }

    private fun startUpdatingShortSeekBar() {
        updateSeekBarJob = CoroutineScope(Dispatchers.Main).launch {
            while (true) {

                // Update seek bar based on current playback position
                shortPlayer.let { player ->
                    // Update seek bar based on current playback position
//                                    shortsAdapter.setSeekBarProgress(20)
                }
                updateShortSeekBar()
                delay(50) // Update seek bar every second (adjust as needed)
            }
        }
    }

    private fun stopUpdatingSeekBar() {
        updateSeekBarJob?.cancel()
    }

    private fun stopUpdatingShortSeekBar() {
        updateSeekBarJob?.cancel()
    }

    override fun onViewRepliesClick(
        data: Comment,
        repliesRecyclerView: RecyclerView,
        position: Int
    ) {
        val TAG = "onViewRepliesClick"
    }

    @SuppressLint("SetTextI18n")
    override fun onViewRepliesClick(
        data: Comment,
        position: Int,
        commentRepliesTV: TextView,
        hideCommentReplies: TextView,
        repliesRecyclerView: RecyclerView,
        isRepliesVisible: Boolean,
        page: Int
    ) {
        val TAG = "onViewRepliesClick"
        lifecycleScope.launch {

            Log.d(TAG, "onViewRepliesClick:  page number $page")
            if (data.hasNextPage) {
                commentRepliesTV.text = "Loading..."

                if (commentRepliesTV.text.equals("Loading...")) {
                    Log.d(TAG, "onViewRepliesClick: contains loading...")
                    withContext(Dispatchers.Main) {
                        hideCommentReplies.visibility = View.GONE
                    }
                }
                val commentReplies = allCommentRepliesOnce(page, data._id)
                val commentWithReplies = Comment(
                    __v = data.__v,
                    _id = data._id,
                    author = data.author,
                    content = data.content,
                    createdAt = data.createdAt,
                    isLiked = data.isLiked,
                    likes = data.likes,
                    postId = data.postId,
                    updatedAt = data.updatedAt,
                    replyCount = data.replyCount,
                    replies = mutableListOf(),
                    hasNextPage = data.hasNextPage,
                    images = data.images,
                    audios = data.audios,
                    docs = data.docs,
                    gifs = data.gifs,
                    thumbnail = data.thumbnail,
                    videos = data.videos,
                    contentType = data.contentType,
                    isPlaying = data.isPlaying,
                    localUpdateId = data.localUpdateId,
                    duration = data.duration,
                    fileName = data.fileName,
                    fileSize = data.fileSize,
                    fileType = data.fileType,
                    numberOfPages = data.numberOfPages

//                    pageNumber =
                )


                Log.d(
                    TAG,
                    "onViewRepliesClick: has next page ${commentReplies.hasNextPage} page number ${commentReplies.pageNumber}"
                )
                val updatedComment = commentWithReplies.copy(
//                        replies = commentReplies.comments,
                    replies = data.replies.toMutableList().apply {
                        // Assuming newReply is the new reply you want to add
                        addAll(commentReplies.comments)
                    },
                    isRepliesVisible = isRepliesVisible,
                    hasNextPage = commentReplies.hasNextPage,
                    pageNumber = commentReplies.pageNumber
                )

                withContext(Dispatchers.Main) {
                    adapter?.updateItem(position, updatedComment)
//                commentRepliesTV.text = "Hide replies"
                    hideCommentReplies.visibility = View.VISIBLE
                }
            }
        }
    }


    override fun onReplyButtonClick(
        position: Int,
        data: Comment
    ) {
        binding.replyToLayout.visibility = View.VISIBLE
    }

    override fun likeUnLikeComment(
        position: Int,
        data: Comment
    ) {
        val TAG = "likeUnLikeComment"

        Log.d("CommentsRecyclerViewAdapter", "likeUnLikeComment: data.isLiked ${data.isLiked}")
//        var updatedComment : com.uyscuti.social.circuit.data.model.Comment? = null
        val updatedComment = if (data.isLiked) {
            data.copy(
                likes = data.likes + 1,
            )
        } else {
            data.copy(
                likes = data.likes - 1,
            )
        }
        Log.d("CommentsRecyclerViewAdapter", "likeUnLikeComment: likes count is ${data.likes}")
        adapter?.updateItem(position, updatedComment)

        if (isInternetAvailable(this)) {
            Log.d(TAG, "likeUnLikeComment: internet is available")
//            Log.d(TAG, "likeUnLikeComment: internet is available")
            lifecycleScope.launch {
                val result = commentLikeUnLike(data._id)
                Log.d(TAG, "likeUnLikeComment server result: $result")

                if (result) {
                }
            }
        } else {
            Log.d(TAG, "likeUnLikeComment: cant like offline")
        }

    }


    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(
            NetworkCapabilities.TRANSPORT_CELLULAR
        ))
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateMongoDBTimestamp(): String {
        val timestamp = OffsetDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")

        return timestamp.format(formatter)
    }

    private fun addComment() {
        val TAG = "addComment"
        Log.d("addCommentReply", "addComment: is reply $isReply")
        if (isInternetAvailable(this)) {

            shortsCommentViewModel.allComments.observe(this) {

                if (it.isNotEmpty()) {
                    Log.d(
                        TAG,
                        "addComment: comments in room count is ${it.size}, localUpdateId ${it[0].localUpdateId}"
                    )
                    commentsViewModel.comment(
                        it[0].postId,
                        it[0].content,
                        "text",
                        it[0].localUpdateId,
                        it[0].isFeedComment
                    )
                    shortsCommentViewModel.viewModelScope.launch {
                        val isDeleted = shortsCommentViewModel.deleteCommentById(it[0].postId)
                        if (isDeleted) {
                            // Deletion was successful, update UI or perform other actions
                            Log.d(TAG, "comment deleted successfully.")
                        } else {
                            // Deletion was not successful, handle accordingly
                            Log.d(TAG, "Failed to delete comment.")
                        }
                    }

                } else {
                    Log.d(TAG, "onSubmit: Room database has no comments")
                }
            }


        } else {
            Log.d(TAG, "addComment: no internet connection")
        }

    }

    private fun updateAdapter(
        data: Comment, position: Int
    )
    {
        val TAG = "updateAdapter"
        Log.d("updateItem", "updated main item images" + data.images)
        Log.d("UpdateItem", "reply count visible ${data.replyCountVisible}")
//        isReply = false

        adapter?.updateItem(position, data)
    }

    private suspend fun commentLikeUnLike(commentId: String): Boolean {
        val TAG = "commentLikeUnLike"
        try {
            val response = retrofitInterface.apiService.likeUnLikeComment(commentId)
            return if (response.isSuccessful) {
                val responseBody = response.body()
                val isLiked = responseBody?.data?.isLiked ?: false
                Log.d(TAG, "likeUnLikeComment $isLiked")
                isLiked
            } else {
                Log.d(TAG, " likeUnLikeComment Error: ${response.message()}")
                false
            }
        } catch (e: HttpException) {
            Log.d(TAG, "Http Exception ${e.message}")
            withContext(Dispatchers.Main) {
//                showToast(this@MainActivity, "Check Internet Connection")
            }
            return false
        } catch (e: IOException) {
            Log.d(TAG, "IOException ${e.message}")
            return false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)


        if (requestCode == REQUEST_CODE) {
            permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
        }
        if (requestCode == READ_EXTERNAL_STORAGE_REQUEST_CODE) {
            permissionGranted2 = grantResults[0] == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun initializeCommentsBottomSheet() {
        val rootView = binding.motionLayout
        emojiPopup = EmojiPopup(rootView, binding.input.inputEditText)
        inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val input = binding.input
        input.setInputListener(this)
        input.setAttachmentsListener(this)
        input.setVoiceListener(this)
        input.setEmojiListener(this)
        input.setGifListener(this)
        val toolbar = binding.toolbar
        binding.motionLayout.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {}

            override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {
                toolbar.visibility = View.GONE
            }
            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {}

            override fun onTransitionCompleted(p0: MotionLayout?, currentId: Int) {
                // Check if MotionLayout is in the certain state
                if (currentId == R.id.end) {
                    // Bring other views to the front
                    toolbar.visibility = View.VISIBLE
                    binding.motionLayout.visibility = View.VISIBLE

                }
            }
        })
        binding.click.setOnClickListener {
            Log.d("ClickListener", "click is working")
            toggleMotionLayoutVisibility()
        }
    }
    private fun handleNotificationClick(notificationType: String) {
        when (notificationType) {
            "postLiked" -> {
                binding.motionLayout.visibility = View.GONE
            }
            else -> {
                // Toggle MotionLayout visibility for other types
                if (binding.motionLayout.visibility == View.VISIBLE) {
                    binding.motionLayout.transitionToEnd()
                } else {
                    binding.motionLayout.visibility = View.VISIBLE
                    binding.motionLayout.transitionToStart()
                }
            }
        }

    }

    @OptIn(UnstableApi::class)
    private fun toggleMotionLayoutVisibility() {
        val currentVisibility = binding.motionLayout.visibility

        val TAG = "allShortComments"

        if (currentVisibility == View.VISIBLE) {

            // If currently visible, make it gone
            binding.motionLayout.visibility = View.GONE
            binding.VNLayout.visibility = View.GONE
            binding.replyToLayout.visibility = View.GONE
            binding.input.inputEditText.setText("")
            isReply = false
            commentsViewModel.resetLiveData()
//          hideKeyboard()
            hideKeyboard(binding.input.inputEditText)
            deleteRecording()
            stopPlaying()
            commentAudioStop()
            stopWaveRunnable()
            stopRecordWaveRunnable()
            exoPlayer.release()

            // Stop any ongoing audio or recording
            if (mediaRecorder != null) {
                mediaRecorder!!.release()
                mediaRecorder = null
            }

            // Release exoPlayer if initialized
            if (::exoPlayer.isInitialized) {
                if (exoPlayer.isPlaying) {
                    exoPlayer.stop()
                }
            } else {
                exoPlayer.release()
            }
        } else {
            var currentState = binding.motionLayout.currentState

            // If currently gone, make it visible and set the transition to start
            binding.motionLayout.visibility = View.VISIBLE
            binding.motionLayout.transitionToEnd()
        }
    }

    override fun onAddEmoji() {
        initView()
    }

    private fun initView() {
        Thread {
            runOnUiThread {
                emojiShowing = if (emojiPopup.isShowing && emojiShowing) {
                    // Close the emoji keyboard
                    emojiPopup.dismiss() // Dismisses the Popup.
                    inputMethodManager.showSoftInput(
                        binding.input.inputEditText, InputMethodManager.SHOW_IMPLICIT
                    )
                    false
                } else {
                    // Open the emoji keyboard
                    inputMethodManager.hideSoftInputFromWindow(
                        binding.input.inputEditText.windowToken, 0
                    )
                    emojiPopup.toggle() // Toggles visibility of the Popup.
                    true
                }
            }
        }.start()
    }

    override fun onCommentsClick(position: Int, data: UserShortsEntity) {
        Log.d("showBottomSheet", "showBottomSheet: inside show bottom sheet")
        adapter = CommentsRecyclerViewAdapter(this, this@PostDetailsActivity2)
        toggleMotionLayoutVisibility()
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
//        Log.d("isReplyVnPlaying", "isReplyVnPlaying $isReplyVnPlaying")
        Log.d(
            "toggleAudioPlayer",
            "progress received from adapter $progress is reply $isReply    is seeking $isSeeking is vn audio $isVnAudio"
        )
//
        if (currentCommentAudioPath == audioToPlayPath) {
//            Log.d(TAG, "toggleAudioPlayer: currentCommentAudioPosition == position")
            if (seekTo) {
                Log.d("SeekTo", "Seek to $progress")
                EventBus.getDefault().post(PauseShort(true))
                isDurationOnPause = false
//                Log.d(
//                    "isDurationOnPause",
//                    " in play toggle audio player isDurationOnPause is $isDurationOnPause"
//                )
                exoPlayer.seekTo(progress.toLong())
                exoPlayer.play()
            } else if (isSeeking) {
                Log.d("toggleAudioPlayer", "user is seeking so i paused the audio")
                exoPlayer.pause()
            } else if (exoPlayer.isPlaying == true) {
                Log.d(
                    "toggleAudioPlayer",
                    "toggleAudioPlayer: current player is playing then pause"
                )
//                audioPlayPauseBtn.setImageResource(R.drawable.play_svgrepo_com)

                if (isVnAudio) {
                    Log.d("waveProgress", "toggleAudioPlayer: $waveProgress")

                    adapter?.setReplySecondWaveFormProgress(waveProgress, position)
                    adapter?.setSecondWaveFormProgress(waveProgress, position)
                } else {
                    //for seek bar
                    adapter?.setSecondSeekBarProgress(seekBarProgress, position)
                    adapter?.setReplySecondSeekBarProgress(seekBarProgress, position)
                }


                exoPlayer.pause()
                isDurationOnPause = true


//                Log.d(
//                    "toggleAudioPlayer",
//                    " in pause toggle audio player isDurationOnPause is $isDurationOnPause"
//                )

            } else {
                Log.d(
                    "toggleAudioPlayer",
                    "toggleAudioPlayer: current player is not playing then play"
                )
//                audioPlayPauseBtn.setImageResource(R.drawable.baseline_pause_black)
                EventBus.getDefault().post(PauseShort(true))
                isDurationOnPause = false
//                Log.d(
//                    "isDurationOnPause",
//                    " in play toggle audio player isDurationOnPause is $isDurationOnPause"
//                )
                exoPlayer.seekTo(progress.toLong())
                exoPlayer.play()
            }
        } else {
//            Log.d(TAG, "toggleAudioPlayer: currentCommentAudioPosition != position")

            // If a new item is clicked, stop the currently playing item (if any)
            if (exoPlayer.isPlaying == true) {
                Log.d("toggleAudioPlayer", "toggleAudioPlayer: in else player is playing")
                commentAudioPause(audioPlayPauseBtn, isReply)
            }

            if (isReply) {
                Log.d("IsReply", "is reply position $position")
//                adapter?.refreshMainComment(position)
            }
            // Start playing the new audio
//            if(!isReply) {
//                commentAudioStartPlaying(audioToPlayPath, audioPlayPauseBtn)
//            }
            commentAudioStartPlaying(audioToPlayPath, audioPlayPauseBtn, progress, position)
            currentCommentAudioPosition = position
            currentCommentAudioPath = audioToPlayPath
            Log.d(
                "toggleAudioPlayer",
                "toggleAudioPlayer: position updated $currentCommentAudioPosition"
            )
        }
    }

    @OptIn(UnstableApi::class)
    @SuppressLint("NotifyDataSetChanged")
    private fun commentAudioStartPlaying(
        vnAudio: String,
        audioPlayPauseBtn: ImageView,
        progress: Float, position: Int
    ) {
        val TAG = "commentAudioStartPlaying"
        EventBus.getDefault().post(PauseShort(true))
        isDurationOnPause = false
        if (isVnAudioToPlay) {
            startWaveRunnable()
        }
//        Log.d(
//            "isDurationOnPause",
//            " in comment audio start isDurationOnPause is $isDurationOnPause"
//        )
//        Log.d("TAG", "commentAudioStartPlaying: start playing and change to pause icon")

        audioPlayPauseBtn.setImageResource(R.drawable.baseline_pause_black)

        try {
            val file = File(vnAudio)
            if (file.exists()) {
                val fileUrl = Uri.fromFile(file)
//                val fileUrl = "file://$vnAudio"
                exoPlayer = ExoPlayer.Builder(this)
                    .build()
                Log.d("commentAudioStartPlaying", "commentAudioStartPlaying: Local file $fileUrl")
//                val dataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, "yourApplicationName"))

                val localFileUri =
                    Uri.parse(fileUrl.toString()) // Replace with the path to your local file
                val mediaItem = MediaItem.fromUri(localFileUri)
                exoPlayer!!.setMediaItem(mediaItem)
            } else {
                Log.d("commentAudioStartPlaying", "commentAudioStartPlaying: server file")
                val videoUri = Uri.parse(vnAudio)
                val mediaItem = MediaItem.fromUri(videoUri)
                httpDataSourceFactory = DefaultHttpDataSource.Factory()
                    .setAllowCrossProtocolRedirects(true)
                defaultDataSourceFactory = DefaultDataSourceFactory(
                    this, httpDataSourceFactory
                )
                cacheDataSourceFactory = CacheDataSource.Factory()
                    .setCache(simpleCache)
                    .setUpstreamDataSourceFactory(httpDataSourceFactory)
                    .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
                val mediaSourceFactory: MediaSource.Factory =
                    DefaultMediaSourceFactory(this)
                        .setDataSourceFactory(cacheDataSourceFactory)
                exoPlayer = ExoPlayer.Builder(this)
                    .setMediaSourceFactory(mediaSourceFactory)
                    .build()
                // Create media source
                val mediaSource = ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                    .createMediaSource(mediaItem)
                exoPlayer.setMediaSource(mediaSource)
            }
            exoPlayer.prepare()
            exoPlayer.seekTo(progress.toLong())
            exoPlayer.playWhenReady = true
//            exoPlayer.play()
            exoPlayer.repeatMode = Player.REPEAT_MODE_OFF
            exoPlayer.addListener(playbackStateListener)


            exoPlayer.addListener(object : Player.Listener {
                @Deprecated("Deprecated in Java")
                override fun onPlayerStateChanged(
                    playWhenReady: Boolean,
                    playbackState: Int
                ) {
                    if (playbackState == Player.STATE_READY && exoPlayer.duration != C.TIME_UNSET) {
//                                    shortsSeekBar.max = exoPlayer.duration.toInt()
//                                    shortsAdapter.setSeekBarMax(exoPlayer!!.currentPosition.toInt())
//                    shortSeekBar.max = exoPlayer.duration.toInt()
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    error.printStackTrace()
                    Toast.makeText(
                        this@PostDetailsActivity2,
                        "Can't play this audio",
                        Toast.LENGTH_SHORT
                    ).show()
                }
//
//                @Deprecated("Deprecated in Java")
//                override fun onPositionDiscontinuity(reason: Int) {
//                    updateSeekBar()
//                }
            })
            shortPlayer.addListener(object : Player.Listener {
                @Deprecated("Deprecated in Java")
                override fun onPlayerStateChanged(
                    playWhenReady: Boolean,
                    playbackState: Int
                ) {
                    if (playbackState == Player.STATE_READY && shortPlayer.duration != C.TIME_UNSET) {
//                                    shortsSeekBar.max = exoPlayer.duration.toInt()
//                                    shortsAdapter.setSeekBarMax(exoPlayer!!.currentPosition.toInt())
                        shortSeekBar.max = exoPlayer.duration.toInt()
                    }
                }

//                override fun onPlayerError(error: PlaybackException) {
//                    super.onPlayerError(error)
//                    error.printStackTrace()
//                    Toast.makeText(
//                        this@PostDetailsActivity2,
//                        "Can't play this audio",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }

                @Deprecated("Deprecated in Java")
                override fun onPositionDiscontinuity(reason: Int) {
                    updateSeekBar()
                }
            })

            if (isReplyVnPlaying) {
//                Log.d("isReplyVnPlaying", "isReplyVnPlaying $isReplyVnPlaying")
                val handler = Handler()

                handler.postDelayed({
                    adapter?.refreshMainComment(position)
                }, 200)
            } else {
//                Log.d("isReplyVnPlaying", "isReplyVnPlaying $isReplyVnPlaying")
            }
//            if (isVnAudioToPlay) {
//
//            }

        } catch (e: Exception) {
            Log.d("commentAudioStartPlaying", "commentAudioStartPlaying: error: ${e.message}")
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

        audioPlayPauseBtn.setImageResource(R.drawable.play_svgrepo_com)
        adapter!!.updatePlaybackButton(currentCommentAudioPosition, isReply, audioPlayPauseBtn)
//        val replyAdapter = ReplyCommentAdapter()
//        ReplyCommentAdapter.update
//        player?.pause()
        exoPlayer.pause()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun startRecording() {
        if (!permissionGranted) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
            return
        }
        try {

            if (player?.isPlaying == true) {
                stopPlaying()
            }
            binding.playerTimerTv.visibility = View.GONE
            outputFile = getOutputFilePath("rec")
            outputVnFile = getOutputFilePath("mix")
            wasPaused = false
//            firstTimeSendVn = false
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setOutputFile(outputFile)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                prepare()
                start()
            }
            isRecording = true
            isPaused = false
            isVnResuming = false
            binding.recordVN.setImageResource(R.drawable.baseline_pause_white_24)
            binding.sendVN.setBackgroundResource(R.drawable.ic_ripple)
            binding.deleteVN.setBackgroundResource(R.drawable.ic_ripple)
            timer.start()

            binding.deleteVN.isClickable = true
            binding.sendVN.isClickable = true
//            mediaRecorder.
            recordedAudioFiles.add(outputFile)

//            mediaRecorder.logSessionId

            Log.d("VNFile", outputFile)
            // Add any UI changes or notifications indicating recording has started
        } catch (e: Exception) {
            Log.d("VNFile", "Failed to record audio properly")
            e.printStackTrace()
            // Handle exceptions as needed
        }
    }

    ///ended here
    val count = 0

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun pausePlayEvent(event: PausePlayEvent) {
        Log.d("pausePlayEvent", "pausePlayEvent ${count + 1}")
        if (shortPlayer.isPlaying == true) {
            pauseVideo()
        } else {
            playVideo()
        }
    }

    private fun playVideo() {
        shortPlayer.playWhenReady = true
        isPlaying = true
    }

    private fun pauseVideo() {
        shortPlayer.playWhenReady = false
        isPlaying = false
    }

    private var isPlaying = false
    private fun togglesPausePlay() {
        Log.d("Pause", "togglePausePlay")
        if (isPlaying) {
            pauseVideo()
        } else {
            playVideo()
        }
    }

    ///ended here
    @RequiresApi(Build.VERSION_CODES.O)
    private fun selectGifFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/gif"
        }
        selectGifLauncher.launch(intent)
    }

    override fun onAddGif() {
        Log.d("onAddGif", "onAddGif: Gif Button Clicked")
//        showInputBoxForGifSelection()
//        selectGifFile()
// Create an intent to navigate to ActivityB
        // Create an intent to navigate to ActivityB
        val intent = Intent(this, GifActivity::class.java)
        startActivityForResult(intent, GIF_CODE)
//        startActivity(intent)

    }

    private fun getFileName(uri: Uri): String? {
        var fileName: String? = null
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            fileName = cursor.getString(nameIndex)
        }
        return fileName
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showInputBoxForGifSelection() {
        // Create and show a dialog box for input
        val inputDialog = AlertDialog.Builder(this)
        val inputEditText = EditText(this)
        inputDialog.setTitle("Enter Gif URL")
        inputDialog.setView(inputEditText)
        inputDialog.setPositiveButton("OK") { dialog, _ ->
            val gifUrl = inputEditText.text.toString()
            if (gifUrl.isNotEmpty()) {
                // If URL is not empty, proceed with file selection
                gifUrlType = gifUrl
                selectGifFile()
            } else {
                // Handle case when URL is empty
                Toast.makeText(this, "Please enter a valid GIF URL", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        inputDialog.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        inputDialog.show()
    }

    override fun onTimerTick(duration: String) {
        binding.timerTv.text = duration
        if (mediaRecorder == null) {
            Log.d("mediaRecording", "mediaRecording is null")
        } else {
            var amplitude = mediaRecorder!!.maxAmplitude.toFloat()
            amplitude = if (amplitude > 0) amplitude else 130f
            binding.waveForm.addAmplitude(amplitude)
        }
    }

//    override fun onNotificationLongClick(count: Int) {
//        TODO("Not yet implemented")
//    }

}








