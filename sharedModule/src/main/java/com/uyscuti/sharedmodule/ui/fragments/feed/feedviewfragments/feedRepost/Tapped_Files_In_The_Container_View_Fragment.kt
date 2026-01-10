package com.uyscuti.sharedmodule.ui.fragments.feed.feedviewfragments.feedRepost



import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.pdf.PdfRenderer
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.Parcel
import android.os.ParcelFileDescriptor
import android.os.Parcelable
import android.support.annotation.RequiresApi
import android.util.Log
import android.view.Gravity
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.*
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.*
import java.io.File
import java.io.FileNotFoundException
import java.net.URL
import kotlin.math.abs
import kotlin.random.Random
import java.util.concurrent.TimeUnit
import android.view.animation.LinearInterpolator
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.widget.Toast
import android.Manifest
import android.view.HapticFeedbackConstants
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.navigation.fragment.findNavController
import android.widget.ImageButton
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.uyscuti.sharedmodule.R
import com.uyscuti.sharedmodule.User_Interfaces.OtherUserProfile.OtherUserProfileAccount
import com.uyscuti.sharedmodule.model.GetShortFragments
import com.uyscuti.sharedmodule.model.HideAppBar
import com.uyscuti.sharedmodule.model.HideBottomNav
import com.uyscuti.sharedmodule.model.ShowAppBar
import com.uyscuti.sharedmodule.model.ShowBottomNav
import com.uyscuti.sharedmodule.utils.waveformseekbar.WaveformSeekBar
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.api.retrofit.interfaces.IFlashapi
import org.greenrobot.eventbus.EventBus
import android.content.Intent
import androidx.fragment.app.activityViewModels
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.uyscuti.sharedmodule.adapter.feed.FeedAdapter
import com.uyscuti.sharedmodule.model.ShortsFollowButtonClicked
import com.uyscuti.sharedmodule.ui.fragments.feed.feedviewfragments.feedRepost.Tapped_Files_In_The_Container_View_Fragment.Companion.cachedFollowersList
import com.uyscuti.sharedmodule.utils.FollowingManager
import com.uyscuti.sharedmodule.viewmodels.FollowUnfollowViewModel
import com.uyscuti.social.core.common.data.room.entity.FollowUnFollowEntity
import com.uyscuti.social.network.utils.LocalStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext



//  CONSTANTS
private const val ARG_POST_ID = "post_id"
private const val ARG_POST_DATA = "post_data"
private const val ARG_POST_LIST = "post_list"
private const val ARG_CURRENT_POSITION = "current_position"
private const val TAG = "Tapped Files"


//  MAIN FRAGMENT CLASS
class Tapped_Files_In_The_Container_View_Fragment : Fragment() {


    //  COMPANION OBJECT

    companion object {
        @JvmStatic
        fun newInstance(
            postId: String,
            postData: String? = null,
            postList: ArrayList<PostItem>? = null,
            currentPosition: Int = 0
        ) = Tapped_Files_In_The_Container_View_Fragment().apply {
            arguments = Bundle().apply {
                putString(ARG_POST_ID, postId)
                postData?.let { putString(ARG_POST_DATA, it) }
                postList?.let { putParcelableArrayList(ARG_POST_LIST, it) }
                putInt(ARG_CURRENT_POSITION, currentPosition)
            }
        }
    }


    private var followingUserIds: MutableSet<String> = mutableSetOf()
    private val myFollowersList = mutableSetOf<String>()
    private val myFollowersUsernames = mutableSetOf<String>()
    private val followingManager by lazy { FollowingManager(requireContext()) }
    private val followUnfollowViewModel: FollowUnfollowViewModel by activityViewModels()


    // Retrofit instance
    private lateinit var retrofitInstance: RetrofitInstance
    private lateinit var apiService: IFlashapi

    private var authorNameTextView: TextView? = null
    private var authorUsernameTextView: TextView? = null
    private var authorAvatarImageView: ImageView? = null
    private var authorVerifiedBadge: ImageView? = null

    // Author data
    private var authorName: String? = null
    private var authorUsername: String? = null
    private var authorAvatarUrl: String? = null
    private var isAuthorVerified: Boolean = false

    private lateinit var videoView: VideoView
    private lateinit var mediaController: MediaController
    private lateinit var videoProgressBar: ProgressBar
    private lateinit var videoThumbnail: ImageView
    private var currentVideoPosition = 0
    private var isVideoPlaying = false
    private lateinit var documentScrollView: ScrollView
    private var documentWebView: WebView? = null

    // View Components
    private lateinit var rootView: View
    private lateinit var viewPager: ViewPager2
    private lateinit var pageChangeCallback: ViewPager2.OnPageChangeCallback

    // UI Controls
    private lateinit var cancelButton: ImageButton
    private lateinit var headerMenuButton: ImageButton
    private lateinit var dotsIndicator: LinearLayout
    private lateinit var replyInput: EditText

    // Action Sections
    private lateinit var commentSection: LinearLayout
    private lateinit var retweetSection: LinearLayout
    private lateinit var likeSection: LinearLayout
    private lateinit var viewsSection: LinearLayout
    private lateinit var shareSection: LinearLayout

    // Icons and Text Views
    private lateinit var commentIcon: ImageView
    private lateinit var commentCountTextView: TextView
    private lateinit var reFeedIcon: ImageView
    private lateinit var repostCountTextView: TextView
    private lateinit var likeIcon: ImageView
    private lateinit var likesCount: TextView
    private lateinit var viewsIcon: ImageView
    private lateinit var viewsCount: TextView
    private lateinit var shareIcon: ImageView
    private lateinit var shareCountText: TextView


    private var mediaPlayer: MediaPlayer? = null
    private lateinit var artworkLayout: View
    private lateinit var seekBar: SeekBar

    // Fragment Data
    private var postId: String? = null
    private var postData: String? = null
    private var postList: ArrayList<PostItem>? = null
    private var currentPosition: Int = 0

    // Post State
    private var isLiked = false
    private var isReposted = false

