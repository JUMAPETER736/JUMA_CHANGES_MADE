package com.uyscuti.social.circuit.ui.fragments.chat

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.AnimationDrawable
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
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
import com.uyscuti.sharedmodule.viewmodels.feed.GetFeedViewModel
import com.uyscuti.social.circuit.adapter.FragmentPageAdapter
import com.uyscuti.social.core.common.data.room.entity.FollowUnFollowEntity
import com.uyscuti.social.network.api.response.allFeedRepostsPost.Account
import com.uyscuti.social.network.api.response.allFeedRepostsPost.Author
import com.uyscuti.social.network.api.response.allFeedRepostsPost.Avatar
import com.uyscuti.social.network.api.response.allFeedRepostsPost.CoverImage
import com.uyscuti.social.network.api.response.getfeedandresposts.Duration
import com.uyscuti.social.network.api.response.getfeedandresposts.File
import com.uyscuti.social.network.api.response.getfeedandresposts.FileName
import com.uyscuti.social.network.api.response.getfeedandresposts.FileType
import com.uyscuti.social.network.api.response.getfeedandresposts.NumberOfPage
import com.uyscuti.social.network.api.response.getfeedandresposts.Thumbnail
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


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val TAG = "FeedFragment"


private const val REQUEST_UPLOAD_FEED_ACTIVITY = 1010

class FeedFragment() : Fragment(), Timer.OnTimeTickListener {


    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
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


    private var clicked = false

    private var isPaused = false
    private var isRecording = false
    private lateinit var outputFile: String
    private var mediaRecorder: MediaRecorder? = null
    private lateinit var amplitudes: ArrayList<Float>
    private var player: MediaPlayer? = null
    private val recordedAudioFiles = mutableListOf<String>()
    private var outputVnFile: String = ""
    private var isAudioVNPlaying = false
    private var isAudioVNPaused = false

    private var isVnResuming = false

    private lateinit var timer: Timer
    private var amps = 0

    var vnRecordAudioPlaying = false
    var vnRecordProgress = 0
    var isOnRecordDurationOnPause = false
    private var wasPaused = false
    private var deleteVN: ImageView? = null
    private var recordVN: ImageView? = null
    private lateinit var playVnAudioBtn: ImageView
    private var sendVN: ImageView? = null

    private var timerTv: TextView? = null
    private var playerTimerTv: LinearLayout? = null
    private var secondTimerTv: TextView? = null
    private var thirdTimerTv: TextView? = null

    private var waveForm: WaveFormView? = null
    private var wave: WaveformSeekBar? = null
    private var playAudioLayout: LinearLayout? = null

    private var wifiAnimation: AnimationDrawable? = null

    //    private lateinit var feedUploadImageView: ImageView
    private lateinit var feedUploadView: ImageView
    private lateinit var feedCancelView: ImageView
//    private lateinit var uploadProgressSeekBar: ProgressBar

    //    private lateinit var feedUploadViewModel: FeedUploadViewModel
    private lateinit var settings: SharedPreferences
    private val PREFS_NAME = "LocalSettings"

    private var feedPostPositionFromShorts = -1

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

