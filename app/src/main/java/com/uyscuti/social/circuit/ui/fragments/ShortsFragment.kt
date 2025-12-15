package com.uyscuti.social.circuit.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultDataSourceFactory
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.feedRepost.PostItem
import com.uyscuti.social.circuit.FlashApplication
import com.uyscuti.social.circuit.MainActivity
import com.uyscuti.social.circuit.adapter.CommentAdapter
import com.uyscuti.social.circuit.adapter.OnClickListeners
import com.uyscuti.social.circuit.adapter.OnCommentsClickListener
import com.uyscuti.social.circuit.adapter.OnVideoPreparedListener
import com.uyscuti.social.circuit.adapter.ShortsAdapter
import com.uyscuti.social.circuit.adapter.feed.ShareVideoAdapter
import com.uyscuti.social.circuit.eventbus.FeedFavoriteFollowUpdate
import com.uyscuti.social.circuit.eventbus.HideFeedFloatingActionButton
import com.uyscuti.social.circuit.eventbus.InformShortsFragment2
import com.uyscuti.social.circuit.eventbus.ShowFeedFloatingActionButton
import com.uyscuti.social.circuit.model.CancelShortsUpload
import com.uyscuti.social.circuit.model.FollowListItemViewModel
import com.uyscuti.social.circuit.model.GoToFeedFragment
import com.uyscuti.social.circuit.model.HandleInShortsFollowButtonClick
import com.uyscuti.social.circuit.model.HideBottomNav
import com.uyscuti.social.circuit.model.InformAdapter
import com.uyscuti.social.circuit.model.PausePlayEvent
import com.uyscuti.social.circuit.model.PauseShort
import com.uyscuti.social.circuit.model.ProgressEvent
import com.uyscuti.social.circuit.model.ProgressViewModel
import com.uyscuti.social.circuit.model.ShortAdapterNotifyDatasetChanged
import com.uyscuti.social.circuit.model.ShortsBookmarkButton
import com.uyscuti.social.circuit.model.ShortsFavoriteUnFavorite
import com.uyscuti.social.circuit.model.ShortsFollowButtonClicked
import com.uyscuti.social.circuit.model.ShortsLikeUnLike
import com.uyscuti.social.circuit.model.ShortsLikeUnLike2
import com.uyscuti.social.circuit.model.ShortsLikeUnLikeButton
import com.uyscuti.social.circuit.model.ShortsViewModel
import com.uyscuti.social.circuit.model.ShowBottomNav
import com.uyscuti.social.circuit.model.UploadSuccessful
import com.uyscuti.social.circuit.model.UserProfileShortsViewModel
import com.uyscuti.social.circuit.presentation.GetOtherUsersProfileViewModel
import com.uyscuti.social.circuit.presentation.LikeUnLikeViewModel
import com.uyscuti.social.circuit.service.VideoPreLoadingService
import com.uyscuti.social.circuit.ui.LoginActivity
import com.uyscuti.social.circuit.ui.shorts.ExoPlayerItem
import com.uyscuti.social.circuit.ui.shorts.UploadShortsActivity
import com.uyscuti.social.circuit.utils.Constants
import com.uyscuti.social.circuit.utils.removeDuplicateFollowers
import com.uyscuti.social.circuit.viewmodels.FeedShortsViewModel
import com.uyscuti.social.circuit.viewmodels.FollowUnfollowViewModel
import com.uyscuti.social.circuit.viewmodels.FollowViewModel
import com.uyscuti.social.circuit.R
import com.uyscuti.social.core.common.data.room.database.ChatDatabase
import com.uyscuti.social.core.common.data.room.entity.FollowUnFollowEntity
import com.uyscuti.social.core.common.data.room.entity.ShortsEntity
import com.uyscuti.social.core.common.data.room.entity.ShortsEntityFollowList
import com.uyscuti.social.core.common.data.room.entity.UserShortsEntity
import com.uyscuti.social.core.common.data.room.repository.ProfileRepository
import com.uyscuti.social.network.api.response.getallshorts.FollowListItem
import com.uyscuti.social.network.api.response.getallshorts.Post
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlin.math.abs


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
const val SHORTS = "Shorts Fragment"

@UnstableApi
@AndroidEntryPoint
class ShotsFragment : Fragment(), OnCommentsClickListener, OnClickListeners {

    // Fragment Parameters
    private var param1: String? = null
    private var param2: String? = null

    // UI Components - ViewPager & Adapter
    private lateinit var viewPager: ViewPager2
    private lateinit var shortsAdapter: ShortsAdapter

    // UI Components - Buttons & Actions
    private lateinit var fabAction: FloatingActionButton
    private lateinit var shortsMenu: ImageView
    private lateinit var cancelShortsUpload: ImageView

    // UI Components - Progress Indicators
    private lateinit var progressBar: ProgressBar
    private lateinit var shortsDownloadProgressBar: ProgressBar
    private lateinit var progressBarLayout: LinearLayout
    private lateinit var downloadProgressBarLayout: LinearLayout

    // UI Components - Media & Controls
    private lateinit var playerView: PlayerView
    private lateinit var shortSeekBar: SeekBar
    private lateinit var shortsDownloadImageView: ImageView

    // Data Collections
    private var videoShorts = ArrayList<ShortsEntity>()
    private var shortsList = ArrayList<String>()
    private val exoPlayerItems = ArrayList<ExoPlayerItem>()
    val uniqueEntitiesSet = HashSet<ShortsEntity>()

    // Media Player & ExoPlayer
    private var exoPlayer: ExoPlayer? = null
    private var currentPlayerListener: Player.Listener? = null
    private var isPlayerPreparing = false
    private var isUserSeeking = false

    // ExoPlayer Data Sources
    private lateinit var httpDataSourceFactory: HttpDataSource.Factory
    private lateinit var defaultDataSourceFactory: DefaultDataSourceFactory
    private lateinit var cacheDataSourceFactory: CacheDataSource.Factory
    private val simpleCache: SimpleCache = FlashApplication.cache
    private val playbackStateListener: Player.Listener = playbackStateListener()

    // Media Content & URLs
    private var videoUrl: String? = null
    private var postItem: PostItem? = null

    // UI Animations
    private var wifiAnimation: AnimationDrawable? = null

    // Position & State Management
    private var currentPosition: Int = -1

    // File Picker & Permissions
    private var getContentLauncher: ActivityResultLauncher<String>? = null
    private val PICK_VIDEO_REQUEST = "video/*"
    private val requestCode = 2024
    private val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 12
    private val MY_MANAGE_EXTERNAL_STORAGE_REQUEST_CODE = 202

    // Dependency Injection
    @Inject
    lateinit var retrofitIns: RetrofitInstance

    // ViewModels
    private val progressViewModel: ProgressViewModel by activityViewModels()
    private val likeUnLikeViewModel: LikeUnLikeViewModel by activityViewModels()
    private val shortsViewModel: ShortsViewModel by activityViewModels()
    private val followShortsViewModel: FollowListItemViewModel by viewModels()
    private val followViewModel: FollowViewModel by viewModels()
    private val followUnFollowViewModel: FollowUnfollowViewModel by viewModels()
    private val getOtherUsersProfileViewModel: GetOtherUsersProfileViewModel by viewModels()
    private val userProfileShortsViewModel: UserProfileShortsViewModel by activityViewModels()

    // Repository
    private lateinit var myProfileRepository: ProfileRepository

    // User Profile Data
    private var _id: String? = null
    private var _followUnFollowButton: AppCompatButton? = null
    private var _username: String? = null

    // Back Press & Navigation
    private val doubleBackPressThreshold = 3
    private var backPressCount = 0
    private var feedOnBackPressedData: Boolean = false
    private var feedPostPosition: Int = -1

