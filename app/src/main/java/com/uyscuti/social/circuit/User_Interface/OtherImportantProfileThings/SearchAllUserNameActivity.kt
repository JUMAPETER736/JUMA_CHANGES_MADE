package com.uyscuti.social.circuit.User_Interface.OtherImportantProfileThings


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator
import com.uyscuti.social.business.adapter.MediaPagerAdapter
import com.uyscuti.social.business.room.repository.BusinessRepository

import com.uyscuti.social.circuit.MainActivity
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.User_Interface.OtherUserProfile.OtherUserProfileAccount
import com.uyscuti.social.circuit.databinding.ActivitySearchAllUserNameBinding
import com.uyscuti.social.circuit.model.ShortsViewModel
import com.uyscuti.social.circuit.presentation.RecentUserViewModel
import com.uyscuti.social.core.common.data.room.entity.DialogEntity
import com.uyscuti.social.core.common.data.room.entity.MessageEntity
import com.uyscuti.social.network.api.retrofit.interfaces.IFlashapi
import com.uyscuti.social.core.common.data.room.entity.RecentUser
import com.uyscuti.social.core.common.data.room.entity.UserEntity
import com.uyscuti.social.network.api.models.User
import com.uyscuti.social.network.api.request.business.users.GetBusinessProfileById
import com.uyscuti.social.network.api.response.posts.Account
import com.uyscuti.social.network.api.response.posts.Author
import com.uyscuti.social.network.api.response.posts.Avatar
import com.uyscuti.social.network.api.response.posts.CoverImage
import com.uyscuti.social.network.api.response.posts.File
import com.uyscuti.social.network.api.response.posts.FileType
import com.uyscuti.social.network.api.response.posts.Post
import com.uyscuti.social.network.api.response.posts.RepostedUser
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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale

// Updated ContentFilter enum with your specific categories
enum class ContentFilter {
    ALL,
    SHORTS,
    FEED,
    PEOPLE,
    CHATS,
    BUSINESS
}

enum class SearchContext {
    GLOBAL,
    USER_POSTS,
    USER_PROFILE
}

