package com.uyscuti.sharedmodule.media

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import com.bumptech.glide.Glide
import com.uyscuti.sharedmodule.R
import com.uyscuti.sharedmodule.data.model.Comment
import com.uyscuti.sharedmodule.databinding.ActivityViewImagesBinding
import kotlin.properties.Delegates

class ViewImagesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewImagesBinding
    private lateinit var settings: SharedPreferences
    private val PREFS_NAME = "LocalSettings"

    private val Tag = "ViewImagesActivity"
    private var liked = false

    private var position by Delegates.notNull<Int>()
    private var data: Comment? = null
    private var currentReplyComment: com.uyscuti.social.network.api.response.commentreply.allreplies.Comment? = null


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityViewImagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        settings = getSharedPreferences(PREFS_NAME, 0)

        // Get intent extras
        var imagePath = intent.getStringExtra("imageUrl")
        val owner = intent.getStringExtra("owner")
        position = intent.getIntExtra("position", 0)

        val displayLikeButton = intent.getBooleanExtra("displayLikeButton", false)
        val updateReplyLike = intent.getBooleanExtra("updateReplyLike", false)

        data = intent.extras?.getSerializable("data") as? Comment
        currentReplyComment = intent.extras?.getSerializable("currentItem") as? com.uyscuti.social.network.api.response.commentreply.allreplies.Comment

        // If no imageUrl passed via intent, try to get from SharedPreferences
        if (imagePath.isNullOrEmpty()) {
            imagePath = settings.getString("avatar", "")
            Log.d(Tag, "No imageUrl in intent, loaded from SharedPreferences: $imagePath")
        }

        Log.d(Tag, "currentReplyComment -> $currentReplyComment")
        Log.d(Tag, "updateReplyLike -> $updateReplyLike")
        Log.i(Tag, "imagePath from viewing images - $imagePath")

        // Load the image using Glide
        if (!imagePath.isNullOrEmpty()) {
            Glide.with(this)
                .load(imagePath)
                .placeholder(R.drawable.google)
                .error(R.drawable.round_user)
                .into(binding.fullImageView)

            Log.d(Tag, "Loading image from: $imagePath")
        } else {
            Log.e(Tag, "Image path is null or empty, loading default avatar")
            // Load default avatar image
            binding.fullImageView.setImageResource(R.drawable.round_user)
        }

        // Set up close button click listener
        binding.closeButton.setOnClickListener {
            onReturn()
        }

        // Create a callback for handling back button presses
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d(Tag, "Back button pressed")
                onReturn()
            }
        }

        // Add the callback to the back button dispatcher
        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun onReturn() {
        Log.d(Tag, "onReturn called")
        Log.d(Tag, "currentReplyComment like -> ${currentReplyComment?.isLiked}")
        finish()
    }
}