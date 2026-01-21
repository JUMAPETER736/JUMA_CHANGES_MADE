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
            showContent()
        } else {
            loadBookmarkedPostsOptimized()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerView.adapter = null
        _binding = null
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
        if (!isAdded || _binding == null) return
        feedAdapter.submitItems(posts)
        feedAdapter.initializeCommentCounts(posts)
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
                val response = retrofitInstance.apiService.getFavoriteFeed(page = "1")

                if (!response.isSuccessful) {
                    withContext(Dispatchers.Main) { showEmptyState() }
                    return@launch
                }

                val posts = response.body()?.data?.bookmarkedPosts.orEmpty()
                    .mapNotNull { validateAndFixPost(it) }

                if (posts.isNotEmpty()) {
                    allUserFavorites.addAll(posts)
                    withContext(Dispatchers.Main) {
                        submitToAdapter(allUserFavorites)
                        showContent()
                    }
                } else {
                    withContext(Dispatchers.Main) { showEmptyState() }
                }

                favoritesCache[userId!!] = allUserFavorites.toMutableList()
                cacheTimestamp[userId!!] = System.currentTimeMillis()

            } catch (e: Exception) {
                Log.e(TAG, "Load error", e)
                withContext(Dispatchers.Main) { showEmptyState() }
            }
        }
    }

    // -------------------- VALIDATION --------------------

    private fun validateAndFixPost(post: Post): Post? {
        return try {
            post.isBookmarked = true
            post.comments = post.comments ?: 0
            post.likes = post.likes ?: 0
            post.bookmarkCount = post.bookmarkCount ?: 0
            post.repostCount = post.repostCount ?: 0
            post.shareCount = post.shareCount ?: 0

            if (post.author?.account == null) null else post
        } catch (e: Exception) {
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
        if (!data.isBookmarked && position in allUserFavorites.indices) {
            allUserFavorites.removeAt(position)
            submitToAdapter(allUserFavorites)

            userId?.let {
                favoritesCache[it] = allUserFavorites.toMutableList()
            }

            if (allUserFavorites.isEmpty()) showEmptyState()
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
