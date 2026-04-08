package com.uyscuti.sharedmodule

import android.content.ContentResolver
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.SeekBar
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.Glide
import com.uyscuti.sharedmodule.databinding.ActivityMediaCompositionBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.concurrent.TimeUnit

class MediaCompositionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMediaCompositionBinding
    private var mediaUri: Uri? = null
    private var mediaType: MediaType = MediaType.UNKNOWN
    private var mediaPlayer: MediaPlayer? = null
    private var exoPlayer: ExoPlayer? = null
    private var isPlaying = false

    enum class MediaType {
        IMAGE, VIDEO, AUDIO, DOCUMENT, UNKNOWN
    }

    companion object {
        const val EXTRA_MEDIA_URI = "MEDIA_URI"
        const val EXTRA_MEDIA_TYPE = "MEDIA_TYPE"
        const val EXTRA_CAPTION = "CAPTION"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMediaCompositionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get media URI from intent
        mediaUri = intent.getParcelableExtra(EXTRA_MEDIA_URI)
        val typeString = intent.getStringExtra(EXTRA_MEDIA_TYPE) ?: "UNKNOWN"
        mediaType = MediaType.valueOf(typeString)

        setupUI()
        setupListeners()
        loadMedia()
    }

    private fun setupUI() {
        // Hide all views initially
        binding.imagePreview.visibility = View.GONE
        binding.videoPreview.visibility = View.GONE
        binding.audioControls.visibility = View.GONE
        binding.documentPreview.visibility = View.GONE


    }

    private fun setupListeners() {
        binding.closeButton.setOnClickListener {
            finish()
        }

        binding.sendButton.setOnClickListener {
            sendMedia()
        }

        binding.captionInput.addTextChangedListener {
            // updateCaptionCounter()
        }

        binding.playPauseButton.setOnClickListener {
            togglePlayPause()
        }

        binding.audioSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun loadMedia() {
        mediaUri?.let { uri ->

            // Auto-detect media type if UNKNOWN
            if (mediaType == MediaType.UNKNOWN) {
                CoroutineScope(Dispatchers.IO).launch {
                    mediaType = detectMediaType(uri)

                    withContext(Dispatchers.Main) {
                        when (mediaType) {
                            MediaType.IMAGE -> loadImage(uri)
                            MediaType.VIDEO -> loadVideo(uri)
                            MediaType.AUDIO -> loadAudio(uri)
                            MediaType.DOCUMENT -> loadDocument(uri)
                            MediaType.UNKNOWN -> showError()
                        }
                    }
                }
            } else {
                when (mediaType) {
                    MediaType.IMAGE -> loadImage(uri)
                    MediaType.VIDEO -> loadVideo(uri)
                    MediaType.AUDIO -> loadAudio(uri)
                    MediaType.DOCUMENT -> loadDocument(uri)
                    MediaType.UNKNOWN -> showError()
                }
            }
        }
    }

    private fun loadImage(uri: Uri) {
        binding.imagePreview.visibility = View.VISIBLE
        binding.topGradient.visibility = View.VISIBLE
        Glide.with(this)
            .load(uri)
            .centerInside()
            .into(binding.imagePreview)
    }

    private fun loadVideo(uri: Uri) {
        binding.videoPreview.visibility = View.VISIBLE
        binding.topGradient.visibility = View.VISIBLE

        exoPlayer = ExoPlayer.Builder(this).build().apply {
            binding.videoPreview.player = this
            setMediaItem(MediaItem.fromUri(uri))
            prepare()

            addListener(object : Player.Listener {
                @OptIn(UnstableApi::class)
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_READY) {
                        binding.videoPreview.hideController()
                    }
                }
            })
        }
    }

    private fun loadAudio(uri: Uri) {
        binding.audioControls.visibility = View.VISIBLE

        try {
            // Get audio metadata
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(this, uri)

            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?: getFileName(uri)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0L

            binding.albumArtImageView.setImageResource(R.drawable.music_icon)
            binding.audioFileName.text = title
            binding.audioDuration.text = formatDuration(duration)

            retriever.release()

            // Setup MediaPlayer
            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@MediaCompositionActivity, uri)
                prepare()

                binding.audioSeekBar.max = duration.toInt()

                setOnCompletionListener {
                    this@MediaCompositionActivity.isPlaying = false
                    binding.playPauseButton.setImageResource(R.drawable.ic_play)
                    binding.audioSeekBar.progress = 0
                }
            }

            // Update seekbar
            startSeekBarUpdate()

        } catch (e: Exception) {
            e.printStackTrace()
            showError()
        }
    }

    private fun loadDocument(uri: Uri) {
        binding.documentPreview.visibility = View.VISIBLE

        val fileName = getDocumentName(uri.toString())
        val fileSize = getDocumentSize(uri.toString())

        binding.documentName.text = fileName
        binding.documentSize.text = fileSize

        // Set icon based on file extension
        val extension = getDocumentExtension(fileName)
        val iconRes = when (extension.lowercase()) {
            "pdf" -> R.drawable.pdf_document_svgrepo_com
            "doc", "docx" -> R.drawable.word_document_svgrepo_com
            "xls", "xlsx" -> R.drawable.excel_icon
            "txt" -> R.drawable.txt_document_svgrepo_com
            "ppt", "pptx" -> R.drawable.powerpoint_icon
            else -> R.drawable.gdoc_document_svgrepo_com
        }
        binding.documentIcon.setImageResource(iconRes)
    }

    private fun togglePlayPause() {
        mediaPlayer?.let { player ->
            if (isPlaying) {
                player.pause()
                binding.playPauseButton.setImageResource(R.drawable.ic_play)
            } else {
                player.start()
                binding.playPauseButton.setImageResource(R.drawable.baseline_pause_black)
            }
            isPlaying = !isPlaying
        }
    }

    private fun startSeekBarUpdate() {
        binding.audioSeekBar.postDelayed(object : Runnable {
            override fun run() {
                mediaPlayer?.let {
                    if (it.isPlaying) {
                        binding.audioSeekBar.progress = it.currentPosition
                        binding.audioCurrentTime.text = formatDuration(it.currentPosition.toLong())
                    }
                    binding.audioSeekBar.postDelayed(this, 100)
                }
            }
        }, 100)
    }


    private fun sendMedia() {
        val caption = binding.captionInput.text.toString()

        // Return the result with caption and media URI
        val resultIntent = Intent().apply {
            putExtra(EXTRA_CAPTION, caption)
            putExtra(EXTRA_MEDIA_URI, mediaUri)
            putExtra(EXTRA_MEDIA_TYPE, mediaType.name)
        }

        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun showError() {
        // Show error state
    }

    suspend fun detectMediaType(uri: Uri): MediaType {
        return when (uri.scheme) {
            ContentResolver.SCHEME_CONTENT,
            ContentResolver.SCHEME_FILE -> detectFromLocalUri(uri)

            "http", "https" -> detectFromRemoteUri(uri)

            else -> MediaType.UNKNOWN
        }
    }


    private fun detectFromLocalUri(uri: Uri): MediaType {
        val mimeType = contentResolver.getType(uri) ?: return MediaType.UNKNOWN
        return mapMimeType(mimeType)
    }

    private suspend fun detectFromRemoteUri(uri: Uri): MediaType =
        withContext(Dispatchers.IO) {
            try {
                val connection = URL(uri.toString()).openConnection() as HttpURLConnection
                connection.requestMethod = "HEAD"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val mimeType = connection.contentType
                connection.disconnect()

                if (!mimeType.isNullOrEmpty()) {
                    mapMimeType(mimeType)
                } else {
                    detectFromExtension(uri)
                }
            } catch (e: Exception) {
                detectFromExtension(uri)
            }
        }

    private fun mapMimeType(mimeType: String): MediaType {
        return when {
            mimeType.startsWith("image/") -> MediaType.IMAGE
            mimeType.startsWith("video/") -> MediaType.VIDEO
            mimeType.startsWith("audio/") -> MediaType.AUDIO
            mimeType.startsWith("application/") -> MediaType.DOCUMENT
            else -> MediaType.UNKNOWN
        }
    }

    private fun detectFromExtension(uri: Uri): MediaType {
        val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            .lowercase(Locale.getDefault())

        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            ?: return MediaType.UNKNOWN

        return mapMimeType(mimeType)
    }


    private fun getFileName(uri: Uri): String {
        var result = "unknown"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    result = cursor.getString(nameIndex)
                }
            }
        }
        return result
    }

    private fun getDocumentName(documentPath: String): String {
        return File(documentPath).name
    }

    private fun getDocumentSize(documentPath: String): String {
        val fileSize = File(documentPath).length()
        val kb = fileSize / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0

        return when {
            gb >= 1 -> String.format(Locale.US, "%.2f GB", gb)
            mb >= 1 -> String.format(Locale.US, "%.2f MB", mb)
            kb >= 1 -> String.format(Locale.US, "%.2f KB", kb)
            else -> "$fileSize B"
        }
    }

    private fun getFileSize(uri: Uri): Long {
        var result = 0L
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                if (sizeIndex != -1) {
                    result = cursor.getLong(sizeIndex)
                }
            }
        }
        return result
    }

    private fun formatDuration(millis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }


}