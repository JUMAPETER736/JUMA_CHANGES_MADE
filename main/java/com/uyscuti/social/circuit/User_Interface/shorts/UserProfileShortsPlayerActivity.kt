package com.uyscuti.social.circuit.User_Interface.shorts

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.GestureDetectorCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
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
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.uyscuti.social.chatsuit.messages.CommentsInput
import com.uyscuti.social.core.common.data.room.entity.ShortCommentEntity
import com.uyscuti.social.core.common.data.room.entity.ShortCommentReply
import com.uyscuti.social.core.common.data.room.entity.ShortsEntity
import com.uyscuti.social.core.common.data.room.entity.UserShortsEntity
import com.uyscuti.social.circuit.FlashApplication
import com.uyscuti.social.circuit.adapter.CommentAdapter
import com.uyscuti.social.circuit.User_Interface.fragments.OnClickListeners
import com.uyscuti.social.circuit.User_Interface.fragments.OnCommentsClickListener
import com.uyscuti.social.circuit.adapter.UserProfileShortsAdapter
import com.uyscuti.social.circuit.data.model.Comment
import com.uyscuti.social.circuit.model.FeedAdapterNotifyDatasetChanged
import com.uyscuti.social.circuit.model.PlayPauseEvent
import com.uyscuti.social.circuit.model.ShortAdapterNotifyDatasetChanged
import com.uyscuti.social.circuit.model.ShortsBookmarkButton2
import com.uyscuti.social.circuit.model.ShortsCommentButtonClicked
import com.uyscuti.social.circuit.model.ShortsLikeUnLikeButton2
import com.uyscuti.social.circuit.model.ShortsViewModel
import com.uyscuti.social.circuit.model.UserProfileShortsOnClickEvent
import com.uyscuti.social.circuit.service.VideoPreLoadingService
import com.uyscuti.social.circuit.User_Interface.fragments.SHORTS
import com.uyscuti.social.circuit.User_Interface.fragments.user_profile_fragments.SharedViewModel
import com.uyscuti.social.circuit.utils.Constants
import com.uyscuti.social.circuit.utils.Timer
import com.uyscuti.social.circuit.utils.generateMongoDBTimestamp
import com.uyscuti.social.circuit.utils.generateRandomId
import com.uyscuti.social.circuit.utils.isInternetAvailable
import com.uyscuti.social.circuit.viewmodels.comments.CommentsViewModel
import com.uyscuti.social.circuit.viewmodels.comments.RoomCommentReplyViewModel
import com.uyscuti.social.circuit.viewmodels.comments.RoomCommentsViewModel
import com.uyscuti.social.circuit.viewmodels.comments.ShortCommentsViewModel
import com.uyscuti.social.circuit.viewmodels.feed.FeedLiveDataViewModel
import com.uyscuti.social.circuit.viewmodels.feed.GetFeedViewModel
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.adapter.CommentsRecyclerViewAdapter
import com.uyscuti.social.circuit.adapter.OnViewRepliesClickListener
import com.uyscuti.social.circuit.adapter.notifications.AdPaginatedAdapter
import com.uyscuti.social.circuit.databinding.ActivityUserProfileShortsPlayerBinding
import com.uyscuti.social.network.api.response.comment.allcomments.Account
import com.uyscuti.social.network.api.response.comment.allcomments.Author
import com.uyscuti.social.network.api.response.comment.allcomments.Avatar
import com.uyscuti.social.network.api.response.getallshorts.Post
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.ui.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
private const val TAG = "UserProfileShortsPlayerActivity"
@UnstableApi
@AndroidEntryPoint
class UserProfileShortsPlayerActivity : AppCompatActivity(), OnCommentsClickListener,
    OnClickListeners, CommentsInput.InputListener, CommentsInput.EmojiListener,
    CommentsInput.VoiceListener,
    CommentsInput.GifListener,
    CommentsInput.AttachmentsListener, OnViewRepliesClickListener,
    Timer.OnTimeTickListener {
    private lateinit var shortsAdapter: UserProfileShortsAdapter
    private lateinit var playerView: PlayerView
    private lateinit var httpDataSourceFactory: HttpDataSource.Factory
    private lateinit var defaultDataSourceFactory: DefaultDataSourceFactory
    private lateinit var cacheDataSourceFactory: CacheDataSource.Factory

    private val simpleCache: SimpleCache = FlashApplication.cache
    private val playbackStateListener: Player.Listener = playbackStateListener()

    private var exoPlayer: ExoPlayer? = null
    private val exoPlayerItems = ArrayList<ExoPlayerItem>()
    private var videoShorts = ArrayList<UserShortsEntity>()
    private var shortsList = ArrayList<String>()
    var lastPosition = 0
    private var isUserSeeking = false

    companion object {
        const val SHORTS_LIST = "shorts_list"
        const val CLICKED_SHORT = "clicked_short"
    }

    private lateinit var binding: ActivityUserProfileShortsPlayerBinding

    @Inject
    lateinit var retrofitIns: RetrofitInstance

    private val sharedViewModel by viewModels<SharedViewModel>()

    private var userShortsList: List<UserShortsEntity> = emptyList()

    // Global variable for clickedShort
    private var clickedShort: UserShortsEntity? = null

    private var currentPosition: Int = -1

    private var isPlaying = false

    private lateinit var gestureDetector: GestureDetectorCompat

    private val shortsViewModel: ShortsViewModel by viewModels()


    var postId = ""
    private var isFeedComment = false

    private var adapter: CommentsRecyclerViewAdapter? = null
    private var isReply = false

    private lateinit var commentsViewModel: ShortCommentsViewModel
    private lateinit var shorts2CommentViewModel: RoomCommentsViewModel
    private lateinit var commentViewModel: CommentsViewModel
    private lateinit var feedViewModel: GetFeedViewModel
    private lateinit var roomCommentReplyViewModel: RoomCommentReplyViewModel


    private lateinit var emojiPopup: EmojiPopup
    private lateinit var inputMethodManager: InputMethodManager
    private var emojiShowing = false

    private var vnList = ArrayList<String>()

    private lateinit var settings: SharedPreferences
    private val PREFS_NAME = "LocalSettings"
    private var data: Comment? = null

    private var listOfReplies = mutableListOf<Comment>()

    private var shortToComment: ShortsEntity? = null

    private var feedToComment: com.uyscuti.social.network.api.response.allFeedRepostsPost.Post? = null
    private var myFeedToComment: com.uyscuti.social.network.api.response.allFeedRepostsPost.Post? = null
    private var favoriteFeedToComment: com.uyscuti.social.network.api.response.allFeedRepostsPost.Post? = null

    private lateinit var commentId: String
    private var position: Int = 0

    private val feedLiveDataViewModel: FeedLiveDataViewModel by viewModels()


    @SuppressLint("ClickableViewAccessibility")
    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileShortsPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)


        commentsViewModel = ViewModelProvider(this)[ShortCommentsViewModel::class.java]
        commentViewModel = ViewModelProvider(this)[CommentsViewModel::class.java]
        shorts2CommentViewModel = ViewModelProvider(this)[RoomCommentsViewModel::class.java]
        feedViewModel = ViewModelProvider(this)[GetFeedViewModel::class.java]
        roomCommentReplyViewModel = ViewModelProvider(this)[RoomCommentReplyViewModel::class.java]

        playerView = findViewById(R.id.playerView)

        EventBus.getDefault().register(this)

        userShortsList = intent.getSerializableExtra(SHORTS_LIST) as ArrayList<UserShortsEntity>