    // Feed Business Data
    private var feedShortsBusinessId = ""
    private var feedShortsBusinessFileId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // From the  Tapped Files Viewers Post fragment
        arguments?.let { bundle ->
            videoUrl = bundle.getString("video_url")
            postItem = bundle.getParcelable("post_item")
            val postId = bundle.getString("post_id")

            Log.d("ShotsFragment", "Received video URL: $videoUrl")
            Log.d("ShotsFragment", "Received post ID: $postId")
        }

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            // Check if FEED_SHORT_BUSINESS_ID is set
            feedShortsBusinessId = it.getString(FEED_SHORT_BUSINESS_ID) ?: run {
                Log.d("openShortsFragment", "FEED_SHORT_BUSINESS_ID is not set")
                // Provide a default value or handle the absence of FEED_SHORT_BUSINESS_ID
                "default_value" // Replace "default_value" with an appropriate default or handle as needed
            }
            feedShortsBusinessFileId = it.getString(FEED_SHORT_BUSINESS_FILE_ID) ?: run {
                Log.d("openShortsFragment", "FEED_SHORT_BUSINESS_ID is not set")
                // Provide a default value or handle the absence of FEED_SHORT_BUSINESS_ID
                "default_value_file_id" // Replace "default_value" with an appropriate default or handle as needed
            }
        }
        feedOnBackPressedData = arguments?.getBoolean(FEED_ARG_DATA) == true
        feedPostPosition = arguments?.getInt(FEED_POST_POSITION)!!
        Log.d("openShortsFragment", "onCreate:feedShortsBusinessId $feedShortsBusinessId")
        EventBus.getDefault().register(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        videoUrl?.let { url ->
            setupVideoPlaybackInShots(url)
        }



//        initializeShortsViewModel()
        if (savedInstanceState == null) {
            // Register the observer only when the fragment is created, not recreated
            // observeShortsViewModel()
            Log.d("ViewModel", "onViewCreated: view not created")
        } else {
            Log.d("ViewModel", "onViewCreated: view already created")
        }
    }

    private fun setupVideoPlaybackInShots(videoUrl: String) {
        // Implement video playback logic specific to ShotsFragment
        Log.d("ShotsFragment", "Setting up video playback for: $videoUrl")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun successEvent(event: UploadSuccessful) {
        if (event.success) {
            progressBarLayout.visibility = View.GONE
            progressViewModel.totalProgress = 0
        } else {
            progressBarLayout.visibility = View.VISIBLE
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun shortAdapterNotifyDatasetChanged(event: ShortAdapterNotifyDatasetChanged) {
        val TAG = "shortAdapterNotifyDatasetChanged"
        Log.d(
            TAG,
            "shortAdapterNotifyDatasetChanged: in shorts adapter notify adapter: seh data set changed"
        )
        shortsAdapter.notifyDataSetChanged()
    }

    val count = 0

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun pausePlayEvent(event: PausePlayEvent) {
        Log.d("pausePlayEvent", "pausePlayEvent ${count + 1}")
        if (exoPlayer?.isPlaying == true) {
            pauseVideo()
        } else {
            playVideo()
        }
//        togglesPausePlay()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun pauseShort(event: PauseShort) {
        if (event.pause) {
            pauseVideo()
        }
    }

    @SuppressLint("SetTextI18n")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handleFollowButtonClick(event: HandleInShortsFollowButtonClick) {
        val tag = "handleFollowButtonClick"
        Log.d(tag, "handleFollowButtonClick: inside")
        val connectivityManager =
            requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkInfo = connectivityManager.activeNetworkInfo
        val isConnected = networkInfo != null && networkInfo.isConnected

        event.followButton.setOnClickListener {
            Log.d(tag, "handleFollowButtonClick: button clicked")
            val isFollowing = event.followButton.text != "Following"

            // Update the Room database with the new follow status
            val newFollowEntity = FollowUnFollowEntity(event.userId, isFollowing)

//            EventBus.getDefault().post(FeedFavoriteFollowUpdate(event.userId, isFollowing))
            followViewModel.insertOrUpdateFollow(newFollowEntity)
        }

        // Observe the follow status and update UI accordingly
        followViewModel.getFollowStatus(event.userId)
            .observe(this) { followEntity ->
                followEntity?.let {
                    if (it.isFollowing) {
                        // User is currently following, update UI accordingly
                        event.followButton.text = "Following"
//                        binding.followIcon.setImageResource(R.drawable.notifications_svgrepo_com_fill)
                        event.followButton.setBackgroundResource(R.drawable.shorts_following_button)

                        if (!isConnected) {
                            Log.d(tag, "handleFollowButtonClick: no internet connection")
                        } else {
                            Log.d(tag, "handleFollowButtonClick: internet connected")
                            followUnFollowViewModel.followUnFollow(event.userId)
                            getOtherUsersProfileViewModel.viewModelScope.launch {
                                delay(500)
                                getOtherUsersProfileViewModel.getOtherUsersProfile(event.username)
                            }
                            followUnFollowViewModel.viewModelScope.launch {
                                val isDeleted = followViewModel.deleteFollowById(event.userId)
                                if (isDeleted) {
                                    // Deletion was successful, update UI or perform other actions
                                    Log.d(tag, "Follow deleted successfully.")
                                } else {
                                    // Deletion was not successful, handle accordingly
                                    Log.d(tag, "Failed to delete follow.")
                                }
                            }
                        }

                    } else {
                        // User is not following, update UI accordingly
                        event.followButton.text = "Follow"
                        event.followButton.setBackgroundResource(R.drawable.shorts_follow_button_border)
//                        binding.followIcon.setImageResource(R.drawable.notification_follow_bluejeans)
                        if (!isConnected) {
                            Log.d(tag, "handleFollowButtonClick: no internet connection")
                        } else {
                            Log.d(tag, "handleFollowButtonClick: internet connected")
                            followUnFollowViewModel.followUnFollow(event.userId)
//                            viewModel.getOtherUsersProfile(fromShortsUserAccount!!.username)

                            getOtherUsersProfileViewModel.viewModelScope.launch {
                                delay(500)
                                getOtherUsersProfileViewModel.getOtherUsersProfile(event.username)
                            }

                            followUnFollowViewModel.viewModelScope.launch {
                                val isDeleted = followViewModel.deleteFollowById(event.userId)
                                if (isDeleted) {
                                    // Deletion was successful, update UI or perform other actions
                                    Log.d(tag, "Follow deleted successfully.")
                                } else {
                                    // Deletion was not successful, handle accordingly
                                    Log.d(tag, "Failed to delete follow.")
                                }
                            }
                        }

                    }
                }
            }

        getOtherUsersProfileViewModel.getUserProfileShortsObserver().observe(
            this
        ) { userProfileData ->
//                    binding.posts.text = userProfileData.f
//            binding.followersCount.text = "${userProfileData!!.followersCount}"
//            binding.followingCount.text = "${userProfileData.followingCount}"
//            binding.userBioText.text = userProfileData.bio
            if (userProfileData!!.isFollowing) {
                event.followButton.text = "Following"
            } else {
                event.followButton.text = "Follow"
            }

            Log.d(tag, "initUser followers count: ${userProfileData.followersCount}")
        }
    }

    private val eventProgressSets = HashMap<String, HashSet<Int>>()

    //    private var totalProgress = 0
    private val TOTAL_PROGRESS = 200

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onProgressEvent(event: ProgressEvent) {
        // Check if the progress set for this event exists, if not, create it
        val progressSet = eventProgressSets.getOrPut(event.eventId) { HashSet() }

        // Check if the progress value is unique for this event
        if (progressSet.add(event.progress)) {
            // Update your UI with the progress value
//            Log.d("Progress", "Event ${event.eventId} - Progress ${event.progress}")

//            totalProgress += 1  // Increment by 1 for each unique progress value
            progressViewModel.totalProgress += 1
            // Assuming you have a known total value for the progress (e.g., 200)
            var overallProgress =
                (progressViewModel.totalProgress.toDouble() / TOTAL_PROGRESS) * 100

            Log.d(
                "Progress",
                "Overall Progress: $overallProgress - total progress ${progressViewModel.totalProgress} event id - ${event.eventId}"
            )

            progressBarLayout.visibility = View.VISIBLE
            progressBar.progress = overallProgress.toInt()
            wifiAnimation!!.start()
            cancelShortsUpload.setOnClickListener {
                Log.d("Cancel", "Cancel upload clicked")
                EventBus.getDefault().post(CancelShortsUpload(true))
                progressBarLayout.visibility = View.GONE
                wifiAnimation!!.stop()
                eventProgressSets.clear()
                progressSet.clear()
                progressViewModel.totalProgress = 0
                overallProgress = 0.0
            }

            // Check if the progress is 100%, and hide the ProgressBar
            if (overallProgress.toInt() >= 100) {
                Log.d("overallProgress", "onProgressEvent:$overallProgress ")
                progressViewModel.totalProgress = 0
                overallProgress = 0.0
                wifiAnimation!!.stop()
                eventProgressSets.clear()
                progressSet.clear()
                progressBarLayout.visibility = View.GONE

            } else {


            Log.d("Progress", "Event ${event.eventId} - Duplicate progress value: ${event.progress}")
            }
        } else {


            Log.d("Progress", "Event ${event.eventId} - Duplicate progress value: ${event.progress}")
        }
    }

    private fun updateSeekBar() {
        exoPlayer?.let { player ->
            if (!isUserSeeking) {
                val currentPosition = player.currentPosition.toInt()
                shortSeekBar.progress = currentPosition
            }
        }
    }

    private val feesShortsSharedViewModel: FeedShortsViewModel by activityViewModels()

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.Q)
    @OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        context?.theme?.applyStyle(R.style.DarkTheme)
        val TAG = "onCreateView"
        val view = inflater.inflate(R.layout.shorts_fragment, container, false)
        activity?.window?.statusBarColor = ContextCompat.getColor(requireContext(), R.color.black)
//        activity?.windowLightStatusBar
        // Set the navigation bar color dynamically
        activity?.window?.navigationBarColor =
            ContextCompat.getColor(requireContext(), R.color.black)



        myProfileRepository =
            ProfileRepository(ChatDatabase.getInstance(requireActivity()).profileDao())
        progressBar = view.findViewById(R.id.progressBar)
        shortsDownloadProgressBar = view.findViewById(R.id.shortsDownloadProgressBar)
        progressBarLayout = view.findViewById(R.id.progressBarLayout)
        downloadProgressBarLayout = view.findViewById(R.id.downloadProgressBarLayout)
        shortsDownloadImageView = view.findViewById(R.id.shortsDownloadImageView)
        cancelShortsUpload = view.findViewById(R.id.cancelShortsUpload)
        playerView = view.findViewById(R.id.playerView)
        shortSeekBar = view.findViewById(R.id.shortSeekBar)
        shortsMenu = view.findViewById(R.id.shortsMenu)


        shortsMenu.setOnClickListener {
            val popupMenu = PopupMenu(context, shortsMenu)
            popupMenu.menuInflater.inflate(R.menu.shorts_menu_item, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                // Handle menu item click here
                when (menuItem.itemId) {
                    R.id.menu_setting -> {
                        // Handle menu item 1 click
                        true
                    }

                    R.id.logout -> {
                        // Handle menu item 2 click
                        performLogout()
                        true
                    }
                    // Handle other menu items if needed
                    else -> false
                }
            }
            popupMenu.show()
        }
        shortSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Update playback position when user drags the SeekBar
                if (fromUser) {
                    exoPlayer?.seekTo(progress.toLong())
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


//        getAllShorts()

        // Initialize the launcher
        getContentLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    openUploadShortsActivity(uri)
                }
            }

        (activity as? MainActivity)?.hideAppBar()

        viewPager = view.findViewById(R.id.shortsViewPager)

        httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)

        defaultDataSourceFactory = DefaultDataSourceFactory(
            requireContext(), httpDataSourceFactory
        )


        cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(simpleCache)
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        val mediaSourceFactory: MediaSource.Factory =
            DefaultMediaSourceFactory(requireContext())
                .setDataSourceFactory(cacheDataSourceFactory)
        exoPlayer = ExoPlayer.Builder(requireActivity())
            .setMediaSourceFactory(mediaSourceFactory)
            .build()

        val tag = "handleFollowButtonClick"
