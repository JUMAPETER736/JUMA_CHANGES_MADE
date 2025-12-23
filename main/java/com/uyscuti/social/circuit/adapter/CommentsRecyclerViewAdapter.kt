package com.uyscuti.social.circuit.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
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
import com.uyscuti.social.circuit.data.model.Comment
import com.uyscuti.social.circuit.model.AudioPlayerHandler
import com.uyscuti.social.circuit.model.CommentAudioPlayerHandler
import com.uyscuti.social.circuit.model.ToggleReplyToTextView
import com.uyscuti.social.circuit.User_Interface.media.CommentVideoPlayerActivity
import com.uyscuti.social.circuit.User_Interface.media.ViewImagesActivity
import com.uyscuti.social.circuit.utils.AudioDurationHelper.reverseFormattedDuration
import com.uyscuti.social.circuit.utils.COMMENT_VIDEO_CODE
import com.uyscuti.social.circuit.utils.R_CODE
import com.uyscuti.social.circuit.utils.TrimVideoUtils
import com.uyscuti.social.circuit.utils.WaveFormExtractor
import com.uyscuti.social.circuit.utils.waveformseekbar.SeekBarOnProgressChanged
import com.uyscuti.social.circuit.utils.waveformseekbar.WaveformSeekBar
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.adapter.notifications.AdPaginatedAdapter
import com.uyscuti.social.circuit.adapter.shorts.ReplyCommentAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


private const val TAG = "CommentsRecyclerViewAdapter"

private const val VIEW_TYPE_TEXT_COMMENT = 0
private const val VIEW_TYPE_AUDIO_COMMENT = 1
private const val VIEW_TYPE_IMAGE_COMMENT = 2
private const val VIEW_TYPE_VIDEO_COMMENT = 3
private const val VIEW_TYPE_DOCUMENT_COMMENT = 4
private const val VIEW_TYPE_GIF = 5
private const val VIEW_TYPE_EMPTY = 10



class CommentsRecyclerViewAdapter(
    private val context: Context,
    private val onViewReplies: OnViewRepliesClickListener,


    ) :
    AdPaginatedAdapter<RecyclerView.ViewHolder>() {

    private lateinit var replyCommentAdapter: ReplyCommentAdapter
    private var mPlayingPosition = -1
    private var mParentComment = -1
    private var mReplyPosition = -1

    private var currentComment: Comment? = null

    private var mCurrentWaveForm: WaveformSeekBar? = null
    private var mCurrentSeekBar: SeekBar? = null

    private var secondCurrentWaveForm: WaveformSeekBar? = null
    private var secondCurrentSeekBar: SeekBar? = null

    private var secondAudioDurationTV: TextView? = null
    private val audioWave: WaveformSeekBar? = null
    private var playingMainCommentPosition = -1

    fun getComment(position: Int): Comment {
        return getItem(position)
    }


    inner class CommentGifViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val TAG = "Comment Gif ViewHolder"

        private val imageView: ImageView = itemView.findViewById(R.id.profilePic)
        private val imageComment: ImageView = itemView.findViewById(R.id.imageComment)
        private val likeUnLikeCommentImageView: ImageView =
            itemView.findViewById(R.id.likeUnLikeComment)
        private val likeButton: LinearLayout = itemView.findViewById(R.id.likesCount)
        private val username: TextView = itemView.findViewById(R.id.username)

        //        private val content: TextView = itemView.findViewById(R.id.content)
        private val time: TextView = itemView.findViewById(R.id.time)
        private val reply: TextView = itemView.findViewById(R.id.reply)
        private val commentReplies: TextView = itemView.findViewById(R.id.commentReplies)
        private val hideCommentReplies: TextView = itemView.findViewById(R.id.hideCommentReplies)
        private val likesCount: TextView = itemView.findViewById(R.id.likesCount)

        private val repliesRecyclerView: RecyclerView =
            itemView.findViewById(R.id.repliesRecyclerView)

        private var isReplyCount = false

        private fun formatMongoTimestamp(dateTimeString: String?): String {
            if (dateTimeString.isNullOrBlank()) return "Unknown Time"
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val date = inputFormat.parse(dateTimeString)
                val now = Date()
                val diffInMillis = now.time - (date?.time ?: 0)
                val diffInSeconds = diffInMillis / 1000
                val diffInMinutes = diffInSeconds / 60
                val diffInHours = diffInMinutes / 60
                val diffInDays = diffInHours / 24
                val diffInWeeks = diffInDays / 7
                val diffInMonths = diffInDays / 30 // Approximate
                val diffInYears = diffInDays / 365 // Approximate

                when {
                    diffInSeconds < 60 -> "now"
                    diffInMinutes < 60 -> "${diffInMinutes}m"
                    diffInHours < 24 -> "${diffInHours}h"
                    diffInDays == 1L -> "1d"
                    diffInDays < 7 -> "${diffInDays}d"
                    diffInWeeks == 1L -> "1w"
                    diffInWeeks < 4 -> "${diffInWeeks}w"
                    diffInMonths == 1L -> "a month ago"
                    diffInMonths < 12 -> "${diffInMonths}months"
                    diffInYears == 1L -> "1y"
                    else -> "${diffInYears}years"
                }
            } catch (e: Exception) {
                Log.w("CommentViewHolder", "Failed to format timestamp: $dateTimeString", e)
                "now"
            }
        }

        @SuppressLint("SetTextI18n")
        fun render(data: Comment) {

            var imageUrl = ""
            Log.d(TAG, "data $data")
            val replyCount = data.replyCount
            Glide.with(context)
                .load(data.author!!.account.avatar.url)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .placeholder(R.drawable.flash21)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView)

