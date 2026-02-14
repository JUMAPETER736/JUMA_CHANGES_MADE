package com.uyscuti.social.circuit.ui.fragments.feed

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresExtension
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.FeedAudioViewFragment
import com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.FeedImageViewFragment
import com.uyscuti.social.circuit.adapter.feed.ShareFeedPostAdapter
import com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.FeedMultipleImageViewFragment
import com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.FeedTextViewFragment
import com.uyscuti.sharedmodule.ReportNotificationActivity2
import com.uyscuti.sharedmodule.adapter.feed.FeedAdapter
import com.uyscuti.sharedmodule.model.feed.SetAllFragmentScrollPosition
import com.uyscuti.sharedmodule.ui.fragments.feed.feedviewfragments.FeedDocumentViewFragment
//import com.uyscuti.sharedmodule.ui.fragments.feed.feedviewfragments.FeedMixedFilesViewFragment
import com.uyscuti.sharedmodule.ui.fragments.feed.feedviewfragments.Fragment_Original_Post_With_Repost_Inside
import com.uyscuti.sharedmodule.viewmodels.feed.GetFeedViewModel
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.feed.FeedUploadRepository
import com.uyscuti.sharedmodule.adapter.feed.OnFeedClickListener
import com.uyscuti.sharedmodule.eventbus.FeedFavoriteClick
import com.uyscuti.sharedmodule.eventbus.FeedLikeClick
import com.uyscuti.sharedmodule.eventbus.HideFeedFloatingActionButton
import com.uyscuti.sharedmodule.eventbus.ShowFeedFloatingActionButton
import com.uyscuti.sharedmodule.fragments.CommentsBottomSheet
import com.uyscuti.social.circuit.ui.feedactivities.FeedVideoViewFragment
import com.uyscuti.social.circuit.ui.fragments.feed.feedRepostViewFragments.FeedRepostAudioViewFragment
import com.uyscuti.social.circuit.ui.fragments.feed.feedRepostViewFragments.FeedRepostDocFragment
import com.uyscuti.social.circuit.ui.fragments.feed.feedRepostViewFragments.FeedRepostImageFragment
import com.uyscuti.social.circuit.ui.fragments.feed.feedRepostViewFragments.FeedRepostTextFragment
import com.uyscuti.social.circuit.ui.fragments.feed.feedRepostViewFragments.FeedRepostVideoViewFragment
import com.uyscuti.social.circuit.ui.fragments.feed.feedRepostViewFragmentsimport.FeedRepostMultipleImageFragment
import com.uyscuti.sharedmodule.interfaces.feedinterfaces.FeedTextViewFragmentInterface
import com.uyscuti.sharedmodule.model.FeedCommentClicked
import com.uyscuti.sharedmodule.model.HideAppBar
import com.uyscuti.sharedmodule.model.HideBottomNav
import com.uyscuti.sharedmodule.model.ShortsFollowButtonClicked
import com.uyscuti.sharedmodule.model.ShowAppBar
import com.uyscuti.sharedmodule.model.ShowBottomNav
import com.uyscuti.sharedmodule.ui.fragments.feed.feedviewfragments.NewRepostedPostFragment
import com.uyscuti.sharedmodule.viewmodels.feed.FeedUploadViewModel
import com.uyscuti.social.circuit.log_in_and_register.LoginActivity.UserStorageHelper.getUserId
import com.uyscuti.social.circuit.log_in_and_register.LoginActivity.UserStorageHelper.getUsername
import com.uyscuti.social.core.common.data.room.entity.FollowUnFollowEntity
import com.uyscuti.social.network.api.response.comment.allcomments.Comment
import com.uyscuti.social.network.api.response.posts.Post
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import java.util.ArrayList
import kotlin.collections.MutableList



private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val REQUEST_REPOST_FEED_ACTIVITY = 1020