//        Log.d(tag, "handleFollowButtonClick: inside")
        val connectivityManager =
            requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkInfo = connectivityManager.activeNetworkInfo
        val isConnected = networkInfo != null && networkInfo.isConnected

        lifecycleScope.launch(Dispatchers.IO) {
//            val entities = shortsViewModel.allShortsList

            val followEntity = followShortsViewModel.allShortsList

            withContext(Dispatchers.Main) {
                shortsViewModel.allShortsList.observe(viewLifecycleOwner, Observer {
                    // Update your UI with the shortsList
                    for (entity in it) {
                        Log.d("PreLoad", "onCreateView: $it ")
//                        videoShorts.add(entity)
                    }
                })
            }
            if (!shortsViewModel.isResuming) {
                loadMoreShortsPage1(1)
                Log.d("Resume", "onCreateView: ! ${!shortsViewModel.isResuming}")
            }


            shortsAdapter =
                ShortsAdapter(requireActivity() as OnCommentsClickListener, this@ShotsFragment,
                    exoPlayer!!, object :

                        OnVideoPreparedListener {
                        override fun onVideoPrepared(exoPlayerItem: ExoPlayerItem) {
                            exoPlayerItems.add(exoPlayerItem)
                        }
                    }) { id, username, followButton ->
                }

            withContext(Dispatchers.Main) {
                viewPager.adapter = shortsAdapter
                viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL

                if (shortsViewModel.isResuming && shortsViewModel.mutableShortsList.isEmpty()) {
                    loadMoreShortsPage1(1)
                } else if (shortsViewModel.isResuming) {
//                        Log.d("Resume", "onCreateView is resuming: ${shortsViewModel.isResuming}")

                    lifecycleScope.launch {
//                        Log.d("Resume", "resuming :${shortsViewModel.isResuming}")
//                        Log.d("Resume", "resuming short index:${shortsViewModel.shortIndex}")
//                        Log.d("Resume", "resuming page number:${shortsViewModel.pageNumber}")

                        Log.d(
                            "Resume",
                            "onCreateView: shorts view model size ${shortsViewModel.followList.size}"
                        )
//                            shortsViewModel.mutableShortsList.addAll(shortsEntity)
                        shortsAdapter.addData(shortsViewModel.mutableShortsList)
                        shortsAdapter.addIsFollowingData(shortsViewModel.followList)


                    }

//                        viewPager.currentItem = shortsViewModel.shortIndex
                    currentPosition = shortsViewModel.shortIndex

                    if(feedShortsBusinessId != "default_value" ) {
//                        Log.d("feedShortsBusinessId", "before:feedShortsBusinessId $feedShortsBusinessId shorts total ${shortsViewModel.mutableShortsList.size} feedShortsBusinessFileId $feedShortsBusinessFileId")
                        loadMoreShortsByFeedShortsBusinessId(feedShortsBusinessId)
//                        Log.d("feedShortsBusinessId", "after(1):feedShortsBusinessId $feedShortsBusinessId shorts total ${shortsViewModel.mutableShortsList.size} feedShortsBusinessFileId $feedShortsBusinessFileId")

                        val handler = Handler(Looper.getMainLooper())

                        handler.postDelayed({
                            viewPager.setCurrentItem(shortsViewModel.mutableShortsList.size, false)
                            playVideoAtPosition(shortsViewModel.mutableShortsList.size)
                        }, 200)

//                        Log.d("feedShortsBusinessId", "after(2):feedShortsBusinessId $feedShortsBusinessId not empty play last video : shorts total ${shortsViewModel.mutableShortsList.size} feedShortsBusinessFileId $feedShortsBusinessFileId")


                        for(i in shortsViewModel.mutableShortsList){
                            Log.d("feedShortsBusinessId", "feedShortsBusinessId(i): ${i.feedShortsBusinessId}")
                        }

                    }else {
                        viewPager.setCurrentItem(currentPosition, false)
                        playVideoAtPosition(currentPosition)
                    }



                }
                playerView.player = exoPlayer

                playerView.setOnClickListener {
                    Log.d("Pause", "Player view pause clicked")
                    togglesPausePlay()
                }

                viewPager.registerOnPageChangeCallback(object :
                    ViewPager2.OnPageChangeCallback() {

                    @OptIn(UnstableApi::class)
                    override fun onPageSelected(position: Int) {
//                            exoPlayer!!.stop()
//                            exoPlayer!!.seekTo(0)
//
                        shortsViewModel.shortIndex = position
                        exoPlayer!!.stop()
                        exoPlayer!!.seekTo(0)
//                            exoPlayer.get

                        // Set track selection parameters
                        exoPlayer!!.trackSelectionParameters = exoPlayer!!.trackSelectionParameters
                            .buildUpon()
                            .setMaxVideoSizeSd()
                            .build()

                        playVideoAtPosition(position)

                    }

                    override fun onPageScrolled(
                        position: Int,
                        positionOffset: Float,
                        positionOffsetPixels: Int
                    ) {
                        super.onPageScrolled(position, positionOffset, positionOffsetPixels)

                        if (position > shortsViewModel.lastPosition) {
                            // User is scrolling down
                            // Handle scroll down logic
                            Log.d(
                                "showHideBottomNav",
                                "onPageScrolled: pos $position:::last pos::${shortsViewModel.lastPosition} "
                            )
                            EventBus.getDefault().post(HideBottomNav())
                            EventBus.getDefault().post(HideFeedFloatingActionButton()) // Hide FAB
                            Log.d("showHideBottomNav", "event post scroll next")
                        } else if (position < shortsViewModel.lastPosition) {
                            // User is scrolling up
                            // Handle scroll up logic
                            Log.d(
                                "showHideBottomNav",
                                "onPageScrolled: pos $position:::last pos::${shortsViewModel.lastPosition} "
                            )
                            EventBus.getDefault().post(ShowFeedFloatingActionButton(false)) // Show FAB
                            EventBus.getDefault().post(ShowBottomNav(false))
                            Log.d("showHideBottomNav", "event post scroll previous")
                        }
                        if (position > shortsViewModel.lastPosition) {
                            // User is scrolling down
                            loadMoreVideosIfNeeded(position)

                        }

                        shortsViewModel.lastPosition = position

                        if (positionOffset > 0.5) {
                            // User is scrolling towards the end, update the current position to the next video
                            currentPosition = position + 1


                        } else if (positionOffset < -0.5) {

                            currentPosition = position - 1

                        } else {
                            // User is in a stable position, update the current position to the current video
                            currentPosition = position


                        }

                    }
                    override fun onPageScrollStateChanged(state: Int) {
                        super.onPageScrollStateChanged(state)
                        Log.d("onPageScrollStateChanged", "onPageScrollStateChanged: state $state")

                        // Check if the scroll state is idle
                        if (state == ViewPager.SCROLL_STATE_SETTLING) {
                            Log.d(
                                "onPageScrollStateChanged",
                                "onPageScrollStateChanged: state $state"
                            )
                            // The scroll state is idle, play the video at the updated position
                            playVideoAtPosition(currentPosition)
                            backPressCount = 0
                        }
                    }
                })
            }
        }
        // Find FloatingActionButton
        fabAction = view.findViewById(R.id.fabAction)

        // Set up FloatingActionButton click listener
        fabAction.setOnClickListener {

            getContentLauncher?.launch(PICK_VIDEO_REQUEST)
        }

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                if(feedOnBackPressedData) {
                    Log.d("handleOnBackPressed", "handleOnBackPressed: feedPostPosition $feedPostPosition")
                    EventBus.getDefault().post(GoToFeedFragment(feedPostPosition))
                }else {
                    if (backPressCount == 0) {


                        EventBus.getDefault().post(ShowBottomNav(false))
                        backPressCount++

                        Log.d("handleOnBackPressed", "handleOnBackPressed: 1 - display bottom nav ")

                    }
                    else if (backPressCount == 1) {
                        backPressCount++
                        Log.d(
                            "handleOnBackPressed",
                            "handleOnBackPressed: current position $currentPosition"
                        )
                        shortsViewModel.lastPosition = currentPosition + 2

                        currentPosition += 1
                        Log.d(
                            "handleOnBackPressed",
                            "handleOnBackPressed: current position $currentPosition"
                        )
                        Log.d(
                            "handleOnBackPressed",
                            "handleOnBackPressed: last/prev position ${shortsViewModel.lastPosition}"
                        )
                        EventBus.getDefault().post(ShowBottomNav(false))
                        viewPager.setCurrentItem(currentPosition, false)
                        playVideoAtPosition(currentPosition)


                        Log.d("handleOnBackPressed", "handleOnBackPressed: 2 - next short")
                    }
                    else if (backPressCount == 2) {
                        Log.d(
                            "handleOnBackPressed",
                            "handleOnBackPressed: last/prev position ${shortsViewModel.lastPosition}"
                        )
                        EventBus.getDefault().post(ShowBottomNav(false))
                        backPressCount++

                        if (backPressCount >= doubleBackPressThreshold) {
                            // Reset back press count
                            backPressCount = 0
                            // Finish the activity to exit the app
                            requireActivity().finish()
                        }

                    }
                    else {
                        Log.d("handleOnBackPressed", "handleOnBackPressed: else")

                    }
                }

            }
        }
        // Add the callback to the onBackPressedDispatcher
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )


