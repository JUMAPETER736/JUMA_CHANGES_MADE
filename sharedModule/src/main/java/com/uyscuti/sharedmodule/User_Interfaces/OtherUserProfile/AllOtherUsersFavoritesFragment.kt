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
    private var myUserId: String? = null  // The logged-in user

    @Inject
    lateinit var retrofitInstance: RetrofitInstance

    private lateinit var feedAdapter: FeedAdapter
    private lateinit var localStorage: LocalStorage

    companion object {
        private const val TAG = "OtherUserFavoritesFragment"
        private const val ARG_USER_ID = "userId"
        private const val ARG_USERNAME = "username"

        // Cache for preloading
        internal val favoritesCache = mutableMapOf<String, MutableList<Post>>()
        internal val cacheTimestamp = mutableMapOf<String, Long>()

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

        localStorage = LocalStorage.getInstance(requireContext())
        myUserId = localStorage.getUserId()

        arguments?.let {
            otherUserId = it.getString(ARG_USER_ID)
            username = it.getString(ARG_USERNAME)
        }

        Log.d(TAG, "onCreate: Viewing favorites")
        Log.d(TAG, "  My ID: $myUserId")
        Log.d(TAG, "  Other user ID: $otherUserId (@$username)")
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

        // Check if viewing own profile
        if (otherUserId == myUserId) {
            // Viewing own favorites - load them
            setupRecyclerView()
            loadMyFavorites()
        } else {
            // Viewing someone else's favorites - show privacy message
            Log.d(TAG, "🔒 Showing privacy message - favorites are private")
            showPrivacyMessage()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::feedAdapter.isInitialized) {
            binding.recyclerView.adapter = null
        }
        _binding = null
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

    private fun loadMyFavorites() {
        showLoading()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "📚 Loading my own favorites")

                val response = retrofitInstance.apiService.getFavoriteFeed(page = "1")

                if (!response.isSuccessful) {
                    Log.e(TAG, "API call failed with code: ${response.code()}")
                    withContext(Dispatchers.Main) {
                        showEmptyState()
                    }
                    return@launch
                }

                val responseBody = response.body()
                val bookmarkedPosts = responseBody?.data?.bookmarkedPosts.orEmpty()
                Log.d(TAG, "Received ${bookmarkedPosts.size} bookmarked posts")

                val transformedPosts = bookmarkedPosts
                    .asSequence()
                    .filter { it.bookmarkedBy == myUserId }
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
                        feedAdapter.submitItems(transformedPosts)
                        feedAdapter.initializeCommentCounts(transformedPosts)
                        showContent()
                        Log.d(TAG, "Loaded ${transformedPosts.size} favorites")
                    } else {
                        showEmptyState()
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Load error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    showEmptyState()
                }
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.emptyView.visibility = View.GONE
        binding.privacyView.visibility = View.GONE
    }

    private fun showContent() {
        binding.progressBar.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
        binding.emptyView.visibility = View.GONE
        binding.privacyView.visibility = View.GONE
    }

    private fun showEmptyState() {
        binding.progressBar.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        binding.emptyView.visibility = View.VISIBLE
        binding.privacyView.visibility = View.GONE
    }

    private fun showPrivacyMessage() {
        binding.progressBar.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        binding.emptyView.visibility = View.GONE
        binding.privacyView.visibility = View.VISIBLE
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