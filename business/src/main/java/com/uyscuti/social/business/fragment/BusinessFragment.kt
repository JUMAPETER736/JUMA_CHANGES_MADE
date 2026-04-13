package com.uyscuti.social.business.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.ContentUris
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.ExoDatabaseProvider
import androidx.media3.datasource.DefaultDataSourceFactory
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.uyscuti.sharedmodule.FlashApplication
import com.uyscuti.sharedmodule.adapter.CommentsRecyclerViewAdapter
import com.uyscuti.sharedmodule.adapter.OnViewRepliesClickListener
import com.uyscuti.sharedmodule.adapter.notifications.AdPaginatedAdapter
import com.uyscuti.social.network.api.models.Comment
import com.uyscuti.social.core.models.data.Dialog
import com.uyscuti.social.core.models.data.Message
import com.uyscuti.social.core.models.data.User
import com.uyscuti.sharedmodule.media.CameraActivity
import com.uyscuti.sharedmodule.model.AudioPlayerHandler
import com.uyscuti.sharedmodule.model.CommentAudioPlayerHandler
import com.uyscuti.sharedmodule.model.HideBottomNav
import com.uyscuti.sharedmodule.model.PauseShort
import com.uyscuti.sharedmodule.model.ShowBottomNav
import com.uyscuti.sharedmodule.popupDialog.BusinessProfileDialogFragment
import com.uyscuti.sharedmodule.popupDialog.DialogManager
import com.uyscuti.sharedmodule.presentation.DialogViewModel
import com.uyscuti.sharedmodule.presentation.MessageViewModel
import com.uyscuti.sharedmodule.ui.GifActivity
import com.uyscuti.sharedmodule.uploads.AudioActivity
import com.uyscuti.sharedmodule.uploads.DocumentsActivity
import com.uyscuti.sharedmodule.uploads.ImagesActivity
import com.uyscuti.sharedmodule.uploads.VideosActivity
import com.uyscuti.sharedmodule.utils.AndroidUtil.showToast
import com.uyscuti.sharedmodule.utils.AudioDurationHelper
import com.uyscuti.sharedmodule.utils.AudioDurationHelper.getFormattedDuration
import com.uyscuti.sharedmodule.utils.ChatManager
import com.uyscuti.sharedmodule.utils.ChatManager.ChatManagerListener
import com.uyscuti.sharedmodule.utils.NetworkUtil
import com.uyscuti.sharedmodule.utils.PathUtil
import com.uyscuti.sharedmodule.utils.Timer
import com.uyscuti.sharedmodule.utils.TrimVideoUtils
import com.uyscuti.sharedmodule.utils.WaveFormExtractor
import com.uyscuti.sharedmodule.utils.audiomixer.AudioMixer
import com.uyscuti.sharedmodule.utils.audiomixer.input.GeneralAudioInput
import com.uyscuti.sharedmodule.utils.deleteFiles
import com.uyscuti.sharedmodule.utils.fileType
import com.uyscuti.sharedmodule.utils.formatFileSize
import com.uyscuti.sharedmodule.utils.generateRandomId
import com.uyscuti.sharedmodule.utils.getFileNameFromLocalPath
import com.uyscuti.sharedmodule.utils.getOutputFilePath
import com.uyscuti.sharedmodule.utils.isFileSizeGreaterThan2MB
import com.uyscuti.sharedmodule.utils.waveformseekbar.SeekBarOnProgressChanged
import com.uyscuti.sharedmodule.utils.waveformseekbar.WaveformSeekBar
import com.uyscuti.sharedmodule.views.WaveFormView
import com.uyscuti.social.business.CatalogueDetailsActivity
import com.uyscuti.social.business.CategoryActivity
import com.uyscuti.social.business.R
import com.uyscuti.social.business.adapter.business.BusinessCatalogueAdapter
import com.uyscuti.social.business.adapter.business.OnBusinessClickedListener
import com.uyscuti.social.business.interfaces.BottomNavController
import com.uyscuti.social.business.model.business.BusinessProfile
import com.uyscuti.social.business.repository.IFlashApiRepositoryImplementation
import com.uyscuti.social.business.retro.CreateCatalogueActivity
import com.uyscuti.social.business.viewmodel.CatalogueAdapterViewModel
import com.uyscuti.social.business.viewmodel.business.BusinessCatalogueViewModel
import com.uyscuti.social.business.viewmodel.business.BusinessCatalogueViewModelFactory
import com.uyscuti.social.business.viewmodel.business.BusinessPostsViewModel
import com.uyscuti.social.chatsuit.messages.CommentsInput
import com.uyscuti.social.core.common.data.api.RemoteMessageRepository
import com.uyscuti.social.core.common.data.api.RemoteMessageRepositoryImpl
import com.uyscuti.social.core.common.data.room.entity.DialogEntity
import com.uyscuti.social.core.common.data.room.entity.MessageEntity
import com.uyscuti.social.core.common.data.room.entity.UserEntity
import com.uyscuti.social.network.api.request.messages.SendMessageRequest
import com.uyscuti.social.network.api.response.business.response.post.Post
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage
import com.vanniktech.emoji.EmojiPopup
import dagger.hilt.android.AndroidEntryPoint
import id.zelory.compressor.Compressor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.apache.poi.hwpf.HWPFDocument
import org.apache.poi.hwpf.usermodel.Range
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.Date
import javax.inject.Inject
import kotlin.getValue
import kotlin.properties.Delegates
import kotlin.random.Random

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val TAG = "BusinessFragment"

