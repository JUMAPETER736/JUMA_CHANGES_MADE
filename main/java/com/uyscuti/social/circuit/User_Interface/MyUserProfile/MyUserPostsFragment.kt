package com.uyscuti.social.circuit.User_Interface.MyUserProfile

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

private const val TAG = "MyUsersPostsFragment"

class MyUserPostsFragment : Fragment(), OnFeedClickListener {

    companion object {
        private const val ARG_USER_ID = "userId"
        private const val ARG_USERNAME = "username"

        private val postsCache = mutableMapOf<String, MutableList<Post>>()
        private val cacheTimestamp = mutableMapOf<String, Long>()
        private const val CACHE_VALIDITY_MS = 5 * 60 * 1000L
        private const val INITIAL_LOAD_SIZE = 10 // Show first 10 immediately
        private const val MAX_PAGES = 5 // Reduced from 10

        fun newInstance(userId: String, username: String): MyUserPostsFragment {
            return MyUserPostsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_USER_ID, userId)
                    putString(ARG_USERNAME, username)
                }
            }
        }

        fun clearCache(username: String) {
            val cleanUsername = username.trim().lowercase()
            postsCache.remove(cleanUsername)
            cacheTimestamp.remove(cleanUsername)
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

    private val allUserPosts = mutableListOf<Post>()
    private var isDataLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString(ARG_USER_ID)
            username = it.getString(ARG_USERNAME)
            cleanUsername = username?.trim()?.lowercase()
        }

        val localStorage = LocalStorage(requireContext())
        val retrofitInstance = RetrofitInstance(localStorage, requireContext())
        apiService = retrofitInstance.apiService
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

        setupRecyclerView()

        if (isCacheValid()) {
            val cachedData = postsCache[cleanUsername] ?: mutableListOf()
            allUserPosts.clear()
            allUserPosts.addAll(cachedData)
            isDataLoaded = true
            displayCachedData(cachedData)
        } else {
            allUserPosts.clear()
            isDataLoaded = false
            loadAllPostsOptimized()
        }
    }

    private fun isCacheValid(): Boolean {
        val cached = postsCache[cleanUsername]
        val timestamp = cacheTimestamp[cleanUsername]

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
            recyclerView.visibility = View.VISIBLE
            emptyStateText.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {
        feedAdapter = FeedAdapter(requireContext(), this)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = feedAdapter
            visibility = View.VISIBLE
            setHasFixedSize(true) // Performance optimization
            setItemViewCacheSize(20) // Cache more views
        }

        feedAdapter.recyclerView = recyclerView
    }

    @SuppressLint("SetTextI18n")
    private fun loadAllPostsOptimized() {
        if (isDataLoaded) return

        isDataLoaded = true
        progressBar.visibility = View.VISIBLE
        emptyStateText.visibility = View.GONE
        allUserPosts.clear()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Load first page immediately
                val firstPageResponse = apiService.getAllFeed(page = "1")

                if (firstPageResponse.isSuccessful) {
                    val firstPagePosts = firstPageResponse.body()?.data?.data?.posts ?: emptyList()

                    // Filter and get first batch of user posts
                    val firstBatch = firstPagePosts
                        .mapNotNull { filterUserPost(it) }
                        .take(INITIAL_LOAD_SIZE)
                        .mapNotNull { validateAndFixPost(it) }

                    if (firstBatch.isNotEmpty()) {
                        allUserPosts.addAll(firstBatch)

                        // Show first batch immediately
                        withContext(Dispatchers.Main) {
                            progressBar.visibility = View.GONE
                            feedAdapter.submitItems(firstBatch)
                            feedAdapter.initializeCommentCounts(firstBatch)
                            recyclerView.visibility = View.VISIBLE
                            emptyStateText.visibility = View.GONE
                        }
                    }

                    // Load remaining data in background
                    val remainingFirstPage = firstPagePosts
                        .mapNotNull { filterUserPost(it) }
                        .drop(INITIAL_LOAD_SIZE)

                    loadRemainingDataInBackground(remainingFirstPage)
                } else {
                    withContext(Dispatchers.Main) {
                        handleError("Failed to load posts")
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

    private fun filterUserPost(post: Post): Post? {
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

        return when {
            isDirectPost || isRepostOfUserContent -> post
            else -> null
        }
    }

    private suspend fun loadRemainingDataInBackground(remainingFirstPage: List<Post>) {
        try {
            // Process remaining items from first page
            val remainingValidated = remainingFirstPage.mapNotNull { validateAndFixPost(it) }
            if (remainingValidated.isNotEmpty()) {
                allUserPosts.addAll(remainingValidated)
                updateUI()
            }

            // Load additional pages
            var currentPage = 2
            var hasMorePages = true

            while (hasMorePages && currentPage <= MAX_PAGES) {
                val response = apiService.getAllFeed(page = currentPage.toString())

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val posts = responseBody?.data?.data?.posts ?: emptyList()

                    val userPosts = posts
                        .mapNotNull { filterUserPost(it) }
                        .mapNotNull { validateAndFixPost(it) }

                    if (userPosts.isNotEmpty()) {
                        allUserPosts.addAll(userPosts)
                        updateUI()
                    }

                    val hasNextPage = responseBody?.data?.data?.hasNextPage ?: false
                    val totalPages = responseBody?.data?.data?.totalPages ?: currentPage

                    if (!hasNextPage || currentPage >= totalPages || allUserPosts.size >= 50) {
                        hasMorePages = false
                    } else {
                        currentPage++
                    }
                } else {
                    hasMorePages = false
                }
            }

            // Cache final results
            postsCache[cleanUsername!!] = allUserPosts.toMutableList()
            cacheTimestamp[cleanUsername!!] = System.currentTimeMillis()

            Log.d(TAG, "FINISHED - Found ${allUserPosts.size} posts for @$username")

        } catch (e: Exception) {
            Log.e(TAG, "Background loading error: ${e.message}", e)
        }
    }

    private suspend fun updateUI() {
        withContext(Dispatchers.Main) {
            if (allUserPosts.isEmpty()) {
                showEmptyState()
            } else {
                feedAdapter.submitItems(allUserPosts)
                feedAdapter.initializeCommentCounts(allUserPosts)
                recyclerView.visibility = View.VISIBLE
                emptyStateText.visibility = View.GONE
            }
        }
    }

    private fun validateAndFixPost(post: Post): Post? {
        try {
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
                    return null
                }

                post.comments = post.comments ?: 0
                post.likes = post.likes ?: 0
                post.bookmarkCount = post.bookmarkCount ?: 0
                post.repostCount = post.repostCount ?: 0
                post.shareCount = post.shareCount ?: 0

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

    @SuppressLint("SetTextI18n")
    private fun showEmptyState() {
        recyclerView.visibility = View.GONE
        emptyStateText.apply {
            visibility = View.VISIBLE
            text = "@$username hasn't posted anything yet"
        }
    }

    private fun handleError(message: String) {
        isDataLoaded = false
        progressBar.visibility = View.GONE

        if (allUserPosts.isEmpty()) {
            showEmptyState()
        } else {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        feedAdapter.updatePosts(emptyList())
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