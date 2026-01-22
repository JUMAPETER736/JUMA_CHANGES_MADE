package com.uyscuti.social.circuit.user_interface.userProfile

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.uyscuti.sharedmodule.adapter.feed.FeedAdapter
import com.uyscuti.sharedmodule.adapter.feed.OnFeedClickListener
import com.uyscuti.social.circuit.databinding.MyUserFavoritesFragmentBinding
import com.uyscuti.social.circuit.ui.fragments.feed.AllFragment
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
class MyUserFavoritesFragment : Fragment(), OnFeedClickListener {

    companion object {
        private const val TAG = "MyUserFavoritesFragment"
        private const val ARG_USER_ID = "userId"
        private const val ARG_USERNAME = "username"

        internal val favoritesCache = mutableMapOf<String, MutableList<Post>>()
        internal val cacheTimestamp = mutableMapOf<String, Long>()
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

        internal fun emptyRepostedUser(): RepostedUser {
            return RepostedUser(
                _id = "",
                avatar = Avatar(
                    _id = "",
                    url = "",
                    localPath = ""
                ),
                bio = "",
                coverImage = CoverImage(
                    _id = "",
                    localPath = "",
                    url = ""
                ),
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

    private var _binding: MyUserFavoritesFragmentBinding? = null
    private val binding get() = _binding!!

    private var userId: String? = null
    private var username: String? = null

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
        }

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

        // OPTIMIZATION: Show cached data IMMEDIATELY without checking validity
        val cached = favoritesCache[userId]
        if (!cached.isNullOrEmpty()) {
            allUserFavorites.clear()
            allUserFavorites.addAll(cached)
            submitToAdapter(allUserFavorites)
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
        Log.d(TAG, "Refreshing bookmarks")
        isDataLoaded = false
        // Don't clear cache - let old data show while loading new
        loadBookmarkedFeedPosts()
    }

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
            setItemViewCacheSize(30) // Increased from 20
            // OPTIMIZATION: Add recycled view pool for better scrolling
            recycledViewPool.setMaxRecycledViews(0, 30)
            visibility = View.GONE
        }
    }

