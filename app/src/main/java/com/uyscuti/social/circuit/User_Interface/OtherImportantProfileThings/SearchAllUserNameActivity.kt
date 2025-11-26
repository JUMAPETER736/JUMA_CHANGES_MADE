package com.uyscuti.social.circuit.User_Interface.OtherImportantProfileThings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
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
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
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
import kotlinx.coroutines.*
import retrofit2.HttpException
import java.io.IOException
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class SearchAllUserNameActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchAllUserNameBinding
    private lateinit var searchAdapter: SearchUserNameAdapter


    lateinit var apiService: IFlashapi

    private val shortsViewModel: ShortsViewModel by viewModels()
    private val recentUserViewModel: RecentUserViewModel by viewModels()

    private var searchJob: Job? = null

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
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadRecentUsers() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val recentUsers = withContext(Dispatchers.IO) {
                    recentUserViewModel.getRecentUsers()
                }
                searchAdapter.setRecentUsers(recentUsers.map { it.toUserResult() })
                Log.d("RecentUsers", "Loaded ${recentUsers.size} recent users")
            } catch (e: Exception) {
                Log.e("RecentUsers", "Error loading recent users: ${e.message}", e)
            }
        }
    }

    private fun RecentUser.toUserResult(): UserResult {
        return UserResult(
            userId = id,
            username = name,
            avatarUrl = avatar,
            firstVideoId = "",
            firstVideoUrl = "",
            firstVideoThumbnail = ""
        )
    }

    private fun initSearchResults() {
        searchAdapter = SearchUserNameAdapter(this) { user ->
            Log.d("SearchResults", "User clicked: @${user.username} (${user.userId})")

            onUserClicked(user)
        }

        binding.searchResultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@SearchAllUserNameActivity)
            adapter = searchAdapter
        }
    }



    private fun addUserToRecent(user: RecentUser) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                recentUserViewModel.addRecentUser(user)
                Log.d("RecentUsers", "Added user to recent: ${user.name}")
            } catch (e: Exception) {
                Log.e("RecentUsers", "Error adding recent user: ${e.message}", e)
            }
        }
    }

    private fun setupSearch() {
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.searchEditText.text.toString().trim()
                if (query.isNotEmpty()) performSearch(query)
                hideKeyboard()
                binding.searchEditText.clearFocus()
                true
            } else false
        }

        binding.searchEditText.addTextChangedListener(afterTextChanged = { editable ->
            val query = editable.toString().trim()
            searchJob?.cancel()

            if (query.isEmpty()) {
                loadRecentUsers()
            } else {
                searchJob = CoroutineScope(Dispatchers.Main).launch {
                    delay(500)
                    performSearch(query)
                }
            }
        })
    }

    private fun performSearch(query: String) {
        if (query.isEmpty()) return
        searchAdapter.setLoading(true)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val results = searchUsers(query).sortedBy { it.username }
                if (results.isNotEmpty()) {
                    searchAdapter.setSearchUsers(results)
                } else {
                    searchAdapter.setNoResults()
                }
            } catch (e: Exception) {
                Log.e("SearchError", "Search failed", e)
                searchAdapter.setNoResults()
            }
        }
    }

    private suspend fun searchUsers(query: String): List<UserResult> = withContext(Dispatchers.IO) {
        val users = mutableListOf<UserResult>()
        try {
            val response = apiService.searchUsers(query)
            if (response.isSuccessful && response.body() != null) {
                val apiUsers = response.body()!!.data as? List<com.uyscuti.social.network.api.models.User> ?: emptyList()
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
        } catch (e: Exception) {
            Log.e("SearchUsers", "Error during search", e)
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

    override fun onDestroy() {
        super.onDestroy()
        searchJob?.cancel()
    }
}

data class UserResult(
    val userId: String,
    val username: String,
    val avatarUrl: String,
    val firstVideoId: String = "",
    val firstVideoUrl: String = "",
    val firstVideoThumbnail: String = ""
)

class SearchUserNameAdapter(
    private val context: Context,
    private val listener: (UserResult) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var recentUserList: MutableList<UserResult> = mutableListOf()
    private var searchUserList: MutableList<UserResult> = mutableListOf()
    private var displayRecentUsers = true
    private var isLoading = false
    private var noResults = false

    private val SHIMMER_ITEM_COUNT = 10

    private val TYPE_RECENT_HEADER = 0
    private val TYPE_SEARCH_HEADER = 1
    private val TYPE_USER = 2
    private val TYPE_LOADING = 3
    private val TYPE_NO_RESULTS = 4

    fun setRecentUsers(users: List<UserResult>) {
        recentUserList = users.toMutableList()
        displayRecentUsers = true
        isLoading = false
        noResults = false
        notifyDataSetChanged()
    }

    fun setSearchUsers(users: List<UserResult>) {
        searchUserList = users.toMutableList()
        displayRecentUsers = false
        isLoading = false
        noResults = false
        notifyDataSetChanged()
    }

    fun setLoading(loading: Boolean) {
        isLoading = loading
        if (loading) {
            displayRecentUsers = false
            noResults = false
        }
        notifyDataSetChanged()
    }

    fun setNoResults() {
        noResults = true
        isLoading = false
        displayRecentUsers = false
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = when {
        isLoading -> SHIMMER_ITEM_COUNT
        noResults -> 1
        displayRecentUsers -> if (recentUserList.isEmpty()) 0 else recentUserList.size + 1
        else -> if (searchUserList.isEmpty()) 0 else searchUserList.size + 1
    }

    override fun getItemViewType(position: Int): Int = when {
        isLoading -> TYPE_LOADING
        noResults -> TYPE_NO_RESULTS
        displayRecentUsers && position == 0 -> TYPE_RECENT_HEADER
        !displayRecentUsers && position == 0 -> TYPE_SEARCH_HEADER
        else -> TYPE_USER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        return when (viewType) {
            TYPE_RECENT_HEADER -> HeaderViewHolder(inflater.inflate(R.layout.recent_users_header, parent, false))
            TYPE_SEARCH_HEADER -> HeaderViewHolder(inflater.inflate(R.layout.search_results_header, parent, false))
            TYPE_USER -> UserViewHolder(inflater.inflate(R.layout.search_user_item, parent, false))
            TYPE_LOADING -> LoadingViewHolder(inflater.inflate(R.layout.shimmer_search_user, parent, false))
            TYPE_NO_RESULTS -> NoResultsViewHolder(inflater.inflate(R.layout.no_results_item, parent, false))
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is UserViewHolder -> {
                val userPos = position - 1
                val user = if (displayRecentUsers) recentUserList[userPos] else searchUserList[userPos]
                holder.bind(user, listener)
            }
            is LoadingViewHolder -> holder.showLoading()
        }
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val avatar: ImageView = view.findViewById(R.id.avatar)
        private val username: TextView = view.findViewById(R.id.name)

        fun bind(user: UserResult, listener: (UserResult) -> Unit) {
            Glide.with(itemView.context)
                .load(user.avatarUrl)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(avatar)

            username.text = "@${user.username}"
            itemView.setOnClickListener { listener(user) }
        }
    }

    class LoadingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val shimmer1: View = view.findViewById(R.id.shimmer_view)
        private val shimmer2: View = view.findViewById(R.id.shimmer_view2)

        fun showLoading() {
            shimmer1.visibility = View.VISIBLE
            shimmer2.visibility = View.VISIBLE
        }
    }

    class NoResultsViewHolder(view: View) : RecyclerView.ViewHolder(view)
}