package com.uyscuti.social.circuit.User_Interface.OtherUserProfile

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.adapter.feed.FeedAdapter
import com.uyscuti.social.circuit.adapter.feed.OnFeedClickListener
import com.uyscuti.social.core.common.data.room.entity.FollowUnFollowEntity
import com.uyscuti.social.network.api.response.posts.OriginalPost
import com.uyscuti.social.network.api.response.posts.Post
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.api.retrofit.interfaces.IFlashapi
import com.uyscuti.social.network.utils.LocalStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "AllOtherUsersPostsFragment"

class AllOtherUsersPostsFragment : Fragment(), OnFeedClickListener {

    companion object {
        private const val ARG_USER_ID = "userId"
        private const val ARG_USERNAME = "username"

        // 🚀 SPEED: Static cache shared across instances
        private val staticCache = mutableMapOf<String, Pair<List<Post>, Long>>()

        fun newInstance(userId: String, username: String): AllOtherUsersPostsFragment {
            return AllOtherUsersPostsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_USER_ID, userId)
                    putString(ARG_USERNAME, username)
                }
            }
        }
    }

    private lateinit var apiService: IFlashapi
    private lateinit var recyclerView: RecyclerView
    private lateinit var feedAdapter: FeedAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateText: TextView

    private var userId: String? = null
    private var username: String? = null
    private var cleanUsername: String? = null

    private var currentPage = 1
    private var isLoading = false
    private var hasMoreData = true
    private var totalUserPosts = 0
    private val allUserPosts = mutableListOf<Post>()
    private var hasLoadedOnce = false

    // 🚀 SPEED: Caching system
    private var cachedPosts: List<Post>? = null
    private var cacheTimestamp: Long = 0L
    private val CACHE_DURATION_MS = 5 * 60 * 1000L // 5 minutes

    // 🚀 SPEED: Pagination limits
    private val MAX_PAGES = 10
    private val MAX_ITEMS = 50

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString(ARG_USER_ID)
            username = it.getString(ARG_USERNAME)
            cleanUsername = username?.trim()?.lowercase()
        }

        Log.d(TAG, "Fragment initialized - userId: $userId, username: $username")

        val localStorage = LocalStorage(requireContext())
        val retrofitInstance = RetrofitInstance(localStorage, requireContext())
        apiService = retrofitInstance.apiService

        // 🚀 SPEED: Check static cache immediately
        userId?.let { id ->
            staticCache[id]?.let { (posts, timestamp) ->
                if ((System.currentTimeMillis() - timestamp) < CACHE_DURATION_MS) {
                    cachedPosts = posts
                    cacheTimestamp = timestamp
                    hasLoadedOnce = true
                    Log.d(TAG, "⚡ Loaded ${posts.size} posts from static cache!")
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.all_other_users_posts_fragment, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        progressBar = view.findViewById(R.id.progressBar)
        emptyStateText = view.findViewById(R.id.emptyStateText)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViewOptimized()

        // 🚀 SPEED: Instant display from cache if available
        if (cachedPosts != null && isCacheValid()) {
            Log.d(TAG, "⚡ Displaying cached posts instantly!")
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
        return cachedPosts != null &&
                (System.currentTimeMillis() - cacheTimestamp) < CACHE_DURATION_MS
    }

    private fun displayCachedData() {
        allUserPosts.clear()
        allUserPosts.addAll(cachedPosts!!)
        totalUserPosts = allUserPosts.size

        feedAdapter.clear()
        feedAdapter.submitItems(allUserPosts)
        feedAdapter.initializeCommentCounts(allUserPosts)

        if (allUserPosts.isEmpty()) {
            showEmptyState()
        } else {
            showContent()
        }

        Log.d(TAG, "⚡ Displayed ${allUserPosts.size} cached posts instantly")
    }

    private fun resetAndLoadFresh() {
        allUserPosts.clear()
        totalUserPosts = 0
        currentPage = 1
        isLoading = false
        hasMoreData = true

        loadAllPostsOptimized()
    }

    private fun setupRecyclerViewOptimized() {
        Log.d(TAG, "Setting up optimized RecyclerView")

        feedAdapter = FeedAdapter(requireContext(), this)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = feedAdapter
            visibility = View.VISIBLE

            // 🚀 PERFORMANCE OPTIMIZATIONS
            setHasFixedSize(true)
            setItemViewCacheSize(20)
            isNestedScrollingEnabled = true

            // RecycledViewPool for better performance
            val viewPool = RecyclerView.RecycledViewPool()
            viewPool.setMaxRecycledViews(0, 15)
            setRecycledViewPool(viewPool)

            Log.d(TAG, "RecyclerView setup with optimizations")

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
                            loadAllPostsOptimized()
                        }
                    }
                }
            })
        }

        feedAdapter.recyclerView = recyclerView
    }

    @SuppressLint("SetTextI18n")
    private fun loadAllPostsOptimized() {
        // 🚀 SPEED: Strict limits to prevent infinite loading
        if (isLoading || !hasMoreData || currentPage > MAX_PAGES || allUserPosts.size >= MAX_ITEMS) {
            if (currentPage > MAX_PAGES) {
                Log.d(TAG, "⚠️ Reached max pages ($MAX_PAGES) - stopping")
            }
            if (allUserPosts.size >= MAX_ITEMS) {
                Log.d(TAG, "⚠️ Reached max items ($MAX_ITEMS) - stopping")
            }
            hasMoreData = false
            hasLoadedOnce = true
            return
        }

        isLoading = true
        Log.d(TAG, "⚡ Loading posts page $currentPage for @$username")

        if (currentPage == 1) {

            emptyStateText.visibility = View.GONE
            allUserPosts.clear()
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getAllFeed(page = currentPage.toString())

                Log.d(TAG, "API Response - Success: ${response.isSuccessful}, Code: ${response.code()}")

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val posts = responseBody?.data?.data?.posts ?: emptyList()

                    Log.d(TAG, "Page $currentPage: Received ${posts.size} posts from API")

                    val userPosts = posts.mapNotNull { post ->
                        val isDirectPost = post.author?.let { author ->
                            val matchesUserId = author.owner == userId
                            val apiUsername = author.account?.username?.trim()?.lowercase()
                            val matchesUsername = apiUsername == cleanUsername
                            val isValid = author.account != null

                            matchesUserId && matchesUsername && isValid
                        } ?: false

                        val isRepostOfUserContent = post.isReposted == true &&
                                !post.originalPost.isNullOrEmpty() &&
                                post.originalPost[0].author?.let { originalAuthor ->
                                    val matchesUserId = originalAuthor.owner == userId
                                    val apiUsername = originalAuthor.account?.username?.trim()?.lowercase()
                                    val matchesUsername = apiUsername == cleanUsername
                                    val isValid = originalAuthor.account != null

                                    matchesUserId && matchesUsername && isValid
                                } ?: false

                        when {
                            isDirectPost -> {
                                Log.d(TAG, "✓ Found direct post by @$username")
                                post
                            }
                            isRepostOfUserContent -> {
                                Log.d(TAG, "✓ Found repost of @$username content")
                                post
                            }
                            else -> null
                        }
                    }

                    Log.d(TAG, "Page $currentPage: Found ${userPosts.size} posts for @$username")

                    withContext(Dispatchers.Main) {
                        if (userPosts.isNotEmpty()) {
                            val validatedPosts = userPosts.mapNotNull { validateAndFixPost(it) }

                            if (validatedPosts.isNotEmpty()) {
                                allUserPosts.addAll(validatedPosts)

                                if (currentPage == 1) {
                                    feedAdapter.clear()
                                }
                                feedAdapter.submitItems(validatedPosts)
                                feedAdapter.initializeCommentCounts(validatedPosts)

                                totalUserPosts = allUserPosts.size

                                Log.d(TAG, "✅ Added ${validatedPosts.size} posts. Total: $totalUserPosts")

                                recyclerView.visibility = View.VISIBLE
                                emptyStateText.visibility = View.GONE
                            }
                        }

                        isLoading = false
                    }

                    val hasNextPage = responseBody?.data?.data?.hasNextPage ?: false
                    val totalPages = responseBody?.data?.data?.totalPages ?: currentPage

                    Log.d(TAG, "Pagination: hasNext=$hasNextPage, page=$currentPage/$totalPages")

                    // 🚀 SPEED: Load initial batch (3-5 pages) automatically, then stop
                    val shouldContinueInitialLoad = !hasLoadedOnce &&
                            currentPage < 5 &&
                            currentPage < MAX_PAGES &&
                            allUserPosts.size < MAX_ITEMS

                    if (hasNextPage && currentPage < totalPages && shouldContinueInitialLoad) {
                        currentPage++
                        Log.d(TAG, "🔄 Auto-loading next page: $currentPage")
                        withContext(Dispatchers.Main) {
                            isLoading = false
                            loadAllPostsOptimized()
                        }
                    } else {
                        // Finished initial load or hit limits
                        hasMoreData = hasNextPage && currentPage < totalPages &&
                                currentPage < MAX_PAGES &&
                                allUserPosts.size < MAX_ITEMS
                        hasLoadedOnce = true

                        if (hasNextPage && currentPage < totalPages) {
                            currentPage++ // Prepare for scroll-triggered load
                        }

                        withContext(Dispatchers.Main) {
                            progressBar.visibility = View.GONE

                            // 🚀 SPEED: Update cache
                            cachedPosts = allUserPosts.toList()
                            cacheTimestamp = System.currentTimeMillis()
                            userId?.let { id ->
                                staticCache[id] = Pair(cachedPosts!!, cacheTimestamp)
                            }

                            if (allUserPosts.isEmpty()) {
                                recyclerView.visibility = View.GONE
                                emptyStateText.apply {
                                    visibility = View.VISIBLE
                                    text = "@$username hasn't posted anything yet"
                                }
                            }

                            Log.d(TAG, "✅ FINISHED - Loaded $currentPage pages, ${allUserPosts.size} posts")
                            Log.d(TAG, "Cache updated - ${cachedPosts?.size} items stored")
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
                val allRefreshedPosts = mutableListOf<Post>()
                var page = 1

                // Load up to 3 pages in background
                while (page <= 3 && page <= MAX_PAGES) {
                    val response = apiService.getAllFeed(page = page.toString())

                    if (response.isSuccessful) {
                        val posts = response.body()?.data?.data?.posts ?: emptyList()

                        val userPosts = posts.mapNotNull { post ->
                            val isDirectPost = post.author?.let { author ->
                                val matchesUserId = author.owner == userId
                                val apiUsername = author.account?.username?.trim()?.lowercase()
                                val matchesUsername = apiUsername == cleanUsername
                                val isValid = author.account != null
                                matchesUserId && matchesUsername && isValid
                            } ?: false

                            val isRepostOfUserContent = post.isReposted == true &&
                                    !post.originalPost.isNullOrEmpty() &&
                                    post.originalPost[0].author?.let { originalAuthor ->
                                        val matchesUserId = originalAuthor.owner == userId
                                        val apiUsername = originalAuthor.account?.username?.trim()?.lowercase()
                                        val matchesUsername = apiUsername == cleanUsername
                                        val isValid = originalAuthor.account != null
                                        matchesUserId && matchesUsername && isValid
                                    } ?: false

                            if (isDirectPost || isRepostOfUserContent) {
                                validateAndFixPost(post)
                            } else null
                        }

                        allRefreshedPosts.addAll(userPosts)

                        val hasNextPage = response.body()?.data?.data?.hasNextPage ?: false
                        if (!hasNextPage) break
                    } else {
                        break
                    }
                    page++
                }

                withContext(Dispatchers.Main) {
                    if (allRefreshedPosts.isNotEmpty()) {
                        // Update cache silently
                        cachedPosts = allRefreshedPosts
                        cacheTimestamp = System.currentTimeMillis()
                        userId?.let { id ->
                            staticCache[id] = Pair(cachedPosts!!, cacheTimestamp)
                        }

                        // Update UI if data changed significantly
                        if (allRefreshedPosts.size != allUserPosts.size) {
                            allUserPosts.clear()
                            allUserPosts.addAll(allRefreshedPosts)
                            feedAdapter.clear()
                            feedAdapter.submitItems(allUserPosts)
                            feedAdapter.initializeCommentCounts(allUserPosts)
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
                    Log.w(TAG, "Skipping repost ${post._id}: null original author")
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
                    Log.w(TAG, "Skipping post ${post._id}: null author/account")
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
            Log.e(TAG, "Error validating post ${post._id}: ${e.message}", e)
            return null
        }
    }

    private fun showContent() {
        recyclerView.visibility = View.VISIBLE
        emptyStateText.visibility = View.GONE
    }

    private fun showEmptyState() {
        recyclerView.visibility = View.GONE
        emptyStateText.apply {
            visibility = View.VISIBLE
            text = "@$username hasn't posted anything yet"
        }
    }

    private fun handleError(message: String) {
        isLoading = false
        hasMoreData = false
        hasLoadedOnce = true
        progressBar.visibility = View.GONE

        if (allUserPosts.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyStateText.apply {
                visibility = View.VISIBLE
                text = message
            }
        } else {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Don't clear cache on view destroy - keep it for fast reload
    }

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