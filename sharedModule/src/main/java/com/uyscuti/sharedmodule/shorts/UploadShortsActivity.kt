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
import com.uyscuti.sharedmodule.model.feed.multiple_files.FeedMultipleVideos
import com.uyscuti.sharedmodule.model.feed.multiple_files.MixedFeedUploadDataClass
import com.uyscuti.sharedmodule.ui.feed.FeedUploadWorker
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
import javax.inject.Inject

@AndroidEntryPoint
class UploadShortsActivity : AppCompatActivity(), VideoThumbnailAdapter.ThumbnailClickListener {


    private lateinit var binding: ActivityUploadShortsBinding
    private lateinit var videoUri: Uri
    private lateinit var caption: String
    private lateinit var thumbnail: Bitmap
    private val uris = mutableListOf<Uri>()

    private var imagePickLauncher: ActivityResultLauncher<Intent>? = null

    private var uploadWorkRequest: OneTimeWorkRequest? = null

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


        EventBus.getDefault().register(this)


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
                        // Now 'bitmap' contains the selected image as a Bitmap
                        // Use the 'bitmap' as needed

                        // For example, set the bitmap in an ImageView

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
                // Execute function for when thumbnail is clicked
                // For example, upload logic...
                uploadThumbnail()
            } else {
                // Execute another function when thumbnail is not clicked
                // For example, set the first frame as the thumbnail
                setFirstFrameAsThumbnail()
            }


        }

        binding.topicsLayout.setOnClickListener {
            val intent = Intent(this@UploadShortsActivity, TopicsActivity::class.java)
            startActivityForResult(intent, REQUEST_TOPICS_ACTIVITY)

        }

        cancelShortsUpload()
        backFromShortsUpload()



        GlobalScope.launch(Dispatchers.IO) {
            val videoThumbnails = extractThumbnailsFromVideos()

            // Switch to the main thread to update the RecyclerView
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

    private fun loadBitmapFromUri(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT < 28) {
            // For versions before Android 9 (API level 28)
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        } else {
            // For Android 9 (API level 28) and above
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
        val resultIntent = Intent()
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun uploadThumbnail() {
        caption = binding.editTextText.text.toString().trim()
        uploadShorts(videoUri, caption)
        val resultIntent = Intent()
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private suspend fun extractThumbnail(videoUrl: Uri): List<Bitmap>? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(this, videoUrl)

            // Get the duration of the video in milliseconds
            val durationMs =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
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

    private suspend fun extractThumbnailsFromVideos(): List<Bitmap> {
        // Replace this with your actual implementation to extract thumbnails
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
                // Handle the result when TopicsActivity returns RESULT_OK
                // You can use data to retrieve any additional information passed back
                // For example, val resultValue = data?.getStringExtra("keyName")
                val selectedSubtopics = data?.getStringArrayListExtra("selectedSubtopics")


                val formattedSubtopics = selectedSubtopics?.joinToString(" ") { "#$it" }
                // Get the current text from the EditText
                val currentText = binding.editTextText.text?.toString() ?: ""


                // Set the formatted subtopics to the EditText

                val updatedText = if (currentText.isEmpty()) {
                    formattedSubtopics ?: ""
                } else {
                    "$currentText \n\n$formattedSubtopics"
                }

                // Set the updated text to the EditText
                binding.editTextText.setText(updatedText)

            } else {
                // Handle other result codes if needed
            }
        }
    }

    private fun uploadShorts(videoUri: Uri, caption: String) {

        uris.add(videoUri)


        // 1. compress shorts
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
                Log.d("uriFileSize", "compressShorts: file size in mb $fileSizeInGb")
                if(fileSizeInGb > 1) {
                    Toast.makeText(this, "File too large", Toast.LENGTH_LONG).show()
                } else if(fileSizeInMB <= 10) {
                    Log.d("uriFileSize", "compressShorts: less than 10mb ")

                    val thumbnailFile = saveBitmapToFile(thumbnail, applicationContext)
                    val thumbnailFilePath = thumbnailFile.absolutePath

                    val fileId:String = generateRandomId()
                    val feedShortsBusinessId:String = generateRandomId()

                    val durationString = filePath?.let { getFormattedDuration(it) }
                    val fileName = filePath?.let { getFileNameFromLocalPath(it) }
                    val mixedFeedUploadDataClass: MutableList<MixedFeedUploadDataClass> =
                        mutableListOf()
                    mixedFeedUploadDataClass.add(
                        MixedFeedUploadDataClass(
                            videos = FeedMultipleVideos(
                                videoPath = filePath!!,
                                videoDuration = durationString ?: "00:00",
                                fileName = fileName!!,
                                videoUri = uris[0].toString(),
                                thumbnail = thumbnail
                            ), fileTypes = "video", fileId = fileId
                        )
                    )
                    val words = caption.split("\\s+".toRegex())

                    val topics = mutableListOf<String>()
                    val nonTags = mutableListOf<String>()

                    for (word in words) {
                        if (word.startsWith("#")) {
                            // It's a tag
                            topics.add(word.substring(1)) // Remove the '#' and add to tags
                        } else {
                            // It's non-tag text
                            nonTags.add(word)
                        }
                    }
                    val tagS = mutableListOf<String>()
                    val content = nonTags.joinToString(" ")
                    val tagsString = topics.joinToString(", ")

                    val tags = if (tagsString.isNotEmpty()) {
                        tagsString.split(",").map { it.trim() }
                    } else {
                        tagS
                    }


                    uploadWorkRequest =
                        OneTimeWorkRequestBuilder<ShortsUploadWorker>()

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

                    var workManager = WorkManager.getInstance(applicationContext)

                    Log.d("Upload", "Enqueuing upload work request...")
                    workManager.enqueue(uploadWorkRequest!!)

                    lifecycleScope.launch(Dispatchers.Main) {
                        Log.d("Progress", "Progress ...scope")


                        workManager = WorkManager.getInstance(applicationContext)
                        workManager.getWorkInfoByIdLiveData(uploadWorkRequest!!.id)
                            .observe(this@UploadShortsActivity) { workInfo ->
                                Log.d("Progress", "Observer triggered!")
                                if (workInfo != null) {
                                    val progress =
                                        workInfo.progress.getInt(ShortsUploadWorker.Progress, 0)
                                    // Update your UI with the progress value
                                    Log.d("Progress", "Progress $progress")
                                } else {
                                    Log.d("Progress", "Work info is null")
                                }

                                if (workInfo?.state == WorkInfo.State.RUNNING) {
                                    // Access progress here
                                    Log.d("Progress", "Running")
                                }
                                if (workInfo?.state == WorkInfo.State.SUCCEEDED) {
                                    // Access progress here
                                    Log.d("Progress", "SUCCEEDED")
                                }
                                if (workInfo?.state == WorkInfo.State.ENQUEUED) {
                                    // Access progress here
                                    Log.d("Progress", "ENQUEUED")
                                }
                                if (workInfo?.state == WorkInfo.State.BLOCKED) {
                                    // Access progress here
                                    Log.d("Progress", "BLOCKED")
                                }

                                if (workInfo?.state == WorkInfo.State.CANCELLED) {
                                    // Access progress here
                                    Log.d("Progress", "CANCELLED")
                                }

                            }
                    }
                }
                else {
                    Log.d("uriFileSize", "compressShorts: greater than 10mb ")
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
                                    // In another class or file


                                    //Update UI
                                    if (percent <= 100) {

                                        EventBus.getDefault().post(
                                            ProgressEvent(
                                                uniqueId,
                                                percent.toInt()
                                            )
                                        )

                                    }
                                }

                                override fun onStart(index: Int) {



                                }

                                override fun onSuccess(index: Int, size: Long, path: String?) {

                                    Log.d("Compress", "short compress successful")
                                    Log.d("Compress", "short file size: ${getFileSize(size)}")
                                    Log.d("Compress", "short path: $path")
                                    val thumbnailFile = saveBitmapToFile(thumbnail, applicationContext)
                                    val thumbnailFilePath = thumbnailFile.absolutePath

                                    val fileId:String = generateRandomId()
                                    val feedShortsBusinessId:String = generateRandomId()

                                    val durationString = path?.let { getFormattedDuration(it) }
                                    val fileName = path?.let { getFileNameFromLocalPath(it) }
                                    val mixedFeedUploadDataClass: MutableList<MixedFeedUploadDataClass> =
                                        mutableListOf()
                                    mixedFeedUploadDataClass.add(
                                        MixedFeedUploadDataClass(
                                            videos = FeedMultipleVideos(
                                                videoPath = path!!,
                                                videoDuration = durationString ?: "00:00",
                                                fileName = fileName!!,
                                                videoUri = uris[0].toString(),
                                                thumbnail = thumbnail
                                            ), fileTypes = "video", fileId = fileId
                                        )
                                    )
                                    val words = caption.split("\\s+".toRegex())

                                    val topics = mutableListOf<String>()
                                    val nonTags = mutableListOf<String>()

                                    for (word in words) {
                                        if (word.startsWith("#")) {
                                            // It's a tag
                                            topics.add(word.substring(1)) // Remove the '#' and add to tags
                                        } else {
                                            // It's non-tag text
                                            nonTags.add(word)
                                        }
                                    }
                                    val tagS = mutableListOf<String>()
                                    val content = nonTags.joinToString(" ")
                                    val tagsString = topics.joinToString(", ")

                                    val tags = if (tagsString.isNotEmpty()) {
                                        tagsString.split(",").map { it.trim() }
                                    } else {
                                        tagS
                                    }
                                    
                                    uploadWorkRequest =
                                        OneTimeWorkRequestBuilder<ShortsUploadWorker>()

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

                                    var workManager = WorkManager.getInstance(applicationContext)

                                    Log.d("Upload", "Enqueuing upload work request...")
                                    workManager.enqueue(uploadWorkRequest!!)
                                    // Inside compressShorts function, after enqueueing the work request

                                    lifecycleScope.launch(Dispatchers.Main) {
                                        Log.d("Progress", "Progress ...scope")


                                        workManager = WorkManager.getInstance(applicationContext)
                                        workManager.getWorkInfoByIdLiveData(uploadWorkRequest!!.id)
                                            .observe(this@UploadShortsActivity) { workInfo ->
                                                Log.d("Progress", "Observer triggered!")
                                                if (workInfo != null) {
                                                    val progress =
                                                        workInfo.progress.getInt(ShortsUploadWorker.Progress, 0)
                                                    // Update your UI with the progress value
                                                    Log.d("Progress", "Progress $progress")
                                                } else {
                                                    Log.d("Progress", "Work info is null")
                                                }

                                                if (workInfo?.state == WorkInfo.State.RUNNING) {
                                                    // Access progress here
                                                    Log.d("Progress", "Running")
                                                }
                                                if (workInfo?.state == WorkInfo.State.SUCCEEDED) {
                                                    // Access progress here
                                                    Log.d("Progress", "SUCCEEDED")
                                                }
                                                if (workInfo?.state == WorkInfo.State.ENQUEUED) {
                                                    // Access progress here
                                                    Log.d("Progress", "ENQUEUED")
                                                }
                                                if (workInfo?.state == WorkInfo.State.BLOCKED) {
                                                    // Access progress here
                                                    Log.d("Progress", "BLOCKED")
                                                }

                                                if (workInfo?.state == WorkInfo.State.CANCELLED) {
                                                    // Access progress here
                                                    Log.d("Progress", "CANCELLED")
                                                }

                                            }
                                    }

                                }

                                override fun onFailure(index: Int, failureMessage: String) {
                                    Log.wtf("failureMessage", failureMessage)
                                }

                                override fun onCancelled(index: Int) {
                                    Log.wtf("TAG", "compression has been cancelled")
                                    // make UI changes, cleanup, etc
                                }

                            },

                            )
                    }
                }
            }

        }

    }

    private fun createGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(Uri::class.java, UriTypeAdapter())
            .create()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun uploadMixedFeed(
        mixedFiles: List<MixedFeedUploadDataClass>,
        content: String,
        tags: MutableList<String>,
        feedShortsBusinessId: String
    ) {


        var uploadWorkRequest: OneTimeWorkRequest? = null
        val gson = createGson()
        val tag = "uploadMixedFeed"
        val uploadDataJson = gson.toJson(mixedFiles)
        Log.d(tag, "all feed size: ${getFeedViewModel.getAllFeedData().size}")

        val inputData = Data.Builder()
            .putString("upload_data", uploadDataJson)
            .putString(FeedUploadWorker.CAPTION, content)
            .putString(FeedUploadWorker.FEED_SHORTS_BUSINESS_ID, feedShortsBusinessId)
            .putString(FeedUploadWorker.CONTENT_TYPE, "mixed_files")
            .putStringArray(FeedUploadWorker.TAGS, tags.toTypedArray())
            .build()

        try {
            GlobalScope.launch(Dispatchers.IO) {

                uploadWorkRequest = OneTimeWorkRequestBuilder<FeedUploadWorker>()
                    .setInputData(inputData)
                    .build()


                val workManager = WorkManager.getInstance(applicationContext)

                workManager.enqueue(uploadWorkRequest!!)

            }
        } catch (e: Exception) {
            Log.e(tag, "uploadVideoFeed: error because ${e.message}")
            e.printStackTrace()
        }
    }


    private fun backFromShortsUpload() {
        binding.cancelButton.setOnClickListener {
            finish()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onProgressEvent(event: CancelShortsUpload) {
        Log.d("CancelUpload", "Cancel Upload ${uploadWorkRequest!!.id}")
        VideoCompressor.cancel()


    }

    private fun cancelShortsUpload() {
        binding.cancelButton.setOnClickListener {
            finish()
        }
    }

    companion object {
        const val EXTRA_VIDEO_URI = "extra_video_uri"
        const val REQUEST_TOPICS_ACTIVITY = 123 // You can use any unique value

    }

    private fun addIdToFilePath(originalPath: String, id: String): String {
        val file = File(originalPath)
        val fileName = file.nameWithoutExtension
        val fileExtension = file.extension

        // Construct the new path with the ID inserted before the extension

        return "${file.parent}/$fileName$id.$fileExtension"
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onThumbnailClick(thumbnail: Bitmap) {

        isThumbnailClicked = true
        Glide.with(this)
            .load(thumbnail)
            .into(binding.shortThumbNail)


        this.thumbnail = thumbnail
        // Set the bitmap

    }

    fun saveBitmapToFile(bitmap: Bitmap, context: Context): File {
        // Get the directory for the app's private pictures directory.
        val fileDir = File(context.filesDir, "thumbnails")

        // Create the directory if it doesn't exist
        if (!fileDir.exists()) {
            fileDir.mkdirs()
        }

        // Create a unique filename for the thumbnail

        val fileName = "thumbnail.png"

        // Create the file object
        val file = File(fileDir, fileName)

        try {
            // Save the bitmap to the file
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return file
    }

}