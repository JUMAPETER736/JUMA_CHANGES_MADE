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
import androidx.fragment.app.activityViewModels
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

    @Inject
    lateinit var retrofitInstance: RetrofitInstance

    private lateinit var feedAdapter: FeedAdapter

    private val allFavorites = mutableListOf<Post>()
    private var isDataLoaded = false

    companion object {
        private const val TAG = "AllOtherUserFavoritesFragment"
        private const val ARG_USER_ID = "userId"
        private const val ARG_USERNAME = "username"

        internal val favoritesCache = mutableMapOf<String, MutableList<Post>>()
        internal val cacheTimestamp = mutableMapOf<String, Long>()
        private const val CACHE_VALIDITY_MS = 5 * 60 * 1000L

        fun newInstance(userId: String, username: String): AllOtherUsersFavoritesFragment {
            return AllOtherUsersFavoritesFragment().apply {
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

        internal fun emptyRepostedUser(): RepostedUser {
            return RepostedUser(
                _id = "",
                avatar = Avatar(_id = "", url = "", localPath = ""),
                bio = "",
                coverImage = CoverImage(_id = "", localPath = "", url = ""),
                createdAt = "",
                email = "",
                firstName = "",
                lastName = "",
                owner = "",
                updatedAt = "",
                username = ""
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            otherUserId = it.getString(ARG_USER_ID)
            username = it.getString(ARG_USERNAME)
        }

        Log.d(TAG, "onCreate: userId = $otherUserId, username = @$username")
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

        // Show cached data IMMEDIATELY without checking validity
        val cached = favoritesCache[otherUserId]
        if (!cached.isNullOrEmpty()) {
            allFavorites.clear()
            allFavorites.addAll(cached)
            submitToAdapter(allFavorites)
            showContent()

            // Load fresh data in background only if cache is old
            if (!isCacheValid()) {
                loadBookmarkedFeedPosts()
            }
        } else {
            // No cache - load data
            loadBookmarkedFeedPosts()
        }
    }

    override fun onResume() {
        super.onResume()
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
        Log.d(TAG, "Refreshing bookmarks for @$username")
        isDataLoaded = false
        loadBookmarkedFeedPosts()
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
            setHasFixedSize(true)
            setItemViewCacheSize(30)
            recycledViewPool.setMaxRecycledViews(0, 30)
            visibility = View.GONE
        }
    }

    private fun submitToAdapter(posts: List<Post>) {
        if (!isAdded || _binding == null) return

        try {
            feedAdapter.submitItems(posts)
            feedAdapter.initializeCommentCounts(posts)
            if (feedAdapter.itemCount != posts.size) {
                feedAdapter.notifyDataSetChanged()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error submitting to adapter", e)
        }
    }

    private fun isCacheValid(): Boolean {
        val timestamp = cacheTimestamp[otherUserId] ?: return false
        return System.currentTimeMillis() - timestamp < CACHE_VALIDITY_MS
    }

    private fun loadBookmarkedFeedPosts() {
        if (isDataLoaded) return

        isDataLoaded = true

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "📚 Fetching bookmarked posts for @$username (ID: $otherUserId)")

                // Show loading only if we don't have cache
                if (!isCacheValid()) {
                    withContext(Dispatchers.Main) {
                        showLoading()
                    }
                }

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

                val bookmarkedPosts = responseBody?.data?.bookmarkedPosts.orEmpty()
                Log.d(TAG, "Received ${bookmarkedPosts.size} total bookmarked posts from server")

                // Filter for posts bookmarked by the other user (same as MyUserFavoritesFragment)
                val transformedPosts = bookmarkedPosts
                    .asSequence()
                    .filter {
                        val isBookmarkedByUser = it.bookmarkedBy == otherUserId
                        if (isBookmarkedByUser) {
                            Log.d(TAG, "✓ Found favorite: ${it._id} bookmarked by @$username")
                        }
                        isBookmarkedByUser
                    }
                    .mapNotNull { bookmarkedPost ->
                        try {
                            Post(
                                _id = bookmarkedPost._id,
                                content = bookmarkedPost.content ?: "",
                                duration = bookmarkedPost.duration,
                                feedShortsBusinessId = bookmarkedPost.feedShortsBusinessId,
                                tags = bookmarkedPost.tags,
                                contentType = bookmarkedPost.contentType,
                                numberOfPages = bookmarkedPost.numberOfPages,
                                fileNames = bookmarkedPost.fileNames,
                                fileTypes = bookmarkedPost.fileTypes,
                                fileSizes = bookmarkedPost.fileSizes,
                                files = bookmarkedPost.files,
                                fileIds = bookmarkedPost.fileIds,
                                thumbnail = bookmarkedPost.thumbnail,
                                author = bookmarkedPost.author,
                                isReposted = bookmarkedPost.isReposted,
                                repostedByUserId = bookmarkedPost.repostedByUserId ?: "",
                                repostedUsers = bookmarkedPost.repostedUsers,
                                createdAt = bookmarkedPost.createdAt,
                                updatedAt = bookmarkedPost.updatedAt,
                                __v = bookmarkedPost.__v,
                                comments = bookmarkedPost.comments,
                                likes = bookmarkedPost.likes,
                                isLiked = bookmarkedPost.isLiked,
                                isFollowing = bookmarkedPost.isFollowing,
                                isBookmarked = true,
                                bookmarkCount = bookmarkedPost.bookmarkCount,
                                isInCloseFriends = bookmarkedPost.isInCloseFriends,
                                isPostsMuted = bookmarkedPost.isPostsMuted,
                                isStoriesMuted = bookmarkedPost.isStoriesMuted,
                                isFavorite = bookmarkedPost.isFavorite,
                                isRestricted = bookmarkedPost.isRestricted,
                                originalPost = bookmarkedPost.originalPost,
                                isExpanded = false,
                                isLocal = false,
                                repostCount = 0,
                                shareCount = 0,
                                repostedUser = bookmarkedPost.repostedUser ?: emptyRepostedUser(),
                                isBusinessPost = false
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error transforming post ${bookmarkedPost._id}: ${e.message}")
                            null
                        }
                    }
                    .toList()

                Log.d(TAG, "📚 Found ${transformedPosts.size} favorites for @$username")

                withContext(Dispatchers.Main) {
                    if (transformedPosts.isNotEmpty()) {
                        allFavorites.clear()
                        allFavorites.addAll(transformedPosts)
                        submitToAdapter(allFavorites)
                        showContent()

                        // Update cache
                        otherUserId?.let { uid ->
                            favoritesCache[uid] = allFavorites.toMutableList()
                            cacheTimestamp[uid] = System.currentTimeMillis()
                            Log.d(TAG, "Cache updated - ${allFavorites.size} items stored")
                        }
                    } else {
                        showEmptyState()
                        Log.d(TAG, "No favorites found for @$username")
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

    // FeedAdapter callbacks - delegate to parent activity's
    override fun likeUnLikeFeed(position: Int, data: Post) {
       Log.e(TAG, " not found")
    }

    override fun feedCommentClicked(position: Int, data: Post) {
       Log.e(TAG, " not found")
    }

    override fun feedFavoriteClick(position: Int, data: Post) {
       Log.e(TAG, " not found")
    }

    override fun moreOptionsClick(position: Int, data: Post) {
        Log.e(TAG, " not found")
    }

    override fun feedFileClicked(position: Int, data: Post) {
         Log.e(TAG, " not found")
    }

    override fun feedRepostFileClicked(position: Int, data: OriginalPost) {
         Log.e(TAG, " not found")
    }

    override fun feedShareClicked(position: Int, data: Post) {
        Log.e(TAG, " not found")
    }

    override fun followButtonClicked(followUnFollowEntity: FollowUnFollowEntity, followButton: AppCompatButton) {
        Log.e(TAG, " not found")
    }

    override fun feedRepostPost(position: Int, data: Post) {
         Log.e(TAG, " not found")
    }

    override fun feedRepostPostClicked(position: Int, data: Post) {
         Log.e(TAG, " not found")
    }

    override fun feedClickedToOriginalPost(position: Int, originalPostId: String) {
        Log.e(TAG, " not found")
    }

    override fun onImageClick() {
         Log.e(TAG, " not found")
    }
}