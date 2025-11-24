package com.uyscuti.social.circuit


import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.media.AudioRecord
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
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
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
import androidx.annotation.RequiresExtension
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.loader.content.CursorLoader
import androidx.media3.common.C
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
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior.STATE_SCROLLED_DOWN
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior.STATE_SCROLLED_UP
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.Fragment_Original_Post_With_Repost_Inside
import com.uyscuti.social.call.repository.MainRepository
import com.uyscuti.social.call.service.MainServiceRepository
import com.uyscuti.social.chatsuit.messages.CommentsInput
import com.uyscuti.social.circuit.User_Interface.Log_In_And_Register.LoginActivity
import com.uyscuti.social.circuit.User_Interface.MyUserProfile.MyUserProfileAccount
import com.uyscuti.social.circuit.User_Interface.OtherImportantProfileThings.GifActivity
import com.uyscuti.social.circuit.User_Interface.OtherImportantProfileThings.SearchShortActivity
import com.uyscuti.social.circuit.User_Interface.OtherImportantProfileThings.SettingsActivity
import com.uyscuti.social.circuit.User_Interface.fragments.ChatFragment
import com.uyscuti.social.circuit.User_Interface.fragments.FeedFragment
import com.uyscuti.social.circuit.User_Interface.fragments.NotificationHostFragment
import com.uyscuti.social.circuit.User_Interface.fragments.ShotsFragment
import com.uyscuti.social.circuit.User_Interface.fragments.user_profile_fragments.UserProfileShortsPlayerFragment
import com.uyscuti.social.circuit.User_Interface.fragments.user_profile_fragments.UserProfileShortsPlayerFragment.Companion.CLICKED_SHORT
import com.uyscuti.social.circuit.User_Interface.fragments.user_profile_fragments.UserProfileShortsPlayerFragment.Companion.FROM_FAVORITE_FRAGMENT
import com.uyscuti.social.circuit.User_Interface.fragments.user_profile_fragments.UserProfileShortsPlayerFragment.Companion.SHORTS_LIST
import com.uyscuti.social.circuit.User_Interface.shorts.UniqueIdGenerator
import com.uyscuti.social.circuit.User_Interface.shorts.getFileSize
import com.uyscuti.social.circuit.User_Interface.uploads.AudioActivity
import com.uyscuti.social.circuit.User_Interface.uploads.CameraActivity
import com.uyscuti.social.circuit.User_Interface.uploads.VideosActivity
import com.uyscuti.social.circuit.adapter.CommentsRecyclerViewAdapter
import com.uyscuti.social.circuit.User_Interface.fragments.OnCommentsClickListener
import com.uyscuti.social.circuit.adapter.OnViewRepliesClickListener
import com.uyscuti.social.circuit.adapter.feed.FeedAdapter
import com.uyscuti.social.circuit.adapter.notifications.AdPaginatedAdapter
import com.uyscuti.social.circuit.callbacks.ChatSocketWorker
import com.uyscuti.social.circuit.callbacks.CombinedWorker
import com.uyscuti.social.circuit.calls.viewmodel.CallViewModel
import com.uyscuti.social.circuit.colorimagebottomnav.BottomNavigationView
import com.uyscuti.social.circuit.colorimagebottomnav.NavigationItem
import com.uyscuti.social.circuit.data.model.Dialog
import com.uyscuti.social.circuit.data.model.Message
import com.uyscuti.social.circuit.data.model.User
import com.uyscuti.social.circuit.data.model.shortsmodels.Comment
import com.uyscuti.social.circuit.data.model.shortsmodels.CommentReplyResults
import com.uyscuti.social.circuit.databinding.ActivityMainBinding
import com.uyscuti.social.circuit.interfaces.OnBackPressedListener
import com.uyscuti.social.circuit.model.AudioPlayerHandler
import com.uyscuti.social.circuit.model.CleanCache
import com.uyscuti.social.circuit.model.CommentAudioPlayerHandler
import com.uyscuti.social.circuit.model.FeedAdapterNotifyDatasetChanged
import com.uyscuti.social.circuit.model.FeedCommentClicked
import com.uyscuti.social.circuit.model.FeedDetailPage
import com.uyscuti.social.circuit.model.GoToFeedFragment
import com.uyscuti.social.circuit.model.GoToShortsFragment
import com.uyscuti.social.circuit.model.GoToUserProfileFragment
import com.uyscuti.social.circuit.model.GoToUserProfileFragment2
import com.uyscuti.social.circuit.model.GoToUserProfileShortsPlayerFragment
import com.uyscuti.social.circuit.model.HideAppBar
import com.uyscuti.social.circuit.model.HideBottomNav
import com.uyscuti.social.circuit.model.LikeComment
import com.uyscuti.social.circuit.model.LikeCommentReply
import com.uyscuti.social.circuit.model.PauseShort
import com.uyscuti.social.circuit.model.ProfileImageEvent
import com.uyscuti.social.circuit.model.ShortAdapterNotifyDatasetChanged
import com.uyscuti.social.circuit.model.ShortsCacheEvent
import com.uyscuti.social.circuit.model.ShortsFavoriteUnFavorite
import com.uyscuti.social.circuit.model.ShortsLikeUnLike2
import com.uyscuti.social.circuit.model.ShortsViewModel
import com.uyscuti.social.circuit.model.ShowAppBar
import com.uyscuti.social.circuit.model.ShowBottomNav
import com.uyscuti.social.circuit.model.ToggleReplyToTextView
import com.uyscuti.social.circuit.model.UserProfileShortsStartGet
import com.uyscuti.social.circuit.model.UserProfileShortsViewModel
import com.uyscuti.social.circuit.model.notifications_data_class.INotification
import com.uyscuti.social.circuit.presentation.DialogViewModel
import com.uyscuti.social.circuit.presentation.GroupDialogViewModel
import com.uyscuti.social.circuit.presentation.LikeUnLikeViewModel
import com.uyscuti.social.circuit.presentation.MainViewModel
import com.uyscuti.social.circuit.presentation.MessageViewModel
import com.uyscuti.social.circuit.service.VideoPreLoadingService
import com.uyscuti.social.circuit.utils.AndroidUtil.showToast
import com.uyscuti.social.circuit.utils.AudioDurationHelper.getFormattedDuration
import com.uyscuti.social.circuit.utils.AudioDurationHelper.reverseFormattedDuration
import com.uyscuti.social.circuit.utils.COMMENT_VIDEO_CODE
import com.uyscuti.social.circuit.utils.ChatManager
import com.uyscuti.social.circuit.utils.Constants
import com.uyscuti.social.circuit.utils.CustomAlertDialog
import com.uyscuti.social.circuit.utils.GIF_CODE
import com.uyscuti.social.circuit.utils.MongoDBTimeFormatter
import com.uyscuti.social.circuit.utils.NavigationController
import com.uyscuti.social.circuit.utils.PathUtil
import com.uyscuti.social.circuit.utils.R_CODE
import com.uyscuti.social.circuit.utils.Timer
import com.uyscuti.social.circuit.utils.TrimVideoUtils
import com.uyscuti.social.circuit.utils.audio_compressor.FFMPEG_AudioCompressor
import com.uyscuti.social.circuit.utils.audiomixer.AudioMixer
import com.uyscuti.social.circuit.utils.audiomixer.input.GeneralAudioInput
import com.uyscuti.social.circuit.utils.createMultipartBody
import com.uyscuti.social.circuit.utils.deleteFiled
import com.uyscuti.social.circuit.utils.deleteFiles
import com.uyscuti.social.circuit.utils.extractThumbnailFromVideo
import com.uyscuti.social.circuit.utils.fileType
import com.uyscuti.social.circuit.utils.formatFileSize
import com.uyscuti.social.circuit.utils.generateRandomId
import com.uyscuti.social.circuit.utils.getFileNameFromLocalPath
import com.uyscuti.social.circuit.utils.getNavigationController
import com.uyscuti.social.circuit.utils.getOutputFilePath
import com.uyscuti.social.circuit.utils.isFileExists
import com.uyscuti.social.circuit.utils.isFileSizeGreaterThan2MB
import com.uyscuti.social.circuit.utils.waveformseekbar.WaveformSeekBar
import com.uyscuti.social.circuit.viewmodels.GetShortsByUsernameViewModel
import com.uyscuti.social.circuit.viewmodels.comments.CommentsViewModel
import com.uyscuti.social.circuit.viewmodels.comments.RoomCommentFilesViewModel
import com.uyscuti.social.circuit.viewmodels.comments.RoomCommentReplyViewModel
import com.uyscuti.social.circuit.viewmodels.comments.RoomCommentsViewModel
import com.uyscuti.social.circuit.viewmodels.comments.ShortCommentReplyViewModel
import com.uyscuti.social.circuit.viewmodels.comments.ShortCommentsViewModel
import com.uyscuti.social.circuit.viewmodels.comments.ShortCommentsViewModel.CommentSubmissionStatus
import com.uyscuti.social.circuit.viewmodels.feed.FeedLiveDataViewModel
import com.uyscuti.social.circuit.viewmodels.feed.GetFeedViewModel
import com.uyscuti.social.circuit.viewmodels.notificationViewModel.NotificationViewModel
import com.uyscuti.social.compressor.CompressionListener
import com.uyscuti.social.compressor.VideoCompressor
import com.uyscuti.social.compressor.VideoQuality
import com.uyscuti.social.compressor.config.*
import com.uyscuti.social.core.common.data.room.database.ChatDatabase
import com.uyscuti.social.core.common.data.room.entity.CallLogEntity
import com.uyscuti.social.core.common.data.room.entity.CommentsFilesEntity
import com.uyscuti.social.core.common.data.room.entity.GroupDialogEntity
import com.uyscuti.social.core.common.data.room.entity.MessageEntity
import com.uyscuti.social.core.common.data.room.entity.ProfileEntity
import com.uyscuti.social.core.common.data.room.entity.ShortCommentEntity
import com.uyscuti.social.core.common.data.room.entity.ShortCommentReply
import com.uyscuti.social.core.common.data.room.entity.ShortsEntity
import com.uyscuti.social.core.common.data.room.entity.UserEntity
import com.uyscuti.social.core.common.data.room.entity.UserShortsEntity
import com.uyscuti.social.core.common.data.room.repository.ProfileRepository
import com.uyscuti.social.core.pushnotifications.socket.chatsocket.CoreChatSocketClient
import com.uyscuti.social.core.pushnotifications.socket.chatsocket.social.FlashNotificationEvent
import com.uyscuti.social.core.service.DirectReplyService
import com.uyscuti.social.medialoader.DefaultConfigFactory
import com.uyscuti.social.medialoader.DownloadManager
import com.uyscuti.social.medialoader.MediaLoader
import com.uyscuti.social.medialoader.MediaLoaderConfig
import com.uyscuti.social.medialoader.data.file.naming.Md5FileNameCreator
import com.uyscuti.social.network.api.response.comment.allcomments.Account
import com.uyscuti.social.network.api.response.comment.allcomments.Author
import com.uyscuti.social.network.api.response.comment.allcomments.Avatar
import com.uyscuti.social.network.api.response.comment.allcomments.CommentFiles
import com.uyscuti.social.network.api.response.commentreply.allreplies.AllCommentReplies
import com.uyscuti.social.network.api.response.getallshorts.Post
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.interfaces.DirectReplyListener
import com.uyscuti.social.network.utils.LocalStorage
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.twitter.TwitterEmojiProvider
import dagger.hilt.android.AndroidEntryPoint
import id.zelory.compressor.Compressor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.poi.hwpf.HWPFDocument
import org.apache.poi.hwpf.usermodel.Range
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.HttpException
import retrofit2.Response
import ru.nikartm.support.ImageBadgeView
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.Math.sqrt
import java.net.HttpURLConnection
import java.net.URL
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Collections.emptyList
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.properties.Delegates
import kotlin.random.Random


private const val TAG = "MainActivity"


class FollowingManager(private val context: Context) {

    private val localStorage = LocalStorage.getInstance(context)
    private var retrofitInstance: RetrofitInstance? = null

    init {
        try {
            retrofitInstance = RetrofitInstance(localStorage, context)
        } catch (e: Exception) {
            Log.e("FollowingManager", "Failed to initialize Retrofit", e)
        }
    }

    suspend fun loadFollowingList(): Set<String> {
        // First, load from cache
        val cachedList = localStorage.getFollowingList()
        Log.d("FollowingManager", "Loaded ${cachedList.size} users from cache")

        // Update adapter cache immediately
        FeedAdapter.setCachedFollowingList(cachedList)

        // Then fetch fresh data from server in background
        fetchFollowingListFromServer()

        return cachedList
    }

    private suspend fun fetchFollowingListFromServer() {
        withContext(Dispatchers.IO) {
            try {
                val currentUsername = localStorage.getUsername() ?: return@withContext
                val followingUserIds = mutableSetOf<String>()
                var currentPage = 1
                var hasMorePages = true

                Log.d("FollowingManager", "Fetching following list from server for: $currentUsername")

                while (hasMorePages) {
                    val response = retrofitInstance?.apiService?.getOtherUserFollowing(
                        username = currentUsername,
                        page = currentPage,
                        limit = 50
                    )

                    if (response?.isSuccessful == true) {
                        val users = response.body()?.data ?: emptyList()

                        // Extract user IDs
                        users.forEach { user ->
                            user._id?.let { followingUserIds.add(it) }
                        }

                        Log.d("FollowingManager", "Page $currentPage: Found ${users.size} users, Total: ${followingUserIds.size}")

                        // Check if there are more pages
                        hasMorePages = users.size >= 50
                        currentPage++

                        if (!hasMorePages) {
                            Log.d("FollowingManager", "Completed fetching all pages. Total: ${followingUserIds.size} users")
                        }
                    } else {
                        Log.e("FollowingManager", "Failed to fetch page $currentPage: ${response?.code()}")
                        hasMorePages = false
                    }
                }

                // Save to local storage
                withContext(Dispatchers.Main) {
                    if (followingUserIds.isNotEmpty()) {
                        val json = Gson().toJson(followingUserIds.toList())
                        localStorage.saveFollowingList(json)
                        FeedAdapter.setCachedFollowingList(followingUserIds)
                        Log.d("FollowingManager", "Saved ${followingUserIds.size} following users")
                    }
                }

            } catch (e: Exception) {
                Log.e("FollowingManager", "Error fetching following list", e)
            }
        }
    }

    fun addToFollowing(userId: String) {
        val currentList = localStorage.getFollowingList().toMutableSet()
        currentList.add(userId)
        val json = Gson().toJson(currentList.toList())
        localStorage.saveFollowingList(json)
        FeedAdapter.setCachedFollowingList(currentList)
        Log.d("FollowingManager", "Added user $userId to following list")
    }

    fun removeFromFollowing(userId: String) {
        val currentList = localStorage.getFollowingList().toMutableSet()
        currentList.remove(userId)
        val json = Gson().toJson(currentList.toList())
        localStorage.saveFollowingList(json)
        FeedAdapter.setCachedFollowingList(currentList)
        Log.d("FollowingManager", "Removed user $userId from following list")
    }
}

@UnstableApi
@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationController, DirectReplyListener,
    CommentsInput.InputListener, CommentsInput.EmojiListener, CommentsInput.VoiceListener,
    CommentsInput.GifListener,
    CommentsInput.AttachmentsListener, OnCommentsClickListener, OnViewRepliesClickListener,
    Timer.OnTimeTickListener, CustomAlertDialog.DialogCallback {


    // PERMISSIONS

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.READ_MEDIA_IMAGES
    )

    private lateinit var followingManager: FollowingManager
    private var permissionGranted = false
    private var permissionGranted2 = false
    private var permissionGranted3 = false
    private val REQUEST_CODE = 2024
    private val IMAGES_REQUEST_CODE = 2023
    private val READ_EXTERNAL_STORAGE_REQUEST_CODE = 101
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200


   // VIEW BINDING & UI COMPONENTS

    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var feedAdapter: FeedAdapter
    private lateinit var emojiPopup: EmojiPopup
    private lateinit var inputMethodManager: InputMethodManager
    private lateinit var waveformScrollView: HorizontalScrollView
    private lateinit var waveDotsContainer: LinearLayout
    private var actionMode: ActionMode? = null
    private var customDialog: CustomAlertDialog? = null


   // NAVIGATION

    private lateinit var item: NavigationItem
    private lateinit var item1: NavigationItem
    private lateinit var item2: NavigationItem
    private lateinit var item3: NavigationItem
    private lateinit var item4: NavigationItem
    private var lastFragmentId: String? = null
    private val onBackPressedListeners: MutableList<OnBackPressedListener> = mutableListOf()


  // DEPENDENCY INJECTIONS

    @Inject
    lateinit var retrofitInterface: RetrofitInstance

    @Inject
    lateinit var coreChatSocketClient: CoreChatSocketClient

    @Inject
    lateinit var chatManager: ChatManager

    @Inject
    lateinit var mainRepository: MainRepository

    @Inject
    lateinit var chatDatabase: ChatDatabase

    @Inject
    lateinit var localStorage: LocalStorage

    @Inject
    lateinit var mainServiceRepository: MainServiceRepository


    // REPOSITORIES & PREFERENCES

    private lateinit var myProfileRepository: ProfileRepository
    private lateinit var settings: SharedPreferences
    private val PREFS_NAME = "LocalSettings"
    private val WORKER_TAG = "flash_socket_worker"


    // VIEW MODELS

    private val messageViewModel: MessageViewModel by viewModels()
    private val dialogViewModel: DialogViewModel by viewModels()
    private val groupDialogViewModel: GroupDialogViewModel by viewModels()
    private val viewModel: UserProfileShortsViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()
    private val callViewModel: CallViewModel by viewModels()
    private val feedLiveDataViewModel: FeedLiveDataViewModel by viewModels()
    private lateinit var shortsViewModel: ShortsViewModel
    private lateinit var feedViewModel: GetFeedViewModel
    private lateinit var userProfileShortsViewModel: GetShortsByUsernameViewModel
    private lateinit var userShortsFragment: UserProfileShortsViewModel
    private lateinit var shortsCommentViewModel: RoomCommentsViewModel
    private lateinit var commentFilesViewModel: RoomCommentFilesViewModel
    private lateinit var commentsViewModel: ShortCommentsViewModel
    private lateinit var commentsReplyViewModel: ShortCommentReplyViewModel
    private lateinit var roomCommentReplyViewModel: RoomCommentReplyViewModel
    private lateinit var commentViewModel: CommentsViewModel
    private lateinit var myViewModel: LikeUnLikeViewModel
    private lateinit var notificationViewModel: NotificationViewModel


    // AUDIO RECORDING & PLAYBACK

    private lateinit var outputFile: String
    private var mediaRecorder: MediaRecorder? = null
    private lateinit var audioRecorder: AudioRecord
    private var audioRecord: AudioRecord? = null
    private var amps = 0
    private var isRecording = false
    private var isPaused = false
    private var isListeningToAudio = false
    private lateinit var amplitudes: ArrayList<Float>
    private var outputVnFile: String = ""
    private val recordedAudioFiles = mutableListOf<String>()
    private var recordingStartTime = 0L
    private var recordingElapsedTime = 0L
    private var totalRecordedDuration = 0L

    // Playback
    private var exoPlayer: ExoPlayer? = null
    private var player: MediaPlayer? = null
    private val simpleCache: SimpleCache = FlashApplication.cache
    private lateinit var httpDataSourceFactory: HttpDataSource.Factory
    private lateinit var defaultDataSourceFactory: DefaultDataSourceFactory
    private lateinit var cacheDataSourceFactory: CacheDataSource.Factory
    private val playbackStateListener: Player.Listener = playbackStateListener()
    private var isAudioVNPlaying = false
    private var isAudioVNPaused = false
    private var isVnResuming = false
    private var vnRecordAudioPlaying = false
    private var vnRecordProgress = 0
    private var playbackTimerRunnable: Runnable? = null

    // Voice Note State
    private var voiceNoteState = VoiceNoteState.IDLE
    private var wasPaused = false
    private var firstTimeSendVn = false
    private var mixingCompleted = false
    private var isDurationOnPause = false
    private var isOnRecordDurationOnPause = false


