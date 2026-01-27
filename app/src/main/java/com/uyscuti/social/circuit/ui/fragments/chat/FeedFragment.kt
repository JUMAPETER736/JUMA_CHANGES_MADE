package com.uyscuti.social.circuit.ui.fragments.chat

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.GradientDrawable
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.FeedMultipleImageViewFragment
import com.uyscuti.sharedmodule.eventbus.FeedFavoriteFollowUpdate
import com.uyscuti.sharedmodule.eventbus.HideFeedFloatingActionButton
import com.uyscuti.sharedmodule.eventbus.InformFeedFragment
import com.uyscuti.sharedmodule.eventbus.InformShortsFragment
import com.uyscuti.sharedmodule.eventbus.ShowFeedFloatingActionButton
import com.uyscuti.sharedmodule.model.FeedUploadProgress
import com.uyscuti.sharedmodule.model.FeedUploadSuccessful
import com.uyscuti.sharedmodule.model.PauseShort
import com.uyscuti.sharedmodule.model.ProgressEvent
import com.uyscuti.sharedmodule.model.UploadSuccessful
import com.uyscuti.sharedmodule.model.feed.SetAllFragmentScrollPosition
import com.uyscuti.sharedmodule.utils.AudioDurationHelper
import com.uyscuti.sharedmodule.utils.Timer
import com.uyscuti.sharedmodule.utils.TrimVideoUtils
import com.uyscuti.sharedmodule.utils.WaveFormExtractor
import com.uyscuti.sharedmodule.utils.audiomixer.AudioMixer
import com.uyscuti.sharedmodule.utils.audiomixer.input.GeneralAudioInput
import com.uyscuti.sharedmodule.utils.deleteFiles
import com.uyscuti.sharedmodule.utils.feedutils.deserializeFeedUploadDataList
import com.uyscuti.sharedmodule.utils.filterStringsContainingSubstring
import com.uyscuti.sharedmodule.utils.getFileNameFromLocalPath
import com.uyscuti.sharedmodule.utils.getOutputFilePath
import com.uyscuti.sharedmodule.utils.waveformseekbar.SeekBarOnProgressChanged
import com.uyscuti.sharedmodule.utils.waveformseekbar.WaveformSeekBar
import com.uyscuti.sharedmodule.viewmodels.FeedShortsViewModel
import com.uyscuti.sharedmodule.viewmodels.feed.FeedUploadProgressViewModel
import com.uyscuti.sharedmodule.views.WaveFormView
import com.uyscuti.social.circuit.MainActivity
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.ui.fragments.feed.AllFragment
import com.uyscuti.social.circuit.ui.fragments.feed.FavoriteFragment
import com.uyscuti.sharedmodule.UploadFeedActivity
import com.uyscuti.sharedmodule.utils.AudioDurationHelper.getFormattedDuration
import com.uyscuti.sharedmodule.viewmodels.feed.GetFeedViewModel
import com.uyscuti.social.circuit.MainActivity.VoiceNoteState
import com.uyscuti.social.circuit.adapter.FragmentPageAdapter
import com.uyscuti.social.core.common.data.room.entity.FollowUnFollowEntity
import com.uyscuti.social.network.api.response.posts.Account
import com.uyscuti.social.network.api.response.posts.Author
import com.uyscuti.social.network.api.response.posts.Avatar
import com.uyscuti.social.network.api.response.posts.CoverImage
import com.uyscuti.social.network.api.response.posts.Duration
import com.uyscuti.social.network.api.response.posts.FileName
import com.uyscuti.social.network.api.response.posts.FileType
import com.uyscuti.social.network.api.response.posts.NumberOfPageX
import com.uyscuti.social.network.api.response.posts.Post
import com.uyscuti.social.network.api.response.posts.RepostedUser
import com.uyscuti.social.network.api.response.posts.ThumbnailX
import com.uyscuti.social.network.utils.LocalStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.IOException
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.io.File
import kotlin.math.sqrt


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val TAG = "FeedFragment"


private const val REQUEST_UPLOAD_FEED_ACTIVITY = 1010

@UnstableApi
class FeedFragment() : Fragment(), Timer.OnTimeTickListener {


    companion object {

        private const val FEED_POST_POSITION_FROM_SHORTS = "feed_post_position_from_shorts"
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FeedFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        fun feedPostFromShorts(feedPostPositionFromShorts: Int = -1): FeedFragment {
            val feedFragment = FeedFragment()

            val args = Bundle()
            feedPostPositionFromShorts.let {
                args.putInt(FEED_POST_POSITION_FROM_SHORTS, it)
            }
            feedFragment.arguments = args
            return feedFragment
        }
    }

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var uploadSeekBar: SeekBar
    private val feedMultipleImageViewFragment : FeedMultipleImageViewFragment? = null
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager2: ViewPager2
    private lateinit var adapter: FragmentPageAdapter
    private lateinit var fileFloatingActionButton: FloatingActionButton
    private lateinit var vnFloatingActionButton: FloatingActionButton
    private lateinit var fabAction: FloatingActionButton
    private val feedUploadProgressViewModel: FeedUploadProgressViewModel by viewModels()
    private val getFeedViewModel: GetFeedViewModel by activityViewModels()
    private val rotateOpen: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireActivity(),
            R.anim.rotate_open_anim
        )
    }
    private val rotateClose: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireActivity(),
            R.anim.rotate_close_anim
        )
    }
    private val fromBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireActivity(),
            R.anim.from_bottom_anim
        )
    }
    private val toBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireActivity(),
            R.anim.to_bottom_anim
        )
    }

    private var deleteVN: ImageView? = null
    private var recordVN: ImageView? = null
    private lateinit var playVnAudioBtn: ImageView
    private var sendVN: ImageView? = null

    private var timerTv: TextView? = null
    private var playerTimerTv: LinearLayout? = null
    private var secondTimerTv: TextView? = null


    private var waveForm: WaveFormView? = null
    private var wave: WaveformSeekBar? = null
    private var playAudioLayout: LinearLayout? = null
    private var totalRecordedDuration = 0L
    private var voiceNoteState = VoiceNoteState.IDLE
    private var mixingCompleted = false

    // Add to your existing variable declarations
    private val waveBars = mutableListOf<View>()
    private var waveBarCount = 0
    private val maxWaveBars = 100
    private var audioRecord: AudioRecord? = null
    private var isListeningToAudio = false
    private var recordingStartTime = 0L
    private var recordingElapsedTime = 0L
    private var playbackTimerRunnable: kotlinx.coroutines.Runnable? = null
    private var dialog: BottomSheetDialog? = null
    private var wifiAnimation: AnimationDrawable? = null

    //    private lateinit var feedUploadImageView: ImageView
    private lateinit var feedUploadView: ImageView
    private lateinit var feedCancelView: ImageView
