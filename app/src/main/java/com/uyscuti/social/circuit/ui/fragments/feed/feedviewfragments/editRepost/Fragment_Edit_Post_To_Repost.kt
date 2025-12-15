package com.uyscuti.social.circuit.ui.fragments.feed.feedviewfragments.editRepost

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Outline
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.pdf.PdfRenderer
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.MediaMetadataRetriever
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcel
import android.os.Parcelable
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.feedRepost.Tapped_Files_In_The_Container_View_Fragment
import com.uyscuti.social.circuit.MainActivity
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.FragmentEditPostToRepostBinding
import com.uyscuti.social.network.api.response.allFeedRepostsPost.Post
import me.relex.circleindicator.CircleIndicator3
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import com.bumptech.glide.request.target.CustomTarget
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.uyscuti.social.circuit.adapter.feed.FeedAdapter
import com.uyscuti.social.circuit.ui.shorts.UploadShortsActivity.Companion.LOCATION_PERMISSION_REQUEST_CODE
import com.uyscuti.social.circuit.ui.uploads.CameraActivity
import com.uyscuti.social.circuit.ui.uploads.feed_uploads.FeedAudioActivity
import com.uyscuti.social.circuit.ui.uploads.feed_uploads.FeedSelectVideoActivity
import com.uyscuti.social.network.api.response.allFeedRepostsPost.RepostRequest
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date
import javax.inject.Inject
import javax.net.ssl.SSLException


@AndroidEntryPoint
class Fragment_Edit_Post_To_Repost(private val data: Post) : Fragment() {


    @Inject
    lateinit var retrofitInterface: RetrofitInstance


    companion object {
        internal const val TAG = "Fragment_Edit_Post_To_Repost"
        private const val MAX_COMMENT_LENGTH = 280

        // View types for the adapter
        private const val VIEW_TYPE_IMAGE_FEED = 1
        private const val VIEW_TYPE_AUDIO_FEED = 2
        private const val VIEW_TYPE_VIDEO_FEED = 3
        private const val VIEW_TYPE_DOCUMENT_FEED = 4
        private const val VIEW_TYPE_COMBINATION_OF_MULTIPLE_FILES = 5

        fun newInstance(post: Post): Fragment_Edit_Post_To_Repost {
            return Fragment_Edit_Post_To_Repost(post)
        }
    }

    private var _binding: FragmentEditPostToRepostBinding? = null
    private val binding get() = _binding!!

    // Current post reference
    private var currentPost: Post? = null
    private var isReposting = false


    // View components from XML
    private lateinit var cancelButton: ImageView
    private lateinit var repostButton: TextView
    private lateinit var userProfile: ImageView
    private lateinit var hashtags: TextView
    private lateinit var shortVideoThumbNail: FrameLayout
    private lateinit var shortThumbNail: ImageView
    private lateinit var multipleImagesContainers: ConstraintLayout
    private lateinit var viewPagers: ViewPager2
    private lateinit var circleIndicator: CircleIndicator3
    private lateinit var originalPostProfileImage: ImageView
    private lateinit var originalPostUsername: TextView
    private lateinit var originalUserHandle: TextView
    private lateinit var repostedTimeIndicator: TextView
    private lateinit var originalFeedTextContent: TextView
    private lateinit var mixedFilesCardView: CardView
    private lateinit var recyclerView: RecyclerView
    private lateinit var originalHashtags: TextView
    private lateinit var likesCount: TextView
    private lateinit var commentCount: TextView
    private lateinit var bookmarkCount: TextView
    private lateinit var shareCount: TextView
    private lateinit var replyInput: EditText

    // Media adapter for displaying files
    private lateinit var mediaAdapter: OriginalPostMediaAdapter

    // Activity result launchers
    private lateinit var pickMultipleMedia: ActivityResultLauncher<PickVisualMediaRequest>

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var audioPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var videoPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var imagePickLauncher: ActivityResultLauncher<Intent>
    private lateinit var documentPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var attachmentFile: CardView

    // Additional media for repost
    private val additionalMediaUris = mutableListOf<Uri>()

    // Listener for multiple file clicks
    private var onMultipleFilesClickListener: OnMultipleFilesClickListener? = null

    private val selectedPeople = mutableSetOf<String>()
    private val selectedTopics = mutableSetOf<String>()
    private var selectedLocation: String? = null

    // Other properties
    private var allPosts: List<Post> = listOf()
    private var feedAdapter: FeedAdapter? = null
    private var feedRecyclerView: RecyclerView? = null
    private lateinit var tagPeopleLayout: View
    private lateinit var topicsLayout: View
    private lateinit var locationLayout: View




    interface OnMultipleFilesClickListener {
        fun multipleFileClickListener(
            position: Int,
            files: List<com.uyscuti.social.network.api.response.getrepostsPostsoriginal.File>,
            fileIds: List<String>
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActivityResultLaunchers()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditPostToRepostBinding.inflate(inflater, container, false)
        return binding.root
    }

    @OptIn(UnstableApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize all lateinit properties from binding
        cancelButton = binding.cancelButton
        repostButton = binding.repostButton
        userProfile = binding.userprofile
        hashtags = binding.hashtags
        shortVideoThumbNail = binding.shortVideoThumbNail
        shortThumbNail = binding.shortThumbNail
        multipleImagesContainers = binding.multipleImagesContainers
        viewPagers = binding.viewPagers
        circleIndicator = binding.circleIndicator
        originalPostProfileImage = binding.originalPostProfileImage
        originalPostUsername = binding.originalPostUsername
        originalUserHandle = binding.originalUserHandle
        repostedTimeIndicator = binding.repostedTimeIndicator
        originalFeedTextContent = binding.originalFeedTextContent
        mixedFilesCardView = binding.mixedFilesCardView
        recyclerView = binding.recyclerView
        originalHashtags = binding.originalHashtags
        likesCount = binding.likesCount
        commentCount = binding.commentCount
        bookmarkCount = binding.bookmarkCount
        shareCount = binding.shareCount
        replyInput = binding.replyInput
        tagPeopleLayout = binding.tagPeopleLayout
        topicsLayout = binding.topicsLayout
        locationLayout = binding.locationLayout

        // Initialize button state
        repostButton.isEnabled = true
        repostButton.alpha = 0.5f

        render(data)

        setupClickListeners()
        setupTextWatcher()

        hideSystemBars()
        setupSystemBarVisibilityListener()

        // Hide main activity UI elements
        (activity as? MainActivity)?.hideAppBar()
        (activity as? MainActivity)?.hideBottomNavigation()

        setupBackNavigation()
        initializeMediaHandling()

        binding.replyInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.replyInput.clearFocus()
                hideKeyboard()
                true
            } else {
                false
            }
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    private fun setupTextWatcher() {
        replyInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No action needed
            }

