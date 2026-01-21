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
class MyUserFavoritesFragment : Fragment(), OnFeedClickListener {

    companion object {
        private const val TAG = "MyUserFavoritesFragment"
        private const val ARG_USER_ID = "userId"
        private const val ARG_USERNAME = "username"

        private val favoritesCache = mutableMapOf<String, MutableList<Post>>()
        private val cacheTimestamp = mutableMapOf<String, Long>()
        private const val CACHE_VALIDITY_MS = 5 * 60 * 1000L

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

    private val allUserFavorites = mutableListOf<Post>()
    private var isDataLoaded = false

    // -------------------- LIFECYCLE --------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString(ARG_USER_ID)
            username = it.getString(ARG_USERNAME)
        }

        // Get logged in user ID from LocalStorage if not provided
        if (userId == null) {
            userId = LocalStorage.getInstance(requireContext()).getUserId()
        }

        Log.d(TAG, "onCreate: userId = $userId")
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
            val cached = favoritesCache[userId] ?: emptyList()
            allUserFavorites.clear()
            allUserFavorites.addAll(cached)
            submitToAdapter(allUserFavorites)
            if (cached.isNotEmpty()) {
                showContent()
            } else {
                showEmptyState()
            }
        } else {
            loadBookmarkedPostsOptimized()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to this fragment if cache is invalid
        if (!isCacheValid()) {
            refreshBookmarks()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerView.adapter = null
        _binding = null
    }

    private fun refreshBookmarks() {
        Log.d(TAG, "Refreshing bookmarks")
        isDataLoaded = false
        clearCache(userId ?: "")
        allUserFavorites.clear()
        loadBookmarkedPostsOptimized()
    }

    // -------------------- RECYCLER --------------------

    private fun setupRecyclerView() {
        feedAdapter = FeedAdapter(
            requireContext(),
            retrofitInstance,
            this,
            fragmentManager = requireActivity().supportFragmentManager
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = feedAdapter
            setHasFixedSize(true)
            setItemViewCacheSize(20)
            visibility = View.GONE
        }
    }

    private fun submitToAdapter(posts: List<Post>) {
        if (!isAdded || _binding == null) {
            Log.w(TAG, "Fragment not attached, skipping adapter submission")
            return
        }

        Log.d(TAG, "Submitting ${posts.size} posts to adapter")

        try {
            feedAdapter.submitItems(posts)
            feedAdapter.initializeCommentCounts(posts)

            // Notify adapter that data has changed
            feedAdapter.notifyDataSetChanged()

            Log.d(TAG, "Successfully submitted posts to adapter")
        } catch (e: Exception) {
            Log.e(TAG, "Error submitting to adapter", e)
            e.printStackTrace()
        }
    }

    // -------------------- CACHE --------------------

    private fun isCacheValid(): Boolean {
        val timestamp = cacheTimestamp[userId] ?: return false
        return System.currentTimeMillis() - timestamp < CACHE_VALIDITY_MS
    }

    // -------------------- NETWORK --------------------

    private fun loadBookmarkedPostsOptimized() {
        if (isDataLoaded) return

        isDataLoaded = true
        showLoading()
        allUserFavorites.clear()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching bookmarked posts for user: $userId")
                val response = retrofitInstance.apiService.getFavoriteFeed(page = "1")

                if (!response.isSuccessful) {
                    Log.e(TAG, "API call failed with code: ${response.code()}")
                    withContext(Dispatchers.Main) {
                        showEmptyState()
                        isDataLoaded = false
                    }
                    return@launch
                }

                val responseBody = response.body()
                Log.d(TAG, "Response body: ${responseBody?.message}")

                // Get posts from the bookmarkedPosts array
                val bookmarkedPosts = responseBody?.data?.bookmarkedPosts.orEmpty()
                Log.d(TAG, "Received ${bookmarkedPosts.size} bookmarked posts from server")

                // Filter posts to only show the ones bookmarked by the current logged-in user
                // Since bookmarkedBy field exists in the bookmarked posts response
                val myBookmarkedPosts = bookmarkedPosts
                    .filter { post ->
                        val isMyBookmark = post.bookmarkedBy == userId
                        Log.d(TAG, "Post ${post._id}: bookmarkedBy=${post.bookmarkedBy}, userId=$userId, isMyBookmark=$isMyBookmark")
                        isMyBookmark
                    }
                    .mapNotNull { post ->
                        val validatedPost = validateAndFixPost(post)
                        if (validatedPost == null) {
                            Log.w(TAG, "Post ${post._id} failed validation")
                        }
                        validatedPost
                    }

                Log.d(TAG, "Filtered to ${myBookmarkedPosts.size} posts for user $userId")

                // Log details of filtered posts
                myBookmarkedPosts.forEachIndexed { index, post ->
                    Log.d(TAG, "Filtered Post $index:")
                    Log.d(TAG, "  ID: ${post._id}")
                    Log.d(TAG, "  Content: ${post.content}")
                    Log.d(TAG, "  Files: ${post.files?.size ?: 0}")
                    Log.d(TAG, "  Author: ${post.author?.account?.username}")
                    Log.d(TAG, "  Avatar URL: ${post.author?.account?.avatar?.url}")
                    Log.d(TAG, "  isBookmarked: ${post.isBookmarked}")
                    post.files?.forEachIndexed { fileIndex, file ->
                        Log.d(TAG, "    File $fileIndex: ${file.url}")
                    }
                }

                withContext(Dispatchers.Main) {
                    if (myBookmarkedPosts.isNotEmpty()) {
                        allUserFavorites.addAll(myBookmarkedPosts)
                        Log.d(TAG, "Submitting ${allUserFavorites.size} posts to adapter")
                        submitToAdapter(allUserFavorites)
                        showContent()

                        // Update cache
                        userId?.let { uid ->
                            favoritesCache[uid] = allUserFavorites.toMutableList()
                            cacheTimestamp[uid] = System.currentTimeMillis()
                            Log.d(TAG, "Cache updated for user $uid")
                        }
                    } else {
                        Log.d(TAG, "No bookmarked posts found for user $userId, showing empty state")
                        showEmptyState()
                    }
                    isDataLoaded = false
                }

            } catch (e: Exception) {
                Log.e(TAG, "Load error: ${e.message}", e)
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    showEmptyState()
                    isDataLoaded = false
                }
            }
        }
    }

    // -------------------- VALIDATION --------------------

    private fun validateAndFixPost(post: Post): Post? {
        return try {
            Log.d(TAG, "Validating post ${post._id}")

            // Ensure the post is marked as bookmarked
            post.isBookmarked = true

            // Initialize counts if null
            post.comments = post.comments ?: 0
            post.likes = post.likes ?: 0
            post.bookmarkCount = post.bookmarkCount ?: 0
            post.repostCount = post.repostCount ?: 0
            post.shareCount = post.shareCount ?: 0

            // Validate author exists
            if (post.author?.account == null) {
                Log.w(TAG, "Post ${post._id} has no author, skipping")
                return null
            }

            // Ensure files list is not null
            if (post.files == null) {
                post.files = emptyList()
                Log.d(TAG, "Post ${post._id}: files was null, set to empty list")
            }

            // Ensure fileTypes list is not null
            if (post.fileTypes == null) {
                post.fileTypes = emptyList()
                Log.d(TAG, "Post ${post._id}: fileTypes was null, set to empty list")
            }

            // Ensure fileIds list is not null
            if (post.fileIds == null) {
                post.fileIds = emptyList()
                Log.d(TAG, "Post ${post._id}: fileIds was null, set to empty list")
            }

            // Ensure duration list is not null
            if (post.duration == null) {
                post.duration = emptyList()
            }

            // Ensure tags list is not null
            if (post.tags == null) {
                post.tags = emptyList()
            }

            // Ensure originalPost list is not null
            if (post.originalPost == null) {
                post.originalPost = emptyList()
            }

            // Ensure thumbnail list is not null
            if (post.thumbnail == null) {
                post.thumbnail = emptyList()
            }

            // Ensure numberOfPages list is not null
            if (post.numberOfPages == null) {
                post.numberOfPages = emptyList()
            }

            // Ensure fileNames list is not null
            if (post.fileNames == null) {
                post.fileNames = emptyList()
            }

            // Ensure fileSizes list is not null
            if (post.fileSizes == null) {
                post.fileSizes = emptyList()
            }

            // Log complete post information for debugging
            Log.d(TAG, "Post ${post._id} validated successfully:")
            Log.d(TAG, "  Content: ${post.content}")
            Log.d(TAG, "  ContentType: ${post.contentType}")
            Log.d(TAG, "  Files: ${post.files?.size ?: 0}")
            post.files?.forEachIndexed { index, file ->
                Log.d(TAG, "    File $index: ${file.url}")
            }
            Log.d(TAG, "  FileTypes: ${post.fileTypes?.size ?: 0}")
            Log.d(TAG, "  Author: ${post.author.account.username}")
            Log.d(TAG, "  Author Name: ${post.author.firstName} ${post.author.lastName}")
            Log.d(TAG, "  Avatar URL: ${post.author.account.avatar?.url}")
            Log.d(TAG, "  Comments: ${post.comments}")
            Log.d(TAG, "  Likes: ${post.likes}")
            Log.d(TAG, "  Bookmarks: ${post.bookmarkCount}")
            Log.d(TAG, "  isBookmarked: ${post.isBookmarked}")

            post
        } catch (e: Exception) {
            Log.e(TAG, "Error validating post ${post._id}", e)
            e.printStackTrace()
            null
        }
    }

    // -------------------- UI STATES --------------------

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.emptyView.visibility = View.GONE
    }

    private fun showContent() {
        binding.progressBar.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
        binding.emptyView.visibility = View.GONE
    }

    private fun showEmptyState() {
        binding.progressBar.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        binding.emptyView.visibility = View.VISIBLE
    }

    // -------------------- FEED CALLBACKS --------------------

    override fun feedFavoriteClick(position: Int, data: Post) {
        // When user unbookmarks, remove from list
        if (!data.isBookmarked && position in allUserFavorites.indices) {
            Log.d(TAG, "Removing unbookmarked post at position $position")
            allUserFavorites.removeAt(position)
            submitToAdapter(allUserFavorites)

            // Update cache
            userId?.let {
                favoritesCache[it] = allUserFavorites.toMutableList()
            }

            // Show empty state if no more bookmarks
            if (allUserFavorites.isEmpty()) {
                showEmptyState()
            }
        }
    }

    override fun likeUnLikeFeed(position: Int, data: Post) {}
    override fun feedCommentClicked(position: Int, data: Post) {}
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
