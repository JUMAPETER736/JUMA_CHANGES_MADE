package com.uyscuti.social.circuit.User_Interface.fragments.feed

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaPlayer
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
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView

import com.uyscuti.social.circuit.adapter.feed.FeedAdapter
import com.uyscuti.social.circuit.adapter.feed.OnFeedClickListener
import com.uyscuti.social.circuit.adapter.feed.ShareFeedPostAdapter
import com.uyscuti.social.circuit.eventbus.FeedFavoriteClick
import com.uyscuti.social.circuit.eventbus.FeedLikeClick
import com.uyscuti.social.circuit.eventbus.HideFeedFloatingActionButton
import com.uyscuti.social.circuit.eventbus.ShowFeedFloatingActionButton
import com.uyscuti.social.circuit.interfaces.feedinterfaces.FeedTextViewFragmentInterface
import com.uyscuti.social.circuit.model.FeedCommentClicked
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
import com.uyscuti.social.circuit.User_Interface.fragments.feed.feedviewfragments.NewRepostedPostFragment
import com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.Fragment_Original_Post_With_Repost_Inside
import com.uyscuti.social.circuit.viewmodels.FeedShortsViewModel
import com.uyscuti.social.circuit.viewmodels.FollowUnfollowViewModel
import com.uyscuti.social.circuit.viewmodels.GetShortsByUsernameViewModel
import com.uyscuti.social.circuit.viewmodels.feed.FeedUploadViewModel
import com.uyscuti.social.circuit.viewmodels.feed.GetFeedViewModel
import com.uyscuti.social.circuit.R

