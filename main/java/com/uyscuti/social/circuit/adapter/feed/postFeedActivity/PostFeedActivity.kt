package com.uyscuti.social.circuit.adapter.feed.postFeedActivity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.uyscuti.social.core.common.data.room.entity.UserShortsEntity
import com.uyscuti.social.circuit.adapter.OnClickListeners
import com.uyscuti.social.circuit.adapter.OnCommentsClickListener
import com.uyscuti.social.circuit.data.model.Comment
import com.uyscuti.social.circuit.service.VideoPreLoadingService
import com.uyscuti.social.circuit.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import com.uyscuti.social.circuit.utils.Timer
import com.uyscuti.social.circuit.viewmodels.comments.CommentsViewModel
import com.uyscuti.social.circuit.viewmodels.comments.ShortCommentsViewModel
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.chatsuit.messages.CommentsInput
import com.uyscuti.social.circuit.adapter.CommentsRecyclerViewAdapter
import com.uyscuti.social.circuit.adapter.OnViewRepliesClickListener
import com.uyscuti.social.circuit.databinding.ActivityPostFeedBinding
import com.vanniktech.emoji.EmojiPopup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.properties.Delegates

private const val TAG = "PostFeedActivity"

@AndroidEntryPoint
class PostFeedActivity : AppCompatActivity(),CommentsInput.EmojiListener, OnCommentsClickListener,
    CommentsInput.VoiceListener, CommentsInput.GifListener, CommentsInput.InputListener,
    CommentsInput.AttachmentsListener, Timer.OnTimeTickListener, OnViewRepliesClickListener,
    OnClickListeners {
    private lateinit var binding: ActivityPostFeedBinding
    private lateinit var timer: Timer
    @Inject
    lateinit var retrofitInterface: RetrofitInstance
    private lateinit var emojiPopup: EmojiPopup
    private lateinit var inputMethodManager: InputMethodManager
    private var emojiShowing = false
    private lateinit var outputFile: String
    private var mediaRecorder: MediaRecorder? = null
    private lateinit var postId: String
    private lateinit var commentId: String
    private var commentCount by Delegates.notNull<Int>()
    private var adapter: CommentsRecyclerViewAdapter? = null
    private lateinit var commentsViewModel: ShortCommentsViewModel
    private lateinit var commentViewModel: CommentsViewModel
    private var isRecording = false
    private var isPaused = false
    private var isAudioVNPlaying = false
    private var isAudioVNPaused = false
    var wasPaused = false
    var sending = false
    private var isVnResuming = false


    private lateinit var context : Context

    private var feedToComment: com.uyscuti.social.network.api.response.allFeedRepostsPost.Post? = null
    private var favoriteFeedToComment: com.uyscuti.social.network.api.response.allFeedRepostsPost.Post? = null
    private lateinit var data: com.uyscuti.social.network.api.response.allFeedRepostsPost.OriginalPost
    private var position = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        Log.d(TAG, "onCreate: Clicked")
//        if (data.originalPostId == null)
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
    private var isFeedComment = false
    private fun generateSampleData(count: Int): List<com.uyscuti.social.circuit.data.model.shortsmodels.Comment> {
        val itemList = mutableListOf<com.uyscuti.social.circuit.data.model.shortsmodels.Comment>()
        for (i in 1..count) {
            itemList.add(com.uyscuti.social.circuit.data.model.shortsmodels.Comment("Item $i"))
        }
        return itemList
    }
    // Function to hide the keyboard
    private fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    @SuppressLint("SetTextI18n")
    @OptIn(UnstableApi::class)

    private fun stopPlaying() {
//        binding.playVnAudioBtn.setImageResource(R.drawable.play_svgrepo_com)
////        player?.release()
////        player = null
//        isAudioVNPlaying = false
////        vnRecordAudioPlaying = false
////        isOnRecordDurationOnPause = false
////        stopRecordWaveRunnable()
//        binding.wave.progress = 0F
//        vnRecordProgress = 0
    }

    private fun showProgressBar() {
//        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
//        binding.progressBar.visibility = View.GONE
    }

    private var vnList = ArrayList<String>()

    @OptIn(UnstableApi::class)
    private fun startPreLoadingService() {
        Log.d("VNCache", "Preloading called")
        val preloadingServiceIntent =
            Intent(this, VideoPreLoadingService::class.java)
        preloadingServiceIntent.putStringArrayListExtra(Constants.VIDEO_LIST, vnList)
        startService(preloadingServiceIntent)
    }



    private fun observeComments() {
        val TAG = "observeComments"
        commentsViewModel.commentsLiveData.observe(this) { it ->
            Log.d(TAG, "observeComments comments size: ${it.size}")
//            val commentsWithReplies = it.find{it.}
            val commentsWithReplies = it.filter { it.replyCount > 0 }
            Log.d(TAG, "observeComments comments with replies size: ${commentsWithReplies.size}")

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

    override fun onCommentsClick(position: Int, data: UserShortsEntity) {
        TODO("Not yet implemented")
    }
}