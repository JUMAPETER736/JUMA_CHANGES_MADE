package com.uyscuti.social.circuit.User_Interface.fragments.feed.feedviewfragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Outline
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
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
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.util.UnstableApi
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson
import com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.feedRepost.PostItem
import com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.feedRepost.Tapped_Files_In_The_Container_View_Fragment
import com.uyscuti.social.circuit.MainActivity
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.adapter.feed.multiple_files.OnMultipleFilesClickListener
import com.uyscuti.social.circuit.databinding.FragmentOriginalPostWithoutRepostInsideBinding
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.stream.MalformedJsonException
import com.uyscuti.social.network.api.response.posts.OriginalPost
import com.uyscuti.social.network.api.response.posts.Post
import com.uyscuti.social.network.api.response.posts.Duration
import com.uyscuti.social.network.api.response.posts.ThumbnailX
import com.uyscuti.social.network.api.response.posts.File
import com.uyscuti.social.network.api.response.posts.FileType
import java.text.SimpleDateFormat
import java.util.*
import com.uyscuti.social.business.retro.model.User
import com.uyscuti.social.circuit.User_Interface.OtherUserProfile.OtherUserProfileAccount
import com.uyscuti.social.circuit.model.FeedCommentClicked
import com.uyscuti.social.network.utils.LocalStorage
import retrofit2.Call
import retrofit2.Callback
import kotlin.math.abs
import com.uyscuti.social.circuit.User_Interface.fragments.feed.feedviewfragments.editRepost.Fragment_Edit_Post_To_Repost
import com.uyscuti.social.circuit.data.model.shortsmodels.OtherUsersProfile
import com.uyscuti.social.circuit.model.GoToUserProfileFragment
import com.uyscuti.social.core.common.data.room.entity.FollowUnFollowEntity
import com.uyscuti.social.network.api.response.posts.Avatar
import com.uyscuti.social.network.api.response.allFeedRepostsPost.BookmarkRequest
import com.uyscuti.social.network.api.response.allFeedRepostsPost.BookmarkResponse
import com.uyscuti.social.network.api.response.allFeedRepostsPost.CommentCountResponse
import com.uyscuti.social.network.api.response.allFeedRepostsPost.CommentsResponse
import com.uyscuti.social.network.api.response.allFeedRepostsPost.LikeRequest
import com.uyscuti.social.network.api.response.allFeedRepostsPost.LikeResponse
import com.uyscuti.social.network.api.response.allFeedRepostsPost.RepostResponse
import com.uyscuti.social.network.api.response.allFeedRepostsPost.RetrofitClient
import com.uyscuti.social.network.api.response.allFeedRepostsPost.ShareResponse
import com.uyscuti.social.network.api.response.comment.allcomments.Comment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.Response




class Fragment_Original_Post_Without_Repost_Inside : Fragment(), OnMultipleFilesClickListener {

    companion object {
        private const val TAG = "Fragment_Original_Post_Without_Repost_Inside"
        internal const val ARG_ORIGINAL_POST = "ARG_ORIGINAL_POST"
        private const val ARG_POST = "ARG_POST"

        fun newInstance(

            data: com.uyscuti.social.network.api.response.posts.Post,
            clickListener: com.uyscuti.social.circuit.adapter.feed.OnFeedClickListener? = null
            
        ): Fragment_Original_Post_Without_Repost_Inside {
            return Fragment_Original_Post_Without_Repost_Inside().apply {
                arguments = Bundle().apply {
                    putString(ARG_ORIGINAL_POST, data.toString())
                    putString("post_data", Gson().toJson(data))
                }
            }
        }
    }

    // View binding
    private var _binding: FragmentOriginalPostWithoutRepostInsideBinding? = null
    private val binding get() = _binding!!

    // Navigation flags
    private var isNavigating = false
    private var isNavigatingBack = false

    // Data
    private var post: Post? = null
    private var originalPost: OriginalPost? = null
    private var currentPost: Post? = null
    private var currentPosition: Int = 0

    // Counters

    private var totalMixedComments = 0
    private var totalMixedLikesCounts = 0
    private var totalMixedBookMarkCounts = 0
    private var totalMixedShareCounts = 0
    private var totalMixedRePostCounts = 0


    // Views - using lateinit with proper checking
    private lateinit var itemView: View
    private lateinit var cancelButton: ImageView
    private lateinit var headerTitle: TextView
    private lateinit var headerMenuButton: ImageView

    // Original Post Views
    private lateinit var quotedPostCard: CardView
    private lateinit var originalPostContainer: LinearLayout
    private lateinit var originalPosterProfileImage: ImageView
    private lateinit var originalPosterName: TextView
    private lateinit var tvQuotedUserHandle: TextView
    private lateinit var dateTime: TextView
    private lateinit var originalPostText: TextView
    private lateinit var tvQuotedHashtags: TextView
    private lateinit var mixedFilesCardView: CardView
    private lateinit var originalFeedImage: ImageView
    private lateinit var videoContainer: LinearLayout
    private lateinit var multipleAudiosContainer: LinearLayout
    private lateinit var recyclerViews: RecyclerView
    private lateinit var ivQuotedPostImage: ImageView

    // Action Button Views
    private lateinit var likeSection: LinearLayout
    private lateinit var likeButtonIcon: ImageView
    private lateinit var likesCount: TextView
    private lateinit var commentSection: LinearLayout
    private lateinit var commentButtonIcon: ImageView
    private lateinit var commentCount: TextView
    private lateinit var favoriteSection: LinearLayout
    private lateinit var favoritesButton: ImageView
    private lateinit var favoriteCounts: TextView
    private lateinit var retweetSection: LinearLayout
    private lateinit var repostPost: ImageView
    private lateinit var repostCount: TextView
    private lateinit var shareSection: LinearLayout
    private lateinit var shareButtonIcon: ImageView
    private lateinit var shareCount: TextView
    private lateinit var followButton: AppCompatButton
    private var isFollowing = false

    private fun handleFollowButtonClick() = toggleFollow()
    private val navigationHandler = Handler(Looper.getMainLooper())
    private var isNavigationInProgress = false

    private val mainActivity: MainActivity?
        @OptIn(UnstableApi::class)
        get() = activity as? MainActivity



    private val Post.safeRepostCount: Int
        get() = repostCount ?: 0

    private val Post.safeLikes: Int
        get() = likes ?: 0

    private val Post.safeCommentCount: Int
        get() = comments ?: 0

    private val Post.safeBookmarkCount: Int
        get() = bookmarkCount ?: 0

