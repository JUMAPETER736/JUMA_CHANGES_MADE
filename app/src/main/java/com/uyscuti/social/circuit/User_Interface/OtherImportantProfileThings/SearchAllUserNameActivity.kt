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
import com.uyscuti.social.network.api.response.posts.Author
import com.uyscuti.social.network.api.response.getallshorts.Author as ShortsAuthor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    // Cache for all authors
    private val allAuthorsCache = mutableListOf<Author>()

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
        loadAllAuthors()
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

    private fun loadAllAuthors() {
        lifecycleScope.launch {
            searchAdapter.showLoading()
            val authors = withContext(Dispatchers.IO) {
                fetchAllAuthorsFromShortsAndFeed()
            }
            allAuthorsCache.clear()
            allAuthorsCache.addAll(authors)

            // After loading, show recent users
            loadRecentUsers()
        }
    }

    private suspend fun fetchAllAuthorsFromShortsAndFeed(): List<Author> {
        val authorsMap = mutableMapOf<String, Author>()

        try {
            // Fetch from shorts (multiple pages)
            for (page in 1..5) {
                try {
                    val shortsResponse = apiService.getShorts(page.toString())
                    if (shortsResponse.isSuccessful) {
                        val body = shortsResponse.body()
                        if (body != null) {
                            val responseData = body.data
                            if (responseData != null) {
                                val postsData = responseData.posts
                                if (postsData != null) {
                                    val postsList = postsData.posts
                                    for (post in postsList) {
                                        val shortsAuthor = post.author
                                        val author = shortsAuthor.toFeedAuthor()
                                        if (!authorsMap.containsKey(author._id)) {
                                            authorsMap[author._id] = author
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SearchUsers", "Error fetching shorts page $page: ${e.message}")
                }
            }

            // Fetch from feed (multiple pages)
            for (page in 1..5) {
                try {
                    val feedResponse = apiService.getAllFeed(page.toString())
                    if (feedResponse.isSuccessful) {
                        val body = feedResponse.body()
                        if (body != null) {
                            val data = body.data
                            if (data != null) {
                                val dataX = data.data
                                if (dataX != null) {
                                    val postsList = dataX.posts
                                    for (post in postsList) {
                                        val author = post.author
                                        if (!authorsMap.containsKey(author._id)) {
                                            authorsMap[author._id] = author
                                        }

                                        // Also get authors from reposted content
                                        val originalPosts = post.originalPost
                                        for (originalPost in originalPosts) {
                                            val originalAuthor = originalPost.author
                                            if (!authorsMap.containsKey(originalAuthor._id)) {
                                                authorsMap[originalAuthor._id] = originalAuthor.toAuthor()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SearchUsers", "Error fetching feed page $page: ${e.message}")
                }
            }

        } catch (e: HttpException) {
            Log.e("SearchUsers", "HTTP error: ${e.message}")
        } catch (e: IOException) {
            Log.e("SearchUsers", "Network error: ${e.message}")
        } catch (e: Exception) {
            Log.e("SearchUsers", "Unexpected error: ${e.message}")
        }

        return authorsMap.values.toList()
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
            Log.d("SearchResults", "User clicked: @${author.account.username} (${author._id})")
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
                performSearch(query)
            }
        })
    }

    private fun performSearch(query: String) {
        lifecycleScope.launch {
            val results = searchUsers(query).sortedBy { it.account.username }
            if (results.isNotEmpty()) {
                searchAdapter.showSearchResults(results)
            } else {
                searchAdapter.showNoResults()
            }
        }
    }

    private suspend fun searchUsers(query: String): List<Author> = withContext(Dispatchers.Default) {
        if (allAuthorsCache.isEmpty()) {
            return@withContext emptyList()
        }

        val lowerQuery = query.lowercase()
        allAuthorsCache.filter { author ->
            author.account.username.lowercase().contains(lowerQuery) ||
                    author.firstName.lowercase().contains(lowerQuery) ||
                    author.lastName.lowercase().contains(lowerQuery) ||
                    "${author.firstName} ${author.lastName}".lowercase().contains(lowerQuery)
        }
    }

    @OptIn(UnstableApi::class)
    private fun onUserClicked(author: Author) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("navigate_to", "shorts")
            putExtra("user_id", author._id)
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
        username.text = "@${author.account.username}"
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