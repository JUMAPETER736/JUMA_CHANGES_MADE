package com.uyscuti.social.circuit.adapter.shorts
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.gson.reflect.TypeToken
import com.uyscuti.social.circuit.utils.waveformseekbar.SeekBarOnProgressChanged
import com.uyscuti.social.circuit.utils.waveformseekbar.WaveformSeekBar


import com.uyscuti.social.circuit.model.AudioPlayerHandler
import com.uyscuti.social.circuit.model.CommentAudioPlayerHandler
import com.uyscuti.social.circuit.model.LikeCommentReply
import com.uyscuti.social.circuit.model.ToggleReplyToTextView
import com.uyscuti.social.circuit.User_Interface.media.CommentVideoPlayerActivity
import com.uyscuti.social.circuit.User_Interface.media.ViewImagesActivity
import com.uyscuti.social.circuit.utils.AudioDurationHelper
import com.uyscuti.social.circuit.utils.COMMENT_VIDEO_CODE
import com.uyscuti.social.circuit.utils.MongoDBTimeFormatter
import com.uyscuti.social.circuit.utils.R_CODE
import com.uyscuti.social.circuit.utils.TimeUtils
import com.uyscuti.social.circuit.utils.TrimVideoUtils
import com.uyscuti.social.circuit.utils.WaveFormExtractor
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.adapter.OnViewRepliesClickListener
import com.uyscuti.social.circuit.cache.objectcache.CacheManager
import com.uyscuti.social.network.api.response.commentreply.allreplies.Comment
import com.uyscuti.social.circuit.cache.objectcache.DiskCache
import com.uyscuti.social.circuit.cache.objectcache.PutCallback
import com.uyscuti.social.circuit.databinding.CommentAudioItemReplyBinding
import com.uyscuti.social.circuit.databinding.CommentDocumentItemReplyBinding
import com.uyscuti.social.circuit.databinding.CommentImageItemReplyBinding
import com.uyscuti.social.circuit.databinding.CommentReplyBinding
import com.uyscuti.social.circuit.databinding.CommentVideoItemReplyBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.io.File


private const val TAG = "ReplyCommentAdapter"
private const val VIEW_TYPE_TEXT = 0
private const val VIEW_TYPE_AUDIO = 1
private const val VIEW_TYPE_IMAGE = 2
private const val VIEW_TYPE_VIDEO = 3
private const val VIEW_TYPE_DOCUMENT = 4
private const val VIEW_TYPE_GIF = 5
private const val VIEW_TYPE_EMPTY = 10

