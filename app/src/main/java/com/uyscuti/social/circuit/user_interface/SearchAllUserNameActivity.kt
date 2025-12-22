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
            searchAdapter.showRecentUsers(recentUsers.map { it.toAuthor() })
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
                    delay(500) // Debounce 500ms to avoid too many API calls
                    performSearch(query)
                }
            }
        })
    }

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
        searchAdapter = SearchUserNameAdapter { author, postCount ->
            Log.d("SearchResults", " User clicked: @${author.account.username} (${author._id}) - $postCount posts")
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
            searchAdapter.showLoading()

            try {

                Log.d("SearchUsers", "CALLING BACKEND API")
                Log.d("SearchUsers", "Query: '$query'")



                // Call your backend search endpoint
                val response = withContext(Dispatchers.IO) {
                    apiService.getSearchAllFeedByUserId(query, page = "1", limit = "100")
                }

                if (response.isSuccessful) {
                    val body = response.body()

                    if (body != null && body.success) {
                        val data = body.data
                        val matchingUsers = data?.matchingUsers ?: emptyList()
                        val totalPosts = data?.totalPosts ?: 0

                        Log.d("SearchUsers", "\nAPI RESPONSE SUCCESS")
                        Log.d("SearchUsers", "Total Posts: $totalPosts")
                        Log.d("SearchUsers", "Matching Users: ${matchingUsers.size}")

                        if (matchingUsers.isNotEmpty()) {
                            // Create a map to track post counts per user
                            val userPostCounts = mutableMapOf<String, Int>()

                            // Count posts per user from the returned posts
                            data?.posts?.forEach { post ->
                                val authorId = post.author._id
                                userPostCounts[authorId] = (userPostCounts[authorId] ?: 0) + 1
                            }

                            // Convert matching users to Author objects with post counts
                            val authorsWithCounts = matchingUsers.map { user ->
                                val postCount = userPostCounts[user._id] ?: 0

                                Log.d("SearchUsers", "    @${user.username}: $postCount posts (User ID: ${user._id})")

                                // Find the author details from posts if available
                                val authorFromPost = data?.posts?.find { it.author._id == user._id }?.author

                                if (authorFromPost != null) {
                                    AuthorWithCount(authorFromPost, postCount)
                                } else {
                                    // Create minimal author from user data
                                    AuthorWithCount(
                                        Author(
                                            __v = 0,
                                            _id = user._id,
                                            account = com.uyscuti.social.network.api.response.posts.Account(
                                                _id = user._id,
                                                avatar = com.uyscuti.social.network.api.response.posts.Avatar(
                                                    _id = "",
                                                    localPath = "",
                                                    url = ""
                                                ),
                                                createdAt = "",
                                                email = user.email,
                                                updatedAt = "",
                                                username = user.username
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
                                        ),
                                        postCount
                                    )
                                }
                            }

                            Log.d("SearchUsers", "========================================\n")

                            searchAdapter.showSearchResults(authorsWithCounts)
                        } else {
                            Log.d("SearchUsers", "No users found matching '$query'")
                            Log.d("SearchUsers", "========================================\n")
                            searchAdapter.showNoResults()
                        }
                    } else {
                        Log.e("SearchUsers", "Response body is null or unsuccessful")
                        Log.e("SearchUsers", "Response: $body")
                        searchAdapter.showNoResults()
                    }
                } else {
                    Log.e("SearchUsers", "API ERROR")
                    Log.e("SearchUsers", "Code: ${response.code()}")
                    Log.e("SearchUsers", "Message: ${response.message()}")
                    Log.e("SearchUsers", "========================================\n")
                    searchAdapter.showNoResults()
                }

            } catch (e: HttpException) {
                Log.e("SearchUsers", "HTTP EXCEPTION: ${e.message()}", e)
                searchAdapter.showNoResults()
            } catch (e: IOException) {
                Log.e("SearchUsers", "NETWORK ERROR: ${e.message}", e)
                searchAdapter.showNoResults()
            } catch (e: Exception) {
                Log.e("SearchUsers", "UNEXPECTED ERROR: ${e.message}", e)
                e.printStackTrace()
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

// Data class to hold author with post count
data class AuthorWithCount(
    val author: Author,
    val postCount: Int
)

class SearchUserNameAdapter(
    private val onUserClicked: (Author, Int) -> Unit
) : ListAdapter<Any, RecyclerView.ViewHolder>(SearchDiffCallback()) {

    fun showRecentUsers(authors: List<Author>) {
        submitList(listOf("RECENT_HEADER") + authors.map { AuthorWithCount(it, 0) })
    }

    fun showSearchResults(authorsWithCounts: List<AuthorWithCount>) {
        submitList(listOf("SEARCH_HEADER") + authorsWithCounts)
    }

    fun showLoading() {
        submitList(List(10) { "LOADING" })
    }

    fun showNoResults() {
        submitList(listOf("NO_RESULTS"))
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        "RECENT_HEADER", "SEARCH_HEADER" -> 0
        is AuthorWithCount -> 1
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
                val authorWithCount = getItem(position) as AuthorWithCount
                holder.bind(authorWithCount.author, authorWithCount.postCount, onUserClicked)
            }
            is LoadingViewHolder -> holder.showLoading()
        }
    }
}

private class SearchDiffCallback : DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        if (oldItem is AuthorWithCount && newItem is AuthorWithCount) {
            return oldItem.author._id == newItem.author._id
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

    fun bind(author: Author, postCount: Int, listener: (Author, Int) -> Unit) {
        Glide.with(itemView.context)
            .load(author.account.avatar.url)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .placeholder(R.drawable.flash21)
            .into(avatar)

        // Show username with post count
        username.text = if (postCount > 0) {
            "@${author.account.username} ($postCount posts)"
        } else {
            "@${author.account.username}"
        }

        itemView.setOnClickListener { listener(author, postCount) }
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