// Observe data from ViewModel
        feesShortsSharedViewModel.data.observe(viewLifecycleOwner) { newData ->
            Log.d("feedShortsSharedViewModel", "feedShortsSharedViewModel: in shorts $newData")

            val followListItem: List<ShortsEntityFollowList> = listOf(
                ShortsEntityFollowList(
                    newData.userId, newData.isFollowing
                )
            )

            lifecycleScope.launch(Dispatchers.IO) {

                val uniqueFollowList = removeDuplicateFollowers(followListItem)

                Log.d(
                    "feedShortsSharedViewModel",
                    "feedShortsSharedViewModel: Inserted uniqueFollowList in shorts: $uniqueFollowList"
                )
                delay(100)
                followShortsViewModel.insertFollowListItems(uniqueFollowList)
                shortsViewModel.followList.add(
                    ShortsEntityFollowList(
                        newData.userId,
                        newData.isFollowing
                    )
                )

            }

            lifecycleScope.launch(Dispatchers.Main) {

                followShortsViewModel._followListItems.observe(viewLifecycleOwner) {
                    shortsAdapter.addIsFollowingData(it)
                    shortsAdapter.notifyDataSetChanged()
                    Log.d(
                        "feedShortsSharedViewModel",
                        "feedShortsSharedViewModel: size: ${it.size} in shorts: $it "
                    )
                }
            }
        }
        return view
    }



    private fun playVideoAtPosition(position: Int) {
        val videoShorts = shortsViewModel.videoShorts

        // Validate position bounds
        if (position < 0 || position >= videoShorts.size) {
            Log.e("playVideoAtPosition", "Invalid position: $position, size: ${videoShorts.size}")
            return
        }

        // Prevent multiple simultaneous preparations
        if (isPlayerPreparing) {
            Log.d("playVideoAtPosition", "Player is already preparing, ignoring request")
            return
        }

        val shortVideo = videoShorts[position]
        Log.d("playVideoAtPosition", "Playing video for: ${shortVideo.author.account.username}")
        Log.d("playVideoAtPosition", "Video data: $shortVideo")
        Log.d("playVideoAtPosition", "Total videos: ${videoShorts.size}")

        // Get video URL from the shortVideo object
        val rawVideoUrl = shortVideo.images.firstOrNull()?.url

        if (rawVideoUrl.isNullOrEmpty()) {
            Log.e("playVideoAtPosition", "Video URL is null or empty at position $position")
            return
        }

        // Determine the correct video URL based on the content
        val finalVideoUrl = when {
            // If it's already a complete HTTP URL, use it as is
            rawVideoUrl.startsWith("http://") || rawVideoUrl.startsWith("https://") -> {
                rawVideoUrl
            }
            // If it's a relative path for mixed files, construct the mixed files URL
            rawVideoUrl.contains("mixed_files") || rawVideoUrl.contains("temp") -> {
                val serverBaseUrl = "http://192.168.1.103:8080/feed_mixed_files/"
                serverBaseUrl + rawVideoUrl.trimStart('/')
            }
            // For regular images/videos, use the images endpoint
            else -> {
                val serverBaseUrl = "http://192.168.1.103:8080/"
                if (rawVideoUrl.startsWith("/")) {
                    serverBaseUrl + rawVideoUrl.trimStart('/')
                } else {
                    serverBaseUrl + rawVideoUrl
                }
            }
        }

        Log.d("playVideoAtPosition", "Raw video URL: $rawVideoUrl")
        Log.d("playVideoAtPosition", "Final video URL: $finalVideoUrl")

        // Validate and play the video
        validateAndPlayVideo(finalVideoUrl, position)
    }

    private fun validateAndPlayVideo(videoUrl: String, position: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // First, check if the URL is reachable and get content info
                val urlConnection = URL(videoUrl).openConnection()
                urlConnection.connectTimeout = 5000 // Increased timeout
                urlConnection.readTimeout = 5000

                val contentType = urlConnection.contentType
                val contentLength = urlConnection.contentLength

                Log.d("VideoValidation", "URL: $videoUrl")
                Log.d("VideoValidation", "Content-Type: $contentType")
                Log.d("VideoValidation", "Content-Length: $contentLength")

                // Check if content type indicates a video
                if (contentType != null && !contentType.startsWith("video/")) {
                    Log.e("VideoValidation", "Content is not a video: $contentType")
                    withContext(Dispatchers.Main) {
                        handlePlaybackError(position)
                    }
                    return@launch
                }

                // Check if content length is reasonable (not empty, not too small)
                if (contentLength <= 0 || contentLength < 1024) { // Less than 1KB probably not a valid video
                    Log.e("VideoValidation", "Content length too small or invalid: $contentLength")
                    withContext(Dispatchers.Main) {
                        handlePlaybackError(position)
                    }
                    return@launch
                }

                // If validation passes, proceed with playback on main thread
                withContext(Dispatchers.Main) {
                    isPlayerPreparing = true
                    prepareAndPlayVideo(videoUrl, position)
                }

            } catch (e: Exception) {
                Log.e("VideoValidation", "URL validation failed for: $videoUrl", e)
                withContext(Dispatchers.Main) {
                    handlePlaybackError(position)
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun prepareAndPlayVideo(videoUrl: String, position: Int) {
        try {
            val videoUri = Uri.parse(videoUrl)
            Log.d("prepareAndPlayVideo", "Preparing video URI: $videoUri")

            // Create MediaItem with explicit MIME type detection
            val mediaItem = MediaItem.Builder()
                .setUri(videoUri)
                .apply {
                    // Let ExoPlayer auto-detect MIME type, but provide fallback
                    val detectedMimeType = getMimeTypeFromUrl(videoUrl)
                    if (detectedMimeType != MimeTypes.VIDEO_UNKNOWN) {
                        setMimeType(detectedMimeType)
                    }
                }
                .build()

            // Create media source with enhanced configuration
            val mediaSource = createEnhancedMediaSource(mediaItem, videoUrl)

            // Create and set up the player listener
            currentPlayerListener = createPlayerListener(position)

            exoPlayer?.let { player ->
                // Remove any existing listeners
                currentPlayerListener?.let { oldListener ->
                    player.removeListener(oldListener)
                }

                // Add the new listener
                player.addListener(currentPlayerListener!!)

                // Configure player settings
                player.repeatMode = Player.REPEAT_MODE_ONE
                player.playWhenReady = true

                // Set media source and prepare
                player.setMediaSource(mediaSource)
                player.prepare()

                Log.d("prepareAndPlayVideo", "Video preparation started for position: $position")
            }
        } catch (e: Exception) {
            Log.e("prepareAndPlayVideo", "Error in prepareAndPlayVideo", e)
            isPlayerPreparing = false
            handlePlaybackError(position)
        }
    }

    private fun getMimeTypeFromUrl(url: String): String {
        val lowerUrl = url.lowercase()
        return when {
            lowerUrl.contains(".mp4") -> MimeTypes.VIDEO_MP4
            lowerUrl.contains(".mov") -> "video/quicktime"
            lowerUrl.contains(".avi") -> "video/x-msvideo"
            lowerUrl.contains(".mkv") -> "video/x-matroska"
            lowerUrl.contains(".webm") -> "video/webm"
            lowerUrl.contains(".3gp") -> "video/3gpp"
            else -> MimeTypes.VIDEO_MP4 // Default fallback
        }
    }

    @OptIn(UnstableApi::class)
    private fun createEnhancedMediaSource(mediaItem: MediaItem, videoUrl: String): MediaSource {
        return try {
            // Create a more robust data source factory
            val dataSourceFactory = createRobustDataSourceFactory()

            // Try different media source factories based on URL characteristics
            // For most MP4 files, use ProgressiveMediaSource
            ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mediaItem)
        } catch (e: Exception) {
            Log.e("createEnhancedMediaSource", "Error creating enhanced media source", e)
            // Ultimate fallback - basic progressive source with default factory
            val defaultFactory = DefaultDataSource.Factory(requireContext())
            ProgressiveMediaSource.Factory(defaultFactory)
                .createMediaSource(mediaItem)
        }
    }

    @OptIn(UnstableApi::class)
    private fun createRobustDataSourceFactory(): DataSource.Factory {
        return try {
            // Create cache data source factory with fallback
            val cacheFactory = if (::cacheDataSourceFactory.isInitialized) {
                cacheDataSourceFactory
            } else {
                Log.w("createRobustDataSourceFactory", "Cache factory not initialized, using default")
                DefaultDataSource.Factory(requireContext())
            }

            // Wrap with retry mechanism
            cacheFactory
        } catch (e: Exception) {
            Log.e("createRobustDataSourceFactory", "Error creating robust data source factory", e)
            DefaultDataSource.Factory(requireContext())
        }
    }

    private fun createPlayerListener(position: Int): Player.Listener {
        return object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)

                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        Log.d("PlayerState", "Buffering video at position: $position")

                    }
                    Player.STATE_READY -> {
                        Log.d("PlayerState", "Video ready at position: $position")
                        isPlayerPreparing = false


                        exoPlayer?.let { player ->
                            if (player.duration != C.TIME_UNSET) {
                                shortSeekBar.max = player.duration.toInt()
                                Log.d("PlayerState", "Duration set: ${player.duration}")
                            }
                            player.play()
                        }
                    }
                    Player.STATE_ENDED -> {
                        Log.d("PlayerState", "Video ended at position: $position")
                    }
                    Player.STATE_IDLE -> {
                        Log.d("PlayerState", "Player idle at position: $position")
                        isPlayerPreparing = false
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                isPlayerPreparing = false

                Log.e("PlayerError", "Playback error at position $position", error)
                Log.e("PlayerError", "Error cause: ${error.cause}")
                Log.e("PlayerError", "Error message: ${error.message}")
                Log.e("PlayerError", "Error code: ${error.errorCode}")

                // Enhanced error handling with specific messages
                when (error.errorCode) {
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> {

                        Log.e("PlayerError", "Network error: ${error.message}")
                    }
                    PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED,
                    PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED -> {

                        Log.e("PlayerError", "Format error: ${error.message}")
                    }
                    PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> {

                        Log.e("PlayerError", "File not found: ${error.message}")
                    }
                    PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED -> {

                        Log.e("PlayerError", "Unsupported container: ${error.message}")
                    }
                    PlaybackException.ERROR_CODE_DECODER_INIT_FAILED -> {

                        Log.e("PlayerError", "Decoder init failed: ${error.message}")
                    }
                    else -> {
                        // Check if it's the specific UnrecognizedInputFormatException
                        if (error.cause is androidx.media3.exoplayer.source.UnrecognizedInputFormatException) {

                            Log.e("PlayerError", "Unrecognized input format - file may be corrupted")
                        } else {

                            Log.e("PlayerError", "Unknown error: ${error.message}")
                        }
                    }
                }

                // Enhanced recovery mechanism
                handlePlaybackError(position)
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                super.onPositionDiscontinuity(oldPosition, newPosition, reason)
                updateSeekBar()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                Log.d("PlayerState", "Is playing: $isPlaying at position: $position")

            }
        }
    }

    private fun handlePlaybackError(position: Int) {
        lifecycleScope.launch {
            delay(1000) // Wait before trying next action

            // Try to skip to next video
            val nextPosition = if (position + 1 < shortsViewModel.videoShorts.size) {
                position + 1
            } else {
                0 // Loop back to first video
            }

            // Also update the current position in your view model or adapter
            // to reflect that this video failed to play
            markVideoAsFailedToLoad(position)

            Log.d("ErrorRecovery", "Trying to play video at position: $nextPosition")
            playVideoAtPosition(nextPosition)
        }
    }

    private fun markVideoAsFailedToLoad(position: Int) {
        // You might want to add this functionality to mark videos as failed
        // so you don't keep trying to play them
        Log.d("ErrorRecovery", "Marking video at position $position as failed to load")
        // Implementation depends on your data structure
    }


    override fun onDestroyView() {
        super.onDestroyView()
        releasePlayer()
        exoPlayer?.pause()
        val TAG = "onDestroyView"
//        bottomNavigationView = requireActivity().findViewById(R.id.bottomNavigationView)
//        Log.d(TAG, "onDestroyView: shorts fragment")
//        val parent = bottomNavigationView.parent as? CoordinatorLayout
//        parent?.removeView(bottomNavigationView)


        lifecycleScope.launch {
            shortsViewModel.isResuming = true
        }
    }

    fun loadMoreVideosIfNeeded(position: Int) {
        // Check if the position meets the condition
        if (position >= 5 && (position - 5) % 5 == 0) {
            // Calculate the parameter to pass to loadMoreVideos based on the position
            val loadMoreValue = 2 + (position - 5) / 5
            // Call loadMoreVideos with the calculated value
            loadMoreVideos(loadMoreValue)
        }
    }

    private fun loadMoreVideos(pageNumber: Int) {
        // Call the function that makes a request to the server for more videos
        lifecycleScope.launch(Dispatchers.IO) {
            // Your code to fetch more videos goes here
            Log.d("TAG", "Call for more videos")
            // For example, you can update the entities list and notify the adapter
            // with the new data received from the server
            // val newEntities = // fetch new entities from the server
            // shortsViewModel.updateShortsList(newEntities)
//            getShorts(pageNumber)
            delay(50)
            loadMoreShorts(pageNumber)
            shortsViewModel.pageNumber = pageNumber
            lifecycleScope.launch(Dispatchers.Main) {
            }
//            if (!shortsCalled) {
//
////                shortsCalled = true
//            } else {
////                Log.d("PlayerActivity", "get all shorts already called")
//            }
            // Notify the adapter with the updated data
//            withContext(Dispatchers.Main) {
////                shortsAdapter.notifyDataSetChanged()
////                shortsAdapter.notifyItemRangeInserted(9, 10)
//            }
        }
    }

    private fun serverResponseToEntity(serverResponse: List<Post>): List<ShortsEntity> {
        return serverResponse.map { serverResponseItem ->
            ShortsEntity(
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
                feedShortsBusinessId = serverResponseItem.feedShortsBusinessId

                // map other properties...
            )
        }
    }

    private fun serverResponseToFollowEntity(serverResponse: List<FollowListItem>): List<ShortsEntityFollowList> {
        return serverResponse.map { serverResponse ->
            ShortsEntityFollowList(
                followersId = serverResponse.followersId,
                isFollowing = serverResponse.isFollowing
            )
        }
    }

