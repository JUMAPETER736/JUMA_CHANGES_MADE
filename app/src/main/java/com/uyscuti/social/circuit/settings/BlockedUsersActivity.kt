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
import com.uyscuti.social.network.api.response.follow_unfollow.BlockedUserItem
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
    private lateinit var adapter: BlockedUsersAdapter
    private val blockedUsersList = mutableListOf<BlockedUserItem>()

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
        supportActionBar?.title = "Blocked Users"
        toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        adapter = BlockedUsersAdapter(blockedUsersList) { blockedUser ->
            showUnblockDialog(blockedUser)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
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
                        blockedUsersList.clear()
                        blockedUsersList.addAll(blockedUsersResponse.data.blockedUsers)
                        adapter.notifyDataSetChanged()

                        showEmptyState(blockedUsersList.isEmpty())
                    } else {
                        Toast.makeText(
                            this@BlockedUsersActivity,
                            "Failed to load blocked users",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@BlockedUsersActivity,
                        "Error: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading blocked users: ${e.message}", e)
                Toast.makeText(
                    this@BlockedUsersActivity,
                    "Network error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showUnblockDialog(blockedUser: BlockedUserItem) {
        val username = blockedUser.user.username

        AlertDialog.Builder(this)
            .setTitle("Unblock @$username?")
            .setMessage("You'll be able to see and contact @$username again.")
            .setPositiveButton("Unblock") { dialog, _ ->
                unblockUser(blockedUser)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun unblockUser(blockedUser: BlockedUserItem) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    retrofitInstance.apiService.unBlockUser(blockedUser.user._id)
                }

                if (response.isSuccessful) {
                    val position = blockedUsersList.indexOf(blockedUser)
                    if (position != -1) {
                        blockedUsersList.removeAt(position)
                        adapter.notifyItemRemoved(position)
                        showEmptyState(blockedUsersList.isEmpty())

                        Snackbar.make(
                            recyclerView,
                            "Unblocked @${blockedUser.user.username}",
                            Snackbar.LENGTH_LONG
                        ).setAction("Undo") {
                            reblockUser(blockedUser, position)
                        }.show()
                    }
                } else {
                    Toast.makeText(
                        this@BlockedUsersActivity,
                        "Failed to unblock user",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error unblocking user: ${e.message}", e)
                Toast.makeText(
                    this@BlockedUsersActivity,
                    "Network error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun reblockUser(blockedUser: BlockedUserItem, position: Int) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    retrofitInstance.apiService.blockUser(blockedUser.user._id)
                }

                if (response.isSuccessful) {
                    blockedUsersList.add(position, blockedUser)
                    adapter.notifyItemInserted(position)
                    showEmptyState(blockedUsersList.isEmpty())
                    Toast.makeText(
                        this@BlockedUsersActivity,
                        "Blocked again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error re-blocking user: ${e.message}", e)
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showEmptyState(isEmpty: Boolean) {
        emptyTextView.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
}