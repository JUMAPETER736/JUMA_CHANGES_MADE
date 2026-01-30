package com.uyscuti.sharedmodule

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.InsetDrawable
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.ConnectivityManager
import android.net.Uri
import android.net.http.HttpException
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresExtension
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
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
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.uyscuti.sharedmodule.adapter.CommentsRecyclerViewAdapter
import com.uyscuti.sharedmodule.adapter.OnCommentsClickListener
import com.uyscuti.sharedmodule.adapter.OnViewRepliesClickListener
import com.uyscuti.sharedmodule.adapter.ProfileTabsAdapter
import com.uyscuti.sharedmodule.adapter.notifications.AdPaginatedAdapter
import com.uyscuti.sharedmodule.calls.viewmodel.CallViewModel
import com.uyscuti.sharedmodule.data.model.Dialog
import com.uyscuti.sharedmodule.data.model.Message
import com.uyscuti.sharedmodule.data.model.shortsmodels.CommentReplyResults
import com.uyscuti.sharedmodule.data.model.shortsmodels.OtherUsersProfile
import com.uyscuti.sharedmodule.databinding.OtherUserProfileRedesignBinding
import com.uyscuti.sharedmodule.eventbus.FromOtherUsersFeedCommentClick
import com.uyscuti.sharedmodule.eventbus.HideToolBar
import com.uyscuti.sharedmodule.eventbus.InformFeedFragment
import com.uyscuti.sharedmodule.eventbus.InformOtherUsersFeedProfileFragment
import com.uyscuti.sharedmodule.eventbus.InformShortsFragment2
import com.uyscuti.sharedmodule.interfaces.feedinterfaces.OnShortThumbnailClickListener
import com.uyscuti.sharedmodule.media.ViewImagesActivity
import com.uyscuti.sharedmodule.model.AudioPlayerHandler
import com.uyscuti.sharedmodule.model.CommentAudioPlayerHandler
import com.uyscuti.sharedmodule.model.FeedAdapterNotifyDatasetChanged
import com.uyscuti.sharedmodule.model.FollowListItemViewModel
import com.uyscuti.sharedmodule.model.InformAdapter
import com.uyscuti.sharedmodule.model.LikeCommentReply
import com.uyscuti.sharedmodule.model.PauseShort
import com.uyscuti.sharedmodule.model.ShortAdapterNotifyDatasetChanged
import com.uyscuti.sharedmodule.model.ShortsViewModel
import com.uyscuti.sharedmodule.model.ToggleReplyToTextView
import com.uyscuti.sharedmodule.model.User
import com.uyscuti.sharedmodule.presentation.DialogViewModel
import com.uyscuti.sharedmodule.presentation.GetOtherUsersProfileViewModel
import com.uyscuti.sharedmodule.service.VideoPreLoadingService
import com.uyscuti.sharedmodule.shorts.UniqueIdGenerator
import com.uyscuti.sharedmodule.shorts.getFileSize
import com.uyscuti.sharedmodule.testui.TrialFragment
import com.uyscuti.sharedmodule.ui.GifActivity
import com.uyscuti.sharedmodule.uploads.CameraActivity
import com.uyscuti.sharedmodule.uploads.VideosActivity
import com.uyscuti.sharedmodule.utils.AndroidUtil.showToast
import com.uyscuti.sharedmodule.utils.AudioDurationHelper
import com.uyscuti.sharedmodule.utils.AudioDurationHelper.getFormattedDuration
import com.uyscuti.sharedmodule.utils.AudioDurationHelper.reverseFormattedDuration
import com.uyscuti.sharedmodule.utils.COMMENT_VIDEO_CODE
import com.uyscuti.sharedmodule.utils.Constants
import com.uyscuti.sharedmodule.utils.GIF_CODE
import com.uyscuti.sharedmodule.utils.PathUtil
import com.uyscuti.sharedmodule.utils.R_CODE
import com.uyscuti.sharedmodule.utils.Timer
import com.uyscuti.sharedmodule.utils.TrimVideoUtils
import com.uyscuti.sharedmodule.utils.WaveFormExtractor
import com.uyscuti.sharedmodule.utils.audio_compressor.FFMPEG_AudioCompressor
import com.uyscuti.sharedmodule.utils.audiomixer.AudioMixer
import com.uyscuti.sharedmodule.utils.audiomixer.input.GeneralAudioInput
import com.uyscuti.sharedmodule.utils.audiomixer.ui.AudioActivity
import com.uyscuti.sharedmodule.utils.createMultipartBody
import com.uyscuti.sharedmodule.utils.deleteFiled
import com.uyscuti.sharedmodule.utils.deleteFiles
import com.uyscuti.sharedmodule.utils.extractThumbnailFromVideo
import com.uyscuti.sharedmodule.utils.fileType
import com.uyscuti.sharedmodule.utils.formatFileSize
import com.uyscuti.sharedmodule.utils.generateMongoDBTimestamp
import com.uyscuti.sharedmodule.utils.generateRandomId
import com.uyscuti.sharedmodule.utils.getFileNameFromLocalPath
import com.uyscuti.sharedmodule.utils.getOutputFilePath
import com.uyscuti.sharedmodule.utils.isFileExists
import com.uyscuti.sharedmodule.utils.isFileSizeGreaterThan2MB
import com.uyscuti.sharedmodule.utils.isInternetAvailable
import com.uyscuti.sharedmodule.utils.removeDuplicateFollowers
import com.uyscuti.sharedmodule.utils.waveformseekbar.SeekBarOnProgressChanged
import com.uyscuti.sharedmodule.utils.waveformseekbar.WaveformSeekBar
import com.uyscuti.sharedmodule.viewmodels.FeedShortsViewModel
import com.uyscuti.sharedmodule.viewmodels.FollowUnfollowViewModel
import com.uyscuti.sharedmodule.viewmodels.FollowViewModel
import com.uyscuti.sharedmodule.viewmodels.GetShortsByUsernameViewModel
import com.uyscuti.sharedmodule.viewmodels.comments.CommentsViewModel
import com.uyscuti.sharedmodule.viewmodels.comments.RoomCommentFilesViewModel
import com.uyscuti.sharedmodule.viewmodels.comments.RoomCommentReplyViewModel
import com.uyscuti.sharedmodule.viewmodels.comments.RoomCommentsViewModel
import com.uyscuti.sharedmodule.viewmodels.comments.ShortCommentReplyViewModel
import com.uyscuti.sharedmodule.viewmodels.comments.ShortCommentsViewModel
import com.uyscuti.sharedmodule.viewmodels.feed.FeedLiveDataViewModel
import com.uyscuti.sharedmodule.viewmodels.feed.GetFeedViewModel
import com.uyscuti.sharedmodule.viewmodels.otherusersprofile.OtherUsersProfileViewModel
import com.uyscuti.social.call.models.DataModel
import com.uyscuti.social.call.models.DataModelType
import com.uyscuti.social.call.repository.MainRepository
import com.uyscuti.social.call.ui.CallActivity
import com.uyscuti.social.chatsuit.messages.CommentsInput
import com.uyscuti.social.compressor.CompressionListener
import com.uyscuti.social.compressor.VideoCompressor
import com.uyscuti.social.compressor.VideoQuality
import com.uyscuti.social.compressor.config.Configuration
import com.uyscuti.social.compressor.config.SaveLocation
import com.uyscuti.social.compressor.config.SharedStorageConfiguration
import com.uyscuti.social.core.common.data.room.entity.CallLogEntity
import com.uyscuti.social.core.common.data.room.entity.CommentsFilesEntity
import com.uyscuti.social.core.common.data.room.entity.DialogEntity
import com.uyscuti.social.core.common.data.room.entity.FollowUnFollowEntity
import com.uyscuti.social.core.common.data.room.entity.MessageEntity
import com.uyscuti.social.core.common.data.room.entity.ShortCommentEntity
import com.uyscuti.social.core.common.data.room.entity.ShortCommentReply
import com.uyscuti.social.core.common.data.room.entity.ShortsEntity
import com.uyscuti.social.core.common.data.room.entity.ShortsEntityFollowList
import com.uyscuti.social.core.common.data.room.entity.UserEntity
import com.uyscuti.social.core.common.data.room.entity.UserShortsEntity
import com.uyscuti.social.medialoader.MediaLoader
import com.uyscuti.social.network.api.response.comment.allcomments.Account
import com.uyscuti.social.network.api.response.comment.allcomments.Author
import com.uyscuti.social.network.api.response.comment.allcomments.Avatar
import com.uyscuti.social.network.api.response.comment.allcomments.CommentFiles
import com.uyscuti.social.network.api.response.commentreply.allreplies.AllCommentReplies
import com.uyscuti.social.network.api.response.commentreply.allreplies.Comment
import com.uyscuti.social.network.api.response.posts.Post
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage
import com.vanniktech.emoji.EmojiPopup
import dagger.hilt.android.AndroidEntryPoint
import id.zelory.compressor.Compressor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.Collections
import java.util.Date
import javax.inject.Inject
import retrofit2.Response
import kotlin.properties.Delegates
import kotlin.random.Random

private const val TAG = "OtherUserProfile"
private const val PREFS_NAME = "LocalSettings"

