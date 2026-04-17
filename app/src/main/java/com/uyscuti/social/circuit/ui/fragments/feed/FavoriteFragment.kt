package com.uyscuti.social.circuit.ui.fragments.feed


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.ClipboardManager
import android.content.ContentUris
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
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
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.uyscuti.social.circuit.adapter.feed.ShareFeedPostAdapter
import com.uyscuti.social.circuit.ui.feedactivities.FeedVideoViewFragment
import com.uyscuti.social.circuit.ui.fragments.feed.feedRepostViewFragments.FeedRepostDocFragment
import com.uyscuti.social.circuit.ui.fragments.feed.feedRepostViewFragments.FeedRepostImageFragment
import com.uyscuti.social.circuit.ui.fragments.feed.feedRepostViewFragments.FeedRepostTextFragment
import com.uyscuti.social.circuit.ui.fragments.feed.feedRepostViewFragments.FeedRepostVideoViewFragment
import com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.FeedAudioViewFragment
import com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.FeedMultipleImageViewFragment
import com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.FeedTextViewFragment
import com.uyscuti.sharedmodule.FlashApplication
import com.uyscuti.sharedmodule.ReportNotificationActivity2
import com.uyscuti.sharedmodule.adapter.CommentsRecyclerViewAdapter
import com.uyscuti.sharedmodule.adapter.OnViewRepliesClickListener
import com.uyscuti.sharedmodule.adapter.feed.FeedAdapter
import com.uyscuti.sharedmodule.adapter.feed.OnFeedClickListener
import com.uyscuti.sharedmodule.adapter.notifications.AdPaginatedAdapter
import com.uyscuti.social.network.api.models.Comment
import com.uyscuti.social.core.models.data.Dialog
import com.uyscuti.social.core.models.data.Message
import com.uyscuti.social.core.models.data.User
import com.uyscuti.sharedmodule.eventbus.FeedFavoriteClick
import com.uyscuti.sharedmodule.eventbus.FeedFavoriteFollowUpdate
import com.uyscuti.sharedmodule.eventbus.FeedLikeClick
import com.uyscuti.sharedmodule.eventbus.FromFavoriteFragmentFeedFavoriteClick
import com.uyscuti.sharedmodule.eventbus.FromFavoriteFragmentFeedLikeClick
import com.uyscuti.sharedmodule.eventbus.HideFeedFloatingActionButton
import com.uyscuti.sharedmodule.eventbus.ShowFeedFloatingActionButton
import com.uyscuti.sharedmodule.media.CameraActivity
import com.uyscuti.sharedmodule.model.AudioPlayerHandler
import com.uyscuti.sharedmodule.model.CommentAudioPlayerHandler
import com.uyscuti.sharedmodule.model.ContentType
import com.uyscuti.sharedmodule.model.FeedAdapterNotifyDatasetChanged
import com.uyscuti.sharedmodule.model.FeedCommentClicked
import com.uyscuti.sharedmodule.model.HideAppBar
import com.uyscuti.sharedmodule.model.HideBottomNav
import com.uyscuti.sharedmodule.model.PauseShort
import com.uyscuti.sharedmodule.model.ShowAppBar
import com.uyscuti.sharedmodule.model.ShowBottomNav
import com.uyscuti.sharedmodule.popupDialog.DialogManager
import com.uyscuti.sharedmodule.presentation.DialogViewModel
import com.uyscuti.sharedmodule.presentation.MessageViewModel
import com.uyscuti.sharedmodule.ui.GifActivity
import com.uyscuti.sharedmodule.ui.fragments.feed.feedviewfragments.FeedMixedFilesViewFragment
import com.uyscuti.sharedmodule.ui.fragments.feed.feedviewfragments.Fragment_Original_Post_With_Repost_Inside
import com.uyscuti.sharedmodule.ui.fragments.feed.feedviewfragments.NewRepostedPostFragment
import com.uyscuti.sharedmodule.uploads.AudioActivity
import com.uyscuti.sharedmodule.uploads.DocumentsActivity
import com.uyscuti.sharedmodule.uploads.ImagesActivity
import com.uyscuti.sharedmodule.uploads.VideosActivity
import com.uyscuti.sharedmodule.utils.AndroidUtil.showToast
import com.uyscuti.sharedmodule.utils.AudioDurationHelper
import com.uyscuti.sharedmodule.utils.AudioDurationHelper.getFormattedDuration
import com.uyscuti.sharedmodule.utils.ChatManager
import com.uyscuti.sharedmodule.utils.ChatManager.ChatManagerListener
import com.uyscuti.sharedmodule.utils.NetworkUtil
import com.uyscuti.sharedmodule.utils.PathUtil
import com.uyscuti.sharedmodule.utils.Timer
import com.uyscuti.sharedmodule.utils.TrimVideoUtils
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
import com.uyscuti.sharedmodule.utils.removeDuplicateFollowers
import com.uyscuti.sharedmodule.utils.waveformseekbar.SeekBarOnProgressChanged
import com.uyscuti.sharedmodule.utils.waveformseekbar.WaveformSeekBar
import com.uyscuti.sharedmodule.viewmodels.FeedShortsViewModel
import com.uyscuti.sharedmodule.viewmodels.FollowUnfollowViewModel
import com.uyscuti.sharedmodule.viewmodels.GetShortsByUsernameViewModel
import com.uyscuti.sharedmodule.viewmodels.feed.FeedUploadViewModel
import com.uyscuti.sharedmodule.viewmodels.feed.GetFeedViewModel
import com.uyscuti.social.business.CatalogueDetailsActivity
import com.uyscuti.social.business.adapter.business.BusinessCatalogueAdapter
import com.uyscuti.social.business.adapter.business.OnBusinessClickedListener
import com.uyscuti.social.business.repository.IFlashApiRepositoryImplementation
import com.uyscuti.social.business.viewmodel.business.BusinessCatalogueViewModel
import com.uyscuti.social.business.viewmodel.business.BusinessCatalogueViewModelFactory
import com.uyscuti.social.business.viewmodel.business.BusinessPostsViewModel
import com.uyscuti.social.chatsuit.messages.CommentsInput
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.FragmentFavoriteBinding
import com.uyscuti.social.circuit.ui.fragments.feed.feedRepostViewFragments.FeedRepostAudioViewFragment
import com.uyscuti.social.circuit.ui.fragments.feed.feedRepostViewFragmentsimport.FeedRepostMultipleImageFragment
import com.uyscuti.social.core.common.data.api.RemoteMessageRepository
import com.uyscuti.social.core.common.data.api.RemoteMessageRepositoryImpl
import com.uyscuti.social.core.common.data.room.entity.DialogEntity
import com.uyscuti.social.core.common.data.room.entity.FollowUnFollowEntity
import com.uyscuti.social.core.common.data.room.entity.MessageEntity
import com.uyscuti.social.core.common.data.room.entity.ShortsEntityFollowList
import com.uyscuti.social.core.common.data.room.entity.UserEntity
import com.uyscuti.social.network.api.request.messages.SendMessageRequest
import com.uyscuti.social.network.api.response.business.response.post.Post
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage
import com.vanniktech.emoji.EmojiPopup
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
import org.apache.poi.hwpf.HWPFDocument
import org.apache.poi.hwpf.usermodel.Range
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlin.properties.Delegates
import kotlin.random.Random

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FavoriteFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

private const val TAG = "FavoriteFragment"
private const val REQUEST_REPOST_FEED_ACTIVITY = 1020

@UnstableApi
@AndroidEntryPoint
class FavoriteFragment : Fragment(),
    OnFeedClickListener,
    com.uyscuti.sharedmodule.interfaces.feedinterfaces.FeedTextViewFragmentInterface,
    OnBusinessClickedListener,
    CommentsInput.InputListener,
    CommentsInput.EmojiListener,
    CommentsInput.VoiceListener,
    CommentsInput.GifListener,
    CommentsInput.AttachmentsListener,
    OnViewRepliesClickListener,
    Timer.OnTimeTickListener,
    ChatManagerListener

