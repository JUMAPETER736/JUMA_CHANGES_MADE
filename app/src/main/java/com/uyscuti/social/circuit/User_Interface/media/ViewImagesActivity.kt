package com.uyscuti.social.circuit.User_Interface.media

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import com.bumptech.glide.Glide
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.data.model.Comment
import com.uyscuti.social.circuit.databinding.ActivityViewImagesBinding
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
//    private var updateReplyLike:Boolean = false
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
        binding.toolbar.backIcon.setOnClickListener {
            finish()
        }
        binding.toolbar.apply {
            username.text = owner

            replyIcon.setOnClickListener {
//                Log.d(Tag, "is liked $liked")
                reply = true
                onReturn()
//                val resultIntent = Intent()
//                data!!.isLiked = liked
//                resultIntent.putExtra("data", data)
//                resultIntent.putExtra("position", position)
//                setResult(Activity.RESULT_OK, resultIntent)
//                finish()
            }

            if (displayLikeButton == true) {
                likeIcon.visibility = View.VISIBLE
                if(updateReplyLike == true) {
                    if (currentReplyComment?.isLiked == true) {
                        likeIcon.setImageResource(R.drawable.filled_favorite_like)
                    } else {
                        likeIcon.setImageResource(R.drawable.like_svgrepo_com_white)
                    }
                }else {
                    if (data!!.isLiked) {
                        likeIcon.setImageResource(R.drawable.filled_favorite_like)
                    } else {
                        likeIcon.setImageResource(R.drawable.like_svgrepo_com_white)
                    }
                }

            }
            likeIcon.setOnClickListener {
                if (updateReplyLike == true){
//                    updateReplyLike = true
                    updateReplyLikes = true
                    currentReplyComment?.isLiked = !currentReplyComment?.isLiked!!
                    if (currentReplyComment!!.isLiked) {
                        likeIcon.setImageResource(R.drawable.filled_favorite_like)
                        YoYo.with(Techniques.Tada)
                            .duration(700)
                            .repeat(1)
                            .playOn(likeIcon)
                    } else {
                        likeIcon.setImageResource(R.drawable.like_svgrepo_com_white)
                        YoYo.with(Techniques.Tada)
                            .duration(700)
                            .repeat(1)
                            .playOn(likeIcon)
                    }
                }else{
                    data!!.isLiked = !data!!.isLiked
                    liked = data!!.isLiked
                    updateLike = true
                    if (data!!.isLiked) {
                        likeIcon.setImageResource(R.drawable.filled_favorite_like)
                        YoYo.with(Techniques.Tada)
                            .duration(700)
                            .repeat(1)
                            .playOn(likeIcon)
                    } else {
                        likeIcon.setImageResource(R.drawable.like_svgrepo_com_white)
                        YoYo.with(Techniques.Tada)
                            .duration(700)
                            .repeat(1)
                            .playOn(likeIcon)
                    }
                }
                }

//                EventBus.getDefault().post(LikeComment(data!!, position!!))

        }

//        setSupportActionBar(binding.toolbar)
//
//        binding.toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24)
//        supportActionBar?.title = owner
//
//        binding.toolbar.setNavigationOnClickListener {
//            onBackPressed()
//        }

        Log.i(Tag, "imagePath from viewing images - $imagePath")
        Glide.with(this).load(imagePath)
            .error(R.drawable.ic_action_copy).into(binding.imageView)

        // Add pinch-to-zoom functionality
        val scaleGestureDetector = ScaleGestureDetector(this, ScaleListener(binding.imageView))

        binding.imageView.setOnTouchListener { _, motionEvent ->
            scaleGestureDetector.onTouchEvent(motionEvent)
            true
        }

        // Swipe to close the activity
        binding.imageView.setOnClickListener {
            finish()
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

//    @Deprecated("Deprecated in Java")
//    override fun onBackPressed() {
//        super.onBackPressed()
//        Log.d("onBackPressed", "onBackPressed")
//        onReturn()
//    }

    private fun onReturn(){
        Log.d("onReturn", "onReturn")

//        Log.d(Tag, "is liked $liked and reply $reply update like $updateLike")
        Log.d(Tag, "currentReplyComment like -> ${currentReplyComment?.isLiked}")
//
//        if(!updateReplyLikes){
//            data!!.isLiked = liked
//        }
//
//        if(updateLike){
//            data!!.isLiked = liked
//        }
//        val resultIntent = Intent()
//        resultIntent.putExtra("data", data)
//        resultIntent.putExtra("reply", reply)
//        resultIntent.putExtra("currentReplyComment", currentReplyComment)
//        resultIntent.putExtra("updateLike", updateLike)
//        resultIntent.putExtra("updateReplyLikes", updateReplyLikes)
//        resultIntent.putExtra("position", position)
//        setResult(Activity.RESULT_OK, resultIntent)
        finish()


    }
    private inner class ScaleListener(private val imageView: ImageView) :
        ScaleGestureDetector.SimpleOnScaleGestureListener() {
        private var scaleFactor = 1f

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = Float.max(1.0f, Float.min(scaleFactor, 3.0f)) // Limit the zoom level
            imageView.scaleX = scaleFactor
            imageView.scaleY = scaleFactor
            return true
        }
    }

}