//    private lateinit var uploadProgressSeekBar: ProgressBar

    //    private lateinit var feedUploadViewModel: FeedUploadViewModel
    private lateinit var settings: SharedPreferences
    private val PREFS_NAME = "LocalSettings"



    // UPLOAD STATE & PROGRESS

    private var progressAnimator: ValueAnimator? = null
    private var isUploadInProgress = false


    // AUDIO RECORDING & PLAYBACK

    private var mediaRecorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private lateinit var outputFile: String
    private var outputVnFile: String = ""
    private val recordedAudioFiles = mutableListOf<String>()
    private lateinit var amplitudes: ArrayList<Float>
    private lateinit var timer: Timer
    private var amps = 0


    // AUDIO STATE FLAGS

    private var isRecording = false
    private var isPaused = false
    private var isAudioVNPlaying = false
    private var isAudioVNPaused = false
    private var isVnResuming = false
    private var vnRecordAudioPlaying = false
    private var isOnRecordDurationOnPause = false
    private var wasPaused = false


    // GENERAL STATE FLAGS

    private var clicked = false
    private var vnRecordProgress = 0
    private var feedPostPositionFromShorts = -1

    private val waveHandler = Handler(Looper.getMainLooper())
    private val timerHandler = Handler(Looper.getMainLooper())

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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.READ_MEDIA_IMAGES
    )
    private val REQUEST_CODE = 2024

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        feedPostPositionFromShorts = arguments?.getInt(FEED_POST_POSITION_FROM_SHORTS, -1)!!
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        if (::playVnAudioBtn.isInitialized) {
            stopPlaying()
            stopRecording()
            stopRecordWaveRunnable()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @OptIn(UnstableApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        (activity as? MainActivity)?.showAppBar()
        activity?.window?.statusBarColor = ContextCompat.getColor(requireContext(), R.color.white)
        // Set the navigation bar color dynamically
        activity?.window?.navigationBarColor =
            ContextCompat.getColor(requireContext(), R.color.white)
        val decor: View? = activity?.window?.decorView





        if (decor!!.systemUiVisibility != View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
            decor.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        else
            decor.systemUiVisibility = 0

        val view = inflater.inflate(R.layout.fragment_feed, container, false)
        settings = requireActivity().getSharedPreferences(PREFS_NAME, 0)
        fabAction = view.findViewById(R.id.fabAction)
        fileFloatingActionButton = view.findViewById(R.id.fileFloatingActionButton)
        vnFloatingActionButton = view.findViewById(R.id.vnFloatingActionButton)

        feedUploadView = view.findViewById(R.id.feedUploadView)
        feedCancelView = view.findViewById(R.id.feedCancelView)



        timer = Timer(this)


        permissionGranted = ActivityCompat.checkSelfPermission(
            requireContext(), permissions[0]
        ) == PackageManager.PERMISSION_GRANTED
        if (!permissionGranted) {
            ActivityCompat.requestPermissions(requireActivity(), permissions, REQUEST_CODE)
        }
        fabAction.setOnClickListener {
            Log.d("fabAction", "fabAction: fab action clicked")

            onAddButtonClick()
        }

        fileFloatingActionButton.setOnClickListener {
            onUploadFilesButtonClick()
        }

        vnFloatingActionButton.setOnClickListener {
            onUploadVNButtonClick()
        }

        tabLayout = view.findViewById(R.id.tabLayout)
        viewPager2 = view.findViewById(R.id.viewPager2)

        adapter = FragmentPageAdapter(requireActivity().supportFragmentManager, lifecycle)

        tabLayout.addTab(tabLayout.newTab().setText("All"))
        tabLayout.addTab(tabLayout.newTab().setText("Following"))


       viewPager2.adapter = adapter


        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {

                if (tab != null) {
                    viewPager2.currentItem = tab.position
                    when (tab.position) {
                        0 -> {
                            Log.d("onTabSelected", "onTabSelected: ${tab.position}")

                            val fragment = AllFragment()
                            fragment.forShow()
                            // Uncomment and set a listener if needed
                            // fragment.setListener(this@FeedFragment)
                        }
                        1 -> {
                            Log.d("onTabSelected", "onTabSelected: ${tab.position}")
                            val fragment = FavoriteFragment()
                            fragment.forShow()
                            // Uncomment and set a listener if needed
                            // fragment.setListener(this@FeedFragment)
                        }

                    }
                }
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {



            }

            override fun onTabReselected(p0: TabLayout.Tab?) {

            }

        })


        Log.d("feedPostPositionFromShorts",
            "onCreateView: of feed fragment $feedPostPositionFromShorts" +
                    " viewPager2.currentItem ${viewPager2.currentItem}")


        if(feedPostPositionFromShorts != -1) {
            val currentFragment = adapter.getFragment(viewPager2.currentItem)
            if (currentFragment is AllFragment) {
                Log.d(TAG, "onCreateView: current frag $feedPostPositionFromShorts")
                // Pass data to the current fragment
//                currentFragment.updateData("New Data")
                currentFragment.setPositionFromShorts(
                    SetAllFragmentScrollPosition(
                        true, feedPostPositionFromShorts))
            }else {
                Log.d(TAG, "onCreateView: current fragment not all fragment")
            }
        }

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.d(TAG, "onPageSelected: $feedPostPositionFromShorts")
                tabLayout.selectTab(tabLayout.getTabAt(position))
            }
        })
        viewPager2.offscreenPageLimit = 3
        // Approach 2: Disable swipe using isUserInputEnabled
        viewPager2.isUserInputEnabled = false
        return view
    }

    private fun onAddButtonClick() {
        setVisibility(clicked)
        setAnimation(clicked)
        clicked = !clicked
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun feedFavoriteFollowUpdate(event: FeedFavoriteFollowUpdate) {

        Log.d("feedFavoriteFollowUpdate",
            "feedFavoriteFollowUpdate: code to inform shorts fragment from feed feed fragment")
        EventBus.getDefault().post(InformShortsFragment())
    }

    private val feesShortsSharedViewModel: FeedShortsViewModel by activityViewModels()
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun feedFollowUpdate(event: InformFeedFragment) {
        Log.d("InformFeedFragment",
            "InformFeedFragment: code  from other user profile to feed fragment")

        feesShortsSharedViewModel.setData(
            FollowUnFollowEntity(
                event.userId,
                event.isFollowing
            )
        )
    }

    private fun setVisibility(clicked: Boolean) {
        if (!clicked) {
            fileFloatingActionButton.visibility = View.VISIBLE
            vnFloatingActionButton.visibility = View.VISIBLE
//            fabAction.visibility = View.VISIBLE
        } else {
            fileFloatingActionButton.visibility = View.INVISIBLE
            vnFloatingActionButton.visibility = View.INVISIBLE

        }

    }


    private fun setAnimation(clicked: Boolean) {
        if (!clicked) {
            fileFloatingActionButton.startAnimation(fromBottom)
            vnFloatingActionButton.startAnimation(fromBottom)
            fabAction.startAnimation(rotateOpen)
        } else {
            fileFloatingActionButton.startAnimation(toBottom)
            vnFloatingActionButton.startAnimation(toBottom)
            fabAction.startAnimation(rotateClose)
        }
    }


    private fun onUploadFilesButtonClick() {
        val intent = Intent(requireActivity(), UploadFeedActivity::class.java)
        startActivityForResult(intent, REQUEST_UPLOAD_FEED_ACTIVITY)
    }

    private fun generateMongoDBTimestamp(): String {
        val timestamp = OffsetDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        return timestamp.format(formatter)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_UPLOAD_FEED_ACTIVITY && resultCode == Activity.RESULT_OK) {
            startUploadAnimation()
            val mixedFilesData = data?.getStringExtra("mixedFiles")
            val caption = data?.getStringExtra("caption")
            val tags = data?.getStringExtra("tags")
            val contentType = data?.getStringExtra("contentType")
            val dataList = mixedFilesData?.let { deserializeFeedUploadDataList(it) } ?: mutableListOf()

            val filesList = ArrayList<com.uyscuti.social.network.api.response.posts.File>()
            val fileTypes: MutableList<com.uyscuti.social.network.api.response.posts.FileType> = mutableListOf()
            val duration: MutableList<com.uyscuti.social.network.api.response.posts.Duration> = mutableListOf()
            val fileIds: MutableList<String> = mutableListOf()
            val thumbnail: MutableList<ThumbnailX> = mutableListOf()
            val numberOfPages: MutableList<NumberOfPageX> = mutableListOf()
            val fileName: MutableList<com.uyscuti.social.network.api.response.posts.FileName> = mutableListOf()

            val profilePic2 = settings.getString("profile_pic", "").toString()
            val userId = settings.getString("_id", "").toString()

            val avatar = Avatar(_id = "", localPath = "", url = "")
            val account = Account(
                _id = "", avatar = avatar, createdAt = "", email = "", updatedAt = "",
                username = LocalStorage.getInstance(requireContext()).getUsername()
            )
            val author = Author(
                _id = userId, account = account, firstName = "", lastName = "", bio = "",
                countryCode = "", createdAt = "", __v = 0, dob = "", owner = "", location = "",
                updatedAt = "", phoneNumber = "", coverImage = CoverImage(_id = "", localPath = "", url = profilePic2)
            )
            val mongoDbTimeStamp = generateMongoDBTimestamp()

            dataList.forEach {
                fileIds.add(it.fileId)
                fileTypes.add(FileType(it.fileId, it.fileTypes))
                if (it.documents != null) {
                    filesList.add(
                        com.uyscuti.social.network.api.response.posts.File(
                            _id = "", fileId = it.fileId, url = it.documents!!.pdfFilePath,
                            localPath = it.documents!!.uri.toString(), mimeType = null
                        )
                    )
                    numberOfPages.add(NumberOfPageX(fileId = it.fileId, numberOfPage = "1"))
                    fileName.add(FileName(fileId = it.fileId, fileName = it.documents!!.filename))
                    thumbnail.add(
                        ThumbnailX(_id = "", thumbnailUrl = it.documents!!.pdfFilePath,
                            thumbnailLocalPath = it.documents!!.pdfFilePath, fileId = it.fileId)
                    )
                } else if (it.images != null) {

                    filesList.add(
                        com.uyscuti.social.network.api.response.posts.File(
                            _id = "", fileId = it.fileId, url = it.images!!.imagePath,
                            localPath = it.images!!.compressedImagePath, mimeType = null
                        )
                    )
                } else if (it.videos != null) {
                    filesList.add(
                        com.uyscuti.social.network.api.response.posts.File(
                            _id = "", fileId = it.fileId, url = it.videos!!.videoPath,
                            localPath = it.videos!!.videoUri, mimeType = null
                        )
                    )
                    thumbnail.add(
                        ThumbnailX(_id = "", thumbnailUrl = it.videos!!.videoPath,
                            thumbnailLocalPath = it.videos!!.videoUri, fileId = it.fileId)
                    )
                    duration.add(Duration(it.fileId, it.videos!!.videoDuration))
                } else if (it.audios != null) {
                    filesList.add(
                        com.uyscuti.social.network.api.response.posts.File(
                            _id = "", fileId = it.fileId, url = it.audios!!.audioPath,
                            localPath = it.audios!!.audioPath, mimeType = null
                        )
                    )
                    duration.add(Duration(it.fileId, it.audios!!.duration))
                }
            }

            val post = Post(
                __v = 1, _id = "", author = author, bookmarkCount = 0, comments = 0,
                content = caption ?: "", contentType = contentType ?: "", createdAt = mongoDbTimeStamp,
                duration = duration, feedShortsBusinessId = "", fileIds = fileIds, fileNames = fileName,
                fileSizes = listOf(), fileTypes = fileTypes, files = filesList, isBookmarked = false,
                isExpanded = false, isFollowing = false, isLiked = false, isLocal = true,
                isReposted = false, likes = 0, numberOfPages = numberOfPages, originalPost = listOf(),
                repostedByUserId = "", repostedUser = RepostedUser(
                    _id = "", avatar = Avatar(_id = "", localPath = "", url = ""), bio = "",
                    coverImage = CoverImage(_id = "", localPath = "", url = ""), createdAt = "",
                    email = "", firstName = "", lastName = "", owner = "", updatedAt = "", username = ""
                ), repostedUsers = listOf(), tags = listOf(), thumbnail = thumbnail,
                updatedAt = mongoDbTimeStamp, shareCount = 0, repostCount = 0
            )
            getFeedViewModel.addSingleAllFeedData(post)
        }
    }


    @SuppressLint("ShowToast")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun successEvent(event: UploadSuccessful) {


        val rootView: View = requireActivity().findViewById(android.R.id.content)
        wifiAnimation!!.stop()
//        uploadProgressMainLayout.visibility = View.GONE
        feedUploadView.visibility = View.GONE
        feedCancelView.visibility = View.GONE
        EventBus.getDefault().post(FeedUploadProgress(100, 0))
        if (!event.success) {
            Toast.makeText(
                requireContext(),
                "Failed to upload, Please try again!!",
                Toast.LENGTH_LONG
            ).show()
        }else {
            // Create and show the SnackBar
            val snackBar = Snackbar.make(rootView, "Feed upload successful", Snackbar.LENGTH_LONG)
            // Retrieve colors from resources
            val snackBarBackgroundColor = ContextCompat.getColor(requireContext(), R.color.green)
            val snackBarTextColor = ContextCompat.getColor(requireContext(), R.color.white)
            val snackBarActionColor = ContextCompat.getColor(requireContext(), R.color.gray_dark_transparent)
            val snackBarView = snackBar.view
            snackBarView.setBackgroundColor(snackBarBackgroundColor)
            // Optional: Customize text color
            snackBar.setTextColor(snackBarTextColor)
            // Optional: Customize action button color
            snackBar.setActionTextColor(snackBarActionColor)

            snackBar.show()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun feedVideoSuccessEvent(event: FeedUploadSuccessful) {
        wifiAnimation!!.stop()
//        uploadProgressMainLayout.visibility = View.GONE
        feedUploadView.visibility = View.GONE
        feedCancelView.visibility = View.GONE
        EventBus.getDefault().post(FeedUploadProgress(100, 0))

        val matchingStrings =
            filterStringsContainingSubstring(event.filesToDelete, "flash_feed_video_compresses")
        val deleteFiles = deleteFiles(matchingStrings)
        if (deleteFiles) {
            Log.d(TAG, "feedVideoSuccessEvent: file delete successful")
        } else {
            Log.e(TAG, "feedVideoSuccessEvent: failed to delete file ")
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onProgressEvent(event: ProgressEvent) {
//        uploadProgressMainLayout.visibility = View.VISIBLE

        feedUploadView.visibility = View.VISIBLE
        feedCancelView.visibility = View.VISIBLE
        feedUploadView.setBackgroundResource(R.drawable.feed_upload_animation)
        wifiAnimation = feedUploadView.background as AnimationDrawable
        val currentProgress = 100 + event.progress

        when (event.eventId) {
            "uniqueIdAudio" -> {

                EventBus.getDefault().post(FeedUploadProgress(200, event.progress))
            }

            "workerUniqueIdAudio" -> {

                EventBus.getDefault().post(FeedUploadProgress(200, currentProgress))
            }

            "uniqueIdVideo" -> {

                EventBus.getDefault().post(FeedUploadProgress(200, event.progress))
            }

            "workerUniqueIdVideo" -> {

                EventBus.getDefault().post(FeedUploadProgress(200, currentProgress))
            }

            "mixed_files" -> {
                EventBus.getDefault().post(FeedUploadProgress(100, currentProgress))
            }

            else -> {

                EventBus.getDefault().post(FeedUploadProgress(100, event.progress))
            }
        }


        wifiAnimation!!.start()
    }


    override fun onGetLayoutInflater(
        savedInstanceState: Bundle?
    ): LayoutInflater {
        // Use a custom theme for the fragment layout


        return super.onGetLayoutInflater(savedInstanceState).cloneInContext(
            ContextThemeWrapper(
                requireContext(), R.style.DarkTheme
            )
        )
    }

    override fun onResume() {
        super.onResume()
        updateStatusBar()
    }

    private fun updateStatusBar() {
        val decor: View? = activity?.window?.decorView


        decor?.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR


    }


    private var permissionGranted = false


    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun successEvent(event: HideFeedFloatingActionButton) {
        Log.d("HideFeedFloatingActionButton", "successEvent: HideFeedFloatingActionButton ")


                fileFloatingActionButton.visibility = View.GONE
                vnFloatingActionButton.visibility = View.GONE
                fabAction.visibility = View.GONE
                tabLayout.visibility = View.VISIBLE
                val params = viewPager2.layoutParams as ViewGroup.MarginLayoutParams
                val newMarginBottomPx =
                    resources.getDimensionPixelSize(R.dimen.feed_new_margin_bottom) // Replace with your desired margin in pixels
                params.bottomMargin = newMarginBottomPx
                viewPager2.layoutParams = params
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun successEvent(event: ShowFeedFloatingActionButton) {

        fabAction.visibility = View.VISIBLE
        tabLayout.visibility = View.VISIBLE
        vnFloatingActionButton.visibility = View.GONE
        val params = viewPager2.layoutParams as ViewGroup.MarginLayoutParams
        val newMarginBottomPx =
            resources.getDimensionPixelSize(R.dimen.feed_reset_margin_bottom)
        // Replace with your desired margin in pixels
        params.bottomMargin = newMarginBottomPx
        viewPager2.layoutParams = params
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun showVNDialog() {
        dialog = BottomSheetDialog(requireContext())  // Remove 'val' - use class variable
        dialog?.setContentView(R.layout.vn_record_layout)

        // Initialize ALL views from the layout
        deleteVN = dialog?.findViewById(R.id.deleteVN)!!
        recordVN = dialog?.findViewById<ImageView>(R.id.recordVN)!!
        playVnAudioBtn = dialog?.findViewById<ImageView>(R.id.playVnAudioBtn)!!
        sendVN = dialog?.findViewById<ImageView>(R.id.sendVN)!!
        timerTv = dialog?.findViewById<TextView>(R.id.timerTv)!!
        secondTimerTv = dialog?.findViewById<TextView>(R.id.secondTimerTv)!!
        wave = dialog?.findViewById<WaveformSeekBar>(R.id.wave)!!
        playAudioLayout = dialog?.findViewById<LinearLayout>(R.id.playVNRecorded)!!

        // Get the waveform containers
        val waveformScrollView = dialog?.findViewById<HorizontalScrollView>(R.id.waveformScrollView)!!
        val waveDotsContainer = dialog?.findViewById<LinearLayout>(R.id.waveDotsContainer)!!

        // Clear any existing views
        waveDotsContainer.removeAllViews()
        waveBars.clear()

        // Set initial visibility
        timerTv!!.visibility = View.VISIBLE
        timerTv!!.text = "00:00"
        playAudioLayout!!.visibility = View.GONE
        wave!!.visibility = View.GONE
        waveformScrollView.visibility = View.VISIBLE
        waveDotsContainer.visibility = View.VISIBLE

        playerTimerTv = playAudioLayout

        Log.d(TAG, "showVNDialog: All views initialized")

        val dialogView = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        dialogView?.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up))

        // Start recording AFTER all views are initialized
        startRecording()

        deleteVN!!.setOnClickListener {
            deleteRecording()
            if (player?.isPlaying == true) {
                stopPlaying()
            }
            dialog?.dismiss()
        }

        recordVN!!.setOnClickListener {
            Log.d(TAG, "recordVN clicked - isPaused: $isPaused, isRecording: $isRecording")
            when {
                isPaused -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        resumeRecording()
                    }
                }
                isRecording -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        pauseRecording()
                    }
                }
                else -> Log.d("recordVN", "onCreate: else in vn record btn on click")
            }
        }

        sendVN!!.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                Log.d(TAG, "sendVN: recorded files size ${recordedAudioFiles.size}")
                Log.d(TAG, "sendVN: wasPaused $wasPaused")

                if (isRecording && !isPaused) {
                    try {
                        isListeningToAudio = false
                        val currentTime = System.currentTimeMillis()
                        recordingElapsedTime += (currentTime - recordingStartTime)
                        mediaRecorder?.apply {
                            stop()
                            release()
                        }
                        mediaRecorder = null
                        timerHandler.removeCallbacksAndMessages(null)
                        Log.d("SendVN", "Stopped recording before sending")
                    } catch (e: Exception) {
                        Log.e("SendVN", "Error stopping recording: ${e.message}")
                    }
                }

                if (!wasPaused && isRecording) {
                    Log.d("SendVN", "When sending vn was paused was false")
                    mixVoiceNote()
                    delay(500)
                }

                lifecycleScope.launch(Dispatchers.Main) {
                    delay(500)
                    stopRecording()
                }
            }
            dialog?.dismiss()
        }

        dialog?.setOnDismissListener {
            if (isRecording && !isPaused) {
                try {
                    timer.stop()
                    isListeningToAudio = false
                    audioRecord?.release()
                    audioRecord = null
                } catch (e: Exception) {
                    Log.e(TAG, "Error stopping timer: ${e.message}")
                }
            }
        }

        dialog?.show()
    }


    //  ADD THESE VARIABLES AT THE TOP OF YOUR CLASS

    internal enum class VoiceNoteState {
        IDLE,
        RECORDING,
        PLAYING,
        PAUSED
    }


    //  REPLACE pauseRecording COMPLETELY

    @RequiresApi(Build.VERSION_CODES.R)
    private fun pauseRecording() {
        val TAG = "pauseRecording"
        if (isRecording && !isPaused) {
            try {
                Log.d(TAG, "Pausing recording...")

                isListeningToAudio = false

                // **CRITICAL FIX: Calculate elapsed time BEFORE stopping recorder**
                val currentTime = System.currentTimeMillis()
                recordingElapsedTime += (currentTime - recordingStartTime)

                mediaRecorder?.apply {
                    stop()
                    release()
                }
                mediaRecorder = null
            } catch (e: Exception) {
                Log.d(TAG, "Failed to stop media recorder: $e")
                e.printStackTrace()
            }

            isPaused = true

            // **CRITICAL FIX: Stop the recording timer**
            timerHandler.removeCallbacksAndMessages(null)

            // **CRITICAL FIX: Update both timers to show current recorded duration**
            requireActivity().runOnUiThread {
                val seconds = (recordingElapsedTime / 1000) % 60
                val minutes = (recordingElapsedTime / 1000) / 60
                val formatted = String.format("%02d:%02d", minutes, seconds)
                timerTv?.text = formatted
                secondTimerTv?.text = formatted
            }

            // **CRITICAL FIX: Use updateVoiceNoteUserInterfaceState**
            updateVoiceNoteUserInterfaceState(VoiceNoteState.PAUSED)
            recordVN!!.setImageResource(R.drawable.mic_2)

            Log.d(TAG, "list of recordings size: ${recordedAudioFiles.size}")
            Log.d(TAG, "list of recordings: $recordedAudioFiles")

            mixVoiceNote()
        }
    }

