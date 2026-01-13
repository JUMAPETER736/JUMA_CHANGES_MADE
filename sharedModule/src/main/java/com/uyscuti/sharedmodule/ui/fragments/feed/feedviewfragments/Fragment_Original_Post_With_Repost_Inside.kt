package com.uyscuti.sharedmodule.ui.fragments.feed.feedviewfragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
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
import com.uyscuti.social.network.api.response.allFeedRepostsPost.OriginalPost
import com.uyscuti.social.network.api.response.allFeedRepostsPost.Post
import java.text.SimpleDateFormat
import java.util.*
import androidx.activity.OnBackPressedCallback
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.util.UnstableApi
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.uyscuti.social.network.api.response.getrepostsPostsoriginal.File
import kotlin.collections.isNotEmpty
import com.uyscuti.social.network.api.response.getfeedandresposts.Thumbnail
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.card.MaterialCardView
import com.uyscuti.sharedmodule.databinding.FragmentOriginalPostWithRepostInsideBinding
import com.uyscuti.sharedmodule.model.ShowAppBar
import com.uyscuti.sharedmodule.model.ShowBottomNav
import com.uyscuti.sharedmodule.ui.fragments.feed.feedviewfragments.editRepost.Fragment_Edit_Post_To_Repost
import com.uyscuti.sharedmodule.ui.fragments.feed.feedviewfragments.feedRepost.PostItem
import com.uyscuti.sharedmodule.ui.fragments.feed.feedviewfragments.feedRepost.Tapped_Files_In_The_Container_View_Fragment
import com.uyscuti.social.network.api.response.feed.getallfeed.more_feed_data_classes.AudioDuration
import com.uyscuti.social.network.api.response.feed.getallfeed.more_feed_data_classes.Duration
import org.greenrobot.eventbus.EventBus


private const val TAG = "Fragment_Original_Post_With_Repost_Inside"

class Fragment_Original_Post_With_Repost_Inside() : Fragment() {

    // Views from header_toolbar.xml
    private lateinit var backButton: ImageButton
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

    // Data
    private var originalPost: OriginalPost? = null

    private var post: Post? = null

    private var isFollowing = false

    private var _binding: FragmentOriginalPostWithRepostInsideBinding? = null
    private val binding get() = _binding!!

    private lateinit var onBackPressedCallback: OnBackPressedCallback

    private lateinit var fileTypeIcon: ImageView

    private lateinit var originalImageContainer: LinearLayout

    private var containerLayout: LinearLayout? = null
    private var imageView: ImageView? = null
    private var materialCardView: MaterialCardView? = null



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
                        postId = fileIds.getOrNull(index) ?: "file_$index",

                        userId = null,
                        username = null,
                        authorName = null,
                        avatarUrl = null,
                        isVerified = false,

                        audioUrl = file.url,
                        audioThumbnailUrl = null,
                        videoUrl = file.url,
                        videoThumbnailUrl = null,

                        data = "Post data for file $index",
                        files = arrayListOf(file.url),
                        fileType = ""
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
                // Navigate back to previous fragment/activity
                if (parentFragmentManager.backStackEntryCount > 0) {
                    parentFragmentManager.popBackStack()
                } else {
                    requireActivity().finish()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )

        initializeViews(view)
        setupClickListeners()
        setupRecyclerViews()

        post = arguments?.getSerializable(ARG_ORIGINAL_POST) as? Post
        Log.d("populateViews", "post: $post")
        originalPost?.let { populateViews(it) }

