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
import javax.inject.Inject

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
        setContentView(R.layout.activity_muted_posts)

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
        supportActionBar?.title = "Close Friends"
        toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupRecyclerView() {
        adapter = RelationshipUsersAdapter(
            context = this,
            relationshipType = RelationshipUsersAdapter.RelationshipType.CLOSE_FRIENDS,
            onActionClick = { userId, item -> showRemoveDialog(userId, item) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadCloseFriends() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    retrofitInstance.apiService.getCloseFriends()
                }

                if (response.isSuccessful && response.body()?.success == true) {
                    val items = response.body()?.data?.map { closeFriendItem ->
                        UserRelationshipItem(
                            userId = closeFriendItem.user?._id ?: "",
                            username = closeFriendItem.user?.username ?: "",
                            email = closeFriendItem.user?.email,
                            firstName = closeFriendItem.user?.firstName,
                            lastName = closeFriendItem.user?.lastName,
                            avatar = closeFriendItem.user?.avatar?.let {
                                com.uyscuti.social.network.api.response.posts.Avatar(
                                    _id = it._id,
                                    localPath = it.localPath,
                                    url = it.url
                                )
                            },
                            actionDate = closeFriendItem.addedAt
                        )
                    } ?: emptyList()

                    closeFriendsList.clear()
                    closeFriendsList.addAll(items)
                    adapter.updateList(closeFriendsList)
                    showEmptyState(closeFriendsList.isEmpty())
                } else {
                    Toast.makeText(this@CloseFriendsActivity, "Failed to load", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.message}", e)
                Toast.makeText(this@CloseFriendsActivity, "Network error", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showRemoveDialog(userId: String, item: UserRelationshipItem) {
        AlertDialog.Builder(this)
            .setTitle("Remove @${item.username} from close friends?")
            .setMessage("They won't be notified.")
            .setPositiveButton("Remove") { dialog, _ ->
                removeFromCloseFriends(userId, item)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
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

                        Snackbar.make(recyclerView, "Removed from close friends", Snackbar.LENGTH_LONG)
                            .setAction("Undo") { addBackToCloseFriends(userId, item, position) }
                            .show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.message}", e)
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
                    closeFriendsList.add(position, item)
                    adapter.updateList(closeFriendsList)
                    showEmptyState(closeFriendsList.isEmpty())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.message}", e)
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showEmptyState(isEmpty: Boolean) {
        emptyTextView.visibility = if (isEmpty) View.VISIBLE else View.GONE
        emptyTextView.text = "No close friends"
        recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
}