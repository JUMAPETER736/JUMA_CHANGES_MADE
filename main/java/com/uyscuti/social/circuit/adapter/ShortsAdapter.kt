package com.uyscuti.social.circuit.adapter

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.appcompat.widget.AppCompatButton
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.data.model.shortsmodels.OtherUsersProfile
import com.uyscuti.social.circuit.model.*
import com.uyscuti.social.circuit.User_Interface.shorts.ExoPlayerItem
import com.uyscuti.social.chatsuit.commons.ViewHolder
import com.uyscuti.social.core.common.data.room.entity.*
import com.uyscuti.social.network.utils.LocalStorage
import org.greenrobot.eventbus.EventBus
import java.util.Date
import kotlin.compareTo
import kotlin.div
import kotlin.text.toInt

// Constants
private const val TAG = "ShortsAdapter"
private const val TAG2 = "MyData"
private const val PROGRESS_UPDATE_INTERVAL = 100L


// INTERFACES


interface OnCommentsClickListener {
    fun onCommentsClick(position: Int, data: UserShortsEntity)
}

interface OnClickListeners {
    fun onSeekBarChanged(progress: Int)
    fun onDownloadClick(url: String, fileLocation: String)
    fun onShareClick(position: Int)
    fun onUploadCancelClick()
}

interface OnVideoPreparedListener {
    fun onVideoPrepared(exoPlayerItem: ExoPlayerItem)
}

// DATA CLASSES


data class MyData(
    val shortsEntity: ShortsEntity,
    val followItemEntity: ShortsEntityFollowList
)


// MAIN ADAPTER CLASS


