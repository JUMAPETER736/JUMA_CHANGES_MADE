package com.uyscuti.social.circuit.User_Interface.fragments.user_profile_fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
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
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.uyscuti.social.network.api.response.comment.allcomments.Comment
import com.uyscuti.social.circuit.FlashApplication
import com.uyscuti.social.circuit.MainActivity
import com.uyscuti.social.circuit.adapter.CommentAdapter
import com.uyscuti.social.circuit.User_Interface.fragments.OnClickListeners
import com.uyscuti.social.circuit.User_Interface.fragments.OnCommentsClickListener
import com.uyscuti.social.circuit.adapter.UserProfileShortsAdapter
import com.uyscuti.social.circuit.model.PlayPauseEvent
import com.uyscuti.social.circuit.model.ShortsBookmarkButton2
import com.uyscuti.social.circuit.model.ShortsFavoriteUnFavorite
import com.uyscuti.social.circuit.model.ShortsLikeUnLike2
import com.uyscuti.social.circuit.model.ShortsLikeUnLikeButton2
import com.uyscuti.social.circuit.model.ShortsViewModel
import com.uyscuti.social.circuit.model.UserProfileShortsViewModel
import com.uyscuti.social.circuit.service.VideoPreLoadingService
import com.uyscuti.social.circuit.User_Interface.fragments.SHORTS
import com.uyscuti.social.circuit.utils.Constants
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.FragmentUserProfileShortsPlayerBinding
import com.uyscuti.social.circuit.utils.AndroidUtil
import com.uyscuti.social.core.common.data.room.entity.UserShortsEntity
import com.uyscuti.social.core.models.BookmarkedShortsEntity
import com.uyscuti.social.network.api.response.getallshorts.Post
import com.uyscuti.social.network.api.response.getfavoriteshorts.BookmarkedPost
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UserProfileShortsPlayerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

private const val TAG = "UserProfileShortsPlayerFragment"

