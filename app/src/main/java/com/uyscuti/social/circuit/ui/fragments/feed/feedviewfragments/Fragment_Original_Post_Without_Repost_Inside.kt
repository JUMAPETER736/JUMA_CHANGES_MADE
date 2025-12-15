package com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uyscuti.social.circuit.R
import com.uyscuti.social.network.api.response.allFeedRepostsPost.OriginalPost
import com.uyscuti.social.network.api.response.allFeedRepostsPost.Post
import java.text.SimpleDateFormat
import java.util.*
import androidx.activity.OnBackPressedCallback
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.util.UnstableApi
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.feedRepost.Tapped_Files_In_The_Container_View_Fragment
import com.uyscuti.social.network.api.response.getrepostsPostsoriginal.File
import com.uyscuti.social.network.api.response.getfeedandresposts.Thumbnail
import com.google.android.material.card.MaterialCardView
import com.uyscuti.social.circuit.MainActivity
import com.uyscuti.social.network.api.response.feed.getallfeed.more_feed_data_classes.Duration
import com.uyscuti.social.network.api.response.getrepostsPostsoriginal.FileType

private const val TAG = "Fragment_Original_Post_Without_Repost_Inside"

class Fragment_Original_Post_Without_Repost_Inside : Fragment() {

    // Views from header_toolbar.xml
    private lateinit var cancelButton: ImageButton
    private lateinit var headerTitle: TextView
    private lateinit var headerMenuButton: ImageButton

