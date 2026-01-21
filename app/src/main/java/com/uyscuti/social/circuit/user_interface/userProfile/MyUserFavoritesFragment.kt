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
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.sharedmodule.adapter.feed.FeedAdapter
import com.uyscuti.sharedmodule.adapter.feed.OnFeedClickListener
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.MyUserFavoritesFragmentBinding
import com.uyscuti.social.core.common.data.room.entity.FollowUnFollowEntity
import com.uyscuti.social.network.api.response.posts.File
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
import kotlin.collections.isNotEmpty


@AndroidEntryPoint
class MyUserFavoritesFragment : Fragment(), OnFeedClickListener {

    companion object {
        private const val TAG = "MyUserFavoritesFragment"
        private const val ARG_USER_ID = "userId"
        private const val ARG_USERNAME = "username"

        fun newInstance(userId: String, username: String): MyUserFavoritesFragment {
            return MyUserFavoritesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_USER_ID, userId)
                    putString(ARG_USERNAME, username)
                }
            }
        }
    }

    private var _binding: MyUserFavoritesFragmentBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var retrofitInstance: RetrofitInstance
    private lateinit var apiService: IFlashapi

    private lateinit var feedAdapter: FeedAdapter
    private val favoritesList = mutableListOf<Post>()

    private var userId: String = ""
    private var username: String = ""

    private var currentPage = 1
    private var isLoading = false
    private var hasMorePages = true
    private var totalBookmarkedPosts = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString(ARG_USER_ID, "")
            username = it.getString(ARG_USERNAME, "")
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

        initializeApiService()
        setupRecyclerView()
        loadFavoritePosts()
    }

    private fun initializeApiService() {
        if (!::retrofitInstance.isInitialized) {
            val localStorage = LocalStorage(requireContext())
            retrofitInstance = RetrofitInstance(localStorage, requireContext())
        }
        apiService = retrofitInstance.apiService
    }

    private fun setupRecyclerView() {
        feedAdapter = FeedAdapter(
            context = requireContext(),
            retrofitInterface = retrofitInstance,
            feedClickListener = this,
            fragmentManager = parentFragmentManager
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = feedAdapter

            // Pagination
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if (!isLoading && hasMorePages) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 2
                            && firstVisibleItemPosition >= 0) {
                            loadMoreFavorites()
                        }
                    }
                }
            })
        }
    }

    private fun loadFavoritePosts() {
        if (isLoading) return

        isLoading = true
        showLoading()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getFavoriteFeed(currentPage.toString())

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!.data
                    val posts = data.bookmarkedPosts

                    totalBookmarkedPosts = data.totalBookmarkedPosts
                    hasMorePages = data.hasNextPage

                    withContext(Dispatchers.Main) {
                        if (posts.isEmpty() && currentPage == 1) {
                            showEmptyState()
                        } else {
                            favoritesList.clear()
                            favoritesList.addAll(posts)

                            // Update adapter with new posts
                            feedAdapter.clear()
                            feedAdapter.addAll(favoritesList)
                            feedAdapter.initializeCommentCounts(posts)
                            feedAdapter.notifyDataSetChanged()

                            showContent()
                        }

                        isLoading = false
                        hideLoading()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showError("Failed to load favorites")
                        isLoading = false
                        hideLoading()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading favorites: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    showError("Error: ${e.message}")
                    isLoading = false
                    hideLoading()
                }
            }
        }
    }

    private fun loadMoreFavorites() {
        if (isLoading || !hasMorePages) return

        isLoading = true
        currentPage++

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getFavoriteFeed(currentPage.toString())

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!.data
                    val posts = data.bookmarkedPosts

                    hasMorePages = data.hasNextPage

                    withContext(Dispatchers.Main) {
                        val oldSize = favoritesList.size
                        favoritesList.addAll(posts)

                        // Add new posts to adapter
                        feedAdapter.addAll(posts)
                        feedAdapter.notifyItemRangeInserted(oldSize, posts.size)

                        isLoading = false
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        currentPage--
                        isLoading = false
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading more favorites: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    currentPage--
                    isLoading = false
                }
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.emptyView.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showContent() {
        binding.recyclerView.visibility = View.VISIBLE
        binding.emptyView.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
    }

    private fun showEmptyState() {
        binding.emptyView.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.progressBar.visibility = View.GONE

        // Set empty state icon
        binding.emptyIcon.setImageResource(R.drawable.favorite_black)
    }

    private fun showError(message: String) {
        binding.emptyView.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.progressBar.visibility = View.GONE

        // Show error icon and message
        binding.emptyIcon.setImageResource(android.R.drawable.stat_notify_error)
        binding.emptyTitle.text = "Error Loading Favorites"
        binding.emptyMessage.text = message

        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    // OnFeedClickListener implementations
    override fun likeUnLikeFeed(position: Int, data: Post) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = if (data.isLiked) {
                    apiService.unlikePost(data._id)
                } else {
                    apiService.likePost(data._id)
                }

                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        data.isLiked = !data.isLiked
                        data.likes += if (data.isLiked) 1 else -1
                        feedAdapter.notifyItemChanged(position)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling like: ${e.message}", e)
            }
        }
    }

    override fun feedCommentClicked(position: Int, data: Post) {
        Log.d(TAG, "Comment clicked for post: ${data._id}")
        // Open comment bottom sheet or navigate to comments
    }

    override fun feedFavoriteClick(position: Int, data: Post) {
        // Item was unbookmarked in the ViewHolder
        // Now remove it from the favorites list

        val itemToRemove = favoritesList.getOrNull(position)
        if (itemToRemove != null && itemToRemove._id == data._id) {
            favoritesList.removeAt(position)
            feedAdapter.notifyItemRemoved(position)
            totalBookmarkedPosts--

            if (favoritesList.isEmpty()) {
                showEmptyState()
            }

            Toast.makeText(
                requireContext(),
                "Removed from favorites",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun moreOptionsClick(position: Int, data: Post) {
        Log.d(TAG, "More options clicked for post: ${data._id}")
        // Show options menu
    }

    override fun feedFileClicked(position: Int, data: Post) {
        Log.d(TAG, "Feed file clicked for post: ${data._id}")
        // Open media viewer
    }

    override fun feedRepostFileClicked(position: Int, data: OriginalPost) {
        Log.d(TAG, "Repost file clicked: ${data._id}")
    }

    override fun feedShareClicked(position: Int, data: Post) {
        Log.d(TAG, "Share clicked for post: ${data._id}")
        // Open share options
    }

    override fun followButtonClicked(
        followUnFollowEntity: FollowUnFollowEntity,
        followButton: AppCompatButton
    ) {
        Log.d(TAG, "Follow button clicked for user: ${followUnFollowEntity.userId}")
        // Handle follow/unfollow
    }

    override fun feedRepostPost(position: Int, data: Post) {
        Log.d(TAG, "Repost clicked for post: ${data._id}")
        // Handle repost
    }

    override fun feedRepostPostClicked(position: Int, data: Post) {
        Log.d(TAG, "Repost post clicked: ${data._id}")
    }

    override fun feedClickedToOriginalPost(position: Int, originalPostId: String) {
        Log.d(TAG, "Original post clicked: $originalPostId")
    }

    override fun onImageClick() {
        Log.d(TAG, "Image clicked")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}