@UnstableApi
@AndroidEntryPoint
class UserProfileShortsPlayerFragment : Fragment(), OnCommentsClickListener,
    OnClickListeners {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var shortsAdapter: UserProfileShortsAdapter
    private lateinit var playerView: PlayerView

    //    private lateinit var shortSeekBar: SeekBar
    private lateinit var httpDataSourceFactory: HttpDataSource.Factory
    private lateinit var defaultDataSourceFactory: DefaultDataSourceFactory
    private lateinit var cacheDataSourceFactory: CacheDataSource.Factory

    private val simpleCache: SimpleCache = FlashApplication.cache
    private val playbackStateListener: Player.Listener = playbackStateListener()

    private var exoPlayer: ExoPlayer? = null

    private var videoShorts = ArrayList<UserShortsEntity>()
    private var shortsList = ArrayList<String>()
    var lastPosition = 0
    private var isUserSeeking = false

    @Inject
    lateinit var retrofitIns: RetrofitInstance

//    private var userShortsList: List<UserShortsEntity> = emptyList()

    // Global variable for clickedShort
    private var clickedShort: UserShortsEntity? = null

    private var currentPosition: Int = -1

    private var isPlaying = false

    //    private var fromUserShortsFragment by Delegates.notNull<Boolean>()
    private var favoriteFragment = false

    //    private val shortsViewModel: ShortsViewModel by activityViewModels()
    private val viewModel: UserProfileShortsViewModel by activityViewModels()
    private val shortsViewModel: ShortsViewModel by activityViewModels()

    private lateinit var binding: FragmentUserProfileShortsPlayerBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @OptIn(UnstableApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        binding = FragmentUserProfileShortsPlayerBinding.inflate(inflater, container, false)
        activity?.window?.statusBarColor = ContextCompat.getColor(requireContext(), R.color.black)

        // Set the navigation bar color dynamically
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity?.window?.navigationBarColor =
                ContextCompat.getColor(requireContext(), R.color.black)
        }

        playerView = binding.playerView
        (activity as? MainActivity)?.hideAppBar()


        EventBus.getDefault().register(this)

        clickedShort = arguments?.getSerializable(CLICKED_SHORT) as UserShortsEntity
        favoriteFragment = arguments?.getBoolean(FROM_FAVORITE_FRAGMENT)!!

        Log.d(TAG, "onCreateView: from favorite fragment: $favoriteFragment")

        if (favoriteFragment) {
            currentPosition = viewModel.mutableFavoriteShortsList.indexOf(clickedShort)

        } else {
            currentPosition = viewModel.mutableShortsList.indexOf(clickedShort)

        }



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
        exoPlayer = ExoPlayer.Builder(requireActivity())
            .setMediaSourceFactory(mediaSourceFactory)
            .build()

        shortsAdapter = UserProfileShortsAdapter(
            requireActivity() as OnCommentsClickListener,
            this@UserProfileShortsPlayerFragment,
        )

        if (favoriteFragment) {

            shortsAdapter.addData(viewModel.mutableFavoriteShortsList)
            for (userShort in viewModel.mutableFavoriteShortsList) {

                videoShorts.add(userShort)

            }
        } else {

            shortsAdapter.addData(viewModel.mutableShortsList)
            for (userShort in viewModel.mutableShortsList) {

                videoShorts.add(userShort)

            }
        }





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
                        if (position > lastPosition) {

                            loadMoreVideosIfNeeded(position)

                        }

                        lastPosition = position

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

                    }

                    override fun onPageScrollStateChanged(state: Int) {
                        super.onPageScrollStateChanged(state)


                        // Check if the scroll state is idle
                        if (state == ViewPager.SCROLL_STATE_SETTLING) {

                            // The scroll state is idle, play the video at the updated position
                            playVideoAtPosition(currentPosition)
                        }
                    }

                })
            }

        }


        // Handle the back button press
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // Perform any additional checks if needed before popping the back stack
            // For example, you might want to check if the fragment is in a certain state
            // before allowing the back press to pop the fragment.

            // If all conditions are met, pop the back stack
            parentFragmentManager.popBackStack()
        }

        return binding.root

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun pausePlayEvent(event: PlayPauseEvent) {
//        Log.d(TAG, "pausePlayEvent ")
        if (exoPlayer?.isPlaying == true) {
            pauseVideo()
        } else {
            playVideo()
        }
    }

    fun loadMoreVideosIfNeeded(position: Int) {
        // Check if the position meets the condition
        if (position >= 5 && (position - 5) % 5 == 0) {
            // Calculate the parameter to pass to loadMoreVideos based on the position
            val loadMoreValue = 2 + (position - 5) / 5

            loadMoreVideos(loadMoreValue)


        }
    }

    private fun loadMoreVideos(pageNumber: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            loadMoreShorts(pageNumber)
            lifecycleScope.launch(Dispatchers.Main) {
            }
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

    private fun serverResponseToBookmarkedPost(serverResponse: List<BookmarkedPost>): List<BookmarkedShortsEntity> {
        return serverResponse.map { serverResponseItem ->
            BookmarkedShortsEntity(
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
        if (favoriteFragment) {
            try {

                val response = retrofitIns.apiService.getFavoriteShorts(currentPage.toString())


                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val shortsEntity =
                        responseBody?.data?.bookmarkedPosts?.let { serverResponseToBookmarkedPost(it) }

                    // Now, insert yourEntity into the Room database
                    if (shortsEntity != null) {


                        lifecycleScope.launch(Dispatchers.IO) {

                            val convertShorts = bookmarkedToUserShortsEntity(shortsEntity)

                            for (entity in shortsEntity) {
                                // Access the list of images for each entity
                                val images = entity.images

                                viewModel.mutableFavoriteShortsList =
                                    ((viewModel.mutableFavoriteShortsList + entity) as MutableList<UserShortsEntity>)

                                // Iterate through the list of images
                                for (image in images) {
                                    // Access individual image properties or perform any desired actions
                                    val imageUrl = image.url
                                    Log.d(SHORTS, "imageUrl - $imageUrl")
                                    shortsList.add(imageUrl)

                                }
                            }

                            startPreLoadingService()
                            withContext(Dispatchers.Main) {
                                shortsAdapter.addData(convertShorts)
                            }

                        }

                    } else {
                        Log.d(SHORTS, "failed to add shorts to local database")
                    }


                } else {
                    Log.d("AllShorts", "Error: ${response.message()}")
                    requireActivity().runOnUiThread {
                        AndroidUtil.showToast(requireActivity(), response.message())
                    }
                }

            } catch (e: HttpException) {
                Log.d("AllShorts", "Http Exception ${e.message}")
                requireActivity().runOnUiThread {
                    AndroidUtil.showToast(requireContext(), "Failed to connect try again...")
                }
            } catch (e: IOException) {
                Log.d("AllShorts", "IOException ${e.message}")
                requireActivity().runOnUiThread {
                    AndroidUtil.showToast(requireActivity(), "Failed to connect try again...")
                }
            }
        } else {
            try {
//                Log.d(TAG, "onCreateView: current my shorts fragment")

                val response = retrofitIns.apiService.myShorts(currentPage.toString())
//            retrofitIns.apiService.getShortsByUsernameWithPage("", "")

                if (response.isSuccessful) {
                    val responseBody = response.body()

                    val shortsEntity =
                        responseBody?.data?.posts?.let { serverResponseToUserEntity(it) }

                    if (shortsEntity != null) {


                        lifecycleScope.launch(Dispatchers.IO) {

                            for (entity in shortsEntity) {
                                // Access the list of images for each entity
                                val images = entity.images

                                viewModel.mutableShortsList =
                                    (viewModel.mutableShortsList + entity).toMutableList()

                                // Iterate through the list of images
                                for (image in images) {
                                    // Access individual image properties or perform any desired actions
                                    val imageUrl = image.url
                                    Log.d(SHORTS, "imageUrl - $imageUrl")
                                    shortsList.add(imageUrl)

                                }
                            }

                            startPreLoadingService()
                            withContext(Dispatchers.Main) {
                                shortsAdapter.addData(shortsEntity)
                            }

                        }

                    } else {
                        Log.d(SHORTS, "failed to add shorts to local database")
                    }


                } else {
                    Log.d("AllShorts", "Error: ${response.message()}")
                    requireActivity().runOnUiThread {
                        AndroidUtil.showToast(requireActivity(), response.message())
                    }
                }

            } catch (e: HttpException) {
                Log.d("AllShorts", "Http Exception ${e.message}")
                requireActivity().runOnUiThread {
                    AndroidUtil.showToast(requireContext(), "Failed to connect try again...")
                }
            } catch (e: IOException) {
                Log.d("AllShorts", "IOException ${e.message}")
                requireActivity().runOnUiThread {
                    AndroidUtil.showToast(requireActivity(), "Failed to connect try again...")
                }
            }
        }
    }

    private fun bookmarkedToUserShortsEntity(serverResponse: List<BookmarkedShortsEntity>): List<UserShortsEntity> {
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

    @OptIn(UnstableApi::class)
    private fun playVideoAtPosition(position: Int) {
        var url = ""


        val relativePosition = currentPosition - position

        val finalPosition = relativePosition.toLong() * C.TIME_UNSET

        if (favoriteFragment) {
            url = viewModel.mutableFavoriteShortsList[position].images[0].url
        } else {
            url = viewModel.mutableShortsList[position].images[0].url
        }
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

                updateSeekBar()


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

    private fun updateStatusBar() {
        val decor: View? = activity?.window?.decorView

        decor?.systemUiVisibility = 0

    }

    override fun onResume() {
        super.onResume()
        updateStatusBar()
        exoPlayer!!.play()
    }

    private fun updateSeekBar() {
        exoPlayer?.let { player ->
            if (!isUserSeeking) {
                val currentPosition = player.currentPosition.toInt()
                binding.shortSeekBar.progress = currentPosition
            }
        }
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

                    updateSeekBar()
                    delay(50) // Update seek bar every second (adjust as needed)
                }
            }
        }

        private fun stopUpdatingSeekBar() {
            updateSeekBarJob?.cancel()
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun likeButtonClicked(event: ShortsLikeUnLikeButton2) {
        var button = event.likeUnLikeButton
        var shortOwnerId = event.shortsEntity.author.account._id
        var postId = event.shortsEntity._id

        event.likeCount.text = event.shortsEntity.likes.toString()


        if (event.shortsEntity.isLiked) {
            button.setImageResource(R.drawable.filled_favorite_like)
            Log.d(TAG, "likeButtonClicked: is liked count ${event.shortsEntity.likes}")
        } else {
            button.setImageResource(R.drawable.favorite_svgrepo_com)
            Log.d(TAG, "likeButtonClicked: is liked count ${event.shortsEntity.likes}")
        }
        button.setOnClickListener {
            handleLikeClick(postId, event.likeCount, button, event.shortsEntity)
        }

    }


    private fun handleLikeClick(
        postId: String,
        likeCount: TextView,
        btnLike: ImageButton,
        shortsEntity: UserShortsEntity
    ) {
        EventBus.getDefault().post(ShortsLikeUnLike2(postId))

        val isNowLiked = !shortsEntity.isLiked
        shortsEntity.isLiked = isNowLiked

        // Update like count safely
        if (isNowLiked) {
            shortsEntity.likes += 1
            btnLike.setImageResource(R.drawable.filled_favorite_like)
        } else {
            if (shortsEntity.likes > 0) shortsEntity.likes -= 1
            btnLike.setImageResource(R.drawable.favorite_svgrepo_com)
        }

        // Update UI on main thread
        requireActivity().runOnUiThread {
            likeCount.text = shortsEntity.likes.toString()
        }

        // Apply animation
        YoYo.with(Techniques.Tada)
            .duration(700)
            .repeat(1)
            .playOn(btnLike)

        // Update all references
        val userShorts = viewModel.mutableShortsList.find { it._id == shortsEntity._id }
        val shorts = shortsViewModel.mutableShortsList.find { it._id == postId }
        val myFavoriteShorts = viewModel.mutableFavoriteShortsList.find { it._id == postId }

        userShorts?.apply {
            isLiked = isNowLiked
            if (favoriteFragment && isNowLiked) likes += 1
            if (favoriteFragment && !isNowLiked && likes > 0) likes -= 1
        }

        shorts?.apply {
            isLiked = isNowLiked
            if (isNowLiked) likes += 1 else if (likes > 0) likes -= 1
        }

        myFavoriteShorts?.apply {
            isLiked = isNowLiked
            if (!favoriteFragment && isNowLiked) likes += 1
            if (!favoriteFragment && !isNowLiked && likes > 0) likes -= 1
        }
    }



    private suspend fun likeUnLikeShort(shortOwnerId: String) {
        try {
            val response = retrofitIns.apiService.likeUnLikeShort(shortOwnerId)
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

        var button = event.favoriteButton
        var shortOwnerId = event.shortsEntity.author.account._id
        var postId = event.shortsEntity._id

        viewModel.isFavorite = event.shortsEntity.isBookmarked
        Log.d(TAG, "inside favorite: entity ${event.shortsEntity}")

        if (viewModel.isFavorite) {
            button.setImageResource(R.drawable.filled_favorite)
        } else {
            button.setImageResource(R.drawable.favorite_svgrepo_com__1_)
        }

        button.setOnClickListener {
            handleFavoriteClick(postId, button, event.shortsEntity)
        }

    }

    @SuppressLint("NotifyDataSetChanged")

    private fun handleFavoriteClick(
        postId: String,
        button: ImageView,
        shortsEntity: UserShortsEntity
    ) {
        // Toggle favorite status
        val isNowFavorite = !shortsEntity.isBookmarked
        shortsEntity.isBookmarked = isNowFavorite
        viewModel.isFavorite = isNowFavorite

        // Post event
        EventBus.getDefault().post(ShortsFavoriteUnFavorite(postId))

        // Update UI
        button.setImageResource(
            if (isNowFavorite) R.drawable.filled_favorite
            else R.drawable.favorite_svgrepo_com__1_
        )

        // Animate
        YoYo.with(Techniques.Tada)
            .duration(700)
            .repeat(1)
            .playOn(button)

        // Update all related lists
        val myShorts = viewModel.mutableShortsList.find { it._id == postId }
        val globalShorts = shortsViewModel.mutableShortsList.find { it._id == postId }
        val favShort = viewModel.mutableFavoriteShortsList.find { it._id == postId }

        myShorts?.isBookmarked = isNowFavorite
        globalShorts?.isBookmarked = isNowFavorite

        if (isNowFavorite) {
            if (favShort == null) {
                // Only add if not already in list
                viewModel.mutableFavoriteShortsList.add(0, shortsEntity)
                Log.d(TAG, "Added to favorites: ${shortsEntity._id}")
            }
        } else {
            // Only remove if not in favorite fragment
            if (!favoriteFragment) {
                viewModel.mutableFavoriteShortsList.removeIf { it._id == postId }
                Log.d(TAG, "Removed from favorites: $postId")
            }

            // Add to removal list if it exists in favorites
            favShort?.let {
                viewModel.shortsToRemove.add(it)
                Log.d(TAG, "Marked for removal: ${it._id}")
            }
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

    private fun startPreLoadingService() {
        Log.d(SHORTS, "Preloading called")
        val preloadingServiceIntent = Intent(requireContext(), VideoPreLoadingService::class.java)
        preloadingServiceIntent.putStringArrayListExtra(Constants.VIDEO_LIST, shortsList)
        requireActivity().startService(preloadingServiceIntent)
    }

    companion object {

        const val SHORTS_LIST = "shorts_list"
        const val CLICKED_SHORT = "clicked_short"
        const val FROM_FAVORITE_FRAGMENT = "favorite_short"
//        var isFromFavorite: Boolean = false
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment UserProfileShortsPlayerFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UserProfileShortsPlayerFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        fun newInstance(
            userShortsList: ArrayList<UserShortsEntity>,
            clickedShort: UserShortsEntity,
            isFromFavorite: Boolean
        ): UserProfileShortsPlayerFragment {
            val fragment = UserProfileShortsPlayerFragment()
            val bundle = Bundle().apply {
                putSerializable(SHORTS_LIST, userShortsList)
                putSerializable(CLICKED_SHORT, clickedShort)
                putBoolean(FROM_FAVORITE_FRAGMENT, isFromFavorite)
            }
            fragment.arguments = bundle
            return fragment
        }
    }



    private lateinit var dialog: BottomSheetDialog


    private lateinit var itemAdapter: CommentAdapter
    private lateinit var recyclerView: RecyclerView

    private fun showBottomSheet() {
        val list = ArrayList<Comment>()

        for (i in 1..3) {
            list.add(
                Comment(
                    __v = 0,
                    _id = "id_$i",
                    author = null,
                    content = "This is comment $i",
                    contentType = "",
                    createdAt = "2025-05-27T00:00:00Z",
                    isLiked = false,
                    likes = 0,
                    postId = "post_$i",
                    updatedAt = "2025-05-27T00:00:00Z",
                    replyCount = 0
                )
            )
        }

        val dialogView = layoutInflater.inflate(R.layout.activity_bottom_sheet1, null)
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        dialog.setContentView(dialogView)

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerView)
        val itemAdapter = CommentAdapter(list)

        recyclerView.adapter = itemAdapter
        dialog.show()
    }


    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
        exoPlayer?.removeListener(playbackStateListener)

        EventBus.getDefault().unregister(this)
    }

    override fun onPause() {
        super.onPause()
        exoPlayer!!.pause()
    }

    private fun releasePlayer() {
        exoPlayer?.release()
        exoPlayer = null
    }

    override fun onSeekBarChanged(progress: Int) {

    }

    override fun onDownloadClick(url: String, fileLocation: String) {

    }

    override fun onShareClick(position: Int) {

    }

    override fun onUploadCancelClick() {
        TODO("Not yet implemented")
    }

    fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onCommentsClick(
        position: Int,
        data: UserShortsEntity,
        isFeedComment: Boolean
    ) {

    }

    fun onImageClick() {
        TODO("Not yet implemented")
    }
}