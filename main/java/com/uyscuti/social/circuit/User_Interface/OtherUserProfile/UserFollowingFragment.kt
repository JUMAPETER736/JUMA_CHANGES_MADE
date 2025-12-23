package com.uyscuti.social.circuit.User_Interface.OtherUserProfile

import UserFollowingDisplayModel
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
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.gson.JsonSyntaxException
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.ActivityUserFollowingBinding
import com.uyscuti.social.network.api.response.follow_unfollow.OtherUserDisplayFollowersModel
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.util.Date

private const val TAG = "UserFollowingFragment"

@UnstableApi
@AndroidEntryPoint
class UserFollowingFragment : AppCompatActivity() {

    private lateinit var binding: ActivityUserFollowingBinding
    private lateinit var followingAdapter: FollowingAdapter
    private lateinit var blockedAdapter: BlockedAdapter
    private lateinit var retrofitInstance: RetrofitInstance

    private var userId: String = ""
    private var username: String = ""
    private var fullName: String = ""
    private var followingCount: Int = 0

    // Lists for following users
    private var followingList = mutableListOf<UserFollowingDisplayModel>()
    private var filteredFollowingList = mutableListOf<UserFollowingDisplayModel>()

    // Lists for blocked users
    private var blockedList = mutableListOf<UserFollowingDisplayModel>()
    private var filteredBlockedList = mutableListOf<UserFollowingDisplayModel>()