{
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentFavoriteBinding


    private val requestCode = 2024
    private val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 12
    private val shortsViewModel: GetShortsByUsernameViewModel by activityViewModels()
    private val getFeedViewModel: GetFeedViewModel by activityViewModels()
    private val feedUploadViewModel: FeedUploadViewModel by activityViewModels()
    private val followUnFollowViewModel: FollowUnfollowViewModel by viewModels()
    private var feedVideoViewFragment: FeedVideoViewFragment? = null
    private var feedTextViewFragment: FeedTextViewFragment?= null
    private var feedAudioViewFragment: FeedAudioViewFragment? = null
    private var feedMultipleImageViewFragment: FeedMultipleImageViewFragment? = null
    private var feedMixedFilesViewFragment: FeedMixedFilesViewFragment? = null
    private lateinit var favoriteFeedAdapter: FeedAdapter
    private val feesShortsSharedViewModel: FeedShortsViewModel by activityViewModels()
    private var fragmentOriginalPostWithRepostInside: Fragment_Original_Post_With_Repost_Inside? = null
    private var feedRepostDocFragment : FeedRepostDocFragment? = null
    private var feedRepostTextFragment : FeedRepostTextFragment? = null
    private var feedRepostVideoViewFragment : FeedRepostVideoViewFragment? = null
    private var feedRepostAudioViewFragment : FeedRepostAudioViewFragment? = null
    private var feedRepostImageFragment : FeedRepostImageFragment? = null
    private var feedRepostMultipleImageFragment: FeedRepostMultipleImageFragment? = null


    private lateinit var businessAdapter: BusinessCatalogueAdapter

    private val businessPostsViewModel: BusinessPostsViewModel by activityViewModels()

    private lateinit var catalogueViewModel: BusinessCatalogueViewModel

    private val dialogViewModel: DialogViewModel by activityViewModels()

    private lateinit var dialogManager: DialogManager

    private lateinit var gifsPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var audioPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var videoPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var docsPickerLauncher: ActivityResultLauncher<Intent>

    private var commentAdapter: CommentsRecyclerViewAdapter? = null
    private lateinit var commentRecyclerView: RecyclerView

    private lateinit var inputMethodManager: InputMethodManager
    private lateinit var emojiPopup: EmojiPopup

    private var emojiShowing = false

    private var isReply = false
    private var businessPostId = ""
    private var postPosition = 0
    private var commentId = ""

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


    private lateinit var repository: IFlashApiRepositoryImplementation

    @Inject
    lateinit var retrofitInstance: RetrofitInstance

    @Inject
    lateinit var localStorage: LocalStorage

    @Inject // or another appropriate scope
    lateinit var chatManager: ChatManager
    private var offerMessage = ""

    private val messageViewModel: MessageViewModel by activityViewModels()
    private var messageEntity: MessageEntity? = null

    private lateinit var remoteMessageRepository: RemoteMessageRepository

    private fun setUpViewModel() {

        repository = IFlashApiRepositoryImplementation(retrofitInstance)

        val factory = BusinessCatalogueViewModelFactory(repository)

        catalogueViewModel = ViewModelProvider(this,factory)[BusinessCatalogueViewModel::class.java]

    }

    private fun observeViewModel() {
        // Observe catalogue items and update adapter
        catalogueViewModel.catalogueItems.observe(viewLifecycleOwner) { items ->
            val list = ArrayList<Post>()
            items.forEach {
                if (it.isBookmarked) {
                    list.add(it)
                }
            }
            businessAdapter.updateCatalogue(list)
        }


        businessPostsViewModel.commentLiveData.observe(viewLifecycleOwner) { commentState ->
            if (binding.motionLayout.isVisible) {
                if (commentState.isReply) {
                    processReplyComments(commentState.comment)
                } else {
                    commentAdapter!!.submitItem(commentState.comment,0)
                    businessAdapter.updateCommentCount(postPosition)

                    if(commentAdapter!!.itemCount == 1) {
                        updateUI(false)
                    }
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        EventBus.getDefault().register(this)
        remoteMessageRepository = RemoteMessageRepositoryImpl(retrofitInstance)
    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("CutPasteId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFavoriteBinding.inflate(layoutInflater)

        setUpViewModel()

        val config = ConcatAdapter.Config.Builder()
            .setIsolateViewTypes(true) // Prevents view type conflicts
            .setStableIdMode(ConcatAdapter.Config.StableIdMode.NO_STABLE_IDS)
            .build()


        favoriteFeedAdapter = FeedAdapter(
            requireActivity(),
            retrofitInstance,
            this,
            fragmentManager = childFragmentManager
        )

        businessAdapter = BusinessCatalogueAdapter(
            requireActivity(),
            retrofitInstance,
            localStorage,
            onItemClick = { item ->
                // Handle item click - navigate to detail screen
                navigateToItemDetail(item)
            },
            this,
            onBookmarkClick = {item ->
                bookmarkBusinessPost(item)
            },
            onFollowClick = { item ->
                followBusinessPostOwner(item)
            },
            onMessageClick = { user, post ->
                val productReference = post.images.first()
                dialogManager = DialogManager(
                    requireActivity(),
                    dialogViewModel,
                    post.owner,
                    productReference
                )
                dialogManager.openChat(user)
            },
            onSendOfferClicked = {amount, message, data ->
                val user = User(
                    data.owner,
                    data.userDetails.username,
                    data.userDetails.avatar,
                    false,
                    Date()
                )

                var messageToSend = ""

                messageToSend = if (message.isEmpty()) {
                    "" +
                            "Hi! I'm interested in your ${data.itemName}." +
                            "\nWould you accept MWK$amount ?" +
                            "\nLet me know, thanks!"
                } else {
                    "" +
                            "${data.itemName}.\n${data.description}." +
                            "\nOffer Amount: MWK$amount ?" +
                            "\n$message"
                }

                offerMessage = messageToSend

                if (NetworkUtil.isConnected(requireActivity())) {
                    addDialogInTheBackGround(user, messageToSend)
                } else {
                    showToast(requireActivity(),"You have no internet connection")
                }

            },
            childFragmentManager
        )

        var isScrollingDown = false
        val scrollThreshold = 5 // M
        Log.d("RecyclerViewTwo", "Adapter set: $favoriteFeedAdapter")



        binding.rv.layoutManager = LinearLayoutManager(requireContext())
        val concatAdapter = ConcatAdapter(config, favoriteFeedAdapter, businessAdapter)
        binding.rv.adapter = concatAdapter

        favoriteFeedAdapter.setOnPaginationListener(object : com. uyscuti. sharedmodule. adapter. FeedPaginatedAdapter. OnPaginationListener {
            override fun onCurrentPage(page: Int) {
//                Toast.makeText(requireContext(), "Page $page loaded!", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "currentPage: page number $page")

            }
            override fun onNextPage(page: Int) {
                lifecycleScope.launch(Dispatchers.Main) {
//                    loadMoreShorts(page)
                    Log.d(TAG, "onNextPage: page number $page")
                    getAllFeed(page)
                }
            }

            override fun onFinish() {
                Log.d(TAG, "finished: page number")
            }
        })


        binding.rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            private var totalDy = 0 // Track accumulated scroll distance

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                // You can handle scroll state changes here if needed
            }
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                getFeedViewModel.allFeedDataLastViewPosition = firstVisibleItemPosition + 1
                getFeedViewModel.allFeedDataLastViewPosition = lastVisibleItemPosition + 1
                // Show/hide FAB based on scroll direction
                totalDy += dy
                if (totalDy > scrollThreshold && !isScrollingDown) {
                    // Scrolling down → Hide FAB
                    isScrollingDown = true
                    EventBus.getDefault().post(HideFeedFloatingActionButton())
                    totalDy = 0 // Reset after action
                } else if (totalDy < -scrollThreshold && isScrollingDown) {
                    // Scrolling up → Show FAB
                    isScrollingDown = false
                    EventBus.getDefault().post(ShowFeedFloatingActionButton(false))
                    totalDy = 0 // Reset after action
                }
            }
        })


        val callback = object : OnBackPressedCallback(true) {

            override fun handleOnBackPressed() {
                if (binding.motionLayout.isVisible) {
                    toggleBusinessCommentBottomSheet()
                } else {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }


        inputMethodManager = requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        timer = Timer(this)
        audioDurationTVCount = TextView(requireActivity())
        audioFormWave = WaveformSeekBar(requireActivity())
        audioSeekBar = SeekBar(requireActivity())


        getAllFeed(favoriteFeedAdapter.startPage)
        setupInputManager()

        registerGifPickerLauncher()
        registerVideoPickerLauncher()
        registerAudioPickerLauncher()
        registerCameraLauncher()
        registerImagePicker()
        registerDocPicker()
        handleEventsLister()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        chatManager.listener = this@FavoriteFragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        catalogueViewModel.refreshCatalogue()

        observeViewModel()

        getFeedViewModel.isFavoritesFeedDataAvailable.observe(viewLifecycleOwner) { isDataAvailable ->
            // Handle the updated value of isResuming here
            if (isDataAvailable) {
                // Do something when isResuming is true
                Log.d(
                    TAG,
                    "onCreateView: data is available and size is ${getFeedViewModel.getAllFavoriteFeedData().size}"
                )
//                    allFeedAdapter.item
                favoriteFeedAdapter.submitItems(getFeedViewModel.getAllFavoriteFeedData())
                getFeedViewModel.setIsDataAvailable(false)

            } else {
                // Do something when isResuming is false
                Log.d(TAG, "onCreateView: data not added")

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun handleEventsLister() {
        binding.click.setOnClickListener {
            hideKeyboard(binding.input.inputEditText)
            toggleBusinessCommentBottomSheet()
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
                    if( ContextCompat.checkSelfPermission(
                            requireActivity(),
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED) {
                        startRecordingVn()
                    } else {
                        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }

                }
            }
        }

    }

    @androidx.annotation.OptIn(UnstableApi::class)
    private fun navigateToItemDetail(item: Post) {
        // Navigate to item detail screen
        val intent = Intent(requireActivity(), CatalogueDetailsActivity::class.java)
        intent.putExtra("catalogue", item)
        requireActivity().startActivity(intent)
    }

    private fun followBusinessPostOwner(post: Post) {

        viewLifecycleOwner.lifecycleScope.launch {
            if (NetworkUtil.isConnected(requireActivity())) {
                catalogueViewModel.refreshCatalogue()
                val success =   businessPostsViewModel.followUnfollowBusinessPostOwner(post.owner)
                if (success) {
                    withContext(Dispatchers.Main) {
                        showToast(requireActivity(), "You have started following ${post.userDetails.username}")
                    }
                }
            }
        }
    }

    private fun bookmarkBusinessPost(data: Post) {
        if (NetworkUtil.isConnected(requireActivity())) {
            businessPostsViewModel.bookmarkUnBookmarkBusinessPost(data._id)
        }
    }



    private fun getAllFeed(page: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {

                Log.d(
                    TAG,
                    "getAllFeed: page number $page feed data empty?: ${
                        getFeedViewModel.getAllFavoriteFeedData().isEmpty()
                    }"
                )

                val response = retrofitInstance.apiService.getFavoriteFeed(
                    page.toString()
                )

                Log.d(TAG,"Response: $response")

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d(TAG, "feed: response $response")
                    Log.d(TAG, "feed: response body message ${responseBody!!.message}")
                    Log.d(TAG, "getAllFeed: size ${responseBody.data.totalBookmarkedPosts}")
                    val data = responseBody.data
                    Log.d(TAG, "getAllFeed: ${data.bookmarkedPosts.toMutableList()}")
                    getFeedViewModel.addAllFavoriteFeedData(data.bookmarkedPosts.toMutableList())
                    withContext(Dispatchers.Main) {
                        favoriteFeedAdapter.submitItems(data.bookmarkedPosts.toMutableList())
                        favoriteFeedAdapter.notifyDataSetChanged()
                    }
                    Log.d(TAG, "text comment data response: $data")
                } else {
                    Log.d(TAG, "text comment data response: $response")
                }

            } catch (e: Exception) {
                Log.e(TAG, "comment: $e")
                Log.e(TAG, "comment: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FavoriteFragment.
         **/
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FavoriteFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }



    override fun likeUnLikeFeed(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {
        try {
            val updatedComment = if (data.isLiked) {
                data.copy(
                    content = data.content?:"",
                    likes = data.likes + 1,
                    repostedByUserId = data.repostedByUserId?:"",
                    isLiked = true
                )
            } else {
                data.copy(
                    content = data.content?:"",
                    likes = data.likes - 1,
                    repostedByUserId = data.repostedByUserId?:"",
                    isLiked = false
                )
            }
            lifecycleScope.launch {
                feedUploadViewModel.likeUnLikeFeed(data._id)
            }
            Log.d("likeUnLikeFeed", "likeUnLikeFeed: likes count is ${data.likes}")
            val updatedItems = getFeedViewModel.getAllFavoriteFeedData()
            for (updatedItem in updatedItems) {
                if (updatedItem._id == data._id) {
                    if (data.isLiked) {
                        updatedItem.likes += 1
                    } else {
                        updatedItem.likes -= 1
                    }
                }
            }
            EventBus.getDefault().post(FromFavoriteFragmentFeedLikeClick(position, updatedComment))
            favoriteFeedAdapter.updateItem(position, updatedComment)
            val isMyFeedEmpty = getFeedViewModel.getMyFeedData().isEmpty()
            if (!isMyFeedEmpty) {
                val myFeedData = getFeedViewModel.getMyFeedData()
                val feedToUpdate = myFeedData.find { feed -> feed._id == data._id }
                if (feedToUpdate != null) {
                    feedToUpdate.isLiked = data.isLiked
                    feedToUpdate.likes = data.likes
                    val myFeedDataPosition =
                        getFeedViewModel.getMyFeedPositionById(feedToUpdate._id)
                    getFeedViewModel.updateMyFeedData(myFeedDataPosition, feedToUpdate)
                } else {
                    Log.d(TAG, "likeUnLikeFeed: feed to update is not available in the list")
                }
            } else {
                Log.i(TAG, "likeUnLikeFeed: my feed data is empty")
            }
            Log.d(TAG, "likeUnLikeFeed: ")
        } catch (e: Exception) {
            Log.e(TAG, "likeUnLikeFeed: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun feedCommentClicked(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {
        EventBus.getDefault().post(
            FeedCommentClicked(
                position,
                data
            )
        )
    }

    override fun feedFavoriteClick(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {
        EventBus.getDefault().post(FromFavoriteFragmentFeedFavoriteClick(position, data))
        val isMyFeedEmpty = getFeedViewModel.getMyFeedData().isEmpty()
        if (!isMyFeedEmpty) {
            val myFeedData = getFeedViewModel.getMyFeedData()
            val feedToUpdate = myFeedData.find { feed -> feed._id == data._id }
            if (feedToUpdate != null) {
                feedToUpdate.isBookmarked = data.isBookmarked
                val myFeedDataPosition = getFeedViewModel.getMyFeedPositionById(feedToUpdate._id)
                getFeedViewModel.updateMyFeedData(myFeedDataPosition, feedToUpdate)
            } else {
                Log.d(TAG, "feedFavoriteClick: feed to update is not available in the list")
            }
        } else {
            Log.i(TAG, "feedFavoriteClick: my feed data is empty")
        }
        if (!data.isBookmarked) {
            favoriteFeedAdapter.removeItem(position)
            getFeedViewModel.removeFavoriteFeed(position)
        }
        lifecycleScope.launch {
            feedUploadViewModel.favoriteFeed(data._id)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun feedAdapterNotifyDatasetChanged(event: FeedAdapterNotifyDatasetChanged) {
        Log.d(
            TAG,
            "FeedAdapterNotifyDatasetChanged: in feed adapter notify adapter: seh data set changed"
        )
        favoriteFeedAdapter.notifyDataSetChanged()

    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun favoriteFeedClick(event: FeedFavoriteClick) {

        Log.d(TAG, "favoriteFeedClick: ${getFeedViewModel.getFollowList()}")
        if (event.data.isBookmarked) {
            // Check if the feed already exists in the viewModel
            val existingFeed = getFeedViewModel.getPositionById(event.data._id)
            Log.d(TAG, "favoriteFeedClick: existing feed $existingFeed")
            if (existingFeed == -1) {
                getFeedViewModel.addFavoriteFeed(0, event.data)
                favoriteFeedAdapter.addFollowList(getFeedViewModel.getFollowList())
                favoriteFeedAdapter.notifyDataSetChanged()
            } else {
                Log.e(TAG, "favoriteFeedClick: feed already exists")
            }

            favoriteFeedAdapter.submitItem(event.data, 0)

        } else {
            val existingFeedPosition = getFeedViewModel.getPositionById(event.data._id)
            Log.d(TAG, "favoriteFeedClick: existingFeedPosition $existingFeedPosition")
            if (existingFeedPosition != -1) {

                getFeedViewModel.removeFavoriteFeed(existingFeedPosition)
            } else {
                Log.e(
                    TAG,
                    "favoriteFeedClick: you can't delete if there is no existing feed position"
                )
            }
            val feedPosition = favoriteFeedAdapter.getPositionById(event.data._id)

            Log.d(
                TAG,
                "favoriteFeedClick: item to remove on position $feedPosition"
            )
            favoriteFeedAdapter.removeItem(feedPosition)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun likeFeedClick(event: FeedLikeClick) {
        Log.d(
            TAG,
            "likeFeedClick: event bus position ${event.position} is bookmarked ${event.data.likes}"
        )
        val feedPosition = favoriteFeedAdapter.getPositionById(event.data._id)
        favoriteFeedAdapter.updateItem(feedPosition, event.data)
        getFeedViewModel.updateForFavoriteFragment(feedPosition, event.data)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun feedFavoriteFollowUpdate(event: FeedFavoriteFollowUpdate) {

    }


    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: called")
        EventBus.getDefault().unregister(this)
    }
    fun updateLayoutVisibility(contentType: ContentType, downloadFeedLayout: View, followUnfollowLayout: View, muteUser: View, hideFavorite: View) {
        when (contentType) {
            ContentType.TEXT -> {
                downloadFeedLayout.visibility = View.GONE
                followUnfollowLayout.visibility = View.GONE
                muteUser.visibility = View.VISIBLE // Or handle it accordingly
                hideFavorite.visibility = View.VISIBLE
            }
            ContentType.VIDEO -> {
                downloadFeedLayout.visibility = View.VISIBLE
                followUnfollowLayout.visibility = View.VISIBLE
                muteUser.visibility = View.GONE
                hideFavorite.visibility = View.GONE
            }
            ContentType.IMAGE -> {
                downloadFeedLayout.visibility = View.VISIBLE
                followUnfollowLayout.visibility = View.VISIBLE
                muteUser.visibility = View.VISIBLE
                hideFavorite.visibility = View.VISIBLE
            }
            ContentType.AUDIO -> {
                downloadFeedLayout.visibility = View.VISIBLE
                followUnfollowLayout.visibility = View.VISIBLE
                muteUser.visibility = View.VISIBLE
                hideFavorite.visibility = View.VISIBLE
            }
        }
    }


    fun onDownloadClick(url: String, fileLocation: String) {
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

                    download(url, fileLocation)
                }


            } else {
                download(url, fileLocation)
            }
        }




    }


    @OptIn(DelicateCoroutinesApi::class)
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
//
                        }
                        outputStream.write(buffer, 0, bytes)
                        bytes = inputStream.read(buffer)
                    }

                    requireActivity().runOnUiThread {

                        Log.d("Download", "File Downloaded : $storageDirectory")

                        val downloadedFile = File(storageDirectory)

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

                }
            }
        }
    }
    private fun generateUniqueFileName(originalUrl: String): String {
        val timestamp =
            SimpleDateFormat("yyyy_MM_dd_HHmmss", Locale.getDefault()).format(Date())
        val originalFileName = originalUrl.split("/").last()
        val fileExtension = MimeTypeMap.getFileExtensionFromUrl(originalFileName)
        val randomString = UUID.randomUUID().toString().substring(0, 8)
        return "$timestamp-$randomString.$fileExtension"
    }

    @SuppressLint("InflateParams", "MissingInflatedId")
    override fun moreOptionsClick(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {
        Log.d(TAG, "moreOptionsClick: More options clicked")
        val view: View = layoutInflater.inflate(R.layout.feed_more_options_layout, null)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(view)
        dialog.show()
        val downloadFiles: View = view.findViewById(R.id.downloadFeedLayout)
        val followUnfollowLayout: View = view.findViewById(R.id.followUnfollowLayout)
        val reportUser: View = view.findViewById(R.id.reportOptionLayout)
        val hidePostLayout: View = view.findViewById(R.id.hidePostLayout)
        val copyLink: View = view.findViewById(R.id.copyLinkLayout)
        val muteOptionLayout: View = view.findViewById(R.id.muteOptionLayout)
        val QuoteFeedLayout: View = view.findViewById(R.id.rePostFeedLayout)

        downloadFiles.setOnClickListener {
            Log.d("DownloadButton", "Data: $data")

            onDownloadClick(data.files[0].url, "FlashShorts")
            dialog.dismiss()
        }

        muteOptionLayout.setOnClickListener {
            Log.d("MuteButton", "Data: $data")
        }
        followUnfollowLayout.visibility = View.GONE
        QuoteFeedLayout.setOnClickListener {
            Log.d("QuoteButton", "Data: $data")
            val fragment = NewRepostedPostFragment(data)
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, fragment) // Ensure fragment_container is correct
            transaction.addToBackStack(null)
            transaction.commit()
            dialog.dismiss()

        }
        copyLink.setOnClickListener {
            val postId = data._id // Adjust this based on your actual Post class property name
            val linkToCopy =
                "https:/circuitSocial.app/post/$postId" // Replace with your actual link
            val clipboard =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = android.content.ClipData.newPlainText("Copied Link", linkToCopy)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(requireContext(), "Link copied to clipboard/$postId", Toast.LENGTH_SHORT)
                .show()
            dialog.dismiss()
        }

        val notInterested: View = view.findViewById(R.id.notInterestedLayout)
        notInterested.setOnClickListener {
            handleNotInterested(data)
            dialog.dismiss()

        }

        hidePostLayout.setOnClickListener {
            Log.d(TAG, "hidePostLayout: hide post clicked")
            hideSinglePost(position, data)
            dialog.dismiss()

        }

        val downloadOption: View = view.findViewById(R.id.downloadFeedLayout)
        if (data.isBookmarked) {
            data.isBookmarked = true
        }
        if (data.contentType == "text") {
            downloadOption.visibility = View.GONE
        }

        reportUser.setOnClickListener {
            Log.d("reportUser", "has been clicked")
            val intent = Intent(requireActivity(), ReportNotificationActivity2::class.java)
            startActivityForResult(intent, REQUEST_REPOST_FEED_ACTIVITY)
            dialog.dismiss()
        }

        downloadOption.setOnClickListener {
            Log.d(TAG, "Download option clicked for post: $data")
            Toast.makeText(requireContext(), "download clicked", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
    }

    private fun handleNotInterested(data: com.uyscuti.social.network.api.response.posts.Post) {

        val sharedPrefs = requireContext().getSharedPreferences("NotInterestedPosts", Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putBoolean(data._id.toString(), true)
            apply()
        }


        Toast.makeText(requireContext(), "We'll show you less content like this", Toast.LENGTH_SHORT).show()
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun hideSinglePost(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {
        Log.d(TAG, "hideSinglePost: Hiding post at position: $position, PostId: ${data._id}")
        try {
            if (::favoriteFeedAdapter.isInitialized) {


                favoriteFeedAdapter.removeItem(position)
                favoriteFeedAdapter.notifyItemRemoved(position)
//                allFeedAdapter.notifyItemChanged(position)
                // Optional: Add fade-out animation
                val viewHolder = binding.rv.findViewHolderForAdapterPosition(position)
                if (viewHolder != null) {
                    viewHolder.itemView.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction {
                            favoriteFeedAdapter.notifyItemRemoved(position)
                        }
                        .start()
                } else {
                    Log.w(TAG, "ViewHolder at position $position is null, notifying removal directly")
                    favoriteFeedAdapter.notifyItemRemoved(position) // Fallback for off-screen items
                }

                // Show Snackbar with Undo button
                Snackbar.make(binding.rv, "Post hidden", Snackbar.LENGTH_LONG)
                    .setAction("Undo") {
                        // Restore the post

                        favoriteFeedAdapter.notifyItemInserted(position)
                    }
                    .show()
                return
            }

            val sharedPrefs = requireContext().getSharedPreferences("HiddenPosts", Context.MODE_PRIVATE)
            with(sharedPrefs.edit()) {
                putBoolean(data._id, true)
                apply()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error hiding post: ${e.message}")
            Toast.makeText(requireContext(), "Failed to hide post", Toast.LENGTH_SHORT).show()
        }
    }




    private fun muteUserOption(userId: String) {
        Log.d(TAG, "muteUserOption: $userId")
        val sharedPreferences = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val mutedUsersSet = sharedPreferences.getStringSet("muted_users", mutableSetOf()) ?: mutableSetOf()
        mutedUsersSet.add(userId)
        editor.putStringSet("muted_users", mutedUsersSet)
        editor.apply()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun downloadMediaFile(fileUrl: String, fileName: String, fileType: String) {
        // Check for permissions (write external storage)
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), WRITE_EXTERNAL_STORAGE_REQUEST_CODE)
            return
        }
        // Determine directory based on file type
        val directoryType = when (fileType) {
            "audio" -> Environment.DIRECTORY_MUSIC
            "image" -> Environment.DIRECTORY_PICTURES
            "video" -> Environment.DIRECTORY_MOVIES
            else -> Environment.DIRECTORY_DOWNLOADS
        }
        val file = File(requireContext().getExternalFilesDir(directoryType), fileName)
        // Start download in a coroutine to avoid blocking the main thread
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = URL(fileUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()
                if (connection.responseCode in 200..299) {
                    val inputStream = connection.inputStream
                    val outputStream = FileOutputStream(file)

                    inputStream.use { input ->
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }
                    // Notify the user on download completion
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Download completed: ${file.absolutePath}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Download failed: ${connection.responseMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("Download", "Error downloading file", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @SuppressLint("ServiceCast")



    private fun showDeleteConfirmationDialog(feedId: String, position: Int) {
        val inflater = LayoutInflater.from(requireContext())
        val customTitleView: View = inflater.inflate(R.layout.delete_title_custom_layout, null)
        val builder = AlertDialog.Builder(requireContext())

        builder.setCustomTitle(customTitleView)
        builder.setMessage("Are you sure you want to delete this feed?")

        // Positive Button
        builder.setPositiveButton("Delete") { dialog, which ->
            // Handle delete action

            handleDeleteAction(feedId = feedId, position){ isSuccess, message ->
                if (isSuccess) {
                    Log.d(TAG, "handleDeleteAction $message")
                    dialog.dismiss()
                } else {
                    dialog.dismiss()
                    Log.e(TAG, "handleDeleteAction $message")
                }}
            dialog.dismiss()
        }


        // Negative Button
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss() // Dismiss the dialog
        }

        // Create and show the AlertDialog
        val alertDialog = builder.create()
        alertDialog.show()
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun handleDeleteAction(feedId: String, position: Int, callback: (Boolean, String) -> Unit) {
        // Logic to delete the item
        // e.g., remove it from a list or database
        Log.d(TAG, "handleDeleteAction: remove from database")
        lifecycleScope.launch {
            val response = retrofitInstance.apiService.deleteFeed(feedId)

            Log.d(TAG, "handleDeleteAction: $response")
            Log.d(TAG, "handleDeleteAction body: ${response.body()}")
            Log.d(TAG, "handleDeleteAction isSuccessful: ${response.isSuccessful}")
            if(response.isSuccessful) {
                getFeedViewModel.removeMyFeed(position)
                favoriteFeedAdapter.removeItem(position)


                shortsViewModel.postCount -= 1
                shortsViewModel.setIsRefreshPostCount(true)

                Log.d(TAG, "handleDeleteAction: delete successful")
                showSnackBar("File has been deleted successfully")
                val isAllFeedDataEmpty = getFeedViewModel.getAllFeedData().isEmpty()
                val isFavoriteFeedDataEmpty = getFeedViewModel.getAllFavoriteFeedData().isEmpty()

                if(!isFavoriteFeedDataEmpty) {
                    val favoriteFeed = getFeedViewModel.getAllFavoriteFeedData()
                    val feedToUpdate = favoriteFeed.find { feed-> feed._id == feedId }

                    if(feedToUpdate != null) {

                        Log.d(TAG, "handleDeleteAction: feed to update id ${feedToUpdate._id}")
                        try {
                            Log.d("feedResponse", "handleDeleteAction: 1 ${feedToUpdate._id}")
                            val feedPos = getFeedViewModel.getPositionById(feedId)
                            Log.d("feedResponse", "handleDeleteAction: 2 ${feedToUpdate._id}")
                            getFeedViewModel.removeFavoriteFeed(feedPos)
                            Log.d("feedResponse", "handleDeleteAction: 3 ${feedToUpdate._id}")

                            Log.d("feedResponse", "handleDeleteAction: 4 ${feedToUpdate._id}")

                        }catch (e: Exception) {
                            Log.e(TAG, "handleDeleteAction: error on bookmark delete ${e.message}")
                            e.printStackTrace()
                        }

                    }else {
                        Log.e("feedResponse", "handleDeleteAction: feed to un-favorite not available")
                    }
                }

                if(!isAllFeedDataEmpty) {
                    val allFeedData = getFeedViewModel.getAllFeedData()
                    val feedToUpdate = allFeedData.find { feed -> feed._id == feedId }
                    if (feedToUpdate != null) {
                        Log.d(TAG, "handleDeleteAction: feed data found for all fragment")
                        val pos = getFeedViewModel.getAllFeedDataPositionById(feedToUpdate._id)
                        try{
                            getFeedViewModel.removeAllFeedFragment(pos)
                        }catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }else {
                        Log.d(TAG, "handleDeleteAction: feed data not found for all fragment")
                    }
                }else {
                    Log.i(TAG, "handleDeleteAction: all feed data is empty")
                }

                if(!isFavoriteFeedDataEmpty) {
                    val favoriteFeedData = getFeedViewModel.getAllFavoriteFeedData()
                    val feedToUpdate = favoriteFeedData.find { feed -> feed._id == feedId }
                    if (feedToUpdate != null) {
                        Log.d(TAG, "handleDeleteAction: feed data found for favorite")
                        getFeedViewModel.setRefreshMyData(position, true)
                    }else {
                        Log.d(TAG, "handleDeleteAction: feed data not found for favorite")
                    }
                }else {
                    Log.i(TAG, "handleDeleteAction: favorite feed data is empty")
                }
            }else {
                callback(false, "Failed to delete file")
                showSnackBar("Please try again!!!")
            }

        }
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(requireActivity().findViewById(android.R.id.content), message, 1000)
            .setBackgroundTint((ContextCompat.getColor(requireContext(),R.color.green_dark))) // Custom background color
            .setAction("OK") {
                // Handle undo action if needed
            }
            .show()
    }

    fun forShow() {
        Log.d("forShow", "forShow: is called")
    }


    override fun feedFileClicked(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {
        val contentType = data.contentType
        if (contentType.isNullOrEmpty()) {
            Log.e("FavoriteFragment", "Invalid or null contentType for data: $data at position: $position")
            Toast.makeText(requireContext(), "Unsupported content type", Toast.LENGTH_SHORT).show()
            return
        }
        when (data.contentType){
            "text" -> {
                EventBus.getDefault().post(HideBottomNav())
                EventBus.getDefault().post(HideAppBar())
                EventBus.getDefault().post(HideFeedFloatingActionButton())
                binding.feedTextView.visibility = View.VISIBLE
                //allFeedAdapterRecyclerView.visibility = View.GONE
                val args = Bundle().apply {
                    putInt("position", position)
                    putSerializable("data", data) // Adjust type if needed
                }
                feedTextViewFragment = FeedTextViewFragment()
                feedTextViewFragment?.setListener(this)
                feedTextViewFragment?.arguments = args

                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.feed_text_view_fragment,
                        feedTextViewFragment!!
                    ) // Use the correct container ID
                    .addToBackStack(null) // Optional, to add to back stack
                    .commit()
            }

            "video"-> {
                EventBus.getDefault().post(HideBottomNav())
                EventBus.getDefault().post(HideAppBar())
                EventBus.getDefault().post(HideFeedFloatingActionButton())
                binding.feedTextView.visibility = View.VISIBLE
                binding.rv.visibility = View.GONE
                val args = Bundle().apply {
                    putInt("position", position)
                    putSerializable("data", data) // Adjust type if needed
                }
                feedVideoViewFragment = FeedVideoViewFragment()
                feedVideoViewFragment?.setListener(this)
                feedVideoViewFragment?.arguments = args
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.feed_text_view,
                        feedVideoViewFragment!!
                    ) // Use the correct container ID
                    .addToBackStack(null) // Optional, to add to back stack
                    .commit()
            }

            "mixed_files"-> {

                EventBus.getDefault().post(HideBottomNav())
                EventBus.getDefault().post(HideAppBar())
                EventBus.getDefault().post(HideFeedFloatingActionButton())
                binding.feedTextView.visibility = View.VISIBLE
                binding.rv.visibility = View.GONE


                feedMixedFilesViewFragment = FeedMixedFilesViewFragment()
                feedMixedFilesViewFragment?.setListener(this)

                val args = Bundle().apply {
                    putInt("position", position)
                    putSerializable("data", data) // Adjust type if needed
                }
                feedMixedFilesViewFragment?.arguments = args
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.feed_text_view,
                        feedMixedFilesViewFragment!!
                    ) // Use the correct container ID
                    .addToBackStack(null) // Optional, to add to back stack
                    .commit()

            }

            "audio" -> {
                EventBus.getDefault().post(HideBottomNav())
                EventBus.getDefault().post(HideAppBar())
                EventBus.getDefault().post(HideFeedFloatingActionButton())
                binding.feedTextView.visibility = View.VISIBLE
                binding.rv.visibility = View.GONE
                val args = Bundle().apply {
                    putInt("position", position)
                    putSerializable("data", data) // Adjust type if needed
                }
                feedAudioViewFragment = FeedAudioViewFragment()
                feedAudioViewFragment?.setListener(this)
                feedAudioViewFragment?.arguments = args
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.feed_text_view,
                        feedAudioViewFragment!!
                    ) // Use the correct container ID
                    .addToBackStack(null) // Optional, to add to back stack
                    .commit()
            }

            "multiple_images" -> {
                EventBus.getDefault().post(HideBottomNav())
                EventBus.getDefault().post(HideAppBar())
                EventBus.getDefault().post(HideFeedFloatingActionButton())
                binding.feedTextView.visibility = View.VISIBLE
                binding.rv.visibility = View.GONE
                val args = Bundle().apply {
                    putInt("position", position)
                    putSerializable("data", data) // Adjust type if needed
                }
                feedMultipleImageViewFragment = FeedMultipleImageViewFragment()
                feedMultipleImageViewFragment?.setListener(this)
                feedMultipleImageViewFragment?.arguments = args
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.feed_text_view,
                        feedMultipleImageViewFragment!!
                    ) // Use the correct container ID
                    .addToBackStack(null) // Optional, to add to back stack
                    .commit()
            }

        }

    }



    override fun feedRepostFileClicked(position: Int,data: com.uyscuti.social.network.api.response.posts.OriginalPost
    ) {
        when (data.contentType){
            "mixed_files" -> {

                EventBus.getDefault().post(HideBottomNav())
                EventBus.getDefault().post(HideAppBar())
                EventBus.getDefault().post(HideFeedFloatingActionButton())
                binding.feedTextView.visibility = View.VISIBLE
                binding.rv.visibility = View.GONE
                feedMixedFilesViewFragment?.setListener(this)
                val args = Bundle().apply {
                    putInt("position", position)
                    putSerializable("data", data) // Adjust type if needed
                }
                fragmentOriginalPostWithRepostInside?.arguments = args
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.feed_text_view_fragment,
                        fragmentOriginalPostWithRepostInside!!
                    ) // Use the correct container ID
                    .addToBackStack(null) // Optional, to add to back stack
                    .commit()
            }
            "multiple_images" -> {

                EventBus.getDefault().post(HideBottomNav())
                EventBus.getDefault().post(HideAppBar())
                EventBus.getDefault().post(HideFeedFloatingActionButton())
                binding.feedTextView.visibility = View.VISIBLE
                binding.rv.visibility = View.GONE
                val args = Bundle().apply {
                    putInt("position", position)
                    putSerializable("data", data) // Adjust type if needed
                }
                feedRepostMultipleImageFragment = FeedRepostMultipleImageFragment()
                feedRepostMultipleImageFragment?.setListener(this)
                feedRepostMultipleImageFragment?.arguments = args
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.feed_text_view_fragment,
                        feedRepostMultipleImageFragment!!
                    ) // Use the correct container ID
                    .addToBackStack(null) // Optional, to add to back stack
                    .commit()

            }

            "audio", "vn" -> {
                EventBus.getDefault().post(HideBottomNav())
                EventBus.getDefault().post(HideAppBar())
                EventBus.getDefault().post(HideFeedFloatingActionButton())
                binding.feedTextView.visibility = View.VISIBLE
                binding.rv.visibility = View.GONE
                val args = Bundle().apply {
                    putInt("position", position)
                    putSerializable("data", data) // Adjust type if needed
                }
                feedRepostAudioViewFragment = FeedRepostAudioViewFragment()
                feedRepostAudioViewFragment?.setListener(this)
                feedRepostAudioViewFragment?.arguments = args
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.feed_text_view_fragment,
                        feedRepostAudioViewFragment!!
                    ) // Use the correct container ID
                    .addToBackStack(null) // Optional, to add to back stack
                    .commit()

            }
            "image" -> {

                EventBus.getDefault().post(HideBottomNav())
                EventBus.getDefault().post(HideAppBar())
                EventBus.getDefault().post(HideFeedFloatingActionButton())
                binding.feedTextView.visibility = View.VISIBLE
                binding.rv.visibility = View.GONE
                val args = Bundle().apply {
                    putInt("position", position)
                    putSerializable("data", data) // Adjust type if needed
                }
                feedRepostImageFragment = FeedRepostImageFragment()
                feedRepostImageFragment?.setListener(this)
                feedRepostImageFragment?.arguments = args
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.feed_text_view_fragment,
                        feedRepostImageFragment!!
                    ) // Use the correct container ID
                    .addToBackStack(null) // Optional, to add to back stack
                    .commit()
            }

            "text" -> {
                EventBus.getDefault().post(HideBottomNav())
                EventBus.getDefault().post(HideAppBar())
                EventBus.getDefault().post(HideFeedFloatingActionButton())
                binding.feedTextView.visibility = View.VISIBLE
                binding.rv.visibility = View.GONE
                val args = Bundle().apply {
                    putInt("position", position)
                    putSerializable("data", data) // Adjust type if needed
                }
                feedRepostTextFragment = FeedRepostTextFragment()
                feedRepostTextFragment?.setListener(this)
                feedRepostTextFragment?.arguments = args
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.feed_text_view_fragment,
                        feedRepostTextFragment!!
                    ) // Use the correct container ID
                    .addToBackStack(null) // Optional, to add to back stack
                    .commit()
            }

            "docs" -> {

                EventBus.getDefault().post(HideBottomNav())
                EventBus.getDefault().post(HideAppBar())
                EventBus.getDefault().post(HideFeedFloatingActionButton())
                binding.feedTextView.visibility = View.VISIBLE
                binding.rv.visibility = View.GONE
                val args = Bundle().apply {
                    putInt("position", position)
                    putSerializable("data", data) // Adjust type if needed
                }
                feedRepostDocFragment = FeedRepostDocFragment()
                feedRepostDocFragment?.setListener(this)
                feedRepostDocFragment?.arguments = args
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.feed_text_view_fragment,
                        feedRepostDocFragment!!
                    ) // Use the correct container ID
                    .addToBackStack(null) // Optional, to add to back stack
                    .commit()

            }

            "video" -> {

                EventBus.getDefault().post(HideBottomNav())
                EventBus.getDefault().post(HideAppBar())
                EventBus.getDefault().post(HideFeedFloatingActionButton())
                binding.feedTextView.visibility = View.VISIBLE
                binding.rv.visibility = View.GONE
                val args = Bundle().apply {
                    putInt("position", position)
                    putSerializable("data", data) // Adjust type if needed
                }
                feedRepostVideoViewFragment = FeedRepostVideoViewFragment()
                feedRepostVideoViewFragment?.setListener(this)
                feedRepostVideoViewFragment?.arguments = args
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.feed_text_view_fragment,
                        feedRepostVideoViewFragment!!
                    ) // Use the correct container ID
                    .addToBackStack(null) // Optional, to add to back stack
                    .commit()

            }
        }
    }

    private fun  shareTextFeed(data: com.uyscuti.social.network.api.response.allFeedRepostsPost.Post) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, data.content)
            type = "text/plain"
        }
        // Verify that the Intent will resolve to an activity
        if (sendIntent.resolveActivity(requireContext().packageManager) != null) {
            // Start the activity to share the text
            startActivity(Intent.createChooser(sendIntent, "Share via"))
        }

    }

    private fun replaceFragment(fragment: Fragment) {
        val supportFragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }



    @SuppressLint("MissingInflatedId", "ServiceCast", "InflateParams")// Suppresses lint warning for missing inflated ID check
    override fun feedShareClicked
                (position: Int, data: com.uyscuti.social.network.api.response.posts.Post)
    {
        val context = requireContext()

        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val shareView = layoutInflater.inflate(R.layout.example, null)
        val close_button = shareView.findViewById<ImageButton>(R.id.close_button)
        val recyclerView = shareView.findViewById<RecyclerView>(R.id.apps_recycler_view)

        bottomSheetDialog.setContentView(shareView)
        bottomSheetDialog.show()

        close_button.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        // Fetch installed apps that support sharing
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain" }
        val resolveInfoList = packageManager?.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = resolveInfoList?.let { ShareFeedPostAdapter(it, context, data) }


    }

    override fun followButtonClicked(
        followUnFollowEntity: FollowUnFollowEntity,
        followButton: AppCompatButton
    ) {

        EventBus.getDefault().post(
            FeedFavoriteFollowUpdate(
                followUnFollowEntity.userId,
                followUnFollowEntity.isFollowing
            )
        )

        feesShortsSharedViewModel.setData(
            FollowUnFollowEntity(
                followUnFollowEntity.userId,
                followUnFollowEntity.isFollowing
            )
        )

        followClicked(followUnFollowEntity)

    }

    override fun feedRepostPost(position: Int,data: com.uyscuti.social.network.api.response.posts.Post) {
        val view: View = layoutInflater.inflate(R.layout.feed_moreoptions_bottomsheet_layout, null)
        val reportUser : MaterialCardView = view.findViewById(R.id.reportOptionLayout)
        val quoteButton: MaterialCardView = view.findViewById(R.id.rePostFeedLayout)
        val repostButton: MaterialCardView = view.findViewById(R.id.shareFeedLayout)
        val download: MaterialCardView = view.findViewById(R.id.downloadFeedLayout)
        download.visibility = View.GONE
        repostButton.visibility = View.VISIBLE

        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(view)
        dialog.show()


        repostButton.setOnClickListener {
            if (data.isReposted) {
                data.repostedUsers.size > 1 // Example, add current user to reposted users+= "currentUserId" // Example, append current user to reposted users


            } else {
                data.repostedUsers.size < 0

            }
        }

        quoteButton.setOnClickListener {
            dialog.dismiss()

            val fragment = NewRepostedPostFragment(data)
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, fragment) // Ensure fragment_container is correct
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    override fun feedRepostPostClicked(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.Post
    ) {
        TODO("Not yet implemented")
    }

    override fun feedClickedToOriginalPost(position: Int, originalPostId: String) {
        TODO("Not yet implemented")
    }

    override fun onImageClick() {
        TODO("Not yet implemented")
    }

    private fun followClicked(followUnFollowEntity: FollowUnFollowEntity) {
        Log.d("followButtonClicked", "followButtonClicked: $followUnFollowEntity")
        val followListItem: List<ShortsEntityFollowList> = listOf(
            ShortsEntityFollowList(
                followUnFollowEntity.userId, followUnFollowEntity.isFollowing
            )
        )
        lifecycleScope.launch(Dispatchers.IO) {

            val uniqueFollowList = removeDuplicateFollowers(followListItem)

            followUnFollowViewModel.followUnFollow(followUnFollowEntity.userId)
            Log.d(
                "followButtonClicked",
                "followButtonClicked: Inserted uniqueFollowList $uniqueFollowList"
            )
            delay(100)
        }
    }



    override fun backPressedFromFeedTextViewFragment() {
        Log.d(TAG, "backPressedFromFeedTextViewFragment: listening back pressed ")
        binding.rv.visibility = View.VISIBLE
        binding.feedTextView.visibility = View.GONE
        EventBus.getDefault().post(ShowBottomNav(false))
        EventBus.getDefault().post(ShowAppBar(false))
        EventBus.getDefault().post(ShowFeedFloatingActionButton(false))
    }
    override fun onCommentClickFromFeedTextViewFragment(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {
        EventBus.getDefault().post(
            FeedCommentClicked(
                position,
                data
            )
        )
    }
    override fun onLikeUnLikeFeedFromFeedTextViewFragment(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {
        try {

            val updatedComment = if (data.likes>1) {
                data.copy(

                    likes = data.likes + 1,
                    repostedByUserId = data.repostedByUserId?:"",
                )
            } else {
                data.copy(
                    likes = data.likes - 1,
                    repostedByUserId = data.repostedByUserId?:"",
                )
            }
            lifecycleScope.launch {
                feedUploadViewModel.likeUnLikeFeed(data._id)
            }
            Log.d("likeUnLikeFeed", "likeUnLikeFeed: likes count is ${data.likes}")
            val updatedItems = getFeedViewModel.getAllFeedData()
            for (updatedItem in updatedItems) {
                if (updatedItem._id == data._id) {
                    updatedItem.likes = data.likes
                    if (data.likes > 1) {
                        updatedItem.likes +=1
                    } else {
                        updatedItem.likes -= 1
                    }
                }
            }

            val isFavoriteFeedDataEmpty = getFeedViewModel.getAllFavoriteFeedData().isEmpty()
            if (!isFavoriteFeedDataEmpty) {
                val favoriteFeedData = getFeedViewModel.getAllFavoriteFeedData()
                val feedToUpdate = favoriteFeedData.find { feed -> feed._id == data._id }
                if (feedToUpdate != null) {
                    EventBus.getDefault().post(FeedLikeClick(position, updatedComment))
                    Log.d("likeUnLikeFeed", "likeUnLikeFeed: remove feed from favorite fragment")
                } else {
                    Log.d("likeUnLikeFeed", "likeUnLikeFeed: add feed to favorite fragment")
                }
            } else {
                Log.i("likeUnLikeFeed", "likeUnLikeFeed: my feed data is empty")
            }
            val isMyFeedEmpty = getFeedViewModel.getMyFeedData().isEmpty()
            if (!isMyFeedEmpty) {
                val myFeedData = getFeedViewModel.getMyFeedData()
                val feedToUpdate = myFeedData.find { feed -> feed._id == data._id }
                if (feedToUpdate != null) {

                    feedToUpdate.likes = data.likes
                    val myFeedDataPosition =
                        getFeedViewModel.getMyFeedPositionById(feedToUpdate._id)
                    getFeedViewModel.updateMyFeedData(myFeedDataPosition, feedToUpdate)
                } else {
                    Log.d(TAG, "likeUnLikeFeed: feed to update is not available in the list")
                }
            } else {
                Log.i(TAG, "likeUnLikeFeed: my feed data is empty")
            }
        } catch (e: Exception) {
            Log.e("likeUnLikeFeed", "likeUnLikeFeed: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun onFeedFavoriteClickFromFeedTextViewFragment(position: Int, data:com.uyscuti.social.network.api.response.posts.Post) {
        EventBus.getDefault().post(FeedFavoriteClick(position, data))

        val isMyFeedEmpty = getFeedViewModel.getMyFeedData().isEmpty()
        if (!isMyFeedEmpty) {
            val myFeedData = getFeedViewModel.getMyFeedData()
            val feedToUpdate = myFeedData.find { feed -> feed._id == data._id }
            if (feedToUpdate != null) {
                feedToUpdate.isBookmarked = data.isBookmarked
                val myFeedDataPosition = getFeedViewModel.getMyFeedPositionById(feedToUpdate._id)
                getFeedViewModel.updateMyFeedData(myFeedDataPosition, feedToUpdate)
            } else {
                Log.d(TAG, "feedFavoriteClick: feed to update is not available in the list")
            }
        } else {
            Log.i(TAG, "feedFavoriteClick: my feed data is empty")
        }

        val allFeed = getFeedViewModel.getAllFeedData().isEmpty()
        if (!allFeed) {
            Log.i(TAG, "onFeedFavoriteClickFromFeedTextViewFragment: allFeed is not empty")
            val allFeedPost = getFeedViewModel.getAllFeedData().find { it._id == data._id }
            if (allFeedPost != null) {
                Log.i(TAG, "onFeedFavoriteClickFromFeedTextViewFragment: allFeedPost is not null")
                val allFeedPosition = getFeedViewModel.getAllFeedDataPositionById(allFeedPost._id)
                getFeedViewModel.updateForAllFeedFragment(allFeedPosition, data)
            } else {
                Log.i(TAG, "onFeedFavoriteClickFromFeedTextViewFragment: allFeedPost is null")
            }
        } else {
            Log.e(TAG, "onFeedFavoriteClickFromFeedTextViewFragment: allFeed is empty")
        }
        lifecycleScope.launch {
            feedUploadViewModel.favoriteFeed(data._id)
        }
    }

    override fun onMoreOptionsClickFromFeedTextViewFragment(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {
        val view: View = layoutInflater.inflate(R.layout.feed_moreoptions_bottomsheet_layout, null)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(view)
        dialog.show()
        val reportUser : MaterialCardView = view.findViewById(R.id.reportOptionLayout)
        val quoteButton: MaterialCardView = view.findViewById(R.id.rePostFeedLayout)
        val repostButton: MaterialCardView = view.findViewById(R.id.shareFeedLayout)
        val download: MaterialCardView = view.findViewById(R.id.downloadFeedLayout)
        download.setOnClickListener {

            dialog.dismiss()
        }

    }

    override fun finishedPlayingVideo(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onRePostClickFromFeedTextViewFragment(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.Post
    ) {
        TODO("Not yet implemented")
    }


    private fun initiateDownload(post: com.uyscuti.social.network.api.response.getfeedandresposts.Post) {
        post.files.forEachIndexed { index, file ->
            val fileName = post.fileNames.getOrNull(index)?.fileName ?: "default_file_name"
            val fileType = post.fileTypes.getOrNull(index)?.fileType ?: post.contentType
            val fileUrl = file.url // Assuming `File` class has a `url` attribute

            downloadMediaFile(fileUrl, fileName, fileType)
        }
    }


    private fun getFileNameWithExtension(uri: Uri): String {
        var fileName = "document_${System.currentTimeMillis()}"

        // Try to get the original filename
        requireActivity().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst()) {
                fileName = cursor.getString(nameIndex) ?: fileName
            }
        }

        // If filename doesn't have an extension, get it from MIME type
        if (!fileName.contains(".")) {
            val mimeType = requireActivity().contentResolver.getType(uri)
            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
            if (extension != null) {
                fileName = "${fileName}.${extension}"
            }
        }

        return fileName
    }

    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = requireActivity().contentResolver.openInputStream(uri)
            val fileName = getFileNameWithExtension(uri)
            val tempFile = File(requireActivity().cacheDir, fileName)

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

        requireActivity().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
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
                    getNumberOfPagesFromUriForPDF(requireActivity(), uri)
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
        val inputStream: InputStream = requireActivity().contentResolver.openInputStream(uri) ?: return 0
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
            inputStream = requireActivity().contentResolver.openInputStream(uri)
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
        var numberOfSheets = 0
        try {
            requireActivity().contentResolver.openInputStream(uri)?.use { inputStream ->
                // WorkbookFactory automatically handles both .xls and .xlsx
                WorkbookFactory.create(inputStream).use { workbook ->
                    numberOfSheets = workbook.numberOfSheets
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return numberOfSheets
    }


    private fun getNumberOfPagesFromUriForDocx(uri: Uri): Int {
        var numberOfPages = 0
        val inputStream: InputStream = requireActivity().contentResolver.openInputStream(uri) ?: return 0
        val xwpfDocument = XWPFDocument(inputStream)

        // Count the paragraphs or sections in the document
        numberOfPages = xwpfDocument.paragraphs.size

        xwpfDocument.close()
        inputStream.close()

        return numberOfPages

    }


    private fun registerCameraLauncher() {
        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data

                // Get media type (photo or video)
                val mediaType = data?.getStringExtra(CameraActivity.EXTRA_MEDIA_TYPE)
                val mediaUri = data?.getStringExtra(CameraActivity.EXTRA_MEDIA_URI)
                val mediaPath = data?.getStringExtra(CameraActivity.EXTRA_MEDIA_PATH)

                when (mediaType) {
                    CameraActivity.MEDIA_TYPE_PHOTO -> {
                        Log.d("From Camera activity", "Image url: $mediaUri \n Image path: $mediaPath")
                        //handlePhotoResult(mediaUri, mediaPath)
                    }
                    CameraActivity.MEDIA_TYPE_VIDEO -> {
                        Log.d("From Camera activity", "Video url: $mediaUri \n Video path: $mediaPath")
                        //  handleVideoResult(mediaUri, mediaPath)
                    }
                }
            } else {
                Toast.makeText(requireActivity(), "Camera cancelled", Toast.LENGTH_SHORT).show()
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

                    if(videoPath != null) {
                        Log.d("VideoPicker", "File path: $videoPath")
                        val durationString = getFormattedDuration(videoPath)
                        val file = File(videoPath)
                        Log.d("VideoPicker", "File path durationString: $durationString")

                        if(file.exists()) {
                            val fileSizeInBytes = file.length()
                            val fileSizeInKB = fileSizeInBytes / 1024
                            val fileSizeInMB = fileSizeInKB / 1024

                            val fileSizeInGB = fileSizeInMB / 1024 // Conversion from MB to GB

                            Log.d("VideoPicker", "File size: $fileSizeInMB MB")

                            if (fileSizeInGB.toInt() == 1) {
                                showToast(requireActivity(), "File size too large")
                            } else if(fileSizeInMB > 10) {
                                Log.d("VideoPicker", "File size: greater than $fileSizeInMB MB")
                                if(isReply) {
                                    uploadVideoComment(videoPath, caption, isReply)
                                } else {
                                    uploadVideoComment(videoPath, caption)
                                }
                            } else {
                                Log.d("VideoPicker", "File size: less than $fileSizeInMB MB")
                                if(isReply) {
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

    private fun registerAudioPickerLauncher() {
        audioPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

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

    private fun  registerGifPickerLauncher() {
        gifsPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data
            val gifUri = data?.getStringExtra("gifUri")
            Log.d(TAG, "Gif Uri $gifUri")

            if(gifUri!!.isNotEmpty()) {

                val localUpdateId = generateRandomId()

                if (isReply) {
                    businessPostsViewModel.addCommentReply(
                        commentId,
                        contentType = "gif",
                        localUpdateId = localUpdateId,
                        gif = gifUri,
                        isReply = isReply
                    )
                    isReply = false
                } else {
                    businessPostsViewModel.addComment(
                        businessPostId,
                        contentType = "gif",
                        localUpdateId = localUpdateId,
                        gif = gifUri
                    )
                }

            }

        }
    }

    private fun registerImagePicker() {
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // Handle image selection result here
                val data = result.data
                // Process the selected image data
                val imagePath = data?.getStringExtra("image_url")
                val caption = data?.getStringExtra("caption") ?: ""

                val filePath = PathUtil.getPath(
                    requireActivity(),
                    imagePath!!.toUri()
                ) // Use the utility class to get the real file path
                Log.d("PhotoPicker", "File path: $filePath")
                Log.d("PhotoPicker", "File path: $isReply")

                val file = filePath?.let { File(it) }
                if (file?.exists() == true) {
                    lifecycleScope.launch {
                        val compressedImageFile = Compressor.compress(requireActivity(), file)
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

    private fun registerDocPicker() {
        docsPickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val data = result.data
                    // Process the selected image data
                    val docPath = data?.getStringExtra("doc_url")
                    val caption = data?.getStringExtra("caption") ?: ""

                    Log.d(TAG, "Path: $docPath caption: $caption")

                    handleDocumentUri(
                        getContentUriFromFilePath(requireActivity(), docPath!!)!!,
                        caption
                    )
                }
            }
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

    private fun openDocPickerLauncher() {
        val intent = Intent(requireActivity(), DocumentsActivity::class.java)
        docsPickerLauncher.launch(intent)

    }

    private fun openImagePicker() {
        val intent = Intent(requireActivity(), ImagesActivity::class.java)
        imagePickerLauncher.launch(intent)
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
                businessPostsViewModel.addCommentReply(
                    commentId,
                    content = caption,
                    contentType = "video",
                    localUpdateId = localUpdateId,
                    file = file,
                    isReply = isReply1
                )
                isReply = false
            } else {
                businessPostsViewModel.addComment(
                    businessPostId,
                    content = caption,
                    contentType = "video",
                    localUpdateId = localUpdateId,
                    file = file
                )
            }
        }

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
                businessPostsViewModel.addCommentReply(
                    commentId,
                    content = caption,
                    contentType = "image",
                    localUpdateId = localUpdateId,
                    file = file,
                    isReply = isReply1
                )
                isReply = false
            } else {
                businessPostsViewModel.addComment(
                    businessPostId,
                    content = caption,
                    contentType = "image",
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
        if(file.exists()) {
            Log.d("UploadingDocument", "Upload document called")

            if (isReply) {
                businessPostsViewModel.addCommentReply(
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
                businessPostsViewModel.addComment(
                    businessPostId,
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
                businessPostsViewModel.addCommentReply(
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
                businessPostsViewModel.addComment(
                    businessPostId,
                    content = caption,
                    file = file,
                    contentType = contentType,
                    localUpdateId = localUpdateId,
                    fileType = fileType
                )
            }

        }
    }



    private fun updateRecordWaveProgress(progress: Float) {

        CoroutineScope(Dispatchers.Main).launch {
            binding.wave.progress = progress
//            currentComment?.progress = progress
            Log.d("updateWaveProgress", "updateWaveProgress: $progress")
        }
    }

    private val onRecordWaveRunnable = object : kotlinx.coroutines.Runnable {
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

    private val waveRunnable = object : kotlinx.coroutines.Runnable {
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


        //                Log.d(TAG, "render: file $audioUrl can execute: ${file.canExecute()}")

//        binding.wave.setSampleFrom(audioFile)
        CoroutineScope(Dispatchers.IO).launch {
            WaveFormExtractor.getSampleFrom(requireActivity().applicationContext, outputVN) {

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
                    requireActivity().runOnUiThread {
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
                    uploadAudioComment(outputVnFile, isReply1 = isReply,  fileType = "vnAudio")
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
                exoPlayer = ExoPlayer.Builder(requireActivity()).build()

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
                    Log.e("commentAudioStartPlaying", "Cache error, clearing and retrying", cacheException)

                    // Clear corrupted cache
                    clearExoPlayerCache()

                    // Retry with fresh cache
                    try {
                        exoPlayer = buildExoPlayerWithCache(mediaItem)
                        Log.d("commentAudioStartPlaying", "Cache cleared, using fresh cache")
                    } catch (retryException: Exception) {
                        Log.e("commentAudioStartPlaying", "Cache still failing, playing directly from server", retryException)

                        // Fallback to direct server playback without cache
                        exoPlayer = buildExoPlayerWithoutCache(mediaItem)
                        Log.d("commentAudioStartPlaying", "Playing directly from server without cache")
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
                            showToast(requireActivity(), "Can't play this audio")
                        }
                    } else {
                        showToast(requireActivity(), "Can't play this audio")
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
            showToast(requireActivity(), "Error playing audio")
        }
    }

    // Helper method to build ExoPlayer with cache
    private fun buildExoPlayerWithCache(mediaItem: MediaItem): ExoPlayer {
        httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)

        defaultDataSourceFactory = DefaultDataSourceFactory(
            requireActivity(), httpDataSourceFactory
        )

        cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(simpleCache!!)
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        val mediaSourceFactory: MediaSource.Factory =
            DefaultMediaSourceFactory(requireActivity())
                .setDataSourceFactory(cacheDataSourceFactory)

        val player = ExoPlayer.Builder(requireActivity())
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

        val player = ExoPlayer.Builder(requireActivity()).build()

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

            val exoPlayerCacheDir = File(requireActivity().cacheDir, "exoplayer")
            if (exoPlayerCacheDir.exists()) {
                exoPlayerCacheDir.deleteRecursively()
                Log.d("clearExoPlayerCache", "Cache cleared successfully")
            }

            // Reinitialize cache
            val leastRecentlyUsedCacheEvictor = LeastRecentlyUsedCacheEvictor(1024 * 1024 * 1024) // 1GB
            val exoDatabaseProvider = ExoDatabaseProvider(requireActivity())
            exoPlayerCacheDir.mkdirs()
            simpleCache = SimpleCache(exoPlayerCacheDir, leastRecentlyUsedCacheEvictor, exoDatabaseProvider)

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
                                    commentAdapter!!.setSecondSeekBarProgress(seekBarProgress, currentCommentAudioPosition)
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
        commentAdapter!!.updatePlaybackButton(currentCommentAudioPosition, isReply, audioPlayPauseBtn)
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

        } else if(comment.contentType == "gif") {
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

        } else if(comment.contentType == "video") {
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
        }  else if(comment.contentType == "audio") {
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
        } else if(comment.contentType == "docs") {
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
        val localSettings = requireActivity().getSharedPreferences("LocalSettings", MODE_PRIVATE)
        val profilePic = localSettings.getString("profile_pic", "").toString()

        val avatar = com.uyscuti.social.network.api.response.commentreply.allreplies.Avatar(
            "", "", url = profilePic
        )

        val account = com.uyscuti.social.network.api.response.commentreply.allreplies.Account(
            _id = "", avatar = avatar, "", LocalStorage.getInstance(requireActivity()).getUsername()
        )
        val author =
            com.uyscuti.social.network.api.response.commentreply.allreplies.Author(
                _id = "21", account = account, firstName = "", lastName = ""
            )

        return author
    }

    private fun setupInputManager() {
        emojiPopup = EmojiPopup(binding.motionLayout, binding.input.inputEditText)

        binding.input.setInputListener(this)
        binding.input.setAttachmentsListener(this)
        binding.input.setVoiceListener(this)
        binding.input.setEmojiListener(this)
        binding.input.setGifListener(this)
    }

    private fun toggleBusinessCommentBottomSheet() {
        val currentVisibility = binding.motionLayout.visibility

        if(currentVisibility == View.VISIBLE) {
            binding.motionLayout.visibility = View.GONE
            binding.VnLayout.visibility = View.GONE

            binding.replyToLayout.visibility = View.GONE
            binding.input.inputEditText.setText("")

            deleteRecording()
            stopPlayingVn()
            commentAudioStop()
            stopWaveRunnable()
            stopRecordWaveRunnable()
            exoPlayer?.release()
            // EventBus.getDefault().post(ShowBottomNav(true))

        } else {
            // EventBus.getDefault().post(HideBottomNav(true))
            binding.motionLayout.visibility = View.VISIBLE
            binding.motionLayout.transitionToStart()
        }

    }

    private fun initCommentAdapter() {
        commentAdapter = CommentsRecyclerViewAdapter(requireActivity(), this)
        commentAdapter?.setDefaultRecyclerView(requireActivity(), R.id.commentSRecyclerView)

        commentRecyclerView = binding.commentSRecyclerView

        commentRecyclerView.itemAnimator = null
    }

    private fun getBusinessComments(page: Int) {

        lifecycleScope.launch(Dispatchers.IO) {

            withContext(Dispatchers.Main) {
                if(page == 1) {
                    showShimmer()
                } else {
                    showProgressBar()
                }
            }

            try {

                val commentsWithReplies = businessPostsViewModel.getBusinessPostComments(businessPostId, page)
                withContext(Dispatchers.Main) {

                    if(page == 1) {
                        hideShimmer()
                    } else {
                        hideProgressBar()
                    }

                    commentAdapter!!.submitItems(commentsWithReplies)
                    if(commentsWithReplies.isEmpty()) {
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

    private fun hideKeyboard(view: View) {
        val imm = inputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
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

    @SuppressLint("InflateParams")
    private fun showAttachmentDialog() {
        val dialog = BottomSheetDialog(requireActivity())

        val view = LayoutInflater.from(requireActivity()).inflate(
            com.uyscuti.social.business.R.layout.file_upload_dialog,
            null
        )

        val video = view.findViewById<LinearLayout>(com.uyscuti.social.business.R.id.upload_video)
        val audio = view.findViewById<LinearLayout>(com.uyscuti.social.business.R.id.upload_audio)
        val image = view.findViewById<LinearLayout>(com.uyscuti.social.business.R.id.upload_image)
        val camera = view.findViewById<LinearLayout>(com.uyscuti.social.business.R.id.open_camera)
        val doc = view.findViewById<LinearLayout>(com.uyscuti.social.business.R.id.upload_doc)
        val location = view.findViewById<LinearLayout>(com.uyscuti.social.business.R.id.share_location)

        image!!.setOnClickListener {
            openImagePicker()
            dialog.dismiss()
        }

        video!!.setOnClickListener {
            val intent = Intent(requireActivity(), VideosActivity::class.java)
            videoPickerLauncher.launch(intent)
            dialog.dismiss()
        }

        audio!!.setOnClickListener {
            val intent = Intent(requireActivity(), AudioActivity::class.java)
            audioPickerLauncher.launch(intent)
            dialog.dismiss()
        }

        doc?.setOnClickListener {
            openDocPickerLauncher()
            dialog.dismiss()
        }

        camera!!.setOnClickListener {
            val intent = Intent(requireActivity(), CameraActivity::class.java)
            cameraLauncher.launch(intent)
            dialog.dismiss()
        }

        location?.visibility = View.INVISIBLE
        dialog.setContentView(view)
        dialog.show()

    }

    override fun businessCommentClickedListener(
        position: Int,
        post: Post
    ) {
        businessPostId = post._id
        postPosition = position

        initCommentAdapter()

        toggleBusinessCommentBottomSheet()


        commentAdapter!!.setOnPaginationListener(object : AdPaginatedAdapter.OnPaginationListener {

            override fun onCurrentPage(page: Int) {
                Log.d(TAG, "currentPage: page number $page")
            }

            override fun onNextPage(page: Int) {
                lifecycleScope.launch(Dispatchers.Main) {
                    Log.d(TAG, "onNextPage: page number $page")
                    getBusinessComments(page)
                }
            }

            override fun onFinish() {
                Log.d(TAG, "finished: page number")
            }
        })

        lifecycleScope.launch(Dispatchers.Main) {
            getBusinessComments(commentAdapter!!.startPage)
        }

    }

    override fun onSubmit(input: CharSequence?): Boolean {
        hideKeyboard(binding.input.inputEditText)
        val localUpdateId = generateRandomId()

        if (!isReply) {
            businessPostsViewModel.addComment(
                businessPostId,
                input.toString(),
                "text",
                localUpdateId
            )

        } else {
            businessPostsViewModel.addCommentReply(
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
        val intent = Intent(requireActivity(), GifActivity::class.java)
        gifsPickerLauncher.launch(intent)
    }

    override fun onAddAttachments() {
        showAttachmentDialog()
    }

    override fun onViewRepliesClick(
        data: Comment,
        repliesRecyclerView: RecyclerView,
        position: Int
    ) {
        TODO("Not yet implemented")
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

            }  else {
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
        if (NetworkUtil.isConnected(requireActivity())) {
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
            businessPostsViewModel.likeUnlikeBusinessComment(data._id)
        } else {
            showToast(requireActivity(), "Like failed. No internet access.")
        }
    }

    override fun likeUnlikeCommentReply(
        replyPosition: Int,
        replyData: com.uyscuti.social.network.api.response.commentreply.allreplies.Comment,
        mainCommentPosition: Int,
        mainComment: Comment
    ) {
        if (NetworkUtil.isConnected(requireActivity())) {
            if(replyData.isLiked) {
                replyData.copy(
                    likes = replyData.likes + 1
                )
            }  else {
                replyData.copy(
                    likes = replyData.likes - 1
                )
            }
            mainComment.replies[replyPosition] = replyData

            commentAdapter?.updateItem(mainCommentPosition, mainComment)
            businessPostsViewModel.likeUnlikeBusinessCommentReplies(replyData._id)

        }  else {
            showToast(requireActivity(), "Like failed. No internet access.")
        }
    }

    override fun onTimerTick(duration: String) {
        binding.timerTv.text = duration

        var amplitude = mediaRecorder!!.maxAmplitude.toFloat()
        amplitude = if (amplitude > 0) amplitude else 130f

        binding.waveForm.addAmplitude(amplitude)
    }

    private fun addDialogInTheBackGround(user: User, lastMessage: String) {
        val singleUserList = arrayListOf(user)

        val message = Message(
            user.id,
            user,
            lastMessage,
            Date()
        )

        val tempDialog = Dialog(
            user.id,
            user.name,
            user.avatar,
            singleUserList,
            message,
            0
        )

        val messageId = "Text_${Random.Default.nextInt()}"

        val textMessage = MessageEntity(
            id = messageId,
            chatId = user.id,
            userName = "You",
            user = user.toUserEntity(),
            userId = localStorage.getUserId(),
            text = lastMessage,
            createdAt = System.currentTimeMillis(),
            imageUrl = null,
            voiceUrl = null,
            voiceDuration = 0,
            status = "Sending",
            videoUrl = null,
            audioUrl = null,
            docUrl = null,
            fileSize = 0
        )

        messageEntity = textMessage

        val dialogEntity = DialogEntity(
            id = tempDialog.dialogName,
            dialogPhoto = tempDialog.dialogPhoto,
            dialogName = tempDialog.dialogName,
            users = listOf(user.toUserEntity()),
            lastMessage = textMessage,
            unreadCount = 0
        )

        insertDialog(dialogEntity)
    }

    private fun insertDialog(dialog: DialogEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            dialogViewModel.insertDialog(dialog)
        }
    }


    private fun User.toUserEntity(): UserEntity {
        return UserEntity(
            id,
            name,
            avatar,
            lastSeen = Date(),
            true
        )
    }

    private suspend fun insertMessage(message: MessageEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            messageViewModel.insertMessage(message)
        }
    }

    override fun onDialogUpdated(newDialogId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = remoteMessageRepository.sendMessage(
                newDialogId,
                SendMessageRequest(content = offerMessage)
            )

            messageEntity?.chatId = newDialogId
            messageEntity?.status = "Sent"
            CoroutineScope(Dispatchers.IO).launch { insertMessage(messageEntity!!) }
            Log.d("Catalogue", "Chat: $newDialogId Message Result: $result")
        }
    }


}

