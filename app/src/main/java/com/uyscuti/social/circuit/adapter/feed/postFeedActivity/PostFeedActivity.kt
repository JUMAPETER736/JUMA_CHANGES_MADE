package com.uyscuti.social.circuit.adapter.feed.postFeedActivity

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.uyscuti.social.core.common.data.room.entity.UserShortsEntity
import com.uyscuti.social.circuit.User_Interface.fragments.OnClickListeners
import com.uyscuti.social.circuit.User_Interface.fragments.OnCommentsClickListener
import com.uyscuti.social.circuit.data.model.Comment
import dagger.hilt.android.AndroidEntryPoint
import com.uyscuti.social.circuit.utils.Timer
import com.uyscuti.social.circuit.viewmodels.comments.ShortCommentsViewModel
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.chatsuit.messages.CommentsInput
import com.uyscuti.social.circuit.adapter.OnViewRepliesClickListener
import com.uyscuti.social.circuit.databinding.ActivityPostFeedBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "PostFeedActivity"

@AndroidEntryPoint
class PostFeedActivity : AppCompatActivity(),

    CommentsInput.EmojiListener,
    OnCommentsClickListener,
    CommentsInput.VoiceListener,
    CommentsInput.GifListener,
    CommentsInput.InputListener,
    CommentsInput.AttachmentsListener,
    Timer.OnTimeTickListener,
    OnViewRepliesClickListener,

    OnClickListeners { private lateinit var binding: ActivityPostFeedBinding
    private lateinit var timer: Timer
    @Inject
    lateinit var retrofitInterface: RetrofitInstance
    private lateinit var commentsViewModel: ShortCommentsViewModel
    var wasPaused = false
    var sending = false


    private lateinit var data: com.uyscuti.social.network.api.response.allFeedRepostsPost.OriginalPost
    private var position = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        Log.d(TAG, "onCreate: Clicked")

        val originalPostId = intent.getStringExtra("originalPostId")
        position = intent?.getIntExtra("position", 0)!!

        binding.toolbar.backIcon.setOnClickListener {
            finish()

        }
        binding.toolbar.username.text = originalPostId.toString()
        Glide.with(this)
            .load(originalPostId)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.toolbar.feedProfilePic)
        getPostById(originalPostId)

    }

    private fun getPostById(originalPostId: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = retrofitInterface.apiService.getPostById(originalPostId.toString())
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val originalPostId = response.body()
                        if (originalPostId != null) {
                            Log.d(TAG, "getPostById: $data")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "comment: $e")
                Log.e(TAG, "comment: ${e.message}")
                e.printStackTrace()
            }
        }
    }


    override fun onAddVoiceNote() {
        TODO("Not yet implemented")
    }

    override fun onAddGif() {
        TODO("Not yet implemented")
    }

    override fun onSubmit(input: CharSequence?): Boolean {
        TODO("Not yet implemented")
    }

    override fun onAddAttachments() {
        TODO("Not yet implemented")
    }

    override fun onTimerTick(duration: String) {
        TODO("Not yet implemented")
    }

    override fun onViewRepliesClick(
        data: Comment,
        repliesRecyclerView: RecyclerView,
        position: Int
    ) {
        TODO("Not yet implemented")
    }

    override fun onViewRepliesClick(
        data: Comment,
        position: Int,
        commentRepliesTV: TextView,
        hideCommentReplies: TextView,
        repliesRecyclerView: RecyclerView,
        isRepliesVisible: Boolean,
        page: Int
    ) {
        TODO("Not yet implemented")
    }

    override fun toggleAudioPlayer(
        audioPlayPauseBtn: ImageView,
        audioToPlayPath: String,
        position: Int,
        isReply: Boolean,
        progress: Float,
        isSeeking: Boolean,
        seekTo: Boolean,
        isVnAudio: Boolean
    ) {
        TODO("Not yet implemented")
    }

    override fun onReplyButtonClick(position: Int, data: Comment) {
        TODO("Not yet implemented")
    }

    override fun likeUnLikeComment(position: Int, data: Comment) {
        TODO("Not yet implemented")
    }

    override fun onSeekBarChanged(progress: Int) {
        TODO("Not yet implemented")
    }

    override fun onDownloadClick(url: String, fileLocation: String) {
        TODO("Not yet implemented")
    }

    override fun onShareClick(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onUploadCancelClick() {
        TODO("Not yet implemented")
    }

    override fun onAddEmoji() {
        TODO("Not yet implemented")
    }

    override fun onCommentsClick(
        position: Int,
        data: UserShortsEntity,
        isFeedComment: Boolean
    ) {
        TODO("Not yet implemented")
    }
}