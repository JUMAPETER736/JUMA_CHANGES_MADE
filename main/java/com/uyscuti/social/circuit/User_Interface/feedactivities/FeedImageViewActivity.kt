package com.uyscuti.social.circuit.User_Interface.feedactivities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.ActivityFeedImageViewBinding

class FeedImageViewActivity : AppCompatActivity() {

    private lateinit var data: com.uyscuti.social.network.api.response.allFeedRepostsPost.Post
    private var position = 0
    private lateinit var binding: ActivityFeedImageViewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFeedImageViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        data = (intent?.extras?.getSerializable("data") as com.uyscuti.social.network.api.response.allFeedRepostsPost.Post?)!!
        position = intent?.getIntExtra("position", 0)!!

        Glide.with(this)
            .load(data.files[0].url)
            .placeholder(R.drawable.flash21)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.imageView)
    }
}