    // Post Metrics
    private var commentCount = 0
    private var repostCount = 0
    private var likeCount = 0
    private var viewCount = 0
    private var shareCount = 0
    private var isNavigatingBack = false


//  LIFECYCLE METHODS

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        extractArguments()
        setupBackPressHandler()
        hideSystemBars()
        initializeApiService()
        loadMyFollowersList()
        loadFollowingListFromCache()

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Only inflate the layout here
        rootView = inflater.inflate(R.layout.tapped_files_in_the_container_view, container, false)
        return rootView
    }

    // Call these in onViewCreated after setting up ViewPager
    @OptIn(UnstableApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hideSystemBars()

        EventBus.getDefault().post(HideAppBar(true))
        EventBus.getDefault().post(HideBottomNav(true))

        // Initialize video components
        videoView = view.findViewById(R.id.videoView)
        videoProgressBar = view.findViewById(R.id.videoProgressBar)

        // Initialize all UI components
        commentCountTextView = view.findViewById(R.id.commentCount)
        repostCountTextView = view.findViewById(R.id.repostCount)
        likesCount = view.findViewById(R.id.likesCount)
        viewsCount = view.findViewById(R.id.viewsCount)
        shareCountText = view.findViewById(R.id.shareCountText)
        seekBar = view.findViewById(R.id.waveSeekBar)
        artworkLayout = view.findViewById(R.id.artworkLayout)
        documentScrollView = view.findViewById(R.id.documentScrollView)
        documentWebView = view.findViewById(R.id.documentWebView)

        // Setup WebView settings
        documentWebView?.apply {
            settings.javaScriptEnabled = true
            loadUrl("https://example.com")
        }

        // Initialize other views
        initializeAllViews(view)

        // Setup ViewPager and video controls
        setupViewPager(view)
        setupVideoPlayer()
        setupVideoControls()
        loadInitialPost()
        setupHeaderProfileClick()
        setupFollowButton()
        setupFollowObserver()
        setupViewPagerPageChangeListener()

        // Get the post item from arguments or fallback to null
        val postItem = arguments?.getParcelable<PostItem>("current_post_item")

        // Extract postId safely
        val postId = postItem?.postId ?: arguments?.getString("post_id")

        if (postId == null) {
            Log.e(TAG, "Post ID must not be null!")
            // Handle error: for example, close fragment or show message
            requireActivity().onBackPressedDispatcher.onBackPressed()
            return
        }

        // Load post content with a valid postId
        loadPostContent(postId)

        var isNavigating = false

        cancelButton.setOnClickListener {
            if (isNavigating) return@setOnClickListener
            isNavigating = true

            try {
                // For Cleaning up resources before navigating back
                cleanupResources()

                // For Restore system bars
                restoreSystemBars()

                // Show app bar if it was hidden
                EventBus.getDefault().post(ShowAppBar(true))
                EventBus.getDefault().post(ShowBottomNav(true))

                // Use the activity's onBackPressedDispatcher for proper back navigation
                requireActivity().onBackPressedDispatcher.onBackPressed()

            } catch (e: Exception) {
                Log.e(TAG, "Error during back navigation", e)
                // Fallback to fragment manager if onBackPressed fails
                try {
                    parentFragmentManager.popBackStack()
                } catch (fallbackException: Exception) {
                    Log.e(TAG, "Fallback navigation also failed", fallbackException)
                }
            } finally {
                // Reset navigation flag after a delay to prevent rapid clicks
                Handler(Looper.getMainLooper()).postDelayed({
                    if (isAdded) { // Check if fragment is still attached
                        isNavigating = false
                    }
                }, 100)
            }
        }
    }

    private fun initializeApiService() {
        if (!::retrofitInstance.isInitialized) {
            val localStorage = LocalStorage(requireContext())
            retrofitInstance = RetrofitInstance(localStorage, requireContext())
        }
        apiService = retrofitInstance.apiService
    }

    private fun initializeAllViews(view: View) {

        authorNameTextView = view.findViewById(R.id.authorName)
        authorUsernameTextView = view.findViewById(R.id.authorUsername)
        authorAvatarImageView = view.findViewById(R.id.authorAvatar)
        authorVerifiedBadge = view.findViewById(R.id.verifiedBadge)

        // Initialize UI Controls
        cancelButton = view.findViewById(R.id.cancelButton)
        headerMenuButton = view.findViewById(R.id.headerMenuButton)
        dotsIndicator = view.findViewById(R.id.dotsIndicator)
        replyInput = view.findViewById(R.id.replyInput)

        // Initialize video components
        videoView = view.findViewById(R.id.videoView)
        videoProgressBar = view.findViewById(R.id.videoProgressBar)
        videoThumbnail = view.findViewById(R.id.videoThumbnail)

        // Initialize UI components
        commentCountTextView = view.findViewById(R.id.commentCount)
        repostCountTextView = view.findViewById(R.id.repostCount)
        likesCount = view.findViewById(R.id.likesCount)
        viewsCount = view.findViewById(R.id.viewsCount)
        shareCountText = view.findViewById(R.id.shareCountText)
        seekBar = view.findViewById(R.id.waveSeekBar)
        artworkLayout = view.findViewById(R.id.artworkLayout)

        // Initialize other UI controls
        cancelButton = view.findViewById(R.id.cancelButton)
        headerMenuButton = view.findViewById(R.id.headerMenuButton)
        dotsIndicator = view.findViewById(R.id.dotsIndicator)
        replyInput = view.findViewById(R.id.replyInput)

        // Initialize action sections
        commentSection = view.findViewById(R.id.commentLayout)
        retweetSection = view.findViewById(R.id.repostSection)
        likeSection = view.findViewById(R.id.like_layout)
        viewsSection = view.findViewById(R.id.viewsSection)
        shareSection = view.findViewById(R.id.share_layout)

        // Initialize icons
        commentIcon = view.findViewById(R.id.commentButtonIcon)
        reFeedIcon = view.findViewById(R.id.repostPost)
        likeIcon = view.findViewById(R.id.likeButtonIcon)
        viewsIcon = view.findViewById(R.id.views)
        shareIcon = view.findViewById(R.id.shareButtonIcon)

        // Initialize ViewPager
        viewPager = view.findViewById(R.id.viewPager)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Click on avatar
        authorAvatarImageView?.setOnClickListener {
            navigateToAuthorProfile()
        }

        // Click on name
        authorNameTextView?.setOnClickListener {
            navigateToAuthorProfile()
        }

        // Click on username
        authorUsernameTextView?.setOnClickListener {
            navigateToAuthorProfile()
        }

        // Menu button click listener
        headerMenuButton.setOnClickListener { view ->
            showOptionsMenu(view)
        }

        // Action section click listeners
        commentSection.setOnClickListener {
            handleCommentClick()
        }

        retweetSection.setOnClickListener {
            handleRetweetClick()
        }

        likeSection.setOnClickListener {
            handleLikeClick()
        }

        viewsSection.setOnClickListener {
            handleViewsClick()
        }

        shareSection.setOnClickListener {
            handleShareClick()
        }
    }

    private fun loadAuthorDetails(post: PostItem) {
        val userId = post.userId
        val username = post.username

        // ADD DEBUG
        val myUserId = LocalStorage.getInstance(requireContext()).getUserId()
        val myUsername = LocalStorage.getInstance(requireContext()).getUsername()
        Log.d(TAG, "MY USER ID: $myUserId")
        Log.d(TAG, "MY USERNAME: $myUsername")
        Log.d(TAG, "POST USER ID: $userId")
        Log.d(TAG, "POST USERNAME: $username")
        Log.d(TAG, "IDs MATCH: ${myUserId == userId}")
        Log.d(TAG, "USERNAMES MATCH: ${myUsername?.trim()?.lowercase() == username?.trim()?.lowercase()}")

        Log.d(TAG, "loadAuthorDetails called - userId: $userId, username: $username")

        if (userId.isNullOrEmpty() && username.isNullOrEmpty()) {
            Log.w(TAG, "No userId or username available")
            showDefaultAuthorInfo()
            return
        }

        // Set username immediately from post data
        authorUsername = username

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = when {
                    !username.isNullOrEmpty() -> {
                        Log.d(TAG, "Fetching profile by username: $username")
                        apiService.getOtherUsersProfileByUsername(username)
                    }
                    !userId.isNullOrEmpty() -> {
                        Log.d(TAG, "Fetching profile by userId: $userId")
                        apiService.getOtherUsersProfileByUsername(userId)
                    }
                    else -> {
                        withContext(Dispatchers.Main) { showDefaultAuthorInfo() }
                        return@launch
                    }
                }

                if (response.isSuccessful) {
                    val profileData = response.body()?.data
                    if (profileData != null) {
                        val firstName = extractFieldValue(profileData, "firstName", "first_name") ?: ""
                        val lastName = extractFieldValue(profileData, "lastName", "last_name") ?: ""

                        authorName = "$firstName $lastName".trim().takeIf { it.isNotEmpty() } ?: "Unknown User"

                        val apiUsername = extractFieldValue(profileData, "username")
                            ?: extractNestedFieldValue(profileData, "account", "username")

                        if (!apiUsername.isNullOrEmpty()) {
                            authorUsername = apiUsername
                        }
                        if (authorUsername.isNullOrEmpty()) {
                            authorUsername = username
                        }

                        authorAvatarUrl = extractNestedFieldValue(profileData, "account", "avatar", "url")
                        isAuthorVerified = extractFieldValue(profileData, "isVerified")?.toBoolean() ?: false

                        Log.d(TAG, "Author details loaded - Name: $authorName, Username: $authorUsername, Avatar: $authorAvatarUrl")

                        withContext(Dispatchers.Main) {
                            updateAuthorUI()
                        }
                    } else {
                        Log.w(TAG, "Profile data is null")
                        withContext(Dispatchers.Main) {
                            updateAuthorUI()
                        }
                    }
                } else {
                    Log.e(TAG, "API call failed with code: ${response.code()}")
                    withContext(Dispatchers.Main) {
                        updateAuthorUI()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading author details", e)
                withContext(Dispatchers.Main) {
                    updateAuthorUI()
                }
            }
        }
    }

    private fun updateAuthorUI() {

        updateFollowButtonVisibility()

        val displayName = authorName ?: "Unknown User"
        val displayUsername = authorUsername?.let { "@$it" } ?: ""

        Log.d(TAG, "updateAuthorUI - Name: $displayName, Username: $displayUsername")

        // Update hidden TextViews (for compatibility)
        authorNameTextView?.text = displayName
        authorUsernameTextView?.text = displayUsername

        // Update VISIBLE header TextViews
        view?.findViewById<TextView>(R.id.fullNameTextView)?.apply {
            text = displayName
            visibility = View.VISIBLE
        }

        view?.findViewById<TextView>(R.id.usernameTextView)?.apply {
            text = displayUsername
            visibility = if (displayUsername.isNotEmpty()) View.VISIBLE else View.GONE
        }

        // Load avatar
        view?.findViewById<ImageView>(R.id.userProfileIcon)?.let { imageView ->
            if (!authorAvatarUrl.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(authorAvatarUrl)
                    .placeholder(R.drawable.flash21)
                    .error(R.drawable.flash21)
                    .circleCrop()
                    .into(imageView)
            } else {
                imageView.setImageResource(R.drawable.flash21)
            }
        }

        // Show/hide verified badge
        view?.findViewById<ImageView>(R.id.verifiedBadge)?.apply {
            visibility = if (isAuthorVerified) View.VISIBLE else View.GONE
        }

    }

    private fun showDefaultAuthorInfo() {
        // Update hidden TextViews
        authorNameTextView?.text = "Unknown User"
        authorUsernameTextView?.text = ""

        // Update visible header TextViews
        val headerFullName = view?.findViewById<TextView>(R.id.fullNameTextView)
        val headerUsername = view?.findViewById<TextView>(R.id.usernameTextView)
        val headerProfileIcon = view?.findViewById<ImageView>(R.id.userProfileIcon)
        val verifiedBadge = view?.findViewById<ImageView>(R.id.verifiedBadge)
        val followButton = view?.findViewById<Button>(R.id.followButton)

        headerFullName?.text = "Unknown User"
        headerUsername?.text = ""
        headerUsername?.visibility = View.GONE
        headerProfileIcon?.setImageResource(R.drawable.flash21)
        verifiedBadge?.visibility = View.GONE

        // ALWAYS hide follow button
        followButton?.visibility = View.GONE
    }

    private fun loadMyFollowersList() {
        try {
            val cachedFollowers = FeedAdapter.getCachedFollowiList()
            val cachedFollowersUsernames = FeedAdapter.getCachedFollowersUsernames()

            myFollowersList.clear()
            myFollowersList.addAll(cachedFollowers)

            myFollowersUsernames.clear()
            myFollowersUsernames.addAll(cachedFollowersUsernames)

            Log.d(TAG, "Loaded ${myFollowersList.size} followers")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading followers: ${e.message}")
        }
    }

    // ========== SMART FOLLOWER CHECK ==========
    private fun checkIfUserFollowsBack(feedOwnerId: String, feedOwnerUsername: String? = null): Boolean {
        // Clean username - remove @ symbol and trim
        val cleanUsername = feedOwnerUsername?.replace("@", "")?.trim()?.lowercase()

        Log.d(TAG, "Checking if follows back - ID: $feedOwnerId, Username: $cleanUsername")
        Log.d(TAG, "My followers IDs: $myFollowersList")
        Log.d(TAG, "My followers usernames: $myFollowersUsernames")

        // Check 1: Exact ID match
        if (myFollowersList.contains(feedOwnerId)) {
            Log.d(TAG, "✅ Match found - Exact ID match")
            return true
        }

        // Check 2: Exact username match (100%) - PRIORITY CHECK
        if (!cleanUsername.isNullOrEmpty()) {
            val usernameMatch = myFollowersUsernames.any { followerUsername ->
                val cleanFollowerUsername = followerUsername.replace("@", "").trim().lowercase()
                cleanFollowerUsername == cleanUsername
            }
            if (usernameMatch) {
                Log.d(TAG, "✅ Match found - Username match: $cleanUsername")
                return true
            }
        }

        // Check 3: FeedAdapter cache
        if (FeedAdapter.isUserInMyFollowersList(feedOwnerId)) {
            Log.d(TAG, "✅ Match found - FeedAdapter cache")
            return true
        }

        // Check 4: Fuzzy ID match (90%) + Username match (100%)
        if (!cleanUsername.isNullOrEmpty() && feedOwnerId.length >= 20) {
            var bestMatchScore = 0.0

            myFollowersList.forEach { followerId ->
                val similarity = calculateIdSimilarity(feedOwnerId, followerId)
                if (similarity > bestMatchScore) {
                    bestMatchScore = similarity
                }
            }

            // If 90%+ ID match AND username matches exactly
            if (bestMatchScore >= 0.90) {
                val usernameMatch = myFollowersUsernames.any { followerUsername ->
                    val cleanFollowerUsername = followerUsername.replace("@", "").trim().lowercase()
                    cleanFollowerUsername == cleanUsername
                }
                if (usernameMatch) {
                    Log.d(TAG, "✅ Match found - Fuzzy ID (${(bestMatchScore * 100).toInt()}%) + Username")
                    return true
                }
            }
        }

        Log.d(TAG, "❌ No match found")
        return false
    }

    // ========== SIMILARITY CALCULATION ==========
    private fun calculateIdSimilarity(id1: String, id2: String): Double {
        if (id1 == id2) return 1.0
        if (id1.isEmpty() || id2.isEmpty()) return 0.0

        val maxLength = maxOf(id1.length, id2.length)
        val distance = levenshteinDistance(id1, id2)

        return 1.0 - (distance.toDouble() / maxLength.toDouble())
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val len1 = s1.length
        val len2 = s2.length
        val matrix = Array(len1 + 1) { IntArray(len2 + 1) }

        for (i in 0..len1) matrix[i][0] = i
        for (j in 0..len2) matrix[0][j] = j

        for (i in 1..len1) {
            for (j in 1..len2) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                matrix[i][j] = minOf(
                    matrix[i - 1][j] + 1,
                    matrix[i][j - 1] + 1,
                    matrix[i - 1][j - 1] + cost
                )
            }
        }

        return matrix[len1][len2]
    }

    // ========== FOLLOW BUTTON CLICK ==========
    private fun handleFollowButtonClick(followButton: Button, feedOwnerId: String, feedOwnerUsername: String) {
        try {
            YoYo.with(Techniques.Pulse).duration(300).playOn(followButton)
        } catch (e: Exception) {
            Log.w(TAG, "Animation unavailable: ${e.message}")
        }

        val isFollowed = followingUserIds.contains(feedOwnerId)
        val newFollowStatus = !isFollowed
        val followEntity = FollowUnFollowEntity(feedOwnerId, newFollowStatus)

        if (newFollowStatus) {
            // NOW FOLLOWING - Hide button
            followButton.visibility = View.GONE
            followingUserIds.add(feedOwnerId)

            FeedAdapter.addToFollowingCache(feedOwnerId)
            FeedAdapter.setCachedFollowingList(followingUserIds)
            followingManager.addToFollowing(feedOwnerId)

            Log.d(TAG, "Following @$feedOwnerUsername")
        } else {
            // UNFOLLOWED - Check if they follow you
            val theyFollowMe = checkIfUserFollowsBack(feedOwnerId, feedOwnerUsername)

            followButton.text = if (theyFollowMe) "Follow Back" else "Follow"
            followButton.visibility = View.VISIBLE

            followingUserIds.remove(feedOwnerId)
            FeedAdapter.removeFromFollowingCache(feedOwnerId)
            FeedAdapter.setCachedFollowingList(followingUserIds)
            followingManager.removeFromFollowing(feedOwnerId)

            Log.d(TAG, "Unfollowed @$feedOwnerUsername")
        }

        EventBus.getDefault().post(ShortsFollowButtonClicked(followEntity))
        followUnfollowViewModel.followUnFollow(feedOwnerId)
    }

    // ========== UPDATE FOLLOW BUTTON VISIBILITY ==========
    private fun updateFollowButtonVisibility() {
        val followButton = view?.findViewById<Button>(R.id.followButton) ?: return
        val post = postList?.getOrNull(viewPager.currentItem)
        val feedOwnerId = post?.userId
        val feedOwnerUsername = post?.username

        val localStorage = LocalStorage.getInstance(requireContext())
        val currentUserId = localStorage.getUserId()
        val currentUsername = localStorage.getUsername()?.trim()?.lowercase()
        val feedOwnerUsernameLower = feedOwnerUsername?.trim()?.lowercase()

        // CASE 1: Own post (username match)
        if (!currentUsername.isNullOrEmpty() &&
            !feedOwnerUsernameLower.isNullOrEmpty() &&
            currentUsername == feedOwnerUsernameLower) {
            followButton.visibility = View.GONE
            return
        }

        // CASE 2: Own post (ID match)
        if (feedOwnerId != null && feedOwnerId == currentUserId) {
            followButton.visibility = View.GONE
            return
        }

        // CASE 3: No feed owner
        if (feedOwnerId == null) {
            followButton.visibility = View.GONE
            return
        }

        // CASE 4: Already following them - HIDE button
        val isFollowing = followingUserIds.contains(feedOwnerId)
        if (isFollowing) {
            followButton.visibility = View.GONE
            Log.d(TAG, "Button hidden - already following")
            return
        }

        // CASE 5: Not following them - Check if THEY follow YOU
        val theyFollowMe = checkIfUserFollowsBack(feedOwnerId, feedOwnerUsername)

        followButton.visibility = View.VISIBLE

        if (theyFollowMe) {
            // They follow you, you don't follow them → "Follow Back"
            followButton.text = "Follow Back"
            Log.d(TAG, "Button: 'Follow Back' - @$feedOwnerUsername follows you")
        } else {
            // Neither follows each other → "Follow"
            followButton.text = "Follow"
            Log.d(TAG, "Button: 'Follow' - no mutual following")
        }
    }

    // ========== SETUP FOLLOW BUTTON ==========
    private fun setupFollowButton() {
        val followButton = view?.findViewById<Button>(R.id.followButton) ?: return

        followButton.setOnClickListener {
            val post = postList?.getOrNull(viewPager.currentItem) ?: return@setOnClickListener
            val feedOwnerId = post.userId ?: return@setOnClickListener
            val feedOwnerUsername = post.username ?: "unknown"

            if (!isValidUserId(feedOwnerId)) {
                Log.e(TAG, "Invalid user ID: $feedOwnerId")
                Toast.makeText(requireContext(), "Error: Invalid user ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            handleFollowButtonClick(followButton, feedOwnerId, feedOwnerUsername)
        }
    }

    // ========== LOAD FOLLOWING LIST ==========
    private fun loadFollowingListFromCache() {
        try {
            val cachedFollowingIds = FeedAdapter.getCachedFollowingList()
            followingUserIds.clear()
            followingUserIds.addAll(cachedFollowingIds)

            Log.d(TAG, "Loaded ${followingUserIds.size} following")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading following: ${e.message}")
        }

        // Backup from FollowingManager
        try {
            val managerFollowingIds = followingManager.getFollowingList()
            managerFollowingIds.forEach { userId ->
                if (!followingUserIds.contains(userId)) {
                    followingUserIds.add(userId)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading from manager: ${e.message}")
        }
    }

    // ========== FOLLOW OBSERVER ==========
    private fun setupFollowObserver() {
        followUnfollowViewModel.followUnFollowObserver().observe(viewLifecycleOwner) { isFollowing ->
            if (!isAdded || view == null) return@observe

            val post = postList?.getOrNull(viewPager.currentItem) ?: return@observe
            val feedOwnerUsername = post.username ?: "unknown"

            val message = if (isFollowing) {
                "Now following @$feedOwnerUsername ✓"
            } else {
                "Unfollowed @$feedOwnerUsername"
            }
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

            updateFollowButtonVisibility()
        }
    }

    // ========== PAGE CHANGE LISTENER ==========
    private fun setupViewPagerPageChangeListener() {
        pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                updateFollowButtonVisibility()

                val post = postList?.getOrNull(position)
                if (post != null) {
                    loadAuthorDetails(post)
                }
            }
        }
        viewPager.registerOnPageChangeCallback(pageChangeCallback)
    }

    // ========== VALIDATION ==========
    private fun isValidUserId(userId: String?): Boolean {
        if (userId.isNullOrEmpty()) return false
        if (userId.length != 24) return false
        if (!userId.matches(Regex("^[a-fA-F0-9]{24}$"))) return false
        return true
    }



    private fun loadPostContent(postId: String) {
        // Fetch post data by ID from your post list
        val post = postList?.find { it.postId == postId } ?: return

        // Load author details using the userId from the post
        post.postId?.let { userId ->
            loadAuthorDetails(post)
        } ?: run {
            Log.w(TAG, "No userId found for post $postId")
            showDefaultAuthorInfo()
        }

        val firstFile = post.files?.firstOrNull()
        if (firstFile != null) {
            // displayMediaPreview(firstFile)
        } else {
            Log.w(TAG, "No media file found for post $postId")
        }
    }

    private fun setupHeaderProfileClick() {
        val headerProfileIcon = view?.findViewById<ImageView>(R.id.userProfileIcon)
        val headerFullName = view?.findViewById<TextView>(R.id.fullNameTextView)
        val headerUsername = view?.findViewById<TextView>(R.id.usernameTextView)

        headerProfileIcon?.setOnClickListener { navigateToAuthorProfile() }
        headerFullName?.setOnClickListener { navigateToAuthorProfile() }
        headerUsername?.setOnClickListener { navigateToAuthorProfile() }
    }

    @OptIn(UnstableApi::class)
    private fun navigateToAuthorProfile() {
        val post = postList?.getOrNull(viewPager.currentItem) ?: return

        val userId = post.userId
        val username = post.username

        if (userId == null) {
            Toast.makeText(requireContext(), "User not available", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(requireContext(), OtherUserProfileAccount::class.java).apply {
            putExtra("extra_user_id", userId)
            putExtra("extra_username", username ?: "")
            putExtra("extra_user_name", authorName ?: "")
            putExtra("user_full_name", authorName ?: "")
            putExtra("extra_avatar_url", authorAvatarUrl)
        }

        startActivity(intent)
    }

    private fun extractArguments() {
        arguments?.let { bundle ->
            postId = bundle.getString(ARG_POST_ID) ?: bundle.getString("post_id")
            postData = bundle.getString(ARG_POST_DATA)
            postList = bundle.getParcelableArrayList(ARG_POST_LIST) ?:
                    bundle.getParcelableArrayList("post_list")
            currentPosition = bundle.getInt(ARG_CURRENT_POSITION,
                bundle.getInt("current_index", 0))

            // LOAD FOLLOWING LIST FROM ARGUMENTS (passed from adapter)
            val followingIds = bundle.getStringArrayList("following_ids")

            if (!followingIds.isNullOrEmpty()) {
                followingUserIds = followingIds.toMutableSet()
                Log.d(TAG, "Following IDs loaded from arguments: ${followingUserIds.size}")
                Log.d(TAG, "Following IDs: $followingUserIds")
            } else {
                // Fallback to FeedAdapter cache
                followingUserIds = FeedAdapter.getCachedFollowingList().toMutableSet()
                Log.d(TAG, "Using FeedAdapter cache: ${followingUserIds.size} IDs")
                Log.d(TAG, "Cache IDs: $followingUserIds")
            }

            // If still empty, load from FollowingManager (localStorage first, then server)
            if (followingUserIds.isEmpty()) {
                // Load from localStorage synchronously first
                followingUserIds = followingManager.getFollowingList().toMutableSet()
                Log.d(TAG, "Loaded from localStorage: ${followingUserIds.size} IDs")

                // Then fetch fresh data from server in background
                lifecycleScope.launch {
                    try {
                        val serverList = followingManager.loadFollowingList()
                        followingUserIds = serverList.toMutableSet()
                        Log.d(TAG, "Loaded from server: ${followingUserIds.size} IDs")

                        // Update UI after loading from server
                        updateFollowButtonVisibility()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error loading from server: ${e.message}")
                    }
                }
            }

            Log.d(TAG, "Arguments extracted - PostList size: ${postList?.size}, CurrentPosition: $currentPosition")
            Log.d(TAG, "INITIAL Following List Size: ${followingUserIds.size}")
        }
    }

    private fun loadInitialPost() {
        postList?.let { posts ->
            if (posts.isNotEmpty() && currentPosition < posts.size) {
                val post = posts[currentPosition]
                postId = post.postId

                // Load author details
                post.postId?.let { userId ->
                    loadAuthorDetails(post)
                }

                loadPostMetrics(postId!!)
                loadPostContent(postId!!)
                updateUI()

                // Set current item AFTER adapter is set
                viewPager.setCurrentItem(currentPosition, false)
                Log.d(TAG, "Initial post loaded: ${post.postId}")
            }
        }
    }

    //  VIEWPAGER SETUP

    private fun setupViewPager(view: View) {
        viewPager = view.findViewById(R.id.viewPager)

        view.post {
            // Set up the adapter ONLY ONCE
            postList?.let { posts ->
                if (posts.isNotEmpty()) {
                    val adapter = PostPagerAdapter(requireActivity(), posts)
                    viewPager.adapter = adapter
                    Log.d(TAG, "ViewPager Adapter set with ${posts.size} Posts")
                } else {
                    Log.w(TAG, "Post list is empty, cannot set Adapter")
                }
            } ?: run {
                Log.e(TAG, "Post list is null, cannot set Adapter")
            }

            // Set up page change callback
            pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {

                override fun onPageSelected(position: Int) {
                    Log.d(TAG, "Page selected: $position")
                    val post = postList?.getOrNull(position)
                    postId = post?.postId

                    // Load author details for new post
                    post?.postId?.let { userId ->
                        loadAuthorDetails(post)
                    }

                    postId?.let {
                        loadPostMetrics(it)
                        loadPostContent(it)
                        updateUI()
                    }
                    updateFollowButtonVisibility()
                }
            }

            viewPager.registerOnPageChangeCallback(pageChangeCallback)
        }
    }

    //  HELPER METHODS

    private fun extractFieldValue(obj: Any, vararg fieldNames: String): String? {
        for (fieldName in fieldNames) {
            try {
                val field = obj.javaClass.getDeclaredField(fieldName)
                field.isAccessible = true
                val value = field.get(obj)
                return when (value) {
                    null -> null
                    is String -> value
                    else -> value.toString()
                }
            } catch (e: NoSuchFieldException) {
                continue
            } catch (e: Exception) {
                Log.w(TAG, "Error accessing field '$fieldName': ${e.message}")
                continue
            }
        }
        return null
    }

    private fun extractNestedFieldValue(obj: Any, vararg fieldPath: String): String? {
        try {
            var currentObj: Any? = obj
            for (i in 0 until fieldPath.size - 1) {
                val field = currentObj?.javaClass?.getDeclaredField(fieldPath[i])
                field?.isAccessible = true
                currentObj = field?.get(currentObj)
                if (currentObj == null) return null
            }

            val finalField = currentObj?.javaClass?.getDeclaredField(fieldPath.last())
            finalField?.isAccessible = true
            val value = finalField?.get(currentObj)?.toString()
            return if (value != "null") value else null
        } catch (e: Exception) {
            return null
        }
    }


    // Add these handler methods for the action sections
    private fun handleCommentClick() {
        // Navigate to comments or show comment dialog
        Toast.makeText(requireContext(), "Comments: $commentCount", Toast.LENGTH_SHORT).show()
    }

    private fun handleRetweetClick() {
        // Toggle repost state
        isReposted = !isReposted
        if (isReposted) {
            repostCount++
            reFeedIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.repost_color))
        } else {
            repostCount--
            reFeedIcon.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.white))
        }
        repostCountTextView.text = formatCount(repostCount)
        Toast.makeText(requireContext(), if (isReposted) "Reposted" else "Repost removed", Toast.LENGTH_SHORT).show()
    }

    private fun handleLikeClick() {
        // Add haptic feedback
        view?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

        // Toggle like state
        isLiked = !isLiked
        if (isLiked) {
            likeCount++
            likeIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.like_color))
            // Add like animation here if needed
        } else {
            likeCount--
            likeIcon.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.white))
        }
        likesCount.text = formatCount(likeCount)
    }

    private fun handleViewsClick() {
        // Show view details or analytics
        Toast.makeText(requireContext(), "Views: $viewCount", Toast.LENGTH_SHORT).show()
    }

    private fun handleShareClick() {
        // Show share options
        shareCount++
        shareCountText.text = formatCount(shareCount)
        showShareOptions()
    }

    private fun showShareOptions() {
        // Create and show share intent or custom share dialog
        Toast.makeText(requireContext(), "Share options", Toast.LENGTH_SHORT).show()
    }

    private fun showOptionsMenu(anchor: View) {
        val context = requireContext()
        val popup = PopupMenu(context, anchor, Gravity.END)

        // Apply custom style and force show icons (optional)
        try {
            val fieldPopup = PopupMenu::class.java.getDeclaredField("mPopup")
            fieldPopup.isAccessible = true
            val menuPopupWindow = fieldPopup.get(popup)

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

    private fun hideSystemBars() {
        val window = requireActivity().window

        // Hide status bar and navigation bar
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.let { controller ->
            // Hide both status bar and navigation bar
            controller.hide(WindowInsetsCompat.Type.systemBars())

            // Set behavior for when user swipes to show bars
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // Keep screen on while viewing content
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun cleanupResources() {
        try {
            // Stop and release video player
            videoView.stopPlayback()

            // Release media player
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
            mediaPlayer = null

            // Clear WebView
            documentWebView?.apply {
                stopLoading()
                loadUrl("about:blank")
            }

            // Remove any pending handlers/callbacks
            // Add any other cleanup needed for your specific resources

        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up resources", e)
        }
    }

    private fun restoreSystemBars() {
        try {
            val window = requireActivity().window

            // First, restore the decor fits system windows
            WindowCompat.setDecorFitsSystemWindows(window, true)

            val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
            windowInsetsController.let { controller ->
                // Show both status bar and navigation bar
                controller.show(WindowInsetsCompat.Type.statusBars())
                controller.show(WindowInsetsCompat.Type.navigationBars())

                // Or show all system bars at once
                controller.show(WindowInsetsCompat.Type.systemBars())

                // Reset the system bars behavior to default
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
            }

            // Remove keep screen on flag
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            // Additional fix: Force the window to re-layout
            window.decorView.requestApplyInsets()

            // Alternative approach if the above doesn't work
            // You can also try clearing the system UI visibility flags directly
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE

        } catch (e: Exception) {
            Log.e(TAG, "Error restoring system bars", e)
        }
    }

    override fun onDestroyView() {
        cleanupViewPager()
        super.onDestroyView()
    }

    private fun setupVideoPlayer() {
        mediaController = MediaController(requireContext())
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)

        videoView.setOnPreparedListener { mp ->
            videoProgressBar.visibility = View.GONE
            videoThumbnail.visibility = View.GONE

            mp.setOnVideoSizeChangedListener { _, _, _ ->
                mediaController.setAnchorView(videoView)
            }
        }

    }

    private fun setupVideoControls() {
        rootView.setOnTouchListener(object : OnSwipeTouchListener(requireContext()) {
            override fun onSwipeLeft() {
                if (currentPosition < (postList?.size ?: 0) - 1) {
                    currentPosition++
                    loadVideoForCurrentPosition()
                }
            }

            override fun onSwipeRight() {
                if (currentPosition > 0) {
                    currentPosition--
                    loadVideoForCurrentPosition()
                }
            }
        })
    }

    open class OnSwipeTouchListener(ctx: Context) : View.OnTouchListener {

        private val gestureDetector = GestureDetector(ctx, GestureListener())

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            // Null-safe call
            return event?.let { gestureDetector.onTouchEvent(it) } ?: false
        }

        private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 100
            private val SWIPE_VELOCITY_THRESHOLD = 100

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null) return false

                val diffX = e2.x - e1.x
                val diffY = e2.y - e1.y

                if (abs(diffX) > abs(diffY)) {
                    if (abs(
                            diffX) > SWIPE_THRESHOLD && abs(
                            velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight()
                        } else {
                            onSwipeLeft()
                        }
                        return true
                    }
                }
                return false
            }
        }

        open fun onSwipeLeft() {}
        open fun onSwipeRight() {}
    }

    @SuppressLint("UseKtx")
    private fun loadVideoForCurrentPosition() {
        val currentPost = postList?.get(currentPosition)
        val videoUrl = currentPost?.videoUrl

        if (!videoUrl.isNullOrEmpty()) {
            videoProgressBar.visibility = View.VISIBLE
            videoThumbnail.visibility = View.VISIBLE


            Glide.with(requireContext())
                .load(currentPost.videoThumbnailUrl)
                .into(videoThumbnail)

            videoView.setVideoURI(videoUrl.toUri())
            isVideoPlaying = false

        } else {
            videoView.visibility = View.GONE
            videoThumbnail.visibility = View.GONE

        }
    }

    override fun onPause() {
        super.onPause()
        videoView.pause()
        currentVideoPosition = videoView.currentPosition
    }

    override fun onResume() {
        super.onResume()
        if (currentVideoPosition > 0) {
            videoView.seekTo(currentVideoPosition)
        }
    }

    private fun updateSeekBar() {
        mediaPlayer?.let { player ->
            seekBar.progress = (player.currentPosition * 100) / player.duration
            if (player.isPlaying) {
                seekBar.postDelayed({ updateSeekBar() }, 1000)
            }
        }
    }



    @SuppressLint("UseKtx", "ObsoleteSdkInt")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)



    //  INITIALIZATION METHODS

    private fun setupBackPressHandler() {

    }


    private fun cleanupViewPager() {
        if (this::pageChangeCallback.isInitialized && this::viewPager.isInitialized) {
            viewPager.unregisterOnPageChangeCallback(pageChangeCallback)
        }
    }

    //  DATA LOADING METHODS

    private fun loadPostMetrics(postId: String) {
        // Only load metrics if they are available/exist
        // Otherwise, keep default values (zero)

        if (hasMetricsData(postId)) {
            val seed = postId.hashCode()
            commentCount = generateMetricValue(10, 5000, seed)
            repostCount = generateMetricValue(5, 2000, seed + 1)
            likeCount = generateMetricValue(50, 15000, seed + 2)
            viewCount = generateMetricValue(1000, 1000000, seed + 3)
            shareCount = generateMetricValue(20, 5000, seed + 4)
        }
        // If no metrics data available, values remain at their default (zero)

        loadUserInteractionState(postId)
    }

    private fun hasMetricsData(postId: String): Boolean {
        // Add your logic to determine if this post should have metrics
        // For example: check if post is from a certain source, user type, etc.
        return false // Default to false, so metrics remain zero
    }



    private fun loadUserInteractionState(postId: String) {
        // TODO: Load from user preferences or API
        isLiked = false
        isReposted = false
    }


    //  UI UPDATE METHODS

    private fun updateUI() {
        updateCounters()
        updateInteractionStates()
    }

    private fun updateCounters() {
        commentCountTextView.text = formatCount(commentCount)
        repostCountTextView.text = formatCount(repostCount)
        likesCount.text = formatCount(likeCount)
        viewsCount.text = formatCount(viewCount)
        shareCountText.text = formatCount(shareCount)
    }

    private fun updateInteractionStates() {
        updateLikeButtonState()
        updateRepostButtonState()
    }

    private fun updateLikeButtonState() {

        if (!::likeIcon.isInitialized) {
            return
        }

        if (isLiked) {
            // Filled heart with red color
            likeIcon?.setImageResource(R.drawable.filled_favorite_like)
        } else {
            // Unfilled heart with white/gray color
            likeIcon?.setImageResource(R.drawable.favorite_svgrepo_com)

        }
    }

    private fun updateRepostButtonState() {
        val color = if (isReposted) {
            ContextCompat.getColor(requireContext(), android.R.color.holo_green_light)
        } else {
            ContextCompat.getColor(requireContext(), android.R.color.white)
        }
        reFeedIcon.setColorFilter(color)
    }



    //  UTILITY METHODS

    private fun generateMetricValue(min: Int, max: Int, seed: Int = 0): Int {
        return if (seed != 0) {
            val random = Random(seed)
            random.nextInt(min, max + 1)
        } else {
            (min..max).random()
        }
    }

    private fun formatCount(count: Int): String {
        return when {
            count >= 1_000_000 -> {
                val millions = count / 1_000_000.0
                if (millions == millions.toInt().toDouble()) {
                    "${millions.toInt()}M"
                } else {
                    String.format("%.1f", millions) + "M"
                }
            }
            count >= 1_000 -> {
                val thousands = count / 1_000.0
                if (thousands == thousands.toInt().toDouble()) {
                    "${thousands.toInt()}K"
                } else {
                    String.format("%.1f", thousands) + "K"
                }
            }
            else -> count.toString()
        }
    }


}