class ReplyCommentAdapter(
    private val context: Context,
    private val data: com.uyscuti.social.circuit.data.model.Comment,
    private val postId: String,
    private val mPosition: Int
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val itemList: MutableList<Comment> = data.replies
    private var mCurrentPosition = -1
    private var mPlayingPosition = mPosition
    private var mPlayListener: OnPlayListener? = null
    private var previousPosition = -1

    // cache
    private var cachePath: String
    private var cacheFile: File
    private var diskCache: DiskCache
    private var cacheManager: CacheManager

    private var currentComment: Comment? = null

    private var mCurrentWaveForm: WaveformSeekBar? = null
    private var mCurrentSeekBar: SeekBar? = null
    private var secondCurrentSeekBar: SeekBar? = null
    private var secondCurrentWaveForm: WaveformSeekBar? = null
    private var secondAudioDurationTeV: TextView? = null

    private lateinit var listener: OnViewRepliesClickListener
    private var isPlay: Boolean = false
    private lateinit var waveRunnable: Runnable

    private val mWaveForms: ArrayList<WaveformSeekBar> = arrayListOf()

    init {
        cachePath = context.cacheDir.path
        cacheFile = File(cachePath + File.separator + "com.uyscuti.social.flashdesign")
        diskCache = DiskCache(cacheFile, 1, 1024 * 1024 * 10)

        cacheManager = CacheManager.getInstance(diskCache)

        if (mPosition != -1) {

            Log.d("ReplyPlayPosition", " playing position $mPosition")
        } else {
            itemList.map {
                it.isPlaying = false
            }
        }

    }


    inner class GifViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = CommentImageItemReplyBinding.bind(itemView)

        @SuppressLint("SetTextI18n")
        fun bind(currentItem: Comment) {
            binding.apply {

                Log.d("currentItemData", "currentItemContent ${currentItem.content}")
                var imageUrl = ""
                val owner = currentItem.author!!.account.username
                username.text = currentItem.author!!.account.username

                time.text = TimeUtils.formatMongoTimestamp(currentItem.createdAt)

                val inputString = currentItem.content
                val regex = Regex("@\\w+")
                val matches = regex.findAll(inputString)
                if (matches.none()) {
                    // No mentions, simply set the text
                    content.text = inputString
                } else {
                    val highlightColor: Int by lazy {
                        ContextCompat.getColor(content.context, R.color.bluejeans)
                    }
                    val spannableString = SpannableString(inputString)

                    matches.forEach {
                        val start = it.range.first
                        val end = it.range.last + 1 // +1 to include the "@" symbol

                        // Set blue color to the mentioned user
                        spannableString.setSpan(
                            ForegroundColorSpan(highlightColor),
                            start,
                            end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }

                    content.text = spannableString
                }

                val avatar = com.uyscuti.social.network.api.response.comment.allcomments.Avatar(
                    _id = currentItem.author!!.account.avatar._id,
                    url = currentItem.author!!.account.avatar.url,
                    localPath = currentItem.author!!.account.avatar.localPath
                )
                val account = com.uyscuti.social.network.api.response.comment.allcomments.Account(
                    _id = currentItem.author!!.account._id,
                    avatar = avatar,
                    email = currentItem.author!!.account.email,
                    username = currentItem.author!!.account.username
                )
                val author = com.uyscuti.social.network.api.response.comment.allcomments.Author(
                    _id = currentItem.author!!._id,
                    account = account,
                    firstName = currentItem.author!!.firstName,
                    lastName = currentItem.author!!.lastName,
                    avatar = null
                )


                val commentReply = com.uyscuti.social.circuit.data.model.Comment(
                    __v = currentItem.__v,
                    _id = currentItem.commentId,
                    author = author,
                    content = currentItem.content,
                    createdAt = currentItem.createdAt,
                    isLiked = currentItem.isLiked,
                    likes = currentItem.likes,
                    postId = postId,
                    updatedAt = currentItem.updatedAt,
                    replyCount = 0,
                    replies = mutableListOf(),
                    images = data.images,
                    audios = data.audios,
                    docs = data.docs,
                    videos = data.videos,
                    thumbnail = data.thumbnail,
                    gifs = data.gifs,
                    contentType = currentItem.contentType,
                    localUpdateId = data.localUpdateId
                )
                reply.setOnClickListener {
                    // Handle the click event for the reply button
                    EventBus.getDefault().post(ToggleReplyToTextView(data, commentReply.__v))
                }

                Glide.with(profilePic.context)
                    .load(currentItem.author!!.account.avatar.url)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .placeholder(R.drawable.flash21)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(profilePic)


                if (currentItem.gifs != "") {
                    Log.d("currentItemData", "images ${currentItem.gifs}")
                    imageUrl = currentItem.gifs.toString()
                    if (imageUrl.isNotEmpty()) {
                        Log.d("currentItemData", "Image url is not empty for holder")
                        Glide.with(context)
                            .load(currentItem.gifs)
                            .placeholder(R.drawable.flash21)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(imageComment)
                    } else {
                        Log.d("currentItemData", "Image url is empty for holder")
                    }

                }

                imageComment.setOnClickListener {
                    val intent = Intent(context, ViewImagesActivity::class.java)
                    intent.putExtra("imageUrl", imageUrl)
                    intent.putExtra("owner", owner)
                    intent.putExtra("displayLikeButton", true)
                    intent.putExtra("updateReplyLike", true)
                    intent.putExtra("position", commentReply.__v)
                    intent.putExtra("data", data)
                    intent.putExtra("currentItem", currentItem)

                    (context as Activity).startActivityForResult(intent, R_CODE)

                }


                when (currentItem.likes) {
                    0 -> {
                        likesCount.visibility = View.GONE
                    }

                    1 -> {
                        likesCount.visibility = View.VISIBLE
                        likesCount.text = "${currentItem.likes} like"
                    }

                    else -> {
                        likesCount.visibility = View.VISIBLE
                        likesCount.text = "${currentItem.likes} likes"
                    }
                }


                // Handle like button (using likesCount as the clickable element since likeUnLikeComment is commented out)
                likesCount.setOnClickListener {
                    currentItem.isLiked = !currentItem.isLiked
                    EventBus.getDefault().post(LikeCommentReply(currentItem, data, commentReply.__v))
                    // Update likes count
                    when {
                        currentItem.isLiked -> currentItem.likes += 1
                        currentItem.likes > 0 -> currentItem.likes -= 1
                    }
                    when (currentItem.likes) {
                        0 -> likesCount.visibility = View.GONE
                        1 -> {
                            likesCount.visibility = View.VISIBLE
                            likesCount.text = "${currentItem.likes} like"
                        }
                        else -> {
                            likesCount.visibility = View.VISIBLE
                            likesCount.text = "${currentItem.likes} likes"
                        }
                    }
                    // Apply animation
                    YoYo.with(Techniques.Tada)
                        .duration(700)
                        .repeat(1)
                        .playOn(likesCount)
                }

            }

        }

    }


    private fun setCacheSample(sample: IntArray, path: String) {
        cacheManager.putAsync(path, sample, object : PutCallback {
            override fun onSuccess() {
                Log.d("Cache", "sample cached")
            }


            override fun onFailure(e: java.lang.Exception?) {
                Log.d("Cache", "failed to cache sample")

            }

        })
    }

    private fun getSample(path: String): IntArray? {
        val tokenType = object : TypeToken<IntArray?>() {}.type

        var data: IntArray? = null

        data = cacheManager.get(path, IntArray::class.java, tokenType) as IntArray?

        return data
    }

    fun setListener(listener: OnViewRepliesClickListener) {
        this.listener = listener
    }

    fun setPlayListener(l: OnPlayListener) {
        mPlayListener = l
    }


    var secondWaveProgress = 0f

    //CommentAudioItemReplyBinding


    fun setSecondWaveFormProgress(progress: Float, position: Int) {
        Log.d(
            "setSecondWaveFormProgress",
            "setSecondWaveFormProgress $progress,,, position $mCurrentPosition"
        )

        secondWaveProgress = progress
        if (secondCurrentWaveForm != null) {
            Log.d("setSecondWaveFormProgress", "setSecondWaveFormProgress is not null")
            CoroutineScope(Dispatchers.Main).launch {
                secondCurrentWaveForm?.progress = progress

                secondAudioDurationTeV?.text = String.format(
                    "%s",
                    TrimVideoUtils.stringForTime(progress)
                )
                Log.d(
                    "setSecondWaveFormProgress",
                    "setSecondWaveFormProgress second wave progress ${secondCurrentWaveForm!!.progress} progress received from main $progress"
                )
            }

            secondCurrentWaveForm?.progress = progress
            Log.d(
                "setSecondWaveFormProgress",
                "(outer)->setSecondWaveFormProgress second wave progress ${secondCurrentWaveForm!!.progress} progress received from main $progress"
            )

        } else {
            Log.d("setSecondWaveFormProgress", "setSecondWaveFormProgress is null")

        }
    }

    fun setSecondSeekBarProgress(progress: Float, position: Int) {
        Log.d(
            "secondCurrentSeekBar",
            "secondCurrentSeekBar $progress,,, position $mCurrentPosition"
        )
        if (secondCurrentSeekBar != null) {
            Log.d("secondCurrentSeekBar", "secondCurrentSeekBar is not null")
            CoroutineScope(Dispatchers.Main).launch {
                secondCurrentSeekBar?.progress = progress.toInt()

                secondAudioDurationTeV?.text = String.format(
                    "%s",
                    TrimVideoUtils.stringForTime(progress)
                )
                Log.d(
                    "secondCurrentSeekBar",
                    "secondCurrentSeekBar second wave progress ${secondCurrentSeekBar!!.progress} progress received from main $progress"
                )
            }

            secondCurrentSeekBar?.progress = progress.toInt()
            Log.d(
                "secondCurrentSeekBar",
                "(outer)->secondCurrentSeekBar second wave progress ${secondCurrentSeekBar!!.progress} progress received from main $progress"
            )

        } else {
            Log.d("secondCurrentSeekBar", "secondCurrentSeekBar is null")

        }
    }

    fun updateSeekBarProgress(progress: Float, seekBar: SeekBar) {
        if (mPlayingPosition < 0) {
            return
        }


        if (mCurrentSeekBar != null) {
            CoroutineScope(Dispatchers.Main).launch {
                mCurrentSeekBar!!.progress = progress.toInt()

            }
        } else {

            mCurrentSeekBar = seekBar
            CoroutineScope(Dispatchers.Main).launch {
                seekBar.progress = progress.toInt()
                Log.d("updateWaveProgress", "updateWaveProgress: $progress")
            }

        }
    }

    fun updateWaveProgress(progress: Float, waveformSeekBar: WaveformSeekBar) {

        if (mPlayingPosition < 0) {
            return
        }



        if (mCurrentWaveForm != null) {
            CoroutineScope(Dispatchers.Main).launch {
                mCurrentWaveForm!!.progress = progress


            }
        } else {

            mCurrentWaveForm = waveformSeekBar
            CoroutineScope(Dispatchers.Main).launch {
                waveformSeekBar.progress = progress
                Log.d("updateWaveProgress", "updateWaveProgress: $progress")


            }
        }
    }

    inner class TextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val TAG = "Reply Comment Text Only ViewHolder"

        val binding = CommentReplyBinding.bind(itemView)


        @SuppressLint("SetTextI18n")
        fun bind(currentItem: Comment) {
            binding.apply {
                Log.d("currentItemData", "currentItemContent ${currentItem.content}")
                username.text = currentItem.author!!.account.username

                val timeFormatter = MongoDBTimeFormatter()

                time.text = TimeUtils.formatMongoTimestamp(currentItem.createdAt)

                val avatar = com.uyscuti.social.network.api.response.comment.allcomments.Avatar(
                    _id = currentItem.author!!.account.avatar._id,
                    url = currentItem.author!!.account.avatar.url,
                    localPath = currentItem.author!!.account.avatar.localPath
                )
                val account = com.uyscuti.social.network.api.response.comment.allcomments.Account(
                    _id = currentItem.author!!.account._id,
                    avatar = avatar,
                    email = currentItem.author!!.account.email,
                    username = currentItem.author!!.account.username
                )

                val author = com.uyscuti.social.network.api.response.comment.allcomments.Author(
                    _id = currentItem.author!!._id,
                    account = account,
                    firstName = currentItem.author!!.firstName,
                    lastName = currentItem.author!!.lastName,
                    avatar = null
                )


                val commentReply = com.uyscuti.social.circuit.data.model.Comment(
                    __v = currentItem.__v,
                    _id = currentItem.commentId,
                    author = author,
                    content = currentItem.content,
                    createdAt = currentItem.createdAt,
                    isLiked = currentItem.isLiked,
                    likes = currentItem.likes,
                    postId = postId,
                    updatedAt = currentItem.updatedAt,
                    replyCount = 0,
                    replies = mutableListOf(),
                    images = data.images,
                    audios = data.audios,
                    docs = data.docs,
                    videos = data.videos,
                    thumbnail = data.thumbnail,
                    gifs = data.gifs,
                    contentType = currentItem.contentType,
                    localUpdateId = data.localUpdateId
                )
                reply.setOnClickListener {
                    // Handle the click event for the reply button
                    EventBus.getDefault().post(ToggleReplyToTextView(data, commentReply.__v))
                }

                Glide.with(profilePic.context)
                    .load(currentItem.author!!.account.avatar.url)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .placeholder(R.drawable.flash21)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(profilePic)

                val inputString = currentItem.content
                val regex = Regex("@\\w+")
                val matches = regex.findAll(inputString)
                if (matches.none()) {
                    // No mentions, simply set the text
                    content.text = inputString
                } else {
                    val highlightColor: Int by lazy {
                        ContextCompat.getColor(content.context, R.color.bluejeans)
                    }
                    val spannableString = SpannableString(inputString)

                    matches.forEach {
                        val start = it.range.first
                        val end = it.range.last + 1 // +1 to include the "@" symbol

                        // Set blue color to the mentioned user
                        spannableString.setSpan(
                            ForegroundColorSpan(highlightColor),
                            start,
                            end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }

                    content.text = spannableString
                }

                if (currentItem.likes == 0) {
                    likesCount.visibility = View.GONE
                } else if (currentItem.likes == 1) {
                    likesCount.visibility = View.VISIBLE
                    likesCount.text = "${currentItem.likes} like"
                } else {
                    likesCount.visibility = View.VISIBLE
                    likesCount.text = "${currentItem.likes} likes"
                }

                if (currentItem.isLiked) {
                    likeUnLikeComment.text = "Like"
                    likeUnLikeComment.setTextColor(ContextCompat.getColor(context, R.color.dark_gray))
                } else {
                    likeUnLikeComment.text = "Like"
                    likeUnLikeComment.setTextColor(ContextCompat.getColor(context, R.color.bluejeans))
                }

                likeUnLikeComment.setOnClickListener {
                    currentItem.isLiked = !currentItem.isLiked

                    EventBus.getDefault()
                        .post(LikeCommentReply(currentItem, data, commentReply.__v))

                    if (currentItem.isLiked) {
                        likeUnLikeComment.text = "Like"
                        likeUnLikeComment.setTextColor(ContextCompat.getColor(context, R.color.dark_gray))
                        YoYo.with(Techniques.Tada)
                            .duration(700)
                            .repeat(1)
                            .playOn(likeUnLikeComment)
                    } else {
                        likeUnLikeComment.text = "Like"
                        likeUnLikeComment.setTextColor(ContextCompat.getColor(context, R.color.bluejeans))
                        YoYo.with(Techniques.Tada)
                            .duration(700)
                            .repeat(1)
                            .playOn(likeUnLikeComment)
                    }
                }

            }

        }

    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val TAG = "Reply Comment Image ViewHolder"

        val binding = CommentImageItemReplyBinding.bind(itemView)

        @SuppressLint("SetTextI18n")
        fun bind(currentItem: Comment) {
            binding.apply {

                Log.d("currentItemData", "currentItemContent ${currentItem.content}")
                var imageUrl = ""
                val owner = currentItem.author!!.account.username
                username.text = currentItem.author!!.account.username


                val inputString = currentItem.content
                val regex = Regex("@\\w+")
                val matches = regex.findAll(inputString)
                if (matches.none()) {
                    // No mentions, simply set the text
                    content.text = inputString
                } else {
                    val highlightColor: Int by lazy {
                        ContextCompat.getColor(content.context, R.color.bluejeans)
                    }
                    val spannableString = SpannableString(inputString)

                    matches.forEach {
                        val start = it.range.first
                        val end = it.range.last + 1 // +1 to include the "@" symbol

                        // Set blue color to the mentioned user
                        spannableString.setSpan(
                            ForegroundColorSpan(highlightColor),
                            start,
                            end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }

                    content.text = spannableString
                }
                time.text = TimeUtils.formatMongoTimestamp(currentItem.createdAt)

                val avatar = com.uyscuti.social.network.api.response.comment.allcomments.Avatar(
                    _id = currentItem.author!!.account.avatar._id,
                    url = currentItem.author!!.account.avatar.url,
                    localPath = currentItem.author!!.account.avatar.localPath
                )
                val account = com.uyscuti.social.network.api.response.comment.allcomments.Account(
                    _id = currentItem.author!!.account._id,
                    avatar = avatar,
                    email = currentItem.author!!.account.email,
                    username = currentItem.author!!.account.username
                )

                val author = com.uyscuti.social.network.api.response.comment.allcomments.Author(
                    _id = currentItem.author!!._id,
                    account = account,
                    firstName = currentItem.author!!.firstName,
                    lastName = currentItem.author!!.lastName,
                    avatar = null
                )


                val commentReply = com.uyscuti.social.circuit.data.model.Comment(
                    __v = currentItem.__v,
                    _id = currentItem.commentId,
                    author = author,
                    content = currentItem.content,
                    createdAt = currentItem.createdAt,
                    isLiked = currentItem.isLiked,
                    likes = currentItem.likes,
                    postId = postId,
                    updatedAt = currentItem.updatedAt,
                    replyCount = 0,
                    replies = mutableListOf(),
                    images = data.images,
                    audios = data.audios,
                    docs = data.docs,
                    videos = data.videos,
                    thumbnail = data.thumbnail,
                    gifs = data.gifs,
                    contentType = currentItem.contentType,
                    localUpdateId = data.localUpdateId
                )
                reply.setOnClickListener {
                    // Handle the click event for the reply button
                    EventBus.getDefault().post(ToggleReplyToTextView(data, commentReply.__v))
                }

                Glide.with(profilePic.context)
                    .load(currentItem.author!!.account.avatar.url)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .placeholder(R.drawable.flash21)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(profilePic)


                if (currentItem.images.size > 0) {
                    Log.d("currentItemData", "images ${currentItem.images}")
                    imageUrl = currentItem.images[0].url
                    if (imageUrl.isNotEmpty()) {
                        Log.d("currentItemData", "Image url is not empty for holder")
                        Glide.with(context)
                            .load(currentItem.images[0].url)
                            .placeholder(R.drawable.flash21)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(imageComment)
                    } else {
                        Log.d("currentItemData", "Image url is empty for holder")
                    }

                }

                imageComment.setOnClickListener {
                    val intent = Intent(context, ViewImagesActivity::class.java)
                    intent.putExtra("imageUrl", imageUrl)
                    intent.putExtra("owner", owner)
                    intent.putExtra("displayLikeButton", true)
                    intent.putExtra("updateReplyLike", true)
                    intent.putExtra("position", commentReply.__v)
                    intent.putExtra("data", data)
                    intent.putExtra("currentItem", currentItem)

                    (context as Activity).startActivityForResult(intent, R_CODE)

                }


                if (currentItem.likes == 0) {
                    likesCount.visibility = View.GONE
                } else if (currentItem.likes == 1) {
                    likesCount.visibility = View.VISIBLE
                    likesCount.text = "${currentItem.likes} like"
                } else {
                    likesCount.visibility = View.VISIBLE
                    likesCount.text = "${currentItem.likes} likes"
                }

                if (currentItem.isLiked) {
                    likeUnLikeComment.text = "Like"
                    likeUnLikeComment.setTextColor(ContextCompat.getColor(context, R.color.dark_gray))
                } else {
                    likeUnLikeComment.text = "Like"
                    likeUnLikeComment.setTextColor(ContextCompat.getColor(context, R.color.bluejeans))
                }

                likeUnLikeComment.setOnClickListener {
                    currentItem.isLiked = !currentItem.isLiked

                    EventBus.getDefault()
                        .post(LikeCommentReply(currentItem, data, commentReply.__v))

                    if (currentItem.isLiked) {
                        likeUnLikeComment.text = "Like"
                        likeUnLikeComment.setTextColor(ContextCompat.getColor(context, R.color.dark_gray))
                        YoYo.with(Techniques.Tada)
                            .duration(700)
                            .repeat(1)
                            .playOn(likeUnLikeComment)
                    } else {
                        likeUnLikeComment.text = "Like"
                        likeUnLikeComment.setTextColor(ContextCompat.getColor(context, R.color.bluejeans))
                        YoYo.with(Techniques.Tada)
                            .duration(700)
                            .repeat(1)
                            .playOn(likeUnLikeComment)
                    }
                }
            }

        }

    }

    inner class AudioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val TAG = "Reply Comment Audio ViewHolder"

        // Add references to audio views here
        val binding = CommentAudioItemReplyBinding.bind(itemView)

        @SuppressLint("SetTextI18n", "DefaultLocale")
        fun bind(currentItem: Comment) {
            binding.apply {
                Log.d("currentItemData", "currentItemContent ${currentItem.content}")
                val audioUrl = currentItem.audios[0].url

                var audioDuration = 0L
                if (currentItem.duration.isNotEmpty()) {
                    audioDuration = AudioDurationHelper.reverseFormattedDuration(currentItem.duration)
                }

                // Handle mentions in content (if needed for future text display)
                val inputString = currentItem.content
                val regex = Regex("@\\w+")
                val matches = regex.findAll(inputString)

                if (matches.none()) {
                    // No mentions, simply set the text
                    // content.text = inputString
                } else {
                    val highlightColor: Int by lazy {
                        ContextCompat.getColor(itemView.context, R.color.bluejeans)
                    }
                    val spannableString = SpannableString(inputString)

                    matches.forEach {
                        val start = it.range.first
                        val end = it.range.last + 1 // +1 to include the "@" symbol

                        // Set blue color to the mentioned user
                        spannableString.setSpan(
                            ForegroundColorSpan(highlightColor),
                            start,
                            end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                    // content.text = spannableString
                }

                Log.d("audioDuration", "reverse duration $audioDuration")

                // Create comment reply object
                val avatar = com.uyscuti.social.network.api.response.comment.allcomments.Avatar(
                    _id = currentItem.author!!.account.avatar._id,
                    url = currentItem.author!!.account.avatar.url,
                    localPath = currentItem.author!!.account.avatar.localPath
                )
                val account = com.uyscuti.social.network.api.response.comment.allcomments.Account(
                    _id = currentItem.author!!.account._id,
                    avatar = avatar,
                    email = currentItem.author!!.account.email,
                    username = currentItem.author!!.account.username
                )

                val author = com.uyscuti.social.network.api.response.comment.allcomments.Author(
                    _id = currentItem.author!!._id,
                    account = account,
                    firstName = currentItem.author!!.firstName,
                    lastName = currentItem.author!!.lastName,
                    avatar = null
                )

                val commentReply = com.uyscuti.social.circuit.data.model.Comment(
                    __v = currentItem.__v,
                    _id = currentItem.commentId,
                    author = author,
                    content = currentItem.content,
                    createdAt = currentItem.createdAt,
                    isLiked = currentItem.isLiked,
                    likes = currentItem.likes,
                    postId = postId,
                    updatedAt = currentItem.updatedAt,
                    replyCount = 0,
                    replies = mutableListOf(),
                    images = data.images,
                    audios = data.audios,
                    docs = data.docs,
                    videos = data.videos,
                    thumbnail = data.thumbnail,
                    gifs = data.gifs,
                    contentType = data.contentType,
                    isReplyPlaying = data.isReplyPlaying,
                    progress = currentItem.progress,
                    isPlaying = currentItem.isPlaying,
                    localUpdateId = data.localUpdateId
                )

                // Set username
                username.text = currentItem.author!!.account.username

                // Handle audio playback state
                if (mPlayingPosition == absoluteAdapterPosition) {
                    Log.d(
                        "CurrentProgress",
                        "(with pause icon)->Current Item Progress ${currentItem.progress} is playing ${currentItem.isPlaying}"
                    )
                    playVnAudioBtn.setImageResource(R.drawable.baseline_pause_black)
                    currentComment = currentItem
                    audioDurationTVCount.visibility = View.VISIBLE
                    secondAudioDurationTV.visibility = View.GONE

                    if (currentItem.fileType == "vnAudio") {
                        mCurrentWaveForm = wave
                        wave.visibility = View.VISIBLE
                        secondWave.visibility = View.GONE
                        commentAudioSeekBar.visibility = View.GONE
                        secondCommentAudioSeekBar.visibility = View.GONE
                        secondCurrentWaveForm = secondWave
                    } else {
                        mCurrentSeekBar = commentAudioSeekBar
                        commentAudioSeekBar.visibility = View.VISIBLE
                        secondCommentAudioSeekBar.visibility = View.GONE
                        secondWave.visibility = View.GONE
                        wave.visibility = View.GONE
                        secondCurrentSeekBar = secondCommentAudioSeekBar
                    }
                } else {
                    previousPosition = absoluteAdapterPosition
                    playVnAudioBtn.setImageResource(R.drawable.play_svgrepo_com)
                    audioDurationTVCount.visibility = View.GONE
                    secondAudioDurationTV.visibility = View.VISIBLE

                    if (currentItem.fileType == "vnAudio") {
                        wave.visibility = View.GONE
                        secondWave.visibility = View.VISIBLE
                        commentAudioSeekBar.visibility = View.GONE
                        secondCommentAudioSeekBar.visibility = View.GONE

                        secondWave.onProgressChanged = object : SeekBarOnProgressChanged {
                            override fun onProgressChanged(
                                waveformSeekBar: WaveformSeekBar,
                                progress: Float,
                                fromUser: Boolean
                            ) {
                                secondAudioDurationTV.text = String.format(
                                    "%s",
                                    TrimVideoUtils.stringForTime(progress)
                                )
                            }

                            override fun onRelease(event: MotionEvent?, progress: Float) {
                                Log.d(
                                    "MotionEventReplyAdapter",
                                    "(audioWave)->onRelease:: user stopped seeking "
                                )
                            }
                        }
                    } else {
                        secondCommentAudioSeekBar.visibility = View.VISIBLE
                        commentAudioSeekBar.visibility = View.GONE
                        secondWave.visibility = View.GONE
                        wave.visibility = View.GONE

                        secondCommentAudioSeekBar.max = audioDuration.toInt()
                        secondCommentAudioSeekBar.setOnSeekBarChangeListener(object :
                            SeekBar.OnSeekBarChangeListener {
                            override fun onProgressChanged(
                                seekBar: SeekBar?,
                                progress: Int,
                                fromUser: Boolean
                            ) {
                                Log.d(
                                    "secondCommentAudioSeekBar",
                                    "Progress $progress on position $absoluteAdapterPosition, playing position $mPlayingPosition"
                                )

                                secondAudioDurationTV.text = String.format(
                                    "%s",
                                    TrimVideoUtils.stringForTime(progress.toFloat())
                                )

                                if (fromUser) {
                                    Log.d(
                                        "secondCommentAudioSeekBar",
                                        "secondCommentAudioSeekBar from user progress: $progress"
                                    )
                                    secondAudioDurationTV.text = String.format(
                                        "%s",
                                        TrimVideoUtils.stringForTime(progress.toFloat())
                                    )
                                }
                            }

                            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                        })
                    }
                }

                // Set total audio duration
                audioDurationTV.text = currentItem.duration

                // Handle waveform for voice notes
                if (currentItem.fileType == "vnAudio") {
                    CoroutineScope(Dispatchers.IO).launch {
                        val cachedSample = getSample(audioUrl)

                        if (cachedSample != null) {
                            wave.setSampleFrom(cachedSample)
                            secondWave.setSampleFrom(cachedSample)
                        } else {
                            WaveFormExtractor.getSampleFrom(itemView.context, audioUrl) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    wave.setSampleFrom(it)
                                    secondWave.setSampleFrom(it)
                                    val sample = wave.sample
                                    try {
                                        if (sample != null) {
                                            setCacheSample(sample, audioUrl)
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        }

                        CoroutineScope(Dispatchers.Main).launch {
                            wave.maxProgress = audioDuration.toFloat()
                            secondWave.maxProgress = audioDuration.toFloat()

                            wave.progress = currentItem.progress
                            secondWave.progress = wave.progress

                            secondAudioDurationTV.text = String.format(
                                "%s",
                                TrimVideoUtils.stringForTime(wave.progress)
                            )

                            wave.onProgressChanged = object : SeekBarOnProgressChanged {
                                override fun onProgressChanged(
                                    waveformSeekBar: WaveformSeekBar,
                                    progress: Float,
                                    fromUser: Boolean
                                ) {
                                    wave.visibility = View.VISIBLE
                                    secondWave.visibility = View.GONE

                                    Log.d(
                                        "OnSeekBarChangeListener",
                                        "Progress $progress on position $absoluteAdapterPosition, playing position $mPlayingPosition"
                                    )

                                    if (fromUser) {
                                        listener.toggleAudioPlayer(
                                            playVnAudioBtn,
                                            currentItem.audios[0].url,
                                            commentReply.__v,
                                            true,
                                            progress,
                                            isSeeking = true,
                                            seekTo = false,
                                            isVnAudio = true
                                        )
                                    }

                                    audioDurationTVCount.text = String.format(
                                        "%s",
                                        TrimVideoUtils.stringForTime(currentItem.progress)
                                    )
                                    currentItem.progress = progress
                                }

                                override fun onRelease(event: MotionEvent?, progress: Float) {
                                    Log.d(
                                        "MotionEventReplyAdapter",
                                        "(audioWave)->onRelease:: user stopped seeking progress $progress"
                                    )
                                    listener.toggleAudioPlayer(
                                        playVnAudioBtn,
                                        currentItem.audios[0].url,
                                        commentReply.__v,
                                        true,
                                        progress,
                                        isSeeking = false,
                                        seekTo = true,
                                        isVnAudio = true
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Handle regular audio files with SeekBar
                    commentAudioSeekBar.max = audioDuration.toInt()
                    commentAudioSeekBar.progress = currentItem.progress.toInt()
                    secondCommentAudioSeekBar.progress = commentAudioSeekBar.progress
                    secondAudioDurationTV.text = String.format(
                        "%s",
                        TrimVideoUtils.stringForTime(commentAudioSeekBar.progress.toFloat())
                    )

                    commentAudioSeekBar.setOnSeekBarChangeListener(object :
                        SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(
                            seekBar: SeekBar?,
                            progress: Int,
                            fromUser: Boolean
                        ) {
                            Log.d(
                                "OnSeekBarChangeListener",
                                "Progress $progress on position $absoluteAdapterPosition, playing position $mPlayingPosition"
                            )

                            commentAudioSeekBar.visibility = View.VISIBLE
                            secondCommentAudioSeekBar.visibility = View.GONE
                            commentAudioSeekBar.progress = progress

                            if (fromUser) {
                                listener.toggleAudioPlayer(
                                    playVnAudioBtn,
                                    currentItem.audios[0].url,
                                    commentReply.__v,
                                    true,
                                    progress.toFloat(),
                                    isSeeking = false,
                                    seekTo = true,
                                    isVnAudio = false
                                )

                                currentItem.progress = progress.toFloat()
                                Log.d(
                                    "OnSeekBarChangeListener",
                                    "User is touching seek bar data progress ${currentItem.progress} data is playing ${currentItem.isPlaying}"
                                )
                                audioDurationTVCount.text = String.format(
                                    "%s",
                                    TrimVideoUtils.stringForTime(progress.toFloat())
                                )
                            }

                            currentItem.progress = progress.toFloat()
                            audioDurationTVCount.text = String.format(
                                "%s",
                                TrimVideoUtils.stringForTime(progress.toFloat())
                            )
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                    })
                }

                // Set timestamp
                time.text = TimeUtils.formatMongoTimestamp(currentItem.createdAt)

                // Handle reply button click
                reply.setOnClickListener {
                    EventBus.getDefault().post(ToggleReplyToTextView(data, commentReply.__v))
                }

                // Handle like button and like count display
                likeUnLikeComment.setOnClickListener {
                    // Handle like button click
                    // Add your like handling logic here
                }

                // Display like count according to XML structure
                when (currentItem.likes) {
                    0 -> {
                        likeCountContainer.visibility = View.GONE
                    }
                    1 -> {
                        likeCountContainer.visibility = View.VISIBLE
                        likesCount.text = "1"
                    }
                    else -> {
                        likeCountContainer.visibility = View.VISIBLE
                        likesCount.text = "${currentItem.likes}"
                    }
                }

                // Load profile picture
                Glide.with(profilePic.context)
                    .load(currentItem.author!!.account.avatar.url)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .placeholder(R.drawable.flash21)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(profilePic)

                // Handle play button click
                playVnAudioBtn.setOnClickListener {
                    isPlay = !isPlay
                    val wasPlaying = currentItem.isPlaying
                    isPlay = !wasPlaying

                    changeReplyAudioPlayingStatus()
                    currentItem.isPlaying = !wasPlaying

                    if (currentItem.fileType == "vnAudio") {
                        mCurrentWaveForm = wave
                        listener.toggleAudioPlayer(
                            playVnAudioBtn,
                            currentItem.audios[0].url,
                            commentReply.__v,
                            true,
                            secondWave.progress,
                            isSeeking = false,
                            seekTo = false,
                            isVnAudio = true
                        )
                    } else {
                        mCurrentSeekBar = commentAudioSeekBar
                        listener.toggleAudioPlayer(
                            playVnAudioBtn,
                            currentItem.audios[0].url,
                            commentReply.__v,
                            true,
                            secondCommentAudioSeekBar.progress.toFloat(),
                            isSeeking = false,
                            seekTo = false,
                            isVnAudio = false
                        )
                    }

                    if (previousPosition != -1) {
                        // Handle previous position cleanup
                    }

                    mCurrentPosition = absoluteAdapterPosition
                    mPlayingPosition = if (mPlayingPosition == absoluteAdapterPosition) -1 else absoluteAdapterPosition

                    mPlayListener?.onPlay(mPlayingPosition)

                    if (isPlay) {
                        if (currentItem.fileType == "vnAudio") {
                            mPlayListener?.isPlaying(absoluteAdapterPosition, isPlay, wave.progress)
                        } else {
                            mPlayListener?.isPlaying(
                                absoluteAdapterPosition,
                                isPlay,
                                commentAudioSeekBar.progress.toFloat()
                            )
                        }
                    } else {
                        if (currentItem.fileType == "vnAudio") {
                            mPlayListener?.isPlaying(absoluteAdapterPosition, isPlay, wave.progress)
                        } else {
                            mPlayListener?.isPlaying(
                                absoluteAdapterPosition,
                                isPlay,
                                commentAudioSeekBar.progress.toFloat()
                            )
                        }
                    }

                    if (currentItem.fileType == "vnAudio") {
                        EventBus.getDefault().post(
                            AudioPlayerHandler(
                                audioUrl,
                                wave,
                                audioDurationTVCount,
                                wave.progress,
                                absoluteAdapterPosition
                            )
                        )
                    } else {
                        val audioMaxDuration = AudioDurationHelper.reverseFormattedDuration(currentItem.duration)
                        EventBus.getDefault().post(
                            CommentAudioPlayerHandler(
                                audioUrl,
                                commentAudioSeekBar,
                                audioDurationTVCount,
                                commentAudioSeekBar.progress.toFloat(),
                                absoluteAdapterPosition,
                                audioMaxDuration
                            )
                        )
                    }

                    currentItem.isPlaying = !wasPlaying
                    notifyItemChanged(absoluteAdapterPosition)
                }

                if (currentItem.likes == 0) {
                    likesCount.visibility = View.GONE
                } else if (currentItem.likes == 1) {
                    likesCount.visibility = View.VISIBLE
                    likesCount.text = "${currentItem.likes} like"
                } else {
                    likesCount.visibility = View.VISIBLE
                    likesCount.text = "${currentItem.likes} likes"
                }

                if (currentItem.isLiked) {
                    likeUnLikeComment.text = "Like"
                    likeUnLikeComment.setTextColor(ContextCompat.getColor(context, R.color.dark_gray))
                } else {
                    likeUnLikeComment.text = "Like"
                    likeUnLikeComment.setTextColor(ContextCompat.getColor(context, R.color.bluejeans))
                }

                likeUnLikeComment.setOnClickListener {
                    currentItem.isLiked = !currentItem.isLiked

                    EventBus.getDefault()
                        .post(LikeCommentReply(currentItem, data, commentReply.__v))

                    if (currentItem.isLiked) {
                        likeUnLikeComment.text = "Like"
                        likeUnLikeComment.setTextColor(ContextCompat.getColor(context, R.color.dark_gray))
                        YoYo.with(Techniques.Tada)
                            .duration(700)
                            .repeat(1)
                            .playOn(likeUnLikeComment)
                    } else {
                        likeUnLikeComment.text = "Like"
                        likeUnLikeComment.setTextColor(ContextCompat.getColor(context, R.color.bluejeans))
                        YoYo.with(Techniques.Tada)
                            .duration(700)
                            .repeat(1)
                            .playOn(likeUnLikeComment)
                    }
                }
            }
        }
    }

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val TAG = "Reply Comment Video ViewHolder"

        val binding = CommentVideoItemReplyBinding.bind(itemView)

        @SuppressLint("SetTextI18n")
        fun bind(currentItem: Comment) {
            binding.apply {

                Log.d("currentItemData", "currentItemContent ${currentItem.content}")
                var videoUrl = ""
                val owner = currentItem.author!!.account.username
                username.text = currentItem.author!!.account.username

                time.text = TimeUtils.formatMongoTimestamp(currentItem.createdAt)
                val inputString = currentItem.content
                val regex = Regex("@\\w+")
                val matches = regex.findAll(inputString)
                if (matches.none()) {
                    // No mentions, simply set the text
                    content.text = inputString
                } else {
                    val highlightColor: Int by lazy {
                        ContextCompat.getColor(content.context, R.color.bluejeans)
                    }
                    val spannableString = SpannableString(inputString)

                    matches.forEach {
                        val start = it.range.first
                        val end = it.range.last + 1 // +1 to include the "@" symbol

                        // Set blue color to the mentioned user
                        spannableString.setSpan(
                            ForegroundColorSpan(highlightColor),
                            start,
                            end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }

                    content.text = spannableString
                }
                val avatar = com.uyscuti.social.network.api.response.comment.allcomments.Avatar(
                    _id = currentItem.author!!.account.avatar._id,
                    url = currentItem.author!!.account.avatar.url,
                    localPath = currentItem.author!!.account.avatar.localPath
                )
                val account = com.uyscuti.social.network.api.response.comment.allcomments.Account(
                    _id = currentItem.author!!.account._id,
                    avatar = avatar,
                    email = currentItem.author!!.account.email,
                    username = currentItem.author!!.account.username
                )
                val author = com.uyscuti.social.network.api.response.comment.allcomments.Author(
                    _id = currentItem.author!!._id,
                    account = account,
                    firstName = currentItem.author!!.firstName,
                    lastName = currentItem.author!!.lastName,
                    avatar = null
                )

                val commentReply = com.uyscuti.social.circuit.data.model.Comment(
                    __v = currentItem.__v,
                    _id = currentItem.commentId,
                    author = author,
                    content = currentItem.content,
                    createdAt = currentItem.createdAt,
                    isLiked = currentItem.isLiked,
                    likes = currentItem.likes,
                    postId = postId,
                    updatedAt = currentItem.updatedAt,
                    replyCount = 0,
                    replies = mutableListOf(),
                    images = data.images,
                    audios = data.audios,
                    docs = data.docs,
                    videos = data.videos,
                    thumbnail = data.thumbnail,
                    gifs = data.gifs,
                    contentType = currentItem.contentType,
                    localUpdateId = data.localUpdateId
                )
                reply.setOnClickListener {
                    // Handle the click event for the reply button
                    EventBus.getDefault().post(ToggleReplyToTextView(data, commentReply.__v))
                }

                Glide.with(profilePic.context)
                    .load(currentItem.author!!.account.avatar.url)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .placeholder(R.drawable.flash21)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(profilePic)


                if (currentItem.videos.size > 0) {
                    Log.d("currentItemData", "images ${currentItem.videos}")
                    videoUrl = currentItem.videos[0].url
                    if (videoUrl.isNotEmpty()) {
                        commentVideoDurationTextView.text = currentItem.duration
                        Log.d("currentItemData", "Image url is not empty for holder")
                        Glide.with(context)
                            .load(currentItem.videos[0].url)
                            .placeholder(R.drawable.flash21)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(videoThumbnail)
                    } else {
                        Log.d("currentItemData", "Image url is empty for holder")
                    }

                }

                videoThumbnail.setOnClickListener {
                    Log.d("VideoThumbNail", "Clicked")

                    val intent = Intent(context, CommentVideoPlayerActivity::class.java)
                    intent.putExtra("position", absoluteAdapterPosition)
                    intent.putExtra("data", data)
                    intent.putExtra("videoUrl", videoUrl)
                    intent.putExtra("owner", owner)
                    intent.putExtra("updateReplyLike", true)
                    intent.putExtra("position", commentReply.__v)
                    intent.putExtra("currentItem", currentItem)
                    (context as Activity).startActivityForResult(intent, COMMENT_VIDEO_CODE)
                }

                if (currentItem.likes == 0) {
                    likesCount.visibility = View.GONE
                } else if (currentItem.likes == 1) {
                    likesCount.visibility = View.VISIBLE
                    likesCount.text = "${currentItem.likes} like"
                } else {
                    likesCount.visibility = View.VISIBLE
                    likesCount.text = "${currentItem.likes} likes"
                }

                if (currentItem.isLiked) {
                    likeUnLikeComment.text = "Like"
                    likeUnLikeComment.setTextColor(ContextCompat.getColor(context, R.color.dark_gray))
                } else {
                    likeUnLikeComment.text = "Like"
                    likeUnLikeComment.setTextColor(ContextCompat.getColor(context, R.color.bluejeans))
                }

                likeUnLikeComment.setOnClickListener {
                    currentItem.isLiked = !currentItem.isLiked

                    EventBus.getDefault()
                        .post(LikeCommentReply(currentItem, data, commentReply.__v))

                    if (currentItem.isLiked) {
                        likeUnLikeComment.text = "Like"
                        likeUnLikeComment.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.dark_gray
                            )
                        )
                        YoYo.with(Techniques.Tada)
                            .duration(700)
                            .repeat(1)
                            .playOn(likeUnLikeComment)
                    } else {
                        likeUnLikeComment.text = "Like"
                        likeUnLikeComment.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.bluejeans
                            )
                        )
                        YoYo.with(Techniques.Tada)
                            .duration(700)
                            .repeat(1)
                            .playOn(likeUnLikeComment)
                    }

                }

            }

        }

    }

    inner class DocumentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val TAG = "Reply Comment Document ViewHolder"

        val binding = CommentDocumentItemReplyBinding.bind(itemView)

        @SuppressLint("SetTextI18n")
        fun bind(currentItem: Comment) {
            binding.apply {

                Log.d("currentItemData", "currentItemContent ${currentItem.content}")
                var docsUrl = ""
                val owner = currentItem.author!!.account.username
                username.text = currentItem.author!!.account.username

                time.text = TimeUtils.formatMongoTimestamp(currentItem.createdAt)
                val inputString = currentItem.content
                val regex = Regex("@\\w+")
                val matches = regex.findAll(inputString)
                if (matches.none()) {
                    // No mentions, simply set the text
                    content.text = inputString
                } else {
                    val highlightColor: Int by lazy {
                        ContextCompat.getColor(content.context, R.color.bluejeans)
                    }
                    val spannableString = SpannableString(inputString)

                    matches.forEach {
                        val start = it.range.first
                        val end = it.range.last + 1 // +1 to include the "@" symbol

                        // Set blue color to the mentioned user
                        spannableString.setSpan(
                            ForegroundColorSpan(highlightColor),
                            start,
                            end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }

                    content.text = spannableString
                }
                val avatar = com.uyscuti.social.network.api.response.comment.allcomments.Avatar(
                    _id = currentItem.author!!.account.avatar._id,
                    url = currentItem.author!!.account.avatar.url,
                    localPath = currentItem.author!!.account.avatar.localPath
                )
                val account = com.uyscuti.social.network.api.response.comment.allcomments.Account(
                    _id = currentItem.author!!.account._id,
                    avatar = avatar,
                    email = currentItem.author!!.account.email,
                    username = currentItem.author!!.account.username
                )
                val author = com.uyscuti.social.network.api.response.comment.allcomments.Author(
                    _id = currentItem.author!!._id,
                    account = account,
                    firstName = currentItem.author!!.firstName,
                    lastName = currentItem.author!!.lastName,
                    avatar = null
                )

                val commentReply = com.uyscuti.social.circuit.data.model.Comment(
                    __v = currentItem.__v,
                    _id = currentItem.commentId,
                    author = author,
                    content = currentItem.content,
                    createdAt = currentItem.createdAt,
                    isLiked = currentItem.isLiked,
                    likes = currentItem.likes,
                    postId = postId,
                    updatedAt = currentItem.updatedAt,
                    replyCount = 0,
                    replies = mutableListOf(),
                    images = data.images,
                    audios = data.audios,
                    docs = data.docs,
                    videos = data.videos,
                    thumbnail = data.thumbnail,
                    gifs = data.gifs,
                    contentType = currentItem.contentType,
                    localUpdateId = data.localUpdateId
                )
                reply.setOnClickListener {
                    // Handle the click event for the reply button
                    EventBus.getDefault().post(ToggleReplyToTextView(data, commentReply.__v))
                }

                Glide.with(profilePic.context)
                    .load(currentItem.author!!.account.avatar.url)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .placeholder(R.drawable.flash21)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(profilePic)


                // Load comment image if available
                if (data.docs.isNotEmpty() && data.docs[0].url.isNotEmpty()) {
                    docsUrl = data.docs[0].url
                    Log.d(TAG, "Document URL is not empty for holder")

                    Glide.with(itemView.context)
                        .load(docsUrl)
                        .placeholder(R.drawable.flash21)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(documentImageView)
                    documentLayout.visibility = View.VISIBLE

                } else {
                    Log.d(TAG, "Document URL is empty for holder")
                    documentLayout.visibility = View.GONE
                }

                // Handle document display
                if (data.docs.isNotEmpty()) {
                    docsUrl = data.docs[0].url
                    if (docsUrl.isNotEmpty()) {
                        Log.d(TAG, "Loading Document Thumbnail from: $docsUrl")
                        Glide.with(itemView.context)
                            .load(docsUrl)
                            .placeholder(R.drawable.documents)
                            .error(R.drawable.documents)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(documentImageView)

                        docTitle.text = data.fileName
                        docInfo.text = "${data.numberOfPages} pages"
                        documentLayout.visibility = View.VISIBLE

                    } else {
                        Log.d(TAG, "Document URL is empty for holder")
                        documentLayout.visibility = View.GONE

                    }
                } else {
                    documentLayout.visibility = View.GONE
                }


                if (currentItem.likes == 0) {
                    likesCount.visibility = View.GONE
                } else if (currentItem.likes == 1) {
                    likesCount.visibility = View.VISIBLE
                    likesCount.text = "${currentItem.likes} Like"
                } else {
                    likesCount.visibility = View.VISIBLE
                    likesCount.text = "${currentItem.likes} Likes"
                }

                if (currentItem.isLiked) {
                    likeUnLikeComment.text = "Like"
                    likeUnLikeComment.setTextColor(ContextCompat.getColor(context, R.color.dark_gray))
                } else {
                    likeUnLikeComment.text = "Like"
                    likeUnLikeComment.setTextColor(ContextCompat.getColor(context, R.color.bluejeans))
                }

                likeUnLikeComment.setOnClickListener {
                    currentItem.isLiked = !currentItem.isLiked

                    EventBus.getDefault()
                        .post(LikeCommentReply(currentItem, data, commentReply.__v))

                    if (currentItem.isLiked) {
                        likeUnLikeComment.text = "Like"
                        likeUnLikeComment.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.dark_gray
                            )
                        )
                        YoYo.with(Techniques.Tada)
                            .duration(700)
                            .repeat(1)
                            .playOn(likeUnLikeComment)
                    } else {
                        likeUnLikeComment.text = "Like"
                        likeUnLikeComment.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.bluejeans
                            )
                        )
                        YoYo.with(Techniques.Tada)
                            .duration(700)
                            .repeat(1)
                            .playOn(likeUnLikeComment)
                    }

                }


            }

        }

    }

    override fun getItemViewType(position: Int): Int {
        val currentItem = itemList[position]
        return when (currentItem.contentType) {
            "text" -> VIEW_TYPE_TEXT
            "audio" -> VIEW_TYPE_AUDIO
            "image" -> VIEW_TYPE_IMAGE
            "video" -> VIEW_TYPE_VIDEO
            "docs" -> VIEW_TYPE_DOCUMENT
            "gif" -> VIEW_TYPE_GIF
            else -> VIEW_TYPE_EMPTY
        }
    }

    // ViewHolder class to hold the views
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = CommentReplyBinding.bind(itemView)
    }

    fun changeReplyAudioPlayingStatus() {
        Log.d("changeReplyAudioPlayingStatus", "changeReplyAudioPlayingStatus")
        for (i in itemList.indices) {
            val item: Comment = itemList[i]

            if (item.isPlaying) { // Assuming there's a method to check if the item is playing
                // If an item with isPlaying true is found, update its position and set it to false
                Log.d(
                    "changeReplyAudioPlayingStatus",
                    "changeReplyAudioPlayingStatus item playing position $i"
                )

                item.isPlaying = false
                item.progress = 0f


                itemList[i] = item
                notifyItemChanged(i)
            }


        }
    }

    fun refreshPosition() {
        Log.i("refreshPosition", "refresh position")
        Log.d(
            "refreshPosition",
            "refreshPosition: current wave progress ${mCurrentWaveForm?.progress}"
        )
        if (mCurrentPosition != -1) {
            Log.d("refreshPosition", "refreshPosition: mCurrentPosition $mCurrentPosition")
            val last = mCurrentPosition
            mCurrentPosition = -1
            mPlayingPosition = -1
            changeReplyAudioPlayingStatus()
            notifyItemChanged(last)
        } else {
            Log.i("refreshPosition", "(else)-refresh position")

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_TEXT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.comment_reply, parent, false)
                TextViewHolder(view)
            }

            VIEW_TYPE_AUDIO -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.comment_audio_item_reply, parent, false)
                AudioViewHolder(view)
            }

            VIEW_TYPE_IMAGE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.comment_image_item_reply, parent, false)
                ImageViewHolder(view)
            }

            VIEW_TYPE_VIDEO -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.comment_video_item_reply, parent, false)
                VideoViewHolder(view)
            }

            VIEW_TYPE_DOCUMENT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.comment_document_item_reply, parent, false)
                DocumentViewHolder(view)
            }
            VIEW_TYPE_GIF -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.comment_image_item_reply, parent, false)
                GifViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.comment_reply, parent, false)
                TextViewHolder(view)
            }
        }
    }

    // Bind data to the views
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = itemList[position]
        when (holder) {
            is TextViewHolder -> {
                holder.bind(currentItem)
            }

            is AudioViewHolder -> {
                holder.bind(currentItem)
            }

            is ImageViewHolder -> {
                holder.bind(currentItem)
            }

            is VideoViewHolder -> {
                holder.bind(currentItem)
            }

            is DocumentViewHolder -> {
                holder.bind(currentItem)
            }
            is GifViewHolder -> {
                holder.bind(currentItem)
            }
        }
    }

    // Return the size of the dataset
    override fun getItemCount(): Int {
        return itemList.size
    }


    interface OnPlayListener {
        fun onPlay(position: Int)
        fun isPlaying(position: Int, isPlaying: Boolean, progress: Float)
        fun refreshParent(position: Int)
    }

}

