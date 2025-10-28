package com.uyscuti.social.circuit.User_Interface.fragments

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
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.uyscuti.social.circuit.MainActivity
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.adapter.feed.FragmentPageAdapter
import com.uyscuti.social.circuit.eventbus.FeedFavoriteFollowUpdate
import com.uyscuti.social.circuit.eventbus.HideFeedFloatingActionButton
import com.uyscuti.social.circuit.eventbus.InformShortsFragment
import com.uyscuti.social.circuit.eventbus.ShowFeedFloatingActionButton
import com.uyscuti.social.circuit.model.PauseShort
import com.uyscuti.social.circuit.model.feed.SetAllFragmentScrollPosition
import com.uyscuti.social.circuit.User_Interface.fragments.feed.AllFragment
import com.uyscuti.social.circuit.User_Interface.fragments.feed.FavoriteFragment
import com.uyscuti.social.circuit.User_Interface.fragments.feed.FollowingFragment
import com.uyscuti.social.circuit.User_Interface.fragments.feed.UploadFeedActivity
import com.uyscuti.social.circuit.utils.AudioDurationHelper
import com.uyscuti.social.circuit.utils.Timer
import com.uyscuti.social.circuit.utils.WaveFormExtractor
import com.uyscuti.social.circuit.utils.audiomixer.AudioMixer
import com.uyscuti.social.circuit.utils.audiomixer.input.GeneralAudioInput
import com.uyscuti.social.circuit.utils.feedutils.deserializeFeedUploadDataList
import com.uyscuti.social.circuit.utils.waveformseekbar.SeekBarOnProgressChanged
import com.uyscuti.social.circuit.utils.waveformseekbar.WaveformSeekBar
import com.uyscuti.social.circuit.viewmodels.feed.GetFeedViewModel
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import androidx.annotation.RequiresExtension
import com.uyscuti.social.circuit.viewmodels.feed.FeedUploadViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.uyscuti.social.network.api.response.posts.Account
import com.uyscuti.social.network.api.response.posts.Author
import com.uyscuti.social.network.api.response.posts.Avatar
import com.uyscuti.social.network.api.response.posts.CoverImage
import com.uyscuti.social.network.api.response.posts.Post
import com.uyscuti.social.network.api.response.posts.Duration
import com.uyscuti.social.network.api.response.posts.FileName
import com.uyscuti.social.network.api.response.posts.FileType
import com.uyscuti.social.network.api.response.posts.NumberOfPageX
import com.uyscuti.social.network.api.response.posts.RepostedUser
import com.uyscuti.social.network.api.response.posts.ThumbnailX
import com.uyscuti.social.network.utils.LocalStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.io.IOException
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import androidx.work.WorkManager
import com.uyscuti.social.network.api.response.feed.FeedUploadResponse
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject
import android.media.AudioRecord
import com.uyscuti.social.circuit.adapter.feed.FeedAdapter
import kotlin.math.abs


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"





@AndroidEntryPoint
class FeedFragment : Fragment(), Timer.OnTimeTickListener {

    companion object {
        private const val TAG = "FeedFragment"
        private const val FEED_POST_POSITION_FROM_SHORTS = "feed_post_position_from_shorts"
        private const val REQUEST_UPLOAD_FEED_ACTIVITY = 1001

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

    private val PREFS_NAME = "LocalSettings"
    private val REQUEST_CODE = 2024

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.READ_MEDIA_IMAGES
    )

    @Inject
    lateinit var retrofitInterface: RetrofitInstance
    private lateinit var feedAdapter: FeedAdapter
    private var workManager: WorkManager? = null
    private var currentWorkerId: UUID? = null
    private val feedUploadViewModel: FeedUploadViewModel by activityViewModels()
    private val getFeedViewModel: GetFeedViewModel by activityViewModels()
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager2: ViewPager2
    private lateinit var adapter: FragmentPageAdapter
    private lateinit var fabAction: FloatingActionButton
    private lateinit var fileFloatingActionButton: FloatingActionButton
    private lateinit var vnFloatingActionButton: FloatingActionButton
    private lateinit var uploadSeekBar: SeekBar
    private lateinit var feedUploadView: ImageView
    private lateinit var feedCancelView: ImageView
    private var deleteVN: ImageView? = null
    private var recordVN: ImageView? = null
    private lateinit var playVnAudioBtn: ImageView
    private var sendVN: ImageView? = null
    private var timerTv: TextView? = null
    private var secondTimerTv: TextView? = null
    private var wave: WaveformSeekBar? = null
    private lateinit var waveformScrollView: HorizontalScrollView
    private lateinit var waveDotsContainer: LinearLayout
    private lateinit var recordingLayout: LinearLayout
    private lateinit var playVNRecorded: LinearLayout
    private var mixingCompleted = false
    private var progressAnimator: ValueAnimator? = null
    private var isUploadInProgress = false
    private var mediaRecorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private lateinit var outputFile: String
    private var outputVnFile: String = ""
    private val recordedAudioFiles = mutableListOf<String>()
    private lateinit var timer: Timer
    private var isRecording = false
    private var isPaused = false
    private var isAudioVNPlaying = false
    private var isAudioVNPaused = false
    private var isVnResuming = false
    private var vnRecordAudioPlaying = false
    private var isOnRecordDurationOnPause = false
    private var wasPaused = false
    private var clicked = false
    private var vnRecordProgress = 0
    private var feedPostPositionFromShorts = -1
    private lateinit var settings: SharedPreferences
    private var param1: String? = null
    private var param2: String? = null



    private val waveHandler = Handler(Looper.getMainLooper())
    private val maxWaveBars = 200
    private var waveBarCount = 0
    private val waveBars = mutableListOf<View>()
    private var audioRecord: AudioRecord? = null
    private var isListeningToAudio = false
    private var audioThread: Thread? = null

    private var permissionGranted = false

    private fun isMediaRecorderValid(): Boolean {
        return mediaRecorder != null && isRecording && !isPaused
    }

    private enum class RecorderState {
        IDLE, INITIALIZED, PREPARED, RECORDING, PAUSED, STOPPED, RELEASED
    }

