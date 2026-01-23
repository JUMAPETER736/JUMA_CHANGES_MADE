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
import com.uyscuti.social.network.api.response.profile.followingList.RestrictedItem
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RestrictedAccountsActivity : AppCompatActivity() {

    @Inject
    lateinit var retrofitInstance: RetrofitInstance

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyTextView: TextView
    private lateinit var adapter: RelationshipUsersAdapter<RestrictedItem>
    private val restrictedList = mutableListOf<RestrictedItem>()

    companion object {
        private const val TAG = "RestrictedAccountsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_muted_posts)

        initializeViews()
        setupToolbar()
        setupRecyclerView()
        loadRestrictedAccounts()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        emptyTextView = findViewById(R.id.emptyTextView)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Restricted Accounts"
        toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupRecyclerView() {
        adapter = RelationshipUsersAdapter(
            items = restrictedList,
            buttonText = "Unrestrict",
            onButtonClick = { item -> showUnrestrictDialog(item) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadRestrictedAccounts() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    retrofitInstance.apiService.getRestrictedUsers()
                }

                if (response.isSuccessful && response.body()?.success == true) {
                    restrictedList.clear()
                    restrictedList.addAll(response.body()?.data ?: emptyList())
                    adapter.notifyDataSetChanged()
                    showEmptyState(restrictedList.isEmpty())
                } else {
                    Toast.makeText(this@RestrictedAccountsActivity, "Failed to load", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.message}", e)
                Toast.makeText(this@RestrictedAccountsActivity, "Network error", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showUnrestrictDialog(item: RestrictedItem) {
        AlertDialog.Builder(this)
            .setTitle("Unrestrict @${item.user?.username}?")
            .setMessage("This account will be able to see when you're active and when you've read their messages.")
            .setPositiveButton("Unrestrict") { dialog, _ ->
                unrestrictUser(item)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun unrestrictUser(item: RestrictedItem) {
        lifecycleScope.launch {
            try {
                val userId = item.user?._id ?: return@launch
                val response = withContext(Dispatchers.IO) {
                    retrofitInstance.apiService.unRestrictUser(userId)
                }

                if (response.isSuccessful) {
                    val position = restrictedList.indexOf(item)
                    if (position != -1) {
                        restrictedList.removeAt(position)
                        adapter.notifyItemRemoved(position)
                        showEmptyState(restrictedList.isEmpty())

                        Snackbar.make(recyclerView, "Unrestricted", Snackbar.LENGTH_LONG)
                            .setAction("Undo") { restrictUserAgain(item, position) }
                            .show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.message}", e)
            }
        }
    }

    private fun restrictUserAgain(item: RestrictedItem, position: Int) {
        lifecycleScope.launch {
            try {
                val userId = item.user?._id ?: return@launch
                val response = withContext(Dispatchers.IO) {
                    retrofitInstance.apiService.restrictUser(userId)
                }

                if (response.isSuccessful) {
                    restrictedList.add(position, item)
                    adapter.notifyItemInserted(position)
                    showEmptyState(restrictedList.isEmpty())
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
        emptyTextView.text = "No restricted accounts"
        recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
}