class ShortsAdapter(

    private val commentsClickListener: OnCommentsClickListener,
    private var clickListeners: OnClickListeners,
    private var exoplayer: ExoPlayer,
    private var videoPreparedListener: OnVideoPreparedListener,
    private val onFollow: (String, String, AppCompatButton) -> Unit

) : RecyclerView.Adapter<StringViewHolder>() {

    // Properties
    private val viewHolderList = mutableListOf<StringViewHolder>()
    private val shortsList: MutableList<ShortsEntity> = mutableListOf()
    private val followingData: MutableList<ShortsEntityFollowList> = mutableListOf()
    private var currentViewHolder: StringViewHolder? = null
    private var surfaceList: ArrayList<PlayerView> = arrayListOf()
    private var currentActivePosition: Int = 0



    @SuppressLint("NotifyDataSetChanged")
    fun addData(newData: List<ShortsEntity>) {
        val startPosition = shortsList.size
        shortsList.addAll(newData)
        Log.d(TAG, "addData: shorts list size: ${shortsList.size}")

        if (startPosition == 0) {
            notifyDataSetChanged()
        } else {
            notifyItemRangeInserted(startPosition, newData.size)
        }
    }

    fun addIsFollowingData(isFollowingData: List<ShortsEntityFollowList>) {
        followingData.addAll(isFollowingData)
    }

    fun updateBtn(text: String) {
        currentViewHolder?.updateButton(text)
    }


    // UPLOAD PROGRESS METHODS


    fun getCurrentViewHolderUploadSeekBar(): SeekBar? {
        return currentViewHolder?.getUploadTopSeekBar()
    }

    fun getCurrentViewHolder(): StringViewHolder? {
        return if (currentActivePosition in 0 until viewHolderList.size) {
            viewHolderList[currentActivePosition]
        } else {
            Log.w("ShortsAdapter", "Invalid currentActivePosition: $currentActivePosition")
            null
        }
    }

    fun getCurrentViewHolderUploadCancelButton(): ImageButton? {
        return try {
            val currentViewHolder = getCurrentViewHolder()
            currentViewHolder?.itemView?.findViewById(R.id.shortsUploadCancelButton)
        } catch (e: Exception) {
            Log.e("ShortsAdapter", "Error getting cancel button: ${e.message}")
            null
        }
    }

    fun updateCurrentViewHolderUploadProgress(progress: Int) {
        currentViewHolder?.updateUploadProgress(progress)
            ?: Log.w("ShortsAdapter", "Current ViewHolder not available for progress update")
    }

    fun showCurrentViewHolderUploadProgress() {
        currentViewHolder?.showUploadProgress()
            ?: Log.w("ShortsAdapter", "Current ViewHolder not available to show upload progress")
    }

    fun hideCurrentViewHolderUploadProgress() {
        currentViewHolder?.hideUploadProgress()
            ?: Log.w("ShortsAdapter", "Current ViewHolder not available to hide upload progress")
    }

    fun setCurrentViewHolderUploadCancelListener(listener: View.OnClickListener) {
        currentViewHolder?.setUploadCancelClickListener(listener)
            ?: Log.w("ShortsAdapter", "Current ViewHolder not available to set cancel listener")
    }

    fun getViewHolderUploadSeekBar(position: Int): SeekBar? {
        return if (position >= 0 && position < viewHolderList.size) {
            viewHolderList[position].getUploadTopSeekBar()
        } else {
            Log.w("ShortsAdapter", "Invalid position $position for ViewHolder list size ${viewHolderList.size}")
            null
        }
    }

    fun getCurrentActivePosition(): Int {
        return currentActivePosition
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StringViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.shorts_view_pager_item, parent, false
        )
        val viewHolder = StringViewHolder(
            view,
            commentsClickListener,
            clickListeners,
            exoplayer,
            videoPreparedListener,
            onFollow
        )
        viewHolderList.add(viewHolder)
        return viewHolder
    }


    override fun onBindViewHolder(holder: StringViewHolder, @SuppressLint("RecyclerView") position: Int) {
        currentViewHolder = holder
        currentActivePosition = position
        val data = shortsList[position]

        // Safe handling of missing follow data
        val isFollowingData = followingData.findLast { it.followersId == data.author.account._id }
            ?: ShortsEntityFollowList(
                followersId = data.author.account._id,
                isFollowing = false
            )

        val myData = MyData(data, isFollowingData)
        ensureFollowDataExists(data)

        Log.d(TAG2, "onBindViewHolder: MyData position $position: follow: ${myData.followItemEntity}: follow size ${followingData.size}")
        holder.onBind(myData)

        val surface = holder.getSurface()

    }

    fun ensureFollowDataExists(shortsEntity: ShortsEntity) {
        val authorId = shortsEntity.author.account._id
        val exists = followingData.any { it.followersId == authorId }

        if (!exists) {
            followingData.add(
                ShortsEntityFollowList(
                    followersId = authorId,
                    isFollowing = false
                )
            )
        }
    }

    override fun onViewRecycled(holder: StringViewHolder) {
        super.onViewRecycled(holder)
        holder.onViewRecycled()

        if (holder.bindingAdapterPosition == getCurrentActivePosition()) {
            currentViewHolder = holder
        }
    }


    override fun getItemCount(): Int {
        return shortsList.size
    }


    override fun onViewAttachedToWindow(holder: StringViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.onViewAttached()

        // CRITICAL: Reattach player when view comes back
        holder.getSurface().player = exoplayer
        holder.getSurface().visibility = View.VISIBLE
    }

}


// VIEWHOLDER CLASS