//  REPLACE resumeRecording COMPLETELY

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun resumeRecording() {
        if (isPaused) {
            Log.d(TAG, "Resuming recording...")

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

            // **CRITICAL FIX: Resume timer from where it left off**
            recordingStartTime = System.currentTimeMillis()
            updateRecordingTimer()

            playVnAudioBtn.setImageResource(R.drawable.play_svgrepo_com)
            recordVN!!.setImageResource(R.drawable.baseline_pause_white_24)

            // **CRITICAL FIX: Use updateVoiceNoteUserInterfaceState**
            updateVoiceNoteUserInterfaceState(VoiceNoteState.RECORDING)

            recordedAudioFiles.add(outputFile)

            // Resume audio listening
            Thread {
                listenToAudio()
            }.start()
        }
    }

//  ADD THIS NEW FUNCTION

    private fun updateVoiceNoteUserInterfaceState(newState: VoiceNoteState) {
        voiceNoteState = newState

        val waveformScrollView = dialog?.findViewById<HorizontalScrollView>(R.id.waveformScrollView)
        val waveDotsContainer = dialog?.findViewById<LinearLayout>(R.id.waveDotsContainer)

        when (newState) {
            VoiceNoteState.RECORDING -> {
                timerTv?.visibility = View.VISIBLE
                playAudioLayout?.visibility = View.GONE
                wave?.visibility = View.GONE
                waveformScrollView?.visibility = View.VISIBLE
                waveDotsContainer?.visibility = View.VISIBLE
            }

            VoiceNoteState.PLAYING -> {
                timerTv?.visibility = View.GONE
                playAudioLayout?.visibility = View.VISIBLE
                wave?.visibility = View.GONE
                playVnAudioBtn.setImageResource(R.drawable.baseline_pause_black)
                waveformScrollView?.visibility = View.VISIBLE
                waveDotsContainer?.visibility = View.VISIBLE
            }

            VoiceNoteState.PAUSED -> {
                timerTv?.visibility = View.GONE
                playAudioLayout?.visibility = View.VISIBLE
                wave?.visibility = View.GONE
                playVnAudioBtn.setImageResource(R.drawable.play_svgrepo_com)
                waveformScrollView?.visibility = View.VISIBLE
                waveDotsContainer?.visibility = View.VISIBLE

                // Scroll to left to show full waveform when paused
                waveformScrollView?.post {
                    waveformScrollView.scrollTo(0, 0)
                }
            }

            VoiceNoteState.IDLE -> {
                timerTv?.visibility = View.VISIBLE
                playAudioLayout?.visibility = View.GONE
                wave?.visibility = View.GONE
                waveformScrollView?.visibility = View.GONE
                waveDotsContainer?.visibility = View.GONE
                clearWaveform()
            }
        }
    }

    //  REPLACE mixVN COMPLETELY

    private fun mixVoiceNote() {
        val TAG = "mixVoiceNote"
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
                    // Not used
                }

                override fun onEnd() {
                    requireActivity().runOnUiThread {
                        audioMixer.release()
                        mixingCompleted = true // **CRITICAL FIX: Set this flag**

                        val file = File(outputVnFile)
                        Log.d(TAG, "onEnd: output vn file exists ${file.exists()}")
                        Log.d(TAG, "onEnd: media muxed success")

                        // **CRITICAL FIX: Keep waveform visible, hide wave seekbar**
                        val waveformScrollView = dialog?.findViewById<HorizontalScrollView>(R.id.waveformScrollView)
                        val waveDotsContainer = dialog?.findViewById<LinearLayout>(R.id.waveDotsContainer)
                        waveformScrollView?.visibility = View.VISIBLE
                        waveDotsContainer?.visibility = View.VISIBLE
                        wave?.visibility = View.GONE // Don't show WaveformSeekBar

                        playVnAudioBtn.setOnClickListener {
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
                                    pauseVoiceNote(currentProgress)
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

    //  REPLACE startPlaying COMPLETELY

    private fun startPlaying(vnAudio: String) {
        EventBus.getDefault().post(PauseShort(true))
        isAudioVNPlaying = true
        vnRecordAudioPlaying = true

        // **CRITICAL FIX: Update UI state**
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
        updatePlaybackTimer() // Start playback timer
    }

    //  ADD THIS NEW FUNCTION

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
                        secondTimerTv?.text = String.format("%02d:%02d", currentMinutes, currentSeconds)
                        timerHandler.postDelayed(this, 100)
                    } catch (e: Exception) {
                        Log.e("PlaybackTimer", "Error updating timer: ${e.message}")
                    }
                }
            }
        }
        timerHandler.post(playbackTimerRunnable!!)
    }

    //  ADD THIS NEW FUNCTION

    private fun stopPlaybackTimerRunnable() {
        playbackTimerRunnable?.let { timerHandler.removeCallbacks(it) }
        playbackTimerRunnable = null
    }

    //  REPLACE pauseVn COMPLETELY

    @SuppressLint("DefaultLocale")
    private fun pauseVoiceNote(progress: Int) {
        Log.d("pauseVn", "vnRecordProgress $vnRecordProgress..... progress $progress")

        val waveformScrollView = dialog?.findViewById<HorizontalScrollView>(R.id.waveformScrollView)
        val scrollAnimator = waveformScrollView?.tag as? ValueAnimator
        scrollAnimator?.cancel()

        player?.pause()
        player?.seekTo(progress)

        isAudioVNPlaying = false
        isAudioVNPaused = true
        isOnRecordDurationOnPause = true

        stopPlaybackTimerRunnable()

        // Stop animations but keep waveforms visible
        stopWaveDotsAnimation()

        // Show current playback position
        val currentMinutes = (progress / 1000) / 60
        val currentSeconds = (progress / 1000) % 60
        secondTimerTv?.text = String.format("%02d:%02d", currentMinutes, currentSeconds)

        updateVoiceNoteUserInterfaceState(VoiceNoteState.PAUSED)
    }

    //  ADD THIS NEW FUNCTION

    @SuppressLint("DefaultLocale")
    private fun stopPlayingOnCompletion() {
        val waveformScrollView = dialog?.findViewById<HorizontalScrollView>(R.id.waveformScrollView)
        val scrollAnimator = waveformScrollView?.tag as? ValueAnimator
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

        playVnAudioBtn.setImageResource(R.drawable.play_svgrepo_com)

        val totalMinutes = (totalDuration / 1000) / 60
        val totalSeconds = (totalDuration / 1000) % 60
        secondTimerTv?.text = String.format("%02d:%02d", totalMinutes, totalSeconds)

        vnRecordProgress = 0

        // Scroll back to start
        waveformScrollView?.post {
            waveformScrollView.scrollTo(0, 0)
        }
    }

    //  REPLACE stopPlaying COMPLETELY

    private fun stopPlaying() {
        val waveformScrollView = dialog?.findViewById<HorizontalScrollView>(R.id.waveformScrollView)
        val scrollAnimator = waveformScrollView?.tag as? ValueAnimator
        scrollAnimator?.cancel()

        playVnAudioBtn.setImageResource(R.drawable.play_svgrepo_com)
        player?.release()
        player = null
        isAudioVNPlaying = false
        vnRecordAudioPlaying = false
        isOnRecordDurationOnPause = false

        stopWaveDotsAnimation()
        updateVoiceNoteUserInterfaceState(VoiceNoteState.PAUSED)

        stopPlaybackTimerRunnable()
        wave?.progress = 0F
        vnRecordProgress = 0
    }

    //  ADD THIS NEW FUNCTION

    private fun animatePlaybackWaves() {
        val waveformScrollView = dialog?.findViewById<HorizontalScrollView>(R.id.waveformScrollView)
        val waveDotsContainer = dialog?.findViewById<LinearLayout>(R.id.waveDotsContainer)
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
            waveformScrollView?.post {
                val maxScroll = ((waveDotsContainer?.width ?: 0) - (waveformScrollView?.width ?: 0)).coerceAtLeast(0)
                if (maxScroll > 0) {
                    val scrollAnimator = ValueAnimator.ofInt(maxScroll, 0).apply {
                        this.duration = duration
                        interpolator = LinearInterpolator()
                        addUpdateListener { animation ->
                            if (isAudioVNPlaying) {
                                val scrollX = animation.animatedValue as Int
                                waveformScrollView?.scrollTo(scrollX, 0)
                            }
                        }
                    }
                    scrollAnimator.start()
                    waveformScrollView?.tag = scrollAnimator
                }
            }
        }
    }

    //  ADD THIS NEW FUNCTION

    private fun stopWaveDotsAnimation() {
        waveBars.forEach { bar ->
            (bar.tag as? ObjectAnimator)?.cancel()
            // Restore original height
            val storedHeight = bar.tag as? Float ?: 1.0f
            if (storedHeight is Float) {
                bar.scaleY = storedHeight
            }
        }
    }

    //  REPLACE deleteRecording COMPLETELY

    @SuppressLint("SetTextI18n")
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

            recordVN!!.setImageResource(com.uyscuti.social.call.R.drawable.ic_mic_on)
            sendVN!!.setBackgroundResource(R.drawable.ic_ripple)
            sendVN!!.isClickable = false

            updateVoiceNoteUserInterfaceState(VoiceNoteState.IDLE)

            timerTv?.text = "00:00"
            secondTimerTv?.text = "00:00"

            amplitudes = waveForm!!.clear()
            amps = 0
            timer.stop()

            Log.d(TAG, "deleteRecording: recorded files size ${recordedAudioFiles.size}")

            deleteVn()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "Error deleting recording: $e")
        }
    }

    //  REPLACE onTimerTick

    @SuppressLint("DefaultLocale")
    override fun onTimerTick(duration: String) {
        // This is called by the old Timer class - we don't use it anymore
        // Keep it empty or just log it
        Log.d(TAG, "onTimerTick (old timer): $duration - IGNORED")
    }

    //  REPLACE clearWaveform

    private fun clearWaveform() {
        val waveDotsContainer = dialog?.findViewById<LinearLayout>(R.id.waveDotsContainer)
        waveBars.forEach { bar ->
            (bar.tag as? ObjectAnimator)?.cancel()
        }
        waveDotsContainer?.removeAllViews()
        waveBars.clear()
    }
    

    // REPLACE or ADD the updateRecordingTimer function:

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
                    timerTv?.text = formatted

                    timerHandler.postDelayed(this, 100) // Update every 100ms
                }
            }
        })
    }

    // MAKE SURE your stopRecording function uses the correct file:

    @SuppressLint("SetTextI18n")
    private fun stopRecording() {
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
            wasPaused = false
            timerTv!!.text = "00:00"

            recordVN!!.setImageResource(com.uyscuti.social.call.R.drawable.ic_mic_on)
            sendVN!!.setBackgroundResource(R.drawable.ic_ripple)
            sendVN!!.isClickable = false

            amplitudes = waveForm!!.clear()
            amps = 0
            timer.stop()
            if (player?.isPlaying == true) {
                stopPlaying()
            }

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
                return
            }

            val durationString = getFormattedDuration(audioFilePath)
            val fileName = getFileNameFromLocalPath(audioFilePath)

            val intent = Intent(requireActivity(), UploadFeedActivity::class.java)
            intent.putExtra("vnFilePath", audioFilePath)
            intent.putExtra("vnFileName", fileName)
            intent.putExtra("vnDurationString", durationString)
            startActivityForResult(intent, REQUEST_UPLOAD_FEED_ACTIVITY)

            recordedAudioFiles.clear()
            mixingCompleted = false
        } catch (e: Exception) {
            Log.e(TAG, "stopRecording: $e")
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun onUploadVNButtonClick() {
        showVNDialog()
    }

    private fun startUploadAnimation(uploadDurationMs: Long = 10000) {
        uploadSeekBar.visibility = View.VISIBLE  // Changed from uploadSeekBar
        uploadSeekBar.progress = 0

        progressAnimator = ValueAnimator.ofInt(0, 100).apply {
            duration = uploadDurationMs
            interpolator = LinearInterpolator()

            addUpdateListener { animator ->
                val currentProgress = animator.animatedValue as Int
                uploadSeekBar.progress = currentProgress  // Changed from uploadSeekBar
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    hideUploadSeekBar()
                    showUploadSuccess()
                }
            })
            start()
        }
    }

    private fun hideUploadSeekBar() {
        uploadSeekBar.visibility = View.GONE
        uploadSeekBar.progress = 0
        isUploadInProgress = false
    }

    private fun showUploadSuccess() {
        val rootView: View = requireActivity().findViewById(android.R.id.content)
        val snackBar = Snackbar.make(rootView, "Feed upload successful", Snackbar.LENGTH_LONG)
        val snackBarTextColor = ContextCompat.getColor(requireContext(), R.color.green)
        val snackBarView = snackBar.view

        // Make background transparent
        snackBarView.setBackgroundColor(android.graphics.Color.TRANSPARENT)

        // Set text color to green
        snackBar.setTextColor(snackBarTextColor)

        snackBar.show()
    }

    private fun updateRecordWaveProgress(progress: Float) {

        CoroutineScope(Dispatchers.Main).launch {
            wave!!.progress = progress
            Log.d("updateWaveProgress", "updateWaveProgress: $progress")
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

    private fun calculateBarsNeededForFullWidth(): Int {
        val screenWidth = resources.displayMetrics.widthPixels
        val barWidth = dpToPx(4)
        val barMargin = dpToPx(6)
        val totalBarWidth = barWidth + barMargin
        return (screenWidth / totalBarWidth) + 5
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun listenToAudio() {
        try {
            Log.d(TAG, "listenToAudio: STARTING")

            val minBufferSize = AudioRecord.getMinBufferSize(
                44100,
                android.media.AudioFormat.CHANNEL_IN_MONO,
                android.media.AudioFormat.ENCODING_PCM_16BIT
            )

            Log.d(TAG, "listenToAudio: minBufferSize = $minBufferSize")

            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e(TAG, "listenToAudio: RECORD_AUDIO permission NOT GRANTED")
                return
            }

            Log.d(TAG, "listenToAudio: Creating AudioRecord...")
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                44100,
                android.media.AudioFormat.CHANNEL_IN_MONO,
                android.media.AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize * 2
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "listenToAudio: AudioRecord NOT INITIALIZED - state = ${audioRecord?.state}")
                return
            }

            Log.d(TAG, "listenToAudio: Starting AudioRecord recording...")
            audioRecord?.startRecording()

            val buffer = ShortArray(minBufferSize)
            var loopCount = 0

            Log.d(TAG, "listenToAudio: Entering main loop - isListeningToAudio=$isListeningToAudio, isRecording=$isRecording")

            while (isListeningToAudio && isRecording) {
                val readSize = audioRecord?.read(buffer, 0, minBufferSize) ?: 0
                loopCount++

                if (loopCount % 20 == 0) { // Log every 20 iterations (once per second)
                    Log.d(TAG, "listenToAudio: Loop #$loopCount - readSize=$readSize")
                }

                if (readSize > 0) {
                    // Calculate RMS (Root Mean Square) for better amplitude detection
                    var sum = 0.0
                    for (i in 0 until readSize) {
                        sum += (buffer[i].toDouble() * buffer[i].toDouble())
                    }
                    val rms = sqrt(sum / readSize)

                    // Normalize amplitude to 0-1 range
                    val normalizedAmplitude = (rms / 5000.0).coerceIn(0.0, 1.0).toFloat()

                    if (loopCount % 20 == 0) {
                        Log.d(TAG, "listenToAudio: rms=$rms, normalizedAmplitude=$normalizedAmplitude")
                    }

                    requireActivity().runOnUiThread {
                        try {
                            if (normalizedAmplitude > 0.05f) { // Sound detected
                                val heightMultiplier = 0.3f + (normalizedAmplitude * 2.2f)
                                addWaveBarForSound(heightMultiplier)

                                if (loopCount % 20 == 0) {
                                    Log.d(TAG, "listenToAudio: Added sound bar - height=$heightMultiplier")
                                }
                            } else { // No sound
                                addIdleDottedBarAtEnd()

                                if (loopCount % 20 == 0) {
                                    Log.d(TAG, "listenToAudio: Added idle dot")
                                }
                            }
                            scrollToRight()
                        } catch (e: Exception) {
                            Log.e(TAG, "listenToAudio: Error in UI thread - ${e.message}")
                            e.printStackTrace()
                        }
                    }
                }

                Thread.sleep(50) // Update every 50ms
            }

            Log.d(TAG, "listenToAudio: Exited main loop - total iterations=$loopCount")
            audioRecord?.release()
            audioRecord = null
            Log.d(TAG, "listenToAudio: FINISHED")
        } catch (e: Exception) {
            Log.e(TAG, "listenToAudio: EXCEPTION - ${e.message}")
            e.printStackTrace()
        }
    }

