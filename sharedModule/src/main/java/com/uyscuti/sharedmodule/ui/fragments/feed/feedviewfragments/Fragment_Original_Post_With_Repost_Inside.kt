package com.uyscuti.sharedmodule.ui.fragments.feed.feedviewfragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uyscuti.sharedmodule.R
import java.util.*
import androidx.activity.OnBackPressedCallback
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlin.collections.isNotEmpty
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.uyscuti.sharedmodule.ReportNotificationActivity2
import com.uyscuti.sharedmodule.adapter.feed.TAG
import com.uyscuti.sharedmodule.databinding.FragmentOriginalPostWithRepostInsideBinding
import com.uyscuti.sharedmodule.model.ShortsFollowButtonClicked
import com.uyscuti.sharedmodule.ui.fragments.feed.feedviewfragments.Fragment_Original_Post_Without_Repost_Inside.CommentCountUpdatedEvent
import com.uyscuti.sharedmodule.ui.fragments.feed.feedviewfragments.Fragment_Original_Post_Without_Repost_Inside.CommentsLoadedEvent
import com.uyscuti.sharedmodule.ui.fragments.feed.feedviewfragments.Fragment_Original_Post_Without_Repost_Inside.OnFeedClickListener
import com.uyscuti.social.network.api.response.feed.getallfeed.more_feed_data_classes.AudioDuration
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.getValue
import com.uyscuti.sharedmodule.adapter.feed.FeedAdapter
import com.uyscuti.sharedmodule.model.FeedCommentClicked
import com.uyscuti.sharedmodule.model.ShowAppBar
import com.uyscuti.sharedmodule.model.ShowBottomNav
import com.uyscuti.sharedmodule.ui.fragments.feed.feedviewfragments.editRepost.Fragment_Edit_Post_To_Repost
import com.uyscuti.sharedmodule.ui.fragments.feed.feedviewfragments.feedRepost.PostItem
import com.uyscuti.sharedmodule.ui.fragments.feed.feedviewfragments.feedRepost.Tapped_Files_In_The_Container_View_Fragment
import com.uyscuti.sharedmodule.utils.FollowingManager
import com.uyscuti.sharedmodule.viewmodels.feed.GetFeedViewModel
import com.uyscuti.sharedmodule.viewmodels.feed.UserRelationshipsViewModel
import com.uyscuti.social.network.api.response.posts.OriginalPost
import com.uyscuti.social.network.api.response.posts.Post
import com.uyscuti.social.network.api.response.posts.Duration
import com.uyscuti.social.network.api.response.posts.ThumbnailX
import com.uyscuti.social.network.api.response.posts.File
import com.uyscuti.social.network.api.response.posts.FileType
import java.text.SimpleDateFormat
import java.util.*
import com.uyscuti.social.network.utils.LocalStorage
import retrofit2.Call
import retrofit2.Callback
import kotlin.math.abs
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
import com.uyscuti.social.network.api.response.post.Thumbnail
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.Response
import javax.inject.Inject
import kotlin.getValue


private const val TAG = "Fragment_Original_Post_With_Repost_Inside"
private const val FRAGMENT_ORIGINAL_POST_WITH_REPOST = 1

@AndroidEntryPoint
class Fragment_Original_Post_With_Repost_Inside() : Fragment() {

