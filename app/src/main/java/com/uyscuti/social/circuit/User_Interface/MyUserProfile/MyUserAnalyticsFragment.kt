package com.uyscuti.social.circuit.User_Interface.MyUserProfile

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.MyUserAnalyticsFragmentBinding
import com.uyscuti.social.circuit.databinding.MyUserItemAnalyticsBinding
import com.uyscuti.social.circuit.User_Interface.Log_In_And_Register.LoginActivity.UserStorageHelper.getAccessToken
import com.uyscuti.social.circuit.User_Interface.Log_In_And_Register.LoginActivity.UserStorageHelper.getAvatarUrl
import com.uyscuti.social.circuit.User_Interface.Log_In_And_Register.LoginActivity.UserStorageHelper.getEmail
import com.uyscuti.social.circuit.User_Interface.Log_In_And_Register.LoginActivity.UserStorageHelper.getUserId
import com.uyscuti.social.circuit.User_Interface.Log_In_And_Register.LoginActivity.UserStorageHelper.getUsername
import com.uyscuti.social.network.api.response.posts.Post
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.api.retrofit.interfaces.IFlashapi
import com.uyscuti.social.network.utils.LocalStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

private const val TAG = "MyUserAnalyticsFragment"

@AndroidEntryPoint
class MyUserAnalyticsFragment : Fragment() {