//    private fun getShorts(page: Int) {
//        viewModel.getShorts(page)
//
//    }

    private suspend fun loadMoreShortsPage1(currentPage: Int) {
        try {
            val response = retrofitIns.apiService.getShorts(currentPage.toString())

            if (response.isSuccessful) {
                val responseBody = response.body()
                Log.d(
                    "AllShorts3",
                    "Shorts List in page $currentPage ${responseBody?.data!!.posts}"
                )
                Log.d(
                    "AllShorts3",
                    "loadMoreShorts: followItem:  ${responseBody.data.followList}"
                )


                val shortsEntity = serverResponseToEntity(responseBody.data.posts.posts)

                val followListItem =
                    responseBody.data.followList.let { serverResponseToFollowEntity(it) }
//                Log.d("AllShorts", "loadMoreShorts: followItem=  ${followListItem[0]}")

                // Now, insert yourEntity into the Room database
                lifecycleScope.launch(Dispatchers.IO) {
//                      shortsViewModel.addAllShorts(shortsEntity)
                    val uniqueFollowList = removeDuplicateFollowers(followListItem)
                    Log.d(
                        "AllShorts3",
                        "getAllShort3: Inserted uniqueFollowList $uniqueFollowList"
                    )
                    followShortsViewModel.insertFollowListItems(uniqueFollowList)
                    if (uniqueFollowList.isEmpty()) {
                        Log.d("AllShorts3", "loadMoreShorts:uniqueFollowList is empty")

                        withContext(Dispatchers.Main) {
                            followShortsViewModel._followListItems.observe(viewLifecycleOwner) {
                                shortsViewModel.followList.addAll(it)

                                shortsAdapter.addIsFollowingData(it)

                            }
                        }
                    }
//                    withContext(Dispatchers.Main) {
//                        followShortsViewModel._followListItems.observe(viewLifecycleOwner) {
//                            shortsAdapter.addIsFollowingData(it)
//                        }
//                    }

//                    val uniqueEntitiesSet = HashSet<ShortsEntity>()

                    for (entity in shortsEntity) {

                        // Access the list of images for each entity
                        val images = entity.images

                        Log.d("ShortsData", "short: $entity")

//                            videoShorts.add(entity)
//                        if (uniqueEntitiesSet.add(entity)) {
                            // Add the unique entity to both the set and your ViewModel's list
                            shortsViewModel.videoShorts.add(entity)
//                        continue
//                        }
//                        shortsViewModel.videoShorts.add(entity)

                        for (image in images) {
                            // Access individual image properties or perform any desired actions
                            val imageUrl = image.url
//                            Log.d(SHORTS, "imageUrl - $imageUrl")
                            shortsList.add(imageUrl)
                        }
                    }
//                        viewPager.offscreenPageLimit = 21
                    startPreLoadingService()
                    withContext(Dispatchers.Main) {
                        if (shortsEntity.isNotEmpty()) {
                            Log.d("AllShorts3", "loadMoreShorts: shorts entity is not empty")
//                                shortsAdapter.addData(shortsEntity)

                            shortsViewModel.mutableShortsList.addAll(shortsEntity)
                            shortsViewModel.followList.addAll(followListItem)
                            shortsAdapter.addData(shortsEntity)

                            shortsAdapter.addIsFollowingData(followListItem)
//                                shortsAdapter.addIsFollowingData(followListItem)
                        } else {
                            Log.d("AllShorts3", "loadMoreShorts:shorts entity is empty")
                        }

                    }

                }
//                Log.d(SHORTS, "Handle the updated list of persons")
//                shortsViewModel.addAllShorts(personList)

            } else {
                Log.d("ErrorInShortsFragment", "Error message: ${response.message()}")
                Log.d("ErrorInShortsFragment", "Error body: ${response.body()}")
                Log.d("ErrorInShortsFragment", "Error error body: ${response.errorBody()}")
                Log.d("ErrorInShortsFragment", "Error response: $response")
                Log.d("ErrorInShortsFragment", "Error response code: ${response.code()}")
                Log.d("ErrorInShortsFragment", "Error response headers: ${response.headers()}")
                Log.d("ErrorInShortsFragment", "Error response raw: ${response.raw()}")
//                Log.d("AllShorts3", "Error error body: ${response.}")
                requireActivity().runOnUiThread {
                    showToast(response.message())
                }
            }

        } catch (e: HttpException) {
            Log.d("AllShorts", "Http Exception ${e.message}")
            requireActivity().runOnUiThread {
                showToast("Failed to connect try again...")
            }
        } catch (e: IOException) {
            Log.d("AllShorts", "IOException ${e.message}")
            requireActivity().runOnUiThread {
                showToast("Failed to connect try again...")
            }
        }
    }

