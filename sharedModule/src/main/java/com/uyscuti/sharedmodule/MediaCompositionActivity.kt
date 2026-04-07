package com.uyscuti.sharedmodule

import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.exoplayer.ExoPlayer

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


}