    // Pagination and loading state
    private var currentPage = 1
    private var isLoading = false
    private var hasMoreData = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserFollowingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        extractIntentData()
        setupRetrofit()
        setupToolbar()
        setupSearchView()
        setupRecyclerView()
        setupSwipeRefresh()
        loadFollowingList()
    }

    private fun extractIntentData() {
        userId = intent.getStringExtra("user_id") ?: ""
        username = intent.getStringExtra("username") ?: ""
        fullName = intent.getStringExtra("full_name") ?: ""
        followingCount = intent.getIntExtra("following_count", 0)

        Log.d(TAG, "User data - ID: $userId, Username: $username, Following: $followingCount")
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Set title to show following count
        val formattedCount = formatCount(followingCount)
        binding.toolbarTitle.text = "$formattedCount Following"

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupSearchView() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString() ?: ""
                filterList(query)

                // Show/hide clear button based on query
                binding.clearSearchButton.visibility = if (query.isEmpty()) View.GONE else View.VISIBLE
            }
        })

        binding.clearSearchButton.setOnClickListener {
            binding.searchEditText.text?.clear()
        }
    }

    private fun setupRecyclerView() {
        // Initialize adapters with empty lists - they'll be updated later
        followingAdapter = FollowingAdapter(
            following = mutableListOf(),
            onFollowingClick = { user ->
                navigateToOtherUserProfile(user)
            },
            onUnfollowClick = { user ->
              //  showUnfollowConfirmation(user)
            },
            onMoreOptionsClick = { user ->
                showMoreOptions(user)
            }
        )

        blockedAdapter = BlockedAdapter(
            blockedUsers = mutableListOf(),
            onUserClick = { user ->
                navigateToOtherUserProfile(user)
            },
            onUnblockClick = { user ->
                showUnblockConfirmation(user)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@UserFollowingFragment)
            adapter = followingAdapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }
    }

    private fun refreshData() {
        currentPage = 1
        hasMoreData = true
        followingList.clear()
        filteredFollowingList.clear()
        loadFollowingList()
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

    // SIMPLIFIED: Skip the follow status check since it's causing JSON errors
    // Just use the followsBack value from the API response
    private suspend fun checkFollowingStatus(
        users: List<com.uyscuti.social.network.api.response.profile.followingList.Data>
    ): List<UserFollowingDisplayModel> {
        return users.map { user ->
            // Use followsBack directly without making additional API calls
            UserFollowingDisplayModel.fromFollowingUser(user, user.followsBack)
        }
    }

    private fun handleFollowingResponse(usersList: List<UserFollowingDisplayModel>) {
        Log.d(TAG, "handleFollowingResponse called with ${usersList.size} users")

        if (currentPage == 1) {
            followingList.clear()
            Log.d(TAG, "Cleared followingList for first page")
        }

        followingList.addAll(usersList)
        Log.d(TAG, "followingList now has ${followingList.size} items")

        filteredFollowingList.clear()
        filteredFollowingList.addAll(followingList)
        Log.d(TAG, "filteredFollowingList now has ${filteredFollowingList.size} items")

        followingAdapter.updateList(filteredFollowingList)
        Log.d(TAG, "Adapter updated, item count: ${followingAdapter.itemCount}")

        // Update UI visibility
        if (followingList.isEmpty()) {
            Log.d(TAG, "List is empty, showing empty state")
            binding.emptyStateLayout.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            Log.d(TAG, "List has data, showing RecyclerView")
            binding.emptyStateLayout.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }

        hasMoreData = usersList.size >= 20
        currentPage++
    }

    private fun filterList(query: String) {
        filteredFollowingList.clear()
        if (query.isEmpty()) {
            filteredFollowingList.addAll(followingList)
        } else {
            val filteredList = followingList.filter { user ->
                user.username.contains(query, ignoreCase = true) ||
                        user.fullName.contains(query, ignoreCase = true)
            }
            filteredFollowingList.addAll(filteredList)
        }
        followingAdapter.notifyDataSetChanged()

        // Show/hide empty view
        binding.emptyStateLayout.visibility = if (filteredFollowingList.isEmpty()) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (filteredFollowingList.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun loadFollowingList() {
        if (isLoading) return

        isLoading = true
        showLoading(true)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Loading following list for username: $username, page: $currentPage")

                val response = retrofitInstance.apiService.getOtherUserFollowing(
                    username = username,
                    page = currentPage,
                    limit = 20
                )

                Log.d(TAG, "API response code: ${response.code()}")

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d(TAG, "Response body is null: ${responseBody == null}")

                    if (responseBody == null) {
                        withContext(Dispatchers.Main) {
                            handleError("Empty response from server")
                        }
                        return@launch
                    }

                    val usersList = responseBody.data
                    Log.d(TAG, "Users list size: ${usersList?.size ?: 0}")

                    usersList?.let { users ->
                        // Check follow status for each user
                        val usersWithStatus = checkFollowingStatus(users)
                        Log.d(TAG, "Processed ${usersWithStatus.size} users with status")

                        withContext(Dispatchers.Main) {
                            handleFollowingResponse(usersWithStatus)
                        }
                    } ?: run {
                        Log.d(TAG, "Users list is null, showing empty")
                        withContext(Dispatchers.Main) {
                            handleFollowingResponse(emptyList())
                        }
                    }
                } else {
                    Log.e(TAG, "API error: ${response.code()}, ${response.errorBody()?.string()}")
                    withContext(Dispatchers.Main) {
                        val errorMessage = when (response.code()) {
                            404 -> "User not found"
                            401 -> "Authentication required. Please login again."
                            403 -> "Access denied"
                            500 -> "Server error, please try again"
                            else -> "Failed to load following: ${response.code()}"
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
                    val errorMessage = when (e.code()) {
                        401 -> "Session expired. Please login again."
                        else -> "Network error: ${e.code()}"
                    }
                    handleError(errorMessage)
                }
            } catch (e: IOException) {
                Log.e(TAG, "Network error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    handleError("No internet connection. Please check your network.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading following: ${e.message}", e)
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

    private fun loadBlockedList() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.emptyStateLayout.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    retrofitInstance.apiService.getBlockedUsers(
                        page = 1,
                        limit = 50
                    )
                }

                if (response.isSuccessful && response.body() != null) {
                    val usersResponse = response.body()!!
                    val users = usersResponse.data ?: emptyList()

                    blockedList.clear()
                    blockedList.addAll(users.map { user ->
                        UserFollowingDisplayModel(user, false)
                    })

                    filteredBlockedList.clear()
                    filteredBlockedList.addAll(blockedList)

                    // Switch to blocked adapter
                    binding.recyclerView.adapter = blockedAdapter
                    blockedAdapter.notifyDataSetChanged()

                    if (blockedList.isEmpty()) {
                        binding.emptyStateLayout.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.GONE
                    } else {
                        binding.emptyStateLayout.visibility = View.GONE
                        binding.recyclerView.visibility = View.VISIBLE
                    }
                } else {
                    showError("Failed to load blocked users")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading blocked users", e)
                showError("Network error: ${e.message}")
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun navigateToOtherUserProfile(user: OtherUserDisplayFollowersModel) {
        Log.d(TAG, "Navigate to Other User Profile: ${user.username}")

        try {
            val intent = Intent(this, OtherUserProfileAccount::class.java).apply {
                putExtra("user_id", user.id)
                putExtra("username", user.username)
                putExtra("full_name", user.fullName)
                putExtra("avatar_url", user.avatar?.url)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to profile", e)
            Toast.makeText(this, "Unable to open profile", Toast.LENGTH_SHORT).show()
        }
    }



    private fun showUnblockConfirmation(user: UserFollowingDisplayModel) {
        AlertDialog.Builder(this)
            .setTitle("Unblock @${user.username}?")
            .setMessage("Are you sure you want to unblock this user?")
            .setPositiveButton("Unblock") { _, _ ->
                performUnBlockUser(user)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showMoreOptions(user: UserFollowingDisplayModel) {
        val options = arrayOf(
            "View Profile",
            "Send Message",
            "Block User",
            "Report User"
        )

        AlertDialog.Builder(this)
            .setTitle("@${user.username}")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> navigateToOtherUserProfile(user)
                    1 -> sendMessageToUser(user)
                    2 -> blockUser(user)
                    3 -> reportUser(user)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Add this extension function
    fun UserFollowingDisplayModel.toUserDisplayFollowersModel(): OtherUserDisplayFollowersModel {
        return OtherUserDisplayFollowersModel(
            _id = this._id,
            avatar = this.avatar,
            email = this.email ?: "",
            isEmailVerified = this.isEmailVerified ?: false,
            role = this.role ?: "",
            username = this.username,
            lastseen = this.lastseen ?: Date(),
            isFollowing = this.isFollowing,
            firstName = this.firstName ?: "",
            lastName = this.lastName ?: "",
            isVerified = this.isVerified ?: false,
            isOnline = this.isOnline ?: false,
            hasActiveStory = this.hasActiveStory ?: false,
            mutualConnectionsCount = this.mutualConnectionsCount ?: 0,
            isSuggested = this.isSuggested ?: false,

        )
    }

    // Then use it in your methods
    private fun navigateToOtherUserProfile(user: UserFollowingDisplayModel) {
        val followerModel = user.toUserDisplayFollowersModel()
        // Use followerModel for navigation
    }

    private fun sendMessageToUser(user: UserFollowingDisplayModel) {
        Log.d(TAG, "Send message to: ${user.username}")
        Toast.makeText(this, "Message feature coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun blockUser(user: UserFollowingDisplayModel) {
        AlertDialog.Builder(this)
            .setTitle("Block @${user.username}?")
            .setMessage("This user will no longer be able to see your content or contact you.")
            .setPositiveButton("Block") { _, _ ->
                performBlockUser(user)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    @OptIn(UnstableApi::class)
    private fun performBlockUser(user: UserFollowingDisplayModel) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    retrofitInstance.apiService.blockUser(user.id)
                }

                if (response.isSuccessful) {
                    followingList.removeAll { it.id == user.id }
                    filteredFollowingList.removeAll { it.id == user.id }
                    followingAdapter.notifyDataSetChanged()

                    Toast.makeText(
                        this@UserFollowingFragment,
                        "Blocked @${user.username}",
                        Toast.LENGTH_SHORT
                    ).show()

                    if (filteredFollowingList.isEmpty()) {
                        binding.emptyStateLayout.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.GONE
                    }

                    updateFollowingCount(-1)
                } else {
                    showError("Failed to block user")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error blocking user", e)
                showError("Network error: ${e.message}")
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun performUnBlockUser(user: UserFollowingDisplayModel) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    retrofitInstance.apiService.unBlockUser(user.id)
                }

                if (response.isSuccessful) {
                    blockedList.removeAll { it.id == user.id }
                    filteredBlockedList.removeAll { it.id == user.id }
                    blockedAdapter.notifyDataSetChanged()

                    Toast.makeText(
                        this@UserFollowingFragment,
                        "Unblocked @${user.username}",
                        Toast.LENGTH_SHORT
                    ).show()

                    if (filteredBlockedList.isEmpty()) {
                        binding.emptyStateLayout.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.GONE
                    }
                } else {
                    showError("Failed to unblock user")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error unblocking user", e)
                showError("Network error: ${e.message}")
            }
        }
    }

    private fun reportUser(user: UserFollowingDisplayModel) {
        Log.d(TAG, "Report user: ${user.username}")
        Toast.makeText(this, "Report submitted", Toast.LENGTH_SHORT).show()
    }

    @OptIn(UnstableApi::class)
    private fun unfollowUser(user: UserFollowingDisplayModel) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    retrofitInstance.apiService.unfollowUser(user.id)
                }

                if (response.isSuccessful) {
                    followingList.removeAll { it.id == user.id }
                    filteredFollowingList.removeAll { it.id == user.id }
                    followingAdapter.notifyDataSetChanged()

                    Toast.makeText(
                        this@UserFollowingFragment,
                        "Unfollowed @${user.username}",
                        Toast.LENGTH_SHORT
                    ).show()

                    if (filteredFollowingList.isEmpty()) {
                        binding.emptyStateLayout.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.GONE
                    }

                    updateFollowingCount(-1)
                } else {
                    showError("Failed to unfollow user")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error unfollowing user", e)
                showError("Network error: ${e.message}")
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun updateFollowingCount(change: Int) {
        followingCount += change
        val formattedCount = formatCount(followingCount)
        binding.toolbarTitle.text = "$formattedCount Following"
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


// Following Adapter
class FollowingAdapter(
    private val following: MutableList<UserFollowingDisplayModel>,
    private val onFollowingClick: (UserFollowingDisplayModel) -> Unit,
    private val onUnfollowClick: (UserFollowingDisplayModel) -> Unit,
    private val onMoreOptionsClick: ((UserFollowingDisplayModel) -> Unit)? = null
) : RecyclerView.Adapter<FollowingAdapter.FollowingViewHolder>() {

    inner class FollowingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImageContainer: View = view.findViewById(R.id.profileImageContainer)
        val profileImage: ShapeableImageView = view.findViewById(R.id.profile_image)
        val usernameText: TextView = view.findViewById(R.id.usernameText)
        val fullNameText: TextView = view.findViewById(R.id.full_name)
        val followButton: MaterialButton = view.findViewById(R.id.followButton)
        val moreOptionsButton: MaterialButton = view.findViewById(R.id.moreOptionsButton)
        val verificationBadge: ImageView = view.findViewById(R.id.verificationBadge)
        val suggestionContainer: View = view.findViewById(R.id.suggestionContainer)
        val suggestionText: TextView = view.findViewById(R.id.suggestionText)
        val mutualConnectionsText: TextView = view.findViewById(R.id.mutualConnectionsText)
        val onlineStatusIndicator: View = view.findViewById(R.id.onlineStatusIndicator)
        val storyRing: ShapeableImageView = view.findViewById(R.id.storyRing)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_of_other_users_following, parent, false)
        return FollowingViewHolder(view)
    }

    override fun onBindViewHolder(holder: FollowingViewHolder, position: Int) {
        val followingUser = following[position]

        holder.usernameText.text = "@${followingUser.username}"
        holder.fullNameText.text = followingUser.fullName

        followingUser.avatar?.let { avatar ->
            Glide.with(holder.profileImage.context)
                .load(avatar.url)
                .placeholder(R.drawable.flash21)
                .error(R.drawable.flash21)
                .circleCrop()
                .into(holder.profileImage)
        } ?: run {
            holder.profileImage.setImageResource(R.drawable.flash21)
        }

        holder.followButton.text = "Following"
        holder.followButton.backgroundTintList = ContextCompat.getColorStateList(
            holder.itemView.context,
            R.color.blueJeans
        )

        setupOptionalElements(holder, followingUser)

        holder.itemView.setOnClickListener { onFollowingClick(followingUser) }
        holder.followButton.setOnClickListener { onUnfollowClick(followingUser) }
        holder.moreOptionsButton.setOnClickListener {
            onMoreOptionsClick?.invoke(followingUser)
        }
    }

    private fun setupOptionalElements(holder: FollowingViewHolder, user: UserFollowingDisplayModel) {
        holder.verificationBadge.visibility = if (user.isVerified) View.VISIBLE else View.GONE

        holder.suggestionContainer.visibility = if (user.isSuggested) View.VISIBLE else View.GONE
        if (user.isSuggested) {
            holder.suggestionText.text = "Suggested for you"
        }

        if (user.mutualConnectionsCount > 0) {
            holder.mutualConnectionsText.visibility = View.VISIBLE
            holder.mutualConnectionsText.text = "Followed by ${user.mutualConnectionsCount} others"
        } else {
            holder.mutualConnectionsText.visibility = View.GONE
        }

        holder.onlineStatusIndicator.visibility = if (user.isOnline) View.VISIBLE else View.GONE
        if (user.isOnline) {
            holder.onlineStatusIndicator.setBackgroundResource(R.drawable.ic_edit_text_bkg)
        }

        holder.storyRing.visibility = if (user.hasActiveStory) View.VISIBLE else View.GONE
    }

    override fun getItemCount(): Int = following.size

    fun updateList(newList: List<UserFollowingDisplayModel>) {
        following.clear()
        following.addAll(newList)
        notifyDataSetChanged()
    }
}

// Blocked Users Adapter
class BlockedAdapter(
    private val blockedUsers: MutableList<UserFollowingDisplayModel>,
    private val onUserClick: (UserFollowingDisplayModel) -> Unit,
    private val onUnblockClick: (UserFollowingDisplayModel) -> Unit
) : RecyclerView.Adapter<BlockedAdapter.BlockedViewHolder>() {

    inner class BlockedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ShapeableImageView = view.findViewById(R.id.profile_image)
        val usernameText: TextView = view.findViewById(R.id.usernameText)
        val fullNameText: TextView = view.findViewById(R.id.full_name)
        val actionButton: MaterialButton = view.findViewById(R.id.followButton)
        val moreOptionsButton: MaterialButton = view.findViewById(R.id.moreOptionsButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_of_other_users_following, parent, false)
        return BlockedViewHolder(view)
    }

    override fun onBindViewHolder(holder: BlockedViewHolder, position: Int) {
        val blockedUser = blockedUsers[position]

        holder.usernameText.text = "@${blockedUser.username}"
        holder.fullNameText.text = blockedUser.fullName

        blockedUser.avatar?.let { avatar ->
            Glide.with(holder.profileImage.context)
                .load(avatar.url)
                .placeholder(R.drawable.person_button_svgrepo_com)
                .error(R.drawable.person_button_svgrepo_com)
                .circleCrop()
                .into(holder.profileImage)
        } ?: run {
            holder.profileImage.setImageResource(R.drawable.person_button_svgrepo_com)
        }

        holder.actionButton.text = "Unblock"
        holder.actionButton.backgroundTintList = ContextCompat.getColorStateList(
            holder.itemView.context,
            R.color.red
        )

        holder.moreOptionsButton.visibility = View.GONE

        holder.itemView.setOnClickListener { onUserClick(blockedUser) }
        holder.actionButton.setOnClickListener { onUnblockClick(blockedUser) }
    }

    override fun getItemCount(): Int = blockedUsers.size

    fun updateList(newList: List<UserFollowingDisplayModel>) {
        blockedUsers.clear()
        blockedUsers.addAll(newList)
        notifyDataSetChanged()
    }
}