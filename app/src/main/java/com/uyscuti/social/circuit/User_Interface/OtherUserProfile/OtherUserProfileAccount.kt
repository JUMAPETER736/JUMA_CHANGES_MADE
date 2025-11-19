package com.uyscuti.social.circuit.User_Interface.OtherUserProfile


import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.util.UnstableApi
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.OtherUserProfileAccountBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.Serializable
import kotlin.math.abs
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.uyscuti.social.network.api.retrofit.interfaces.IFlashapi
import com.uyscuti.social.network.utils.LocalStorage


private const val TAG = "OtherUserProfileAccount"



@UnstableApi
@AndroidEntryPoint
class OtherUserProfileAccount : AppCompatActivity() {

    companion object {
        private const val EXTRA_USER = "extra_user"
        private const val EXTRA_USER_ID = "extra_user_id"
        private const val EXTRA_USER_NAME = "extra_user_name"
        private const val EXTRA_USERNAME = "extra_username"
        private const val EXTRA_AVATAR_URL = "extra_avatar_url"
        private const val EXTRA_DIALOG_PHOTO = "extra_dialog_photo"
        private const val EXTRA_DIALOG_ID = "extra_dialog_id"
        private const val EXTRA_FULL_NAME = "user_full_name"

        fun open(context: Context, user: Any, dialogPhoto: String?, dialogId: String) {
            val intent = Intent(context, OtherUserProfileAccount::class.java).apply {
                putExtra(EXTRA_USER, user as? Serializable)
                putExtra(EXTRA_DIALOG_PHOTO, dialogPhoto)
                putExtra(EXTRA_DIALOG_ID, dialogId)
            }
            context.startActivity(intent)
        }

    }


    private lateinit var apiService: IFlashapi


    private lateinit var binding: OtherUserProfileAccountBinding
    private var isFollowing = false
    private lateinit var retrofitInstance: RetrofitInstance
    val userProfileLiveData = MutableLiveData<Any>()
    val onErrorFeedBack = MutableLiveData<String>()



    private var currentUsername: String = ""
    private var currentUserId: String = ""
    private var currentPage = 1

    // User data variables
    internal var userId: String = ""
    private var userName: String = ""
    internal var username: String = ""
    private var fullName: String = ""
    private var avatarUrl: String? = null
    private var followerCount: Int = 0
    private var followingCount: Int = 0
    private var likesCount: Int = 0
    private var joinDate: String = ""
    private var userLocation: String = ""
    private var userBio: String = ""


    private fun initializeApiService() {
        if (!::retrofitInstance.isInitialized) {
            val localStorage = LocalStorage(this)
            retrofitInstance = RetrofitInstance(localStorage, this)
        }
        apiService = retrofitInstance.apiService
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = OtherUserProfileAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeApiService()
        currentUsername = username
        currentUserId = userId

        extractUserData()
        setupUserInterface()
        setupToolbar()
        //setupTabLayout()
        setupClickListeners()
        setupScrollBehavior()
        setupStoryRingAnimation()
        observeUserProfile()

        // Load profile if we have username
        if (username.isNotEmpty()) {
            loadUserProfile(username)
        }
    }

    @SuppressLint("UseKtx")
    private fun setupClickListeners() {
        // Existing click listeners
        binding.backButton.setOnClickListener { finish() }

        binding.followIcon.setOnClickListener {
            isFollowing = !isFollowing
            binding.followIcon.text = if (isFollowing) "Following" else "Follow"
        }

        binding.actionMessage.setOnClickListener {
            Log.d(TAG, "Message button clicked for user: $userId")

        }

        binding.callButton.setOnClickListener {
            Log.d(TAG, "Call button clicked for user: $userId")

        }

        binding.shareProfileButton.setOnClickListener {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Check out $fullName's profile (@$username)!")
            }
            startActivity(Intent.createChooser(shareIntent, "Share Profile"))
        }

        binding.qrCodeButton.setOnClickListener {
            showQRCodeDialog()
            Log.d(TAG, "More QRCODE clicked for user: $userId")
        }

        binding.moreOptionsButton.setOnClickListener { view ->
            showOptionsMenu(view)
            Log.d(TAG, "More options clicked for user: $userId")
        }

