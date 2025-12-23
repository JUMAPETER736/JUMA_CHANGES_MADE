package com.uyscuti.social.circuit.User_Interface.media

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.MediaController
import android.widget.VideoView
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.ActivityPlayVideoBinding

class PlayVideoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayVideoBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val videoPath = intent.getStringExtra("videoPath")

        val owner = intent.getStringExtra("owner")

        setSupportActionBar(binding.toolbar)

        binding.toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24)
        supportActionBar?.title = owner

        Log.d("VideoDebug", "VideoPath from full video display $videoPath")

//        setSupportActionBar(binding.toolbar)

//        binding.toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24)


        val videoView = findViewById<VideoView>(R.id.videoView)
        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)

        val videoUri = Uri.parse(videoPath)
        //val uri: Uri = parse("android.resource://"+packageName+"/"+R.raw.gage_road)
        videoView.setMediaController(mediaController)
        videoView.setVideoURI(videoUri)

        videoView.requestFocus()
        videoView.start()

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        // The above line will handle the back navigation
        // If you want to perform additional actions, you can add them here
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}