@UnstableApi
@AndroidEntryPoint
class OtherUserProfile : AppCompatActivity(), OnViewRepliesClickListener,

    CommentsInput.InputListener, CommentsInput.EmojiListener, CommentsInput.VoiceListener,
    CommentsInput.GifListener,
    CommentsInput.AttachmentsListener, OnCommentsClickListener,
    Timer.OnTimeTickListener, OnShortThumbnailClickListener {

    private lateinit var binding: OtherUserProfileRedesignBinding
    private var user: com.uyscuti.sharedmodule.data.model.User? = null
    private lateinit var avatar: String
    private lateinit var dialogId: String


    private lateinit var username: String

    private val dialogViewModel: DialogViewModel by viewModels()

    private lateinit var callViewModel: CallViewModel

    @Inject
    lateinit var mainRepository: MainRepository

    @Inject
    lateinit var localStorage: LocalStorage

    @Inject
    lateinit var retrofitInterface: RetrofitInstance

    private val fromShortsTag = "FromShortsTag"


    private var fromShortsUserAccount: OtherUsersProfile? = null


    private val viewModel: GetOtherUsersProfileViewModel by viewModels()
    private val shortsViewModel: GetShortsByUsernameViewModel by viewModels()
    private val followUnFollowViewModel: FollowUnfollowViewModel by viewModels()
    private val followViewModel: FollowViewModel by viewModels()
    private val followShortsViewModel: FollowListItemViewModel by viewModels()
    private val feedShortsSharedViewModel: FeedShortsViewModel by viewModels()
    private val feedLiveDataViewModel: FeedLiveDataViewModel by viewModels()
    private val otherUsersProfileViewModel: OtherUsersProfileViewModel by viewModels()

    private var isFollowed = false
    private lateinit var followListItem: List<ShortsEntityFollowList>


    private lateinit var commentsViewModel: ShortCommentsViewModel
    private lateinit var commentViewModel: CommentsViewModel
    private lateinit var shortsCommentViewModel: RoomCommentsViewModel
    private lateinit var shortsCommentsViewModel: ShortsViewModel
    private lateinit var feedViewModel: GetFeedViewModel
    private lateinit var roomCommentReplyViewModel: RoomCommentReplyViewModel
    private lateinit var commentsReplyViewModel: ShortCommentReplyViewModel
    private lateinit var commentFilesViewModel: RoomCommentFilesViewModel



    private var vnList = ArrayList<String>()
    private var isReply = false

    private var isFeedComment = false

    var postId = ""
    private var data: com.uyscuti.sharedmodule.data.model.Comment? = null
    private var listOfReplies = mutableListOf<com.uyscuti.sharedmodule.data.model.Comment>()

    private var shortToComment: ShortsEntity? = null
    private var commentsAdapter: CommentsRecyclerViewAdapter? = null
    private lateinit var settings: SharedPreferences

    private var feedToComment: Post? = null
    private var myFeedToComment: Post? = null
    private var favoriteFeedToComment: Post? = null


    private lateinit var commentId: String
    private var position: Int = 0
    private lateinit var emojiPopup: EmojiPopup
    private lateinit var inputMethodManager: InputMethodManager
    private var emojiShowing = false

    private var updatePosition = -1
    private var updateReplyPosition = -1

    private val recordedAudioFiles = mutableListOf<String>()

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var audioPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var videoPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var docsPickerLauncher: ActivityResultLauncher<Intent>

    private val toCompressUris = mutableListOf<Uri>()

    private var currentCommentAudioPath = ""
    private var currentCommentAudioPosition = RecyclerView.NO_POSITION
    private var isReplyVnPlaying = false
    private var isVnAudioToPlay = false
    private val waveHandler = Handler()

    var isDurationOnPause = false
    var isOnRecordDurationOnPause = false
    var waveProgress = 0f
    var seekBarProgress = 0f

    private var exoPlayer: ExoPlayer? = null
    private lateinit var httpDataSourceFactory: HttpDataSource.Factory
    private lateinit var defaultDataSourceFactory: DefaultDataSourceFactory
    private lateinit var cacheDataSourceFactory: CacheDataSource.Factory

    private val simpleCache: SimpleCache = FlashApplication.Companion.cache
    private val playbackStateListener: Player.Listener = playbackStateListener()


    private lateinit var audioDurationTVCount: TextView
    private var wavePosition = -1
    private var seekPosition = -1

    private lateinit var audioFormWave: WaveformSeekBar
    private lateinit var audioSeekBar: SeekBar

    private var currentHandler: Handler? = null

    var maxDuration = 0L
    private var permissionGranted = false

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.READ_MEDIA_IMAGES
    )

    private val REQUEST_CODE = 2024

    private var player: MediaPlayer? = null
    private lateinit var outputFile: String
    private var outputVnFile: String = ""

    var wasPaused = false
    var sending = false

    private var mediaRecorder: MediaRecorder? = null

    private var isRecording = false
    private var isPaused = false
    private var isAudioVNPlaying = false
    private var isAudioVNPaused = false

    private var isVnResuming = false

    private lateinit var timer: Timer

    var vnRecordAudioPlaying = false
    var vnRecordProgress = 0


    private lateinit var amplitudes: ArrayList<Float>
    private var amps = 0

    private var mixingCompleted = false


    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = OtherUserProfileRedesignBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        callViewModel = ViewModelProvider(this)[CallViewModel::class.java]
        commentsViewModel = ViewModelProvider(this)[ShortCommentsViewModel::class.java]
        commentViewModel = ViewModelProvider(this)[CommentsViewModel::class.java]
        shortsCommentViewModel = ViewModelProvider(this)[RoomCommentsViewModel::class.java]
        roomCommentReplyViewModel = ViewModelProvider(this)[RoomCommentReplyViewModel::class.java]
        shortsCommentsViewModel = ViewModelProvider(this)[ShortsViewModel::class.java]
        feedViewModel = ViewModelProvider(this)[GetFeedViewModel::class.java]
        commentsReplyViewModel = ViewModelProvider(this)[ShortCommentReplyViewModel::class.java]
        commentFilesViewModel = ViewModelProvider(this)[RoomCommentFilesViewModel::class.java]


        user = intent.getParcelableExtra("User_Extra")
        avatar = intent.getStringExtra("Avatar_Extra").toString()
        dialogId = intent.getStringExtra("Dialog_Extra").toString()

        fromShortsUserAccount = intent.getSerializableExtra("user_profile") as OtherUsersProfile
        Log.d(fromShortsTag, "onCreate: $fromShortsUserAccount")
        settings = getSharedPreferences(PREFS_NAME, 0)
        username = localStorage.getUsername()

        Log.d(TAG, "onCreate: $user")
        Log.d(TAG, "onCreate: $avatar")
        Log.d(TAG, "onCreate: $dialogId")
        Log.d(TAG, "onCreate: $username")

        val connectivityManager =
            getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkInfo = connectivityManager.activeNetworkInfo
        val isConnected = networkInfo != null && networkInfo.isConnected


        supportActionBar?.title = ""

        val user = User(
            _id = fromShortsUserAccount!!.userId,
            avatar = fromShortsUserAccount!!.profilePic,
            email = "unknown@example.com",
            isEmailVerified = false,
            role = "user",
            username = fromShortsUserAccount!!.username,
            lastseen = Date()
        )


        val tabsAdapter = ProfileTabsAdapter(this, supportFragmentManager, user)
        tabsAdapter.setUsername(fromShortsUserAccount?.username)

        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = tabsAdapter
        viewPager.offscreenPageLimit = 3

        tabsAdapter.setListener(this)
        val size =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40f, resources.displayMetrics)
                .toInt()


        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val navigationIcon = ContextCompat.getDrawable(this, R.drawable.baseline_arrow_back_ios_24)

        navigationIcon?.let {
            it.setBounds(0, 0, it.intrinsicWidth, it.intrinsicHeight)

            val wrappedDrawable = DrawableCompat.wrap(it)
            DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(this, R.color.black))
            val drawableMargin = InsetDrawable(wrappedDrawable, 0, 0, 0, 0)
            binding.toolbar.navigationContentDescription = "Navigate up"
            binding.toolbar.navigationIcon = drawableMargin
        }


        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val tabs: TabLayout = binding.tabLayout
        tabs.setupWithViewPager(viewPager)


        for (i in 0 until tabsAdapter.count) {
            tabs.getTabAt(i)?.icon = tabsAdapter.getIcon(i)
        }

        initUser()

        binding.userAvatar.setOnClickListener {
            user.username.let { it1 -> viewImage(avatar, it1) } ?: run {
                // Use another object (let's say anotherUser) as a fallback
                fromShortsUserAccount?.profilePic?.let { it1 -> viewImage(avatar, it1) }
            }
        }

        binding.actionMessage.setOnClickListener {
            messageUser(dialogId)
        }
        otherUsersProfileViewModel.getOpenShortsPlayerFragment.observe(this) { openShortsPlayer ->
            if (openShortsPlayer) {

                Log.d("openShortsPlayer", "openShortsPlayer: step 6")
            } else {
                Log.d("openShortsPlayer", "openShortsPlayer openShortsPlayer: $openShortsPlayer")
            }
        }

        // Observe the follow status and update UI accordingly
        followViewModel.getFollowStatus(fromShortsUserAccount!!.userId)
            .observe(this) { followEntity ->
                followEntity?.let {
                    if (it.isFollowing) {
                        // User is currently following, update UI accordingly

                        if (!isConnected) {
                            Log.d(TAG, "onCreate: no internet connection")
                        } else {
                            Log.d(TAG, "onCreate: internet connected")
                            followUnFollowViewModel.followUnFollow(fromShortsUserAccount!!.userId)
                            viewModel.viewModelScope.launch {
                                delay(500)
                                viewModel.getOtherUsersProfile(fromShortsUserAccount!!.username)
                            }
                            followUnFollowViewModel.viewModelScope.launch {
                                val isDeleted =
                                    followViewModel.deleteFollowById(fromShortsUserAccount!!.userId)
                                if (isDeleted) {
                                    // Deletion was successful, update UI or perform other actions
                                    Log.d(TAG, "Follow deleted successfully.")
                                } else {
                                    // Deletion was not successful, handle accordingly
                                    Log.d(TAG, "Failed to delete follow.")
                                }
                            }
                        }

                    } else {
                        // User is not following, update UI accordingly

                        if (!isConnected) {
                            Log.d(TAG, "onCreate: no internet connection")
                        } else {

                            Log.d(TAG, "onCreate: internet connected")
                            followUnFollowViewModel.followUnFollow(fromShortsUserAccount!!.userId)

                            viewModel.viewModelScope.launch {
                                delay(500)
                                viewModel.getOtherUsersProfile(fromShortsUserAccount!!.username)
                            }

                            followUnFollowViewModel.viewModelScope.launch {
                                val isDeleted =
                                    followViewModel.deleteFollowById(fromShortsUserAccount!!.userId)
                                if (isDeleted) {
                                    // Deletion was successful, update UI or perform other actions
                                    Log.d(TAG, "Follow deleted successfully.")
                                } else {
                                    // Deletion was not successful, handle accordingly
                                    Log.d(TAG, "Failed to delete follow.")
                                }
                            }
                        }

                    }
                }
            }
        feedShortsSharedViewModel.data.observe(this) { newData ->
            Log.d(
                "feesShortsSharedViewModel",
                "onCreateView: data from all shorts fragment $newData"
            )

            EventBus.getDefault().post(InformShortsFragment2(newData.userId, newData.isFollowing))

            val followersCountText = binding.followersCount.text.toString()

            val followersCount: Int = try {
                followersCountText.toInt()

            } catch (e: NumberFormatException) {
                // Handle the case where the text is not a valid integer
                0 // Or any default value or error handling
            }

            if (newData.isFollowing) {



                binding.followersCount.text = (followersCount + 1).toString()
            } else {

                binding.followersCount.text = (followersCount - 1).toString()
            }
            YoYo.with(Techniques.Tada)
                .duration(700)
                .repeat(1)
                .pivotX(16.6F)
                .playOn(binding.followIcon)
        }
        binding.followIcon.setOnClickListener {
            // Toggle the follow status
            val isFollowing = binding.followIcon.text != "Following"


            // Update the Room database with the new follow status
            val newFollowEntity = FollowUnFollowEntity(fromShortsUserAccount!!.userId, isFollowing)


            EventBus.getDefault()
                .post(InformFeedFragment(fromShortsUserAccount!!.userId, isFollowing))
            EventBus.getDefault().post(
                InformOtherUsersFeedProfileFragment(
                    fromShortsUserAccount!!.userId,
                    isFollowing
                )
            )
            followViewModel.insertOrUpdateFollow(newFollowEntity)

            followListItem =
                listOf(ShortsEntityFollowList(fromShortsUserAccount!!.userId, isFollowing))

            lifecycleScope.launch(Dispatchers.IO) {
                val uniqueFollowList = removeDuplicateFollowers(followListItem)
                Log.d("AllShorts3", "getAllShort3: Inserted uniqueFollowList $uniqueFollowList")
                followShortsViewModel.insertFollowListItems(uniqueFollowList)
                EventBus.getDefault().post(InformAdapter())

            }

            YoYo.with(Techniques.Tada)
                .duration(700)
                .repeat(1)
                .pivotX(16.6F)
                .playOn(binding.followIcon)
        }

        binding.followIcon.setOnClickListener {
            // Toggle the follow status
            val isFollowing = binding.followIcon.text != "Following"

            // Update the Room database with the new follow status
            val newFollowEntity = FollowUnFollowEntity(fromShortsUserAccount!!.userId, isFollowing)
            followViewModel.insertOrUpdateFollow(newFollowEntity)

            YoYo.with(Techniques.Tada)
                .duration(700)
                .repeat(1)
                .pivotX(16.6F)
                .playOn(binding.followIcon)
        }

        initializeCommentsBottomSheet()


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
                                    Compressor.compress(this@OtherUserProfile, file)
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

                    } else {
                        Log.d("PhotoPicker", "No media selected")
                    }
                }
            }

        audioPickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    // Handle image selection result here
                    val data = result.data
                    // Process the selected image data
                    val audioPath = data?.getStringExtra("audio_url")

                    val uriString = data?.getStringExtra("aUri")
                    val aUri = Uri.parse(uriString)

                    if (audioPath != null) {
                        Log.d("AudioPicker", "File path: $audioPath")
                        Log.d("AudioPicker", "File path: $isReply")
                        val durationString = getFormattedDuration(audioPath)
                        val fileName = getFileNameFromLocalPath(audioPath)
                        val reverseDurationString = reverseFormattedDuration(durationString)


                        Log.d("AudioPicker", "File name: $fileName")
                        Log.d("AudioPicker", "durationString: $durationString")

                        val file = File(audioPath)


                        var fileSizeInBytes by Delegates.notNull<Long>()
                        var fileSizeInKB by Delegates.notNull<Long>()
                        var fileSizeInMB by Delegates.notNull<Long>()


                        fileSizeInBytes = file.length()
                        fileSizeInKB = fileSizeInBytes / 1024
                        fileSizeInMB = fileSizeInKB / 1024

                        if (fileSizeInMB > 2) {
                            Log.d("AudioPicker", "onCreate: file needs compression")
                            val outputFileName =
                                "compressed_audio_${System.currentTimeMillis()}.mp3" // Example output file name
                            val outputFilePath = File(cacheDir, outputFileName)


                            lifecycleScope.launch(Dispatchers.IO) {

                                Log.d(
                                    "AudioPicker",
                                    "onCreate: file needs compression is reply $isReply"
                                )
                                withContext(Dispatchers.Main) {
                                    if (!isReply) {
                                        Log.d("AudioPicker", "onCreate:not reply use place holder")
                                        uploadVnComment(
                                            vnToUpload = audioPath,
                                            fileName = fileName,
                                            durationString = durationString,
                                            fileType = "mAudio",
                                            update = false,
                                            placeholder = true
                                        )
                                    } else {
                                        Log.d("AudioPicker", "onCreate: reply use placeholder")
                                        uploadReplyVnComment(
                                            audioPath,
                                            fileName,
                                            durationString,
                                            fileType = "mAudio",
                                            update = false,
                                            placeholder = true
                                        )
                                    }
                                }

                                val compressor = FFMPEG_AudioCompressor()
                                val isCompressionSuccessful =
                                    compressor.compress(audioPath, outputFilePath.absolutePath)

                                if (isCompressionSuccessful) {
                                    Log.d("AudioPicker", "AudioPicker: Compression successful ")

                                    fileSizeInBytes = outputFilePath.length()
                                    fileSizeInKB = fileSizeInBytes / 1024
                                    fileSizeInMB = fileSizeInKB / 1024
                                    Log.d(
                                        "AudioPicker",
                                        "File size: $fileSizeInKB KB,  $fileSizeInMB MB"
                                    )

                                    val fileSizeInGB =
                                        fileSizeInMB / 1024 // Conversion from MB to GB

                                    withContext(Dispatchers.Main) {
                                        if (!isReply) {
                                            uploadVnComment(
                                                vnToUpload = outputFilePath.absolutePath,
                                                fileName = fileName,
                                                durationString = durationString,
                                                fileType = "mAudio",
                                                update = true,
                                                placeholder = false
                                            )
                                        } else {
                                            uploadReplyVnComment(
                                                outputFilePath.absolutePath,
                                                fileName,
                                                durationString,
                                                fileType = "mAudio",
                                                update = true,
                                                placeholder = false
                                            )
                                        }
                                    }

                                } else {
                                    Log.d("AudioPicker", "AudioPicker: Compression not successful")
                                }
                            }
                        } else {

                            if (isReply) {
                                uploadReplyVnComment(
                                    audioPath,
                                    fileName,
                                    durationString,
                                    "mAudio",
                                    false
                                )
                            } else {
                                uploadVnComment(
                                    audioPath,
                                    fileName,
                                    durationString,
                                    "mAudio",
                                    false
                                )
                            }
                        }



                    } else {
                        Log.d("AudioPicker", "File path: $audioPath")
                    }

                }
            }
        // Register to listen to events
        EventBus.getDefault().register(this)

        addComment()
        addCommentReply()
        addGifComment()
        addImageComment()
        addVideoComment()
        addDocumentComment()
        addCommentVN()
        addCommentFileReply()


        permissionGranted = ActivityCompat.checkSelfPermission(
            this, permissions[0]
        ) == PackageManager.PERMISSION_GRANTED

        timer = Timer(this)

        binding.recordVN.setOnClickListener {
            when {
                isPaused -> resumeRecording()
                isRecording -> pauseRecording()
                else -> Log.d("recordVN", "onCreate: else in vn record btn on click")
            }
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

                    delay(500)
                    stopRecording()
                }

            }
        }

    }


    private fun viewImage(url: String, name: String) {
        val intent = Intent(this, ViewImagesActivity::class.java)
        intent.putExtra("imageUrl", url)
        intent.putExtra("owner", name)
        startActivity(intent)
    }


    @SuppressLint("SetTextI18n")
    private fun initUser() {
        if (user != null) {
            Glide.with(this)
                .asBitmap()
                .load(avatar)
                .into(object : SimpleTarget<Bitmap>() {

                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        val drawable = RoundedBitmapDrawableFactory.create(resources, resource)

                        drawable.isCircular = true

                        val marginDrawable = InsetDrawable(drawable, 0, 0, 0, 0)
                        binding.userAvatar.setImageDrawable(marginDrawable)
                    }
                })

            binding.groupNameET.text = user?.name

        } else if (fromShortsUserAccount != null) {
            Log.d(fromShortsTag, "initUser: not empty")
            Glide.with(this)
                .asBitmap()
                .load(fromShortsUserAccount!!.profilePic)
                .into(object : SimpleTarget<Bitmap>() {

                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        val drawable = RoundedBitmapDrawableFactory.create(resources, resource)

                        drawable.isCircular = true

                        val marginDrawable = InsetDrawable(drawable, 0, 0, 0, 0)
                        binding.userAvatar.setImageDrawable(marginDrawable)
                    }
                })

            binding.groupNameET.text = fromShortsUserAccount!!.name
            viewModel.getOtherUsersProfile(fromShortsUserAccount!!.username)
            shortsViewModel.getOtherUsersProfileShorts(fromShortsUserAccount!!.username)

            viewModel.getUserProfileShortsObserver().observe(
                this
            ) { userProfileData ->

                binding.followersCount.text = "${userProfileData!!.followersCount}"
                binding.followingCount.text = "${userProfileData.followingCount}"
                binding.userBioText.text = userProfileData.bio
                if (userProfileData.isFollowing) {

                } else {

                }

                Log.d(TAG, "initUser followers count: ${userProfileData.followersCount}")
            }


            viewModel.getOnErrorFeedBackObserver().observe(this) { onErrorFeedback ->
                MotionToast.createToast(
                    this,
                    "Failed To Retrieve Data☹️",
                    onErrorFeedback,
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(this, R.font.helvetica_regular)
                )
            }
            shortsViewModel.getUserProfileShortsObserver().observe(
                this
            ) { userShortsData ->
                binding.postsCount.text = "${userShortsData.totalPosts}"
            }
            shortsViewModel.getOnErrorFeedBackObserver().observe(this) { errorFeedback ->
                MotionToast.createToast(
                    this,
                    "Failed To Retrieve Data☹️",
                    errorFeedback,
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(this, R.font.helvetica_regular)
                )
            }
        }
    }


    private fun showCallTypeDialog() {
        val callTypes = arrayOf("Video Call", "Voice Call")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Call Type")
        builder.setItems(callTypes) { dialog, which ->
            when (which) {
                0 -> startVideoCall()
                1 -> startVoiceCall()
            }
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Logout")
        builder.setMessage("Are you sure you want to logout?")
        builder.setPositiveButton("Video Call") { dialog, which ->
            // Handle logout here
            startVideoCall()
            dialog.dismiss()
        }
        builder.setNegativeButton("Voice Call") { dialog, which ->
            // Dismiss the dialog
            startVoiceCall()
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.one_dialog_menu, menu)

        return true
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Prepare the result data
        binding.otherUsersShortsPlayFragment.visibility = View.GONE
        val resultIntent = Intent()
        resultIntent.putExtra("key", "value")  // Replace with actual key-value pairs

        // Set the result
        setResult(RESULT_OK, resultIntent)

        // Hide the action bar
        supportActionBar?.hide()

        super.onBackPressed()

        // Finish the current activity
        finish()
        if (supportFragmentManager.backStackEntryCount > 0) {
            Log.d("onBackPressed", "onBackPressed: backStackEntryCount > 0 ")

        } else {


        }

    }



    private fun updateDataBeforeFinish() {
        // Update your data here and use setResult to send data back
        val updatedData = isFollowed
        val resultIntent = Intent()
        resultIntent.putExtra("updatedData", updatedData)
        setResult(RESULT_OK, resultIntent)
    }

    private fun initiateVideoCall() {
        // Code to start a video call
    }

    private fun initiateVoiceCall() {
        // Code to start a voice call
    }


    companion object {
        const val REQUEST_CODE_OTHER_USER_PROFILE = 2024
        fun open(context: Context, user: com.uyscuti.sharedmodule.data.model.User?, dialogPhone: String, dialogId: String) {
            val intent = Intent(context, OtherUserProfile::class.java)
            intent.putExtra("User_Extra", user)
            intent.putExtra("Avatar_Extra", dialogPhone)
            intent.putExtra("Dialog_Extra", dialogId)
            context.startActivity(intent)
        }

        fun openFromShorts(context: Context, otherUserProfile: OtherUsersProfile) {
            val intent = Intent(context, OtherUserProfile::class.java)
            intent.putExtra("user_profile", otherUserProfile)

            context.startActivity(intent)

        }
    }


    private fun fromDialogEntity(entity: DialogEntity): Dialog {
        val users = convertUserEntitiesToUsers(entity.users)

        val usersList: List<com.uyscuti.sharedmodule.data.model.User> = users as List<com.uyscuti.sharedmodule.data.model.User>

        val usersArrayList: ArrayList<com.uyscuti.sharedmodule.data.model.User> = ArrayList(usersList)

        val lastMessage = entity.lastMessage?.let { convertMessageEntityToMessage(it) }

        return Dialog(
            entity.id,
            entity.dialogName,
            entity.dialogPhoto,
            usersArrayList,
            lastMessage,
            entity.unreadCount
        )
    }

    private fun openMessages(dialog: Dialog) {
        val temporally = dialog.id == dialog.dialogName
        MessagesActivity.Companion.open(this, dialog.dialogName, dialog, temporally,"")
        resetUnreadCount(dialog)
    }

    private fun messageUser(dialogId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("CallerDialog", "CallerId $dialogId")

            val callerDialog = dialogViewModel.getDialog(dialogId)

            Log.d("CallerDialog", "$callerDialog")

            withContext(Dispatchers.Main) {
                val dialog = fromDialogEntity(callerDialog)
                openMessages(dialog)
            }
        }
    }


    private fun resetUnreadCount(dialog: Dialog) {
        CoroutineScope(Dispatchers.IO).launch {
            val dg = dialogViewModel.getDialog(dialog.id)
            dg.unreadCount = 0
            dialogViewModel.updateDialog(dg)
        }
    }

    private fun convertMessageEntityToMessage(messageEntity: MessageEntity): Message {
        // Convert the properties from ChatMessageEntity to Message
        val id = messageEntity.id
        val user =
            com.uyscuti.sharedmodule.data.model.User(
                messageEntity.userId,
                messageEntity.userName,
                messageEntity.user.avatar,
                messageEntity.user.online,
                messageEntity.user.lastSeen
            ) // You might need to fetch the user details
        val text = messageEntity.text
        val createdAt = Date(messageEntity.createdAt)

        val message = Message(id, user, text, createdAt)

        // Set additional properties like image and voice if needed
        if (messageEntity.imageUrl != null) {
            message.setImage(Message.Image(messageEntity.imageUrl!!))
        }

        if (messageEntity.videoUrl != null) {
            message.setVideo(Message.Video(messageEntity.videoUrl!!))
        }

        if (messageEntity.voiceUrl != null) {
            message.setVoice(Message.Voice(messageEntity.voiceUrl!!, messageEntity.voiceDuration))
        }

        return message
    }

    private fun convertUserEntitiesToUsers(userEntities: List<UserEntity>): List<com.uyscuti.sharedmodule.data.model.User> {
        return userEntities.map { userEntity ->
            com.uyscuti.sharedmodule.data.model.User(
                userEntity.id,
                userEntity.name,
                userEntity.avatar,
                userEntity.online,
                userEntity.lastSeen
            )
        }
    }

    private fun startVoiceCall() {
        mainRepository.sendConnectionRequest(
            DataModel(
                DataModelType.StartVoiceCall, username, user!!.name, null
            )
        ) {
            if (it) {
                startActivity(Intent(this, CallActivity::class.java).apply {
                    putExtra("target", user!!.name)
                    putExtra("isVideoCall", false)
                    putExtra("isCaller", true)
                    putExtra("avatar", user!!.avatar)
                })
            }
        }
        val newCallLog = CallLogEntity(
            id = Random.Default.nextLong(),
            callerName = user!!.name,
            System.currentTimeMillis(),
            callDuration = 0,
            "Outgoing",
            "Not Answered",
            avatar,
            dialogId,
            false,
            false
        )
        insertCallLog(newCallLog)
    }

    private fun startVideoCall() {
        mainRepository.sendConnectionRequest(
            DataModel(
                DataModelType.StartVideoCall, username, user!!.name, null
            )
        ) {
            if (it) {
                startActivity(Intent(this, CallActivity::class.java).apply {
                    putExtra("target", user!!.name)
                    putExtra("isVideoCall", true)
                    putExtra("isCaller", true)
                    putExtra("avatar", user!!.avatar)
                })
            }
        }
        val newCallLog = CallLogEntity(
            id = Random.Default.nextLong(),
            callerName = user!!.name,
            System.currentTimeMillis(),
            callDuration = 0,
            "Outgoing",
            "Not Answered",
            avatar,
            dialogId,
            true,
            false
        )
        insertCallLog(newCallLog)
    }


    private fun insertCallLog(callLog: CallLogEntity) {
        callViewModel.insertCallLog(callLog)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun feedFromOtherUsersFeedCommentClick(event: FromOtherUsersFeedCommentClick) {
        Log.d("feedUploadResponseEvent", "feedUploadResponseEvent: ")

        isFeedComment = true
        postId = event.data._id


        commentsAdapter = CommentsRecyclerViewAdapter(this, this@OtherUserProfile)

        val layoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = layoutManager
        commentsAdapter?.setRecyclerView(binding.recyclerView)
        binding.recyclerView.itemAnimator = null
        toggleMotionLayoutVisibility()

        commentsAdapter!!.setOnPaginationListener(object : AdPaginatedAdapter.OnPaginationListener {
            override fun onCurrentPage(page: Int) {

                Log.d(TAG, "currentPage: page number $page")
            }

            override fun onNextPage(page: Int) {
                lifecycleScope.launch(Dispatchers.Main) {

                    Log.d(TAG, "onNextPage: page number $page")
                    allFeedComments(page, event.data._id)
                }
            }

            override fun onFinish() {

            }
        })
        lifecycleScope.launch(Dispatchers.Main) {
            allFeedComments(commentsAdapter!!.startPage, event.data._id)

        }

        observeComments()
    }

    private fun observeComments() {
        val TAG = "observeComments"
        commentsViewModel.commentsLiveData.observe(this) { it ->
            Log.d(TAG, "observeComments comments size: ${it.size}")

            val commentsWithReplies = it.filter { it.replyCount > 0 }
            Log.d(TAG, "observeComments comments with replies size: ${commentsWithReplies.size}")

        }
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showShimmer() {
        binding.shimmerLayout.startShimmerAnimation()
        binding.shimmerLayout.visibility = View.VISIBLE
    }

    private fun hideShimmer() {
        binding.shimmerLayout.stopShimmerAnimation()
        binding.shimmerLayout.visibility = View.GONE
    }

    private fun startPreLoadingService() {
        Log.d("VNCache", "Preloading called")
        val preloadingServiceIntent =
            Intent(this, VideoPreLoadingService::class.java)
        preloadingServiceIntent.putStringArrayListExtra(Constants.VIDEO_LIST, vnList)
        startService(preloadingServiceIntent)
    }

    private fun updateUI(dataEmpty: Boolean) {
        if (dataEmpty) {
            binding.recyclerView.visibility = View.GONE
            binding.placeholderLayout.visibility = View.VISIBLE
        } else {
            binding.placeholderLayout.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }

    }

    @OptIn(DelicateCoroutinesApi::class)
    private suspend fun allFeedComments(page: Int, postId: String) {

        val tag = "bindingAdapter"
        GlobalScope.launch(Dispatchers.IO) {

            withContext(Dispatchers.Main) {
                if (page == 1) {
                    showShimmer()
                } else {
                    showProgressBar()
                }
            }
            try {


                if (commentsAdapter == null) {
                    Log.d("feedEventCommentClick", "allFeedComments: comment adapter still null")
                } else {
                    Log.d("feedEventCommentClick", "allFeedComments: comment adapter not null")

                    val commentsWithReplies = commentViewModel.fetchFeedComments(postId, page)
                    Log.d(
                        "feedEventCommentClick",
                        "allFeedComments: size ${commentsWithReplies.size}"
                    )


                    withContext(Dispatchers.Main) {
                        delay(500)
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
                        if (commentsAdapter != null) {

                            commentsAdapter!!.submitItems(commentsWithReplies)
                        } else {
                            Log.d(
                                "feedEventCommentClick",
                                "allFeedComments: when submitting adapter is null"
                            )
                        }


                        if (page == 1) {
                            if (commentsWithReplies.isEmpty()) {
                                updateUI(true)
                            } else {
                                updateUI(false)
                            }
                        } else {
                            updateUI(false)
                        }

                    }
                }

            } catch (e: Exception) {
                Log.e("feedEventCommentClick", "Exception: ${e.message}")
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


    @SuppressLint("SetTextI18n")
    @androidx.annotation.OptIn(UnstableApi::class)
    private fun toggleMotionLayoutVisibility() {
        val currentVisibility = binding.motionLayout.visibility

        if (currentVisibility == View.VISIBLE) {
            // If currently visible, make it gone
            binding.motionLayout.visibility = View.GONE
            binding.VNLayout.visibility = View.GONE

            binding.replyToLayout.visibility = View.GONE
            binding.input.inputEditText.setText("")
            commentsViewModel.resetLiveData()

            binding.mainContainer.isClickable = true

            binding.toolbar.visibility = View.VISIBLE
        } else {
            var currentState = binding.motionLayout.currentState

            // If currently gone, make it visible and set the transition to start
            binding.motionLayout.visibility = View.VISIBLE

            binding.motionLayout.transitionToStart()


            binding.toolbar.visibility = View.GONE

        }
    }

    private fun initializeCommentsBottomSheet() {

        val rootView = binding.motionLayout


        emojiPopup = EmojiPopup(rootView, binding.input.inputEditText)
        inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager


        val input = findViewById<CommentsInput>(R.id.input)
        input.setInputListener(this)
        input.setAttachmentsListener(this)
        input.setVoiceListener(this)
        input.setEmojiListener(this)
        input.setGifListener(this)


        binding.motionLayout.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {}

            override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {}

            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {}

            override fun onTransitionCompleted(p0: MotionLayout?, currentId: Int) {
                // Check if MotionLayout is in the certain state
                if (currentId == R.id.end) {
                    // Bring other views to the front

                    binding.motionLayout.visibility = View.GONE

                }
            }
        })

        binding.click.setOnClickListener {

            toggleMotionLayoutVisibility()

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPlaying()
        stopRecording()
        commentAudioStop()
        stopWaveRunnable()
        stopRecordWaveRunnable()
        MediaLoader.getInstance(this).destroy()
        exoPlayer?.removeListener(playbackStateListener)
        // Unregister to avoid memory leaks
        EventBus.getDefault().unregister(this)
    }

    private suspend fun allCommentRepliesOnce(
        page: Int, commentId: String
    ): CommentReplyResults {
        try {
            var hasNextPage: Boolean
            val pageNumber = page + 1
            val comments: MutableList<Comment> =
                mutableListOf()
            withContext(Dispatchers.IO) {
                // Handle UI-related tasks if needed
                lateinit var response: Response<AllCommentReplies>

                if (isFeedComment) {
                    response =
                        retrofitInterface.apiService.getFeedCommentReplies(
                            commentId,
                            page.toString()
                        )
                } else {
                    response =
                        retrofitInterface.apiService.getCommentReplies(commentId, page.toString())
                }


                val responseBody = response.body()


                responseBody?.data?.comments?.let { comments.addAll(it) }
                hasNextPage = responseBody?.data?.hasNextPage ?: false

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

                    Log.d(
                        TAG, "allShortComments: total comments for this post: ${comments.size}"
                    )
                }
                for (i in comments) {
                    Log.d(TAG, "All comments images ${i.images}")
                }

            }
            return CommentReplyResults(comments, hasNextPage, pageNumber)

        } catch (e: Exception) {
            Log.e("UserProfileShortsViewModel", "Exception: ${e.message}")
            lifecycleScope.launch {
                // Handle UI-related tasks if needed
                Toast.makeText(
                    this@OtherUserProfile, e.message, Toast.LENGTH_LONG
                ).show()
            }
            e.printStackTrace()
        }

        return CommentReplyResults(Collections.emptyList(), false, page)
    }



    override fun onSubmit(input: CharSequence?): Boolean {

        val localUpdateId = generateRandomId()
        if (!isReply) {

            val mongoDbTimeStamp = generateMongoDBTimestamp()

            val profilePic2 = settings.getString("profile_pic", "").toString()
            val avatar = Avatar("", "", url = profilePic2)
            val account =
                Account(
                    _id = "",
                    avatar = avatar,
                    "",
                    LocalStorage.Companion.getInstance(this).getUsername()
                )
            val author = Author(
                _id = "12", account = account, firstName = "", lastName = "",
                avatar = null
            )
            val comment = com.uyscuti.sharedmodule.data.model.Comment(
                __v = 1,
                _id = commentsAdapter!!.itemCount.toString(),
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
                ShortCommentEntity(
                    postId,
                    input.toString(),
                    localUpdateId = localUpdateId,
                    isFeedComment = isFeedComment
                )
            shortsCommentViewModel.insertComment(newCommentEntity)
            Log.d(TAG, "onSubmit: inserted comment $newCommentEntity")

            listOfReplies.add(comment)

            Log.d(TAG, "onSubmit: comment $comment")



            commentsAdapter!!.submitItem(comment, commentsAdapter!!.itemCount)
            updateUI(dataEmpty = false)
            if (!isFeedComment) {
                shortToComment = shortsCommentsViewModel.mutableShortsList.find { it._id == postId }
                Log.d(TAG, "onSubmit: count before ${shortToComment!!.comments}")

                if (shortToComment != null) {
                    shortToComment!!.comments += 1

                    // Update the count in the mutableShortsList
                    // Update the count in the mutableShortsList
                    shortsCommentsViewModel.mutableShortsList.forEach { short ->
                        if (short._id == postId) {
                            short.comments = shortToComment!!.comments
                        }
                    }
                    val newShortToComment =
                        shortsCommentsViewModel.mutableShortsList.find { it._id == postId }
                    Log.d(TAG, "onSubmit: count after ${newShortToComment!!.comments}")

                    EventBus.getDefault().post(ShortAdapterNotifyDatasetChanged())
                }
            } else {
                feedToComment = feedViewModel.getAllFeedData().find { it._id == postId }
                Log.d("feedToComment", "(1)onSubmit: total before feed count is $feedToComment")
                myFeedToComment = feedViewModel.getMyFeedData().find { it._id == postId }
                favoriteFeedToComment =
                    feedViewModel.getAllFavoriteFeedData().find { it._id == postId }!!
                Log.d(TAG, "onSubmit: total before feed count is ${feedToComment?.comments}")

                if (myFeedToComment != null) {


                    feedViewModel.getMyFeedData().forEach { feed ->
                        if (feed._id == postId) {
                            feed.comments = myFeedToComment!!.comments
                        }
                    }


                }
                if (favoriteFeedToComment != null) {



                    feedViewModel.getAllFavoriteFeedData().forEach { feed ->
                        if (feed._id == postId) {
                            feed.comments = favoriteFeedToComment!!.comments
                        }
                    }



                }
                if (feedToComment != null) {



                    feedViewModel.getAllFeedData().forEach { feed ->
                        if (feed._id == postId) {
                            feed.comments = feedToComment!!.comments
                        }
                    }
                    feedToComment = feedViewModel.getAllFeedData().find { it._id == postId }
                    Log.d(TAG, "onSubmit: total after feed count is ${feedToComment?.comments}")

                    EventBus.getDefault()
                        .post(FeedAdapterNotifyDatasetChanged(commentsAdapter!!.itemCount))
                }

                feedLiveDataViewModel.setBoolean(true)
            }

        } else {

            val profilePic2 = settings.getString("profile_pic", "").toString()
            val avatar = com.uyscuti.social.network.api.response.commentreply.allreplies.Avatar(
                "", "", url = profilePic2
            )
            val account = com.uyscuti.social.network.api.response.commentreply.allreplies.Account(
                _id = "", avatar = avatar, "", LocalStorage.Companion.getInstance(this).getUsername()
            )


            val commentReplyAuthor = com.uyscuti.social.network.api.response.commentreply.allreplies.Author(
                _id = "21", account = account, firstName = "", lastName = ""
            )

            Log.d(TAG, "onSubmit: handle reply to a comment")
            isReply = false
            val newCommentReplyEntity =
                ShortCommentReply(
                    commentId,
                    input.toString(),
                    localUpdateId,
                    isFeedCommentReply = isFeedComment
                )
            roomCommentReplyViewModel.insertCommentReply(newCommentReplyEntity)
            Log.d(TAG, "onSubmit: inserted comment $newCommentReplyEntity")
            lifecycleScope.launch {

            }
            val mongoDbTimeStamp = generateMongoDBTimestamp()

            val newReply = Comment(
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
                com.uyscuti.sharedmodule.data.model.Comment(
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
        binding.replyToLayout.visibility = View.GONE
        return true
    }

    @SuppressLint("SetTextI18n")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun toggleReplyToTextView(event: ToggleReplyToTextView) {

        isReply = true
        commentId = event.comment._id
        data = event.comment
        position = event.position
        Log.d(
            TAG,
            "toggleReplyToTextView: comment id $commentId data comment id ${data!!._id} comment position $position"
        )
        Log.d(TAG, "toggleReplyToTextView: data ${event.comment}")
        val username = event.comment.author!!.account.username

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

    private fun updateAdapter(
        data: com.uyscuti.sharedmodule.data.model.Comment, position: Int
    ) {
        Log.d("UpdateItem", "reply count visible ${data.replyCountVisible}")
        commentsAdapter?.updateItem(position, data)
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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onAddVoiceNote() {
        binding.VNLayout.visibility = View.VISIBLE
        binding.playAudioLayout.visibility = View.GONE
        binding.waveForm.visibility = View.VISIBLE
        binding.timerTv.visibility = View.VISIBLE
        startRecording()
        EventBus.getDefault().post(PauseShort(true))
    }

    override fun onAddGif() {
        val intent = Intent(this, GifActivity::class.java)
        startActivityForResult(intent, GIF_CODE)
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onAddAttachments() {
        showAttachmentDialog()
    }

    override fun onCommentsClick(position: Int, data: UserShortsEntity, isFeedComment: Boolean) {

    }

    override fun onTimerTick(duration: String) {
        binding.timerTv.text = duration

        var amplitude = mediaRecorder!!.maxAmplitude.toFloat()
        amplitude = if (amplitude > 0) amplitude else 130f

        binding.waveForm.addAmplitude(amplitude)
    }

    private fun addComment() {

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

    private fun addCommentReply() {
        Log.d(TAG, "addCommentReply: inside")

        if (isInternetAvailable(this)) {

            roomCommentReplyViewModel.allCommentReplies.observe(this) {

                if (it.isNotEmpty()) {
                    Log.d(TAG, "addComment: comments in room count is ${it.size}")
                    commentsReplyViewModel.commentReply(
                        it[0].commentId,
                        it[0].content,
                        it[0].localUpdateId,
                        it[0].isFeedCommentReply
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

    private fun addImageComment() {
        val TAG = "addImageComment"
        Log.d("addImageComment", "addImageComment: is reply $isReply")


        if (isInternetAvailable(this)) {

            commentFilesViewModel.allCommentFiles.observe(this) {

                Log.d(TAG, "Comments observed size:${it.size}")

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

            commentFilesViewModel.allCommentReplyFiles.observe(this) { it ->

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

                        bitmapThumbnail?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)

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

                                val comment = commentsAdapter?.getComment(it[0].parentPosition)

                                Log.d(TAG, "addCommentFileReply: comment get successful  $comment")
                                val replyToUpdate = comment?.replies?.find { reply ->
                                    Log.d(
                                        TAG,
                                        "addCommentFileReply: ids it[0].uploadId  ${it[0].uploadId} reply.uploadId  ${reply.uploadId}"
                                    )
                                    reply.uploadId == it[0].uploadId
                                }
                                replyToUpdate?._id = data._id
                                commentsAdapter?.notifyItemChanged(it[0].parentPosition)

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

    private fun addDocumentComment() {
        val TAG = "addDocumentComment"
        Log.d("addDocumentComment", "addDocumentComment: is reply $isReply")


        if (isInternetAvailable(this)) {

            commentFilesViewModel.allCommentFiles.observe(this) {

                Log.d(TAG, "Comments observed size:${it.size}")
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

                    }


                } else {
                    Log.d(TAG, "onSubmit: Room database has no comments")
                }
            }


        } else {
            Log.d(TAG, "addComment: no internet connection")
        }

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
                                Compressor.compress(this@OtherUserProfile, file)
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


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun showAttachmentDialog() {
        val dialog = BottomSheetDialog(this)

        dialog.setContentView(R.layout.file_upload_dialog)

        val video = dialog.findViewById<LinearLayout>(R.id.upload_video)
        val audio = dialog.findViewById<LinearLayout>(R.id.upload_audio)
        val image = dialog.findViewById<LinearLayout>(R.id.upload_image)
        val camera = dialog.findViewById<LinearLayout>(R.id.open_camera)
        val doc = dialog.findViewById<LinearLayout>(R.id.upload_doc)
        val location = dialog.findViewById<LinearLayout>(R.id.share_location)
        // Apply animation to the dialog's view
        val dialogView =
            dialog.findViewById<View>(R.id.design_bottom_sheet)
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

            val intent = Intent(this@OtherUserProfile, VideosActivity::class.java)
            dialog.dismiss()
            videoPickerLauncher.launch(intent)


        }

        audio?.setOnClickListener {
            val intent = Intent(this@OtherUserProfile, AudioActivity::class.java)
            
            dialog.dismiss()
            audioPickerLauncher.launch(intent)

        }

        doc?.setOnClickListener {
            openFilePicker()
            dialog.dismiss()

        }
        camera?.setOnClickListener {
            val intent = Intent(this@OtherUserProfile, CameraActivity::class.java)
//            startActivity(intent)
            cameraLauncher.launch(intent)
            dialog.dismiss()
        }

        location?.visibility = View.INVISIBLE
        location?.setOnClickListener {

        }


        dialog.show()
    }

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
                Account(
                    _id = "",
                    avatar = avatar,
                    "",
                    LocalStorage.Companion.getInstance(this).getUsername()
                )
            val author = Author(
                _id = "12", account = account, firstName = "", lastName = "",
                avatar = null
            )
            val imageFile = CommentFiles(
                _id = "124",
                url = imageFilePathToUpload,
                localPath = imageFilePathToUpload
            )
            val comment = com.uyscuti.sharedmodule.data.model.Comment(
                __v = 1,
                _id = commentsAdapter!!.itemCount.toString(),
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
                        localUpdateId,
                        isFeedComment = isFeedComment
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
                Log.d(TAG, "uploadImageComment: added on the adapter")
                updateUI(false)
            }

            Log.d("uploadImageComment", "uploadImageComment: comment $comment")
//        adapter.submitItems(listOf(comment) )
//            adapter!!.submitItem(comment, (adapter?.itemCount?.minus(1)!!))
//            adapter!!.submitItem(commentsAndRepliesModel, adapter!!.itemCount)

            recordedAudioFiles.clear()
            if (!update) {
                listOfReplies.add(comment)
                updateUI(false)
                commentsAdapter!!.submitItem(comment, commentsAdapter!!.itemCount)
                Log.d(TAG, "uploadImageComment: added on the adapter")
                if (!isFeedComment) {
                    shortToComment =
                        shortsCommentsViewModel.mutableShortsList.find { it._id == postId }
                    Log.d(
                        "uploadImageComment",
                        "uploadImageComment: count before ${shortToComment!!.comments}"
                    )

                    if (shortToComment != null) {
                        shortToComment!!.comments += 1

                        // Update the count in the mutableShortsList
                        // Update the count in the mutableShortsList
                        shortsCommentsViewModel.mutableShortsList.forEach { short ->
                            if (short._id == postId) {
                                short.comments = shortToComment!!.comments
                            }
                        }
                        val newShortToComment =
                            shortsCommentsViewModel.mutableShortsList.find { it._id == postId }
                        Log.d(
                            "uploadImageComment",
                            "onSubmit: count after ${newShortToComment!!.comments}"
                        )

                        EventBus.getDefault().post(ShortAdapterNotifyDatasetChanged())
                    }
                } else {
                    feedToComment = feedViewModel.getAllFeedData().find { it._id == postId }
                    Log.d(TAG, "onSubmit: total before feed count is ${feedToComment?.comments}")
                    myFeedToComment = feedViewModel.getMyFeedData().find { it._id == postId }
                    favoriteFeedToComment =
                        feedViewModel.getAllFavoriteFeedData().find { it._id == postId }

                    if (myFeedToComment != null) {
//                        myFeedToComment!!.comments += 1

                        feedViewModel.getMyFeedData().forEach { feed ->
                            if (feed._id == postId) {
                                feed.comments = myFeedToComment!!.comments
                            }
                        }
                    }
                    if (favoriteFeedToComment != null) {
//                        favoriteFeedToComment!!.comments += 1

                        feedViewModel.getAllFavoriteFeedData().forEach { feed ->
                            if (feed._id == postId) {
                                feed.comments = favoriteFeedToComment!!.comments
                            }
                        }

                    }
                    if (feedToComment != null) {
//                        feedToComment!!.comments += 1

                        feedViewModel.getAllFeedData().forEach { feed ->
                            if (feed._id == postId) {
                                feed.comments = feedToComment!!.comments
                            }
                        }
                        feedToComment = feedViewModel.getAllFeedData().find { it._id == postId }
                        Log.d(TAG, "onSubmit: total after feed count is ${feedToComment?.comments}")

                        EventBus.getDefault()
                            .post(FeedAdapterNotifyDatasetChanged(commentsAdapter!!.itemCount))

                    }
                }
            }

        } else {
            Log.e(TAG, "File does not exist")
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
            _id = "", avatar = avatar, "", LocalStorage.Companion.getInstance(this).getUsername()
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
                    content = binding.input.inputEditText.text.toString(),
                    isFeedComment = isFeedComment
                )
            commentFilesViewModel.insertCommentFile(newCommentReplyEntity)

            Log.d(
                "uploadReplyImageComment",
                "uploadReplyImageComment: inserted comment $newCommentReplyEntity"
            )
        }

        val mongoDbTimeStamp = generateMongoDBTimestamp()
        val imageFile = CommentFiles(_id = "", url = vnToUpload, localPath = "image")

        val newReply = Comment(
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
        val commentWithReplies = com.uyscuti.sharedmodule.data.model.Comment(
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

    @SuppressLint("SetTextI18n")
    private fun uploadReplyVideoComment(
        videoToUpload: String, durationString: String,
        placeholder: Boolean = false, update: Boolean = false
    ) {
        Log.d("uploadReplyVideoComment", "uploadReplyVideoComment: $videoToUpload")
        Log.d("uploadReplyVideoComment", "uploadReplyVideoComment: isReply is $isReply")


        val localUpdateId = generateRandomId()
        val profilePic2 = settings.getString("profile_pic", "").toString()
        val avatar = com.uyscuti.social.network.api.response.commentreply.allreplies.Avatar(
            "", "", url = profilePic2
        )
        val account = com.uyscuti.social.network.api.response.commentreply.allreplies.Account(
            _id = "", avatar = avatar, "", LocalStorage.Companion.getInstance(this).getUsername()
        )


        val commentReplyAuthor = com.uyscuti.social.network.api.response.commentreply.allreplies.Author(
            _id = "21", account = account, firstName = "", lastName = ""
        )

        Log.d("uploadReplyVideoComment", "uploadReplyVideoComment: handle reply to a comment")

//        val newCommentReplyEntity = CommentsFilesEntity(commentId, vnToUpload, vnToUpload, isReply = 1)

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
                    content = binding.input.inputEditText.text.toString(),
                    isFeedComment = isFeedComment
                )
            commentFilesViewModel.insertCommentFile(newCommentReplyEntity)

            Log.d(
                "uploadReplyVideoComment",
                "uploadReplyVideoComment: inserted comment $newCommentReplyEntity"
            )
        }

        lifecycleScope.launch {

        }
        val mongoDbTimeStamp = generateMongoDBTimestamp()
        val videoFile = CommentFiles(_id = "", url = videoToUpload, localPath = "video")

        val newReply = Comment(
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
        val commentWithReplies = com.uyscuti.sharedmodule.data.model.Comment(
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
                "uploadReplyVideoComment",
                "uploadReplyVideoComment: comment id = data is? $commentId = ${data!!._id} on position $position"
            )
            Log.d(
                "uploadReplyVideoComment",
                "uploadReplyVideoComment: comment id = data is? $commentId = ${data!!._id} on position $position"
            )
        }
        binding.input.inputEditText.setText("")
        binding.replyToLayout.visibility = View.GONE
    }

    @RequiresApi(Build.VERSION_CODES.O)
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
                Account(
                    _id = "",
                    avatar = avatar,
                    "",
                    LocalStorage.Companion.getInstance(this).getUsername()
                )
            val author = Author(
                _id = "12", account = account, firstName = "", lastName = "",
                avatar = null
            )
            val videoFile = CommentFiles(
                _id = localUpdateId,
                url = videoFilePathToUpload,
                localPath = videoFilePathToUpload
            )
            val comment = com.uyscuti.sharedmodule.data.model.Comment(
                __v = 1,
                _id = commentsAdapter!!.itemCount.toString(),
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
                        duration = durationString,
                        isFeedComment = isFeedComment
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
                updateUI(false)
            }

            Log.d("uploadVideoComment", "uploadVideoComment: comment $comment")

            recordedAudioFiles.clear()
            if (!update) {
                listOfReplies.add(comment)

                runOnUiThread {
                    updatePosition = commentsAdapter!!.itemCount
                    commentsAdapter!!.submitItem(comment, commentsAdapter!!.itemCount)
                    updateUI(false)

                }

                if (!isFeedComment) {
                    shortToComment =
                        shortsCommentsViewModel.mutableShortsList.find { it._id == postId }
                    Log.d(
                        "uploadVideoComment",
                        "uploadVideoComment: count before ${shortToComment!!.comments}"
                    )

                    if (shortToComment != null) {
                        shortToComment!!.comments += 1

                        shortsCommentsViewModel.mutableShortsList.forEach { short ->
                            if (short._id == postId) {
                                short.comments = shortToComment!!.comments
                            }
                        }
                        val newShortToComment =
                            shortsCommentsViewModel.mutableShortsList.find { it._id == postId }
                        Log.d(
                            "uploadVideoComment",
                            "onSubmit: count after ${newShortToComment!!.comments}"
                        )

                        EventBus.getDefault().post(ShortAdapterNotifyDatasetChanged())
                    }
                } else {
                    feedToComment = feedViewModel.getAllFeedData().find { it._id == postId }
                    Log.d(TAG, "onSubmit: total before feed count is ${feedToComment?.comments}")
                    myFeedToComment = feedViewModel.getMyFeedData().find { it._id == postId }
                    favoriteFeedToComment =
                        feedViewModel.getAllFavoriteFeedData().find { it._id == postId }
//                    Log.d(TAG, "onSubmit: total before feed count is ${feedToComment?.comments}")

                    if (myFeedToComment != null) {
//                        myFeedToComment!!.comments += 1

                        feedViewModel.getMyFeedData().forEach { feed ->
                            if (feed._id == postId) {
                                feed.comments = myFeedToComment!!.comments
                            }
                        }
//                        EventBus.getDefault().post(FeedAdapterNotifyDatasetChanged(adapter!!.itemCount))
                    }
                    if (favoriteFeedToComment != null) {
//                        favoriteFeedToComment!!.comments += 1

                        feedViewModel.getAllFavoriteFeedData().forEach { feed ->
                            if (feed._id == postId) {
                                feed.comments = favoriteFeedToComment!!.comments
                            }
                        }
//                        favoriteFeedToComment = feedViewModel.getAllFavoriteFeedData().find { it._id == postId }
//                        Log.d(TAG, "onSubmit: total after feed count is ${favoriteFeedToComment?.comments}")
//
//                        EventBus.getDefault().post(FeedAdapterNotifyDatasetChanged(adapter!!.itemCount))

                    }
                    if (feedToComment != null) {
//                        feedToComment!!.comments += 1

                        feedViewModel.getAllFeedData().forEach { feed ->
                            if (feed._id == postId) {
                                feed.comments = feedToComment!!.comments
                            }
                        }
                        feedToComment = feedViewModel.getAllFeedData().find { it._id == postId }
                        Log.d(TAG, "onSubmit: total after feed count is ${feedToComment?.comments}")

                        EventBus.getDefault()
                            .post(FeedAdapterNotifyDatasetChanged(commentsAdapter!!.itemCount))

                    }
                }

                //            addCommentVN()


            }
        } else {
            Log.e(TAG, "File does not exist")
        }


    }

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

                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onSuccess(index: Int, size: Long, path: String?) {

                        Log.d("Compress", "comment compress successful is reply $isReply")
                        Log.d("Compress", "comment file size: ${getFileSize(size)}")
                        Log.d("Compress", "comment path: $path")
//                        val thumbnailFile = saveBitmapToFile(thumbnail, applicationContext)
//                        val thumbnailFilePath = thumbnailFile.absolutePath

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

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            intent.setType("application/*")

        }
        getDocumentContent.launch(intent)
    }

    private val getDocumentContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    // Handle the selected document URI
                    handleDocumentUri(uri)
                }
            }
        }

    @RequiresApi(Build.VERSION_CODES.O)
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
//                toCompressUris.add(uri)
////                                }
//                compressShorts(
//                    "",
//                    fileType = "doc",
//                    fileName = fileName,
//                    numberOfPages = numberOfPages,
//                    documentType = documentType,
//                    formattedFileSize = formattedFileSize
//                )
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
                                if (postId != " ") {
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
                Account(
                    _id = "",
                    avatar = avatar,
                    "",
                    LocalStorage.Companion.getInstance(this).getUsername()
                )
            val author =
                Author(_id = "12", account = account, firstName = "", lastName = "", avatar = null)
            val documentFile = CommentFiles(
                _id = "124",
                url = documentFilePathToUpload,
                localPath = documentFilePathToUpload
            )
            val comment = com.uyscuti.sharedmodule.data.model.Comment(
                __v = 1,
                _id = commentsAdapter!!.itemCount.toString(),
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
                        fileName = fileName,
                        isFeedComment = isFeedComment
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
                updateUI(false)
            }

            Log.d("uploadDocumentComment", "uploadDocumentComment: comment $comment")
//        adapter.submitItems(listOf(comment) )
//            adapter!!.submitItem(comment, (adapter?.itemCount?.minus(1)!!))
//            adapter!!.submitItem(commentsAndRepliesModel, adapter!!.itemCount)

            recordedAudioFiles.clear()
            if (!update) {
                listOfReplies.add(comment)

                commentsAdapter!!.submitItem(comment, commentsAdapter!!.itemCount)
                updateUI(false)
                if (!isFeedComment) {
                    shortToComment =
                        shortsCommentsViewModel.mutableShortsList.find { it._id == postId }
                    Log.d(
                        "uploadDocumentComment",
                        "uploadDocumentComment: count before ${shortToComment!!.comments}"
                    )

                    if (shortToComment != null) {
                        shortToComment!!.comments += 1

                        // Update the count in the mutableShortsList
                        // Update the count in the mutableShortsList
                        shortsCommentsViewModel.mutableShortsList.forEach { short ->
                            if (short._id == postId) {
                                short.comments = shortToComment!!.comments
                            }
                        }
                        val newShortToComment =
                            shortsCommentsViewModel.mutableShortsList.find { it._id == postId }
                        Log.d(
                            "uploadDocumentComment",
                            "onSubmit: count after ${newShortToComment!!.comments}"
                        )

                        EventBus.getDefault().post(ShortAdapterNotifyDatasetChanged())
                    }
                } else {
                    feedToComment = feedViewModel.getAllFeedData().find { it._id == postId }
                    myFeedToComment = feedViewModel.getMyFeedData().find { it._id == postId }
                    favoriteFeedToComment =
                        feedViewModel.getAllFavoriteFeedData().find { it._id == postId }
                    Log.d(TAG, "onSubmit: total before feed count is ${feedToComment?.comments}")

                    if (myFeedToComment != null) {
//                        myFeedToComment!!.comments += 1

                        feedViewModel.getMyFeedData().forEach { feed ->
                            if (feed._id == postId) {
                                feed.comments = myFeedToComment!!.comments
                            }
                        }
//                        EventBus.getDefault().post(FeedAdapterNotifyDatasetChanged(adapter!!.itemCount))
                    }
                    if (favoriteFeedToComment != null) {
//                        favoriteFeedToComment!!.comments += 1

                        feedViewModel.getAllFavoriteFeedData().forEach { feed ->
                            if (feed._id == postId) {
                                feed.comments = favoriteFeedToComment!!.comments
                            }
                        }
//                        favoriteFeedToComment = feedViewModel.getAllFavoriteFeedData().find { it._id == postId }
//                        Log.d(TAG, "onSubmit: total after feed count is ${favoriteFeedToComment?.comments}")
//
//                        EventBus.getDefault().post(FeedAdapterNotifyDatasetChanged(adapter!!.itemCount))

                    }
                    if (feedToComment != null) {
//                        feedToComment!!.comments += 1

                        feedViewModel.getAllFeedData().forEach { feed ->
                            if (feed._id == postId) {
                                feed.comments = feedToComment!!.comments
                            }
                        }
                        feedToComment = feedViewModel.getAllFeedData().find { it._id == postId }
                        Log.d(TAG, "onSubmit: total after feed count is ${feedToComment?.comments}")

                        EventBus.getDefault()
                            .post(FeedAdapterNotifyDatasetChanged(commentsAdapter!!.itemCount))

                    }
                }
//            addCommentVN()

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
            _id = "", avatar = avatar, "", LocalStorage.Companion.getInstance(this).getUsername()
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
                    content = binding.input.inputEditText.text.toString(),
                    isFeedComment = isFeedComment
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

        val newReply = Comment(
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
        val commentWithReplies = com.uyscuti.sharedmodule.data.model.Comment(
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun uploadVnComment(
        vnToUpload: String,
        fileName: String,
        durationString: String,
        fileType: String,
        placeholder: Boolean = false, update: Boolean = false
    ) {
        Log.d("uploadVnComment", "uploadVnComment: placeholder $placeholder")
//        Log.d("uploadVnComment", "stopRecording: isReply is $isReply")
//        Log.d("uploadVnComment", "stopRecording: duration is $durationString")

        val mongoDbTimeStamp = generateMongoDBTimestamp()

        val localUpdateId = generateRandomId()
        val uploadId = generateRandomId()
        val file = File(vnToUpload)

        if (file.exists()) {
            Log.d(TAG, "File exists, creating comment.......")
            val profilePic2 = settings.getString("profile_pic", "").toString()
            val avatar = Avatar("", "", url = profilePic2)
            val account =
                Account(
                    _id = "",
                    avatar = avatar,
                    "",
                    LocalStorage.Companion.getInstance(this).getUsername()
                )
            val author =
                Author(_id = "12", account = account, firstName = "", lastName = "", avatar = null)
            val vnFile = CommentFiles(_id = "124", url = vnToUpload, localPath = vnToUpload)
            val comment = com.uyscuti.sharedmodule.data.model.Comment(
                __v = 1,
                _id = commentsAdapter!!.itemCount.toString(),
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
                fileType = fileType,
                uploadId = uploadId
            )


            if (!placeholder) {
                val newCommentEntity =
                    CommentsFilesEntity(
                        postId,
                        "audio",
                        vnToUpload,
                        isReply = 0,
                        localUpdateId,
                        fileName = fileName,
                        duration = durationString,
                        fileType = fileType,
                        isFeedComment = isFeedComment,
                        uploadId = uploadId
                    )
                commentFilesViewModel.insertCommentFile(newCommentEntity)
                Log.d(TAG, "uploadVnComment: inserted comment $newCommentEntity")
            }
            if (update) {
                Log.d("uploadVnComment", "updatePosition: $updatePosition")
                listOfReplies.add(comment)
                updateAdapter(comment, updatePosition)
                updatePosition = -1
                updateUI(false)
            }

            Log.d(TAG, "uploadVnComment: comment $comment")
//        adapter.submitItems(listOf(comment) )
//            adapter!!.submitItem(comment, (adapter?.itemCount?.minus(1)!!))
//            adapter!!.submitItem(commentsAndRepliesModel, adapter!!.itemCount)
            recordedAudioFiles.clear()

            if (!update) {
                listOfReplies.add(comment)
                updatePosition = commentsAdapter!!.itemCount
                commentsAdapter!!.submitItem(comment, commentsAdapter!!.itemCount)
                updateUI(false)
                if (!isFeedComment) {
                    shortToComment =
                        shortsCommentsViewModel.mutableShortsList.find { it._id == postId }
                    Log.d(TAG, "uploadVnComment: count before ${shortToComment!!.comments}")

                    if (shortToComment != null) {
                        shortToComment!!.comments += 1

                        // Update the count in the mutableShortsList
                        // Update the count in the mutableShortsList
                        shortsCommentsViewModel.mutableShortsList.forEach { short ->
                            if (short._id == postId) {
                                short.comments = shortToComment!!.comments
                            }
                        }
                        val newShortToComment =
                            shortsCommentsViewModel.mutableShortsList.find { it._id == postId }
                        Log.d(TAG, "onSubmit: count after ${newShortToComment!!.comments}")

                        EventBus.getDefault().post(ShortAdapterNotifyDatasetChanged())
                    }
                } else {
                    feedToComment = feedViewModel.getAllFeedData().find { it._id == postId }
                    Log.d(TAG, "onSubmit: total before feed count is ${feedToComment?.comments}")
                    myFeedToComment = feedViewModel.getMyFeedData().find { it._id == postId }
                    favoriteFeedToComment =
                        feedViewModel.getAllFavoriteFeedData().find { it._id == postId }
//                    Log.d(TAG, "onSubmit: total before feed count is ${feedToComment?.comments}")

                    if (myFeedToComment != null) {
//                        myFeedToComment!!.comments += 1

                        feedViewModel.getMyFeedData().forEach { feed ->
                            if (feed._id == postId) {
                                feed.comments = myFeedToComment!!.comments
                            }
                        }
//                        EventBus.getDefault().post(FeedAdapterNotifyDatasetChanged(adapter!!.itemCount))
                    }
                    if (favoriteFeedToComment != null) {
//                        favoriteFeedToComment!!.comments += 1

                        feedViewModel.getAllFavoriteFeedData().forEach { feed ->
                            if (feed._id == postId) {
                                feed.comments = favoriteFeedToComment!!.comments
                            }
                        }
//                        favoriteFeedToComment = feedViewModel.getAllFavoriteFeedData().find { it._id == postId }
//                        Log.d(TAG, "onSubmit: total after feed count is ${favoriteFeedToComment?.comments}")
//
//                        EventBus.getDefault().post(FeedAdapterNotifyDatasetChanged(adapter!!.itemCount))

                    }
                    if (feedToComment != null) {
//                        feedToComment!!.comments += 1

                        feedViewModel.getAllFeedData().forEach { feed ->
                            if (feed._id == postId) {
                                feed.comments = feedToComment!!.comments
                            }
                        }
                        feedToComment = feedViewModel.getAllFeedData().find { it._id == postId }
                        Log.d(TAG, "onSubmit: total after feed count is ${feedToComment?.comments}")

                        EventBus.getDefault()
                            .post(FeedAdapterNotifyDatasetChanged(commentsAdapter!!.itemCount))

                    }
                }
//            addCommentVN()

            }

        } else {
            Log.e(TAG, "File does not exist")
        }


    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
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
            _id = "", avatar = avatar, "", LocalStorage.Companion.getInstance(this).getUsername()
        )


        val commentReplyAuthor = com.uyscuti.social.network.api.response.commentreply.allreplies.Author(
            _id = "21", account = account, firstName = "", lastName = ""
        )

        Log.d(TAG, "onSubmit: handle reply to a comment")
//        isReply = false

//        val newCommentReplyEntity = CommentsFilesEntity(commentId, vnToUpload, vnToUpload, isReply = 1)

        //if it clash on upload un comment the line below//


        val mongoDbTimeStamp = generateMongoDBTimestamp()
        val vnFile = CommentFiles(_id = "", url = vnToUpload, localPath = vnToUpload)

        val uploadId = generateRandomId()
        val newReply = Comment(
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
            duration = durationString,
            uploadId = uploadId
        )

        val replyCount = data!!.replyCount + 1
        val commentWithReplies = com.uyscuti.sharedmodule.data.model.Comment(
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

        if (!placeholder) {
            Log.d("placeholder", "uploadReplyVnComment: not placeholder")
            val newCommentReplyEntity =
                CommentsFilesEntity(
                    postId, "audio", vnToUpload, isReply = 1, localUpdateId,
                    fileName = fileName, fileType = fileType, duration = durationString,
                    content = binding.input.inputEditText.text.toString(),
                    isFeedComment = isFeedComment,
                    parentPosition = updateReplyPosition,
                    uploadId = uploadId
                )
//            commentFilesViewModel.insertCommentFile(newCommentReplyEntity)


            commentFilesViewModel.insertCommentFile(newCommentReplyEntity)
            Log.d(TAG, "onSubmit: inserted comment $newCommentReplyEntity")
//            commentFilesViewModel
        } else {
//            isReply = false
            Log.d("placeholder", "uploadReplyVnComment: its placeholder ")
        }


        if (update) {
            isReply = false
            Log.d("placeholder", "updatePosition: $updatePosition")
            updateAdapter(commentWithReplies, updateReplyPosition)
            updateReplyPosition = -1
        }


        if (!update) {
            listOfReplies.add(commentWithReplies)
            Log.d(
                "placeholder",
                "onSubmit: comment id = data is? $commentId = ${data!!._id} on position $position"
            )
            Log.d(
                "placeholder",
                "onSubmit: comment id = data is? $commentId = ${data!!._id} on position $position"
            )
            updateAdapter(commentWithReplies, position)
        }

        binding.input.inputEditText.setText("")
        binding.replyToLayout.visibility = View.GONE
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

    override fun onViewRepliesClick(
        data: com.uyscuti.sharedmodule.data.model.Comment,
        repliesRecyclerView: RecyclerView,
        position: Int
    ) {

    }

    @SuppressLint("SetTextI18n")
    override fun onViewRepliesClick(
        data: com.uyscuti.sharedmodule.data.model.Comment,
        position: Int,
        commentRepliesTV: TextView,
        hideCommentReplies: TextView,
        repliesRecyclerView: RecyclerView,
        isRepliesVisible: Boolean,
        page: Int
    ) {
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
                val commentWithReplies = com.uyscuti.sharedmodule.data.model.Comment(
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
                )


                Log.d(
                    TAG,
                    "onViewRepliesClick: has next page ${commentReplies.hasNextPage} page number ${commentReplies.pageNumber}"
                )
                val updatedComment = commentWithReplies.copy(
                    replies = data.replies.toMutableList().apply {
                        addAll(commentReplies.comments)
                    },
                    isRepliesVisible = isRepliesVisible,
                    hasNextPage = commentReplies.hasNextPage,
                    pageNumber = commentReplies.pageNumber
                )

                withContext(Dispatchers.Main) {
                    commentsAdapter?.updateItem(position, updatedComment)
                    hideCommentReplies.visibility = View.VISIBLE
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
//                audioPlayPauseBtn.setImageResource(R.drawable.play_svgrepo_com)

                if (isVnAudio) {
                    Log.d("waveProgress", "toggleAudioPlayer: $waveProgress")

                    commentsAdapter?.setReplySecondWaveFormProgress(waveProgress, position)
                    commentsAdapter?.setSecondWaveFormProgress(waveProgress, position)
                } else {
                    //for seek bar
                    commentsAdapter?.setSecondSeekBarProgress(seekBarProgress, position)
                    commentsAdapter?.setReplySecondSeekBarProgress(seekBarProgress, position)
                }


                exoPlayer?.pause()
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

            if (isReply) {
                Log.d("IsReply", "is reply position $position")
//                adapter?.refreshMainComment(position)
            }

            commentAudioStartPlaying(audioToPlayPath, audioPlayPauseBtn, progress, position)

            currentCommentAudioPosition = position
            currentCommentAudioPath = audioToPlayPath
            Log.d(
                "toggleAudioPlayer",
                "toggleAudioPlayer: position updated $currentCommentAudioPosition"
            )
        }
    }

    override fun onReplyButtonClick(
        position: Int,
        data: com.uyscuti.sharedmodule.data.model.Comment
    ) {
        TODO("Not yet implemented")
    }

    override fun likeUnLikeComment(
        position: Int,
        data: com.uyscuti.sharedmodule.data.model.Comment
    ) {
        TODO("Not yet implemented")
    }

    override fun likeUnlikeCommentReply(
        replyPosition: Int,
        replyData: com.uyscuti.social.network.api.response.commentreply.allreplies.Comment,
        mainCommentPosition: Int,
        mainComment: com.uyscuti.sharedmodule.data.model.Comment
    ) {
        TODO("Not yet implemented")
    }

    private fun commentAudioPause(audioPlayPauseBtn: ImageView, isReply: Boolean) {
        Log.d("TAG", "commentAudioPause: is Reply $isReply")
        isDurationOnPause = true

        audioPlayPauseBtn.setImageResource(R.drawable.play_svgrepo_com)
        commentsAdapter!!.updatePlaybackButton(
            currentCommentAudioPosition,
            isReply,
            audioPlayPauseBtn
        )
        exoPlayer?.pause()
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    @SuppressLint("NotifyDataSetChanged")
    private fun commentAudioStartPlaying(
        vnAudio: String,
        audioPlayPauseBtn: ImageView,
        progress: Float, position: Int
    ) {
        EventBus.getDefault().post(PauseShort(true))
        isDurationOnPause = false
        if (isVnAudioToPlay) {
            startWaveRunnable()
        }

        audioPlayPauseBtn.setImageResource(R.drawable.baseline_pause_black)

        try {
            val file = File(vnAudio)
            if (file.exists()) {
                val fileUrl = Uri.fromFile(file)
                exoPlayer = ExoPlayer.Builder(this)
                    .build()
                Log.d("commentAudioStartPlaying", "commentAudioStartPlaying: Local file $fileUrl")

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

                exoPlayer!!.setMediaSource(mediaSource)
            }

            exoPlayer!!.prepare()
            exoPlayer!!.seekTo(progress.toLong())
            exoPlayer!!.playWhenReady = true
//            exoPlayer!!.play()
            exoPlayer!!.repeatMode = Player.REPEAT_MODE_OFF
            exoPlayer!!.addListener(playbackStateListener)
            exoPlayer!!.addListener(object : Player.Listener {
                @Deprecated("Deprecated in Java")
                override fun onPlayerStateChanged(
                    playWhenReady: Boolean,
                    playbackState: Int
                ) {
//                    if (playbackState == Player.STATE_READY && exoPlayer!!.duration != C.TIME_UNSET) {
//                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    error.printStackTrace()
                    Toast.makeText(
                        this@OtherUserProfile,
                        "Can't play this audio",
                        Toast.LENGTH_SHORT
                    ).show()

                }


            })
            if (isReplyVnPlaying) {
//                Log.d("isReplyVnPlaying", "isReplyVnPlaying $isReplyVnPlaying")
                val handler = Handler()

                handler.postDelayed({
                    commentsAdapter?.refreshMainComment(position)
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
                            commentsAdapter?.updateReplyWaveProgress(0f, audioFormWave)
                            if (isReplyVnPlaying) {
                                Log.d("isReplyVnPlaying", "isReplyVnPlaying $isReplyVnPlaying")
                                val handler = Handler()

                                handler.postDelayed({
                                    commentsAdapter?.refreshMainComment(position)
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
                        commentsAdapter?.refreshAudioComment(currentCommentAudioPosition)
                    }

                    Log.d(
                        "audioSeekBar",
                        "currentCommentAudioPosition $currentCommentAudioPosition"
                    )

                    commentsAdapter?.refreshMainComment(position)
                    commentsAdapter?.changePlayingStatus()
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
                }

                else -> {
                    Log.d("TAG", "STOP SEEK BAR")
                }
            }
        }

        override fun onIsPlayingChanged(isVideoPlaying: Boolean) {
        }

        override fun onEvents(player: Player, events: Player.Events) {
            if (events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED) ||
                events.contains(Player.EVENT_IS_PLAYING_CHANGED)
            ) {
            }

            if (events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION)
            ) {
            }
        }
    }

    private val waveRunnable = object : Runnable {
        override fun run() {
            if (!isDurationOnPause) {
                val currentPosition = exoPlayer?.currentPosition?.toFloat()!!

                waveProgress = currentPosition
                if (isReplyVnPlaying) {
                    commentsAdapter!!.updateReplyWaveProgress(currentPosition, audioFormWave)
                } else {
                    commentsAdapter!!.updateWaveProgress(currentPosition, wavePosition)
                }
                audioDurationTVCount.text = String.format(
                    "%s",
                    TrimVideoUtils.stringForTime(currentPosition)
                )
            }
            waveHandler.postDelayed(this, 20)
        }
    }

    private fun stopWaveRunnable() {
        try {
            waveHandler.removeCallbacks(waveRunnable)
            isDurationOnPause = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun commentAudioStop() {
        if (isVnAudioToPlay) {
            if (::audioFormWave.isInitialized) {
                audioFormWave.progress = 0f
            }
            commentsAdapter?.setSecondWaveFormProgress(0f, currentCommentAudioPosition)
            commentsAdapter?.setReplySecondWaveFormProgress(0f, currentCommentAudioPosition)
        } else {
            commentsAdapter?.setSecondSeekBarProgress(0f, currentCommentAudioPosition)
            commentsAdapter?.setReplySecondSeekBarProgress(0f, currentCommentAudioPosition)
        }
        currentCommentAudioPosition = RecyclerView.NO_POSITION
        currentCommentAudioPath = ""
        commentsAdapter?.resetAudioPlay()

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
                                commentsAdapter!!.updateReplySeekBarProgress(
                                    it.currentPosition.toFloat(),
                                    audioSeekBar
                                )

                            } else {
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
        currentHandler = handler
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun commentAudioSeekBar(event: CommentAudioPlayerHandler) {
        audioSeekBar = event.audioSeekBar
        audioDurationTVCount = event.leftDuration
        seekPosition = event.position
        maxDuration = event.maxDuration
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

    private fun updateRecordWaveProgress(progress: Float) {
        CoroutineScope(Dispatchers.Main).launch {
            binding.wave.progress = progress
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
//            binding.playVnAudioBtn.setImageResource(R.drawable.baseline_pause_black)
            binding.playVnAudioBtn.setImageResource(R.drawable.play_svgrepo_com)

            binding.recordVN.setImageResource(R.drawable.baseline_pause_black)
        }

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

    @SuppressLint("SetTextI18n")
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
//            binding.recordVN.setImageResource(R.drawable.baseline_pause_24)
            binding.recordVN.setImageResource(com.uyscuti.social.call.R.drawable.ic_mic_on)

//            binding.deleteVN.setBackgroundResource(R.drawable.ic_ripple_disabled)
//            binding.deleteVN.isClickable = false
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

    @SuppressLint("SetTextI18n")
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
//            binding.recordVN.setImageResource(R.drawable.baseline_pause_24)
            binding.recordVN.setImageResource(com.uyscuti.social.call.R.drawable.ic_mic_on)
//            binding.deleteVN.setBackgroundResource(R.drawable.ic_ripple_disabled)
//            binding.deleteVN.isClickable = false
            binding.sendVN.setBackgroundResource(R.drawable.ic_ripple_disabled)
            binding.sendVN.isClickable = false

            amplitudes = binding.waveForm.clear()
            amps = 0
            timer.stop()
            if (player?.isPlaying == true) {
                stopPlaying()
            }
            binding.VNLayout.visibility = View.GONE

            binding.replyToLayout.visibility = View.GONE

            val file = File(outputVnFile)
            val file2 = File(outputFile)


            if (!isReply) {

                if (recordedAudioFiles.size != 1) {
                    val durationString = getFormattedDuration(outputVnFile)
                    val fileName = getFileNameFromLocalPath(outputVnFile)
                    uploadVnComment(outputVnFile, fileName, durationString, "vnAudio")
//                    firstTimeSendVn = false
                } else {
                    val durationString = getFormattedDuration(outputFile)
                    val fileName = getFileNameFromLocalPath(outputFile)
                    uploadVnComment(outputFile, fileName, durationString, "vnAudio")
                }
            } else {
                if (recordedAudioFiles.size != 1) {
                    val durationString = getFormattedDuration(outputVnFile)
                    val reverseDurationString = reverseFormattedDuration(durationString)
                    val fileName = getFileNameFromLocalPath(outputVnFile)
                    uploadReplyVnComment(outputVnFile, fileName, durationString, "vnAudio")
                } else {
                    val durationString = getFormattedDuration(outputFile)
                    val fileName = getFileNameFromLocalPath(outputFile)
                    uploadReplyVnComment(outputFile, fileName, durationString, "vnAudio")
                }
            }


        } catch (e: Exception) {
            e.printStackTrace()
            // Handle exceptions as needed
        }
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
//        }
    }

    @SuppressLint("DefaultLocale")
    private fun inflateWave(outputVN: String) {

        val audioFile = File(outputVN)
        binding.wave.visibility = View.VISIBLE
        binding.playerTimerTv.visibility = View.VISIBLE

        val file = File(outputVN)

        val localAudioDuration = AudioDurationHelper.getLocalAudioDuration(outputVN)
        if (localAudioDuration != null) {
            val minutes = (localAudioDuration / 1000) / 60
            val seconds = (localAudioDuration / 1000) % 60

            binding.thirdTimerTv.text = String.format("%02d:%02d", minutes, seconds)
        } else {
            Log.e(TAG, "render: failed to retrieve audio duration")

        }

        CoroutineScope(Dispatchers.IO).launch {
            WaveFormExtractor.getSampleFrom(applicationContext, outputVN) {

                CoroutineScope(Dispatchers.Main).launch {

                    if (localAudioDuration != null) {
                        binding.wave.maxProgress = localAudioDuration.toFloat()
                    }
                    binding.wave.setSampleFrom(it)

                    binding.wave.onProgressChanged = object : SeekBarOnProgressChanged {
                        override fun onProgressChanged(
                            waveformSeekBar: WaveformSeekBar,
                            progress: Float,
                            fromUser: Boolean
                        ) {
                            binding.secondTimerTv.text = String.format(
                                "%s",
                                TrimVideoUtils.stringForTime(progress)
                            )

                            if (fromUser) {
                                if (vnRecordAudioPlaying) {
                                    pauseVn(progress = progress.toInt())
                                } else {
                                    vnRecordProgress = progress.toInt()
                                }

                            }
                        }

                        override fun onRelease(event: MotionEvent?, progress: Float) {
                            if (outputVN.isNotEmpty()) {
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
    }

    private fun pauseVn(progress: Int) {
        player?.pause()
        player?.seekTo(progress)
        isAudioVNPlaying = false
        isAudioVNPaused = true
        isOnRecordDurationOnPause = true

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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun audioWave(event: AudioPlayerHandler) {
        audioFormWave = event.audioWave
        audioDurationTVCount = event.leftDuration
        wavePosition = event.position
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == R_CODE && resultCode == RESULT_OK && data != null) {
            // Handle the result from the adapter
            // You can extract data from the Intent if needed
            val modifiedData =
                data.getSerializableExtra("data") as com.uyscuti.sharedmodule.data.model.Comment
            val currentReplyData =
                data.getSerializableExtra("currentReplyComment") as Comment?

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

        } else if (requestCode == COMMENT_VIDEO_CODE && resultCode == RESULT_OK && data != null) {

            val modifiedData =
                data.getSerializableExtra("data") as com.uyscuti.sharedmodule.data.model.Comment
            val currentReplyData =
                data.getSerializableExtra("currentReplyComment") as Comment?

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


    @SuppressLint("SetTextI18n")
    private fun toggleReplyFromViewsActivity(
        data: com.uyscuti.sharedmodule.data.model.Comment,
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

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private fun likeUnLikeCommentFromViewsActivity(
        position: Int, data: com.uyscuti.sharedmodule.data.model.Comment
    ) {
        val updatedComment = if (data.isLiked) {
            data.copy(
                likes = data.likes + 1,
            )
        } else {
            data.copy(
                likes = data.likes - 1,
            )
        }
        commentsAdapter?.updateItem(position, updatedComment)

        if (isInternetAvailable(this)) {

            var result by Delegates.notNull<Boolean>()
            lifecycleScope.launch {
                result = if (isFeedComment) {
                    feedCommentLikeUnLike(data._id)

                } else {
                    commentLikeUnLike(data._id)
                }

            }
        } else {
            Log.d(TAG, "likeUnLikeCommentFromViewsActivity: cant like offline")
        }

    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private fun likeCommentReplyFromViewsActivity(event: LikeCommentReply) {

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
        commentsAdapter?.updateItem(event.position, event.comment)

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
                Account(
                    _id = "",
                    avatar = avatar,
                    "",
                    LocalStorage.Companion.getInstance(this).getUsername()
                )
            val author =
                Author(_id = "12", account = account, firstName = "", lastName = "", avatar = null)
//            val gifFile = CommentFiles(
//                _id = "124",
//                url = gifFilePathToUpload,
//                localPath = gifFilePathToUpload
//            )
            val comment = com.uyscuti.sharedmodule.data.model.Comment(
                __v = 1,
                _id = commentsAdapter!!.itemCount.toString(),
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
                    isFeedComment = isFeedComment
                )
            commentFilesViewModel.insertCommentFile(newCommentEntity)
            Log.d("uploadGifComment", "uploadGifComment: inserted comment $newCommentEntity")

            Log.d("uploadGifComment", "uploadGifComment: comment $comment")
            listOfReplies.add(comment)

            recordedAudioFiles.clear()
            commentsAdapter!!.submitItem(comment, commentsAdapter!!.itemCount)

            updateUI(false)

            if (!isFeedComment) {
                shortToComment = shortsCommentsViewModel.mutableShortsList.find { it._id == postId }
                Log.d(
                    "uploadGifComment",
                    "uploadGifComment: count before ${shortToComment!!.comments}"
                )

                if (shortToComment != null) {
                    shortToComment!!.comments += 1

                    shortsCommentsViewModel.mutableShortsList.forEach { short ->
                        if (short._id == postId) {
                            short.comments = shortToComment!!.comments
                        }
                    }
                    val newShortToComment =
                        shortsCommentsViewModel.mutableShortsList.find { it._id == postId }
                    Log.d(
                        "uploadGifComment",
                        "onSubmit: count after ${newShortToComment!!.comments}"
                    )

                    EventBus.getDefault().post(ShortAdapterNotifyDatasetChanged())
                }
            } else {
                feedToComment = feedViewModel.getAllFeedData().find { it._id == postId }
//                Log.d(TAG, "onSubmit: total before feed count is ${feedToComment?.comments}")
                myFeedToComment = feedViewModel.getMyFeedData().find { it._id == postId }
                favoriteFeedToComment =
                    feedViewModel.getAllFavoriteFeedData().find { it._id == postId }
                Log.d(TAG, "onSubmit: total before feed count is ${feedToComment?.comments}")

                if (myFeedToComment != null) {
//                    myFeedToComment!!.comments += 1

                    feedViewModel.getMyFeedData().forEach { feed ->
                        if (feed._id == postId) {
                            feed.comments = myFeedToComment!!.comments
                        }
                    }
                }
                if (favoriteFeedToComment != null) {
//                    favoriteFeedToComment!!.comments += 1

                    feedViewModel.getAllFavoriteFeedData().forEach { feed ->
                        if (feed._id == postId) {
                            feed.comments = favoriteFeedToComment!!.comments
                        }
                    }
                }
                if (feedToComment != null) {
//                    feedToComment!!.comments += 1

                    feedViewModel.getAllFeedData().forEach { feed ->
                        if (feed._id == postId) {
                            feed.comments = feedToComment!!.comments
                        }
                    }
                    feedToComment = feedViewModel.getAllFeedData().find { it._id == postId }
                    Log.d(TAG, "onSubmit: total after feed count is ${feedToComment?.comments}")

                    EventBus.getDefault()
                        .post(FeedAdapterNotifyDatasetChanged(commentsAdapter!!.itemCount))

                }
            }

        } else {
            Log.e(TAG, "File does not exist")
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
            _id = "", avatar = avatar, "", LocalStorage.Companion.getInstance(this).getUsername()
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
                content = binding.input.inputEditText.text.toString(),
                isFeedComment = isFeedComment
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

        val newReply = Comment(
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
        val commentWithReplies = com.uyscuti.sharedmodule.data.model.Comment(
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

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private suspend fun feedCommentLikeUnLike(commentId: String): Boolean {
        val TAG = "commentLikeUnLike"
        try {
            val response = retrofitInterface.apiService.likeUnLikeFeedComment(commentId)
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

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
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

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
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

//    private var shortPlayerFragment: OtherUserProfileShortsPlayerFragment? = null

    @SuppressLint("CommitTransaction")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun hideToolBar(event: HideToolBar) {
//        binding.toolbarLayout.visibility = View.GONE
//        binding.toolbar.visibility = View.GONE
//        binding.mainContainer.visibility = View.GONE

//        binding.otherUsersShortsPlayFragment.visibility = View.VISIBLE

        Log.d("hideToolBar", "hideToolBar: step 1")
        binding.otherUsersShortsPlayFragment.visibility = View.VISIBLE
        Log.d("hideToolBar", "hideToolBar: step 2")
//        try {
//            otherUserProfileShortsPlayerFragment = OtherUserProfileShortsPlayerFragment()
//            otherUserProfileShortsPlayerFragment.arguments = Bundle().apply {
//                putSerializable(
//                    UserProfileShortsPlayerActivity.CLICKED_SHORT,
//                    event.userShortEntity
//                )
//                putSerializable(UserProfileShortsPlayerActivity.SHORTS_LIST, event.shortsProfile)
//            }
//
//            val transaction = supportFragmentManager.beginTransaction()
//            transaction.replace(
//                R.id.other_users_shorts_play_fragment,
//                otherUserProfileShortsPlayerFragment
//            )
//            transaction.addToBackStack("shortPlayerFragment")
//            transaction.commit()
//
//            binding.otherUsersShortsPlayFragment.visibility = View.GONE
//        } catch (e: Exception) {
//            Log.e(TAG, "hideToolBar: ${e.message}")
//        }


        // Begin fragment transaction
//        val transaction = supportFragmentManager.beginTransaction()
//        Log.d("hideToolBar", "hideToolBar: step 3")
//        // Replace fragment in the container
//        transaction.replace(R.id.other_users_shorts_play_fragment, otherUserProfileShortsPlayerFragment)
//        Log.d("hideToolBar", "hideToolBar: step 4")
//        // Optional: add this transaction to the back stack
//        transaction.addToBackStack("shortPlayerFragment")
//        Log.d("hideToolBar", "hideToolBar: step 5")
//        // Commit the transaction
//        transaction.commit()
//        Log.d("hideToolBar", "hideToolBar: step 6")
//        shortPlayerFragment = OtherUserProfileShortsPlayerFragment()
//
//        supportFragmentManager.beginTransaction()
//            .replace(
//                R.id.other_users_shorts_play_fragment,
//                shortPlayerFragment!!
//            ) // Use the correct container ID
//            .addToBackStack(null) // Optional, to add to back stack
//            .commit()
//        otherUsersProfileViewModel.setOpenShortsPlayerFragment(true)
//        runOnUiThread {
//            val shortPlayerFragment = OtherUserProfileShortsPlayerFragment()
//            supportFragmentManager.beginTransaction()
//                .replace(
//                    R.id.other_users_shorts_play_fragment,
//                    shortPlayerFragment,
//                    "MY_FRAGMENT_TAG"
//                )
//                .addToBackStack("null")
//                .commit()
//        }
//        lifecycleScope.launch(Dispatchers.Main) {
//
//        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun businessCommentsClicked(event: BusinessCommentsClicked) {
//        binding.toolbarLayout.visibility = View.GONE
//        binding.toolbar.visibility = View.GONE
//        binding.mainContainer.visibility = View.GONE

//        binding.otherUsersShortsPlayFragment.visibility = View.VISIBLE

//        Log.d("businessCommentsClicked", "businessCommentsClicked: step 1 ${event.data}")

//    }
    //    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
//        super.onCreate(savedInstanceState, persistentState)
//    }
//    override fun onShortClick(
//        shortsProfile: ArrayList<UserShortsEntity>,
//        userShortEntity: UserShortsEntity
//    ) {
//        if (!supportFragmentManager.isStateSaved) {
//            Log.d(TAG, "onShortClick: state not saved")
//
//            val shortPlayerFragment = TrialFragment()
//            val transaction = supportFragmentManager.beginTransaction()
//
//            // Check if the fragment already exists
//            val existingFragment = supportFragmentManager.findFragmentById(binding.otherUsersShortsPlayFragment.id)
//
//            if (existingFragment == null || existingFragment.javaClass != TrialFragment::class.java) {
//                // Replace the fragment if it's not already present
//                Log.d(TAG, "not existingFragment")
//                transaction.replace(binding.otherUsersShortsPlayFragment.id, shortPlayerFragment)
//                transaction.setReorderingAllowed(true)
//                transaction.addToBackStack("shortPlayerFragment")
//                transaction.commit()
//            } else {
//                Log.d(TAG, "Fragment already exists: ${existingFragment.javaClass.simpleName}")
//            }
//        } else {
//            Log.d(TAG, "onShortClick: state saved")
//        }
//    }
    override fun onShortClick(
        shortsProfile: ArrayList<UserShortsEntity>,
        userShortEntity: UserShortsEntity
    ) {
//        binding.coordinatorLayout.visibility = View.GONE
        binding.otherUsersShortsPlayFragment.visibility = View.VISIBLE
        // Make sure to perform fragment transactions on the main thread
        if (Looper.myLooper() == Looper.getMainLooper()) {
            performFragmentTransaction()
        } else {
            // Post the transaction to the main thread if not on the main thread
            Handler(Looper.getMainLooper()).post { performFragmentTransaction() }
        }
    }

    private fun performFragmentTransaction() {
        if (!supportFragmentManager.isStateSaved) {
            Log.d(TAG, "onShortClick: state not saved")

            val shortPlayerFragment = TrialFragment()
            val transaction = supportFragmentManager.beginTransaction()

            // Check if the fragment already exists
            val existingFragment =
                supportFragmentManager.findFragmentById(binding.otherUsersShortsPlayFragment.id)

            if (existingFragment == null || existingFragment.javaClass != TrialFragment::class.java) {
                // Replace the fragment if it's not already present
                transaction.replace(binding.otherUsersShortsPlayFragment.id, shortPlayerFragment)
                transaction.setReorderingAllowed(true)
                transaction.addToBackStack("shortPlayerFragment")
                transaction.commit()
            } else {
                Log.d(TAG, "Fragment already exists: ${existingFragment.javaClass.simpleName}")
            }
        } else {
            Log.d(TAG, "onShortClick: state saved")

            // Optionally, use commitAllowingStateLoss() if necessary
            val shortPlayerFragment = TrialFragment()
            val transaction = supportFragmentManager.beginTransaction()

            transaction.replace(binding.otherUsersShortsPlayFragment.id, shortPlayerFragment)
            transaction.setReorderingAllowed(true)
            transaction.addToBackStack("shortPlayerFragment")
            transaction.commitAllowingStateLoss()
        }
    }

//
//    override fun onShortClick(
//        shortsProfile: ArrayList<UserShortsEntity>,
//        userShortEntity: UserShortsEntity
//    ) {
//        if (supportFragmentManager.backStackEntryCount > 0) {
//            Log.d("onShortClick", "onBackPressed: backStackEntryCount > 0 ")
//        } else {
//            Log.d("onShortClick", "onBackPressed: backStackEntryCount < 0 ")
//        }
//        binding.otherUsersShortsPlayFragment.visibility = View.VISIBLE
//
//        if (!supportFragmentManager.isStateSaved) {
//            Log.d(TAG, "onShortClick: state not saved")
//            val shortPlayerFragment = TrialFragment()
//            val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
//            transaction.replace(binding.otherUsersShortsPlayFragment.id, shortPlayerFragment)
//            transaction.setReorderingAllowed(true)
////
////            if (transaction.isAddToBackStackAllowed) {
////                Log.d(TAG, "isAddToBackStackAllowed")
////                transaction.addToBackStack("isAddToBackStackAllowed");
////            }else {
////                Log.d(TAG, "isAddToBackStackAllowed not allowed")
////            }
//
//            // Check if the transaction is not already on the back stack
//            val existingFragment =
//                supportFragmentManager.findFragmentById(binding.otherUsersShortsPlayFragment.id)
//            if (existingFragment == null || existingFragment !is TrialFragment) {
//                transaction.addToBackStack("shortPlayerFragment")
//                Log.d(TAG, "existingFragment")
//            } else {
//                Log.d(TAG, "not existingFragment")
//            }
//            transaction.commit();
//        } else {
//            Log.d(TAG, "onShortClick: state  saved")
//        }
//
//
//    }

    //        try {
//            otherUserProfileShortsPlayerFragment = OtherUserProfileShortsPlayerFragment()
//            otherUserProfileShortsPlayerFragment.arguments = Bundle().apply {
//                putSerializable(
//                    UserProfileShortsPlayerActivity.CLICKED_SHORT,
//                    userShortEntity
//                )
//                putSerializable(UserProfileShortsPlayerActivity.SHORTS_LIST, shortsProfile)
//            }
//
//            val transaction = supportFragmentManager.beginTransaction()
//            transaction.replace(
//                R.id.other_users_shorts_play_fragment,
//                otherUserProfileShortsPlayerFragment
//            )
//            transaction.addToBackStack("shortPlayerFragment")
//            transaction.commit()
//
//            binding.otherUsersShortsPlayFragment.visibility = View.GONE
//        } catch (e: Exception) {
//            Log.e(TAG, "hideToolBar: ${e.message}")
//        }
//        Log.d(TAG, "onShortClick: in other user profile")
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "onRestart: onRestart")
    }
}