//            Log.d("")
            if (data.gifs != "") {
                imageUrl = data.gifs
                if (imageUrl.isNotEmpty()) {
                    Log.d(TAG, "Image url is not empty for holder")
                    Glide.with(context)
                        .load(data.gifs)
//                        .apply(RequestOptions.bitmapTransform(CircleCrop()))
                        .placeholder(R.drawable.flash21)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imageComment)
                } else {
                    Log.d(TAG, "Image url is empty for holder")
                }

            }



            try {

                for (commentReply in data.replies) {
                    commentReply.__v = absoluteAdapterPosition
//                    commentReply.
                }

                Log.d("ImageCommentRenderData", "data.replies ${data.replies}")
                if (data.isReplyPlaying) {
//                    mReplyPosition = mReplyPosition
                    replyCommentAdapter =
                        ReplyCommentAdapter(context, data, data.postId, mReplyPosition)

                } else {
                    replyCommentAdapter = ReplyCommentAdapter(context, data, data.postId, -1)
                }

                repliesRecyclerView.adapter = replyCommentAdapter
                replyCommentAdapter.setListener(onViewReplies)
                replyCommentAdapter.setPlayListener(object : ReplyCommentAdapter.OnPlayListener {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onPlay(position: Int) {
                        changePlayingStatus()
                        Log.d(
                            "onPlay",
                            "onPlay: in main comments playing child comment on $position"
                        )
                        mPlayingPosition = -1
                        mParentComment = absoluteAdapterPosition
                        Log.d(
                            "onPlay",
                            "onPlay: in main comments playing parent comment on $mParentComment"
                        )
                        mReplyPosition = position
                    }

                    override fun isPlaying(position: Int, isPlaying: Boolean, progress: Float) {
                        Log.d(TAG, "isPlaying: position $position is playing $isPlaying")


                        data.isReplyPlaying = isPlaying
                        data.replies[position].progress = progress
                        notifyItemChanged(absoluteAdapterPosition)

                    }

                    override fun refreshParent(position: Int) {
                        data.isReplyPlaying = true
                        notifyItemChanged(absoluteAdapterPosition)
                    }

                })
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (replyCount == 0) {
                commentReplies.visibility = View.GONE
                hideCommentReplies.visibility = View.GONE
            } else {


                if (!data.hasNextPage && data.isRepliesVisible) {
                    commentReplies.visibility = View.GONE
                } else {
                    commentReplies.visibility = View.VISIBLE
                    if (data.isRepliesVisible) {
                        if (data.replyCountVisible) {
                            commentReplies.text =
                                if (replyCount == 1) "...View 1 reply" else "...View more replies"
                        } else {
                            commentReplies.visibility = View.GONE
                        }
//                        commentReplies.text =
//                            if (replyCount == 1) "...View 1 reply" else "...View more replies"
                    } else {
                        commentReplies.text =
                            if (replyCount == 1) "...View 1 reply" else "...View $replyCount replies"
                    }
                }



                if (data.isRepliesVisible) {
                    hideCommentReplies.visibility = View.VISIBLE
                    repliesRecyclerView.visibility = View.VISIBLE
                } else {
                    hideCommentReplies.visibility = View.GONE
                    repliesRecyclerView.visibility = View.GONE
                    commentReplies.visibility = View.VISIBLE
                }
            }

            hideCommentReplies.setOnClickListener {
                data.isRepliesVisible = false
//                data.pageNumber = 1
                repliesRecyclerView.visibility = View.GONE
                hideCommentReplies.visibility = View.GONE
                commentReplies.visibility = View.VISIBLE
                commentReplies.text =
                    if (replyCount == 1) "...View 1 reply" else "...View $replyCount replies"
            }

            commentReplies.setOnClickListener {

                repliesRecyclerView.visibility = View.VISIBLE
                data.isRepliesVisible = true
                if (data.isRepliesVisible) {
                    hideCommentReplies.visibility = View.VISIBLE
                } else {
                    hideCommentReplies.visibility = View.GONE
                }

                if (!data.hasNextPage) {
                    commentReplies.visibility = View.GONE
                }
                commentReplies.text = if (replyCount == 1) "...View 1 reply" else "...View more"

                onViewReplies.onViewRepliesClick(
                    data, absoluteAdapterPosition, commentReplies, hideCommentReplies,
                    repliesRecyclerView,
                    data.isRepliesVisible, data.pageNumber
                )
            }


            username.text = data.author.account.username
            val owner = data.author.account.username

            imageComment.setOnClickListener {
                val intent = Intent(context, ViewImagesActivity::class.java)
                intent.putExtra("imageUrl", imageUrl)
                intent.putExtra("owner", owner)
                intent.putExtra("displayLikeButton", true)
                intent.putExtra("position", absoluteAdapterPosition)
                intent.putExtra("data", data)
//                context.startActivity(intent)
                (context as Activity).startActivityForResult(intent, R_CODE)
            }
            val inputString = data.content
            val regex = Regex("@\\w+")

            time.text = formatMongoTimestamp(data.createdAt)

            reply.setOnClickListener {
//                Log.d(TAG, "render: comment to reply on position $absoluteAdapterPosition and id ${data._id}")
                EventBus.getDefault().post(ToggleReplyToTextView(data, absoluteAdapterPosition))
//                onViewReplies.onReplyButtonClick(position = absoluteAdapterPosition, data)
            }

            when (data.likes) {
                0 -> {
                    likeButton.visibility = View.GONE
                }
                1 -> {
                    likeButton.visibility = View.VISIBLE
                    likesCount.text = "1"
                }
                else -> {
                    likeButton.visibility = View.VISIBLE
                    likesCount.text = "${data.likes}"
                }
            }

            // Like button setup
            if (data.isLiked) {
                likesCount.text = "Like"
                likesCount.setTextColor(ContextCompat.getColor(context, R.color.bluejeans))
            } else {
                likesCount.text = "Like"
                likesCount.setTextColor(ContextCompat.getColor(context, R.color.dark_gray))
            }

            likeButton.setOnClickListener {
                data.isLiked = !data.isLiked
                onViewReplies.likeUnLikeComment(absoluteAdapterPosition, data)
                if (data.isLiked) {
                    likesCount.text = "Like"
                    likesCount.setTextColor(ContextCompat.getColor(context, R.color.bluejeans))
                    YoYo.with(Techniques.Tada)
                        .duration(700)
                        .repeat(1)
                        .playOn(likeButton)
                } else {
                    likesCount.text = "Like"
                    likesCount.setTextColor(ContextCompat.getColor(context, R.color.dark_gray))
                }
            }

            // Reply button
            reply.setOnClickListener {
                EventBus.getDefault().post(ToggleReplyToTextView(data, absoluteAdapterPosition))
            }

            // Replies visibility and click listeners
            if (replyCount == 0) {
                commentReplies.visibility = View.GONE
                hideCommentReplies.visibility = View.GONE
                repliesRecyclerView.visibility = View.GONE
            } else {
                if (!data.hasNextPage && data.isRepliesVisible) {
                    commentReplies.visibility = View.GONE
                } else {
                    commentReplies.visibility = View.VISIBLE
                    if (data.isRepliesVisible && !data.replyCountVisible) {
                        commentReplies.visibility = View.GONE
                    } else {
                        commentReplies.text = if (data.isRepliesVisible) {
                            if (replyCount == 1) "...View 1 reply" else "...View more replies"
                        } else {
                            if (replyCount == 1) "...View 1 reply" else "...View $replyCount replies"
                        }
                    }
                }

                if (data.isRepliesVisible) {
                    hideCommentReplies.visibility = View.VISIBLE
                    repliesRecyclerView.visibility = View.VISIBLE
                } else {
                    hideCommentReplies.visibility = View.GONE
                    repliesRecyclerView.visibility = View.GONE
                    commentReplies.visibility = View.VISIBLE
                }
            }

            hideCommentReplies.setOnClickListener {
                data.isRepliesVisible = false
                repliesRecyclerView.visibility = View.GONE
                hideCommentReplies.visibility = View.GONE
                commentReplies.visibility = View.VISIBLE
                commentReplies.text = if (replyCount == 1) "...View 1 reply" else "...View $replyCount replies"
            }

            commentReplies.setOnClickListener {
                repliesRecyclerView.visibility = View.VISIBLE
                data.isRepliesVisible = true
                hideCommentReplies.visibility = View.VISIBLE
                if (!data.hasNextPage) {
                    commentReplies.visibility = View.GONE
                }
                commentReplies.text = if (replyCount == 1) "...View 1 reply" else "...View more"
                onViewReplies.onViewRepliesClick(
                    data, absoluteAdapterPosition, commentReplies, hideCommentReplies,
                    repliesRecyclerView, data.isRepliesVisible, data.pageNumber
                )
            }

        }
    }

    inner class CommentTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val TAG = "Main Comment Text Only ViewHolder"

        private val imageView: ImageView = itemView.findViewById(R.id.profilePic)
        private val likeButton: TextView = itemView.findViewById(R.id.likeButton)
        private val username: TextView = itemView.findViewById(R.id.username)
        private val content: TextView = itemView.findViewById(R.id.content)
        private val time: TextView = itemView.findViewById(R.id.time)
        private val reply: TextView = itemView.findViewById(R.id.reply)
        private val commentReplies: TextView = itemView.findViewById(R.id.commentReplies)
        private val hideCommentReplies: TextView = itemView.findViewById(R.id.hideCommentReplies)
        private val likesCount: TextView = itemView.findViewById(R.id.likesCount)
        private val likeCountContainer: LinearLayout = itemView.findViewById(R.id.likeCountContainer)
        private val viewRepliesContainer: LinearLayout = itemView.findViewById(R.id.viewRepliesContainer)
        private val repliesRecyclerView: RecyclerView = itemView.findViewById(R.id.repliesRecyclerView)

        private var isReplyCount = false

        private fun formatMongoTimestamp(dateTimeString: String?): String {
            if (dateTimeString.isNullOrBlank()) return "Unknown Time"
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val date = inputFormat.parse(dateTimeString)
                val now = Date()
                val diffInMillis = now.time - (date?.time ?: 0)
                val diffInSeconds = diffInMillis / 1000
                val diffInMinutes = diffInSeconds / 60
                val diffInHours = diffInMinutes / 60
                val diffInDays = diffInHours / 24
                val diffInWeeks = diffInDays / 7
                val diffInMonths = diffInDays / 30 // Approximate
                val diffInYears = diffInDays / 365 // Approximate

                when {
                    diffInSeconds < 60 -> "now"
                    diffInMinutes < 60 -> "${diffInMinutes}m"
                    diffInHours < 24 -> "${diffInHours}h"
                    diffInDays == 1L -> "1d"
                    diffInDays < 7 -> "${diffInDays}d"
                    diffInWeeks == 1L -> "1w"
                    diffInWeeks < 4 -> "${diffInWeeks}w"
                    diffInMonths == 1L -> "a month ago"
                    diffInMonths < 12 -> "${diffInMonths}months"
                    diffInYears == 1L -> "1y"
                    else -> "${diffInYears}years"
                }
            } catch (e: Exception) {
                Log.w("CommentViewHolder", "Failed to format timestamp: $dateTimeString", e)
                "now"
            }
        }

        @SuppressLint("SetTextI18n")
        fun render(data: Comment) {
            var replyCount = data.replyCount

            Glide.with(context)
                .load(data.author!!.account.avatar.url)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .placeholder(R.drawable.flash21)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView)

            try {
                for (commentReply in data.replies) {
                    commentReply.__v = absoluteAdapterPosition
                }

                replyCommentAdapter = if (data.isReplyPlaying) {
                    ReplyCommentAdapter(context, data, data.postId, mReplyPosition)
                } else {
                    ReplyCommentAdapter(context, data, data.postId, -1)
                }

                repliesRecyclerView.adapter = replyCommentAdapter
                replyCommentAdapter.setListener(onViewReplies)
                replyCommentAdapter.setPlayListener(object : ReplyCommentAdapter.OnPlayListener {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onPlay(position: Int) {
                        Log.d("onPlay", "onPlay: in main comments playing child comment on $position")
                        mPlayingPosition = -1
                        mParentComment = absoluteAdapterPosition
                        Log.d("onPlay", "onPlay: in main comments playing parent comment on $mParentComment")
                        mReplyPosition = position
                    }

                    override fun isPlaying(position: Int, isPlaying: Boolean, progress: Float) {
                        Log.d(TAG, "isPlaying: position $position is playing $isPlaying")
                        data.isReplyPlaying = isPlaying
                        data.replies[position].progress = progress
                        notifyItemChanged(absoluteAdapterPosition)
                    }

                    override fun refreshParent(position: Int) {
                        data.isReplyPlaying = true
                        notifyItemChanged(absoluteAdapterPosition)
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Handle replies visibility
            if (replyCount == 0) {
                viewRepliesContainer.visibility = View.GONE
            } else {
                viewRepliesContainer.visibility = View.VISIBLE

                if (!data.hasNextPage && data.isRepliesVisible) {
                    commentReplies.visibility = View.GONE
                } else {
                    commentReplies.visibility = View.VISIBLE
                    if (data.isRepliesVisible) {
                        if (data.replyCountVisible) {
                            commentReplies.text = if (replyCount == 1) "...View 1 reply" else "...View more replies"
                        } else {
                            commentReplies.visibility = View.GONE
                        }
                    } else {
                        commentReplies.text = if (replyCount == 1) "...View 1 reply" else "...View $replyCount replies"
                    }
                }

                if (data.isRepliesVisible) {
                    hideCommentReplies.visibility = View.VISIBLE
                    repliesRecyclerView.visibility = View.VISIBLE
                } else {
                    hideCommentReplies.visibility = View.GONE
                    repliesRecyclerView.visibility = View.GONE
                    commentReplies.visibility = View.VISIBLE
                }
            }

            hideCommentReplies.setOnClickListener {
                data.isRepliesVisible = false
                repliesRecyclerView.visibility = View.GONE
                hideCommentReplies.visibility = View.GONE
                commentReplies.visibility = View.VISIBLE
                commentReplies.text = if (replyCount == 1) "...View 1 reply" else "...View $replyCount replies"
            }

            commentReplies.setOnClickListener {
                repliesRecyclerView.visibility = View.VISIBLE
                data.isRepliesVisible = true
                if (data.isRepliesVisible) {
                    hideCommentReplies.visibility = View.VISIBLE
                } else {
                    hideCommentReplies.visibility = View.GONE
                }

                if (!data.hasNextPage) {
                    commentReplies.visibility = View.GONE
                }
                commentReplies.text = if (replyCount == 1) "...View 1 reply" else "...View more"

                onViewReplies.onViewRepliesClick(
                    data, absoluteAdapterPosition, commentReplies, hideCommentReplies,
                    repliesRecyclerView, data.isRepliesVisible, data.pageNumber
                )
            }

            username.text = data.author.account.username

            // Handle mentions in content
            val inputString = data.content
            val regex = Regex("@\\w+")
            val matches = regex.findAll(inputString)

            if (matches.none()) {
                content.text = inputString
            } else {
                val highlightColor: Int by lazy {
                    ContextCompat.getColor(context, R.color.bluejeans)
                }
                val spannableString = SpannableString(inputString)

                matches.forEach {
                    val start = it.range.first
                    val end = it.range.last + 1

                    spannableString.setSpan(
                        ForegroundColorSpan(highlightColor),
                        start, end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                content.text = spannableString
            }

            time.text = formatMongoTimestamp(data.createdAt)

            reply.setOnClickListener {
                EventBus.getDefault().post(ToggleReplyToTextView(data, absoluteAdapterPosition))
            }

            // Handle likes display
            when (data.likes) {
                0 -> {
                    likeCountContainer.visibility = View.GONE
                }
                1 -> {
                    likeCountContainer.visibility = View.VISIBLE
                    likesCount.text = "1"
                }
                else -> {
                    likeCountContainer.visibility = View.VISIBLE
                    likesCount.text = "${data.likes}"
                }
            }

            // Handle like button functionality
            // Update the likeButton text and color based on like status
            if (data.isLiked) {
                likeButton.text = "Like"
                likeButton.setTextColor(ContextCompat.getColor(context, R.color.bluejeans)) // or your preferred color
            } else {
                likeButton.text = "Like"
                likeButton.setTextColor(ContextCompat.getColor(context, R.color.dark_gray)) // default color
            }

            likeButton.setOnClickListener {
                data.isLiked = !data.isLiked
                onViewReplies.likeUnLikeComment(absoluteAdapterPosition, data)

                if (data.isLiked) {
                    likeButton.text = "Like"
                    likeButton.setTextColor(ContextCompat.getColor(context, R.color.bluejeans))
                    // You can add animation here if needed
                    YoYo.with(Techniques.Tada)
                        .duration(700)
                        .repeat(1)
                        .playOn(likeButton)
                } else {
                    likeButton.text = "Like"
                    likeButton.setTextColor(ContextCompat.getColor(context, R.color.dark_gray))
                }
            }
        }
    }

    inner class CommentImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val TAG = "Comment Image ViewHolder"

        private val imageView: ImageView = itemView.findViewById(R.id.profilePic)
        private val imageComment: ImageView = itemView.findViewById(R.id.imageComment)
        private val username: TextView = itemView.findViewById(R.id.username)
        private val captionContent: TextView = itemView.findViewById(R.id.content)
        private val time: TextView = itemView.findViewById(R.id.time)
        private val reply: TextView = itemView.findViewById(R.id.reply)
        private val commentReplies: TextView = itemView.findViewById(R.id.commentReplies)
        private val hideCommentReplies: TextView = itemView.findViewById(R.id.hideCommentReplies)
        private val likesCount: TextView = itemView.findViewById(R.id.likesCount)
        private val likeCountContainer: LinearLayout = itemView.findViewById(R.id.likeCountContainer)
        private val likeButton: TextView = itemView.findViewById(R.id.likeButton)
        private val repliesRecyclerView: RecyclerView = itemView.findViewById(R.id.repliesRecyclerView)

        private var isReplyCount = false

        private fun formatMongoTimestamp(dateTimeString: String?): String {
            if (dateTimeString.isNullOrBlank()) return "Unknown Time"
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val date = inputFormat.parse(dateTimeString)
                val now = Date()
                val diffInMillis = now.time - (date?.time ?: 0)
                val diffInSeconds = diffInMillis / 1000
                val diffInMinutes = diffInSeconds / 60
                val diffInHours = diffInMinutes / 60
                val diffInDays = diffInHours / 24
                val diffInWeeks = diffInDays / 7
                val diffInMonths = diffInDays / 30 // Approximate
                val diffInYears = diffInDays / 365 // Approximate

                when {
                    diffInSeconds < 60 -> "now"
                    diffInMinutes < 60 -> "${diffInMinutes}m"
                    diffInHours < 24 -> "${diffInHours}h"
                    diffInDays == 1L -> "1d"
                    diffInDays < 7 -> "${diffInDays}d"
                    diffInWeeks == 1L -> "1w"
                    diffInWeeks < 4 -> "${diffInWeeks}w"
                    diffInMonths == 1L -> "a month ago"
                    diffInMonths < 12 -> "${diffInMonths}months"
                    diffInYears == 1L -> "1y"
                    else -> "${diffInYears}years"
                }
            } catch (e: Exception) {
                Log.w("CommentViewHolder", "Failed to format timestamp: $dateTimeString", e)
                "now"
            }
        }

        @SuppressLint("SetTextI18n")
        fun render(data: Comment) {
            var imageUrl = ""
            Log.d(TAG, "data $data")
            val replyCount = data.replyCount

            // Load profile picture
            Glide.with(itemView.context)
                .load(data.author!!.account.avatar.url)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .placeholder(R.drawable.flash21)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView)

            // Load comment image if available
            if (data.images.isNotEmpty() && data.images[0].url.isNotEmpty()) {
                imageUrl = data.images[0].url
                Log.d(TAG, "Image url is not empty for holder")
                Glide.with(itemView.context)
                    .load(imageUrl)
                    .placeholder(R.drawable.flash21)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageComment)
                imageComment.visibility = View.VISIBLE
            } else {
                Log.d(TAG, "Image url is empty for holder")
                imageComment.visibility = View.GONE
            }

            // Handle mention text
            if (data.content.contains(Regex("@\\w+"))) {
                captionContent.text = data.content
                captionContent.visibility = View.VISIBLE
            } else {
                captionContent.visibility = View.GONE
            }

            // Set up replies RecyclerView
            try {
                for (commentReply in data.replies) {
                    commentReply.__v = absoluteAdapterPosition
                }
                Log.d("ImageCommentRenderData", "data.replies ${data.replies}")
                replyCommentAdapter = if (data.isReplyPlaying) {
                    ReplyCommentAdapter(itemView.context, data, data.postId, mReplyPosition)
                } else {
                    ReplyCommentAdapter(itemView.context, data, data.postId, -1)
                }
                repliesRecyclerView.adapter = replyCommentAdapter
                replyCommentAdapter.setListener(onViewReplies)
                replyCommentAdapter.setPlayListener(object : ReplyCommentAdapter.OnPlayListener {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onPlay(position: Int) {
                        changePlayingStatus()
                        Log.d("onPlay", "onPlay: in main comments playing child comment on $position")
                        mPlayingPosition = -1
                        mParentComment = absoluteAdapterPosition
                        Log.d("onPlay", "onPlay: in main comments playing parent comment on $mParentComment")
                        mReplyPosition = position
                    }

                    override fun isPlaying(position: Int, isPlaying: Boolean, progress: Float) {
                        Log.d(TAG, "isPlaying: position $position is playing $isPlaying")
                        data.isReplyPlaying = isPlaying
                        data.replies[position].progress = progress
                        notifyItemChanged(absoluteAdapterPosition)
                    }

                    override fun refreshParent(position: Int) {
                        data.isReplyPlaying = true
                        notifyItemChanged(absoluteAdapterPosition)
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Handle reply visibility and text
            if (replyCount == 0) {
                commentReplies.visibility = View.GONE
                hideCommentReplies.visibility = View.GONE
                repliesRecyclerView.visibility = View.GONE
            } else {
                if (!data.hasNextPage && data.isRepliesVisible) {
                    commentReplies.visibility = View.GONE
                } else {
                    commentReplies.visibility = View.VISIBLE
                    commentReplies.text = if (data.isRepliesVisible) {
                        if (data.replyCountVisible) {
                            if (replyCount == 1) "...View 1 reply" else "...View more replies"
                        } else {
                            commentReplies.visibility = View.GONE
                            ""
                        }
                    } else {
                        if (replyCount == 1) "...View 1 reply" else "...View $replyCount replies"
                    }
                }

                if (data.isRepliesVisible) {
                    hideCommentReplies.visibility = View.VISIBLE
                    repliesRecyclerView.visibility = View.VISIBLE
                } else {
                    hideCommentReplies.visibility = View.GONE
                    repliesRecyclerView.visibility = View.GONE
                    commentReplies.visibility = View.VISIBLE
                }
            }

            // Handle hide replies click
            hideCommentReplies.setOnClickListener {
                data.isRepliesVisible = false
                repliesRecyclerView.visibility = View.GONE
                hideCommentReplies.visibility = View.GONE
                commentReplies.visibility = View.VISIBLE
                commentReplies.text = if (replyCount == 1) "...View 1 reply" else "...View $replyCount replies"
            }

            // Handle view replies click
            commentReplies.setOnClickListener {
                repliesRecyclerView.visibility = View.VISIBLE
                data.isRepliesVisible = true
                hideCommentReplies.visibility = View.VISIBLE
                if (!data.hasNextPage) {
                    commentReplies.visibility = View.GONE
                }
                commentReplies.text = if (replyCount == 1) "...View 1 reply" else "...View more"
                onViewReplies.onViewRepliesClick(
                    data, absoluteAdapterPosition, commentReplies, hideCommentReplies,
                    repliesRecyclerView, data.isRepliesVisible, data.pageNumber
                )
            }

            // Set username and time
            username.text = data.author.account.username
            time.text = formatMongoTimestamp(data.createdAt)

            // Handle image click
            imageComment.setOnClickListener {
                val intent = Intent(itemView.context, ViewImagesActivity::class.java)
                intent.putExtra("imageUrl", imageUrl)
                intent.putExtra("owner", data.author.account.username)
                intent.putExtra("displayLikeButton", true)
                intent.putExtra("position", absoluteAdapterPosition)
                intent.putExtra("data", data)
                (itemView.context as Activity).startActivityForResult(intent, R_CODE)
            }

            // Handle reply click
            reply.setOnClickListener {
                EventBus.getDefault().post(ToggleReplyToTextView(data, absoluteAdapterPosition))
            }

            when (data.likes) {
                0 -> likeCountContainer.visibility = View.GONE
                1 -> {
                    likeCountContainer.visibility = View.VISIBLE
                    likesCount.text = "1"
                }
                else -> {
                    likeCountContainer.visibility = View.VISIBLE
                    likesCount.text = "${data.likes}"
                }
            }

            // Like button setup
            if (data.isLiked) {
                likeButton.text = "Like"
                likeButton.setTextColor(ContextCompat.getColor(itemView.context, R.color.bluejeans))
            } else {
                likeButton.text = "Like"
                likeButton.setTextColor(ContextCompat.getColor(itemView.context, R.color.dark_gray))
            }

            likeButton.setOnClickListener {
                data.isLiked = !data.isLiked
                onViewReplies.likeUnLikeComment(absoluteAdapterPosition, data)
                if (data.isLiked) {
                    likeButton.text = "Like"
                    likeButton.setTextColor(ContextCompat.getColor(itemView.context, R.color.bluejeans))
                    YoYo.with(Techniques.Tada)
                        .duration(700)
                        .repeat(1)
                        .playOn(likeButton)
                } else {
                    likeButton.text = "Like"
                    likeButton.setTextColor(ContextCompat.getColor(itemView.context, R.color.dark_gray))
                }
            }

            // Reply button
            reply.setOnClickListener {
                EventBus.getDefault().post(ToggleReplyToTextView(data, absoluteAdapterPosition))
            }

            // Replies visibility and click listeners
            if (replyCount == 0) {
                commentReplies.visibility = View.GONE
                hideCommentReplies.visibility = View.GONE
                repliesRecyclerView.visibility = View.GONE
            } else {
                if (!data.hasNextPage && data.isRepliesVisible) {
                    commentReplies.visibility = View.GONE
                } else {
                    commentReplies.visibility = View.VISIBLE
                    if (data.isRepliesVisible && !data.replyCountVisible) {
                        commentReplies.visibility = View.GONE
                    } else {
                        commentReplies.text = if (data.isRepliesVisible) {
                            if (replyCount == 1) "...View 1 reply" else "...View more replies"
                        } else {
                            if (replyCount == 1) "...View 1 reply" else "...View $replyCount replies"
                        }
                    }
                }

                if (data.isRepliesVisible) {
                    hideCommentReplies.visibility = View.VISIBLE
                    repliesRecyclerView.visibility = View.VISIBLE
                } else {
                    hideCommentReplies.visibility = View.GONE
                    repliesRecyclerView.visibility = View.GONE
                    commentReplies.visibility = View.VISIBLE
                }
            }

            hideCommentReplies.setOnClickListener {
                data.isRepliesVisible = false
                repliesRecyclerView.visibility = View.GONE
                hideCommentReplies.visibility = View.GONE
                commentReplies.visibility = View.VISIBLE
                commentReplies.text = if (replyCount == 1) "...View 1 reply" else "...View $replyCount replies"
            }

            commentReplies.setOnClickListener {
                repliesRecyclerView.visibility = View.VISIBLE
                data.isRepliesVisible = true
                hideCommentReplies.visibility = View.VISIBLE
                if (!data.hasNextPage) {
                    commentReplies.visibility = View.GONE
                }
                commentReplies.text = if (replyCount == 1) "...View 1 reply" else "...View more"
                onViewReplies.onViewRepliesClick(
                    data, absoluteAdapterPosition, commentReplies, hideCommentReplies,
                    repliesRecyclerView, data.isRepliesVisible, data.pageNumber
                )

            }
        }
    }

    inner class CommentAudioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val TAG = "Comment Audio ViewHolder"

        private val imageView: ImageView = itemView.findViewById(R.id.profilePic)
        // Use likeButton TextView instead of likeUnLikeCommentImageView
        private val likeButton: TextView = itemView.findViewById(R.id.likeButton)
        private val captionContent: TextView = itemView.findViewById(R.id.content)
        private val likeCountContainer: LinearLayout = itemView.findViewById(R.id.likeCountContainer)
        private val username: TextView = itemView.findViewById(R.id.username)
        private val time: TextView = itemView.findViewById(R.id.time)
        private val reply: TextView = itemView.findViewById(R.id.reply)
        private val commentReplies: TextView = itemView.findViewById(R.id.commentReplies)
        private val hideCommentReplies: TextView = itemView.findViewById(R.id.hideCommentReplies)
        private val likesCount: TextView = itemView.findViewById(R.id.likesCount)
        private val audioDurationTextView: TextView = itemView.findViewById(R.id.audioDurationTV)
        private val secondAudioDurationTextView: TextView = itemView.findViewById(R.id.secondAudioDurationTVCount)
        private val audioDurationTVCount: TextView = itemView.findViewById(R.id.audioDurationTVCount)
        private val audioWave: WaveformSeekBar = itemView.findViewById(R.id.wave)
        private val secondAudioWave: WaveformSeekBar = itemView.findViewById(R.id.secondWave)
        private val commentAudioSeekBar: SeekBar = itemView.findViewById(R.id.commentAudioSeekBar)
        private val secondCommentAudioSeekBar: SeekBar = itemView.findViewById(R.id.secondCommentAudioSeekBar)
        val repliesRecyclerView: RecyclerView = itemView.findViewById(R.id.repliesRecyclerView)
        private val playVnAudioBtn: ImageView = itemView.findViewById(R.id.playVnAudioBtn)

        private var isReplyCount = false

        init {
            // Add null checks for critical views to prevent crashes
            checkNotNull(likeButton) { "likeButton view not found in layout" }
            checkNotNull(likeCountContainer) { "likeCountContainer view not found in layout" }
        }

        private fun formatMongoTimestamp(dateTimeString: String?): String {
            if (dateTimeString.isNullOrBlank()) return "now"
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val date = inputFormat.parse(dateTimeString)
                val now = Date()
                val diffInMillis = now.time - (date?.time ?: 0)
                val diffInSeconds = diffInMillis / 1000
                val diffInMinutes = diffInSeconds / 60
                val diffInHours = diffInMinutes / 60
                val diffInDays = diffInHours / 24
                val diffInWeeks = diffInDays / 7
                val diffInMonths = diffInDays / 30 // Approximate
                val diffInYears = diffInDays / 365 // Approximate

                when {
                    diffInSeconds < 60 -> "now"
                    diffInMinutes < 60 -> "${diffInMinutes}m"
                    diffInHours < 24 -> "${diffInHours}h"
                    diffInDays == 1L -> "1d"
                    diffInDays < 7 -> "${diffInDays}d"
                    diffInWeeks == 1L -> "1w"
                    diffInWeeks < 4 -> "${diffInWeeks}w"
                    diffInMonths == 1L -> "a month ago"
                    diffInMonths < 12 -> "${diffInMonths}months"
                    diffInYears == 1L -> "1y"
                    else -> "${diffInYears}years"
                }
            } catch (e: Exception) {
                Log.w("CommentViewHolder", "Failed to format timestamp: $dateTimeString", e)
                "now"
            }
        }

        @SuppressLint("SetTextI18n", "NotifyDataSetChanged", "DefaultLocale")
        fun render(data: Comment) {
            var audioUrl = ""

            Log.d("DataIsPlaying", "Data is playing ${data.isPlaying}")
            Log.d("CommentAudioViewHolder", "audio type: ${data.fileType}")
            Log.d("CommentAudioViewHolder", "audio duration: ${data.duration}")
            Log.d("CommentAudioViewHolder", "audio progress: ${data.progress}")

            // Handle mention text
            if (data.content.contains(Regex("@\\w+"))) {
                captionContent.text = data.content
                captionContent.visibility = View.VISIBLE
            } else {
                captionContent.visibility = View.GONE
            }

            // WhatsApp-like audio behavior: Show different UI based on playing state
            setupAudioUI(data)

            if (data.audios.isNotEmpty()) {
                try {
                    audioUrl = data.audios[0].url
                    audioDurationTextView.text = data.duration

                    val audioDurationMs = reverseFormattedDuration(data.duration) // Returns Long (milliseconds)
                    val audioDuration = audioDurationMs.toFloat() // Convert to Float for WaveformSeekBar/SeekBar
                    Log.d("audioDuration", "reverse duration $audioDurationMs")

                    when (data.fileType) {
                        "vnAudio" -> setupVoiceNoteWaveform(data, audioUrl, audioDuration)
                        else -> setupRegularAudioSeekBar(data, audioUrl, audioDuration)
                    }
                } catch (e: IllegalArgumentException) {
                    Log.e("audioDuration", "Failed to process audio duration: ${e.message}")
                    audioDurationTextView.text = "Invalid duration"
                }
            }

            // Set up play button click listener
            setupPlayButtonListener(data, audioUrl)

            // Rest of the UI setup
            setupUserInfo(data)
            setupReplies(data)
            setupLikeButton(data)
        }

        private fun setupVoiceNoteWaveform(data: Comment, audioUrl: String, audioDuration: Float) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    WaveFormExtractor.getSampleFrom(itemView.context, audioUrl) { waveformData ->
                        CoroutineScope(Dispatchers.Main).launch {
                            // Set up both waveforms with same data
                            audioWave.maxProgress = audioDuration
                            audioWave.setSampleFrom(waveformData)
                            secondAudioWave.setSampleFrom(waveformData)
                            secondAudioWave.maxProgress = audioDuration

                            if (data.isPlaying) {
                                audioWave.progress = data.progress
                                audioDurationTVCount.text = TrimVideoUtils.stringForTime(data.progress)
                            } else {
                                // Reset progress when not playing
                                secondAudioWave.progress = 0F
                                secondAudioDurationTextView.text = data.duration
                            }

                            // Set up playing waveform listener
                            audioWave.onProgressChanged = object : SeekBarOnProgressChanged {
                                override fun onProgressChanged(
                                    waveformSeekBar: WaveformSeekBar,
                                    progress: Float,
                                    fromUser: Boolean
                                ) {
                                    Log.d("MotionEvent", "(audioWave)->onProgressChanged:: progress $progress")

                                    // Always show current time during playback (WhatsApp style)
                                    audioDurationTVCount.text = TrimVideoUtils.stringForTime(progress)
                                    data.progress = progress

                                    if (fromUser) {
                                        // User is scrubbing - pause and update position
                                        onViewReplies.toggleAudioPlayer(
                                            playVnAudioBtn,
                                            audioUrl,
                                            absoluteAdapterPosition,
                                            false,
                                            progress,
                                            true,
                                            seekTo = false,
                                            isVnAudio = true
                                        )
                                        data.isPlaying = false
                                        Log.d("FromUser", "User scrubbing: progress ${data.progress}, playing ${data.isPlaying}")
                                    }
                                }

                                override fun onRelease(event: MotionEvent?, progress: Float) {
                                    Log.d("MotionEvent", "(audioWave)->onRelease:: user stopped seeking $progress")
                                    // Seek to new position
                                    onViewReplies.toggleAudioPlayer(
                                        playVnAudioBtn,
                                        audioUrl,
                                        absoluteAdapterPosition,
                                        false,
                                        progress,
                                        isSeeking = false,
                                        seekTo = true,
                                        isVnAudio = true
                                    )
                                }
                            }

                            // Set up static waveform listener (for preview when not playing)
                            secondAudioWave.onProgressChanged = object : SeekBarOnProgressChanged {
                                override fun onProgressChanged(
                                    waveformSeekBar: WaveformSeekBar,
                                    progress: Float,
                                    fromUser: Boolean
                                ) {
                                    if (fromUser && !data.isPlaying) {
                                        // Show preview time when scrubbing static waveform
                                        secondAudioDurationTextView.text = TrimVideoUtils.stringForTime(progress)
                                    }
                                }

                                override fun onRelease(event: MotionEvent?, progress: Float) {
                                    if (!data.isPlaying) {
                                        // Return to total duration after scrubbing (WhatsApp behavior)
                                        secondAudioDurationTextView.text = data.duration
                                        secondAudioWave.progress = 0F
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Waveform Extraction error: ${e.message}")
                    e.printStackTrace()
                }
            }
        }

        private fun setupRegularAudioSeekBar(data: Comment, audioUrl: String, audioDuration: Float) {
            val maxDuration = audioDuration.toInt() // Convert to Int for SeekBar max

            // Set up playing seekbar
            commentAudioSeekBar.max = maxDuration
            commentAudioSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    Log.d("OnSeekBarChangeListener", "Progress $progress, fromUser: $fromUser")

                    // Always show current time during playback
                    audioDurationTVCount.text = TrimVideoUtils.stringForTime(progress.toFloat())
                    data.progress = progress.toFloat()

                    if (fromUser) {
                        // User is seeking
                        onViewReplies.toggleAudioPlayer(
                            playVnAudioBtn,
                            audioUrl,
                            absoluteAdapterPosition,
                            false,
                            progress.toFloat(),
                            isSeeking = false,
                            seekTo = true,
                            isVnAudio = false
                        )
                        Log.d("OnSeekBarChangeListener", "User seeking: progress ${data.progress}")
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

            // Set up static seekbar (when not playing)
            secondCommentAudioSeekBar.max = maxDuration
            secondCommentAudioSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser && !data.isPlaying) {
                        // Show preview time when scrubbing
                        secondAudioDurationTextView.text = TrimVideoUtils.stringForTime(progress.toFloat())
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    if (!data.isPlaying) {
                        // Return to total duration after scrubbing (WhatsApp behavior)
                        secondAudioDurationTextView.text = data.duration
                        secondCommentAudioSeekBar.progress = 0
                    }
                }
            })
        }

        private fun setupAudioUI(data: Comment) {
            if (data.isPlaying) {
                // PLAYING STATE - WhatsApp style
                playVnAudioBtn.setImageResource(R.drawable.baseline_pause_black)
                currentComment = data

                if (data.fileType == "vnAudio") {
                    // Show animated waveform with current time
                    commentAudioSeekBar.visibility = View.GONE
                    secondCommentAudioSeekBar.visibility = View.GONE
                    audioWave.visibility = View.VISIBLE
                    secondAudioWave.visibility = View.GONE
                    audioDurationTVCount.visibility = View.VISIBLE
                    secondAudioDurationTextView.visibility = View.GONE

                    // Show current playback time (WhatsApp behavior)
                    audioDurationTVCount.text = TrimVideoUtils.stringForTime(data.progress)

                    // Set global references
                    mCurrentWaveForm = audioWave
                } else {
                    // Regular audio playing
                    audioWave.visibility = View.GONE
                    secondAudioWave.visibility = View.GONE
                    commentAudioSeekBar.visibility = View.VISIBLE
                    secondCommentAudioSeekBar.visibility = View.GONE
                    audioDurationTVCount.visibility = View.VISIBLE
                    secondAudioDurationTextView.visibility = View.GONE

                    // Show current playback time
                    audioDurationTVCount.text = TrimVideoUtils.stringForTime(data.progress)

                    // Set global references
                    mCurrentSeekBar = commentAudioSeekBar
                }
            } else {
                // NOT PLAYING STATE - WhatsApp style
                playVnAudioBtn.setImageResource(R.drawable.play_svgrepo_com)

                if (data.fileType == "vnAudio") {
                    // Show static waveform with total duration
                    commentAudioSeekBar.visibility = View.GONE
                    secondCommentAudioSeekBar.visibility = View.GONE
                    audioWave.visibility = View.GONE
                    audioDurationTVCount.visibility = View.GONE
                    secondAudioWave.visibility = View.VISIBLE
                    secondAudioDurationTextView.visibility = View.VISIBLE

                    // Show total duration (WhatsApp behavior)
                    secondAudioDurationTextView.text = data.duration
                } else {
                    // Regular audio not playing
                    audioWave.visibility = View.GONE
                    secondAudioWave.visibility = View.GONE
                    commentAudioSeekBar.visibility = View.GONE
                    audioDurationTVCount.visibility = View.GONE
                    secondCommentAudioSeekBar.visibility = View.VISIBLE
                    secondAudioDurationTextView.visibility = View.VISIBLE

                    // Show total duration
                    secondAudioDurationTextView.text = data.duration
                }
            }
        }

        private fun setupPlayButtonListener(data: Comment, audioUrl: String) {
            playVnAudioBtn.setOnClickListener {
                val wasPlaying = data.isPlaying
                Log.d("MainCommentPlayClick", "wasPlaying: $wasPlaying, replyPlaying: ${data.isReplyPlaying}")

                if (!wasPlaying) {
                    changePlayingStatus()
                }

                // Toggle playing state
                data.isPlaying = !wasPlaying

                // Update UI based on new state (WhatsApp behavior)
                updateUIForPlayingState(data)

                // Get progress from appropriate source
                val currentProgress = if (data.fileType == "vnAudio") {
                    if (wasPlaying) data.progress else secondAudioWave.progress
                } else {
                    if (wasPlaying) data.progress else secondCommentAudioSeekBar.progress.toFloat()
                }

                // Call audio player
                onViewReplies.toggleAudioPlayer(
                    playVnAudioBtn,
                    audioUrl,
                    absoluteAdapterPosition,
                    false,
                    currentProgress,
                    isSeeking = false,
                    seekTo = false,
                    isVnAudio = (data.fileType == "vnAudio")
                )

                // Update adapter state
                mReplyPosition = -1
                mPlayingPosition = if (mPlayingPosition == absoluteAdapterPosition) -1 else absoluteAdapterPosition
                notifyItemChanged(absoluteAdapterPosition)

                // Post events
                if (data.fileType == "vnAudio") {
                    mCurrentWaveForm = audioWave
                    EventBus.getDefault().post(
                        AudioPlayerHandler(
                            audioUrl,
                            audioWave,
                            audioDurationTVCount,
                            currentProgress,
                            absoluteAdapterPosition
                        )
                    )
                } else {
                    val audioMaxDuration = reverseFormattedDuration(data.duration)
                    mCurrentSeekBar = commentAudioSeekBar
                    EventBus.getDefault().post(
                        CommentAudioPlayerHandler(
                            audioUrl,
                            commentAudioSeekBar,
                            audioDurationTVCount,
                            currentProgress,
                            absoluteAdapterPosition,
                            audioMaxDuration
                        )
                    )
                }
            }
        }

        private fun updateUIForPlayingState(data: Comment) {
            if (data.isPlaying) {
                // Just started playing - switch to animated view
                playVnAudioBtn.setImageResource(R.drawable.baseline_pause_black)

                if (data.fileType == "vnAudio") {
                    // Switch from static to animated waveform
                    secondAudioWave.visibility = View.GONE
                    secondAudioDurationTextView.visibility = View.GONE
                    audioWave.visibility = View.VISIBLE
                    audioDurationTVCount.visibility = View.VISIBLE
                    audioDurationTVCount.text = TrimVideoUtils.stringForTime(data.progress)
                } else {
                    // Switch to playing seekbar
                    secondCommentAudioSeekBar.visibility = View.GONE
                    secondAudioDurationTextView.visibility = View.GONE
                    commentAudioSeekBar.visibility = View.VISIBLE
                    audioDurationTVCount.visibility = View.VISIBLE
                    audioDurationTVCount.text = TrimVideoUtils.stringForTime(data.progress)
                }
            } else {
                // Just stopped playing - switch to static view
                playVnAudioBtn.setImageResource(R.drawable.play_svgrepo_com)

                if (data.fileType == "vnAudio") {
                    // Switch from animated to static waveform
                    audioWave.visibility = View.GONE
                    audioDurationTVCount.visibility = View.GONE
                    secondAudioWave.visibility = View.VISIBLE
                    secondAudioDurationTextView.visibility = View.VISIBLE
                    secondAudioDurationTextView.text = data.duration // Show total duration
                    secondAudioWave.progress = 0F // Reset to beginning
                } else {
                    // Switch to static seekbar
                    commentAudioSeekBar.visibility = View.GONE
                    audioDurationTVCount.visibility = View.GONE
                    secondCommentAudioSeekBar.visibility = View.VISIBLE
                    secondAudioDurationTextView.visibility = View.VISIBLE
                    secondAudioDurationTextView.text = data.duration // Show total duration
                    secondCommentAudioSeekBar.progress = 0 // Reset to beginning
                }
            }
        }

        private fun setupUserInfo(data: Comment) {
            username.text = data.author?.account?.username ?: "Unknown"
            time.text = formatMongoTimestamp(data.createdAt)

            Glide.with(itemView.context)
                .load(data.author?.account?.avatar?.url)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .placeholder(R.drawable.flash21)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView)
        }

        private fun setupReplies(data: Comment) {
            val replyCount = data.replyCount

            try {
                for (commentReply in data.replies) {
                    commentReply.__v = absoluteAdapterPosition
                }

                if (mParentComment == absoluteAdapterPosition) {
                    replyCommentAdapter = ReplyCommentAdapter(context, data, data.postId, mReplyPosition)
                } else {
                    if (mParentComment != -1) {
                        val handler = Handler()
                        handler.postDelayed({
                            refreshMainComment(mParentComment)
                        }, 500)
                    }
                    replyCommentAdapter = ReplyCommentAdapter(context, data, data.postId, -1)
                }

                repliesRecyclerView.adapter = replyCommentAdapter
                repliesRecyclerView.itemAnimator = null
                replyCommentAdapter.setListener(onViewReplies)
                replyCommentAdapter.setPlayListener(object : ReplyCommentAdapter.OnPlayListener {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onPlay(position: Int) {
                        changePlayingStatus()
                        Log.d("onPlay", "onPlay: in main comments playing child comment on $position")
                        mPlayingPosition = -1
                        mParentComment = absoluteAdapterPosition
                        Log.d("onPlay", "onPlay: in main comments playing parent comment on $mParentComment")
                        mReplyPosition = position
                    }

                    override fun isPlaying(position: Int, isPlaying: Boolean, progress: Float) {
                        Log.d("isPlaying", "isPlaying: position $position is playing $isPlaying progress $progress")
                        val comment = getItem(absoluteAdapterPosition)
                        if (position != -1) {
                            comment.replies[position].progress = progress
                        }
                        comment.isReplyPlaying = isPlaying
                        setItem(absoluteAdapterPosition, comment)
                        notifyItemChanged(absoluteAdapterPosition)
                    }

                    override fun refreshParent(position: Int) {
                        data.isReplyPlaying = true
                        notifyItemChanged(absoluteAdapterPosition)
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Reply visibility logic
            if (replyCount == 0) {
                commentReplies.visibility = View.GONE
                hideCommentReplies.visibility = View.GONE
                repliesRecyclerView.visibility = View.GONE
            } else {
                if (!data.hasNextPage && data.isRepliesVisible) {
                    commentReplies.visibility = View.GONE
                } else {
                    commentReplies.visibility = View.VISIBLE
                    if (data.isRepliesVisible && !data.replyCountVisible) {
                        commentReplies.visibility = View.GONE
                    } else {
                        commentReplies.text = if (data.isRepliesVisible) {
                            if (replyCount == 1) "...View 1 reply" else "...View more replies"
                        } else {
                            if (replyCount == 1) "...View 1 reply" else "...View $replyCount replies"
                        }
                    }
                }

                if (data.isRepliesVisible) {
                    hideCommentReplies.visibility = View.VISIBLE
                    repliesRecyclerView.visibility = View.VISIBLE
                } else {
                    hideCommentReplies.visibility = View.GONE
                    repliesRecyclerView.visibility = View.GONE
                    commentReplies.visibility = View.VISIBLE
                }
            }

            // Click listeners
            hideCommentReplies.setOnClickListener {
                data.isRepliesVisible = false
                repliesRecyclerView.visibility = View.GONE
                hideCommentReplies.visibility = View.GONE
                commentReplies.visibility = View.VISIBLE
                commentReplies.text = if (replyCount == 1) "...View 1 reply" else "...View $replyCount replies"
            }

            commentReplies.setOnClickListener {
                repliesRecyclerView.visibility = View.VISIBLE
                data.isRepliesVisible = true
                hideCommentReplies.visibility = View.VISIBLE
                if (!data.hasNextPage) {
                    commentReplies.visibility = View.GONE
                }
                commentReplies.text = if (replyCount == 1) "...View 1 reply" else "...View more"
                onViewReplies.onViewRepliesClick(
                    data, absoluteAdapterPosition, commentReplies, hideCommentReplies,
                    repliesRecyclerView, data.isRepliesVisible, data.pageNumber
                )
            }

            reply.setOnClickListener {
                EventBus.getDefault().post(ToggleReplyToTextView(data, absoluteAdapterPosition))
            }
        }

        private fun setupLikeButton(data: Comment) {
            // Like count visibility
            when (data.likes) {
                0 -> {
                    likeCountContainer.visibility = View.GONE
                    likesCount.visibility = View.GONE
                }
                1 -> {
                    likeCountContainer.visibility = View.VISIBLE
                    likesCount.visibility = View.VISIBLE
                    likesCount.text = "1"
                }
                else -> {
                    likeCountContainer.visibility = View.VISIBLE
                    likesCount.visibility = View.VISIBLE
                    likesCount.text = "${data.likes}"
                }
            }

            // Like button setup
            if (data.isLiked) {
                likeButton.text = "Like"
                likeButton.setTextColor(ContextCompat.getColor(itemView.context, R.color.bluejeans))
            } else {
                likeButton.text = "Like"
                likeButton.setTextColor(ContextCompat.getColor(itemView.context, R.color.dark_gray))
            }

            likeButton.setOnClickListener {
                data.isLiked = !data.isLiked
                onViewReplies.likeUnLikeComment(absoluteAdapterPosition, data)
                if (data.isLiked) {
                    likeButton.text = "Like"
                    likeButton.setTextColor(ContextCompat.getColor(itemView.context, R.color.bluejeans))
                    YoYo.with(Techniques.Tada)
                        .duration(700)
                        .repeat(1)
                        .playOn(likeButton)
                } else {
                    likeButton.text = "Like"
                    likeButton.setTextColor(ContextCompat.getColor(itemView.context, R.color.dark_gray))
                }
            }
        }


    }

    inner class CommentVideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val TAG = "Comment Video ViewHolder"

        private val captionContent: TextView = itemView.findViewById(R.id.content)
        private val imageView: ImageView = itemView.findViewById(R.id.profilePic)
        private val videoThumbnail: ImageView = itemView.findViewById(R.id.videoThumbnail)
        private val likeButton: TextView = itemView.findViewById(R.id.likeButton) // Updated to use likeButton
        private val likeCountContainer: LinearLayout = itemView.findViewById(R.id.likeCountContainer) // Updated to use likeCountContainer
        private val username: TextView = itemView.findViewById(R.id.username)
        private val time: TextView = itemView.findViewById(R.id.time)
        private val reply: TextView = itemView.findViewById(R.id.reply)
        private val commentReplies: TextView = itemView.findViewById(R.id.commentReplies)
        private val hideCommentReplies: TextView = itemView.findViewById(R.id.hideCommentReplies)
        private val likesCount: TextView = itemView.findViewById(R.id.likesCount)
        private val commentVideoDurationTextView: TextView = itemView.findViewById(R.id.commentVideoDurationTextView)
        private val repliesRecyclerView: RecyclerView = itemView.findViewById(R.id.repliesRecyclerView)
        private var isReplyCount = false

        private fun formatMongoTimestamp(dateTimeString: String?): String {
            if (dateTimeString.isNullOrBlank()) return "now"
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val date = inputFormat.parse(dateTimeString)
                val now = Date()
                val diffInMillis = now.time - (date?.time ?: 0)
                val diffInSeconds = diffInMillis / 1000
                val diffInMinutes = diffInSeconds / 60
                val diffInHours = diffInMinutes / 60
                val diffInDays = diffInHours / 24
                val diffInWeeks = diffInDays / 7
                val diffInMonths = diffInDays / 30 // Approximate
                val diffInYears = diffInDays / 365 // Approximate

                when {
                    diffInSeconds < 60 -> "now"
                    diffInMinutes < 60 -> "${diffInMinutes}m"
                    diffInHours < 24 -> "${diffInHours}h"
                    diffInDays == 1L -> "1d"
                    diffInDays < 7 -> "${diffInDays}d"
                    diffInWeeks == 1L -> "1w"
                    diffInWeeks < 4 -> "${diffInWeeks}w"
                    diffInMonths == 1L -> "a month ago"
                    diffInMonths < 12 -> "${diffInMonths}months"
                    diffInYears == 1L -> "1y"
                    else -> "${diffInYears}years"
                }
            } catch (e: Exception) {
                Log.w("CommentViewHolder", "Failed to format timestamp: $dateTimeString", e)
                "now"
            }
        }

        @SuppressLint("SetTextI18n")
        fun render(data: Comment) {
            var videourl = ""
            var thumbnail = ""
            Log.d(TAG, "data $data")
            val replyCount = data.replyCount
            commentVideoDurationTextView.text = data.duration


            // Handle mention text
            if (data.content.contains(Regex("@\\w+"))) {
                captionContent.text = data.content
                captionContent.visibility = View.VISIBLE
            } else {
                captionContent.visibility = View.GONE
            }

            Glide.with(context)
                .load(data.author!!.account.avatar.url)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .placeholder(R.drawable.flash21)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView)

            if (data.thumbnail.size > 0) {
                videourl = data.videos[0].url
                thumbnail = data.thumbnail[0].url
                if (thumbnail.isNotEmpty()) {
                    Log.d(TAG, "videos url is not empty for holder")
                    Glide.with(context)
                        .load(thumbnail)
                        .placeholder(R.drawable.flash21)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(videoThumbnail)
                } else {
                    Log.d(TAG, "videos url is empty for holder")
                }
            } else {
                Log.d("MainVideoComment", "No thumbnail available")
            }

            if (data.videos.size > 0) {
                videourl = data.videos[0].url
                if (videourl.isNotEmpty()) {
                    Log.d(TAG, "videos url is not empty for holder")
                    if (data.thumbnail.isEmpty()) {
                        Log.d("MainVideoComment", "thumbnail is empty")
                        Glide.with(context)
                            .load(data.videos[0].url)
                            .placeholder(R.drawable.flash21)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(videoThumbnail)
                    } else {
                        Log.d("MainVideoComment", "thumbnail is available")
                    }
                } else {
                    Log.d(TAG, "videos url is empty for holder")
                }
            } else {
                Log.d("MainVideoComment", "No videos available")
            }

            try {
                for (commentReply in data.replies) {
                    commentReply.__v = absoluteAdapterPosition
                }

                Log.d("CommentVideoViewHolder", "data.replies ${data.replies}")
                if (data.isReplyPlaying) {
                    replyCommentAdapter = ReplyCommentAdapter(context, data, data.postId, mReplyPosition)
                } else {
                    replyCommentAdapter = ReplyCommentAdapter(context, data, data.postId, -1)
                }

                repliesRecyclerView.adapter = replyCommentAdapter
                replyCommentAdapter.setListener(onViewReplies)
                replyCommentAdapter.setPlayListener(object : ReplyCommentAdapter.OnPlayListener {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onPlay(position: Int) {
                        Log.d("onPlay", "onPlay: in main comments playing child comment on $position")
                        mPlayingPosition = -1
                        mParentComment = absoluteAdapterPosition
                        Log.d("onPlay", "onPlay: in main comments playing parent comment on $mParentComment")
                        mReplyPosition = position
                    }

                    override fun isPlaying(position: Int, isPlaying: Boolean, progress: Float) {
                        Log.d(TAG, "isPlaying: position $position is playing $isPlaying")
                        data.isReplyPlaying = isPlaying
                        data.replies[position].progress = progress
                        notifyItemChanged(absoluteAdapterPosition)
                    }

                    override fun refreshParent(position: Int) {
                        data.isReplyPlaying = true
                        notifyItemChanged(absoluteAdapterPosition)
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (replyCount == 0) {
                commentReplies.visibility = View.GONE
                hideCommentReplies.visibility = View.GONE
            } else {
                if (!data.hasNextPage && data.isRepliesVisible) {
                    commentReplies.visibility = View.GONE
                } else {
                    commentReplies.visibility = View.VISIBLE
                    if (data.isRepliesVisible) {
                        if (data.replyCountVisible) {
                            commentReplies.text = if (replyCount == 1) "...View 1 reply" else "...View more replies"
                        } else {
                            commentReplies.visibility = View.GONE
                        }
                    } else {
                        commentReplies.text = if (replyCount == 1) "...View 1 reply" else "...View $replyCount replies"
                    }
                }

                if (data.isRepliesVisible) {
                    hideCommentReplies.visibility = View.VISIBLE
                    repliesRecyclerView.visibility = View.VISIBLE
                } else {
                    hideCommentReplies.visibility = View.GONE
                    repliesRecyclerView.visibility = View.GONE
                    commentReplies.visibility = View.VISIBLE
                }
            }

            hideCommentReplies.setOnClickListener {
                data.isRepliesVisible = false
                repliesRecyclerView.visibility = View.GONE
                hideCommentReplies.visibility = View.GONE
                commentReplies.visibility = View.VISIBLE
                commentReplies.text = if (replyCount == 1) "...View 1 reply" else "...View $replyCount replies"
            }

            commentReplies.setOnClickListener {
                repliesRecyclerView.visibility = View.VISIBLE
                data.isRepliesVisible = true
                if (data.isRepliesVisible) {
                    hideCommentReplies.visibility = View.VISIBLE
                } else {
                    hideCommentReplies.visibility = View.GONE
                }

                if (!data.hasNextPage) {
                    commentReplies.visibility = View.GONE
                }
                commentReplies.text = if (replyCount == 1) "...View 1 reply" else "...View more"

                onViewReplies.onViewRepliesClick(
                    data, absoluteAdapterPosition, commentReplies, hideCommentReplies,
                    repliesRecyclerView, data.isRepliesVisible, data.pageNumber
                )
            }

            username.text = data.author.account.username
            val owner = data.author.account.username

            videoThumbnail.setOnClickListener {
                val intent = Intent(context, CommentVideoPlayerActivity::class.java)
                intent.putExtra("videoUrl", videourl)
                intent.putExtra("owner", owner)
                intent.putExtra("position", absoluteAdapterPosition)
                intent.putExtra("data", data)
                (context as Activity).startActivityForResult(intent, COMMENT_VIDEO_CODE)
            }

            time.text = formatMongoTimestamp(data.createdAt)

            reply.setOnClickListener {
                EventBus.getDefault().post(ToggleReplyToTextView(data, absoluteAdapterPosition))
            }

            // Like count visibility
            when (data.likes) {
                0 -> {
                    likeCountContainer.visibility = View.GONE
                    likesCount.visibility = View.GONE
                }
                1 -> {
                    likeCountContainer.visibility = View.VISIBLE
                    likesCount.visibility = View.VISIBLE
                    likesCount.text = "1"
                }
                else -> {
                    likeCountContainer.visibility = View.VISIBLE
                    likesCount.visibility = View.VISIBLE
                    likesCount.text = "${data.likes}"
                }
            }

            // Like button setup
            if (data.isLiked) {
                likeButton.text = "Like"
                likeButton.setTextColor(ContextCompat.getColor(context, R.color.bluejeans))
            } else {
                likeButton.text = "Like"
                likeButton.setTextColor(ContextCompat.getColor(context, R.color.dark_gray))
            }

            likeButton.setOnClickListener {
                data.isLiked = !data.isLiked
                onViewReplies.likeUnLikeComment(absoluteAdapterPosition, data)
                if (data.isLiked) {
                    likeButton.text = "Like"
                    likeButton.setTextColor(ContextCompat.getColor(context, R.color.bluejeans))
                    YoYo.with(Techniques.Tada)
                        .duration(700)
                        .repeat(1)
                        .playOn(likeButton)
                } else {
                    likeButton.text = "Like"
                    likeButton.setTextColor(ContextCompat.getColor(context, R.color.dark_gray))
                }
            }

            // Reply button
            reply.setOnClickListener {
                EventBus.getDefault().post(ToggleReplyToTextView(data, absoluteAdapterPosition))
            }

            // Replies visibility and click listeners
            if (replyCount == 0) {
                commentReplies.visibility = View.GONE
                hideCommentReplies.visibility = View.GONE
                repliesRecyclerView.visibility = View.GONE
            } else {
                if (!data.hasNextPage && data.isRepliesVisible) {
                    commentReplies.visibility = View.GONE
                } else {
                    commentReplies.visibility = View.VISIBLE
                    if (data.isRepliesVisible && !data.replyCountVisible) {
                        commentReplies.visibility = View.GONE
                    } else {
                        commentReplies.text = if (data.isRepliesVisible) {
                            if (replyCount == 1) "...View 1 reply" else "...View more replies"
                        } else {
                            if (replyCount == 1) "...View 1 reply" else "...View $replyCount replies"
                        }
                    }
                }

                if (data.isRepliesVisible) {
                    hideCommentReplies.visibility = View.VISIBLE
                    repliesRecyclerView.visibility = View.VISIBLE
                } else {
                    hideCommentReplies.visibility = View.GONE
                    repliesRecyclerView.visibility = View.GONE
                    commentReplies.visibility = View.VISIBLE
                }
            }

            hideCommentReplies.setOnClickListener {
                data.isRepliesVisible = false
                repliesRecyclerView.visibility = View.GONE
                hideCommentReplies.visibility = View.GONE
                commentReplies.visibility = View.VISIBLE
                commentReplies.text = if (replyCount == 1) "...View 1 reply" else "...View $replyCount replies"
            }

            commentReplies.setOnClickListener {
                repliesRecyclerView.visibility = View.VISIBLE
                data.isRepliesVisible = true
                hideCommentReplies.visibility = View.VISIBLE
                if (!data.hasNextPage) {
                    commentReplies.visibility = View.GONE
                }
                commentReplies.text = if (replyCount == 1) "...View 1 reply" else "...View more"
                onViewReplies.onViewRepliesClick(
                    data, absoluteAdapterPosition, commentReplies, hideCommentReplies,
                    repliesRecyclerView, data.isRepliesVisible, data.pageNumber
                )
            }
        }
    }

    inner class CommentDocumentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val TAG = "Comment Document ViewHolder"


        private val imageView: ImageView = itemView.findViewById(R.id.profilePic)
        private val username: TextView = itemView.findViewById(R.id.username)
        private val time: TextView = itemView.findViewById(R.id.time)
        private val reply: TextView = itemView.findViewById(R.id.reply)
        private val commentReplies: TextView = itemView.findViewById(R.id.commentReplies)
        private val hideCommentReplies: TextView = itemView.findViewById(R.id.hideCommentReplies)
        private val likesCount: TextView = itemView.findViewById(R.id.likesCount)
        private val likeUnLikeComment: TextView = itemView.findViewById(R.id.likeUnLikeComment)
        private val likeCountContainer: LinearLayout = itemView.findViewById(R.id.likeCountContainer)
        private val repliesRecyclerView: RecyclerView = itemView.findViewById(R.id.repliesRecyclerView)
        private val docTitle: TextView = itemView.findViewById(R.id.docTitle)
        private val docInfo: TextView = itemView.findViewById(R.id.docInfo)
        private val documentLayout: RelativeLayout = itemView.findViewById(R.id.documentLayout)
        private val documentImageView: ImageView = itemView.findViewById(R.id.documentImageView)
        private  val captionContent: TextView = itemView.findViewById(R.id.content)

        private fun formatMongoTimestamp(dateTimeString: String?): String {
            if (dateTimeString.isNullOrBlank()) return "now"
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val date = inputFormat.parse(dateTimeString)
                val now = Date()
                val diffInMillis = now.time - (date?.time ?: 0)
                val diffInSeconds = diffInMillis / 1000
                val diffInMinutes = diffInSeconds / 60
                val diffInHours = diffInMinutes / 60
                val diffInDays = diffInHours / 24
                val diffInWeeks = diffInDays / 7
                val diffInMonths = diffInDays / 30 // Approximate
                val diffInYears = diffInDays / 365 // Approximate

                when {
                    diffInSeconds < 60 -> "now"
                    diffInMinutes < 60 -> "${diffInMinutes}m"
                    diffInHours < 24 -> "${diffInHours}h"
                    diffInDays == 1L -> "1d"
                    diffInDays < 7 -> "${diffInDays}d"
                    diffInWeeks == 1L -> "1w"
                    diffInWeeks < 4 -> "${diffInWeeks}w"
                    diffInMonths == 1L -> "a month ago"
                    diffInMonths < 12 -> "${diffInMonths}months"
                    diffInYears == 1L -> "1y"
                    else -> "${diffInYears}years"
                }
            } catch (e: Exception) {
                Log.w("CommentViewHolder", "Failed to format timestamp: $dateTimeString", e)
                "now"
            }
        }

        @SuppressLint("SetTextI18n")
        fun render(data: Comment) {

            var documentUrl = ""
            Log.d(TAG, "data $data")
            val replyCount = data.replyCount

            // Handle mention text
            if (data.content.contains(Regex("@\\w+"))) {
                captionContent.text = data.content
                captionContent.visibility = View.VISIBLE
            } else {
                captionContent.visibility = View.GONE
            }

            // Load profile picture
            Glide.with(itemView.context)
                .load(data.author!!.account.avatar.url)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .placeholder(R.drawable.flash21)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView)

            // Load comment image if available
            if (data.docs.isNotEmpty() && data.docs[0].url.isNotEmpty()) {
                documentUrl = data.docs[0].url
                Log.d(TAG, "Document URL is not empty for holder")
                Glide.with(itemView.context)
                    .load(documentUrl)
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
                documentUrl = data.docs[0].url
                if (documentUrl.isNotEmpty()) {
                    Log.d(TAG, "Loading Document Thumbnail from: $documentUrl")
                    Glide.with(itemView.context)
                        .load(documentUrl)
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

            // Set up replies RecyclerView
            try {
                for (commentReply in data.replies) {
                    commentReply.__v = absoluteAdapterPosition
                }
                replyCommentAdapter = if (data.isReplyPlaying) {
                    ReplyCommentAdapter(itemView.context, data, data.postId, mReplyPosition)
                } else {
                    ReplyCommentAdapter(itemView.context, data, data.postId, -1)
                }
                repliesRecyclerView.adapter = replyCommentAdapter
                replyCommentAdapter.setListener(onViewReplies)
                replyCommentAdapter.setPlayListener(object : ReplyCommentAdapter.OnPlayListener {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onPlay(position: Int) {
                        mPlayingPosition = -1
                        mParentComment = absoluteAdapterPosition
                        mReplyPosition = position
                    }
                    override fun isPlaying(position: Int, isPlaying: Boolean, progress: Float) {
                        Log.d(TAG, "isPlaying: position $position is playing $isPlaying")
                        data.isReplyPlaying = isPlaying
                        data.replies[position].progress = progress
                        notifyItemChanged(absoluteAdapterPosition)
                    }
                    override fun refreshParent(position: Int) {
                        data.isReplyPlaying = true
                        notifyItemChanged(absoluteAdapterPosition)
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Handle replies visibility
            if (replyCount == 0) {
                commentReplies.visibility = View.GONE
                hideCommentReplies.visibility = View.GONE
                repliesRecyclerView.visibility = View.GONE
            } else {
                if (!data.hasNextPage && data.isRepliesVisible) {
                    commentReplies.visibility = View.GONE
                } else {
                    commentReplies.visibility = View.VISIBLE
                    if (data.isRepliesVisible) {
                        if (data.replyCountVisible) {
                            commentReplies.text =
                                if (replyCount == 1) "...View 1 reply" else "...View more replies"
                        } else {
                            commentReplies.visibility = View.GONE
                        }
                    } else {
                        commentReplies.text =
                            if (replyCount == 1) "...View 1 reply" else "...View $replyCount replies"
                    }
                }

                if (data.isRepliesVisible) {
                    hideCommentReplies.visibility = View.VISIBLE
                    repliesRecyclerView.visibility = View.VISIBLE
                } else {
                    hideCommentReplies.visibility = View.GONE
                    repliesRecyclerView.visibility = View.GONE
                    commentReplies.visibility = View.VISIBLE
                }
            }

            // Set up click listeners for replies
            hideCommentReplies.setOnClickListener {
                data.isRepliesVisible = false
                repliesRecyclerView.visibility = View.GONE
                hideCommentReplies.visibility = View.GONE
                commentReplies.visibility = View.VISIBLE
                commentReplies.text =
                    if (replyCount == 1) "...View 1 reply" else "...View $replyCount replies"
            }

            commentReplies.setOnClickListener {
                repliesRecyclerView.visibility = View.VISIBLE
                data.isRepliesVisible = true
                if (data.isRepliesVisible) {
                    hideCommentReplies.visibility = View.VISIBLE
                } else {
                    hideCommentReplies.visibility = View.GONE
                }
                if (!data.hasNextPage) {
                    commentReplies.visibility = View.GONE
                }
                commentReplies.text = if (replyCount == 1) "...View 1 reply" else "...View more"
                onViewReplies.onViewRepliesClick(
                    data, absoluteAdapterPosition, commentReplies, hideCommentReplies,
                    repliesRecyclerView, data.isRepliesVisible, data.pageNumber
                )
            }

            // Set username and time
            username.text = data.author.account.username
            time.text = formatMongoTimestamp(data.createdAt)

            // Handle reply button
            reply.setOnClickListener {
                EventBus.getDefault().post(ToggleReplyToTextView(data, absoluteAdapterPosition))
            }

            // Like count visibility
            when (data.likes) {
                0 -> likeCountContainer.visibility = View.GONE
                1 -> {
                    likeCountContainer.visibility = View.VISIBLE
                    likesCount.text = "1"
                }
                else -> {
                    likeCountContainer.visibility = View.VISIBLE
                    likesCount.text = "${data.likes}"
                }
            }

            // Like button setup
            if (data.isLiked) {
                likeUnLikeComment.text = "Like"
                likeUnLikeComment.setTextColor(ContextCompat.getColor(itemView.context, R.color.bluejeans))
            } else {
                likeUnLikeComment.text = "Like"
                likeUnLikeComment.setTextColor(ContextCompat.getColor(itemView.context, R.color.dark_gray))
            }

            likeUnLikeComment.setOnClickListener {
                data.isLiked = !data.isLiked
                onViewReplies.likeUnLikeComment(absoluteAdapterPosition, data)
                if (data.isLiked) {
                    likeUnLikeComment.text = "Like"
                    likeUnLikeComment.setTextColor(ContextCompat.getColor(itemView.context, R.color.bluejeans))
                    YoYo.with(Techniques.Tada)
                        .duration(700)
                        .repeat(1)
                        .playOn(likeUnLikeComment)
                } else {
                    likeUnLikeComment.text = "Like"
                    likeUnLikeComment.setTextColor(ContextCompat.getColor(itemView.context, R.color.dark_gray))
                }
            }

            // Reply button
            reply.setOnClickListener {
                EventBus.getDefault().post(ToggleReplyToTextView(data, absoluteAdapterPosition))
            }

            // Replies visibility and click listeners
            if (replyCount == 0) {
                commentReplies.visibility = View.GONE
                hideCommentReplies.visibility = View.GONE
                repliesRecyclerView.visibility = View.GONE
            } else {
                if (!data.hasNextPage && data.isRepliesVisible) {
                    commentReplies.visibility = View.GONE
                } else {
                    commentReplies.visibility = View.VISIBLE
                    if (data.isRepliesVisible && !data.replyCountVisible) {
                        commentReplies.visibility = View.GONE
                    } else {
                        commentReplies.text = if (data.isRepliesVisible) {
                            if (replyCount == 1) "...View 1 reply" else "...View more replies"
                        } else {
                            if (replyCount == 1) "...View 1 reply" else "...View $replyCount replies"
                        }
                    }
                }

                if (data.isRepliesVisible) {
                    hideCommentReplies.visibility = View.VISIBLE
                    repliesRecyclerView.visibility = View.VISIBLE
                } else {
                    hideCommentReplies.visibility = View.GONE
                    repliesRecyclerView.visibility = View.GONE
                    commentReplies.visibility = View.VISIBLE
                }
            }

            hideCommentReplies.setOnClickListener {
                data.isRepliesVisible = false
                repliesRecyclerView.visibility = View.GONE
                hideCommentReplies.visibility = View.GONE
                commentReplies.visibility = View.VISIBLE
                commentReplies.text = if (replyCount == 1) "...View 1 reply" else "...View $replyCount replies"
            }

            commentReplies.setOnClickListener {
                repliesRecyclerView.visibility = View.VISIBLE
                data.isRepliesVisible = true
                hideCommentReplies.visibility = View.VISIBLE
                if (!data.hasNextPage) {
                    commentReplies.visibility = View.GONE
                }
                commentReplies.text = if (replyCount == 1) "...View 1 reply" else "...View more"
                onViewReplies.onViewRepliesClick(
                    data, absoluteAdapterPosition, commentReplies, hideCommentReplies,
                    repliesRecyclerView, data.isRepliesVisible, data.pageNumber
                )

            }
        }
    }


    fun getMentionedNames(): List<String> {
        val inputString =
            "Hello @user1, how are you? @user2 is also here. Mention @user3 in your reply."

        val regex = Regex("@\\w+")
        val matches = regex.findAll(inputString)

        return matches.map { it.value }.toList()
//        println("Mentioned users: $mentionedUsers")
    }

    fun refreshMainComment(position: Int) {
        Log.d("refreshMainComment", "refreshMainComment position $position")
        notifyItemChanged(position)
    }

    fun refreshAudioComment(position: Int) {
        val audioComment = getItem(position)
        audioComment.progress = 0F
        notifyItemChanged(position)
    }

    inner class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // You can customize this ViewHolder if needed
    }

    fun updatePlaybackButton(position: Int, isReply: Boolean, playPauseButton: ImageView) {

        Log.d("updatePlaybackButton", "updatePlaybackButton: is reply $isReply")

        if (position < 0) {
            return
        }

        if (!isReply) {
            notifyItemChanged(position)
        }

        try {

            if (::replyCommentAdapter.isInitialized) {
                replyCommentAdapter.refreshPosition()
            }
        } catch (e: NoSuchMethodError) {
            Log.e("updatePlaybackButton", "updatePlaybackButton: error on refresh position ${e.message}")
            Log.e("updatePlaybackButton", "updatePlaybackButton: error cause on refresh position ${e.cause}")
            Log.e("updatePlaybackButton", "updatePlaybackButton: error $e")
            e.printStackTrace()
        }
    }

    fun setSecondSeekBarProgress(progress: Float, position: Int) {
        Log.d("setSecondSeekBarProgress", "setSecondSeekBarProgress: progress $progress position $position")
        secondAudioDurationTV?.text = String.format(
            "%s",
            TrimVideoUtils.stringForTime(progress)
        )
        secondCurrentSeekBar?.progress = progress.toInt()
//        secondCurrentSeekBar?.progress = progress.toInt()
        if(progress == 0.0f) {
            Log.d("setSecondSeekBarProgress", "notify item")
            notifyItemChanged(position)
        }
    }

    fun setSecondWaveFormProgress(progress: Float, position: Int) {
        Log.d(
            "setSecondWaveFormProgress",
            "(main comment setSecondWaveFormProgress)-> progress $progress"
        )
        if (secondCurrentWaveForm != null && mPlayingPosition == position) {
            Log.d(
                "setSecondWaveFormProgress",
                "(main comment is initialized setSecondWaveFormProgress)-> progress $progress"
            )

            CoroutineScope(Dispatchers.Main).launch {
                secondCurrentWaveForm!!.progress = progress
                secondAudioDurationTV?.text = String.format(
                    "%s",
                    TrimVideoUtils.stringForTime(progress)
                )
            }
        } else {
            Log.d(
                "setSecondWaveFormProgress",
                "setSecondWaveFormProgress playing position $mPlayingPosition -- position $position"
            )
            Log.d("setSecondWaveFormProgress", "for main comment is not initialized")
        }
    }

    fun setReplySecondWaveFormProgress(progress: Float, position: Int) {
        if (::replyCommentAdapter.isInitialized) {
            replyCommentAdapter.setSecondWaveFormProgress(progress, position)
        }
    }

    fun setReplySecondSeekBarProgress(progress: Float, position: Int) {
        if (::replyCommentAdapter.isInitialized) {
            replyCommentAdapter.setSecondSeekBarProgress(progress, position)
        }
    }

    fun updateWaveProgress(progress: Float, position: Int) {

        Log.d("updateWaveProgress", "position $position playing $mPlayingPosition")
        if (mCurrentWaveForm != null && mPlayingPosition == position) {
            CoroutineScope(Dispatchers.Main).launch {
                mCurrentWaveForm!!.progress = progress
                currentComment?.progress = progress
//                Log.d("updateWaveProgress", "updateWaveProgress: $progress")
            }
        } else {
            mCurrentWaveForm?.progress = 0F
        }
    }

    fun updateReplyWaveProgress(progress: Float, waveformSeekBar: WaveformSeekBar) {

        replyCommentAdapter.updateWaveProgress(progress, waveformSeekBar)
    }

    fun updateReplySeekBarProgress(progress: Float, seekBar: SeekBar) {
        replyCommentAdapter.updateSeekBarProgress(progress, seekBar)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun resetAudioPlay() {
        mPlayingPosition = -1
        mParentComment = -1
        mReplyPosition = -1
        changePlayingStatus()
        if (::replyCommentAdapter.isInitialized) {
            replyCommentAdapter.refreshPosition()
            replyCommentAdapter.changeReplyAudioPlayingStatus()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

//        Log.d(TAG, "onCreateViewHolder: view type $viewType")
        return when (viewType) {
            VIEW_TYPE_TEXT_COMMENT -> {
                val itemView = inflater.inflate(R.layout.bottom_sheet_1_item, parent, false)
                CommentTextViewHolder(itemView)
            }

            VIEW_TYPE_AUDIO_COMMENT -> {
                val itemView = inflater.inflate(R.layout.comment_audio_item, parent, false)
                CommentAudioViewHolder(itemView)
            }

            VIEW_TYPE_IMAGE_COMMENT -> {
                val itemView = inflater.inflate(R.layout.comment_image_item, parent, false)
                CommentImageViewHolder(itemView)
            }

            VIEW_TYPE_VIDEO_COMMENT -> {
                val itemView = inflater.inflate(R.layout.comment_video_item, parent, false)
                CommentVideoViewHolder(itemView)
            }

            VIEW_TYPE_DOCUMENT_COMMENT -> {
                val itemView = inflater.inflate(R.layout.comment_document_item, parent, false)
                CommentDocumentViewHolder(itemView)
            }

            VIEW_TYPE_EMPTY -> {
                val itemView = inflater.inflate(R.layout.placeholder_layout, parent, false)
                EmptyViewHolder(itemView)
            }

            VIEW_TYPE_GIF -> {
                val itemView = inflater.inflate(R.layout.comment_image_item, parent, false)
                CommentGifViewHolder(itemView)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        when (holder) {
            is CommentTextViewHolder -> {
                holder.render(getItem(position))
            }

            is CommentAudioViewHolder -> {
                holder.render(getItem(position))
            }

            is CommentImageViewHolder -> {
                // You can customize this part if needed
                holder.render(getItem(position))
            }

            is CommentVideoViewHolder -> {
                holder.render(getItem(position))
            }
            is CommentDocumentViewHolder -> {
                holder.render(getItem(position))
            }
            is CommentGifViewHolder -> {
                holder.render(getItem(position))
            }
            is EmptyViewHolder -> {
                // You can customize this part if needed
                Log.d("EmptyViewHolder", "This is for empty view holder")
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val tag = "CommentType"
//        Log.d(TAG, "getItemViewType: size $itemCount")
        return when (getItem(position).contentType) {
            "audio" -> {
//                Log.d(TAG, "getItemViewType: audio type")
                VIEW_TYPE_AUDIO_COMMENT
            }

            "text" -> {
//                Log.d(TAG, "getItemViewType: text type")
                VIEW_TYPE_TEXT_COMMENT
            }

            "image" -> {
//                Log.d(tag, "getItemViewType: text type")
                VIEW_TYPE_IMAGE_COMMENT
            }

            "video" -> {
                VIEW_TYPE_VIDEO_COMMENT
            }

            "docs" -> {
                VIEW_TYPE_DOCUMENT_COMMENT
            }
            "gif" -> {
                VIEW_TYPE_GIF
            }
            else -> {
                Log.d(tag, "getItemViewType: unknown type")
                VIEW_TYPE_EMPTY
            }
        }
    }


}

interface OnViewRepliesClickListener {
    fun onViewRepliesClick(
        data: Comment,
        repliesRecyclerView: RecyclerView,
        position: Int,
    )

    fun onViewRepliesClick(
        data: Comment, position: Int,
        commentRepliesTV: TextView, hideCommentReplies: TextView,
        repliesRecyclerView: RecyclerView, isRepliesVisible: Boolean,
        page: Int
    )

    fun toggleAudioPlayer(
        audioPlayPauseBtn: ImageView,
        audioToPlayPath: String,
        position: Int,
        isReply: Boolean,
        progress: Float,
        isSeeking: Boolean,
        seekTo: Boolean,
        isVnAudio: Boolean
    )

    fun onReplyButtonClick(position: Int, data: Comment)

    fun likeUnLikeComment(position: Int, data: Comment)


}
