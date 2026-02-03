package com.uyscuti.sharedmodule.shorts

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.uyscuti.sharedmodule.adapter.UriTypeAdapter
import com.uyscuti.sharedmodule.databinding.ActivityUploadShortsBinding
import com.uyscuti.sharedmodule.model.CancelShortsUpload
import com.uyscuti.sharedmodule.model.ProgressEvent
import com.uyscuti.sharedmodule.model.UploadSuccessful
import com.uyscuti.sharedmodule.model.feed.multiple_files.FeedMultipleVideos
import com.uyscuti.sharedmodule.model.feed.multiple_files.MixedFeedUploadDataClass
import com.uyscuti.sharedmodule.utils.AudioDurationHelper.getFormattedDuration
import com.uyscuti.sharedmodule.utils.generateRandomId
import com.uyscuti.sharedmodule.utils.getFileNameFromLocalPath
import com.uyscuti.sharedmodule.utils.getFilePathFromContentUri
import com.uyscuti.sharedmodule.utils.getFilePathFromUri
import com.uyscuti.sharedmodule.utils.getFileSizeFromUri
import com.uyscuti.sharedmodule.utils.getRealPathFromUri
import com.uyscuti.sharedmodule.viewmodels.feed.GetFeedViewModel
import com.uyscuti.social.compressor.CompressionListener
import com.uyscuti.social.compressor.VideoCompressor
import com.uyscuti.social.compressor.VideoQuality
import com.uyscuti.social.compressor.config.Configuration
import com.uyscuti.social.compressor.config.SaveLocation
import com.uyscuti.social.compressor.config.SharedStorageConfiguration
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class UploadShortsActivity : AppCompatActivity(), VideoThumbnailAdapter.ThumbnailClickListener {

    private lateinit var binding: ActivityUploadShortsBinding
    private lateinit var videoUri: Uri
    private lateinit var caption: String
    private lateinit var thumbnail: Bitmap
    private val uris = mutableListOf<Uri>()

    private var imagePickLauncher: ActivityResultLauncher<Intent>? = null

    // CRITICAL: Track upload state
    private var uploadWorkRequest: OneTimeWorkRequest? = null
    private var isCompressing = false
    private var isUploading = false
    private var hasStartedUpload = false

    @Inject
    lateinit var retrofitIns: RetrofitInstance

    private var isThumbnailSelected = false
    private var isThumbnailClicked = false
    private val getFeedViewModel: GetFeedViewModel by viewModels()

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadShortsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Register EventBus
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        // Enable the Up button for back navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        videoUri = intent.getParcelableExtra(EXTRA_VIDEO_URI)!!

        imagePickLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val data = result.data
                    if (data != null && data.data != null) {
                        val selectedImageUri: Uri = data.data!!

                        isThumbnailClicked = true

                        // Load the bitmap from the URI
                        val bitmap = loadBitmapFromUri(selectedImageUri)

                        Glide.with(this)
                            .load(selectedImageUri)
                            .into(binding.shortThumbNail)
                        thumbnail = bitmap
                    }
                }
            }

        // Display the video using Glide and VideoView
        videoUri.let {
            Glide.with(this)
                .load(it)
                .into(binding.shortThumbNail)
        }

        binding.postButton.setOnClickListener {
            if (isThumbnailClicked) {
                uploadThumbnail()
            } else {
                setFirstFrameAsThumbnail()
            }
        }

        binding.topicsLayout.setOnClickListener {
            val intent = Intent(this@UploadShortsActivity, TopicsActivity::class.java)
            startActivityForResult(intent, REQUEST_TOPICS_ACTIVITY)
        }

        // CRITICAL: Setup cancel button
        setupCancelButton()

        GlobalScope.launch(Dispatchers.IO) {
            val videoThumbnails = extractThumbnailsFromVideos()

            withContext(Dispatchers.Main) {
                setupRecyclerView(videoThumbnails)
            }
        }

        binding.interactionsBox.setOnClickListener {
            ImagePicker.with(this).cropSquare().compress(512).maxResultSize(512, 512)
                .createIntent { intent: Intent ->
                    imagePickLauncher?.launch(intent)
                }
        }
    }

    // CRITICAL: Proper cancel button setup
    private fun setupCancelButton() {
        binding.cancelButton.setOnClickListener {
            if (hasStartedUpload) {
                // Show confirmation dialog if upload has started
                showCancelConfirmationDialog()
            } else {
                // Just finish if no upload has started
                finish()
            }
        }
    }

    // CRITICAL: Show confirmation dialog before cancelling
    private fun showCancelConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Cancel Upload")
            .setMessage("Are you sure you want to cancel the upload? Your progress will be lost.")
            .setPositiveButton("Yes, Cancel") { _, _ ->
                performCancel()
            }
            .setNegativeButton("No, Continue", null)
            .setCancelable(false)
            .show()
    }

    // CRITICAL: Comprehensive cancel handler
    private fun performCancel() {
        Log.d("CancelUpload", "===== CANCEL INITIATED =====")
        Log.d("CancelUpload", "isCompressing: $isCompressing")
        Log.d("CancelUpload", "isUploading: $isUploading")
        Log.d("CancelUpload", "uploadWorkRequest: ${uploadWorkRequest?.id}")

        var cancelledSomething = false

        // 1. Cancel compression if in progress
        if (isCompressing) {
            Log.d("CancelUpload", "Cancelling video compression...")
            try {
                VideoCompressor.cancel()
                isCompressing = false
                cancelledSomething = true
                Log.d("CancelUpload", "✓ Video compression cancelled")
            } catch (e: Exception) {
                Log.e("CancelUpload", "Error cancelling compression", e)
            }
        }

        // 2. Cancel upload work if in progress
        if (isUploading && uploadWorkRequest != null) {
            Log.d("CancelUpload", "Cancelling upload work: ${uploadWorkRequest!!.id}")
            try {
                val workManager = WorkManager.getInstance(applicationContext)
                workManager.cancelWorkById(uploadWorkRequest!!.id)
                isUploading = false
                cancelledSomething = true
                Log.d("CancelUpload", "✓ Upload work cancelled")
            } catch (e: Exception) {
                Log.e("CancelUpload", "Error cancelling upload work", e)
            }
        }

        // 3. Post cancellation event to notify other components
        try {
            EventBus.getDefault().post(CancelShortsUpload(cancel = true))
            Log.d("CancelUpload", "✓ CancelShortsUpload event posted")
        } catch (e: Exception) {
            Log.e("CancelUpload", "Error posting cancel event", e)
        }

        // 4. Reset state
        hasStartedUpload = false
        isCompressing = false
        isUploading = false

        // 5. Show feedback
        val message = if (cancelledSomething) {
            "Upload cancelled successfully"
        } else {
            "Upload cancelled"
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

        Log.d("CancelUpload", "===== CANCEL COMPLETED =====")

        // 6. Finish activity
        finish()
    }

    private fun loadBitmapFromUri(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        } else {
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        }
    }

    private fun setFirstFrameAsThumbnail() {
        val firstFrame: Bitmap? = VideoUtils.getFirstFrame(this, videoUri)
        if (firstFrame != null) {
            thumbnail = firstFrame
        }
        caption = binding.editTextText.text.toString().trim()
        uploadShorts(videoUri, caption)
    }

    private fun uploadThumbnail() {
        caption = binding.editTextText.text.toString().trim()
        uploadShorts(videoUri, caption)
    }

    private suspend fun extractThumbnail(videoUrl: Uri): List<Bitmap>? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(this, videoUrl)

            val durationMs =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
                    ?: 0

            val frameIntervalMs = 1000L

            val thumbnails = mutableListOf<Bitmap>()

            for (timeMs in 0 until durationMs step frameIntervalMs) {
                val bitmap: Bitmap? = retriever.getFrameAtTime(
                    timeMs * 1000,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                )
                bitmap?.let { thumbnails.add(it) }
            }

            retriever.release()

            thumbnails
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun extractThumbnailsFromVideos(): List<Bitmap> {
        val videoUrls = listOf(videoUri)
        val thumbnails = mutableListOf<Bitmap>()

        for (videoUrl in videoUrls) {
            val thumbnail = extractThumbnail(videoUrl)
            thumbnail?.let { thumbnails.addAll(it) }
        }

        return thumbnails
    }

    private fun setupRecyclerView(videoThumbnails: List<Bitmap>) {
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val adapter = VideoThumbnailAdapter(videoThumbnails, this)

        binding.recyclerView2.layoutManager = layoutManager
        binding.recyclerView2.adapter = adapter
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_TOPICS_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                val selectedSubtopics = data?.getStringArrayListExtra("selectedSubtopics")
                val formattedSubtopics = selectedSubtopics?.joinToString(" ") { "#$it" }
                val currentText = binding.editTextText.text?.toString() ?: ""

                val updatedText = if (currentText.isEmpty()) {
                    formattedSubtopics ?: ""
                } else {
                    "$currentText \n\n$formattedSubtopics"
                }

                binding.editTextText.setText(updatedText)
            }
        }
    }

    private fun uploadShorts(videoUri: Uri, caption: String) {
        uris.add(videoUri)
        hasStartedUpload = true
        compressShorts()
    }

    @SuppressLint("SetTextI18n")
    private fun compressShorts() {
        val uniqueId = UniqueIdGenerator.generateUniqueId()
        Log.d("progress id", uniqueId)

        val uri = uris[0]
        val uriFileSize = getFileSizeFromUri(this, uri)

        val fileSizeInKB = uriFileSize?.div(1024)
        val fileSizeInMB = fileSizeInKB?.div(1024)
        val fileSizeInGb = fileSizeInMB?.div(1024)

        Log.d("uriFileSize", "uri.scheme ${uri.scheme} compressShorts:uriFileSize: $uriFileSize  fileSizeInKB $fileSizeInKB fileSizeInMB $fileSizeInMB fileSizeInGb $fileSizeInGb")

        val filePath = when (uri.scheme) {
            "content" -> getRealPathFromUri(this, uri) ?: getFilePathFromContentUri(this, uri)
            "file" -> getFilePathFromUri(uri)
            else -> null
        }

        Log.d("uriFileSize", "compressShorts: file path $filePath ")

        if (fileSizeInMB != null) {
            Log.d("uriFileSize", "compressShorts: file size in mb $fileSizeInMB")

            if (fileSizeInGb != null) {
                Log.d("uriFileSize", "compressShorts: file size in gb $fileSizeInGb")

                if(fileSizeInGb > 1) {
                    Toast.makeText(this, "File too large (max 1GB)", Toast.LENGTH_LONG).show()
                    hasStartedUpload = false
                } else if(fileSizeInMB <= 10) {
                    Log.d("uriFileSize", "compressShorts: less than 10mb - uploading directly")
                    // Upload directly without compression
                    uploadDirectly(filePath, uniqueId)
                } else {
                    Log.d("uriFileSize", "compressShorts: greater than 10mb - compressing first")
                    // Compress then upload
                    compressAndUpload(uniqueId)
                }
            }
        }
    }

    // CRITICAL: Upload without compression
    private fun uploadDirectly(filePath: String?, uniqueId: String) {
        if (filePath == null) {
            Toast.makeText(this, "Invalid file path", Toast.LENGTH_SHORT).show()
            return
        }

        val thumbnailFile = saveBitmapToFile(thumbnail, applicationContext)
        val thumbnailFilePath = thumbnailFile.absolutePath

        val fileId: String = generateRandomId()
        val feedShortsBusinessId: String = generateRandomId()

        isUploading = true

        uploadWorkRequest = OneTimeWorkRequestBuilder<ShortsUploadWorker>()
            .setInputData(
                Data.Builder()
                    .putString(ShortsUploadWorker.EXTRA_FILE_PATH, filePath)
                    .putString(ShortsUploadWorker.CAPTION, caption)
                    .putString(ShortsUploadWorker.FILE_ID, fileId)
                    .putString(ShortsUploadWorker.FEED_SHORTS_BUSINESS_ID, feedShortsBusinessId)
                    .putString(ShortsUploadWorker.THUMBNAIL, thumbnailFilePath)
                    .build()
            )
            .build()

        val workManager = WorkManager.getInstance(applicationContext)

        Log.d("Upload", "Enqueuing upload work request: ${uploadWorkRequest!!.id}")
        workManager.enqueue(uploadWorkRequest!!)

        observeWorkProgress(workManager)
    }

    // CRITICAL: Compress and upload with proper cancellation handling
    private fun compressAndUpload(uniqueId: String) {
        isCompressing = true

        lifecycleScope.launch {
            VideoCompressor.start(
                context = applicationContext,
                uris,
                isStreamable = true,
                sharedStorageConfiguration = SharedStorageConfiguration(
                    saveAt = SaveLocation.movies,
                    subFolderName = "flash_shorts"
                ),
                configureWith = Configuration(
                    quality = VideoQuality.MEDIUM,
                    videoNames = uris.map { uri -> uri.pathSegments.last() },
                    isMinBitrateCheckEnabled = false,
                ),
                listener = object : CompressionListener {
                    override fun onProgress(index: Int, percent: Float) {
                        if (percent <= 100 && isCompressing) {
                            EventBus.getDefault().post(
                                ProgressEvent(
                                    uniqueId,
                                    percent.toInt()
                                )
                            )
                            Log.d("Compress", "Compression progress: ${percent.toInt()}%")
                        }
                    }

                    override fun onStart(index: Int) {
                        Log.d("Compress", "Compression started for index: $index")
                        runOnUiThread {
                            Toast.makeText(
                                this@UploadShortsActivity,
                                "Compressing video...",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onSuccess(index: Int, size: Long, path: String?) {
                        isCompressing = false
                        Log.d("Compress", "✓ Compression successful")
                        Log.d("Compress", "Compressed file size: ${getFileSize(size)}")
                        Log.d("Compress", "Compressed file path: $path")

                        if (path != null) {
                            // Upload the compressed file
                            uploadCompressedFile(path)
                        } else {
                            runOnUiThread {
                                Toast.makeText(
                                    this@UploadShortsActivity,
                                    "Compression failed: Invalid path",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    override fun onFailure(index: Int, failureMessage: String) {
                        isCompressing = false
                        Log.e("Compress", "✗ Compression failed: $failureMessage")
                        runOnUiThread {
                            Toast.makeText(
                                this@UploadShortsActivity,
                                "Compression failed: $failureMessage",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onCancelled(index: Int) {
                        isCompressing = false
                        Log.w("Compress", "✗ Compression cancelled for index: $index")
                        runOnUiThread {
                            Toast.makeText(
                                this@UploadShortsActivity,
                                "Compression cancelled",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
            )
        }
    }

    // CRITICAL: Upload compressed file
    private fun uploadCompressedFile(path: String) {
        val thumbnailFile = saveBitmapToFile(thumbnail, applicationContext)
        val thumbnailFilePath = thumbnailFile.absolutePath

        val fileId: String = generateRandomId()
        val feedShortsBusinessId: String = generateRandomId()

        isUploading = true

        uploadWorkRequest = OneTimeWorkRequestBuilder<ShortsUploadWorker>()
            .setInputData(
                Data.Builder()
                    .putString(ShortsUploadWorker.EXTRA_FILE_PATH, path)
                    .putString(ShortsUploadWorker.CAPTION, caption)
                    .putString(ShortsUploadWorker.FILE_ID, fileId)
                    .putString(ShortsUploadWorker.FEED_SHORTS_BUSINESS_ID, feedShortsBusinessId)
                    .putString(ShortsUploadWorker.THUMBNAIL, thumbnailFilePath)
                    .build()
            )
            .build()

        val workManager = WorkManager.getInstance(applicationContext)

        Log.d("Upload", "Enqueuing upload work request: ${uploadWorkRequest!!.id}")
        workManager.enqueue(uploadWorkRequest!!)

        observeWorkProgress(workManager)
    }

    // CRITICAL: Centralized work progress observation with proper cancellation handling
    private fun observeWorkProgress(workManager: WorkManager) {
        lifecycleScope.launch(Dispatchers.Main) {
            Log.d("Progress", "Starting work progress observation")

            workManager.getWorkInfoByIdLiveData(uploadWorkRequest!!.id)
                .observe(this@UploadShortsActivity) { workInfo ->

                    if (workInfo != null) {
                        val progress = workInfo.progress.getInt(ShortsUploadWorker.Progress, 0)

                        Log.d("Progress", "Work state: ${workInfo.state}, Progress: $progress%")

                        when (workInfo.state) {
                            WorkInfo.State.ENQUEUED -> {
                                Log.d("Progress", "Work ENQUEUED")
                            }
                            WorkInfo.State.RUNNING -> {
                                Log.d("Progress", "Work RUNNING - Progress: $progress%")
                            }
                            WorkInfo.State.SUCCEEDED -> {
                                Log.d("Progress", "✓ Work SUCCEEDED")
                                isUploading = false
                                hasStartedUpload = false

                                Toast.makeText(
                                    this@UploadShortsActivity,
                                    "Upload successful!",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // Post success event
                                EventBus.getDefault().post(UploadSuccessful(success = true))

                                // Return result and finish
                                val resultIntent = Intent()
                                setResult(RESULT_OK, resultIntent)
                                finish()
                            }
                            WorkInfo.State.FAILED -> {
                                Log.e("Progress", "✗ Work FAILED")
                                isUploading = false
                                hasStartedUpload = false

                                Toast.makeText(
                                    this@UploadShortsActivity,
                                    "Upload failed. Please try again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            WorkInfo.State.BLOCKED -> {
                                Log.w("Progress", "Work BLOCKED")
                            }
                            WorkInfo.State.CANCELLED -> {
                                Log.w("Progress", "✗ Work CANCELLED")
                                isUploading = false
                                hasStartedUpload = false

                                Toast.makeText(
                                    this@UploadShortsActivity,
                                    "Upload cancelled",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        Log.w("Progress", "Work info is null")
                    }
                }
        }
    }

    private fun createGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(Uri::class.java, UriTypeAdapter())
            .create()
    }

    // CRITICAL: Handle cancel event from EventBus
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCancelShortsUploadEvent(event: CancelShortsUpload) {
        Log.d("CancelUpload", "Received CancelShortsUpload event with cancel=${event.cancel}")
        if (event.cancel) {
            performCancel()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
            Log.d("EventBus", "UploadShortsActivity unregistered from EventBus")
        }
    }

    override fun onBackPressed() {
        if (hasStartedUpload) {
            // Show confirmation if upload is in progress
            showCancelConfirmationDialog()
        } else {
            // Just finish if no upload
            super.onBackPressed()
        }
    }

    companion object {
        const val EXTRA_VIDEO_URI = "extra_video_uri"
        const val REQUEST_TOPICS_ACTIVITY = 123
    }

    override fun onThumbnailClick(thumbnail: Bitmap) {
        isThumbnailClicked = true
        Glide.with(this)
            .load(thumbnail)
            .into(binding.shortThumbNail)

        this.thumbnail = thumbnail
    }

    fun saveBitmapToFile(bitmap: Bitmap, context: Context): File {
        val fileDir = File(context.filesDir, "thumbnails")

        if (!fileDir.exists()) {
            fileDir.mkdirs()
        }

        val fileName = "thumbnail_${UUID.randomUUID()}.png"
        val file = File(fileDir, fileName)

        try {
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
            Log.d("Thumbnail", "Thumbnail saved: ${file.absolutePath}")
        } catch (e: IOException) {
            Log.e("Thumbnail", "Error saving thumbnail", e)
            e.printStackTrace()
        }

        return file
    }

    private fun getFileSize(size: Long): String {
        val kb = size / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0

        return when {
            gb >= 1 -> String.format("%.2f GB", gb)
            mb >= 1 -> String.format("%.2f MB", mb)
            else -> String.format("%.2f KB", kb)
        }
    }
}