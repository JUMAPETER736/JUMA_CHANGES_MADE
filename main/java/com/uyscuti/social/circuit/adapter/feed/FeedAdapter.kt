package com.uyscuti.social.circuit.adapter.feed

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.GestureDetector
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.media3.common.util.UnstableApi
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
import com.google.gson.Gson
import com.google.gson.stream.MalformedJsonException
import com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.Fragment_Original_Post_With_Repost_Inside
import com.uyscuti.social.circuit.User_Interface.fragments.feed.feedviewfragments.Fragment_Original_Post_Without_Repost_Inside
import com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.feedRepost.Tapped_Files_In_The_Container_View_Fragment
import com.uyscuti.social.business.retro.model.User
import com.uyscuti.social.circuit.FollowingManager
import com.uyscuti.social.circuit.adapter.feed.multiple_files.FeedMixedFilesViewAdapter
import com.uyscuti.social.circuit.adapter.feed.multiple_files.FeedRepostViewFileAdapter
import com.uyscuti.social.circuit.adapter.feed.multiple_files.OnMultipleFilesClickListener
import com.uyscuti.social.circuit.data.model.shortsmodels.OtherUsersProfile
import com.uyscuti.social.circuit.model.GoToUserProfileFragment
import com.uyscuti.social.circuit.model.ShortsFollowButtonClicked
import com.uyscuti.social.network.api.response.posts.Post
import com.uyscuti.social.network.api.response.posts.OriginalPost
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.User_Interface.OtherUserProfile.OtherUserProfileAccount
import com.uyscuti.social.circuit.User_Interface.fragments.feed.feedviewfragments.editRepost.Fragment_Edit_Post_To_Repost
import com.uyscuti.social.core.common.data.room.entity.FollowUnFollowEntity
import com.uyscuti.social.core.common.data.room.entity.ShortsEntity
import com.uyscuti.social.core.common.data.room.entity.ShortsEntityFollowList
import com.uyscuti.social.network.api.response.posts.Avatar
import com.uyscuti.social.network.api.response.allFeedRepostsPost.BookmarkRequest
import com.uyscuti.social.network.api.response.allFeedRepostsPost.BookmarkResponse
import com.uyscuti.social.network.api.response.allFeedRepostsPost.CommentCountResponse
import com.uyscuti.social.network.api.response.allFeedRepostsPost.CommentsResponse
import com.uyscuti.social.network.api.response.allFeedRepostsPost.LikeRequest
import com.uyscuti.social.network.api.response.allFeedRepostsPost.LikeResponse
//import com.uyscuti.social.network.api.response.posts.OriginalPost
import com.uyscuti.social.network.api.response.allFeedRepostsPost.RepostResponse
import com.uyscuti.social.network.api.response.allFeedRepostsPost.RetrofitClient
import com.uyscuti.social.network.api.response.allFeedRepostsPost.ShareResponse
import com.uyscuti.social.network.api.response.comment.allcomments.Comment
import com.uyscuti.social.network.api.response.getrepostsPostsoriginal.File
import com.uyscuti.social.network.api.response.posts.Author
import com.uyscuti.social.network.api.response.posts.AuthorX
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Collections.addAll
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs


internal const val VIEW_TYPE_TEXT_FEED = 0
internal const val VIEW_TYPE_MIXED_FEED_FILES = 1
internal const val VIEW_TPE_REPOST_POST = 3
private const val VIEW_TRENDING_SHORTS = 4
internal const val  VIEW_TPE_REPOST_POST_WITH_NEW_FILES  = 5
internal const val VIEW_TYPE_VOICE_NOTE = 6

private const val TAG = "FeedAdapter"



