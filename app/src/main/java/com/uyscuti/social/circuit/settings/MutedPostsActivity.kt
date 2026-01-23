package com.uyscuti.social.circuit.settings

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.circuit.R
import com.uyscuti.social.network.api.response.profile.followingList.MutedPostsItem
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance

class MutedPostsActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyTextView: TextView
    private lateinit var adapter: MutedUsersAdapter
    private val mutedUsersList = mutableListOf<MutedPostsItem>()
    private val retrofitInstance = RetrofitInstance()

    companion object {
        private const val TAG = "MutedPostsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_muted_posts)

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
        supportActionBar?.title = "Muted Posts"
        toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        adapter = MutedUsersAdapter(mutedUsersList, "Unmute") { mutedUser ->
            showUnmuteDialog(mutedUser)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadMutedUsers() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    retrofitInstance.apiService.getMutedPostsUsers()
                }

                if (response.isSuccessful && response.body()?.success == true) {
                    mutedUsersList.clear()
                    mutedUsersList.addAll(response.body()?.data ?: emptyList())
                    adapter.notifyDataSetChanged()
                    showEmptyState(mutedUsersList.isEmpty())
                } else {
                    Toast.makeText(this@MutedPostsActivity, "Failed to load muted users", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.message}", e)
                Toast.makeText(this@MutedPostsActivity, "Network error", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showUnmuteDialog(mutedUser: MutedPostsItem) {
        AlertDialog.Builder(this)
            .setTitle("Unmute posts from @${mutedUser.user?.username}?")
            .setMessage("You'll start seeing posts from this user again.")
            .setPositiveButton("Unmute") { dialog, _ ->
                unmuteUser(mutedUser)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun unmuteUser(mutedUser: MutedPostsItem) {
        lifecycleScope.launch {
            try {
                val userId = mutedUser.user?._id ?: return@launch
                val response = withContext(Dispatchers.IO) {
                    retrofitInstance.apiService.unMutePosts(userId)
                }

                if (response.isSuccessful) {
                    val position = mutedUsersList.indexOf(mutedUser)
                    if (position != -1) {
                        mutedUsersList.removeAt(position)
                        adapter.notifyItemRemoved(position)
                        showEmptyState(mutedUsersList.isEmpty())

                        Snackbar.make(recyclerView, "Unmuted posts", Snackbar.LENGTH_LONG)
                            .setAction("Undo") { remuteUser(mutedUser, position) }
                            .show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.message}", e)
            }
        }
    }

    private fun remuteUser(mutedUser: MutedPostsItem, position: Int) {
        lifecycleScope.launch {
            try {
                val userId = mutedUser.user?._id ?: return@launch
                val response = withContext(Dispatchers.IO) {
                    retrofitInstance.apiService.mutePosts(userId)
                }

                if (response.isSuccessful) {
                    mutedUsersList.add(position, mutedUser)
                    adapter.notifyItemInserted(position)
                    showEmptyState(mutedUsersList.isEmpty())
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
        emptyTextView.text = "No muted posts"
        recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
}