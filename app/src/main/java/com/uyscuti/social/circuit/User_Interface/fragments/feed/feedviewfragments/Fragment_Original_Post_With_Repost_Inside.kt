package com.uyscuti.social.circuit.User_Interface.fragments.feed.feedviewfragments

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Color
import android.graphics.Outline
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.FragmentOriginalPostWithRepostInsideBinding
import com.uyscuti.social.network.api.response.posts.OriginalPost
import com.uyscuti.social.network.api.response.posts.Post
import java.text.SimpleDateFormat
import java.util.*
import androidx.activity.OnBackPressedCallback
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.feedRepost.PostItem
import com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.feedRepost.Tapped_Files_In_The_Container_View_Fragment
import com.uyscuti.social.network.api.response.posts.File
import kotlin.collections.isNotEmpty
import com.uyscuti.social.network.api.response.getfeedandresposts.Thumbnail
import com.google.android.material.card.MaterialCardView
import com.uyscuti.social.circuit.MainActivity
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.imageview.ShapeableImageView
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.stream.MalformedJsonException
import com.uyscuti.social.circuit.User_Interface.fragments.feed.feedviewfragments.editRepost.Fragment_Edit_Post_To_Repost
import com.uyscuti.social.circuit.utils.waveformseekbar.WaveformSeekBar
import com.uyscuti.social.network.api.response.allFeedRepostsPost.CommentsResponse
import com.uyscuti.social.network.api.response.allFeedRepostsPost.LikeRequest
import com.uyscuti.social.network.api.response.allFeedRepostsPost.LikeResponse
import com.uyscuti.social.network.api.response.allFeedRepostsPost.RetrofitClient
//import com.uyscuti.social.network.api.response.getrepostsPostsoriginal.FileType
import com.uyscuti.social.network.api.response.posts.FileType
import com.uyscuti.social.network.utils.LocalStorage
import com.uyscuti.social.network.api.response.allFeedRepostsPost.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.uyscuti.social.business.retro.model.User
import com.uyscuti.social.circuit.FollowingManager
import com.uyscuti.social.circuit.User_Interface.OtherUserProfile.OtherUserProfileAccount
import com.uyscuti.social.circuit.User_Interface.fragments.feed.feedviewfragments.Fragment_Original_Post_Without_Repost_Inside
import com.uyscuti.social.circuit.adapter.feed.FeedAdapter
import com.uyscuti.social.circuit.data.model.shortsmodels.OtherUsersProfile
import com.uyscuti.social.circuit.model.GoToUserProfileFragment
import com.uyscuti.social.core.common.data.room.entity.FollowUnFollowEntity
import com.uyscuti.social.network.api.response.comment.allcomments.Comment
import com.uyscuti.social.network.api.response.posts.Author
import com.uyscuti.social.network.api.response.posts.ThumbnailX
import org.greenrobot.eventbus.EventBus
import kotlin.math.abs
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


private const val TAG = "Fragment_Original_Post_With_Repost_Inside"


class Fragment_Original_Post_With_Repost_Inside : Fragment() {

    companion object {
        internal const val ARG_ORIGINAL_POST = "original_post"
        private const val TAG = "Fragment_Original_Post_With_Repost_Inside"

        fun newInstance(data: Post): Fragment_Original_Post_With_Repost_Inside {
            return Fragment_Original_Post_With_Repost_Inside().apply {
                arguments = Bundle().apply {
                    putString(ARG_ORIGINAL_POST, Gson().toJson(data))
                }
            }
        }
    }



    // Data
    private var originalPost: Post? = null
    private var post: Post? = null
    private var isFollowing = false
    private var isNavigating = false
    private var isNavigatingBack = false

    private var _binding: FragmentOriginalPostWithRepostInsideBinding? = null
    private val binding get() = _binding!!
    private var followingUserIds: Set<String> = emptySet()
    private lateinit var itemView: View
    private var onImageClickListener: ((Int, List<File>, List<String>) -> Unit)? = null

    private var currentPost: Post? = null
    private var currentPosition: Int = 0
    private var totalComments = 0
    private var likes: Int = 0
    private var isLiked: Boolean = false

    private var bookmarkCount: Int = 0
    private var isBookmarked: Boolean = false

    private var isReposted: Boolean = false
    private var isShared: Boolean = false

    private var isFollowed = false
    private var totalRepostComments = 0
    private var totalMixedLikesCounts = 0
    private var totalMixedBookMarkCounts = 0
    private var totalMixedShareCounts = 0
    private var totalMixedRePostCounts = 0

    // UI Elements - declare all the views used
    private lateinit var cancelButton: ImageButton
    private lateinit var headerTitle: TextView
    private lateinit var headerMenuButton: ImageButton
    private lateinit var userReposterProfileImage: ImageView
    private lateinit var reposterFullName: TextView
    private lateinit var tvUserHandle: TextView
    private lateinit var dateTimeCreate: TextView
    private lateinit var followButton: AppCompatButton
    private lateinit var repostContainer: LinearLayout
    private lateinit var tvPostTag: TextView
    private lateinit var userComment: TextView
    private lateinit var tvHashtags: TextView
    private lateinit var mixedFilesCardViews: CardView
    private lateinit var originalFeedImages: ImageView

    // private lateinit var multipleMediaContainer: LinearLayout
    private lateinit var multipleAudiosContainers: LinearLayout
    private lateinit var recyclerViews: RecyclerView
    private lateinit var quotedPostCard: CardView
    private lateinit var originalPostContainer: LinearLayout
    private lateinit var originalPosterProfileImage: ImageView
    private lateinit var originalPosterName: TextView
    private lateinit var tvQuotedUserHandle: TextView
    private lateinit var originalPostText: TextView
    private lateinit var tvQuotedHashtags: TextView
    private lateinit var dateTime: TextView
    private lateinit var mixedFilesCardView: CardView
    private lateinit var originalFeedImage: ImageView
    private lateinit var videoContainer: FrameLayout
    private lateinit var multipleAudiosContainer: LinearLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var ivQuotedPostImage: ImageView

    // Action buttons
    private lateinit var likeLayout: LinearLayout
    private lateinit var likeButtonIcon: ImageView
    private lateinit var likesCount: TextView
    private lateinit var commentLayout: LinearLayout
    private lateinit var commentButtonIcon: ImageView
    private lateinit var favoriteLayout: LinearLayout
    private lateinit var favoritesButton: ImageView
    private lateinit var repostLayout: LinearLayout
    private lateinit var repostPost: ImageView
    private lateinit var repostCount: TextView
    private lateinit var shareLayout: LinearLayout
    private lateinit var shareButtonIcon: ImageView
    private lateinit var multipleMediaContainer: FrameLayout

    private lateinit var likeCount: TextView
    private lateinit var bookmarkCountView: TextView
    private lateinit var commentCount: TextView
    private lateinit var favoriteCounts: TextView
    private lateinit var shareCount: TextView


    private fun isPostLiked() = false
    private fun isPostBookmarked() = originalPost?.bookmarkCount?: false

    private val navigationHandler = Handler(Looper.getMainLooper())
    private var isNavigationInProgress = false



    // Data classes that need to be used
    data class FeedCommentClicked(
        val position: Int,
        val post: Post
    )

    data class PostUpdatedEvent(
        val postId: String,
        val updatedPost: Post? = null
    )

    data class CommentsLoadedEvent(
        val postId: String,
        val commentCount: Int,
        val comments: List<Comment>
    )

    data class CommentCountUpdatedEvent(
        val postId: String,
        val commentCount: Int
    )

    data class CommentPostedEvent(
        val postId: String,
        val comment: Comment
    )

    data class CommentDeletedEvent(
        val postId: String,
        val commentId: String
    )

    private fun processLoadedComments(comments: List<Comment>) {
        Log.d(TAG, "processLoadedComments: Processing ${comments.size} comments")

    }


    private val OriginalPost.safeRepostCount: Int
        get() = repostCount ?: 0

    private val OriginalPost.safeLikes: Int
        get() = likes ?: 0

    private val OriginalPost.safeCommentCount: Int
        get() = commentCount ?: 0

    private val OriginalPost.safeBookmarkCount: Int
        get() = bookmarkCount ?: 0

    private val OriginalPost.safeShareCount: Int
        get() = shareCount ?: 0

