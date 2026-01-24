package com.uyscuti.social.circuit.settings

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.uyscuti.sharedmodule.adapter.feed.FeedAdapter
import com.uyscuti.sharedmodule.adapter.feed.OnFeedClickListener
import com.uyscuti.sharedmodule.viewmodels.feed.UserRelationshipsViewModel
import com.uyscuti.social.circuit.R
import com.uyscuti.social.core.common.data.room.entity.FollowUnFollowEntity
import com.uyscuti.social.network.api.response.posts.OriginalPost
import com.uyscuti.social.network.api.response.posts.Post
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MutedPostsActivity : AppCompatActivity(), OnFeedClickListener {

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var feedAdapter: FeedAdapter
    private lateinit var retrofitInstance: RetrofitInstance

    private val TAG = "MutedPostsActivity"
    private val mutedPosts = mutableListOf<Post>()
    private val mutedUserIds = mutableSetOf<String>()

    @Inject
    lateinit var localStorage: LocalStorage

    private val relationshipsViewModel: UserRelationshipsViewModel by viewModels()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_muted_posts)

        // Initialize RetrofitInstance with injected dependencies
        retrofitInstance = RetrofitInstance(localStorage, this)

        // Initialize views
        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)

        setSupportActionBar(toolbar)
        supportActionBar?.title = "Muted Posts"
        toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        // Setup RecyclerView with FeedAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        feedAdapter = FeedAdapter(
            context = this,
            retrofitInterface = retrofitInstance,
            feedClickListener = this,
            fragmentManager = supportFragmentManager
        )
        recyclerView.adapter = feedAdapter

        loadMutedPosts()
    }

    private fun loadMutedPosts() {
        lifecycleScope.launch {
            try {
                showLoading(true)

                // First, get the list of muted users
                val mutedUsersResponse = withContext(Dispatchers.IO) {
                    retrofitInstance.apiService.getMutedPostsUsers()
                }

                if (mutedUsersResponse.isSuccessful && mutedUsersResponse.body()?.success == true) {
                    // Extract muted user IDs
                    mutedUserIds.clear()
                    mutedUsersResponse.body()?.data?.forEach { mutedPostsItem ->
                        mutedPostsItem.user?._id?.let { mutedUserIds.add(it) }
                    }

                    Log.d(TAG, "Found ${mutedUserIds.size} muted users: $mutedUserIds")

                    if (mutedUserIds.isEmpty()) {
                        showEmptyState(true)
                        showLoading(false)
                        return@launch
                    }

                    // IMPORTANT: Temporarily clear the muted cache so getAllFeed returns all posts
                    val originalMutedUsers = FeedAdapter.getMutedPostsUsers().toSet()
                    FeedAdapter.clearMutedPostsCache()

                    // Fetch all posts from the feed and filter posts from muted users
                    mutedPosts.clear()

                    // Fetch from feed and filter
                    val response = withContext(Dispatchers.IO) {
                        retrofitInstance.apiService.getAllFeed("1")
                    }

                    if (response.isSuccessful && response.body() != null) {
                        val allPosts = response.body()!!.data.data.posts

                        // Filter posts from muted users
                        val foundMutedPosts = allPosts.filter { post ->
                            mutedUserIds.contains(post.author._id)
                        }

                        mutedPosts.addAll(foundMutedPosts)
                        Log.d(TAG, "Found ${foundMutedPosts.size} muted posts from page 1")

                        // If we need more pages to find all muted posts
                        if (foundMutedPosts.size < 10 && mutedUserIds.size > 1) {
                            // Try fetching more pages
                            for (page in 2..5) { // Check up to 5 pages
                                val pageResponse = withContext(Dispatchers.IO) {
                                    retrofitInstance.apiService.getAllFeed(page.toString())
                                }

                                if (pageResponse.isSuccessful && pageResponse.body() != null) {
                                    val pagePosts = pageResponse.body()!!.data.data.posts
                                    val pageMutedPosts = pagePosts.filter { post ->
                                        mutedUserIds.contains(post.author._id) &&
                                                !mutedPosts.any { it._id == post._id }
                                    }
                                    mutedPosts.addAll(pageMutedPosts)
                                    Log.d(TAG, "Found ${pageMutedPosts.size} more muted posts from page $page")
                                }
                            }
                        }
                    }

                    // IMPORTANT: Restore the muted cache
                    FeedAdapter.setMutedPostsUsers(originalMutedUsers)

                    // Sort by post creation date (most recent first)
                    mutedPosts.sortByDescending { it.createdAt }

                    // Update adapter
                    feedAdapter.submitItems(mutedPosts.toMutableList())

                    showEmptyState(mutedPosts.isEmpty())
                    showLoading(false)

                    Log.d(TAG, "Successfully loaded ${mutedPosts.size} muted posts from ${mutedUserIds.size} users")

                    // Sort by post creation date (most recent first)
                    mutedPosts.sortByDescending { it.createdAt }

                    // Update adapter
                    if (mutedPosts.isNotEmpty()) {
                        feedAdapter.submitItems(mutedPosts.toMutableList())
                        showEmptyState(false)
                    } else {
                        showEmptyState(true)
                    }

                    showLoading(false)
                    Log.d(TAG, "Successfully loaded ${mutedPosts.size} muted posts from ${mutedUserIds.size} users")

                } else {
                    val errorMessage = mutedUsersResponse.body()?.message ?: "Failed to load muted users"
                    showError(errorMessage)
                    showLoading(false)
                    showEmptyState(true)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error loading muted posts: ${e.message}", e)
                Toast.makeText(
                    this@MutedPostsActivity,
                    "Failed to load muted posts: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                showLoading(false)
                showEmptyState(true)
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
        emptyStateLayout.visibility = View.GONE
    }

    private fun showEmptyState(show: Boolean) {
        emptyStateLayout.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // ==================== OnFeedClickListener IMPLEMENTATION ====================

    override fun likeUnLikeFeed(position: Int, data: Post) {
        Toast.makeText(this, "Unmute this user's posts to like", Toast.LENGTH_SHORT).show()
    }

    override fun feedCommentClicked(position: Int, data: Post) {
        Toast.makeText(this, "Unmute this user's posts to comment", Toast.LENGTH_SHORT).show()
    }

    override fun feedFavoriteClick(position: Int, data: Post) {
        Toast.makeText(this, "Unmute this user's posts to bookmark", Toast.LENGTH_SHORT).show()
    }

    override fun moreOptionsClick(position: Int, data: Post) {
        showUnmuteDialog(position, data)
    }

    override fun feedFileClicked(position: Int, data: Post) {
        Toast.makeText(this, "Unmute this user's posts to view media", Toast.LENGTH_SHORT).show()
    }

    override fun feedRepostFileClicked(position: Int, data: OriginalPost) {
        Toast.makeText(this, "Unmute this user's posts to view", Toast.LENGTH_SHORT).show()
    }

    override fun feedShareClicked(position: Int, data: Post) {
        Toast.makeText(this, "Unmute this user's posts to share", Toast.LENGTH_SHORT).show()
    }

    override fun followButtonClicked(
        followUnFollowEntity: FollowUnFollowEntity,
        followButton: AppCompatButton
    ) {
        Toast.makeText(this, "Unmute this user's posts to follow", Toast.LENGTH_SHORT).show()
    }

    override fun feedRepostPost(position: Int, data: Post) {
        Toast.makeText(this, "Unmute this user's posts to repost", Toast.LENGTH_SHORT).show()
    }

    override fun feedRepostPostClicked(position: Int, data: Post) {
        Toast.makeText(this, "Unmute this user's posts to view repost", Toast.LENGTH_SHORT).show()
    }

    override fun feedClickedToOriginalPost(position: Int, originalPostId: String) {
        Toast.makeText(this, "Unmute this user's posts to view original", Toast.LENGTH_SHORT).show()
    }

    override fun onImageClick() {
        Toast.makeText(this, "Unmute this user's posts to view images", Toast.LENGTH_SHORT).show()
    }

    // ==================== UNMUTE FUNCTIONALITY ====================

    private fun showUnmuteDialog(position: Int, data: Post) {
        val username = data.author.account.username

        AlertDialog.Builder(this)
            .setTitle("Unmute posts from @$username?")
            .setMessage("You'll start seeing posts from this user again in your feed.")
            .setPositiveButton("Unmute") { dialog, _ ->
                handleUnmuteUser(position, data)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun handleUnmuteUser(position: Int, data: Post) {
        val userId = data.author._id
        val username = data.author.account.username

        Log.d(TAG, "Unmuting posts from user: $userId (@$username)")

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    retrofitInstance.apiService.unMutePosts(userId)
                }

                if (response.isSuccessful) {
                    // Remove all posts from this user from the list
                    val removedPosts = mutableListOf<Pair<Int, Post>>()
                    val iterator = mutedPosts.iterator()
                    var index = 0

                    while (iterator.hasNext()) {
                        val post = iterator.next()
                        if (post.author._id == userId) {
                            removedPosts.add(Pair(index, post))
                            iterator.remove()
                        }
                        index++
                    }

                    // Remove from muted users set
                    mutedUserIds.remove(userId)

                    // Update ViewModel
                    relationshipsViewModel.removeMutedPosts(userId)

                    // Update FeedAdapter cache
                    FeedAdapter.removeFromMutedPostsCache(userId)

                    // Update adapter
                    feedAdapter.submitItems(mutedPosts.toMutableList())

                    Log.d(TAG, "Removed ${removedPosts.size} posts from @$username. Remaining: ${mutedPosts.size}")

                    // Show snackbar with undo option
                    Snackbar.make(
                        recyclerView,
                        "Posts from @$username unmuted",
                        Snackbar.LENGTH_LONG
                    ).setAction("Undo") {
                        // Re-mute the user
                        remuteUser(userId, username, removedPosts)
                    }.show()

                    // Check if list is now empty
                    if (mutedPosts.isEmpty()) {
                        showEmptyState(true)
                        Log.d(TAG, "No more muted posts")
                    }
                } else {
                    showError("Failed to unmute posts")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error unmuting posts: ${e.message}", e)
                showError("Failed to unmute. Please try again.")
            }
        }
    }

    private fun remuteUser(userId: String, username: String, removedPosts: List<Pair<Int, Post>>) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    retrofitInstance.apiService.mutePosts(userId)
                }

                if (response.isSuccessful) {
                    // Add user back to muted set
                    mutedUserIds.add(userId)

                    // Add posts back to the list
                    removedPosts.forEach { (_, post) ->
                        mutedPosts.add(post)
                    }

                    // Sort again by creation date
                    mutedPosts.sortByDescending { it.createdAt }

                    // Update ViewModel
                    relationshipsViewModel.addMutedPosts(userId)

                    // Update FeedAdapter cache
                    FeedAdapter.addToMutedPostsCache(userId)

                    // Update adapter
                    feedAdapter.submitItems(mutedPosts.toMutableList())

                    showEmptyState(false)

                    Toast.makeText(
                        this@MutedPostsActivity,
                        "@$username posts muted again",
                        Toast.LENGTH_SHORT
                    ).show()

                    Log.d(TAG, "Undo: Re-muted posts from @$username")
                } else {
                    showError("Failed to mute posts again")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error muting posts again: ${e.message}", e)
                showError("Failed to undo. Please try again.")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: Reloading muted posts")
        // Reload when returning to this screen
        loadMutedPosts()
    }
}