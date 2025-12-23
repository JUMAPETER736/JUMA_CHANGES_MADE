package com.uyscuti.social.circuit.User_Interface.OtherUserProfile

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import android.widget.ImageButton
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.JsonSyntaxException
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.ActivityUserFollowersBinding
import com.uyscuti.social.network.api.response.follow_unfollow.OtherUserDisplayFollowersModel
import com.uyscuti.social.network.api.response.profile.followersList.Data
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException


private const val TAG = "UserFollowersFragment"

@UnstableApi
@AndroidEntryPoint
class UserFollowersFragment : AppCompatActivity() {

    private lateinit var binding: ActivityUserFollowersBinding
    private lateinit var followersAdapter: FollowersAdapter
    private lateinit var retrofitInstance: RetrofitInstance

    private var userId: String = ""
    private var username: String = ""
    private var fullName: String = ""
    private var followersCount: Int = 0
    private var isMyFollowers: Boolean = false

    private var followersList = mutableListOf<OtherUserDisplayFollowersModel>()
    private var filteredFollowersList = mutableListOf<OtherUserDisplayFollowersModel>()

    private var currentPage = 1
    private var isLoading = false
    private var hasMoreData = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserFollowersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        extractIntentData()
        setupRetrofit()
        setupToolbar()
        setupRecyclerView()
        setupSearchView()
        setupPullToRefresh()
        loadFollowers()
    }

    private fun extractIntentData() {
        userId = intent.getStringExtra("user_id") ?: ""
        username = intent.getStringExtra("username") ?: ""
        fullName = intent.getStringExtra("full_name") ?: ""
        followersCount = intent.getIntExtra("followers_count", 0)
        isMyFollowers = intent.getBooleanExtra("is_my_followers", false)

        Log.d(TAG, "User data - ID: $userId, Username: $username, Followers: $followersCount, IsMyFollowers: $isMyFollowers")
    }

    private fun setupRetrofit() {
        val localStorage = LocalStorage(this)

        try {
            retrofitInstance = RetrofitInstance(
                localStorage = localStorage,
                context = this
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to setup Retrofit", e)
            handleError("Failed to initialize network connection")
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val formattedCount = formatCount(followersCount)
        val titleText = if (isMyFollowers) {
            "My $formattedCount Followers"
        } else {
            "$formattedCount Followers"
        }
        binding.toolbarTitle.text = titleText

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    @OptIn(UnstableApi::class)
    private fun setupRecyclerView() {
        followersAdapter = FollowersAdapter(
            followers = filteredFollowersList,
            onFollowerClick = { user -> openUserProfile(user) },
            onFollowClick = { user -> toggleFollowUser(user) },
            onMoreOptionsClick = { user -> showMoreOptions(user) },
            isMyFollowers = isMyFollowers
        )

        binding.recyclerView.apply {
            adapter = followersAdapter
            layoutManager = LinearLayoutManager(this@UserFollowersFragment)

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    if (!isLoading && hasMoreData) {
                        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                        val visibleItemCount = layoutManager.childCount
                        val totalItemCount = layoutManager.itemCount
                        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5) {
                            loadMoreFollowers()
                        }
                    }
                }
            })
        }
    }

    private fun setupSearchView() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString() ?: ""
                filterList(query)
                binding.clearSearchButton.visibility = if (query.isEmpty()) View.GONE else View.VISIBLE
            }
        })

        binding.clearSearchButton.setOnClickListener {
            binding.searchEditText.text?.clear()
        }
    }

    private fun setupPullToRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshFollowers()
        }

        binding.swipeRefreshLayout.setColorSchemeColors(
            ContextCompat.getColor(this, R.color.blueJeans)
        )
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun handleError(message: String) {
        Log.e(TAG, message)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        binding.emptyStateLayout.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
    }


    private suspend fun checkFollowStatus(
        users: List<com.uyscuti.social.network.api.response.profile.followersList.Data>):
            List<OtherUserDisplayFollowersModel> {

        return users.map { user ->
            try {
                val followStatusResponse = retrofitInstance.apiService.getOtherUsersFollowersAndFollowingStatus(user._id)
                val isFollowing = if (followStatusResponse.isSuccessful && followStatusResponse.body() != null) {
                    followStatusResponse.body()?.data?.isFollowing ?: user.isFollowingBack
                } else {
                    user.isFollowingBack
                }
                OtherUserDisplayFollowersModel.fromApiData(user, isFollowing)
            } catch (e: com.google.gson.JsonSyntaxException) {
                Log.e(TAG, "JSON error checking follow status for ${user.username}: ${e.message}")
                // Use isFollowingBack from the API response as fallback
                OtherUserDisplayFollowersModel.fromApiData(user, user.isFollowingBack)
            } catch (e: Exception) {
                Log.e(TAG, "Error checking follow status for ${user.username}: ${e.message}")
                OtherUserDisplayFollowersModel.fromApiData(user, user.isFollowingBack)
            }
        }
    }

    private fun loadFollowers() {
        if (isLoading) return

        if (username.isEmpty()) {
            handleError("Username is required")
            return
        }

        isLoading = true
        showLoading(true)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = retrofitInstance.apiService.getOtherUserFollowers(username, currentPage, 20)

                if (response.isSuccessful) {
                    val responseBody = response.body()

                    if (responseBody == null) {
                        withContext(Dispatchers.Main) {
                            handleError("Empty response from server")
                        }
                        return@launch
                    }

                    handleFollowersResponse(responseBody.data)
                } else {
                    withContext(Dispatchers.Main) {
                        val errorMessage = when (response.code()) {
                            404 -> "Followers not found"
                            401 -> "Authentication required"
                            403 -> "Access denied"
                            500 -> "Server error, please try again"
                            else -> "Failed to load followers: ${response.code()}"
                        }
                        handleError(errorMessage)
                    }
                }
            } catch (e: JsonSyntaxException) {
                Log.e(TAG, "JSON parsing error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    handleError("Invalid server response. Please try again.")
                }
            } catch (e: HttpException) {
                Log.e(TAG, "HTTP error: ${e.code()}", e)
                withContext(Dispatchers.Main) {
                    handleError("Network error: ${e.code()}")
                }
            } catch (e: IOException) {
                Log.e(TAG, "Network error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    handleError("No internet connection. Please check your network.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading followers: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    val errorMessage = when {
                        e.message?.contains("timeout", ignoreCase = true) == true ->
                            "Request timeout. Check your connection."
                        e.message?.contains("unable to resolve host", ignoreCase = true) == true ->
                            "No internet connection"
                        else -> "Network error: ${e.message ?: "Unknown error"}"
                    }
                    handleError(errorMessage)
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                    showLoading(false)
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }

    private fun loadMoreFollowers() {
        if (!hasMoreData || isLoading) return

        isLoading = true

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = retrofitInstance.apiService.getOtherUserFollowers(username, currentPage, 20)

                if (response.isSuccessful) {
                    val responseBody = response.body()

                    if (responseBody != null) {
                        val newFollowersWithStatus = checkFollowStatus(responseBody.data)
                        val newFollowers = newFollowersWithStatus.filter { newFollower ->
                            followersList.none { it._id == newFollower._id }
                        }

                        if (newFollowers.isNotEmpty()) {
                            val insertPosition = followersList.size
                            followersList.addAll(newFollowers)
                            filteredFollowersList.addAll(newFollowers)

                            withContext(Dispatchers.Main) {
                                followersAdapter.notifyItemRangeInserted(insertPosition, newFollowers.size)
                            }
                        }

                        hasMoreData = responseBody.data.size >= 20
                        currentPage++
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading more followers: ${e.message}", e)
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }

    private suspend fun handleFollowersResponse(followers: List<Data>) {
        withContext(Dispatchers.Main) {
            if (currentPage == 1) {
                followersList.clear()
                filteredFollowersList.clear()
            }

            val followersWithStatus = checkFollowStatus(followers)
            followersList.addAll(followersWithStatus)
            filteredFollowersList.addAll(followersWithStatus)

            followersAdapter.notifyDataSetChanged()
            updateEmptyState()

            hasMoreData = followers.size >= 20
            currentPage++
        }
    }

    private fun refreshFollowers() {
        currentPage = 1
        hasMoreData = true
        followersList.clear()
        filteredFollowersList.clear()
        followersAdapter.notifyDataSetChanged()
        loadFollowers()
    }

    private fun filterList(query: String) {
        filteredFollowersList.clear()

        if (query.isEmpty()) {
            filteredFollowersList.addAll(followersList)
        } else {
            val filteredList = followersList.filter { user ->
                user.firstName.contains(query, ignoreCase = true) ||
                        user.lastName.contains(query, ignoreCase = true) ||
                        user.username.contains(query, ignoreCase = true)
            }
            filteredFollowersList.addAll(filteredList)
        }

        followersAdapter.notifyDataSetChanged()
        updateEmptyState()
    }

    private fun updateEmptyState() {
        if (filteredFollowersList.isEmpty()) {
            binding.emptyStateLayout.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.emptyStateLayout.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }

    @UnstableApi
    private fun openUserProfile(user: OtherUserDisplayFollowersModel) {
        try {
            val intent = Intent(this, OtherUserProfileAccount::class.java).apply {
                putExtra("user_id", user.id)
                putExtra("username", user.username)
                putExtra("full_name", user.fullName)
                putExtra("avatar_url", user.avatar?.url)
            }
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to profile", e)
            Toast.makeText(this, "Unable to open profile", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleFollowUser(user: OtherUserDisplayFollowersModel) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = retrofitInstance.apiService.followUnFollow(user.id)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        user.isFollowing = !user.isFollowing
                        followersAdapter.notifyDataSetChanged()

                        val message = if (user.isFollowing) {
                            "Following ${user.username}"
                        } else {
                            "Unfollowed ${user.username}"
                        }
                        Toast.makeText(this@UserFollowersFragment, message, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@UserFollowersFragment, "Action failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling follow: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@UserFollowersFragment, "Network error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showMoreOptions(user: OtherUserDisplayFollowersModel) {
        if (isMyFollowers) {
            AlertDialog.Builder(this)
                .setTitle("Remove follower?")
                .setMessage("${user.username} will no longer be able to see your posts.")
                .setPositiveButton("Remove") { _, _ ->
                    performRemoveFollower(user)
                }
                .setNegativeButton("Cancel", null)
                .show()
        } else {
            AlertDialog.Builder(this)
                .setTitle("Options")
                .setItems(arrayOf("View Profile", "Block User")) { _, which ->
                    when (which) {
                        0 -> openUserProfile(user)
                        1 -> blockUser(user)
                    }
                }
                .show()
        }
    }

    private fun performRemoveFollower(user: OtherUserDisplayFollowersModel) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = retrofitInstance.apiService.removeFollower(user.id)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        followersList.remove(user)
                        filteredFollowersList.remove(user)
                        followersAdapter.notifyDataSetChanged()
                        updateEmptyState()
                        updateFollowersCount(-1)

                        Toast.makeText(this@UserFollowersFragment, "Follower removed", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@UserFollowersFragment, "Failed to remove follower", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error removing follower: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@UserFollowersFragment, "Network error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun blockUser(user: OtherUserDisplayFollowersModel) {
        AlertDialog.Builder(this)
            .setTitle("Block ${user.username}?")
            .setMessage("They won't be able to see your profile or posts.")
            .setPositiveButton("Block") { _, _ ->
                performBlockUser(user)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performBlockUser(user: OtherUserDisplayFollowersModel) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = retrofitInstance.apiService.blockUser(user.id)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        followersList.remove(user)
                        filteredFollowersList.remove(user)
                        followersAdapter.notifyDataSetChanged()
                        updateEmptyState()

                        Toast.makeText(this@UserFollowersFragment, "Blocked ${user.username}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@UserFollowersFragment, "Failed to block user", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error blocking user: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@UserFollowersFragment, "Network error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateFollowersCount(change: Int) {
        followersCount += change
        val formattedCount = formatCount(followersCount)
        val titleText = if (isMyFollowers) {
            "My $formattedCount Followers"
        } else {
            "$formattedCount Followers"
        }
        binding.toolbarTitle.text = titleText
    }

    private fun formatCount(count: Int): String {
        return when {
            count >= 1_000_000 -> {
                val millions = count / 1_000_000.0
                String.format("%.1fM", millions).replace(".0M", "M")
            }
            count >= 1_000 -> {
                val thousands = count / 1_000.0
                String.format("%.1fK", thousands).replace(".0K", "K")
            }
            else -> count.toString()
        }
    }
}

class FollowersAdapter(

    private val followers: MutableList<OtherUserDisplayFollowersModel>,
    private val onFollowerClick: (OtherUserDisplayFollowersModel) -> Unit,
    private val onFollowClick: (OtherUserDisplayFollowersModel) -> Unit,
    private val onMoreOptionsClick: (OtherUserDisplayFollowersModel) -> Unit,
    private val isMyFollowers: Boolean = false

) : RecyclerView.Adapter<FollowersAdapter.FollowerViewHolder>() {

    inner class FollowerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView = view.findViewById(R.id.profile_image)
        val usernameText: TextView = view.findViewById(R.id.usernameText)
        val fullNameText: TextView = view.findViewById(R.id.full_name)
        val bioText: TextView = view.findViewById(R.id.bio)
        val followButton: Button = view.findViewById(R.id.followButton)
        val moreOptionsButton: ImageButton = view.findViewById(R.id.more_options)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_of_other_users_followers, parent, false)
        return FollowerViewHolder(view)
    }

    override fun onBindViewHolder(holder: FollowerViewHolder, position: Int) {
        val follower = followers[position]

        holder.usernameText.text = "@${follower.username}"
        holder.fullNameText.text = follower.fullName

        // Show bio if available
        if (follower.bio.isNotEmpty()) {
            holder.bioText.visibility = View.VISIBLE
            holder.bioText.text = follower.bio
        } else {
            holder.bioText.visibility = View.GONE
        }

        // Load profile image
        follower.avatar?.let { avatar ->
            Glide.with(holder.profileImage.context)
                .load(avatar.url)
                .placeholder(R.drawable.flash21)
                .error(R.drawable.flash21)
                .circleCrop()
                .into(holder.profileImage)
        } ?: run {
            holder.profileImage.setImageResource(R.drawable.flash21)
        }

        // Update follow button state
        if (follower.isFollowing) {
            holder.followButton.text = "Following"
            holder.followButton.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.background_secondary)
            )
            holder.followButton.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.text_primary)
            )
        } else {
            holder.followButton.text = "Follow"
            holder.followButton.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.blueJeans)
            )
            holder.followButton.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.white)
            )
        }

        holder.itemView.setOnClickListener { onFollowerClick(follower) }
        holder.followButton.setOnClickListener { onFollowClick(follower) }
        holder.moreOptionsButton.setOnClickListener { onMoreOptionsClick(follower) }
    }

    override fun getItemCount(): Int = followers.size
}