// MEDIA PICKERS & LAUNCHERS

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var audioPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var videoPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var docsPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var openFilePicker: ActivityResultLauncher<Intent>

    private val getDocumentContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    // Handle the selected document URI
                    handleDocumentUri(uri)
                }
            }
        }


    // COMMENTS & SOCIAL FEATURES

    private var listOfReplies = mutableListOf<com.uyscuti.social.circuit.data.model.Comment>()
    private var adapter: CommentsRecyclerViewAdapter? = null
    private var isReply = false
    private lateinit var postId: String
    private lateinit var commentId: String
    private var commentCount by Delegates.notNull<Int>()
    private var shortToComment: ShortsEntity? = null
    private var data: com.uyscuti.social.circuit.data.model.Comment? = null
    private var position: Int = 0
    private val timeFormatter = MongoDBTimeFormatter()
    private lateinit var directReplyListener: DirectReplyListener


    // UI STATE & INTERACTION

    private var emojiShowing = false
    private var backPressCount = 0
    private val doubleBackPressThreshold = 3
    var count = 0
    private lateinit var myId: String
    private var sending = false


   // WAVEFORM VISUALIZATION

    private val waveBars = mutableListOf<View>()
    private var waveBarCount = 0
    private val maxWaveBars = 100
    private var waveProgress = 0f
    private var seekBarProgress = 0f
    private val waveHandler = Handler()


    // TIMERS & HANDLERS

    private lateinit var timer: Timer
    private var currentHandler: Handler? = null
    private val timerHandler = Handler(Looper.getMainLooper())

    private val waveRunnable = object : Runnable {
        override fun run() {
            if (!isDurationOnPause) {
                val currentPosition = exoPlayer?.currentPosition?.toFloat()!!
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

    internal enum class VoiceNoteState {
        IDLE,
        RECORDING,
        PLAYING,
        PAUSED
    }

    @SuppressLint("DefaultLocale")
    private fun updateRecordingTimer() {
        timerHandler.post(object : Runnable {
            override fun run() {
                if (isRecording && !isPaused) {
                    val currentTime = System.currentTimeMillis()
                    val elapsed = recordingElapsedTime + (currentTime - recordingStartTime)

                    val seconds = (elapsed / 1000) % 60
                    val minutes = (elapsed / 1000) / 60

                    val formatted = String.format("%02d:%02d", minutes, seconds)
                    binding.recordingTimerTv.text = formatted

                    timerHandler.postDelayed(this, 100) // Update every 100ms
                }
            }
        })
    }

    @SuppressLint("DefaultLocale")
    private fun updatePlaybackTimer() {
        // Remove any existing callbacks first
        playbackTimerRunnable?.let { timerHandler.removeCallbacks(it) }

        playbackTimerRunnable = object : Runnable {
            override fun run() {
                if (isAudioVNPlaying && player != null) {
                    try {
                        val currentPosition = player?.currentPosition ?: 0
                        val currentMinutes = (currentPosition / 1000) / 60
                        val currentSeconds = (currentPosition / 1000) % 60
                        binding.pausedTimerTv.text = String.format("%02d:%02d", currentMinutes, currentSeconds)
                        timerHandler.postDelayed(this, 100)
                    } catch (e: Exception) {
                        Log.e("PlaybackTimer", "Error updating timer: ${e.message}")
                    }
                }
            }
        }
        timerHandler.post(playbackTimerRunnable!!)
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

            outputFile = getOutputFilePath("rec")
            outputVnFile = getOutputFilePath("mix")
            wasPaused = false

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setOutputFile(outputFile)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                prepare()
                start()
            }

            isRecording = true
            isPaused = false
            isListeningToAudio = true

            // Reset and start timer
            recordingStartTime = System.currentTimeMillis()
            recordingElapsedTime = 0L
            updateRecordingTimer() // Start the timer

            binding.recordVN.setImageResource(R.drawable.baseline_pause_white_24)
            binding.sendVN.setBackgroundResource(R.drawable.ic_ripple)
            binding.deleteVN.setBackgroundResource(R.drawable.ic_ripple)

            binding.recordingLayout.visibility = View.VISIBLE
            updateVoiceNoteUserInterfaceState(VoiceNoteState.RECORDING)

            binding.deleteVN.isClickable = true
            binding.sendVN.isClickable = true
            recordedAudioFiles.add(outputFile)

            // Initialize waveform with dots
            initializeDottedWaveform()

            // Start audio listening in background thread
            Thread {
                listenToAudio()
            }.start()

            Log.d("VNFile", outputFile)
        } catch (e: Exception) {
            Log.d("VNFile", "Failed to record: ${e.message}")
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun pauseRecording() {
        if (isRecording && !isPaused) {
            try {
                isListeningToAudio = false

                // Calculate elapsed time before stopping
                val currentTime = System.currentTimeMillis()
                recordingElapsedTime += (currentTime - recordingStartTime)

                mediaRecorder?.let { recorder ->
                    try {
                        recorder.stop()
                        recorder.release()
                    } catch (e: Exception) {
                        Log.e("pauseRecording", "Error: $e")
                    }
                }
                mediaRecorder = null
            } catch (e: Exception) {
                Log.d("pauseRecording", "Error: $e")
                e.printStackTrace()
            }

            isPaused = true

            // Stop the recording timer
            timerHandler.removeCallbacksAndMessages(null)

            // Update both timers to show the current recorded duration
            runOnUiThread {
                val seconds = (recordingElapsedTime / 1000) % 60
                val minutes = (recordingElapsedTime / 1000) / 60
                val formatted = String.format("%02d:%02d", minutes, seconds)
                binding.recordingTimerTv.text = formatted
                binding.pausedTimerTv.text = formatted
            }

            updateVoiceNoteUserInterfaceState(VoiceNoteState.PAUSED)
            binding.recordVN.setImageResource(R.drawable.mic_2)

            Log.d("pauseRecording", "Recordings: ${recordedAudioFiles.size}")
            mixVoiceNote()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun resumeRecording() {
        if (isPaused) {
            // Create new recording file for this segment
            outputFile = getOutputFilePath("rec")

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setOutputFile(outputFile)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                prepare()
                start()
            }

            isPaused = false
            isListeningToAudio = true
            isRecording = true

            // Resume timer from where it left off
            recordingStartTime = System.currentTimeMillis()
            updateRecordingTimer()

            binding.playVNRecorded.visibility = View.GONE
            binding.recordingTimerTv.visibility = View.VISIBLE

            binding.playVnAudioBtn.setImageResource(R.drawable.play_svgrepo_com)
            binding.recordVN.setImageResource(R.drawable.baseline_pause_white_24)

            updateVoiceNoteUserInterfaceState(VoiceNoteState.RECORDING)

            recordedAudioFiles.add(outputFile)

            // Resume audio listening
            Thread {
                listenToAudio()
            }.start()
        }
    }

    private fun startPlaying(vnAudio: String) {
        EventBus.getDefault().post(PauseShort(true))
        isAudioVNPlaying = true
        vnRecordAudioPlaying = true

        updateVoiceNoteUserInterfaceState(VoiceNoteState.PLAYING)

        isOnRecordDurationOnPause = false

        if (isAudioVNPaused) {
            if (vnRecordProgress != 0) {
                player?.seekTo(vnRecordProgress)
            }
            player?.start()
        } else {
            player = MediaPlayer().apply {
                try {
                    setDataSource(vnAudio)
                    prepare()
                    totalRecordedDuration = duration.toLong()
                    if (vnRecordProgress != 0) {
                        seekTo(vnRecordProgress)
                    }
                    start()
                    setOnCompletionListener {
                        isAudioVNPaused = false
                        isAudioVNPlaying = false
                        stopPlayingOnCompletion()
                    }
                } catch (e: IOException) {
                    Log.e("MediaRecorder", "prepare() failed")
                }
            }
        }

        animatePlaybackWaves()
        updatePlaybackTimer() // This will now work correctly
    }

    private fun deleteRecording() {
        val TAG = "Recording"
        try {
            isListeningToAudio = false

            // Stop all timers
            timerHandler.removeCallbacksAndMessages(null)
            playbackTimerRunnable?.let { timerHandler.removeCallbacks(it) }

            mediaRecorder?.apply {
                try {
                    stop()
                    release()
                } catch (e: Exception) {
                    Log.e(TAG, "Error stopping recorder: $e")
                }
            }
            mediaRecorder = null

            isRecording = false
            isPaused = false
            isAudioVNPlaying = false

            // Reset timer variables
            recordingStartTime = 0L
            recordingElapsedTime = 0L

            binding.recordVN.setImageResource(R.drawable.mic_2)
            binding.sendVN.setBackgroundResource(R.drawable.ic_ripple_disabled)
            binding.sendVN.isClickable = false

            updateVoiceNoteUserInterfaceState(VoiceNoteState.IDLE)

            binding.recordingTimerTv.text = "00:00"
            binding.pausedTimerTv.text = "00:00"

            Log.d(TAG, "Recordings deleted: ${recordedAudioFiles.size}")
            deleteVn()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "Error deleting recording: $e")
        }
    }

    private fun stopPlaybackTimerRunnable() {
        playbackTimerRunnable?.let { timerHandler.removeCallbacks(it) }
        playbackTimerRunnable = null
    }

    private fun updateVoiceNoteUserInterfaceState(newState: VoiceNoteState) {
        voiceNoteState = newState

        when (newState) {
            VoiceNoteState.RECORDING -> {
                binding.recordingTimerTv.visibility = View.VISIBLE
                binding.playVNRecorded.visibility = View.GONE
                binding.waveformScrollView.visibility = View.VISIBLE
                binding.waveDotsContainer.visibility = View.VISIBLE
            }

            VoiceNoteState.PLAYING -> {
                binding.recordingTimerTv.visibility = View.GONE
                binding.playVNRecorded.visibility = View.VISIBLE
                binding.playVnAudioBtn.setImageResource(R.drawable.baseline_pause_black)
                binding.waveformScrollView.visibility = View.VISIBLE
                binding.waveDotsContainer.visibility = View.VISIBLE
            }

            VoiceNoteState.PAUSED -> {
                binding.recordingTimerTv.visibility = View.GONE
                binding.playVNRecorded.visibility = View.VISIBLE
                binding.playVnAudioBtn.setImageResource(R.drawable.play_svgrepo_com)
                binding.waveformScrollView.visibility = View.VISIBLE
                binding.waveDotsContainer.visibility = View.VISIBLE

                // Scroll to left to show full waveform when paused
                binding.waveformScrollView.post {
                    binding.waveformScrollView.scrollTo(0, 0)
                }
            }

            VoiceNoteState.IDLE -> {
                binding.recordingLayout.visibility = View.GONE
                clearWaveform()
            }
        }
    }

    @SuppressLint("DefaultLocale")
    override fun onTimerTick(duration: String) {
        // During recording, show increasing time in MM:SS format
        runOnUiThread {
            val parts = duration.split(":")
            val formatted = if (parts.size >= 2) {
                String.format("%02d:%02d",
                    parts[0].toIntOrNull() ?: 0,
                    parts[1].toIntOrNull() ?: 0)
            } else {
                "00:00"
            }
            binding.recordingTimerTv.text = formatted
            if (voiceNoteState == VoiceNoteState.PAUSED) {
                binding.pausedTimerTv.text = formatted
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun pauseVn(progress: Int) {
        val scrollAnimator = binding.waveformScrollView.tag as? ValueAnimator
        scrollAnimator?.cancel()

        player?.pause()
        player?.seekTo(progress)

        isAudioVNPlaying = false
        isAudioVNPaused = true

        stopPlaybackTimerRunnable()

        // Stop animations but keep waveforms visible
        waveBars.forEach { bar ->
            (bar.tag as? ObjectAnimator)?.cancel()
            val storedHeight = bar.tag as? Float ?: 1.0f
            bar.scaleY = storedHeight
        }

        // Show current playback position
        val currentMinutes = (progress / 1000) / 60
        val currentSeconds = (progress / 1000) % 60
        binding.pausedTimerTv.text = String.format("%02d:%02d", currentMinutes, currentSeconds)

        updateVoiceNoteUserInterfaceState(VoiceNoteState.PAUSED)
    }

    @SuppressLint("DefaultLocale")
    private fun stopPlayingOnCompletion() {
        val scrollAnimator = binding.waveformScrollView.tag as? ValueAnimator
        scrollAnimator?.cancel()

        val totalDuration = player?.duration ?: 0

        player?.release()
        player = null

        isAudioVNPlaying = false
        isAudioVNPaused = false
        vnRecordAudioPlaying = false

        stopPlaybackTimerRunnable()
        stopWaveDotsAnimation()

        // Return to paused state showing total duration and PLAY icon
        updateVoiceNoteUserInterfaceState(VoiceNoteState.PAUSED)

        binding.playVnAudioBtn.setImageResource(R.drawable.play_svgrepo_com)

        val totalMinutes = (totalDuration / 1000) / 60
        val totalSeconds = (totalDuration / 1000) % 60
        binding.pausedTimerTv.text = String.format("%02d:%02d", totalMinutes, totalSeconds)

        vnRecordProgress = 0

        // Scroll back to start
        binding.waveformScrollView.post {
            binding.waveformScrollView.scrollTo(0, 0)
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private fun stopRecordingVoiceNote() {
        val TAG = "StopRecording"
        try {



            // Stop media recorder
            if (mediaRecorder != null) {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
                mediaRecorder = null
            }

            isRecording = false
            isPaused = false
            stopWaveDotsAnimation()
            binding.recordingLayout.visibility = View.GONE
            binding.recordingTimerTv?.text = "00:00"
            binding.recordVN?.setImageResource(com.uyscuti.social.call.R.drawable.ic_mic_on)
            binding.sendVN?.setBackgroundResource(R.drawable.ic_ripple_disabled)
            binding.sendVN?.isClickable = false
            timer.stop()

            if (player?.isPlaying == true) {
                stopPlaying()
            }

            Log.d(TAG, "stopRecording: recorded files size ${recordedAudioFiles.size}")

            // Select appropriate audio file
            val audioFilePath = if (mixingCompleted && File(outputVnFile).exists()) {
                Log.d(TAG, "Using mixed audio file: $outputVnFile")
                outputVnFile
            } else {
                Log.d(TAG, "Using single recording file: $outputFile")
                outputFile
            }

            val file = File(audioFilePath)
            if (!file.exists()) {
                Log.e(TAG, "Audio file not found: $audioFilePath")
                Toast.makeText(this, "Voice note file not found", Toast.LENGTH_SHORT).show()
                return
            }

            // Get duration and filename
            val durationString = getFormattedDuration(audioFilePath)
            val fileName = file.name

            Log.d(TAG, "Voice note prepared - File: $fileName, Duration: $durationString, Path: $audioFilePath")

            // Check file size and compress if needed
            val fileSizeInBytes = file.length()
            val fileSizeInKB = fileSizeInBytes / 1024
            val fileSizeInMB = fileSizeInKB / 1024

            Log.d(TAG, "Voice note file size: $fileSizeInKB KB, $fileSizeInMB MB")

            if (fileSizeInMB > 2) {
                Log.d(TAG, "Voice note needs compression")
                val outputFileName = "compressed_audio_${System.currentTimeMillis()}.mp3"
                val outputFilePath = File(cacheDir, outputFileName)

                lifecycleScope.launch(Dispatchers.IO) {
                    // First, add to UI and Room DB with original file
                    withContext(Dispatchers.Main) {
                        if (!isReply) {
                            Log.d(TAG, "Uploading VN comment (will be compressed)")
                            uploadVnComment(
                                vnToUpload = audioFilePath,
                                fileName = fileName,
                                durationString = durationString,
                                fileType = "vnAudio",
                                update = false,
                                placeholder = false
                            )
                        } else {
                            Log.d(TAG, "Uploading reply VN comment (will be compressed)")
                            uploadReplyVnComment(
                                audioFilePath,
                                fileName,
                                durationString,
                                fileType = "vnAudio",
                                update = false,
                                placeholder = false
                            )
                        }
                    }

                    // Compress audio in background
                    val compressor = FFMPEG_AudioCompressor()
                    val isCompressionSuccessful = compressor.compress(audioFilePath, outputFilePath.absolutePath)

                    if (isCompressionSuccessful) {
                        Log.d(TAG, "Voice note compression successful")
                        val compressedSizeInBytes = outputFilePath.length()
                        val compressedSizeInKB = compressedSizeInBytes / 1024
                        val compressedSizeInMB = compressedSizeInKB / 1024
                        Log.d(TAG, "Compressed file size: $compressedSizeInKB KB, $compressedSizeInMB MB")

                        // Update with compressed file
                        withContext(Dispatchers.Main) {
                            if (!isReply) {
                                uploadVnComment(
                                    vnToUpload = outputFilePath.absolutePath,
                                    fileName = fileName,
                                    durationString = durationString,
                                    fileType = "vnAudio",
                                    update = true,
                                    placeholder = false
                                )
                            } else {
                                uploadReplyVnComment(
                                    outputFilePath.absolutePath,
                                    fileName,
                                    durationString,
                                    fileType = "vnAudio",
                                    update = true,
                                    placeholder = false
                                )
                            }

                            // NOW trigger the actual upload to server after compression
                            addCommentVN()
                        }
                    } else {
                        Log.e(TAG, "Voice note compression failed - using original file")
                        withContext(Dispatchers.Main) {
                            // Trigger upload with original file
                            addCommentVN()
                        }
                    }
                }
            } else {
                // File is small enough, upload directly
                Log.d(TAG, "Voice note doesn't need compression, uploading directly")
                if (isReply) {
                    uploadReplyVnComment(
                        audioFilePath,
                        fileName,
                        durationString,
                        "vnAudio",
                        update = false,
                        placeholder = false
                    )
                } else {
                    uploadVnComment(
                        audioFilePath,
                        fileName,
                        durationString,
                        "vnAudio",
                        update = false,
                        placeholder = false
                    )
                }

                // Trigger the actual upload to server
                addCommentVN()
            }

            // Hide VN recording UI, keep comment section visible
            binding.VNLayout.visibility = View.GONE

            // Clean up recording state
            wasPaused = false
            mixingCompleted = false
            recordedAudioFiles.clear()
            sending = false

            Log.d(TAG, "Voice note comment processing completed")

        } catch (e: Exception) {
            Log.e(TAG, "Error in stopRecording: ${e.message}", e)
            Toast.makeText(this, "", Toast.LENGTH_SHORT).show()
            sending = false
        }
    }

    private fun mixVoiceNote() {
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
                override fun onProgress(progress: Double) {}

                override fun onEnd() {
                    runOnUiThread {
                        audioMixer.release()
                        mixingCompleted = true
                        val file = File(outputVnFile)
                        Log.d(TAG, "onEnd: output vn file exists ${file.exists()}")
                        Log.d(TAG, "onEnd: media muxed success")

                        binding.waveformScrollView.visibility = View.VISIBLE
                        binding.waveDotsContainer.visibility = View.VISIBLE
                        binding.wave.visibility = View.GONE

                        binding.playVnAudioBtn.setOnClickListener {
                            Log.d("playVnAudioBtn", "onEnd: play vn button clicked")
                            when {
                                !isAudioVNPlaying -> {
                                    Log.d("playVnAudioBtn", "play vn")
                                    startPlaying(outputVnFile)
                                }
                                else -> {
                                    Log.d("playVnAudioBtn", "pause VN")
                                    vnRecordAudioPlaying = true
                                    val currentProgress = player?.currentPosition ?: vnRecordProgress
                                    vnRecordProgress = currentProgress
                                    pauseVn(currentProgress)
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

    private fun listenToAudio() {
        try {
            val minBufferSize = AudioRecord.getMinBufferSize(
                44100,
                android.media.AudioFormat.CHANNEL_IN_MONO,
                android.media.AudioFormat.ENCODING_PCM_16BIT
            )

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    REQUEST_RECORD_AUDIO_PERMISSION
                )
                return
            }

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                44100,
                android.media.AudioFormat.CHANNEL_IN_MONO,
                android.media.AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize * 2
            )

            audioRecord?.startRecording()
            val buffer = ShortArray(minBufferSize)

            while (isListeningToAudio && isRecording) {
                val readSize = audioRecord?.read(buffer, 0, minBufferSize) ?: 0

                if (readSize > 0) {
                    // Calculate RMS (Root Mean Square) for better amplitude detection
                    var sum = 0.0
                    for (i in 0 until readSize) {
                        sum += (buffer[i].toDouble() * buffer[i].toDouble())
                    }
                    val rms = sqrt(sum / readSize)

                    // Normalize amplitude to 0-1 range (adjust 5000.0 for sensitivity)
                    val normalizedAmplitude = (rms / 5000.0).coerceIn(0.0, 1.0).toFloat()

                    runOnUiThread {
                        if (normalizedAmplitude > 0.05f) { // Sound detected threshold
                            // Map amplitude to height multiplier (0.3 to 2.5)
                            val heightMultiplier = 0.3f + (normalizedAmplitude * 2.2f)
                            addWaveBarForSound(heightMultiplier)
                        } else { // No sound or very quiet
                            addIdleDottedBarAtEnd()
                        }
                        scrollToRight()
                    }
                }

                Thread.sleep(50) // Update every 50ms for smooth animation
            }

            audioRecord?.release()
            audioRecord = null
        } catch (e: Exception) {
            Log.e("ListenToAudio", "Error: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun addWaveBarForSound(heightMultiplier: Float) {
        val bar = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                dpToPx(4), // 4dp width
                dpToPx(48) // 48dp max height
            ).apply {
                marginEnd = dpToPx(6) // 6dp spacing between bars
                gravity = android.view.Gravity.CENTER_VERTICAL
            }
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(Color.parseColor("#2563EB")) // Blue color
                cornerRadius = dpToPx(2).toFloat() // Rounded corners
            }
            // Apply height multiplier with clamping
            scaleY = heightMultiplier.coerceIn(0.2f, 2.5f)
            alpha = 1.0f
            tag = heightMultiplier // Store original height
        }

        binding.waveDotsContainer.addView(bar)
        waveBars.add(bar)

        // Remove old bars from START (left side) if exceeding limit
        if (waveBars.size > maxWaveBars) {
            binding.waveDotsContainer.removeViewAt(0)
            waveBars.removeAt(0)
        }

        scrollToRight()
    }

    private fun addIdleDottedBarAtEnd() {
        val bar = View(this).apply {
            val dotSize = dpToPx(5) // 5dp circular dot
            layoutParams = LinearLayout.LayoutParams(
                dotSize,
                dotSize
            ).apply {
                marginEnd = dpToPx(3) // 3dp spacing between dots
                gravity = android.view.Gravity.CENTER_VERTICAL
            }

            // Create circular dot with blue color
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#2563EB")) // Same blue as bars
            }

            scaleY = 1.0f
            alpha = 1.0f
            tag = "idle_dot" // Mark as idle dot
        }

        binding.waveDotsContainer.addView(bar)
        waveBars.add(bar)

        // Remove old bars from START (left side) if exceeding limit
        if (waveBars.size > maxWaveBars) {
            binding.waveDotsContainer.removeViewAt(0)
            waveBars.removeAt(0)
        }
    }

    private fun initializeDottedWaveform() {
        binding.waveDotsContainer.removeAllViews()
        waveBars.clear()

        val barsToFill = calculateBarsNeededForFullWidth()
        repeat(barsToFill) {
            addIdleDottedBarAtEnd()
        }

        // Scroll to right after initialization
        binding.waveformScrollView.post {
            val maxScroll = (binding.waveDotsContainer.width - binding.waveformScrollView.width).coerceAtLeast(0)
            if (maxScroll > 0) {
                binding.waveformScrollView.scrollTo(maxScroll, 0)
            }
        }
    }

    private fun calculateBarsNeededForFullWidth(): Int {
        val screenWidth = resources.displayMetrics.widthPixels
        val barWidth = dpToPx(4)
        val barMargin = dpToPx(6)
        val totalBarWidth = barWidth + barMargin
        return (screenWidth / totalBarWidth) + 5 // Add extra for smooth scrolling
    }

    private fun scrollToRight() {
        binding.waveformScrollView.post {
            val maxScroll = (binding.waveDotsContainer.width - binding.waveformScrollView.width).coerceAtLeast(0)
            if (maxScroll > 0) {
                binding.waveformScrollView.smoothScrollTo(maxScroll, 0)
            }
        }
    }

    private fun clearWaveform() {
        waveBars.forEach { bar ->
            (bar.tag as? ObjectAnimator)?.cancel()
        }
        binding.waveDotsContainer.removeAllViews()
        waveBars.clear()
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun animatePlaybackWaves() {
        val duration = player?.duration?.toLong() ?: 0L
        if (duration > 0) {
            // Animate existing waveforms during playback
            waveBars.forEachIndexed { index, bar ->
                val storedHeight = bar.tag as? Float ?: 1.0f
                val heights = floatArrayOf(
                    storedHeight * 0.8f,
                    storedHeight * 1.0f,
                    storedHeight * 0.9f,
                    storedHeight * 1.1f,
                    storedHeight * 0.8f
                )
                val animator = ObjectAnimator.ofFloat(bar, "scaleY", *heights).apply {
                    this.duration = 800 + (index * 20L)
                    repeatCount = ObjectAnimator.INFINITE
                    repeatMode = ObjectAnimator.RESTART
                    interpolator = AccelerateDecelerateInterpolator()
                }
                animator.start()
                bar.tag = animator
            }

            // Scroll animation from right to left
            binding.waveformScrollView.post {
                val maxScroll = (binding.waveDotsContainer.width - binding.waveformScrollView.width).coerceAtLeast(0)
                if (maxScroll > 0) {
                    val scrollAnimator = ValueAnimator.ofInt(maxScroll, 0).apply {
                        this.duration = duration
                        interpolator = LinearInterpolator()
                        addUpdateListener { animation ->
                            if (isAudioVNPlaying) {
                                val scrollX = animation.animatedValue as Int
                                binding.waveformScrollView.scrollTo(scrollX, 0)
                            }
                        }
                    }
                    scrollAnimator.start()
                    binding.waveformScrollView.tag = scrollAnimator
                }
            }
        }
    }

    private fun stopWaveDotsAnimation() {
        waveBars.forEach { bar ->
            (bar.tag as? ObjectAnimator)?.cancel()
        }
    }

    private fun stopPlaying() {
        val scrollAnimator = binding.waveformScrollView.tag as? ValueAnimator
        scrollAnimator?.cancel()

        binding.playVnAudioBtn.setImageResource(R.drawable.play_svgrepo_com)
        player?.release()
        player = null
        isAudioVNPlaying = false
        vnRecordAudioPlaying = false
        isOnRecordDurationOnPause = false

        stopWaveDotsAnimation()
        updateVoiceNoteUserInterfaceState(VoiceNoteState.PAUSED)

        stopPlaybackTimerRunnable()
        vnRecordProgress = 0
    }

    private fun addIdleDottedBar() {
        val bar = View(this).apply {
            val dotSize = dpToPx(5)
            layoutParams = LinearLayout.LayoutParams(
                dotSize,
                dotSize
            ).apply {
                marginEnd = dpToPx(3) // 3dp spacing
                gravity = android.view.Gravity.CENTER_VERTICAL
            }
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#2563EB"))
            }
            scaleY = 1.0f
            tag = "idle_dot"
        }

        binding.waveDotsContainer.addView(bar)
        waveBars.add(bar)
        waveBarCount++

        // Remove old bars from the START (left side) if too many
        if (waveBars.size > maxWaveBars) {
            binding.waveDotsContainer.removeViewAt(0)
            waveBars.removeAt(0)
        }
    }

    private fun updateRecordWaveProgress(progress: Float) {
        CoroutineScope(Dispatchers.Main).launch {
            binding.wave.progress = progress
            Log.d("updateWaveProgress", "updateWaveProgress: $progress")
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

    private fun stopWaveRunnable() {
        try {
            waveHandler.removeCallbacks(waveRunnable)
            isDurationOnPause = true
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

    private fun deleteVn() {
        recordedAudioFiles.clear()
        val isDeleted = deleteFiles(recordedAudioFiles)
        val outputVnFileList = mutableListOf<String>().apply { add(outputVnFile) }
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




    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    @OptIn(UnstableApi::class)
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get postId from intent
        postId = intent.getStringExtra("postId") ?: ""

        // Upload any pending comments for this post
        if (postId.isNotEmpty()) {
            uploadPendingComments()
        }

        followingManager = FollowingManager(this)

        // Load following list immediately
        lifecycleScope.launch {
            val followingList = followingManager.loadFollowingList()
            Log.d("MainActivity", "Loaded ${followingList.size} following users on app start")
        }

        fun resetCountValue() {
            countValue = 0
            resetCountValue()
        }
        binding = ActivityMainBinding.inflate(layoutInflater)

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
        setContentView(binding.root)

        directReplyListener = this

        timer = Timer(this)

        var navigateTo = intent.getStringExtra("fragment") ?: "shorts"

        var userProfileFragment = intent.getStringExtra("UserProfileFragment")
        intent.getStringExtra("title") ?: ""

        if (userProfileFragment != null) {
            navigate("R.id.profile", "userProfileFragment")
        }

        if (navigateTo == "profile") {
            getNavigationController().navigate("R.id.profile", "Profile")
        } else {
            getNavigationController().navigate("R.id.shots", "Shorts")
            lastFragmentId = navigateTo

        }

        startDirectReplyService()

        bottomNavigation = binding.bottomNavigationView
        EventBus.getDefault().post(UserProfileShortsStartGet())
        // Initialize the ViewModel
        myViewModel = ViewModelProvider(this)[LikeUnLikeViewModel::class.java]
        shortsViewModel = ViewModelProvider(this)[ShortsViewModel::class.java]
        feedViewModel = ViewModelProvider(this)[GetFeedViewModel::class.java]

        userProfileShortsViewModel =
            ViewModelProvider(this)[GetShortsByUsernameViewModel::class.java]
        userShortsFragment = ViewModelProvider(this)[UserProfileShortsViewModel::class.java]
        shortsCommentViewModel = ViewModelProvider(this)[RoomCommentsViewModel::class.java]
        commentFilesViewModel = ViewModelProvider(this)[RoomCommentFilesViewModel::class.java]
        roomCommentReplyViewModel = ViewModelProvider(this)[RoomCommentReplyViewModel::class.java]
        commentsViewModel = ViewModelProvider(this)[ShortCommentsViewModel::class.java]
        commentsReplyViewModel = ViewModelProvider(this)[ShortCommentReplyViewModel::class.java]
        commentViewModel = ViewModelProvider(this)[CommentsViewModel::class.java]

        lifecycleScope.launch {}

        myProfileRepository = ProfileRepository(ChatDatabase.getInstance(this).profileDao())

        settings = getSharedPreferences(PREFS_NAME, 0)
        val accessToken = settings.getString("token", "").toString()


        Log.d("accessToken", "accessToken: $accessToken")
        myId = localStorage.getUserId()

        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.title = ""

        getNavigationController().navigate("R.id.shots", "Shorts")



        // Find the menu item by ID
        val editMenuItem: MenuItem? = binding.toolbar.menu.findItem(R.id.menu_edit)
        val settingsMenuItem: MenuItem? = binding.toolbar.menu.findItem(R.id.menu_setting)
        val logoutMenuItem: MenuItem? = binding.toolbar.menu.findItem(R.id.logout)
        val searchMenuItem: MenuItem? = binding.toolbar.menu.findItem(R.id.menu_search)

        searchMenuItem?.setOnMenuItemClickListener {
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


        observeMediator()

        val flashDir = "Flash"
        val storageDirectory = File(
            Environment.getExternalStorageDirectory(), flashDir
        )


        // Ensure that the directory exists
        if (!storageDirectory.exists()) {
            storageDirectory.mkdirs()
        }

        Log.d("Download", "directory path - $storageDirectory")



        setNavigationListener()
        getUserProfile()


        val profilePic = settings.getString("avatar", "").toString()
        val profilePic2 = settings.getString("profile_pic", "").toString()

        Log.d("ProfilePic", "Avatar path: $profilePic")
        Log.d("ProfilePic", "Avatar path2: $profilePic2")

        item = NavigationItem(this@MainActivity, R.drawable.nav_notification_icon)
        item1 = NavigationItem(this@MainActivity, R.drawable.chat_round_svgrepo_com)
        item2 = NavigationItem(this@MainActivity, R.drawable.play_svgrepo_com)
        item3 = NavigationItem(this@MainActivity, R.drawable.scroll_text_line_svgrepo_com)
        item4 = NavigationItem(this@MainActivity, R.drawable.flash21)

        item3.drawableWidth = 36
        item3.drawableHeight = 36
        item2.drawableWidth = 36
        item2.drawableHeight = 36
        item1.drawableWidth = 36
        item1.drawablePadding = 15
        item1.drawableHeight = 36
        item.drawableWidth = 36
        item.drawableHeight = 36

        item4.drawableWidth = 36
        item4.drawableHeight = 36

        item.setsBadge(count)
        item1.setsBadge(4)

        item.hideBadge()
        item1.hideBadge()
        item2.hideBadge()
        item3.hideBadge()
        item4.hideBadge()


        binding.bottomNavigationView.addItem(item)
        binding.bottomNavigationView.addItem(item1)
        binding.bottomNavigationView.addItem(item2)
        binding.bottomNavigationView.addItem(item3)
        binding.bottomNavigationView.addItem(item4)


        val TAG = "onCreate"
        binding.bottomNavigationView.position = 2
        Log.d(TAG, "onCreate is selected: ${item2.isSelected}")
        Log.d(TAG, "onCreate is checked: ${item2.isChecked}")
        Log.d(TAG, "onCreate is activated: ${item2.isActivated}")
        Log.d(TAG, "onCreate is enabled: ${item2.isEnabled}")

        if (item2.isEnabled) {
            item2.drawableTint = Color.WHITE
        }

        loadProfileImage(profilePic2)


        lifecycleScope.launch {

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
                        reverseFormattedDuration(durationString)


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

        docsPickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    // Handle image selection result here
                    val data = result.data
                    // Process the selected image data
                    val docPath = data?.getStringExtra("doc_url")

                    Log.d("Document Results", "Picked Document : $docPath")

                    if (docPath != null) {
                        // You now have the imagePath from the DisplayImages activity.
                        // You can use it as needed, for example, to send the image in a message.
                        Log.d("ChatActivityDocPath", "Selected Document path: $docPath")


                        "files/${System.currentTimeMillis()}.jpg" // Change the file name as needed


                        val user = User("0", "You", "test", true, Date())
                        val messageId = "Doc_${Random.nextInt()}"

                        val date = Date(System.currentTimeMillis())

                        Message(
                            messageId, user, // Set user ID as needed
                            null, date
                        )

                        Uri.parse(docPath)
                        val file = File(docPath)
                        if (file.exists()) {

                            Log.d("Document File", "Document File Exists : $file")
                            val absolutePath = file.absolutePath

                            val fileUri = Uri.fromFile(file)
                            val fileUrl = fileUri.toString()


                        }

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
                                    Compressor.compress(this@MainActivity, file)
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
        customDialog = CustomAlertDialog(this)

        customDialog?.setDialogCallback(this)
        getUserBussinessProfile()

        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val fragmentManager = supportFragmentManager // if this is the main Activity

                val currentVisibility = binding.motionLayout.visibility
                if (currentVisibility == View.VISIBLE) {
                    binding.VNLayout.visibility = View.GONE
                    stopPlaying()
                    deleteRecording()
                    binding.motionLayout.visibility = View.GONE

                    Log.d("handleOnBackPressed", "UI cleared, not popping fragment yet.")
                } else {
                    if (fragmentManager.isStateSaved) {
                        Log.w("handleOnBackPressed", "FragmentManager isStateSaved, can't pop now.")
                        return
                    }

                    Handler(Looper.getMainLooper()).post {
                        try {
                            if (fragmentManager.backStackEntryCount > 0) {
                                fragmentManager.popBackStack()
                                Log.d("handleOnBackPressed", "Popped fragment from back stack.")
                            } else {
                                finish()
                                Log.d("handleOnBackPressed", "No fragments left, finishing activity.")
                            }
                        } catch (e: IllegalStateException) {
                            Log.e("handleOnBackPressed", "Error popping back stack: ${e.message}")
                        }
                    }
                }
            }
        }


        initializeCommentsBottomSheet()


        binding.VNLinearLayout.setOnClickListener {
            Log.d(TAG, "onCreate: vn linear layout touched")
        }


        if (binding.motionLayout.visibility == View.GONE) {
            binding.VNLayout.visibility = View.GONE
        } else {

        }


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
                    mixVoiceNote() // Execute mixVN asynchronously
                }

                lifecycleScope.launch(Dispatchers.Main) {

                    delay(500)
                    stopRecordingVoiceNote()
                }


            }
        }

        waveBarCount = 0

        binding.waveDotsContainer.removeAllViews()
        waveBars.clear()
        waveBarCount = 0
        val barsToFill = calculateBarsNeededForFullWidth()
        repeat(barsToFill) {
            addIdleDottedBar()
        }
        binding.waveformScrollView.post {
            binding.waveformScrollView.fullScroll(View.FOCUS_RIGHT)
        }

        setupCommentCountObservers()

    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE && grantResults.size > 0) {
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

    @SuppressLint("SetTextI18n")
    @OptIn(UnstableApi::class)
    private fun toggleMotionLayoutVisibility() {
        val currentVisibility = binding.motionLayout.visibility

        if (currentVisibility == View.VISIBLE) {
            // If currently visible, make it gone
            binding.motionLayout.visibility = View.GONE
            binding.VNLayout.visibility = View.GONE

            binding.replyToLayout.visibility = View.GONE
            binding.input.inputEditText.setText("")
            isReply = false
            commentsViewModel.resetLiveData()

            hideKeyboard(binding.input.inputEditText)
            deleteRecording()
            stopPlaying()
            commentAudioStop()
            stopWaveRunnable()
            stopRecordWaveRunnable()
            exoPlayer?.release()


            if (mediaRecorder != null) {
            }


        } else {
            binding.motionLayout.currentState

            // If currently gone, make it visible and set the transition to start
            binding.motionLayout.visibility = View.VISIBLE

            binding.motionLayout.transitionToStart()
        }
    }

    private fun generateSampleData(count: Int): List<Comment> {
        val itemList = mutableListOf<Comment>()
        for (i in 1..count) {
            itemList.add(Comment("Item $i"))
        }
        return itemList
    }

    private fun allShortComments(page: Int) {

        lifecycleScope.launch(Dispatchers.IO) {

            withContext(Dispatchers.Main) {
                if (page == 1) {
                    showShimmer()
                } else {
                    showProgressBar()
                }
            }
            try {

                val commentsWithReplies = commentViewModel.fetchAllComments(postId, page)

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

                    if (commentsWithReplies.isEmpty()) {
                        updateUI(true)
                    } else {
                        updateUI(false)
                    }
                }

            } catch (e: Exception) {
                Log.e("UserProfileShortsViewModel", "Exception: ${e.message}")
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun showBottomNav(event: ShowBottomNav) {

        Log.d("showBottomNav", "showBottomNav: Show bottom nav")
        val constraintLayout = binding.constraintLayout
        val bottomNavigationView = binding.bottomNavigationView
        val reference = binding.reference

        val hiddenConstraintSet = ConstraintSet()
        hiddenConstraintSet.clone(constraintLayout)
        hiddenConstraintSet.clear(bottomNavigationView.id, ConstraintSet.TOP)
        hiddenConstraintSet.connect(
            bottomNavigationView.id, ConstraintSet.BOTTOM, reference.id, ConstraintSet.BOTTOM
        )
        applyConstraintSetWithAnimation(constraintLayout, hiddenConstraintSet, 250)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun showAppBar(event: ShowAppBar) {
        binding.appbar.visibility = View.VISIBLE
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun hideAppBar(event: HideAppBar) {
        binding.appbar.visibility = View.GONE
    }

    fun showBottomNavView() {
        Log.d("showBottomNavView", "showBottomNavView: Show bottom nav")

        val constraintLayout = binding.constraintLayout
        val bottomNavigationView = binding.bottomNavigationView
        val reference = binding.reference

        val hiddenConstraintSet = ConstraintSet()
        hiddenConstraintSet.clone(constraintLayout)
        hiddenConstraintSet.clear(bottomNavigationView.id, ConstraintSet.TOP)
        hiddenConstraintSet.connect(
            bottomNavigationView.id, ConstraintSet.BOTTOM, reference.id, ConstraintSet.BOTTOM
        )
        applyConstraintSetWithAnimation(constraintLayout, hiddenConstraintSet, 250)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun hideBottomNav(event: HideBottomNav) {
        Log.d("showBottomNav", "hideBottomNav: hide bottom nav")


        val constraintLayout = binding.constraintLayout
        val bottomNavigationView = binding.bottomNavigationView
        val reference = binding.reference


        val visibleConstraintSet = ConstraintSet()
        visibleConstraintSet.clone(constraintLayout)
        visibleConstraintSet.clear(bottomNavigationView.id, ConstraintSet.BOTTOM)
        visibleConstraintSet.connect(
            bottomNavigationView.id, ConstraintSet.TOP, reference.id, ConstraintSet.BOTTOM
        )

        applyConstraintSetWithAnimation(constraintLayout, visibleConstraintSet, 350)
    }


    private fun applyConstraintSetWithAnimation(
        constraintLayout: ConstraintLayout, constraintSet: ConstraintSet, duration: Long
    ) {
        // TransitionManager for smooth animation
        val transition = ChangeBounds()
        transition.duration = duration // Set your desired duration
        TransitionManager.beginDelayedTransition(constraintLayout, transition)
        constraintSet.applyTo(constraintLayout)
    }

    private suspend fun loadMoreShorts(nextPage: Int) {
        // This function will be called when there are more shorts available

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onProfileImageEvent(event: ProfileImageEvent) {
        // Handle the event in the MainActivity
        loadProfileImage(event.profilePic)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    private fun loadProfileImage(profilePic: String) {
        Glide.with(this).asBitmap().load(profilePic).transform(CircleCrop())
            .placeholder(R.drawable.flash21).error(R.drawable.error_drawable)
            .fallback(R.drawable.fallback_drawable).diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val drawable = BitmapDrawable(resources, resource)
                    item4.setDrawable(drawable)
                    Log.d("ProfilePic", "onResourceReady")

                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    Log.d("ProfilePic", "onLoadCleared")

                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    Log.d("ProfilePic", "onLoadFailed")
                    item4.setDrawable(errorDrawable)
                }
            })
    }


    private fun serverResponseToEntity(serverResponse: List<Post>): List<UserShortsEntity> {
        return serverResponse.map { serverResponseItem ->
            UserShortsEntity(
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
                thumbnail = serverResponseItem.thumbnail
                // map other properties...
            )
        }
    }


    private fun startCombinedWorker() {
        val workManager = WorkManager.getInstance(this)

        val existingWorkPolicy = if (workManager.getWorkInfosByTag(WORKER_TAG).get().isEmpty()) {
            ExistingWorkPolicy.REPLACE
        } else {
            Log.d(WORKER_TAG, "Combined Work already exists, Replacing.......")

            ExistingWorkPolicy.REPLACE

        }

        val request = OneTimeWorkRequestBuilder<CombinedWorker>().setBackoffCriteria(
            BackoffPolicy.LINEAR,
            10,
            TimeUnit.SECONDS
        ).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).addTag(WORKER_TAG)
            .build()

        try {
            workManager.enqueueUniqueWork(WORKER_TAG, existingWorkPolicy, request)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(WORKER_TAG, "Error enqueuing work: ${e.message}")
        }

        observeWorkStatus(request.id)
    }

    private fun downloadAndSaveImage(
        mUrl: String, fileName: String, menuItem: MenuItem
    ) {
        Log.d("Download", "downloadAndSaveImage")
        GlobalScope.launch(Dispatchers.IO) {
            val url = URL(mUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept-Encoding", "identity")
            connection.connect()

            try {
                if (connection.responseCode in 200..299) {
                    val inputStream = connection.inputStream
                    val internalStoragePath = File(filesDir, "images")
                    if (!internalStoragePath.exists()) {
                        internalStoragePath.mkdirs()
                    }

                    val destinationFile = File(internalStoragePath, fileName)

                    val outputStream = FileOutputStream(destinationFile)

                    var bytesCopied: Long = 0
                    val buffer = ByteArray(1024)
                    var bytes = inputStream.read(buffer)
                    while (bytes >= 0) {
                        bytesCopied += bytes
                        outputStream.write(buffer, 0, bytes)
                        bytes = inputStream.read(buffer)
                    }

                    Log.d("Download", "File Downloaded: ${destinationFile.absolutePath}")
                    Glide.with(applicationContext).asBitmap().load(destinationFile)
                        .transform(CircleCrop()).placeholder(R.drawable.google)

                        .error(R.drawable.error_drawable) // Drawable to display on load failure
                        .fallback(R.drawable.fallback_drawable)
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(
                                resource: Bitmap, transition: Transition<in Bitmap>?
                            ) {
                                menuItem.icon = BitmapDrawable(resources, resource)
                                Log.d("ProfilePic", "onResourceReady")
                                Log.d(
                                    "BitmapSize",
                                    "Width: ${resource.width}, Height: ${resource.height}"
                                )

                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                                // Handle when the drawable is cleared
                                menuItem.icon = placeholder
                                Log.d("ProfilePic", "onLoadCleared")
                            }

                            override fun onLoadFailed(errorDrawable: Drawable?) {
                                // Handle failure
                                menuItem.icon = errorDrawable
                                Log.d("ProfilePic", "onLoadFailed")

                            }
                        })
                    outputStream.close()
                    inputStream.close()
                } else {
                    Log.e("DownloadFailed", "HTTP response code: ${connection.responseCode}")
                }
            } catch (e: Exception) {
                Log.e("DownloadFailed", e.message.toString())
                e.printStackTrace()
            }
        }
    }

    private fun startCombinedWorkerPeriodic() {
        Log.d(WORKER_TAG, "Creating Work Instance")
        try {
            val workManager = WorkManager.getInstance(this)

            // Add constraints for internet connection or unmetered network
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED) // Requires an internet connection
                .build()

            // Schedule the worker to run every 10 minutes with a flex interval of 5 minutes
            val request = PeriodicWorkRequestBuilder<CombinedWorker>(
                repeatInterval = 10, // Repeat every 10 minutes
                repeatIntervalTimeUnit = TimeUnit.MINUTES,
                flexTimeInterval = 5,
                flexTimeIntervalUnit = TimeUnit.MINUTES
            ).addTag("Flash_Master_Periodic_second").build()

            try {
                workManager.enqueueUniquePeriodicWork(
                    "Flash_Master_Periodic_second",
                    ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                    request
                )
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(WORKER_TAG, "Error enqueuing work: ${e.message}")
            }

            // observeWorkStatus(request.id)
        } catch (e: Exception) {
            Log.d(WORKER_TAG, "Error creating Work Instance: ${e.message}")
        }
    }

    private fun observeWorkStatus(id: UUID) {
        val workManager = WorkManager.getInstance(this)

        workManager.getWorkInfoByIdLiveData(id).observe(this, Observer {
            Log.d(WORKER_TAG, "Work Status: ${it.state}")
        })
    }

    private fun startChatSocketWorker() {
        val request = OneTimeWorkRequestBuilder<ChatSocketWorker>().setBackoffCriteria(
            BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS
        ) // Customize backoff criteria
            .build()

        WorkManager.getInstance(applicationContext).enqueue(request)
    }

    private fun startDirectReplyService() {
        val serviceIntent = Intent(this, DirectReplyService::class.java)
        startService(serviceIntent)
    }

    private fun startCallWorker() {
        val constraints =
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        val request = OneTimeWorkRequestBuilder<FlashWorker>()

            .setInitialDelay(10, TimeUnit.SECONDS).setBackoffCriteria(
                BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS
            ) // Customize backoff criteria
            .build()

        WorkManager.getInstance(applicationContext).enqueue(request)

    }

    private fun startPeriodicCallWorker() {
        val constraints =
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        val request = PeriodicWorkRequestBuilder<FlashWorker>(
            repeatInterval = 20, // 20 minutes
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        ).setConstraints(constraints).setBackoffCriteria(
            BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS
        ) // Customize backoff criteria
            .build()

        WorkManager.getInstance(applicationContext).enqueue(request)
    }


    private fun launchOauth() {
        val handler = Handler()
        handler.postDelayed(
            {
                val intent = Intent(this, GoogleOAuth::class.java)
                startActivity(intent)
            }, 2000
        )
    }

    private fun observerAction() {
        mainViewModel.selectedDialogsCount.observe(this, Observer { count ->
            if (count != null && count > 0) {
                if (actionMode == null) {
                    // ActionMode not started yet, start it
                    startAction(count)
                } else {
                    // ActionMode already started, update the title
                    actionMode?.title = "$count selected"
                }


            } else {
                // No items selected, end the ActionMode if it's active
                actionMode?.finish()

            }
        })
    }

    private fun observeMediator() {
        // Observe both LiveData objects
        val mediatorLiveData = MediatorLiveData<Pair<Int, Int>>()
        mediatorLiveData.addSource(mainViewModel.selectedDialogsCount) {
            mediatorLiveData.value = Pair(it, mainViewModel.selectedCallLogsCount.value ?: 0)
        }
        mediatorLiveData.addSource(mainViewModel.selectedCallLogsCount) {
            mediatorLiveData.value = Pair(mainViewModel.selectedDialogsCount.value ?: 0, it)
        }

        // Do something when both values are greater than 1
        mediatorLiveData.observe(this@MainActivity) { (selectedDialogsCount, selectedCallLogsCount) ->
            if (selectedDialogsCount > 0 || selectedCallLogsCount > 0) {
                // Perform the action here
                // For example, show a toast
                val count = selectedDialogsCount + selectedCallLogsCount

                if (actionMode == null) {
                    // ActionMode not started yet, start it
                    startAction(count)
                } else {
                    // ActionMode already started, update the title
                    actionMode?.title = "$count selected"
                }
            } else {
                // No items selected, end the ActionMode if it's active
                actionMode?.finish()

            }
        }

    }


    private fun startAction(count: Int) {
        actionMode = startSupportActionMode(object : ActionMode.Callback {
            // Implement the necessary callbacks for the ActionMode

            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                // Inflate the menu for the ActionMode
                menuInflater.inflate(R.menu.contextual_action_bar, menu)

                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                // Update the UI based on the selected items
                return false
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                return when (item?.itemId) {
                    R.id.delete -> {
                        // Handle delete icon press
                        val dialogs = mainViewModel.selectedDialogsList
                        val callLogs = mainViewModel.selectedCallLogs

                        if (dialogs.isNotEmpty()) {
                            deleteDialogs(dialogs)
                        } else if (callLogs.isNotEmpty()) {
                            deleteCallLogs(callLogs)
                        }

                        true // Indicate that the ActionMode has handled this item
                    }

                    R.id.more -> {
                        // Handle more item (inside overflow menu) press
                        true // Indicate that the ActionMode has handled this item
                    }

                    else -> false // Indicate that the regular onOptionsItemSelected should handle this item
                }
            }


            override fun onDestroyActionMode(mode: ActionMode?) {
                // ActionMode finished, reset the reference

                actionMode = null
                clear()
                mainViewModel.resetSelectedDialogsCount()
            }
        })

        // Set the initial title
        actionMode?.title = "$count selected"
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

    private fun deleteDialogs(dialogs: List<Dialog>) {
        val dialogIds = dialogs.map { it.id }
        CoroutineScope(Dispatchers.IO).launch {
            if (dialogs.all { it.users.size > 1 }) {

                dialogIds.map {
                    messageViewModel.markDeleted(it)
                    val dialog = groupDialogViewModel.getGroupDialog(it)
                    val empty = setEmptyMessage(it, dialog)
                    groupDialogViewModel.updateLastMessageForThisGroup(it, empty)
                }
            } else {

                dialogIds.map {
                    messageViewModel.deleteMessagesByChat(it)
                    dialogViewModel.setNullLastMessage(it)
                }
            }
        }
        clear()
        mainViewModel.resetSelectedDialogsCount()
    }

    private fun deleteCallLogs(callLogs: List<CallLogEntity>) {
        val callLogIds = callLogs.map { it.id }

        CoroutineScope(Dispatchers.IO).launch {
            callViewModel.deleteCallLogs(callLogIds)
        }

        clear()
        mainViewModel.resetSelectedDialogsCount()
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

    private fun setEmptyMessage(chatId: String, dialog: GroupDialogEntity): MessageEntity {
        val createdAt = System.currentTimeMillis()
        val lastseen = Date(createdAt)

        val avatar = settings.getString("avatar", "avatar").toString()

        val user = UserEntity(
            id = myId, name = "You", avatar = "avatar", online = true, lastSeen = lastseen
        )

        return MessageEntity(
            id = "DeletedMessage_${Random.nextInt()}",
            chatId = chatId,
            text = "",
            userId = "Flash",
            user = user,
            createdAt = dialog.lastMessage.createdAt,
            imageUrl = null,
            voiceUrl = null,
            voiceDuration = 0,
            userName = "You",
            status = "Received",
            videoUrl = null,
            audioUrl = null,
            docUrl = null,
            fileSize = 0,
            deleted = true
        )
    }


    private fun createMessage(text: String, chatId: String): MessageEntity {
        val createdAt = System.currentTimeMillis()
        val lastSeen = Date(createdAt)

        val avatar = settings.getString("avatar", "avatar").toString()

        val user = UserEntity(
            id = "0", name = "You", avatar = avatar, online = true, lastSeen = lastSeen
        )

        return MessageEntity(
            id = "Text_${Random.nextInt()}",
            chatId = chatId,
            text = text,
            userId = myId,
            user = user,
            createdAt = createdAt,
            imageUrl = null,
            voiceUrl = null,
            voiceDuration = 0,
            userName = "You",
            status = "Sent",
            videoUrl = null,
            audioUrl = null,
            docUrl = null,
            fileSize = 0,
            deleted = false
        )
    }


    override fun onBackPressed() {
        val count = mainViewModel.selectedDialogsCount.value

        if (mainViewModel.selectedDialogsCount.value == 0) {
            // If no dialogs are selected, call the default back button behavior
            super.onBackPressed()
        } else {

            clear()
            mainViewModel.resetSelectedDialogsCount()
        }


    }


    private fun clear() {
        // Do some work
        // Notify listeners
        notifyOnSomeEvent()
    }

    private fun notifyOnSomeEvent() {
        onBackPressedListeners.forEach { it.onBackButtonPressed() }
    }


    // Register a listener in your fragment
    fun addOnBackPressedListener(listener: OnBackPressedListener) {
        onBackPressedListeners.add(listener)
    }

    // Unregister a listener in your fragment
    fun removeOnBackPressedListener(listener: OnBackPressedListener) {
        onBackPressedListeners.remove(listener)
    }


    private fun initializeCallService() {
        CoroutineScope(Dispatchers.IO).launch {

            val username = settings.getString("username", "").toString()
            val userId = settings.getString("_id", "").toString()

            Log.d("VideoCall", "username : $username")
            Log.d("VideoCall", "userId : $userId")

            mainRepository.init(username)
            mainServiceRepository.startService(username)
            mainRepository.setUserName(username)

        }
    }


    private fun performLogout() {
        CoroutineScope(Dispatchers.IO).launch {
            //delete data from local db
            deleteUserProfile()
            //clear shared prefs
            LocalStorage.getInstance(this@MainActivity).clear()
            LocalStorage.getInstance(this@MainActivity).clearToken()
            settings.edit().clear().apply()

            callViewModel.clearAll()
            messageViewModel.clearAll()
            dialogViewModel.clearAll()
            groupDialogViewModel.clearAll()
        }

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }


    fun setDarkTheme() {
        setTheme(R.style.DarkTheme)
    }

    private fun getUserProfile() {

        GlobalScope.launch {
            val response = try {

                retrofitInterface.apiService.getMyProfile()
            } catch (e: HttpException) {
                Log.d("RetrofitActivity", "Http Exception ${e.message}")
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity, "HTTP error. Please try again.", Toast.LENGTH_SHORT
                    ).show()
                }

                return@launch
            } catch (e: IOException) {
                Log.d("RetrofitActivity", "IOException ${e.message}")
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity, "Network error. Please try again.", Toast.LENGTH_SHORT
                    ).show()
                }
                return@launch
            } finally {

            }

            if (response.isSuccessful) {
                val responseBody = response.body()

                if (responseBody?.data != null) {

                    val editor = settings.edit()
                    editor.putString("firstname", responseBody.data.firstName)
                    editor.putString("lastname", responseBody.data.lastName)
                    editor.putString("avatar", responseBody.data.account.avatar.url)
                    editor.putString("bio", responseBody.data.bio)
                    editor.apply()

                    val myProfile = ProfileEntity(
                        __v = responseBody.data.__v,
                        _id = responseBody.data._id,
                        bio = responseBody.data.bio,
                        firstName = responseBody.data.firstName,
                        lastName = responseBody.data.lastName,
                        account = responseBody.data.account,
                        createdAt = responseBody.data.createdAt,
                        dob = responseBody.data.dob,
                        countryCode = responseBody.data.countryCode,
                        coverImage = responseBody.data.coverImage,
                        updatedAt = responseBody.data.updatedAt,
                        followersCount = responseBody.data.followersCount,
                        isFollowing = responseBody.data.isFollowing,
                        location = responseBody.data.location,
                        owner = responseBody.data.owner,
                        phoneNumber = responseBody.data.phoneNumber,
                        followingCount = responseBody.data.followingCount
                    )

                    insertProfile(myProfile)


                } else {
                    Log.d("RetrofitActivity", "Response body or data is null")
                }
            }

        }

    }


    private fun setUpTabs() {

    }


    suspend fun deleteUserProfile() {
        myProfileRepository.deleteMyProfile()
    }

    private fun insertProfile(myProfile: ProfileEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            myProfileRepository.insertProfile(myProfile)
        }
    }

    private fun setItemColor(item: NavigationItem) {

    }

    private fun setNavigationListener() {
        bottomNavigation.setOnClickedButtonListener { button, pos ->
            // Reset all items to black background
            item.drawableTint = Color.BLACK
            item1.drawableTint = Color.BLACK
            item2.drawableTint = Color.BLACK
            item3.drawableTint = Color.BLACK


            when (pos) {
                0 -> {
                    // Handle click on the first button
                    item.drawableTint = Color.WHITE

                    getNavigationController().navigate("R.id.notifications", "Notification")
                    Log.d("bottomNavigation", "position $pos")
                }

                1 -> {
                    // Handle click on the second button
                    item1.drawableTint = Color.WHITE
                    getNavigationController().navigate("R.id.chat", "Chat")
                    Log.d("bottomNavigation", "position $pos")
                }

                2 -> {
                    // Handle click on the third button
                    item2.drawableTint = Color.WHITE
                    getNavigationController().navigate("R.id.shots", "Shorts")
                    Log.d("bottomNavigation", "position $pos")
                }

                3 -> {
                    // Handle click on the third button
                    item3.drawableTint = Color.WHITE
                    getNavigationController().navigate("R.id.feed", "Feed")
                    Log.d("bottomNavigation", "position $pos")
                }

                4 -> {



                    getNavigationController().navigate("R.id.profile", "Profile")
                    Log.d("bottomNavigation", "position $pos")
                }

                // ... add more cases for other positions

                else -> {
                    // Handle unknown position
                    Log.d("bottomNavigation", "position position")
                }
            }
        }


    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_setting -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }

            R.id.logout -> {
                showLogoutConfirmationDialog()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun openChat(id: Long, prepopulateText: String?) {
        Log.d("NavigationController", "Open Chat")
    }

    override fun openPhoto(photo: Uri) {
        Log.d("NavigationController", "Open Photo")
    }

    override fun navigate(id: String, title: String) {
        Log.d("NavigationController", "On Navigate")
        binding.pageTitle.text = title
        val TAG = "navigate"


        when (id) {


            "R.id.notifications" -> {
                replaceFragment(NotificationHostFragment.newInstance("", ""))
            }

            "R.id.shots" -> {
                replaceFragment(ShotsFragment.newInstance("", ""))
            }

            "R.id.feed" -> {
                replaceFragment(FeedFragment.newInstance("", ""))
            }

            "R.id.chat" -> {
                replaceFragment(ChatFragment.newInstance("", ""))
            }

            "R.id.profile" -> {

                val intent = Intent(this, MyUserProfileAccount::class.java)
                startActivity(intent)
            }

            "R.id.feedPost" -> {

            }

            else -> {
                Log.d("NavigationController", "Unknown Tab")
            }
        }
    }

    override fun updateAppBar(
        showContact: Boolean, hidden: Boolean, body: (name: TextView, icon: ImageView) -> Unit
    ) {
        Log.d("NavigationController", "Update App Bar")

    }

    private fun showTabs(id: String): Boolean {
        return when (id) {
            "R.id.chat" -> true
            else -> false
        }
    }

    private fun replaceFragment(fragment: Fragment) {

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }

    private fun replaceFragment2(
        fragment: Fragment,
        userShortsList: ArrayList<UserShortsEntity>,
        clickedShort: UserShortsEntity,
        isFromFavorite: Boolean
    ) {

        val fragmentManager = supportFragmentManager


        val bundle = Bundle().apply {
            putSerializable(SHORTS_LIST, userShortsList)
            putSerializable(CLICKED_SHORT, clickedShort)
            putBoolean(FROM_FAVORITE_FRAGMENT, isFromFavorite)
        }

        fragment.arguments = bundle

        val fragmentTransaction = fragmentManager.beginTransaction()

        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun openUserProfileFragment(event: GoToUserProfileFragment) {


        binding.bottomNavigationView.visibility = View.VISIBLE
        binding.bottomNavigationView.position = 4
        item2.drawableTint = Color.BLACK


        setRingDrawable(item4.imageView)

        getNavigationController().navigate("R.id.profile", "Profile")


    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun openFeedDetailsFragment(event: FeedDetailPage) {


        runOnUiThread {
            val originalPostList = event.data.originalPost
            if (originalPostList.isNotEmpty()) {
                val originalPost = originalPostList.first()

                val fragment = Fragment_Original_Post_With_Repost_Inside.newInstance(event.data)

                val args = Bundle().apply {
                    putInt("position", event.position)
                    putSerializable("data", originalPost)
                }

                fragment.arguments = args

                supportFragmentManager.beginTransaction()
                    .replace(R.id.frame_layout, fragment)
                    .addToBackStack(null)
                    .commit()
            } else {
                Log.e("Feed", "OriginalPost list is empty in the selected Post")
                Toast.makeText(this, "Original post not available", Toast.LENGTH_SHORT).show()
            }
        }



    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun openUserProfileShortsPlayerFragment(event: GoToUserProfileShortsPlayerFragment) {

        Log.d("openUserProfileShortsPlayerFragment", "openUserProfileShortsPlayerFragment: inside ")

        binding.bottomNavigationView.visibility = View.GONE

        replaceFragment2(
            UserProfileShortsPlayerFragment(),
            event.userShortsList,
            event.clickedShort,
            event.isFromFavorite
        )
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun openShortsFragment(event: GoToShortsFragment) {

        Log.d(
            "openShortsFragment",
            "openShortsFragment: inside feedPostPosition ${event.feedPostPosition}"
        )


        hideAppBar()

        binding.bottomNavigationView.visibility = View.VISIBLE
        binding.bottomNavigationView.position = 2
        item2.drawableTint = Color.WHITE
        item3.drawableTint = Color.BLACK

        replaceFragment(
            ShotsFragment.willReturnToFeedInstance(
                true,
                event.feedPostPosition,
                event.feedShortsBusinessId,
                event.fileId
            )
        )


    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun openFeedFragment(event: GoToFeedFragment) {

        Log.d("openFeedFragment", "openFeedFragment: inside ")


        binding.bottomNavigationView.visibility = View.VISIBLE
        binding.bottomNavigationView.position = 3
        item3.drawableTint = Color.WHITE
        item2.drawableTint = Color.BLACK
        showAppBar()
        replaceFragment(FeedFragment.feedPostFromShorts(event.feedPostPosition))

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

        Log.d(TAG, "openUserProfileShortsPlayerFragment: ")
        lifecycleScope.launch {
            try {
                val response = retrofitInterface.apiService.favoriteShort(event.postId)
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d(
                        TAG, "favoriteUnFavoriteShort ${responseBody?.data!!.isBookmarked}"
                    )
                } else {
                    Log.d(TAG, "Error: ${response.message()}")

                }

            } catch (e: HttpException) {
                Log.d(TAG, "Http Exception ${e.message}")
                runOnUiThread {
                    showToast(this@MainActivity, "Failed to connect try again...")
                }
            } catch (e: IOException) {
                Log.d(TAG, "IOException ${e.message}")

            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun openUserProfileFragment2(event: GoToUserProfileFragment2) {


        binding.bottomNavigationView.visibility = View.VISIBLE
        binding.bottomNavigationView.position = 4
        item2.drawableTint = Color.BLACK


        setRingDrawable(item4.imageView)

        getNavigationController().navigate("R.id.profile", "Profile")


    }

    private fun setRingDrawable(imageView: ImageBadgeView) {
        // Set the ring drawable programmatically

        val TAG = "setRingDrawable"
        Log.d(TAG, "setRingDrawable: set ring drawable")
        val ringDrawable = ContextCompat.getDrawable(this, R.drawable.profile_ring_background)

        if (ringDrawable != null) {
            imageView.background = ringDrawable
            Log.d(TAG, "setRingDrawable: able to load ring drawable.")
        } else {
            Log.e(TAG, "setRingDrawable: Unable to load ring drawable.")
        }
    }

    override fun onDirectReply(message: String, chatId: String) {
        Log.d("DirectReply", "onDirectReply : $message, $chatId")
    }


    fun hideAppBar() {
        binding.appbar.visibility = View.GONE
    }

    fun showAppBar() {
        binding.appbar.visibility = View.VISIBLE
    }

    fun hideBottomNavigation() {
        binding.bottomNavigationView.visibility = View.GONE
    }

    fun showBottomNavigation() {
        binding.bottomNavigationView.visibility = View.VISIBLE
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun shortsCacheEvent(event: ShortsCacheEvent) {
        Log.d("shortsCacheEvent", "shortsCacheEvent - ${event.videoPath}")


    }

    // Restore state in onRestoreInstanceState or onViewCreated
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val savedState = savedInstanceState.getInt("bottomNavState", STATE_SCROLLED_UP)
        // Set the state based on the savedState
        if (savedState == STATE_SCROLLED_DOWN) {

            val behavior = HideBottomViewOnScrollBehavior<BottomNavigationView>()
            behavior.slideUp(binding.bottomNavigationView)
        } else {

            val behavior = HideBottomViewOnScrollBehavior<BottomNavigationView>()
            behavior.slideUp(binding.bottomNavigationView)
        }
    }

    override fun onPause() {
        super.onPause()

        if (player?.isPlaying == true) {
            player?.pause()
        }
        if (exoPlayer?.isPlaying == true) {
            exoPlayer?.stop()
        }

    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onDestroy() {
        super.onDestroy()
        timerHandler.removeCallbacksAndMessages(null)
        playbackTimerRunnable = null
        stopPlaying()
        commentAudioStop()
        stopWaveRunnable()
        stopRecordWaveRunnable()
        MediaLoader.getInstance(this).destroy()
        exoPlayer?.removeListener(playbackStateListener)

        if (::outputFile.isInitialized || isRecording) {
            stopRecordingVoiceNote()
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
                avatar = null
            )
            val comment = com.uyscuti.social.circuit.data.model.Comment(
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


            adapter!!.submitItem(comment, adapter!!.itemCount)
            updateUI(dataEmpty = false)
            if (!isFeedComment) {
                shortToComment = shortsViewModel.mutableShortsList.find { it._id == postId }
                Log.d(TAG, "onSubmit: count before ${shortToComment!!.comments}")
                if (shortToComment != null) {
                    shortToComment!!.comments += 1
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
            } else {
                val feedToComment: com.uyscuti.social.network.api.response.posts.Post? =
                    feedViewModel.getAllFeedData().find { it._id == postId }

                Log.d(TAG, "onSubmit: total before feed count is ${feedToComment?.comments}")

                val myFeedToComment: com.uyscuti.social.network.api.response.posts.Post?
                        = feedViewModel.getMyFeedData().find { it._id == postId }

                val favoriteFeedToComment: com.uyscuti.social.network.api.response.posts.Post? =
                    feedViewModel.getAllFavoriteFeedData().find { it._id == postId }

                Log.d(TAG, "onSubmit: total before feed count is ${feedToComment?.comments}")

                if (myFeedToComment != null) {
                    myFeedToComment!!.comments + 1

                    feedViewModel.getMyFeedData().forEach { feed ->
                        if (feed._id == postId) {
                            feed.comments = myFeedToComment!!.comments
                        }
                    }

                }
                if (favoriteFeedToComment != null) {
                    favoriteFeedToComment!!.comments + 1

                    feedViewModel.getAllFavoriteFeedData().forEach { feed ->
                        if (feed._id == postId) {
                            feed.comments = favoriteFeedToComment!!.comments
                        }
                    }

                }
                if (feedToComment != null) {
                    feedToComment!!.comments + 1

                    feedViewModel.getAllFeedData().forEach { feed ->
                        if (feed._id == postId) {
                            feed.comments = feedToComment!!.comments
                        }
                    }

                    val feedToComment = feedViewModel.getAllFeedData().find { it._id == postId }
                    Log.d(TAG, "onSubmit: total after feed count is ${feedToComment?.comments}")

                    EventBus.getDefault().post(FeedAdapterNotifyDatasetChanged(adapter!!.itemCount))
                }

                feedLiveDataViewModel.setBoolean(true)
            }
        } else {

            val profilePic2 = settings.getString("profile_pic", "").toString()
            val avatar = com.uyscuti.social.network.api.response.commentreply.allreplies.Avatar(
                "", "", url = profilePic2
            )
            val account = com.uyscuti.social.network.api.response.commentreply.allreplies.Account(
                _id = "", avatar = avatar, "", LocalStorage.getInstance(this).getUsername()
            )
            val commentReplyAuthor =
                com.uyscuti.social.network.api.response.commentreply.allreplies.Author(
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
                com.uyscuti.social.circuit.data.model.Comment(
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


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onAddVoiceNote() {

        val TAG = "onAddVoiceNote"
        Log.d(TAG, "onAddVoiceNote: start VN clicked")
        binding.VNLayout.visibility = View.VISIBLE
        binding.playVNRecorded.visibility = View.GONE

        startRecording()
        EventBus.getDefault().post(PauseShort(true))


    }

    // Function to hide the keyboard
    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun generateMongoDBTimestamp(): String {
        val timestamp = OffsetDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")

        return timestamp.format(formatter)
    }

    private fun addAudioComment(postId: String, content: String, audio: File) {


        createAudioMultipart(audio)


    }

    private fun createAudioMultipart(audioFile: File): MultipartBody.Part {
        // Create RequestBody from file
        val requestFile = audioFile.asRequestBody("audio/*".toMediaTypeOrNull())


        // Create MultipartBody.Part from RequestBody
        return MultipartBody.Part.createFormData("audio", audioFile.name, requestFile)
    }

    override fun onAddEmoji() {
        initView()
    }



    private fun setupCommentCountObservers() {
        if (!::commentsViewModel.isInitialized) {
            Log.w("CommentCount", "commentsViewModel not initialized, skipping observer setup")
            return
        }

        if (!::feedAdapter.isInitialized) {
            Log.w("CommentCount", "feedAdapter not initialized, skipping observer setup")
            return
        }

        // Observe immediate comment count updates
        commentsViewModel.commentCountUpdate.observe(this) { countUpdate ->
            Log.d("CommentCount", "Updating count for post ${countUpdate.postId} by ${countUpdate.increment}")

            if (::feedAdapter.isInitialized) {
                // Update the adapter's comment count map immediately
                feedAdapter.updateCommentCount(countUpdate.postId, countUpdate.increment)

                // Force UI refresh for the specific post
                feedAdapter.refreshPostCommentCount(countUpdate.postId)
            }
        }

        // Observe comment submission status
        commentsViewModel.commentSubmissionStatus.observe(this) { status ->
            when (status) {
                is ShortCommentsViewModel.CommentSubmissionStatus.Submitting -> {
                    Log.d("CommentStatus", "Comment submitting...")
                }
                is ShortCommentsViewModel.CommentSubmissionStatus.Success -> {
                    Log.d("CommentStatus", "Comment added successfully for post: ${status.postId}")

                    // Ensure UI is updated after successful submission
                    if (::feedAdapter.isInitialized) {
                        feedAdapter.notifyCommentAdded(status.postId)
                    }
                }
                is ShortCommentsViewModel.CommentSubmissionStatus.Error -> {
                    Log.e("CommentStatus", "Failed to add comment: ${status.error}")
                    Toast.makeText(this, "Failed to add comment: ${status.error}", Toast.LENGTH_SHORT).show()

                    // Refresh the specific post to show correct count
                    if (::feedAdapter.isInitialized) {
                        feedAdapter.refreshPostCommentCount(status.postId)
                    }
                }
            }
        }
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

                    // MODIFIED: The updated commentsViewModel.comment() will now automatically
                    // update the UI count immediately before making the server request
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
                            Log.d(TAG, "comment deleted successfully from Room.")
                        } else {
                            Log.d(TAG, "Failed to delete comment from Room.")
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
        val TAG = "addCommentReply"
        Log.d(TAG, "addCommentReply: inside")

        if (isInternetAvailable(this)) {

            roomCommentReplyViewModel.allCommentReplies.observe(this) {

                if (it.isNotEmpty()) {
                    Log.d(TAG, "addComment: comments in room count is ${it.size}")

                    // For replies, you might want to track parent comment count updates too
                    // This depends on your UI structure - if replies affect the main comment count
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
                            Log.d(TAG, "Reply deleted successfully from Room.")
                        } else {
                            Log.d(TAG, "Failed to delete reply from Room.")
                        }
                    }

                } else {
                    Log.d(TAG, "onSubmit: Room database has no comments")
                }
            }

        } else {
            Log.d(TAG, "addCommentReply: no internet connection")
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

            val intent = Intent(this@MainActivity, VideosActivity::class.java)
            dialog.dismiss()
            videoPickerLauncher.launch(intent)

        }

        audio?.setOnClickListener {
            val intent = Intent(this@MainActivity, AudioActivity::class.java)
            dialog.dismiss()
            audioPickerLauncher.launch(intent)


        }

        doc?.setOnClickListener {
            openFilePicker()
            dialog.dismiss()

        }
        camera?.setOnClickListener {
            val intent = Intent(this@MainActivity, CameraActivity::class.java)

            cameraLauncher.launch(intent)
            dialog.dismiss()
        }

        location?.visibility = View.INVISIBLE
        location?.setOnClickListener {

        }


        dialog.show()
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

    private fun resolveContentUriToFilePath(contentUri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursorLoader = CursorLoader(this, contentUri, projection, null, null, null)
        val cursor = cursorLoader.loadInBackground()

        return cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndex(MediaStore.Images.Media.DATA)
                it.getString(columnIndex)
            } else {
                null // Handle the error, unable to retrieve the actual path
            }
        }
    }

    private fun copyFileToInternalStorage(
        context: Context, sourceFilePath: String, destinationFileName: String
    ): File? {
        val destinationDir = context.filesDir // Use "context.cacheDir" for cache directory

        // Create the destination file
        val destinationFile = File(destinationDir, destinationFileName)

        try {
            // Open the source and destination streams
            FileInputStream(File(sourceFilePath)).use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    // Copy the file content
                    val buffer = ByteArray(4 * 1024)
                    var read: Int
                    while (inputStream.read(buffer).also { read = it } != -1) {
                        outputStream.write(buffer, 0, read)
                    }
                    outputStream.flush()
                }
            }
            return destinationFile
        } catch (e: IOException) {
            Log.e("FileOperation", "Error : ${e.message}")
            e.printStackTrace()
        }

        return null
    }

    private fun installTwitter() {
        EmojiManager.install(TwitterEmojiProvider())
    }

    private var isFeedComment = false

    override fun onCommentsClick(position: Int, data: UserShortsEntity, isFeedComment: Boolean ) {
        val TAG = "allShortComments"

        postId = data._id
        commentCount = data.comments
//        commentId = data.images

 //       isFeedComment = false

        Log.d("showBottomSheet", "showBottomSheet: inside show bottom sheet")
        val items = generateSampleData(50)
        Log.d("showBottomSheet", "showBottomSheet: postId $postId")

        adapter = CommentsRecyclerViewAdapter(this, this@MainActivity)

        adapter?.setDefaultRecyclerView(this, R.id.recyclerView)
        binding.recyclerView.itemAnimator = null

        if (adapter?.itemCount == 0) {

        }
        toggleMotionLayoutVisibility()
        adapter!!.setOnPaginationListener(object : AdPaginatedAdapter.OnPaginationListener {
            override fun onCurrentPage(page: Int) {

                Log.d(TAG, "currentPage: page number $page")

            }

            override fun onNextPage(page: Int) {
                lifecycleScope.launch(Dispatchers.Main) {
                    Log.d(TAG, "onNextPage: page number $page")
                    allShortComments(page)
                }
            }

            override fun onFinish() {
                Log.d(TAG, "finished: page number")


            }
        })

        lifecycleScope.launch(Dispatchers.Main) {
            allShortComments(adapter!!.startPage)

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


    private fun updateAdapter(
        data: com.uyscuti.social.circuit.data.model.Comment, position: Int
    ) {
        val TAG = "updateAdapter"


        Log.d("UpdateItem", "reply count visible ${data.replyCountVisible}")


        adapter?.updateItem(position, data)
    }

    private suspend fun allCommentReplies2(
        page: Int, commentId: String
    ): List<com.uyscuti.social.network.api.response.commentreply.allreplies.Comment> =
        suspendCoroutine { continuation ->
            val TAG = "allCommentReplies"
            lifecycleScope.launch(Dispatchers.IO) {

                withContext(Dispatchers.Main) {
                    // Handle UI-related tasks if needed
                }
                try {
                    Log.d(TAG, "allCommentReplies: comment id $commentId page $page")
                    val response =
                        retrofitInterface.apiService.getCommentReplies(commentId, page.toString())

                    Log.d(TAG, "allCommentReplies: $response")
                    Log.d(TAG, "allCommentReplies: ${response.errorBody()}")
                    Log.d(TAG, "allCommentReplies: ${response.message()}")
                    Log.d(TAG, "allCommentReplies: ${response.body()}")
                    val responseBody = response.body()

                    val comments = responseBody?.data?.comments ?: emptyList()
                    // responseBody.data.totalReplyComments

                    val hasNextPage = responseBody?.data?.hasNextPage ?: false

                    val uniqueCommentsList = comments.distinctBy { it._id }

                    val filteredNewItems = uniqueCommentsList.filter { newItem ->
                        commentsReplyViewModel.commentsReplyMutableList.none { existingItem -> existingItem._id == newItem._id }
                    }
                    withContext(Dispatchers.Main) {

                        commentsReplyViewModel.commentsReplyMutableList.addAll(filteredNewItems)

                        if (hasNextPage) {
                            // Continue the coroutine with the result of the recursive call
                            allCommentReplies2(page + 1, commentId).let {
                                continuation.resume((comments + it))
                            }
                        } else {
                            // Complete the coroutine with the result
                            continuation.resume(comments)
                        }

                        Log.d(TAG, "allCommentReplies: $comments")
                        Log.d(
                            TAG,
                            "allShortComments: total comments for this post: ${filteredNewItems.size}"
                        )
                        Log.d(
                            TAG, "allShortComments: total comments for this post: ${comments.size}"
                        )
                    }

                } catch (e: Exception) {
                    Log.e("UserProfileShortsViewModel", "Exception: ${e.message}")
                    lifecycleScope.launch {
                        // Handle UI-related tasks if needed
                        Toast.makeText(
                            this@MainActivity, e.message, Toast.LENGTH_LONG
                        ).show()
                    }
                    e.printStackTrace()
                }
            }
        }

    private suspend fun allCommentRepliesOnce(
        page: Int, commentId: String
    ): CommentReplyResults {
        val TAG = "allCommentReplies"
        try {
            var hasNextPage: Boolean
            val pageNumber = page + 1
            val comments: MutableList<com.uyscuti.social.network.api.response.commentreply.allreplies.Comment> =
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
                    this@MainActivity, e.message, Toast.LENGTH_LONG
                ).show()
            }
            e.printStackTrace()
        }

        return CommentReplyResults(emptyList(), false, page)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewRepliesClick(
        data: com.uyscuti.social.circuit.data.model.Comment,
        repliesRecyclerView: RecyclerView,
        position: Int
    ) {
        val TAG = "onViewRepliesClick"

    }



    @SuppressLint("SetTextI18n")
    override fun onViewRepliesClick(
        data: com.uyscuti.social.circuit.data.model.Comment,
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
                val commentWithReplies = com.uyscuti.social.circuit.data.model.Comment(
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
                        // Assuming newReply is the new reply you want to add
                        addAll(commentReplies.comments)
                    },
                    isRepliesVisible = isRepliesVisible,
                    hasNextPage = commentReplies.hasNextPage,
                    pageNumber = commentReplies.pageNumber
                )

                withContext(Dispatchers.Main) {
                    adapter?.updateItem(position, updatedComment)

                    hideCommentReplies.visibility = View.VISIBLE
                }
            }


        }
    }


    override fun onReplyButtonClick(
        position: Int, data: com.uyscuti.social.circuit.data.model.Comment
    ) {
        binding.replyToLayout.visibility = View.VISIBLE
    }

    override fun likeUnLikeComment(
        position: Int, data: com.uyscuti.social.circuit.data.model.Comment
    ) {
        val TAG = "likeUnLikeComment"

        Log.d(
            "CommentsRecyclerViewAdapter",
            "override: likeUnLikeComment: data.isLiked ${data.isLiked}"
        )

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
            "CommentsRecyclerViewAdapter",
            "override: likeUnLikeComment: likes count is ${data.likes}"
        )
        adapter?.updateItem(position, updatedComment)

        if (isInternetAvailable(this)) {
            Log.d(TAG, "override :likeUnLikeComment: internet is available")

            var result by Delegates.notNull<Boolean>()
            lifecycleScope.launch {
                result = if (isFeedComment) {
                    feedCommentLikeUnLike(data._id)

                } else {
                    commentLikeUnLike(data._id)
                }
            }
        } else {
            Log.d(TAG, "likeUnLikeComment: cant like offline")
        }

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

            }
            return false
        } catch (e: IOException) {
            Log.d(TAG, "IOException ${e.message}")
            return false
        }
    }

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

            }
            return false
        } catch (e: IOException) {
            Log.d(TAG, "IOException ${e.message}")
            return false
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun likeComment(
        event: LikeComment
    ) {
        val TAG = "likeUnLikeComment"

        val data = event.data
        val position = event.position
        Log.d(
            "CommentsRecyclerViewAdapter",
            "Subscribe: likeUnLikeComment: data.isLiked ${data.isLiked}"
        )
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
            "CommentsRecyclerViewAdapter",
            "Subscribe: likeUnLikeComment: likes count is ${data.likes}"
        )
        adapter?.updateItem(position, updatedComment)

        if (isInternetAvailable(this)) {
            Log.d(TAG, "likeUnLikeComment: internet is available")
            var result by Delegates.notNull<Boolean>()
            lifecycleScope.launch {
                result = if (isFeedComment) {
                    feedCommentLikeUnLike(data._id)
                } else {
                    commentLikeUnLike(data._id)
                }

            }
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

            Log.d(
                TAG,
                "likeUnLikeComment: item to update id ${itemToUpdate?._id} and comment reply id ${event.commentReply._id}"
            )

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

            }
            return false
        } catch (e: IOException) {
            Log.d(TAG, "IOException ${e.message}")
            return false
        }
    }

    private suspend fun feedCommentReplyLikeUnLike(commentReplyId: String): Boolean {
        val TAG = "commentReplyLikeUnLike"
        try {
            val response = retrofitInterface.apiService.likeUnLikeFeedCommentReply(commentReplyId)
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

            }
            return false
        } catch (e: IOException) {
            Log.d(TAG, "IOException ${e.message}")
            return false
        }
    }







    private fun playAudioFile(audioFile: String) {
        val TAG = "pauseRecording"
        Log.d(TAG, "playAudioFile: audio file $audioFile")
        val mediaPlayer = MediaPlayer()

        try {
            mediaPlayer.setDataSource(audioFile)
            mediaPlayer.prepare()
            mediaPlayer.start()
        } catch (e: Exception) {
            Log.d(TAG, "playAudioFile: exception ${e.message}")
            e.printStackTrace()
        }

        mediaPlayer.setOnCompletionListener { player ->
            player.release()
        }
    }

    private var commentAudioIsPlaying = false
    private var commentAudioIsPaused = false
    private var currentCommentAudioPath = ""
    private var currentCommentAudioPosition = RecyclerView.NO_POSITION
    private var isReplyVnPlaying = false
    private var isVnAudioToPlay = false

    //    private var previous
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

        Log.d(
            "toggleAudioPlayer",
            "progress received from adapter $progress is reply $isReply    is seeking $isSeeking is vn audio $isVnAudio"
        )



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

                    adapter?.setReplySecondWaveFormProgress(waveProgress, position)
                    adapter?.setSecondWaveFormProgress(waveProgress, position)
                } else {

                    adapter?.setSecondSeekBarProgress(seekBarProgress, position)
                    adapter?.setReplySecondSeekBarProgress(seekBarProgress, position)
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


            // If a new item is clicked, stop the currently playing item (if any)
            if (exoPlayer?.isPlaying == true) {
                Log.d("toggleAudioPlayer", "toggleAudioPlayer: in else player is playing")
                commentAudioPause(audioPlayPauseBtn, isReply)
            }

            if (isReply) {
                Log.d("IsReply", "is reply position $position")

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
                    if (playbackState == Player.STATE_READY && exoPlayer!!.duration != C.TIME_UNSET) {


                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    error.printStackTrace()
                    Toast.makeText(
                        this@MainActivity,
                        "Can't play this audio",
                        Toast.LENGTH_SHORT
                    ).show()

                }


            })
            if (isReplyVnPlaying) {

                val handler = Handler()

                handler.postDelayed({
                    adapter?.refreshMainComment(position)
                }, 200)
            } else {

            }


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

        exoPlayer?.pause()
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
            adapter?.setSecondWaveFormProgress(0f, currentCommentAudioPosition)
            adapter?.setReplySecondWaveFormProgress(0f, currentCommentAudioPosition)
        } else {
            adapter?.setSecondSeekBarProgress(0f, currentCommentAudioPosition)
            adapter?.setReplySecondSeekBarProgress(0f, currentCommentAudioPosition)
        }



        currentCommentAudioPosition = RecyclerView.NO_POSITION
        currentCommentAudioPath = ""
        adapter?.resetAudioPlay()


        exoPlayer?.let { exoPlayer ->
            if (exoPlayer.isPlaying) {
                exoPlayer.stop()
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
                        audioSeekBar.progress = 0
                        adapter?.refreshAudioComment(currentCommentAudioPosition)
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

    private lateinit var audioFormWave: WaveformSeekBar
    private lateinit var audioSeekBar: SeekBar
    private var leftProgress: Long = 0
    private var rightProgress: Long = 0
    private var isSeeking = false
    private var selectedAudio = ""
    private lateinit var audioDurationTVCount: TextView
    private var wavePosition = -1
    private var seekPosition = -1

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun audioWave(event: AudioPlayerHandler) {

        val TAG = "audioWave"

        audioFormWave = event.audioWave
//        event.audioWave.setSampleFrom(event.audioPath)
        audioDurationTVCount = event.leftDuration
        wavePosition = event.position
        Log.d(TAG, "audioWave: position $wavePosition ")


    }

    var maxDuration = 0L

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun commentAudioSeekBar(event: CommentAudioPlayerHandler) {
        val TAG = "commentAudioSeekBar"
        audioSeekBar = event.audioSeekBar

        audioDurationTVCount = event.leftDuration
        seekPosition = event.position
        maxDuration = event.maxDuration
        Log.d(TAG, "commentAudioSeekBar: position $wavePosition ")
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
            Toast.makeText(this@MainActivity, "Error clean cache", Toast.LENGTH_LONG).show()
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

    private var vnList = ArrayList<String>()
    private fun startPreLoadingService() {
        Log.d("VNCache", "Preloading called")
        val preloadingServiceIntent =
            Intent(this, VideoPreLoadingService::class.java)
        preloadingServiceIntent.putStringArrayListExtra(Constants.VIDEO_LIST, vnList)
        startService(preloadingServiceIntent)
    }

    @SuppressLint("NewApi")
    private val pickMultipleVideos =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(5)) { uris ->
            // Callback is invoked after the user selects media items or closes the
            // photo picker.
            if (uris.isNotEmpty()) {
                for (uri in uris) {
                    val filePath = PathUtil.getPath(
                        this,
                        uri
                    ) // Use the utility class to get the real file path
                    Log.d("VideoPicker", "File path: $filePath")
                    Log.d("VideoPicker", "File path: $isReply")
                    if (filePath != null && !isReply) {
//                        uploadVideoComment(filePath)
                    } else {
                        if (filePath != null) {
//                            uploadReplyImageComment(filePath)
                        }
                    }
                }
//                Log.d("PhotoPicker", "Number of items selected: ${uris.size}")
//                Log.d("PhotoPicker", "photo uris ${uris.toString()}")
            } else {
                Log.d("PhotoPicker", "No media selected")
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
                            val compressedImageFile = Compressor.compress(this@MainActivity, file)
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



    private fun uploadPendingComments() {
        val TAG = "Upload Pending Comments"

        // Make sure postId is initialized
        if (!::postId.isInitialized) {
            Log.d(TAG, "postId not initialized, skipping pending uploads")
            return
        }

        if (!isInternetAvailable(this)) {
            Log.d(TAG, "No internet connection, will retry later")
            return
        }

        Log.d(TAG, "Checking for pending comments for postId: $postId")

        commentFilesViewModel.allCommentFiles.observeOnce(this) { commentFiles ->
            val pendingForThisPost = commentFiles.filter {
                it.uploadId == postId &&
                        it.localPath == "audio" &&
                        it.isReply == 0
            }

            Log.d(TAG, "Found ${pendingForThisPost.size} pending comments for this post")

            if (pendingForThisPost.isNotEmpty()) {
                for (commentFile in pendingForThisPost) {
                    val file = File(commentFile.url)

                    if (file.exists()) {
                        Log.d(TAG, "Uploading pending comment: ${commentFile.fileName}")

                        val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                        val filePart = MultipartBody.Part.createFormData("audio", file.name, requestFile)
                        val video = MultipartBody.Part.createFormData("video", file.name, requestFile)
                        val image = MultipartBody.Part.createFormData("image", file.name, requestFile)
                        val docs = MultipartBody.Part.createFormData("docs", file.name, requestFile)
                        val gif = MultipartBody.Part.createFormData("gif", file.name, requestFile)
                        val thumbnail = MultipartBody.Part.createFormData("thumbnail", file.name, requestFile)

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
                            commentFile.localUpdateId,
                            fileName = commentFile.fileName,
                            fileType = commentFile.fileType,
                            duration = commentFile.duration,
                            isFeedComment = commentFile.isFeedComment
                        )

                        // Delete after upload
                        commentFilesViewModel.viewModelScope.launch {
                            commentFilesViewModel.deleteCommentById(commentFile.id)
                            Log.d(TAG, "Deleted pending comment from Room DB")
                        }
                    } else {
                        Log.e(TAG, "Pending comment file not found, deleting: ${commentFile.url}")
                        commentFilesViewModel.viewModelScope.launch {
                            commentFilesViewModel.deleteCommentById(commentFile.id)
                        }
                    }
                }
            }
        }
    }


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
            _id = "", avatar = avatar, "", LocalStorage.getInstance(this).getUsername()
        )


        val commentReplyAuthor =
            com.uyscuti.social.network.api.response.commentreply.allreplies.Author(
                _id = "21", account = account, firstName = "", lastName = ""
            )

        Log.d(TAG, "onSubmit: handle reply to a comment")

        val mongoDbTimeStamp = generateMongoDBTimestamp()
        val vnFile = CommentFiles(_id = "", url = vnToUpload, localPath = vnToUpload)

        val uploadId = generateRandomId()
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
            duration = durationString,
            uploadId = uploadId
        )

        val replyCount = data!!.replyCount + 1
        val commentWithReplies = com.uyscuti.social.circuit.data.model.Comment(
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



            commentFilesViewModel.insertCommentFile(newCommentReplyEntity)
            Log.d(TAG, "onSubmit: inserted comment $newCommentReplyEntity")

        } else {

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


        val commentReplyAuthor =
            com.uyscuti.social.network.api.response.commentreply.allreplies.Author(
                _id = "21", account = account, firstName = "", lastName = ""
            )

        Log.d("uploadReplyImageComment", "uploadReplyImageComment: handle reply to a comment")

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

            contentType = "image"
        )

        val replyCount = data!!.replyCount + 1
        val commentWithReplies = com.uyscuti.social.circuit.data.model.Comment(
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


    // Fixed uploadVnComment - creates the comment in UI and Room DB
    @RequiresApi(Build.VERSION_CODES.O)
    private fun uploadVnComment(
        vnToUpload: String,
        fileName: String,
        durationString: String,
        fileType: String,
        placeholder: Boolean = false,
        update: Boolean = false
    ) {
        Log.d("uploadVnComment", "uploadVnComment: update=$update, placeholder=$placeholder")

        val mongoDbTimeStamp = generateMongoDBTimestamp()
        val localUpdateId = generateRandomId()
        val uploadId = generateRandomId()
        val file = File(vnToUpload)

        if (file.exists()) {
            Log.d(TAG, "File exists, creating comment.......")

            val profilePic2 = settings.getString("profile_pic", "").toString()
            val avatar = Avatar("", "", url = profilePic2)
            val account = Account(_id = "", avatar = avatar, "", LocalStorage.getInstance(this).getUsername())
            val author = Author(_id = "12", account = account, firstName = "", lastName = "", avatar = null)
            val vnFile = CommentFiles(_id = "124", url = vnToUpload, localPath = vnToUpload)

            val comment = com.uyscuti.social.circuit.data.model.Comment(
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
                fileType = fileType,
                uploadId = uploadId
            )

            if (update) {
                Log.d("uploadVnComment", "updatePosition: $updatePosition")
                if (updatePosition >= 0 && updatePosition < listOfReplies.size) {
                    listOfReplies[updatePosition] = comment
                    updateAdapter(comment, updatePosition)
                }
                updatePosition = -1
                updateUI(false)
            } else {
                // Add new comment to UI
                listOfReplies.add(comment)
                updatePosition = adapter!!.itemCount
                adapter!!.submitItem(comment, adapter!!.itemCount)
                updateUI(false)

                // Update comment count
                if (!isFeedComment) {
                    shortToComment = shortsViewModel.mutableShortsList.find { it._id == postId }
                    Log.d(TAG, "uploadVnComment: count before ${shortToComment?.comments}")

                    if (shortToComment != null) {
                        shortToComment!!.comments += 1
                        shortsViewModel.mutableShortsList.forEach { short ->
                            if (short._id == postId) {
                                short.comments = shortToComment!!.comments
                            }
                        }
                        EventBus.getDefault().post(ShortAdapterNotifyDatasetChanged())
                    }
                } else {
                    val feedToComment = feedViewModel.getAllFeedData().find { it._id == postId }
                    val myFeedToComment = feedViewModel.getMyFeedData().find { it._id == postId }
                    val favoriteFeedToComment = feedViewModel.getAllFavoriteFeedData().find { it._id == postId }

                    myFeedToComment?.let {
                        feedViewModel.getMyFeedData().forEach { feed ->
                            if (feed._id == postId) {
                                feed.comments = it.comments
                            }
                        }
                    }

                    favoriteFeedToComment?.let {
                        feedViewModel.getAllFavoriteFeedData().forEach { feed ->
                            if (feed._id == postId) {
                                feed.comments = it.comments
                            }
                        }
                    }

                    feedToComment?.let {
                        feedViewModel.getAllFeedData().forEach { feed ->
                            if (feed._id == postId) {
                                feed.comments = it.comments
                            }
                        }
                        EventBus.getDefault().post(FeedAdapterNotifyDatasetChanged(adapter!!.itemCount))
                    }
                }
            }

            Log.d(TAG, "uploadVnComment: comment added to UI")

            // Insert to Room DB and upload to server
            if (!placeholder) {
                val newCommentEntity = CommentsFilesEntity(
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

                // Use coroutine to ensure insert completes before upload
                commentFilesViewModel.viewModelScope.launch {
                    try {
                        commentFilesViewModel.insertCommentFile(newCommentEntity)
                        Log.d(TAG, "uploadVnComment: inserted comment to Room DB $newCommentEntity")

                        // Wait for Room to complete the insert
                        delay(200)

                        // Call upload on main thread
                        withContext(Dispatchers.Main) {
                            addCommentVN()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error inserting comment to Room DB: ${e.message}")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, "Failed to save comment", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            recordedAudioFiles.clear()

        } else {
            Log.e(TAG, "File does not exist: $vnToUpload")
            Toast.makeText(this, "Voice note file not found", Toast.LENGTH_SHORT).show()
        }
    }


    private fun addCommentVN() {
        val TAG = "addCommentVN"

        Log.d(TAG, "addCommentVN: called for uploading to server")

        if (!isInternetAvailable(this)) {
            Log.d(TAG, "No internet connection - comment will be uploaded when online")
            Toast.makeText(this, "No internet. Comment saved and will upload when online", Toast.LENGTH_SHORT).show()
            return
        }

        // Get all pending comments from Room DB
        commentFilesViewModel.allCommentFiles.observeOnce(this) { commentFiles ->
            Log.d(TAG, "Comments in Room DB: ${commentFiles.size}")

            if (commentFiles.isEmpty()) {
                Log.d(TAG, "No pending comments to upload")
                return@observeOnce
            }

            // Process each comment
            commentFiles.forEach { commentFile ->
                // Only process audio comments that are not replies
                if (commentFile.localPath == "audio" && commentFile.isReply == 0) {
                    val file = File(commentFile.url)
                    Log.d(TAG, "Processing audio file: ${commentFile.url}, exists: ${file.exists()}")

                    if (file.exists()) {
                        try {
                            val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())

                            val filePart = MultipartBody.Part.createFormData("audio", file.name, requestFile)
                            val video = MultipartBody.Part.createFormData("video", "", "".toRequestBody())
                            val image = MultipartBody.Part.createFormData("image", "", "".toRequestBody())
                            val docs = MultipartBody.Part.createFormData("docs", "", "".toRequestBody())
                            val gif = MultipartBody.Part.createFormData("gif", "", "".toRequestBody())
                            val thumbnail = MultipartBody.Part.createFormData("thumbnail", "", "".toRequestBody())

                            Log.d(TAG, "Uploading audio comment to server for postId: ${commentFile.id}")
                            Log.d(TAG, "File details - name: ${commentFile.fileName}, type: ${commentFile.fileType}, duration: ${commentFile.duration}")

                            // Upload to server - commentAudio handles success/failure via LiveData
                            commentsViewModel.commentAudio(
                                postId = commentFile.id,
                                content = "",
                                contentType = "audio",
                                audio = filePart,
                                video = video,
                                thumbnail = thumbnail,
                                gif = gif,
                                docs = docs,
                                image = image,
                                localUpdateId = commentFile.localUpdateId,
                                fileType = commentFile.fileType,
                                fileName = commentFile.fileName,
                                duration = commentFile.duration,
                                isFeedComment = commentFile.isFeedComment
                            )

                            // Observe submission status to delete from Room DB after successful upload
                            observeCommentSubmissionStatus(commentFile)

                        } catch (e: Exception) {
                            Log.e(TAG, "Error uploading file: ${e.message}")
                            Toast.makeText(this, "Failed to upload voice note: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e(TAG, "File does not exist: ${commentFile.url}")
                        // Delete non-existent file entry
                        commentFilesViewModel.viewModelScope.launch {
                            try {
                                commentFilesViewModel.deleteCommentById(commentFile.id)
                                Log.d(TAG, "Deleted non-existent file entry from Room DB")
                            } catch (e: Exception) {
                                Log.e(TAG, "Error deleting non-existent file entry: ${e.message}")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun observeCommentSubmissionStatus(commentFile: CommentsFilesEntity) {
        val TAG = "observeCommentSubmissionStatus"

        commentsViewModel.commentSubmissionStatus.observeOnce(this) { status ->
            when (status) {
                is CommentSubmissionStatus.Success -> {
                    if (status.postId == commentFile.id) {
                        Log.d(TAG, "Upload successful for ${commentFile.fileName}, deleting from Room DB")

                        // Delete from Room DB after successful upload
                        commentFilesViewModel.viewModelScope.launch {
                            try {
                                val isDeleted = commentFilesViewModel.deleteCommentById(commentFile.id)
                                if (isDeleted) {
                                    Log.d(TAG, "Comment deleted from Room DB after successful upload")
                                } else {
                                    Log.e(TAG, "Failed to delete comment from Room DB")
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error deleting comment: ${e.message}")
                            }
                        }
                    }
                }
                is CommentSubmissionStatus.Error -> {
                    if (status.postId == commentFile.id) {
                        Log.e(TAG, "Upload failed for ${commentFile.fileName}: ${status.error}")
                        Log.d(TAG, "Keeping comment in Room DB for retry later")
                        // Don't delete - keep for retry when internet is available
                    }
                }
                is CommentSubmissionStatus.Submitting -> {
                    Log.d(TAG, "Upload in progress for ${commentFile.fileName}")
                }
                else -> {
                    Log.d(TAG, "Unknown submission status")
                }
            }
        }
    }

    fun <T> LiveData<T>.observeOnce(owner: LifecycleOwner, observer: (T) -> Unit) {
        observe(owner, object : Observer<T> {
            override fun onChanged(value: T) {
                observer(value)
                removeObserver(this)
            }
        })
    }


    private var updatePosition = -1
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
            val author =
                Author(_id = "12", account = account, firstName = "", lastName = "", avatar = null)
            val videoFile = CommentFiles(
                _id = localUpdateId,
                url = videoFilePathToUpload,
                localPath = videoFilePathToUpload
            )
            val comment = com.uyscuti.social.circuit.data.model.Comment(
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
                    updatePosition = adapter!!.itemCount
                    adapter!!.submitItem(comment, adapter!!.itemCount)
                    updateUI(false)

                }

                if (!isFeedComment) {
                    shortToComment = shortsViewModel.mutableShortsList.find { it._id == postId }
                    Log.d(
                        "uploadVideoComment",
                        "uploadVideoComment: count before ${shortToComment!!.comments}"
                    )

                    if (shortToComment != null) {
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
                } else {

                    val feedToComment = feedViewModel.getAllFeedData().find { it._id == postId }

                    Log.d(TAG, "onSubmit: total before feed count is ${feedToComment?.comments}")

                    val myFeedToComment = feedViewModel.getMyFeedData().find { it._id == postId }

                    val favoriteFeedToComment =
                        feedViewModel.getAllFavoriteFeedData().find { it._id == postId }


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
                        val feedToComment = feedViewModel.getAllFeedData().find { it._id == postId }
                        Log.d(TAG, "onSubmit: total after feed count is ${feedToComment?.comments}")
                        EventBus.getDefault()
                            .post(FeedAdapterNotifyDatasetChanged(adapter!!.itemCount))
                    }
                }
            }
        } else {
            Log.e(TAG, "File does not exist")
        }
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
                Account(_id = "", avatar = avatar, "", LocalStorage.getInstance(this).getUsername())
            val author =
                Author(_id = "12", account = account, firstName = "", lastName = "", avatar = null)
            val imageFile = CommentFiles(
                _id = "124",
                url = imageFilePathToUpload,
                localPath = imageFilePathToUpload
            )
            val comment = com.uyscuti.social.circuit.data.model.Comment(
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


            recordedAudioFiles.clear()
            if (!update) {
                listOfReplies.add(comment)
                updateUI(false)
                adapter!!.submitItem(comment, adapter!!.itemCount)
                Log.d(TAG, "uploadImageComment: added on the adapter")
                if (!isFeedComment) {
                    shortToComment = shortsViewModel.mutableShortsList.find { it._id == postId }
                    Log.d(
                        "uploadImageComment",
                        "uploadImageComment: count before ${shortToComment!!.comments}"
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
                            "uploadImageComment",
                            "onSubmit: count after ${newShortToComment!!.comments}"
                        )

                        EventBus.getDefault().post(ShortAdapterNotifyDatasetChanged())
                    }
                } else {
                    val feedToComment = feedViewModel.getAllFeedData().find { it._id == postId }
                    Log.d(TAG, "onSubmit: total before feed count is ${feedToComment?.comments}")
                    val myFeedToComment = feedViewModel.getMyFeedData().find { it._id == postId }

                    val favoriteFeedToComment =
                        feedViewModel.getAllFavoriteFeedData().find { it._id == postId }


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
                        val feedToComment = feedViewModel.getAllFeedData().find { it._id == postId }
                        Log.d(TAG, "onSubmit: total after feed count is ${feedToComment?.comments}")

                        EventBus.getDefault()
                            .post(FeedAdapterNotifyDatasetChanged(adapter!!.itemCount))

                    }
                }



            }

        } else {
            Log.e(TAG, "File does not exist")
        }


    }


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
            val author =
                Author(_id = "12", account = account, firstName = "", lastName = "", avatar = null)
            val documentFile = CommentFiles(
                _id = "124",
                url = documentFilePathToUpload,
                localPath = documentFilePathToUpload
            )
            val comment = com.uyscuti.social.circuit.data.model.Comment(
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


            recordedAudioFiles.clear()
            if (!update) {
                listOfReplies.add(comment)

                adapter!!.submitItem(comment, adapter!!.itemCount)
                updateUI(false)
                if (!isFeedComment) {
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
                } else {
                    val feedToComment = feedViewModel.getAllFeedData().find { it._id == postId }
                    val myFeedToComment = feedViewModel.getMyFeedData().find { it._id == postId }
                    val favoriteFeedToComment =
                        feedViewModel.getAllFavoriteFeedData().find { it._id == postId }
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
                        val feedToComment = feedViewModel.getAllFeedData().find { it._id == postId }
                        Log.d(TAG, "onSubmit: total after feed count is ${feedToComment?.comments}")

                        EventBus.getDefault()
                            .post(FeedAdapterNotifyDatasetChanged(adapter!!.itemCount))

                    }
                }


            }

        } else {
            Log.e(TAG, "File does not exist")
        }


    }


    private var updateReplyPosition = -1

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
            _id = "", avatar = avatar, "", LocalStorage.getInstance(this).getUsername()
        )


        val commentReplyAuthor =
            com.uyscuti.social.network.api.response.commentreply.allreplies.Author(
                _id = "21", account = account, firstName = "", lastName = ""
            )

        Log.d("uploadReplyVideoComment", "uploadReplyVideoComment: handle reply to a comment")


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
        val commentWithReplies = com.uyscuti.social.circuit.data.model.Comment(
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


        val commentReplyAuthor =
            com.uyscuti.social.network.api.response.commentreply.allreplies.Author(
                _id = "21", account = account, firstName = "", lastName = ""
            )

        Log.d("uploadReplyDocumentComment", "uploadReplyDocumentComment: handle reply to a comment")


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

            contentType = "docs",
            fileName = fileName,
            fileSize = fileSize,
            fileType = fileType,
            numberOfPages = numberOfPages.toString()
        )

        val replyCount = data!!.replyCount + 1
        val commentWithReplies = com.uyscuti.social.circuit.data.model.Comment(
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


                    }


                } else {
                    Log.d(TAG, "addVideoComment: Room database has no comments")
                }
            }


        } else {
            Log.d(TAG, "addVideoComment: no internet connection")
        }

    }


    private fun observeCommentRepliesToRefresh() {
        commentsReplyViewModel.getReplyCommentsLiveData().observe(this) { data ->
            // Handle the response data here
            for (i in listOfReplies) {

                Log.d("observeCommentRepliesToRefresh", "list of replies id ${i.localUpdateId}")
            }

            Log.d("observeCommentRepliesToRefresh", "list of replies size ${listOfReplies.size}")
            Log.d("observeCommentRepliesToRefresh", data.toString())
        }

    }

    private fun observeMainCommentToRefresh() {
        commentsViewModel.commentsObserver().observe(this) { data ->
            // Handle the response data here
            for (mainComment in listOfReplies) {

                Log.d(
                    "UpdateReplyData",
                    "list of replies id ${mainComment.localUpdateId} position ${mainComment._id}"
                )
                try {
                    if (mainComment.localUpdateId == data.localUpdateId) {
                        Log.d(
                            "UpdateReplyData",
                            "We have an equal to update on position ${mainComment._id}"
                        )
                        val position: Int = mainComment._id.toInt()
                        mainComment._id = data._id
                        updateAdapter(mainComment, position)
                        Log.d("UpdateReplyData", "observeMainCommentToRefresh: mainCommentId")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "observeMainCommentToRefresh: ${e.message}")
                    e.printStackTrace()
                }
            }

        }

    }


    @SuppressLint("SetTextI18n")
    private fun toggleReplyFromViewsActivity(
        data: com.uyscuti.social.circuit.data.model.Comment,
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

    private fun likeUnLikeCommentFromViewsActivity(
        position: Int, data: com.uyscuti.social.circuit.data.model.Comment
    ) {
        val TAG = "likeUnLikeCommentFromViewsActivity"

        Log.d(
            "likeUnLikeCommentFromViewsActivity",
            "likeUnLikeComment: data.isLiked ${data.isLiked} position $position isFeedComment: $isFeedComment"
        )

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

            var result by Delegates.notNull<Boolean>()
            lifecycleScope.launch {
                result = if (isFeedComment) {
                    feedCommentLikeUnLike(data._id)

                } else {
                    commentLikeUnLike(data._id)
                }
                Log.d(TAG, "likeUnLikeCommentFromViewsActivity server result: $result")

            }
        } else {
            Log.d(TAG, "likeUnLikeCommentFromViewsActivity: cant like offline")
        }

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

    private fun likeFeedCommentReplyFromViewsActivity(event: LikeCommentReply) {

        val TAG = "likeFeedCommentReplyFromViewsActivity"

        Log.d(
            "likeFeedCommentReplyFromViewsActivity",
            "likeCommentReplyFromViewsActivity: is liked count is ${event.commentReply.isLiked} is feed comment $isFeedComment"
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
        var result by Delegates.notNull<Boolean>()
        if (isInternetAvailable(this)) {
            Log.d(
                TAG,
                "likeCommentReplyFromViewsActivity: item to update id ${itemToUpdate._id} and comment reply id ${event.commentReply._id}"
            )
            lifecycleScope.launch {
                result = if (isFeedComment) {
                    commentReplyLikeUnLike(itemToUpdate._id)
                } else {
                    commentReplyLikeUnLike(itemToUpdate._id)
                }
                Log.d(TAG, "likeCommentReplyFromViewsActivity server result: $result")

            }
        } else {
            Log.d(TAG, "likeCommentReplyFromViewsActivity: cant like offline")
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

                    } else {
                        // Not a GIF file, handle error or inform the user
                        Log.d("selectGifLauncher", "selectGifLauncher: Selected file is not GIF")

                    }
                }
            }
        }

    private fun uploadGifToServer(gifPath: String, gifType: String) {
        val TAG = "uploadGifToServer"
        Log.d("uploadGifToServer", "uploadGifToServer: is reply $isReply")

        Log.d(TAG, "url $gifPath type $gifType")
        if (isFileExists(this, gifPath.toUri())) {

            val gif = createMultipartBody(this, gifPath.toUri(), "gif")

            addGif(
                gif!!,
                gifType,
            )

        } else {
            Log.d(TAG, "File does not exist")

        }

    }

    @kotlin.OptIn(DelicateCoroutinesApi::class)
    fun addGif(
        gif: MultipartBody.Part,
        fileType: String,
    ) {
        val TAG = "addGif"
        Log.d(TAG, "Inside addGif")
        GlobalScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Inside try block addGif")
                val fileTypePart: RequestBody = fileType
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                Log.d(TAG, "Content type is  gif type $fileType")
                val response = retrofitInterface.apiService.addGif(
                    gif = gif,
                    fileType = fileTypePart
                )
                val responseBody = response.body()
                Log.d(TAG, "addGif: response $response")
                Log.d(TAG, "addGif: response message ${response.message()}")
                Log.d(
                    TAG,
                    "addGif: response message error body ${response.errorBody()}"
                )
                Log.d(TAG, "addGif: response body $responseBody")
                Log.d(TAG, "addGif: response body data ${responseBody?.data}")
                Log.d(TAG, "addGif: response body message ${responseBody!!.message}")
                val data = responseBody.data

                Log.d(TAG, " addGif data response: $data")

            } catch (e: Exception) {
                Log.e(TAG, "addGif: $e")
                Log.e(TAG, "addGif: ${e.message}")
                e.printStackTrace()
            }
        }

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

    private fun selectGifFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/gif"
        }


        selectGifLauncher.launch(intent)
    }

    override fun onAddGif() {
        Log.d("onAddGif", "onAddGif: Gif Button Clicked")

        val intent = Intent(this, GifActivity::class.java)
        startActivityForResult(intent, GIF_CODE)

    }

    private var gifUrlType = ""

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


    private fun addGifComment() {
        val TAG = "addGifComment"
        Log.d("addGifComment", "addGifComment: is reply $isReply")



        if (isInternetAvailable(this)) {

            commentFilesViewModel.allCommentFiles.observe(this) {

                Log.d(TAG, "Comments observed size:${it.size}")

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


                    }


                } else {
                    Log.d(TAG, "onSubmit: Room database has no comments")
                }
            }


        } else {
            Log.d(TAG, "addComment: no internet connection")
        }

    }


    private var userDialogInput = ""
    private var dialogDismissed = false
    override fun onUserInputEntered(userInput: String) {
//        editText.setText(userInput)
        userDialogInput = userInput
        Toast.makeText(this, "User input: $userInput", Toast.LENGTH_SHORT).show()
    }

    override fun onDialogDismissed() {
        dialogDismissed = true
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == R_CODE && resultCode == RESULT_OK && data != null) {
            // Handle the result from the adapter
            // You can extract data from the Intent if needed
            val modifiedData =
                data.getSerializableExtra("data") as com.uyscuti.social.circuit.data.model.Comment
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


        } else if (requestCode == COMMENT_VIDEO_CODE && resultCode == RESULT_OK && data != null) {

            val modifiedData =
                data.getSerializableExtra("data") as com.uyscuti.social.circuit.data.model.Comment
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


        val commentReplyAuthor =
            com.uyscuti.social.network.api.response.commentreply.allreplies.Author(
                _id = "21", account = account, firstName = "", lastName = ""
            )

        Log.d("uploadGifImageComment", "uploadGifImageComment: handle reply to a comment")
        isReply = false


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

            contentType = "gif"
        )

        val replyCount = data!!.replyCount + 1
        val commentWithReplies = com.uyscuti.social.circuit.data.model.Comment(
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
            val author =
                Author(_id = "12", account = account, firstName = "", lastName = "", avatar = null)

            val comment = com.uyscuti.social.circuit.data.model.Comment(
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
                    isFeedComment = isFeedComment
                )
            commentFilesViewModel.insertCommentFile(newCommentEntity)
            Log.d("uploadGifComment", "uploadGifComment: inserted comment $newCommentEntity")

            Log.d("uploadGifComment", "uploadGifComment: comment $comment")
            listOfReplies.add(comment)

            recordedAudioFiles.clear()
            adapter!!.submitItem(comment, adapter!!.itemCount)

            updateUI(false)

            if (!isFeedComment) {
                shortToComment = shortsViewModel.mutableShortsList.find { it._id == postId }
                Log.d(
                    "uploadGifComment",
                    "uploadGifComment: count before ${shortToComment!!.comments}"
                )

                if (shortToComment != null) {
                    shortToComment!!.comments += 1

                    shortsViewModel.mutableShortsList.forEach { short ->
                        if (short._id == postId) {
                            short.comments = shortToComment!!.comments
                        }
                    }
                    val newShortToComment =
                        shortsViewModel.mutableShortsList.find { it._id == postId }
                    Log.d(
                        "uploadGifComment",
                        "onSubmit: count after ${newShortToComment!!.comments}"
                    )

                    EventBus.getDefault().post(ShortAdapterNotifyDatasetChanged())
                }
            } else {
                val feedToComment = feedViewModel.getAllFeedData().find { it._id == postId }

                val myFeedToComment = feedViewModel.getMyFeedData().find { it._id == postId }
                val favoriteFeedToComment =
                    feedViewModel.getAllFavoriteFeedData().find { it._id == postId }
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
                    val feedToComment = feedViewModel.getAllFeedData().find { it._id == postId }
                    Log.d(TAG, "onSubmit: total after feed count is ${feedToComment?.comments}")

                    EventBus.getDefault().post(FeedAdapterNotifyDatasetChanged(adapter!!.itemCount))

                }
            }

        } else {
            Log.e(TAG, "File does not exist")
        }


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

    private fun getUserBussinessProfile() {
        CoroutineScope(Dispatchers.IO).launch {


        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun feedEventCommentClick(event: FeedCommentClicked) {

        adapter = null
        binding.recyclerView.adapter = null
        binding.recyclerView.layoutManager = null
        Log.d(
            "feedEventCommentClick",
            "feedEventCommentClick: event bus position ${event.position}"
        )
        feedCommentClicked(event.position, event.data)
    }


    fun setAdapter() {

    }

    private fun feedCommentClicked(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.Post
    ) {
        val TAG = "feedEventCommentClick"

        val tag = "bindingAdapter"

        isFeedComment = true
        postId = data._id

        if (binding.recyclerView.adapter == null) {
            Log.d(tag, "feedCommentClick: recycler view adapter is null")
        } else {
            Log.d(tag, "feedCommentClick: recycler view adapter is not null")
        }

        if (adapter == null) {
            Log.d(TAG, "feedCommentClick: comment adapter is null")
            runOnUiThread {
                adapter = CommentsRecyclerViewAdapter(this@MainActivity, this@MainActivity)

                val layoutManager = LinearLayoutManager(this)
                binding.recyclerView.layoutManager = layoutManager
                adapter?.setRecyclerView(binding.recyclerView)
                binding.recyclerView.itemAnimator = null

                if (binding.recyclerView.layoutManager == null) {
                    Log.e(
                        TAG,
                        "feedCommentClick:  recycler adapter?.recyclerView?.layoutManager is null ${adapter?.layoutManager}"
                    )


                } else {
                    Log.d(
                        TAG,
                        "feedCommentClick:${binding.recyclerView.layoutManager} recycler adapter?.recyclerView?.layoutManager is not null ${adapter?.layoutManager}"
                    )
                    adapter?.setPagination()
                }
                if (binding.recyclerView.adapter == null) {

                    binding.recyclerView.adapter = adapter?.adapter
                } else {
                    Log.d(
                        TAG,
                        "feedCommentClick: recycler view adapter is not null ${adapter?.adapter}"
                    )
                }
            }

        } else {
            Log.d(TAG, "feedCommentClick: comment adapter is not null")
        }
        Log.d("showBottomSheet", "showBottomSheet: postId $postId")


        toggleMotionLayoutVisibility()
        adapter!!.setOnPaginationListener(object : AdPaginatedAdapter.OnPaginationListener {
            override fun onCurrentPage(page: Int) {

                Log.d(TAG, "currentPage: page number $page")
            }

            override fun onNextPage(page: Int) {
                lifecycleScope.launch(Dispatchers.Main) {

                    Log.d(TAG, "onNextPage: page number $page")
                    allFeedComments(page)
                }
            }

            override fun onFinish() {
                Log.d(TAG, "finished: page number")

            }
        })
        lifecycleScope.launch(Dispatchers.Main) {
            allFeedComments(adapter!!.startPage)

        }
        observeComments()
    }

    private fun allFeedComments(page: Int) {

        val tag = "bindingAdapter"
        lifecycleScope.launch(Dispatchers.IO) {

            withContext(Dispatchers.Main) {
                if (page == 1) {
                    showShimmer()
                } else {
                    showProgressBar()
                }
            }
            try {


                if (adapter == null) {
                    Log.d("feedEventCommentClick", "allFeedComments: comment adapter still null")
                } else {
                    Log.d("feedEventCommentClick", "allFeedComments: comment adapter not null")

                    if (binding.recyclerView.adapter == null) {
                        Log.d(
                            tag,
                            "feedCommentClick: recycler view adapter is null ${adapter?.adapter}"
                        )
                    } else {
                        Log.d(
                            tag,
                            "feedCommentClick: recycler view adapter is not null ${adapter?.adapter}"
                        )
                    }

                    val commentsWithReplies = commentViewModel.fetchFeedComments(postId, page)
                    Log.d(
                        "feedEventCommentClick",
                        "allFeedComments: size ${commentsWithReplies.size}"
                    )
                    Log.d(
                        "feedEventCommentClick",
                        "allFeedComments: isEmpty ${commentsWithReplies.isEmpty()}"
                    )
                    Log.d(
                        "feedEventCommentClick",
                        "allFeedComments: isEmpty ${commentsWithReplies[0].contentType}"
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
                        if (adapter != null) {
                            Log.d(
                                "feedEventCommentClick",
                                "allFeedComments: before submitting adapter is not null"
                            )
                            if (binding.recyclerView.layoutManager == null) {
                                Log.e("LayoutManager", "allFeedComments: layout manager null")
                                try {

                                } catch (e: Exception) {
                                    Log.e("LayoutManager", "allFeedComments: ${e.message}")
                                }

                            } else {
                                Log.i("LayoutManager", "allFeedComments: layout manager not null")
                            }

                            adapter!!.submitItems(commentsWithReplies)
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

    private fun updateUI(dataEmpty: Boolean) {
        if (dataEmpty) {
            binding.recyclerView.visibility = View.GONE
            binding.placeholderLayout.visibility = View.VISIBLE
        } else {
            binding.placeholderLayout.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }

    private var countValue = 0

    @Subscribe(threadMode = ThreadMode.MAIN)
    private fun onBadgeCountEvent(event: FlashNotificationEvent) {
        countValue++

        Log.d("onBadgeCountEvent", "onBadgeCountEvent: ")
        item.setsBadge(countValue)
    }

    private fun updateNotifications(newNotification: ArrayList<INotification>) {
        notificationViewModel.setNotifications(newNotification)
    }




}