//    private suspend fun loadShortsByFeedShortsBusinessId(feedShortsBusinessId: String) {
//        try {
//            val response = retrofitIns.apiService.getAllShortsByFeedShortBusinessId(feedShortsBusinessId)
//
//            if (response.isSuccessful) {
//                val responseBody = response.body()
//                Log.d(
//                    "AllShorts3",
//                    "Shorts List in page $feedShortsBusinessId ${responseBody?.data!!.posts}"
//                )
//                Log.d(
//                    "AllShorts3",
//                    "loadMoreShorts: followItem:  ${responseBody.data.followList}"
//                )
//
//
//                val shortsEntity = serverResponseToEntity(responseBody.data.posts.posts)
//
//                val followListItem =
//                    responseBody.data.followList.let { serverResponseToFollowEntity(it) }
////                Log.d("AllShorts", "loadMoreShorts: followItem=  ${followListItem[0]}")
//
//                // Now, insert yourEntity into the Room database
//                lifecycleScope.launch(Dispatchers.IO) {
////                      shortsViewModel.addAllShorts(shortsEntity)
//                    val uniqueFollowList = removeDuplicateFollowers(followListItem)
//                    Log.d(
//                        "AllShorts3",
//                        "getAllShort3: Inserted uniqueFollowList $uniqueFollowList"
//                    )
//                    followShortsViewModel.insertFollowListItems(uniqueFollowList)
//                    if (uniqueFollowList.isEmpty()) {
//                        Log.d("AllShorts3", "loadMoreShorts:uniqueFollowList is empty")
//
//                        withContext(Dispatchers.Main) {
//                            followShortsViewModel._followListItems.observe(viewLifecycleOwner) {
//                                shortsViewModel.followList.addAll(it)
//
//                                shortsAdapter.addIsFollowingData(it)
//
//                            }
//                        }
//                    }
////                    withContext(Dispatchers.Main) {
////                        followShortsViewModel._followListItems.observe(viewLifecycleOwner) {
////                            shortsAdapter.addIsFollowingData(it)
////                        }
////                    }
//
////                    val uniqueEntitiesSet = HashSet<ShortsEntity>()
//
//                    for (entity in shortsEntity) {
//                        // Access the list of images for each entity
//                        val images = entity.images
////                            videoShorts.add(entity)
//                        if (uniqueEntitiesSet.add(entity)) {
//                            // Add the unique entity to both the set and your ViewModel's list
//                            shortsViewModel.videoShorts.add(entity)
//                        }
////                        shortsViewModel.videoShorts.add(entity)
//
//                        for (image in images) {
//                            // Access individual image properties or perform any desired actions
//                            val imageUrl = image.url
////                            Log.d(SHORTS, "imageUrl - $imageUrl")
//                            shortsList.add(imageUrl)
//
//                        }
//                    }
////                        viewPager.offscreenPageLimit = 21
//                    startPreLoadingService()
//                    withContext(Dispatchers.Main) {
//                        if (shortsEntity.isNotEmpty()) {
//                            Log.d("AllShorts3", "loadMoreShorts: shorts entity is not empty")
////                                shortsAdapter.addData(shortsEntity)
//
//                            shortsViewModel.mutableShortsList.addAll(shortsEntity)
//                            shortsViewModel.followList.addAll(followListItem)
//                            shortsAdapter.addData(shortsEntity)
//
//                            shortsAdapter.addIsFollowingData(followListItem)
////                                shortsAdapter.addIsFollowingData(followListItem)
//                        } else {
//                            Log.d("AllShorts3", "loadMoreShorts:shorts entity is empty")
//                        }
//
//                    }
//
//                }
////                Log.d(SHORTS, "Handle the updated list of persons")
////                shortsViewModel.addAllShorts(personList)
//
//            } else {
//                Log.d("ErrorInShortsFragment", "Error message: ${response.message()}")
//                Log.d("ErrorInShortsFragment", "Error body: ${response.body()}")
//                Log.d("ErrorInShortsFragment", "Error error body: ${response.errorBody()}")
//                Log.d("ErrorInShortsFragment", "Error response: $response")
//                Log.d("ErrorInShortsFragment", "Error response code: ${response.code()}")
//                Log.d("ErrorInShortsFragment", "Error response headers: ${response.headers()}")
//                Log.d("ErrorInShortsFragment", "Error response raw: ${response.raw()}")
////                Log.d("AllShorts3", "Error error body: ${response.}")
//                requireActivity().runOnUiThread {
//                    showToast(response.message())
//                }
//            }
//
//        } catch (e: HttpException) {
//            Log.d("AllShorts", "Http Exception ${e.message}")
//            requireActivity().runOnUiThread {
//                showToast("Failed to connect try again...")
//            }
//        } catch (e: IOException) {
//            Log.d("AllShorts", "IOException ${e.message}")
//            requireActivity().runOnUiThread {
//                showToast("Failed to connect try again...")
//            }
//        }
//    }

    @SuppressLint("NotifyDataSetChanged")
    private suspend fun loadMoreShorts(currentPage: Int) {
        try {
            val response = retrofitIns.apiService.getShorts(currentPage.toString())

            if (response.isSuccessful) {
                val responseBody = response.body()
                Log.d(
                    "AllShorts3",
                    "Shorts List in page $currentPage ${responseBody?.data!!.posts}"
                )
                Log.d(
                    "AllShorts3",
                    "loadMoreShorts: followItem:  ${responseBody.data.followList}"
                )


                val shortsEntity = serverResponseToEntity(responseBody.data.posts.posts)

                val followListItem =
                    responseBody.data.followList.let { serverResponseToFollowEntity(it) }
//                Log.d("AllShorts", "loadMoreShorts: followItem=  ${followListItem[0]}")

                // Now, insert yourEntity into the Room database
                lifecycleScope.launch(Dispatchers.IO) {
//                      shortsViewModel.addAllShorts(shortsEntity)
                    val uniqueFollowList = removeDuplicateFollowers(followListItem)
                    Log.d(
                        "AllShorts3",
                        "getAllShort3: Inserted uniqueFollowList $uniqueFollowList"
                    )
                    followShortsViewModel.insertFollowListItems(uniqueFollowList)
                    if (uniqueFollowList.isEmpty()) {
                        Log.d("AllShorts3", "loadMoreShorts:uniqueFollowList is empty")

                        withContext(Dispatchers.Main) {
                            followShortsViewModel._followListItems.observe(viewLifecycleOwner) {
                                shortsViewModel.followList.addAll(followListItem)

                                shortsAdapter.addIsFollowingData(followListItem)
//                                    shortsAdapter.addIsFollowingData(it)
                            }
                        }
                    }
//                    withContext(Dispatchers.Main) {
//                        followShortsViewModel._followListItems.observe(viewLifecycleOwner) {
//                            shortsAdapter.addIsFollowingData(it)
//                        }
//                    }
                    for (entity in shortsEntity) {
                        // Access the list of images for each entity
                        val images = entity.images
                        //                            videoShorts.add(entity)
//                        shortsViewModel.videoShorts.add(entity)
                        if (uniqueEntitiesSet.add(entity)) {
                            Log.d("SHORTS", "Processing entity: $entity")
                            // Add the unique entity to both the set and your ViewModel's list
                            shortsViewModel.videoShorts.add(entity)
                            continue
                        }
                        // Iterate through the list of images
                        for (image in images) {
                            // Access individual image properties or perform any desired actions
                            val imageUrl = image.url
//                            Log.d(SHORTS, "imageUrl - $imageUrl")
                            shortsList.add(imageUrl)
//                                viewPager.offscreenPageLimit = viewpager2Limit + 10
//                                EventBus.getDefault().post(ShortsCacheEvent(shortsList))
                            // Do something with the imageUrl...
                        }
                    }
//                        viewPager.offscreenPageLimit = 21
                    startPreLoadingService()
                    withContext(Dispatchers.Main) {
                        if (shortsEntity.isNotEmpty()) {
                            Log.d("AllShorts3", "loadMoreShorts: shorts entity is not empty")
//                                shortsAdapter.addData(shortsEntity)
                            shortsViewModel.mutableShortsList.addAll(shortsEntity)
                            shortsAdapter.addData(shortsEntity)
                            shortsViewModel.followList.addAll(followListItem)

                            shortsAdapter.addIsFollowingData(followListItem)
//                            shortsAdapter.notifyDataSetChanged()
//                                shortsAdapter.addIsFollowingData(followListItem)
                        } else {
                            Log.d("AllShorts3", "loadMoreShorts:shorts entity is empty")
                        }

                    }

                }
//                Log.d(SHORTS, "Handle the updated list of persons")
//                shortsViewModel.addAllShorts(personList)

            } else {
                Log.d("AllShorts3", "Error: ${response.message()}")
                requireActivity().runOnUiThread {
                    showToast(response.message())
                }
            }

        } catch (e: HttpException) {
            Log.d("AllShorts", "Http Exception ${e.message}")
            requireActivity().runOnUiThread {
                showToast("Failed to connect try again...")
            }
        } catch (e: IOException) {
            Log.d("AllShorts", "IOException ${e.message}")
            requireActivity().runOnUiThread {
                showToast("Failed to connect try again...")
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private suspend fun loadMoreShortsByFeedShortsBusinessId(feedShortsBusinessId: String) {
        try {
            val response = retrofitIns.apiService.getAllShortsByFeedShortBusinessId(feedShortsBusinessId)

            if (response.isSuccessful) {
                val responseBody = response.body()
                Log.d(
                    "loadMoreShortsByFeedShortsBusinessId",
                    "Shorts List in page $feedShortsBusinessId ${responseBody?.data!!.posts}"
                )
                Log.d(
                    "loadMoreShortsByFeedShortsBusinessId",
                    "loadMoreShortsByFeedShortsBusinessId: followItem:  ${responseBody.data.followList}"
                )


                val shortsEntity = serverResponseToEntity(responseBody.data.posts.posts)

                val followListItem =
                    responseBody.data.followList.let { serverResponseToFollowEntity(it) }
//                Log.d("AllShorts", "loadMoreShorts: followItem=  ${followListItem[0]}")

                // Now, insert yourEntity into the Room database
                lifecycleScope.launch(Dispatchers.IO) {
//                      shortsViewModel.addAllShorts(shortsEntity)
                    val uniqueFollowList = removeDuplicateFollowers(followListItem)
                    Log.d(
                        "loadMoreShortsByFeedShortsBusinessId",
                        "loadMoreShortsByFeedShortsBusinessId: Inserted uniqueFollowList $uniqueFollowList"
                    )
                    followShortsViewModel.insertFollowListItems(uniqueFollowList)
                    if (uniqueFollowList.isEmpty()) {
                        Log.d("loadMoreShortsByFeedShortsBusinessId", "loadMoreShorts:uniqueFollowList is empty")

                        withContext(Dispatchers.Main) {
                            followShortsViewModel._followListItems.observe(viewLifecycleOwner) {
                                shortsViewModel.followList.addAll(followListItem)

                                shortsAdapter.addIsFollowingData(followListItem)
//                                    shortsAdapter.addIsFollowingData(it)
                            }
                        }
                    }
//                    withContext(Dispatchers.Main) {
//                        followShortsViewModel._followListItems.observe(viewLifecycleOwner) {
//                            shortsAdapter.addIsFollowingData(it)
//                        }
//                    }

                    for (entity in shortsEntity) {
                        // Access the list of images for each entity
                        val images = entity.images
//                            videoShorts.add(entity)
//                        shortsViewModel.videoShorts.add(entity)
                        if (uniqueEntitiesSet.add(entity) ) {

                            // Add the unique entity to both the set and your ViewModel's list
                            shortsViewModel.videoShorts.add(entity)

                        }else{
                            Log.d("DuplicateEntity", "Duplicate entity found: $entity")
                            shortsViewModel.videoShorts.add(entity)

                        }
                        // Iterate through the list of images
                        for (image in images) {
                            // Access individual image properties or perform any desired actions
                            val imageUrl = image.url
                            Log.d(SHORTS, "imageUrl - $imageUrl")
                            if (imageUrl !in shortsList) {
                                shortsList.add(imageUrl)
                            }
                            shortsList.add(imageUrl)
//                          viewPager.offscreenPageLimit = viewpager2Limit + 10
//                          EventBus.getDefault().post(ShortsCacheEvent(shortsList))
                            // Do something with the imageUrl...
                        }
                    }
//                        viewPager.offscreenPageLimit = 21
                    startPreLoadingService()
                    withContext(Dispatchers.Main) {
                        if (shortsEntity.isNotEmpty()) {
                            Log.d("loadMoreShortsByFeedShortsBusinessId", "loadMoreShorts: shorts entity is not empty")
//                                shortsAdapter.addData(shortsEntity)
                            shortsViewModel.mutableShortsList.addAll(shortsEntity)
                            shortsAdapter.addData(shortsEntity)
                            shortsViewModel.followList.addAll(followListItem)

                            shortsAdapter.addIsFollowingData(followListItem)
//                            shortsAdapter.notifyDataSetChanged()
//                                shortsAdapter.addIsFollowingData(followListItem)
                        } else {
                            Log.d("loadMoreShortsByFeedShortsBusinessId", "loadMoreShorts:shorts entity is empty")
                        }

                    }

                }
//                Log.d(SHORTS, "Handle the updated list of persons")
//                shortsViewModel.addAllShorts(personList)

            } else {
                Log.d("loadMoreShortsByFeedShortsBusinessId", "Error: ${response.message()}")
                requireActivity().runOnUiThread {
                    showToast(response.message())
                }
            }

        } catch (e: HttpException) {
            Log.d("AllShorts", "Http Exception ${e.message}")
            requireActivity().runOnUiThread {
                showToast("Failed to connect try again...")
            }
        } catch (e: IOException) {
            Log.d("AllShorts", "IOException ${e.message}")
            requireActivity().runOnUiThread {
                showToast("Failed to connect try again...")
            }
        }
    }


    fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun openUploadShortsActivity(videoUri: Uri) {
        val intent = Intent(requireContext(), UploadShortsActivity::class.java).apply {
            putExtra(UploadShortsActivity.EXTRA_VIDEO_URI, videoUri)
        }
        startActivityForResult(intent, REQUEST_UPLOAD_SHORTS_ACTIVITY)

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_UPLOAD_SHORTS_ACTIVITY) {
            if (resultCode == Activity.RESULT_OK) {
                // Handle the result when TopicsActivity returns RESULT_OK
                // You can use data to retrieve any additional information passed back
                // For example, val resultValue = data?.getStringExtra("keyName")
//                val selectedSubtopics = data?.getStringArrayListExtra("selectedSubtopics")
//                Log.d("selectedSubtopics", selectedSubtopics.toString())
//                binding.editTextText.setText(selectedSubtopics.toString())

            } else {
                // Handle other result codes if needed
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        releasePlayer()
        exoPlayer?.removeListener(playbackStateListener)

        if (exoPlayerItems.isNotEmpty()) {
            for (item in exoPlayerItems) {
                val player = item.exoPlayer
                player.stop()
                player.clearMediaItems()
            }
        }


        lifecycleScope.launch {
            shortsViewModel.isResuming = true
        }
    }


    companion object {
        const val REQUEST_UPLOAD_SHORTS_ACTIVITY = 123 // You can use any unique value
        private const val FEED_ARG_DATA = "feed_arg_data"
        private const val FEED_POST_POSITION = "feed_post_position"
        private const val FEED_SHORT_BUSINESS_ID = "feed_short_business_id"
        private const val FEED_SHORT_BUSINESS_FILE_ID = "feed_short_business_file_id"

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ShotsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }


        //        fun willReturnToFeedInstance(data: String? = null): ShotsFragment {
        fun willReturnToFeedInstance(
            data: Boolean = false, feedPostPosition: Int = -1,
            feedShortsBusinessId:String, feedShortsBusinessFileId: String
        ): ShotsFragment {
            Log.d("openShortsFragment", "willReturnToFeedInstance: feedPostPosition $feedPostPosition")
            val fragment = ShotsFragment()
            val args = Bundle()
            data.let {
                args.putBoolean(FEED_ARG_DATA, it)
            }
            feedPostPosition.let {
                args.putInt(FEED_POST_POSITION, it)
            }

            feedShortsBusinessId.let {
                args.putString(FEED_SHORT_BUSINESS_ID, feedShortsBusinessId)
            }

            feedShortsBusinessFileId.let {
                args.putString(FEED_SHORT_BUSINESS_FILE_ID, it)
            }

            fragment.arguments = args
            return fragment
        }
    }


    override fun onPause() {
        super.onPause()
        exoPlayer!!.pause()
        val index = exoPlayerItems.indexOfFirst { it.position == viewPager.currentItem }
        if (index != -1) {
            val player = exoPlayerItems[index].exoPlayer
            player.pause()
//            player.
            player.playWhenReady = false
            player.seekTo(0)
        }
    }

    private fun releasePlayer() {
        exoPlayer?.release()
        exoPlayer = null
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.hideAppBar()
        val vNLayout = activity?.findViewById<ConstraintLayout>(R.id.VNLayout)
        if (vNLayout?.visibility == View.VISIBLE) {
            pauseVideo()
        } else {
            exoPlayer!!.play()
            val index = exoPlayerItems.indexOfFirst { it.position == viewPager.currentItem }
            if (index != -1) {
                val player = exoPlayerItems[index].exoPlayer
                player.playWhenReady = true
                player.play()
                player.seekTo(0)
            }
        }
        updateStatusBar()

    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun informAdapter(event: InformAdapter) {
//        Log.d("informAdapter", "informAdapter: Inform adapter")
        lifecycleScope.launch(Dispatchers.Main) {
            followShortsViewModel._followListItems.observe(viewLifecycleOwner) {
                shortsAdapter.addIsFollowingData(it)
                shortsAdapter.notifyDataSetChanged()
//                Log.d("informAdapter", "informAdapter:$it ")
            }
        }

//        shortsAdapter.updateBtn("")
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun feedFavoriteFollowUpdate(event: FeedFavoriteFollowUpdate) {
        val tag = "feedFavoriteFollowUpdate"
        Log.d(tag, "feedFavoriteFollowUpdate: from favorites or all feed in short fragment")
        val followListItem: List<ShortsEntityFollowList> = listOf(
            ShortsEntityFollowList(
                event.userId, event.isFollowing
            )
        )

        lifecycleScope.launch(Dispatchers.IO) {
//            delay(200)
            val uniqueFollowList = removeDuplicateFollowers(followListItem)

            Log.d(
                "followButtonClicked",
                "followButtonClicked: Inserted uniqueFollowList $uniqueFollowList"
            )
            delay(100)
            followShortsViewModel.insertFollowListItems(uniqueFollowList)

        }

        lifecycleScope.launch(Dispatchers.Main) {
//            delay(200)
            followShortsViewModel._followListItems.observe(viewLifecycleOwner) {
                shortsAdapter.addIsFollowingData(it)
                shortsAdapter.notifyDataSetChanged()
                Log.d("followButtonClicked", "followButtonClicked:$it ")
            }
        }
//        shortsAdapter.addIsFollowingData()
//        shortsAdapter.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun followButtonClicked(event: ShortsFollowButtonClicked) {
        Log.d("followButtonClicked", "followButtonClicked: ${event.followUnFollowEntity}")

//        followViewModel.insertOrUpdateFollow(event.followUnFollowEntity)
        followUnFollowViewModel.followUnFollow(event.followUnFollowEntity.userId)
        followUnFollowViewModel.followUnFollowObserver().observe(viewLifecycleOwner) {
            Log.d("followButtonClicked", "followButtonClicked: follow observer value $it")
        }

        val followListItem: List<ShortsEntityFollowList> = listOf(
            ShortsEntityFollowList(
                event.followUnFollowEntity.userId, event.followUnFollowEntity.isFollowing
            )
        )

        feesShortsSharedViewModel.setData(
            FollowUnFollowEntity(
                event.followUnFollowEntity.userId,
                event.followUnFollowEntity.isFollowing
            )
        )

        lifecycleScope.launch(Dispatchers.IO) {
//            delay(200)
            val uniqueFollowList = removeDuplicateFollowers(followListItem)

            Log.d(
                "followButtonClicked",
                "followButtonClicked: Inserted uniqueFollowList $uniqueFollowList"
            )
            delay(100)
            followShortsViewModel.insertFollowListItems(uniqueFollowList)

        }

        lifecycleScope.launch(Dispatchers.Main) {
//            delay(200)
            followShortsViewModel._followListItems.observe(viewLifecycleOwner) {
                shortsAdapter.addIsFollowingData(it)
                shortsAdapter.notifyDataSetChanged()
                Log.d("followButtonClicked", "followButtonClicked:$it ")
            }
        }

//        shortsAdapter.updateBtn("")
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLikeUnLikeClick(event: ShortsLikeUnLike) {
        Log.d("onLikeUnLikeClick", "onLikeUnLikeClick: ${event.isLiked}")

    }

    @Subscribe(threadMode = ThreadMode.MAIN)

    fun likeButtonClicked(event: ShortsLikeUnLikeButton) {
//            Log.d("likeButtonClicked", "before onLikeUnLikeClick:")

        var button = event.likeUnLikeButton
        var shortOwnerId = event.shortsEntity.author.account._id
        var postId = event.shortsEntity._id
//            Log.d("likeButtonClicked", "likeButtonClicked: Post id: $postId")

        shortsViewModel.isLiked = event.shortsEntity.isLiked
//            shortsViewModel.totalLikes = event.shortsEntity.likes

//            Log.d("likeButtonClicked", "likeButtonClicked: event is liked: ${event.shortsEntity.isLiked}")
        event.likeCount.text = event.shortsEntity.likes.toString()
//        likes = event.shortsEntity.likes

        if (shortsViewModel.isLiked) {
            button.setImageResource(R.drawable.filled_favorite_like)
//                shortsViewModel.isLiked = event.shortsEntity.isLiked
            Log.d(
                "likeButtonClicked",
                "likeButtonClicked: event is liked: ${event.shortsEntity.isLiked}"
            )

        } else {
            button.setImageResource(R.drawable.favorite_svgrepo_com)
//                shortsViewModel.isLiked = event.shortsEntity.isLiked
            Log.d(
                "likeButtonClicked",
                "likeButtonClicked: event is liked: ${event.shortsEntity.isLiked}"
            )

        }

        button.setOnClickListener {
//                Log.d("likeButtonClicked", "likeButtonClicked: button clicked")
//                Log.d("likeButtonClicked", "likeButtonClicked: click is liked ${shortsViewModel.isLiked}")
//                Log.d("likeButtonClicked", "likeButtonClicked:click event is liked ${event.shortsEntity.isLiked}")
            handleLikeClick(postId, event.likeCount, button, event.shortsEntity)
        }

    }

    private fun handleLikeClick(
        postId: String,
        likeCount: TextView,
        btnLike: ImageButton,
        shortsEntity: ShortsEntity
    ) {

        Log.d("handleLikeClick", "handleLikeClick: before ${shortsViewModel.isLiked}")
        shortsViewModel.isLiked = !shortsViewModel.isLiked
        Log.d("handleLikeClick", "handleLikeClick: after ! ${shortsViewModel.isLiked}")
        EventBus.getDefault().post(ShortsLikeUnLike2(postId))

        if (!shortsEntity.isLiked) {

//                shortsViewModel.totalLikes += 1
            shortsEntity.likes += 1
            likeCount.text = shortsEntity.likes.toString()

            btnLike.setImageResource(R.drawable.filled_favorite_like)
            YoYo.with(Techniques.Tada)
                .duration(700)
                .repeat(1)
                .playOn(btnLike)

            shortsEntity.isLiked = true


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
            shortsEntity.likes -= 1
            likeCount.text = shortsEntity.likes.toString()
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
            shortsEntity.isLiked = false

            shortsViewModel.isLiked = false
            YoYo.with(Techniques.Tada)
                .duration(700)
                .repeat(1)
                .playOn(btnLike)
        }
    }

    private suspend fun likeUnLikeShort(shortOwnerId: String) {
        try {
            val response = retrofitIns.apiService.likeUnLikeShort(shortOwnerId)

//            Log.d("likeUnLikeShort", "likeUnLikeShort: response: $response")
//            Log.d("likeUnLikeShort", "likeUnLikeShort: response body: ${response.body()}")
//            Log.d("likeUnLikeShort", "likeUnLikeShort: response error body: ${response.errorBody()}")
            if (response.isSuccessful) {
                val responseBody = response.body()
                Log.d(
                    "likeUnLikeShort",
                    "likeUnLikeShort ${responseBody?.data!!.isLiked}"
                )
            } else {
                Log.d("likeUnLikeShort", "Error: ${response.message()}")
                requireActivity().runOnUiThread {
                    showToast(response.message())
                }
            }

        } catch (e: HttpException) {
            Log.d("likeUnLikeShort", "Http Exception ${e.message}")
            requireActivity().runOnUiThread {
                showToast("Failed to connect try again...")
            }
        } catch (e: IOException) {
            Log.d("likeUnLikeShort", "IOException ${e.message}")
            requireActivity().runOnUiThread {
                showToast("Failed to connect try again...")
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun favoriteButtonClicked(event: ShortsBookmarkButton) {
//            Log.d("likeButtonClicked", "before onLikeUnLikeClick:")

        val button = event.favoriteButton
        var shortOwnerId = event.shortsEntity.author.account._id
        val postId = event.shortsEntity._id
//            Log.d("likeButtonClicked", "likeButtonClicked: Post id: $postId")

        shortsViewModel.isFavorite = event.shortsEntity.isBookmarked

        if (shortsViewModel.isFavorite) {
            button.setImageResource(R.drawable.filled_favorite)

        } else {
            button.setImageResource(R.drawable.favorite_svgrepo_com__1_)
        }

        button.setOnClickListener {
            handleFavoriteClick(postId, button, event.shortsEntity)
        }

    }

    private fun handleFavoriteClick(postId: String, button: ImageView, shortsEntity: ShortsEntity) {

        val TAG = "handleFavoriteClick"
        shortsViewModel.isFavorite = !shortsViewModel.isFavorite
        EventBus.getDefault().post(ShortsFavoriteUnFavorite(postId))

        if (!shortsEntity.isBookmarked) {

            button.setImageResource(R.drawable.filled_favorite)
            YoYo.with(Techniques.Tada)
                .duration(700)
                .repeat(1)
                .playOn(button)

            shortsEntity.isBookmarked = true
//            shortsEntity.likes += shortsViewModel.totalLikes

            shortsViewModel.isFavorite = true

            val myShorts = userProfileShortsViewModel.mutableShortsList.find { it._id == postId }
            val myFavoriteShorts =
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
            val convertedShort = shortsEntityToUserShortsEntity(shortsEntity)
            userProfileShortsViewModel.mutableFavoriteShortsList.add(0, convertedShort)
        } else {
            button.setImageResource(R.drawable.favorite_svgrepo_com__1_)
            shortsEntity.isBookmarked = false
//            shortsEntity.likes += shortsViewModel.totalLikes
            shortsViewModel.isFavorite = false

            val myShorts = userProfileShortsViewModel.mutableShortsList.find { it._id == postId }
            val myFavoriteShorts =
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

    private suspend fun favoriteShort(postId: String) {
        try {
            val response = retrofitIns.apiService.favoriteShort(postId)

//            Log.d("likeUnLikeShort", "likeUnLikeShort: response: $response")
//            Log.d("likeUnLikeShort", "likeUnLikeShort: response body: ${response.body()}")
//            Log.d("likeUnLikeShort", "likeUnLikeShort: response error body: ${response.errorBody()}")
            if (response.isSuccessful) {
                val responseBody = response.body()
                Log.d(
                    "favoriteButtonClicked",
                    "favoriteButtonClicked ${responseBody?.data!!.isBookmarked}"
                )
            } else {
                Log.d("favoriteButtonClicked", "Error: ${response.message()}")
                requireActivity().runOnUiThread {
                    showToast(response.message())
                }
            }

        } catch (e: HttpException) {
            Log.d("favoriteButtonClicked", "Http Exception ${e.message}")
            requireActivity().runOnUiThread {
                showToast("Failed to connect try again...")
            }
        } catch (e: IOException) {
            Log.d("favoriteButtonClicked", "IOException ${e.message}")
            requireActivity().runOnUiThread {
                showToast("Failed to connect try again...")
            }
        }
    }

    private fun updateStatusBar() {
        val decor: View? = activity?.window?.decorView

        // Your logic to determine the status bar appearance based on the fragment's theme
//        val isLightTheme = // Your logic to determine if the fragment has a light theme
//        decor?.systemUiVisibility = 0
//        decor?.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        decor?.systemUiVisibility = 0

//            if (isLightTheme) {
//                // Light theme
//            } else {
//                // Dark theme
//                decor?.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
//            }
    }

    override fun onCommentsClick(position: Int, data: UserShortsEntity) {
//        showBottomSheet(commentsRecyclerView)
        showBottomSheet()

    }

    private lateinit var dialog: BottomSheetDialog
    private lateinit var itemAdapter: CommentAdapter
    private lateinit var recyclerView: RecyclerView
    private val list = ArrayList<String>()

    @SuppressLint("InflateParams")
    private fun showBottomSheet() {


    }


    private fun startPreLoadingService() {
        Log.d(SHORTS, "Preloading called")
        val preloadingServiceIntent =
            Intent(requireContext(), VideoPreLoadingService::class.java)
        preloadingServiceIntent.putStringArrayListExtra(Constants.VIDEO_LIST, shortsList)
        requireContext().startService(preloadingServiceIntent)
    }

    override fun onSeekBarChanged(progress: Int) {
//        exoPlayer!!.duration

        exoPlayer!!.seekTo(progress.toLong())
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

    private fun playVideo() {
        exoPlayer?.playWhenReady = true
        isPlaying = true
    }

    private fun pauseVideo() {
        exoPlayer?.playWhenReady = false
        isPlaying = false
    }

    private fun playbackStateListener() = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            when (state) {
                ExoPlayer.STATE_ENDED -> {
                    // The video playback ended. Move to the next video if available.
                }
                // Add other cases if needed
                Player.STATE_BUFFERING -> {

                }

                Player.STATE_IDLE -> {
                }

                Player.STATE_READY -> {

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

        private var updateSeekBarJob: Job? = null

        private fun startUpdatingSeekBar() {
            updateSeekBarJob = CoroutineScope(Dispatchers.Main).launch {
                while (true) {
                    // Update seek bar based on current playback position
//                    exoPlayer?.let { player ->
//                        // Update seek bar based on current playback position
//                        shortsAdapter.setSeekBarProgress(20)
//                    }
                    updateSeekBar()
                    delay(50) // Update seek bar every second (adjust as needed)
                }
            }
        }

        private fun stopUpdatingSeekBar() {
            updateSeekBarJob?.cancel()
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
                requireActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(requireActivity(), permissions, requestCode)
        } else {
            // You have permission, proceed with your file operations

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                // Check if the permission is not granted
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // Request the permission
                    ActivityCompat.requestPermissions(
                        requireActivity(),
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

//                        download2(url,progressbar,fileLocation)


    }

    override fun onShareClick(position: Int) {
        val context = requireContext()

        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val shareView = layoutInflater.inflate(R.layout.example, null)
        val close_button = shareView.findViewById<ImageButton>(R.id.close_button)
        val recyclerView = shareView.findViewById<RecyclerView>(R.id.apps_recycler_view)
        val userRecyclerView = shareView.findViewById<RecyclerView>(R.id.users_recycler_view)

        bottomSheetDialog.setContentView(shareView)
        bottomSheetDialog.show()

        // Fetch installed apps that support sharing
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain" }
        val resolveInfoList = packageManager?.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = resolveInfoList?.let { ShareVideoAdapter(it, context, position) }
    }

    override fun onUploadCancelClick() {
        TODO("Not yet implemented")
    }

    fun generateUniqueValue(): String {
        val currentTimeMillis = System.currentTimeMillis()

        // You can format the timestamp as needed
        val dateFormat = SimpleDateFormat("yyyy_MM_dd_HHmmss", Locale.getDefault())

        // Create a unique value using the formatted timestamp or customize it as needed

        return dateFormat.format(Date(currentTimeMillis))
    }

    private fun String.replaceLast(oldValue: String, newValue: String): String {
        val lastIndexOf = this.lastIndexOf(oldValue)
        return if (lastIndexOf == -1) this else this.substring(
            0,
            lastIndexOf
        ) + newValue + this.substring(lastIndexOf + oldValue.length)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int,
        fileLocation: String
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create a Notification Channel for Android Oreo and above
        val channelId = "channel_id"
        val channel = NotificationChannel(
            channelId,
            "Channel Name",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = "Channel Description"
        channel.enableLights(true)
        channel.lightColor = Color.BLUE
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

    private fun generateRandomNotificationId(): Int {
        val randomUUID = UUID.randomUUID()
        return abs(randomUUID.hashCode())
    }

    private fun generateUniqueFileName(originalUrl: String): String {
        val timestamp =
            SimpleDateFormat("yyyy_MM_dd_HHmmss", Locale.getDefault()).format(Date())
        val originalFileName = originalUrl.split("/").last()
        val fileExtension = MimeTypeMap.getFileExtensionFromUrl(originalFileName)
        val randomString = UUID.randomUUID().toString().substring(0, 8)
        return "$timestamp-$randomString.$fileExtension"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @kotlin.OptIn(DelicateCoroutinesApi::class)
    private fun download(
        mUrl: String,
        fileLocation: String,
    ) {
        //STORAGE_FOLDER += fileLocation
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
                        requireActivity().runOnUiThread {
                            //progressbar.visibility = View.VISIBLE
//                            progressbar.progress = downloadProgress
//                        progressCountTv.text = "$downloadProgress%"
                            downloadProgressBarLayout.visibility = View.VISIBLE
                            shortsDownloadImageView.setBackgroundResource(R.drawable.shorts_download_animation)
                            wifiAnimation =
                                shortsDownloadImageView.background as AnimationDrawable
                            wifiAnimation!!.start()
                                Log.d("Download", "Progress $downloadProgress")
                            shortsDownloadProgressBar.progress = downloadProgress
                        }
                        outputStream.write(buffer, 0, bytes)
                        bytes = inputStream.read(buffer)
                    }
                    // progressbar.visibility = View.GONE
                    //progressCountTv.visibility = View.GONE
                    requireActivity().runOnUiThread {
                        // Update the UI components here
//                        progressbar.visibility = View.GONE
//                        coreChatSocketClient.sendDownLoadedEvent(myId,message.id)
                        Log.d("Download", "File Downloaded : $storageDirectory")
                        downloadProgressBarLayout.visibility = View.GONE

                        wifiAnimation!!.stop()

                        // Show notification
                        showNotification(
                            requireActivity(),
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
                    requireActivity().runOnUiThread {
                        Toast.makeText(
                            requireActivity(),
                            "Not successful",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("DownloadFailed", e.message.toString())

                e.printStackTrace()
                requireActivity().runOnUiThread {
//                    progressbar.visibility = View.GONE
                    downloadProgressBarLayout.visibility = View.GONE
                }
            }
        }
    }

    override fun onGetLayoutInflater(
        savedInstanceState: Bundle?
    ): LayoutInflater {
        // Use a custom theme for the fragment layout
//        val themeId = if (someCondition) {
//            R.style.FragmentLightTheme
//        } else {
//        }

        return super.onGetLayoutInflater(savedInstanceState).cloneInContext(
            ContextThemeWrapper(
                requireContext(), R.style.DarkTheme
            )
        )
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
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
//            deleteUserProfile()
            //clear shared prefs
            LocalStorage.getInstance(requireContext()).clear()
            LocalStorage.getInstance(requireContext()).clearToken()
//            settings.edit().clear().apply()

//            callViewModel.clearAll()
//            messageViewModel.clearAll()
//            dialogViewModel.clearAll()
//            groupDialogViewModel.clearAll()
        }

        val intent = Intent(requireActivity(), LoginActivity::class.java)
        requireActivity().finish()
        startActivity(intent)
    }


    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun feedInformShortsFragment(event: InformShortsFragment2) {
        Log.d("feedInformShortsFragment", "InformShortsFragment2: shorts fragment informed")
        val list: MutableList<ShortsEntityFollowList> = mutableListOf()
        list.add(ShortsEntityFollowList(event.userId, event.isFollowing))
        val followListItem: List<ShortsEntityFollowList> = listOf(
            ShortsEntityFollowList(
                event.userId, event.isFollowing
            )
        )
        lifecycleScope.launch(Dispatchers.IO) {
//            delay(200)
            val uniqueFollowList = removeDuplicateFollowers(followListItem)
            Log.d(
                "feedShortsSharedViewModel",
                "feedShortsSharedViewModel: Inserted uniqueFollowList in shorts: $uniqueFollowList"
            )
            delay(100)
            followShortsViewModel.insertFollowListItems(uniqueFollowList)
            shortsViewModel.followList.add(ShortsEntityFollowList(event.userId, event.isFollowing))
        }
        shortsAdapter.addIsFollowingData(list)
        shortsAdapter.notifyDataSetChanged()
    }
}


