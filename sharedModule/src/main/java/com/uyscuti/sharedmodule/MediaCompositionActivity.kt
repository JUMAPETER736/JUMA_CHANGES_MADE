package com.uyscuti.sharedmodule

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.exoplayer.ExoPlayer
import com.uyscuti.sharedmodule.databinding.ActivityMediaCompositionBinding

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


}