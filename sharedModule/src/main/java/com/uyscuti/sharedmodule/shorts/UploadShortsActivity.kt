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
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.postDelayed
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
import com.uyscuti.sharedmodule.model.UploadSuccessful
import com.uyscuti.sharedmodule.model.ProgressEvent
import com.uyscuti.sharedmodule.model.feed.multiple_files.FeedMultipleVideos
import com.uyscuti.sharedmodule.model.feed.multiple_files.MixedFeedUploadDataClass
import com.uyscuti.sharedmodule.utils.AudioDurationHelper.getFormattedDuration
import com.uyscuti.sharedmodule.utils.generateRandomId
import com.uyscuti.sharedmodule.utils.getFileNameFromLocalPath
import com.uyscuti.sharedmodule.utils.getFilePathFromContentUri
import com.uyscuti.sharedmodule.utils.getFilePathFromUri
import com.uyscuti.sharedmodule.utils.getFileSizeFromUri
import com.uyscuti.sharedmodule.utils.getRealPathFromUri
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
import javax.inject.Inject


// In your events file
data class UploadStarted(val uniqueId: String)


@AndroidEntryPoint
class UploadShortsActivity : AppCompatActivity(), VideoThumbnailAdapter.ThumbnailClickListener {

    companion object {
        const val EXTRA_VIDEO_URI = "extra_video_uri"
        const val REQUEST_TOPICS_ACTIVITY = 123
    }

    @Inject
    lateinit var retrofitIns: RetrofitInstance

    private var isThumbnailClicked = false
    private lateinit var binding: ActivityUploadShortsBinding
    private lateinit var videoUri: Uri
    private lateinit var caption: String
    private lateinit var thumbnail: Bitmap
    private val uris = mutableListOf<Uri>()
    private var imagePickLauncher: ActivityResultLauncher<Intent>? = null
    private var uploadWorkRequest: OneTimeWorkRequest? = null
    private var currentUploadUniqueId: String? = null


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onProgressEvent(event: CancelShortsUpload) {
        Log.d("CancelUpload", "Received cancel event in UploadShortsActivity")

        // Cancel WorkManager
        uploadWorkRequest?.let { workRequest ->
            val workManager = WorkManager.getInstance(applicationContext)
            workManager.cancelWorkById(workRequest.id)
            Log.d("CancelUpload", "Cancelled WorkManager in Activity: ${workRequest.id}")
            uploadWorkRequest = null
        }

        // Cancel video compression
        VideoCompressor.cancel()
        Log.d("CancelUpload", "Cancelled VideoCompressor in Activity")

        // Finish activity immediately
        finish()
    }