        if (requestCode == REQUEST_UPLOAD_FEED_ACTIVITY) {
            if (resultCode == Activity.RESULT_OK) {


                val mixedFilesData = data?.getStringExtra("mixedFiles")
                val caption = data?.getStringExtra("caption")
                val tags = data?.getStringExtra("tags")
                val contentType = data?.getStringExtra("contentType")
                val dataList =
                    mixedFilesData?.let { deserializeFeedUploadDataList(it) } ?: mutableListOf()
                Log.d("onActivityResult", "onActivityResult: mixed files data $mixedFilesData")
                Log.d("onActivityResult", "onActivityResult: caption $caption")
                Log.d("onActivityResult", "onActivityResult: tags $tags")

                val filesList = ArrayList<File>()

                val fileTypes: MutableList<FileType> =
                    mutableListOf()
                val duration: MutableList<Duration> =
                    mutableListOf()
                val fileIds: MutableList<String> = mutableListOf()
                val thumbnail: MutableList<Thumbnail> = mutableListOf()
                val numberOfPages: MutableList<NumberOfPage> = mutableListOf()
                val fileName: MutableList<FileName> = mutableListOf()

                val fileThumbnail: Bitmap? = null

                val profilePic2 = settings.getString("profile_pic", "").toString()
                val userId = settings.getString("_id", "").toString()
                val avatar = Avatar(
                    _id = "",
                    localPath = "",
                    url = "",
                    type = ""
                )
                val account =

                    Account(
                        _id = "",
                        avatar = avatar,
                        createdAt = "",
                        email = "",
                        updatedAt = "",
                        LocalStorage.Companion.getInstance(requireContext()).getUsername()
                    )
                val author = Author(
                    _id = userId,
                    account = account,
                    firstName = "",
                    lastName = "",
                    bio = "",
                    countryCode = "",
                    createdAt = "",
                    __v = 0,
                    dob = "",
                    owner = "",
                    location = "",
                    updatedAt = "",
                    phoneNumber = "",
                    avatar = null,
                    coverImage = CoverImage(
                        _id = "",
                        localPath = "",
                        url = profilePic2
                    ),
                    username = "unknown"
                )
                val mongoDbTimeStamp = generateMongoDBTimestamp()

                dataList.forEach {

                    fileIds.add(it.fileId)
                    fileTypes.add(
                        FileType(
                            it.fileId,
                            it.fileTypes
                        )
                    )


                    Log.d("onActivityResult", "onActivityResult: $it")

                    if (it.documents != null) {
                        filesList.add(
                            File(
                                _id = "",
                                fileId = it.fileId,
                                url = it.documents!!.pdfFilePath,
                                localPath = it.documents!!.uri.toString()
                            )
                        )
                        numberOfPages.add(
                            NumberOfPage(
//                                fileId = it.fileId,
//                                numberOfPages = it.documents!!.numberOfPages
                            )
                        )

                        fileName.add(
                            FileName(
                                fileId = it.fileId,
                                fileName = it.documents!!.filename
                            )
                        )
                        thumbnail.add(
                            Thumbnail(
                                _id = "",
                                thumbnailUrl = it.documents!!.pdfFilePath,
                                thumbnailLocalPath = it.documents!!.pdfFilePath,
                                fileId = it.fileId,
//                                fileThumbnail = it.documents!!.documentThumbnailFilePath
                            )
                        )

                    } else if (it.images != null) {
                        filesList.add(
                            File(
                                _id = "",
                                fileId = it.fileId,
                                url = it.images!!.imagePath,
                                localPath = it.images!!.compressedImagePath
                            )
                        )
                    } else if (it.videos != null) {
                        filesList.add(
                            File(
                                _id = "",
                                fileId = it.fileId,
                                url = it.videos!!.videoPath,
                                localPath = it.videos!!.videoUri
                            )
                        )

                        thumbnail.add(
                            Thumbnail(
                                _id = "",
                                thumbnailUrl = it.videos!!.videoPath,
                                thumbnailLocalPath = it.videos!!.videoUri,
                                fileId = it.fileId
                            )
                        )
                        duration.add(
                            Duration(
                                it.fileId, it.videos!!.videoDuration
                            )
                        )

                    } else if (it.audios != null) {
                        filesList.add(
                            File(
                                _id = "",
                                fileId = it.fileId,
                                url = it.audios!!.audioPath,
                                localPath = it.audios!!.audioPath


                            )
                        )
                        duration.add(
                            Duration(
                                it.fileId, it.audios!!.duration
                            )
                        )
                    }
                }

//                val post = com.uyscuti.social.network.api.response.posts.Post(
//                    __v = 1,
//                    _id = "",
//                    author = author,
//                    comments = 0,
//                    content = caption ?: "",
//                    contentType = contentType ?: "",
//                    createdAt = mongoDbTimeStamp,
//                    duration = listOf(),
//                    feedShortsBusinessId = "",
//                    fileIds = mutableListOf(),
//                    fileNames = mutableListOf(),
//                    fileSizes = mutableListOf(),
//                    fileTypes = mutableListOf(),
//                    files = mutableListOf(),
//                    isFollowing = false,
//                    isBookmarked = false,
//                    isReposted = false,
//                    likes = 0,
//                    numberOfPages = mutableListOf(),
//                    originalPost = mutableListOf(),
//                    repostedByUserId = "",
//                    repostedUsers = mutableListOf(),
//                    tags = mutableListOf(),
//                    thumbnail = mutableListOf(),
//                    updatedAt = mongoDbTimeStamp,
//                    isExpanded = false,
//                    isLocal = false,
//                    isLiked = false,
//                    bookmarkCount = 0,
//                    shareCount = 0,
//                    commentCount = 0,
//                    repostCount = 0,
//                    isShared = false,
//                    url = "",
//                    type = "",
//
//                    repostedUser = RepostedUser(
//                        _id = "",
//                        avatar = Avatar(
//                            _id = " ",
//                            localPath = " ",
//                            url = " ",
//                            type = ""
//                        ),
//                        createdAt = "",
//                        email = "",
//                        updatedAt = "",
//                        username = "",
//                        url = "",
//                        type = ""
//                    ),
//                    )


              //  Log.d("PostObject", "Post Object: $post")

               // getFeedViewModel.addSingleAllFeedData(post)

            } else {
                // Handle other result codes if needed
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun stopRecording() {

//        val TAG = "StopRecording"
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
            timerTv!!.text = "00:00.00"

            recordVN!!.setImageResource(R.drawable.ic_mic_on)

            sendVN!!.setBackgroundResource(R.drawable.ic_ripple_disabled)
            sendVN!!.isClickable = false

            amplitudes = waveForm!!.clear()
            amps = 0
            timer.stop()
            if (player?.isPlaying == true) {
                stopPlaying()
            }
//           VNLayout.visibility = View.GONE

            val file = java.io.File(outputVnFile)
            val file2 = java.io.File(outputFile)
            Log.d(TAG, "stopRecording: recorded files size ${recordedAudioFiles.size}")

            var fileName = ""
            var durationString = ""

            if (recordedAudioFiles.size != 1) {
                durationString = AudioDurationHelper.getFormattedDuration(outputVnFile)
                fileName = getFileNameFromLocalPath(outputVnFile)
                Log.d("AudioPicker", "File path: $outputVnFile")
                Log.d("AudioPicker", "File name: $fileName")
                Log.d("AudioPicker", "durationString: $durationString")
                val intent = Intent(requireActivity(), UploadFeedActivity::class.java)
                intent.putExtra("vnFilePath", outputVnFile)
                intent.putExtra("vnFileName", fileName)
                intent.putExtra("vnDurationString", durationString)
                startActivityForResult(intent, REQUEST_UPLOAD_FEED_ACTIVITY)
            } else {
                durationString = AudioDurationHelper.getFormattedDuration(outputFile)
                fileName = getFileNameFromLocalPath(outputFile)
                Log.d("AudioPicker", "File path: $outputFile")
                Log.d("AudioPicker", "File name: $fileName")
                Log.d("AudioPicker", "durationString: $durationString")
                val intent = Intent(requireActivity(), UploadFeedActivity::class.java)
                intent.putExtra("vnFilePath", outputFile)
                intent.putExtra("vnDurationString", durationString)
                intent.putExtra("vnFileName", fileName)
                startActivityForResult(intent, REQUEST_UPLOAD_FEED_ACTIVITY)
//                requireActivity().startActivity(intent)
            }
            recordedAudioFiles.clear()

        } catch (e: Exception) {
            Log.d(TAG, "stopRecording: $e")
            e.printStackTrace()
            // Handle exceptions as needed
        }
    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun onUploadVNButtonClick() {
        showVNDialog()
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
//            isAudioVNPlaying = false

            timerTv!!.text = "00:00.00"
//            binding.recordVN.setImageResource(R.drawable.baseline_pause_24)
            recordVN!!.setImageResource(R.drawable.ic_mic_on)

//            binding.deleteVN.setBackgroundResource(R.drawable.ic_ripple_disabled)
//            binding.deleteVN.isClickable = false
            sendVN!!.setBackgroundResource(R.drawable.ic_ripple_disabled)
            sendVN!!.isClickable = false

            amplitudes = waveForm!!.clear()
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

    private val waveHandler = Handler(Looper.getMainLooper())

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

    private fun stopPlaying() {
        playVnAudioBtn.setImageResource(R.drawable.play_svgrepo_com)
        player?.release()
        player = null
        isAudioVNPlaying = false

        stopRecordWaveRunnable()
        wave!!.progress = 0F
//        vnRecordProgress = 0
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun resumeRecording() {
        if (isPaused) {
            isVnResuming = true
            startRecording() // Start a new recording session, appending to the previous file
            waveForm!!.visibility = View.VISIBLE
            timerTv!!.visibility = View.VISIBLE
            playAudioLayout!!.visibility = View.GONE
            playVnAudioBtn.setImageResource(R.drawable.play_svgrepo_com)
            recordVN!!.setImageResource(R.drawable.baseline_pause_black)
        }

    }

    override fun onTimerTick(duration: String) {
        timerTv!!.text = duration

        var amplitude = mediaRecorder!!.maxAmplitude.toFloat()
        amplitude = if (amplitude > 0) amplitude else 130f
//        Log.d("onTimerTick", "onTimerTick: media recorder amplitude: ${mediaRecorder?.maxAmplitude}")
        waveForm!!.addAmplitude(amplitude)

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
            timerTv!!.visibility = View.INVISIBLE
            waveForm!!.visibility = View.GONE
            playAudioLayout!!.visibility = View.VISIBLE
            playVnAudioBtn.setImageResource(R.drawable.play_svgrepo_com)
            recordVN!!.setImageResource(R.drawable.ic_mic_on)


            Log.d(TAG, "pauseRecording: list of recordings  size: ${recordedAudioFiles.size}")
            Log.d(TAG, "pauseRecording: list of recordings $recordedAudioFiles")

            mixVN()
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
                    requireActivity().runOnUiThread {
                        audioMixer.release()
//                        mixingCompleted = true // Set the flag to indicate mixing is completed
                        // Additional code as needed
                        val file = java.io.File(outputVnFile)
                        Log.d(TAG, "onEnd: output vn file exists ${file.exists()}")
                        Log.d(TAG, "onEnd: media muxed success")

                        inflateWave(outputVnFile)

//                        if(stopRecording) {
//                            stopRecording()
//                        }
                        playVnAudioBtn.setOnClickListener {
                            Log.d("playVnAudioBtn", "onEnd: play vn button clicked")
                            when {
                                !isAudioVNPlaying -> {
                                    playVnAudioBtn.setImageResource(R.drawable.baseline_pause_black)
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
                                    playVnAudioBtn.setImageResource(R.drawable.play_svgrepo_com)
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

    private fun pauseVn(progress: Int) {
        Log.d("pauseVn", "vnRecordProgress $vnRecordProgress..... progress $progress")

        player?.pause()
        player?.seekTo(progress)
        isAudioVNPlaying = false
        isAudioVNPaused = true
        isOnRecordDurationOnPause = true

        playVnAudioBtn.setImageResource(R.drawable.play_svgrepo_com)
    }

    private fun startPlaying(vnAudio: String) {
        playVnAudioBtn.setImageResource(R.drawable.baseline_pause_white_24)
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

    @SuppressLint("DefaultLocale")
    private fun inflateWave(outputVN: String) {

//        outputVnFile = outputVN

        val TAG = "inflateWave"
        Log.d("playVnAudioBtn", "inflateWave: outputvn $outputVN")

        val audioFile = java.io.File(outputVN)
        wave!!.visibility = View.VISIBLE
        playerTimerTv!!.visibility = View.VISIBLE
        Log.d(TAG, "render: does not start with http")
        //                audioDuration = 100L
        val file = java.io.File(outputVN)
        Log.d(TAG, "render: file $outputVN exists: ${file.exists()}")
        val locaAudioDuration = AudioDurationHelper.getLocalAudioDuration(outputVN)
        if (locaAudioDuration != null) {
            // Duration is available, do something with it
            //                    println("Audio duration: ${duration}ms")
            val minutes = (locaAudioDuration / 1000) / 60
            val seconds = (locaAudioDuration / 1000) % 60
            //                println("Audio duration: $minutes minutes $seconds seconds")
            thirdTimerTv!!.text = String.format("%02d:%02d", minutes, seconds)
        } else {
            // File does not exist or error retrieving duration

            Log.e(TAG, "render: failed to retrieve audio duration")

        }

        CoroutineScope(Dispatchers.IO).launch {
            WaveFormExtractor.getSampleFrom(requireContext(), outputVN) {

                CoroutineScope(Dispatchers.Main).launch {

                    if (locaAudioDuration != null) {
                        wave!!.maxProgress = locaAudioDuration.toFloat()
                    }
                    wave!!.setSampleFrom(it)

                    wave!!.onProgressChanged = object : SeekBarOnProgressChanged {
                        override fun onProgressChanged(
                            waveformSeekBar: WaveformSeekBar,
                            progress: Float,
                            fromUser: Boolean
                        ) {
                            secondTimerTv!!.text = String.format(
                                "%s",
                                TrimVideoUtils.stringForTime(progress)
                            )
                            vnRecordProgress = progress.toInt()
                            if (fromUser) {
                                if (vnRecordAudioPlaying) {
                                    vnRecordProgress = progress.toInt()
                                    pauseVn(progress = progress.toInt())
                                } else {
                                    vnRecordProgress = progress.toInt()
                                    Log.d("FromUser", "Scroll to this $progress")
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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun startRecording() {
        if (!permissionGranted) {
            ActivityCompat.requestPermissions(requireActivity(), permissions, REQUEST_CODE)
            return
        }
        try {

            Log.d(TAG, "recorded files size ${recordedAudioFiles.size}")
            if (player?.isPlaying == true) {
                stopPlaying()
            }
            playerTimerTv!!.visibility = View.GONE
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
//            isVnResuming = false
            recordVN!!.setImageResource(R.drawable.baseline_pause_white_24)
            sendVN!!.setBackgroundResource(R.drawable.ic_ripple)
            deleteVN!!.setBackgroundResource(R.drawable.ic_ripple)
            timer.start()

            deleteVN!!.isClickable = true
            sendVN!!.isClickable = true
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


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun showVNDialog() {
        val dialog = BottomSheetDialog(requireContext())

        dialog.setContentView(R.layout.vn_record_layout)
        deleteVN = dialog.findViewById(R.id.deleteVN)!!
        recordVN = dialog.findViewById<ImageView>(R.id.recordVN)!!
        playVnAudioBtn = dialog.findViewById<ImageView>(R.id.playVnAudioBtn)!!
        sendVN = dialog.findViewById<ImageView>(R.id.sendVN)!!

        timerTv = dialog.findViewById<TextView>(R.id.timerTv)!!
        playerTimerTv = dialog.findViewById(R.id.playerTimerTv)!!
        secondTimerTv = dialog.findViewById<TextView>(R.id.secondTimerTv)!!
        thirdTimerTv = dialog.findViewById<TextView>(R.id.thirdTimerTv)!!

        waveForm = dialog.findViewById<WaveFormView>(R.id.waveForm)!!
        wave = dialog.findViewById<WaveformSeekBar>(R.id.wave)!!

        playAudioLayout = dialog.findViewById<LinearLayout>(R.id.playAudioLayout)!!

        val dialogView = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        dialogView?.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up))

        val selectableItemBackground = TypedValue()
        deleteVN!!.context?.theme?.resolveAttribute(
            android.R.attr.selectableItemBackground, selectableItemBackground, true
        )
        recordVN!!.context?.theme?.resolveAttribute(
            android.R.attr.selectableItemBackground, selectableItemBackground, true
        )
        playVnAudioBtn.context?.theme?.resolveAttribute(
            android.R.attr.selectableItemBackground, selectableItemBackground, true
        )

        sendVN!!.context?.theme?.resolveAttribute(
            android.R.attr.selectableItemBackground, selectableItemBackground, true
        )
        timerTv!!.context?.theme?.resolveAttribute(
            android.R.attr.selectableItemBackground, selectableItemBackground, true
        )
        playerTimerTv!!.context?.theme?.resolveAttribute(
            android.R.attr.selectableItemBackground, selectableItemBackground, true
        )
        secondTimerTv!!.context?.theme?.resolveAttribute(
            android.R.attr.selectableItemBackground, selectableItemBackground, true
        )
        thirdTimerTv!!.context?.theme?.resolveAttribute(
            android.R.attr.selectableItemBackground, selectableItemBackground, true
        )
        waveForm!!.context?.theme?.resolveAttribute(
            android.R.attr.selectableItemBackground, selectableItemBackground, true
        )
        wave!!.context?.theme?.resolveAttribute(
            android.R.attr.selectableItemBackground, selectableItemBackground, true
        )
        playAudioLayout!!.context?.theme?.resolveAttribute(
            android.R.attr.selectableItemBackground, selectableItemBackground, true
        )
        startRecording()
        deleteVN!!.setOnClickListener {
            if (mediaRecorder != null) {
                Log.d(TAG, "onCreate: media recorder not null")
            } else {
                Log.d(TAG, "onCreate: media recorder null")
            }
            deleteRecording()
            if (player?.isPlaying == true) {
                stopPlaying()
            }
            dialog.dismiss()
        }
        recordVN!!.setOnClickListener {
            when {
                isPaused -> resumeRecording()
                isRecording -> pauseRecording()
                else -> Log.d("recordVN", "onCreate: else in vn record btn on click")
                //                else->startRecording()
            }
        }

        sendVN!!.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                Log.d(com.uyscuti.social.circuit.ui.fragments.TAG, "sendVN: recorded files size ${recordedAudioFiles.size}")
                Log.d(com.uyscuti.social.circuit.ui.fragments.TAG, "sendVN: wasPaused $wasPaused")

                if (!wasPaused) {

                    timer.stop()
                    mediaRecorder?.apply {
                        stop()
                        release()
                    }
                    mediaRecorder = null
                    Log.d("SendVN", "When sending vn was paused was false")
                    // Execute mixVN asynchronously
                    mixVN()
                }
                lifecycleScope.launch(Dispatchers.Main) {

                    delay(500)
                    stopRecording()
                }

                // Stop recording after mixVN finishes executing or immediately if wasPaused is true
            }
            dialog.dismiss()
        }

        dialog.show()
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
}