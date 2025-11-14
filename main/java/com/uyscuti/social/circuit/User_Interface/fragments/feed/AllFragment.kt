package com.uyscuti.social.circuit.User_Interface.fragments.feed

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionInflater
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.uyscuti.social.circuit.adapter.feed.FeedAdapter
import com.uyscuti.social.circuit.adapter.feed.OnFeedClickListener
import com.uyscuti.social.circuit.adapter.feed.postFeedActivity.PostFeedActivity
import com.uyscuti.social.circuit.eventbus.AllFeedUpdateLike
import com.uyscuti.social.circuit.eventbus.FeedFavoriteClick
import com.uyscuti.social.circuit.eventbus.FeedFavoriteFollowUpdate
import com.uyscuti.social.circuit.eventbus.FeedLikeClick
import com.uyscuti.social.circuit.eventbus.FeedUploadResponseEvent
import com.uyscuti.social.circuit.eventbus.FromFavoriteFragmentFeedFavoriteClick
import com.uyscuti.social.circuit.eventbus.FromFavoriteFragmentFeedLikeClick
import com.uyscuti.social.circuit.eventbus.FromOtherUsersFeedFavoriteClick
import com.uyscuti.social.circuit.eventbus.HideFeedFloatingActionButton
import com.uyscuti.social.circuit.eventbus.ShowFeedFloatingActionButton
import com.uyscuti.social.circuit.interfaces.feedinterfaces.FeedTextViewFragmentInterface
import com.uyscuti.social.circuit.interfaces.feedinterfaces.ToggleFeedFloatingActionButton
import com.uyscuti.social.circuit.model.FeedAdapterNotifyDatasetChanged
import com.uyscuti.social.circuit.model.FeedCommentClicked
import com.uyscuti.social.circuit.model.FeedUploadProgress
import com.uyscuti.social.circuit.model.HideAppBar
import com.uyscuti.social.circuit.model.HideBottomNav
import com.uyscuti.social.circuit.model.ShowAppBar
import com.uyscuti.social.circuit.model.ShowBottomNav
import com.uyscuti.social.circuit.model.feed.SetAllFragmentScrollPosition
import com.uyscuti.social.circuit.User_Interface.feedactivities.FeedVideoViewFragment
import com.uyscuti.social.circuit.User_Interface.feedactivities.ReportNotificationActivity2
import com.uyscuti.social.circuit.User_Interface.fragments.FeedFragment
import com.uyscut.flashdesign.ui.fragments.feed.feedRepostViewFragments.FeedRepostAudioViewFragment
import com.uyscuti.social.circuit.User_Interface.fragments.feed.feedRepostViewFragments.FeedRepostDocFragment
import com.uyscuti.social.circuit.User_Interface.fragments.feed.feedRepostViewFragments.FeedRepostImageFragment
import com.uyscuti.social.circuit.User_Interface.fragments.feed.feedRepostViewFragments.FeedRepostTextFragment
import com.uyscuti.social.circuit.User_Interface.fragments.feed.feedRepostViewFragments.FeedRepostVideoViewFragment
import com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.FeedAudioViewFragment
import com.uyscuti.social.circuit.User_Interface.fragments.feed.feedviewfragments.FeedDocumentViewFragment
import com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.FeedImageViewFragment
import com.uyscuti.social.circuit.User_Interface.fragments.feed.feedviewfragments.FeedMixedFilesViewFragment
import com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.FeedMultipleImageViewFragment
import com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.FeedTextViewFragment
import com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.Fragment_Original_Post_With_Repost_Inside
import com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.feedRepost.Tapped_Files_In_The_Container_View_Fragment

import com.uyscuti.social.circuit.utils.removeDuplicateFollowers
import com.uyscuti.social.circuit.viewmodels.FeedShortsViewModel
import com.uyscuti.social.circuit.viewmodels.FollowUnfollowViewModel
import com.uyscuti.social.circuit.viewmodels.GetShortsByUsernameViewModel
import com.uyscuti.social.circuit.viewmodels.feed.FeedUploadViewModel
import com.uyscuti.social.circuit.viewmodels.feed.GetFeedViewModel
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.adapter.feed.FeedPaginatedAdapter
import com.uyscuti.social.circuit.feed.FeedUploadRepository
import com.uyscuti.social.circuit.model.feed.multiple_files.MixedFeedUploadDataClass
import com.uyscuti.social.circuit.User_Interface.fragments.feed.feedRepostViewFragmentsimport.FeedRepostMultipleImageFragment
import com.uyscuti.social.circuit.User_Interface.fragments.feed.feedviewfragments.editRepost.Fragment_Edit_Post_To_Repost
import com.uyscuti.social.core.common.data.room.entity.FollowUnFollowEntity
import com.uyscuti.social.core.common.data.room.entity.ShortsEntityFollowList
import com.uyscuti.social.network.api.response.posts.OriginalPost
import com.uyscuti.social.network.api.response.posts.Post
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
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

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"



private const val TAG = "AllFragment"
private const val REQUEST_REPOST_FEED_ACTIVITY = 1020
private val PRELOAD_THRESHOLD = 10
private const val INITIAL_PAGES_TO_LOAD = 10
private const val BATCH_SIZE = 3