data class PostItem(

    val postId: String,
    val userId: String?,
    val username: String?,
    val authorName: String?,
    val avatarUrl: String?,
    val audioUrl: String?,
    val audioThumbnailUrl: String?,
    val videoUrl: String?,
    val videoThumbnailUrl: String?,
    val data: String,
    val files: ArrayList<String>? = null,
    val fileType: String = ""
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString() ?: "",
        parcel.createStringArrayList(),
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(postId)
        parcel.writeString(userId)
        parcel.writeString(username)
        parcel.writeString(authorName)
        parcel.writeString(avatarUrl)
        parcel.writeString(audioUrl)
        parcel.writeString(audioThumbnailUrl)
        parcel.writeString(videoUrl)
        parcel.writeString(videoThumbnailUrl)
        parcel.writeString(data)
        parcel.writeStringList(files)
        parcel.writeString(fileType)
    }


    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<PostItem> {
        override fun createFromParcel(parcel: Parcel): PostItem = PostItem(parcel)
        override fun newArray(size: Int): Array<PostItem?> = arrayOfNulls(size)
    }
}

//  ADAPTER CLASSES

private class PostPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val postList: List<PostItem>
) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = postList.size

    override fun createFragment(position: Int): Fragment {
        return PostContentFragment.newInstance(postList[position])
    }
}



//  ENHANCED FRAGMENT CLASSES
class PostContentFragment : Fragment() {

