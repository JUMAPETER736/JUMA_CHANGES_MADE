package com.uyscuti.social.circuit.user_interface.userProfile

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.uyscuti.sharedmodule.adapter.feed.FeedAdapter
import com.uyscuti.sharedmodule.adapter.feed.OnFeedClickListener
import com.uyscuti.sharedmodule.model.ShortsFollowButtonClicked
import com.uyscuti.social.circuit.databinding.MyUserFavoritesFragmentBinding
import com.uyscuti.social.circuit.ui.LoginActivity.UserStorageHelper.getUsername
import com.uyscuti.social.core.common.data.room.entity.FollowUnFollowEntity
import com.uyscuti.social.network.api.response.posts.File
import com.uyscuti.social.network.api.response.posts.OriginalPost
import com.uyscuti.social.network.api.response.posts.Post
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject
import kotlin.collections.isNotEmpty
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


@AndroidEntryPoint
class MyUserFavoritesFragment : Fragment(), OnFeedClickListener {

    companion object {
        private const val TAG = "MyUserFavoritesFragment"
        private const val ARG_USER_ID = "userId"
        private const val ARG_USERNAME = "username"

        private val favoritesCache = mutableMapOf<String, MutableList<Post>>()
        private val cacheTimestamp = mutableMapOf<String, Long>()
        private const val CACHE_VALIDITY_MS = 5 * 60 * 1000L
        private const val INITIAL_LOAD_SIZE = 10
        private const val MAX_PAGES = 5

        fun newInstance(userId: String, username: String): MyUserFavoritesFragment {
            return MyUserFavoritesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_USER_ID, userId)
                    putString(ARG_USERNAME, username)
                }
            }
        }

        fun clearCache(userId: String) {
            favoritesCache.remove(userId)
            cacheTimestamp.remove(userId)
        }
    }

    private var _binding: MyUserFavoritesFragmentBinding? = null
    private val binding get() = _binding!!

    private var userId: String? = null
    private var username: String? = null
    private var cleanUsername: String? = null

    @Inject
    lateinit var retrofitInstance: RetrofitInstance

    private lateinit var feedAdapter: FeedAdapter

    private val allUserFavorites = mutableListOf<Post>()
    private var isDataLoaded = false

    // ✅ Follow management - same as FollowingFragment
    private var followingUserIds = mutableSetOf<String>()
    private var blockedUserIds = mutableSetOf<String>()
    private val followingUserMap = mutableMapOf<String, String>()
    private var hasLoadedFollowingList = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString(ARG_USER_ID)
            username = it.getString(ARG_USERNAME)
            cleanUsername = username?.trim()?.lowercase()
        }

        // ✅ Register for EventBus (follow/unfollow events)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MyUserFavoritesFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        // ✅ Load following lists in background WITHOUT blocking bookmark loading
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                loadFollowingUserIds()
                loadMyFollowersList()
                loadBlockedUsers()
            } catch (e: Exception) {
                Log.e(TAG, "Error loading follow lists: ${e.message}", e)
            }
        }

        // ✅ Load bookmarks immediately - don't wait for following lists
        if (isCacheValid()) {
            val cachedData = favoritesCache[userId] ?: mutableListOf()
            allUserFavorites.clear()
            allUserFavorites.addAll(cachedData)
            isDataLoaded = true
            displayCachedData(cachedData)
        } else {
            allUserFavorites.clear()
            isDataLoaded = false
            loadBookmarkedPostsOptimized()
        }
    }

    private fun isCacheValid(): Boolean {
        val cached = favoritesCache[userId]
        val timestamp = cacheTimestamp[userId]

        if (cached == null || timestamp == null) return false

        val currentTime = System.currentTimeMillis()
        return (currentTime - timestamp) < CACHE_VALIDITY_MS
    }

    @SuppressLint("SetTextI18n")
    private fun displayCachedData(cachedPosts: List<Post>) {
        if (cachedPosts.isEmpty()) {
            showEmptyState()
        } else {
            feedAdapter.submitItems(cachedPosts)
            feedAdapter.initializeCommentCounts(cachedPosts)

            // ✅ Update following list in adapter
            feedAdapter.updateFollowingList(followingUserIds.toList())
            feedAdapter.updateFollowingUsernames(followingUserMap.values.toList())

            showContent()
        }
    }

    private fun setupRecyclerView() {
        val parentActivity = requireActivity()

        feedAdapter = FeedAdapter(
            requireContext(),
            retrofitInstance,
            this,
            fragmentManager = parentActivity.supportFragmentManager
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = feedAdapter
            visibility = View.GONE
            setHasFixedSize(true)
            setItemViewCacheSize(20)
        }

        feedAdapter.recyclerView = binding.recyclerView
    }

    @SuppressLint("SetTextI18n")
    private fun loadBookmarkedPostsOptimized() {
        if (isDataLoaded) return

        isDataLoaded = true
        showLoading()
        allUserFavorites.clear()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "═══════════════════════════════════════")
                Log.d(TAG, "LOADING BOOKMARKED POSTS")
                Log.d(TAG, "═══════════════════════════════════════")

                val firstPageResponse = retrofitInstance.apiService.getFavoriteFeed(page = "1")

                if (firstPageResponse.isSuccessful) {
                    val firstPagePosts = firstPageResponse.body()?.data?.bookmarkedPosts ?: emptyList()

                    Log.d(TAG, "✅ API returned ${firstPagePosts.size} bookmarked posts on page 1")

                    val firstBatch = firstPagePosts
                        .take(INITIAL_LOAD_SIZE)
                        .mapNotNull { post ->
                            val validated = validateAndFixPost(post)
                            if (validated == null) {
                                Log.w(TAG, "⚠️ Post ${post._id} failed validation - SKIPPED")
                            } else {
                                Log.d(TAG, "✅ Post ${post._id} validated successfully")
                            }
                            validated
                        }

                    Log.d(TAG, "First batch: ${firstBatch.size} valid posts out of ${INITIAL_LOAD_SIZE} attempted")

                    if (firstBatch.isNotEmpty()) {
                        allUserFavorites.addAll(firstBatch)

                        withContext(Dispatchers.Main) {
                            hideLoading()
                            Log.d(TAG, "📱 Submitting ${firstBatch.size} posts to adapter")
                            feedAdapter.submitItems(firstBatch)
                            feedAdapter.initializeCommentCounts(firstBatch)

                            // ✅ Update following lists (these might still be loading, that's OK)
                            if (followingUserIds.isNotEmpty()) {
                                feedAdapter.updateFollowingList(followingUserIds.toList())
                                feedAdapter.updateFollowingUsernames(followingUserMap.values.toList())
                                Log.d(TAG, "📋 Updated adapter with ${followingUserIds.size} following users")
                            } else {
                                Log.d(TAG, "⏳ Following list not loaded yet - will update when ready")
                            }

                            showContent()
                        }
                    } else {
                        Log.w(TAG, "⚠️ No valid posts in first batch!")
                        withContext(Dispatchers.Main) {
                            hideLoading()
                            showEmptyState()
                        }
                        return@launch
                    }

                    val remainingFirstPage = firstPagePosts
                        .drop(INITIAL_LOAD_SIZE)
                        .mapNotNull { post ->
                            val validated = validateAndFixPost(post)
                            if (validated != null) {
                                Log.d(TAG, "✅ Remaining post ${post._id} validated")
                            }
                            validated
                        }

                    Log.d(TAG, "Remaining on page 1: ${remainingFirstPage.size} valid posts")

                    loadRemainingDataInBackground(remainingFirstPage)

                } else {
                    Log.e(TAG, "❌ API call failed: ${firstPageResponse.code()}")
                    Log.e(TAG, "Error body: ${firstPageResponse.errorBody()?.string()}")
                    withContext(Dispatchers.Main) {
                        hideLoading()
                        handleError("Failed to load bookmarks: ${firstPageResponse.code()}")
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception loading bookmarked posts: ${e.message}", e)
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    hideLoading()
                    handleError("Error loading bookmarks: ${e.message}")
                }
            }
        }
    }

    private suspend fun loadRemainingDataInBackground(remainingFirstPage: List<Post>) {
        try {
            Log.d(TAG, "═══════════════════════════════════════")
            Log.d(TAG, "LOADING REMAINING PAGES")
            Log.d(TAG, "═══════════════════════════════════════")

            if (remainingFirstPage.isNotEmpty()) {
                Log.d(TAG, "Adding ${remainingFirstPage.size} remaining posts from page 1")
                allUserFavorites.addAll(remainingFirstPage)
                updateUI()
            }

            var currentPage = 2
            var hasMorePages = true
            var totalLoaded = allUserFavorites.size

            while (hasMorePages && currentPage <= MAX_PAGES) {
                Log.d(TAG, "Loading page $currentPage...")

                val response = retrofitInstance.apiService.getFavoriteFeed(
                    page = currentPage.toString()
                )

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val posts = responseBody?.data?.bookmarkedPosts ?: emptyList()

                    Log.d(TAG, "Page $currentPage: API returned ${posts.size} posts")

                    val validPosts = posts.mapNotNull { post ->
                        val validated = validateAndFixPost(post)
                        if (validated == null) {
                            Log.w(TAG, "  ⚠️ Post ${post._id} failed validation")
                        }
                        validated
                    }

                    Log.d(TAG, "Page $currentPage: ${validPosts.size} valid posts")

                    if (validPosts.isNotEmpty()) {
                        allUserFavorites.addAll(validPosts)
                        totalLoaded += validPosts.size
                        Log.d(TAG, "Total loaded so far: $totalLoaded posts")
                        updateUI()
                    }

                    hasMorePages = responseBody?.data?.hasNextPage ?: false
                    val totalPages = responseBody?.data?.totalPages ?: currentPage

                    Log.d(TAG, "Has more pages: $hasMorePages, Total pages: $totalPages")

                    if (!hasMorePages || currentPage >= totalPages) {
                        hasMorePages = false
                    } else {
                        currentPage++
                        delay(200) // Small delay between pages
                    }
                } else {
                    Log.e(TAG, "Page $currentPage failed: ${response.code()}")
                    hasMorePages = false
                }
            }

            if (allUserFavorites.isNotEmpty()) {
                favoritesCache[userId!!] = allUserFavorites.toMutableList()
                cacheTimestamp[userId!!] = System.currentTimeMillis()
                Log.d(TAG, "✅ Cached ${allUserFavorites.size} bookmarked posts")
            }

            withContext(Dispatchers.Main) {
                if (allUserFavorites.isEmpty()) {
                    Log.w(TAG, "⚠️ No bookmarked posts found after loading all pages")
                    showEmptyState()
                } else {
                    Log.d(TAG, "✅ FINISHED: Total ${allUserFavorites.size} bookmarked posts loaded")
                }
            }

            Log.d(TAG, "═══════════════════════════════════════")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Background loading error: ${e.message}", e)
            e.printStackTrace()
        }
    }

    private suspend fun updateUI() {
        withContext(Dispatchers.Main) {
            if (allUserFavorites.isEmpty()) {
                showEmptyState()
            } else {
                feedAdapter.submitItems(allUserFavorites)
                feedAdapter.initializeCommentCounts(allUserFavorites)

                // ✅ Always update following lists when UI updates
                feedAdapter.updateFollowingList(followingUserIds.toList())
                feedAdapter.updateFollowingUsernames(followingUserMap.values.toList())

                showContent()
            }
        }
    }

    // ✅ Load following users - same as FollowingFragment
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

                withContext(Dispatchers.Main) {
                    if (::feedAdapter.isInitialized) {
                        FeedAdapter.setCachedFollowingList(followingUserIds)
                        feedAdapter.updateFollowingList(followingIdsList)
                        feedAdapter.updateFollowingUsernames(followingUsernamesList)
                        feedAdapter.notifyDataSetChanged()
                        Log.d(TAG, "Updated adapter with ${followingUserIds.size} following users")
                    }
                }

                Log.d(TAG, "Successfully loaded ${followingUserIds.size} following users")

            } else {
                Log.e(TAG, "API error: ${response.code()}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error loading following list: ${e.message}", e)
        }
    }

    // ✅ Load my followers - same as FollowingFragment
    private suspend fun loadMyFollowersList() {
        try {
            Log.d(TAG, "Loading MY followers list...")

            val myUsername = getUsername(requireContext())
            if (myUsername.isEmpty()) {
                Log.e(TAG, "Username is empty, cannot load my followers list")
                return
            }

            val response = retrofitInstance.apiService.getOtherUserFollowers(
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

                FeedAdapter.setMyFollowersList(myFollowerIds)
                Log.d(TAG, "Populated my followers cache with ${myFollowerIds.size} followers")

            } else {
                Log.e(TAG, "API error loading my followers: ${response.code()}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error loading my followers list: ${e.message}", e)
        }
    }

    // ✅ Load blocked users - same as FollowingFragment
    private suspend fun loadBlockedUsers() {
        try {
            Log.d(TAG, "Loading blocked users...")

            val response = retrofitInstance.apiService.getAllBlockedUsers(page = 1, limit = 100)

            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!

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

    private fun validateAndFixPost(post: Post): Post? {
        try {
            // ✅ Mark as bookmarked
            post.isBookmarked = true

            if (post.isReposted == true && !post.originalPost.isNullOrEmpty()) {
                val originalPost = post.originalPost[0]

                // ✅ Validate original post author has complete data
                if (originalPost.author == null) {
                    Log.w(TAG, "Original post ${originalPost._id} has null author - SKIPPING")
                    return null
                }

                if (originalPost.author.account == null) {
                    Log.w(TAG, "Original post ${originalPost._id} author has null account - SKIPPING")
                    return null
                }

                // ✅ Ensure author has avatar data
                if (originalPost.author.account.avatar == null) {
                    Log.w(TAG, "Original post ${originalPost._id} author has null avatar - using default")
                    // Don't skip, just log warning - ViewHolder will handle null avatar
                }

                // ✅ Validate username exists
                if (originalPost.author.account.username.isNullOrBlank()) {
                    Log.w(TAG, "Original post ${originalPost._id} author has no username - SKIPPING")
                    return null
                }

                // ✅ Log author info for debugging
                Log.d(TAG, "Original Post Author: @${originalPost.author.account.username}")
                Log.d(TAG, "  Name: ${originalPost.author.firstName} ${originalPost.author.lastName}")
                Log.d(TAG, "  Owner ID: ${originalPost.author.owner}")
                Log.d(TAG, "  Avatar URL: ${originalPost.author.account.avatar?.url ?: "none"}")

                post.comments = originalPost.commentCount ?: 0
                post.likes = originalPost.likeCount ?: 0
                post.bookmarkCount = originalPost.bookmarkCount ?: 0
                post.repostCount = originalPost.repostCount ?: 0
                post.shareCount = 0

                if (post.contentType.isNullOrEmpty() || post.contentType == "mixed") {
                    post.contentType = determineContentType(originalPost.files, post.files, originalPost.content, post.content)
                }

            } else {
                // ✅ Validate main post author has complete data
                if (post.author == null) {
                    Log.w(TAG, "Post ${post._id} has null author - SKIPPING")
                    return null
                }

                if (post.author.account == null) {
                    Log.w(TAG, "Post ${post._id} author has null account - SKIPPING")
                    return null
                }

                // ✅ Ensure author has avatar data
                if (post.author.account.avatar == null) {
                    Log.w(TAG, "Post ${post._id} author has null avatar - using default")
                    // Don't skip, just log warning
                }

                // ✅ Validate username exists
                if (post.author.account.username.isNullOrBlank()) {
                    Log.w(TAG, "Post ${post._id} author has no username - SKIPPING")
                    return null
                }

                // ✅ Log author info for debugging
                Log.d(TAG, "Post Author: @${post.author.account.username}")
                Log.d(TAG, "  Name: ${post.author.firstName} ${post.author.lastName}")
                Log.d(TAG, "  Account ID: ${post.author.account._id}")
                Log.d(TAG, "  Avatar URL: ${post.author.account.avatar?.url ?: "none"}")

                post.comments = post.comments ?: 0
                post.likes = post.likes ?: 0
                post.bookmarkCount = post.bookmarkCount ?: 0
                post.repostCount = post.repostCount ?: 0
                post.shareCount = post.shareCount ?: 0

                if (post.contentType.isNullOrEmpty()) {
                    post.contentType = determineContentType(post.files, emptyList(), post.content, null)
                }
            }

            // ✅ Final validation: ensure we have the essential author data for display
            val hasValidAuthor = if (post.isReposted == true && !post.originalPost.isNullOrEmpty()) {
                val origAuthor = post.originalPost[0].author
                origAuthor != null &&
                        origAuthor.account != null &&
                        !origAuthor.account.username.isNullOrBlank()
            } else {
                post.author != null &&
                        post.author.account != null &&
                        !post.author.account.username.isNullOrBlank()
            }

            if (!hasValidAuthor) {
                Log.e(TAG, "Post ${post._id} failed final author validation - SKIPPING")
                return null
            }

            return post
        } catch (e: Exception) {
            Log.e(TAG, "Error validating Post ${post._id}: ${e.message}", e)
            return null
        }
    }

    private fun determineContentType(
        primaryFiles: List<File>?,
        secondaryFiles: List<File>,
        primaryContent: String?,
        secondaryContent: String?
    ): String {
        val files = primaryFiles ?: secondaryFiles
        return when {
            files.isNotEmpty() -> {
                when {
                    files.size > 1 -> "mixed_files"
                    files.any { it.fileId == "video" } -> "videos"
                    else -> "mixed_files"
                }
            }
            !primaryContent.isNullOrEmpty() || !secondaryContent.isNullOrEmpty() -> "text"
            else -> "text"
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.emptyView.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showContent() {
        binding.emptyView.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
    }

    @SuppressLint("SetTextI18n")
    private fun showEmptyState() {
        binding.emptyView.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
    }

    private fun handleError(message: String) {
        isDataLoaded = false
        hideLoading()

        if (allUserFavorites.isEmpty()) {
            showEmptyState()
        } else {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ Listen for follow/unfollow events from other fragments
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFollowEvent(event: ShortsFollowButtonClicked) {
        val followEntity = event.followUnFollowEntity

        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "FOLLOW EVENT in MyUserFavoritesFragment")
        Log.d(TAG, "User: ${followEntity.userId}")
        Log.d(TAG, "isFollowing: ${followEntity.isFollowing}")
        Log.d(TAG, "═══════════════════════════════════════")

        if (followEntity.isFollowing) {
            // User followed someone new
            followingUserIds.add(followEntity.userId)
            FeedAdapter.addToFollowingCache(followEntity.userId)
            Log.d(TAG, "Added user to following list. Total following: ${followingUserIds.size}")
        } else {
            // User unfollowed someone
            followingUserIds.remove(followEntity.userId)
            FeedAdapter.removeFromFollowingCache(followEntity.userId)
            Log.d(TAG, "Removed user from following list. Total following: ${followingUserIds.size}")
        }

        // ✅ Refresh adapter to update follow buttons
        feedAdapter.updateFollowingList(followingUserIds.toList())
        feedAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        feedAdapter.updatePosts(emptyList())
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    // ✅ OnFeedClickListener implementations
    override fun likeUnLikeFeed(position: Int, data: Post) {
        Log.d(TAG, "Like clicked at position $position - handled by FeedPostViewHolder")
    }

    override fun feedCommentClicked(position: Int, data: Post) {
        Log.d(TAG, "Comment clicked at position $position - handled by FeedPostViewHolder")
    }

    override fun feedFavoriteClick(position: Int, data: Post) {
        Log.d(TAG, "Favorite clicked at position $position - handled by FeedPostViewHolder")

        // When user unbookmarks, remove from list immediately
        if (data.isBookmarked == false) {
            allUserFavorites.removeAt(position)
            feedAdapter.submitItems(allUserFavorites)

            // Update cache
            favoritesCache[userId!!] = allUserFavorites.toMutableList()

            if (allUserFavorites.isEmpty()) {
                showEmptyState()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun followButtonClicked(
        followUnFollowEntity: FollowUnFollowEntity,
        followButton: AppCompatButton
    ) {
        Log.d(TAG, "Follow button clicked - handled by FeedPostViewHolder")

        // ✅ Update local following list
        if (followUnFollowEntity.isFollowing) {
            followingUserIds.add(followUnFollowEntity.userId)
            followButton.visibility = View.GONE
            Log.d(TAG, "Now following user: ${followUnFollowEntity.userId}")
        } else {
            followingUserIds.remove(followUnFollowEntity.userId)
            followButton.text = "Follow"
            followButton.visibility = View.VISIBLE
            Log.d(TAG, "Unfollowed user: ${followUnFollowEntity.userId}")
        }

        // ✅ Update adapter's following list
        feedAdapter.updateFollowingList(followingUserIds.toList())
    }

    override fun moreOptionsClick(position: Int, data: Post) {
        Log.d(TAG, "More options clicked at position $position - handled by FeedPostViewHolder")
    }

    override fun feedFileClicked(position: Int, data: Post) {
        Log.d(TAG, "File clicked at position $position - handled by FeedPostViewHolder")
    }

    override fun feedRepostFileClicked(position: Int, data: OriginalPost) {
        Log.d(TAG, "Repost file clicked at position $position - handled by FeedPostViewHolder")
    }

    override fun feedShareClicked(position: Int, data: Post) {
        Log.d(TAG, "Share clicked at position $position - handled by FeedPostViewHolder")
    }

    override fun feedRepostPost(position: Int, data: Post) {
        Log.d(TAG, "Repost clicked at position $position - handled by FeedPostViewHolder")
    }

    override fun feedRepostPostClicked(position: Int, data: Post) {
        Log.d(TAG, "Repost post clicked at position $position - handled by FeedPostViewHolder")
    }

    override fun feedClickedToOriginalPost(position: Int, originalPostId: String) {
        Log.d(TAG, "Original post clicked: $originalPostId - handled by FeedPostViewHolder")
    }

    override fun onImageClick() {
        Log.d(TAG, "Image clicked - handled by FeedPostViewHolder")
    }
}