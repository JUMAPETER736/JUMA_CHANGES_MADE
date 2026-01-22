package com.uyscuti.social.circuit.ui.fragments.feed

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
import com.uyscuti.social.circuit.adapter.UserListAdapter
import com.uyscuti.social.circuit.adapter.feed.ShareFeedPostAdapter
import com.uyscuti.social.circuit.ui.feedactivities.FeedVideoViewFragment
import com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.FeedAudioViewFragment
import com.uyscuti.social.circuit.ui.fragments.feed.feedRepostViewFragments.FeedRepostDocFragment
import com.uyscuti.social.circuit.ui.fragments.feed.feedRepostViewFragments.FeedRepostImageFragment
import com.uyscuti.social.circuit.ui.fragments.feed.feedRepostViewFragments.FeedRepostTextFragment
import com.uyscuti.social.circuit.ui.fragments.feed.feedRepostViewFragments.FeedRepostVideoViewFragment
import com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.FeedImageViewFragment
import com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.FeedMultipleImageViewFragment
import com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.FeedTextViewFragment
import com.uyscuti.sharedmodule.ReportNotificationActivity2
import com.uyscuti.sharedmodule.adapter.feed.FeedAdapter
import com.uyscuti.sharedmodule.adapter.feed.OnFeedClickListener
import com.uyscuti.sharedmodule.adapter.feed.feed.postFeedActivity.PostFeedActivity
import com.uyscuti.sharedmodule.eventbus.AllFeedUpdateLike
import com.uyscuti.sharedmodule.eventbus.FeedFavoriteClick
import com.uyscuti.sharedmodule.eventbus.FeedFavoriteFollowUpdate
import com.uyscuti.sharedmodule.eventbus.FeedLikeClick
import com.uyscuti.sharedmodule.eventbus.FeedUploadResponseEvent
import com.uyscuti.sharedmodule.eventbus.FromFavoriteFragmentFeedFavoriteClick
import com.uyscuti.sharedmodule.eventbus.FromFavoriteFragmentFeedLikeClick
import com.uyscuti.sharedmodule.eventbus.FromOtherUsersFeedFavoriteClick
import com.uyscuti.sharedmodule.eventbus.HideFeedFloatingActionButton
import com.uyscuti.sharedmodule.eventbus.ShowFeedFloatingActionButton
import com.uyscuti.sharedmodule.interfaces.feedinterfaces.FeedTextViewFragmentInterface
import com.uyscuti.sharedmodule.interfaces.feedinterfaces.ToggleFeedFloatingActionButton
import com.uyscuti.sharedmodule.model.FeedAdapterNotifyDatasetChanged
import com.uyscuti.sharedmodule.model.FeedCommentClicked
import com.uyscuti.sharedmodule.model.FeedUploadProgress
import com.uyscuti.sharedmodule.model.HideAppBar
import com.uyscuti.sharedmodule.model.HideBottomNav
import com.uyscuti.sharedmodule.model.ShowAppBar
import com.uyscuti.sharedmodule.model.ShowBottomNav
import com.uyscuti.sharedmodule.model.feed.SetAllFragmentScrollPosition
import com.uyscuti.sharedmodule.presentation.DialogViewModel
import com.uyscuti.sharedmodule.ui.fragments.feed.feedviewfragments.FeedDocumentViewFragment
import com.uyscuti.sharedmodule.ui.fragments.feed.feedviewfragments.FeedMixedFilesViewFragment
import com.uyscuti.sharedmodule.ui.fragments.feed.feedviewfragments.Fragment_Original_Post_With_Repost_Inside
import com.uyscuti.sharedmodule.ui.fragments.feed.feedviewfragments.editRepost.Fragment_Edit_Post_To_Repost
import com.uyscuti.sharedmodule.ui.fragments.feed.feedviewfragments.feedRepost.Tapped_Files_In_The_Container_View_Fragment
import com.uyscuti.sharedmodule.utils.removeDuplicateFollowers
import com.uyscuti.sharedmodule.viewmodels.FeedShortsViewModel
import com.uyscuti.sharedmodule.viewmodels.FollowUnfollowViewModel
import com.uyscuti.sharedmodule.viewmodels.GetShortsByUsernameViewModel
import com.uyscuti.sharedmodule.viewmodels.feed.FeedUploadViewModel
import com.uyscuti.sharedmodule.viewmodels.feed.GetFeedViewModel
import com.uyscuti.sharedmodule.viewmodels.feed.UserRelationshipsViewModel
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.feed.FeedUploadRepository
import com.uyscuti.social.circuit.ui.LoginActivity
import com.uyscuti.social.circuit.ui.fragments.chat.FeedFragment
import com.uyscuti.social.circuit.ui.fragments.feed.feedRepostViewFragments.FeedRepostAudioViewFragment
import com.uyscuti.social.circuit.ui.fragments.feed.feedRepostViewFragmentsimport.FeedRepostMultipleImageFragment
import com.uyscuti.social.core.common.data.room.entity.FollowUnFollowEntity
import com.uyscuti.social.core.common.data.room.entity.ShortsEntityFollowList
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
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