    private fun submitToAdapter(posts: List<Post>) {
        if (!isAdded || _binding == null) return

        try {
            feedAdapter.submitItems(posts)
            feedAdapter.initializeCommentCounts(posts)
            // OPTIMIZATION: Only notify if actually needed
            if (feedAdapter.itemCount != posts.size) {
                feedAdapter.notifyDataSetChanged()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error submitting to adapter", e)
        }
    }

    private fun isCacheValid(): Boolean {
        val timestamp = cacheTimestamp[userId] ?: return false
        return System.currentTimeMillis() - timestamp < CACHE_VALIDITY_MS
    }

    private fun loadBookmarkedFeedPosts() {
        if (isDataLoaded) return

        isDataLoaded = true

        // Don't show loading immediately - let cache show first
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching bookmarked posts for user: $userId")

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
                Log.d(TAG, "Received ${bookmarkedPosts.size} bookmarked posts from server")

                // Transform bookmarked posts to regular posts with proper field mapping
                val transformedPosts = bookmarkedPosts
                    .asSequence() // Use sequence for better performance
                    .filter { it.bookmarkedBy == userId }
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

                withContext(Dispatchers.Main) {
                    if (transformedPosts.isNotEmpty()) {
                        allUserFavorites.clear()
                        allUserFavorites.addAll(transformedPosts)
                        submitToAdapter(allUserFavorites)
                        showContent()

                        // Update cache
                        userId?.let { uid ->
                            favoritesCache[uid] = allUserFavorites.toMutableList()
                            cacheTimestamp[uid] = System.currentTimeMillis()
                        }
                    } else {
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
    

    // Replace the stub methods in MyUserFeedFragment with these implementations:

    override fun likeUnLikeFeed(position: Int, data: Post) {
        Log.d(TAG, "Like clicked at position $position - delegating to AllFragment")

        // Get reference to AllFragment
        val allFragment = requireActivity().supportFragmentManager.findFragmentByTag("AllFragment") as? AllFragment

        if (allFragment != null) {
            allFragment.likeUnLikeFeed(position, data)
        } else {
            Log.e(TAG, "AllFragment not found, cannot delegate like action")
        }
    }

    override fun feedCommentClicked(position: Int, data: Post) {
        Log.d(TAG, "Comment clicked at position $position - delegating to AllFragment")

        val allFragment = requireActivity().supportFragmentManager.findFragmentByTag("AllFragment") as? AllFragment

        if (allFragment != null) {
            allFragment.feedCommentClicked(position, data)
        } else {
            Log.e(TAG, "AllFragment not found, cannot delegate comment action")
        }
    }

    override fun feedFavoriteClick(position: Int, data: Post) {
        Log.d(TAG, "Favorite clicked at position $position - delegating to AllFragment")

        val allFragment = requireActivity().supportFragmentManager.findFragmentByTag("AllFragment") as? AllFragment

        if (allFragment != null) {
            allFragment.feedFavoriteClick(position, data)
        } else {
            Log.e(TAG, "AllFragment not found, cannot delegate favorite action")
        }
    }

    override fun moreOptionsClick(position: Int, data: Post) {
        Log.d(TAG, "More options clicked at position $position - delegating to AllFragment")

        val allFragment = requireActivity().supportFragmentManager.findFragmentByTag("AllFragment") as? AllFragment

        if (allFragment != null) {
            allFragment.moreOptionsClick(position, data)
        } else {
            Log.e(TAG, "AllFragment not found, cannot delegate more options action")
        }
    }

    override fun feedFileClicked(position: Int, data: Post) {
        Log.d(TAG, "File clicked at position $position - delegating to AllFragment")

        val allFragment = requireActivity().supportFragmentManager.findFragmentByTag("AllFragment") as? AllFragment

        if (allFragment != null) {
            allFragment.feedFileClicked(position, data)
        } else {
            Log.e(TAG, "AllFragment not found, cannot delegate file click action")
        }
    }

    override fun feedRepostFileClicked(position: Int, data: OriginalPost) {
        Log.d(TAG, "Repost file clicked at position $position - delegating to AllFragment")

        val allFragment = requireActivity().supportFragmentManager.findFragmentByTag("AllFragment") as? AllFragment

        if (allFragment != null) {
            allFragment.feedRepostFileClicked(position, data)
        } else {
            Log.e(TAG, "AllFragment not found, cannot delegate repost file click action")
        }
    }

    override fun feedShareClicked(position: Int, data: Post) {
        Log.d(TAG, "Share clicked at position $position - delegating to AllFragment")

        val allFragment = requireActivity().supportFragmentManager.findFragmentByTag("AllFragment") as? AllFragment

        if (allFragment != null) {
            allFragment.feedShareClicked(position, data)
        } else {
            Log.e(TAG, "AllFragment not found, cannot delegate share action")
        }
    }

    override fun followButtonClicked(followUnFollowEntity: FollowUnFollowEntity, followButton: AppCompatButton) {
        Log.d(TAG, "Follow clicked for user ${followUnFollowEntity.userId} - delegating to AllFragment")

        val allFragment = requireActivity().supportFragmentManager.findFragmentByTag("AllFragment") as? AllFragment

        if (allFragment != null) {
            allFragment.followButtonClicked(followUnFollowEntity, followButton)
        } else {
            Log.e(TAG, "AllFragment not found, cannot delegate follow action")
        }
    }

    override fun feedRepostPost(position: Int, data: Post) {
        Log.d(TAG, "Repost clicked at position $position - delegating to AllFragment")

        val allFragment = requireActivity().supportFragmentManager.findFragmentByTag("AllFragment") as? AllFragment

        if (allFragment != null) {
            allFragment.feedRepostPost(position, data)
        } else {
            Log.e(TAG, "AllFragment not found, cannot delegate repost action")
        }
    }

    override fun feedRepostPostClicked(position: Int, data: Post) {
        Log.d(TAG, "Repost post clicked at position $position - delegating to AllFragment")

        val allFragment = requireActivity().supportFragmentManager.findFragmentByTag("AllFragment") as? AllFragment

        if (allFragment != null) {
            allFragment.feedRepostPostClicked(position, data)
        } else {
            Log.e(TAG, "AllFragment not found, cannot delegate repost post click action")
        }
    }

    override fun feedClickedToOriginalPost(position: Int, originalPostId: String) {
        Log.d(TAG, "Original post clicked: $originalPostId - delegating to AllFragment")

        val allFragment = requireActivity().supportFragmentManager.findFragmentByTag("AllFragment") as? AllFragment

        if (allFragment != null) {
            allFragment.feedClickedToOriginalPost(position, originalPostId)
        } else {
            Log.e(TAG, "AllFragment not found, cannot delegate original post click action")
        }
    }

    override fun onImageClick() {
        Log.d(TAG, "Image clicked - delegating to AllFragment")

        val allFragment = requireActivity().supportFragmentManager.findFragmentByTag("AllFragment") as? AllFragment

        if (allFragment != null) {
            allFragment.onImageClick()
        } else {
            Log.e(TAG, "AllFragment not found, cannot delegate image click action")
        }
    }
}