    // Views from original_post_section.xml
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
    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private var isNavigating = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.fragment_original_post_without_repost_inside,
            container,
            false
        )
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupClickListeners()
        setupRecyclerViews()

        (activity as? MainActivity)?.hideAppBar()
        (activity as? MainActivity)?.hideBottomNavigation()

        post = arguments?.getSerializable(ARG_ORIGINAL_POST) as? Post
        Log.d(TAG, "post: $post")
        post?.let { populatePostData(it) }
    }

    private fun initializeViews(view: View) {
        // Header Views
        cancelButton = view.findViewById(R.id.cancelButton)
        headerTitle = view.findViewById(R.id.headerTitle)
        headerMenuButton = view.findViewById(R.id.headerMenuButton)

        // Original Post (Quoted Post) Views
        quotedPostCard = view.findViewById(R.id.quotedPostCard)
        originalPostContainer = view.findViewById(R.id.originalPostContainer)
        originalPosterProfileImage = view.findViewById(R.id.originalPosterProfileImage)
        originalPosterName = view.findViewById(R.id.originalPosterName)
        tvQuotedUserHandle = view.findViewById(R.id.tvQuotedUserHandle)
        dateTime = view.findViewById(R.id.date_time)
        originalPostText = view.findViewById(R.id.originalPostText)
        tvQuotedHashtags = view.findViewById(R.id.tvQuotedHashtags)
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

    @OptIn(UnstableApi::class)
    private fun setupClickListeners() {
        // Header click listeners
        cancelButton.setOnClickListener {
            if (isNavigating) return@setOnClickListener
            isNavigating = true
            try {
                cleanupResources()
                restoreSystemBars()
                (activity as? MainActivity)?.showAppBar()
                (activity as? MainActivity)?.showBottomNavigation()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            } catch (e: Exception) {
                Log.e(TAG, "Error during back navigation", e)
                try {
                    parentFragmentManager.popBackStack()
                } catch (fallbackException: Exception) {
                    Log.e(TAG, "Fallback navigation also failed", fallbackException)
                }
            } finally {
                Handler(Looper.getMainLooper()).postDelayed({
                    if (isAdded) isNavigating = false
                }, 100)
            }
        }

        headerMenuButton.setOnClickListener { handleMenuButtonClick() }

        // Action button click listeners
        likeSection.setOnClickListener { handleLikeClick() }
        commentSection.setOnClickListener { handleCommentClick() }
        favoriteSection.setOnClickListener { handleFavoriteClick() }
        retweetSection.setOnClickListener { handleRetweetClick() }
        shareSection.setOnClickListener { handleShareClick() }

        // Media click listeners
        mixedFilesCardView.setOnClickListener { handleOriginalMediaClick() }
        originalFeedImage.setOnClickListener { handleOriginalFileClick() }

        // Original post container click listener
        originalPostContainer.setOnClickListener {
            Log.d(TAG, "Original Post container clicked!")
            navigateToFragment_Original_Post_Without_Repost_Inside()
        }
        quotedPostCard.setOnClickListener {
            Log.d(TAG, "Quoted Post Card clicked!")
            navigateToFragment_Original_Post_Without_Repost_Inside()
        }
    }

    private fun navigateToFragment_Original_Post_Without_Repost_Inside() {
        if (isNavigating) return
        isNavigating = true
        try {
            val fragment = Fragment_Original_Post_Without_Repost_Inside()
            val bundle = Bundle().apply {
                post?.let { putSerializable("ARG_POST", it) }
            }
            fragment.arguments = bundle
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .replace(R.id.frame_layout, fragment)
                .addToBackStack("fragment_original_post_without_repost_inside")
                .commit()
            Log.d(TAG, "Navigation to Fragment_Original_Post_Without_Repost_Inside initiated")
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to Fragment_Original_Post_Without_Repost_Inside", e)
        } finally {
            Handler(Looper.getMainLooper()).postDelayed({
                if (isAdded) isNavigating = false
            }, 100)
        }
    }

    private fun navigateToTappedFilesFragment(context: Context, index: Int, files: List<File>, fileIds: List<String>) {
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

    private fun cleanupResources() {
        try {
            Log.d(TAG, "Resources cleaned up successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error during resource cleanup", e)
        }
    }

    private fun restoreSystemBars() {
        try {
            activity?.window?.let { window ->
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    window.setDecorFitsSystemWindows(true)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring system bars", e)
        }
    }

    private fun setupRecyclerViews() {
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        recyclerView.isNestedScrollingEnabled = false
    }

    private fun populatePostData(post: Post) {
        headerTitle.text = "Post"

        // Fix 1: Use proper null safety and list checking
        if (!post.originalPost.isNullOrEmpty()) {
            val repostedPostData = post.originalPost[0]
            populateOriginalPostData(repostedPostData)
        } else {
            // Fix 2: Populate author data from post.author
            if (post.author != null) {
                originalPosterName.text = post.author.firstName ?: post.author.username ?: "Unknown User"
                tvQuotedUserHandle.text = "@${post.author.username ?: "unknown"}"

                // Load profile image
                post.author.account?.avatar?.url?.let { avatarUrl ->
                    loadProfileImage(avatarUrl, originalPosterProfileImage)
                }
            }

            originalPostText.text = post.content
            dateTime.text = formatDateTime(post.createdAt)

            // Fix 4: Handle tags properly with null safety
            val tagsText = post.tags?.filterNotNull()?.joinToString(" ") { "#$it" } ?: ""
            tvQuotedHashtags.text = tagsText
            tvQuotedHashtags.visibility = if (tagsText.isNotEmpty()) View.VISIBLE else View.GONE

            populatePostInteractionData(post)
            handleOriginalPostMediaFiles(post)
        }
    }

    private fun populatePostInteractionData(post: Post) {
        likesCount.text = formatCount(post.likes)
        commentCount.text = formatCount(post.commentCount)
        repostCount.text = formatCount(post.repostCount)
        favCount.text = formatCount(post.bookmarkCount)
        shareCount.text = formatCount(post.shareCount)
        updatePostInteractionStates(post)
    }

    private fun updatePostInteractionStates(post: Post) {
        updateLikeUI(post.isLikedCount)
        updateFavoriteUI(post.isBookmarked)
        reFeed.setImageResource(
            if (post.isReposted) R.drawable.retweet else R.drawable.repeat_svgrepo_com
        )
    }

    private fun populateOriginalPostData(originalPost: OriginalPost) {
        if (originalPost.author.isNotEmpty()) {
            val originalAuthor = originalPost.author[0]
            originalPosterName.text = originalAuthor.firstName ?: originalAuthor.username ?: "Unknown User"
            tvQuotedUserHandle.text = "@${originalAuthor.username ?: "unknown"}"
            originalAuthor.avatar?.let { profileUrl ->
                loadProfileImage(profileUrl.toString(), originalPosterProfileImage)
            }
        }
        originalPostText.text = originalPost.content
        dateTime.text = formatDateTime(originalPost.createdAt)
        val originalTagsText = originalPost.tags.filterNotNull().joinToString(" ") { "#$it" }
        tvQuotedHashtags.text = originalTagsText
        tvQuotedHashtags.visibility = if (originalTagsText.isNotEmpty()) View.VISIBLE else View.GONE
        populateOriginalPostInteractionData(originalPost)
        handleOriginalPostMediaFiles(originalPost)
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
        reFeed.setImageResource(
            if (originalPost.isReposted) R.drawable.retweet else R.drawable.repeat_svgrepo_com
        )
    }


    private fun handleOriginalMediaClick() {
        post?.let { postData ->
            if (postData.files.isNotEmpty()) {
                val files = postData.files.map { file ->
                    File(
                        _id = file._id?.ifBlank { "unknown_id" } ?: "unknown_id",
                        fileId = file.fileId?.ifBlank { "no_file_id" } ?: "no_file_id",
                        localPath = file.localPath?.ifBlank { "" } ?: "",
                        url = file.url?.ifBlank { "" } ?: "",
                        type = file.type?.ifBlank { "unknown_type" } ?: "unknown_type",
                        mimeType = file.mimeType?.ifBlank { "" } ?: "",
                        fileType = ""
                    )
                }
                val fileIds = files.map { it.fileId }
                navigateToTappedFilesFragment(requireContext(), 0, files, fileIds)
            }
        }
    }

    private fun handleOriginalFileClick() {
        post?.let { postData ->
            if (postData.files.isNotEmpty()) {
                val files = postData.files.map { file ->
                    File(
                        _id = file._id?.ifBlank { "unknown_id" } ?: "unknown_id",
                        fileId = file.fileId?.ifBlank { "no_file_id" } ?: "no_file_id",
                        localPath = file.localPath?.ifBlank { "" } ?: "",
                        url = file.url?.ifBlank { "" } ?: "",
                        type = file.type?.ifBlank { "unknown_type" } ?: "unknown_type",
                        mimeType = file.mimeType?.ifBlank { "" } ?: "",
                        fileType = ""
                    )
                }
                val fileIds = files.map { it.fileId }
                navigateToTappedFilesFragment(requireContext(), 0, files, fileIds)
            }
        }
    }

    private fun handleOriginalPostMediaFiles(post: Any) {
        val files: List<File>
        val thumbnail: List<Thumbnail>
        val duration: List<Duration> // Use Duration to match the data model
        when (post) {
            is OriginalPost -> {
                files = post.files
                thumbnail = post.thumbnail
                duration = post.duration
            }
            is Post -> {
                files = post.files
                thumbnail = post.thumbnail
                duration = post.duration
            }
            else -> return
        }
        if (files.isNotEmpty()) {
            val firstFile = files[0]
            when {
                isImageFile(firstFile) -> showOriginalImagesOnly(post, firstFile)
                isVideoFile(firstFile) -> showOriginalVideosOnly(post, firstFile)
                isAudioFile(firstFile) -> showOriginalAudiosOnly(post, firstFile)
                isDocumentFile(firstFile) -> showOriginalDocumentsOnly(post, firstFile)
                isMultipleCombinationFile(firstFile) -> showOriginalCombinationOfMultipleFiles(post, firstFile)
                else -> hideAllOriginalMediaViews()
            }
        } else {
            hideAllOriginalMediaViews()
        }
        handleThumbnails(thumbnail, ivQuotedPostImage)
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

    private fun handleThumbnails(thumbnails: List<Thumbnail>, imageView: ImageView) {
        if (thumbnails.isNotEmpty()) {
            val thumbnailUrl = thumbnails.firstOrNull()?.thumbnailUrl
            if (thumbnailUrl?.isNotEmpty() == true && mixedFilesCardView.visibility == View.VISIBLE) {
                loadImage(thumbnailUrl, imageView)
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private fun isImageFile(file: File, fileTypes: List<FileType>? = null): Boolean {
        val fileType = fileTypes?.find { it.fileId == file.fileId }?.fileType?.lowercase()
        val mimeType = file.mimeType?.lowercase()
        val url = file.url?.lowercase()
        return when {
            fileType == "image" -> true
            mimeType?.startsWith("image") == true -> true
            url?.matches(".*\\.(jpg|jpeg|png|gif|bmp|webp)$".toRegex()) == true -> true
            else -> false
        }
    }

    @SuppressLint("RestrictedApi")
    private fun isAudioFile(file: File, fileTypes: List<FileType>? = null): Boolean {
        val fileType = fileTypes?.find { it.fileId == file.fileId }?.fileType?.lowercase()
        val mimeType = file.mimeType?.lowercase()
        val url = file.url?.lowercase()
        return when {
            fileType == "audio" -> true
            mimeType?.startsWith("audio") == true -> true
            url?.matches(".*\\.(mp3|wav|aac|ogg|flac|m4a|opus|amr|3gp)$".toRegex()) == true -> true
            else -> false
        }
    }

    @SuppressLint("RestrictedApi")
    private fun isVideoFile(file: File, fileTypes: List<FileType>? = null): Boolean {
        val fileType = fileTypes?.find { it.fileId == file.fileId }?.fileType?.lowercase()
        val mimeType = file.mimeType?.lowercase()
        val url = file.url?.lowercase()
        return when {
            fileType == "video" -> true
            mimeType?.startsWith("video") == true -> true
            url?.matches(".*\\.(mp4|avi|mkv|mov|wmv|flv)$".toRegex()) == true -> true
            else -> false
        }
    }

    @SuppressLint("RestrictedApi")
    private fun isDocumentFile(file: File, fileTypes: List<FileType>? = null): Boolean {
        val fileType = fileTypes?.find { it.fileId == file.fileId }?.fileType?.lowercase()
        val mimeType = file.mimeType?.lowercase()
        val url = file.url?.lowercase()
        return when {
            fileType in listOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "rtf", "odt", "ods", "odp", "csv") -> true
            mimeType?.contains("pdf") == true || url?.contains(".pdf") == true -> true
            mimeType?.contains("msword") == true || mimeType?.contains("wordprocessingml") == true || url?.contains(".doc") == true || url?.contains(".docx") == true -> true
            mimeType?.contains("ms-excel") == true || mimeType?.contains("spreadsheetml") == true || url?.contains(".xls") == true || url?.contains(".xlsx") == true -> true
            mimeType?.contains("ms-powerpoint") == true || mimeType?.contains("presentationml") == true || url?.contains(".ppt") == true || url?.contains(".pptx") == true -> true
            mimeType?.contains("text/plain") == true || mimeType?.contains("text/rtf") == true || url?.contains(".txt") == true || url?.contains(".rtf") == true -> true
            mimeType?.contains("opendocument") == true || url?.contains(".odt") == true || url?.contains(".ods") == true || url?.contains(".odp") == true -> true
            else -> false
        }
    }

    @SuppressLint("RestrictedApi")
    private fun isMultipleCombinationFile(file: File, fileTypes: List<FileType>? = null): Boolean {
        return isImageFile(file, fileTypes) || isAudioFile(file, fileTypes) || isVideoFile(file, fileTypes) || isDocumentFile(file, fileTypes)
    }


    private fun calculateActualImageHeight(context: Context, filePath: String): Int {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }

            // Decode only the image bounds to get width & height
            BitmapFactory.decodeFile(filePath, options)

            val screenWidth = context.resources.displayMetrics.widthPixels

            if (options.outWidth > 0 && options.outHeight > 0) {
                // Calculate aspect ratio
                val aspectRatio = options.outHeight.toFloat() / options.outWidth.toFloat()

                // Calculate height based on screen width
                val displayHeight = (screenWidth * aspectRatio).toInt()

                // Make sure height is never zero or negative
                displayHeight.coerceAtLeast(1)
            } else {
                // Fallback height: 75% of screen width
                (screenWidth * 0.75).toInt()
            }
        } catch (e: Exception) {
            // Fallback in case of any decoding error
            (context.resources.displayMetrics.widthPixels * 0.75).toInt()
        }
    }

    private fun createCountTextView(context: Context, extraCount: Int): TextView {
        return TextView(context).apply {
            setTextColor(Color.WHITE)
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.END
                marginEnd = 8.dpToPx(context)
                bottomMargin = 8.dpToPx(context)
            }
            setPadding(16.dpToPx(context), 8.dpToPx(context), 16.dpToPx(context), 8.dpToPx(context))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 8f
                setColor(Color.parseColor("#80000000"))
            }
            visibility = if (extraCount > 0) View.VISIBLE else View.GONE
            text = if (extraCount > 0) "+$extraCount" else ""
        }
    }

    private fun getDocumentPlaceholder(file: File?): Int {
        val mimeType = file?.mimeType?.lowercase()
        val url = file?.url?.lowercase()
        return when {
            mimeType?.contains("pdf") == true || url?.contains(".pdf") == true -> R.drawable.pdf_placeholder
            mimeType?.contains("msword") == true || mimeType?.contains("wordprocessingml") == true || url?.contains(".doc") == true || url?.contains(".docx") == true -> R.drawable.word_placeholder
            mimeType?.contains("ms-excel") == true || mimeType?.contains("spreadsheetml") == true || url?.contains(".xls") == true || url?.contains(".xlsx") == true -> R.drawable.excel_placeholder
            mimeType?.contains("ms-powerpoint") == true || mimeType?.contains("presentationml") == true || url?.contains(".ppt") == true || url?.contains(".pptx") == true -> R.drawable.powerpoint_placeholder
            mimeType?.contains("text") == true || url?.contains(".txt") == true || url?.contains(".rtf") == true -> R.drawable.text_placeholder
            else -> R.drawable.text_placeholder
        }
    }

    private fun hideAllOriginalMediaViews() {
        mixedFilesCardView.visibility = View.GONE
        multipleAudiosContainer.visibility = View.GONE
        recyclerView.visibility = View.GONE
    }

    // Image Media Functions
    private fun showOriginalImagesOnly(post: Any, firstFile: File) {
        val files = when (post) {
            is OriginalPost -> post.files.filter { isImageFile(it) }
            is Post -> post.files.filter { isImageFile(it) }
            else -> emptyList()
        }
        showImageMedia(files, mixedFilesCardView, false, post)
    }

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
        val minHeight = (screenWidth * 0.4).toInt()
        val maxHeight = (screenWidth * 0.6).toInt()
        val standardImageHeight = (minHeight + maxHeight) / 2

        containerView.removeAllViews()
        containerView.visibility = View.VISIBLE
        if (files.isEmpty()) {
            containerView.visibility = View.GONE
            return
        }

        when (files.size) {
            1 -> createSingleImageLayout(context, containerView, files, isRepost, post)
            2 -> createTwoImageLayout(context, containerView, files, standardImageHeight, spaceBetweenRows, isRepost, post)
            3 -> createThreeImageLayout(context, containerView, files, standardImageHeight, margin, spaceBetweenRows, isRepost, post)
            else -> createGridImagesOnly(context, containerView, files, standardImageHeight, screenWidth, spaceBetweenRows, isRepost, post)
        }
    }

    // Audio Media Functions
    private fun showOriginalAudiosOnly(post: Any, firstFile: File) {
        val files = when (post) {
            is OriginalPost -> post.files.filter { isAudioFile(it) }
            is Post -> post.files.filter { isAudioFile(it) }
            else -> emptyList()
        }
        val duration = when (post) {
            is OriginalPost -> post.duration
            is Post -> post.duration
            else -> emptyList()
        }
        val thumbnail = when (post) {
            is OriginalPost -> post.thumbnail
            is Post -> post.thumbnail
            else -> emptyList()
        }
        showAudioMedia(files, multipleAudiosContainer, duration, thumbnail, false, post)
    }

    private fun showAudioMedia(
        files: List<File>,
        containerView: ViewGroup,
        durationData: List<Duration> = emptyList(),
        thumbnailData: List<Thumbnail>? = null,
        isRepost: Boolean = false,
        post: Any? = null
    ) {
        val context = containerView.context
        val screenWidth = context.resources.displayMetrics.widthPixels
        val spaceBetweenCards = 2.dpToPx(context)
        val minHeight = (screenWidth * 0.4).toInt()
        val maxHeight = (screenWidth * 0.6).toInt()
        val standardImageHeight = (minHeight + maxHeight) / 2

        containerView.removeAllViews()
        containerView.visibility = View.VISIBLE
        containerView.setPadding(0, 0, 0, 0)

        if (files.isEmpty()) {
            containerView.visibility = View.GONE
            return
        }

        when (files.size) {
            1 -> createSingleAudiosOnly(context, containerView, files, durationData, thumbnailData, maxHeight, isRepost, post)
            2 -> createTwoAudioLayout(context, containerView, files, durationData, thumbnailData, standardImageHeight, spaceBetweenCards, isRepost, post)
            3 -> createThreeAudioLayout(context, containerView, files, durationData, thumbnailData, standardImageHeight, spaceBetweenCards, isRepost, post)
            else -> createGridAudiosOnly(context, containerView, files, durationData, thumbnailData, standardImageHeight, screenWidth, spaceBetweenCards, isRepost, post)
        }
    }

    // Video Media Functions
    private fun showOriginalVideosOnly(post: Any, firstFile: File) {
        val files = when (post) {
            is OriginalPost -> post.files.filter { isVideoFile(it) }
            is Post -> post.files.filter { isVideoFile(it) }
            else -> emptyList()
        }
        val thumbnail = when (post) {
            is OriginalPost -> post.thumbnail
            is Post -> post.thumbnail
            else -> emptyList()
        }
        val duration = when (post) {
            is OriginalPost -> post.duration
            is Post -> post.duration
            else -> emptyList()
        }
        showVideoMedia(files, mixedFilesCardView, thumbnail, duration, false, post)
    }

    private fun showVideoMedia(
        files: List<File>,
        containerView: CardView,
        thumbnailData: List<Thumbnail>? = null,
        durationData: List<Duration>? = null,
        isRepost: Boolean = false,
        post: Any? = null
    ) {
        val context = requireContext()
        val screenWidth = context.resources.displayMetrics.widthPixels
        val margin = 4.dpToPx(context)
        val spaceBetweenRows = 4.dpToPx(context)
        val minHeight = (screenWidth * 0.4).toInt()
        val maxHeight = (screenWidth * 0.6).toInt()
        val standardImageHeight = (minHeight + maxHeight) / 2

        containerView.removeAllViews()
        containerView.visibility = View.VISIBLE
        if (files.isEmpty()) {
            containerView.visibility = View.GONE
            return
        }

        when (files.size) {
            1 -> createSingleVideoOnly(context, containerView, files, thumbnailData, durationData, standardImageHeight, isRepost, post)
            2 -> createTwoVideosUI(context, containerView, files, thumbnailData, durationData, standardImageHeight, spaceBetweenRows, isRepost, post)
            3 -> createThreeVideosUI(context, containerView, files, thumbnailData, durationData, standardImageHeight, margin, spaceBetweenRows, isRepost, post)
            else -> createGridVideosOnly(context, containerView, files, thumbnailData, durationData, standardImageHeight, screenWidth, spaceBetweenRows, isRepost, post)
        }
    }

    // Document Media Functions
    private fun showOriginalDocumentsOnly(post: Any?, firstFile: File) {
        val files = when (post) {
            is OriginalPost -> post.files.filter { isDocumentFile(it) }
            is Post -> post.files.filter { isDocumentFile(it) }
            else -> emptyList()
        }
        val fileIds = when (post) {
            is OriginalPost -> post.fileIds
            is Post -> post.fileIds
            else -> emptyList()
        }
        val fileTypes = when (post) {
            is OriginalPost -> post.fileTypes
            is Post -> post.fileTypes
            else -> emptyList()
        }
        val thumbnail = when (post) {
            is OriginalPost -> post.thumbnail
            is Post -> post.thumbnail
            else -> emptyList()
        }
        showDocumentMedia(files, mixedFilesCardView, fileIds as List<String>, fileTypes, thumbnail, post)
    }

    private fun showDocumentMedia(
        files: List<File>,
        containerView: CardView,
        fileIds: List<String>,
        fileTypes: List<FileType>,
        thumbnailData: List<Thumbnail>?,
        post: Any?
    ) {
        val context = requireContext()
        val screenWidth = context.resources.displayMetrics.widthPixels
        val margin = 4.dpToPx(context)
        val minHeight = (screenWidth * 0.4).toInt()
        val maxHeight = (screenWidth * 0.6).toInt()
        val standardImageHeight = (minHeight + maxHeight) / 2

        containerView.removeAllViews()
        containerView.visibility = View.VISIBLE
        if (files.isEmpty()) {
            containerView.visibility = View.GONE
            return
        }

        if (files.size == 1) {
            createSingleDocumentOnly(context, containerView, files, thumbnailData, fileTypes, standardImageHeight, post)
        } else {
            createMultipleDocumentsOnly(context, containerView, files, thumbnailData, fileTypes, standardImageHeight, margin, post)
        }
    }

    // Fixed Mixed Media Functions

    private fun showMixedMedia(
        files: List<File>,
        containerView: CardView,
        thumbnailData: List<Thumbnail>? = null,
        durationData: List<Duration>? = null,
        fileIds: List<String>,
        fileTypes: List<FileType>,
        isRepost: Boolean = false,
        post: Any? = null
    ) {
        val context = requireContext()
        val screenWidth = context.resources.displayMetrics.widthPixels
        val margin = 4.dpToPx(context)
        val spaceBetweenRows = 4.dpToPx(context)
        val minHeight = (screenWidth * 0.4).toInt()
        val maxHeight = (screenWidth * 0.6).toInt()
        val standardImageHeight = (minHeight + maxHeight) / 2

        containerView.removeAllViews()
        containerView.visibility = View.VISIBLE

        // Log file types for debugging
        files.forEachIndexed { index, file ->
            Log.d("MixedMedia", "File $index: fileId=${file.fileId}, url=${file.url}, isImage=${isImageFile(file, fileTypes)}, isAudio=${isAudioFile(file, fileTypes)}, isVideo=${isVideoFile(file, fileTypes)}, isDocument=${isDocumentFile(file, fileTypes)}")
        }

        // Make sure audio container is also visible for mixed content
        if (files.any { isAudioFile(it, fileTypes) }) {
            multipleAudiosContainer.visibility = View.VISIBLE
            multipleAudiosContainer.removeAllViews()
        } else {
            multipleAudiosContainer.visibility = View.GONE
        }

        if (files.isEmpty()) {
            containerView.visibility = View.GONE
            multipleAudiosContainer.visibility = View.GONE
            return
        }

        when (files.size) {
            1 -> {
                val file = files[0]
                when {
                    isImageFile(file, fileTypes) -> createSingleImageLayout(context, containerView, files, isRepost, post)
                    isVideoFile(file, fileTypes) -> createSingleVideoOnly(context, containerView, files, thumbnailData, durationData, maxHeight, isRepost, post)
                    isAudioFile(file, fileTypes) -> {
                        createSingleAudioInMixedMedia(context, multipleAudiosContainer, files, durationData ?: emptyList(), thumbnailData, maxHeight, isRepost, post)
                    }
                    isDocumentFile(file, fileTypes) -> createSingleDocumentOnly(context, containerView, files, thumbnailData, fileTypes, standardImageHeight, post)
                }
            }
            2 -> createTwoMixedMediaLayout(context, containerView, files, thumbnailData, durationData, fileTypes, standardImageHeight, spaceBetweenRows, isRepost, post)
            3 -> createThreeMixedMediaLayout(context, containerView, files, thumbnailData, durationData, fileTypes, standardImageHeight, margin, spaceBetweenRows, isRepost, post)
            else -> createGridCombinationOfMultipleFiles(context, containerView, files, thumbnailData, durationData, fileTypes, standardImageHeight, screenWidth, spaceBetweenRows, isRepost, post)
        }
    }

    private fun createSingleAudioInMixedMedia(
        context: Context,
        containerView: ViewGroup, // Changed to ViewGroup to use multipleAudiosContainer
        files: List<File>,
        durationData: List<Duration>,
        thumbnailData: List<Thumbnail>?,
        height: Int,
        isRepost: Boolean,
        post: Any?
    ) {
        val audioView = createAudioCard(context, files, 0, durationData, thumbnailData, height, isRepost, post)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            height
        ).apply {
            setMargins(0, 0, 0, 0)
        }
        containerView.addView(audioView, layoutParams)
        Log.d("MixedMedia", "Created single audio card for fileId=${files[0].fileId}")
    }



    private fun showOriginalCombinationOfMultipleFiles(post: Any, firstFile: File) {
        val files = when (post) {
            is OriginalPost -> post.files.filter { isMultipleCombinationFile(it, post.fileTypes) }
            is Post -> post.files.filter { isMultipleCombinationFile(it, post.fileTypes) }
            else -> emptyList()
        }
        val thumbnail = when (post) {
            is OriginalPost -> post.thumbnail
            is Post -> post.thumbnail
            else -> emptyList()
        }
        val duration = when (post) {
            is OriginalPost -> post.duration
            is Post -> post.duration
            else -> emptyList()
        }
        val fileIds = when (post) {
            is OriginalPost -> post.fileIds
            is Post -> post.fileIds
            else -> emptyList()
        }
        val fileTypes = when (post) {
            is OriginalPost -> post.fileTypes
            is Post -> post.fileTypes
            else -> emptyList()
        }
        showMixedMedia(files, mixedFilesCardView, thumbnail, duration, fileIds as List<String>, fileTypes, false, post)
    }

    // New function to handle single audio in mixed media context
    private fun createSingleAudioInMixedMedia(
        context: Context,
        containerView: CardView,
        files: List<File>,
        durationData: List<Duration>,
        thumbnailData: List<Thumbnail>?,
        height: Int,
        isRepost: Boolean,
        post: Any?
    ) {
        val audioView = createAudioCard(context, files, 0, durationData, thumbnailData, height, isRepost, post)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            height
        ).apply {
            setMargins(0, 0, 0, 0)
        }
        containerView.addView(audioView, layoutParams)
    }

    // Fixed Two Mixed Media Layout
    private fun createTwoMixedMediaLayout(
        context: Context,
        containerView: CardView,
        files: List<File>,
        thumbnailData: List<Thumbnail>?,
        durationData: List<Duration>?,
        fileTypes: List<FileType>,
        standardImageHeight: Int,
        spaceBetweenRows: Int,
        isRepost: Boolean,
        post: Any?
    ) {
        val horizontalLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                standardImageHeight
            )
        }
        for (i in 0..1) {
            val file = files[i]
            val cardView = when {
                isImageFile(file) -> createImageCard(context, files, i, standardImageHeight, isRepost, post)
                isVideoFile(file) -> createVideoCard(context, files, i, thumbnailData, durationData, standardImageHeight, isRepost, post)
                isAudioFile(file) -> createAudioCard(context, files, i, durationData ?: emptyList(), thumbnailData, standardImageHeight, isRepost, post)
                isDocumentFile(file) -> createDocumentCard(context, files, i, thumbnailData, fileTypes, standardImageHeight, post)
                else -> createImageCard(context, files, i, standardImageHeight, isRepost, post)
            }
            val layoutParams = LinearLayout.LayoutParams(
                0,
                standardImageHeight,
                1f
            ).apply {
                if (i == 0) rightMargin = spaceBetweenRows / 2
                else leftMargin = spaceBetweenRows / 2
            }
            cardView.layoutParams = layoutParams
            horizontalLayout.addView(cardView)
        }
        containerView.addView(horizontalLayout)
    }

    // Fixed Three Mixed Media Layout
    private fun createThreeMixedMediaLayout(
        context: Context,
        containerView: CardView,
        files: List<File>,
        thumbnailData: List<Thumbnail>?,
        durationData: List<Duration>?,
        fileTypes: List<FileType>,
        standardImageHeight: Int,
        margin: Int,
        spaceBetweenRows: Int,
        isRepost: Boolean,
        post: Any?
    ) {
        val horizontalLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                standardImageHeight
            )
        }
        val screenWidth = context.resources.displayMetrics.widthPixels
        val leftCard = when {
            isImageFile(files[0]) -> createImageCard(context, files, 0, standardImageHeight, isRepost, post)
            isVideoFile(files[0]) -> createVideoCard(context, files, 0, thumbnailData, durationData, standardImageHeight, isRepost, post)
            isAudioFile(files[0]) -> createAudioCard(context, files, 0, durationData ?: emptyList(), thumbnailData, standardImageHeight, isRepost, post)
            isDocumentFile(files[0]) -> createDocumentCard(context, files, 0, thumbnailData, fileTypes, standardImageHeight, post)
            else -> createImageCard(context, files, 0, standardImageHeight, isRepost, post)
        }
        val leftParams = LinearLayout.LayoutParams(
            (screenWidth - spaceBetweenRows) / 2,
            standardImageHeight
        ).apply { rightMargin = spaceBetweenRows / 2 }
        leftCard.layoutParams = leftParams

        val rightLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                (screenWidth - spaceBetweenRows) / 2,
                standardImageHeight
            ).apply { leftMargin = spaceBetweenRows / 2 }
        }
        for (i in 1..2) {
            val file = files[i]
            val cardView = when {
                isImageFile(file) -> createImageCard(context, files, i, (standardImageHeight - margin) / 2, isRepost, post)
                isVideoFile(file) -> createVideoCard(context, files, i, thumbnailData, durationData, (standardImageHeight - margin) / 2, isRepost, post)
                isAudioFile(file) -> createAudioCard(context, files, i, durationData ?: emptyList(), thumbnailData, (standardImageHeight - margin) / 2, isRepost, post)
                isDocumentFile(file) -> createDocumentCard(context, files, i, thumbnailData, fileTypes, (standardImageHeight - margin) / 2, post)
                else -> createImageCard(context, files, i, (standardImageHeight - margin) / 2, isRepost, post)
            }
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (standardImageHeight - margin) / 2
            ).apply { if (i == 1) bottomMargin = margin }
            cardView.layoutParams = layoutParams
            rightLayout.addView(cardView)
        }
        horizontalLayout.addView(leftCard)
        horizontalLayout.addView(rightLayout)
        containerView.addView(horizontalLayout)
    }

    // Fixed Grid for Mixed Media (4+ items)
    private fun createGridCombinationOfMultipleFiles(
        context: Context,
        containerView: CardView,
        files: List<File>,
        thumbnailData: List<Thumbnail>?,
        durationData: List<Duration>?,
        fileTypes: List<FileType>,
        standardImageHeight: Int,
        screenWidth: Int,
        spaceBetweenRows: Int,
        isRepost: Boolean,
        post: Any?
    ) {
        val verticalLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                standardImageHeight
            )
        }
        val gridItemHeight = (standardImageHeight * 2 - spaceBetweenRows) / 2
        listOf(0..1, 2..3).forEachIndexed { rowIndex, range ->
            val rowLayout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    gridItemHeight
                ).apply { if (rowIndex == 1) topMargin = spaceBetweenRows }
            }
            range.forEach { i ->
                if (i < files.size) {
                    val extraCount = if (i == 3 && files.size > 4) files.size - 4 else 0
                    val file = files[i]
                    val cardView = when {
                        isImageFile(file) -> createImageCard(context, files, i, gridItemHeight, isRepost, post, extraCount)
                        isVideoFile(file) -> createVideoCard(context, files, i, thumbnailData, durationData, gridItemHeight, isRepost, post, extraCount)
                        isAudioFile(file) -> createAudioCard(context, files, i, durationData ?: emptyList(), thumbnailData, gridItemHeight, isRepost, post, extraCount)
                        isDocumentFile(file) -> createDocumentCard(context, files, i, thumbnailData, fileTypes, gridItemHeight, post, extraCount)
                        else -> createImageCard(context, files, i, gridItemHeight, isRepost, post, extraCount)
                    }
                    val layoutParams = LinearLayout.LayoutParams(
                        (screenWidth - spaceBetweenRows) / 2,
                        gridItemHeight
                    ).apply {
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



    // Make sure your audio card creation function handles null/empty cases properly
    private fun createAudioCard(
        context: Context,
        files: List<File>,
        index: Int,
        durationData: List<Duration>,
        thumbnailData: List<Thumbnail>?,
        height: Int,
        isRepost: Boolean,
        post: Any?,
        extraCount: Int = 0
    ): View {
        // Your existing createAudioCard implementation
        // Make sure it handles empty durationData and thumbnailData gracefully

        // Example structure (adapt to your existing implementation):
        val cardView = CardView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                height
            )
            radius = 8.dpToPx(context).toFloat()
            elevation = 4.dpToPx(context).toFloat()
        }

        // Add your audio-specific UI elements here
        // Handle cases where durationData might be empty
        val duration = if (index < durationData.size) durationData[index].duration else "00:00"

        // Handle cases where thumbnailData might be null or empty
        val thumbnail = thumbnailData?.getOrNull(index)

        // Add extra count overlay if needed
        if (extraCount > 0) {
            val countTextView = createCountTextView(context, extraCount)
            if (cardView is FrameLayout) {
                cardView.addView(countTextView)
            }
        }

        return cardView
    }



    // Single Item Layout Functions
    private fun createSingleImageLayout(
        context: Context,
        containerView: CardView,
        files: List<File>,
        isRepost: Boolean,
        post: Any?
    ) {
        val imageView = createImageCard(context, files, 0, calculateActualImageHeight(context, files[0].toString()), isRepost, post)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            calculateActualImageHeight(context, files[0].toString())
        )
        containerView.addView(imageView, layoutParams)
    }

    private fun createSingleAudiosOnly(
        context: Context,
        containerView: ViewGroup,
        files: List<File>,
        durationData: List<Duration>,
        thumbnailData: List<Thumbnail>?,
        height: Int,
        isRepost: Boolean,
        post: Any?
    ) {
        val audioView = createAudioCard(context, files, 0, durationData, thumbnailData, height, isRepost, post)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            height
        ).apply {
            setMargins(0, 0, 0, 0)
        }
        containerView.addView(audioView, layoutParams)
    }

    private fun createSingleVideoOnly(
        context: Context,
        containerView: CardView,
        files: List<File>,
        thumbnailData: List<Thumbnail>?,
        durationData: List<Duration>?,
        height: Int,
        isRepost: Boolean,
        post: Any?
    ) {
        val reducedHeight = (height * 0.8).toInt()
        val cardView = createVideoCard(context, files, 0, thumbnailData, durationData, reducedHeight, isRepost, post)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            reducedHeight
        )
        containerView.addView(cardView, layoutParams)
    }

    private fun createSingleDocumentOnly(
        context: Context,
        containerView: CardView,
        files: List<File>,
        thumbnailData: List<Thumbnail>?,
        fileTypes: List<FileType>?,
        height: Int,
        post: Any?
    ) {
        val screenWidth = context.resources.displayMetrics.widthPixels
        val documentWidth = (screenWidth * 0.7).toInt()
        val sideMargin = (screenWidth - documentWidth) / 2

        val backgroundView = View(context).apply {
            setBackgroundColor(Color.parseColor("#F5F5F5"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                height
            )
        }

        val frameLayout = FrameLayout(context)
        val documentCard = createDocumentCard(context, files, 0, thumbnailData, fileTypes, height, post, 0)

        val layoutParams = FrameLayout.LayoutParams(
            documentWidth,
            height
        ).apply {
            leftMargin = sideMargin
            rightMargin = sideMargin
            gravity = Gravity.CENTER_HORIZONTAL
        }

        frameLayout.addView(backgroundView)
        frameLayout.addView(documentCard, layoutParams)
        containerView.addView(frameLayout)
    }

    // Two Item Layout Functions
    private fun createTwoImageLayout(
        context: Context,
        containerView: CardView,
        files: List<File>,
        standardImageHeight: Int,
        spaceBetweenRows: Int,
        isRepost: Boolean,
        post: Any?
    ) {
        val horizontalLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                standardImageHeight
            )
        }
        for (i in 0..1) {
            val imageView = createImageCard(context, files, i, standardImageHeight, isRepost, post)
            val layoutParams = LinearLayout.LayoutParams(
                0,
                standardImageHeight,
                1f
            ).apply {
                if (i == 0) rightMargin = spaceBetweenRows / 2
                else leftMargin = spaceBetweenRows / 2
            }
            imageView.layoutParams = layoutParams
            horizontalLayout.addView(imageView)
        }
        containerView.addView(horizontalLayout)
    }

    private fun createTwoAudioLayout(
        context: Context,
        containerView: ViewGroup,
        files: List<File>,
        durationData: List<Duration>,
        thumbnailData: List<Thumbnail>?,
        standardImageHeight: Int,
        spaceBetweenCards: Int,
        isRepost: Boolean,
        post: Any?
    ) {
        val horizontalLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                standardImageHeight
            )
            setPadding(0, 0, 0, 0)
        }

        for (i in 0..1) {
            val audioView = createAudioCard(context, files, i, durationData, thumbnailData, standardImageHeight, isRepost, post)
            val layoutParams = LinearLayout.LayoutParams(
                0,
                standardImageHeight,
                1f
            ).apply {
                when (i) {
                    0 -> rightMargin = spaceBetweenCards / 2
                    1 -> leftMargin = spaceBetweenCards / 2
                }
            }
            audioView.layoutParams = layoutParams
            horizontalLayout.addView(audioView)
        }
        containerView.addView(horizontalLayout)
    }

    private fun createTwoVideosUI(
        context: Context,
        containerView: CardView,
        files: List<File>,
        thumbnailData: List<Thumbnail>?,
        durationData: List<Duration>?,
        standardImageHeight: Int,
        spaceBetweenRows: Int,
        isRepost: Boolean,
        post: Any?
    ) {
        val horizontalLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                standardImageHeight
            )
        }
        for (i in 0..1) {
            val cardView = createVideoCard(context, files, i, thumbnailData, durationData, standardImageHeight, isRepost, post)
            val layoutParams = LinearLayout.LayoutParams(
                0,
                standardImageHeight,
                1f
            ).apply {
                if (i == 0) rightMargin = spaceBetweenRows / 2
                else leftMargin = spaceBetweenRows / 2
            }
            cardView.layoutParams = layoutParams
            horizontalLayout.addView(cardView)
        }
        containerView.addView(horizontalLayout)
    }



    // Three Item Layout Functions
    private fun createThreeImageLayout(
        context: Context,
        containerView: CardView,
        files: List<File>,
        standardImageHeight: Int,
        margin: Int,
        spaceBetweenRows: Int,
        isRepost: Boolean,
        post: Any?
    ) {
        val horizontalLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                standardImageHeight
            )
        }
        val screenWidth = context.resources.displayMetrics.widthPixels
        val leftImage = createImageCard(context, files, 0, standardImageHeight, isRepost, post)
        val leftParams = LinearLayout.LayoutParams(
            (screenWidth - spaceBetweenRows) / 2,
            standardImageHeight
        ).apply { rightMargin = spaceBetweenRows / 2 }
        leftImage.layoutParams = leftParams

        val rightLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                (screenWidth - spaceBetweenRows) / 2,
                standardImageHeight
            ).apply { leftMargin = spaceBetweenRows / 2 }
        }
        for (i in 1..2) {
            val imageView = createImageCard(context, files, i, (standardImageHeight - margin) / 2, isRepost, post)
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (standardImageHeight - margin) / 2
            ).apply { if (i == 1) bottomMargin = margin }
            imageView.layoutParams = layoutParams
            rightLayout.addView(imageView)
        }
        horizontalLayout.addView(leftImage)
        horizontalLayout.addView(rightLayout)
        containerView.addView(horizontalLayout)
    }

    private fun createThreeAudioLayout(
        context: Context,
        containerView: ViewGroup,
        files: List<File>,
        durationData: List<Duration>,
        thumbnailData: List<Thumbnail>?,
        standardImageHeight: Int,
        spaceBetweenCards: Int,
        isRepost: Boolean,
        post: Any?
    ) {
        val horizontalLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                standardImageHeight
            )
            setPadding(0, 0, 0, 0)
        }

        val screenWidth = context.resources.displayMetrics.widthPixels
        val leftCard = createAudioCard(context, files, 0, durationData, thumbnailData, standardImageHeight, isRepost, post)
        val leftParams = LinearLayout.LayoutParams(
            (screenWidth - spaceBetweenCards) / 2,
            standardImageHeight
        ).apply {
            rightMargin = spaceBetweenCards / 2
        }
        leftCard.layoutParams = leftParams

        val rightLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                (screenWidth - spaceBetweenCards) / 2,
                standardImageHeight
            ).apply {
                leftMargin = spaceBetweenCards / 2
            }
        }

        for (i in 1..2) {
            val cardView = createAudioCard(context, files, i, durationData, thumbnailData, (standardImageHeight - spaceBetweenCards) / 2, isRepost, post)
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (standardImageHeight - spaceBetweenCards) / 2
            ).apply {
                if (i == 1) bottomMargin = spaceBetweenCards
            }
            cardView.layoutParams = layoutParams
            rightLayout.addView(cardView)
        }

        horizontalLayout.addView(leftCard)
        horizontalLayout.addView(rightLayout)
        containerView.addView(horizontalLayout)
    }

    private fun createThreeVideosUI(
        context: Context,
        containerView: CardView,
        files: List<File>,
        thumbnailData: List<Thumbnail>?,
        durationData: List<Duration>?,
        standardImageHeight: Int,
        margin: Int,
        spaceBetweenRows: Int,
        isRepost: Boolean,
        post: Any?
    ) {
        val horizontalLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                standardImageHeight
            )
        }
        val screenWidth = context.resources.displayMetrics.widthPixels
        val leftCard = createVideoCard(context, files, 0, thumbnailData, durationData, standardImageHeight, isRepost, post)
        val leftParams = LinearLayout.LayoutParams(
            (screenWidth - spaceBetweenRows) / 2,
            standardImageHeight
        ).apply { rightMargin = spaceBetweenRows / 2 }
        leftCard.layoutParams = leftParams

        val rightLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                (screenWidth - spaceBetweenRows) / 2,
                standardImageHeight
            ).apply { leftMargin = spaceBetweenRows / 2 }
        }
        for (i in 1..2) {
            val cardView = createVideoCard(context, files, i, thumbnailData, durationData, (standardImageHeight - margin) / 2, isRepost, post)
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (standardImageHeight - margin) / 2
            ).apply { if (i == 1) bottomMargin = margin }
            cardView.layoutParams = layoutParams
            rightLayout.addView(cardView)
        }
        horizontalLayout.addView(leftCard)
        horizontalLayout.addView(rightLayout)
        containerView.addView(horizontalLayout)
    }


    // Four and Above Item Layout Functions
    private fun createGridImagesOnly(
        context: Context,
        containerView: CardView,
        files: List<File>,
        standardImageHeight: Int,
        screenWidth: Int,
        spaceBetweenRows: Int,
        isRepost: Boolean,
        post: Any?
    ) {
        val verticalLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                standardImageHeight
            )
        }
        val gridItemHeight = (standardImageHeight * 2 - spaceBetweenRows) / 2

        listOf(0..1, 2..3).forEachIndexed { rowIndex, range ->
            val rowLayout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    gridItemHeight
                ).apply { if (rowIndex == 1) topMargin = spaceBetweenRows }
            }
            range.forEach { i ->
                if (i < files.size) {
                    val extraCount = if (i == 3 && files.size > 4) files.size - 4 else 0
                    val imageView = createImageCard(context, files, i, gridItemHeight, isRepost, post, extraCount)
                    val layoutParams = LinearLayout.LayoutParams(
                        (screenWidth - spaceBetweenRows) / 2,
                        gridItemHeight
                    ).apply {
                        if (i % 2 == 0) rightMargin = spaceBetweenRows / 2
                        else leftMargin = spaceBetweenRows / 2
                    }
                    imageView.layoutParams = layoutParams
                    rowLayout.addView(imageView)
                }
            }
            verticalLayout.addView(rowLayout)
        }
        containerView.addView(verticalLayout)
    }

    private fun createGridAudiosOnly(
        context: Context,
        containerView: ViewGroup,
        files: List<File>,
        durationData: List<Duration>,
        thumbnailData: List<Thumbnail>?,
        standardImageHeight: Int,
        screenWidth: Int,
        spaceBetweenCards: Int,
        isRepost: Boolean,
        post: Any?
    ) {
        val verticalLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                standardImageHeight
            )
            setPadding(0, 0, 0, 0)
        }
        val gridItemHeight = (standardImageHeight * 2 - spaceBetweenCards) / 2 // Increased by 20%

        listOf(0..1, 2..3).forEachIndexed { rowIndex, range ->
            val rowLayout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    gridItemHeight
                ).apply {
                    if (rowIndex == 1) topMargin = spaceBetweenCards
                }
            }

            range.forEach { i ->
                if (i < files.size) {
                    val extraCount = if (i == 3 && files.size > 4) files.size - 4 else 0
                    val audioView = createAudioCard(context, files, i, durationData, thumbnailData, gridItemHeight, isRepost, post, extraCount)
                    val layoutParams = LinearLayout.LayoutParams(
                        (screenWidth - spaceBetweenCards) / 2,
                        gridItemHeight
                    ).apply {
                        when (i % 2) {
                            0 -> rightMargin = spaceBetweenCards / 2
                            1 -> leftMargin = spaceBetweenCards / 2
                        }
                    }
                    audioView.layoutParams = layoutParams
                    rowLayout.addView(audioView)
                }
            }
            verticalLayout.addView(rowLayout)
        }
        containerView.addView(verticalLayout)
    }

    private fun createGridVideosOnly(
        context: Context,
        containerView: CardView,
        files: List<File>,
        thumbnailData: List<Thumbnail>?,
        durationData: List<Duration>?,
        standardImageHeight: Int,
        screenWidth: Int,
        spaceBetweenRows: Int,
        isRepost: Boolean,
        post: Any?
    ) {
        val verticalLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                standardImageHeight
            )
        }
        val gridItemHeight = (standardImageHeight * 2 - spaceBetweenRows) / 2 // Increased by 20%
        listOf(0..1, 2..3).forEachIndexed { rowIndex, range ->
            val rowLayout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    gridItemHeight
                ).apply { if (rowIndex == 1) topMargin = spaceBetweenRows }
            }
            range.forEach { i ->
                if (i < files.size) {
                    val extraCount = if (i == 3 && files.size > 4) files.size - 4 else 0
                    val cardView = createVideoCard(context, files, i, thumbnailData, durationData, gridItemHeight, isRepost, post, extraCount)
                    val layoutParams = LinearLayout.LayoutParams(
                        (screenWidth - spaceBetweenRows) / 2,
                        gridItemHeight
                    ).apply {
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

    private fun createMultipleDocumentsOnly(
        context: Context,
        containerView: CardView,
        files: List<File>,
        thumbnailData: List<Thumbnail>?,
        fileTypes: List<FileType>?,
        standardImageHeight: Int,
        margin: Int,
        post: Any?
    ) {
        val verticalLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                standardImageHeight
            )
        }
        val gridItemHeight = (standardImageHeight * 2 - margin) / 2 // Increased by 20%

        listOf(0..1, 2..3).forEachIndexed { rowIndex, range ->
            val rowLayout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    gridItemHeight
                ).apply { if (rowIndex == 1) topMargin = margin }
            }
            val screenWidth = context.resources.displayMetrics.widthPixels
            range.forEach { i ->
                if (i < files.size) {
                    val extraCount = if (i == 3 && files.size > 4) files.size - 4 else 0
                    val cardView = createDocumentCard(context, files, i, thumbnailData, fileTypes, gridItemHeight, post, extraCount)
                    val layoutParams = LinearLayout.LayoutParams(
                        (screenWidth - margin) / 2,
                        gridItemHeight
                    ).apply {
                        if (i % 2 == 0) rightMargin = margin / 2
                        else leftMargin = margin / 2
                    }
                    cardView.layoutParams = layoutParams
                    rowLayout.addView(cardView)
                }
            }
            verticalLayout.addView(rowLayout)
        }
        containerView.addView(verticalLayout)
    }



    // Card Creation Functions
    private fun createImageCard(
        context: Context,
        files: List<File>,
        index: Int,
        height: Int,
        isRepost: Boolean,
        post: Any?,
        extraCount: Int = 0
    ): CardView {
        val cardView = CardView(context).apply {
            radius = 8.dpToPx(context).toFloat()
            cardElevation = 0f
            setCardBackgroundColor(Color.WHITE)
            clipToOutline = true
        }
        val frameLayout = FrameLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                height
            )
        }
        val imageView = ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        loadImageWithGlide(context, imageView, files, index)
        val countTextView = createCountTextView(context, extraCount)
        frameLayout.addView(imageView)
        frameLayout.addView(countTextView)
        cardView.addView(frameLayout)
        cardView.setOnClickListener { handleImageClick(index, files, isRepost, post) }
        return cardView
    }



    private fun createVideoCard(
        context: Context,
        files: List<File>,
        index: Int,
        thumbnailData: List<Thumbnail>?,
        durationData: List<Duration>?,
        height: Int,
        isRepost: Boolean,
        post: Any?,
        extraCount: Int = 0
    ): CardView {
        val cardView = CardView(context).apply {
            radius = 8.dpToPx(context).toFloat()
            cardElevation = 0f
            setCardBackgroundColor(Color.WHITE)
            clipToOutline = true
        }

        val frameLayout = FrameLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                height
            )
        }

        val thumbnailImageView = createVideoThumbnailImageView(context)
        val playButton = createVideoPlayButton(context)
        val durationLayout = createVideoDurationLayout(context, durationData, files, index)
        val countTextView = createCountTextView(context, extraCount)

        loadVideoThumbnailAndDuration(
            context,
            thumbnailImageView,
            durationLayout,
            files,
            index,
            thumbnailData,
            durationData
        )

        frameLayout.apply {
            addView(thumbnailImageView)
            addView(playButton)
            addView(durationLayout)
            addView(countTextView)
        }

        cardView.addView(frameLayout)
        cardView.setOnClickListener { handleVideoClick(index, files, isRepost, post) }

        return cardView
    }

    private fun createDocumentCard(
        context: Context,
        files: List<File>,
        index: Int,
        thumbnailData: List<Thumbnail>?,
        fileTypes: List<FileType>?,
        height: Int,
        post: Any?,
        extraCount: Int = 0
    ): CardView {
        val cardView = CardView(context).apply {
            radius = 8.dpToPx(context).toFloat()
            cardElevation = 0f
            setCardBackgroundColor(Color.WHITE)
            clipToOutline = true
        }

        val frameLayout = FrameLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                height
            )
        }

        val imageView = ImageView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.FIT_XY
        }

        val fileIdToFind = files.getOrNull(index)?.fileId
        val documentType = fileTypes?.find { it.fileId == fileIdToFind }

        val fileIconOverlay = ImageView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                20.dpToPx(context),
                20.dpToPx(context),
                Gravity.TOP or Gravity.START
            ).apply {
                setMargins(8.dpToPx(context), 8.dpToPx(context), 0, 0)
            }

            documentType?.let { docType ->
                setImageResource(
                    when (docType.fileType.lowercase()) {
                        "pdf" -> R.drawable.pdf_icon
                        "doc", "docx" -> R.drawable.word_icon
                        "ppt", "pptx" -> R.drawable.powerpoint_icon
                        "xls", "xlsx" -> R.drawable.excel_icon
                        "txt", "rtf" -> R.drawable.text_icon
                        "odt" -> R.drawable.word_icon
                        "csv" -> R.drawable.excel_icon
                        else -> R.drawable.text_icon
                    }
                )
            } ?: setImageResource(R.drawable.text_icon)
            visibility = View.VISIBLE
        }

        val thumbnailUrl = thumbnailData?.find { it.fileId == fileIdToFind }?.thumbnailUrl
        if (!thumbnailUrl.isNullOrEmpty()) {
            Glide.with(context)
                .load(thumbnailUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transform(com.bumptech.glide.load.resource.bitmap.RoundedCorners(8.dpToPx(context)))
                .into(imageView)
        } else {
            imageView.setImageResource(getDocumentPlaceholder(files.getOrNull(index)))
        }

        val countTextView = createCountTextView(context, extraCount)

        frameLayout.addView(imageView)
        frameLayout.addView(fileIconOverlay)
        frameLayout.addView(countTextView)
        cardView.addView(frameLayout)

        cardView.setOnClickListener {
            val fileIds = when (post) {
                is OriginalPost -> post.fileIds as? List<String> ?: emptyList()
                is Post -> post.fileIds as? List<String> ?: emptyList()
                else -> emptyList()
            }
            navigateToTappedFilesFragment(context, index, files, fileIds)
        }

        return cardView
    }

    // Supporting Functions for Audio
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
            val isVoiceNote = files.getOrNull(index)?.let { file ->
                file.mimeType?.lowercase()?.contains("ogg|aac|wav|flac|amr|3gp|opus".toRegex()) == true ||
                        file.url?.lowercase()?.matches(".*\\.(ogg|aac|wav|flac|amr|3gp|opus)$".toRegex()) == true
            } ?: false
            setBackgroundColor(if (isVoiceNote) Color.parseColor("#616161") else Color.WHITE)

            val thumbnailUrl = getAudioThumbnailUrl(thumbnailData, index)
            if (!thumbnailUrl.isNullOrEmpty()) {
                Glide.with(context)
                    .load(thumbnailUrl)
                    .placeholder(if (isVoiceNote) R.drawable.ic_audio_white_icon else R.drawable.music_icon)
                    .error(if (isVoiceNote) R.drawable.ic_audio_white_icon else R.drawable.music_icon)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .transform(com.bumptech.glide.load.resource.bitmap.RoundedCorners(12.dpToPx(context)))
                    .into(this)
            } else {
                val audioFile = files.getOrNull(index)
                if (audioFile?.url != null) {
                    Glide.with(context)
                        .load(audioFile.url)
                        .placeholder(if (isVoiceNote) R.drawable.ic_audio_white_icon else R.drawable.music_icon)
                        .error(if (isVoiceNote) R.drawable.ic_audio_white_icon else R.drawable.music_icon)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .transform(com.bumptech.glide.load.resource.bitmap.RoundedCorners(12.dpToPx(context)))
                        .into(this)
                } else {
                    setImageResource(if (isVoiceNote) R.drawable.ic_audio_white_icon else R.drawable.music_icon)
                }
            }
        }
    }

    private fun createPlayButtonOverlay(context: Context): ImageView {
        return ImageView(context).apply {
            setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play_button_filled))
            layoutParams = FrameLayout.LayoutParams(
                32.dpToPx(context),
                32.dpToPx(context),
                Gravity.CENTER
            )
            setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        }
    }

    private fun createAudioDurationLayout(context: Context, durationData: List<Duration>, index: Int): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.START
                setMargins(8.dpToPx(context), 0, 0, 8.dpToPx(context))
            }
            val durationIcon = ImageView(context).apply {
                setImageResource(R.drawable.ic_audio_white_icon)
                layoutParams = LinearLayout.LayoutParams(
                    16.dpToPx(context),
                    16.dpToPx(context)
                )
                setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
            }
            val durationTextView = TextView(context).apply {
                text = getAudioDuration(durationData, index) ?: "0:00"
                setTextColor(Color.WHITE)
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                setPadding(4.dpToPx(context), 0, 0, 0)
            }
            addView(durationIcon)
            addView(durationTextView)
        }
    }

    private fun getAudioDuration(durationData: List<Duration>, index: Int): String? {
        return when {
            index < durationData.size -> durationData[index].duration
            durationData.isNotEmpty() -> durationData.first().duration
            else -> null
        }
    }

    private fun getAudioThumbnailUrl(thumbnailData: List<Thumbnail>?, index: Int): String? {
        return thumbnailData?.getOrNull(index)?.thumbnailUrl?.takeIf { it.isNotBlank() }
    }

    // Supporting Functions for Video
    private fun createVideoThumbnailImageView(context: Context): ImageView {
        return ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.WHITE)
            clipToOutline = true
        }
    }

    private fun createVideoPlayButton(context: Context): ImageView {
        return ImageView(context).apply {
            setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play_button_filled))
            layoutParams = FrameLayout.LayoutParams(
                64.dpToPx(context),
                64.dpToPx(context),
                Gravity.CENTER
            )
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        }
    }

    private fun createVideoDurationLayout(
        context: Context,
        durationData: List<Duration>?,
        files: List<File>,
        index: Int
    ): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.START
                setMargins(8.dpToPx(context), 0, 0, 8.dpToPx(context))
            }

            val fileIdToFind = files.getOrNull(index)?.fileId
            val durationItem = durationData?.find { it.fileId == fileIdToFind }
            val formattedDuration = durationItem?.duration ?: "0:00"

            val durationIcon = ImageView(context).apply {
                setImageResource(com.uyscuti.social.call.R.drawable.ic_video_call)
                layoutParams = LinearLayout.LayoutParams(
                    16.dpToPx(context),
                    16.dpToPx(context)
                )
                setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
            }

            val durationTextView = TextView(context).apply {
                text = formattedDuration
                setTextColor(Color.WHITE)
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                setPadding(4.dpToPx(context), 0, 0, 0)
            }

            addView(durationIcon)
            addView(durationTextView)
        }
    }

    private fun loadVideoThumbnailAndDuration(
        context: Context,
        thumbnailImageView: ImageView,
        durationLayout: LinearLayout,
        files: List<File>,
        index: Int,
        thumbnailData: List<Thumbnail>?,
        durationData: List<Duration>?
    ) {
        if (index >= files.size) {
            thumbnailImageView.setImageResource(R.drawable.videoplaceholder)
            durationLayout.visibility = View.GONE
            return
        }
        val file = files[index]
        val thumbnailUrl = getVideoThumbnailUrl(thumbnailData, file.fileId, index)
        if (!thumbnailUrl.isNullOrEmpty()) {
            Glide.with(context)
                .load(thumbnailUrl)
                .placeholder(R.drawable.videoplaceholder)
                .error(R.drawable.videoplaceholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transform(com.bumptech.glide.load.resource.bitmap.RoundedCorners(8.dpToPx(context)))
                .into(thumbnailImageView)
        } else {
            thumbnailImageView.setImageResource(R.drawable.videoplaceholder)
        }
        val duration = getVideoDuration(durationData, file.fileId, index)
        durationLayout.visibility = if (!duration.isNullOrEmpty()) View.VISIBLE else View.GONE
        (durationLayout.getChildAt(1) as? TextView)?.text = duration ?: "0:00"
    }

    private fun getVideoThumbnailUrl(thumbnailData: List<Thumbnail>?, fileId: String?, index: Int): String? {
        return when {
            thumbnailData != null && !fileId.isNullOrEmpty() ->
                thumbnailData.find { it.fileId == fileId }?.thumbnailUrl
            thumbnailData != null && index < thumbnailData.size ->
                thumbnailData[index].thumbnailUrl
            else -> null
        }
    }

    private fun getVideoDuration(durationData: List<Duration>?, fileId: String?, index: Int): String? {
        return when {
            durationData != null && !fileId.isNullOrEmpty() ->
                durationData.find { it.fileId == fileId }?.duration
            durationData != null && index < durationData.size ->
                durationData[index].duration
            else -> null
        }
    }

    // Click Handlers
    private fun loadImageWithGlide(context: Context, imageView: ImageView, files: List<File>, index: Int) {
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
                .transform(com.bumptech.glide.load.resource.bitmap.RoundedCorners(8.dpToPx(context)))
                .into(imageView)
        } else {
            imageView.setImageResource(R.drawable.imageplaceholder)
        }
    }

    private fun handleImageClick(index: Int, files: List<File>, isRepost: Boolean, post: Any?) {
        val logTag = if (isRepost) "RepostImageClick" else "OriginalImageClick"
        Log.d(logTag, "Image at index $index clicked")
        if (index < files.size) {
            val fileIds = when (post) {
                is OriginalPost -> post.fileIds as? List<String> ?: emptyList()
                is Post -> post.fileIds as? List<String> ?: emptyList()
                else -> emptyList()
            }
            navigateToTappedFilesFragment(requireContext(), index, files, fileIds)
        }
    }

    private fun handleAudioClick(index: Int, files: List<File>, isRepost: Boolean, post: Any?) {
        val logTag = if (isRepost) "RepostAudioClick" else "OriginalAudioClick"
        Log.d(logTag, "Audio at index $index clicked")
        if (index < files.size) {
            val fileIds = when (post) {
                is OriginalPost -> post.fileIds as? List<String> ?: emptyList()
                is Post -> post.fileIds as? List<String> ?: emptyList()
                else -> emptyList()
            }
            navigateToTappedFilesFragment(requireContext(), index, files, fileIds)
        }
    }

    private fun handleVideoClick(index: Int, files: List<File>, isRepost: Boolean, post: Any?) {
        val logTag = if (isRepost) "RepostVideoClick" else "OriginalVideoClick"
        Log.d(logTag, "Video at index $index clicked")
        if (index < files.size) {
            val fileIds = when (post) {
                is OriginalPost -> post.fileIds as? List<String> ?: emptyList()
                is Post -> post.fileIds as? List<String> ?: emptyList()
                else -> emptyList()
            }
            navigateToTappedFilesFragment(requireContext(), index, files, fileIds)
        }
    }


    private fun handleMenuButtonClick() = showToast("Options menu")
    private fun handleLikeClick() = toggleLike()
    private fun handleCommentClick() = showToast("Opening comments...")
    private fun handleFavoriteClick() = toggleFavorite()
    private fun handleRetweetClick() = showRetweetOptions()
    private fun handleShareClick() = sharePost()

    private fun updateLikeUI(isLiked: Boolean) {
        like.setImageResource(
            if (isLiked) R.drawable.filled_favorite_like else R.drawable.heart_svgrepo_com
        )
    }

    private fun updateFavoriteUI(isFavorited: Boolean) {
        fav.setImageResource(
            if (isFavorited) R.drawable.filled_favorite else R.drawable.favorite_svgrepo_com__1_
        )
    }

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
            val newBookmarkCount = if (isPostBookmarked()) currentBookmarkCount - 1 else currentBookmarkCount + 1
            updateFavoriteUI(newBookmarkCount > currentBookmarkCount)
            favCount.text = formatCount(newBookmarkCount)
            showToast(
                if (newBookmarkCount > currentBookmarkCount) "Added to favorites!" else "Removed from favorites"
            )
        }
    }

    private fun showRetweetOptions() {
        originalPost?.let { post ->
            val currentRepostCount = repostCount.text.toString().toIntOrNull() ?: post.repostCount
            val newRepostCount = if (post.isReposted) currentRepostCount - 1 else currentRepostCount + 1
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
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
            outputFormat.format(inputFormat.parse(dateTimeString) ?: dateTimeString)
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
        return "0"
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cleanupResources()
    }

    override fun onDetach() {
        super.onDetach()
        isNavigating = false
    }

    companion object {
        internal const val ARG_ORIGINAL_POST = "original_post"
        fun newInstance(data: Post): Fragment_Original_Post_Without_Repost_Inside {
            return Fragment_Original_Post_Without_Repost_Inside().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_ORIGINAL_POST, data)
                }
            }
        }
    }
}