    private val Post.safeShareCount: Int
        get() = shareCount ?: 0



    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCommentsLoaded(event: CommentsLoadedEvent) {
        Log.d(TAG, "onCommentsLoaded: Received comments loaded event with ${event.commentCount} comments for post ${event.postId}")

        // Check if this event is for the current post
        val currentPostId = if (currentPost?.originalPost?.isNotEmpty() == true) {
            currentPost?.originalPost?.get(0)?._id
        } else {
            currentPost?._id
        }

        if (currentPostId == event.postId) {
            Log.d(TAG, "onCommentsLoaded: Updating UI for matching post")
            updateCommentCount(event.commentCount)
        } else {
            Log.d(TAG, "onCommentsLoaded: Event for different post, ignoring")
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCommentCountUpdated(event: CommentCountUpdatedEvent) {
        Log.d(TAG, "onCommentCountUpdated: Received count ${event.commentCount} for post ${event.postId}")

        // Check if this event is for the current post
        val currentPostId = if (currentPost?.originalPost?.isNotEmpty() == true) {
            currentPost?.originalPost?.get(0)?._id
        } else {
            currentPost?._id
        }

        if (currentPostId == event.postId) {
            Log.d(TAG, "onCommentCountUpdated: Updating UI for matching post")
            updateCommentCount(event.commentCount)
        }
    }

    fun updateCommentCount(newCount: Int) {
        Log.d(TAG, "updateCommentCount: Updating comment count from $totalMixedComments to $newCount")

        val previousCount = totalMixedComments
        totalMixedComments = if (newCount < 0) {
            Log.w(TAG, "updateCommentCount: Negative count received, setting to 0")
            0
        } else {
            newCount
        }

        // Update the post object
        currentPost?.let { post ->
            post.comments = totalMixedComments
            post.comments = totalMixedComments
            Log.d(TAG, "updateCommentCount: Updated post object with count $totalMixedComments")
        }

        // CRITICAL: Always update UI display on main thread
        if (Thread.currentThread() != Looper.getMainLooper().thread) {
            Handler(Looper.getMainLooper()).post {
                updateMetricDisplay(commentCount, totalMixedComments, "comment")
            }
        } else {
            updateMetricDisplay(commentCount, totalMixedComments, "comment")
        }

        // Animate only if count actually changed
        if (previousCount != totalMixedComments) {
            YoYo.with(Techniques.Pulse)
                .duration(500)
                .playOn(commentCount)
        }

        Log.d(TAG, "updateCommentCount: Successfully updated UI to $totalMixedComments")
    }

    private fun handleFeedCommentClicked(position: Int, post: Post?) {
        Log.d(TAG, "handleFeedCommentClicked: Posting comment event for post ${post?._id}")
        try {
            EventBus.getDefault().post(FeedCommentClicked(position,
                post as com.uyscuti.social.network.api.response.posts.Post
            ))

            // Immediately try to refresh comment count from server
            val postIdToFetch = if (post?.originalPost?.isNotEmpty() == true) {
                post?.originalPost[0]?._id ?: 0
            } else {
                post?._id ?: 0
            }

            // Delay the fetch slightly to allow UI to settle
            Handler(Looper.getMainLooper()).postDelayed({
                fetchAndUpdateCommentCount(postIdToFetch.toString())
            }, 500)

        } catch (e: Exception) {
            Log.e(TAG, "Error posting comment event: ${e.message}")
            e.printStackTrace()
        }
    }


    override fun onResume() {
        super.onResume()

        post?.let { safePost ->
            val postIdToFetch = if (safePost.originalPost.isNotEmpty() == true) {
                safePost.originalPost[0]._id
            } else {
                safePost._id
            }
            if (totalMixedComments == 0) { // Only fetch if count is stale
                fetchAndUpdateCommentCount(postIdToFetch)
            }
        }
    }

    private val feedClickListener: com.uyscuti.social.circuit.adapter.feed.OnFeedClickListener by lazy {
        (activity as? com.uyscuti.social.circuit.adapter.feed.OnFeedClickListener) ?:
        object : com.uyscuti.social.circuit.adapter.feed.OnFeedClickListener {

            override fun likeUnLikeFeed(position: Int, post: com.uyscuti.social.network.api.response.posts.Post) {
                Log.d(TAG, "feedClickListener: likeUnLikeFeed position $position for post ${post._id}")
            }

            override fun feedCommentClicked(
                position: Int,
                data: com.uyscuti.social.network.api.response.posts.Post
            ) {
                Log.d(TAG, "feedClickListener: feedCommentClicked position $position for post ${post?._id}")
                handleFeedCommentClicked(position, post)
            }

            override fun feedFavoriteClick(position: Int, post: com.uyscuti.social.network.api.response.posts.Post) {
                Log.d(TAG, "feedClickListener: feedFavoriteClick position $position for post ${post._id}")
            }

            override fun moreOptionsClick(
                position: Int,
                data: com.uyscuti.social.network.api.response.posts.Post
            ) {
                Log.d(TAG, "feedClickListener: moreOptionsClick position $position for post ${data._id}")
            }

            override fun feedFileClicked(
                position: Int,
                data: com.uyscuti.social.network.api.response.posts.Post
            ) {
                Log.d(TAG, "feedClickListener: feedFileClicked position $position for post ${data._id}")
            }

            override fun feedRepostFileClicked(position: Int, data: OriginalPost) {
                Log.d(TAG, "feedClickListener: feedRepostFileClicked position $position")
            }

            override fun feedShareClicked(
                position: Int,
                data: com.uyscuti.social.network.api.response.posts.Post
            ) {
                Log.d(TAG, "feedClickListener: feedShareClicked position $position for post ${post?._id}")
            }

            override fun followButtonClicked(
                followUnFollowEntity: FollowUnFollowEntity,
                followButton: AppCompatButton
            ) {
                Log.d(TAG, "feedClickListener: followButtonClicked")
            }

            override fun feedRepostPost(position: Int, post: com.uyscuti.social.network.api.response.posts.Post) {
                Log.d(TAG, "feedClickListener: feedRepostPost position $position for post ${post._id}")
            }

            override fun feedRepostPostClicked(
                position: Int,
                data: com.uyscuti.social.network.api.response.posts.Post
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
    private lateinit var postMediaHandler: PostMediaHandler

    override fun multipleFileClickListener(currentIndex: Int, files: List<File>, fileIds: List<String>) {
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
        _binding = FragmentOriginalPostWithoutRepostInsideBinding.inflate(inflater, container, false)
        return binding.root
    }

    @OptIn(UnstableApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            // Initialize views first
            initializeViews(view)
            initializeRecyclerView()
            setupInteractionButtonsClickPrevention()

            // Verify views are initialized
            if (!isViewsInitialized()) {
                Log.e(TAG, "Critical views not initialized properly")
                return
            }

            // Setup other components
            setupRecyclerViews()
            setupBackNavigation()

            // Hide UI elements
            (activity as? MainActivity)?.hideAppBar()
            (activity as? MainActivity)?.hideBottomNavigation()

            // UPDATED: Get post data - try JSON string first, then fallback to Serializable
            post = try {
                val postJson = arguments?.getString(ARG_ORIGINAL_POST)
                if (postJson != null) {
                    Gson().fromJson(postJson, Post::class.java)
                } else {
                    // Fallback to old method for backward compatibility
                    arguments?.getSerializable(ARG_ORIGINAL_POST) as? Post
                        ?: arguments?.getSerializable(ARG_POST) as? Post
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing post data", e)
                arguments?.getSerializable(ARG_ORIGINAL_POST) as? Post
                    ?: arguments?.getSerializable(ARG_POST) as? Post
            }

            post?.let { postData ->

                Log.d(TAG, "Post ID: ${postData._id}")
                Log.d(TAG, "Post commentCount: ${postData.comments}")
                Log.d(TAG, "Post comments: ${postData.comments}")
                Log.d(TAG, "Post likes: ${postData.likes}")
                Log.d(TAG, "Post safeCommentCount: ${postData.safeCommentCount}")
                Log.d(TAG, "Post safeLikes: ${postData.safeLikes}")

                currentPost = postData

                // Get the actual comment count
                totalMixedComments = if (postData.originalPost.isNotEmpty() == true) {
                    val originalPost = postData.originalPost[0]
                    Log.d(TAG, "Original post comment count: ${originalPost.commentCount}")
                    originalPost.commentCount ?: postData.comments
                } else {
                    postData.comments ?: postData.comments
                }

                Log.d(TAG, "Final totalMixedComments: $totalMixedComments")


                // CRITICAL: Force immediate UI updates with actual values
                Handler(Looper.getMainLooper()).post {
                    Log.d(TAG, "Forcing immediate UI update with values:")
                    Log.d(TAG, "- Comments: $totalMixedComments")
                    Log.d(TAG, "- Likes: ${postData.safeLikes}")
                    Log.d(TAG, "- Bookmarks: ${postData.safeBookmarkCount}")
                    Log.d(TAG, "- Shares: ${postData.safeShareCount}")
                    Log.d(TAG, "- Reposts: ${postData.safeRepostCount}")

                    // Update each metric individually with error handling
                    try {
                        commentCount.text = totalMixedComments.toString()
                        commentCount.visibility = View.VISIBLE
                        commentCount.requestLayout()
                        Log.d(TAG, "Comment count updated to: ${commentCount.text}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating comment count", e)
                    }

                    try {
                        likesCount.text = postData.safeLikes.toString()
                        likesCount.visibility = View.VISIBLE
                        likesCount.requestLayout()
                        Log.d(TAG, "Likes count updated to: ${likesCount.text}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating likes count", e)
                    }

                    try {
                        favoriteCounts.text = postData.safeBookmarkCount.toString()
                        favoriteCounts.visibility = View.VISIBLE
                        favoriteCounts.requestLayout()
                        Log.d(TAG, "Bookmark count updated to: ${favoriteCounts.text}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating bookmark count", e)
                    }

                    try {
                        shareCount.text = postData.safeShareCount.toString()
                        shareCount.visibility = View.VISIBLE
                        shareCount.requestLayout()
                        Log.d(TAG, "Share count updated to: ${shareCount.text}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating share count", e)
                    }

                    try {
                        repostCount.text = postData.safeRepostCount.toString()
                        repostCount.visibility = View.VISIBLE
                        repostCount.requestLayout()
                        Log.d(TAG, "Repost count updated to: ${repostCount.text}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating repost count", e)
                    }

                    // Force parent layout refresh
                    try {
                        (view as? ViewGroup)?.requestLayout()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error refreshing parent layout", e)
                    }
                }

                // Also try the updateMetricDisplay method
                Handler(Looper.getMainLooper()).postDelayed({
                    updateMetricDisplay(commentCount, totalMixedComments, "comment")
                    updateMetricDisplay(likesCount, postData.safeLikes, "like")
                    updateMetricDisplay(favoriteCounts, postData.safeBookmarkCount, "bookmark")
                    updateMetricDisplay(shareCount, postData.safeShareCount, "share")
                    updateMetricDisplay(repostCount, postData.safeRepostCount, "repost")
                }, 100)

                // Populate other data
                populatePostData(postData)

                // Setup buttons
                setupLikeButton(postData)
                setupBookmarkButton(postData)
                setupCommentButton(postData)
                setupShareButton(postData)
                setupRepostButton(postData)
                setupClickListeners(postData)

                // Force another refresh after everything is set up
                Handler(Looper.getMainLooper()).postDelayed({
                    forceRefreshAllMetrics()
                }, 300)

                Log.d(TAG, "Post data populated successfully")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in onViewCreated: ${e.message}", e)
        }
    }

    private fun populatePostData(post: Post) {
        Log.d(TAG, "populatePostData: Starting to populate data for post ${post._id}")
        Log.d(TAG, "populatePostData: Post comments = ${post.comments}")
        Log.d(TAG, "populatePostData: Post likes = ${post.likes}")
        Log.d(TAG, "populatePostData: Post bookmarkCount = ${post.bookmarkCount}")
        Log.d(TAG, "populatePostData: Post shareCount = ${post.shareCount}")
        Log.d(TAG, "populatePostData: Post repostCount = ${post.repostCount}")

        if (post.originalPost?.isNotEmpty() == true) {
            val originalPost = post.originalPost[0]
            Log.d(TAG, "populatePostData: OriginalPost commentCount = ${originalPost.commentCount}")
        }

        try {
            totalMixedComments = post.comments
            updateMetricDisplay(commentCount, totalMixedComments, "comment")

            // Ensure views are initialized before using them
            if (!isViewsInitialized()) {
                Log.e(TAG, "Views not initialized, cannot populate post data")
                return
            }

            // Set header title with safe access
            if (::headerTitle.isInitialized) {
                headerTitle.text = "Post"
            } else {
                Log.w(TAG, "headerTitle not initialized")
            }

            // Initialize and setup media handler based on post type
            postMediaHandler = if (post.originalPost.isNotEmpty()) {
                val repostedPostData = post.originalPost[0]
                Log.d("MediaDebug", "Handling media from original post with ${repostedPostData.files.size} files")
                showRepostHeader(post)

                // **KEY FIX: Populate the original post content and author info for reposts**
                populatePostContent(repostedPostData)
                populateOriginalAuthorInfo(repostedPostData)

                PostMediaHandler(post, repostedPostData)
            } else {
                Log.d("MediaDebug", "Handling media for regular post with ${post.files.size} files")

                // For regular posts, populate normally
                populateRegularPost(post)

                PostMediaHandler(post, null)
            }

            // Setup media views
            postMediaHandler.setupMediaViews()

        } catch (e: Exception) {
            Log.e(TAG, "Error populating post data: ${e.message}", e)
        }
    }

    private fun populatePostContent(post: OriginalPost) {
        try {
            if (::dateTime.isInitialized) {
                dateTime.text = formattedMongoDateTime(post.createdAt)
            }
            if (::originalPostText.isInitialized) {
                originalPostText.text = post.content
            }

            val tagsText = post.tags.filterNotNull().joinToString(" ") { "#$it" }
            populateTagsViews(tagsText)
        } catch (e: Exception) {
            Log.e(TAG, "Error populating post content: ${e.message}", e)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun populateRegularPost(post: Post) {
        try {
            // Handle regular posts (not reposts)
            val userToDisplay = post.author

            // Build proper display name using full name logic
            val displayName = when {
                // Try to build full name first
                userToDisplay.firstName.isNotBlank() && userToDisplay.lastName.isNotBlank() ->
                    "${userToDisplay.firstName} ${userToDisplay.lastName}"
                userToDisplay.firstName.isNotBlank() -> userToDisplay.firstName
                userToDisplay.lastName.isNotBlank() -> userToDisplay.lastName
                // Fall back to account username
                userToDisplay.account.username.isNotBlank() -> userToDisplay.account.username
                // Final fallback
                else -> "Unknown User"
            }

            val userHandle = if (userToDisplay.account.username.isNotBlank()) {
                "@${userToDisplay.account.username}"
            } else {
                "@unknown_user"
            }

            // Set user information with safe initialization checks
            if (::originalPosterName.isInitialized && ::tvQuotedUserHandle.isInitialized) {
                originalPosterName.text = displayName
                tvQuotedUserHandle.text = userHandle
            } else {
                Log.w(TAG, "User display views not initialized")
                if (::originalPosterName.isInitialized) {
                    originalPosterName.text = displayName
                }
                if (::tvQuotedUserHandle.isInitialized) {
                    tvQuotedUserHandle.text = userHandle
                }
            }

            // Load profile image
            userToDisplay.account.avatar.url.let { url ->
                if (::originalPosterProfileImage.isInitialized && url.isNotEmpty()) {
                    loadProfileImage(url, originalPosterProfileImage)
                } else {
                    Log.w(TAG, "Profile image view not initialized or URL empty")
                }
            }

            // Set post content
            if (::originalPostText.isInitialized) {
                originalPostText.text = post.content
                originalPostText.visibility = View.VISIBLE
                Log.d(TAG, "Post content set to: '${post.content}'")
            } else {
                Log.w(TAG, "originalPostText not initialized")
            }

            // Set post date
            if (::dateTime.isInitialized) {
                dateTime.text = formattedMongoDateTime(post.createdAt)
            } else {
                Log.w(TAG, "dateTime not initialized")
            }

            // Set hashtags
            if (::tvQuotedHashtags.isInitialized) {
                val tagsText = post.tags.filterNotNull().joinToString(" ") { "#$it" }
                tvQuotedHashtags.text = tagsText
                tvQuotedHashtags.visibility = if (tagsText.isNotEmpty()) View.VISIBLE else View.GONE
            } else {
                Log.w(TAG, "tvQuotedHashtags not initialized")
            }

            // Populate interaction data
            populatePostInteractionData(post)

            Log.d(TAG, "Regular post populated - Name: '$displayName', Handle: '$userHandle'")

        } catch (e: Exception) {
            Log.e(TAG, "Error populating regular post: ${e.message}", e)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun populateOriginalAuthorInfo(post: OriginalPost) {
        try {
            val originalAuthor = post.author

            // Build display name using the same logic as populateRegularPost
            val displayName = when {
                // Try to build full name first
                originalAuthor.firstName.isNotBlank() && originalAuthor.lastName.isNotBlank() ->
                    "${originalAuthor.firstName} ${originalAuthor.lastName}"
                originalAuthor.firstName.isNotBlank() -> originalAuthor.firstName
                originalAuthor.lastName.isNotBlank() -> originalAuthor.lastName
                // Fall back to account username
                originalAuthor.account.username.isNotBlank() -> originalAuthor.account.username
                // Final fallback
                else -> "Unknown User"
            }

            val userHandle = if (originalAuthor.account.username.isNotBlank()) {
                "@${originalAuthor.account.username}"
            } else {
                "@unknown_user"
            }

            if (::originalPosterName.isInitialized) {
                originalPosterName.text = displayName
            }
            if (::tvQuotedUserHandle.isInitialized) {
                tvQuotedUserHandle.text = userHandle
            }

            originalAuthor.account.avatar?.let { profileUrl ->
                if (::originalPosterProfileImage.isInitialized) {
                    loadProfileImage(profileUrl.toString(), originalPosterProfileImage)
                }
            }

            Log.d(TAG, "Original author info populated - Name: '$displayName', Handle: '$userHandle'")
        } catch (e: Exception) {
            Log.e(TAG, "Error populating original author info: ${e.message}", e)
        }
    }

    @OptIn(UnstableApi::class)
    private fun setupBackNavigation() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d(TAG, "Back pressed - immediate navigation")
                immediateNavigateBack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    @OptIn(UnstableApi::class)
    private fun immediateNavigateBack() {
        // Prevent multiple simultaneous navigation attempts
        if (isNavigationInProgress) {
            Log.d(TAG, "Navigation already in progress, ignoring")
            return
        }

        isNavigationInProgress = true

        try {
            Log.d(TAG, "Starting immediate navigation back")

            // Clean up resources first
            cleanupResources()

            // Restore system UI immediately
            restoreSystemBarsImmediately()

            // Don't use any delays - try to navigate immediately but safely
            performSafeBackNavigation()

        } catch (e: Exception) {
            Log.e(TAG, "Error in immediate navigation", e)
            isNavigationInProgress = false
        }
    }

    private fun performSafeBackNavigation() {
        try {
            // Check if fragment is still valid
            if (!isAdded || isDetached || activity == null) {
                Log.w(TAG, "Fragment not attached, cannot navigate back")
                isNavigationInProgress = false
                return
            }

            val fragmentManager = parentFragmentManager

            // Multiple checks to ensure FragmentManager is ready
            if (fragmentManager.isStateSaved || fragmentManager.isDestroyed) {
                Log.w(TAG, "FragmentManager not ready, cannot navigate")
                isNavigationInProgress = false
                return
            }

            // Check if there's anything to pop
            if (fragmentManager.backStackEntryCount <= 0) {
                Log.d(TAG, "No back stack entries, staying in current state")
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
                    Log.d(TAG, "Standard back navigation successful")
                    isNavigationInProgress = false
                }
            } catch (e: IllegalStateException) {
                Log.w(TAG, "Immediate navigation failed, trying alternative", e)
                // Alternative approach: Post to a different thread
                Thread {
                    try {
                        Thread.sleep(50) // Very short wait
                        requireActivity().runOnUiThread {
                            performDelayedNavigation()
                        }
                    } catch (e2: Exception) {
                        Log.e(TAG, "Thread-based navigation failed", e2)
                        requireActivity().runOnUiThread {
                            isNavigationInProgress = false
                        }
                    }
                }.start()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in performSafeBackNavigation", e)
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
                        Log.d(TAG, "ViewPager navigation delegated to activity")
                        isNavigationInProgress = false
                    } catch (e: Exception) {
                        Log.e(TAG, "Activity navigation failed", e)
                        performDelayedNavigation()
                    }
                }
            } else {
                performDelayedNavigation()
            }
        } catch (e: Exception) {
            Log.e(TAG, "ViewPager navigation failed", e)
            performDelayedNavigation()
        }
    }

    private fun performDelayedNavigation() {
        navigationHandler.postDelayed({
            try {
                if (!isAdded || isDetached || activity == null) {
                    isNavigationInProgress = false
                    return@postDelayed
                }

                val fragmentManager = parentFragmentManager

                if (!fragmentManager.isStateSaved && !fragmentManager.isDestroyed) {
                    if (fragmentManager.backStackEntryCount > 0) {
                        try {
                            fragmentManager.popBackStack()
                            Log.d(TAG, "Delayed navigation successful")
                        } catch (e: Exception) {
                            Log.e(TAG, "Delayed navigation failed", e)
                            // Last resort: just hide the fragment
                            hideFragmentManually()
                        }
                    }
                }
                isNavigationInProgress = false
            } catch (e: Exception) {
                Log.e(TAG, "Delayed navigation error", e)
                isNavigationInProgress = false
            }
        }, 200) // Longer delay for ViewPager2 to settle
    }

    private fun hideFragmentManually() {
        try {
            if (!isAdded || activity == null) {
                return
            }

            val fragmentManager = parentFragmentManager

            if (!fragmentManager.isStateSaved) {
                val transaction = fragmentManager.beginTransaction()
                transaction.hide(this)
                // Use commit instead of commitNow to avoid immediate execution conflicts
                transaction.commit()
                Log.d(TAG, "Fragment hidden manually")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Manual fragment hiding failed", e)
        }
    }

    @OptIn(UnstableApi::class)
    private fun restoreSystemBarsImmediately() {
        try {
            val activity = activity ?: run {
                Log.w(TAG, "Activity is null, skipping system bars restoration")
                return
            }

            if (!isAdded) {
                Log.w(TAG, "Fragment not added, skipping system bars restoration")
                return
            }

            // Restore system bars
            WindowCompat.setDecorFitsSystemWindows(activity.window, true)
            WindowInsetsControllerCompat(activity.window, activity.window.decorView)
                .show(WindowInsetsCompat.Type.systemBars())

            // Restore MainActivity UI elements with safety checks
            (activity as? MainActivity)?.let { mainActivity ->
                try {
                    mainActivity.showAppBar()
                    mainActivity.showBottomNavigation()
                } catch (e: Exception) {
                    Log.e(TAG, "Error showing MainActivity UI elements", e)
                }
            }

            Log.d(TAG, "System bars restored immediately")

        } catch (e: Exception) {
            Log.e(TAG, "Error restoring system bars immediately", e)
        }
    }

    private fun cleanupResources() {
        try {
            _binding?.let { binding ->
                // Clear focus first
                binding.replyInput.clearFocus()

                // Hide keyboard
                val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.replyInput.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }

            Log.d(TAG, "Resources cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }

    private fun cancelPendingNavigations() {
        try {
            navigationHandler.removeCallbacksAndMessages(null)
            isNavigationInProgress = false
            Log.d(TAG, "Pending navigations cancelled")
        } catch (e: Exception) {
            Log.e(TAG, "Error canceling pending navigations", e)
        }
    }

    override fun onDestroyView() {
        cancelPendingNavigations()
        super.onDestroyView()
        try {
            Log.d(TAG, "onDestroyView: Cleaning up binding-dependent resources")
            cleanupResources()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroyView cleanup", e)
        } finally {
            _binding = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            Log.d(TAG, "onDestroy: Final cleanup")
            cancelPendingNavigations()
            isNavigating = false
            isNavigatingBack = false

        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroy", e)
        }
    }


    private fun setupClickListeners(data: Post) {
        Log.d(TAG, "setupClickListeners - Data type: ${data::class.java.simpleName}, ID: ${data._id}")

        cancelButton.setOnClickListener {
            Log.d(TAG, "Cancel button clicked - immediate navigation")
            it.isEnabled = false
            immediateNavigateBack()
            Handler(Looper.getMainLooper()).postDelayed({
                if (isAdded) it.isEnabled = true
            }, 100)
        }
        headerMenuButton.setOnClickListener { handleMenuButtonClick() }
        mixedFilesCardView.setOnClickListener { handleOriginalMediaClick() }
        originalFeedImage.setOnClickListener { handleOriginalFileClick() }
        followButton.setOnClickListener { handleFollowButtonClick() }


        originalPosterProfileImage.setOnClickListener {
            navigateToUserProfile(
                feedOwnerId = data.author?.account?._id ?: "",
                feedOwnerName = data.author?.account?.username ?: "",
                feedOwnerUsername = data.author?.account?.username ?: "",
                profilePicUrl = data.author?.account?.avatar?.url ?: ""
            )
        }
        originalPosterName.setOnClickListener {
            navigateToUserProfile(
                feedOwnerId = data.author?.account?._id ?: "",
                feedOwnerName = data.author?.account?.username ?: "",
                feedOwnerUsername = data.author?.account?.username ?: "",
                profilePicUrl = data.author?.account?.avatar?.url ?: ""
            )
        }

    }

    private fun navigateToFragment(fragment: Fragment, tag: String) {
        try {
            val activity = activity
            if (activity != null) {
                val currentFragment = activity.supportFragmentManager.fragments.lastOrNull {
                    it.isVisible && it.view != null
                }
                val fragmentManager = if (currentFragment != null &&
                    currentFragment.childFragmentManager.fragments.isNotEmpty()
                ) {
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

    private fun isViewsInitialized(): Boolean {
        return try {
            _binding != null &&
                    ::itemView.isInitialized &&
                    ::headerTitle.isInitialized &&
                    ::originalPosterProfileImage.isInitialized &&
                    ::originalPosterName.isInitialized &&
                    ::recyclerViews.isInitialized &&
                    ::multipleAudiosContainer.isInitialized &&
                    ::mixedFilesCardView.isInitialized &&
                    ::originalFeedImage.isInitialized &&
                    ::cancelButton.isInitialized &&
                    ::headerMenuButton.isInitialized &&
                    ::quotedPostCard.isInitialized &&
                    ::originalPostContainer.isInitialized &&
                    ::tvQuotedUserHandle.isInitialized &&
                    ::dateTime.isInitialized &&
                    ::originalPostText.isInitialized &&
                    ::tvQuotedHashtags.isInitialized &&
                    ::ivQuotedPostImage.isInitialized &&
                    ::likeSection.isInitialized &&
                    ::likeButtonIcon.isInitialized &&
                    ::likesCount.isInitialized &&
                    ::commentSection.isInitialized &&
                    ::commentButtonIcon.isInitialized &&
                    ::commentCount.isInitialized &&
                    ::favoriteSection.isInitialized &&
                    ::favoritesButton.isInitialized &&
                    ::favoriteCounts.isInitialized &&
                    ::retweetSection.isInitialized &&
                    ::repostPost.isInitialized &&
                    ::repostCount.isInitialized &&
                    ::shareSection.isInitialized &&
                    ::shareButtonIcon.isInitialized &&
                    ::shareCount.isInitialized &&
                    ::followButton.isInitialized  // Add this line
        } catch (e: Exception) {
            Log.e(TAG, "Views initialization check failed: ${e.message}")
            false
        }
    }

    private fun initializeViews(view: View) {
        itemView = view
        try {
            _binding?.let { safeBinding ->
                cancelButton = safeBinding.cancelButton
                headerTitle = safeBinding.headerTitle
                headerMenuButton = safeBinding.headerMenuButton
                quotedPostCard = safeBinding.quotedPostCard
                originalPostContainer = safeBinding.originalPostContainer
                originalPosterProfileImage = safeBinding.originalPosterProfileImage
                originalPosterName = safeBinding.originalPosterName
                tvQuotedUserHandle = safeBinding.tvQuotedUserHandle
                dateTime = safeBinding.dateTime
                originalPostText = safeBinding.originalPostText
                tvQuotedHashtags = safeBinding.tvQuotedHashtags
                mixedFilesCardView = safeBinding.mixedFilesCardView
                originalFeedImage = safeBinding.originalFeedImage
                multipleAudiosContainer = safeBinding.multipleAudiosContainer
                recyclerViews = safeBinding.recyclerView
                ivQuotedPostImage = safeBinding.ivQuotedPostImage

                likeSection = safeBinding.likeLayout
                likeButtonIcon = safeBinding.likeButtonIcon
                likesCount = safeBinding.likesCount
                commentSection = safeBinding.commentLayout
                commentButtonIcon = safeBinding.commentButtonIcon
                commentCount = safeBinding.commentCount
                favoriteSection = safeBinding.favoriteSection
                favoritesButton = safeBinding.favoritesButton
                favoriteCounts = safeBinding.favoriteCounts
                retweetSection = safeBinding.repostLayout
                repostPost = safeBinding.repostPost
                repostCount = safeBinding.repostCount
                shareSection = safeBinding.shareLayout
                shareButtonIcon = safeBinding.shareButton
                shareCount = safeBinding.shareCount

                followButton = safeBinding.followButton  // Add this line

                Log.d(TAG, "All views initialized successfully")
            } ?: run {
                Log.e(TAG, "Binding is null, cannot initialize views")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views: ${e.message}", e)
        }
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

        // Assuming 'post' is your Post object variable name
        post?.let { currentPost ->
            val userToFollow = when {
                currentPost.originalPost.isNotEmpty() -> {
                    currentPost.originalPost[0].author.account.username
                }
                else -> {
                    currentPost.author.account.username
                }
            }

            showToast(
                if (isFollowing) "Now following $userToFollow"
                else "Unfollowed $userToFollow"
            )
        }
    }

    private fun setupInitialMetrics(post: Post) {
        Log.d(TAG, "setupInitialMetrics: Setting up metrics for post ${post._id}")

        // Initialize other metrics (likes, bookmarks, shares, reposts)
        updateMetricDisplay(likesCount, post.safeLikes, "like")
        updateMetricDisplay(favoriteCounts, post.safeBookmarkCount, "bookmark")
        updateMetricDisplay(shareCount, post.safeShareCount, "share")
        updateMetricDisplay(repostCount, post.safeRepostCount, "repost") // Add this line

        // FIX: Initialize comment count correctly
        val initialCommentCount = if (post.originalPost.isNotEmpty() == true) {
            // For reposted content, get count from original post
            post.originalPost[0].commentCount
        } else {
            // For regular posts
            post.comments ?: post.comments
        }

        totalMixedComments = initialCommentCount
        updateMetricDisplay(commentCount, totalMixedComments, "comment")
        Log.d(TAG, "setupInitialMetrics: Set initial comment count to $totalMixedComments")

        // Update the post object to ensure consistency
        currentPost?.let {
            it.comments = totalMixedComments
            it.comments = totalMixedComments
        }
    }

    fun bind(post: Post, position: Int) {
        this.currentPost = post
        this.currentPosition = position

        Log.d(TAG, "bind: Binding post ${post._id} at position $position")

        // Get initial comment count with proper null handling
        val initialCommentCount = if (post.originalPost.isNotEmpty() == true) {
            val originalPost = post.originalPost[0]
            originalPost.commentCount
        } else {
            post.comments ?: post.comments
        }

        totalMixedComments = initialCommentCount
        Log.d(TAG, "bind: Set initial comment count to $totalMixedComments for post ${post._id}")

        // Setup initial metrics with all counts
        setupInitialMetrics(post)

        // CRITICAL: Force immediate UI update with actual values
        updateMetricDisplay(commentCount, totalMixedComments, "comment")
        updateMetricDisplay(likesCount, post.safeLikes, "like")
        updateMetricDisplay(favoriteCounts, post.safeBookmarkCount, "bookmark")
        updateMetricDisplay(shareCount, post.safeShareCount, "share")
        updateMetricDisplay(repostCount, post.safeRepostCount, "repost")

        // Setup button listeners
        setupLikeButton(post)
        setupBookmarkButton(post)
        setupCommentButton(post)
        setupRepostButton(post)
        setupShareButton(post)

        // Populate other post data
        populatePostData(post)

        // Fetch fresh count from server after UI is updated with initial values
        Handler(Looper.getMainLooper()).postDelayed({
            val postIdToFetch = if (post.originalPost.isNotEmpty() == true) {
                post.originalPost[0]._id
            } else {
                post._id
            }
            fetchAndUpdateCommentCount(postIdToFetch)
        }, 300) // Increased delay to ensure UI is ready
    }

    @SuppressLint("DefaultLocale")
    private fun updateMetricDisplay(textView: TextView, count: Int, metricType: String) {
        Log.d(TAG, "updateMetricDisplay: Updating $metricType display to $count")

        val displayText = when {
            count == 0 -> "0"
            count < 1000 -> count.toString()
            count < 1000000 -> {
                val formatted = count / 1000.0
                if (formatted == formatted.toInt().toDouble()) {
                    "${formatted.toInt()}K"
                } else {
                    String.format("%.1fK", formatted)
                }
            }
            else -> {
                val formatted = count / 1000000.0
                if (formatted == formatted.toInt().toDouble()) {
                    "${formatted.toInt()}M"
                } else {
                    String.format("%.1fM", formatted)
                }
            }
        }

        val updateUI = {
            try {
                textView.text = displayText
                textView.visibility = View.VISIBLE

                // Force layout refresh
                textView.requestLayout()
                textView.invalidate()

                // Force parent to refresh
                (textView.parent as? View)?.requestLayout()

                Log.d(TAG, "updateMetricDisplay: Set $metricType text to '$displayText' - TextView ID: ${textView.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating $metricType display", e)
            }
        }

        if (Thread.currentThread() != Looper.getMainLooper().thread) {

        } else {
            updateUI()
        }

        // Double-check the UI was actually updated
        Handler(Looper.getMainLooper()).postDelayed({
            if (textView.text.toString() != displayText) {
                Log.w(TAG, "UI update failed for $metricType, retrying...")
                textView.text = displayText
                textView.requestLayout()
            }
        }, 100)
    }

    private fun forceRefreshAllMetrics() {
        currentPost?.let { post ->
            Log.d(TAG, "forceRefreshAllMetrics: Forcing refresh of all metric displays")

            Handler(Looper.getMainLooper()).post {
                try {
                    // Force update all metrics with current values
                    updateMetricDisplay(commentCount, totalMixedComments, "comment")
                    updateMetricDisplay(likesCount, post.safeLikes, "like")
                    updateMetricDisplay(favoriteCounts, post.safeBookmarkCount, "bookmark")
                    updateMetricDisplay(shareCount, post.safeShareCount, "share")
                    updateMetricDisplay(repostCount, post.safeRepostCount, "repost")

                    Log.d(TAG, "forceRefreshAllMetrics: All metrics refreshed")
                } catch (e: Exception) {
                    Log.e(TAG, "Error in forceRefreshAllMetrics", e)
                }
            }
        }
    }

    private fun fetchAndUpdateCommentCount(postId: String) {
        Log.d(TAG, "fetchAndUpdateCommentCount: Fetching current comment count for post: $postId")

        RetrofitClient.commentService.getCommentCount(postId)
            .enqueue(object : Callback<CommentCountResponse> {
                override fun onResponse(call: Call<CommentCountResponse>, response: Response<CommentCountResponse>) {
                    if (response.isSuccessful && isAdded) {
                        response.body()?.let { countResponse ->
                            val actualCount = countResponse.count
                            Log.d(TAG, "fetchAndUpdateCommentCount: API returned count: $actualCount for post: $postId")

                            // Check if this is still the current post
                            val currentPostId = if (currentPost?.originalPost?.isNotEmpty() == true) {
                                currentPost?.originalPost?.get(0)?._id
                            } else {
                                currentPost?._id
                            }

                            if (currentPostId == postId) {
                                // Only update if the count has changed to avoid unnecessary UI updates
                                if (totalMixedComments != actualCount) {
                                    Log.d(TAG, "fetchAndUpdateCommentCount: Count changed from $totalMixedComments to $actualCount")

                                    totalMixedComments = actualCount
                                    currentPost?.comments = actualCount
                                    currentPost?.comments = actualCount

                                    updateMetricDisplay(commentCount, actualCount, "comment")

                                    // Add a subtle animation to indicate the count was updated
                                    YoYo.with(Techniques.Pulse)
                                        .duration(300)
                                        .playOn(commentCount)
                                } else {
                                    Log.d(TAG, "fetchAndUpdateCommentCount: Count unchanged at $actualCount")
                                }
                            }
                        }
                    } else {
                        Log.e(TAG, "fetchAndUpdateCommentCount: Failed with code: ${response.code()}")
                        if (response.code() == 404) {
                            // Post might not exist or have no comments
                            updateMetricDisplay(commentCount, 0, "comment")
                        } else {
                            // Fallback to loading comments
                            loadCommentsAndUpdateCount(postId)
                        }
                    }
                }

                override fun onFailure(call: Call<CommentCountResponse>, t: Throwable) {
                    Log.e(TAG, "fetchAndUpdateCommentCount: Network error", t)
                    // Don't override existing count on network failure
                    Log.d(TAG, "fetchAndUpdateCommentCount: Keeping existing count: $totalMixedComments")
                }
            })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCommentAdded(event: CommentAddedEvent) {
        Log.d(TAG, "onCommentAdded: Received event for post ${event.postId}")
        if (currentPost?._id == event.postId) {
            incrementCommentCount()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCommentDeleted(event: CommentDeletedEvent) {
        Log.d(TAG, "onCommentDeleted: Received event for post ${event.postId}")
        if (currentPost?._id == event.postId) {
            decrementCommentCount()
        }
    }

    // 6. Create these event classes if they don't exist
    data class CommentAddedEvent(val postId: String)
    data class CommentDeletedEvent(val postId: String)

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onStop() {
        super.onStop()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    private fun setupCommentButton(data: Post) {
        commentButtonIcon.setOnClickListener {
            if (!commentButtonIcon.isEnabled) return@setOnClickListener

            Log.d(TAG, "setupCommentButton: Comment button clicked for post ${data._id}")

            // Animate the comment button
            YoYo.with(Techniques.Tada)
                .duration(700)
                .repeat(1)
                .playOn(commentButtonIcon)

            // Post event to MainActivity via EventBus
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


    data class CommentsLoadedEvent(
        val postId: String,
        val commentCount: Int,
        val comments: List<Comment>
    )

    data class CommentCountUpdatedEvent(
        val postId: String,
        val commentCount: Int
    )

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCommentEventReceived(event: FeedCommentClicked) {
        Log.d(TAG, "onCommentEventReceived: Comment event received in fragment for post ${event.data?._id}")
    }

    private fun loadCommentsAndUpdateCount(postId: String) {
        Log.d(TAG, "loadCommentsAndUpdateCount: Loading comments to get count for post: $postId")

        RetrofitClient.commentService.getCommentsForPost(postId)
            .enqueue(object : Callback<CommentsResponse> {
                override fun onResponse(call: Call<CommentsResponse>, response: Response<CommentsResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { commentsResponse ->
                            if (commentsResponse.success) {
                                val actualCount = commentsResponse.comments.size
                                Log.d(TAG, "loadCommentsAndUpdateCount: Counted ${actualCount} comments")
                                updateCommentCount(actualCount)
                                currentPost?.comments = actualCount
                            } else {
                                Log.e(TAG, "loadCommentsAndUpdateCount: API returned error: ${commentsResponse.message}")
                            }
                        }
                    } else {
                        Log.e(TAG, "loadCommentsAndUpdateCount: Failed with code: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<CommentsResponse>, t: Throwable) {
                    Log.e(TAG, "loadCommentsAndUpdateCount: Network error", t)
                }
            })
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
                    setData(Uri.parse("smsto:")) // Use setData to set the Intent's data property
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
        totalMixedRePostCounts = data.safeRepostCount
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
        val newCount = maxOf(0, totalMixedComments - 1)
        Log.d(tag, "decrementCommentCount: Decrementing from $totalMixedComments to $newCount")
        updateCommentCount(newCount)
    }


    fun incrementCommentCount() {
        val newCount = totalMixedComments + 1
        Log.d(tag, "incrementCommentCount: Incrementing from $totalMixedComments to $newCount")
        updateCommentCount(newCount)
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


    private fun handleLikeClick() {
        currentPost?.let { post ->
            Log.d(tag, "Like clicked for post: ${post._id}")
        }
    }

    private fun handleCommentClick() {
        currentPost?.let { post ->
            Log.d(tag, "Comment clicked for post: ${post._id}")
        }
    }

    private fun handleFavoriteClick() {
        currentPost?.let { post ->
            Log.d(tag, "Favorite clicked for post: ${post._id}")
        }
    }

    private fun handleRepostClick() {
        currentPost?.let { post ->
            Log.d(tag, "Retweet clicked for post: ${post._id}")
        }
    }

    private fun handleShareClick() {
        currentPost?.let { post ->
            Log.d(tag, "Share clicked for post: ${post._id}")
        }
    }

    private fun handleFollowClick() {
        currentPost?.let { post ->
            Log.d(tag, "Follow clicked for post: ${post._id}")
        }
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

    private fun consumeClickEvent(view: View) {
        view.isPressed = false
        view.parent?.requestDisallowInterceptTouchEvent(true)
    }

    private fun handleMoreOptionsClick() {
        currentPost?.let { post ->
            Log.d(TAG, "More options clicked for post: ${post._id}")
        }
    }

    private fun setupInteractionButtonsClickPrevention() {
        val interactionButtons = listOf(
            likeButtonIcon,
            commentButtonIcon,
            favoritesButton,
            repostPost,
            shareButtonIcon,
           // followButton,
            headerMenuButton
        )

        interactionButtons.forEach { button ->
            button.setOnClickListener { view ->
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                when (view.id) {
                    R.id.likeButtonIcon -> {
                        handleLikeClick()
                        consumeClickEvent(view)
                    }
                    R.id.commentButtonIcon -> {
                        handleCommentClick()
                        consumeClickEvent(view)
                    }
                    R.id.favoriteSection -> {
                        handleFavoriteClick()
                        consumeClickEvent(view)
                    }
                    R.id.repostPost -> {
                        handleRepostClick()
                        consumeClickEvent(view)
                    }
                    R.id.shareButtonIcon -> {
                        handleShareClick()
                        consumeClickEvent(view)
                    }
                    R.id.followButton -> {
                        handleFollowClick()
                        consumeClickEvent(view)
                    }
                    R.id.moreOptions -> {
                        handleMoreOptionsClick()
                        consumeClickEvent(view)
                    }
                }
            }
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

    private fun showRepostHeader(post: Post) {
        // Show who reposted this
        post.repostedUser?.let { repostedUser ->
            // You might want to add a "Reposted by" section in your UI
            Log.d("RepostInfo", "Reposted by: ${repostedUser.username}")


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
                val context = itemView.context // or requireContext() if in Fragment
                if (feedOwnerId == LocalStorage.getInstance(context).getUserId()) {
                    EventBus.getDefault().post(GoToUserProfileFragment())
                    Log.d(TAG, "Navigating to current user's profile: $feedOwnerId")
                } else {
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
                    OtherUserProfileAccount.open(
                        context = context,
                        user = otherUsersProfile,
                        dialogPhoto = profilePicUrl,
                        dialogId = feedOwnerId
                    )
                    Log.d(TAG, "Navigating to other user's profile: $feedOwnerId")
                }
            } else {
                Log.e(TAG, "Fragment not attached, cannot navigate to user profile: $feedOwnerId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to user profile: $feedOwnerId", e)
        }
    }

    private fun initializeRecyclerView() {
        recyclerViews?.let { rv ->
            // Ensure RecyclerView has proper layout parameters
            val layoutParams = rv.layoutParams ?: ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            rv.layoutParams = layoutParams

            // Clear any existing adapter to prevent conflicts
            rv.adapter = null

            Log.d(TAG, "RecyclerView initialized and ready for media content")
        }
    }

    private fun populateTagsViews(tagsText: String) {
        try {
            if (::tvQuotedHashtags.isInitialized) {
                tvQuotedHashtags.text = tagsText
                tvQuotedHashtags.visibility = if (tagsText.isNotEmpty()) View.VISIBLE else View.GONE
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error populating tags views: ${e.message}", e)
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

    private fun updateInteractionStates(post: OriginalPost) {
//        updateLikeUI(post.isLikedCount)
        updateFavoriteUI(post.bookmarks.isNotEmpty())
        if (::repostPost.isInitialized) {
            repostPost.setImageResource(
                if (post.isReposted) R.drawable.retweet else R.drawable.repeat_svgrepo_com
            )
        }
    }


    // Add missing methods
    private fun populatePostInteractionData(post: Post) {
        try {
            if (::likesCount.isInitialized) {
                likesCount.text = formatCount(post.likes)
            }
            if (::commentCount.isInitialized) {
                commentCount.text = formatCount(post.comments)
            }
            if (::repostCount.isInitialized) {
                repostCount.text = formatCount(post.repostCount)
            }
            if (::favoriteCounts.isInitialized) {
                favoriteCounts.text = formatCount(post.bookmarkCount)
            }
            if (::shareCount.isInitialized) {
                shareCount.text = formatCount(post.shareCount)
            }
            updatePostInteractionStates(post)
        } catch (e: Exception) {
            Log.e(TAG, "Error populating post interaction data: ${e.message}", e)
        }
    }

    private fun updatePostInteractionStates(post: Post) {
//        updateLikeUI(post.isLikedCount)
        updateFavoriteUI(post.isBookmarked)
        if (::repostPost.isInitialized) {
            repostPost.setImageResource(
                if (post.isReposted) R.drawable.retweet else R.drawable.repeat_svgrepo_com
            )
        }
    }


    @SuppressLint("DefaultLocale")

    private fun loadProfileImage(url: String, imageView: ImageView) {

        try {
            if (isAdded) {
                Glide.with(itemView.context)
                    .load(url)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.flash21)
                    .error(R.drawable.flash21)
                    .into(imageView)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading profile image: ${e.message}", e)
        }
    }




    private fun navigateToTappedFilesFragment(
        context: Context,
        index: Int,
        files: List<File>,
        fileIds: List<String>) {
        // Navigate to detailed view fragment with file data
        val bundle = Bundle().apply {
            putSerializable("files", ArrayList(files))
            putInt("current_position", index)
            putStringArrayList("file_ids", ArrayList(fileIds))
        }
        val fragment = Tapped_Files_In_The_Container_View_Fragment().apply {
            arguments = bundle
        }
        val fragmentManager = (context as? FragmentActivity)?.supportFragmentManager
        fragmentManager?.beginTransaction()
            ?.replace(R.id.frame_layout, fragment)
            ?.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            ?.addToBackStack("media_detail")
            ?.commit()
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
    }

    private fun handleOriginalMediaClick() {
        post?.let { postData ->
            if (postData.files.isNotEmpty()) {
                val filesList = postData.files//.map { file ->
//                    File(
//                        _id = file._id?.ifBlank { "unknown_id" } ?: "unknown_id",
//                        fileId = file.fileId?.ifBlank { "no_file_id" } ?: "no_file_id",
//                        localPath = file.localPath?.ifBlank { "" } ?: "",
//                        url = file.url?.ifBlank { "" } ?: "",
//                        type = file.type?.ifBlank { "unknown_type" } ?: "unknown_type",
//                        mimeType = file.mimeType?.ifBlank { "" } ?: "",
//                        fileType = ""
//                    )
//                }
                val fileIds = filesList.map { it.fileId }
                navigateToTappedFilesFragment(requireContext(), 0, filesList, fileIds)
            }
        }
    }

    private fun handleOriginalFileClick() {
        post?.let { postData ->
            if (postData.files.isNotEmpty()) {
                val filesList = postData.files//.map { file ->
//                    File(
//                        _id = file._id,
//                        fileId = (file.fileId?.ifBlank { "no_file_id" } ?: "no_file_id").toString(),
//                        localPath = (file.localPath?.ifBlank { "" } ?: "").toString(),
//                        url = file.url?.ifBlank { "" } ?: "",
//                        type = file.type?.ifBlank { "unknown_type" } ?: "unknown_type",
//                        mimeType = file.mimeType?.ifBlank { "" } ?: "",
//                        fileType = ""
//                    )
//                }
                val fileIds = filesList.map { it.fileId }
                navigateToTappedFilesFragment(requireContext(), 0, filesList, fileIds)
            }
        }
    }

    enum class MediaType {
        Image,
        Video,
        Audio,
        Document,
        CombinationOfMultipleFiles,
        Unknown
    }


    private fun getMediaType(
        file: File?,
        fileTypes: List<FileType> = emptyList()
    ): MediaType {
        if (file == null) {
            Log.d(TAG, "File is null, returning MediaType Unknown")
            return MediaType.Unknown
        }

        Log.d(TAG, "File details: fileId=${file.fileId}, fileName=${file.url}, mimeType=${file.mimeType}")

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

        // Fallback to original fileType check
        when (file.mimeType?.lowercase()) {
            "video" -> {
                Log.d(TAG, "Detected video via file fileType")
                return MediaType.Video
            }
            "pdf" -> {
                Log.d(TAG, "Detected pdf via file fileType")
                return MediaType.Document
            }
            "image" -> {
                Log.d(TAG, "Detected image via file fileType")
                return MediaType.Image
            }
            "audio" -> {
                Log.d(TAG, "Detected audio via file fileType")
                return MediaType.Audio
            }
            "mixed_files" -> {
                Log.d(TAG, "Detected mixed_files via file fileType")
                return MediaType.CombinationOfMultipleFiles
            }
        }

        // Check file extension
        val extension = file.fileId?.substringAfterLast(".")?.lowercase()
            ?: file.url?.substringAfterLast(".")?.substringBefore("?")?.lowercase()

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


    private fun isImageFile(
        file: File,
        fileTypes: List<FileType>?): Boolean {
        val matchingFileType = fileTypes?.find { it.fileId == file.fileId }
        if (matchingFileType?.fileType?.contains("image", ignoreCase = true) == true) {
            return true
        }

        val mimeType = file.mimeType?.lowercase()
        val url = file.url?.lowercase()
        val fileType = file.mimeType?.lowercase()

        return when {
            fileType?.contains("image") == true -> true
            mimeType?.startsWith("image") == true -> true
            url?.matches(".*\\.(jpg|jpeg|png|gif|bmp|webp)(\\?.*)?$".toRegex()) == true -> true
            file.fileId?.matches(".*\\.(jpg|jpeg|png|gif|bmp|webp)$".toRegex()) == true -> true
            else -> false
        }
    }

    private fun isVideoFile(
        file: File,
        fileTypes: List<FileType>?): Boolean {
        val matchingFileType = fileTypes?.find { it.fileId == file.fileId }
        if (matchingFileType?.fileType?.contains("video", ignoreCase = true) == true) {
            return true
        }

        val mimeType = file.mimeType?.lowercase()
        val url = file.url?.lowercase()
        val fileType = file.mimeType?.lowercase()

        return when {
            fileType?.contains("video") == true -> true
            mimeType?.startsWith("video") == true -> true
            url?.matches(".*\\.(mp4|avi|mov|wmv|flv|webm|mkv)(\\?.*)?$".toRegex()) == true -> true
            file.fileId?.matches(".*\\.(mp4|avi|mov|wmv|flv|webm|mkv)$".toRegex()) == true -> true
            else -> false
        }
    }

    private fun isAudioFile(
        file: File,
        fileTypes: List<FileType>?): Boolean {
        val matchingFileType = fileTypes?.find { it.fileId == file.fileId }
        if (matchingFileType?.fileType?.contains("audio", ignoreCase = true) == true) {
            return true
        }

        val mimeType = file.mimeType?.lowercase()
        val url = file.url?.lowercase()
        val fileType = file.mimeType?.lowercase()

        return when {
            fileType?.contains("audio") == true -> true
            mimeType?.startsWith("audio") == true -> true
            url?.matches(".*\\.(mp3|wav|ogg|m4a|aac|flac)(\\?.*)?$".toRegex()) == true -> true
            file.fileId?.matches(".*\\.(mp3|wav|ogg|m4a|aac|flac)$".toRegex()) == true -> true
            else -> false
        }
    }

    private fun isDocumentFile(
        file: File,
        fileTypes: List<FileType>?): Boolean {
        val matchingFileType = fileTypes?.find { it.fileId == file.fileId }
        val fileTypeStr = matchingFileType?.fileType?.lowercase()
        if (fileTypeStr?.contains("pdf") == true || fileTypeStr?.contains("doc") == true) {
            return true
        }

        val mimeType = file.mimeType?.lowercase()
        val url = file.url?.lowercase()
        val type = file.mimeType?.lowercase()

        return when {
            type?.contains("pdf") == true -> true
            type?.contains("doc") == true -> true
            type?.contains("application/pdf") == true -> true
            mimeType?.contains("pdf") == true -> true
            mimeType?.contains("msword") == true -> true
            mimeType?.contains("wordprocessingml") == true -> true
            mimeType?.contains("ms-excel") == true -> true
            mimeType?.contains("spreadsheetml") == true -> true
            mimeType?.contains("ms-powerpoint") == true -> true
            mimeType?.contains("presentationml") == true -> true
            url?.matches(".*\\.(pdf|doc|docx|xls|xlsx|ppt|pptx|txt|rtf|odt|csv)(\\?.*)?$".toRegex()) == true -> true
            file.fileId?.matches(".*\\.(pdf|doc|docx|xls|xlsx|ppt|pptx|txt|rtf|odt|csv)$".toRegex()) == true -> true
            else -> false
        }
    }


    private fun isMultipleCombinationFiles(
        file: File,
        fileTypes: List<FileType>?
    ): Boolean {
        val matchingFileType = fileTypes?.find { it.fileId == file.fileId }
        if (matchingFileType?.fileType?.contains("mixed_files", ignoreCase = true) == true) {
            return true
        }

        val mimeType = file.mimeType?.lowercase()
        val fileType = file.mimeType?.lowercase()

        return when {
            fileType?.contains("mixed_files") == true -> true
            mimeType?.contains("mixed_files") == true -> true
            else -> false
        }
    }


    data class MediaItem(
        val file: File,
        val thumbnail: ThumbnailX?,
        val duration: Duration?,
        val fileType: String,
        val fileId: String,
        val fileName: String?
    )





    inner class PostMediaHandler(

        private val post: Post,
        private val originalPost: OriginalPost?

    )

    {

        private val files: List<File>
        private val thumbnails: List<ThumbnailX>
        private val durations: List<Duration>
        private val fileTypes: List<FileType>?
        private val fileIds: List<String>

        init {
            when (post) {
                is OriginalPost -> {
                    files = post.files
                    thumbnails = post.thumbnail
                    durations = post.duration
                    fileTypes = post.fileTypes
                    fileIds = post.fileIds as List<String>
                }
                is Post -> {
                    val originalPost = post.originalPost.firstOrNull()
                    if (originalPost != null) {
                        files = originalPost.files
                        thumbnails = originalPost.thumbnail
                        durations = originalPost.duration
                        fileTypes = originalPost.fileTypes
                        fileIds = originalPost.fileIds as List<String>
                    } else {
                        files = post.files
                        thumbnails = post.thumbnail
                        durations = post.duration
                        fileTypes = post.fileTypes
                        fileIds = post.fileIds as List<String>
                    }
                }
                else -> {
                    files = emptyList()
                    thumbnails = emptyList()
                    durations = emptyList()
                    fileTypes = null
                    fileIds = emptyList()
                }
            }

            logMediaDetails()
        }



        private fun logMediaDetails() {
            Log.d("PostMediaHandler", "Post Type: ${post::class.java.simpleName}")
            Log.d("PostMediaHandler", "Files Count: ${files.size}")
            Log.d("PostMediaHandler", "Thumbnails Count: ${thumbnails.size}")
            Log.d("PostMediaHandler", "Durations Count: ${durations.size}")
            Log.d("PostMediaHandler", "FileTypes Count: ${fileTypes?.size ?: 0}")
            Log.d("PostMediaHandler", "FileIds Count: ${fileIds.size}")

            files.forEachIndexed { index, file ->
                val fileType = getFileTypeForFile(file)
                val isAudio = isAudioFile(file, fileTypes)
                Log.d("PostMediaHandler", "File $index: URL=${file.url}, FileId=${file.fileId}, DetectedType=$fileType, IsAudio=$isAudio")
            }

            fileTypes?.forEachIndexed { index, fileType ->
                Log.d("PostMediaHandler", "FileType $index: FileId=${fileType.fileId}, Type=${fileType.fileType}")
            }
        }

        fun setupCleanRecyclerView(adapter: MediaOriginalPostAdapter) {
            recyclerViews?.let { recyclerView ->
                if (files.isEmpty()) {
                    Log.d("PostMediaHandler", "No files to display in RecyclerView")
                    recyclerView.visibility = View.GONE
                    return
                }

                Log.d("PostMediaHandler", "Setting up RecyclerView with ${files.size} files")

                // CRITICAL FIX 1: Ensure RecyclerView is properly configured before layout manager
                recyclerView.visibility = View.VISIBLE

                // CRITICAL FIX 2: Set proper layout parameters FIRST
                val displayMetrics = recyclerView.context.resources.displayMetrics
                val layoutParams = recyclerView.layoutParams ?: ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                // Ensure minimum dimensions
                if (layoutParams.width <= 0) {
                    layoutParams.width = displayMetrics.widthPixels
                }
                if (layoutParams.height <= 0) {
                    layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                }

                recyclerView.layoutParams = layoutParams

                Log.d("PostMediaHandler", "Set layout params - Width: ${layoutParams.width}, Height: ${layoutParams.height}")

                // CRITICAL FIX 3: Set layout manager with proper span count
                val fileCount = files.size
                val layoutManager = when (fileCount) {
                    1 -> GridLayoutManager(recyclerView.context, 1)
                    2 -> GridLayoutManager(recyclerView.context, 2)
                    3 -> {
                        // For 3 items, use StaggeredGrid but ensure proper setup
                        val staggered = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                        staggered.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
                        staggered
                    }
                    else -> GridLayoutManager(recyclerView.context, 2)
                }

                recyclerView.layoutManager = layoutManager

                // CRITICAL FIX 4: Clear any existing adapter to avoid conflicts
                recyclerView.adapter = null

                // CRITICAL FIX 5: Create MediaItems with proper validation
                val mediaItems = files.mapIndexed { index, file ->
                    val fileId = fileIds.getOrNull(index) ?: file.fileId
                    MediaItem(
                        file = file,
                        thumbnail = thumbnails.find { it.fileId == fileId },
                        duration = durations.find { it.fileId == fileId },
                        fileType = getFileTypeForFile(file),
                        fileId = fileId,
                        fileName = file._id
                    )
                }

                Log.d("PostMediaHandler", "Created ${mediaItems.size} MediaItems")

                // CRITICAL FIX 6: Set adapter and submit list
                adapter.submitList(mediaItems)
                recyclerView.adapter = adapter

                // CRITICAL FIX 7: Force layout with multiple attempts
                recyclerView.requestLayout()

                // First check after immediate layout
                recyclerView.post {
                    Log.d("PostMediaHandler", "First check - RecyclerView: ${recyclerView.width}x${recyclerView.height}")
                    Log.d("PostMediaHandler", "First check - Children: ${recyclerView.childCount}, Items: ${adapter.itemCount}")

                    if (recyclerView.childCount == 0 && adapter.itemCount > 0) {
                        Log.w("PostMediaHandler", "No children rendered, forcing layout refresh")

                        // Try invalidating the layout manager
                        recyclerView.layoutManager?.let { lm ->
                            lm.requestLayout()
                        }

                        // Force measure and layout
                        recyclerView.measure(
                            View.MeasureSpec.makeMeasureSpec(recyclerView.width, View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                        )
                        recyclerView.layout(recyclerView.left, recyclerView.top, recyclerView.right, recyclerView.bottom)

                        // Second attempt with delay
                        recyclerView.postDelayed({
                            Log.d("PostMediaHandler", "Second check - Children: ${recyclerView.childCount}")

                            if (recyclerView.childCount == 0) {
                                Log.e("PostMediaHandler", "CRITICAL: RecyclerView still empty after all attempts")
                                Log.e("PostMediaHandler", "Parent visibility: ${(recyclerView.parent as? View)?.visibility}")
                                Log.e("PostMediaHandler", "RecyclerView visibility: ${recyclerView.visibility}")

                                // Last resort: try recreating adapter
                                val newAdapter = MediaOriginalPostAdapter(files, thumbnails, durations, fileTypes, fileIds, recyclerView.context, post)
                                newAdapter.submitList(mediaItems)
                                recyclerView.adapter = newAdapter
                                recyclerView.invalidate()
                            }
                        }, 200)
                    }
                }

                Log.d("PostMediaHandler", "RecyclerView setup completed")
            } ?: Log.e("PostMediaHandler", "RecyclerView is null!")
        }

        fun setupMediaViews() {
            if (files.isEmpty()) {
                Log.d("PostMediaHandler", "No files found, hiding media views")
                hideAllMediaViews()
                return
            }

            Log.d("PostMediaHandler", "Setting up media views for ${files.size} files")
            val mediaType = determineMediaType()
            Log.d("PostMediaHandler", "Determined media type: $mediaType")

            val mediaAdapter = MediaOriginalPostAdapter(
                files = files,
                thumbnails = thumbnails,
                durations = durations,
                fileTypes = fileTypes,
                fileIds = fileIds,
                context = requireContext(),
                post = post
            )

            when (mediaType) {
                MediaType.Image -> {
                    Log.d("PostMediaHandler", "Setting up for Image type")
                    showImageContainer()
                    setupCleanRecyclerView(mediaAdapter)
                }
                MediaType.Video -> {
                    Log.d("PostMediaHandler", "Setting up for Video type")
                    showVideoContainer()
                    setupCleanRecyclerView(mediaAdapter)
                }
                MediaType.Audio -> {
                    Log.d("PostMediaHandler", "Setting up for Audio type")
                    showAudioContainer()
                    setupCleanRecyclerView(mediaAdapter)

                    // CRITICAL FIX: Force audio container to stay visible with delay
                    multipleAudiosContainer?.postDelayed({
                        Log.d("PostMediaHandler", "Post-setup check - multipleAudiosContainer visibility: ${multipleAudiosContainer?.visibility}")
                        if (multipleAudiosContainer?.visibility != View.VISIBLE) {
                            Log.w("PostMediaHandler", "Audio container became invisible, forcing back to visible")
                            multipleAudiosContainer?.visibility = View.VISIBLE
                            recyclerViews?.requestLayout()
                        }
                    }, 100)
                }
                MediaType.Document -> {
                    Log.d("PostMediaHandler", "Setting up for Document type")
                    showDocumentContainer()
                    setupCleanRecyclerView(mediaAdapter)
                }
                MediaType.CombinationOfMultipleFiles, MediaType.Unknown -> {
                    Log.d("PostMediaHandler", "Setting up for Mixed/Unknown type")
                    showMixedContainer()
                    setupCleanRecyclerView(mediaAdapter)
                }
            }
        }


        private fun determineMediaType(): MediaType {
            if (files.isEmpty()) {
                Log.d("PostMediaHandler", "No files - returning Unknown")
                return MediaType.Unknown
            }

            val uniqueMediaTypes = files.map { file ->
                when {
                    isImageFile(file, fileTypes) -> MediaType.Image
                    isVideoFile(file, fileTypes) -> MediaType.Video
                    isAudioFile(file, fileTypes) -> MediaType.Audio
                    isDocumentFile(file, fileTypes) -> MediaType.Document
                    isMultipleCombinationFiles(file, fileTypes) -> MediaType.CombinationOfMultipleFiles
                    else -> MediaType.Unknown
                }
            }.distinct().filter { it != MediaType.Unknown }

            Log.d("PostMediaHandler", "Detected unique media types: $uniqueMediaTypes")

            return when {
                uniqueMediaTypes.isEmpty() -> MediaType.Unknown
                uniqueMediaTypes.size == 1 -> uniqueMediaTypes.first()
                else -> MediaType.CombinationOfMultipleFiles
            }
        }

        private fun showImageContainer() {
            Log.d("PostMediaHandler", "Showing Images Only Container")
            mixedFilesCardView?.visibility = View.VISIBLE
            originalFeedImage?.visibility = View.VISIBLE
            multipleAudiosContainer?.visibility = View.GONE
            recyclerViews?.visibility = View.GONE
        }

        private fun showAudioContainer() {
            Log.d("PostMediaHandler", "Showing Audios Only Container")

            multipleAudiosContainer?.let { container ->
                // Make sure all parents in the chain are visible
                var currentParent = container.parent
                while (currentParent is View) {
                    if (currentParent.visibility != View.VISIBLE) {
                        Log.w("PostMediaHandler", "Found invisible parent, making visible: ${currentParent::class.java.simpleName}")
                        currentParent.visibility = View.VISIBLE
                    }
                    currentParent = currentParent.parent
                }

                container.visibility = View.VISIBLE

                // Set container to wrap content properly
                val containerLayoutParams = container.layoutParams
                if (containerLayoutParams != null) {
                    containerLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    containerLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                    container.layoutParams = containerLayoutParams
                    Log.d("PostMediaHandler", "Set container dimensions - Width: MATCH_PARENT, Height: WRAP_CONTENT")
                }
            }

            // Hide other containers
            mixedFilesCardView?.visibility = View.VISIBLE
            originalFeedImage?.visibility = View.VISIBLE

            // CRITICAL FIX: Use WRAP_CONTENT for RecyclerView to show all items
            recyclerViews?.let { rv ->
                rv.visibility = View.VISIBLE

                val layoutParams = rv.layoutParams ?: ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                // REMOVE FIXED HEIGHT - Let it wrap content to show all items
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT

                rv.layoutParams = layoutParams

                Log.d("PostMediaHandler", "Set RecyclerView dimensions - Width: MATCH_PARENT, Height: WRAP_CONTENT")

                rv.requestLayout()
            }

            // Enhanced layout verification
            multipleAudiosContainer?.post {
                multipleAudiosContainer?.requestLayout()
                recyclerViews?.requestLayout()

                recyclerViews?.postDelayed({
                    Log.d("PostMediaHandler", "Final verification dimensions: ${recyclerViews?.width}x${recyclerViews?.height}")
                    Log.d("PostMediaHandler", "Final verification  children: ${recyclerViews?.childCount}")
                    Log.d("PostMediaHandler", "Final verification - Container visible: ${multipleAudiosContainer?.visibility == View.VISIBLE}")
                }, 100)
            }
        }


        private fun getFileNameForFile(file: File, index: Int): String {
            // Try to get filename from the fileNames list first
            val fileName = post.originalPost.firstOrNull()?.fileNames?.find { it.fileId == file.fileId }?.fileName
            if (!fileName.isNullOrEmpty()) {
                return fileName
            }

            // Fallback to extracting from URL or using index
            val url = file.url ?: file.localPath ?: ""
            val extractedName = url.substringAfterLast("/").substringBefore(".")
            return if (extractedName.isNotEmpty()) extractedName else "Audio ${index + 1}"
        }

        private fun showVideoContainer() {
            Log.d("PostMediaHandler", "Showing Videos Only  Ccontainer")
            mixedFilesCardView?.visibility = View.VISIBLE
            originalFeedImage?.visibility = View.GONE
            multipleAudiosContainer?.visibility = View.GONE
            recyclerViews?.visibility = View.GONE
        }

        private fun showDocumentContainer() {

            Log.d("PostMediaHandler", "Showing Documents Only Container")
            mixedFilesCardView?.visibility = View.VISIBLE
            originalFeedImage?.visibility = View.GONE
            multipleAudiosContainer?.visibility = View.GONE
            recyclerViews?.visibility = View.GONE
        }

        private fun showMixedContainer() {
            Log.d("PostMediaHandler", "Showing Combination Of Multiple Files Container")
            mixedFilesCardView?.visibility = View.VISIBLE
            originalFeedImage?.visibility = View.VISIBLE
            recyclerViews?.visibility = View.VISIBLE
            multipleAudiosContainer?.visibility = View.GONE
        }

        private fun hideAllMediaViews() {
            Log.d("PostMediaHandler", "Hiding All Media Files Views")
            mixedFilesCardView?.visibility = View.GONE
            originalFeedImage?.visibility = View.GONE
            multipleAudiosContainer?.visibility = View.GONE
            recyclerViews?.visibility = View.GONE
        }

        private fun getFileTypeForFile(file: File): String {
            val fileType = fileTypes?.find { it.fileId == file.fileId }?.fileType
            if (!fileType.isNullOrEmpty()) {
                Log.d("PostMediaHandler", "Found fileType from list: $fileType for fileId: ${file.fileId}")
                return fileType
            }

            val url = file.url ?: file.localPath ?: ""
            val detectedType = when {
                url.contains(".jpg", true) || url.contains(".png", true) ||
                        url.contains(".gif", true) || url.contains(".webp", true) ||
                        url.contains(".jpeg", true) -> "image"

                url.contains(".mp4", true) || url.contains(".avi", true) ||
                        url.contains(".mov", true) || url.contains(".webm", true) ||
                        url.contains(".mkv", true) -> "video"

                // ENHANCED AUDIO DETECTION - matches FeedAudiosOnly formats
                url.contains(".mp3", true) || url.contains(".wav", true) ||
                        url.contains(".ogg", true) || url.contains(".m4a", true) ||
                        url.contains(".aac", true) || url.contains(".flac", true) ||
                        url.contains(".amr", true) || url.contains(".3gp", true) ||
                        url.contains(".opus", true) -> "audio"

                url.contains(".pdf", true) || url.contains(".doc", true) ||
                        url.contains(".txt", true) || url.contains(".docx", true) -> "document"
                else -> "unknown"
            }

            Log.d("PostMediaHandler", "Detected fileType from URL: $detectedType for URL: $url")
            return detectedType
        }
    }


    class MediaOriginalPostAdapter(
        private val files: List<File>,
        private val thumbnails: List<ThumbnailX>,
        private val durations: List<Duration>,
        private val fileTypes: List<FileType>?,
        private val fileIds: List<String>,
        private val context: Context,
        private val post: Post
    )

        : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private var mediaItems: List<MediaItem> = emptyList()

        override fun getItemCount(): Int {
            Log.d("MediaOriginalPostAdapter", "getItemCount returning: ${mediaItems.size}")
            return mediaItems.size
        }



        @SuppressLint("Notify Data Set Changed")
        fun submitList(items: List<MediaItem>) {
            Log.d("MediaAdapter", "submitList called with ${items.size} items")
            mediaItems = items
            notifyDataSetChanged()
            Log.d("MediaAdapter", "notify Data Set Changed () called")
        }

        override fun getItemViewType(position: Int): Int {
            val mediaItem = mediaItems[position]
            Log.d("MediaOriginalPostAdapter", "Getting view type for position $position: fileType=${mediaItem.fileType}")

            val viewType = when (mediaItem.fileType.lowercase()) {
                "image" -> R.layout.feed_multiple_images_only_view_item
                "video" -> R.layout.feed_multiple_videos_only_view_item
                "audio" -> R.layout.feed_multiple_audios_only_view_item
                "document" -> R.layout.feed_multiple_documents_only_view_item
                else -> {
                    Log.w("MediaOriginalPostAdapter", "Unknown file type: ${mediaItem.fileType}, using combination layout")
                    R.layout.feed_multiple_combination_of_files_view_item
                }
            }

            Log.d("MediaOriginalPostAdapter", "Returning view type: $viewType for fileType: ${mediaItem.fileType}")
            return viewType
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            Log.d("MediaOriginalPostAdapter", "onCreateViewHolder called with viewType: $viewType")
            val view = LayoutInflater.from(context).inflate(viewType, parent, false)

            val holder = when (viewType) {
                R.layout.feed_multiple_images_only_view_item -> {

                    FeedImagesOnly(view)
                }
                R.layout.feed_multiple_videos_only_view_item -> {

                    FeedVideosOnly(view)
                }
                R.layout.feed_multiple_audios_only_view_item -> {

                    FeedAudiosOnly(view)
                }
                R.layout.feed_multiple_documents_only_view_item -> {

                    FeedDocumentsOnly(view)
                }
                else -> {

                    FeedCombinationOfMultipleFiles(view)
                }
            }

            Log.d("MediaOriginalPostAdapter", "ViewHolder created: ${holder::class.java.simpleName}")
            return holder
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            Log.d("MediaOriginalPostAdapter", "onBindViewHolder called for position $position with holder: ${holder::class.java.simpleName}")
            val mediaItem = mediaItems[position]

            when (holder) {
                is FeedImagesOnly -> {

                    holder.onBind(post)
                }
                is FeedVideosOnly -> {

                    holder.onBind(post)
                }
                is FeedAudiosOnly -> {

                    holder.onBind(post)

                }
                is FeedDocumentsOnly -> {

                    holder.onBind(post)
                }
                is FeedCombinationOfMultipleFiles -> {

                    holder.onBind(post)
                }
            }
            Log.d("MediaOriginalPostAdapter", "onBindViewHolder completed for position $position")
        }



    }


    class FeedImagesOnly(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val materialCardView: MaterialCardView =
            itemView.findViewById(R.id.materialCardView)
        private val countTextView: TextView = itemView.findViewById(R.id.textView)
        private val imageView2: ImageView = itemView.findViewById(R.id.imageView2)

        fun Int.dpToPx(context: Context): Int {
            return (this * context.resources.displayMetrics.density).toInt()
        }

        // Helper function to calculate adaptive heights based on screen size
        private fun getAdaptiveHeights(context: Context): Pair<Int, Int> {
            val displayMetrics = context.resources.displayMetrics
            val screenHeight = displayMetrics.heightPixels

            // Calculate min and max heights as percentages of screen height
            val minHeight = (screenHeight * 0.12).toInt() // 12% of screen height
            val maxHeight = (screenHeight * 0.35).toInt()

            return Pair(minHeight, maxHeight)
        }

        // Helper function to get a constrained height within min/max bounds
        private fun getConstrainedHeight(context: Context, preferredHeight: Int): Int {
            val (minHeight, maxHeight) = getAdaptiveHeights(context)
            return preferredHeight.coerceIn(minHeight, maxHeight)
        }

        // Helper function to configure MaterialCardView with proper corner radius
        private fun setupCardViewCorners(context: Context) {
            val cornerRadius = 8.dpToPx(context).toFloat()

            // Set the corner radius
            materialCardView.radius = cornerRadius

            // Ensure card is clipped to bounds to show rounded corners
            materialCardView.clipToOutline = true
            materialCardView.clipChildren = true

            // Remove any elevation that might interfere with corners
            materialCardView.cardElevation = 0f
            materialCardView.maxCardElevation = 0f

            // Set stroke width to 0 to avoid border issues
            materialCardView.strokeWidth = 0

            // Ensure content padding doesn't interfere
            materialCardView.setContentPadding(0, 0, 0, 0)

            // Set background color
            materialCardView.setCardBackgroundColor(Color.WHITE)

            // Configure ImageView to respect the card's rounded corners
            imageView.clipToOutline = true
            imageView.outlineProvider = ViewOutlineProvider.BACKGROUND
        }

        // Helper function to get AppCompatActivity from context
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

                Log.d(TAG, "Navigated to Tapped_Files_In_The_Container_View with ${files.size} files, starting at index $currentIndex")
            } else {
                Log.e(TAG, "Activity is null, cannot navigate to fragment")
            }
        }

        @SuppressLint("SetTextI18n")
        fun onBind(data: Post) {
            Log.d(TAG, "image feed $absoluteAdapterPosition item count $")

            val context = itemView.context

            // Setup card view corners first
            setupCardViewCorners(context)


            val position = absoluteAdapterPosition

            // Determine which files and fileIds to use
            var actualFiles: List<File> = data.files
            var actualFileIds: List<String> = data.fileIds as List<String>

            // Check if this is a repost with original post data
            if ((data.files == null || data.files.isEmpty()) &&
                data.originalPost != null && data.originalPost.isNotEmpty()) {

                val originalPost: OriginalPost = data.originalPost[0]
                actualFiles = originalPost.files ?: emptyList()
                actualFileIds = (originalPost.fileIds ?: emptyList()) as List<String>
            }

            Log.d(TAG, "onBind: file type Images $position item count ${actualFiles.size}")

            if (actualFiles.isEmpty() || actualFileIds.isEmpty()) {
                Log.e("Fragment_Original_Post_Inside", "No files or fileIds available")
                return
            }

            // Validate absoluteAdapterPosition using actualFileIds
            if (position < 0 || position >= actualFileIds.size) {
                Log.e(TAG, "Invalid absoluteAdapterPosition: $position for actualFileIds size: ${actualFileIds.size}")
                itemView.visibility = View.GONE
                return
            }

            itemView.setBackgroundColor(Color.TRANSPARENT)

            val displayMetrics = context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val margin = 4.dpToPx(context)
            val spaceBetweenRows = 4.dpToPx(context)

            // Get adaptive heights
            val (minHeight, maxHeight) = getAdaptiveHeights(context)

            val fileIdToFind = actualFileIds.getOrNull(absoluteAdapterPosition) ?: ""
            val file = actualFiles.find { it.fileId == fileIdToFind }
            val imageUrl = file?.url ?: actualFiles.getOrNull(absoluteAdapterPosition)?.url ?: ""

            val fileSize = actualFiles.size
            Log.d(TAG, "image getItemCount: $fileSize $imageUrl")



            itemView.setOnClickListener {
                navigateToTappedFilesFragment(
                    context,
                    absoluteAdapterPosition,
                    actualFiles,
                    actualFileIds
                )
            }

            imageView.setOnClickListener {
                navigateToTappedFilesFragment(
                    context,
                    absoluteAdapterPosition,
                    actualFiles,
                    actualFileIds
                )
            }

            materialCardView.setOnClickListener {
                navigateToTappedFilesFragment(
                    context,
                    absoluteAdapterPosition,
                    actualFiles,
                    actualFileIds
                )
            }


            val layoutParams = materialCardView.layoutParams as ViewGroup.MarginLayoutParams

            when {

                fileSize <= 1 -> {

                    Glide.with(context)
                        .asBitmap()
                        .load(imageUrl)
                        .placeholder(R.drawable.flash21)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(object : CustomTarget<Bitmap>() {

                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                val imageWidth = resource.width
                                val imageHeight = resource.height

                                val aspectRatio = imageHeight.toFloat() / imageWidth.toFloat()
                                val screenWidth = Resources.getSystem().displayMetrics.widthPixels

                                // Limit image width to screen width and calculate height based on actual aspect ratio
                                val displayWidth = screenWidth
                                val displayHeight = (displayWidth * aspectRatio).toInt()

                                val layoutParams = imageView.layoutParams as ViewGroup.MarginLayoutParams
                                layoutParams.width = displayWidth
                                layoutParams.height = displayHeight
                                layoutParams.setMargins(0, 0, 0, 0)

                                imageView.layoutParams = layoutParams
                                imageView.setImageBitmap(resource)
                                imageView.adjustViewBounds = true

                                // Automatically choose CENTER_CROP or FIT_CENTER based on image shape
                                imageView.scaleType = if (aspectRatio > 1.2f) {
                                    // Portrait image
                                    ImageView.ScaleType.CENTER_CROP
                                } else {
                                    // Landscape image or near-square
                                    ImageView.ScaleType.CENTER_CROP
                                }
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                                // Optionally handle placeholder cleanup
                            }
                        })
                }


                fileSize == 2 -> {
                    layoutParams.width = screenWidth / 2
                    // Use adaptive height instead of fixed 300dp
                    layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.75).toInt())
                    layoutParams.topMargin = 0
                    layoutParams.bottomMargin = 0

                    val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
                    layoutParams.leftMargin = if (isLeftColumn) 0 else spaceBetweenRows
                    layoutParams.rightMargin = if (isLeftColumn) spaceBetweenRows else 0
                }

                fileSize == 3 -> {
                    when (absoluteAdapterPosition) {
                        0 -> {
                            // First image takes left half with FULL height
                            layoutParams.width = screenWidth / 2
                            layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.75).toInt())
                            layoutParams.leftMargin = 0
                            layoutParams.rightMargin = (spaceBetweenRows/2)
                            layoutParams.topMargin = 0
                            layoutParams.bottomMargin = 0

                            if (layoutParams is StaggeredGridLayoutManager.LayoutParams) {
                                layoutParams.isFullSpan = false
                            }
                        }

                        1, 2 -> {
                            // Second and third images stack vertically on the right side
                            layoutParams.width = screenWidth / 2
                            // Each takes half the FULL height (so together they equal position 0's height)
                            layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.75).toInt()) /2

                            layoutParams.leftMargin = (spaceBetweenRows/2)
                            layoutParams.rightMargin = 0

                            if (absoluteAdapterPosition == 1) {
                                // Top right image
                                layoutParams.topMargin = 0
                                layoutParams.bottomMargin = (spaceBetweenRows/2)
                            } else {
                                // Bottom right image (position 2)
                                layoutParams.topMargin = (spaceBetweenRows/2)
                                layoutParams.bottomMargin = 0
                            }
                        }
                    }
                }

                fileSize == 4 -> {
                    layoutParams.width = screenWidth / 2
                    // Make height adaptive but maintain square-ish aspect ratio
                    val preferredSquareHeight = screenWidth / 2
                    layoutParams.height = getConstrainedHeight(context, preferredSquareHeight)

                    layoutParams.topMargin = if (absoluteAdapterPosition < 2) 0 else spaceBetweenRows
                    layoutParams.bottomMargin = if (absoluteAdapterPosition >= 2) 0 else 0

                    val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
                    layoutParams.leftMargin = if (isLeftColumn) 0 else (spaceBetweenRows/2)
                    layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows/2) else 0
                }

                fileSize == 5 -> {
                    if (absoluteAdapterPosition >= 4) {
                        // Hide anything beyond the first 4 items
                        itemView.visibility = View.GONE
                        layoutParams.width = 0
                        layoutParams.height = 0
                        itemView.layoutParams = layoutParams
                        return
                    }

                    itemView.visibility = View.VISIBLE

                    layoutParams.width = screenWidth / 2
                    val preferredSquareHeight = screenWidth / 2
                    layoutParams.height = getConstrainedHeight(context, preferredSquareHeight)

                    // 🟢 Default spacing
                    val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
                    layoutParams.leftMargin = if (isLeftColumn) 0 else (spaceBetweenRows /2)
                    layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows /2) else 0
                    layoutParams.topMargin = if (absoluteAdapterPosition < 2) 0 else spaceBetweenRows / 2
                    layoutParams.bottomMargin = if (absoluteAdapterPosition < 2) spaceBetweenRows / 2 else 0



                    itemView.layoutParams = layoutParams

                    // 🟣 Show +X overlay only on 4th item
                    if (absoluteAdapterPosition == 3) {
                        countTextView.visibility = View.VISIBLE
                        countTextView.text = "+${fileSize - 4}"
                        countTextView.textSize = 32f
                        countTextView.setPadding(12, 4, 12, 4)
                        countTextView.background = GradientDrawable().apply {
                            shape = GradientDrawable.RECTANGLE
                            cornerRadius = 16f
                            setColor("#80000000".toColorInt())
                        }
                    } else {
                        countTextView.visibility = View.GONE
                        countTextView.setPadding(0, 0, 0, 0)
                        countTextView.background = null
                    }
                }

                fileSize > 4 -> {
                    if (absoluteAdapterPosition >= 4) {
                        // Hide anything beyond the first 4 items
                        itemView.visibility = View.GONE
                        layoutParams.width = 0
                        layoutParams.height = 0
                        itemView.layoutParams = layoutParams
                        return
                    }

                    itemView.visibility = View.VISIBLE

                    layoutParams.width = screenWidth / 2
                    val preferredSquareHeight = screenWidth / 2
                    layoutParams.height = getConstrainedHeight(context, preferredSquareHeight)

                    // 🟢 Default spacing
                    val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
                    layoutParams.leftMargin = if (isLeftColumn) 0 else spaceBetweenRows
                    layoutParams.rightMargin = if (isLeftColumn) spaceBetweenRows else 0
                    layoutParams.topMargin = if (absoluteAdapterPosition < 2) 0 else spaceBetweenRows / 2
                    layoutParams.bottomMargin = if (absoluteAdapterPosition < 2) spaceBetweenRows / 2 else 0



                    itemView.layoutParams = layoutParams

                    // 🟣 Show +X overlay only on 4th item
                    if (absoluteAdapterPosition == 3) {
                        countTextView.visibility = View.VISIBLE
                        countTextView.text = "+${fileSize - 4}"
                        countTextView.textSize = 32f
                        countTextView.setPadding(12, 4, 12, 4)
                        countTextView.background = GradientDrawable().apply {
                            shape = GradientDrawable.RECTANGLE
                            cornerRadius = 16f
                            setColor("#80000000".toColorInt())
                        }
                    } else {
                        countTextView.visibility = View.GONE
                        countTextView.setPadding(0, 0, 0, 0)
                        countTextView.background = null
                    }
                }

            }

            materialCardView.layoutParams = layoutParams

            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.flash21)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop() // Use centerCrop for better image fitting
                .into(imageView)
        }
    }


    class FeedAudiosOnly(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val materialCardView: MaterialCardView = itemView.findViewById(R.id.materialCardView)
        private val artworkImageView: ImageView = itemView.findViewById(R.id.artworkImageView)
        private val countTextView: TextView = itemView.findViewById(R.id.textView)
        private val audioDurationTextView: TextView = itemView.findViewById(R.id.audioDuration)

        // Add these missing view declarations
        private val artworkLayout: View = itemView.findViewById(R.id.artworkLayout)
        private val artworkVn: ImageView = itemView.findViewById(R.id.artworkVn)

        fun Int.dpToPx(context: Context): Int {
            return (this * context.resources.displayMetrics.density).toInt()
        }

        private fun getAdaptiveHeights(context: Context): Pair<Int, Int> {
            val displayMetrics = context.resources.displayMetrics
            val screenHeight = displayMetrics.heightPixels
            val minHeight = (screenHeight * 0.12).toInt()
            val maxHeight = (screenHeight * 0.35).toInt()
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
                val fragment = Tapped_Files_In_The_Container_View_Fragment()
                val bundle = Bundle().apply {
                    putInt("current_index", currentIndex)
                    putInt("total_files", files.size)
                    putStringArrayList("file_urls", ArrayList(files.map { it.url }))
                    putStringArrayList("file_ids", ArrayList(fileIds))
                    val postItems = ArrayList<PostItem>()
                    files.forEachIndexed { index, file ->
                        postItems.add(PostItem(
                            audioUrl = file.url,
                            audioThumbnailUrl = null,
                            videoUrl = null,
                            videoThumbnailUrl = null,
                            postId = fileIds.getOrNull(index) ?: "audio_file_$index",
                            data = "Audio file",
                            files = arrayListOf(file.url)
                        ))
                    }
                    putParcelableArrayList("post_list", postItems)
                    putString("post_id", fileIds.getOrNull(currentIndex) ?: "audio_file_$currentIndex")
                    putString("media_type", "audio")
                }
                fragment.arguments = bundle
                activity.supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                    .replace(R.id.frame_layout, fragment)
                    .addToBackStack("tapped_audio_files_view")
                    .commit()
                Log.d(TAG, "Navigated to Tapped_Files_In_The_Container_View with ${files.size} audio files")
            } else {
                Log.e(TAG, "Activity is null, cannot navigate to fragment")
            }
        }

        @SuppressLint("SetTextI18n")
        fun onBind(data: Post) {
            Log.d("FeedAudiosOnly", "=== onBind called for position $absoluteAdapterPosition ===")

            var actualFiles: List<File> = data.files
            var actualFileIds: List<String> = data.fileIds as List<String>
            var actualFileTypes: List<FileType> = data.fileTypes
            var actualThumbnails: List<ThumbnailX> = data.thumbnail

            // Check if this is a repost with original post data
            if ((data.files == null || data.files.isEmpty()) &&
                data.originalPost != null && data.originalPost.isNotEmpty()) {
                val originalPost: OriginalPost = data.originalPost[0]
                actualFiles = originalPost.files ?: emptyList()
                actualFileIds = (originalPost.fileIds ?: emptyList()) as List<String>
                actualFileTypes = originalPost.fileTypes ?: emptyList()
                actualThumbnails = originalPost.thumbnail ?: emptyList()
            }

            itemView.visibility = View.VISIBLE
            materialCardView.visibility = View.VISIBLE

            // Reset state
            materialCardView.setCardBackgroundColor(Color.WHITE)
            artworkImageView.setImageDrawable(null)
            countTextView.visibility = View.GONE
            audioDurationTextView.text = ""

            val position = absoluteAdapterPosition
            if (position < 0 || position >= actualFileIds.size) {
                Log.e("FeedAudiosOnly", "Invalid position, hiding item")
                itemView.visibility = View.GONE
                return
            }

            val context = itemView.context
            val fileIdToFind = actualFileIds[position]
            val file = actualFiles.find { it.fileId == fileIdToFind }
            val fileUrl = file?.url ?: ""
            val thumbnail = actualThumbnails.find { it.fileId == fileIdToFind }
            val durationItem = data.duration?.find { it.fileId == fileIdToFind }
            val fileName = data.fileNames?.find { it.fileId == fileIdToFind }?.fileName ?: ""

            // Now we can use fileName for audio detection
            when {
                fileName.endsWith(".mp3", true) ||
                        fileName.endsWith(".wav", true) -> {
                    // Show artwork for common audio formats with music placeholder
                    materialCardView.setCardBackgroundColor(Color.WHITE)

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

                    val artworkLayoutWrapper = itemView.findViewById<MaterialCardView>(R.id.artworkLayoutWrapper)
                    artworkLayoutWrapper?.visibility = View.VISIBLE
                    artworkLayoutWrapper?.setCardBackgroundColor(Color.parseColor("#616161"))

                    // Show artworkLayout for voice notes
                    artworkLayout.visibility = View.VISIBLE
                    artworkLayout.setBackgroundColor(Color.parseColor("#616161"))

                    // Configure the mic icon
                    artworkVn.setImageResource(R.drawable.ic_audio_white_icon)
                    artworkVn.visibility = View.VISIBLE

                    // Set proper dimensions for the mic icon
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

            // Load thumbnail or fallback icon (only for music files, not voice notes)
            if (thumbnail != null && (fileName.endsWith(".mp3", true) || fileName.endsWith(".m4a", true) ||
                        (!fileName.endsWith(".ogg", true) && !fileName.endsWith(".aac", true) &&
                                !fileName.endsWith(".wav", true) && !fileName.endsWith(".flac", true) &&
                                !fileName.endsWith(".amr", true) && !fileName.endsWith(".3gp", true) &&
                                !fileName.endsWith(".opus", true)))) {
                Log.d("FeedAudiosOnly", "Loading thumbnail for audio at $fileUrl")
                Glide.with(context)
                    .load(thumbnail.thumbnailUrl)
                    .placeholder(R.drawable.music_icon)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(artworkImageView)
            }

            // Set duration
            audioDurationTextView.text = durationItem?.duration ?: ""
            audioDurationTextView.visibility = if (audioDurationTextView.text.isNotEmpty()) View.VISIBLE else View.GONE

            val displayMetrics = context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val margin = 4.dpToPx(context)
            val spaceBetweenRows = 4.dpToPx(context)
            val layoutParams = materialCardView.layoutParams as ViewGroup.MarginLayoutParams

            val fileSize = actualFiles.size
            when {
                fileSize <= 1 -> {
                    layoutParams.width = screenWidth
                    layoutParams.height = getConstrainedHeight(context, (getAdaptiveHeights(context).second * 0.75).toInt())
                    layoutParams.setMargins(0, 0, 0, 0)
                }
                fileSize == 2 -> {
                    layoutParams.width = screenWidth / 2
                    layoutParams.height = getConstrainedHeight(context, (getAdaptiveHeights(context).second * 0.75).toInt())
                    val isLeftColumn = (position % 2 == 0)
                    layoutParams.leftMargin = if (isLeftColumn) 0 else (spaceBetweenRows / 2)
                    layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows / 2) else 0
                }
                fileSize == 3 -> {
                    when (position) {
                        0 -> {
                            layoutParams.width = screenWidth / 2
                            layoutParams.height = getConstrainedHeight(context, (getAdaptiveHeights(context).second * 0.75).toInt())
                            layoutParams.leftMargin = 0
                            layoutParams.rightMargin = (spaceBetweenRows / 2)
                        }
                        1, 2 -> {
                            layoutParams.width = screenWidth / 2
                            layoutParams.height = getConstrainedHeight(context, (getAdaptiveHeights(context).second * 0.75).toInt()) / 2
                            layoutParams.leftMargin = (spaceBetweenRows / 2)
                            layoutParams.rightMargin = 0
                            layoutParams.topMargin = if (position == 1) 0 else (spaceBetweenRows / 2)
                            layoutParams.bottomMargin = if (position == 1) (spaceBetweenRows / 2) else 0
                        }
                    }
                }
                fileSize == 4 -> {
                    layoutParams.width = screenWidth / 2
                    layoutParams.height = getConstrainedHeight(context, screenWidth / 2)
                    val isLeftColumn = (position % 2 == 0)
                    layoutParams.leftMargin = if (isLeftColumn) 0 else (spaceBetweenRows / 2)
                    layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows / 2) else 0
                    layoutParams.topMargin = if (position < 2) 0 else (spaceBetweenRows / 2)
                    layoutParams.bottomMargin = if (position < 2) (spaceBetweenRows / 2) else 0
                }
                fileSize > 4 -> {
                    if (position >= 4) {
                        itemView.visibility = View.GONE
                        layoutParams.width = 0
                        layoutParams.height = 0
                        return
                    }
                    layoutParams.width = screenWidth / 2
                    layoutParams.height = getConstrainedHeight(context, screenWidth / 2)
                    val isLeftColumn = (position % 2 == 0)
                    layoutParams.leftMargin = if (isLeftColumn) 0 else (spaceBetweenRows / 2)
                    layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows / 2) else 0
                    layoutParams.topMargin = if (position < 2) 0 else (spaceBetweenRows / 2)
                    layoutParams.bottomMargin = if (position < 2) (spaceBetweenRows / 2) else 0
                    if (position == 3) {
                        countTextView.visibility = View.VISIBLE
                        countTextView.text = "+${fileSize - 4}"
                        countTextView.textSize = 32f
                        countTextView.setPadding(12, 4, 12, 4)
                        countTextView.background = GradientDrawable().apply {
                            shape = GradientDrawable.RECTANGLE
                            cornerRadius = 16f
                            setColor(Color.parseColor("#80000000"))
                        }
                    }
                }
            }

            materialCardView.layoutParams = layoutParams
            materialCardView.requestLayout()

            // Set click listeners
            val clickListener = View.OnClickListener {
                navigateToTappedFilesFragment(context, position, actualFiles, actualFileIds)
            }
            itemView.setOnClickListener(clickListener)
            materialCardView.setOnClickListener(clickListener)
            artworkImageView.setOnClickListener(clickListener)
            countTextView.setOnClickListener(clickListener)
            audioDurationTextView.setOnClickListener(clickListener)

            Log.d("FeedAudiosOnly", "=== onBind completed ===")
        }
    }


    class FeedVideosOnly(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun Int.dpToPx(context: Context): Int {
            return (this * context.resources.displayMetrics.density).toInt()
        }

        private val feedThumbnail: ImageView = itemView.findViewById(R.id.feedThumbnail)
        private val feedVideoDurationTextView: TextView =
            itemView.findViewById(R.id.feedVideoDurationTextView)
        private val cardView: CardView = itemView.findViewById(R.id.cardView)
        private val countTextView: TextView = itemView.findViewById(R.id.countTextView)
        private val imageView2: ImageView = itemView.findViewById(R.id.imageView2)

        // Helper function to get adaptive heights based on screen size
        private fun getAdaptiveHeights(context: Context): Pair<Int, Int> {
            val displayMetrics = context.resources.displayMetrics
            val screenHeight = displayMetrics.heightPixels


            val minHeight = (screenHeight * 0.18).toInt() // 12% of screen height
            val maxHeight = (screenHeight * 0.45).toInt()
            return Pair(minHeight, maxHeight)
        }

        // Helper function to constrain height within min/max bounds
        private fun getConstrainedHeight(context: Context, targetHeight: Int): Int {
            val (minHeight, maxHeight) = getAdaptiveHeights(context)
            return targetHeight.coerceIn(minHeight, maxHeight)
        }

        // Helper function to get AppCompatActivity from context
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

                    // **ADD THIS: Create PostItem list for the ViewPager**
                    val postItems = ArrayList<PostItem>()
                    files.forEachIndexed { index, file ->
                        val postItem = PostItem(
                            audioUrl = file.url,
                            audioThumbnailUrl = null,
                            videoUrl = file.url, // or null if it's not a video
                            videoThumbnailUrl = null,
                            postId = fileIds.getOrNull(index) ?: "file_$index",
                            data = "Post data for file $index",
                            files = arrayListOf(file.url) // Pass the URL
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
                    )
                    .replace(R.id.frame_layout, fragment)
                    .addToBackStack("tapped_files_view")
                    .commit()

                Log.d(
                    TAG, "Navigated to Tapped_Files_In_The_Container_View with ${files.size} " +
                            "files, starting at index $currentIndex")
            } else {
                Log.e(TAG, "Activity is null, cannot navigate to fragment")
            }
        }


        @SuppressLint("SetTextI18n")
        fun onBind(data: Post) {

            val position = absoluteAdapterPosition

            // Determine which files and fileIds to use
            var actualFiles: List<File> = data.files
            var actualFileIds: List<String> = data.fileIds as List<String>

            // Check if this is a repost with original post data
            if ((data.files == null || data.files.isEmpty()) &&
                data.originalPost != null && data.originalPost.isNotEmpty()) {

                val originalPost: OriginalPost = data.originalPost[0]
                actualFiles = originalPost.files ?: emptyList()
                actualFileIds = (originalPost.fileIds ?: emptyList()) as List<String>
            }

            Log.d(TAG, "onBind: file type Videos $position item count ${actualFiles.size}")

            if (actualFiles.isEmpty() || actualFileIds.isEmpty()) {
                Log.e("Fragment_Original_Post_Inside", "No files or fileIds available")
                return
            }

            // Validate absoluteAdapterPosition using actualFileIds
            if (position < 0 || position >= actualFileIds.size) {
                Log.e(TAG, "Invalid absoluteAdapterPosition: $position for actualFileIds size: ${actualFileIds.size}")
                itemView.visibility = View.GONE
                return
            }

            cardView.setCardBackgroundColor(Color.WHITE)
            itemView.setBackgroundColor(Color.TRANSPARENT)
            val fileIdToFind = actualFileIds[position] // Use actualFileIds

            // Consolidated click listener - use actualFiles and actualFileIds
            val clickListener = View.OnClickListener {
                navigateToTappedFilesFragment(
                    itemView.context,
                    position,
                    actualFiles,
                    actualFileIds
                )
            }
            itemView.setOnClickListener(clickListener)
            cardView.setOnClickListener(clickListener)
            feedThumbnail.setOnClickListener(clickListener)
            feedVideoDurationTextView.setOnClickListener(clickListener)
            countTextView.setOnClickListener(clickListener)
            imageView2.setOnClickListener(clickListener)

            val fileSize = actualFiles.size // Use actualFiles.size
            val context = itemView.context
            val displayMetrics = context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val margin = 4.dpToPx(context)
            val spaceBetweenRows = 4.dpToPx(context)
            cardView.radius = 8.dpToPx(context).toFloat()

            // Get duration and thumbnail from the correct source
            val durationItem: Duration?
            val thumbnail: ThumbnailX?

            if (data.files.isEmpty() && data.originalPost.isNotEmpty()) {
                // For reposted content, use original post data
                val originalPost = data.originalPost[0]
                durationItem = originalPost.duration.find { it.fileId == fileIdToFind }
                thumbnail = originalPost.thumbnail.find { it.fileId == fileIdToFind }
            } else {
                // For regular posts, use direct post data
                durationItem = data.duration.find { it.fileId == fileIdToFind }
                thumbnail = data.thumbnail.find { it.fileId == fileIdToFind }
            }

            feedVideoDurationTextView.text = durationItem?.duration ?: "00:00"


            Glide.with(context)
                .load(thumbnail?.thumbnailUrl ?: R.drawable.videoplaceholder)
                .placeholder(R.drawable.flash21)
                .error(R.drawable.videoplaceholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(feedThumbnail)

            feedVideoDurationTextView.text = durationItem?.duration ?: "00:00"


            Glide.with(context)
                .load(thumbnail?.thumbnailUrl ?: R.drawable.videoplaceholder)
                .placeholder(R.drawable.flash21)
                .error(R.drawable.videoplaceholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(feedThumbnail)

            val layoutParams = cardView.layoutParams as ViewGroup.MarginLayoutParams

            when {
                fileSize <= 1 -> {
                    layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                    layoutParams.height = getConstrainedHeight(context, (getAdaptiveHeights(context).second * 0.75).toInt())
                    layoutParams.setMargins(0, 0, 0, 0)
                }
                fileSize == 2 -> {
                    layoutParams.width = screenWidth / 2
                    layoutParams.height = getConstrainedHeight(context, (getAdaptiveHeights(context).second * 0.75).toInt())
                    layoutParams.topMargin = 0
                    layoutParams.bottomMargin = 0
                    val isLeftColumn = (position % 2 == 0)
                    layoutParams.leftMargin = if (isLeftColumn) 0 else (spaceBetweenRows / 2)
                    layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows / 2) else 0
                }
                fileSize == 3 -> {
                    val spanLayout = cardView.layoutParams as? StaggeredGridLayoutManager.LayoutParams
                    when (position) {
                        0 -> {
                            spanLayout?.isFullSpan = false
                            layoutParams.width = screenWidth / 2
                            val baseVideoHeight = getConstrainedHeight(context, (getAdaptiveHeights(context).second * 0.65).toInt())
                            val rightSideItemHeight = baseVideoHeight / 2
                            val totalRightSideHeight = (rightSideItemHeight * 2) + (spaceBetweenRows / 2)
                            layoutParams.height = totalRightSideHeight
                            layoutParams.setMargins(0, 0, spaceBetweenRows / 2, 0)
                        }
                        1, 2 -> {
                            spanLayout?.isFullSpan = false
                            layoutParams.width = screenWidth / 2
                            val baseVideoHeight = getConstrainedHeight(context, (getAdaptiveHeights(context).second * 0.65).toInt())
                            val totalHeight = baseVideoHeight + (spaceBetweenRows / 2)
                            layoutParams.height = (totalHeight - (spaceBetweenRows / 2)) / 2
                            layoutParams.leftMargin = spaceBetweenRows / 2
                            layoutParams.rightMargin = 0
                            layoutParams.topMargin = if (position == 1) 0 else (spaceBetweenRows / 2)
                            layoutParams.bottomMargin = if (position == 1) (spaceBetweenRows / 2) else 0
                        }
                    }
                }
                fileSize == 4 -> {
                    layoutParams.width = screenWidth / 2
                    layoutParams.height = getConstrainedHeight(context, screenWidth / 2)
                    val isLeftColumn = (position % 2 == 0)
                    layoutParams.leftMargin = if (isLeftColumn) 0 else (spaceBetweenRows / 2)
                    layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows / 2) else 0
                    layoutParams.topMargin = if (position < 2) 0 else (margin / 2)
                    layoutParams.bottomMargin = if (position < 2) (margin / 2) else 0
                }
                fileSize > 4 -> {
                    if (position >= 4) {
                        itemView.visibility = View.GONE
                        layoutParams.width = 0
                        layoutParams.height = 0
                        return
                    }
                    itemView.visibility = View.VISIBLE
                    layoutParams.width = screenWidth / 2
                    layoutParams.height = getConstrainedHeight(context, screenWidth / 2)
                    val isLeftColumn = (position % 2 == 0)
                    layoutParams.leftMargin = if (isLeftColumn) 0 else spaceBetweenRows
                    layoutParams.rightMargin = if (isLeftColumn) spaceBetweenRows else 0
                    layoutParams.topMargin = if (position < 2) 0 else (spaceBetweenRows / 2)
                    layoutParams.bottomMargin = if (position < 2) (spaceBetweenRows / 2) else 0

                    if (position == 3) {
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
            cardView.setContentPadding(0, 0, 0, 0)
        }


    }


    class FeedDocumentsOnly(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun Int.dpToPx(context: Context): Int {
            return (this * context.resources.displayMetrics.density).toInt()
        }

        private val tag = "FeedDocument"
        private val pdfImageView: ImageView = itemView.findViewById(R.id.pdfImageView)
        private val documentContainer: CardView = itemView.findViewById(R.id.documentContainer)
        private val fileTypeIcon: ImageView = itemView.findViewById(R.id.fileTypeIcon)

        private fun getAdaptiveHeights(context: Context): Pair<Int, Int> {
            val displayMetrics = context.resources.displayMetrics
            val screenHeight = displayMetrics.heightPixels
            val minHeight = (screenHeight * 0.15).toInt()
            val maxHeight = (screenHeight * 0.38).toInt()
            return Pair(minHeight, maxHeight)
        }

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

        private fun navigateToTappedFilesFragment(
            context: Context,
            currentIndex: Int,
            files: List<File>,
            fileIds: List<String>
        ) {
            val activity = getActivityFromContext(context)
            if (activity != null) {
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
                Log.d(
                    tag, "Navigated to Tapped_Files_In_The_Container_View with ${files.size} " +
                            "files, starting at index $currentIndex"
                )
            } else {
                Log.e(tag, "Activity is null, cannot navigate to fragment")
            }
        }

        fun onBind(data: Post) {
            var actualFiles: List<File> = data.files ?: emptyList()
            var actualFileIds: List<String> = data.fileIds as? List<String> ?: emptyList()
            var actualFileTypes: List<FileType> = data.fileTypes ?: emptyList()
            var actualThumbnails: List<ThumbnailX> = data.thumbnail ?: emptyList()

            if (actualFiles.isEmpty() && data.originalPost != null && data.originalPost.isNotEmpty()) {
                val originalPost: OriginalPost = data.originalPost[0]
                actualFiles = originalPost.files ?: emptyList()
                actualFileIds = (originalPost.fileIds ?: emptyList()) as List<String>
                actualFileTypes = originalPost.fileTypes ?: emptyList()
                actualThumbnails = originalPost.thumbnail ?: emptyList()
            }

            itemView.visibility = View.VISIBLE
            documentContainer.removeAllViews()
            pdfImageView.setImageDrawable(null)
            fileTypeIcon.setImageDrawable(null)
            fileTypeIcon.visibility = View.GONE

            val position = absoluteAdapterPosition
            Log.d(tag, "onBind: file type Documents position=$position, item count=${actualFiles.size}, fileIds=$actualFileIds")

            if (actualFiles.isEmpty() || actualFileIds.isEmpty()) {
                Log.e(tag, "No files or fileIds available")
                itemView.visibility = View.GONE
                itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
                return
            }

            if (position < 0 || position >= actualFileIds.size) {
                Log.e(tag, "Invalid absoluteAdapterPosition: $position")
                itemView.visibility = View.GONE
                itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
                return
            }

            val context = itemView.context
            val sideMargin = 2.dpToPx(context)
            val fileIdToFind = actualFileIds[position]
            val documentType = actualFileTypes.find { it.fileId == fileIdToFind }
            val fileSize = actualFiles.size
            val (minHeight, maxHeight) = getAdaptiveHeights(context)

            // Set file type icon
            val fallbackDrawable = when (documentType?.fileType) {
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

            fileTypeIcon.setImageResource(fallbackDrawable)
            fileTypeIcon.visibility = View.VISIBLE

            // Load thumbnail with improved error handling
            val thumbnail = actualThumbnails.find { it.fileId == fileIdToFind }
            pdfImageView.visibility = View.VISIBLE
            if (thumbnail != null && !thumbnail.thumbnailUrl.isNullOrEmpty()) {
                Glide.with(context)
                    .load(thumbnail.thumbnailUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(fallbackDrawable)
                    .error(fallbackDrawable)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            Log.e(tag, "Glide failed to load thumbnail for fileId=$fileIdToFind, url=${thumbnail.thumbnailUrl}, error=${e?.message}")
                            pdfImageView.setImageResource(fallbackDrawable) // Ensure fallback is set on failure
                            return false
                        }



                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable>,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.d(tag, "Glide successfully loaded thumbnail for fileId=$fileIdToFind")
                            return false
                        }
                    })
                    .into(pdfImageView)
            } else {
                Log.w(tag, "No thumbnail found for fileId=$fileIdToFind, using fallback drawable")
                pdfImageView.setImageResource(fallbackDrawable)
            }

            // Set click listeners
            val clickListener = View.OnClickListener {
                navigateToTappedFilesFragment(context, position, actualFiles, actualFileIds)
            }
            itemView.setOnClickListener(clickListener)
            pdfImageView.setOnClickListener(clickListener)
            documentContainer.setOnClickListener(clickListener)
            fileTypeIcon.setOnClickListener(clickListener)

            // Apply layout based on file size
            when {


                fileSize == 1 -> {


                    val adaptiveHeight = getConstrainedHeight(context, (maxHeight * 0.85).toInt())
                    val containerParams = documentContainer.layoutParams as ViewGroup.MarginLayoutParams
                    containerParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                    containerParams.height = adaptiveHeight
                    containerParams.setMargins(0, (-8).dpToPx(context), 0, 0)
                    documentContainer.layoutParams = containerParams

                    val imageLayoutParams = pdfImageView.layoutParams as ViewGroup.MarginLayoutParams
                    imageLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                    imageLayoutParams.height = adaptiveHeight
                    pdfImageView.layoutParams = imageLayoutParams
                    pdfImageView.scaleType = ImageView.ScaleType.FIT_CENTER
                }

                fileSize == 2 -> {
                    val adaptiveHeight = getConstrainedHeight(context, (maxHeight * 0.70).toInt())
                    val containerParams = documentContainer.layoutParams as ViewGroup.MarginLayoutParams
                    containerParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                    containerParams.height = adaptiveHeight
                    when (position) {
                        0 -> containerParams.setMargins(0, 0, sideMargin, 0)
                        1 -> containerParams.setMargins(sideMargin, 0, 0, 0)
                    }
                    documentContainer.layoutParams = containerParams

                    val imageLayoutParams = pdfImageView.layoutParams as ViewGroup.MarginLayoutParams
                    imageLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                    imageLayoutParams.height = adaptiveHeight
                    pdfImageView.layoutParams = imageLayoutParams
                    pdfImageView.scaleType = ImageView.ScaleType.FIT_XY
                }

                fileSize >= 3 -> {
                    if (position >= 2) {
                        itemView.visibility = View.GONE
                        itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
                        return
                    }
                    val adaptiveHeight = getConstrainedHeight(context, (maxHeight * 0.70).toInt())
                    val containerParams = documentContainer.layoutParams as ViewGroup.MarginLayoutParams
                    containerParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                    containerParams.height = adaptiveHeight
                    when (position) {
                        0 -> containerParams.setMargins(0, 0, sideMargin, 0)
                        1 -> containerParams.setMargins(sideMargin, 0, 0, 0)
                    }
                    documentContainer.layoutParams = containerParams

                    val imageLayoutParams = pdfImageView.layoutParams as ViewGroup.MarginLayoutParams
                    imageLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                    imageLayoutParams.height = adaptiveHeight
                    pdfImageView.layoutParams = imageLayoutParams
                    pdfImageView.scaleType = ImageView.ScaleType.FIT_XY

                }

            }

        }


    }


    class FeedCombinationOfMultipleFiles(itemView: View) : RecyclerView.ViewHolder(itemView) {

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

        private fun getAdaptiveHeights(context: Context): Pair<Int, Int> {
            val displayMetrics = context.resources.displayMetrics
            val screenHeight = displayMetrics.heightPixels
            val minHeight = (screenHeight * 0.12).toInt()
            val maxHeight = (screenHeight * 0.35).toInt()
            return Pair(minHeight, maxHeight)
        }

        private fun getConstrainedHeight(context: Context, preferredHeight: Int): Int {
            val (minHeight, maxHeight) = getAdaptiveHeights(context)
            return preferredHeight.coerceIn(minHeight, maxHeight)
        }

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
                    params.marginEnd = 8.dpToPx(context)
                    params.bottomMargin = 8.dpToPx(context)
                    countTextView.layoutParams = params
                }
                is FrameLayout.LayoutParams -> {
                    params.gravity = Gravity.BOTTOM or Gravity.END
                    params.marginEnd = 8.dpToPx(context)
                    params.bottomMargin = 8.dpToPx(context)
                    countTextView.layoutParams = params
                }
                is ViewGroup.MarginLayoutParams -> {
                    params.marginEnd = 8.dpToPx(context)
                    params.bottomMargin = 8.dpToPx(context)
                    countTextView.layoutParams = params
                }
            }
        }

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

            // Apply corner radius to all image views
            val views = listOf(imageView2, fileTypeIcon, playButton, feedVideoImageView, countTextView, feedVideoDurationTextView)
            views.forEach { view ->
                view.clipToOutline = true
                view.outlineProvider = object : ViewOutlineProvider() {
                    override fun getOutline(v: View, outline: Outline) {
                        outline.setRoundRect(0, 0, v.width, v.height, cornerRadius)
                    }
                }
            }
        }

        private fun getActivityFromContext(context: Context): AppCompatActivity? {
            return when (context) {
                is AppCompatActivity -> context
                is ContextWrapper -> getActivityFromContext(context.baseContext)
                else -> null
            }
        }

        private fun isDocument(mimeType: String): Boolean {
            return mimeType.contains("pdf") || mimeType.contains("docx") ||
                    mimeType.contains("pptx") || mimeType.contains("xlsx") ||
                    mimeType.contains("ppt") || mimeType.contains("xls") ||
                    mimeType.contains("txt") || mimeType.contains("rtf") ||
                    mimeType.contains("odt") || mimeType.contains("csv")
        }

        private fun getCorrectFileIndex(
            data: Post,
            actualFiles: List<File>,
            actualFileTypes: List<FileType>,
            currentPosition: Int): Int {

            if (actualFiles.size != 3) return currentPosition

            val documentIndex = actualFileTypes.indexOfFirst { isDocument(it.fileType) }
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

        // New helper method to find thumbnail by fileId
        private fun findThumbnailByFileId(thumbnails: List<ThumbnailX>, fileId: String): ThumbnailX? {
            return thumbnails.find { it.fileId == fileId }
        }

        // New helper method to get thumbnail URL with fallbacks
        private fun getThumbnailUrl(
            thumbnails: List<ThumbnailX>,
            fileId: String, fileUrl: String, mimeType: String): String? {
            // First try to find thumbnail by fileId
            val thumbnail = findThumbnailByFileId(thumbnails, fileId)
            if (thumbnail?.thumbnailUrl?.isNotEmpty() == true) {
                return thumbnail.thumbnailUrl
            }

            // For videos, try to use the video URL itself as Glide can generate thumbnails
            if (mimeType.startsWith("video") && fileUrl.isNotEmpty()) {
                return fileUrl
            }

            // For images, use the original URL
            if (mimeType.startsWith("image") && fileUrl.isNotEmpty()) {
                return fileUrl
            }

            return null
        }

        @SuppressLint("SetTextI18n", "UseKtx")
        fun onBind(data: Post) {
            var actualFiles: List<File> = data.files
            var actualFileIds: List<String> = data.fileIds as List<String>
            var actualFileTypes: List<FileType> = data.fileTypes
            var actualThumbnails: List<ThumbnailX> = data.thumbnail

            // Check if this is a repost with original post data
            if (data.files.isEmpty() && data.originalPost.isNotEmpty()) {
                val originalPost: OriginalPost = data.originalPost[0]
                actualFiles = originalPost.files ?: emptyList()
                actualFileIds = (originalPost.fileIds ?: emptyList()) as List<String>
                actualFileTypes = originalPost.fileTypes ?: emptyList()
                actualThumbnails = originalPost.thumbnail ?: emptyList()
            }

            val context = itemView.context
            val position = absoluteAdapterPosition

            setupCardViewCorners(context)
            itemView.setBackgroundColor(Color.TRANSPARENT)

            val displayMetrics = context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val spaceBetweenRows = 4.dpToPx(context)
            val (minHeight, maxHeight) = getAdaptiveHeights(context)

            // Use actualFileIds and actualFiles for indexing
            val actualFileIndex = getCorrectFileIndex(data, actualFiles, actualFileTypes, position)
            val fileIdToFind = actualFileIds.getOrNull(actualFileIndex) ?: ""
            val file = actualFiles.find { it.fileId == fileIdToFind }
            val fileUrl = file?.url ?: actualFiles.getOrNull(actualFileIndex)?.url ?: ""
            val mimeType = actualFileTypes.getOrNull(actualFileIndex)?.fileType ?: ""
            // Get duration and thumbnail from the correct source
            val actualDuration: List<Duration>


            if (data.files.isEmpty() && data.originalPost.isNotEmpty()) {
                val originalPost = data.originalPost[0]
                actualDuration = originalPost.duration ?: emptyList()
                actualThumbnails = originalPost.thumbnail ?: emptyList()
            } else {
                actualDuration = data.duration ?: emptyList()
                actualThumbnails = data.thumbnail ?: emptyList()
            }

            val durationItem = actualDuration.find { it.fileId == fileIdToFind }
            feedVideoDurationTextView.text = durationItem?.duration ?: "00:00"

            val fileSize = actualFiles.size

            // Reset visibility states
            playButton.visibility = View.GONE
            feedVideoImageView.visibility = View.GONE
            feedVideoDurationTextView.visibility = if (mimeType.startsWith("video")) View.VISIBLE else View.GONE
            imageView2.visibility = View.GONE
            countTextView.visibility = View.GONE
            fileTypeIcon.visibility = View.GONE

            // Set click listeners
            val clickListener = View.OnClickListener {
                navigateToTappedFilesFragment(context, actualFileIndex, actualFiles, actualFileIds)
            }
            itemView.setOnClickListener(clickListener)
            imageView.setOnClickListener(clickListener)
            materialCardView.setOnClickListener(clickListener)
            countTextView.setOnClickListener(clickListener)
            imageView2.setOnClickListener(clickListener)
            feedVideoImageView.setOnClickListener(clickListener)

            val layoutParams = materialCardView.layoutParams as ViewGroup.MarginLayoutParams

            when {


                fileSize == 2 -> {
                    layoutParams.width = screenWidth / 2
                    layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.75).toInt())
                    layoutParams.topMargin = 0
                    layoutParams.bottomMargin = 0
                    val isLeftColumn = (position % 2 == 0)
                    layoutParams.leftMargin = if (isLeftColumn) 0 else (spaceBetweenRows / 2)
                    layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows / 2) else 0
                    loadFileContent(fileUrl, mimeType, actualThumbnails, fileIdToFind, context)
                }

                fileSize == 3 -> {
                    val documentCount = actualFileTypes.count { isDocument(it.fileType) }

                    if (documentCount == 2) {
                        // Two documents case: documents go to positions 0 and 1, non-document to position 2 (hidden)
                        val documentIndices = actualFileTypes.mapIndexed { index, fileType ->
                            if (isDocument(fileType.fileType)) index else -1
                        }.filter { it != -1 }

                        val actualFileIndex = when (position) {
                            0 -> documentIndices[0]
                            1 -> documentIndices[1]
                            2 -> actualFileTypes.indices.find { !documentIndices.contains(it) } ?: 0
                            else -> position
                        }

                        val fileIdToFind = actualFileIds[actualFileIndex]
                        val file = actualFiles.find { it.fileId == fileIdToFind }
                        val fileUrl = file?.url ?: actualFiles.getOrNull(actualFileIndex)?.url ?: ""
                        val mimeType = actualFileTypes.getOrNull(actualFileIndex)?.fileType ?: ""



                        val durationItem = actualDuration.find { it.fileId == fileIdToFind }
                        feedVideoDurationTextView.text = durationItem?.duration ?: "00:00"

                        layoutParams.width = screenWidth / 2
                        val baseFileHeight = getConstrainedHeight(context, (maxHeight * 0.75).toInt())
                        layoutParams.height = baseFileHeight

                        when (position) {
                            0 -> {
                                layoutParams.leftMargin = 0
                                layoutParams.rightMargin = (spaceBetweenRows/2)
                                layoutParams.topMargin = 0
                                layoutParams.bottomMargin = 0
                            }
                            1 -> {
                                layoutParams.leftMargin = (spaceBetweenRows/2)
                                layoutParams.rightMargin = 0
                                layoutParams.topMargin = 0
                                layoutParams.bottomMargin = 0
                                setupCountTextViewStyling(context, "+1")
                            }
                            2 -> {
                                itemView.visibility = View.GONE
                                layoutParams.width = 0
                                layoutParams.height = 0
                                itemView.layoutParams = layoutParams
                                return
                            }
                        }

                        if (position != 2) {
                            itemView.visibility = View.VISIBLE
                            loadFileContent(fileUrl, mimeType, actualThumbnails, fileIdToFind, context)
                        }
                    } else if (documentCount == 1) {
                        // One document case: document goes to position 0, others to positions 1 and 2
                        val documentIndex = actualFileTypes.indexOfFirst { isDocument(it.fileType) }
                        val nonDocumentIndices = actualFileTypes.mapIndexed { index, fileType ->
                            if (!isDocument(fileType.fileType)) index else -1
                        }.filter { it != -1 }

                        val actualFileIndex = when (position) {
                            0 -> documentIndex
                            1 -> nonDocumentIndices.getOrNull(0) ?: 1
                            2 -> nonDocumentIndices.getOrNull(1) ?: 2
                            else -> position
                        }

                        val fileIdToFind = actualFileIds[actualFileIndex]
                        val file = actualFiles.find { it.fileId == fileIdToFind }
                        val fileUrl = file?.url ?: actualFiles.getOrNull(actualFileIndex)?.url ?: ""
                        val mimeType = actualFileTypes.getOrNull(actualFileIndex)?.fileType ?: ""


                        when (position) {
                            0 -> {
                                layoutParams.width = screenWidth / 2
                                layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.75).toInt())
                                layoutParams.leftMargin = 0
                                layoutParams.rightMargin = (spaceBetweenRows/2)
                                layoutParams.topMargin = 0
                                layoutParams.bottomMargin = 0
                            }
                            1, 2 -> {
                                layoutParams.width = screenWidth / 2
                                layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.75).toInt()) / 2
                                layoutParams.leftMargin = (spaceBetweenRows/2)
                                layoutParams.rightMargin = 0
                                layoutParams.topMargin = if (position == 1) 0 else (spaceBetweenRows/2)
                                layoutParams.bottomMargin = if (position == 2) 0 else 0
                            }
                        }

                        loadFileContent(fileUrl, mimeType, actualThumbnails, fileIdToFind, context)

                    } else {
                        // No documents case: use the original layout (left-right split)
                        when (position) {
                            0 -> {
                                layoutParams.width = screenWidth / 2
                                layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.75).toInt())
                                layoutParams.leftMargin = 0
                                layoutParams.rightMargin = (spaceBetweenRows/2)
                                layoutParams.topMargin = 0
                                layoutParams.bottomMargin = 0
                            }
                            1, 2 -> {
                                layoutParams.width = screenWidth / 2
                                layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.75).toInt()) / 2
                                layoutParams.leftMargin = (spaceBetweenRows/2)
                                layoutParams.rightMargin = 0
                                layoutParams.topMargin = if (position == 1) 0 else (spaceBetweenRows/2)
                                layoutParams.bottomMargin = if (position == 2) 0 else (spaceBetweenRows/2)
                            }
                        }

                        loadFileContent(fileUrl, mimeType, actualThumbnails, fileIdToFind, context)

                    }
                }

                fileSize == 4 -> {

                    layoutParams.width = screenWidth / 2
                    layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.75).toInt())
                    layoutParams.topMargin = if (position < 2) 0 else spaceBetweenRows
                    layoutParams.bottomMargin = if (position >= 2) 0 else 0
                    val isLeftColumn = (position % 2 == 0)
                    layoutParams.leftMargin = if (isLeftColumn) 0 else (spaceBetweenRows / 2)
                    layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows / 2) else 0
                    loadFileContent(fileUrl, mimeType, actualThumbnails, fileIdToFind, context)

                }

                fileSize >= 5 -> {
                    if (position >= 4) {
                        itemView.visibility = View.GONE
                        layoutParams.width = 0
                        layoutParams.height = 0
                        itemView.layoutParams = layoutParams
                        return
                    }
                    itemView.visibility = View.VISIBLE
                    layoutParams.width = screenWidth / 2
                    layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.75).toInt())
                    val isLeftColumn = (position % 2 == 0)
                    layoutParams.leftMargin = if (isLeftColumn) 0 else (spaceBetweenRows / 2)
                    layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows / 2) else 0
                    layoutParams.topMargin = if (position < 2) 0 else spaceBetweenRows
                    layoutParams.bottomMargin = if (position < 2) spaceBetweenRows / 2 else 0
                    itemView.layoutParams = layoutParams
                    if (position == 3) {
                        setupCountTextViewStyling(context, "+${fileSize - 4}")
                    } else {
                        countTextView.visibility = View.GONE
                        countTextView.setPadding(0, 0, 0, 0)
                        countTextView.background = null
                    }

                    loadFileContent(fileUrl, mimeType, actualThumbnails, fileIdToFind, context)

                }
            }

            materialCardView.layoutParams = layoutParams
        }

        private fun isMusicAudioFile(url: String): Boolean {
            return url.contains(".mp3", true) || url.contains(".m4a", true)
        }

        private fun isOtherAudioFile(url: String): Boolean {
            return url.contains(".wav", true) || url.contains(".ogg", true) ||
                    url.contains(".flac", true) || url.contains(".aac", true) ||
                    url.contains(".amr", true) || url.contains(".3gp", true) ||
                    url.contains(".opus", true)
        }

        private fun loadFileContent(
            fileUrl: String,
            mimeType: String,
            thumbnails: List<ThumbnailX>,
            fileIdToFind: String,
            context: Context
        ) {
            // Reset image view state
            imageView.setBackgroundColor(Color.TRANSPARENT)
            imageView.setImageDrawable(null)

            when {
                mimeType.startsWith("image") -> {
                    val thumbnailUrl = getThumbnailUrl(thumbnails, fileIdToFind, fileUrl, mimeType)
                    loadImage(thumbnailUrl ?: fileUrl)
                    fileTypeIcon.visibility = View.GONE
                }

                mimeType.startsWith("video") -> {
                    val thumbnailUrl = getThumbnailUrl(thumbnails, fileIdToFind, fileUrl, mimeType)
                    loadVideoThumbnail(thumbnailUrl ?: fileUrl)
                    fileTypeIcon.visibility = View.VISIBLE
                    playButton.visibility = View.VISIBLE
                    feedVideoImageView.visibility = View.VISIBLE

                    // Position the file type icon
                    val params = fileTypeIcon.layoutParams as FrameLayout.LayoutParams
                    params.gravity = Gravity.BOTTOM or Gravity.START
                    params.marginStart = 8.dpToPx(context)
                    params.bottomMargin = 8.dpToPx(context)
                    fileTypeIcon.layoutParams = params
                }

                mimeType.startsWith("audio") -> {
                    // Check if there's a thumbnail for audio
                    val thumbnail = findThumbnailByFileId(thumbnails, fileIdToFind)
                    if (thumbnail?.thumbnailUrl?.isNotEmpty() == true) {
                        // Load thumbnail if available
                        loadImage(thumbnail.thumbnailUrl)
                    } else {
                        // Use default audio handling
                        when {
                            isMusicAudioFile(fileUrl) -> {
                                imageView.setImageResource(R.drawable.music_icon)
                                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                            }
                            isOtherAudioFile(fileUrl) -> {
                                imageView.setBackgroundColor(Color.parseColor("#616161"))
                                imageView.setImageDrawable(null)
                            }
                            else -> {
                                imageView.setImageResource(R.drawable.music_icon)
                                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                            }
                        }
                    }

                    imageView.visibility = View.VISIBLE
                    playButton.visibility = View.GONE
                    fileTypeIcon.visibility = View.VISIBLE
                    fileTypeIcon.setImageResource(R.drawable.ic_audio_white_icon)

                    val params = fileTypeIcon.layoutParams as FrameLayout.LayoutParams
                    params.gravity = Gravity.BOTTOM or Gravity.START
                    params.marginStart = 8.dpToPx(context)
                    params.bottomMargin = 8.dpToPx(context)
                    fileTypeIcon.layoutParams = params
                }

                isDocument(mimeType) -> {
                    val thumbnail = findThumbnailByFileId(thumbnails, fileIdToFind)
                    imageView.scaleType = ImageView.ScaleType.CENTER_CROP

                    // Use the same fallback logic as DocumentsOnly
                    val fallbackDrawable = when {
                        mimeType.contains("pdf") -> R.drawable.pdf_icon
                        mimeType.contains("doc") || mimeType.contains("docx") -> R.drawable.word_icon
                        mimeType.contains("ppt") || mimeType.contains("pptx") -> R.drawable.powerpoint_icon
                        mimeType.contains("xls") || mimeType.contains("xlsx") -> R.drawable.excel_icon
                        mimeType.contains("txt") -> R.drawable.text_icon
                        mimeType.contains("rtf") -> R.drawable.text_icon
                        mimeType.contains("odt") -> R.drawable.word_icon
                        mimeType.contains("csv") -> R.drawable.excel_icon
                        else -> R.drawable.text_icon
                    }

                    if (thumbnail?.thumbnailUrl?.isNotEmpty() == true) {
                        Log.d("ThumbnailLoad", "Loading document thumbnail: ${thumbnail.thumbnailUrl}")
                        Glide.with(itemView.context)
                            .load(thumbnail.thumbnailUrl)
                            .placeholder(fallbackDrawable)
                            .error(fallbackDrawable)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop()
                            .into(imageView)
                    } else {
                        Log.d("ThumbnailLoad", "No thumbnail found for document, using fallback drawable")
                        imageView.setImageResource(fallbackDrawable)
                    }

                    // Set file type icon using the same logic as before
                    fileTypeIcon.setImageResource(fallbackDrawable)
                    fileTypeIcon.visibility = View.VISIBLE
                    imageView.visibility = View.VISIBLE
                }

                else -> {
                    Log.d("ThumbnailLoad", "Unknown file type: $mimeType")
                    imageView.setImageResource(R.drawable.feed_mixed_image_view_rounded_corners)
                    imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                    fileTypeIcon.visibility = View.GONE
                }
            }
        }

        private fun loadImage(url: String) {
            Log.d("ThumbnailLoad", "Loading image: $url")
            Glide.with(itemView.context)
                .load(url)
                .placeholder(R.drawable.flash21)
                .error(R.drawable.flash21)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        Log.e("ThumbnailLoad", "Failed to load image: $url", e)
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable?>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean

                    ): Boolean {
                        Log.d("ThumbnailLoad", "Successfully loaded image: $url")
                        return false
                    }

                })
                .into(imageView)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.clipToOutline = true
        }

        private fun loadVideoThumbnail(url: String) {
            Log.d("ThumbnailLoad", "Loading video thumbnail: $url")
            Glide.with(itemView.context)
                .asBitmap()
                .load(url)
                .placeholder(R.drawable.flash21)
                .error(R.drawable.videoplaceholder) // Use same error as VideosOnly
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                        Log.e("ThumbnailLoad", "Failed to load video thumbnail: $url", e)
                        return false
                    }

                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any?,
                        target: Target<Bitmap?>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d("ThumbnailLoad", "Successfully loaded video thumbnail: $url")
                        return false
                    }
                })
                .into(imageView)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.clipToOutline = true
        }

    }


    class FeedNewPostWithRepostInsideFilesPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val TAG = "FeedRepostedWithNewFilesPostViewHolder"

        // UI Elements - New Post Section (Top)
        private val userProfileImage: ImageView = itemView.findViewById(R.id.userProfileImage)
        private val repostedUserName: TextView = itemView.findViewById(R.id.repostedUserName)
        private val tvUserHandle: TextView = itemView.findViewById(R.id.tvUserHandle)
        private val dateTimeCreate: TextView = itemView.findViewById(R.id.date_time_create)
        private val followButton: AppCompatButton = itemView.findViewById(R.id.followButton)
        private val moreOptionsButton: ImageButton = itemView.findViewById(R.id.moreOptions)

        // Main clickable containers
        private val repostContainer: LinearLayout = itemView.findViewById(R.id.repostContainer)
        private val originalPostContainer: LinearLayout = itemView.findViewById(R.id.originalPostContainer)
        private val quotedPostCard: CardView = itemView.findViewById(R.id.quotedPostCard)

        // New Post Content Section (Top)
        private val tvPostTag: TextView = itemView.findViewById(R.id.tvPostTag)
        private val userComment: TextView = itemView.findViewById(R.id.userComment)
        private val tvHashtags: TextView = itemView.findViewById(R.id.tvHashtags)

        // New Post Media Section (Top)
        private val newPostMediaCard: CardView = itemView.findViewById(R.id.newPostMediaCard)
        private val newPostImage: ImageView = itemView.findViewById(R.id.newPostImage)
        private val newPostMultipleMediaContainer: ConstraintLayout = itemView.findViewById(R.id.newPostMultipleMediaContainer)
        private val newPostMediaRecyclerView: RecyclerView = itemView.findViewById(R.id.newPostMediaRecyclerView)

        // Original Post Media (for backward compatibility)
        private val mixedFilesCardViews: CardView = itemView.findViewById(R.id.mixedFilesCardViews)
        private val originalFeedImages: ImageView = itemView.findViewById(R.id.originalFeedImages)
        private val multipleAudiosContainers: ConstraintLayout = itemView.findViewById(R.id.multipleAudiosContainers)
        private val recyclerViews: RecyclerView = itemView.findViewById(R.id.recyclerViews)

        // Quoted/Original Post Section (Bottom)
        private val originalPosterProfileImage: ImageView = itemView.findViewById(R.id.originalPosterProfileImage)
        private val originalPosterName: TextView = itemView.findViewById(R.id.originalPosterName)
        private val tvQuotedUserHandle: TextView = itemView.findViewById(R.id.tvQuotedUserHandle)
        private val originalPostText: TextView = itemView.findViewById(R.id.originalPostText)
        private val tvQuotedHashtags: TextView = itemView.findViewById(R.id.tvQuotedHashtags)

        // Quoted Post Media
        private val mixedFilesCardView: CardView = itemView.findViewById(R.id.mixedFilesCardView)
        private val originalFeedImage: ImageView = itemView.findViewById(R.id.originalFeedImage)
        private val multipleAudiosContainer: ConstraintLayout = itemView.findViewById(R.id.multipleAudiosContainer)
        private val recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerView)
        private val ivQuotedPostImage: ImageView = itemView.findViewById(R.id.ivQuotedPostImage)

        // Interaction Buttons
        private val likeSection: LinearLayout = itemView.findViewById(R.id.likeLayout)
        private val likeButton: ImageView = itemView.findViewById(R.id.likeButtonIcon)
        private val likesCount: TextView = itemView.findViewById(R.id.likesCount)

        private val commentSection: LinearLayout = itemView.findViewById(R.id.commentLayout)
        private val commentButton: ImageView = itemView.findViewById(R.id.commentButtonIcon)
        private val feedCommentsCount: TextView = itemView.findViewById(R.id.commentCount)

        private val favoriteSection: LinearLayout = itemView.findViewById(R.id.favoriteSection)
        private val favoriteButton: ImageView = itemView.findViewById(R.id.favoritesButton)
        private val favCount: TextView = itemView.findViewById(R.id.favoriteCounts)

        private val retweetSection: LinearLayout = itemView.findViewById(R.id.repostPost)
        private val repostPost: ImageView = itemView.findViewById(R.id.repostPost)
        private val repostCountTextView: TextView = itemView.findViewById(R.id.repostCount)

        private val shareSection: LinearLayout = itemView.findViewById(R.id.shareButtonIcon)
        private val shareImageView: ImageView = itemView.findViewById(R.id.shareButtonIcon)
        private val shareCountTextView: TextView = itemView.findViewById(R.id.shareCount)

        // Additional UI elements
        private val feedMixedFilesContainer: CardView = itemView.findViewById(R.id.feedMixedFilesContainer)
        private val bottomDivider: View = itemView.findViewById(R.id.bottomDivider)
        private val interactionButtonsCard: LinearLayout = itemView.findViewById(R.id.interactionButtonsCard)

        // State variables - ID management
        private var currentPostId: String = ""
        private var currentAuthorId: String = ""
        private var originalPostId: String = ""
        private var originalAuthorId: String = ""
        private var repostedUserId: String = ""

        // Other state variables
        private var isFollowed = false
        private var totalMixedComments = 0
        private var serverCommentCount = 0
        private var loadedCommentCount = 0
        private var currentPost: Post? = null
        private var totalMixedLikesCounts = 0
        private var totalMixedBookMarkCounts = 0
        private var totalMixedShareCounts = 0
        private var totalMixedRePostCounts = 0
        private var postClicked = false

        // For media adapter - requires these views
        private val materialCardView: CardView by lazy { itemView.findViewById(R.id.materialCardView) }
        private val imageView: ImageView by lazy { itemView.findViewById(R.id.imageView) }
        private val imageView2: ImageView by lazy { itemView.findViewById(R.id.imageView2) }
        private val fileTypeIcon: ImageView by lazy { itemView.findViewById(R.id.fileTypeIcon) }
        private val playButton: ImageView by lazy { itemView.findViewById(R.id.playButton) }
        private val feedVideoImageView: ImageView by lazy { itemView.findViewById(R.id.feedVideoImageView) }
        private val countTextView: TextView by lazy { itemView.findViewById(R.id.countTextView) }
        private val feedVideoDurationTextView: TextView by lazy { itemView.findViewById(R.id.feedVideoDurationTextView) }

        fun Int.dpToPx(context: Context): Int {
            return (this * context.resources.displayMetrics.density).toInt()
        }

        private fun getAdaptiveHeights(context: Context): Pair<Int, Int> {
            val displayMetrics = context.resources.displayMetrics
            val screenHeight = displayMetrics.heightPixels
            val minHeight = (screenHeight * 0.12).toInt()
            val maxHeight = (screenHeight * 0.35).toInt()
            return Pair(minHeight, maxHeight)
        }

        private fun getConstrainedHeight(context: Context, preferredHeight: Int): Int {
            val (minHeight, maxHeight) = getAdaptiveHeights(context)
            return preferredHeight.coerceIn(minHeight, maxHeight)
        }

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
                    params.marginEnd = 8.dpToPx(context)
                    params.bottomMargin = 8.dpToPx(context)
                    countTextView.layoutParams = params
                }
                is FrameLayout.LayoutParams -> {
                    params.gravity = Gravity.BOTTOM or Gravity.END
                    params.marginEnd = 8.dpToPx(context)
                    params.bottomMargin = 8.dpToPx(context)
                    countTextView.layoutParams = params
                }
                is ViewGroup.MarginLayoutParams -> {
                    params.marginEnd = 8.dpToPx(context)
                    params.bottomMargin = 8.dpToPx(context)
                    countTextView.layoutParams = params
                }
            }
        }

        private fun setupCardViewCorners(context: Context) {
            val cornerRadius = 8.dpToPx(context).toFloat()

            materialCardView.radius = cornerRadius
            materialCardView.clipToOutline = true
            materialCardView.clipChildren = true
            materialCardView.cardElevation = 0f
            materialCardView.maxCardElevation = 0f

            materialCardView.setContentPadding(0, 0, 0, 0)
            materialCardView.useCompatPadding = false
            materialCardView.setCardBackgroundColor(Color.WHITE)

            val views = listOf(imageView, imageView2, fileTypeIcon, playButton, feedVideoImageView, countTextView, feedVideoDurationTextView)
            views.forEach { view ->
                view.clipToOutline = true
                view.outlineProvider = object : ViewOutlineProvider() {
                    override fun getOutline(view: View, outline: Outline) {
                        outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
                    }
                }
            }

            val imageLayoutParams = imageView.layoutParams as FrameLayout.LayoutParams
            imageLayoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT
            imageLayoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT
            imageLayoutParams.setMargins(0, 0, 0, 0)
            imageView.layoutParams = imageLayoutParams
        }

        private fun getActivityFromContext(context: Context): AppCompatActivity? {
            return when (context) {
                is AppCompatActivity -> context
                is ContextWrapper -> getActivityFromContext(context.baseContext)
                else -> null
            }
        }

        private fun isDocument(mimeType: String): Boolean {
            return mimeType.contains("pdf") || mimeType.contains("docx") ||
                    mimeType.contains("pptx") || mimeType.contains("xlsx") ||
                    mimeType.contains("ppt") || mimeType.contains("xls") ||
                    mimeType.contains("txt") || mimeType.contains("rtf") ||
                    mimeType.contains("odt") || mimeType.contains("csv")
        }

        private fun getCorrectFileIndex(
            data: Post,
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

                    // Pass relevant IDs
                    putString("current_post_id", currentPostId)
                    putString("current_author_id", currentAuthorId)
                    putString("original_post_id", originalPostId)
                    putString("original_author_id", originalAuthorId)

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

        @OptIn(UnstableApi::class)
        @SuppressLint("SetTextI18n", "SuspiciousIndentation")
        fun render(data: Post) {
            Log.d(TAG, "render: feed data $data")

            // Store current post reference and extract all IDs
            currentPost = data
            extractAndStoreIds(data)

            totalMixedComments = data.comments
            totalMixedLikesCounts = data.likes
            totalMixedBookMarkCounts = data.bookmarkCount
            totalMixedShareCounts = data.shareCount
            totalMixedRePostCounts = data.repostCount


            setupNewPostMediaFiles(data)
            setupOriginalPostContent(data)
            setupEngagementButtons(data)
            setupProfileClickListeners(data)
            setupFollowButton()
            setupPostClickListeners(data)
            ensurePostClickability(data)
            setupInteractionButtonsClickPrevention()
        }

        private fun extractAndStoreIds(data: Post) {
            // Extract main post ID
            currentPostId = data._id

            // Extract reposter/author IDs
            repostedUserId = data.repostedUser?._id ?: ""
            currentAuthorId = data.author?.account?._id ?: repostedUserId

            // Extract original post IDs if this is a repost
            if (data.originalPost?.isNotEmpty() == true) {
                val originalPostData = data.originalPost[0]
                originalPostId = originalPostData._id

                // Get original author ID
                originalAuthorId = originalPostData.author._id
            }

            Log.d(TAG, "IDs extracted - Post: $currentPostId, Author: $currentAuthorId, " +
                    "Reposter: $repostedUserId, Original Post: $originalPostId, Original Author: $originalAuthorId")
        }

        @SuppressLint("SetTextI18n", "UseKtx")
        fun onBind(data: Post) {
            val context = itemView.context

            val position = absoluteAdapterPosition + 1

            setupCardViewCorners(context)
            itemView.setBackgroundColor(Color.TRANSPARENT)

            val displayMetrics = context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val spaceBetweenRows = 4.dpToPx(context)
            val (minHeight, maxHeight) = getAdaptiveHeights(context)

            val actualFileIndex = if (data.files.size == 3) getCorrectFileIndex(data, position) else {
                position
            }

            val fileIdToFind = data.fileIds[actualFileIndex]
            val file = data.files.find { it.fileId == fileIdToFind }
            val fileUrl = file?.url ?: data.files.getOrNull(actualFileIndex)?.url ?: ""
            val mimeType = data.fileTypes.getOrNull(actualFileIndex)?.fileType ?: ""
            val durationItem = data.duration?.find { it.fileId == fileIdToFind }
            feedVideoDurationTextView.text = durationItem?.duration

            val fileSize = data.files.size

            // Reset visibility
            playButton.visibility = View.GONE
            feedVideoImageView.visibility = View.GONE
            feedVideoDurationTextView.visibility = View.VISIBLE
            imageView2.visibility = View.GONE
            countTextView.visibility = View.GONE

            // Setup click listeners with proper ID passing
            setupFileItemClickListeners(context, actualFileIndex, data)

            val layoutParams = materialCardView.layoutParams as ViewGroup.MarginLayoutParams

            when {
                fileSize == 2 -> {
                    setupTwoFileLayout(layoutParams, screenWidth, spaceBetweenRows, context, maxHeight)
                    loadFileContent(fileUrl, mimeType, data, fileIdToFind.toString(), context)
                }
                fileSize == 3 -> {
                    setupThreeFileLayout(layoutParams, screenWidth, spaceBetweenRows,
                        context, maxHeight, data, actualFileIndex,
                        fileIdToFind.toString(), fileUrl, mimeType)
                }
                fileSize == 4 -> {
                    setupFourFileLayout(layoutParams, screenWidth, spaceBetweenRows, context, maxHeight)
                    loadFileContent(fileUrl, mimeType, data, fileIdToFind.toString(), context)
                }
                fileSize == 5 -> {
                    setupFiveFileLayout(layoutParams, screenWidth, spaceBetweenRows, context, maxHeight, fileSize)
                    if (position < 4) {
                        loadFileContent(fileUrl, mimeType, data, fileIdToFind.toString(), context)
                    }
                }
                fileSize > 4 -> {
                    setupMoreThanFourFileLayout(layoutParams, screenWidth, spaceBetweenRows, context, maxHeight, fileSize)
                    if (position < 4) {
                        loadFileContent(fileUrl, mimeType, data, fileIdToFind.toString(), context)
                    }
                }
            }

            materialCardView.layoutParams = layoutParams
        }

        private fun setupFileItemClickListeners(
            context: Context,
            actualFileIndex: Int,
            data: Post
        ) {
            val clickListener = View.OnClickListener {
                navigateToTappedFilesFragment(
                    context,
                    actualFileIndex,
                    data.files,
                    data.fileIds as List<String>
                )
            }

            // Apply the same click listener to all relevant views
            listOf(itemView, imageView, materialCardView, countTextView, imageView2, feedVideoImageView)
                .forEach { it.setOnClickListener(clickListener) }
        }

        private fun setupTwoFileLayout(
            layoutParams: ViewGroup.MarginLayoutParams,
            screenWidth: Int,
            spaceBetweenRows: Int,
            context: Context,
            maxHeight: Int
        ) {
            layoutParams.width = screenWidth / 2
            layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.75).toInt())
            layoutParams.topMargin = 0
            layoutParams.bottomMargin = 0

            val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
            layoutParams.leftMargin = if (isLeftColumn) 0 else (spaceBetweenRows / 2)
            layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows / 2) else 0
        }

        private fun setupThreeFileLayout(
            layoutParams: ViewGroup.MarginLayoutParams,
            screenWidth: Int,
            spaceBetweenRows: Int,
            context: Context,
            maxHeight: Int,
            data: Post,
            actualFileIndex: Int,
            fileIdToFind: String,
            fileUrl: String,
            mimeType: String
        ) {
            val documentCount = data.fileTypes.count { isDocument(it.fileType) }

            if (documentCount == 2) {
                setupThreeFileLayoutWithTwoDocuments(layoutParams, screenWidth, spaceBetweenRows, context, maxHeight, data, actualFileIndex, fileIdToFind, fileUrl, mimeType)
            } else {
                setupThreeFileLayoutStandard(layoutParams, screenWidth, spaceBetweenRows, context, maxHeight, fileUrl, mimeType, data, fileIdToFind.toString())
            }
        }

        private fun setupThreeFileLayoutWithTwoDocuments(
            layoutParams: ViewGroup.MarginLayoutParams,
            screenWidth: Int,
            spaceBetweenRows: Int,
            context: Context,
            maxHeight: Int,
            data: Post,
            actualFileIndex: Int,
            fileIdToFind: String,
            fileUrl: String,
            mimeType: String
        ) {
            val documentIndices = data.fileTypes.mapIndexed { index, fileType ->
                if (isDocument(fileType.fileType)) index else -1
            }.filter { it != -1 }

            val correctedActualFileIndex = when (absoluteAdapterPosition) {
                0 -> documentIndices[0]
                1 -> documentIndices[1]
                2 -> data.fileTypes.indices.find { !documentIndices.contains(it) } ?: 0
                else -> actualFileIndex
            }

            val correctedFileIdToFind = data.fileIds[correctedActualFileIndex]
            val file = data.files.find { it.fileId == correctedFileIdToFind }
            val correctedFileUrl = file?.url ?: data.files.getOrNull(correctedActualFileIndex)?.url ?: ""
            val correctedMimeType = data.fileTypes.getOrNull(correctedActualFileIndex)?.fileType ?: ""
            val durationItem = data.duration?.find { it.fileId == correctedFileIdToFind }
            feedVideoDurationTextView.text = durationItem?.duration

            layoutParams.width = screenWidth / 2
            val baseFileHeight = getConstrainedHeight(context, (maxHeight * 0.8).toInt())
            layoutParams.height = baseFileHeight

            when (absoluteAdapterPosition) {
                0 -> {
                    layoutParams.leftMargin = 0
                    layoutParams.rightMargin = (spaceBetweenRows / 2)
                    layoutParams.topMargin = 0
                    layoutParams.bottomMargin = 0
                }
                1 -> {
                    layoutParams.leftMargin = (spaceBetweenRows / 2)
                    layoutParams.rightMargin = 0
                    layoutParams.topMargin = 0
                    layoutParams.bottomMargin = 0
                    setupCountTextViewStyling(context, "+1")
                }
                2 -> {
                    itemView.visibility = View.GONE
                    layoutParams.width = 0
                    layoutParams.height = 0
                    itemView.layoutParams = layoutParams
                    return
                }
            }

            if (absoluteAdapterPosition != 2) {
                itemView.visibility = View.VISIBLE
                loadFileContent(correctedFileUrl, correctedMimeType, data, correctedFileIdToFind.toString(), context)
            }
        }

        private fun setupThreeFileLayoutStandard(
            layoutParams: ViewGroup.MarginLayoutParams,
            screenWidth: Int,
            spaceBetweenRows: Int,
            context: Context,
            maxHeight: Int,
            fileUrl: String,
            mimeType: String,
            data: Post,
            fileIdToFind: String
        ) {
            when (absoluteAdapterPosition) {
                0 -> {
                    layoutParams.width = screenWidth / 2
                    val baseFileHeight = getConstrainedHeight(context, (maxHeight * 0.8).toInt())
                    val rightSideItemHeight = baseFileHeight / 2
                    val totalRightSideHeight = (rightSideItemHeight * 2) + (spaceBetweenRows / 2)
                    layoutParams.height = totalRightSideHeight
                    layoutParams.leftMargin = 0
                    layoutParams.rightMargin = (spaceBetweenRows / 2)
                    layoutParams.topMargin = 0
                    layoutParams.bottomMargin = 0
                }
                1 -> {
                    layoutParams.width = screenWidth / 2
                    val baseFileHeight = getConstrainedHeight(context, (maxHeight * 0.8).toInt())
                    val totalHeight = baseFileHeight + (spaceBetweenRows / 2)
                    layoutParams.height = (totalHeight - (spaceBetweenRows / 2)) / 2
                    layoutParams.leftMargin = (spaceBetweenRows / 2)
                    layoutParams.rightMargin = 0
                    layoutParams.topMargin = 0
                    layoutParams.bottomMargin = 0
                }
                2 -> {
                    layoutParams.width = screenWidth / 2
                    val baseFileHeight = getConstrainedHeight(context, (maxHeight * 0.8).toInt())
                    val totalHeight = baseFileHeight + (spaceBetweenRows / 2)
                    layoutParams.height = (totalHeight - (spaceBetweenRows / 2)) / 2
                    layoutParams.leftMargin = (spaceBetweenRows / 2)
                    layoutParams.rightMargin = 0
                    layoutParams.topMargin = (spaceBetweenRows / 2)
                    layoutParams.bottomMargin = 0
                }
            }
            loadFileContent(fileUrl, mimeType, data, fileIdToFind, context)
        }

        private fun setupFourFileLayout(
            layoutParams: ViewGroup.MarginLayoutParams,
            screenWidth: Int,
            spaceBetweenRows: Int,
            context: Context,
            maxHeight: Int
        ) {
            layoutParams.width = screenWidth / 2
            layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.75).toInt())

            layoutParams.topMargin = if (absoluteAdapterPosition < 2) 0 else spaceBetweenRows
            layoutParams.bottomMargin = 0

            val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
            layoutParams.leftMargin = if (isLeftColumn) 0 else (spaceBetweenRows / 2)
            layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows / 2) else 0
        }

        private fun setupFiveFileLayout(
            layoutParams: ViewGroup.MarginLayoutParams,
            screenWidth: Int,
            spaceBetweenRows: Int,
            context: Context,
            maxHeight: Int,
            fileSize: Int
        ) {
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
            layoutParams.leftMargin = if (isLeftColumn) 0 else (spaceBetweenRows / 2)
            layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows / 2) else 0
            layoutParams.topMargin = if (absoluteAdapterPosition < 2) 0 else spaceBetweenRows
            layoutParams.bottomMargin = if (absoluteAdapterPosition < 2) spaceBetweenRows / 2 else 0

            itemView.layoutParams = layoutParams

            if (absoluteAdapterPosition == 3) {
                setupCountTextViewStyling(context, "+${fileSize - 4}")
            } else {
                countTextView.visibility = View.GONE
                countTextView.setPadding(0, 0, 0, 0)
                countTextView.background = null
            }
        }

        private fun setupMoreThanFourFileLayout(
            layoutParams: ViewGroup.MarginLayoutParams,
            screenWidth: Int,
            spaceBetweenRows: Int,
            context: Context,
            maxHeight: Int,
            fileSize: Int
        ) {
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
            layoutParams.leftMargin = if (isLeftColumn) 0 else spaceBetweenRows / 2
            layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows / 2) else 0
            layoutParams.topMargin = if (absoluteAdapterPosition < 2) 0 else spaceBetweenRows
            layoutParams.bottomMargin = if (absoluteAdapterPosition < 2) spaceBetweenRows / 2 else 0

            itemView.layoutParams = layoutParams

            if (absoluteAdapterPosition == 3) {
                setupCountTextViewStyling(context, "+${fileSize - 4}")
            } else {
                countTextView.visibility = View.GONE
                countTextView.setPadding(0, 0, 0, 0)
                countTextView.background = null
            }
        }

        private fun loadFileContent(
            fileUrl: String,
            mimeType: String,
            data: Post,
            fileId: String,
            context: Context
        ) {
            when {
                mimeType.contains("video") -> {
                    loadVideoContent(fileUrl, context)
                }
                mimeType.contains("image") -> {
                    loadImageContent(fileUrl, context)
                }
                mimeType.contains("audio") -> {
                    loadAudioContent(fileUrl, context)
                }
                isDocument(mimeType) -> {
                    loadDocumentContent(fileUrl, mimeType, context)
                }
                else -> {
                    loadImageContent(fileUrl, context) // Default fallback
                }
            }
        }

        private fun loadVideoContent(fileUrl: String, context: Context) {
            playButton.visibility = View.VISIBLE
            feedVideoImageView.visibility = View.VISIBLE
            feedVideoDurationTextView.visibility = View.VISIBLE

            // Load video thumbnail using Glide
            Glide.with(context)
                .load(fileUrl)
                .placeholder(R.drawable.videoplaceholder)
                .error(R.drawable.videoplaceholder)
                .centerCrop()
                .into(feedVideoImageView)

            imageView.visibility = View.GONE
            imageView2.visibility = View.GONE
        }

        private fun loadImageContent(fileUrl: String, context: Context) {
            imageView.visibility = View.VISIBLE
            playButton.visibility = View.GONE
            feedVideoImageView.visibility = View.GONE
            feedVideoDurationTextView.visibility = View.GONE
            imageView2.visibility = View.GONE

            // Load image using Glide
            Glide.with(context)
                .load(fileUrl)
                .placeholder(R.drawable.imageplaceholder)
                .error(R.drawable.imageplaceholder)
                .centerCrop()
                .into(imageView)
        }

        private fun loadAudioContent(fileUrl: String, context: Context) {
            imageView2.visibility = View.VISIBLE
            playButton.visibility = View.GONE
            imageView.visibility = View.GONE
            feedVideoImageView.visibility = View.GONE
            feedVideoDurationTextView.visibility = View.VISIBLE

            // Set audio placeholder
            imageView2.setImageResource(R.drawable.music_icon)
        }

        private fun loadDocumentContent(fileUrl: String, mimeType: String, context: Context) {
            imageView2.visibility = View.VISIBLE
            fileTypeIcon.visibility = View.VISIBLE
            imageView.visibility = View.GONE
            playButton.visibility = View.GONE
            feedVideoImageView.visibility = View.GONE
            feedVideoDurationTextView.visibility = View.GONE

            // Set document icon based on type
            val iconRes = when {
                mimeType.contains("pdf") -> R.drawable.pdf_icon
                mimeType.contains("docx") || mimeType.contains("doc") -> R.drawable.word_icon
                mimeType.contains("pptx") || mimeType.contains("ppt") -> R.drawable.powerpoint_icon
                mimeType.contains("xlsx") || mimeType.contains("xls") -> R.drawable.excel_icon
                mimeType.contains("txt") -> R.drawable.text_icon
                else -> R.drawable.documents
            }


            fileTypeIcon.setImageResource(iconRes)
        }



        private fun setupNewPostMediaFiles(data: Post) {
            // Handle new post media files if they exist
            if (data.files.isNotEmpty()) {
                newPostMediaCard.visibility = View.VISIBLE
                setupNewPostMediaRecyclerView(data)
            } else {
                newPostMediaCard.visibility = View.GONE
            }
        }

        private fun setupNewPostMediaRecyclerView(data: Post) {
            // Setup RecyclerView for new post media
            newPostMediaRecyclerView.layoutManager = GridLayoutManager(itemView.context, 2)
            // Set adapter for media files
            // This would typically use a separate adapter for media files
        }


        private fun setupOriginalPostContent(data: Post) {
            if (data.originalPost?.isNotEmpty() == true) {
                val originalPost = data.originalPost[0]
                quotedPostCard.visibility = View.VISIBLE


                setupOriginalPostMedia(originalPost)
            } else {
                quotedPostCard.visibility = View.GONE
            }
        }


        private fun setupOriginalPostMedia(originalPost: OriginalPost) {
            // Handle original post media files
            if (originalPost.files.isNotEmpty()) {
                // Show appropriate media container based on content type
                // This is a simplified version - you'd need to implement based on your requirements
                mixedFilesCardView.visibility = View.VISIBLE

                // Load first image as preview
                val firstFile = originalPost.files[0]
                Glide.with(itemView.context)
                    .load(firstFile.url)
                    .placeholder(R.drawable.imageplaceholder)
                    .error(R.drawable.imageplaceholder)
                    .centerCrop()
                    .into(originalFeedImage)
            } else {
                mixedFilesCardView.visibility = View.GONE
            }
        }

        private fun setupEngagementButtons(data: Post) {
            // Update like button state and count
            updateLikeButton(data.isLiked)
            likesCount.text = formatCount(totalMixedLikesCounts)

            // Update comment count
            feedCommentsCount.text = formatCount(totalMixedComments)

            // Update bookmark button state and count
            updateBookmarkButton(data.isBookmarked)
            favCount.text = formatCount(totalMixedBookMarkCounts)

            // Update repost button state and count
            updateRepostButton(data.isReposted)
            repostCountTextView.text = formatCount(totalMixedRePostCounts)

            // Update share count
            shareCountTextView.text = formatCount(totalMixedShareCounts)
        }

        private fun updateLikeButton(isLiked: Boolean) {
            if (isLiked) {
                likeButton.setImageResource(R.drawable.heart_svgrepo_com)

            } else {
                likeButton.setImageResource(com.uyscuti.social.business.R.drawable.ic_heart)

            }
        }

        private fun updateBookmarkButton(isBookmarked: Boolean) {
            if (isBookmarked) {
                favoriteButton.setImageResource(R.drawable.favorite_svgrepo_com__1_)

            } else {
                favoriteButton.setImageResource(R.drawable.filled_favorite)

            }
        }

        private fun updateRepostButton(isReposted: Boolean) {
            if (isReposted) {
                repostPost.setImageResource(R.drawable.retweet)

            } else {
                repostPost.setImageResource(R.drawable.retweet)

            }
        }

        private fun setupProfileClickListeners(data: Post) {
            val profileClickListener = View.OnClickListener {
                // Navigate to user profile
                navigateToUserProfile(data.repostedUser?._id ?: data.author?.account?._id ?: "")
            }

            userProfileImage.setOnClickListener(profileClickListener)
            repostedUserName.setOnClickListener(profileClickListener)

            // Original poster profile click
            if (data.originalPost?.isNotEmpty() == true) {
                val originalProfileClickListener = View.OnClickListener {
                    navigateToUserProfile(originalAuthorId)
                }

                originalPosterProfileImage.setOnClickListener(originalProfileClickListener)
                originalPosterName.setOnClickListener(originalProfileClickListener)
            }
        }

        private fun setupFollowButton() {
            followButton.setOnClickListener {
                // Handle follow/unfollow logic
                handleFollowAction()
            }

            // Update follow button text based on follow status
            followButton.text = if (isFollowed) "Following" else "Follow"
        }

        private fun setupPostClickListeners(data: Post) {
            val postClickListener = View.OnClickListener {
                if (!postClicked) {
                    navigateToPostDetail(data)
                }
            }

            // Apply to main post areas but not interaction buttons
            repostContainer.setOnClickListener(postClickListener)
            originalPostContainer.setOnClickListener(postClickListener)
            userComment.setOnClickListener(postClickListener)
            originalPostText.setOnClickListener(postClickListener)
        }

        private fun ensurePostClickability(data: Post) {
            // Ensure the main containers are clickable
            repostContainer.isClickable = true
            originalPostContainer.isClickable = true
            quotedPostCard.isClickable = true
        }

        private fun setupInteractionButtonsClickPrevention() {
            // Prevent post click when interaction buttons are tapped
            val preventPostClickListener = View.OnClickListener { postClicked = true }

            likeSection.setOnClickListener { handleLikeAction() }
            commentSection.setOnClickListener { handleCommentAction() }
            favoriteSection.setOnClickListener { handleBookmarkAction() }
            retweetSection.setOnClickListener { handleRepostAction() }
            shareSection.setOnClickListener { handleShareAction() }

            // Reset postClicked flag after a delay
            itemView.postDelayed({ postClicked = false }, 300)
        }

        // Action handlers
        private fun handleLikeAction() {
            // Implement like/unlike logic
        }

        private fun handleCommentAction() {
            // Navigate to comments or open comment dialog
        }

        private fun handleBookmarkAction() {
            // Implement bookmark/unbookmark logic
        }

        private fun handleRepostAction() {
            // Implement repost logic
        }

        private fun handleShareAction() {
            // Implement share functionality
        }

        private fun handleFollowAction() {
            // Implement follow/unfollow logic
            isFollowed = !isFollowed
            followButton.text = if (isFollowed) "Following" else "Follow"
        }

        // Navigation methods
        private fun navigateToUserProfile(userId: String) {
            if (userId.isNotEmpty()) {
                // Navigate to user profile fragment/activity
            }
        }

        private fun navigateToPostDetail(data: Post) {
            // Navigate to post detail fragment/activity
        }

        // Utility methods
        private fun formatDate(dateString: String?): String {
            // Implement date formatting logic
            return dateString ?: ""
        }

        private fun formatCount(count: Int): String {
            return when {
                count < 1000 -> count.toString()
                count < 1000000 -> String.format("%.1fK", count / 1000.0)
                else -> String.format("%.1fM", count / 1000000.0)
            }
        }
    }



    private fun handleMenuButtonClick() = showToast("Options menu")


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
            "Unknown time"
        }
    }

    private fun formatCount(count: Int?): String {
        if (count != null) {
            return when {
                count >= 1000000 -> "${count / 1000000}M"
                count >= 1000 -> "${count / 1000}K"
                else -> count.toString()
            }
        }
        return "0"
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDetach() {
        super.onDetach()
        isNavigating = false
    }





}