class StringViewHolder(
    itemView: View,
    private val commentsClickListener: OnCommentsClickListener,
    private var onClickListeners: OnClickListeners,
    private var exoplayer: ExoPlayer,
    private var videoPreparedListener: OnVideoPreparedListener,
    private val onFollow: (String, String, AppCompatButton) -> Unit
) : ViewHolder<MyData>(itemView) {


    // UI COMPONENTS


    // Video components

    private val videoView: PlayerView = itemView.findViewById(R.id.video_view)
    private val bottomVideoSeekBar: SeekBar = itemView.findViewById(R.id.bottomShortsVideoProgressSeekBar)
    private val btnPlayPause: ImageView = itemView.findViewById(R.id.btnPlayPause)
    private var shortsUploadTopSeekBar: SeekBar? = null

    // User interaction components
    private val btnLike: ImageButton = itemView.findViewById(R.id.btnLike)
    private val favorite: ImageView = itemView.findViewById(R.id.favorite)
    private val shareBtn: ImageButton = itemView.findViewById(R.id.shareBtn)
    private val downloadBtn: ImageButton = itemView.findViewById(R.id.downloadBtn)
    private val comments: ImageView = itemView.findViewById(R.id.comments)
    private val commentsParentLayout: LinearLayout = itemView.findViewById(R.id.commentsParentLayout)

    // Profile components
    private val profileImageView: ImageView = itemView.findViewById(R.id.profileImageView)
    private val followButton: AppCompatButton = itemView.findViewById(R.id.followButton)
    private val username: TextView = itemView.findViewById(R.id.shortUsername)

    // Content components
    private val captionTextView: TextView = itemView.findViewById(R.id.tvReadMoreLess)
    private val likeCount: TextView = itemView.findViewById(R.id.likeCount)
    private val commentsCount: TextView = itemView.findViewById(R.id.commentsCount)

    // Layout components
    private val shortsViewPager: FrameLayout = itemView.findViewById(R.id.shortsViewPager)
    private val shortsUploadCancelButton: ImageButton = itemView.findViewById(R.id.shortsUploadCancelButton)


    // PROPERTIES


    private var player: ExoPlayer? = null
    private var totalLikes = 0
    private var totalComments = 0
    private var isLiked = false
    private var isFavorite = false
    private var isFollowed = false
    private var isUserSeeking = false
    private var isPlaying = false
    private var videoDuration = 0L

    // Progress tracking
    private val mainHandler = Handler(Looper.getMainLooper())
    private val progressUpdateRunnable = object : Runnable {
        override fun run() {
            updateSeekBarProgress()
            mainHandler.postDelayed(this, PROGRESS_UPDATE_INTERVAL)
        }
    }





    private fun setupClickListeners(
        data: MyData,
        url: String,
        shortOwnerId: String,
        shortOwnerName: String,
        shortOwnerUsername: String,
        shortOwnerProfilePic: String
    ) {
        // Clear any existing listeners to prevent duplicates
        commentsParentLayout.setOnClickListener(null)
        shareBtn.setOnClickListener(null)
        downloadBtn.setOnClickListener(null)
        username.setOnClickListener(null)
        profileImageView.setOnClickListener(null)
        shortsViewPager.setOnClickListener(null)

        // Set new listeners
        commentsParentLayout.setOnClickListener {
            Log.d(TAG, "onBind: Posting for main activity to open comments")
            val userShortsEntity = shortsEntityToUserShortsEntity(data.shortsEntity)
            commentsClickListener.onCommentsClick(bindingAdapterPosition, userShortsEntity)
        }

        shareBtn.setOnClickListener {
            onClickListeners.onShareClick(bindingAdapterPosition)
        }

        downloadBtn.setOnClickListener {
            onClickListeners.onDownloadClick(url, "FlashShorts")
        }

        username.setOnClickListener {
            handleUsernameClick(shortOwnerId, shortOwnerName, shortOwnerUsername, shortOwnerProfilePic)
        }

        // Profile image click (moved from setupProfileImage)
        profileImageView.setOnClickListener {
            handleProfileImageClick(shortOwnerId, shortOwnerName, shortOwnerUsername, shortOwnerProfilePic)
        }

        // Pause/Play on frame click
        shortsViewPager.setOnClickListener {
            EventBus.getDefault().post(PausePlayEvent(true))
        }
    }


    private fun setupProfileImage(
        shortOwnerId: String,
        shortOwnerName: String,
        shortOwnerUsername: String,
        shortOwnerProfilePic: String
    ) {
        // Only load the image - click listener is set in setupClickListeners
        Glide.with(itemView.context)
            .load(shortOwnerProfilePic)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .apply(RequestOptions.placeholderOf(R.drawable.flash21))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(profileImageView)


    }


    init {

        setupSeekBar()
        setupPlayer()
        setupUploadComponents()
    }

    // 5. UPDATED onBind method
    @OptIn(UnstableApi::class)
    @SuppressLint("SetTextI18n")
    override fun onBind(data: MyData) {
        val shortsEntity = data.shortsEntity

        // Properly attach player
        videoView.player = null
        videoView.player = exoplayer
        videoView.useController = false
        videoView.keepScreenOn = true
        videoView.visibility = View.VISIBLE
        videoView.requestLayout()

        val url = shortsEntity.images[0].url
        val shortOwnerId = shortsEntity.author.account._id
        val shortOwnerUsername = shortsEntity.author.account.username
        val shortOwnerName = "${shortsEntity.author.firstName} ${shortsEntity.author.lastName}"
        val shortOwnerProfilePic = shortsEntity.author.account.avatar.url

        totalComments = shortsEntity.comments

        setupEventBusEvents(shortsEntity)

        // IMPORTANT: Setup profile image BEFORE click listeners
        setupProfileImage(shortOwnerId, shortOwnerName, shortOwnerUsername, shortOwnerProfilePic)

        // NOW setup all click listeners (including profile image click)
        setupClickListeners(data, url, shortOwnerId, shortOwnerName, shortOwnerUsername, shortOwnerProfilePic)

        setupFollowButton(data, shortOwnerId)
        setupContent(shortsEntity)

        // Reset video progress
        bottomVideoSeekBar.progress = 0
        videoDuration = 0L
    }


    fun onViewRecycled() {
        stopProgressUpdates()

        // Clear click listeners to prevent memory leaks
        commentsParentLayout.setOnClickListener(null)
        shareBtn.setOnClickListener(null)
        downloadBtn.setOnClickListener(null)
        username.setOnClickListener(null)
        profileImageView.setOnClickListener(null)
        shortsViewPager.setOnClickListener(null)



        // Reset state
        videoDuration = 0L
        bottomVideoSeekBar.progress = 0
        isPlaying = false
    }


    fun reattachPlayer() {
        videoView.player = null
        videoView.post {
            videoView.player = exoplayer
            videoView.visibility = View.VISIBLE
            videoView.requestLayout()
        }
    }

    private fun setupPlayer() {
        player = exoplayer

        // Add surface callback to ensure video renders
        videoView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                Log.d(TAG, "VideoView attached to window")
                if (videoView.player == null) {
                    videoView.player = exoplayer
                }
            }

            override fun onViewDetachedFromWindow(v: View) {
                Log.d(TAG, "VideoView detached from window")
            }
        })

        exoplayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        videoDuration = exoplayer.duration
                        if (videoDuration > 0) {
                            bottomVideoSeekBar.max = (videoDuration / 1000).toInt()
                            bottomVideoSeekBar.secondaryProgress = 0
                            Log.d(TAG, "Video duration: ${videoDuration}ms, Bottom SeekBar max: ${bottomVideoSeekBar.max}")
                        }
                        // Force redraw when ready
                        videoView.invalidate()
                    }
                    Player.STATE_ENDED -> {
                        stopProgressUpdates()
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                this@StringViewHolder.isPlaying = isPlaying
                if (isPlaying) {
                    startProgressUpdates()
                    // Ensure surface is visible when playing
                    videoView.visibility = View.VISIBLE
                } else {
                    stopProgressUpdates()
                }
                Log.d(TAG, "Player isPlaying: $isPlaying")
            }

            override fun onRenderedFirstFrame() {
                Log.d(TAG, "First frame rendered - video is displaying")
            }
        })
    }

    private fun setupUploadComponents() {
        shortsUploadTopSeekBar = itemView.findViewById(R.id.uploadTopSeekBar)
        if (shortsUploadTopSeekBar == null) {
            Log.w("StringViewHolder", "uploadTopSeekBar not found in layout")
        }
        shortsUploadTopSeekBar?.visibility = View.GONE
    }


    private fun setupSeekBar() {
        // Hide the secondary progress line completely
        bottomVideoSeekBar.apply {
            secondaryProgress = 0
            splitTrack = false
            secondaryProgressTintList = ColorStateList.valueOf(
                Color.TRANSPARENT
            )
        }

        bottomVideoSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    Log.d(TAG, "User seeking to: $progress")
                    val seekPosition = (progress * 1000).toLong()
                    exoplayer.seekTo(seekPosition)
                    onClickListeners.onSeekBarChanged(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = true
                Log.d(TAG, "User started seeking")
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = false
                Log.d(TAG, "User stopped seeking")
            }
        })
    }

    // UPLOAD PROGRESS METHODS

    fun getUploadTopSeekBar(): SeekBar? = shortsUploadTopSeekBar


    fun updateUploadProgress(progress: Int) {
        // Implementation depends on your upload progress UI
        shortsUploadTopSeekBar?.progress = progress
    }

    fun hideUploadProgress() {
        shortsUploadTopSeekBar?.visibility = View.GONE
        shortsUploadCancelButton.visibility = View.GONE
    }

    fun showUploadProgress() {
        shortsUploadTopSeekBar?.visibility = View.VISIBLE
        shortsUploadCancelButton.visibility = View.VISIBLE
    }

    fun setUploadCancelClickListener(listener: View.OnClickListener) {
        shortsUploadCancelButton.setOnClickListener(listener)
    }

    private fun cancelUpload() {
        hideUploadProgress()
        // Add your cancel upload logic here
    }


    // PROGRESS TRACKING METHODS

    private fun updateSeekBarProgress() {
        if (!isUserSeeking && exoplayer.duration > 0) {
            val currentPosition = exoplayer.currentPosition
            val progress = (currentPosition / 1000).toInt()
            bottomVideoSeekBar.progress = progress
            Log.d(TAG, "Updating bottom SeekBar progress: ${currentPosition}ms -> ${progress}s")
        }
    }

    private fun startProgressUpdates() {
        stopProgressUpdates()
        mainHandler.post(progressUpdateRunnable)
    }

    private fun stopProgressUpdates() {
        mainHandler.removeCallbacks(progressUpdateRunnable)
    }

    fun updateSeekBarProgress(progress: Long) {
        Log.d(TAG, "External updateSeekBarProgress $progress")
        if (!isUserSeeking) {
            val progressInSeconds = (progress / 1000).toInt()
            bottomVideoSeekBar.progress = progressInSeconds
        }
    }

    fun setSeekBarMaxValue(max: Int) {
        bottomVideoSeekBar.max = max
        Log.d(TAG, "Bottom SeekBar max set to: $max")
    }

    fun onViewAttached() {
        if (isPlaying) {
            startProgressUpdates()
        }
    }

    fun getSurface(): PlayerView = videoView

    private fun setupEventBusEvents(shortsEntity: ShortsEntity) {
        EventBus.getDefault().post(ShortsLikeUnLikeButton(shortsEntity, btnLike, isLiked, likeCount))
        EventBus.getDefault().post(ShortsBookmarkButton(shortsEntity, favorite))
    }


    private fun setupFollowButton(data: MyData, shortOwnerId: String) {
        if (shortOwnerId == LocalStorage.getInstance(profileImageView.context).getUserId()) {
            followButton.visibility = View.INVISIBLE
            Log.d(TAG, "onBind: short owner id == logged user id")
        } else {
            followButton.visibility = View.VISIBLE
            Log.d(TAG, "onBind: ${data.followItemEntity.isFollowing}")

            updateFollowButtonState(data.followItemEntity.isFollowing)

            followButton.setOnClickListener {
                handleFollowButtonClick(shortOwnerId)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateFollowButtonState(isFollowing: Boolean) {
        if (isFollowing) {
            followButton.text = "Following"
            followButton.isAllCaps = false
            followButton.setBackgroundResource(R.drawable.shorts_following_button)
            isFollowed = true
        } else {
            followButton.text = "Follow"
            followButton.setBackgroundResource(R.drawable.shorts_follow_button_border)
            isFollowed = false
        }
    }


    private fun setupContent(shortsEntity: ShortsEntity) {
        val caption = shortsEntity.content.toString()
        if (caption.isNotEmpty()) {
            captionTextView.text = caption
        } else {
            Log.d("Caption", "Caption empty")
        }

        username.text = shortsEntity.author.account.username
        commentsCount.text = totalComments.toString()
    }


    // CLICK HANDLERS


    private fun handleUsernameClick(
        shortOwnerId: String,
        shortOwnerName: String,
        shortOwnerUsername: String,
        shortOwnerProfilePic: String
    ) {
        if (shortOwnerId == LocalStorage.getInstance(username.context).getUserId()) {
            EventBus.getDefault().post(GoToUserProfileFragment())
        } else {
            navigateToOtherUserProfile(shortOwnerName, shortOwnerUsername, shortOwnerProfilePic, shortOwnerId)
        }
    }

    private fun handleProfileImageClick(
        shortOwnerId: String,
        shortOwnerName: String,
        shortOwnerUsername: String,
        shortOwnerProfilePic: String
    ) {
        if (shortOwnerId == LocalStorage.getInstance(profileImageView.context).getUserId()) {
            EventBus.getDefault().post(GoToUserProfileFragment())
        } else {
            navigateToOtherUserProfile(shortOwnerName, shortOwnerUsername, shortOwnerProfilePic, shortOwnerId)
        }
    }

    @OptIn(UnstableApi::class)
    private fun navigateToOtherUserProfile(
        shortOwnerName: String,
        shortOwnerUsername: String,
        shortOwnerProfilePic: String,
        shortOwnerId: String
    ) {
        Log.d(TAG, "onBind: Clicked on another users profile")
        OtherUsersProfile(
            shortOwnerName, shortOwnerUsername, shortOwnerProfilePic, shortOwnerId,
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
       // UserProfileAccount.openFromShorts(profileImageView.context, otherUsersProfile)
    }

    @SuppressLint("SetTextI18n")
    private fun handleFollowButtonClick(shortOwnerId: String) {
        Log.d(TAG, "handleFollowButtonClick: is followed value $isFollowed")
        isFollowed = !isFollowed

        val followUnFollowEntity = FollowUnFollowEntity(shortOwnerId, isFollowed)

        if (isFollowed) {
            followButton.text = "Following"
            Log.d(TAG, "handleFollowButtonClick: following $isFollowed")
        } else {
            followButton.text = "Follow"
            Log.d(TAG, "handleFollowButtonClick: follow $isFollowed")
        }

        EventBus.getDefault().post(ShortsFollowButtonClicked(followUnFollowEntity))
    }


    // UTILITY METHODS

    fun updateButton(newText: String) {
        followButton.text = newText
    }

    private fun shortsEntityToUserShortsEntity(serverResponseItem: ShortsEntity): UserShortsEntity {
        return UserShortsEntity(
            __v = serverResponseItem.__v,
            _id = serverResponseItem._id,
            content = serverResponseItem.content,
            author = serverResponseItem.author,
            comments = serverResponseItem.comments,
            createdAt = serverResponseItem.createdAt,
            images = serverResponseItem.images,
            isBookmarked = serverResponseItem.isBookmarked,
            isLiked = serverResponseItem.isLiked,
            likes = serverResponseItem.likes,
            tags = serverResponseItem.tags,
            updatedAt = serverResponseItem.updatedAt,
            thumbnail = serverResponseItem.thumbnail
        )
    }


    // UNUSED METHODS (Consider removing if not needed)


    private fun handleLikeClick(shortOwnerId: String) {
        isLiked = !isLiked
        if (isLiked) {
            totalLikes += 1
            likeCount.text = totalLikes.toString()
            btnLike.setImageResource(R.drawable.filled_favorite_like)
            YoYo.with(Techniques.Tada)
                .duration(700)
                .repeat(1)
                .playOn(btnLike)
            EventBus.getDefault().post(ShortsLikeUnLike(shortOwnerId, isLiked))
        } else {
            totalLikes -= 1
            likeCount.text = totalLikes.toString()
            btnLike.setImageResource(R.drawable.favorite_svgrepo_com)
            YoYo.with(Techniques.Tada)
                .duration(700)
                .repeat(1)
                .playOn(btnLike)
            EventBus.getDefault().post(ShortsLikeUnLike(shortOwnerId, isLiked))
        }
    }

    private fun handleFavoriteClick() {
        isFavorite = !isFavorite
        if (isFavorite) {
            YoYo.with(Techniques.Tada)
                .duration(700)
                .repeat(1)
                .playOn(favorite)
            favorite.setImageResource(R.drawable.filled_favorite)
        } else {
            YoYo.with(Techniques.Tada)
                .duration(700)
                .repeat(1)
                .pivotX(2.6F)
                .playOn(favorite)
            favorite.setImageResource(R.drawable.favorite_svgrepo_com__1_)
        }
    }

}