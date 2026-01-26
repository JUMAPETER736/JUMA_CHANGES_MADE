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
import androidx.lifecycle.ViewModelProvider
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import android.content.SharedPreferences



@AndroidEntryPoint
class MutedPostsActivity : AppCompatActivity(), OnFeedClickListener {

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var feedAdapter: FeedAdapter
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var retrofitInstance: RetrofitInstance

    private val TAG = "MutedPostsActivity"
    private val mutedPosts = mutableListOf<Post>()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_muted_posts)

        // Initialize RetrofitInstance properly
        val localStorage = LocalStorage.getInstance(this)
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

        sharedPrefs = getSharedPreferences("user_actions", Context.MODE_PRIVATE)

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

    // FIXED showLoading method - Don't hide emptyStateLayout here
    private fun showLoading(show: Boolean) {
        Log.d(TAG, "showLoading called with: $show")

        progressBar.visibility = if (show) View.VISIBLE else View.GONE

        if (show) {
            // When loading, hide both recyclerView and emptyStateLayout
            recyclerView.visibility = View.GONE
            emptyStateLayout.visibility = View.GONE
        }
        // When not loading, don't change recyclerView or emptyStateLayout visibility
        // Let showEmptyState handle that

        Log.d(TAG, "Loading state - progressBar: ${progressBar.visibility}, recyclerView: ${recyclerView.visibility}, emptyStateLayout: ${emptyStateLayout.visibility}")
    }

    // FIXED showEmptyState method
    private fun showEmptyState(show: Boolean) {
        Log.d(TAG, "showEmptyState called with: $show")
        Log.d(TAG, "emptyStateLayout visibility before: ${emptyStateLayout.visibility}")
        Log.d(TAG, "recyclerView visibility before: ${recyclerView.visibility}")
        Log.d(TAG, "mutedPosts size: ${mutedPosts.size}")

        if (show) {
            emptyStateLayout.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            progressBar.visibility = View.GONE
        } else {
            emptyStateLayout.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }

        Log.d(TAG, "emptyStateLayout visibility after: ${emptyStateLayout.visibility}")
        Log.d(TAG, "recyclerView visibility after: ${recyclerView.visibility}")
    }

    // UPDATED loadMutedPosts - correct order of calls
    private fun loadMutedPosts() {
        lifecycleScope.launch {
            try {
                showLoading(true)

                val mutedUserIds = getMutedUserIds()
                Log.d(TAG, "Found ${mutedUserIds.size} muted user IDs: $mutedUserIds")

                if (mutedUserIds.isEmpty()) {
                    mutedPosts.clear()
                    feedAdapter.submitItems(mutableListOf())
                    showEmptyState(true)  // Call this AFTER showLoading(false)
                    Log.d(TAG, "No muted users found - showing empty state")
                    return@launch
                }

                // Fetch all posts from the feed and filter muted ones
                mutedPosts.clear()

                val response = withContext(Dispatchers.IO) {
                    retrofitInstance.apiService.getAllFeed("1")
                }

                if (response.isSuccessful && response.body() != null) {
                    val allPosts = response.body()!!.data.data.posts

                    val foundMutedPosts = allPosts.filter { post ->
                        val authorId = post.author?.account?._id
                        val reposterId = post.repostedUser?.owner
                        val posterId = reposterId ?: authorId

                        val isMuted = posterId?.let { mutedUserIds.contains(it) } ?: false

                        if (isMuted) {
                            Log.d(TAG, "Found muted post from user: $posterId")
                        }

                        isMuted
                    }

                    mutedPosts.addAll(foundMutedPosts)
                    Log.d(TAG, "Found ${foundMutedPosts.size} muted posts from page 1")

                    // If we need more pages
                    if (foundMutedPosts.isNotEmpty()) {
                        for (page in 2..5) {
                            val pageResponse = withContext(Dispatchers.IO) {
                                retrofitInstance.apiService.getAllFeed(page.toString())
                            }

                            if (pageResponse.isSuccessful && pageResponse.body() != null) {
                                val pagePosts = pageResponse.body()!!.data.data.posts

                                val pageMutedPosts = pagePosts.filter { post ->
                                    val authorId = post.author?.account?._id
                                    val reposterId = post.repostedUser?.owner
                                    val posterId = reposterId ?: authorId

                                    val isMuted = posterId?.let { mutedUserIds.contains(it) } ?: false

                                    isMuted && !mutedPosts.any { it._id == post._id }
                                }

                                mutedPosts.addAll(pageMutedPosts)
                                Log.d(TAG, "Found ${pageMutedPosts.size} more muted posts from page $page")

                                if (pageMutedPosts.isEmpty()) break
                            }
                        }
                    }
                }

                // Sort by most recent
                mutedPosts.sortByDescending { post ->
                    post.createdAt
                }

                // Update adapter
                feedAdapter.submitItems(mutedPosts.toMutableList())

                // Show empty state or recycler view based on posts count
                showEmptyState(mutedPosts.isEmpty())

                Log.d(TAG, "Successfully loaded ${mutedPosts.size} muted posts")

            } catch (e: Exception) {
                Log.e(TAG, "Error loading muted posts: ${e.message}", e)
                Toast.makeText(
                    this@MutedPostsActivity,
                    "Failed to load muted posts: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                showEmptyState(true)
            }
        }
    }

    // ===== Get muted user IDs from ALL three sources (same as feed filtering) =====
    private fun getMutedUserIds(): Set<String> {
        val allMutedIds = mutableSetOf<String>()

        // Source 1: FeedAdapter companion object static cache
        allMutedIds.addAll(FeedAdapter.getMutedPostsUsers())

        // Source 2: SharedPreferences
        val prefsIds = sharedPrefs.getStringSet("muted_posts", emptySet()) ?: emptySet()
        allMutedIds.addAll(prefsIds)

        Log.d(TAG, "Source 1 (FeedAdapter cache): ${FeedAdapter.getMutedPostsUsers().size}")
        Log.d(TAG, "Source 2 (SharedPreferences): ${prefsIds.size}")
        Log.d(TAG, "Total unique muted user IDs: ${allMutedIds.size}")

        return allMutedIds
    }
    

    // ==================== OnFeedClickListener IMPLEMENTATION ====================

    override fun likeUnLikeFeed(position: Int, data: Post) {
        Toast.makeText(this, "Unmute the user to interact with their posts", Toast.LENGTH_SHORT).show()
    }

    override fun feedCommentClicked(position: Int, data: Post) {
        Toast.makeText(this, "Unmute the user to comment", Toast.LENGTH_SHORT).show()
    }

    override fun feedFavoriteClick(position: Int, data: Post) {
        Toast.makeText(this, "Unmute the user to bookmark", Toast.LENGTH_SHORT).show()
    }

    override fun moreOptionsClick(position: Int, data: Post) {
        showUnmuteDialog(position, data)
    }

    override fun feedFileClicked(position: Int, data: Post) {
        Toast.makeText(this, "Unmute the user to view media", Toast.LENGTH_SHORT).show()
    }

    override fun feedRepostFileClicked(position: Int, data: OriginalPost) {
        Toast.makeText(this, "Unmute the user to view", Toast.LENGTH_SHORT).show()
    }

    override fun feedShareClicked(position: Int, data: Post) {
        Toast.makeText(this, "Unmute the user to share", Toast.LENGTH_SHORT).show()
    }

    override fun followButtonClicked(
        followUnFollowEntity: FollowUnFollowEntity,
        followButton: AppCompatButton
    ) {
        Toast.makeText(this, "Unmute the user to follow", Toast.LENGTH_SHORT).show()
    }

    override fun feedRepostPost(position: Int, data: Post) {
        Toast.makeText(this, "Unmute the user to repost", Toast.LENGTH_SHORT).show()
    }

    override fun feedRepostPostClicked(position: Int, data: Post) {
        Toast.makeText(this, "Unmute the user to view repost", Toast.LENGTH_SHORT).show()
    }

    override fun feedClickedToOriginalPost(position: Int, originalPostId: String) {
        Toast.makeText(this, "Unmute the user to view original", Toast.LENGTH_SHORT).show()
    }

    override fun onImageClick() {
        Toast.makeText(this, "Unmute the user to view images", Toast.LENGTH_SHORT).show()
    }

    // ==================== UNMUTE FUNCTIONALITY ====================

    private fun showUnmuteDialog(position: Int, data: Post) {
        val userId = data.author?.account?._id ?: return

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Unmute User")
            .setMessage("Do you want to unmute this user and show their posts in your feed again?")
            .setPositiveButton("Unmute") { dialog, _ ->
                handleUnmuteUser(position, data, userId)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun handleUnmuteUser(position: Int, data: Post, userId: String) {
        Log.d(TAG, "Unmuting user: $userId at position: $position")

        lifecycleScope.launch {
            try {
                // Call API to unmute
                val response = withContext(Dispatchers.IO) {
                    retrofitInstance.apiService.unMutePosts(userId)
                }

                if (response.isSuccessful) {
                    Log.d(TAG, "API call successful. Updating ALL caches...")

                    // Update ALL caches (same as feed mute/unmute)
                    // 1. Remove from FeedAdapter cache
                    FeedAdapter.removeFromMutedPostsCache(userId)

                    // 2. Remove from SharedPreferences
                    val currentMutedIds = sharedPrefs.getStringSet("muted_posts", emptySet())?.toMutableSet() ?: mutableSetOf()
                    currentMutedIds.remove(userId)
                    sharedPrefs.edit().putStringSet("muted_posts", currentMutedIds).apply()

                    Log.d(TAG, "Removed $userId from all caches")

                    // Remove ALL posts from this user from the list
                    val postsToRemove = mutedPosts.filter { post ->
                        val authorId = post.author?.account?._id
                        val reposterId = post.repostedUser?.owner
                        val posterId = reposterId ?: authorId
                        posterId == userId
                    }

                    mutedPosts.removeAll(postsToRemove)
                    feedAdapter.submitItems(mutedPosts.toMutableList())

                    Log.d(TAG, "Removed ${postsToRemove.size} posts from user. Remaining: ${mutedPosts.size}")

                    // Show snackbar with undo option
                    Snackbar.make(
                        recyclerView,
                        "User unmuted",
                        Snackbar.LENGTH_LONG
                    ).setAction("Undo") {
                        // Re-mute the user
                        lifecycleScope.launch {
                            try {
                                val muteResponse = withContext(Dispatchers.IO) {
                                    retrofitInstance.apiService.mutePosts(userId)
                                }

                                if (muteResponse.isSuccessful) {
                                    // Re-add to caches
                                    FeedAdapter.addToMutedPostsCache(userId)

                                    val currentIds = sharedPrefs.getStringSet("muted_posts", emptySet())?.toMutableSet() ?: mutableSetOf()
                                    currentIds.add(userId)
                                    sharedPrefs.edit().putStringSet("muted_posts", currentIds).apply()

                                    Log.d(TAG, "Undo: Re-muted user $userId")

                                    // Re-add posts to list
                                    mutedPosts.addAll(postsToRemove)
                                    mutedPosts.sortByDescending { it.createdAt }
                                    feedAdapter.submitItems(mutedPosts.toMutableList())

                                    showEmptyState(false)

                                    Toast.makeText(this@MutedPostsActivity, "User muted again", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error undoing unmute: ${e.message}")
                                Toast.makeText(this@MutedPostsActivity, "Failed to undo", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }.show()

                    // Check if list is now empty
                    if (mutedPosts.isEmpty()) {
                        showEmptyState(true)
                        Log.d(TAG, "No more muted posts")
                    }

                } else {
                    Log.e(TAG, "API call failed with code: ${response.code()}")
                    Toast.makeText(this@MutedPostsActivity, "Failed to unmute user", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error in handleUnmuteUser: ${e.message}", e)
                Toast.makeText(this@MutedPostsActivity, "Network error", Toast.LENGTH_SHORT).show()
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