    private val mainActivity: MainActivity?
        @OptIn(UnstableApi::class)
        get() = activity as? MainActivity


    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
            Log.d(TAG, "EventBus registered successfully")
        }
    }

    override fun onStop() {
        super.onStop()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
            Log.d(TAG, "EventBus unregistered successfully")
        }
    }



    private val feedClickListener: com.uyscuti.social.circuit.adapter.feed.OnFeedClickListener by lazy {
        (activity as? com.uyscuti.social.circuit.adapter.feed.OnFeedClickListener) ?:
        object : com.uyscuti.social.circuit.adapter.feed.OnFeedClickListener {

            override fun likeUnLikeFeed(position: Int, post: Post) {
                Log.d(TAG, "feedClickListener: likeUnLikeFeed position $position for post ${post._id}")
            }

            override fun feedCommentClicked(
                position: Int,
                data: Post
            ) {
                Log.d(TAG, "feedClickListener: feedCommentClicked position $position for post ${post?._id}")
                handleFeedCommentClicked(position, post!!)
            }

            override fun feedFavoriteClick(position: Int, post: Post) {
                Log.d(TAG, "feedClickListener: feedFavoriteClick position $position for post ${post._id}")
            }

            override fun moreOptionsClick(
                position: Int,
                data: Post
            ) {
                Log.d(TAG, "feedClickListener: moreOptionsClick position $position for post ${data._id}")
            }

            override fun feedFileClicked(
                position: Int,
                data: Post
            ) {
                Log.d(TAG, "feedClickListener: feedFileClicked position $position for post ${data._id}")
            }

            override fun feedRepostFileClicked(position: Int, data: OriginalPost) {
                Log.d(TAG, "feedClickListener: feedRepostFileClicked position $position")
            }

            override fun feedShareClicked(
                position: Int,
                data: Post
            ) {
                Log.d(TAG, "feedClickListener: feedShareClicked position $position for post ${post?._id}")
            }

            override fun followButtonClicked(
                followUnFollowEntity: FollowUnFollowEntity,
                followButton: AppCompatButton
            ) {
                Log.d(TAG, "feedClickListener: followButtonClicked")
            }

            override fun feedRepostPost(position: Int, post: Post) {
                Log.d(TAG, "feedClickListener: feedRepostPost position $position for post ${post._id}")
            }

            override fun feedRepostPostClicked(
                position: Int,
                data: Post
            ) {
                Log.d(TAG, "feedClickListener: feedRepostPostClicked position $position for post ${data._id}")
            }

            override fun feedClickedToOriginalPost(position: Int, originalPostId: String) {
                Log.d(TAG, "feedClickListener: feedClickedToOriginalPost position $position, originalPostId $originalPostId")
            }

            override fun onImageClick() {
                Log.d(TAG, "feedClickListener: onImageClick")
            }
        }
    }


    private var onMultipleFilesClickListener:
            com.uyscuti.social.circuit.adapter.feed.multiple_files.OnMultipleFilesClickListener? = null


    fun multipleFileClickListener(currentIndex: Int, files: List<File>, fileIds: List<String>) {
        onMultipleFilesClickListener?.multipleFileClickListener(currentIndex, files, fileIds)
    }

    interface OnMultipleFilesClickListener {
        fun multipleFileClickListener(currentIndex: Int, files: List<File>, fileIds: List<String>)
    }


    interface OnFeedClickListener {

        fun likeUnLikeFeed(position: Int, data: Post)

        fun feedCommentClicked(position: Int, data: Post)

        fun feedFavoriteClick(position: Int, data: Post)

        fun moreOptionsClick(position: Int, data: Post)

        fun feedFileClicked(position: Int, data: Post)

        fun feedRepostFileClicked(position: Int, data: OriginalPost)

        fun feedShareClicked(position: Int, data: Post)

        fun followButtonClicked(
            followUnFollowEntity: FollowUnFollowEntity,
            followButton: AppCompatButton
        )

        fun feedRepostPost(position: Int, data: Post)

        fun feedRepostPostClicked(position: Int, data: Post)

        fun feedClickedToOriginalPost(position: Int, originalPostId: String)

        fun onImageClick()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOriginalPostWithRepostInsideBinding.inflate(inflater, container, false)
        return binding.root
    }



    @SuppressLint("UnsafeOptInUsageError")
    @OptIn(UnstableApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            // Initialize views first
            initializeViews(view)

            // Verify views are initialized before proceeding
            if (!isViewsInitialized()) {
                Log.e(TAG, "Critical views not initialized properly")
                return
            }

            // Get post data from arguments - FIXED: Use JSON string instead of Serializable
            val postJson = arguments?.getString(ARG_ORIGINAL_POST)
            post = postJson?.let {
                try {
                    Gson().fromJson(it, Post::class.java)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing post JSON: ${e.message}", e)
                    null
                }
            }
            Log.d(TAG, "post: $post")

            // Handle null post case
            if (post == null) {
                Log.e(TAG, "Post data is null in onViewCreated")
                context?.let { Toast.makeText(it, "Unable to load post", Toast.LENGTH_SHORT).show() }
                return
            }

            // Setup other components with valid post
            post?.let { safePost ->
                setupClickListeners(safePost)
                setupRecyclerViews()
                setupBackNavigation()

                // Hide UI elements
                (activity as? MainActivity)?.hideAppBar()
                (activity as? MainActivity)?.hideBottomNavigation()

                // Populate data
                Log.d(TAG, "Post type: ${safePost::class.java.simpleName}")
                Log.d(TAG, "Post ID: ${safePost._id}")

                // Use repost's own comment count (prefer comments if commentCount is null)
                totalRepostComments = safePost.comments ?: safePost.comments ?: 0
                updateMetricDisplay(commentCount, totalRepostComments, "comment")
                Log.d(TAG, "onViewCreated: Set repost comment count to $totalRepostComments")

                // Fetch fresh count for the repost container
                fetchAndUpdateCommentCount(safePost._id)
                Log.d(TAG, "onViewCreated: Fetching comments for repost ID: ${safePost._id}")

                // Fetch fresh count for the original post (if it exists)
                val originalPostId = safePost.originalPost?.firstOrNull()?._id
                if (originalPostId != null) {
                    fetchAndUpdateCommentCount(originalPostId)
                    Log.d(TAG, "onViewCreated: Fetching comments for ORIGINAL POST ID: $originalPostId")
                }

                setupLikeButton(safePost)
                setupBookmarkButton(safePost)
                setupRepostButton(safePost)
                setupShareButton(safePost)
                setupCommentButton(safePost)

                populatePostData(safePost)
                Log.d(TAG, "Post data populated successfully")
            }

            // Handle originalPost separately if needed
            originalPost?.let {
                populateViews(it)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in onViewCreated: ${e.message}", e)
            context?.let { Toast.makeText(it, "Error loading post", Toast.LENGTH_SHORT).show() }
        }
    }

    @OptIn(UnstableApi::class)
    private fun setupClickListeners(post: Post) {

        setupInitialFollowButtonState(post)

        cancelButton.setOnClickListener { button ->
            Log.d(TAG, "Cancel button clicked - immediate navigation")
            button.isEnabled = false
            immediateNavigateBack()
            Handler(Looper.getMainLooper()).postDelayed({
                if (isAdded) button.isEnabled = true
            }, 1000)
        }

        headerMenuButton.setOnClickListener { view ->
            showOptionsMenu(view)
        }

        followButton.setOnClickListener {
            handleFollowButtonClick()
        }

        repostContainer.setOnClickListener {
            handleMainPostClick()
        }

        mixedFilesCardViews.setOnClickListener {
            handleRepostMediaClick()
        }

        originalFeedImages.setOnClickListener {
            handleRepostFileClick()
        }

        originalFeedImage.setOnClickListener {
            handleOriginalFileClick()
        }

        // FIXED: Reposter Profile Image Click
        userReposterProfileImage.setOnClickListener { view ->
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

            try {
                // Check if we have repostedUser data first (for actual reposts)
                val repostedUser = post.repostedUser

                if (repostedUser != null) {
                    // Use reposted user data
                    val feedOwnerId = repostedUser._id
                    val feedOwnerUsername = repostedUser.username
                    val profilePicUrl = repostedUser.avatar?.url ?: ""

                    Log.d(TAG, "Navigating to reposter profile: $feedOwnerUsername (ID: $feedOwnerId)")

                    navigateToUserProfile(
                        feedOwnerId = feedOwnerId,
                        feedOwnerName = feedOwnerUsername,
                        feedOwnerUsername = feedOwnerUsername,
                        profilePicUrl = profilePicUrl
                    )
                } else {
                    // Fall back to main author if no reposted user
                    val author = post.author
                    if (author != null) {
                        val feedOwnerId = author._id
                        val feedOwnerUsername = author.account.username

                        // Build display name
                        val feedOwnerName = buildDisplayName(author)

                        // Get avatar URL with proper type handling
                        val profilePicUrl = when (val avatar = author.account.avatar) {
                            is Avatar -> avatar.url
                            is String -> avatar
                            else -> ""
                        }

                        Log.d(TAG, "Navigating to main author profile: $feedOwnerName (ID: $feedOwnerId)")

                        navigateToUserProfile(
                            feedOwnerId = feedOwnerId,
                            feedOwnerName = feedOwnerName,
                            feedOwnerUsername = feedOwnerUsername,
                            profilePicUrl = profilePicUrl
                        )
                    } else {
                        Log.e(TAG, "Error: Both repostedUser and author are null")
                        Toast.makeText(requireContext(), "Unable to load profile", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling reposter profile click", e)
                Toast.makeText(requireContext(), "Unable to load profile", Toast.LENGTH_SHORT).show()
            }
        }

        // FIXED: Original Poster Profile Image Click
        originalPosterProfileImage.setOnClickListener { view ->
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

            try {
                // Get the original post
                val originalPost = post.originalPost?.firstOrNull()

                if (originalPost != null) {
                    // Use original post author (AuthorX type)
                    val author = originalPost.author

                    val feedOwnerId = author._id
                    val feedOwnerUsername = author.account.username

                    // Build display name for AuthorX
                    val feedOwnerName = when {
                        author.firstName.isNotBlank() && author.lastName.isNotBlank() ->
                            "${author.firstName} ${author.lastName}"
                        author.firstName.isNotBlank() -> author.firstName
                        author.lastName.isNotBlank() -> author.lastName
                        author.account.username.isNotBlank() -> author.account.username
                        else -> "Unknown User"
                    }

                    // Get avatar URL
                    val profilePicUrl = author.account.avatar.url

                    Log.d(TAG, "Navigating to original poster profile: $feedOwnerName (ID: $feedOwnerId)")

                    navigateToUserProfile(
                        feedOwnerId = feedOwnerId,
                        feedOwnerName = feedOwnerName,
                        feedOwnerUsername = feedOwnerUsername,
                        profilePicUrl = profilePicUrl
                    )
                } else {
                    // If no original post, use main author
                    val author = post.author
                    if (author != null) {
                        val feedOwnerId = author._id
                        val feedOwnerUsername = author.account.username
                        val feedOwnerName = buildDisplayName(author)

                        val profilePicUrl = when (val avatar = author.account.avatar) {
                            is Avatar -> avatar.url
                            is String -> avatar
                            else -> ""
                        }

                        Log.d(TAG, "No original post, navigating to main author: $feedOwnerName (ID: $feedOwnerId)")

                        navigateToUserProfile(
                            feedOwnerId = feedOwnerId,
                            feedOwnerName = feedOwnerName,
                            feedOwnerUsername = feedOwnerUsername,
                            profilePicUrl = profilePicUrl
                        )
                    } else {
                        Log.e(TAG, "Error: Author data is null for original poster")
                        Toast.makeText(requireContext(), "Unable to load profile", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling original poster profile click", e)
                Toast.makeText(requireContext(), "Unable to load profile", Toast.LENGTH_SHORT).show()
            }
        }

        // Quoted Post Card Click
        quotedPostCard.setOnClickListener { view ->
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

            Log.d(TAG, "Quoted Post / Original Post Card clicked! Post ID: ${post._id}")

            try {
                navigateToFragment_Original_Post_Without_Repost_Inside(post)
            } catch (e: Exception) {
                Log.e(TAG, "Error navigating to original post fragment", e)
                Toast.makeText(requireContext(), "Unable to load post", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun populateReposterInfo(post: Post) {
        try {
            var profilePicUrl: String? = null
            var feedOwnerUsername = ""
            var feedOwnerDisplayName = "" // For full name display
            var userHandle = ""
            var feedOwnerId = "" // For follow check

            if (post.repostedUser != null) {
                // ✅ Use OWNER field (account ID), not _id (profile ID)
                feedOwnerId = post.repostedUser.owner
                profilePicUrl = post.repostedUser.avatar?.url

                // ✅ Build full name with proper cascade
                feedOwnerDisplayName = when {
                    post.repostedUser.firstName.isNotBlank() && post.repostedUser.lastName.isNotBlank() ->
                        "${post.repostedUser.firstName} ${post.repostedUser.lastName}"
                    post.repostedUser.firstName.isNotBlank() -> post.repostedUser.firstName
                    post.repostedUser.lastName.isNotBlank() -> post.repostedUser.lastName
                    else -> post.repostedUser.username
                }

                feedOwnerUsername = post.repostedUser.username
                userHandle = "@${post.repostedUser.username}"

                Log.d(TAG, "📍 Reposted by: $feedOwnerDisplayName (Account/Owner: $feedOwnerId, Username: @$feedOwnerUsername)")

            } else if (post.originalPost != null && post.originalPost.isNotEmpty()) {
                // ✅ Use author.owner (the account ID)
                val originalAuthor = post.originalPost[0].author
                feedOwnerId = originalAuthor.owner // Account ID!
                profilePicUrl = originalAuthor.account.avatar.url

                // ✅ Build full name for AuthorX type
                feedOwnerDisplayName = when {
                    originalAuthor.firstName.isNotBlank() && originalAuthor.lastName.isNotBlank() ->
                        "${originalAuthor.firstName} ${originalAuthor.lastName}"
                    originalAuthor.firstName.isNotBlank() -> originalAuthor.firstName
                    originalAuthor.lastName.isNotBlank() -> originalAuthor.lastName
                    else -> originalAuthor.account.username
                }

                feedOwnerUsername = originalAuthor.account.username
                userHandle = "@${originalAuthor.account.username}"

                Log.d(TAG, "📍 Original author: $feedOwnerDisplayName (Account/Owner ID: $feedOwnerId, Username: @$feedOwnerUsername)")

            } else {
                // Fall back to main post author
                val author = post.author
                feedOwnerId = author.account._id // Account ID
                profilePicUrl = author.account.avatar?.url

                // ✅ Build full name for Author type
                feedOwnerDisplayName = when {
                    author.firstName.isNotBlank() && author.lastName.isNotBlank() ->
                        "${author.firstName} ${author.lastName}"
                    author.firstName.isNotBlank() -> author.firstName
                    author.lastName.isNotBlank() -> author.lastName
                    author.account.username.isNotBlank() -> author.account.username
                    else -> "Unknown User"
                }

                feedOwnerUsername = author.account.username
                userHandle = if (author.account.username.isNotBlank()) {
                    "@${author.account.username}"
                } else {
                    "@unknown"
                }

                Log.d(TAG, "📍 Main author: $feedOwnerDisplayName (Account: $feedOwnerId, Username: @$feedOwnerUsername)")
            }

            // ✅ Set UI elements with FULL NAME (not username)
            if (::reposterFullName.isInitialized) {
                reposterFullName.text = feedOwnerDisplayName // Show full name!
            }
            if (::tvUserHandle.isInitialized) {
                tvUserHandle.text = userHandle
            }
            if (::userReposterProfileImage.isInitialized) {
                loadProfileImage(profilePicUrl, userReposterProfileImage)
            }

            // ✅ Update follow button visibility
            updateFollowButtonVisibility(feedOwnerId, feedOwnerUsername)

            Log.d(TAG, "Handling media from reposter info with ${post.files?.size ?: 0} files")
            handlePostMedia(post)

        } catch (e: Exception) {
            Log.e(TAG, "Error populating reposter info: ${e.message}", e)
            if (::reposterFullName.isInitialized) reposterFullName.text = "Unknown User"
            if (::tvUserHandle.isInitialized) tvUserHandle.text = "@unknown"
            if (::userReposterProfileImage.isInitialized) {
                loadProfileImage(null, userReposterProfileImage)
            }
            // Hide follow button on error
            if (::followButton.isInitialized) {
                followButton.visibility = View.GONE
            }
        }
    }


    private fun updateFollowButtonVisibility(accountId: String, username: String) {
        if (!::followButton.isInitialized) return

        try {
            val currentUserId = LocalStorage.getInstance(requireContext()).getUserId()

            // Get cached following lists
            val cachedFollowingList = FeedAdapter.getCachedFollowingList()
            val cachedFollowingUsernames = FeedAdapter.getCachedFollowingUsernames()

            // Check by BOTH ID and USERNAME
            val isUserFollowing = cachedFollowingList.contains(accountId) ||
                    cachedFollowingUsernames.contains(username)

            Log.d(TAG, "═══════════════════════════════════════")
            Log.d(TAG, "FOLLOW BUTTON VISIBILITY CHECK")
            Log.d(TAG, "Account ID: $accountId")
            Log.d(TAG, "Username: @$username")
            Log.d(TAG, "Current user ID: $currentUserId")
            Log.d(TAG, "Is following (by ID): ${cachedFollowingList.contains(accountId)}")
            Log.d(TAG, "Is following (by username): ${cachedFollowingUsernames.contains(username)}")
            Log.d(TAG, "═══════════════════════════════════════")

            // Hide button if it's own post OR already following
            val shouldHideButton = accountId == currentUserId || isUserFollowing

            if (shouldHideButton) {
                followButton.visibility = View.GONE
                Log.d(TAG, "✓✓✓ HIDING follow button - Reason: ${when {
                    accountId == currentUserId -> "Own post"
                    cachedFollowingUsernames.contains(username) -> "Already following (by username: @$username)"
                    cachedFollowingList.contains(accountId) -> "Already following (by ID: $accountId)"
                    else -> "Unknown"
                }}")
            } else {
                followButton.visibility = View.VISIBLE
                followButton.text = "Follow"
                Log.d(TAG, "✓✓✓ SHOWING follow button for account: $accountId (@$username)")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error updating follow button visibility", e)
            followButton.visibility = View.GONE
        }
    }


    private fun showOptionsMenu(anchor: View) {
        val context = requireContext()
        val popup = PopupMenu(context, anchor, Gravity.END)

        // Apply custom style and force show icons (optional)
        try {
            val fieldPopup = PopupMenu::class.java.getDeclaredField("mPopup")
            fieldPopup.isAccessible = true
            val menuPopupWindow = fieldPopup.get(popup)
            // Note: MenuPopupWindow.setForceShowIcon() might not be available in all versions
            // You may need to handle this differently based on your target SDK
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Inflate the menu
        popup.menuInflater.inflate(R.menu.post_options_menu, popup.menu)

        // Set menu item click listener
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_report -> {
                    handleReportPost()
                    true
                }
                R.id.menu_block_user -> {
                    handleBlockUser()
                    true
                }
                R.id.menu_mute_user -> {
                    handleMuteUser()
                    true
                }
                R.id.menu_copy_link -> {
                    handleCopyLink()
                    true
                }
                R.id.menu_save_post -> {
                    handleSavePost()
                    true
                }
                R.id.menu_not_interested -> {
                    handleNotInterested()
                    true
                }
                else -> false
            }
        }

        // Show the popup menu
        popup.show()
    }

    private fun handleReportPost() {
        // Show report dialog or navigate to report screen
        Toast.makeText(requireContext(), "Report post", Toast.LENGTH_SHORT).show()
    }

    private fun handleBlockUser() {
        // Show confirmation dialog and block user
        Toast.makeText(requireContext(), "User blocked", Toast.LENGTH_SHORT).show()
    }

    private fun handleMuteUser() {
        // Show confirmation dialog and mute user
        Toast.makeText(requireContext(), "User muted", Toast.LENGTH_SHORT).show()
    }

    private fun handleCopyLink() {
        // Copy post link to clipboard
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Post Link", "https://example.com/post/123")
        clipboard.setPrimaryClip(clip)
        Toast.makeText(requireContext(), "Link copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun handleSavePost() {
        // Save/unsave post
        Toast.makeText(requireContext(), "Post saved", Toast.LENGTH_SHORT).show()
    }

    private fun handleNotInterested() {
        // Mark as not interested
        Toast.makeText(requireContext(), "We'll show you fewer posts like this", Toast.LENGTH_SHORT).show()
    }


    private fun handleFollowButtonClick() {
        post?.let { currentPost ->
            //  Extract ACCOUNT ID and USERNAME
            val feedOwnerId: String
            val feedOwnerUsername: String

            when {
                // Case 1: Reposted post - use original author's account ID
                currentPost.originalPost.isNotEmpty() -> {
                    val originalAuthor = currentPost.originalPost[0].author
                    feedOwnerId = originalAuthor.owner  //  Use owner field (account ID)
                    feedOwnerUsername = originalAuthor.account.username
                    Log.d(TAG, "Follow button - Original author ID: $feedOwnerId (@$feedOwnerUsername)")
                }
                // Case 2: Regular post - use main author's account ID
                else -> {
                    feedOwnerId = currentPost.author?.account?._id ?: ""
                    feedOwnerUsername = currentPost.author?.account?.username ?: "unknown"
                    Log.d(TAG, "Follow button - Main author ID: $feedOwnerId (@$feedOwnerUsername)")
                }
            }

            val currentUserId = LocalStorage.getInstance(requireContext()).getUserId()

            // Check following status by BOTH ID and USERNAME
            val cachedFollowingList = FeedAdapter.getCachedFollowingList()
            val cachedFollowingUsernames = FeedAdapter.getCachedFollowingUsernames()

            val isAlreadyFollowing = followingUserIds.contains(feedOwnerId) ||
                    cachedFollowingList.contains(feedOwnerId) ||
                    cachedFollowingUsernames.contains(feedOwnerUsername)

            // Hide button if it's own post OR already following
            if (feedOwnerId == currentUserId || isAlreadyFollowing) {
                followButton.visibility = View.GONE
                Log.d(TAG, "Follow button hidden - Own post or already following")
                return
            }

            // Toggle follow status
            isFollowing = !isFollowing

            if (isFollowing) {
                // Hide button immediately
                followButton.visibility = View.GONE

                // Add to following lists
                // Note: You'll need to get the adapter instance or use FollowingManager
                FollowingManager(requireContext()).addToFollowing(feedOwnerId)

                // Build display name for toast
                val displayName = when {
                    currentPost.originalPost.isNotEmpty() -> {
                        val author = currentPost.originalPost[0].author
                        when {
                            author.firstName.isNotBlank() && author.lastName.isNotBlank() ->
                                "${author.firstName} ${author.lastName}"
                            author.firstName.isNotBlank() -> author.firstName
                            author.lastName.isNotBlank() -> author.lastName
                            else -> author.account.username
                        }
                    }
                    else -> {
                        val author = currentPost.author
                        when {
                            author.firstName.isNotBlank() && author.lastName.isNotBlank() ->
                                "${author.firstName} ${author.lastName}"
                            author.firstName.isNotBlank() -> author.firstName
                            author.lastName.isNotBlank() -> author.lastName
                            else -> author.account.username
                        }
                    }
                }

                showToast("Now following $displayName")
                Log.d(TAG, "✓ Added account $feedOwnerId (@$feedOwnerUsername) to following list")
            } else {
                // Show button
                followButton.visibility = View.VISIBLE
                followButton.text = "Follow"

                // Remove from following lists
                FollowingManager(requireContext()).removeFromFollowing(feedOwnerId)

                showToast("Unfollowed")
                Log.d(TAG, "✓ Removed account $feedOwnerId (@$feedOwnerUsername) from following list")
            }

            updateFollowButtonUI()
        }
    }

    private fun setupInitialFollowButtonState(data: Post) {
        val feedOwnerId: String
        val feedOwnerUsername: String

        when {
            data.originalPost.isNotEmpty() -> {
                val originalAuthor = data.originalPost[0].author
                feedOwnerId = originalAuthor.owner
                feedOwnerUsername = originalAuthor.account.username
            }
            else -> {
                feedOwnerId = data.author?.account?._id ?: ""
                feedOwnerUsername = data.author?.account?.username ?: "unknown"
            }
        }

        val currentUserId = LocalStorage.getInstance(requireContext()).getUserId()
        val cachedFollowingList = FeedAdapter.getCachedFollowingList()
        val cachedFollowingUsernames = FeedAdapter.getCachedFollowingUsernames()

        val isAlreadyFollowing = followingUserIds.contains(feedOwnerId) ||
                cachedFollowingList.contains(feedOwnerId) ||
                cachedFollowingUsernames.contains(feedOwnerUsername)

        if (feedOwnerId == currentUserId || isAlreadyFollowing) {
            followButton.visibility = View.GONE
            Log.d(TAG, "Initial setup: Follow button hidden for $feedOwnerId (@$feedOwnerUsername)")
        } else {
            followButton.visibility = View.VISIBLE
            followButton.text = "Follow"
            Log.d(TAG, "Initial setup: Follow button shown for $feedOwnerId (@$feedOwnerUsername)")
        }
    }


    private fun handleMainPostClick() = showToast("Opening full post ...")

    private fun buildDisplayName(author: Author): String {
        return when {
            author.firstName.isNotBlank() && author.lastName.isNotBlank() ->
                "${author.firstName} ${author.lastName}"
            author.firstName.isNotBlank() -> author.firstName
            author.lastName.isNotBlank() -> author.lastName
            author.account.username.isNotBlank() -> author.account.username
            else -> "Unknown User"
        }
    }

    @OptIn(UnstableApi::class)
    private fun navigateToUserProfile(

        feedOwnerId: String,
        feedOwnerName: String,
        feedOwnerUsername: String,
        profilePicUrl: String

    ) {
        try {
            if (isAdded && !isDetached) {
                val context = requireContext()

                if (feedOwnerId == LocalStorage.getInstance(context).getUserId()) {
                    EventBus.getDefault().post(GoToUserProfileFragment())
                    Log.d(TAG, "Navigating to current user's profile: $feedOwnerId")
                } else {
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

                    OtherUserProfileAccount.open(
                        context = context,
                        user = otherUsersProfile,
                        dialogPhoto = profilePicUrl,
                        dialogId = feedOwnerId
                    )

                    Log.d(TAG, "Navigating to other user's profile: $feedOwnerName (ID: $feedOwnerId)")
                }
            } else {
                Log.e(TAG, "Fragment not attached, cannot navigate to user profile: $feedOwnerId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to user profile: $feedOwnerId", e)
            Toast.makeText(context, "Unable to open profile", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isViewsInitialized(): Boolean {
        return _binding != null &&
                ::itemView.isInitialized &&
                ::headerTitle.isInitialized &&
                ::userReposterProfileImage.isInitialized &&
                ::reposterFullName.isInitialized
    }

    private fun initializeViews(view: View) {
        itemView = view
        try {
            _binding?.let { safeBinding ->
                // Header Section Views
                cancelButton = safeBinding.cancelButton
                headerTitle = safeBinding.headerTitle
                headerMenuButton = safeBinding.headerMenuButton

                // Repost User Section Views
                userReposterProfileImage = safeBinding.userReposterProfileImage
                reposterFullName = safeBinding.reposterFullName
                tvUserHandle = safeBinding.tvUserHandle
                dateTimeCreate = safeBinding.dateTimeCreate
                followButton = safeBinding.followButton

                // Repost Content Views
                repostContainer = safeBinding.repostContainer
                tvPostTag = safeBinding.tvPostTag
                userComment = safeBinding.userComment
                tvHashtags = safeBinding.tvHashtags

                // Repost Media Views
                mixedFilesCardViews = safeBinding.mixedFilesCardViews
                originalFeedImages = safeBinding.originalFeedImages
                multipleMediaContainer = safeBinding.multipleMediaContainer
                multipleAudiosContainers = safeBinding.multipleAudiosContainers
                recyclerViews = safeBinding.recyclerViews

                // Quoted Post Section Views
                quotedPostCard = safeBinding.quotedPostCard
                originalPostContainer = safeBinding.originalPostContainer
                originalPosterProfileImage = safeBinding.originalPosterProfileImage
                originalPosterName = safeBinding.originalPosterName
                tvQuotedUserHandle = safeBinding.tvQuotedUserHandle
                dateTime = safeBinding.dateTime
                originalPostText = safeBinding.originalPostText
                tvQuotedHashtags = safeBinding.tvQuotedHashtags

                // Quoted Post Media Views
                mixedFilesCardView = safeBinding.mixedFilesCardView
                originalFeedImage = safeBinding.originalFeedImage
                videoContainer = safeBinding.videoContainer
                multipleAudiosContainer = safeBinding.multipleAudiosContainer
                recyclerView = safeBinding.recyclerView
                ivQuotedPostImage = safeBinding.ivQuotedPostImage

                // Action Buttons and Counters
                likeLayout = safeBinding.likeLayout
                likeButtonIcon = safeBinding.likeButtonIcon
                likesCount = safeBinding.likesCount
                likeCount = safeBinding.likesCount
                commentLayout = safeBinding.commentLayout
                commentButtonIcon = safeBinding.commentButtonIcon
                commentCount = safeBinding.commentCount
                favoriteLayout = safeBinding.favoriteSection
                favoritesButton = safeBinding.favoritesButton
                favoriteCounts = safeBinding.favoriteCounts
                repostLayout = safeBinding.repostLayout
                repostPost = safeBinding.repostPost
                repostCount = safeBinding.repostCount
                shareLayout = safeBinding.shareLayout
                shareButtonIcon = safeBinding.shareButtonIcon
                shareCount = safeBinding.shareCount

                Log.d(TAG, "All views initialized successfully")
            } ?: run {
                Log.e(TAG, "Binding is null, cannot initialize views")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views: ${e.message}", e)
        }
    }

    override fun onResume() {
        super.onResume()

    }

    private fun setupInitialMetrics(post: Post) {
        Log.d(TAG, "setupInitialMetrics: Setting up metrics for post ${post._id}")

        // For likes, bookmarks, shares - use the repost container's metrics
        updateMetricDisplay(likesCount, post.likes, "like")
        updateMetricDisplay(favoriteCounts, post.bookmarkCount, "bookmark")
        updateMetricDisplay(shareCount, post.shareCount, "share")

        // For comments - use the repost's own comment count
        totalRepostComments = post.comments ?: post.comments ?: 0
        updateMetricDisplay(commentCount, totalRepostComments, "comment")
        Log.d(TAG, "setupInitialMetrics: Set repost comment count to $totalRepostComments")

        // Fetch fresh count for the repost
        fetchAndUpdateCommentCount(post._id)

        // Fetch fresh count for the original post (if needed for quoted post UI)
        val originalPostId = post.originalPost?.firstOrNull()?._id
        if (originalPostId != null) {
            fetchAndUpdateCommentCount(originalPostId)
        }
    }

    @SuppressLint("DefaultLocale")
    private fun updateMetricDisplay(textView: TextView, count: Int, type: String) {
        Log.d(TAG, "updateMetricDisplay: Updating $type with count: $count")

        if (Looper.myLooper() == Looper.getMainLooper()) {
            textView.text = when {
                count == 0 -> "0"
                count < 1000 -> count.toString()
                count < 1000000 -> String.format("%.1fK", count / 1000.0)
                else -> String.format("%.1fM", count / 1000000.0)
            }
            Log.d(TAG, "updateMetricDisplay: Set $type text to: ${textView.text}")
        } else {
            textView.post {
                textView.text = when {
                    count == 0 -> "0"
                    count < 1000 -> count.toString()
                    count < 1000000 -> String.format("%.1fK", count / 1000.0)
                    else -> String.format("%.1fM", count / 1000000.0)
                }
                Log.d(TAG, "updateMetricDisplay: Set $type text to: ${textView.text} (via post)")
            }
        }
    }

    private fun populateViews(originalPost: Post) {
        Log.d(TAG, "populateViews: Populating Original Post ${originalPost._id}")

        // Populate original post's UI elements
        originalPosterName.text = originalPost.author.account.username ?: "Unknown"
        tvQuotedUserHandle.text = "@${originalPost.author.account.username ?: "unknown"}"
       // dateTime.text = originalPost.createdAt?.let { formatDate(it) } ?: ""
        originalPostText.text = originalPost.content ?: ""

        // Set original post's comment count
        val originalCommentCount = originalPost.comments ?: originalPost.comments ?: 0
        updateMetricDisplay(commentCount, originalCommentCount, "original_comment")
        Log.d(TAG, "populateViews: Set Original Post Comment Count to $originalCommentCount")
    }

    private fun setupCommentButton(data: Post) {
        val originalPost = data.originalPost?.firstOrNull()
        val originalPostId = originalPost?._id

        if (originalPost == null || originalPostId == null) {
            Log.e(TAG, "No original post found, disabling comment functionality")
            commentButtonIcon.isEnabled = false
            commentCount.isEnabled = false
            return
        }

        commentButtonIcon.setOnClickListener {
            if (!commentButtonIcon.isEnabled) return@setOnClickListener

            Log.d(TAG, "Comment button clicked for ORIGINAL POST ${originalPostId}")

            YoYo.with(Techniques.Tada)
                .duration(700)
                .repeat(1)
                .playOn(commentButtonIcon)

            // Pass the ORIGINAL POST to comment handler
            handleFeedCommentClicked(0, data)

            commentButtonIcon.isEnabled = true
        }

        commentCount.setOnClickListener {
            if (!commentCount.isEnabled) return@setOnClickListener

            YoYo.with(Techniques.Tada)
                .duration(700)
                .repeat(1)
                .playOn(commentCount)

            handleFeedCommentClicked(0, data)
            commentCount.isEnabled = true
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCommentsLoaded(event: CommentsLoadedEvent) {
        Log.d(TAG, "onCommentsLoaded: Received event for post ${event.postId} with ${event.commentCount} comments")

        val repostId = post?._id
        val originalPostId = post?.originalPost?.firstOrNull()?._id

        when (event.postId) {
            repostId -> {
                Log.d(TAG, "onCommentsLoaded: Event matches repost, updating count to ${event.commentCount}")
                totalRepostComments = event.commentCount
                updateMetricDisplay(commentCount, totalRepostComments, "comment")
                post?.comments = event.commentCount
                processLoadedComments(event.comments)
            }
            originalPostId -> {
                Log.d(TAG, "onCommentsLoaded: Event matches original post, updating original count to ${event.commentCount}")
                post?.originalPost?.firstOrNull()?.commentCount = event.commentCount
                // Update original post UI if needed
                updateMetricDisplay(commentCount, event.commentCount, "original_comment")
            }
            else -> {
                Log.d(TAG, "onCommentsLoaded: Event ignored (not for repost or original post)")
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCommentCountUpdated(event: CommentCountUpdatedEvent) {
        Log.d(TAG, "onCommentCountUpdated: Received event for post ${event.postId} with count ${event.commentCount}")

        val repostId = post?._id
        val originalPostId = post?.originalPost?.firstOrNull()?._id

        when (event.postId) {
            repostId -> {
                Log.d(TAG, "onCommentCountUpdated: Event matches repost, updating count")
                updateCommentCount(event.commentCount)
            }
            originalPostId -> {
                Log.d(TAG, "onCommentCountUpdated: Event matches original post, updating original count")
                post?.originalPost?.firstOrNull()?.commentCount = event.commentCount
                // Update original post UI if needed
                updateMetricDisplay(commentCount, event.commentCount, "original_comment")
            }
            else -> {
                Log.d(TAG, "onCommentCountUpdated: Event not for repost or original post")
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCommentPosted(event: CommentPostedEvent) {
        Log.d(TAG, "onCommentPosted: New comment posted for post ${event.postId}")

        val originalPostId = post?.originalPost?.firstOrNull()?._id

        if (event.postId == originalPostId) {
            Log.d(TAG, "onCommentPosted: Event matches ORIGINAL POST, incrementing count")
            incrementCommentCount()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCommentDeleted(event: CommentDeletedEvent) {
        Log.d(TAG, "onCommentDeleted: Comment deleted for post ${event.postId}")

        val originalPostId = post?.originalPost?.firstOrNull()?._id

        if (event.postId == originalPostId) {
            Log.d(TAG, "onCommentDeleted: Event matches ORIGINAL POST, decrementing count")
            decrementCommentCount()
        }
    }

    fun bind(post: Post, position: Int) {
        this.currentPost = post
        this.currentPosition = position

        Log.d(TAG, "bind: Binding post ${post._id} at position $position")


        val initialCommentCount = getOriginalPostCommentCount(post)

        totalRepostComments = initialCommentCount
        Log.d(TAG, "bind: Set initial comment count to $totalRepostComments from original post")

        // Setup initial metrics
        setupInitialMetrics(post)

        // Then setup button listeners
        setupLikeButton(post)
        setupBookmarkButton(post)
        setupCommentButton(post)
        setupRepostButton(post)
        setupShareButton(post)

        // Populate other post data
        populatePostData(post)
    }

    private fun getOriginalPostCommentCount(post: Post): Int {
        return post.originalPost?.firstOrNull()?.commentCount ?: post.originalPost?.firstOrNull()?.commentCount ?: 0
    }

    private fun getOriginalPostId(post: Post): String {
        return post.originalPost?.firstOrNull()?._id ?: post._id
    }

    private fun fetchAndUpdateCommentCount(postId: String) {
        Log.d(TAG, "fetchAndUpdateCommentCount: Fetching current comment count for post: $postId")

        RetrofitClient.commentService.getCommentCount(postId)
            .enqueue(object : Callback<CommentCountResponse> {
                override fun onResponse(call: Call<CommentCountResponse>, response: Response<CommentCountResponse>) {
                    if (response.isSuccessful && isAdded) {
                        response.body()?.let { countResponse ->
                            val serverCount = countResponse.count
                            Log.d(TAG, "fetchAndUpdateCommentCount: API returned count: $serverCount")

                            if (postId == post?._id) {
                                // Update repost comment count
                                updateCommentCount(serverCount)
                                currentPost?.comments = serverCount
                                Log.d(TAG, "fetchAndUpdateCommentCount: Updated repost count to $serverCount")
                            } else if (postId == post?.originalPost?.firstOrNull()?._id) {
                                // Update original post comment count
                                post?.originalPost?.firstOrNull()?.commentCount = serverCount
                                updateMetricDisplay(commentCount, serverCount, "original_comment")
                                Log.d(TAG, "fetchAndUpdateCommentCount: Updated original post count to $serverCount")
                            }
                        } ?: run {
                            Log.w(TAG, "fetchAndUpdateCommentCount: Response body is null, falling back to comments API")
                            fallbackToCommentsAPI(postId)
                        }
                    } else {
                        Log.e(TAG, "fetchAndUpdateCommentCount: Failed with code: ${response.code()}, falling back to comments API")
                        fallbackToCommentsAPI(postId)
                    }
                }

                override fun onFailure(call: Call<CommentCountResponse>, t: Throwable) {
                    when (t) {
                        is JsonSyntaxException, is JsonSyntaxException -> {
                            Log.e(TAG, "fetchAndUpdateCommentCount: JSON parsing error - malformed response from server", t)
                        }
                        is MalformedJsonException -> {
                            Log.e(TAG, "fetchAndUpdateCommentCount: Malformed JSON response from server", t)
                        }
                        else -> {
                            Log.e(TAG, "fetchAndUpdateCommentCount: Network error", t)
                        }
                    }

                    // Keep existing count
                    currentPost?.let { post ->
                        Log.d(TAG, "fetchAndUpdateCommentCount: Keeping existing count: ${post.comments}")
                    }
                    fallbackToCommentsAPI(postId)
                }
            })
    }

    private fun fallbackToCommentsAPI(postId: String) {
        Log.d(TAG, "fallbackToCommentsAPI: Using comments API to get accurate count for post: $postId")

        // Use your existing fetchFeedComments method which works correctly
        fetchFeedComments(postId)
    }

    private fun fetchFeedComments(postId: String) {
        Log.d(TAG, "fetchFeedComments: inside")

        RetrofitClient.commentService.getCommentsForPost(postId)
            .enqueue(object : Callback<CommentsResponse> {
                override fun onResponse(call: Call<CommentsResponse>, response: Response<CommentsResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val commentsResponse = response.body()!!
                        if (commentsResponse.success) {
                            val commentCount = commentsResponse.comments.size
                            Log.d(TAG, "fetchFeedComments: response success totalComments?: $commentCount")

                            // Update comment count from response using your existing method
                            updateCommentCountFromCommentsResponse(postId, commentCount)

                            // POST THE COMMENT COUNT UPDATE EVENT HERE
                            EventBus.getDefault().post(CommentCountUpdatedEvent(postId, commentCount))

                            // Also post the comments loaded event
                            EventBus.getDefault().post(CommentsLoadedEvent(postId, commentCount, commentsResponse.comments))
                        } else {
                            Log.e(TAG, "fetchFeedComments: API returned success=false: ${commentsResponse.message}")
                        }
                    } else {
                        Log.e(TAG, "fetchFeedComments: Response unsuccessful or body null. Code: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<CommentsResponse>, t: Throwable) {
                    Log.e(TAG, "fetchFeedComments: Network error", t)

                    // Keep existing count in case of failure
                    currentPost?.let { post ->
                        Log.d(TAG, "fetchFeedComments: Keeping existing count due to error: ${post.comments}")
                    }
                }
            })
    }



    private fun navigateToFragment_Original_Post_Without_Repost_Inside(data: Post) {
        try {

            Log.d(TAG, "Navigating to original Post for Post ID: ${data._id}")

            val fragment = Fragment_Original_Post_Without_Repost_Inside().apply {
                arguments = Bundle().apply {
                    putSerializable(Fragment_Original_Post_Without_Repost_Inside.ARG_ORIGINAL_POST, data)
                    putString("post_id", data._id)
                    val absoluteAdapterPosition = 0
                    putInt("adapter_position", absoluteAdapterPosition)
                    putString("navigation_source", "feed_mixed_files")
                    putLong("navigation_timestamp", System.currentTimeMillis())
                }
            }
            val activity = getActivityFromContext(itemView.context)
            if (activity != null) {
                val fragmentManager = activity.supportFragmentManager
                fragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                    )
                    .replace(R.id.frame_layout, fragment)
                    .addToBackStack("fragment_original_post_without_repost_inside")
                    .commit()
                Log.d(TAG, "Successfully navigated to fragment for post ID: ${data._id}")
            } else {
                Log.e(TAG, "Activity is null, cannot navigate to fragment")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to original post fragment: ${e.message}", e)
        }
    }


    private fun handleFeedCommentClicked(position: Int, post: Post) {
        Log.d(TAG, "handleFeedCommentClicked: Starting comment flow for post ${post?._id}")

        try {
            // Post the event
            EventBus.getDefault().post(com.uyscuti.social.circuit.model.FeedCommentClicked(position, post))
            Log.d(TAG, "handleFeedCommentClicked: Event posted successfully")

            // Don't fetch comment count immediately - wait for the comment activity/fragment to finish
            // The count will be updated via EventBus when comments are loaded or posted

        } catch (e: Exception) {
            Log.e(TAG, "Error posting comment event: ${e.message}", e)
        }
    }

    fun updateCommentCount(newCount: Int) {
        Log.d(TAG, "updateCommentCount: Updating comment count from $totalRepostComments to $newCount")

        val previousCount = totalRepostComments
        totalRepostComments = if (newCount < 0) {
            Log.w(TAG, "updateCommentCount: Negative count received, setting to 0")
            0
        } else {
            newCount
        }

        // Update the current post object
        currentPost?.comments = totalRepostComments

        // If this is a repost, also update the original post
        post?.originalPost?.firstOrNull()?.commentCount = totalRepostComments

        // Update UI on main thread
        if (Looper.myLooper() == Looper.getMainLooper()) {
            updateMetricDisplay(commentCount, totalRepostComments, "comment")

            // Only animate if count actually changed
            if (previousCount != totalRepostComments) {
                YoYo.with(Techniques.Pulse)
                    .duration(500)
                    .playOn(commentCount)
            }
        } else {
            // Post to main thread
            commentCount.post {
                updateMetricDisplay(commentCount, totalRepostComments, "comment")

                if (previousCount != totalRepostComments) {
                    YoYo.with(Techniques.Pulse)
                        .duration(500)
                        .playOn(commentCount)
                }
            }
        }

        Log.d(TAG, "updateCommentCount: Successfully updated to $totalRepostComments")
    }

    private fun setupLikeButton(data: Post) {
        Log.d(TAG, "Setting up like button - Initial state: isLiked=${data.isLiked}, likes=${data.likes}")
        updateLikeButtonUI(data.isLiked ?: false)
        updateMetricDisplay(likesCount, data.likes, "like")

        likeButtonIcon.setOnClickListener {
            if (!likeButtonIcon.isEnabled) return@setOnClickListener

            Log.d(TAG, "Like clicked for post: ${data._id}")
            Log.d(TAG, "Current state before toggle: isLiked=${data.isLiked}, likes=${data.likes}")

            val newLikeStatus = !(data.isLiked ?: false)
            val previousLikeStatus = data.isLiked ?: false
            val previousLikesCount = data.likes

            // Update data immediately
            data.isLiked = newLikeStatus
            data.likes = if (newLikeStatus) data.likes + 1 else maxOf(0, data.likes - 1)
            totalMixedLikesCounts = data.likes

            Log.d(TAG, "New state after toggle: isLiked=${data.isLiked}, likes=${data.likes}")

            // Update UI immediately for better UX
            updateLikeButtonUI(data.isLiked ?: false)
            updateMetricDisplay(likesCount, data.likes, "like")

            // Animation
            YoYo.with(if (newLikeStatus) Techniques.Tada else Techniques.Pulse)
                .duration(300)
                .repeat(1)
                .playOn(likeButtonIcon)

            // Disable button during network call
            likeButtonIcon.isEnabled = false
            likeButtonIcon.alpha = 0.8f

            val likeRequest = LikeRequest(newLikeStatus)
            RetrofitClient.likeService.toggleLike(data._id, likeRequest)
                .enqueue(object : Callback<LikeResponse> {
                    override fun onResponse(call: Call<LikeResponse>, response: Response<LikeResponse>) {
                        likeButtonIcon.alpha = 1f
                        likeButtonIcon.isEnabled = true

                        if (response.isSuccessful) {
                            response.body()?.let { likeResponse ->
                                Log.d(TAG, "Like API success - Server count: ${likeResponse.likesCount}")
                                // Only update if server count is significantly different
                                if (likeResponse.likesCount != null &&
                                    abs(likeResponse.likesCount - data.likes) > 1
                                ) {
                                    data.likes = likeResponse.likesCount
                                    totalMixedLikesCounts = data.likes
                                    updateMetricDisplay(likesCount, data.likes, "like")
                                    Log.d(TAG, "Updated likes count from server: ${data.likes}")
                                }
                            }
                        } else {
                            Log.e(TAG, "Like sync failed: ${response.code()}")
                            // Only revert on actual API errors, not JSON parsing issues
                            if (response.code() != 200) {
                                data.isLiked = previousLikeStatus
                                data.likes = previousLikesCount
                                totalMixedLikesCounts = data.likes
                                updateLikeButtonUI(data.isLiked ?: false)
                                updateMetricDisplay(likesCount, data.likes, "like")
                                Log.d(TAG, "Reverted to previous state: isLiked=${data.isLiked}, likes=${data.likes}")
                            }
                        }
                    }

                    override fun onFailure(call: Call<LikeResponse>, t: Throwable) {
                        likeButtonIcon.alpha = 1f
                        likeButtonIcon.isEnabled = true

                        // Check if it's a JSON parsing error
                        if (t is MalformedJsonException ||
                            t.message?.contains("MalformedJsonException") == true) {
                            Log.w(TAG, "Like API returned malformed JSON but operation likely succeeded - keeping UI state")
                            // Don't revert UI changes for JSON parsing errors as the operation likely succeeded
                            return
                        }

                        Log.e(TAG, "Like network error - reverting changes", t)
                        // Only revert for actual network failures
                        data.isLiked = previousLikeStatus
                        data.likes = previousLikesCount
                        totalMixedLikesCounts = data.likes
                        updateLikeButtonUI(data.isLiked ?: false)
                        updateMetricDisplay(likesCount, data.likes, "like")
                        Log.d(TAG, "Reverted to previous state after network error: isLiked=${data.isLiked}, likes=${data.likes}")
                    }
                })

            feedClickListener.likeUnLikeFeed(0, data)
        }
    }

    private fun setupBookmarkButton(data: Post) {
        Log.d(TAG, "Setting up bookmark button - Initial state: isBookmarked=${data.isBookmarked}, bookmarkCount=${data.bookmarkCount}")
        updateBookmarkButtonUI(data.isBookmarked ?: false)
        updateMetricDisplay(favoriteCounts, data.bookmarkCount, "bookmark")

        favoritesButton.setOnClickListener {
            if (!favoritesButton.isEnabled) return@setOnClickListener

            Log.d(TAG, "Bookmark clicked for post: ${data._id}")
            Log.d(TAG, "Current state before toggle: isBookmarked=${data.isBookmarked}, bookmarkCount=${data.bookmarkCount}")

            val newBookmarkStatus = !(data.isBookmarked ?: false)
            val previousBookmarkStatus = data.isBookmarked ?: false
            val previousBookmarkCount = data.bookmarkCount

            // Update data immediately
            data.isBookmarked = newBookmarkStatus
            data.bookmarkCount = if (newBookmarkStatus) data.bookmarkCount + 1 else maxOf(0, data.bookmarkCount - 1)
            totalMixedBookMarkCounts = data.bookmarkCount

            Log.d(TAG, "New state after toggle: isBookmarked=${data.isBookmarked}, bookmarkCount=${data.bookmarkCount}")

            // Update UI immediately for better UX
            updateBookmarkButtonUI(data.isBookmarked ?: false)
            updateMetricDisplay(favoriteCounts, data.bookmarkCount, "bookmark")

            // Animation
            YoYo.with(if (newBookmarkStatus) Techniques.Tada else Techniques.Pulse)
                .duration(500)
                .repeat(1)
                .playOn(favoritesButton)

            // Disable button during network call
            favoritesButton.isEnabled = false
            favoritesButton.alpha = 0.8f

            val bookmarkRequest = BookmarkRequest(newBookmarkStatus)
            RetrofitClient.bookmarkService.toggleBookmark(data._id, bookmarkRequest)
                .enqueue(object : Callback<BookmarkResponse> {
                    override fun onResponse(call: Call<BookmarkResponse>, response: Response<BookmarkResponse>) {
                        favoritesButton.alpha = 1f
                        favoritesButton.isEnabled = true

                        if (response.isSuccessful) {
                            response.body()?.let { bookmarkResponse ->
                                Log.d(TAG, "Bookmark API success - Server count: ${bookmarkResponse.bookmarkCount}")
                                if (abs(bookmarkResponse.bookmarkCount - data.bookmarkCount) > 1) {
                                    data.bookmarkCount = bookmarkResponse.bookmarkCount
                                    totalMixedBookMarkCounts = data.bookmarkCount
                                    updateMetricDisplay(favoriteCounts, data.bookmarkCount, "bookmark")
                                    Log.d(TAG, "Updated bookmark count from server: ${data.bookmarkCount}")
                                }
                            }
                        } else {
                            Log.e(TAG, "Bookmark sync failed: ${response.code()}")
                            // Only revert on actual API errors, not JSON parsing issues
                            if (response.code() != 200) {
                                data.isBookmarked = previousBookmarkStatus
                                data.bookmarkCount = previousBookmarkCount
                                totalMixedBookMarkCounts = data.bookmarkCount
                                updateBookmarkButtonUI(data.isBookmarked ?: false)
                                updateMetricDisplay(favoriteCounts, data.bookmarkCount, "bookmark")
                                Log.d(TAG, "Reverted to previous state: isBookmarked=${data.isBookmarked}, bookmarkCount=${data.bookmarkCount}")
                            }
                        }
                    }

                    override fun onFailure(call: Call<BookmarkResponse>, t: Throwable) {
                        favoritesButton.alpha = 1f
                        favoritesButton.isEnabled = true

                        // Check if it's a JSON parsing error
                        if (t is MalformedJsonException ||
                            t.message?.contains("MalformedJsonException") == true) {
                            Log.w(TAG, "Bookmark API returned malformed JSON but operation likely succeeded - keeping UI state")
                            // Don't revert UI changes for JSON parsing errors as the operation likely succeeded
                            return
                        }

                        Log.e(TAG, "Bookmark network error - reverting changes", t)
                        // Only revert for actual network failures
                        data.isBookmarked = previousBookmarkStatus
                        data.bookmarkCount = previousBookmarkCount
                        totalMixedBookMarkCounts = data.bookmarkCount
                        updateBookmarkButtonUI(data.isBookmarked ?: false)
                        updateMetricDisplay(favoriteCounts, data.bookmarkCount, "bookmark")
                        Log.d(TAG, "Reverted to previous state after network error: isBookmarked=${data.isBookmarked}, bookmarkCount=${data.bookmarkCount}")
                    }
                })

            feedClickListener.feedFavoriteClick(0, data)
        }
    }

    private fun setupShareButton(data: Post) {
        updateMetricDisplay(shareCount, data.shareCount, "share")
        shareButtonIcon.setOnClickListener {
            if (!shareButtonIcon.isEnabled) return@setOnClickListener

            Log.d(TAG, "Share clicked for post: ${data._id}")
            val previousShareCount = data.shareCount

            // Update immediately for better UX
            data.shareCount += 1
            totalMixedShareCounts = data.shareCount
            updateMetricDisplay(shareCount, data.shareCount, "share")

            YoYo.with(Techniques.Tada)
                .duration(700)
                .repeat(1)
                .playOn(shareButtonIcon)

            shareButtonIcon.isEnabled = false
            shareButtonIcon.alpha = 0.8f

            // Make API call to sync with server
            RetrofitClient.shareService.incrementShare(data._id)
                .enqueue(object : Callback<ShareResponse> {
                    override fun onResponse(call: Call<ShareResponse>, response: Response<ShareResponse>) {
                        shareButtonIcon.alpha = 1f
                        shareButtonIcon.isEnabled = true

                        if (response.isSuccessful) {
                            response.body()?.let { shareResponse ->
                                if (abs(shareResponse.shareCount - data.shareCount) > 1) {
                                    data.shareCount = shareResponse.shareCount
                                    totalMixedShareCounts = data.shareCount
                                    updateMetricDisplay(shareCount, data.shareCount, "share")
                                    Log.d(TAG, "Updated share count from server: ${data.shareCount}")
                                }
                            }
                        } else {
                            Log.e(TAG, "Share sync failed: ${response.code()}")
                            // Only revert on actual API errors, not JSON parsing issues
                            if (response.code() != 200) {
                                data.shareCount = previousShareCount
                                totalMixedShareCounts = data.shareCount
                                updateMetricDisplay(shareCount, data.shareCount, "share")
                            }
                        }
                    }

                    override fun onFailure(call: Call<ShareResponse>, t: Throwable) {
                        shareButtonIcon.alpha = 1f
                        shareButtonIcon.isEnabled = true

                        // Check if it's a JSON parsing error
                        if (t is MalformedJsonException ||
                            t.message?.contains("MalformedJsonException") == true) {
                            Log.w(TAG, "Share API returned malformed JSON but operation likely succeeded - keeping UI state")
                            // Don't revert UI changes for JSON parsing errors as the operation likely succeeded
                            return
                        }

                        Log.e(TAG, "Share network error - reverting changes", t)
                        // Only revert for actual network failures
                        data.shareCount = previousShareCount
                        totalMixedShareCounts = data.shareCount
                        updateMetricDisplay(shareCount, data.shareCount, "share")
                    }
                })

            // Show the share dialog
            feedShareClicked(0, data)
        }
    }

    @SuppressLint("MissingInflatedId")
    fun feedShareClicked(
        position: Int,
        data: Post
    ) {
        val context = requireContext()
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val shareView = layoutInflater.inflate(R.layout.bottom_dialog_for_share, null)

        // Find views from the XML layout
        val btnWhatsApp = shareView.findViewById<ImageButton>(R.id.btnWhatsApp)
        val btnSMS = shareView.findViewById<ImageButton>(R.id.btnSMS)
        val btnInstagram = shareView.findViewById<ImageButton>(R.id.btnInstagram)
        val btnMessenger = shareView.findViewById<ImageButton>(R.id.btnMessenger)
        val btnFacebook = shareView.findViewById<ImageButton>(R.id.btnFacebook)
        val btnTelegram = shareView.findViewById<ImageButton>(R.id.btnTelegram)
        val btnReport = shareView.findViewById<ImageButton>(R.id.btnReport)
        val btnNotInterested = shareView.findViewById<ImageButton>(R.id.btnNotInterested)
        val btnSaveVideo = shareView.findViewById<ImageButton>(R.id.btnSaveVideo)
        val btnDuet = shareView.findViewById<ImageButton>(R.id.btnDuet)
        val btnReact = shareView.findViewById<ImageButton>(R.id.btnReact)
        val btnAddToFavorites = shareView.findViewById<ImageButton>(R.id.btnAddToFavorites)
        val btnCancel = shareView.findViewById<TextView>(R.id.btnCancel)

        bottomSheetDialog.setContentView(shareView)
        bottomSheetDialog.show()

        // Helper function to share content
        fun shareToApp(packageName: String, appName: String) {
            try {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, data.content) // Assuming Post has a content field
                    setPackage(packageName)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share via $appName"))
            } catch (e: Exception) {
                Toast.makeText(context, "Unable to share to $appName", Toast.LENGTH_SHORT).show()
            }
        }

        // Set click listeners for share buttons
        btnWhatsApp.setOnClickListener { shareToApp("com.whatsapp", "WhatsApp") }

        btnSMS.setOnClickListener {
            try {
                val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                    setData(Uri.parse("smsto:"))
                    putExtra("sms_body", data.content)
                }
                context.startActivity(Intent.createChooser(smsIntent, "Share via SMS"))
            } catch (e: Exception) {
                Toast.makeText(context, "Unable to share via SMS", Toast.LENGTH_SHORT).show()
            }
        }

        btnInstagram.setOnClickListener { shareToApp("com.instagram.android", "Instagram") }
        btnMessenger.setOnClickListener { shareToApp("com.facebook.orca", "Messenger") }
        btnFacebook.setOnClickListener { shareToApp("com.facebook.katana", "Facebook") }
        btnTelegram.setOnClickListener { shareToApp("org.telegram.messenger", "Telegram") }

        // Set click listeners for action buttons (placeholder implementations)
        btnReport.setOnClickListener {
            Toast.makeText(context, "Report clicked", Toast.LENGTH_SHORT).show()
            // Implement report functionality
        }
        btnNotInterested.setOnClickListener {
            Toast.makeText(context, "Not Interested clicked", Toast.LENGTH_SHORT).show()
            // Implement not interested functionality
        }
        btnSaveVideo.setOnClickListener {
            Toast.makeText(context, "Save Video clicked", Toast.LENGTH_SHORT).show()
            // Implement save video functionality
        }
        btnDuet.setOnClickListener {
            Toast.makeText(context, "Duet clicked", Toast.LENGTH_SHORT).show()
            // Implement duet functionality
        }
        btnReact.setOnClickListener {
            Toast.makeText(context, "React clicked", Toast.LENGTH_SHORT).show()
            // Implement react functionality
        }
        btnAddToFavorites.setOnClickListener {
            Toast.makeText(context, "Add to Favorites clicked", Toast.LENGTH_SHORT).show()
            // Implement add to favorites functionality
        }

        // Set click listener for cancel button
        btnCancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
    }

    private fun setupRepostButton(data: Post) {
        totalMixedRePostCounts = data.repostCount
        updateMetricDisplay(repostCount, totalMixedRePostCounts, "repost")
        updateRepostButtonAppearance(data.isReposted)
        repostPost.setOnClickListener { view ->
            if (! repostPost.isEnabled) return@setOnClickListener
            repostPost.isEnabled = false
            try {
                val wasReposted = data.isReposted
                data.isReposted = !wasReposted
                totalMixedRePostCounts = if (data.isReposted) totalMixedRePostCounts + 1 else maxOf(0, totalMixedRePostCounts - 1)
                data.repostCount = totalMixedRePostCounts
                updateMetricDisplay(repostCount, totalMixedRePostCounts, "repost")
                updateRepostButtonAppearance(data.isReposted)
                YoYo.with(if (data.isReposted) Techniques.Tada else Techniques.Pulse)
                    .duration(700)
                    .playOn( repostPost)
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
                                    data.repostCount = repostResponse.repostCount
                                    totalMixedRePostCounts = repostResponse.repostCount
                                    updateMetricDisplay(repostCount, totalMixedRePostCounts, "repost")
                                }
                            }
                        } else {
                            Log.e(TAG, "Repost API failed: ${response.code()}")
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
                feedClickListener.feedRepostPost(0, data)
            } catch (e: Exception) {
                repostPost.isEnabled = true
                repostPost.alpha = 1f
                Log.e(TAG, "Exception in repost click listener", e)
            }
        }
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCommentEventReceived(event: com.uyscuti.social.circuit.model.FeedCommentClicked) {
        Log.d(TAG, "onCommentEventReceived: Comment event received in fragment for post ${event.data?._id}")
        // Handle comment event
        currentPost?.let { post ->
            if (post._id == event.data?._id) {
                // Refresh comment count for this post
                fetchAndUpdateCommentCount(post._id)
            }
        }
    }

    private fun updateCommentCountFromCommentsResponse(postId: String, newCommentCount: Int) {
        if (currentPost?._id == postId) {
            Log.d(TAG, "updateCommentCountFromCommentsResponse: Updating count to $newCommentCount for post $postId")
            totalRepostComments = newCommentCount
            currentPost?.comments = newCommentCount
            updateMetricDisplay(commentCount, totalRepostComments, "comment")

            // Add subtle animation to indicate update
            YoYo.with(Techniques.Pulse)
                .duration(300)
                .playOn(commentCount)
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
                                post.comments = newCount  // Fix: use commentCount instead of comments
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

    private fun loadCommentsFromAPI(postId: String, callback: (List<Comment>) -> Unit) {
        Log.d(TAG, "loadCommentsFromAPI: Loading comments for post: $postId")
        RetrofitClient.commentService.getCommentsForPost(postId)
            .enqueue(object : Callback<CommentsResponse> {
                override fun onResponse(call: Call<CommentsResponse>, response: Response<CommentsResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { commentsResponse ->
                            if (commentsResponse.success) {
                                Log.d(TAG, "loadCommentsFromAPI: Successfully loaded ${commentsResponse.comments.size} comments")
                                processLoadedComments(commentsResponse.comments)  // Use the method
                                callback(commentsResponse.comments)
                            } else {
                                Log.e(TAG, "loadCommentsFromAPI: API returned error: ${commentsResponse.message}")
                                callback(emptyList())
                            }
                        } ?: run {
                            Log.e(TAG, "loadCommentsFromAPI: Response body is null")
                            callback(emptyList())
                        }
                    } else {
                        Log.e(TAG, "loadCommentsFromAPI: API call failed with code: ${response.code()}")
                        callback(emptyList())
                    }
                }
                override fun onFailure(call: Call<CommentsResponse>, t: Throwable) {
                    Log.e(TAG, "loadCommentsFromAPI: Network error", t)
                    callback(emptyList())
                }
            })
    }

    private fun updateLikeButtonUI(isLiked: Boolean) {
        Log.d(tag, "Updating like button UI: isLiked=$isLiked")
        try {
            if (isLiked) {
                likeButtonIcon.setImageResource(R.drawable.filled_favorite_like)
            } else {
                likeButtonIcon.setImageResource(R.drawable.heart_svgrepo_com)
                likeButtonIcon.clearColorFilter()
            }
        } catch (e: Exception) {
            Log.e(tag, "Error updating like button UI", e)
        }
    }

    private fun updateBookmarkButtonUI(isBookmarked: Boolean) {
        Log.d(tag, "Updating bookmark button UI: isBookmarked=$isBookmarked")
        try {
            if (isBookmarked) {
                favoritesButton.setImageResource(R.drawable.filled_favorite)
            } else {
                favoritesButton.setImageResource(R.drawable.favorite_svgrepo_com__1_)
                favoritesButton.clearColorFilter()
            }
        } catch (e: Exception) {
            Log.e(tag, "Error updating bookmark button UI", e)
        }
    }

    private fun updateRepostButtonAppearance(isReposted: Boolean) {
        if (isReposted) {
            repostPost.setImageResource(R.drawable.repeat_svgrepo_com)
            repostPost.scaleX = 1.1f
            repostPost.scaleY = 1.1f
        } else {
            repostPost.setImageResource(R.drawable.repeat_svgrepo_com)
            repostPost.scaleX = 1.0f
            repostPost.scaleY = 1.0f
        }
    }

    private fun showMoreOptionsDialog(post: Post) {
        // Implementation for showing more options
        val options = arrayOf("Edit", "Delete", "Report", "Share")
        AlertDialog.Builder(requireContext())
            .setTitle("Post Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> editPost(post)
                    1 -> deletePost(post)
                    2 -> reportPost(post)
                    3 -> sharePost(post)
                }
            }
            .show()
    }

    private fun editPost(post: Post) {
        // Handle post editing
        EventBus.getDefault().post(PostUpdatedEvent(post._id, post))
    }

    private fun deletePost(post: Post) {
        // Handle post deletion
    }

    private fun reportPost(post: Post) {
        // Handle post reporting
    }

    private fun sharePost(post: Post) {
        // Handle post sharing
        feedShareClicked(0, post)
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

    private fun navigateToEditPostToRepost(data: Post) {
        try {
            val fragment = Fragment_Edit_Post_To_Repost(data).apply {
                arguments = Bundle().apply {
                    putString("post_data", Gson().toJson(data))
                    putString("post_id", data._id)
                    val currentUser = LocalStorage.getInstance(itemView.context).getUser() as? User
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
                    putBoolean("is_editing_existing_repost", data.isReposted == true)
                    putInt("adapter_position", 0)
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

    private fun updateLikeUI(isLiked: Boolean) {
        Log.d(TAG, "Updating like button UI: isLiked=$isLiked")
        try {
            if (isLiked) {
                likeButtonIcon.setImageResource(R.drawable.filled_favorite_like)
            } else {
                likeButtonIcon.setImageResource(R.drawable.heart_svgrepo_com)
                likeButtonIcon.clearColorFilter()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating like button UI", e)
        }
    }

    private fun updateFavoriteUI(isFavorited: Boolean) {
        Log.d(TAG, "Updating bookmark button UI: isBookmarked=$isFavorited")
        try {
            if (isFavorited) {
                favoritesButton.setImageResource(R.drawable.filled_favorite)
            } else {
                favoritesButton.setImageResource(R.drawable.favorite_svgrepo_com__1_)
                favoritesButton.clearColorFilter()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating bookmark button UI", e)
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
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                    )
                    .replace(R.id.frame_layout, fragment)
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
    private fun immediateNavigateBack() {
        // Prevent multiple simultaneous navigation attempts
        if (isNavigationInProgress) {
            Log.d(Companion.TAG, "Navigation already in progress, ignoring")
            return
        }

        isNavigationInProgress = true

        try {
            Log.d(Companion.TAG, "Starting immediate navigation back")

            // Clean up resources first
            cleanupResources()

            // Restore system UI immediately
            restoreSystemBarsImmediately()

            // Don't use any delays - try to navigate immediately but safely
            performSafeBackNavigation()

        } catch (e: Exception) {
            Log.e(Companion.TAG, "Error in immediate navigation", e)
            isNavigationInProgress = false
        }
    }

    private fun performSafeBackNavigation() {
        try {
            // Check if fragment is still valid
            if (!isAdded || isDetached || activity == null) {
                Log.w(Companion.TAG, "Fragment not attached, cannot navigate back")
                isNavigationInProgress = false
                return
            }

            val fragmentManager = parentFragmentManager

            // Multiple checks to ensure FragmentManager is ready
            if (fragmentManager.isStateSaved || fragmentManager.isDestroyed) {
                Log.w(Companion.TAG, "FragmentManager not ready, cannot navigate")
                isNavigationInProgress = false
                return
            }

            // Check if there's anything to pop
            if (fragmentManager.backStackEntryCount <= 0) {
                Log.d(Companion.TAG, "No back stack entries, staying in current state")
                isNavigationInProgress = false
                return
            }

            // Try immediate navigation first
            try {
                // Use a different approach: remove this fragment from the parent container
                // This avoids ViewPager2 conflicts
                val parentFragment = parentFragment
                if (parentFragment != null) {
                    // If we're in a ViewPager, handle it differently
                    handleViewPagerNavigation()
                } else {
                    // Standard fragment navigation
                    fragmentManager.popBackStackImmediate()
                    Log.d(Companion.TAG, "Standard back navigation successful")
                    isNavigationInProgress = false
                }
            } catch (e: IllegalStateException) {
                Log.w(Companion.TAG, "Immediate navigation failed, trying alternative", e)
                // Alternative approach: Post to a different thread
                Thread {
                    try {
                        Thread.sleep(50) // Very short wait
                        requireActivity().runOnUiThread {
                            performDelayedNavigation()
                        }
                    } catch (e2: Exception) {
                        Log.e(Companion.TAG, "Thread-based navigation failed", e2)
                        requireActivity().runOnUiThread {
                            isNavigationInProgress = false
                        }
                    }
                }.start()
            }

        } catch (e: Exception) {
            Log.e(Companion.TAG, "Error in performSafeBackNavigation", e)
            isNavigationInProgress = false
        }
    }

    @OptIn(UnstableApi::class)
    private fun handleViewPagerNavigation() {
        try {

            val activity = activity as? MainActivity
            if (activity != null) {

                activity.runOnUiThread {
                    try {

                        activity.onBackPressedDispatcher.onBackPressed()
                        Log.d(Companion.TAG, "ViewPager navigation delegated to activity")
                        isNavigationInProgress = false
                    } catch (e: Exception) {
                        Log.e(Companion.TAG, "Activity navigation failed", e)
                        performDelayedNavigation()
                    }
                }
            } else {
                performDelayedNavigation()
            }
        } catch (e: Exception) {
            Log.e(Companion.TAG, "ViewPager navigation failed", e)
            performDelayedNavigation()
        }
    }

    @OptIn(UnstableApi::class)
    private fun setupBackNavigation() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d(TAG, "Back pressed - starting navigation")
                navigateBack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    @OptIn(UnstableApi::class)
    private fun navigateBack() {
        // Prevent multiple simultaneous navigation attempts
        if (isNavigationInProgress) {
            Log.d(TAG, "Navigation already in progress, ignoring")
            return
        }

        isNavigationInProgress = true

        try {
            // Clean up resources first
            cleanupResources()
            restoreSystemBarsImmediately()

            // Single navigation attempt with proper fallback
            performNavigation()

        } catch (e: Exception) {
            Log.e(TAG, "Error in navigation", e)
            isNavigationInProgress = false
        }
    }

    private fun performNavigation() {
        // Check if fragment is still valid
        if (!isAdded || isDetached || activity == null) {
            Log.w(TAG, "Fragment not attached, cannot navigate back")
            isNavigationInProgress = false
            return
        }

        val fragmentManager = parentFragmentManager

        // Check FragmentManager state
        if (fragmentManager.isStateSaved || fragmentManager.isDestroyed) {
            Log.w(TAG, "FragmentManager not ready, cannot navigate")
            isNavigationInProgress = false
            return
        }

        // Check if there's anything to pop
        if (fragmentManager.backStackEntryCount <= 0) {
            Log.d(TAG, "No back stack entries, finishing navigation")
            isNavigationInProgress = false
            return
        }

        // Try immediate navigation, with single fallback
        try {
            fragmentManager.popBackStackImmediate()
            Log.d(TAG, "Immediate navigation successful")
            isNavigationInProgress = false
        } catch (e: IllegalStateException) {
            Log.w(TAG, "Immediate navigation failed, using delayed approach", e)
            performDelayedNavigation()
        }
    }

    private fun performDelayedNavigation() {
        // Use view.post instead of Handler for better synchronization with UI thread
        view?.post {
            try {
                if (!isAdded || isDetached || activity == null) {
                    isNavigationInProgress = false
                    return@post
                }

                val fragmentManager = parentFragmentManager

                if (!fragmentManager.isStateSaved && !fragmentManager.isDestroyed) {
                    if (fragmentManager.backStackEntryCount > 0) {
                        try {
                            // Use regular popBackStack (not immediate) to avoid conflicts
                            fragmentManager.popBackStack()
                            Log.d(TAG, "Delayed navigation successful")
                        } catch (e: Exception) {
                            Log.e(TAG, "Delayed navigation failed", e)
                            // If all else fails, try to remove the fragment
                            removeFragmentSafely()
                        }
                    }
                }
                isNavigationInProgress = false
            } catch (e: Exception) {
                Log.e(TAG, "Delayed navigation error", e)
                isNavigationInProgress = false
            }
        }
    }

    private fun removeFragmentSafely() {
        try {
            if (!isAdded || activity == null) {
                return
            }

            val fragmentManager = parentFragmentManager
            if (!fragmentManager.isStateSaved && !fragmentManager.isDestroyed) {
                val transaction = fragmentManager.beginTransaction()
                transaction.remove(this)
                transaction.commitAllowingStateLoss() // Allow state loss for edge cases
                Log.d(TAG, "Fragment removed safely")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Safe fragment removal failed", e)
        }
    }

    // Keep your existing cleanup methods
    @OptIn(UnstableApi::class)
    private fun restoreSystemBarsImmediately() {
        try {
            val activity = activity ?: return
            if (!isAdded) return

            WindowCompat.setDecorFitsSystemWindows(activity.window, true)
            WindowInsetsControllerCompat(activity.window, activity.window.decorView)
                .show(WindowInsetsCompat.Type.systemBars())

            (activity as? MainActivity)?.let { mainActivity ->
                try {
                    mainActivity.showAppBar()
                    mainActivity.showBottomNavigation()
                } catch (e: Exception) {
                    Log.e(TAG, "Error showing MainActivity UI elements", e)
                }
            }

            Log.d(TAG, "System bars restored")
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring system bars", e)
        }
    }

    private fun cleanupResources() {
        try {
            _binding?.let { binding ->
                binding.replyInput.clearFocus()
                val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.replyInput.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
            Log.d(TAG, "Resources cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }

    override fun onDestroyView() {
        // Cancel any pending navigation operations
        view?.removeCallbacks(null)
        isNavigationInProgress = false

        super.onDestroyView()
        try {
            cleanupResources()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroyView cleanup", e)
        } finally {
            _binding = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isNavigationInProgress = false
        // Clean up other resources as needed
    }



    private fun loadProfileImage(url: String?, imageView: ImageView) {
        try {
            if (isAdded) { // Check if fragment is still attached
                if (!url.isNullOrBlank()) {
                    Glide.with(itemView.context)
                        .load(url)
                        .apply(RequestOptions.bitmapTransform(CircleCrop()))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.flash21)
                        .error(R.drawable.flash21)
                        .into(imageView)
                } else {
                    imageView.setImageResource(R.drawable.flash21)
                    Log.w(TAG, "Profile image URL is null or blank, using default image")
                }
            } else {
                imageView.setImageResource(R.drawable.flash21)
                Log.e(TAG, "Fragment not attached, cannot load profile image")
            }
        } catch (e: Exception) {
            imageView.setImageResource(R.drawable.flash21)
            Log.e(TAG, "Error loading profile image: ${e.message}", e)
        }
    }


    @SuppressLint("SetTextI18n")
    private fun populateOriginalAuthorInfo(post: Post?) {
        try {
            val originalAuthor = post?.author
            if (originalAuthor == null) {
                Log.w(TAG, "Original author is null, using fallback values")
                if (::originalPosterName.isInitialized) originalPosterName.text = "Unknown User"
                if (::tvQuotedUserHandle.isInitialized) tvQuotedUserHandle.text = "@unknown_user"
                if (::originalPosterProfileImage.isInitialized) {
                    loadProfileImage(null, originalPosterProfileImage)
                }
                return
            }

            // Build display name
            val displayName = when {
                originalAuthor.firstName.isNotBlank() && originalAuthor.lastName.isNotBlank() ->
                    "${originalAuthor.firstName} ${originalAuthor.lastName}"
                originalAuthor.firstName.isNotBlank() -> originalAuthor.firstName
                originalAuthor.lastName.isNotBlank() -> originalAuthor.lastName
                originalAuthor.account.username.isNotBlank() -> originalAuthor.account.username
                else -> "Unknown User"
            }

            val userHandle = if (originalAuthor.account.username.isNotBlank()) {
                "@${originalAuthor.account.username}"
            } else {
                "@unknown_user"
            }

            // Set UI elements
            if (::originalPosterName.isInitialized) {
                originalPosterName.text = displayName
            }
            if (::tvQuotedUserHandle.isInitialized) {
                tvQuotedUserHandle.text = userHandle
            }
            if (::originalPosterProfileImage.isInitialized) {
                loadProfileImage(originalAuthor.account.avatar?.url, originalPosterProfileImage)
            }

            Log.d(TAG, "Original author info populated - Name: '$displayName', Handle: '$userHandle'")
        } catch (e: Exception) {
            Log.e(TAG, "Error populating original author info: ${e.message}", e)
            if (::originalPosterName.isInitialized) originalPosterName.text = "Unknown User"
            if (::tvQuotedUserHandle.isInitialized) tvQuotedUserHandle.text = "@unknown_user"
            if (::originalPosterProfileImage.isInitialized) {
                loadProfileImage(null, originalPosterProfileImage)
            }
        }
    }

    private fun populateOriginalPostData(originalPost: OriginalPost) {
        try {
            if (!isViewsInitialized()) {
                Log.e(TAG, "Views not initialized, cannot populate original post data")
                return
            }

            val author = originalPost.author

            // Build display name
            val displayName = when {
                author.firstName.isNotBlank() && author.lastName.isNotBlank() ->
                    "${author.firstName} ${author.lastName}"
                author.firstName.isNotBlank() -> author.firstName
                author.lastName.isNotBlank() -> author.lastName
                author.account.username.isNotBlank() -> author.account.username
                else -> "Unknown User"
            }

            val userHandle = if (author.account.username.isNotBlank()) {
                "@${author.account.username}"
            } else {
                "@unknown_user"
            }

            // Populate UI
            if (::originalPosterName.isInitialized) {
                originalPosterName.text = displayName
            }
            if (::tvQuotedUserHandle.isInitialized) {
                tvQuotedUserHandle.text = userHandle
            }
            if (::originalPostText.isInitialized) {
                originalPostText.text = originalPost.content
            }
            if (::originalPosterProfileImage.isInitialized) {
                loadProfileImage(author.account.avatar?.url, originalPosterProfileImage)
            }

            // Handle hashtags
            if (::tvQuotedHashtags.isInitialized) {
                val validTags = originalPost.tags.filterNotNull().map { it.toString() }
                if (validTags.isNotEmpty()) {
                    tvQuotedHashtags.text = validTags.joinToString(" ") { if (it.startsWith("#")) it else "#$it" }
                    tvQuotedHashtags.visibility = View.VISIBLE
                } else {
                    tvQuotedHashtags.text = "#SoftwareAppreciation #GameChanger #MustHave"
                    tvQuotedHashtags.visibility = View.VISIBLE
                }
            }

            Log.d(TAG, "Original post data populated - Name: '$displayName', Handle: '$userHandle'")
        } catch (e: Exception) {
            Log.e(TAG, "Error populating original post data: ${e.message}", e)
            if (::originalPosterName.isInitialized) originalPosterName.text = "Unknown User"
            if (::tvQuotedUserHandle.isInitialized) tvQuotedUserHandle.text = "@unknown_user"
            if (::originalPostText.isInitialized) originalPostText.visibility = View.GONE
            if (::originalPosterProfileImage.isInitialized) {
                loadProfileImage(null, originalPosterProfileImage)
            }
            if (::tvQuotedHashtags.isInitialized) {
                tvQuotedHashtags.text = "#SoftwareAppreciation #GameChanger #MustHave"
                tvQuotedHashtags.visibility = View.VISIBLE
            }
        }
    }

    private fun populatePostData(post: Post) {
        try {
            // Ensure views are initialized before using them
            if (!isViewsInitialized()) {
                Log.e(TAG, "Views not initialized, cannot populate post data")
                return
            }

            // Use safe access for lateinit properties
            if (::headerTitle.isInitialized) {
                headerTitle.text = "Post"
            }

            populateReposterInfo(post)
            populateOriginalAuthorInfo(originalPost)
            populateRepostContent(post)

            // Safe call for itemView and null check
            if (::itemView.isInitialized) {
                handleRepostMediaFiles(
                    post = post,
                    itemView = itemView,
                    ivQuotedPostImage = if (::ivQuotedPostImage.isInitialized) ivQuotedPostImage else null
                )
            }

            // Handle original post safely
            if (post.originalPost?.isNotEmpty() == true) {
                val originalPost = post.originalPost[0]
                populateOriginalPostData(originalPost)
                populatePostContent(originalPost, post.createdAt)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error populating post data: ${e.message}", e)
        }
    }

    private fun populateRepostContent(post: Post) {
        try {
            // Handle repost content if any
            if (::userComment.isInitialized) {
                if (post.content.isNotEmpty()) {
                    userComment.text = post.content
                    userComment.visibility = View.VISIBLE
                } else {
                    userComment.visibility = View.GONE
                }
            }

            // Handle hashtags
            if (::tvHashtags.isInitialized) {
                if (post.tags.isNotEmpty()) {
                    tvHashtags.text = post.tags.joinToString(" ") { "#$it" }
                    tvHashtags.visibility = View.VISIBLE
                } else {
                    tvHashtags.visibility = View.GONE
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error populating repost content: ${e.message}", e)
        }
    }

    private fun populatePostContent(originalPost: OriginalPost, repostCreatedAt: String) {
        try {
            // Time for the  Repost Created
            if (::dateTimeCreate.isInitialized) {
                dateTimeCreate.text = formattedMongoDateTime(repostCreatedAt)
            }

            // Time for the original Post Created
            if (::dateTime.isInitialized) {
                dateTime.text = formattedMongoDateTime(originalPost.createdAt)
            }

            if (::userComment.isInitialized) {
                userComment.text = originalPost.content.takeIf { it.isNotEmpty() } ?: "No caption"
            }
            if (::originalPostText.isInitialized) {
                originalPostText.text = originalPost.content
            }

            val tagsText = originalPost.tags.filterNotNull().joinToString(" ") { "#$it" }
            populateTagsViews(tagsText)
        } catch (e: Exception) {
            Log.e(TAG, "Error populating post content: ${e.message}", e)
        }
    }



    private fun File.toPostFile():
            File {
        return File(
            _id = this._id,
            fileId = this.fileId,
            localPath = this.localPath,
            url = this.url,
            mimeType = this.url
        )
    }


    private fun List<File>.toPostFiles():
            List<File> {
        return this.map { it.toPostFile() }
    }


    private fun handlePostMedia(post: Post) {
        try {
            Log.d(
                "MediaDebug",
                "Handling media from regular post with ${post.files.size} files"
            )
            handleMediaFiles(
                item = post,
                files = post.files,
                fileTypes = post.fileTypes,
                itemView = itemView,
                ivQuotedPostImage = if (::ivQuotedPostImage.isInitialized) ivQuotedPostImage else null,
                bindRepostViewHolder = ::bindRepostViewHolder,
                bindOriginalViewHolder = { mediaType, view, item, file ->
                    // Convert Post to handle as repost scenario
                    Log.d(TAG, "Handling Post item in original viewholder context")
                    bindRepostViewHolder(mediaType, view, item, file)
                },
                hideViews = ::hideAllRepostMediaViews
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error handling post media: ${e.message}", e)
        }
    }


    private fun populateTagsViews(tagsText: String) {
        try {
            if (::tvHashtags.isInitialized) {
                tvHashtags.text = tagsText
                tvHashtags.visibility = if (tagsText.isNotEmpty()) View.VISIBLE else View.GONE
            }
            if (::tvQuotedHashtags.isInitialized) {
                tvQuotedHashtags.text = tagsText
                tvQuotedHashtags.visibility = if (tagsText.isNotEmpty()) View.VISIBLE else View.GONE
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error populating tags views: ${e.message}", e)
        }
    }

    private fun handleOriginalPostThumbnails(thumbnails: List<ThumbnailX>, imageView: ImageView) {
        try {
            thumbnails?.firstOrNull()?.thumbnailUrl?.takeIf { it.isNotEmpty() }
                ?.let { thumbnailUrl ->
                    loadImage(thumbnailUrl, imageView)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling original post thumbnails: ${e.message}", e)
        }
    }

    private fun categorizeOriginalPostFiles(originalPost: OriginalPost): CategorizedFiles {
        val files = originalPost.files

        val imageFiles = files.filter { isImageFile(it, emptyList()) }
        val audioFiles = files.filter { isAudioFile(it, emptyList()) }
        val videoFiles = files.filter { isVideoFile(it, emptyList()) }
        val documentFiles = files.filter { isDocumentFile(it, emptyList()) }
        val combinationFiles = files.filter { isCombinationOfMultipleFile(it, emptyList()) }

        return CategorizedFiles(
            imageFiles = imageFiles,
            audioFiles = audioFiles,
            videoFiles = videoFiles,
            documentFiles = documentFiles,
            combinationOfMultipleFiles = combinationFiles
        )
    }

    private fun handleOriginalPostMediaFiles(
        originalPost: OriginalPost,
        itemView: View,
        ivQuotedPostImage: ImageView?

    ) {
        val files = originalPost.files ?: return
        Log.d(TAG, "Processing OriginalPost with ${files.size} files")

        val categorizedFiles = categorizeOriginalPostFiles(originalPost)
        logFileCounts(categorizedFiles)

        val finalMediaType = when {
            categorizedFiles.getTypeCount() > 1 -> MediaType.CombinationOfMultipleFiles
            categorizedFiles.imageFiles.isNotEmpty() -> MediaType.Image
            categorizedFiles.audioFiles.isNotEmpty() -> MediaType.Audio
            categorizedFiles.videoFiles.isNotEmpty() -> MediaType.Video
            categorizedFiles.documentFiles.isNotEmpty() -> MediaType.Document
            else -> MediaType.Unknown
        }

        when (finalMediaType) {
            MediaType.Audio -> {
                showAudioContainer()
                bindRepostedOriginalAudiosOnlyViewHolder(originalPost, files)
            }

            MediaType.Video -> {
                showVideoContainer()
                bindRepostedOriginalVideosOnlyViewHolder(originalPost, files)
            }

            MediaType.Image -> {
                showImageContainer()
                bindRepostedOriginalImagesOnlyViewHolder(originalPost, files)
            }

            MediaType.Document -> {
                showDocumentContainer()
                bindRepostedOriginalDocumentsOnlyViewHolder(originalPost, files)
            }

            MediaType.CombinationOfMultipleFiles -> {
                showMultipleCombinedFiles()
                bindRepostedOriginalCombinationOfMultipleFilesViewHolder(originalPost, files)
            }

            else -> hideAllRepostMediaViews(itemView)
        }

        ivQuotedPostImage?.let { imageView ->
            handleOriginalPostThumbnails(originalPost.thumbnail, imageView)
        }
    }


    private fun showImageContainer() {
        try {
            hideAllRepostMediaViews(itemView)
            // Show the main container that holds the RecyclerView
            binding.mixedFilesCardView.visibility = View.VISIBLE
            Log.d(TAG, "Reposted Image Container shown")
        } catch (e: Exception) {
            Log.e(TAG, "Error Showing Reposted Image Container: ${e.message}", e)
        }
    }

    private fun showVideoContainer() {
        try {
            hideAllRepostMediaViews(itemView)
            // Show the main container that holds the RecyclerView
            binding.mixedFilesCardView.visibility = View.VISIBLE
            Log.d(TAG, "Reposted Videos Container shown")
        } catch (e: Exception) {
            Log.e(TAG, "Error Showing Reposted Videos Container: ${e.message}", e)
        }
    }

    private fun showAudioContainer() {
        try {
            hideAllRepostMediaViews(itemView)
            // Show the main container that holds the RecyclerView
            binding.mixedFilesCardView.visibility = View.VISIBLE
            Log.d(TAG, "Reposted Audios Container shown")
        } catch (e: Exception) {
            Log.e(TAG, "Error Showing Reposted Audios Container: ${e.message}", e)
        }
    }

    private fun showDocumentContainer() {
        try {
            hideAllRepostMediaViews(itemView)
            // Show the main container that holds the RecyclerView
            binding.mixedFilesCardView.visibility = View.VISIBLE
            Log.d(TAG, "Reposted Documents Container shown")
        } catch (e: Exception) {
            Log.e(TAG, "Error Showing Reposted Documents Container: ${e.message}", e)
        }
    }

    private fun showMultipleCombinedFiles() {
        try {
            hideAllRepostMediaViews(itemView)
            // Show the main container that holds the RecyclerView
            binding.mixedFilesCardView.visibility = View.VISIBLE
            Log.d(TAG, "Reposted Multiple Files Container shown")
        } catch (e: Exception) {
            Log.e(TAG, "Error Showing Reposted Multiple Files Container: ${e.message}", e)
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun bindRepostedImagesOnlyViewHolder(
        post: Post,
        files: ArrayList<File>) {
        try {
            Log.d(TAG, "bind Images Only ViewHolder called with ${files.size} files")
            binding.mixedFilesCardView.visibility = View.VISIBLE

            // For Post type, get the original post for the adapter
            val originalPost = post.originalPost.firstOrNull()
            if (originalPost != null) {
                val urls = files.map { file ->
                    val url = file.url
                    Log.d(TAG, "File URL: $url")
                    url
                }

                Log.d(TAG, "Creating adapter with ${urls.size} URLs for Post")

                val adapter = FeedRepostViewFileAdapter(urls, originalPost)

                // Use binding.recyclerView instead of recyclerViews (same as working OriginalPost methods)
                setupCleanRecyclerView(files.size, adapter, binding.recyclerView)

                Log.d(TAG, "RecyclerView adapter set successfully for Post")


                binding.recyclerView.post {
                    adapter.notifyDataSetChanged()
                    Log.d(TAG, "RecyclerView notify Data Set Changed called for Post")
                }
            } else {
                Log.w(TAG, "No original post found for Images Only File view")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error binding Images Only Files view holder: ${e.message}", e)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun bindRepostedVideosOnlyViewHolder(
        post: Post,
        files: ArrayList<File>) {
        try {
            Log.d(TAG, "bind Videos Only ViewHolder called with ${files.size} files")
            binding.mixedFilesCardView.visibility = View.VISIBLE

            // For Post type, get the original post for the adapter
            val originalPost = post.originalPost.firstOrNull()
            if (originalPost != null) {
                val urls = files.map { file ->
                    val url = file.url
                    Log.d(TAG, "File URL: $url")
                    url
                }

                Log.d(TAG, "Creating adapter with ${urls.size} URLs for Post")

                val adapter = FeedRepostViewFileAdapter(urls, originalPost)

                // Use binding.recyclerView instead of recyclerViews
                setupCleanRecyclerView(files.size, adapter, binding.recyclerView)

                Log.d(TAG, "RecyclerView adapter set successfully for Post")

                // Force refresh
                binding.recyclerView.post {
                    adapter.notifyDataSetChanged()
                    Log.d(TAG, "RecyclerView notify Data Set Changed called for Post")
                }
            } else {
                Log.w(TAG, "No original post found for Videos Only File view")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error binding Videos Only Files view holder: ${e.message}", e)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun bindRepostedAudiosOnlyViewHolder(
        post: Post,
        files: ArrayList<File>) {
        try {
            Log.d(TAG, "bind Audios Only ViewHolder called with ${files.size} files")
            binding.mixedFilesCardView.visibility = View.VISIBLE

            // For Post type, get the original post for the adapter
            val originalPost = post.originalPost.firstOrNull()
            if (originalPost != null) {
                val urls = files.map { file ->
                    val url = file.url
                    Log.d(TAG, "File URL: $url")
                    url
                }

                Log.d(TAG, "Creating adapter with ${urls.size} URLs for Post")

                val adapter = FeedRepostViewFileAdapter(urls, originalPost)

                // Use binding.recyclerView instead of recyclerViews
                setupCleanRecyclerView(files.size, adapter, binding.recyclerView)

                Log.d(TAG, "RecyclerView adapter set successfully for Post")

                // Force refresh
                binding.recyclerView.post {
                    adapter.notifyDataSetChanged()
                    Log.d(TAG, "RecyclerView notify Data Set Changed called for Post")
                }
            } else {
                Log.w(TAG, "No original post found for Audios Only File view")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error binding Audios Only Files view holder: ${e.message}", e)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun bindRepostedDocumentsOnlyViewHolder(
        post: Post,
        files: ArrayList<File>) {
        try {
            Log.d(TAG, "bind Documents Only ViewHolder called with ${files.size} files")
            binding.mixedFilesCardView.visibility = View.VISIBLE

            // For Post type, get the original post for the adapter
            val originalPost = post.originalPost.firstOrNull()
            if (originalPost != null) {
                val urls = files.map { file ->
                    val url = file.url
                    Log.d(TAG, "File URL: $url")
                    url
                }

                Log.d(TAG, "Creating adapter with ${urls.size} URLs for Post")

                val adapter = FeedRepostViewFileAdapter(urls, originalPost)

                // Use binding.recyclerView instead of recyclerViews
                setupCleanRecyclerView(files.size, adapter, binding.recyclerView)

                Log.d(TAG, "RecyclerView adapter set successfully for Post")

                // Force refresh
                binding.recyclerView.post {
                    adapter.notifyDataSetChanged()
                    Log.d(TAG, "RecyclerView notify Data Set Changed called for Post")
                }
            } else {
                Log.w(TAG, "No original post found for Documents Only File view")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error binding Documents Only Files view holder: ${e.message}", e)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun bindRepostedCombinationOfMultipleFilesViewHolder(
        post: Post,
        files: ArrayList<File>) {

        try {
            Log.d(TAG, "bindCombinationViewHolder called with ${files.size} files")

            if (::recyclerViews.isInitialized) {
                // For Post type, get the original post for the adapter
                val originalPost = post.originalPost.firstOrNull()
                if (originalPost != null) {
                    val urls = files.map { file ->
                        val url = file.url
                        Log.d(TAG, "File URL: $url")
                        url
                    }

                    Log.d(TAG, "Creating adapter with ${urls.size} URLs for Post")

                    val adapter = FeedRepostViewFileAdapter(urls, originalPost)

                    // Use the clean setup function with proper layout management
                    setupCleanRecyclerView(files.size, adapter, recyclerViews)

                    Log.d(TAG, "RecyclerView adapter set successfully for Post")

                    // Force refresh
                    recyclerViews.post {
                        adapter.notifyDataSetChanged()
                        Log.d(TAG, "RecyclerView notify Data Set Changed called for Post")
                    }
                } else {
                    Log.w(TAG, "No original post found for Combination of Multiple File view")
                }
            } else {
                Log.e(TAG, "recyclerViews not initialized")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error binding Combination of Multiple Files view holder: ${e.message}", e)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun bindRepostedOriginalImagesOnlyViewHolder(post: OriginalPost, files: List<File>) {
        try {
            Log.d(TAG, "bindOriginal Images Only ViewHolder called with ${files.size} files")
            binding.mixedFilesCardView.visibility = View.VISIBLE

            // Extract URLs and log them
            val urls = files.map { file ->
                val url = file.url
                Log.d(TAG, "File URL: $url")
                url
            }

            Log.d(TAG, "Creating adapter with ${urls.size} URLs")

            // Create adapter
            val adapter = FeedRepostViewFileAdapter(urls, post)

            // Use the clean setup function with proper layout management
            setupCleanRecyclerView(files.size, adapter, binding.recyclerView)

            Log.d(TAG, "RecyclerView adapter set successfully")

            // Force RecyclerView to measure and layout
            binding.recyclerView.post {
                adapter.notifyDataSetChanged()
                Log.d(TAG, "RecyclerView notify Data Set Changed called")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error binding original Images Only  File view holder: ${e.message}", e)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun bindRepostedOriginalVideosOnlyViewHolder(post: OriginalPost, files: List<File>) {
        try {
            Log.d(TAG, "bindOriginal Videos Only ViewHolder called with ${files.size} files")
            binding.mixedFilesCardView.visibility = View.VISIBLE

            // Extract URLs and log them
            val urls = files.map { file ->
                val url = file.url
                Log.d(TAG, "File URL: $url")
                url
            }

            Log.d(TAG, "Creating adapter with ${urls.size} URLs")

            // Create adapter
            val adapter = FeedRepostViewFileAdapter(urls, post)

            // Use the clean setup function with proper layout management
            setupCleanRecyclerView(files.size, adapter, binding.recyclerView)

            Log.d(TAG, "RecyclerView adapter set successfully")

            // Force RecyclerView to measure and layout
            binding.recyclerView.post {
                adapter.notifyDataSetChanged()
                Log.d(TAG, "RecyclerView notify Data Set Changed called")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error binding original Videos Only File view holder: ${e.message}", e)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun bindRepostedOriginalAudiosOnlyViewHolder(post: OriginalPost, files: List<File>) {
        try {
            Log.d(TAG, "bindOriginal Audios Only ViewHolder called with ${files.size} files")
            binding.mixedFilesCardView.visibility = View.VISIBLE

            // Extract URLs and log them
            val urls = files.map { file ->
                val url = file.url
                Log.d(TAG, "File URL: $url")
                url
            }

            Log.d(TAG, "Creating adapter with ${urls.size} URLs")

            // Create adapter
            val adapter = FeedRepostViewFileAdapter(urls, post)

            // Use the clean setup function with proper layout management
            setupCleanRecyclerView(files.size, adapter, binding.recyclerView)

            Log.d(TAG, "RecyclerView adapter set successfully")

            // Force RecyclerView to measure and layout
            binding.recyclerView.post {
                adapter.notifyDataSetChanged()
                Log.d(TAG, "RecyclerView notify Data Set Changed called")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error binding original Audios Only  File view holder: ${e.message}", e)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun bindRepostedOriginalDocumentsOnlyViewHolder(post: OriginalPost, files: List<File>) {

        try {
            Log.d(TAG, "bindOriginal Documents Only ViewHolder called with ${files.size} files")
            binding.mixedFilesCardView.visibility = View.VISIBLE

            // Extract URLs and log them
            val urls = files.map { file ->
                val url = file.url
                Log.d(TAG, "File URL: $url")
                url
            }

            Log.d(TAG, "Creating adapter with ${urls.size} URLs")

            // Create adapter
            val adapter = FeedRepostViewFileAdapter(urls, post)

            // Use the clean setup function with proper layout management
            setupCleanRecyclerView(files.size, adapter, binding.recyclerView)

            Log.d(TAG, "RecyclerView adapter set successfully")

            // Force RecyclerView to measure and layout
            binding.recyclerView.post {
                adapter.notifyDataSetChanged()
                Log.d(TAG, "RecyclerView notify Data Set Changed called")
            }

        } catch (e: Exception) {
            Log.e(
                TAG,
                "Error binding original Documents Only File view holder: ${e.message}",
                e
            )
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun bindRepostedOriginalCombinationOfMultipleFilesViewHolder(post: OriginalPost, files: List<File>) {
        try {
            Log.d(TAG, "bindOriginalCombinationViewHolder called with ${files.size} files")
            binding.mixedFilesCardView.visibility = View.VISIBLE

            // Extract URLs and log them
            val urls = files.map { file ->
                val url = file.url
                Log.d(TAG, "File URL: $url")
                url
            }

            Log.d(TAG, "Creating adapter with ${urls.size} URLs")

            // Create adapter
            val adapter = FeedRepostViewFileAdapter(urls, post)

            // Use the clean setup function with proper layout management
            setupCleanRecyclerView(files.size, adapter, binding.recyclerView)

            Log.d(TAG, "RecyclerView adapter set successfully")

            // Force RecyclerView to measure and layout
            binding.recyclerView.post {
                adapter.notifyDataSetChanged()
                Log.d(TAG, "RecyclerView notify Data Set Changed called")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error binding original Combination of Multiple File view holder: ${e.message}", e)
        }
    }

    private fun setupCleanRecyclerView(fileCount: Int, adapter: FeedRepostViewFileAdapter, recyclerView: RecyclerView) {

        recyclerView?.let { recyclerView ->
            recyclerView.visibility = View.VISIBLE

            val effectiveFileCount = if (fileCount == 3) {
                val documentCount = originalPost?.fileTypes?.count { isDocument(it.fileType) }
                if (documentCount == 2) {
                    2 // Treat as 2-file layout when we have 3 files with 2 documents
                } else {
                    3 // Normal 3-file layout
                }
            } else {
                fileCount
            }


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


    private fun isDocument(mimeType: String): Boolean {
        return mimeType.contains("pdf") || mimeType.contains("docx") ||
                mimeType.contains("pptx") || mimeType.contains("xlsx") ||
                mimeType.contains("ppt") || mimeType.contains("xls") ||
                mimeType.contains("txt") || mimeType.contains("rtf") ||
                mimeType.contains("odt") || mimeType.contains("csv")
    }

    private fun hideAllRepostMediaViews(itemView: View) {
        try {
            // Hide all containers individually
            if (::originalFeedImages.isInitialized) {
                originalFeedImages.visibility = View.GONE
            }
            if (::originalFeedImage.isInitialized) {
                originalFeedImage.visibility = View.GONE
            }
            if (::videoContainer.isInitialized) {
                videoContainer.visibility = View.GONE
            }
            if (::multipleAudiosContainers.isInitialized) {
                multipleAudiosContainers.visibility = View.GONE
            }
            if (::multipleAudiosContainer.isInitialized) {
                multipleAudiosContainer.visibility = View.GONE
            }
            if (::mixedFilesCardViews.isInitialized) {
                mixedFilesCardViews.visibility = View.GONE
            }
            if (::mixedFilesCardView.isInitialized) {
                mixedFilesCardView.visibility = View.GONE
            }
            if (::recyclerViews.isInitialized) {
                recyclerViews.visibility = View.GONE
            }
            if (::multipleMediaContainer.isInitialized) {
                multipleMediaContainer.visibility = View.GONE
            }

            Log.d(TAG, "All media views hidden")
        } catch (e: Exception) {
            Log.e(TAG, "Error hiding media views: ${e.message}", e)
        }
    }

    private fun <T> handleMediaFiles(
        item: T,
        files: List<File>,
        fileTypes: List<FileType> = emptyList(),
        itemView: View,
        ivQuotedPostImage: ImageView?,
        bindRepostViewHolder: (MediaType, View, T, File) -> Unit,
        bindOriginalViewHolder: (MediaType, View, T, File) -> Unit,
        hideViews: (View) -> Unit

    ) {
        if (files.isNullOrEmpty()) {
            Log.d(TAG, "Files Empty")
            hideViews(itemView)
            return
        }

        try {
            Log.d(TAG, "Processing ${item?.javaClass?.simpleName} with ${files.size} files")

            // Count file types
            var imageCount = 0
            var audioCount = 0
            var videoCount = 0
            var documentCount = 0

            files.forEach { file ->
                val fileId = file.fileId
                Log.d(
                    TAG,
                    "Checking if file $fileId is image - fileTypes: ${fileTypes.find { it.fileId == fileId }?.fileType}"
                )

                when {
                    isImageFile(file, fileTypes) -> {
                        imageCount++
                        Log.d(TAG, "File $fileId detected as Image")
                    }

                    isAudioFile(file, fileTypes) -> {
                        audioCount++
                        Log.d(TAG, "File $fileId detected as audio")
                    }

                    isVideoFile(file, fileTypes) -> {
                        videoCount++
                        Log.d(TAG, "File $fileId detected as video via fileTypes")
                    }

                    isDocumentFile(file, fileTypes) -> {
                        documentCount++
                        Log.d(TAG, "File $fileId detected as document")
                    }

                    else -> {
                        Log.d(TAG, "File $fileId NOT detected as any known type")
                    }
                }
            }

            Log.d(
                TAG,
                "File counts - Images: $imageCount, Audio: $audioCount, Videos: $videoCount, Documents: $documentCount"
            )

            // Determine media type based on counts
            val mediaType = when {
                listOf(
                    imageCount,
                    audioCount,
                    videoCount,
                    documentCount
                ).count { it > 0 } > 1 -> MediaType.CombinationOfMultipleFiles

                imageCount > 0 -> MediaType.Image
                audioCount > 0 -> MediaType.Audio
                videoCount > 0 -> MediaType.Video
                documentCount > 0 -> MediaType.Document
                else -> MediaType.Unknown
            }

            Log.d(TAG, "Final media type: $mediaType")

            // Show appropriate container
            when (mediaType) {
                MediaType.Image -> {
                    Log.d(TAG, "Showing Reposted Image Container")
                    showImageContainer()
                }

                MediaType.Audio -> {
                    Log.d(TAG, "Showing Reposted Audio Container")
                    showAudioContainer()
                }

                MediaType.Video -> {
                    Log.d(TAG, "Showing Reposted Video Container")
                    showVideoContainer()
                }

                MediaType.Document -> {
                    Log.d(TAG, "Showing Reposted Document Container")
                    showDocumentContainer()
                }

                MediaType.CombinationOfMultipleFiles -> {
                    Log.d(TAG, "Showing Reposted Multiple Combined Files Container")
                    showMultipleCombinedFiles()
                }

                else -> {
                    Log.d(TAG, "Unknown media type, hiding all views")
                    hideViews(itemView)
                }
            }

            // Call bind methods with proper logging
            if (mediaType != MediaType.Unknown && files.isNotEmpty()) {
                val firstFile = files.first()
                when (item) {
                    is Post -> {
                        Log.d(TAG, "Calling bindRepostViewHolder for Post")
                        bindRepostViewHolder(mediaType, itemView, item, firstFile)
                    }

                    is OriginalPost -> {
                        Log.d(TAG, "Calling bindOriginalViewHolder for OriginalPost")
                        bindOriginalViewHolder(mediaType, itemView, item, firstFile)
                    }

                    else -> {
                        Log.w(TAG, "Unknown item type: ${item?.javaClass?.simpleName}")
                    }
                }
            }

            // Handle thumbnails
            ivQuotedPostImage?.let { imageView ->
                val thumbnails = when (item) {
                    is Post -> item.thumbnail
                    is OriginalPost -> item.thumbnail
                    else -> null
                }
                handleThumbnails(thumbnails as List<Thumbnail>?, imageView)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in handleMediaFiles: ${e.message}", e)
            hideViews(itemView)
        }
    }


    private fun bindRepostViewHolder(mediaType: MediaType, itemView: View, item: Post, file: File) {
        try {
            Log.d(TAG, "Binding repost view holder for media type: $mediaType")

            when (mediaType) {
                MediaType.Image -> bindRepostedImagesOnlyViewHolder(
                    item,
                    item.files
                )

                MediaType.Video -> bindRepostedVideosOnlyViewHolder(
                    item,
                    item.files
                )

                MediaType.Audio -> bindRepostedAudiosOnlyViewHolder(
                    item,
                    item.files
                )

                MediaType.Document -> bindRepostedDocumentsOnlyViewHolder(
                    item,
                    item.files
                )

                MediaType.CombinationOfMultipleFiles -> bindRepostedCombinationOfMultipleFilesViewHolder(
                    item,
                    item.files
                )

                MediaType.Unknown -> Log.d(TAG, "Unknown media type for repost")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error binding repost view holder: ${e.message}", e)
        }
    }

    private fun bindOriginalViewHolder(
        mediaType: MediaType,
        itemView: View,
        item: OriginalPost,
        file: File

    ) {
        try {
            Log.d(TAG, "Binding original view holder for media type: $mediaType")

            when (mediaType) {
                MediaType.Image -> bindRepostedOriginalImagesOnlyViewHolder(item,  item.files)
                MediaType.Video -> bindRepostedOriginalVideosOnlyViewHolder(item,  item.files)
                MediaType.Audio -> bindRepostedOriginalAudiosOnlyViewHolder(item, item.files)
                MediaType.Document -> bindRepostedOriginalDocumentsOnlyViewHolder(item, item.files)
                MediaType.CombinationOfMultipleFiles -> bindRepostedOriginalCombinationOfMultipleFilesViewHolder(
                    item,
                    item.files
                )

                MediaType.Unknown -> Log.d(TAG, "Unknown media type for original post")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error binding original view holder: ${e.message}", e)
        }
    }

    private fun populateInteractionData(post: OriginalPost) {
        try {
            if (::likesCount.isInitialized) {
                likesCount.text = formatCount(post.likeCount)
            }
            if (::commentCount.isInitialized) {
                commentCount.text = formatCount(post.commentCount)
            }
            if (::repostCount.isInitialized) {
                repostCount.text = formatCount(post.repostCount)
            }
            if (::favoriteCounts.isInitialized) {
                favoriteCounts.text = formatCount(post.bookmarkCount)
            }
            if (::shareCount.isInitialized) {
                shareCount.text = "0"
            }

            updateInteractionStates(post)
        } catch (e: Exception) {
            Log.e(TAG, "Error populating interaction data: ${e.message}", e)
        }
    }

    private fun handleThumbnails(thumbnails: List<Thumbnail>?, imageView: ImageView) {
        try {
            thumbnails?.firstOrNull()?.thumbnailUrl?.takeIf { it.isNotEmpty() }
                ?.let { thumbnailUrl ->
                    if (::mixedFilesCardView.isInitialized && mixedFilesCardView.visibility == View.VISIBLE) {
                        loadImage(thumbnailUrl, imageView)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling thumbnails: ${e.message}", e)
        }
    }

    private fun loadImage(url: String?, imageView: ImageView) {
        try {
            if (isAdded) {
                Glide.with(imageView.context)
                    .load(url)
                    .placeholder(R.drawable.imageplaceholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading image: ${e.message}", e)
        }
    }


    sealed class MediaType {
        object Image : MediaType()
        object Video : MediaType()
        object Audio : MediaType()
        object Document : MediaType()
        object CombinationOfMultipleFiles : MediaType()
        object Unknown : MediaType()
    }

    private fun getMediaType(
        file: File?,
        fileTypes: List<FileType> = emptyList()
    ): MediaType {
        if (file == null) {
            Log.d(TAG, "File is null, returning MediaType Unknown")
            return MediaType.Unknown
        }

        Log.d(
            TAG,
            "File details: fileId=${file.fileId}, mimeType=${file.mimeType}, url=${file.url}"
        )

        // First check if we have fileTypes list and find matching fileType for this file
        val matchingFileType = fileTypes.find { it.fileId == file.fileId }
        matchingFileType?.let { fileTypeObj ->
            when (fileTypeObj.fileType?.lowercase()) {
                "video" -> {
                    Log.d(TAG, "Detected video via fileTypes")
                    return MediaType.Video
                }

                "pdf" -> {
                    Log.d(TAG, "Detected pdf via fileTypes")
                    return MediaType.Document
                }

                "image" -> {
                    Log.d(TAG, "Detected image via fileTypes")
                    return MediaType.Image
                }

                "audio" -> {
                    Log.d(TAG, "Detected audio via fileTypes")
                    return MediaType.Audio
                }

                "mixed_files" -> {
                    Log.d(TAG, "Detected mixed_files via fileTypes")
                    return MediaType.CombinationOfMultipleFiles
                }
            }
        }

        // Check file extension from fileId or URL
        val extension = file.fileId.substringAfterLast(".").lowercase()
            .takeIf { it != file.fileId } // Only use if there was actually a dot
            ?: file.url.substringAfterLast(".").substringBefore("?").lowercase()
                .takeIf { it != file.url.substringBefore("?") } // Only use if there was actually a dot

        when (extension) {
            "mp4", "mpeg", "mpe", "mpg", "avi", "mov", "wmv", "flv", "webm", "mkv" -> {
                Log.d(TAG, "Detected video via extension: $extension")
                return MediaType.Video
            }

            "pdf", "pdg", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "rtf", "odt", "csv" -> {
                Log.d(TAG, "Detected document via extension: $extension")
                return MediaType.Document
            }

            "jpg", "jpeg", "png", "gif", "bmp", "webp" -> {
                Log.d(TAG, "Detected image via extension: $extension")
                return MediaType.Image
            }

            "mp3", "wav", "ogg", "m4a", "aac", "flac" -> {
                Log.d(TAG, "Detected audio via extension: $extension")
                return MediaType.Audio
            }
        }

        // Check MIME type
        when {
            file.mimeType?.startsWith("image/", ignoreCase = true) == true -> {
                Log.d(TAG, "Detected image via mimeType: ${file.mimeType}")
                return MediaType.Image
            }

            file.mimeType?.startsWith("video/", ignoreCase = true) == true -> {
                Log.d(TAG, "Detected video via mimeType: ${file.mimeType}")
                return MediaType.Video
            }

            file.mimeType?.startsWith("audio/", ignoreCase = true) == true -> {
                Log.d(TAG, "Detected audio via mimeType: ${file.mimeType}")
                return MediaType.Audio
            }

            file.mimeType?.startsWith("application/", ignoreCase = true) == true -> {
                Log.d(TAG, "Detected document via mimeType: ${file.mimeType}")
                return MediaType.Document
            }

            file.mimeType == "mixed_files" -> {
                Log.d(TAG, "Detected mixed_files via mimeType")
                return MediaType.CombinationOfMultipleFiles
            }
        }

        Log.d(TAG, "No type detected, returning MediaType.Unknown")
        return MediaType.Unknown
    }


    private fun isImageFile(file: File, fileTypes: List<FileType>?): Boolean {
        Log.d(
            TAG,
            "Checking if file ${file.fileId} is image - fileTypes: ${fileTypes?.find { it.fileId == file.fileId }?.fileType}"
        )

        // First check fileTypes array (most reliable)
        val matchingFileType = fileTypes?.find { it.fileId == file.fileId }
        if (matchingFileType?.fileType?.contains("image", ignoreCase = true) == true) {
            Log.d(TAG, "File ${file.fileId} detected as Image via fileTypes")
            return true
        }

        // Check file extension from URL
        val url = file.url.lowercase()
        if (url.matches(".*\\.(jpg|jpeg|png|gif|bmp|webp|svg|tiff|ico)(\\?.*)?$".toRegex())) {
            Log.d(TAG, "File ${file.fileId} detected as Image via URL extension")
            return true
        }

        // Check localPath for extension
        val localPath = file.localPath.lowercase()
        if (localPath.matches(".*\\.(jpg|jpeg|png|gif|bmp|webp|svg|tiff|ico)$".toRegex())) {
            Log.d(TAG, "File ${file.fileId} detected as Image via localPath extension")
            return true
        }

        // Fallback check with mimeType only
        val mimeType = file.mimeType?.lowercase()

        return when {
            mimeType?.startsWith("image") == true -> {
                Log.d(TAG, "File ${file.fileId} detected as Image via mimeType")
                true
            }
            else -> {
                Log.d(TAG, "File ${file.fileId} NOT detected as Image")
                false
            }
        }
    }

    private fun isAudioFile(file: File, fileTypes: List<FileType>?): Boolean {
        Log.d(
            TAG,
            "Checking if file ${file.fileId} is audio - fileTypes: ${fileTypes?.find { it.fileId == file.fileId }?.fileType}"
        )

        // First check fileTypes array (most reliable)
        val matchingFileType = fileTypes?.find { it.fileId == file.fileId }
        if (matchingFileType?.fileType?.contains("audio", ignoreCase = true) == true) {
            Log.d(TAG, "File ${file.fileId} detected as audio via fileTypes")
            return true
        }

        // Check file extension from URL
        val url = file.url.lowercase()
        if (url.matches(".*\\.(mp3|wav|ogg|m4a|aac|flac|wma|opus|amr)(\\?.*)?$".toRegex())) {
            Log.d(TAG, "File ${file.fileId} detected as audio via URL extension")
            return true
        }

        // Check localPath for extension
        val localPath = file.localPath.lowercase()
        if (localPath.matches(".*\\.(mp3|wav|ogg|m4a|aac|flac|wma|opus|amr)$".toRegex())) {
            Log.d(TAG, "File ${file.fileId} detected as audio via localPath extension")
            return true
        }

        // Fallback check with mimeType only
        val mimeType = file.mimeType?.lowercase()

        return when {
            mimeType?.startsWith("audio") == true -> {
                Log.d(TAG, "File ${file.fileId} detected as audio via mimeType")
                true
            }
            else -> {
                Log.d(TAG, "File ${file.fileId} NOT detected as audio")
                false
            }
        }
    }

    private fun isVideoFile(file: File, fileTypes: List<FileType>?): Boolean {
        Log.d(
            TAG,
            "Checking if file ${file.fileId} is video - fileTypes: ${fileTypes?.find { it.fileId == file.fileId }?.fileType}"
        )

        // First check fileTypes array (most reliable)
        val matchingFileType = fileTypes?.find { it.fileId == file.fileId }
        if (matchingFileType?.fileType?.contains("video", ignoreCase = true) == true) {
            Log.d(TAG, "File ${file.fileId} detected as video via fileTypes")
            return true
        }

        // Check file extension from URL
        val url = file.url.lowercase()
        if (url.matches(".*\\.(mp4|avi|mov|wmv|flv|webm|mkv|m4v|3gp|ogv)(\\?.*)?$".toRegex())) {
            Log.d(TAG, "File ${file.fileId} detected as video via URL extension")
            return true
        }

        // Check localPath for extension
        val localPath = file.localPath.lowercase()
        if (localPath.matches(".*\\.(mp4|avi|mov|wmv|flv|webm|mkv|m4v|3gp|ogv)$".toRegex())) {
            Log.d(TAG, "File ${file.fileId} detected as video via localPath extension")
            return true
        }

        // Fallback check with mimeType only
        val mimeType = file.mimeType?.lowercase()

        return when {
            mimeType?.startsWith("video") == true -> {
                Log.d(TAG, "File ${file.fileId} detected as video via mimeType")
                true
            }
            else -> {
                Log.d(TAG, "File ${file.fileId} NOT detected as video")
                false
            }
        }
    }

    private fun isDocumentFile(file: File, fileTypes: List<FileType>?): Boolean {
        Log.d(
            TAG,
            "Checking if file ${file.fileId} is document - fileTypes: ${fileTypes?.find { it.fileId == file.fileId }?.fileType}"
        )

        // First check fileTypes array
        val matchingFileType = fileTypes?.find { it.fileId == file.fileId }
        val fileTypeStr = matchingFileType?.fileType?.lowercase()
        if (fileTypeStr?.contains("pdf") == true ||
            fileTypeStr?.contains("doc") == true ||
            fileTypeStr?.contains("document") == true
        ) {
            Log.d(TAG, "File ${file.fileId} detected as document via fileTypes")
            return true
        }

        // Check file extension from URL
        val url = file.url.lowercase()
        if (url.matches(".*\\.(pdf|doc|docx|xls|xlsx|ppt|pptx|txt|rtf|odt|csv|pages|numbers|key)(\\?.*)?$".toRegex())) {
            Log.d(TAG, "File ${file.fileId} detected as document via URL extension")
            return true
        }

        // Check localPath for extension
        val localPath = file.localPath.lowercase()
        if (localPath.matches(".*\\.(pdf|doc|docx|xls|xlsx|ppt|pptx|txt|rtf|odt|csv|pages|numbers|key)$".toRegex())) {
            Log.d(TAG, "File ${file.fileId} detected as document via localPath extension")
            return true
        }

        // Fallback checks with mimeType only
        val mimeType = file.mimeType?.lowercase()

        return when {
            mimeType?.contains("pdf") == true -> true
            mimeType?.contains("msword") == true -> true
            mimeType?.contains("wordprocessingml") == true -> true
            mimeType?.contains("ms-excel") == true -> true
            mimeType?.contains("spreadsheetml") == true -> true
            mimeType?.contains("ms-powerpoint") == true -> true
            mimeType?.contains("presentationml") == true -> true
            else -> {
                Log.d(TAG, "File ${file.fileId} NOT detected as document")
                false
            }
        }
    }

    private fun isCombinationOfMultipleFile(file: File, fileTypes: List<FileType>?): Boolean {
        val matchingFileType = fileTypes?.find { it.fileId == file.fileId }
        if (matchingFileType?.fileType?.contains("mixed_files", ignoreCase = true) == true) {
            return true
        }

        val mimeType = file.mimeType?.lowercase()

        return when {
            mimeType?.contains("mixed_files") == true -> true
            else -> false
        }
    }

    private fun handleRepostMediaFiles(post: Post, itemView: View, ivQuotedPostImage: ImageView?) {
        val originalPost = post.originalPost?.firstOrNull() ?: return
        Log.d(
            TAG,
            "Processing Repost with original Post Containing ${originalPost.files?.size ?: 0} files"
        )

        // Filter files by type
        val categorizedFiles = categorizeFiles(originalPost)
        logFileCounts(categorizedFiles)

        val isCombinationOfMultipleFiles = categorizedFiles.getTypeCount() > 1

        val finalMediaType = when {
            categorizedFiles.getTypeCount() > 1 -> MediaType.CombinationOfMultipleFiles
            categorizedFiles.imageFiles.isNotEmpty() -> MediaType.Image
            categorizedFiles.audioFiles.isNotEmpty() -> MediaType.Audio
            categorizedFiles.videoFiles.isNotEmpty() -> MediaType.Video
            categorizedFiles.documentFiles.isNotEmpty() -> MediaType.Document
            else -> MediaType.CombinationOfMultipleFiles
        }

        Log.d(TAG, "Final media type: $finalMediaType")


        when (finalMediaType) {
            MediaType.Audio -> showAudioContainer()
            MediaType.Video -> showVideoContainer()
            MediaType.Image -> showImageContainer()
            MediaType.Document -> showDocumentContainer()
            MediaType.CombinationOfMultipleFiles -> showMultipleCombinedFiles()
            else -> hideAllRepostMediaViews(itemView)
        }

        var files = originalPost.files

        when (finalMediaType) {
            MediaType.Audio -> {
                showAudioContainer()
                bindRepostedOriginalAudiosOnlyViewHolder(originalPost, files)
            }

            MediaType.Video -> {
                showVideoContainer()
                bindRepostedOriginalVideosOnlyViewHolder(originalPost, files)
            }

            MediaType.Image -> {
                showImageContainer()
                bindRepostedOriginalImagesOnlyViewHolder(originalPost, files)
            }

            MediaType.Document -> {
                showDocumentContainer()
                bindRepostedOriginalDocumentsOnlyViewHolder(originalPost, files)
            }

            MediaType.CombinationOfMultipleFiles -> {
                showMultipleCombinedFiles()
                bindRepostedOriginalCombinationOfMultipleFilesViewHolder(originalPost, files)
            }

            else -> hideAllRepostMediaViews(itemView)
        }


    }




    private data class CategorizedFiles(
        val imageFiles: List<File>,
        val audioFiles: List<File>,
        val videoFiles: List<File>,
        val documentFiles: List<File>,
        val combinationOfMultipleFiles: List<File>

    ) {

        fun getTypeCount(): Int = listOf(
            imageFiles.isNotEmpty(),
            audioFiles.isNotEmpty(),
            videoFiles.isNotEmpty(),
            documentFiles.isNotEmpty(),
            combinationOfMultipleFiles.isNotEmpty()
        ).count { it }

        fun getAllFiles(): List<File> = mutableListOf<File>().apply {
            addAll(imageFiles)
            addAll(videoFiles)
            addAll(audioFiles)
            addAll(documentFiles)
            addAll(combinationOfMultipleFiles)
        }
    }

    private fun categorizeFiles(originalPost: OriginalPost): CategorizedFiles {
        val files = originalPost.files ?: emptyList()
        val fileTypes = originalPost.fileTypes

        return CategorizedFiles(
            imageFiles = files.filter { isImageFile(it, fileTypes) },
            audioFiles = files.filter { isAudioFile(it, fileTypes) },
            videoFiles = files.filter { isVideoFile(it, fileTypes) },
            documentFiles = files.filter { isDocumentFile(it, fileTypes) },
            combinationOfMultipleFiles = files.filter { isCombinationOfMultipleFile(it, fileTypes) }
        )
    }

    private fun logFileCounts(categorizedFiles: CategorizedFiles) {
        with(categorizedFiles) {
            Log.d(
                TAG, "File counts - Images: ${imageFiles.size}, Audio: ${audioFiles.size}, " +
                        "Videos: ${videoFiles.size}, Documents: ${documentFiles.size}"
            )
        }
    }


    private val TAG = "Fragment_Original_Post_With_Repost_Inside"


    class FeedRepostViewFileAdapter(

        private val images: List<String>,
        private val feedPost: OriginalPost,


        ) :

        RecyclerView.Adapter<RecyclerView.ViewHolder>() {


        internal var onMultipleFilesClickListener: OnMultipleFilesClickListener? = null


        fun setOnMultipleFilesClickListener(listener: OnMultipleFilesClickListener) {
            onMultipleFilesClickListener = listener
        }

        interface OnMultipleFilesClickListener {
            fun multipleFileClickListener(
                position: Int, files: List<File>,
                fileIds: List<String>
            )
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            when (holder) {

                is FeedRepostImagesOnly -> {
                    holder.onBind(feedPost)
                }

                is FeedRepostAudiosOnly -> {
                    holder.onBind(feedPost)
                }

                is FeedRepostVideosOnly -> {
                    holder.onBind(feedPost)
                }

                is FeedRepostDocumentsOnly -> {
                    holder.onBind(feedPost)
                }

                is FeedRepostCombinationOfMultipleFiles -> {
                    holder.onBind(feedPost)
                }

            }
        }

        override fun getItemViewType(position: Int): Int {


            return when (feedPost.fileTypes[position].fileType) {

                "image" -> {
                    val VIEW_TYPE_IMAGE_FEED = 2
                    VIEW_TYPE_IMAGE_FEED
                }

                "audio" -> {
                    val VIEW_TYPE_AUDIO_FEED = 3
                    VIEW_TYPE_AUDIO_FEED
                }

                "video" -> {
                    val VIEW_TYPE_VIDEO_FEED = 4
                    VIEW_TYPE_VIDEO_FEED
                }

                "doc", "pdf" -> {
                    val VIEW_TYPE_DOCUMENT_FEED = 5
                    VIEW_TYPE_DOCUMENT_FEED
                }

                "image", "audio", "video", "doc", "pdf" -> {
                    val VIEW_TYPE_COMBINATION_OF_MULTIPLE_FILES = 1
                    VIEW_TYPE_COMBINATION_OF_MULTIPLE_FILES
                }

                else -> {

                    Log.d(TAG, "getItemViewType: unknown type")
                }

            }
        }


        private fun isDocument(mimeType: String): Boolean {
            return mimeType.contains("pdf") || mimeType.contains("docx") ||
                    mimeType.contains("pptx") || mimeType.contains("xlsx") ||
                    mimeType.contains("ppt") || mimeType.contains("xls") ||
                    mimeType.contains("txt") || mimeType.contains("rtf") ||
                    mimeType.contains("odt") || mimeType.contains("csv")
        }


        override fun getItemCount(): Int {
            val fileCount = feedPost.files.size

            // Special case: 3 files with 2 documents should only show 2 items
            if (fileCount == 3) {
                val documentCount = feedPost.fileTypes.count { isDocument(it.fileType) }
                if (documentCount == 2) {
                    return 2 // Only create 2 ViewHolders for the 2 documents
                }
            }

            // For all other cases
            return when (fileCount) {
                0 -> 0
                1 -> 1
                2 -> 2
                3 -> 3 // Normal 3-file case (when not 2 documents)
                4 -> 4
                5 -> 4 // Show only 4 with +1 count
                else -> 4 // Show only 4 with +N count
            }
        }





        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)

            val VIEW_TYPE_COMBINATION_OF_MULTIPLE_FILES = 1
            val VIEW_TYPE_IMAGE_FEED = 2
            val VIEW_TYPE_AUDIO_FEED = 3
            val VIEW_TYPE_VIDEO_FEED = 4
            val VIEW_TYPE_DOCUMENT_FEED = 5


            return when (viewType) {

                VIEW_TYPE_IMAGE_FEED -> {

                    val itemView = inflater.inflate(
                        R.layout.feed_multiple_images_only_view_item, parent, false
                    )
                    FeedRepostImagesOnly(itemView)
                }

                VIEW_TYPE_AUDIO_FEED -> {

                    val itemView = inflater.inflate(
                        R.layout.feed_multiple_audios_only_view_item, parent, false
                    )
                    FeedRepostAudiosOnly(itemView)
                }

                VIEW_TYPE_VIDEO_FEED -> {

                    val itemView = inflater.inflate(
                        R.layout.feed_multiple_videos_only_view_item, parent, false
                    )
                    FeedRepostVideosOnly(itemView)
                }

                VIEW_TYPE_DOCUMENT_FEED -> {

                    val itemView =
                        inflater.inflate(
                            R.layout.feed_multiple_documents_only_view_item, parent, false
                        )
                    FeedRepostDocumentsOnly(itemView)
                }

                VIEW_TYPE_COMBINATION_OF_MULTIPLE_FILES -> {

                    val itemView =
                        inflater.inflate(
                            R.layout.feed_multiple_combination_of_files_view_item, parent, false
                        )
                    FeedRepostCombinationOfMultipleFiles(itemView)
                }


                else -> throw IllegalArgumentException("Invalid view type")
            }
        }


        inner class FeedRepostImagesOnly(itemView: View) : RecyclerView.ViewHolder(itemView) {

            private val imageView: ImageView = itemView.findViewById(R.id.imageView)
            private val materialCardView: MaterialCardView =
                itemView.findViewById(R.id.materialCardView)
            private val countTextView: TextView = itemView.findViewById(R.id.textView)
            private val imageView2: ImageView = itemView.findViewById(R.id.imageView2)

            fun Int.dpToPx(context: Context): Int {
                return (this * context.resources.displayMetrics.density).toInt()
            }

            private fun getAdaptiveHeights(screenHeight: Int): Pair<Int, Int> {
                val minHeight = (screenHeight * 0.15).toInt()
                val maxHeight = (screenHeight * 0.4).toInt()
                return Pair(minHeight, maxHeight)
            }

            private fun getConstrainedHeight(
                desiredHeight: Int,
                minHeight: Int,
                maxHeight: Int
            ): Int {
                return desiredHeight.coerceIn(minHeight, maxHeight)
            }

            private fun getActivityFromContext(context: Context): AppCompatActivity? {
                return when (context) {
                    is AppCompatActivity -> context
                    is ContextWrapper -> getActivityFromContext(context.baseContext)
                    else -> null
                }
            }

            private fun navigateToTappedFilesFragment(
                context: Context,
                currentIndex: Int,
                files: List<File>,
                fileIds: List<String>
            ) {
                val activity = getActivityFromContext(context)
                if (activity != null) {
                    activity.findViewById<View>(R.id.topBar)?.visibility = View.GONE
                    activity.findViewById<View>(R.id.bottomNavigationView)?.visibility = View.GONE

                    val fragment = Tapped_Files_In_The_Container_View_Fragment()

                    val bundle = Bundle().apply {
                        putInt("current_index", currentIndex)
                        putInt("total_files", files.size)

                        val fileUrls = ArrayList<String>()
                        files.forEach { file -> fileUrls.add(file.url) }
                        putStringArrayList("file_urls", fileUrls)
                        putStringArrayList("file_ids", ArrayList(fileIds))

                        val postItems = ArrayList<PostItem>()
                        files.forEachIndexed { index, file ->
                            val postItem = PostItem(
                                audioUrl = file.url,
                                audioThumbnailUrl = null,
                                videoUrl = file.url,
                                videoThumbnailUrl = null,
                                postId = fileIds.getOrNull(index) ?: "file_$index",
                                data = "Post data for file $index",
                                files = arrayListOf(file.url)
                            )
                            postItems.add(postItem)
                        }
                        putParcelableArrayList("post_list", postItems)
                        putString(
                            "post_id",
                            fileIds.getOrNull(currentIndex) ?: "file_$currentIndex"
                        )
                    }

                    fragment.arguments = bundle

                    activity.supportFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left
                        )
                        .replace(R.id.frame_layout, fragment)
                        .addToBackStack("tapped_files_view")
                        .commit()

                    Log.d(
                        TAG, "Navigated to Tapped_Files_In_The_Container_View with " +
                                "${files.size} files, starting at index $currentIndex"
                    )
                } else {
                    Log.e(TAG, "Activity is null, cannot navigate to fragment")
                }
            }

            @SuppressLint("SetTextI18n")
            fun onBind(data: OriginalPost) {
                Log.d(TAG, "image feed $absoluteAdapterPosition item count $itemCount")

                val context = itemView.context
                val displayMetrics = context.resources.displayMetrics
                val screenWidth = displayMetrics.widthPixels
                val screenHeight = displayMetrics.heightPixels
                val SpaceBetweenRows = 2.dpToPx(context)

                val (minHeight, maxHeight) = getAdaptiveHeights(screenHeight)
                val availableWidth = ((screenWidth - SpaceBetweenRows * 1) / 2 * 0.95f).toInt()

                val fileIdToFind = data.fileIds[absoluteAdapterPosition]
                val file = data.files.find { it.fileId == fileIdToFind }
                val imageUrl = file?.url ?: data.files.getOrNull(absoluteAdapterPosition)?.url ?: ""

                val fileSize = itemCount
                Log.d(TAG, "image getItemCount: $fileSize $imageUrl")

                materialCardView.setCardBackgroundColor(Color.TRANSPARENT)
                materialCardView.cardElevation = 0f

                val clickListener = View.OnClickListener {
                    navigateToTappedFilesFragment(
                        context,
                        absoluteAdapterPosition,
                        data.files,
                        data.fileIds as List<String>
                    )

                    onMultipleFilesClickListener?.multipleFileClickListener(
                        absoluteAdapterPosition,
                        data.files,
                        data.fileIds as List<String>
                    )
                }

                itemView.setOnClickListener(clickListener)
                imageView.setOnClickListener(clickListener)
                materialCardView.setOnClickListener(clickListener)

                val layoutParams = if (materialCardView.layoutParams != null) {
                    materialCardView.layoutParams as ViewGroup.MarginLayoutParams
                } else {
                    ViewGroup.MarginLayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }

                imageView.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP

                when {

                    fileSize <= 1 -> {

                        layoutParams.width = screenWidth - (SpaceBetweenRows * 2)
                        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                        resetMargins(layoutParams)
                        layoutParams.leftMargin = SpaceBetweenRows
                        layoutParams.rightMargin = SpaceBetweenRows

                        imageView.layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        imageView.adjustViewBounds = true
                        imageView.scaleType = ImageView.ScaleType.FIT_CENTER

                        loadImage(context, imageUrl)

                    }

                    fileSize == 2 -> {
                        val desiredHeight = (maxHeight * 0.6).toInt()
                        layoutParams.width = availableWidth
                        layoutParams.height =
                            getConstrainedHeight(desiredHeight, minHeight, maxHeight)
                        resetMargins(layoutParams)

                        when (absoluteAdapterPosition) {
                            0 -> layoutParams.rightMargin = (SpaceBetweenRows / 2)
                            1 -> layoutParams.leftMargin = (SpaceBetweenRows / 2)
                        }

                        loadImage(context, imageUrl)
                    }

                    fileSize == 3 -> {
                        val largeImageHeight =
                            getConstrainedHeight((maxHeight * 0.6).toInt(), minHeight, maxHeight)
                        val smallImageHeight = largeImageHeight / 2

                        when (absoluteAdapterPosition) {
                            0 -> {
                                // IMAGE 0: Left side, full height
                                layoutParams.width = availableWidth
                                layoutParams.height = largeImageHeight

                                resetMargins(layoutParams)
                                layoutParams.rightMargin = (SpaceBetweenRows / 2)
                            }

                            1, 2 -> {
                                // IMAGES 1 & 2: Right stacked images, half the height of image 0
                                layoutParams.width = availableWidth
                                layoutParams.height = smallImageHeight

                                resetMargins(layoutParams)
                                layoutParams.leftMargin = (SpaceBetweenRows / 2)

                                if (absoluteAdapterPosition == 1) {
                                    layoutParams.bottomMargin = SpaceBetweenRows
                                } else {
                                    layoutParams.topMargin = SpaceBetweenRows
                                }
                            }
                        }

                        // Ensure items don’t span full width
                        if (layoutParams is StaggeredGridLayoutManager.LayoutParams) {
                            layoutParams.isFullSpan = false
                        }

                        loadImage(context, imageUrl)
                    }

                    fileSize == 4 -> {
                        val desiredHeight = (maxHeight * 0.6).toInt()
                        val uniformHeight =
                            getConstrainedHeight(desiredHeight, minHeight, maxHeight)

                        layoutParams.width = availableWidth
                        layoutParams.height = uniformHeight
                        resetMargins(layoutParams)

                        when (absoluteAdapterPosition) {
                            0 -> {
                                layoutParams.rightMargin = (SpaceBetweenRows / 2)
                                layoutParams.bottomMargin = SpaceBetweenRows
                            }

                            1 -> {
                                layoutParams.leftMargin = (SpaceBetweenRows / 2)
                                layoutParams.bottomMargin = SpaceBetweenRows
                            }

                            2 -> {
                                layoutParams.rightMargin = (SpaceBetweenRows / 2)
                                layoutParams.topMargin = SpaceBetweenRows
                            }

                            3 -> {
                                layoutParams.leftMargin = (SpaceBetweenRows / 2)
                                layoutParams.topMargin = SpaceBetweenRows
                            }
                        }

                        loadImage(context, imageUrl)
                    }

                    fileSize > 4 -> {
                        if (absoluteAdapterPosition >= 4) {
                            itemView.visibility = View.GONE
                            layoutParams.width = 0
                            layoutParams.height = 0
                            materialCardView.layoutParams = layoutParams
                            return
                        }

                        itemView.visibility = View.VISIBLE
                        val desiredHeight = (maxHeight * 0.6).toInt()
                        val uniformHeight =
                            getConstrainedHeight(desiredHeight, minHeight, maxHeight)

                        layoutParams.width = availableWidth
                        layoutParams.height = uniformHeight
                        resetMargins(layoutParams)

                        when (absoluteAdapterPosition) {
                            0 -> {
                                layoutParams.rightMargin = (SpaceBetweenRows / 2)
                                layoutParams.bottomMargin = SpaceBetweenRows
                            }

                            1 -> {
                                layoutParams.leftMargin = (SpaceBetweenRows / 2)
                                layoutParams.bottomMargin = SpaceBetweenRows
                            }

                            2 -> {
                                layoutParams.rightMargin = (SpaceBetweenRows / 2)
                                layoutParams.topMargin = SpaceBetweenRows
                            }

                            3 -> {
                                layoutParams.leftMargin = (SpaceBetweenRows / 2)
                                layoutParams.topMargin = SpaceBetweenRows
                            }
                        }

                        if (absoluteAdapterPosition == 3) {
                            countTextView.visibility = View.VISIBLE
                            countTextView.text = "+${fileSize - 4}"
                            countTextView.textSize = 32f
                            countTextView.setPadding(12, 4, 12, 4)

                            val background = GradientDrawable().apply {
                                shape = GradientDrawable.RECTANGLE
                                cornerRadius = 16f
                                setColor(Color.parseColor("#80000000"))
                            }
                            countTextView.background = background
                        } else {
                            countTextView.visibility = View.GONE
                            countTextView.setPadding(0, 0, 0, 0)
                            countTextView.background = null
                        }

                        loadImage(context, imageUrl)
                    }
                }

                materialCardView.layoutParams = layoutParams
            }

            private fun resetMargins(layoutParams: ViewGroup.MarginLayoutParams) {
                layoutParams.leftMargin = 0
                layoutParams.rightMargin = 0
                layoutParams.topMargin = 0
                layoutParams.bottomMargin = 0
            }

            private fun loadImage(context: Context, imageUrl: String) {
                Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.imageplaceholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView)
            }
        }


        inner class FeedRepostAudiosOnly(itemView: View) : RecyclerView.ViewHolder(itemView) {

            private val audioDurationTextView: TextView = itemView.findViewById(R.id.audioDuration)
            private val materialCardView: MaterialCardView =
                itemView.findViewById(R.id.materialCardView)
            private val artworkLayout: LinearLayout = itemView.findViewById(R.id.artworkLayout)
            private val countTextView: TextView = itemView.findViewById(R.id.textView)
            private val imageView2: ImageView = itemView.findViewById(R.id.imageView2)
            private val artworkImageView: ImageView = itemView.findViewById(R.id.artworkImageView)
            private val seekBar: SeekBar = itemView.findViewById(R.id.seekBar)
            private val waveSeekBar: WaveformSeekBar = itemView.findViewById(R.id.waveSeekBar)
            private val artworkVn: ShapeableImageView = itemView.findViewById(R.id.artworkVn)

            fun Int.dpToPx(context: Context): Int {
                return (this * context.resources.displayMetrics.density).toInt()
            }

            private fun getAdaptiveHeights(context: Context): Pair<Int, Int> {
                val displayMetrics = context.resources.displayMetrics
                val screenHeight = displayMetrics.heightPixels
                val minHeight = (screenHeight * 0.18).toInt()
                val maxHeight = (screenHeight * 0.45).toInt()
                return Pair(minHeight, maxHeight)
            }

            private fun getConstrainedHeight(context: Context, preferredHeight: Int): Int {
                val (minHeight, maxHeight) = getAdaptiveHeights(context)
                return preferredHeight.coerceIn(minHeight, maxHeight)
            }

            private fun getActivityFromContext(context: Context): AppCompatActivity? {
                return when (context) {
                    is AppCompatActivity -> context
                    is ContextWrapper -> getActivityFromContext(context.baseContext)
                    else -> null
                }
            }

            private fun navigateToTappedFilesFragment(
                context: Context,
                currentIndex: Int,
                files: List<File>,
                fileIds: List<String>
            ) {
                val activity = getActivityFromContext(context)
                if (activity != null) {
                    activity.findViewById<View>(R.id.topBar)?.visibility = View.GONE
                    activity.findViewById<View>(R.id.bottomNavigationView)?.visibility = View.GONE

                    val fragment = Tapped_Files_In_The_Container_View_Fragment()

                    val bundle = Bundle().apply {
                        putInt("current_index", currentIndex)
                        putInt("total_files", files.size)

                        val fileUrls = ArrayList<String>()
                        files.forEach { file -> fileUrls.add(file.url) }
                        putStringArrayList("file_urls", fileUrls)
                        putStringArrayList("file_ids", ArrayList(fileIds))

                        val postItems = ArrayList<PostItem>()
                        files.forEachIndexed { index, file ->
                            val fileId = fileIds.getOrNull(index)
                            val fileName =
                                data?.fileNames?.find { it.fileId == fileId }?.fileName ?: ""
                            val postItem = PostItem(
                                audioUrl = file.url,
                                audioThumbnailUrl = null,
                                videoUrl = null,
                                videoThumbnailUrl = null,
                                postId = fileId ?: "audio_file_$index",
                                data = "Audio file: $fileName",
                                files = arrayListOf(file.url),
                                fileType = "audio"
                            )
                            postItems.add(postItem)
                        }
                        putParcelableArrayList("post_list", postItems)
                        putString(
                            "post_id",
                            fileIds.getOrNull(currentIndex) ?: "audio_file_$currentIndex"
                        )
                        putString("media_type", "audio")
                    }

                    fragment.arguments = bundle

                    activity.supportFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left
                        )
                        .replace(R.id.frame_layout, fragment)
                        .addToBackStack("tapped_audio_files_view")
                        .commit()

                    Log.d(
                        TAG,
                        "Navigated to Tapped_Files_In_The_Container_View with ${files.size} audio files, starting at index $currentIndex"
                    )
                } else {
                    Log.e(TAG, "Activity is null, cannot navigate to fragment")
                }
            }

            private var data: OriginalPost? = null

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @SuppressLint("SetTextI18n")
            fun onBind(data: OriginalPost) {
                this.data = data
                val context = itemView.context
                val fileIdToFind = data.fileIds[absoluteAdapterPosition]
                val displayMetrics = context.resources.displayMetrics
                val screenWidth = displayMetrics.widthPixels
                val SpaceBetweenRows = 2.dpToPx(context)

                val (minHeight, maxHeight) = getAdaptiveHeights(context)
                val availableWidth = ((screenWidth - SpaceBetweenRows * 1) / 2 * 0.95f).toInt()
                val fullWidth = (screenWidth - SpaceBetweenRows * 2) // Full width for single items

                val layoutParams = if (materialCardView.layoutParams != null) {
                    materialCardView.layoutParams as ViewGroup.MarginLayoutParams
                } else {
                    ViewGroup.MarginLayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }

                resetMargins(layoutParams)
                imageView2.visibility = View.GONE
                countTextView.visibility = View.GONE
                itemView.visibility = View.VISIBLE
                seekBar.visibility = View.GONE
                waveSeekBar.visibility = View.GONE

                val durationItem = data.duration?.find { it.fileId == fileIdToFind }
                if (!durationItem?.duration.isNullOrEmpty()) {
                    audioDurationTextView.text = durationItem?.duration
                    audioDurationTextView.visibility = View.VISIBLE
                } else {
                    audioDurationTextView.visibility = View.GONE
                }

                val fileName = data.fileNames?.find { it.fileId == fileIdToFind }?.fileName ?: ""

                val clickListener = View.OnClickListener {
                    navigateToTappedFilesFragment(
                        context,
                        absoluteAdapterPosition,
                        data.files,
                        data.fileIds as List<String>
                    )

                    onMultipleFilesClickListener?.multipleFileClickListener(
                        absoluteAdapterPosition,
                        data.files,
                        data.fileIds as List<String>
                    )
                }

                itemView.setOnClickListener(clickListener)
                artworkImageView.setOnClickListener(clickListener)
                materialCardView.setOnClickListener(clickListener)
                artworkLayout.setOnClickListener(clickListener)
                countTextView.setOnClickListener(clickListener)
                audioDurationTextView.setOnClickListener(clickListener)

                configureAudioUI(fileName)

                val itemCount = data.files.size

                when {
                    itemCount <= 1 -> {
                        // Make single items cover full width
                        layoutParams.width = fullWidth
                        layoutParams.height =
                            getConstrainedHeight(context, (maxHeight * 0.6).toInt())
                        layoutParams.leftMargin = SpaceBetweenRows
                        layoutParams.rightMargin = SpaceBetweenRows
                        materialCardView.radius = 8.dpToPx(context).toFloat()
                    }

                    itemCount == 2 -> {
                        layoutParams.width = availableWidth
                        layoutParams.height =
                            getConstrainedHeight(context, (maxHeight * 0.6).toInt())
                        materialCardView.radius = 8.dpToPx(context).toFloat()

                        when (absoluteAdapterPosition) {
                            0 -> layoutParams.rightMargin = (SpaceBetweenRows / 2)
                            1 -> layoutParams.leftMargin = (SpaceBetweenRows / 2)
                        }
                    }

                    itemCount == 3 -> {
                        // Calculate total height for position 0
                        val desiredHeight = (maxHeight * 0.6).toInt()
                        val constrainedHeight = getConstrainedHeight(context, desiredHeight)

                        // Compute half-height for items 1 and 2 with spacing in mind
                        val halfHeight = (constrainedHeight - SpaceBetweenRows) / 2

                        when (absoluteAdapterPosition) {
                            0 -> {
                                layoutParams.width = availableWidth
                                layoutParams.height = constrainedHeight
                                layoutParams.rightMargin = (SpaceBetweenRows / 2)
                                layoutParams.leftMargin = 0
                                layoutParams.topMargin = 0
                                layoutParams.bottomMargin = 0
                            }

                            1 -> {
                                layoutParams.width = availableWidth
                                layoutParams.height = halfHeight
                                layoutParams.leftMargin = (SpaceBetweenRows / 2)
                                layoutParams.rightMargin = 0
                                layoutParams.topMargin = 0
                                layoutParams.bottomMargin = SpaceBetweenRows
                            }

                            2 -> {
                                layoutParams.width = availableWidth
                                layoutParams.height = halfHeight
                                layoutParams.leftMargin = (SpaceBetweenRows / 2)
                                layoutParams.rightMargin = 0
                                layoutParams.topMargin = SpaceBetweenRows
                                layoutParams.bottomMargin = 0
                            }
                        }

                        materialCardView.radius = 8.dpToPx(context).toFloat()
                    }

                    itemCount == 4 -> {
                        val preferredHeight =
                            getConstrainedHeight(context, (maxHeight * 0.6).toInt())

                        layoutParams.width = availableWidth
                        layoutParams.height = preferredHeight
                        materialCardView.radius = 6.dpToPx(context).toFloat()

                        when (absoluteAdapterPosition) {
                            0 -> {
                                layoutParams.rightMargin = (SpaceBetweenRows / 2)
                                layoutParams.bottomMargin = SpaceBetweenRows
                            }

                            1 -> {
                                layoutParams.leftMargin = (SpaceBetweenRows / 2)
                                layoutParams.bottomMargin = SpaceBetweenRows
                            }

                            2 -> {
                                layoutParams.rightMargin = (SpaceBetweenRows / 2)
                                layoutParams.topMargin = SpaceBetweenRows
                            }

                            3 -> {
                                layoutParams.leftMargin = (SpaceBetweenRows / 2)
                                layoutParams.topMargin = SpaceBetweenRows
                            }
                        }
                    }

                    itemCount > 4 -> {
                        if (absoluteAdapterPosition >= 4) {
                            itemView.visibility = View.GONE
                            layoutParams.width = 0
                            layoutParams.height = 0
                            materialCardView.layoutParams = layoutParams
                            return
                        }

                        itemView.visibility = View.VISIBLE
                        val preferredHeight =
                            getConstrainedHeight(context, (maxHeight * 0.6).toInt())

                        layoutParams.width = availableWidth
                        layoutParams.height = preferredHeight
                        materialCardView.radius = 8.dpToPx(context).toFloat()

                        when (absoluteAdapterPosition) {
                            0 -> {
                                layoutParams.rightMargin = (SpaceBetweenRows / 2)
                                layoutParams.bottomMargin = SpaceBetweenRows
                            }

                            1 -> {
                                layoutParams.leftMargin = (SpaceBetweenRows / 2)
                                layoutParams.bottomMargin = SpaceBetweenRows
                            }

                            2 -> {
                                layoutParams.rightMargin = (SpaceBetweenRows / 2)
                                layoutParams.topMargin = SpaceBetweenRows
                            }

                            3 -> {
                                layoutParams.leftMargin = (SpaceBetweenRows / 2)
                                layoutParams.topMargin = SpaceBetweenRows
                            }
                        }

                        if (absoluteAdapterPosition == 3) {
                            countTextView.visibility = View.VISIBLE
                            countTextView.text = "+${itemCount - 4}"
                            countTextView.textSize = 32f
                            countTextView.setPadding(12, 4, 12, 4)

                            val background = GradientDrawable().apply {
                                shape = GradientDrawable.RECTANGLE
                                cornerRadius = 16f
                                setColor(Color.parseColor("#80000000"))
                            }
                            countTextView.background = background
                        } else {
                            countTextView.visibility = View.GONE
                            countTextView.setPadding(0, 0, 0, 0)
                            countTextView.background = null
                        }
                    }
                }

                materialCardView.layoutParams = layoutParams
            }


            private fun configureAudioUI(fileName: String) {
                val context = itemView.context

                // Reset all views first
                seekBar.visibility = View.GONE
                waveSeekBar.visibility = View.GONE

                // Handle different audio formats - EXACTLY like the first code
                when {
                    fileName.endsWith(".mp3", true) ||
                            fileName.endsWith(".wav", true) -> {
                        // Show artwork for common audio formats with music placeholder
                        materialCardView.setCardBackgroundColor(Color.WHITE) // Set white for music files

                        // Show main artwork image with music icon
                        artworkImageView.setImageResource(R.drawable.music_icon)
                        artworkImageView.visibility = View.VISIBLE
                        artworkImageView.scaleType = ImageView.ScaleType.CENTER_CROP

                        // Hide the artworkLayout (mic icon layout)
                        artworkLayout.visibility = View.GONE
                    }

                    fileName.endsWith(".ogg", true) ||
                            fileName.endsWith(".aac", true) ||
                            fileName.endsWith(".m4a", true) ||
                            fileName.endsWith(".flac", true) ||
                            fileName.endsWith(".amr", true) ||
                            fileName.endsWith(".3gp", true) ||
                            fileName.endsWith(".opus", true) -> {
                        // Set gray background for voice note formats
                        materialCardView.setCardBackgroundColor(Color.parseColor("#616161"))

                        // Hide the main artwork image
                        artworkImageView.visibility = View.GONE

                        val artworkLayoutWrapper =
                            itemView.findViewById<MaterialCardView>(R.id.artworkLayoutWrapper)
                        artworkLayoutWrapper?.visibility = View.VISIBLE
                        artworkLayoutWrapper?.setCardBackgroundColor(Color.parseColor("#616161"))

                        // Original approach if not using wrapper:
                        artworkLayout.visibility = View.VISIBLE
                        artworkLayout.setBackgroundColor(Color.parseColor("#616161")) // Match dark_gray

                        // Ensure the mic icon (artworkVn) is properly configured with corner radius
                        artworkVn.setImageResource(R.drawable.ic_audio_white_icon)
                        artworkVn.visibility = View.VISIBLE

                        // Make sure the layout parameters are correct for centering
                        val layoutParams = artworkVn.layoutParams
                        if (layoutParams != null) {
                            layoutParams.width = 120.dpToPx(context)
                            layoutParams.height = 270.dpToPx(context)
                            artworkVn.layoutParams = layoutParams
                        }
                    }

                    else -> {
                        // Default case - show artwork with music placeholder
                        materialCardView.setCardBackgroundColor(Color.WHITE)

                        // Show main artwork image with music icon
                        artworkImageView.setImageResource(R.drawable.music_icon)
                        artworkImageView.visibility = View.VISIBLE
                        artworkImageView.scaleType = ImageView.ScaleType.CENTER_CROP

                        // Hide the artworkLayout
                        artworkLayout.visibility = View.GONE
                    }
                }

                // Find the audioDurationLayout and keep it visible
                val audioDurationLayout =
                    itemView.findViewById<LinearLayout>(R.id.audioDurationLayout)
                audioDurationLayout?.visibility = View.VISIBLE
            }

            private fun addPlayIconOverlay(targetImageView: ImageView) {
                val context = targetImageView.context
                val parent = targetImageView.parent as? ViewGroup ?: return

                // Remove any existing play icon overlays to avoid duplicates
                for (i in parent.childCount - 1 downTo 0) {
                    val child = parent.getChildAt(i)
                    if (child.tag == "play_icon_overlay_image") {
                        parent.removeView(child)
                    }
                }

                // Create a FrameLayout to overlay on top of the image
                val overlayContainer = FrameLayout(context)
                val containerParams = when (parent) {
                    is FrameLayout -> FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )

                    is LinearLayout -> LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )

                    else -> ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
                overlayContainer.layoutParams = containerParams
                overlayContainer.tag = "play_icon_overlay_image"

                // Create a play icon overlay
                val playIcon = ImageView(context)
                playIcon.setImageResource(R.drawable.play_button_filled)
                playIcon.setColorFilter(Color.WHITE)
                val playIconSize = 48.dpToPx(context)
                val playLayoutParams = FrameLayout.LayoutParams(playIconSize, playIconSize)
                playLayoutParams.gravity = Gravity.CENTER
                playIcon.layoutParams = playLayoutParams

                // Add semi-transparent background to the play icon for better visibility
                val playBackground = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor("#80000000")) // Semi-transparent black
                }
                playIcon.background = playBackground
                playIcon.setPadding(12, 12, 12, 12)

                // Add the play icon to the overlay container
                overlayContainer.addView(playIcon)

                // Add the overlay container to the parent
                parent.addView(overlayContainer)
            }

            private fun addPlayIconOverlayToLayout(targetLayout: LinearLayout) {
                val context = targetLayout.context

                // Remove any existing play icon overlays to avoid duplicates
                for (i in targetLayout.childCount - 1 downTo 0) {
                    val child = targetLayout.getChildAt(i)
                    if (child.tag == "play_icon_overlay") {
                        targetLayout.removeView(child)
                    }
                }

                // Create a FrameLayout to hold both the audio icon and play button
                val overlayContainer = FrameLayout(context)
                val containerParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                containerParams.gravity = Gravity.CENTER
                overlayContainer.layoutParams = containerParams

                // Create a play icon overlay
                val playIcon = ImageView(context)
                playIcon.setImageResource(R.drawable.play_button_filled)
                playIcon.setColorFilter(Color.WHITE)
                val playIconSize = 48.dpToPx(context)
                val playLayoutParams = FrameLayout.LayoutParams(playIconSize, playIconSize)
                playLayoutParams.gravity = Gravity.CENTER
                playIcon.layoutParams = playLayoutParams

                // Add semi-transparent background to the play icon for better visibility
                val playBackground = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor("#80000000")) // Semi-transparent black
                }
                playIcon.background = playBackground
                playIcon.setPadding(12, 12, 12, 12)
                playIcon.tag = "play_icon_overlay"

                // Add the play icon to the overlay container
                overlayContainer.addView(playIcon)

                // Add the overlay container to the target layout
                targetLayout.addView(overlayContainer)
            }

            private fun resetMargins(layoutParams: ViewGroup.MarginLayoutParams) {
                layoutParams.leftMargin = 0
                layoutParams.rightMargin = 0
                layoutParams.topMargin = 0
                layoutParams.bottomMargin = 0
            }

        }


        inner class FeedRepostVideosOnly(itemView: View) : RecyclerView.ViewHolder(itemView) {

            fun Int.dpToPx(context: Context): Int {
                return (this * context.resources.displayMetrics.density).toInt()
            }

            private val feedThumbnail: ImageView = itemView.findViewById(R.id.feedThumbnail)
            private val feedVideoDurationTextView: TextView =
                itemView.findViewById(R.id.feedVideoDurationTextView)
            private val cardView: CardView = itemView.findViewById(R.id.cardView)
            private val countTextView: TextView = itemView.findViewById(R.id.countTextView)
            private val imageView2: ImageView = itemView.findViewById(R.id.imageView2)

            private fun getAdaptiveHeights(screenHeight: Int): Pair<Int, Int> {
                val minHeight = (screenHeight * 0.18).toInt()
                val maxHeight = (screenHeight * 0.45).toInt()
                return Pair(minHeight, maxHeight)
            }

            private fun getConstrainedHeight(
                desiredHeight: Int,
                minHeight: Int,
                maxHeight: Int
            ): Int {
                return desiredHeight.coerceIn(minHeight, maxHeight)
            }

            private fun getActivityFromContext(context: Context): AppCompatActivity? {
                return when (context) {
                    is AppCompatActivity -> context
                    is ContextWrapper -> getActivityFromContext(context.baseContext)
                    else -> null
                }
            }

            private fun navigateToTappedFilesFragment(
                context: Context,
                currentIndex: Int,
                files: List<File>,
                fileIds: List<String>
            ) {
                val activity = getActivityFromContext(context)
                if (activity != null) {
                    activity.findViewById<View>(R.id.topBar)?.visibility = View.GONE
                    activity.findViewById<View>(R.id.bottomNavigationView)?.visibility = View.GONE

                    val fragment = Tapped_Files_In_The_Container_View_Fragment()

                    val bundle = Bundle().apply {
                        putInt("current_index", currentIndex)
                        putInt("total_files", files.size)

                        val fileUrls = ArrayList<String>()
                        files.forEach { file -> fileUrls.add(file.url) }
                        putStringArrayList("file_urls", fileUrls)
                        putStringArrayList("file_ids", ArrayList(fileIds))

                        val postItems = ArrayList<PostItem>()
                        files.forEachIndexed { index, file ->
                            val postItem = PostItem(
                                audioUrl = file.url,
                                audioThumbnailUrl = null,
                                videoUrl = file.url,
                                videoThumbnailUrl = null,
                                postId = fileIds.getOrNull(index) ?: "file_$index",
                                data = "Post data for file $index",
                                files = arrayListOf(file.url)
                            )
                            postItems.add(postItem)
                        }
                        putParcelableArrayList("post_list", postItems)
                        putString(
                            "post_id",
                            fileIds.getOrNull(currentIndex) ?: "file_$currentIndex"
                        )
                    }

                    fragment.arguments = bundle

                    activity.supportFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left
                        )
                        .replace(R.id.frame_layout, fragment)
                        .addToBackStack("tapped_files_view")
                        .commit()

                    Log.d(
                        TAG, "Navigated to Tapped_Files_In_The_Container_View with" +
                                " ${files.size} files, starting at index $currentIndex"
                    )
                } else {
                    Log.e(TAG, "Activity is null, cannot navigate to fragment")
                }
            }

            @SuppressLint("SetTextI18n")
            fun onBind(data: OriginalPost) {
                Log.d(TAG, "onBind: file type Video $absoluteAdapterPosition item count $itemCount")

                val fileIdToFind = data.fileIds[absoluteAdapterPosition]
                val durationItem = data.duration?.find { it.fileId == fileIdToFind }
                val thumbnail = data.thumbnail.find { it.fileId == fileIdToFind }

                feedVideoDurationTextView.text = durationItem?.duration

                val fileSize = itemCount

                val context = itemView.context
                val displayMetrics = context.resources.displayMetrics
                val screenWidth = displayMetrics.widthPixels
                val screenHeight = displayMetrics.heightPixels
                val margin = 2.dpToPx(context)
                val spaceBetweenRows = 2.dpToPx(context)

                val (minHeight, maxHeight) = getAdaptiveHeights(screenHeight)
                val availableWidth = ((screenWidth - spaceBetweenRows * 1) / 2 * 0.95f).toInt()

                if (thumbnail != null) {
                    Glide.with(context)
                        .load(thumbnail.thumbnailUrl)
                        .placeholder(R.drawable.flash21)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(feedThumbnail)
                } else {
                    Glide.with(context)
                        .load(R.drawable.videoplaceholder)
                        .placeholder(R.drawable.flash21)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(feedThumbnail)
                }

                val layoutParams = cardView.layoutParams as ViewGroup.MarginLayoutParams
                cardView.radius = 8.dpToPx(context).toFloat()

                itemView.setOnClickListener {
                    navigateToTappedFilesFragment(
                        context,
                        absoluteAdapterPosition,
                        data.files,
                        data.fileIds as List<String>
                    )

                    onMultipleFilesClickListener?.multipleFileClickListener(
                        absoluteAdapterPosition,
                        data.files,
                        data.fileIds as List<String>
                    )
                }

                feedThumbnail.setOnClickListener {
                    navigateToTappedFilesFragment(
                        context,
                        absoluteAdapterPosition,
                        data.files,
                        data.fileIds as List<String>
                    )
                }

                cardView.setOnClickListener {
                    navigateToTappedFilesFragment(
                        context,
                        absoluteAdapterPosition,
                        data.files,
                        data.fileIds as List<String>
                    )
                }

                when {

                    fileSize <= 1 -> {
                        val desiredHeight = (maxHeight * 0.6).toInt()
                        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                        layoutParams.height =
                            getConstrainedHeight(desiredHeight, minHeight, maxHeight)
                        layoutParams.leftMargin = spaceBetweenRows
                        layoutParams.rightMargin = spaceBetweenRows
                        layoutParams.topMargin = 0
                        layoutParams.bottomMargin = 0
                    }

                    fileSize == 2 -> {
                        val desiredHeight = (maxHeight * 0.6).toInt()
                        layoutParams.width = availableWidth
                        layoutParams.height =
                            getConstrainedHeight(desiredHeight, minHeight, maxHeight)
                        layoutParams.topMargin = 0
                        layoutParams.bottomMargin = 0

                        val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
                        layoutParams.leftMargin =
                            if (isLeftColumn) (spaceBetweenRows / 2) else (spaceBetweenRows * 2)
                        layoutParams.rightMargin =
                            if (isLeftColumn) (spaceBetweenRows * 2) else (spaceBetweenRows / 2)
                    }

                    fileSize == 3 -> {
                        val spanLayout =
                            cardView.layoutParams as? StaggeredGridLayoutManager.LayoutParams

                        // Step 1: Calculate full height for position 0
                        val desiredHeight = (maxHeight * 0.6).toInt()
                        val constrainedHeight =
                            getConstrainedHeight(desiredHeight, minHeight, maxHeight)

                        // Step 2: Calculate half height for positions 1 and 2, minus spacing adjustment
                        val halfHeight = (constrainedHeight - spaceBetweenRows) / 2

                        when (absoluteAdapterPosition) {
                            0 -> {
                                spanLayout?.isFullSpan = false
                                layoutParams.width = availableWidth
                                layoutParams.height = constrainedHeight

                                layoutParams.leftMargin = (spaceBetweenRows / 2)
                                layoutParams.rightMargin = (spaceBetweenRows / 2)
                                layoutParams.topMargin = 0
                                layoutParams.bottomMargin = 0
                            }

                            1 -> {
                                spanLayout?.isFullSpan = false
                                layoutParams.width = availableWidth
                                layoutParams.height = halfHeight

                                layoutParams.leftMargin = (spaceBetweenRows / 2)
                                layoutParams.rightMargin = (spaceBetweenRows / 2)
                                layoutParams.topMargin = 0
                                layoutParams.bottomMargin = (spaceBetweenRows)
                            }

                            2 -> {
                                spanLayout?.isFullSpan = false
                                layoutParams.width = availableWidth
                                layoutParams.height = halfHeight

                                layoutParams.leftMargin = (spaceBetweenRows / 2)
                                layoutParams.rightMargin = (spaceBetweenRows / 2)
                                layoutParams.topMargin = spaceBetweenRows
                                layoutParams.bottomMargin = 0
                            }
                        }

                        cardView.layoutParams = layoutParams
                    }


                    fileSize == 4 -> {
                        val desiredHeight = (maxHeight * 0.6).toInt()
                        val adaptiveGridHeight =
                            getConstrainedHeight(desiredHeight, minHeight, maxHeight)

                        layoutParams.width = availableWidth
                        layoutParams.height = adaptiveGridHeight

                        val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
                        layoutParams.leftMargin =
                            if (isLeftColumn) (spaceBetweenRows) else (spaceBetweenRows * 2)
                        layoutParams.rightMargin =
                            if (isLeftColumn) (spaceBetweenRows * 2) else (spaceBetweenRows)

                        layoutParams.topMargin =
                            if (absoluteAdapterPosition < 2) spaceBetweenRows else spaceBetweenRows
                        layoutParams.bottomMargin = spaceBetweenRows
                    }

                    else -> {
                        if (absoluteAdapterPosition >= 4) {
                            itemView.visibility = View.GONE
                            layoutParams.width = 0
                            layoutParams.height = 0
                            itemView.layoutParams = layoutParams
                            return
                        }

                        itemView.visibility = View.VISIBLE
                        val desiredHeight = (maxHeight * 0.6).toInt()
                        val adaptiveGridHeight =
                            getConstrainedHeight(desiredHeight, minHeight, maxHeight)

                        layoutParams.width = availableWidth
                        layoutParams.height = adaptiveGridHeight

                        val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
                        layoutParams.leftMargin =
                            if (isLeftColumn) (spaceBetweenRows) else (spaceBetweenRows * 2)
                        layoutParams.rightMargin =
                            if (isLeftColumn) (spaceBetweenRows * 2) else (spaceBetweenRows)
                        layoutParams.topMargin =
                            if (absoluteAdapterPosition < 2) spaceBetweenRows else spaceBetweenRows
                        layoutParams.bottomMargin = spaceBetweenRows

                        if (absoluteAdapterPosition == 3) {
                            countTextView.visibility = View.VISIBLE
                            countTextView.text = "+${fileSize - 4}"
                            countTextView.textSize = 32f
                            countTextView.setPadding(12, 4, 12, 4)

                            val background = GradientDrawable().apply {
                                shape = GradientDrawable.RECTANGLE
                                cornerRadius = 16f
                                setColor(Color.parseColor("#80000000"))
                            }
                            countTextView.background = background
                        } else {
                            countTextView.visibility = View.GONE
                            countTextView.setPadding(0, 0, 0, 0)
                            countTextView.background = null
                        }
                    }
                }

                cardView.layoutParams = layoutParams
            }
        }


        inner class FeedRepostDocumentsOnly(itemView: View) : RecyclerView.ViewHolder(itemView) {

            fun Int.dpToPx(context: Context): Int {
                return (this * context.resources.displayMetrics.density).toInt()
            }

            private var tag = "FeedDocument"
            private val pdfImageView: ImageView = itemView.findViewById(R.id.pdfImageView)
            private val documentContainer: CardView = itemView.findViewById(R.id.documentContainer)
            private val fileTypeIcon: ImageView = itemView.findViewById(R.id.fileTypeIcon)

            // Helper function to get adaptive heights based on screen size
            private fun getAdaptiveHeights(context: Context): Pair<Int, Int> {
                val displayMetrics = context.resources.displayMetrics
                val screenHeight = displayMetrics.heightPixels

                // For documents: min = 15% of screen height, max = 38% of screen height
                val minHeight = (screenHeight * 0.20).toInt()
                val maxHeight = (screenHeight * 0.45).toInt()

                return Pair(minHeight, maxHeight)
            }

            // Helper function to constrain height within min/max bounds
            private fun getConstrainedHeight(context: Context, targetHeight: Int): Int {
                val (minHeight, maxHeight) = getAdaptiveHeights(context)
                return targetHeight.coerceIn(minHeight, maxHeight)
            }

            private fun getActivityFromContext(context: Context): AppCompatActivity? {
                return when (context) {
                    is AppCompatActivity -> context
                    is ContextWrapper -> getActivityFromContext(context.baseContext)
                    else -> null
                }
            }

            // Add the navigation function
            private fun navigateToTappedFilesFragment(
                context: Context,
                currentIndex: Int,
                files: List<File>,
                fileIds: List<String>
            ) {
                val activity = getActivityFromContext(context)
                if (activity != null) {
                    // Hide AppBar (Toolbar) if available
                    activity.findViewById<View>(R.id.topBar)?.visibility = View.GONE
                    // Hide Bottom Navigation if available
                    activity.findViewById<View>(R.id.bottomNavigationView)?.visibility = View.GONE

                    val fragment = Tapped_Files_In_The_Container_View_Fragment()

                    val bundle = Bundle().apply {
                        putInt("current_index", currentIndex)
                        putInt("total_files", files.size)

                        val fileUrls = ArrayList<String>()
                        files.forEach { file -> fileUrls.add(file.url) }
                        putStringArrayList("file_urls", fileUrls)
                        putStringArrayList("file_ids", ArrayList(fileIds))

                        val postItems = ArrayList<PostItem>()
                        files.forEachIndexed { index, file ->
                            val postItem = PostItem(
                                audioUrl = file.url,
                                audioThumbnailUrl = null,
                                videoUrl = file.url,
                                videoThumbnailUrl = null,
                                postId = fileIds.getOrNull(index) ?: "file_$index",
                                data = "Post data for file $index",
                                files = arrayListOf(file.url)
                            )
                            postItems.add(postItem)
                        }
                        putParcelableArrayList("post_list", postItems)
                        putString(
                            "post_id",
                            fileIds.getOrNull(currentIndex) ?: "file_$currentIndex"
                        )
                    }

                    fragment.arguments = bundle

                    activity.supportFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left
                        )
                        .replace(R.id.frame_layout, fragment)
                        .addToBackStack("tapped_files_view")
                        .commit()

                    Log.d(
                        TAG,
                        "Navigated to Tapped_Files_In_The_Container_View with ${files.size} files, starting at index $currentIndex"
                    )
                } else {
                    Log.e(TAG, "Activity is null, cannot navigate to fragment")
                }
            }

            @SuppressLint("SetTextI18n", "UseKtx")
            fun onBind(data: OriginalPost) {

                val sideMargin = 2.dpToPx(itemView.context)
                val context = itemView.context // Define context properly

                Log.d(TAG, "onBind: file type Document $absoluteAdapterPosition item count $itemCount")

                val fileIdToFind = data.fileIds[absoluteAdapterPosition]
                val documentType = data.fileTypes?.find { it.fileId == fileIdToFind }

                val fileSize = itemCount

                // Get adaptive heights
                val (minHeight, maxHeight) = getAdaptiveHeights(context)

                // Replace the existing click listener with this:
                itemView.setOnClickListener {
                    // Navigate to the fragment
                    navigateToTappedFilesFragment(
                        context,
                        absoluteAdapterPosition,
                        data.files,
                        data.fileIds as List<String>
                    )

                    // Optional: Still call the original listener if needed
                    onMultipleFilesClickListener?.multipleFileClickListener(
                        absoluteAdapterPosition,
                        data.files,
                        data.fileIds as List<String>
                    )
                }

                // Add click listener to the document image
                pdfImageView.setOnClickListener {
                    navigateToTappedFilesFragment(
                        context,
                        absoluteAdapterPosition,
                        data.files,
                        data.fileIds as List<String>
                    )
                }

                // Add click listener to the document container
                documentContainer.setOnClickListener {
                    navigateToTappedFilesFragment(
                        context,
                        absoluteAdapterPosition,
                        data.files,
                        data.fileIds as List<String>
                    )
                }

                // Set the file type icon (e.g., PDF, DOCX, PPTX)
                if (documentType != null) {

                    val fileExtension = documentType.fileType

                    fileTypeIcon.setImageResource(
                        when (fileExtension) {
                            "pdf" -> R.drawable.pdf_icon
                            "doc", "docx" -> R.drawable.word_icon
                            "ppt", "pptx" -> R.drawable.powerpoint_icon
                            "xls", "xlsx" -> R.drawable.excel_icon
                            "txt" -> R.drawable.text_icon
                            "rtf" -> R.drawable.text_icon
                            "odt" -> R.drawable.word_icon
                            "csv" -> R.drawable.excel_icon
                            else -> R.drawable.text_icon
                        }
                    )
                    fileTypeIcon.visibility = View.VISIBLE
                }

                if (documentType != null) {

                    // Handle PDF files
                    if (documentType.fileType == "pdf") {
                        val thumbnail = data.thumbnail.find { it.fileId == fileIdToFind }
                        pdfImageView.scaleType = ImageView.ScaleType.CENTER_INSIDE

                        if (thumbnail != null) {
                            Glide.with(context)
                                .load(thumbnail.thumbnailUrl)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(pdfImageView)
                        }

                        pdfImageView.visibility = View.VISIBLE
                    } else if (documentType.fileType == "docx" || documentType.fileType == "pptx") {
                        val thumbnail = data.thumbnail.find { it.fileId == fileIdToFind }
                        pdfImageView.scaleType = ImageView.ScaleType.CENTER_INSIDE

                        Log.d(tag, "onBind: Documents File type is not pdf")
                        Glide.with(context)
                            .load(thumbnail?.thumbnailUrl)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(pdfImageView)
                        pdfImageView.visibility = View.VISIBLE
                    }

                    when {

                        fileSize == 1 -> {

                            Log.d(TAG, "bind: file size 1")

                            val topMargin = (-8).dpToPx(context)
                            // Use 85% of max height for single document
                            val adaptiveHeight =
                                getConstrainedHeight(context, (maxHeight * 0.75).toInt())

                            val containerParams =
                                documentContainer.layoutParams as ViewGroup.MarginLayoutParams
                            containerParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                            containerParams.height = adaptiveHeight
                            containerParams.setMargins(0, topMargin, 0, 0)
                            documentContainer.layoutParams = containerParams
                            documentContainer.setBackgroundColor(Color.BLACK)

                            // Clear container
                            documentContainer.removeAllViews()

                            val centerContainer = FrameLayout(context).apply {
                                layoutParams = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                setPadding(0, 0, 0, 0)
                                setBackgroundColor(Color.rgb(160, 160, 160))
                            }

                            // Create a new ImageView for single document view
                            val singleImageView = ImageView(context).apply {
                                layoutParams = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    Gravity.CENTER
                                ).apply {
                                    height = adaptiveHeight
                                }
                                scaleType = ImageView.ScaleType.FIT_CENTER
                            }

                            // Add image view to the center container
                            centerContainer.addView(singleImageView)

                            // Create an overlay layout for fileTypeIcon
                            val overlayLayout = FrameLayout(context).apply {
                                layoutParams = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            }

                            // Configure fileTypeIcon
                            (fileTypeIcon.parent as? ViewGroup)?.removeView(fileTypeIcon)
                            fileTypeIcon.layoutParams = FrameLayout.LayoutParams(
                                20.dpToPx(context),
                                20.dpToPx(context),
                                Gravity.TOP or Gravity.START
                            ).apply {
                                setMargins(8.dpToPx(context), 8.dpToPx(context), 0, 0)
                            }

                            // Add fileTypeIcon to the overlay
                            overlayLayout.addView(fileTypeIcon)

                            // Add the overlay on top of the center container
                            centerContainer.addView(overlayLayout)

                            // Add complete center container to documentContainer
                            documentContainer.addView(centerContainer)

                            // Filter and load thumbnail into singleImageView
                            val thumbnails = data.thumbnail.filter { thumb ->
                                data.fileIds.contains(thumb.fileId)
                            }

                            thumbnails.getOrNull(0)?.let { thumb ->
                                Glide.with(context)
                                    .load(thumb.thumbnailUrl)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(singleImageView)
                            }
                        }

                        fileSize == 2 -> {

                            Log.d(TAG, "onBind: Document file size == 2")

                            // Ensure itemView is visible and takes proper space
                            itemView.visibility = View.VISIBLE

                            // Set RecyclerView.LayoutParams to ensure horizontal layout
                            val recyclerParams = RecyclerView.LayoutParams(
                                0, // Width = 0dp for weight-based distribution
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            itemView.layoutParams = recyclerParams

                            val cardView = itemView.findViewById<CardView>(R.id.documentContainer)
                            val imageView = itemView.findViewById<ImageView>(R.id.pdfImageView)

                            // Use 75% of max height for two documents
                            val adaptiveHeight =
                                getConstrainedHeight(context, (maxHeight * 0.65).toInt())

                            // Layout params for CardView - crucial for horizontal alignment
                            val cardLayoutParams =
                                cardView.layoutParams as ViewGroup.MarginLayoutParams
                            cardLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                            cardLayoutParams.height = adaptiveHeight

                            // Reset all margins first
                            cardLayoutParams.topMargin = 0
                            cardLayoutParams.bottomMargin = 0
                            cardLayoutParams.leftMargin = 0
                            cardLayoutParams.rightMargin = 0

                            when (absoluteAdapterPosition) {
                                0 -> {
                                    // First item: Small margin on right for gap
                                    cardLayoutParams.rightMargin = sideMargin / 2
                                }
                                1 -> {
                                    // Second item: Small margin on left for gap
                                    cardLayoutParams.leftMargin = sideMargin / 2
                                }
                            }

                            cardView.layoutParams = cardLayoutParams

                            // Match ImageView height to the CardView's height
                            val imageLayoutParams =
                                imageView.layoutParams as ViewGroup.MarginLayoutParams
                            imageLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                            imageLayoutParams.height = cardLayoutParams.height
                            imageLayoutParams.topMargin = 0
                            imageLayoutParams.bottomMargin = 0
                            imageView.layoutParams = imageLayoutParams

                            // Ensure image scales properly
                            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                        }

                        fileSize >= 3 -> {

                            Log.d(TAG, "onBind: Document file size >= 3")

                            // Hide additional items (index 2 and beyond)
                            if (absoluteAdapterPosition >= 2) {
                                Log.d(TAG, "onBind: position >= 2, hiding item view")
                                itemView.visibility = View.GONE
                                itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
                                return
                            }

                            // Ensure visible items have proper layout
                            itemView.visibility = View.VISIBLE

                            // Set equal weight for first two items
                            val recyclerParams = RecyclerView.LayoutParams(
                                0, // Width = 0dp for weight-based distribution
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            itemView.layoutParams = recyclerParams

                            val cardView = itemView.findViewById<CardView>(R.id.documentContainer)
                            val imageView = itemView.findViewById<ImageView>(R.id.pdfImageView)

                            // Use 70% of max height for multiple documents
                            val adaptiveHeight =
                                getConstrainedHeight(context, (maxHeight * 0.65).toInt())

                            // Match CardView to parent with adaptive height
                            val cardLayoutParams =
                                cardView.layoutParams as ViewGroup.MarginLayoutParams
                            cardLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                            cardLayoutParams.height = adaptiveHeight

                            // Reset margins
                            cardLayoutParams.topMargin = 0
                            cardLayoutParams.bottomMargin = 0
                            cardLayoutParams.leftMargin = 0
                            cardLayoutParams.rightMargin = 0

                            when (absoluteAdapterPosition) {
                                0 -> {
                                    // First item: Small gap on right
                                    cardLayoutParams.rightMargin = sideMargin / 2
                                }
                                1 -> {
                                    // Second item: Small gap on left
                                    cardLayoutParams.leftMargin = sideMargin / 2
                                }
                            }

                            cardView.layoutParams = cardLayoutParams

                            // Set the ImageView height to match the CardView
                            val imageLayoutParams =
                                imageView.layoutParams as ViewGroup.MarginLayoutParams
                            imageLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                            imageLayoutParams.height = cardLayoutParams.height
                            imageView.layoutParams = imageLayoutParams

                            // Ensure the image scales properly
                            imageView.scaleType = ImageView.ScaleType.CENTER_CROP

                            // Handle different cases for position 0 and position 1 (additional files)
                            when (absoluteAdapterPosition) {

                                0 -> {
                                    Log.d(TAG, "onBind: position 0 for document with additional files")

                                    // Remove any previously added overlays (from recycled views)
                                    val parent = imageView.parent as ViewGroup
                                    parent.findViewWithTag<View>("overlay_tag")?.let {
                                        parent.removeView(it)
                                    }
                                    parent.findViewWithTag<View>("text_overlay_container")?.let {
                                        parent.removeView(it)
                                    }
                                }

                                1 -> {
                                    Log.d(TAG, "onBind: position 1 for document with additional files")

                                    val remainingFilesCount = fileSize - 2
                                    val plusCountText = "+$remainingFilesCount"

                                    val parent = imageView.parent as ViewGroup

                                    // Add overlay only if not already added
                                    if (parent.findViewWithTag<View>("overlay_tag") == null) {
                                        // Create a FrameLayout to wrap the ImageView + Overlay + Text
                                        val imageWrapper = FrameLayout(context).apply {
                                            layoutParams = imageView.layoutParams
                                        }

                                        // Remove the ImageView from its parent and re-add in wrapper
                                        val index = parent.indexOfChild(imageView)
                                        parent.removeView(imageView)
                                        imageWrapper.addView(imageView)

                                        // Create the container for the dim effect around the "+N" count text
                                        val overlayContainer = FrameLayout(context).apply {
                                            // Create rounded dimmed background
                                            background = GradientDrawable().apply {
                                                shape = GradientDrawable.RECTANGLE
                                                cornerRadius = 16f // Adjust the radius as needed
                                                setColor(Color.parseColor("#80000000")) // Semi-transparent black
                                            }

                                            tag = "overlay_tag"

                                            layoutParams = FrameLayout.LayoutParams(
                                                FrameLayout.LayoutParams.WRAP_CONTENT,
                                                FrameLayout.LayoutParams.WRAP_CONTENT
                                            ).apply {
                                                gravity = Gravity.BOTTOM or Gravity.END
                                                marginEnd = 8
                                                bottomMargin = 8
                                            }

                                            setPadding(12, 4, 12, 4) // Padding around the text
                                        }

                                        // Create the "+N" TextView
                                        val textView = TextView(context).apply {
                                            text = plusCountText
                                            setTextColor(Color.WHITE)
                                            textSize = 32f
                                            gravity = Gravity.CENTER
                                        }

                                        // Add TextView to the container
                                        overlayContainer.addView(textView)

                                        // Add container to the image wrapper
                                        imageWrapper.addView(overlayContainer)

                                        // Add everything back to the original parent
                                        parent.addView(imageWrapper, index)
                                    }
                                }
                            }
                        }
                    }
                }
            }


        }


        inner class FeedRepostCombinationOfMultipleFiles(itemView: View) : RecyclerView.ViewHolder(itemView) {

            private val imageView: ImageView = itemView.findViewById(R.id.imageView)
            private val materialCardView: MaterialCardView = itemView.findViewById(R.id.materialCardView)
            private val countTextView: TextView = itemView.findViewById(R.id.countTextView)
            private val imageView2: ImageView = itemView.findViewById(R.id.imageViewOverlay)
            private val fileTypeIcon: ImageView = itemView.findViewById(R.id.fileTypeIcon)
            private val playButton: ImageView = itemView.findViewById(R.id.playButton)
            private val feedVideoImageView: ImageView = itemView.findViewById(R.id.feedVideoImageView)
            private val feedVideoDurationTextView: TextView = itemView.findViewById(R.id.feedVideoDurationTextView)

            fun Int.dpToPx(context: Context): Int {
                return (this * context.resources.displayMetrics.density).toInt()
            }

            // Helper function to calculate adaptive heights based on screen size
            private fun getAdaptiveHeights(context: Context): Pair<Int, Int> {
                val displayMetrics = context.resources.displayMetrics
                val screenHeight = displayMetrics.heightPixels

                val minHeight = (screenHeight * 0.12).toInt()
                val maxHeight = (screenHeight * 0.35).toInt()

                return Pair(minHeight, maxHeight)
            }

            // Helper function to get a constrained height within min/max bounds
            private fun getConstrainedHeight(context: Context, preferredHeight: Int): Int {
                val (minHeight, maxHeight) = getAdaptiveHeights(context)
                return preferredHeight.coerceIn(minHeight, maxHeight)
            }

            // Helper function to setup consistent +N count styling
            private fun setupCountTextViewStyling(context: Context, countText: String) {
                countTextView.visibility = View.VISIBLE
                countTextView.text = countText
                countTextView.textSize = 32f
                countTextView.setTextColor(Color.WHITE)
                countTextView.setPadding(12, 4, 12, 4)

                val background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 16f
                    setColor(Color.parseColor("#80000000"))
                }
                countTextView.background = background

                when (val params = countTextView.layoutParams) {
                    is ConstraintLayout.LayoutParams -> {
                        params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                        params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                        params.marginEnd = 8.dpToPx(context) // Matches XML layout_marginEnd="8dp"
                        params.bottomMargin = 8.dpToPx(context) // Matches XML layout_marginBottom="8dp"
                        countTextView.layoutParams = params
                    }
                    is FrameLayout.LayoutParams -> {
                        params.gravity = Gravity.BOTTOM or Gravity.END
                        params.marginEnd = 8.dpToPx(context) // Matches XML layout_marginEnd="8dp"
                        params.bottomMargin = 8.dpToPx(context) // Matches XML layout_marginBottom="8dp"
                        countTextView.layoutParams = params
                    }
                    is ViewGroup.MarginLayoutParams -> {
                        params.marginEnd = 8.dpToPx(context) // Matches XML layout_marginEnd="8dp"
                        params.bottomMargin = 8.dpToPx(context) // Matches XML layout_marginBottom="8dp"
                        countTextView.layoutParams = params
                    }
                }
            }

            // Helper function to configure MaterialCardView with proper corner radius for ALL elements
            private fun setupCardViewCorners(context: Context) {
                val cornerRadius = 8.dpToPx(context).toFloat()

                materialCardView.radius = cornerRadius
                materialCardView.clipToOutline = true
                materialCardView.clipChildren = true
                materialCardView.cardElevation = 0f
                materialCardView.maxCardElevation = 0f
                materialCardView.strokeWidth = 0
                materialCardView.setContentPadding(0, 0, 0, 0)
                materialCardView.useCompatPadding = false
                materialCardView.setCardBackgroundColor(Color.WHITE)

                imageView.clipToOutline = true
                imageView.outlineProvider = object : ViewOutlineProvider() {
                    override fun getOutline(view: View, outline: Outline) {
                        outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
                    }
                }
                val imageLayoutParams = imageView.layoutParams as FrameLayout.LayoutParams
                imageLayoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT
                imageLayoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT
                imageLayoutParams.setMargins(0, 0, 0, 0)
                imageView.layoutParams = imageLayoutParams

                imageView2.clipToOutline = true
                imageView2.outlineProvider = object : ViewOutlineProvider() {
                    override fun getOutline(view: View, outline: Outline) {
                        outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
                    }
                }

                fileTypeIcon.clipToOutline = true
                fileTypeIcon.outlineProvider = object : ViewOutlineProvider() {
                    override fun getOutline(view: View, outline: Outline) {
                        outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
                    }
                }

                playButton.clipToOutline = true
                playButton.outlineProvider = object : ViewOutlineProvider() {
                    override fun getOutline(view: View, outline: Outline) {
                        outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
                    }
                }

                feedVideoImageView.clipToOutline = true
                feedVideoImageView.outlineProvider = object : ViewOutlineProvider() {
                    override fun getOutline(view: View, outline: Outline) {
                        outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
                    }
                }

                countTextView.clipToOutline = true
                countTextView.outlineProvider = object : ViewOutlineProvider() {
                    override fun getOutline(view: View, outline: Outline) {
                        outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
                    }
                }

                feedVideoDurationTextView.clipToOutline = true
                feedVideoDurationTextView.outlineProvider = object : ViewOutlineProvider() {
                    override fun getOutline(view: View, outline: Outline) {
                        outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
                    }
                }
            }

            // Helper function to get AppCompatActivity from context
            private fun getActivityFromContext(context: Context): AppCompatActivity? {
                return when (context) {
                    is AppCompatActivity -> context
                    is ContextWrapper -> getActivityFromContext(context.baseContext)
                    else -> null
                }
            }

            // Helper function to check if a file is a document
            private fun isDocument(mimeType: String): Boolean {
                return mimeType.contains("pdf") || mimeType.contains("docx") ||
                        mimeType.contains("pptx") || mimeType.contains("xlsx") ||
                        mimeType.contains("ppt") || mimeType.contains("xls") ||
                        mimeType.contains("txt") || mimeType.contains("rtf") ||
                        mimeType.contains("odt") || mimeType.contains("csv")
            }

            // Helper function to get the correct file index for fileSize == 3
            private fun getCorrectFileIndex(
                data: OriginalPost,
                currentPosition: Int
            ): Int {
                if (data.files.size != 3) return currentPosition

                var documentIndex = -1
                data.fileTypes.forEachIndexed { index, fileType ->
                    if (isDocument(fileType.fileType)) {
                        documentIndex = index
                        return@forEachIndexed
                    }
                }

                if (documentIndex == -1) return currentPosition

                return when (currentPosition) {
                    0 -> documentIndex
                    1 -> if (documentIndex == 0) 1 else if (documentIndex == 1) 0 else 1
                    2 -> if (documentIndex == 2) 1 else 2
                    else -> currentPosition
                }
            }

            private fun navigateToTappedFilesFragment(
                context: Context,
                currentIndex: Int,
                files: List<File>,
                fileIds: List<String>
            ) {
                val activity = getActivityFromContext(context)
                if (activity != null) {
                    activity.findViewById<View>(R.id.topBar)?.visibility = View.GONE
                    activity.findViewById<View>(R.id.bottomNavigationView)?.visibility = View.GONE

                    val fragment = Tapped_Files_In_The_Container_View_Fragment()

                    val bundle = Bundle().apply {
                        putInt("current_index", currentIndex)
                        putInt("total_files", files.size)

                        val fileUrls = ArrayList<String>()
                        files.forEach { file -> fileUrls.add(file.url) }
                        putStringArrayList("file_urls", fileUrls)
                        putStringArrayList("file_ids", ArrayList(fileIds))

                        val postItems = ArrayList<PostItem>()
                        files.forEachIndexed { index, file ->
                            val postItem = PostItem(
                                audioUrl = file.url,
                                audioThumbnailUrl = null,
                                videoUrl = file.url,
                                videoThumbnailUrl = null,
                                postId = fileIds.getOrNull(index) ?: "file_$index",
                                data = "Post data for file $index",
                                files = arrayListOf(file.url)
                            )
                            postItems.add(postItem)
                        }
                        putParcelableArrayList("post_list", postItems)
                        putString("post_id", fileIds.getOrNull(currentIndex) ?: "file_$currentIndex")
                    }

                    fragment.arguments = bundle

                    activity.supportFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left
                        )
                        .replace(R.id.frame_layout, fragment)
                        .addToBackStack("tapped_files_view")
                        .commit()
                }
            }

            @SuppressLint("SetTextI18n", "UseKtx")
            fun onBind(data: OriginalPost) {

                val context = itemView.context
                setupCardViewCorners(context)
                itemView.setBackgroundColor(Color.TRANSPARENT)

                val displayMetrics = context.resources.displayMetrics
                val screenWidth = displayMetrics.widthPixels
                val margin = 4.dpToPx(context)
                val spaceBetweenRows = 4.dpToPx(context)

                val (minHeight, maxHeight) = getAdaptiveHeights(context)

                // Check if this is the special case: 3 files with 2 documents
                val isSpecial3FileCase = data.files.size == 3 && data.fileTypes.count { isDocument(it.fileType) } == 2

                // Get the actual file index based on the scenario
                val actualFileIndex = if (isSpecial3FileCase) {
                    // For 2-document case, map positions directly to document indices
                    val documentIndices = data.fileTypes.mapIndexed { index, fileType ->
                        if (isDocument(fileType.fileType)) index else -1
                    }.filter { it != -1 }

                    when (absoluteAdapterPosition) {
                        0 -> documentIndices[0]
                        1 -> documentIndices[1]
                        else -> documentIndices[0] // Fallback
                    }
                } else if (data.files.size == 3) {
                    getCorrectFileIndex(data, absoluteAdapterPosition)
                } else {
                    absoluteAdapterPosition
                }

                val fileIdToFind = data.fileIds[actualFileIndex]
                val file = data.files.find { it.fileId == fileIdToFind }
                val fileUrl = file?.url ?: data.files.getOrNull(actualFileIndex)?.url ?: ""
                val mimeType = data.fileTypes.getOrNull(actualFileIndex)?.fileType ?: ""
                val durationItem = data.duration?.find { it.fileId == fileIdToFind }
                feedVideoDurationTextView.text = durationItem?.duration

                // Determine effective file size for layout logic
                val fileSize = if (isSpecial3FileCase) {
                    2 // Treat as 2-file layout
                } else {
                    itemCount
                }

                // Reset visibility states
                playButton.visibility = View.GONE
                feedVideoImageView.visibility = View.GONE
                feedVideoDurationTextView.visibility = View.VISIBLE
                imageView2.visibility = View.GONE
                countTextView.visibility = View.GONE

                // Set click listeners
                val clickListener = View.OnClickListener {
                    navigateToTappedFilesFragment(
                        context,
                        actualFileIndex,
                        data.files,
                        data.fileIds as List<String>
                    )
                }

                itemView.setOnClickListener(clickListener)
                imageView.setOnClickListener(clickListener)
                materialCardView.setOnClickListener(clickListener)
                countTextView.setOnClickListener(clickListener)
                imageView2.setOnClickListener(clickListener)
                feedVideoImageView.setOnClickListener(clickListener)

                val layoutParams = materialCardView.layoutParams as ViewGroup.MarginLayoutParams

                when (fileSize) {

                    2 -> {
                        // This handles both actual 2-file posts AND the special 3-file with 2-document case
                        val documentWidth = (screenWidth * 0.45).toInt() // Reduced width
                        layoutParams.width = documentWidth
                        layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.75).toInt())
                        layoutParams.topMargin = 0
                        layoutParams.bottomMargin = 0

                        val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
                        layoutParams.leftMargin = if (isLeftColumn) margin else (spaceBetweenRows/2)
                        layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows/2) else margin

                        // CRITICAL: Show +1 count on the right item ONLY if this is the special 3-file case
                        if (isSpecial3FileCase && absoluteAdapterPosition == 1) {
                            setupCountTextViewStyling(context, "+1")
                        } else {
                            countTextView.visibility = View.GONE
                        }

                        loadFileContent(fileUrl, mimeType, data, fileIdToFind.toString(), context)
                    }

                    3 -> {
                        // Normal 3-file cases (NOT the 2-document case)
                        when (absoluteAdapterPosition) {
                            0 -> {
                                layoutParams.width = screenWidth / 2
                                val baseFileHeight = getConstrainedHeight(context, (maxHeight * 0.8).toInt())
                                val rightSideItemHeight = baseFileHeight / 2
                                val totalRightSideHeight = (rightSideItemHeight * 2) + (spaceBetweenRows / 2)
                                layoutParams.height = totalRightSideHeight
                                layoutParams.leftMargin = 0
                                layoutParams.rightMargin = (spaceBetweenRows/2)
                                layoutParams.topMargin = 0
                                layoutParams.bottomMargin = 0
                            }
                            1 -> {
                                layoutParams.width = screenWidth / 2
                                val baseFileHeight = getConstrainedHeight(context, (maxHeight * 0.8).toInt())
                                val totalHeight = baseFileHeight + (spaceBetweenRows / 2)
                                layoutParams.height = (totalHeight - (spaceBetweenRows / 2)) / 2
                                layoutParams.leftMargin = (spaceBetweenRows/2)
                                layoutParams.rightMargin = 0
                                layoutParams.topMargin = 0
                                layoutParams.bottomMargin = 0
                            }
                            2 -> {
                                layoutParams.width = screenWidth / 2
                                val baseFileHeight = getConstrainedHeight(context, (maxHeight * 0.8).toInt())
                                val totalHeight = baseFileHeight + (spaceBetweenRows / 2)
                                layoutParams.height = (totalHeight - (spaceBetweenRows / 2)) / 2
                                layoutParams.leftMargin = (spaceBetweenRows/2)
                                layoutParams.rightMargin = 0
                                layoutParams.topMargin = (spaceBetweenRows/2)
                                layoutParams.bottomMargin = 0
                            }
                        }
                        itemView.visibility = View.VISIBLE
                        loadFileContent(fileUrl, mimeType, data, fileIdToFind.toString(), context)
                    }

                    4 -> {
                        layoutParams.width = screenWidth / 2
                        layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.75).toInt())

                        layoutParams.topMargin = if (absoluteAdapterPosition < 2) 0 else spaceBetweenRows
                        layoutParams.bottomMargin = if (absoluteAdapterPosition >= 2) 0 else 0

                        val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
                        layoutParams.leftMargin = if (isLeftColumn) 0 else (spaceBetweenRows/2)
                        layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows/2) else 0

                        loadFileContent(fileUrl, mimeType, data, fileIdToFind.toString(), context)
                    }

                    5 -> {
                        if (absoluteAdapterPosition >= 4) {
                            itemView.visibility = View.GONE
                            layoutParams.width = 0
                            layoutParams.height = 0
                            itemView.layoutParams = layoutParams
                            return
                        }

                        itemView.visibility = View.VISIBLE

                        layoutParams.width = screenWidth / 2
                        layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.75).toInt())

                        val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
                        layoutParams.leftMargin = if (isLeftColumn) 0 else (spaceBetweenRows /2)
                        layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows /2) else 0
                        layoutParams.topMargin = if (absoluteAdapterPosition < 2) 0 else spaceBetweenRows
                        layoutParams.bottomMargin = if (absoluteAdapterPosition < 2) spaceBetweenRows / 2 else 0

                        if (absoluteAdapterPosition == 3) {
                            setupCountTextViewStyling(context, "+${fileSize - 4}")
                        }

                        loadFileContent(fileUrl, mimeType, data, fileIdToFind.toString(), context)
                    }

                    else -> { // fileSize > 5
                        if (absoluteAdapterPosition >= 4) {
                            itemView.visibility = View.GONE
                            layoutParams.width = 0
                            layoutParams.height = 0
                            itemView.layoutParams = layoutParams
                            return
                        }

                        itemView.visibility = View.VISIBLE

                        layoutParams.width = screenWidth / 2
                        layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.75).toInt())

                        val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
                        layoutParams.leftMargin = if (isLeftColumn) 0 else spaceBetweenRows
                        layoutParams.rightMargin = if (isLeftColumn) spaceBetweenRows else 0
                        layoutParams.topMargin = if (absoluteAdapterPosition < 2) 0 else spaceBetweenRows / 2
                        layoutParams.bottomMargin = if (absoluteAdapterPosition < 2) spaceBetweenRows / 2 else 0

                        if (absoluteAdapterPosition == 3) {
                            setupCountTextViewStyling(context, "+${fileSize - 4}")
                        }

                        loadFileContent(fileUrl, mimeType, data, fileIdToFind.toString(), context)
                    }
                }

                materialCardView.layoutParams = layoutParams
            }

            private fun loadFileContent(
                fileUrl: String,
                mimeType: String,
                data: OriginalPost,
                fileIdToFind: String,
                context: Context,
                fitImage: Boolean = false
            ) {
                when {
                    mimeType.startsWith("image") -> {
                        loadImage(fileUrl)
                        fileTypeIcon.visibility = View.GONE
                    }

                    mimeType.startsWith("video") -> {
                        loadVideoThumbnail(fileUrl)
                        fileTypeIcon.visibility = View.VISIBLE
                        playButton.visibility = View.VISIBLE
                        feedVideoImageView.visibility = View.VISIBLE

                        val params = fileTypeIcon.layoutParams as FrameLayout.LayoutParams
                        params.gravity = Gravity.BOTTOM or Gravity.START
                        params.marginStart = 8.dpToPx(context)
                        params.bottomMargin = 8.dpToPx(context)
                        fileTypeIcon.layoutParams = params
                    }

                    mimeType.startsWith("audio") -> {
                        imageView.setImageResource(R.drawable.music_icon)
                        imageView.visibility = View.VISIBLE
                        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                        playButton.visibility = View.GONE
                        fileTypeIcon.visibility = View.VISIBLE
                        fileTypeIcon.setImageResource(R.drawable.ic_audio_white_icon)

                        val params = fileTypeIcon.layoutParams as FrameLayout.LayoutParams
                        params.gravity = Gravity.BOTTOM or Gravity.START
                        params.marginStart = 8.dpToPx(context)
                        params.bottomMargin = 8.dpToPx(context)
                        fileTypeIcon.layoutParams = params
                    }

                    mimeType.contains("pdf") || mimeType.contains("docx") ||
                            mimeType.contains("pptx") || mimeType.contains("xlsx") ||
                            mimeType.contains("ppt") || mimeType.contains("xls") ||
                            mimeType.contains("txt") || mimeType.contains("rtf") ||
                            mimeType.contains("odt") || mimeType.contains("csv") -> {

                        val thumbnail = data.thumbnail.find { it.fileId == fileIdToFind }
                        imageView.scaleType = ImageView.ScaleType.CENTER_CROP

                        if (thumbnail != null) {
                            Glide.with(itemView.context)
                                .load(thumbnail.thumbnailUrl)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .centerCrop()
                                .into(imageView)
                        }

                        fileTypeIcon.setImageResource(
                            when {
                                mimeType.contains("pdf") -> R.drawable.pdf_icon
                                mimeType.contains("docx") -> R.drawable.word_icon
                                mimeType.contains("pptx") -> R.drawable.powerpoint_icon
                                mimeType.contains("xlsx") -> R.drawable.excel_icon
                                mimeType.contains("ppt") -> R.drawable.powerpoint_icon
                                mimeType.contains("xls") -> R.drawable.excel_icon
                                mimeType.contains("txt") -> R.drawable.text_icon
                                mimeType.contains("rtf") -> R.drawable.text_icon
                                mimeType.contains("odt") -> R.drawable.word_icon
                                mimeType.contains("csv") -> R.drawable.excel_icon
                                else -> R.drawable.text_icon
                            }
                        )
                        fileTypeIcon.visibility = View.VISIBLE
                        imageView.visibility = View.VISIBLE
                    }
                    else -> {
                        imageView.setImageResource(R.drawable.feed_mixed_image_view_rounded_corners)
                        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                        fileTypeIcon.visibility = View.GONE
                    }
                }
            }

            private fun loadImage(url: String) {
                Glide.with(itemView.context)
                    .load(url)
                    .placeholder(R.drawable.flash21)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(imageView)

                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                imageView.clipToOutline = true
            }

            private fun loadVideoThumbnail(url: String) {
                Glide.with(itemView.context)
                    .asBitmap()
                    .load(url)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(imageView)

                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                imageView.clipToOutline = true
            }
        }


    }





    private fun navigateToTappedFilesFragment(
        context: Context,
        currentIndex: Int,
        files: List<File>,
        fileIds: List<String>

    ) {
        val activity = getActivityFromContext(context)
        if (activity != null) {
            // Create the fragment instance
            val fragment = Tapped_Files_In_The_Container_View_Fragment()

            // Create bundle to pass data to the fragment
            val bundle = Bundle().apply {
                putInt("current_index", currentIndex)
                putInt("total_files", files.size)

                // Convert files to ArrayList of URLs for easy passing
                val fileUrls = ArrayList<String>()
                files.forEach { file ->
                    fileUrls.add(file.url)
                }
                putStringArrayList("file_urls", fileUrls)
                putStringArrayList("file_ids", ArrayList(fileIds))

                // Create PostItem list for the ViewPager
                val postItems = ArrayList<PostItem>()
                files.forEachIndexed { index, file ->
                    val postItem = PostItem(
                        audioUrl = file.url,
                        audioThumbnailUrl = null,
                        videoUrl = file.url,
                        videoThumbnailUrl = null,
                        postId = fileIds.getOrNull(index) ?: "file_$index",
                        data = "Post data for file $index",
                        files = arrayListOf(file.url)
                    )
                    postItems.add(postItem)
                }
                putParcelableArrayList("post_list", postItems)

                // Set a default post ID
                putString("post_id", fileIds.getOrNull(currentIndex) ?: "file_$currentIndex")
            }

            fragment.arguments = bundle

            // Navigate to the fragment with animation
            activity.supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .replace(R.id.frame_layout, fragment)
                .addToBackStack("tapped_files_view")
                .commit()

            Log.d(
                TAG,
                "Navigated to Tapped_Files_In_The_Container_View with ${files.size} " +
                        "files, starting at index $currentIndex"
            )
        } else {
            Log.e(TAG, "Activity is null, cannot navigate to fragment")
        }
    }

    private fun getActivityFromContext(context: Context): AppCompatActivity? {
        return when (context) {
            is AppCompatActivity -> context
            is ContextWrapper -> getActivityFromContext(context.baseContext)
            else -> null
        }
    }

    private fun setupRecyclerViews() {
        recyclerViews.layoutManager = GridLayoutManager(requireContext(), 3)
        recyclerViews.isNestedScrollingEnabled = false
        recyclerViews.layoutManager = GridLayoutManager(requireContext(), 3)
        recyclerViews.isNestedScrollingEnabled = false
    }

    private fun handleRepostFileClick() {
        post?.let { currentPost ->
            if (currentPost.files.isNotEmpty()) {
                val files = currentPost.files.map { file ->
                    File(
                        _id = file._id,
                        fileId = file.fileId,
                        localPath = file.localPath,
                        url = file.url,
                     //   type = file.type,
                        mimeType = file.url,
                     //   fileType = file.fileType
                    ).apply {
                     //   url = file.url
                     //   mimeType = file.mimeType
                    }
                }
                val fileIds = currentPost.files.map { it ?: "unknown_id" }
                navigateToTappedFilesFragment(
                    requireContext(),
                    0, files, fileIds as List<String>
                )
            }
        }
    }

    private fun handleOriginalFileClick() {
        post?.originalPost?.firstOrNull()?.let { originalPost ->
            if (originalPost.files.isNotEmpty()) {
                val files = originalPost.files.map { file ->
                    File(
                        _id = file._id,
                        fileId = file.fileId,
                        localPath = file.localPath,
                        url = file.url,
                      //  type = file.type,
                        mimeType = file.mimeType,
                       // fileType = file.fileType
                    )
                }

                val fileIds = originalPost.files.map { it.fileId ?: "unknown_id" }
                navigateToTappedFilesFragment(requireContext(), 0, files, fileIds)
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        // Reset navigation flag when fragment is detached
        isNavigating = false
    }

    private fun handleRepostMediaClick() {
        post?.let { currentPost ->
            if (currentPost.files.isNotEmpty()) {
                val files = currentPost.files.map { file ->
                    File(
                        _id = file._id,
                        fileId = file.fileId,
                        localPath = file.localPath,
                        url = file.url,
                       // type = file.type,
                        mimeType = file.url,
                       // fileType = file.fileType
                    ).apply {
                      //  url = file.url
                      //  mimeType = file.mimeType
                    }
                }
                val fileIds = currentPost.files.map { it ?: "unknown_id" }
                navigateToTappedFilesFragment(requireContext(), 0, files, fileIds as List<String>)
            }
        }
    }

    private fun updateInteractionStates(post: OriginalPost) {
       // updateLikeUI(post.isLikedCount)
        updateFavoriteUI(post.bookmarks.isNotEmpty())
        updateFollowButtonUI()
        repostPost.setImageResource(R.drawable.retweet)
    }

    private fun updateFollowButtonUI() {
        if (isFollowing) {
            // Hide the button when following
            followButton.visibility = View.GONE
        } else {
            // Show the button when not following
            followButton.visibility = View.VISIBLE
            followButton.text = "Follow"
            followButton.setBackgroundResource(R.drawable.shorts_following_button)
        }
    }

    private fun toggleFollow() {
        isFollowing = !isFollowing
        updateFollowButtonUI()

        originalPost?.let { post ->
            val userToFollow = when {
                post.originalPost.isNotEmpty() -> {
                    post.originalPost[0].author
                }
                else -> {
                    post.author.account.username
                }
            }

            showToast(
                if (isFollowing) "Now following $userToFollow"
                else "Unfollowed $userToFollow"
            )
        }
    }

    private fun formattedMongoDateTime(dateTimeString: String?): String {

        if (dateTimeString.isNullOrBlank()) return "now"
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
            val diffInMonths = diffInDays / 30 // Approximate
            val diffInYears = diffInDays / 365 // Approximate

            when {
                diffInSeconds < 60 -> "now"
                diffInMinutes < 60 -> "${diffInMinutes}m"
                diffInHours < 24 -> "${diffInHours}h"
                diffInDays == 1L -> "1d"
                diffInDays < 7 -> "${diffInDays}d"
                diffInWeeks == 1L -> "1w"
                diffInWeeks < 4 -> "${diffInWeeks}w"
                diffInMonths == 1L -> "a month ago"
                diffInMonths < 12 -> "${diffInMonths}months"
                diffInYears == 1L -> "1y"
                else -> "${diffInYears}y"
            }
        } catch (e: Exception) {
            Log.w("DateFormat", "Failed to format date: $dateTimeString", e)
            "Unknown Time"
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }


}