    companion object {
        private const val ARG_ORIGINAL_POST = "original_post"

        fun newInstance(data: Post): Fragment_Original_Post_With_Repost_Inside {
            return Fragment_Original_Post_With_Repost_Inside().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_ORIGINAL_POST, data)
                }
            }
        }

    }

    private lateinit var feedPost: Post


    @Inject
    lateinit var retrofitInstance: RetrofitInstance

    // Views from header_toolbar.xml
    private lateinit var cancelButton: ImageButton
    private lateinit var headerTitle: TextView
    private lateinit var headerMenuButton: ImageButton

    // Views from user_info_section.xml
    private lateinit var userProfileImage: ImageView
    private lateinit var repostedUserName: TextView
    private lateinit var tvUserHandle: TextView
    private lateinit var dateTimeCreate: TextView
    private lateinit var followButton: AppCompatButton

    // Views from post_content_section.xml
    private lateinit var repostContainer: LinearLayout
    private lateinit var tvPostTag: TextView
    private lateinit var userComment: TextView
    private lateinit var tvHashtags: TextView

    // Views from media_section.xml
    private lateinit var mixedFilesCardViews: CardView
    private lateinit var originalFeedImages: ImageView
    private lateinit var multipleMediaContainer: LinearLayout
    private lateinit var multipleAudiosContainers: LinearLayout
    private lateinit var recyclerViews: RecyclerView

    // Views from original_post_section.xml
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


    // Views from action_buttons_section.xml
    private lateinit var likeSection: LinearLayout
    private lateinit var like: ImageView
    private lateinit var likesCount: TextView
    private lateinit var commentSection: LinearLayout
    private lateinit var comment: ImageView
    private lateinit var commentCount: TextView
    private lateinit var favoriteSection: LinearLayout
    private lateinit var fav: ImageView
    private lateinit var favCount: TextView
    private lateinit var retweetSection: LinearLayout
    private lateinit var reFeed: ImageView
    private lateinit var repostCount: TextView
    private lateinit var shareSection: LinearLayout
    private lateinit var share: ImageView
    private lateinit var shareCount: TextView


    private var currentPost: Post? = null
    private var currentPosition: Int = 0


    // Counters

    private var totalMixedComments = 0
    private var totalMixedLikesCounts = 0
    private var totalMixedBookMarkCounts = 0
    private var totalMixedShareCounts = 0
    private var totalMixedRePostCounts = 0

    // Data
    private var originalPost: OriginalPost? = null
    private var post: Post? = null
    private var isFollowing = false
    private var _binding: FragmentOriginalPostWithRepostInsideBinding? = null
    private val binding get() = _binding!!
    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private var containerLayout: LinearLayout? = null

    private val followingUserIds = mutableSetOf<String>()
    private val relationshipsViewModel: UserRelationshipsViewModel by activityViewModels()
    private lateinit var allFeedAdapter: FeedAdapter
    private var blockedUserIds = mutableSetOf<String>()
    private val getFeedViewModel: GetFeedViewModel by activityViewModels()
    private lateinit var feedListView: RecyclerView

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




    private fun populatePostData(post: Post) {
        currentPost = post

        // Set header
        headerTitle.text = "Post"

        // Populate reposter information
        populateReposterInfo(post)

        // Populate repost content
        populateRepostContent(post)

        // FIX: Handle files from the ORIGINAL post if it exists
        if (post.originalPost.isNotEmpty()) {
            val originalPost = post.originalPost[0]

            // FIX: Initialize counts from the ORIGINAL post (not the repost wrapper)
            totalMixedComments = originalPost.commentCount
            totalMixedLikesCounts = originalPost.likeCount
            totalMixedBookMarkCounts = originalPost.bookmarkCount
            totalMixedRePostCounts = originalPost.repostCount
            totalMixedShareCounts = originalPost.shareCount

            // Update UI with actual counts immediately
            forceRefreshAllMetrics()

            populateOriginalPostData(originalPost)

            // FIX: Fetch fresh comment count for the ORIGINAL post
            fetchAndUpdateCommentCount(originalPost._id)
        } else {
            // If no original post, use the repost's own data
            totalMixedComments = post.comments
            totalMixedLikesCounts = post.likes
            totalMixedBookMarkCounts = post.bookmarkCount
            totalMixedRePostCounts = post.repostCount ?: 0
            totalMixedShareCounts = post.shareCount

            forceRefreshAllMetrics()
        }

        setupInitialFollowButtonState(post)

        // Handle repost media files
        handleRepostMediaFiles(post)
    }

    // 2. ADD: Force refresh all metrics
    private fun forceRefreshAllMetrics() {
        Log.d(TAG, "forceRefreshAllMetrics: Forcing refresh of all metric displays")

        Handler(Looper.getMainLooper()).post {
            updateMetricDisplay(commentCount, totalMixedComments, "comment")
            updateMetricDisplay(likesCount, totalMixedLikesCounts, "like")
            updateMetricDisplay(favCount, totalMixedBookMarkCounts, "bookmark")
            updateMetricDisplay(repostCount, totalMixedRePostCounts, "repost")
            updateMetricDisplay(shareCount, totalMixedShareCounts, "share")

            Log.d(TAG, "forceRefreshAllMetrics: All metrics refreshed")
        }
    }

    // 3. FIX: Update handleFeedCommentClicked to get correct post ID
    private fun handleFeedCommentClicked(position: Int, post: Post?) {
        Log.d(TAG, "handleFeedCommentClicked: Posting comment event for post ${post?._id}")
        try {
            post?.let {
                EventBus.getDefault().post(FeedCommentClicked(position, post))
            }

            // FIX: Get the ORIGINAL post ID (already a String)
            val postIdToFetch = if (currentPost?.originalPost?.isNotEmpty() == true) {
                currentPost?.originalPost?.firstOrNull()?._id
            } else {
                currentPost?._id
            }

            Log.d(TAG, "handleFeedCommentClicked: Will fetch count for post ID: $postIdToFetch")

            // Delay the fetch slightly to allow UI to settle
            Handler(Looper.getMainLooper()).postDelayed({
                postIdToFetch?.let { id ->
                    fetchAndUpdateCommentCount(id)  // id is already a String
                }
            }, 500)

        } catch (e: Exception) {
            Log.e(TAG, "Error posting comment event: ${e.message}")
            e.printStackTrace()
        }
    }

    // 4. FIX: Update event handlers to use correct post ID
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCommentsLoaded(event: CommentsLoadedEvent) {
        Log.d(TAG, "onCommentsLoaded: Received comments loaded event with ${event.commentCount} comments for post ${event.postId}")

        // FIX: Get the correct post ID (already a String)
        val currentPostId = currentPost?.originalPost?.firstOrNull()?._id ?: currentPost?._id

        if (currentPostId == event.postId) {
            Log.d(TAG, "onCommentsLoaded: Updating UI for matching post")
            updateCommentCount(event.commentCount)
        } else {
            Log.d(TAG, "onCommentsLoaded: Event for different post (expected: $currentPostId, got: ${event.postId}), ignoring")
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCommentCountUpdated(event: CommentCountUpdatedEvent) {
        Log.d(TAG, "onCommentCountUpdated: Received count ${event.commentCount} for post ${event.postId}")

        // FIX: Get the correct post ID (already a String)
        val currentPostId = currentPost?.originalPost?.firstOrNull()?._id ?: currentPost?._id

        if (currentPostId == event.postId) {
            Log.d(TAG, "onCommentCountUpdated: Updating UI for matching post")
            updateCommentCount(event.commentCount)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCommentAdded(event: CommentAddedEvent) {
        Log.d(TAG, "onCommentAdded: Received event for post ${event.postId}")

        // FIX: Compare with original post ID if it exists
        val currentPostId = currentPost?.originalPost?.firstOrNull()?._id ?: currentPost?._id

        if (currentPostId == event.postId) {
            incrementCommentCount()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCommentDeleted(event: CommentDeletedEvent) {
        Log.d(TAG, "onCommentDeleted: Received event for post ${event.postId}")

        // FIX: Compare with original post ID if it exists
        val currentPostId = currentPost?.originalPost?.firstOrNull()?._id ?: currentPost?._id

        if (currentPostId == event.postId) {
            decrementCommentCount()
        }
    }

    // 5. FIX: Update fetchAndUpdateCommentCount
    private fun fetchAndUpdateCommentCount(postId: String) {
        Log.d(TAG, "fetchAndUpdateCommentCount: Fetching current comment count for post: $postId")

        RetrofitClient.commentService.getCommentCount(postId)
            .enqueue(object : Callback<CommentCountResponse> {
                override fun onResponse(call: Call<CommentCountResponse>, response: Response<CommentCountResponse>) {
                    if (response.isSuccessful && isAdded) {
                        response.body()?.let { countResponse ->
                            val actualCount = countResponse.count
                            Log.d(TAG, "fetchAndUpdateCommentCount: API returned count: $actualCount for post: $postId")

                            // FIX: Check if this is still the current post (String comparison)
                            val currentPostId = currentPost?.originalPost?.firstOrNull()?._id ?: currentPost?._id

                            if (currentPostId == postId) {
                                // Only update if the count has changed
                                if (totalMixedComments != actualCount) {
                                    Log.d(TAG, "fetchAndUpdateCommentCount: Count changed from $totalMixedComments to $actualCount")

                                    totalMixedComments = actualCount
                                    currentPost?.comments = actualCount

                                    // Update the original post's comment count if it exists
                                    currentPost?.originalPost?.firstOrNull()?.let { original ->
                                        original.commentCount = actualCount
                                    }

                                    updateMetricDisplay(commentCount, actualCount, "comment")

                                    // Add animation
                                    YoYo.with(Techniques.Pulse)
                                        .duration(300)
                                        .playOn(commentCount)
                                } else {
                                    Log.d(TAG, "fetchAndUpdateCommentCount: Count unchanged at $actualCount")
                                }
                            } else {
                                Log.d(TAG, "fetchAndUpdateCommentCount: Post ID mismatch. Expected: $currentPostId, Got: $postId")
                            }
                        }
                    } else {
                        Log.e(TAG, "fetchAndUpdateCommentCount: Failed with code: ${response.code()}")
                        if (response.code() == 404) {
                            updateMetricDisplay(commentCount, 0, "comment")
                        } else {
                            loadCommentsAndUpdateCount(postId)
                        }
                    }
                }

                override fun onFailure(call: Call<CommentCountResponse>, t: Throwable) {
                    Log.e(TAG, "fetchAndUpdateCommentCount: Network error", t)
                    Log.d(TAG, "fetchAndUpdateCommentCount: Keeping existing count: $totalMixedComments")
                }
            })
    }

    // 6. FIX: Update handleRepostMediaFiles to properly show files
    private fun handleRepostMediaFiles(post: Post) {
        if (post.files.isNotEmpty()) {
            // These are NEW files added by the reposter
            val firstFile = post.files[0]
            Log.d(TAG, "handleRepostMediaFiles: Processing ${post.files.size} files from reposter")

            when {
                firstFile.mimeType?.startsWith("image") == true -> {
                    showRepostImageMedia(post, firstFile)
                }
                firstFile.mimeType?.startsWith("video") == true -> {
                    showRepostVideoMedia(post, firstFile)
                }
                firstFile.mimeType?.startsWith("audio") == true -> {
                    showRepostAudioMedia(post, firstFile)
                }
                isDocumentFile(firstFile) -> {
                    showRepostDocumentMedia(post, firstFile)
                }
                else -> {
                    showRepostCombinationOfMultiplesMedia(post, firstFile)
                }
            }
        } else {
            // No new files from reposter, hide these views
            hideAllRepostMediaViews()
        }

        // Handle thumbnails
        handleThumbnails(post.thumbnail, ivQuotedPostImage)
    }

    // 7. FIX: Update handleOriginalPostMediaFiles to use correct file reference
    private fun handleOriginalPostMediaFiles(originalPost: OriginalPost) {
        Log.d(TAG, "handleOriginalPostMediaFiles: Processing ${originalPost.files.size} files from original post")

        if (originalPost.files.isNotEmpty()) {
            val firstFile = originalPost.files[0]

            Log.d(TAG, "handleOriginalPostMediaFiles: First file - mimeType: ${firstFile.mimeType}, url: ${firstFile.url}")

            when {
                isImageFile(firstFile) -> {
                    Log.d(TAG, "handleOriginalPostMediaFiles: Showing image media")
                    showOriginalImageMedia(originalPost, firstFile)
                }
                isVideoFile(firstFile) -> {
                    Log.d(TAG, "handleOriginalPostMediaFiles: Showing video media")
                    showOriginalVideoMedia(originalPost, firstFile)
                }
                isAudioFile(firstFile) -> {
                    Log.d(TAG, "handleOriginalPostMediaFiles: Showing audio media")
                    showOriginalAudioMedia(originalPost, firstFile)
                }
                isDocumentFile(firstFile) -> {
                    Log.d(TAG, "handleOriginalPostMediaFiles: Showing document media")
                    showOriginalDocumentMedia(originalPost, firstFile)
                }
                else -> {
                    Log.d(TAG, "handleOriginalPostMediaFiles: Unknown file type, hiding all media views")
                    hideAllOriginalMediaViews()
                }
            }
        } else {
            Log.d(TAG, "handleOriginalPostMediaFiles: No files found, hiding all media views")
            hideAllOriginalMediaViews()
        }

        handleThumbnails(originalPost.thumbnail, ivQuotedPostImage)
    }
    

    // 7. FIX: Update updateMetricDisplay to ensure TextView is properly updated
    private fun updateMetricDisplay(textView: TextView, count: Int, metricType: String) {
        Log.d(TAG, "updateMetricDisplay: Updating $metricType display to $count")

        // FIX: Ensure we're on the main thread
        if (Thread.currentThread() != Looper.getMainLooper().thread) {
            Handler(Looper.getMainLooper()).post {
                updateMetricDisplayOnMainThread(textView, count, metricType)
            }
        } else {
            updateMetricDisplayOnMainThread(textView, count, metricType)
        }
    }

    private fun updateMetricDisplayOnMainThread(textView: TextView, count: Int, metricType: String) {
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
        Log.d(TAG, "updateMetricDisplay: Set $metricType text to '${textView.text}' - TextView ID: ${textView.id}")
    }

    private val feedClickListener: OnFeedClickListener by lazy {
        (activity as? OnFeedClickListener) ?:
        object : OnFeedClickListener {

            override fun likeUnLikeFeed(position: Int, post: Post) {
                Log.d(TAG, "feedClickListener: likeUnLikeFeed position $position for post ${post._id}")
            }

            override fun feedCommentClicked(
                position: Int,
                data: Post
            ) {
                Log.d(TAG, "feedClickListener: feedCommentClicked position $position for post ${post?._id}")
                handleFeedCommentClicked(position, data)
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

    private fun showRepostDocumentMedia(post: Post, firstFile: File) {
        mixedFilesCardViews.visibility = View.VISIBLE
        multipleAudiosContainers.visibility = View.GONE
        recyclerViews.visibility = View.GONE

        val thumbnailUrl = post.thumbnail.firstOrNull()?.thumbnailUrl
        if (!thumbnailUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(thumbnailUrl)
                .placeholder(getDocumentPlaceholder(firstFile))
                .error(getDocumentPlaceholder(firstFile))
                .into(originalFeedImages)
        } else {
            originalFeedImages.setImageResource(getDocumentPlaceholder(firstFile))
        }
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

    private fun setupCommentButton(data: Post) {
        comment.setOnClickListener {
            if (!comment.isEnabled) return@setOnClickListener

            Log.d(TAG, "setupCommentButton: Comment button clicked for post ${data._id}")

            // Animate the comment button
            YoYo.with(Techniques.Tada)
                .duration(700)
                .repeat(1)
                .playOn(comment)

            // Post event to MainActivity via EventBus
            handleFeedCommentClicked(0, data)

            comment.isEnabled = true
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

    private fun setupShareButton(data: Post) {
        updateMetricDisplay(shareCount, data.shareCount, "share")
        share.setOnClickListener {
            if (!share.isEnabled) return@setOnClickListener

            Log.d(TAG, "Share clicked for post: ${data._id}")
            val previousShareCount = data.shareCount

            // Update immediately for better UX
            data.shareCount += 1
            totalMixedShareCounts = data.shareCount
            updateMetricDisplay(shareCount, data.shareCount, "share")

            YoYo.with(Techniques.Tada)
                .duration(700)
                .repeat(1)
                .playOn(share)

            share.isEnabled = false
            share.alpha = 0.8f

            // Make API call to sync with server
            RetrofitClient.shareService.incrementShare(data._id)
                .enqueue(object : Callback<ShareResponse> {
                    override fun onResponse(call: Call<ShareResponse>, response: Response<ShareResponse>) {
                        share.alpha = 1f
                        share.isEnabled = true

                        if (response.isSuccessful) {
                            response.body()?.let { shareResponse ->
                                if (abs(shareResponse.shareCount - data.shareCount) > 1) {
                                    data.shareCount = shareResponse.shareCount
                                    totalMixedShareCounts = data.shareCount
                                    updateMetricDisplay(shareCount, data.shareCount, "share")
                                }
                            }
                        } else {
                            Log.e(TAG, "Share sync failed: ${response.code()}")
                            if (response.code() != 200) {
                                data.shareCount = previousShareCount
                                totalMixedShareCounts = data.shareCount
                                updateMetricDisplay(shareCount, data.shareCount, "share")
                            }
                        }
                    }

                    override fun onFailure(call: Call<ShareResponse>, t: Throwable) {
                        share.alpha = 1f
                        share.isEnabled = true
                        Log.e(TAG, "Share network error", t)
                    }
                })

            // Show the share dialog
            feedShareClicked(0, data)
        }
    }

    private fun setupRepostButton(data: Post) {
        totalMixedRePostCounts = data.safeRepostCount
        updateMetricDisplay(repostCount, totalMixedRePostCounts, "repost")
        updateRepostButtonAppearance(data.isReposted)

        reFeed.setOnClickListener { view ->
            if (!reFeed.isEnabled) return@setOnClickListener
            reFeed.isEnabled = false

            try {
                val wasReposted = data.isReposted
                data.isReposted = !wasReposted
                totalMixedRePostCounts = if (data.isReposted) totalMixedRePostCounts + 1 else maxOf(0, totalMixedRePostCounts - 1)
                data.repostCount = totalMixedRePostCounts
                updateMetricDisplay(repostCount, totalMixedRePostCounts, "repost")
                updateRepostButtonAppearance(data.isReposted)

                YoYo.with(if (data.isReposted) Techniques.Tada else Techniques.Pulse)
                    .duration(700)
                    .playOn(reFeed)

                reFeed.alpha = 0.8f

                val apiCall = if (data.isReposted) {
                    RetrofitClient.repostService.incrementRepost(data._id)
                } else {
                    RetrofitClient.repostService.decrementRepost(data._id)
                }

                apiCall.enqueue(object : Callback<RepostResponse> {
                    override fun onResponse(call: Call<RepostResponse>, response: Response<RepostResponse>) {
                        reFeed.isEnabled = true
                        reFeed.alpha = 1f

                        if (response.isSuccessful) {
                            response.body()?.let { repostResponse ->
                                if (abs(repostResponse.repostCount - totalMixedRePostCounts) > 1) {
                                    data.repostCount = repostResponse.repostCount
                                    totalMixedRePostCounts = repostResponse.repostCount
                                    updateMetricDisplay(repostCount, totalMixedRePostCounts, "repost")
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<RepostResponse>, t: Throwable) {
                        reFeed.isEnabled = true
                        reFeed.alpha = 1f
                        Log.e(TAG, "Repost network error", t)
                    }
                })

                if (data.isReposted) {
                    navigateToEditPostToRepost(data)
                }

                feedClickListener.feedRepostPost(0, data)
            } catch (e: Exception) {
                reFeed.isEnabled = true
                reFeed.alpha = 1f
                Log.e(TAG, "Exception in repost click listener", e)
            }
        }
    }

    private fun updateRepostButtonAppearance(isReposted: Boolean) {
        if (isReposted) {
            reFeed.setImageResource(R.drawable.repeat_svgrepo_com)
            reFeed.scaleX = 1.1f
            reFeed.scaleY = 1.1f
        } else {
            reFeed.setImageResource(R.drawable.repeat_svgrepo_com)
            reFeed.scaleX = 1.0f
            reFeed.scaleY = 1.0f
        }
    }

    private fun navigateToEditPostToRepost(data: Post) {
        try {
            val fragment = Fragment_Edit_Post_To_Repost(data)
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, fragment)
                .addToBackStack("edit_post_to_repost")
                .commit()
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to edit post fragment", e)
        }
    }

    private fun feedShareClicked(position: Int, data: Post) {
        // Your existing share dialog implementation
        showToast("Share functionality")
    }

    // 6. Create these event classes if they don't exist
    data class CommentAddedEvent(val postId: String)
    data class CommentDeletedEvent(val postId: String)

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
        fileIds: List<String>,
        post: Post
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

                    val author = post.author
                    val account = author?.account

                    val postItem = PostItem(
                        postId = post._id,
                        userId = author?._id,
                        username = author.account.username,
                        authorName = listOfNotNull(
                            author?.firstName?.takeIf { it.isNotBlank() },
                            author?.lastName?.takeIf { it.isNotBlank() }
                        ).joinToString(" ").ifBlank { account?.username },
                        avatarUrl = account?.avatar?.url,
                        audioUrl = file.url.takeIf { it.endsWith(".mp3", true) || it.endsWith(".aac", true) },
                        audioThumbnailUrl = null,
                        videoUrl = file.url.takeIf { it.endsWith(".mp4", true) || it.endsWith(".mkv", true) },
                        videoThumbnailUrl = null,
                        data = post.content.orEmpty(),
                        files = arrayListOf(file.url),
                        fileType = file.url.substringAfterLast('.', "")
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
                .replace(android.R.id.content, fragment)
                .addToBackStack("tapped_files_view")
                .commit()

            Log.d(
                TAG,
                "Navigated to Tapped_Files_In_The_Container_View with ${files.size} " +
                        "files, starting at index $currentIndex")
        } else {
            Log.e(TAG,
                "Activity is null, cannot navigate to fragment")
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.fragment_original_post_with_repost_inside,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup back pressed callback
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                cleanupAndGoBack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)

        initializeViews(view)
        setupRecyclerViews()

        // Get post data
        post = arguments?.getSerializable(ARG_ORIGINAL_POST) as? Post

        post?.let { postData ->
            Log.d(TAG, "Post ID: ${postData._id}")

            currentPost = postData

            // Get the actual comment count
            totalMixedComments = if (postData.originalPost.isNotEmpty()) {
                val originalPost = postData.originalPost[0]
                originalPost.commentCount
            } else {
                postData.comments
            }

            // CRITICAL: Force immediate UI updates
            Handler(Looper.getMainLooper()).post {
                try {
                    commentCount.text = totalMixedComments.toString()
                    commentCount.visibility = View.VISIBLE
                    commentCount.requestLayout()

                    likesCount.text = postData.safeLikes.toString()
                    likesCount.visibility = View.VISIBLE
                    likesCount.requestLayout()

                    favCount.text = postData.safeBookmarkCount.toString()
                    favCount.visibility = View.VISIBLE
                    favCount.requestLayout()

                    shareCount.text = postData.safeShareCount.toString()
                    shareCount.visibility = View.VISIBLE
                    shareCount.requestLayout()

                    repostCount.text = postData.safeRepostCount.toString()
                    repostCount.visibility = View.VISIBLE
                    repostCount.requestLayout()
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating counts", e)
                }
            }

            // Populate post data
            populatePostData(postData)

            // Setup buttons
            setupLikeButton(postData)
            setupBookmarkButton(postData)
            setupCommentButton(postData)
            setupShareButton(postData)
            setupRepostButton(postData)
            setupClickListeners(postData)

            // Force refresh after setup
            Handler(Looper.getMainLooper()).postDelayed({
                forceRefreshAllMetrics()
            }, 300)
        }
    }

    private fun initializeViews(view: View) {

        // Header Views
        cancelButton = view.findViewById(R.id.cancelButton)
        headerTitle = view.findViewById(R.id.headerTitle)
        headerMenuButton = view.findViewById(R.id.headerMenuButton)

        // User Info Views
        userProfileImage = view.findViewById(R.id.userProfileImage)
        repostedUserName = view.findViewById(R.id.repostedUserName)
        tvUserHandle = view.findViewById(R.id.tvUserHandle)
        dateTimeCreate = view.findViewById(R.id.date_time_create)
        followButton = view.findViewById(R.id.followButton)

        // Post Content Views
        repostContainer = view.findViewById(R.id.repostContainer)
        tvPostTag = view.findViewById(R.id.tvPostTag)
        userComment = view.findViewById(R.id.userComment)
        tvHashtags = view.findViewById(R.id.tvHashtags)

        // Media Views - FIXED
        mixedFilesCardViews = view.findViewById(R.id.mixedFilesCardViews)
        originalFeedImages = view.findViewById(R.id.originalFeedImages)
        multipleAudiosContainers = view.findViewById(R.id.multipleAudiosContainers)
        recyclerViews = view.findViewById(R.id.recyclerViews)

        // Original Post Views
        quotedPostCard = view.findViewById(R.id.quotedPostCard)
        originalPostContainer = view.findViewById(R.id.originalPostContainer)
        originalPosterProfileImage = view.findViewById(R.id.originalPosterProfileImage)
        originalPosterName = view.findViewById(R.id.originalPosterName)
        tvQuotedUserHandle = view.findViewById(R.id.tvQuotedUserHandle)
        originalPostText = view.findViewById(R.id.originalPostText)
        tvQuotedHashtags = view.findViewById(R.id.tvQuotedHashtags)
        dateTime = view.findViewById(R.id.date_time)
        mixedFilesCardView = view.findViewById(R.id.mixedFilesCardView)
        originalFeedImage = view.findViewById(R.id.originalFeedImage)
        videoContainer = view.findViewById(R.id.videoContainer)
        multipleAudiosContainer = view.findViewById(R.id.multipleAudiosContainer)
        recyclerView = view.findViewById(R.id.recyclerView)
        ivQuotedPostImage = view.findViewById(R.id.ivQuotedPostImage)

        // Action Button Views
        likeSection = view.findViewById(R.id.like_layout)
        like = view.findViewById(R.id.like)
        likesCount = view.findViewById(R.id.likesCount)
        commentSection = view.findViewById(R.id.comment_layout)
        comment = view.findViewById(R.id.comment)
        commentCount = view.findViewById(R.id.commentCount)
        favoriteSection = view.findViewById(R.id.favoriteSection)
        fav = view.findViewById(R.id.fav)
        favCount = view.findViewById(R.id.favCount)
        retweetSection = view.findViewById(R.id.repost_layout)
        reFeed = view.findViewById(R.id.reFeed)
        repostCount = view.findViewById(R.id.repostCount)
        shareSection = view.findViewById(R.id.share_layout)
        share = view.findViewById(R.id.share)
        shareCount = view.findViewById(R.id.shareCount)
    }

    private fun setupClickListeners(data: Post) {

        // Header click listeners
        cancelButton.setOnClickListener {
            Log.d(TAG, "Cancel button clicked")
            cleanupAndGoBack()
        }

        headerMenuButton.setOnClickListener {
            moreOptionsClick(currentPosition, data)
        }

        // User interaction click listeners
        followButton.setOnClickListener {
            handleFollowButtonClick()
        }

        // Post click listeners
        repostContainer.setOnClickListener { handleMainPostClick() }
        quotedPostCard.setOnClickListener { handleOriginalPostClick() }

        // Action button click listeners
        likeSection.setOnClickListener {
            currentPost?.let { post ->
                setupLikeButton(post)
            }
        }

        favoriteSection.setOnClickListener {
            currentPost?.let { post ->
                setupBookmarkButton(post)
            }
        }

        commentSection.setOnClickListener { handleCommentClick() }

        retweetSection.setOnClickListener { handleRetweetClick() }
        shareSection.setOnClickListener { handleShareClick() }

        // Media click listeners - ADD THESE NEW ONES
        mixedFilesCardViews.setOnClickListener { handleRepostMediaClick() }
        mixedFilesCardView.setOnClickListener { handleOriginalMediaClick() }

        // ADD THESE NEW CLICK LISTENERS FOR DOCUMENT FILES
        originalFeedImages.setOnClickListener { handleRepostFileClick() }
        originalFeedImage.setOnClickListener { handleOriginalFileClick() }
    }

    @OptIn(UnstableApi::class)
    private fun cleanupAndGoBack() {
        // IMMEDIATE: Go back first - this is the priority
        try {
            if (isAdded && !parentFragmentManager.isStateSaved) {
                parentFragmentManager.popBackStackImmediate()
            }
        } catch (e: Exception) {
            Log.e(Fragment_Edit_Post_To_Repost.Companion.TAG, "Error popping back stack", e)
            // If immediate fails, try regular popBackStack
            parentFragmentManager.popBackStack()
        }

        // Everything else happens AFTER we're already going back
        view?.post {
            // Clear focus


            // Hide keyboard
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view?.windowToken, 0)

            // Restore system bars
            activity?.let { act ->
                WindowCompat.setDecorFitsSystemWindows(act.window, true)
                WindowInsetsControllerCompat(act.window, act.window.decorView)
                    .show(WindowInsetsCompat.Type.systemBars())

                EventBus.getDefault().post(ShowAppBar(true))
                EventBus.getDefault().post(ShowBottomNav(true))
            }
        }
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

                        mimeType = file.mimeType,

                    ).apply {

                    }
                }
                val fileIds = currentPost.files.map { it ?: "unknown_id" }
                navigateToTappedFilesFragment(requireContext(),
                    0, files, fileIds as List<String>, currentPost )
            }
        }
    }

    @SuppressLint("InflateParams", "MissingInflatedId", "ServiceCast")
    fun moreOptionsClick(
        position: Int,
        data: Post
    ) {
        Log.d(TAG, "moreOptionsClick: More options clicked")
        val view: View = layoutInflater.inflate(R.layout.feed_more_options_layout, null)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(view)

        // Get all views from XML
        val downloadFiles: View = view.findViewById(R.id.downloadAction)
        val followUnfollowLayout: View = view.findViewById(R.id.followAction)
        val reportUser: View = view.findViewById(R.id.reportOptionLayout)
        val hidePostLayout: View = view.findViewById(R.id.hidePostLayout)
        val copyLink: View = view.findViewById(R.id.copyLinkLayout)
        val muteOptionLayout: MaterialCardView = view.findViewById(R.id.muteOptionLayout)
        val blockUserLayout: MaterialCardView = view.findViewById(R.id.blockUserLayout)
        val quoteFeedLayout: View = view.findViewById(R.id.repostAction)
        val shareAction: View = view.findViewById(R.id.shareAction)
        val notInterested: View = view.findViewById(R.id.notInterestedLayout)

        // Get the author ID and username
        val authorId = data.author?.account?._id
        val username = data.author?.account?.username ?: "User"

        // Update mute button text based on current state
        if (authorId != null) {
            // Find the nested LinearLayout inside muteOptionLayout
            val muteCard = muteOptionLayout.getChildAt(0) as? LinearLayout
            val muteTextContainer = muteCard?.getChildAt(1) as? LinearLayout
            val muteStaticText = muteTextContainer?.getChildAt(0) as? TextView
            val muteUsernameText = muteTextContainer?.getChildAt(1) as? TextView

            if (relationshipsViewModel.isPostsMuted(authorId)) {
                muteStaticText?.text = "Unmute "
                muteUsernameText?.text = username
            } else {
                muteStaticText?.text = "Mute "
                muteUsernameText?.text = username
            }
        }

        // Update block button username text
        val blockCard = blockUserLayout.getChildAt(0) as? LinearLayout
        val blockContentLayout = blockCard?.getChildAt(1) as? LinearLayout
        val blockDescriptionLayout = blockContentLayout?.getChildAt(1) as? LinearLayout
        val usernameBlockText = blockDescriptionLayout?.findViewById<TextView>(R.id.usernameBlock)
        usernameBlockText?.text = username

        // Show the dialog
        dialog.show()

        // ==================== SHARE ACTION ====================
        shareAction.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, data.content)
            startActivity(Intent.createChooser(shareIntent, "Share via"))
            dialog.dismiss()
        }

        // ==================== DOWNLOAD ACTION ====================
        downloadFiles.setOnClickListener {
            Log.d("DownloadButton", "Data: $data")
            if (data.files.isNotEmpty()) {
                onDownloadClick(data.files[0].url, "FlashShorts")
            } else {
                Toast.makeText(context, "No files to download", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        // Hide download if text-only post
        if (data.contentType == "text") {
            downloadFiles.visibility = View.GONE
        }

        // ==================== MUTE ACTION ====================
        muteOptionLayout.setOnClickListener {
            Log.d("MuteButton", "Mute button clicked for user: $authorId")
            dialog.dismiss()

            authorId?.let { userId ->
                handleMuteToggle(userId, position)
            } ?: run {
                Toast.makeText(context, "Cannot mute: User ID not found", Toast.LENGTH_SHORT).show()
            }
        }

        // ==================== BLOCK USER ACTION ====================
        blockUserLayout.setOnClickListener {
            Log.d("BlockButton", "Block button clicked for user: $authorId")
            dialog.dismiss()

            authorId?.let { userId ->
                // Check if user is trying to block themselves

                // Check if user is already blocked
                if (blockedUserIds.contains(userId)) {
                    // Unblock the user
                    handleUnblockUser(userId, username)
                } else {
                    // Block the user
                    showBlockConfirmationDialog(userId, username, position)
                }
            } ?: run {
                Toast.makeText(context, "Cannot block: User ID not found", Toast.LENGTH_SHORT).show()
            }
        }

        // ==================== REPOST ACTION ====================
        quoteFeedLayout.setOnClickListener {
            val fragment = Fragment_Edit_Post_To_Repost(data)
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
            dialog.dismiss()
        }

        // ==================== COPY LINK ACTION ====================
        copyLink.setOnClickListener {
            val postId = data._id
            val linkToCopy = "https://circuitSocial.app/post/$postId"
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied Link", linkToCopy)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(requireContext(), "Link copied to clipboard", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        // ==================== NOT INTERESTED ACTION ====================
        notInterested.setOnClickListener {
            handleNotInterested(data)
            dialog.dismiss()
        }

        // ==================== HIDE POST ACTION ====================
        hidePostLayout.setOnClickListener {
            Log.d(TAG, "hidePostLayout: hide post clicked")
            hideSinglePost(position, data)
            dialog.dismiss()
        }

        // ==================== REPORT USER ACTION ====================
        reportUser.setOnClickListener {
            Log.d("reportUser", "Report button clicked")
            val intent = Intent(requireActivity(), ReportNotificationActivity2::class.java)
            startActivityForResult(intent, FRAGMENT_ORIGINAL_POST_WITH_REPOST)
            dialog.dismiss()
        }

        // Hide follow button (you can show it if needed based on relationship status)
        followUnfollowLayout.visibility = View.GONE
    }

    // ==================== MUTE TOGGLE ====================
    private fun handleMuteToggle(userId: String, position: Int) {
        lifecycleScope.launch {
            try {
                if (relationshipsViewModel.isPostsMuted(userId)) {
                    // Un mute
                    val response = retrofitInstance.apiService.unMutePosts(userId)
                    if (response.isSuccessful) {
                        relationshipsViewModel.removeMutedPosts(userId)
                        Toast.makeText(context, "Posts unmuted", Toast.LENGTH_SHORT).show()
                        // Refresh feed to show posts again
                        allFeedAdapter.notifyDataSetChanged()
                    }
                } else {
                    // Mute
                    val response = retrofitInstance.apiService.mutePosts(userId)
                    if (response.isSuccessful) {
                        relationshipsViewModel.addMutedPosts(userId)

                        // Remove the post from the adapter
                        allFeedAdapter.removeItem(position)
                        allFeedAdapter.notifyItemRemoved(position)

                        // Show Snackbar with Undo
                        Snackbar.make(feedListView, "Posts from this user muted", Snackbar.LENGTH_LONG)
                            .setAction("Undo") {
                                // Unmute the user
                                lifecycleScope.launch {
                                    val undoResponse = retrofitInstance.apiService.unMutePosts(userId)
                                    if (undoResponse.isSuccessful) {
                                        relationshipsViewModel.removeMutedPosts(userId)
                                        Toast.makeText(context, "Unmuted", Toast.LENGTH_SHORT).show()
                                        // Reload feed

                                    }
                                }
                            }
                            .show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling mute: ${e.message}", e)
                Toast.makeText(context, "Failed to update mute status", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ==================== BLOCK USER CONFIRMATION ====================
    private fun showBlockConfirmationDialog(userId: String, username: String, position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Block $username?")
            .setMessage("You won't be able to see or contact $username. They won't be notified that you blocked them.")
            .setPositiveButton("Block") { dialog, _ ->
                handleBlockUser(userId, position)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    // ==================== BLOCK USER ====================
    private fun handleBlockUser(userId: String, position: Int) {
        lifecycleScope.launch {
            try {
                val response = retrofitInstance.apiService.blockUser(userId)
                if (response.isSuccessful) {
                    // Add to blocked list
                    blockedUserIds.add(userId)

                    // Remove all posts from this user
                    val itemsToRemove = mutableListOf<Int>()
                    for (i in 0 until allFeedAdapter.itemCount) {
                        val item = getFeedViewModel.getAllFeedData().getOrNull(i)
                        val itemAuthorId = item?.author?.account?._id
                        if (itemAuthorId == userId) {
                            itemsToRemove.add(i)
                        }
                    }

                    // Remove items in reverse order to maintain indices
                    itemsToRemove.reversed().forEach { pos ->
                        allFeedAdapter.removeItem(pos)
                        getFeedViewModel.removeAllFeedFragment(pos)
                    }

                    allFeedAdapter.notifyDataSetChanged()
                    Toast.makeText(context, "User blocked", Toast.LENGTH_SHORT).show()

                    // Show Snackbar with Undo
                    Snackbar.make(feedListView, "User blocked", Snackbar.LENGTH_LONG)
                        .setAction("Undo") {
                            lifecycleScope.launch {
                                val unblockResponse = retrofitInstance.apiService.unBlockUser(userId)
                                if (unblockResponse.isSuccessful) {
                                    blockedUserIds.remove(userId)
                                    Toast.makeText(context, "User unblocked", Toast.LENGTH_SHORT).show()
                                    // Reload feed

                                }
                            }
                        }
                        .show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error blocking user: ${e.message}", e)
                Toast.makeText(context, "Failed to block user", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // ==================== UNBLOCK USER ====================
    private fun handleUnblockUser(userId: String, username: String) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    retrofitInstance.apiService.unBlockUser(userId)
                }

                if (response.isSuccessful) {
                    // Remove from blocked list
                    blockedUserIds.remove(userId)

                    Toast.makeText(
                        context,
                        "Unblocked @$username",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Show Snackbar with option to undo
                    Snackbar.make(
                        feedListView,
                        "User unblocked",
                        Snackbar.LENGTH_LONG
                    ).setAction("Undo") {
                        // Re-block the user
                        lifecycleScope.launch {
                            try {
                                val blockResponse = retrofitInstance.apiService.blockUser(userId)
                                if (blockResponse.isSuccessful) {
                                    blockedUserIds.add(userId)
                                    Toast.makeText(
                                        context,
                                        "User blocked again",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error re-blocking user: ${e.message}", e)
                                Toast.makeText(
                                    context,
                                    "Failed to block user",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }.show()


                } else {
                    Toast.makeText(
                        context,
                        "Failed to unblock user",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error unblocking user: ${e.message}", e)
                Toast.makeText(
                    context,
                    "Network error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    private fun handleNotInterested(
        data: Post) {

        val sharedPrefs =
            requireContext().getSharedPreferences("NotInterestedPosts", Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putBoolean(data._id.toString(), true)
            apply()
        }



        // Show confirmation
        Toast.makeText(
            requireContext(),
            "We'll show you less content like this",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun onDownloadClick(url: String, fileLocation: String) {
        Log.d(
            "Download",
            "OnDownload $url  \nto path : $fileLocation"
        )

        val permissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

        } else {
            // You have permission, proceed with your file operations

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                // Check if the permission is not granted
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // Request the permission

                } else {


                }


            } else {

            }
        }


    }

    @SuppressLint("NotifyDataSetChanged")
    private fun hideSinglePost(
        position: Int,
        data: Post
    ) {
        Log.d(
            TAG,
            "hideSinglePost: Hiding post at position: $position, PostId: ${data._id}")
        try {
            if (::allFeedAdapter.isInitialized) {


                allFeedAdapter.removeItem(position)
                allFeedAdapter.notifyItemRemoved(position)

                // Optional: Add fade-out animation
                val viewHolder = feedListView.findViewHolderForAdapterPosition(position)
                if (viewHolder != null) {
                    viewHolder.itemView.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction {
                            allFeedAdapter.notifyItemRemoved(position)
                        }
                        .start()
                } else {
                    Log.w(
                        TAG,
                        "ViewHolder at position $position is null, notifying removal directly"
                    )
                    allFeedAdapter.notifyItemRemoved(position) // Fallback for off-screen items
                }
                // Show Snackbar with Undo button
                Snackbar.make(feedListView, "Post hidden", Snackbar.LENGTH_LONG)
                    .setAction("Undo") {
                        // Restore the post

                        allFeedAdapter.notifyItemInserted(position)
                    }
                    .show()
                return
            }

            val sharedPrefs =
                requireContext().getSharedPreferences(
                    "HiddenPosts", Context.MODE_PRIVATE)
            with(sharedPrefs.edit()) {
                putBoolean(data._id, true)
                apply()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error hiding post: ${e.message}")
            Toast.makeText(requireContext(),
                "Failed to hide post", Toast.LENGTH_SHORT).show()
        }
    }



    private fun handleMainPostClick() = showToast("Opening full post ...")
    private fun handleOriginalPostClick() = showToast("Opening original post...")
    private fun handleCommentClick() = showToast("Opening comments...")
    private fun handleRetweetClick() = showRetweetOptions()
    private fun handleShareClick() = sharePost()

    private fun handleFollowButtonClick() {
        post?.let { currentPost ->
            // Extract ACCOUNT ID and USERNAME
            val feedOwnerId: String
            val feedOwnerUsername: String

            when {
                // Case 1: Reposted post - use reposter's account ID
                currentPost.originalPost.isNotEmpty() -> {
                    val reposter = currentPost.author
                    feedOwnerId = reposter?.account?._id ?: ""
                    feedOwnerUsername = reposter?.account?.username ?: "unknown"
                    Log.d(TAG, "Follow button - Reposter ID: $feedOwnerId (@$feedOwnerUsername)")
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

                // Update FeedAdapter cache
                FeedAdapter.addToFollowingCache(feedOwnerId)
                FeedAdapter.setCachedFollowingList(followingUserIds)

                // Save to local storage
                FollowingManager(requireContext()).addToFollowing(feedOwnerId)

                // Build display name for toast
                val displayName = when {
                    currentPost.originalPost.isNotEmpty() -> {
                        val author = currentPost.author
                        when {
                            author?.firstName?.isNotBlank() == true && author.lastName?.isNotBlank() == true ->
                                "${author.firstName} ${author.lastName}"
                            author?.firstName?.isNotBlank() == true -> author.firstName
                            author?.lastName?.isNotBlank() == true -> author.lastName
                            else -> author?.account?.username ?: "User"
                        }
                    }
                    else -> {
                        val author = currentPost.author
                        when {
                            author?.firstName?.isNotBlank() == true && author.lastName?.isNotBlank() == true ->
                                "${author.firstName} ${author.lastName}"
                            author?.firstName?.isNotBlank() == true -> author.firstName
                            author?.lastName?.isNotBlank() == true -> author.lastName
                            else -> author?.account?.username ?: "User"
                        }
                    }
                }

                showToast("Now following $displayName")
                Log.d(TAG, "✓ Added account $feedOwnerId (@$feedOwnerUsername) to following list")

                // Post EventBus event to sync across app
                val followEntity = FollowUnFollowEntity(
                    userId = feedOwnerId,
                    isFollowing = true,
                    isButtonVisible = false
                )
                EventBus.getDefault().post(ShortsFollowButtonClicked(followEntity))

            } else {
                // Check if they follow you to show correct button text
                val theyFollowMe = FeedAdapter.isUserInMyFollowersList(feedOwnerId)

                // Show button with appropriate text
                followButton.text = if (theyFollowMe) "Follow Back" else "Follow"
                followButton.visibility = View.VISIBLE

                // Update FeedAdapter cache
                FeedAdapter.removeFromFollowingCache(feedOwnerId)
                FeedAdapter.setCachedFollowingList(followingUserIds)

                // Remove from local storage
                FollowingManager(requireContext()).removeFromFollowing(feedOwnerId)

                showToast("Unfollowed")
                Log.d(TAG, "✓ Removed account $feedOwnerId (@$feedOwnerUsername) from following list")

                // Post EventBus event to sync across app
                val followEntity = FollowUnFollowEntity(
                    userId = feedOwnerId,
                    isFollowing = false,
                    isButtonVisible = true
                )
                EventBus.getDefault().post(ShortsFollowButtonClicked(followEntity))
            }

            updateFollowButtonUI()
        }
    }

    private fun setupInitialFollowButtonState(data: Post) {
        val feedOwnerId: String
        val feedOwnerUsername: String

        when {
            data.originalPost.isNotEmpty() -> {
                val reposter = data.author
                feedOwnerId = reposter?.account?._id ?: ""
                feedOwnerUsername = reposter?.account?.username ?: "unknown"
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

            // Check if this user follows us back
            val theyFollowMe = FeedAdapter.isUserInMyFollowersList(feedOwnerId)
            followButton.text = if (theyFollowMe) "Follow Back" else "Follow"

            Log.d(TAG, "Initial setup: Follow button shown for $feedOwnerId (@$feedOwnerUsername) - Text: '${followButton.text}'")
        }
    }

    private fun updateFollowButtonUI() {
        if (isFollowing) {
            followButton.visibility = View.GONE
        } else {
            followButton.visibility = View.VISIBLE

            post?.let { currentPost ->
                val feedOwnerId = when {
                    currentPost.originalPost.isNotEmpty() ->
                        currentPost.author?.account?._id ?: ""
                    else ->
                        currentPost.author?.account?._id ?: ""
                }

                // Just read from cache - FollowingFragment already loaded this
                val theyFollowMe = FeedAdapter.isUserInMyFollowersList(feedOwnerId)
                followButton.text = if (theyFollowMe) "Follow Back" else "Follow"

                Log.d(TAG, "Updated button UI - Text: '${followButton.text}' for user $feedOwnerId")
            }

            followButton.backgroundTintList = ContextCompat.getColorStateList(
                requireContext(),
                R.color.blueJeans
            )
        }
    }

    // Update existing media click handlers to also handle file navigation
    private fun handleRepostMediaClick() {
        post?.let { currentPost ->
            if (currentPost.files.isNotEmpty()) {
                val files = currentPost.files.map { file ->
                    File(
                        _id = file._id,
                        fileId = file.fileId,
                        localPath = file.localPath,
                        url = file.url,
                        mimeType = file.mimeType,

                    ).apply {

                    }
                }
                val fileIds = currentPost.files.map { it ?: "unknown_id" }
                navigateToTappedFilesFragment(requireContext(), 0, files, fileIds as List<String>,  currentPost)
            }
        }
    }


    // Fixed setupLikeButton - Replace in your FeedAdapter.kt

    private fun setupLikeButton(data: Post) {
        updateLikeButtonUI(data.isLiked)
        updateMetricDisplay(likesCount, data.likes, "like")

        like.setOnClickListener {
            if (!like.isEnabled) return@setOnClickListener

            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

            val previousLikeStatus = data.isLiked
            val previousLikeCount = data.likes

            // Optimistic UI update
            data.isLiked = !previousLikeStatus
            data.likes = if (data.isLiked) previousLikeCount + 1 else maxOf(0, previousLikeCount - 1)

            updateLikeButtonUI(data.isLiked)
            updateMetricDisplay(likesCount, data.likes, "like")

            YoYo.with(if (data.isLiked) Techniques.Tada else Techniques.Pulse)
                .duration(500)
                .repeat(1)
                .playOn(like)

            like.isEnabled = false
            like.alpha = 0.8f

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val response = retrofitInstance.apiService.likeUnLikeFeed(data._id)

                    like.alpha = 1f
                    like.isEnabled = true

                    if (response.isSuccessful) {
                        response.body()?.let { likeResponse ->
                            if (likeResponse.success) {
                                // Sync with server data
                                data.isLiked = likeResponse.data.isLiked

                                // Handle potential null likeCount from server
                                // Since your server only returns { isLiked: true/false }
                                // We keep our optimistic count
                                // data.likes stays as is (our optimistic update)

                                updateLikeButtonUI(data.isLiked)
                                updateMetricDisplay(likesCount, data.likes, "like")

                                // Safely access likedByUserIds (it might be null)
                                val likedByCount = likeResponse.data.likedByUserIds?.size ?: 0
                                Log.d(TAG, "Like synced - isLiked=${data.isLiked}, count=${data.likes}, likedBy=$likedByCount users")

                                // Notify adapter
                                feedClickListener.likeUnLikeFeed(0, data)
                            } else {
                                Log.e(TAG, "Like failed - success=false")
                                revertLikeState(data, previousLikeStatus, previousLikeCount)
                            }
                        } ?: run {
                            Log.e(TAG, "Like response body is null")
                            revertLikeState(data, previousLikeStatus, previousLikeCount)
                        }
                    } else {
                        Log.e(TAG, "Like API error: ${response.code()} - ${response.message()}")
                        revertLikeState(data, previousLikeStatus, previousLikeCount)

                        Toast.makeText(
                            like.context,
                            "Failed to update like",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    like.alpha = 1f
                    like.isEnabled = true

                    Log.e(TAG, "Like network error", e)
                    revertLikeState(data, previousLikeStatus, previousLikeCount)

                    Toast.makeText(
                        like.context,
                        "Network error. Please check your connection.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun revertLikeState(data: Post, previousStatus: Boolean, previousCount: Int) {
        data.isLiked = previousStatus
        data.likes = previousCount
        updateLikeButtonUI(data.isLiked)
        updateMetricDisplay(likesCount, data.likes, "like")
        Log.d(TAG, "Reverted to previous state: isLiked=$previousStatus, likes=$previousCount")
    }

    private fun setupBookmarkButton(data: Post) {
        Log.d(TAG, "Setting up bookmark button - postId=${data._id}, isBookmarked=${data.isBookmarked}, count=${data.bookmarkCount}")

        updateBookmarkButtonUI(data.isBookmarked)
        updateMetricDisplay(favCount, data.bookmarkCount, "bookmark")

        fav.setOnClickListener {
            if (!fav.isEnabled) return@setOnClickListener

            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

            val newBookmarkStatus = !data.isBookmarked
            val previousBookmarkStatus = data.isBookmarked
            val previousBookmarkCount = data.bookmarkCount

            Log.d(TAG, "Bookmark clicked - Post: ${data._id}, Current: $previousBookmarkStatus → New: $newBookmarkStatus")

            // Optimistic UI update
            data.isBookmarked = newBookmarkStatus
            data.bookmarkCount = if (newBookmarkStatus) previousBookmarkCount + 1 else maxOf(0, previousBookmarkCount - 1)

            updateBookmarkButtonUI(data.isBookmarked)
            updateMetricDisplay(favCount, data.bookmarkCount, "bookmark")

            YoYo.with(if (newBookmarkStatus) Techniques.Tada else Techniques.Pulse)
                .duration(500)
                .repeat(1)
                .playOn(fav)

            fav.isEnabled = false
            fav.alpha = 0.8f

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val bookmarkRequest = BookmarkRequest(newBookmarkStatus)

                    //  Use retrofitInstance.apiService instead of retrofitInterface.apiService
                    val response = retrofitInstance.apiService.toggleBookmark(data._id, bookmarkRequest)

                    fav.alpha = 1f
                    fav.isEnabled = true

                    if (response.isSuccessful) {
                        response.body()?.let { bookmarkResponse ->
                            if (bookmarkResponse.success) {
                                val serverData = bookmarkResponse.data

                                Log.d(TAG, "Bookmark success - Server: isBookmarked=${serverData.isBookmarked}, count=${serverData.bookmarkCount}")

                                data.isBookmarked = serverData.isBookmarked
                                data.bookmarkCount = serverData.bookmarkCount

                                updateBookmarkButtonUI(data.isBookmarked)
                                updateMetricDisplay(favCount, data.bookmarkCount, "bookmark")

                                feedClickListener.feedFavoriteClick(0, data)

                                // ✅ FIXED: Use requireContext() or context
                                Toast.makeText(
                                    requireContext(),
                                    bookmarkResponse.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Log.e(TAG, "Bookmark failed - success=false")
                                revertBookmarkState(data, previousBookmarkStatus, previousBookmarkCount)
                                Toast.makeText(
                                    requireContext(),
                                    "Failed to update bookmark",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } ?: run {
                            Log.e(TAG, "Bookmark response body is null")
                            revertBookmarkState(data, previousBookmarkStatus, previousBookmarkCount)
                            Toast.makeText(
                                requireContext(),
                                "Failed to update bookmark",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Log.e(TAG, "Bookmark API error: ${response.code()} - ${response.message()}")
                        revertBookmarkState(data, previousBookmarkStatus, previousBookmarkCount)
                        Toast.makeText(
                            requireContext(),
                            "Failed to update bookmark",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    fav.alpha = 1f
                    fav.isEnabled = true

                    Log.e(TAG, "Bookmark network error", e)
                    revertBookmarkState(data, previousBookmarkStatus, previousBookmarkCount)

                    Toast.makeText(
                        requireContext(),
                        "Network error. Please check your connection.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // Helper function to revert bookmark state on error
    private fun revertBookmarkState(
        data: Post,
        previousBookmarkStatus: Boolean,
        previousBookmarkCount: Int
    ) {
        data.isBookmarked = previousBookmarkStatus
        data.bookmarkCount = previousBookmarkCount
        updateBookmarkButtonUI(data.isBookmarked)
        updateMetricDisplay(favCount, data.bookmarkCount, "bookmark")
        Log.d(TAG, "Reverted to previous state: isBookmarked=$previousBookmarkStatus, count=$previousBookmarkCount")
    }


    private fun updateLikeButtonUI(isLiked: Boolean) {

        Log.d(TAG, "Updating like button UI: isLiked=$isLiked")
        try {
            if (isLiked) {
                like.setImageResource(R.drawable.filled_favorite_like)
            } else {
                like.setImageResource(R.drawable.heart_svgrepo_com)
                like.clearColorFilter()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating like button UI", e)
        }
    }

    private fun updateBookmarkButtonUI(isBookmarked: Boolean) {
        Log.d(tag, "Updating bookmark button UI: isBookmarked=$isBookmarked")
        try {
            if (isBookmarked) {
                fav.setImageResource(R.drawable.filled_favorite)
            } else {
                fav.setImageResource(R.drawable.favorite_svgrepo_com__1_)
                fav.clearColorFilter()
            }
        } catch (e: Exception) {
            Log.e(tag, "Error updating bookmark button UI", e)
        }
    }


    private fun setupRecyclerViews() {
        recyclerViews.layoutManager = GridLayoutManager(requireContext(), 3)
        recyclerViews.isNestedScrollingEnabled = false

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        recyclerView.isNestedScrollingEnabled = false
    }

    private fun populateOriginalPostData(originalPost: OriginalPost) {
        Log.d(TAG, "populateOriginalPostData: originalPost=$originalPost")

        // Original post author info - Access through account
        originalPosterName.text = originalPost.author.account.username ?: "Unknown User"
        tvQuotedUserHandle.text = "@${originalPost.author.account.username ?: "unknown"}"

        // Load avatar from account
        loadProfileImage(originalPost.author.account.avatar.url, originalPosterProfileImage)

        // Original post content
        originalPostText.text = originalPost.content
        dateTime.text = formatDateTime(originalPost.createdAt)

        // Original post tags
        val originalTagsText = originalPost.tags.filterNotNull().joinToString(" ") { "#$it" }
        tvQuotedHashtags.text = originalTagsText
        tvQuotedHashtags.visibility = if (originalTagsText.isNotEmpty()) View.VISIBLE else View.GONE

        // Use the counts we already set in populatePostData
        updateOriginalPostInteractionStates(originalPost)

        // Handle original post media files
        handleOriginalPostMediaFiles(originalPost)
    }

    private fun handleOriginalMediaClick() {
        val currentPost = post ?: return
        currentPost.originalPost.firstOrNull()?.let { originalPost ->
            if (originalPost.files.isNotEmpty()) {
                val filesList = originalPost.files
                val fileIds = filesList.map { it.fileId }
                // Pass currentPost but use originalPost files
                navigateToTappedFilesFragment(requireContext(), 0, filesList, fileIds, currentPost)
            }
        }
    }

    private fun handleOriginalFileClick() {
        val currentPost = post ?: return
        currentPost.originalPost.firstOrNull()?.let { originalPost ->
            if (originalPost.files.isNotEmpty()) {
                val filesList = originalPost.files
                val fileIds = filesList.map { it.fileId }
                navigateToTappedFilesFragment(requireContext(), 0, filesList, fileIds, currentPost)
            }
        }
    }

    private fun populateReposterInfo(post: Post) {

        post.repostedUser?.let { reposter ->
            repostedUserName.text = reposter.username ?: "Unknown User"
            tvUserHandle.text = "@${reposter.username ?: "unknown"}"

            reposter.avatar?.url?.let { profileUrl ->
                loadProfileImage(profileUrl, userProfileImage)
            }
        }

    }



    private fun loadProfileImage(url: String, imageView: ImageView) {
        Glide.with(this)
            .load(url)
            .placeholder(R.drawable.imageplaceholder)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imageView)
    }

    private fun loadImage(url: String?, imageView: ImageView) {
        Glide.with(this)
            .load(url)
            .placeholder(R.drawable.imageplaceholder)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imageView)
    }



// CONTENT POPULATION METHODS....


    private fun populatePostContent(post: OriginalPost) {
        // Post creation date
        dateTimeCreate.text = formatDateTime(post.createdAt)
        dateTime.text = formatDateTime(post.createdAt)

        // Post content
        userComment.text = post.content.takeIf { it.isNotEmpty() } ?: "No caption"
        originalPostText.text = post.content

        // Handle tags
        val tagsText = post.tags.filterNotNull().joinToString(" ") { "#$it" }
        populateTagsViews(tagsText)
    }

    private fun populateRepostContent(post: Post) {
        // Post creation date
        dateTimeCreate.text = formatDateTime(post.createdAt)

        // Post content (repost comment)
        userComment.text = post.content.takeIf { it.isNotEmpty() } ?: "No caption"

        // Handle tags for repost
        val tagsText = post.tags.filterNotNull().joinToString(" ") { "#$it" }
        populateTagsViews(tagsText)
    }

    private fun populateTagsViews(tagsText: String) {
        tvHashtags.text = tagsText.takeIf { it.isNotEmpty() } ?: ""
        tvHashtags.visibility = if (tagsText.isNotEmpty()) View.VISIBLE else View.GONE
        tvQuotedHashtags.text = tagsText
        tvQuotedHashtags.visibility = if (tagsText.isNotEmpty()) View.VISIBLE else View.GONE
    }


    // INTERACTION DATA METHODS

    private fun populateInteractionData(post: OriginalPost) {
        likesCount.text = formatCount(post.likeCount)
        commentCount.text = formatCount(post.commentCount)
        repostCount.text = formatCount(post.repostCount)
        favCount.text = formatCount(post.bookmarkCount)
        shareCount.text = "0"

        updateInteractionStates(post)
    }

    private fun populateOriginalPostInteractionData(originalPost: OriginalPost) {
        likesCount.text = formatCount(originalPost.likeCount)
        commentCount.text = formatCount(originalPost.commentCount)
        repostCount.text = formatCount(originalPost.repostCount)
        favCount.text = formatCount(originalPost.bookmarkCount)
        shareCount.text = "0"

        updateOriginalPostInteractionStates(originalPost)
    }

    private fun updateOriginalPostInteractionStates(originalPost: OriginalPost) {
        updateLikeUI(originalPost.isReposted)
        updateFavoriteUI(originalPost.bookmarks.isNotEmpty())
        updateFollowButtonUI()

        reFeed.setImageResource(
            if (originalPost.isReposted) R.drawable.retweet
            else R.drawable.retweet
        )
    }

    private fun handleThumbnails(
        thumbnails: List<ThumbnailX>,
        imageView: ImageView
    ) {
        if (thumbnails.isNotEmpty()) {
            val thumbnailUrl = thumbnails.firstOrNull()?.thumbnailUrl
            if (thumbnailUrl?.isNotEmpty() == true && mixedFilesCardView.visibility == View.VISIBLE) {
                loadImage(thumbnailUrl, imageView)
            }
        }
    }


    private fun updateInteractionStates(post: OriginalPost) {
        // Update like button state
        updateLikeUI(post.isReposted)

        // Update bookmark/favorite button state
        updateFavoriteUI(post.bookmarks.isNotEmpty())

        // Update follow button state
        updateFollowButtonUI()

        // Update repost button state
        reFeed.setImageResource(
            if (post.isReposted) R.drawable.retweet
            else R.drawable.retweet
        )

    }


    // IMAGE MEDIA TYPE HANDLERS

    @SuppressLint("RestrictedApi")
    private fun isImageFile(file: File): Boolean {
        val mimeType = file.mimeType?.lowercase()
        val url = file.url?.lowercase()

        return when {
            mimeType?.startsWith("image") == true -> true
            url?.matches(".*\\.(jpg|jpeg|png|gif|bmp|webp)$".toRegex()) == true -> true
            else -> false
        }
    }


    // Generic function to show image media for both original posts and reposts
    private fun showImageMedia(
        files: List<File>,
        containerView: CardView,
        isRepost: Boolean = false,
        post: Any? = null
    ) {
        val context = requireContext()
        val screenWidth = context.resources.displayMetrics.widthPixels
        val margin = 4.dpToPx(context)
        val spaceBetweenRows = 4.dpToPx(context)

        containerView.removeAllViews()
        containerView.visibility = View.VISIBLE

        if (files.isEmpty()) {
            containerView.visibility = View.GONE
            return
        }

        when (files.size) {
            1 -> createSingleImageLayout(context, containerView, files, isRepost, post)
            2 -> createTwoImageLayout(context, containerView, files, spaceBetweenRows, isRepost, post)
            3 -> createThreeImageLayout(context, containerView, files, margin, spaceBetweenRows, isRepost, post)
            else -> createGridImageLayout(context, containerView, files, screenWidth, spaceBetweenRows, isRepost, post)
        }
    }

    private fun createSingleImageLayout(
        context: Context,
        containerView: CardView,
        files: List<File>,
        isRepost: Boolean,
        post: Any?
    ) {
        val cardView = createImageCard(context, files, 0, isRepost, post)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            300.dpToPx(context)
        )
        cardView.layoutParams = layoutParams
        containerView.addView(cardView)
    }

    private fun createTwoImageLayout(
        context: Context,
        containerView: CardView,
        files: List<File>,
        spaceBetweenRows: Int,
        isRepost: Boolean,
        post: Any?
    ) {
        val horizontalLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        for (i in 0..1) {
            val cardView = createImageCard(context, files, i, isRepost, post)
            val layoutParams = LinearLayout.LayoutParams(0, 300.dpToPx(context), 1f)
            layoutParams.apply {
                if (i == 0) rightMargin = spaceBetweenRows / 2
                else leftMargin = spaceBetweenRows / 2
            }
            cardView.layoutParams = layoutParams
            horizontalLayout.addView(cardView)
        }
        containerView.addView(horizontalLayout)
    }

    private fun createThreeImageLayout(
        context: Context,
        containerView: CardView,
        files: List<File>,
        margin: Int,
        spaceBetweenRows: Int,
        isRepost: Boolean,
        post: Any?
    ) {
        val verticalLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // First image - full width
        val firstCard = createImageCard(context, files, 0, isRepost, post)
        val firstParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            300.dpToPx(context)
        ).apply { bottomMargin = margin }
        firstCard.layoutParams = firstParams
        verticalLayout.addView(firstCard)

        // Bottom row with two images
        val bottomLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        for (i in 1..2) {
            val cardView = createImageCard(context, files, i, isRepost, post)
            val layoutParams = LinearLayout.LayoutParams(0, 300.dpToPx(context), 1f)
            layoutParams.apply {
                if (i == 1) rightMargin = spaceBetweenRows / 2
                else leftMargin = spaceBetweenRows / 2
            }
            cardView.layoutParams = layoutParams
            bottomLayout.addView(cardView)
        }

        verticalLayout.addView(bottomLayout)
        containerView.addView(verticalLayout)
    }

    private fun createGridImageLayout(
        context: Context,
        containerView: CardView,
        files: List<File>,
        screenWidth: Int,
        spaceBetweenRows: Int,
        isRepost: Boolean,
        post: Any?
    ) {
        val verticalLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Create two rows
        listOf(0..1, 2..3).forEachIndexed { rowIndex, range ->
            val rowLayout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                if (rowIndex == 1) setPadding(0, spaceBetweenRows, 0, 0)
            }

            range.forEach { i ->
                if (i < files.size) { // Add bounds check
                    val extraCount = if (i == 3 && files.size > 4) files.size - 4 else 0
                    val cardView = createImageCard(context, files, i, isRepost, post, extraCount)
                    val size = (screenWidth - spaceBetweenRows) / 2
                    val layoutParams = LinearLayout.LayoutParams(size, size)
                    layoutParams.apply {
                        if (i % 2 == 0) rightMargin = spaceBetweenRows / 2
                        else leftMargin = spaceBetweenRows / 2
                    }
                    cardView.layoutParams = layoutParams
                    rowLayout.addView(cardView)
                }
            }
            verticalLayout.addView(rowLayout)
        }
        containerView.addView(verticalLayout)
    }

    private fun createImageCard(
        context: Context,
        files: List<File>,
        index: Int,
        isRepost: Boolean,
        post: Any?,
        extraCount: Int = 0
    ): CardView {
        val cardView = CardView(context).apply {
            radius = 8.dpToPx(context).toFloat()
            cardElevation = 4.dpToPx(context).toFloat()
            setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
        }

        val frameLayout = FrameLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // Add all UI components
        val imageView = createImageView(context)
        val countTextView = createImageCountTextView(context, extraCount)

        // Load image
        loadImageWithGlide(context, imageView, files, index)

        // Build view hierarchy
        frameLayout.apply {
            addView(imageView)
            addView(countTextView)
        }
        cardView.addView(frameLayout)

        // Set click listener
        cardView.setOnClickListener {
            handleImageClick(index, files, isRepost, post)
        }

        return cardView
    }

    private fun createImageView(context: Context): ImageView {
        return ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(ContextCompat.getColor(context, android.R.color.darker_gray))
        }
    }

    private fun createImageCountTextView(context: Context, extraCount: Int): TextView {
        return TextView(context).apply {
            setTextColor(Color.WHITE)
            textSize = 32f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(Color.parseColor("#80000000"))
            }
            visibility = if (extraCount > 0) View.VISIBLE else View.GONE
            text = if (extraCount > 0) "+$extraCount" else ""
        }
    }

    private fun loadImageWithGlide(
        context: Context,
        imageView: ImageView,
        files: List<File>,
        index: Int
    ) {
        if (index >= files.size) {
            imageView.setImageResource(R.drawable.imageplaceholder)
            return
        }

        val file = files[index]
        val imageUrl = file.url

        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.imageplaceholder)
                .error(R.drawable.imageplaceholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView)
        } else {
            imageView.setImageResource(R.drawable.imageplaceholder)
        }
    }

    private fun handleImageClick(index: Int, files: List<File>, isRepost: Boolean, post: Any?) {
        val logTag = if (isRepost) "RepostImageClick" else "OriginalImageClick"
        Log.d(logTag, "Image at index $index clicked")

        if (index < files.size) {
            val imageFile = files[index]
            // Extract fileIds from post object
            val fileIds: List<String> = when (post) {
                is OriginalPost -> post.fileIds as? List<String> ?: emptyList()
                is Post -> post.fileIds as? List<String> ?: emptyList()
                else -> emptyList()
            }

            // Call the image click listener
            onImageClickListener?.invoke(index, files, fileIds)
        }
    }

    // Convenience methods for different post types
    private fun showOriginalImageMedia(originalPost: OriginalPost, firstFile: File) {
        val imageFiles = originalPost.files.filter { isImageFile(it) }

        showImageMedia(
            files = imageFiles,
            containerView = mixedFilesCardView,
            isRepost = false,
            post = originalPost
        )
    }

    private fun showRepostImageMedia(post: Post, firstFile: File) {
        val imageFiles = post.files.filter { isImageFile(it) }

        showImageMedia(
            files = imageFiles,
            containerView = mixedFilesCardViews,
            isRepost = true,
            post = post
        )
    }


    // Add this property to handle clicks
    private var onImageClickListener: ((Int, List<File>, List<String>) -> Unit)? = null


    // AUDIO MEDIA TYPE HANDLERS

    @SuppressLint("RestrictedApi")
    private fun isAudioFile(file: File): Boolean {
        val mimeType = file.mimeType?.lowercase()
        val url = file.url?.lowercase()

        return when {
            mimeType?.startsWith("audio") == true -> true
            url?.matches(".*\\.(mp3|wav|aac|ogg|flac|m4a)$".toRegex()) == true -> true
            else -> false
        }
    }

    // Generic function to show audio media for both original posts and reposts
    private fun showAudioMedia(
        files: List<File>,
        containerView: ViewGroup,
        durationData: List<Any?> = emptyList(),
        thumbnailData: List<Thumbnail>? = null,
        isRepost: Boolean = false,
        post: Any? = null
    ) {
        val context = containerView.context
        val spaceBetweenRows = 8.dpToPx(context)

        containerView.removeAllViews()
        containerView.visibility = View.VISIBLE

        if (files.isEmpty()) {
            containerView.visibility = View.GONE
            return
        }

        when (files.size) {
            1 -> createSingleAudioLayout(context, containerView, files,
                durationData as List<AudioDuration>, thumbnailData, isRepost, post)
            2 -> createTwoAudioLayout(context, containerView, files,
                durationData as List<AudioDuration>, thumbnailData, spaceBetweenRows, isRepost, post)
            3 -> createThreeAudioLayout(context, containerView, files,
                durationData as List<AudioDuration>, thumbnailData, spaceBetweenRows, isRepost, post)
            else -> createMultiAudioLayout(context, containerView, files,
                durationData as List<AudioDuration>, thumbnailData, spaceBetweenRows, isRepost, post)
        }
    }

    private fun createSingleAudioLayout(
        context: Context,
        containerView: ViewGroup,
        files: List<File>,
        durationData: List<AudioDuration>,
        thumbnailData: List<Thumbnail>?,
        isRepost: Boolean,
        post: Any?
    ) {
        val audioView = createAudioCard(context, files, 0, durationData, thumbnailData, isRepost, post)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            120.dpToPx(context)
        )
        containerView.addView(audioView, layoutParams)
    }

    private fun createTwoAudioLayout(
        context: Context,
        containerView: ViewGroup,
        files: List<File>,
        durationData: List<AudioDuration>,
        thumbnailData: List<Thumbnail>?,
        spaceBetweenRows: Int,
        isRepost: Boolean,
        post: Any?
    ) {
        for (i in 0..1) {
            val audioView = createAudioCard(context, files, i, durationData, thumbnailData, isRepost, post)
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                120.dpToPx(context)
            ).apply {
                if (i == 0) bottomMargin = spaceBetweenRows
            }
            containerView.addView(audioView, layoutParams)
        }
    }

    private fun createThreeAudioLayout(
        context: Context,
        containerView: ViewGroup,
        files: List<File>,
        durationData: List<AudioDuration>,
        thumbnailData: List<Thumbnail>?,
        spaceBetweenRows: Int,
        isRepost: Boolean,
        post: Any?
    ) {
        for (i in 0..2) {
            val audioView = createAudioCard(context, files, i, durationData, thumbnailData, isRepost, post)
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                120.dpToPx(context)
            ).apply {
                if (i < 2) bottomMargin = spaceBetweenRows
            }
            containerView.addView(audioView, layoutParams)
        }
    }

    private fun createMultiAudioLayout(
        context: Context,
        containerView: ViewGroup,
        files: List<File>,
        durationData: List<AudioDuration>,
        thumbnailData: List<Thumbnail>?,
        spaceBetweenRows: Int,
        isRepost: Boolean,
        post: Any?
    ) {
        // Show first 3 items with count overlay on last
        for (i in 0..2) {
            val extraCount = if (i == 2) files.size - 3 else 0
            val audioView = createAudioCard(context, files, i, durationData, thumbnailData, isRepost, post, extraCount)
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                120.dpToPx(context)
            ).apply {
                if (i < 2) bottomMargin = spaceBetweenRows
            }
            containerView.addView(audioView, layoutParams)
        }
    }

    private fun createAudioCard(
        context: Context,
        files: List<File>,
        index: Int,
        durationData: List<AudioDuration>,
        thumbnailData: List<Thumbnail>?,
        isRepost: Boolean,
        post: Any?,
        extraCount: Int = 0
    ): MaterialCardView {
        return MaterialCardView(context).apply {
            radius = 12.dpToPx(context).toFloat()
            cardElevation = 2.dpToPx(context).toFloat()
            setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
            strokeWidth = 1.dpToPx(context)
            strokeColor = ContextCompat.getColor(context, android.R.color.darker_gray)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                120.dpToPx(context)
            )

            addView(createAudioContent(context, files, index, durationData, thumbnailData, extraCount))

            setOnClickListener {
                handleAudioClick(index, files, isRepost, post)
            }
        }
    }

    private fun createAudioContent(
        context: Context,
        files: List<File>,
        index: Int,
        durationData: List<AudioDuration>,
        thumbnailData: List<Thumbnail>?,
        extraCount: Int
    ): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setPadding(16.dpToPx(context), 12.dpToPx(context), 16.dpToPx(context), 12.dpToPx(context))

            // Thumbnail container
            addView(createAudioThumbnailContainer(context, files, index, thumbnailData))

            // Text content
            addView(createAudioTextContent(context, files, index, durationData))

            // Extra count overlay if needed
            if (extraCount > 0) {
                addView(createCountOverlay(context, extraCount))
            }
        }
    }

    private fun createCountOverlay(context: Context, extraCount: Int): View {
        return FrameLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#80000000")) // Semi-transparent black

            addView(TextView(context).apply {
                text = "+$extraCount"
                textSize = 24f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            })
        }
    }

    private fun createAudioThumbnailContainer(
        context: Context,
        files: List<File>,
        index: Int,
        thumbnailData: List<Thumbnail>?
    ): FrameLayout {
        return FrameLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                64.dpToPx(context),
                64.dpToPx(context),
                Gravity.CENTER
            )

            // Add thumbnail image view
            addView(createThumbnailImageView(context, files, index, thumbnailData))

            // Add play button overlay
            addView(createPlayButtonOverlay(context))
        }
    }

    private fun createThumbnailImageView(
        context: Context,
        files: List<File>,
        index: Int,
        thumbnailData: List<Thumbnail>?
    ): ImageView {
        return ImageView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = ContextCompat.getDrawable(context, R.drawable.baseline_headphones_24) // Your default drawable

            val thumbnailUrl = getAudioThumbnailUrl(thumbnailData, index)
            if (!thumbnailUrl.isNullOrEmpty()) {
                // Load thumbnail using your preferred image loading library (Glide, Coil, etc.)
                Glide.with(context)
                    .load(thumbnailUrl)
                    .placeholder(R.drawable.baseline_headphones_24)
                    .error(R.drawable.baseline_headphones_24)
                    .into(this)
            } else {
                setImageResource(R.drawable.baseline_headphones_24)
            }
        }
    }

    private fun createPlayButtonOverlay(context: Context): ImageView {
        return ImageView(context).apply {
            setImageDrawable(ContextCompat.getDrawable(context, R.drawable.baseline_play_arrow_24))
            layoutParams = FrameLayout.LayoutParams(
                32.dpToPx(context),
                32.dpToPx(context),
                Gravity.CENTER
            )
            setColorFilter(ContextCompat.getColor(context, android.R.color.white), PorterDuff.Mode.SRC_IN)
        }
    }

    private fun createAudioTextContent(
        context: Context,
        files: List<File>,
        index: Int,
        durationData: List<AudioDuration>
    ): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            setPadding(16.dpToPx(context), 0, 0, 0)

            // Title
            addView(TextView(context).apply {
               // text = files.getOrNull(index)?.name ?: "Audio ${index + 1}"
                textSize = 16f
                setTextColor(Color.BLACK)
                typeface = Typeface.DEFAULT_BOLD
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                ellipsize = TextUtils.TruncateAt.END
                maxLines = 1
            })

            // Duration
            addView(TextView(context).apply {
                text = getAudioDuration(durationData, index) ?: "0:00"
                textSize = 14f
                setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            })
        }
    }

    private fun getAudioDuration(
        durationData: List<AudioDuration>,
        index: Int
    ): String? {
        return when {
            index < durationData.size -> durationData[index].duration
            durationData.isNotEmpty() -> durationData.first().duration
            else -> null
        }
    }

    private fun getAudioThumbnailUrl(thumbnailData: List<Thumbnail>?, index: Int): String? {
        return when {
            thumbnailData != null && index < thumbnailData.size && !thumbnailData[index].thumbnailUrl.isNullOrBlank() ->
                thumbnailData[index].thumbnailUrl
            else -> null
        }
    }

    private fun handleAudioClick(index: Int, files: List<File>, isRepost: Boolean, post: Any?) {
        val logTag = if (isRepost) "RepostAudioClick" else "OriginalAudioClick"
        Log.d(logTag, "Audio at index $index clicked")

        if (index < files.size) {
            files[index]

        }
    }

    // Convenience methods for different post types
    private fun showOriginalAudioMedia(originalPost: OriginalPost, firstFile: File) {
        showAudioMedia(
            files = originalPost.files,
            containerView = binding.multipleAudiosContainer,
            durationData = originalPost.duration ?: emptyList(),
            isRepost = false,
            post = originalPost
        )
    }

    private fun showRepostAudioMedia(post: Post, firstFile: File) {
        showAudioMedia(
            files = post.files,
            containerView = binding.multipleAudiosContainers,
            durationData = post.duration ?: emptyList(),
            isRepost = true,
            post = post
        )
    }

    // VIDEOS MEDIA TYPE HANDLERS

    @SuppressLint("RestrictedApi")
    private fun isVideoFile(file: File): Boolean {
        val mimeType = file.mimeType?.lowercase()
        val url = file.url?.lowercase()

        return when {
            mimeType?.startsWith("video") == true -> true
            url?.matches(".*\\.(mp4|avi|mkv|mov|wmv|flv)$".toRegex()) == true -> true
            else -> false
        }
    }

    // Extension function for dp to px conversion
    private fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }

    // Generic function to show video media for both original posts and reposts
    private fun showVideoMedia(
        files: List<File>,
        containerView: CardView,
        thumbnailData: List<Thumbnail>? = null,
        durationData: List<Duration>? = null, // Changed from List<Any?> to List<Duration>?
        isRepost: Boolean = false,
        post: Any? = null
    ) {
        val context = requireContext()
        val screenWidth = context.resources.displayMetrics.widthPixels
        val margin = 4.dpToPx(context)
        val spaceBetweenRows = 4.dpToPx(context)

        containerView.removeAllViews()
        containerView.visibility = View.VISIBLE

        if (files.isEmpty()) {
            containerView.visibility = View.GONE
            return
        }

        when (files.size) {
            1 -> createSingleVideoLayout(context, containerView, files, thumbnailData,
                durationData, isRepost, post)
            2 -> createTwoVideoLayout(context, containerView, files, thumbnailData,
                durationData, spaceBetweenRows, isRepost, post)
            3 -> createThreeVideoLayout(context, containerView, files, thumbnailData,
                durationData, margin, spaceBetweenRows, isRepost, post)
            else -> createGridVideoLayout(context, containerView, files, thumbnailData,
                durationData, screenWidth, spaceBetweenRows, isRepost, post)
        }
    }

    private fun createSingleVideoLayout(
        context: Context,
        containerView: CardView,
        files: List<File>,
        thumbnailData: List<Thumbnail>?,
        durationData: List<Duration>?,
        isRepost: Boolean,
        post: Any?
    ) {
        val cardView = createVideoCard(context, files, 0, thumbnailData, durationData, isRepost, post)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            250.dpToPx(context)
        )
        cardView.layoutParams = layoutParams
        containerView.addView(cardView)
    }

    private fun createTwoVideoLayout(
        context: Context,
        containerView: CardView,
        files: List<File>,
        thumbnailData: List<Thumbnail>?,
        durationData: List<Duration>?,
        spaceBetweenRows: Int,
        isRepost: Boolean,
        post: Any?
    ) {
        val horizontalLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        for (i in 0..1) {
            val cardView = createVideoCard(context, files, i, thumbnailData, durationData, isRepost, post)
            val layoutParams = LinearLayout.LayoutParams(0, 200.dpToPx(context), 1f)
            layoutParams.apply {
                if (i == 0) rightMargin = spaceBetweenRows / 2
                else leftMargin = spaceBetweenRows / 2
            }
            cardView.layoutParams = layoutParams
            horizontalLayout.addView(cardView)
        }
        containerView.addView(horizontalLayout)
    }

    private fun createThreeVideoLayout(
        context: Context,
        containerView: CardView,
        files: List<File>,
        thumbnailData: List<Thumbnail>?,
        durationData: List<Duration>?,
        margin: Int,
        spaceBetweenRows: Int,
        isRepost: Boolean,
        post: Any?
    ) {
        val verticalLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // First video - full width
        val firstCard = createVideoCard(context, files, 0, thumbnailData, durationData, isRepost, post)
        val firstParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            200.dpToPx(context)
        ).apply { bottomMargin = margin }
        firstCard.layoutParams = firstParams
        verticalLayout.addView(firstCard)

        // Bottom row with two videos
        val bottomLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        for (i in 1..2) {
            val cardView = createVideoCard(context, files, i, thumbnailData, durationData, isRepost, post)
            val layoutParams = LinearLayout.LayoutParams(0, 200.dpToPx(context), 1f)
            layoutParams.apply {
                if (i == 1) rightMargin = spaceBetweenRows / 2
                else leftMargin = spaceBetweenRows / 2
            }
            cardView.layoutParams = layoutParams
            bottomLayout.addView(cardView)
        }

        verticalLayout.addView(bottomLayout)
        containerView.addView(verticalLayout)
    }

    private fun createGridVideoLayout(
        context: Context,
        containerView: CardView,
        files: List<File>,
        thumbnailData: List<Thumbnail>?,
        durationData: List<Duration>?,
        screenWidth: Int,
        spaceBetweenRows: Int,
        isRepost: Boolean,
        post: Any?
    ) {
        val verticalLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Create two rows
        listOf(0..1, 2..3).forEachIndexed { rowIndex, range ->
            val rowLayout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                if (rowIndex == 1) setPadding(0, spaceBetweenRows, 0, 0)
            }

            range.forEach { i ->
                if (i < files.size) { // Add bounds check
                    val extraCount = if (i == 3 && files.size > 4) files.size - 4 else 0
                    val cardView = createVideoCard(context, files, i, thumbnailData, durationData, isRepost, post, extraCount)
                    val size = (screenWidth - spaceBetweenRows) / 2
                    val layoutParams = LinearLayout.LayoutParams(size, size)
                    layoutParams.apply {
                        if (i % 2 == 0) rightMargin = spaceBetweenRows / 2
                        else leftMargin = spaceBetweenRows / 2
                    }
                    cardView.layoutParams = layoutParams
                    rowLayout.addView(cardView)
                }
            }
            verticalLayout.addView(rowLayout)
        }
        containerView.addView(verticalLayout)
    }

    private fun createVideoCard(
        context: Context,
        files: List<File>,
        index: Int,
        thumbnailData: List<Thumbnail>?,
        durationData: List<Duration>?,
        isRepost: Boolean,
        post: Any?,
        extraCount: Int = 0
    ): CardView {
        val cardView = CardView(context).apply {
            radius = 8.dpToPx(context).toFloat()
            cardElevation = 4.dpToPx(context).toFloat()
            setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
        }

        val frameLayout = FrameLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // Add all UI components
        val thumbnailImageView = createVideoThumbnailImageView(context)
        val playButton = createVideoPlayButton(context)
        val durationTextView = createVideoDurationTextView(context)
        val countTextView = createVideoCountTextView(context, extraCount)

        // Load data
        loadVideoThumbnailAndDuration(context, thumbnailImageView, durationTextView, files, index, thumbnailData, durationData)

        // Build view hierarchy
        frameLayout.apply {
            addView(thumbnailImageView)
            addView(playButton)
            addView(durationTextView)
            addView(countTextView)
        }
        cardView.addView(frameLayout)

        // Set click listener
        cardView.setOnClickListener {
            handleVideoClick(index, files, isRepost)
        }

        return cardView
    }

    private fun createVideoThumbnailImageView(context: Context): ImageView {
        return ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(ContextCompat.getColor(context, android.R.color.darker_gray))
        }
    }

    private fun createVideoPlayButton(context: Context): ImageView {
        return ImageView(context).apply {
            setImageDrawable(ContextCompat.getDrawable(context, R.drawable.baseline_play_arrow_24))
            layoutParams = FrameLayout.LayoutParams(
                64.dpToPx(context),
                64.dpToPx(context),
                Gravity.CENTER
            )
            scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
    }

    private fun createVideoDurationTextView(context: Context): TextView {
        return TextView(context).apply {
            setTextColor(Color.WHITE)
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(8.dpToPx(context), 4.dpToPx(context), 8.dpToPx(context), 4.dpToPx(context))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 4.dpToPx(context).toFloat()
                setColor(Color.parseColor("#80000000"))
            }
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.END
                marginEnd = 8.dpToPx(context)
                bottomMargin = 8.dpToPx(context)
            }
        }
    }

    private fun createVideoCountTextView(context: Context, extraCount: Int): TextView {
        return TextView(context).apply {
            setTextColor(Color.WHITE)
            textSize = 32f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(Color.parseColor("#80000000"))
            }
            visibility = if (extraCount > 0) View.VISIBLE else View.GONE
            text = if (extraCount > 0) "+$extraCount" else ""
        }
    }

    private fun loadVideoThumbnailAndDuration(
        context: Context,
        thumbnailImageView: ImageView,
        durationTextView: TextView,
        files: List<File>,
        index: Int,
        thumbnailData: List<Thumbnail>?,
        durationData: List<Duration>?
    ) {
        if (index >= files.size) {
            thumbnailImageView.setImageResource(R.drawable.videoplaceholder)
            durationTextView.visibility = View.GONE
            return
        }

        val file = files[index]

        // Load thumbnail
        val thumbnailUrl = getVideoThumbnailUrl(thumbnailData, file.fileId, index)
        if (!thumbnailUrl.isNullOrEmpty()) {
            Glide.with(context)
                .load(thumbnailUrl)
                .placeholder(R.drawable.videoplaceholder)
                .error(R.drawable.videoplaceholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(thumbnailImageView)
        } else {
            thumbnailImageView.setImageResource(R.drawable.videoplaceholder)
        }

        // Load duration
        val duration = getVideoDuration(durationData, file.fileId, index)
        if (!duration.isNullOrEmpty()) {
            durationTextView.text = duration
            durationTextView.visibility = View.VISIBLE
        } else {
            durationTextView.visibility = View.GONE
        }
    }

    private fun getVideoThumbnailUrl(thumbnailData: List<Thumbnail>?, fileId: String?, index: Int): String? {
        return when {
            // First try to match by fileId
            thumbnailData != null && !fileId.isNullOrEmpty() -> {
                thumbnailData.find { it._id == fileId }?.thumbnailUrl
            }
            // Fallback to index if fileId doesn't match
            thumbnailData != null && index < thumbnailData.size -> {
                thumbnailData[index].thumbnailUrl
            }
            else -> null
        }
    }

    private fun getVideoDuration(durationData: List<Duration>?, fileId: String?, index: Int): String? {
        return when {
            // First try to match by fileId
            durationData != null && !fileId.isNullOrEmpty() -> {
                durationData.find { it.fileId == fileId }?.duration
            }
            // Fallback to index if fileId doesn't match
            durationData != null && index < durationData.size -> {
                durationData[index].duration
            }
            else -> null
        }
    }

    private fun handleVideoClick(index: Int, files: List<File>, isRepost: Boolean) {
        val logTag = if (isRepost) "RepostVideoClick" else "OriginalVideoClick"
        Log.d(logTag, "Video at index $index clicked")

        if (index < files.size) {
            files[index]
            navigateToVideoFragment(files, index)
        }
    }

    private fun navigateToVideoFragment(files: List<File>, position: Int) {
        val bundle = Bundle().apply {
            putSerializable("video_files", ArrayList(files))
            putInt("current_position", position)
        }

        val fragment = Tapped_Files_In_The_Container_View_Fragment().apply {
            arguments = bundle
        }

        parentFragmentManager.beginTransaction()
            .replace(android.R.id.content, fragment)
            .addToBackStack("video_detail")
            .commit()
    }



    // Convenience methods for different post types
    private fun showOriginalVideoMedia(originalPost: OriginalPost, firstFile: File) {
        showVideoMedia(
            files = originalPost.files,
            containerView = mixedFilesCardView,
            durationData = originalPost.duration, // Remove ?: emptyList() and casting
            isRepost = false,
            post = originalPost
        )
    }

    private fun showRepostVideoMedia(post: Post, firstFile: File) {
        originalFeedImages.visibility = View.GONE

        showVideoMedia(
            files = post.files,
            containerView = mixedFilesCardViews,
            durationData = post.duration, // Remove ?: emptyList() and casting
            isRepost = true,
            post = post
        )
    }


    // DOCUMENTS MEDIA TYPE HANDLERS

    @SuppressLint("RestrictedApi")

    private fun isDocumentFile(file: File): Boolean {
        val mimeType = file.mimeType?.lowercase()
        val url = file.url?.lowercase()

        return when {
            // PDF files
            mimeType?.contains("pdf") == true || url?.contains(".pdf") == true -> true

            // Microsoft Office files
            mimeType?.contains("msword") == true || url?.contains(".doc") == true -> true
            mimeType?.contains("wordprocessingml") == true || url?.contains(".docx") == true -> true
            mimeType?.contains("ms-excel") == true || url?.contains(".xls") == true -> true
            mimeType?.contains("spreadsheetml") == true || url?.contains(".xlsx") == true -> true
            mimeType?.contains("ms-powerpoint") == true || url?.contains(".ppt") == true -> true
            mimeType?.contains("presentationml") == true || url?.contains(".pptx") == true -> true

            // Text files
            mimeType?.contains("text/plain") == true || url?.contains(".txt") == true -> true
            mimeType?.contains("text/rtf") == true || url?.contains(".rtf") == true -> true

            // OpenDocument files
            mimeType?.contains("opendocument") == true -> true
            url?.contains(".odt") == true || url?.contains(".ods") == true || url?.contains(".odp") == true -> true

            else -> false
        }
    }

    private fun getDocumentPlaceholder(file: File): Int {
        val mimeType = file.mimeType?.lowercase()
        val url = file.url?.lowercase()

        return when {
            // PDF files
            mimeType?.contains("pdf") == true ||
                    url?.contains(".pdf") == true ->
                R.drawable.pdf_placeholder// Replace with PDF icon if available

            // Microsoft Word files
            mimeType?.contains("msword") == true ||
                    mimeType?.contains("wordprocessingml") == true ||
                    url?.contains(".doc") == true ||
                    url?.contains(".docx") == true ->
                R.drawable.word_placeholder // Replace with Word icon if available

            // Microsoft Excel files
            mimeType?.contains("ms-excel") == true ||
                    mimeType?.contains("spreadsheetml") == true ||
                    url?.contains(".xls") == true ||
                    url?.contains(".xlsx") == true ->
                R.drawable.excel_placeholder // Replace with Excel icon if available

            // Microsoft PowerPoint files
            mimeType?.contains("ms-powerpoint") == true ||
                    mimeType?.contains("presentationml") == true ||
                    url?.contains(".ppt") == true ||
                    url?.contains(".pptx") == true ->
                R.drawable.powerpoint_placeholder// Replace with PowerPoint icon if available

            // Text files
            mimeType?.contains("text") == true ||
                    url?.contains(".txt") == true
                    || url?.contains(".rtf") == true ->
                R.drawable.text_placeholder // Replace with text file icon if available

            else -> R.drawable.text_placeholder
        }
    }


    private fun showOriginalDocumentMedia(originalPost: OriginalPost, firstFile: File) {
        mixedFilesCardView.visibility = View.VISIBLE
        multipleAudiosContainer.visibility = View.GONE
        recyclerView.visibility = View.GONE

        fun Int.dpToPx(context1: Context?, context: Context): Int {
            return (this * context.resources.displayMetrics.density).toInt()
        }

        val fileSize = originalPost.files.size
        val sideMargin = 2.dpToPx(context, requireContext())
        val standardImageHeight = 300.dpToPx(context, requireContext())
        var cornerRadius: Float = 14.dpToPx(context, requireContext()).toFloat()

        // Overload for Int values if needed
        fun dpToPx(context: Context, dp: Int): Int {
            return (dp * context.resources.displayMetrics.density).toInt()
        }

        when {
            fileSize == 1 -> {
                val context = requireContext()
                val topMargin = (-8).dpToPx(context, context)

                val containerParams = mixedFilesCardView.layoutParams as ViewGroup.MarginLayoutParams
                containerParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                containerParams.height = standardImageHeight
                containerParams.setMargins(0, topMargin, 0, 0)
                mixedFilesCardView.layoutParams = containerParams
                mixedFilesCardView.setBackgroundColor(Color.WHITE) // Changed from BLACK to WHITE

                // Clear container
                mixedFilesCardView.removeAllViews()

                val centerContainer = FrameLayout(context).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setPadding(0, 0, 0, 0)
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.RECTANGLE
                        cornerRadius = cornerRadius
                        setColor(Color.WHITE) // Changed from gray to WHITE
                    }
                }

                // Create ImageView for document
                val singleImageView = ImageView(context).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER
                    ).apply {
                        height = standardImageHeight
                    }
                    scaleType = ImageView.ScaleType.FIT_CENTER
                }

                centerContainer.addView(singleImageView)

                // Create overlay for file type icon
                val overlayLayout = FrameLayout(context).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }

                // Find the first file's document type
                val fileIdToFind = originalPost.fileIds.firstOrNull()
                val documentType = originalPost.fileTypes?.find { it.fileId == fileIdToFind }

                // Create file type icon for overlay
                val overlayFileIcon = ImageView(context).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        20.dpToPx(context, context),
                        20.dpToPx(context, context),
                        Gravity.TOP or Gravity.START
                    ).apply {
                        setMargins(8.dpToPx(context, context), 8.dpToPx(context, context), 0, 0)
                    }

                    documentType?.let { docType ->
                        setImageResource(
                            when (docType.fileType) {
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
                    }
                    visibility = View.VISIBLE
                }

                overlayLayout.addView(overlayFileIcon)
                centerContainer.addView(overlayLayout)
                mixedFilesCardView.addView(centerContainer)

                // Load thumbnail with rounded corners
                val thumbnailUrl = originalPost.thumbnail.firstOrNull()?.thumbnailUrl
                if (!thumbnailUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(thumbnailUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .transform(RoundedCorners(cornerRadius.toInt()))
                        .into(singleImageView)
                }
            }

            fileSize == 2 -> {
                val context = requireContext()

                // Clear container and set it up for side-by-side layout
                mixedFilesCardView.removeAllViews()

                val containerParams = mixedFilesCardView.layoutParams as ViewGroup.MarginLayoutParams
                containerParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                containerParams.height = standardImageHeight
                containerParams.setMargins(0, 0, 0, 0)
                mixedFilesCardView.layoutParams = containerParams

                // Create horizontal LinearLayout for side-by-side images
                val horizontalLayout = LinearLayout(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    orientation = LinearLayout.HORIZONTAL
                }

                // Create two ImageView containers for the two files
                for (index in 0..1) {
                    val imageContainer = FrameLayout(context).apply {
                        val params = LinearLayout.LayoutParams(
                            0,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            1f // Equal weight for both images
                        )

                        when (index) {
                            0 -> params.setMargins(0, 0, sideMargin, 0) // First image: margin on right
                            1 -> params.setMargins(sideMargin, 0, 0, 0) // Second image: margin on left
                        }

                        layoutParams = params
                        background = GradientDrawable().apply {
                            shape = GradientDrawable.RECTANGLE
                            cornerRadius = cornerRadius
                            setColor(Color.WHITE) // Changed from gray to WHITE
                        }
                    }

                    val imageView = ImageView(context).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        scaleType = ImageView.ScaleType.FIT_XY
                    }

                    imageContainer.addView(imageView)

                    // Add file type icon overlay for each image
                    val fileIdToFind = originalPost.fileIds.getOrNull(index)
                    val documentType = originalPost.fileTypes?.find { it.fileId == fileIdToFind }

                    val fileIconOverlay = ImageView(context).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            20.dpToPx(context, context),
                            20.dpToPx(context, context),
                            Gravity.TOP or Gravity.START
                        ).apply {
                            setMargins(8.dpToPx(context, context), 8.dpToPx(context, context), 0, 0)
                        }

                        documentType?.let { docType ->
                            setImageResource(
                                when (docType.fileType) {
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
                        }
                        visibility = View.VISIBLE
                    }

                    imageContainer.addView(fileIconOverlay)

                    // Load the corresponding thumbnail with rounded corners
                    val thumbnailUrl = originalPost.thumbnail.getOrNull(index)?.thumbnailUrl
                    if (!thumbnailUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(thumbnailUrl)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .transform(RoundedCorners(cornerRadius.toInt()))
                            .into(imageView)
                    } else {
                        imageView.setImageResource(getDocumentPlaceholder(firstFile))
                    }

                    horizontalLayout.addView(imageContainer)
                }

                mixedFilesCardView.addView(horizontalLayout)
            }

            fileSize >= 3 -> {
                val context = requireContext()

                // Clear container and set it up for side-by-side layout
                mixedFilesCardView.removeAllViews()

                val containerParams = mixedFilesCardView.layoutParams as ViewGroup.MarginLayoutParams
                containerParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                containerParams.height = standardImageHeight
                containerParams.setMargins(0, 0, 0, 0)
                mixedFilesCardView.layoutParams = containerParams

                // Create horizontal LinearLayout for side-by-side images
                val horizontalLayout = LinearLayout(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    orientation = LinearLayout.HORIZONTAL
                }

                // Create two ImageView containers for the first two files
                for (index in 0..1) {
                    val imageContainer = FrameLayout(context).apply {
                        val params = LinearLayout.LayoutParams(
                            0,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            1f // Equal weight for both images
                        )

                        when (index) {
                            0 -> params.setMargins(0, 0, sideMargin, 0) // First image: margin on right
                            1 -> params.setMargins(sideMargin, 0, 0, 0) // Second image: margin on left
                        }

                        layoutParams = params
                        background = GradientDrawable().apply {
                            shape = GradientDrawable.RECTANGLE
                            cornerRadius = cornerRadius
                            setColor(Color.WHITE) // Changed from gray to WHITE
                        }
                    }

                    val imageView = ImageView(context).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        scaleType = ImageView.ScaleType.FIT_XY
                    }

                    imageContainer.addView(imageView)

                    // Add file type icon overlay for each image
                    val fileIdToFind = originalPost.fileIds.getOrNull(index)
                    val documentType = originalPost.fileTypes?.find { it.fileId == fileIdToFind }

                    val fileIconOverlay = ImageView(context).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            20.dpToPx(context, context),
                            20.dpToPx(context, context),
                            Gravity.TOP or Gravity.START
                        ).apply {
                            setMargins(8.dpToPx(context, context),
                                8.dpToPx(context, context), 0, 0)
                        }

                        documentType?.let { docType ->
                            setImageResource(
                                when (docType.fileType) {
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
                        }
                        visibility = View.VISIBLE
                    }

                    imageContainer.addView(fileIconOverlay)



                    if (index == 1) {
                        val remainingFilesCount = fileSize - 2
                        val plusCountText = "+$remainingFilesCount"

                        // Create the container for the "+N" count text
                        val overlayContainer = FrameLayout(context).apply {
                            // Create rounded background with better contrast
                            background = GradientDrawable().apply {
                                shape = GradientDrawable.RECTANGLE
                                cornerRadius = 16f
                                setColor(Color.parseColor("#80000000"))
                            }

                            tag = "overlay_tag"

                            layoutParams = FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.WRAP_CONTENT,
                                FrameLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                gravity = Gravity.BOTTOM or Gravity.END
                                marginEnd = dpToPx(context, 8)
                                bottomMargin = dpToPx(context, 8)
                            }

                            setPadding(
                                dpToPx(context, 16),
                                dpToPx(context, 8),
                                dpToPx(context, 16),
                                dpToPx(context, 8)
                            )
                        }

                        val textView = TextView(context).apply {
                            text = plusCountText
                            setTextColor(Color.WHITE)
                            textSize = 16f // Slightly smaller for better proportion
                            gravity = Gravity.CENTER
                            typeface = Typeface.DEFAULT_BOLD

                            // Add subtle text shadow for better visibility
                            setShadowLayer(4f, 0f, 2f, Color.BLACK)
                        }

                        overlayContainer.addView(textView)
                        imageContainer.addView(overlayContainer)
                    }



                    // Load the corresponding thumbnail with rounded corners
                    val thumbnailUrl = originalPost.thumbnail.getOrNull(index)?.thumbnailUrl
                    if (!thumbnailUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(thumbnailUrl)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .transform(RoundedCorners(cornerRadius.toInt()))
                            .into(imageView)
                    } else {
                        imageView.setImageResource(getDocumentPlaceholder(firstFile))
                    }

                    horizontalLayout.addView(imageContainer)
                }

                mixedFilesCardView.addView(horizontalLayout)
            }
        }
    }


    @SuppressLint("RestrictedApi")
    private fun showRepostCombinationOfMultiplesMedia(originalPost: Post, firstFile: File) {

        // Extension function for dp to px conversion
        fun Int.dpToPx(): Int {
            return (this * resources.displayMetrics.density).toInt()
        }

        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val spaceBetweenRows = 4.dpToPx()
        val cardHeight = 300.dpToPx()

        val fileSize = originalPost.files.size

        // Clear any existing views in container
        containerLayout?.removeAllViews()

        // Create and configure views for each file (up to 4)
        val maxDisplayFiles = minOf(fileSize, 4)

        for (position in 0 until maxDisplayFiles) {

            // Create individual card view for each file
            val cardView = MaterialCardView(requireContext()).apply {
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setContentPadding(0, 0, 0, 0)
            }

            // Create frame layout to hold image and overlays
            val frameLayout = FrameLayout(requireContext())

            // Create main image view
            val mainImageView = ImageView(requireContext()).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                id = View.generateViewId() // For finding the view later if needed
            }

            // Create overlay image view (for +N more overlay)
            val overlayImageView = ImageView(requireContext()).apply {
                visibility = View.GONE
                setBackgroundColor(Color.parseColor("#80000000"))
            }

            // Create count text for +N more
            val countText = TextView(requireContext()).apply {
                visibility = View.GONE
                textSize = 32f
                setTextColor(Color.WHITE)
                setTypeface(null, Typeface.NORMAL)
                gravity = Gravity.CENTER
            }

            // Create file type icon
            val fileIcon = ImageView(requireContext()).apply {
                visibility = View.GONE
                scaleType = ImageView.ScaleType.CENTER
            }

            // Create play button for videos/audio
            val playBtn = ImageView(requireContext()).apply {
                visibility = View.GONE
                setImageResource(R.drawable.play_svgrepo_com_white)
                scaleType = ImageView.ScaleType.CENTER
            }

            // Create video feed image view (for video thumbnails)
            val feedVideoImageView = ImageView(requireContext()).apply {
                visibility = View.GONE
                scaleType = ImageView.ScaleType.CENTER_CROP
            }

            // Create video duration text
            val videoDurationText = TextView(requireContext()).apply {
                visibility = View.VISIBLE
                setTextColor(Color.WHITE)
                textSize = 12f
                setPadding(8.dpToPx(), 4.dpToPx(), 8.dpToPx(), 4.dpToPx())
                // Create rounded background
                val background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 8f
                    setColor(Color.parseColor("#80000000"))
                }
                setBackground(background)
            }

            // Get file data for current position
            val fileIdToFind = originalPost.fileIds[position]
            val file = originalPost.files.find { it.fileId == fileIdToFind }
            val fileUrl = file?.url ?: originalPost.files.getOrNull(position)?.url ?: ""
            val mimeType = originalPost.fileTypes.getOrNull(position)?.fileType ?: ""
            val durationItem = originalPost.duration?.find { it.fileId == fileIdToFind }

            // Set video duration
            videoDurationText.text = durationItem?.duration

            // Set default visibility for media controls
            playBtn.visibility = View.GONE
            feedVideoImageView.visibility = View.GONE
            videoDurationText.visibility = View.VISIBLE
            overlayImageView.visibility = View.GONE
            countText.visibility = View.GONE

            // Handle different file types - matching the original logic exactly
            when {
                mimeType.startsWith("image") -> {
                    loadImage(fileUrl, mainImageView)
                    fileIcon.visibility = View.GONE
                }

                mimeType.startsWith("video") -> {
                    loadVideoThumbnail(fileUrl, mainImageView)
                    fileIcon.visibility = View.VISIBLE
                    playBtn.visibility = View.VISIBLE
                    feedVideoImageView.visibility = View.VISIBLE
                }

                mimeType.startsWith("audio") -> {
                    fileIcon.setImageResource(R.drawable.ic_audio)
                    fileIcon.visibility = View.VISIBLE
                    playBtn.visibility = View.VISIBLE
                }

                mimeType.contains("pdf") || mimeType.contains("docx") ||
                        mimeType.contains("pptx") || mimeType.contains("xlsx") ||
                        mimeType.contains("ppt") || mimeType.contains("xls") ||
                        mimeType.contains("txt") || mimeType.contains("rtf") ||
                        mimeType.contains("odt") || mimeType.contains("csv") -> {

                    // Load the first page (thumbnail) of the document
                    val thumbnail = originalPost.thumbnail.find { it.fileId == fileIdToFind }
                    mainImageView.scaleType = ImageView.ScaleType.FIT_XY

                    if (thumbnail != null) {
                        Glide.with(this)
                            .load(thumbnail.thumbnailUrl)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(mainImageView)
                    }

                    fileIcon.setImageResource(
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
                    fileIcon.visibility = View.VISIBLE
                    mainImageView.visibility = View.VISIBLE
                }

                else -> {
                    mainImageView.setImageResource(R.drawable.feed_mixed_image_view_rounded_corners)
                    fileIcon.visibility = View.GONE
                }
            }

            // Layout positioning based on file count and position - exactly matching original logic
            val layoutParams = cardView.layoutParams as ViewGroup.MarginLayoutParams

            when {
                fileSize == 2 -> {
                    // Set layout width to half of screen minus half the margin to prevent overflow
                    layoutParams.width = screenWidth / 2
                    layoutParams.height = cardHeight
                    layoutParams.topMargin = 0
                    layoutParams.bottomMargin = 0

                    when (position) {
                        0 -> {
                            // First item: Touches left screen edge
                            layoutParams.leftMargin = 0
                            layoutParams.rightMargin = spaceBetweenRows / 2
                        }
                        1 -> {
                            // Second item: Touches right screen edge
                            layoutParams.leftMargin = spaceBetweenRows / 2
                            layoutParams.rightMargin = 0
                        }
                    }
                }

                fileSize == 3 -> {
                    if (position == 0) {
                        // Full-width item at top
                        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                        layoutParams.height = cardHeight
                        layoutParams.setMargins(0, 0, 0, spaceBetweenRows) // Space below it
                    } else {
                        // Items 1 and 2 in a row
                        layoutParams.width = screenWidth / 2
                        layoutParams.height = cardHeight
                        layoutParams.topMargin = spaceBetweenRows
                        layoutParams.bottomMargin = 0

                        when (position) {
                            1 -> {
                                // Left item: Touch left screen edge
                                layoutParams.leftMargin = 0
                                layoutParams.rightMargin = spaceBetweenRows / 2
                            }
                            2 -> {
                                // Right item: Touch right screen edge
                                layoutParams.leftMargin = spaceBetweenRows / 2
                                layoutParams.rightMargin = 0
                            }
                        }
                    }
                }

                fileSize == 4 -> {
                    layoutParams.width = screenWidth / 2
                    layoutParams.height = cardHeight

                    val isLeftColumn = (position % 2 == 0)

                    // Horizontal margins: ensure left and right items touch screen edges
                    layoutParams.leftMargin = if (isLeftColumn) 0 else spaceBetweenRows / 2
                    layoutParams.rightMargin = if (isLeftColumn) spaceBetweenRows / 2 else 0

                    // Vertical spacing between rows
                    layoutParams.topMargin = if (position < 2) 0 else spaceBetweenRows
                    layoutParams.bottomMargin = 0
                }

                fileSize > 4 -> {
                    if (position == 3) {
                        overlayImageView.visibility = View.VISIBLE
                        countText.visibility = View.VISIBLE

                        // Set the "+N" text
                        countText.text = "+${fileSize - 4}"
                        countText.textSize = 32f
                        countText.setTextColor(Color.WHITE)
                        countText.setTypeface(null, Typeface.NORMAL)
                        countText.setPadding(12.dpToPx(), 4.dpToPx(), 12.dpToPx(), 4.dpToPx())

                        // Create rounded dimmed background
                        val background = GradientDrawable().apply {
                            shape = GradientDrawable.RECTANGLE
                            cornerRadius = 16f
                            setColor(Color.parseColor("#80000000"))
                        }
                        countText.background = background
                    } else {
                        overlayImageView.visibility = View.GONE
                        countText.visibility = View.GONE
                    }

                    // Ensure 2-column layout touches screen edges
                    layoutParams.width = screenWidth / 2
                    layoutParams.height = cardHeight

                    val isLeftColumn = (position % 2 == 0)

                    // Horizontal margins: ensure left and right items touch screen edges
                    layoutParams.leftMargin = if (isLeftColumn) 0 else spaceBetweenRows / 2
                    layoutParams.rightMargin = if (isLeftColumn) spaceBetweenRows / 2 else 0

                    // Vertical spacing between rows
                    layoutParams.topMargin = if (position < 2) 0 else spaceBetweenRows
                    layoutParams.bottomMargin = 0
                }
            }

            cardView.layoutParams = layoutParams

            // Set up frame layout params for child views
            mainImageView.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )

            overlayImageView.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )

            // Position count text at bottom-right with proper margins
            val marginInDp = 8
            val marginInPx = marginInDp.dpToPx()
            val countParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.END
                marginEnd = marginInPx
                bottomMargin = marginInPx
            }
            countText.layoutParams = countParams

            // Position file icon at center
            fileIcon.layoutParams = FrameLayout.LayoutParams(
                48.dpToPx(),
                48.dpToPx(),
                Gravity.CENTER
            )

            // Position play button at center
            playBtn.layoutParams = FrameLayout.LayoutParams(
                48.dpToPx(),
                48.dpToPx(),
                Gravity.CENTER
            )

            // Position video feed image view
            feedVideoImageView.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )

            // Position duration text at bottom-right
            val durationParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.END
                marginEnd = 8.dpToPx()
                bottomMargin = 8.dpToPx()
            }
            videoDurationText.layoutParams = durationParams

            // Add views to frame layout in proper order
            frameLayout.addView(mainImageView)
            frameLayout.addView(feedVideoImageView)
            frameLayout.addView(overlayImageView)
            frameLayout.addView(fileIcon)
            frameLayout.addView(playBtn)
            frameLayout.addView(videoDurationText)
            frameLayout.addView(countText)

            // Add frame layout to card view
            cardView.addView(frameLayout)

            // Add card view to container
            containerLayout?.addView(cardView)
        }
    }

    private fun loadVideoThumbnail(url: String, imageView: ImageView) {
        Glide.with(this)
            .asBitmap()
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imageView)
    }


    private fun hideAllRepostMediaViews() {
        mixedFilesCardViews.visibility = View.GONE
        multipleAudiosContainers.visibility = View.GONE
        recyclerViews.visibility = View.GONE
    }

    private fun hideAllOriginalMediaViews() {
        mixedFilesCardView.visibility = View.GONE
        multipleAudiosContainer.visibility = View.GONE
        recyclerView.visibility = View.GONE
    }



    override fun onDestroyView() {
        super.onDestroyView()
        onBackPressedCallback.remove()
    }




    // UI update methods
    private fun updateLikeUI(isLiked: Boolean) {
        like.setImageResource(
            if (isLiked) R.drawable.filled_favorite_like
            else R.drawable.favorite_svgrepo_com
        )
    }

    private fun updateFavoriteUI(isFavorited: Boolean) {
        fav.setImageResource(
            if (isFavorited) R.drawable.filled_favorite
            else R.drawable.favorite_black
        )
    }



    // Helper methods
    private fun isPostLiked() = false
    private fun isPostBookmarked() = originalPost?.bookmarks?.isNotEmpty() ?: false

    private fun showRetweetOptions() {
        originalPost?.let { post ->
            val currentRepostCount = repostCount.text.toString().toIntOrNull() ?: post.repostCount
            val newRepostCount =
                if (post.isReposted) currentRepostCount - 1 else currentRepostCount + 1

            repostCount.text = formatCount(newRepostCount)
            showToast(if (!post.isReposted) "Reposted!" else "Repost removed")
        }
    }

    private fun sharePost() {
        currentPost?.let { post ->
            val postId = if (post.originalPost.isNotEmpty()) {
                post.originalPost[0]._id
            } else {
                post._id
            }

            val shareText = buildString {
                append(post.content)
                append("\n\nhttps://circuitSocial.app/post/$postId")
                val tags = post.tags.filterNotNull().joinToString(" ") { "#$it" }
                if (tags.isNotEmpty()) append("\n\n$tags")
            }

            startActivity(
                Intent.createChooser(
                    Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    },
                    "Share Post"
                )
            )
        }
    }

    private fun formatDateTime(dateTimeString: String): String {
        return try {
            val inputFormat = SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()
            )
            val outputFormat = SimpleDateFormat(
                "MMM dd, yyyy • hh:mm a", Locale.getDefault()
            )
            outputFormat.format(
                inputFormat.parse(dateTimeString) ?: dateTimeString
            )
        } catch (e: Exception) {
            dateTimeString
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
        return TODO("Provide the return value")
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }




}