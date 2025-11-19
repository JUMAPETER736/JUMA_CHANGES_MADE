package com.uyscuti.social.circuit.User_Interface.MyUserProfile

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
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Outline
import android.media.MediaMetadataRetriever
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
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.uyscuti.social.network.api.response.posts.Post
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.api.retrofit.interfaces.IFlashapi
import com.uyscuti.social.network.utils.LocalStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

private const val TAG = "My Videos Only Fragment"

class MyUserVideosOnlyFragment : Fragment() {

    companion object {
        private const val ARG_USER_ID = "userId"
        private const val ARG_USERNAME = "username"

        private val videosCache = mutableMapOf<String, MutableList<Post>>()
        private val cacheTimestamp = mutableMapOf<String, Long>()
        private const val CACHE_VALIDITY_MS = 5 * 60 * 1000L
        private const val MAX_PAGES = 10

        fun newInstance(userId: String, username: String): MyUserVideosOnlyFragment {
            return MyUserVideosOnlyFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_USER_ID, userId)
                    putString(ARG_USERNAME, username)
                }
            }
        }

        fun clearCache(username: String) {
            val cleanUsername = username.trim().lowercase()
            videosCache.remove(cleanUsername)
            cacheTimestamp.remove(cleanUsername)
        }
    }

    private lateinit var apiService: IFlashapi
    private lateinit var gridLayout: GridLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyTextView: TextView
    private lateinit var adapter: MyUserVideosOnlyAdapter

    private var userId: String? = null
    private var username: String? = null
    private var cleanUsername: String? = null

    private val allUserVideos = mutableListOf<Post>()
    private var isDataLoaded = false

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
    }

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

        adapter = MyUserVideosOnlyAdapter(gridLayout, requireContext())

        adapter.setOnPostClickListener { post ->
            Log.d(TAG, "Video clicked: ${post._id}")
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isCacheValid()) {
            val cachedData = videosCache[cleanUsername] ?: mutableListOf()
            allUserVideos.clear()
            allUserVideos.addAll(cachedData)
            isDataLoaded = true
            displayCachedData(cachedData)
            Log.d(TAG, "Loaded from cache: ${cachedData.size} videos")
        } else {
            allUserVideos.clear()
            isDataLoaded = false
            loadVideos()
        }
    }

    private fun isCacheValid(): Boolean {
        val cached = videosCache[cleanUsername]
        val timestamp = cacheTimestamp[cleanUsername]

        if (cached == null || timestamp == null) return false

        val currentTime = System.currentTimeMillis()
        return (currentTime - timestamp) < CACHE_VALIDITY_MS
    }

    @SuppressLint("SetTextI18n")
    private fun displayCachedData(cachedVideos: List<Post>) {
        if (cachedVideos.isEmpty()) {
            showEmptyState()
        } else {
            gridLayout.visibility = View.VISIBLE
            emptyTextView.visibility = View.GONE
            adapter.submitList(cachedVideos.toList())
            Log.d(TAG, "Displayed ${cachedVideos.size} cached videos")
        }
    }

    @OptIn(UnstableApi::class)
    private fun loadVideos() {
        if (isDataLoaded) {
            Log.d(TAG, "Data already loaded, skipping")
            return
        }

        isDataLoaded = true
        Log.d(TAG, "Loading videos for userId: $userId")

        progressBar.visibility = View.VISIBLE
        emptyTextView.visibility = View.GONE
        gridLayout.visibility = View.GONE
        allUserVideos.clear()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                var currentPage = 1
                var hasMorePages = true

                while (hasMorePages && currentPage <= MAX_PAGES) {
                    val response = apiService.getAllFeed(page = currentPage.toString())

                    Log.d(TAG, "Response successful: ${response.isSuccessful}, Code: ${response.code()}")

                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        val posts = responseBody?.data?.data?.posts ?: emptyList()

                        Log.d(TAG, "Page $currentPage: Received ${posts.size} total posts")

                        val userVideoPosts = posts.mapNotNull { post ->
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

                        Log.d(TAG, "Page $currentPage: Found ${userVideoPosts.size} video posts for @$username")

                        if (userVideoPosts.isNotEmpty()) {
                            val validatedPosts = userVideoPosts.mapNotNull { validateVideoPost(it) }
                            allUserVideos.addAll(validatedPosts)
                            Log.d(TAG, "✓ Added ${validatedPosts.size} videos. Total: ${allUserVideos.size}")
                        }

                        val hasNextPage = responseBody?.data?.data?.hasNextPage ?: false
                        val totalPages = responseBody?.data?.data?.totalPages ?: currentPage

                        Log.d(TAG, "Pagination: hasNextPage=$hasNextPage, currentPage=$currentPage, totalPages=$totalPages")

                        if (!hasNextPage || currentPage >= totalPages) {
                            hasMorePages = false
                        } else {
                            currentPage++
                        }

                        if (allUserVideos.size >= 50) {
                            hasMorePages = false
                            Log.d(TAG, "Reached 50 videos, stopping early")
                        }
                    } else {
                        Log.e(TAG, "API error: ${response.code()}")
                        hasMorePages = false
                    }
                }

                videosCache[cleanUsername!!] = allUserVideos.toMutableList()
                cacheTimestamp[cleanUsername!!] = System.currentTimeMillis()

                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE

                    if (allUserVideos.isEmpty()) {
                        showEmptyState()
                    } else {
                        gridLayout.visibility = View.VISIBLE
                        emptyTextView.visibility = View.GONE
                        adapter.submitList(allUserVideos.toList())
                    }

                    Log.d(TAG, "FINISHED - Found ${allUserVideos.size} videos for @$username")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Exception: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    handleError("Error loading videos: ${e.message}")
                }
            }
        }
    }

    private fun validateVideoPost(post: Post): Post? {
        try {
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

    @SuppressLint("SetTextI18n")
    private fun showEmptyState() {
        gridLayout.visibility = View.GONE
        emptyTextView.visibility = View.VISIBLE
        emptyTextView.text = "@$username hasn't posted any videos yet"
    }

    private fun handleError(message: String) {
        isDataLoaded = false
        progressBar.visibility = View.GONE

        if (allUserVideos.isEmpty()) {
            showEmptyState()
        } else {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    class MyUserVideosOnlyAdapter(private val parentView: ViewGroup, private val context: Context) {

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
                setBackgroundColor(Color.parseColor("#1a1a1a"))
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

            // Load video thumbnail with optimized settings
            loadVideoThumbnail(imageView, videoUrl, size)

            // Add video overlay
            addVideoOverlay(container, post, videoFileId)

            container.setOnClickListener {
                onPostClickListener?.invoke(post)
            }

            return container
        }

        private fun addVideoOverlay(container: FrameLayout, post: Post, fileId: String?) {
            // Semi-transparent overlay for better visibility
            val overlay = View(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(Color.parseColor("#20000000"))
            }
            container.addView(overlay)

            // White play button
            val playButton = ImageView(context).apply {
                setImageResource(R.drawable.play_button_filled)
                setColorFilter(Color.WHITE)
                layoutParams = FrameLayout.LayoutParams(
                    48.dpToPx(context),
                    48.dpToPx(context)
                ).apply {
                    gravity = Gravity.CENTER
                }
                alpha = 0.9f
            }
            container.addView(playButton)

            // Duration text - bottom left
            val durationItem = post.duration?.find { it.fileId == fileId }
            if (durationItem?.duration != null) {
                val durationText = TextView(context).apply {
                    text = durationItem.duration
                    setTextColor(Color.WHITE)
                    textSize = 11f
                    setShadowLayer(6f, 0f, 0f, Color.BLACK)
                    setPadding(4.dpToPx(context), 2.dpToPx(context), 4.dpToPx(context), 2.dpToPx(context))
                    setBackgroundColor(Color.parseColor("#80000000"))
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.BOTTOM or Gravity.END
                        marginEnd = 4.dpToPx(context)
                        bottomMargin = 4.dpToPx(context)
                    }
                }
                container.addView(durationText)
            }
        }

        private fun loadVideoThumbnail(imageView: ImageView, url: String, thumbnailSize: Int) {
            if (url.isBlank()) {
                imageView.setImageResource(R.drawable.imageplaceholder)
                return
            }

            // Optimized Glide request for faster loading
            val requestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(thumbnailSize, thumbnailSize) // Resize to exact thumbnail size
                .centerCrop()
                .dontAnimate()
                .skipMemoryCache(false)

            Glide.with(context)
                .asBitmap()
                .load(url)
                .apply(requestOptions)
                .thumbnail(0.1f) // Load low-res version first
                .error(R.drawable.imageplaceholder)
                .into(imageView)
        }
    }
}