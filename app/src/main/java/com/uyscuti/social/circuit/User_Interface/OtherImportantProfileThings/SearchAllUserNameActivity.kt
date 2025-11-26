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
import com.uyscuti.social.network.api.retrofit.interfaces.IFlashapi
import com.uyscuti.social.circuit.databinding.ActivitySearchShortBinding
import com.uyscuti.social.circuit.model.ShortsViewModel
import kotlinx.coroutines.*
import retrofit2.HttpException
import java.io.IOException
import com.uyscuti.social.circuit.presentation.RecentUserViewModel
import com.uyscuti.social.core.common.data.room.entity.RecentUser
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class SearchUserNameActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchShortBinding
    private lateinit var searchAdapter: SearchUserNameAdapter

    @Inject
    lateinit var apiService: IFlashapi

    private val shortsViewModel: ShortsViewModel by viewModels()
    private val recentUserViewModel: RecentUserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySearchShortBinding.inflate(layoutInflater)
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
            onBackPressed()
        }
    }

    private fun loadRecentUsers() {
        CoroutineScope(Dispatchers.IO).launch {
            val recentUsers = recentUserViewModel.getRecentUsers()
            searchAdapter.setRecentUsers(recentUsers.map { it.toUserResult() })
            Log.d("RecentUsers", "RecentUsers: $recentUsers")
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
            addUserToRecent(user.toRecentUser())
            onUserClicked(user)
        }

        binding.searchResultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@SearchUserNameActivity)
            adapter = searchAdapter
        }
    }

    private fun UserResult.toRecentUser(): RecentUser {
        return RecentUser(
            userId,
            username,
            avatarUrl,
            Date(),
            false,
            Date()
        )
    }

    private fun addUserToRecent(user: RecentUser) {
        CoroutineScope(Dispatchers.IO).launch {
            recentUserViewModel.addRecentUser(user)
        }
    }

    private fun setupSearch() {
        // Handle the "Search" button on the keyboard
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchAdapter.setLoading(true)
                performSearch(binding.searchEditText.text.toString())
                hideKeyboard()
                binding.searchEditText.clearFocus()
                true
            } else {
                false
            }
        }

        // Detect text changes after user finishes typing
        binding.searchEditText.addTextChangedListener(afterTextChanged = { editable ->
            val searchText = editable.toString().trim()
            Log.d("FinishedTyping", "FinishedTyping: $searchText")

            if (searchText.isEmpty()) {
                // Show recent users when search is empty
                loadRecentUsers()
            } else {
                searchAdapter.setLoading(true)
                performSearch(searchText)
            }
        })
    }

    private fun performSearch(query: String) {
        searchAdapter.setLoading(true)

        CoroutineScope(Dispatchers.Main).launch {
            val searchResults = searchUsers(query).sortedBy { it.username }
            hideKeyboard()
            binding.searchEditText.clearFocus()

            Log.d("SearchResults", "Search results: $searchResults")
            Log.d("SearchResults", "Results count: ${searchResults.size}")

            if (searchResults.isNotEmpty()) {
                searchAdapter.setSearchUsers(searchResults)
            } else {
                searchAdapter.setNoResults()
            }
        }
    }

    private suspend fun searchUsers(query: String): List<UserResult> {
        searchAdapter.setLoading(true)
        return withContext(Dispatchers.IO) {
            val users = mutableListOf<UserResult>()

            try {
                Log.d("SearchUsers", "Calling searchUsers API with query: '$query'")
                val response = apiService.searchUsers(query)

                if (response.isSuccessful) {
                    Log.d("SearchUsers", "Success Message: ${response.body()?.message}")
                    Log.d("SearchUsers", "Success Data: ${response.body()?.data}")

                    val apiUsers = response.body()?.data as? List<com.uyscuti.social.network.api.models.User>
                        ?: emptyList()

                    apiUsers.forEach { apiUser ->
                        try {
                            val userResult = UserResult(
                                userId = apiUser._id,
                                username = apiUser.username,
                                avatarUrl = apiUser.avatar.url,
                                firstVideoId = "",
                                firstVideoUrl = "",
                                firstVideoThumbnail = ""
                            )
                            users.add(userResult)
                        } catch (e: Exception) {
                            Log.e("SearchUsers", "Error converting user: ${e.message}")
                        }
                    }
                } else {
                    Log.e("SearchUsers", "Error: ${response.message()}")
                }
            } catch (e: HttpException) {
                Log.e("SearchUsers", "HttpException: ${e.message}")
                e.printStackTrace()
            } catch (e: IOException) {
                Log.e("SearchUsers", "IOException: ${e.message}")
                e.printStackTrace()
            } catch (e: Exception) {
                Log.e("SearchUsers", "Exception: ${e.message}")
                e.printStackTrace()
            }

            users
        }
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
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
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

    // View types
    private val TYPE_RECENT_HEADER = 0
    private val TYPE_SEARCH_HEADER = 1
    private val TYPE_USER = 2
    private val TYPE_LOADING = 3
    private val TYPE_NO_RESULTS = 4

    fun setRecentUsers(recentUsers: List<UserResult>) {
        this.recentUserList = recentUsers.toMutableList()
        if (displayRecentUsers) {
            notifyDataSetChanged()
        } else {
            displayRecentUsers = true
            notifyDataSetChanged()
        }
    }

    fun setSearchUsers(searchUsers: List<UserResult>) {
        this.searchUserList = searchUsers.toMutableList()
        isLoading = false
        displayRecentUsers = false
        noResults = false
        notifyDataSetChanged()
    }

    fun setLoading(isLoading: Boolean) {
        this.isLoading = isLoading
        displayRecentUsers = false
        notifyDataSetChanged()
    }

    fun setNoResults() {
        this.noResults = true
        this.isLoading = false
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)

        return when (viewType) {
            TYPE_RECENT_HEADER -> {
                val view = inflater.inflate(R.layout.recent_users_header, parent, false)
                HeaderViewHolder(view)
            }
            TYPE_SEARCH_HEADER -> {
                val view = inflater.inflate(R.layout.search_results_header, parent, false)
                HeaderViewHolder(view)
            }
            TYPE_USER -> {
                val view = inflater.inflate(R.layout.search_user_item, parent, false)
                UserViewHolder(view)
            }
            TYPE_LOADING -> {
                val view = inflater.inflate(R.layout.shimmer_search_user, parent, false)
                LoadingViewHolder(view)
            }
            TYPE_NO_RESULTS -> {
                val view = inflater.inflate(R.layout.no_results_item, parent, false)
                NoResultsViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int {
        return when {
            displayRecentUsers -> recentUserList.size + 1
            isLoading -> SHIMMER_ITEM_COUNT
            noResults -> 1
            else -> searchUserList.size + 1
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            displayRecentUsers && position == 0 -> TYPE_RECENT_HEADER
            !displayRecentUsers && position == 0 -> TYPE_SEARCH_HEADER
            isLoading -> TYPE_LOADING
            noResults -> TYPE_NO_RESULTS
            else -> TYPE_USER
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                // Bind header data if needed
            }
            is UserViewHolder -> {
                val userPosition = if (displayRecentUsers) position - 1 else position - 1
                holder.bind(
                    if (displayRecentUsers) recentUserList[userPosition]
                    else searchUserList[userPosition],
                    listener
                )
            }
            is LoadingViewHolder -> {
                if (isLoading) {
                    holder.showLoading()
                } else {
                    holder.hideLoading()
                }
            }
            is NoResultsViewHolder -> {
                // No results message is handled in the layout
            }
        }
    }

    // ViewHolder for header
    private class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    // ViewHolder for user items
    private class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatarImageView: ImageView = itemView.findViewById(R.id.avatar)
        private val userNameTextView: TextView = itemView.findViewById(R.id.name)

        init {
            val selectableItemBackground = TypedValue()
            itemView.context.theme.resolveAttribute(
                android.R.attr.selectableItemBackground,
                selectableItemBackground,
                true
            )
            itemView.setBackgroundResource(selectableItemBackground.resourceId)
        }

        fun bind(user: UserResult, listener: (UserResult) -> Unit) {
            Glide.with(itemView.context)
                .load(user.avatarUrl)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .into(avatarImageView)

            userNameTextView.text = "@${user.username}"

            itemView.setOnClickListener {
                listener.invoke(user)
            }
        }
    }

    // ViewHolder for loading
    private class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val shimmerView: View = itemView.findViewById(R.id.shimmer_view)
        private val shimmerView2: View = itemView.findViewById(R.id.shimmer_view2)
        private var handler: Handler? = null
        private var shimmerRunnable: Runnable? = null

        init {
            showLoading()
        }

        fun showLoading() {
            shimmerView.visibility = View.VISIBLE
            shimmerView2.visibility = View.VISIBLE
            startShimmerEffect()
        }

        fun hideLoading() {
            shimmerView.visibility = View.GONE
            shimmerView2.visibility = View.GONE
            stopShimmerEffect()
        }

        private fun startShimmerEffect() {
            handler = Handler(Looper.getMainLooper())
            shimmerRunnable = object : Runnable {
                override fun run() {
                    shimmerView.alpha = 0.7f
                    shimmerView2.alpha = 0.7f

                    handler?.postDelayed({
                        shimmerView.alpha = 1f
                        shimmerView2.alpha = 1f
                    }, 500)

                    handler?.postDelayed(this, 600)
                }
            }
            handler?.post(shimmerRunnable!!)
        }

        private fun stopShimmerEffect() {
            shimmerRunnable?.let { handler?.removeCallbacks(it) }
            handler = null
            shimmerRunnable = null
        }
    }

    // ViewHolder for no results
    private class NoResultsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}