@AndroidEntryPoint
class SearchAllUserNameActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchAllUserNameBinding
    private lateinit var searchAdapter: SearchUserNameAdapter
    private var searchJob: Job? = null

    private var currentSearchContext = SearchContext.GLOBAL
    private var currentFilter = ContentFilter.ALL
    private var selectedUserId: String? = null
    private var selectedUsername: String? = null
    private val recentUserViewModel: RecentUserViewModel by viewModels()
    private lateinit var businessRepository: BusinessRepository
    private lateinit var sharedPreferences: SharedPreferences

    private val apiService: IFlashapi by lazy {
        RetrofitInstance(LocalStorage(this), this).apiService
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySearchAllUserNameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupToolbar()
        initSearchResults()
        setupSearch()
        setupFilters()
        loadRecentUsers()
    }



    private fun applyFilter(filter: ContentFilter) {
        currentFilter = filter

        // Update chip selection
        binding.chipAll.isChecked = (filter == ContentFilter.ALL)
        binding.chipPeople.isChecked = (filter == ContentFilter.PEOPLE)
        binding.chipFeed.isChecked = (filter == ContentFilter.FEED)
        binding.chipShorts.isChecked = (filter == ContentFilter.SHORTS)
        binding.chipChats.isChecked = (filter == ContentFilter.CHATS)
        binding.chipBusiness.isChecked = (filter == ContentFilter.BUSINESS)

        // Refresh search with current query
        val query = binding.searchEditText.text.toString().trim()
        if (query.isNotEmpty()) {
            performSearch(query)
        }
    }

    private fun showContextIndicator(username: String) {
        binding.contextChip.visibility = View.VISIBLE
        binding.contextChip.text = "Posts by @$username"
        binding.contextChip.setOnCloseIconClickListener {
            resetToGlobalSearch()
        }
    }

    private fun resetToGlobalSearch() {
        currentSearchContext = SearchContext.GLOBAL
        selectedUserId = null
        selectedUsername = null
        binding.contextChip.visibility = View.GONE

        val query = binding.searchEditText.text.toString().trim()
        if (query.isNotEmpty()) {
            performSearch(query)
        } else {
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

            searchJob?.cancel()

            if (query.isEmpty()) {
                binding.filterChipsGroup.visibility = View.GONE
                binding.noResultsText.visibility = View.GONE
                loadRecentUsers()
            } else {
                binding.filterChipsGroup.visibility = View.VISIBLE
                // Reduced delay from 300ms to 150ms for faster response
                searchJob = lifecycleScope.launch {
                    delay(150)  // Changed from 300
                    performSearch(query)
                }
            }
        })
    }

    private fun setupFilters() {
        binding.filterChipsGroup.visibility = View.GONE

        // Set ALL as default
        currentFilter = ContentFilter.ALL
        binding.chipAll.isChecked = true

        binding.chipAll.setOnClickListener { applyFilter(ContentFilter.ALL) }
        binding.chipPeople.setOnClickListener { applyFilter(ContentFilter.PEOPLE) }
        binding.chipFeed.setOnClickListener { applyFilter(ContentFilter.FEED) }
        binding.chipShorts.setOnClickListener { applyFilter(ContentFilter.SHORTS) }
        binding.chipChats.setOnClickListener { applyFilter(ContentFilter.CHATS) }
        binding.chipBusiness.setOnClickListener { applyFilter(ContentFilter.BUSINESS) }
    }

    private fun performSearch(query: String) {
        if (query.isEmpty()) return

        lifecycleScope.launch {
            Log.d("SearchUsers", "Search: '$query', Context: $currentSearchContext, Filter: $currentFilter")

            // Show progress - Removed progressBar references since we removed it from layout
            binding.noResultsText.visibility = View.GONE
            searchAdapter.showLoading()

            try {
                when (currentSearchContext) {
                    SearchContext.GLOBAL -> {
                        val results = searchGlobalContent(query)
                        displaySearchResults(results)
                    }
                    SearchContext.USER_POSTS -> {
                        selectedUserId?.let { userId ->
                            val results = searchUserContent(query, userId)
                            displayUserContentResults(results)
                        }
                    }
                    SearchContext.USER_PROFILE -> {
                        // Handle user profile view
                    }
                }
            } catch (e: Exception) {
                Log.e("SearchUsers", "Search failed: ${e.message}", e)
                binding.noResultsText.visibility = View.VISIBLE
                searchAdapter.showNoResults()
            }
        }
    }


    private suspend fun searchGlobalContent(query: String): SearchResults =
        withContext(Dispatchers.IO) {
            try {
                // Execute ALL API calls in parallel using async
                val usersDeferred = async { searchUsers(query) }
                val shortsDeferred = async {
                    try {
                        apiService.getShorts("1")
                    } catch (e: Exception) {
                        Log.e("SearchGlobal", "Error fetching shorts", e)
                        null
                    }
                }
                val feedDeferred = async {
                    try {
                        apiService.getUserFeedForSearch("", "1", "100")
                    } catch (e: Exception) {
                        Log.e("SearchGlobal", "Error fetching feed", e)
                        null
                    }
                }
                val businessDeferred = async {
                    try {
                        apiService.getBusinessPost("1")
                    } catch (e: Exception) {
                        Log.e("SearchGlobal", "Error fetching business", e)
                        null
                    }
                }
                val chatsDeferred = async {
                    if (query.isNotBlank()) searchChats(query) else emptyList()
                }

                // Wait for all results
                val allUsers = usersDeferred.await()
                val shortsResponse = shortsDeferred.await()
                val feedResponse = feedDeferred.await()
                val businessPostsResponse = businessDeferred.await()
                val filteredChats = chatsDeferred.await()

                // -------- PROCESS USERS/PEOPLE --------
                val peopleAuthors = if (query.isBlank()) {
                    // Don't fetch profiles for empty query
                    allUsers.map { user ->
                        Author(
                            __v = 0,
                            _id = user._id,
                            account = Account(
                                _id = user._id,
                                avatar = Avatar(
                                    _id = user.avatar?._id ?: "",
                                    localPath = user.avatar?.localPath ?: "",
                                    url = user.avatar?.url ?: ""
                                ),
                                createdAt = "",
                                email = user.email ?: "",
                                updatedAt = "",
                                username = user.username ?: ""
                            ),
                            bio = "",
                            countryCode = "",
                            coverImage = CoverImage("", "", "https://via.placeholder.com/800x450.png"),
                            createdAt = "",
                            dob = "",
                            firstName = "",
                            lastName = "",
                            location = "",
                            owner = user._id,
                            phoneNumber = "",
                            updatedAt = ""
                        )
                    }
                } else {
                    // Fetch profiles in parallel for matched users
                    allUsers.map { user ->
                        async {
                            try {
                                val profileResponse = apiService.getOtherUsersProfileByUsername(user.username ?: "")
                                val profileData = profileResponse.body()?.data
                                val currentDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(Date())

                                Author(
                                    __v = profileData?.__v ?: 0,
                                    _id = profileData?._id ?: user._id,
                                    account = Account(
                                        _id = profileData?.account?._id ?: user._id,
                                        avatar = Avatar(
                                            _id = user.avatar?._id ?: "",
                                            localPath = user.avatar?.localPath ?: "",
                                            url = user.avatar?.url ?: ""
                                        ),
                                        createdAt = currentDate,
                                        email = profileData?.account?.email ?: user.email ?: "",
                                        updatedAt = currentDate,
                                        username = profileData?.account?.username ?: user.username ?: ""
                                    ),
                                    bio = profileData?.bio ?: "",
                                    countryCode = profileData?.countryCode ?: "",
                                    coverImage = CoverImage(
                                        _id = profileData?.coverImage?._id ?: "",
                                        localPath = profileData?.coverImage?.localPath ?: "",
                                        url = profileData?.coverImage?.url ?: "https://via.placeholder.com/800x450.png"
                                    ),
                                    createdAt = profileData?.createdAt ?: currentDate,
                                    dob = profileData?.dob ?: "",
                                    firstName = profileData?.firstName ?: "",
                                    lastName = profileData?.lastName ?: "",
                                    location = profileData?.location ?: "",
                                    owner = profileData?.owner ?: user._id,
                                    phoneNumber = profileData?.phoneNumber ?: "",
                                    updatedAt = profileData?.updatedAt ?: currentDate
                                )
                            } catch (e: Exception) {
                                Log.e("SearchGlobal", "Error fetching profile for ${user.username}", e)
                                null
                            }
                        }
                    }.awaitAll().filterNotNull()
                }

                // -------- PROCESS SHORTS --------
                val shortsRaw = shortsResponse?.body()?.data?.posts?.posts ?: emptyList()
                val convertedShorts: List<Post> = shortsRaw.map { it.toPostsPost() }

                val filteredShorts = if (query.isBlank()) {
                    convertedShorts.take(20)  // Limit initial results
                } else {
                    convertedShorts.filter { post ->
                        (post.content?.contains(query, ignoreCase = true) == true) ||
                                (post.author.account.username?.contains(query, ignoreCase = true) == true) ||
                                (post.author.firstName?.contains(query, ignoreCase = true) == true) ||
                                (post.author.lastName?.contains(query, ignoreCase = true) == true) ||
                                post.tags.any { it?.toString()?.contains(query, ignoreCase = true) == true }
                    }
                }

                val shortsOnly = filteredShorts.filter { post ->
                    post.fileTypes.any { it.fileType.equals("video", ignoreCase = true) }
                }

                // -------- PROCESS FEED POSTS --------
                val feedPosts: List<Post> = feedResponse?.body()?.data?.data?.posts ?: emptyList()

                val filteredFeed = if (query.isBlank()) {
                    feedPosts.take(20)  // Limit initial results
                } else {
                    feedPosts.filter { post ->
                        (post.content?.contains(query, ignoreCase = true) == true) ||
                                post.tags.any { it?.toString()?.contains(query, ignoreCase = true) == true } ||
                                (post.author.account.username?.contains(query, ignoreCase = true) == true) ||
                                (post.author.firstName?.contains(query, ignoreCase = true) == true) ||
                                (post.author.lastName?.contains(query, ignoreCase = true) == true)
                    }
                }

                val feedOnly = filteredFeed.filter { post ->
                    post.contentType.equals("text", ignoreCase = true) ||
                            (post.contentType.equals("mixed_files", ignoreCase = true) &&
                                    post.fileTypes.all { !it.fileType.equals("video", ignoreCase = true) })
                }

                // -------- PROCESS BUSINESS POSTS --------
                val allBusinessPosts = businessPostsResponse?.body()?.data?.posts ?: emptyList()

                val filteredBusinessPosts = if (query.isBlank()) {
                    allBusinessPosts.take(20)  // Limit initial results
                } else {
                    allBusinessPosts.filter { businessPost ->
                        val username = businessPost.userDetails.username.orEmpty()
                        (businessPost.itemName?.contains(query, ignoreCase = true) == true) ||
                                (businessPost.description?.contains(query, ignoreCase = true) == true) ||
                                username.contains(query, ignoreCase = true)
                    }
                }.map { businessPost ->
                    Post(
                        __v = businessPost.__v ?: 0,
                        _id = businessPost._id,
                        author = Author(
                            __v = 0,
                            _id = businessPost.owner,
                            account = Account(
                                _id = businessPost.owner,
                                avatar = Avatar(
                                    _id = "",
                                    localPath = "",
                                    url = businessPost.userDetails.avatar ?: ""
                                ),
                                createdAt = businessPost.createdAt ?: "",
                                email = "",
                                updatedAt = businessPost.updatedAt ?: "",
                                username = businessPost.userDetails.username ?: ""
                            ),
                            bio = "",
                            countryCode = "",
                            coverImage = CoverImage("", "", "https://via.placeholder.com/800x450.png"),
                            createdAt = businessPost.createdAt ?: "",
                            dob = "",
                            firstName = "",
                            lastName = "",
                            location = "",
                            owner = businessPost.owner,
                            phoneNumber = "",
                            updatedAt = businessPost.updatedAt ?: ""
                        ),
                        bookmarkCount = businessPost.bookmarkCount ?: 0,
                        comments = businessPost.comments ?: 0,
                        content = businessPost.description ?: "",
                        contentType = "business",
                        createdAt = businessPost.createdAt ?: "",
                        duration = emptyList(),
                        feedShortsBusinessId = businessPost._id,
                        fileIds = emptyList(),
                        fileNames = emptyList(),
                        fileSizes = emptyList(),
                        fileTypes = emptyList(),
                        files = ArrayList(businessPost.images?.map { imageUrl ->
                            File("", "", "", imageUrl, "image")
                        } ?: emptyList()),
                        isBookmarked = businessPost.isBookmarked ?: false,
                        isExpanded = false,
                        isFollowing = businessPost.isFollowing ?: false,
                        isLiked = businessPost.isLiked ?: false,
                        isLocal = false,
                        isReposted = false,
                        likes = businessPost.likes ?: 0,
                        numberOfPages = emptyList(),
                        originalPost = emptyList(),
                        repostedByUserId = "",
                        repostedUser = RepostedUser("", Avatar("", "", ""), "", CoverImage("", "", ""), "", "", "", "", "", "", ""),
                        repostedUsers = emptyList(),
                        tags = emptyList(),
                        thumbnail = emptyList(),
                        updatedAt = businessPost.updatedAt ?: "",
                        shareCount = 0,
                        repostCount = 0,
                        isBusinessPost = true,
                        category = "business",
                        businessDetails = null,
                        isFavorited = null,
                        favorites = null
                    )
                }

                Log.d("SearchGlobal", "Results - People: ${peopleAuthors.size}, Shorts: ${shortsOnly.size}, Feed: ${feedOnly.size}, Chats: ${filteredChats.size}, Business: ${filteredBusinessPosts.size}")

                SearchResults(
                    allPosts = filteredShorts + filteredFeed,
                    shorts = shortsOnly,
                    feedPosts = feedOnly,
                    people = peopleAuthors,
                    chats = filteredChats,
                    business = filteredBusinessPosts
                )

            } catch (e: Exception) {
                Log.e("SearchGlobal", "Error", e)
                SearchResults()
            }
        }

    private suspend fun searchUsers(query: String): List<User> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getUsers()
                val users = response.body()?.data ?: emptyList()

                if (query.isBlank()) return@withContext users

                users.filter { user ->
                    (user.username?.contains(query, true) == true) ||
                            (user.email?.contains(query, true) == true)
                }

            } catch (e: Exception) {
                emptyList()
            }
        }

    private suspend fun searchChats(query: String): List<DialogEntity> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getChats(limit = 100, offset = 0)
                val chats = response.body()?.data ?: emptyList()

                if (query.isBlank()) return@withContext emptyList()

                // Filter chats by participants, chat name, or last message
                val filteredChats = chats.filter { chat ->
                    chat.participants.any { participant ->
                        participant.username?.contains(query, true) == true
                    } ||
                            chat.name?.contains(query, true) == true ||
                            chat.lastMessage?.content?.contains(query, true) == true
                }

                // Convert to DialogEntity WITHOUT fetching profiles (much faster)
                filteredChats.map { chat ->
                    val enrichedUsers = chat.participants.map { participant ->
                        UserEntity(
                            id = participant._id,
                            name = participant.username ?: "Unknown",
                            avatar = participant.avatar?.url ?: "",
                            lastSeen = participant.lastseen ?: Date(),
                            online = false
                        )
                    }

                    val firstParticipant = enrichedUsers.firstOrNull()

                    DialogEntity(
                        id = chat._id,
                        dialogPhoto = firstParticipant?.avatar ?: chat.participants.firstOrNull()?.avatar?.url ?: "",
                        dialogName = firstParticipant?.name ?: chat.name ?: "Chat",
                        users = enrichedUsers,
                        lastMessage = null,
                        unreadCount = 0
                    )
                }

            } catch (e: Exception) {
                Log.e("SearchChats", "Error searching chats: ${e.message}", e)
                emptyList()
            }
        }

    fun com.uyscuti.social.network.api.response.getallshorts.Avatar.toPostsAvatar(): Avatar {
        return com.uyscuti.social.network.api.response.posts.Avatar(
            _id = this._id,
            url = this.url,
            localPath = this.localPath
        )
    }

    fun com.uyscuti.social.network.api.response.getallshorts.CoverImage.toPostsCoverImage(): CoverImage {
        return com.uyscuti.social.network.api.response.posts.CoverImage(
            _id = this._id,
            url = this.url,
            localPath = this.localPath
        )
    }

    fun com.uyscuti.social.network.api.response.getallshorts.Account.toPostsAccount(): Account {
        return Account(
            _id = this._id,
            avatar = this.avatar.toPostsAvatar(),
            createdAt = "",
            email = this.email,
            updatedAt = "",
            username = this.username
        )
    }

    fun com.uyscuti.social.network.api.response.getallshorts.Author.toPostsAuthor(): Author {
        return Author(
            _id = this._id,
            account = this.account.toPostsAccount(),
            bio = this.bio,
            countryCode = this.countryCode,
            coverImage = this.coverImage.toPostsCoverImage(),
            createdAt = this.createdAt,
            dob = this.dob,
            firstName = this.firstName,
            lastName = this.lastName,
            location = this.location,
            owner = this.owner,
            phoneNumber = this.phoneNumber,
            updatedAt = this.updatedAt,
            __v = this.__v
        )
    }

    // Convert get all shorts Post to posts.Post
    fun com.uyscuti.social.network.api.response.getallshorts.Post.toPostsPost(): Post {

        val fileTypes = images.map {
            FileType(
                fileId = it._id,
                fileType = if (it.url.endsWith(".mp4", true)) "video" else "image"
            )
        }

        val files = ArrayList(
            images.map {
                File(
                    _id = it._id,
                    fileId = it._id,
                    localPath = it.localPath,
                    url = it.url,
                    mimeType = null
                )
            }
        )

        return Post(
            __v = __v,
            _id = _id,
            author = author.toPostsAuthor(),

            content = content,
            contentType = if (fileTypes.any { it.fileType == "video" }) "mixed_files" else "text",

            createdAt = createdAt,
            updatedAt = updatedAt,

            fileIds = images.map { it._id },
            fileNames = emptyList(),
            fileSizes = emptyList(),
            fileTypes = fileTypes,
            files = files,
            thumbnail = emptyList(),
            duration = emptyList(),

            bookmarkCount = 0,
            comments = comments,
            isBookmarked = isBookmarked,
            isExpanded = false,
            isFollowing = false,
            isLiked = isLiked,
            isLocal = false,
            isReposted = false,
            likes = likes,

            numberOfPages = emptyList(),
            originalPost = emptyList(),
            repostedByUserId = "",
            repostedUser = RepostedUser(
                _id = "",
                avatar = Avatar(_id = "", localPath = "", url = ""),
                bio = "",
                coverImage = CoverImage(_id = "", localPath = "", url = ""),
                createdAt = "",
                email = "",
                firstName = "",
                lastName = "",
                owner = "",
                updatedAt = "",
                username = ""
            ),

            repostedUsers = emptyList(),

            tags = tags,
            shareCount = 0,
            repostCount = 0,

            feedShortsBusinessId = "",
            isBusinessPost = false,
            category = null,
            businessDetails = null,

            isFavorited = null,
            favorites = null
        )
    }


    private suspend fun searchUserContent(query: String, userId: String): SearchResults = withContext(Dispatchers.IO) {
        try {
            Log.d("SearchUserContent", "Searching user content: userId=$userId, username=$selectedUsername, query=$query")

            selectedUsername?.let { username ->
                Log.d("SearchUserContent", "Fetching posts for user: $username")

                val response = apiService.getUserFeedForSearch(
                    username = username,
                    page = "1",
                    limit = "100"
                )

                Log.d("SearchUserContent", "Response code: ${response.code()}")

                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string()
                    Log.e("SearchUserContent", "HTTP Error: ${response.code()} - $errorBody")
                    return@withContext SearchResults()
                }

                val body = response.body()
                if (body == null) {
                    Log.e("SearchUserContent", "Response body is null")
                    return@withContext SearchResults()
                }

                if (!body.success) {
                    Log.e("SearchUserContent", "API returned success=false: ${body.message}")
                    return@withContext SearchResults()
                }

                // PATH: FeedResponse -> data -> data -> posts -> List<posts.Post>
                val allPosts = body.data?.data?.posts ?: emptyList()
                Log.d("SearchUserContent", "Fetched ${allPosts.size} posts from user")

                // CLIENT-SIDE FILTERING on posts.Post
                val filteredPosts = if (query.isEmpty()) {
                    allPosts
                } else {
                    allPosts.filter { post ->
                        val matchesContent = post.content.contains(query, ignoreCase = true)
                        val matchesTags = post.tags.any { tag ->
                            tag?.toString()?.contains(query, ignoreCase = true) == true
                        }
                        matchesContent || matchesTags
                    }
                }

                Log.d("SearchUserContent", "Filtered to ${filteredPosts.size} posts")

                // Filter shorts - check contentType and fileTypes for videos
                val shorts = filteredPosts.filter { post ->
                    post.contentType.equals("mixed_files", ignoreCase = true) &&
                            post.fileTypes.any { fileType ->
                                fileType.fileType.equals("video", ignoreCase = true)
                            }
                }

                // Filter feed posts - text or non-video mixed_files
                val feedPosts = filteredPosts.filter { post ->
                    // Text posts
                    post.contentType.equals("text", ignoreCase = true) ||
                            // Mixed files without videos (images only)
                            (post.contentType.equals("mixed_files", ignoreCase = true) &&
                                    post.fileTypes.all { fileType ->
                                        !fileType.fileType.equals("video", ignoreCase = true)
                                    })
                }

                // -------- FETCH AND FILTER BUSINESS POSTS --------
                val businessPostsResponse = apiService.getBusinessPost("1")
                val allBusinessPosts = businessPostsResponse.body()?.data?.posts ?: emptyList()

                Log.d("SearchUserContent", "Total business posts: ${allBusinessPosts.size}")

                // Filter by userId FIRST
                val userBusinessPosts = allBusinessPosts.filter { businessPost ->
                    businessPost.owner == userId
                }

                Log.d("SearchUserContent", "User's business posts: ${userBusinessPosts.size}")

                // Then filter by query
                val filteredBusinessPosts = if (query.isBlank()) {
                    userBusinessPosts
                } else {
                    userBusinessPosts.filter { businessPost ->
                        (businessPost.itemName?.contains(query, ignoreCase = true) == true) ||
                                (businessPost.description?.contains(query, ignoreCase = true) == true)
                    }
                }.map { businessPost ->
                    Post(
                        __v = businessPost.__v ?: 0,
                        _id = businessPost._id,
                        author = Author(
                            __v = 0,
                            _id = businessPost.owner,
                            account = Account(
                                _id = businessPost.owner,
                                avatar = Avatar(
                                    _id = "",
                                    localPath = "",
                                    url = businessPost.userDetails.avatar ?: ""
                                ),
                                createdAt = businessPost.createdAt ?: "",
                                email = "",
                                updatedAt = businessPost.updatedAt ?: "",
                                username = businessPost.userDetails.username ?: ""
                            ),
                            bio = "",
                            countryCode = "",
                            coverImage = CoverImage("", "", "https://via.placeholder.com/800x450.png"),
                            createdAt = businessPost.createdAt ?: "",
                            dob = "",
                            firstName = "",
                            lastName = "",
                            location = "",
                            owner = businessPost.owner,
                            phoneNumber = "",
                            updatedAt = businessPost.updatedAt ?: ""
                        ),
                        bookmarkCount = businessPost.bookmarkCount ?: 0,
                        comments = businessPost.comments ?: 0,
                        content = businessPost.description ?: "",
                        contentType = "business",
                        createdAt = businessPost.createdAt ?: "",
                        duration = emptyList(),
                        feedShortsBusinessId = businessPost._id,
                        fileIds = emptyList(),
                        fileNames = emptyList(),
                        fileSizes = emptyList(),
                        fileTypes = emptyList(),
                        files = ArrayList(businessPost.images?.map { imageUrl ->
                            File("", "", "", imageUrl, "image")
                        } ?: emptyList()),
                        isBookmarked = businessPost.isBookmarked ?: false,
                        isExpanded = false,
                        isFollowing = businessPost.isFollowing ?: false,
                        isLiked = businessPost.isLiked ?: false,
                        isLocal = false,
                        isReposted = false,
                        likes = businessPost.likes ?: 0,
                        numberOfPages = emptyList(),
                        originalPost = emptyList(),
                        repostedByUserId = "",
                        repostedUser = RepostedUser("", Avatar("", "", ""), "", CoverImage("", "", ""), "", "", "", "", "", "", ""),
                        repostedUsers = emptyList(),
                        tags = emptyList(),
                        thumbnail = emptyList(),
                        updatedAt = businessPost.updatedAt ?: "",
                        shareCount = 0,
                        repostCount = 0,
                        isBusinessPost = true,
                        category = "business",
                        businessDetails = null,
                        isFavorited = null,
                        favorites = null
                    )
                }

                Log.d("SearchUserContent", "User Results - Shorts: ${shorts.size}, Feed: ${feedPosts.size}, Business: ${filteredBusinessPosts.size}")

                return@withContext SearchResults(
                    allPosts = filteredPosts + filteredBusinessPosts,
                    shorts = shorts,
                    feedPosts = feedPosts,
                    people = emptyList(),
                    chats = emptyList(),
                    business = filteredBusinessPosts
                )
            }

        } catch (e: HttpException) {
            Log.e("SearchUserContent", "HTTP Exception: ${e.code()} - ${e.message()}")
            try {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("SearchUserContent", "Error body: $errorBody")
            } catch (ex: Exception) {
                Log.e("SearchUserContent", "Could not read error body", ex)
            }
        } catch (e: IOException) {
            Log.e("SearchUserContent", "Network Error: ${e.message}", e)
        } catch (e: Exception) {
            Log.e("SearchUserContent", "Unexpected Error: ${e.message}", e)
            e.printStackTrace()
        }

        return@withContext SearchResults()
    }

    private fun displaySearchResults(results: SearchResults) {
        val items = mutableListOf<Any>()

        when (currentFilter) {

            ContentFilter.ALL -> {

                // PEOPLE — SHOW ALL
                if (results.people.isNotEmpty()) {
                    items.add("PEOPLE_HEADER")
                    items.addAll(results.people)
                }

                // CHATS — SHOW ALL (below PEOPLE)
                if (results.chats.isNotEmpty()) {
                    items.add("CHATS_HEADER")
                    items.addAll(results.chats)
                }

                // SHORTS — SHOW ALL
                if (results.shorts.isNotEmpty()) {
                    items.add("SHORTS_HEADER")
                    items.addAll(results.shorts)
                }

                // FEED — SHOW ALL
                if (results.feedPosts.isNotEmpty()) {
                    items.add("FEED_HEADER")
                    items.addAll(results.feedPosts)
                }

                // BUSINESS — SHOW ALL
                if (results.business.isNotEmpty()) {
                    items.add("BUSINESS_HEADER")
                    items.addAll(results.business)
                }
            }

            ContentFilter.SHORTS -> {
                if (results.shorts.isNotEmpty()) {
                    items.add("SHORTS_HEADER")
                    items.addAll(results.shorts)
                }
            }

            ContentFilter.FEED -> {
                if (results.feedPosts.isNotEmpty()) {
                    items.add("FEED_HEADER")
                    items.addAll(results.feedPosts)
                }
            }

            ContentFilter.PEOPLE -> {
                if (results.people.isNotEmpty()) {
                    items.add("PEOPLE_HEADER")
                    items.addAll(results.people)
                }
            }

            ContentFilter.CHATS -> {
                if (results.chats.isNotEmpty()) {
                    items.add("CHATS_HEADER")
                    items.addAll(results.chats)
                }
            }

            ContentFilter.BUSINESS -> {
                if (results.business.isNotEmpty()) {
                    items.add("BUSINESS_HEADER")
                    items.addAll(results.business)
                }
            }


        }

        if (items.isEmpty()) {
            binding.noResultsText.visibility = View.VISIBLE
            searchAdapter.showNoResults()
        } else {
            binding.noResultsText.visibility = View.GONE
            searchAdapter.submitList(items)
        }
    }

    private fun displayUserContentResults(results: SearchResults) {
        val items = mutableListOf<Any>()

        if (results.allPosts.isNotEmpty()) {
            items.add("USER_CONTENT_HEADER")
            items.addAll(results.allPosts)
        }

        if (items.isEmpty()) {
            binding.noResultsText.visibility = View.VISIBLE
            searchAdapter.showNoResults()
        } else {
            binding.noResultsText.visibility = View.GONE
            searchAdapter.submitList(items)
        }
    }

    private fun initSearchResults() {

        // Create real LocalStorage instance
        val localStorage = LocalStorage(this@SearchAllUserNameActivity)

        searchAdapter = SearchUserNameAdapter(
            localStorage = localStorage,
            onUserClicked = { author ->
                Log.d("SearchResults", "User clicked: @${author.account.username}")
                addUserToRecent(author.toRecentUser())

                // Open user profile instead of switching to user context
                openUserProfile(author)
            },
            onPostClicked = { post ->
                Log.d("SearchResults", "Post clicked: ${post._id}")

            }
        )

        binding.searchResultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@SearchAllUserNameActivity)
            adapter = searchAdapter
            setHasFixedSize(false)
        }
    }

    @OptIn(UnstableApi::class)
    private fun openUserProfile(author: Author) {
        val intent = Intent(this, OtherUserProfileAccount::class.java).apply {
            putExtra("extra_user_id", author.owner)  // Account ID
            putExtra("extra_user_name", "${author.firstName} ${author.lastName}".trim())
            putExtra("extra_username", author.account.username)
            putExtra("extra_avatar_url", author.account.avatar.url)
            putExtra("user_full_name", "${author.firstName} ${author.lastName}".trim())
        }
        startActivity(intent)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Handle back button click
        binding.backButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadRecentUsers() {
        lifecycleScope.launch {
            try {
                val recentUsers = withContext(Dispatchers.IO) {
                    recentUserViewModel.getRecentUsers()
                }

                if (recentUsers.isNotEmpty()) {
                    searchAdapter.showRecentUsers(recentUsers.map { it.toAuthor() })
                    binding.noResultsText.visibility = View.GONE
                } else {
                    searchAdapter.submitList(emptyList())
                }
            } catch (e: Exception) {
                Log.e("SearchUsers", "Error loading recent users: ${e.message}")
                searchAdapter.submitList(emptyList())
            }
        }
    }

    private fun addUserToRecent(user: RecentUser) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                recentUserViewModel.addRecentUser(user)
            } catch (e: Exception) {
                Log.e("SearchUsers", "Error adding recent user: ${e.message}")
            }
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
    }

    override fun onBackPressed() {
        if (currentSearchContext != SearchContext.GLOBAL) {
            resetToGlobalSearch()
        } else {
            super.onBackPressed()
        }
    }
}


