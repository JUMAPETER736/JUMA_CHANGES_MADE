package com.uyscuti.sharedmodule.User_Interfaces.OtherUserProfile

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.uyscuti.sharedmodule.R
import com.uyscuti.sharedmodule.adapter.feed.FeedAdapter
import com.uyscuti.sharedmodule.adapter.feed.OnFeedClickListener
import com.uyscuti.social.core.common.data.room.entity.FollowUnFollowEntity
import com.uyscuti.social.network.api.response.posts.OriginalPost
import com.uyscuti.social.network.api.response.posts.Post
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.api.retrofit.interfaces.IFlashapi
import com.uyscuti.social.network.utils.LocalStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "AllOtherUsersPostsFragment"


@AndroidEntryPoint
class AllOtherUsersPostsFragment : Fragment(), OnFeedClickListener {

    companion object {
        private const val ARG_USER_ID = "userId"
        private const val ARG_USERNAME = "username"

        // Static cache shared across fragment instances
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

    @Inject
    lateinit var retrofitInstance: RetrofitInstance
    private lateinit var apiService: IFlashapi
    private lateinit var recyclerView: RecyclerView
    private lateinit var feedAdapter: FeedAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateText: TextView

    private var userId: String? = null
    private var username: String? = null
    private var cleanUsername: String? = null

    private val allUserPosts = mutableListOf<Post>()
    private var cachedPosts: List<Post>? = null
    private var cacheTimestamp: Long = 0L
    private val CACHE_DURATION_MS = 5 * 60 * 1000L // 5 minutes

    private var currentPage = 1
    private var isLoading = false
    private var hasMoreData = true
    private var hasLoadedOnce = false
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

        apiService = retrofitInstance.apiService

        // Load from static cache if available
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
        setupRecyclerView()

        // If cache exists and valid, display instantly
        if (cachedPosts != null && isCacheValid()) {
            displayCachedData()
            if ((System.currentTimeMillis() - cacheTimestamp) > (CACHE_DURATION_MS / 2)) {
                refreshInBackground()
            }
        } else {
            resetAndLoadFresh()
        }
    }