@AndroidEntryPoint
class AllFragment : Fragment(), OnFeedClickListener, FeedTextViewFragmentInterface,
    ToggleFeedFloatingActionButton {



    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AllFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)

                }
            }
    }



    private var param1: String? = null
    private var param2: String? = null
    private var parentFragment: FeedFragment? = null // Reference to parent fragment

    private lateinit var fileFloatingActionButton: FloatingActionButton
    private lateinit var vnFloatingActionButton: FloatingActionButton
    private lateinit var fabAction: FloatingActionButton

    var bitmap: Bitmap? = null
    private val getFeedViewModel: GetFeedViewModel by activityViewModels()
    private val feedUploadViewModel: FeedUploadViewModel by activityViewModels()
    private val followUnFollowViewModel: FollowUnfollowViewModel by viewModels()
    private val shortsViewModel: GetShortsByUsernameViewModel by activityViewModels()
    private lateinit var myFeedAdapter: FeedAdapter

    private lateinit var feedListView: RecyclerView
    private lateinit var allFeedAdapter: FeedAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var frameLayout: FrameLayout
    private val requestCode = 2024
    private val PICK_VIDEO_REQUEST = "video/*"
    private val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 12
    private var isLoadingNextPage = false

    @Inject
    lateinit var retrofitInstance: RetrofitInstance
    private var feedTextViewFragment: FeedTextViewFragment? = null
    private var feedImageViewFragment: FeedImageViewFragment? = null
    private var feedVideoViewFragment: FeedVideoViewFragment? = null
    private var feedMixedFilesViewFragment: FeedMixedFilesViewFragment? = null
    private var feedDocsViewFragment: FeedDocumentViewFragment? = null
    private var feedAudioViewFragment: FeedAudioViewFragment? = null
    private var fragmentOriginalPostWithRepostInside: Fragment_Original_Post_With_Repost_Inside? = null
    private var feedMultipleImageViewFragment: FeedMultipleImageViewFragment? = null
    private var feedRepostDocFragment: FeedRepostDocFragment? = null
    private var feedRepostTextFragment: FeedRepostTextFragment? = null
    private var feedRepostVideoViewFragment: FeedRepostVideoViewFragment? = null
    private var feedRepostAudioViewFragment: FeedRepostAudioViewFragment? = null
    private var feedRepostImageFragment: FeedRepostImageFragment? = null
    private val feedShortsSharedViewModel: FeedShortsViewModel by activityViewModels()
    private var currentAdapterPosition = -1
    private lateinit var feedUploadRepository: FeedUploadRepository
    private var positionFromShorts: SetAllFragmentScrollPosition? = null
    private var feedRepostMultipleImageFragment: FeedRepostMultipleImageFragment? = null
    private var hasNotifiedDatasetChanged = false
    private var isFragmentOpen = false


    private val loadedPages = mutableSetOf<Int>()
    private val loadingPages = mutableSetOf<Int>()
    private var hasMorePages = true
    private val pageLock = Object()
    private var isInitialLoadComplete = false
    private val preloadScope = CoroutineScope(Dispatchers.IO + SupervisorJob())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = TransitionInflater.from(requireContext())
        exitTransition = inflater.inflateTransition(R.transition.feed_fragment_fade)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        EventBus.getDefault().register(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var isScrollingDown = false
        loadedPages.clear()
        loadingPages.clear()
        hasMorePages = true
        isInitialLoadComplete = false

        feedUploadRepository = FeedUploadRepository()
        feedListView = view.findViewById(R.id.rv)
        progressBar = view.findViewById(R.id.progressBar)
        frameLayout = view.findViewById(R.id.feed_text_view_fragment)

        allFeedAdapter = FeedAdapter(requireActivity(), this@AllFragment)
        feedListView.adapter = allFeedAdapter
        feedListView.layoutManager = LinearLayoutManager(requireContext())
        feedListView.itemAnimator = null

        // Show loading indicator
        progressBar.visibility = View.VISIBLE

        if (getFeedViewModel.getAllFeedData().isEmpty()) {
            // Aggressive initial load
            loadInitialBatch()
        } else {
            allFeedAdapter.submitItems(getFeedViewModel.getAllFeedData())
            progressBar.visibility = View.GONE
            isInitialLoadComplete = true
            // Continue background loading
            continueBackgroundLoading()
        }

        // Setup pagination listener
        allFeedAdapter.setOnPaginationListener(object : FeedPaginatedAdapter.OnPaginationListener {
            override fun onCurrentPage(page: Int) {
                Log.d(TAG, "onCurrentPage: $page")
                // Trigger preload of next batch
                preloadNextBatch(page)
            }

            override fun onNextPage(page: Int) {
                Log.d(TAG, "onNextPage: $page")
                // This should rarely be called now since we preload
                ensurePageLoaded(page)
            }

            override fun onFinish() {
                Log.d(TAG, "Pagination finished")
            }
        })

        // Optimized scroll listener
        feedListView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
                val adapter = recyclerView.adapter ?: return

                if (adapter.itemCount == 0) return

                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                val totalItemCount = adapter.itemCount

                // Handle FAB visibility
                if (dy > 5) {
                    EventBus.getDefault().post(HideFeedFloatingActionButton())
                    EventBus.getDefault().post(HideBottomNav())
                } else if (dy < -5) {
                    EventBus.getDefault().post(ShowFeedFloatingActionButton(false))
                    EventBus.getDefault().post(ShowBottomNav(false))
                }

                // Save scroll position
                getFeedViewModel.allFeedDataLastViewPosition = firstVisibleItemPosition + 1

                // Aggressive preloading when scrolling
                if (isInitialLoadComplete && dy > 0) {
                    val currentPage = lastVisibleItemPosition / 10
                    preloadNextBatch(currentPage + 1)
                }
            }
        })

        // Observe feed data
        lifecycleScope.launch(Dispatchers.Main) {
            getFeedViewModel.isFeedDataAvailable.observe(viewLifecycleOwner) { isDataAvailable ->
                if (isDataAvailable) {
                    allFeedAdapter.submitItems(getFeedViewModel.getAllFeedData())
                    allFeedAdapter.addFollowList(getFeedViewModel.getFollowList())

                    if (positionFromShorts?.setPosition == true) {
                        feedListView.scrollToPosition(positionFromShorts!!.allFragmentFeedPosition)
                        val feedPostData = getFeedViewModel.getAllFeedDataByPosition(
                            positionFromShorts!!.allFragmentFeedPosition)
                        feedFileClicked(positionFromShorts!!.allFragmentFeedPosition, feedPostData)
                    } else {
                        feedListView.scrollToPosition(getFeedViewModel.allFeedDataLastViewPosition)
                    }
                    getFeedViewModel.setIsDataAvailable(false)
                }
            }
        }
    }



    fun updateFollowingList(followingIds: Set<String>) {
        Log.d("AllFragment", "Received ${followingIds.size} following IDs")

        // Update the adapter if it's initialized
        if (::allFeedAdapter.isInitialized) {
            allFeedAdapter.updateFollowingList(followingIds)
            allFeedAdapter.notifyDataSetChanged()
            Log.d("AllFragment", "Updated adapter with following list")
        } else {
            Log.w("AllFragment", "Adapter not initialized yet")
        }
    }

    private fun loadInitialBatch() {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                // Load first batch in parallel
                val jobs = mutableListOf<Job>()

                for (page in 1..INITIAL_PAGES_TO_LOAD) {
                    val job = preloadScope.launch {
                        loadPageSilently(page)
                    }
                    jobs.add(job)
                }

                // Wait for all initial pages
                jobs.joinAll()

                // Update UI
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    isInitialLoadComplete = true
                    allFeedAdapter.submitItems(getFeedViewModel.getAllFeedData())

                    // Continue loading more in background
                    continueBackgroundLoading()
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error in initial batch load: ${e.message}")
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Failed to load feed. Pull to refresh.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun continueBackgroundLoading() {
        preloadScope.launch {
            var currentPage = INITIAL_PAGES_TO_LOAD + 1

            while (hasMorePages && currentPage <= 100) {
                // Load in batches
                val jobs = mutableListOf<Job>()

                for (i in 0 until BATCH_SIZE) {
                    if (!hasMorePages) break

                    val page = currentPage + i
                    val job = launch {
                        loadPageSilently(page)
                    }
                    jobs.add(job)
                }

                jobs.joinAll()
                currentPage += BATCH_SIZE

                // Small delay between batches to not overwhelm server
                delay(500)
            }
        }
    }

    private fun preloadNextBatch(currentPage: Int) {
        preloadScope.launch {
            // Preload next 5 pages
            for (page in currentPage..(currentPage + 5)) {
                if (!hasMorePages) break
                loadPageSilently(page)
            }
        }
    }

    private fun ensurePageLoaded(page: Int) {
        preloadScope.launch {
            loadPageSilently(page)
        }
    }

    private suspend fun loadPageSilently(page: Int) {
        // Prevent duplicate loading
        synchronized(pageLock) {
            if (loadedPages.contains(page) || loadingPages.contains(page) || !hasMorePages) {
                return
            }
            loadingPages.add(page)
        }

        try {
            val response = retrofitInstance.apiService.getAllFeed(page.toString())

            if (!response.isSuccessful || response.body() == null) {
                synchronized(pageLock) {
                    loadingPages.remove(page)
                }
                return
            }

            val posts = response.body()!!.data.data.posts

            // Check if we've reached the end
            if (posts.isEmpty()) {
                synchronized(pageLock) {
                    hasMorePages = false
                    loadingPages.remove(page)
                }
                return
            }

            withContext(Dispatchers.Main) {
                // Add data to ViewModel
                getFeedViewModel.addAllFeedData(posts.toMutableList())

                // Only update adapter if initial load is complete
                if (isInitialLoadComplete) {
                    allFeedAdapter.submitItems(getFeedViewModel.getAllFeedData())
                }

                synchronized(pageLock) {
                    loadedPages.add(page)
                    loadingPages.remove(page)
                }

                Log.d(TAG, "Silently loaded page $page. Total: ${getFeedViewModel.getAllFeedData().size}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error loading page $page: ${e.message}")
            withContext(Dispatchers.Main) {
                synchronized(pageLock) {
                    loadingPages.remove(page)
                }
            }
        }
    }

    fun getAllFeed(page: Int) {
        lifecycleScope.launch {
            loadPageSilently(page)
        }
    }

    fun refreshFeed() {
        loadedPages.clear()
        loadingPages.clear()
        hasMorePages = true
        isInitialLoadComplete = false
        getFeedViewModel.clearAllFeedData()
        allFeedAdapter.clearItems()
        progressBar.visibility = View.VISIBLE
        loadInitialBatch()
    }

    override fun onResume() {
        super.onResume()
        getFeedViewModel.isResuming = true
        feedListView.visibility = View.VISIBLE
        frameLayout.visibility = View.GONE
        EventBus.getDefault().post(ShowBottomNav(false))
        EventBus.getDefault().post(HideFeedFloatingActionButton())
        EventBus.getDefault().post(ShowAppBar(false))

        // Only refresh if data is empty
        if (getFeedViewModel.getAllFeedData().isEmpty()) {
            refreshFeed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isFragmentOpen = false
        EventBus.getDefault().unregister(this)
    }

    fun setPositionFromShorts(positionFromShorts: SetAllFragmentScrollPosition) {
        Log.d(TAG, "setPositionFromShorts: ${positionFromShorts.allFragmentFeedPosition}")
        this.positionFromShorts = positionFromShorts
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_all, container, false)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun successEvent(event: FeedUploadProgress) {
        progressBar.max = event.maxProgress
        progressBar.progress = event.currentProgress
    }

    override fun onDetach() {
        super.onDetach()
        Log.d(TAG, "onDetach: called")

    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: called")
    }

    override fun onPause() {
        super.onPause()
        currentAdapterPosition = allFeedAdapter.getCurrentItemDisplayPosition()
        Log.d(TAG, "onCreateView: data added last view position $currentAdapterPosition")
        Log.d(TAG, "onPause: called")

    }

    private fun getCurrentFeedPosition(): Int? {
        val layoutManager = feedListView.layoutManager as LinearLayoutManager
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()


        return firstVisibleItemPosition.takeIf { it <= lastVisibleItemPosition }

    }



    override fun likeUnLikeFeed(position: Int, data: Post) {

        Log.d("likeUnLikeFeed", "likeUnLikeFeed: $data")

        try {
            // Toggle like status and update likes count
            val newLikeStatus = !data.isLiked
            val updatedComment = data.copy(
                likes = if (newLikeStatus) data.likes + 1 else maxOf(0, data.likes - 1),
                repostedByUserId = data.repostedByUserId ?: "",
                content = data.content ?: "",
                contentType = data.contentType ?: "",
                isLiked = newLikeStatus,
               // url = data.url // Ensure url is passed to avoid nullability issue
            )

            // Update server with like/unlike action
            lifecycleScope.launch {
                feedUploadViewModel.likeUnLikeFeed(data._id)
            }

            Log.d("likeUnLikeFeed", "likeUnLikeFeed: likes count is ${updatedComment.likes}")

            // Update all feed data
            val updatedItems = getFeedViewModel.getAllFeedData()
            for (updatedItem in updatedItems) {
                if (updatedItem._id == data._id) {
                    updatedItem.likes = updatedComment.likes
                    updatedItem.isLiked = newLikeStatus
                }
            }

            // Handle favorite feed data
            val isFavoriteFeedDataEmpty = getFeedViewModel.getAllFavoriteFeedData().isEmpty()
            if (!isFavoriteFeedDataEmpty) {
                val favoriteFeedData = getFeedViewModel.getAllFavoriteFeedData()
                val feedToUpdate = favoriteFeedData.find { feed -> feed._id == data._id }
                if (feedToUpdate != null) {
                    EventBus.getDefault().post(FeedLikeClick(position, updatedComment))
                    Log.d("likeUnLikeFeed", "likeUnLikeFeed: updated feed in favorite fragment")
                } else {
                    Log.d("likeUnLikeFeed", "likeUnLikeFeed: add feed to favorite fragment")
                }
            } else {
                Log.i("likeUnLikeFeed", "likeUnLikeFeed: favorite feed data is empty")
            }

            // Update adapter with new data
            allFeedAdapter.updateItem(position, updatedComment)

            // Update my feed data
            val isMyFeedEmpty = getFeedViewModel.getMyFeedData().isEmpty()
            if (!isMyFeedEmpty) {
                val myFeedData = getFeedViewModel.getMyFeedData()
                val feedToUpdate = myFeedData.find { feed -> feed._id == data._id }
                if (feedToUpdate != null) {
                    feedToUpdate.isLiked = newLikeStatus
                    feedToUpdate.likes = updatedComment.likes
                    val myFeedDataPosition = getFeedViewModel.getMyFeedPositionById(feedToUpdate._id)
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

    override fun feedCommentClicked(position: Int, data: Post) {
        //implemented in main activity kt
        Log.d(TAG, "feedCommentClick: this is the one listening")
        EventBus.getDefault().post(FeedCommentClicked(position, data))
    }

    private fun hidingBottomNav() {
        EventBus.getDefault().post(HideBottomNav())
    }

    override fun feedFavoriteClick(
        position: Int,
        data: Post
    ) {
        Log.d(TAG, "feedFavoriteClick: favorite clicked")
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

        lifecycleScope.launch {
            feedUploadViewModel.favoriteFeed(data._id)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun favoriteFeedClick(event: FromFavoriteFragmentFeedFavoriteClick) {
        Log.d("FromFavoriteFragmentFeedFavoriteClick",
            "FromFavoriteFragmentFeedFavoriteClick: ")
        val feedPosition = allFeedAdapter.getPositionById(event.data._id)
        allFeedAdapter.updateItem(feedPosition, event.data)
        getFeedViewModel.updateForAllFeedFragment(feedPosition, event.data)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun favoriteFromOtherUsersFeedFavoriteClick(event: FromOtherUsersFeedFavoriteClick) {
        Log.d("FromOtherUsersFeedFavoriteClick", "FromOtherUsersFeedFavoriteClick: ")
        val feedPosition = allFeedAdapter.getPositionById(event.data._id)
        EventBus.getDefault().post(FeedFavoriteClick(event.position, event.data))
        allFeedAdapter.updateItem(feedPosition, event.data)
        getFeedViewModel.updateForAllFeedFragment(feedPosition, event.data)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun feedUploadResponseEvent(event: FeedUploadResponseEvent) {
        Log.d("feedUploadResponseEvent", "feedUploadResponseEvent: ")

        val feedPost = getFeedViewModel.getSingleAllFeedData()

        feedPost._id = event.id

        Log.i("feedUploadResponseEvent",
            "feedUploadResponseEvent:feed post id:  ${feedPost._id}")
        allFeedAdapter.updateItem(0, feedPost)
        getFeedViewModel.updateForAllFeedFragment(0, feedPost)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun likeFeedClick(event: FromFavoriteFragmentFeedLikeClick) {
        Log.d(
            TAG,
            "likeFeedClick: event bus position " +
                    "${event.position} isLiked ${event.data.isLiked} likes ${event.data.likes}"
        )
        val feedPosition = allFeedAdapter.getPositionById(event.data._id)
        allFeedAdapter.updateItem(feedPosition, event.data)
        getFeedViewModel.updateForAllFeedFragment(feedPosition, event.data)
    }

    @SuppressLint("InflateParams", "MissingInflatedId", "ServiceCast", "CutPasteId")
    override fun moreOptionsClick(position: Int, data: Post) {

        Log.d(TAG, "moreOptionsClick: More Options Clicked")
        val view: View = layoutInflater.inflate(
            R.layout.feed_more_options_layout, null)
        val dialog = BottomSheetDialog(requireContext())

        dialog.setContentView(view)
        dialog.show()
        val downloadFiles: View = view.findViewById(R.id.downloadAction)
        val followUnfollowLayout: View = view.findViewById(R.id.followAction)
        val reportUser: View = view.findViewById(R.id.reportOptionLayout)
        val hidePostLayout: View = view.findViewById(R.id.hidePostLayout)
        val copyLink: View = view.findViewById(R.id.copyLinkLayout)
        val muteOptionLayout: View = view.findViewById(R.id.muteOptionLayout)
        val QuoteFeedLayout: View = view.findViewById(R.id.repostAction)
        val shareAction: View = view.findViewById(R.id.shareAction)

        shareAction.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, data.content)
            startActivity(Intent.createChooser(shareIntent, "Share via"))
            dialog.dismiss()
        }
        downloadFiles.setOnClickListener {
            Log.d("DownloadButton", "Data: $data")
            onDownloadClick(data.files[0].url, "FlashShorts")
            dialog.dismiss()
        }
        muteOptionLayout.setOnClickListener {
            Log.d("MuteButton", "Data: $data")
            Toast.makeText(context, "User muted", Toast.LENGTH_SHORT).show()

        }
        followUnfollowLayout.visibility = View.GONE
        QuoteFeedLayout.setOnClickListener {
            val fragment = Fragment_Edit_Post_To_Repost(data)

            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(
                R.id.frame_layout, fragment) // Ensure fragment_container is correct
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
            Toast.makeText(requireContext(),
                "Link copied to clipboard/$postId", Toast.LENGTH_SHORT)
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

        val downloadOption: View = view.findViewById(R.id.downloadAction)
        if (data.isBookmarked) {
            data.isBookmarked = true
        }
        if (data.contentType == "text") {
            downloadOption.visibility = View.GONE
        }

        reportUser.setOnClickListener {
            Log.d("reportUser", "has been clicked")
            val intent = Intent(requireActivity(),
                ReportNotificationActivity2::class.java)
            startActivityForResult(intent, REQUEST_REPOST_FEED_ACTIVITY)
            dialog.dismiss()
        }

        downloadOption.setOnClickListener {
            Log.d(TAG, "Download option clicked for post: $data")
            Toast.makeText(requireContext(), "download clicked",
                Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
    }

    fun onSeekBarChanged(progress: Int) {
        TODO("Not yet implemented")
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



        val STORAGE_FOLDER = "/Download/Flash/$fileLocation"


        val fileName = generateUniqueFileName(mUrl)

        val storageDirectory =
            Environment.getExternalStorageDirectory().toString() + STORAGE_FOLDER + "/$fileName"

        Log.d("Download", "directory path - $storageDirectory")
        val file = File(

            Environment.getExternalStorageDirectory().toString() + STORAGE_FOLDER)
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
            SimpleDateFormat("yyyy_MM_dd_HHmmss",
                Locale.getDefault()).format(Date())
        val originalFileName = originalUrl.split("/").last()
        val fileExtension = MimeTypeMap.getFileExtensionFromUrl(originalFileName)
        val randomString = UUID.randomUUID().toString().substring(0, 8)
        return "$timestamp-$randomString.$fileExtension"
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun hideSinglePost(
        position: Int,
        data: Post
    ) {
        Log.d(TAG,
            "hideSinglePost: Hiding post at position: $position, PostId: ${data._id}")
        try {
            if (::allFeedAdapter.isInitialized) {


                allFeedAdapter.removeItem(position)
                allFeedAdapter.notifyItemRemoved(position)

                val viewHolder = feedListView.findViewHolderForAdapterPosition(position)
                if (viewHolder != null) {
                    viewHolder.itemView.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction {
                            allFeedAdapter.notifyItemRemoved(position)
                        }
                        .start()
                } else {
                    Log.w(
                        TAG,
                        "ViewHolder at position $position is null, notifying removal directly"
                    )
                    allFeedAdapter.notifyItemRemoved(position) // Fallback for off-screen items
                }
                // Show Snackbar with Undo button
                Snackbar.make(feedListView, "Post hidden", Snackbar.LENGTH_LONG)
                    .setAction("Undo") {

                        allFeedAdapter.notifyItemInserted(position)
                    }
                    .show()
                return
            }

            val sharedPrefs =
                requireContext().getSharedPreferences(
                    "HiddenPosts", Context.MODE_PRIVATE)
            with(sharedPrefs.edit()) {
                putBoolean(data._id, true)
                apply()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error hiding post: ${e.message}")
            Toast.makeText(requireContext(),
                "Failed to hide post", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showReportConfirmationDialog(feedId: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Report User")
        builder.setMessage("Are you sure you want to report this user?")
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }

    }

    private fun handleNotInterested(
        data: Post
    ) {

        val sharedPrefs =
            requireContext().getSharedPreferences("NotInterestedPosts", Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putBoolean(data._id.toString(), true)
            apply()
        }



        // Show confirmation
        Toast.makeText(
            requireContext(),
            "We'll show you less content like this",
            Toast.LENGTH_SHORT
        ).show()
    }

    @SuppressLint("InflateParams")
    private fun showDeleteConfirmationDialog(feedId: String, position: Int) {
        val inflater = LayoutInflater.from(requireContext())
        val customTitleView: View = inflater.inflate(
            R.layout.delete_title_custom_layout, null)
        val builder = AlertDialog.Builder(requireContext())

        builder.setCustomTitle(customTitleView)
        builder.setMessage("Are you sure you want to delete this feed?")

        // Positive Button
        builder.setPositiveButton("Delete") { dialog, which ->


            handleDeleteAction(feedId = feedId, position) { isSuccess, message ->
                if (isSuccess) {
                    Log.d(TAG, "handleDeleteAction $message")
                    dialog.dismiss()
                } else {
                    dialog.dismiss()
                    Log.e(TAG, "handleDeleteAction $message")
                }
            }
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

    private fun handleDeleteAction(
        feedId: String,
        position: Int,
        callback: (Boolean, String) -> Unit
    ) {

        Log.d(TAG, "handleDeleteAction: remove from database")
        lifecycleScope.launch {
            val response = retrofitInstance.apiService.deleteFeed(feedId)
            Log.d(TAG, "handleDeleteAction: $response")
            Log.d(TAG, "handleDeleteAction body: ${response.body()}")
            Log.d(TAG, "handleDeleteAction isSuccessful: ${response.isSuccessful}")
            if (response.isSuccessful) {
                getFeedViewModel.removeMyFeed(position)
                myFeedAdapter.removeItem(position)


                shortsViewModel.postCount -= 1
                shortsViewModel.setIsRefreshPostCount(true)


                Log.d(TAG, "handleDeleteAction: delete successful")
                showSnackBar("File has been deleted successfully")
                val isAllFeedDataEmpty = getFeedViewModel.getAllFeedData().isEmpty()
                val isFavoriteFeedDataEmpty = getFeedViewModel.getAllFavoriteFeedData().isEmpty()

                if (!isFavoriteFeedDataEmpty) {
                    val favoriteFeed = getFeedViewModel.getAllFavoriteFeedData()
                    val feedToUpdate = favoriteFeed.find { feed -> feed._id == feedId }

                    if (feedToUpdate != null) {
                        Log.d(TAG, "handleDeleteAction: feed to update id ${feedToUpdate._id}")
                        try {
                            Log.d("feedResponse", "handleDeleteAction: 1 ${feedToUpdate._id}")
                            val feedPos = getFeedViewModel.getPositionById(feedId)
                            Log.d("feedResponse", "handleDeleteAction: 2 ${feedToUpdate._id}")
                            getFeedViewModel.removeFavoriteFeed(feedPos)
                            Log.d("feedResponse", "handleDeleteAction: 3 ${feedToUpdate._id}")

                            Log.d("feedResponse", "handleDeleteAction: 4 ${feedToUpdate._id}")

                        } catch (e: Exception) {
                            Log.e(TAG, "handleDeleteAction: error on bookmark delete ${e.message}")
                            e.printStackTrace()
                        }

                    } else {
                        Log.e(
                            "feedResponse",
                            "handleDeleteAction: feed to un-favorite not available"
                        )
                    }
                }

                if (!isAllFeedDataEmpty) {
                    val allFeedData = getFeedViewModel.getAllFeedData()
                    val feedToUpdate = allFeedData.find { feed -> feed._id == feedId }
                    if (feedToUpdate != null) {
                        Log.d(TAG, "handleDeleteAction: feed data found for all fragment")
                        val pos = getFeedViewModel.getAllFeedDataPositionById(feedToUpdate._id)
                        try {
                            getFeedViewModel.removeAllFeedFragment(pos)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    } else {
                        Log.d(TAG, "handleDeleteAction: feed data not found for all fragment")
                    }
                } else {
                    Log.i(TAG, "handleDeleteAction: all feed data is empty")
                }

                if (!isFavoriteFeedDataEmpty) {
                    val favoriteFeedData = getFeedViewModel.getAllFavoriteFeedData()
                    val feedToUpdate = favoriteFeedData.find { feed -> feed._id == feedId }
                    if (feedToUpdate != null) {
                        Log.d(TAG, "handleDeleteAction: feed data found for favorite")
                        getFeedViewModel.setRefreshMyData(position, true)
                    } else {
                        Log.d(TAG, "handleDeleteAction: feed data not found for favorite")
                    }
                } else {
                    Log.i(TAG, "handleDeleteAction: favorite feed data is empty")
                }
            } else {
                callback(false, "Failed to delete file")
                showSnackBar("Please try again!!!")
            }
        }
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(
            requireActivity().findViewById(
                android.R.id.content), message, 1000)
            .setBackgroundTint(
                (ContextCompat.getColor(
                    requireContext(),
                    R.color.green_dark
                ))
            ) // Custom background color
            .setAction("OK") {
                // Handle undo action if needed
            }
            .show()
    }

    override fun feedFileClicked(position: Int, data: Post) {
        Log.d(TAG, "feedFileClicked: clicked on isBookmarked ${data.isBookmarked}")

        // Hide UI elements
        EventBus.getDefault().post(HideBottomNav())
        EventBus.getDefault().post(HideAppBar())
        EventBus.getDefault().post(HideFeedFloatingActionButton())

        // Show container and hide feed list
        frameLayout.visibility = View.VISIBLE
        feedListView.visibility = View.GONE

        // Calling the Tapped_Files_In_The_Container_View fragment
        val tappedFilesFragment = Tapped_Files_In_The_Container_View_Fragment()

        // Set up arguments
        val args = Bundle().apply {
            putInt("position", position)
            putSerializable("data", data)
            putString("contentType", data.contentType) // Pass content type for handling
        }
        tappedFilesFragment.arguments = args

        // Replace fragment
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, tappedFilesFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun feedRepostFileClicked(
        position: Int,
        data: OriginalPost
    ) {
        // Hide UI elements
        EventBus.getDefault().post(HideBottomNav())
        EventBus.getDefault().post(HideAppBar())
        EventBus.getDefault().post(HideFeedFloatingActionButton())

        // Show container and hide feed list
        frameLayout.visibility = View.VISIBLE
        feedListView.visibility = View.GONE

        // Calling the Tapped_Files_In_The_Container_View fragment
        val tappedFilesFragment = Tapped_Files_In_The_Container_View_Fragment()

        // Set up arguments
        val args = Bundle().apply {
            putInt("position", position)
            putSerializable("data", data)
            putString("contentType", data.contentType) // Pass content type for handling
            putBoolean("isRepost", true) // Flag to indicate this is a repost
        }
        tappedFilesFragment.arguments = args


        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, tappedFilesFragment)
            .addToBackStack(null)
            .commit()
    }


    @SuppressLint("MissingInflatedId")
    override fun feedShareClicked(position: Int, data: Post) {
        val context = requireContext()

        // Create and set up the bottom sheet dialog
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val shareView = layoutInflater.inflate(R.layout.bottom_dialog_for_share, null)
        bottomSheetDialog.setContentView(shareView)

        // Find buttons from the XML layout
        val btnWhatsApp = shareView.findViewById<ImageButton>(R.id.btnWhatsApp)
        val btnSMS = shareView.findViewById<ImageButton>(R.id.btnSMS)
        val btnInstagram = shareView.findViewById<ImageButton>(R.id.btnInstagram)
        val btnMessenger = shareView.findViewById<ImageButton>(R.id.btnMessenger)
        val btnFacebook = shareView.findViewById<ImageButton>(R.id.btnFacebook)
        val btnTelegram = shareView.findViewById<ImageButton>(R.id.btnTelegram)
        val btnReport = shareView.findViewById<ImageButton>(R.id.btnReport)
        val btnNotInterested = shareView.findViewById<ImageButton>(R.id.btnNotInterested)
        val btnSaveVideo = shareView.findViewById<ImageButton>(R.id.btnSaveVideo)
        val btnDuet = shareView.findViewById<ImageButton>(R.id.btnDuet)
        val btnReact = shareView.findViewById<ImageButton>(R.id.btnReact)
        val btnAddToFavorites = shareView.findViewById<ImageButton>(R.id.btnAddToFavorites)
        val btnCancel = shareView.findViewById<TextView>(R.id.btnCancel)

        // Helper function to create share intent
        fun createShareIntent(packageName: String? = null) {
            val shareText = data.content
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
                if (packageName != null) {
                    setPackage(packageName)
                }
            }
            startActivity(Intent.createChooser(intent, "Share to"))
            bottomSheetDialog.dismiss()
        }

        // Set up click listeners for share buttons
        btnWhatsApp.setOnClickListener {
            createShareIntent("com.whatsapp")
        }

        btnSMS.setOnClickListener {
            createShareIntent("com.android.mms")
        }

        btnInstagram.setOnClickListener {
            createShareIntent("com.instagram.android")
        }

        btnMessenger.setOnClickListener {
            createShareIntent("com.facebook.orca")
        }

        btnFacebook.setOnClickListener {
            createShareIntent("com.facebook.katana")
        }

        btnTelegram.setOnClickListener {
            createShareIntent("org.telegram.messenger")
        }

        // Placeholder click listeners for action buttons
        btnReport.setOnClickListener {
            // Implement report functionality (e.g., open report dialog or send to server)
            Toast.makeText(context, "Report clicked", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }

        btnNotInterested.setOnClickListener {
            // Implement not interested functionality (e.g., update feed preferences)
            Toast.makeText(context, "Not Interested clicked", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }

        btnSaveVideo.setOnClickListener {
            // Implement save video functionality
            Toast.makeText(context, "Save Video clicked", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }

        btnDuet.setOnClickListener {
            // Implement duet functionality
            Toast.makeText(context, "Duet clicked", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }

        btnReact.setOnClickListener {
            // Implement react functionality
            Toast.makeText(context, "React clicked", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }

        btnAddToFavorites.setOnClickListener {
            // Implement add to favorites functionality
            Toast.makeText(context, "Add to Favorites clicked", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }

        // Cancel button
        btnCancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        // Show the dialog
        bottomSheetDialog.show()
    }

    // Replace fragment helper method
    private fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction = parentFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container, fragment)  // Replace with your container's ID
        fragmentTransaction.addToBackStack(null)  // Optional, if you want to add it to the back stack
        fragmentTransaction.commit()
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun feedFavoriteFollowUpdate(event: FeedFavoriteFollowUpdate) {
        Log.d(TAG, "feedFavoriteFollowUpdate: feed follow update")

    }

    override fun followButtonClicked(
        followUnFollowEntity: FollowUnFollowEntity,
        followButton: AppCompatButton
    ) {
        Log.d("followButtonClicked", "followButtonClicked: clicked")

        EventBus.getDefault().post(
            FeedFavoriteFollowUpdate(
                followUnFollowEntity.userId,
                followUnFollowEntity.isFollowing
            )
        )

        feedShortsSharedViewModel.setData(
            FollowUnFollowEntity(
                followUnFollowEntity.userId,
                followUnFollowEntity.isFollowing
            )
        )

        followClicked(followUnFollowEntity)
    }

    @SuppressLint("MissingInflatedId", "CommitTransaction", "CutPasteId")
    override fun feedRepostPost(
        position: Int,
        data: Post
    ) {
        val view: View = layoutInflater.inflate(
            R.layout.feed_moreoptions_bottomsheet_layout, null)

        val quoteButton: MaterialCardView = view.findViewById(R.id.rePostFeedLayout)
        val repostButton: MaterialCardView = view.findViewById(R.id.shareFeedLayout)
        val download: MaterialCardView = view.findViewById(R.id.downloadFeedLayout)
        val followUnfollowLayout: MaterialCardView = view.findViewById(R.id.followUnfollowLayout)
        val shareFeedLayout: MaterialCardView = view.findViewById(R.id.shareFeedLayout)
        val notInterestedLayout: MaterialCardView = view.findViewById(R.id.notInterestedLayout)
        val hidePostLayout: MaterialCardView = view.findViewById(R.id.hidePostLayout)
        val reportOptionLayout: MaterialCardView = view.findViewById(R.id.reportOptionLayout)
        val copyLinkLayout: MaterialCardView = view.findViewById(R.id.copyLinkLayout)
        val muteUser: MaterialCardView = view.findViewById(R.id.muteOptionLayout)
        download.visibility = View.GONE
        repostButton.visibility = View.GONE
        repostButton.visibility = View.GONE
        download.visibility = View.GONE
        shareFeedLayout.visibility = View.GONE
        notInterestedLayout.visibility = View.GONE
        hidePostLayout.visibility = View.GONE
        reportOptionLayout.visibility = View.GONE
        copyLinkLayout.visibility = View.GONE
        followUnfollowLayout.visibility = View.GONE
        quoteButton.visibility = View.VISIBLE
        muteUser.visibility = View.GONE
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(view)
        dialog.show()

        quoteButton.setOnClickListener {
            Log.d("QuoteButton", "Data: $data")
            dialog.dismiss()
            val fragment = Fragment_Edit_Post_To_Repost(data)
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, fragment) // Ensure fragment_container is correct
            transaction.addToBackStack("NewRepostedPostFragment") // Name the back stack entry
            transaction.commit()
            hidingBottomNav()

        }
    }

    override fun feedRepostPostClicked(position: Int, data: Post) {
        TODO("Not yet implemented")
    }

    override fun feedClickedToOriginalPost(position: Int, originalPostId: String) {
        val intent = Intent(requireActivity(), PostFeedActivity::class.java)
        intent.putExtra("originalPostId", originalPostId)
        startActivity(intent)
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

    private fun shareTextFeed(data: com.uyscuti.social.network.api.response.allFeedRepostsPost.Post) {
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

    private fun shareImageFeed(data: com.uyscuti.social.network.api.response.getfeedandresposts.Post) {
        Glide.with(this).asBitmap().load(data.files[0].url).into(
            object : CustomTarget<Bitmap?>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap?>?
                ) {
                    bitmap = resource
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            }
        )
        if (bitmap != null) {
            shareImage()
        }
    }

    private fun shareImageFeed(data: com.uyscuti.social.network.api.response.allFeedRepostsPost.Post) {
        Glide.with(this).asBitmap().load(data.files[0].url).into(
            object : CustomTarget<Bitmap?>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap?>?
                ) {
                    bitmap = resource
                    // Call shareImage() once the bitmap is ready
                    shareImage()
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Handle cleanup if needed
                }
            }
        )
    }

    private fun shareImage() {
        try {
            val cachePath = File(requireActivity().cacheDir, "images")
            cachePath.mkdir()
            val stream = FileOutputStream("${cachePath}/sharable_image.png")
            bitmap?.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

        } catch (e: IOException) {
            e.printStackTrace()
        }

        val imagePath = File(requireActivity().cacheDir, "images")
        val newFile = File(imagePath, "sharable_image.png")
        val contentUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireActivity().applicationContext.packageName}.file provider",
            newFile
        )

        if (contentUri != null) {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            shareIntent.setDataAndType(
                contentUri,
                requireActivity().contentResolver.getType(contentUri)
            )

            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
            shareIntent.putExtra(Intent.EXTRA_TEXT, "This image is shared from flash")
            startActivity(Intent.createChooser(shareIntent, "Share via"))
        }
    }

    fun forShow() {
        Log.d("forShow", "forShow: is called")
    }


    override fun backPressedFromFeedTextViewFragment() {
        Log.d(TAG, "backPressedFromFeedTextViewFragment: listening back pressed ")
        feedListView.visibility = View.VISIBLE
        frameLayout.visibility = View.GONE
        EventBus.getDefault().post(ShowBottomNav(false))
        EventBus.getDefault().post(ShowAppBar(false))
        EventBus.getDefault().post(ShowFeedFloatingActionButton(false))

    }

    override fun onCommentClickFromFeedTextViewFragment(position: Int, data: Post) {
        EventBus.getDefault().post(FeedCommentClicked(position, data))
        Log.d(TAG, "onCommentClickFromFeedTextViewFragment: comment clicked")
    }

    override fun onLikeUnLikeFeedFromFeedTextViewFragment(position: Int, data: Post) {
        try {
            val updatedComment = if (data.isLiked) {
                data.copy(
                    likes = data.likes + 1,
                    repostedByUserId = data.repostedByUserId ?: "",
                    feedShortsBusinessId = data.feedShortsBusinessId // Ensure this field is set
                )
            } else {
                data.copy(
                    likes = data.likes - 1,
                    repostedByUserId = data.repostedByUserId ?: "",
                    feedShortsBusinessId = data.feedShortsBusinessId // Ensure this field is set
                )
            }
            lifecycleScope.launch {
                feedUploadViewModel.likeUnLikeFeed(data._id)
            }
            Log.d("likeUnLikeFeed", "likeUnLikeFeed: likes count is ${data.likes}")
            val updatedItems = getFeedViewModel.getAllFeedData()
            for (updatedItem in updatedItems) {
                if (updatedItem._id == data._id) {
                    updatedItem.isLiked = data.isLiked
                    if (data.isLiked) {
                        updatedItem.likes += 1
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
            allFeedAdapter.updateItem(position, updatedComment)
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
        } catch (e: Exception) {
            Log.e("likeUnLikeFeed", "likeUnLikeFeed: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun onFeedFavoriteClickFromFeedTextViewFragment(position: Int, data: Post) {
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
                allFeedAdapter.updateItem(position, data)
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

    @SuppressLint("InflateParams")
    override fun onMoreOptionsClickFromFeedTextViewFragment(position: Int, data: Post) {

        val view: View = layoutInflater.inflate(R.layout.more_options_redesign_layout, null)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(view)
        dialog.show()
        val reportOptionLayout: LinearLayout = view.findViewById(R.id.reportOption)
        val hidePostLayout: LinearLayout = view.findViewById(R.id.hidePostOption)
        val followUnfollowLayout: LinearLayout = view.findViewById(R.id.followUnfollowOption)
        val notInterestedLayout: LinearLayout = view.findViewById(R.id.notInterestedOption)
        notInterestedLayout.visibility = View.GONE
        hidePostLayout.visibility = View.GONE
        followUnfollowLayout.visibility = View.GONE

        hidePostLayout.setOnClickListener {
            Log.d("HideLayout", "has been clicked")

        }
        reportOptionLayout.setOnClickListener {
            Log.d("reportUser", "has been clicked")
            val intent = Intent(requireActivity(), ReportNotificationActivity2::class.java)
            startActivityForResult(intent, REQUEST_REPOST_FEED_ACTIVITY)
        }
    }

    override fun finishedPlayingVideo(position: Int) {
        Log.d("finishedPlayingVideo", "finishedPlayingVideo: refresh on position $position")
        allFeedAdapter.notifyItemChanged(position)
    }

    override fun onRePostClickFromFeedTextViewFragment(position: Int, data: Post) {
        TODO("Not yet implemented")
    }

    override fun onFullScreenClicked(data: MixedFeedUploadDataClass) {
        TODO("Not yet implemented")
    }

    override fun onMediaClick(data: MixedFeedUploadDataClass) {
        TODO("Not yet implemented")
    }

    override fun onMediaPrepared(mp: MediaPlayer) {
        TODO("Not yet implemented")
    }

    override fun onMediaError() {
        TODO("Not yet implemented")
    }

    @SuppressLint("NotifyDataSetChanged")
    fun onBackPressed() {

        frameLayout.visibility = View.GONE
        EventBus.getDefault().post(ShowBottomNav(false))
        allFeedAdapter.notifyDataSetChanged()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun feedAllFeedUpdateLike(event: AllFeedUpdateLike) {

        Log.d(
            "AllFeedUpdateLike",
            "AllFeedUpdateLike: event bus position ${event.position} isLiked ${event.data.isLiked} likes ${event.data.likes}"
        )

        val feedPosition = allFeedAdapter.getPositionById(event.data._id)
        allFeedAdapter.updateItem(feedPosition, event.data)
        getFeedViewModel.updateForAllFeedFragment(feedPosition, event.data)

        val isFavoriteFeedDataEmpty = getFeedViewModel.getAllFavoriteFeedData().isEmpty()
        if (!isFavoriteFeedDataEmpty) {
            val favoriteFeedData = getFeedViewModel.getAllFavoriteFeedData()
            val feedToUpdate = favoriteFeedData.find { feed -> feed._id == event.data._id }
            if (feedToUpdate != null) {
                EventBus.getDefault().post(FeedLikeClick(event.position, event.data))
                Log.d("likeUnLikeFeed", "likeUnLikeFeed: remove feed from favorite fragment")
            } else {
                Log.d("likeUnLikeFeed", "likeUnLikeFeed: add feed to favorite fragment")
            }

        } else {

            Log.i("likeUnLikeFeed", "likeUnLikeFeed: my feed data is empty")
        }
    }

    override fun hideFloatingActionButton() {

        TODO("Not yet implemented")
    }

    override fun displayFloatingActionButton() {
        TODO("Not yet implemented")
    }


}