    private var recorderState = RecorderState.IDLE

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

    private val rotateOpen: Animation by lazy {
        AnimationUtils.loadAnimation(requireActivity(), R.anim.rotate_open_anim)
    }
    private val rotateClose: Animation by lazy {
        AnimationUtils.loadAnimation(requireActivity(), R.anim.rotate_close_anim)
    }
    private val fromBottom: Animation by lazy {
        AnimationUtils.loadAnimation(requireActivity(), R.anim.from_bottom_anim)
    }
    private val toBottom: Animation by lazy {
        AnimationUtils.loadAnimation(requireActivity(), R.anim.to_bottom_anim)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        feedPostPositionFromShorts = arguments?.getInt(FEED_POST_POSITION_FROM_SHORTS, -1)!!
        EventBus.getDefault().register(this)
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @OptIn(UnstableApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as? MainActivity)?.showAppBar()
        activity?.window?.statusBarColor = ContextCompat.getColor(requireContext(), R.color.white)
        activity?.window?.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.white)
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
        uploadSeekBar = view.findViewById(R.id.uploadSeekBar)
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
        tabLayout.addTab(tabLayout.newTab().setText("Business"))
        viewPager2.adapter = adapter

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    viewPager2.currentItem = tab.position
                    when (tab.position) {

                        0 -> {
                            Log.d("onTabSelected", "All Fragment Selected : ${tab.position}")
                            val fragment = AllFragment()
                            fragment.forShow()
                        }

                        1 -> {
                            Log.d("onTabSelected", "Following Fragment Selected: ${tab.position}")
                            val fragment = FollowingFragment()
                            fragment.forShow()
                        }

                        2 -> {
                            Log.d("onTabSelected", "Favorite Fragment Selected: ${tab.position}")
                            val fragment = FavoriteFragment()
                            fragment.forShow()
                        }


                    }
                }
            }
            override fun onTabUnselected(p0: TabLayout.Tab?) {}
            override fun onTabReselected(p0: TabLayout.Tab?) {}
        })

        Log.d("feedPostPositionFromShorts",
            "onCreateView: of feed fragment $feedPostPositionFromShorts viewPager2.currentItem ${viewPager2.currentItem}")

        if (feedPostPositionFromShorts != -1) {
            val currentFragment = adapter.getFragment(viewPager2.currentItem)
            if (currentFragment is AllFragment) {
                Log.d(TAG, "onCreateView: current frag $feedPostPositionFromShorts")
                currentFragment.setPositionFromShorts(
                    SetAllFragmentScrollPosition(true, feedPostPositionFromShorts))
            } else {
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


    private fun setVisibility(clicked: Boolean) {
        if (!clicked) {
            fileFloatingActionButton.visibility = View.VISIBLE
            vnFloatingActionButton.visibility = View.VISIBLE
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
            val fileTypes: MutableList<FileType> = mutableListOf()
            val duration: MutableList<Duration> = mutableListOf()
            val fileIds: MutableList<String> = mutableListOf()
            val thumbnail: MutableList<ThumbnailX> = mutableListOf()
            val numberOfPages: MutableList<NumberOfPageX> = mutableListOf()
            val fileName: MutableList<FileName> = mutableListOf()

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
                            _id = "", fileId = it.fileId, url = it.images.imagePath,
                            localPath = it.images.compressedImagePath, mimeType = null
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

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun onUploadVNButtonClick() {
        showVoiceNoteDialog()
    }

    private fun startUploadAnimation(uploadDurationMs: Long = 10000) {
        uploadSeekBar.visibility = View.VISIBLE
        uploadSeekBar.progress = 0
        progressAnimator = ValueAnimator.ofInt(0, 100).apply {
            duration = uploadDurationMs
            interpolator = LinearInterpolator()
            addUpdateListener { animator ->
                val currentProgress = animator.animatedValue as Int
                uploadSeekBar.progress = currentProgress
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
        val snackBar = Snackbar.make(rootView, "Feed Upload Successful", Snackbar.LENGTH_LONG)
        val snackBarTextColor = ContextCompat.getColor(requireContext(), R.color.green)
        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(Color.TRANSPARENT)
        snackBar.setTextColor(snackBarTextColor)
        snackBar.show()
    }

    override fun onGetLayoutInflater(savedInstanceState: Bundle?): LayoutInflater {
        return super.onGetLayoutInflater(savedInstanceState).cloneInContext(
            ContextThemeWrapper(requireContext(), R.style.DarkTheme)
        )
    }

    override fun onResume() {
        super.onResume()
    }

    private fun updateRecordWaveProgress(progress: Float) {
        CoroutineScope(Dispatchers.Main).launch {
            wave!!.progress = progress
            Log.d("updateWaveProgress", "updateWaveProgress: $progress")
        }
    }


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
        Log.d("HideFeedFloatingActionButton", "successEvent: HideFeedFloatingActionButton")
        fileFloatingActionButton.visibility = View.GONE
        vnFloatingActionButton.visibility = View.GONE
        fabAction.visibility = View.VISIBLE
        tabLayout.visibility = View.VISIBLE
        val params = viewPager2.layoutParams as ViewGroup.MarginLayoutParams
        val newMarginBottomPx = resources.getDimensionPixelSize(R.dimen.feed_new_margin_bottom)
        params.bottomMargin = newMarginBottomPx
        viewPager2.layoutParams = params
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun successEvent(event: ShowFeedFloatingActionButton) {
        fabAction.visibility = View.VISIBLE
        tabLayout.visibility = View.VISIBLE
        vnFloatingActionButton.visibility = View.GONE
        val params = viewPager2.layoutParams as ViewGroup.MarginLayoutParams
        val newMarginBottomPx = resources.getDimensionPixelSize(R.dimen.feed_reset_margin_bottom)
        params.bottomMargin = newMarginBottomPx
        viewPager2.layoutParams = params
    }

    private fun getFormattedDuration(filePath: String): String {
        val duration = AudioDurationHelper.getLocalAudioDuration(filePath)
        return if (duration != null) {
            val minutes = (duration / 1000) / 60
            val seconds = (duration / 1000) % 60
            String.format("%02d:%02d", minutes, seconds)
        } else {
            "00:00"
        }
    }

    private fun getOutputFilePath(prefix: String): String {
        val timestamp = System.currentTimeMillis()
        val fileName = "${prefix}_$timestamp.m4a"
        return "${requireContext().getExternalFilesDir(null)?.absolutePath}/$fileName"
    }

    private fun deleteFiles(files: List<String>): Boolean {
        var allDeleted = true
        files.forEach { filePath ->
            val file = File(filePath)
            if (file.exists()) {
                if (!file.delete()) {
                    allDeleted = false
                }
            }
        }
        return allDeleted
    }


    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun showVoiceNoteDialog() {
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(R.layout.vn_record_layout)

        deleteVN = dialog.findViewById<ImageView>(R.id.deleteVN)
        recordVN = dialog.findViewById<ImageView>(R.id.recordVN)
        playVnAudioBtn = dialog.findViewById<ImageView>(R.id.playVnAudioBtn)!!
        sendVN = dialog.findViewById<ImageView>(R.id.sendVN)
        timerTv = dialog.findViewById<TextView>(R.id.timerTv)
        secondTimerTv = dialog.findViewById<TextView>(R.id.secondTimerTv)
        waveformScrollView = dialog.findViewById<HorizontalScrollView>(R.id.waveformScrollView)!!
        waveDotsContainer = dialog.findViewById<LinearLayout>(R.id.waveDotsContainer)!!
        wave = dialog.findViewById<WaveformSeekBar>(R.id.wave)
        recordingLayout = dialog.findViewById<LinearLayout>(R.id.recordingLayout)!!
        playVNRecorded = dialog.findViewById<LinearLayout>(R.id.playVNRecorded)!!

        if (deleteVN == null || recordVN == null || sendVN == null ||
            timerTv == null || secondTimerTv == null || wave == null) {
            Log.e(TAG, "One or more views not found in vn_record_layout")
            Toast.makeText(requireContext(), "Failed to initialize voice note recorder", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            return
        }

        val dialogView = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        dialogView?.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up))

        startRecordingVoiceNote()

        deleteVN?.setOnClickListener {
            deleteVoiceNoteRecording()
            if (player?.isPlaying == true) {
                stopPlaying()
            }
            dialog.dismiss()
        }

        recordVN?.setOnClickListener {
            when {
                isPaused -> resumeRecording()
                isRecording -> pauseVoiceNoteRecording()
                else -> Log.d("recordVN", "No action needed")
            }
        }

        playVnAudioBtn.setOnClickListener {
            Log.d("playVnAudioBtn", "play vn button clicked")
            when {
                !isAudioVNPlaying -> {
                    playVnAudioBtn.setImageResource(R.drawable.baseline_pause_white_24)
                    Log.d("playVnAudioBtn", "play vn")
                    startPlaying(outputVnFile)
                }
                else -> {
                    Log.d("playVnAudioBtn", "pause VN")
                    playVnAudioBtn.setImageResource(R.drawable.play_svgrepo_com)
                    pausedVoiceNote(vnRecordProgress)
                }
            }
        }

        sendVN?.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                Log.d(TAG, "sendVN: recorded files size ${recordedAudioFiles.size}")
                Log.d(TAG, "sendVN: wasPaused $wasPaused")
                if (!wasPaused) {
                    mediaRecorder.apply {
                        this!!.stop()
                        release()
                    }
                    Log.d("SendVN", "When sending vn was paused was false")
                    mixVoiceNote()
                }
                lifecycleScope.launch(Dispatchers.Main) {
                    delay(500)
                    stopRecordingVoiceNote()
                }
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun creatingVoiceNoteAsOriginalPost(filePath: String, fileName: String, duration: String): Post {
        val profilePic = settings.getString("profile_pic", "").toString()
        val userId = settings.getString("_id", "").toString()
        val mongoDbTimeStamp = generateMongoDBTimestamp()

        val avatar = Avatar(_id = "", localPath = "", url = "")
        val account = Account(
            _id = "", avatar = avatar, createdAt = "", email = "", updatedAt = "",
            username = LocalStorage.getInstance(requireContext()).getUsername()
        )
        val author = Author(
            _id = userId, account = account, firstName = "", lastName = "", bio = "",
            countryCode = "", createdAt = "", __v = 0, dob = "", owner = "", location = "",
            updatedAt = "", phoneNumber = "",
            coverImage = CoverImage(_id = "", localPath = "", url = profilePic)
        )

        val fileId = UUID.randomUUID().toString()

        val filesList = ArrayList<com.uyscuti.social.network.api.response.posts.File>().apply {
            add(com.uyscuti.social.network.api.response.posts.File(
                _id = fileId,
                fileId = fileId,
                url = filePath,
                localPath = filePath,
                mimeType = "audio/m4a"
            ))
        }

        val fileTypes = mutableListOf<FileType>().apply {
            add(FileType(fileId, "vn"))
        }
        val durations = mutableListOf<Duration>().apply {
            add(Duration(fileId, duration))
        }
        val fileNames = mutableListOf<FileName>().apply {
            add(FileName(fileId, fileName))
        }

        return Post(
            __v = 1,
            _id = "",
            author = author,
            bookmarkCount = 0,
            comments = 0,
            content = "",
            contentType = "vn",
            createdAt = mongoDbTimeStamp,
            duration = durations,
            feedShortsBusinessId = "",
            fileIds = listOf(fileId),
            fileNames = fileNames,
            fileSizes = listOf(),
            fileTypes = fileTypes,
            files = filesList,
            isBookmarked = false,
            isExpanded = false,
            isFollowing = false,
            isLiked = false,
            isLocal = true,
            isReposted = false,
            likes = 0,
            numberOfPages = listOf(),
            originalPost = listOf(),
            repostedByUserId = "",
            repostedUser = RepostedUser(
                _id = "", avatar = Avatar(_id = "", localPath = "", url = ""), bio = "",
                coverImage = CoverImage(_id = "", localPath = "", url = ""), createdAt = "",
                email = "", firstName = "", lastName = "", owner = "", updatedAt = "", username = ""
            ),
            repostedUsers = listOf(),
            tags = listOf(),
            thumbnail = listOf(),
            updatedAt = mongoDbTimeStamp,
            shareCount = 0,
            repostCount = 0
        )
    }

    private fun convertUploadResponseToPost(uploadResponse: FeedUploadResponse): Post {
        val data = uploadResponse.data
        val profilePic = settings.getString("profile_pic", "").toString()
        val userId = settings.getString("_id", "").toString()

        // Map files from response (Image class from API)
        val filesList = ArrayList<com.uyscuti.social.network.api.response.posts.File>().apply {
            data.files.forEach { file ->
                add(com.uyscuti.social.network.api.response.posts.File(
                    _id = file._id,
                    fileId = file._id, // Use _id as fileId
                    url = file.url,
                    localPath = file.localPath,
                    mimeType = "audio/m4a"
                ))
            }
        }

        // Extract file metadata
        val fileId = data.files.firstOrNull()?._id ?: ""

        val fileTypes = mutableListOf<FileType>().apply {
            add(FileType(fileId, "vn"))
        }

        // Get duration from file name or use default
        val duration = getFormattedDuration(data.files.firstOrNull()?.localPath ?: "")
        val durations = mutableListOf<Duration>().apply {
            add(Duration(fileId, duration))
        }

        val fileNames = mutableListOf<FileName>().apply {
            val fileName = data.files.firstOrNull()?.localPath?.let { File(it).name } ?: "voice_note.m4a"
            add(FileName(fileId, fileName))
        }

        // Create Avatar from API response
        val avatar = com.uyscuti.social.network.api.response.posts.Avatar(
            _id = data.author.account.avatar._id,
            localPath = data.author.account.avatar.localPath,
            url = data.author.account.avatar.url
        )

        // Create Account from API response
        val account = com.uyscuti.social.network.api.response.posts.Account(
            _id = data.author.account._id,
            avatar = avatar,
            createdAt = data.author.createdAt,
            email = data.author.account.email,
            updatedAt = data.author.updatedAt,
            username = data.author.account.username
        )

        // Create CoverImage from API response
        val coverImage = com.uyscuti.social.network.api.response.posts.CoverImage(
            _id = data.author.coverImage._id,
            localPath = data.author.coverImage.localPath,
            url = data.author.coverImage.url
        )

        // Create Author from API response
        val author = com.uyscuti.social.network.api.response.posts.Author(
            _id = data.author._id,
            account = account,
            firstName = data.author.firstName,
            lastName = data.author.lastName,
            bio = data.author.bio,
            countryCode = data.author.countryCode,
            createdAt = data.author.createdAt,
            __v = data.author.__v,
            dob = data.author.dob,
            owner = data.author.owner,
            location = data.author.location,
            updatedAt = data.author.updatedAt,
            phoneNumber = data.author.phoneNumber,
            coverImage = coverImage
        )

        return Post(
            __v = data.__v,
            _id = data._id,
            author = author,
            bookmarkCount = 0,
            comments = data.comments,
            content = data.content,
            contentType = "vn",
            createdAt = data.createdAt,
            duration = durations,
            feedShortsBusinessId = "",
            fileIds = listOf(fileId),
            fileNames = fileNames,
            fileSizes = listOf(),
            fileTypes = fileTypes,
            files = filesList,
            isBookmarked = data.isBookmarked,
            isExpanded = false,
            isFollowing = false,
            isLiked = data.isLiked,
            isLocal = false,  // No longer local after upload
            isReposted = false,
            likes = data.likes,
            numberOfPages = listOf(),
            originalPost = listOf(),
            repostedByUserId = "",
            repostedUser = RepostedUser(
                _id = "",
                avatar = com.uyscuti.social.network.api.response.posts.Avatar(_id = "", localPath = "", url = ""),
                bio = "",
                coverImage = com.uyscuti.social.network.api.response.posts.CoverImage(_id = "", localPath = "", url = ""),
                createdAt = "",
                email = "",
                firstName = "",
                lastName = "",
                owner = "",
                updatedAt = "",
                username = ""
            ),
            repostedUsers = listOf(),
            tags = data.tags,
            thumbnail = listOf(),
            updatedAt = data.updatedAt,
            shareCount = 0,
            repostCount = 0
        )
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun startRecordingVoiceNote() {
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
            recordVN?.setImageResource(R.drawable.baseline_pause_white_24)
            sendVN?.setBackgroundResource(R.drawable.ic_ripple)
            deleteVN?.setBackgroundResource(R.drawable.ic_ripple)

            // Show recording UI, hide playback UI
            recordingLayout?.visibility = View.VISIBLE
            timerTv?.visibility = View.VISIBLE
            playVNRecorded?.visibility = View.GONE  // ✅ Only set once

            deleteVN?.isClickable = true
            sendVN?.isClickable = true

            recordedAudioFiles.add(outputFile)
            Log.d("VNFile", outputFile)

            initializeDottedWaveform()
            startTimer()

            Thread {
                listenToAudio()
            }.start()

        } catch (e: Exception) {
            Log.d("VNFile", "Failed to record audio properly: ${e.message}")
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun pauseVoiceNoteRecording() {
        if (isRecording && !isPaused) {
            try {
                isListeningToAudio = false

                mediaRecorder?.let { recorder ->
                    try {
                        // Only stop if actually recording
                        if (isRecording) {
                            recorder.stop()
                        }
                        recorder.release()
                    } catch (e: IllegalStateException) {
                        Log.e("pauseRecording", "MediaRecorder in invalid state: $e")
                        try {
                            recorder.release()
                        } catch (ex: Exception) {
                            Log.e("pauseRecording", "Failed to release: $ex")
                        }
                    } catch (e: Exception) {
                        Log.e("pauseRecording", "Failed to stop media recorder: $e")
                    }
                }
                mediaRecorder = null

                isPaused = true
                secondTimerTv?.text = timerTv?.text

                // Hide recording UI, show playback UI
                recordingLayout?.visibility = View.GONE
                timerTv?.visibility = View.GONE
                playVNRecorded?.visibility = View.VISIBLE

                playVnAudioBtn?.setImageResource(R.drawable.play_svgrepo_com)
                recordVN?.setImageResource(R.drawable.mic_2)

                stopWaveDotsAnimation()

                waveformScrollView?.visibility = View.VISIBLE
                waveDotsContainer?.visibility = View.VISIBLE
                wave?.visibility = View.GONE

                Log.d("pauseRecording", "List of recordings size: ${recordedAudioFiles.size}")
                mixVoiceNote()

            } catch (e: Exception) {
                Log.e("pauseRecording", "Error in pauseVoiceNoteRecording: ${e.message}", e)
                e.printStackTrace()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun resumeRecording() {
        if (isPaused) {
            startRecordingVoiceNote()

            // Hide playback UI, show recording UI
            playVNRecorded?.visibility = View.GONE  // ✅ Only set once
            timerTv?.visibility = View.VISIBLE

            playVnAudioBtn?.setImageResource(R.drawable.play_svgrepo_com)
            recordVN?.setImageResource(R.drawable.baseline_pause_black)
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private fun stopRecordingVoiceNote() {
        val TAG = "StopRecording"
        try {
            // Stop media recorder - with proper state checking
            mediaRecorder?.let { recorder ->
                try {
                    // Only call stop if recording is actually active
                    if (isRecording && !isPaused) {
                        recorder.stop()
                    }
                    recorder.release()
                } catch (e: IllegalStateException) {
                    Log.e(TAG, "MediaRecorder was in invalid state: ${e.message}")
                    // Still try to release even if stop failed
                    try {
                        recorder.release()
                    } catch (releaseEx: Exception) {
                        Log.e(TAG, "Failed to release recorder: ${releaseEx.message}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error stopping/releasing mediaRecorder: ${e.message}")
                }
            }
            mediaRecorder = null

            // Stop audio listening thread
            isListeningToAudio = false

            isRecording = false
            isPaused = false
            stopWaveDotsAnimation()
            recordingLayout.visibility = View.GONE
            timerTv?.text = "00:00"
            recordVN?.setImageResource(com.uyscuti.social.call.R.drawable.ic_mic_on)
            sendVN?.setBackgroundResource(R.drawable.ic_ripple_disabled)
            sendVN?.isClickable = false
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
                try {
                    Toast.makeText(requireContext(), "Voice note file not found", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.e(TAG, "Fragment context not available: ${e.message}")
                }
                return
            }

            // Get duration and filename
            val durationString = getFormattedDuration(audioFilePath)
            val fileName = file.name

            Log.d(TAG, "Voice note prepared - File: $fileName, Duration: $durationString, Path: $audioFilePath")

            // Add local post for immediate UI feedback
            val localPost = creatingVoiceNoteAsOriginalPost(audioFilePath, fileName, durationString)
            getFeedViewModel.addSingleAllFeedData(localPost)

            // Upload to server
            lifecycleScope.launch {
                startUploadAnimation()

                val result = uploadVoiceNoteToServer(
                    file = file,
                    duration = durationString,
                    thumbnailFile = null,
                    content = "",
                    tags = null
                )

                result.onSuccess { uploadResponse ->
                    Log.d(TAG, "Voice note uploaded successfully")

                    try {
                        val serverPost = convertUploadResponseToPost(uploadResponse)
                        getFeedViewModel.removeSingleAllFeedData(localPost.toString())
                        getFeedViewModel.addSingleAllFeedData(serverPost)
                        Log.d(TAG, "Local post replaced with server post: ${serverPost._id}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to convert response: ${e.message}", e)
                    }

                }.onFailure { error ->
                    Log.e(TAG, "Voice note upload failed: ${error.message}")
                    Toast.makeText(requireContext(), "Upload failed: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in stopRecording: ${e.message}", e)
            try {
                Toast.makeText(requireContext(), "", Toast.LENGTH_SHORT).show()
            } catch (ex: Exception) {
                Log.e(TAG, "Fragment context not available: ${ex.message}")
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun pausedVoiceNote(progress: Int) {
        Log.d("pauseVn", "pauseVoiceNote called - vnRecordProgress $vnRecordProgress, progress $progress")

        val scrollAnimator = waveformScrollView?.tag as? ValueAnimator
        scrollAnimator?.cancel()

        player?.pause()
        player?.seekTo(progress)

        isAudioVNPlaying = false
        isAudioVNPaused = true

        stopPlaybackTimerRunnable()

        // KEEP the existing waveform - DO NOT regenerate
        // The waveform with variable heights is already there from recording
        wave?.visibility = View.GONE
        waveformScrollView?.visibility = View.VISIBLE
        waveDotsContainer?.visibility = View.VISIBLE

        // Scroll to the progress position WITHOUT regenerating bars
        waveformScrollView?.post {
            val maxScroll = (waveDotsContainer?.width ?: 0) - (waveformScrollView?.width ?: 0)
            if (maxScroll > 0) {
                val duration = player?.duration?.toFloat() ?: 1f
                if (duration > 0) {
                    val progressRatio = progress.toFloat() / duration
                    val targetScroll = (maxScroll * progressRatio).toInt().coerceIn(0, maxScroll)
                    waveformScrollView?.scrollTo(targetScroll, 0)
                    Log.d("pausedVoiceNote", "Scrolled to position: $targetScroll / $maxScroll")
                }
            }
        }

        recordingLayout?.visibility = View.GONE
        playVNRecorded?.visibility = View.VISIBLE
        playVnAudioBtn?.setImageResource(R.drawable.play_svgrepo_com)

        val currentMinutes = (progress / 1000) / 60
        val currentSeconds = (progress / 1000) % 60
        secondTimerTv?.text = String.format("%02d:%02d", currentMinutes, currentSeconds)
    }

    @SuppressLint("DefaultLocale")
    private fun stopPlayingWithPause() {
        val scrollAnimator = waveformScrollView?.tag as? ValueAnimator
        scrollAnimator?.cancel()

        val lastDuration = player?.duration ?: 0
        player?.release()
        player = null

        isAudioVNPlaying = false
        isAudioVNPaused = false

        stopPlaybackTimerRunnable()

        // KEEP the existing waveform - DO NOT regenerate
        wave?.visibility = View.GONE
        waveformScrollView?.visibility = View.VISIBLE
        waveDotsContainer?.visibility = View.VISIBLE

        recordingLayout?.visibility = View.GONE
        playVNRecorded?.visibility = View.VISIBLE
        playVnAudioBtn?.setImageResource(R.drawable.play_svgrepo_com)
        playVnAudioBtn?.visibility = View.VISIBLE // Show play button to allow replay

        val totalMinutes = (lastDuration / 1000) / 60
        val totalSeconds = (lastDuration / 1000) % 60
        secondTimerTv?.text = String.format("%02d:%02d", totalMinutes, totalSeconds)

        vnRecordProgress = 0
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        progressAnimator?.cancel()

        // Cancel WorkManager jobs if running
        currentWorkerId?.let { workId ->
            try {
                workManager?.cancelWorkById(workId)
                Log.d(TAG, "Cancelled WorkManager job: $workId")
            } catch (e: Exception) {
                Log.e(TAG, "Error cancelling WorkManager job: ${e.message}")
            }
        }

        // Clean up audio resources
        try {
            stopPlaying()

            // Only release mediaRecorder if initialized (don't call stopRecording as it may already be stopped)
            if (mediaRecorder != null) {
                try {
                    mediaRecorder?.stop()
                    mediaRecorder?.release()
                    mediaRecorder = null
                } catch (e: Exception) {
                    Log.e(TAG, "Error stopping mediaRecorder: ${e.message}")
                }
            }

            stopRecordWaveRunnable()
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up audio resources: ${e.message}")
        }

        // Clean up recorded files
        deleteVoiceNote()
    }

    private fun mixVoiceNote() {
        val TAG = "mixVN"
        try {
            wasPaused = true
            Log.d(TAG, "mixVN: outputFile: $outputVnFile")

            val audioMixer = AudioMixer(outputVnFile)
            for (input in recordedAudioFiles) {
                val ai = GeneralAudioInput(input)
                audioMixer.addDataSource(ai)
            }

            audioMixer.mixingType = AudioMixer.MixingType.SEQUENTIAL
            audioMixer.setProcessingListener(object : AudioMixer.ProcessingListener {
                override fun onProgress(progress: Double) {}

                override fun onEnd() {
                    lifecycleScope.launch(Dispatchers.Main) {
                        audioMixer.release()
                        mixingCompleted = true
                        val file = File(outputVnFile)
                        Log.d(TAG, "onEnd: output vn file exists ${file.exists()}")
                        Log.d(TAG, "onEnd: media muxed success")

                        // DO NOT CALL inflateWave() HERE!
                        // We want to keep the original waveform from recording
                        // The waveform bars are already created and should stay visible

                        Log.d(TAG, "Mixing complete - keeping original waveform visible")
                    }
                }
            })

            try {
                audioMixer.start()
                audioMixer.processAsync()
            } catch (e: IOException) {
                audioMixer.release()
                e.printStackTrace()
                Log.d(TAG, "mixVN: exception 1 $e")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, "mixVN: exception 2 $e")
        }
    }

    private fun startPlaying(vnAudio: String) {
        playVnAudioBtn?.setImageResource(R.drawable.baseline_pause_white_24)
        EventBus.getDefault().post(PauseShort(true))

        isAudioVNPlaying = true

        // KEEP playVNRecorded visible to show the timer during playback
        playVNRecorded?.visibility = View.VISIBLE  // ✅ Changed from GONE to VISIBLE
        recordingLayout?.visibility = View.GONE     // Keep this hidden

        // Hide the play button, but keep the timer (secondTimerTv) visible
        playVnAudioBtn?.visibility = View.GONE      // ✅ Hide the play button during playback

        // Use the amplitude-based waveform animation
        startPlaybackAmplitudeSimulation()

        if (isAudioVNPaused) {
            Log.d("startPlaying", "vnRecordProgress $vnRecordProgress")
            if (vnRecordProgress != 0) {
                player?.seekTo(vnRecordProgress)
            }
            player?.start()
        } else {
            player = MediaPlayer().apply {
                try {
                    setDataSource(vnAudio)
                    prepare()

                    if (vnRecordProgress != 0) {
                        seekTo(vnRecordProgress)
                    }

                    start()
                    setOnCompletionListener {
                        isAudioVNPaused = false
                        stopPlayingWithPause()
                    }
                } catch (e: IOException) {
                    Log.e("MediaRecorder", "prepare() failed: $e")
                }
            }
        }

        updatePlaybackTimer()
        animatePlaybackScroll()
    }

    private fun updatePlaybackTimer() {
        waveHandler.post(object : Runnable {
            @SuppressLint("DefaultLocale")
            override fun run() {
                if (isAudioVNPlaying && player != null) {
                    val currentPosition = player?.currentPosition ?: 0
                    val currentMinutes = (currentPosition / 1000) / 60
                    val currentSeconds = (currentPosition / 1000) % 60
                    // Update secondTimerTv instead of timerTv during playback
                    secondTimerTv?.text = String.format("%02d:%02d", currentMinutes, currentSeconds)
                    waveHandler.postDelayed(this, 100)
                }
            }
        })
    }

    private fun deleteVoiceNoteRecording() {
        try {
            isListeningToAudio = false
            mediaRecorder?.let { recorder ->
                try {
                    recorder.stop()
                    recorder.release()
                } catch (e: Exception) {
                    Log.e(TAG, "Error stopping mediaRecorder: ${e.message}")
                }
            }
            mediaRecorder = null
        } catch (e: Exception) {
            Log.e(TAG, "Error in deleteRecording: ${e.message}", e)
        }

        isRecording = false
        isPaused = false
        isAudioVNPlaying = false

        stopWaveDotsAnimation()
        recordVN?.setImageResource(R.drawable.mic_2)
        sendVN?.setBackgroundResource(R.drawable.ic_ripple_disabled)
        sendVN?.isClickable = false

        deleteVoiceNote()
    }

    private suspend fun uploadVoiceNoteToServer(
        file: File,
        thumbnailFile: File?,
        content: String,
        duration: String,
        tags: Array<String>? = null
    ): Result<FeedUploadResponse> = withContext(Dispatchers.IO) {
        try {
            // Check if retrofitInterface is initialized (FIXED: removed duplicate)
            if (!::retrofitInterface.isInitialized) {
                return@withContext Result.failure(Exception("Network interface not initialized"))
            }

            // Basic fields
            val contentRequestBody = content.toRequestBody("text/plain".toMediaType())
            val contentTypeRequestBody = "vn".toRequestBody("text/plain".toMediaType())

            // Empty feedShortsBusinessId (required by API)
            val feedShortsBusinessIdRequestBody = "".toRequestBody("text/plain".toMediaType())

            // Generate unique fileId from filename (without extension)
            val fileId = file.nameWithoutExtension

            // Duration JSON object with fileId (as Array)
            val durationJson = JSONObject().apply {
                put("fileId", fileId)
                put("duration", duration)
            }
            val durationRequestBody = arrayOf(durationJson.toString().toRequestBody("text/plain".toMediaType()))

            // File type JSON object with fileId (as Array)
            val fileTypeJson = JSONObject().apply {
                put("fileId", fileId)
                put("fileType", "audio/mpeg")
            }
            val fileTypeRequestBody = arrayOf(fileTypeJson.toString().toRequestBody("text/plain".toMediaType()))

            // Number of pages JSON object with fileId (as Array) - for audio, use 0
            val numberOfPagesJson = JSONObject().apply {
                put("fileId", fileId)
                put("numberOfPages", 0)
            }
            val numberOfPagesRequestBody = arrayOf(numberOfPagesJson.toString().toRequestBody("text/plain".toMediaType()))

            // File name JSON object with fileId (as Array)
            val fileNameJson = JSONObject().apply {
                put("fileId", fileId)
                put("fileName", file.name)
            }
            val fileNameRequestBody = arrayOf(fileNameJson.toString().toRequestBody("text/plain".toMediaType()))

            // File size JSON object with fileId (as Array)
            val fileSizeJson = JSONObject().apply {
                put("fileId", fileId)
                put("fileSize", file.length())
            }
            val fileSizeRequestBody = arrayOf(fileSizeJson.toString().toRequestBody("text/plain".toMediaType()))

            // FileIds as single string
            val fileIdsRequestBody = fileId.toRequestBody("text/plain".toMediaType())

            // Create file multipart with fileId as filename (as List)
            val fileMultipartList = listOf(
                MultipartBody.Part.createFormData(
                    "files",
                    "$fileId.mp3",
                    file.asRequestBody("audio/mpeg".toMediaType())
                )
            )

            // Create thumbnail multipart list (empty if no thumbnail)
            val thumbnailMultipartList = if (thumbnailFile != null) {
                listOf(
                    MultipartBody.Part.createFormData(
                        "thumbnail",
                        "$fileId.jpg",
                        thumbnailFile.asRequestBody("image/*".toMediaType())
                    )
                )
            } else {
                emptyList()
            }

            // Tags request body
            val tagsRequestBody = tags?.map { tag ->
                tag.toRequestBody("text/plain".toMediaType())
            }?.toTypedArray()

            // Make API call with all required fields
            val response = retrofitInterface.apiService.uploadMixedFilesFeed(
                content = contentRequestBody,
                contentType = contentTypeRequestBody,
                feedShortsBusinessId = feedShortsBusinessIdRequestBody,
                duration = durationRequestBody,
                fileTypes = fileTypeRequestBody,
                numberOfPages = numberOfPagesRequestBody,
                fileNames = fileNameRequestBody,
                fileSizes = fileSizeRequestBody,
                fileIds = fileIdsRequestBody,
                files = fileMultipartList,
                thumbnail = thumbnailMultipartList,
                tags = tagsRequestBody
            )

            return@withContext if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Voice Note Upload failed: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun onTimerTick(duration: String) {
        timerTv?.text = duration
    }

    private fun deleteVoiceNote() {
        recordedAudioFiles.clear()
        val isDeleted = deleteFiles(recordedAudioFiles)
        val outputVnFileList = mutableListOf<String>().apply { add(outputVnFile) }
        val deleteMixVn = deleteFiles(outputVnFileList)

        if (isDeleted) {
            Log.d(TAG, "File record deleted successfully")
        } else {
            Log.e(TAG, "Failed to delete recorded files")
        }

        if (deleteMixVn) {
            Log.d(TAG, "File mix vn deleted successfully")
        } else {
            Log.e(TAG, "Failed to delete mixed vn file")
        }
    }

    private fun startTimer() {
        var seconds = 0
        var minutes = 0

        waveHandler.post(object : Runnable {
            @SuppressLint("DefaultLocale")
            override fun run() {
                if (isRecording && !isPaused) {
                    seconds++
                    if (seconds == 60) {
                        minutes++
                        seconds = 0
                    }
                    timerTv?.text = String.format("%02d:%02d", minutes, seconds)
                    waveHandler.postDelayed(this, 1000)
                }
            }
        })
    }

    private fun listenToAudio() {
        try {
            val minBufferSize = AudioRecord.getMinBufferSize(
                44100,
                android.media.AudioFormat.CHANNEL_IN_MONO,
                android.media.AudioFormat.ENCODING_PCM_16BIT
            )

            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
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
                    var sum = 0.0
                    for (i in 0 until readSize) {
                        sum += abs(buffer[i].toDouble())
                    }

                    val average = sum / readSize
                    val amplitude = (average / 32768.0).toFloat()
                    val soundThreshold = 0.012f // Lower threshold for better detection

                    // Add bars based on sound detection
                    if (amplitude > soundThreshold) {
                        // Sound detected - calculate proportional height
                        // Map amplitude to height multiplier: 0.4 to 2.5 (not too high)
                        val normalizedAmplitude = ((amplitude - soundThreshold) / (0.5f - soundThreshold))
                            .coerceIn(0f, 1f)
                        val heightMultiplier = 0.4f + (normalizedAmplitude * 2.1f) // Range: 0.4 to 2.5

                        waveHandler.post {
                            addNewWaveBarAtEnd(heightMultiplier)
                            scrollWaveformToRight()
                        }
                    } else {
                        // Silence - add small rounded dot
                        waveHandler.post {
                            addIdleDottedBarAtEnd()
                            scrollWaveformToRight()
                        }
                    }
                }

                Thread.sleep(50) // Update frequency
            }

            audioRecord?.release()
            audioRecord = null
        } catch (e: Exception) {
            Log.e("ListenToAudio", "Error in audio listening thread: ${e.message}")
        }
    }

    private fun stopRecordWaveRunnable() {
        try {
            isListeningToAudio = false
            waveHandler.removeCallbacksAndMessages(null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopPlaying() {
        val scrollAnimator = waveformScrollView?.tag as? ValueAnimator
        scrollAnimator?.cancel()

        playVnAudioBtn?.setImageResource(R.drawable.play_svgrepo_com)
        player?.release()
        player = null

        isAudioVNPlaying = false
        stopWaveDotsAnimation()

        recordingLayout?.visibility = View.GONE
        vnRecordProgress = 0
    }

    private fun stopWaveDotsAnimation() {
        waveBars.forEach { bar ->
            val animator = bar.tag as? ObjectAnimator
            animator?.cancel()
        }
    }

    private fun animatePlaybackScroll() {
        val scrollDuration = player?.duration?.toLong() ?: 0L

        if (scrollDuration > 0) {
            waveformScrollView?.post {
                val maxScroll = waveDotsContainer?.width?.minus(waveformScrollView?.width ?: 0) ?: 0

                if (maxScroll > 0) {
                    val scrollAnimator = ValueAnimator.ofInt(0, maxScroll).apply {
                        duration = scrollDuration
                        interpolator = LinearInterpolator()
                        addUpdateListener { animation ->
                            val scrollX = animation.animatedValue as Int
                            waveformScrollView?.scrollTo(scrollX, 0)
                        }
                    }

                    scrollAnimator.start()
                    waveformScrollView?.tag = scrollAnimator
                }
            }
        }
    }

    private fun stopPlaybackTimerRunnable() {
        waveHandler.removeCallbacksAndMessages(null)
    }

    private fun calculateBarsNeededForFullWidth(): Int {
        val screenWidth = resources.displayMetrics.widthPixels
        val barWidth = dpToPx(4)
        val barMargin = dpToPx(6)
        val totalBarWidth = barWidth + barMargin
        return (screenWidth / totalBarWidth) + 5
    }

    private fun addNewWaveBarAtEnd(heightMultiplier: Float) {
        val bar = View(requireContext()).apply {
            val baseHeightDp = 40 // Base height in dp
            val actualHeight = (baseHeightDp * heightMultiplier).toInt().coerceIn(16, 100)

            layoutParams = LinearLayout.LayoutParams(
                dpToPx(4),
                dpToPx(actualHeight)
            ).apply {
                marginEnd = dpToPx(4)
                gravity = android.view.Gravity.CENTER_VERTICAL
            }


            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(Color.parseColor("#2563EB"))
                cornerRadius = dpToPx(2).toFloat()
            }

            scaleY = 1.0f // No additional scaling needed
            alpha = 1.0f  // Full opacity for sound bars
            tag = "sound_bar"
        }

        // Add to END of container (right side)
        waveDotsContainer?.addView(bar)
        waveBars.add(bar)

        // Remove old bars from the START (left side) if too many
        if (waveBars.size > 200) {
            waveDotsContainer?.removeViewAt(0)
            waveBars.removeAt(0)
        }
    }

    private fun addIdleDottedBarAtEnd() {
        val bar = View(requireContext()).apply {
            val dotSize = dpToPx(5)
            layoutParams = LinearLayout.LayoutParams(
                dotSize,
                dotSize
            ).apply {
                marginEnd = dpToPx(3) // 4dp spacing
                gravity = android.view.Gravity.CENTER_VERTICAL
            }

            // Create circular dot with same blue color as bars
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#2563EB"))
            }

            scaleY = 1.0f

            tag = "idle_dot"
        }

        // Add to END of container (right side)
        waveDotsContainer?.addView(bar)
        waveBars.add(bar)

        // Remove old bars from the START (left side) if too many
        if (waveBars.size > 200) {
            waveDotsContainer?.removeViewAt(0)
            waveBars.removeAt(0)
        }
    }

    private fun scrollWaveformToRight() {
        waveformScrollView?.post {
            val maxScroll = (waveDotsContainer?.width ?: 0) - (waveformScrollView?.width ?: 0)
            if (maxScroll > 0) {
                waveformScrollView?.smoothScrollTo(maxScroll, 0)
            }
        }
    }

    private fun initializeDottedWaveform() {
        waveDotsContainer?.removeAllViews()
        waveBars.clear()

        val barsToFill = calculateBarsNeededForFullWidth()
        repeat(barsToFill) {
            addIdleDottedBarAtEnd()
        }

        waveformScrollView?.post {
            val maxScroll = (waveDotsContainer?.width ?: 0) - (waveformScrollView?.width ?: 0)
            if (maxScroll > 0) {
                waveformScrollView?.scrollTo(maxScroll, 0)
            }
        }
    }

    private fun startPlaybackAmplitudeSimulation() {
        Thread {
            while (isAudioVNPlaying && player != null) {
                // Simulate varying amplitudes (you can also read from actual audio file if needed)
                val amplitude = 0.3f + (Math.random().toFloat() * 0.7f)
                val heightMultiplier = 0.4f + (amplitude * 2.1f) // Same calculation as recording

                waveHandler.post {
                    // Remove leftmost bar
                    if (waveBars.isNotEmpty()) {
                        waveDotsContainer?.removeViewAt(0)
                        waveBars.removeAt(0)
                    }

                    // Add new bar at the end
                    addNewWaveBarAtEnd(heightMultiplier)
                    scrollWaveformToRight()
                }

                Thread.sleep(50) // Same update frequency as recording
            }
        }.start()
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }


}