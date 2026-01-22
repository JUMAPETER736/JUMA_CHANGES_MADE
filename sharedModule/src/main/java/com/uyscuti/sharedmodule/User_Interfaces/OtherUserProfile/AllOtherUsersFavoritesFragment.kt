package com.uyscuti.social.circuit.User_Interface.OtherUserProfile

import android.annotation.SuppressLint
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
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.sharedmodule.adapter.feed.FeedAdapter
import com.uyscuti.sharedmodule.adapter.feed.OnFeedClickListener
import com.uyscuti.sharedmodule.databinding.AllOtherUsersFavoritesFragmentBinding
import com.uyscuti.social.core.common.data.room.entity.FollowUnFollowEntity
import com.uyscuti.social.network.api.response.posts.Avatar
import com.uyscuti.social.network.api.response.posts.CoverImage
import com.uyscuti.social.network.api.response.posts.OriginalPost
import com.uyscuti.social.network.api.response.posts.Post
import com.uyscuti.social.network.api.response.posts.RepostedUser
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class AllOtherUsersFavoritesFragment : Fragment(), OnFeedClickListener {

    private var _binding: AllOtherUsersFavoritesFragmentBinding? = null
    private val binding get() = _binding!!

    private var otherUserId: String? = null  // The profile being viewed
    private var username: String? = null
    private var cleanUsername: String? = null

    private var myUserId: String? = null  // The logged-in user

    @Inject
    lateinit var retrofitInstance: RetrofitInstance

    private lateinit var feedAdapter: FeedAdapter
    private lateinit var localStorage: LocalStorage

    private var currentPage = 1
    private var isLoading = false
    private var hasMoreData = true
    private val allMutualFavorites = mutableListOf<Post>()
    private var hasLoadedOnce = false

    // Caching system
    private var cachedFavorites: List<Post>? = null
    private var cacheTimestamp: Long = 0L
    private val CACHE_DURATION_MS = 5 * 60 * 1000L

    // Pagination limits
    private val MAX_PAGES = 10
    private val MAX_ITEMS = 50

    companion object {
        private const val TAG = "MutualFavoritesFragment"
        private const val ARG_USER_ID = "userId"
        private const val ARG_USERNAME = "username"

        // Static cache: "myUserId_otherUserId" -> (favorites, timestamp)
        private val staticCache = mutableMapOf<String, Pair<List<Post>, Long>>()
        internal val favoritesCache = mutableMapOf<String, MutableList<Post>>()
        internal val cacheTimestamp = mutableMapOf<String, Long>()

        fun newInstance(userId: String, username: String): AllOtherUsersFavoritesFragment {
            return AllOtherUsersFavoritesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_USER_ID, userId)
                    putString(ARG_USERNAME, username)
                }
            }
        }

        internal fun emptyRepostedUser(): RepostedUser {
            return RepostedUser(
                _id = "", avatar = Avatar(_id = "", url = "", localPath = ""),
                bio = "", coverImage = CoverImage(_id = "", localPath = "", url = ""),
                createdAt = "", email = "", firstName = "", lastName = "",
                owner = "", updatedAt = "", username = ""
            )
        }

        fun clearCache(myUserId: String, otherUserId: String) {
            val cacheKey = "${myUserId}_${otherUserId}"
            staticCache.remove(cacheKey)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        localStorage = LocalStorage.getInstance(requireContext())
        myUserId = localStorage.getUserId()

        arguments?.let {
            otherUserId = it.getString(ARG_USER_ID)
            username = it.getString(ARG_USERNAME)
            cleanUsername = username?.trim()?.lowercase()
        }

        Log.d(TAG, "🔍 Finding mutual favorites")
        Log.d(TAG, "   My ID: $myUserId")
        Log.d(TAG, "   Other user ID: $otherUserId (@$username)")

        // Check cache
        val cacheKey = "${myUserId}_${otherUserId}"
        staticCache[cacheKey]?.let { (favorites, timestamp) ->
            if ((System.currentTimeMillis() - timestamp) < CACHE_DURATION_MS) {
                cachedFavorites = favorites
                cacheTimestamp = timestamp
                hasLoadedOnce = true
                Log.d(TAG, "⚡ Loaded ${favorites.size} mutual favorites from cache!")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AllOtherUsersFavoritesFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        // Show cached data instantly if available
        if (cachedFavorites != null && isCacheValid()) {
            Log.d(TAG, "⚡ Displaying cached mutual favorites instantly!")
            displayCachedData()

            // Background refresh if stale
            if ((System.currentTimeMillis() - cacheTimestamp) > (CACHE_DURATION_MS / 2)) {
                Log.d(TAG, "🔄 Refreshing stale cache in background")
                refreshInBackground()
            }
        } else {
            // No cache, load fresh data
            resetAndLoadFresh()
        }
    }

    private fun isCacheValid(): Boolean {
        return cachedFavorites != null &&
                (System.currentTimeMillis() - cacheTimestamp) < CACHE_DURATION_MS
    }

    private fun displayCachedData() {
        allMutualFavorites.clear()
        allMutualFavorites.addAll(cachedFavorites!!)

        feedAdapter.clear()
        feedAdapter.submitItems(allMutualFavorites)
        feedAdapter.initializeCommentCounts(allMutualFavorites)

        if (allMutualFavorites.isEmpty()) {
            showEmptyState()
        } else {
            showContent()
        }

        Log.d(TAG, "⚡ Displayed ${allMutualFavorites.size} mutual favorites")
    }

    private fun resetAndLoadFresh() {
        allMutualFavorites.clear()
        currentPage = 1
        isLoading = false
        hasMoreData = true
        loadMutualFavorites()
    }

    private fun setupRecyclerView() {
        feedAdapter = FeedAdapter(
            requireContext(),
            retrofitInstance,
            this,
            fragmentManager = childFragmentManager
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = feedAdapter
            visibility = View.GONE
            setHasFixedSize(true)
            setItemViewCacheSize(20)
            isNestedScrollingEnabled = true

            val viewPool = RecyclerView.RecycledViewPool()
            viewPool.setMaxRecycledViews(0, 15)
            setRecycledViewPool(viewPool)

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if (!isLoading && hasMoreData && hasLoadedOnce) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5 &&
                            firstVisibleItemPosition >= 0) {
                            loadMutualFavorites()
                        }
                    }
                }
            })
        }

        feedAdapter.recyclerView = binding.recyclerView
    }

    private fun loadMutualFavorites() {
        if (isLoading || !hasMoreData || currentPage > MAX_PAGES ||
            allMutualFavorites.size >= MAX_ITEMS) {
            if (currentPage > MAX_PAGES) {
                Log.d(TAG, "⚠️ Reached max pages ($MAX_PAGES)")
            }
            if (allMutualFavorites.size >= MAX_ITEMS) {
                Log.d(TAG, "⚠️ Reached max items ($MAX_ITEMS)")
            }
            hasMoreData = false
            hasLoadedOnce = true
            return
        }

        isLoading = true
        Log.d(TAG, "⚡ Loading mutual favorites page $currentPage")

        if (currentPage == 1) {
            showLoading()
            allMutualFavorites.clear()
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Fetch all bookmarked posts (includes bookmarkedBy field)
                val response = retrofitInstance.apiService.getFavoriteFeed(page = currentPage.toString())

                Log.d(TAG, "API Response - Success: ${response.isSuccessful}, Code: ${response.code()}")

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val allBookmarkedPosts = responseBody?.data?.bookmarkedPosts ?: emptyList()

                    Log.d(TAG, "Page $currentPage: Received ${allBookmarkedPosts.size} total bookmarked posts")

                    // CRITICAL: Find posts bookmarked by BOTH users
                    // Group posts by _id to see who bookmarked them
                    val postBookmarkers = mutableMapOf<String, MutableSet<String>>()
                    val postData = mutableMapOf<String, Post>()

                    allBookmarkedPosts.forEach { post ->
                        val postId = post._id
                        val bookmarkedBy = post.bookmarkedBy ?: ""

                        if (bookmarkedBy.isNotEmpty()) {
                            postBookmarkers.getOrPut(postId) { mutableSetOf() }.add(bookmarkedBy)
                            postData[postId] = post
                        }
                    }

                    // Find posts where BOTH myUserId and otherUserId appear as bookmarkers
                    val mutualPostIds = postBookmarkers.filter { (_, bookmarkers) ->
                        bookmarkers.contains(myUserId) && bookmarkers.contains(otherUserId)
                    }.keys

                    val mutualFavorites = mutualPostIds.mapNotNull { postId ->
                        postData[postId]
                    }

                    Log.d(TAG, "🎯 Page $currentPage: Found ${mutualFavorites.size} MUTUAL favorites")
                    Log.d(TAG, "   (Posts bookmarked by both @${localStorage.getUsername()} and @$username)")

                    withContext(Dispatchers.Main) {
                        if (mutualFavorites.isNotEmpty()) {
                            val validatedPosts = mutualFavorites.mapNotNull {
                                validateAndFixPost(it)
                            }

                            if (validatedPosts.isNotEmpty()) {
                                allMutualFavorites.addAll(validatedPosts)

                                if (currentPage == 1) {
                                    feedAdapter.clear()
                                }
                                feedAdapter.submitItems(validatedPosts)
                                feedAdapter.initializeCommentCounts(validatedPosts)

                                Log.d(TAG, "✅ Added ${validatedPosts.size} mutual favorites. Total: ${allMutualFavorites.size}")

                                showContent()
                            }
                        }

                        isLoading = false
                    }

                    // Check pagination
                    val hasNextPage = responseBody?.data?.hasNextPage ?: false
                    val totalPages = responseBody?.data?.totalPages ?: currentPage

                    Log.d(TAG, "Pagination: hasNext=$hasNextPage, page=$currentPage/$totalPages")

                    // Auto-load initial batch (3-5 pages)
                    val shouldContinueInitialLoad = !hasLoadedOnce &&
                            currentPage < 5 &&
                            currentPage < MAX_PAGES &&
                            allMutualFavorites.size < MAX_ITEMS

                    if (hasNextPage && currentPage < totalPages && shouldContinueInitialLoad) {
                        currentPage++
                        Log.d(TAG, "🔄 Auto-loading next page: $currentPage")
                        withContext(Dispatchers.Main) {
                            isLoading = false
                            loadMutualFavorites()
                        }
                    } else {
                        hasMoreData = hasNextPage && currentPage < totalPages &&
                                currentPage < MAX_PAGES &&
                                allMutualFavorites.size < MAX_ITEMS
                        hasLoadedOnce = true

                        if (hasNextPage && currentPage < totalPages) {
                            currentPage++
                        }

                        withContext(Dispatchers.Main) {
                            hideLoading()

                            // Update cache
                            cachedFavorites = allMutualFavorites.toList()
                            cacheTimestamp = System.currentTimeMillis()
                            val cacheKey = "${myUserId}_${otherUserId}"
                            staticCache[cacheKey] = Pair(cachedFavorites!!, cacheTimestamp)

                            if (allMutualFavorites.isEmpty()) {
                                showEmptyState()
                            }

                            Log.d(TAG, "✅ FINISHED - Loaded $currentPage pages, ${allMutualFavorites.size} mutual favorites")
                            Log.d(TAG, "Cache updated - ${cachedFavorites?.size} items stored")
                        }
                    }
                } else {
                    Log.e(TAG, "API error: ${response.code()}")
                    withContext(Dispatchers.Main) {
                        handleError("Failed to load: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    handleError("Error: ${e.message}")
                }
            }
        }
    }

    private fun refreshInBackground() {
        if (isLoading) return

        Log.d(TAG, "🔄 Starting background refresh of mutual favorites")
        isLoading = true

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val allRefreshedMutuals = mutableListOf<Post>()
                var page = 1

                // Load up to 3 pages in background
                while (page <= 3 && page <= MAX_PAGES) {
                    val response = retrofitInstance.apiService.getFavoriteFeed(page = page.toString())

                    if (response.isSuccessful) {
                        val allBookmarkedPosts = response.body()?.data?.bookmarkedPosts ?: emptyList()

                        // Find mutual favorites
                        val postBookmarkers = mutableMapOf<String, MutableSet<String>>()
                        val postData = mutableMapOf<String, Post>()

                        allBookmarkedPosts.forEach { post ->
                            val postId = post._id
                            val bookmarkedBy = post.bookmarkedBy ?: ""

                            if (bookmarkedBy.isNotEmpty()) {
                                postBookmarkers.getOrPut(postId) { mutableSetOf() }.add(bookmarkedBy)
                                postData[postId] = post
                            }
                        }

                        val mutualPostIds = postBookmarkers.filter { (_, bookmarkers) ->
                            bookmarkers.contains(myUserId) && bookmarkers.contains(otherUserId)
                        }.keys

                        val mutualFavorites = mutualPostIds.mapNotNull { postId ->
                            postData[postId]?.let { validateAndFixPost(it) }
                        }

                        allRefreshedMutuals.addAll(mutualFavorites)

                        val hasNextPage = response.body()?.data?.hasNextPage ?: false
                        if (!hasNextPage) break
                    } else {
                        break
                    }
                    page++
                }

                withContext(Dispatchers.Main) {
                    if (allRefreshedMutuals.isNotEmpty()) {
                        // Update cache silently
                        cachedFavorites = allRefreshedMutuals
                        cacheTimestamp = System.currentTimeMillis()
                        val cacheKey = "${myUserId}_${otherUserId}"
                        staticCache[cacheKey] = Pair(cachedFavorites!!, cacheTimestamp)

                        // Update UI if data changed significantly
                        if (allRefreshedMutuals.size != allMutualFavorites.size) {
                            allMutualFavorites.clear()
                            allMutualFavorites.addAll(allRefreshedMutuals)
                            feedAdapter.clear()
                            feedAdapter.submitItems(allMutualFavorites)
                            feedAdapter.initializeCommentCounts(allMutualFavorites)
                            Log.d(TAG, "✅ Background refresh completed - UI updated")
                        } else {
                            Log.d(TAG, "✅ Background refresh completed - no changes")
                        }
                    }
                    isLoading = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Background refresh failed (silent)", e)
                isLoading = false
            }
        }
    }

    private fun validateAndFixPost(post: Post): Post? {
        try {
            if (post.isReposted == true && !post.originalPost.isNullOrEmpty()) {
                val originalPost = post.originalPost[0]

                if (originalPost.author?.account == null) {
                    Log.w(TAG, "Skipping repost ${post._id}: null Original Author")
                    return null
                }

                post.comments = originalPost.commentCount ?: 0
                post.likes = originalPost.likeCount ?: 0
                post.bookmarkCount = originalPost.bookmarkCount ?: 0
                post.repostCount = originalPost.repostCount ?: 0
                post.shareCount = 0

                if (post.contentType.isNullOrEmpty() || post.contentType == "mixed") {
                    post.contentType = when {
                        !originalPost.files.isNullOrEmpty() -> {
                            when {
                                originalPost.files.size > 1 -> "mixed_files"
                                originalPost.fileTypes?.any { it.fileType == "video" } == true -> "videos"
                                else -> "mixed_files"
                            }
                        }
                        post.files.isNotEmpty() -> {
                            when {
                                post.files.size > 1 -> "mixed_files"
                                post.fileTypes?.any { it.fileType == "video" } == true -> "videos"
                                else -> "mixed_files"
                            }
                        }
                        !originalPost.content.isNullOrEmpty() || !post.content.isNullOrEmpty() -> "text"
                        else -> "text"
                    }
                }
            } else {
                if (post.author == null || post.author.account == null) {
                    Log.w(TAG, "Skipping Post ${post._id}: null Author/Account")
                    return null
                }

                if (post.comments == null) post.comments = 0
                if (post.likes == null) post.likes = 0
                if (post.bookmarkCount == null) post.bookmarkCount = 0
                if (post.repostCount == null) post.repostCount = 0
                if (post.shareCount == null) post.shareCount = 0

                if (post.contentType.isNullOrEmpty()) {
                    post.contentType = when {
                        post.files.isNotEmpty() -> {
                            when {
                                post.files.size > 1 -> "mixed_files"
                                post.fileTypes?.any { it.fileType == "video" } == true -> "videos"
                                else -> "mixed_files"
                            }
                        }
                        !post.content.isNullOrEmpty() -> "text"
                        else -> "text"
                    }
                }
            }

            // Ensure it's marked as bookmarked
            post.isBookmarked = true

            return post
        } catch (e: Exception) {
            Log.e(TAG, "Error validating Post ${post._id}: ${e.message}", e)
            return null
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

    private fun showEmptyState() {
        binding.emptyView.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
        hasMoreData = false
    }

    private fun handleError(message: String) {
        isLoading = false
        hasMoreData = false
        hasLoadedOnce = true
        hideLoading()

        if (allMutualFavorites.isEmpty()) {
            showEmptyState()
        } else {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerView.adapter = null
        _binding = null
    }

    // FeedAdapter callbacks
    override fun likeUnLikeFeed(position: Int, data: Post) {}
    override fun feedCommentClicked(position: Int, data: Post) {}
    override fun feedFavoriteClick(position: Int, data: Post) {
        // When unbookmarked, remove from mutual list
        if (!data.isBookmarked && position in allMutualFavorites.indices) {
            Log.d(TAG, "Removing unbookmarked post at position $position")
            allMutualFavorites.removeAt(position)
            feedAdapter.submitItems(allMutualFavorites)
            feedAdapter.notifyItemRemoved(position)

            // Update cache
            val cacheKey = "${myUserId}_${otherUserId}"
            cachedFavorites = allMutualFavorites.toList()
            cacheTimestamp = System.currentTimeMillis()
            staticCache[cacheKey] = Pair(cachedFavorites!!, cacheTimestamp)

            if (allMutualFavorites.isEmpty()) {
                showEmptyState()
            }
        }
    }
    override fun moreOptionsClick(position: Int, data: Post) {}
    override fun feedFileClicked(position: Int, data: Post) {}
    override fun feedRepostFileClicked(position: Int, data: OriginalPost) {}
    override fun feedShareClicked(position: Int, data: Post) {}
    override fun followButtonClicked(
        followUnFollowEntity: FollowUnFollowEntity,
        followButton: AppCompatButton
    ) {}
    override fun feedRepostPost(position: Int, data: Post) {}
    override fun feedRepostPostClicked(position: Int, data: Post) {}
    override fun feedClickedToOriginalPost(position: Int, originalPostId: String) {}
    override fun onImageClick() {}
}