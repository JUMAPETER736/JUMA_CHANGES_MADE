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
class BlockedUsersActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyTextView: TextView
    private lateinit var adapter: RelationshipUsersAdapter
    private val blockedUsersList = mutableListOf<UserRelationshipItem>()

    @Inject
    lateinit var localStorage: LocalStorage

    private lateinit var retrofitInstance: RetrofitInstance

    companion object {
        private const val TAG = "BlockedUsersActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blocked_users)

        // Initialize RetrofitInstance with injected dependencies
        retrofitInstance = RetrofitInstance(localStorage, this)

        initializeViews()
        setupToolbar()
        setupRecyclerView()
        loadBlockedUsers()
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
            title = "Blocked Users"
            setDisplayHomeAsUpEnabled(true)
        }
        toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = RelationshipUsersAdapter(
            context = this,
            relationshipType = RelationshipUsersAdapter.RelationshipType.BLOCKED,
            onActionClick = { userId, item -> showUnblockDialog(userId, item) }
        )
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@BlockedUsersActivity)
            adapter = this@BlockedUsersActivity.adapter
            setHasFixedSize(true)
        }
    }

    private fun loadBlockedUsers() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    retrofitInstance.apiService.getAllBlockedUsers(page = 1, limit = 100)
                }

                if (response.isSuccessful) {
                    val blockedUsersResponse = response.body()
                    if (blockedUsersResponse != null && blockedUsersResponse.success) {
                        val items = blockedUsersResponse.data.blockedUsers.mapNotNull { blockedItem ->
                            // Only add items with valid user data
                            blockedItem.user?.let { user ->
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
                                    actionDate = blockedItem.blockedAt ?: ""
                                )
                            }
                        }

                        blockedUsersList.clear()
                        blockedUsersList.addAll(items)
                        adapter.updateList(blockedUsersList)
                        showEmptyState(blockedUsersList.isEmpty())
                    } else {
                        showError("Failed to load blocked users")
                    }
                } else {
                    showError("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading blocked users: ${e.message}", e)
                showError("Network error. Please check your connection.")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showUnblockDialog(userId: String, item: UserRelationshipItem) {
        AlertDialog.Builder(this)
            .setTitle("Unblock @${item.username}?")
            .setMessage("You'll be able to see and contact @${item.username} again.")
            .setPositiveButton("Unblock") { dialog, _ ->
                unblockUser(userId, item)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .setCancelable(true)
            .show()
    }

    private fun unblockUser(userId: String, item: UserRelationshipItem) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    retrofitInstance.apiService.unBlockUser(userId)
                }

                if (response.isSuccessful) {
                    val position = blockedUsersList.indexOf(item)
                    if (position != -1) {
                        blockedUsersList.removeAt(position)
                        adapter.removeItem(position)
                        showEmptyState(blockedUsersList.isEmpty())

                        Snackbar.make(
                            recyclerView,
                            "@${item.username} unblocked",
                            Snackbar.LENGTH_LONG
                        ).setAction("Undo") {
                            reblockUser(userId, item, position)
                        }.show()
                    }
                } else {
                    showError("Failed to unblock user")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error unblocking user: ${e.message}", e)
                showError("Failed to unblock. Please try again.")
            }
        }
    }

    private fun reblockUser(userId: String, item: UserRelationshipItem, position: Int) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    retrofitInstance.apiService.blockUser(userId)
                }

                if (response.isSuccessful) {
                    // Insert at the original position
                    val insertPosition = position.coerceAtMost(blockedUsersList.size)
                    blockedUsersList.add(insertPosition, item)
                    adapter.updateList(blockedUsersList)
                    showEmptyState(blockedUsersList.isEmpty())

                    Toast.makeText(
                        this@BlockedUsersActivity,
                        "@${item.username} blocked again",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    showError("Failed to block user again")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error re-blocking user: ${e.message}", e)
                showError("Failed to undo. Please try again.")
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE

        if (show) {
            // When loading, hide both recyclerView and emptyTextView
            recyclerView.visibility = View.GONE
            emptyTextView.visibility = View.GONE
        }

    }

    private fun showEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            emptyTextView.visibility = View.VISIBLE
            emptyTextView.text = "No blocked users"
            recyclerView.visibility = View.GONE
            progressBar.visibility = View.GONE  // Make sure progress bar is hidden
        } else {
            emptyTextView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            progressBar.visibility = View.GONE  // Make sure progress bar is hidden
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