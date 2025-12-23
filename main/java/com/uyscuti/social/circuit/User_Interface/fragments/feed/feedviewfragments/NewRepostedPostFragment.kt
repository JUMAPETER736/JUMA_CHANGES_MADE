package com.uyscuti.social.circuit.User_Interface.fragments.feed.feedviewfragments

import com.uyscuti.social.circuit.R
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import androidx.core.content.ContextCompat
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.pdf.PdfRenderer
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toUri
import androidx.core.view.setPadding
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.uyscuti.social.circuit.adapter.feed.FeedAdapter
import com.uyscuti.social.circuit.adapter.feed.MultipleImagesListener
import com.uyscuti.social.circuit.adapter.feed.multiple_files.FeedMixedFilesViewPagerAdapter
import com.uyscuti.social.circuit.adapter.feed.multiple_files.FeedRepostViewFileAdapter
import com.uyscuti.social.circuit.adapter.feed.multiple_files.FeedVideoThumbnailAdapter
import com.uyscuti.social.circuit.adapter.feed.multiple_files.MixedFilesUploadAdapter
import com.uyscuti.social.circuit.adapter.feed.multiple_files.MultipleAudiosListener
import com.uyscuti.social.circuit.adapter.feed.multiple_files.MultipleFeedAudioAdapter
import com.uyscuti.social.circuit.adapter.feed.multiple_files.MultipleFeedFilesPagerAdapter
import com.uyscuti.social.circuit.adapter.feed.multiple_files.MultipleSelectedFeedVideoAdapter
import com.uyscuti.social.circuit.adapter.feed.multiple_files.MultipleVideosListener
import com.uyscuti.social.circuit.adapter.feed.multiple_files.UriTypeAdapter
import com.uyscuti.social.circuit.eventbus.FeedUploadResponseEvent
import com.uyscuti.social.circuit.eventbus.ShowFeedFloatingActionButton
import com.uyscuti.social.circuit.interfaces.feedinterfaces.FeedTextViewFragmentInterface
import com.uyscuti.social.circuit.model.ShowAppBar
import com.uyscuti.social.circuit.model.ShowBottomNav
import com.uyscuti.social.circuit.model.feed.FeedMultipleImages
import com.uyscuti.social.circuit.model.feed.multiple_files.FeedMultipleAudios
import com.uyscuti.social.circuit.model.feed.multiple_files.FeedMultipleDocumentsDataClass
import com.uyscuti.social.circuit.model.feed.multiple_files.FeedMultipleVideos
import com.uyscuti.social.circuit.model.feed.multiple_files.MixedFeedUploadDataClass
import com.uyscuti.social.circuit.model.feed.multiple_files.MultipleAudios
import com.uyscuti.social.circuit.User_Interface.feed.FeedUploadWorker
import com.uyscuti.social.circuit.User_Interface.fragments.feed.UploadFeedActivity
import com.uyscuti.social.circuit.User_Interface.shorts.ShortsUploadWorker
import com.uyscuti.social.circuit.User_Interface.shorts.TopicsActivity
import com.uyscuti.social.circuit.User_Interface.shorts.UploadShortsActivity
import com.uyscuti.social.circuit.User_Interface.shorts.VideoUtils
import com.uyscuti.social.circuit.User_Interface.uploads.CameraActivity
import com.uyscuti.social.circuit.User_Interface.uploads.feed_uploads.FeedAudioActivity
import com.uyscuti.social.circuit.User_Interface.uploads.feed_uploads.FeedSelectVideoActivity
import com.uyscuti.social.circuit.utils.AudioDurationHelper.getFormattedDuration
import com.uyscuti.social.circuit.utils.AudioDurationHelper.reverseFormattedDuration
import com.uyscuti.social.circuit.utils.PathUtil
import com.uyscuti.social.circuit.utils.audio_compressor.AudioCompressorWithProgress
import com.uyscuti.social.circuit.utils.feedutils.ThumbnailUtil
import com.uyscuti.social.circuit.utils.fileType
import com.uyscuti.social.circuit.utils.formatFileSize
import com.uyscuti.social.circuit.utils.generateRandomFileName
import com.uyscuti.social.circuit.utils.generateRandomId
import com.uyscuti.social.circuit.utils.getFileNameFromLocalPath
import com.uyscuti.social.circuit.utils.isFileSizeGreaterThan2MB
import com.uyscuti.social.circuit.utils.uriToFile2
import com.uyscuti.social.circuit.viewmodels.feed.FeedUploadViewModel
import com.uyscuti.social.network.api.response.allFeedRepostsPost.Post
import com.uyscuti.social.network.api.response.allFeedRepostsPost.RepostRequest
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage
import dagger.hilt.android.AndroidEntryPoint
import id.zelory.compressor.Compressor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.relex.circleindicator.CircleIndicator3
import org.apache.poi.hwpf.HWPFDocument
import org.apache.poi.hwpf.usermodel.Range
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [NewRepostedPostFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
private const val TAG = "NewRepostedPostFragment"

@AndroidEntryPoint
class NewRepostedPostFragment(
    val data: com.uyscuti.social.network.api.response.posts.Post
) :
    Fragment(),
    MultipleImagesListener,
    FeedVideoThumbnailAdapter.ThumbnailClickListener, MultipleAudiosListener,
    AudioCompressorWithProgress.ProgressListener,
    MultipleVideosListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val postList:
            MutableList<Post> =
        mutableListOf()
    private var attachedMediaUris: MutableList<Uri> = mutableListOf()
    private lateinit var postAdapter: FeedAdapter
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var audioPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var videoPickerLauncher: ActivityResultLauncher<Intent>
    private var addMoreFeedFiles = true
    var fileType: String = ""
    private lateinit var repostUser: SharedPreferences
    private lateinit var backPressedCallback: OnBackPressedCallback
    private var adapter: FeedMixedFilesViewPagerAdapter? = null
    private lateinit var username: String
    private lateinit var avatar: String
    private val PREFS_NAME = "LocalSettings" // Change this to a unique name for your app
    private var caption = ""
    private var multipleFeedFilesPagerAdapter: MultipleFeedFilesPagerAdapter? = null
    private lateinit var feedUploadViewModel: FeedUploadViewModel
    private lateinit var attachmentFile: CardView
    private var compressedImageFile: File? = null
    private var compressedImageFiles: MutableList<File> = mutableListOf()
    private var isThumbnailClicked = false
    private lateinit var thumbnail: Bitmap
    private var imagesList = mutableListOf<String>()
    private var isMultipleImages = false
    private var imagePickLauncher: ActivityResultLauncher<Intent>? = null
    private lateinit var multipleAudioAdapter: MultipleFeedAudioAdapter
    private var audiosList = mutableListOf<MultipleAudios>()
    private var audioDurationStringList: MutableList<String> = mutableListOf()
    private var durationString = ""
    val audioPathList: MutableList<String> = mutableListOf()
    private var audioPath = ""
    private var videosList = mutableListOf<FeedMultipleVideos>()
    var text = ""

    private var uploadWorkRequest: OneTimeWorkRequest? = null
    private val REQUEST_CODE = 2024
    val tags: MutableList<String> = mutableListOf()

    private var documentUriListToUpload: MutableList<String> = mutableListOf()
    private var documentFileNamesToUpload: MutableList<String> = mutableListOf()
    private var documentNumberOfPagesToUpload: MutableList<String> = mutableListOf()
    private var documentTypesToUpload: MutableList<String> = mutableListOf()
    private var documentThumbnailsToUpload: MutableList<String> = mutableListOf()

    private var feedTextViewFragmentInterface: FeedTextViewFragmentInterface? = null

    private var fileName = ""
    private var docType = ""
    private var docFilePath = ""
    private var numberOfPages = ""
    val gson = createGson()
    private var position = 0

    private lateinit var multipleSelectedFeedVideoAdapter: MultipleSelectedFeedVideoAdapter

    private var permissionGranted = false
    private lateinit var mixedFilesUploadAdapter: MixedFilesUploadAdapter

    private var mediaPlayer: MediaPlayer? = null
    private var audioPlayingPosition = -1
    private var isPlaying = false
    private lateinit var sharedPreferences: SharedPreferences

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.READ_MEDIA_IMAGES
    )

    private var videoUris: MutableList<Uri> = mutableListOf()
    private var videoPaths: MutableList<String> = mutableListOf()

    private lateinit var context: Context

    @Inject
    lateinit var retrofitInstance: RetrofitInstance

    @Inject
    lateinit var localStorage: LocalStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            position = it.getInt("position")
            //data = (it.getSerializable("data") as
        // com.uyscut.network.api.response.allFeedRepostsPost. Post?)!!
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint(
        "SuspiciousIndentation", "CheckResult", "MissingInflatedId", "CutPasteId",
        "ResourceType"
    )
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        return inflater.inflate(
            R.layout.fragment_edit_post_to_repost, container, false)
    }

    @SuppressLint("CutPasteId")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val originalFeedImage = view.findViewById<ImageView>(R.id.originalFeedImage)
        val editTextText: EditText = view.findViewById(R.id.editTextText)
        val originalPostProfileImage: ImageView = view.findViewById(
            R.id.originalPostProfileImage)

        val originalPostUsername: TextView = view.findViewById(R.id.originalPostUsername)
        val originalFeedTextContent: TextView = view.findViewById(R.id.originalFeedTextContent)
        val userprofile: ImageView = view.findViewById(R.id.userprofile)
//        val usernameText: TextView = view.findViewById(R.id.usernameText)
        val backButton: ImageView = view.findViewById(R.id.backButton)
        val repostButton: AppCompatButton = view.findViewById(R.id.repostButton)
        val viewPager: ViewPager2 = view.findViewById(R.id.viewPagers)
//        val viewPager2 : ViewPager2 = view.findViewById(R.id.viewPagers2)
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView2)
        val recyclerView2: RecyclerView = view.findViewById(R.id.recyclerView)
        val mixedFilesCardView: CardView = view.findViewById(R.id.mixedFilesCardView)
        val addMoreFeed: ImageView = view.findViewById(R.id.addMoreFeed)
        val content: TextView = view.findViewById(R.id.content)

        val multipleImagesContainers: ConstraintLayout =
            view.findViewById(R.id.multipleImagesContainers)
        val mixedFilesCardViews: CardView = view.findViewById(R.id.mixedFilesCardViews)
        repostUser = requireActivity().getSharedPreferences(PREFS_NAME, 0)
        avatar = repostUser.getString("avatar", "").toString()
        sharedPreferences =
            requireActivity().getSharedPreferences(
                "RepostDrafts", Context.MODE_PRIVATE)
        feedUploadViewModel = ViewModelProvider(
            this)[FeedUploadViewModel::class.java]
        Glide.with(this)
            .load(avatar)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .placeholder(R.drawable.profilepic2)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(userprofile)

        val username = repostUser.getString("username", "").toString()
