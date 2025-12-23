package com.uyscuti.social.circuit.User_Interface.feedactivities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.uyscuti.social.circuit.databinding.ActivityFeedShareActivivityBinding

class FeedShareActivity : AppCompatActivity() {
    private lateinit var  binding: ActivityFeedShareActivivityBinding
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityFeedShareActivivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
      binding.shareFeedLayout.setOnClickListener {
            // Create an intent to share text
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_TEXT, "Check out this amazing post!")
                type = "text/plain"
            }

            startActivity(Intent.createChooser(shareIntent, "Share via"))
        }
    }
}