data class SearchResults(
    val allPosts: List<Post> = emptyList(),
    val shorts: List<Post> = emptyList(),
    val feedPosts: List<Post> = emptyList(),
    val people: List<Author> = emptyList(),
    val chats: List<DialogEntity> = emptyList(),  // Changed from List<Chat>
    val business: List<Post> = emptyList()
)

// Extension function to convert RecentUser to Author
fun RecentUser.toAuthor(): Author {
    return Author(
        __v = 0,
        _id = this.id,
        account = Account(
            _id = this.id,
            avatar = Avatar(
                _id = "",
                localPath = "",
                url = this.avatar
            ),
            createdAt = "",
            email = "",
            updatedAt = "",
            username = this.name
        ),
        bio = "",
        countryCode = "",
        coverImage = CoverImage(
            _id = "",
            localPath = "",
            url = "https://via.placeholder.com/800x450.png"
        ),
        createdAt = "",
        dob = "",
        firstName = this.name.split(" ").firstOrNull() ?: this.name,
        lastName = this.name.split(" ").drop(1).joinToString(" "),
        location = "",
        owner = this.id,
        phoneNumber = "",
        updatedAt = ""
    )
}

// Extension function to convert Author to RecentUser
fun Author.toRecentUser(): RecentUser {
    return RecentUser(
        id = this._id,
        name = "${this.firstName} ${this.lastName}".trim().ifEmpty { this.account.username },
        avatar = this.account.avatar.url,
        lastSeen = Date(),
        online = false,
        dateAdded = Date()
    )
}


