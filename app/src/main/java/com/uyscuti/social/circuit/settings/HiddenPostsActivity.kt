package com.uyscuti.social.circuit.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.uyscuti.social.circuit.R
import com.uyscuti.social.network.api.response.posts.Post
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HiddenPostsActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateText: TextView
    private lateinit var adapter: HiddenPostsAdapter

    private val retrofitInstance = RetrofitInstance
    private val hiddenPosts = mutableListOf<HiddenPostItem>()
    private lateinit var sharedPrefs: SharedPreferences

    companion object {
        private const val TAG = "HiddenPostsActivity"
        private const val PREFS_NAME = "HiddenPosts"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_relationship_users)

        sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        initViews()
        setupToolbar()
        setupRecyclerView()
        loadHiddenPosts()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        emptyStateText = findViewById(R.id.emptyStateText)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Hidden Posts"
            setDisplayHomeAsUpEnabled(true)
        }
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        adapter = HiddenPostsAdapter(
            context = this,
            onUnhideClick = { postId, position ->
                showUnhideConfirmation(postId, position)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadHiddenPosts() {
        lifecycleScope.launch {
            try {
                showLoading(true)

                // Get all hidden post IDs from SharedPreferences
                val hiddenPostIds = sharedPrefs.all.keys.filter { key ->
                    sharedPrefs.getBoolean(key, false)
                }

                if (hiddenPostIds.isEmpty()) {
                    hiddenPosts.clear()
                    adapter.updateList(hiddenPosts)
                    updateEmptyState()
                    showLoading(false)
                    return@launch
                }

                // Fetch post details for each hidden post
                val posts = mutableListOf<HiddenPostItem>()

                withContext(Dispatchers.IO) {
                    hiddenPostIds.forEach { postId ->
                        try {
                            // Fetch individual post details
                            val response = retrofitInstance.apiService.getPostById(postId)

                            if (response.isSuccessful && response.body() != null) {
                                val post = response.body()!!.data

                                posts.add(HiddenPostItem(
                                    postId = post._id,
                                    content = post.content ?: "Post content unavailable",
                                    hiddenAt = "Recently", // Since we don't store timestamp
                                    authorUsername = post.author?.account?.username ?: "Unknown",
                                    authorAvatar = post.author?.account?.avatar
                                ))
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error fetching post $postId: ${e.message}")
                        }
                    }
                }

                hiddenPosts.clear()
                hiddenPosts.addAll(posts)
                adapter.updateList(hiddenPosts)
                updateEmptyState()

            } catch (e: Exception) {
                Log.e(TAG, "Error loading hidden posts: ${e.message}", e)
                Toast.makeText(
                    this@HiddenPostsActivity,
                    "Error loading hidden posts",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showUnhideConfirmation(postId: String, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Unhide Post")
            .setMessage("This post will reappear in your feed.")
            .setPositiveButton("Unhide") { dialog, _ ->
                unhidePost(postId, position)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun unhidePost(postId: String, position: Int) {
        try {
            // Store the removed item for undo
            val removedPost = hiddenPosts[position]

            // Remove from SharedPreferences
            with(sharedPrefs.edit()) {
                remove(postId)
                apply()
            }

            // Remove from list
            adapter.removeItem(position)
            hiddenPosts.removeAt(position)
            updateEmptyState()

            // Show Snackbar with Undo
            Snackbar.make(
                recyclerView,
                "Post unhidden",
                Snackbar.LENGTH_LONG
            ).setAction("Undo") {
                // Re-hide the post
                with(sharedPrefs.edit()) {
                    putBoolean(postId, true)
                    apply()
                }

                // Re-add to list
                hiddenPosts.add(position, removedPost)
                adapter.updateList(hiddenPosts)
                updateEmptyState()

                Toast.makeText(
                    this@HiddenPostsActivity,
                    "Post hidden again",
                    Toast.LENGTH_SHORT
                ).show()
            }.show()

        } catch (e: Exception) {
            Log.e(TAG, "Error unhiding post: ${e.message}", e)
            Toast.makeText(
                this@HiddenPostsActivity,
                "Failed to unhide post",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun updateEmptyState() {
        if (hiddenPosts.isEmpty()) {
            emptyStateText.visibility = View.VISIBLE
            emptyStateText.text = "No hidden posts"
            recyclerView.visibility = View.GONE
        } else {
            emptyStateText.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
}

// Hidden Post Item Data Class
data class HiddenPostItem(
    val postId: String,
    val content: String,
    val hiddenAt: String,
    val authorUsername: String,
    val authorAvatar: com.uyscuti.social.network.api.response.posts.Avatar?
)