//        usernameText.text = username
        val userId = repostUser.getString("userId", "").toString()
        val imageList: MutableList<String> = mutableListOf()

        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d("backbutton", "has been clicked")
                if (feedTextViewFragmentInterface != null) {
                    Log.d("backbutton", "has been clicked")

                    feedTextViewFragmentInterface?.backPressedFromFeedTextViewFragment()
                }
            }
        }

        context = requireContext()

        mixedFilesUploadAdapter = MixedFilesUploadAdapter(
            context, this, this,
            multipleDocumentsListener = null
        )

        feedUploadViewModel.displayText.observe(viewLifecycleOwner) { text ->
            mixedFilesUploadAdapter = MixedFilesUploadAdapter(
                context, this, this,
                multipleDocumentsListener = null

            )
        }


        if (data.content == "" && data.content.isEmpty()) {
            Log.d("clicked", "render: original post text")
            originalFeedTextContent.visibility = View.GONE
        } else {
            originalFeedTextContent.visibility = View.GONE
            originalFeedTextContent.text = data.content
        }
        if (data.originalPost.isNotEmpty()) {

            originalFeedTextContent.text = data.originalPost[0].content
            originalFeedTextContent.visibility = View.VISIBLE

            mixedFilesCardView.visibility = View.GONE
        } else {
            originalFeedTextContent.visibility = View.GONE
//
            mixedFilesCardView.visibility = View.VISIBLE
        }

        if (data.originalPost.isNotEmpty()) {
            val fileList: MutableList<String> = mutableListOf()

            for (file in data.originalPost[0].files) {
                Log.d(TAG, "render: images ${file.url}")
                fileList.add(file.url)
            }
        } else {
            Log.d(TAG, "render: data files is empty")
        }


        /**Set the original post image (thumbnail)*/
        val fileList: MutableList<String> = mutableListOf()
        if (data.files.isNotEmpty()) {
            for (file in data.files) {
                Log.d(TAG, "render: images ${file.url}")
                fileList.add(file.url)
            }
        } else {
            Log.d(TAG, "render: data files is empty")
        }

        try {
            when (data.contentType) {

                "text" -> {
                    if (data.content.isNotEmpty()) {
                        Log.d("clicked", "render: original post text")
                        originalFeedTextContent.visibility = View.VISIBLE
                        originalFeedTextContent.text = data.content
                        mixedFilesCardView.visibility = View.GONE

                    } else {
                        originalFeedTextContent.visibility = View.GONE
                        mixedFilesCardView.visibility = View.VISIBLE
                    }
                }

                "mixed_files" -> {

                    Log.d("clicked", "render: original post mixed files")
                    mixedFilesCardView.visibility = View.VISIBLE
                    if (data.files.isNotEmpty()) {
                        Log.d("clicked", "render: data files are empty")
                        originalFeedTextContent.visibility = View.VISIBLE
                        mixedFilesCardView.visibility = View.VISIBLE
                        recyclerView.visibility = View.VISIBLE
                    } else {
                        originalFeedTextContent.visibility = View.GONE
                        mixedFilesCardView.visibility = View.GONE
                    }
                    // Display original post text if available
                    if (data.content.isNotEmpty()) {
                        originalFeedTextContent.visibility = View.VISIBLE
                        originalFeedTextContent.text = data.content
                    } else {
                        originalFeedTextContent.visibility = View.GONE
                    }

                    if (data.originalPost.isNotEmpty()) {
                        val originalPost = data.originalPost[0]
                        val imageUrls = originalPost.files.map { it.url } // Convert List<File> → List<String>
                        val adapter = FeedRepostViewFileAdapter(imageUrls, originalPost)
                        recyclerView.adapter = adapter
                    } else {
                        Log.e(TAG, "No original post available to create the adapter")
                    }



                    when (fileList.size) {
                        1 -> {
                            recyclerView.layoutManager = GridLayoutManager(
                                requireContext(), 1)
                            recyclerView.setHasFixedSize(true)
                            // Ensures items won't change size
                            recyclerView.adapter = adapter

                            recyclerView2.layoutManager = GridLayoutManager(
                                requireContext(), 1)
                            recyclerView2.setHasFixedSize(true)
                            // Ensures items won't change size
                            recyclerView2.adapter = adapter
                        }

                        2 -> {
                            recyclerView.layoutManager =
                                GridLayoutManager(
                                    requireContext(), 2) // 2 columns in the grid
                            recyclerView.setHasFixedSize(true)
                            // Ensures items won't change size
                            recyclerView.adapter = adapter // Replace with your adapter

                            recyclerView2.layoutManager =
                                GridLayoutManager(
                                    requireContext(), 2) // 2 columns in the grid
                            recyclerView2.setHasFixedSize(
                                true) // Ensures items won't change size
                            recyclerView2.adapter = adapter // Replace with your adapter
                        }

                        3 -> {
                            // Use a GridLayoutManager with span size 2
                            // for the first row and 1 for the second row
                            val layoutManager = GridLayoutManager(
                                requireContext(), 2)
                            layoutManager.spanSizeLookup =
                                object : GridLayoutManager.SpanSizeLookup() {
                                    override fun getSpanSize(position: Int): Int {
                                        return if (position < 2) 1 else 2
                                    }
                                }
                            recyclerView.layoutManager = layoutManager
                            // Ensures items won't change size
                            recyclerView.setHasFixedSize(true)
                            recyclerView.adapter = adapter // Replace with your adapter


                            recyclerView2.layoutManager = layoutManager
                            // Ensures items won't change size
                            recyclerView2.setHasFixedSize(true)
                            recyclerView2.adapter = adapter // Replace with your adapter
                        }

                        else -> {
                            val layoutManager = GridLayoutManager(
                                requireContext(), 2)

                            layoutManager.spanSizeLookup =
                                object : GridLayoutManager.SpanSizeLookup() {
                                    override fun getSpanSize(position: Int): Int {
                                        return when (position) {
                                            0, 1 -> 1  // First and second items span 2 columns
                                            else -> 1   // All other items span 1 column
                                        }
                                    }
                                }
                            recyclerView.layoutManager = layoutManager
                            recyclerView.adapter = adapter

                            recyclerView2.layoutManager = layoutManager
                            recyclerView2.adapter = adapter
                        }
                    }
                }

                "image" -> {
                    if (data.files.isNotEmpty()) {
                        Log.d("clicked", "render: image content")
                        originalFeedTextContent.visibility = View.VISIBLE
                        originalFeedTextContent.text = data.originalPost[0].content
                        originalFeedImage.visibility = View.VISIBLE

                        Glide.with(context)

                            // Assuming the image URL is in `files[0].url`
                            .load(data.files[0].url)
                            .placeholder(R.drawable.flash21)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(originalFeedImage)

                    } else {
                        Log.d("clicked", "render: image content")
                        originalFeedTextContent.visibility = View.GONE
                    }
                }

                "video" -> {
                    Log.d("clicked", "render: video content")
                    // Hide other views
                    if (data.files.isNotEmpty()) {
                        originalFeedTextContent.visibility = View.VISIBLE
                        mixedFilesCardView.visibility = View.VISIBLE

                    } else {
                        originalFeedTextContent.visibility = View.GONE
                        mixedFilesCardView.visibility = View.GONE

                    }
                }

                else -> {
                }
            }
        } catch (e: Exception) {
            Log.d("Exception", "onCreate: ${e.message}")
        }

        /**buttons added*/
        addMoreFeed.setOnClickListener {
            val mixedFeedFiles = feedUploadViewModel.getMixedFeedUploadDataClass()
            if (mixedFeedFiles.size > 10) {
                Toast.makeText(context, "Select 10 files only",
                    Toast.LENGTH_SHORT).show()
            } else {
                Log.d(TAG, "onCreate: addMoreFeedFiles $addMoreFeedFiles")
                addMoreFeedFiles = true
                showAttachmentDialog()
            }
        }

        val shortThumbNail: ImageView = view.findViewById(R.id.shortThumbNail)
        val shortThumbNail2: ImageView = view.findViewById(R.id.shortThumbNails)
        shortThumbNail2.setOnClickListener {

            ImagePicker.with(
                this).cropSquare().compress(
                512).maxResultSize(512, 512)
                .createIntent { intent: Intent ->
                    imagePickLauncher?.launch(intent)
                }
        }
        val subTopicsTwo: ImageView = view.findViewById(R.id.subTopicstwo)
        subTopicsTwo.setOnClickListener {
            Log.d("subTopics", "subTopics clicked")
            val intent = Intent(requireContext(), TopicsActivity::class.java)
            startActivityForResult(intent,
                UploadShortsActivity.REQUEST_TOPICS_ACTIVITY)
        }


        // Set click listeners
        repostButton.setOnClickListener {
            repostsFeed() // Your repost function
            Toast.makeText(requireContext(), "Reposted successfully!", Toast.LENGTH_SHORT).show()

            Handler(Looper.getMainLooper()).postDelayed({
                findNavController().popBackStack()
            }, 500) // 0.5 second delay
        }

        backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    



        activity?.window?.navigationBarColor =
            ContextCompat.getColor(requireContext(), R.color.black)
        attachmentFile = view.findViewById(R.id.shortVideoThumbNail)


        attachmentFile.setOnClickListener {
            showAttachmentDialog()
            addMoreFeedFiles = true
        }
        imagePickLauncher =
            registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    setAddMoreFeedVisible()
                    val data = result.data
                    if (data != null && data.data != null) {
                        val selectedImageUri: Uri = data.data!!
                        Log.d("imagepicker", "onActivityResult: $selectedImageUri")
                        isThumbnailClicked = true
                        // Load the bitmap from the URI
                        val bitmap = loadBitmapFromUri(selectedImageUri)
                        shortThumbNail.colorFilter = null
                        shortThumbNail.setPadding(0)
                        Glide.with(this)
                            .load(selectedImageUri)
                            .into(shortThumbNail)
                        thumbnail = bitmap

                    }
                }
            }

        videoPickerLauncher =
            registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()) { result ->
                Log.d("VideoDebug", "onActivityResult callback triggered")
                if (result.resultCode == RESULT_OK) {
                    setAddMoreFeedVisible()
                    val newVideosList: MutableList<FeedMultipleVideos> = mutableListOf()
                    fileType = "mixed_files"
                    val data = result.data

                    if (!addMoreFeedFiles) {
                        videosList.clear()
                    }

                    val videoPaths = data?.getStringArrayListExtra("video_url")
                    val uriString = data?.getStringArrayListExtra("vUri")

                    val videoUri: MutableList<Uri> = mutableListOf()
                    val videoPathList: MutableList<String> = mutableListOf()
                    if (uriString != null && videoPaths != null) {

                        feedUploadViewModel.mixedFilesCount += videoPaths.size
                        Log.d(
                            TAG,
                            "feedUploadViewModel:  mixedFilesCount = " +
                                    "${feedUploadViewModel.mixedFilesCount}"
                        )
                        for (uri in uriString) {
                            val vUri = Uri.parse(uri)
                            videoUri.add(vUri)


                            Log.d(
                                "videoThumbnail",
                                "onCreate: Uri $uri videoUri $videoUri vUri $vUri"
                            )
                        }
                        for (i in videoPaths.indices) {
                            val videoPath = videoPaths[i]
                            val videoPathUri = videoUri[i]

                            Log.d("videoThumbnail", "Video Path: $videoPath")

                            videoPathList.add(videoPath)

                            // Get additional information
                            val durationString = getFormattedDuration(videoPath)
                            val fileName = getFileNameFromLocalPath(videoPath)

                            val videoThumbnail = getFirstFrameAsThumbnail(videoUri[i])
                            Log.d("videoThumbnail",
                                "onCreate videoThumbnail: $videoThumbnail")
                            val videoItem = FeedMultipleVideos(
                                videoPath,
                                durationString,
                                fileName,
                                videoPathUri.toString(),
                                videoThumbnail,

                            )
                            videosList.add(videoItem)
                            newVideosList.add(videoItem)

                        }
                    }


                    Log.d(
                        TAG,
                        "onCreate: videoUri.size " +
                                "${videoUri.size} videoPathList.size ${videoPathList.size}"
                    )
                    videoUris.addAll(videoUri)
                    this.videoPaths.addAll(videoPathList)

                    // Setup ViewPager2 with the adapter
                    val arrayList: ArrayList<FeedMultipleVideos> = ArrayList(videosList)
                    if (addMoreFeedFiles) {

                        multipleFeedFilesPagerAdapter =
                            MultipleFeedFilesPagerAdapter(
                                requireActivity(),

                                isFullScreen = true
                            )
                        viewPager.adapter = multipleFeedFilesPagerAdapter

                        for (video in newVideosList) {
                            feedUploadViewModel.addMixedFeedUploadDataClass(
                                MixedFeedUploadDataClass(
                                    videos = video, fileTypes = "video"
                                )
                            )

                        }

                        val mixedFeedFiles = feedUploadViewModel.getMixedFeedUploadDataClass()
                        multipleFeedFilesPagerAdapter?.setMixedFeedUploadDataClass(
                            mixedFeedFiles
                        )

                    } else {

                        for (video in newVideosList) {
                            feedUploadViewModel.addMixedFeedUploadDataClass(
                                MixedFeedUploadDataClass(
                                    videos = video, fileTypes = "video"
                                )
                            )
                        }
                        multipleSelectedFeedVideoAdapter =
                            MultipleSelectedFeedVideoAdapter(
                                context, arrayList, this)
                        viewPager.adapter = multipleSelectedFeedVideoAdapter
                    }

                    viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
                    viewPager.registerOnPageChangeCallback(object :
                        ViewPager2.OnPageChangeCallback() {
                        override fun onPageScrolled(
                            position: Int,
                            positionOffset: Float,
                            positionOffsetPixels: Int
                        ) {

                        }

                        @SuppressLint("SetTextI18n")
                        override fun onPageSelected(position: Int) {

                            Log.d("ViewPager2", "Page selected: $position")
                            content.visibility = View.VISIBLE
                            if (!addMoreFeedFiles) {

                                val videoDetails =
                                    multipleSelectedFeedVideoAdapter.getVideoDetails(position)
                                Log.d("ViewPager2",
                                    "Page selected videoDetails: $videoDetails")
                                val handler = Handler(Looper.getMainLooper())
                                handler.postDelayed({
                                    Log.d(
                                        "ViewPager2",
                                        "File name:" +
                                                " ${videoDetails.fileName} Duration: " +
                                                "${videoDetails.videoDuration}"
                                    )
                                    content.text =
                                        "File name:" +
                                                " ${videoDetails.fileName} " +
                                                "\nDuration: ${videoDetails.videoDuration}"
                                }, 500)

                                lifecycleScope.launch(Dispatchers.IO) {
                                    val videoThumbnails =
                                        extractThumbnailsFromVideos(
                                            videoDetails.videoUri.toUri())

                                    // Switch to the main thread to update the RecyclerView
                                    withContext(Dispatchers.Main) {
                                        setupRecyclerView(videoThumbnails, videoDetails)
                                    }
                                }
                            } else {


                                val videoDetails =
                                    multipleFeedFilesPagerAdapter?.getVideoDetails(position)
                                Log.d("ViewPager2", "Page selected videoDetails:" +
                                        " $videoDetails")
                                content.visibility = View.VISIBLE

                                if (videoDetails == null) {
                                } else {
                                    content.visibility = View.VISIBLE
                                }
                                val handler = Handler(Looper.getMainLooper())
                                handler.postDelayed({

                                    if (videoDetails != null) {
                                        content.text =
                                            "File name: ${videoDetails.fileName} \nDuration: " +
                                                    "${videoDetails.videoDuration}"
                                    }
                                }, 500)

                                lifecycleScope.launch(Dispatchers.IO) {
                                    val videoThumbnails =
                                        videoDetails?.let {
                                            extractThumbnailsFromVideos(
                                                it.videoUri.toUri()) }

                                    // Switch to the main thread to update the RecyclerView
                                    withContext(Dispatchers.Main) {
                                        if (videoDetails != null) {
                                            if (videoThumbnails != null) {
                                                content.visibility = View.VISIBLE
                                                setupRecyclerView(videoThumbnails, videoDetails)
                                            }
                                        }
                                    }
                                }
                            }

                        }

                        override fun onPageScrollStateChanged(state: Int) {

                            when (state) {
                                ViewPager2.SCROLL_STATE_IDLE -> {

                                }

                                ViewPager2.SCROLL_STATE_DRAGGING -> {

                                }

                                ViewPager2.SCROLL_STATE_SETTLING -> {

                                }
                            }
                        }
                    })

                    // Ensure visibility settings are correct
                    recyclerView2.visibility = View.INVISIBLE
                    multipleImagesContainers.visibility = View.VISIBLE
                    shortThumbNail.visibility = View.GONE

                }
            }

        cameraLauncher =
            registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    // Handle image selection result here
                    val data = result.data
                    // Process the selected image data
                    fileType = "mixed_files"
                    val imagePath = data?.getStringExtra("image_url")
                    Log.d(
                        "cameraLauncher", "Selected image path from camera: $imagePath"
                    )
                    val imageUri = Uri.parse(imagePath)
                    Log.d(
                        "cameraLauncher", "Selected image path from camera: $imageUri"
                    )
                    if (imagePath != null) {

                        setAddMoreFeedVisible()
                        val file = File(imagePath)
                        if (file.exists()) {
                            lifecycleScope.launch {
                                val compressedImageFile =
                                    Compressor.compress(context, file)
                                Log.d(
                                    "cameraLauncher",
                                    "cameraLauncher: compressedImageFile absolutePath: " +
                                            "${compressedImageFile.absolutePath}"
                                )
                                val fileSizeInBytes = compressedImageFile.length()
                                val fileSizeInKB = fileSizeInBytes / 1024
                                val fileSizeInMB = fileSizeInKB / 1024

                                this@NewRepostedPostFragment.compressedImageFile =
                                    compressedImageFile
                                Log.d(
                                    "cameraLauncher",
                                    "cameraLauncher: compressedImageFile size " +
                                            "$fileSizeInKB KB," +
                                            " $fileSizeInMB MB addMoreFeedFiles" +
                                            " $addMoreFeedFiles"
                                )

                                feedUploadViewModel.addMixedFeedUploadDataClass(
                                    MixedFeedUploadDataClass(


                                        images = FeedMultipleImages(
                                            imagePath = imagePath,
                                            compressedImagePath = compressedImageFile.absolutePath
                                        ),
                                        fileTypes = "image"
                                    )
                                )
                                if (addMoreFeedFiles) {
                                    multipleFeedFilesPagerAdapter =
                                        MultipleFeedFilesPagerAdapter(
                                            requireActivity(),

                                            isFullScreen = true
                                        )
                                    viewPager.adapter = multipleFeedFilesPagerAdapter


                                    val mixedFeedFiles =
                                        feedUploadViewModel.getMixedFeedUploadDataClass()

                                    multipleFeedFilesPagerAdapter?.setMixedFeedUploadDataClass(
                                        mixedFeedFiles
                                    )
                                    viewPager.orientation =
                                        ViewPager2.ORIENTATION_HORIZONTAL

                                    // Setup CircleIndicator for ViewPager2
                                    val indicator =
                                        view.findViewById<CircleIndicator3>(
                                            R.id.circleIndicator)
                                    indicator.setViewPager(viewPager)
                                    viewPager.registerOnPageChangeCallback(object :
                                        ViewPager2.OnPageChangeCallback() {
                                        override fun onPageScrolled(
                                            position: Int,
                                            positionOffset: Float,
                                            positionOffsetPixels: Int
                                        ) {

                                        }

                                        override fun onPageSelected(position: Int) {
                                            // This method will be invoked when
                                            // a new page becomes selected.
                                            // You can perform actions here
                                            // based on the selected page position.
                                            Log.d("ViewPager2", "onPageSelected: $position")

                                            recyclerView2.visibility = View.INVISIBLE
                                            multipleImagesContainers.visibility = View.VISIBLE
                                            shortThumbNail.visibility = View.GONE
                                            content.text = ""
                                            content.visibility = View.GONE
                                        }

                                        override fun onPageScrollStateChanged(state: Int) {

                                            when (state) {
                                                ViewPager2.SCROLL_STATE_IDLE -> {

                                                }

                                                ViewPager2.SCROLL_STATE_DRAGGING -> {

                                                }

                                                ViewPager2.SCROLL_STATE_SETTLING -> {

                                                }
                                            }
                                        }
                                    })
                                    // Ensure visibility settings are correct
                                    recyclerView2.visibility = View.INVISIBLE
                                    multipleImagesContainers.visibility = View.VISIBLE
                                    shortThumbNail.visibility = View.GONE
//                                    binding.content.visibility = View.VISIBLE
                                } else {

                                    Glide.with(this@NewRepostedPostFragment)
                                        .load(compressedImageFile)
                                        .into(shortThumbNail)
                                }
//                                binding.content.text = ""
                                feedUploadViewModel.setText("")
                                recyclerView2.visibility = View.INVISIBLE
                                shortThumbNail.colorFilter = null
                                shortThumbNail.setPadding(0)
                            }
                        }

                    }
                }

            }


        audioPickerLauncher =
            registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    // Handle image selection result here
                    fileType = "mixed_files"
                    setAddMoreFeedVisible()