@UnstableApi
@Suppress("DEPRECATION")
@AndroidEntryPoint
class BusinessFragment : Fragment(),
    OnBusinessClickedListener,
    CommentsInput.InputListener,
    CommentsInput.EmojiListener,
    CommentsInput.VoiceListener,
    CommentsInput.GifListener,
    CommentsInput.AttachmentsListener,
    OnViewRepliesClickListener,
    Timer.OnTimeTickListener,
    ChatManagerListener {

    @Inject
    lateinit var retrofitInstance: RetrofitInstance

    private var bottomNavController: BottomNavController? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->

    }


    private lateinit var businessRecycleView: RecyclerView
    private lateinit var businessAdapter: BusinessCatalogueAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var recycleViewProgressBar: ProgressBar
    private lateinit var errorTextView: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var viewModel: BusinessCatalogueViewModel
    private lateinit var motionLayout: MotionLayout
    private lateinit var vnLayout: ConstraintLayout
    private lateinit var replyToLayout: LinearLayout
    private lateinit var textInput: CommentsInput
    private lateinit var click: View
    private lateinit var emojiPopup: EmojiPopup
    private lateinit var inputMethodManager: InputMethodManager

    private lateinit var sellerButton: MaterialButton
    private lateinit var category: MaterialButton
    private lateinit var shimmerLayout: ShimmerFrameLayout
    private lateinit var placeholderLayout: LinearLayout

    //Vn layout views
    private lateinit var playAudioLayout: LinearLayout
    private lateinit var waveForm: WaveFormView
    private lateinit var timerTv: TextView
    private lateinit var deleteVn: ImageView
    private lateinit var exitReply: ImageView

    private lateinit var replyToTextView: TextView

    private lateinit var settings: SharedPreferences

    private var businessPostId = ""
    private var commentId = ""

    private var postPosition = 0


    private var commentAdapter: CommentsRecyclerViewAdapter? = null
    private lateinit var commentRecyclerView: RecyclerView

    private val businessPostsViewModel: BusinessPostsViewModel by activityViewModels()

    private val catalogueViewModel: CatalogueAdapterViewModel by activityViewModels()


    private var isScrollingDown = false
    private var isReply = false

    private var emojiShowing = false

    private lateinit var businessProfileId: String

    @Inject
    lateinit var localStorage: LocalStorage

    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var audioPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var videoPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var gifsPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var docsPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var categoryLauncher: ActivityResultLauncher<Intent>
    private var query = ""
    private lateinit var categoryTextView: TextView
    private lateinit var categoryLayout: LinearLayout
    private lateinit var emptyStateTextView: TextView
    private lateinit var emptyStateLayout: LinearLayout
    private var isInSearchMode = false
    private var lastSearchQuery = ""

    private lateinit var repository: IFlashApiRepositoryImplementation
    private var exoPlayer: ExoPlayer? = null

    private var businessProfile: Result<BusinessProfile>? = null
    private var profileDeferred: Deferred<Boolean>? = null
    private var hasBusinessProfile: Boolean = false

    private var commentToAddReplies: Comment? = null
    private var commentPosition = 0

    private lateinit var httpDataSourceFactory: HttpDataSource.Factory
    private lateinit var defaultDataSourceFactory: DefaultDataSourceFactory
    private lateinit var cacheDataSourceFactory: CacheDataSource.Factory


    private var simpleCache: SimpleCache? = FlashApplication.cache

    private var isReplyVnPlaying = false
    private var isVnAudioToPlay = false
    var isDurationOnPause = false
    private var currentCommentAudioPosition = RecyclerView.NO_POSITION
    private var currentCommentAudioPath = ""
    private lateinit var audioFormWave: WaveformSeekBar
    private lateinit var audioSeekBar: SeekBar
    private var currentHandler: Handler? = null
    var seekBarProgress = 0f
    var waveProgress = 0f
    private var wavePosition = -1
    private var seekPosition = -1
    private var position: Int = 0
    var maxDuration = 0L

    private lateinit var audioDurationTVCount: TextView
    private var player: MediaPlayer? = null
    private val waveHandler = Handler()

    private lateinit var recordVn: ImageView

    private lateinit var sendVn: ImageView

    private lateinit var playerTimerTv: LinearLayout

    private lateinit var playVnAudioBtn: ImageView

    var vnRecordAudioPlaying = false
    var vnRecordProgress = 0
    var isOnRecordDurationOnPause = false

    private lateinit var wave: WaveformSeekBar

    private val recordedAudioFiles = mutableListOf<String>()

    private var mediaRecorder: MediaRecorder? = null

    private lateinit var outputFile: String

    private var outputVnFile: String = ""

    var wasPaused = false
    var sending = false
    var firstTimeSendVn = false

    private var isRecording = false
    private var isPaused = false
    private var isAudioVNPlaying = false
    private var isAudioVNPaused = false

    private var mixingCompleted = false

    private var isVnResuming = false

    private lateinit var timer: Timer

    private lateinit var secondTimerTv: TextView

    private lateinit var thirdTimerTv: TextView

    private lateinit var amplitudes: ArrayList<Float>

    private var amps = 0

    private val dialogViewModel: DialogViewModel by activityViewModels()

    private lateinit var dialogManager: DialogManager

    @Inject // or another appropriate scope
    lateinit var chatManager: ChatManager
    private var offerMessage = ""

    private val messageViewModel: MessageViewModel by activityViewModels()
    private var messageEntity: MessageEntity? = null

    private lateinit var remoteMessageRepository: RemoteMessageRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CatalogueFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BusinessFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun registerImagePicker() {
        imagePickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    // Handle image selection result here
                    val data = result.data
                    // Process the selected image data
                    val imagePath = data?.getStringExtra("image_url")
                    val caption = data?.getStringExtra("caption") ?: ""

                    val filePath = PathUtil.getPath(
                        requireActivity(),
                        imagePath!!.toUri()
                    ) // Use the utility class to get the real file path
                    Log.d("PhotoPicker", "File path: $filePath")
                    Log.d("PhotoPicker", "File path: $isReply")

                    val file = filePath?.let { File(it) }
                    if (file?.exists() == true) {
                        lifecycleScope.launch {
                            val compressedImageFile = Compressor.compress(requireActivity(), file)
                            Log.d(
                                "PhotoPicker",
                                "PhotoPicker: compressedImageFile absolutePath: ${compressedImageFile.absolutePath}"
                            )

                            val fileSizeInBytes = compressedImageFile.length()
                            val fileSizeInKB = fileSizeInBytes / 1024
                            val fileSizeInMB = fileSizeInKB / 1024

                            Log.d(
                                "PhotoPicker",
                                "PhotoPicker: compressedImageFile size $fileSizeInKB KB, $fileSizeInMB MB"
                            )

                            if (!isReply) {
                                uploadImageComment(
                                    compressedImageFile.absolutePath,
                                    caption,
                                    isReply
                                )
                            } else {
                                uploadImageComment(
                                    compressedImageFile.absolutePath,
                                    caption,
                                    isReply
                                )
                            }
                        }
                    }

                }
            }
    }

    private fun registerDocPicker() {
        docsPickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val data = result.data
                    // Process the selected image data
                    val docPath = data?.getStringExtra("doc_url")
                    val caption = data?.getStringExtra("caption") ?: ""

                    Log.d(TAG, "Path: $docPath caption: $caption")

                    handleDocumentUri(
                        getContentUriFromFilePath(requireActivity(), docPath!!)!!,
                        caption
                    )
                }
            }
    }

    private fun getContentUriFromFilePath(context: Context, filePath: String): Uri? {
        val file = File(filePath)
        val projection = arrayOf(MediaStore.Files.FileColumns._ID)
        val selection = "${MediaStore.Files.FileColumns.DATA}=?"
        val selectionArgs = arrayOf(file.absolutePath)

        context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id =
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
                return ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id)
            }
        }
        return null
    }

    private fun getFileNameWithExtension(uri: Uri): String {
        var fileName = "document_${System.currentTimeMillis()}"

        // Try to get the original filename
        requireActivity().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst()) {
                fileName = cursor.getString(nameIndex) ?: fileName
            }
        }

        // If filename doesn't have an extension, get it from MIME type
        if (!fileName.contains(".")) {
            val mimeType = requireActivity().contentResolver.getType(uri)
            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
            if (extension != null) {
                fileName = "${fileName}.${extension}"
            }
        }

        return fileName
    }

    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = requireActivity().contentResolver.openInputStream(uri)
            val fileName = getFileNameWithExtension(uri)
            val tempFile = File(requireActivity().cacheDir, fileName)

            inputStream?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            // Debug: Check if file exists and has content
            Log.d("FileDebug", "File path: ${tempFile.absolutePath}")
            Log.d("FileDebug", "File exists: ${tempFile.exists()}")
            Log.d("FileDebug", "File size: ${tempFile.length()} bytes")

            if (tempFile.exists() && tempFile.length() > 0) {
                tempFile
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun handleDocumentUri(uri: Uri, caption: String) {

        val file = getFileFromUri(uri)

        requireActivity().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            cursor.moveToFirst()
            val fileName = cursor.getString(nameIndex)
            val fileSize = cursor.getLong(sizeIndex)
//            val numberOfPages = getNumberOfPagesFromUri(this, uri)
            var numberOfPages = 0
            val formattedFileSize = formatFileSize(fileSize)

            val fileSizes = isFileSizeGreaterThan2MB(fileSize)
            val documentType = fileType(fileName)
            Log.d("handleDocumentUri", ": $fileName")
            Log.d("handleDocumentUri", "uri $uri")
            Log.d("handleDocumentUri", "formattedFileSize $formattedFileSize")
            Log.d("handleDocumentUri", "Document type $documentType")

            numberOfPages = when (documentType) {
                "doc" -> {
                    getNumberOfPagesFromUriForDoc(uri)
                }

                "docx", "pptx" -> {
                    getNumberOfPagesFromUriForDocx(uri)
                }

                "xlsx", "xls" -> {
                    getNumberOfSheetsFromUri(uri)
                }

                else -> {
                    getNumberOfPagesFromUriForPDF(requireActivity(), uri)
                }
            }


            Log.d("handleDocumentUri", "File path: ${file?.absolutePath}")

            if (fileSizes) {
                if (!isReply) {
                    Log.d("handleDocumentUri", "handleDocumentUri for main document")

                    uploadDocumentComment(
                        file?.absolutePath!!,
                        caption,
                        numberOfPages,
                        formattedFileSize,
                        documentType,
                        fileName,
                        isReply
                    )
                } else {
                    Log.d("handleDocumentUri", "This is for document reply")
                    uploadDocumentComment(
                        file?.absolutePath!!,
                        caption,
                        numberOfPages,
                        formattedFileSize,
                        documentType,
                        fileName,
                        isReply
                    )
                }

            } else {

                if (!isReply) {
                    Log.d("handleDocumentUri", "handleDocumentUri for main document")
                    uploadDocumentComment(
                        file?.absolutePath!!,
                        caption,
                        numberOfPages,
                        formattedFileSize,
                        documentType,
                        fileName,
                        isReply
                    )
                } else {
                    Log.d("handleDocumentUri", "This is for document reply")
                    uploadDocumentComment(
                        file?.absolutePath!!,
                        caption,
                        numberOfPages,
                        formattedFileSize,
                        documentType,
                        fileName,
                        isReply
                    )
                }
            }

        }
    }

    private fun getNumberOfPagesFromUriForDoc(uri: Uri): Int {
        var numberOfPages = 0
        val inputStream: InputStream =
            requireActivity().contentResolver.openInputStream(uri) ?: return 0
        val hwpfDocument = HWPFDocument(inputStream)
        val range = hwpfDocument.range

        // Count the paragraphs within the range
        val paragraphs = Range(range.startOffset, range.endOffset, hwpfDocument).numParagraphs()
        numberOfPages = paragraphs

        hwpfDocument.close()
        inputStream.close()

        return numberOfPages

    }

    private fun getNumberOfPagesFromUriForPDF(context: Context, uri: Uri): Int {
        var inputStream: InputStream? = null
        var numberOfPages = 0
        try {
            inputStream = requireActivity().contentResolver.openInputStream(uri)
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

    private fun getNumberOfSheetsFromUri(uri: Uri): Int {
        var numberOfSheets = 0
        try {
            requireActivity().contentResolver.openInputStream(uri)?.use { inputStream ->
                // WorkbookFactory automatically handles both .xls and .xlsx
                WorkbookFactory.create(inputStream).use { workbook ->
                    numberOfSheets = workbook.numberOfSheets
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return numberOfSheets
    }

    private fun getNumberOfPagesFromUriForDocx(uri: Uri): Int {
        var numberOfPages = 0
        val inputStream: InputStream =
            requireActivity().contentResolver.openInputStream(uri) ?: return 0
        val xwpfDocument = XWPFDocument(inputStream)

        // Count the paragraphs or sections in the document
        numberOfPages = xwpfDocument.paragraphs.size

        xwpfDocument.close()
        inputStream.close()

        return numberOfPages

    }

    private fun uploadDocumentComment(
        documentFilePathToUpload: String,
        caption: String = "",
        numberOfPages: Int,
        fileSize: String,
        fileType: String,
        fileName: String,
        isReply1: Boolean
    ) {

        val file = File(documentFilePathToUpload)

        val localUpdateId = generateRandomId()
        Log.d("UploadingDocument", "File exist: ${file.exists()}")
        if (file.exists()) {
            Log.d("UploadingDocument", "Upload document called")

            if (isReply) {
                businessPostsViewModel.addCommentReply(
                    commentId,
                    file = file,
                    content = caption,
                    contentType = "docs",
                    localUpdateId = localUpdateId,
                    numberOfPages = numberOfPages,
                    fileType = fileType,
                    fileName = fileName,
                    fileSize = fileSize,
                    isReply = isReply1
                )

                isReply = false
            } else {
                businessPostsViewModel.addComment(
                    businessPostId,
                    file = file,
                    content = caption,
                    contentType = "docs",
                    localUpdateId = localUpdateId,
                    numberOfPages = numberOfPages,
                    fileType = fileType,
                    fileName = fileName,
                    fileSize = fileSize
                )
            }

        }

    }

    private fun uploadAudioComment(
        audio: String,
        caption: String? = "",
        contentType: String = "audio",
        isReply1: Boolean,
        fileType: String
    ) {
        val localUpdateId = generateRandomId()
        val file = File(audio)

        if (file.exists()) {
            if (isReply) {
                businessPostsViewModel.addCommentReply(
                    commentId,
                    content = caption,
                    file = file,
                    contentType = contentType,
                    localUpdateId = localUpdateId,
                    isReply = isReply1,
                    fileType = fileType
                )

                isReply = false
            } else {
                businessPostsViewModel.addComment(
                    businessPostId,
                    content = caption,
                    file = file,
                    contentType = contentType,
                    localUpdateId = localUpdateId,
                    fileType = fileType
                )
            }

        }


    }

    private fun uploadVideoComment(
        caption: String? = "",
        videoFilePathToUpload: String,
        isReply1: Boolean = false
    ) {
        Log.d("uploadVideoComment", "uploadVideoComment: $videoFilePathToUpload")

        val file = File(videoFilePathToUpload)

        val localUpdateId = generateRandomId()

        if (file.exists()) {
            if (isReply) {
                businessPostsViewModel.addCommentReply(
                    commentId,
                    content = caption,
                    contentType = "video",
                    localUpdateId = localUpdateId,
                    file = file,
                    isReply = isReply1
                )
                isReply = false
            } else {
                businessPostsViewModel.addComment(
                    businessPostId,
                    content = caption,
                    contentType = "video",
                    localUpdateId = localUpdateId,
                    file = file
                )
            }
        }

    }

    private fun uploadImageComment(
        imageFilePathToUpload: String,
        caption: String = "",
        isReply1: Boolean
    ) {

        Log.d("uploadImageComment", "uploadImageComment: $imageFilePathToUpload")
        Log.d("uploadImageComment", "uploadImageComment: isReply is $isReply")

        val file = File(imageFilePathToUpload)

        val localUpdateId = generateRandomId()

        if (file.exists()) {
            if (isReply) {
                businessPostsViewModel.addCommentReply(
                    commentId,
                    content = caption,
                    contentType = "image",
                    localUpdateId = localUpdateId,
                    file = file,
                    isReply = isReply1
                )
                isReply = false
            } else {
                businessPostsViewModel.addComment(
                    businessPostId,
                    content = caption,
                    contentType = "image",
                    localUpdateId = localUpdateId,
                    file = file
                )
            }
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        bottomNavController = parentFragment as? BottomNavController

    }

    private fun initCommentAdapter() {
        commentAdapter = CommentsRecyclerViewAdapter(requireActivity(), this@BusinessFragment)
        commentAdapter?.setDefaultRecyclerView(requireActivity(), R.id.recyclerView)

        commentRecyclerView.itemAnimator = null
    }

    private suspend fun setUpBusinessProfile(): Boolean {
        return try {
            businessProfile = repository.getBusinessProfile()

            if (businessProfile!!.isSuccess) {
                Log.d("ApiService", "${businessProfile.toString()}")
                true // Return the result
            } else {
                Log.d("ApiService", "${businessProfile.toString()}")
                false // Return the result
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Error getting business profile", e)
            false // Return false on error
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("SuspiciousIndentation")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_business, container, false)

        settings = requireActivity().getSharedPreferences("BusinessProfile", MODE_PRIVATE)

        businessProfileId = settings.getString("businessId", "").toString()
        Log.d("BUSINESS_PROFILE_ID", "BUSINESS_PROFILE_ID: $businessProfileId")

        initViews(view)

        remoteMessageRepository = RemoteMessageRepositoryImpl(retrofitInstance)

        registerVideoPickerLauncher()
        registerAudioPickerLauncher()
        registerGifPickerLauncher()
        registerCameraLauncher()
        registerDocPicker()
        registerImagePicker()
        registerCategoryLauncher()

        businessRecycleView.layoutManager = LinearLayoutManager(requireActivity())

        businessAdapter = BusinessCatalogueAdapter(
            requireActivity(),
            retrofitInstance,
            localStorage,
            onItemClick = { item ->
                // Handle item click - navigate to detail screen
                navigateToItemDetail(item)
            },
            this@BusinessFragment,
            onBookmarkClick = { item ->
                bookmarkBusinessPost(item)
            },
            onFollowClick = { item ->
                followBusinessPostOwner(item)
            },
            onMessageClick = { user, post ->

                val productReference = post.images.first()

                dialogManager = DialogManager(
                    requireActivity(),
                    dialogViewModel,
                    post.owner,
                    productReference
                )
                dialogManager.openChat(user)
            },
            onSendOfferClicked = { amount, message, data ->
                val user = User(
                    data.owner,
                    data.userDetails.username,
                    data.userDetails.avatar,
                    false,
                    Date()
                )

                var messageToSend = ""

                messageToSend = if (message.isEmpty()) {
                    "" +
                            "Hi! I'm interested in your ${data.itemName}." +
                            "\nWould you accept MWK$amount ?" +
                            "\nLet me know, thanks!"
                } else {
                    "" +
                            "${data.itemName}.\n${data.description}." +
                            "\nOffer Amount: MWK$amount ?" +
                            "\n$message"
                }

                offerMessage = messageToSend

                if (NetworkUtil.isConnected(requireActivity())) {
                    addDialogInTheBackGround(user, messageToSend)
                } else {
                    showToast(requireActivity(), "You have no internet connection")
                }
            },
            childFragmentManager,
            onLoadMore = {
                viewModel.loadMore()
            }
        )

        businessRecycleView.apply {
            setHasFixedSize(true) // If item size is constant
            itemAnimator = DefaultItemAnimator() // Ensure animations are enabled
            setItemViewCacheSize(20) // Increase cache size
        }
        businessRecycleView.adapter = businessAdapter


        setUpViewModel()
        observeViewModel()
        handleOnClickListener()

        businessRecycleView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && !isScrollingDown) {
                    isScrollingDown = true
                    EventBus.getDefault().post(HideBottomNav())
                } else if (dy < 0 && isScrollingDown) {
                    isScrollingDown = false
                    EventBus.getDefault().post(ShowBottomNav(false))
                }
            }
        })

        return view
    }


    private fun initViews(view: View) {
        businessRecycleView = view.findViewById(R.id.business_recycle_view)
        progressBar = view.findViewById(R.id.progress_bar)
        errorTextView = view.findViewById(R.id.tv_error)
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)
        sellerButton = view.findViewById(R.id.btn_seller)
        category = view.findViewById(R.id.btn_category)
        categoryTextView = view.findViewById(R.id.category)
        categoryLayout = view.findViewById(R.id.category_layout)
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout)
        emptyStateTextView = view.findViewById(R.id.emptyState)
        motionLayout = view.findViewById(R.id.motionLayout)
        vnLayout = view.findViewById(R.id.VnLayout)
        replyToLayout = view.findViewById(R.id.replyToLayout)
        textInput = view.findViewById(R.id.input)

        emojiPopup = EmojiPopup(motionLayout, textInput.inputEditText)
        inputMethodManager =
            requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        textInput.setInputListener(this)
        textInput.setAttachmentsListener(this)
        textInput.setVoiceListener(this)
        textInput.setEmojiListener(this)
        textInput.setGifListener(this)

        click = view.findViewById(R.id.click)

        commentRecyclerView = view.findViewById(R.id.recyclerView)
        shimmerLayout = view.findViewById(R.id.shimmer_layout)
        recycleViewProgressBar = view.findViewById(R.id.progressBar)
        placeholderLayout = view.findViewById(R.id.placeholderLayout)


        waveForm = view.findViewById(R.id.waveForm)
        playAudioLayout = view.findViewById(R.id.playAudioLayout)
        timerTv = view.findViewById(R.id.timerTv)
        deleteVn = view.findViewById(R.id.deleteVN)
        deleteVn.isClickable = true

        exitReply = view.findViewById(R.id.exitReply)
        replyToTextView = view.findViewById(R.id.replyToTextView)
        audioSeekBar = SeekBar(requireActivity())
        audioFormWave = WaveformSeekBar(requireActivity())

        recordVn = view.findViewById(R.id.recordVN)
        sendVn = view.findViewById(R.id.sendVN)
        playerTimerTv = view.findViewById(R.id.playerTimerTv)
        playVnAudioBtn = view.findViewById(R.id.playVnAudioBtn)
        wave = view.findViewById(R.id.wave)

        secondTimerTv = view.findViewById(R.id.secondTimerTv)

        thirdTimerTv = view.findViewById(R.id.thirdTimerTv)

        timer = Timer(this)

        audioDurationTVCount = TextView(requireActivity())

        repository = IFlashApiRepositoryImplementation(retrofitInstance)
    }

    private fun setUpViewModel() {

        val factory = BusinessCatalogueViewModelFactory(repository)

        viewModel = ViewModelProvider(this, factory)[BusinessCatalogueViewModel::class.java]

        // Setup swipe to refresh
        swipeRefreshLayout.setOnRefreshListener {
            if (viewModel.isInSearchMode()) {
                // Refresh search results
                viewModel.searchItemsImmediate(
                    query,
                    refresh = true
                )
                businessRecycleView.isVisible = false
                emptyStateLayout.isVisible = false
            } else {
                // Refresh regular catalogue
                businessRecycleView.isVisible = false
                viewModel.refreshCatalogue()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun observeViewModel() {
        // Observe catalogue items and update adapter
        viewModel.catalogueItems.observe(viewLifecycleOwner) { items ->
            val currentMode = viewModel.isInSearchMode()
            val currentQuery = if (currentMode) query else ""  // Or get from viewModel

            // Detect ANY change that needs animation disable
            val modeChanged = isInSearchMode != currentMode
            val queryChanged = isInSearchMode && currentMode && (lastSearchQuery != currentQuery)
            val needsNoAnimation = modeChanged || queryChanged

            if (items.isNotEmpty()) {
                if (needsNoAnimation) {
                    // Disable animations for mode change OR query change
                    val itemAnimator = businessRecycleView.itemAnimator
                    businessRecycleView.itemAnimator = null

                    businessAdapter.submitList(null)

                    businessRecycleView.post {
                        businessAdapter.submitList(items.toList())

                        businessRecycleView.postDelayed({
                            businessRecycleView.itemAnimator = itemAnimator
                        }, 100)
                    }

                    if (modeChanged) {
                        businessAdapter.resetPagination()
                    }
                } else {
                    // Same mode, same query - pagination or refresh
                    businessAdapter.updateCatalogue(items)
                }

                // Update tracking variables
                isInSearchMode = currentMode
                lastSearchQuery = currentQuery

                emptyStateLayout.isVisible = false
                businessRecycleView.isVisible = true

            } else {
                isInSearchMode = currentMode
                lastSearchQuery = currentQuery

                if (currentMode) {
                    emptyStateLayout.isVisible = true
                    emptyStateTextView.text = "Search results not found for '$currentQuery'."
                    businessRecycleView.isVisible = false
                }
            }
        }

        // 2. Observe paginated items (NEW PAGE DATA)
        viewModel.newPageItems.observe(viewLifecycleOwner) { newItems ->
            if (newItems != null && newItems.isNotEmpty()) {
                businessAdapter.appendCatalogue(newItems)
            }
        }

        catalogueViewModel.cataloguePost.observe(viewLifecycleOwner) { post ->
            businessAdapter.addCatalogue(post)
            businessRecycleView.post {
                businessRecycleView.smoothScrollToPosition(0)
            }
        }

        businessPostsViewModel.commentLiveData.observe(viewLifecycleOwner) { commentState ->

            if (motionLayout.isVisible) {
                if (commentState.isReply) {
                    processReplyComments(commentState.comment)
                } else {
                    commentAdapter!!.submitItem(commentState.comment, 0)
                    businessAdapter.updateCommentCount(postPosition)

                    if (commentAdapter!!.itemCount == 1) {
                        updateUI(false)
                    }
                }
            }

        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            swipeRefreshLayout.isRefreshing = isLoading
        }

        // 4. Observe pagination loading state
        viewModel.isLoadingMore.observe(viewLifecycleOwner) { isLoadingMore ->
            businessRecycleView.post {
                businessAdapter.setLoadingMore(isLoadingMore)
            }

            // Stop swipe refresh if it's active
            if (!isLoadingMore) {
                swipeRefreshLayout.isRefreshing = false
            }
        }

        // 5. Observe pagination availability
        viewModel.hasMoreData.observe(viewLifecycleOwner) { hasMore ->
            businessAdapter.setHasMoreData(hasMore)
        }


        // Observe error messages
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                showError(it)
                viewModel.clearError()
            }
        }

    }

    private fun processReplyComments(comment: Comment) {

        if (comment.contentType == "text") {
            val newReply = com.uyscuti.social.network.api.response.commentreply.allreplies.Comment(
                __v = comment.__v,
                _id = comment._id,
                author = getReliesAuthor(),
                content = comment.content!!,
                contentType = comment.contentType,
                createdAt = comment.createdAt,
                isLiked = false,
                likes = 0,
                commentId = commentId,
                updatedAt = comment.updatedAt,
            )

            commentToAddReplies?.replies?.add(0, newReply)

        } else if (comment.contentType == "image") {
            val newReply = com.uyscuti.social.network.api.response.commentreply.allreplies.Comment(
                __v = comment.__v,
                _id = comment._id,
                author = getReliesAuthor(),
                contentType = comment.contentType,
                createdAt = comment.createdAt,
                isLiked = false,
                likes = 0,
                commentId = commentId,
                updatedAt = comment.updatedAt,
                images = comment.images
            )

            commentToAddReplies?.replies?.add(0, newReply)

        } else if (comment.contentType == "gif") {
            val newReply = com.uyscuti.social.network.api.response.commentreply.allreplies.Comment(
                __v = comment.__v,
                _id = comment._id,
                author = getReliesAuthor(),
                contentType = comment.contentType,
                createdAt = comment.createdAt,
                isLiked = comment.isLiked,
                likes = comment.likes,
                commentId = commentId,
                updatedAt = comment.updatedAt,
                gifs = comment.gifs
            )

            commentToAddReplies?.replies?.add(0, newReply)

        } else if (comment.contentType == "video") {
            val newReply = com.uyscuti.social.network.api.response.commentreply.allreplies.Comment(
                __v = comment.__v,
                _id = comment._id,
                author = getReliesAuthor(),
                contentType = comment.contentType,
                createdAt = comment.createdAt,
                isLiked = comment.isLiked,
                likes = comment.likes,
                commentId = commentId,
                updatedAt = comment.updatedAt,
                duration = comment.duration,
                videos = comment.videos
            )

            commentToAddReplies?.replies?.add(0, newReply)
        } else if (comment.contentType == "audio") {
            val newReply = com.uyscuti.social.network.api.response.commentreply.allreplies.Comment(
                __v = comment.__v,
                _id = comment._id,
                author = getReliesAuthor(),
                contentType = comment.contentType,
                createdAt = comment.createdAt,
                isLiked = comment.isLiked,
                likes = comment.likes,
                commentId = commentId,
                updatedAt = comment.updatedAt,
                duration = comment.duration,
                audios = comment.audios,
                fileSize = comment.fileSize,
                fileType = comment.fileType,
                fileName = comment.fileName
            )

            commentToAddReplies?.replies?.add(0, newReply)
        } else if (comment.contentType == "docs") {
            val newReply = com.uyscuti.social.network.api.response.commentreply.allreplies.Comment(
                __v = comment.__v,
                _id = comment._id,
                author = getReliesAuthor(),
                contentType = comment.contentType,
                createdAt = comment.createdAt,
                isLiked = comment.isLiked,
                likes = comment.likes,
                commentId = commentId,
                updatedAt = comment.updatedAt,
                docs = comment.docs,
                fileSize = comment.fileSize,
                fileType = comment.fileType,
                fileName = comment.fileName,
                numberOfPages = comment.numberOfPages
            )

            commentToAddReplies?.replies?.add(0, newReply)
        }


        val replyCount = commentToAddReplies?.replyCount?.plus(1)
        commentToAddReplies?.replyCount = replyCount!!
        commentAdapter?.updateItem(commentPosition, commentToAddReplies)
    }

    private fun showError(message: String) {
        Snackbar.make(businessRecycleView, message, Snackbar.LENGTH_LONG)
            .setAction("Retry") {
                viewModel.loadMore()
            }
            .show()
    }

    private fun getReliesAuthor(): com.uyscuti.social.network.api.response.commentreply.allreplies.Author {
        val localSettings = requireActivity().getSharedPreferences("LocalSettings", MODE_PRIVATE)
        val profilePic = localSettings.getString("profile_pic", "").toString()

        val avatar = com.uyscuti.social.network.api.response.commentreply.allreplies.Avatar(
            "", "", url = profilePic
        )

        val account = com.uyscuti.social.network.api.response.commentreply.allreplies.Account(
            _id = "", avatar = avatar, "", LocalStorage.getInstance(requireActivity()).getUsername()
        )
        val author =
            com.uyscuti.social.network.api.response.commentreply.allreplies.Author(
                _id = "21", account = account, firstName = "", lastName = ""
            )

        return author
    }

    private fun registerVideoPickerLauncher() {
        // Register the launcher in onCreate
        videoPickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

                if (result.resultCode == RESULT_OK) {
                    val data = result.data

                    val videoPath = data?.getStringExtra("video_url")
                    val uriString = data?.getStringExtra("vUri")
                    val caption = data?.getStringExtra("caption") ?: ""
                    val vUri = Uri.parse(uriString)

                    val uri = Uri.parse(videoPath)

                    if (videoPath != null) {
                        Log.d("VideoPicker", "File path: $videoPath")
                        val durationString = getFormattedDuration(videoPath)
                        val file = File(videoPath)
                        Log.d("VideoPicker", "File path durationString: $durationString")

                        if (file.exists()) {
                            val fileSizeInBytes = file.length()
                            val fileSizeInKB = fileSizeInBytes / 1024
                            val fileSizeInMB = fileSizeInKB / 1024

                            val fileSizeInGB = fileSizeInMB / 1024 // Conversion from MB to GB

                            Log.d("VideoPicker", "File size: $fileSizeInMB MB")

                            if (fileSizeInGB.toInt() == 1) {
                                showToast(requireActivity(), "File size too large")
                            } else if (fileSizeInMB > 10) {
                                Log.d("VideoPicker", "File size: greater than $fileSizeInMB MB")
                                if (isReply) {
                                    uploadVideoComment(caption, videoPath, isReply)
                                } else {
                                    uploadVideoComment(caption, videoPath)
                                }
                            } else {
                                Log.d("VideoPicker", "File size: less than $fileSizeInMB MB")
                                if (isReply) {
                                    uploadVideoComment(caption, videoPath, isReply)
                                } else {
                                    uploadVideoComment(caption, videoPath)
                                }
                            }
                        }
                    }


                }
            }

    }

    private fun registerGifPickerLauncher() {
        gifsPickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                val data = result.data
                val gifUri = data?.getStringExtra("gifUri")
                Log.d(TAG, "Gif Uri $gifUri")

                if (gifUri!!.isNotEmpty()) {

                    val localUpdateId = generateRandomId()

                    if (isReply) {
                        businessPostsViewModel.addCommentReply(
                            commentId,
                            contentType = "gif",
                            localUpdateId = localUpdateId,
                            gif = gifUri,
                            isReply = isReply
                        )
                        isReply = false
                    } else {
                        businessPostsViewModel.addComment(
                            businessPostId,
                            contentType = "gif",
                            localUpdateId = localUpdateId,
                            gif = gifUri
                        )
                    }

                }

            }
    }

    private fun registerCameraLauncher() {
        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data

                // Get media type (photo or video)
                val mediaType = data?.getStringExtra(CameraActivity.EXTRA_MEDIA_TYPE)
                val mediaUri = data?.getStringExtra(CameraActivity.EXTRA_MEDIA_URI)
                val mediaPath = data?.getStringExtra(CameraActivity.EXTRA_MEDIA_PATH)

                when (mediaType) {
                    CameraActivity.MEDIA_TYPE_PHOTO -> {
                        Log.d(
                            "From Camera activity",
                            "Image url: $mediaUri \n Image path: $mediaPath"
                        )
                        //handlePhotoResult(mediaUri, mediaPath)
                    }

                    CameraActivity.MEDIA_TYPE_VIDEO -> {
                        Log.d(
                            "From Camera activity",
                            "Video url: $mediaUri \n Video path: $mediaPath"
                        )
                        //  handleVideoResult(mediaUri, mediaPath)
                    }
                }
            } else {
                Toast.makeText(requireActivity(), "Camera cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerAudioPickerLauncher() {
        audioPickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

                val data = result.data
                val audioPath = data?.getStringExtra("audio_url")
                val uriString = data?.getStringExtra("aUri")
                val caption = data?.getStringExtra("caption") ?: ""

                if (audioPath != null) {
                    Log.d("AudioPicker", "File path: $audioPath")
                    val durationString = getFormattedDuration(audioPath)
                    val fileName = getFileNameFromLocalPath(audioPath)

                    Log.d("AudioPicker", "File name: $fileName")
                    Log.d("AudioPicker", "durationString: $durationString")
//                        Log.d("AudioPicker", "reverseDurationString: $reverseDurationString")
                    val file = File(audioPath)

                    var fileSizeInBytes by Delegates.notNull<Long>()
                    var fileSizeInKB by Delegates.notNull<Long>()
                    var fileSizeInMB by Delegates.notNull<Long>()


                    fileSizeInBytes = file.length()
                    fileSizeInKB = fileSizeInBytes / 1024
                    fileSizeInMB = fileSizeInKB / 1024

                    if (isReply) {
                        uploadAudioComment(
                            file.absolutePath,
                            caption,
                            isReply1 = isReply,
                            fileType = file.extension
                        )
                    } else {
                        Log.d("AudioPicker", "Calling upload audio comment")
                        uploadAudioComment(
                            file.absolutePath,
                            caption,
                            isReply1 = isReply,
                            fileType = file.extension
                        )
                    }

                }
            }

    }

    private fun openDocPickerLauncher() {
        val intent = Intent(requireActivity(), DocumentsActivity::class.java)
        docsPickerLauncher.launch(intent)

    }

    @SuppressLint("SetTextI18n")
    private fun registerCategoryLauncher() {
        categoryLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                val data = result.data
                if (data != null) {
                    val doSearchForUser = data.getBooleanExtra("do_search", false)
                    if (doSearchForUser) {
                        query = data.getStringExtra("query_key").toString()
                        businessRecycleView.isVisible = false
                        emptyStateLayout.isVisible = false
                        viewModel.searchItemsImmediate(query)
                        categoryLayout.isVisible = true
                        categoryTextView.text = "Search for $query"

                    }
                }
            }
    }

    private fun openImagePicker() {
        val intent = Intent(requireActivity(), ImagesActivity::class.java)
        imagePickerLauncher.launch(intent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val callback = object : OnBackPressedCallback(true) {

            override fun handleOnBackPressed() {

                if (motionLayout.isVisible) {
                    toggleBusinessCommentBottomSheet()
                } else if(viewModel.isInSearchMode()) {
                    viewModel.clearSearch()
                    categoryTextView.text = ""
                    categoryLayout.isVisible = false
                    emptyStateLayout.isVisible = false
                }
                else {
                    isEnabled = false
                    requireActivity().onBackPressed()
                }
            }
        }
        profileDeferred = lifecycleScope.async {
            setUpBusinessProfile()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        chatManager.listener = this@BusinessFragment

        if (motionLayout.isVisible) {
            EventBus.getDefault().post(HideBottomNav(true))
        } else {
            EventBus.getDefault().post(ShowBottomNav(false))
        }
    }

    private fun showShimmer() {
        shimmerLayout.startShimmerAnimation()
        shimmerLayout.visibility = View.VISIBLE
    }

    private fun hideShimmer() {
        shimmerLayout.stopShimmerAnimation()
        shimmerLayout.visibility = View.GONE
    }

    private fun updateRecordWaveProgress(progress: Float) {

        CoroutineScope(Dispatchers.Main).launch {
            wave.progress = progress
//            currentComment?.progress = progress
            Log.d("updateWaveProgress", "updateWaveProgress: $progress")
        }
    }

    private val onRecordWaveRunnable = object : Runnable {
        override fun run() {
            try {
                if (!isOnRecordDurationOnPause) {
                    val currentPosition = player?.currentPosition?.toFloat()!!
                    updateRecordWaveProgress(currentPosition)
                }
                waveHandler.postDelayed(this, 20)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("Exception", "run: ${e.message}")
            }

        }
    }

    private fun startWaveRunnable() {
        try {
            waveHandler.removeCallbacks(waveRunnable)
            waveHandler.post(waveRunnable)
            isDurationOnPause = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startRecordWaveRunnable() {
        try {
            waveHandler.removeCallbacks(onRecordWaveRunnable)
            waveHandler.post(onRecordWaveRunnable)
            isOnRecordDurationOnPause = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopRecordWaveRunnable() {
        try {
            waveHandler.removeCallbacks(onRecordWaveRunnable)
            isOnRecordDurationOnPause = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopPlayingVn() {
        playVnAudioBtn.setImageResource(R.drawable.play_svgrepo_com)
        player?.release()
        player = null
        isAudioVNPlaying = false
        vnRecordAudioPlaying = false
        isOnRecordDurationOnPause = false
        stopRecordWaveRunnable()
        wave.progress = 0F
        vnRecordProgress = 0
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun resumeRecordingVn() {
        if (isPaused) {
            isVnResuming = true
            startRecordingVn() // Start a new recording session, appending to the previous file
            waveForm.visibility = View.VISIBLE
            timerTv.visibility = View.VISIBLE
            playAudioLayout.visibility = View.GONE
            playVnAudioBtn.setImageResource(R.drawable.play_svgrepo_com)
            recordVn.setImageResource(R.drawable.baseline_pause_black)
        }

    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun pauseRecordingVn() {
        val TAG = "pauseRecording"
//        firstTimeSendVn = true
        if (isRecording && !isPaused) {

            try {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
                mediaRecorder = null
            } catch (e: Exception) {
                Log.d(TAG, " failed to stop media recorder: $e")
                e.printStackTrace()
            }

            isPaused = true
            timer.pause() // Pause the recording timer
            timerTv.visibility = View.INVISIBLE
            waveForm.visibility = View.GONE
            playAudioLayout.visibility = View.VISIBLE
            playVnAudioBtn.setImageResource(R.drawable.play_svgrepo_com)
            recordVn.setImageResource(R.drawable.mic_2)


            Log.d(TAG, "pauseRecording: list of recordings  size: ${recordedAudioFiles.size}")
            Log.d(TAG, "pauseRecording: list of recordings $recordedAudioFiles")

            mixVN()
        }
    }

    private fun startPlayingVn(vnAudio: String) {
        playVnAudioBtn.setImageResource(R.drawable.baseline_pause_white_24)
        EventBus.getDefault().post(PauseShort(true))
//        player?.reset()
        isAudioVNPlaying = true
        vnRecordAudioPlaying = true

        isOnRecordDurationOnPause = false
        startRecordWaveRunnable()
        if (isAudioVNPaused) {
//            progressAnim.resume()
            Log.d("startPlaying", "(isAudioVNPaused)->vnRecordProgress $vnRecordProgress")

            if (vnRecordProgress != 0) {
                player?.seekTo(vnRecordProgress)
            }
            player?.start()
        } else {

            player = MediaPlayer().apply {
                try {
                    setDataSource(vnAudio)
//                inputStream.close()
                    prepare()
                    Log.d("startPlaying", "vnRecordProgress $vnRecordProgress")
                    if (vnRecordProgress != 0) {
                        player?.seekTo(vnRecordProgress)
                    }
                    start()
                    setOnCompletionListener {
                        // Playback completed, restart playback
                        isAudioVNPaused = false
                        stopPlayingVn()
                    }
                } catch (e: IOException) {
                    Log.e("MediaRecorder", "prepare() failed")
                }
            }

        }
    }

    private fun pauseVn(progress: Int) {
        Log.d("pauseVn", "vnRecordProgress $vnRecordProgress..... progress $progress")

        player?.pause()
        player?.seekTo(progress)
        isAudioVNPlaying = false
        isAudioVNPaused = true
        isOnRecordDurationOnPause = true

//        progressAnim.pause()
        playVnAudioBtn.setImageResource(R.drawable.play_svgrepo_com)
    }

    @SuppressLint("DefaultLocale")
    private fun inflateWave(outputVN: String) {

//        outputVnFile = outputVN

        val TAG = "inflateWave"
        Log.d("playVnAudioBtn", "inflateWave: outputvn $outputVN")

        val audioFile = File(outputVN)
        wave.visibility = View.VISIBLE
        playerTimerTv.visibility = View.VISIBLE
        Log.d(TAG, "render: does not start with http")
        //                audioDuration = 100L
        val file = File(outputVN)
        Log.d(TAG, "render: file $outputVN exists: ${file.exists()}")
        val locaAudioDuration = AudioDurationHelper.getLocalAudioDuration(outputVN)
        if (locaAudioDuration != null) {
            // Duration is available, do something with it
            //                    println("Audio duration: ${duration}ms")
            val minutes = (locaAudioDuration / 1000) / 60
            val seconds = (locaAudioDuration / 1000) % 60
            //                println("Audio duration: $minutes minutes $seconds seconds")
            thirdTimerTv.text = String.format("%02d:%02d", minutes, seconds)
        } else {
            // File does not exist or error retrieving duration
//            println("Unable to retrieve audio duration.")
            Log.e(TAG, "render: failed to retrieve audio duration")

        }


        //                Log.d(TAG, "render: file $audioUrl can execute: ${file.canExecute()}")

//        binding.wave.setSampleFrom(audioFile)
        CoroutineScope(Dispatchers.IO).launch {
            WaveFormExtractor.getSampleFrom(requireActivity().applicationContext, outputVN) {

                CoroutineScope(Dispatchers.Main).launch {
//                    binding.wave.progress = 0F
//                    binding.wave.progress = currentItem.progress

                    if (locaAudioDuration != null) {
                        wave.maxProgress = locaAudioDuration.toFloat()
                    }
                    wave.setSampleFrom(it)

                    wave.onProgressChanged = object : SeekBarOnProgressChanged {
                        override fun onProgressChanged(
                            waveformSeekBar: WaveformSeekBar,
                            progress: Float,
                            fromUser: Boolean
                        ) {
//                                    wave.progress = progress
                            secondTimerTv.text = String.format(
                                "%s",
                                TrimVideoUtils.stringForTime(progress)
                            )

//                            currentItem.progress = progress

                            if (fromUser) {
                                if (vnRecordAudioPlaying) {
                                    pauseVn(progress = progress.toInt())
                                } else {
                                    vnRecordProgress = progress.toInt()
                                    Log.d("FromUser", "Scroll to this $progress")
                                }

                            }
                        }

                        override fun onRelease(event: MotionEvent?, progress: Float) {
                            if (outputVN.isNotEmpty()) {
//                                inflateWave(outputVN)
                                if (vnRecordAudioPlaying) {
                                    Log.d(
                                        "onRelease",
                                        "vnRecordAudioPlaying $isAudioVNPlaying progress $progress"
                                    )
                                    vnRecordProgress = progress.toInt()
                                    startPlayingVn(outputVN)
                                } else {
                                    Log.d("onRelease", "Start playing from this progress $progress")
                                    vnRecordProgress = progress.toInt()
                                }

                            } else {
                                Log.d("onRelease", "output vn is empty")
                            }
                        }
                    }
                }
            }
        }

    }

    private fun mixVN() {
        val TAG = "mixVN"
        try {
            wasPaused = true
            Log.d(TAG, "pauseRecording: outputFile: $outputVnFile")

            val audioMixer = AudioMixer(outputVnFile)

            for (input in recordedAudioFiles) {
                val ai = GeneralAudioInput(input)
                audioMixer.addDataSource(ai)
            }
            audioMixer.mixingType = AudioMixer.MixingType.SEQUENTIAL

            audioMixer.setProcessingListener(object : AudioMixer.ProcessingListener {
                override fun onProgress(progress: Double) {
                    // Not used in this example, but you can handle progress updates if needed
                }

                override fun onEnd() {
                    requireActivity().runOnUiThread {
                        audioMixer.release()
                        mixingCompleted = true // Set the flag to indicate mixing is completed
                        // Additional code as needed
                        val file = File(outputVnFile)
                        Log.d(TAG, "onEnd: output vn file exists ${file.exists()}")
                        Log.d(TAG, "onEnd: media muxed success")

                        inflateWave(outputVnFile)

                        playVnAudioBtn.setOnClickListener {
                            Log.d("playVnAudioBtn", "onEnd: play vn button clicked")
                            when {
                                !isAudioVNPlaying -> {
                                    playVnAudioBtn.setImageResource(R.drawable.baseline_pause_black)
                                    Log.d(
                                        "playVnAudioBtn",
                                        "play vn"
                                    )
                                    startPlayingVn(outputVnFile)
                                }

                                else -> {
                                    Log.d(
                                        "playVnAudioBtn",
                                        "pause VN"
                                    )
                                    playVnAudioBtn.setImageResource(R.drawable.play_svgrepo_com)
                                    vnRecordAudioPlaying = true
                                    pauseVn(vnRecordProgress)
                                }
                            }
                        }
                    }
                }
            })

            try {
                audioMixer.start()
                audioMixer.processAsync()
            } catch (e: IOException) {
                audioMixer.release()
                e.printStackTrace()
                Log.d(TAG, "pauseRecording: exception 1 $e")
                Log.d(TAG, "pauseRecording: exception 1 ${e.message}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, "pauseRecording: exception 2 $e")
            Log.d(TAG, "pauseRecording: exception 2 ${e.message}")
        }
    }

    private fun startRecordingVn() {

        Log.d("StartRecording", "startRecoding vn called")
        try {

            if (player?.isPlaying == true) {
                stopPlayingVn()
            }

            playerTimerTv.visibility = View.GONE
            outputFile = getOutputFilePath("rec")
            outputVnFile = getOutputFilePath("mix")
            wasPaused = false

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setOutputFile(outputFile)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

//                setAudioSource(MediaRecorder.AudioSource.MIC)
//                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
//                setOutputFile(outputFile)
//                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

                prepare()
                start()
            }

            isRecording = true
            isPaused = false
            isVnResuming = false
            recordVn.setImageResource(R.drawable.baseline_pause_white_24)
            sendVn.setBackgroundResource(R.drawable.ic_ripple)
            deleteVn.setBackgroundResource(R.drawable.ic_ripple)
            timer.start()

            deleteVn.isClickable = true
            sendVn.isClickable = true
            recordedAudioFiles.add(outputFile)

            Log.d("VNFile", outputFile)

        } catch (e: Exception) {
            Log.d("VNFile", "Failed to record audio properly")
            e.printStackTrace()
        }
    }

    private fun deleteRecording() {

        val TAG = "Recording"

        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            isPaused = false
            isAudioVNPlaying = false

            timerTv.text = "00:00.00"
            secondTimerTv.visibility = View.GONE
            thirdTimerTv.visibility = View.GONE
//            binding.recordVN.setImageResource(R.drawable.baseline_pause_24)
            recordVn.setImageResource(R.drawable.mic_2)


            sendVn.setBackgroundResource(R.drawable.ic_ripple_disabled)
            sendVn.isClickable = false

            amplitudes = waveForm.clear()
            amps = 0
            timer.stop()
            Log.d("TAG", "deleteRecording: recorded files size ${recordedAudioFiles.size}")
            deleteVn()
//            if()
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle exceptions as needed
        }
    }

    private fun deleteVn() {
        recordedAudioFiles.clear()
//        if (recordedAudioFiles.isNotEmpty()) {
        val isDeleted = deleteFiles(recordedAudioFiles)
        var outputVnFileList = mutableListOf<String>()
        outputVnFileList.add(outputVnFile)
        val deleteMixVn = deleteFiles(outputVnFileList)
        if (isDeleted) {
            Log.d(TAG, "File record deleted successfully")
        } else {
            println("Failed to delete file.")
        }

        if (deleteMixVn) {
            Log.d(TAG, "File mix vn deleted successfully")
        } else {
            println("Failed to delete file.")
        }
//        }
    }

    private fun stopRecordingAndSendVn() {

        try {

            if (mediaRecorder != null) {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
                mediaRecorder = null
            }
            isRecording = false
            isPaused = false

            timerTv.text = "00:00.00"
            recordVn.setImageResource(R.drawable.ic_mic_on)
            sendVn.setBackgroundResource(R.drawable.ic_ripple_disabled)
            sendVn.isClickable = false

            amplitudes = waveForm.clear()
            amps = 0
            timer.stop()
            if (player?.isPlaying == true) {
                stopPlayingVn()
            }
            vnLayout.visibility = View.GONE

            // Add any UI changes or notifications indicating recording has stopped
            secondTimerTv.text = " 00:00"
            thirdTimerTv.text = "00:00"
            thirdTimerTv.visibility = View.GONE
            secondTimerTv.visibility = View.GONE
            replyToLayout.visibility = View.GONE


            if (!isReply) {

                if (recordedAudioFiles.size != 1) {
                    uploadAudioComment(outputVnFile, isReply1 = isReply, fileType = "vnAudio")
                } else {
                    uploadAudioComment(outputVnFile, isReply1 = isReply, fileType = "vnAudio")
                }
            } else {
                if (recordedAudioFiles.size != 1) {
                    uploadAudioComment(outputVnFile, isReply1 = isReply, fileType = "vnAudio")
                } else {
                    uploadAudioComment(outputVnFile, isReply1 = isReply, fileType = "vnAudio")
                }
            }


        } catch (e: Exception) {
            e.printStackTrace()
            // Handle exceptions as needed
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun handleOnClickListener() {

        deleteVn.setOnClickListener {
            if (mediaRecorder != null) {
                Log.d(TAG, "onCreate: media recorder not null")
            } else {
                Log.d(TAG, "onCreate: media recorder null")
            }
            lifecycleScope.launch(Dispatchers.Main) {
                delay(500)
                deleteRecording()
                sendVn.isClickable = true
            }
            if (player?.isPlaying == true) {
                stopPlayingVn()
            }
            vnLayout.visibility = View.GONE
        }

        recordVn.setOnClickListener {
            when {
                isPaused -> resumeRecordingVn()
                isRecording -> pauseRecordingVn()
                else -> {
                    if (ContextCompat.checkSelfPermission(
                            requireActivity(),
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        startRecordingVn()
                    } else {
                        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }

                }
            }
        }

        sendVn.setOnClickListener {
            sending = true
            CoroutineScope(Dispatchers.Main).launch {
                if (!wasPaused) {
                    timer.stop()
                    mediaRecorder?.apply {
                        stop()
                        release()
                    }
                    mediaRecorder = null
                    Log.d("SendVN", "When sending vn was paused was false")
                    mixVN() // Execute mixVN asynchronously
                }

                lifecycleScope.launch(Dispatchers.Main) {
                    delay(500)
                    stopRecordingAndSendVn()
                }

            }
        }

        click.setOnClickListener {
            toggleBusinessCommentBottomSheet()
            EventBus.getDefault().post(ShowBottomNav(true))
        }

        category.setOnClickListener {
            val intent = Intent(requireActivity(), CategoryActivity::class.java)
            categoryLauncher.launch(intent)
        }


        sellerButton.setOnClickListener {

            viewLifecycleOwner.lifecycleScope.launch {
                profileDeferred?.let { deffered ->

                    if (deffered.isCompleted) {
                        val hasProfile = deffered.await()
                        hasBusinessProfile = hasProfile

                        if (hasBusinessProfile) {
                            val intent = Intent(
                                requireContext(),
                                CreateCatalogueActivity::class.java
                            )

                            requireActivity().startActivityForResult(intent, 111)

                        } else {

                            val dialog = BusinessProfileDialogFragment.newInstance()
                            dialog.setListener(object :
                                BusinessProfileDialogFragment.BusinessProfileDialogListener {

                                override fun onCreateProfile() {
                                    bottomNavController?.navigateToChildFragments(1)
                                    dialog.dismiss()
                                }
                            })
                            dialog.show(childFragmentManager, "BusinessProfileDialog")
                            Log.d("HasBusinessProfile", "$hasProfile")
                        }
                    }

                }
            }

        }
    }

    private fun navigateToItemDetail(item: Post) {
        // Navigate to item detail screen
        val intent = Intent(requireActivity(), CatalogueDetailsActivity::class.java)
        intent.putExtra("catalogue", item)
        requireActivity().startActivity(intent)
    }

    private fun followBusinessPostOwner(post: Post) {

        viewLifecycleOwner.lifecycleScope.launch {
            if (NetworkUtil.isConnected(requireActivity())) {
                viewModel.refreshCatalogue()
                val success = businessPostsViewModel.followUnfollowBusinessPostOwner(post.owner)
                if (success) {
                    withContext(Dispatchers.Main) {
                        showToast(
                            requireActivity(),
                            "You have started following ${post.userDetails.username}"
                        )
                    }
                }
            }
        }
    }

    private fun bookmarkBusinessPost(data: Post) {
        if (NetworkUtil.isConnected(requireActivity())) {
            businessPostsViewModel.bookmarkUnBookmarkBusinessPost(data._id)
        }
    }

    override fun businessCommentClickedListener(
        position: Int,
        post: Post
    ) {
        businessPostId = post._id
        postPosition = position

        initCommentAdapter()

        toggleBusinessCommentBottomSheet()

        commentAdapter!!.setOnPaginationListener(object : AdPaginatedAdapter.OnPaginationListener {

            override fun onCurrentPage(page: Int) {
                Log.d(TAG, "currentPage: page number $page")
            }

            override fun onNextPage(page: Int) {
                lifecycleScope.launch(Dispatchers.Main) {
                    Log.d(TAG, "onNextPage: page number $page")
                    getBusinessComments(page)
                }
            }

            override fun onFinish() {
                Log.d(TAG, "finished: page number")
            }
        })

        lifecycleScope.launch(Dispatchers.Main) {
            getBusinessComments(commentAdapter!!.startPage)
        }

    }

    private fun showProgressBar() {
        recycleViewProgressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        recycleViewProgressBar.visibility = View.GONE
    }

    private fun getBusinessComments(page: Int) {

        lifecycleScope.launch(Dispatchers.IO) {

            withContext(Dispatchers.Main) {
                if (page == 1) {
                    showShimmer()
                } else {
                    showProgressBar()
                }
            }

            try {

                val commentsWithReplies =
                    businessPostsViewModel.getBusinessPostComments(businessPostId, page)
                withContext(Dispatchers.Main) {

                    if (page == 1) {
                        hideShimmer()
                    } else {
                        hideProgressBar()
                    }

                    commentAdapter!!.submitItems(commentsWithReplies)
                    if (commentsWithReplies.isEmpty()) {
                        updateUI(true)
                    } else {
                        updateUI(false)
                    }
                }

            } catch (e: Exception) {
                lifecycleScope.launch {

                    if (page == 1) {
                        hideShimmer()
                    } else {
                        hideProgressBar()
                    }
                }
                e.printStackTrace()
            }

        }
    }


    private fun updateUI(dataEmpty: Boolean) {
        if (dataEmpty) {
            commentRecyclerView.visibility = View.GONE
            placeholderLayout.visibility = View.VISIBLE
        } else {
            placeholderLayout.visibility = View.GONE
            commentRecyclerView.visibility = View.VISIBLE
        }
    }

    override fun onSubmit(input: CharSequence?): Boolean {
        hideKeyboard(textInput.inputEditText)
        val localUpdateId = generateRandomId()

        if (!isReply) {
            businessPostsViewModel.addComment(
                businessPostId,
                input.toString(),
                "text",
                localUpdateId
            )

        } else {
            businessPostsViewModel.addCommentReply(
                commentId,
                input.toString(),
                "text",
                localUpdateId,
                isReply = isReply
            )

            isReply = false
        }

        return true
    }

    private fun initEmojiView() {
        if (emojiShowing) {
            emojiPopup.dismiss()
            // Show keyboard after a slight delay to ensure smooth transition
            textInput.inputEditText?.postDelayed({
                inputMethodManager.showSoftInput(
                    textInput.inputEditText, InputMethodManager.SHOW_IMPLICIT
                )
            }, 50)
            emojiShowing = false
        } else {
            // Hide keyboard first
            inputMethodManager.hideSoftInputFromWindow(
                textInput.inputEditText?.windowToken, 0
            )
            // Show emoji popup after a slight delay
            textInput.inputEditText?.postDelayed({
                emojiPopup.toggle()
            }, 50)
            emojiShowing = true
        }
    }

    override fun onAddEmoji() {
        initEmojiView()
    }

    override fun onAddVoiceNote() {
        vnLayout.visibility = View.VISIBLE
        playAudioLayout.visibility = View.GONE
        waveForm.visibility = View.VISIBLE
        timerTv.visibility = View.VISIBLE
    }

    override fun onAddGif() {
        val intent = Intent(requireActivity(), GifActivity::class.java)
        gifsPickerLauncher.launch(intent)
    }

    override fun onAddAttachments() {
        showAttachmentDialog()
    }

    private fun hideKeyboard(view: View) {
        val imm = requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun showAttachmentDialog() {
        val dialog = BottomSheetDialog(requireActivity())

        dialog.setContentView(R.layout.file_upload_dialog)

        val video = dialog.findViewById<LinearLayout>(R.id.upload_video)
        val audio = dialog.findViewById<LinearLayout>(R.id.upload_audio)
        val image = dialog.findViewById<LinearLayout>(R.id.upload_image)
        val camera = dialog.findViewById<LinearLayout>(R.id.open_camera)
        val doc = dialog.findViewById<LinearLayout>(R.id.upload_doc)
        val location = dialog.findViewById<LinearLayout>(R.id.share_location)

        val dialogView =
            dialog.findViewById<View>(R.id.design_bottom_sheet)
        dialogView?.startAnimation(AnimationUtils.loadAnimation(requireActivity(), R.anim.slide_up))

        val selectableItemBackground = TypedValue()

        image?.context?.theme?.resolveAttribute(
            android.R.attr.selectableItemBackground, selectableItemBackground, true
        )
        image?.setBackgroundResource(selectableItemBackground.resourceId)


        video?.context?.theme?.resolveAttribute(
            android.R.attr.selectableItemBackground, selectableItemBackground, true
        )
        video?.setBackgroundResource(selectableItemBackground.resourceId)


        audio?.context?.theme?.resolveAttribute(
            android.R.attr.selectableItemBackground, selectableItemBackground, true
        )
        audio?.setBackgroundResource(selectableItemBackground.resourceId)


        camera?.context?.theme?.resolveAttribute(
            android.R.attr.selectableItemBackground, selectableItemBackground, true
        )
        camera?.setBackgroundResource(selectableItemBackground.resourceId)


        doc?.context?.theme?.resolveAttribute(
            android.R.attr.selectableItemBackground, selectableItemBackground, true
        )
        doc?.setBackgroundResource(selectableItemBackground.resourceId)

        location?.context?.theme?.resolveAttribute(
            android.R.attr.selectableItemBackground, selectableItemBackground, true
        )
        location?.setBackgroundResource(selectableItemBackground.resourceId)

        image!!.setOnClickListener {
            openImagePicker()
            dialog.dismiss()
        }

        video!!.setOnClickListener {
            val intent = Intent(requireActivity(), VideosActivity::class.java)
            videoPickerLauncher.launch(intent)
            dialog.dismiss()
        }

        audio!!.setOnClickListener {
            val intent = Intent(requireActivity(), AudioActivity::class.java)
            audioPickerLauncher.launch(intent)
            dialog.dismiss()
        }

        doc?.setOnClickListener {
            openDocPickerLauncher()
            dialog.dismiss()
        }

        camera!!.setOnClickListener {
            val intent = Intent(requireActivity(), CameraActivity::class.java)
            cameraLauncher.launch(intent)
            dialog.dismiss()
        }



        location?.visibility = View.INVISIBLE
        dialog.show()

    }

    private fun toggleBusinessCommentBottomSheet() {
        val currentVisibility = motionLayout.visibility

        if (currentVisibility == View.VISIBLE) {
            motionLayout.visibility = View.GONE
            vnLayout.visibility = View.GONE

            replyToLayout.visibility = View.GONE
            textInput.inputEditText.setText("")
            placeholderLayout.visibility = View.GONE

            deleteRecording()
            stopPlayingVn()
            commentAudioStop()
            stopWaveRunnable()
            stopRecordWaveRunnable()
            exoPlayer?.release()

            EventBus.getDefault().post(ShowBottomNav(true))

        } else {
            EventBus.getDefault().post(HideBottomNav(true))
            motionLayout.visibility = View.VISIBLE
            motionLayout.transitionToStart()
        }

    }

    override fun onViewRepliesClick(
        data: Comment,
        repliesRecyclerView: RecyclerView,
        position: Int
    ) {
        TODO("Not yet implemented")
    }

    override fun onViewRepliesClick(
        data: Comment,
        position: Int,
        commentRepliesTV: TextView,
        hideCommentReplies: TextView,
        repliesRecyclerView: RecyclerView,
        isRepliesVisible: Boolean,
        page: Int
    ) {

        lifecycleScope.launch {

            if (data.hasNextPage) {

                withContext(Dispatchers.Main) {
                    commentRepliesTV.text = "Loading..."
                }


                if (commentRepliesTV.text.equals("Loading...")) {

                    withContext(Dispatchers.Main) {
                        hideCommentReplies.visibility = View.GONE
                    }

                    withContext(Dispatchers.Main) {
                        commentRepliesTV.visibility = View.GONE
                        hideCommentReplies.visibility = View.VISIBLE
                    }
                }
            }
        }

    }

    private fun commentAudioStartPlaying(
        audio: String,
        audioPlayPauseBtn: ImageView,
        progress: Float,
        position: Int
    ) {

        EventBus.getDefault().post(PauseShort(true))
        isDurationOnPause = false

        if (isVnAudioToPlay) {
            startWaveRunnable()
        }

        audioPlayPauseBtn.setImageResource(R.drawable.baseline_pause_black)

        try {
            val file = File(audio)

            if (file.exists()) {
                // Local file playback
                val fileUrl = Uri.fromFile(file)
                exoPlayer = ExoPlayer.Builder(requireActivity()).build()

                Log.d("commentAudioStartPlaying", "commentAudioStartPlaying: Local file $fileUrl")

                val localFileUri = Uri.parse(fileUrl.toString())
                val mediaItem = MediaItem.fromUri(localFileUri)
                exoPlayer!!.setMediaItem(mediaItem)
            } else {
                // Server file playback
                Log.d("commentAudioStartPlaying", "commentAudioStartPlaying: server file $audio")

                val audioUri = Uri.parse(audio)
                Log.d("commentAudioStartPlaying", "audioUri $audioUri")
                val mediaItem = MediaItem.fromUri(audioUri)

                // Try playing with cache first
                try {
                    exoPlayer = buildExoPlayerWithCache(mediaItem)
                    Log.d("commentAudioStartPlaying", "Using cached playback")
                } catch (cacheException: Exception) {
                    Log.e(
                        "commentAudioStartPlaying",
                        "Cache error, clearing and retrying",
                        cacheException
                    )

                    // Clear corrupted cache
                    clearExoPlayerCache()

                    // Retry with fresh cache
                    try {
                        exoPlayer = buildExoPlayerWithCache(mediaItem)
                        Log.d("commentAudioStartPlaying", "Cache cleared, using fresh cache")
                    } catch (retryException: Exception) {
                        Log.e(
                            "commentAudioStartPlaying",
                            "Cache still failing, playing directly from server",
                            retryException
                        )

                        // Fallback to direct server playback without cache
                        exoPlayer = buildExoPlayerWithoutCache(mediaItem)
                        Log.d(
                            "commentAudioStartPlaying",
                            "Playing directly from server without cache"
                        )
                    }
                }
            }

            exoPlayer!!.prepare()
            exoPlayer!!.seekTo(progress.toLong())
            exoPlayer!!.playWhenReady = true
            exoPlayer!!.repeatMode = Player.REPEAT_MODE_OFF
            exoPlayer!!.addListener(playbackStateListener())
            exoPlayer!!.addListener(object : Player.Listener {
                @Deprecated("Deprecated in Java")
                override fun onPlayerStateChanged(
                    playWhenReady: Boolean,
                    playbackState: Int
                ) {
                    if (playbackState == Player.STATE_READY && exoPlayer!!.duration != C.TIME_UNSET) {
                        // Player ready
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    error.printStackTrace()

                    // Check if error is cache-related
                    if (isCacheError(error)) {
                        Log.e("commentAudioStartPlaying", "Cache error detected during playback")
                        clearExoPlayerCache()

                        // Retry without cache
                        try {
                            exoPlayer?.release()
                            exoPlayer = buildExoPlayerWithoutCache(MediaItem.fromUri(audio))
                            exoPlayer!!.prepare()
                            exoPlayer!!.seekTo(progress.toLong())
                            exoPlayer!!.playWhenReady = true
                            exoPlayer!!.addListener(playbackStateListener())
                            Log.d("commentAudioStartPlaying", "Retrying playback without cache")
                        } catch (e: Exception) {
                            showToast(requireActivity(), "Can't play this audio")
                        }
                    } else {
                        showToast(requireActivity(), "Can't play this audio")
                    }
                }
            })

            if (isReplyVnPlaying) {
                val handler = Handler()
                handler.postDelayed({
                    commentAdapter?.refreshMainComment(position)
                }, 200)
            }

        } catch (e: Exception) {
            Log.d("commentAudioStartPlaying", "commentAudioStartPlaying: error: ${e.message}")
            e.printStackTrace()
            showToast(requireActivity(), "Error playing audio")
        }
    }

    // Helper method to build ExoPlayer with cache
    private fun buildExoPlayerWithCache(mediaItem: MediaItem): ExoPlayer {
        httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)

        defaultDataSourceFactory = DefaultDataSourceFactory(
            requireActivity(), httpDataSourceFactory
        )

        cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(simpleCache!!)
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        val mediaSourceFactory: MediaSource.Factory =
            DefaultMediaSourceFactory(requireActivity())
                .setDataSourceFactory(cacheDataSourceFactory)

        val player = ExoPlayer.Builder(requireActivity())
            .setMediaSourceFactory(mediaSourceFactory)
            .build()

        val mediaSource = ProgressiveMediaSource.Factory(cacheDataSourceFactory)
            .createMediaSource(mediaItem)

        player.setMediaSource(mediaSource)
        return player
    }

    // Helper method to build ExoPlayer without cache (direct server playback)
    private fun buildExoPlayerWithoutCache(mediaItem: MediaItem): ExoPlayer {
        val directHttpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(30000)
            .setReadTimeoutMs(30000)

        val player = ExoPlayer.Builder(requireActivity()).build()

        val mediaSource = ProgressiveMediaSource.Factory(directHttpDataSourceFactory)
            .createMediaSource(mediaItem)

        player.setMediaSource(mediaSource)
        return player
    }

    // Helper method to check if error is cache-related
    private fun isCacheError(error: PlaybackException): Boolean {
        val cause = error.cause
        return cause is IllegalStateException ||
                cause?.cause is IllegalStateException ||
                error.message?.contains("cache", ignoreCase = true) == true ||
                cause?.message?.contains("SimpleCache", ignoreCase = true) == true
    }

    // Helper method to clear ExoPlayer cache
    private fun clearExoPlayerCache() {
        try {
            simpleCache?.release()

            val exoPlayerCacheDir = File(requireActivity().cacheDir, "exoplayer")
            if (exoPlayerCacheDir.exists()) {
                exoPlayerCacheDir.deleteRecursively()
                Log.d("clearExoPlayerCache", "Cache cleared successfully")
            }

            // Reinitialize cache
            val leastRecentlyUsedCacheEvictor =
                LeastRecentlyUsedCacheEvictor(1024 * 1024 * 1024) // 1GB
            val exoDatabaseProvider = ExoDatabaseProvider(requireActivity())
            exoPlayerCacheDir.mkdirs()
            simpleCache =
                SimpleCache(exoPlayerCacheDir, leastRecentlyUsedCacheEvictor, exoDatabaseProvider)

            Log.d("clearExoPlayerCache", "Cache reinitialized")
        } catch (e: Exception) {
            Log.e("clearExoPlayerCache", "Error clearing cache", e)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun audioWave(event: AudioPlayerHandler) {

        val TAG = "audioWave"

        audioFormWave = event.audioWave
//        event.audioWave.setSampleFrom(event.audioPath)
        audioDurationTVCount = event.leftDuration
        wavePosition = event.position
        Log.d(TAG, "audioWave: position $wavePosition ")


    }

    private fun commentAudioStop() {
        Log.d(
            "TAG",
            "commentAudioStop: Comment audio completed playing player is playing ${player?.isPlaying}"
        )

        Log.d("isDurationOnPause", " in comment audio stop isDurationOnPause is $isDurationOnPause")

        Log.d("commentAudioStop", "commentAudioStop: was reply playing $isReplyVnPlaying")

        if (isVnAudioToPlay) {
            if (::audioFormWave.isInitialized) {
                audioFormWave.progress = 0f
            }
            commentAdapter?.setSecondWaveFormProgress(0f, currentCommentAudioPosition)
            commentAdapter?.setReplySecondWaveFormProgress(0f, currentCommentAudioPosition)
        } else {
            commentAdapter?.setSecondSeekBarProgress(0f, currentCommentAudioPosition)
            commentAdapter?.setReplySecondSeekBarProgress(0f, currentCommentAudioPosition)
        }


        currentCommentAudioPosition = RecyclerView.NO_POSITION
        currentCommentAudioPath = ""
        commentAdapter?.resetAudioPlay()

        exoPlayer?.let { exoPlayer ->
            if (exoPlayer.isPlaying) {
                exoPlayer.stop()
            }
        }
    }

    private fun initializeSeekBar(exoPlayer: ExoPlayer) {
        audioSeekBar.max = exoPlayer.duration.toInt()
// Remove callbacks from the current handler, if any
        currentHandler?.removeCallbacksAndMessages(currentHandler)
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable {
            override fun run() {
                try {
                    Log.d(
                        "initializeSeekBar",
                        "Position $currentCommentAudioPosition is reply $isReplyVnPlaying"
                    )
                    if (!isVnAudioToPlay && exoPlayer.isPlaying) {

                        exoPlayer.let {
                            if (isReplyVnPlaying) {
                                commentAdapter!!.updateReplySeekBarProgress(
                                    it.currentPosition.toFloat(),
                                    audioSeekBar
                                )
                            } else {

                                CoroutineScope(Dispatchers.Main).launch {
                                    Log.d(
                                        TAG,
                                        "Position: ${it.currentPosition}, progress: ${audioSeekBar.progress}"
                                    )
                                    audioSeekBar.progress = it.currentPosition.toInt()
                                    seekBarProgress = it.currentPosition.toFloat()
                                    commentAdapter!!.setSecondSeekBarProgress(
                                        seekBarProgress,
                                        currentCommentAudioPosition
                                    )
                                    audioDurationTVCount.text = String.format(
                                        "%s",
                                        TrimVideoUtils.stringForTime(it.currentPosition.toFloat())
                                    )
                                }

                            }

                            handler.postDelayed(this, 1000)

                        }
                    }
                } catch (e: Exception) {
                    audioSeekBar.progress = 0
                    e.printStackTrace()
                }
            }
        }, 0)
        // Set the new handler as the current handler
        currentHandler = handler
    }

    private val waveRunnable = object : Runnable {
        override fun run() {
//            Log.d("isDurationOnPause" , " in comment audio runnable isDurationOnPause is $isDurationOnPause")
            if (!isDurationOnPause) {
                val currentPosition = exoPlayer?.currentPosition?.toFloat()!!

                Log.d("ExoPlayerPosition", "Current Position: $currentPosition")

                waveProgress = currentPosition
                if (isReplyVnPlaying) {
                    commentAdapter!!.updateReplyWaveProgress(currentPosition, audioFormWave)
                } else {
                    commentAdapter!!.updateWaveProgress(currentPosition, wavePosition)
                }
                audioDurationTVCount.text = String.format(
                    "%s",
                    TrimVideoUtils.stringForTime(currentPosition)
                )
            }
            waveHandler.postDelayed(this, 20)
        }
    }

    private fun stopWaveRunnable() {
        try {
            waveHandler.removeCallbacks(waveRunnable)
            isDurationOnPause = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun commentAudioPause(audioPlayPauseBtn: ImageView, isReply: Boolean) {
        Log.d("TAG", "commentAudioPause: is Reply $isReply")
        isDurationOnPause = true

        Log.d(
            "isDurationOnPause",
            " in comment audio pause isDurationOnPause is $isDurationOnPause"
        )

        audioPlayPauseBtn.setImageResource(R.drawable.play_svgrepo_com)
        commentAdapter!!.updatePlaybackButton(
            currentCommentAudioPosition,
            isReply,
            audioPlayPauseBtn
        )
        exoPlayer?.pause()
    }

    private fun playbackStateListener() = object : Player.Listener {
        @SuppressLint("SetTextI18n")
        override fun onPlaybackStateChanged(state: Int) {
            when (state) {
                ExoPlayer.STATE_ENDED -> {
//                     The video playback ended. Move to the next video if available.
                    Log.d(
                        "playbackStateListener",
                        "commentAudioStartPlaying: comment audio completed"
                    )
//                    audioPlayPauseBtn.setImageResource(R.drawable.play_svgrepo_com)
                    if (isVnAudioToPlay) {
                        if (::audioDurationTVCount.isInitialized) {
                            audioDurationTVCount.text = "00:00"
                            commentAdapter?.updateReplyWaveProgress(0f, audioFormWave)
                            if (isReplyVnPlaying) {
                                Log.d("isReplyVnPlaying", "isReplyVnPlaying $isReplyVnPlaying")
                                val handler = Handler()

                                handler.postDelayed({
                                    commentAdapter?.refreshMainComment(position)
                                }, 200)
                            } else {
                                Log.d("isReplyVnPlaying", "isReplyVnPlaying $isReplyVnPlaying")
                            }
                        }
                    }

                    if (isVnAudioToPlay) {
                        audioFormWave.progress = 0f
                    } else {
                        if (::audioDurationTVCount.isInitialized) {
                            audioDurationTVCount.text = "00:00"
                        }
                        audioSeekBar.progress = 0
                        commentAdapter?.refreshAudioComment(currentCommentAudioPosition)
                    }

                    Log.d(
                        "audioSeekBar",
                        "currentCommentAudioPosition $currentCommentAudioPosition"
                    )

                    commentAdapter?.refreshMainComment(position)
                    commentAdapter?.changePlayingStatus()
//                    adapter?.resetWaveForm()
//                    adapter?.notifyDataSetChanged()
                    if (isVnAudioToPlay) {
                        stopWaveRunnable()

                    }
                    commentAudioStop()
                }
                // Add other cases if needed
                Player.STATE_BUFFERING -> {

                }

                Player.STATE_IDLE -> {
                }

                Player.STATE_READY -> {
                    if (!isVnAudioToPlay) {
                        exoPlayer?.let { initializeSeekBar(it) }
                    }
                    Log.d("TAG", "STATE_READY")
//                    startUpdatingSeekBar()
//                    shortsAdapter.setSeekBarProgress(exoPlayer!!.currentPosition.toInt())

                }

                else -> {
                    Log.d("TAG", "STOP SEEK BAR")
                    // Stop updating seek bar in other states
//                    stopUpdatingSeekBar()
                }
            }
        }

        override fun onIsPlayingChanged(isVideoPlaying: Boolean) {
//        super.onIsPlayingChanged(isPlaying)

        }

        override fun onEvents(player: Player, events: Player.Events) {
//        super.onEvents(player, events)
            if (events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED) ||
                events.contains(Player.EVENT_IS_PLAYING_CHANGED)
            ) {

//                progressBar.visibility = View.GONE
            }

            if (events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION)
            ) {
//                player.seekTo(5000L)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun commentAudioSeekBar(event: CommentAudioPlayerHandler) {
        val TAG = "commentAudioSeekBar"
        audioSeekBar = event.audioSeekBar
//        event.audioWave.setSampleFrom(event.audioPath)
        audioDurationTVCount = event.leftDuration
        seekPosition = event.position
        maxDuration = event.maxDuration
        Log.d(TAG, "commentAudioSeekBar: position $wavePosition ")
    }


    override fun toggleAudioPlayer(
        audioPlayPauseBtn: ImageView,
        audioToPlayPath: String,
        position: Int,
        isReply: Boolean,
        progress: Float,
        isSeeking: Boolean,
        seekTo: Boolean,
        isVnAudio: Boolean
    ) {
        isReplyVnPlaying = isReply
        isVnAudioToPlay = isVnAudio

        wavePosition = position
        currentCommentAudioPosition = position

        if (currentCommentAudioPath == audioToPlayPath) {

            if (seekTo) {
                Log.d("SeekTo", "Seek to $progress")
                EventBus.getDefault().post(PauseShort(true))
                isDurationOnPause = false
                exoPlayer?.seekTo(progress.toLong())
                exoPlayer?.play()
            } else if (isSeeking) {
                Log.d("toggleAudioPlayer", "user is seeking so i paused the audio")
                exoPlayer?.pause()
            } else if (exoPlayer?.isPlaying == true) {
                Log.d(
                    "toggleAudioPlayer",
                    "toggleAudioPlayer: current player is playing then pause"
                )

                if (isVnAudio) {
                    Log.d("waveProgress", "toggleAudioPlayer: $waveProgress")

                    commentAdapter?.setReplySecondWaveFormProgress(waveProgress, position)
                    commentAdapter?.setSecondWaveFormProgress(waveProgress, position)
                } else {
                    //for seek bar
                    commentAdapter?.setSecondSeekBarProgress(seekBarProgress, position)
                    commentAdapter?.setReplySecondSeekBarProgress(seekBarProgress, position)
                }
                exoPlayer?.pause()
                isDurationOnPause = true

            } else {
                Log.d(
                    "toggleAudioPlayer",
                    "toggleAudioPlayer: current player is not playing then play"
                )
                EventBus.getDefault().post(PauseShort(true))
                isDurationOnPause = false
                exoPlayer?.seekTo(progress.toLong())
                exoPlayer?.play()
            }
        } else {

            if (exoPlayer?.isPlaying == true) {
                Log.d("toggleAudioPlayer", "toggleAudioPlayer: in else player is playing")
                commentAudioPause(audioPlayPauseBtn, isReply)
            }

            commentAudioStartPlaying(audioToPlayPath, audioPlayPauseBtn, progress, position)

            currentCommentAudioPath = audioToPlayPath
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onReplyButtonClick(
        position: Int,
        data: Comment,
        isMainComment: Boolean
    ) {

        commentToAddReplies = data
        commentPosition = commentAdapter!!.findCommentPosition(data._id)
        var username = ""
        isReply = true

        if (isMainComment) {
            username = data.author!!.account.username
            replyToLayout.visibility = View.VISIBLE

            replyToTextView.text = "Replying to $username"
            commentId = data._id
        } else {

            username = data.replies[position].author!!.account.username

            replyToLayout.visibility = View.VISIBLE

            replyToTextView.text = "Replying to $username"
            commentId = data.replies[position]._id
        }

        textInput.inputEditText.setText("@$username")
        textInput.inputEditText.setSelection(textInput.inputEditText.text!!.length)

        exitReply.setOnClickListener {
            replyToLayout.visibility = View.GONE
            textInput.inputEditText.setText("")
            isReply = false
        }


    }

    override fun likeUnLikeComment(
        position: Int,
        data: Comment
    ) {
        val updatedComment = if (data.isLiked) {
            data.copy(
                likes = data.likes + 1,
            )
        } else {
            data.copy(
                likes = data.likes - 1,
            )
        }

        if (NetworkUtil.isConnected(requireActivity())) {
            commentAdapter?.updateItem(position, updatedComment)
            businessPostsViewModel.likeUnlikeBusinessComment(data._id)
        } else {
            showToast(requireActivity(), "Like failed. No internet access.")
        }

    }

    override fun likeUnlikeCommentReply(
        replyPosition: Int,
        replyData: com.uyscuti.social.network.api.response.commentreply.allreplies.Comment,
        mainCommentPosition: Int,
        mainComment: Comment
    ) {

        if (replyData.isLiked) {
            replyData.copy(
                likes = replyData.likes + 1
            )
        } else {
            replyData.copy(
                likes = replyData.likes - 1
            )
        }
        mainComment.replies[replyPosition] = replyData


        if (NetworkUtil.isConnected(requireActivity())) {
            commentAdapter?.updateItem(mainCommentPosition, mainComment)
            businessPostsViewModel.likeUnlikeBusinessCommentReplies(replyData._id)
        } else {
            showToast(requireActivity(), "Like failed. No internet access.")
        }
    }

    override fun onTimerTick(duration: String) {
        timerTv.text = duration

        var amplitude = mediaRecorder!!.maxAmplitude.toFloat()
        amplitude = if (amplitude > 0) amplitude else 130f

        waveForm.addAmplitude(amplitude)
    }

    private fun insertDialog(dialog: DialogEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            dialogViewModel.insertDialog(dialog)
        }
    }

    private fun addDialogInTheBackGround(user: User, lastMessage: String) {
        val singleUserList = arrayListOf(user)

        val message = Message(
            user.id,
            user,
            lastMessage,
            Date()
        )

        val tempDialog = Dialog(
            user.id,
            user.name,
            user.avatar,
            singleUserList,
            message,
            0
        )

        val messageId = "Text_${Random.Default.nextInt()}"

        val textMessage = MessageEntity(
            id = messageId,
            chatId = user.id,
            userName = "You",
            user = user.toUserEntity(),
            userId = localStorage.getUserId(),
            text = lastMessage,
            createdAt = System.currentTimeMillis(),
            imageUrl = null,
            voiceUrl = null,
            voiceDuration = 0,
            status = "Sending",
            videoUrl = null,
            audioUrl = null,
            docUrl = null,
            fileSize = 0
        )

        messageEntity = textMessage

        val dialogEntity = DialogEntity(
            id = tempDialog.dialogName,
            dialogPhoto = tempDialog.dialogPhoto,
            dialogName = tempDialog.dialogName,
            users = listOf(user.toUserEntity()),
            lastMessage = textMessage,
            unreadCount = 0
        )

        insertDialog(dialogEntity)
    }

    private fun User.toUserEntity(): UserEntity {
        return UserEntity(
            id,
            name,
            avatar,
            lastSeen = Date(),
            true
        )
    }

    private suspend fun insertMessage(message: MessageEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            messageViewModel.insertMessage(message)
        }
    }

    override fun onDialogUpdated(newDialogId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = remoteMessageRepository.sendMessage(
                newDialogId,
                SendMessageRequest(content = offerMessage)
            )

            messageEntity?.chatId = newDialogId
            messageEntity?.status = "Sent"
            CoroutineScope(Dispatchers.IO).launch { insertMessage(messageEntity!!) }
            Log.d("Catalogue", "Chat: $newDialogId Message Result: $result")
        }
    }
}