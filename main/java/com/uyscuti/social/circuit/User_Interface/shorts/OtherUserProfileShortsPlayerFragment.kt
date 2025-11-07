package com.uyscuti.social.circuit.User_Interface.shorts

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
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
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.uyscuti.social.circuit.FlashApplication
import com.uyscuti.social.circuit.User_Interface.fragments.OnClickListeners
import com.uyscuti.social.circuit.User_Interface.fragments.OnCommentsClickListener
import com.uyscuti.social.circuit.adapter.UserProfileShortsAdapter
import com.uyscuti.social.circuit.model.PlayPauseEvent
import com.uyscuti.social.circuit.model.ShortsBookmarkButton2
import com.uyscuti.social.circuit.model.ShortsLikeUnLikeButton2
import com.uyscuti.social.circuit.model.ShortsViewModel
import com.uyscuti.social.circuit.service.VideoPreLoadingService
import com.uyscuti.social.circuit.User_Interface.fragments.SHORTS
import com.uyscuti.social.circuit.utils.Constants
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.FragmentOtherUserProfileShortsPlayerBinding
import com.uyscuti.social.core.common.data.room.entity.UserShortsEntity
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [OtherUserProfileShortsPlayerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

private const val TAG = "OtherUserProfileShortsPlayerFragment"

@UnstableApi
class OtherUserProfileShortsPlayerFragment : Fragment(), OnCommentsClickListener,
    OnClickListeners {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    private lateinit var shortsAdapter: UserProfileShortsAdapter
//    private late init var playerView: PlayerView

    //    private late init var shortSeekBar: SeekBar
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


    private var userShortsList: List<UserShortsEntity> = emptyList()

    // Global variable for clickedShort
    private var clickedShort: UserShortsEntity? = null

    private var currentPosition: Int = -1

    private var isPlaying = false

    private lateinit var gestureDetector: GestureDetectorCompat

    private val shortsViewModel: ShortsViewModel by viewModels()

    @Inject
    lateinit var retrofitIns: RetrofitInstance


    private lateinit var binding: FragmentOtherUserProfileShortsPlayerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            userShortsList =
                (it.getSerializable(UserProfileShortsPlayerActivity.SHORTS_LIST) as ArrayList<UserShortsEntity>?)!!
            clickedShort =
                it.getSerializable(UserProfileShortsPlayerActivity.CLICKED_SHORT) as UserShortsEntity
        }
        EventBus.getDefault().register(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentOtherUserProfileShortsPlayerBinding.inflate(
            layoutInflater
        )
//        playerView = findViewById(R.id.playerView)



//        userShortsList = intent.getSerializableExtra(UserProfileShortsPlayerActivity.SHORTS_LIST) as ArrayList<UserShortsEntity>
//        Log.d(TAG, "onCreate: user shorts list $userShortsList")
//        Log.d(TAG, "onCreate: user shorts list size ${userShortsList.size}")
//        val storedShortsList = intent.getSerializableExtra(SHORTS_LIST) as UserShortsEntity
//        clickedShort = intent.getSerializableExtra(UserProfileShortsPlayerActivity.CLICKED_SHORT) as UserShortsEntity


        if(userShortsList.isNotEmpty()) {
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
                requireContext(), httpDataSourceFactory
            )


            cacheDataSourceFactory = CacheDataSource.Factory()
                .setCache(simpleCache)
                .setUpstreamDataSourceFactory(httpDataSourceFactory)
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
            val mediaSourceFactory: MediaSource.Factory =
                DefaultMediaSourceFactory(requireContext())
                    .setDataSourceFactory(cacheDataSourceFactory)
            exoPlayer = ExoPlayer.Builder(requireContext())
                .setMediaSourceFactory(mediaSourceFactory)
                .build()

            shortsAdapter = UserProfileShortsAdapter(
                this@OtherUserProfileShortsPlayerFragment,
                this@OtherUserProfileShortsPlayerFragment,
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
        }else {
            Log.d(TAG, "onCreateView: list empty")
        }
       

        Log.d("OtherUserProfileShortsPlayerFragment", "onCreateView: ")
        return binding.root
    }

    private fun serverResponseToUserEntity(serverResponse: List<com.uyscuti.social.network.api.response.getallshorts.Post>): List<UserShortsEntity> {
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
                requireActivity().runOnUiThread {
                    showToast( response.message())
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
                    requireContext(),
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

    override fun onResume() {
        super.onResume()
        Log.d("onResume", "onResume: ")
        if(shortsList.isNotEmpty()) {
            exoPlayer!!.play()
        }

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
        val preloadingServiceIntent = Intent(requireContext(), VideoPreLoadingService::class.java)
        preloadingServiceIntent.putStringArrayListExtra(Constants.VIDEO_LIST, shortsList)
        requireActivity().startService(preloadingServiceIntent)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun likeButtonClicked(event: ShortsLikeUnLikeButton2) {
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
                requireActivity().runOnUiThread {
                    showToast(response.message())
                }
            }

        } catch (e: HttpException) {
            Log.d(TAG, "Http Exception ${e.message}")
            requireActivity().runOnUiThread {
                showToast("Failed to connect try again...")
            }
        } catch (e: IOException) {
            Log.d(TAG, "IOException ${e.message}")
            requireActivity().runOnUiThread {
                showToast("Failed to connect try again...")
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun favoriteButtonClicked(event: ShortsBookmarkButton2) {
//            Log.d("likeButtonClicked", "before onLikeUnLikeClick:")

        val button = event.favoriteButton
        var shortOwnerId = event.shortsEntity.author.account._id
        val postId = event.shortsEntity._id
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

//            Log.d("likeUnLikeShort", "likeUnLikeShort: response: $response")
//            Log.d("likeUnLikeShort", "likeUnLikeShort: response body: ${response.body()}")
//            Log.d("likeUnLikeShort", "likeUnLikeShort: response error body: ${response.errorBody()}")
            if (response.isSuccessful) {
                val responseBody = response.body()
                Log.d(
                    TAG,
                    "favoriteButtonClicked ${responseBody?.data!!.isBookmarked}"
                )
            } else {
                Log.d(TAG, "Error: ${response.message()}")
                requireActivity().runOnUiThread {
                    showToast(response.message())
                }
            }

        } catch (e: HttpException) {
            Log.d(TAG, "Http Exception ${e.message}")
            requireActivity().runOnUiThread {
                showToast("Failed to connect try again...")
            }
        } catch (e: IOException) {
            Log.d(TAG, "IOException ${e.message}")
            requireActivity().runOnUiThread {
                showToast("Failed to connect try again...")
            }
        }
    }

    fun showToast(message: String) {

        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val SHORTS_LIST = "shorts_list"
        const val CLICKED_SHORT = "clicked_short"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment OtherUserProfileShortsPlayerFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            OtherUserProfileShortsPlayerFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onCommentsClick(position: Int, data: UserShortsEntity) {

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

    fun onImageClick() {
        TODO("Not yet implemented")
    }
}