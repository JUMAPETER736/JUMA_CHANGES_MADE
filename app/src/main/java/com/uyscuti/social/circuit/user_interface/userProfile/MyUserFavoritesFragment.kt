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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.collections.isNotEmpty


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString(ARG_USER_ID)
            username = it.getString(ARG_USERNAME)
            cleanUsername = username?.trim()?.lowercase()
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
            showContent()
        }
    }

    private fun setupRecyclerView() {
        // ✅ Use activity's fragment manager like MyUserFeedFragment
        val parentActivity = requireActivity()

        feedAdapter = FeedAdapter(
            requireContext(),
            retrofitInstance,
            this,
            fragmentManager = parentActivity.supportFragmentManager  // ✅ Use activity's fragment manager
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
                favoritesCache[userId!!] = allUserFavorites.toMutableList()
                cacheTimestamp[userId!!] = System.currentTimeMillis()
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

    // OnFeedClickListener implementations - These are just stubs, FeedPostViewHolder handles everything
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

    override fun followButtonClicked(followUnFollowEntity: FollowUnFollowEntity, followButton: AppCompatButton) {
        Log.d(TAG, "Follow clicked for user ${followUnFollowEntity.userId} - handled by FeedPostViewHolder")
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