private const val TAG = "FollowingFragment"
@AndroidEntryPoint
class FollowingFragment : Fragment(), OnFeedClickListener, FeedTextViewFragmentInterface {



    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FollowingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }

            }
    }

    private lateinit var frameLayout:FrameLayout
    private lateinit var followedPostsAdapter: FeedAdapter
    private lateinit var feedListView: RecyclerView
    private lateinit var progressBar: ProgressBar
    var currentAdapterPosition = -1
    private lateinit var feedUploadRepository: FeedUploadRepository
    private var myUserId: String = ""

    private var followingUserIds = mutableSetOf<String>()
    private var blockedUserIds = mutableSetOf<String>()
    private val followingUserMap = mutableMapOf<String, String>()
    private val feedUploadViewModel: FeedUploadViewModel by activityViewModels()
    private val getFeedViewModel: GetFeedViewModel by activityViewModels()

    private var isLoading = false
    private var hasLoadedFollowingList = false
    private var hasMoreData = true
    var bitmap: Bitmap? = null

    private var param1: String? = null
    private var param2: String? = null
    private var positionFromShorts: SetAllFragmentScrollPosition? = null
    private var feedVideoViewFragment: FeedVideoViewFragment? = null
    private var feedTextViewFragment: FeedTextViewFragment?= null
    private var feedAudioViewFragment: FeedAudioViewFragment? = null
    private var feedMultipleImageViewFragment: FeedMultipleImageViewFragment? = null


    private var feedDocsViewFragment: FeedDocumentViewFragment? = null
    private var feedImageViewFragment: FeedImageViewFragment? = null
    private var feedRepostDocFragment : FeedRepostDocFragment? = null
    private var feedRepostTextFragment : FeedRepostTextFragment? = null
    private var feedRepostVideoViewFragment : FeedRepostVideoViewFragment? = null
    private var feedRepostAudioViewFragment : FeedRepostAudioViewFragment? = null
    private var feedRepostImageFragment : FeedRepostImageFragment? = null
    private var feedRepostMultipleImageFragment: FeedRepostMultipleImageFragment? = null
    private var fragmentOriginalPostWithRepostInside: Fragment_Original_Post_With_Repost_Inside? = null



    @Inject
    lateinit var retrofitInstance: RetrofitInstance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    @SuppressLint("MissingInflatedId", "CutPasteId")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_following, container, false)

    }


    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        var isScrollingDown = false
        val scrollThreshold = 10

        lifecycleScope.launch(Dispatchers.IO) {

            loadMyFollowersList()
            loadBlockedUsers()
        }

        feedUploadRepository = FeedUploadRepository()

        feedListView = view.findViewById(R.id.rvq)
        progressBar = view.findViewById(R.id.progressBar)
        frameLayout = view.findViewById(R.id.feed_text_view_fragment)

        myUserId = getUserId(requireContext())
        Log.d(TAG, "My user ID: $myUserId")

        followedPostsAdapter = FeedAdapter(
            requireActivity(),
            retrofitInstance,
            this@FollowingFragment,
            fragmentManager = childFragmentManager
        )

        Log.d("RecyclerViewTwo", "Adapter set: $followedPostsAdapter")
        feedListView.adapter = followedPostsAdapter

        Log.d("RecyclerViewTwo", "Adapter set: $followedPostsAdapter")

        feedListView.layoutManager = LinearLayoutManager(requireContext())
        Log.d("RecyclerViewDebug", "Adapter set: ${true}")

        // Check if the data is empty before loading
        // Check if the data is empty before loading
        if (getFeedViewModel.getAllFeedData().isEmpty()) {
            getAllFeed(followedPostsAdapter.startPage)
        } else {
            // Don't fetch data again if the ViewModel already has data
            followedPostsAdapter.submitItems(getFeedViewModel.getAllFeedData())
            Log.d(TAG, "Data already available, using the cached data")

            // Load followers cache even when using cached data
            lifecycleScope.launch(Dispatchers.IO) {
                loadMyFollowersList()
            }
        }

        followedPostsAdapter.setOnPaginationListener(
            object : com.uyscuti.sharedmodule.adapter.FeedPaginatedAdapter.OnPaginationListener {
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

        followedPostsAdapter.recyclerView = feedListView
        feedListView.itemAnimator = null
        feedListView.addOnChildAttachStateChangeListener(
            object : RecyclerView.OnChildAttachStateChangeListener {
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
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                getFeedViewModel.allFeedDataLastViewPosition = firstVisibleItemPosition + 1
                getFeedViewModel.allFeedDataLastViewPosition = lastVisibleItemPosition + 1


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

        lifecycleScope.launch(Dispatchers.Main) {
            // Filter own posts from cached data on view creation
            val currentUserId = getUserId(requireContext())
            getFeedViewModel.filterOutUserPosts(currentUserId)

            // Only check if data is already loaded
            if (getFeedViewModel.getAllFeedData().isNotEmpty()) {
                followedPostsAdapter.submitItems(getFeedViewModel.getAllFeedData())
                Log.d(TAG, "onCreateView: Using cached data (${getFeedViewModel.getAllFeedData().size} posts, own posts excluded)")
            }

            getFeedViewModel.isFeedDataAvailable.observe(viewLifecycleOwner) { isDataAvailable ->
                if (isDataAvailable) {
                    // Filter again when data becomes available
                    getFeedViewModel.filterOutUserPosts(currentUserId)

                    followedPostsAdapter.submitItems(getFeedViewModel.getAllFeedData())
                    followedPostsAdapter.addFollowList(getFeedViewModel.getFollowList())

                    if (positionFromShorts?.setPosition == true) {
                        Log.i(
                            TAG,
                            "onCreateView: positionFromShorts!!.allFragmentFeedPosition" +
                                    " ${positionFromShorts!!.allFragmentFeedPosition}"
                        )
                        feedListView.scrollToPosition(
                            positionFromShorts!!.allFragmentFeedPosition)

                        val feedPostData =
                            getFeedViewModel.getAllFeedDataByPosition(
                                positionFromShorts!!.allFragmentFeedPosition)
                        feedFileClicked(positionFromShorts!!.allFragmentFeedPosition,
                            feedPostData)
                        val feedRepostData =
                            getFeedViewModel.getAllFeedRepostDataByPosition(
                                positionFromShorts!!.allFragmentFeedPosition)
                        feedRepostFileClicked(
                            positionFromShorts!!.allFragmentFeedPosition, feedRepostData)

                    } else {
                        Log.i(
                            TAG,
                            "onCreateView: getFeedViewModel.allFeedDataLastViewPosition" +
                                    " ${getFeedViewModel.allFeedDataLastViewPosition}"
                        )

                        feedListView.scrollToPosition(
                            getFeedViewModel.allFeedDataLastViewPosition)
                    }

                    getFeedViewModel.setIsDataAvailable(false)
                } else {
                    Log.d(TAG, "onCreateView: data not added")
                }
            }

            getFeedViewModel.isFeedDataAvailable.observe(viewLifecycleOwner) { isDataAvailable ->
                if (isDataAvailable) {
                    getFeedViewModel.filterOutUserPosts(currentUserId)

                    val feedData = getFeedViewModel.getAllFeedData()

                    // DEBUG: Log what's about to be displayed
                    Log.d(TAG, "📱 DISPLAYING ${feedData.size} posts:")
                    feedData.forEachIndexed { index, post ->
                        val author = post.author?.account?.username ?: "Unknown"
                        val authorId = post.author?.account?._id ?: "null"
                        val isRepost = post.originalPost.isNotEmpty()
                        Log.d(TAG, "  [$index] @$author (ID: $authorId) ${if (isRepost) "[REPOST]" else ""}")
                    }

                    followedPostsAdapter.submitItems(feedData)
                    followedPostsAdapter.addFollowList(getFeedViewModel.getFollowList())


                    // Filter before displaying
                    getFeedViewModel.filterOutUserPosts(currentUserId)

                    followedPostsAdapter.submitItems(getFeedViewModel.getAllFeedData())
                    followedPostsAdapter.submitItem(
                        getFeedViewModel.getSingleAllFeedData(), 0)
                    followedPostsAdapter.addFollowList(getFeedViewModel.getFollowList())
                    feedListView.smoothScrollToPosition(0)
                    getFeedViewModel.setIsDataAvailable(false)
                } else {
                    Log.d(TAG, "onCreateView: data not added")
                }
            }
        }

        Log.d(TAG, "onCreateView: currentAdapterPosition $currentAdapterPosition")

    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun getAllFeed(page: Int) {

        Log.d(TAG, "getAllFeed: page number $page")
        Log.d(TAG, "Following ${followingUserIds.size} users")


        if (isLoading) {
            Log.d(TAG, "Already loading, skipping request")
            return
        }

        isLoading = true
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Load following list first if not loaded
                if (page == 1 && !hasLoadedFollowingList) {
                    Log.d(TAG, "First load - fetching following list...")
                    loadFollowingUserIds()
                    delay(500)

                    if (followingUserIds.isEmpty()) {
                        withContext(Dispatchers.Main) {
                            progressBar.visibility = View.GONE
                            isLoading = false

                        }
                        return@launch
                    }

                    // Load multiple pages initially to get posts from all followed users
                    Log.d(TAG, "Loading multiple pages to find posts from all ${followingUserIds.size} followed users...")
                    loadAllFollowingPostsInitially()
                    return@launch
                }

                // Regular single page load for pagination
                loadPostsFromFollowing(page)

            } catch (e: Exception) {
                Log.e(TAG, "Error loading posts: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    isLoading = false
                    handleError("Error: ${e.message}")
                }
            }
        }
    }

    private suspend fun loadAllFollowingPostsInitially() {

        val currentUserId = getUserId(requireContext())
        val allFollowingPosts = mutableListOf<Post>()
        var pageNum = 1
        val uniqueAuthors = mutableSetOf<String>()
        val maxPages = 20

        withContext(Dispatchers.Main) {
            progressBar.visibility = View.VISIBLE

            // Clear old cached data before loading new data
            getFeedViewModel.clearAllFeedData()
            followedPostsAdapter.submitItems(mutableListOf())
            Log.d(TAG, "CLEARED old cached posts from adapter and ViewModel")
        }


        Log.d(TAG, "SIMPLE FOLLOWING FEED RULE:")
        Log.d(TAG, "Following ${followingUserIds.size} users")
        Log.d(TAG, "ONLY show posts BY these ${followingUserIds.size} people")
        Log.d(TAG, "Don't care about reposts content - only WHO posted it")


        while (uniqueAuthors.size < followingUserIds.size && pageNum <= maxPages) {
            try {
                val response = retrofitInstance.apiService.getAllFeed(pageNum.toString())
                if (!response.isSuccessful || response.body() == null) break

                val pagePosts = response.body()!!.data.data.posts
                Log.d(TAG, "Page $pageNum: ${pagePosts.size} posts")

                val filtered = pagePosts.mapNotNull { post ->
                    try {


                        val posterAccountId: String
                        val posterUsername: String

                        if (post.repostedUser != null) {
                            // This is a REPOST - check who REPOSTED it (not original author)
                            posterAccountId = post.repostedUser!!.owner
                            posterUsername = post.repostedUser!!.username.trim().lowercase()
                            Log.d(TAG, "  REPOST by @${post.repostedUser!!.username} (ID: $posterAccountId)")
                        } else {
                            // This is an ORIGINAL POST - check the author
                            posterAccountId = post.author?.account?._id ?: return@mapNotNull null
                            posterUsername = post.author.account.username.trim().lowercase()
                            Log.d(TAG, "  ORIGINAL POST by @${post.author.account.username} (ID: $posterAccountId)")
                        }

                        // Skip own posts
                        if (posterAccountId == currentUserId) {
                            Log.d(TAG, "    ⊘ This is MY post - SKIPPING")
                            return@mapNotNull null
                        }

                        // Skip blocked users
                        if (blockedUserIds.contains(posterAccountId)) {
                            Log.d(TAG, "Skipping post from blocked user ID $posterAccountId")
                            return@mapNotNull null
                        }

                        // ═══════════════════════════════════════
                        // STEP 2: DO I FOLLOW THE POSTER?
                        // ═══════════════════════════════════════

                        val isFollowingById = followingUserIds.contains(posterAccountId)
                        val isFollowingByUsername = followingUserMap.values.any {
                            it.trim().lowercase() == posterUsername
                        }

                        Log.d(TAG, "    • Do I follow this person by ID? $isFollowingById")
                        Log.d(TAG, "    • Do I follow this person by username? $isFollowingByUsername")

                        if (isFollowingById || isFollowingByUsername) {
                            uniqueAuthors.add(posterAccountId)
                            Log.d(TAG, "    YES, I follow @$posterUsername - INCLUDE THIS POST")
                            return@mapNotNull post
                        } else {
                            Log.d(TAG, "    NO, I DON'T follow @$posterUsername - EXCLUDE THIS POST")
                            return@mapNotNull null
                        }

                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing post: ${e.message}")
                        return@mapNotNull null
                    }
                }

                allFollowingPosts.addAll(filtered)


                Log.d(TAG, "Page $pageNum Results:")
                Log.d(TAG, "  Total posts: ${pagePosts.size}")
                Log.d(TAG, "  Included: ${filtered.size}")
                Log.d(TAG, "  Excluded: ${pagePosts.size - filtered.size}")


                if (uniqueAuthors.size >= followingUserIds.size) break
                pageNum++
                delay(180)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading page $pageNum: ${e.message}")
                break
            }
        }

        withContext(Dispatchers.Main) {
            progressBar.visibility = View.GONE
            isLoading = false


            Log.d(TAG, "LOADING COMPLETE:")
            Log.d(TAG, "Total posts: ${allFollowingPosts.size}")
            Log.d(TAG, "ALL posts are BY people you follow")


            if (allFollowingPosts.isEmpty()) {
                // Make sure adapter is empty
                followedPostsAdapter.submitItems(mutableListOf())

            } else {
                // CRITICAL: Clear old data first, then add new filtered data
                getFeedViewModel.clearAllFeedData()
                getFeedViewModel.addAllFeedData(allFollowingPosts.toMutableList())
                getFeedViewModel.filterOutUserPosts(currentUserId)

                val finalPosts = getFeedViewModel.getAllFeedData()
                Log.d(TAG, "Submitting ${finalPosts.size} posts to adapter")
                followedPostsAdapter.submitItems(finalPosts)

                // Force adapter to refresh
                followedPostsAdapter.notifyDataSetChanged()
            }

            hasMoreData = allFollowingPosts.size >= 20
        }
    }

    private fun loadPostsFromFollowing(page: Int) {
        if (isLoading) return
        isLoading = true
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val currentUserId = getUserId(requireContext())
                val response = retrofitInstance.apiService.getAllFeed(page.toString())

                if (!response.isSuccessful || response.body() == null) {
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        isLoading = false
                    }
                    return@launch
                }

                val pagePosts = response.body()!!.data.data.posts

                val filtered = pagePosts.mapNotNull { post ->
                    try {
                        // WHO POSTED THIS?
                        val posterAccountId: String
                        val posterUsername: String

                        if (post.repostedUser != null) {
                            posterAccountId = post.repostedUser!!.owner
                            posterUsername = post.repostedUser!!.username.trim().lowercase()
                        } else {
                            posterAccountId = post.author?.account?._id ?: return@mapNotNull null
                            posterUsername = post.author.account.username.trim().lowercase()
                        }

                        // Skip own posts
                        if (posterAccountId == currentUserId) return@mapNotNull null

                        // Skip blocked users
                        if (blockedUserIds.contains(posterAccountId)) {
                            Log.d(TAG, "Skipping post from blocked user ID $posterAccountId")
                            return@mapNotNull null
                        }

                        // DO I FOLLOW THE POSTER?
                        val isFollowingById = followingUserIds.contains(posterAccountId)
                        val isFollowingByUsername = followingUserMap.values.any {
                            it.trim().lowercase() == posterUsername
                        }

                        return@mapNotNull if (isFollowingById || isFollowingByUsername) post else null

                    } catch (e: Exception) {
                        return@mapNotNull null
                    }
                }

                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    isLoading = false

                    if (filtered.isNotEmpty()) {
                        getFeedViewModel.addAllFeedData(filtered.toMutableList())
                        getFeedViewModel.filterOutUserPosts(currentUserId)
                        followedPostsAdapter.submitItems(getFeedViewModel.getAllFeedData())
                    }

                    hasMoreData = filtered.size >= 20
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    isLoading = false
                }
            }
        }
    }


    private suspend fun loadMyFollowersList() {
        try {
            Log.d(TAG, "Loading MY followers list...")

            val myUsername = getUsername(requireContext())
            if (myUsername.isEmpty()) {
                Log.e(TAG, "Username is empty, cannot load my followers list")
                return
            }

            // Get YOUR followers (people who follow YOU)
            val response = retrofitInstance.apiService.getUserFollowers(
                username = myUsername,
                page = 1,
                limit = 1000
            )

            if (response.isSuccessful && response.body() != null) {
                val myFollowers = response.body()!!.data

                val myFollowerIds = mutableListOf<String>()

                myFollowers?.forEach { follower ->
                    val followerId = follower._id
                    if (followerId.isNotEmpty()) {
                        myFollowerIds.add(followerId)
                        Log.d(TAG, "My follower: @${follower.username} (ID: $followerId)")
                    }
                }

                // Update FeedAdapter cache with YOUR followers
                FeedAdapter.setMyFollowersList(myFollowerIds)
                Log.d(TAG, "Populated my followers cache with ${myFollowerIds.size} followers")

            } else {
                Log.e(TAG, "API error loading my followers: ${response.code()}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error loading my followers list: ${e.message}", e)
        }
    }

    private suspend fun loadFollowingUserIds() {
        try {
            Log.d(TAG, "Loading following list...")

            val myUsername = getUsername(requireContext())
            if (myUsername.isEmpty()) {
                Log.e(TAG, "Username is empty, cannot load following list")
                return
            }

            val response = retrofitInstance.apiService.getUserFollowing(
                username = myUsername,
                page = 1,
                limit = 1000
            )

            if (response.isSuccessful && response.body() != null) {
                val followingUsers = response.body()!!.data

                followingUserIds.clear()
                followingUserMap.clear()

                val followingIdsList = mutableListOf<String>()
                val followingUsernamesList = mutableListOf<String>()

                followingUsers?.forEach { user ->
                    val userId = user._id
                    val username = user.username

                    if (userId.isNotEmpty()) {
                        followingUserIds.add(userId)
                        followingUserMap[userId] = username
                        followingIdsList.add(userId)
                        followingUsernamesList.add(username)

                        Log.d(TAG, "Following: @$username (ID: $userId)")
                    }
                }

                hasLoadedFollowingList = true

                // Update adapter on main thread
                withContext(Dispatchers.Main) {
                    if (::followedPostsAdapter.isInitialized) {
                        FeedAdapter.setCachedFollowingList(followingUserIds)
                        followedPostsAdapter.updateFollowingList(followingIdsList)
                        followedPostsAdapter.updateFollowingUsernames(followingUsernamesList)
                        followedPostsAdapter.notifyDataSetChanged()
                        Log.d(TAG, "Updated adapter with ${followingUserIds.size} following users")
                    }
                }

                Log.d(TAG, "Successfully loaded ${followingUserIds.size} following users")

                // Also load YOUR followers (people who follow YOU)
                loadMyFollowersList()

            } else {
                Log.e(TAG, "API error: ${response.code()}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error loading following list: ${e.message}", e)
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


    private fun handleError(message: String) {
        Log.e(TAG, message)
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
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
        currentAdapterPosition = followedPostsAdapter.getCurrentItemDisplayPosition()
        Log.d(TAG, "onCreateView: data added last view position " +
                "$currentAdapterPosition")
        Log.d(TAG, "onPause: called")

    }

    @SuppressLint("SetTextI18n")
    override fun followButtonClicked(
        followUnFollowEntity: FollowUnFollowEntity,
        followButton: AppCompatButton
    ) {
        followButton.setOnClickListener(null)

        if (followUnFollowEntity.isFollowing) {
            followButton.text = "Unfollow"
            followButton.visibility = View.GONE

            followButton.setOnClickListener {
                followUnFollowEntity.isFollowing = false
                followButton.visibility = View.GONE

                // Remove user from following list
                followingUserIds.remove(followUnFollowEntity.userId)
                Log.d(TAG, "Unfollowed user: ${followUnFollowEntity.userId}")
                Log.d(TAG, "Now following ${followingUserIds.size} users")

                // Refresh feed to remove their posts
                refreshFeedAfterUnfollow()
            }
        } else {
            followButton.visibility = View.GONE
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFollowEvent(event: ShortsFollowButtonClicked) {
        val followEntity = event.followUnFollowEntity

        Log.d(TAG, "FOLLOW EVENT in FollowingFragment")
        Log.d(TAG, "User: ${followEntity.userId}")
        Log.d(TAG, "isFollowing: ${followEntity.isFollowing}")

        if (followEntity.isFollowing) {
            // User followed someone new
            followingUserIds.add(followEntity.userId)

            // Update FeedAdapter cache
            FeedAdapter.addToFollowingCache(followEntity.userId)

            Log.d(TAG, "Added user to following list. Total following: ${followingUserIds.size}")

            // Optionally reload feed to show their posts
            // clearAndReloadFeed()
        } else {
            // User unfollowed someone - IMMEDIATE REMOVAL
            followingUserIds.remove(followEntity.userId)
            followingUserMap.remove(followEntity.userId)

            // Update FeedAdapter cache
            FeedAdapter.removeFromFollowingCache(followEntity.userId)

            Log.d(TAG, "Removed user from following list. Total following: ${followingUserIds.size}")

            // IMMEDIATELY filter and update the adapter
            val currentUserId = getUserId(requireContext())
            val currentPosts = getFeedViewModel.getAllFeedData()

            val filteredPosts = currentPosts.filter { post ->
                try {
                    // WHO POSTED THIS?
                    val posterAccountId: String = if (post.repostedUser != null) {
                        post.repostedUser!!.owner
                    } else {
                        post.author?.account?._id ?: return@filter false
                    }

                    // Keep post only if:
                    // 1. Not my own post
                    // 2. I still follow the poster
                    posterAccountId != currentUserId && followingUserIds.contains(posterAccountId)

                } catch (e: Exception) {
                    Log.e(TAG, "Error filtering post: ${e.message}")
                    false
                }
            }

            Log.d(TAG, "Posts before unfollow: ${currentPosts.size}")
            Log.d(TAG, "Posts after unfollow: ${filteredPosts.size}")
            Log.d(TAG, "Removed ${currentPosts.size - filteredPosts.size} posts")

            // Update ViewModel
            getFeedViewModel.clearAllFeedData()
            getFeedViewModel.addAllFeedData(filteredPosts.toMutableList())

            // FORCE adapter to update immediately
            followedPostsAdapter.submitItems(filteredPosts.toMutableList())
            followedPostsAdapter.notifyDataSetChanged()

            // Scroll to top to show the change
            feedListView.scrollToPosition(0)
        }
    }

    private fun refreshFeedAfterUnfollow() {
        val currentUserId = getUserId(requireContext())
        val allPosts = getFeedViewModel.getAllFeedData()

        Log.d(TAG, "REFRESHING AFTER UNFOLLOW")
        Log.d(TAG, "Now following ${followingUserIds.size} users")
        Log.d(TAG, "Following IDs: $followingUserIds")

        val filteredData = allPosts.filter { post ->
            try {
                // WHO POSTED THIS?
                val posterAccountId: String = if (post.repostedUser != null) {
                    post.repostedUser!!.owner
                } else {
                    post.author?.account?._id ?: return@filter false
                }

                val posterUsername: String = if (post.repostedUser != null) {
                    post.repostedUser!!.username.trim().lowercase()
                } else {
                    post.author?.account?.username?.trim()?.lowercase() ?: return@filter false
                }

                // Skip own posts
                if (posterAccountId == currentUserId) return@filter false

                // DO I FOLLOW THE POSTER?
                val isFollowingById = followingUserIds.contains(posterAccountId)
                val isFollowingByUsername = followingUserMap.values.any {
                    it.trim().lowercase() == posterUsername
                }

                val shouldKeep = isFollowingById || isFollowingByUsername

                if (shouldKeep) {
                    Log.d(TAG, "  ✓ Keeping post by @$posterUsername (ID: $posterAccountId)")
                } else {
                    Log.d(TAG, "  ✗ Removing post by @$posterUsername (ID: $posterAccountId)")
                }

                shouldKeep

            } catch (e: Exception) {
                Log.e(TAG, "Error filtering post: ${e.message}")
                false
            }
        }

        // Update ViewModel
        getFeedViewModel.clearAllFeedData()
        getFeedViewModel.addAllFeedData(filteredData.toMutableList())

        // Force update adapter
        followedPostsAdapter.submitItems(filteredData.toMutableList())
        followedPostsAdapter.notifyDataSetChanged()

        Log.d(TAG, "Posts before: ${allPosts.size}")
        Log.d(TAG, "Posts after: ${filteredData.size}")
        Log.d(TAG, "Removed: ${allPosts.size - filteredData.size} posts")
    }

    override fun likeUnLikeFeed(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {
        try {
            val updatedPost = if (data.isLiked) {
                data.copy(
                    likes = data.likes - 1,
                    isLiked = false
                )
            } else {
                data.copy(
                    likes = data.likes + 1,
                    isLiked = true
                )
            }

            // Update the post in your ViewModel
            val viewModel = null
            viewModel.updatePost(position, updatedPost)

            // Make API call to like/unlike
            lifecycleScope.launch {
                feedUploadViewModel.likeUnLikeFeed(data._id)
            }

            // Notify adapter
            followedPostsAdapter.updateItem(position, updatedPost)

        } catch (e: Exception) {
            Log.e(TAG, "Error in likeUnLikeFeed: ${e.message}")
        }
    }


    override fun feedFavoriteClick(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {
        try {
            val updatedPost = data.copy(isBookmarked = !data.isBookmarked)

            val viewModel = null
            // Update in ViewModel
            viewModel.updatePost(position, updatedPost)

            // Make API call
            lifecycleScope.launch {
                feedUploadViewModel.favoriteFeed(data._id)
            }

            // Notify adapter
            followedPostsAdapter.updateItem(position, updatedPost)

            // Show feedback to user
            val message = if (updatedPost.isBookmarked) "Added to favorites"
            else "Removed from favorites"
            Toast.makeText(requireContext(),
                message, Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Log.e(TAG, "Error in feedFavoriteClick: ${e.message}")
        }
    }


    override fun feedCommentClicked(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.Post
    ) {

        val commentList: MutableList<Comment> = data.comments.toMutableList()

        val bottomSheet = CommentsBottomSheet(
            context = requireContext(),
            dataList = ArrayList(commentList),
            onCommentCountChanged = { newCount ->
                println("Updated comment count: $newCount")
            },
            onCommentDeleted = {
                if (commentList.isNotEmpty()) {
                    commentList.removeAt(commentList.lastIndex)
                }
            },
            onCommentAdded = {
                val currentTime = System.currentTimeMillis()
                val isoTimestamp = SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()
                ).apply { timeZone = TimeZone.getTimeZone("UTC") }
                    .format(Date(currentTime))

                val newComment = Comment(
                    __v = 0,
                    _id = currentTime.toString(),
                    author = null,
                    content = "New Comment added at $isoTimestamp",
                    contentType = "",
                    createdAt = isoTimestamp,
                    isLiked = false,
                    likes = 0,
                    postId = data._id,
                    updatedAt = isoTimestamp,
                    replyCount = 0,
                    audios = mutableListOf(),
                    images = mutableListOf(),
                    videos = mutableListOf(),
                    docs = mutableListOf(),
                    thumbnail = mutableListOf(),
                    gifs = "",
                    duration = "00:00",
                    numberOfPages = "0",
                    fileSize = "0B",
                    fileType = "unknown",
                    fileName = "unknown"
                )
                commentList.add(newComment)
            },
            postId = data._id,
            commentCount = data.comments,

            )

        bottomSheet.showBottomSheet()
    }


    @SuppressLint("CutPasteId")
    override fun moreOptionsClick(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.Post

    ) {
        Log.d(TAG, "moreOptionsClick: More options clicked")
        val view: View = layoutInflater.inflate(
            R.layout.feed_more_options_layout, null)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(view)
        dialog.show()
        val downloadFiles : View = view.findViewById(R.id.downloadAction)
        val followUnfollowLayout :View = view.findViewById(R.id.followAction)
        val reportUser: View = view.findViewById(R.id.reportOptionLayout)
        val  hidePostLayout : View = view.findViewById(R.id.hidePostLayout)
        val copyLink: View = view.findViewById(R.id.copyLinkLayout)
        val muteOptionLayout: View = view.findViewById(R.id.muteOptionLayout)
        val QuoteFeedLayout : View = view.findViewById(R.id.repostAction)

        if (data.contentType == "text"){
            downloadFiles.visibility = View.GONE
        }
        downloadFiles.setOnClickListener {
            Log.d("DownloadButton", "Data: $data")

            dialog.dismiss()
        }
        muteOptionLayout.setOnClickListener {
            Log.d("MuteButton", "Data: $data")
        }
        followUnfollowLayout.visibility = View.GONE
        QuoteFeedLayout.setOnClickListener {
            val fragment = NewRepostedPostFragment(data)

            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, fragment) // Ensure fragment_container is correct
            transaction.addToBackStack(null)
            transaction.commit()
            dialog.dismiss()

        }
        copyLink.setOnClickListener {
            val postId = data._id // Adjust this based on your actual Post class property name
            val linkToCopy = "https:/circuitSocial.app/post/$postId" // Replace with your actual link
            val clipboard = requireContext().getSystemService(
                Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied Link", linkToCopy)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(requireContext(),
                "Link copied to clipboard/$postId", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        val notInterested : View = view.findViewById(R.id.notInterestedLayout)
        notInterested.setOnClickListener {

            dialog.dismiss()

        }

        hidePostLayout.setOnClickListener {
            Log.d(TAG, "hidePostLayout: hide post clicked")

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
            Log.d("reportUser","has been clicked")
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

    override fun feedFileClicked(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.Post
    ) {
        when (data.contentType) {
            "mixed_files" -> {


                EventBus.getDefault().post(HideBottomNav())
                EventBus.getDefault().post(HideAppBar())
                EventBus.getDefault().post(HideFeedFloatingActionButton())
                frameLayout.visibility = View.VISIBLE
                feedListView.visibility = View.GONE


                val args = Bundle().apply {
                    putInt("position", position)
                    putSerializable("data", data) // Adjust type if needed
                }


            }

            "image" -> {


                EventBus.getDefault().post(HideBottomNav())
                EventBus.getDefault().post(HideAppBar())
                EventBus.getDefault().post(HideFeedFloatingActionButton())
                frameLayout.visibility = View.VISIBLE
                feedListView.visibility = View.GONE
                val args = Bundle().apply {
                    putInt("position", position)
                    putSerializable("data", data) // Adjust type if needed
                }
                feedImageViewFragment = FeedImageViewFragment()
                feedImageViewFragment?.setListener(this)
                feedImageViewFragment?.arguments = args
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.feed_text_view_fragment,
                        feedImageViewFragment!!
                    ) // Use the correct container ID
                    .addToBackStack(null) // Optional, to add to back stack
                    .commit()
            }

            "text" -> {
                EventBus.getDefault().post(HideBottomNav())
                EventBus.getDefault().post(HideAppBar())
                EventBus.getDefault().post(HideFeedFloatingActionButton())
                frameLayout.visibility = View.VISIBLE
                feedListView.visibility = View.GONE
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

            "docs" -> {


                EventBus.getDefault().post(HideBottomNav())
                EventBus.getDefault().post(HideAppBar())
                EventBus.getDefault().post(HideFeedFloatingActionButton())
                frameLayout.visibility = View.VISIBLE
                feedListView.visibility = View.GONE
                val args = Bundle().apply {
                    putInt("position", position)
                    putSerializable("data", data) // Adjust type if needed
                }
                feedDocsViewFragment = FeedDocumentViewFragment()
                feedDocsViewFragment?.setListener(this)
                feedDocsViewFragment?.arguments = args
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.feed_text_view_fragment,
                        feedDocsViewFragment!!
                    ) // Use the correct container ID
                    .addToBackStack(null) // Optional, to add to back stack
                    .commit()

            }

            "video" -> {


                EventBus.getDefault().post(HideBottomNav())
                EventBus.getDefault().post(HideAppBar())
                EventBus.getDefault().post(HideFeedFloatingActionButton())

                frameLayout.visibility = View.VISIBLE
                feedListView.visibility = View.GONE
                val args = Bundle().apply {
                    putInt("position", position)
                    putSerializable("data", data) // Adjust type if needed
                }
                feedVideoViewFragment = FeedVideoViewFragment()
                feedVideoViewFragment?.setListener(this)
                feedVideoViewFragment?.arguments = args
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.feed_text_view_fragment,
                        feedVideoViewFragment!!
                    ) // Use the correct container ID
                    .addToBackStack(null) // Optional, to add to back stack
                    .commit()
            }

            "audio", "vn" -> {
                EventBus.getDefault().post(HideBottomNav())
                EventBus.getDefault().post(HideAppBar())
                EventBus.getDefault().post(HideFeedFloatingActionButton())
                frameLayout.visibility = View.VISIBLE
                feedListView.visibility = View.GONE
                val args = Bundle().apply {
                    putInt("position", position)
                    putSerializable("data", data) // Adjust type if needed
                }
                feedAudioViewFragment = FeedAudioViewFragment()
                feedAudioViewFragment?.setListener(this)
                feedAudioViewFragment?.arguments = args
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.feed_text_view_fragment,
                        feedAudioViewFragment!!
                    ) // Use the correct container ID
                    .addToBackStack(null) // Optional, to add to back stack
                    .commit()

            }

            "multiple_images" -> {

                EventBus.getDefault().post(HideBottomNav())
                EventBus.getDefault().post(HideAppBar())
                EventBus.getDefault().post(HideFeedFloatingActionButton())



                frameLayout.visibility = View.VISIBLE
                feedListView.visibility = View.GONE
                val args = Bundle().apply {
                    putInt("position", position)
                    putSerializable("data", data) // Adjust type if needed
                }
                feedMultipleImageViewFragment = FeedMultipleImageViewFragment()
                feedMultipleImageViewFragment?.setListener(this)
                feedMultipleImageViewFragment?.arguments = args
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.feed_text_view_fragment,
                        feedMultipleImageViewFragment!!
                    ) // Use the correct container ID
                    .addToBackStack(null) // Optional, to add to back stack
                    .commit()

            }
        }

    }

    override fun feedRepostFileClicked(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.OriginalPost
    ) {
        when (data.contentType){
            "mixed_files" -> {

                EventBus.getDefault().post(HideBottomNav())
                EventBus.getDefault().post(HideAppBar())
                EventBus.getDefault().post(HideFeedFloatingActionButton())
                frameLayout.visibility = View.VISIBLE
                feedListView.visibility = View.GONE

                fragmentOriginalPostWithRepostInside?.setListener(this)
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
                frameLayout.visibility = View.VISIBLE
                feedListView.visibility = View.GONE
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
                frameLayout.visibility = View.VISIBLE
                feedListView.visibility = View.GONE
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
                frameLayout.visibility = View.VISIBLE
                feedListView.visibility = View.GONE
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
                frameLayout.visibility = View.VISIBLE
                feedListView.visibility = View.GONE
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
                frameLayout.visibility = View.VISIBLE
                feedListView.visibility = View.GONE
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
                frameLayout.visibility = View.VISIBLE
                feedListView.visibility = View.GONE
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

    @SuppressLint("MissingInflatedId")
    override fun feedShareClicked(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.Post
    ) {
        val context = requireContext()

        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val shareView = layoutInflater.inflate(R.layout.bottom_dialog_for_share, null)
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
        val resolveInfoList = packageManager?.queryIntentActivities(intent,
            PackageManager.MATCH_DEFAULT_ONLY)

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = resolveInfoList?.let {
            ShareFeedPostAdapter(it, context, data) }


    }


    @SuppressLint("CutPasteId", "InflateParams")
    override fun feedRepostPost(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {

        val view: View = layoutInflater.inflate(
            R.layout.feed_moreoptions_bottomsheet_layout, null)

        val quoteButton: MaterialCardView = view.findViewById(R.id.rePostFeedLayout)
        val repostButton: MaterialCardView = view.findViewById(R.id.shareFeedLayout)
        val download: MaterialCardView = view.findViewById(R.id.downloadFeedLayout)
        val followUnfollowLayout : MaterialCardView = view.findViewById(R.id.followUnfollowLayout)
        val shareFeedLayout : MaterialCardView = view.findViewById(R.id.shareFeedLayout)
        val notInterestedLayout : MaterialCardView = view.findViewById(R.id.notInterestedLayout)
        val hidePostLayout : MaterialCardView = view.findViewById(R.id.hidePostLayout)
        val reportOptionLayout : MaterialCardView = view.findViewById(R.id.reportOptionLayout)
        val copyLinkLayout: MaterialCardView = view.findViewById(R.id.copyLinkLayout)
        val muteUser : MaterialCardView = view.findViewById(R.id.muteOptionLayout)

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
            val fragment = NewRepostedPostFragment(data)
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, fragment) // Ensure fragment_container is correct
            transaction.addToBackStack("NewRepostedPostFragment") // Name the back stack entry
            transaction.commit()
        }

    }

    override fun feedRepostPostClicked(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.Post
    ) {

    }

    override fun feedClickedToOriginalPost(position: Int, originalPostId: String) {

    }

    override fun onImageClick() {

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
        data: Post
    ) {
        EventBus.getDefault().post(FeedCommentClicked(position, data))
    }

    override fun onLikeUnLikeFeedFromFeedTextViewFragment(
        position: Int,
        data: Post
    ) {
        try {
            val updatedComment = if (data.isLiked) {
                data.copy(
                    likes = data.likes + 1,
                    repostedByUserId = data.repostedByUserId,
                    feedShortsBusinessId = data.feedShortsBusinessId // Ensure this field is set
                )
            } else {
                data.copy(
                    likes = data.likes - 1,
                    repostedByUserId = data.repostedByUserId,
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

            followedPostsAdapter.updateItem(position, updatedComment)
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
                }
                else {
                    Log.d(TAG, "likeUnLikeFeed: feed to update is not available in the list")
                }
            }
            else {
                Log.i(TAG, "likeUnLikeFeed: my feed data is empty")
            }
        }
        catch (e: Exception) {
            Log.e("likeUnLikeFeed", "likeUnLikeFeed: ${e.message}")
            e.printStackTrace()
        }

    }

    override fun onFeedFavoriteClickFromFeedTextViewFragment(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.Post
    ) {
        EventBus.getDefault().post(FeedFavoriteClick(position, data))

        val isMyFeedEmpty = getFeedViewModel.getMyFeedData().isEmpty()

        if (!isMyFeedEmpty) {
            val myFeedData = getFeedViewModel.getMyFeedData()
            val feedToUpdate = myFeedData.find { feed -> feed._id == data._id }
            if (feedToUpdate != null) {
                feedToUpdate.isBookmarked = data.isBookmarked
                val myFeedDataPosition = getFeedViewModel.getMyFeedPositionById(feedToUpdate._id)
                getFeedViewModel.updateMyFeedData(myFeedDataPosition, feedToUpdate)
            }
            else {
                Log.d(TAG, "feedFavoriteClick: feed to update is not available in the list")
            }
        }
        else {
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
                followedPostsAdapter.updateItem(position, data)
            } else {
                Log.i(TAG, "onFeedFavoriteClickFromFeedTextViewFragment: allFeedPost is null")
            }
        }
        else {
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
        val reportOptionLayout : LinearLayout = view.findViewById(R.id.reportOption)
        val hidePostLayout : LinearLayout = view.findViewById(R.id.hidePostOption)

        val followUnfollowLayout : LinearLayout = view.findViewById(R.id.followUnfollowOption)
        val notInterestedLayout : LinearLayout = view.findViewById(R.id.notInterestedOption)
        notInterestedLayout.visibility = View.GONE
        hidePostLayout.visibility = View.GONE
        followUnfollowLayout.visibility = View.GONE

        hidePostLayout.setOnClickListener {
            Log.d("HideLayout","has been clicked")

        }
        reportOptionLayout.setOnClickListener {
            Log.d("reportUser","has been clicked")
            val intent = Intent(requireActivity(), ReportNotificationActivity2::class.java)
            startActivityForResult(intent, REQUEST_REPOST_FEED_ACTIVITY)
        }
    }

    override fun finishedPlayingVideo(position: Int) {
        followedPostsAdapter.notifyItemChanged(position)

    }

    override fun onRePostClickFromFeedTextViewFragment(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.Post
    ) {

    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onResume() {
        super.onResume()

        getFeedViewModel.isResuming = true
        Log.d(TAG, "onResume: called")

        feedListView.visibility = View.VISIBLE
        frameLayout.visibility = View.GONE

        EventBus.getDefault().post(ShowBottomNav(false))
        EventBus.getDefault().post(ShowFeedFloatingActionButton(false))
        EventBus.getDefault().post(ShowAppBar(false))

        // CRITICAL: Reload following list from server AND refresh feed
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Reload following list from API
                loadFollowingUserIds()

                withContext(Dispatchers.Main) {
                    // Sync adapter cache with fresh data
                    if (::followedPostsAdapter.isInitialized) {
                        FeedAdapter.setCachedFollowingList(followingUserIds)
                        followedPostsAdapter.updateFollowingList(followingUserIds.toList())
                        followedPostsAdapter.updateFollowingUsernames(followingUserMap.values.toList())

                        Log.d(TAG, "onResume: Synced cache - now following ${followingUserIds.size} users")
                    }

                    // Filter posts based on current following list
                    val currentUserId = getUserId(requireContext())
                    val currentPosts = getFeedViewModel.getAllFeedData()

                    val filteredPosts = currentPosts.filter { post ->
                        try {
                            val posterAccountId: String = if (post.repostedUser != null) {
                                post.repostedUser!!.owner
                            } else {
                                post.author?.account?._id ?: return@filter false
                            }

                            // Keep post only if still following the poster
                            posterAccountId != currentUserId && followingUserIds.contains(posterAccountId)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error filtering post: ${e.message}")
                            false
                        }
                    }

                    Log.d(TAG, "onResume: Posts before: ${currentPosts.size}, after: ${filteredPosts.size}")

                    // Update ViewModel and adapter
                    getFeedViewModel.clearAllFeedData()
                    getFeedViewModel.addAllFeedData(filteredPosts.toMutableList())
                    followedPostsAdapter.submitItems(filteredPosts.toMutableList())
                    followedPostsAdapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in onResume refresh: ${e.message}", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

}

private fun Fragment_Original_Post_With_Repost_Inside?.setListener(
    fragment: FollowingFragment
) {
}

private fun Nothing?.updatePost(
    i: Int,
    post: com.uyscuti.social.network.api.response.posts.Post
) {
}

private fun Int.toMutableList(): MutableList<Comment> {
    return mutableListOf() // Return empty mutable list or implement your logic
}

private fun Nothing?.removeComment(value: Any) {}


data class FollowUnFollowEntity(
    val userId: String,
    val isFollowed: Boolean
)