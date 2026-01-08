package com.uyscuti.social.circuit.User_Interface.OtherImportantProfileThings


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.GestureDetector
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.colormoon.readmoretextview.ReadMoreTextView
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.stream.MalformedJsonException
import com.uyscuti.sharedmodule.MessagesActivity
import com.uyscuti.sharedmodule.User_Interfaces.OtherUserProfile.OtherUserProfileAccount
import com.uyscuti.sharedmodule.adapter.feed.FeedAdapter
import com.uyscuti.sharedmodule.adapter.feed.FeedAdapter.Companion.getCachedFollowingList
import com.uyscuti.sharedmodule.adapter.feed.FeedAdapter.Companion.getCachedFollowingUsernames
import com.uyscuti.sharedmodule.adapter.feed.OnFeedClickListener
import com.uyscuti.sharedmodule.adapter.feed.TAG
import com.uyscuti.sharedmodule.adapter.feed.feed.multiple_files.FeedMixedFilesViewAdapter
import com.uyscuti.sharedmodule.adapter.feed.feed.multiple_files.FeedRepostViewFileAdapter
import com.uyscuti.sharedmodule.adapter.feed.feed.multiple_files.OnMultipleFilesClickListener
import com.uyscuti.sharedmodule.data.model.Dialog
import com.uyscuti.sharedmodule.data.model.Message
import com.uyscuti.sharedmodule.data.model.shortsmodels.OtherUsersProfile
import com.uyscuti.sharedmodule.databinding.BottomDialogForShareBinding
import com.uyscuti.sharedmodule.model.GoToUserProfileFragment
import com.uyscuti.sharedmodule.model.ShortsFollowButtonClicked
import com.uyscuti.sharedmodule.presentation.RecentUserViewModel
import com.uyscuti.sharedmodule.ui.fragments.feed.feedviewfragments.Fragment_Original_Post_With_Repost_Inside
import com.uyscuti.sharedmodule.ui.fragments.feed.feedviewfragments.Fragment_Original_Post_Without_Repost_Inside
import com.uyscuti.sharedmodule.ui.fragments.feed.feedviewfragments.editRepost.Fragment_Edit_Post_To_Repost
import com.uyscuti.sharedmodule.ui.fragments.feed.feedviewfragments.feedRepost.Tapped_Files_In_The_Container_View_Fragment
import com.uyscuti.sharedmodule.utils.FollowingManager
import com.uyscuti.social.business.CatalogueDetailsActivity
import com.uyscuti.social.business.databinding.BusinessPostLayoutBinding
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.ActivitySearchAllUserNameBinding
import com.uyscuti.social.core.common.data.room.entity.DialogEntity
import com.uyscuti.social.network.api.retrofit.interfaces.IFlashapi
import com.uyscuti.social.core.common.data.room.entity.RecentUser
import com.uyscuti.social.core.common.data.room.entity.UserEntity
import com.uyscuti.social.network.api.models.User
import com.uyscuti.social.network.api.response.posts.Account
import com.uyscuti.social.network.api.response.posts.Author
import com.uyscuti.social.network.api.response.posts.Avatar
import com.uyscuti.social.network.api.response.posts.CoverImage
import com.uyscuti.social.network.api.response.posts.File
import com.uyscuti.social.network.api.response.posts.FileType
import com.uyscuti.social.network.api.response.posts.Post
import com.uyscuti.social.network.api.response.posts.RepostedUser
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
import java.text.SimpleDateFormat
import java.util.Locale
import com.uyscuti.social.core.common.data.room.entity.MessageEntity
import com.uyscuti.social.business.viewmodel.business.BusinessPostsViewModel
import com.uyscuti.social.core.common.data.room.entity.FollowUnFollowEntity
import com.uyscuti.social.network.api.models.Chat
import com.uyscuti.social.network.api.response.allFeedRepostsPost.BookmarkRequest
import com.uyscuti.social.network.api.response.allFeedRepostsPost.BookmarkResponse
import com.uyscuti.social.network.api.response.allFeedRepostsPost.CommentCountResponse
import com.uyscuti.social.network.api.response.allFeedRepostsPost.LikeRequest
import com.uyscuti.social.network.api.response.allFeedRepostsPost.LikeResponse
import com.uyscuti.social.network.api.response.allFeedRepostsPost.RepostResponse
import com.uyscuti.social.network.api.response.allFeedRepostsPost.RetrofitClient
import com.uyscuti.social.network.api.response.allFeedRepostsPost.ShareResponse
import com.uyscuti.social.network.api.response.posts.AccountB
import com.uyscuti.social.network.api.response.posts.AuthorB
import com.uyscuti.social.network.api.response.posts.AuthorX
import com.uyscuti.social.network.api.response.posts.AvatarB
import com.uyscuti.social.network.api.response.posts.BackgroundPhoto
import com.uyscuti.social.network.api.response.posts.BusinessPost
import com.uyscuti.social.network.api.response.posts.BusinessProfile
import com.uyscuti.social.network.api.response.posts.Duration
import com.uyscuti.social.network.api.response.posts.OriginalPost
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.TimeZone
import kotlin.math.abs



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


