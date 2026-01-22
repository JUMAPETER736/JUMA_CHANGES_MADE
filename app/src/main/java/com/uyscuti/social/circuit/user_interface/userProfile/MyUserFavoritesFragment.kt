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

        private fun emptyRepostedUser(): RepostedUser {
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
        clearCache(userId ?: "")
        allUserFavorites.clear()
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
            feedAdapter.notifyDataSetChanged()

            Log.d(TAG, "Successfully submitted posts to adapter")
        } catch (e: Exception) {
            Log.e(TAG, "Error submitting to adapter", e)
            e.printStackTrace()
        }
    }

    private fun isCacheValid(): Boolean {
        val timestamp = cacheTimestamp[userId] ?: return false
        return System.currentTimeMillis() - timestamp < CACHE_VALIDITY_MS
    }

    private fun loadBookmarkedFeedPosts() {
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

                val bookmarkedPosts = responseBody?.data?.bookmarkedPosts.orEmpty()
                Log.d(TAG, "Received ${bookmarkedPosts.size} bookmarked posts from server")

                // Transform bookmarked posts to regular posts with proper field mapping
                val transformedPosts = bookmarkedPosts
                    .filter { it.bookmarkedBy == userId }
                    .mapNotNull { bookmarkedPost ->
                        try {
                            // Create a properly structured Post object
                            val post = Post(
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
                                files = bookmarkedPost.files, // This is critical!
                                fileIds = bookmarkedPost.fileIds,
                                thumbnail = bookmarkedPost.thumbnail,
                                author = bookmarkedPost.author,
                                isReposted = bookmarkedPost.isReposted,
                                repostedByUserId = bookmarkedPost.repostedByUserId,
                                repostedUsers = bookmarkedPost.repostedUsers,
                                createdAt = bookmarkedPost.createdAt,
                                updatedAt = bookmarkedPost.updatedAt,
                                __v = bookmarkedPost.__v,
                                comments = bookmarkedPost.comments,
                                likes = bookmarkedPost.likes,
                                isLiked = bookmarkedPost.isLiked,
                                isFollowing = bookmarkedPost.isFollowing,
                                isBookmarked = true, // Always true for bookmarked items
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

                            Log.d(TAG, "Transformed Post ${post._id}:")
                            Log.d(TAG, "  - Content: ${post.content}")
                            Log.d(TAG, "  - ContentType: ${post.contentType}")
                            Log.d(TAG, "  - Files count: ${post.files.size}")
                            Log.d(TAG, "  - FileIds count: ${post.fileIds.size}")
                            Log.d(TAG, "  - FileTypes count: ${post.fileTypes.size}")
                            Log.d(TAG, "  - isBookmarked: ${post.isBookmarked}")

                            post.files.forEachIndexed { index, file ->
                                Log.d(TAG, "  - File $index: ${file.url}")
                            }

                            post
                        } catch (e: Exception) {
                            Log.e(TAG, "Error transforming bookmarked post ${bookmarkedPost._id}", e)
                            null
                        }
                    }

                Log.d(TAG, "Transformed ${transformedPosts.size} posts successfully")

                withContext(Dispatchers.Main) {
                    if (transformedPosts.isNotEmpty()) {
                        allUserFavorites.addAll(transformedPosts)
                        submitToAdapter(allUserFavorites)
                        showContent()

                        // Update cache
                        userId?.let { uid ->
                            favoritesCache[uid] = allUserFavorites.toMutableList()
                            cacheTimestamp[uid] = System.currentTimeMillis()
                            Log.d(TAG, "Cache updated for user $uid with ${allUserFavorites.size} posts")
                        }
                    } else {
                        Log.d(TAG, "No bookmarked posts found")
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

    // FeedAdapter callbacks
    override fun feedFavoriteClick(position: Int, data: Post) {
        if (!data.isBookmarked && position in allUserFavorites.indices) {
            Log.d(TAG, "Removing unbookmarked post at position $position")
            allUserFavorites.removeAt(position)
            feedAdapter.submitItems(allUserFavorites)
            feedAdapter.notifyItemRemoved(position)

            // Update cache
            userId?.let {
                favoritesCache[it] = allUserFavorites.toMutableList()
            }

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