//        Log.d(TAG, "onCreate: user shorts list $userShortsList")
//        Log.d(TAG, "onCreate: user shorts list size ${userShortsList.size}")
//        val storedShortsList = intent.getSerializableExtra(SHORTS_LIST) as UserShortsEntity
        clickedShort = intent.getSerializableExtra(CLICKED_SHORT) as UserShortsEntity


        currentPosition = userShortsList.indexOf(clickedShort)

        Log.d(TAG, "onCreate: Clicked short: $clickedShort")
//        Log.d(TAG, "onCreate: Clicked short: $currentPosition")

        binding.shortSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
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

        shortsAdapter = UserProfileShortsAdapter(
            this@UserProfileShortsPlayerActivity,
            this@UserProfileShortsPlayerActivity,
        )

        // Now you can work with the list of UserShortsEntity instances
//        Log.d(TAG, "onCreate: storedShortsList: $userShortsList")
//        Log.d(TAG, "onCreate: storedShortsList size: ${userShortsList.size}")
        shortsAdapter.addData(userShortsList)

        for (userShort in userShortsList) {
            // Do something with each UserShortsEntity
//            Log.d(TAG, "onCreate: userShort: $userShortsList")
            videoShorts.add(userShort)

        }

//        shortsAdapter.addData(userShortsList)

        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                binding.shortsViewPager.adapter = shortsAdapter
                binding.shortsViewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
                binding.playerView.player = exoPlayer

                binding.shortsViewPager.setCurrentItem(currentPosition, false)
                playVideoAtPosition(currentPosition)


                binding.shortsViewPager.registerOnPageChangeCallback(object :
                    ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        exoPlayer!!.stop()
                        exoPlayer!!.seekTo(0)

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

                        // Check if the user is scrolling towards the end or the beginning
                        // Check if the user is scrolling towards the end or the beginning
                        if (positionOffset > 0.5) {
                            // User is scrolling towards the end, update the current position to the next video
                            currentPosition = position + 1
                        } else if (positionOffset < -0.5) {
                            // User is scrolling towards the beginning, update the current position to the previous video
                            currentPosition = position - 1
                        } else {
                            // User is in a stable position, update the current position to the current video
                            currentPosition = position
                        }

                        if (position > lastPosition) {
                            // User is scrolling down
//                            Log.d("Exoplayer", "User scrolling down")
                            loadMoreVideosIfNeeded(position)
                        }

                        lastPosition = position

                        // Play the video at the updated position
//                        playVideoAtPosition(currentPosition)
                    }

                    override fun onPageScrollStateChanged(state: Int) {
                        super.onPageScrollStateChanged(state)
                        Log.d(TAG, "onPageScrollStateChanged: state $state")

                        // Check if the scroll state is idle
                        if (state == ViewPager.SCROLL_STATE_SETTLING) {
                            Log.d(TAG, "onPageScrollStateChanged: state $state")
                            // The scroll state is idle, play the video at the updated position
                            playVideoAtPosition(currentPosition)
                        }
                    }

                })
            }

        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Custom back press handling code
                // Assuming you perform some actions and want to return a result
                val resultIntent = Intent().apply {
                    putExtra("resultKey", "resultValue") // Add data to be returned
                }

                // Set the result and finish the activity
                setResult(RESULT_OK, resultIntent)
                finish()
                Log.d(TAG, "handleOnBackPressed: on back pressed")
            }
        })



        initializeCommentsBottomSheet()
        settings = getSharedPreferences(PREFS_NAME, 0)

        addComment()
    }

    private fun addComment() {
        Log.d("addCommentReply", "addComment: is reply $isReply")
        if (isInternetAvailable(this)) {

            shorts2CommentViewModel.allComments.observe(this) {

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
                    shorts2CommentViewModel.viewModelScope.launch {
                        val isDeleted = shorts2CommentViewModel.deleteCommentById(it[0].postId)
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun pausePlayEvent(event: PlayPauseEvent) {
        Log.d(TAG, "pausePlayEvent ")
        if (exoPlayer?.isPlaying == true) {
            pauseVideo()
        } else {
            playVideo()
        }
//        togglesPausePlay()
    }

    private inner class MyGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            // Toggle between play and pause
            playerView.player?.let {
                if (it.isPlaying) {
                    it.pause()
                } else {
                    it.play()
                }
            }
            return true
        }
    }

    fun loadMoreVideosIfNeeded(position: Int) {
        // Check if the position meets the condition
        if (position >= 5 && (position - 5) % 5 == 0) {
            // Calculate the parameter to pass to loadMoreVideos based on the position
            val loadMoreValue = 2 + (position - 5) / 5

//            viewPager.offscreenPageLimit = 10
            // Call loadMoreVideos with the calculated value
            loadMoreVideos(loadMoreValue)

//            viewPager.offscreenPageLimit += viewpager2Limit
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
            loadMoreShorts(pageNumber)
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
                thumbnail = serverResponseItem.thumbnail
                // map other properties...
            )
        }
    }

    private fun serverResponseToUserEntity(serverResponse: List<Post>): List<UserShortsEntity> {
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

    private suspend fun loadMoreShorts(currentPage: Int) {
        try {
            val response = retrofitIns.apiService.getShorts(currentPage.toString())
//            retrofitIns.apiService.getShortsByUsernameWithPage("", "")

            if (response.isSuccessful) {
                val responseBody = response.body()
//                Log.d("AllShorts", "Shorts List in page $currentPage ${responseBody?.data}")


                val shortsEntity =
                    responseBody?.data?.posts?.posts?.let { serverResponseToUserEntity(it) }

                // Now, insert yourEntity into the Room database
                if (shortsEntity != null) {


                    lifecycleScope.launch(Dispatchers.IO) {
//                      shortsViewModel.addAllShorts(shortsEntity)

                        for (entity in shortsEntity) {
                            // Access the list of images for each entity
                            val images = entity.images
                            videoShorts.add(entity)
                            userShortsList = userShortsList + entity

                            // Iterate through the list of images
                            for (image in images) {
                                // Access individual image properties or perform any desired actions
                                val imageUrl = image.url
                                Log.d(SHORTS, "imageUrl - $imageUrl")
                                shortsList.add(imageUrl)
//                                userShortsList.ad
//                                viewPager.offscreenPageLimit = viewpager2Limit + 10

//                                EventBus.getDefault().post(ShortsCacheEvent(shortsList))

                                // Do something with the imageUrl...
                            }
                        }
//                        viewPager.offscreenPageLimit = 21
                        startPreLoadingService()
                        withContext(Dispatchers.Main) {
                            shortsAdapter.addData(shortsEntity)
                        }

                    }
//
//                    Log.d(SHORTS, "Data added to local database - $shortsEntity")
                } else {
                    Log.d(SHORTS, "failed to add shorts to local database")
                }
//                Log.d(SHORTS, "Handle the updated list of persons")
//                shortsViewModel.addAllShorts(personList)

            } else {
                Log.d("AllShorts", "Error: ${response.message()}")
                runOnUiThread {
                    showToast(response.message())
                }
            }

        } catch (e: HttpException) {
            Log.d("AllShorts", "Http Exception ${e.message}")
            runOnUiThread {
                showToast("Failed to connect try again...")
            }
        } catch (e: IOException) {
            Log.d("AllShorts", "IOException ${e.message}")
            runOnUiThread {
                showToast("Failed to connect try again...")
            }
        }
    }


    @OptIn(UnstableApi::class)
    private fun playVideoAtPosition(position: Int) {

        Log.d(TAG, "onPageSelected: currentPosition = $currentPosition and position $position")


        // Calculate the relative position based on the distance from the clicked position
        val relativePosition = currentPosition - position
        Log.d(TAG, "onPageSelected: relativePosition = $relativePosition")

        // Adjust the playback based on the relative position
        val finalPosition = relativePosition.toLong() * C.TIME_UNSET
        Log.d(TAG, "onPageSelected: finalPosition = $finalPosition")

        // Get the video URL for the selected position
        val url = userShortsList[position].images[0].url
        val videoUri = Uri.parse(url)
        val mediaItem = MediaItem.fromUri(videoUri)

        // Create media source
        val mediaSource = ProgressiveMediaSource.Factory(cacheDataSourceFactory)
            .createMediaSource(mediaItem)

        // Set the initial position for the ExoPlayer
        exoPlayer!!.setMediaSource(mediaSource, finalPosition)
        exoPlayer!!.prepare()
        exoPlayer!!.playWhenReady = true
        exoPlayer!!.play()
        exoPlayer!!.repeatMode = Player.REPEAT_MODE_ONE
        exoPlayer!!.addListener(playbackStateListener)
        exoPlayer!!.addListener(object : Player.Listener {
            @Deprecated("Deprecated in Java")
            override fun onPlayerStateChanged(
                playWhenReady: Boolean,
                playbackState: Int
            ) {
                if (playbackState == Player.STATE_READY && exoPlayer!!.duration != C.TIME_UNSET) {
//                                    shortsSeekBar.max = exoPlayer.duration.toInt()
//                                    shortsAdapter.setSeekBarMax(exoPlayer!!.currentPosition.toInt())
                    binding.shortSeekBar.max = exoPlayer!!.duration.toInt()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                error.printStackTrace()
                Toast.makeText(
                    this@UserProfileShortsPlayerActivity,
                    "Can't play this video",
                    Toast.LENGTH_SHORT
                ).show()

            }

            @Deprecated("Deprecated in Java")
            override fun onPositionDiscontinuity(reason: Int) {
                // Update SeekBar on position discontinuity
//                                updateSeekBar()
                updateSeekBar()

//                                shortsAdapter.setSeekBarProgress(exoPlayer!!.currentPosition.toLong())
            }
        })
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: UserProfileShortsOnClickEvent) {
        // Handle the event here
        Log.d(TAG, "onMessageEvent: user short entity: ${event.shortsEntity}")
        Log.d(TAG, "onMessageEvent: user short entity: ${event.shortsEntity?.size}")
    }

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

//    override fun onCommentsClick(position: Int) {
//        showBottomSheet()
//    }

    private lateinit var dialog: BottomSheetDialog

    //    private lateinit var dialog: BottomSheetDialog
    private lateinit var itemAdapter: CommentAdapter
    private lateinit var recyclerView: RecyclerView

    private fun showBottomSheet() {
        val list = ArrayList<com.uyscuti.social.network.api.response.comment.allcomments.Comment>()

        for (i in 1..3) {
            list.add(
                com.uyscuti.social.network.api.response.comment.allcomments.Comment(
                    __v = 0,
                    _id = i.toString(),
                    author = null,
                    content = "This is comment #$i",
                    createdAt = "",
                    isLiked = false,
                    likes = 0,
                    postId = "",
                    replyCount = 0,
                    updatedAt = "2025-05-27T00:00:00Z",
                )
            )
        }

        val dialogView = layoutInflater.inflate(R.layout.activity_bottom_sheet1, null)
        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme).apply {
            setContentView(dialogView)
        }

        val recyclerView: RecyclerView = dialogView.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = CommentAdapter(list)

        dialog.show()
    }




    override fun onSeekBarChanged(progress: Int) {

    }

    override fun onDownloadClick(url: String, fileLocation: String) {

    }

    override fun onShareClick(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onUploadCancelClick() {
        TODO("Not yet implemented")
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
        exoPlayer?.removeListener(playbackStateListener)

        EventBus.getDefault().unregister(this)
    }

    override fun onResume() {
        super.onResume()
        exoPlayer!!.play()
    }

    override fun onPause() {
        super.onPause()
        exoPlayer!!.pause()
    }

    private fun releasePlayer() {
        exoPlayer?.release()
        exoPlayer = null
    }

    private fun updateSeekBar() {
        exoPlayer?.let { player ->
            if (!isUserSeeking) {
                val currentPosition = player.currentPosition.toInt()
                binding.shortSeekBar.progress = currentPosition
            }
        }
    }

    private fun startPreLoadingService() {
        Log.d(SHORTS, "Preloading called")
        val preloadingServiceIntent = Intent(this, VideoPreLoadingService::class.java)
        preloadingServiceIntent.putStringArrayListExtra(Constants.VIDEO_LIST, shortsList)
        startService(preloadingServiceIntent)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun likeButtonClicked(event: ShortsLikeUnLikeButton2) {
//            Log.d("likeButtonClicked", "before onLikeUnLikeClick:")

        val button = event.likeUnLikeButton
        var shortOwnerId = event.shortsEntity.author.account._id
        val postId = event.shortsEntity._id
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
                TAG,
                "likeButtonClicked: event is liked: ${event.shortsEntity.isLiked}"
            )

        } else {
            button.setImageResource(R.drawable.favorite_svgrepo_com)
//                shortsViewModel.isLiked = event.shortsEntity.isLiked
            Log.d(
                TAG,
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun shortCommentButtonClicked(event: ShortsCommentButtonClicked) {
        Log.d(TAG, "shortCommentButtonClicked: ")
        val data = event.userShortEntity
        postId = data._id


        isFeedComment = false

        adapter = CommentsRecyclerViewAdapter(this, this@UserProfileShortsPlayerActivity)

        adapter?.setDefaultRecyclerView(this, R.id.recyclerView)
        binding.recyclerView.itemAnimator = null

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
                Log.d(TAG, "finished: page number")

//                Toast.makeText(requireContext(), "finish", Toast.LENGTH_SHORT).show()
            }
        })

        lifecycleScope.launch(Dispatchers.Main) {
            allShortComments(adapter!!.startPage)
//            allCommentReplies(adapter!!.startPage)
        }
        observeComments()

    }

    private fun observeComments() {
        commentsViewModel.commentsLiveData.observe(this) { it ->
            val commentsWithReplies = it.filter { it.replyCount > 0 }
            Log.d(TAG, "observeComments comments with replies size: ${commentsWithReplies.size}")
        }
    }

    private fun handleLikeClick(
        postId: String,
        likeCount: TextView,
        btnLike: ImageButton,
        shortsEntity: UserShortsEntity
    ) {

        Log.d(TAG, "handleLikeClick: before ${shortsViewModel.isLiked}")
        shortsViewModel.isLiked = !shortsViewModel.isLiked
        Log.d(TAG, "handleLikeClick: after ! ${shortsViewModel.isLiked}")

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
            shortsEntity.likes += shortsViewModel.totalLikes

            shortsViewModel.isLiked = true
            lifecycleScope.launch {
                likeUnLikeShort(postId)
            }
        } else {
//                shortsViewModel.totalLikes -= 1
//                likeCount.text = shortsViewModel.totalLikes.toString()
            shortsEntity.likes -= 1
            likeCount.text = shortsEntity.likes.toString()
            lifecycleScope.launch {
                likeUnLikeShort(postId)
            }
            btnLike.setImageResource(R.drawable.favorite_svgrepo_com)
            shortsEntity.isLiked = false
            shortsEntity.likes += shortsViewModel.totalLikes
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
                    TAG,
                    "likeUnLikeShort ${responseBody?.data!!.isLiked}"
                )
            } else {
                Log.d(TAG, "Error: ${response.message()}")
                runOnUiThread {
                    showToast(response.message())
                }
            }

        } catch (e: HttpException) {
            Log.d(TAG, "Http Exception ${e.message}")
            runOnUiThread {
                showToast("Failed to connect try again...")
            }
        } catch (e: IOException) {
            Log.d(TAG, "IOException ${e.message}")
            runOnUiThread {
                showToast("Failed to connect try again...")
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun favoriteButtonClicked(event: ShortsBookmarkButton2) {
//            Log.d("likeButtonClicked", "before onLikeUnLikeClick:")

        var button = event.favoriteButton
        var shortOwnerId = event.shortsEntity.author.account._id
        var postId = event.shortsEntity._id
//            Log.d("likeButtonClicked", "likeButtonClicked: Post id: $postId")

        shortsViewModel.isFavorite = event.shortsEntity.isBookmarked
//            shortsViewModel.totalLikes = event.shortsEntity.likes

//            Log.d("likeButtonClicked", "likeButtonClicked: event is liked: ${event.shortsEntity.isLiked}")
//        event.likeCount.text = event.shortsEntity.likes.toString()
//        likes = event.shortsEntity.likes

        if (shortsViewModel.isFavorite) {
            button.setImageResource(R.drawable.filled_favorite)
//                shortsViewModel.isLiked = event.shortsEntity.isLiked
            Log.d(
                TAG,
                "favoriteButtonClicked: event is liked: ${event.shortsEntity.isBookmarked}"
            )

        } else {
            button.setImageResource(R.drawable.favorite_svgrepo_com__1_)
//                shortsViewModel.isLiked = event.shortsEntity.isLiked
            Log.d(
                TAG,
                "favoriteButtonClicked: event is liked: ${event.shortsEntity.isBookmarked}"
            )

        }

        button.setOnClickListener {
//                Log.d("likeButtonClicked", "likeButtonClicked: button clicked")
//                Log.d("likeButtonClicked", "likeButtonClicked: click is liked ${shortsViewModel.isLiked}")
//                Log.d("likeButtonClicked", "likeButtonClicked:click event is liked ${event.shortsEntity.isLiked}")
            handleFavoriteClick(postId, button, event.shortsEntity)
        }

    }

    private fun handleFavoriteClick(
        postId: String,
        button: ImageView,
        shortsEntity: UserShortsEntity
    ) {

        shortsViewModel.isFavorite = !shortsViewModel.isFavorite
        if (!shortsEntity.isBookmarked) {

//                shortsViewModel.totalLikes += 1
//            shortsEntity.likes += 1
//            likeCount.text = shortsEntity.likes.toString()

            button.setImageResource(R.drawable.filled_favorite)
            YoYo.with(Techniques.Tada)
                .duration(700)
                .repeat(1)
                .playOn(button)

            shortsEntity.isBookmarked = true
//            shortsEntity.likes += shortsViewModel.totalLikes

            shortsViewModel.isFavorite = true
            lifecycleScope.launch {
                favoriteShort(postId)
            }
        } else {
//                shortsViewModel.totalLikes -= 1
//                likeCount.text = shortsViewModel.totalLikes.toString()
//            shortsEntity.likes -= 1
//            likeCount.text = shortsEntity.likes.toString()
            lifecycleScope.launch {
                favoriteShort(postId)
            }
            button.setImageResource(R.drawable.favorite_svgrepo_com__1_)
            shortsEntity.isBookmarked = false
//            shortsEntity.likes += shortsViewModel.totalLikes
            shortsViewModel.isFavorite = false
            YoYo.with(Techniques.Tada)
                .duration(700)
                .repeat(1)
                .playOn(button)
        }
    }

    private suspend fun favoriteShort(postId: String) {
        try {
            val response = retrofitIns.apiService.favoriteShort(postId)


            if (response.isSuccessful) {
                val responseBody = response.body()
                Log.d(
                    TAG,
                    "favoriteButtonClicked ${responseBody?.data!!.isBookmarked}"
                )
            } else {
                Log.d(TAG, "Error: ${response.message()}")
                runOnUiThread {
                    showToast(response.message())
                }
            }

        } catch (e: HttpException) {
            Log.d(TAG, "Http Exception ${e.message}")
            runOnUiThread {
                showToast("Failed to connect try again...")
            }
        } catch (e: IOException) {
            Log.d(TAG, "IOException ${e.message}")
            runOnUiThread {
                showToast("Failed to connect try again...")
            }
        }
    }

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onCommentsClick(position: Int, data: UserShortsEntity, isFeedComment: Boolean) {

        Log.d(TAG, "onCommentsClick: comments clicked")

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
//            Log.d(TAG, "onCreate: toggle layout")
            toggleMotionLayoutVisibility()

        }
    }

    @SuppressLint("SetTextI18n")
    @OptIn(UnstableApi::class)
    private fun toggleMotionLayoutVisibility() {
        val currentVisibility = binding.motionLayout.visibility

        if (currentVisibility == View.VISIBLE) {
            exoPlayer?.play()
            binding.motionLayout.visibility = View.GONE
            binding.VNLayout.visibility = View.GONE

            binding.replyToLayout.visibility = View.GONE
            binding.input.inputEditText.setText("")
            isReply = false
            commentsViewModel.resetLiveData()
            hideKeyboard(binding.input.inputEditText)
//            deleteRecording()
//            stopPlaying()
//            commentAudioStop()
//            stopWaveRunnable()
//            stopRecordWaveRunnable()
//            exoPlayer?.release()


        } else {
            var currentState = binding.motionLayout.currentState

            exoPlayer?.pause()
            binding.motionLayout.visibility = View.VISIBLE

            binding.motionLayout.transitionToStart()
        }
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

                    if (commentsWithReplies.isEmpty()) {
                        updateUI(true)
                    } else {
                        updateUI(false)
                    }
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

    override fun onSubmit(input: CharSequence?): Boolean {
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
                ShortCommentEntity(
                    postId,
                    input.toString(),
                    localUpdateId = localUpdateId,
                    isFeedComment = isFeedComment
                )
            shorts2CommentViewModel.insertComment(newCommentEntity)
            Log.d(TAG, "onSubmit: inserted comment $newCommentEntity")

            listOfReplies.add(comment)

            Log.d(TAG, "onSubmit: comment $comment")


            adapter!!.submitItem(comment, adapter!!.itemCount)
            updateUI(dataEmpty = false)
            if (!isFeedComment) {
                shortToComment = shortsViewModel.mutableShortsList.find { it._id == postId }


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
                    Log.d(TAG, "onSubmit: count after ${newShortToComment!!.comments}")

                    EventBus.getDefault().post(ShortAdapterNotifyDatasetChanged())
                }
            } else {

                val feedToComment = feedViewModel.getAllFeedData().find { it._id == postId }
                Log.d(TAG, "onSubmit: total before feed count is ${feedToComment?.comments}")
                val myFeedToComment = feedViewModel.getMyFeedData().find { it._id == postId }
                val favoriteFeedToComment =
                    feedViewModel.getAllFavoriteFeedData().find { it._id == postId }
                Log.d(TAG, "onSubmit: total before feed count is ${feedToComment?.comments}")

                if (myFeedToComment != null) {
//                    myFeedToComment!!.comments += 1

                    feedViewModel.getMyFeedData().forEach { feed ->
                        if (feed._id == postId) {
                            feed.comments = myFeedToComment!!.comments
                        }
                    }
//                        EventBus.getDefault().post(FeedAdapterNotifyDatasetChanged(adapter!!.itemCount))
                }
                if (favoriteFeedToComment != null) {
//                    favoriteFeedToComment!!.comments += 1

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
//                    feedToComment!!.comments += 1

                    feedViewModel.getAllFeedData().forEach { feed ->
                        if (feed._id == postId) {
                            feed.comments = feedToComment!!.comments
                        }
                    }
                    val feedToComment = feedViewModel.getAllFeedData().find { it._id == postId }
                    Log.d(TAG, "onSubmit: total after feed count is ${feedToComment?.comments}")

                    EventBus.getDefault().post(FeedAdapterNotifyDatasetChanged(adapter!!.itemCount))
                }
//                feedLiveDataViewModel.incrementCounter()
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

    private fun updateUI(dataEmpty: Boolean) {
        if (dataEmpty) {
            binding.recyclerView.visibility = View.GONE
            binding.placeholderLayout.visibility = View.VISIBLE
        } else {
            binding.placeholderLayout.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }

    private fun updateAdapter(
        data: Comment, position: Int
    ) {
        adapter?.updateItem(position, data)
    }

    override fun onAddEmoji() {

    }

    override fun onAddVoiceNote() {

    }

    override fun onAddGif() {

    }

    override fun onAddAttachments() {
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
    }

    override fun onReplyButtonClick(position: Int, data: Comment) {
    }

    override fun likeUnLikeComment(position: Int, data: Comment) {
    }

    override fun onTimerTick(duration: String) {
    }

}
