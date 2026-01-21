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
import com.uyscuti.social.circuit.databinding.MyUserFavoritesFragmentBinding
import com.uyscuti.social.core.common.data.room.entity.FollowUnFollowEntity
import com.uyscuti.social.network.api.response.posts.File
import com.uyscuti.social.network.api.response.posts.OriginalPost
import com.uyscuti.social.network.api.response.posts.Post
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.collections.isNotEmpty



@AndroidEntryPoint
class MyUserFavoritesFragment : Fragment() {

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

    @Inject
    lateinit var retrofitInstance: RetrofitInstance

    private lateinit var feedAdapter: FeedAdapter
    private lateinit var localStorage: LocalStorage

    private val allUserFavorites = mutableListOf<Post>()
    private var isDataLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString(ARG_USER_ID)
            username = it.getString(ARG_USERNAME)
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

        setupLocalStorage()
        setupRecyclerView()

        // ✅ Load ALL lists in background FIRST (like main feed does)
        lifecycleScope.launch(Dispatchers.IO) {
            loadFollowingList()
            loadFollowersList()
            loadBlockedUsers()

            // ✅ THEN load posts
            withContext(Dispatchers.Main) {
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
        }
    }

    private fun setupLocalStorage() {
        localStorage = LocalStorage.getInstance(requireContext())
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
            showContent()
        }
    }

    private fun setupRecyclerView() {
        // ✅ Create adapter with NO listener - FeedAdapter handles everything internally
        feedAdapter = FeedAdapter(
            context = requireContext(),
            retrofitInterface = retrofitInstance,
            feedClickListener = createNoOpListener(),
            fragmentManager = requireActivity().supportFragmentManager
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

    // ✅ Create a no-op listener - FeedAdapter handles all clicks internally
    private fun createNoOpListener(): OnFeedClickListener {
        return object : OnFeedClickListener {
            override fun likeUnLikeFeed(position: Int, data: Post) {
                // ✅ FeedAdapter handles this
            }

            override fun feedCommentClicked(position: Int, data: Post) {
                // ✅ FeedAdapter handles this - opens comment fragment
            }

            override fun feedFavoriteClick(position: Int, data: Post) {
                // ✅ Handle unbookmark in favorites list
                if (data.isBookmarked == false) {
                    // Remove from list when unbookmarked
                    val positionToRemove = allUserFavorites.indexOfFirst { it._id == data._id }
                    if (positionToRemove != -1) {
                        allUserFavorites.removeAt(positionToRemove)
                        feedAdapter.submitItems(allUserFavorites)

                        // Update cache
                        userId?.let {
                            favoritesCache[it] = allUserFavorites.toMutableList()
                        }

                        if (allUserFavorites.isEmpty()) {
                            showEmptyState()
                        }
                    }
                }
            }

            override fun moreOptionsClick(position: Int, data: Post) {
                // ✅ FeedAdapter handles this
            }

            override fun feedFileClicked(position: Int, data: Post) {
                // ✅ FeedAdapter handles this
            }

            override fun feedRepostFileClicked(position: Int, data: OriginalPost) {
                // ✅ FeedAdapter handles this
            }

            override fun feedShareClicked(position: Int, data: Post) {
                // ✅ FeedAdapter handles this
            }

            override fun followButtonClicked(followUnFollowEntity: FollowUnFollowEntity, followButton: AppCompatButton) {
                // ✅ FeedAdapter handles this
            }

            override fun feedRepostPost(position: Int, data: Post) {
                // ✅ FeedAdapter handles this
            }

            override fun feedRepostPostClicked(position: Int, data: Post) {
                // ✅ FeedAdapter handles this
            }

            override fun feedClickedToOriginalPost(position: Int, originalPostId: String) {
                // ✅ FeedAdapter handles this
            }

            override fun onImageClick() {
                // ✅ FeedAdapter handles this
            }
        }
    }

    // ✅ Load following list to hide Follow buttons and show correct user info
    private suspend fun loadFollowingList() {
        try {
            val currentUserId = localStorage.getUserId()
            val currentUsername = localStorage.getUsername()

            Log.d(TAG, "Loading following list for user: $currentUsername")

            // ✅ Use getOtherUserFollowing like main feed does
            val response = retrofitInstance.apiService.getOtherUserFollowing(
                username = currentUsername,
                page = 1,
                limit = 1000
            )

            if (response.isSuccessful && response.body() != null) {
                val followingUsers = response.body()!!.data

                val followingIds = mutableListOf<String>()
                val followingUsernames = mutableListOf<String>()

                followingUsers?.forEach { user ->
                    val userId = user._id
                    val username = user.username

                    if (userId.isNotEmpty()) {
                        followingIds.add(userId)
                        followingUsernames.add(username)
                        Log.d(TAG, "Following: @$username (ID: $userId)")
                    }
                }

                withContext(Dispatchers.Main) {
                    // ✅ Update FeedAdapter's following list
                    feedAdapter.updateFollowingList(followingIds)
                    feedAdapter.updateFollowingUsernames(followingUsernames)

                    // ✅ Update global cache
                    FeedAdapter.setCachedFollowingList(followingIds.toSet())

                    Log.d(TAG, "✅ Following list loaded: ${followingIds.size} users")
                    Log.d(TAG, "Following IDs: $followingIds")
                    Log.d(TAG, "Following usernames: $followingUsernames")
                }
            } else {
                Log.e(TAG, "Failed to load following list: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading following list: ${e.message}", e)
        }
    }

    // ✅ Load followers list (like UserFollowersFragment does) to populate FeedAdapter cache
    private suspend fun loadFollowersList() {
        try {
            val currentUsername = localStorage.getUsername()

            Log.d(TAG, "Loading followers list for user: $currentUsername")

            val response = retrofitInstance.apiService.getOtherUserFollowers(
                username = currentUsername,
                page = 1,
                limit = 1000
            )

            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                val followers = responseBody.data

                // Extract follower IDs
                val followerIds = followers.map { it._id }

                withContext(Dispatchers.Main) {
                    // ✅ Populate the FeedAdapter cache with YOUR followers
                    FeedAdapter.setMyFollowersList(followerIds)
                    Log.d(TAG, "✅ Followers list loaded: ${followerIds.size} followers")
                    Log.d(TAG, "Populated my followers cache for Follow Back detection")
                }
            } else {
                Log.e(TAG, "Failed to load followers list: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading followers list: ${e.message}", e)
        }
    }

    // ✅ Load blocked users (like main feed does)
    private suspend fun loadBlockedUsers() {
        try {
            Log.d(TAG, "Loading blocked users...")

            val response = retrofitInstance.apiService.getAllBlockedUsers(page = 1, limit = 100)

            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!

                // Store blocked user IDs
                val blockedUserIds = responseBody.data.blockedUsers.map { it.user._id }.toSet()

                Log.d(TAG, "✅ Loaded ${blockedUserIds.size} blocked users")
                Log.d(TAG, "Blocked user IDs: $blockedUserIds")

                // TODO: If you need to filter out blocked users' posts, you can store this
                // in a companion object or pass it to the adapter
            } else {
                Log.e(TAG, "Failed to load blocked users: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading blocked users: ${e.message}", e)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadBookmarkedPostsOptimized() {
        if (isDataLoaded) return

        isDataLoaded = true
        showLoading()
        allUserFavorites.clear()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Loading bookmarked posts for current user")

                // ✅ Load first page immediately
                val firstPageResponse = retrofitInstance.apiService.getFavoriteFeed(page = "1")

                if (firstPageResponse.isSuccessful) {
                    val firstPagePosts = firstPageResponse.body()?.data?.bookmarkedPosts ?: emptyList()

                    // ✅ Take first batch and show immediately
                    val firstBatch = firstPagePosts
                        .take(INITIAL_LOAD_SIZE)
                        .mapNotNull { validateAndFixPost(it) }

                    if (firstBatch.isNotEmpty()) {
                        allUserFavorites.addAll(firstBatch)

                        withContext(Dispatchers.Main) {
                            hideLoading()
                            feedAdapter.submitItems(firstBatch)
                            feedAdapter.initializeCommentCounts(firstBatch)
                            showContent()
                        }
                    }

                    // ✅ Get remaining posts from first page
                    val remainingFirstPage = firstPagePosts
                        .drop(INITIAL_LOAD_SIZE)
                        .mapNotNull { validateAndFixPost(it) }

                    // ✅ Load remaining data in background
                    loadRemainingDataInBackground(remainingFirstPage)

                } else {
                    withContext(Dispatchers.Main) {
                        handleError("Failed to load bookmarks")
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Exception loading bookmarked posts: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    handleError("Error loading bookmarks: ${e.message}")
                }
            }
        }
    }

    private suspend fun loadRemainingDataInBackground(remainingFirstPage: List<Post>) {
        try {
            // ✅ Add remaining posts from first page
            if (remainingFirstPage.isNotEmpty()) {
                allUserFavorites.addAll(remainingFirstPage)
                updateUI()
            }

            // ✅ Load additional pages
            var currentPage = 2
            var hasMorePages = true

            while (hasMorePages && currentPage <= MAX_PAGES) {
                val response = retrofitInstance.apiService.getFavoriteFeed(
                    page = currentPage.toString()
                )

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val posts = responseBody?.data?.bookmarkedPosts ?: emptyList()

                    val validPosts = posts.mapNotNull { validateAndFixPost(it) }

                    if (validPosts.isNotEmpty()) {
                        allUserFavorites.addAll(validPosts)
                        updateUI()
                    }

                    hasMorePages = responseBody?.data?.hasNextPage ?: false
                    val totalPages = responseBody?.data?.totalPages ?: currentPage

                    if (!hasMorePages || currentPage >= totalPages) {
                        hasMorePages = false
                    } else {
                        currentPage++
                    }
                } else {
                    hasMorePages = false
                }
            }

            // ✅ Cache final results
            if (allUserFavorites.isNotEmpty()) {
                userId?.let {
                    favoritesCache[it] = allUserFavorites.toMutableList()
                    cacheTimestamp[it] = System.currentTimeMillis()
                }
                Log.d(TAG, "Cached ${allUserFavorites.size} bookmarked posts")
            }

            withContext(Dispatchers.Main) {
                if (allUserFavorites.isEmpty()) {
                    showEmptyState()
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Background loading error: ${e.message}", e)
        }
    }

    private suspend fun updateUI() {
        withContext(Dispatchers.Main) {
            if (allUserFavorites.isEmpty()) {
                showEmptyState()
            } else {
                feedAdapter.submitItems(allUserFavorites)
                feedAdapter.initializeCommentCounts(allUserFavorites)
                showContent()
            }
        }
    }

    private fun validateAndFixPost(post: Post): Post? {
        try {
            // ✅ All posts from bookmarks endpoint are already bookmarked
            post.isBookmarked = true

            if (post.isReposted == true && !post.originalPost.isNullOrEmpty()) {
                val originalPost = post.originalPost[0]

                if (originalPost.author?.account == null) {
                    return null
                }

                post.comments = originalPost.commentCount ?: 0
                post.likes = originalPost.likeCount ?: 0
                post.bookmarkCount = originalPost.bookmarkCount ?: 0
                post.repostCount = originalPost.repostCount ?: 0
                post.shareCount = 0

                if (post.contentType.isNullOrEmpty() || post.contentType == "mixed") {
                    post.contentType = determineContentType(originalPost.files, post.files, originalPost.content, post.content)
                }

            } else {
                if (post.author == null || post.author.account == null) {
                    return null
                }

                post.comments = post.comments ?: 0
                post.likes = post.likes ?: 0
                post.bookmarkCount = post.bookmarkCount ?: 0
                post.repostCount = post.repostCount ?: 0
                post.shareCount = post.shareCount ?: 0

                if (post.contentType.isNullOrEmpty()) {
                    post.contentType = determineContentType(post.files, emptyList(), post.content, null)
                }
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

    override fun onDestroyView() {
        super.onDestroyView()
        feedAdapter.updatePosts(emptyList())
        _binding = null
    }
}