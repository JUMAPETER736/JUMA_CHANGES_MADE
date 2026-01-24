package com.uyscuti.social.circuit.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.uyscuti.sharedmodule.adapter.feed.FeedAdapter
import com.uyscuti.sharedmodule.adapter.feed.OnFeedClickListener
import com.uyscuti.social.circuit.R
import com.uyscuti.social.core.common.data.room.entity.FollowUnFollowEntity
import com.uyscuti.social.network.api.response.posts.OriginalPost
import com.uyscuti.social.network.api.response.posts.Post
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext



class HiddenFeedPostsActivity : AppCompatActivity(), OnFeedClickListener {

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateTextView: TextView
    private lateinit var feedAdapter: FeedAdapter
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var retrofitInstance: RetrofitInstance

    private val TAG = "HiddenPostsActivity"
    private val hiddenPosts = mutableListOf<Post>()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hidden_posts)

        // Initialize RetrofitInstance properly
        val localStorage = LocalStorage.getInstance(this)
        retrofitInstance = RetrofitInstance(localStorage, this)

        // Initialize views
        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.hiddenPostsRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        emptyStateTextView = findViewById(R.id.emptyStateTextView)

        setSupportActionBar(toolbar)
        supportActionBar?.title = "Hidden Posts"
        toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        sharedPrefs = getSharedPreferences("HiddenPosts", Context.MODE_PRIVATE)

        // Setup RecyclerView with FeedAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        feedAdapter = FeedAdapter(
            context = this,
            retrofitInterface = retrofitInstance,
            feedClickListener = this,
            fragmentManager = supportFragmentManager
        )
        recyclerView.adapter = feedAdapter

        loadHiddenPosts()
    }

    private fun loadHiddenPosts() {
        lifecycleScope.launch {
            try {
                showLoading(true)

                // Get all hidden post IDs from SharedPreferences
                val hiddenPostIds = sharedPrefs.all.keys
                    .filter { !it.endsWith("_timestamp") }
                    .toList()

                Log.d(TAG, "Found ${hiddenPostIds.size} hidden post IDs: $hiddenPostIds")

                if (hiddenPostIds.isEmpty()) {
                    showEmptyState(true)
                    showLoading(false)
                    return@launch
                }

                // Fetch all posts from the feed and filter hidden ones
                hiddenPosts.clear()

                // Fetch from feed and filter
                val response = withContext(Dispatchers.IO) {
                    retrofitInstance.apiService.getAllFeed("1")
                }

                if (response.isSuccessful && response.body() != null) {
                    val allPosts = response.body()!!.data.data.posts

                    // Filter only the hidden posts
                    val foundHiddenPosts = allPosts.filter { post ->
                        hiddenPostIds.contains(post._id)
                    }

                    hiddenPosts.addAll(foundHiddenPosts)
                    Log.d(TAG, "Found ${foundHiddenPosts.size} hidden posts from page 1")

                    // If we need more pages to find all hidden posts
                    if (foundHiddenPosts.size < hiddenPostIds.size) {
                        // Try fetching more pages
                        for (page in 2..5) { // Check up to 5 pages
                            val pageResponse = withContext(Dispatchers.IO) {
                                retrofitInstance.apiService.getAllFeed(page.toString())
                            }

                            if (pageResponse.isSuccessful && pageResponse.body() != null) {
                                val pagePosts = pageResponse.body()!!.data.data.posts
                                val pageHiddenPosts = pagePosts.filter { post ->
                                    hiddenPostIds.contains(post._id) && !hiddenPosts.any { it._id == post._id }
                                }
                                hiddenPosts.addAll(pageHiddenPosts)
                                Log.d(TAG, "Found ${pageHiddenPosts.size} more hidden posts from page $page")

                                if (hiddenPosts.size >= hiddenPostIds.size) break
                            }
                        }
                    }
                }

                // Sort by hidden date (most recent first)
                hiddenPosts.sortByDescending { post ->
                    sharedPrefs.getLong("${post._id}_timestamp", 0L)
                }

                // Update adapter
                feedAdapter.submitItems(hiddenPosts.toMutableList())

                showEmptyState(hiddenPosts.isEmpty())
                showLoading(false)

                Log.d(TAG, "Successfully loaded ${hiddenPosts.size} hidden posts")

            } catch (e: Exception) {
                Log.e(TAG, "Error loading hidden posts: ${e.message}", e)
                Toast.makeText(
                    this@HiddenFeedPostsActivity,
                    "Failed to load hidden posts: ${e.message}",
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
        emptyStateTextView.visibility = View.GONE
    }

    private fun showEmptyState(show: Boolean) {
        emptyStateTextView.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    // ==================== OnFeedClickListener IMPLEMENTATION ====================

    override fun likeUnLikeFeed(position: Int, data: Post) {
        Toast.makeText(this, "Unhide the post to like it", Toast.LENGTH_SHORT).show()
    }

    override fun feedCommentClicked(position: Int, data: Post) {
        Toast.makeText(this, "Unhide the post to comment", Toast.LENGTH_SHORT).show()
    }

    override fun feedFavoriteClick(position: Int, data: Post) {
        Toast.makeText(this, "Unhide the post to bookmark", Toast.LENGTH_SHORT).show()
    }

    override fun moreOptionsClick(position: Int, data: Post) {
        showUnhideDialog(position, data)
    }

    override fun feedFileClicked(position: Int, data: Post) {
        Toast.makeText(this, "Unhide the post to view media", Toast.LENGTH_SHORT).show()
    }

    override fun feedRepostFileClicked(position: Int, data: OriginalPost) {
        Toast.makeText(this, "Unhide the post to view", Toast.LENGTH_SHORT).show()
    }

    override fun feedShareClicked(position: Int, data: Post) {
        Toast.makeText(this, "Unhide the post to share", Toast.LENGTH_SHORT).show()
    }

    override fun followButtonClicked(
        followUnFollowEntity: FollowUnFollowEntity,
        followButton: AppCompatButton
    ) {
        Toast.makeText(this, "Unhide the post to follow users", Toast.LENGTH_SHORT).show()
    }

    override fun feedRepostPost(position: Int, data: Post) {
        Toast.makeText(this, "Unhide the post to repost", Toast.LENGTH_SHORT).show()
    }

    override fun feedRepostPostClicked(position: Int, data: Post) {
        Toast.makeText(this, "Unhide the post to view repost", Toast.LENGTH_SHORT).show()
    }

    override fun feedClickedToOriginalPost(position: Int, originalPostId: String) {
        Toast.makeText(this, "Unhide the post to view original", Toast.LENGTH_SHORT).show()
    }

    override fun onImageClick() {
        Toast.makeText(this, "Unhide the post to view images", Toast.LENGTH_SHORT).show()
    }

    // ==================== UNHIDE FUNCTIONALITY ====================

    private fun showUnhideDialog(position: Int, data: Post) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Unhide Post")
            .setMessage("Do you want to unhide this post and show it in your feed again?")
            .setPositiveButton("Unhide") { dialog, _ ->
                handleUnhidePost(position, data)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun handleUnhidePost(position: Int, data: Post) {
        val postId = data._id

        Log.d(TAG, "Unhiding post: $postId at position: $position")

        // Remove from SharedPreferences
        with(sharedPrefs.edit()) {
            remove(postId)
            remove("${postId}_timestamp")
            apply()
        }

        // Remove from FeedAdapter cache
        FeedAdapter.removeFromHiddenPostsCache(postId)

        // Remove from list and adapter
        if (position >= 0 && position < hiddenPosts.size) {
            val removedPost = hiddenPosts.removeAt(position)
            feedAdapter.removeItem(position)
            feedAdapter.notifyItemRemoved(position)
            feedAdapter.notifyItemRangeChanged(position, hiddenPosts.size)

            Log.d(TAG, "Removed post from list. Remaining: ${hiddenPosts.size}")

            // Show snackbar with undo option
            Snackbar.make(
                recyclerView,
                "Post unhidden",
                Snackbar.LENGTH_LONG
            ).setAction("Undo") {
                // Re-hide the post
                with(sharedPrefs.edit()) {
                    putBoolean(postId, true)
                    putLong("${postId}_timestamp", System.currentTimeMillis())
                    apply()
                }
                FeedAdapter.addToHiddenPostsCache(postId)

                Log.d(TAG, "Undo: Re-hiding post $postId")

                // Add back to list
                hiddenPosts.add(position, removedPost)
                feedAdapter.submitItems(hiddenPosts.toMutableList())
                feedAdapter.notifyItemInserted(position)

                showEmptyState(false)

                Toast.makeText(this, "Post hidden again", Toast.LENGTH_SHORT).show()
            }.show()
        }

        // Check if list is now empty
        if (hiddenPosts.isEmpty()) {
            showEmptyState(true)
            Log.d(TAG, "No more hidden posts")
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: Reloading hidden posts")
        // Reload when returning to this screen
        loadHiddenPosts()
    }
}