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

class FavoritesActivity : AppCompatActivity() {

    private val retrofitInstance: RetrofitInstance by lazy {
        RetrofitInstance(LocalStorage(this), this)
    }

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyTextView: TextView
    private lateinit var adapter: RelationshipUsersAdapter
    private val favoritesList = mutableListOf<UserRelationshipItem>()

    companion object {
        private const val TAG = "FavoritesActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_muted_posts)

        initializeViews()
        setupToolbar()
        setupRecyclerView()
        loadFavorites()
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
            title = "Favorites"
            setDisplayHomeAsUpEnabled(true)
        }
        toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = RelationshipUsersAdapter(
            context = this,
            relationshipType = RelationshipUsersAdapter.RelationshipType.FAVORITES,
            onActionClick = { userId, item -> showRemoveDialog(userId, item) }
        )
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@FavoritesActivity)
            adapter = this@FavoritesActivity.adapter
            setHasFixedSize(true)
        }
    }

    private fun loadFavorites() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    retrofitInstance.apiService.getFavorites()
                }

                if (response.isSuccessful && response.body()?.success == true) {
                    val items = response.body()?.data?.mapNotNull { favoriteItem ->
                        // Only add items with valid user data
                        favoriteItem.user?.let { user ->
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
                                actionDate = favoriteItem.addedAt ?: ""
                            )
                        }
                    } ?: emptyList()

                    favoritesList.clear()
                    favoritesList.addAll(items)
                    adapter.updateList(favoritesList)
                    showEmptyState(favoritesList.isEmpty())
                } else {
                    val errorMessage = response.body()?.message ?: "Failed to load favorites"
                    showError(errorMessage)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading favorites: ${e.message}", e)
                showError("Network error. Please check your connection.")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showRemoveDialog(userId: String, item: UserRelationshipItem) {
        AlertDialog.Builder(this)
            .setTitle("Remove @${item.username} from favorites?")
            .setMessage("Posts from this account will no longer appear higher in your feed.")
            .setPositiveButton("Remove") { dialog, _ ->
                removeFromFavorites(userId, item)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .setCancelable(true)
            .show()
    }

    private fun removeFromFavorites(userId: String, item: UserRelationshipItem) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    retrofitInstance.apiService.removeFromFavorites(userId)
                }

                if (response.isSuccessful) {
                    val position = favoritesList.indexOf(item)
                    if (position != -1) {
                        favoritesList.removeAt(position)
                        adapter.removeItem(position)
                        showEmptyState(favoritesList.isEmpty())

                        Snackbar.make(
                            recyclerView,
                            "@${item.username} removed from favorites",
                            Snackbar.LENGTH_LONG
                        ).setAction("Undo") {
                            addBackToFavorites(userId, item, position)
                        }.show()
                    }
                } else {
                    showError("Failed to remove from favorites")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error removing from favorites: ${e.message}", e)
                showError("Failed to remove. Please try again.")
            }
        }
    }

    private fun addBackToFavorites(userId: String, item: UserRelationshipItem, position: Int) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    retrofitInstance.apiService.addToFavorites(userId)
                }

                if (response.isSuccessful) {
                    // Insert at the original position
                    val insertPosition = position.coerceAtMost(favoritesList.size)
                    favoritesList.add(insertPosition, item)
                    adapter.updateList(favoritesList)
                    showEmptyState(favoritesList.isEmpty())

                    Toast.makeText(
                        this@FavoritesActivity,
                        "@${item.username} added back to favorites",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    showError("Failed to add back to favorites")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error adding back to favorites: ${e.message}", e)
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
            emptyTextView.text = "No favorites yet"
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