        binding.followingSection.setOnClickListener {
            Log.d(TAG, "Following section clicked for user: $userId")
            openFollowingScreen()
        }

        binding.followersSection.setOnClickListener {
            Log.d(TAG, "Followers section clicked for user: $userId")
            openFollowerScreen()
        }

        binding.likesSection.setOnClickListener {
            Log.d(TAG, "Likes section clicked for user: $userId")

        }

        binding.linkInBio.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, "https://linktr.ee/$username".toUri())
            startActivity(intent)
        }

        binding.copyLinkButton.setOnClickListener {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(
                "Profile Link",
                "https://app.com/profile/$userId"
            )
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
        }


        // Profile Avatar and Frame
        binding.userProfileAvatar.setOnClickListener {
            Log.d(TAG, "Profile avatar clicked for user: $userId")
            showFullProfilePicture()
        }

        binding.avatarFrame.setOnClickListener {
            Log.d(TAG, "Avatar frame clicked for user: $userId")
            showFullProfilePicture()
        }

        binding.addFriendIcon.setOnClickListener {
            Log.d(TAG, "Add friend clicked for user: $userId")
            handleAddFriend()
        }

        binding.editProfileButton.setOnClickListener {
            Log.d(TAG, "Edit profile clicked for user: $userId")

        }

        binding.accountTypeBadge.setOnClickListener {
            Log.d(TAG, "Account type badge clicked for user: $userId")
            showAccountTypeInfo()
        }

        binding.trendingStatus.setOnClickListener {
            Log.d(TAG, "Trending status clicked for user: $userId")
            showTrendingInfo()
        }

        binding.youtubeButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, "https://youtube.com/@$username".toUri())
            startActivity(intent)
        }

        binding.instagramButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, "https://instagram.com/$username".toUri())
            startActivity(intent)
        }

        binding.tiktokShopBadge.setOnClickListener {
            Log.d(TAG, "TikTok Shop clicked for user: $userId")

        }



        binding.analyticsPreview.setOnClickListener {
            Log.d(TAG, "Analytics preview clicked for user: $userId")

        }

        binding.mutualConnectionsSection.setOnClickListener {
            Log.d(TAG, "Mutual connections clicked for user: $userId")
            showMutualConnections()
        }

        binding.userBioText.setOnClickListener {
            Log.d(TAG, "Bio text clicked for user: $userId")

        }

        binding.storyRing.setOnClickListener {
            Log.d(TAG, "Story ring clicked for user: $userId")
            openUserStories()
        }

        binding.liveIndicator.setOnClickListener {
            Log.d(TAG, "Live indicator clicked for user: $userId")
            joinLiveStream()
        }

        // Tab layout listener
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                Log.d(TAG, "Tab selected: ${tab?.position}")

            // Fragments handle their own data loading....
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })



        binding.messageEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                Log.d(TAG, "Message sent: ${binding.messageEditText.text}")

                binding.messageEditText.text.clear()
                true
            } else {
                false
            }
        }
    }

    private fun extractAndProcessProfile(profileData: Any) {
        try {
            // Extract user stats
            followerCount = extractFieldValueAsInt(profileData, "followersCount", "followers") ?: 0
            followingCount = extractFieldValueAsInt(profileData, "followingCount", "following") ?: 0
            val postsCount = extractFieldValueAsInt(profileData, "postsCount", "posts") ?: 0

            // Extract profile info
            val bio = extractFieldValue(profileData, "bio") ?: ""
            val location = extractFieldValue(profileData, "location") ?: ""
            val joinedDate = extractFieldValue(profileData, "joinedDate", "createdAt")

            // Extract user identity
            val extractedFirstName = extractFieldValue(profileData, "firstName") ?: ""
            val extractedLastName = extractFieldValue(profileData, "lastName") ?: ""

            // CRITICAL FIX: Use "owner" field (account ID), NOT "_id" (author profile ID)
            val extractedOwnerId = extractFieldValue(profileData, "owner") ?: ""
            val extractedProfileId = extractFieldValue(profileData, "_id", "id") ?: ""
            val extractedUsername = extractFieldValue(profileData, "username") ?: username

            // Extract avatar from nested structure
            val avatarFromProfile = extractNestedFieldValue(profileData, "account", "avatar", "url")

            // CRITICAL: Set userId to the owner/account ID, NOT the profile _id
            if (extractedOwnerId.isNotEmpty()) {
                userId = extractedOwnerId  // This is the correct account ID!
                Log.d(TAG, "✓ Set userId to owner/account ID: $userId")
            } else if (extractedProfileId.isNotEmpty()) {
                // Fallback: try to get owner from nested account
                val nestedOwnerId = extractNestedFieldValue(profileData, "account", "_id")
                if (!nestedOwnerId.isNullOrEmpty()) {
                    userId = nestedOwnerId
                    Log.d(TAG, "✓ Set userId from nested account._id: $userId")
                } else {
                    userId = extractedProfileId
                    Log.w(TAG, "⚠ Using profile _id as userId (may be wrong): $userId")
                }
            }

            if (extractedUsername.isNotEmpty()) username = extractedUsername

            if (extractedFirstName.isNotEmpty() || extractedLastName.isNotEmpty()) {
                fullName = "$extractedFirstName $extractedLastName".trim()
                if (fullName.isEmpty()) fullName = username
                userName = fullName
            }

            if (!avatarFromProfile.isNullOrEmpty()) {
                avatarUrl = avatarFromProfile
                Log.d(TAG, "Avatar URL: $avatarUrl")
            }

            // Set profile data
            userBio = bio.ifBlank {
                "✨ Content Creator | Dancer ✨\n🎵 Music Lover | Viral Videos\n#Dance #Comedy #Viral"
            }
            userLocation = location.ifBlank { "Lilongwe, Malawi" }
            joinDate = formatJoinDate(joinedDate)

            // Store posts count temporarily
            val initialPostsCount = postsCount

            Log.d(TAG, "Profile processed - userId: $userId, username: $username, name: $fullName")

            // Update UI immediately with available data
            lifecycleScope.launch(Dispatchers.Main) {
                setupUserInterface()
                // Show initial posts count
                binding.postsCount.text = formatCount(initialPostsCount)
            }

            // Fetch accurate posts count in background
            fetchUserTotalPostsAndLikes(userId)

        } catch (e: Exception) {
            Log.e(TAG, "Error processing profile data: ${e.message}", e)
        }
    }

    private fun extractFromUserObject(userObject: Any) {
        try {
            // CRITICAL: Extract owner field (account ID) first
            val extractedOwnerId = extractFieldValue(userObject, "owner")
            val extractedProfileId = extractFieldValue(userObject, "userId", "_id", "id")
            val extractedFirstName = extractFieldValue(userObject, "firstName", "first_name") ?: ""
            val extractedLastName = extractFieldValue(userObject, "lastName", "last_name") ?: ""
            val extractedUsername = extractFieldValue(userObject, "username")
            val extractedDisplayName = extractFieldValue(userObject, "displayName", "name")

            // CRITICAL FIX: Prioritize owner field over _id
            if (!extractedOwnerId.isNullOrBlank()) {
                userId = extractedOwnerId
                Log.d(TAG, "✓ Using owner as userId: $userId")
            } else {
                // Fallback: try nested account._id
                val nestedAccountId = extractNestedFieldValue(userObject, "account", "_id")
                if (!nestedAccountId.isNullOrBlank()) {
                    userId = nestedAccountId
                    Log.d(TAG, "✓ Using account._id as userId: $userId")
                } else if (!extractedProfileId.isNullOrBlank()) {
                    userId = extractedProfileId
                    Log.w(TAG, "⚠ Using profile _id as userId (may be incorrect): $userId")
                }
            }

            if (!extractedUsername.isNullOrBlank()) username = extractedUsername

            // Build full name
            fullName = when {
                !extractedDisplayName.isNullOrBlank() -> extractedDisplayName
                extractedFirstName.isNotBlank() && extractedLastName.isNotBlank() -> "$extractedFirstName $extractedLastName"
                extractedFirstName.isNotBlank() -> extractedFirstName
                extractedLastName.isNotBlank() -> extractedLastName
                !extractedUsername.isNullOrBlank() -> extractedUsername
                else -> "Unknown User"
            }

            // Try to extract avatar URL
            avatarUrl = extractNestedFieldValue(userObject, "avatar", "url") ?:
                    extractNestedFieldValue(userObject, "account", "avatar", "url")

            Log.d(TAG, "Extracted from user object - ID: $userId, Name: $fullName, Username: $username")
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting from user object: ${e.message}", e)
        }
    }

    private fun extractUserData() {
        try {
            Log.d(TAG, "Starting to extract user data from Intent")

            // Try to get specific user data first
            userId = intent.getStringExtra(EXTRA_USER_ID) ?: ""
            userName = intent.getStringExtra(EXTRA_USER_NAME) ?: ""
            username = intent.getStringExtra(EXTRA_USERNAME) ?: ""
            avatarUrl = intent.getStringExtra(EXTRA_AVATAR_URL)
            fullName = intent.getStringExtra(EXTRA_FULL_NAME) ?: ""

            Log.d(TAG, "Direct extras - ID: $userId, Full Name: $fullName, Username: $username")

            // If specific data not available, try to extract from user object
            if (userId.isEmpty() || username.isEmpty()) {
                Log.d(TAG, "Direct extras incomplete, trying user object")
                val userObject = intent.getSerializableExtra(EXTRA_USER)
                if (userObject != null) {
                    extractFromUserObject(userObject)
                }
            }

            // Ensure fullName is set properly
            if (fullName.isEmpty() && userName.isNotEmpty()) {
                fullName = userName
            } else if (fullName.isEmpty() && username.isNotEmpty()) {
                fullName = username
            }

            Log.d(TAG, "Final data - ID: $userId, Name: $fullName, Username: $username")

        } catch (e: Exception) {
            Log.e(TAG, "Error extracting user data: ${e.message}", e)
            fullName = "Unknown User"
            userName = fullName
            username = "unknown"
            userId = "unknown_id"
        }
    }

    private fun fetchUserTotalPostsAndLikes(userId: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching posts for username: $username")

                var totalPosts = 0
                var totalLikes = 0
                var page = 1
                var hasMorePages = true

                // Fetch all pages to get accurate count
                while (hasMorePages && page <= 10) { // Limit to 10 pages for safety
                    val response = retrofitInstance.apiService.getShortsByUsername(username)

                    if (response.isSuccessful) {
                        val body = response.body()?.data

                        body?.posts?.let { posts ->
                            // Count posts by this username
                            val userPosts = posts.filter { post ->
                                post.author?.account?.username == username
                            }

                            totalPosts += userPosts.size

                            // Sum likes
                            userPosts.forEach { post ->
                                totalLikes += post.likes
                            }
                        }

                        // Check for more pages
                        hasMorePages = body?.hasNextPage == true
                        page++
                    } else {
                        Log.e(TAG, "API error: ${response.code()}")
                        break
                    }
                }

                Log.d(TAG, "Total posts: $totalPosts, Total likes: $totalLikes")

                // Update UI
                withContext(Dispatchers.Main) {
                    binding.postsCount.text = formatCount(totalPosts)
                    // Uncomment if you have a likes display:
                    // binding.likesCount.text = formatCount(totalLikes)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching posts: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    binding.postsCount.text = "0"
                }
            }
        }
    }

    private fun setupUserInterface() {
        binding.fullName.text = fullName.ifEmpty { "Unknown User" }
        binding.userName.text = if (username.isNotEmpty()) "@$username" else "@unknown"
        binding.toolbarUserName.text = if (username.isNotEmpty()) "@$username" else "@unknown"

        binding.followersCount.text = formatCount(followerCount)
        binding.followingCount.text = formatCount(followingCount)
        // Posts count will be updated by fetchUserTotalPostsAndLikes()
        // Don't set it here to avoid showing wrong data

        binding.dateJoined.text = joinDate.ifEmpty { "Join date not available" }
        binding.userActualLocation.text = userLocation
        binding.userBioText.text = userBio

        loadProfileImage()
    }


    private fun showFullProfilePicture() {

        Toast.makeText(this, "View full profile picture", Toast.LENGTH_SHORT).show()
    }

    private fun handleAddFriend() {
        lifecycleScope.launch {
            try {
                val response = apiService.followUnFollow(userId)
                if (response.isSuccessful) {
                    Toast.makeText(this@OtherUserProfileAccount, "Friend request sent", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error adding friend", e)
            }
        }
    }

    private fun showAccountTypeInfo() {
        AlertDialog.Builder(this)
            .setTitle("Account Type")
            .setMessage("This is a creator account with verified status")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showTrendingInfo() {
        Toast.makeText(this, "This user is trending!", Toast.LENGTH_SHORT).show()
    }


    private fun joinLiveStream() {
        Toast.makeText(this, "Joining live stream...", Toast.LENGTH_SHORT).show()

    }

    private fun showMutualConnections() {
        Toast.makeText(this, "Mutual Connections", Toast.LENGTH_SHORT).show()

    }

    private fun openUserStories() {
        Toast.makeText(this, "Opening stories...", Toast.LENGTH_SHORT).show()

    }


    private fun loadUserProfile(username: String) {
        if (!::retrofitInstance.isInitialized) {
            val localStorage = LocalStorage(this)
            retrofitInstance = RetrofitInstance(localStorage, this)
        }
        getOtherUsersProfile(username)
    }

    private fun observeUserProfile() {
        userProfileLiveData.observe(this) { profileData ->
            Log.d(TAG, "Profile data received: $profileData")

            // Setup TabLayout AFTER userId is correctly extracted
            if (binding.viewPager2.adapter == null) {
                Log.d(TAG, "Setting up TabLayout with correct userId: $userId, username: $username")
                setupTabLayout()
            }
        }

        onErrorFeedBack.observe(this) { errorMessage ->
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getOtherUsersProfile(username: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = retrofitInstance.apiService.getOtherUsersProfileByUsername(username)
                val responseBody = response.body()

                if (responseBody != null) {
                    extractAndProcessProfile(responseBody.data)
                    withContext(Dispatchers.Main) {
                        userProfileLiveData.postValue(responseBody.data)
                    }
                    Log.d(TAG, "Profile loaded successfully for: $username")
                } else {
                    withContext(Dispatchers.Main) {
                        onErrorFeedBack.postValue("User data is empty")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading profile: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onErrorFeedBack.postValue("Error connecting to server. Check internet connection.")
                }
            }
        }
    }


    private fun loadProfileImage() {
        val imageView = binding.userProfileAvatar

        if (!avatarUrl.isNullOrEmpty()) {
            try {
                Glide.with(this)
                    .load(avatarUrl)
                    .apply(
                        RequestOptions()
                            .circleCrop()
                            .placeholder(R.drawable.flash21)
                            .error(R.drawable.flash21)
                    )
                    .into(imageView)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading profile image: ${e.message}", e)
                imageView.setImageResource(R.drawable.flash21)
            }
        } else {
            imageView.setImageResource(R.drawable.flash21)
        }
    }

    private fun extractFieldValue(obj: Any, vararg fieldNames: String): String? {
        for (fieldName in fieldNames) {
            try {
                val field = obj.javaClass.getDeclaredField(fieldName)
                field.isAccessible = true
                val value = field.get(obj)
                return when (value) {
                    null -> null
                    is String -> value
                    is Date -> value.toString()
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

    private fun extractFieldValueAsInt(obj: Any, vararg fieldNames: String): Int? {
        for (fieldName in fieldNames) {
            try {
                val field = obj.javaClass.getDeclaredField(fieldName)
                field.isAccessible = true
                val value = field.get(obj)
                return when (value) {
                    is Int -> value
                    is Long -> value.toInt()
                    is String -> value.toIntOrNull()
                    null -> null
                    else -> value.toString().toIntOrNull()
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

    private fun formatJoinDate(dateString: String?): String {
        if (dateString.isNullOrBlank()) return "Join date not available"

        return try {
            val formats = arrayOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd"
            )

            var parsedDate: Date? = null
            for (format in formats) {
                try {
                    val sdf = SimpleDateFormat(format, Locale.getDefault())
                    if (format.contains("'Z'")) {
                        sdf.timeZone = TimeZone.getTimeZone("UTC")
                    }
                    parsedDate = sdf.parse(dateString)
                    break
                } catch (e: Exception) {
                    continue
                }
            }

            if (parsedDate != null) {
                val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                "Joined ${formatter.format(parsedDate)}"
            } else {
                "Joined $dateString"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing date: $dateString", e)
            "Joined $dateString"
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

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbarUserName.alpha = 0f
    }



    private fun setupTabLayout() {

        val adapter = ProfilePagerAdapter(
            this,
            userId = userId,
            username = username
        )
        binding.viewPager2.adapter = adapter


        // Ensure ViewPager2 fills width properly
        binding.viewPager2.offscreenPageLimit = 3

        TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
            when (position) {
                0 -> tab.setIcon(R.drawable.scroll_text_line_svgrepo_com)
                1 -> tab.setIcon(R.drawable.play_svgrepo_com_white)
                2 -> tab.setIcon(R.drawable.favorite_black)
                3 -> tab.setIcon(R.drawable.business_bag_svgrepo_com)
            }
        }.attach()
    }



    private fun setupScrollBehavior() {

        binding.appBarLayout.addOnOffsetChangedListener(

            AppBarLayout.OnOffsetChangedListener {
                                                 appBarLayout, verticalOffset ->

                val totalScrollRange = appBarLayout.totalScrollRange
            val percentage = abs(verticalOffset).toFloat() / totalScrollRange.toFloat()
            binding.toolbarUserName.alpha = percentage
        })
    }

    private fun setupStoryRingAnimation() {
        val animator = ObjectAnimator.ofFloat(binding.storyRing, "rotation", 0f, 360f)
        animator.duration = 2000
        animator.repeatCount = ObjectAnimator.INFINITE
        animator.repeatMode = ObjectAnimator.RESTART
        animator.start()
    }

    @SuppressLint("SetTextI18n")
    private fun showQRCodeDialog() {

        val dialog = Dialog(this)

        dialog.setContentView(R.layout.qr_code_dialog)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val qrImageView = dialog.findViewById<ImageView>(R.id.qrImageView)
        val usernameText = dialog.findViewById<TextView>(R.id.usernameText)
        val profileImageView = dialog.findViewById<ImageView>(R.id.profileImageView)
        val verificationBadge = dialog.findViewById<ImageView>(R.id.verificationBadge)

        // Set username
        usernameText.text = "@$username"

        // Set profile image (you may need to load this with Glide/Picasso)
        // Glide.with(this).load(userProfileImageUrl).into(profileImageView)
        profileImageView.setImageResource(R.drawable.flash21) // Placeholder

        // Show verification badge if user is verified
        // verificationBadge.visibility = if (isUserVerified) View.VISIBLE else View.GONE

        // Generate QR code
        val profileUrl = "https://app.com/profile/$userId"
        val qrCodeBitmap = generateQRCode(profileUrl, 512, 512)
        qrImageView.setImageBitmap(qrCodeBitmap)

        // Handle dialog click for actions (save/share)
        dialog.setOnCancelListener {
            showQRCodeActions(qrCodeBitmap)
        }

        // Long press to save directly
        qrImageView.setOnLongClickListener {
            saveQRCodeToGallery(qrCodeBitmap)
            Toast.makeText(this, "QR code saved to gallery", Toast.LENGTH_SHORT).show()
            true
        }

        // Tap to share
        qrImageView.setOnClickListener {
            shareQRCode(qrCodeBitmap)
        }

        dialog.show()
    }

    private fun showQRCodeActions(qrCodeBitmap: Bitmap?) {
        val options = arrayOf("Save to Gallery", "Share QR Code", "Cancel")

        AlertDialog.Builder(this)
            .setTitle("QR Code Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        saveQRCodeToGallery(qrCodeBitmap)
                        Toast.makeText(this, "QR code saved to gallery", Toast.LENGTH_SHORT).show()
                    }
                    1 -> shareQRCode(qrCodeBitmap)
                }
            }
            .show()
    }

    @SuppressLint("UseKtx")
    private fun generateQRCode(text: String, width: Int, height: Int): Bitmap? {
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height)
            val bitmap = createBitmap(width, height, Bitmap.Config.RGB_565)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap[x, y] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                }
            }
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Error generating QR code", e)
            null
        }
    }

    private fun saveQRCodeToGallery(bitmap: Bitmap?) {
        bitmap?.let {

            try {
                val filename = "QR_${username}_${System.currentTimeMillis()}.jpg"
                val fos: OutputStream?

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resolver = contentResolver
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    }
                    val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    fos = imageUri?.let { resolver.openOutputStream(it) }

                } else {
                    Log.e(TAG, "Error in saving QR code to the Gallery")
                }

                Log.e(TAG, "Error in saving QR code to the Gallery")

            } catch (e: Exception) {
                Log.e(TAG, "Error saving QR code", e)

            }

        }

    }

    private fun shareQRCode(bitmap: Bitmap?) {
        bitmap?.let {
            try {
                val bytes = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
                val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "QR_$username", null)
                val uri = Uri.parse(path)

                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, uri)
                    type = "image/jpeg"
                    putExtra(Intent.EXTRA_TEXT, "Scan to follow @$username!")
                }
                startActivity(Intent.createChooser(shareIntent, "Share QR Code"))
            } catch (e: Exception) {
                Log.e(TAG, "Error sharing QR code", e)
            }
        }
    }

    private fun showOptionsMenu(anchor: View) {
        val popup = PopupMenu(this, anchor)  // Use 'this' instead of requireContext()

        // Apply custom style and force show icons (optional)
        try {
            val fieldPopup = PopupMenu::class.java.getDeclaredField("mPopup")
            fieldPopup.isAccessible = true
            val menuPopupWindow = fieldPopup.get(popup)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Rest of your code remains the same...
        popup.menuInflater.inflate(R.menu.post_options_menu, popup.menu)

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

        popup.show()
    }

    private fun handleReportPost() {
        Toast.makeText(this, "Report post", Toast.LENGTH_SHORT).show()
    }

    private fun handleBlockUser() {
        Toast.makeText(this, "User blocked", Toast.LENGTH_SHORT).show()
    }

    private fun handleMuteUser() {
        Toast.makeText(this, "User muted", Toast.LENGTH_SHORT).show()
    }

    private fun handleCopyLink() {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Post Link", "https://example.com/post/123")
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun handleSavePost() {
        Toast.makeText(this, "Post saved", Toast.LENGTH_SHORT).show()
    }

    private fun handleNotInterested() {
        Toast.makeText(this, "We'll show you fewer posts like this", Toast.LENGTH_SHORT).show()
    }


    // FOLLOWERS SCREEN
    private fun openFollowerScreen() {
        val intent = Intent(this, UserFollowersFragment::class.java).apply {
            putExtra("user_id", userId)
            putExtra("username", username)
            putExtra("full_name", fullName)
            putExtra("followers_count", followerCount)
            putExtra("tab_index", 0)
        }


        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

        findViewById<View>(android.R.id.content).performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)

    }

    // FOLLOWING SCREEN
    private fun openFollowingScreen() {
        val intent = Intent(this, UserFollowingFragment::class.java).apply {
            putExtra("user_id", userId)
            putExtra("username", username)
            putExtra("full_name", fullName)
            putExtra("following_count", followingCount)
            putExtra("tab_index", 0)
        }

        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

        findViewById<View>(android.R.id.content).performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)

    }


    inner class ProfilePagerAdapter(
        private val activity: FragmentActivity,
        private val userId: String,
        private val username: String

    ) : FragmentStateAdapter(activity) {

        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> {

                    // Posts Fragment
                    AllOtherUsersPostsFragment.newInstance(userId, username)
                }

                1 -> {

                    // Videos Fragment
                    AllVideosOnlyFragment().apply {
                        arguments = Bundle().apply {
                            putString("userId", userId)
                            putString("username", username)
                        }
                    }
                }
                2 -> {

                    // Favorites Fragment
                    AllOtherUsersFavoritesFragment().apply {
                        arguments = Bundle().apply {
                            putString("userId", userId)
                            putString("username", username)
                        }
                    }
                }

                3 -> {

                    // Business Fragment
                    AllOtherUsersBusinessFragment().apply {
                        arguments = Bundle().apply {
                            putString("userId", userId)
                            putString("username", username)
                        }
                    }

                }

                else -> throw IllegalStateException("Invalid position: $position")

            }
        }
    }


}





