package com.uyscuti.social.circuit.User_Interface.feedactivities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.ActivityFeedTextViewBinding


class FeedTextViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFeedTextViewBinding
    private lateinit var data: com.uyscuti.social.network.api.response.getrepostsPostsoriginal.Post
    private var position = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFeedTextViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        data = (intent?.extras?.getSerializable("data") as com.uyscuti.social.network.api.response.getrepostsPostsoriginal.Post?)!!
        position = intent?.getIntExtra("position", 0)!!

        if (data.content == "") {
            binding.feedTextContent.text = ""
        } else {
            binding.feedTextContent.text = data.content
        }



        Glide.with(this)
            .load(data.author[0].account.avatar.url)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .placeholder(R.drawable.profilepic2)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.toolbar.feedProfilePic)

        binding.toolbar.backIcon.setOnClickListener {
            finish()
        }

        binding.toolbar.username.text = data.author[0].account.username
        binding.likesCount.text = data.likes.toString()
        binding.feedCommentsCount.text = data.comments.toString()
        if (data.isLiked) {
            binding.likeButtonIcon.setImageResource(R.drawable.filled_favorite_like)
        } else {
            binding.likeButtonIcon.setImageResource(R.drawable.like_svgrepo_com)
        }
        if(data.isBookmarked) {
            binding.favoriteSection.setImageResource(R.drawable.filled_favorite)
        }else {
            binding.favoriteSection.setImageResource(R.drawable.favorite_svgrepo_com__1_)
        }
    }
}