    private fun setupRecyclerView() {
        // Use parentFragmentManager instead of childFragmentManager
        feedAdapter = FeedAdapter(
            requireContext(),
            retrofitInstance,
            this,
            fragmentManager = parentFragmentManager
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = feedAdapter
            setHasFixedSize(true)
            setItemViewCacheSize(20)
            isNestedScrollingEnabled = true

            // RecycledViewPool for better performance
            val viewPool = RecyclerView.RecycledViewPool()
            viewPool.setMaxRecycledViews(0, 15)
            setRecycledViewPool(viewPool)

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                    val layoutManager = rv.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if (!isLoading && hasMoreData && hasLoadedOnce) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5
                            && firstVisibleItemPosition >= 0
                        ) {
                            loadPosts()
                        }
                    }
                }
            })
        }

        // Assign RecyclerView to adapter safely after current transactions
        recyclerView.post {
            feedAdapter.recyclerView = recyclerView
        }
    }


    private fun isCacheValid(): Boolean =
        cachedPosts != null && (System.currentTimeMillis() - cacheTimestamp) < CACHE_DURATION_MS

    private fun displayCachedData() {
        allUserPosts.clear()
        allUserPosts.addAll(cachedPosts!!)
        feedAdapter.clear()
        feedAdapter.submitItems(allUserPosts)
        feedAdapter.initializeCommentCounts(allUserPosts)
        if (allUserPosts.isEmpty()) showEmptyState() else showContent()
        Log.d(TAG, "⚡ Displayed ${allUserPosts.size} cached posts instantly")
    }

    private fun resetAndLoadFresh() {
        allUserPosts.clear()
        currentPage = 1
        isLoading = false
        hasMoreData = true
        loadPosts()
    }

    private fun loadPosts() {
        if (isLoading || !hasMoreData || currentPage > MAX_PAGES || allUserPosts.size >= MAX_ITEMS) {
            hasMoreData = false
            hasLoadedOnce = true
            return
        }

        isLoading = true
        if (currentPage == 1) emptyStateText.visibility = View.GONE

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getAllFeed(page = currentPage.toString())
                if (response.isSuccessful) {
                    val posts = response.body()?.data?.data?.posts ?: emptyList()
                    val userPosts = posts.mapNotNull { post ->
                        if (isPostByUser(post)) validateAndFixPost(post) else null
                    }

                    withContext(Dispatchers.Main) {
                        if (userPosts.isNotEmpty()) {
                            allUserPosts.addAll(userPosts)
                            if (currentPage == 1) feedAdapter.clear()
                            feedAdapter.submitItems(userPosts)
                            feedAdapter.initializeCommentCounts(userPosts)
                        }

                        val hasNextPage = response.body()?.data?.data?.hasNextPage ?: false
                        hasMoreData = hasNextPage && currentPage < MAX_PAGES && allUserPosts.size < MAX_ITEMS
                        hasLoadedOnce = true
                        currentPage++

                        cachedPosts = allUserPosts.toList()
                        cacheTimestamp = System.currentTimeMillis()
                        userId?.let { staticCache[it] = cachedPosts!! to cacheTimestamp }

                        progressBar.visibility = View.GONE
                        if (allUserPosts.isEmpty()) showEmptyState() else showContent()
                    }
                } else {
                    withContext(Dispatchers.Main) { handleError("Failed to load: ${response.code()}") }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { handleError("Error: ${e.message}") }
            } finally {
                isLoading = false
            }
        }
    }

    private fun isPostByUser(post: Post): Boolean {
        val matchesDirect = post.author?.owner == userId &&
                post.author.account?.username?.trim()?.lowercase() == cleanUsername

        val matchesRepost = post.isReposted == true &&
                !post.originalPost.isNullOrEmpty() &&
                post.originalPost[0].author?.owner == userId &&
                post.originalPost[0].author.account?.username?.trim()?.lowercase() == cleanUsername

        return matchesDirect || matchesRepost
    }

    private fun refreshInBackground() {
        if (isLoading) return
        lifecycleScope.launch(Dispatchers.IO) {
            repeat(3) { page ->
                val response = apiService.getAllFeed(page = (page + 1).toString())
                if (response.isSuccessful) {
                    val posts = response.body()?.data?.data?.posts ?: emptyList()
                    val userPosts = posts.mapNotNull { post -> if (isPostByUser(post)) validateAndFixPost(post) else null }
                    if (userPosts.isNotEmpty()) allUserPosts.clear(); allUserPosts.addAll(userPosts)
                }
            }
            withContext(Dispatchers.Main) {
                feedAdapter.clear()
                feedAdapter.submitItems(allUserPosts)
                feedAdapter.initializeCommentCounts(allUserPosts)
            }
        }
    }

    private fun validateAndFixPost(post: Post): Post? {
        // Simplified validation
        if (post.author?.account == null) return null
        post.comments = post.comments ?: 0
        post.likes = post.likes ?: 0
        post.bookmarkCount = post.bookmarkCount ?: 0
        post.repostCount = post.repostCount ?: 0
        post.shareCount = post.shareCount ?: 0
        post.contentType = post.contentType ?: "text"
        return post
    }

    private fun showContent() {
        recyclerView.visibility = View.VISIBLE
        emptyStateText.visibility = View.GONE
    }

    private fun showEmptyState() {
        recyclerView.visibility = View.GONE
        emptyStateText.visibility = View.VISIBLE
        emptyStateText.text = "@$username hasn't posted anything yet"
    }

    private fun handleError(message: String) {
        isLoading = false
        hasMoreData = false
        hasLoadedOnce = true
        progressBar.visibility = View.GONE
        if (allUserPosts.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyStateText.text = message
            emptyStateText.visibility = View.VISIBLE
        } else {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ Handle post click - delegate to parent activity or open as dialog
    @OptIn(UnstableApi::class)
    override fun feedClickedToOriginalPost(position: Int, originalPostId: String) {
        // This will be called from FeedAdapter when post is clicked
        // Since we're in AllOtherUsersPostsFragment inside OtherUserProfileAccount,
        // we need to let the activity handle the navigation

        try {
            val post = allUserPosts.getOrNull(position) ?: return
            Log.d(TAG, "Post clicked at position $position, delegating to activity")

            // Call a method on the parent activity to handle navigation
            (activity as? OtherUserProfileAccount)?.openPostDetail(post, position)

        } catch (e: Exception) {
            Log.e(TAG, "Error handling post click: ${e.message}", e)
            Toast.makeText(requireContext(), "Unable to open post", Toast.LENGTH_SHORT).show()
        }
    }

    // OnFeedClickListener stubs
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
    override fun onImageClick() {}
}