@AndroidEntryPoint
class SearchAllUserNameActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchAllUserNameBinding
    private lateinit var searchAdapter: SearchUserNameAdapter
    private var searchJob: Job? = null
    private lateinit var businessViewModel: BusinessPostsViewModel
    private var currentSearchContext = SearchContext.GLOBAL
    private var currentFilter = ContentFilter.ALL
    private var selectedUserId: String? = null
    private var selectedUsername: String? = null
    private val recentUserViewModel: RecentUserViewModel by viewModels()

    // ===== CACHE ALL DATA HERE - LOAD ONCE =====
    private var cachedPeople: List<Author> = emptyList()
    private var cachedShorts: List<Post> = emptyList()
    private var cachedFeedPosts: List<Post> = emptyList()
    private var cachedChats: List<DialogEntity> = emptyList()
    private var cachedBusinessPosts: List<Post> = emptyList()
    private var isDataLoaded = false
    private var isLoadingData = false

    private val feedClickListener = object : OnFeedClickListener {
        override fun likeUnLikeFeed(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {}
        override fun feedCommentClicked(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {}
        override fun feedFavoriteClick(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {}
        override fun moreOptionsClick(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {}
        override fun feedFileClicked(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {}
        override fun feedRepostFileClicked(position: Int, data: OriginalPost) {}
        override fun feedShareClicked(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {}
        override fun followButtonClicked(followUnFollowEntity: FollowUnFollowEntity, followButton: AppCompatButton) {}
        override fun feedRepostPost(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {}
        override fun feedRepostPostClicked(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {}
        override fun feedClickedToOriginalPost(position: Int, originalPostId: String) {}
        override fun onImageClick() {}
    }

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

        businessViewModel = ViewModelProvider(this)[BusinessPostsViewModel::class.java]

        setupToolbar()
        initSearchResults()
        setupSearch()
        setupFilters()
        loadRecentUsers()

        // ===== LOAD ALL DATA ONCE ON STARTUP =====
        loadAllDataOnce()
    }


    // =====  LOAD ALL DATA ONCE =====
    private fun loadAllDataOnce() {
        if (isDataLoaded || isLoadingData) return

        isLoadingData = true
        lifecycleScope.launch {
            try {
                Log.d("SearchOptimized", "Loading all data once...")

                // Show loading indicator
                binding.noResultsText.text = "Loading data..."
                binding.noResultsText.visibility = View.VISIBLE

                withContext(Dispatchers.IO) {
                    // Load all data in parallel
                    val deferredPeople = async { loadAllPeople() }
                    val deferredShorts = async { loadAllShorts() }
                    val deferredFeed = async { loadAllFeedPosts() }
                    val deferredChats = async { loadAllChats() }
                    val deferredBusiness = async { loadAllBusinessPosts() }

                    // Wait for all to complete
                    cachedPeople = deferredPeople.await()
                    cachedShorts = deferredShorts.await()
                    cachedFeedPosts = deferredFeed.await()
                    cachedChats = deferredChats.await()
                    cachedBusinessPosts = deferredBusiness.await()
                }

                isDataLoaded = true
                isLoadingData = false
                binding.noResultsText.visibility = View.GONE

                Log.d("SearchOptimized", "Data loaded - People: ${cachedPeople.size}, Shorts: ${cachedShorts.size}, Feed: ${cachedFeedPosts.size}, Chats: ${cachedChats.size}, Business: ${cachedBusinessPosts.size}")

            } catch (e: Exception) {
                Log.e("SearchOptimized", "Error loading data: ${e.message}", e)
                isLoadingData = false
                binding.noResultsText.text = "Error loading data. Pull to refresh."
                binding.noResultsText.visibility = View.VISIBLE
            }
        }
    }

    // ===== LOAD FUNCTIONS (CALLED ONCE) =====
    private suspend fun loadAllPeople(): List<Author> {
        return try {
            val response = apiService.getUsers()
            val users = response.body()?.data ?: emptyList()

            users.mapNotNull { user ->
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
                    Log.e("LoadPeople", "Error: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("LoadPeople", "Error: ${e.message}")
            emptyList()
        }
    }

    private suspend fun loadAllShorts(): List<Post> {
        return try {
            val response = apiService.getShorts("1")
            val shorts = response.body()?.data?.posts?.posts ?: emptyList()
            shorts.map { it.toPostsPost() }.filter { post ->
                post.fileTypes.any { it.fileType.equals("video", ignoreCase = true) }
            }
        } catch (e: Exception) {
            Log.e("LoadShorts", "Error: ${e.message}")
            emptyList()
        }
    }

    private suspend fun loadAllFeedPosts(): List<Post> {
        return try {
            val response = apiService.getUserFeedForSearch("", "1", "500")
            val posts = response.body()?.data?.data?.posts ?: emptyList()
            posts.filter { post ->
                post.contentType.equals("text", ignoreCase = true) ||
                        (post.contentType.equals("mixed_files", ignoreCase = true) &&
                                post.fileTypes.all { !it.fileType.equals("video", ignoreCase = true) })
            }
        } catch (e: Exception) {
            Log.e("LoadFeed", "Error: ${e.message}")
            emptyList()
        }
    }

    private suspend fun loadAllChats(): List<DialogEntity> {
        return try {
            val response = apiService.getChats(limit = 200, offset = 0)
            val chats = response.body()?.data ?: emptyList()

            chats.map { chat ->
                val enrichedUsers = chat.participants.mapNotNull { participant ->
                    try {
                        val profileResponse = apiService.getOtherUsersProfileByUsername(participant.username ?: "")
                        val profileData = profileResponse.body()?.data

                        val firstName = profileData?.firstName ?: ""
                        val lastName = profileData?.lastName ?: ""
                        val username = profileData?.account?.username ?: participant.username ?: ""
                        val fullName = "${firstName} ${lastName}".trim()
                        val combinedName = if (fullName.isNotEmpty()) "$fullName|$username" else username

                        UserEntity(
                            id = profileData?._id ?: participant._id,
                            name = combinedName,
                            avatar = participant.avatar?.url ?: "",
                            lastSeen = participant.lastseen ?: Date(),
                            online = false
                        )
                    } catch (e: Exception) {
                        UserEntity(
                            id = participant._id,
                            name = participant.username ?: "Unknown",
                            avatar = participant.avatar?.url ?: "",
                            lastSeen = participant.lastseen ?: Date(),
                            online = false
                        )
                    }
                }

                val firstParticipant = enrichedUsers.firstOrNull()
                DialogEntity(
                    id = chat._id,
                    dialogPhoto = firstParticipant?.avatar ?: chat.participants.firstOrNull()?.avatar?.url ?: "",
                    dialogName = firstParticipant?.name?.split("|")?.firstOrNull() ?: chat.name ?: "Chat",
                    users = enrichedUsers,
                    lastMessage = null,
                    unreadCount = 0
                )
            }
        } catch (e: Exception) {
            Log.e("LoadChats", "Error: ${e.message}")
            emptyList()
        }
    }

    private suspend fun loadAllBusinessPosts(): List<Post> {
        return try {
            val response = apiService.getBusinessPost("1")
            val businessPosts = response.body()?.data?.posts ?: emptyList()

            businessPosts.mapNotNull { businessPost ->
                try {
                    val profileResponse = apiService.getOtherUsersProfileByUsername(businessPost.userDetails.username ?: "")
                    val profileData = profileResponse.body()?.data
                    val firstName = profileData?.firstName ?: ""
                    val lastName = profileData?.lastName ?: ""
                    val username = profileData?.account?.username ?: businessPost.userDetails.username ?: ""

                    Post(
                        __v = businessPost.__v ?: 0,
                        _id = businessPost._id,
                        author = Author(
                            __v = 0,
                            _id = businessPost.owner,
                            account = Account(
                                _id = businessPost.owner,
                                avatar = Avatar("", "", businessPost.userDetails.avatar ?: ""),
                                createdAt = businessPost.createdAt ?: "",
                                email = "",
                                updatedAt = businessPost.updatedAt ?: "",
                                username = username
                            ),
                            bio = "",
                            countryCode = "",
                            coverImage = CoverImage("", "", "https://via.placeholder.com/800x450.png"),
                            createdAt = businessPost.createdAt ?: "",
                            dob = "",
                            firstName = firstName,
                            lastName = lastName,
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
                        files = ArrayList(businessPost.images?.map { File("", "", "", it, "image") } ?: emptyList()),
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
                        businessDetails = BusinessPost(
                            _id = businessPost._id,
                            owner = businessPost.owner,
                            catalogue = businessPost.catalogue ?: "",
                            itemName = businessPost.itemName ?: "",
                            description = businessPost.description ?: "",
                            features = businessPost.features ?: emptyList(),
                            images = businessPost.images ?: emptyList(),
                            price = businessPost.price?.toString() ?: "0",
                            createdAt = businessPost.createdAt ?: "",
                            updatedAt = businessPost.updatedAt ?: "",
                            __v = businessPost.__v ?: 0,
                            author = AuthorB(
                                _id = businessPost.owner,
                                firstName = firstName,
                                lastName = lastName,
                                account = AccountB(
                                    _id = businessPost.owner,
                                    avatar = AvatarB(businessPost.userDetails.avatar ?: "", "", ""),
                                    username = username
                                )
                            ),
                            businessProfile = BusinessProfile("", businessPost.userDetails.username ?: "", "", "", BackgroundPhoto(""))
                        ),
                        isFavorited = null,
                        favorites = null
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("LoadBusiness", "Error: ${e.message}")
            emptyList()
        }
    }

    // ===== INSTANT FILTER FROM CACHE =====
    private fun performSearch(query: String) {
        if (!isDataLoaded) {
            Log.d("SearchOptimized", "Data not loaded yet, showing loading...")
            return
        }

        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            delay(50) // Minimal delay for smooth typing

            Log.d("SearchOptimized", "Filtering cached data for: '$query'")

            val filteredResults = withContext(Dispatchers.Default) {
                filterCachedData(query)
            }

            displaySearchResults(filteredResults)
        }
    }

    // ===== FILTER CACHED DATA (INSTANT) =====
    private fun filterCachedData(query: String): SearchResults {
        if (query.isEmpty()) {
            return SearchResults(
                allPosts = cachedShorts + cachedFeedPosts,
                shorts = cachedShorts,
                feedPosts = cachedFeedPosts,
                people = cachedPeople,
                chats = cachedChats,
                business = cachedBusinessPosts
            )
        }

        val queryLower = query.lowercase()

        val filteredPeople = cachedPeople.filter { author ->
            author.account.username.lowercase().contains(queryLower) ||
                    author.firstName.lowercase().contains(queryLower) ||
                    author.lastName.lowercase().contains(queryLower) ||
                    author.account.email.lowercase().contains(queryLower)
        }

        val filteredShorts = cachedShorts.filter { post ->
            (post.content?.lowercase()?.contains(queryLower) == true) ||
                    post.author.account.username.lowercase().contains(queryLower) ||
                    post.author.firstName.lowercase().contains(queryLower) ||
                    post.author.lastName.lowercase().contains(queryLower) ||
                    post.tags.any { it?.toString()?.lowercase()?.contains(queryLower) == true }
        }

        val filteredFeed = cachedFeedPosts.filter { post ->
            (post.content?.lowercase()?.contains(queryLower) == true) ||
                    post.author.account.username.lowercase().contains(queryLower) ||
                    post.author.firstName.lowercase().contains(queryLower) ||
                    post.author.lastName.lowercase().contains(queryLower) ||
                    post.tags.any { it?.toString()?.lowercase()?.contains(queryLower) == true }
        }

        val filteredChats = cachedChats.filter { chat ->
            chat.dialogName.lowercase().contains(queryLower) ||
                    chat.users.any { it.name.lowercase().contains(queryLower) }
        }

        val filteredBusiness = cachedBusinessPosts.filter { post ->
            (post.businessDetails?.itemName?.lowercase()?.contains(queryLower) == true) ||
                    (post.businessDetails?.description?.lowercase()?.contains(queryLower) == true) ||
                    post.author.account.username.lowercase().contains(queryLower)
        }

        return SearchResults(
            allPosts = filteredShorts + filteredFeed + filteredBusiness,
            shorts = filteredShorts,
            feedPosts = filteredFeed,
            people = filteredPeople,
            chats = filteredChats,
            business = filteredBusiness
        )
    }

    // Keep rest of the methods unchanged...
    private fun initSearchResults() {
        val localStorage = LocalStorage(this@SearchAllUserNameActivity)
        searchAdapter = SearchUserNameAdapter(
            feedClickListener = feedClickListener,
            viewModel = businessViewModel,
            localStorage = localStorage,
            onUserClicked = { author ->
                addUserToRecent(author.toRecentUser())
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

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.backButton.setOnClickListener { onBackPressed() }
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
                searchJob = lifecycleScope.launch {
                    delay(50)
                    performSearch(query)
                }
            }
        })
    }

    private fun setupFilters() {
        binding.filterChipsGroup.visibility = View.GONE
        currentFilter = ContentFilter.ALL
        binding.chipAll.isChecked = true

        binding.chipAll.setOnClickListener { applyFilter(ContentFilter.ALL) }
        binding.chipPeople.setOnClickListener { applyFilter(ContentFilter.PEOPLE) }
        binding.chipFeed.setOnClickListener { applyFilter(ContentFilter.FEED) }
        binding.chipShorts.setOnClickListener { applyFilter(ContentFilter.SHORTS) }
        binding.chipChats.setOnClickListener { applyFilter(ContentFilter.CHATS) }
        binding.chipBusiness.setOnClickListener { applyFilter(ContentFilter.BUSINESS) }
    }

    private fun applyFilter(filter: ContentFilter) {
        currentFilter = filter
        searchAdapter.currentFilter = filter

        binding.chipAll.isChecked = (filter == ContentFilter.ALL)
        binding.chipPeople.isChecked = (filter == ContentFilter.PEOPLE)
        binding.chipFeed.isChecked = (filter == ContentFilter.FEED)
        binding.chipShorts.isChecked = (filter == ContentFilter.SHORTS)
        binding.chipChats.isChecked = (filter == ContentFilter.CHATS)
        binding.chipBusiness.isChecked = (filter == ContentFilter.BUSINESS)

        val query = binding.searchEditText.text.toString().trim()
        if (query.isNotEmpty()) {
            performSearch(query)
        }
    }

    private fun displaySearchResults(results: SearchResults) {
        val items = mutableListOf<Any>()

        when (currentFilter) {
            ContentFilter.ALL -> {
                if (results.people.isNotEmpty()) {
                    items.add("PEOPLE_HEADER")
                    items.addAll(results.people)
                }
                if (results.chats.isNotEmpty()) {
                    items.add("CHATS_HEADER")
                    items.addAll(results.chats)
                }
                if (results.shorts.isNotEmpty()) {
                    items.add("SHORTS_HEADER")
                    items.addAll(results.shorts)
                }
                if (results.feedPosts.isNotEmpty()) {
                    items.add("FEED_HEADER")
                    items.addAll(results.feedPosts)
                }
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
            updateLayoutManager(items)
        }
    }

    private fun updateLayoutManager(items: List<Any>) {
        val hasShorts = items.any {
            it is Post && it.contentType.equals("mixed_files", ignoreCase = true) &&
                    it.fileTypes.any { fileType -> fileType.fileType.equals("video", ignoreCase = true) }
        }

        when {
            currentFilter == ContentFilter.SHORTS -> {
                val gridLayoutManager = GridLayoutManager(this, 3)
                gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return when (searchAdapter.getItemViewType(position)) {
                            SearchUserNameAdapter.TYPE_SHORTS_GRID -> 1
                            else -> 3
                        }
                    }
                }
                binding.searchResultsRecyclerView.layoutManager = gridLayoutManager
            }
            currentFilter == ContentFilter.BUSINESS -> {
                val gridLayoutManager = GridLayoutManager(this, 2)
                gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return when (searchAdapter.getItemViewType(position)) {
                            SearchUserNameAdapter.TYPE_BUSINESS_GRID -> 1
                            else -> 2
                        }
                    }
                }
                binding.searchResultsRecyclerView.layoutManager = gridLayoutManager
            }
            currentFilter == ContentFilter.ALL && hasShorts -> {
                val gridLayoutManager = GridLayoutManager(this, 3)
                gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return when (searchAdapter.getItemViewType(position)) {
                            SearchUserNameAdapter.TYPE_SHORTS_GRID -> 1
                            SearchUserNameAdapter.TYPE_BUSINESS -> 3
                            else -> 3
                        }
                    }
                }
                binding.searchResultsRecyclerView.layoutManager = gridLayoutManager
            }
            else -> {
                binding.searchResultsRecyclerView.layoutManager = LinearLayoutManager(this)
            }
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
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
                searchAdapter.submitList(emptyList())
            }
        }
    }


    @OptIn(UnstableApi::class)
    private fun openUserProfile(author: Author) {
        val intent = Intent(this, OtherUserProfileAccount::class.java).apply {
            putExtra("extra_user_id", author.owner)
            putExtra("extra_user_name", "${author.firstName} ${author.lastName}".trim())
            putExtra("extra_username", author.account.username)
            putExtra("extra_avatar_url", author.account.avatar.url)
            putExtra("user_full_name", "${author.firstName} ${author.lastName}".trim())
        }
        startActivity(intent)
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

    override fun onBackPressed() {
        if (currentSearchContext != SearchContext.GLOBAL) {
            resetToGlobalSearch()
        } else {
            super.onBackPressed()
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
                    mimeType = if (it.url.endsWith(".mp4", true)) "video/mp4" else "image/jpeg"
                )
            }
        )

        // Extract duration from video files - will be populated later when video is loaded
        val extractedDuration = images.mapNotNull { image ->
            if (image.url.endsWith(".mp4", true)) {
                Duration(
                    duration = "0", // Will be updated when video loads
                    fileId = image._id
                )
            } else null
        }

        Log.d("PostConversion", "Extracted duration for short ${this._id}: $extractedDuration")

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
            duration = extractedDuration,
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

    fun com.uyscuti.social.network.api.response.getallshorts.Avatar.toPostsAvatar(): Avatar {
        return Avatar(
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

    fun com.uyscuti.social.network.api.response.getallshorts.CoverImage.toPostsCoverImage(): CoverImage {
        return CoverImage(
            _id = this._id,
            url = this.url,
            localPath = this.localPath
        )
    }


}




class SearchUserNameAdapter(

    private val feedClickListener: com.uyscuti.social.circuit.adapter.feed.OnFeedClickListener,
    private val viewModel: BusinessPostsViewModel,
    private val localStorage: LocalStorage,
    private val onUserClicked: (Author) -> Unit,
    private val onPostClicked: (Post) -> Unit = {},
    private val onChatClicked: (DialogEntity) -> Unit = {},

    ) : androidx.recyclerview.widget.ListAdapter<Any, RecyclerView.ViewHolder>(SearchDiffCallback())
{

    companion object {

        private const val TYPE_CHAT = 1
        private const val TYPE_FEED = 2
        private const val TYPE_USER = 3
        private const val TYPE_HEADER = 4
        private const val TYPE_LOADING = 5
        private const val TYPE_SEE_ALL = 6
        private const val TYPE_TEXT_FEED = 7
        internal const val TYPE_BUSINESS = 8
        private const val TYPE_NO_RESULTS = 9
        private const val TYPE_NO_BUSINESS = 10
        private const val TYPE_REPOST_POST = 11
        internal const val TYPE_SHORTS_GRID = 12
        internal const val TYPE_BUSINESS_GRID = 13
        private const val TYPE_MIXED_FEED_FILES = 14
        private const val TYPE_REPOST_WITH_NEW_FILES = 15

    }

    interface OnFeedClickListener {


        fun likeUnLikeFeed(
            position: Int,
            data: com.uyscuti.social.network.api.response.posts.Post
        )


        fun feedCommentClicked(
            position: Int,
            data: com.uyscuti.social.network.api.response.posts.Post
        )

        fun feedFavoriteClick(
            position: Int,
            data: com.uyscuti.social.network.api.response.posts.Post
        )


        fun moreOptionsClick(
            position: Int,
            data: com.uyscuti.social.network.api.response.posts.Post
        )

        fun feedFileClicked(
            position: Int,
            data: com.uyscuti.social.network.api.response.posts.Post
        )

        fun feedRepostFileClicked(
            position: Int, data: OriginalPost
        )

        fun feedShareClicked(
            position: Int, data: com.uyscuti.social.network.api.response.posts.Post
        )


        fun followButtonClicked(
            followUnFollowEntity: FollowUnFollowEntity,
            followButton: AppCompatButton
        )

        fun feedRepostPost(
            position: Int,
            data: com.uyscuti.social.network.api.response.posts.Post
        )

        fun feedRepostPostClicked(position: Int, data: com.uyscuti.social.network.api.response.posts.Post)

        fun feedClickedToOriginalPost(position: Int, originalPostId: String)
        fun onImageClick()


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

    var currentFilter: com.uyscuti.social.circuit.user_interface.ContentFilter = com.uyscuti.social.circuit.user_interface.ContentFilter.ALL


    // Add these properties to handle following list
    private val followingUserIds = mutableSetOf<String>()


    override fun getItemViewType(position: Int): Int = when (val item = getItem(position)) {
        "RECENT_HEADER", "SEARCH_HEADER", "PEOPLE_HEADER",
        "SHORTS_HEADER", "FEED_HEADER", "BUSINESS_HEADER",
        "CHATS_HEADER", "USER_CONTENT_HEADER" -> TYPE_HEADER

        is Author -> TYPE_USER
        "LOADING" -> TYPE_LOADING
        "NO_RESULTS" -> TYPE_NO_RESULTS
        "SEE_ALL_PEOPLE", "SEE_ALL_SHORTS", "SEE_ALL_POSTS" -> TYPE_SEE_ALL

        is Post -> {
            // Business posts
            if (item.isBusinessPost == true) {
                if (currentFilter == com.uyscuti.social.circuit.user_interface.ContentFilter.BUSINESS) {
                    TYPE_BUSINESS_GRID
                } else {
                    TYPE_BUSINESS
                }
            }
            // Shorts (video grid)
            else if (item.contentType.equals("mixed_files", ignoreCase = true) &&
                item.fileTypes.any { it.fileType.equals("video", ignoreCase = true) }) {
                TYPE_SHORTS_GRID
            }
            // FEED POSTS - Use FeedAdapter logic
            else {
                // Determine which feed view type based on post content
                when {
                    // Text-only post
                    item.contentType.equals("text", ignoreCase = true) &&
                            item.files.isEmpty() -> TYPE_TEXT_FEED

                    // Repost with new files added
                    item.originalPost?.isNotEmpty() == true &&
                            item.files.isNotEmpty() -> TYPE_REPOST_WITH_NEW_FILES

                    // Regular repost
                    item.originalPost?.isNotEmpty() == true -> TYPE_REPOST_POST

                    // Mixed files (images/videos/audio)
                    item.contentType.equals("mixed_files", ignoreCase = true) ||
                            item.files.isNotEmpty() -> TYPE_MIXED_FEED_FILES

                    // Default to text feed
                    else -> TYPE_TEXT_FEED
                }
            }
        }

        is DialogEntity -> TYPE_CHAT
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

            // ============ REUSE FEEDADAPTER LAYOUTS ============

            TYPE_TEXT_FEED -> {
                // Reuse FeedAdapter's text-only layout
                val view = inflater.inflate(R.layout.feed_original_text_post_adapter, parent, false)
                FeedTextOnyViewHolder(view)
            }

            TYPE_MIXED_FEED_FILES -> {
                val itemView = inflater.inflate(
                    com.uyscuti.sharedmodule.R.layout.feed_mixed_files_original_post_adapter_view, parent, false
                )
                FeedPostViewHolder(itemView)
            }

            TYPE_REPOST_POST -> {
                val itemView = inflater.inflate(
                    com.uyscuti.sharedmodule.R.layout.feed_mixed_files_original_post_with_repost_view, parent, false
                )
                FeedRepostViewHolder(itemView)
            }

            TYPE_REPOST_WITH_NEW_FILES -> {
                val itemView = inflater.inflate(
                    com.uyscuti.sharedmodule.R.layout.feed_mixed_files_new_post_with_reposted_files_inside_adapter,
                    parent, false
                )
                FeedNewPostWithRepostInsideFilesPostViewHolder(itemView)
            }

            // ============ END FEEDADAPTER LAYOUTS ============

            TYPE_SHORTS_GRID -> {
                val view = inflater.inflate(R.layout.search_shorts_grid_item, parent, false)
                ShortsGridViewHolder(view)
            }
            TYPE_SEE_ALL -> {
                val view = inflater.inflate(R.layout.search_see_all_item, parent, false)
                SeeAllViewHolder(view)
            }
            TYPE_CHAT -> {
                val view = inflater.inflate(R.layout.chats_item_layout, parent, false)
                ChatViewHolder(view, localStorage)
            }
            TYPE_BUSINESS -> {
                val view = inflater.inflate(
                    com.uyscuti.social.business.R.layout.business_post_layout, parent, false)
                BusinessViewHolder(view, viewModel)
            }
            TYPE_BUSINESS_GRID -> {
                val view = inflater.inflate(R.layout.search_business_grid_item, parent, false)
                BusinessGridViewHolder(view)
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

            // ============ BIND FEEDADAPTER VIEWHOLDERS ============

            is FeedTextOnyViewHolder -> {
                val post = getItem(position) as Post
                holder.render(post)
            }

            is FeedPostViewHolder -> {
                val post = getItem(position) as Post
                holder.render(post)
            }

            is FeedRepostViewHolder -> {
                val post = getItem(position) as Post
                holder.render(post)
            }

            is FeedNewPostWithRepostInsideFilesPostViewHolder -> {
                val post = getItem(position) as Post
                holder.render(post)
            }

            // ============ END FEEDADAPTER BINDINGS ============

            is ShortsGridViewHolder -> {
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
                val post = getItem(position) as Post
                holder.bind(post)
            }
            is BusinessGridViewHolder -> {
                val post = getItem(position) as Post
                holder.bind(post, onPostClicked)
            }
            is NoBusinessViewHolder -> {
                val username = getItem(position) as String
                holder.bind(username)
            }
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

    // HeaderViewHolder
    private class HeaderViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val headerText: TextView = view.findViewById(R.id.headerText)

        fun bind(headerType: String) {
            headerText.text = when (headerType) {
                "RECENT_HEADER" -> "Recent Searches"
                "SEARCH_HEADER" -> "Search Results"
                "PEOPLE_HEADER" -> "People"
                "SHORTS_HEADER" -> "Shorts"
                "FEED_HEADER" -> "Feed"
                "BUSINESS_HEADER" -> "Business"
                "CHATS_HEADER" -> "Chats"
                "USER_CONTENT_HEADER" -> "Content"
                else -> ""
            }
        }
    }

    // ShortsGridViewHolder for grid display
    private class ShortsGridViewHolder(private val itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val videoThumbnail: ImageView = itemView.findViewById(R.id.videoThumbnail)
        private val playIcon: ImageView = itemView.findViewById(R.id.playIcon)
        private val viewsCount: TextView = itemView.findViewById(R.id.viewsCount)
        private val likesCount: TextView = itemView.findViewById(R.id.likesCount)
        private val durationBadge: TextView = itemView.findViewById(R.id.durationBadge)
        private val authorAvatar: ImageView = itemView.findViewById(R.id.authorAvatar)
        private val authorName: TextView = itemView.findViewById(R.id.authorName)
        private val timePosted: TextView = itemView.findViewById(R.id.timePosted)

        fun bind(post: Post, listener: (Post) -> Unit) {
            // Load video thumbnail with rounded corners
            val thumbnailUrl = post.thumbnail?.firstOrNull()?.thumbnailUrl ?: post.files?.firstOrNull()?.url

            Glide.with(itemView.context)
                .load(thumbnailUrl)
                .centerCrop()
                .placeholder(R.drawable.flash21)
                .error(R.drawable.flash21)
                .into(videoThumbnail)

            // Load author avatar - perfectly circular
            Glide.with(itemView.context)
                .load(post.author.account.avatar.url)
                .circleCrop()
                .placeholder(R.drawable.flash21)
                .error(R.drawable.flash21)
                .into(authorAvatar)

            // Set author name
            val fullName = buildString {
                if (post.author.firstName.isNotEmpty()) append(post.author.firstName)
                if (post.author.lastName.isNotEmpty()) {
                    if (isNotEmpty()) append(" ")
                    append(post.author.lastName)
                }
            }.trim()

            authorName.text = if (fullName.isNotEmpty()) fullName else "@${post.author.account.username}"

            // Format views count (using comments as proxy for views)
            viewsCount.text = formatCount(post.comments)

            // Format likes count
            likesCount.text = formatCount(post.likes)

            // Calculate and display time posted
            timePosted.text = getTimeAgo(post.createdAt)

            // Load actual video duration from the video file
            loadVideoDuration(post)

            // Click listener
            itemView.setOnClickListener { listener(post) }
        }

        private fun loadVideoDuration(post: Post) {
            val videoFile = post.files?.firstOrNull {
                it.url.endsWith(".mp4", ignoreCase = true)
            }

            if (videoFile != null) {
                try {
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(videoFile.url, HashMap<String, String>())

                    val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    val durationMs = durationStr?.toLongOrNull() ?: 0L
                    val durationSeconds = (durationMs / 1000).toInt()

                    retriever.release()

                    if (durationSeconds > 0) {
                        durationBadge.text = formatDuration(durationSeconds)
                        durationBadge.visibility = View.VISIBLE
                    } else {
                        durationBadge.visibility = View.GONE
                    }
                } catch (e: Exception) {
                    Log.e("ShortsGrid", "Error loading video duration: ${e.message}")
                    durationBadge.visibility = View.GONE
                }
            } else {
                durationBadge.visibility = View.GONE
            }
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

        private fun formatDuration(seconds: Int): String {
            val hours = seconds / 3600
            val minutes = (seconds % 3600) / 60
            val secs = seconds % 60

            return when {
                hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, secs)
                minutes > 0 -> String.format("%d:%02d", minutes, secs)
                else -> String.format("0:%02d", secs)
            }
        }

        private fun getTimeAgo(createdAt: String): String {
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                val date = sdf.parse(createdAt) ?: return ""

                val now = System.currentTimeMillis()
                val diff = now - date.time

                val seconds = diff / 1000
                val minutes = seconds / 60
                val hours = minutes / 60
                val days = hours / 24
                val weeks = days / 7
                val months = days / 30
                val years = days / 365

                when {
                    years > 0 -> "${years}y"
                    months > 0 -> "${months}mo"
                    weeks > 0 -> "${weeks}w"
                    days > 0 -> "${days}d"
                    hours > 0 -> "${hours}h"
                    minutes > 0 -> "${minutes}m"
                    else -> "${seconds}s"
                }
            } catch (e: Exception) {
                ""
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

    private class ChatViewHolder(private val itemView: View, private val localStorage: LocalStorage) : RecyclerView.ViewHolder(itemView) {

        private val chatAvatar: ImageView = itemView.findViewById(R.id.chatAvatar)
        private val fullNameText: TextView = itemView.findViewById(R.id.fullName)
        private val usernameText: TextView = itemView.findViewById(R.id.userName)

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
        ): Dialog {

            val myUserId = localStorage.getUserId()
            val otherUser = users.firstOrNull { it.id != myUserId }

            val usersList = ArrayList<com.uyscuti.sharedmodule.data.model.User>()
            otherUser?.let { usersList.add(it.toUser()) }

            val message = lastMessage?.toMessage()
            val dialogName = usersList.firstOrNull()?.name ?: this.dialogName

            return Dialog(
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

        private fun MessageEntity.toMessage(): Message {
            val username = if (user.name.contains("|")) user.name.split("|")[1].trim() else user.name
            val msgUser = com.uyscuti.sharedmodule.data.model.User(
                user.id,
                username,
                user.avatar,
                user.online,
                user.lastSeen
            )
            return Message(
                id,
                msgUser,
                text,
                Date(createdAt)
            )
        }
    }

    inner class BusinessViewHolder(itemView: View, private val viewModel: BusinessPostsViewModel) : RecyclerView.ViewHolder(itemView) {

        private val binding = BusinessPostLayoutBinding.bind(itemView)

        @OptIn(UnstableApi::class)
        fun bind(post: Post) {
            val author = post.author
            val account = author.account

            // Load avatar
            Glide.with(itemView.context)
                .load(account.avatar.url)
                .circleCrop()
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(binding.ivUserAvatar)

            // Full name
            val fullName = "${author.firstName} ${author.lastName}".trim()
            binding.tvUsername.text = fullName.ifEmpty { "@${account.username}" }

            // Username handle below full name
            if (binding.root.findViewById<TextView>(R.id.tv_user_handle) == null) {
                // Dynamically add if not in XML
                val tvHandle = TextView(itemView.context).apply {
                    id = R.id.tv_user_handle
                    text = "@${account.username}"
                    setTextColor(ContextCompat.getColor(itemView.context, R.color.gray_second))
                    textSize = 12f
                }
                (binding.tvUsername.parent as LinearLayout).addView(tvHandle, 1)
            } else {
                binding.root.findViewById<TextView>(R.id.tv_user_handle).text = "@${account.username}"
            }

            // Post time
            binding.tvPostTime.text = getTimeAgo(post.createdAt)

            // Catalogue info
            binding.tvItemTitle.text = post.businessDetails?.itemName ?: post.content.take(50)
            binding.tvDescription.text = post.businessDetails?.description ?: post.content
            binding.tvItemPrice.text = "MWK ${post.businessDetails?.price ?: "0"}"

            // Media section
            val images = post.businessDetails?.images ?: post.files.mapNotNull { it.url }
            binding.businessRecycler.layoutManager =
                if (images.size <= 2) GridLayoutManager(itemView.context, images.size)
                else StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

            binding.businessRecycler.adapter = BusinessMediaAdapter(images, itemView.context) { position ->
                // Media click handler
            }

            binding.tvMediaCounter.visibility = if (images.size > 4) View.VISIBLE else View.GONE

            // ------------------ BUTTON CLICKS ------------------

            // Follow
            binding.btnFollow.setOnClickListener {
                CoroutineScope(Dispatchers.Main).launch {
                    viewModel.followUnfollowBusinessPostOwner(author._id)
                }
            }

            // Like
            binding.llLike.setOnClickListener {
                CoroutineScope(Dispatchers.Main).launch {
                    viewModel.likeUnlikeBusinessPost(post._id)
                }
            }

            // Comment
            binding.llComment.setOnClickListener {
                // Open comment UI / activity
            }

            // Bookmark
            binding.llBookmark.setOnClickListener {
                viewModel.bookmarkUnBookmarkBusinessPost(post._id)
            }

            // Repost / Offer
            binding.sendOffer.setOnClickListener {
                // Handle send offer logic
            }

            // Share
            binding.llRepost.setOnClickListener {
                // Handle share intent
            }

            // Whole item click
            itemView.setOnClickListener {
                val intent = Intent(itemView.context, CatalogueDetailsActivity::class.java)
                intent.putExtra("catalogue", post)
                itemView.context.startActivity(intent)
            }
        }

        private fun getTimeAgo(createdAt: String): String {
            // Implement your time formatting here
            return ""
        }
    }

    inner class BusinessMediaAdapter(private val mediaUrls: List<String>, private val context: Context, private val onMediaClick: (position: Int) -> Unit) : RecyclerView.Adapter<BusinessMediaAdapter.MediaViewHolder>() {

        inner class MediaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imageView: ImageView = view.findViewById(R.id.mediaImageView)

            init {
                view.setOnClickListener {
                    val pos = bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        onMediaClick(pos)
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_business_media_simple, parent, false)
            return MediaViewHolder(view)
        }

        override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
            Glide.with(context)
                .load(mediaUrls[position])
                .centerCrop()
                .placeholder(R.drawable.flash21)
                .error(R.drawable.flash21)
                .into(holder.imageView)
        }

        override fun getItemCount(): Int = mediaUrls.size
    }

    inner class BusinessGridViewHolder(private val itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val businessImage: ImageView = itemView.findViewById(R.id.businessItemImage)
        private val businessPrice: TextView = itemView.findViewById(R.id.businessItemPrice)
        private val businessName: TextView = itemView.findViewById(R.id.businessItemName)
        private val businessLocation: TextView = itemView.findViewById(R.id.businessItemLocation)
        private val justListedBadge: TextView = itemView.findViewById(R.id.justListedBadge)

        fun bind(post: Post, listener: (Post) -> Unit) {
            // Load image
            val firstImage = post.files?.firstOrNull()?.url
            Glide.with(itemView.context)
                .load(firstImage)
                .centerCrop()
                .placeholder(R.drawable.flash21)
                .error(R.drawable.flash21)
                .into(businessImage)

            // Price
            businessPrice.text = extractPrice(post)

            // Name - use businessDetails if available
            businessName.text = post.businessDetails?.itemName ?: post.content?.trim()?.take(60) ?: "Business Item"

            // Location - from author's account
            businessLocation.text = if (post.author.location.isNotBlank()) {
                post.author.location
            } else {
                "Lilongwe, Malawi"
            }

            // Just listed badge
            justListedBadge.visibility = if (isRecentlyListed(post.createdAt)) View.VISIBLE else View.GONE

            // Click listener to open CatalogueDetailsActivity
            itemView.setOnClickListener {
                openCatalogueDetails(post)
                listener(post)
            }
        }

        @OptIn(UnstableApi::class)
        private fun openCatalogueDetails(post: Post) {
            val context = itemView.context

            // Convert the Post to the businessposts.Post format for CatalogueDetailsActivity
            val cataloguePost = com.uyscuti.social.network.api.response.business.response.post.Post(
                _id = post._id,
                __v = post.__v,

                // Business details
                itemName = post.businessDetails?.itemName ?: post.content?.trim()?.take(60) ?: "Business Item",
                price = post.businessDetails?.price ?: extractPriceNumeric(post).toString(),
                description = post.businessDetails?.description ?: post.content ?: "",
                catalogue = post.businessDetails?.catalogue ?: "",
                features = post.businessDetails?.features ?: emptyList(),

                // Images
                images = post.files?.mapNotNull { it.url } ?: emptyList(),

                // Timestamps
                createdAt = post.createdAt,
                updatedAt = post.updatedAt,

                // User details
                userDetails = com.uyscuti.social.network.api.response.business.response.post.UserDetails(
                    username = post.author.account.username,
                    avatar = post.author.account.avatar.url,
                    createdAt = post.author.createdAt,
                    updatedAt = post.author.updatedAt
                ),
                owner = post.author._id,

                // Interaction states
                isLiked = post.isLiked,
                isBookmarked = post.isBookmarked,
                isFollowing = post.isFollowing,

                // Metrics
                likes = post.likes,
                comments = post.comments,
                bookmarkCount = post.bookmarkCount
            )

            val intent = Intent(context, CatalogueDetailsActivity::class.java).apply {
                putExtra("catalogue", cataloguePost)
                putExtra("position", 0)
            }
            context.startActivity(intent)
        }

        private fun extractPriceNumeric(post: Post): Double {
            // First check businessDetails
            post.businessDetails?.price?.let {
                return it.toDoubleOrNull() ?: 0.0
            }

            // Otherwise parse from content
            val content = post.content ?: return 0.0
            val patterns = listOf(
                """MWK\s*(\d{1,3}(?:,\d{3})*)""".toRegex(),
                """K\s*(\d{1,3}(?:,\d{3})*)""".toRegex(),
                """\$\s*(\d{1,3}(?:,\d{3})*)""".toRegex()
            )

            for (pattern in patterns) {
                pattern.find(content)?.groupValues?.get(1)?.let {
                    return it.replace(",", "").toDoubleOrNull() ?: 0.0
                }
            }

            return 0.0
        }

        private fun extractPrice(post: Post): String {
            // First check businessDetails
            post.businessDetails?.price?.let {
                return formatPrice(it)
            }

            // Otherwise parse from content
            val content = post.content ?: return "Contact for price"
            val patterns = listOf(
                """MWK\s*(\d{1,3}(?:,\d{3})*)""".toRegex(),
                """K\s*(\d{1,3}(?:,\d{3})*)""".toRegex(),
                """\$\s*(\d{1,3}(?:,\d{3})*)""".toRegex()
            )

            for (pattern in patterns) {
                pattern.find(content)?.groupValues?.get(1)?.let {
                    return formatPrice(it.replace(",", ""))
                }
            }

            return "Contact for price"
        }

        private fun formatPrice(price: String): String {
            val numericPrice = price.toDoubleOrNull() ?: return "MWK$price"
            return when {
                numericPrice >= 1_000_000 -> "MWK${String.format("%.1fM", numericPrice / 1_000_000).replace(".0M", "M")}"
                numericPrice >= 1_000 -> "MWK${String.format("%,.0f", numericPrice)}"
                else -> "MWK${numericPrice.toInt()}"
            }
        }

        private fun isRecentlyListed(createdAt: String): Boolean {
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                val date = sdf.parse(createdAt) ?: return false
                System.currentTimeMillis() - date.time < 24 * 60 * 60 * 1000
            } catch (e: Exception) {
                false
            }
        }
    }

    inner class NoBusinessViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.no_business_message)

        @SuppressLint("SetTextI18n")
        fun bind(username: String) {
            messageText.text = "@$username does not have a Business Profile"
        }
    }

    inner class FeedTextOnyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {



        private val TAG = "FeedTextOnlyViewHolder"

        // UI Components
        // Header Section Views
        private val profileImageView: ImageView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.profileImageView)
        private val textView: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.textView)
        private val handerText: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.handerText)
        private val dateTime: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.date_time)
        private val followButton: AppCompatButton = itemView.findViewById(com.uyscuti.sharedmodule.R.id.followButton)
        private val moreOptionsButton: ImageView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.moreOptions)

        // Content Section Views
        private val caption: ReadMoreTextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.caption)
        private val tags: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.tags)

        // Media Section Views
        private val recyclerView: RecyclerView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.recyclerView)

        // Interaction Buttons
        private val likeButton: ImageView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.likeButtonIcon)
        private val commentButton: ImageView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.commentButtonIcon)
        private val favoriteButton: ImageView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.favoriteSection)
        private val repostedPost: ImageView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.repostPost)
        private val feedShare: ImageView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.shareButtonIcon)

        // Interaction Counters
        private val likesCount: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.likesCount)
        private val commentCount: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.commentCount)
        private val favoriteCounts: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.favoriteCounts)
        private val repostCount: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.repostCount)
        private val shareCount: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.shareCount)

        // Container Views
        private val feedTextLayoutContainer: ConstraintLayout = itemView.findViewById(com.uyscuti.sharedmodule.R.id.feedMixedFilesContainer)

        // State variables
        private var isFollowed = false
        private var totalTextComments = 0
        private var currentPost: com.uyscuti.social.network.api.response.posts.Post? = null
        private var totalTextLikesCounts = 0
        private var totalTextBookMarkCounts = 0
        private var totalTextShareCounts = 0
        private var totalTextRePostCounts = 0
        private var postClicked = false
        private var isFollowingUser = false

        private val com.uyscuti.social.network.api.response.posts.Post.safeCommentCount: Int
            get() = 0

        private val com.uyscuti.social.network.api.response.posts.Post.safeLikes: Int
            get() = likes

        private val com.uyscuti.social.network.api.response.posts.Post.safeBookmarkCount: Int
            get() = bookmarkCount

        private var com.uyscuti.social.network.api.response.posts.Post.safeRepostCount: Int
            get() = repostCount
            set(value) {
                repostCount = value
            }

        private var com.uyscuti.social.network.api.response.posts.Post.safeShareCount: Int
            get() = shareCount
            set(value) {
                shareCount = value
            }

        @OptIn(UnstableApi::class)
        @SuppressLint("SetTextI18n", "SimpleDateFormat", "SuspiciousIndentation")
        fun render(data: com.uyscuti.social.network.api.response.posts.Post) {

            data.isBusinessPost?.let {
                if(!it) {

                    // Store current post reference
                    currentPost = data

                    // Add null safety checks for author and account
                    val author = data.author
                    if (author == null) {
                        Log.e(TAG, "render: Author is null for post, skipping render")
                        return
                    }

                    val account = author.account
                    if (account == null) {
                        Log.e(TAG, "render: Account is null for author, skipping render")
                        return
                    }


                    val feedOwnerId = account._id

                    isFollowingUser = followingUserIds.contains(feedOwnerId)
                    Log.d(TAG, "render: User $feedOwnerId following status: $isFollowingUser")


                    val feedOwnerName = listOfNotNull(
                        author.firstName?.takeIf { it.isNotBlank() },
                        author.lastName?.takeIf { it.isNotBlank() }
                    ).joinToString(" ").trim()
                    val profilePicUrl = account.avatar?.url ?: ""
                    val feedOwnerUsername = account.username ?: ""

                    // Log all count values for debugging
                    logCountDebuggingInfo(data)

                    // Get all metric counts - Initialize properly
                    totalTextLikesCounts = getLikesCount(data)
                    totalTextComments = getCommentCount(data)
                    totalTextBookMarkCounts = getBookmarkCount(data)
                    totalTextShareCounts = getShareCount(data)
                    totalTextRePostCounts = getRepostCount(data)

                    Log.d(
                        TAG,
                        "render: Final counts - " +
                                "Comments: $totalTextComments," +
                                " Likes: $totalTextLikesCounts, " +
                                "Bookmarks: $totalTextBookMarkCounts," +
                                " Reposts: $totalTextRePostCounts, " +
                                "Shares: $totalTextShareCounts"
                    )

                    // Update all displays
                    updateAllMetricDisplays(
                        data,
                        totalTextComments,
                        totalTextLikesCounts,
                        totalTextBookMarkCounts,
                        totalTextRePostCounts,
                        totalTextShareCounts
                    )

                    // Update button states to reflect current data
                    updateLikeButtonUI(data.isLiked ?: false)
                    updateBookmarkButtonUI(data.isBookmarked ?: false)
                    updateRepostButtonAppearance(data.isReposted ?: false)

                    // Set username and profile image
                    setupUserProfile(data)

                    // Setup all UI components
                    setupProfileClickHandlers(feedOwnerId, feedOwnerName, feedOwnerUsername, profilePicUrl)
                    setupContentAndCaption(data)
                    setupInteractionButtons(data)
                    ensurePostClickability(data)
                    setupChildClickBubbling(data)
                    setupPostClickListeners(data)

                }
            }


        }

        private fun setupFollowButton(data: com.uyscuti.social.network.api.response.posts.Post) {
            val feedOwnerId = data.author?.account?._id ?: return
            val feedOwnerUsername = data.author?.account?.username ?: return
            val currentUserId = LocalStorage.getInstance(itemView.context).getUserId()

            // Check multiple sources for following status (by ID and username)
            val cachedFollowingList = getCachedFollowingList()
            val cachedFollowingUsernames = getCachedFollowingUsernames()

            val isUserFollowing = followingUserIds.contains(feedOwnerId) ||
                    cachedFollowingList.contains(feedOwnerId) ||
                    cachedFollowingUsernames.contains(feedOwnerUsername)

            Log.d(TAG, "setupFollowButton: Checking user $feedOwnerId (@$feedOwnerUsername)")
            Log.d(TAG, "  - Match by ID: ${followingUserIds.contains(feedOwnerId) || cachedFollowingList.contains(feedOwnerId)}")
            Log.d(TAG, "  - Match by username: ${cachedFollowingUsernames.contains(feedOwnerUsername)}")

            // Hide follow button if viewing own post OR already following
            if (feedOwnerId == currentUserId || isFollowingUser || isUserFollowing) {
                followButton.visibility = View.GONE
                Log.d(TAG, "setupFollowButton: Hidden for user $feedOwnerId (@$feedOwnerUsername) - Following: true")
                return
            }

            // Show follow button only for users we're NOT following
            followButton.visibility = View.VISIBLE
            followButton.text = "Follow"
            followButton.backgroundTintList = ContextCompat.getColorStateList(
                itemView.context,
                com.uyscuti.sharedmodule.R.color.blueJeans
            )

            Log.d(TAG, "setupFollowButton: Showing follow button for $feedOwnerId (@$feedOwnerUsername)")

            followButton.setOnClickListener {
                handleFollowButtonClick(feedOwnerId, feedOwnerUsername)
            }
        }

        @SuppressLint("SetTextI18n")
        private fun handleFollowButtonClick(feedOwnerId: String, username: String) {
            YoYo.with(Techniques.Pulse)
                .duration(300)
                .playOn(followButton)

            Log.d(TAG, "Follow button clicked for user: $feedOwnerId")

            isFollowed = !isFollowed
            val followEntity = FollowUnFollowEntity(feedOwnerId, isFollowed)

            if (isFollowed) {
                // Hide button immediately
                followButton.visibility = View.GONE

                // Add to adapter's following list AND persistent storage
                (bindingAdapter as? FeedAdapter)?.addToFollowing(feedOwnerId, username)

                // Also update via manager for consistency
                FollowingManager(itemView.context).addToFollowing(feedOwnerId)

                Log.d(TAG, "Now following user $feedOwnerId")
            } else {
                // Show button
                followButton.text = "Follow"
                followButton.visibility = View.VISIBLE

                // Remove from adapter's following list AND persistent storage
                (bindingAdapter as? FeedAdapter)?.removeFromFollowing(feedOwnerId, username)

                // Also update via manager for consistency
                FollowingManager(itemView.context).removeFromFollowing(feedOwnerId)

                Log.d(TAG, "Unfollowed user $feedOwnerId")
            }

            // Notify listener
            feedClickListener.followButtonClicked(followEntity, followButton)
            EventBus.getDefault().post(ShortsFollowButtonClicked(followEntity))
        }

        private fun setupPostClickListeners(data: com.uyscuti.social.network.api.response.posts.Post) {
            // Clear existing click listeners to avoid conflicts
            feedTextLayoutContainer.setOnClickListener(null)
            caption.setOnClickListener(null)
            tags.setOnClickListener(null)
            dateTime.setOnClickListener(null)

            // Set up main post container click
            feedTextLayoutContainer.setOnClickListener { view ->
                if (postClicked) return@setOnClickListener
                postClicked = true
                Log.d(TAG, "Main post container clicked")
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                navigateToOriginalPostWithoutRepostInside(data)
                view.postDelayed({ postClicked = false }, 1000)
            }

            // Set up child elements to bubble clicks to main container
            preventChildClickInterference(data)
        }

        private fun preventChildClickInterference(data: com.uyscuti.social.network.api.response.posts.Post) {
            val childViews = listOfNotNull(
                caption,
                tags,
                dateTime,
                textView
            )

            childViews.forEach { childView ->
                childView.setOnClickListener { view ->
                    Log.d(TAG, "Child element clicked, bubbling to main container")
                    feedTextLayoutContainer.performClick()
                }
            }
        }

        private fun setupChildClickBubbling(data: com.uyscuti.social.network.api.response.posts.Post) {
            val childViews = listOfNotNull(
                caption,
                tags,
                dateTime,
                textView
            )

            childViews.forEach { childView ->
                childView.setOnClickListener { view ->
                    Log.d(TAG, "Child element clicked, bubbling to main container")
                    feedTextLayoutContainer.performClick()
                }
            }
        }

        private fun ensurePostClickability(data: com.uyscuti.social.network.api.response.posts.Post) {
            feedTextLayoutContainer.isClickable = true
            feedTextLayoutContainer.isFocusable = true
            try {
                val typedValue = TypedValue()
                val context = itemView.context
                if (context.theme.resolveAttribute(
                        android.R.attr.selectableItemBackground,
                        typedValue, true
                    )) {
                    feedTextLayoutContainer.foreground = ContextCompat.getDrawable(context, typedValue.resourceId)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not set ripple background for main container: ${e.message}")
            }
            feedTextLayoutContainer.contentDescription = "Post, tap to view full post"
            feedTextLayoutContainer.elevation = 4f
            Log.d(TAG, "Post clickability ensured for post: ${data._id}")
        }

        private fun getCommentCount(data: com.uyscuti.social.network.api.response.posts.Post): Int {
            return when {
                data.comments != null -> {
                    Log.d(TAG, "getCommentCount: Using commentCount: ${data.comments}")
                    data.comments!!
                }
                data.safeCommentCount >= 0 -> {
                    Log.d(TAG, "getCommentCount: Using safeCommentCount: ${data.safeCommentCount}")
                    data.safeCommentCount
                }
                else -> {
                    Log.d(TAG, "getCommentCount: No valid comment count found, defaulting to 0")
                    0
                }
            }
        }

        private fun getLikesCount(data: com.uyscuti.social.network.api.response.posts.Post): Int {
            return when {
                data.likes >= 0 -> data.likes
                data.safeLikes >= 0 -> data.safeLikes
                else -> 0
            }
        }

        private fun getBookmarkCount(data: com.uyscuti.social.network.api.response.posts.Post): Int {
            return when {
                data.bookmarkCount >= 0 -> data.bookmarkCount
                data.safeBookmarkCount >= 0 -> data.safeBookmarkCount
                else -> 0
            }
        }

        private fun getRepostCount(data: com.uyscuti.social.network.api.response.posts.Post): Int {
            return when {
                data.safeRepostCount != null -> data.safeRepostCount!!
                data.safeRepostCount >= 0 -> data.safeRepostCount
                else -> 0
            }
        }

        private fun getShareCount(data: com.uyscuti.social.network.api.response.posts.Post): Int {
            return when {
                data.safeShareCount >= 0 -> data.safeShareCount
                data.safeShareCount >= 0 -> data.safeShareCount
                else -> 0
            }
        }

        private fun setupInteractionButtons(data: com.uyscuti.social.network.api.response.posts.Post) {
            setupLikeButton(data)
            setupBookmarkButton(data)
            setupCommentButton(data)
            setupRepostButton(data)
            setupShareButton(data)
            setupMoreOptionsButton(data)
            setupFollowButton(data)
        }

        private fun setupLikeButton(data: com.uyscuti.social.network.api.response.posts.Post) {
            Log.d(TAG, "Setting up like button - Initial state: isLiked=${data.isLiked}, likes=${data.likes}")
            updateLikeButtonUI(data.isLiked ?: false)
            updateMetricDisplay(likesCount, data.likes, "like")

            likeButton.setOnClickListener {
                if (!likeButton.isEnabled) return@setOnClickListener

                Log.d(TAG, "Like clicked for post: ${data._id}")
                Log.d(TAG, "Current state before toggle: isLiked=${data.isLiked}, likes=${data.likes}")

                val newLikeStatus = !(data.isLiked ?: false)
                val previousLikeStatus = data.isLiked ?: false
                val previousLikesCount = data.likes

                // Update data immediately for optimistic UI update
                data.isLiked = newLikeStatus
                data.likes = if (newLikeStatus) data.likes + 1 else maxOf(0, data.likes - 1)
                totalTextLikesCounts = data.likes

                Log.d(TAG, "New state after toggle: isLiked=${data.isLiked}, likes=${data.likes}")

                // Update UI immediately for better UX
                updateLikeButtonUI(newLikeStatus)
                updateMetricDisplay(likesCount, data.likes, "like")

                // Animation for like/unlike
                YoYo.with(if (newLikeStatus) Techniques.Tada else Techniques.Pulse)
                    .duration(300)
                    .repeat(1)
                    .playOn(likeButton)

                // Disable button during network call
                likeButton.isEnabled = false
                likeButton.alpha = 0.8f

                // Call likeUnLikeFeed safely
                try {
                    feedClickListener.likeUnLikeFeed(absoluteAdapterPosition, data)
                } catch (e: Exception) {
                    Log.e(TAG, "Error calling likeUnLikeFeed: ${e.message}")
                }

                // Make network call to sync like status
                val likeRequest = LikeRequest(newLikeStatus)
                RetrofitClient.likeService.toggleLike(data._id, likeRequest)
                    .enqueue(object : Callback<LikeResponse> {
                        override fun onResponse(call: Call<LikeResponse>, response: Response<LikeResponse>) {
                            likeButton.alpha = 1f
                            likeButton.isEnabled = true

                            if (response.isSuccessful) {
                                response.body()?.let { likeResponse ->
                                    Log.d(TAG, "Like API success - Server count: ${likeResponse.likesCount}")
                                    // Update likes count if significantly different
                                    if (likeResponse.likesCount != null &&
                                        abs(likeResponse.likesCount - data.likes) > 1
                                    ) {
                                        data.likes = likeResponse.likesCount
                                        totalTextLikesCounts = data.likes
                                        updateMetricDisplay(likesCount, data.likes, "like")
                                        Log.d(TAG, "Updated likes count from server: ${data.likes}")
                                    }
                                }
                            } else {
                                Log.e(TAG, "Like sync failed: ${response.code()}")
                                // Revert on actual API errors
                                if (response.code() != 200) {
                                    data.isLiked = previousLikeStatus
                                    data.likes = previousLikesCount
                                    totalTextLikesCounts = data.likes
                                    updateLikeButtonUI(previousLikeStatus)
                                    updateMetricDisplay(likesCount, data.likes, "like")
                                    Log.d(TAG, "Reverted to previous state: isLiked=${data.isLiked}, likes=${data.likes}")
                                }
                            }
                        }

                        override fun onFailure(call: Call<LikeResponse>, t: Throwable) {
                            likeButton.alpha = 1f
                            likeButton.isEnabled = true

                            // Handle JSON parsing errors separately
                            if (t is MalformedJsonException ||
                                t.message?.contains("MalformedJsonException") == true) {
                                Log.w(TAG, "Like API returned malformed JSON but operation likely succeeded - keeping UI state")
                                return
                            }

                            Log.e(TAG, "Like network error - reverting changes", t)
                            // Revert for network failures
                            data.isLiked = previousLikeStatus
                            data.likes = previousLikesCount
                            totalTextLikesCounts = data.likes
                            updateLikeButtonUI(previousLikeStatus)
                            updateMetricDisplay(likesCount, data.likes, "like")
                            Log.d(TAG, "Reverted to previous state after network error: isLiked=${data.isLiked}, likes=${data.likes}")
                        }
                    })
            }
        }

        private fun setupCommentButton(data: com.uyscuti.social.network.api.response.posts.Post) {

            commentButton.setOnClickListener {
                if (!commentButton.isEnabled) return@setOnClickListener
                Log.d(TAG, "Comment button clicked for post ${data._id}")

                YoYo.with(Techniques.Tada)
                    .duration(700)
                    .repeat(1)
                    .playOn(commentButton)

                feedClickListener.feedCommentClicked(absoluteAdapterPosition, data)
                commentButton.isEnabled = true
            }

            commentCount.setOnClickListener {
                if (!commentCount.isEnabled) return@setOnClickListener
                YoYo.with(Techniques.Tada)
                    .duration(700)
                    .repeat(1)
                    .playOn(commentCount)
                feedClickListener.feedCommentClicked(absoluteAdapterPosition, data)
            }
        }

        private fun setupBookmarkButton(data: com.uyscuti.social.network.api.response.posts.Post) {

            Log.d(TAG, "Setting up bookmark button - Initial state: isBookmarked=${data.isBookmarked}, bookmarkCount=${data.bookmarkCount}")
            updateBookmarkButtonUI(data.isBookmarked ?: false)
            updateMetricDisplay(favoriteCounts, data.bookmarkCount, "bookmark")

            favoriteButton.setOnClickListener {
                if (!favoriteButton.isEnabled) return@setOnClickListener

                Log.d(TAG, "Bookmark clicked for post: ${data._id}")
                Log.d(TAG, "Current state before toggle: isBookmarked=${data.isBookmarked}, bookmarkCount=${data.bookmarkCount}")

                val newBookmarkStatus = !(data.isBookmarked ?: false)
                val previousBookmarkStatus = data.isBookmarked ?: false
                val previousBookmarkCount = data.bookmarkCount

                // Update data immediately
                data.isBookmarked = newBookmarkStatus
                data.bookmarkCount = if (newBookmarkStatus) data.bookmarkCount + 1 else maxOf(0, data.bookmarkCount - 1)
                totalTextBookMarkCounts = data.bookmarkCount

                Log.d(TAG, "New state after toggle: isBookmarked=${data.isBookmarked}, bookmarkCount=${data.bookmarkCount}")

                // Update UI immediately for better UX
                updateBookmarkButtonUI(data.isBookmarked ?: false)
                updateMetricDisplay(favoriteCounts, data.bookmarkCount, "bookmark")

                // Animation
                YoYo.with(if (newBookmarkStatus) Techniques.Tada else Techniques.Pulse)
                    .duration(500)
                    .repeat(1)
                    .playOn(favoriteButton)

                // Disable button during network call
                favoriteButton.isEnabled = false
                favoriteButton.alpha = 0.8f

                val bookmarkRequest = BookmarkRequest(newBookmarkStatus)
                RetrofitClient.bookmarkService.toggleBookmark(data._id, bookmarkRequest)
                    .enqueue(object : Callback<BookmarkResponse> {
                        override fun onResponse(call: Call<BookmarkResponse>, response: Response<BookmarkResponse>) {
                            favoriteButton.alpha = 1f
                            favoriteButton.isEnabled = true

                            if (response.isSuccessful) {
                                response.body()?.let { bookmarkResponse ->
                                    Log.d(TAG, "Bookmark API success - Server count: ${bookmarkResponse.bookmarkCount}")
                                    if (abs(bookmarkResponse.bookmarkCount - data.bookmarkCount) > 1) {
                                        data.bookmarkCount = bookmarkResponse.bookmarkCount
                                        totalTextBookMarkCounts = data.bookmarkCount
                                        updateMetricDisplay(favoriteCounts, data.bookmarkCount, "bookmark")
                                        Log.d(TAG, "Updated bookmark count from server: ${data.bookmarkCount}")
                                    }
                                }
                            } else {
                                Log.e(TAG, "Bookmark sync failed: ${response.code()}")
                                // Only revert on actual HTTP errors (not 2xx status codes)
                                if (response.code() >= 400) {
                                    data.isBookmarked = previousBookmarkStatus
                                    data.bookmarkCount = previousBookmarkCount
                                    totalTextBookMarkCounts = data.bookmarkCount
                                    updateBookmarkButtonUI(data.isBookmarked ?: false)
                                    updateMetricDisplay(favoriteCounts, data.bookmarkCount, "bookmark")
                                    Log.d(TAG, "Reverted to previous state due to HTTP error: ${response.code()}")
                                }
                            }
                        }

                        override fun onFailure(call: Call<BookmarkResponse>, t: Throwable) {
                            favoriteButton.alpha = 1f
                            favoriteButton.isEnabled = true

                            // Handle JSON parsing errors separately - don't revert UI
                            if (t is MalformedJsonException ||
                                t.message?.contains("MalformedJsonException") == true ||
                                t.message?.contains("JsonReader.setStrictness") == true) {
                                Log.w(TAG, "Bookmark API returned malformed JSON but operation likely succeeded - keeping UI state")
                                // Don't revert the UI changes as the operation likely succeeded on the server
                                return
                            }

                            // Only revert for actual network failures
                            Log.e(TAG, "Bookmark network error - reverting changes", t)
                            data.isBookmarked = previousBookmarkStatus
                            data.bookmarkCount = previousBookmarkCount
                            totalTextBookMarkCounts = data.bookmarkCount
                            updateBookmarkButtonUI(data.isBookmarked ?: false)
                            updateMetricDisplay(favoriteCounts, data.bookmarkCount, "bookmark")
                            Log.d(TAG, "Reverted to previous state after network error: isBookmarked=${data.isBookmarked}, bookmarkCount=${data.bookmarkCount}")
                        }
                    })

                // Always notify the listener regardless of API status
                feedClickListener.feedFavoriteClick(absoluteAdapterPosition, data)
            }
        }

        private fun setupRepostButton(data: com.uyscuti.social.network.api.response.posts.Post) {

            totalTextRePostCounts = data.safeRepostCount
            updateMetricDisplay(repostCount, totalTextRePostCounts, "repost")
            updateRepostButtonAppearance(data.isReposted)

            repostedPost.setOnClickListener { view ->

                if (!repostedPost.isEnabled) return@setOnClickListener
                repostedPost.isEnabled = false

                try {

                    val wasReposted = data.isReposted
                    data.isReposted = !wasReposted
                    totalTextRePostCounts = if (data.isReposted) totalTextRePostCounts + 1 else maxOf(0, totalTextRePostCounts - 1)

                    data.repostCount = totalTextRePostCounts
                    updateMetricDisplay(repostCount, totalTextRePostCounts, "repost")
                    updateRepostButtonAppearance(data.isReposted)

                    YoYo.with(if (data.isReposted) Techniques.Tada else Techniques.Pulse)
                        .duration(700)
                        .playOn(repostedPost)
                    repostedPost.alpha = 0.8f

                    val apiCall = if (data.isReposted) {
                        RetrofitClient.repostService.incrementRepost(data._id)
                    } else {
                        RetrofitClient.repostService.decrementRepost(data._id)
                    }

                    apiCall.enqueue(object : Callback<RepostResponse> {

                        override fun onResponse(call: Call<RepostResponse>, response: Response<RepostResponse>) {
                            repostedPost.isEnabled = true
                            repostedPost.alpha = 1f

                            if (response.isSuccessful) {
                                response.body()?.let { repostResponse ->
                                    if (abs(repostResponse.repostCount - totalTextRePostCounts) > 1) {
                                        data.safeRepostCount = repostResponse.repostCount
                                        totalTextRePostCounts = repostResponse.repostCount
                                        updateMetricDisplay(repostCount, totalTextRePostCounts, "repost")
                                    }
                                }
                            } else {
                                Log.e(TAG, "Repost API failed: ${response.code()}")
                            }
                        }

                        override fun onFailure(call: Call<RepostResponse>, t: Throwable) {
                            repostedPost.isEnabled = true
                            repostedPost.alpha = 1f
                            Log.e(TAG, "Repost network error - will sync later", t)
                        }
                    })


                    feedClickListener.feedRepostPost(absoluteAdapterPosition, data)

                } catch (e: Exception) {
                    repostedPost.isEnabled = true
                    repostedPost.alpha = 1f
                    Log.e(TAG, "Exception in repost click listener", e)

                }
            }
        }

        private fun setupShareButton(data: com.uyscuti.social.network.api.response.posts.Post) {
            updateMetricDisplay(shareCount, data.safeShareCount, "share")

            feedShare.setOnClickListener {
                if (!feedShare.isEnabled) return@setOnClickListener

                Log.d(TAG, "Share clicked for post: ${data._id}")

                // Show share bottom sheet
                showShareBottomSheet(data)
            }
        }

        private fun showShareBottomSheet(data: com.uyscuti.social.network.api.response.posts.Post) {
            val context = feedShare.context
            val bottomSheetDialog = BottomSheetDialog(context)
            val binding = BottomDialogForShareBinding.inflate(LayoutInflater.from(context))
            bottomSheetDialog.setContentView(binding.root)

            // Prepare share content
            val shareText = "Check out this post on Flash!\n" +
                    "By: ${data.author?.account?.username ?: "Unknown"}\n" +
                    "${data.content ?: ""}"
            val postUrl = data.files?.firstOrNull()?.url ?: data.files?.size
            val fullShareText = if (postUrl != null) "$shareText\n$postUrl" else shareText

            // Setup share buttons
            binding.btnWhatsApp.setOnClickListener {
                shareToWhatsApp(context, fullShareText)
                incrementShareCount(data)
                bottomSheetDialog.dismiss()
            }

            binding.btnSMS.setOnClickListener {
                shareViaSMS(context, fullShareText)
                incrementShareCount(data)
                bottomSheetDialog.dismiss()
            }

            binding.btnInstagram.setOnClickListener {
                shareToInstagram(context, fullShareText)
                incrementShareCount(data)
                bottomSheetDialog.dismiss()
            }

            binding.btnMessenger.setOnClickListener {
                shareToMessenger(context, fullShareText)
                incrementShareCount(data)
                bottomSheetDialog.dismiss()
            }

            binding.btnFacebook.setOnClickListener {
                shareToFacebook(context, fullShareText)
                incrementShareCount(data)
                bottomSheetDialog.dismiss()
            }

            binding.btnTelegram.setOnClickListener {
                shareToTelegram(context, fullShareText)
                incrementShareCount(data)
                bottomSheetDialog.dismiss()
            }

            // Setup action buttons
            binding.btnReport.setOnClickListener {
                Toast.makeText(context, "Report functionality", Toast.LENGTH_SHORT).show()
                bottomSheetDialog.dismiss()
            }

            binding.btnNotInterested.setOnClickListener {
                Toast.makeText(context, "Not interested", Toast.LENGTH_SHORT).show()
                bottomSheetDialog.dismiss()
            }

            binding.btnSaveVideo.setOnClickListener {
                Toast.makeText(context, "Save post functionality", Toast.LENGTH_SHORT).show()
                bottomSheetDialog.dismiss()
            }

            binding.btnDuet.setOnClickListener {
                Toast.makeText(context, "Duet functionality", Toast.LENGTH_SHORT).show()
                bottomSheetDialog.dismiss()
            }

            binding.btnReact.setOnClickListener {
                Toast.makeText(context, "React functionality", Toast.LENGTH_SHORT).show()
                bottomSheetDialog.dismiss()
            }

            binding.btnAddToFavorites.setOnClickListener {
                Toast.makeText(context, "Add to favorites", Toast.LENGTH_SHORT).show()
                bottomSheetDialog.dismiss()
            }

            // Setup cancel button
            binding.btnCancel.setOnClickListener {
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.show()
        }

        private fun incrementShareCount(data: com.uyscuti.social.network.api.response.posts.Post) {
            val previousShareCount = data.safeShareCount

            // Update immediately for better UX
            data.shareCount += 1
            totalTextShareCounts = data.safeShareCount
            updateMetricDisplay(shareCount, data.safeShareCount, "share")

            YoYo.with(Techniques.Tada)
                .duration(700)
                .repeat(1)
                .playOn(feedShare)

            feedShare.isEnabled = false
            feedShare.alpha = 0.8f

            // Make API call to sync with server
            RetrofitClient.shareService.incrementShare(data._id)
                .enqueue(object : Callback<ShareResponse> {
                    override fun onResponse(call: Call<ShareResponse>, response: Response<ShareResponse>) {
                        feedShare.alpha = 1f
                        feedShare.isEnabled = true

                        if (response.isSuccessful) {
                            response.body()?.let { shareResponse ->
                                if (abs(shareResponse.shareCount - data.safeShareCount) > 1) {
                                    data.safeShareCount = shareResponse.shareCount
                                    totalTextShareCounts = data.safeShareCount
                                    updateMetricDisplay(shareCount, data.safeShareCount, "share")
                                    Log.d(TAG, "Updated share count from server: ${data.safeShareCount}")
                                }
                            }
                        } else {
                            Log.e(TAG, "Share sync failed: ${response.code()}")
                            // Revert on failure
                            data.safeShareCount = previousShareCount
                            totalTextShareCounts = data.safeShareCount
                            updateMetricDisplay(shareCount, data.safeShareCount, "share")
                        }
                    }

                    override fun onFailure(call: Call<ShareResponse>, t: Throwable) {
                        feedShare.alpha = 1f
                        feedShare.isEnabled = true
                        Log.e(TAG, "Share network error - will sync later", t)
                        // Revert on network failure
                        data.safeShareCount = previousShareCount
                        totalTextShareCounts = data.safeShareCount
                        updateMetricDisplay(shareCount, data.safeShareCount, "share")
                    }
                })

            feedClickListener.feedShareClicked(absoluteAdapterPosition, data)
        }

        // Share helper functions with multiple package name variants
        private fun shareToWhatsApp(context: Context, text: String) {
            val packages = listOf(
                "com.whatsapp",
                "com.whatsapp.w4b"
            )
            shareToApp(context, text, packages, "WhatsApp")
        }

        private fun shareViaSMS(context: Context, text: String) {
            try {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = "smsto:".toUri()
                    putExtra("sms_body", text)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "SMS app not available", Toast.LENGTH_SHORT).show()
            }
        }

        private fun shareToInstagram(context: Context, text: String) {
            val packages = listOf(
                "com.instagram.android"
            )
            shareToApp(context, text, packages, "Instagram")
        }

        private fun shareToMessenger(context: Context, text: String) {
            val packages = listOf(
                "com.facebook.orca",
                "com.facebook.mlite"
            )
            shareToApp(context, text, packages, "Messenger")
        }

        private fun shareToFacebook(context: Context, text: String) {
            val packages = listOf(
                "com.facebook.katana",
                "com.facebook.lite"
            )
            shareToApp(context, text, packages, "Facebook")
        }

        private fun shareToTelegram(context: Context, text: String) {
            val packages = listOf(
                "org.telegram.messenger",
                "org.telegram.messenger.web",
                "org.thunderdog.challegram"
            )
            shareToApp(context, text, packages, "Telegram")
        }

        // Generic function to try multiple package names
        private fun shareToApp(context: Context, text: String, packages: List<String>, appName: String) {
            try {
                for (packageName in packages) {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        setPackage(packageName)
                        putExtra(Intent.EXTRA_TEXT, text)
                    }

                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                        return
                    }
                }

                Toast.makeText(context, "$appName not installed", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "$appName not available", Toast.LENGTH_SHORT).show()
            }
        }

        private fun setupMoreOptionsButton(data: com.uyscuti.social.network.api.response.posts.Post) {
            moreOptionsButton.setOnClickListener {
                feedClickListener.moreOptionsClick(absoluteAdapterPosition, data)
            }
        }



        private fun updateLikeButtonUI(isLiked: Boolean) {
            Log.d(TAG, "Updating like button UI: isLiked=$isLiked")
            try {
                if (isLiked) {
                    likeButton.setImageResource(com.uyscuti.sharedmodule.R.drawable.filled_favorite_like)
                    // Add blue color tint for liked state
                    likeButton.setColorFilter(ContextCompat.getColor(itemView.context, com.uyscuti.sharedmodule.R.color.bluejeans), PorterDuff.Mode.SRC_IN)
                } else {
                    likeButton.setImageResource(com.uyscuti.sharedmodule.R.drawable.heart_svgrepo_com)
                    // Remove color filter for unfilled state
                    likeButton.clearColorFilter()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating like button UI", e)
            }
        }

        private fun updateBookmarkButtonUI(isBookmarked: Boolean) {
            Log.d(TAG, "Updating bookmark button UI: isBookmarked=$isBookmarked")
            try {
                if (isBookmarked) {
                    favoriteButton.setImageResource(com.uyscuti.sharedmodule.R.drawable.filled_favorite)
                } else {
                    favoriteButton.setImageResource(com.uyscuti.sharedmodule.R.drawable.favorite_svgrepo_com__1_)
                    // Remove color filter for unfilled state
                    favoriteButton.clearColorFilter()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating bookmark button UI", e)
            }
        }

        private fun updateRepostButtonAppearance(isReposted: Boolean) {
            if (isReposted) {
                repostedPost.setImageResource(com.uyscuti.sharedmodule.R.drawable.repeat_svgrepo_com)
                repostedPost.scaleX = 1.1f
                repostedPost.scaleY = 1.1f
            } else {
                repostedPost.setImageResource(com.uyscuti.sharedmodule.R.drawable.repeat_svgrepo_com)
                repostedPost.scaleX = 1.0f
                repostedPost.scaleY = 1.0f
            }
        }

        fun updateCommentCount(newCount: Int) {
            Log.d(TAG, "updateCommentCount: Updating comment count from $totalTextComments to $newCount")
            totalTextComments = if (newCount < 0) {
                Log.w(TAG, "updateCommentCount: Negative count received, setting to 0")
                0
            } else {
                newCount
            }

            currentPost?.let { post ->
                post.comments= totalTextComments
                try {
                    val field = post::class.java.getDeclaredField("safeCommentCount")
                    field.isAccessible = true
                    field.set(post, totalTextComments)
                } catch (e: NoSuchFieldException) {
                    Log.w(TAG, "safeCommentCount field not found in post object.")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            updateMetricDisplay(commentCount, totalTextComments, "comment")
            YoYo.with(Techniques.Pulse)
                .duration(500)
                .playOn(commentCount)
        }

        fun decrementCommentCount() {
            val newCount = maxOf(0, totalTextComments - 1)
            Log.d(TAG, "decrementCommentCount: Decrementing from $totalTextComments to $newCount")
            updateCommentCount(newCount)
        }

        fun incrementCommentCount() {
            val newCount = totalTextComments + 1
            Log.d(TAG, "incrementCommentCount: Incrementing from $totalTextComments to $newCount")
            updateCommentCount(newCount)
        }



        private fun updateMetricDisplay(textView: TextView, count: Int, metricType: String) {
            Log.d(TAG, "updateMetricDisplay: Updating $metricType with count: $count")
            textView.text = formatCount(count)
            textView.visibility = View.VISIBLE
            textView.contentDescription = when (metricType) {
                "like" -> "$count ${if (count == 1) "like" else "likes"}"
                "comment" -> "$count ${if (count == 1) "comment" else "comments"}"
                "bookmark" -> "$count ${if (count == 1) "bookmark" else "bookmarks"}"
                "repost" -> "$count ${if (count == 1) "repost" else "reposts"}"
                "share" -> "$count ${if (count == 1) "share" else "shares"}"
                else -> "$count $metricType"
            }
        }

        private fun updateAllMetricDisplays(
            data: com.uyscuti.social.network.api.response.posts.Post,
            commentsCount: Int,
            likesCount: Int,
            bookmarksCount: Int,
            repostsCount: Int,
            sharesCount: Int
        ) {
            updateMetricDisplay(commentCount, commentsCount, "comment")
            updateMetricDisplay(this.likesCount, likesCount, "like")
            updateMetricDisplay(favoriteCounts, bookmarksCount, "bookmark")
            updateMetricDisplay(repostCount, repostsCount, "repost")
            updateMetricDisplay(shareCount, sharesCount, "share")
        }

        @SuppressLint("DefaultLocale")
        private fun formatCount(count: Int): String {
            return when {
                count >= 1_000_000 -> {
                    val millions = count / 1_000_000.0
                    if (millions == millions.toInt().toDouble()) {
                        "${millions.toInt()}M"
                    } else {
                        String.format("%.1fM", millions)
                    }
                }
                count >= 1_000 -> {
                    val thousands = count / 1_000.0
                    if (thousands == thousands.toInt().toDouble()) {
                        "${thousands.toInt()}K"
                    } else {
                        String.format("%.1fK", thousands)
                    }
                }
                else -> count.toString()
            }
        }

        private fun setupUserProfile(data: com.uyscuti.social.network.api.response.posts.Post) {

            dateTime.text = formattedMongoDateTime(data.createdAt)

            val fullName = listOfNotNull(
                data.author?.firstName?.takeIf { it.isNotBlank() },
                data.author?.lastName?.takeIf { it.isNotBlank() }
            ).joinToString(" ").trim()
            textView.text = if (fullName.isNotEmpty()) fullName else data.author?.account?.username ?: "Unknown User"
            loadImageWithGlide(data.author?.account?.avatar?.url, profileImageView, itemView.context)
        }

        private fun navigateToOriginalPostWithoutRepostInside(data: com.uyscuti.social.network.api.response.posts.Post) {
            try {
                Log.d(TAG, "Navigating to original post for post ID: ${data._id}")
                val fragment = Fragment_Original_Post_Without_Repost_Inside().apply {
                    arguments = Bundle().apply {
                        // Changed from putSerializable to putString with JSON
                        putString(Fragment_Original_Post_Without_Repost_Inside.ARG_ORIGINAL_POST, Gson().toJson(data))
                        putString("post_id", data._id)
                        putInt("adapter_position", absoluteAdapterPosition)
                        putString("navigation_source", "feed_text")
                        putLong("navigation_timestamp", System.currentTimeMillis())
                    }
                }
                navigateToFragment(fragment, "original_post_without_repost")
            } catch (e: Exception) {
                Log.e(TAG, "Error navigating to original post fragment: ${e.message}")
                e.printStackTrace()
            }
        }

        private fun getActivityFromContext(context: Context): AppCompatActivity? {
            return when (context) {
                is AppCompatActivity -> context
                is ContextWrapper -> getActivityFromContext(context.baseContext)
                else -> null
            }
        }

        private fun navigateToFragment(fragment: Fragment, tag: String) {
            try {
                val activity = getActivityFromContext(itemView.context)
                if (activity != null) {
                    val currentFragment = activity.supportFragmentManager.fragments.lastOrNull {
                        it.isVisible && it.view != null
                    }
                    val fragmentManager = if (currentFragment != null &&
                        currentFragment.childFragmentManager.fragments.isNotEmpty()) {
                        currentFragment.childFragmentManager
                    } else {
                        activity.supportFragmentManager
                    }
                    fragmentManager.beginTransaction()
                        .setCustomAnimations(
                            com.uyscuti.sharedmodule.R.anim.slide_in_right,
                            com.uyscuti.sharedmodule.R.anim.slide_out_left,
                            com.uyscuti.sharedmodule.R.anim.slide_in_left,
                            com.uyscuti.sharedmodule.R.anim.slide_out_right
                        )
                        .replace(com.uyscuti.sharedmodule.R.id.frame_layout, fragment)
                        .addToBackStack(tag)
                        .commit()
                    Log.d(TAG, "Successfully navigated to fragment: $tag")
                } else {
                    Log.e(TAG, "Activity is null, cannot navigate to fragment: $tag")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error navigating to fragment: $tag", e)
            }
        }



        @OptIn(UnstableApi::class)
        private fun setupProfileClickHandlers(
            feedOwnerId: String,
            feedOwnerName: String,
            feedOwnerUsername: String,
            profilePicUrl: String
        ) {
            val profileClickListener = View.OnClickListener {
                if (feedOwnerId == LocalStorage.getInstance(itemView.context).getUserId()) {
                    EventBus.getDefault().post(GoToUserProfileFragment())
                } else {
                    Log.d(TAG, "setupProfileClickHandlers: Clicked on another user's profile")
                    val otherUsersProfile = OtherUsersProfile(
                        feedOwnerName, feedOwnerUsername, profilePicUrl, feedOwnerId,
                        isVerified = false,
                        bio = "",
                        linkInBio = "",
                        isCreator = false,
                        isTrending = false,
                        isFollowing = false,
                        isPrivate = false,
                        followersCount = 0L,
                        followingCount = 0L,
                        postsCount = 0L,
                        shortsCount = 0L,
                        videosCount = 0L,
                        isOnline = false,
                        lastSeen = null,
                        joinedDate = Date(),
                        location = "",
                        website = "",
                        email = "",
                        phoneNumber = "",
                        dateOfBirth = null,
                        gender = "",
                        accountType = "user",
                        isBlocked = false,
                        isMuted = false,
                        badgeType = null,
                        level = 1,
                        reputation = 0L,
                        coverPhoto = null,
                        theme = null,
                        language = null,
                        timezone = null,
                        notificationsEnabled = true,
                        privacySettings = emptyMap(),
                        socialLinks = emptyMap(),
                        achievements = emptyList(),
                        interests = emptyList(),
                        categories = emptyList()
                    )
                    // Open the OtherUserProfileAccount activity
                    OtherUserProfileAccount.open(
                        context = itemView.context,
                        user = otherUsersProfile,
                        dialogPhoto = profilePicUrl,
                        dialogId = feedOwnerId
                    )
                }
            }
            profileImageView.setOnClickListener(profileClickListener)
            textView.setOnClickListener(profileClickListener)
        }

        private fun setupContentAndCaption(data: com.uyscuti.social.network.api.response.posts.Post) {
            if (!data.content.isNullOrEmpty()) {
                Log.d(TAG, "setupContentAndCaption: Setting content: ${data.content}")
                caption.text = data.content
                caption.visibility = View.VISIBLE
            } else {
                caption.text = ""
                caption.visibility = View.GONE
            }
        }

        fun refreshCommentCountFromDatabase(postId: String) {
            Log.d(TAG, "refreshCommentCountFromDatabase: Refreshing count for post: $postId")
            RetrofitClient.commentService.getCommentCount(postId)
                .enqueue(object : Callback<CommentCountResponse> {
                    override fun onResponse(call: Call<CommentCountResponse>, response: Response<CommentCountResponse>) {
                        if (response.isSuccessful) {
                            response.body()?.let { countResponse ->
                                val newCount = countResponse.count
                                Log.d(TAG, "refreshCommentCountFromDatabase: Got count: $newCount")
                                updateCommentCount(newCount)
                                currentPost?.let { post ->
                                    post.comments = newCount
                                    try {
                                        val field = post::class.java.getDeclaredField("safeCommentCount")
                                        field.isAccessible = true
                                        field.set(post, newCount)
                                    } catch (e: Exception) {
                                        Log.w(TAG, "Could not update safeCommentCount: ${e.message}")
                                    }
                                }
                            }
                        } else {
                            Log.e(TAG, "refreshCommentCountFromDatabase: Failed with code: ${response.code()}")
                        }
                    }
                    override fun onFailure(call: Call<CommentCountResponse>, t: Throwable) {
                        Log.e(TAG, "refreshCommentCountFromDatabase: Network error", t)
                    }
                })
        }

        private fun logCountDebuggingInfo(data: com.uyscuti.social.network.api.response.posts.Post) {
            Log.d(TAG, "=== COUNT DEBUG INFO FOR POST ${data._id} ===")
            Log.d(TAG, "Raw comment count from API: ${data.comments}")
            Log.d(TAG, "Safe comment count: ${data.safeCommentCount}")
            Log.d(TAG, "Raw likes: ${data.likes}")
            Log.d(TAG, "Safe likes: ${data.safeLikes}")
            Log.d(TAG, "Raw bookmark count: ${data.bookmarkCount}")
            Log.d(TAG, "Safe bookmark count: ${data.safeBookmarkCount}")
            Log.d(TAG, "Raw repost count: ${data.safeRepostCount}")
            Log.d(TAG, "Safe repost count: ${data.safeRepostCount}")
            Log.d(TAG, "Raw share count: ${data.safeShareCount}")
            Log.d(TAG, "Safe share count: ${data.safeShareCount}")
            Log.d(TAG, "=== END COUNT DEBUG INFO ===")
        }

        private fun loadImageWithGlide(imageUrl: String?, imageView: ImageView, context: Context) {
            if (!imageUrl.isNullOrBlank()) {
                Glide.with(context)
                    .load(imageUrl)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(com.uyscuti.sharedmodule.R.drawable.flash21)
                    .error(com.uyscuti.sharedmodule.R.drawable.flash21)
                    .into(imageView)
            } else {
                imageView.setImageResource(com.uyscuti.sharedmodule.R.drawable.flash21)
            }
        }

        private fun formattedMongoDateTime(dateTimeString: String?): String {
            if (dateTimeString.isNullOrBlank()) return "Unknown Time"
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val date = inputFormat.parse(dateTimeString)
                val now = Date()
                val diffInMillis = now.time - (date?.time ?: 0)
                val diffInSeconds = diffInMillis / 1000
                val diffInMinutes = diffInSeconds / 60
                val diffInHours = diffInMinutes / 60
                val diffInDays = diffInHours / 24
                val diffInWeeks = diffInDays / 7
                val diffInMonths = diffInDays / 30
                val diffInYears = diffInDays / 365

                when {
                    diffInSeconds < 60 -> "now"
                    diffInMinutes < 60 -> "${diffInMinutes}m"
                    diffInHours < 24 -> "${diffInHours}h"
                    diffInDays == 1L -> "1d"
                    diffInDays < 7 -> "${diffInDays}d"
                    diffInWeeks < 4 -> "${diffInWeeks}w"
                    diffInMonths == 0L -> "1mo"
                    diffInMonths == 1L -> "1mo"
                    diffInMonths < 12 -> "${diffInMonths}mo"
                    diffInYears == 1L -> "1y"
                    else -> "${diffInYears}y"
                }
            } catch (e: Exception) {
                Log.w("DateFormat", "Failed to format date: $dateTimeString", e)
                "now"
            }
        }

    }

    inner class FeedPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val TAG = "FeedPostViewHolder"
        // Profile and Header Elements
        private val profileImageView: ImageView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.profileImageView)
        private val textView: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.textView)
        private val handerText: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.handerText)
        private val dateTime: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.date_time)
        private val followButton: AppCompatButton = itemView.findViewById(com.uyscuti.sharedmodule.R.id.followButton)
        private val moreOptionsButton: ImageView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.moreOptions)

        // Content Elements
        private val caption: ReadMoreTextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.caption)
        private val tags: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.tags)

        // Media Elements
        private val mixedFilesCardView: CardView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.mixedFilesCardView)
        val recyclerView: RecyclerView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.recyclerView)

        // Interaction Elements
        private val likeButton: ImageView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.likeButtonIcon)
        private val likesCount: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.likesCount)
        private val commentButton: ImageView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.commentButtonIcon)
        private val commentCount: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.commentCount)
        private val favoriteButton: ImageView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.favoriteSection)
        private val favoriteCounts: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.favoriteCounts)
        private val repostPost: ImageView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.repostPost)
        private val repostCount: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.repostCount)
        private val feedShare: ImageView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.shareButtonIcon)
        private val shareCountText: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.shareCount)

        // Container Elements
        private val feedMixedFilesContainer: ConstraintLayout = itemView.findViewById(com.uyscuti.sharedmodule.R.id.feedMixedFilesContainer)

        // State variables
        private var isFollowed = false
        private var totalMixedComments = 0
        private var serverCommentCount = 0
        private var loadedCommentCount = 0
        private var currentPost: com.uyscuti.social.network.api.response.posts.Post? = null
        private var totalMixedLikesCounts = 0
        private var totalMixedBookMarkCounts = 0
        private var totalMixedShareCounts = 0
        private var totalMixedRePostCounts = 0
        private var postClicked = false
        private var isFollowingUser = false


        @OptIn(UnstableApi::class)
        @SuppressLint("SetTextI18n", "SuspiciousIndentation")
        fun render(data: com.uyscuti.social.network.api.response.posts.Post) {

            data.isBusinessPost?.let {
                if (!it) {

                    Log.d(TAG, "render: feed data $data")

                    // Store current post reference
                    currentPost = data

                    val feedOwnerId = data.author?.account?._id ?: "Unknown"

                    // Check if this post has an original post (meaning it's a repost)
                    val originalPost = data.originalPost?.firstOrNull()


                    isFollowingUser = followingUserIds.contains(feedOwnerId)
                    Log.d(TAG, "render: User ${data.author?.account?.username} following status: $isFollowingUser")




                    if (originalPost != null) {
                        // This is a repost - use original post's engagement metrics
                        totalMixedComments = originalPost.commentCount
                        totalMixedLikesCounts = originalPost.likeCount  // Note: likeCount in OriginalPost
                        totalMixedBookMarkCounts = originalPost.bookmarkCount
                        totalMixedShareCounts = 0
                        totalMixedRePostCounts = originalPost.repostCount



                        Log.d(TAG, "Using original post metrics - Likes: ${originalPost.likeCount}, Comments: ${originalPost.commentCount}")
                    } else {
                        // This is a regular post - use its own metrics
                        totalMixedComments = data.comments
                        totalMixedLikesCounts = data.likes
                        totalMixedBookMarkCounts = data.bookmarkCount
                        totalMixedShareCounts = 0
                        totalMixedRePostCounts = data.safeRepostCount

                        Log.d(TAG, "Using direct post metrics - Likes: ${data.likes}, Comments: ${data.comments}")
                    }

                    setupUserInfo(data, feedOwnerId)
                    setupPostInfo(data)
                    setupMediaFiles(data)
                    setupContentAndTags(data)
                    setupEngagementButtons(data)
                    setupProfileClickListeners(data, feedOwnerId)
                    val feedOwnerUsername = data.author?.account?.username ?: "unknown"
                    setupFollowButton(feedOwnerId, feedOwnerUsername)
                    setupPostClickListeners(data)
                    ensurePostClickability(data)
                }
            }



        }


        private fun setupFollowButton(feedOwnerId: String, feedOwnerUsername: String) {
            val currentUserId = LocalStorage.getInstance(itemView.context).getUserId()

            // Check multiple sources for following status (by ID and username)
            val cachedFollowingList = getCachedFollowingList()
            val cachedFollowingUsernames = getCachedFollowingUsernames()

            val isUserFollowing = followingUserIds.contains(feedOwnerId) ||
                    cachedFollowingList.contains(feedOwnerId) ||
                    cachedFollowingUsernames.contains(feedOwnerUsername)

            Log.d(TAG, "setupFollowButton: Checking user $feedOwnerId (@$feedOwnerUsername)")
            Log.d(TAG, "  - Match by ID: ${followingUserIds.contains(feedOwnerId) || cachedFollowingList.contains(feedOwnerId)}")
            Log.d(TAG, "  - Match by username: ${cachedFollowingUsernames.contains(feedOwnerUsername)}")

            if (feedOwnerId == currentUserId || isFollowingUser || isUserFollowing) {
                followButton.visibility = View.GONE
                Log.d(TAG, "setupFollowButton: Hidden for user $feedOwnerId (@$feedOwnerUsername) - Following: true")
                return
            }

            // Show follow button only for users we're NOT following
            followButton.visibility = View.VISIBLE
            followButton.text = "Follow"
            followButton.backgroundTintList = ContextCompat.getColorStateList(
                itemView.context,
                com.uyscuti.sharedmodule.R.color.blueJeans
            )

            Log.d(TAG, "setupFollowButton: Showing follow button for $feedOwnerId (@$feedOwnerUsername)")

            followButton.setOnClickListener {
                handleFollowButtonClick(feedOwnerId, feedOwnerUsername)
            }
        }

        @SuppressLint("SetTextI18n")
        private fun handleFollowButtonClick(feedOwnerId: String, username: String){
            YoYo.with(Techniques.Pulse)
                .duration(300)
                .playOn(followButton)

            Log.d(TAG, "Follow button clicked for user: $feedOwnerId")

            isFollowed = !isFollowed
            val followEntity = FollowUnFollowEntity(feedOwnerId, isFollowed)

            if (isFollowed) {
                // Hide button immediately
                followButton.visibility = View.GONE

                // Add to adapter's following list AND persistent storage
                (bindingAdapter as? FeedAdapter)?.addToFollowing(feedOwnerId, username)

                // Also update via manager for consistency
                FollowingManager(itemView.context).addToFollowing(feedOwnerId)

                Log.d(TAG, "Now following user $feedOwnerId")
            } else {
                // Show button
                followButton.text = "Follow"
                followButton.visibility = View.VISIBLE

                // Remove from adapter's following list AND persistent storage
                (bindingAdapter as? FeedAdapter)?.removeFromFollowing(feedOwnerId, username)

                // Also update via manager for consistency
                FollowingManager(itemView.context).removeFromFollowing(feedOwnerId)

                Log.d(TAG, "Unfollowed user $feedOwnerId")
            }

            // Notify listener
            feedClickListener.followButtonClicked(followEntity, followButton)
            EventBus.getDefault().post(ShortsFollowButtonClicked(followEntity))
        }

        private fun setupPostClickListeners(data: com.uyscuti.social.network.api.response.posts.Post) {
            // Clear existing click listeners to avoid conflicts
            feedMixedFilesContainer.setOnClickListener(null)
            mixedFilesCardView.setOnClickListener(null)
            recyclerView.setOnClickListener(null)
            caption.setOnClickListener(null)
            tags.setOnClickListener(null)
            dateTime.setOnClickListener(null)

            // Set up main post container click
            feedMixedFilesContainer.setOnClickListener { view ->
                if (postClicked) return@setOnClickListener
                postClicked = true
                Log.d(TAG, "Main post container clicked")
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                navigateToOriginalPostWithoutRepostInside(data)
                view.postDelayed({ postClicked = false }, 1000)
            }

            // Set up media card click
            mixedFilesCardView.setOnClickListener { view ->
                if (postClicked) return@setOnClickListener
                postClicked = true
                Log.d(TAG, "Mixed files card clicked")
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                navigateToOriginalPostWithoutRepostInside(data)
                view.postDelayed({ postClicked = false }, 1000)
            }

            // Set up child elements to bubble clicks to main container
            preventChildClickInterference(data)
        }

        private fun preventChildClickInterference(data: com.uyscuti.social.network.api.response.posts.Post) {
            val childViews = listOfNotNull(
                caption,
                tags,
                dateTime,
                textView,
                handerText
            )

            childViews.forEach { childView ->
                childView.setOnClickListener { view ->
                    Log.d(TAG, "Child element clicked, bubbling to main container")
                    feedMixedFilesContainer.performClick()
                }
            }

            recyclerView.setOnClickListener {
                Log.d(TAG, "Recycler view clicked, bubbling to main container")
                feedMixedFilesContainer.performClick()
            }
        }

        private fun ensurePostClickability(data: com.uyscuti.social.network.api.response.posts.Post) {
            // Ensure main container is clickable
            feedMixedFilesContainer.isClickable = true
            feedMixedFilesContainer.isFocusable = true
            try {
                val typedValue = TypedValue()
                val context = itemView.context
                if (context.theme.resolveAttribute(
                        android.R.attr.selectableItemBackground,
                        typedValue, true
                    )) {
                    feedMixedFilesContainer.foreground = ContextCompat.getDrawable(context, typedValue.resourceId)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not set ripple background for main container: ${e.message}")
            }
            feedMixedFilesContainer.contentDescription = "Post, tap to view full post"
            feedMixedFilesContainer.elevation = 4f

            // Ensure media card is clickable
            mixedFilesCardView.isClickable = true
            mixedFilesCardView.isFocusable = true
            try {
                val typedValue = TypedValue()
                val context = itemView.context
                if (context.theme.resolveAttribute(
                        android.R.attr.selectableItemBackground,
                        typedValue, true
                    )) {
                    mixedFilesCardView.foreground = ContextCompat.getDrawable(context, typedValue.resourceId)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not set ripple background for mixed files card: ${e.message}")
            }
            mixedFilesCardView.contentDescription = "Post media, tap to view full post"
            mixedFilesCardView.elevation = 4f

            Log.d(TAG, "Post clickability ensured for post: ${data._id}")
        }

        private fun setupContentAndTags(data: com.uyscuti.social.network.api.response.posts.Post) {
            // Caption setup
            if (data.content.isNotEmpty()) {
                caption.text = data.content
                caption.visibility = View.VISIBLE
            } else {
                caption.visibility = View.GONE
            }

            // Tags setup
            if (data.tags.isNotEmpty()) {
                tags.visibility = View.VISIBLE
                val formattedTags = data.tags.joinToString(" ") {
                    val tag = it.toString()
                    if (tag.startsWith("#")) tag else "#$tag"
                }
                tags.text = formattedTags
            } else {
                tags.visibility = View.GONE
            }
        }

        private fun navigateToOriginalPostWithoutRepostInside(data: com.uyscuti.social.network.api.response.posts.Post) {
            try {
                Log.d(TAG, "Navigating to original Post for Post ID: ${data._id}")

                // Extract author information from the Post
                val firstName = data.author?.firstName ?: ""
                val lastName = data.author?.lastName ?: ""
                val displayName = when {
                    firstName.isNotBlank() && lastName.isNotBlank() -> "$firstName $lastName"
                    firstName.isNotBlank() -> firstName
                    lastName.isNotBlank() -> lastName
                    else -> data.author?.account?.username ?: "Unknown User"
                }

                val fragment = Fragment_Original_Post_Without_Repost_Inside().apply {
                    arguments = Bundle().apply {
                        // Post data
                        putString(Fragment_Original_Post_Without_Repost_Inside.ARG_ORIGINAL_POST, Gson().toJson(data))
                        putString("post_id", data._id)
                        putInt("adapter_position", absoluteAdapterPosition)
                        putString("navigation_source", "feed_mixed_files")
                        putLong("navigation_timestamp", System.currentTimeMillis())

                        // ADD AUTHOR INFORMATION
                        putString("author_name", displayName)
                        putString("author_username", data.author?.account?.username ?: "unknown_user")
                        putString("author_profile_image_url", data.author?.account?.avatar?.url ?: "")
                        putString("user_id", data.author?._id ?: "")

                        // Log for debugging
                        Log.d(TAG, "Author Info - Name: $displayName, Username: ${data.author?.account?.username}, ID: ${data.author?._id}")
                    }
                }

                navigateToFragment(fragment, "original_post_without_repost")

            } catch (e: Exception) {
                Log.e(TAG, "Error navigating to original post fragment: ${e.message}")
                e.printStackTrace()
            }
        }

        private fun navigateToFragment(fragment: Fragment, tag: String) {
            try {
                val activity = getActivityFromContext(itemView.context)
                if (activity != null) {
                    val currentFragment = activity.supportFragmentManager.fragments.lastOrNull {
                        it.isVisible && it.view != null
                    }
                    val fragmentManager = if (currentFragment != null &&
                        currentFragment.childFragmentManager.fragments.isNotEmpty()) {
                        currentFragment.childFragmentManager
                    } else {
                        activity.supportFragmentManager
                    }

                    fragmentManager.beginTransaction()
                        .setCustomAnimations(
                            com.uyscuti.sharedmodule.R.anim.slide_in_right,
                            com.uyscuti.sharedmodule.R.anim.slide_out_left,

                            )
                        .replace(com.uyscuti.sharedmodule.R.id.frame_layout, fragment)
                        .addToBackStack(tag)
                        .commit()
                    Log.d(TAG, "Successfully navigated to fragment: $tag")

                } else {
                    Log.e(TAG, "Activity is null, cannot navigate to fragment: $tag")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error navigating to fragment: $tag", e)
            }
        }

        private fun setupPostInfo(data: com.uyscuti.social.network.api.response.posts.Post) {
            // Date and time
            dateTime.text = formattedMongoDateTime(data.createdAt)

            // Comment count
            initializeCommentCounts(data)
            updateCommentCountDisplay()

            // Initialize all counts
            updateEngagementCounts(data)
        }

        private fun initializeCommentCounts(data: com.uyscuti.social.network.api.response.posts.Post) {
            serverCommentCount = data.comments
            totalMixedComments = serverCommentCount
            loadedCommentCount = 0
            Log.d(TAG, "Initialized comment counts - Server: $serverCommentCount, Total: $totalMixedComments")
        }

        private fun updateCommentCountDisplay() {
            commentCount.text = formatCount(totalMixedComments)
            commentCount.visibility = View.VISIBLE
            Log.d(TAG, "Updated comment count display: ${commentCount.text}")
        }

        private fun setupUserInfo(data: com.uyscuti.social.network.api.response.posts.Post, feedOwnerId: String) {
            // Profile image
            val avatarUrl = data.author?.account?.avatar?.url
            loadImageWithGlide(avatarUrl, profileImageView, itemView.context)

            // Username and handle
            val fullName = listOfNotNull(
                data.author?.firstName?.takeIf { it.isNotBlank() },
                data.author?.lastName?.takeIf { it.isNotBlank() }
            ).joinToString(" ").trim()
            textView.text = if (fullName.isNotEmpty()) fullName else data.author?.account?.username ?: "Unknown User"
            handerText.text = "@${data.author?.account?.username ?: "unknown"}"
        }

        private fun setupEngagementButtons(data: com.uyscuti.social.network.api.response.posts.Post) {
            setupLikeButton(data)
            setupCommentButton(data)
            setupShareButton(data)
            setupRepostButton(data)
            setupBookmarkButton(data)
            setupMoreOptionsButton(data)
        }

        private fun setupLikeButton(data: com.uyscuti.social.network.api.response.posts.Post) {
            Log.d(TAG, "Setting up like button - Initial state: isLiked=${data.isLiked}, likes=${totalMixedLikesCounts}")
            updateLikeButtonUI(data.isLiked ?: false)
            updateMetricDisplay(likesCount, totalMixedLikesCounts, "like")

            likeButton.setOnClickListener {
                if (!likeButton.isEnabled) return@setOnClickListener

                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

                Log.d(TAG, "Like clicked for post: ${data._id}")
                Log.d(TAG, "Current state before toggle: isLiked=${data.isLiked}, likes=${totalMixedLikesCounts}")

                val newLikeStatus = !(data.isLiked ?: false)
                val previousLikeStatus = data.isLiked ?: false
                val previousLikesCount = totalMixedLikesCounts

                // Update data immediately for optimistic UI update
                data.isLiked = newLikeStatus
                totalMixedLikesCounts = if (newLikeStatus) totalMixedLikesCounts + 1 else maxOf(0, totalMixedLikesCounts - 1)
                data.likes = totalMixedLikesCounts

                Log.d(TAG, "New state after toggle: isLiked=${data.isLiked}, likes=${totalMixedLikesCounts}")

                // Update UI immediately for better UX
                updateLikeButtonUI(newLikeStatus)
                updateMetricDisplay(likesCount, totalMixedLikesCounts, "like")

                // Animation for like/unlike
                YoYo.with(if (newLikeStatus) Techniques.Tada else Techniques.Pulse)
                    .duration(300)
                    .repeat(1)
                    .playOn(likeButton)

                // Disable button during network call
                likeButton.isEnabled = false
                likeButton.alpha = 0.8f

                // Call feedClickListener safely
                try {
                    feedClickListener.likeUnLikeFeed(absoluteAdapterPosition, data)
                } catch (e: Exception) {
                    Log.e(TAG, "Error calling likeUnLikeFeed: ${e.message}")
                }

                // Make network call to sync like status
                val likeRequest = LikeRequest(newLikeStatus)
                RetrofitClient.likeService.toggleLike(data._id, likeRequest)
                    .enqueue(object : Callback<LikeResponse> {
                        override fun onResponse(call: Call<LikeResponse>, response: Response<LikeResponse>) {
                            likeButton.alpha = 1f
                            likeButton.isEnabled = true

                            if (response.isSuccessful) {
                                response.body()?.let { likeResponse ->
                                    Log.d(TAG, "Like API success - Server count: ${likeResponse.likesCount}")
                                    // Update likes count if significantly different
                                    if (likeResponse.likesCount != null &&
                                        abs(likeResponse.likesCount - totalMixedLikesCounts) > 1
                                    ) {
                                        data.likes = likeResponse.likesCount
                                        totalMixedLikesCounts = data.likes
                                        updateMetricDisplay(likesCount, totalMixedLikesCounts, "like")
                                        Log.d(TAG, "Updated likes count from server: ${totalMixedLikesCounts}")
                                    }
                                }
                            } else {
                                Log.e(TAG, "Like sync failed: ${response.code()}")
                                // Revert on actual API errors
                                if (response.code() != 200) {
                                    data.isLiked = previousLikeStatus
                                    data.likes = previousLikesCount
                                    totalMixedLikesCounts = previousLikesCount
                                    updateLikeButtonUI(previousLikeStatus)
                                    updateMetricDisplay(likesCount, totalMixedLikesCounts, "like")
                                    Log.d(TAG, "Reverted to previous state: isLiked=${data.isLiked}, likes=${totalMixedLikesCounts}")
                                }
                            }
                        }

                        override fun onFailure(call: Call<LikeResponse>, t: Throwable) {
                            likeButton.alpha = 1f
                            likeButton.isEnabled = true

                            // Handle JSON parsing errors separately
                            if (t is MalformedJsonException ||
                                t.message?.contains("MalformedJsonException") == true) {
                                Log.w(TAG, "Like API returned malformed JSON but operation likely succeeded - keeping UI state")
                                return
                            }

                            Log.e(TAG, "Like network error - reverting changes", t)
                            // Revert for network failures
                            data.isLiked = previousLikeStatus
                            data.likes = previousLikesCount
                            totalMixedLikesCounts = previousLikesCount
                            updateLikeButtonUI(previousLikeStatus)
                            updateMetricDisplay(likesCount, totalMixedLikesCounts, "like")
                            Log.d(TAG, "Reverted to previous state after network error: isLiked=${data.isLiked}, likes=${totalMixedLikesCounts}")
                        }
                    })
            }
        }

        private fun setupBookmarkButton(data: com.uyscuti.social.network.api.response.posts.Post) {
            Log.d(TAG, "Setting up bookmark button - Initial state: isBookmarked=${data.isBookmarked}, bookmarkCount=${totalMixedBookMarkCounts}")
            updateBookmarkButtonUI(data.isBookmarked ?: false)
            updateMetricDisplay(favoriteCounts, totalMixedBookMarkCounts, "bookmark")

            favoriteButton.setOnClickListener {
                if (!favoriteButton.isEnabled) return@setOnClickListener

                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

                Log.d(TAG, "Bookmark clicked for post: ${data._id}")
                Log.d(TAG, "Current state before toggle: isBookmarked=${data.isBookmarked}, bookmarkCount=${totalMixedBookMarkCounts}")

                val newBookmarkStatus = !(data.isBookmarked ?: false)
                val previousBookmarkStatus = data.isBookmarked ?: false
                val previousBookmarkCount = totalMixedBookMarkCounts

                // Update data immediately
                data.isBookmarked = newBookmarkStatus
                totalMixedBookMarkCounts = if (newBookmarkStatus) totalMixedBookMarkCounts + 1 else maxOf(0, totalMixedBookMarkCounts - 1)
                data.bookmarkCount = totalMixedBookMarkCounts

                Log.d(TAG, "New state after toggle: isBookmarked=${data.isBookmarked}, bookmarkCount=${totalMixedBookMarkCounts}")

                // Update UI immediately for better UX
                updateBookmarkButtonUI(data.isBookmarked ?: false)
                updateMetricDisplay(favoriteCounts, totalMixedBookMarkCounts, "bookmark")

                // Animation
                YoYo.with(if (newBookmarkStatus) Techniques.Tada else Techniques.Pulse)
                    .duration(500)
                    .repeat(1)
                    .playOn(favoriteButton)

                // Disable button during network call
                favoriteButton.isEnabled = false
                favoriteButton.alpha = 0.8f

                val bookmarkRequest = BookmarkRequest(newBookmarkStatus)
                RetrofitClient.bookmarkService.toggleBookmark(data._id, bookmarkRequest)
                    .enqueue(object : Callback<BookmarkResponse> {
                        override fun onResponse(call: Call<BookmarkResponse>, response: Response<BookmarkResponse>) {
                            favoriteButton.alpha = 1f
                            favoriteButton.isEnabled = true

                            if (response.isSuccessful) {
                                response.body()?.let { bookmarkResponse ->
                                    Log.d(TAG, "Bookmark API success - Server count: ${bookmarkResponse.bookmarkCount}")
                                    if (abs(bookmarkResponse.bookmarkCount - totalMixedBookMarkCounts) > 1) {
                                        data.bookmarkCount = bookmarkResponse.bookmarkCount
                                        totalMixedBookMarkCounts = data.bookmarkCount
                                        updateMetricDisplay(favoriteCounts, totalMixedBookMarkCounts, "bookmark")
                                        Log.d(TAG, "Updated bookmark count from server: ${totalMixedBookMarkCounts}")
                                    }
                                }
                            } else {
                                Log.e(TAG, "Bookmark sync failed: ${response.code()}")
                                // Only revert on actual HTTP errors (not 2xx status codes)
                                if (response.code() >= 400) {
                                    data.isBookmarked = previousBookmarkStatus
                                    data.bookmarkCount = previousBookmarkCount
                                    totalMixedBookMarkCounts = data.bookmarkCount
                                    updateBookmarkButtonUI(data.isBookmarked ?: false)
                                    updateMetricDisplay(favoriteCounts, totalMixedBookMarkCounts, "bookmark")
                                    Log.d(TAG, "Reverted to previous state due to HTTP error: ${response.code()}")
                                }
                            }
                        }

                        override fun onFailure(call: Call<BookmarkResponse>, t: Throwable) {
                            favoriteButton.alpha = 1f
                            favoriteButton.isEnabled = true

                            // Handle JSON parsing errors separately - don't revert UI
                            if (t is MalformedJsonException ||
                                t.message?.contains("MalformedJsonException") == true ||
                                t.message?.contains("JsonReader.setStrictness") == true) {
                                Log.w(TAG, "Bookmark API returned malformed JSON but operation likely succeeded - keeping UI state")
                                // Don't revert the UI changes as the operation likely succeeded on the server
                                return
                            }

                            // Only revert for actual network failures
                            Log.e(TAG, "Bookmark network error - reverting changes", t)
                            data.isBookmarked = previousBookmarkStatus
                            data.bookmarkCount = previousBookmarkCount
                            totalMixedBookMarkCounts = data.bookmarkCount
                            updateBookmarkButtonUI(data.isBookmarked ?: false)
                            updateMetricDisplay(favoriteCounts, totalMixedBookMarkCounts, "bookmark")
                            Log.d(TAG, "Reverted to previous state after network error: isBookmarked=${data.isBookmarked}, bookmarkCount=${totalMixedBookMarkCounts}")
                        }
                    })

                // Always notify the listener regardless of API status
                feedClickListener.feedFavoriteClick(absoluteAdapterPosition, data)
            }
        }

        private fun updateLikeButtonUI(isLiked: Boolean) {
            Log.d(TAG, "Updating like button UI: isLiked=$isLiked")
            try {
                if (isLiked) {
                    likeButton.setImageResource(com.uyscuti.sharedmodule.R.drawable.filled_favorite_like)
                    // Add blue color tint for liked state
                    likeButton.setColorFilter(ContextCompat.getColor(itemView.context, com.uyscuti.sharedmodule.R.color.bluejeans), PorterDuff.Mode.SRC_IN)
                } else {
                    likeButton.setImageResource(com.uyscuti.sharedmodule.R.drawable.heart_svgrepo_com)
                    likeButton.clearColorFilter()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating like button UI", e)
            }
        }

        private fun updateBookmarkButtonUI(isBookmarked: Boolean) {
            Log.d(TAG, "Updating bookmark button UI: isBookmarked=$isBookmarked")
            try {
                if (isBookmarked) {
                    favoriteButton.setImageResource(com.uyscuti.sharedmodule.R.drawable.filled_favorite)
                } else {
                    favoriteButton.setImageResource(com.uyscuti.sharedmodule.R.drawable.favorite_svgrepo_com__1_)
                    favoriteButton.clearColorFilter()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating bookmark button UI", e)
            }
        }

        private fun updateMetricDisplay(textView: TextView, count: Int, metricType: String) {
            Log.d(TAG, "updateMetricDisplay: Updating $metricType with count: $count")
            textView.text = formatCount(count)
            textView.visibility = View.VISIBLE
            textView.contentDescription = when (metricType) {
                "like" -> "$count ${if (count == 1) "like" else "likes"}"
                "comment" -> "$count ${if (count == 1) "comment" else "comments"}"
                "bookmark" -> "$count ${if (count == 1) "bookmark" else "bookmarks"}"
                "repost" -> "$count ${if (count == 1) "repost" else "reposts"}"
                "share" -> "$count ${if (count == 1) "share" else "shares"}"
                else -> "$count $metricType"
            }
        }

        private fun setupCommentButton(data: com.uyscuti.social.network.api.response.posts.Post) {
            commentButton.setOnClickListener {
                if (!commentButton.isEnabled) return@setOnClickListener
                Log.d(TAG, "Comment button clicked for post ${data._id}")

                YoYo.with(Techniques.Tada)
                    .duration(700)
                    .repeat(1)
                    .playOn(commentButton)

                feedClickListener.feedCommentClicked(absoluteAdapterPosition, data)
                commentButton.isEnabled = true
            }

            commentCount.setOnClickListener {
                if (!commentCount.isEnabled) return@setOnClickListener
                YoYo.with(Techniques.Tada)
                    .duration(700)
                    .repeat(1)
                    .playOn(commentCount)
                feedClickListener.feedCommentClicked(absoluteAdapterPosition, data)
            }
        }

        private fun setupShareButton(data: com.uyscuti.social.network.api.response.posts.Post) {
            val originalPost = data.originalPost?.firstOrNull()
            val targetPostId = originalPost?._id ?: data._id  // Use original post ID for API calls

            updateMetricDisplay(shareCountText, 0, "share")

            feedShare.setOnClickListener {
                if (!feedShare.isEnabled) return@setOnClickListener

                Log.d(TAG, "Share clicked for post: $targetPostId")

                // Show share bottom sheet
                showShareBottomSheet(data, targetPostId)
            }
        }

        private fun showShareBottomSheet(data: com.uyscuti.social.network.api.response.posts.Post, targetPostId: String) {
            val context = feedShare.context
            val bottomSheetDialog = BottomSheetDialog(context)
            val binding = BottomDialogForShareBinding.inflate(LayoutInflater.from(context))
            bottomSheetDialog.setContentView(binding.root)

            // Prepare share content
            val shareText = "Check out this post on Flash!\n" +
                    "By: ${data.author?.account?.username ?: "Unknown"}\n" +
                    "${data.content ?: ""}"
            val postUrl = data.files?.firstOrNull()?.url
            val fullShareText = if (postUrl != null) "$shareText\n$postUrl" else shareText

            // Setup share buttons
            binding.btnWhatsApp.setOnClickListener {
                shareToWhatsApp(context, fullShareText)
                incrementShareCount(data, targetPostId)
                bottomSheetDialog.dismiss()
            }

            binding.btnSMS.setOnClickListener {
                shareViaSMS(context, fullShareText)
                incrementShareCount(data, targetPostId)
                bottomSheetDialog.dismiss()
            }

            binding.btnInstagram.setOnClickListener {
                shareToInstagram(context, fullShareText)
                incrementShareCount(data, targetPostId)
                bottomSheetDialog.dismiss()
            }

            binding.btnMessenger.setOnClickListener {
                shareToMessenger(context, fullShareText)
                incrementShareCount(data, targetPostId)
                bottomSheetDialog.dismiss()
            }

            binding.btnFacebook.setOnClickListener {
                shareToFacebook(context, fullShareText)
                incrementShareCount(data, targetPostId)
                bottomSheetDialog.dismiss()
            }

            binding.btnTelegram.setOnClickListener {
                shareToTelegram(context, fullShareText)
                incrementShareCount(data, targetPostId)
                bottomSheetDialog.dismiss()
            }

            // Setup action buttons
            binding.btnReport.setOnClickListener {
                Toast.makeText(context, "Report functionality", Toast.LENGTH_SHORT).show()
                bottomSheetDialog.dismiss()
            }

            binding.btnNotInterested.setOnClickListener {
                Toast.makeText(context, "Not interested", Toast.LENGTH_SHORT).show()
                bottomSheetDialog.dismiss()
            }

            binding.btnSaveVideo.setOnClickListener {
                Toast.makeText(context, "Save post functionality", Toast.LENGTH_SHORT).show()
                bottomSheetDialog.dismiss()
            }

            binding.btnDuet.setOnClickListener {
                Toast.makeText(context, "Duet functionality", Toast.LENGTH_SHORT).show()
                bottomSheetDialog.dismiss()
            }

            binding.btnReact.setOnClickListener {
                Toast.makeText(context, "React functionality", Toast.LENGTH_SHORT).show()
                bottomSheetDialog.dismiss()
            }

            binding.btnAddToFavorites.setOnClickListener {
                Toast.makeText(context, "Add to favorites", Toast.LENGTH_SHORT).show()
                bottomSheetDialog.dismiss()
            }

            // Setup cancel button
            binding.btnCancel.setOnClickListener {
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.show()
        }

        private fun incrementShareCount(data: com.uyscuti.social.network.api.response.posts.Post, targetPostId: String) {
            YoYo.with(Techniques.Tada)
                .duration(700)
                .repeat(1)
                .playOn(feedShare)

            feedShare.isEnabled = false
            feedShare.alpha = 0.8f

            // Use targetPostId for API call
            RetrofitClient.shareService.incrementShare(targetPostId)
                .enqueue(object : Callback<ShareResponse> {
                    override fun onResponse(call: Call<ShareResponse>, response: Response<ShareResponse>) {
                        feedShare.alpha = 1f
                        feedShare.isEnabled = true
                    }

                    override fun onFailure(call: Call<ShareResponse>, t: Throwable) {
                        feedShare.alpha = 1f
                        feedShare.isEnabled = true
                        Log.e(TAG, "Share network error", t)
                    }
                })

            feedClickListener.feedShareClicked(absoluteAdapterPosition, data)
        }

        // Share helper functions with multiple package name variants
        private fun shareToWhatsApp(context: Context, text: String) {
            val packages = listOf(
                "com.whatsapp",
                "com.whatsapp.w4b"
            )
            shareToApp(context, text, packages, "WhatsApp")
        }

        private fun shareViaSMS(context: Context, text: String) {
            try {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = "smsto:".toUri()
                    putExtra("sms_body", text)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "SMS app not available", Toast.LENGTH_SHORT).show()
            }
        }

        private fun shareToInstagram(context: Context, text: String) {
            val packages = listOf(
                "com.instagram.android"
            )
            shareToApp(context, text, packages, "Instagram")
        }

        private fun shareToMessenger(context: Context, text: String) {
            val packages = listOf(
                "com.facebook.orca",
                "com.facebook.mlite"
            )
            shareToApp(context, text, packages, "Messenger")
        }

        private fun shareToFacebook(context: Context, text: String) {
            val packages = listOf(
                "com.facebook.katana",
                "com.facebook.lite"
            )
            shareToApp(context, text, packages, "Facebook")
        }

        private fun shareToTelegram(context: Context, text: String) {
            val packages = listOf(
                "org.telegram.messenger",
                "org.telegram.messenger.web",
                "org.thunderdog.challegram"
            )
            shareToApp(context, text, packages, "Telegram")
        }

        // Generic function to try multiple package names
        private fun shareToApp(context: Context, text: String, packages: List<String>, appName: String) {
            try {
                for (packageName in packages) {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        setPackage(packageName)
                        putExtra(Intent.EXTRA_TEXT, text)
                    }

                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                        return
                    }
                }

                Toast.makeText(context, "$appName not installed", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "$appName not available", Toast.LENGTH_SHORT).show()
            }
        }

        private fun setupRepostButton(data: com.uyscuti.social.network.api.response.posts.Post) {
            val originalPost = data.originalPost?.firstOrNull()
            val targetPostId = originalPost?._id ?: data._id  // Use original post ID for API calls

            totalMixedRePostCounts = data.safeRepostCount
            updateMetricDisplay(repostCount, totalMixedRePostCounts, "repost")
            updateRepostButtonAppearance(data.isReposted)

            repostPost.setOnClickListener { view ->
                if (!repostPost.isEnabled) return@setOnClickListener
                repostPost.isEnabled = false

                try {
                    val wasReposted = data.isReposted
                    data.isReposted = !wasReposted
                    totalMixedRePostCounts = if (data.isReposted) totalMixedRePostCounts + 1 else maxOf(0, totalMixedRePostCounts - 1)

                    updateMetricDisplay(repostCount, totalMixedRePostCounts, "repost")
                    updateRepostButtonAppearance(data.isReposted)

                    YoYo.with(if (data.isReposted) Techniques.Tada else Techniques.Pulse)
                        .duration(700)
                        .playOn(repostPost)

                    repostPost.alpha = 0.8f

                    // Use targetPostId for API call
//                    val apiCall = if (data.isReposted) {
//                        RetrofitClient.repostService.incrementRepost(targetPostId)
//                    } else {
//                        RetrofitClient.repostService.decrementRepost(targetPostId)
//                    }

//                    apiCall.enqueue(object : Callback<RepostResponse> {
//                        override fun onResponse(call: Call<RepostResponse>, response: Response<RepostResponse>) {
//                            repostPost.isEnabled = true
//                            repostPost.alpha = 1f
//                            if (response.isSuccessful) {
//                                response.body()?.let { repostResponse ->
//                                    if (abs(repostResponse.repostCount - totalMixedRePostCounts) > 1) {
//
//                                        totalMixedRePostCounts = repostResponse.repostCount
//                                        updateMetricDisplay(repostCount, totalMixedRePostCounts, "repost")
//                                    }
//                                }
//                            }
//                        }
//
//                        override fun onFailure(call: Call<RepostResponse>, t: Throwable) {
//                            repostPost.isEnabled = true
//                            repostPost.alpha = 1f
//                            Log.e(TAG, "Repost network error", t)
//                        }
//                    })


                    feedClickListener.feedRepostPost(absoluteAdapterPosition, data)
                } catch (e: Exception) {
                    repostPost.isEnabled = true
                    repostPost.alpha = 1f
                    Log.e(TAG, "Exception in repost click listener", e)
                }
            }
        }

        private fun updateRepostButtonAppearance(isReposted: Boolean) {
            if (isReposted) {
                repostPost.setImageResource(com.uyscuti.sharedmodule.R.drawable.repeat_svgrepo_com)
                repostPost.scaleX = 1.1f
                repostPost.scaleY = 1.1f
            } else {
                repostPost.setImageResource(com.uyscuti.sharedmodule.R.drawable.repeat_svgrepo_com)
                repostPost.scaleX = 1.0f
                repostPost.scaleY = 1.0f
            }
        }


        fun updateCommentCount(newCount: Int) {
            Log.d(TAG, "updateCommentCount: Updating comment count from $totalMixedComments to $newCount")
            totalMixedComments = if (newCount < 0) {
                Log.w(TAG, "updateCommentCount: Negative count received, setting to 0")
                0
            } else {
                newCount
            }

            currentPost?.let { post ->
                post.comments = totalMixedComments
            }

            updateMetricDisplay(commentCount, totalMixedComments, "comment")
            YoYo.with(Techniques.Pulse)
                .duration(500)
                .playOn(commentCount)
        }

        fun decrementCommentCount() {
            val newCount = maxOf(0, totalMixedComments - 1)
            Log.d(TAG, "decrementCommentCount: Decrementing from $totalMixedComments to $newCount")
            updateCommentCount(newCount)
        }

        fun incrementCommentCount() {
            val newCount = totalMixedComments + 1
            Log.d(TAG, "incrementCommentCount: Incrementing from $totalMixedComments to $newCount")
            updateCommentCount(newCount)
        }



        fun refreshCommentCountFromDatabase(postId: String) {
            Log.d(TAG, "refreshCommentCountFromDatabase: Refreshing count for post: $postId")
            RetrofitClient.commentService.getCommentCount(postId)
                .enqueue(object : Callback<CommentCountResponse> {
                    override fun onResponse(call: Call<CommentCountResponse>, response: Response<CommentCountResponse>) {
                        if (response.isSuccessful) {
                            response.body()?.let { countResponse ->
                                val newCount = countResponse.count
                                Log.d(TAG, "refreshCommentCountFromDatabase: Got count: $newCount")
                                updateCommentCount(newCount)
                                currentPost?.let { post ->
                                    post.comments = newCount
                                }
                            }
                        } else {
                            Log.e(TAG, "refreshCommentCountFromDatabase: Failed with code: ${response.code()}")
                        }
                    }
                    override fun onFailure(call: Call<CommentCountResponse>, t: Throwable) {
                        Log.e(TAG, "refreshCommentCountFromDatabase: Network error", t)
                    }
                })
        }




        @SuppressLint("DefaultLocale")
        private fun formatCount(count: Int): String {
            return when {
                count >= 1_000_000 -> {
                    val millions = count / 1_000_000.0
                    if (millions == millions.toInt().toDouble()) {
                        "${millions.toInt()}M"
                    } else {
                        String.format("%.1fM", millions)
                    }
                }
                count >= 1_000 -> {
                    val thousands = count / 1_000.0
                    if (thousands == thousands.toInt().toDouble()) {
                        "${thousands.toInt()}K"
                    } else {
                        String.format("%.1fK", thousands)
                    }
                }
                else -> count.toString()
            }
        }


        private fun setupMoreOptionsButton(data: com.uyscuti.social.network.api.response.posts.Post) {
            moreOptionsButton.setOnClickListener {
                feedClickListener.moreOptionsClick(absoluteAdapterPosition, data)
            }
        }



        private fun setupProfileClickListeners(data: com.uyscuti.social.network.api.response.posts.Post, feedOwnerId: String) {
            val feedOwnerName = "${data.author?.firstName} ${data.author?.lastName}"
            val profilePicUrl = data.author?.account?.avatar?.url
            val feedOwnerUsername = data.author?.account?.username
            val profileClickListener = View.OnClickListener {
                handleProfileClick(feedOwnerId, feedOwnerName, feedOwnerUsername, profilePicUrl)
            }
            profileImageView.setOnClickListener(profileClickListener)
            textView.setOnClickListener(profileClickListener)
            handerText.setOnClickListener(profileClickListener)
        }


        @OptIn(UnstableApi::class)
        private fun handleProfileClick(
            feedOwnerId: String,
            feedOwnerName: String,
            feedOwnerUsername: String?,
            profilePicUrl: String?
        ) {
            if (feedOwnerId == LocalStorage.getInstance(itemView.context).getUserId()) {
                EventBus.getDefault().post(GoToUserProfileFragment())
            } else {
                Log.d(TAG, "Opening other user's profile")

                val otherUsersProfile = OtherUsersProfile(
                    feedOwnerName,
                    feedOwnerUsername ?: "unknown",
                    profilePicUrl.toString(),
                    feedOwnerId,
                    isVerified = false,
                    bio = "",
                    linkInBio = "",
                    isCreator = false,
                    isTrending = false,
                    isFollowing = false,
                    isPrivate = false,
                    followersCount = 0L,
                    followingCount = 0L,
                    postsCount = 0L,
                    shortsCount = 0L,
                    videosCount = 0L,
                    isOnline = false,
                    lastSeen = null,
                    joinedDate = Date(),
                    location = "",
                    website = "",
                    email = "",
                    phoneNumber = "",
                    dateOfBirth = null,
                    gender = "",
                    accountType = "user",
                    isBlocked = false,
                    isMuted = false,
                    badgeType = null,
                    level = 1,
                    reputation = 0L,
                    coverPhoto = null,
                    theme = null,
                    language = null,
                    timezone = null,
                    notificationsEnabled = true,
                    privacySettings = emptyMap(),
                    socialLinks = emptyMap(),
                    achievements = emptyList(),
                    interests = emptyList(),
                    categories = emptyList()
                )

                // Open the OtherUserProfileAccount activity
                OtherUserProfileAccount.open(
                    context = itemView.context,
                    user = otherUsersProfile,
                    dialogPhoto = profilePicUrl,
                    dialogId = feedOwnerId
                )
            }
        }


        private fun updateEngagementCounts(data: com.uyscuti.social.network.api.response.posts.Post) {
            likesCount.text = formatCount(totalMixedLikesCounts)
            updateMetricDisplay(favoriteCounts, totalMixedBookMarkCounts, "bookmark")
            updateMetricDisplay(repostCount, totalMixedRePostCounts, "repost")
            updateMetricDisplay(shareCountText, totalMixedShareCounts, "share")
            Log.d(TAG, "Updated all engagement counts")
        }

        private fun setupMediaFiles(data: com.uyscuti.social.network.api.response.posts.Post) {
            val fileList: MutableList<String> = mutableListOf()
            if (data.files.isNotEmpty()) {
                data.files.forEach { file ->
                    Log.d(TAG, "File URL: ${file.url}")
                    fileList.add(file.url)
                }
            } else {
                Log.d(TAG, "No files in post")
                recyclerView.visibility = View.GONE
                mixedFilesCardView.visibility = View.GONE
                return
            }

            // Setup RecyclerView layout based on file count
            recyclerView.visibility = View.VISIBLE
            mixedFilesCardView.visibility = View.VISIBLE

            recyclerView.layoutManager = when (fileList.size) {
                1 -> {
                    // Single file - use simple LinearLayoutManager
                    LinearLayoutManager(itemView.context)
                }
                2 -> {
                    // Two files - use StaggeredGridLayoutManager with 2 columns
                    StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                }
                3 -> {

                    StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                }
                else -> {

                    GridLayoutManager(itemView.context, 2)
                }
            }

            recyclerView.setHasFixedSize(true)

            // Setup adapter
            val adapter = FeedMixedFilesViewAdapter(data)
            recyclerView.adapter = adapter

            adapter.setOnMultipleFilesClickListener(object : OnMultipleFilesClickListener {
                override fun multipleFileClickListener(
                    currentIndex: Int,
                    files: List<com.uyscuti.social.network.api.response.posts.File>,
                    fileIds: List<String>
                ) {
                    navigateToTappedFilesInTheContainerView(
                        files as ArrayList<com.uyscuti.social.network.api.response.posts.File>, "mixed_files", currentIndex)
                }
            })
        }

        private fun navigateToTappedFilesInTheContainerView(
            files: ArrayList<com.uyscuti.social.network.api.response.posts.File>,
            mediaType: String,
            selectedPosition: Int
        ) {
            try {
                val fragment = Tapped_Files_In_The_Container_View_Fragment().apply {
                    arguments = Bundle().apply {
                        putString("files_data", Gson().toJson(files))
                        putString("media_type", mediaType)
                        putInt("selected_position", selectedPosition)
                        putInt("total_files", files.size)
                        putStringArray("file_urls", files.map { it.url }.toTypedArray())
                        currentPost?.let { post ->
                            putString("post_id", post._id)
                            putString("post_data", Gson().toJson(post))
                            putString("post_author_id", post.author?.account?._id)
                            putString("post_author_username", post.author?.account?.username)
                        }
                        putInt("adapter_position", absoluteAdapterPosition)
                        putString("navigation_source", "feed_mixed_files")
                        putString("media_source", mediaType)
                        putLong("navigation_timestamp", System.currentTimeMillis())
                        putBoolean("can_download", true)
                        putBoolean("can_share", true)
                        putBoolean("show_engagement_data", true)
                    }
                }
                navigateToFragment(fragment, "files_container_view")
            } catch (e: Exception) {
                Log.e(TAG, "Error navigating to files container fragment: ${e.message}")
                e.printStackTrace()
            }
        }

        private fun getActivityFromContext(context: Context): AppCompatActivity? {
            return when (context) {
                is AppCompatActivity -> context
                is ContextWrapper -> getActivityFromContext(context.baseContext)
                else -> null
            }
        }

        private val com.uyscuti.social.network.api.response.posts.Post.safeRepostCount: Int
            get() =  0



        private fun formattedMongoDateTime(dateTimeString: String?): String {
            if (dateTimeString.isNullOrBlank()) return "Unknown Time"
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val date = inputFormat.parse(dateTimeString)
                val now = Date()
                val diffInMillis = now.time - (date?.time ?: 0)
                val diffInSeconds = diffInMillis / 1000
                val diffInMinutes = diffInSeconds / 60
                val diffInHours = diffInMinutes / 60
                val diffInDays = diffInHours / 24
                val diffInWeeks = diffInDays / 7
                val diffInMonths = diffInDays / 30
                val diffInYears = diffInDays / 365

                when {
                    diffInSeconds < 60 -> "now"
                    diffInMinutes < 60 -> "${diffInMinutes}m"
                    diffInHours < 24 -> "${diffInHours}h"
                    diffInDays == 1L -> "1d"
                    diffInDays < 7 -> "${diffInDays}d"
                    diffInWeeks < 4 -> "${diffInWeeks}w"
                    diffInMonths == 0L -> "1mo"
                    diffInMonths == 1L -> "1mo"
                    diffInMonths < 12 -> "${diffInMonths}mo"
                    diffInYears == 1L -> "1y"
                    else -> "${diffInYears}y"
                }
            } catch (e: Exception) {
                Log.w("DateFormat", "Failed to format date: $dateTimeString", e)
                "now"
            }
        }

        private fun loadImageWithGlide(imageUrl: String?, imageView: ImageView, context: Context) {
            if (!imageUrl.isNullOrBlank()) {
                Glide.with(context)
                    .load(imageUrl)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(com.uyscuti.sharedmodule.R.drawable.flash21)
                    .error(com.uyscuti.sharedmodule.R.drawable.flash21)
                    .into(imageView)
            } else {
                imageView.setImageResource(com.uyscuti.sharedmodule.R.drawable.flash21)
            }
        }

    }

    inner class FeedRepostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // Constants and Fields
        private val tag = "FeedRepostViewHolder"

        private var isFollowed = false
        private var currentPost: com.uyscuti.social.network.api.response.posts.Post? = null
        private var totalMixedLikesCounts = 0
        private var totalMixedBookMarkCounts = 0
        private var totalMixedShareCounts = 0
        private var totalMixedRePostCounts = 0
        private var totalRepostComments = 0
        private var totalMixedComments = 0
        private var isFollowingUser = false


        // UI Components - User Info Section
        private val userProfileImage: ImageView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.userReposterProfileImage)
        private val repostedUserName: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.repostedUserName)
        private val tvUserHandle: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.tvUserHandle)
        private val dateTimeCreate: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.date_time_create)
        private val followButton: AppCompatButton = itemView.findViewById(com.uyscuti.sharedmodule.R.id.followButton)
        private val moreOptionsButton: ImageButton = itemView.findViewById(com.uyscuti.sharedmodule.R.id.moreOptions)

        // Main clickable containers
        private val repostContainer: LinearLayout = itemView.findViewById(com.uyscuti.sharedmodule.R.id.repostContainer)
        private val originalPostContainer: LinearLayout? = itemView.findViewById(com.uyscuti.sharedmodule.R.id.originalPostContainer)
        private val quotedPostCard: CardView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.quotedPostCard)

        // Post Content Section
        private val tvPostTag: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.tvPostTag)
        private val userComment: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.userComment)
        private val tvHashtags: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.tvHashtags)

        // Original Mixed Files Section
        private val mixedFilesCardViews: CardView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.mixedFilesCardViews)
        private val originalFeedImages: ImageView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.originalFeedImages)
        private val multipleAudiosContainers: ConstraintLayout = itemView.findViewById(com.uyscuti.sharedmodule.R.id.multipleAudiosContainers)
        private val recyclerViews: RecyclerView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.recyclerViews)

        // Quoted/Original Post Section
        private val originalPosterProfileImage: ImageView? = itemView.findViewById(com.uyscuti.sharedmodule.R.id.originalPosterProfileImage)
        private val originalPosterName: TextView? = itemView.findViewById(com.uyscuti.sharedmodule.R.id.originalPosterName)
        private val tvQuotedUserHandle: TextView? = itemView.findViewById(com.uyscuti.sharedmodule.R.id.tvQuotedUserHandle)
        private val originalPostText: TextView? = itemView.findViewById(com.uyscuti.sharedmodule.R.id.originalPostText)
        private val tvQuotedHashtags: TextView? = itemView.findViewById(com.uyscuti.sharedmodule.R.id.tvQuotedHashtags)

        // Quoted Post Media
        private val mixedFilesCardView: CardView? = itemView.findViewById(com.uyscuti.sharedmodule.R.id.mixedFilesCardView)
        private val originalFeedImage: ImageView? = itemView.findViewById(com.uyscuti.sharedmodule.R.id.originalFeedImage)
        private val multipleAudiosContainer: ConstraintLayout? = itemView.findViewById(com.uyscuti.sharedmodule.R.id.multipleAudiosContainer)
        private val recyclerView: RecyclerView? = itemView.findViewById(com.uyscuti.sharedmodule.R.id.recyclerView)
        private val ivQuotedPostImage: ImageView? = itemView.findViewById(com.uyscuti.sharedmodule.R.id.ivQuotedPostImage)

        // Interaction Buttons
        private val likeSection: LinearLayout = itemView.findViewById(com.uyscuti.sharedmodule.R.id.likeLayout)
        private val likeButton: ImageView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.likeButtonIcon)
        private val likesCount: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.likesCount)

        private val commentSection: LinearLayout = itemView.findViewById(com.uyscuti.sharedmodule.R.id.commentLayout)
        private val commentButton: ImageView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.commentButtonIcon)
        private val commentCount: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.commentCount)

        private val favoriteSection: LinearLayout = itemView.findViewById(com.uyscuti.sharedmodule.R.id.favoriteSection)
        private val favoriteButton: ImageView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.favoritesButton)
        private val favoriteCounts: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.favoriteCounts)

        private val repostSection: LinearLayout = itemView.findViewById(com.uyscuti.sharedmodule.R.id.repostLayout)
        private val repostButton: ImageView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.repostPost)
        private val repostCounts: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.repostCount)

        private val shareSection: LinearLayout = itemView.findViewById(com.uyscuti.sharedmodule.R.id.share_layout)
        private val shareButton: ImageView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.shareButtonIcon)
        private val shareCounts: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.shareCount)


        @OptIn(UnstableApi::class)
        @SuppressLint("SetTextI18n", "CheckResult", "SuspiciousIndentation")
        fun render(data: com.uyscuti.social.network.api.response.posts.Post) {

            data.isBusinessPost?.let {
                if (!it) {

                    currentPost = data

                    // Extract ACCOUNT ID and USERNAME for follow check
                    val feedReposterOwnerId: String
                    val feedReposterUsername: String

                    when {
                        // Case 1: Someone reposted - use their OWNER field (account ID) and username
                        data.repostedUser != null -> {
                            feedReposterOwnerId = data.repostedUser.owner  // Use owner field, not _id!
                            feedReposterUsername = data.repostedUser.username
                            Log.d(TAG, "RepostedUser account ID (owner): $feedReposterOwnerId")
                            Log.d(TAG, "RepostedUser username: @$feedReposterUsername")
                            Log.d(TAG, "NOT using repostedUser._id which is: ${data.repostedUser._id})")
                        }

                        // Case 2: Original post - use author.owner (the account ID!)
                        data.originalPost != null && data.originalPost.isNotEmpty() -> {
                            val originalAuthor = data.originalPost[0].author
                            feedReposterOwnerId = originalAuthor.owner  // This is the account ID!
                            feedReposterUsername = originalAuthor.account.username
                            Log.d(TAG, "Using author.owner (account ID): $feedReposterOwnerId")
                            Log.d(TAG, " Username: @$feedReposterUsername")
                            Log.d(TAG, "NOT using author._id which is: ${originalAuthor._id})")
                        }

                        // Case 3: Regular post - use main author's account ID
                        else -> {
                            feedReposterOwnerId = data.author?.account?._id ?: "Unknown"
                            feedReposterUsername = data.author?.account?.username ?: "unknown"
                            Log.d(TAG, "Main author account ID: $feedReposterOwnerId")
                            Log.d(TAG, "Username: @$feedReposterUsername")
                        }
                    }

                    // Check following status by BOTH ID and USERNAME
                    val cachedFollowingList = getCachedFollowingList()
                    val cachedFollowingUsernames = getCachedFollowingUsernames()

                    isFollowingUser = followingUserIds.contains(feedReposterOwnerId) ||
                            cachedFollowingList.contains(feedReposterOwnerId) ||
                            cachedFollowingUsernames.contains(feedReposterUsername)


                    Log.d(TAG, "REPOST FOLLOW CHECK - Post ID: ${data._id}")
                    Log.d(TAG, ">>> Account ID (for follow check): $feedReposterOwnerId")
                    Log.d(TAG, ">>> Username (for follow check): @$feedReposterUsername")
                    Log.d(TAG, ">>> Is following this account: $isFollowingUser")
                    Log.d(TAG, ">>> Match in followingUserIds: ${followingUserIds.contains(feedReposterOwnerId)}")
                    Log.d(TAG, ">>> Match in cachedList: ${cachedFollowingList.contains(feedReposterOwnerId)}")
                    Log.d(TAG, ">>> Match by username: ${cachedFollowingUsernames.contains(feedReposterUsername)}")
                    Log.d(TAG, "Following list (${followingUserIds.size} users): ${followingUserIds.joinToString(", ")}")


                    totalMixedComments = data.comments
                    totalMixedLikesCounts = data.likes
                    totalMixedBookMarkCounts = data.bookmarkCount
                    totalMixedShareCounts = data.shareCount
                    totalMixedRePostCounts = data.repostCount
                    totalRepostComments = totalMixedComments

                    setupRepostedUser(data)
                    setupOriginalPostContent(data)
                    dateTimeCreate.text = formattedMongoDateTime(data.createdAt)

                    setupLikeButton(data)
                    setupBookmarkButton(data)
                    setupRepostButton(data)
                    setupShareButton(data)
                    setupCommentButton(data)

                    updateMetricDisplay(likesCount, totalMixedLikesCounts, "like")
                    updateMetricDisplay(commentCount, totalMixedComments, "comment")
                    updateMetricDisplay(favoriteCounts, totalMixedBookMarkCounts, "bookmark")
                    updateMetricDisplay(repostCounts, totalMixedRePostCounts, "repost")
                    updateMetricDisplay(shareCounts, totalMixedShareCounts, "share")

                    // Pass BOTH the ACCOUNT ID and USERNAME to setupFollowButton
                    setupFollowButton(feedReposterOwnerId, feedReposterUsername)
                    setupMoreOptionsButton(data)
                    setupFileTapNavigation(data)
                    finalizeClickSetup(data)
                    setupRepostedUserProfileClicks(data)
                    setupOriginalPostAuthorClicks(data)

                }
            }


        }

        private fun setupFollowButton(accountId: String, username: String) {
            val currentUserId = LocalStorage.getInstance(itemView.context).getUserId()

            val cachedFollowingList = getCachedFollowingList()
            val cachedFollowingUsernames =
                getCachedFollowingUsernames()

            // Check by BOTH ID and USERNAME
            val isUserFollowing = followingUserIds.contains(accountId) ||
                    cachedFollowingList.contains(accountId) ||
                    cachedFollowingUsernames.contains(username)


            Log.d(TAG, "SETUP FOLLOW BUTTON")
            Log.d(TAG, "Account ID to check: $accountId")
            Log.d(TAG, "Username to check: @$username")
            Log.d(TAG, "Current user ID: $currentUserId")
            Log.d(TAG, "isFollowingUser (from render): $isFollowingUser")
            Log.d(TAG, "isUserFollowing (local check): $isUserFollowing")
            Log.d(TAG, "Match in followingUserIds: ${followingUserIds.contains(accountId)}")
            Log.d(TAG, "Match in cachedFollowingList: ${cachedFollowingList.contains(accountId)}")
            Log.d(TAG, "Match by username: ${cachedFollowingUsernames.contains(username)}")

            // Hide button if it's own post OR already following (by ID or username)
            val shouldHideButton = accountId == currentUserId || isFollowingUser || isUserFollowing

            if (shouldHideButton) {
                followButton.visibility = View.GONE
                Log.d(
                    TAG, "✓✓✓ HIDING follow button - Reason: ${when {
                        accountId == currentUserId -> "Own post"
                        cachedFollowingUsernames.contains(username) -> "Already following (by username: @$username)"
                        followingUserIds.contains(accountId) -> "Already following (by ID from followingUserIds)"
                        cachedFollowingList.contains(accountId) -> "Already following (by ID from cachedList)"
                        isFollowingUser -> "Already following (render check)"
                        isUserFollowing -> "Already following (local check)"
                        else -> "Unknown"
                    }}")
                return
            }

            // Show follow button
            followButton.visibility = View.VISIBLE
            followButton.text = "Follow"
            followButton.backgroundTintList = ContextCompat.getColorStateList(
                itemView.context,
                com.uyscuti.sharedmodule.R.color.blueJeans
            )

            Log.d(TAG, "SHOWING follow button for account: $accountId (@$username)")


            // Pass the ACCOUNT ID (not author ID) to handleFollowButtonClick
            followButton.setOnClickListener {
                handleFollowButtonClick(accountId, username)  // Pass both ID and username
            }
        }

        @SuppressLint("NotifyDataSetChanged")
        private fun handleFollowButtonClick(accountId: String, username: String) {
            YoYo.with(Techniques.Pulse).duration(300).playOn(followButton)


            Log.d(TAG, "FOLLOW BUTTON CLICKED")
            Log.d(TAG, "Account ID to follow: $accountId")
            Log.d(TAG, "Username to follow: @$username")
            Log.d(TAG, "This ID will be added to following list")


            isFollowed = !isFollowed
            val followEntity = FollowUnFollowEntity(accountId, isFollowed)

            if (isFollowed) {
                // Hide button immediately
                followButton.visibility = View.GONE

                // Add ACCOUNT ID and USERNAME to following list
                (bindingAdapter as? FeedAdapter)?.addToFollowing(accountId, username)
                FollowingManager(itemView.context).addToFollowing(accountId)

                Log.d(TAG, "✓ Added account $accountId (@$username) to following list")
            } else {
                // Show button
                followButton.text = "Follow"
                followButton.visibility = View.VISIBLE

                // Remove from following list
                (bindingAdapter as? FeedAdapter)?.removeFromFollowing(accountId, username)
                FollowingManager(itemView.context).removeFromFollowing(accountId)

                Log.d(TAG, "Removed account $accountId (@$username) from following list")
            }

            // Notify listener
            feedClickListener.followButtonClicked(followEntity, followButton)
            EventBus.getDefault().post(ShortsFollowButtonClicked(followEntity))

            // Refresh adapter
            (bindingAdapter as? FeedAdapter)?.notifyDataSetChanged()
        }

        private fun setupRepostedUser(data: com.uyscuti.social.network.api.response.posts.Post) {
            var feedOwnerId = ""
            var profilePicUrl: String? = null
            var feedOwnerUsername = ""
            var userHandle = ""

            val repostedUser = data.repostedUser
            if (repostedUser != null) {
                // Use owner field (account ID), not _id (profile ID)
                feedOwnerId = repostedUser.owner
                profilePicUrl = repostedUser.avatar?.url

                feedOwnerUsername = when {
                    repostedUser.firstName.isNotBlank() && repostedUser.lastName.isNotBlank() ->
                        "${repostedUser.firstName} ${repostedUser.lastName}"
                    repostedUser.firstName.isNotBlank() -> repostedUser.firstName
                    repostedUser.lastName.isNotBlank() -> repostedUser.lastName
                    else -> repostedUser.username
                }
                userHandle = "@${repostedUser.username}"
                Log.d(tag, "📍 Reposted by: $feedOwnerUsername (Account/Owner: $feedOwnerId, Username: @${repostedUser.username})")

            } else if (data.originalPost != null && data.originalPost.isNotEmpty()) {
                // Use author.owner (the account ID)
                val originalAuthor = data.originalPost[0].author
                feedOwnerId = originalAuthor.owner  // Account ID!
                profilePicUrl = originalAuthor.account.avatar.url

                feedOwnerUsername = when {
                    originalAuthor.firstName.isNotBlank() && originalAuthor.lastName.isNotBlank() ->
                        "${originalAuthor.firstName} ${originalAuthor.lastName}"
                    originalAuthor.firstName.isNotBlank() -> originalAuthor.firstName
                    originalAuthor.lastName.isNotBlank() -> originalAuthor.lastName
                    else -> originalAuthor.account.username
                }
                userHandle = "@${originalAuthor.account.username}"
                Log.d(tag, "Original author: $feedOwnerUsername (Account/Owner ID: $feedOwnerId, Username: @${originalAuthor.account.username})")

            } else {
                // Main author
                val author = data.author
                feedOwnerId = author.account._id
                profilePicUrl = author.account.avatar?.url
                feedOwnerUsername = buildDisplayName(author)
                userHandle = "@${author.account.username}"
                Log.d(tag, "Main author: $feedOwnerUsername (Account: $feedOwnerId, Username: @${author.account.username})")
            }

            repostedUserName.text = feedOwnerUsername
            tvUserHandle.text = userHandle

            if (!profilePicUrl.isNullOrBlank()) {
                Glide.with(itemView.context)
                    .load(profilePicUrl)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(com.uyscuti.sharedmodule.R.drawable.flash21)
                    .error(com.uyscuti.sharedmodule.R.drawable.flash21)
                    .into(userProfileImage)
            } else {
                userProfileImage.setImageResource(com.uyscuti.sharedmodule.R.drawable.flash21)
            }

            tvPostTag.text = if (repostedUser != null) "Had to Repost This!" else "Shared a Post!"
            userComment.visibility = if (!data.content.isNullOrBlank()) View.VISIBLE else View.GONE
            userComment.text = data.content
            setupRepostHashtags(data)
        }

        @SuppressLint("ClickableViewAccessibility")
        private fun setupRepostedUserProfileClicks(data: com.uyscuti.social.network.api.response.posts.Post) {
            // Extract ACCOUNT ID (owner field) for profile navigation
            var feedOwnerId = ""
            var feedOwnerName = ""
            var feedOwnerUsername = ""
            var profilePicUrl = ""

            val repostedUser = data.repostedUser
            if (repostedUser != null) {
                feedOwnerId = repostedUser.owner  //Use owner field (account ID)
                feedOwnerName = when {
                    repostedUser.firstName.isNotBlank() && repostedUser.lastName.isNotBlank() ->
                        "${repostedUser.firstName} ${repostedUser.lastName}"
                    repostedUser.firstName.isNotBlank() -> repostedUser.firstName
                    repostedUser.lastName.isNotBlank() -> repostedUser.lastName
                    else -> repostedUser.username
                }
                feedOwnerUsername = repostedUser.username
                profilePicUrl = repostedUser.avatar?.url ?: ""

            } else if (data.originalPost != null && data.originalPost.isNotEmpty()) {

                //Use owner field (account ID)
                val originalAuthor = data.originalPost[0].author
                feedOwnerId = originalAuthor.owner  // Account ID!
                feedOwnerName = buildDisplayName(originalAuthor)
                feedOwnerUsername = originalAuthor.account.username
                profilePicUrl = originalAuthor.account.avatar.url

            } else {
                feedOwnerId = data.author.account._id
                feedOwnerName = buildDisplayName(data.author)
                feedOwnerUsername = data.author.account.username
                profilePicUrl = data.author.account.avatar?.url ?: ""
            }

            Log.d(tag, "Profile click - Account ID: $feedOwnerId (@$feedOwnerUsername)")

            userProfileImage.setOnClickListener { view ->
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                handleProfileClick(feedOwnerId, feedOwnerName, feedOwnerUsername, profilePicUrl)
                view.isClickable = true
                true
            }

            userProfileImage.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    userProfileImage.performClick()
                    true
                } else {
                    false
                }
            }
        }


        @SuppressLint("ClickableViewAccessibility")
        private fun setupOriginalPostAuthorClicks(data: com.uyscuti.social.network.api.response.posts.Post) {
            //  Check if originalPost exists and has items
            if (!data.originalPost.isNullOrEmpty()) {
                val originalPostData = data.originalPost[0]
                val author = originalPostData.author

                //   Check if author exists
                if (author == null) {
                    Log.w(tag, "Original post author is null for post ${data._id}")
                    return
                }

                //  Check if account exists
                if (author.account == null) {
                    Log.w(tag, "Original post author account is null for post ${data._id}")
                    return
                }

                // Use owner field (account ID) with null safety
                val feedOwnerId = author.owner
                if (feedOwnerId.isNullOrEmpty()) {
                    Log.w(tag, "Original post owner ID is null or empty")
                    return
                }

                val feedOwnerName = buildDisplayName(author)
                val feedOwnerUsername = author.account.username ?: "unknown"
                val profilePicUrl = author.account.avatar?.url ?: ""

                Log.d(tag, "Original poster click - Account ID: $feedOwnerId (@$feedOwnerUsername)")

                originalPosterProfileImage?.setOnClickListener { view ->
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    handleProfileClick(feedOwnerId, feedOwnerName, feedOwnerUsername, profilePicUrl)
                }

                originalPosterProfileImage?.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_UP) {
                        originalPosterProfileImage?.performClick()
                        true
                    } else {
                        false
                    }
                }

            } else {
                // Fallback to main author
                val author = data.author

                //  Check if author exists
                if (author == null) {
                    Log.w(tag, "Main author is null for post ${data._id}")
                    return
                }

                //  Check if account exists
                if (author.account == null) {
                    Log.w(tag, " Main author account is null for post ${data._id}")
                    return
                }

                val feedOwnerId = author.account._id
                if (feedOwnerId.isNullOrEmpty()) {
                    Log.w(tag, "Main author ID is null or empty")
                    return
                }

                val feedOwnerName = buildDisplayName(author)
                val feedOwnerUsername = author.account.username ?: "unknown"
                val profilePicUrl = author.account.avatar?.url ?: ""

                Log.d(tag, "Main author click - Account ID: $feedOwnerId (@$feedOwnerUsername)")

                val profileClickListener = View.OnClickListener {
                    handleProfileClick(feedOwnerId, feedOwnerName, feedOwnerUsername, profilePicUrl)
                }

                originalPosterProfileImage?.setOnClickListener { view ->
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    handleProfileClick(feedOwnerId, feedOwnerName, feedOwnerUsername, profilePicUrl)
                }

                originalPosterName?.setOnClickListener(profileClickListener)
                tvQuotedUserHandle?.setOnClickListener(profileClickListener)

                originalPosterProfileImage?.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_UP) {
                        originalPosterProfileImage?.performClick()
                        true
                    } else {
                        false
                    }
                }
            }
        }

        @SuppressLint("ClickableViewAccessibility")
        private fun preventQuotedCardChildClickInterference() {

            // List of child views that should delegate clicks to their parent (quoted post card)
            val quotedCardChildren = listOfNotNull(
                originalPosterName,
                tvQuotedUserHandle,
                originalPostText,
                tvQuotedHashtags
            )

            quotedCardChildren.forEach { childView ->
                childView.setOnClickListener { view ->
                    Log.d(tag, "Child element clicked, delegating to Quoted Post Card")
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    quotedPostCard.performClick()
                }
            }



            // Handle media views in quoted card
            originalFeedImage?.setOnClickListener { view ->
                Log.d(tag, "Original feed image clicked, delegating to quoted post card")
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                quotedPostCard.performClick()
            }

            ivQuotedPostImage?.setOnClickListener { view ->
                Log.d(tag, "Quoted post image clicked, delegating to quoted post card")
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                quotedPostCard.performClick()
            }

            mixedFilesCardView?.setOnClickListener { view ->
                Log.d(tag, "Mixed files card clicked, delegating to quoted post card")
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                quotedPostCard.performClick()
            }

            // For RecyclerView, let it handle its own touch events
            recyclerView?.setOnTouchListener { _, _ ->
                false
            }
        }

        private fun buildDisplayName(author: Any): String {
            return when (author) {
                is AuthorX -> {
                    // For original post author
                    when {
                        author.firstName.isNotBlank() && author.lastName.isNotBlank() ->
                            "${author.firstName} ${author.lastName}"
                        author.firstName.isNotBlank() -> author.firstName
                        author.lastName.isNotBlank() -> author.lastName
                        author.account.username.isNotBlank() -> author.account.username
                        else -> "Unknown User"
                    }
                }
                is Author -> {
                    // For main post author
                    when {
                        author.firstName.isNotBlank() && author.lastName.isNotBlank() ->
                            "${author.firstName} ${author.lastName}"
                        author.firstName.isNotBlank() -> author.firstName
                        author.lastName.isNotBlank() -> author.lastName
                        author.account.username.isNotBlank() -> author.account.username
                        else -> "Unknown User"
                    }
                }
                else -> "Unknown User"
            }
        }

        private fun setupReposterTextClickDelegation() {
            // Make reposter name and handle delegate to repost container
            repostedUserName.setOnClickListener { view ->
                Log.d(tag, "Reposter name clicked, delegating to repost container")
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                repostContainer.performClick()
            }

            tvUserHandle.setOnClickListener { view ->
                Log.d(tag, "User handle clicked, delegating to repost container")
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                repostContainer.performClick()
            }
        }

        @OptIn(UnstableApi::class)
        private fun handleProfileClick(
            feedOwnerId: String,
            feedOwnerName: String,
            feedOwnerUsername: String,
            profilePicUrl: String) {
            Log.d(tag, "Profile clicked - ID: $feedOwnerId, Name: $feedOwnerName")

            // Check if it's the current user's profile
            if (feedOwnerId == LocalStorage.getInstance(itemView.context).getUserId()) {
                EventBus.getDefault().post(GoToUserProfileFragment())
            } else {
                Log.d(tag, "Opening other user's profile: $feedOwnerName")

                val otherUsersProfile = OtherUsersProfile(
                    feedOwnerName,
                    feedOwnerUsername,
                    profilePicUrl,
                    feedOwnerId,
                    isVerified = false,
                    bio = "",
                    linkInBio = "",
                    isCreator = false,
                    isTrending = false,
                    isFollowing = false,
                    isPrivate = false,
                    followersCount = 0L,
                    followingCount = 0L,
                    postsCount = 0L,
                    shortsCount = 0L,
                    videosCount = 0L,
                    isOnline = false,
                    lastSeen = null,
                    joinedDate = Date(),
                    location = "",
                    website = "",
                    email = "",
                    phoneNumber = "",
                    dateOfBirth = null,
                    gender = "",
                    accountType = "user",
                    isBlocked = false,
                    isMuted = false,
                    badgeType = null,
                    level = 1,
                    reputation = 0L,
                    coverPhoto = null,
                    theme = null,
                    language = null,
                    timezone = null,
                    notificationsEnabled = true,
                    privacySettings = emptyMap(),
                    socialLinks = emptyMap(),
                    achievements = emptyList(),
                    interests = emptyList(),
                    categories = emptyList()
                )

                // Open the OtherUserProfileAccount activity
                OtherUserProfileAccount.open(
                    context = itemView.context,
                    user = otherUsersProfile,
                    dialogPhoto = profilePicUrl,
                    dialogId = feedOwnerId
                )
            }
        }


        private fun setupNavigationClickListenersForAllContainers(data: com.uyscuti.social.network.api.response.posts.Post) {
            // Clear any existing click listeners to avoid conflicts
            repostContainer.setOnClickListener(null)
            originalPostContainer?.setOnClickListener(null)
            quotedPostCard.setOnClickListener(null)

            // Set up main repost container click (shows full repost with context)
            repostContainer.setOnClickListener { view ->
                Log.d(tag, "Repost container clicked")
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

                // Prevent double clicks
                if (!view.isClickable) return@setOnClickListener
                view.isClickable = false
                view.postDelayed({ view.isClickable = true }, 500)

                try {
                    navigateToOriginalPostWithRepostInside(data)
                } catch (e: Exception) {
                    Log.e(tag, "Error navigating from repost container", e)
                    view.isClickable = true
                }
            }

            // Set up original post container click (shows original post without repost context)
            originalPostContainer?.setOnClickListener { view ->
                Log.d(tag, "Original Post Card clicked")
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

                // Prevent double clicks
                if (!view.isClickable) return@setOnClickListener
                view.isClickable = false
                view.postDelayed({ view.isClickable = true }, 500)

                try {
                    navigateToOriginalPostWithoutRepostInside(data)
                } catch (e: Exception) {
                    Log.e(tag, "Error navigating from Original Post Container", e)
                    view.isClickable = true
                }
            }

            // Set up quoted post card click (same as original post container)
            quotedPostCard.setOnClickListener { view ->
                Log.d(tag, "Quoted Post / Original Post Card Clicked")
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

                // Prevent double clicks
                if (!view.isClickable) return@setOnClickListener
                view.isClickable = false
                view.postDelayed({ view.isClickable = true }, 500)

                try {
                    navigateToOriginalPostWithoutRepostInside(data)
                } catch (e: Exception) {
                    Log.e(tag, "Error navigating from Quoted Post Card", e)
                    view.isClickable = true
                }
            }

            // Setup interaction buttons to prevent click conflicts
            setupInteractionButtonsClickHandling()
        }

        private fun setupInteractionButtonsClickHandling() {

            likeSection.setOnClickListener { view ->
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                // The actual like functionality is handled in setupLikeButton
                likeButton.performClick()
            }

            // Comment section
            commentSection.setOnClickListener { view ->
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                commentButton.performClick()
            }

            // Favorite section
            favoriteSection.setOnClickListener { view ->
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                favoriteButton.performClick()
            }

            // Repost section
            repostSection.setOnClickListener { view ->
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                repostButton.performClick()
            }

            // Share section
            shareSection.setOnClickListener { view ->
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                shareButton.performClick()
            }

            followButton.setOnClickListener { view ->
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

                // Get the correct user ID and USERNAME from current post
                val feedReposterOwnerId: String
                val feedReposterUsername: String

                when {
                    currentPost?.repostedUser != null -> {
                        feedReposterOwnerId = currentPost?.repostedUser?.owner ?: ""
                        feedReposterUsername = currentPost?.repostedUser?.username ?: "unknown"
                    }
                    currentPost?.author?.account != null -> {
                        feedReposterOwnerId = currentPost?.author?.account?._id ?: ""
                        feedReposterUsername = currentPost?.author?.account?.username ?: "unknown"
                    }
                    else -> {
                        feedReposterOwnerId = ""
                        feedReposterUsername = "unknown"
                    }
                }

                handleFollowButtonClick(feedReposterOwnerId, feedReposterUsername)
            }

            // More options button - prevent bubbling to parent containers
            moreOptionsButton.setOnClickListener { view ->
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                setupMoreOptionsButton(currentPost ?: return@setOnClickListener)
            }

            // Prevent clicks on quoted card children from interfering with parent clicks
            preventQuotedCardChildClickInterference()

        }

        @SuppressLint("ResourceType")
        private fun ensureContainerClickability() {

            // Helper function to resolve the selectableItemBackground attribute
            fun getSelectableItemBackground(context: Context): Drawable? {
                val attrs = intArrayOf(android.R.attr.selectableItemBackground)
                val typedArray = context.obtainStyledAttributes(attrs)
                val drawableResId = typedArray.getResourceId(0, 0)
                typedArray.recycle()
                return if (drawableResId != 0) {
                    ContextCompat.getDrawable(context, drawableResId)
                } else {
                    null // Fallback in case the resource ID is not found
                }
            }

            // Ensure main repost container is clickable
            repostContainer.apply {
                isClickable = true
                isFocusable = true
                background = getSelectableItemBackground(context) ?: ContextCompat.getDrawable(context, android.R.drawable.btn_default)
            }

            // Ensure original post container is clickable
            originalPostContainer?.apply {
                isClickable = true
                isFocusable = true
                background = getSelectableItemBackground(context) ?: ContextCompat.getDrawable(context, android.R.drawable.btn_default)
            }

            // Ensure quoted post card is clickable with proper visual feedback
            quotedPostCard.apply {
                isClickable = true
                isFocusable = true
                foreground = getSelectableItemBackground(context) ?: ContextCompat.getDrawable(context, android.R.drawable.btn_default)
            }
        }

        private fun finalizeClickSetup(data: com.uyscuti.social.network.api.response.posts.Post) {

            setupNavigationClickListenersForAllContainers(data)
            setupReposterTextClickDelegation()
            ensureContainerClickability()



            Log.d(tag, "Container clickable states:")
            Log.d(tag, "- repostContainer: clickable=${repostContainer.isClickable}, focusable=${repostContainer.isFocusable}")
            Log.d(tag, "- originalPostContainer: clickable=${originalPostContainer?.isClickable}, focusable=${originalPostContainer?.isFocusable}")
            Log.d(tag, "- quotedPostCard: clickable=${quotedPostCard.isClickable}, focusable=${quotedPostCard.isFocusable}")
        }

        private fun setupLikeButton(data: com.uyscuti.social.network.api.response.posts.Post) {
            Log.d(TAG, "Setting up like button - Initial state: isLiked=${data.isLiked}, likes=${totalMixedLikesCounts}")
            updateLikeButtonUI(data.isLiked ?: false)
            updateMetricDisplay(likesCount, totalMixedLikesCounts, "like")  // Use totalMixedLikesCounts

            likeButton.setOnClickListener {
                if (!likeButton.isEnabled) return@setOnClickListener

                Log.d(TAG, "Like clicked for post: ${data._id}")
                val newLikeStatus = !(data.isLiked ?: false)
                val previousLikeStatus = data.isLiked ?: false
                val previousLikesCount = totalMixedLikesCounts  // Use totalMixedLikesCounts

                // Update data immediately
                data.isLiked = newLikeStatus
                totalMixedLikesCounts = if (newLikeStatus) totalMixedLikesCounts + 1 else maxOf(0, totalMixedLikesCounts - 1)
                data.likes = totalMixedLikesCounts



                // Update UI immediately for better UX
                updateLikeButtonUI(data.isLiked ?: false)
                updateMetricDisplay(likesCount, data.likes, "like")

                // Animation
                YoYo.with(if (newLikeStatus) Techniques.Tada else Techniques.Pulse)
                    .duration(300)
                    .repeat(1)
                    .playOn(likeButton)

                // Disable button during network call
                likeButton.isEnabled = false
                likeButton.alpha = 0.8f

                val likeRequest = LikeRequest(newLikeStatus)
                RetrofitClient.likeService.toggleLike(data._id, likeRequest)
                    .enqueue(object : Callback<LikeResponse> {
                        override fun onResponse(call: Call<LikeResponse>, response: Response<LikeResponse>) {
                            likeButton.alpha = 1f
                            likeButton.isEnabled = true

                            if (response.isSuccessful) {
                                response.body()?.let { likeResponse ->
                                    Log.d(TAG, "Like API success - Server count: ${likeResponse.likesCount}")
                                    if (likeResponse.likesCount != null &&
                                        abs(likeResponse.likesCount - data.likes) > 1) {
                                        data.likes = likeResponse.likesCount
                                        totalMixedLikesCounts = data.likes
                                        updateMetricDisplay(likesCount, data.likes, "like")
                                    }
                                }
                            } else {
                                Log.e(TAG, "Like sync failed: ${response.code()}")
                                if (response.code() != 200) {
                                    data.isLiked = previousLikeStatus
                                    data.likes = previousLikesCount
                                    totalMixedLikesCounts = data.likes
                                    updateLikeButtonUI(data.isLiked ?: false)
                                    updateMetricDisplay(likesCount, data.likes, "like")
                                }
                            }
                        }

                        override fun onFailure(call: Call<LikeResponse>, t: Throwable) {
                            likeButton.alpha = 1f
                            likeButton.isEnabled = true

                            if (t is MalformedJsonException ||
                                t.message?.contains("MalformedJsonException") == true) {
                                Log.w(TAG, "Like API returned malformed JSON but operation likely succeeded")
                                return
                            }

                            Log.e(TAG, "Like network error - reverting changes", t)
                            data.isLiked = previousLikeStatus
                            data.likes = previousLikesCount
                            totalMixedLikesCounts = data.likes
                            updateLikeButtonUI(data.isLiked ?: false)
                            updateMetricDisplay(likesCount, data.likes, "like")
                        }
                    })

                feedClickListener.likeUnLikeFeed(absoluteAdapterPosition, data)
            }
        }

        private fun setupBookmarkButton(data: com.uyscuti.social.network.api.response.posts.Post) {

            Log.d(
                TAG,
                "Setting up bookmark button - Initial state: isBookmarked=${data.isBookmarked}," +
                        " bookmarkCount=${totalMixedBookMarkCounts}")

            updateBookmarkButtonUI(data.isBookmarked ?: false)
            updateMetricDisplay(favoriteCounts, totalMixedBookMarkCounts, "bookmark")  // Use totalMixedBookMarkCounts

            favoriteButton.setOnClickListener {
                if (!favoriteButton.isEnabled) return@setOnClickListener

                Log.d(TAG, "Bookmark clicked for post: ${data._id}")
                val newBookmarkStatus = !(data.isBookmarked ?: false)
                val previousBookmarkStatus = data.isBookmarked ?: false
                val previousBookmarkCount = totalMixedBookMarkCounts  // Use totalMixedBookMarkCounts

                // Update data immediately
                data.isBookmarked = newBookmarkStatus
                totalMixedBookMarkCounts = if (newBookmarkStatus) totalMixedBookMarkCounts + 1 else maxOf(0, totalMixedBookMarkCounts - 1)
                data.bookmarkCount = totalMixedBookMarkCounts

                // Update UI immediately for better UX
                updateBookmarkButtonUI(data.isBookmarked ?: false)
                updateMetricDisplay(favoriteCounts, data.bookmarkCount, "bookmark")

                // Animation
                YoYo.with(if (newBookmarkStatus) Techniques.Tada else Techniques.Pulse)
                    .duration(500)
                    .repeat(1)
                    .playOn(favoriteButton)

                // Disable button during network call
                favoriteButton.isEnabled = false
                favoriteButton.alpha = 0.8f

                val bookmarkRequest = BookmarkRequest(newBookmarkStatus)
                RetrofitClient.bookmarkService.toggleBookmark(data._id, bookmarkRequest)
                    .enqueue(object : Callback<BookmarkResponse> {
                        override fun onResponse(call: Call<BookmarkResponse>, response: Response<BookmarkResponse>) {
                            favoriteButton.alpha = 1f
                            favoriteButton.isEnabled = true

                            if (response.isSuccessful) {
                                response.body()?.let { bookmarkResponse ->
                                    if (abs(bookmarkResponse.bookmarkCount - data.bookmarkCount) > 1) {
                                        data.bookmarkCount = bookmarkResponse.bookmarkCount
                                        totalMixedBookMarkCounts = data.bookmarkCount
                                        updateMetricDisplay(favoriteCounts, data.bookmarkCount, "bookmark")
                                    }
                                }
                            } else {
                                if (response.code() != 200) {
                                    data.isBookmarked = previousBookmarkStatus
                                    data.bookmarkCount = previousBookmarkCount
                                    totalMixedBookMarkCounts = data.bookmarkCount
                                    updateBookmarkButtonUI(data.isBookmarked ?: false)
                                    updateMetricDisplay(favoriteCounts, data.bookmarkCount, "bookmark")
                                }
                            }
                        }

                        override fun onFailure(call: Call<BookmarkResponse>, t: Throwable) {
                            favoriteButton.alpha = 1f
                            favoriteButton.isEnabled = true

                            if (t is MalformedJsonException ||
                                t.message?.contains("MalformedJsonException") == true) {
                                Log.w(TAG, "Bookmark API returned malformed JSON but operation likely succeeded")
                                return
                            }

                            data.isBookmarked = previousBookmarkStatus
                            data.bookmarkCount = previousBookmarkCount
                            totalMixedBookMarkCounts = data.bookmarkCount
                            updateBookmarkButtonUI(data.isBookmarked ?: false)
                            updateMetricDisplay(favoriteCounts, data.bookmarkCount, "bookmark")
                        }
                    })

                feedClickListener.feedFavoriteClick(absoluteAdapterPosition, data)
            }
        }

        private fun setupCommentButton(data: com.uyscuti.social.network.api.response.posts.Post) {
            commentButton.setOnClickListener {
                if (!commentButton.isEnabled) return@setOnClickListener
                Log.d(TAG, "Comment button clicked for post ${data._id}")

                YoYo.with(Techniques.Tada)
                    .duration(700)
                    .repeat(1)
                    .playOn(commentButton)

                feedClickListener.feedCommentClicked(absoluteAdapterPosition, data)
                commentButton.isEnabled = true
            }

            commentCount.setOnClickListener {
                if (!commentCount.isEnabled) return@setOnClickListener
                YoYo.with(Techniques.Tada)
                    .duration(700)
                    .repeat(1)
                    .playOn(commentCount)
                feedClickListener.feedCommentClicked(absoluteAdapterPosition, data)
            }
        }

        private fun setupRepostButton(data: com.uyscuti.social.network.api.response.posts.Post) {
            totalMixedRePostCounts = 0
            updateMetricDisplay(repostCounts, totalMixedRePostCounts, "repost")
            updateRepostButtonAppearance(data.isReposted)

            repostButton.setOnClickListener { view ->
                if (!repostButton.isEnabled) return@setOnClickListener
                repostButton.isEnabled = false

                try {
                    val wasReposted = data.isReposted
                    data.isReposted = !wasReposted
                    totalMixedRePostCounts = if (data.isReposted) totalMixedRePostCounts + 1 else maxOf(0, totalMixedRePostCounts - 1)


                    updateMetricDisplay(repostCounts, totalMixedRePostCounts, "repost")
                    updateRepostButtonAppearance(data.isReposted)

                    YoYo.with(if (data.isReposted) Techniques.Tada else Techniques.Pulse)
                        .duration(700)
                        .playOn(repostButton)

                    repostButton.alpha = 0.8f
                    val apiCall = if (data.isReposted) {
                        RetrofitClient.repostService.incrementRepost(data._id)
                    } else {
                        RetrofitClient.repostService.decrementRepost(data._id)
                    }

                    apiCall.enqueue(object : Callback<RepostResponse> {
                        override fun onResponse(call: Call<RepostResponse>, response: Response<RepostResponse>) {
                            repostButton.isEnabled = true
                            repostButton.alpha = 1f
                            if (response.isSuccessful) {
                                response.body()?.let { repostResponse ->
                                    if (abs(repostResponse.repostCount - totalMixedRePostCounts) > 1) {

                                        totalMixedRePostCounts = repostResponse.repostCount
                                        updateMetricDisplay(repostCounts, totalMixedRePostCounts, "repost")
                                    }
                                }
                            }
                        }

                        override fun onFailure(call: Call<RepostResponse>, t: Throwable) {
                            repostButton.isEnabled = true
                            repostButton.alpha = 1f
                            Log.e(TAG, "Repost network error - will sync later", t)
                        }
                    })

                    if (data.isReposted) {
                        navigateToEditPostToRepost(data)
                    }
                    feedClickListener.feedRepostPost(absoluteAdapterPosition, data)
                } catch (e: Exception) {
                    repostButton.isEnabled = true
                    repostButton.alpha = 1f
                    Log.e(TAG, "Exception in repost click listener", e)
                }
            }
        }

        private fun setupShareButton(data: com.uyscuti.social.network.api.response.posts.Post) {
            totalMixedShareCounts = data.shareCount ?: 0
            updateMetricDisplay(shareCounts, totalMixedShareCounts, "share")

            shareButton.setOnClickListener {
                if (!shareButton.isEnabled) return@setOnClickListener

                Log.d(TAG, "Share clicked for Post: ${data._id}")

                // Show share bottom sheet
                showShareBottomSheet(data)
            }
        }

        private fun showShareBottomSheet(data: com.uyscuti.social.network.api.response.posts.Post) {
            val context = shareButton.context
            val bottomSheetDialog = BottomSheetDialog(context)
            val binding = BottomDialogForShareBinding.inflate(LayoutInflater.from(context))
            bottomSheetDialog.setContentView(binding.root)

            // Prepare share content
            val shareText = "Check out this post on Flash!\n" +
                    "By: ${data.author?.account?.username ?: "Unknown"}\n" +
                    "${data.content ?: ""}"
            val postUrl = data.files?.firstOrNull()?.url
            val fullShareText = if (postUrl != null) "$shareText\n$postUrl" else shareText

            // Setup share buttons
            binding.btnWhatsApp.setOnClickListener {
                shareToWhatsApp(context, fullShareText)
                incrementShareCount(data)
                bottomSheetDialog.dismiss()
            }

            binding.btnSMS.setOnClickListener {
                shareViaSMS(context, fullShareText)
                incrementShareCount(data)
                bottomSheetDialog.dismiss()
            }

            binding.btnInstagram.setOnClickListener {
                shareToInstagram(context, fullShareText)
                incrementShareCount(data)
                bottomSheetDialog.dismiss()
            }

            binding.btnMessenger.setOnClickListener {
                shareToMessenger(context, fullShareText)
                incrementShareCount(data)
                bottomSheetDialog.dismiss()
            }

            binding.btnFacebook.setOnClickListener {
                shareToFacebook(context, fullShareText)
                incrementShareCount(data)
                bottomSheetDialog.dismiss()
            }

            binding.btnTelegram.setOnClickListener {
                shareToTelegram(context, fullShareText)
                incrementShareCount(data)
                bottomSheetDialog.dismiss()
            }

            // Setup action buttons
            binding.btnReport.setOnClickListener {
                Toast.makeText(context, "Report functionality", Toast.LENGTH_SHORT).show()
                bottomSheetDialog.dismiss()
            }

            binding.btnNotInterested.setOnClickListener {
                Toast.makeText(context, "Not interested", Toast.LENGTH_SHORT).show()
                bottomSheetDialog.dismiss()
            }

            binding.btnSaveVideo.setOnClickListener {
                Toast.makeText(context, "Save post functionality", Toast.LENGTH_SHORT).show()
                bottomSheetDialog.dismiss()
            }

            binding.btnDuet.setOnClickListener {
                Toast.makeText(context, "Duet functionality", Toast.LENGTH_SHORT).show()
                bottomSheetDialog.dismiss()
            }

            binding.btnReact.setOnClickListener {
                Toast.makeText(context, "React functionality", Toast.LENGTH_SHORT).show()
                bottomSheetDialog.dismiss()
            }

            binding.btnAddToFavorites.setOnClickListener {
                Toast.makeText(context, "Add to favorites", Toast.LENGTH_SHORT).show()
                bottomSheetDialog.dismiss()
            }

            // Setup cancel button
            binding.btnCancel.setOnClickListener {
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.show()
        }

        private fun incrementShareCount(data: com.uyscuti.social.network.api.response.posts.Post) {
            val previousShareCount = totalMixedShareCounts

            // Update immediately for better UX
            totalMixedShareCounts += 1
            data.shareCount = totalMixedShareCounts
            updateMetricDisplay(shareCounts, totalMixedShareCounts, "share")

            YoYo.with(Techniques.Tada)
                .duration(700)
                .repeat(1)
                .playOn(shareButton)

            shareButton.isEnabled = false
            shareButton.alpha = 0.8f

            // Make API call to sync with server
            RetrofitClient.shareService.incrementShare(data._id)
                .enqueue(object : Callback<ShareResponse> {
                    override fun onResponse(call: Call<ShareResponse>, response: Response<ShareResponse>) {
                        shareButton.alpha = 1f
                        shareButton.isEnabled = true

                        if (response.isSuccessful) {
                            response.body()?.let { shareResponse ->
                                if (abs(shareResponse.shareCount - totalMixedShareCounts) > 1) {
                                    data.shareCount = shareResponse.shareCount
                                    totalMixedShareCounts = data.shareCount
                                    updateMetricDisplay(shareCounts, data.shareCount, "share")
                                }
                            }
                        } else {
                            // Revert on failure
                            data.shareCount = previousShareCount
                            totalMixedShareCounts = data.shareCount
                            updateMetricDisplay(shareCounts, data.shareCount, "share")
                        }
                    }

                    override fun onFailure(call: Call<ShareResponse>, t: Throwable) {
                        shareButton.alpha = 1f
                        shareButton.isEnabled = true
                        // Revert on network failure
                        data.shareCount = previousShareCount
                        totalMixedShareCounts = data.shareCount
                        updateMetricDisplay(shareCounts, data.shareCount, "share")
                    }
                })

            feedClickListener.feedShareClicked(absoluteAdapterPosition, data)
        }

        // Share helper functions with multiple package name variants
        private fun shareToWhatsApp(context: Context, text: String) {
            val packages = listOf(
                "com.whatsapp",
                "com.whatsapp.w4b"
            )
            shareToApp(context, text, packages, "WhatsApp")
        }

        private fun shareViaSMS(context: Context, text: String) {
            try {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = "smsto:".toUri()
                    putExtra("sms_body", text)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "SMS app not available", Toast.LENGTH_SHORT).show()
            }
        }

        private fun shareToInstagram(context: Context, text: String) {
            val packages = listOf(
                "com.instagram.android"
            )
            shareToApp(context, text, packages, "Instagram")
        }

        private fun shareToMessenger(context: Context, text: String) {
            val packages = listOf(
                "com.facebook.orca",
                "com.facebook.mlite"
            )
            shareToApp(context, text, packages, "Messenger")
        }

        private fun shareToFacebook(context: Context, text: String) {
            val packages = listOf(
                "com.facebook.katana",
                "com.facebook.lite"
            )
            shareToApp(context, text, packages, "Facebook")
        }

        private fun shareToTelegram(context: Context, text: String) {
            val packages = listOf(
                "org.telegram.messenger",
                "org.telegram.messenger.web",
                "org.thunderdog.challegram"
            )
            shareToApp(context, text, packages, "Telegram")
        }

        // Generic function to try multiple package names
        private fun shareToApp(context: Context, text: String, packages: List<String>, appName: String) {
            try {
                for (packageName in packages) {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        setPackage(packageName)
                        putExtra(Intent.EXTRA_TEXT, text)
                    }

                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                        return
                    }
                }

                Toast.makeText(context, "$appName not installed", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "$appName not available", Toast.LENGTH_SHORT).show()
            }
        }

        fun updateCommentCount(newCount: Int) {
            Log.d(tag, "updateCommentCount: Updating comment count from $totalRepostComments to $newCount")
            totalRepostComments = if (newCount < 0) {
                Log.w(tag, "updateCommentCount: Negative count received, setting to 0")
                0
            } else {
                newCount
            }

            currentPost?.let { post ->
                post.comments = totalRepostComments
            }

            commentCount.text = formatCount(totalRepostComments)
            YoYo.with(Techniques.Pulse)
                .duration(500)
                .playOn(commentCount)
        }

        fun decrementCommentCount() {
            val newCount = maxOf(0, totalRepostComments - 1)
            Log.d(tag, "decrementCommentCount: Decrementing from $totalRepostComments to $newCount")
            updateCommentCount(newCount)
        }

        fun incrementCommentCount() {
            val newCount = totalRepostComments + 1
            Log.d(tag, "incrementCommentCount: Incrementing from $totalRepostComments to $newCount")
            updateCommentCount(newCount)
        }

        private fun updateLikeButtonUI(isLiked: Boolean) {
            Log.d(tag, "Updating like button UI: isLiked=$isLiked")
            try {
                if (isLiked) {
                    likeButton.setImageResource(com.uyscuti.sharedmodule.R.drawable.filled_favorite_like)
                } else {
                    likeButton.setImageResource(com.uyscuti.sharedmodule.R.drawable.heart_svgrepo_com)
                    likeButton.clearColorFilter()
                }
            } catch (e: Exception) {
                Log.e(tag, "Error updating like button UI", e)
            }
        }

        private fun updateBookmarkButtonUI(isBookmarked: Boolean) {
            Log.d(tag, "Updating bookmark button UI: isBookmarked=$isBookmarked")
            try {
                if (isBookmarked) {
                    favoriteButton.setImageResource(com.uyscuti.sharedmodule.R.drawable.filled_favorite)
                } else {
                    favoriteButton.setImageResource(com.uyscuti.sharedmodule.R.drawable.favorite_svgrepo_com__1_)
                    favoriteButton.clearColorFilter()
                }
            } catch (e: Exception) {
                Log.e(tag, "Error updating bookmark button UI", e)
            }
        }

        private fun updateRepostButtonAppearance(isReposted: Boolean) {
            if (isReposted) {
                repostButton.setImageResource(com.uyscuti.sharedmodule.R.drawable.repeat_svgrepo_com)
                repostButton.scaleX = 1.1f
                repostButton.scaleY = 1.1f
            } else {
                repostButton.setImageResource(com.uyscuti.sharedmodule.R.drawable.repeat_svgrepo_com)
                repostButton.scaleX = 1.0f
                repostButton.scaleY = 1.0f
            }
        }

        private fun updateMetricDisplay(textView: TextView, count: Int, metricType: String) {
            Log.d(TAG, "updateMetricDisplay: Updating $metricType with count: $count")
            textView.text = formatCount(count)
            textView.visibility = View.VISIBLE
            textView.contentDescription = when (metricType) {
                "like" -> "$count ${if (count == 1) "like" else "likes"}"
                "comment" -> "$count ${if (count == 1) "comment" else "comments"}"
                "bookmark" -> "$count ${if (count == 1) "bookmark" else "bookmarks"}"
                "repost" -> "$count ${if (count == 1) "repost" else "reposts"}"
                "share" -> "$count ${if (count == 1) "share" else "shares"}"
                else -> "$count $metricType"
            }
        }

        private fun navigateToOriginalPostWithRepostInside(originalPostData: com.uyscuti.social.network.api.response.posts.Post) {
            try {
                val fragment = Fragment_Original_Post_With_Repost_Inside.newInstance(originalPostData)
                navigateToFragment(fragment, "repost_with_context")
            } catch (e: Exception) {
                Log.e(tag, "Error navigating to repost fragment: ${e.message}")
                e.printStackTrace()
            }
        }

        private fun navigateToOriginalPostWithoutRepostInside(originalPostData: com.uyscuti.social.network.api.response.posts.Post) {

            try {

                val fragment = Fragment_Original_Post_Without_Repost_Inside().apply {
                    arguments = Bundle().apply {

                        putString(Fragment_Original_Post_Without_Repost_Inside.ARG_ORIGINAL_POST, Gson().toJson(originalPostData))


                        putSerializable(Fragment_Original_Post_Without_Repost_Inside.ARG_ORIGINAL_POST, originalPostData)


                        putString("post_data", Gson().toJson(originalPostData))
                    }
                }
                navigateToFragment(fragment, "original_post_without_repost")
            } catch (e: Exception) {
                Log.e(tag, "Error navigating to original post fragment: ${e.message}")
                e.printStackTrace()
            }
        }

        private fun navigateToTappedFilesInTheContainerView(
            files: List<Any>,
            mediaType: String,
            selectedPosition: Int
        ) {
            try {
                val fragment = Tapped_Files_In_The_Container_View_Fragment().apply {
                    arguments = Bundle().apply {
                        putString("files_data", Gson().toJson(files))
                        putString("media_type", mediaType)
                        putInt("selected_position", selectedPosition)
                        putInt("total_files", files.size)
                        val fileUrls = when {
                            files.first() is com.uyscuti.social.network.api.response.getrepostsPostsoriginal.File -> {
                                (files as List<com.uyscuti.social.network.api.response.getrepostsPostsoriginal.File>).map { it.url }
                            }
                            files.first() is com.uyscuti.social.network.api.response.getrepostsPostsoriginal.File -> {
                                (files as List<com.uyscuti.social.network.api.response.getrepostsPostsoriginal.File>).map { it.url }
                            }
                            else -> files.map { it.toString() }
                        }
                        putStringArray("file_urls", fileUrls.toTypedArray())
                        currentPost?.let { post ->
                            putString("post_id", post._id)
                            putString("post_data", Gson().toJson(post))
                            putString("post_author_id", post.repostedUser?._id)
                            putString("post_author_username", post.repostedUser?.username)
                        }
                        if (mediaType.contains("original") || mediaType.contains("quoted")) {
                            currentPost?.originalPost?.firstOrNull()?.let { originalPost ->
                                putString("original_post_id", originalPost._id)
                                putString("original_post_data", Gson().toJson(originalPost))
                                originalPost.author?.let { author ->
                                    putString("original_author_id", author._id)
                                    putString("original_author_username", author.account.username)
                                }
                            }
                        }
                        putInt("adapter_position", absoluteAdapterPosition)
                        putString("navigation_source", "feed_reposted_post")
                        putString("media_source", mediaType)
                        putLong("navigation_timestamp", System.currentTimeMillis())
                        putBoolean("can_download", true)
                        putBoolean("can_share", true)
                        putBoolean("show_engagement_data", true)
                    }
                }
                navigateToFragment(fragment, "files_container_view")
            } catch (e: Exception) {
                Log.e(tag, "Error navigating to files container fragment: ${e.message}")
                e.printStackTrace()
            }
        }

        private fun navigateToEditPostToRepost(data: com.uyscuti.social.network.api.response.posts.Post) {
            try {
                val fragment = Fragment_Edit_Post_To_Repost(data).apply {
                    arguments = Bundle().apply {
                        putString("post_data", Gson().toJson(data))
                        putString("post_id", data._id)
                        data.originalPost.firstOrNull()?.let { originalPost ->
                            putString("original_post_data", Gson().toJson(originalPost))
                            putString("original_post_id", originalPost._id)
                            putString("original_content", originalPost.content)
                            putString("original_content_type", originalPost.contentType)
                            putString("original_created_at", originalPost.createdAt)
                            putString("original_author_id", originalPost.author._id)
                            putString("original_author_username", originalPost.author.account.username)
                            putString(
                                "original_author_display_name",
                                listOfNotNull(
                                    originalPost.author.firstName.takeIf { it.isNotBlank() },
                                    originalPost.author.lastName.takeIf { it.isNotBlank() }
                                ).joinToString(" ").trim().takeIf { it.isNotEmpty() }
                                    ?: originalPost.author.account.username
                            )
                            putString("original_author_avatar", originalPost.author.account.avatar.url)
                            if (originalPost.files.isNotEmpty()) {
                                putString("original_files_data", Gson().toJson(originalPost.files))
                                putInt("original_files_count", originalPost.files.size)
                            }
                        }
                        val currentUser = LocalStorage.getInstance(itemView.context).getUser() as? com.uyscuti.sharedmodule.model.business.User
                        currentUser?.let { user ->
                            putString("current_user_id", user._id)
                            putString("current_user_username", user.account?.username)
                            putString(
                                "current_user_avatar",
                                when {
                                    user.avatar is Avatar -> user.avatar.url
                                    user.avatar is String -> user.avatar
                                    else -> null
                                }.toString()
                            )
                        }
                        putString("repost_type", "quote_repost")
                        putString("existing_comment", data.content)
                        putBoolean("is_editing_existing_repost", data.isReposted)
                        putInt("adapter_position", absoluteAdapterPosition)
                        putString("navigation_source", "repost_button_click")
                        putLong("navigation_timestamp", System.currentTimeMillis())
                    }
                }
                navigateToFragment(fragment, "edit_post_to_repost")
            } catch (e: Exception) {
                Log.e(tag, "Error navigating to edit post fragment: ${e.message}")
                e.printStackTrace()
            }
        }

        private fun getActivityFromContext(context: Context?): AppCompatActivity? {
            return when (context) {
                is AppCompatActivity -> context
                is ContextWrapper -> getActivityFromContext(context.baseContext)
                else -> null
            }
        }

        private fun navigateToFragment(fragment: Fragment, backStackName: String) {
            val activity = getActivityFromContext(itemView.context)
            if (activity != null) {
                val currentFragment = activity.supportFragmentManager.fragments.lastOrNull {
                    it.isVisible && it.view != null
                }
                val fragmentManager = if (currentFragment != null &&
                    currentFragment.childFragmentManager.fragments.isNotEmpty()) {
                    currentFragment.childFragmentManager
                } else {
                    activity.supportFragmentManager
                }
                fragmentManager.beginTransaction()
                    .setCustomAnimations(
                        com.uyscuti.sharedmodule.R.anim.slide_in_right,
                        com.uyscuti.sharedmodule.R.anim.slide_out_left
                    )
                    .replace(com.uyscuti.sharedmodule.R.id.frame_layout, fragment)
                    .addToBackStack(backStackName)
                    .commit()
                Log.d(tag, "Successfully navigated to $backStackName")
            } else {
                Log.e(tag, "Could not find AppCompatActivity from context")
            }
        }

        private fun setupFileTapNavigation(data: com.uyscuti.social.network.api.response.posts.Post) {
            setupMediaFileTapListener(originalFeedImages, data.files, "reposter_single_image")
            setupMediaFileTapListener(mixedFilesCardViews, data.files, "reposter_mixed_files")
            setupRecyclerViewFileTapListener(recyclerViews, data.files, "reposter_multiple_files")
            data.originalPost?.firstOrNull()?.let { originalPost ->
                originalFeedImage?.let { imageView ->
                    setupMediaFileTapListener(imageView, originalPost.files, "original_single_image")
                }
                mixedFilesCardView?.let { cardView ->
                    setupMediaFileTapListener(cardView, originalPost.files, "original_mixed_files")
                }
                recyclerView?.let { recyclerView ->
                    setupRecyclerViewFileTapListener(recyclerView, originalPost.files, "original_multiple_files")
                }
                ivQuotedPostImage?.let { imageView ->
                    setupMediaFileTapListener(imageView, originalPost.files, "quoted_post_image")
                }
            }
        }

        private fun setupMediaFileTapListener(view: View, files: List<Any>?, mediaType: String) {
            if (files.isNullOrEmpty()) return
            view.setOnClickListener { clickedView ->
                clickedView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                navigateToTappedFilesInTheContainerView(files, mediaType, 0)
            }
        }

        private fun setupCleanRecyclerView(fileCount: Int, adapter: FeedRepostViewFileAdapter) {
            recyclerView?.let { recyclerView ->
                recyclerView.visibility = View.VISIBLE
                when (fileCount) {
                    1 -> recyclerView.layoutManager = GridLayoutManager(itemView.context, 1)
                    2 -> recyclerView.layoutManager = GridLayoutManager(itemView.context, 2)
                    3 -> {
                        recyclerView.layoutManager = StaggeredGridLayoutManager(2,
                            StaggeredGridLayoutManager.VERTICAL)
                    }
                    else -> recyclerView.layoutManager = GridLayoutManager(itemView.context, 2)
                }
                recyclerView.setHasFixedSize(true)
                recyclerView.adapter = adapter
            }
        }

        private fun setupOriginalPostMedia(originalPostData: OriginalPost) {
            hideAllMediaViews()
            mixedFilesCardViews.setOnClickListener {
                currentPost?.let { post ->
                    feedClickListener.feedRepostFileClicked(
                        absoluteAdapterPosition,
                        originalPostData
                    )
                }
            }
            originalFeedImages.setOnClickListener {
                currentPost?.let { post ->
                    feedClickListener.feedRepostFileClicked(
                        absoluteAdapterPosition,
                        originalPostData
                    )
                }
            }
            try {
                // Check file count first - if exactly 3 files, always use the 3-item layout
                if (originalPostData.files.size == 3) {
                    mixedFilesCardView?.visibility = View.VISIBLE
                    originalFeedImage?.visibility = View.GONE
                    multipleAudiosContainer?.visibility = View.VISIBLE
                    val images = originalPostData.files.map { it.url }
                    val adapter = FeedRepostViewFileAdapter(originalPostData)
                    setupCleanRecyclerView(3, adapter) // This will force the 3-item layout
                    return
                }

                // For other counts, use the existing content type logic
                when (originalPostData.contentType) {

                    "text" -> {
                        // Only text content, no media
                    }

                    "image" -> {
                        if (originalPostData.files.isNotEmpty()) {
                            mixedFilesCardView?.visibility = View.VISIBLE
                            originalFeedImage?.visibility = View.VISIBLE
                            multipleAudiosContainer?.visibility = View.GONE
                            originalFeedImage?.let { imageView ->
                                Glide.with(itemView.context)
                                    .load(originalPostData.files[0].url)
                                    .placeholder(com.uyscuti.sharedmodule.R.drawable.imageplaceholder)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(imageView)
                                imageView.setOnClickListener {
                                    feedClickListener.feedRepostFileClicked(
                                        absoluteAdapterPosition,
                                        originalPostData
                                    )
                                }
                            }
                        }
                    }

                    "mixed_files" -> {
                        if (originalPostData.files.isNotEmpty()) {
                            mixedFilesCardView?.visibility = View.VISIBLE
                            originalFeedImage?.visibility = View.GONE
                            multipleAudiosContainer?.visibility = View.VISIBLE
                            val images = originalPostData.files.map { it.url }
                            val adapter = FeedRepostViewFileAdapter(originalPostData)
                            setupCleanRecyclerView(originalPostData.files.size, adapter)
                        }
                    }

                    "video" -> {
                        if (originalPostData.files.isNotEmpty()) {
                            mixedFilesCardView?.visibility = View.VISIBLE
                            originalFeedImage?.visibility = View.GONE
                            multipleAudiosContainer?.visibility = View.VISIBLE
                            val images = originalPostData.files.map { it.url }
                            val adapter = FeedRepostViewFileAdapter(originalPostData)
                            setupCleanRecyclerView(originalPostData.files.size, adapter)
                        }
                    }

                    else -> {
                        Log.w(tag, "Unknown content type: ${originalPostData.contentType}")
                    }

                }

            } catch (e: Exception) {
                Log.e(tag, "Error setting up original post media: ${e.message}")
                hideAllMediaViews()
            }
        }

        private fun setupRecyclerViewFileTapListener(
            recyclerView: RecyclerView, files: List<Any>?, mediaType: String
        ) {
            if (files.isNullOrEmpty()) return
            recyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
                private val gestureDetector = GestureDetector(itemView.context,
                    object : GestureDetector.SimpleOnGestureListener() {
                        override fun onSingleTapUp(e: MotionEvent): Boolean {
                            val childView = recyclerView.findChildViewUnder(e.x, e.y)
                            if (childView != null) {
                                val position = recyclerView.getChildAdapterPosition(childView)
                                if (position != RecyclerView.NO_POSITION && position < files.size) {
                                    childView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                    navigateToTappedFilesInTheContainerView(files, mediaType, position)
                                }
                            }
                            return true
                        }
                    })

                override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                    gestureDetector.onTouchEvent(e)
                    return false
                }

                override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
                override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
            })
        }

        private fun setupMoreOptionsButton(data: com.uyscuti.social.network.api.response.posts.Post) {
            moreOptionsButton.setOnClickListener {
                feedClickListener.moreOptionsClick(absoluteAdapterPosition, data)
            }
        }

        private fun setupOriginalPosterInfo(originalPostData: OriginalPost) {
            Log.d(tag, "ORIGINAL POSTER Details: ${originalPostData}")
            try {
                Log.d(tag, "Setting up original poster info")
                Log.d(tag, "originalPostReposter size: ${originalPostData.originalPostReposter.size}")

                var displayName = " "
                var userHandle = " "
                var avatarUrl: String? = null

                // Use author data (AuthorX is a single object, not a list)
                val author = originalPostData.author
                avatarUrl = author.account.avatar.url


                displayName = when {
                    // Try to build full name first
                    author.firstName.isNotBlank() && author.lastName.isNotBlank() ->
                        "${author.firstName} ${author.lastName}"
                    author.firstName.isNotBlank() -> author.firstName
                    author.lastName.isNotBlank() -> author.lastName
                    // Fall back to account username
                    author.account.username.isNotBlank() -> author.account.username
                    // Final fallback
                    else -> "Unknown User"
                }

                userHandle = if (author.account.username.isNotBlank()) {
                    "@${author.account.username}"
                } else {
                    "@unknown_user"
                }
                Log.d(tag, "Using author data: $displayName ($userHandle)")

                // Set profile image
                originalPosterProfileImage?.let { imageView ->
                    if (!avatarUrl.isNullOrBlank()) {
                        Glide.with(itemView.context)
                            .load(avatarUrl)
                            .apply(RequestOptions.bitmapTransform(CircleCrop()))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(com.uyscuti.sharedmodule.R.drawable.flash21)
                            .error(com.uyscuti.sharedmodule.R.drawable.flash21)
                            .into(imageView)
                        Log.d(tag, "Loading avatar: $avatarUrl")
                    } else {
                        imageView.setImageResource(com.uyscuti.sharedmodule.R.drawable.flash21)
                        Log.d(tag, "No avatar URL available, using default image")
                    }
                }

                // Set display name and handle
                originalPosterName?.text = displayName
                tvQuotedUserHandle?.text = userHandle

                Log.d(tag, "Final UI setup - Name: '$displayName', Handle: '$userHandle'")

            } catch (e: Exception) {
                Log.e(tag, "Error setting up original poster info", e)
                // Set safe fallback values
                originalPosterProfileImage?.setImageResource(com.uyscuti.sharedmodule.R.drawable.flash21)
                originalPosterName?.text = "Unknown User"
                tvQuotedUserHandle?.text = "@unknown_user"
            }
        }

        private fun setupQuotedUserFromMainAuthor(data: com.uyscuti.social.network.api.response.posts.Post) {
            try {
                val author = data.author
                Log.d("QuotedUser", "Using main author for quoted section: $author")

                // Set profile image - author.account.avatar is directly an Avatar object
                val avatarUrl = author.account.avatar.url

                originalPosterProfileImage?.let { imageView ->
                    if (!avatarUrl.isNullOrBlank()) {
                        Glide.with(itemView.context)
                            .load(avatarUrl)
                            .apply(RequestOptions.bitmapTransform(CircleCrop()))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(com.uyscuti.sharedmodule.R.drawable.flash21)
                            .error(com.uyscuti.sharedmodule.R.drawable.flash21)
                            .into(imageView)
                    } else {
                        imageView.setImageResource(com.uyscuti.sharedmodule.R.drawable.flash21)
                    }
                }

                // Set user name - build display name with proper fallbacks
                val displayName = when {
                    // Try to build from author's firstName and lastName
                    author.firstName.isNotBlank() || author.lastName.isNotBlank() -> {
                        val fullName = listOfNotNull(
                            author.firstName.takeIf { it.isNotBlank() },
                            author.lastName.takeIf { it.isNotBlank() }
                        ).joinToString(" ").trim()
                        if (fullName.isNotEmpty()) fullName else author.account.username
                    }

                    // Fall back to account username
                    author.account.username.isNotBlank() -> author.account.username

                    // Final fallback
                    else -> "Unknown User"
                }

                originalPosterName?.text = displayName

                // Set handle
                val handle = if (author.account.username.isNotBlank()) {
                    "@${author.account.username}"
                } else {
                    "@unknown"
                }

                tvQuotedUserHandle?.text = handle

                Log.d("QuotedUser", "Set quoted user name to: '$displayName'")
                Log.d("QuotedUser", "Set quoted user handle to: '$handle'")
                Log.d("QuotedUser", "Avatar URL used: '$avatarUrl'")

                // Set the quoted post content
                if (data.content.isNotBlank()) {
                    originalPostText?.visibility = View.VISIBLE
                    originalPostText?.text = data.content
                } else {
                    originalPostText?.visibility = View.GONE
                }

                // Set hashtags - use tags from post if available, otherwise use default
                if (data.tags.isNotEmpty()) {
                    val validTags = data.tags.filterNotNull()
                        .mapNotNull { it.toString().takeIf { tag -> tag.isNotBlank() } }

                    if (validTags.isNotEmpty()) {
                        val hashtagText = validTags.joinToString(" ") {
                            if (it.startsWith("#")) it else "#$it"
                        }
                        tvQuotedHashtags?.text = hashtagText
                    } else {
                        tvQuotedHashtags?.text = "#SoftwareAppreciation #GameChanger #MustHave"
                    }
                } else {
                    tvQuotedHashtags?.text = "#SoftwareAppreciation #GameChanger #MustHave"
                }
                tvQuotedHashtags?.visibility = View.VISIBLE

            } catch (e: Exception) {
                Log.e("QuotedUser", "Error setting up quoted user from main author", e)
                // Set fallback values
                originalPosterProfileImage?.setImageResource(com.uyscuti.sharedmodule.R.drawable.flash21)
                originalPosterName?.text = "Unknown User"
                tvQuotedUserHandle?.text = "@unknown"
                originalPostText?.visibility = View.GONE
                tvQuotedHashtags?.text = "#SoftwareAppreciation #GameChanger #MustHave"
                tvQuotedHashtags?.visibility = View.VISIBLE
            }
        }

        private fun setupRepostHashtags(data: com.uyscuti.social.network.api.response.posts.Post) {
            val hashtagText = "#Repost #GreatContent #MustSee"
            tvHashtags.text = hashtagText
            tvHashtags.visibility = View.VISIBLE
        }

        private fun setupOriginalPostContent(data: com.uyscuti.social.network.api.response.posts.Post) {
            Log.d("RepostData", "Original Post: ${data.originalPost}")
            if (data.originalPost != null && data.originalPost.isNotEmpty()) {
                val originalPostData = data.originalPost[0]
                quotedPostCard?.visibility = View.VISIBLE
                setupOriginalPosterInfo(originalPostData)
                setupOriginalPostText(originalPostData)
                setupOriginalPostHashtags(originalPostData)
                setupOriginalPostMedia(originalPostData)
            } else {
                Log.e(TAG, "originalPost is null or empty, using main post author data instead")
                quotedPostCard?.visibility = View.VISIBLE
                // Use the main post's author data for the quoted section
                setupQuotedUserFromMainAuthor(data)
            }
        }

        private fun setupOriginalPostText(originalPostData: OriginalPost) {
            if (!originalPostData.content.isNullOrBlank()) {
                originalPostText?.visibility = View.VISIBLE
                originalPostText?.text = originalPostData.content
            } else {
                originalPostText?.visibility = View.GONE
            }
        }

        private fun setupOriginalPostHashtags(originalPostData: OriginalPost) {
            val validTags = originalPostData.tags?.filterNotNull()
                ?.mapNotNull { it.toString().takeIf { tag -> tag.isNotBlank() } }
            if (!validTags.isNullOrEmpty()) {
                val hashtagText = validTags.joinToString(" ") {
                    if (it.startsWith("#")) it else "#$it"
                }
                tvQuotedHashtags?.text = hashtagText
                tvQuotedHashtags?.visibility = View.VISIBLE
            } else {
                tvQuotedHashtags?.text = "#SoftwareAppreciation #GameChanger #MustHave"
                tvQuotedHashtags?.visibility = View.VISIBLE
            }
        }

        private fun hideAllMediaViews() {
            mixedFilesCardViews.visibility = View.GONE
            originalFeedImages.visibility = View.GONE
            multipleAudiosContainers.visibility = View.GONE
            recyclerViews.visibility = View.GONE
            mixedFilesCardView?.visibility = View.GONE
            originalFeedImage?.visibility = View.GONE
            multipleAudiosContainer?.visibility = View.GONE
            recyclerView?.visibility = View.GONE
            ivQuotedPostImage?.visibility = View.GONE
        }

        private fun formatCount(count: Int?): String? {
            if (count != null) {
                return when {
                    count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
                    count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
                    else -> count.toString()
                }
            }
            return null
        }

        private fun formattedMongoDateTime(dateTimeString: String?): String {
            if (dateTimeString.isNullOrBlank()) return "Unknown Time"
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val date = inputFormat.parse(dateTimeString)
                val now = Date()
                val diffInMillis = now.time - (date?.time ?: 0)
                val diffInSeconds = diffInMillis / 1000
                val diffInMinutes = diffInSeconds / 60
                val diffInHours = diffInMinutes / 60
                val diffInDays = diffInHours / 24
                val diffInWeeks = diffInDays / 7
                val diffInMonths = diffInDays / 30
                val diffInYears = diffInDays / 365

                when {
                    diffInSeconds < 60 -> "now"
                    diffInMinutes < 60 -> "${diffInMinutes}m"
                    diffInHours < 24 -> "${diffInHours}h"
                    diffInDays == 1L -> "1d"
                    diffInDays < 7 -> "${diffInDays}d"
                    diffInWeeks < 4 -> "${diffInWeeks}w"
                    diffInMonths == 0L -> "1mo"
                    diffInMonths == 1L -> "1mo"
                    diffInMonths < 12 -> "${diffInMonths}mo"
                    diffInYears == 1L -> "1y"
                    else -> "${diffInYears}y"
                }
            } catch (e: Exception) {
                Log.w("DateFormat", "Failed to format date: $dateTimeString", e)
                "now"
            }
        }


    }

    inner class FeedNewPostWithRepostInsideFilesPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val TAG = "FeedRepostedWithNewFilesPostViewHolder"

        // UI Elements - New Post Section (Top)
        private val userProfileImage: ImageView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.userProfileImage)
        private val repostedUserName: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.repostedUserName)
        private val tvUserHandle: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.tvUserHandle)
        private val dateTimeCreate: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.date_time_create)
        private val followButton: AppCompatButton = itemView.findViewById(com.uyscuti.sharedmodule.R.id.followButton)
        private val moreOptionsButton: ImageButton = itemView.findViewById(com.uyscuti.sharedmodule.R.id.moreOptions)

        // Main clickable containers
        private val repostContainer: LinearLayout = itemView.findViewById(com.uyscuti.sharedmodule.R.id.repostContainer)
        private val originalPostContainer: LinearLayout = itemView.findViewById(com.uyscuti.sharedmodule.R.id.originalPostContainer)
        private val quotedPostCard: LinearLayout = itemView.findViewById(com.uyscuti.sharedmodule.R.id.quotedPostCard)

        // New Post Content Section (Top)
        private val tvPostTag: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.tvPostTag)
        private val userComment: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.userComment)
        private val tvHashtags: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.tvHashtags)

        // New Post Media Section (Top)
        private val newPostMediaCard: CardView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.newPostMediaCard)
        private val newPostImage: ImageView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.newPostImage)
        private val newPostMultipleMediaContainer: ConstraintLayout = itemView.findViewById(com.uyscuti.sharedmodule.R.id.newPostMultipleMediaContainer)
        private val newPostMediaRecyclerView: RecyclerView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.newPostMediaRecyclerView)

        // Original Post Media (for backward compatibility)
        private val mixedFilesCardViews: LinearLayout = itemView.findViewById(com.uyscuti.sharedmodule.R.id.mixedFilesCardViews)
        private val originalFeedImages: ImageView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.originalFeedImages)
        private val multipleAudiosContainers: ConstraintLayout = itemView.findViewById(com.uyscuti.sharedmodule.R.id.multipleAudiosContainers)
        private val recyclerViews: RecyclerView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.recyclerViews)

        // Quoted/Original Post Section (Bottom)
        private val originalPosterProfileImage: ImageView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.originalPosterProfileImage)
        private val originalPosterName: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.originalPosterName)
        private val tvQuotedUserHandle: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.tvQuotedUserHandle)
        private val originalPostText: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.originalPostText)
        private val tvQuotedHashtags: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.tvQuotedHashtags)

        // Quoted Post Media
        private val mixedFilesCardView: LinearLayout = itemView.findViewById(com.uyscuti.sharedmodule.R.id.mixedFilesCardView)
        private val originalFeedImage: ImageView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.originalFeedImage)
        private val multipleAudiosContainer: ConstraintLayout = itemView.findViewById(com.uyscuti.sharedmodule.R.id.multipleAudiosContainer)
        private val recyclerView: RecyclerView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.recyclerView)
        private val ivQuotedPostImage: ImageView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.ivQuotedPostImage)

        // Interaction Buttons
        private val likeSection: LinearLayout = itemView.findViewById(com.uyscuti.sharedmodule.R.id.likeLayout)
        private val likeButton: ImageView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.likeButtonIcon)
        private val likesCount: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.likesCount)

        private val commentSection: LinearLayout = itemView.findViewById(com.uyscuti.sharedmodule.R.id.commentLayout)
        private val commentButton: ImageView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.commentButtonIcon)
        private val feedCommentsCount: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.commentCount)

        private val favoriteSection: LinearLayout = itemView.findViewById(com.uyscuti.sharedmodule.R.id.favoriteSection)
        private val favoriteButton: ImageView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.favoritesButton)
        private val favoritesCount: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.favoriteCounts)

        private val repostSection: LinearLayout = itemView.findViewById(com.uyscuti.sharedmodule.R.id.repostPost)
        private val repostPost: ImageView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.repostPost)
        private val repostCountTextView: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.repostCount)

        private val shareSection: LinearLayout = itemView.findViewById(com.uyscuti.sharedmodule.R.id.shareLayout)
        private val shareCountTextView: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.shareCount)
        private val shareImageView: TextView = itemView.findViewById(com.uyscuti.sharedmodule.R.id.shareImageView)

        // State variables
        private var isFollowed = false
        private var totalMixedComments = 0
        private var serverCommentCount = 0
        private var loadedCommentCount = 0
        private var currentPost: com.uyscuti.social.network.api.response.posts.Post? = null
        private var totalMixedLikesCounts = 0
        private var totalMixedBookMarkCounts = 0
        private var totalMixedShareCounts = 0
        private var totalMixedRePostCounts = 0
        private var postClicked = false

        private var isFollowingUser = false
        private var totalRepostComments = 0

        @OptIn(UnstableApi::class)
        @SuppressLint("SetTextI18n", "SuspiciousIndentation")
        fun render(data: com.uyscuti.social.network.api.response.posts.Post) {

            data.isBusinessPost?.let {
                if (!it) {

                    Log.d(TAG, "render: feed data $data")

                    // Store current post reference
                    currentPost = data

                    // ✅ Extract ACCOUNT ID and USERNAME for follow check
                    val feedReposterOwnerId: String
                    val feedReposterUsername: String

                    when {
                        // Case 1: Someone reposted - use their OWNER field (account ID) and username
                        data.repostedUser != null -> {
                            feedReposterOwnerId = data.repostedUser.owner  // ✅ Use owner field, not _id!
                            feedReposterUsername = data.repostedUser.username
                            Log.d(TAG, "🔵 Case 1: RepostedUser account ID (owner): $feedReposterOwnerId")
                            Log.d(TAG, "🔵 Case 1: RepostedUser username: @$feedReposterUsername")
                            Log.d(TAG, "🔵 Case 1: (NOT using repostedUser._id which is: ${data.repostedUser._id})")
                        }

                        // Case 2: Original post - use author.owner (the account ID!)
                        data.originalPost != null && data.originalPost.isNotEmpty() -> {
                            val originalAuthor = data.originalPost[0].author
                            feedReposterOwnerId = originalAuthor.owner  // ✅ This is the account ID!
                            feedReposterUsername = originalAuthor.account.username
                            Log.d(TAG, "🔵 Case 2: Using author.owner (account ID): $feedReposterOwnerId")
                            Log.d(TAG, "🔵 Case 2: Username: @$feedReposterUsername")
                            Log.d(TAG, "🔵 Case 2: (NOT using author._id which is: ${originalAuthor._id})")
                        }

                        // Case 3: Regular post - use main author's account ID
                        else -> {
                            feedReposterOwnerId = data.author?.account?._id ?: "Unknown"
                            feedReposterUsername = data.author?.account?.username ?: "unknown"
                            Log.d(TAG, "🔵 Case 3: Main author account ID: $feedReposterOwnerId")
                            Log.d(TAG, "🔵 Case 3: Username: @$feedReposterUsername")
                        }
                    }

                    // Check following status by BOTH ID and USERNAME
                    val cachedFollowingList = getCachedFollowingList()
                    val cachedFollowingUsernames = getCachedFollowingUsernames()

                    isFollowingUser = followingUserIds.contains(feedReposterOwnerId) ||
                            cachedFollowingList.contains(feedReposterOwnerId) ||
                            cachedFollowingUsernames.contains(feedReposterUsername)

                    Log.d(TAG, "═══════════════════════════════════════")
                    Log.d(TAG, "REPOST FOLLOW CHECK - Post ID: ${data._id}")
                    Log.d(TAG, ">>> Account ID (for follow check): $feedReposterOwnerId")
                    Log.d(TAG, ">>> Username (for follow check): @$feedReposterUsername")
                    Log.d(TAG, ">>> Is following this account: $isFollowingUser")
                    Log.d(TAG, ">>> Match in followingUserIds: ${followingUserIds.contains(feedReposterOwnerId)}")
                    Log.d(TAG, ">>> Match in cachedList: ${cachedFollowingList.contains(feedReposterOwnerId)}")
                    Log.d(TAG, ">>> Match by username: ${cachedFollowingUsernames.contains(feedReposterUsername)}")
                    Log.d(TAG, "Following list (${followingUserIds.size} users): ${followingUserIds.joinToString(", ")}")
                    Log.d(TAG, "═══════════════════════════════════════")

                    totalMixedComments = data.comments
                    totalMixedLikesCounts = data.likes
                    totalMixedBookMarkCounts = data.bookmarkCount
                    totalMixedShareCounts = data.shareCount
                    totalMixedRePostCounts = data.repostCount
                    totalRepostComments = totalMixedComments

                    setupUserInfo(data, feedReposterOwnerId)
                    setupPostInfo(data)
                    setupNewPostMediaFiles(data)
                    setupContentAndTags(data)
                    setupOriginalPostContent(data)
                    setupEngagementButtons(data)
                    setupProfileClickListeners(data, feedReposterOwnerId)
                    setupFollowButton(feedReposterOwnerId, feedReposterUsername) // Pass both ID and username
                    setupPostClickListeners(data)
                    ensurePostClickability(data)
                    setupInteractionButtonsClickPrevention()


                }
            }


        }

        private fun setupPostClickListeners(data: com.uyscuti.social.network.api.response.posts.Post) {
            // Clear existing click listeners to avoid conflicts
            repostContainer.setOnClickListener(null)
            originalPostContainer.setOnClickListener(null)
            quotedPostCard.setOnClickListener(null)

            // Set up main repost container click (shows full repost with context)
            repostContainer.setOnClickListener { view ->
                if (postClicked) return@setOnClickListener
                postClicked = true
                Log.d(TAG, "Main repost container clicked")
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                navigateToOriginalPostWithRepostInside(data)
                view.postDelayed({ postClicked = false }, 1000)
            }

            // Set up original post container click (shows original post without repost context)
            originalPostContainer.setOnClickListener { view ->
                if (postClicked) return@setOnClickListener
                postClicked = true
                Log.d(TAG, "Original post container clicked")
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                navigateToOriginalPostWithoutRepostInside(data)
                view.postDelayed({ postClicked = false }, 1000)
            }

            // Set up quoted post card click (same as original post container)
            quotedPostCard.setOnClickListener { view ->
                if (postClicked) return@setOnClickListener
                postClicked = true
                Log.d(TAG, "Quoted post card clicked")
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                navigateToOriginalPostWithoutRepostInside(data)
                view.postDelayed({ postClicked = false }, 1000)
            }

            // Set up child elements to bubble clicks to main container
            preventChildClickInterference(data)
        }

        private fun preventChildClickInterference(data: com.uyscuti.social.network.api.response.posts.Post) {
            val childViews = listOfNotNull(
                userComment,
                tvHashtags,
                dateTimeCreate,
                repostedUserName,
                tvUserHandle,
                tvPostTag,
                originalPostText,
                tvQuotedHashtags,
                originalPosterName,
                tvQuotedUserHandle
            )

            childViews.forEach { childView ->
                childView.setOnClickListener { view ->
                    Log.d(TAG, "Child element clicked, bubbling to main container")
                    repostContainer.performClick()
                }
            }

            newPostMediaRecyclerView.setOnClickListener {
                Log.d(TAG, "New post recycler view clicked, bubbling to main container")
                repostContainer.performClick()
            }

            recyclerView.setOnClickListener {
                Log.d(TAG, "Quoted recycler view clicked, bubbling to quoted card")
                quotedPostCard.performClick()
            }
        }

        private fun setupInteractionButtonsClickPrevention() {
            // Like section
            likeSection.setOnClickListener { view ->
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                likeButton.performClick()
            }

            // Comment section
            commentSection.setOnClickListener { view ->
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                commentButton.performClick()
            }

            // Favorite section
            favoriteSection.setOnClickListener { view ->
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                favoriteButton.performClick()
            }

            // Repost section
            repostSection.setOnClickListener { view ->
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                repostPost.performClick()
            }

            // Share section
            shareSection.setOnClickListener { view ->
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                shareImageView.performClick()
            }

            followButton.setOnClickListener { view ->
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

                // Get the correct user ID and USERNAME from current post
                val feedReposterOwnerId: String
                val feedReposterUsername: String

                when {
                    currentPost?.repostedUser != null -> {
                        feedReposterOwnerId = currentPost?.repostedUser?.owner ?: ""
                        feedReposterUsername = currentPost?.repostedUser?.username ?: "unknown"
                    }
                    currentPost?.author?.account != null -> {
                        feedReposterOwnerId = currentPost?.author?.account?._id ?: ""
                        feedReposterUsername = currentPost?.author?.account?.username ?: "unknown"
                    }
                    else -> {
                        feedReposterOwnerId = ""
                        feedReposterUsername = "unknown"
                    }
                }

                handleFollowButtonClick(feedReposterOwnerId, feedReposterUsername)
            }

            moreOptionsButton.setOnClickListener { view ->
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                setupMoreOptionsButton(currentPost ?: return@setOnClickListener)
            }
        }



        private fun ensurePostClickability(data: com.uyscuti.social.network.api.response.posts.Post) {

            // Ensure main container is clickable
            repostContainer.isClickable = true
            repostContainer.isFocusable = true
            try {
                val typedValue = TypedValue()
                val context = itemView.context
                if (context.theme.resolveAttribute(
                        android.R.attr.selectableItemBackground,
                        typedValue, true
                    )) {
                    repostContainer.foreground = ContextCompat.getDrawable(context, typedValue.resourceId)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not set ripple background for main container: ${e.message}")
            }
            repostContainer.contentDescription = "Repost, tap to view full post"
            repostContainer.elevation = 4f

            // Ensure quoted card is clickable
            quotedPostCard.isClickable = true
            quotedPostCard.isFocusable = true
            try {
                val typedValue = TypedValue()
                val context = itemView.context
                if (context.theme.resolveAttribute(
                        android.R.attr.selectableItemBackground,
                        typedValue, true
                    )) {
                    quotedPostCard.foreground = ContextCompat.getDrawable(context, typedValue.resourceId)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not set ripple background for quoted card: ${e.message}")
            }
            quotedPostCard.contentDescription = "Quoted post, tap to view full post"
            quotedPostCard.elevation = 4f

            Log.d(TAG, "Post clickability ensured for post: ${data._id}")
        }

        private fun setupContentAndTags(data: com.uyscuti.social.network.api.response.posts.Post) {

            // Caption setup - this is the reposter's comment
            if (data.content.isNotEmpty()) {
                userComment.text = data.content
                userComment.visibility = View.VISIBLE
            } else {
                userComment.visibility = View.GONE
            }

            // Tags setup - these are the reposter's tags
            if (data.tags.isNotEmpty()) {
                tvHashtags.visibility = View.VISIBLE
                val formattedTags = data.tags.joinToString(" ") {
                    val tag = it.toString()
                    if (tag.startsWith("#")) tag else "#$tag"
                }
                tvHashtags.text = formattedTags
            } else {
                tvHashtags.visibility = View.GONE
            }

            // Post tag setup - differentiate between simple repost and quote repost
            if (data.files.isNotEmpty() && data.originalPost?.isNotEmpty() == true) {
                // Quote repost with new content
                tvPostTag.text = "Quote Repost with New Content!"
            } else if (data.content.isNotEmpty() && data.originalPost?.isNotEmpty() == true) {
                // Quote repost with comment only
                tvPostTag.text = "Quote Repost!"
            } else if (data.originalPost?.isNotEmpty() == true) {
                // Simple repost
                tvPostTag.text = "Reposted!"
            } else {
                // Regular post
                tvPostTag.text = "New Post!"
            }
            tvPostTag.visibility = View.VISIBLE
        }

        private fun setupNewPostMediaFiles(data: com.uyscuti.social.network.api.response.posts.Post) {

            val fileList: MutableList<String> = mutableListOf()

            // Check if this is a repost with new files added by the reposter
            if (data.files.isNotEmpty() && data.isReposted == true) {
                Log.d(TAG, "Setting up new post media files for reposter")
                data.files.forEach { file ->
                    Log.d(TAG, "New Post File URL: ${file.url}")
                    fileList.add(file.url)
                }
            } else if (data.files.isNotEmpty() && data.originalPost?.isNotEmpty() == true) {
                // This is a quote repost with new content
                Log.d(TAG, "Setting up new content for quote repost")
                data.files.forEach { file ->
                    Log.d(TAG, "Quote Repost New File URL: ${file.url}")
                    fileList.add(file.url)
                }
            } else if (data.files.isEmpty() && data.originalPost?.isNotEmpty() == true) {
                // Pure repost without new files
                Log.d(TAG, "Pure repost without new files")
                newPostMediaCard.visibility = View.GONE
                return
            } else {
                Log.d(TAG, "No new files to display")
                newPostMediaCard.visibility = View.GONE
                return
            }

            // Setup New Post Media RecyclerView
            newPostMediaCard.visibility = View.VISIBLE

            when (fileList.size) {
                1 -> {
                    // Single file - show in ImageView
                    newPostImage.visibility = View.VISIBLE
                    newPostMultipleMediaContainer.visibility = View.GONE
                    Glide.with(itemView.context)
                        .load(fileList[0])
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(com.uyscuti.sharedmodule.R.drawable.imageplaceholder)
                        .error(com.uyscuti.sharedmodule.R.drawable.imageplaceholder)
                        .into(newPostImage)

                    newPostImage.setOnClickListener {
                        navigateToTappedFilesInTheContainerView(data.files, "new_post_single_image", 0)
                    }
                }
                else -> {
                    // Multiple files - use RecyclerView
                    newPostImage.visibility = View.GONE
                    newPostMultipleMediaContainer.visibility = View.VISIBLE

                    newPostMediaRecyclerView.layoutManager = when (fileList.size) {
                        2 -> StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                        3 -> StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                        else -> GridLayoutManager(itemView.context, 2)
                    }

                    newPostMediaRecyclerView.setHasFixedSize(true)

                    // Setup adapter for new post media
                    val adapter = FeedMixedFilesViewAdapter(data)
                    newPostMediaRecyclerView.adapter = adapter

                    adapter.setOnMultipleFilesClickListener(object :
                        OnMultipleFilesClickListener {
                        override fun multipleFileClickListener(
                            position: Int, // Changed from currentIndex to position
                            files: List<com.uyscuti.social.network.api.response.posts.File>,
                            fileIds: List<String>
                        ) {
                            navigateToTappedFilesInTheContainerView(files, "new_post_multiple_files", position)
                        }
                    })
                }
            }
        }

        private fun setupOriginalPostContent(data: com.uyscuti.social.network.api.response.posts.Post) {

            Log.d(TAG, "Original Post: ${data.originalPost}")

            if (data.originalPost != null && data.originalPost.isNotEmpty()) {

                val originalPostData = data.originalPost[0]
                quotedPostCard.visibility = View.VISIBLE
                setupOriginalPosterInfo(originalPostData)
                setupOriginalPostText(originalPostData)
                setupOriginalPostHashtags(originalPostData)
                setupOriginalPostMedia(originalPostData)

            } else {
                Log.e(TAG, "render: originalPost is null or empty")
                quotedPostCard.visibility = View.GONE
            }
        }

        private fun setupOriginalPosterInfo(originalPostData: OriginalPost) {

            Log.d(TAG, "ORIGINAL POSTER Details: ${originalPostData}")

            try {
                Log.d(TAG, "Setting up original poster info")
                Log.d(TAG, "originalPostReposter size: ${originalPostData.originalPostReposter.size}")

                var displayName = " "
                var userHandle = " "
                var avatarUrl: String? = null

                // Use author data (AuthorX is a single object, not a list)
                val author = originalPostData.author
                avatarUrl = author.account.avatar.url

                // Build display name from author data - prioritize full name over username
                displayName = when {
                    // Try to build full name first
                    author.firstName.isNotBlank() && author.lastName.isNotBlank() ->
                        "${author.firstName} ${author.lastName}"
                    author.firstName.isNotBlank() -> author.firstName
                    author.lastName.isNotBlank() -> author.lastName
                    // Fall back to account username
                    author.account.username.isNotBlank() -> author.account.username
                    // Final fallback
                    else -> "Unknown User"
                }

                userHandle = if (author.account.username.isNotBlank()) {
                    "@${author.account.username}"
                } else {
                    "@unknown_user"
                }
                Log.d(TAG, "Using author data: $displayName ($userHandle)")

                // Set profile image
                originalPosterProfileImage?.let { imageView ->
                    if (!avatarUrl.isNullOrBlank()) {
                        Glide.with(itemView.context)
                            .load(avatarUrl)
                            .apply(RequestOptions.bitmapTransform(CircleCrop()))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(com.uyscuti.sharedmodule.R.drawable.flash21)
                            .error(com.uyscuti.sharedmodule.R.drawable.flash21)
                            .into(imageView)
                        Log.d(TAG, "Loading avatar: $avatarUrl")
                    } else {
                        imageView.setImageResource(com.uyscuti.sharedmodule.R.drawable.flash21)
                        Log.d(TAG, "No avatar URL available, using default image")
                    }
                }

                // Set display name and handle
                originalPosterName?.text = displayName
                tvQuotedUserHandle?.text = userHandle

                Log.d(TAG, "Final UI setup - Name: '$displayName', Handle: '$userHandle'")

            } catch (e: Exception) {
                Log.e(TAG, "Error setting up original poster info", e)
                // Set safe fallback values
                originalPosterProfileImage?.setImageResource(com.uyscuti.sharedmodule.R.drawable.flash21)
                originalPosterName?.text = "Unknown User"
                tvQuotedUserHandle?.text = "@unknown_user"
            }
        }

        private fun setupOriginalPostText(originalPostData: OriginalPost) {

            if (!originalPostData.content.isNullOrBlank()) {
                originalPostText.visibility = View.VISIBLE
                originalPostText.text = originalPostData.content
            } else {
                originalPostText.visibility = View.GONE
            }
        }

        private fun setupOriginalPostHashtags(originalPostData: OriginalPost) {

            val validTags = originalPostData.tags?.filterNotNull()
                ?.mapNotNull { it.toString().takeIf { tag -> tag.isNotBlank() } }

            if (!validTags.isNullOrEmpty()) {
                val hashtagText = validTags.joinToString(" ") {
                    if (it.startsWith("#")) it else "#$it"
                }
                tvQuotedHashtags.text = hashtagText
                tvQuotedHashtags.visibility = View.VISIBLE

            } else {
                tvQuotedHashtags.text = "#SoftwareAppreciation #GameChanger #MustHave"
                tvQuotedHashtags.visibility = View.VISIBLE
            }
        }

        private fun setupOriginalPostMedia(originalPostData: OriginalPost) {

            hideAllOriginalMediaViews()

            try {
                // Check file count first - if exactly 3 files, always use the 3-item layout
                if (originalPostData.files.size == 3) {
                    mixedFilesCardView.visibility = View.VISIBLE
                    originalFeedImage.visibility = View.GONE
                    multipleAudiosContainer.visibility = View.VISIBLE
                    val images = originalPostData.files.map { it.url }
                    val adapter = FeedRepostViewFileAdapter(originalPostData)
                    setupCleanOriginalRecyclerView(3, adapter)
                    return
                }

                // For other counts, use the existing content type logic
                when (originalPostData.contentType) {
                    "text" -> {
                        // Only text content, no media
                    }
                    "image" -> {
                        if (originalPostData.files.isNotEmpty()) {
                            mixedFilesCardView.visibility = View.VISIBLE
                            originalFeedImage.visibility = View.VISIBLE
                            multipleAudiosContainer.visibility = View.GONE
                            originalFeedImage.let { imageView ->
                                Glide.with(itemView.context)
                                    .load(originalPostData.files[0].url)
                                    .placeholder(com.uyscuti.sharedmodule.R.drawable.imageplaceholder)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(imageView)
                                imageView.setOnClickListener {
                                    navigateToTappedFilesInTheContainerView(
                                        originalPostData.files,
                                        "original_single_image",
                                        0
                                    )
                                }
                            }
                        }
                    }

                    "mixed_files" -> {
                        if (originalPostData.files.isNotEmpty()) {
                            mixedFilesCardView.visibility = View.VISIBLE
                            originalFeedImage.visibility = View.GONE
                            multipleAudiosContainer.visibility = View.VISIBLE
                            val images = originalPostData.files.map { it.url }
                            val adapter = FeedRepostViewFileAdapter(originalPostData)
                            setupCleanOriginalRecyclerView(originalPostData.files.size, adapter)
                        }
                    }

                    "video" -> {
                        if (originalPostData.files.isNotEmpty()) {
                            mixedFilesCardView.visibility = View.VISIBLE
                            originalFeedImage.visibility = View.GONE
                            multipleAudiosContainer.visibility = View.VISIBLE
                            val images = originalPostData.files.map { it.url }
                            val adapter = FeedRepostViewFileAdapter(originalPostData)
                            setupCleanOriginalRecyclerView(originalPostData.files.size, adapter)
                        }
                    }

                    else -> {
                        Log.w(TAG, "Unknown content type: ${originalPostData.contentType}")
                    }

                }

            } catch (e: Exception) {
                Log.e(TAG, "Error setting up original post media: ${e.message}")
                hideAllOriginalMediaViews()
            }
        }

        private fun setupCleanOriginalRecyclerView(fileCount: Int, adapter: FeedRepostViewFileAdapter) {

            recyclerView.let { recyclerView ->

                recyclerView.visibility = View.VISIBLE
                when (fileCount) {
                    1 -> recyclerView.layoutManager = GridLayoutManager(itemView.context, 1)
                    2 -> recyclerView.layoutManager = GridLayoutManager(itemView.context, 2)
                    3 -> {
                        recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                    }
                    else -> recyclerView.layoutManager = GridLayoutManager(itemView.context, 2)
                }

                recyclerView.setHasFixedSize(true)
                recyclerView.adapter = adapter
            }
        }

        private fun hideAllOriginalMediaViews() {
            mixedFilesCardView.visibility = View.GONE
            originalFeedImage.visibility = View.GONE
            multipleAudiosContainer.visibility = View.GONE
            recyclerView.visibility = View.GONE
            ivQuotedPostImage.visibility = View.GONE
        }

        private fun navigateToFragment(fragment: Fragment, tag: String) {

            try {

                val activity = getActivityFromContext(itemView.context)

                if (activity != null) {

                    val currentFragment = activity.supportFragmentManager.fragments.lastOrNull {
                        it.isVisible && it.view != null
                    }

                    val fragmentManager = if (currentFragment != null &&
                        currentFragment.childFragmentManager.fragments.isNotEmpty()) {
                        currentFragment.childFragmentManager

                    } else {
                        activity.supportFragmentManager
                    }

                    fragmentManager.beginTransaction()
                        .setCustomAnimations(
                            com.uyscuti.sharedmodule.R.anim.slide_in_right,
                            com.uyscuti.sharedmodule.R.anim.slide_out_left,
                            com.uyscuti.sharedmodule.R.anim.slide_in_left,
                            com.uyscuti.sharedmodule.R.anim.slide_out_right
                        )
                        .replace(com.uyscuti.sharedmodule.R.id.frame_layout, fragment)
                        .addToBackStack(tag)
                        .commit()
                    Log.d(TAG, "Successfully navigated to fragment: $tag")

                } else {
                    Log.e(TAG, "Activity is null, cannot navigate to fragment: $tag")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error navigating to fragment: $tag", e)
            }
        }

        private fun setupPostInfo(data: com.uyscuti.social.network.api.response.posts.Post) {
            // Date and time
            dateTimeCreate.text = formattedMongoDateTime(data.createdAt)

            // Comment count
            initializeCommentCounts(data)
            updateCommentCountDisplay()

            // Initialize all counts
            updateEngagementCounts(data)
        }

        private fun initializeCommentCounts(data: com.uyscuti.social.network.api.response.posts.Post) {
            serverCommentCount = data.comments
            totalMixedComments = serverCommentCount
            loadedCommentCount = 0
            Log.d(TAG, "Initialized comment counts - Server: $serverCommentCount, Total: $totalMixedComments")
        }

        private fun updateCommentCountDisplay() {
            feedCommentsCount.text = formatCount(totalMixedComments)
            feedCommentsCount.visibility = View.VISIBLE
            Log.d(TAG, "Updated comment count display: ${feedCommentsCount.text}")
        }

        private fun setupUserInfo(data: com.uyscuti.social.network.api.response.posts.Post, feedOwnerId: String) {
            // Profile image and user info from reposter
            val repostedUser = data.repostedUser

            if (repostedUser != null) {
                val avatarUrl = repostedUser.avatar?.url
                loadImageWithGlide(avatarUrl, userProfileImage, itemView.context)
                repostedUserName.text = repostedUser.username
                tvUserHandle.text = "@${repostedUser.username}"

            } else {
                // Fallback to original author if reposter info is not available
                val avatarUrl = data.author?.account?.avatar?.url
                loadImageWithGlide(avatarUrl, userProfileImage, itemView.context)

                val fullName = listOfNotNull(
                    data.author?.firstName?.takeIf { it.isNotBlank() },
                    data.author?.lastName?.takeIf { it.isNotBlank() }
                ).joinToString(" ").trim()
                repostedUserName.text = if (fullName.isNotEmpty()) fullName else data.author?.account?.username ?: "Unknown User"
                tvUserHandle.text = "@${data.author?.account?.username ?: "unknown"}"
            }
        }

        private fun setupEngagementButtons(data: com.uyscuti.social.network.api.response.posts.Post) {
            setupLikeButton(data)
            setupCommentButton(data)
            setupShareButton(data)
            setupRepostButton(data)
            setupBookmarkButton(data)
            setupMoreOptionsButton(data)
        }

        private fun setupLikeButton(data: com.uyscuti.social.network.api.response.posts.Post) {

            Log.d(TAG, "Setting up like button - Initial state: isLiked=${data.isLiked}, likes=${totalMixedLikesCounts}")
            updateLikeButtonUI(data.isLiked ?: false)
            updateMetricDisplay(likesCount, totalMixedLikesCounts, "like")

            likeButton.setOnClickListener {
                if (!likeButton.isEnabled) return@setOnClickListener

                Log.d(TAG, "Like clicked for post: ${data._id}")
                val newLikeStatus = !(data.isLiked ?: false)
                val previousLikeStatus = data.isLiked ?: false
                val previousLikesCount = totalMixedLikesCounts

                // Update data immediately
                data.isLiked = newLikeStatus
                totalMixedLikesCounts = if (newLikeStatus) totalMixedLikesCounts + 1 else maxOf(0, totalMixedLikesCounts - 1)
                data.likes = totalMixedLikesCounts

                // Update UI immediately for better UX
                updateLikeButtonUI(data.isLiked ?: false)
                updateMetricDisplay(likesCount, data.likes, "like")

                // Animation
                YoYo.with(if (newLikeStatus) Techniques.Tada else Techniques.Pulse)
                    .duration(300)
                    .repeat(1)
                    .playOn(likeButton)

                // Disable button during network call
                likeButton.isEnabled = false
                likeButton.alpha = 0.8f

                val likeRequest = LikeRequest(newLikeStatus)
                RetrofitClient.likeService.toggleLike(data._id, likeRequest)
                    .enqueue(object : Callback<LikeResponse> {
                        override fun onResponse(call: Call<LikeResponse>, response: Response<LikeResponse>) {
                            likeButton.alpha = 1f
                            likeButton.isEnabled = true

                            if (response.isSuccessful) {
                                response.body()?.let { likeResponse ->
                                    Log.d(TAG, "Like API success - Server count: ${likeResponse.likesCount}")
                                    if (likeResponse.likesCount != null &&
                                        abs(likeResponse.likesCount - data.likes) > 1) {
                                        data.likes = likeResponse.likesCount
                                        totalMixedLikesCounts = data.likes
                                        updateMetricDisplay(likesCount, data.likes, "like")
                                    }
                                }
                            } else {
                                Log.e(TAG, "Like sync failed: ${response.code()}")
                                if (response.code() != 200) {
                                    data.isLiked = previousLikeStatus
                                    data.likes = previousLikesCount
                                    totalMixedLikesCounts = data.likes
                                    updateLikeButtonUI(data.isLiked ?: false)
                                    updateMetricDisplay(likesCount, data.likes, "like")
                                }
                            }
                        }

                        override fun onFailure(call: Call<LikeResponse>, t: Throwable) {
                            likeButton.alpha = 1f
                            likeButton.isEnabled = true

                            if (t is MalformedJsonException ||
                                t.message?.contains("MalformedJsonException") == true) {
                                Log.w(TAG, "Like API returned malformed JSON but operation likely succeeded")
                                return
                            }

                            Log.e(TAG, "Like network error - reverting changes", t)
                            data.isLiked = previousLikeStatus
                            data.likes = previousLikesCount
                            totalMixedLikesCounts = data.likes
                            updateLikeButtonUI(data.isLiked ?: false)
                            updateMetricDisplay(likesCount, data.likes, "like")
                        }
                    })

                feedClickListener.likeUnLikeFeed(absoluteAdapterPosition, data)
            }
        }

        private fun setupCommentButton(data: com.uyscuti.social.network.api.response.posts.Post) {
            commentButton.setOnClickListener {
                if (!commentButton.isEnabled) return@setOnClickListener
                Log.d(TAG, "Comment button clicked for post ${data._id}")

                YoYo.with(Techniques.Tada)
                    .duration(700)
                    .repeat(1)
                    .playOn(commentButton)

                feedClickListener.feedCommentClicked(absoluteAdapterPosition, data)
                commentButton.isEnabled = true
            }

            feedCommentsCount.setOnClickListener {
                if (!feedCommentsCount.isEnabled) return@setOnClickListener
                YoYo.with(Techniques.Tada)
                    .duration(700)
                    .repeat(1)
                    .playOn(feedCommentsCount)
                feedClickListener.feedCommentClicked(absoluteAdapterPosition, data)
            }
        }

        fun updateCommentCount(newCount: Int) {

            Log.d(TAG, "updateCommentCount: Updating comment count from $totalRepostComments to $newCount")
            totalRepostComments = if (newCount < 0) {
                Log.w(TAG, "updateCommentCount: Negative count received, setting to 0")
                0
            } else {
                newCount
            }

            currentPost?.let { post ->
                post.comments = totalRepostComments
            }

            feedCommentsCount.text = formatCount(totalRepostComments)
            YoYo.with(Techniques.Pulse)
                .duration(500)
                .playOn(feedCommentsCount)
        }

        fun decrementCommentCount() {
            val newCount = maxOf(0, totalRepostComments - 1)
            Log.d(TAG, "decrementCommentCount: Decrementing from $totalRepostComments to $newCount")
            updateCommentCount(newCount)
        }

        fun incrementCommentCount() {
            val newCount = totalRepostComments + 1
            Log.d(TAG, "incrementCommentCount: Incrementing from $totalRepostComments to $newCount")
            updateCommentCount(newCount)
        }

        private fun setupBookmarkButton(data: com.uyscuti.social.network.api.response.posts.Post) {

            Log.d(TAG,
                "Setting up bookmark button - Initial state: isBookmarked=${data.isBookmarked}," +
                        " bookmarkCount=${totalMixedBookMarkCounts}")

            updateBookmarkButtonUI(data.isBookmarked ?: false)
            updateMetricDisplay(favoritesCount, totalMixedBookMarkCounts, "bookmark")

            favoriteButton.setOnClickListener {
                if (!favoriteButton.isEnabled) return@setOnClickListener

                Log.d(TAG, "Bookmark clicked for post: ${data._id}")
                val newBookmarkStatus = !(data.isBookmarked ?: false)
                val previousBookmarkStatus = data.isBookmarked ?: false
                val previousBookmarkCount = totalMixedBookMarkCounts

                // Update data immediately
                data.isBookmarked = newBookmarkStatus
                totalMixedBookMarkCounts = if (newBookmarkStatus) totalMixedBookMarkCounts + 1 else maxOf(0, totalMixedBookMarkCounts - 1)
                data.bookmarkCount = totalMixedBookMarkCounts

                // Update UI immediately for better UX
                updateBookmarkButtonUI(data.isBookmarked ?: false)
                updateMetricDisplay(favoritesCount, data.bookmarkCount, "bookmark")

                // Animation
                YoYo.with(if (newBookmarkStatus) Techniques.Tada else Techniques.Pulse)
                    .duration(500)
                    .repeat(1)
                    .playOn(favoriteButton)

                // Disable button during network call
                favoriteButton.isEnabled = false
                favoriteButton.alpha = 0.8f

                val bookmarkRequest = BookmarkRequest(newBookmarkStatus)

                RetrofitClient.bookmarkService.toggleBookmark(data._id, bookmarkRequest)
                    .enqueue(object : Callback<BookmarkResponse> {
                        override fun onResponse(call: Call<BookmarkResponse>, response: Response<BookmarkResponse>) {
                            favoriteButton.alpha = 1f
                            favoriteButton.isEnabled = true

                            if (response.isSuccessful) {
                                response.body()?.let { bookmarkResponse ->
                                    if (abs(bookmarkResponse.bookmarkCount - data.bookmarkCount) > 1) {
                                        data.bookmarkCount = bookmarkResponse.bookmarkCount
                                        totalMixedBookMarkCounts = data.bookmarkCount
                                        updateMetricDisplay(favoritesCount, data.bookmarkCount, "bookmark")
                                    }
                                }
                            } else {
                                if (response.code() != 200) {
                                    data.isBookmarked = previousBookmarkStatus
                                    data.bookmarkCount = previousBookmarkCount
                                    totalMixedBookMarkCounts = data.bookmarkCount
                                    updateBookmarkButtonUI(data.isBookmarked ?: false)
                                    updateMetricDisplay(favoritesCount, data.bookmarkCount, "bookmark")
                                }
                            }
                        }

                        override fun onFailure(call: Call<BookmarkResponse>, t: Throwable) {
                            favoriteButton.alpha = 1f
                            favoriteButton.isEnabled = true

                            if (t is MalformedJsonException ||
                                t.message?.contains("MalformedJsonException") == true) {
                                Log.w(TAG, "Bookmark API returned malformed JSON but operation likely succeeded")
                                return
                            }

                            data.isBookmarked = previousBookmarkStatus
                            data.bookmarkCount = previousBookmarkCount
                            totalMixedBookMarkCounts = data.bookmarkCount
                            updateBookmarkButtonUI(data.isBookmarked ?: false)
                            updateMetricDisplay(favoritesCount, data.bookmarkCount, "bookmark")
                        }
                    })

                feedClickListener.feedFavoriteClick(absoluteAdapterPosition, data)
            }
        }

        private fun setupRepostButton(data: com.uyscuti.social.network.api.response.posts.Post) {

            totalMixedRePostCounts = 0
            updateMetricDisplay(repostCountTextView, totalMixedRePostCounts, "repost")
            updateRepostButtonAppearance(data.isReposted)

            repostPost.setOnClickListener { view ->
                if (!repostPost.isEnabled) return@setOnClickListener
                repostPost.isEnabled = false

                try {
                    val wasReposted = data.isReposted
                    data.isReposted = !wasReposted
                    totalMixedRePostCounts = if (data.isReposted) totalMixedRePostCounts + 1 else maxOf(0, totalMixedRePostCounts - 1)
                    updateMetricDisplay(repostCountTextView, totalMixedRePostCounts, "repost")
                    updateRepostButtonAppearance(data.isReposted)

                    YoYo.with(if (data.isReposted) Techniques.Tada else Techniques.Pulse)
                        .duration(700)
                        .playOn(repostPost)

                    repostPost.alpha = 0.8f
                    val apiCall = if (data.isReposted) {
                        RetrofitClient.repostService.incrementRepost(data._id)
                    } else {
                        RetrofitClient.repostService.decrementRepost(data._id)
                    }

                    apiCall.enqueue(object : Callback<RepostResponse> {
                        override fun onResponse(call: Call<RepostResponse>, response: Response<RepostResponse>) {
                            repostPost.isEnabled = true
                            repostPost.alpha = 1f
                            if (response.isSuccessful) {
                                response.body()?.let { repostResponse ->
                                    if (abs(repostResponse.repostCount - totalMixedRePostCounts) > 1) {
                                        totalMixedRePostCounts = repostResponse.repostCount
                                        updateMetricDisplay(repostCountTextView, totalMixedRePostCounts, "repost")
                                    }
                                }
                            }
                        }

                        override fun onFailure(call: Call<RepostResponse>, t: Throwable) {
                            repostPost.isEnabled = true
                            repostPost.alpha = 1f
                            Log.e(TAG, "Repost network error - will sync later", t)
                        }
                    })

                    if (data.isReposted) {
                        navigateToEditPostToRepost(data)
                    }
                    feedClickListener.feedRepostPost(absoluteAdapterPosition, data)
                } catch (e: Exception) {
                    repostPost.isEnabled = true
                    repostPost.alpha = 1f
                    Log.e(TAG, "Exception in repost click listener", e)
                }
            }
        }

        private fun updateRepostButtonAppearance(isReposted: Boolean) {

            if (isReposted) {

                repostPost.setImageResource(com.uyscuti.sharedmodule.R.drawable.repeat_svgrepo_com)
                repostPost.scaleX = 1.1f
                repostPost.scaleY = 1.1f

            } else {
                repostPost.setImageResource(com.uyscuti.sharedmodule.R.drawable.repeat_svgrepo_com)
                repostPost.scaleX = 1.0f
                repostPost.scaleY = 1.0f
            }
        }

        private fun setupShareButton(data: com.uyscuti.social.network.api.response.posts.Post) {

            totalMixedShareCounts = data.shareCount ?: data.shareCount ?: 0
            updateMetricDisplay(shareCountTextView, totalMixedShareCounts, "share")

            shareImageView.setOnClickListener {
                if (!shareImageView.isEnabled) return@setOnClickListener

                Log.d(TAG, "Share clicked for Post: ${data._id}")
                val previousShareCount = totalMixedShareCounts

                // Update immediately for better UX
                totalMixedShareCounts += 1
                data.shareCount = totalMixedShareCounts
                updateMetricDisplay(shareCountTextView, totalMixedShareCounts, "share")

                YoYo.with(Techniques.Tada)
                    .duration(700)
                    .repeat(1)
                    .playOn(shareImageView)

                shareImageView.isEnabled = false
                shareImageView.alpha = 0.8f

                // Make API call to sync with server
                RetrofitClient.shareService.incrementShare(data._id)
                    .enqueue(object : Callback<ShareResponse> {
                        override fun onResponse(call: Call<ShareResponse>, response: Response<ShareResponse>) {
                            shareImageView.alpha = 1f
                            shareImageView.isEnabled = true

                            if (response.isSuccessful) {
                                response.body()?.let { shareResponse ->
                                    if (abs(shareResponse.shareCount - 0) > 1) {
                                        data.shareCount = shareResponse.shareCount
                                        totalMixedShareCounts = data.shareCount
                                        updateMetricDisplay(shareCountTextView, data.shareCount, "share")
                                    }
                                }
                            } else {
                                data.shareCount = previousShareCount
                                totalMixedShareCounts = data.shareCount
                                updateMetricDisplay(shareCountTextView, data.shareCount, "share")
                            }
                        }

                        override fun onFailure(call: Call<ShareResponse>, t: Throwable) {
                            shareImageView.alpha = 1f
                            shareImageView.isEnabled = true
                            data.shareCount = previousShareCount
                            totalMixedShareCounts = data.shareCount
                            updateMetricDisplay(shareCountTextView, data.shareCount, "share")
                        }
                    })

                feedClickListener.feedShareClicked(absoluteAdapterPosition, data)
            }
        }

        private fun setupMoreOptionsButton(data: com.uyscuti.social.network.api.response.posts.Post) {
            moreOptionsButton.setOnClickListener {
                Log.d(TAG, "More options clicked for post: ${data._id}")
                showMoreOptionsDialog(data)
            }
        }

        private fun updateLikeButtonUI(isLiked: Boolean) {

            Log.d(TAG, "Updating like button UI: isLiked=$isLiked")
            try {
                if (isLiked) {
                    likeButton.setImageResource(com.uyscuti.sharedmodule.R.drawable.filled_favorite_like)
                } else {
                    likeButton.setImageResource(com.uyscuti.sharedmodule.R.drawable.heart_svgrepo_com)
                    likeButton.clearColorFilter()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating like button UI", e)
            }
        }

        private fun updateBookmarkButtonUI(isBookmarked: Boolean) {

            Log.d(TAG, "Updating bookmark button UI: isBookmarked=$isBookmarked")
            try {
                if (isBookmarked) {
                    favoriteButton.setImageResource(com.uyscuti.sharedmodule.R.drawable.filled_favorite)
                } else {
                    favoriteButton.setImageResource(com.uyscuti.sharedmodule.R.drawable.favorite_svgrepo_com__1_)
                    favoriteButton.clearColorFilter()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating bookmark button UI", e)
            }
        }

        private fun navigateToEditPostToRepost(data: com.uyscuti.social.network.api.response.posts.Post) {

            try {

                val fragment = Fragment_Edit_Post_To_Repost(data).apply {
                    arguments = Bundle().apply {
                        putString("post_data", Gson().toJson(data))
                        putString("post_id", data._id)
                        data.originalPost.firstOrNull()?.let { originalPost ->
                            putString("original_post_data", Gson().toJson(originalPost))
                            putString("original_post_id", originalPost._id)
                            putString("original_content", originalPost.content)
                            putString("original_content_type", originalPost.contentType)
                            putString("original_created_at", originalPost.createdAt)
                            putString("original_author_id", originalPost.author._id)
                            putString("original_author_username", originalPost.author.account.username)
                            putString(
                                "original_author_display_name",
                                listOfNotNull(
                                    originalPost.author.firstName.takeIf { it.isNotBlank() },
                                    originalPost.author.lastName.takeIf { it.isNotBlank() }
                                ).joinToString(" ").trim().takeIf { it.isNotEmpty() }
                                    ?: originalPost.author.account.username
                            )
                            putString("original_author_avatar", originalPost.author.account.avatar.url)

                            if (originalPost.files.isNotEmpty()) {
                                putString("original_files_data", Gson().toJson(originalPost.files))
                                putInt("original_files_count", originalPost.files.size)
                            }
                        }

                        val currentUser = LocalStorage.getInstance(itemView.context).getUser() as? com.uyscuti.sharedmodule.model.business.User
                        currentUser?.let { user ->
                            putString("current_user_id", user._id)
                            putString("current_user_username", user.account?.username)
                            putString(
                                "current_user_avatar",
                                when {
                                    user.avatar is Avatar -> user.avatar.url
                                    user.avatar is String -> user.avatar
                                    else -> null
                                }.toString()
                            )
                        }
                        putString("repost_type", "quote_repost")
                        putString("existing_comment", data.content)
                        putBoolean("is_editing_existing_repost", data.isReposted)
                        putInt("adapter_position", absoluteAdapterPosition)
                        putString("navigation_source", "repost_button_click")
                        putLong("navigation_timestamp", System.currentTimeMillis())
                    }
                }
                navigateToFragment(fragment, "edit_post_to_repost")
            } catch (e: Exception) {
                Log.e(TAG, "Error navigating to edit post fragment: ${e.message}")
                e.printStackTrace()
            }
        }

        private fun updateEngagementCounts(data: com.uyscuti.social.network.api.response.posts.Post) {
            updateMetricDisplay(likesCount, data.likes, "like")
            updateMetricDisplay(feedCommentsCount, data.comments, "comment")
            updateMetricDisplay(favoritesCount, data.bookmarkCount, "bookmark")
            updateMetricDisplay(repostCountTextView, data.repostCount, "repost")
            updateMetricDisplay(shareCountTextView, data.shareCount, "share")
        }

        private fun updateMetricDisplay(textView: TextView, count: Int, type: String) {
            textView.text = formatCount(count)
            Log.d(TAG, "Updated $type count display: ${textView.text}")
        }

        private fun formatCount(count: Int): String {
            return when {
                count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
                count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
                else -> count.toString()
            }
        }

        private fun setupProfileClickListeners(data: com.uyscuti.social.network.api.response.posts.Post, feedOwnerId: String) {
            val profileClickListener = View.OnClickListener {
                Log.d(TAG, "Profile clicked for user: $feedOwnerId")

            }

            userProfileImage.setOnClickListener(profileClickListener)
            repostedUserName.setOnClickListener(profileClickListener)
            tvUserHandle.setOnClickListener(profileClickListener)

            // Original poster profile click
            val originalProfileClickListener = View.OnClickListener {
                val originalPosterId = if (data.originalPost.isNotEmpty()) {
                    val originalPost = data.originalPost[0]
                    // Get the author ID from the original post's author (AuthorX object)
                    originalPost.author.account._id
                } else {
                    null
                }

                if (originalPosterId != null) {
                    Log.d(TAG, "Original profile clicked for user: $originalPosterId")
                    // Add your navigation logic here
                }
            }

            originalPosterProfileImage.setOnClickListener(originalProfileClickListener)
            originalPosterName.setOnClickListener(originalProfileClickListener)
            tvQuotedUserHandle.setOnClickListener(originalProfileClickListener)
        }

        private fun setupFollowButton(accountId: String, username: String) {
            val currentUserId = LocalStorage.getInstance(itemView.context).getUserId()

            val cachedFollowingList = getCachedFollowingList()
            val cachedFollowingUsernames = getCachedFollowingUsernames()

            // Check by BOTH ID and USERNAME
            val isUserFollowing = followingUserIds.contains(accountId) ||
                    cachedFollowingList.contains(accountId) ||
                    cachedFollowingUsernames.contains(username)

            Log.d(TAG, "───────────────────────────────────────")
            Log.d(TAG, "SETUP FOLLOW BUTTON")
            Log.d(TAG, "Account ID to check: $accountId")
            Log.d(TAG, "Username to check: @$username")
            Log.d(TAG, "Current user ID: $currentUserId")
            Log.d(TAG, "isFollowingUser (from render): $isFollowingUser")
            Log.d(TAG, "isUserFollowing (local check): $isUserFollowing")
            Log.d(TAG, "Match in followingUserIds: ${followingUserIds.contains(accountId)}")
            Log.d(TAG, "Match in cachedFollowingList: ${cachedFollowingList.contains(accountId)}")
            Log.d(TAG, "Match by username: ${cachedFollowingUsernames.contains(username)}")

            // Hide button if it's own post OR already following (by ID or username)
            val shouldHideButton = accountId == currentUserId || isFollowingUser || isUserFollowing

            if (shouldHideButton) {
                followButton.visibility = View.GONE
                Log.d(TAG, "✓✓✓ HIDING follow button - Reason: ${when {
                    accountId == currentUserId -> "Own post"
                    cachedFollowingUsernames.contains(username) -> "Already following (by username: @$username)"
                    followingUserIds.contains(accountId) -> "Already following (by ID from followingUserIds)"
                    cachedFollowingList.contains(accountId) -> "Already following (by ID from cachedList)"
                    isFollowingUser -> "Already following (render check)"
                    isUserFollowing -> "Already following (local check)"
                    else -> "Unknown"
                }}")
                Log.d(TAG, "───────────────────────────────────────")
                return
            }

            // Show follow button
            followButton.visibility = View.VISIBLE
            followButton.text = "Follow"
            followButton.backgroundTintList = ContextCompat.getColorStateList(
                itemView.context,
                com.uyscuti.sharedmodule.R.color.blueJeans
            )

            Log.d(TAG, "✓✓✓ SHOWING follow button for account: $accountId (@$username)")
            Log.d(TAG, "───────────────────────────────────────")

            // ✅ CRITICAL: Pass the ACCOUNT ID (not author ID) to handleFollowButtonClick
            followButton.setOnClickListener {
                handleFollowButtonClick(accountId, username)  // Pass both ID and username
            }
        }

        @SuppressLint("NotifyDataSetChanged")
        private fun handleFollowButtonClick(accountId: String, username: String) {
            YoYo.with(Techniques.Pulse).duration(300).playOn(followButton)

            Log.d(TAG, "═══════════════════════════════════════")
            Log.d(TAG, "FOLLOW BUTTON CLICKED")
            Log.d(TAG, "Account ID to follow: $accountId")
            Log.d(TAG, "Username to follow: @$username")
            Log.d(TAG, "This ID will be added to following list")
            Log.d(TAG, "═══════════════════════════════════════")

            isFollowed = !isFollowed
            val followEntity = FollowUnFollowEntity(accountId, isFollowed)

            if (isFollowed) {
                // Hide button immediately
                followButton.visibility = View.GONE

                // Add ACCOUNT ID and USERNAME to following list
                (bindingAdapter as? FeedAdapter)?.addToFollowing(accountId, username)
                FollowingManager(itemView.context).addToFollowing(accountId)

                Log.d(TAG, "✓ Added account $accountId (@$username) to following list")
            } else {
                // Show button
                followButton.text = "Follow"
                followButton.visibility = View.VISIBLE

                // Remove from following list
                (bindingAdapter as? FeedAdapter)?.removeFromFollowing(accountId, username)
                FollowingManager(itemView.context).removeFromFollowing(accountId)

                Log.d(TAG, "✓ Removed account $accountId (@$username) from following list")
            }

            // Notify listener
            feedClickListener.followButtonClicked(followEntity, followButton)
            EventBus.getDefault().post(ShortsFollowButtonClicked(followEntity))

            // Refresh adapter
            (bindingAdapter as? FeedAdapter)?.notifyDataSetChanged()
        }


        private fun navigateToOriginalPostWithRepostInside(originalPostData: com.uyscuti.social.network.api.response.posts.Post) {
            try {
                val fragment = Fragment_Original_Post_With_Repost_Inside.newInstance(originalPostData)
                navigateToFragment(fragment, "repost_with_context")
            } catch (e: Exception) {
                Log.e(TAG, "Error navigating to repost fragment: ${e.message}")
                e.printStackTrace()
            }
        }

        private fun navigateToOriginalPostWithoutRepostInside(originalPostData: com.uyscuti.social.network.api.response.posts.Post) {
            try {

            } catch (e: Exception) {
                Log.e(TAG, "Error navigating to original post fragment: ${e.message}")
                e.printStackTrace()
            }
        }

        private fun navigateToTappedFilesInTheContainerView(
            files: List<Any>,
            mediaType: String,
            selectedPosition: Int
        ) {
            try {
                val fragment = Tapped_Files_In_The_Container_View_Fragment().apply {
                    arguments = Bundle().apply {
                        putString("files_data", Gson().toJson(files))
                        putString("media_type", mediaType)
                        putInt("selected_position", selectedPosition)
                        putInt("total_files", files.size)
                        val fileUrls = when {
                            files.first() is com.uyscuti.social.network.api.response.getrepostsPostsoriginal.File -> {
                                (files as List<com.uyscuti.social.network.api.response.getrepostsPostsoriginal.File>).map { it.url }
                            }
                            else -> files.map { it.toString() }
                        }
                        putStringArray("file_urls", fileUrls.toTypedArray())
                        currentPost?.let { post ->
                            putString("post_id", post._id)
                            putString("post_data", Gson().toJson(post))
                            putString("post_author_id", post.repostedUser?._id)
                            putString("post_author_username", post.repostedUser?.username)
                        }
                        if (mediaType.contains("original") || mediaType.contains("quoted")) {
                            currentPost?.originalPost?.firstOrNull()?.let { originalPost ->
                                putString("original_post_id", originalPost._id)
                                putString("original_post_data", Gson().toJson(originalPost))
                                val author = originalPost.author
                                putString("original_author_id", author._id)
                                putString("original_author_username", author.account.username)
                            }
                        }
                        putInt("adapter_position", absoluteAdapterPosition)
                        putString("navigation_source", "feed_reposted_post")
                        putString("media_source", mediaType)
                        putLong("navigation_timestamp", System.currentTimeMillis())
                        putBoolean("can_download", true)
                        putBoolean("can_share", true)
                        putBoolean("show_engagement_data", true)
                    }
                }
                navigateToFragment(fragment, "files_container_view")
            } catch (e: Exception) {
                Log.e(TAG, "Error navigating to files container fragment: ${e.message}")
                e.printStackTrace()
            }
        }



        private fun showMoreOptionsDialog(data: com.uyscuti.social.network.api.response.posts.Post) {
            val options = arrayOf("Report Post", "Hide Post", "Copy Link", "Save Post")
            val context = itemView.context

            AlertDialog.Builder(context)
                .setTitle("Post Options")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> reportPost(data)
                        1 -> hidePost(data)
                        2 -> copyPostLink(data)
                        3 -> savePost(data)
                    }
                }
                .show()
        }

        private fun showRepostDialog(data: com.uyscuti.social.network.api.response.posts.Post) {
            val options = arrayOf("Repost", "Quote Repost")
            val context = itemView.context

            AlertDialog.Builder(context)
                .setTitle("Repost Options")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> repost(data)
                        1 -> quoteRepost(data)
                    }
                }
                .show()
        }

        private fun sharePost(data: com.uyscuti.social.network.api.response.posts.Post) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Check out this post: ${data.content}")
                putExtra(Intent.EXTRA_SUBJECT, "Shared from Social Circuit")
            }

            val context = itemView.context
            context.startActivity(Intent.createChooser(shareIntent, "Share Post"))
        }

        private fun formattedMongoDateTime(dateTimeString: String?): String {
            if (dateTimeString.isNullOrBlank()) return "Unknown Time"
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val date = inputFormat.parse(dateTimeString)
                val now = Date()
                val diffInMillis = now.time - (date?.time ?: 0)
                val diffInSeconds = diffInMillis / 1000
                val diffInMinutes = diffInSeconds / 60
                val diffInHours = diffInMinutes / 60
                val diffInDays = diffInHours / 24
                val diffInWeeks = diffInDays / 7
                val diffInMonths = diffInDays / 30
                val diffInYears = diffInDays / 365

                when {
                    diffInSeconds < 60 -> "now"
                    diffInMinutes < 60 -> "${diffInMinutes}m"
                    diffInHours < 24 -> "${diffInHours}h"
                    diffInDays == 1L -> "1d"
                    diffInDays < 7 -> "${diffInDays}d"
                    diffInWeeks < 4 -> "${diffInWeeks}w"
                    diffInMonths == 0L -> "1mo"
                    diffInMonths == 1L -> "1mo"
                    diffInMonths < 12 -> "${diffInMonths}mo"
                    diffInYears == 1L -> "1y"
                    else -> "${diffInYears}y"
                }
            } catch (e: Exception) {
                Log.w("DateFormat", "Failed to format date: $dateTimeString", e)
                "now"
            }
        }

        private fun loadImageWithGlide(url: String?, imageView: ImageView, context: Context) {
            Glide.with(context)
                .load(url)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(com.uyscuti.sharedmodule.R.drawable.flash21)
                .error(com.uyscuti.sharedmodule.R.drawable.flash21)
                .into(imageView)
        }

        private fun getActivityFromContext(context: Context?): AppCompatActivity? {
            return when (context) {
                is AppCompatActivity -> context
                is ContextWrapper -> getActivityFromContext(context.baseContext)
                else -> null
            }
        }



        private fun reportPost(data: com.uyscuti.social.network.api.response.posts.Post) {
            // Implement report functionality
            Log.d(TAG, "Reporting post: ${data._id}")
        }

        private fun hidePost(data: com.uyscuti.social.network.api.response.posts.Post) {
            // Implement hide post functionality
            Log.d(TAG, "Hiding post: ${data._id}")
        }

        @SuppressLint("ServiceCast")
        private fun copyPostLink(data: com.uyscuti.social.network.api.response.posts.Post) {
            val clipboard = itemView.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Post Link", "https://app.com/post/${data._id}")
            clipboard.setPrimaryClip(clip)
            Toast.makeText(itemView.context, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        private fun savePost(data: com.uyscuti.social.network.api.response.posts.Post) {
            // Implement save post functionality
            Log.d(TAG, "Saving post: ${data._id}")
        }

        private fun repost(data: com.uyscuti.social.network.api.response.posts.Post) {
            // Implement repost functionality
            Log.d(TAG, "Reposting: ${data._id}")
        }

        private fun quoteRepost(data: com.uyscuti.social.network.api.response.posts.Post) {
            // Implement quote repost functionality
            Log.d(TAG, "Quote reposting: ${data._id}")
        }




    }



}