//                    content.visibility = View.VISIBLE
                    val data = result.data
                    // Process the selected image data
                    val audioPath = data?.getStringArrayListExtra("audio_url")

                    val audioToUpload: MutableList<String> = mutableListOf()
                    val feedMultipleAudios: MutableList<FeedMultipleAudios> = mutableListOf()

                    val audioDurationToUpload: MutableList<String> = mutableListOf()
                    if (audioPath != null) {
                        if (!addMoreFeedFiles) {
                            audiosList.clear()
                            audioPathList.clear()
                            audioDurationStringList.clear()
                        }

                        for (audioFilePath in audioPath) {
                            val durationString = getFormattedDuration(audioFilePath)
                            val fileName = getFileNameFromLocalPath(audioFilePath)


                            audioDurationStringList.add(durationString)
                            audioPathList.add(audioFilePath)
                            audiosList.add(MultipleAudios(
                                audioFilePath, durationString, fileName))
                            audioToUpload.add(audioFilePath)
//                            audioToUpload = audioFilePath
                            audioDurationToUpload.add(durationString)
                            feedMultipleAudios.add(
                                FeedMultipleAudios(
                                    durationString,
                                    audioFilePath,
                                    fileName
                                )
                            )
                        }



                        if (addMoreFeedFiles) {
                            Log.d("addMoreFeedFiles",
                                "onCreate: add more files $audioToUpload")
                            multipleFeedFilesPagerAdapter =
                                MultipleFeedFilesPagerAdapter(
                                    requireActivity(),

                                    isFullScreen = true
                                )
                            viewPager.adapter = multipleFeedFilesPagerAdapter

                            for (audio in feedMultipleAudios) {
                                Log.d(TAG, "onCreate: audio to upload $audio")
                                feedUploadViewModel.addMixedFeedUploadDataClass(
                                    MixedFeedUploadDataClass(

                                        audios = audio, fileTypes = "audio"
                                    )
                                )
                            }
                            val mixedFeedFiles = feedUploadViewModel.getMixedFeedUploadDataClass()
                            multipleFeedFilesPagerAdapter?.setMixedFeedUploadDataClass(
                                mixedFeedFiles
                            )


                        } else {
                            Log.d("addMoreFeedFiles", "onCreate: do not add more files")
                            // Setup ViewPager2 with the adapter
                            multipleAudioAdapter = MultipleFeedAudioAdapter(
                                requireContext(),
                                audiosList,
                                requireActivity() as UploadFeedActivity
                            )
                            viewPager.adapter = multipleAudioAdapter

                            for (audio in feedMultipleAudios) {
                                Log.d(TAG, "onCreate: audio to upload $audio")
                                feedUploadViewModel.addMixedFeedUploadDataClass(
                                    MixedFeedUploadDataClass(
//                                        videos = video
                                        audios = audio, fileTypes = "audio"
                                    )
                                )
                            }
                        }

                        viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
                        viewPager.registerOnPageChangeCallback(object :
                            ViewPager2.OnPageChangeCallback() {
                            override fun onPageScrolled(
                                position: Int,
                                positionOffset: Float,
                                positionOffsetPixels: Int
                            ) {

                            }

                            override fun onPageSelected(position: Int) {
                                // This method will be invoked when a new page becomes selected.
                                // You can perform actions here based on the selected page position.
                                Log.d("ViewPager2", "Page selected: $position")
//                                content.visibility = View.VISIBLE
                                if (!addMoreFeedFiles) {
                                    val audioDetails =
                                        multipleAudioAdapter.getAudioDetails(position)
                                    Log.d("ViewPager2",
                                        "Page selected audioDetails: $audioDetails")

                                    val handler = Handler(Looper.getMainLooper())
                                    handler.postDelayed({


                                    }, 500)
                                } else {

                                    val audioDetails =
                                        multipleFeedFilesPagerAdapter?.getAudioDetails(position)
                                    Log.d(
                                        "ViewPager2",
                                        "onPageSelected: get audio details $audioDetails"
                                    )


                                    recyclerView2.visibility = View.INVISIBLE
//                                    content.visibility = View.VISIBLE
                                    val handler = Handler(Looper.getMainLooper())
                                    handler.postDelayed({
                                        if (audioDetails != null) {


                                        }
                                    }, 500)
                                }


                            }

                            override fun onPageScrollStateChanged(state: Int) {
                                // Called when the scroll state changes:


                                when (state) {
                                    ViewPager2.SCROLL_STATE_IDLE -> {
                                        // The pager is in an idle, settled state.
                                        Log.d("ViewPager2",
                                            "Page selected: SCROLL_STATE_IDLE")
                                    }

                                    ViewPager2.SCROLL_STATE_DRAGGING -> {
                                        // The user is dragging the pager.
                                        Log.d("ViewPager2",
                                            "Page selected: SCROLL_STATE_DRAGGING")
                                    }

                                    ViewPager2.SCROLL_STATE_SETTLING -> {
                                        // The pager is settling to a final position.
                                        Log.d("ViewPager2",
                                            "Page selected: SCROLL_STATE_SETTLING")
                                    }
                                }
                            }
                        })
                        // Setup CircleIndicator for ViewPager2
                        val indicator = view.findViewById<CircleIndicator3>(R.id.circleIndicator)
                        indicator.setViewPager(viewPager)
                        // Ensure visibility settings are correct
                        recyclerView2.visibility = View.INVISIBLE
                        multipleImagesContainers.visibility = View.VISIBLE
                        shortThumbNail.visibility = View.GONE


                    }
                }
            }
        viewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }



            override fun onPageSelected(position: Int) {
                // This method will be invoked when a new page becomes selected.
                // You can perform actions here based on the selected page position.
                Log.d("ViewPager2", "Page selected: $position")
                if (!addMoreFeedFiles) {
                    val audioDetails =
                        multipleAudioAdapter.getAudioDetails(position)
                    Log.d("ViewPager2", "Page selected audioDetails: $audioDetails")

                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed({

                    }, 500)
                } else {

                    val audioDetails =
                        multipleFeedFilesPagerAdapter?.getAudioDetails(position)
                    Log.d(
                        "ViewPager2",
                        "onPageSelected: get audio details $audioDetails"
                    )

                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed({
                        if (audioDetails != null) {

                        }
                    }, 500)
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
                // Called when the scroll state changes:

                when (state) {
                    ViewPager2.SCROLL_STATE_IDLE -> {
                        // The pager is in an idle, settled state.
                        Log.d("ViewPager2", "Page selected: SCROLL_STATE_IDLE")
                    }

                    ViewPager2.SCROLL_STATE_DRAGGING -> {
                        // The user is dragging the pager.
                        Log.d("ViewPager2", "Page selected: SCROLL_STATE_DRAGGING")
                    }

                    ViewPager2.SCROLL_STATE_SETTLING -> {
                        // The pager is settling to a final position.
                        Log.d("ViewPager2", "Page selected: SCROLL_STATE_SETTLING")
                    }
                }
            }
        })
        // Setup CircleIndicator for ViewPager2
        val indicator = view.findViewById<CircleIndicator3>(R.id.circleIndicator)
        indicator.setViewPager(viewPager)
        // Ensure visibility settings are correct
        recyclerView2.visibility = View.INVISIBLE
        multipleImagesContainers.visibility = View.VISIBLE
        shortThumbNail.visibility = View.VISIBLE
    }

    private fun getAudioListener(): UploadFeedActivity {
        return requireActivity() as UploadFeedActivity
    }

    private val pickMultipleMedia =
        registerForActivityResult(
            ActivityResultContracts.PickMultipleVisualMedia(10)) { uris ->
            // Callback is invoked after the user selects media items or closes the
            // photo picker.
            setAddMoreFeedVisible()

            val viewPager2 = view?.findViewById<ViewPager2>(R.id.viewPager2)
            val recyclerView2 = view?.findViewById<RecyclerView>(R.id.recyclerView)
            val multipleImagesContainers =
                view?.findViewById<ConstraintLayout>(R.id.multipleImagesContainers)
            val shortThumbNail = view?.findViewById<ImageView>(R.id.shortThumbNail)

            val newImagesList: MutableList<String> = mutableListOf()
            val newCompressedImageFiles: MutableList<File> = mutableListOf()
            if (uris.isNotEmpty()) {
                if (!addMoreFeedFiles) {
                    imagesList.clear()
                }
                if (uris.isNotEmpty()) {
                    Log.d(TAG, "selected more than 1 image: ${uris.size}")
                    isMultipleImages = true

                    feedUploadViewModel.mixedFilesCount += uris.size

                    Log.d(
                        TAG,
                        "feedUploadViewModel:  mixedFilesCount = " +
                                "${feedUploadViewModel.mixedFilesCount}"
                    )
                    for (uri in uris) {
                        val filePath = PathUtil.getPath(
                            requireContext(),
                            uri
                        ) // Use the utility class to get the real file path
                        Log.d("PhotoPicker", "File path: $filePath")
                        Log.d("PhotoPicker", "Selected image path from camera: $uri")
                        if (filePath != null) {
                            imagesList.add(filePath)
                            newImagesList.add(filePath)
                            val file = File(filePath)
                            lifecycleScope.launch {
                                val compressedImageFile =
                                    Compressor.compress(context, file)
                                Log.d(
                                    "PhotoPicker",
                                    "PhotoPicker: compressedImageFile absolutePath: " +
                                            "${compressedImageFile.absolutePath}"
                                )


                                val fileSizeInBytes = compressedImageFile.length()
                                val fileSizeInKB = fileSizeInBytes / 1024
                                val fileSizeInMB = fileSizeInKB / 1024

                                Log.d(
                                    "PhotoPicker",
                                    "PhotoPicker: compressedImageFile size" +
                                            " $fileSizeInKB KB, $fileSizeInMB MB"
                                )
                                compressedImageFiles.add(compressedImageFile)
                                newCompressedImageFiles.add(compressedImageFile)

                                multipleFeedFilesPagerAdapter =
                                    MultipleFeedFilesPagerAdapter(
                                        requireActivity(),

                                        isFullScreen = true
                                    )

                                viewPager2!!.adapter = multipleFeedFilesPagerAdapter
                                val compressedImagePath =
                                    compressedImageFile.absolutePath
                                val fileType = File(compressedImagePath).extension
                                Log.d(
                                    "newCompressedImageFiles",
                                    "newCompressedImageFiles: extension:" +
                                            " $fileType file path: $compressedImagePath"
                                )

                                feedUploadViewModel.addMixedFeedUploadDataClass(
                                    MixedFeedUploadDataClass(
                                        images = FeedMultipleImages(
                                            imagePath = filePath,
                                            compressedImagePath = compressedImagePath
                                        ), fileTypes = "image"
                                    )
                                )

                                val mixedFeedFiles =
                                    feedUploadViewModel.getMixedFeedUploadDataClass()

                                multipleFeedFilesPagerAdapter?.setMixedFeedUploadDataClass(
                                    mixedFeedFiles
                                )

                                this@NewRepostedPostFragment.fileType = "mixed_files"

                                viewPager2.orientation = ViewPager2.ORIENTATION_HORIZONTAL

                                // Setup CircleIndicator for ViewPager2
                                val indicator =
                                    view?.findViewById<CircleIndicator3>(R.id.circleIndicator)
                                indicator!!.setViewPager(viewPager2)
                                viewPager2.registerOnPageChangeCallback(object :
                                    ViewPager2.OnPageChangeCallback() {
                                    override fun onPageScrolled(
                                        position: Int,
                                        positionOffset: Float,
                                        positionOffsetPixels: Int
                                    ) {
                                        }

                                    override fun onPageSelected(position: Int) {
                                        // This method will be invoked
                                        // when a new page becomes selected.
                                        // You can perform actions here
                                        // based on the selected page position.
                                        Log.d("ViewPager2", "onPageSelected: $position")

                                        recyclerView2!!.visibility = View.INVISIBLE
                                        multipleImagesContainers!!.visibility = View.VISIBLE
                                        shortThumbNail!!.visibility = View.GONE


                                    }

                                    override fun onPageScrollStateChanged(state: Int) {
                                        // Called when the scroll state changes:

                                        when (state) {
                                            ViewPager2.SCROLL_STATE_IDLE -> {

                                            }

                                            ViewPager2.SCROLL_STATE_DRAGGING -> {

                                            }

                                            ViewPager2.SCROLL_STATE_SETTLING -> {

                                            }
                                        }
                                    }
                                })
                                // Ensure visibility settings are correct
                                recyclerView2!!.visibility = View.INVISIBLE
                                multipleImagesContainers!!.visibility = View.VISIBLE
                                shortThumbNail!!.visibility = View.GONE

                            }
                        }


                    }

                }
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

    private fun saveDraftFeed() {
        val editTextText: EditText = requireView().findViewById(R.id.editTextText)
        val text = editTextText.text.toString().trim()
        with(sharedPreferences.edit()) {
            if (text.isNotEmpty() || attachedMediaUris.isNotEmpty() || postList.isNotEmpty()) {
                val gson = Gson()
                // Save user's comment
                putString("draft_text", text)
                putLong("draft_timestamp", System.currentTimeMillis())
                // Save original post details
                putString("original_post", gson.toJson(postList))
                // Save user's attached media URIs
                if (attachedMediaUris.isNotEmpty()) {
                    putString(
                        "draft_media_uris",
                        gson.toJson(attachedMediaUris.map { it.toString() })
                    )
                }
            } else {

            }
            apply()
        }

    }

    private fun getFirstFrameAsThumbnail(videoUri: Uri): Bitmap? {
        Log.d("getFirstFrameAsThumbnail", "getFirstFrameAsThumbnail: ")
        return try {
            val firstFrame: Bitmap? = VideoUtils.getFirstFrame(context, videoUri)
            if (firstFrame != null) {
                Log.d(TAG, "getFirstFrameAsThumbnail: thumbnail not null")
                return firstFrame // Return the first non-null frame immediately
            } else {
                Log.d(TAG, "getFirstFrameAsThumbnail: thumbnail is null")
            }
            // No frames were found; return null
            Log.d(TAG, "getFirstFrameAsThumbnail: No valid thumbnails found")
            null
        } catch (e: Exception) {
            Log.e(TAG, "getFirstFrameAsThumbnail: ${e.message}")
            e.printStackTrace()
            null // Return null in case of an exception
        }
    }

    private fun loadBitmapFromUri(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT < 28) {
            // For versions before Android 9 (API level 28)
            MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
        } else {
            // For Android 9 (API level 28) and above
            val source = ImageDecoder.createSource(requireActivity().contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        }
    }


    private fun setAddMoreFeedVisible() {
        val addMoreFeed: ImageView = requireView().findViewById(R.id.addMoreFeed)
        addMoreFeed.visibility = View.VISIBLE
    }


    @SuppressLint("CommitTransaction")
    private fun repostsFeed() {
        val editTextText: EditText = requireView().findViewById(R.id.editTextText)
        val caption = editTextText.text.toString()
        Log.d(TAG, "repostsFeed: caption $caption")
        editTextText.setText(caption)
        this.caption = caption
        Log.d(TAG, "repostsFeed: data _id ${data._id}")
        // If you have additional logic for mixed files or text uploads, it remains unchanged here.
        // Call the function to repost and dismiss the fragment
        repostAndDismiss()

        if (fileType == "mixed_files") {
            val mixedFeedFiles = feedUploadViewModel.getMixedFeedUploadDataClass()
            Log.d(TAG, "onCreate: send mixed files $mixedFeedFiles")

            val resultIntent = Intent()

            if (text.isNotEmpty()) {
                val tagsArray = ArrayList<String>()
                tagsArray.add(tags.toString())
                val uploadDataJson = gson.toJson(mixedFeedFiles)
                resultIntent.putExtra("mixedFiles", uploadDataJson)
                resultIntent.putExtra("caption", text)
                resultIntent.putExtra("tags", tags.toString())
                resultIntent.putExtra("contentType", "mixed_files")

                uploadMixedFeed(
                    mixedFeedFiles,
                    text,
                    tags
                )
            } else {
                val tagsArray = ArrayList<String>()
                tagsArray.add(tags.toString())
                val uploadDataJson = gson.toJson(mixedFeedFiles)
                resultIntent.putExtra("mixedFiles", uploadDataJson)
                resultIntent.putExtra("caption", caption)

                resultIntent.putExtra("tags", tags.toString())
                resultIntent.putExtra("contentType", "mixed_files")

                uploadMixedFeed(
                    mixedFeedFiles,
                    caption,
                    tags
                )
            }
        } else {
            Log.d(TAG, "onCreate: Lets upload some text")
//                    resultIntent.putExtra("tags", tags.toString())
            val resultIntent = Intent()
            resultIntent.putExtra("tags", tags.toString())
            resultIntent.putExtra("contentType", "text")

            if (text.isNotEmpty()) {
                resultIntent.putExtra("caption", text)

//                    Log.d(TAG, "I have found this $text and tags $tags")
                feedUploadViewModel.uploadTextFeed(
                    text, "text", tags,
                    onSuccess = { data ->
                        // Handle the successful data
                        Log.d(TAG, "Data received: $data")
                        EventBus.getDefault().post(FeedUploadResponseEvent(data._id))
                        Log.i(TAG, "onCreate: after event bus")
                    },
                    onError = { errorMessage ->
                        // Handle the error
                        Log.e(TAG, "Error occurred: $errorMessage")
                        // Show an error message to the user or log it
                    }
                )
            } else {
                resultIntent.putExtra("caption", caption)

//                    Log.d(TAG, "I have found this $caption and tags $tags")
                feedUploadViewModel.uploadTextFeed(
                    caption, "text", tags,
                    onSuccess = { data ->
                        // Handle the successful data
                        Log.d(TAG, "Data received: $data")
                        EventBus.getDefault().post(FeedUploadResponseEvent(data._id))
                        Log.i(TAG, "onCreate: after event bus2")
                    },
                    onError = { errorMessage ->
                        // Handle the error
                        Log.e(TAG, "Error occurred: $errorMessage")
                        // Show an error message to the user or log it
                    }
                )
            }


        }
    }

    private fun repostAndDismiss() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Create the request body
                val repostRequest = RepostRequest(
                    isReposted = true,
                    comment = "", // Add comment if needed
                    files = null, // Optional
                    tags = null // Optional
                )

                val response = retrofitInstance.apiService.repostsFeed(
                    postId = data._id,
                    request = repostRequest // Add the missing request parameter
                )

                val responseBody = response.body()
                Log.d(
                    "repostsFeed",
                    "Feed Feed repostsFeed feed: response body message " +
                            "${responseBody!!.message}"
                )
                val responseData = responseBody.data
                Log.d("repostsFeed",
                    "Feed Feed repostsFeed feed: response body data ${responseData}")

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        // Add success handling here
                        Toast.makeText(requireContext(),
                            "Reposted successfully", Toast.LENGTH_SHORT).show()
                        // Dismiss dialog or navigate back
                        // dismiss() // if this is in a dialog fragment
                        // or requireActivity().onBackPressed() // if you want to go back
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(),
                        "Error reposting", Toast.LENGTH_SHORT).show()
                }
                Log.e(TAG, "repost error: $e")
                Log.e(TAG, "repost error message: ${e.message}")
            }
        }
    }


    private fun createGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(Uri::class.java, UriTypeAdapter())
            .create()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)


    /// Private....
    override fun showAttachmentDialog() {
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(R.layout.shorts_and_all_feed_file_upload_bottom_dialog)
        val video = dialog.findViewById<LinearLayout>(R.id.upload_video)
        val audio = dialog.findViewById<LinearLayout>(R.id.upload_audio)
        val image = dialog.findViewById<LinearLayout>(R.id.upload_image)
        val camera = dialog.findViewById<LinearLayout>(R.id.open_camera)
        val doc = dialog.findViewById<LinearLayout>(R.id.upload_document)
        val location = dialog.findViewById<LinearLayout>(R.id.share_location)
        val vnRecord = dialog.findViewById<LinearLayout>(R.id.vnRecord)


        val selectableItemBackground = TypedValue()
        image?.context?.theme?.resolveAttribute(
            android.R.attr.selectableItemBackground,
            selectableItemBackground, true
        )
        image?.setBackgroundResource(selectableItemBackground.resourceId)


        video?.context?.theme?.resolveAttribute(
            android.R.attr.selectableItemBackground,
            selectableItemBackground, true
        )
        video?.setBackgroundResource(selectableItemBackground.resourceId)


        audio?.context?.theme?.resolveAttribute(
            android.R.attr.selectableItemBackground,
            selectableItemBackground, true
        )
        audio?.setBackgroundResource(selectableItemBackground.resourceId)


        camera?.context?.theme?.resolveAttribute(
            android.R.attr.selectableItemBackground,
            selectableItemBackground, true
        )
        camera?.setBackgroundResource(selectableItemBackground.resourceId)


        doc?.context?.theme?.resolveAttribute(
            android.R.attr.selectableItemBackground,
            selectableItemBackground, true
        )
        doc?.setBackgroundResource(selectableItemBackground.resourceId)

        location?.context?.theme?.resolveAttribute(
            android.R.attr.selectableItemBackground,
            selectableItemBackground, true
        )
        location?.setBackgroundResource(selectableItemBackground.resourceId)

        vnRecord?.context?.theme?.resolveAttribute(
            android.R.attr.selectableItemBackground,
            selectableItemBackground, true
        )
        vnRecord?.setBackgroundResource(selectableItemBackground.resourceId)

        image?.setOnClickListener {
            Log.d("SelectImage", "Image selector button clicked")
            pickMultipleMedia.launch(PickVisualMediaRequest(
                ActivityResultContracts.PickVisualMedia.ImageOnly))
            dialog.dismiss()

        }

        video?.setOnClickListener {
            Log.d("Selectvideo", "Image selector button clicked")

            val intent = Intent(requireContext(),
                FeedSelectVideoActivity::class.java)
            videoPickerLauncher.launch(intent)
            dialog.dismiss()

        }

        audio?.setOnClickListener {
            Log.d("Selectaudio", "Image selector button clicked")

            val intent = Intent(requireContext(),
                FeedAudioActivity::class.java)
            dialog.dismiss()
            audioPickerLauncher.launch(intent)

        }

        doc?.setOnClickListener {
            dialog.dismiss()
        }

        camera?.setOnClickListener {
            Log.d("cameraSelected", "the camera is selected")
            val intent = Intent(requireContext(),
                CameraActivity::class.java)
            cameraLauncher.launch(intent)
            dialog.dismiss()
        }
        location?.visibility = View.GONE
        vnRecord?.visibility = View.INVISIBLE
        vnRecord?.setOnClickListener {
            Log.d("vnRecordSelected", "the camera is selected")
            dialog.dismiss()
        }
        location?.setOnClickListener {
        }
        dialog.show()
    }

    override fun backFromShortsUpload() {
        TODO("Not yet implemented")
    }

    override fun cancelShortsUpload() {
        TODO("Not yet implemented")
    }

    private val filePickerLauncher =
        registerForActivityResult(
            ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            // Handle selected files URIs here
            for (uri in uris) {
                // Process each selected file URI
            }
        }

    private val permissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
