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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MutedStoriesActivity : AppCompatActivity() {

    @Inject
    lateinit var retrofitInstance: RetrofitInstance

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyTextView: TextView
    private lateinit var adapter: RelationshipUsersAdapter
    private val mutedStoriesList = mutableListOf<UserRelationshipItem>()

    companion object {
        private const val TAG = "MutedStoriesActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_muted_posts)

        initializeViews()
        setupToolbar()
        setupRecyclerView()
        loadMutedStories()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        emptyTextView = findViewById(R.id.emptyTextView)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Muted Stories"
        toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupRecyclerView() {
        adapter = RelationshipUsersAdapter(
            context = this,
            relationshipType = RelationshipUsersAdapter.RelationshipType.MUTED_STORIES,
            onActionClick = { userId, item -> showUnmuteDialog(userId, item) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadMutedStories() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    retrofitInstance.apiService.getMutedStoriesUsers()
                }

                if (response.isSuccessful && response.body()?.success == true) {
                    val items = response.body()?.data?.map { mutedStoriesItem ->
                        UserRelationshipItem(
                            userId = mutedStoriesItem.user?._id ?: "",
                            username = mutedStoriesItem.user?.username ?: "",
                            email = mutedStoriesItem.user?.email,
                            firstName = mutedStoriesItem.user?.firstName,
                            lastName = mutedStoriesItem.user?.lastName,
                            avatar = mutedStoriesItem.user?.avatar?.let {
                                com.uyscuti.social.network.api.response.posts.Avatar(
                                    _id = it._id,
                                    localPath = it.localPath,
                                    url = it.url
                                )
                            },
                            actionDate = mutedStoriesItem.mutedAt
                        )
                    } ?: emptyList()

                    mutedStoriesList.clear()
                    mutedStoriesList.addAll(items)
                    adapter.updateList(mutedStoriesList)
                    showEmptyState(mutedStoriesList.isEmpty())
                } else {
                    Toast.makeText(this@MutedStoriesActivity, "Failed to load", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.message}", e)
                Toast.makeText(this@MutedStoriesActivity, "Network error", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showUnmuteDialog(userId: String, item: UserRelationshipItem) {
        AlertDialog.Builder(this)
            .setTitle("Unmute stories from @${item.username}?")
            .setMessage("You'll start seeing stories from this user again.")
            .setPositiveButton("Unmute") { dialog, _ ->
                unmuteStories(userId, item)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun unmuteStories(userId: String, item: UserRelationshipItem) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    retrofitInstance.apiService.unMuteStories(userId)
                }

                if (response.isSuccessful) {
                    val position = mutedStoriesList.indexOf(item)
                    if (position != -1) {
                        mutedStoriesList.removeAt(position)
                        adapter.removeItem(position)
                        showEmptyState(mutedStoriesList.isEmpty())

                        Snackbar.make(recyclerView, "Unmuted stories", Snackbar.LENGTH_LONG)
                            .setAction("Undo") { remuteStories(userId, item, position) }
                            .show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.message}", e)
            }
        }
    }

    private fun remuteStories(userId: String, item: UserRelationshipItem, position: Int) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    retrofitInstance.apiService.muteStories(userId)
                }

                if (response.isSuccessful) {
                    mutedStoriesList.add(position, item)
                    adapter.updateList(mutedStoriesList)
                    showEmptyState(mutedStoriesList.isEmpty())
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
        emptyTextView.text = "No muted stories"
        recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
}