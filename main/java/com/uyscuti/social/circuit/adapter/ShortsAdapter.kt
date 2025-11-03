package com.uyscuti.social.circuit.adapter

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
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
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
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
import com.uyscuti.social.circuit.User_Interface.OtherUserProfile.OtherUserProfileAccount
import com.uyscuti.social.core.common.data.room.entity.*
import com.uyscuti.social.network.utils.LocalStorage
import org.greenrobot.eventbus.EventBus
import java.util.Date

// Constants
private const val TAG = "ShortsAdapter"
private const val TAG2 = "MyData"
private const val PROGRESS_UPDATE_INTERVAL = 100L
private const val PRELOAD_COUNT = 10 // Number of videos to preload

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
    private var currentActivePosition: Int = 0

    // Preloading management
    private val preloadedVideos = mutableSetOf<Int>()
    private val preloadHandler = Handler(Looper.getMainLooper())



    override fun onBindViewHolder(holder: StringViewHolder, @SuppressLint("RecyclerView") position: Int) {
        // Only set currentViewHolder and position, don't call onBind for follow updates
        if (currentViewHolder != holder || currentActivePosition != position) {
            currentViewHolder = holder
            currentActivePosition = position
            val data = shortsList[position]

            val isFollowingData = followingData.findLast { it.followersId == data.author.account._id }
                ?: ShortsEntityFollowList(
                    followersId = data.author.account._id,
                    isFollowing = false
                )

            val myData = MyData(data, isFollowingData)
            ensureFollowDataExists(data)

            Log.d(TAG2, "onBindViewHolder: MyData position $position: follow: ${myData.followItemEntity}: follow size ${followingData.size}")
            holder.onBind(myData)

            // Preload adjacent videos
            preloadVideosAround(position)
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun addData(newData: List<ShortsEntity>) {
        val startPosition = shortsList.size
        shortsList.addAll(newData)
        Log.d(TAG, "addData: shorts list size: ${shortsList.size}")

        if (startPosition == 0) {
            notifyDataSetChanged()
            // Preload first batch
            preloadVideosAround(0)
        } else {
            notifyItemRangeInserted(startPosition, newData.size)
        }
    }

    fun addIsFollowingData(isFollowingData: List<ShortsEntityFollowList>) {
        followingData.addAll(isFollowingData)
    }

//    fun updateFollowState(userId: String, isFollowing: Boolean) {
//        val followData = followingData.find { it.followersId == userId }
//        if (followData != null) {
//            followData.isFollowing = isFollowing
//            Log.d(TAG, "Updated follow state for user $userId to $isFollowing without rebinding")
//        } else {
//            followingData.add(
//                ShortsEntityFollowList(
//                    followersId = userId,
//                    isFollowing = isFollowing
//                )
//            )
//            Log.d(TAG, "Added new follow state for user $userId: $isFollowing")
//        }
//    }

    // PRELOADING METHODS
    private fun preloadVideosAround(position: Int) {
        preloadHandler.removeCallbacksAndMessages(null)

        preloadHandler.post {
            val startPos = maxOf(0, position - PRELOAD_COUNT)
            val endPos = minOf(shortsList.size - 1, position + PRELOAD_COUNT)

            for (i in startPos..endPos) {
                if (i != position && !preloadedVideos.contains(i)) {
                    preloadVideo(i)
                }
            }

            // Clear old preloaded videos that are too far away
            val iterator = preloadedVideos.iterator()
            while (iterator.hasNext()) {
                val preloadedPos = iterator.next()
                if (preloadedPos < position - PRELOAD_COUNT ||
                    preloadedPos > position + PRELOAD_COUNT) {
                    iterator.remove()
                }
            }
        }
    }

    private fun preloadVideo(position: Int) {
        if (position < 0 || position >= shortsList.size) return

        try {
            val videoUrl = shortsList[position].images[0].url
            val mediaItem = MediaItem.fromUri(videoUrl)

            // Mark as preloaded
            preloadedVideos.add(position)

            Log.d(TAG, "Preloading video at position: $position")
        } catch (e: Exception) {
            Log.e(TAG, "Error preloading video at position $position: ${e.message}")
        }
    }

    fun onPositionChanged(newPosition: Int) {
        if (newPosition != currentActivePosition) {
            currentActivePosition = newPosition
            preloadVideosAround(newPosition)
            Log.d(TAG, "Position changed to: $newPosition, preloading adjacent videos")
        }
    }

    // UPLOAD PROGRESS METHODS
    fun getCurrentViewHolderUploadSeekBar(): SeekBar? {
        return currentViewHolder?.getUploadTopSeekBar()
    }

    fun getCurrentViewHolder(): StringViewHolder? {
        return if (currentActivePosition in 0 until viewHolderList.size) {
            viewHolderList[currentActivePosition]
        } else {
            Log.w(TAG, "Invalid currentActivePosition: $currentActivePosition")
            null
        }
    }

    fun getCurrentViewHolderUploadCancelButton(): ImageButton? {
        return try {
            val currentViewHolder = getCurrentViewHolder()
            currentViewHolder?.itemView?.findViewById(R.id.shortsUploadCancelButton)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cancel button: ${e.message}")
            null
        }
    }

    fun updateCurrentViewHolderUploadProgress(progress: Int) {
        currentViewHolder?.updateUploadProgress(progress)
            ?: Log.w(TAG, "Current ViewHolder not available for progress update")
    }

    fun showCurrentViewHolderUploadProgress() {
        currentViewHolder?.showUploadProgress()
            ?: Log.w(TAG, "Current ViewHolder not available to show upload progress")
    }

    fun hideCurrentViewHolderUploadProgress() {
        currentViewHolder?.hideUploadProgress()
            ?: Log.w(TAG, "Current ViewHolder not available to hide upload progress")
    }

    fun setCurrentViewHolderUploadCancelListener(listener: View.OnClickListener) {
        currentViewHolder?.setUploadCancelClickListener(listener)
            ?: Log.w(TAG, "Current ViewHolder not available to set cancel listener")
    }

    fun getViewHolderUploadSeekBar(position: Int): SeekBar? {
        return if (position >= 0 && position < viewHolderList.size) {
            viewHolderList[position].getUploadTopSeekBar()
        } else {
            Log.w(TAG, "Invalid position $position for ViewHolder list size ${viewHolderList.size}")
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

        // CRITICAL: Properly reattach player
        holder.reattachPlayer()
    }

    override fun onViewDetachedFromWindow(holder: StringViewHolder) {
        super.onViewDetachedFromWindow(holder)
        // Don't detach player completely, just pause updates
        holder.pauseUpdates()
    }

    // Add this method to ShortsAdapter
    fun updateFollowState(userId: String, isFollowing: Boolean) {
        val followData = followingData.find { it.followersId == userId }
        if (followData != null) {
            followData.isFollowing = isFollowing

            // Update the current ViewHolder's follow button without rebinding
            currentViewHolder?.updateFollowButtonState(isFollowing)

            Log.d(TAG, "Updated follow state for user $userId to $isFollowing without rebinding")
        } else {
            followingData.add(
                ShortsEntityFollowList(
                    followersId = userId,
                    isFollowing = isFollowing
                )
            )
            Log.d(TAG, "Added new follow state for user $userId: $isFollowing")
        }
    }

}

class StringViewHolder @OptIn(UnstableApi::class) constructor(
    itemView: View,
    private val commentsClickListener: OnCommentsClickListener,
    private var onClickListeners: OnClickListeners,
    private var exoplayer: ExoPlayer,
    private var videoPreparedListener: OnVideoPreparedListener,
    private val onFollow: (String, String, AppCompatButton) -> Unit
) : ViewHolder<MyData>(itemView) {

    companion object {
        private const val TAG = "StringViewHolder"
        private const val PROGRESS_UPDATE_INTERVAL = 100L
    }

    // UI COMPONENTS
    private val videoView: PlayerView = itemView.findViewById(R.id.video_view)
    private val bottomVideoSeekBar: SeekBar = itemView.findViewById(R.id.bottomShortsVideoProgressSeekBar)
    private val btnPlayPause: ImageView = itemView.findViewById(R.id.btnPlayPause)
    private var shortsUploadTopSeekBar: SeekBar? = null

    private val btnLike: ImageButton = itemView.findViewById(R.id.btnLike)
    private val favorite: ImageView = itemView.findViewById(R.id.favorite)
    private val shareBtn: ImageButton = itemView.findViewById(R.id.shareBtn)
    private val downloadBtn: ImageButton = itemView.findViewById(R.id.downloadBtn)
    private val comments: ImageView = itemView.findViewById(R.id.comments)
    private val commentsParentLayout: LinearLayout = itemView.findViewById(R.id.commentsParentLayout)

    private val shortsProfileImage: ImageView = itemView.findViewById(R.id.profileImageForShorts)
    private val followButton: AppCompatButton = itemView.findViewById(R.id.followButton)
    private val username: TextView = itemView.findViewById(R.id.shortUsername)

    private val captionTextView: TextView = itemView.findViewById(R.id.tvReadMoreLess)
    private val likeCount: TextView = itemView.findViewById(R.id.likeCount)
    private val commentsCount: TextView = itemView.findViewById(R.id.commentsCount)
    private val favoriteCount: TextView = itemView.findViewById(R.id.favoriteCounts)
    private val shareCount: TextView = itemView.findViewById(R.id.shareCount)
    private val downloadCount: TextView = itemView.findViewById(R.id.downloadCount)
    private val thumbnailImageView: ImageView = itemView.findViewById(R.id.videoThumbnail)
    private val shortsViewPager: FrameLayout = itemView.findViewById(R.id.shortsViewPager)
    private val shortsUploadCancelButton: ImageButton = itemView.findViewById(R.id.shortsUploadCancelButton)

    // PROPERTIES
    private var player: ExoPlayer? = null
    private var totalLikes = 0
    private var totalComments = 0
    private var totalFavorites = 0
    private var totalShares = 0
    private var totalDownloads = 0
    private var isLiked = false
    private var isFavorite = false
    private var isFollowed = false
    private var isUserSeeking = false
    private var isPlaying = false
    private var videoDuration = 0L

    // CRITICAL: Add flags to prevent multiple callbacks
    private var hasHiddenThumbnail = false
    private var isAttached = false

    private val mainHandler = Handler(Looper.getMainLooper())
    private val progressUpdateRunnable = object : Runnable {
        override fun run() {
            updateSeekBarProgress()
            mainHandler.postDelayed(this, PROGRESS_UPDATE_INTERVAL)
        }
    }

    init {
        setupSeekBar()
        setupPlayer()
        setupUploadComponents()

        videoView.apply {
            useController = false
            keepScreenOn = true
            setBackgroundColor(Color.BLACK)
            setShutterBackgroundColor(Color.BLACK)
        }
    }

    fun pauseUpdates() {
        stopProgressUpdates()
    }

    fun reattachPlayer() {
        if (!isAttached) {
            isAttached = true
            videoView.post {
                videoView.player = null
                videoView.player = exoplayer
                videoView.visibility = View.VISIBLE
                videoView.useController = false
                videoView.keepScreenOn = true
                videoView.requestLayout()
                videoView.invalidate()
                Log.d(TAG, "Player reattached, visibility: ${videoView.visibility}")
            }
        }
    }

    fun getUploadTopSeekBar(): SeekBar? = shortsUploadTopSeekBar

    fun updateUploadProgress(progress: Int) {
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

    fun updateSeekBarProgress(progress: Long) {
        if (!isUserSeeking) {
            val progressInSeconds = (progress / 1000).toInt()
            bottomVideoSeekBar.progress = progressInSeconds
        }
    }

    fun setSeekBarMaxValue(max: Int) {
        bottomVideoSeekBar.max = max
    }

    fun onViewAttached() {
        isAttached = true
        videoView.visibility = View.VISIBLE
        if (videoView.player == null) {
            videoView.player = exoplayer
        }
        if (isPlaying) {
            startProgressUpdates()
        }
    }

    fun getSurface(): PlayerView = videoView

    fun setUploadCancelClickListener(listener: View.OnClickListener) {
        shortsUploadCancelButton.setOnClickListener(listener)
    }

    // CRITICAL: Add method to show thumbnail
    fun showThumbnail() {
        thumbnailImageView.visibility = View.VISIBLE
        thumbnailImageView.alpha = 1f
        hasHiddenThumbnail = false
    }

    // CRITICAL: Fix the hide thumbnail method
    fun hideThumbnail() {
        // Only hide once per bind
        if (!hasHiddenThumbnail && thumbnailImageView.visibility == View.VISIBLE) {
            hasHiddenThumbnail = true
            thumbnailImageView.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction {
                    thumbnailImageView.visibility = View.GONE
                    thumbnailImageView.alpha = 1f
                }
                .start()
        }
    }

    @OptIn(UnstableApi::class)
    @SuppressLint("SetTextI18n")
    override fun onBind(data: MyData) {
        // CRITICAL: Reset flags on new bind
        hasHiddenThumbnail = false
        isAttached = false

        val shortsEntity = data.shortsEntity
        val url = shortsEntity.images[0].url
        val shortOwnerId = shortsEntity.author.account._id
        val shortOwnerUsername = shortsEntity.author.account.username
        val shortOwnerName = "${shortsEntity.author.firstName} ${shortsEntity.author.lastName}"
        val shortOwnerProfilePic = shortsEntity.author.account.avatar.url

        // Load and show thumbnail immediately
        val thumbnailUrl = shortsEntity.thumbnail.firstOrNull()?.thumbnailUrl
        if (!thumbnailUrl.isNullOrEmpty()) {
            Glide.with(itemView.context)
                .load(thumbnailUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(thumbnailImageView)
            thumbnailImageView.visibility = View.VISIBLE
            thumbnailImageView.alpha = 1f
        } else {
            thumbnailImageView.visibility = View.GONE
        }

        videoView.visibility = View.VISIBLE

        totalComments = shortsEntity.comments
        totalLikes = shortsEntity.likes
        isLiked = shortsEntity.isLiked
        isFavorite = shortsEntity.isBookmarked

        updateLikeButtonState()
        updateFavoriteButtonState()
        setupProfileImage(shortOwnerId, shortOwnerName, shortOwnerUsername, shortOwnerProfilePic)
        setupClickListeners(data, url, shortOwnerId, shortOwnerName, shortOwnerUsername, shortOwnerProfilePic)
        setupFollowButton(data, shortOwnerId)
        setupContent(shortsEntity)

        if (exoplayer.duration > 0) {
            bottomVideoSeekBar.max = (exoplayer.duration / 1000).toInt()
        }
    }

    private fun setupPlayer() {
        player = exoplayer

        videoView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                Log.d(TAG, "VideoView attached to window")
                if (videoView.player == null) {
                    videoView.player = exoplayer
                    videoView.visibility = View.VISIBLE
                }
            }

            override fun onViewDetachedFromWindow(v: View) {
                Log.d(TAG, "VideoView detached from window")
                isAttached = false
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
                            Log.d(TAG, "Video ready: ${videoDuration}ms")
                        }
                    }
                    Player.STATE_BUFFERING -> {
                        Log.d(TAG, "Video buffering")
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
                } else {
                    stopProgressUpdates()
                }
                Log.d(TAG, "Player isPlaying: $isPlaying")
            }

            override fun onRenderedFirstFrame() {
                // CRITICAL: Only log once and hide thumbnail once
                if (!hasHiddenThumbnail) {
                    Log.d(TAG, "First frame rendered - hiding thumbnail smoothly")
                    hideThumbnail()
                }
            }
        })
    }

    fun onViewRecycled() {
        stopProgressUpdates()

        // CRITICAL: Reset all flags
        hasHiddenThumbnail = false
        isAttached = false

        // Show thumbnail for next use
        thumbnailImageView.visibility = View.VISIBLE
        thumbnailImageView.alpha = 1f
        videoView.visibility = View.VISIBLE

        commentsParentLayout.setOnClickListener(null)
        btnLike.setOnClickListener(null)
        favorite.setOnClickListener(null)
        shareBtn.setOnClickListener(null)
        downloadBtn.setOnClickListener(null)
        username.setOnClickListener(null)
        shortsProfileImage.setOnClickListener(null)
        shortsViewPager.setOnClickListener(null)

        videoDuration = 0L
        bottomVideoSeekBar.progress = 0
        isPlaying = false
    }

    // Make this method public
    fun updateFollowButtonState(isFollowing: Boolean) {
        isFollowed = isFollowing
        followButton.post {
            if (isFollowing) {
                followButton.text = "Following"
                followButton.isAllCaps = false
                followButton.setBackgroundResource(R.drawable.shorts_following_button)
            } else {
                followButton.text = "Follow"
                followButton.setBackgroundResource(R.drawable.shorts_follow_button_border)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupFollowButton(data: MyData, shortOwnerId: String) {
        if (shortOwnerId == LocalStorage.getInstance(itemView.context).getUserId()) {
            followButton.visibility = View.INVISIBLE
        } else {
            followButton.visibility = View.VISIBLE

            updateFollowButtonState(data.followItemEntity.isFollowing)

            followButton.setOnClickListener(null)

            followButton.setOnClickListener {
                val newFollowState = !isFollowed
                updateFollowButtonState(newFollowState)
                data.followItemEntity.isFollowing = newFollowState

                val followUnFollowEntity = FollowUnFollowEntity(shortOwnerId, newFollowState)
                EventBus.getDefault().post(ShortsFollowButtonClicked(followUnFollowEntity))

                Log.d(TAG, "Follow button clicked: userId=$shortOwnerId, newState=$newFollowState")
            }
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
        commentsParentLayout.setOnClickListener(null)
        btnLike.setOnClickListener(null)
        favorite.setOnClickListener(null)
        shareBtn.setOnClickListener(null)
        downloadBtn.setOnClickListener(null)
        username.setOnClickListener(null)
        shortsProfileImage.setOnClickListener(null)
        shortsViewPager.setOnClickListener(null)

        commentsParentLayout.setOnClickListener {
            Log.d(TAG, "onBind: Posting for main activity to open comments")
            val userShortsEntity = shortsEntityToUserShortsEntity(data.shortsEntity)
            commentsClickListener.onCommentsClick(bindingAdapterPosition, userShortsEntity)
        }

        btnLike.setOnClickListener {
            handleLikeClick(shortOwnerId)
        }

        favorite.setOnClickListener {
            handleFavoriteClick()
        }

        shareBtn.setOnClickListener {
            handleShareClick()
        }

        downloadBtn.setOnClickListener {
            handleDownloadClick(url)
        }

        username.setOnClickListener {
            handleProfileClick(shortOwnerId, shortOwnerName, shortOwnerUsername, shortOwnerProfilePic)
        }

        shortsProfileImage.setOnClickListener {
            handleProfileClick(shortOwnerId, shortOwnerName, shortOwnerUsername, shortOwnerProfilePic)
        }

        shortsViewPager.setOnClickListener {
            EventBus.getDefault().post(PausePlayEvent(true))
        }
    }

    private fun setupContent(shortsEntity: ShortsEntity) {
        val caption = shortsEntity.content.toString()
        if (caption.isNotEmpty()) {
            captionTextView.text = caption
        }

        username.text = shortsEntity.author.account.username
        commentsCount.text = totalComments.toString()
        likeCount.text = totalLikes.toString()
        favoriteCount.text = totalFavorites.toString()
        shareCount.text = totalShares.toString()
        downloadCount.text = totalDownloads.toString()
    }

    private fun handleLikeClick(shortOwnerId: String) {
        isLiked = !isLiked
        if (isLiked) {
            totalLikes += 1
            likeCount.text = totalLikes.toString()
            btnLike.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(itemView.context, R.color.bluejeans)
            )
            btnLike.setImageResource(R.drawable.filled_favorite_like)
            YoYo.with(Techniques.Tada).duration(700).repeat(1).playOn(btnLike)
            EventBus.getDefault().post(ShortsLikeUnLike(shortOwnerId, isLiked))
        } else {
            totalLikes = maxOf(0, totalLikes - 1)
            likeCount.text = totalLikes.toString()
            btnLike.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(itemView.context, R.color.white)
            )
            btnLike.setImageResource(R.drawable.favorite_svgrepo_com)
            YoYo.with(Techniques.Tada).duration(700).repeat(1).playOn(btnLike)
            EventBus.getDefault().post(ShortsLikeUnLike(shortOwnerId, isLiked))
        }
    }

    private fun handleFavoriteClick() {
        isFavorite = !isFavorite
        if (isFavorite) {
            totalFavorites += 1
            favoriteCount.text = totalFavorites.toString()
            favorite.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(itemView.context, R.color.bluejeans)
            )
            favorite.setImageResource(R.drawable.filled_favorite)
            YoYo.with(Techniques.Tada).duration(700).repeat(1).playOn(favorite)
        } else {
            totalFavorites = maxOf(0, totalFavorites - 1)
            favoriteCount.text = totalFavorites.toString()
            favorite.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(itemView.context, R.color.white)
            )
            favorite.setImageResource(R.drawable.favorite_svgrepo_com__1_)
            YoYo.with(Techniques.Tada).duration(700).repeat(1).pivotX(2.6F).playOn(favorite)
        }
    }

    private fun handleShareClick() {
        totalShares += 1
        shareCount.text = totalShares.toString()
        onClickListeners.onShareClick(bindingAdapterPosition)
    }

    private fun handleDownloadClick(url: String) {
        totalDownloads += 1
        downloadCount.text = totalDownloads.toString()
        onClickListeners.onDownloadClick(url, "FlashShorts")
    }

    @OptIn(UnstableApi::class)
    private fun handleProfileClick(
        shortOwnerId: String,
        shortOwnerName: String,
        shortOwnerUsername: String,
        shortOwnerProfilePic: String
    ) {
        if (shortOwnerId == LocalStorage.getInstance(itemView.context).getUserId()) {
            EventBus.getDefault().post(GoToUserProfileFragment())
        } else {
            Log.d(TAG, "handleProfileClick: Navigating to another user's profile")
            val otherUsersProfile = OtherUsersProfile(
                shortOwnerName,
                shortOwnerUsername,
                shortOwnerProfilePic,
                shortOwnerId,
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

            OtherUserProfileAccount.open(
                context = itemView.context,
                user = otherUsersProfile,
                dialogPhoto = shortOwnerProfilePic,
                dialogId = shortOwnerId
            )
        }
    }

    private fun setupProfileImage(
        shortOwnerId: String,
        shortOwnerName: String,
        shortOwnerUsername: String,
        shortOwnerProfilePic: String
    ) {
        Glide.with(itemView.context)
            .load(shortOwnerProfilePic)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .apply(RequestOptions.placeholderOf(R.drawable.flash21))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(shortsProfileImage)
    }

    private fun updateLikeButtonState() {
        likeCount.text = totalLikes.toString()
        if (isLiked) {
            btnLike.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(itemView.context, R.color.bluejeans)
            )
            btnLike.setImageResource(R.drawable.filled_favorite_like)
        } else {
            btnLike.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(itemView.context, R.color.white)
            )
            btnLike.setImageResource(R.drawable.favorite_svgrepo_com)
        }
    }

    private fun updateFavoriteButtonState() {
        if (isFavorite) {
            favorite.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(itemView.context, R.color.bluejeans)
            )
            favorite.setImageResource(R.drawable.filled_favorite)
        } else {
            favorite.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(itemView.context, R.color.white)
            )
            favorite.setImageResource(R.drawable.favorite_svgrepo_com__1_)
        }
    }

    private fun setupUploadComponents() {
        shortsUploadTopSeekBar = itemView.findViewById(R.id.uploadTopSeekBar)
        if (shortsUploadTopSeekBar == null) {
            Log.w(TAG, "uploadTopSeekBar not found in layout")
        }
        shortsUploadTopSeekBar?.visibility = View.GONE
    }

    private fun setupSeekBar() {
        bottomVideoSeekBar.apply {
            secondaryProgress = 0
            splitTrack = false
            secondaryProgressTintList = ColorStateList.valueOf(Color.TRANSPARENT)
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

    private fun updateSeekBarProgress() {
        if (!isUserSeeking && exoplayer.duration > 0) {
            val currentPosition = exoplayer.currentPosition
            val progress = (currentPosition / 1000).toInt()
            bottomVideoSeekBar.progress = progress
        }
    }

    private fun startProgressUpdates() {
        if (isPlaying) {
            mainHandler.post(progressUpdateRunnable)
        }
    }

    private fun stopProgressUpdates() {
        mainHandler.removeCallbacks(progressUpdateRunnable)
    }

    private fun setupEventBusEvents(shortsEntity: ShortsEntity) {
        EventBus.getDefault().post(ShortsLikeUnLikeButton(shortsEntity, btnLike, isLiked, likeCount))
        EventBus.getDefault().post(ShortsBookmarkButton(shortsEntity, favorite))
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

//    @OptIn(UnstableApi::class)
//    @SuppressLint("SetTextI18n")
//    override fun onBind(data: MyData) {
//        val shortsEntity = data.shortsEntity
//        val url = shortsEntity.images[0].url
//        val shortOwnerId = shortsEntity.author.account._id
//        val shortOwnerUsername = shortsEntity.author.account.username
//        val shortOwnerName = "${shortsEntity.author.firstName} ${shortsEntity.author.lastName}"
//        val shortOwnerProfilePic = shortsEntity.author.account.avatar.url
//
//        // DON'T load thumbnail or prepare video here
//        // Just ensure views are in correct state
//        thumbnailImageView.visibility = View.GONE
//        videoView.visibility = View.VISIBLE
//
//        totalComments = shortsEntity.comments
//        totalLikes = shortsEntity.likes
//        isLiked = shortsEntity.isLiked
//        isFavorite = shortsEntity.isBookmarked
//
//        // Rest of your existing onBind code...
//        updateLikeButtonState()
//        updateFavoriteButtonState()
//        setupProfileImage(shortOwnerId, shortOwnerName, shortOwnerUsername, shortOwnerProfilePic)
//        setupClickListeners(data, url, shortOwnerId, shortOwnerName, shortOwnerUsername, shortOwnerProfilePic)
//        setupFollowButton(data, shortOwnerId)
//        setupContent(shortsEntity)
//
//        if (exoplayer.duration > 0) {
//            bottomVideoSeekBar.max = (exoplayer.duration / 1000).toInt()
//        }
//    }

//    private fun setupPlayer() {
//        player = exoplayer
//
//        videoView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
//            override fun onViewAttachedToWindow(v: View) {
//                Log.d(TAG, "VideoView attached to window")
//                if (videoView.player == null) {
//                    videoView.player = exoplayer
//                    videoView.visibility = View.VISIBLE
//                }
//            }
//
//            override fun onViewDetachedFromWindow(v: View) {
//                Log.d(TAG, "VideoView detached from window")
//            }
//        })
//
//        exoplayer.addListener(object : Player.Listener {
//            override fun onPlaybackStateChanged(playbackState: Int) {
//                when (playbackState) {
//                    Player.STATE_READY -> {
//                        videoDuration = exoplayer.duration
//                        if (videoDuration > 0) {
//                            bottomVideoSeekBar.max = (videoDuration / 1000).toInt()
//                            bottomVideoSeekBar.secondaryProgress = 0
//                            Log.d(TAG, "Video ready: ${videoDuration}ms")
//                        }
//
//                        // CHANGED: Hide thumbnail immediately when video is ready
//                        thumbnailImageView.visibility = View.GONE
//                        videoView.visibility = View.VISIBLE
//                        videoView.invalidate()
//                    }
//                    Player.STATE_BUFFERING -> {
//                        Log.d(TAG, "Video buffering")
//                        // CHANGED: Don't show thumbnail during buffering, keep video view visible
//                    }
//                    Player.STATE_ENDED -> {
//                        stopProgressUpdates()
//                    }
//                }
//            }
//
//            override fun onIsPlayingChanged(isPlaying: Boolean) {
//                this@StringViewHolder.isPlaying = isPlaying
//                if (isPlaying) {
//                    startProgressUpdates()
//                    // Hide thumbnail when playing
//                    thumbnailImageView.visibility = View.GONE
//                    videoView.visibility = View.VISIBLE
//                } else {
//                    stopProgressUpdates()
//                }
//                Log.d(TAG, "Player isPlaying: $isPlaying")
//            }
//
//            override fun onRenderedFirstFrame() {
//                Log.d(TAG, "First frame rendered - video is displaying")
//                // Hide thumbnail once first frame is rendered
//                thumbnailImageView.visibility = View.GONE
//                videoView.visibility = View.VISIBLE
//            }
//        })
//    }

//    fun onViewRecycled() {
//        stopProgressUpdates()
//
//        // CHANGED: Keep video view visible, just show thumbnail on top
//        thumbnailImageView.visibility = View.GONE // Changed from VISIBLE
//        videoView.visibility = View.VISIBLE // Keep visible
//
//        commentsParentLayout.setOnClickListener(null)
//        btnLike.setOnClickListener(null)
//        favorite.setOnClickListener(null)
//        shareBtn.setOnClickListener(null)
//        downloadBtn.setOnClickListener(null)
//        username.setOnClickListener(null)
//        shortsProfileImage.setOnClickListener(null)
//        shortsViewPager.setOnClickListener(null)
//
//        videoDuration = 0L
//        bottomVideoSeekBar.progress = 0
//        isPlaying = false
//    }

//    @OptIn(UnstableApi::class)
//    private fun setupClickListeners(
//
//        data: MyData,
//        url: String,
//        shortOwnerId: String,
//        shortOwnerName: String,
//        shortOwnerUsername: String,
//        shortOwnerProfilePic: String
//
//    ) {
//        commentsParentLayout.setOnClickListener(null)
//        btnLike.setOnClickListener(null)
//        favorite.setOnClickListener(null)
//        shareBtn.setOnClickListener(null)
//        downloadBtn.setOnClickListener(null)
//        username.setOnClickListener(null)
//        shortsProfileImage.setOnClickListener(null)
//        shortsViewPager.setOnClickListener(null)
//
//        commentsParentLayout.setOnClickListener {
//            Log.d(TAG, "onBind: Posting for main activity to open comments")
//            val userShortsEntity = shortsEntityToUserShortsEntity(data.shortsEntity)
//            commentsClickListener.onCommentsClick(bindingAdapterPosition, userShortsEntity)
//        }
//
//        btnLike.setOnClickListener {
//            handleLikeClick(shortOwnerId)
//        }
//
//        favorite.setOnClickListener {
//            handleFavoriteClick()
//        }
//
//        shareBtn.setOnClickListener {
//            handleShareClick()
//        }
//
//        downloadBtn.setOnClickListener {
//            handleDownloadClick(url)
//        }
//
//        username.setOnClickListener {
//            handleProfileClick(shortOwnerId, shortOwnerName, shortOwnerUsername, shortOwnerProfilePic)
//        }
//
//        shortsProfileImage.setOnClickListener {
//            handleProfileClick(shortOwnerId, shortOwnerName, shortOwnerUsername, shortOwnerProfilePic)
//        }
//
//        shortsViewPager.setOnClickListener {
//            EventBus.getDefault().post(PausePlayEvent(true))
//        }
//    }

//
//    @SuppressLint("SetTextI18n")
//    private fun setupFollowButton(data: MyData, shortOwnerId: String) {
//        if (shortOwnerId == LocalStorage.getInstance(itemView.context).getUserId()) {
//            followButton.visibility = View.INVISIBLE
//        } else {
//            followButton.visibility = View.VISIBLE
//
//            // Set initial state from data
//            updateFollowButtonState(data.followItemEntity.isFollowing)
//
//            // Remove previous listener to avoid duplicates
//            followButton.setOnClickListener(null)
//
//            followButton.setOnClickListener {
//                // Toggle state immediately for smooth UI
//                val newFollowState = !isFollowed
//                isFollowed = newFollowState
//                updateFollowButtonState(newFollowState)
//
//                // Update the data source immediately
//                data.followItemEntity.isFollowing = newFollowState
//
//                // Post event for backend update (but don't rebind the view)
//                val followUnFollowEntity = FollowUnFollowEntity(shortOwnerId, newFollowState)
//                EventBus.getDefault().post(ShortsFollowButtonClicked(followUnFollowEntity))
//
//                Log.d(TAG, "Follow button clicked: userId=$shortOwnerId, newState=$newFollowState")
//            }
//        }
//    }

//    @SuppressLint("SetTextI18n")
//    private fun updateFollowButtonState(isFollowing: Boolean) {
//        isFollowed = isFollowing
//        if (isFollowing) {
//            followButton.text = "Following"
//            followButton.isAllCaps = false
//            followButton.setBackgroundResource(R.drawable.shorts_following_button)
//        } else {
//            followButton.text = "Follow"
//            followButton.setBackgroundResource(R.drawable.shorts_follow_button_border)
//        }
//    }


//    private fun setupContent(shortsEntity: ShortsEntity) {
//        val caption = shortsEntity.content.toString()
//        if (caption.isNotEmpty()) {
//            captionTextView.text = caption
//        }
//
//        username.text = shortsEntity.author.account.username
//        commentsCount.text = totalComments.toString()
//        likeCount.text = totalLikes.toString()
//        favoriteCount.text = totalFavorites.toString()
//        shareCount.text = totalShares.toString()
//        downloadCount.text = totalDownloads.toString()
//    }
//
//    private fun handleLikeClick(shortOwnerId: String) {
//        isLiked = !isLiked
//        if (isLiked) {
//            totalLikes += 1
//            likeCount.text = totalLikes.toString()
//            btnLike.imageTintList = ColorStateList.valueOf(
//                ContextCompat.getColor(itemView.context, R.color.bluejeans)
//            )
//            btnLike.setImageResource(R.drawable.filled_favorite_like)
//            YoYo.with(Techniques.Tada).duration(700).repeat(1).playOn(btnLike)
//            EventBus.getDefault().post(ShortsLikeUnLike(shortOwnerId, isLiked))
//        } else {
//            totalLikes = maxOf(0, totalLikes - 1)
//            likeCount.text = totalLikes.toString()
//            btnLike.imageTintList = ColorStateList.valueOf(
//                ContextCompat.getColor(itemView.context, R.color.white)
//            )
//            btnLike.setImageResource(R.drawable.favorite_svgrepo_com)
//            YoYo.with(Techniques.Tada).duration(700).repeat(1).playOn(btnLike)
//            EventBus.getDefault().post(ShortsLikeUnLike(shortOwnerId, isLiked))
//        }
//    }
//
//    private fun handleFavoriteClick() {
//        isFavorite = !isFavorite
//        if (isFavorite) {
//            totalFavorites += 1
//            favoriteCount.text = totalFavorites.toString()
//            favorite.imageTintList = ColorStateList.valueOf(
//                ContextCompat.getColor(itemView.context, R.color.bluejeans)
//            )
//            favorite.setImageResource(R.drawable.filled_favorite)
//            YoYo.with(Techniques.Tada).duration(700).repeat(1).playOn(favorite)
//        } else {
//            totalFavorites = maxOf(0, totalFavorites - 1)
//            favoriteCount.text = totalFavorites.toString()
//            favorite.imageTintList = ColorStateList.valueOf(
//                ContextCompat.getColor(itemView.context, R.color.white)
//            )
//            favorite.setImageResource(R.drawable.favorite_svgrepo_com__1_)
//            YoYo.with(Techniques.Tada).duration(700).repeat(1).pivotX(2.6F).playOn(favorite)
//        }
//    }
//
//    private fun handleShareClick() {
//        totalShares += 1
//        shareCount.text = totalShares.toString()
//        onClickListeners.onShareClick(bindingAdapterPosition)
//    }
//
//    private fun handleDownloadClick(url: String) {
//        totalDownloads += 1
//        downloadCount.text = totalDownloads.toString()
//        onClickListeners.onDownloadClick(url, "FlashShorts")
//    }
//
//    @OptIn(UnstableApi::class)
//    private fun handleProfileClick(
//        shortOwnerId: String,
//        shortOwnerName: String,
//        shortOwnerUsername: String,
//        shortOwnerProfilePic: String
//    ) {
//        if (shortOwnerId == LocalStorage.getInstance(itemView.context).getUserId()) {
//            EventBus.getDefault().post(GoToUserProfileFragment())
//        } else {
//            Log.d(TAG, "handleProfileClick: Navigating to another user's profile")
//            val otherUsersProfile = OtherUsersProfile(
//                shortOwnerName,
//                shortOwnerUsername,
//                shortOwnerProfilePic,
//                shortOwnerId,
//                isVerified = false,
//                bio = "",
//                linkInBio = "",
//                isCreator = false,
//                isTrending = false,
//                isFollowing = false,
//                isPrivate = false,
//                followersCount = 0L,
//                followingCount = 0L,
//                postsCount = 0L,
//                shortsCount = 0L,
//                videosCount = 0L,
//                isOnline = false,
//                lastSeen = null,
//                joinedDate = Date(),
//                location = "",
//                website = "",
//                email = "",
//                phoneNumber = "",
//                dateOfBirth = null,
//                gender = "",
//                accountType = "user",
//                isBlocked = false,
//                isMuted = false,
//                badgeType = null,
//                level = 1,
//                reputation = 0L,
//                coverPhoto = null,
//                theme = null,
//                language = null,
//                timezone = null,
//                notificationsEnabled = true,
//                privacySettings = emptyMap(),
//                socialLinks = emptyMap(),
//                achievements = emptyList(),
//                interests = emptyList(),
//                categories = emptyList()
//            )
//
//            OtherUserProfileAccount.open(
//                context = itemView.context,
//                user = otherUsersProfile,
//                dialogPhoto = shortOwnerProfilePic,
//                dialogId = shortOwnerId
//            )
//        }
//    }
//
//    private fun setupProfileImage(
//        shortOwnerId: String,
//        shortOwnerName: String,
//        shortOwnerUsername: String,
//        shortOwnerProfilePic: String
//    ) {
//        Glide.with(itemView.context)
//            .load(shortOwnerProfilePic)
//            .apply(RequestOptions.bitmapTransform(CircleCrop()))
//            .apply(RequestOptions.placeholderOf(R.drawable.flash21))
//            .diskCacheStrategy(DiskCacheStrategy.ALL)
//            .into(shortsProfileImage)
//    }
//
//
//
//    private fun updateLikeButtonState() {
//        likeCount.text = totalLikes.toString()
//        if (isLiked) {
//            btnLike.imageTintList = ColorStateList.valueOf(
//                ContextCompat.getColor(itemView.context, R.color.bluejeans)
//            )
//            btnLike.setImageResource(R.drawable.filled_favorite_like)
//        } else {
//            btnLike.imageTintList = ColorStateList.valueOf(
//                ContextCompat.getColor(itemView.context, R.color.white)
//            )
//            btnLike.setImageResource(R.drawable.favorite_svgrepo_com)
//        }
//    }
//
//    private fun updateFavoriteButtonState() {
//        if (isFavorite) {
//            favorite.imageTintList = ColorStateList.valueOf(
//                ContextCompat.getColor(itemView.context, R.color.bluejeans)
//            )
//            favorite.setImageResource(R.drawable.filled_favorite)
//        } else {
//            favorite.imageTintList = ColorStateList.valueOf(
//                ContextCompat.getColor(itemView.context, R.color.white)
//            )
//            favorite.setImageResource(R.drawable.favorite_svgrepo_com__1_)
//        }
//    }
//
//
//    private fun setupUploadComponents() {
//        shortsUploadTopSeekBar = itemView.findViewById(R.id.uploadTopSeekBar)
//        if (shortsUploadTopSeekBar == null) {
//            Log.w(TAG, "uploadTopSeekBar not found in layout")
//        }
//        shortsUploadTopSeekBar?.visibility = View.GONE
//    }
//
//    private fun setupSeekBar() {
//        bottomVideoSeekBar.apply {
//            secondaryProgress = 0
//            splitTrack = false
//            secondaryProgressTintList = ColorStateList.valueOf(Color.TRANSPARENT)
//        }
//
//        bottomVideoSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                if (fromUser) {
//                    Log.d(TAG, "User seeking to: $progress")
//                    val seekPosition = (progress * 1000).toLong()
//                    exoplayer.seekTo(seekPosition)
//                    onClickListeners.onSeekBarChanged(progress)
//                }
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar?) {
//                isUserSeeking = true
//                Log.d(TAG, "User started seeking")
//            }
//
//            override fun onStopTrackingTouch(seekBar: SeekBar?) {
//                isUserSeeking = false
//                Log.d(TAG, "User stopped seeking")
//            }
//        })
//    }
//
//    private fun updateSeekBarProgress() {
//        if (!isUserSeeking && exoplayer.duration > 0) {
//            val currentPosition = exoplayer.currentPosition
//            val progress = (currentPosition / 1000).toInt()
//            bottomVideoSeekBar.progress = progress
//        }
//    }
//
//    private fun startProgressUpdates() {
//        if (isPlaying) {
//            mainHandler.post(progressUpdateRunnable)
//        }
//    }
//
//    private fun stopProgressUpdates() {
//        mainHandler.removeCallbacks(progressUpdateRunnable)
//    }
//
//    private fun setupEventBusEvents(shortsEntity: ShortsEntity) {
//        EventBus.getDefault().post(ShortsLikeUnLikeButton(shortsEntity, btnLike, isLiked, likeCount))
//        EventBus.getDefault().post(ShortsBookmarkButton(shortsEntity, favorite))
//    }
//
//    private fun shortsEntityToUserShortsEntity(serverResponseItem: ShortsEntity): UserShortsEntity {
//        return UserShortsEntity(
//            __v = serverResponseItem.__v,
//            _id = serverResponseItem._id,
//            content = serverResponseItem.content,
//            author = serverResponseItem.author,
//            comments = serverResponseItem.comments,
//            createdAt = serverResponseItem.createdAt,
//            images = serverResponseItem.images,
//            isBookmarked = serverResponseItem.isBookmarked,
//            isLiked = serverResponseItem.isLiked,
//            likes = serverResponseItem.likes,
//            tags = serverResponseItem.tags,
//            updatedAt = serverResponseItem.updatedAt,
//            thumbnail = serverResponseItem.thumbnail
//        )
//    }
//
//
//
//
//    // Add these methods for thumbnail management
//    fun showThumbnail() {
//        thumbnailImageView.visibility = View.VISIBLE
//        videoView.visibility = View.VISIBLE // Keep both visible during transition
//    }
//
//    fun hideThumbnail() {
//        thumbnailImageView.animate()
//            .alpha(0f)
//            .setDuration(150)
//            .withEndAction {
//                thumbnailImageView.visibility = View.GONE
//                thumbnailImageView.alpha = 1f
//            }
//            .start()
//    }
//
//    // Make this method public so adapter can call it
//    fun updateFollowButtonState(isFollowing: Boolean) {
//        isFollowed = isFollowing
//        if (isFollowing) {
//            followButton.text = "Following"
//            followButton.isAllCaps = false
//            followButton.setBackgroundResource(R.drawable.shorts_following_button)
//        } else {
//            followButton.text = "Follow"
//            followButton.setBackgroundResource(R.drawable.shorts_follow_button_border)
//        }
//    }
//
//    @OptIn(UnstableApi::class)
//    @SuppressLint("SetTextI18n")
//    override fun onBind(data: MyData) {
//        val shortsEntity = data.shortsEntity
//        val url = shortsEntity.images[0].url
//        val shortOwnerId = shortsEntity.author.account._id
//        val shortOwnerUsername = shortsEntity.author.account.username
//        val shortOwnerName = "${shortsEntity.author.firstName} ${shortsEntity.author.lastName}"
//        val shortOwnerProfilePic = shortsEntity.author.account.avatar.url
//
//        // Load thumbnail immediately
//        val thumbnailUrl = shortsEntity.thumbnail.firstOrNull()?.thumbnailUrl
//        if (!thumbnailUrl.isNullOrEmpty()) {
//            Glide.with(itemView.context)
//                .load(thumbnailUrl)
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .into(thumbnailImageView)
//            thumbnailImageView.visibility = View.VISIBLE
//        }
//
//        videoView.visibility = View.VISIBLE
//
//        totalComments = shortsEntity.comments
//        totalLikes = shortsEntity.likes
//        isLiked = shortsEntity.isLiked
//        isFavorite = shortsEntity.isBookmarked
//
//        updateLikeButtonState()
//        updateFavoriteButtonState()
//        setupProfileImage(shortOwnerId, shortOwnerName, shortOwnerUsername, shortOwnerProfilePic)
//        setupClickListeners(data, url, shortOwnerId, shortOwnerName, shortOwnerUsername, shortOwnerProfilePic)
//        setupFollowButton(data, shortOwnerId)
//        setupContent(shortsEntity)
//
//        if (exoplayer.duration > 0) {
//            bottomVideoSeekBar.max = (exoplayer.duration / 1000).toInt()
//        }
//    }
//
//    @SuppressLint("SetTextI18n")
//    private fun setupFollowButton(data: MyData, shortOwnerId: String) {
//        if (shortOwnerId == LocalStorage.getInstance(itemView.context).getUserId()) {
//            followButton.visibility = View.INVISIBLE
//        } else {
//            followButton.visibility = View.VISIBLE
//
//            // Set initial state from data
//            updateFollowButtonState(data.followItemEntity.isFollowing)
//
//            // Remove previous listener to avoid duplicates
//            followButton.setOnClickListener(null)
//
//            followButton.setOnClickListener {
//                // Toggle state immediately for smooth UI (no rebinding)
//                val newFollowState = !isFollowed
//                updateFollowButtonState(newFollowState)
//
//                // Update the data source
//                data.followItemEntity.isFollowing = newFollowState
//
//                // Post event for backend update
//                val followUnFollowEntity = FollowUnFollowEntity(shortOwnerId, newFollowState)
//                EventBus.getDefault().post(ShortsFollowButtonClicked(followUnFollowEntity))
//
//                Log.d(TAG, "Follow button clicked: userId=$shortOwnerId, newState=$newFollowState")
//            }
//        }
//    }
//
//    private fun setupPlayer() {
//        player = exoplayer
//
//        videoView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
//            override fun onViewAttachedToWindow(v: View) {
//                Log.d(TAG, "VideoView attached to window")
//                if (videoView.player == null) {
//                    videoView.player = exoplayer
//                    videoView.visibility = View.VISIBLE
//                }
//            }
//
//            override fun onViewDetachedFromWindow(v: View) {
//                Log.d(TAG, "VideoView detached from window")
//            }
//        })
//
//        exoplayer.addListener(object : Player.Listener {
//            override fun onPlaybackStateChanged(playbackState: Int) {
//                when (playbackState) {
//                    Player.STATE_READY -> {
//                        videoDuration = exoplayer.duration
//                        if (videoDuration > 0) {
//                            bottomVideoSeekBar.max = (videoDuration / 1000).toInt()
//                            bottomVideoSeekBar.secondaryProgress = 0
//                            Log.d(TAG, "Video ready: ${videoDuration}ms")
//                        }
//                        // Don't hide thumbnail here - let onRenderedFirstFrame handle it
//                    }
//                    Player.STATE_BUFFERING -> {
//                        Log.d(TAG, "Video buffering")
//                        // Keep thumbnail visible during buffering
//                    }
//                    Player.STATE_ENDED -> {
//                        stopProgressUpdates()
//                    }
//                }
//            }
//
//            override fun onIsPlayingChanged(isPlaying: Boolean) {
//                this@StringViewHolder.isPlaying = isPlaying
//                if (isPlaying) {
//                    startProgressUpdates()
//                } else {
//                    stopProgressUpdates()
//                }
//                Log.d(TAG, "Player isPlaying: $isPlaying")
//            }
//
//            override fun onRenderedFirstFrame() {
//                Log.d(TAG, "First frame rendered - hiding thumbnail smoothly")
//                // Hide thumbnail smoothly when first frame is rendered
//                hideThumbnail()
//            }
//        })
//    }
//
//    fun onViewRecycled() {
//        stopProgressUpdates()
//
//        // Show thumbnail when recycled
//        thumbnailImageView.visibility = View.VISIBLE
//        thumbnailImageView.alpha = 1f
//        videoView.visibility = View.VISIBLE
//
//        commentsParentLayout.setOnClickListener(null)
//        btnLike.setOnClickListener(null)
//        favorite.setOnClickListener(null)
//        shareBtn.setOnClickListener(null)
//        downloadBtn.setOnClickListener(null)
//        username.setOnClickListener(null)
//        shortsProfileImage.setOnClickListener(null)
//        shortsViewPager.setOnClickListener(null)
//
//        videoDuration = 0L
//        bottomVideoSeekBar.progress = 0
//        isPlaying = false
//    }

}