//                selectFiles()
            } else {


            }
        }

    private fun checkPermissionAndSelectFiles() {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            selectFiles()
        } else {
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun selectFiles() {
        filePickerLauncher.launch(arrayOf("*/*"))
    }

    private fun getNumberOfPagesFromUriForPDF(context: Context, uri: Uri): Int {
        var inputStream: InputStream? = null
        var numberOfPages = 0
        try {
            inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val document = PDDocument.load(inputStream)
                numberOfPages = document.numberOfPages
                document.close()
            }
        } catch (e: Exception) {
            // Handle exceptions
            Log.e("getNumberOfPagesFromUri", "getNumberOfPagesFromUri ex $e")
            e.printStackTrace()
        } finally {
            inputStream?.close()
        }
        return numberOfPages
    }

    private fun getNumberOfPagesFromUriForDoc(uri: Uri): Int {
        val contentResolver = context.contentResolver

        var numberOfPages = 0
        val inputStream: InputStream = contentResolver.openInputStream(uri) ?: return 0
        val hwpfDocument = HWPFDocument(inputStream)
        val range = hwpfDocument.range

        // Count the paragraphs within the range
        val paragraphs = Range(range.startOffset,
            range.endOffset, hwpfDocument).numParagraphs()
        numberOfPages = paragraphs

        hwpfDocument.close()
        inputStream.close()

        return numberOfPages

    }

    private fun getNumberOfPagesFromUriForDocx(uri: Uri): Int {
        val contentResolver = context.contentResolver

        var numberOfPages = 0
        val inputStream: InputStream = contentResolver.openInputStream(uri) ?: return 0
        val xwpfDocument = XWPFDocument(inputStream)

        // Count the paragraphs or sections in the document
        numberOfPages = xwpfDocument.paragraphs.size

        xwpfDocument.close()
        inputStream.close()

        return numberOfPages

    }


    private fun uploadImageFeed(filePath: String, content: String, tags: MutableList<String>) {
        uploadWorkRequest =
            OneTimeWorkRequestBuilder<FeedUploadWorker>()
                .setInputData(
                    Data.Builder()
                        .putString(FeedUploadWorker.EXTRA_FILE_PATH, filePath)
                        .putString(FeedUploadWorker.CAPTION, content)
                        .putString(FeedUploadWorker.CONTENT_TYPE, "image")
                        .putString(FeedUploadWorker.DURATION, "")
                        .putStringArray(FeedUploadWorker.TAGS, tags.toTypedArray())
                        .build()
                )
                .build()
        val applicationContext = context.applicationContext

        val workManager = WorkManager.getInstance(applicationContext)


        Log.d("Upload", "Enqueuing upload work request...")
        workManager.enqueue(uploadWorkRequest!!)

        lifecycleScope.launch(Dispatchers.Main) {
            Log.d("Progress", "Progress ...scope")

            workManager.getWorkInfoByIdLiveData(uploadWorkRequest!!.id)
                .observe(this@NewRepostedPostFragment) { workInfo ->
                    Log.d("Progress", "Observer triggered!")
                    if (workInfo != null) {
                        val progress =
                            workInfo.progress.getInt(FeedUploadWorker.Progress, 0)
                        // Update your UI with the progress value
                        Log.d("Progress", "Progress $progress")
                    } else {
                        Log.d("Progress", "Work info is null")
                    }

                    if (workInfo.state == WorkInfo.State.RUNNING) {
                        // Access progress here
                        Log.d("Progress", "Running")
                    }
                    if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                        // Access progress here
                        Log.d("Progress", "SUCCEEDED")
                    }
                    if (workInfo.state == WorkInfo.State.ENQUEUED) {
                        // Access progress here
                        Log.d("Progress", "ENQUEUED")
                    }
                    if (workInfo.state == WorkInfo.State.BLOCKED) {
                        // Access progress here
                        Log.d("Progress", "BLOCKED")
                    }

                    if (workInfo.state == WorkInfo.State.CANCELLED) {
                        // Access progress here
                        Log.d("Progress", "CANCELLED")
                    }

                }
        }

    }

    private fun uploadMultipleImageFeed(
        compressedImageFiles: MutableList<File>,
        content: String,
        tags: MutableList<String>
    ) {
        Log.d(
            TAG,
            "uploadMultipleImageFeed: 'upload multiple files size: " +
                    "${compressedImageFiles.size}"
        )
// Assuming compressedImageFiles is your list of compressed files
        val multipleImagesList: MutableList<String> = mutableListOf()
        for (compressedFile in compressedImageFiles) {
            val compressedFilePath = compressedFile.absolutePath
            multipleImagesList.add(compressedFilePath)
            Log.d("CompressedFilePath", "Compressed file path: $compressedFilePath")
            // Use compressedFilePath as needed, e.g., to display, upload, etc.
        }

        uploadWorkRequest =
            OneTimeWorkRequestBuilder<FeedUploadWorker>()
                .setInputData(
                    Data.Builder()
                        .putStringArray(
                            FeedUploadWorker.MULTIPLE_IMAGES,
                            multipleImagesList.toTypedArray()
                        )
                        .putString(FeedUploadWorker.CAPTION, content)
                        .putString(FeedUploadWorker.CONTENT_TYPE, "multiple_images")
                        .putString(FeedUploadWorker.DURATION, "")
                        .putStringArray(FeedUploadWorker.TAGS, tags.toTypedArray())
                        .build()
                )
                .build()


        val applicationContext = context.applicationContext
        val workManager = WorkManager.getInstance(applicationContext)

        Log.d("Upload", "Enqueuing upload work request...")
        workManager.enqueue(uploadWorkRequest!!)

        lifecycleScope.launch(Dispatchers.Main) {
            Log.d("Progress", "Progress ...scope")

            workManager.getWorkInfoByIdLiveData(uploadWorkRequest!!.id)
                .observe(this@NewRepostedPostFragment) { workInfo ->
                    Log.d("Progress", "Observer triggered!")
                    if (workInfo != null) {
                        val progress =
                            workInfo.progress.getInt(FeedUploadWorker.Progress, 0)
                        // Update your UI with the progress value
                        Log.d("Progress", "Progress $progress")
                    } else {
                        Log.d("Progress", "Work info is null")
                    }

                    if (workInfo.state == WorkInfo.State.RUNNING) {
                        // Access progress here
                        Log.d("Progress", "Running")
                    }
                    if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                        // Access progress here
                        Log.d("Progress", "SUCCEEDED")
                    }
                    if (workInfo.state == WorkInfo.State.ENQUEUED) {
                        // Access progress here
                        Log.d("Progress", "ENQUEUED")
                    }
                    if (workInfo.state == WorkInfo.State.BLOCKED) {
                        // Access progress here
                        Log.d("Progress", "BLOCKED")
                    }

                    if (workInfo.state == WorkInfo.State.CANCELLED) {
                        // Access progress here
                        Log.d("Progress", "CANCELLED")
                    }

                }
        }

    }

    private suspend fun extractThumbnailsFromVideos(videoUri: Uri): List<Bitmap> {
        // Replace this with your actual implementation to extract thumbnails
        val videoUrls = listOf(videoUri)
        val thumbnails = mutableListOf<Bitmap>()

        for (videoUrl in videoUrls) {
            val thumbnail = extractThumbnail(videoUrl)
            thumbnail?.let { thumbnails.addAll(it) }
        }

        return thumbnails
    }


    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
        }

    }


    override fun saveBitmapToFile(bitmap: Bitmap, context: Context): File {
        val fileDir = File(context.filesDir, "thumbnails")
        if (!fileDir.exists()) {
            fileDir.mkdirs()
        }
        val fileName = "thumbnail.png"

        val file = File(fileDir, fileName)
        try {
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }

    fun saveBitmapToFile(bitmap: Bitmap, context: Context, fileName: String): File {
        // Create a directory for the thumbnails if it doesn't already exist
        val fileDir = File(context.filesDir, "thumbnails")
        if (!fileDir.exists()) {
            fileDir.mkdirs()
        }

        // Create a File object with the specified file name
        val file = File(fileDir, fileName)

        try {
            // Write the bitmap to the file
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return file
    }

    private suspend fun extractThumbnail(videoUrl: Uri): List<Bitmap>? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, videoUrl)

            // Get the duration of the video in milliseconds
            val durationMs =
                retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
                    ?: 0

            // Set the frame interval to 1000ms (1 second)
            val frameIntervalMs = 1000L

            val thumbnails = mutableListOf<Bitmap>()

            // Iterate through each second and retrieve the frame
            for (timeMs in 0 until durationMs step frameIntervalMs) {
                val bitmap: Bitmap? = retriever.getFrameAtTime(
                    timeMs * 1000,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                )
                bitmap?.let { thumbnails.add(it) }
            }

            // Release the MediaMetadataRetriever
            retriever.release()

            thumbnails
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    @SuppressLint("SetTextI18n")
    private fun handleDocumentUriToUpload(uri: Uri) {
        // Handle the selected document URI here
        // For example, you can retrieve the file name
        documentUriListToUpload.add(uri.toString())
        val contentResolver = context.contentResolver
        val recyclerView2: RecyclerView = requireView().findViewById(R.id.recyclerView2)

        contentResolver.query(
            uri, null, null, null, null)?.use { cursor ->

            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            cursor.moveToFirst()
            val fileName = cursor.getString(nameIndex)
            val fileSize = cursor.getLong(sizeIndex)

            var numberOfPages = 0
            val formattedFileSize = formatFileSize(fileSize)
            fileType = "mixed_files"

            val fileSizes = isFileSizeGreaterThan2MB(fileSize)
            val documentType = fileType(fileName)
            Log.d("handleDocumentUri", ": $fileName")
            Log.d("handleDocumentUri", "uri $uri")
            Log.d("handleDocumentUri", "formattedFileSize $formattedFileSize")

            recyclerView2.visibility = View.INVISIBLE

            numberOfPages = when (documentType) {
                "doc" -> {
                    getNumberOfPagesFromUriForDoc(uri)
                }

                "docx", "xlsx", "pptx" -> {
                    getNumberOfPagesFromUriForDocx(uri)
                }

                else -> {
                    getNumberOfPagesFromUriForPDF(context, uri)
                }
            }



            documentFileNamesToUpload.add(fileName)
            documentNumberOfPagesToUpload.add(numberOfPages.toString())
            documentTypesToUpload.add(documentType)
            if (documentType == "pdf") {
                retrieveFirstPageAndSaveAsImage(context, uri)
            }
//            binding.content.text =
        }
    }

    override fun handleDocumentUriToUploadReturn(uri: Uri): FeedMultipleDocumentsDataClass {
        // Handle the selected document URI here
        // For example, you can retrieve the file name
        val contentResolver = context.contentResolver
        Log.d("handleDocumentUri", ": $uri")
        val recyclerView2: RecyclerView = requireView().findViewById(R.id.recyclerView2)
        documentUriListToUpload.add(uri.toString())
        var numberOfPages = 0
        var formattedFileSize: String = ""
        var documentType: String = ""
        var fileName: String = ""
        var bitmapDocument: Bitmap? = null
        var pdfFile: File? = null
        var pdfFilePath = ""

        contentResolver.query(
            uri, null, null, null, null)?.use { cursor ->

            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            cursor.moveToFirst()
            fileName = cursor.getString(nameIndex)
            val fileSize = cursor.getLong(sizeIndex)


            formattedFileSize = formatFileSize(fileSize)
            fileType = "mixed_files"

            val fileSizes = isFileSizeGreaterThan2MB(fileSize)
            documentType = fileType(fileName)
            pdfFile = uriToFile2(context, uri, documentType)
            if (documentType == "pdf") {
                lifecycleScope.launch(Dispatchers.IO) {
                    bitmapDocument = retrieveFirstPageAsBitmap(context, uri)
                }

            }

            if (pdfFile != null) {
                pdfFilePath = pdfFile!!.absolutePath
            }
            Log.d("handleDocumentUri", ": $fileName")
            Log.d("handleDocumentUri", "uri $uri")
            Log.d("handleDocumentUri", "formattedFileSize $formattedFileSize")

            recyclerView2.visibility = View.INVISIBLE

            numberOfPages = when (documentType) {
                "doc" -> {
                    getNumberOfPagesFromUriForDoc(uri)
                }

                "docx", "xlsx", "pptx" -> {
                    getNumberOfPagesFromUriForDocx(uri)
                }

                else -> {
                    getNumberOfPagesFromUriForPDF(context, uri)
                }
            }




            documentFileNamesToUpload.add(fileName)
            documentNumberOfPagesToUpload.add(numberOfPages.toString())
            documentTypesToUpload.add(documentType)


//            binding.content.text =

        }
        if (bitmapDocument != null) {
            return FeedMultipleDocumentsDataClass(
                uri = uri,
                filename = fileName,
                numberOfPages = numberOfPages.toString(),
                documentType = documentType,
                fileSize = formattedFileSize,
                documentThumbnailFilePath = bitmapDocument,
                pdfFilePath = pdfFilePath
            )
        } else {
            val drawable = ContextCompat.getDrawable(context, R.drawable.documents)

            val thumbnailWidth = 100 // Set your desired thumbnail width
            val thumbnailHeight = 100 // Set your desired thumbnail height
            val thumbnail = drawable?.let {
                ThumbnailUtil.drawableToThumbnail(
                    context, it, thumbnailWidth, thumbnailHeight)
            }

            return FeedMultipleDocumentsDataClass(
                uri = uri,
                filename = fileName,
                numberOfPages = numberOfPages.toString(),
                documentType = documentType,
                fileSize = formattedFileSize,
                documentThumbnailFilePath = thumbnail,
                pdfFilePath = pdfFilePath
            )
        }

    }

    @SuppressLint("SetTextI18n")
    private fun handleDocumentUri(uri: Uri) {
        // Handle the selected document URI here
        // For example, you can retrieve the file name
        val contentResolver = context.contentResolver

        contentResolver.query(
            uri, null, null, null, null)?.use { cursor ->

            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            cursor.moveToFirst()
            val fileName = cursor.getString(nameIndex)
            val fileSize = cursor.getLong(sizeIndex)


            val shortThumbNail: ImageView = requireView().findViewById(R.id.shortThumbNail)
            shortThumbNail.setImageResource(R.drawable.documents)
            val recyclerView2: RecyclerView = requireView().findViewById(R.id.recyclerView2)

            var numberOfPages = 0
            val formattedFileSize = formatFileSize(fileSize)
            fileType = "mixed_files"

            val fileSizes = isFileSizeGreaterThan2MB(fileSize)
            val documentType = fileType(fileName)
            Log.d("handleDocumentUri", ": $fileName")
            Log.d("handleDocumentUri", "uri $uri")
            Log.d("handleDocumentUri", "formattedFileSize $formattedFileSize")
            recyclerView2.visibility = View.INVISIBLE

            numberOfPages = when (documentType) {
                "doc" -> {
                    getNumberOfPagesFromUriForDoc(uri)
                }

                "docx", "xlsx", "pptx" -> {
                    getNumberOfPagesFromUriForDocx(uri)
                }

                else -> {
                    getNumberOfPagesFromUriForPDF(context, uri)
                }
            }

            shortThumbNail.setPadding(0)
            shortThumbNail.colorFilter = null
            feedUploadViewModel.setText(
                "File name: $fileName \nFile size: " +
                        "$formattedFileSize \nDocument Type: $documentType \n$numberOfPages pages")

            this.numberOfPages = numberOfPages.toString()
            this.fileName = fileName
            this.docType = documentType
            this.docFilePath = uri.toString()
//            retrieveFirstPageAndSaveAsImage(this, uri)
//            binding.content.text =
        }
    }

    private fun retrieveFirstPageAsBitmap(context: Context, uri: Uri): Bitmap? {
        val contentResolver = context.contentResolver

        Log.i(TAG, "retrieveFirstPageAsBitmap: converting to bitmap")
        return try {
            // Open a ParcelFileDescriptor from the URI
            val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")

            parcelFileDescriptor?.use { pfd ->
                // Create a PdfRenderer from the ParcelFileDescriptor
                val pdfRenderer = PdfRenderer(pfd)

                // Open the first page
                val page = pdfRenderer.openPage(0)

                // Create a bitmap of the page
                val bitmap = Bitmap.createBitmap(
                    page.width, page.height, Bitmap.Config.ARGB_8888)

                // Render the page content into the bitmap
                page.render(
                    bitmap, null, null,
                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                // Close the page and the PdfRenderer
                page.close()
                pdfRenderer.close()

                val bitmapFilePath = saveBitmapToCache2(context, bitmap)
                bitmap
            }
        } catch (e: Exception) {
            Log.e(TAG, "retrieveFirstPageAsBitmap: error retrieving bitmap")
            e.printStackTrace()
            null
        }
    }

    private fun retrieveFirstPageAndSaveAsImage(context: Context, uri: Uri) {
        val contentResolver = context.contentResolver

        Log.i(TAG, "retrieveFirstPageAndSaveAsImage: save to bitmap")
        try {
            // Open a ParcelFileDescriptor from the URI
            val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")

            parcelFileDescriptor?.use { pfd ->
                // Create a PdfRenderer from the ParcelFileDescriptor
                val pdfRenderer = PdfRenderer(pfd)

                // Open the first page
                val page = pdfRenderer.openPage(0)

                // Create a bitmap of the page
                val bitmap = Bitmap.createBitmap(
                    page.width, page.height, Bitmap.Config.ARGB_8888)

                // Render the page content into the bitmap
                page.render(bitmap, null,
                    null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                // Close the page and the PdfRenderer
                page.close()
                pdfRenderer.close()

                // Save bitmap to cache directory
                saveBitmapToCache(context, bitmap)
            }
        } catch (e: Exception) {
            Log.e(TAG, "retrieveFirstPageAndSaveAsImage: not saved to bitmap")
            e.printStackTrace()
        }
    }

    // Function to save bitmap to cache directory
    private fun saveBitmapToCache(context: Context, bitmap: Bitmap) {
        val cacheDir = context.cacheDir // Get the cache directory

        // Create a file in the cache directory
//        val file = File(cacheDir, "first_page_image.png")
        val file = File(cacheDir, generateRandomFileName())

        try {
            // Write the bitmap data to the file
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
            // Bitmap saved successfully
            Log.d(TAG, "Bitmap saved to cache directory: ${file.absolutePath}")
            documentThumbnailsToUpload.add(file.absolutePath)
        } catch (e: IOException) {
            Log.e(TAG, "saveBitmapToCache: not saved to bitmap")
            e.printStackTrace()
        }
    }

    override fun saveBitmapToCache2(context: Context, bitmap: Bitmap): String {
        val cacheDir = context.cacheDir // Get the cache directory

        // Create a file in the cache directory
//        val file = File(cacheDir, "first_page_image.png")
        val file = File(cacheDir, generateRandomFileName())

        try {
            // Write the bitmap data to the file
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
            // Bitmap saved successfully
            Log.d(TAG, "Bitmap saved to cache directory: ${file.absolutePath}")
            return file.absolutePath
//            documentThumbnailsToUpload.add(file.absolutePath)
        } catch (e: IOException) {
            Log.e(TAG, "saveBitmapToCache: not saved to bitmap")
            e.printStackTrace()
            return ""
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun uploadMixedFeed(
        mixedFiles: MutableList<MixedFeedUploadDataClass>,
        content: String,
        tags: MutableList<String>,
    ) {

        val gson = createGson()
        val compressionCount = 0
        val feedShortsBusinessId: String = generateRandomId()
        val audioListToUpload: MutableList<MixedFeedUploadDataClass> = mutableListOf()
        val compressedAudioListToUpload: MutableList<MixedFeedUploadDataClass> = mutableListOf()
        val itemsToRemove = mutableListOf<MixedFeedUploadDataClass>()


        lifecycleScope.launch(Dispatchers.IO) {

            for (mixedFile in mixedFiles) {
                if (mixedFile.audios != null) {
                    audioListToUpload.add(mixedFile)
                    itemsToRemove.add(mixedFile)
                }
            }


            for (video in mixedFiles) {

                if (video.videos != null) {

                    val applicationContext = requireContext().applicationContext

//                    Log.d(TAG, "uploadMixedFeed: video ${video.videos!!.videoPath}")
                    val thumbnailFile =
                        video.videos!!.thumbnail?.let {
                            saveBitmapToFile(it, applicationContext) }
                    val thumbnailFilePath = thumbnailFile?.absolutePath
                    if (thumbnailFilePath != null) {
                        compressShorts(
                            path = video.videos!!.videoPath,
                            caption = caption,
                            thumbnailFilePath = thumbnailFilePath,
                            fileId = video.fileId,
                            feedShortsBusinessId = feedShortsBusinessId
                        )
                    }
                }
            }

            if (itemsToRemove.isNotEmpty()) {
                mixedFiles.removeAll(itemsToRemove)
            }

            val cacheDir = context.cacheDir // Get the cache directory

//            Log.d(TAG, "uploadMixedFeed: audioListToUpload size ${audioListToUpload.size}")
            if (audioListToUpload.isNotEmpty()) {
                for (audioList in audioListToUpload) {

                    val audioPath = audioList.audios?.audioPath
                    val filename = audioList.audios?.filename
                    val duration = audioList.audios?.duration
                    if (audioPath != null) {

//                        Log.d(TAG, "uploadMixedFeed: file paths $audioPath")

//                        Log.d(TAG, "uploadAudioFeed audioPath: $audioPath")
                        val outputFileName =
                            "compressed_audio${System.currentTimeMillis()}.mp3"

                        Log.d(TAG, "uploadMixedFeed: 1")
                        val outputFilePath = File(cacheDir, outputFileName)
                        Log.d(TAG, "uploadMixedFeed: 2")
                        val ffmpegCompressor = AudioCompressorWithProgress()
                        Log.d(TAG, "uploadMixedFeed: 3")
                        val audioDu =
                            reverseFormattedDuration(audioList.audios!!.duration)
//                        withContext(Dispatchers.IO) {
//
//                        }
//                        Log.d(TAG, "uploadMixedFeed: 4")

                        val audioFile = File(audioPath)
                        val fileSizeInBytes = audioFile.length()
                        val fileSizeInKB = fileSizeInBytes / 1024
                        val fileSizeInMB = fileSizeInKB / 1024

//

                        if (fileSizeInMB > 2) {
//

                            val isCompressionSuccessful = ffmpegCompressor.compress(
                                audioPath,
                                outputFilePath.absolutePath,
                                audioDu, this@NewRepostedPostFragment
                            )
//                            Log.d(
//                                TAG,
//                                "uploadAudioFeed outputFileName: ${outputFilePath.absolutePath}"
//                            )
                            if (isCompressionSuccessful) {
                                compressedAudioListToUpload.add(
                                    MixedFeedUploadDataClass(
                                        audios = FeedMultipleAudios(
                                            duration = duration!!,
                                            filename = filename!!,
                                            audioPath = outputFilePath.absolutePath
                                        ), fileTypes = "audio"
                                    )
                                )
//                                Log.d(TAG, "uploadAudioFeed: compression successful")
                            } else {
//                                Log.d(TAG, "uploadAudioFeed: compression not successful")
                            }
                        } else {
//                            Log.d(TAG, "uploadMixedFeed: found one with file size less than 2mbs")
                            compressedAudioListToUpload.add(
                                MixedFeedUploadDataClass(
                                    audios = audioList.audios, fileTypes = "audio"
                                )
                            )
                        }
                    }
                }
//                Log.d(TAG, "uploadMixedFeed: finished for loop")
            }

//            Log.d(TAG, "uploadMixedFeed: after audio compression")

            if (compressedAudioListToUpload.isNotEmpty()) {
                for (audios in compressedAudioListToUpload) {
                    mixedFiles.add(audios)
                }
            }


            for (video in mixedFiles) {
                Log.d(TAG, "uploadMixedFeed video thumbnail:" +
                        " ${video.videos?.thumbnail}")
            }


            val uploadDataJson = gson.toJson(mixedFiles)

            val inputData = Data.Builder()
                .putString("upload_data", uploadDataJson)
                .putString(FeedUploadWorker.CAPTION, content)
                .putString(FeedUploadWorker.FEED_SHORTS_BUSINESS_ID,
                    feedShortsBusinessId)
                .putString(FeedUploadWorker.CONTENT_TYPE, "mixed_files")
                .putStringArray(FeedUploadWorker.TAGS, tags.toTypedArray())
                .build()




            try {
                GlobalScope.launch(Dispatchers.IO) {
                    Log.d(TAG, "uploadVideoFeed: step 3")

//                    Log.d(TAG, "uploadVideoFeed: thumbnailFilePath $thumbnailFilePaths")
                    uploadWorkRequest = OneTimeWorkRequestBuilder<FeedUploadWorker>()
                        .setInputData(inputData)
                        .build()

                    val applicationContext = requireContext().applicationContext

                    val workManager = WorkManager.getInstance(applicationContext)

                    Log.d("Upload", "Enqueuing upload work request...")
                    workManager.enqueue(uploadWorkRequest!!)
                }
            } catch (e: Exception) {
                Log.e(TAG, "uploadVideoFeed: error because ${e.message}")
                e.printStackTrace()
            }
        }
//        Log.d(TAG, "uploadVideoFeed: step 4")
    }

    private fun compressShorts(
        path: String,
        caption: String,
        fileId: String,
        feedShortsBusinessId: String,
        thumbnailFilePath: String
    ) {
        val uploadWorkRequest: OneTimeWorkRequest = OneTimeWorkRequestBuilder<ShortsUploadWorker>()
            .setInputData(
                Data.Builder()
                    .putString(ShortsUploadWorker.EXTRA_FILE_PATH, path)
                    .putString(ShortsUploadWorker.CAPTION, caption)
                    .putString(ShortsUploadWorker.FILE_ID, fileId)
                    .putString(ShortsUploadWorker.FEED_SHORTS_BUSINESS_ID,
                        feedShortsBusinessId)
//                    .putString(ShortsUploadWorker.FEED_SHORTS_BUSINESS_ID, feedShortsBusinessId)
//                        .putString(ShortsUploadWorker.TAGS, tags)
                    .putString(ShortsUploadWorker.THUMBNAIL, thumbnailFilePath)

                    .build()
            )
            .build()
        val applicationContext = requireContext().applicationContext

        var workManager = WorkManager.getInstance(applicationContext)

        Log.d("Upload", "Enqueuing upload work request...")
        workManager.enqueue(uploadWorkRequest)
        // Inside compressShorts function, after enqueueing the work request

        lifecycleScope.launch(Dispatchers.Main) {
            Log.d("Progress", "Progress ...scope")

//                            val workManager = WorkManager.getInstance(applicationContext)
            workManager = WorkManager.getInstance(applicationContext)
            workManager.getWorkInfoByIdLiveData(uploadWorkRequest.id)
                .observe(viewLifecycleOwner) { workInfo ->
                    Log.d("Progress", "Observer triggered!")
                    if (workInfo != null) {
                        val progress =
                            workInfo.progress.getInt(
                                ShortsUploadWorker.Progress, 0)
                        // Update your UI with the progress value
                        Log.d("Progress", "Progress $progress")
                    } else {
                        Log.d("Progress", "Work info is null")
                    }

                    if (workInfo.state == WorkInfo.State.RUNNING) {
                        // Access progress here
                        Log.d("Progress", "Running")
                    }
                    if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                        // Access progress here
                        Log.d("Progress", "SUCCEEDED")
                    }
                    if (workInfo.state == WorkInfo.State.ENQUEUED) {
                        // Access progress here
                        Log.d("Progress", "ENQUEUED")
                    }
                    if (workInfo.state == WorkInfo.State.BLOCKED) {
                        // Access progress here
                        Log.d("Progress", "BLOCKED")
                    }

                    if (workInfo.state == WorkInfo.State.CANCELLED) {
                        // Access progress here
                        Log.d("Progress", "CANCELLED")
                    }

                }
        }


    }

    private fun setupRecyclerView(
        videoThumbnails: List<Bitmap>, videoDetails: FeedMultipleVideos) {

        val layoutManager = LinearLayoutManager(
            context, LinearLayoutManager.HORIZONTAL, false)
        val recyclerView: RecyclerView = requireView().findViewById(R.id.recyclerView)

        val adapter = FeedVideoThumbnailAdapter(videoThumbnails, this)
        adapter.setVideoDetails(videoDetails)
        recyclerView.visibility = View.VISIBLE
//        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
    }


    override fun onDetach() {
        super.onDetach()
        Log.d(TAG, "onDetach: called")
    }

    override fun onImageClick() {

    }

    override fun onResume() {
        Log.d(TAG, "onResume: ")
        super.onResume()
        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle back press
//                navigateBack()
                if (feedTextViewFragmentInterface != null) {
                    feedTextViewFragmentInterface?.backPressedFromFeedTextViewFragment()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            this, backPressedCallback)
    }


    override fun onThumbnailClick(thumbnail: Bitmap, videoDetails: FeedMultipleVideos) {
    }

    override fun onThumbnailLongClick(
        thumbnail: Bitmap,
        videoDetails: FeedMultipleVideos,
        position: Int
    ) {
        TODO("Not yet implemented")
    }

    override fun onAudioClick() {

    }

    override fun onAudioDisplay(details: MultipleAudios) {
        TODO("Not yet implemented")
    }

    override fun onProgress(progress: Int) {
        TODO("Not yet implemented")
    }

    override fun onVideoClick() {
        TODO("Not yet implemented")
    }

    override fun onVideoDisplay(details: FeedMultipleVideos) {
        TODO("Not yet implemented")
    }

    fun backPressedFromFeedTextViewFragment() {
        EventBus.getDefault().post(ShowBottomNav(false))
        EventBus.getDefault().post(ShowAppBar(false))
        EventBus.getDefault().post(ShowFeedFloatingActionButton(false))
        releaseMediaPlayer()
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
        val handler = Handler(Looper.getMainLooper())

//        handler.removeCallbacks(updateSeekBarRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
//        player?.removeListener(playbackStateListener)
//        player?.release()
    }

    fun setListener(listener: FeedTextViewFragmentInterface) {
        feedTextViewFragmentInterface = listener
    }
}