            override fun afterTextChanged(s: Editable?) {
                val hasText = s?.toString()?.trim()?.isNotEmpty() == true
                Log.d(TAG, "Text changed: hasText=$hasText, text='${s?.toString()?.trim()}'")
            }
        })
    }

    private fun showSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, true)
        WindowInsetsControllerCompat(requireActivity().window, requireActivity().window.decorView)
            .show(WindowInsetsCompat.Type.systemBars())
    }

    @OptIn(UnstableApi::class)
    private fun restoreSystemUIAndNavigateBack() {
        try {
            showSystemUI()
            (activity as? MainActivity)?.showAppBar()
            (activity as? MainActivity)?.showBottomNavigation()
            parentFragmentManager.popBackStack()
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring system UI or navigating back", e)
        }
    }

    private fun setupClickListeners() {


        binding.cancelButton.setOnClickListener {
            Log.d(TAG, "Cancel button clicked")
            restoreSystemUIAndNavigateBack()
        }

        binding.repostButton.setOnClickListener {
            Log.d(TAG, "Repost button clicked...")
            if (isReposting) {
                Log.d(TAG, "Repost already in progress, ignoring click")
                Toast.makeText(context, "Repost in progress, please wait", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val comment = ""
            performRepost(comment)
        }

        binding.tagPeopleLayout.setOnClickListener { showTagPeopleDialog() }
        binding.topicsLayout.setOnClickListener { showTopicsDialog() }
        binding.locationLayout.setOnClickListener { showLocationPicker() }
        binding.userprofile.setOnClickListener { handleUserProfileClick() }
        binding.addMoreFeed.setOnClickListener { showAttachmentDialog() }
        binding.originalPostContainer.setOnClickListener { handleOriginalPostClick() }
        binding.originalPostProfileImage.setOnClickListener { handleOriginalUserProfileClick() }
        binding.mixedFilesCardView.setOnClickListener { handleMediaClick() }
        binding.hashtags.setOnClickListener { handleHashtagsClick() }
        binding.originalHashtags.setOnClickListener { handleOriginalHashtagsClick() }
        binding.likeSection.setOnClickListener { handleLikeClick() }
        binding.commentSection.setOnClickListener { handleCommentClick() }
        binding.bookmarkSection.setOnClickListener { handleBookmarkClick() }
        binding.shareSection.setOnClickListener { handleShareClick() }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = (context?.getSystemService(
            Context.CONNECTIVITY_SERVICE) ?: return false) as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected == true
        }
    }


    @SuppressLint("SetTextI18n")
    private fun performRepost(comment: String, newFiles: List<File>? = null) {
        Log.d(TAG, "performRepost called, currentPost=${currentPost?._id}, comment=$comment, newFiles=${newFiles?.size}")

        // Check network connectivity first
        if (!isNetworkAvailable()) {
            Log.e(TAG, "No network connectivity detected")
            Toast.makeText(context, "No internet connection. Please check your network.", Toast.LENGTH_SHORT).show()
            return
        }

        currentPost?.let { post ->
            if (isReposting) {
                Log.d(TAG, "Repost already in progress, ignoring")
                return
            }

            isReposting = true
            Log.d(TAG, "Network available, proceeding with repost...")

            lifecycleScope.launch {
                try {
                    Log.d(TAG, "Initiating repost API call for postId: ${post._id}")

                    val repostRequest = RepostRequest(
                        isReposted = true,
                        comment = comment,
                        files = newFiles as List<com.uyscuti.social.network.api.response.getrepostsPostsoriginal.File>?,
                        tags = null
                    )

                    val response = retrofitInterface.apiService.repostsFeed(post._id, repostRequest)

                    Log.d(TAG, "Repost API response: code=${response.code()}, success=${response.isSuccessful}")

                    if (response.isSuccessful) {
                        val repostResponse = response.body()
                        if (repostResponse != null) {
                            post.isReposted = true

                            Log.d(TAG, "Reposting successful")
                            Toast.makeText(context, "Reposting successful!", Toast.LENGTH_SHORT).show()

                            Handler(Looper.getMainLooper()).postDelayed({
                                restoreSystemUIAndNavigateBack()
                            }, 500)
                        } else {
                            Log.d(TAG, "Repost failed: Response body is null")
                            Toast.makeText(context, "Failed to repost", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val errorMessage = when (response.code()) {
                            400 -> "Invalid request. Please check your input."
                            401 -> "Authentication required. Please log in again."
                            403 -> "You don't have permission to repost this content."
                            404 -> "The original post was not found."
                            409 -> "You have already reposted this content."
                            429 -> "Too many requests. Please try again later."
                            500 -> "Server error. Please try again later."
                            else -> "Failed to repost. Please try again."
                        }
                        Log.e(TAG, "Repost failed with code: ${response.code()}, message: ${response.message()}")
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                } catch (t: Throwable) {
                    Log.e(TAG, "Reposting exception - Exception type: ${t.javaClass.simpleName}")
                    Log.e(TAG, "Reposting exception - Message: ${t.message}")
                    Log.e(TAG, "Reposting exception - Stack trace:", t)

                    val errorMessage = when (t) {
                        is UnknownHostException -> {
                            Log.e(TAG, "UnknownHostException - DNS resolution failed or no internet")
                            "No internet connection or server unreachable. Please check your network."
                        }
                        is SocketTimeoutException -> {
                            Log.e(TAG, "SocketTimeoutException - Request timed out")
                            "Request timed out. Please try again."
                        }
                        is ConnectException -> {
                            Log.e(TAG, "ConnectException - Connection failed")
                            "Connection failed. Please check your internet connection."
                        }
                        is SSLException -> {
                            Log.e(TAG, "SSLException - SSL/TLS error")
                            "Security error. Please try again."
                        }
                        else -> {
                            Log.e(TAG, "Unknown network error: ${t.javaClass.simpleName}")
                            "Network error: ${t.message}"
                        }
                    }
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                } finally {
                    isReposting = false
                }
            }
        } ?: run {
            Log.d(TAG, "performRepost: No post selected")
            Toast.makeText(context, "No post selected", Toast.LENGTH_SHORT).show()
            isReposting = false
        }
    }


    private fun render(data: Post) {
        currentPost = data
        Log.d(TAG, "render: feed data $data")
        Log.d(TAG, "Post ID: ${data._id}")
        Log.d(TAG, "Post content: ${data.content}")
        Log.d(TAG, "Post author: ${data.author?.account?.username}")
        Log.d(TAG, "Post files count: ${data.files.size}")

        setupUserInfo(data, data.author?.account?.username ?: "")
        setupContentAndTags(data)
        setupMediaFiles(data)
        setupPostInfo(data)
        setupEngagementButtons(data)

        Log.d(TAG, "render: completed setup for post ${data._id}")
    }

    private fun showAttachmentDialog() {
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(R.layout.shorts_and_all_feed_file_upload_bottom_dialog)

        val video = dialog.findViewById<LinearLayout>(R.id.upload_video)
        val audio = dialog.findViewById<LinearLayout>(R.id.upload_audio)
        val image = dialog.findViewById<LinearLayout>(R.id.upload_image)
        val camera = dialog.findViewById<LinearLayout>(R.id.open_camera)
        val doc = dialog.findViewById<LinearLayout>(R.id.upload_document)
        val location = dialog.findViewById<LinearLayout>(R.id.share_location)
        val vnRecord = dialog.findViewById<LinearLayout>(R.id.vnRecord)

        val dialogView = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        dialogView?.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up))

        val selectableItemBackground = TypedValue()
        val theme = requireContext().theme

        theme.resolveAttribute(android.R.attr.selectableItemBackground, selectableItemBackground, true)

        image?.setBackgroundResource(selectableItemBackground.resourceId)
        video?.setBackgroundResource(selectableItemBackground.resourceId)
        audio?.setBackgroundResource(selectableItemBackground.resourceId)
        camera?.setBackgroundResource(selectableItemBackground.resourceId)
        doc?.setBackgroundResource(selectableItemBackground.resourceId)
        location?.setBackgroundResource(selectableItemBackground.resourceId)
        vnRecord?.setBackgroundResource(selectableItemBackground.resourceId)

        image?.setOnClickListener {
            Log.d("SelectImage", "Image selector button clicked")
            pickMultipleMedia.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
            dialog.dismiss()
        }

        video?.setOnClickListener {
            val intent = Intent(requireContext(), FeedSelectVideoActivity::class.java)
            videoPickerLauncher.launch(intent)
            dialog.dismiss()
        }

        audio?.setOnClickListener {
            val intent = Intent(requireContext(), FeedAudioActivity::class.java)
            audioPickerLauncher.launch(intent)
            dialog.dismiss()
        }

        doc?.setOnClickListener {
            openDocFilePicker("application/*")
            dialog.dismiss()
        }

        camera?.setOnClickListener {
            val intent = Intent(requireContext(), CameraActivity::class.java)
            cameraLauncher.launch(intent)
            dialog.dismiss()
        }

        location?.visibility = View.GONE
        vnRecord?.visibility = View.INVISIBLE
        vnRecord?.setOnClickListener {
            dialog.dismiss()
        }
        location?.setOnClickListener { }
        dialog.show()
    }

    private fun setupActivityResultLaunchers() {
        pickMultipleMedia = registerForActivityResult(
            ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
        ) { uris ->
            if (uris.isNotEmpty()) {
                handleSelectedImages(uris)
            }
        }

        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                handleCameraResult(result)
            }
        }

        imagePickLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                handleImagePickResult(result)
            }
        }

        audioPickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                handleAudioPickResult(result)
            }
        }

        videoPickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                handleVideoPickResult(result)
            }
        }

        documentPickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                handleDocumentPickResult(result)
            }
        }
    }

    private fun handleSelectedImages(uris: List<Uri>) {
        for (uri in uris) {
            additionalMediaUris.add(uri)
        }
        updateMediaPreview()
    }

    private fun handleCameraResult(result: ActivityResult) {
        val imagePath = result.data?.getStringExtra("image_url")
        if (imagePath != null) {
            val uri = Uri.fromFile(File(imagePath))
            additionalMediaUris.add(uri)
            updateMediaPreview()
        }
    }

    private fun handleImagePickResult(result: ActivityResult) {
        result.data?.data?.let { uri ->
            additionalMediaUris.add(uri)
            updateMediaPreview()
        }
    }

    private fun handleAudioPickResult(result: ActivityResult) {
        val audioPath = result.data?.getStringArrayListExtra("audio_url")
        audioPath?.forEach { path ->
            val uri = Uri.fromFile(File(path))
            additionalMediaUris.add(uri)
        }
        updateMediaPreview()
    }

    private fun handleVideoPickResult(result: ActivityResult) {
        val videoPaths = result.data?.getStringArrayListExtra("video_url")
        videoPaths?.forEach { path ->
            val uri = Uri.fromFile(File(path))
            additionalMediaUris.add(uri)
        }
        updateMediaPreview()
    }

    private fun handleDocumentPickResult(result: ActivityResult) {
        result.data?.data?.let { uri ->
            additionalMediaUris.add(uri)
            updateMediaPreview()
        }
    }

    private fun openDocFilePicker(mimeType: String) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = mimeType
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        documentPickerLauncher.launch(intent)
    }

    private fun showTopicsDialog() {
        val topicsList = arrayOf(
            "Dance", "Comedy", "Music", "Sports", "Fashion", "Food", "Travel",
            "Gaming", "Education", "DIY", "Beauty", "Fitness", "Pets", "Art",
            "Technology", "News", "Lifestyle", "Entertainment"
        )

        val selectedItems = BooleanArray(topicsList.size)

        AlertDialog.Builder(requireContext())
            .setTitle("Select Topics")
            .setMultiChoiceItems(
                topicsList,
                selectedItems) { _, which, isChecked ->
                if (isChecked) {
                    selectedTopics.add(topicsList[which])
                } else {
                    selectedTopics.remove(topicsList[which])
                }
            }
            .setPositiveButton("Done") { dialog, _ ->
                updateTopicsUI()
                applyTopicsFilter()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showTagPeopleDialog() {
        val peopleList = arrayOf(
            "Friends", "Family", "Colleagues", "Followers", "Mutual Friends",
            "Close Friends", "Acquaintances", "Celebrities", "Influencers"
        )

        val selectedItems = BooleanArray(peopleList.size)

        // Pre-select already selected items
        peopleList.forEachIndexed { index, person ->
            selectedItems[index] = selectedPeople.contains(person)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Tag People")
            .setMultiChoiceItems(
                peopleList,
                selectedItems) { _, which, isChecked ->
                if (isChecked) {
                    selectedPeople.add(peopleList[which])
                } else {
                    selectedPeople.remove(peopleList[which])
                }
            }
            .setPositiveButton("Done") { dialog, _ ->
                updateTagPeopleUI()
                applyPeopleFilter()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showLocationPicker() {
        val locations = arrayOf(
            "Current Location", "Nearby", "City Center", "Popular Places",
            "Restaurants", "Parks", "Shopping Centers", "Entertainment Venues",
            "Schools", "Gyms", "Cafes", "Custom Location"
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Add Location")
            .setItems(locations) { dialog, which ->
                when (which) {
                    0 -> getCurrentLocation()
                    1 -> getNearbyLocations()
                    locations.size - 1 -> showCustomLocationDialog()
                    else -> {
                        selectedLocation = locations[which]
                        updateLocationUI()
                        applyLocationFilter()
                    }
                }
                dialog.dismiss()
            }
            .show()
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        // Check location permission
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        try {
            val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

            // Check if GPS is enabled
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(
                    requireContext(), "Please enable GPS",
                    Toast.LENGTH_SHORT).show()
                return
            }

            val locationListener = object : LocationListener {
                @SuppressLint("DefaultLocale")
                override fun onLocationChanged(location: Location) {
                    selectedLocation = "Current Location (${String.format(
                        "%.4f",
                        location.latitude)}, " +
                            "${String.format("%.4f",
                                location.longitude)})"
                    updateLocationUI()
                    applyLocationFilter()
                    locationManager.removeUpdates(this)
                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {
                    Toast.makeText(requireContext(),
                        "GPS disabled", Toast.LENGTH_SHORT).show()
                }
            }

            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0, 0f, locationListener)

            // Add timeout to prevent indefinite waiting
            Handler(Looper.getMainLooper()).postDelayed({
                locationManager.removeUpdates(locationListener)
            }, 10000) // 10 seconds timeout

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Unable to get location",
                Toast.LENGTH_SHORT).show()
            Log.e("LocationError", "Error getting location", e)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation()
                } else {
                    Toast.makeText(requireContext(), "Location permission required",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showCustomLocationDialog() {
        val input = EditText(requireContext())
        input.hint = "Enter location name"

        AlertDialog.Builder(requireContext())
            .setTitle("Custom Location")
            .setView(input)
            .setPositiveButton("Add") { dialog, _ ->
                val customLocation = input.text.toString().trim()
                if (customLocation.isNotEmpty()) {
                    selectedLocation = customLocation
                    updateLocationUI()
                    applyLocationFilter()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun getNearbyLocations() {
        // Simulate getting nearby locations
        val nearbyPlaces = arrayOf(
            "Coffee Shop - 0.2 km", "Restaurant - 0.5 km", "Park - 0.8 km",
            "Mall - 1.2 km", "Gym - 1.5 km", "Cinema - 2.0 km"
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Nearby Locations")
            .setItems(nearbyPlaces) { dialog, which ->
                selectedLocation = nearbyPlaces[which]
                updateLocationUI()
                applyLocationFilter()
                dialog.dismiss()
            }
            .show()
    }

    @SuppressLint("SetTextI18n")
    private fun updateTagPeopleUI() {
        val textView = tagPeopleLayout.findViewById<TextView>(R.id.tagPeopleText)
        if (selectedPeople.isNotEmpty()) {
            textView.text = "Tagged (${selectedPeople.size})"
            textView.setTextColor(ContextCompat.getColor(requireContext(),
                R.color.colorPrimary))
        } else {
            textView.text = "Tag people"
            textView.setTextColor(ContextCompat.getColor(requireContext(),
                android.R.color.black))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateTopicsUI() {
        val textView = topicsLayout.findViewById<TextView>(R.id.topicsText)
        if (selectedTopics.isNotEmpty()) {
            textView.text = "Topics (${selectedTopics.size})"
            textView.setTextColor(ContextCompat.getColor(requireContext(),
                R.color.colorPrimary))
        } else {
            textView.text = "Add topics"
            textView.setTextColor(ContextCompat.getColor(requireContext(),
                android.R.color.black))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateLocationUI() {
        val textView = locationLayout.findViewById<TextView>(R.id.locationText)
        if (selectedLocation != null) {
            textView.text = "Location ✓"
            textView.setTextColor(ContextCompat.getColor(requireContext(),
                R.color.colorPrimary))
        } else {
            textView.text = "Add location"
            textView.setTextColor(ContextCompat.getColor(requireContext(),
                android.R.color.black))
        }
    }

    private fun applyPeopleFilter() {
        val filteredPosts = filterPostsByPeople(selectedPeople.toList())
        updateFeedWithFilteredPosts(filteredPosts)

        // Analytics tracking
        trackFilterUsage("people", selectedPeople.size)
    }

    private fun applyTopicsFilter() {
        // Apply topics filter to feed
        val filteredPosts = filterPostsByTopics(selectedTopics.toList())
        updateFeedWithFilteredPosts(filteredPosts)

        // Show topics chips
        showTopicsChips()

        // Analytics tracking
        trackFilterUsage("topics", selectedTopics.size)
    }

    private fun applyLocationFilter() {
        // Apply location filter to feed
        selectedLocation?.let { location ->
            val filteredPosts = filterPostsByLocation(location)
            updateFeedWithFilteredPosts(filteredPosts)

            // Show location badge
            showLocationBadge(location)

            // Analytics tracking
            trackFilterUsage("location", 1)
        }
    }

    private fun filterPostsByPeople(people: List<String>): List<Post> {
        // Filter posts based on selected people categories
        return allPosts.filter { post ->
            people.any { person ->
                when (person) {
                    "Friends" -> post.isFromFriend ?: false
                    "Family" -> post.isFromFamily ?: false
                    "Followers" -> post.isFromFollower ?: false
                    "Mutual Friends" -> post.isFromMutualFriend ?: false
                    "Close Friends" -> post.isFromCloseFriend ?: false
                    "Celebrities" -> post.isFromCelebrity ?: false
                    "Influencers" -> post.isFromInfluencer ?: false
                    else -> true
                }
            }
        }
    }

    private fun filterPostsByTopics(topics: List<String>): List<Post> {
        // Filter posts based on selected topics
        return allPosts.filter { post ->
            topics.any { topic ->
                post.tags?.contains(topic.lowercase()) == true ||
                        post.description?.contains(topic, ignoreCase = true) == true
            }
        }
    }

    private fun filterPostsByLocation(location: String): List<Post> {
        // Filter posts based on location
        return allPosts.filter { post ->
            post.location?.contains(location, ignoreCase = true) == true ||
                    isNearLocation(post.latitude, post.longitude, location)
        }
    }

    private fun isNearLocation(lat: Double?, lon: Double?, targetLocation: String): Boolean {
        // Implement proximity check logic
        if (lat == null || lon == null) return false

        // Calculate distance and return true if within certain radius
        // This would typically use Google Maps or similar service
        return true // Simplified for example
    }

    private fun updateFeedWithFilteredPosts(posts: List<Post>) {
        // Update RecyclerView with filtered posts
        feedAdapter?.updatePosts(posts)

        // Show filter indicator
        showActiveFiltersIndicator()

        // Smooth scroll to top
        feedRecyclerView?.smoothScrollToPosition(0)
    }

    private fun showTopicsChips() {
        // Show selected topics as chips at top of feed
        val chipsContainer = view?.findViewById<LinearLayout>(R.id.topicsChipsContainer)
        chipsContainer?.removeAllViews()

        selectedTopics.forEach { topic ->
            val chip = createTopicChip(topic)
            chipsContainer?.addView(chip)
        }

        chipsContainer?.visibility = if (selectedTopics.isNotEmpty()) View.VISIBLE else View.GONE
    }

    @SuppressLint("InflateParams")
    private fun createTopicChip(topic: String): View {
        val chip = layoutInflater.inflate(R.layout.topic_chip, null)
        val textView = chip.findViewById<TextView>(R.id.chipText)
        val closeButton = chip.findViewById<ImageView>(R.id.chipClose)

        textView.text = topic
        closeButton.setOnClickListener {
            selectedTopics.remove(topic)
            showTopicsChips()
            applyTopicsFilter()
        }

        return chip
    }

    @SuppressLint("SetTextI18n")
    private fun showLocationBadge(location: String) {
        val locationBadge = view?.findViewById<TextView>(R.id.locationBadge)
        locationBadge?.text = "📍 $location"
        locationBadge?.visibility = View.VISIBLE

        locationBadge?.setOnClickListener {
            selectedLocation = null
            updateLocationUI()
            locationBadge.visibility = View.GONE
            applyLocationFilter()
        }
    }

    private fun showActiveFiltersIndicator() {
        val filtersCount = selectedPeople.size + selectedTopics.size +
                (if (selectedLocation != null) 1 else 0)

        val filterIndicator = view?.findViewById<TextView>(R.id.filterIndicator)
        if (filtersCount > 0) {
            filterIndicator?.text = "Active Filters: $filtersCount"
            filterIndicator?.visibility = View.VISIBLE
        } else {
            filterIndicator?.visibility = View.GONE
        }
    }

    private fun trackFilterUsage(filterType: String, count: Int) {
        // Analytics tracking
        val params = Bundle().apply {
            putString("filter_type", filterType)
            putInt("filter_count", count)
        }
        // FirebaseAnalytics.getInstance(this).logEvent("filter_applied", params)
    }

    @OptIn(UnstableApi::class)
    private fun setupBackNavigation() {
        var isNavigating = false

        cancelButton.setOnClickListener {
            if (isNavigating) return@setOnClickListener
            isNavigating = true

            try {
                cleanupResources()
                restoreSystemBars()

                (activity as? MainActivity)?.showAppBar()
                (activity as? MainActivity)?.showBottomNavigation()

                hideKeyboard()

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
                    if (isAdded) {
                        isNavigating = false
                    }
                }, 100)
            }
        }
    }

    private fun hideSystemBars() {
        if (!isAdded) return

        try {
            val window = requireActivity().window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
            windowInsetsController?.let { controller ->
                controller.hide(WindowInsetsCompat.Type.statusBars())
                controller.hide(WindowInsetsCompat.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } catch (e: Exception) {
            Log.e(TAG, "Error hiding system bars", e)
        }
    }

    private fun cleanupResources() {
        try {
            if (::mediaAdapter.isInitialized) {
                // Clean up adapter resources if needed
            }

            if (isAdded) {
                val decorView = requireActivity().window.decorView
                decorView.setOnSystemUiVisibilityChangeListener(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up resources", e)
        }
    }

    private fun restoreSystemBars() {
        if (!isAdded) return

        try {
            val window = requireActivity().window
            WindowCompat.setDecorFitsSystemWindows(window, true)
            val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
            windowInsetsController?.let { controller ->
                controller.show(WindowInsetsCompat.Type.statusBars())
                controller.show(WindowInsetsCompat.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
            }
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            window.decorView.requestApplyInsets()
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring system bars", e)
        }
    }

    private fun setupSystemBarVisibilityListener() {
        if (!isAdded) return

        try {
            val decorView = requireActivity().window.decorView
            decorView.setOnSystemUiVisibilityChangeListener { visibility ->
                if (isAdded && visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                    hideSystemBars()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up system bar visibility listener", e)
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
    }

    override fun onDestroyView() {
        try {
            if (isAdded) {
                val decorView = requireActivity().window.decorView
                decorView.setOnSystemUiVisibilityChangeListener(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up in onDestroyView", e)
        }
        super.onDestroyView()
        _binding = null  // ADD this line
    }

    private fun handleLikeClick() {
        Toast.makeText(context, "Like clicked", Toast.LENGTH_SHORT).show()
    }

    private fun handleCommentClick() {
        Toast.makeText(context, "Comment clicked", Toast.LENGTH_SHORT).show()
    }

    private fun handleBookmarkClick() {
        Toast.makeText(context, "Bookmark clicked", Toast.LENGTH_SHORT).show()
    }

    private fun handleShareClick() {
        Toast.makeText(context, "Share clicked", Toast.LENGTH_SHORT).show()
    }

    private fun setupPostInfo(data: Post) {
        val mongoDateString = data.createdAt
        repostedTimeIndicator.text = formattedMongoDateTime(mongoDateString)
        updateEngagementCounts(data)
    }

    private fun setupEngagementButtons(data: Post) {
        updateEngagementCounts(data)
    }

    private fun updateEngagementCounts(data: Post) {
        likesCount.text = data.safeLikes.toString()
        commentCount.text = data.safeCommentCount.toString()
        bookmarkCount.text = data.safeBookmarkCount.toString()
        shareCount.text = data.safeShareCount.toString()
    }

    private fun formattedMongoDateTime(mongoDateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(mongoDateString)

            val now = Date()
            val diff = now.time - (date?.time ?: 0)

            when {
                diff < 60000 -> "Just now"
                diff < 3600000 -> "${diff / 60000}m ago"
                diff < 86400000 -> "${diff / 3600000}h ago"
                else -> "${diff / 86400000}d ago"
            }
        } catch (e: Exception) {
            "Recently"
        }
    }

    private fun handleUserProfileClick() {
        Toast.makeText(context, "Navigate to your profile", Toast.LENGTH_SHORT).show()
    }

    private fun handleOriginalPostClick() {
        Toast.makeText(context, "View original post details", Toast.LENGTH_SHORT).show()
    }

    private fun handleOriginalUserProfileClick() {
        val username = data.author.account?.username ?: "Unknown User"
        Toast.makeText(context, "View $username's profile", Toast.LENGTH_SHORT).show()
    }

    private fun handleMediaClick() {
        Toast.makeText(context, "Open media viewer", Toast.LENGTH_SHORT).show()
    }

    private fun handleHashtagsClick() {
        Toast.makeText(context, "Edit hashtags", Toast.LENGTH_SHORT).show()
    }

    private fun handleOriginalHashtagsClick() {
        Toast.makeText(context, "View hashtag feed", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("SetTextI18n")
    private fun setupContentAndTags(data: Post) {
        if (data.content.isNotEmpty()) {
            originalFeedTextContent.visibility = View.VISIBLE
            originalFeedTextContent.text = data.content
            Log.d(TAG, "Original post content: ${data.content}")
        } else {
            originalFeedTextContent.visibility = View.GONE
            Log.d(TAG, "No original post content found")
        }

        if (data.tags.isNotEmpty()) {
            val hashtagsText = data.tags.joinToString(" ") { "#$it" }
            originalHashtags.text = hashtagsText
            originalHashtags.visibility = View.VISIBLE
            Log.d(TAG, "Original hashtags: $hashtagsText")
        } else {
            originalHashtags.text = "#GreatApp #MustTry #Appreciation"
            originalHashtags.visibility = View.VISIBLE
        }

        setupRepostCommentArea()
    }

    @SuppressLint("SetTextI18n")
    private fun setupRepostCommentArea() {
        replyInput.hint = "Add your thoughts about this post..."
        hashtags.text = "#Repost #ProudMoment #Innovation"
        hashtags.visibility = View.VISIBLE


    }

    @SuppressLint("SetTextI18n")
    private fun setupUserInfo(data: Post, feedOwnerId: String) {
        val username = data.author.account.username
        val handle = data.author.account.username

        originalPostUsername.text = username
        originalUserHandle.text = "@$handle"

        Log.d(TAG, "Original post by: $username (@$handle)")

        val profileImageUrl = data.author.account.avatar.url
        if (profileImageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(profileImageUrl)
                .placeholder(R.drawable.circle_background)
                .error(R.drawable.circle_background)
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(originalPostProfileImage)
            Log.d(TAG, "Loading profile image: $profileImageUrl")
        } else {
            originalPostProfileImage.setImageResource(R.drawable.circle_background)
        }

        setupCurrentUserProfile()
    }

    private fun setupCurrentUserProfile() {
        userProfile.setImageResource(R.drawable.circle_background)
    }

    private fun setupMediaFiles(data: Post) {
        Log.d(TAG, "Setting up media files. Count: ${data.files.size}")

        if (data.files.isNotEmpty()) {
            mixedFilesCardView.visibility = View.VISIBLE
            setupMediaRecyclerView(data)
        } else {
            Log.d(TAG, "No media files found")
            mixedFilesCardView.visibility = View.GONE
        }
    }

    private fun setupMediaRecyclerView(data: Post) {
        Log.d(TAG, "Setting up media RecyclerView with ${data.files.size} files")

        recyclerView.layoutManager = GridLayoutManager(context, 2).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (data.files.size) {
                        1 -> 2 // Single item takes full width
                        else -> 1 // All other items take 1 span (half width)
                    }
                }
            }
        }

        mediaAdapter = OriginalPostMediaAdapter(data, recyclerView)
        recyclerView.adapter = mediaAdapter

        recyclerView.itemAnimator = null

        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                // Minimal spacing
            }
        })
    }


    @OptIn(UnstableApi::class)
    override fun onDestroy() {
        try {
            cleanupResources()
            restoreSystemBars()

            (activity as? MainActivity)?.showAppBar()
            (activity as? MainActivity)?.showBottomNavigation()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroy", e)
        }
        super.onDestroy()
    }

    private fun Int.dpToPx(context: Context?): Int {
        return if (context == null) {
            (this * Resources.getSystem().displayMetrics.density).toInt()
        } else {
            (this * context.resources.displayMetrics.density).toInt()
        }
    }

    private fun String.toColorInt(): Int {
        return try {
            Color.parseColor(this)
        } catch (e: Exception) {
            Color.BLACK
        }
    }

    private fun initializeMediaHandling() {
        setupActivityResultLaunchers()

        // Initialize empty state
        updateMediaPreview()
    }

    private fun updateMediaPreview() {
        if (additionalMediaUris.isNotEmpty()) {
            // Show the container
            shortVideoThumbNail.visibility = View.VISIBLE
            multipleImagesContainers.visibility = View.VISIBLE

            // Setup ViewPager2 with adapter and onMediaRemoved callback
            val adapter = MediaViewPagerAdapter(additionalMediaUris) { position, newSize ->
                // Update the indicator
                if (newSize > 0) {
                    circleIndicator.setViewPager(viewPagers)
                    if (newSize <= 1) {
                        circleIndicator.visibility = View.GONE
                    }
                } else {
                    // Hide everything when no media
                    shortVideoThumbNail.visibility = View.GONE
                    multipleImagesContainers.visibility = View.GONE
                    circleIndicator.visibility = View.GONE
                }
            }
            viewPagers.adapter = adapter

            // Setup circle indicator
            circleIndicator.setViewPager(viewPagers)

            // Show/hide indicator based on item count
            if (additionalMediaUris.size > 1) {
                circleIndicator.visibility = View.VISIBLE
            } else {
                circleIndicator.visibility = View.GONE
            }

            // Hide the single thumbnail view since we're using ViewPager
            shortThumbNail.visibility = View.GONE
        } else {
            // Hide everything when no media
            shortVideoThumbNail.visibility = View.GONE
            multipleImagesContainers.visibility = View.GONE
            circleIndicator.visibility = View.GONE
        }
    }

    inner class MediaViewPagerAdapter(

        private val mediaUris: MutableList<Uri>,
        private val onMediaRemoved: (Int, Int) -> Unit
    ) : RecyclerView.Adapter<MediaViewPagerAdapter.MediaViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_media_display, parent, false
            )
            return MediaViewHolder(view)
        }

        override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
            holder.bind(mediaUris[position])
        }

        override fun getItemCount(): Int = mediaUris.size

        inner class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val mediaImageView: ImageView = itemView.findViewById(R.id.mediaImageView)
            private val playButton: ImageView = itemView.findViewById(R.id.playButton)
            private val fileTypeIcon: ImageView = itemView.findViewById(R.id.fileTypeIcon)
            private val removeButton: ImageView = itemView.findViewById(R.id.removeButton)
            private val videoInfoContainer: LinearLayout = itemView.findViewById(R.id.videoInfoContainer)
            private val videoDuration: TextView = itemView.findViewById(R.id.videoDuration)

            fun bind(uri: Uri) {
                playButton.background = null // Remove background to eliminate rounded circle
                var mimeType = itemView.context.contentResolver.getType(uri)
                val fileName = getFileName(uri) ?: "Unknown"

                Log.d("MediaAdapter", "Processing URI: $uri")
                Log.d("MediaAdapter", "Initial MIME type: $mimeType, FileName: $fileName")

                if (mimeType.isNullOrEmpty() || mimeType == "application/octet-stream") {
                    Log.d("MediaAdapter", "Using fallback detection for file: $fileName")
                    mimeType = when {
                        isVideoFile(fileName) -> "video/mp4"
                        isAudioFile(fileName) -> "audio/mpeg"
                        fileName.matches(Regex(".*\\.pdf$", RegexOption.IGNORE_CASE)) -> "application/pdf"
                        fileName.matches(Regex(".*\\.(doc|docx)$", RegexOption.IGNORE_CASE)) -> "application/msword"
                        fileName.matches(Regex(".*\\.(xls|xlsx)$", RegexOption.IGNORE_CASE)) -> "application/vnd.ms-excel"
                        fileName.matches(Regex(".*\\.(ppt|pptx)$", RegexOption.IGNORE_CASE)) -> "application/vnd.ms-powerpoint"
                        fileName.matches(Regex(".*\\.txt$", RegexOption.IGNORE_CASE)) -> "text/plain"
                        fileName.matches(Regex(".*\\.(jpg|jpeg|png|gif|bmp|webp|tiff|svg)$", RegexOption.IGNORE_CASE)) -> "image/jpeg"
                        else -> "application/octet-stream"
                    }
                    Log.d("MediaAdapter", "Fallback MIME type: $mimeType")
                }

                when {
                    mimeType.startsWith("image/") -> {
                        Log.d("MediaAdapter", "Processing as IMAGE")
                        handleImageFile(uri)
                    }
                    mimeType.startsWith("video/") || isVideoFile(fileName) -> {
                        Log.d("MediaAdapter", "Processing as VIDEO")
                        handleVideoFile(uri, fileName)
                    }
                    mimeType.startsWith("audio/") || isAudioFile(fileName) -> {
                        Log.d("MediaAdapter", "Processing as AUDIO")
                        handleAudioFile(uri, fileName)
                    }
                    else -> {
                        Log.d("MediaAdapter", "Processing as DOCUMENT with type: $mimeType")
                        handleDocumentFile(uri, mimeType)
                    }
                }

                removeButton.setOnClickListener {
                    removeMediaFile(adapterPosition)
                }
            }

            private fun handleImageFile(uri: Uri) {
                playButton.visibility = View.GONE
                fileTypeIcon.visibility = View.GONE
                videoInfoContainer.visibility = View.GONE

                mediaImageView.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                mediaImageView.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                mediaImageView.adjustViewBounds = false
                mediaImageView.scaleType = ImageView.ScaleType.CENTER_CROP

                Glide.with(itemView.context)
                    .load(uri)
                    .centerCrop()
                    .placeholder(R.drawable.imageplaceholder)
                    .error(R.drawable.imageplaceholder)
                    .into(mediaImageView)
            }

            private fun handleAudioFile(uri: Uri, fileName: String) {
                playButton.visibility = View.VISIBLE
                fileTypeIcon.visibility = View.GONE
                videoInfoContainer.visibility = View.GONE
                videoDuration.visibility = View.GONE

                loadAudioThumbnail(uri, fileName)
            }

            private fun loadAudioThumbnail(uri: Uri, fileName: String) {
                mediaImageView.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                mediaImageView.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                mediaImageView.adjustViewBounds = false

                try {
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(itemView.context, uri)
                    val genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
                    val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
                    retriever.release()

                    when {
                        genre?.contains("voice", true) == true || duration != null && duration < 60000 -> {
                            mediaImageView.setBackgroundColor(Color.parseColor("#616161"))
                            mediaImageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
                            val drawable = ContextCompat.getDrawable(itemView.context,
                                R.drawable.ic_audio_white_large_icon)?.mutate()
                            drawable?.let {
                                it.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                                mediaImageView.setImageDrawable(it)
                            }
                        }
                        else -> {
                            mediaImageView.setBackgroundColor(Color.WHITE)
                            mediaImageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
                            val drawable = ContextCompat.getDrawable(itemView.context, R.drawable.music_icon)?.mutate()
                            drawable?.let {
                                mediaImageView.setImageDrawable(drawable)
                            }
                            mediaImageView.clearColorFilter()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("MediaAdapter", "Failed to load audio thumbnail for file: $fileName", e)
                    mediaImageView.setBackgroundColor(Color.WHITE)
                    mediaImageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
                    val drawable = ContextCompat.getDrawable(itemView.context, R.drawable.music_icon)?.mutate()
                    drawable?.let {
                        mediaImageView.setImageDrawable(drawable)
                    }
                    mediaImageView.clearColorFilter()
                }
            }

            private fun handleVideoFile(uri: Uri, fileName: String) {
                playButton.visibility = View.VISIBLE
                fileTypeIcon.visibility = View.GONE
                videoInfoContainer.visibility = View.GONE

                loadVideoThumbnail(uri)
            }

            private fun isVideoFile(fileName: String): Boolean {
                return fileName.endsWith(".mp4", true) ||
                        fileName.endsWith(".m4v", true) ||
                        fileName.endsWith(".mov", true) ||
                        fileName.endsWith(".avi", true) ||
                        fileName.endsWith(".mkv", true) ||
                        fileName.endsWith(".wmv", true) ||
                        fileName.endsWith(".flv", true) ||
                        fileName.endsWith(".webm", true) ||
                        fileName.endsWith(".3gp", true) ||
                        fileName.endsWith(".wva", true)
            }

            private fun isAudioFile(fileName: String): Boolean {
                return fileName.endsWith(".mp3", true) ||
                        fileName.endsWith(".m4a", true) ||
                        fileName.endsWith(".ogg", true) ||
                        fileName.endsWith(".aac", true) ||
                        fileName.endsWith(".wav", true) ||
                        fileName.endsWith(".flac", true) ||
                        fileName.endsWith(".amr", true) ||
                        fileName.endsWith(".3gp", true) ||
                        fileName.endsWith(".opus", true)
            }

            private fun loadVideoThumbnail(uri: Uri) {
                mediaImageView.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                mediaImageView.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                mediaImageView.adjustViewBounds = false
                mediaImageView.scaleType = ImageView.ScaleType.CENTER_CROP

                mediaImageView.scaleX = 1f
                mediaImageView.scaleY = 1f

                try {
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(itemView.context, uri)
                    val bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    retriever.release()

                    if (bitmap != null) {
                        Glide.with(itemView.context)
                            .load(bitmap)
                            .centerCrop()
                            .placeholder(R.drawable.videoplaceholder)
                            .error(R.drawable.videoplaceholder)
                            .into(mediaImageView)
                    } else {
                        mediaImageView.setImageResource(R.drawable.videoplaceholder)
                        mediaImageView.scaleType = ImageView.ScaleType.CENTER_CROP
                    }
                } catch (e: Exception) {
                    Log.e("MediaAdapter", "Failed to load video thumbnail for URI: $uri", e)
                    mediaImageView.setImageResource(R.drawable.videoplaceholder)
                    mediaImageView.scaleType = ImageView.ScaleType.CENTER_CROP
                }
            }

            private fun handleDocumentFile(uri: Uri, mimeType: String?) {
                playButton.visibility = View.GONE
                fileTypeIcon.visibility = View.VISIBLE
                videoInfoContainer.visibility = View.GONE

                val fileName = getFileName(uri) ?: "Unknown"

                mediaImageView.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                mediaImageView.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                mediaImageView.adjustViewBounds = false
                mediaImageView.scaleType = ImageView.ScaleType.CENTER_CROP

                when {
                    mimeType?.contains("pdf") == true -> {
                        fileTypeIcon.setImageResource(R.drawable.pdf_icon)
                        loadPdfThumbnail(uri)
                    }
                    mimeType?.contains("document") == true || mimeType?.contains("word") == true -> {
                        fileTypeIcon.setImageResource(R.drawable.word_icon)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            loadDocumentThumbnail(uri, R.drawable.word_icon)
                        } else {
                            mediaImageView.setImageResource(R.drawable.word_icon)
                        }
                    }
                    mimeType?.contains("spreadsheet") == true || mimeType?.contains("excel") == true -> {
                        fileTypeIcon.setImageResource(R.drawable.excel_icon)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            loadDocumentThumbnail(uri, R.drawable.excel_icon)
                        } else {
                            mediaImageView.setImageResource(R.drawable.excel_icon)
                        }
                    }
                    mimeType?.contains("powerpoint") == true || mimeType?.contains("presentation") == true -> {
                        fileTypeIcon.setImageResource(R.drawable.powerpoint_icon)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            loadDocumentThumbnail(uri, R.drawable.powerpoint_icon)
                        } else {
                            mediaImageView.setImageResource(R.drawable.powerpoint_icon)
                        }
                    }
                    mimeType?.contains("text") == true -> {
                        fileTypeIcon.setImageResource(R.drawable.text_icon)
                        mediaImageView.setImageResource(R.drawable.text_icon)
                    }
                    else -> {
                        Log.d("MediaAdapter", "Document fallback for file: $fileName, mimeType: $mimeType")
                        fileTypeIcon.setImageResource(R.drawable.flash21)
                        mediaImageView.setImageResource(R.drawable.flash21)
                    }
                }
            }

            @SuppressLint("UseKtx")
            private fun loadPdfThumbnail(uri: Uri) {
                try {
                    val contentResolver = itemView.context.contentResolver
                    val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")

                    parcelFileDescriptor?.let { pfd ->
                        val pdfRenderer = PdfRenderer(pfd)
                        if (pdfRenderer.pageCount > 0) {
                            val page = pdfRenderer.openPage(0)
                            val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                            Glide.with(itemView.context)
                                .load(bitmap)
                                .centerCrop()
                                .placeholder(R.drawable.pdf_icon)
                                .error(R.drawable.pdf_icon)
                                .into(mediaImageView)

                            page.close()
                        } else {
                            mediaImageView.setImageResource(R.drawable.pdf_icon)
                        }
                        pdfRenderer.close()
                        pfd.close()
                    }
                } catch (e: Exception) {
                    Log.e("MediaAdapter", "Failed to load PDF thumbnail", e)
                    mediaImageView.setImageResource(R.drawable.pdf_icon)
                }
            }

            @RequiresApi(Build.VERSION_CODES.Q)
            private fun loadDocumentThumbnail(uri: Uri, fallbackIcon: Int) {
                try {
                    val contentResolver = itemView.context.contentResolver
                    val thumbnail = contentResolver.loadThumbnail(uri, Size(300, 300), null)

                    Glide.with(itemView.context)
                        .load(thumbnail)
                        .centerCrop()
                        .placeholder(fallbackIcon)
                        .error(fallbackIcon)
                        .into(mediaImageView)
                } catch (e: Exception) {
                    Log.e("MediaAdapter", "Failed to load document thumbnail", e)
                    mediaImageView.setImageResource(fallbackIcon)
                }
            }

            private fun getFileName(uri: Uri): String? {
                var fileName: String? = null
                try {
                    val cursor = itemView.context.contentResolver.query(uri, null, null, null, null)
                    cursor?.use {
                        if (it.moveToFirst()) {
                            val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            if (displayNameIndex != -1) {
                                fileName = it.getString(displayNameIndex)
                            }
                        }
                    }

                    if (fileName.isNullOrEmpty() && uri.scheme == "file") {
                        fileName = File(uri.path ?: "").name
                        Log.d("MediaAdapter", "Fallback file name from URI path: $fileName")
                    }

                    if (fileName.isNullOrEmpty()) {
                        fileName = uri.path?.substringAfterLast("/") ?: "Unknown"
                        Log.d("MediaAdapter", "Last resort file name from URI path: $fileName")
                    }
                } catch (e: Exception) {
                    Log.e("MediaAdapter", "Failed to get file name for URI: $uri", e)
                    fileName = "Unknown"
                }
                return fileName
            }

            private fun removeMediaFile(position: Int) {
                if (position >= 0 && position < mediaUris.size) {
                    mediaUris.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, mediaUris.size)
                    onMediaRemoved(position, mediaUris.size)
                }
            }

        }

    }


    inner class OriginalPostMediaAdapter(

        private val post: Post,
        private val recyclerView: RecyclerView) :

        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private var screenWidth: Int = 0
        private var spaceBetweenItems: Int = 2.dpToPx(context)
        private var margin: Int = 8.dpToPx(context)

        init {
            val displayMetrics = DisplayMetrics()
            (context as? Activity)?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
            screenWidth = displayMetrics.widthPixels
            setupCleanRecyclerView(post.files.size, this)
        }

        private fun setupCleanRecyclerView(fileCount: Int, adapter: OriginalPostMediaAdapter) {
            recyclerView.visibility = View.VISIBLE

            when (fileCount) {

                1 -> recyclerView.layoutManager = GridLayoutManager(context, 1)

                2 -> recyclerView.layoutManager = GridLayoutManager(context, 2)

                3 -> recyclerView.layoutManager = StaggeredGridLayoutManager(2,
                    StaggeredGridLayoutManager.VERTICAL)

                else -> recyclerView.layoutManager = GridLayoutManager(context, 2)
            }
            recyclerView.setHasFixedSize(true)
            recyclerView.adapter = adapter
        }

        override fun getItemViewType(position: Int): Int {
            val fileTypes = post.fileTypes?.map { it.fileType?.lowercase() } ?: emptyList()
            val uniqueTypes = fileTypes.toSet()

            return when {
                fileTypes.isEmpty() -> VIEW_TYPE_IMAGE_FEED
                uniqueTypes.size > 1 -> VIEW_TYPE_COMBINATION_OF_MULTIPLE_FILES
                uniqueTypes.contains("image") -> VIEW_TYPE_IMAGE_FEED
                uniqueTypes.contains("audio") -> VIEW_TYPE_AUDIO_FEED
                uniqueTypes.contains("video") -> VIEW_TYPE_VIDEO_FEED
                uniqueTypes.contains("pdf") || uniqueTypes.contains("docx") ||
                        uniqueTypes.contains("pptx") || uniqueTypes.contains("xlsx") ||
                        uniqueTypes.contains("txt") || uniqueTypes.contains("rtf") ||
                        uniqueTypes.contains("odt") || uniqueTypes.contains("csv") -> VIEW_TYPE_DOCUMENT_FEED
                else -> VIEW_TYPE_COMBINATION_OF_MULTIPLE_FILES
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)

            return when (viewType) {
                VIEW_TYPE_IMAGE_FEED -> {
                    val itemView = inflater.inflate(
                        R.layout.feed_multiple_images_only_view_item, parent, false
                    )
                    EditPostWithFeedImagesOnly(itemView)
                }
                VIEW_TYPE_AUDIO_FEED -> {
                    val itemView = inflater.inflate(
                        R.layout.feed_multiple_audios_only_view_item, parent, false
                    )
                    EditPostWithFeedAudiosOnly(itemView)
                }
                VIEW_TYPE_VIDEO_FEED -> {
                    val itemView = inflater.inflate(
                        R.layout.feed_multiple_videos_only_view_item, parent, false
                    )
                    EditPostWithFeedVideosOnly(itemView)
                }
                VIEW_TYPE_DOCUMENT_FEED -> {
                    val itemView = inflater.inflate(
                        R.layout.feed_multiple_documents_only_view_item, parent, false
                    )
                    EditPostWithFeedDocumentsOnly(itemView)
                }
                VIEW_TYPE_COMBINATION_OF_MULTIPLE_FILES -> {
                    val itemView = inflater.inflate(
                        R.layout.feed_multiple_combination_of_files_view_item, parent, false
                    )
                    EditPostWithFeedCombinationOfMultipleFiles(itemView)
                }
                else -> throw IllegalArgumentException("Invalid view type: $viewType")
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder) {
                is EditPostWithFeedImagesOnly -> holder.onBind(post)
                is EditPostWithFeedAudiosOnly -> holder.onBind(post)
                is EditPostWithFeedVideosOnly -> holder.onBind(post)
                is EditPostWithFeedDocumentsOnly -> holder.onBind(post)
                is EditPostWithFeedCombinationOfMultipleFiles -> holder.onBind(post)
            }
        }

        override fun getItemCount(): Int = post.files.size


        inner class EditPostWithFeedImagesOnly(itemView: View) : RecyclerView.ViewHolder(itemView) {

            private val imageView: ImageView = itemView.findViewById(R.id.imageView)
            private val materialCardView: MaterialCardView = itemView.findViewById(R.id.materialCardView)
            private val countTextView: TextView = itemView.findViewById(R.id.textView)
            private val imageView2: ImageView = itemView.findViewById(R.id.imageView2)

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

            private fun setupCardViewCorners(context: Context) {
                val cornerRadius = 8.dpToPx(context).toFloat()
                materialCardView.radius = cornerRadius
                materialCardView.clipToOutline = true
                materialCardView.clipChildren = true
                materialCardView.cardElevation = 0f
                materialCardView.maxCardElevation = 0f
                materialCardView.strokeWidth = 0
                materialCardView.setContentPadding(0, 0, 0, 0)
                materialCardView.setCardBackgroundColor(Color.WHITE)
                imageView.clipToOutline = true
                imageView.outlineProvider = ViewOutlineProvider.BACKGROUND
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
                files: List<com.uyscuti.social.network.api.response.getrepostsPostsoriginal.File>,
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
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
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
                Log.d(TAG, "image feed $absoluteAdapterPosition item count $itemCount")
                val context = itemView.context
                setupCardViewCorners(context)
                itemView.setBackgroundColor(Color.TRANSPARENT)
                val displayMetrics = context.resources.displayMetrics
                val screenWidth = displayMetrics.widthPixels
                val spaceBetweenItems = 2.dpToPx(context)
                val (minHeight, maxHeight) = getAdaptiveHeights(context)
                val fileIdToFind = data.fileIds[absoluteAdapterPosition]
                val file = data.files.find { it.fileId == fileIdToFind }
                val imageUrl = file?.url ?: data.files.getOrNull(absoluteAdapterPosition)?.url ?: ""
                val fileSize = itemCount
                Log.d(TAG, "image getItemCount: $fileSize $imageUrl")

                itemView.setOnClickListener {
                    navigateToTappedFilesFragment(context, absoluteAdapterPosition, data.files, data.fileIds as List<String>)
                    onMultipleFilesClickListener?.multipleFileClickListener(absoluteAdapterPosition, data.files, data.fileIds as List<String>)
                }
                imageView.setOnClickListener {
                    navigateToTappedFilesFragment(context, absoluteAdapterPosition, data.files, data.fileIds as List<String>)
                }
                materialCardView.setOnClickListener {
                    navigateToTappedFilesFragment(context, absoluteAdapterPosition, data.files, data.fileIds as List<String>)
                }

                val layoutParams = materialCardView.layoutParams as ViewGroup.MarginLayoutParams

                when (fileSize) {

                    1 -> {

                        // Use full available width and height for single image
                        layoutParams.width = (screenWidth * 0.88).toInt()
                        layoutParams.height = maxHeight
                        layoutParams.setMargins(0, 0, 0, 0)

                        Glide.with(context)
                            .load(imageUrl)
                            .placeholder(R.drawable.flash21)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop()
                            .into(imageView)
                    }

                    2 -> {
                        val itemWidth = ((screenWidth - spaceBetweenItems) / 2 * 0.88).toInt()
                        layoutParams.width = itemWidth
                        layoutParams.height = (maxHeight * 0.65).toInt()
                        layoutParams.topMargin = 0
                        layoutParams.bottomMargin = 0
                        val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
                        layoutParams.leftMargin = if (isLeftColumn) 0 else spaceBetweenItems / 2
                        layoutParams.rightMargin = if (isLeftColumn) spaceBetweenItems / 2 else 0
                    }

                    3 -> {
                        when (absoluteAdapterPosition) {
                            0 -> {
                                val itemWidth = ((screenWidth - spaceBetweenItems) / 2 * 0.88).toInt()
                                layoutParams.width = itemWidth
                                layoutParams.height = (maxHeight * 0.7).toInt()
                                layoutParams.setMargins(0, 0, 0, 0)
                            }
                            1, 2 -> {
                                val itemWidth = ((screenWidth - spaceBetweenItems) / 2 * 0.88).toInt()
                                layoutParams.width = itemWidth
                                layoutParams.height = (maxHeight * 0.35).toInt()
                                layoutParams.topMargin = 0
                                layoutParams.bottomMargin = 0
                                val isLeftColumn = (absoluteAdapterPosition == 1)
                                layoutParams.leftMargin = if (isLeftColumn) (spaceBetweenItems) else (spaceBetweenItems)
                                layoutParams.rightMargin = 0
                                layoutParams.bottomMargin = spaceBetweenItems
                            }
                        }
                    }

                    4 -> {

                        val itemWidth = ((screenWidth - spaceBetweenItems) / 2 * 0.88).toInt()
                        val itemHeight = (maxHeight - spaceBetweenItems) / 2
                        layoutParams.width = itemWidth
                        layoutParams.height = itemHeight

                        val isTopRow = absoluteAdapterPosition < 2
                        val isLeft = absoluteAdapterPosition % 2 == 0

                        layoutParams.leftMargin = if (isLeft) 0 else spaceBetweenItems / 2
                        layoutParams.rightMargin = if (isLeft) spaceBetweenItems / 2 else 0
                        layoutParams.topMargin = 0
                        layoutParams.bottomMargin = if (isTopRow) spaceBetweenItems else 0

                        countTextView.visibility = View.GONE
                    }

                    else -> {
                        if (absoluteAdapterPosition >= 4) {
                            itemView.visibility = View.GONE
                            layoutParams.width = 0
                            layoutParams.height = 0
                            itemView.layoutParams = layoutParams
                            return
                        }
                        val itemWidth = ((screenWidth - spaceBetweenItems) / 2 * 0.88).toInt()
                        val itemHeight = (maxHeight - spaceBetweenItems) / 2
                        layoutParams.width = itemWidth
                        layoutParams.height = itemHeight
                        val isTopRow = absoluteAdapterPosition < 2
                        val isLeft = absoluteAdapterPosition % 2 == 0
                        layoutParams.leftMargin = if (isLeft) 0 else spaceBetweenItems / 2
                        layoutParams.rightMargin = if (isLeft) spaceBetweenItems / 2 else 0
                        layoutParams.topMargin = 0
                        layoutParams.bottomMargin = if (isTopRow) spaceBetweenItems else 0
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
                if (fileSize != 1) {
                    Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.flash21)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(imageView)
                }
            }
        }

        inner class EditPostWithFeedAudiosOnly(itemView: View) : RecyclerView.ViewHolder(itemView) {

            private val materialCardView: MaterialCardView = itemView.findViewById(R.id.materialCardView)
            private val artworkLayout: LinearLayout = itemView.findViewById(R.id.artworkLayout)
            private val countTextView: TextView = itemView.findViewById(R.id.textView)
            private val imageView2: ImageView = itemView.findViewById(R.id.imageView2)
            private val artworkVn: ShapeableImageView = itemView.findViewById(R.id.artworkVn)
            private val artworkImageView: ImageView = itemView.findViewById(R.id.artworkImageView)
            private val seekBar: SeekBar = itemView.findViewById(R.id.seekBar)
            private var currentPostData: Post? = null
            private val audioDurationTextView: TextView = itemView.findViewById(R.id.audioDuration)

            fun Int.dpToPx(context: Context): Int {
                return (this * context.resources.displayMetrics.density).toInt()
            }

            fun Float.dpToPx(context: Context): Int {
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

            private fun setupCardViewCorners(context: Context) {
                val cornerRadius = 8.dpToPx(context).toFloat()
                materialCardView.radius = cornerRadius
                materialCardView.clipToOutline = true
                materialCardView.clipChildren = true
                materialCardView.cardElevation = 0f
                materialCardView.maxCardElevation = 0f
                materialCardView.strokeWidth = 0
                materialCardView.setContentPadding(0, 0, 0, 0)
                materialCardView.setCardBackgroundColor(Color.WHITE)
                artworkImageView.clipToOutline = true
                artworkImageView.outlineProvider = ViewOutlineProvider.BACKGROUND
                artworkVn.clipToOutline = true
                artworkVn.outlineProvider = ViewOutlineProvider.BACKGROUND
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
                files: List<com.uyscuti.social.network.api.response.getrepostsPostsoriginal.File>,
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
                            val fileName = currentPostData?.fileNames?.find { it.fileId == fileId }?.fileName ?: ""
                            val postItem = PostItem(
                                audioUrl = file.url,
                                audioThumbnailUrl = null,
                                videoUrl = null,
                                videoThumbnailUrl = null,
                                postId = fileId ?: "audio_file_$index",
                                data = "Audio file: $fileName",
                                files = arrayListOf(file.url)
                            )
                            postItems.add(postItem)
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
                    Log.d(TAG, "Navigated to Tapped_Files_In_The_Container_View with ${files.size} audio files, starting at index $currentIndex")
                } else {
                    Log.e(TAG, "Activity is null, cannot navigate to fragment")
                }
            }

            @SuppressLint("SetTextI18n")
            fun onBind(data: Post) {
                Log.d(TAG, "audio feed $absoluteAdapterPosition item count $itemCount")
                this.currentPostData = data
                val context = itemView.context
                setupCardViewCorners(context)

                val displayMetrics = context.resources.displayMetrics
                val screenWidth = displayMetrics.widthPixels
                val spaceBetweenItems = 2.dpToPx(context) // Reduced from 2dp to 4dp for better visual separation
                val (minHeight, maxHeight) = getAdaptiveHeights(context)

                val fileIdToFind = data.fileIds[absoluteAdapterPosition]
                val file = data.files.find { it.fileId == fileIdToFind }
                val fileSize = itemCount
                Log.d(TAG, "audio getItemCount: $fileSize")

                val durationItem = data.duration?.find { it.fileId == fileIdToFind }
                audioDurationTextView.text = durationItem?.duration ?: ""
                audioDurationTextView.visibility = if (!durationItem?.duration.isNullOrEmpty()) View.VISIBLE else View.GONE

                val fileName = data.fileNames?.find { it.fileId == fileIdToFind }?.fileName ?: ""
                when {
                    fileName.endsWith(".mp3", true) || fileName.endsWith(".m4a", true) -> {
                        artworkImageView.setImageResource(R.drawable.music_icon)
                        artworkImageView.visibility = View.VISIBLE
                        artworkImageView.scaleType = ImageView.ScaleType.CENTER_CROP
                        artworkLayout.visibility = View.GONE
                        artworkVn.visibility = View.GONE
                    }
                    fileName.endsWith(".ogg", true) || fileName.endsWith(".aac", true) || fileName.endsWith(".wav", true) || fileName.endsWith(".flac", true) ||
                            fileName.endsWith(".amr", true) || fileName.endsWith(".3gp", true) || fileName.endsWith(".opus", true) -> {
                        artworkImageView.visibility = View.GONE
                        val artworkLayoutWrapper = itemView.findViewById<MaterialCardView>(R.id.artworkLayoutWrapper)
                        artworkLayoutWrapper?.visibility = View.VISIBLE
                        artworkLayoutWrapper?.setCardBackgroundColor(Color.WHITE)
                        artworkLayout.visibility = View.VISIBLE
                        artworkVn.setImageResource(R.drawable.ic_audio_white_icon)
                        artworkVn.visibility = View.VISIBLE
                        val layoutParams = artworkVn.layoutParams
                        if (layoutParams != null) {
                            layoutParams.width = 120.dpToPx(context)
                            layoutParams.height = 270.dpToPx(context)
                            artworkVn.layoutParams = layoutParams
                        }
                    }
                    else -> {
                        artworkImageView.setImageResource(R.drawable.music_icon)
                        artworkImageView.visibility = View.VISIBLE
                        artworkImageView.scaleType = ImageView.ScaleType.CENTER_CROP
                        artworkLayout.visibility = View.GONE
                        artworkVn.visibility = View.GONE
                    }
                }

                val audioDurationLayout = itemView.findViewById<LinearLayout>(R.id.audioDurationLayout)
                audioDurationLayout?.visibility = View.VISIBLE
                imageView2.visibility = View.GONE
                seekBar.visibility = View.GONE

                itemView.setOnClickListener {
                    navigateToTappedFilesFragment(context, absoluteAdapterPosition, data.files, data.fileIds as List<String>)
                    onMultipleFilesClickListener?.multipleFileClickListener(absoluteAdapterPosition, data.files, data.fileIds as List<String>)
                }
                artworkImageView.setOnClickListener {
                    navigateToTappedFilesFragment(context, absoluteAdapterPosition, data.files, data.fileIds as List<String>)
                }
                artworkLayout.setOnClickListener {
                    navigateToTappedFilesFragment(context, absoluteAdapterPosition, data.files, data.fileIds as List<String>)
                }
                materialCardView.setOnClickListener {
                    navigateToTappedFilesFragment(context, absoluteAdapterPosition, data.files, data.fileIds as List<String>)
                }

                val layoutParams = materialCardView.layoutParams as ViewGroup.MarginLayoutParams

                when (fileSize) {
                    1 -> {
                        // Single file - use full width with no margins
                        layoutParams.width = (screenWidth * 0.87).toInt()
                        layoutParams.height = (maxHeight * 0.8).toInt()
                        layoutParams.setMargins(0, 0, 0, 0)
                    }

                    2 -> {
                        // Two files - reduced gap between items
                        val itemWidth = ((screenWidth - spaceBetweenItems) / 2 * 0.88).toInt()
                        layoutParams.width = itemWidth
                        layoutParams.height = (maxHeight * 0.65).toInt()
                        layoutParams.topMargin = 0
                        layoutParams.bottomMargin = 0
                        val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
                        layoutParams.leftMargin = if (isLeftColumn) 0 else spaceBetweenItems/2
                        layoutParams.rightMargin = 0
                    }

                    3 -> {
                        when (absoluteAdapterPosition) {
                            0 -> {
                                // First item - full width
                                val itemWidth = ((screenWidth - spaceBetweenItems) / 2 * 0.87).toInt()
                                layoutParams.height = (maxHeight * 0.7).toInt()
                                layoutParams.setMargins(0, 0, 0, spaceBetweenItems)
                            }
                            1, 2 -> {
                                // Second and third items - half width each
                                val itemWidth = ((screenWidth - spaceBetweenItems) / 2 * 0.87).toInt()
                                layoutParams.width = itemWidth
                                layoutParams.height = (maxHeight * 0.35).toInt()
                                layoutParams.topMargin = 0
                                layoutParams.bottomMargin = 0
                                val isLeftColumn = (absoluteAdapterPosition == 1)
                                layoutParams.leftMargin = if (isLeftColumn) (spaceBetweenItems) else (spaceBetweenItems)
                                layoutParams.rightMargin = 0
                                layoutParams.bottomMargin = spaceBetweenItems
                            }
                        }
                    }

                    4 -> {
                        // Four files in 2x2 grid - reduced gaps
                        val itemWidth = ((screenWidth - spaceBetweenItems) / 2 * 0.88).toInt()
                        val itemHeight = ((maxHeight - spaceBetweenItems) / 2 * 0.9).toInt()
                        layoutParams.width = itemWidth
                        layoutParams.height = itemHeight

                        val isTopRow = absoluteAdapterPosition < 2
                        val isLeft = absoluteAdapterPosition % 2 == 0

                        layoutParams.leftMargin = 0
                        layoutParams.rightMargin = if (isLeft) spaceBetweenItems/2 else 0
                        layoutParams.topMargin = 0
                        layoutParams.bottomMargin = if (isTopRow) spaceBetweenItems else 0

                        countTextView.visibility = View.GONE
                    }

                    else -> {
                        if (absoluteAdapterPosition >= 4) {
                            itemView.visibility = View.GONE
                            layoutParams.width = 0
                            layoutParams.height = 0
                            itemView.layoutParams = layoutParams
                            return
                        }

                        // More than 4 files - show first 3 and +count on 4th
                        val itemWidth = ((screenWidth - spaceBetweenItems) / 2 * 0.88).toInt()
                        val itemHeight = ((maxHeight - spaceBetweenItems) / 2 * 0.9).toInt()
                        layoutParams.width = itemWidth
                        layoutParams.height = itemHeight

                        val isTopRow = absoluteAdapterPosition < 2
                        val isLeft = absoluteAdapterPosition % 2 == 0

                        layoutParams.leftMargin = 0
                        layoutParams.rightMargin = if (isLeft) spaceBetweenItems/2 else 0
                        layoutParams.topMargin = 0
                        layoutParams.bottomMargin = if (isTopRow) spaceBetweenItems else 0

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
            }
        }

        inner class EditPostWithFeedVideosOnly(itemView: View) : RecyclerView.ViewHolder(itemView) {

            private val cardView: CardView = itemView.findViewById(R.id.cardView)
            private val thumbnailImageView: ShapeableImageView = itemView.findViewById(R.id.feedThumbnail)
            private val overlayImageView: ShapeableImageView = itemView.findViewById(R.id.imageView2)
            private val countTextView: TextView = itemView.findViewById(R.id.countTextView)
            private val videoIconImageView: ImageView = itemView.findViewById(R.id.feedVideoImageView)
            private val videoDurationTextView: TextView = itemView.findViewById(R.id.feedVideoDurationTextView)

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

            private fun setupCardViewCorners(context: Context) {
                cardView.setCardBackgroundColor(Color.WHITE)
                thumbnailImageView.clipToOutline = true
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
                files: List<com.uyscuti.social.network.api.response.getrepostsPostsoriginal.File>,
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
                                audioUrl = null,
                                audioThumbnailUrl = null,
                                videoUrl = file.url,
                                videoThumbnailUrl = null,
                                postId = fileIds.getOrNull(index) ?: "video_file_$index",
                                data = "Video file $index",
                                files = arrayListOf(file.url)
                            )
                            postItems.add(postItem)
                        }
                        putParcelableArrayList("post_list", postItems)
                        putString("post_id", fileIds.getOrNull(currentIndex) ?: "video_file_$currentIndex")
                        putString("media_type", "video")
                    }
                    fragment.arguments = bundle
                    activity.supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                        .replace(R.id.frame_layout, fragment)
                        .addToBackStack("tapped_video_files_view")
                        .commit()
                    Log.d(TAG, "Navigated to Tapped_Files_In_The_Container_View with ${files.size} video files, starting at index $currentIndex")
                } else {
                    Log.e(TAG, "Activity is null, cannot navigate to fragment")
                }
            }

            private fun setupVideoThumbnail(context: Context, videoUrl: String, fileId: String, data: Post) {
                Glide.with(context)
                    .asBitmap()
                    .load(videoUrl)
                    .placeholder(R.drawable.profilepic2)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(thumbnailImageView)

                val durationItem = data.duration?.find { it.fileId == fileId }
                videoDurationTextView.text = durationItem?.duration ?: "00:00"
                videoDurationTextView.visibility = if (!durationItem?.duration.isNullOrEmpty()) View.VISIBLE else View.GONE
            }

            @SuppressLint("SetTextI18n")
            fun onBind(data: Post) {
                Log.d(TAG, "video feed $absoluteAdapterPosition item count $itemCount")
                val context = itemView.context
                setupCardViewCorners(context)
                itemView.setBackgroundColor(Color.TRANSPARENT)
                val displayMetrics = context.resources.displayMetrics
                val screenWidth = displayMetrics.widthPixels
                val spaceBetweenItems = 2.dpToPx(context)
                val (minHeight, maxHeight) = getAdaptiveHeights(context)
                val fileIdToFind = data.fileIds[absoluteAdapterPosition]
                val file = data.files.find { it.fileId == fileIdToFind }
                val videoUrl = file?.url ?: data.files.getOrNull(absoluteAdapterPosition)?.url ?: ""
                val fileSize = itemCount
                Log.d(TAG, "video getItemCount: $fileSize $videoUrl")

                setupVideoThumbnail(context, videoUrl, fileIdToFind.toString(), data)

                val clickListener = View.OnClickListener {
                    navigateToTappedFilesFragment(context, absoluteAdapterPosition, data.files, data.fileIds as List<String>)
                    onMultipleFilesClickListener?.multipleFileClickListener(absoluteAdapterPosition, data.files, data.fileIds as List<String>)
                }

                itemView.setOnClickListener(clickListener)
                thumbnailImageView.setOnClickListener(clickListener)
                cardView.setOnClickListener(clickListener)

                val layoutParams = cardView.layoutParams as ViewGroup.MarginLayoutParams

                when (fileSize) {
                    1 -> {
                        layoutParams.width = (screenWidth * 0.88).toInt()
                        layoutParams.height = (maxHeight * 0.8).toInt()
                        layoutParams.setMargins(0, 0, 0, 0)
                        countTextView.visibility = View.GONE
                        overlayImageView.visibility = View.GONE
                    }

                    2 -> {
                        val itemWidth = ((screenWidth - spaceBetweenItems) / 2 * 0.88).toInt()
                        layoutParams.width = itemWidth
                        layoutParams.height = (maxHeight * 0.65).toInt()
                        layoutParams.topMargin = 0
                        layoutParams.bottomMargin = 0
                        val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
                        layoutParams.leftMargin = if (isLeftColumn) 0 else spaceBetweenItems / 2
                        layoutParams.rightMargin = if (isLeftColumn) spaceBetweenItems / 2 else 0
                        countTextView.visibility = View.GONE
                        overlayImageView.visibility = View.GONE
                    }

                    3 -> {
                        when (absoluteAdapterPosition) {
                            0 -> {
                                val itemWidth = ((screenWidth - spaceBetweenItems) / 2 * 0.88).toInt()
                                layoutParams.width = itemWidth
                                layoutParams.height = (maxHeight * 0.7).toInt()
                                layoutParams.setMargins(0, 0, 0, 0)
                            }
                            1, 2 -> {
                                val itemWidth = ((screenWidth - spaceBetweenItems) / 2 * 0.88).toInt()
                                layoutParams.width = itemWidth
                                layoutParams.height = (maxHeight * 0.35).toInt()
                                layoutParams.topMargin = 0
                                layoutParams.bottomMargin = 0
                                val isLeftColumn = (absoluteAdapterPosition == 1)
                                layoutParams.leftMargin = if (isLeftColumn) (spaceBetweenItems / 2) else (spaceBetweenItems / 2)
                                layoutParams.rightMargin = 0
                                layoutParams.bottomMargin = spaceBetweenItems
                            }
                        }
                        countTextView.visibility = View.GONE
                        overlayImageView.visibility = View.GONE
                    }

                    4 -> {

                        val itemWidth = ((screenWidth - spaceBetweenItems) / 2 * 0.88).toInt()
                        val itemHeight = (maxHeight - spaceBetweenItems) / 2
                        layoutParams.width = itemWidth
                        layoutParams.height = itemHeight

                        val isTopRow = absoluteAdapterPosition < 2
                        val isLeft = absoluteAdapterPosition % 2 == 0

                        layoutParams.leftMargin = if (isLeft) 0 else spaceBetweenItems / 2
                        layoutParams.rightMargin = if (isLeft) spaceBetweenItems / 2 else 0
                        layoutParams.topMargin = 0
                        layoutParams.bottomMargin = if (isTopRow) spaceBetweenItems else 0

                        countTextView.visibility = View.GONE
                    }

                    else -> {
                        if (absoluteAdapterPosition >= 4) {
                            itemView.visibility = View.GONE
                            layoutParams.width = 0
                            layoutParams.height = 0
                            itemView.layoutParams = layoutParams
                            return
                        }
                        val itemWidth = ((screenWidth - spaceBetweenItems) / 2 * 0.88).toInt()
                        val itemHeight = (maxHeight - spaceBetweenItems) / 2
                        layoutParams.width = itemWidth
                        layoutParams.height = itemHeight
                        val isTopRow = absoluteAdapterPosition < 2
                        val isLeft = absoluteAdapterPosition % 2 == 0
                        layoutParams.leftMargin = if (isLeft) 0 else spaceBetweenItems / 2
                        layoutParams.rightMargin = if (isLeft) spaceBetweenItems / 2 else 0
                        layoutParams.topMargin = 0
                        layoutParams.bottomMargin = if (isTopRow) spaceBetweenItems else 0
                        if (absoluteAdapterPosition == 3) {
                            countTextView.visibility = View.VISIBLE
                            countTextView.text = "+${fileSize - 4}"
                            countTextView.textSize = 32f
                            countTextView.setPadding(12, 4, 12, 4)
                            countTextView.background = GradientDrawable().apply {
                                shape = GradientDrawable.RECTANGLE
                                cornerRadius = 16f
                                setColor(Color.parseColor("#80000000"))
                            }
                            overlayImageView.visibility = View.VISIBLE
                        } else {
                            countTextView.visibility = View.GONE
                            countTextView.setPadding(0, 0, 0, 0)
                            countTextView.background = null
                            overlayImageView.visibility = View.GONE
                        }
                    }
                }

                cardView.layoutParams = layoutParams
            }
        }

        inner class EditPostWithFeedDocumentsOnly(itemView: View) : RecyclerView.ViewHolder(itemView) {

            fun Int.dpToPx(context: Context): Int {
                return (this * context.resources.displayMetrics.density).toInt()
            }

            private var tag = "EditPostDocument"
            private val pdfImageView: ImageView = itemView.findViewById(R.id.pdfImageView)
            private val documentContainer: CardView = itemView.findViewById(R.id.documentContainer)
            private val fileTypeIcon: ImageView = itemView.findViewById(R.id.fileTypeIcon)

            // Helper function to get adaptive heights based on screen size
            private fun getAdaptiveHeights(context: Context): Pair<Int, Int> {
                val displayMetrics = context.resources.displayMetrics
                val screenHeight = displayMetrics.heightPixels

                // For documents: min = 15% of screen height, max = 38% of screen height
                val minHeight = (screenHeight * 0.15).toInt()
                val maxHeight = (screenHeight * 0.38).toInt()

                return Pair(minHeight, maxHeight)
            }

            // Helper function to constrain height within min/max bounds
            private fun getConstrainedHeight(context: Context, targetHeight: Int): Int {
                val (minHeight, maxHeight) = getAdaptiveHeights(context)
                return targetHeight.coerceIn(minHeight, maxHeight)
            }

            //A Helper function to get AppCompatActivity from context
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
                files: List<com.uyscuti.social.network.api.response.getrepostsPostsoriginal.File>,
                fileIds: List<String>
            ) {
                val activity = getActivityFromContext(context)
                if (activity != null) {
                    // Creating the fragment instance
                    val fragment = Tapped_Files_In_The_Container_View_Fragment()

                    // Creating bundle to pass data to the fragment
                    val bundle = Bundle().apply {
                        putInt("current_index", currentIndex)
                        putInt("total_files", files.size)

                        // Converting files to ArrayList of URLs for easy passing
                        val fileUrls = ArrayList<String>()
                        files.forEach { file ->
                            fileUrls.add(file.url)
                        }
                        putStringArrayList("file_urls", fileUrls)
                        putStringArrayList("file_ids", ArrayList(fileIds))

                        // Creating PostItem list for the ViewPager
                        val postItems = ArrayList<PostItem>()
                        files.forEachIndexed { index, file ->
                            val fileId = fileIds.getOrNull(index)
                            val fileName = post?.fileNames?.find { it.fileId == fileId }?.fileName ?: ""
                            val postItem = PostItem(
                                audioUrl = null,
                                audioThumbnailUrl = null,
                                videoUrl = null,
                                videoThumbnailUrl = null,
                                postId = fileId ?: "document_file_$index",
                                data = "Document file: $fileName",
                                files = arrayListOf(file.url)
                            )
                            postItems.add(postItem)
                        }
                        putParcelableArrayList("post_list", postItems)

                        // Set a default post ID
                        putString("post_id", fileIds.getOrNull(currentIndex) ?: "document_file_$currentIndex")
                        putString("media_type", "document")
                    }

                    fragment.arguments = bundle

                    // Navigating to the fragment with animation
                    activity.supportFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left,
                        )
                        .replace(R.id.frame_layout, fragment)
                        .addToBackStack("tapped_document_files_view")
                        .commit()

                    Log.d(TAG, "Navigated to Tapped_Files_In_The_Container_View with ${files.size} " +
                            "document files, starting at index $currentIndex")
                } else {
                    Log.e(TAG, "Activity is null, cannot navigate to fragment")
                }
            }

            @SuppressLint("SetTextI18n", "UseKtx")
            fun onBind(data: Post) {

                val sideMargin = 6.dpToPx(itemView.context) // Increased space between items
                val context = itemView.context // Fix: Define context properly
                val displayMetrics = context.resources.displayMetrics
                val screenWidth = displayMetrics.widthPixels

                // Calculate container width for repost (doesn't touch edges) - adjust this percentage as needed
                val containerWidth = (screenWidth * 0.99).toInt() // 95% of screen width instead of full width

                Log.d(TAG, "onBind: file type doc $absoluteAdapterPosition item count $itemCount")

                val fileIdToFind = data.fileIds[absoluteAdapterPosition]
                val documentType = data.fileNames?.find { it.fileId == fileIdToFind }

                val fileSize = itemCount

                // Get adaptive heights
                val (minHeight, maxHeight) = getAdaptiveHeights(context)

                // Set the file type icon (e.g., PDF, DOCX, PPTX)
                if (documentType != null) {
                    val fileName = documentType.fileName

                    fileTypeIcon.setImageResource(
                        when {
                            fileName.endsWith(".pdf", true) -> R.drawable.pdf_icon
                            fileName.endsWith(".doc", true) ||
                                    fileName.endsWith(".docx", true) -> R.drawable.word_icon
                            fileName.endsWith(".ppt", true) ||
                                    fileName.endsWith(".pptx", true) -> R.drawable.powerpoint_icon
                            fileName.endsWith(".xls", true) ||
                                    fileName.endsWith(".xlsx", true) -> R.drawable.excel_icon
                            fileName.endsWith(".txt", true) -> R.drawable.text_icon
                            fileName.endsWith(".rtf", true) -> R.drawable.text_icon
                            fileName.endsWith(".odt", true) -> R.drawable.word_icon
                            fileName.endsWith(".csv", true) -> R.drawable.excel_icon
                            else -> R.drawable.text_icon
                        }
                    )
                    fileTypeIcon.visibility = View.VISIBLE
                }

                // Handle PDF and other document types
                val thumbnail = data.thumbnail?.find { it.fileId == fileIdToFind }
                pdfImageView.scaleType = ImageView.ScaleType.CENTER_INSIDE

                if (thumbnail != null && thumbnail.thumbnailUrl.isNotEmpty()) {
                    Glide.with(context)
                        .load(thumbnail.thumbnailUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(pdfImageView)
                } else {
                    // Set placeholder based on file type
                    val fileName = documentType?.fileName ?: ""
                    val placeholderRes = when {
                        fileName.endsWith(".pdf", true) -> R.drawable.pdf_placeholder
                        fileName.endsWith(".docx", true) || fileName.endsWith(".doc", true) -> R.drawable.word_placeholder
                        fileName.endsWith(".pptx", true) || fileName.endsWith(".ppt", true) -> R.drawable.powerpoint_placeholder
                        fileName.endsWith(".xlsx", true) || fileName.endsWith(".xls", true) -> R.drawable.excel_placeholder
                        fileName.endsWith(".txt", true) -> R.drawable.text_placeholder
                        else -> R.drawable.documents
                    }
                    pdfImageView.setImageResource(placeholderRes)
                }

                pdfImageView.visibility = View.VISIBLE

                // COMPREHENSIVE CLICK HANDLING - Similar to original version
                // Updated click listener to navigate to fragment
                itemView.setOnClickListener {
                    navigateToTappedFilesFragment(
                        context,
                        absoluteAdapterPosition,
                        data.files,
                        data.fileIds as List<String>
                    )

                    // Optional: Still call the original listener if you need it for other purposes
                    onMultipleFilesClickListener?.multipleFileClickListener(
                        absoluteAdapterPosition,
                        data.files,
                        data.fileIds as List<String>
                    )
                }

                // Also add click listener to the document image itself for better UX
                pdfImageView.setOnClickListener {
                    navigateToTappedFilesFragment(
                        context,
                        absoluteAdapterPosition,
                        data.files,
                        data.fileIds as List<String>
                    )
                }

                // Add click listener to the document container (CardView)
                documentContainer.setOnClickListener {
                    navigateToTappedFilesFragment(
                        context,
                        absoluteAdapterPosition,
                        data.files,
                        data.fileIds as List<String>
                    )
                }

                // Add click listener to the file type icon
                fileTypeIcon.setOnClickListener {
                    navigateToTappedFilesFragment(
                        context,
                        absoluteAdapterPosition,
                        data.files,
                        data.fileIds as List<String>
                    )
                }

                when {

                    fileSize == 1 -> {

                        Log.d(TAG, "bind: file size 1")

                        val topMargin = 2.dpToPx(context)
                        // Use 85% of max height for single document
                        val adaptiveHeight = getConstrainedHeight(context, (maxHeight * 0.85).toInt())

                        val containerParams =
                            documentContainer.layoutParams as ViewGroup.MarginLayoutParams
                        containerParams.width = containerWidth // Use container width instead of MATCH_PARENT
                        containerParams.height = adaptiveHeight
                        containerParams.setMargins(
                            ((screenWidth - containerWidth) / 2), // Center horizontally
                            topMargin,
                            ((screenWidth - containerWidth) / 2), // Center horizontally
                            0
                        )
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

                        // Create a new ImageView for single document view to avoid parent conflicts
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

                        // Add click listener to the single image view
                        singleImageView.setOnClickListener {
                            navigateToTappedFilesFragment(
                                context,
                                absoluteAdapterPosition,
                                data.files,
                                data.fileIds as List<String>
                            )
                        }

                        // Add image view to the center container
                        centerContainer.addView(singleImageView)

                        // Create an overlay for the fileTypeIcon
                        val overlayLayout = FrameLayout(context).apply {
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }

                        // Configure the fileTypeIcon for the overlay
                        (fileTypeIcon.parent as? ViewGroup)?.removeView(fileTypeIcon)
                        fileTypeIcon.layoutParams = FrameLayout.LayoutParams(
                            20.dpToPx(context), // Width in dp
                            20.dpToPx(context), // Height in dp
                            Gravity.TOP or Gravity.START // Top-left corner
                        ).apply {
                            setMargins(8.dpToPx(context),
                                8.dpToPx(context), 0, 0) // Optional: add slight margin from top/left
                        }

                        // Re-adding click listener to fileTypeIcon after re-adding to layout
                        fileTypeIcon.setOnClickListener {
                            navigateToTappedFilesFragment(
                                context,
                                absoluteAdapterPosition,
                                data.files,
                                data.fileIds as List<String>
                            )
                        }

                        // Adding fileTypeIcon to the overlay
                        overlayLayout.addView(fileTypeIcon)

                        // Adding click listener to the overlay layout
                        overlayLayout.setOnClickListener {
                            navigateToTappedFilesFragment(
                                context,
                                absoluteAdapterPosition,
                                data.files,
                                data.fileIds as List<String>
                            )
                        }

                        // Adding the overlay on top of the center container
                        centerContainer.addView(overlayLayout)

                        // Add click listener to the center container
                        centerContainer.setOnClickListener {
                            navigateToTappedFilesFragment(
                                context,
                                absoluteAdapterPosition,
                                data.files,
                                data.fileIds as List<String>
                            )
                        }

                        // Adding the center container to the document container
                        documentContainer.addView(centerContainer)

                        val thumbnails = data.thumbnail?.filter { thumb ->
                            data.fileIds.contains(thumb.fileId)
                        } ?: emptyList()

                        // Loading the image thumbnail into singleImageView
                        thumbnails.getOrNull(0)?.let { thumb ->
                            Glide.with(context)
                                .load(thumb.thumbnailUrl)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(singleImageView)
                        }
                    }

                    fileSize == 2 -> {

                        Log.d(TAG, "onBind: Document file size == 2")

                        val cardView = itemView.findViewById<CardView>(R.id.documentContainer)
                        val imageView = itemView.findViewById<ImageView>(R.id.pdfImageView)

                        // Use 75% of max height for two documents
                        val adaptiveHeight = getConstrainedHeight(context, (maxHeight * 0.75).toInt())

                        // Calculate individual item width within the container - REDUCED BY 5%
                        val itemWidth = ((containerWidth - sideMargin) / 2 * 0.95).toInt()

                        // Layout params for CardView
                        val cardLayoutParams = cardView.layoutParams as ViewGroup.MarginLayoutParams
                        cardLayoutParams.width = itemWidth
                        cardLayoutParams.height = adaptiveHeight // Set adaptive height for both CardViews

                        // Reset all margins
                        cardLayoutParams.topMargin = 0
                        cardLayoutParams.bottomMargin = 0

                        when (absoluteAdapterPosition) {
                            0 -> {
                                // First item: Touch left edge of container
                                cardLayoutParams.setMargins(
                                    ((screenWidth - containerWidth) / 2), // Start at container edge
                                    0,
                                    sideMargin,
                                    0
                                )
                            }
                            1 -> {
                                // Second item: Touch right edge of container
                                cardLayoutParams.setMargins(
                                    sideMargin,
                                    0,
                                    ((screenWidth - containerWidth) / 2), // End at container edge
                                    0
                                )
                            }
                        }

                        cardView.layoutParams = cardLayoutParams

                        // Match ImageView height to the CardView's height
                        val imageLayoutParams = imageView.layoutParams as ViewGroup.MarginLayoutParams
                        imageLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                        imageLayoutParams.height = cardLayoutParams.height // Match the height of the CardView
                        imageLayoutParams.topMargin = 0
                        imageLayoutParams.bottomMargin = 0
                        imageView.layoutParams = imageLayoutParams

                        // Ensure image scales properly
                        imageView.scaleType = ImageView.ScaleType.FIT_XY
                    }

                    fileSize >= 3 -> {

                        Log.d(TAG, "onBind: Document file size >= 3")

                        // Hide additional items (index 2 and beyond)
                        if (absoluteAdapterPosition >= 2) {
                            Log.d(TAG, "onBind: position >= 2, hiding item view")
                            itemView.visibility = View.GONE
                            itemView.layoutParams = RecyclerView.LayoutParams(0, 0) // Prevent item from taking space
                            return
                        }

                        val cardView = itemView.findViewById<CardView>(R.id.documentContainer)
                        val imageView = itemView.findViewById<ImageView>(R.id.pdfImageView)

                        // Use 70% of max height for multiple documents
                        val adaptiveHeight = getConstrainedHeight(context, (maxHeight * 0.75).toInt())

                        // Calculate individual item width within the container - REDUCED BY 5%
                        val itemWidth = ((containerWidth - sideMargin) / 2 * 0.95).toInt()

                        // Match CardView to parent with adaptive height
                        val cardLayoutParams = cardView.layoutParams as ViewGroup.MarginLayoutParams
                        cardLayoutParams.width = itemWidth
                        cardLayoutParams.height = adaptiveHeight // Adaptive height for all CardViews

                        when (absoluteAdapterPosition) {
                            0 -> {
                                // First item: Touch left edge of container, gap on right
                                cardLayoutParams.setMargins(
                                    ((screenWidth - containerWidth) / 2), // Start at container edge
                                    0,
                                    sideMargin,
                                    0
                                )
                            }
                            1 -> {
                                // Second item: Gap on left, touch right edge of container
                                cardLayoutParams.setMargins(
                                    sideMargin,
                                    0,
                                    ((screenWidth - containerWidth) / 2), // End at container edge
                                    0
                                )
                            }
                        }

                        cardView.layoutParams = cardLayoutParams

                        // Set the ImageView height to match the CardView
                        val imageLayoutParams = imageView.layoutParams as ViewGroup.MarginLayoutParams
                        imageLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                        imageLayoutParams.height = cardLayoutParams.height // Match the height of the CardView
                        imageView.layoutParams = imageLayoutParams

                        // Ensure the image scales properly
                        imageView.scaleType = ImageView.ScaleType.FIT_XY

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

                                    // Add click listener to the overlay container
                                    overlayContainer.setOnClickListener {
                                        navigateToTappedFilesFragment(
                                            context,
                                            absoluteAdapterPosition,
                                            data.files,
                                            data.fileIds as List<String>
                                        )
                                    }

                                    // Create the "+N" TextView
                                    val textView = TextView(context).apply {
                                        text = plusCountText
                                        setTextColor(Color.WHITE)
                                        textSize = 32f
                                        gravity = Gravity.CENTER
                                    }

                                    // Add click listener to the text view
                                    textView.setOnClickListener {
                                        navigateToTappedFilesFragment(
                                            context,
                                            absoluteAdapterPosition,
                                            data.files,
                                            data.fileIds as List<String>
                                        )
                                    }

                                    // Add TextView to the container
                                    overlayContainer.addView(textView)

                                    // Add container to the image wrapper
                                    imageWrapper.addView(overlayContainer)

                                    // Add click listener to the image wrapper
                                    imageWrapper.setOnClickListener {
                                        navigateToTappedFilesFragment(
                                            context,
                                            absoluteAdapterPosition,
                                            data.files,
                                            data.fileIds as List<String>
                                        )
                                    }

                                    // Add everything back to the original parent
                                    parent.addView(imageWrapper, index)
                                }
                            }
                        }
                    }
                }
            }
        }

        inner class EditPostWithFeedCombinationOfMultipleFiles(itemView: View) : RecyclerView.ViewHolder(itemView) {

            private val materialCardView: MaterialCardView = itemView.findViewById(R.id.materialCardView)
            private val imageView: ImageView = itemView.findViewById(R.id.imageView)
            private val countTextView: TextView = itemView.findViewById(R.id.countTextView)
            private val fileTypeIcon: ImageView = itemView.findViewById(R.id.fileTypeIcon)
            private val feedVideoDurationTextView: TextView = itemView.findViewById(R.id.feedVideoDurationTextView)
            private val pdfImageView: ImageView = itemView.findViewById(R.id.pdfImageView)
            private val playButton: ImageView = itemView.findViewById(R.id.playButton)
            private val feedVideoImageView: ImageView = itemView.findViewById(R.id.feedVideoImageView)
            private val imageView2: ImageView = itemView.findViewById(R.id.imageViewOverlay)
            private var currentPostData: com.uyscuti.social.network.api.response.allFeedRepostsPost.Post? = null
            var onMultipleFilesClickListener: OnMultipleFilesClickListener? = null

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

            private fun setupCardViewCorners(context: Context) {
                val cornerRadius = 8.dpToPx(context).toFloat()
                materialCardView.radius = cornerRadius
                materialCardView.setCardBackgroundColor(Color.WHITE)
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

                imageView2.clipToOutline = true
                imageView2.outlineProvider = object : ViewOutlineProvider() {
                    override fun getOutline(view: View, outline: Outline) {
                        outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
                    }
                }

                pdfImageView.clipToOutline = true
                pdfImageView.outlineProvider = object : ViewOutlineProvider() {
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
                data: com.uyscuti.social.network.api.response.allFeedRepostsPost.Post,
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
                files: List<com.uyscuti.social.network.api.response.getrepostsPostsoriginal.File>,
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
                            val fileType = currentPostData?.fileTypes?.find { it.fileId == fileId }?.fileType?.lowercase() ?: ""
                            val postItem = PostItem(
                                audioUrl = if (fileType.contains("audio")) file.url else null,
                                audioThumbnailUrl = null,
                                videoUrl = if (fileType.contains("video")) file.url else null,
                                videoThumbnailUrl = currentPostData?.thumbnail?.find { it.fileId == fileId }?.thumbnailUrl,
                                postId = fileId ?: "file_$index",
                                data = "Post data for file $index",
                                files = arrayListOf(file.url)
                            )
                            postItems.add(postItem)
                        }
                        putParcelableArrayList("post_list", postItems)
                        putString("post_id", fileIds.getOrNull(currentIndex) ?: "file_$currentIndex")
                    }
                    fragment.arguments = bundle
                    try {
                        activity.supportFragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                            .replace(R.id.frame_layout, fragment)
                            .addToBackStack("tapped_files_view")
                            .commit()
                        Log.d(TAG, "Navigated to Tapped_Files_In_The_Container_View with ${files.size} files, starting at index $currentIndex")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error navigating to fragment", e)
                    }
                } else {
                    Log.e(TAG, "Activity is null, cannot navigate to fragment")
                }
            }

            private fun positionCountTextAtBottomRight(context: Context) {
                countTextView.visibility = View.VISIBLE
                countTextView.setPadding(0, 0, 0, 0)
                val background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 16f
                    setColor("#80000000".toColorInt())
                }
                countTextView.background = background
                countTextView.setPadding(12, 4, 12, 4)
                val layoutParams = countTextView.layoutParams
                if (layoutParams is RelativeLayout.LayoutParams) {
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END)
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                    layoutParams.setMargins(0, 0, 16, 16)
                } else if (layoutParams is ConstraintLayout.LayoutParams) {
                    layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                    layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                    layoutParams.setMargins(0, 0, 16, 16)
                } else if (layoutParams is FrameLayout.LayoutParams) {
                    layoutParams.gravity = Gravity.BOTTOM or Gravity.END
                    layoutParams.setMargins(0, 0, 16, 16)
                }
                countTextView.layoutParams = layoutParams
            }

            @SuppressLint("SetTextI18n", "UseKtx")
            fun onBind(data: com.uyscuti.social.network.api.response.allFeedRepostsPost.Post) {
                Log.d(TAG, "combination feed $absoluteAdapterPosition item count $itemCount")
                currentPostData = data
                val context = itemView.context
                setupCardViewCorners(context)
                itemView.setBackgroundColor(Color.TRANSPARENT)
                val displayMetrics = context.resources.displayMetrics
                val screenWidth = displayMetrics.widthPixels
                val margin = 4.dpToPx(context)
                val spaceBetweenRows = 4.dpToPx(context)
                val (minHeight, maxHeight) = getAdaptiveHeights(context)
                val actualFileIndex = if (data.files.size == 3) {
                    getCorrectFileIndex(data, absoluteAdapterPosition)
                } else {
                    absoluteAdapterPosition
                }
                val fileIdToFind = data.fileIds[actualFileIndex]
                val file = data.files.find { it.fileId == fileIdToFind }
                val fileUrl = file?.url ?: data.files.getOrNull(actualFileIndex)?.url ?: ""
                val mimeType = data.fileTypes.getOrNull(actualFileIndex)?.fileType?.lowercase() ?: ""
                val durationItem = data.duration?.find { it.fileId == fileIdToFind }
                feedVideoDurationTextView.text = durationItem?.duration
                val fileSize = itemCount
                Log.d(TAG, "combination getItemCount: $fileSize $fileUrl")

                playButton.visibility = View.GONE
                feedVideoImageView.visibility = View.GONE
                feedVideoDurationTextView.visibility = View.VISIBLE
                imageView2.visibility = View.GONE
                countTextView.visibility = View.GONE
                imageView.visibility = View.GONE
                fileTypeIcon.visibility = View.GONE
                pdfImageView.visibility = View.GONE

                val clickListener = View.OnClickListener {
                    navigateToTappedFilesFragment(context,
                        actualFileIndex, data.files, data.fileIds as List<String>)
                    onMultipleFilesClickListener?.multipleFileClickListener(
                        actualFileIndex, data.files, data.fileIds as List<String>)
                }
                itemView.setOnClickListener(clickListener)
                imageView.setOnClickListener(clickListener)
                imageView2.setOnClickListener(clickListener)
                fileTypeIcon.setOnClickListener(clickListener)
                pdfImageView.setOnClickListener(clickListener)
                playButton.setOnClickListener(clickListener)
                feedVideoImageView.setOnClickListener(clickListener)
                feedVideoDurationTextView.setOnClickListener(clickListener)
                countTextView.setOnClickListener(clickListener)
                materialCardView.setOnClickListener(clickListener)

                val layoutParams = materialCardView.layoutParams as ViewGroup.MarginLayoutParams

                when {

                    fileSize == 2 -> {
                        layoutParams.width = ((screenWidth - spaceBetweenRows) / 2 * 0.85).toInt()
                        layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.65).toInt())
                        layoutParams.topMargin = 0
                        layoutParams.bottomMargin = 0
                        val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
                        layoutParams.leftMargin = if (isLeftColumn) 0 else (spaceBetweenRows / 2)
                        layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows / 2) else 0
                        loadFileContent(fileUrl, mimeType, data, fileIdToFind.toString(), context)
                    }

                    fileSize == 3 -> {

                        val documentCount = data.fileTypes.count { isDocument(it.fileType) }
                        if (documentCount == 2) {
                            val documentIndices = data.fileTypes.mapIndexed { index, fileType ->
                                if (isDocument(fileType.fileType)) index else -1
                            }.filter { it != -1 }
                            val actualFileIndex = when (absoluteAdapterPosition) {
                                0 -> documentIndices[0]
                                1 -> documentIndices[1]
                                2 -> data.fileTypes.indices.find { !documentIndices.contains(it) } ?: 0
                                else -> absoluteAdapterPosition
                            }
                            val fileIdToFind = data.fileIds[actualFileIndex]
                            val file = data.files.find { it.fileId == fileIdToFind }
                            val fileUrl = file?.url ?: data.files.getOrNull(actualFileIndex)?.url ?: ""
                            val mimeType = data.fileTypes.getOrNull(actualFileIndex)?.fileType?.lowercase() ?: ""
                            val durationItem = data.duration?.find { it.fileId == fileIdToFind }
                            feedVideoDurationTextView.text = durationItem?.duration ?: ""

                            layoutParams.width = ((screenWidth - spaceBetweenRows) / 2 * 0.85).toInt()
                            val baseFileHeight = getConstrainedHeight(context, (maxHeight * 0.7).toInt())
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
                                    countTextView.visibility = View.VISIBLE
                                    countTextView.text = "+${fileSize - 4}"
                                    countTextView.textSize = 32f
                                    countTextView.setPadding(12, 4, 12, 4)
                                    // Position count text at bottom right
                                    positionCountTextAtBottomRight(context)
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
                                loadFileContent(fileUrl, mimeType, data,
                                    fileIdToFind.toString(), context)
                            }
                        }

                        else {
                            when (absoluteAdapterPosition) {
                                0 -> {
                                    layoutParams.width = ((screenWidth - spaceBetweenRows) / 2 * 0.85).toInt()
                                    val baseFileHeight = getConstrainedHeight(context, (maxHeight * 0.7).toInt())
                                    val rightSideItemHeight = baseFileHeight / 2
                                    val totalRightSideHeight = (rightSideItemHeight * 2) + (spaceBetweenRows / 2)
                                    layoutParams.height = totalRightSideHeight
                                    layoutParams.leftMargin = 0
                                    layoutParams.rightMargin = (spaceBetweenRows / 2)
                                    layoutParams.topMargin = 0
                                    layoutParams.bottomMargin = 0
                                    if (layoutParams is StaggeredGridLayoutManager.LayoutParams) {
                                        layoutParams.isFullSpan = false
                                    }
                                }
                                1 -> {
                                    layoutParams.width = ((screenWidth - spaceBetweenRows) / 2 * 0.85).toInt()
                                    val baseFileHeight = getConstrainedHeight(context, (maxHeight * 0.7).toInt())
                                    val totalHeight = baseFileHeight + (spaceBetweenRows / 2)
                                    layoutParams.height = (totalHeight - (spaceBetweenRows / 2)) / 2
                                    layoutParams.leftMargin = (spaceBetweenRows / 2)
                                    layoutParams.rightMargin = 0
                                    layoutParams.topMargin = 0
                                    layoutParams.bottomMargin = (spaceBetweenRows / 2)
                                }
                                2 -> {
                                    layoutParams.width = ((screenWidth - spaceBetweenRows) / 2 * 0.85).toInt()
                                    val baseFileHeight = getConstrainedHeight(context, (maxHeight * 0.7).toInt())
                                    val totalHeight = baseFileHeight + (spaceBetweenRows / 2)
                                    layoutParams.height = (totalHeight - (spaceBetweenRows / 2)) / 2
                                    layoutParams.leftMargin = (spaceBetweenRows / 2)
                                    layoutParams.rightMargin = 0
                                    layoutParams.topMargin = (spaceBetweenRows / 2)
                                    layoutParams.bottomMargin = 0
                                }
                            }
                            loadFileContent(fileUrl, mimeType, data,
                                fileIdToFind.toString(), context)
                        }
                    }

                    fileSize == 4 -> {

                        layoutParams.width = ((screenWidth - spaceBetweenRows) / 2 * 0.85).toInt()
                        layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.65).toInt())
                        layoutParams.topMargin = if (absoluteAdapterPosition < 2) 0 else spaceBetweenRows
                        layoutParams.bottomMargin = if (absoluteAdapterPosition >= 2) 0 else 0
                        val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
                        layoutParams.leftMargin = if (isLeftColumn) 0 else (spaceBetweenRows / 2)
                        layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows / 2) else 0
                        loadFileContent(fileUrl, mimeType, data, fileIdToFind.toString(), context)

                    }

                    fileSize == 5 -> {
                        if (absoluteAdapterPosition >= 4) {
                            itemView.visibility = View.GONE
                            layoutParams.width = 0
                            layoutParams.height = 0
                            itemView.layoutParams = layoutParams
                            return
                        }
                        itemView.visibility = View.VISIBLE
                        layoutParams.width = ((screenWidth - spaceBetweenRows) / 2 * 0.85).toInt()
                        layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.65).toInt())
                        val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
                        layoutParams.leftMargin = if (isLeftColumn) 0 else (spaceBetweenRows / 2)
                        layoutParams.rightMargin = if (isLeftColumn) (spaceBetweenRows / 2) else 0
                        layoutParams.topMargin = if (absoluteAdapterPosition < 2) 0 else spaceBetweenRows
                        layoutParams.bottomMargin = if (absoluteAdapterPosition < 2) spaceBetweenRows / 2 else 0
                        itemView.layoutParams = layoutParams
                        if (absoluteAdapterPosition == 3) {
                            countTextView.visibility = View.VISIBLE
                            countTextView.text = "+${fileSize - 4}"
                            countTextView.textSize = 32f
                            countTextView.setPadding(12, 4, 12, 4)
                            // Position count text at bottom right
                            positionCountTextAtBottomRight(context)
                        } else {
                            countTextView.visibility = View.GONE
                            countTextView.background = null
                        }
                        loadFileContent(fileUrl, mimeType, data, fileIdToFind.toString(), context)
                    }

                    fileSize > 4 -> {
                        if (absoluteAdapterPosition >= 4) {
                            itemView.visibility = View.GONE
                            layoutParams.width = 0
                            layoutParams.height = 0
                            itemView.layoutParams = layoutParams
                            return
                        }
                        itemView.visibility = View.VISIBLE
                        layoutParams.width = ((screenWidth - spaceBetweenRows) / 2 * 0.85).toInt()
                        layoutParams.height = getConstrainedHeight(context, (maxHeight * 0.65).toInt())
                        val isLeftColumn = (absoluteAdapterPosition % 2 == 0)
                        layoutParams.leftMargin = if (isLeftColumn) 0 else spaceBetweenRows
                        layoutParams.rightMargin = if (isLeftColumn) spaceBetweenRows else 0
                        layoutParams.topMargin = if (absoluteAdapterPosition < 2) 0 else spaceBetweenRows / 2
                        layoutParams.bottomMargin = if (absoluteAdapterPosition < 2) spaceBetweenRows / 2 else 0
                        itemView.layoutParams = layoutParams
                        if (absoluteAdapterPosition == 3) {
                            countTextView.visibility = View.VISIBLE
                            countTextView.text = "+${fileSize - 4}"
                            countTextView.textSize = 32f
                            countTextView.setPadding(12, 4, 12, 4)
                            // Position count text at bottom right
                            positionCountTextAtBottomRight(context)
                        } else {
                            countTextView.visibility = View.GONE
                            countTextView.background = null
                        }
                        loadFileContent(fileUrl, mimeType, data, fileIdToFind.toString(), context)
                    }
                }
                materialCardView.layoutParams = layoutParams
            }

            private fun loadFileContent(
                fileUrl: String,
                mimeType: String,
                data: com.uyscuti.social.network.api.response.allFeedRepostsPost.Post,
                fileIdToFind: String,
                context: Context
            ) {
                when {
                    mimeType.startsWith("image") -> {
                        imageView.visibility = View.VISIBLE
                        Glide.with(context)
                            .load(fileUrl)
                            .placeholder(R.drawable.flash21)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop()
                            .into(imageView)
                        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                        fileTypeIcon.visibility = View.GONE
                        pdfImageView.visibility = View.GONE
                    }

                    mimeType.startsWith("video") -> {
                        imageView.visibility = View.VISIBLE
                        playButton.visibility = View.VISIBLE
                        feedVideoImageView.visibility = View.VISIBLE
                        feedVideoDurationTextView.visibility = View.VISIBLE
                        val thumbnail = data.thumbnail.find { it.fileId == fileIdToFind }
                        if (thumbnail != null) {
                            Glide.with(context)
                                .asBitmap()
                                .load(thumbnail.thumbnailUrl)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .centerCrop()
                                .into(imageView)
                        } else {
                            Glide.with(context)
                                .asBitmap()
                                .load(R.drawable.videoplaceholder)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .centerCrop()
                                .into(imageView)
                        }
                        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                        fileTypeIcon.visibility = View.VISIBLE
                        fileTypeIcon.setImageResource(com.uyscuti.social.call.R.drawable.ic_video_call)
                        val params = fileTypeIcon.layoutParams as FrameLayout.LayoutParams
                        params.gravity = Gravity.BOTTOM or Gravity.START
                        params.marginStart = 8.dpToPx(context)
                        params.bottomMargin = 8.dpToPx(context)
                        fileTypeIcon.layoutParams = params
                        pdfImageView.visibility = View.GONE
                    }

                    mimeType.startsWith("audio") -> {
                        imageView.visibility = View.VISIBLE
                        playButton.visibility = View.VISIBLE
                        feedVideoDurationTextView.visibility = View.VISIBLE
                        imageView.setImageResource(R.drawable.music_icon)
                        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                        fileTypeIcon.visibility = View.VISIBLE
                        fileTypeIcon.setImageResource(R.drawable.ic_audio_white_icon)
                        val params = fileTypeIcon.layoutParams as FrameLayout.LayoutParams
                        params.gravity = Gravity.BOTTOM or Gravity.START
                        params.marginStart = 8.dpToPx(context)
                        params.bottomMargin = 8.dpToPx(context)
                        fileTypeIcon.layoutParams = params
                        pdfImageView.visibility = View.GONE
                    }

                    isDocument(mimeType) -> {
                        pdfImageView.visibility = View.VISIBLE
                        fileTypeIcon.visibility = View.VISIBLE
                        val thumbnail = data.thumbnail.find { it.fileId == fileIdToFind }
                        pdfImageView.scaleType = ImageView.ScaleType.CENTER_CROP
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
                                else -> R.drawable.documents
                            }
                        )
                        if (thumbnail != null) {
                            Glide.with(context)
                                .load(thumbnail.thumbnailUrl)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .centerCrop()
                                .into(pdfImageView)
                        } else {
                            pdfImageView.setImageResource(
                                when {
                                    mimeType.contains("pdf") -> R.drawable.pdf_placeholder
                                    mimeType.contains("docx") ||
                                            mimeType.contains("doc") -> R.drawable.word_placeholder
                                    mimeType.contains("pptx") ||
                                            mimeType.contains("ppt") -> R.drawable.powerpoint_placeholder
                                    mimeType.contains("xlsx") ||
                                            mimeType.contains("xls") -> R.drawable.excel_placeholder
                                    mimeType.contains("txt") -> R.drawable.text_placeholder
                                    else -> R.drawable.documents
                                }
                            )
                        }
                        // Position file type icon at TOP LEFT for documents
                        val params = fileTypeIcon.layoutParams as FrameLayout.LayoutParams
                        params.gravity = Gravity.TOP or Gravity.START
                        params.marginStart = 8.dpToPx(context)
                        params.topMargin = 8.dpToPx(context)
                        params.bottomMargin = 0  // Reset bottom margin
                        fileTypeIcon.layoutParams = params
                        imageView.visibility = View.GONE
                    }

                    else -> {
                        pdfImageView.visibility = View.VISIBLE
                        fileTypeIcon.visibility = View.VISIBLE
                       // pdfImageView.setImageResource(R.drawable.feed_mixed_image_view_rounded_corner)
                        pdfImageView.scaleType = ImageView.ScaleType.CENTER_CROP

                        // Set generic file icon
                        fileTypeIcon.setImageResource(R.drawable.documents)

                        // Position file type icon at TOP LEFT for unknown file types
                        val params = fileTypeIcon.layoutParams as FrameLayout.LayoutParams
                        params.gravity = Gravity.TOP or Gravity.START
                        params.marginStart = 8.dpToPx(context)
                        params.topMargin = 8.dpToPx(context)
                        params.bottomMargin = 0  // Reset bottom margin
                        fileTypeIcon.layoutParams = params

                        imageView.visibility = View.GONE
                    }
                }
            }


        }


    }


    data class PostItem(
        val audioUrl: String?,
        val audioThumbnailUrl: String?,
        val videoUrl: String?,
        val videoThumbnailUrl: String?,
        val postId: String,
        val data: String,
        val files: ArrayList<String>
    ) :
        Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.createStringArrayList() ?: ArrayList()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(audioUrl)
            parcel.writeString(audioThumbnailUrl)
            parcel.writeString(videoUrl)
            parcel.writeString(videoThumbnailUrl)
            parcel.writeString(postId)
            parcel.writeString(data)
            parcel.writeStringList(files)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<PostItem> {
            override fun createFromParcel(parcel: Parcel): PostItem {
                return PostItem(parcel)
            }

            override fun newArray(size: Int): Array<PostItem?> {
                return arrayOfNulls(size)
            }
        }
    }
}