class SearchUserNameAdapter(
    private val localStorage: LocalStorage,
    private val onUserClicked: (Author) -> Unit,
    private val onPostClicked: (Post) -> Unit = {},
    private val onChatClicked: (DialogEntity) -> Unit = {}
) : ListAdapter<Any, RecyclerView.ViewHolder>(SearchDiffCallback())
{

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_USER = 1
        private const val TYPE_LOADING = 2
        private const val TYPE_NO_RESULTS = 3
        private const val TYPE_FEED = 4
        private const val TYPE_SEE_ALL = 8
        private const val TYPE_CHAT = 9
        private const val TYPE_BUSINESS = 10
        private const val TYPE_NO_BUSINESS = 11
    }

    fun showRecentUsers(authors: List<Author>) {
        submitList(listOf("RECENT_HEADER") + authors)
    }


    fun showLoading() {
        submitList(List(10) { "LOADING" })
    }

    fun showNoResults() {
        submitList(emptyList())
    }

    override fun getItemViewType(position: Int): Int = when (val item = getItem(position)) {
        "RECENT_HEADER", "SEARCH_HEADER", "PEOPLE_HEADER",
        "SHORTS_HEADER", "FEED_HEADER", "BUSINESS_HEADER",
        "CHATS_HEADER", "USER_CONTENT_HEADER" -> TYPE_HEADER
        is Author -> TYPE_USER
        "LOADING" -> TYPE_LOADING
        "NO_RESULTS" -> TYPE_NO_RESULTS
        "SEE_ALL_PEOPLE", "SEE_ALL_SHORTS", "SEE_ALL_POSTS" -> TYPE_SEE_ALL
        is Post -> TYPE_FEED
        is DialogEntity -> TYPE_CHAT
        is GetBusinessProfileById -> TYPE_BUSINESS
        "NO_BUSINESS" -> TYPE_NO_BUSINESS
        else -> -1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> {
                val view = inflater.inflate(R.layout.search_section_header, parent, false)
                HeaderViewHolder(view)
            }
            TYPE_USER -> {
                val view = inflater.inflate(R.layout.people_search_list, parent, false)
                PeopleViewHolder(view)
            }
            TYPE_LOADING -> {
                val view = inflater.inflate(R.layout.shimmer_search_user, parent, false)
                LoadingViewHolder(view)
            }
            TYPE_FEED -> {
                val view = inflater.inflate(R.layout.search_post_item, parent, false)
                FeedViewHolder(view)
            }
            TYPE_SEE_ALL -> {
                val view = inflater.inflate(R.layout.search_see_all_item, parent, false)
                SeeAllViewHolder(view)
            }
            TYPE_CHAT -> {  // Add this case
                val view = inflater.inflate(R.layout.chats_item_layout, parent, false)
                ChatViewHolder(view, localStorage)
            }
            TYPE_BUSINESS -> {
                val view = inflater.inflate(com.uyscuti.social.business.R.layout.profile_fragment, parent, false)
                BusinessViewHolder(view)
            }
            TYPE_NO_BUSINESS -> {
                val view = inflater.inflate(R.layout.no_business_profile_item, parent, false)
                NoBusinessViewHolder(view)
            }

            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                val headerType = getItem(position) as String
                holder.bind(headerType)
            }
            is PeopleViewHolder -> {
                val author = getItem(position) as Author
                holder.bind(author, onUserClicked)
            }
            is LoadingViewHolder -> {
                holder.showLoading()
            }
            is FeedViewHolder -> {
                val post = getItem(position) as Post
                holder.bind(post, onPostClicked)
            }
            is SeeAllViewHolder -> {
                val type = getItem(position) as String
                holder.bind(type)
            }
            is ChatViewHolder -> {
                val dialogEntity = getItem(position) as DialogEntity
                holder.bind(dialogEntity, onChatClicked)
            }
            is BusinessViewHolder -> {
                val business = getItem(position) as GetBusinessProfileById
                holder.bind(business)
            }
            is NoBusinessViewHolder -> {
                val username = getItem(position) as String
                holder.bind(username)
            }
        }
    }

    // HeaderViewHolder
    private class HeaderViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val headerText: TextView = view.findViewById(R.id.headerText)

        fun bind(headerType: String) {
            headerText.text = when (headerType) {
                "RECENT_HEADER" -> "Recent Searches"
                "SEARCH_HEADER" -> "Search Results"
                "PEOPLE_HEADER" -> "People"
                "SHORTS_HEADER" -> "Shorts"
                "FEED_HEADER" -> "Posts"
                "BUSINESS_HEADER" -> "Business"
                "CHATS_HEADER" -> "Chats"
                "USER_CONTENT_HEADER" -> "Content"
                else -> ""
            }
        }
    }

    // Updated PeopleViewHolder to display first and last name properly
    private class PeopleViewHolder(private val itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatar: ImageView = itemView.findViewById(R.id.avatar)
        private val fullNameText: TextView = itemView.findViewById(R.id.full_name) // bold - top
        private val usernameText: TextView = itemView.findViewById(R.id.name) // lighter - bottom

        fun bind(author: Author, listener: (Author) -> Unit) {
            Glide.with(itemView.context)
                .load(author.account.avatar.url)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .placeholder(R.drawable.flash21)
                .error(R.drawable.flash21)
                .into(avatar)

            val fullName = buildString {
                if (author.firstName.isNotEmpty()) append(author.firstName)
                if (author.lastName.isNotEmpty()) {
                    if (isNotEmpty()) append(" ")
                    append(author.lastName)
                }
            }.trim()

            fullNameText.text = if (fullName.isNotEmpty()) fullName else author.account.username
            usernameText.text = "@${author.account.username}"

            // Click opens profile
            itemView.setOnClickListener { listener(author) }

            // Long press for search context (optional)
            itemView.setOnLongClickListener {
                // Call switchToUserContext if you want to keep that feature
                true
            }
        }
    }

    // Updated FeedViewHolder to show author's full name
    private class FeedViewHolder(private val itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatar: ImageView = itemView.findViewById(R.id.authorAvatar)
        private val nameText: TextView = itemView.findViewById(R.id.authorName) // bold full name
        private val usernameText: TextView = itemView.findViewById(R.id.authorUsername) // lighter text
        private val contentText: TextView = itemView.findViewById(R.id.postContent)
        private val postImage: ImageView? = itemView.findViewById(R.id.postImage)

        fun bind(post: Post, listener: (Post) -> Unit) {
            Glide.with(itemView.context)
                .load(post.author.account.avatar.url)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .placeholder(R.drawable.flash21)
                .error(R.drawable.flash21)
                .into(avatar)

            // Full name
            val fullName = buildString {
                if (post.author.firstName.isNotEmpty()) append(post.author.firstName)
                if (post.author.lastName.isNotEmpty()) {
                    if (isNotEmpty()) append(" ")
                    append(post.author.lastName)
                }
            }.trim()

            nameText.text = if (fullName.isNotEmpty()) fullName else post.author.account.username
            usernameText.text = "@${post.author.account.username}"

            // Content
            contentText.text = if (post.isBusinessPost == true) {
                "${post.content}\n\nPrice: MWK ${post.businessDetails?.price ?: "N/A"}"
            } else post.content

            // First image if available
            val firstFile = post.files?.firstOrNull()
            if (firstFile != null && firstFile.url.isNotEmpty()) {
                postImage?.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(firstFile.url)
                    .centerCrop()
                    .placeholder(R.drawable.flash21)
                    .into(postImage!!)
            } else {
                postImage?.visibility = View.GONE
            }

            itemView.setOnClickListener { listener(post) }
        }
    }


    private class ChatViewHolder(
        private val itemView: View,
        private val localStorage: LocalStorage
    ) : RecyclerView.ViewHolder(itemView)
    {

        private val chatAvatar: ImageView = itemView.findViewById(R.id.chatAvatar)
        private val fullNameText: TextView = itemView.findViewById(R.id.chatName)
        private val usernameText: TextView = itemView.findViewById(R.id.lastMessage)

        // Original bind method for chat items
        fun bind(dialogEntity: DialogEntity, onChatClicked: (DialogEntity) -> Unit) {

            val myUserId = localStorage.getUserId()
            val otherUser = dialogEntity.users.firstOrNull { it.id != myUserId }

            if (otherUser != null) {
                val parts = otherUser.name.split("|")
                val fullName = parts.getOrNull(0)?.trim().orEmpty()
                val username = parts.getOrNull(1)?.trim() ?: fullName

                fullNameText.text = fullName.ifEmpty { username }
                usernameText.text = "@$username"

                Glide.with(itemView.context)
                    .load(otherUser.avatar)
                    .circleCrop()
                    .placeholder(R.drawable.flash21)
                    .error(R.drawable.flash21)
                    .into(chatAvatar)
            }

            itemView.setOnClickListener {
                MessagesActivity.open(
                    context = itemView.context,
                    dialogName = dialogEntity.dialogName,
                    dialog = dialogEntity.toDialog(localStorage),
                    temporally = false,
                    productReference = ""
                )
            }
        }

        // NEW: Bind method for showing the searched contact at the top
        fun bindContact(userEntity: UserEntity, onContactClicked: (UserEntity) -> Unit) {
            val parts = userEntity.name.split("|")
            val fullName = parts.getOrNull(0)?.trim().orEmpty()
            val username = parts.getOrNull(1)?.trim() ?: fullName

            fullNameText.text = fullName.ifEmpty { username }
            usernameText.text = "@$username"

            Glide.with(itemView.context)
                .load(userEntity.avatar)
                .circleCrop()
                .placeholder(R.drawable.flash21)
                .error(R.drawable.flash21)
                .into(chatAvatar)

            itemView.setOnClickListener {
                onContactClicked(userEntity)
            }
        }

        // NEW: Bind method for section headers (Messages, Groups, etc.)
        fun bindSectionHeader(sectionTitle: String) {
            // Use the same layout but style it as a header
            chatAvatar.visibility = View.GONE
            fullNameText.text = sectionTitle
            fullNameText.textSize = 14f
            fullNameText.setTextColor(itemView.context.getColor(android.R.color.darker_gray))
            usernameText.visibility = View.GONE
            itemView.setBackgroundColor(itemView.context.getColor(android.R.color.transparent))
            itemView.isClickable = false
            itemView.setPadding(16, 20, 16, 8)
        }

        // NEW: Bind method for chat/group where the person appears
        fun bindChatWithPerson(dialogEntity: DialogEntity, searchedPersonName: String) {
            val myUserId = localStorage.getUserId()

            // Show the chat/group name
            fullNameText.text = dialogEntity.dialogName

            // Show last message or indication that person is in this chat
            val lastMsg = dialogEntity.lastMessage?.text ?: "Chat with $searchedPersonName"
            usernameText.text = lastMsg

            // Load chat/group avatar
            Glide.with(itemView.context)
                .load(dialogEntity.dialogPhoto)
                .circleCrop()
                .placeholder(R.drawable.flash21)
                .error(R.drawable.flash21)
                .into(chatAvatar)

            itemView.setOnClickListener {
                MessagesActivity.open(
                    context = itemView.context,
                    dialogName = dialogEntity.dialogName,
                    dialog = dialogEntity.toDialog(localStorage),
                    temporally = false,
                    productReference = ""
                )
            }
        }

        private fun DialogEntity.toDialog(
            localStorage: LocalStorage
        ): com.uyscuti.sharedmodule.data.model.Dialog {

            val myUserId = localStorage.getUserId()
            val otherUser = users.firstOrNull { it.id != myUserId }

            val usersList = ArrayList<com.uyscuti.sharedmodule.data.model.User>()
            otherUser?.let { usersList.add(it.toUser()) }

            val message = lastMessage?.toMessage()
            val dialogName = usersList.firstOrNull()?.name ?: this.dialogName

            return com.uyscuti.sharedmodule.data.model.Dialog(
                this.id,
                dialogName,
                this.dialogPhoto,
                usersList,
                message,
                this.unreadCount
            )
        }

        private fun UserEntity.toUser(): com.uyscuti.sharedmodule.data.model.User {
            val username = if (name.contains("|")) name.split("|")[1].trim() else name
            return com.uyscuti.sharedmodule.data.model.User(
                id,
                username,
                avatar,
                online,
                lastSeen
            )
        }

        private fun MessageEntity.toMessage(): com.uyscuti.sharedmodule.data.model.Message {
            val username = if (user.name.contains("|")) user.name.split("|")[1].trim() else user.name
            val msgUser = com.uyscuti.sharedmodule.data.model.User(
                user.id,
                username,
                user.avatar,
                user.online,
                user.lastSeen
            )
            return com.uyscuti.sharedmodule.data.model.Message(
                id,
                msgUser,
                text,
                Date(createdAt)
            )
        }
    }

    inner class BusinessViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Using exact IDs from profile_fragment.xml
        private val viewPager: ViewPager2 = itemView.findViewById(R.id.viewPager)
        private val dotsIndicator: WormDotsIndicator = itemView.findViewById(R.id.worm_dots_indicator)
        private val businessName: EditText = itemView.findViewById(com.uyscuti.social.business.R.id.business_name)
        private val businessType: EditText = itemView.findViewById(com.uyscuti.social.business.R.id.business_type)
        private val descriptionEdit: EditText = itemView.findViewById(com.uyscuti.social.business.R.id.description_edit)
        private val email: EditText = itemView.findViewById(com.uyscuti.social.business.R.id.email)
        private val phoneNumber: EditText = itemView.findViewById(com.uyscuti.social.business.R.id.phonenumber_edit_text)
        private val address: EditText = itemView.findViewById(com.uyscuti.social.business.R.id.address)
        private val businessLocationToggle: SwitchCompat = itemView.findViewById(com.uyscuti.social.business.R.id.business_location_toggle)
        private val currentLocationToggle: SwitchCompat = itemView.findViewById(com.uyscuti.social.business.R.id.current_location_toggle)
        private val uploadButton: AppCompatButton = itemView.findViewById(com.uyscuti.social.business.R.id.upload_button)
        private val saveBtn: AppCompatButton = itemView.findViewById(com.uyscuti.social.business.R.id.save_btn)

        fun bind(business: GetBusinessProfileById) {
            // Populate with business data
            businessName.setText(business.businessName)
            businessType.setText(business.businessType)
            descriptionEdit.setText(business.businessDescription)
            email.setText(business.contact.email)
            phoneNumber.setText(business.contact.phoneNumber)
            address.setText(business.contact.address)

            businessLocationToggle.isChecked = business.location.businessLocation.enabled
            currentLocationToggle.isChecked = business.location.walkingBillboard.enabled

            // Setup ViewPager
            val mediaUrls = arrayListOf<String>()
            mediaUrls.add(business.backgroundPhoto.url)
            business.backgroundVideo?.url?.let { mediaUrls.add(it) }

            val adapter = MediaPagerAdapter(mediaUrls, itemView.context as Activity)
            viewPager.adapter = adapter
            dotsIndicator.attachTo(viewPager)

            // Make read-only
            businessName.isEnabled = false
            businessType.isEnabled = false
            descriptionEdit.isEnabled = false
            email.isEnabled = false
            phoneNumber.isEnabled = false
            address.isEnabled = false
            businessLocationToggle.isEnabled = false
            currentLocationToggle.isEnabled = false

            // Hide buttons
            uploadButton.visibility = View.GONE
            saveBtn.visibility = View.GONE
        }
    }

    inner class NoBusinessViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.no_business_message)

        @SuppressLint("SetTextI18n")
        fun bind(username: String) {
            messageText.text = "@$username does not have a Business Profile"
        }
    }

    // SeeAllViewHolder
    private class SeeAllViewHolder(private val itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val seeAllText: TextView = itemView.findViewById(R.id.seeAllText)

        fun bind(type: String) {
            seeAllText.text = when (type) {
                "SEE_ALL_PEOPLE" -> "See all people"
                "SEE_ALL_SHORTS" -> "See all shorts"
                "SEE_ALL_POSTS" -> "See all posts"
                else -> "See all"
            }
        }
    }

    // LoadingViewHolder
    private class LoadingViewHolder(private val itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val shimmer1: View? = itemView.findViewById(R.id.shimmer_view)
        private val shimmer2: View? = itemView.findViewById(R.id.shimmer_view2)

        fun showLoading() {
            shimmer1?.visibility = View.VISIBLE
            shimmer2?.visibility = View.VISIBLE
        }
    }

    // DiffCallback
    private class SearchDiffCallback : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return when {
                oldItem is Author && newItem is Author -> oldItem._id == newItem._id
                oldItem is Post && newItem is Post -> oldItem._id == newItem._id
                else -> oldItem::class == newItem::class && oldItem == newItem
            }
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean = oldItem == newItem
    }
}