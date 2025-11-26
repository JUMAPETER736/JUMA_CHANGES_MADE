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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException


class SearchShortActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchShortBinding
    private lateinit var searchAdapter: SearchResultsAdapter
    private var allResults = mutableListOf<SearchResult>()

    private lateinit var apiService: IFlashapi

    private val shortsViewModel: ShortsViewModel by viewModels()
    private var currentPage = 1
    private var isLoading = false

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

        // Initialize API service
        apiService = Retrofit(this).regService

        setupSearch()
        initSearchResults()
        setupBackButton()

        hideKeyboard()
        binding.searchEditText.clearFocus()

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
            searchAdapter = SearchResultsAdapter(mutableListOf()) { result ->
                onSearchResultClicked(result)
            }
            adapter = searchAdapter

            // Add scroll listener for pagination
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
                    val totalItemCount = layoutManager.itemCount

                    if (!isLoading && lastVisiblePosition >= totalItemCount - 3) {
                        loadMoreShorts()
                    }
                }
            })
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

        // Handle text changes
        binding.searchEditText.addTextChangedListener(afterTextChanged = { editable ->
            Log.d("SearchShort", "Text changed: $editable")

            val searchText = editable.toString().trim()

            if (searchText.isEmpty()) {
                // Hide results when search is empty
                searchAdapter.setSearchResults(emptyList())
                binding.noResultsText.visibility = View.GONE
            } else {
                searchAdapter.setLoading(true)
                performSearch(searchText)
            }
        })
    }

    private fun loadInitialData() {
        showLoading(true)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Load shorts from API
                val response = apiService.getShorts(currentPage.toString())

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val posts = responseBody?.data?.posts?.posts ?: emptyList()

                    val searchResults = posts.map { post ->
                        SearchResult(
                            id = post._id,
                            title = post.content.take(100),
                            description = post.content,
                            username = post.author.account.username,
                            thumbnailUrl = post.thumbnail.firstOrNull()?.thumbnailUrl ?: "",
                            videoUrl = post.images.firstOrNull()?.url ?: "",
                            authorId = post.author.account._id,
                            avatarUrl = post.author.account.avatar.url,
                            likes = post.likes,
                            comments = post.comments,
                            isLiked = post.isLiked,
                            isBookmarked = post.isBookmarked,
                            tags = post.tags,
                            createdAt = post.createdAt,
                            type = "short"
                        )
                    }

                    withContext(Dispatchers.Main) {
                        allResults.addAll(searchResults)
                        showLoading(false)
                        Log.d("SearchShort", "Loaded ${searchResults.size} shorts")
                    }

                    // Also load videos from feed
                    loadFeedVideos()

                } else {
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                        showToast("Error loading shorts: ${response.message()}")
                        Log.e("SearchShort", "Error: ${response.message()}")
                    }
                }

            } catch (e: HttpException) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showToast("Network error: ${e.message}")
                    Log.e("SearchShort", "HttpException: ${e.message}")
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showToast("Connection error. Please check your internet.")
                    Log.e("SearchShort", "IOException: ${e.message}")
                }
            }
        }
    }

    private suspend fun loadFeedVideos() {
        try {
            val response = apiService.getAllFeed(currentPage.toString())

            if (response.isSuccessful) {
                val responseBody = response.body()

                // Filter only video posts from feed
                val videoPosts = responseBody?.data?.data?.posts?.filter { post ->
                    post.contentType == "mixed_files" && post.fileTypes.any {
                        it.fileType.contains("video", ignoreCase = true) == true
                    }
                } ?: emptyList()

                val searchResults = videoPosts.map { post ->
                    SearchResult(
                        id = post._id,
                        title = post.content.take(100),
                        description = post.content,
                        username = post.author.account.username,
                        thumbnailUrl = post.thumbnail.firstOrNull()?.thumbnailUrl ?: "",
                        videoUrl = post.files.firstOrNull { file ->
                            post.fileTypes.any {
                                it.fileId == file.fileId &&
                                        it.fileType.contains("video", ignoreCase = true) == true
                            }
                        }?.url ?: "",
                        authorId = post.author.account._id,
                        avatarUrl = post.author.account.avatar.url,
                        likes = post.likes,
                        comments = post.comments,
                        isLiked = post.isLiked,
                        isBookmarked = post.isBookmarked,
                        tags = emptyList(),
                        createdAt = post.createdAt,
                        type = "feed_video"
                    )
                }

                withContext(Dispatchers.Main) {
                    allResults.addAll(searchResults)
                    Log.d("SearchShort", "Loaded ${searchResults.size} feed videos")
                }
            }
        } catch (e: Exception) {
            Log.e("SearchShort", "Error loading feed videos: ${e.message}")
        }
    }

    private fun performSearch(query: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val searchResults = searchInResults(query)

            hideKeyboard()
            binding.searchEditText.clearFocus()

            Log.d("SearchShort", "Search results: ${searchResults.size}")

            if (searchResults.isNotEmpty()) {
                searchAdapter.setSearchResults(searchResults)
                binding.noResultsText.visibility = View.GONE
            } else {
                searchAdapter.setNoResults()
                binding.noResultsText.visibility = View.VISIBLE
                binding.noResultsText.text = "No results found for \"$query\""
            }
        }
    }

    private suspend fun searchInResults(query: String): List<SearchResult> {
        searchAdapter.setLoading(true)
        return withContext(Dispatchers.Default) {
            val results = mutableListOf<SearchResult>()

            try {
                results.addAll(
                    allResults.filter { result ->
                        result.username.contains(query, ignoreCase = true)
                    }
                )

                Log.d("SearchShort", "Search completed with ${results.size} results")
            } catch (e: Exception) {
                Log.e("SearchShort", "Search exception: ${e.message}")
                e.printStackTrace()
            }

            results
        }
    }

    private fun loadMoreShorts() {
        if (isLoading) return

        isLoading = true
        currentPage++

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getShorts(currentPage.toString())

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val posts = responseBody?.data?.posts?.posts ?: emptyList()

                    val searchResults = posts.map { post ->
                        SearchResult(
                            id = post._id,
                            title = post.content.take(100),
                            description = post.content,
                            username = post.author.account.username,
                            thumbnailUrl = post.thumbnail.firstOrNull()?.thumbnailUrl ?: "",
                            videoUrl = post.images.firstOrNull()?.url ?: "",
                            authorId = post.author.account._id,
                            avatarUrl = post.author.account.avatar.url,
                            likes = post.likes,
                            comments = post.comments,
                            isLiked = post.isLiked,
                            isBookmarked = post.isBookmarked,
                            tags = post.tags,
                            createdAt = post.createdAt,
                            type = "short"
                        )
                    }

                    withContext(Dispatchers.Main) {
                        allResults.addAll(searchResults)
                        isLoading = false

                        // Update search results if there's an active query
                        val query = binding.searchEditText.text.toString().trim()
                        if (query.isNotEmpty()) {
                            performSearch(query)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoading = false
                    Log.e("SearchShort", "Error loading more: ${e.message}")
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun onSearchResultClicked(result: SearchResult) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("navigate_to", "shorts")
            putExtra("video_id", result.id)
            putExtra("video_url", result.videoUrl)
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
}

data class SearchResult(
    val id: String,
    val title: String,
    val description: String,
    val username: String,
    val thumbnailUrl: String,
    val videoUrl: String,
    val authorId: String,
    val avatarUrl: String,
    val likes: Int,
    val comments: Int,
    val isLiked: Boolean,
    val isBookmarked: Boolean,
    val tags: List<String>,
    val createdAt: String,
    val type: String
)

class SearchResultsAdapter(
    private val results: MutableList<SearchResult>,
    private val onItemClick: (SearchResult) -> Unit
) : RecyclerView.Adapter<SearchResultsAdapter.SearchViewHolder>() {

    private var isLoading = false

    fun setLoading(loading: Boolean) {
        isLoading = loading
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSearchResults(newResults: List<SearchResult>) {
        results.clear()
        results.addAll(newResults)
        isLoading = false
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setNoResults() {
        results.clear()
        isLoading = false
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
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
        return SearchViewHolder(textView, onItemClick)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(results[position])
    }

    override fun getItemCount() = results.size

    inner class SearchViewHolder(
        private val textView: TextView,
        private val onItemClick: (SearchResult) -> Unit
    ) : RecyclerView.ViewHolder(textView) {

        fun bind(result: SearchResult) {
            textView.text = "@${result.username}"
            textView.setOnClickListener { onItemClick(result) }
        }
    }
}