        post?.let { populatePostData(it) }


    }

    private fun initializeViews(view: View) {

        // Header Views
        backButton = view.findViewById(R.id.backButton)
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

        // Media Views - CORRECTED
        mixedFilesCardViews = view.findViewById(R.id.mixedFilesCardViews)
        originalFeedImages = view.findViewById(R.id.originalFeedImages)
        multipleMediaContainer = view.findViewById(R.id.multipleMediaContainer)
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
        likeSection = view.findViewById(R.id.likeSection)
        like = view.findViewById(R.id.like)
        likesCount = view.findViewById(R.id.likesCount)
        commentSection = view.findViewById(R.id.commentSection)
        comment = view.findViewById(R.id.comment)
        commentCount = view.findViewById(R.id.commentCount)
        favoriteSection = view.findViewById(R.id.favoriteSection)
        fav = view.findViewById(R.id.fav)
        favCount = view.findViewById(R.id.favCount)
        retweetSection = view.findViewById(R.id.retweetSection)
        reFeed = view.findViewById(R.id.reFeed)
        repostCount = view.findViewById(R.id.repostCount)
        shareSection = view.findViewById(R.id.shareSection)
        share = view.findViewById(R.id.share)
        shareCount = view.findViewById(R.id.shareCount)

    }

    private fun setupClickListeners() {
        // Header click listeners
        backButton.setOnClickListener {
            Log.d(TAG, "Cancel button clicked")
            cleanupAndGoBack()
        }

        headerMenuButton.setOnClickListener {
            handleMenuButtonClick()
        }

        // User interaction click listeners
        followButton.setOnClickListener {
            handleFollowButtonClick()
        }

        // Post click listeners
        repostContainer.setOnClickListener { handleMainPostClick() }
        quotedPostCard.setOnClickListener { handleOriginalPostClick() }

        // Action button click listeners
        likeSection.setOnClickListener { handleLikeClick() }
        commentSection.setOnClickListener { handleCommentClick() }
        favoriteSection.setOnClickListener { handleFavoriteClick() }
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
                        type = file.type,
                        mimeType = file.mimeType,
                        fileType = file.fileType
                    ).apply {
                        url = file.url
                        mimeType = file.mimeType
                    }
                }
                val fileIds = currentPost.files.map { it ?: "unknown_id" }
                navigateToTappedFilesFragment(requireContext(),
                    0, files, fileIds as List<String>)
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
                        type = file.type,
                        mimeType = file.mimeType,
                        fileType = file.fileType
                    )
                }

                val fileIds = originalPost.files.map { it.fileId ?: "unknown_id" }
                navigateToTappedFilesFragment(requireContext(), 0, files, fileIds)
            }
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
                        type = file.type,
                        mimeType = file.mimeType,
                        fileType = file.fileType
                    ).apply {
                        url = file.url
                        mimeType = file.mimeType
                    }
                }
                val fileIds = currentPost.files.map { it ?: "unknown_id" }
                navigateToTappedFilesFragment(requireContext(), 0, files, fileIds as List<String>)
            }
        }
    }


    private fun setupRecyclerViews() {
        recyclerViews.layoutManager = GridLayoutManager(requireContext(), 3)
        recyclerViews.isNestedScrollingEnabled = false

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        recyclerView.isNestedScrollingEnabled = false
    }



// MAIN POST POPULATION METHODS...

    private fun populateViews(post: OriginalPost) {
        Log.d("populateViews", "post: $post")

        // Set header
        headerTitle.text = "Post"

        populateReposterInfo(post)
        populatePostContent(post)
        populateOriginalAuthorInfo(post)
        populateInteractionData(post)

    }

    private fun handleOriginalMediaClick() {
        post?.originalPost?.firstOrNull()?.let { originalPost ->
            if (originalPost.files.isNotEmpty()) {
                val files = originalPost.files.map { file ->
                    File(
                        _id = file._id?.ifBlank { "unknown_id" } ?: "unknown_id",
                        fileId = file.fileId?.ifBlank { "no_file_id" } ?: "no_file_id",
                        localPath = file.localPath?.ifBlank { "" } ?: "",
                        url = file.url?.ifBlank { "" } ?: "",
                        type = file.type?.ifBlank { "unknown_type" } ?: "unknown_type",
                        mimeType = file.mimeType?.ifBlank { "" } ?: "", // Fixed: handle null mimeType
                        fileType = ""
                    )
                }

                val fileIds = files.map { it.fileId }
                navigateToTappedFilesFragment(requireContext(), 0, files, fileIds)
            }
        }
    }

    fun populatePostData(post: Post) {
        // Set header
        headerTitle.text = "Post"

        // Populate reposter information
        populateReposterInfo(post)

        // Populate repost content
        populateRepostContent(post)

        // Handle repost media files
        handleRepostMediaFiles(post)

        // Handle original post data if available
        if (post.originalPost.isNotEmpty()) {
            val originalPost = post.originalPost[0]
            populateOriginalPostData(originalPost)
        }
    }


