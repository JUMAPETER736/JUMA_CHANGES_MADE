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

    private lateinit var apiService: IFlashapi
    private val shortsViewModel: ShortsViewModel by viewModels()

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
            Log.d("SearchResults", "Search text changed: '$searchText'")

            if (searchText.isEmpty()) {
                Log.d("SearchResults", "Search text is empty, clearing results")
                searchAdapter.setNoResults()
                binding.noResultsText.visibility = View.VISIBLE
                binding.noResultsText.text = "Type to search..."
                binding.progressBar.visibility = View.GONE
            } else {
                searchAdapter.setLoading(true)
                binding.progressBar.visibility = View.VISIBLE
                Log.d("SearchResults", "Performing search for: '$searchText'")
                performSearch(searchText)
            }
        })
    }

    private fun performSearch(query: String) {
        searchAdapter.setLoading(true)
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.Main) {
            val searchResults = searchUsers(query).sortedBy { it.username }

            Log.d("SearchResults", "Search results for '$query': ${searchResults.size} users")
            Log.d("SearchResults", searchResults.map { it.username }.toString())

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
        return withContext(Dispatchers.IO) {
            val users = mutableListOf<UserResult>()

            try {
                Log.d("SearchResults", "Calling searchUsers API with query: '$query'")
                val response = apiService.searchUsers(query)
                Log.d("SearchResults", "API Response Code: ${response.code()}")

                if (response.isSuccessful) {
                    Log.d("SearchResults", "Success Message: ${response.body()?.message}")
                    Log.d("SearchResults", "Success Data: ${response.body()?.data}")

                    val apiUsers = response.body()?.data as? List<com.uyscuti.social.network.api.models.User> ?: emptyList()

                    Log.d("SearchResults", "Converting ${apiUsers.size} API users to UserResult")

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
                            Log.d("SearchResults", "Added user: @${apiUser.username}")
                        } catch (e: Exception) {
                            Log.e("SearchResults", "Error converting user: ${e.message}")
                        }
                    }

                } else {
                    Log.e("SearchResults", "API Error: ${response.code()} - ${response.message()}")
                }

            } catch (e: HttpException) {
                Log.e("SearchResults", "HttpException: ${e.message}")
            } catch (e: IOException) {
                Log.e("SearchResults", "IOException: ${e.message}")
            } catch (e: Exception) {
                Log.e("SearchResults", "Exception: ${e.message}")
                e.printStackTrace()
            }

            users
        }
    }

    @OptIn(UnstableApi::class)
    private fun onUserClicked(user: UserResult) {
        Log.d("SearchResults", "User clicked: @${user.username} (${user.userId})")
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

class SearchResultsAdapter(
    private val users: MutableList<UserResult>,
    private val onItemClick: (UserResult) -> Unit
) : RecyclerView.Adapter<SearchResultsAdapter.UserViewHolder>() {

    private var isLoading = false

    fun setUsers(newUsers: List<UserResult>) {
        users.clear()
        users.addAll(newUsers)
        isLoading = false
        notifyDataSetChanged()
    }

    fun setNoResults() {
        users.clear()
        isLoading = false
        notifyDataSetChanged()
    }

    fun setLoading(loading: Boolean) {
        isLoading = loading
        if (loading) {
            // Optionally clear current results when loading starts
            // users.clear()
            // notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
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