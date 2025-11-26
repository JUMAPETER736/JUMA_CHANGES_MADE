package com.uyscuti.social.circuit.User_Interface.OtherImportantProfileThings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
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
import com.bumptech.glide.Glide
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

    private val allUsers = mutableMapOf<String, UserResult>()

    private lateinit var apiService: IFlashapi
    private val shortsViewModel: ShortsViewModel by viewModels()

    private var currentPage = 1
    private var isLoading = false
    private var isInitialLoadComplete = false

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

        loadInitialData()
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            Log.d("SearchResults", "Back button clicked")
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
                Log.d("SearchResults", "Search box focused, showing all users")
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
            Log.d("SearchResults", "Search text changed: '$searchText'")

            searchJob?.cancel()

            if (!isInitialLoadComplete) {
                Log.d("SearchResults", "Initial load not complete, skipping search")
                return@addTextChangedListener
            }

            if (searchText.isEmpty()) {
                Log.d("SearchResults", "Search text is empty, showing all users")
                showAllUsers()
                binding.progressBar.visibility = View.GONE
            } else {
                binding.progressBar.visibility = View.VISIBLE
                Log.d("SearchResults", "Debouncing search for: '$searchText'")

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
                Log.d("SearchResults", "Starting initial load...")

                loadShortsFromAPI(currentPage)
                loadVideosFromFeed(currentPage)

                withContext(Dispatchers.Main) {
                    isInitialLoadComplete = true
                    showLoading(false)

                    Log.d("SearchResults", "Initial load complete. Total unique users: ${allUsers.size}")

                    showAllUsers()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showToast("Error loading data: ${e.message}")
                    Log.e("SearchResults", "Error in initial load: ${e.message}", e)
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

                Log.d("SearchResults", "Loaded ${posts.size} shorts from page $page")

                posts.forEach { post ->
                    val userId = post.author.account._id
                    val username = post.author.account.username.trim()

                    if (!allUsers.containsKey(userId)) {
                        allUsers[userId] = UserResult(
                            userId = userId,
                            username = username,
                            avatarUrl = post.author.account.avatar.url,
                            firstVideoId = post._id,
                            firstVideoUrl = post.images.firstOrNull()?.url ?: "",
                            firstVideoThumbnail = post.thumbnail.firstOrNull()?.thumbnailUrl ?: ""
                        )
                        Log.d("SearchResults", "Added user from shorts: @$username")
                    }
                }

            } else {
                Log.e("SearchResults", "Shorts API error: ${response.message()}")
            }

        } catch (e: Exception) {
            Log.e("SearchResults", "Error loading shorts: ${e.message}", e)
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

                Log.d("SearchResults", "Loaded ${videoPosts.size} feed videos from page $page")

                videoPosts.forEach { post ->
                    if (post.author == null || post.author.account == null) {
                        return@forEach
                    }

                    val userId = post.author.account._id
                    val username = post.author.account.username.trim()

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
                            Log.d("SearchResults", "Added user from feed: @$username")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SearchResults", "Error loading feed videos: ${e.message}", e)
        }
    }

    private fun showAllUsers() {
        val userList = allUsers.values.toList()
        Log.d("SearchResults", "Showing all ${userList.size} users")
        searchAdapter.setUsers(userList)
        binding.noResultsText.visibility = View.GONE
    }

    private fun performSearch(query: String) {
        lifecycleScope.launch {
            val searchResults = searchUsers(query)

            Log.d("SearchResults", "Search results for '$query': ${searchResults.size}")

            binding.progressBar.visibility = View.GONE

            if (searchResults.isNotEmpty()) {
                Log.d("SearchResults", "Displaying ${searchResults.size} results")
                searchAdapter.setUsers(searchResults)
                binding.noResultsText.visibility = View.GONE
            } else {
                Log.d("SearchResults", "No results found for '$query'")
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

                Log.d("SearchResults", "Searching for: '$normalizedQuery' in ${userList.size} users")

                val results = userList.filter { user ->
                    val normalizedUsername = user.username.trim().lowercase()
                    val matches = normalizedUsername.contains(normalizedQuery)

                    if (matches) {
                        Log.d("SearchResults", "Match found: @${user.username}")
                    }

                    matches
                }

                Log.d("SearchResults", "Search completed with ${results.size} results")
                results
            } catch (e: Exception) {
                Log.e("SearchResults", "Search exception: ${e.message}", e)
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
                Log.d("SearchResults", "Loading page $currentPage")

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

                    Log.d("SearchResults", "Page $currentPage loaded. Total users: ${allUsers.size}")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoading = false
                    Log.e("SearchResults", "Error loading page $currentPage: ${e.message}", e)
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun onUserClicked(user: UserResult) {
        Log.d("SearchResults", "User clicked: @${user.username} (${user.userId})")
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

data class UserResult(
    val userId: String,
    val username: String,
    val avatarUrl: String,
    val firstVideoId: String,
    val firstVideoUrl: String,
    val firstVideoThumbnail: String
)

class SearchResultsAdapter(
    private val users: MutableList<UserResult>,
    private val onItemClick: (UserResult) -> Unit
) : RecyclerView.Adapter<SearchResultsAdapter.UserViewHolder>() {

    fun setUsers(newUsers: List<UserResult>) {
        users.clear()
        users.addAll(newUsers)
        notifyDataSetChanged()
    }

    fun setNoResults() {
        users.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)

        val container = LinearLayout(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            setPadding(16, 16, 16, 16)
            setBackgroundResource(android.R.drawable.list_selector_background)
        }

        val avatarImageView = ImageView(parent.context).apply {
            layoutParams = LinearLayout.LayoutParams(56, 56)
            scaleType = ImageView.ScaleType.CENTER_CROP
            clipToOutline = true
            outlineProvider = object : android.view.ViewOutlineProvider() {
                override fun getOutline(view: android.view.View, outline: android.graphics.Outline) {
                    outline.setOval(0, 0, view.width, view.height)
                }
            }
        }

        val usernameTextView = TextView(parent.context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                marginStart = 16
            }
            gravity = android.view.Gravity.CENTER_VERTICAL
            textSize = 16f
            setTextColor(parent.context.getColor(android.R.color.black))
        }

        container.addView(avatarImageView)
        container.addView(usernameTextView)

        return UserViewHolder(container, avatarImageView, usernameTextView, onItemClick)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount() = users.size

    inner class UserViewHolder(
        private val container: LinearLayout,
        private val avatarImageView: ImageView,
        private val usernameTextView: TextView,
        private val onItemClick: (UserResult) -> Unit
    ) : RecyclerView.ViewHolder(container) {

        fun bind(user: UserResult) {
            usernameTextView.text = "@${user.username}"

            // Load avatar using Glide
            Glide.with(avatarImageView.context)
                .load(user.avatarUrl)
                .circleCrop()
                .into(avatarImageView)

            container.setOnClickListener {
                Log.d("SearchResults", "Item clicked: @${user.username}")
                onItemClick(user)
            }
        }
    }
}