import com.uyscuti.social.circuit.User_Interface.Log_In_And_Register.LoginActivity.UserStorageHelper.getUserId
import com.uyscuti.social.circuit.User_Interface.Log_In_And_Register.LoginActivity.UserStorageHelper.getUsername
import com.uyscuti.social.circuit.adapter.feed.FeedPaginatedAdapter
import com.uyscuti.social.circuit.feed.FeedUploadRepository
import com.uyscuti.social.circuit.model.feed.multiple_files.MixedFeedUploadDataClass
import com.uyscuti.social.circuit.User_Interface.fragments.CommentsBottomSheet
import com.uyscuti.social.circuit.User_Interface.fragments.feed.feedRepostViewFragmentsimport.FeedRepostMultipleImageFragment
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


    private var myUserId: String = ""
    private var followingUserIds = mutableSetOf<String>()
    private var hasLoadedFollowingList = false
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var frameLayout:FrameLayout
    private val getFeedViewModel: GetFeedViewModel by activityViewModels()
    private lateinit var followedPostsAdapter: FeedAdapter
    private val feesShortsSharedViewModel: FeedShortsViewModel by activityViewModels()
    private lateinit var allFeedAdapterRecyclerView: RecyclerView
    private lateinit var feedListView: RecyclerView
    private var isLoading = false
    private var hasMoreData = true
    private var parentFragment: FeedFragment? = null
    private val followingUserMap = mutableMapOf<String, String>()
    var bitmap: Bitmap? = null
    private val feedUploadViewModel: FeedUploadViewModel by activityViewModels()
    private val followUnFollowViewModel: FollowUnfollowViewModel by viewModels()
    private val shortsViewModel: GetShortsByUsernameViewModel by activityViewModels()
    private lateinit var myFeedAdapter: FeedAdapter
    private lateinit var progressBar: ProgressBar
    var currentAdapterPosition = -1
    private lateinit var feedUploadRepository: FeedUploadRepository
    private var positionFromShorts: SetAllFragmentScrollPosition? = null
    private var feedVideoViewFragment: FeedVideoViewFragment? = null
    private var feedTextViewFragment: FeedTextViewFragment?= null
    private var feedAudioViewFragment: FeedAudioViewFragment? = null
    private var feedMultipleImageViewFragment: FeedMultipleImageViewFragment? = null
    private var feedMixedFilesViewFragment: FeedMixedFilesViewFragment? = null
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
    }

    fun setPositionFromShorts(positionFromShorts: SetAllFragmentScrollPosition) {
        Log.d(TAG, "setPositionFromShorts: ${positionFromShorts.allFragmentFeedPosition}")
        this.positionFromShorts = positionFromShorts
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

        feedUploadRepository = FeedUploadRepository()

        feedListView = view.findViewById(R.id.rvq)
        progressBar = view.findViewById(R.id.progressBar)
        frameLayout = view.findViewById(R.id.feed_text_view_fragment)

        myUserId = getUserId(requireContext())
        Log.d(TAG, "My user ID: $myUserId")

        followedPostsAdapter = FeedAdapter(
            requireActivity(),
            this@FollowingFragment
        )

        Log.d("RecyclerViewTwo", "Adapter set: $followedPostsAdapter")
        feedListView.adapter = followedPostsAdapter

        Log.d("RecyclerViewTwo", "Adapter set: $followedPostsAdapter")

        feedListView.layoutManager = LinearLayoutManager(requireContext())
        Log.d("RecyclerViewDebug", "Adapter set: ${true}")

        // Check if the data is empty before loading
        if (getFeedViewModel.getAllFeedData().isEmpty()) {
            getAllFeed(followedPostsAdapter.startPage)
        } else {
            // Don't fetch data again if the ViewModel already has data
            followedPostsAdapter.submitItems(getFeedViewModel.getAllFeedData())
            Log.d(TAG, "Data already available, using the cached data")
        }

        followedPostsAdapter.setOnPaginationListener(
            object : FeedPaginatedAdapter.OnPaginationListener {
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

    fun forShow() {
        Log.d("forShow", "forShow: is called")

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
                            Toast.makeText(
                                requireContext(),
                                "You're not following anyone yet. Follow some users to see their posts here!",
                                Toast.LENGTH_LONG
                            ).show()
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

            // ✅ CRITICAL: Clear old cached data before loading new data
            getFeedViewModel.clearAllFeedData()
            followedPostsAdapter.submitItems(mutableListOf())
            Log.d(TAG, "🧹 CLEARED old cached posts from adapter and ViewModel")
        }

        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "SIMPLE FOLLOWING FEED RULE:")
        Log.d(TAG, "Following ${followingUserIds.size} users")
        Log.d(TAG, "ONLY show posts BY these ${followingUserIds.size} people")
        Log.d(TAG, "Don't care about reposts content - only WHO posted it")
        Log.d(TAG, "═══════════════════════════════════════")

        while (uniqueAuthors.size < followingUserIds.size && pageNum <= maxPages) {
            try {
                val response = retrofitInstance.apiService.getAllFeed(pageNum.toString())
                if (!response.isSuccessful || response.body() == null) break

                val pagePosts = response.body()!!.data.data.posts
                Log.d(TAG, "📦 Page $pageNum: ${pagePosts.size} posts")

                val filtered = pagePosts.mapNotNull { post ->
                    try {
                        // ═══════════════════════════════════════
                        // STEP 1: WHO POSTED THIS?
                        // ═══════════════════════════════════════

                        val posterAccountId: String
                        val posterUsername: String

                        if (post.repostedUser != null) {
                            // This is a REPOST - check who REPOSTED it (not original author)
                            posterAccountId = post.repostedUser.owner
                            posterUsername = post.repostedUser.username.trim().lowercase()
                            Log.d(TAG, "  📌 REPOST by @${post.repostedUser.username} (ID: $posterAccountId)")
                        } else {
                            // This is an ORIGINAL POST - check the author
                            posterAccountId = post.author?.account?._id ?: return@mapNotNull null
                            posterUsername = post.author.account.username.trim().lowercase()
                            Log.d(TAG, "  📌 ORIGINAL POST by @${post.author.account.username} (ID: $posterAccountId)")
                        }

                        // Skip own posts
                        if (posterAccountId == currentUserId) {
                            Log.d(TAG, "    ⊘ This is MY post - SKIPPING")
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
                            Log.d(TAG, "    ✅ YES, I follow @$posterUsername - INCLUDE THIS POST")
                            return@mapNotNull post
                        } else {
                            Log.d(TAG, "    ❌ NO, I DON'T follow @$posterUsername - EXCLUDE THIS POST")
                            return@mapNotNull null
                        }

                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing post: ${e.message}")
                        return@mapNotNull null
                    }
                }

                allFollowingPosts.addAll(filtered)

                Log.d(TAG, "───────────────────────────────────────")
                Log.d(TAG, "Page $pageNum Results:")
                Log.d(TAG, "  Total posts: ${pagePosts.size}")
                Log.d(TAG, "  Included: ${filtered.size}")
                Log.d(TAG, "  Excluded: ${pagePosts.size - filtered.size}")
                Log.d(TAG, "───────────────────────────────────────")

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

            Log.d(TAG, "═══════════════════════════════════════")
            Log.d(TAG, "LOADING COMPLETE:")
            Log.d(TAG, "Total posts: ${allFollowingPosts.size}")
            Log.d(TAG, "ALL posts are BY people you follow")
            Log.d(TAG, "═══════════════════════════════════════")

            if (allFollowingPosts.isEmpty()) {
                // ✅ Make sure adapter is empty
                followedPostsAdapter.submitItems(mutableListOf())
                Toast.makeText(
                    requireContext(),
                    "No posts yet from people you follow",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                // ✅ CRITICAL: Clear old data first, then add new filtered data
                getFeedViewModel.clearAllFeedData()
                getFeedViewModel.addAllFeedData(allFollowingPosts.toMutableList())
                getFeedViewModel.filterOutUserPosts(currentUserId)

                val finalPosts = getFeedViewModel.getAllFeedData()
                Log.d(TAG, "📤 Submitting ${finalPosts.size} posts to adapter")
                followedPostsAdapter.submitItems(finalPosts)

                // ✅ Force adapter to refresh
                followedPostsAdapter.notifyDataSetChanged()
            }

            hasMoreData = allFollowingPosts.size >= 20
        }
    }

    // Same simple logic for pagination
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
                            posterAccountId = post.repostedUser.owner
                            posterUsername = post.repostedUser.username.trim().lowercase()
                        } else {
                            posterAccountId = post.author?.account?._id ?: return@mapNotNull null
                            posterUsername = post.author.account.username.trim().lowercase()
                        }

                        // Skip own posts
                        if (posterAccountId == currentUserId) return@mapNotNull null

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

    // Same simple logic for refresh after unfollow
    private fun refreshFeedAfterUnfollow() {
        val currentUserId = getUserId(requireContext())
        val allPosts = getFeedViewModel.getAllFeedData()

        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "REFRESHING AFTER UNFOLLOW")
        Log.d(TAG, "Now following ${followingUserIds.size} users")
        Log.d(TAG, "═══════════════════════════════════════")

        val filteredData = allPosts.mapNotNull { post ->
            try {
                // WHO POSTED THIS?
                val posterAccountId: String
                val posterUsername: String

                if (post.repostedUser != null) {
                    posterAccountId = post.repostedUser.owner
                    posterUsername = post.repostedUser.username.trim().lowercase()
                } else {
                    posterAccountId = post.author?.account?._id ?: return@mapNotNull null
                    posterUsername = post.author.account.username.trim().lowercase()
                }

                // Skip own posts
                if (posterAccountId == currentUserId) return@mapNotNull null

                // DO I FOLLOW THE POSTER?
                val isFollowingById = followingUserIds.contains(posterAccountId)
                val isFollowingByUsername = followingUserMap.values.any {
                    it.trim().lowercase() == posterUsername
                }

                if (isFollowingById || isFollowingByUsername) {
                    Log.d(TAG, "  ✓ Keeping post by @$posterUsername (I follow them)")
                    return@mapNotNull post
                } else {
                    Log.d(TAG, "  ✗ Removing post by @$posterUsername (I don't follow them)")
                    return@mapNotNull null
                }

            } catch (e: Exception) {
                return@mapNotNull null
            }
        }

        followedPostsAdapter.submitItems(filteredData.toMutableList())

        Log.d(TAG, "───────────────────────────────────────")
        Log.d(TAG, "Posts before: ${allPosts.size}")
        Log.d(TAG, "Posts after: ${filteredData.size}")
        Log.d(TAG, "Removed: ${allPosts.size - filteredData.size} posts")
        Log.d(TAG, "═══════════════════════════════════════")
    }

    // ✅ Add this to your FollowingFragment - call it in onResume or when tab is selected
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun clearAndReloadFeed() {
        Log.d(TAG, "🔄 Clearing and reloading Following feed")

        // Clear all old data
        getFeedViewModel.clearAllFeedData()
        followedPostsAdapter.submitItems(mutableListOf())

        // Reset state
        hasLoadedFollowingList = false
        isLoading = false

        // Reload fresh data
        getAllFeed(1)
    }

    private suspend fun loadFollowingUserIds() {
        try {
            Log.d(TAG, "Loading following list...")

            val myUsername = getUsername(requireContext())
            if (myUsername.isEmpty()) {
                Log.e(TAG, "Username is empty, cannot load following list")
                return
            }

            val response = retrofitInstance.apiService.getOtherUserFollowing(
                username = myUsername,
                page = 1,
                limit = 1000
            )

            if (response.isSuccessful && response.body() != null) {
                val followingUsers = response.body()!!.data

                followingUserIds.clear()
                followingUserMap.clear()

                //  Create lists for both IDs and usernames
                val followingIdsList = mutableListOf<String>()
                val followingUsernamesList = mutableListOf<String>()

                followingUsers?.forEach { user ->
                    val userId = user._id
                    val username = user.username

                    if (userId.isNotEmpty()) {
                        followingUserIds.add(userId)
                        followingUserMap[userId] = username

                        // Add to both lists
                        followingIdsList.add(userId)
                        followingUsernamesList.add(username)

                        Log.d(TAG, "  ✓ Following: @$username (ID: $userId)")
                    }
                }

                hasLoadedFollowingList = true

                // Update the adapter's cached lists
                if (::followedPostsAdapter.isInitialized) {
                    FeedAdapter.setCachedFollowingList(followingUserIds)
                    followedPostsAdapter.updateFollowingList(followingIdsList)
                    followedPostsAdapter.updateFollowingUsernames(followingUsernamesList)
                    Log.d(TAG, "✓ Updated adapter with ${followingUserIds.size} following users")
                }

                Log.d(TAG, "Successfully loaded ${followingUserIds.size} following users")

            } else {
                Log.e(TAG, "API error: ${response.code()}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error loading following list: ${e.message}", e)
        }
    }

    fun updateFollowingList(followingIds: Set<String>) {

        Log.d("FollowingFragment", "Received ${followingIds.size} following IDs")

        // Update local set for filtering logic
        this.followingUserIds.clear()
        this.followingUserIds.addAll(followingIds)
        this.hasLoadedFollowingList = true

        // Extract usernames from followingUserMap
        val followingUsernames = followingUserMap.values.toList()

        // Update adapter with BOTH IDs and usernames
        if (::followedPostsAdapter.isInitialized) {
            FeedAdapter.setCachedFollowingList(followingIds)
            followedPostsAdapter.updateFollowingList(followingIds.toList())
            followedPostsAdapter.updateFollowingUsernames(followingUsernames)
            followedPostsAdapter.notifyDataSetChanged()
            Log.d("FollowingFragment", "Updated adapter with ${followingIds.size} IDs and ${followingUsernames.size} usernames")
        } else {
            Log.w("FollowingFragment", "Adapter not initialized yet")
        }
    }

    private fun handleError(message: String) {
        Log.e(TAG, message)
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onResume() {
        super.onResume()

        if (isVisible) {
            clearAndReloadFeed()
        }

        getFeedViewModel.isResuming = true
        Log.d("getCurrentLocation", "onResume: ${getFeedViewModel.isResuming}")
        Log.d(TAG, "onResume: currentAdapterPosition $currentAdapterPosition")
        Log.d(TAG, "onResume: called")

        feedListView.visibility = View.VISIBLE
        frameLayout.visibility = View.GONE



        EventBus.getDefault().post(ShowBottomNav(false))
        EventBus.getDefault().post(ShowFeedFloatingActionButton(false))
        EventBus.getDefault().post(ShowAppBar(false))
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: called")
        EventBus.getDefault().unregister(this)
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
                feedMixedFilesViewFragment = FeedMixedFilesViewFragment()
                feedMixedFilesViewFragment?.setListener(this)
                val args = Bundle().apply {
                    putInt("position", position)
                    putSerializable("data", data) // Adjust type if needed
                }
                feedMixedFilesViewFragment?.arguments = args
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.feed_text_view_fragment,
                        feedMixedFilesViewFragment!!
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




    interface FeedClickListener {
        fun followButtonClicked(followUnFollowEntity: FollowUnFollowEntity, followButton: AppCompatButton)

        // Add this method to notify the parent UI that a user was unfollowed
        fun userUnfollowed(followUnFollowEntity: FollowUnFollowEntity)
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
        TODO("Not yet implemented")
    }

    override fun feedClickedToOriginalPost(position: Int, originalPostId: String) {
        TODO("Not yet implemented")
    }

    override fun onImageClick() {
        TODO("Not yet implemented")
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
        EventBus.getDefault().post(FeedCommentClicked(position, data))
    }

    override fun onLikeUnLikeFeedFromFeedTextViewFragment(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.Post
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
}

private fun Fragment_Original_Post_With_Repost_Inside?.setListener(
    fragment: com.uyscuti.social.circuit.User_Interface.fragments.feed.FollowingFragment
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