// USER INFORMATION POPULATION METHODS...


    private fun populateReposterInfo(post: OriginalPost) {
        if (post.originalPostReposter.isNotEmpty()) {
            val reposter = post.originalPostReposter[0]
            repostedUserName.text = reposter.username ?: "Unknown User"
            tvUserHandle.text = "@${reposter.username ?: "unknown"}"

            reposter.avatar?.let { profileUrl ->
                loadProfileImage(profileUrl.toString(), userProfileImage)
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

    private fun populateOriginalAuthorInfo(post: OriginalPost) {
        if (post.author.isNotEmpty()) {
            val originalAuthor = post.author[0]
            originalPosterName.text = originalAuthor.username ?: "Unknown User"
            tvQuotedUserHandle.text = "@${originalAuthor.username ?: "unknown"}"

            originalAuthor.avatar?.let { profileUrl ->
                loadProfileImage(profileUrl.toString(), originalPosterProfileImage)
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

    private fun populateOriginalPostData(originalPost: OriginalPost) {

        Log.d("populateViews", "populateOriginalPostData, $originalPost")
        // Original post author info
        if (originalPost.author.isNotEmpty()) {
            val originalAuthor = originalPost.author[0]
            originalPosterName.text = originalAuthor.username ?: "Unknown User"
            tvQuotedUserHandle.text = "@${originalAuthor.username ?: "unknown"}"

            originalAuthor.avatar?.let { profileUrl ->
                loadProfileImage(profileUrl.toString(), originalPosterProfileImage)
            }
        }

        // Original post content
        originalPostText.text = originalPost.content
        dateTime.text = formatDateTime(originalPost.createdAt)

        // Original post tags
        val originalTagsText = originalPost.tags.filterNotNull().joinToString(" ") { "#$it" }
        tvQuotedHashtags.text = originalTagsText
        tvQuotedHashtags.visibility = if (originalTagsText.isNotEmpty()) View.VISIBLE else View.GONE

        // Interaction counts
        populateOriginalPostInteractionData(originalPost)

        // Handle original post media files
        handleOriginalPostMediaFiles(originalPost)
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
        updateLikeUI(originalPost.isLikedCount)
        updateFavoriteUI(originalPost.bookmarks.isNotEmpty())
        updateFollowButtonUI()

        reFeed.setImageResource(
            if (originalPost.isReposted) R.drawable.retweet
            else R.drawable.retweet
        )
    }

// MEDIA HANDLING METHODS

    private fun handleThumbnails(
        thumbnails: List<Thumbnail>,
        imageView: ImageView
    ) {
        if (thumbnails.isNotEmpty()) {
            val thumbnailUrl = thumbnails.firstOrNull()?.thumbnailUrl

            if (thumbnailUrl?.isNotEmpty() == true && mixedFilesCardView.visibility == View.VISIBLE) {
                loadImage(thumbnailUrl, imageView)
            }
        }
    }


    private fun handleRepostMediaFiles(post: Post) {
        if (post.files.isNotEmpty()) {
            val firstFile = post.files[0]

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

                firstFile.mimeType?.startsWith("mixed_files") == true -> {
                    showRepostCombinationOfMultiplesMedia(post, firstFile)
                }

                isDocumentFile(firstFile) -> {
                    showRepostDocumentMedia(post, firstFile)
                }

                else -> {
                    showRepostCombinationOfMultiplesMedia(post, firstFile)
                }
            }
        } else {
            hideAllRepostMediaViews()
        }

        handleThumbnails(post.thumbnail, ivQuotedPostImage)
    }

    private fun handleOriginalPostMediaFiles(originalPost: OriginalPost) {
        if (originalPost.files.isNotEmpty()) {
            val firstFile = originalPost.files[0]

            when {
                isImageFile(firstFile) -> {
                    showOriginalImageMedia(originalPost, firstFile)
                }

                isVideoFile(firstFile) -> {
                    showOriginalVideoMedia(originalPost, firstFile)
                }

                isAudioFile(firstFile) -> {
                    showOriginalAudioMedia(originalPost, firstFile)
                }

                isDocumentFile(firstFile) -> {
                    showOriginalDocumentMedia(originalPost, firstFile)
                }

                else -> {
                    hideAllOriginalMediaViews()
                }
            }
        } else {
            hideAllOriginalMediaViews()
        }

        handleThumbnails(originalPost.thumbnail, ivQuotedPostImage)
    }

    private fun updateInteractionStates(post: OriginalPost) {
        // Update like button state
        updateLikeUI(post.isLikedCount)

        // Update bookmark/favorite button state
        updateFavoriteUI(post.bookmarks.isNotEmpty())

        // Update follow button state
        updateFollowButtonUI()

        // Update repost button state
        reFeed.setImageResource(
            if (post.isReposted) R.drawable.retweet
            else R.drawable.retweet
        )

        // Update comment button state (if needed)
        // commentButton.isEnabled = true

        // Update share button state (if needed)
        // shareButton.isEnabled = true
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
            val audioFile = files[index]
            // TODO: Implement audio player logic
            // playAudio(audioFile.url)
        }
    }

    // Convenience methods for different post types
    private fun showOriginalAudioMedia(originalPost: OriginalPost, firstFile: File) {
        showAudioMedia(
            files = originalPost.files,
            containerView = binding.multipleAudiosContainer,
            durationData = originalPost.duration ?: emptyList(),
            thumbnailData = originalPost.thumbnail,
            isRepost = false,
            post = originalPost
        )
    }

    private fun showRepostAudioMedia(post: Post, firstFile: File) {
        showAudioMedia(
            files = post.files,
            containerView = binding.multipleAudiosContainers,
            durationData = post.duration ?: emptyList(),
            thumbnailData = post.thumbnail,
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
                thumbnailData.find { it.fileId == fileId }?.thumbnailUrl
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
            val videoFile = files[index]
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
            .replace(R.id.frame_layout, fragment)
            .addToBackStack("video_detail")
            .commit()
    }

    // Convenience methods for different post types
    private fun showOriginalVideoMedia(originalPost: OriginalPost, firstFile: File) {
        showVideoMedia(
            files = originalPost.files,
            containerView = mixedFilesCardView,
            thumbnailData = originalPost.thumbnail,
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
            thumbnailData = post.thumbnail,
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
            // Try to generate thumbnail from document URL if possible
            originalFeedImages.setImageResource(getDocumentPlaceholder(firstFile))
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

        fun dpToPx(context: Context, dp: Float): Int {
            return (dp * context.resources.displayMetrics.density).toInt()
        }

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

            // Set click listener for the card view
//            cardView.setOnClickListener {
//                onMultipleFilesClickListener?.multipleFileClickListener(
//                    position,
//                    originalPost.files,
//                    originalPost.fileIds as List<String>
//                )
//            }

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

    private fun hideAllMediaViews() {
        mixedFilesCardView.visibility = View.GONE
        multipleAudiosContainer.visibility = View.GONE
        recyclerView.visibility = View.GONE
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

    private fun handleMenuButtonClick() = showToast("Options menu")
    private fun handleFollowButtonClick() = toggleFollow()
    private fun handleMainPostClick() = showToast("Opening full post ...")
    private fun handleOriginalPostClick() = showToast("Opening original post...")
    private fun handleLikeClick() = toggleLike()
    private fun handleCommentClick() = showToast("Opening comments...")
    private fun handleFavoriteClick() = toggleFavorite()
    private fun handleRetweetClick() = showRetweetOptions()
    private fun handleShareClick() = sharePost()


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

    private fun updateFollowButtonUI() {
        followButton.text = if (isFollowing) "Following" else "Follow"
        followButton.setBackgroundResource(
            if (isFollowing) R.drawable.shorts_following_button
            else R.drawable.shorts_following_button
        )
    }

    // Helper methods
    private fun isPostLiked() = false
    private fun isPostBookmarked() = originalPost?.bookmarks?.isNotEmpty() ?: false

    private fun toggleLike() {
        originalPost?.let { post ->
            val currentLikeCount = likesCount.text.toString().toIntOrNull() ?: post.likeCount
            val newLikeCount = if (isPostLiked()) currentLikeCount - 1 else currentLikeCount + 1

            updateLikeUI(newLikeCount > currentLikeCount)
            likesCount.text = formatCount(newLikeCount)
            showToast(if (newLikeCount > currentLikeCount) "Liked!" else "Like removed")
        }
    }

    private fun toggleFavorite() {
        originalPost?.let { post ->
            val currentBookmarkCount = favCount.text.toString().toIntOrNull() ?: post.bookmarkCount
            val newBookmarkCount =
                if (isPostBookmarked()) currentBookmarkCount - 1 else currentBookmarkCount + 1

            updateFavoriteUI(newBookmarkCount > currentBookmarkCount)
            favCount.text = formatCount(newBookmarkCount)
            showToast(
                if (
                    newBookmarkCount > currentBookmarkCount) "Added to favorites!" else "Removed from favorites"
            )
        }
    }

    private fun toggleFollow() {
        isFollowing = !isFollowing
        updateFollowButtonUI()

        originalPost?.let { post ->
            if (post.originalPostReposter.isNotEmpty()) {
                val reposterName = post.originalPostReposter[0].username ?: "User"
                showToast(
                    if (isFollowing) "Now following $reposterName"
                    else "Unfollowed $reposterName"
                )
            }
        }
    }

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
        originalPost?.let { post ->
            val shareText = buildString {
                append(post.content)
                if (post.url.isNotEmpty()) append("\n\n${post.url}")
                val tags = post.tags.filterNotNull().joinToString(" ") { "#$it" }
                if (tags.isNotEmpty()) append("\n\n$tags")
            }

            startActivity(
                Intent.createChooser(
                    Intent().apply {
                        setAction(Intent.ACTION_SEND)
                        setType("text/plain")
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

    companion object {
        private const val ARG_ORIGINAL_POST = "original_post"

        fun newInstance(data: com.uyscuti.social.network.api.response.posts.Post): Fragment_Original_Post_With_Repost_Inside {
            return Fragment_Original_Post_With_Repost_Inside().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_ORIGINAL_POST, data)
                }
            }
        }

    }


}