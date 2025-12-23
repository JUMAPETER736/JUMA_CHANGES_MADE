package com.uyscuti.social.circuit.adapter
import com.uyscuti.social.circuit.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.annotation.SuppressLint
import org.greenrobot.eventbus.EventBus
import android.text.format.DateUtils
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.uyscuti.social.circuit.data.model.shortsmodels.OtherUsersProfile
import com.uyscuti.social.circuit.model.GoToUserProfileFragment
import com.uyscuti.social.network.api.response.comment.allcomments.Author
import com.uyscuti.social.network.api.response.comment.allcomments.Comment
import com.uyscuti.social.network.utils.LocalStorage
import java.time.Instant
import java.util.Date

/**
 * Adapter for displaying comments in a RecyclerView with comment count tracking
 */
class CommentAdapter(
    private var commentList: ArrayList<Comment> = ArrayList()
) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    private var onReplyClickListener: ((Comment) -> Unit)? = null
    private var onLikeClickListener: ((Comment, Boolean) -> Unit)? = null
    private var onViewRepliesClickListener: ((Comment) -> Unit)? = null
    private var onCommentCountChangeListener: ((Int) -> Unit)? = null

    // Track total comment count (including replies)
    private var totalCommentCount: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.bottom_sheet_1_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = commentList[position]
        holder.bind(comment)
    }

    override fun getItemCount(): Int {
        return commentList.size
    }


    fun setComments(comments: ArrayList<Comment>) {
        commentList.clear()
        commentList.addAll(comments)
        calculateTotalCommentCount()
        notifyDataSetChanged()
    }


    fun addComment(comment: Comment) {
        commentList.add(0, comment) // Add to the beginning to show newest first
        totalCommentCount += 1 + (comment.replyCount ?: 0) // Add main comment + its replies
        notifyItemInserted(0)
        onCommentCountChangeListener?.invoke(totalCommentCount)
    }


    fun removeComment(position: Int) {
        if (position >= 0 && position < commentList.size) {
            val removedComment = commentList.removeAt(position)
            totalCommentCount -= 1 + (removedComment.replyCount ?: 0) // Subtract main comment + its replies
            notifyItemRemoved(position)
            onCommentCountChangeListener?.invoke(totalCommentCount)
        }
    }


    fun updateReplyCount(commentId: String, newReplyCount: Int) {
        val position = commentList.indexOfFirst { it._id == commentId }
        if (position != -1) {
            val comment = commentList[position]
            val oldReplyCount = comment.replyCount ?: 0
            comment.replyCount = newReplyCount

            // Update total count
            totalCommentCount = totalCommentCount - oldReplyCount + newReplyCount

            notifyItemChanged(position)
            onCommentCountChangeListener?.invoke(totalCommentCount)
        }
    }


    private fun calculateTotalCommentCount() {
        totalCommentCount = commentList.sumOf { comment ->
            1 + (comment.replyCount ?: 0) // 1 for the main comment + reply count
        }
        onCommentCountChangeListener?.invoke(totalCommentCount)
    }

    fun getTotalCommentCount(): Int {
        return totalCommentCount
    }


    fun setOnCommentCountChangeListener(listener: (Int) -> Unit) {
        onCommentCountChangeListener = listener
    }


    fun setOnReplyClickListener(listener: (Comment) -> Unit) {
        onReplyClickListener = listener
    }

    fun setOnLikeClickListener(listener: (Comment, Boolean) -> Unit) {
        onLikeClickListener = listener
    }

    fun setOnViewRepliesClickListener(listener: (Comment) -> Unit) {
        onViewRepliesClickListener = listener
    }


    fun refreshCommentCounts() {
        calculateTotalCommentCount()
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profilePic: ImageView = itemView.findViewById(R.id.profilePic)
        private val username: TextView = itemView.findViewById(R.id.username)
        private val time: TextView = itemView.findViewById(R.id.time)
        private val content: TextView = itemView.findViewById(R.id.content)
        private val likeUnLikeComment: ImageView = itemView.findViewById(R.id.likeUnLikeComment)
        private val likesCount: TextView = itemView.findViewById(R.id.likesCount)
        private val reply: TextView = itemView.findViewById(R.id.reply)
        private val repliesRecyclerView: RecyclerView = itemView.findViewById(R.id.repliesRecyclerView)
        private val commentReplies: TextView = itemView.findViewById(R.id.commentReplies)
        private val hideCommentReplies: TextView = itemView.findViewById(R.id.hideCommentReplies)
        private val likeLinearLayout: LinearLayout = itemView.findViewById(R.id.likeButton)

        @SuppressLint("SetTextI18n")
        fun bind(comment: Comment) {
            // Set text content
            username.text = comment.author?.account?.username ?: "Unknown"
            content.text = comment.content

            // Format and set time
            time.text = formattedMongoDateTime(comment.createdAt)

            // Set likes count
            likesCount.text = "${comment.likes} likes"

            // Load profile image properly using context
            val avatarUrl = comment.author?.account?.avatar?.url
            if (!avatarUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(avatarUrl)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .placeholder(R.drawable.person_button_svgrepo_com)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(profilePic)
            } else {
                profilePic.setImageResource(R.drawable.person_button_svgrepo_com)
            }

            // Set like button state
            setLikeButtonState(comment.isLiked)

            // Handle replies visibility and count
            handleRepliesVisibility(comment)

            // Setup click listeners
            setupClickListeners(comment)
        }

        private fun formattedMongoDateTime(string: String): String {
            return try {
                val instant = Instant.parse(string)
                val timeMillis = instant.toEpochMilli()
                DateUtils.getRelativeTimeSpanString(
                    timeMillis,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
                ).toString()
            } catch (_: Exception) {
                "Invalid date"
            }
        }

        private fun setLikeButtonState(isLiked: Boolean) {
            likeUnLikeComment.setImageResource(
                if (isLiked) R.drawable.filled_favorite_like
                else R.drawable.like_svgrepo_com
            )
        }

        @SuppressLint("SetTextI18n")
        private fun handleRepliesVisibility(comment: Comment) {
            val replyCount = comment.replyCount ?: 0

            if (replyCount > 0) {
                commentReplies.visibility = View.VISIBLE
                commentReplies.text = "View $replyCount ${if (replyCount == 1) "reply" else "replies"}"
                repliesRecyclerView.visibility = View.GONE
            } else {
                commentReplies.visibility = View.GONE
                repliesRecyclerView.visibility = View.GONE
            }

            hideCommentReplies.visibility = View.GONE
        }

        @SuppressLint("SetTextI18n")
        private fun setupClickListeners(comment: Comment) {
            // Like button click
            likeLinearLayout.setOnClickListener {
                // Toggle like state
                comment.isLiked = !comment.isLiked

                // Update likes count based on new state
                if (comment.isLiked) {
                    comment.likes = comment.likes + 1
                    setLikeButtonState(true)
                } else {
                    if (comment.likes > 0) {
                        comment.likes = comment.likes - 1
                    }
                    setLikeButtonState(false)
                }

                // Update UI
                likesCount.text = "${comment.likes} likes"

                // Animate the like button
                YoYo.with(Techniques.Tada)
                    .duration(700)
                    .repeat(1)
                    .playOn(likeUnLikeComment)

                // Trigger the callback
                onLikeClickListener?.invoke(comment, comment.isLiked)
            }

            // Reply button click
            reply.setOnClickListener {
                onReplyClickListener?.invoke(comment)
            }

            // View replies click
            commentReplies.setOnClickListener {
                onViewRepliesClickListener?.invoke(comment)

                // Show loading in repliesRecyclerView
                repliesRecyclerView.visibility = View.VISIBLE

                // Toggle visibility of buttons
                commentReplies.visibility = View.GONE
                hideCommentReplies.visibility = View.VISIBLE
            }

            // Hide replies click
            hideCommentReplies.setOnClickListener {
                repliesRecyclerView.visibility = View.GONE
                hideCommentReplies.visibility = View.GONE
                commentReplies.visibility = View.VISIBLE
            }

            // Profile image click
            profilePic.setOnClickListener {
                navigateToUserProfile(comment.author)
            }

            // Username click
            username.setOnClickListener {
                navigateToUserProfile(comment.author)
            }
        }

        @OptIn(UnstableApi::class)
        private fun navigateToUserProfile(author: Author?) {
            val feedOwnerId = author?.account?._id ?: return
            val currentUserId = LocalStorage.getInstance(itemView.context).getUserId()

            if (feedOwnerId == currentUserId) {
                EventBus.getDefault().post(GoToUserProfileFragment())
            } else {
                val feedOwnerName = "${author.firstName} ${author.lastName}".trim()
                val profilePicUrl = author.account.avatar.url
                val feedOwnerUsername = author.account.username

                val otherUsersProfile = OtherUsersProfile(
                    name = feedOwnerName,
                    username = feedOwnerUsername,
                    profilePic = profilePicUrl,
                    userId = feedOwnerId,
                    isVerified = false,
                    bio = "",
                    linkInBio = "",
                    isCreator = false,
                    isTrending = false,
                    isFollowing = false,
                    isPrivate = false,
                    followersCount = 0L,
                    followingCount = 0L,
                    postsCount = 0L,
                    shortsCount = 0L,
                    videosCount = 0L,
                    isOnline = false,
                    lastSeen = null,
                    joinedDate = Date(),
                    location = "",
                    website = "",
                    email = "",
                    phoneNumber = "",
                    dateOfBirth = null,
                    gender = "",
                    accountType = "user",
                    isBlocked = false,
                    isMuted = false,
                    badgeType = null,
                    level = 1,
                    reputation = 0L,
                    coverPhoto = null,
                    theme = null,
                    language = null,
                    timezone = null,
                    notificationsEnabled = true,
                    privacySettings = emptyMap(),
                    socialLinks = emptyMap(),
                    achievements = emptyList(),
                    interests = emptyList(),
                    categories = emptyList()
                )



            }
        }
    }
}