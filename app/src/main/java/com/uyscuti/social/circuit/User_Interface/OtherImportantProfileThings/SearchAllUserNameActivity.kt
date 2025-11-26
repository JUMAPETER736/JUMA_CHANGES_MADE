package com.uyscuti.social.circuit.User_Interface.OtherImportantProfileThings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.uyscuti.social.circuit.MainActivity
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.ActivitySearchAllUserNameBinding
import com.uyscuti.social.network.api.retrofit.interfaces.IFlashapi
import com.uyscuti.social.circuit.model.ShortsViewModel
import com.uyscuti.social.circuit.presentation.RecentUserViewModel
import com.uyscuti.social.core.common.data.room.entity.RecentUser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.util.Date


@AndroidEntryPoint
class SearchAllUserNameActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchAllUserNameBinding
    private lateinit var searchAdapter: SearchUserNameAdapter
    private lateinit var apiService: IFlashapi

    private val shortsViewModel: ShortsViewModel by viewModels()
    private val recentUserViewModel: RecentUserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySearchAllUserNameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupToolbar()
        initSearchResults()
        setupSearch()
        loadRecentUsers()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun loadRecentUsers() {
        lifecycleScope.launch {
            val recentUsers = withContext(Dispatchers.IO) {
                recentUserViewModel.getRecentUsers()
            }
            searchAdapter.showRecentUsers(recentUsers.map { it.toUserResult() })
        }
    }

    // Inside SearchAllUserNameActivity.kt

    private fun UserResult.toRecentUser() = RecentUser(
        id = userId,                 
        name = username,
        avatar = avatarUrl,
        lastSeen = Date(),
        online = false,
        dateAdded = Date()
    )

    private fun RecentUser.toUserResult() = UserResult(
        userId = id,
        username = name,
        avatarUrl = avatar,
        firstVideoId = "",
        firstVideoUrl = "",
        firstVideoThumbnail = ""
    )

    private fun initSearchResults() {
        searchAdapter = SearchUserNameAdapter { user ->
            Log.d("SearchResults", "User clicked: @${user.username} (${user.userId})")
            addUserToRecent(user.toRecentUser())
            onUserClicked(user)
        }

        binding.searchResultsRecyclerView.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@SearchAllUserNameActivity)
            adapter = searchAdapter
        }
    }



    private fun addUserToRecent(user: RecentUser) {
        lifecycleScope.launch(Dispatchers.IO) {
            recentUserViewModel.addRecentUser(user)
        }
    }

    private fun setupSearch() {
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(binding.searchEditText.text.toString().trim())
                hideKeyboard()
                binding.searchEditText.clearFocus()
                true
            } else false
        }

        binding.searchEditText.addTextChangedListener(afterTextChanged = { editable ->
            val query = editable.toString().trim()
            Log.d("FinishedTyping", "FinishedTyping: $query")

            if (query.isEmpty()) {
                loadRecentUsers()
            } else {
                searchAdapter.showLoading()
                performSearch(query)
            }
        })
    }

    private fun performSearch(query: String) {
        lifecycleScope.launch {
            val results = searchUsers(query).sortedBy { it.username }
            if (results.isNotEmpty()) {
                searchAdapter.showSearchResults(results)
            } else {
                searchAdapter.showNoResults()
            }
        }
    }

    private suspend fun searchUsers(query: String): List<UserResult> = withContext(Dispatchers.IO) {
        val users = mutableListOf<UserResult>()
        try {
            val response = apiService.searchUsers(query)
            if (response.isSuccessful) {
                val apiUsers = response.body()?.data as? List<com.uyscuti.social.network.api.models.User> ?: emptyList()
                apiUsers.forEach { apiUser ->
                    users.add(
                        UserResult(
                            userId = apiUser._id,
                            username = apiUser.username,
                            avatarUrl = apiUser.avatar.url,
                            firstVideoId = "",
                            firstVideoUrl = "",
                            firstVideoThumbnail = ""
                        )
                    )
                }
            }
        } catch (e: HttpException) {
            Log.e("SearchUsers", "HTTP error: ${e.message}")
        } catch (e: IOException) {
            Log.e("SearchUsers", "Network error: ${e.message}")
        } catch (e: Exception) {
            Log.e("SearchUsers", "Unexpected error: ${e.message}")
        }
        users
    }

    @OptIn(UnstableApi::class)
    private fun onUserClicked(user: UserResult) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("navigate_to", "shorts")
            putExtra("user_id", user.userId)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        finish()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
    }
}

data class UserResult(
    val userId: String,
    val username: String,
    val avatarUrl: String,
    val firstVideoId: String,
    val firstVideoUrl: String,
    val firstVideoThumbnail: String
)

// ─────────────────────────────────────────────────────────────────────────────
// MODERN LISTADAPTER — 100% THREAD-SAFE, NO MORE NOTIFYDATASETCHANGED()
// ─────────────────────────────────────────────────────────────────────────────

class SearchUserNameAdapter(
    private val onUserClicked: (UserResult) -> Unit
) : ListAdapter<Any, RecyclerView.ViewHolder>(SearchDiffCallback()) {

    fun showRecentUsers(users: List<UserResult>) {
        submitList(listOf("RECENT_HEADER") + users)
    }

    fun showSearchResults(users: List<UserResult>) {
        submitList(listOf("SEARCH_HEADER") + users)
    }

    fun showLoading() {
        submitList(List(10) { "LOADING" })
    }

    fun showNoResults() {
        submitList(listOf("NO_RESULTS"))
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        "RECENT_HEADER", "SEARCH_HEADER" -> 0
        is UserResult -> 1
        "LOADING" -> 2
        "NO_RESULTS" -> 3
        else -> -1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            0 -> HeaderViewHolder(
                if (getItem(0) == "RECENT_HEADER")
                    inflater.inflate(R.layout.recent_users_header, parent, false)
                else
                    inflater.inflate(R.layout.search_results_header, parent, false)
            )
            1 -> UserViewHolder(inflater.inflate(R.layout.search_user_item, parent, false))
            2 -> LoadingViewHolder(inflater.inflate(R.layout.shimmer_search_user, parent, false))
            3 -> NoResultsViewHolder(inflater.inflate(R.layout.no_results_item, parent, false))
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is UserViewHolder -> {
                val user = getItem(position) as UserResult
                holder.bind(user, onUserClicked)
            }
            is LoadingViewHolder -> holder.showLoading()
            // Header & NoResults have static layouts → nothing to bind
        }
    }
}

private class SearchDiffCallback : DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        if (oldItem is UserResult && newItem is UserResult) {
            return oldItem.userId == newItem.userId
        }
        return oldItem::class == newItem::class
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean = oldItem == newItem
}

// ViewHolders (same as before, only minor cleanup)
private class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view)
private class NoResultsViewHolder(view: View) : RecyclerView.ViewHolder(view)

private class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val avatar = itemView.findViewById<ImageView>(R.id.avatar)
    private val username = itemView.findViewById<TextView>(R.id.name)

    fun bind(user: UserResult, listener: (UserResult) -> Unit) {
        Glide.with(itemView.context)
            .load(user.avatarUrl)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .placeholder(R.drawable.person_button_svgrepo_com)
            .into(avatar)
        username.text = "@${user.username}"
        itemView.setOnClickListener { listener(user) }
    }
}

private class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val shimmer1 = itemView.findViewById<View>(R.id.shimmer_view)
    private val shimmer2 = itemView.findViewById<View>(R.id.shimmer_view2)

    fun showLoading() {
        shimmer1.visibility = View.VISIBLE
        shimmer2.visibility = View.VISIBLE
    }
}