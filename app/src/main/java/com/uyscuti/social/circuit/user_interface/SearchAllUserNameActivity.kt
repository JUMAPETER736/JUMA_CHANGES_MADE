package com.uyscuti.social.circuit.user_interface

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
import com.uyscuti.sharedmodule.model.ShortsViewModel
import com.uyscuti.sharedmodule.presentation.RecentUserViewModel
import com.uyscuti.social.circuit.MainActivity
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.ActivitySearchAllUserNameBinding
import com.uyscuti.social.network.api.retrofit.interfaces.IFlashapi
import com.uyscuti.social.core.common.data.room.entity.RecentUser
import com.uyscuti.social.network.api.response.posts.Author
import com.uyscuti.social.network.api.response.getallshorts.Author as ShortsAuthor
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.util.Date
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

@AndroidEntryPoint
class SearchAllUserNameActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchAllUserNameBinding
    private lateinit var searchAdapter: SearchUserNameAdapter
    private var searchJob: Job? = null
    private val apiService: IFlashapi by lazy {
        RetrofitInstance(LocalStorage(this), this).apiService
    }

    private val shortsViewModel: ShortsViewModel by viewModels()
    private val recentUserViewModel: RecentUserViewModel by viewModels()

    // Cache for all authors with their post counts
    private val allAuthorsMap = mutableMapOf<String, AuthorWithCount>()

    data class AuthorWithCount(
        val author: Author,
        var postCount: Int = 0
    )

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
        loadAllAuthorsWithCounts()
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
            searchAdapter.showRecentUsers(recentUsers.map { it.toAuthor() })
        }
    }

    private fun loadAllAuthorsWithCounts() {
        lifecycleScope.launch {
            searchAdapter.showLoading()

            withContext(Dispatchers.IO) {
                try {
                    Log.d("SearchUsers", "🔄 Starting to fetch all authors and count their posts...")

                    // Fetch from shorts (multiple pages)
                    for (page in 1..10) {
                        try {
                            val shortsResponse = apiService.getShorts(page.toString())
                            if (shortsResponse.isSuccessful) {
                                val body = shortsResponse.body()
                                body?.data?.posts?.posts?.forEach { post ->
                                    val author = post.author.toFeedAuthor()
                                    val authorId = author._id

                                    if (allAuthorsMap.containsKey(authorId)) {
                                        allAuthorsMap[authorId]!!.postCount++
                                    } else {
                                        allAuthorsMap[authorId] = AuthorWithCount(author, 1)
                                    }
                                }
                                Log.d("SearchUsers", "📱 Shorts page $page: ${allAuthorsMap.size} unique authors so far")
                            }
                        } catch (e: Exception) {
                            Log.e("SearchUsers", "Error fetching shorts page $page: ${e.message}")
                        }
                    }

                    // Fetch from feed (multiple pages)
                    for (page in 1..10) {
                        try {
                            val feedResponse = apiService.getAllFeed(page.toString())
                            if (feedResponse.isSuccessful) {
                                val body = feedResponse.body()
                                body?.data?.data?.posts?.forEach { post ->
                                    val author = post.author
                                    val authorId = author._id

                                    if (allAuthorsMap.containsKey(authorId)) {
                                        allAuthorsMap[authorId]!!.postCount++
                                    } else {
                                        allAuthorsMap[authorId] = AuthorWithCount(author, 1)
                                    }

                                    // Also count posts from reposted content
                                    post.originalPost?.forEach { originalPost ->
                                        val originalAuthor = originalPost.author.toAuthor()
                                        val originalAuthorId = originalAuthor._id

                                        if (allAuthorsMap.containsKey(originalAuthorId)) {
                                            allAuthorsMap[originalAuthorId]!!.postCount++
                                        } else {
                                            allAuthorsMap[originalAuthorId] = AuthorWithCount(originalAuthor, 1)
                                        }
                                    }
                                }
                                Log.d("SearchUsers", "📰 Feed page $page: ${allAuthorsMap.size} unique authors so far")
                            }
                        } catch (e: Exception) {
                            Log.e("SearchUsers", "Error fetching feed page $page: ${e.message}")
                        }
                    }

                    Log.d("SearchUsers", "✅ Finished loading. Total unique authors: ${allAuthorsMap.size}")

                    // Log top 5 authors with most posts
                    allAuthorsMap.values
                        .sortedByDescending { it.postCount }
                        .take(5)
                        .forEach { authorWithCount ->
                            Log.d("SearchUsers", "👤 ${authorWithCount.author.account.username}: ${authorWithCount.postCount} posts")
                        }

                } catch (e: Exception) {
                    Log.e("SearchUsers", "❌ Error loading authors: ${e.message}", e)
                }
            }

            // After loading, show recent users
            loadRecentUsers()
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

            // Cancel previous search job
            searchJob?.cancel()

            if (query.isEmpty()) {
                loadRecentUsers()
            } else {
                // Start new search job with debounce
                searchJob = lifecycleScope.launch {
                    delay(300)
                    performSearch(query)
                }
            }
        })
    }

    // Extension function to convert ShortsAuthor to Feed Author
    private fun ShortsAuthor.toFeedAuthor() = Author(
        __v = __v,
        _id = _id,
        account = com.uyscuti.social.network.api.response.posts.Account(
            _id = account._id,
            avatar = com.uyscuti.social.network.api.response.posts.Avatar(
                _id = account.avatar._id,
                localPath = account.avatar.localPath,
                url = account.avatar.url
            ),
            createdAt = "",
            email = account.email,
            updatedAt = "",
            username = account.username
        ),
        bio = bio,
        countryCode = countryCode,
        coverImage = com.uyscuti.social.network.api.response.posts.CoverImage(
            _id = coverImage._id,
            localPath = coverImage.localPath,
            url = coverImage.url
        ),
        createdAt = createdAt,
        dob = dob,
        firstName = firstName,
        lastName = lastName,
        location = location,
        owner = owner,
        phoneNumber = phoneNumber,
        updatedAt = updatedAt
    )

    // Extension function to convert AuthorX (from OriginalPost) to Author
    private fun com.uyscuti.social.network.api.response.posts.AuthorX.toAuthor() = Author(
        __v = 0,
        _id = _id,
        account = account,
        bio = bio,
        countryCode = countryCode,
        coverImage = coverImage,
        createdAt = createdAt,
        dob = dob,
        firstName = firstName,
        lastName = lastName,
        location = location,
        owner = owner,
        phoneNumber = phoneNumber,
        updatedAt = updatedAt
    )

    // Extension functions to convert between Author and RecentUser
    private fun Author.toRecentUser() = RecentUser(
        id = _id,
        name = account.username,
        avatar = account.avatar.url,
        lastSeen = Date(),
        online = false,
        dateAdded = Date()
    )

    private fun RecentUser.toAuthor() = Author(
        __v = 0,
        _id = id,
        account = com.uyscuti.social.network.api.response.posts.Account(
            _id = id,
            avatar = com.uyscuti.social.network.api.response.posts.Avatar(
                _id = "",
                localPath = "",
                url = avatar
            ),
            createdAt = "",
            email = "",
            updatedAt = "",
            username = name
        ),
        bio = "",
        countryCode = "",
        coverImage = com.uyscuti.social.network.api.response.posts.CoverImage(
            _id = "",
            localPath = "",
            url = ""
        ),
        createdAt = "",
        dob = "",
        firstName = "",
        lastName = "",
        location = "",
        owner = "",
        phoneNumber = "",
        updatedAt = ""
    )

    private fun initSearchResults() {
        searchAdapter = SearchUserNameAdapter { author ->
            // Find the post count for this author
            val postCount = allAuthorsMap[author._id]?.postCount ?: 0
            Log.d("SearchResults", "User clicked: @${author.account.username} (${author._id}) - $postCount posts")

            addUserToRecent(author.toRecentUser())
            onUserClicked(author)
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

    private fun performSearch(query: String) {
        if (query.isBlank()) {
            loadRecentUsers()
            return
        }

        lifecycleScope.launch {
            Log.d("SearchUsers", "🔍 Searching for: '$query', Cache size: ${allAuthorsMap.size}")

            val lowerQuery = query.lowercase()
            val results = allAuthorsMap.values
                .filter { authorWithCount ->
                    val author = authorWithCount.author
                    author.account.username.lowercase().contains(lowerQuery) ||
                            author.firstName.lowercase().contains(lowerQuery) ||
                            author.lastName.lowercase().contains(lowerQuery) ||
                            "${author.firstName} ${author.lastName}".lowercase().contains(lowerQuery)
                }
                .sortedByDescending { it.postCount } // Sort by post count
                .map { it.author }

            Log.d("SearchUsers", "📊 Found ${results.size} users matching '$query'")

            // Log results with their post counts
            results.take(5).forEach { author ->
                val postCount = allAuthorsMap[author._id]?.postCount ?: 0
                Log.d("SearchUsers", "👤 @${author.account.username}: $postCount posts")
            }

            if (results.isNotEmpty()) {
                searchAdapter.showSearchResults(results)
            } else {
                searchAdapter.showNoResults()
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun onUserClicked(author: Author) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("navigate_to", "shorts")
            putExtra("user_id", author._id)
            putExtra("filter_user_id", author._id)
            putExtra("filter_username", author.account.username)
            putExtra("filter_user_avatar", author.account.avatar.url)
            putExtra("should_filter", true)
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

class SearchUserNameAdapter(
    private val onUserClicked: (Author) -> Unit
) : ListAdapter<Any, RecyclerView.ViewHolder>(SearchDiffCallback()) {

    fun showRecentUsers(authors: List<Author>) {
        submitList(listOf("RECENT_HEADER") + authors)
    }

    fun showSearchResults(authors: List<Author>) {
        submitList(listOf("SEARCH_HEADER") + authors)
    }

    fun showLoading() {
        submitList(List(10) { "LOADING" })
    }

    fun showNoResults() {
        submitList(listOf("NO_RESULTS"))
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        "RECENT_HEADER", "SEARCH_HEADER" -> 0
        is Author -> 1
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
                val author = getItem(position) as Author
                holder.bind(author, onUserClicked)
            }
            is LoadingViewHolder -> holder.showLoading()
        }
    }
}

private class SearchDiffCallback : DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        if (oldItem is Author && newItem is Author) {
            return oldItem._id == newItem._id
        }
        return oldItem::class == newItem::class
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean = oldItem == newItem
}

private class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view)
private class NoResultsViewHolder(view: View) : RecyclerView.ViewHolder(view)

private class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val avatar = itemView.findViewById<ImageView>(R.id.avatar)
    private val username = itemView.findViewById<TextView>(R.id.name)

    fun bind(author: Author, listener: (Author) -> Unit) {
        Glide.with(itemView.context)
            .load(author.account.avatar.url)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .placeholder(R.drawable.flash21)
            .into(avatar)
        username.text = "${author.account.username}"
        itemView.setOnClickListener { listener(author) }
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