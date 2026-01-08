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
import android.os.Build
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
import android.widget.ListAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
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
import com.uyscuti.social.business.CatalogueDetailsActivity
import com.uyscuti.social.business.databinding.BusinessPostLayoutBinding
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.ActivitySearchAllUserNameBinding
import com.uyscuti.social.core.common.data.room.entity.DialogEntity
import com.uyscuti.social.network.api.retrofit.interfaces.IFlashapi
import com.uyscuti.social.core.common.data.room.entity.RecentUser
import com.uyscuti.social.core.common.data.room.entity.UserEntity
import com.uyscuti.social.network.api.models.Chat
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
import com.uyscuti.social.network.api.response.posts.AccountB
import com.uyscuti.social.network.api.response.posts.AuthorB
import com.uyscuti.social.network.api.response.posts.AvatarB
import com.uyscuti.social.network.api.response.posts.BusinessPost
import com.uyscuti.social.network.api.response.posts.BusinessProfile
import com.uyscuti.social.business.viewmodel.business.BusinessPostsViewModel
import com.uyscuti.social.circuit.FollowingManager
import com.uyscuti.social.circuit.User_Interface.OtherUserProfile.OtherUserProfileAccount
import com.uyscuti.social.circuit.User_Interface.fragments.feed.feedviewfragments.Fragment_Original_Post_With_Repost_Inside
import com.uyscuti.social.circuit.User_Interface.fragments.feed.feedviewfragments.Fragment_Original_Post_Without_Repost_Inside
import com.uyscuti.social.circuit.User_Interface.fragments.feed.feedviewfragments.editRepost.Fragment_Edit_Post_To_Repost
import com.uyscuti.social.circuit.User_Interface.fragments.feed.feedviewfragments.feedRepost.Tapped_Files_In_The_Container_View_Fragment
import com.uyscuti.social.circuit.adapter.feed.FeedAdapter
import com.uyscuti.social.circuit.adapter.feed.FeedAdapter.Companion.getCachedFollowingList
import com.uyscuti.social.circuit.adapter.feed.FeedAdapter.Companion.getCachedFollowingUsernames
import com.uyscuti.social.circuit.adapter.feed.OnFeedClickListener
import com.uyscuti.social.circuit.data.model.Dialog
import com.uyscuti.social.circuit.data.model.Message
import com.uyscuti.social.circuit.data.model.shortsmodels.OtherUsersProfile
import com.uyscuti.social.circuit.databinding.BottomDialogForShareBinding
import com.uyscuti.social.circuit.model.GoToUserProfileFragment
import com.uyscuti.social.circuit.model.ShortsFollowButtonClicked
import com.uyscuti.social.circuit.presentation.RecentUserViewModel
import com.uyscuti.social.core.common.data.room.entity.FollowUnFollowEntity
import kotlinx.coroutines.CoroutineScope
import java.util.TimeZone
import com.uyscuti.social.circuit.adapter.feed.multiple_files.FeedRepostViewFileAdapter
import com.uyscuti.social.network.api.models.User
import com.uyscuti.social.network.api.response.allFeedRepostsPost.BookmarkRequest
import com.uyscuti.social.network.api.response.allFeedRepostsPost.BookmarkResponse
import com.uyscuti.social.network.api.response.allFeedRepostsPost.CommentCountResponse
import com.uyscuti.social.network.api.response.allFeedRepostsPost.LikeRequest
import com.uyscuti.social.network.api.response.allFeedRepostsPost.LikeResponse
import com.uyscuti.social.network.api.response.allFeedRepostsPost.RepostResponse
import com.uyscuti.social.network.api.response.allFeedRepostsPost.RetrofitClient
import com.uyscuti.social.network.api.response.allFeedRepostsPost.ShareResponse
import com.uyscuti.social.network.api.response.posts.Account
import com.uyscuti.social.network.api.response.posts.Author
import com.uyscuti.social.network.api.response.posts.AuthorX
import com.uyscuti.social.network.api.response.posts.Avatar
import com.uyscuti.social.network.api.response.posts.BackgroundPhoto
import com.uyscuti.social.network.api.response.posts.CoverImage
import com.uyscuti.social.network.api.response.posts.Duration
import com.uyscuti.social.network.api.response.posts.File
import com.uyscuti.social.network.api.response.posts.FileType
import com.uyscuti.social.network.api.response.posts.OriginalPost
import com.uyscuti.social.network.api.response.posts.Post
import com.uyscuti.social.network.api.response.posts.RepostedUser
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import kotlinx.coroutines.async
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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
    USER_POSTS,
    USER_PROFILE
}

