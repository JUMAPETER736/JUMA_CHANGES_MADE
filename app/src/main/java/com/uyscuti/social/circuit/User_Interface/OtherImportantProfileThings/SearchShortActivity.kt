package com.uyscuti.social.circuit.User_Interface.OtherImportantProfileThings

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException


class SearchShortActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchShortBinding
    private lateinit var searchAdapter: SearchResultsAdapter
    private val allResults = mutableListOf<SearchResult>()
    private val filteredResults = mutableListOf<SearchResult>()

    private lateinit var apiService: IFlashapi

    private val shortsViewModel: ShortsViewModel by viewModels()
    private var searchJob: Job? = null
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
        loadShortsData()
        setupBackButton()
    }

    private fun setupBackButton() {
        binding.backButton?.setOnClickListener {
            finish()
        }
    }

    private fun setupSearch() {
        searchAdapter = SearchResultsAdapter(filteredResults) { result ->
            onSearchResultClicked(result)
        }

        binding.searchResultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@SearchShortActivity)
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

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()

                // Cancel previous search
                searchJob?.cancel()

                if (query.isEmpty()) {
                    filteredResults.clear()
                    searchAdapter.notifyDataSetChanged()
                    binding.noResultsText.visibility = View.GONE
                } else {
                    // Debounce search by 300ms
                    searchJob = lifecycleScope.launch {
                        delay(300)
                        performSearch(query)
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    @SuppressLint("SetTextI18n")
    private fun performSearch(query: String) {
        lifecycleScope.launch(Dispatchers.Default) {
            val results = allResults.filter { result ->
                result.title.contains(query, ignoreCase = true) ||
                        result.description.contains(query, ignoreCase = true) ||
                        result.username.contains(query, ignoreCase = true) ||
                        result.tags.any { it.contains(query, ignoreCase = true) }
            }.toMutableList()

            withContext(Dispatchers.Main) {
                filteredResults.clear()
                filteredResults.addAll(results)
                searchAdapter.notifyDataSetChanged()

                if (filteredResults.isEmpty()) {
                    binding.noResultsText.visibility = View.VISIBLE
                    binding.noResultsText.text = "No results found for \"$query\""
                } else {
                    binding.noResultsText.visibility = View.GONE
                }
            }
        }
    }

    private fun loadShortsData() {
        showLoading(true)
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Load shorts from API
                val response = apiService.getShorts(currentPage.toString())

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val posts = responseBody?.data?.posts?.posts ?: emptyList()

                    val searchResults = posts.map { post ->
                        SearchResult(
                            id = post._id,
                            title = post.content.take(100), // First 100 chars as title
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
                        it.fileType?.contains("video", ignoreCase = true) == true
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
                                        it.fileType?.contains("video", ignoreCase = true) == true
                            }
                        }?.url ?: "",
                        authorId = post.author.account._id,
                        avatarUrl = post.author.account.avatar.url,
                        likes = post.likes,
                        comments = post.comments,
                        isLiked = post.isLiked,
                        isBookmarked = post.isBookmarked,
                        tags = emptyList(), // Feed posts may not have tags
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

    private fun loadMoreShorts() {
        if (isLoading) return

        isLoading = true
        currentPage++

        lifecycleScope.launch(Dispatchers.IO) {
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
        // Navigate back to MainActivity and play the selected short
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
        binding.progressBar?.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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
    private val results: List<SearchResult>,
    private val onItemClick: (SearchResult) -> Unit
) : RecyclerView.Adapter<SearchResultsAdapter.SearchViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val itemView = LinearLayout(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            setPadding(16, 16, 16, 16)
            setBackgroundResource(android.R.drawable.list_selector_background)
        }
        return SearchViewHolder(itemView, onItemClick)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(results[position])
    }

    override fun getItemCount() = results.size

    inner class SearchViewHolder(
        private val itemView: LinearLayout,
        private val onItemClick: (SearchResult) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(result: SearchResult) {
            itemView.apply {
                removeAllViews()

                // Thumbnail ImageView
                val thumbnailView = ImageView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(120, 120).apply {
                        rightMargin = 16
                    }
                    scaleType = ImageView.ScaleType.CENTER_CROP

                    // Load thumbnail with Glide
                    if (result.thumbnailUrl.isNotEmpty()) {
                        Glide.with(context)
                            .load(result.thumbnailUrl)
                            .placeholder(R.drawable.ic_launcher_background)
                            .error(R.drawable.ic_launcher_background)
                            .into(this)
                    } else {
                        setImageResource(R.drawable.ic_launcher_background)
                    }
                }
                addView(thumbnailView)

                // Content Container
                val contentLayout = LinearLayout(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                    orientation = LinearLayout.VERTICAL
                }

                // Title
                val titleView = TextView(context).apply {
                    text = if (result.title.isEmpty()) "Short Video" else result.title
                    textSize = 16f
                    setTextColor(context.getColor(android.R.color.black))
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                    maxLines = 2
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { bottomMargin = 4 }
                }
                contentLayout.addView(titleView)

                // Username
                val usernameView = TextView(context).apply {
                    text = "@${result.username}"
                    textSize = 14f
                    setTextColor(context.getColor(android.R.color.darker_gray))
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { bottomMargin = 4 }
                }
                contentLayout.addView(usernameView)

                // Stats (likes and comments)
                val statsView = TextView(context).apply {
                    text = " ${result.likes}   ${result.comments}"
                    textSize = 12f
                    setTextColor(context.getColor(android.R.color.darker_gray))
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }
                contentLayout.addView(statsView)

                addView(contentLayout)

                setOnClickListener { onItemClick(result) }
            }
        }
    }
}