    companion object {
        private const val ARG_USER_ID = "userId"
        private const val ARG_USERNAME = "username"

        // Static cache that survives fragment recreation
        private val analyticsCache = mutableMapOf<String, CachedAnalyticsData>()

        // Pagination limits
        private const val MAX_PAGES = 10
        private const val MAX_POSTS = 50
        private const val INITIAL_LOAD_PAGES = 5

        // Cache duration: 5 minutes
        private const val CACHE_DURATION_MS = 5 * 60 * 1000L

        data class CachedAnalyticsData(
            val posts: List<Post>,
            val timestamp: Long,
            val hasMoreData: Boolean,
            val lastPage: Int
        ) {
            fun isValid(): Boolean {
                return (System.currentTimeMillis() - timestamp) < CACHE_DURATION_MS
            }
        }

        fun newInstance(userId: String, username: String): MyUserAnalyticsFragment {
            return MyUserAnalyticsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_USER_ID, userId)
                    putString(ARG_USERNAME, username)
                }
            }
        }

        fun clearCache(username: String) {
            analyticsCache.remove(username)
            Log.d(TAG, "Cache cleared for @$username")
        }
    }

    private var _binding: MyUserAnalyticsFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var apiService: IFlashapi
    private var userId: String = ""
    private var username: String = ""
    private var cleanUsername: String = ""
    private var selectedTimeRange: String = "7 days"

    // Profile data
    private var avatarUrl: String? = null
    private var fullName: String = ""

    // Analytics data
    private var allUserPosts = mutableListOf<Post>()
    private var isLoading = false
    private var hasMoreData = true
    private val BLUEJEANS_COLOR = Color.parseColor("#5B9BD5")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = arguments?.getString(ARG_USER_ID) ?: ""
        username = arguments?.getString(ARG_USERNAME) ?: ""
        cleanUsername = username.trim().lowercase()

        val localStorage = LocalStorage(requireContext())
        val retrofitInstance = RetrofitInstance(localStorage, requireContext())
        apiService = retrofitInstance.apiService

        // Load user data from UserStorageHelper
        loadLoggedInUserDetails()

        // Load from cache IMMEDIATELY in onCreate
        val cacheKey = getCacheKey()
        analyticsCache[cacheKey]?.let { cached ->
            if (cached.isValid()) {
                Log.d(TAG, "✓ Cache HIT in onCreate! Loading ${cached.posts.size} posts instantly")
                allUserPosts = cached.posts.toMutableList()
            } else {
                Log.d(TAG, "Cache expired, will fetch fresh data")
                analyticsCache.remove(cacheKey)
            }
        }
    }

    private fun loadLoggedInUserDetails() {
        try {
            // Get data from UserStorageHelper
            val storedUserId = getUserId(requireContext())
            val storedUsername = getUsername(requireContext())
            val storedAvatarUrl = getAvatarUrl(requireContext())
            val storedEmail = getEmail(requireContext())

            if (storedUserId.isNotEmpty() && storedUsername.isNotEmpty()) {
                // Update local variables if they match
                if (userId.isEmpty() || userId == storedUserId) {
                    userId = storedUserId
                    username = storedUsername
                    cleanUsername = username.trim().lowercase()
                    avatarUrl = storedAvatarUrl

                    Log.d(TAG, "✓ Loaded user from storage - ID: $userId, Username: $username")

                    // Load full profile from API
                    loadUserProfile()
                }
            } else {
                Log.w(TAG, "No logged in user found in UserStorageHelper")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading user from UserStorageHelper: ${e.message}", e)
        }
    }

    private fun loadUserProfile() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getMyProfile()

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!.data
                    Log.d(TAG, "✓ Fetched profile: ${data.firstName} ${data.lastName}")

                    // Map data to local vars
                    fullName = "${data.firstName} ${data.lastName}".trim()
                    avatarUrl = data.account.avatar.url

                    // Update UI on main thread
                    withContext(Dispatchers.Main) {
                        updateProfileUI()
                    }
                } else {
                    Log.e(TAG, "Failed to load profile: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading profile", e)
            }
        }
    }

    private fun updateProfileUI() {
        // Update username display
        binding.tvUsername.text = if (fullName.isNotEmpty()) {
            "$fullName (@$username)"
        } else {
            "@$username"
        }

        // Load profile image if you have an ImageView in your layout
        // Uncomment if you add profileImageView to your layout
        /*
        avatarUrl?.let { url ->
            if (url.isNotEmpty()) {
                Glide.with(this)
                    .load(url)
                    .apply(
                        RequestOptions()
                            .circleCrop()
                            .placeholder(R.drawable.flash21)
                            .error(R.drawable.flash21)
                    )
                    .into(binding.profileImageView)
            }
        }
        */
    }

    private fun getCacheKey(): String = "analytics_${userId}_${cleanUsername}"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MyUserAnalyticsFragmentBinding.inflate(inflater, container, false)

        // Username - will be updated in updateProfileUI()
        binding.tvUsername.text = "@$username"

        // Spinner Setup
        val timeRanges = arrayOf("7 days", "28 days", "60 days")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, timeRanges)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTimeRange.adapter = adapter

        binding.spinnerTimeRange.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedTimeRange = timeRanges[position]
                // Update UI with cached data
                if (allUserPosts.isNotEmpty()) {
                    updateAnalyticsData()
                    setupCharts()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // RecyclerView Setup with optimizations
        binding.rvAnalyticsMetrics.apply {
            layoutManager = GridLayoutManager(context, 2)
            setHasFixedSize(true)
            setItemViewCacheSize(6) // Cache all 6 metric cards
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Update profile UI if data is already loaded
        if (avatarUrl != null || fullName.isNotEmpty()) {
            updateProfileUI()
        }

        val cacheKey = getCacheKey()
        val cached = analyticsCache[cacheKey]

        if (cached != null && cached.isValid()) {
            // INSTANT LOAD from cache
            Log.d(TAG, "🚀 INSTANT LOAD - Displaying ${cached.posts.size} cached posts")

            allUserPosts = cached.posts.toMutableList()
            displayAnalyticsData()

            // Background refresh if needed
            if (cached.posts.size < MAX_POSTS && cached.hasMoreData) {
                Log.d(TAG, "Background refresh starting...")
                backgroundRefresh()
            }
        } else {
            // Fresh load
            Log.d(TAG, "Fresh load starting...")
            loadAnalyticsData()
        }
    }

    private fun displayAnalyticsData() {
        updateAnalyticsData()
        setupCharts()
        Log.d(TAG, "✓ Displayed analytics for ${allUserPosts.size} posts")
    }

    private fun backgroundRefresh() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val cacheKey = getCacheKey()
                val cached = analyticsCache[cacheKey] ?: return@launch

                val tempPosts = cached.posts.toMutableList()
                var page = cached.lastPage + 1

                while (page <= MAX_PAGES && tempPosts.size < MAX_POSTS) {
                    val response = apiService.getAllFeed(page = page.toString())

                    if (response.isSuccessful) {
                        val posts = response.body()?.data?.data?.posts ?: emptyList()
                        val userPosts = posts.mapNotNull { if (isUserPost(it)) it else null }

                        if (userPosts.isNotEmpty()) {
                            tempPosts.addAll(userPosts)

                            // Update cache silently
                            analyticsCache[cacheKey] = CachedAnalyticsData(
                                posts = tempPosts.take(MAX_POSTS),
                                timestamp = System.currentTimeMillis(),
                                hasMoreData = tempPosts.size < MAX_POSTS,
                                lastPage = page
                            )
                        }

                        val hasNextPage = response.body()?.data?.data?.hasNextPage ?: false
                        if (!hasNextPage) break

                        page++
                    } else {
                        break
                    }
                }

                Log.d(TAG, "✓ Background refresh complete: ${tempPosts.size} posts")
            } catch (e: Exception) {
                Log.e(TAG, "Background refresh error: ${e.message}")
            }
        }
    }

    private fun loadAnalyticsData() {
        if (isLoading) {
            Log.d(TAG, "Already loading, skipping...")
            return
        }

        isLoading = true
        Log.d(TAG, "Loading analytics data for @$username")

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                allUserPosts.clear()
                var currentPage = 1
                hasMoreData = true

                // Smart pagination: load 3-5 pages initially
                while (hasMoreData && currentPage <= INITIAL_LOAD_PAGES && allUserPosts.size < MAX_POSTS) {
                    val response = apiService.getAllFeed(page = currentPage.toString())

                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        val posts = responseBody?.data?.data?.posts ?: emptyList()

                        // Filter posts by this user
                        val userPosts = posts.mapNotNull { post ->
                            if (isUserPost(post)) post else null
                        }

                        if (userPosts.isNotEmpty()) {
                            allUserPosts.addAll(userPosts)

                            withContext(Dispatchers.Main) {
                                // Update UI progressively
                                displayAnalyticsData()
                            }
                        }

                        Log.d(TAG, "Page $currentPage: Found ${userPosts.size} posts (Total: ${allUserPosts.size})")

                        // Check if we hit limits
                        if (allUserPosts.size >= MAX_POSTS) {
                            Log.d(TAG, "⚠️ Reached MAX_POSTS limit ($MAX_POSTS)")
                            hasMoreData = false
                            break
                        }

                        // Check pagination
                        hasMoreData = responseBody?.data?.data?.hasNextPage ?: false
                        val totalPages = responseBody?.data?.data?.totalPages ?: currentPage

                        if (!hasMoreData || currentPage >= totalPages || currentPage >= MAX_PAGES) {
                            hasMoreData = false
                            break
                        }

                        currentPage++
                    } else {
                        Log.e(TAG, "API error: ${response.code()}")
                        hasMoreData = false
                        break
                    }
                }

                // Cache the results
                val cacheKey = getCacheKey()
                analyticsCache[cacheKey] = CachedAnalyticsData(
                    posts = allUserPosts.take(MAX_POSTS),
                    timestamp = System.currentTimeMillis(),
                    hasMoreData = hasMoreData && currentPage < MAX_PAGES,
                    lastPage = currentPage
                )

                withContext(Dispatchers.Main) {
                    isLoading = false
                    displayAnalyticsData()
                    Log.d(TAG, "✓ Analytics loaded: ${allUserPosts.size} posts for @$username")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error loading analytics: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    isLoading = false
                    Toast.makeText(requireContext(), "Error loading analytics", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun isUserPost(post: Post): Boolean {
        val isDirectPost = post.author?.let { author ->
            val matchesUserId = author.owner == userId
            val apiUsername = author.account?.username?.trim()?.lowercase()
            val matchesUsername = apiUsername == cleanUsername
            val isValid = author.account != null
            matchesUserId && matchesUsername && isValid
        } ?: false

        val isRepostOfUserContent = post.isReposted == true &&
                !post.originalPost.isNullOrEmpty() &&
                post.originalPost[0].author?.let { originalAuthor ->
                    val matchesUserId = originalAuthor.owner == userId
                    val apiUsername = originalAuthor.account?.username?.trim()?.lowercase()
                    val matchesUsername = apiUsername == cleanUsername
                    val isValid = originalAuthor.account != null
                    matchesUserId && matchesUsername && isValid
                } ?: false

        return isDirectPost || isRepostOfUserContent
    }

    private fun updateAnalyticsData() {
        val analyticsData = calculateAnalyticsData()
        binding.rvAnalyticsMetrics.adapter = AnalyticsAdapter(analyticsData)
    }

    private fun calculateAnalyticsData(): List<AnalyticsItem> {
        if (allUserPosts.isEmpty()) {
            return listOf(
                AnalyticsItem("Video Views", "0", "0%", R.drawable.ic_camera_on, true),
                AnalyticsItem("Profile Views", "0", "0%", R.drawable.flash21, true),
                AnalyticsItem("Likes", "0", "0%", R.drawable.filled_favorite_like, true),
                AnalyticsItem("Comments", "0", "0%", R.drawable.comments, true),
                AnalyticsItem("Shares", "0", "0%", R.drawable.share_svgrepo_com, true),
                AnalyticsItem("Followers Gained", "0", "0%", R.drawable.ic_followers_gained, true)
            )
        }

        // Calculate totals from actual post data
        var totalVideoViews = 0
        var totalLikes = 0
        var totalComments = 0
        var totalShares = 0
        var totalReposts = 0

        allUserPosts.forEach { post ->
            val actualPost = if (post.isReposted == true && !post.originalPost.isNullOrEmpty()) {
                post.originalPost[0]
            } else {
                null
            }

            // Count video views
            val hasVideo = post.files.any { it.mimeType == "video" } ||
                    (actualPost?.files?.any { it.mimeType == "video" } == true)

            if (hasVideo) {
                val estimatedViews = if (actualPost != null) {
                    (actualPost.likeCount ?: 0) * 10
                } else {
                    (post.likes ?: 0) * 10
                }
                totalVideoViews += estimatedViews
            }

            // Use actualPost data if available
            if (actualPost != null) {
                totalLikes += actualPost.likeCount ?: 0
                totalComments += actualPost.commentCount ?: 0
                totalShares += actualPost.shareCount ?: 0
                totalReposts += actualPost.repostCount ?: 0
            } else {
                totalLikes += post.likes ?: 0
                totalComments += post.comments ?: 0
                totalShares += post.shareCount ?: 0
                totalReposts += post.repostCount ?: 0
            }
        }

        // Calculate profile views
        val totalProfileViews = allUserPosts.sumOf { post ->
            val actualPost = if (post.isReposted == true && !post.originalPost.isNullOrEmpty()) {
                post.originalPost[0]
            } else {
                null
            }
            val likes = actualPost?.likeCount ?: post.likes ?: 0
            (likes * 15).toLong()
        }.toInt()

        // Calculate percentage changes (mock previous period)
        val prevVideoViews = (totalVideoViews * 0.8).toInt()
        val prevProfileViews = (totalProfileViews * 0.8).toInt()
        val prevLikes = (totalLikes * 0.8).toInt()
        val prevComments = (totalComments * 0.8).toInt()
        val prevShares = (totalShares * 0.8).toInt()
        val prevFollowers = 100

        val videoViewsChange = calculatePercentageChange(prevVideoViews, totalVideoViews)
        val profileViewsChange = calculatePercentageChange(prevProfileViews, totalProfileViews)
        val likesChange = calculatePercentageChange(prevLikes, totalLikes)
        val commentsChange = calculatePercentageChange(prevComments, totalComments)
        val sharesChange = calculatePercentageChange(prevShares, totalShares)

        val followersGained = allUserPosts.size * 2
        val followersChange = calculatePercentageChange(prevFollowers, followersGained)

        return listOf(
            AnalyticsItem("Video Views", formatNumber(totalVideoViews), formatPercentage(videoViewsChange), R.drawable.ic_camera_on, videoViewsChange >= 0),
            AnalyticsItem("Profile Views", formatNumber(totalProfileViews), formatPercentage(profileViewsChange), R.drawable.flash21, profileViewsChange >= 0),
            AnalyticsItem("Likes", formatNumber(totalLikes), formatPercentage(likesChange), R.drawable.filled_favorite_like, likesChange >= 0),
            AnalyticsItem("Comments", formatNumber(totalComments), formatPercentage(commentsChange), R.drawable.comments, commentsChange >= 0),
            AnalyticsItem("Shares", formatNumber(totalShares + totalReposts), formatPercentage(sharesChange), R.drawable.share_svgrepo_com, sharesChange >= 0),
            AnalyticsItem("Followers Gained", formatNumber(followersGained), formatPercentage(followersChange), R.drawable.ic_followers_gained, followersChange >= 0)
        )
    }

    private fun calculatePercentageChange(oldValue: Int, newValue: Int): Double {
        if (oldValue == 0) return if (newValue > 0) 100.0 else 0.0
        return ((newValue - oldValue).toDouble() / oldValue) * 100
    }

    private fun formatNumber(number: Int): String {
        return when {
            number >= 1_000_000 -> String.format("%.1fM", number / 1_000_000.0)
            number >= 1_000 -> String.format("%.1fK", number / 1_000.0)
            else -> number.toString()
        }
    }

    private fun formatPercentage(percentage: Double): String {
        val sign = if (percentage >= 0) "+" else ""
        return String.format("%s%.1f%%", sign, abs(percentage))
    }

    private fun setupCharts() {
        setupVideoViewsChart(binding.chartVideoViews)
        setupFollowersGrowthChart(binding.chartFollowersGrowth)
        setupAudienceGenderChart(binding.chartAudienceGender)
    }

    private fun setupVideoViewsChart(chart: BarChart) {
        val daysCount = when (selectedTimeRange) {
            "7 days" -> 7
            "28 days" -> 28
            "60 days" -> 60
            else -> 7
        }

        val entries = mutableListOf<BarEntry>()
        val videoPosts = allUserPosts.filter { post ->
            val hasDirectVideo = post.files.any { it.mimeType == "video" }
            val hasRepostVideo = post.isReposted == true &&
                    !post.originalPost.isNullOrEmpty() &&
                    post.originalPost[0].files.any { it.mimeType == "video" }
            hasDirectVideo || hasRepostVideo
        }

        if (videoPosts.isNotEmpty()) {
            val viewsPerDay = videoPosts.map { post ->
                val actualPost = if (post.isReposted == true && !post.originalPost.isNullOrEmpty()) {
                    post.originalPost[0]
                } else {
                    null
                }
                val likes = actualPost?.likeCount ?: post.likes ?: 0
                likes * 10
            }

            for (i in 1..daysCount) {
                val index = (i - 1) % viewsPerDay.size
                entries.add(BarEntry(i.toFloat(), viewsPerDay[index].toFloat()))
            }
        } else {
            for (i in 1..daysCount) {
                entries.add(BarEntry(i.toFloat(), 0f))
            }
        }

        val dataSet = BarDataSet(entries, "Video Views").apply {
            color = BLUEJEANS_COLOR
            valueTextSize = 10f
        }

        chart.data = BarData(dataSet)
        chart.description.isEnabled = false
        chart.axisRight.isEnabled = false
        chart.legend.isEnabled = false
        chart.setFitBars(true)
        chart.invalidate()
    }

    private fun setupFollowersGrowthChart(chart: LineChart) {
        val daysCount = when (selectedTimeRange) {
            "7 days" -> 7
            "28 days" -> 28
            "60 days" -> 60
            else -> 7
        }

        val entries = mutableListOf<Entry>()
        val postsPerDay = allUserPosts.size / daysCount.coerceAtLeast(1)

        var cumulative = 0
        for (i in 1..daysCount) {
            cumulative += (postsPerDay * 2)
            entries.add(Entry(i.toFloat(), cumulative.toFloat()))
        }

        val dataSet = LineDataSet(entries, "Followers Gained").apply {
            color = BLUEJEANS_COLOR
            setCircleColor(BLUEJEANS_COLOR)
            lineWidth = 2f
            valueTextSize = 10f
        }

        chart.data = LineData(dataSet)
        chart.description.isEnabled = false
        chart.axisRight.isEnabled = false
        chart.legend.isEnabled = false
        chart.invalidate()
    }

    private fun setupAudienceGenderChart(chart: PieChart) {
        val entries = listOf(
            PieEntry(60f, "Female"),
            PieEntry(40f, "Male")
        )

        val dataSet = PieDataSet(entries, "Gender Distribution").apply {
            colors = listOf(BLUEJEANS_COLOR, Color.parseColor("#A8D5F7"))
            valueTextSize = 10f
        }

        chart.data = PieData(dataSet)
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.setUsePercentValues(true)
        chart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        // Don't clear cache - it's static and should persist
    }
}

data class AnalyticsItem(
    val title: String,
    val value: String,
    val change: String = "",
    val iconResId: Int,
    val isPositiveChange: Boolean = false
)

class AnalyticsAdapter(private val analyticsList: List<AnalyticsItem>) :
    RecyclerView.Adapter<AnalyticsAdapter.ViewHolder>() {

    private val BLUEJEANS_COLOR = Color.parseColor("#2196F3")

    class ViewHolder(val binding: MyUserItemAnalyticsBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = MyUserItemAnalyticsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = analyticsList[position]
        with(holder.binding) {
            analyticsTitle.text = item.title
            analyticsValue.text = item.value
            analyticsChange.text = item.change
            analyticsChange.setTextColor(
                if (item.isPositiveChange) Color.parseColor("#00C853") else Color.parseColor("#FF0000")
            )

            analyticsIcon.setImageResource(item.iconResId)
            analyticsIcon.imageTintList = ColorStateList.valueOf(BLUEJEANS_COLOR)

            root.setOnClickListener {
                Toast.makeText(root.context, "Clicked ${item.title}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int = analyticsList.size
}