private const val TAG = "SearchAllUserNameActivity"


@AndroidEntryPoint
class SearchAllUserNameActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchAllUserNameBinding
    private lateinit var searchAdapter: com.uyscuti.social.circuit.user_interface.SearchUserNameAdapter
    private var searchJob: Job? = null
    private lateinit var businessViewModel: BusinessPostsViewModel
    private var currentSearchContext = com.uyscuti.social.circuit.user_interface.SearchContext.GLOBAL
    private var currentFilter = com.uyscuti.social.circuit.user_interface.ContentFilter.ALL
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
    private val feedClickListener: OnFeedClickListener,
    private val viewModel: BusinessPostsViewModel,
    private val localStorage: LocalStorage,
    private val onUserClicked: (Author) -> Unit,
    private val onPostClicked: (Post) -> Unit
) : ListAdapter<Any, RecyclerView.ViewHolder>(SearchDiffCallback()) {

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
            data: Post
        )


        fun feedCommentClicked(
            position: Int,
            data: Post
        )

        fun feedFavoriteClick(
            position: Int,
            data: Post
        )


        fun moreOptionsClick(
            position: Int,
            data: Post
        )

        fun feedFileClicked(
            position: Int,
            data: Post
        )

        fun feedRepostFileClicked(
            position: Int, data: OriginalPost
        )

        fun feedShareClicked(
            position: Int, data: Post
        )


        fun followButtonClicked(
            followUnFollowEntity: FollowUnFollowEntity,
            followButton: AppCompatButton
        )

        fun feedRepostPost(
            position: Int,
            data: Post
        )

        fun feedRepostPostClicked(position: Int, data: Post)

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

    var currentFilter: ContentFilter = ContentFilter.ALL


    // Add these properties to handle following list
    private val followingUserIds = mutableSetOf<String>()

    fun updateFollowingList(followingIds: Set<String>) {
        followingUserIds.clear()
        followingUserIds.addAll(followingIds)
        notifyDataSetChanged()
    }


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
                if (currentFilter == ContentFilter.BUSINESS) {
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
                    R.layout.feed_mixed_files_original_post_adapter, parent, false
                )
                FeedPostViewHolder(itemView)
            }

            TYPE_REPOST_POST -> {
                val itemView = inflater.inflate(
                    R.layout.feed_mixed_files_original_post_with_repost_adapter, parent, false
                )
                FeedRepostViewHolder(itemView)
            }

            TYPE_REPOST_WITH_NEW_FILES -> {
                val itemView = inflater.inflate(
                    R.layout.feed_mixed_files_new_post_with_reposted_files_inside_adapter,
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
        @OptIn(UnstableApi::class)
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

            val usersList = ArrayList<com.uyscuti.social.circuit.data.model.User>()
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

        private fun UserEntity.toUser(): com.uyscuti.social.circuit.data.model.User {
            val username = if (name.contains("|")) name.split("|")[1].trim() else name
            return com.uyscuti.social.circuit.data.model.User(
                id,
                username,
                avatar,
                online,
                lastSeen
            )
        }

        private fun MessageEntity.toMessage(): Message {
            val username = if (user.name.contains("|")) user.name.split("|")[1].trim() else user.name
            val msgUser = com.uyscuti.social.circuit.data.model.User(
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

            // Name
            businessName.text = post.content?.trim()?.take(60) ?: "Business Item"

            // Location
            businessLocation.text = if (post.author.location.isNotBlank()) post.author.location else "Lilongwe, Malawi"

            // Just listed badge
            justListedBadge.visibility = if (isRecentlyListed(post.createdAt)) View.VISIBLE else View.GONE

            itemView.setOnClickListener { listener(post) }
        }

        private fun extractPrice(post: Post): String {
            post.businessDetails?.price?.let { return formatPrice(it) }
            val content = post.content ?: return "MWK0"
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



}