/**
 * A simple [Fragment] subclass.
 * Use the [AllFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

private const val TAG = "AllFragment"
private const val REQUEST_REPOST_FEED_ACTIVITY = 1020

@AndroidEntryPoint
class AllFragment : Fragment(), OnFeedClickListener, FeedTextViewFragmentInterface,
    ToggleFeedFloatingActionButton {


    companion object {

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AllFragment.
         */
        // TODO: Rename and change types and number of parameters
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
    private val relationshipsViewModel: UserRelationshipsViewModel by activityViewModels()

    private lateinit var feedListView: RecyclerView
    private lateinit var allFeedAdapter: FeedAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var frameLayout: FrameLayout
    private val requestCode = 2024
    private val PICK_VIDEO_REQUEST = "video/*"
    private val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 12

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
    private val dialogViewModel: DialogViewModel by activityViewModels()
    private var currentAdapterPosition = -1
    private lateinit var feedUploadRepository: FeedUploadRepository
    private var positionFromShorts: SetAllFragmentScrollPosition? = null
    private var feedRepostMultipleImageFragment: FeedRepostMultipleImageFragment? = null
    private var blockedUserIds = mutableSetOf<String>()
    private var isFragmentOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = TransitionInflater.from(requireContext())
        exitTransition = inflater.inflateTransition(R.transition.feed_fragment_fade)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        EventBus.getDefault().register(this)

        relationshipsViewModel.loadAllRelationships()
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

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch(Dispatchers.IO) {
            loadBlockedUsers()
        }


        var isScrollingDown = false
        feedUploadRepository = FeedUploadRepository()
        feedListView = view.findViewById(R.id.rv)
        progressBar = view.findViewById(R.id.progressBar)
        frameLayout = view.findViewById(R.id.feed_text_view_fragment)
        allFeedAdapter = FeedAdapter(
            requireActivity(),
            retrofitInstance,
            this@AllFragment,
            fragmentManager = childFragmentManager
        )

        allFeedAdapter.initialize(viewLifecycleOwner, dialogViewModel)

        Log.d("RecyclerViewTwo", "Adapter set: $allFeedAdapter")
        feedListView.adapter = allFeedAdapter
        Log.d("RecyclerViewTwo", "Adapter set: $allFeedAdapter")
        feedListView.layoutManager = LinearLayoutManager(requireContext())
        Log.d("RecyclerViewDebug", "Adapter set: ${true}")
        // Check if the data is empty before loading
        if (getFeedViewModel.getAllFeedData().isEmpty()) {
            getAllFeed(allFeedAdapter.startPage)

        } else {
            // Don't fetch data again if the ViewModel already has data
            allFeedAdapter.submitItems(getFeedViewModel.getAllFeedData())
            Log.d(TAG, "Data already available, using the cached data")
        }
        allFeedAdapter.setOnPaginationListener(object : com. uyscuti. sharedmodule. adapter. FeedPaginatedAdapter. OnPaginationListener {
            override fun onCurrentPage(page: Int) {
                Log.d(TAG, "Feed Feed currentPage: page number $page")
            }

            override fun onNextPage(page: Int) {
                lifecycleScope.launch(Dispatchers.Main) {
                    Log.d(TAG, "Feed Feed  onNextPage: page number $page")
                    getAllFeed(page)
                }
            }

            override fun onFinish() {
                Log.d(TAG, "Feed Feed  finished: page number")
            }
        })
        allFeedAdapter.recyclerView = feedListView
        feedListView.itemAnimator = null
        feedListView.addOnChildAttachStateChangeListener(object :
            RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {
                val position = feedListView.getChildAdapterPosition(view)
                Log.d("RecyclerView", "View attached at position: $position")
                // Handle the item being displayed
            }

            override fun onChildViewDetachedFromWindow(view: View) {
                val position = feedListView.getChildAdapterPosition(view)
                Log.d("RecyclerView", "View detached from position: $position")
                // Handle the item being hidden
            }
        })
        feedListView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            private var totalDy = 0 // Track accumulated scroll distance

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                // You can handle scroll state changes here if needed
                Log.d("RecyclerView", "Scroll state changed: $newState")
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val adapter = recyclerView.adapter

                // Ensure there is data in the adapter before modifying FAB visibility
                if (adapter != null && adapter.itemCount > 0) {
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                    getFeedViewModel.allFeedDataLastViewPosition = firstVisibleItemPosition + 1
                    getFeedViewModel.allFeedDataLastViewPosition = lastVisibleItemPosition + 1

                    if (dy > 5 && !isScrollingDown) {
                        // Scrolling down → Hide FAB & BottomNav
                        isScrollingDown = true
                        EventBus.getDefault().post(HideFeedFloatingActionButton())
                        EventBus.getDefault().post(HideBottomNav())
                    } else if (dy < -5 && isScrollingDown) {
                        // Scrolling up (slightly) → Show FAB & BottomNav immediately
                        isScrollingDown = false
                        EventBus.getDefault().post(ShowFeedFloatingActionButton(false))
                        EventBus.getDefault().post(ShowBottomNav(false))
                    }
                } else {
                    // No data, make sure the FAB remains hidden
                    EventBus.getDefault().post(HideFeedFloatingActionButton())
                }
            }


        })
        //      allFeedAdapterRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        lifecycleScope.launch(Dispatchers.Main) {
            //            Log.d(TAG, "onCreateView: ${getFeedViewModel.getAllFeedData()}")
            if (getFeedViewModel.getAllFeedData().isEmpty()) {
                //              Log.d(TAG, "onCreateView: get all feed data is empty")
                getAllFeed(allFeedAdapter.startPage)
            } else {
                Log.d(TAG, "onCreateView: get all feed data is not empty")
            }
            getFeedViewModel.isFeedDataAvailable.observe(viewLifecycleOwner)
            { isDataAvailable ->
                // Handle the updated value of isResuming here
                if (isDataAvailable) {

                    allFeedAdapter.submitItems(getFeedViewModel.getAllFeedData())
                    allFeedAdapter.addFollowList(getFeedViewModel.getFollowList())
                    allFeedAdapter.notifyDataSetChanged()
                    if (positionFromShorts?.setPosition == true) {
                        Log.i(
                            TAG,
                            "onCreateView: positionFromShorts!!.allFragmentFeedPosition " +
                                    "${positionFromShorts!!.allFragmentFeedPosition}"
                        )
                        feedListView.scrollToPosition(
                            positionFromShorts!!.allFragmentFeedPosition)

                        val feedPostData =
                            getFeedViewModel.getAllFeedDataByPosition(
                                positionFromShorts!!.allFragmentFeedPosition)
                        feedFileClicked(
                            positionFromShorts!!.allFragmentFeedPosition, feedPostData)
                        val feedRepostData =
                            getFeedViewModel.getAllFeedRepostDataByPosition(
                                positionFromShorts!!.allFragmentFeedPosition)
                        feedRepostFileClicked(
                            positionFromShorts!!.allFragmentFeedPosition,
                            feedRepostData
                        )

                    } else {
                        Log.i(
                            TAG,
                            "onCreateView: getFeedViewModel.allFeedDataLastViewPosition " +
                                    "${getFeedViewModel.allFeedDataLastViewPosition}"
                        )

                        feedListView.scrollToPosition(
                            getFeedViewModel.allFeedDataLastViewPosition)
                    }

                    getFeedViewModel.setIsDataAvailable(false)
                } else {
                    // Do something when isResuming is false
                    Log.d(TAG, "onCreateView: data not added")
                }
            }

            getFeedViewModel.isSingleFeedAvailable.observe(
                viewLifecycleOwner) { isDataAvailable ->
                // Handle the updated value of isResuming here
                if (isDataAvailable) {
                    // Do something when isResuming is true
                    Log.d(TAG, "getPosts: data is available")
                    allFeedAdapter.submitItems(getFeedViewModel.getAllFeedData())
                    allFeedAdapter.submitItem(getFeedViewModel.getSingleAllFeedData(), 0)
                    allFeedAdapter.addFollowList(getFeedViewModel.getFollowList())
                    feedListView.smoothScrollToPosition(0)
                    getFeedViewModel.setIsDataAvailable(false)
                } else {
                    // Do something when isResuming is false
                    Log.d(TAG, "onCreateView: data not added")

                }
            }
        }
        Log.d(TAG, "onCreateView: currentAdapterPosition $currentAdapterPosition")

        observeRelationships()
        Log.d(TAG, "Observing the Relationships the User have....")
    }

    // Add this new method to observe relationships
    private fun observeRelationships() {
        // Observe loading state
        lifecycleScope.launch {
            relationshipsViewModel.isLoading.collect { isLoading ->
                if (isLoading) {
                    Log.d(TAG, "Loading relationships...")
                } else {
                    Log.d(TAG, "Relationships loaded")
                }
            }
        }

        // Observe close friends
        lifecycleScope.launch {
            relationshipsViewModel.closeFriendIds.collect { closeFriends ->
                Log.d(TAG, "Close friends updated: ${closeFriends.size}")
            }
        }

        // Observe muted posts
        lifecycleScope.launch {
            relationshipsViewModel.mutedPostsIds.collect { mutedPosts ->
                Log.d(TAG, "Muted posts updated: ${mutedPosts.size}")
            }
        }

        // Observe muted stories
        lifecycleScope.launch {
            relationshipsViewModel.mutedStoriesIds.collect { mutedStories ->
                Log.d(TAG, "Muted stories updated: ${mutedStories.size}")
            }
        }

        // Observe favorites
        lifecycleScope.launch {
            relationshipsViewModel.favoriteIds.collect { favorites ->
                Log.d(TAG, "Favorites updated: ${favorites.size}")
            }
        }

        // Observe restricted
        lifecycleScope.launch {
            relationshipsViewModel.restrictedIds.collect { restricted ->
                Log.d(TAG, "Restricted users updated: ${restricted.size}")
            }
        }
    }

    // Update the getAllFeed method to filter based on relationships
    fun getAllFeed(page: Int) {
        val TAG = "AllFeedTag"
        Log.d(TAG, "getAllFeed: page number $page")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = retrofitInstance.apiService.getAllFeed(page.toString())
                val responseBody = response.body()

                Log.d(TAG, "Feed Feed getAllFeed feed: response $response")

                val posts = responseBody!!.data.data.posts

                // Filter out posts based on multiple criteria
                val filteredPosts = posts.filter { post ->
                    val authorId = post.author?.account?._id
                    val reposterId = post.repostedUser?.owner

                    val posterId = reposterId ?: authorId

                    // Check if user is blocked
                    val isBlocked = posterId?.let { blockedUserIds.contains(it) } ?: false

                    // Check if posts are muted
                    val isPostsMuted = posterId?.let {
                        relationshipsViewModel.isPostsMuted(it)
                    } ?: false

                    // Check if user is restricted (optional: you may want to show restricted user posts differently)
                    val isRestricted = posterId?.let {
                        relationshipsViewModel.isRestricted(it)
                    } ?: false

                    // Filter logic
                    val shouldFilter = isBlocked || isPostsMuted

                    if (shouldFilter) {
                        Log.d(TAG, "Filtering out post from user: $posterId " +
                                "(blocked: $isBlocked, muted: $isPostsMuted, restricted: $isRestricted)")
                    }

                    !shouldFilter
                }

                // Optional: Sort posts to prioritize close friends and favorites
                val sortedPosts = filteredPosts.sortedByDescending { post ->
                    val authorId = post.author?.account?._id ?: return@sortedByDescending 0

                    val isCloseFriend = relationshipsViewModel.isCloseFriend(authorId)
                    val isFavorite = relationshipsViewModel.isFavorite(authorId)

                    // Priority: close friends (3) > favorites (2) > regular (1)
                    when {
                        isCloseFriend -> 3
                        isFavorite -> 2
                        else -> 1
                    }
                }

                Log.d(TAG, "Original posts: ${posts.size}, After filtering: ${filteredPosts.size}, After sorting: ${sortedPosts.size}")

                withContext(Dispatchers.Main) {
                    getFeedViewModel.addAllFeedData(sortedPosts.toMutableList())
                    allFeedAdapter.submitItems(getFeedViewModel.getAllFeedData())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in getAllFeed: $e")
                Log.e(TAG, "Error message: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private suspend fun loadBlockedUsers() {
        try {
            Log.d(TAG, "Loading blocked users...")

            val response = retrofitInstance.apiService.getAllBlockedUsers(page = 1, limit = 100)

            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!

                // Clear existing and add new blocked user IDs
                blockedUserIds.clear()
                blockedUserIds.addAll(responseBody.data.blockedUsers.map { it.user._id })

                Log.d(TAG, "Loaded ${blockedUserIds.size} blocked users")
                Log.d(TAG, "Blocked user IDs: $blockedUserIds")
            } else {
                Log.e(TAG, "Failed to load blocked users: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading blocked users: ${e.message}", e)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun successEvent(event: FeedUploadProgress) {
        progressBar.max = event.maxProgress
        progressBar.progress = event.currentProgress
    }

    private var hasNotifiedDatasetChanged = false

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun feedAdapterNotifyDatasetChanged(event: FeedAdapterNotifyDatasetChanged) {

        Log.d(
            TAG,
            "FeedAdapterNotifyDatasetChanged: in feed adapter notify adapter: seh data set changed"
        )
        allFeedAdapter.notifyDataSetChanged()

        // Set the flag to true after executing the function
        hasNotifiedDatasetChanged = true
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: called")
        isFragmentOpen = false
        EventBus.getDefault().unregister(this)
    }

    override fun onDetach() {
        super.onDetach()
        Log.d(TAG, "onDetach: called")

    }


    override fun onResume() {
        super.onResume()
        getFeedViewModel.isResuming = true
        Log.d("getCurrentLocation", "onResume: ${getFeedViewModel.isResuming}")
        Log.d(TAG, "onResume: currentAdapterPosition $currentAdapterPosition")
        Log.d(TAG, "onResume: called")
        feedListView.visibility = View.VISIBLE
        frameLayout.visibility = View.GONE
        EventBus.getDefault().post(ShowBottomNav(false))

        EventBus.getDefault().post(HideFeedFloatingActionButton())
        EventBus.getDefault().post(ShowAppBar(false))

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

    // Call this method when you need to get the position
    private fun getCurrentFeedPosition(): Int? {
        val layoutManager = feedListView.layoutManager as LinearLayoutManager
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
        // You can determine which item is considered as "current" based on your criteria

        return firstVisibleItemPosition.takeIf { it <= lastVisibleItemPosition }

    }



    override fun likeUnLikeFeed(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.Post
    ) {
        Log.d("likeUnLikeFeed", "likeUnLikeFeed: $data")
        try {
            val updatedComment = if (data.isLiked) {
                data.copy(
                    likes = data.likes + 1,
                    repostedByUserId = data.repostedByUserId ?: "",
                    content = data.content ?: "",
                    contentType = data.contentType ?: "",
                    isLiked = true,

                    )
            } else {
                data.copy(
                    likes = data.likes - 1,
                    repostedByUserId = data.repostedByUserId ?: "",
                    content = data.content ?: "",
                    contentType = data.contentType ?: "",
                    isLiked = false,
                )
            }
            lifecycleScope.launch {
                feedUploadViewModel.likeUnLikeFeed(data._id)
            }
            Log.d("likeUnLikeFeed", "likeUnLikeFeed: likes count is ${data.likes}")
            val updatedItems = getFeedViewModel.getAllFeedData()

            for (updatedItem in updatedItems) {

                if (updatedItem._id == data._id) {
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


    override fun feedCommentClicked(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.Post
    ) {
        //implemented in main activity kt
        Log.d(TAG, "feedCommentClick: this is the one listening")
        EventBus.getDefault().post(
            FeedCommentClicked(
                position,
                data
            )
        )
    }

    private fun hidingBottomNav() {
        EventBus.getDefault().post(HideBottomNav())
    }

    override fun feedFavoriteClick(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.Post
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
//        val feedPosition = allFeedAdapter.getPositionById(event.data._id)
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

    @SuppressLint("InflateParams", "MissingInflatedId", "ServiceCast")
    override fun moreOptionsClick(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.Post
    ) {
        Log.d(TAG, "moreOptionsClick: More options clicked")
        val view: View = layoutInflater.inflate(R.layout.feed_more_options_layout, null)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(view)

        // Get all views from XML
        val downloadFiles: View = view.findViewById(R.id.downloadAction)
        val followUnfollowLayout: View = view.findViewById(R.id.followAction)
        val reportUser: View = view.findViewById(R.id.reportOptionLayout)
        val hidePostLayout: View = view.findViewById(R.id.hidePostLayout)
        val copyLink: View = view.findViewById(R.id.copyLinkLayout)
        val muteOptionLayout: MaterialCardView = view.findViewById(R.id.muteOptionLayout)
        val blockUserLayout: MaterialCardView = view.findViewById(R.id.blockUserLayout)
        val quoteFeedLayout: View = view.findViewById(R.id.repostAction)
        val shareAction: View = view.findViewById(R.id.shareAction)
        val notInterested: View = view.findViewById(R.id.notInterestedLayout)

        // Get the author ID and username
        val authorId = data.author?.account?._id
        val username = data.author?.account?.username ?: "User"

        // Update mute button text based on current state
        if (authorId != null) {
            // Find the nested LinearLayout inside muteOptionLayout
            val muteCard = muteOptionLayout.getChildAt(0) as? LinearLayout
            val muteTextContainer = muteCard?.getChildAt(1) as? LinearLayout
            val muteStaticText = muteTextContainer?.getChildAt(0) as? TextView
            val muteUsernameText = muteTextContainer?.getChildAt(1) as? TextView

            if (relationshipsViewModel.isPostsMuted(authorId)) {
                muteStaticText?.text = "Unmute "
                muteUsernameText?.text = username
            } else {
                muteStaticText?.text = "Mute "
                muteUsernameText?.text = username
            }
        }

        // Update block button username text
        val blockCard = blockUserLayout.getChildAt(0) as? LinearLayout
        val blockContentLayout = blockCard?.getChildAt(1) as? LinearLayout
        val blockDescriptionLayout = blockContentLayout?.getChildAt(1) as? LinearLayout
        val usernameBlockText = blockDescriptionLayout?.findViewById<TextView>(R.id.usernameBlock)
        usernameBlockText?.text = username

        // Show the dialog
        dialog.show()

        // ==================== SHARE ACTION ====================
        shareAction.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, data.content)
            startActivity(Intent.createChooser(shareIntent, "Share via"))
            dialog.dismiss()
        }

        // ==================== DOWNLOAD ACTION ====================
        downloadFiles.setOnClickListener {
            Log.d("DownloadButton", "Data: $data")
            if (data.files.isNotEmpty()) {
                onDownloadClick(data.files[0].url, "FlashShorts")
            } else {
                Toast.makeText(context, "No files to download", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        // Hide download if text-only post
        if (data.contentType == "text") {
            downloadFiles.visibility = View.GONE
        }

        // ==================== MUTE ACTION ====================
        muteOptionLayout.setOnClickListener {
            Log.d("MuteButton", "Mute button clicked for user: $authorId")
            dialog.dismiss()

            authorId?.let { userId ->
                handleMuteToggle(userId, position)
            } ?: run {
                Toast.makeText(context, "Cannot mute: User ID not found", Toast.LENGTH_SHORT).show()
            }
        }

        // ==================== BLOCK USER ACTION ====================
        blockUserLayout.setOnClickListener {
            Log.d("BlockButton", "Block button clicked for user: $authorId")
            dialog.dismiss()

            authorId?.let { userId ->
                // Check if user is trying to block themselves
                val currentUserId = getCurrentUserId() // You need to implement this method
                if (userId == currentUserId) {
                    Toast.makeText(context, "You cannot block yourself", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Check if user is already blocked
                if (blockedUserIds.contains(userId)) {
                    // Unblock the user
                    handleUnblockUser(userId, username)
                } else {
                    // Block the user
                    showBlockConfirmationDialog(userId, username, position)
                }
            } ?: run {
                Toast.makeText(context, "Cannot block: User ID not found", Toast.LENGTH_SHORT).show()
            }
        }

        // ==================== REPOST ACTION ====================
        quoteFeedLayout.setOnClickListener {
            val fragment = Fragment_Edit_Post_To_Repost(data)
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
            dialog.dismiss()
        }

        // ==================== COPY LINK ACTION ====================
        copyLink.setOnClickListener {
            val postId = data._id
            val linkToCopy = "https://circuitSocial.app/post/$postId"
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = android.content.ClipData.newPlainText("Copied Link", linkToCopy)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(requireContext(), "Link copied to clipboard", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        // ==================== NOT INTERESTED ACTION ====================
        notInterested.setOnClickListener {
            handleNotInterested(data)
            dialog.dismiss()
        }

        // ==================== HIDE POST ACTION ====================
        hidePostLayout.setOnClickListener {
            Log.d(TAG, "hidePostLayout: hide post clicked")
            hideSinglePost(position, data)
            dialog.dismiss()
        }

        // ==================== REPORT USER ACTION ====================
        reportUser.setOnClickListener {
            Log.d("reportUser", "Report button clicked")
            val intent = Intent(requireActivity(), ReportNotificationActivity2::class.java)
            startActivityForResult(intent, REQUEST_REPOST_FEED_ACTIVITY)
            dialog.dismiss()
        }

        // Hide follow button (you can show it if needed based on relationship status)
        followUnfollowLayout.visibility = View.GONE
    }

    // ==================== MUTE TOGGLE ====================
    private fun handleMuteToggle(userId: String, position: Int) {
        lifecycleScope.launch {
            try {
                if (relationshipsViewModel.isPostsMuted(userId)) {
                    // Unmute
                    val response = retrofitInstance.apiService.unMutePosts(userId)
                    if (response.isSuccessful) {
                        relationshipsViewModel.removeMutedPosts(userId)
                        Toast.makeText(context, "Posts unmuted", Toast.LENGTH_SHORT).show()
                        // Refresh feed to show posts again
                        allFeedAdapter.notifyDataSetChanged()
                    }
                } else {
                    // Mute
                    val response = retrofitInstance.apiService.mutePosts(userId)
                    if (response.isSuccessful) {
                        relationshipsViewModel.addMutedPosts(userId)

                        // Remove the post from the adapter
                        allFeedAdapter.removeItem(position)
                        allFeedAdapter.notifyItemRemoved(position)

                        // Show Snackbar with Undo
                        Snackbar.make(feedListView, "Posts from this user muted", Snackbar.LENGTH_LONG)
                            .setAction("Undo") {
                                // Unmute the user
                                lifecycleScope.launch {
                                    val undoResponse = retrofitInstance.apiService.unMutePosts(userId)
                                    if (undoResponse.isSuccessful) {
                                        relationshipsViewModel.removeMutedPosts(userId)
                                        Toast.makeText(context, "Unmuted", Toast.LENGTH_SHORT).show()
                                        // Reload feed
                                        getAllFeed(1)
                                    }
                                }
                            }
                            .show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling mute: ${e.message}", e)
                Toast.makeText(context, "Failed to update mute status", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ==================== BLOCK USER CONFIRMATION ====================
    private fun showBlockConfirmationDialog(userId: String, username: String, position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Block $username?")
            .setMessage("You won't be able to see or contact $username. They won't be notified that you blocked them.")
            .setPositiveButton("Block") { dialog, _ ->
                handleBlockUser(userId, position)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    // ==================== BLOCK USER ====================
    private fun handleBlockUser(userId: String, position: Int) {
        lifecycleScope.launch {
            try {
                val response = retrofitInstance.apiService.blockUser(userId)
                if (response.isSuccessful) {
                    // Add to blocked list
                    blockedUserIds.add(userId)

                    // Remove all posts from this user
                    val itemsToRemove = mutableListOf<Int>()
                    for (i in 0 until allFeedAdapter.itemCount) {
                        val item = getFeedViewModel.getAllFeedData().getOrNull(i)
                        val itemAuthorId = item?.author?.account?._id
                        if (itemAuthorId == userId) {
                            itemsToRemove.add(i)
                        }
                    }

                    // Remove items in reverse order to maintain indices
                    itemsToRemove.reversed().forEach { pos ->
                        allFeedAdapter.removeItem(pos)
                        getFeedViewModel.removeAllFeedFragment(pos)
                    }

                    allFeedAdapter.notifyDataSetChanged()
                    Toast.makeText(context, "User blocked", Toast.LENGTH_SHORT).show()

                    // Show Snackbar with Undo
                    Snackbar.make(feedListView, "User blocked", Snackbar.LENGTH_LONG)
                        .setAction("Undo") {
                            lifecycleScope.launch {
                                val unblockResponse = retrofitInstance.apiService.unBlockUser(userId)
                                if (unblockResponse.isSuccessful) {
                                    blockedUserIds.remove(userId)
                                    Toast.makeText(context, "User unblocked", Toast.LENGTH_SHORT).show()
                                    // Reload feed
                                    getAllFeed(1)
                                }
                            }
                        }
                        .show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error blocking user: ${e.message}", e)
                Toast.makeText(context, "Failed to block user", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ==================== GET CURRENT USER ID ====================
   // Helper method to get the current logged-in user's ID
    private fun getCurrentUserId(): String? {
        // Use UserStorageHelper from LoginActivity
        val userId = LoginActivity.UserStorageHelper.getUserId(requireContext())
        return if (userId.isNotEmpty()) userId else null
    }

    // ==================== UNBLOCK USER ====================
    private fun handleUnblockUser(userId: String, username: String) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    retrofitInstance.apiService.unBlockUser(userId)
                }

                if (response.isSuccessful) {
                    // Remove from blocked list
                    blockedUserIds.remove(userId)

                    Toast.makeText(
                        context,
                        "Unblocked @$username",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Show Snackbar with option to undo
                    Snackbar.make(
                        feedListView,
                        "User unblocked",
                        Snackbar.LENGTH_LONG
                    ).setAction("Undo") {
                        // Re-block the user
                        lifecycleScope.launch {
                            try {
                                val blockResponse = retrofitInstance.apiService.blockUser(userId)
                                if (blockResponse.isSuccessful) {
                                    blockedUserIds.add(userId)
                                    Toast.makeText(
                                        context,
                                        "User blocked again",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error re-blocking user: ${e.message}", e)
                                Toast.makeText(
                                    context,
                                    "Failed to block user",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }.show()

                    // Reload feed to potentially show the user's posts again
                    getAllFeed(1)
                } else {
                    Toast.makeText(
                        context,
                        "Failed to unblock user",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error unblocking user: ${e.message}", e)
                Toast.makeText(
                    context,
                    "Network error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
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
        data: com.uyscuti.social.network.api.response.posts.Post
    ) {
        Log.d(TAG,
            "hideSinglePost: Hiding post at position: $position, PostId: ${data._id}")
        try {
            if (::allFeedAdapter.isInitialized) {

//                feedListView.removeViewAt( position )
                allFeedAdapter.removeItem(position)
                allFeedAdapter.notifyItemRemoved(position)
//                allFeedAdapter.notifyItemChanged(position)
                // Optional: Add fade-out animation
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
                        // Restore the post
//                        favoriteFeedAdapter.restoreItem(position, data)
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
        data: com.uyscuti.social.network.api.response.posts.Post) {

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
//        builder.setTitle("Delete Feed Confirmation")
        builder.setCustomTitle(customTitleView)
        builder.setMessage("Are you sure you want to delete this feed?")

        // Positive Button
        builder.setPositiveButton("Delete") { dialog, which ->
//             Handle delete action

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
        // Logic to delete the item
        // e.g., remove it from a list or database
        Log.d(TAG, "handleDeleteAction: remove from database")
        lifecycleScope.launch {
            val response = retrofitInstance.apiService.deleteFeed(feedId)
            Log.d(TAG, "handleDeleteAction: $response")
            Log.d(TAG, "handleDeleteAction body: ${response.body()}")
            Log.d(TAG, "handleDeleteAction isSuccessful: ${response.isSuccessful}")
            if (response.isSuccessful) {
                getFeedViewModel.removeMyFeed(position)
                allFeedAdapter.removeItem(position)


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
//                        getFeedViewModel.setRefreshMyData(pos, true)
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

    override fun feedFileClicked(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {
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

        // Set listener if your fragment implements one
        // tappedFilesFragment.setListener(this)

        // Replace fragment
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, tappedFilesFragment)
            .addToBackStack(null)
            .commit()
    }
    override fun feedRepostFileClicked(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.OriginalPost
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

        // Set listener if your fragment implements one
        // tappedFilesFragment.setListener(this)

        // Replace fragment
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, tappedFilesFragment)
            .addToBackStack(null)
            .commit()
    }


    override fun feedShareClicked(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.Post
    ) {
        val context = requireContext()

        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val shareView = layoutInflater.inflate(R.layout.example, null)
        val close_button = shareView.findViewById<ImageButton>(R.id.close_button)
        val recyclerView = shareView.findViewById<RecyclerView>(R.id.apps_recycler_view)
        val userRecyclerView = shareView.findViewById<RecyclerView>(R.id.users_recycler_view)

        bottomSheetDialog.setContentView(shareView)
        bottomSheetDialog.show()

        close_button.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        // Fetch installed apps that support sharing
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain" }
        val resolveInfoList =
            packageManager?.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)

        // Set up RecyclerView
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = resolveInfoList?.let {
            ShareFeedPostAdapter(it, context, data) }
        userRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        userRecyclerView.adapter = UserListAdapter(context) { user ->

        }
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

    @SuppressLint("MissingInflatedId", "CommitTransaction")
    override fun feedRepostPost(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.Post
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

    override fun feedRepostPostClicked(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.Post
    ) {
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
//            delay(200)
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

    override fun onCommentClickFromFeedTextViewFragment(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.Post
    ) {
        EventBus.getDefault().post(
            FeedCommentClicked(
                position,
                data
            )
        )
        Log.d(TAG, "onCommentClickFromFeedTextViewFragment: comment clicked")
    }

    override fun onLikeUnLikeFeedFromFeedTextViewFragment(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.Post
    ) {
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

    override fun onFeedFavoriteClickFromFeedTextViewFragment(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.Post
    ) {
        EventBus.getDefault().post(FeedFavoriteClick(position, data))
//        EventBus.getDefault().post(FromFavoriteFragmentFeedFavoriteClick(position, data))
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
    override fun onMoreOptionsClickFromFeedTextViewFragment(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.Post
    ) {
        val view: View = layoutInflater.inflate(R.layout.more_options_redesign_layout, null)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(view)
        dialog.show()
        val reportOptionLayout: LinearLayout = view.findViewById(R.id.reportOption)
        val hidePostLayout: LinearLayout = view.findViewById(R.id.hidePostOption)
//        val muteOptionLayout : LinearLayout = view.findViewById(R.id.muteOptionLayout)
        val followUnfollowLayout: LinearLayout = view.findViewById(R.id.followUnfollowOption)
        val notInterestedLayout: LinearLayout = view.findViewById(R.id.notInterestedOption)
        notInterestedLayout.visibility = View.GONE
        hidePostLayout.visibility = View.GONE
        followUnfollowLayout.visibility = View.GONE
//        muteOptionLayout.visibility = View.GONE
        hidePostLayout.setOnClickListener {
            Log.d("HideLayout", "has been clicked")
//            showDeleteConfirmationDialog(data._id, position)
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

    override fun onRePostClickFromFeedTextViewFragment(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {
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
//        Log.d("AllFeedUpdateLike", "AllFeedUpdateLike: in all fragment")
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
