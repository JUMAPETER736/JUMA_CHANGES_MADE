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
import com.uyscuti.social.circuit.adapter.feed.FeedAdapter
import com.uyscuti.social.circuit.adapter.feed.OnFeedClickListener
import com.uyscuti.social.circuit.databinding.AllOtherUsersFavoritesFragmentBinding
import com.uyscuti.social.core.common.data.room.entity.FollowUnFollowEntity
import com.uyscuti.social.network.api.response.posts.OriginalPost
import com.uyscuti.social.network.api.response.posts.Post
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class AllOtherUsersFavoritesFragment : Fragment(), OnFeedClickListener {

    private var _binding: AllOtherUsersFavoritesFragmentBinding? = null
    private val binding get() = _binding!!

    private var userId: String? = null
    private var username: String? = null
    private var cleanUsername: String? = null

    @Inject
    lateinit var retrofitInstance: RetrofitInstance

    private lateinit var feedAdapter: FeedAdapter

    private var currentPage = 1
    private var isLoading = false
    private var hasMoreData = true
    private var totalUserFavorites = 0
    private val allUserFavorites = mutableListOf<Post>()
    private var hasLoadedOnce = false

    // 🚀 SPEED: Caching system
    private var cachedFavorites: List<Post>? = null
    private var cacheTimestamp: Long = 0L
    private val CACHE_DURATION_MS = 5 * 60 * 1000L // 5 minutes

    // 🚀 SPEED: Pagination limits
    private val MAX_PAGES = 10
    private val MAX_ITEMS = 50

    companion object {
        private const val TAG = "AllUserFavoritesFragment"
        private const val ARG_USER_ID = "userId"
        private const val ARG_USERNAME = "username"

        // 🚀 SPEED: Static cache shared across instances
        private val staticCache = mutableMapOf<String, Pair<List<Post>, Long>>()

        fun newInstance(userId: String, username: String): AllOtherUsersFavoritesFragment {
            return AllOtherUsersFavoritesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_USER_ID, userId)
                    putString(ARG_USERNAME, username)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString(ARG_USER_ID)
            username = it.getString(ARG_USERNAME)
            cleanUsername = username?.trim()?.lowercase()
        }

        Log.d(TAG, "Fragment initialized - userId: $userId, username: $username")

        // 🚀 SPEED: Check static cache immediately
        userId?.let { id ->
            staticCache[id]?.let { (favorites, timestamp) ->
                if ((System.currentTimeMillis() - timestamp) < CACHE_DURATION_MS) {
                    cachedFavorites = favorites
                    cacheTimestamp = timestamp
                    hasLoadedOnce = true
                    Log.d(TAG, "⚡ Loaded ${favorites.size} favorites from static cache!")
                }
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

        setupRecyclerViewOptimized()

        // 🚀 SPEED: Instant display from cache if available
        if (cachedFavorites != null && isCacheValid()) {
            Log.d(TAG, "⚡ Displaying cached favorites instantly!")
            displayCachedData()

            // Background refresh if cache is getting stale
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
        allUserFavorites.clear()
        allUserFavorites.addAll(cachedFavorites!!)
        totalUserFavorites = allUserFavorites.size

        feedAdapter.clear()
        feedAdapter.submitItems(allUserFavorites)
        feedAdapter.initializeCommentCounts(allUserFavorites)

        if (allUserFavorites.isEmpty()) {
            showEmptyState()
        } else {
            showContent()
        }

        Log.d(TAG, "⚡ Displayed ${allUserFavorites.size} cached items instantly")
    }

    private fun resetAndLoadFresh() {
        allUserFavorites.clear()
        totalUserFavorites = 0
        currentPage = 1
        isLoading = false
        hasMoreData = true

        loadAllFavoritesOptimized()
    }

    private fun setupRecyclerViewOptimized() {
        Log.d(TAG, "Setting up optimized RecyclerView")

        feedAdapter = FeedAdapter(requireContext(), this)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = feedAdapter
            visibility = View.GONE

            // 🚀 PERFORMANCE OPTIMIZATIONS
            setHasFixedSize(true)
            setItemViewCacheSize(20)
            isNestedScrollingEnabled = true

            // RecycledViewPool for better performance
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

                    // Only load more if we've finished initial batch load
                    if (!isLoading && hasMoreData && hasLoadedOnce) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5
                            && firstVisibleItemPosition >= 0) {
                            loadAllFavoritesOptimized()
                        }
                    }
                }
            })
        }

        feedAdapter.recyclerView = binding.recyclerView
    }

    @SuppressLint("SetTextI18n")
    private fun loadAllFavoritesOptimized() {
        // 🚀 SPEED: Strict limits to prevent infinite loading
        if (isLoading || !hasMoreData || currentPage > MAX_PAGES || allUserFavorites.size >= MAX_ITEMS) {
            if (currentPage > MAX_PAGES) {
                Log.d(TAG, "⚠️ Reached max pages ($MAX_PAGES) - stopping")
            }
            if (allUserFavorites.size >= MAX_ITEMS) {
                Log.d(TAG, "⚠️ Reached max items ($MAX_ITEMS) - stopping")
            }
            hasMoreData = false
            hasLoadedOnce = true
            return
        }

        isLoading = true
        Log.d(TAG, "⚡ Loading favorites page $currentPage for @$username")

        if (currentPage == 1) {
            showLoading()
            allUserFavorites.clear()
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = retrofitInstance.apiService.getAllFeed(currentPage.toString())

                Log.d(TAG, "API Response - Success: ${response.isSuccessful}, Code: ${response.code()}")

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val posts = responseBody?.data?.data?.posts ?: emptyList()

                    Log.d(TAG, "Page $currentPage: Received ${posts.size} posts from API")

                    // Filter for user's favorites (placeholder - replace with real logic)
                    val userFavorites = posts.mapNotNull { post ->
                        // TODO: Add real bookmark filtering logic here
                        // Example: if (post.isBookmarkedByUser(userId)) post else null
                        post // Placeholder: keeping all posts
                    }

                    Log.d(TAG, "Page $currentPage: Found ${userFavorites.size} favorites for @$username")

                    withContext(Dispatchers.Main) {
                        if (userFavorites.isNotEmpty()) {
                            val validatedPosts = userFavorites.mapNotNull { validateAndFixPost(it) }

                            if (validatedPosts.isNotEmpty()) {
                                allUserFavorites.addAll(validatedPosts)

                                if (currentPage == 1) {
                                    feedAdapter.clear()
                                }
                                feedAdapter.submitItems(validatedPosts)
                                feedAdapter.initializeCommentCounts(validatedPosts)

                                totalUserFavorites = allUserFavorites.size

                                Log.d(TAG, "✅ Added ${validatedPosts.size} favorites. Total: $totalUserFavorites")

                                showContent()
                            }
                        }

                        isLoading = false
                    }

                    // Check pagination
                    val hasNextPage = responseBody?.data?.data?.hasNextPage ?: false
                    val totalPages = responseBody?.data?.data?.totalPages ?: currentPage

                    Log.d(TAG, "Pagination: hasNext=$hasNextPage, page=$currentPage/$totalPages")

                    // 🚀 SPEED: Load initial batch (3-5 pages) automatically, then stop
                    val shouldContinueInitialLoad = !hasLoadedOnce &&
                            currentPage < 5 &&
                            currentPage < MAX_PAGES &&
                            allUserFavorites.size < MAX_ITEMS

                    if (hasNextPage && currentPage < totalPages && shouldContinueInitialLoad) {
                        currentPage++
                        Log.d(TAG, "🔄 Auto-loading next page: $currentPage")
                        withContext(Dispatchers.Main) {
                            isLoading = false
                            loadAllFavoritesOptimized()
                        }
                    } else {
                        // Finished initial load or hit limits
                        hasMoreData = hasNextPage && currentPage < totalPages &&
                                currentPage < MAX_PAGES &&
                                allUserFavorites.size < MAX_ITEMS
                        hasLoadedOnce = true

                        if (hasNextPage && currentPage < totalPages) {
                            currentPage++ // Prepare for scroll-triggered load
                        }

                        withContext(Dispatchers.Main) {
                            hideLoading()

                            // 🚀 SPEED: Update cache
                            cachedFavorites = allUserFavorites.toList()
                            cacheTimestamp = System.currentTimeMillis()
                            userId?.let { id ->
                                staticCache[id] = Pair(cachedFavorites!!, cacheTimestamp)
                            }

                            if (allUserFavorites.isEmpty()) {
                                showEmptyState()
                            }

                            Log.d(TAG, "✅ FINISHED - Loaded $currentPage pages, ${allUserFavorites.size} favorites")
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

    // 🚀 SPEED: Silent background refresh
    private fun refreshInBackground() {
        if (isLoading) return

        Log.d(TAG, "🔄 Starting background refresh")
        isLoading = true

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val allRefreshedFavorites = mutableListOf<Post>()
                var page = 1

                // Load up to 3 pages in background
                while (page <= 3 && page <= MAX_PAGES) {
                    val response = retrofitInstance.apiService.getAllFeed(page.toString())

                    if (response.isSuccessful) {
                        val posts = response.body()?.data?.data?.posts ?: emptyList()
                        val userFavorites = posts.mapNotNull { validateAndFixPost(it) }
                        allRefreshedFavorites.addAll(userFavorites)

                        val hasNextPage = response.body()?.data?.data?.hasNextPage ?: false
                        if (!hasNextPage) break
                    } else {
                        break
                    }
                    page++
                }

                withContext(Dispatchers.Main) {
                    if (allRefreshedFavorites.isNotEmpty()) {
                        // Update cache silently
                        cachedFavorites = allRefreshedFavorites
                        cacheTimestamp = System.currentTimeMillis()
                        userId?.let { id ->
                            staticCache[id] = Pair(cachedFavorites!!, cacheTimestamp)
                        }

                        // Update UI if data changed significantly
                        if (allRefreshedFavorites.size != allUserFavorites.size) {
                            allUserFavorites.clear()
                            allUserFavorites.addAll(allRefreshedFavorites)
                            feedAdapter.clear()
                            feedAdapter.submitItems(allUserFavorites)
                            feedAdapter.initializeCommentCounts(allUserFavorites)
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

            return post
        } catch (e: Exception) {
            Log.e(TAG, "Error validating Post ${post._id}: ${e.message}", e)
            return null
        }
    }

    private fun showLoading() {

        binding.emptyView.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showContent() {
        binding.emptyView.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
    }

    @SuppressLint("SetTextI18n")
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

        if (allUserFavorites.isEmpty()) {
            showEmptyState()
        } else {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // OnFeedClickListener implementations (stubs)
    override fun likeUnLikeFeed(position: Int, data: Post) {}
    override fun feedCommentClicked(position: Int, data: Post) {}
    override fun feedFavoriteClick(position: Int, data: Post) {}
    override fun moreOptionsClick(position: Int, data: Post) {}
    override fun feedFileClicked(position: Int, data: Post) {}
    override fun feedRepostFileClicked(position: Int, data: OriginalPost) {}
    override fun feedShareClicked(position: Int, data: Post) {}
    override fun followButtonClicked(followUnFollowEntity: FollowUnFollowEntity, followButton: AppCompatButton) {}
    override fun feedRepostPost(position: Int, data: Post) {}
    override fun feedRepostPostClicked(position: Int, data: Post) {}
    override fun feedClickedToOriginalPost(position: Int, originalPostId: String) {}
    override fun onImageClick() {}

}