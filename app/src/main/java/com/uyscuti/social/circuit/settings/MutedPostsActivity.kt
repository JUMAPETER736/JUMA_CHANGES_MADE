package com.uyscuti.social.circuit.settings

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
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MutedPostsActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyTextView: TextView
    private lateinit var adapter: MutedUsersAdapter
    private val mutedUsersList = mutableListOf<UserRelationshipItem>()

    @Inject
    lateinit var localStorage: LocalStorage

    private lateinit var retrofitInstance: RetrofitInstance

    companion object {
        private const val TAG = "MutedPostsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_muted_posts)

        // Initialize RetrofitInstance with injected dependencies
        retrofitInstance = RetrofitInstance(localStorage, this)

        initializeViews()
        setupToolbar()
        setupRecyclerView()
        loadMutedUsers()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        emptyTextView = findViewById(R.id.emptyTextView)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Muted Posts"
            setDisplayHomeAsUpEnabled(true)
        }
        toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = MutedUsersAdapter(
            context = this,
            onActionClick = { userId, item -> showUnmuteDialog(userId, item) }
        )
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MutedPostsActivity)
            adapter = this@MutedPostsActivity.adapter
            setHasFixedSize(true)
        }
    }

    private fun loadMutedUsers() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    retrofitInstance.apiService.getMutedPostsUsers()
                }

                if (response.isSuccessful && response.body()?.success == true) {
                    val items = response.body()?.data?.mapNotNull { mutedPostsItem ->
                        // Only add items with valid user data
                        mutedPostsItem.user?.let { user ->
                            UserRelationshipItem(
                                userId = user._id ?: return@mapNotNull null,
                                username = user.username ?: return@mapNotNull null,
                                email = user.email,
                                firstName = user.firstName,
                                lastName = user.lastName,
                                avatar = user.avatar?.let {
                                    com.uyscuti.social.network.api.response.posts.Avatar(
                                        _id = it._id,
                                        localPath = it.localPath,
                                        url = it.url
                                    )
                                },
                                actionDate = mutedPostsItem.mutedAt ?: ""
                            )
                        }
                    } ?: emptyList()

                    mutedUsersList.clear()
                    mutedUsersList.addAll(items)
                    adapter.updateList(mutedUsersList)
                    showEmptyState(mutedUsersList.isEmpty())
                } else {
                    val errorMessage = response.body()?.message ?: "Failed to load muted users"
                    showError(errorMessage)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading muted users: ${e.message}", e)
                showError("Network error. Please check your connection.")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showUnmuteDialog(userId: String, item: UserRelationshipItem) {
        AlertDialog.Builder(this)
            .setTitle("Unmute posts from @${item.username}?")
            .setMessage("You'll start seeing posts from this user again.")
            .setPositiveButton("Unmute") { dialog, _ ->
                unmuteUser(userId, item)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .setCancelable(true)
            .show()
    }

    private fun unmuteUser(userId: String, item: UserRelationshipItem) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    retrofitInstance.apiService.unMutePosts(userId)
                }

                if (response.isSuccessful) {
                    val position = mutedUsersList.indexOf(item)
                    if (position != -1) {
                        mutedUsersList.removeAt(position)
                        adapter.removeItem(position)
                        showEmptyState(mutedUsersList.isEmpty())

                        Snackbar.make(
                            recyclerView,
                            "@${item.username} posts unmuted",
                            Snackbar.LENGTH_LONG
                        ).setAction("Undo") {
                            remuteUser(userId, item, position)
                        }.show()
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

    private fun remuteUser(userId: String, item: UserRelationshipItem, position: Int) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    retrofitInstance.apiService.mutePosts(userId)
                }

                if (response.isSuccessful) {
                    // Insert at the original position
                    val insertPosition = position.coerceAtMost(mutedUsersList.size)
                    mutedUsersList.add(insertPosition, item)
                    adapter.updateList(mutedUsersList)
                    showEmptyState(mutedUsersList.isEmpty())

                    Toast.makeText(
                        this@MutedPostsActivity,
                        "@${item.username} posts muted again",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    showError("Failed to mute posts again")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error muting posts again: ${e.message}", e)
                showError("Failed to undo. Please try again.")
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
        emptyTextView.visibility = View.GONE
    }

    private fun showEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            emptyTextView.visibility = View.VISIBLE
            emptyTextView.text = "No muted posts"
            recyclerView.visibility = View.GONE
        } else {
            emptyTextView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}