    companion object {
        private const val ARG_POST_ITEM = "post_item"
        private const val TAG = "PostContentFragment"
        private const val STORAGE_PERMISSION_REQUEST_CODE = 1001

        fun newInstance(postItem: PostItem): PostContentFragment {
            return PostContentFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_POST_ITEM, postItem)
                }
            }
        }
    }

    // Views - using nullable for better safety

    private var postImage: ImageView? = null
    private var audioDurationTextView: TextView? = null
    private var artworkImageView: ImageView? = null
    private var artworkLayout: LinearLayout? = null
    private var audioContainer: LinearLayout? = null
    private var videoView: VideoView? = null
    private var videoContainer: FrameLayout? = null
    private var playButton: ImageButton? = null
    private var videoPlayButton: ImageButton? = null
    private var videoProgressBar: ProgressBar? = null
    private var documentContainer: LinearLayout? = null
    private var pdfImageView: ImageView? = null
    private var fileTypeIcon: ImageView? = null
    private var documentScrollView: ScrollView? = null
    private var documentWebView: WebView? = null
    private var videoThumbnailView: ImageView? = null
    private var audioArtworkView: ImageView? = null
    private var audioTitleTextView: TextView? = null
    private var audioArtistTextView: TextView? = null
    private var downloadDocumentButton: Button? = null
    private var documentName: TextView? = null
    private var documentThumbnail: ImageView? = null
    private var screenTapHandler: Handler? = null
    private var hideIconRunnable: Runnable? = null
    private var waveSeekBar: WaveformSeekBar? = null
    private var currentTime: TextView? = null
    private var totalDuration: TextView? = null
    private var playbackSpeed: TextView? = null
    private var standardSeekBarLayout: LinearLayout? = null
    private var totalDurationStandard: TextView? = null
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private var currentPosition = 0
    private val updateHandler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null
    private var totalDurationValue: Int = 0
    private var waveAnimation: WaveformSeekBar? = null
    private var audioSeekBar: SeekBar? = null
    private var waveAnimator: ValueAnimator? = null
    private var currentAudioPath: String? = null
    private var isWaveformAnimating = false

    private var context: android.content.Context? = null
    private var rootView: View? = null
    private var mainPlayButton: ImageView? = null
    private var pauseButtonMain: ImageView? = null
    private lateinit var currentDocumentUrl: String
    private lateinit var currentFileName: String


    fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.tapped_posted_files_viewers, container, false)
    }

    @OptIn(UnstableApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        Log.d(TAG, "onViewCreated called")

        // Initialize ALL views first
        if (!initializeViews(requireContext(), view, view)) {
            Log.e(TAG, "Failed to initialize views - layout might be incorrect")
            return
        }

        // Get the post item from arguments
        val postItem = arguments?.getParcelable<PostItem>(ARG_POST_ITEM)
        if (postItem != null) {
            Log.d(TAG, "Loading post content for: ${postItem.postId}")
            Log.d(TAG, "Post files: ${postItem.files}")
            Log.d(TAG, "Post videoUrl: ${postItem.videoUrl}")
            loadPostContent(postItem)
        } else {
            Log.e(TAG, "No post item found in arguments")
            showPlaceholder()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initializeViews(context: android.content.Context, rootView: View, view: View): Boolean {

        this.context = context
        this.rootView = rootView

        try {

            // Initialize all views with null safety
            postImage = view.findViewById(R.id.postImage)
            audioDurationTextView = view.findViewById(R.id.audioDuration)
            artworkImageView = view.findViewById(R.id.artworkImageView)
            artworkLayout = view.findViewById(R.id.artworkLayout)
            audioContainer = view.findViewById(R.id.audioContainer)
            videoView = view.findViewById(R.id.videoView)
            videoContainer = view.findViewById(R.id.videoContainer)
            videoProgressBar = view.findViewById(R.id.videoProgressBar)
            documentContainer = view.findViewById(R.id.documentContainer)
            pdfImageView = view.findViewById(R.id.pdfImageView)
            fileTypeIcon = view.findViewById(R.id.fileTypeIcon)
            documentScrollView = view.findViewById(R.id.documentScrollView)
            documentWebView = view.findViewById(R.id.documentWebView)
            videoThumbnailView = view.findViewById(R.id.videoThumbnailView)
            audioArtworkView = view.findViewById(R.id.audioArtworkView)
            audioTitleTextView = view.findViewById(R.id.audioTitleTextView)
            audioArtistTextView = view.findViewById(R.id.audioArtistTextView)
            videoPlayButton = view.findViewById(R.id.videoPlayButton)
            documentThumbnail = view.findViewById(R.id.documentThumbnail)
            videoThumbnailView = view.findViewById(R.id.videoThumbnail)
            waveSeekBar = rootView.findViewById<WaveformSeekBar>(R.id.waveSeekBar)
            currentTime = view.findViewById(R.id.currentTime)
            totalDuration = view.findViewById(R.id.totalDuration)
            mainPlayButton = view.findViewById(R.id.playButtonMain)
            pauseButtonMain = view.findViewById(R.id.pauseButtonMain)



            // Make sure they're visible
            currentTime?.visibility = View.VISIBLE
            totalDuration?.visibility = View.VISIBLE
            playbackSpeed?.visibility = View.VISIBLE

            // Set initial values
            currentTime?.text = "0:00"
            totalDuration?.text = "0:00"
            playbackSpeed?.text = "1.0x"

            Log.d("AudioWaveform",
                "Time TextViews initialized - currentTime: ${currentTime != null}," +
                        " totalDuration: ${totalDuration != null}")



            // Check critical views
            if (postImage == null) {
                Log.e(TAG, "postImage not found in layout")
                return false
            }

            // Configure WebView if available
            documentWebView?.apply {
                settings.javaScriptEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                settings.domStorageEnabled = true
                settings.allowFileAccess = true
                settings.allowContentAccess = true
            }

            Log.d(TAG, "All views initialized successfully")
            return true

        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views: ${e.message}")
            return false
        }

    }

    // MAIN ENTRY POINT
    private fun loadPostContent(postItem: PostItem) {
        Log.d(TAG, "Starting loadPostContent")

        // First, ensure all views are hidden initially
        hideAllViews()

        // Enhanced file detection logic
        val fileUrls = postItem.files
        var mediaUrl: String? = null
        var mimeType: String = ""

        // Priority: Files array first, then videoUrl
        when {
            !fileUrls.isNullOrEmpty() -> {
                mediaUrl = fileUrls[0]
                mimeType = determineMimeTypeFromUrl(mediaUrl)
            }

            !postItem.audioUrl.isNullOrEmpty() -> {
                mediaUrl = postItem.audioUrl  // FIXED: was postItem.videoUrl
                mimeType = "audio/mp3"
            }
            !postItem.audioThumbnailUrl.isNullOrEmpty() -> {
                mediaUrl = postItem.audioThumbnailUrl  // FIXED: was postItem.videoThumbnailUrl
                mimeType = "image/jpeg"
            }

            !postItem.videoUrl.isNullOrEmpty() -> {
                mediaUrl = postItem.videoUrl
                mimeType = "video/mp4"
            }
            !postItem.videoThumbnailUrl.isNullOrEmpty() -> {
                mediaUrl = postItem.videoThumbnailUrl
                mimeType = "image/jpeg"
            }
        }

        if (mediaUrl.isNullOrEmpty()) {
            Log.w(TAG, "No media found for post: ${postItem.postId}")
            showPlaceholder()
            return
        }

        Log.d(TAG, "Processing media URL: $mediaUrl with MIME type: $mimeType")

        // Load content based on MIME type
        when {
            mimeType.startsWith("image") -> {
                Log.d(TAG, "Detected as IMAGE file")
                loadImageContent(mediaUrl)
            }
            mimeType.startsWith("video") -> {
                Log.d(TAG, "Detected as VIDEO file")
                loadVideoContent(mediaUrl, extractFileNameFromUrl(mediaUrl), postItem)
            }
            mimeType.startsWith("audio") -> {
                Log.d(TAG, "Detected as AUDIO file")
                loadAudioContent(mediaUrl, extractFileNameFromUrl(mediaUrl), postItem)
            }
            mimeType.contains("pdf") || mimeType.contains("docx") ||
                    mimeType.contains("pptx") || mimeType.contains("xlsx") ||
                    mimeType.contains("txt") || mimeType.contains("csv") -> {
                Log.d(TAG, "Detected as DOCUMENT file")
                loadDocumentContent(mediaUrl, extractFileNameFromUrl(mediaUrl), postItem)
            }
            else -> {
                Log.w(TAG, "Unknown media type: $mimeType, defaulting to image")
                loadImageContent(mediaUrl)
            }
        }
    }

    private fun showPlaceholder() {
        hideAllViews()
        // Show some placeholder content
        postImage?.apply {
            visibility = View.VISIBLE
            setImageResource(R.drawable.flash21)
        }
    }

//  UTILITY METHODS

    private fun determineMimeTypeFromUrl(url: String): String {
        val fileName = extractFileNameFromUrl(url).lowercase()

        return when {
            // Image extensions
            fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") -> "image/jpeg"
            fileName.endsWith(".png") -> "image/png"
            fileName.endsWith(".gif") -> "image/gif"
            fileName.endsWith(".webp") -> "image/webp"
            fileName.endsWith(".bmp") -> "image/bmp"
            fileName.endsWith(".svg") -> "image/svg+xml"

            // Video extensions
            fileName.endsWith(".mp4") -> "video/mp4"
            fileName.endsWith(".webm") -> "video/webm"
            fileName.endsWith(".avi") -> "video/avi"
            fileName.endsWith(".mov") -> "video/mov"
            fileName.endsWith(".mkv") -> "video/mkv"
            fileName.endsWith(".3gp") -> "video/3gp"

            // Audio extensions
            fileName.endsWith(".mp3") -> "audio/mp3"
            fileName.endsWith(".m4a") -> "audio/m4a"
            fileName.endsWith(".wav") -> "audio/wav"
            fileName.endsWith(".ogg") -> "audio/ogg"
            fileName.endsWith(".flac") -> "audio/flac"

            // Document extensions
            fileName.endsWith(".pdf") -> "application/pdf"
            fileName.endsWith(".docx") -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            fileName.endsWith(".pptx") -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            fileName.endsWith(".xlsx") -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            fileName.endsWith(".txt") -> "text/plain"
            fileName.endsWith(".csv") -> "text/csv"

            else -> "application/octet-stream"
        }
    }

    private fun getMediaType(fileName: String): MediaType {
        val lowerFileName = fileName.lowercase()

        return when {

            // Image files
            lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg") ||
                    lowerFileName.endsWith(".png") || lowerFileName.endsWith(".gif") ||
                    lowerFileName.endsWith(".bmp") || lowerFileName.endsWith(".webp") ||
                    lowerFileName.endsWith(".tiff") || lowerFileName.endsWith(".tif") ||
                    lowerFileName.endsWith(".svg") || lowerFileName.endsWith(".heic") ||
                    lowerFileName.endsWith(".heif") -> MediaType.IMAGE

            // Video files
            lowerFileName.endsWith(".mp4") || lowerFileName.endsWith(".avi") ||
                    lowerFileName.endsWith(".mov") || lowerFileName.endsWith(".wmv") ||
                    lowerFileName.endsWith(".flv") || lowerFileName.endsWith(".webm") ||
                    lowerFileName.endsWith(".mkv") || lowerFileName.endsWith(".3gp") ||
                    lowerFileName.endsWith(".m4v") || lowerFileName.endsWith(".mpeg") ||
                    lowerFileName.endsWith(".mpg") -> MediaType.VIDEO

            // Audio files
            lowerFileName.endsWith(".mp3") || lowerFileName.endsWith(".m4a") ||
                    lowerFileName.endsWith(".aac") || lowerFileName.endsWith(".ogg") ||
                    lowerFileName.endsWith(".wav") || lowerFileName.endsWith(".flac") ||
                    lowerFileName.endsWith(".amr") || lowerFileName.endsWith(".opus") -> MediaType.AUDIO

            // Document files
            lowerFileName.endsWith(".pdf") || lowerFileName.endsWith(".doc") ||
                    lowerFileName.endsWith(".docx") || lowerFileName.endsWith(".ppt") ||
                    lowerFileName.endsWith(".pptx") || lowerFileName.endsWith(".xls") ||
                    lowerFileName.endsWith(".xlsx") || lowerFileName.endsWith(".txt") ||
                    lowerFileName.endsWith(".rtf") || lowerFileName.endsWith(".odt") ||
                    lowerFileName.endsWith(".csv") -> MediaType.DOCUMENT

            else -> MediaType.UNKNOWN
        }
    }

    private fun extractFileNameFromUrl(url: String): String {

        return try {
            val cleanUrl = url.split("?")[0]
            cleanUrl.substringAfterLast("/")
        } catch (e: Exception) {
            Log.w(TAG, "Error extracting filename from URL: $url", e)
            "unknown_file"
        }
    }

    private fun formatDuration(milliseconds: Long): String {
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / (1000 * 60)) % 60
        val hours = (milliseconds / (1000 * 60 * 60)) % 24

        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }

    // VIEW MANAGEMENT

    private fun hideAllViews() {

        postImage?.visibility = View.GONE
        videoContainer?.visibility = View.GONE
        videoView?.visibility = View.GONE
        playButton?.visibility = View.GONE
        videoProgressBar?.visibility = View.GONE
        audioContainer?.visibility = View.GONE
        documentContainer?.visibility = View.GONE
        documentWebView?.visibility = View.GONE
        pdfImageView?.visibility = View.GONE
        fileTypeIcon?.visibility = View.GONE
        artworkLayout?.visibility = View.GONE
        audioDurationTextView?.visibility = View.GONE

        // audio controls
        waveSeekBar?.visibility = View.GONE
        currentTime?.visibility = View.GONE
        totalDuration?.visibility = View.GONE
        playbackSpeed?.visibility = View.GONE
        standardSeekBarLayout?.visibility = View.GONE
        totalDurationStandard?.visibility = View.GONE

    }

    private fun hideVideoViews() {
        videoContainer?.visibility = View.GONE
        videoView?.visibility = View.GONE
        playButton?.visibility = View.GONE
        videoProgressBar?.visibility = View.GONE

    }

    private fun hideAudioViews() {
        audioContainer?.visibility = View.GONE
        artworkLayout?.visibility = View.GONE
        playButton?.visibility = View.GONE
        audioDurationTextView?.visibility = View.GONE

        // New audio controls

        waveSeekBar?.visibility = View.GONE
        currentTime?.visibility = View.GONE
        totalDuration?.visibility = View.GONE
        playbackSpeed?.visibility = View.GONE
        standardSeekBarLayout?.visibility = View.GONE
        totalDurationStandard?.visibility = View.GONE

    }

    private fun hideDocumentViews() {
        documentContainer?.visibility = View.GONE
        documentWebView?.visibility = View.GONE
        pdfImageView?.visibility = View.GONE
        fileTypeIcon?.visibility = View.GONE

    }

    // IMAGE LOADING WITH VISIBLE THUMBNAILS

    private fun loadImageContent(imageUrl: String) {

        artworkLayout?.visibility = View.GONE
        mainPlayButton?.visibility = View.GONE
        audioContainer?.visibility = View.GONE

        // Show image
        postImage?.visibility = View.VISIBLE
        videoContainer?.visibility = View.GONE
        documentContainer?.visibility = View.GONE

        // Fix the height issue
        postImage?.layoutParams = postImage?.layoutParams?.apply {
            height = ViewGroup.LayoutParams.MATCH_PARENT
        }


        Log.d(TAG, "Loading IMAGE content: $imageUrl")

        hideVideoViews()
        hideAudioViews()
        hideDocumentViews()

        postImage?.let { imageView ->
            imageView.visibility = View.VISIBLE

            try {
                val context = context ?: requireContext()

                Glide.with(context)
                    .load(imageUrl)
                    .thumbnail(0.1f) // show small thumbnail first
                    .placeholder(R.drawable.flash21)
                    .error(R.drawable.flash21)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any,
                            target: Target<Drawable>,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.e(TAG, "Glide failed to load image: ${e?.message}")
                            loadImageFallback(imageUrl, imageView)
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable>,
                            dataSource: com.bumptech.glide.load.DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.d(TAG, "Image loaded successfully")
                            return false
                        }
                    })
                    .override(imageView.width, imageView.height) // resize to fit view
                    .into(imageView)

                imageView.scaleType = ImageView.ScaleType.FIT_CENTER
                Log.d(TAG, "Image setup completed for: ${extractFileNameFromUrl(imageUrl)}")

            } catch (e: Exception) {
                Log.e(TAG, "Error loading image: ${e.message}")
                imageView.setImageResource(R.drawable.flash21)
            }
        } ?: Log.e(TAG, "postImage is null!")
    }

    private fun loadImageFallback(imageUrl: String, imageView: ImageView) {
        try {
            if (imageUrl.startsWith("http") || imageUrl.startsWith("https")) {
                Glide.with(requireContext())
                    .load(imageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // enable caching for fallback too
                    //.skipMemoryCache(false) // default false, so can be omitted
                    .into(imageView)
            } else {
                val file = File(imageUrl)
                if (file.exists()) {
                    Glide.with(requireContext())
                        .load(file)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imageView)
                } else {
                    Glide.with(requireContext())
                        .load(Uri.parse(imageUrl))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imageView)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fallback image loading failed: ${e.message}")
        }
    }


    // VIDEOS LOADINING WITH VISIBLE THUMBNAILS

    // FIXED: Add the click listener setup call
    private fun loadVideoContent(fileUrl: String, fileName: String, postItem: PostItem) {
        artworkLayout?.visibility = View.GONE
        mainPlayButton?.visibility = View.GONE
        audioContainer?.visibility = View.GONE

        // Show video container
        videoContainer?.visibility = View.VISIBLE
        postImage?.visibility = View.GONE
        documentContainer?.visibility = View.GONE

        // Fix the height issue
        videoContainer?.layoutParams = videoContainer?.layoutParams?.apply {
            height = ViewGroup.LayoutParams.MATCH_PARENT
        }

        Log.d(TAG, "Loading VIDEO content: $fileUrl")

        // Hide other media views
        hideAudioViews()
        hideDocumentViews()
        postImage?.visibility = View.GONE

        // Show video container but keep VideoView hidden initially
        videoContainer?.visibility = View.VISIBLE
        videoView?.visibility = View.GONE // Keep hidden until play
        videoProgressBar?.visibility = View.GONE
        videoPlayButton?.visibility = View.VISIBLE

        // Only show thumbnail - no VideoView setup
        showVideoThumbnail(fileUrl)

        // ADD THIS LINE - Setup the click listener
        setupVideoClickListener(fileUrl, postItem)
    }

    private fun setupVideoClickListener(fileUrl: String, postItem: PostItem) {
        videoPlayButton?.setOnClickListener {
            Log.d(TAG, "Play button clicked - navigating to ShotsFragment")
            navigateToShotsFragment(fileUrl, postItem)
        }
    }

    // Keep your existing navigation method
    @OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun navigateToShotsFragment(fileUrl: String, postItem: PostItem) {
        try {
            val bundle = Bundle().apply {
                putString("video_url", fileUrl)
                putString("post_id", postItem.postId)
                putParcelable("post_item", postItem)
            }

            // Try NavController first (if properly set up)
            try {
                findNavController().navigate(R.id.nav_graph, bundle)
                Log.d(TAG, "Navigation successful with NavController")
            } catch (e: IllegalStateException) {
                // NavController not available, use Fragment Manager as fallback
                Log.d(TAG, "NavController not found, using Fragment Manager")

                EventBus.getDefault().post(GetShortFragments(bundle))

                Log.d(TAG, "Navigation successful with Fragment Manager")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to ShotsFragment: ${e.message}")
        }
    }

    private fun startVideoPlayback(fileUrl: String) {

        Log.d(TAG, "Starting video playback for: $fileUrl")

        try {
            val uri = if (fileUrl.startsWith("http")) {
                Uri.parse(fileUrl)
            } else {
                val file = File(fileUrl)
                if (file.exists()) Uri.fromFile(file) else Uri.parse(fileUrl)
            }

            videoView?.apply {
                setZOrderMediaOverlay(true)
                visibility = View.VISIBLE
                setVideoURI(uri)
                requestFocus()

                setOnPreparedListener { mediaPlayer ->
                    Log.d(TAG, "Video prepared - starting playback")
                    videoProgressBar?.visibility = View.GONE
                    mediaPlayer.isLooping = true

                    // Start playing immediately
                    start()
                    playButton?.setImageResource(R.drawable.baseline_pause_black)
                    videoPlayButton?.setColorFilter(Color.WHITE) // Keep white color
                    videoThumbnailView?.visibility = View.GONE
                }

                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "Video error: what=$what, extra=$extra")
                    videoProgressBar?.visibility = View.GONE
                    videoPlayButton?.setImageResource(R.drawable.baseline_block_24)
                    videoPlayButton?.setColorFilter(Color.WHITE) // Keep white color
                    true
                }

                setOnCompletionListener {
                    Log.d(TAG, "Video completed")
                    videoPlayButton?.setImageResource(R.drawable.play)
                    videoPlayButton?.setColorFilter(Color.WHITE) // Keep white color
                    videoThumbnailView?.visibility = View.VISIBLE
                    visibility = View.GONE
                }
            }

            videoProgressBar?.visibility = View.VISIBLE

        } catch (e: Exception) {
            Log.e(TAG, "Error starting video playback: ${e.message}")
            videoProgressBar?.visibility = View.GONE
            videoPlayButton?.setImageResource(R.drawable.baseline_block_24)
            videoPlayButton?.setColorFilter(Color.WHITE) // Keep white color
        }
    }

    private fun generateVideoThumbnail(fileUrl: String, callback: (Bitmap?) -> Unit) {
        Thread {
            var retriever: MediaMetadataRetriever? = null
            try {
                retriever = MediaMetadataRetriever()

                if (fileUrl.startsWith("http")) {
                    val headers = HashMap<String, String>()
                    headers["User-Agent"] = "Mozilla/5.0 (Android)"
                    retriever.setDataSource(fileUrl, headers)
                } else {
                    val file = File(fileUrl)
                    if (!file.exists()) {
                        throw FileNotFoundException("Local file not found: $fileUrl")
                    }
                    retriever.setDataSource(file.absolutePath)
                }

                val durationMs = retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_DURATION
                )?.toLongOrNull() ?: 0L

                val timeUs = if (durationMs > 0) durationMs * 1000 / 2 else 1_000_000L

                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    retriever.getScaledFrameAtTime(
                        timeUs,
                        MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                        640,
                        480
                    )
                } else {
                    retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                }

                Handler(Looper.getMainLooper()).post {
                    callback(bitmap)
                }

            } catch (e: Exception) {
                Log.e("VideoThumbnail", "Failed to retrieve video thumbnail", e)
                Handler(Looper.getMainLooper()).post {
                    callback(null)
                }
            } finally {
                try {
                    retriever?.release()
                } catch (e: Exception) {
                    Log.w("VideoThumbnail", "Failed to release retriever", e)
                }
            }
        }.start()
    }

    private fun showVideoThumbnail(fileUrl: String) {

        Log.d(TAG, "Showing video thumbnail only: $fileUrl")

        // Hide other media types
        hideAudioViews()
        hideDocumentViews()
        postImage?.visibility = View.GONE

        // Ensure video container and thumbnail are visible
        videoContainer?.visibility = View.VISIBLE
        videoThumbnailView?.visibility = View.VISIBLE
        videoPlayButton?.visibility = View.VISIBLE

        // Set play button color to white
        videoPlayButton?.setColorFilter(Color.WHITE)

        // Debug log to check if views are found
        Log.d(TAG, "videoContainer: $videoContainer")
        Log.d(TAG, "videoThumbnailView: $videoThumbnailView")
        Log.d(TAG, "playButton: $playButton")

        // Set placeholder first while loading
        videoThumbnailView?.setImageResource(R.drawable.flash21)
        Log.d(TAG, "Set placeholder image")

        // Generate thumbnail from video file or URL
        generateVideoThumbnail(fileUrl) { thumbnail ->
            Log.d(TAG, "Thumbnail generation callback - bitmap: ${thumbnail != null}")
            videoThumbnailView?.apply {
                if (thumbnail != null) {
                    setImageBitmap(thumbnail)
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    Log.d(TAG, "Set thumbnail bitmap successfully")
                } else {
                    setImageResource(R.drawable.flash21)
                    Log.d(TAG, "Using placeholder - thumbnail generation failed")
                }
                visibility = View.VISIBLE
            }
        }
    }


    // AUDIOS LOADING WITH VISIBLE THUMBNAILS

    @SuppressLint("ViewConstructor")
    private fun loadAudioContent(
        audioPath: String,
        fileName: String,
        postItem: PostItem
    ) {
        Log.d("AudioWaveform", "=== Starting loadAudioContent ===")

        // Store the audio path for potential reinitialization
        currentAudioPath = audioPath

        // First setup media container visibility - AUDIO ONLY
        setupMediaContainerVisibilityForAudio()
        Log.d("AudioWaveform", "Media container visibility set for AUDIO")

        // Initialize waveform components FIRST (all GONE except mainPlayButton)
        initializeWaveformComponents()

        // Only mainPlayButton visible at first, others GONE
        setAudioUiInitialState()

        // Setup audio controls UI (does not make visible, just prepares state)
        setupAudioControlsUI()
        Log.d("AudioWaveform", "Audio controls UI setup complete")

        // Setup screen tap functionality for audio control
        setupScreenTapForAudio()

        // Load audio metadata and setup player
        loadAudioMetadataForWaveform(audioPath, fileName)
        initializeWaveformAudioPlayer(audioPath)

        Log.d("AudioWaveform", "Audio content loaded: $fileName")
    }

    private fun setupScreenTapForAudio() {
        // Setup screen tap listener on the main view
        view?.setOnClickListener {
            Log.d("AudioWaveform", "Screen tapped - toggling audio")
            handleScreenTapAudioControl()
        }

        // Make sure the view is clickable
        view?.isClickable = true
        view?.isFocusable = true
    }

    private fun handleScreenTapAudioControl() {

        if (mediaPlayer == null) return

        try {

            if (isPlaying) {
                // PAUSE AUDIO
                Log.d("AudioWaveform", "⏸️ Screen tap - Pausing audio...")
                mediaPlayer?.pause()
                isPlaying = false
                stopProgressUpdates()
                stopWaveAnimation()

                // Hide waveform after 1 second
                screenTapHandler?.postDelayed({
                    waveAnimation?.visibility = View.VISIBLE
                }, 1000)

                // Show pause icon for 2 seconds
                showPauseIconTemporarily()

            } else {
                // PLAY AUDIO
                Log.d("AudioWaveform", "▶️ Screen tap - Starting audio...")

                if (mediaPlayer?.currentPosition ?: 0 <= 0 && currentPosition > 0) {
                    mediaPlayer?.seekTo(currentPosition)
                }

                mediaPlayer?.start()
                isPlaying = true
                startProgressUpdates()

                // Show waveform after 1 second
                screenTapHandler?.postDelayed({
                    waveAnimation?.visibility = View.VISIBLE
                }, 500)

                // START waveform animation
                waveAnimation?.let { waveform ->
                    startWaveAnimation(waveform)
                }

                // Show play icon for 2 seconds
                showPlayIconTemporarily()
            }


        } catch (e: Exception) {
            Log.e("AudioWaveform", "❌ Error in screen tap audio control", e)
            showErrorState("Playback error")
        }
    }

    private fun showPauseIconTemporarily() {
        pauseButtonMain?.visibility = View.VISIBLE
        mainPlayButton?.visibility = View.GONE

        // Hide after 2 seconds
        screenTapHandler?.postDelayed({
            pauseButtonMain?.visibility = View.GONE
        }, 2000)
    }

    private fun showPlayIconTemporarily() {
        mainPlayButton?.visibility = View.VISIBLE
        pauseButtonMain?.visibility = View.GONE

        // Hide after 2 seconds
        screenTapHandler?.postDelayed({
            mainPlayButton?.visibility = View.GONE
        }, 2000)
    }

    private fun setAudioUiInitialState() {
        // Show only mainPlayButton, all others GONE
        mainPlayButton?.visibility = View.VISIBLE
        pauseButtonMain?.visibility = View.GONE
        waveAnimation?.visibility = View.GONE
        audioSeekBar?.visibility = View.GONE
        currentTime?.visibility = View.GONE
        totalDuration?.visibility = View.GONE
        playButton?.visibility = View.GONE
        audioContainer?.visibility = View.GONE
    }

    private fun resetToInitialAudioState() {
        Log.d("AudioWaveform", "Resetting to initial audio state")

        // Hide all audio controls
        waveAnimation?.visibility = View.GONE
        audioSeekBar?.visibility = View.GONE
        currentTime?.visibility = View.GONE
        totalDuration?.visibility = View.GONE
        playButton?.visibility = View.GONE
        audioContainer?.visibility = View.GONE
        pauseButtonMain?.visibility = View.GONE

        // Show only mainPlayButton for replay
        mainPlayButton?.visibility = View.VISIBLE

        // Reset player state
        isPlaying = false
        currentPosition = 0

        Log.d("AudioWaveform", "Reset complete - only mainPlayButton visible")
    }

    @SuppressLint("SetTextI18n")
    private fun handleMetadataLoadError() {
        val sampleData = generateSampleWaveformData()
        waveAnimation?.apply {
            sample = sampleData
            visibility = View.GONE
            requestLayout()
        }
        totalDuration?.apply {
            visibility = View.GONE
            text = "0:00"
        }
        currentTime?.apply {
            visibility = View.GONE
            text = "0:00"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupAudioControlsUI() {
        // Initialize play button, but don't show it yet
        initializePlayButton()

        // Make currentTime and totalDuration ready, but don't show yet
        currentTime?.apply {
            visibility = View.GONE
            text = "0:00"
        }
        totalDuration?.apply {
            visibility = View.GONE
            text = if (totalDurationValue > 0) formatTime(totalDurationValue) else "0:00"
        }

        // Initialize UI state (keeps them hidden)
        initializeUIState()
    }

    @SuppressLint("SetTextI18n")
    private fun initializeUIState() {

        currentTime?.apply {
            visibility = View.GONE
            text = "0:00"
        }
        totalDuration?.apply {
            visibility = View.GONE
            text = if (totalDurationValue > 0) formatTime(totalDurationValue) else "0:00"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleAudioCompletion() {
        Log.d("AudioWaveform", "HandleAudioCompletion called")

        isPlaying = false
        currentPosition = 0
        audioSeekBar?.progress = 0

        currentTime?.text = "0:00"
        totalDuration?.text = formatTime(totalDurationValue)

        stopProgressUpdates()
        stopWaveAnimation()

        // Hide temporary icons
        pauseButtonMain?.visibility = View.GONE
        screenTapHandler?.removeCallbacksAndMessages(null)

        resetToInitialAudioState()
    }

    private fun initializePlayButton() {
        playButton = view?.findViewById(R.id.playButton)
        playButton?.apply {
            visibility = View.GONE
            isEnabled = true
            isClickable = true
            isFocusable = true
            setOnClickListener {
                Log.d("AudioWaveform", "Play button clicked!")
                it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
                togglePlayPause()
            }
        }

        mainPlayButton = view?.findViewById(R.id.playButtonMain)!!
        mainPlayButton?.apply {
            visibility = View.VISIBLE
            setOnClickListener { view ->
                Log.d("AudioWaveform", "Main play button clicked!")
                view.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
                mainPlayButton?.visibility = View.GONE
                showAudioUiControls()
                togglePlayPause()
            }
        }

        // Initialize pause button
        pauseButtonMain = view?.findViewById(R.id.pauseButtonMain)
        pauseButtonMain?.visibility = View.GONE

        // Initialize handler for hiding icons
        screenTapHandler = Handler(Looper.getMainLooper())
    }

    private fun MediaPlayer.setupMediaPlayerListeners() {
        setOnPreparedListener { player ->
            Log.d("AudioWaveform", "MediaPlayer prepared - Duration: ${player.duration}")
            totalDurationValue = player.duration

            totalDuration?.apply {
                visibility = View.GONE
                text = formatTime(totalDurationValue)
            }

            currentTime?.apply {
                visibility = View.GONE
                text = "0:00"
            }

            Log.d("AudioWaveform", "About to setup waveform components after MediaPlayer prepared...")
            setupWaveformComponents(totalDurationValue)

            waveAnimation?.apply {
                visibility = View.GONE
                alpha = 1.0f
                elevation = 8f

                val initialData = generateStaticWaveformData()
                sample = initialData

                post {
                    invalidate()
                    requestLayout()
                    Log.d("AudioWaveform", "Initial waveform data set - visibility: ${visibility == View.GONE}")
                }
            }

            playButton?.apply {
                isEnabled = true
                isClickable = true
                visibility = View.GONE
                Log.d("AudioWaveform", "Play button re-confirmed - enabled: $isEnabled, clickable: $isClickable")
            }
        }

        setOnCompletionListener {
            Log.d("AudioWaveform", "Audio playback completed")
            handleAudioCompletion()
        }

        setOnErrorListener { _, what, extra ->
            Log.e("AudioWaveform", "MediaPlayer error: what=$what, extra=$extra")
            showErrorState("Audio playback error")
            true
        }
    }

    private fun setupMediaContainerVisibilityForAudio() {
        // Make sure the audio container is visible
        artworkLayout?.visibility = View.VISIBLE
        videoContainer?.visibility = View.GONE
        postImage?.visibility = View.GONE
        documentContainer?.visibility = View.GONE
        audioContainer?.visibility = View.GONE

        // CRITICAL: Show mainPlayButton ONLY for audio
        mainPlayButton?.visibility = View.VISIBLE

        Log.d("AudioWaveform", "Media container visibility set - artworkLayout visible, mainPlayButton visible")
    }

    private fun generateAnimatedWaveformData(animationValue: Float): IntArray {
        val sampleCount = 60
        val waveformData = IntArray(sampleCount)

        val time = animationValue * 2 * Math.PI

        for (i in 0 until sampleCount) {
            val indexOffset = i * 0.2

            val shapeWave = Math.sin(time * 0.3 + i * 0.5) * 20
            val randomShape = (Math.random() * 30).toInt()

            val verticalMotion = Math.sin(time + indexOffset) * 30
            val vibration = Math.sin((time + i) * 2.5) * 10
            val jitter = (Math.random() * 6 - 3).toInt()

            val final = 70 + shapeWave + randomShape + verticalMotion + vibration + jitter

            waveformData[i] = final.toInt().coerceIn(10, 200)
        }

        return waveformData
    }

    private fun generateStaticWaveformData(): IntArray {
        val sampleCount = 60
        val waveformData = IntArray(sampleCount)

        for (i in 0 until sampleCount) {
            val randomFactor = Math.random().toFloat()
            val baseAmplitude = 50 + (randomFactor * 60).toInt()

            val staticVariation = (Math.sin(i * 0.4) * 25).toInt()
            val additionalVariation = (Math.sin(i * 0.8) * 15).toInt()

            val staticSpike = if (Math.random() > 0.8) (Math.random() * 30).toInt() else 0

            waveformData[i] = (
                    baseAmplitude +
                            staticVariation +
                            additionalVariation +
                            staticSpike).coerceIn(10, 120)
        }

        return waveformData
    }

    private fun generateSampleWaveformData(): IntArray {
        val sampleCount = 60
        val waveformData = IntArray(sampleCount)

        for (i in 0 until sampleCount) {
            val randomFactor = Math.random().toFloat()
            val amplitude = 40 + (randomFactor * 70).toInt()
            val variation = (Math.sin(i * 0.5) * 20).toInt()
            val spike = if (Math.random() > 0.75) (Math.random() * 25).toInt() else 0

            waveformData[i] = (amplitude + variation + spike).coerceIn(15, 130)
        }

        return waveformData
    }

    private fun initializeWaveformComponents() {
        waveAnimation = view?.findViewById(R.id.waveAnimation)
        audioSeekBar = view?.findViewById(R.id.audioSeekBar)
        currentTime = view?.findViewById(R.id.currentTime)
        totalDuration = view?.findViewById(R.id.totalDuration)
        playButton = view?.findViewById(R.id.playButton)
        mainPlayButton = view?.findViewById(R.id.playButtonMain)!!
        audioContainer = view?.findViewById(R.id.audioContainer)

        // Initially hide all audio components
        waveAnimation?.visibility = View.GONE
        audioSeekBar?.visibility = View.GONE
        currentTime?.visibility = View.GONE
        totalDuration?.visibility = View.GONE
        playButton?.visibility = View.GONE
        audioContainer?.visibility = View.GONE

        // mainPlayButton visibility will be controlled by content type
        mainPlayButton?.visibility = View.GONE

        Log.d("AudioWaveform", "=== Initializing Waveform Components ===")

        waveAnimation = view?.findViewById(R.id.waveAnimation)
        Log.d("AudioWaveform", "waveAnimation found: ${waveAnimation != null}")

        audioSeekBar = view?.findViewById(R.id.audioSeekBar)
        Log.d("AudioWaveform", "audioSeekBar found: ${audioSeekBar != null}")

        currentTime = view?.findViewById(R.id.currentTime)
        Log.d("AudioWaveform", "currentTime found: ${currentTime != null}")

        totalDuration = view?.findViewById(R.id.totalDuration)
        Log.d("AudioWaveform", "totalDuration found: ${totalDuration != null}")

        waveAnimation?.let { waveform ->
            Log.d("AudioWaveform", "Configuring waveform...")

            configureWaveformColors(waveform)
            val staticData = generateStaticWaveformData()
            waveform.sample = staticData
            waveform.post {
                waveform.invalidate()
                waveform.requestLayout()
            }

            waveform.apply {
                visibility = View.GONE
                alpha = 1.0f
                elevation = 8f
                scaleX = 1.0f
                scaleY = 1.0f
            }

            waveform.isEnabled = false
            waveform.isFocusable = false
            waveform.isClickable = false

            configureWaveformColors(waveform)

            waveform.sample = staticData

            waveform.post {
                waveform.invalidate()
                waveform.requestLayout()

                waveform.postDelayed({
                    waveform.invalidate()
                    Log.d("AudioWaveform", "Waveform final configuration check - alpha: ${waveform.alpha}")
                }, 100)
            }

            Log.d("AudioWaveform", "Waveform animation setup complete - initially hidden")
        } ?: Log.e("AudioWaveform", "❌ waveAnimation is null - check your layout XML ID")

        audioSeekBar?.let { seekBar ->
            seekBar.visibility = View.GONE
            seekBar.max = 100
            seekBar.progress = 0
            setupSeekBarListener(seekBar)
            Log.d("AudioWaveform", "SeekBar setup complete")
        } ?: Log.e("AudioWaveform", "❌ audioSeekBar is null")

        Log.d("AudioWaveform", "=== Waveform Components Initialization Complete ===")
    }

    private fun showAudioUiControls() {
        // Show all audio-related UI controls except mainPlayButton
        waveAnimation?.visibility = View.VISIBLE
        audioSeekBar?.visibility = View.VISIBLE
        currentTime?.visibility = View.VISIBLE
        totalDuration?.visibility = View.VISIBLE
        playButton?.visibility = View.VISIBLE
        audioContainer?.visibility = View.GONE

        Log.d("AudioWaveform", "Audio UI controls shown")
    }

    private fun setupWaveformComponents(duration: Int) {
        Log.d("AudioWaveform", "=== Starting setupWaveformComponents with duration: $duration ===")

        audioSeekBar?.let { seekBar ->
            seekBar.max = 100
            seekBar.progress = 0
            Log.d("AudioWaveform", "SeekBar configured - Max: ${seekBar.max}")
        }

        waveAnimation?.let { waveform ->
            configureWaveformColors(waveform)
            Log.d("AudioWaveform", "Waveform animation colors configured")
        }
    }

    private fun loadAudioMetadataForWaveform(audioPath: String, fileName: String) {
        Log.d("AudioWaveform", "=== Starting loadAudioMetadataForWaveform ===")
        val retriever = MediaMetadataRetriever()

        try {
            if (audioPath.startsWith("http")) {
                retriever.setDataSource(audioPath, HashMap<String, String>())
            } else {
                retriever.setDataSource(audioPath)
            }
            Log.d("AudioWaveform", "MediaMetadataRetriever data source set")

            extractAudioMetadata(retriever, fileName)
            Log.d("AudioWaveform", "Audio metadata extracted, duration: $totalDurationValue")

            loadAlbumArtwork(retriever)
            Log.d("AudioWaveform", "Album artwork loading attempted")

        } catch (e: Exception) {
            Log.e("AudioWaveform", "Error loading audio metadata", e)
            handleMetadataLoadError()
        } finally {
            retriever.release()
            Log.d("AudioWaveform", "MediaMetadataRetriever released")
        }
    }

    private fun extractAudioMetadata(retriever: MediaMetadataRetriever, fileName: String) {
        val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        totalDurationValue = durationStr?.toIntOrNull() ?: 0

        totalDuration?.apply {
            visibility = View.VISIBLE
            text = formatTime(totalDurationValue)
        }

        currentTime?.apply {
            visibility = View.VISIBLE
            text = "0:00"
        }
    }

    private fun configureWaveformColors(waveform: WaveformSeekBar) {
        Log.d("AudioWaveform", "Setting waveform to PURE WHITE...")
        try {
            waveform.waveProgressColor = Color.WHITE
            waveform.waveBackgroundColor = Color.parseColor("#40FFFFFF")

            waveform.waveProgressColor = 0xFFFFFFFF.toInt()
            waveform.waveBackgroundColor = 0x40FFFFFF.toInt()

            waveform.apply {
                visibility = View.VISIBLE
                alpha = 1.0f
                elevation = 10f
                scaleX = 1.0f
                scaleY = 1.0f
                requestLayout()
                invalidate()
            }

            waveform.post {
                waveform.waveProgressColor = Color.WHITE
                waveform.invalidate()
            }

            Log.d("AudioWaveform", "Pure WHITE waveform colors applied successfully")
        } catch (e: Exception) {
            Log.e("AudioWaveform", "Error setting white colors", e)
            try {
                waveform.waveProgressColor = -1
                waveform.waveBackgroundColor = 0x30FFFFFF.toInt()
            } catch (e2: Exception) {
                Log.e("AudioWaveform", "Fallback white color setting failed", e2)
            }
        }
    }

    private fun startWaveAnimation(waveform: WaveformSeekBar) {
        Log.d("AudioWaveform", "🎵 Starting DRAMATIC WHITE waveform animation")

        stopWaveAnimation()

        waveform.apply {
            visibility = View.VISIBLE
            alpha = 1.0f
            elevation = 10f

            waveProgressColor = Color.WHITE
            waveProgressColor = 0xFFFFFFFF.toInt()
            waveProgressColor = -1
            waveBackgroundColor = Color.WHITE

            requestLayout()
            invalidate()

            post {
                visibility = View.VISIBLE
                alpha = 1.0f
                waveProgressColor = Color.WHITE
                waveProgressColor = 0xFFFFFFFF.toInt()
                invalidate()
            }
        }

        isWaveformAnimating = true

        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = 600
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = ValueAnimator.RESTART
        animator.interpolator = LinearInterpolator()

        animator.addUpdateListener { animation ->
            if (isWaveformAnimating && isPlaying && waveform.visibility == View.VISIBLE) {
                try {
                    val animatedValue = animation.animatedValue as Float
                    val animatedData = generateAnimatedWaveformData(animatedValue)
                    waveform.sample = animatedData

                    waveform.waveProgressColor = Color.WHITE
                    waveform.waveProgressColor = 0xFFFFFFFF.toInt()

                    waveform.post {
                        waveform.invalidate()
                    }
                } catch (e: Exception) {
                    Log.e("AudioWaveform", "Error updating dramatic white animation", e)
                }
            }
        }

        waveAnimator = animator
        animator.start()
        Log.d("AudioWaveform", "✅ DRAMATIC WHITE waveform animation started")
    }

    private fun stopWaveAnimation() {
        Log.d("AudioWaveform", "⏹️ Stopping wave animation")

        isWaveformAnimating = false
        waveAnimator?.cancel()
        waveAnimator = null

        waveAnimation?.let { waveform ->
            try {
                val staticData = generateStaticWaveformData()
                waveform.sample = staticData
                waveform.post {
                    waveform.invalidate()
                }
                Log.d("AudioWaveform", "✅ Wave animation stopped - set to static state")
            } catch (e: Exception) {
                Log.e("AudioWaveform", "Error setting static state", e)
            }
        }
    }

    private fun setupSeekBarListener(seekBar: SeekBar) {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && mediaPlayer != null) {
                    val newPosition = (progress.toFloat() / seekBar!!.max * totalDurationValue).toInt()
                    currentPosition = newPosition
                    currentTime?.text = formatTime(newPosition)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                stopProgressUpdates()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                mediaPlayer?.let { player ->
                    val newPosition = (seekBar!!.progress.toFloat() / seekBar.max * totalDurationValue).toInt()
                    player.seekTo(newPosition)
                    currentPosition = newPosition

                    if (isPlaying) {
                        startProgressUpdates()
                    }
                }
            }
        })
    }

    private fun initializeWaveformAudioPlayer(audioPath: String) {
        try {
            mediaPlayer?.release()

            mediaPlayer = MediaPlayer().apply {
                setupDataSource(audioPath)
                prepareAsync()
                setupMediaPlayerListeners()
            }

        } catch (e: Exception) {
            Log.e("AudioWaveform", "Error initializing audio player", e)
            showErrorState("Failed to initialize audio player")
        }
    }

    private fun MediaPlayer.setupDataSource(audioPath: String) {
        if (audioPath.startsWith("http")) {
            setDataSource(audioPath)
        } else {
            setDataSource(audioPath)
        }
    }

    private fun loadAlbumArtwork(retriever: MediaMetadataRetriever) {
        val artwork = retriever.embeddedPicture
        artwork?.let {
            artworkImageView?.visibility = View.VISIBLE
        }
    }

    private fun togglePlayPause() {
        Log.d("AudioWaveform", "🎵 === TOGGLE PLAY/PAUSE ===")
        Log.d("AudioWaveform", "MediaPlayer exists: ${mediaPlayer != null}")
        Log.d("AudioWaveform", "Current isPlaying: $isPlaying")

        mediaPlayer?.let { player ->
            try {
                if (isPlaying) {
                    Log.d("AudioWaveform", "⏸️ Pausing audio...")
                    player.pause()
                    isPlaying = false
                    stopProgressUpdates()

                    stopWaveAnimation()
                    Log.d("AudioWaveform", "✅ Audio paused - waveform animation STOPPED")

                } else {
                    Log.d("AudioWaveform", "▶️ Starting audio...")

                    if (player.currentPosition <= 0 && currentPosition > 0) {
                        player.seekTo(currentPosition)
                    }

                    player.start()
                    isPlaying = true
                    startProgressUpdates()

                    waveAnimation?.let { waveform ->
                        startWaveAnimation(waveform)
                        Log.d("AudioWaveform", "✅ Audio playing - waveform animation STARTED")
                    } ?: Log.e("AudioWaveform", "❌ waveAnimation is null!")
                }

            } catch (e: Exception) {
                Log.e("AudioWaveform", "❌ Error in togglePlayPause", e)
                showErrorState("Playback error")
            }
        } ?: Log.e("AudioWaveform", "❌ MediaPlayer is null")

        Log.d("AudioWaveform", "=== TOGGLE COMPLETE ===")
    }

    private fun startProgressUpdates() {
        stopProgressUpdates()

        updateRunnable = object : Runnable {
            override fun run() {
                mediaPlayer?.let { player ->
                    currentPosition = player.currentPosition

                    currentTime?.text = formatTime(currentPosition)

                    val remainingTime = totalDurationValue - currentPosition
                    totalDuration?.text = formatTime(remainingTime.coerceAtLeast(0))

                    if (totalDurationValue > 0) {
                        val progressPercentage = (currentPosition.toFloat() / totalDurationValue * 100).toInt()
                        audioSeekBar?.progress = progressPercentage
                    }

                    if (isPlaying) {
                        updateHandler.postDelayed(this, 100)
                    }
                }
            }
        }
        updateHandler.post(updateRunnable!!)
    }

    private fun stopProgressUpdates() {
        updateRunnable?.let { updateHandler.removeCallbacks(it) }
        updateRunnable = null
    }

    private fun formatTime(milliseconds: Int): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds.toLong())
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds.toLong()) % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    private fun showErrorState(message: String) {
        Log.e("AudioWaveform", message)
        playButton?.isEnabled = false
        currentTime?.apply {
            visibility = View.VISIBLE
            text = "Error"
        }
        totalDuration?.apply {
            visibility = View.VISIBLE
            text = "Error"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanupAudioPlayer()
        clearThumbnailCache()

        activity?.findViewById<View>(R.id.topBar)?.visibility = View.VISIBLE
        activity?.findViewById<View>(R.id.bottomNavigationView)?.visibility = View.VISIBLE

        mainPlayButton?.visibility = View.GONE
        pauseButtonMain?.visibility = View.GONE
        screenTapHandler?.removeCallbacksAndMessages(null)
    }

    private fun cleanupAudioPlayer() {
        stopProgressUpdates()
        stopWaveAnimation()
        mediaPlayer?.release()
        mediaPlayer = null
        isPlaying = false

        mainPlayButton?.visibility = View.GONE
        pauseButtonMain?.visibility = View.GONE
        screenTapHandler?.removeCallbacksAndMessages(null)
    }



    //  DOCUMENT LOADING WITH VISIBLE THUMBNAILS

    private fun loadDocumentContent(
        fileUrl: String,
        fileName: String,
        postItem: PostItem) {

        // Store the fileUrl for later use in download
        currentDocumentUrl = fileUrl
        currentFileName = fileName

        Log.d(TAG, "=== DOCUMENT LOADING DEBUG ===")
        Log.d(TAG, "File URL: $fileUrl")
        Log.d(TAG, "File Name: $fileName")

        artworkLayout?.visibility = View.GONE
        mainPlayButton?.visibility = View.GONE
        audioContainer?.visibility = View.GONE

        // Show document container
        documentContainer?.visibility = View.VISIBLE
        postImage?.visibility = View.GONE
        videoContainer?.visibility = View.GONE

        Log.d(TAG, "Loading DOCUMENT content: $fileUrl")

        postImage?.visibility = View.GONE
        hideVideoViews()
        hideAudioViews()

        val extension = fileName.substringAfterLast('.', "").lowercase()
        Log.d(TAG, "File extension detected: $extension")

        when (extension) {
            "pdf" -> {
                loadPdfFromUrl(fileUrl, fileName)
                setupDocumentContainer(fileName, DocumentType.PDF) // Add this line
            }
            "doc", "docx" -> {
                loadOfficeDocument(fileUrl, fileName, DocumentType.WORD)
                setupDocumentContainer(fileName, DocumentType.WORD) // Add this line
            }
            "ppt", "pptx" -> {
                loadOfficeDocument(fileUrl, fileName, DocumentType.POWERPOINT)
                setupDocumentContainer(fileName, DocumentType.POWERPOINT) // Add this line
            }
            "xls", "xlsx" -> {
                loadOfficeDocument(fileUrl, fileName, DocumentType.EXCEL)
                setupDocumentContainer(fileName, DocumentType.EXCEL) // Add this line
            }
            "txt", "rtf", "md", "log", "cfg", "config", "ini", "properties" -> {
                loadTextDocument(fileUrl, fileName)
                setupDocumentContainer(fileName, DocumentType.TEXT) // Add this line
            }
            "html", "htm", "xml", "json" -> {
                loadWebDocument(fileUrl, fileName)
                setupDocumentContainer(fileName, DocumentType.WEB) // Add this line
            }
            "odt" -> {
                loadOfficeDocument(fileUrl, fileName, DocumentType.WORD)
                setupDocumentContainer(fileName, DocumentType.WORD) // Add this line
            }
            "odp" -> {
                loadOfficeDocument(fileUrl, fileName, DocumentType.POWERPOINT)
                setupDocumentContainer(fileName, DocumentType.POWERPOINT) // Add this line
            }
            "ods" -> {
                loadOfficeDocument(fileUrl, fileName, DocumentType.EXCEL)
                setupDocumentContainer(fileName, DocumentType.EXCEL) // Add this line
            }
            "csv" -> {
                loadCsvDocument(fileUrl, fileName)
                setupDocumentContainer(fileName, DocumentType.CSV) // Add this line
            }
            "pages" -> {
                loadOfficeDocument(fileUrl, fileName, DocumentType.WORD)
                setupDocumentContainer(fileName, DocumentType.WORD) // Add this line
            }
            "numbers" -> {
                loadOfficeDocument(fileUrl, fileName, DocumentType.EXCEL)
                setupDocumentContainer(fileName, DocumentType.EXCEL) // Add this line
            }
            "keynote" -> {
                loadOfficeDocument(fileUrl, fileName, DocumentType.POWERPOINT)
                setupDocumentContainer(fileName, DocumentType.POWERPOINT) // Add this line
            }
            "epub", "mobi", "azw", "azw3" -> {
                loadEbookDocument(fileUrl, fileName)
                setupDocumentContainer(fileName, DocumentType.EBOOK) // Add this line
            }
            else -> {
                loadGenericDocument(fileUrl, fileName)
                setupDocumentContainer(fileName, DocumentType.GENERIC) // Add this line
            }
        }

        Log.d(TAG, "Document setup completed for: $fileName")
    }

    private fun setupDocumentContainer(
        fileName: String,
        documentType: DocumentType) {

        Log.d(TAG, "=== SETUP DOCUMENT CONTAINER DEBUG ===")
        Log.d(TAG, "Setting up container for: $fileName, type: $documentType")

        documentContainer?.visibility = View.VISIBLE
        Log.d(TAG, "Document container made visible")

        // Show file type icon aligned with document container edge
        fileTypeIcon?.let { iconView ->
            Log.d(TAG, "Setting up file type icon")
            iconView.visibility = View.VISIBLE
            iconView.setImageResource(getFileTypeIcon(fileName))

            // Position at top left, aligned with document container edge
            val params = iconView.layoutParams as? RelativeLayout.LayoutParams
            params?.let {
                it.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                it.addRule(RelativeLayout.ALIGN_PARENT_START)
                // Remove all margins to align with document edge
                it.topMargin = 0
                it.marginStart = 0
                it.marginEnd = 0
                it.bottomMargin = 0
                iconView.layoutParams = it
            }

            // Remove any padding from the icon to touch the edge
            iconView.setPadding(0, 0, 0, 0)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                iconView.elevation = 12f
            }
            iconView.bringToFront()
            iconView.invalidate()
            Log.d(TAG, "File type icon setup completed")
        } ?: Log.w(TAG, "File type icon is null!")

        // Position download button at bottom right corner
        downloadDocumentButton?.let { downloadBtn ->
            Log.d(TAG, "Setting up download button")
            downloadBtn.visibility = View.VISIBLE

            // Position at bottom right corner
            val params = downloadBtn.layoutParams as? RelativeLayout.LayoutParams
            params?.let {
                it.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                it.addRule(RelativeLayout.ALIGN_PARENT_END)
                it.bottomMargin = 32
                it.marginEnd = 32
                it.topMargin = 0
                it.marginStart = 0
                downloadBtn.layoutParams = it
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                downloadBtn.elevation = 10f
            }
            downloadBtn.bringToFront()
            downloadBtn.invalidate()

            // Clear any existing click listeners first
            downloadBtn.setOnClickListener(null)

            // Add click listener to handle download
            downloadBtn.setOnClickListener { view ->
                Log.d(TAG, "*** DOWNLOAD BUTTON CLICKED ***")
                Log.d(TAG, "Button view: $view")
                Log.d(TAG, "File name: $fileName")
                Log.d(TAG, "Document type: $documentType")
                Log.d(TAG, "Current URL: ${if (::currentDocumentUrl.isInitialized) currentDocumentUrl else "NOT INITIALIZED"}")

                // Add haptic feedback to confirm click
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                } else {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                }

                downloadDocument(fileName, documentType)
            }

            // Test if button is clickable
            Log.d(TAG, "Download button clickable: ${downloadBtn.isClickable}")
            Log.d(TAG, "Download button enabled: ${downloadBtn.isEnabled}")
            Log.d(TAG, "Download button visibility: ${downloadBtn.visibility}")
            Log.d(TAG, "Download button alpha: ${downloadBtn.alpha}")

            Log.d(TAG, "Download button setup completed")
        } ?: Log.e(TAG, "Download button is NULL! Check your layout XML.")
    }

    private fun downloadDocument(fileName: String, documentType: DocumentType) {
        try {
            Log.d(TAG, "=== DOWNLOAD DOCUMENT DEBUG ===")
            Log.d(TAG, "Starting download for: $fileName")
            Log.d(TAG, "Document type: $documentType")

            // Check if we have the document URL
            if (!::currentDocumentUrl.isInitialized) {
                Log.e(TAG, "currentDocumentUrl is not initialized!")
                showErrorMessage("Document URL not available - not initialized")
                return
            }

            if (currentDocumentUrl.isEmpty()) {
                Log.e(TAG, "currentDocumentUrl is empty!")
                showErrorMessage("Document URL not available - empty")
                return
            }

            Log.d(TAG, "Document URL available: $currentDocumentUrl")

            // Check for storage permission (Android 6.0+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val hasPermission = ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED

                Log.d(TAG, "Storage permission granted: $hasPermission")

                if (!hasPermission) {
                    Log.d(TAG, "Requesting storage permission")
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        STORAGE_PERMISSION_REQUEST_CODE
                    )
                    return
                }
            }

            // Show loading indicator
            showDownloadProgress(true)

            // Download the file using the actual URL
            downloadFile(fileName, currentDocumentUrl)

        } catch (e: Exception) {
            Log.e(TAG, "Download failed with exception: ${e.message}", e)
            showDownloadProgress(false)
            showErrorMessage("Download failed: ${e.message}")
        }
    }

    // Debug method to check button state
    private fun debugDownloadButton() {
        downloadDocumentButton?.let { btn ->
            Log.d(TAG, "=== DOWNLOAD BUTTON STATE ===")
            Log.d(TAG, "Button exists: true")
            Log.d(TAG, "Button visibility: ${btn.visibility}")
            Log.d(TAG, "Button enabled: ${btn.isEnabled}")
            Log.d(TAG, "Button clickable: ${btn.isClickable}")
            Log.d(TAG, "Button alpha: ${btn.alpha}")
            Log.d(TAG, "Button width: ${btn.width}")
            Log.d(TAG, "Button height: ${btn.height}")
            Log.d(TAG, "Button has click listener: ${btn.hasOnClickListeners()}")
        } ?: Log.e(TAG, "Download button is NULL!")
    }

    // Call this method after setupDocumentContainer to debug
    private fun testDownloadButton() {
        Log.d(TAG, "Testing download button...")
        debugDownloadButton()

        // Programmatically trigger click for testing
        downloadDocumentButton?.performClick()
    }


    private fun downloadFile(fileName: String, fileUrl: String) {
        try {
            Log.d(TAG, "Downloading from URL: $fileUrl")

            val downloadManager = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

            // Use the actual document URL
            val downloadUri = Uri.parse(fileUrl)

            val request = DownloadManager.Request(downloadUri).apply {
                setTitle("Downloading $fileName")
                setDescription("Downloading file...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)

                // Allow all file types
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }

            val downloadId = downloadManager.enqueue(request)
            Log.d(TAG, "Download enqueued with ID: $downloadId")

            // Track download progress
            trackDownloadProgress(downloadId)

        } catch (e: Exception) {
            Log.e(TAG, "Error starting download: ${e.message}", e)
            showDownloadProgress(false)
            showErrorMessage("Failed to start download: ${e.message}")
        }
    }

    private fun trackDownloadProgress(downloadId: Long) {
        val query = DownloadManager.Query().setFilterById(downloadId)
        val downloadManager = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                try {
                    val cursor = downloadManager.query(query)
                    if (cursor.moveToFirst()) {
                        val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                        val reason = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))

                        Log.d(TAG, "Download status: $status, reason: $reason")

                        when (status) {
                            DownloadManager.STATUS_SUCCESSFUL -> {
                                showDownloadProgress(false)
                                showSuccessMessage("Download completed successfully")
                                cursor.close()
                                return
                            }
                            DownloadManager.STATUS_FAILED -> {
                                showDownloadProgress(false)
                                val failureReason = when (reason) {
                                    DownloadManager.ERROR_CANNOT_RESUME -> "Cannot resume download"
                                    DownloadManager.ERROR_DEVICE_NOT_FOUND -> "Device not found"
                                    DownloadManager.ERROR_FILE_ALREADY_EXISTS -> "File already exists"
                                    DownloadManager.ERROR_FILE_ERROR -> "File error"
                                    DownloadManager.ERROR_HTTP_DATA_ERROR -> "HTTP data error"
                                    DownloadManager.ERROR_INSUFFICIENT_SPACE -> "Insufficient space"
                                    DownloadManager.ERROR_TOO_MANY_REDIRECTS -> "Too many redirects"
                                    DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> "Unhandled HTTP code"
                                    DownloadManager.ERROR_UNKNOWN -> "Unknown error"
                                    else -> "Download failed (reason: $reason)"
                                }
                                showErrorMessage(failureReason)
                                cursor.close()
                                return
                            }
                            DownloadManager.STATUS_RUNNING -> {
                                // Get progress info
                                val bytesDownloaded = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                                val bytesTotal = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

                                if (bytesTotal > 0) {
                                    val progress = (bytesDownloaded * 100 / bytesTotal).toInt()
                                    Log.d(TAG, "Download progress: $progress% ($bytesDownloaded/$bytesTotal bytes)")
                                }

                                // Continue tracking
                                handler.postDelayed(this, 1000)
                            }
                            DownloadManager.STATUS_PENDING -> {
                                Log.d(TAG, "Download pending...")
                                handler.postDelayed(this, 1000)
                            }
                            DownloadManager.STATUS_PAUSED -> {
                                Log.d(TAG, "Download paused...")
                                handler.postDelayed(this, 1000)
                            }
                        }
                    } else {
                        Log.w(TAG, "Download query returned no results")
                        showDownloadProgress(false)
                        showErrorMessage("Download tracking failed")
                    }
                    cursor.close()
                } catch (e: Exception) {
                    Log.e(TAG, "Error tracking download progress: ${e.message}", e)
                    showDownloadProgress(false)
                    showErrorMessage("Download tracking error: ${e.message}")
                }
            }
        }
        handler.post(runnable)
    }

    private fun showDownloadProgress(show: Boolean) {
        downloadDocumentButton?.isEnabled = !show
        if (show) {
            Log.d(TAG, "Showing download progress")
            downloadDocumentButton?.text = "Downloading..."
            // Show progress bar or loading indicator if you have one
            // progressBar?.visibility = View.VISIBLE
        } else {
            Log.d(TAG, "Hiding download progress")
            downloadDocumentButton?.text = "Download"
            // Hide progress bar or loading indicator
            // progressBar?.visibility = View.GONE
        }
    }

    private fun showSuccessMessage(message: String) {
        Log.d(TAG, "Success: $message")
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showErrorMessage(message: String) {
        Log.e(TAG, "Error: $message")
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun getFileTypeIcon(fileName: String): Int {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return when (extension) {
            "pdf" -> R.drawable.pdf_icon
            "doc", "docx" -> R.drawable.word_icon
            "ppt", "pptx" -> R.drawable.powerpoint_icon
            "xls", "xlsx" -> R.drawable.excel_icon
            "txt" -> R.drawable.text_icon
            "rtf" -> R.drawable.text_icon
            "odt" -> R.drawable.word_icon
            "csv" -> R.drawable.excel_icon
            "html", "htm" -> R.drawable.text_icon
            "xml" -> R.drawable.text_icon
            "json" -> R.drawable.text_icon
            "md" -> R.drawable.text_icon
            "log" -> R.drawable.text_icon
            "cfg", "config" -> R.drawable.text_icon
            "ini" -> R.drawable.text_icon
            "properties" -> R.drawable.text_icon
            "odp" -> R.drawable.powerpoint_icon
            "ods" -> R.drawable.excel_icon
            "pages" -> R.drawable.word_icon
            "numbers" -> R.drawable.excel_icon
            "keynote" -> R.drawable.powerpoint_icon
            "epub" -> R.drawable.text_icon
            "mobi" -> R.drawable.text_icon
            "azw", "azw3" -> R.drawable.text_icon
            else -> R.drawable.text_icon
        }
    }

    // Add these document types if not already defined
    enum class DocumentType {
        PDF, WORD, POWERPOINT, EXCEL, TEXT, WEB, CSV, EBOOK, GENERIC
    }

//  OFFICE DOCUMENTS (DOC, PPT, XLS)

    private fun loadOfficeDocument(
        fileUrl: String,
        fileName: String
        , documentType: DocumentType) {
        setupDocumentContainer(fileName, documentType)

        // Show loading indicator
        documentThumbnail?.apply {
            visibility = View.VISIBLE
            setImageResource(R.drawable.flash21)
            //scaleType = ImageView.ScaleType.FIT_CENTER
//            adjustViewBounds = true
        }

        // Generate preview for Office documents
        generateOfficePreview(fileUrl, fileName, documentType) { preview ->
            activity?.runOnUiThread {
                documentThumbnail?.apply {
                    if (preview != null) {
                        setImageBitmap(preview)
                    } else {
                        // Fallback to document-specific preview
                        setImageResource(getDocumentPreviewResource(documentType))
                    }
                    //scaleType = ImageView.ScaleType.FIT_CENTER
//                    adjustViewBounds = true
                }
            }
        }

        Log.d(TAG, "Office document loading started for: $fileName")
    }

    private fun generateOfficePreview(
        fileUrl: String,
        fileName: String,
        documentType: DocumentType,
        callback: (Bitmap?) -> Unit) {
        Thread {
            var tempFile: File? = null

            try {
                // Create temporary file
                tempFile = File(context?.cacheDir, "temp_${System.currentTimeMillis()}_$fileName")

                // Download file
                downloadFile(fileUrl, tempFile)

                // For Office documents, we'll create a text-based preview
                // In production, you might want to use libraries like Apache POI
                val preview = when (documentType) {
                    DocumentType.WORD -> generateWordPreview(tempFile)
                    DocumentType.EXCEL -> generateExcelPreview(tempFile)
                    DocumentType.POWERPOINT -> generatePowerPointPreview(tempFile)
                    else -> null
                }

                callback(preview)

            } catch (e: Exception) {
                Log.e(TAG, "Error generating Office preview: ${e.message}", e)
                callback(null)
            } finally {
                tempFile?.delete()
            }
        }.start()
    }

    private fun generateWordPreview(file: File): Bitmap? {
        return try {
            // Create a simple preview showing document icon and basic info
            createDocumentPreview("Word Document", "Double tap to open", R.drawable.word_icon)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating Word preview", e)
            null
        }
    }

    private fun generateExcelPreview(file: File): Bitmap? {
        return try {
            createDocumentPreview("Excel Spreadsheet", "Tap to download", R.drawable.excel_icon)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating Excel preview", e)
            null
        }
    }

    private fun generatePowerPointPreview(file: File): Bitmap? {
        return try {
            createDocumentPreview("PowerPoint Presentation", "Tap to open", R.drawable.powerpoint_icon)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating PowerPoint preview", e)
            null
        }
    }

//  TEXT DOCUMENTS

    private fun loadTextDocument(fileUrl: String, fileName: String) {
        setupDocumentContainer(fileName, DocumentType.TEXT)

        documentThumbnail?.apply {
            visibility = View.VISIBLE
            setImageResource(R.drawable.flash21)
//            scaleType = ImageView.ScaleType.FIT_CENTER
//            adjustViewBounds = true
        }

        // Generate text preview
        generateTextPreview(fileUrl, fileName) { preview ->
            activity?.runOnUiThread {
                documentThumbnail?.apply {
                    if (preview != null) {
                        setImageBitmap(preview)
                    } else {
                        setImageResource(R.drawable.text_icon)
                    }
//                    scaleType = ImageView.ScaleType.FIT_CENTER
//                    adjustViewBounds = true
                }
            }
        }
    }

    private fun generateTextPreview(fileUrl: String, fileName: String, callback: (Bitmap?) -> Unit) {
        Thread {
            try {
                val url = URL(fileUrl)
                val connection = url.openConnection().apply {
                    connectTimeout = 5000
                    readTimeout = 10000
                }

                val content = connection.inputStream.bufferedReader().use { reader ->
                    reader.readText().take(500) // First 500 characters
                }

                val preview = createTextPreview(content, fileName)
                callback(preview)

            } catch (e: Exception) {
                Log.e(TAG, "Error generating text preview: ${e.message}", e)
                callback(null)
            }
        }.start()
    }

// WEB DOCUMENTS (HTML, XML, JSON)

    private fun loadWebDocument(fileUrl: String, fileName: String) {
        setupDocumentContainer(fileName, DocumentType.WEB)

        documentThumbnail?.apply {
            visibility = View.VISIBLE
            setImageResource(R.drawable.flash21)
//            scaleType = ImageView.ScaleType.FIT_CENTER
//            adjustViewBounds = true
        }

        generateWebPreview(fileUrl, fileName) { preview ->
            activity?.runOnUiThread {
                documentThumbnail?.apply {
                    if (preview != null) {
                        setImageBitmap(preview)
                    } else {
                        setImageResource(getFileTypeIcon(fileName))
                    }
//                    scaleType = ImageView.ScaleType.FIT_CENTER
//                    adjustViewBounds = true
                }
            }
        }

    }

    private fun generateWebPreview(fileUrl: String, fileName: String, callback: (Bitmap?) -> Unit) {
        Thread {
            try {
                val url = URL(fileUrl)
                val connection = url.openConnection().apply {
                    connectTimeout = 5000
                    readTimeout = 10000
                }

                val content = connection.inputStream.bufferedReader().use { reader ->
                    reader.readText().take(300)
                }

                val preview = createWebDocumentPreview(content, fileName)
                callback(preview)

            } catch (e: Exception) {
                Log.e(TAG, "Error generating web document preview: ${e.message}", e)
                callback(null)
            }
        }.start()
    }

//  CSV DOCUMENTS

    private fun loadCsvDocument(fileUrl: String, fileName: String) {
        setupDocumentContainer(fileName, DocumentType.CSV)

        documentThumbnail?.apply {
            visibility = View.VISIBLE
            setImageResource(R.drawable.flash21)
//            scaleType = ImageView.ScaleType.FIT_CENTER
//            adjustViewBounds = true
        }

        generateCsvPreview(fileUrl, fileName) { preview ->
            activity?.runOnUiThread {
                documentThumbnail?.apply {
                    if (preview != null) {
                        setImageBitmap(preview)
                    } else {
                        setImageResource(R.drawable.excel_icon)
                    }
//                    scaleType = ImageView.ScaleType.FIT_CENTER
//                    adjustViewBounds = true
                }
            }
        }
    }

    private fun generateCsvPreview(fileUrl: String, fileName: String, callback: (Bitmap?) -> Unit) {
        Thread {
            try {
                val url = URL(fileUrl)
                val connection = url.openConnection().apply {
                    connectTimeout = 5000
                    readTimeout = 10000
                }

                val lines = connection.inputStream.bufferedReader().use { reader ->
                    reader.readLines().take(5) // First 5 rows
                }

                val preview = createCsvPreview(lines, fileName)
                callback(preview)

            } catch (e: Exception) {
                Log.e(TAG, "Error generating CSV preview: ${e.message}", e)
                callback(null)
            }
        }.start()
    }

// EBOOK DOCUMENTS

    private fun loadEbookDocument(fileUrl: String, fileName: String) {
        setupDocumentContainer(fileName, DocumentType.EBOOK)

        documentThumbnail?.apply {
            visibility = View.VISIBLE
            setImageResource(R.drawable.flash21)
//            scaleType = ImageView.ScaleType.FIT_CENTER
//            adjustViewBounds = true
        }

        // For ebooks, show a generic book preview
        Thread {
            val preview = createEbookPreview(fileName)
            activity?.runOnUiThread {
                documentThumbnail?.apply {
                    if (preview != null) {
                        setImageBitmap(preview)
                    } else {
                        setImageResource(R.drawable.text_icon)
                    }
//                    scaleType = ImageView.ScaleType.FIT_CENTER
//                    adjustViewBounds = true
                }
            }
        }.start()
    }

// GENERIC DOCUMENTS

    private fun loadGenericDocument(fileUrl: String, fileName: String) {
        setupDocumentContainer(fileName, DocumentType.GENERIC)

        documentThumbnail?.apply {
            visibility = View.VISIBLE
            setImageResource(getFileTypeIcon(fileName))

        }
    }

// PREVIEW GENERATION HELPERS

    private fun createDocumentPreview(title: String, subtitle: String, iconRes: Int): Bitmap {
        val displayMetrics = context?.resources?.displayMetrics
        val width = displayMetrics?.widthPixels ?: 1080
        val height = displayMetrics?.heightPixels ?: 1920

        val bitmap = Bitmap.createBitmap(width, height - 160, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        canvas.drawColor(Color.WHITE)

        // Draw a simple preview layout
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Draw title
        paint.color = Color.BLACK
        paint.textSize = 48f
        paint.typeface = Typeface.DEFAULT_BOLD
        val titleX = width / 2f - paint.measureText(title) / 2f
        canvas.drawText(title, titleX, height / 2f - 100, paint)

        // Draw subtitle
        paint.textSize = 32f
        paint.typeface = Typeface.DEFAULT
        paint.color = Color.GRAY
        val subtitleX = width / 2f - paint.measureText(subtitle) / 2f
        canvas.drawText(subtitle, subtitleX, height / 2f - 40, paint)

        return bitmap
    }

    private fun createTextPreview(content: String, fileName: String): Bitmap {
        val displayMetrics = context?.resources?.displayMetrics
        val width = displayMetrics?.widthPixels ?: 1080
        val height = displayMetrics?.heightPixels ?: 1920

        val bitmap = Bitmap.createBitmap(width, height - 160, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        canvas.drawColor(Color.WHITE)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.BLACK
        paint.textSize = 24f

        // Draw content preview
        val lines = content.split('\n').take(20)
        var y = 80f

        for (line in lines) {
            if (y > height - 200) break
            val trimmedLine = if (line.length > 50) line.take(47) + "..." else line
            canvas.drawText(trimmedLine, 40f, y, paint)
            y += 35f
        }

        return bitmap
    }

    private fun createWebDocumentPreview(content: String, fileName: String): Bitmap {
        val displayMetrics = context?.resources?.displayMetrics
        val width = displayMetrics?.widthPixels ?: 1080
        val height = displayMetrics?.heightPixels ?: 1920

        val bitmap = Bitmap.createBitmap(width, height - 160, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        canvas.drawColor(Color.parseColor("#f8f9fa"))

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Title
        paint.color = Color.BLACK
        paint.textSize = 32f
        paint.typeface = Typeface.DEFAULT_BOLD
        val title = when {
            fileName.endsWith(".html") || fileName.endsWith(".htm") -> "HTML Document"
            fileName.endsWith(".xml") -> "XML Document"
            fileName.endsWith(".json") -> "JSON Document"
            else -> "Web Document"
        }
        canvas.drawText(title, 40f, 80f, paint)

        // Content preview
        paint.textSize = 20f
        paint.typeface = Typeface.MONOSPACE
        paint.color = Color.DKGRAY

        val cleanContent = content.replace(Regex("<[^>]*>"), "").trim()
        val lines = cleanContent.split('\n').take(15)
        var y = 140f

        for (line in lines) {
            if (y > height - 200) break
            val trimmedLine = if (line.length > 40) line.take(37) + "..." else line
            canvas.drawText(trimmedLine, 40f, y, paint)
            y += 30f
        }

        return bitmap
    }

    private fun createCsvPreview(lines: List<String>, fileName: String): Bitmap {
        val displayMetrics = context?.resources?.displayMetrics
        val width = displayMetrics?.widthPixels ?: 1080
        val height = displayMetrics?.heightPixels ?: 1920

        val bitmap = Bitmap.createBitmap(width, height - 160, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        canvas.drawColor(Color.WHITE)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Title
        paint.color = Color.BLACK
        paint.textSize = 32f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("CSV Spreadsheet", 40f, 80f, paint)

        // Table preview
        paint.textSize = 18f
        paint.typeface = Typeface.MONOSPACE

        var y = 140f
        for ((index, line) in lines.withIndex()) {
            if (y > height - 200) break

            paint.color = if (index == 0) Color.BLACK else Color.DKGRAY
            paint.typeface = if (index == 0) Typeface.DEFAULT_BOLD else Typeface.DEFAULT

            val columns = line.split(',').take(3)
            val displayLine = columns.joinToString(" | ") {
                if (it.length > 12) it.take(9) + "..." else it
            }

            canvas.drawText(displayLine, 40f, y, paint)
            y += 35f
        }

        return bitmap
    }

    private fun createEbookPreview(fileName: String): Bitmap {
        val displayMetrics = context?.resources?.displayMetrics
        val width = displayMetrics?.widthPixels ?: 1080
        val height = displayMetrics?.heightPixels ?: 1920

        val bitmap = Bitmap.createBitmap(width, height - 160, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        canvas.drawColor(Color.parseColor("#f5f5dc"))

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Book icon representation
        paint.color = Color.parseColor("#8b4513")
        val bookRect = RectF(width/2f - 150, height/2f - 200, width/2f + 150, height/2f + 100)
        canvas.drawRoundRect(bookRect, 20f, 20f, paint)

        // Title
        paint.color = Color.BLACK
        paint.textSize = 28f
        paint.typeface = Typeface.DEFAULT_BOLD
        val title = "E-Book"
        val titleX = width / 2f - paint.measureText(title) / 2f
        canvas.drawText(title, titleX, height/2f + 150, paint)

        // Filename
        paint.textSize = 20f
        paint.typeface = Typeface.DEFAULT
        paint.color = Color.GRAY
        val displayName = if (fileName.length > 20) fileName.take(17) + "..." else fileName
        val nameX = width / 2f - paint.measureText(displayName) / 2f
        canvas.drawText(displayName, nameX, height/2f + 180, paint)

        return bitmap
    }

    private fun getDocumentPreviewResource(documentType: DocumentType): Int {
        return when (documentType) {
            DocumentType.WORD -> R.drawable.word_icon
            DocumentType.EXCEL -> R.drawable.excel_icon
            DocumentType.POWERPOINT -> R.drawable.powerpoint_icon
            DocumentType.PDF -> R.drawable.pdf_icon
            DocumentType.TEXT -> R.drawable.text_icon
            DocumentType.WEB -> R.drawable.text_icon
            DocumentType.EBOOK -> R.drawable.text_icon
            DocumentType.CSV -> R.drawable.excel_icon
            DocumentType.GENERIC -> R.drawable.text_icon
        }
    }

    private fun downloadFile(fileUrl: String, outputFile: File) {
        val url = URL(fileUrl)
        val connection = url.openConnection().apply {
            connectTimeout = 5000
            readTimeout = 10000
            setRequestProperty("Accept-Encoding", "gzip")
        }

        connection.inputStream.use { input ->
            outputFile.outputStream().use { output ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                }
            }
        }
    }

//  EXISTING PDF LOADING

    private fun loadPdfFromUrl(fileUrl: String, fileName: String) {
        documentContainer?.visibility = View.VISIBLE

        // Show file type icon at top left corner with safe positioning
        fileTypeIcon?.let { iconView ->
            iconView.visibility = View.VISIBLE
            iconView.setImageResource(R.drawable.pdf_icon)

            // Ensure proper positioning with increased margins
            val params = iconView.layoutParams as? ViewGroup.MarginLayoutParams
            params?.let {
                it.topMargin = 32
                it.leftMargin = 24
                it.rightMargin = 0
                it.bottomMargin = 0
                iconView.layoutParams = it
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                iconView.elevation = 12f
            }
            iconView.bringToFront()
            iconView.invalidate()
        }

        // Ensure document name is visible
        documentName?.let { nameView ->
            nameView.visibility = View.VISIBLE
            nameView.text = fileName
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                nameView.elevation = 10f
            }
            nameView.bringToFront()
            nameView.invalidate()
        }

        // Ensure download button is visible
        downloadDocumentButton?.let { downloadBtn ->
            downloadBtn.visibility = View.VISIBLE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                downloadBtn.elevation = 10f
            }
            downloadBtn.bringToFront()
            downloadBtn.invalidate()
        }

        // Show immediate loading state
        pdfImageView?.apply {
            visibility = View.VISIBLE
            setImageResource(R.drawable.flash21)
            scaleType = ImageView.ScaleType.CENTER_CROP
            adjustViewBounds = true
        }



        // Start download and thumbnail generation immediately
        downloadAndGeneratePdfThumbnail(fileUrl, fileName) { thumbnail ->
            activity?.runOnUiThread {
                pdfImageView?.apply {
                    if (thumbnail != null) {
                        setImageBitmap(thumbnail)
//                        scaleType = ImageView.ScaleType.FIT_CENTER
//                        adjustViewBounds = true
                    } else {
                        setImageResource(R.drawable.pdf_icon)
//                        scaleType = ImageView.ScaleType.FIT_CENTER
//                        adjustViewBounds = true
                    }
                }
            }
        }

        Log.d(TAG, "PDF loading started for: $fileName")
    }

    private fun clearThumbnailCache() {
        pdfThumbnailCache.values.forEach { bitmap ->
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
        pdfThumbnailCache.clear()
    }

    // Keep existing PDF methods...
    private val pdfThumbnailCache = mutableMapOf<String, Bitmap>()

    @SuppressLint("UseKtx")
    private fun downloadAndGeneratePdfThumbnail(fileUrl: String, fileName: String, callback: (Bitmap?) -> Unit) {
        // Keep existing PDF thumbnail generation code unchanged
        Thread {
            var tempFile: File? = null
            var fileDescriptor: ParcelFileDescriptor? = null
            var pdfRenderer: PdfRenderer? = null

            try {
                // Create temporary file for PDF
                tempFile = File(context?.cacheDir, "temp_${System.currentTimeMillis()}_$fileName")

                // Download PDF file with optimized connection settings
                val url = URL(fileUrl)
                val connection = url.openConnection().apply {
                    connectTimeout = 5000
                    readTimeout = 10000
                    setRequestProperty("Accept-Encoding", "gzip")
                }

                // Download with progress tracking
                connection.inputStream.use { input ->
                    tempFile.outputStream().use { output ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                        }
                    }
                }

                // Generate thumbnail immediately after download
                fileDescriptor = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
                pdfRenderer = PdfRenderer(fileDescriptor)

                if (pdfRenderer.pageCount > 0) {
                    val page: PdfRenderer.Page = pdfRenderer.openPage(0)

                    // Get actual screen dimensions
                    val displayMetrics = context?.resources?.displayMetrics
                    val screenWidth = displayMetrics?.widthPixels ?: 1080
                    val screenHeight = displayMetrics?.heightPixels ?: 1920

                    // Account for UI elements - the XML margins handle the space
                    // Create bitmap that fits within the ImageView bounds
                    val renderWidth = screenWidth
                    val renderHeight = screenHeight - 160 // Account for top (60dp) + bottom (100dp) margins

                    // Create bitmap
                    val bitmap: Bitmap = Bitmap.createBitmap(renderWidth,
                        renderHeight, Bitmap.Config.ARGB_8888)
                    bitmap.eraseColor(android.graphics.Color.WHITE)

                    // Calculate scaling to maintain aspect ratio
                    val pageAspectRatio = page.width.toFloat() / page.height.toFloat()
                    val bitmapAspectRatio = renderWidth.toFloat() / renderHeight.toFloat()

                    val renderRect = if (pageAspectRatio > bitmapAspectRatio) {
                        // Page is wider - fit to width
                        val scaledHeight = (renderWidth / pageAspectRatio).toInt()
                        val yOffset = (renderHeight - scaledHeight) / 2
                        android.graphics.Rect(0, yOffset, renderWidth, yOffset + scaledHeight)
                    } else {
                        // Page is taller - fit to height
                        val scaledWidth = (renderHeight * pageAspectRatio).toInt()
                        val xOffset = (renderWidth - scaledWidth) / 2
                        android.graphics.Rect(xOffset, 0, xOffset + scaledWidth, renderHeight)
                    }

                    page.render(bitmap, renderRect, null,
                        PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()

                    callback(bitmap)
                } else {
                    callback(null)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error downloading/processing PDF: ${e.message}", e)
                callback(null)
            } finally {
                // Clean up resources
                try {
                    pdfRenderer?.close()
                    fileDescriptor?.close()
                    tempFile?.delete()
                } catch (e: Exception) {
                    Log.e(TAG, "Error cleaning up resources: ${e.message}")
                }
            }
        }.start()
    }



    enum class MediaType {
        IMAGE, VIDEO, AUDIO, DOCUMENT, UNKNOWN
    }
}


//  INTERFACES

interface OnMultipleFilesClickListener {
    fun multipleFileClickListener(
        currentIndex: Int,
        files: List<File>,
        fileIds: List<String>
    )
}

// MOCK CLASSES

// TODO: Replace with your actual Post class
class Post {
    var files: List<File> = emptyList()
    var fileIds: List<String> = emptyList()
}


class ImageViewerFragment : Fragment() {
    companion object {
        fun newInstance(
            files: List<File>,
            fileIds: List<String>,
            currentIndex: Int,
            postId: String,
            likeCount: Int,
            commentCount: Int,
            repostCount: Int,
            isLiked: Boolean,
            isReposted: Boolean
        ): ImageViewerFragment {
            val fragment = ImageViewerFragment()
            val args = Bundle().apply {
                // Add your arguments here
                putSerializable("files", ArrayList(files))
                putStringArrayList("fileIds", ArrayList(fileIds))
                putInt("currentIndex", currentIndex)
                putString("postId", postId)
                putInt("likeCount", likeCount)
                putInt("commentCount", commentCount)
                putInt("repostCount", repostCount)
                putBoolean("isLiked", isLiked)
                putBoolean("isReposted", isReposted)
            }
            fragment.arguments = args
            return fragment
        }
    }
}