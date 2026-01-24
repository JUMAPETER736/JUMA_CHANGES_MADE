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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CloseFriendsActivity : AppCompatActivity() {

    private val retrofitInstance: RetrofitInstance by lazy {
        RetrofitInstance(LocalStorage(this), this)
    }

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyTextView: TextView
    private lateinit var adapter: RelationshipUsersAdapter
    private val closeFriendsList = mutableListOf<UserRelationshipItem>()

    companion object {
        private const val TAG = "CloseFriendsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_close_friends)  // Changed from activity_muted_posts

        initializeViews()
        setupToolbar()
        setupRecyclerView()
        loadCloseFriends()
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
            title = "Close Friends"
            setDisplayHomeAsUpEnabled(true)
        }
        toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = RelationshipUsersAdapter(
            context = this,
            relationshipType = RelationshipUsersAdapter.RelationshipType.CLOSE_FRIENDS,
            onActionClick = { userId, item -> showRemoveDialog(userId, item) }
        )
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@CloseFriendsActivity)
            adapter = this@CloseFriendsActivity.adapter
            setHasFixedSize(true)
        }
    }

    private fun loadCloseFriends() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    retrofitInstance.apiService.getCloseFriends()
                }

                if (response.isSuccessful && response.body()?.success == true) {
                    val items = response.body()?.data?.mapNotNull { closeFriendItem ->
                        // Only add items with valid user data
                        closeFriendItem.user?.let { user ->
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
                                actionDate = closeFriendItem.addedAt ?: ""
                            )
                        }
                    } ?: emptyList()

                    closeFriendsList.clear()
                    closeFriendsList.addAll(items)
                    adapter.updateList(closeFriendsList)
                    showEmptyState(closeFriendsList.isEmpty())
                } else {
                    val errorMessage = response.body()?.message ?: "Failed to load close friends"
                    showError(errorMessage)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading close friends: ${e.message}", e)
                showError("Network error. Please check your connection.")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showRemoveDialog(userId: String, item: UserRelationshipItem) {
        AlertDialog.Builder(this)
            .setTitle("Remove @${item.username} from close friends?")
            .setMessage("They won't be notified that you removed them from your close friends list.")
            .setPositiveButton("Remove") { dialog, _ ->
                removeFromCloseFriends(userId, item)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .setCancelable(true)
            .show()
    }

    private fun removeFromCloseFriends(userId: String, item: UserRelationshipItem) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    retrofitInstance.apiService.removeFromCloseFriends(userId)
                }

                if (response.isSuccessful) {
                    val position = closeFriendsList.indexOf(item)
                    if (position != -1) {
                        closeFriendsList.removeAt(position)
                        adapter.removeItem(position)
                        showEmptyState(closeFriendsList.isEmpty())

                        Snackbar.make(
                            recyclerView,
                            "@${item.username} removed from close friends",
                            Snackbar.LENGTH_LONG
                        ).setAction("Undo") {
                            addBackToCloseFriends(userId, item, position)
                        }.show()
                    }
                } else {
                    showError("Failed to remove from close friends")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error removing from close friends: ${e.message}", e)
                showError("Failed to remove. Please try again.")
            }
        }
    }

    private fun addBackToCloseFriends(userId: String, item: UserRelationshipItem, position: Int) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    retrofitInstance.apiService.addToCloseFriends(userId)
                }

                if (response.isSuccessful) {
                    // Insert at the original position
                    val insertPosition = position.coerceAtMost(closeFriendsList.size)
                    closeFriendsList.add(insertPosition, item)
                    adapter.updateList(closeFriendsList)
                    showEmptyState(closeFriendsList.isEmpty())

                    Toast.makeText(
                        this@CloseFriendsActivity,
                        "@${item.username} added back to close friends",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    showError("Failed to add back to close friends")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error adding back to close friends: ${e.message}", e)
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
            emptyTextView.text = "No close friends yet"
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