class FeedAdapter(

    private val context: Context,
    private val feedClickListener: OnFeedClickListener,
    private var currentCommentCount: Int = 0,
    private var followingUserIds: Set<String> = emptySet()

) : FeedPaginatedAdapter<RecyclerView.ViewHolder>(), OnMultipleImagesClickListener {

    // Track comment counts by post ID for more reliable updates
    private val commentCountMap = mutableMapOf<String, Int>()

    private var followList: MutableList<ShortsEntityFollowList> =
        mutableListOf()

    private var currentItemDisplayPosition = -1


    companion object {

        private const val PRELOAD_AHEAD_COUNT = 20
        // Static set to persist across adapter instances
        private var cachedFollowingUserIds: Set<String> = emptySet()

        fun setCachedFollowingList(userIds: Set<String>) {
            cachedFollowingUserIds = userIds
            Log.d("FeedAdapter", "Cached following list updated with ${userIds.size} users")
        }

        fun getCachedFollowingList(): Set<String> = cachedFollowingUserIds
    }

    init {
        // Load from cache on initialization
        followingUserIds = cachedFollowingUserIds
        Log.d("FeedAdapter", "Initialized with ${followingUserIds.size} following users from cache")
    }


    fun clearItems() {
        submitItems(mutableListOf())
        notifyDataSetChanged()
    }

    private fun updatePostsForUser(userId: String) {
        for (i in 0 until itemCount) {
            val post = getItem(i) as? Post
            if (post?.author?.account?._id == userId) {
                notifyItemChanged(i)
            }
        }
    }

    fun addToFollowing(userId: String) {
        followingUserIds = followingUserIds + userId
        cachedFollowingUserIds = followingUserIds
        Log.d("FeedAdapter", "Added user $userId to following list")
        updatePostsForUser(userId)

        // Save to local storage
        saveFollowingListToStorage(context, followingUserIds)
    }

    fun removeFromFollowing(userId: String) {
        followingUserIds = followingUserIds - userId
        cachedFollowingUserIds = followingUserIds
        Log.d("FeedAdapter", "Removed user $userId from following list")
        updatePostsForUser(userId)

        // Save to local storage
        saveFollowingListToStorage(context, followingUserIds)
    }

    private fun saveFollowingListToStorage(context: Context, userIds: Set<String>) {
        try {
            val localStorage = LocalStorage.getInstance(context)
            val json = Gson().toJson(userIds.toList())
            localStorage.saveFollowingList(json)
            Log.d("FeedAdapter", "Saved ${userIds.size} following users to storage")
        } catch (e: Exception) {
            Log.e("FeedAdapter", "Error saving following list", e)
        }
    }


    fun updateFollowingList(newFollowingIds: Set<String>) {
        followingUserIds = newFollowingIds
        cachedFollowingUserIds = newFollowingIds
        Log.d("FeedAdapter", "Following list updated: ${followingUserIds.size} users")

        // Save to storage
        saveFollowingListToStorage(context, followingUserIds)
    }

    fun isUserFollowing(userId: String): Boolean {
        return followingUserIds.contains(userId) || cachedFollowingUserIds.contains(userId)
    }





            @SuppressLint("NotifyDataSetChanged")
    fun updatePosts(newPosts: List<Post>) {
        clear()
        addAll(newPosts.toMutableList())
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun appendPosts(newPosts: List<Post>) {
        val startPosition = itemCount
        addAll(newPosts.toMutableList())
        initializeCommentCounts(newPosts)
        notifyItemRangeInserted(startPosition, newPosts.size)
    }


    var onItemVisible: ((Int) -> Unit)? = null



    // Initialize comment counts when setting data
    fun initializeCommentCounts(posts: List<Post>) {
        posts.forEach { post ->
            val initialCount = when {
                post.originalPost?.isNotEmpty() == true -> {
                    // For reposts, use original post comment count
                    post.originalPost[0].commentCount ?: 0
                }
                else -> {
                    // For regular posts
                     0
                }
            }
            commentCountMap[post._id] = initialCount
        }
    }

    // Method to update comment count for a specific post
    fun updateCommentCount(postId: String, increment: Int) {
        val currentCount = commentCountMap[postId] ?: 0
        val newCount = (currentCount + increment).coerceAtLeast(0)
        commentCountMap[postId] = newCount

        Log.d("FeedAdapter", "Updated comment count for post $postId: $currentCount -> $newCount")

        // Find the position of this post and update the view
        val position = findPostPosition(postId)
        if (position != -1) {
            notifyItemChanged(position, "comment_count_update")
        }
    }

    // Helper method to find post position in the list
    private fun findPostPosition(postId: String): Int {
        for (i in 0 until itemCount) {
            val post = getItem(i) as? Post
            if (post != null) {
                when {
                    post._id == postId -> return i
                    post.originalPost?.any { it._id == postId } == true -> return i
                }
            }
        }
        return -1
    }

    fun refreshPostCommentCount(postId: String) {
        val position = findPostPosition(postId)
        if (position != -1) {
            Log.d("FeedAdapter", "Refreshing comment count for post at position $position")
            notifyItemChanged(position)
        }
    }

    fun notifyCommentAdded(postId: String) {
        val position = findPostPosition(postId)
        if (position != -1) {
            Log.d("FeedAdapter", "Notifying comment added for post at position $position")
            // Update the post data and refresh the view
            notifyItemChanged(position)
        }
    }



    // Enhanced helper method to get the correct comment count
    private fun getCommentCount(post: Post): Int {
        return commentCountMap[post._id] ?: run {
            // Fallback to original count if not in map
            val originalCount = when {
                post.originalPost?.isNotEmpty() == true -> {
                    post.originalPost[0].commentCount ?: 0
                }
                else -> {
                     0
                }
            }
            commentCountMap[post._id] = originalCount
            originalCount
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)


        Log.d(TAG, "View Type $viewType")

        return when (viewType) {

            VIEW_TYPE_TEXT_FEED -> {
                val itemView = inflater.inflate(
                    R.layout.feed_original_text_post_adapter, parent, false
                )
                FeedTextOnyViewHolder(itemView)
            }

            VIEW_TYPE_VOICE_NOTE -> {
                val itemView = inflater.inflate(
                    R.layout.feed_mixed_files_original_post_adapter, parent, false
                )
                FeedPostViewHolder(itemView)
            }

            VIEW_TYPE_MIXED_FEED_FILES -> {
                val itemView = inflater.inflate(
                    R.layout.feed_mixed_files_original_post_adapter, parent, false
                )
                FeedPostViewHolder(itemView)
            }

            VIEW_TPE_REPOST_POST -> {
                val itemView = inflater.inflate(
                    R.layout.feed_mixed_files_original_post_with_repost_adapter, parent, false
                )
                FeedRepostViewHolder(itemView)
            }

            VIEW_TPE_REPOST_POST_WITH_NEW_FILES -> {
                val itemView = inflater.inflate(
                    R.layout.feed_mixed_files_new_post_with_reposted_files_inside_adapter,
                    parent, false
                )
                FeedNewPostWithRepostInsideFilesPostViewHolder(itemView)
            }


            VIEW_TRENDING_SHORTS -> {
                val itemView = inflater.inflate(
                    R.layout.item_trending_video, parent, false
                )
                TrendingVideosPostViewHolder(itemView)
            }

            else -> throw IllegalArgumentException("Invalid view type")

        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {

    }



    fun getCurrentItemDisplayPosition(): Int {
        return currentItemDisplayPosition
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        @SuppressLint("RecyclerView") position: Int,
        payloads: MutableList<Any>

    ) {
        currentItemDisplayPosition = position
        when (holder) {

            is FeedTextOnyViewHolder -> {
                holder.render(getItem(position))
            }

            is FeedPostViewHolder -> {
                holder.render(getItem(position))
            }

            is FeedRepostViewHolder -> {
                holder.render(getItem(position))
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val tag = "FeedType"

        val item = getItem(position)
        val contentType = item.contentType ?: ""

        if (item.isReposted) {

            return VIEW_TPE_REPOST_POST
        }

        if (contentType.isEmpty()){
            Log.d(tag, "Get Item View Type: empty")
            Log.d(tag, "type : $contentType")
            Log.d(tag, "post : ${getItem(position)}")
            return  VIEW_TYPE_TEXT_FEED
        }
        else {
            if(false) {

                return  VIEW_TYPE_TEXT_FEED

            } else {

                return when (item.contentType) {

                    "text" -> {
                        VIEW_TYPE_TEXT_FEED
                    }

                    "mixed_files" -> {
                        VIEW_TYPE_MIXED_FEED_FILES
                    }

                    "videos" -> {
                        VIEW_TRENDING_SHORTS
                    }

                    "vn" ->  {
                        VIEW_TYPE_VOICE_NOTE
                    }

                    else -> {
                        Log.d(tag, "Get Item View Type: Unknown Type")
                        VIEW_TYPE_TEXT_FEED
                    }
                }
            }

        }
    }

    fun addFollowList(follow: List<ShortsEntityFollowList>) {
        this.followList.addAll(follow)
    }

    fun getFollowList(): List<ShortsEntityFollowList> {
        return followList
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addSingleFollowList(follow: ShortsEntityFollowList) {
        followList.add(follow)
        notifyDataSetChanged()
    }

    override fun multipleImagesClickListener() {

    }

    override fun SetOnPaginationListener(onPaginationListener: OnPaginationListener) {

    }


    inner class FeedTextOnyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val TAG = "FeedTextOnlyViewHolder"

        // UI Components
        // Header Section Views
        private val profileImageView: ImageView = itemView.findViewById(R.id.profileImageView)
        private val textView: TextView = itemView.findViewById(R.id.textView)
        private val handerText: TextView = itemView.findViewById(R.id.handerText)
        private val dateTime: TextView = itemView.findViewById(R.id.date_time)
        private val followButton: AppCompatButton = itemView.findViewById(R.id.followButton)
        private val moreOptionsButton: ImageView = itemView.findViewById(R.id.moreOptions)

        // Content Section Views
        private val caption: ReadMoreTextView = itemView.findViewById(R.id.caption)
        private val tags: TextView = itemView.findViewById(R.id.tags)

        // Media Section Views
        private val recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerView)

        // Interaction Buttons
        private val likeButton: ImageView = itemView.findViewById(R.id.likeButtonIcon)
        private val commentButton: ImageView = itemView.findViewById(R.id.commentButtonIcon)
        private val favoriteButton: ImageView = itemView.findViewById(R.id.favoriteSection)
        private val repostedPost: ImageView = itemView.findViewById(R.id.repostPost)
        private val feedShare: ImageView = itemView.findViewById(R.id.shareButtonIcon)

        // Interaction Counters
        private val likesCount: TextView = itemView.findViewById(R.id.likesCount)
        private val commentCount: TextView = itemView.findViewById(R.id.commentCount)
        private val favoriteCounts: TextView = itemView.findViewById(R.id.favoriteCounts)
        private val repostCount: TextView = itemView.findViewById(R.id.repostCount)
        private val shareCount: TextView = itemView.findViewById(R.id.shareCount)

        // Container Views
        private val feedTextLayoutContainer: ConstraintLayout = itemView.findViewById(R.id.feedMixedFilesContainer)

        // State variables
        private var isFollowed = false
        private var totalTextComments = 0
        private var currentPost: Post? = null
        private var totalTextLikesCounts = 0
        private var totalTextBookMarkCounts = 0
        private var totalTextShareCounts = 0
        private var totalTextRePostCounts = 0
        private var postClicked = false
        private var isFollowingUser = false

        private val Post.safeCommentCount: Int
            get() = 0

        private val Post.safeLikes: Int
            get() = likes

        private val Post.safeBookmarkCount: Int
            get() = bookmarkCount

        private var Post.safeRepostCount: Int
            get() = repostCount
            set(value) {
                repostCount = value
            }

        private var Post.safeShareCount: Int
            get() = shareCount
            set(value) {
                shareCount = value
            }

        @OptIn(UnstableApi::class)
        @SuppressLint("SetTextI18n", "SimpleDateFormat", "SuspiciousIndentation")
        fun render(data: Post) {
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

        private fun setupFollowButton(data: Post) {
            val feedOwnerId = data.author?.account?._id ?: return
            val currentUserId = LocalStorage.getInstance(itemView.context).getUserId()

            // Check multiple sources for following status
            val isUserFollowing = followingUserIds.contains(feedOwnerId) ||
                    FeedAdapter.getCachedFollowingList().contains(feedOwnerId)

            // Hide follow button if viewing own post OR already following
            if (feedOwnerId == currentUserId || isFollowingUser || isUserFollowing) {
                followButton.visibility = View.GONE
                Log.d(TAG, "setupFollowButton: Hidden for user $feedOwnerId - Following: true")
                return
            }

            // Show follow button only for users we're NOT following
            followButton.visibility = View.VISIBLE
            followButton.text = "Follow"
            followButton.backgroundTintList = ContextCompat.getColorStateList(
                itemView.context,
                R.color.blueJeans
            )

            followButton.setOnClickListener {
                handleFollowButtonClick(feedOwnerId)
            }
        }

        @SuppressLint("SetTextI18n")
        private fun handleFollowButtonClick(feedOwnerId: String) {
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
                (bindingAdapter as? FeedAdapter)?.addToFollowing(feedOwnerId)

                // Also update via manager for consistency
                FollowingManager(itemView.context).addToFollowing(feedOwnerId)

                Log.d(TAG, "Now following user $feedOwnerId")
            } else {
                // Show button
                followButton.text = "Follow"
                followButton.visibility = View.VISIBLE

                // Remove from adapter's following list AND persistent storage
                (bindingAdapter as? FeedAdapter)?.removeFromFollowing(feedOwnerId)

                // Also update via manager for consistency
                FollowingManager(itemView.context).removeFromFollowing(feedOwnerId)

                Log.d(TAG, "Unfollowed user $feedOwnerId")
            }

            // Notify listener
            feedClickListener.followButtonClicked(followEntity, followButton)
            EventBus.getDefault().post(ShortsFollowButtonClicked(followEntity))
        }

        private fun setupPostClickListeners(data: Post) {
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

        private fun preventChildClickInterference(data: Post) {
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

        private fun setupChildClickBubbling(data: Post) {
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

        private fun ensurePostClickability(data: Post) {
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

        private fun getCommentCount(data: Post): Int {
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

        private fun getLikesCount(data: Post): Int {
            return when {
                data.likes >= 0 -> data.likes
                data.safeLikes >= 0 -> data.safeLikes
                else -> 0
            }
        }

        private fun getBookmarkCount(data: Post): Int {
            return when {
                data.bookmarkCount >= 0 -> data.bookmarkCount
                data.safeBookmarkCount >= 0 -> data.safeBookmarkCount
                else -> 0
            }
        }

        private fun getRepostCount(data: Post): Int {
            return when {
                data.safeRepostCount != null -> data.safeRepostCount!!
                data.safeRepostCount >= 0 -> data.safeRepostCount
                else -> 0
            }
        }

        private fun getShareCount(data: Post): Int {
            return when {
                data.safeShareCount >= 0 -> data.safeShareCount
                data.safeShareCount >= 0 -> data.safeShareCount
                else -> 0
            }
        }

        private fun setupInteractionButtons(data: Post) {
            setupLikeButton(data)
            setupBookmarkButton(data)
            setupCommentButton(data)
            setupRepostButton(data)
            setupShareButton(data)
            setupMoreOptionsButton(data)
            setupFollowButton(data)
        }

        private fun setupLikeButton(data: Post) {
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

        private fun setupCommentButton(data: Post) {

            commentButton.setOnClickListener {
                if (!commentButton.isEnabled) return@setOnClickListener
                Log.d(com.uyscuti.social.circuit.adapter.feed.TAG, "Comment button clicked for post ${data._id}")

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

        private fun setupBookmarkButton(data: Post) {
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

        private fun setupRepostButton(data: Post) {

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

        private fun setupShareButton(data: Post) {
            updateMetricDisplay(shareCount, data.safeShareCount, "share")
            feedShare.setOnClickListener {
                if (!feedShare.isEnabled) return@setOnClickListener

                Log.d(TAG, "Share clicked for post: ${data._id}")
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
        }

        private fun setupMoreOptionsButton(data: Post) {
            moreOptionsButton.setOnClickListener {
                feedClickListener.moreOptionsClick(absoluteAdapterPosition, data)
            }
        }



        private fun updateLikeButtonUI(isLiked: Boolean) {
            Log.d(TAG, "Updating like button UI: isLiked=$isLiked")
            try {
                if (isLiked) {
                    likeButton.setImageResource(R.drawable.filled_favorite_like)
                    // Add blue color tint for liked state
                    likeButton.setColorFilter(ContextCompat.getColor(itemView.context, R.color.bluejeans), PorterDuff.Mode.SRC_IN)
                } else {
                    likeButton.setImageResource(R.drawable.heart_svgrepo_com)
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
                    favoriteButton.setImageResource(R.drawable.filled_favorite)
                } else {
                    favoriteButton.setImageResource(R.drawable.favorite_svgrepo_com__1_)
                    // Remove color filter for unfilled state
                    favoriteButton.clearColorFilter()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating bookmark button UI", e)
            }
        }

        private fun updateRepostButtonAppearance(isReposted: Boolean) {
            if (isReposted) {
                repostedPost.setImageResource(R.drawable.repeat_svgrepo_com)
                repostedPost.scaleX = 1.1f
                repostedPost.scaleY = 1.1f
            } else {
                repostedPost.setImageResource(R.drawable.repeat_svgrepo_com)
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
            data: Post,
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

        private fun setupUserProfile(data: Post) {

            dateTime.text = formattedMongoDateTime(data.createdAt)

            val fullName = listOfNotNull(
                data.author?.firstName?.takeIf { it.isNotBlank() },
                data.author?.lastName?.takeIf { it.isNotBlank() }
            ).joinToString(" ").trim()
            textView.text = if (fullName.isNotEmpty()) fullName else data.author?.account?.username ?: "Unknown User"
            loadImageWithGlide(data.author?.account?.avatar?.url, profileImageView, itemView.context)
        }

        private fun navigateToOriginalPostWithoutRepostInside(data: Post) {
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

        private fun setupContentAndCaption(data: Post) {
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

        private fun logCountDebuggingInfo(data: Post) {
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
                    .placeholder(R.drawable.flash21)
                    .error(R.drawable.flash21)
                    .into(imageView)
            } else {
                imageView.setImageResource(R.drawable.flash21)
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
                "now"
            }
        }

    }

    inner class FeedPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val TAG = "FeedPostViewHolder"
        // Profile and Header Elements
        private val profileImageView: ImageView = itemView.findViewById(R.id.profileImageView)
        private val textView: TextView = itemView.findViewById(R.id.textView)
        private val handerText: TextView = itemView.findViewById(R.id.handerText)
        private val dateTime: TextView = itemView.findViewById(R.id.date_time)
        private val followButton: AppCompatButton = itemView.findViewById(R.id.followButton)
        private val moreOptionsButton: ImageView = itemView.findViewById(R.id.moreOptions)

        // Content Elements
        private val caption: ReadMoreTextView = itemView.findViewById(R.id.caption)
        private val tags: TextView = itemView.findViewById(R.id.tags)

        // Media Elements
        private val mixedFilesCardView: CardView = itemView.findViewById(R.id.mixedFilesCardView)
        val recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerView)

        // Interaction Elements
        private val likeButton: ImageView = itemView.findViewById(R.id.likeButtonIcon)
        private val likesCount: TextView = itemView.findViewById(R.id.likesCount)
        private val commentButton: ImageView = itemView.findViewById(R.id.commentButtonIcon)
        private val commentCount: TextView = itemView.findViewById(R.id.commentCount)
        private val favoriteButton: ImageView = itemView.findViewById(R.id.favoriteSection)
        private val favoriteCounts: TextView = itemView.findViewById(R.id.favoriteCounts)
        private val repostPost: ImageView = itemView.findViewById(R.id.repostPost)
        private val repostCount: TextView = itemView.findViewById(R.id.repostCount)
        private val feedShare: ImageView = itemView.findViewById(R.id.shareButtonIcon)
        private val shareCountText: TextView = itemView.findViewById(R.id.shareCount)

        // Container Elements
        private val feedMixedFilesContainer: ConstraintLayout = itemView.findViewById(R.id.feedMixedFilesContainer)

        // State variables
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
        private var isFollowingUser = false


        @OptIn(UnstableApi::class)
        @SuppressLint("SetTextI18n", "SuspiciousIndentation")
        fun render(data: Post) {
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
            setupFollowButton(feedOwnerId)
            setupPostClickListeners(data)
            ensurePostClickability(data)

        }


        private fun setupFollowButton(feedOwnerId: String) {
            val currentUserId = LocalStorage.getInstance(itemView.context).getUserId()

            // Check multiple sources for following status
            val isUserFollowing = followingUserIds.contains(feedOwnerId) ||
                    FeedAdapter.getCachedFollowingList().contains(feedOwnerId)


            if (feedOwnerId == currentUserId || isFollowingUser || isUserFollowing) {
                followButton.visibility = View.GONE
                Log.d(TAG, "setupFollowButton: Hidden for user $feedOwnerId - Following: true")
                return
            }

            // Show follow button only for users we're NOT following
            followButton.visibility = View.VISIBLE
            followButton.text = "Follow"
            followButton.backgroundTintList = ContextCompat.getColorStateList(
                itemView.context,
                R.color.blueJeans
            )

            followButton.setOnClickListener {
                handleFollowButtonClick(feedOwnerId)
            }
        }

        @SuppressLint("SetTextI18n")
        private fun handleFollowButtonClick(feedOwnerId: String) {
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
                (bindingAdapter as? FeedAdapter)?.addToFollowing(feedOwnerId)

                // Also update via manager for consistency
                FollowingManager(itemView.context).addToFollowing(feedOwnerId)

                Log.d(TAG, "Now following user $feedOwnerId")
            } else {
                // Show button
                followButton.text = "Follow"
                followButton.visibility = View.VISIBLE

                // Remove from adapter's following list AND persistent storage
                (bindingAdapter as? FeedAdapter)?.removeFromFollowing(feedOwnerId)

                // Also update via manager for consistency
                FollowingManager(itemView.context).removeFromFollowing(feedOwnerId)

                Log.d(TAG, "Unfollowed user $feedOwnerId")
            }

            // Notify listener
            feedClickListener.followButtonClicked(followEntity, followButton)
            EventBus.getDefault().post(ShortsFollowButtonClicked(followEntity))
        }

        private fun setupPostClickListeners(data: Post) {
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

        private fun preventChildClickInterference(data: Post) {
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

        private fun ensurePostClickability(data: Post) {
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

        private fun setupContentAndTags(data: Post) {
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

        private fun navigateToOriginalPostWithoutRepostInside(data: Post) {
            try {
                Log.d(TAG, "Navigating to original Post for Post ID: ${data._id}")

                // ✅ Extract author information from the Post
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

                        // ✅ ADD AUTHOR INFORMATION
                        putString("author_name", displayName)
                        putString("author_username", data.author?.account?.username ?: "unknown_user")
                        putString("author_profile_image_url", data.author?.account?.avatar?.url ?: "")
                        putString("user_id", data.author?._id ?: "")

                        // ✅ Log for debugging
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
                            R.anim.slide_in_right,
                            R.anim.slide_out_left,
                          //  R.anim.slide_in_left,
                          //  R.anim.slide_out_right
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

        private fun setupPostInfo(data: Post) {
            // Date and time
            dateTime.text = formattedMongoDateTime(data.createdAt)

            // Comment count
            initializeCommentCounts(data)
            updateCommentCountDisplay()

            // Initialize all counts
            updateEngagementCounts(data)
        }

        private fun initializeCommentCounts(data: Post) {
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

        private fun setupUserInfo(data: Post, feedOwnerId: String) {
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

        private fun setupEngagementButtons(data: Post) {
            setupLikeButton(data)
            setupCommentButton(data)
            setupShareButton(data)
            setupRepostButton(data)
            setupBookmarkButton(data)
            setupMoreOptionsButton(data)
        }

        private fun setupLikeButton(data: Post) {
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

        private fun setupBookmarkButton(data: Post) {
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
                    likeButton.setImageResource(R.drawable.filled_favorite_like)
                    // Add blue color tint for liked state
                    likeButton.setColorFilter(ContextCompat.getColor(itemView.context, R.color.bluejeans), PorterDuff.Mode.SRC_IN)
                } else {
                    likeButton.setImageResource(R.drawable.heart_svgrepo_com)
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
                    favoriteButton.setImageResource(R.drawable.filled_favorite)
                } else {
                    favoriteButton.setImageResource(R.drawable.favorite_svgrepo_com__1_)
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

        private fun setupCommentButton(data: Post) {
            commentButton.setOnClickListener {
                if (!commentButton.isEnabled) return@setOnClickListener
                Log.d(com.uyscuti.social.circuit.adapter.feed.TAG, "Comment button clicked for post ${data._id}")

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

        private fun setupShareButton(data: Post) {
            val originalPost = data.originalPost?.firstOrNull()
            val targetPostId = originalPost?._id ?: data._id  // Use original post ID for API calls

            updateMetricDisplay(shareCountText, 0, "share")
            feedShare.setOnClickListener {
                if (!feedShare.isEnabled) return@setOnClickListener

                Log.d(TAG, "Share clicked for post: $targetPostId")


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
        }

        private fun setupRepostButton(data: Post) {
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
//                    data.repostCount = totalMixedRePostCounts
                    updateMetricDisplay(repostCount, totalMixedRePostCounts, "repost")
                    updateRepostButtonAppearance(data.isReposted)

                    YoYo.with(if (data.isReposted) Techniques.Tada else Techniques.Pulse)
                        .duration(700)
                        .playOn(repostPost)

                    repostPost.alpha = 0.8f

                    // Use targetPostId for API call
                    val apiCall = if (data.isReposted) {
                        RetrofitClient.repostService.incrementRepost(targetPostId)
                    } else {
                        RetrofitClient.repostService.decrementRepost(targetPostId)
                    }

                    apiCall.enqueue(object : Callback<RepostResponse> {
                        override fun onResponse(call: Call<RepostResponse>, response: Response<RepostResponse>) {
                            repostPost.isEnabled = true
                            repostPost.alpha = 1f
                            if (response.isSuccessful) {
                                response.body()?.let { repostResponse ->
                                    if (abs(repostResponse.repostCount - totalMixedRePostCounts) > 1) {
//                                        data.repostCount = repostResponse.repostCount
                                        totalMixedRePostCounts = repostResponse.repostCount
                                        updateMetricDisplay(repostCount, totalMixedRePostCounts, "repost")
                                    }
                                }
                            }
                        }

                        override fun onFailure(call: Call<RepostResponse>, t: Throwable) {
                            repostPost.isEnabled = true
                            repostPost.alpha = 1f
                            Log.e(TAG, "Repost network error", t)
                        }
                    })


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
                repostPost.setImageResource(R.drawable.repeat_svgrepo_com)
                repostPost.scaleX = 1.1f
                repostPost.scaleY = 1.1f
            } else {
                repostPost.setImageResource(R.drawable.repeat_svgrepo_com)
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


        private fun setupMoreOptionsButton(data: Post) {
            moreOptionsButton.setOnClickListener {
                feedClickListener.moreOptionsClick(absoluteAdapterPosition, data)
            }
        }



        private fun setupProfileClickListeners(data: Post, feedOwnerId: String) {
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


        private fun updateEngagementCounts(data: Post) {
            likesCount.text = formatCount(totalMixedLikesCounts)
            updateMetricDisplay(favoriteCounts, totalMixedBookMarkCounts, "bookmark")
            updateMetricDisplay(repostCount, totalMixedRePostCounts, "repost")
            updateMetricDisplay(shareCountText, totalMixedShareCounts, "share")
            Log.d(TAG, "Updated all engagement counts")
        }

        private fun setupMediaFiles(data: Post) {
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

        private val Post.safeRepostCount: Int
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
                "now"
            }
        }

        private fun loadImageWithGlide(imageUrl: String?, imageView: ImageView, context: Context) {
            if (!imageUrl.isNullOrBlank()) {
                Glide.with(context)
                    .load(imageUrl)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.flash21)
                    .error(R.drawable.flash21)
                    .into(imageView)
            } else {
                imageView.setImageResource(R.drawable.flash21)
            }
        }

    }

    inner class FeedRepostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // Constants and Fields
        private val tag = "FeedRepostViewHolder"

        private var isFollowed = false
        private var currentPost: Post? = null
        private var totalMixedLikesCounts = 0
        private var totalMixedBookMarkCounts = 0
        private var totalMixedShareCounts = 0
        private var totalMixedRePostCounts = 0
        private var totalRepostComments = 0
        private var totalMixedComments = 0
        private var isFollowingUser = false


        // UI Components - User Info Section
        private val userProfileImage: ImageView = itemView.findViewById(R.id.userReposterProfileImage)
        private val repostedUserName: TextView = itemView.findViewById(R.id.repostedUserName)
        private val tvUserHandle: TextView = itemView.findViewById(R.id.tvUserHandle)
        private val dateTimeCreate: TextView = itemView.findViewById(R.id.date_time_create)
        private val followButton: AppCompatButton = itemView.findViewById(R.id.followButton)
        private val moreOptionsButton: ImageButton = itemView.findViewById(R.id.moreOptions)

        // Main clickable containers
        private val repostContainer: LinearLayout = itemView.findViewById(R.id.repostContainer)
        private val originalPostContainer: LinearLayout? = itemView.findViewById(R.id.originalPostContainer)
        private val quotedPostCard: CardView = itemView.findViewById(R.id.quotedPostCard)

        // Post Content Section
        private val tvPostTag: TextView = itemView.findViewById(R.id.tvPostTag)
        private val userComment: TextView = itemView.findViewById(R.id.userComment)
        private val tvHashtags: TextView = itemView.findViewById(R.id.tvHashtags)

        // Original Mixed Files Section
        private val mixedFilesCardViews: CardView = itemView.findViewById(R.id.mixedFilesCardViews)
        private val originalFeedImages: ImageView = itemView.findViewById(R.id.originalFeedImages)
        private val multipleAudiosContainers: ConstraintLayout = itemView.findViewById(R.id.multipleAudiosContainers)
        private val recyclerViews: RecyclerView = itemView.findViewById(R.id.recyclerViews)

        // Quoted/Original Post Section
        private val originalPosterProfileImage: ImageView? = itemView.findViewById(R.id.originalPosterProfileImage)
        private val originalPosterName: TextView? = itemView.findViewById(R.id.originalPosterName)
        private val tvQuotedUserHandle: TextView? = itemView.findViewById(R.id.tvQuotedUserHandle)
        private val originalPostText: TextView? = itemView.findViewById(R.id.originalPostText)
        private val tvQuotedHashtags: TextView? = itemView.findViewById(R.id.tvQuotedHashtags)

        // Quoted Post Media
        private val mixedFilesCardView: CardView? = itemView.findViewById(R.id.mixedFilesCardView)
        private val originalFeedImage: ImageView? = itemView.findViewById(R.id.originalFeedImage)
        private val multipleAudiosContainer: ConstraintLayout? = itemView.findViewById(R.id.multipleAudiosContainer)
        private val recyclerView: RecyclerView? = itemView.findViewById(R.id.recyclerView)
        private val ivQuotedPostImage: ImageView? = itemView.findViewById(R.id.ivQuotedPostImage)

        // Interaction Buttons
        private val likeSection: LinearLayout = itemView.findViewById(R.id.likeLayout)
        private val likeButton: ImageView = itemView.findViewById(R.id.likeButtonIcon)
        private val likesCount: TextView = itemView.findViewById(R.id.likesCount)

        private val commentSection: LinearLayout = itemView.findViewById(R.id.commentLayout)
        private val commentButton: ImageView = itemView.findViewById(R.id.commentButtonIcon)
        private val commentCount: TextView = itemView.findViewById(R.id.commentCount)

        private val favoriteSection: LinearLayout = itemView.findViewById(R.id.favoriteSection)
        private val favoriteButton: ImageView = itemView.findViewById(R.id.favoritesButton)
        private val favoriteCounts: TextView = itemView.findViewById(R.id.favoriteCounts)

        private val repostSection: LinearLayout = itemView.findViewById(R.id.repostLayout)
        private val repostButton: ImageView = itemView.findViewById(R.id.repostPost)
        private val repostCounts: TextView = itemView.findViewById(R.id.repostCount)

        private val shareSection: LinearLayout = itemView.findViewById(R.id.share_layout)
        private val shareButton: ImageView = itemView.findViewById(R.id.shareButtonIcon)
        private val shareCounts: TextView = itemView.findViewById(R.id.shareCount)


        // In your FeedRepostViewHolder class, replace the render() method:

        @OptIn(UnstableApi::class)
        @SuppressLint("SetTextI18n", "CheckResult", "SuspiciousIndentation")
        fun render(data: Post) {
            currentPost = data

            // ✅ FIX: Use the correct ID based on data structure
            val feedReposterOwnerId = when {
                // If there's a reposted user, use their ACCOUNT ID
                data.repostedUser != null -> data.repostedUser._id

                // If there's an original post, use the original AUTHOR's OWNER ID (account ID)
                data.originalPost != null && data.originalPost.isNotEmpty() -> {
                    data.originalPost[0].author.owner // This is the account ID
                }

                // Fallback to main author's account ID
                else -> data.author?.account?._id ?: "Unknown"
            }

            // Enhanced following check with detailed logging
            val cachedFollowingList = FeedAdapter.getCachedFollowingList()
            isFollowingUser = followingUserIds.contains(feedReposterOwnerId) ||
                    cachedFollowingList.contains(feedReposterOwnerId)

            Log.d(TAG, "REPOST FOLLOW CHECK")
            Log.d(TAG, "Post ID: ${data._id}")
            Log.d(TAG, "Reposted User ID: ${data.repostedUser?._id}")
            Log.d(TAG, "Original Author Owner ID: ${data.originalPost?.firstOrNull()?.author?.owner}")
            Log.d(TAG, "Main Author Account ID: ${data.author?.account?._id}")
            Log.d(TAG, "Selected feedReposterOwnerId: $feedReposterOwnerId")
            Log.d(TAG, "isFollowingUser: $isFollowingUser")
            Log.d(TAG, "followingUserIds contains: ${followingUserIds.contains(feedReposterOwnerId)}")
            Log.d(TAG, "cachedFollowingList contains: ${cachedFollowingList.contains(feedReposterOwnerId)}")
            Log.d(TAG, "followingUserIds size: ${followingUserIds.size}")
            Log.d(TAG, "cachedFollowingList size: ${cachedFollowingList.size}")

            totalMixedComments = data.comments
            totalMixedLikesCounts = data.likes
            totalMixedBookMarkCounts = data.bookmarkCount
            totalMixedShareCounts = data.shareCount
            totalMixedRePostCounts = data.repostCount
            totalRepostComments = totalMixedComments

            // Setup all content first
            setupRepostedUser(data)
            setupOriginalPostContent(data)
            dateTimeCreate.text = formattedMongoDateTime(data.createdAt)

            // Setup interaction buttons
            setupLikeButton(data)
            setupBookmarkButton(data)
            setupRepostButton(data)
            setupShareButton(data)
            setupCommentButton(data)

            // Update displays
            updateMetricDisplay(likesCount, totalMixedLikesCounts, "like")
            updateMetricDisplay(commentCount, totalMixedComments, "comment")
            updateMetricDisplay(favoriteCounts, totalMixedBookMarkCounts, "bookmark")
            updateMetricDisplay(repostCounts, totalMixedRePostCounts, "repost")
            updateMetricDisplay(shareCounts, totalMixedShareCounts, "share")

            // Setup follow button with correct ID
            setupFollowButton(feedReposterOwnerId)
            setupMoreOptionsButton(data)
            setupFileTapNavigation(data)
            finalizeClickSetup(data)
            setupRepostedUserProfileClicks(data)
            setupOriginalPostAuthorClicks(data)
        }

        // ✅ Also update setupFollowButton to handle both ID formats
        private fun setupFollowButton(feedOwnerId: String) {
            val currentUserId = LocalStorage.getInstance(itemView.context).getUserId()

            // Enhanced following check
            val cachedFollowingList = FeedAdapter.getCachedFollowingList()
            val isUserFollowing = followingUserIds.contains(feedOwnerId) ||
                    cachedFollowingList.contains(feedOwnerId)

            Log.d(TAG, "SETUP FOLLOW BUTTON")
            Log.d(TAG, "feedOwnerId: $feedOwnerId")
            Log.d(TAG, "currentUserId: $currentUserId")
            Log.d(TAG, "isFollowingUser (from render): $isFollowingUser")
            Log.d(TAG, "isUserFollowing (local check): $isUserFollowing")
            Log.d(TAG, "followingUserIds.contains: ${followingUserIds.contains(feedOwnerId)}")
            Log.d(TAG, "cachedFollowingList.contains: ${cachedFollowingList.contains(feedOwnerId)}")

            // Hide button if: it's current user's post OR we're already following them
            if (feedOwnerId == currentUserId || isFollowingUser || isUserFollowing) {
                followButton.visibility = View.GONE
                Log.d(TAG, "✓ Follow button HIDDEN - Reason: ${when {
                    feedOwnerId == currentUserId -> "Own post"
                    isFollowingUser -> "Already following (from render check)"
                    isUserFollowing -> "Already following (from local check)"
                    else -> "Unknown"
                }}")
                return
            }

            // Show follow button only for users we're NOT following
            followButton.visibility = View.VISIBLE
            followButton.text = "Follow"
            followButton.backgroundTintList = ContextCompat.getColorStateList(
                itemView.context,
                R.color.blueJeans
            )

            Log.d(TAG, "✓ Follow button VISIBLE for user: $feedOwnerId")

            followButton.setOnClickListener {
                handleFollowButtonClick(feedOwnerId)
            }
        }

        // ✅ Update setupRepostedUser to use consistent ID
        private fun setupRepostedUser(data: Post) {
            var feedOwnerId = ""
            var profilePicUrl: String? = null
            var feedOwnerUsername = ""
            var userHandle = ""

            val repostedUser = data.repostedUser
            if (repostedUser != null) {
                // Use reposted user's ACCOUNT ID (this is what's in the following list)
                feedOwnerId = repostedUser._id
                profilePicUrl = repostedUser.avatar?.url

                feedOwnerUsername = when {
                    repostedUser.firstName.isNotBlank() && repostedUser.lastName.isNotBlank() ->
                        "${repostedUser.firstName} ${repostedUser.lastName}"
                    repostedUser.firstName.isNotBlank() -> repostedUser.firstName
                    repostedUser.lastName.isNotBlank() -> repostedUser.lastName
                    else -> repostedUser.username
                }
                userHandle = "@${repostedUser.username}"
                Log.d(tag, "Using reposted user: $feedOwnerUsername (Account ID: $feedOwnerId)")
            } else if (data.originalPost != null && data.originalPost.isNotEmpty()) {
                // ✅ FIX: Use the OWNER ID (account ID) from original post author
                val originalAuthor = data.originalPost[0].author
                feedOwnerId = originalAuthor.owner // This is the account ID
                profilePicUrl = originalAuthor.account.avatar.url

                feedOwnerUsername = when {
                    originalAuthor.firstName.isNotBlank() && originalAuthor.lastName.isNotBlank() ->
                        "${originalAuthor.firstName} ${originalAuthor.lastName}"
                    originalAuthor.firstName.isNotBlank() -> originalAuthor.firstName
                    originalAuthor.lastName.isNotBlank() -> originalAuthor.lastName
                    else -> originalAuthor.account.username
                }
                userHandle = "@${originalAuthor.account.username}"
                Log.d(tag, "Using original post author: $feedOwnerUsername (Owner ID: $feedOwnerId)")
            } else {
                // Fallback to main author
                val author = data.author
                feedOwnerId = author.account._id // Use account ID
                profilePicUrl = author.account.avatar?.url
                feedOwnerUsername = buildDisplayName(author)
                userHandle = "@${author.account.username}"
                Log.d(tag, "Using main author: $feedOwnerUsername (Account ID: $feedOwnerId)")
            }

            // UI binding
            repostedUserName.text = feedOwnerUsername
            tvUserHandle.text = userHandle

            if (!profilePicUrl.isNullOrBlank()) {
                Glide.with(itemView.context)
                    .load(profilePicUrl)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.flash21)
                    .error(R.drawable.flash21)
                    .into(userProfileImage)
            } else {
                userProfileImage.setImageResource(R.drawable.flash21)
            }

            tvPostTag.text = if (repostedUser != null) "Had to Repost This!" else "Shared a Post!"
            userComment.visibility = if (!data.content.isNullOrBlank()) View.VISIBLE else View.GONE
            userComment.text = data.content
            setupRepostHashtags(data)
        }

        // ✅ Update profile click handlers to use consistent IDs
        @SuppressLint("ClickableViewAccessibility")
        private fun setupRepostedUserProfileClicks(data: Post) {
            var feedOwnerId = ""
            var feedOwnerName = ""
            var feedOwnerUsername = ""
            var profilePicUrl = ""

            val repostedUser = data.repostedUser
            if (repostedUser != null) {
                feedOwnerId = repostedUser._id // Account ID
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
                // ✅ Use owner ID for original post author
                val originalAuthor = data.originalPost[0].author
                feedOwnerId = originalAuthor.owner // Account ID
                feedOwnerName = buildDisplayName(originalAuthor)
                feedOwnerUsername = originalAuthor.account.username
                profilePicUrl = originalAuthor.account.avatar.url
            } else {
                // Fallback
                feedOwnerId = data.author.account._id
                feedOwnerName = buildDisplayName(data.author)
                feedOwnerUsername = data.author.account.username
                profilePicUrl = data.author.account.avatar?.url ?: ""
            }

            userProfileImage.setOnClickListener { view ->
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                Log.d(tag, "userProfileImage clicked - ID: $feedOwnerId, Name: $feedOwnerName")
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

        @SuppressLint("NotifyDataSetChanged")
        private fun handleFollowButtonClick(feedOwnerId: String) {
            YoYo.with(Techniques.Pulse).duration(300).playOn(followButton)

            Log.d(TAG, "Follow button clicked for user: $feedOwnerId")

            isFollowed = !isFollowed
            val followEntity = FollowUnFollowEntity(feedOwnerId, isFollowed)

            if (isFollowed) {
                // Hide button immediately
                followButton.visibility = View.GONE

                // Add to adapter's following list AND persistent storage
                (bindingAdapter as? FeedAdapter)?.addToFollowing(feedOwnerId)

                // Also update via manager for consistency
                FollowingManager(itemView.context).addToFollowing(feedOwnerId)

                Log.d(TAG, "Now following user $feedOwnerId")
            } else {
                // Show button
                followButton.text = "Follow"
                followButton.visibility = View.VISIBLE

                // Remove from adapter's following list AND persistent storage
                (bindingAdapter as? FeedAdapter)?.removeFromFollowing(feedOwnerId)

                // Also update via manager for consistency
                FollowingManager(itemView.context).removeFromFollowing(feedOwnerId)

                Log.d(TAG, "Unfollowed user $feedOwnerId")
            }

            // Notify listener
            feedClickListener.followButtonClicked(followEntity, followButton)
            EventBus.getDefault().post(ShortsFollowButtonClicked(followEntity))

            // Refresh adapter to update all instances
            (bindingAdapter as? FeedAdapter)?.notifyDataSetChanged()
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

//        @SuppressLint("ClickableViewAccessibility")
//        private fun setupRepostedUserProfileClicks(data: Post) {
//            val repostedUser = data.repostedUser
//            val author = data.author
//
//            var feedOwnerId = ""
//            var feedOwnerName = ""
//            var feedOwnerUsername = ""
//            var profilePicUrl = ""
//
//            if (repostedUser != null) {
//                // This is an actual repost - use reposted user data
//                feedOwnerId = repostedUser._id
//                feedOwnerName = repostedUser.username // Or build full name if available
//                feedOwnerUsername = repostedUser.username
//                profilePicUrl = repostedUser.avatar?.url ?: ""
//            } else {
//                // Use main author data
//                feedOwnerId = author._id
//                feedOwnerName = buildDisplayName(author)
//                feedOwnerUsername = author.account.username
//                profilePicUrl = author.account.avatar?.url ?: ""
//            }
//
//
//            userProfileImage.setOnClickListener { view ->
//                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
//                Log.d(tag, "userProfileImage clicked - ID: $feedOwnerId, Name: $feedOwnerName")
//                handleProfileClick(feedOwnerId, feedOwnerName, feedOwnerUsername, profilePicUrl)
//                // Consume the click event to prevent it from bubbling to repostContainer
//                view.isClickable = true
//                true // Indicate the event is consumed
//            }
//
//
//            // Prevent userProfileImage clicks from bubbling to repostContainer
//            userProfileImage.setOnTouchListener { _, event ->
//                if (event.action == MotionEvent.ACTION_UP) {
//                    userProfileImage.performClick()
//                    true // Consume the touch event to prevent bubbling
//                } else {
//                    false // Allow other touch events (e.g., long press) to pass through
//                }
//            }
//        }

        @SuppressLint("ClickableViewAccessibility")
        private fun setupOriginalPostAuthorClicks(data: Post) {
            // Only set up if we have an original post
            if (data.originalPost != null && data.originalPost.isNotEmpty()) {
                val originalPostData = data.originalPost[0]
                val author = originalPostData.author

                val feedOwnerId = author._id
                val feedOwnerName = buildDisplayName(author)
                val feedOwnerUsername = author.account.username
                val profilePicUrl = author.account.avatar.url




                // Set click listener specifically for originalPosterProfileImage to only navigate to profile
                originalPosterProfileImage?.setOnClickListener { view ->
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    Log.d(tag, "originalPosterProfileImage clicked - ID: $feedOwnerId, Name: $feedOwnerName")
                    handleProfileClick(feedOwnerId, feedOwnerName, feedOwnerUsername, profilePicUrl)
                    // Consume the click event to prevent bubbling to parent containers
                    true
                }


                // Prevent originalPosterProfileImage clicks from bubbling to parent containers
                originalPosterProfileImage?.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_UP) {
                        originalPosterProfileImage.performClick()
                        true // Consume the touch event to prevent bubbling
                    } else {
                        false // Allow other touch events (e.g., long press) to pass through
                    }
                }
            } else {
                // If no original post, set up clicks for the main author in quoted section
                val author = data.author
                val feedOwnerId = author._id
                val feedOwnerName = buildDisplayName(author)
                val feedOwnerUsername = author.account.username
                val profilePicUrl = author.account.avatar.url

                // Click listener for profile navigation (used for name and handle)
                val profileClickListener = View.OnClickListener {
                    Log.d(tag, "Profile element clicked (main author) - ID: $feedOwnerId, Name: $feedOwnerName")
                    handleProfileClick(feedOwnerId, feedOwnerName, feedOwnerUsername, profilePicUrl)
                }

                // Set click listener specifically for originalPosterProfileImage to only navigate to profile
                originalPosterProfileImage?.setOnClickListener { view ->
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    Log.d(tag, "originalPosterProfileImage clicked (main author) - ID: $feedOwnerId, Name: $feedOwnerName")
                    handleProfileClick(feedOwnerId, feedOwnerName, feedOwnerUsername, profilePicUrl)
                    // Consume the click event to prevent bubbling to parent containers
                    true
                }

                // Set click listeners for other profile elements (name and handle)
                originalPosterName?.setOnClickListener(profileClickListener)
                tvQuotedUserHandle?.setOnClickListener(profileClickListener)

                // Prevent originalPosterProfileImage clicks from bubbling to parent containers
                originalPosterProfileImage?.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_UP) {
                        originalPosterProfileImage.performClick()
                        true // Consume the touch event to prevent bubbling
                    } else {
                        false // Allow other touch events (e.g., long press) to pass through
                    }
                }
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


        private fun setupNavigationClickListenersForAllContainers(data: Post) {
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

                // Get the correct user ID from current post
                val feedReposterOwnerId = currentPost?.repostedUser?._id
                    ?: currentPost?.author?.account?._id
                    ?: ""

                handleFollowButtonClick(feedReposterOwnerId)
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

        private fun finalizeClickSetup(data: Post) {

            setupNavigationClickListenersForAllContainers(data)
            setupReposterTextClickDelegation()
            ensureContainerClickability()



            Log.d(tag, "Container clickable states:")
            Log.d(tag, "- repostContainer: clickable=${repostContainer.isClickable}, focusable=${repostContainer.isFocusable}")
            Log.d(tag, "- originalPostContainer: clickable=${originalPostContainer?.isClickable}, focusable=${originalPostContainer?.isFocusable}")
            Log.d(tag, "- quotedPostCard: clickable=${quotedPostCard.isClickable}, focusable=${quotedPostCard.isFocusable}")
        }

        private fun setupLikeButton(data: Post) {
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

        private fun setupBookmarkButton(data: Post) {

            Log.d(TAG,
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

        private fun setupCommentButton(data: Post) {
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

        private fun setupShareButton(data: Post) {
            totalMixedShareCounts = data.shareCount ?: data.shareCount ?: 0
            updateMetricDisplay(shareCounts, totalMixedShareCounts, "share")

            shareButton.setOnClickListener {
                if (!shareButton.isEnabled) return@setOnClickListener

                Log.d(TAG, "Share clicked for Post: ${data._id}")
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
                                    if (abs(shareResponse.shareCount - 0) > 1) {
                                        data.shareCount = shareResponse.shareCount
                                        totalMixedShareCounts = data.shareCount
                                        updateMetricDisplay(shareCounts, data.shareCount, "share")
                                    }
                                }
                            } else {
                                data.shareCount = previousShareCount
                                totalMixedShareCounts = data.shareCount
                                updateMetricDisplay(shareCounts, data.shareCount, "share")
                            }
                        }

                        override fun onFailure(call: Call<ShareResponse>, t: Throwable) {
                            shareButton.alpha = 1f
                            shareButton.isEnabled = true
                            data.shareCount = previousShareCount
                            totalMixedShareCounts = data.shareCount
                            updateMetricDisplay(shareCounts, data.shareCount, "share")
                        }
                    })

                feedClickListener.feedShareClicked(absoluteAdapterPosition, data)
            }
        }

        private fun setupRepostButton(data: Post) {
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
//                    data.repostCount = totalMixedRePostCounts
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
//                                        data.repostCount = repostResponse.repostCount
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
                    likeButton.setImageResource(R.drawable.filled_favorite_like)
                } else {
                    likeButton.setImageResource(R.drawable.heart_svgrepo_com)
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
                    favoriteButton.setImageResource(R.drawable.filled_favorite)
                } else {
                    favoriteButton.setImageResource(R.drawable.favorite_svgrepo_com__1_)
                    favoriteButton.clearColorFilter()
                }
            } catch (e: Exception) {
                Log.e(tag, "Error updating bookmark button UI", e)
            }
        }

        private fun updateRepostButtonAppearance(isReposted: Boolean) {
            if (isReposted) {
                repostButton.setImageResource(R.drawable.repeat_svgrepo_com)
                repostButton.scaleX = 1.1f
                repostButton.scaleY = 1.1f
            } else {
                repostButton.setImageResource(R.drawable.repeat_svgrepo_com)
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

        private fun navigateToOriginalPostWithRepostInside(originalPostData: Post) {
            try {
                val fragment = Fragment_Original_Post_With_Repost_Inside.newInstance(originalPostData)
                navigateToFragment(fragment, "repost_with_context")
            } catch (e: Exception) {
                Log.e(tag, "Error navigating to repost fragment: ${e.message}")
                e.printStackTrace()
            }
        }

        private fun navigateToOriginalPostWithoutRepostInside(originalPostData: Post) {

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
                            files.first() is File -> {
                                (files as List<File>).map { it.url }
                            }
                            files.first() is File -> {
                                (files as List<File>).map { it.url }
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

        private fun navigateToEditPostToRepost(data: Post) {
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
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                    .replace(R.id.frame_layout, fragment)
                    .addToBackStack(backStackName)
                    .commit()
                Log.d(tag, "Successfully navigated to $backStackName")
            } else {
                Log.e(tag, "Could not find AppCompatActivity from context")
            }
        }

        private fun setupFileTapNavigation(data: Post) {
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
                    val adapter = FeedRepostViewFileAdapter(images, originalPostData)
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
                                    .placeholder(R.drawable.imageplaceholder)
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
                            val adapter = FeedRepostViewFileAdapter(images, originalPostData)
                            setupCleanRecyclerView(originalPostData.files.size, adapter)
                        }
                    }
                    "video" -> {
                        if (originalPostData.files.isNotEmpty()) {
                            mixedFilesCardView?.visibility = View.VISIBLE
                            originalFeedImage?.visibility = View.GONE
                            multipleAudiosContainer?.visibility = View.VISIBLE
                            val images = originalPostData.files.map { it.url }
                            val adapter = FeedRepostViewFileAdapter(images, originalPostData)
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

        private fun setupMoreOptionsButton(data: Post) {
            moreOptionsButton.setOnClickListener {
                feedClickListener.moreOptionsClick(absoluteAdapterPosition, data)
            }
        }

//        private fun setupRepostedUser(data: Post) {
//            var feedOwnerId = ""
//            var profilePicUrl: String? = null
//            var feedOwnerUsername = ""
//            var userHandle = ""
//
//            val repostedUser = data.repostedUser
//            if (repostedUser != null) {
//                // ---- REPOSTED USER (the one who actually reposted) ----
//                feedOwnerId      = repostedUser._id
//                profilePicUrl    = repostedUser.avatar?.url
//                // ---- NEW: use firstName / lastName if they exist ----
//                feedOwnerUsername = when {
//                    repostedUser.firstName.isNotBlank() && repostedUser.lastName.isNotBlank() ->
//                        "${repostedUser.firstName} ${repostedUser.lastName}"
//                    repostedUser.firstName.isNotBlank() -> repostedUser.firstName
//                    repostedUser.lastName.isNotBlank()  -> repostedUser.lastName
//                    else -> repostedUser.username               // fallback
//                }
//                userHandle = "@${repostedUser.username}"
//                Log.d(tag, "Using reposted user: $feedOwnerUsername")
//            } else {
//                // ---- FALLBACK to the main author (same as before) ----
//                Log.w(tag, "RepostedUser is null for post ${data._id}, using main author")
//                val author = data.author
//                feedOwnerId      = author._id
//                profilePicUrl    = author.account.avatar?.url
//                feedOwnerUsername = buildDisplayName(author)          // existing helper
//                userHandle       = "@${author.account.username}"
//                Log.d(tag, "Using main author: $feedOwnerUsername $userHandle")
//            }
//
//            // ---- UI binding (unchanged) ----
//            repostedUserName.text = feedOwnerUsername
//            tvUserHandle.text     = userHandle
//
//            if (!profilePicUrl.isNullOrBlank()) {
//                Glide.with(itemView.context)
//                    .load(profilePicUrl)
//                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
//                    .diskCacheStrategy(DiskCacheStrategy.ALL)
//                    .placeholder(R.drawable.flash21)
//                    .error(R.drawable.flash21)
//                    .into(userProfileImage)
//            } else {
//                userProfileImage.setImageResource(R.drawable.flash21)
//            }
//
//            tvPostTag.text = if (repostedUser != null) "Had to Repost This!" else "Shared a Post!"
//            userComment.visibility = if (!data.content.isNullOrBlank()) View.VISIBLE else View.GONE
//            userComment.text = data.content
//            setupRepostHashtags(data)
//        }

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
                            .placeholder(R.drawable.flash21)
                            .error(R.drawable.flash21)
                            .into(imageView)
                        Log.d(tag, "Loading avatar: $avatarUrl")
                    } else {
                        imageView.setImageResource(R.drawable.flash21)
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
                originalPosterProfileImage?.setImageResource(R.drawable.flash21)
                originalPosterName?.text = "Unknown User"
                tvQuotedUserHandle?.text = "@unknown_user"
            }
        }

        private fun setupQuotedUserFromMainAuthor(data: Post) {
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
                            .placeholder(R.drawable.flash21)
                            .error(R.drawable.flash21)
                            .into(imageView)
                    } else {
                        imageView.setImageResource(R.drawable.flash21)
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
                originalPosterProfileImage?.setImageResource(R.drawable.flash21)
                originalPosterName?.text = "Unknown User"
                tvQuotedUserHandle?.text = "@unknown"
                originalPostText?.visibility = View.GONE
                tvQuotedHashtags?.text = "#SoftwareAppreciation #GameChanger #MustHave"
                tvQuotedHashtags?.visibility = View.VISIBLE
            }
        }

        private fun setupRepostHashtags(data: Post) {
            val hashtagText = "#Repost #GreatContent #MustSee"
            tvHashtags.text = hashtagText
            tvHashtags.visibility = View.VISIBLE
        }

        private fun setupOriginalPostContent(data: Post) {
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
                "now"
            }
        }


    }

    inner class FeedNewPostWithRepostInsideFilesPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

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
        private val quotedPostCard: LinearLayout = itemView.findViewById(R.id.quotedPostCard)

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
        private val mixedFilesCardViews: LinearLayout = itemView.findViewById(R.id.mixedFilesCardViews)
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
        private val mixedFilesCardView: LinearLayout = itemView.findViewById(R.id.mixedFilesCardView)
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
        private val favoritesCount: TextView = itemView.findViewById(R.id.favoriteCounts)

        private val repostSection: LinearLayout = itemView.findViewById(R.id.repostPost)
        private val repostPost: ImageView = itemView.findViewById(R.id.repostPost)
        private val repostCountTextView: TextView = itemView.findViewById(R.id.repostCount)

        private val shareSection: LinearLayout = itemView.findViewById(R.id.shareLayout)
        private val shareImageView: ImageView = itemView.findViewById(R.id.shareButtonIcon)
        private val shareCountTextView: TextView = itemView.findViewById(R.id.shareCount)

        // State variables
        private var isFollowed = false
        private var totalMixedComments = 0
        private var serverCommentCount = 0
        private var loadedCommentCount = 0
        private var currentPost: Post? = null
        private var totalMixedLikesCounts = 0
        private var totalMixedBookMarkCounts = 0
        private var totalMixedShareCounts = 0
        private var totalMixedRePostCounts = 0
        private var postClicked = false // Debounce flag for post navigation

        @OptIn(UnstableApi::class)
        @SuppressLint("SetTextI18n", "SuspiciousIndentation")
        fun render(data: Post) {
            Log.d(TAG, "render: feed data $data")

            // Store current post reference
            currentPost = data

            val feedOwnerId = data.repostedUser?._id ?: data.author?.account?._id ?: "Unknown"

            totalMixedComments = data.comments
            totalMixedLikesCounts = data.likes
            totalMixedBookMarkCounts = data.bookmarkCount
            totalMixedShareCounts = data.shareCount
            totalMixedRePostCounts = data.repostCount

            setupUserInfo(data, feedOwnerId)
            setupPostInfo(data)
            setupNewPostMediaFiles(data) // New post media (top)
            setupContentAndTags(data)
            setupOriginalPostContent(data) // Quoted/original post (bottom)
            setupEngagementButtons(data)
            setupProfileClickListeners(data, feedOwnerId)
            setupFollowButton(feedOwnerId)
            setupPostClickListeners(data)
            ensurePostClickability(data)
            setupInteractionButtonsClickPrevention()
        }

        private fun setupPostClickListeners(data: Post) {
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

        private fun preventChildClickInterference(data: Post) {
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
            val interactionButtons = listOf(
                likeSection,
                commentSection,
                favoriteSection,
                repostSection,
                shareSection,
                followButton,
                moreOptionsButton
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
                            handleRetweetClick()
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

        private fun consumeClickEvent(view: View) {
            view.isPressed = false
            view.parent?.requestDisallowInterceptTouchEvent(true)
        }

        private fun ensurePostClickability(data: Post) {
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

        private fun setupContentAndTags(data: Post) {
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
                tvPostTag.text = "💬 Quote Repost with New Content! 💬"
            } else if (data.content.isNotEmpty() && data.originalPost?.isNotEmpty() == true) {
                // Quote repost with comment only
                tvPostTag.text = "💭 Quote Repost! 💭"
            } else if (data.originalPost?.isNotEmpty() == true) {
                // Simple repost
                tvPostTag.text = "🔄 Reposted! 🔄"
            } else {
                // Regular post
                tvPostTag.text = "📱 New Post! 📱"
            }
            tvPostTag.visibility = View.VISIBLE
        }

        private fun setupNewPostMediaFiles(data: Post) {
            val fileList: MutableList<String> = mutableListOf()

            // Check if this is a repost with new files added by the reposter
            if (data.files.isNotEmpty() && data.isReposted == true) {
                Log.d(Fragment_Edit_Post_To_Repost.Companion.TAG, "Setting up new post media files for reposter")
                data.files.forEach { file ->
                    Log.d(Fragment_Edit_Post_To_Repost.Companion.TAG, "New Post File URL: ${file.url}")
                    fileList.add(file.url)
                }
            } else if (data.files.isNotEmpty() && data.originalPost?.isNotEmpty() == true) {
                // This is a quote repost with new content
                Log.d(Fragment_Edit_Post_To_Repost.Companion.TAG, "Setting up new content for quote repost")
                data.files.forEach { file ->
                    Log.d(Fragment_Edit_Post_To_Repost.Companion.TAG, "Quote Repost New File URL: ${file.url}")
                    fileList.add(file.url)
                }
            } else if (data.files.isEmpty() && data.originalPost?.isNotEmpty() == true) {
                // Pure repost without new files
                Log.d(Fragment_Edit_Post_To_Repost.Companion.TAG, "Pure repost without new files")
                newPostMediaCard.visibility = View.GONE
                return
            } else {
                Log.d(Fragment_Edit_Post_To_Repost.Companion.TAG, "No new files to display")
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
                        .placeholder(R.drawable.imageplaceholder)
                        .error(R.drawable.imageplaceholder)
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

        private fun setupOriginalPostContent(data: Post) {
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
                            .placeholder(R.drawable.flash21)
                            .error(R.drawable.flash21)
                            .into(imageView)
                        Log.d(TAG, "Loading avatar: $avatarUrl")
                    } else {
                        imageView.setImageResource(R.drawable.flash21)
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
                originalPosterProfileImage?.setImageResource(R.drawable.flash21)
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
                    val adapter = FeedRepostViewFileAdapter(images, originalPostData)
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
                                    .placeholder(R.drawable.imageplaceholder)
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
                            val adapter = FeedRepostViewFileAdapter(images, originalPostData)
                            setupCleanOriginalRecyclerView(originalPostData.files.size, adapter)
                        }
                    }
                    "video" -> {
                        if (originalPostData.files.isNotEmpty()) {
                            mixedFilesCardView.visibility = View.VISIBLE
                            originalFeedImage.visibility = View.GONE
                            multipleAudiosContainer.visibility = View.VISIBLE
                            val images = originalPostData.files.map { it.url }
                            val adapter = FeedRepostViewFileAdapter(images, originalPostData)
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

        private fun setupPostInfo(data: Post) {
            // Date and time
            dateTimeCreate.text = formattedMongoDateTime(data.createdAt)

            // Comment count
            initializeCommentCounts(data)
            updateCommentCountDisplay()

            // Initialize all counts
            updateEngagementCounts(data)
        }

        private fun initializeCommentCounts(data: Post) {
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

        private fun setupUserInfo(data: Post, feedOwnerId: String) {
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

        private fun setupEngagementButtons(data: Post) {
            setupLikeButton(data)
            setupCommentButton(data)
            setupShareButton(data)
            setupRepostButton(data)
            setupBookmarkButton(data)
            setupMoreOptionsButton(data)
        }

        private fun setupLikeButton(data: Post) {
            Log.d(TAG, "Setting up like button - Initial state: isLiked=${data.isLiked}, likes=${data.likes}")
            updateLikeButtonUI(data.isLiked ?: false)
            updateMetricDisplay(likesCount, data.likes, "like")

            likeSection.setOnClickListener {
                if (!likeButton.isEnabled) return@setOnClickListener

                Log.d(TAG, "Like clicked for post: ${data._id}")
                Log.d(TAG, "Current state before toggle: isLiked=${data.isLiked}, likes=${data.likes}")

                val newLikeStatus = !(data.isLiked ?: false)
                val previousLikeStatus = data.isLiked ?: false
                val previousLikesCount = data.likes

                // Update data immediately
                data.isLiked = newLikeStatus
                data.likes = if (newLikeStatus) data.likes + 1 else maxOf(0, data.likes - 1)
                totalMixedLikesCounts = // Continuing from setupLikeButton method where it was cut off
                    data.likes

                Log.d(TAG, "New state after toggle: isLiked=${data.isLiked}, likes=${data.likes}")

                // Update UI immediately
                updateLikeButtonUI(newLikeStatus)
                updateMetricDisplay(likesCount, data.likes, "like")

                // Disable button temporarily to prevent rapid clicking
                likeButton.isEnabled = false

                // Make API call
                makeApiCall(
                    endpoint = if (newLikeStatus) "like" else "unlike",
                    postId = data._id,
                    onSuccess = {
                        Log.d(TAG, "Like API call successful")
                        likeButton.isEnabled = true
                    },
                    onError = { error ->
                        Log.e(TAG, "Like API call failed: $error")
                        // Revert changes on error
                        data.isLiked = previousLikeStatus
                        data.likes = previousLikesCount
                        totalMixedLikesCounts = previousLikesCount
                        updateLikeButtonUI(previousLikeStatus)
                        updateMetricDisplay(likesCount, previousLikesCount, "like")
                        likeButton.isEnabled = true
                    }
                )
            }
        }

        private fun setupCommentButton(data: Post) {
            commentSection.setOnClickListener {
                Log.d(TAG, "Comment clicked for post: ${data._id}")

            }
        }

        private fun setupShareButton(data: Post) {
            updateMetricDisplay(shareCountTextView, data.shareCount, "share")

            shareSection.setOnClickListener {
                Log.d(TAG, "Share clicked for post: ${data._id}")
                handleShareClick()
            }
        }

        private fun setupRepostButton(data: Post) {
            updateMetricDisplay(repostCountTextView, data.repostCount, "repost")

            repostSection.setOnClickListener {
                Log.d(TAG, "Repost clicked for post: ${data._id}")
                handleRetweetClick()
            }
        }

        private fun setupBookmarkButton(data: Post) {
            Log.d(TAG, "Setting up bookmark button - Initial state: isBookmarked=${data.isBookmarked}, bookmarks=${data.bookmarkCount}")
            updateBookmarkButtonUI(data.isBookmarked ?: false)
            updateMetricDisplay(favoritesCount, data.bookmarkCount, "bookmark")

            favoriteSection.setOnClickListener {
                if (!favoriteButton.isEnabled) return@setOnClickListener

                Log.d(TAG, "Bookmark clicked for post: ${data._id}")
                val newBookmarkStatus = !(data.isBookmarked ?: false)
                val previousBookmarkStatus = data.isBookmarked ?: false
                val previousBookmarkCount = data.bookmarkCount

                // Update data immediately
                data.isBookmarked = newBookmarkStatus
                data.bookmarkCount = if (newBookmarkStatus) data.bookmarkCount + 1 else maxOf(0, data.bookmarkCount - 1)
                totalMixedBookMarkCounts = data.bookmarkCount

                // Update UI immediately
                updateBookmarkButtonUI(newBookmarkStatus)
                updateMetricDisplay(favoritesCount, data.bookmarkCount, "bookmark")

                // Disable button temporarily
                favoriteButton.isEnabled = false

                // Make API call
                makeApiCall(
                    endpoint = if (newBookmarkStatus) "bookmark" else "unbookmark",
                    postId = data._id,
                    onSuccess = {
                        Log.d(TAG, "Bookmark API call successful")
                        favoriteButton.isEnabled = true
                    },
                    onError = { error ->
                        Log.e(TAG, "Bookmark API call failed: $error")
                        // Revert changes on error
                        data.isBookmarked = previousBookmarkStatus
                        data.bookmarkCount = previousBookmarkCount
                        totalMixedBookMarkCounts = previousBookmarkCount
                        updateBookmarkButtonUI(previousBookmarkStatus)
                        updateMetricDisplay(favoritesCount, previousBookmarkCount, "bookmark")
                        favoriteButton.isEnabled = true
                    }
                )
            }
        }

        private fun setupMoreOptionsButton(data: Post) {
            moreOptionsButton.setOnClickListener {
                Log.d(TAG, "More options clicked for post: ${data._id}")
                showMoreOptionsDialog(data)
            }
        }

        private fun updateLikeButtonUI(isLiked: Boolean) {
            if (isLiked) {
                likeButton.setImageResource(R.drawable.heart_svgrepo_com)

            } else {
                likeButton.setImageResource(R.drawable.heart_svgrepo_com)
                likeButton.imageTintList = ContextCompat.getColorStateList(itemView.context, R.color.blueJeans)
            }
        }

        private fun updateBookmarkButtonUI(isBookmarked: Boolean) {
            if (isBookmarked) {
                favoriteButton.setImageResource(R.drawable.favorite_svgrepo_com__1_)

            } else {
                favoriteButton.setImageResource(R.drawable.favorite_svgrepo_com__1_)
                favoriteButton.imageTintList = ContextCompat.getColorStateList(itemView.context, R.color.blueJeans)
            }
        }

        private fun updateEngagementCounts(data: Post) {
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

        private fun setupProfileClickListeners(data: Post, feedOwnerId: String) {
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

        private fun setupFollowButton(feedOwnerId: String) {
            // Check follow status and update button
            checkFollowStatus(feedOwnerId) { isFollowed ->
                this.isFollowed = isFollowed
                updateFollowButtonUI(isFollowed)
            }

            followButton.setOnClickListener {
                if (!followButton.isEnabled) return@setOnClickListener

                val newFollowStatus = !isFollowed
                followButton.isEnabled = false

                makeApiCall(
                    endpoint = if (newFollowStatus) "follow" else "unfollow",
                    postId = feedOwnerId,
                    onSuccess = {
                        isFollowed = newFollowStatus
                        updateFollowButtonUI(newFollowStatus)
                        followButton.isEnabled = true
                        Log.d(TAG, "Follow status changed to: $newFollowStatus")
                    },
                    onError = { error ->
                        Log.e(TAG, "Follow API call failed: $error")
                        followButton.isEnabled = true
                    }
                )
            }
        }

        private fun updateFollowButtonUI(isFollowed: Boolean) {
            if (isFollowed) {
                followButton.text = "Following"

                followButton.setTextColor(ContextCompat.getColor(itemView.context, R.color.blueJeans))
            } else {
                followButton.text = "Follow"
                followButton.background = ContextCompat.getDrawable(itemView.context, R.drawable.fill_button_color)
                followButton.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
            }
        }

        private fun handleLikeClick() {
            currentPost?.let { post ->
                Log.d(TAG, "Handling like click for post: ${post._id}")
                // Like logic is handled in setupLikeButton
            }
        }

        private fun handleCommentClick() {
            currentPost?.let { post ->
                Log.d(TAG, "Handling comment click for post: ${post._id}")

            }
        }

        private fun handleFavoriteClick() {
            currentPost?.let { post ->
                Log.d(TAG, "Handling favorite click for post: ${post._id}")
                // Bookmark logic is handled in setupBookmarkButton
            }
        }

        private fun handleRetweetClick() {
            currentPost?.let { post ->
                Log.d(TAG, "Handling retweet click for post: ${post._id}")
                showRepostDialog(post)
            }
        }

        private fun handleShareClick() {
            currentPost?.let { post ->
                Log.d(TAG, "Handling share click for post: ${post._id}")
                sharePost(post)
            }
        }

        private fun handleFollowClick() {
            Log.d(TAG, "Handling follow click")
            // Follow logic is handled in setupFollowButton
        }

        private fun handleMoreOptionsClick() {
            currentPost?.let { post ->
                Log.d(TAG, "Handling more options click for post: ${post._id}")
                showMoreOptionsDialog(post)
            }
        }

        private fun navigateToOriginalPostWithRepostInside(originalPostData: Post) {
            try {
                val fragment = Fragment_Original_Post_With_Repost_Inside.newInstance(originalPostData)
                navigateToFragment(fragment, "repost_with_context")
            } catch (e: Exception) {
                Log.e(TAG, "Error navigating to repost fragment: ${e.message}")
                e.printStackTrace()
            }
        }

        private fun navigateToOriginalPostWithoutRepostInside(originalPostData: Post) {
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
                            files.first() is File -> {
                                (files as List<File>).map { it.url }
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



        private fun showMoreOptionsDialog(data: Post) {
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

        private fun showRepostDialog(data: Post) {
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

        private fun sharePost(data: Post) {
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

        private fun loadImageWithGlide(url: String?, imageView: ImageView, context: Context) {
            Glide.with(context)
                .load(url)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.flash21)
                .error(R.drawable.flash21)
                .into(imageView)
        }

        private fun getActivityFromContext(context: Context?): AppCompatActivity? {
            return when (context) {
                is AppCompatActivity -> context
                is ContextWrapper -> getActivityFromContext(context.baseContext)
                else -> null
            }
        }


        // API and data operations (implement according to your API structure)
        private fun makeApiCall(endpoint: String, postId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
            // Implement your API call logic here
            // This is a placeholder - replace with your actual API implementation
            try {
                // Simulate API call
                Handler(Looper.getMainLooper()).postDelayed({
                    onSuccess()
                }, 500)
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error")
            }
        }

        private fun checkFollowStatus(userId: String, callback: (Boolean) -> Unit) {

            callback(false)
        }

        private fun reportPost(data: Post) {
            // Implement report functionality
            Log.d(TAG, "Reporting post: ${data._id}")
        }

        private fun hidePost(data: Post) {
            // Implement hide post functionality
            Log.d(TAG, "Hiding post: ${data._id}")
        }

        @SuppressLint("ServiceCast")
        private fun copyPostLink(data: Post) {
            val clipboard = itemView.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Post Link", "https://app.com/post/${data._id}")
            clipboard.setPrimaryClip(clip)
            Toast.makeText(itemView.context, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        private fun savePost(data: Post) {
            // Implement save post functionality
            Log.d(TAG, "Saving post: ${data._id}")
        }

        private fun repost(data: Post) {
            // Implement repost functionality
            Log.d(TAG, "Reposting: ${data._id}")
        }

        private fun quoteRepost(data: Post) {
            // Implement quote repost functionality
            Log.d(TAG, "Quote reposting: ${data._id}")
        }
    }

    inner class TrendingVideosPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val trendingVideosFeedAdapter = TrendingVideosFeedAdapter(mutableListOf())
        private val ivThumbnail: ImageView = itemView.findViewById(R.id.ivThumbnail)
        private val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)

        init {
            itemView.findViewById<RecyclerView>(R.id.rvTrendingVideos).apply {
                adapter = trendingVideosFeedAdapter
                layoutManager = LinearLayoutManager(
                    context, LinearLayoutManager.HORIZONTAL, false
                )
            }
        }

        fun render(videos: List<ShortsEntity>) {

            trendingVideosFeedAdapter.updateData(videos)
        }

        fun bind(video: List<ShortsEntity>) {
            // Get the first thumbnail URL (if exists)
            val thumbnailUrl = video.firstOrNull()
            // Load thumbnail using Glide
            Glide.with(itemView.context)
                .load(thumbnailUrl)
                .placeholder(R.drawable.music_placeholder) // Add a placeholder image
                .into(ivThumbnail)
            tvDuration.text = video[0].content // Display content as title
            itemView.setOnClickListener {
                // Handle item click
            }
        }
    }

}



interface OnClickListeners {
    fun onSeekBarChanged(progress: Int)
    fun onDownloadClick(url: String, fileLocation: String)

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