// ========== REPLACE addWaveBarForSound WITH LOGGING ==========

    private fun addWaveBarForSound(heightMultiplier: Float) {
        try {
            val waveDotsContainer = dialog?.findViewById<LinearLayout>(R.id.waveDotsContainer)

            if (waveDotsContainer == null) {
                Log.e(TAG, "addWaveBarForSound: waveDotsContainer is NULL!")
                return
            }

            val bar = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    dpToPx(4), // 4dp width
                    dpToPx(48) // 48dp max height
                ).apply {
                    marginEnd = dpToPx(6)
                    gravity = android.view.Gravity.CENTER_VERTICAL
                }
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    setColor(Color.parseColor("#3284fc")) // Blue color
                    cornerRadius = dpToPx(2).toFloat()
                }
                scaleY = heightMultiplier.coerceIn(0.2f, 2.5f)
                alpha = 1.0f
                tag = heightMultiplier
            }

            waveDotsContainer.addView(bar)
            waveBars.add(bar)

            Log.d(TAG, "addWaveBarForSound: Added bar - total bars=${waveBars.size}, container children=${waveDotsContainer.childCount}")

            if (waveBars.size > maxWaveBars) {
                waveDotsContainer.removeViewAt(0)
                waveBars.removeAt(0)
            }

            scrollToRight()
        } catch (e: Exception) {
            Log.e(TAG, "addWaveBarForSound: EXCEPTION - ${e.message}")
            e.printStackTrace()
        }
    }

