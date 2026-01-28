package com.uyscuti.sharedmodule.media

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import com.bumptech.glide.Glide
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.uyscuti.sharedmodule.R
import com.uyscuti.sharedmodule.data.model.Comment
import com.uyscuti.sharedmodule.databinding.ActivityViewImagesBinding
import java.lang.Float
import kotlin.properties.Delegates

class ViewImagesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewImagesBinding

    private val Tag = "ViewImagesActivity"
    private var liked = false

    private var position by Delegates.notNull<Int>()
    private var data: Comment? = null
    private var currentReplyComment:com.uyscuti.social.network.api.response.commentreply.allreplies.Comment? = null
    private var reply:Boolean = false
    private var updateLike:Boolean = false

    private var updateReplyLikes:Boolean = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityViewImagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imagePath = intent.getStringExtra("imageUrl")
        val owner = intent.getStringExtra("owner")
        position = intent?.getIntExtra("position", 0)!!

        val displayLikeButton = intent?.getBooleanExtra("displayLikeButton", false)
        val updateReplyLike = intent?.getBooleanExtra("updateReplyLike", false)

        data = intent?.extras?.getSerializable("data") as Comment?
        currentReplyComment = intent?.extras?.getSerializable("currentItem") as com.uyscuti.social.network.api.response.commentreply.allreplies.Comment?

        Log.d(Tag, "currentReplyComment -> $currentReplyComment")
        Log.d(Tag, "updateReplyLike -> $updateReplyLike")
        Log.i(Tag, "imagePath from viewing images - $imagePath")

        // Load the image using Glide
        if (!imagePath.isNullOrEmpty()) {
            Glide.with(this)
                .load(imagePath)
                .placeholder(R.drawable.google) // Optional: placeholder while loading
                .error(R.drawable.google) // Optional: error image if loading fails
                .into(binding.fullImageView)
        } else {
            Log.e(Tag, "Image path is null or empty")
        }

        // Set up close button click listener
        binding.closeButton.setOnClickListener {
            onReturn()
        }

        // Create a callback for handling back button presses
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle the back button press here
                Log.d("onBackPressed", "Back button pressed")
                onReturn()
            }
        }

        // Add the callback to the back button dispatcher
        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun onReturn(){
        Log.d("onReturn", "onReturn")
        Log.d(Tag, "currentReplyComment like -> ${currentReplyComment?.isLiked}")
        finish()
    }
}