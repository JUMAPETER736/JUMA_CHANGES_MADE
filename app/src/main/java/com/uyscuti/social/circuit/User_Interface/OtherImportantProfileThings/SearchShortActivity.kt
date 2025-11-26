package com.uyscuti.social.circuit.User_Interface.OtherImportantProfileThings

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.circuit.MainActivity
import com.uyscuti.social.circuit.R
import com.uyscuti.social.network.api.retrofit.interfaces.IFlashapi
import com.uyscuti.social.circuit.databinding.ActivitySearchShortBinding
import com.uyscuti.social.circuit.model.ShortsViewModel
import com.uyscuti.social.core.common.data.api.Retrofit
import kotlinx.coroutines.*
import retrofit2.HttpException
import java.io.IOException


class SearchShortActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchShortBinding
    private lateinit var searchAdapter: SearchResultsAdapter

    // Store unique users (not videos)
    private val allUsers = mutableMapOf<String, UserResult>() // userId -> UserResult

    private lateinit var apiService: IFlashapi
    private val shortsViewModel: ShortsViewModel by viewModels()

    private var currentPage = 1
    private var isLoading = false
    private var isInitialLoadComplete = false

    // For debouncing search
    private var searchJob: Job? = null
    private val searchDebounceTime = 300L

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

        apiService = Retrofit(this).regService

        initSearchResults()
        setupBackButton()
        setupSearch()

        // Load initial data
        loadInitialData()
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun initSearchResults() {
        binding.searchResultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@SearchShortActivity)
            searchAdapter = SearchResultsAdapter(mutableListOf()) { user ->
                onUserClicked(user)
            }
            adapter = searchAdapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
                    val totalItemCount = layoutManager.itemCount

                    if (!isLoading && lastVisiblePosition >= totalItemCount - 3 && isInitialLoadComplete) {
                        loadMoreData()
                    }
                }
            })
        }
    }

    private fun setupSearch() {
        binding.searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && binding.searchEditText.text.isEmpty() && isInitialLoadComplete) {
                showAllUsers()
            }
        }

        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                binding.searchEditText.clearFocus()
                true
            } else {
                false
            }
        }

        binding.searchEditText.addTextChangedListener(afterTextChanged = { editable ->
            val searchText = editable.toString().trim()

            searchJob?.cancel()

            if (!isInitialLoadComplete) {
                // Don't search until initial load is complete
                return@addTextChangedListener
            }

            if (searchText.isEmpty()) {
                showAllUsers()
                binding.progressBar.visibility = View.GONE
            } else {
                binding.progressBar.visibility = View.VISIBLE

                searchJob = lifecycleScope.launch {
                    delay(searchDebounceTime)
                    performSearch(searchText)
                }
            }
        })
    }

    private fun loadInitialData() {
        showLoading(true)
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Log.d("SearchShort", "Starting initial load...")

                loadShortsFromAPI(currentPage)
                loadVideosFromFeed(currentPage)

                withContext(Dispatchers.Main) {
                    isInitialLoadComplete = true
                    showLoading(false)

                    Log.d("SearchShort", "Initial load complete. Total unique users: ${allUsers.size}")

                    // Show all users initially
                    showAllUsers()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showToast("Error loading data: ${e.message}")
                    Log.e("SearchShort", "Error in initial load: ${e.message}", e)
                }
            }
        }
    }

    private suspend fun loadShortsFromAPI(page: Int) {
        try {
            val response = apiService.getShorts(page.toString())

            if (response.isSuccessful) {
                val responseBody = response.body()
                val posts = responseBody?.data?.posts?.posts ?: emptyList()

                Log.d("SearchShort", "Loaded ${posts.size} shorts from page $page")

                posts.forEach { post ->
                    val userId = post.author.account._id
                    val username = post.author.account.username.trim()

                    // Only add if this user isn't already in our map
                    if (!allUsers.containsKey(userId)) {
                        allUsers[userId] = UserResult(
                            userId = userId,
                            username = username,
                            avatarUrl = post.author.account.avatar.url,
                            firstVideoId = post._id,
                            firstVideoUrl = post.images.firstOrNull()?.url ?: "",
                            firstVideoThumbnail = post.thumbnail.firstOrNull()?.thumbnailUrl ?: ""
                        )
                        Log.d("SearchShort", "Added user: @$username")
                    }
                }

            } else {
                Log.e("SearchShort", "Shorts API error: ${response.message()}")
            }

        } catch (e: Exception) {
            Log.e("SearchShort", "Error loading shorts: ${e.message}", e)
        }
    }

    private suspend fun loadVideosFromFeed(page: Int) {
        try {
            val response = apiService.getAllFeed(page.toString())

            if (response.isSuccessful) {
                val responseBody = response.body()

                val videoPosts = responseBody?.data?.data?.posts?.filter { post ->
                    post.contentType == "mixed_files" && post.fileTypes.any {
                        it.fileType?.contains("video", ignoreCase = true) == true
                    }
                } ?: emptyList()

                Log.d("SearchShort", "Loaded ${videoPosts.size} feed videos from page $page")

                videoPosts.forEach { post ->
                    if (post.author == null || post.author.account == null) {
                        return@forEach
                    }

                    val userId = post.author.account._id
                    val username = post.author.account.username.trim()

                    // Only add if this user isn't already in our map
                    if (!allUsers.containsKey(userId)) {
                        val videoFile = post.files.firstOrNull { file ->
                            post.fileTypes.any {
                                it.fileId == file.fileId &&
                                        it.fileType?.contains("video", ignoreCase = true) == true
                            }
                        }

                        val videoThumbnail = post.thumbnail.firstOrNull { thumb ->
                            post.fileTypes.any {
                                it.fileId == thumb.fileId &&
                                        it.fileType?.contains("video", ignoreCase = true) == true
                            }
                        }

                        if (videoFile != null) {
                            allUsers[userId] = UserResult(
                                userId = userId,
                                username = username,
                                avatarUrl = post.author.account.avatar.url,
                                firstVideoId = post._id,
                                firstVideoUrl = videoFile.url,
                                firstVideoThumbnail = videoThumbnail?.thumbnailUrl ?: ""
                            )
                            Log.d("SearchShort", "Added user from feed: @$username")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SearchShort", "Error loading feed videos: ${e.message}", e)
        }
    }

    private fun showAllUsers() {
        val userList = allUsers.values.toList()
        searchAdapter.setUsers(userList)
        binding.noResultsText.visibility = View.GONE

        Log.d("SearchShort", "Showing all ${userList.size} users")
    }

    private fun performSearch(query: String) {
        lifecycleScope.launch {
            val searchResults = searchUsers(query)

            Log.d("SearchShort", "Search results for '$query': ${searchResults.size}")

            binding.progressBar.visibility = View.GONE

            if (searchResults.isNotEmpty()) {
                searchAdapter.setUsers(searchResults)
                binding.noResultsText.visibility = View.GONE
            } else {
                searchAdapter.setNoResults()
                binding.noResultsText.visibility = View.VISIBLE
                binding.noResultsText.text = "No users found for \"$query\""
            }
        }
    }

    private suspend fun searchUsers(query: String): List<UserResult> {
        return withContext(Dispatchers.Default) {
            try {
                val normalizedQuery = query.trim().lowercase()
                val userList = allUsers.values.toList()

                Log.d("SearchShort", "Searching for: '$normalizedQuery' in ${userList.size} users")

                val results = userList.filter { user ->
                    val normalizedUsername = user.username.trim().lowercase()
                    val matches = normalizedUsername.contains(normalizedQuery)

                    if (matches) {
                        Log.d("SearchShort", "Match found: @${user.username}")
                    }

                    matches
                }

                Log.d("SearchShort", "Search completed with ${results.size} results")
                results
            } catch (e: Exception) {
                Log.e("SearchShort", "Search exception: ${e.message}", e)
                emptyList()
            }
        }
    }

    private fun loadMoreData() {
        if (isLoading) return

        isLoading = true
        currentPage++

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Log.d("SearchShort", "Loading page $currentPage")

                loadShortsFromAPI(currentPage)
                loadVideosFromFeed(currentPage)

                withContext(Dispatchers.Main) {
                    isLoading = false

                    val query = binding.searchEditText.text.toString().trim()
                    if (query.isNotEmpty()) {
                        performSearch(query)
                    } else {
                        showAllUsers()
                    }

                    Log.d("SearchShort", "Page $currentPage loaded. Total users: ${allUsers.size}")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoading = false
                    Log.e("SearchShort", "Error loading page $currentPage: ${e.message}", e)
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun onUserClicked(user: UserResult) {
        // Navigate to shorts and play this user's first video
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("navigate_to", "shorts")
            putExtra("video_id", user.firstVideoId)
            putExtra("video_url", user.firstVideoUrl)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        finish()
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        searchJob?.cancel()
    }
}

// Changed from SearchResult to UserResult - stores unique users, not videos
data class UserResult(
    val userId: String,
    val username: String,
    val avatarUrl: String,
    val firstVideoId: String,      // Their first video to play when clicked
    val firstVideoUrl: String,
    val firstVideoThumbnail: String
)

class SearchResultsAdapter(
    private val users: MutableList<UserResult>,
    private val onItemClick: (UserResult) -> Unit
) : RecyclerView.Adapter<SearchResultsAdapter.UserViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun setUsers(newUsers: List<UserResult>) {
        users.clear()
        users.addAll(newUsers)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setNoResults() {
        users.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val textView = TextView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(48, 32, 48, 32)
            textSize = 18f
            setTextColor(parent.context.getColor(android.R.color.black))
            setBackgroundResource(android.R.drawable.list_selector_background)
        }
        return UserViewHolder(textView, onItemClick)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount() = users.size

    inner class UserViewHolder(
        private val textView: TextView,
        private val onItemClick: (UserResult) -> Unit
    ) : RecyclerView.ViewHolder(textView) {

        fun bind(user: UserResult) {
            textView.text = "@${user.username}"
            textView.setOnClickListener { onItemClick(user) }
        }
    }
}