    // Subscribe to upload success/failure events
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUploadComplete(event: UploadSuccessful) {
        Log.d("UploadActivity", "Upload completed: success=${event.success}")

        // Now finish the activity after upload completes
        val resultIntent = Intent()
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadShortsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        videoUri = intent.getParcelableExtra(EXTRA_VIDEO_URI)!!

        imagePickLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val data = result.data
                    if (data != null && data.data != null) {
                        val selectedImageUri: Uri = data.data!!
                        isThumbnailClicked = true
                        val bitmap = loadBitmapFromUri(selectedImageUri)
                        Glide.with(this)
                            .load(selectedImageUri)
                            .into(binding.shortThumbNail)
                        thumbnail = bitmap
                    }
                }
            }

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

        cancelShortsUpload()

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

    private fun uploadThumbnail() {
        caption = binding.editTextText.text.toString().trim()

        // Generate uniqueId FIRST
        currentUploadUniqueId = UniqueIdGenerator.generateUniqueId()

        // Post the upload started event IMMEDIATELY so UI can prepare
        EventBus.getDefault().post(UploadStarted(currentUploadUniqueId!!))

        // Start upload
        uploadShorts(videoUri, caption)

        // Pass the uniqueId back to ShotsFragment
        val resultIntent = Intent().apply {
            putExtra("upload_unique_id", currentUploadUniqueId)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun setFirstFrameAsThumbnail() {
        val firstFrame: Bitmap? = VideoUtils.getFirstFrame(this, videoUri)
        if (firstFrame != null) {
            thumbnail = firstFrame
        }
        caption = binding.editTextText.text.toString().trim()

        // Generate uniqueId FIRST
        currentUploadUniqueId = UniqueIdGenerator.generateUniqueId()

        // Post the upload started event IMMEDIATELY
        EventBus.getDefault().post(UploadStarted(currentUploadUniqueId!!))

        // Start upload
        uploadShorts(videoUri, caption)

        // Pass the uniqueId back
        val resultIntent = Intent().apply {
            putExtra("upload_unique_id", currentUploadUniqueId)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun uploadShorts(videoUri: Uri, caption: String) {
        uris.add(videoUri)
        // Don't generate uniqueId here - it's already generated above
        compressShorts(currentUploadUniqueId!!)
    }

    private fun loadBitmapFromUri(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        } else {
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        }
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

    @SuppressLint("SetTextI18n")
    private fun compressShorts(uniqueId: String) {
      
        Log.d("progress id", uniqueId)

        val uri = uris[0]
        val uriFileSize = getFileSizeFromUri(this, uri)
        val fileSizeInKB = uriFileSize?.div(1024)
        val fileSizeInMB = fileSizeInKB?.div(1024)
        val fileSizeInGb = fileSizeInMB?.div(1024)

        this.currentUploadUniqueId = uniqueId
        EventBus.getDefault().post(UploadStarted(uniqueId))
        Log.d("uriFileSize", "uri.scheme ${uri.scheme} compressShorts:uriFileSize: $uriFileSize  fileSizeInKB $fileSizeInKB fileSizeInMB $fileSizeInMB fileSizeInGb $fileSizeInGb")

        val filePath = when (uri.scheme) {
            "content" -> getRealPathFromUri(this, uri) ?: getFilePathFromContentUri(this, uri)
            "file" -> getFilePathFromUri(uri)
            else -> null
        }

        Log.d("uriFileSize", "compressShorts: file path $filePath ")

        if (fileSizeInMB != null && fileSizeInGb != null) {
            Log.d("uriFileSize", "compressShorts: file size in mb $fileSizeInMB")

            if (fileSizeInMB <= 10) {
                Log.d("uriFileSize", "compressShorts: less than 10mb - no compression needed")

                val thumbnailFile = saveBitmapToFile(thumbnail, applicationContext)
                val thumbnailFilePath = thumbnailFile.absolutePath
                val fileId: String = generateRandomId()
                val feedShortsBusinessId: String = generateRandomId()

                uploadWorkRequest =
                    OneTimeWorkRequestBuilder<ShortsUploadWorker>()
                        .setInputData(
                            Data.Builder()
                                .putString(ShortsUploadWorker.EXTRA_FILE_PATH, filePath)
                                .putString(ShortsUploadWorker.CAPTION, caption)
                                .putString(ShortsUploadWorker.FILE_ID, fileId)
                                .putString(
                                    ShortsUploadWorker.FEED_SHORTS_BUSINESS_ID,
                                    feedShortsBusinessId
                                )
                                .putString(ShortsUploadWorker.THUMBNAIL, thumbnailFilePath)
                                .putString(ShortsUploadWorker.UNIQUE_ID, uniqueId)
                                .build()
                        )
                        .build()

                var workManager = WorkManager.getInstance(applicationContext)
                Log.d("Upload", "Enqueuing upload work request...")
                workManager.enqueue(uploadWorkRequest!!)

            } else {
                Log.d("uriFileSize", "compressShorts: greater than 10mb - compression needed")

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
                                if (percent <= 100) {
                                    val compressionProgress = (percent / 2).toInt()
                                    EventBus.getDefault().post(
                                        ProgressEvent(uniqueId, compressionProgress)
                                    )
                                    Log.d("Compress", "Compression progress: $compressionProgress%")
                                }
                            }

                            override fun onStart(index: Int) {
                                Log.d("Compress", "Compression started")
                                EventBus.getDefault().post(ProgressEvent(uniqueId, 0))
                            }

                            override fun onSuccess(index: Int, size: Long, path: String?) {
                                Log.d("Compress", "Compression complete - starting upload")
                                Log.d("Compress", "short file size: ${getFileSize(size)}")
                                Log.d("Compress", "short path: $path")

                                EventBus.getDefault().post(ProgressEvent(uniqueId, 50))

                                val thumbnailFile = saveBitmapToFile(thumbnail, applicationContext)
                                val thumbnailFilePath = thumbnailFile.absolutePath
                                val fileId: String = generateRandomId()
                                val feedShortsBusinessId: String = generateRandomId()

                                uploadWorkRequest =
                                    OneTimeWorkRequestBuilder<ShortsUploadWorker>()
                                        .setInputData(
                                            Data.Builder()
                                                .putString(ShortsUploadWorker.EXTRA_FILE_PATH, path)
                                                .putString(ShortsUploadWorker.CAPTION, caption)
                                                .putString(ShortsUploadWorker.FILE_ID, fileId)
                                                .putString(ShortsUploadWorker.FEED_SHORTS_BUSINESS_ID, feedShortsBusinessId)
                                                .putString(ShortsUploadWorker.THUMBNAIL, thumbnailFilePath)
                                                .putString(ShortsUploadWorker.UNIQUE_ID, uniqueId)
                                                .build()
                                        )
                                        .build()

                                var workManager = WorkManager.getInstance(applicationContext)
                                Log.d("Upload", "Enqueuing upload work request...")
                                workManager.enqueue(uploadWorkRequest!!)

                                lifecycleScope.launch(Dispatchers.Main) {
                                    workManager = WorkManager.getInstance(applicationContext)
                                    workManager.getWorkInfoByIdLiveData(uploadWorkRequest!!.id)
                                        .observe(this@UploadShortsActivity) { workInfo ->
                                            if (workInfo != null) {
                                                val progress = workInfo.progress.getInt(ShortsUploadWorker.Progress, 0)
                                                Log.d("Progress", "Progress $progress")
                                            }

                                            when (workInfo?.state) {
                                                WorkInfo.State.RUNNING -> Log.d("Progress", "Running")
                                                WorkInfo.State.SUCCEEDED -> Log.d("Progress", "SUCCEEDED")
                                                WorkInfo.State.ENQUEUED -> Log.d("Progress", "ENQUEUED")
                                                WorkInfo.State.BLOCKED -> Log.d("Progress", "BLOCKED")
                                                WorkInfo.State.CANCELLED -> Log.d("Progress", "CANCELLED")
                                                else -> {}
                                            }
                                        }
                                }
                            }

                            override fun onFailure(index: Int, failureMessage: String) {
                                Log.wtf("failureMessage", failureMessage)
                                EventBus.getDefault().post(UploadSuccessful(success = false))
                            }

                            override fun onCancelled(index: Int) {
                                Log.wtf("TAG", "compression has been cancelled")
                                EventBus.getDefault().post(UploadSuccessful(success = false))
                            }
                        },
                    )
                }
            }
        }
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

    private fun cancelShortsUpload() {
        binding.cancelButton.setOnClickListener {
            Log.d("CancelUpload", "Cancel button in Activity clicked")

            uploadWorkRequest?.let { workRequest ->
                val workManager = WorkManager.getInstance(applicationContext)
                workManager.cancelWorkById(workRequest.id)
                uploadWorkRequest = null
            }

            VideoCompressor.cancel()
            EventBus.getDefault().post(CancelShortsUpload(cancel = true))
            EventBus.getDefault().post(UploadSuccessful(success = false))

            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        uploadWorkRequest?.let {
            WorkManager.getInstance(applicationContext).cancelWorkById(it.id)
        }
        VideoCompressor.cancel()

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
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

}