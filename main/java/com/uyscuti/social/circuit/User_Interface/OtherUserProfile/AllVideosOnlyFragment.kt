package com.uyscuti.social.circuit.User_Interface.OtherUserProfile

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ProgressBar
import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import com.uyscuti.social.circuit.R
import kotlinx.coroutines.launch
import android.content.Context
import android.graphics.Color
import android.graphics.Outline
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.uyscuti.social.network.api.response.posts.Post
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.api.retrofit.interfaces.IFlashapi
import com.uyscuti.social.network.utils.LocalStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "All Videos Only Fragment"

class AllVideosOnlyFragment : Fragment() {

    private lateinit var apiService: IFlashapi
    private lateinit var gridLayout: GridLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyTextView: TextView
    private lateinit var adapter: OtherUsersVideosOnlyAdapter

    private var userId: String? = null
    private var username: String? = null
    private var cleanUsername: String? = null

    private var currentPage = 1
    private var isLoading = false
    private var hasMoreData = true
    private var isInitialLoad = true

    companion object {
        private const val ARG_USER_ID = "userId"
        private const val ARG_USERNAME = "username"

        // Static cache that survives fragment recreation
        private val videoCache = mutableMapOf<String, CachedVideosData>()

        // Pagination limits
        private const val MAX_PAGES = 10
        private const val MAX_ITEMS = 50
        private const val INITIAL_LOAD_PAGES = 5

        // Cache duration: 5 minutes
        private const val CACHE_DURATION_MS = 5 * 60 * 1000L

        fun newInstance(userId: String, username: String): AllVideosOnlyFragment {
            return AllVideosOnlyFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_USER_ID, userId)
                    putString(ARG_USERNAME, username)
                }
            }
        }
    }

    data class CachedVideosData(
        val videos: List<Post>,
        val timestamp: Long,
        val hasMoreData: Boolean,
        val lastPage: Int
    ) {
        fun isValid(): Boolean {
            return (System.currentTimeMillis() - timestamp) < CACHE_DURATION_MS
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString(ARG_USER_ID)
            username = it.getString(ARG_USERNAME)
            cleanUsername = username?.trim()?.lowercase()
        }

        Log.d(TAG, "Fragment initialized - User Id: $userId, username: $username")

        val localStorage = LocalStorage(requireContext())
        val retrofitInstance = RetrofitInstance(localStorage, requireContext())
        apiService = retrofitInstance.apiService

        // Load from cache IMMEDIATELY in onCreate
        val cacheKey = getCacheKey()
        videoCache[cacheKey]?.let { cached ->
            if (cached.isValid()) {
                Log.d(TAG, "✓ Cache HIT in onCreate! Loading ${cached.videos.size} videos instantly")
                // Cache will be used in onViewCreated
            } else {
                Log.d(TAG, "Cache expired, will fetch fresh data")
                videoCache.remove(cacheKey)
            }
        }
    }

    private fun getCacheKey(): String = "videos_${userId}_${cleanUsername}"

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.all_videos_only_fragment, container, false)

        gridLayout = view.findViewById(R.id.postsGridLayout)
        progressBar = view.findViewById(R.id.progressBar)
        emptyTextView = view.findViewById(R.id.emptyTextView)

        adapter = OtherUsersVideosOnlyAdapter(gridLayout, requireContext())

        adapter.setOnPostClickListener { post ->
            Log.d(TAG, "Video clicked: ${post._id}")
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cacheKey = getCacheKey()
        val cached = videoCache[cacheKey]

        if (cached != null && cached.isValid()) {
            // INSTANT LOAD from cache
            Log.d(TAG, "🚀 INSTANT LOAD - Displaying ${cached.videos.size} cached videos")

            displayCachedData(cached)

            // Background refresh if needed
            if (cached.videos.size < MAX_ITEMS && cached.hasMoreData) {
                Log.d(TAG, "Background refresh starting...")
                backgroundRefresh()
            }
        } else {
            // Fresh load
            Log.d(TAG, "Fresh load starting...")
            currentPage = 1
            isLoading = false
            hasMoreData = true
            isInitialLoad = true

            loadVideos()
        }
    }

    private fun displayCachedData(cached: CachedVideosData) {
        progressBar.visibility = View.GONE

        if (cached.videos.isEmpty()) {
            gridLayout.visibility = View.GONE
            emptyTextView.visibility = View.VISIBLE
            emptyTextView.text = "@$username hasn't posted any videos yet"
        } else {
            gridLayout.visibility = View.VISIBLE
            emptyTextView.visibility = View.GONE
            adapter.submitList(cached.videos)
        }

        // Restore state
        currentPage = cached.lastPage
        hasMoreData = cached.hasMoreData
        isInitialLoad = false
    }

    private fun backgroundRefresh() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val cacheKey = getCacheKey()
                val cached = videoCache[cacheKey] ?: return@launch

                val tempVideos = cached.videos.toMutableList()
                var page = cached.lastPage + 1

                while (page <= MAX_PAGES && tempVideos.size < MAX_ITEMS) {
                    val response = apiService.getAllFeed(page = page.toString())

                    if (response.isSuccessful) {
                        val posts = response.body()?.data?.data?.posts ?: emptyList()
                        val userVideos = filterUserVideos(posts)

                        if (userVideos.isNotEmpty()) {
                            val validated = userVideos.mapNotNull { validateVideoPost(it) }
                            tempVideos.addAll(validated)

                            // Update cache silently
                            videoCache[cacheKey] = CachedVideosData(
                                videos = tempVideos.take(MAX_ITEMS),
                                timestamp = System.currentTimeMillis(),
                                hasMoreData = tempVideos.size < MAX_ITEMS,
                                lastPage = page
                            )
                        }

                        val hasNextPage = response.body()?.data?.data?.hasNextPage ?: false
                        if (!hasNextPage) break

                        page++
                    } else {
                        break
                    }
                }

                Log.d(TAG, "✓ Background refresh complete: ${tempVideos.size} videos")
            } catch (e: Exception) {
                Log.e(TAG, "Background refresh error: ${e.message}")
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun loadVideos() {
        if (isLoading || !hasMoreData) {
            Log.d(TAG, "Skipping load - isLoading: $isLoading, hasMoreData: $hasMoreData")
            return
        }

        // Enforce pagination limits
        if (currentPage > MAX_PAGES) {
            Log.d(TAG, "⚠️ Reached MAX_PAGES limit ($MAX_PAGES)")
            hasMoreData = false
            isInitialLoad = false
            finalizeLoading()
            return
        }

        isLoading = true
        Log.d(TAG, "Loading videos page $currentPage for userId: $userId")

        if (currentPage == 1) {

            emptyTextView.visibility = View.GONE
            gridLayout.visibility = View.GONE
        }

        val allUserVideos = mutableListOf<Post>()
        val cacheKey = getCacheKey()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getAllFeed(page = currentPage.toString())

                Log.d(TAG, "Response successful: ${response.isSuccessful}, Code: ${response.code()}")

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val posts = responseBody?.data?.data?.posts ?: emptyList()

                    Log.d(TAG, "Page $currentPage: Received ${posts.size} total posts")

                    val userVideoPosts = filterUserVideos(posts)

                    Log.d(TAG, "Page $currentPage: Found ${userVideoPosts.size} video posts for @$username")

                    withContext(Dispatchers.Main) {
                        if (userVideoPosts.isNotEmpty()) {
                            val validatedPosts = userVideoPosts.mapNotNull { validateVideoPost(it) }

                            if (validatedPosts.isNotEmpty()) {
                                // Get current cache or create new
                                val currentCache = videoCache[cacheKey]
                                val existingVideos = currentCache?.videos?.toMutableList() ?: mutableListOf()
                                existingVideos.addAll(validatedPosts)

                                // Limit total items
                                val limitedVideos = existingVideos.take(MAX_ITEMS)
                                allUserVideos.addAll(limitedVideos)

                                gridLayout.visibility = View.VISIBLE
                                emptyTextView.visibility = View.GONE

                                adapter.submitList(allUserVideos.toList())

                                Log.d(TAG, "✓ Added ${validatedPosts.size} videos. Total: ${allUserVideos.size}")

                                // Check item limit
                                if (allUserVideos.size >= MAX_ITEMS) {
                                    Log.d(TAG, "⚠️ Reached MAX_ITEMS limit ($MAX_ITEMS)")
                                    hasMoreData = false
                                    isInitialLoad = false

                                    // Update cache
                                    videoCache[cacheKey] = CachedVideosData(
                                        videos = allUserVideos,
                                        timestamp = System.currentTimeMillis(),
                                        hasMoreData = false,
                                        lastPage = currentPage
                                    )

                                    finalizeLoading()
                                    return@withContext
                                }
                            }
                        }

                        isLoading = false
                    }

                    val hasNextPage = responseBody?.data?.data?.hasNextPage ?: false
                    val totalPages = responseBody?.data?.data?.totalPages ?: currentPage

                    Log.d(TAG, "Pagination: hasNextPage=$hasNextPage, currentPage=$currentPage, totalPages=$totalPages")

                    if (hasNextPage && currentPage < totalPages && currentPage < MAX_PAGES && allUserVideos.size < MAX_ITEMS) {
                        currentPage++

                        // Smart initial load: only 3-5 pages
                        if (isInitialLoad && currentPage <= INITIAL_LOAD_PAGES) {
                            Log.d(TAG, "Initial load - auto-loading page: $currentPage")
                            withContext(Dispatchers.Main) {
                                isLoading = false
                            }
                            loadVideos()
                        } else {
                            isInitialLoad = false

                            withContext(Dispatchers.Main) {
                                // Update cache
                                videoCache[cacheKey] = CachedVideosData(
                                    videos = allUserVideos,
                                    timestamp = System.currentTimeMillis(),
                                    hasMoreData = true,
                                    lastPage = currentPage - 1
                                )

                                finalizeLoading()
                            }
                        }
                    } else {
                        hasMoreData = false
                        isInitialLoad = false

                        withContext(Dispatchers.Main) {
                            // Update cache
                            videoCache[cacheKey] = CachedVideosData(
                                videos = allUserVideos,
                                timestamp = System.currentTimeMillis(),
                                hasMoreData = false,
                                lastPage = currentPage
                            )

                            finalizeLoading()
                        }
                    }
                } else {
                    Log.e(TAG, "API error: ${response.code()}")
                    withContext(Dispatchers.Main) {
                        handleError("Failed to load videos: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    handleError("Error loading videos: ${e.message}")
                }
            }
        }
    }

    private fun filterUserVideos(posts: List<Post>): List<Post> {
        return posts.mapNotNull { post ->
            // Check if post belongs to target user
            val isUserPost = post.author?.let { author ->
                val matchesUserId = author.owner == userId
                val apiUsername = author.account?.username?.trim()?.lowercase()
                val matchesUsername = apiUsername == cleanUsername
                val isValid = author.account != null

                matchesUserId && matchesUsername && isValid
            } ?: false

            val isRepostOfUserVideo = post.isReposted == true &&
                    !post.originalPost.isNullOrEmpty() &&
                    post.originalPost[0].author?.let { originalAuthor ->
                        val matchesUserId = originalAuthor.owner == userId
                        val apiUsername = originalAuthor.account?.username?.trim()?.lowercase()
                        val matchesUsername = apiUsername == cleanUsername
                        val isValid = originalAuthor.account != null

                        matchesUserId && matchesUsername && isValid
                    } ?: false

            // Check if post contains video
            val hasVideo = if (post.isReposted == true && !post.originalPost.isNullOrEmpty()) {
                val original = post.originalPost[0]
                original.fileTypes?.any { it.fileType?.startsWith("video") == true } == true ||
                        original.contentType == "videos"
            } else {
                post.fileTypes?.any { it.fileType?.startsWith("video") == true } == true ||
                        post.contentType == "videos"
            }

            when {
                isUserPost && hasVideo -> {
                    Log.d(TAG, "✓ Found video post by @$username on page $currentPage")
                    post
                }
                isRepostOfUserVideo && hasVideo -> {
                    Log.d(TAG, "✓ Found repost of @$username video on page $currentPage")
                    post
                }
                else -> null
            }
        }
    }

    private fun finalizeLoading() {
        progressBar.visibility = View.GONE
        isLoading = false

        val cacheKey = getCacheKey()
        val cached = videoCache[cacheKey]
        val totalVideos = cached?.videos?.size ?: 0

        if (totalVideos == 0) {
            gridLayout.visibility = View.GONE
            emptyTextView.visibility = View.VISIBLE
            emptyTextView.text = "@$username hasn't posted any videos yet"
        } else {
            gridLayout.visibility = View.VISIBLE
            emptyTextView.visibility = View.GONE
        }

        Log.d(TAG, "FINISHED - Searched $currentPage pages, found $totalVideos videos for @$username")
    }

    private fun validateVideoPost(post: Post): Post? {
        try {
            // Validate video exists
            if (post.isReposted == true && !post.originalPost.isNullOrEmpty()) {
                val originalPost = post.originalPost[0]

                if (originalPost.author?.account == null) {
                    Log.w(TAG, "Skipping repost ${post._id}: null original author")
                    return null
                }

                val hasVideo = originalPost.fileTypes?.any {
                    it.fileType?.startsWith("video") == true
                } == true

                if (!hasVideo) {
                    Log.w(TAG, "Skipping repost ${post._id}: no video content")
                    return null
                }

                post.comments = originalPost.commentCount ?: 0
                post.likes = originalPost.likeCount ?: 0
                post.contentType = "videos"
            } else {
                if (post.author == null || post.author.account == null) {
                    Log.w(TAG, "Skipping post ${post._id}: null author/account")
                    return null
                }

                val hasVideo = post.fileTypes?.any {
                    it.fileType?.startsWith("video") == true
                } == true

                if (!hasVideo) {
                    Log.w(TAG, "Skipping post ${post._id}: no video content")
                    return null
                }

                if (post.comments == null) post.comments = 0
                if (post.likes == null) post.likes = 0
                post.contentType = "videos"
            }

            return post
        } catch (e: Exception) {
            Log.e(TAG, "Error validating video post ${post._id}: ${e.message}", e)
            return null
        }
    }

    private fun handleError(message: String) {
        isLoading = false
        hasMoreData = false
        isInitialLoad = false
        progressBar.visibility = View.GONE

        val cacheKey = getCacheKey()
        val cached = videoCache[cacheKey]
        val totalVideos = cached?.videos?.size ?: 0

        if (totalVideos == 0) {
            gridLayout.visibility = View.GONE
            emptyTextView.apply {
                visibility = View.VISIBLE
                text = message
            }
        } else {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Don't clear cache - it's static and should persist
    }

    class OtherUsersVideosOnlyAdapter(private val parentView: ViewGroup, private val context: Context) {

        private var onPostClickListener: ((Post) -> Unit)? = null
        private var posts: List<Post> = emptyList()

        private val scrollView: ScrollView = ScrollView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        private val gridLayout: GridLayout = GridLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            columnCount = 3
            setPadding(0, 0, 0, 0)
        }

        init {
            scrollView.addView(gridLayout)
            parentView.addView(scrollView)
        }

        fun setOnPostClickListener(listener: (Post) -> Unit) {
            onPostClickListener = listener
        }

        private fun Int.dpToPx(context: Context): Int {
            return (this * context.resources.displayMetrics.density).toInt()
        }

        fun submitList(newPosts: List<Post>) {
            posts = newPosts
            populateGrid()
        }

        private fun populateGrid() {
            gridLayout.removeAllViews()

            val displayMetrics = context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val itemSize = screenWidth / 3

            posts.forEachIndexed { index, post ->
                val container = createPostContainer(itemSize, post, index)
                gridLayout.addView(container)
            }
        }

        private fun createPostContainer(size: Int, post: Post, index: Int): FrameLayout {
            val container = FrameLayout(context).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = size
                    height = size
                    setMargins(0, 0, 0, 0)
                }
            }

            val imageView = ImageView(context).apply {
                id = View.generateViewId()
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.CENTER_CROP
                clipToOutline = true
                outlineProvider = object : ViewOutlineProvider() {
                    override fun getOutline(view: View, outline: Outline) {
                        outline.setRect(0, 0, view.width, view.height)
                    }
                }
            }
            container.addView(imageView)

            // Find the first video file
            val videoFileIndex = post.fileTypes?.indexOfFirst {
                it.fileType?.startsWith("video") == true
            } ?: 0

            val videoFile = post.files?.getOrNull(videoFileIndex)
            val videoFileId = post.fileIds?.getOrNull(videoFileIndex)
            val videoUrl = videoFile?.url ?: ""

            // Load video thumbnail
            loadVideoThumbnail(imageView, videoUrl)

            // Add video overlay
            addVideoOverlay(container, post, videoFileId)

            container.setOnClickListener {
                onPostClickListener?.invoke(post)
            }

            return container
        }

        private fun addVideoOverlay(container: FrameLayout, post: Post, fileId: String?) {
            // White play button
            val playButton = ImageView(context).apply {
                setImageResource(R.drawable.play_button_filled)
                setColorFilter(Color.WHITE)
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER
                }
            }
            container.addView(playButton)

            // Duration text - bottom left
            val durationItem = post.duration?.find { it.fileId == fileId }
            if (durationItem?.duration != null) {
                val durationText = TextView(context).apply {
                    text = durationItem.duration
                    setTextColor(Color.WHITE)
                    textSize = 10f
                    setShadowLayer(4f, 0f, 0f, Color.BLACK)
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.BOTTOM or Gravity.START
                        marginStart = 6.dpToPx(context)
                        bottomMargin = 6.dpToPx(context)
                    }
                }
                container.addView(durationText)
            }
        }

        private fun loadVideoThumbnail(imageView: ImageView, url: String) {
            Glide.with(context)
                .asBitmap()
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .placeholder(R.drawable.imageplaceholder)
                .error(R.drawable.imageplaceholder)
                .into(imageView)

            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        }
    }
}