// ========== REPLACE addIdleDottedBarAtEnd WITH LOGGING ==========

    private fun addIdleDottedBarAtEnd() {
        try {
            val waveDotsContainer = dialog?.findViewById<LinearLayout>(R.id.waveDotsContainer)

            if (waveDotsContainer == null) {
                Log.e(TAG, "addIdleDottedBarAtEnd: waveDotsContainer is NULL!")
                return
            }

            val bar = View(requireContext()).apply {
                val dotSize = dpToPx(5)
                layoutParams = LinearLayout.LayoutParams(
                    dotSize,
                    dotSize
                ).apply {
                    marginEnd = dpToPx(3)
                    gravity = android.view.Gravity.CENTER_VERTICAL
                }
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor("#3284fc"))
                }
                scaleY = 1.0f
                alpha = 1.0f
                tag = "idle_dot"
            }

            waveDotsContainer.addView(bar)
            waveBars.add(bar)

            if (waveBars.size > maxWaveBars) {
                waveDotsContainer.removeViewAt(0)
                waveBars.removeAt(0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "addIdleDottedBarAtEnd: EXCEPTION - ${e.message}")
            e.printStackTrace()
        }
    }

// ========== REPLACE initializeDottedWaveform WITH LOGGING ==========

    private fun initializeDottedWaveform() {
        try {
            val waveDotsContainer = dialog?.findViewById<LinearLayout>(R.id.waveDotsContainer)
            val waveformScrollView = dialog?.findViewById<HorizontalScrollView>(R.id.waveformScrollView)

            Log.d(TAG, "initializeDottedWaveform: waveDotsContainer=$waveDotsContainer")
            Log.d(TAG, "initializeDottedWaveform: waveformScrollView=$waveformScrollView")

            if (waveDotsContainer == null) {
                Log.e(TAG, "initializeDottedWaveform: waveDotsContainer is NULL!")
                return
            }

            if (waveformScrollView == null) {
                Log.e(TAG, "initializeDottedWaveform: waveformScrollView is NULL!")
                return
            }

            waveDotsContainer.removeAllViews()
            waveBars.clear()

            val barsToFill = calculateBarsNeededForFullWidth()
            Log.d(TAG, "initializeDottedWaveform: Creating $barsToFill initial bars")

            repeat(barsToFill) {
                addIdleDottedBarAtEnd()
            }

            Log.d(TAG, "initializeDottedWaveform: Created ${waveBars.size} bars, container has ${waveDotsContainer.childCount} children")

            waveformScrollView.post {
                val maxScroll = (waveDotsContainer.width - waveformScrollView.width).coerceAtLeast(0)
                Log.d(TAG, "initializeDottedWaveform: Scrolling to right - maxScroll=$maxScroll")
                if (maxScroll > 0) {
                    waveformScrollView.scrollTo(maxScroll, 0)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "initializeDottedWaveform: EXCEPTION - ${e.message}")
            e.printStackTrace()
        }
    }

// ========== REPLACE scrollToRight WITH LOGGING ==========

    private fun scrollToRight() {
        try {
            val waveDotsContainer = dialog?.findViewById<LinearLayout>(R.id.waveDotsContainer) ?: return
            val waveformScrollView = dialog?.findViewById<HorizontalScrollView>(R.id.waveformScrollView) ?: return

            waveformScrollView.post {
                val maxScroll = (waveDotsContainer.width - waveformScrollView.width).coerceAtLeast(0)
                if (maxScroll > 0) {
                    waveformScrollView.smoothScrollTo(maxScroll, 0)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "scrollToRight: EXCEPTION - ${e.message}")
            e.printStackTrace()
        }
    }

// ========== ADD THIS DIAGNOSTIC FUNCTION ==========

    private fun checkWaveformViewsStatus() {
        val waveDotsContainer = dialog?.findViewById<LinearLayout>(R.id.waveDotsContainer)
        val waveformScrollView = dialog?.findViewById<HorizontalScrollView>(R.id.waveformScrollView)

        Log.d(TAG, "=== WAVEFORM VIEWS STATUS ===")
        Log.d(TAG, "dialog = $dialog")
        Log.d(TAG, "waveDotsContainer = $waveDotsContainer")
        Log.d(TAG, "waveformScrollView = $waveformScrollView")

        if (waveDotsContainer != null) {
            Log.d(TAG, "waveDotsContainer.visibility = ${waveDotsContainer.visibility}")
            Log.d(TAG, "waveDotsContainer.childCount = ${waveDotsContainer.childCount}")
            Log.d(TAG, "waveDotsContainer.width = ${waveDotsContainer.width}")
            Log.d(TAG, "waveDotsContainer.height = ${waveDotsContainer.height}")
        }

        if (waveformScrollView != null) {
            Log.d(TAG, "waveformScrollView.visibility = ${waveformScrollView.visibility}")
            Log.d(TAG, "waveformScrollView.width = ${waveformScrollView.width}")
            Log.d(TAG, "waveformScrollView.height = ${waveformScrollView.height}")
        }

        Log.d(TAG, "waveBars.size = ${waveBars.size}")
        Log.d(TAG, "isListeningToAudio = $isListeningToAudio")
        Log.d(TAG, "isRecording = $isRecording")
        Log.d(TAG, "voiceNoteState = $voiceNoteState")
        Log.d(TAG, "===========================")
    }

// ========== UPDATE startRecording to call checkWaveformViewsStatus ==========

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun startRecording() {
        if (!permissionGranted) {
            ActivityCompat.requestPermissions(requireActivity(), permissions, REQUEST_CODE)
            return
        }
        try {
            Log.d(TAG, "startRecording: BEGIN")

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

            // Initialize timer variables and start updateRecordingTimer
            recordingStartTime = System.currentTimeMillis()
            recordingElapsedTime = 0L
            updateRecordingTimer()

            recordVN!!.setImageResource(R.drawable.baseline_pause_white_24)
            sendVN!!.setBackgroundResource(R.drawable.ic_ripple)
            deleteVN!!.setBackgroundResource(R.drawable.ic_ripple)

            // Update UI state
            updateVoiceNoteUserInterfaceState(VoiceNoteState.RECORDING)

            deleteVN!!.isClickable = true
            sendVN!!.isClickable = true
            recordedAudioFiles.add(outputFile)

            // **ADD DIAGNOSTIC CHECK**
            requireActivity().runOnUiThread {
                Handler(Looper.getMainLooper()).postDelayed({
                    checkWaveformViewsStatus()
                }, 500) // Check after 500ms
            }

            // Initialize waveform with dots
            initializeDottedWaveform()

            // Start audio listening in background thread
            Thread {
                listenToAudio()
            }.start()

            Log.d("VNFile", "Recording to: $outputFile")
            Log.d(TAG, "startRecording: COMPLETE")
        } catch (e: Exception) {
            Log.e(TAG, "startRecording: Failed - ${e.message}")
            e.printStackTrace()
        }
    }

}