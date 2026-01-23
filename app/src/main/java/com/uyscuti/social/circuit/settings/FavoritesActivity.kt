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
import com.uyscuti.social.circuit.adapter.RelationshipUsersAdapter
import com.uyscuti.social.network.api.response.profile.followingList.FavoriteItem
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FavoritesActivity : AppCompatActivity() {

    @Inject
    lateinit var retrofitInstance: RetrofitInstance

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyTextView: TextView
    private lateinit var adapter: RelationshipUsersAdapter<FavoriteItem>
    private val favoritesList = mutableListOf<FavoriteItem>()

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
        supportActionBar?.title = "Favorites"
        toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupRecyclerView() {
        adapter = RelationshipUsersAdapter(
            items = favoritesList,
            buttonText = "Remove",
            onButtonClick = { item -> showRemoveDialog(item) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadFavorites() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    retrofitInstance.apiService.getFavorites()
                }

                if (response.isSuccessful && response.body()?.success == true) {
                    favoritesList.clear()
                    favoritesList.addAll(response.body()?.data ?: emptyList())
                    adapter.notifyDataSetChanged()
                    showEmptyState(favoritesList.isEmpty())
                } else {
                    Toast.makeText(this@FavoritesActivity, "Failed to load", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.message}", e)
                Toast.makeText(this@FavoritesActivity, "Network error", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showRemoveDialog(item: FavoriteItem) {
        AlertDialog.Builder(this)
            .setTitle("Remove @${item.user?.username} from favorites?")
            .setPositiveButton("Remove") { dialog, _ ->
                removeFromFavorites(item)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun removeFromFavorites(item: FavoriteItem) {
        lifecycleScope.launch {
            try {
                val userId = item.user?._id ?: return@launch
                val response = withContext(Dispatchers.IO) {
                    retrofitInstance.apiService.removeFromFavorites(userId)
                }

                if (response.isSuccessful) {
                    val position = favoritesList.indexOf(item)
                    if (position != -1) {
                        favoritesList.removeAt(position)
                        adapter.notifyItemRemoved(position)
                        showEmptyState(favoritesList.isEmpty())

                        Snackbar.make(recyclerView, "Removed from favorites", Snackbar.LENGTH_LONG)
                            .setAction("Undo") { addBackToFavorites(item, position) }
                            .show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.message}", e)
            }
        }
    }

    private fun addBackToFavorites(item: FavoriteItem, position: Int) {
        lifecycleScope.launch {
            try {
                val userId = item.user?._id ?: return@launch
                val response = withContext(Dispatchers.IO) {
                    retrofitInstance.apiService.addToFavorites(userId)
                }

                if (response.isSuccessful) {
                    favoritesList.add(position, item)
                    adapter.notifyItemInserted(position)
                    showEmptyState(favoritesList.isEmpty())
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
        emptyTextView.text = "No favorites"
        recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
}