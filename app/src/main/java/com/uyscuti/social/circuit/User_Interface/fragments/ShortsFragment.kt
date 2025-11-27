package com.uyscuti.social.circuit.User_Interface.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.net.ConnectivityManager
import com.uyscuti.social.circuit.R
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultDataSourceFactory
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.feedRepost.PostItem
import com.uyscuti.social.chatsuit.commons.ViewHolder
import com.uyscuti.social.circuit.FlashApplication
import com.uyscuti.social.circuit.MainActivity
import com.uyscuti.social.circuit.eventbus.FeedFavoriteFollowUpdate
import com.uyscuti.social.circuit.eventbus.HideFeedFloatingActionButton
import com.uyscuti.social.circuit.eventbus.InformShortsFragment2
import com.uyscuti.social.circuit.eventbus.ShowFeedFloatingActionButton
import com.uyscuti.social.circuit.model.CancelShortsUpload
import com.uyscuti.social.circuit.model.FollowListItemViewModel
import com.uyscuti.social.circuit.model.GoToFeedFragment
import com.uyscuti.social.circuit.model.HideBottomNav
import com.uyscuti.social.circuit.model.InformAdapter
import com.uyscuti.social.circuit.model.PausePlayEvent
import com.uyscuti.social.circuit.model.PauseShort
import com.uyscuti.social.circuit.model.ProgressEvent
import com.uyscuti.social.circuit.model.ProgressViewModel
import com.uyscuti.social.circuit.model.ShortAdapterNotifyDatasetChanged
import com.uyscuti.social.circuit.model.ShortsBookmarkButton
import com.uyscuti.social.circuit.model.ShortsFavoriteUnFavorite
import com.uyscuti.social.circuit.model.ShortsFollowButtonClicked
import com.uyscuti.social.circuit.model.ShortsLikeUnLike
import com.uyscuti.social.circuit.model.ShortsLikeUnLike2
import com.uyscuti.social.circuit.model.ShortsLikeUnLikeButton
import com.uyscuti.social.circuit.model.ShortsViewModel
import com.uyscuti.social.circuit.model.ShowBottomNav
import com.uyscuti.social.circuit.model.UploadSuccessful
import com.uyscuti.social.circuit.model.UserProfileShortsViewModel
import com.uyscuti.social.circuit.presentation.GetOtherUsersProfileViewModel
import com.uyscuti.social.circuit.presentation.LikeUnLikeViewModel
import com.uyscuti.social.circuit.service.VideoPreLoadingService
import com.uyscuti.social.circuit.User_Interface.Log_In_And_Register.LoginActivity
import com.uyscuti.social.circuit.User_Interface.shorts.ExoPlayerItem
import com.uyscuti.social.circuit.User_Interface.shorts.UploadShortsActivity
import com.uyscuti.social.circuit.utils.Constants
import com.uyscuti.social.circuit.utils.removeDuplicateFollowers
import com.uyscuti.social.circuit.viewmodels.FeedShortsViewModel
import com.uyscuti.social.circuit.viewmodels.FollowUnfollowViewModel
import com.uyscuti.social.circuit.viewmodels.FollowViewModel
import com.uyscuti.social.circuit.User_Interface.OtherImportantProfileThings.SearchAllUserNameActivity
import com.uyscuti.social.circuit.User_Interface.OtherUserProfile.OtherUserProfileAccount
import com.uyscuti.social.circuit.data.model.shortsmodels.OtherUsersProfile
import com.uyscuti.social.circuit.model.GoToUserProfileFragment
import com.uyscuti.social.core.common.data.room.database.ChatDatabase
import com.uyscuti.social.core.common.data.room.entity.FollowUnFollowEntity
import com.uyscuti.social.core.common.data.room.entity.ShortsEntity
import com.uyscuti.social.core.common.data.room.entity.ShortsEntityFollowList
import com.uyscuti.social.core.common.data.room.entity.UserShortsEntity
import com.uyscuti.social.core.common.data.room.repository.ProfileRepository
import com.uyscuti.social.network.api.response.allFeedRepostsPost.RetrofitClient
import com.uyscuti.social.network.api.response.allFeedRepostsPost.ShareResponse
import com.uyscuti.social.network.api.response.getallshorts.FollowListItem
import com.uyscuti.social.network.api.response.getallshorts.Post
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlin.math.abs
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.signature.ObjectKey
import android.graphics.drawable.Drawable
import com.uyscuti.social.network.api.response.allFeedRepostsPost.CommentsResponse
import kotlin.jvm.java


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
const val SHORTS = "ShortsFragment"


// INTERFACES
interface OnCommentsClickListener {
    fun onCommentsClick(
        position: Int,
        data: UserShortsEntity,
        isFeedComment: Boolean = false
    )

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
)

    : RecyclerView.Adapter<StringViewHolder>() {

    // Properties
    private val viewHolderList = mutableListOf<StringViewHolder>()
    private val shortsList: MutableList<ShortsEntity> = mutableListOf()
    private val followingData: MutableList<ShortsEntityFollowList> = mutableListOf()
    private var currentViewHolder: StringViewHolder? = null
    private var currentActivePosition: Int = 0

    // Preloading management
    private val preloadedVideos = mutableSetOf<Int>()
    private val preloadHandler = Handler(Looper.getMainLooper())




    override fun onBindViewHolder(holder: StringViewHolder, position: Int) {
        val data = shortsList[position]

        val isFollowingData = followingData.findLast { it.followersId == data.author.account._id }
            ?: ShortsEntityFollowList(
                followersId = data.author.account._id,
                isFollowing = false
            )

        val myData = MyData(data, isFollowingData)
        ensureFollowDataExists(data)

        holder.onBind(myData)

        if (currentActivePosition == position) {
            currentViewHolder = holder
        }

        preloadVideosAround(position)
    }

    override fun onViewRecycled(holder: StringViewHolder) {
        super.onViewRecycled(holder)
        holder.onViewRecycled()

        if (holder.bindingAdapterPosition == getCurrentActivePosition()) {
            currentViewHolder = holder
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addData(newData: List<ShortsEntity>) {
        val startPosition = shortsList.size
        shortsList.addAll(newData)
        Log.d(TAG, "addData: shorts list size: ${shortsList.size}")

        if (startPosition == 0) {
            notifyDataSetChanged()
            preloadVideosAround(0)
        } else {
            notifyItemRangeInserted(startPosition, newData.size)
        }
    }

    fun addIsFollowingData(isFollowingData: List<ShortsEntityFollowList>) {
        followingData.addAll(isFollowingData)
    }

    fun updateFollowState(userId: String, isFollowing: Boolean) {
        val followData = followingData.find { it.followersId == userId }
        if (followData != null) {
            followData.isFollowing = isFollowing
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

    // Comment count management methods
    fun updateCommentCountForPosition(position: Int, newCount: Int) {
        if (position in 0 until shortsList.size) {
            shortsList[position].comments = newCount
            viewHolderList.getOrNull(position)?.updateCommentCount(newCount)
            Log.d(TAG, "Updated comment count for position $position to $newCount")
        }
    }

    fun incrementCommentCountForPosition(position: Int) {
        if (position in 0 until shortsList.size) {
            val newCount = shortsList[position].comments + 1
            updateCommentCountForPosition(position, newCount)
        }
    }

    fun decrementCommentCountForPosition(position: Int) {
        if (position in 0 until shortsList.size) {
            val newCount = maxOf(0, shortsList[position].comments - 1)
            updateCommentCountForPosition(position, newCount)
        }
    }

    // Update in ShortsAdapter class
    fun refreshCommentCountFromDatabase(position: Int, isFeedComment: Boolean = false) {
        if (position in 0 until shortsList.size) {
            val shortsId = shortsList[position]._id
            viewHolderList.getOrNull(position)?.refreshCommentCountFromDatabase(shortsId, isFeedComment)
        }
    }

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

    override fun getItemCount(): Int {
        return shortsList.size
    }

    override fun onViewAttachedToWindow(holder: StringViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.onViewAttached()
        holder.reattachPlayer()
    }

    override fun onViewDetachedFromWindow(holder: StringViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.pauseUpdates()
    }

    companion object {
        private const val TAG = "ShortsAdapter"
        private const val PRELOAD_COUNT = 2
    }
}


// VIEWHOLDER CLASS
class StringViewHolder @OptIn(UnstableApi::class) constructor(
    itemView: View,
    private val commentsClickListener: OnCommentsClickListener,
    private var onClickListeners: OnClickListeners,
    private var exoplayer: ExoPlayer,
    private var videoPreparedListener: OnVideoPreparedListener,
    private val onFollow: (String, String, AppCompatButton) -> Unit
)

    : ViewHolder<MyData>(itemView) {

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

    private var currentThumbnailUrl: String? = null

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

    // Comment count state variables
    private var serverCommentCount = 0
    private var loadedCommentCount = 0
    private var currentShorts: ShortsEntity? = null

    private val mainHandler = Handler(Looper.getMainLooper())
    private val progressUpdateRunnable = object : Runnable {
        override fun run() {
            updateSeekBarProgress()
            mainHandler.postDelayed(this, PROGRESS_UPDATE_INTERVAL)
        }
    }

    private val playerListener = object : Player.Listener {
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
                    if (currentThumbnailUrl != null) {
                        thumbnailImageView.visibility = View.VISIBLE
                    }
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
            Log.d(TAG, "Top video thumbnail visible and the bottom coming video thumbnail")
            thumbnailImageView.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction {
                    thumbnailImageView.visibility = View.GONE
                    thumbnailImageView.setImageDrawable(null)
                    thumbnailImageView.alpha = 1f
                    currentThumbnailUrl = null
                }
                .start()
            videoView.visibility = View.VISIBLE
        }
    }

    init {
        setupSeekBar()
        setupUploadComponents()

        videoView.apply {
            useController = false
            keepScreenOn = true
            setBackgroundColor(Color.BLACK)
            setShutterBackgroundColor(Color.BLACK)
        }

        exoplayer.repeatMode = Player.REPEAT_MODE_ONE
    }

    fun pauseUpdates() {
        stopProgressUpdates()
    }

    fun loadThumbnail(thumbnailUrl: String?) {
        thumbnailImageView.setImageDrawable(null)

        if (thumbnailUrl != null && thumbnailUrl.isNotEmpty()) {
            currentThumbnailUrl = thumbnailUrl
            thumbnailImageView.visibility = View.VISIBLE
            thumbnailImageView.alpha = 1f
            videoView.visibility = View.VISIBLE

            Glide.with(itemView.context)
                .load(thumbnailUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .signature(ObjectKey(thumbnailUrl))
                .into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: com.bumptech.glide.request.transition.Transition<in Drawable>?
                    ) {
                        if (currentThumbnailUrl == thumbnailUrl) {
                            thumbnailImageView.setImageDrawable(resource)
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        thumbnailImageView.setImageDrawable(placeholder)
                    }
                })
        } else {
            thumbnailImageView.visibility = View.GONE
            currentThumbnailUrl = null
        }
    }

    fun reattachPlayer() {
        videoView.post {
            videoView.player = null
            videoView.player = exoplayer
            videoView.visibility = View.VISIBLE
            videoView.useController = false
            videoView.keepScreenOn = true
            videoView.requestLayout()
            videoView.invalidate()

            exoplayer.addListener(playerListener)

            Log.d(TAG, "Player reattached, showing the bottom coming video: ${videoView.visibility}")
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
        videoView.visibility = View.VISIBLE
        if (videoView.player == null) {
            videoView.player = exoplayer
        }
        exoplayer.addListener(playerListener)
        if (isPlaying) {
            startProgressUpdates()
        }
    }

    fun getSurface(): PlayerView = videoView

    fun setUploadCancelClickListener(listener: View.OnClickListener) {
        shortsUploadCancelButton.setOnClickListener(listener)
    }

    @OptIn(UnstableApi::class)
    @SuppressLint("SetTextI18n")
    override fun onBind(data: MyData) {
        val shortsEntity = data.shortsEntity
        val url = shortsEntity.images[0].url
        val shortOwnerId = shortsEntity.author.account._id
        val shortOwnerUsername = shortsEntity.author.account.username
        val shortOwnerName = "${shortsEntity.author.firstName} ${shortsEntity.author.lastName}"
        val shortOwnerProfilePic = shortsEntity.author.account.avatar.url

        // Store current shorts reference
        currentShorts = shortsEntity

        val thumbnailUrl = shortsEntity.thumbnail.firstOrNull()?.thumbnailUrl
        currentThumbnailUrl = thumbnailUrl

        thumbnailImageView.visibility = View.GONE
        videoView.visibility = View.VISIBLE

        // Initialize comment counts
        initializeCommentCounts(shortsEntity)

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

    // Comment count initialization
    private fun initializeCommentCounts(shortsEntity: ShortsEntity) {
        serverCommentCount = shortsEntity.comments
        totalComments = serverCommentCount
        loadedCommentCount = 0
        Log.d(TAG, "Initialized comment counts - Server: $serverCommentCount, Total: $totalComments")
    }

    // Update comment count display
    private fun updateCommentCountDisplay() {
        commentsCount.text = formatCount(totalComments)
        commentsCount.visibility = View.VISIBLE
        Log.d(TAG, "Updated comment count display: ${commentsCount.text}")
    }

    // Public method to update comment count (called from adapter)
    fun updateCommentCount(newCount: Int) {
        Log.d(TAG, "updateCommentCount: Updating comment count from $totalComments to $newCount")
        totalComments = if (newCount < 0) {
            Log.w(TAG, "updateCommentCount: Negative count received, setting to 0")
            0
        } else {
            newCount
        }

        currentShorts?.let { shorts ->
            shorts.comments = totalComments
        }

        updateCommentCountDisplay()

        // Add animation
        YoYo.with(Techniques.Pulse)
            .duration(500)
            .playOn(commentsCount)
    }

    // Decrement comment count
    fun decrementCommentCount() {
        val newCount = maxOf(0, totalComments - 1)
        Log.d(TAG, "decrementCommentCount: Decrementing from $totalComments to $newCount")
        updateCommentCount(newCount)
    }

    // Increment comment count
    fun incrementCommentCount() {
        val newCount = totalComments + 1
        Log.d(TAG, "incrementCommentCount: Incrementing from $totalComments to $newCount")
        updateCommentCount(newCount)
    }

    // Updated method that works for both Shorts and Feed comments
    fun refreshCommentCountFromDatabase(shortsId: String, isFeedComment: Boolean = false) {
        Log.d(TAG, "refreshCommentCountFromDatabase: Refreshing count for ${if (isFeedComment) "feed" else "shorts"}: $shortsId")

        // Choose the appropriate endpoint based on the flag
        val call = if (isFeedComment) {
            RetrofitClient.commentService.getFeedComments(shortsId, page = 1, limit = 1)
        } else {
            RetrofitClient.commentService.getShortsComments(shortsId, page = 1, limit = 1)
        }

        call.enqueue(object : Callback<CommentsResponse> {
            override fun onResponse(call: Call<CommentsResponse>, response: Response<CommentsResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { commentsResponse ->
                        val newCount = commentsResponse.totalCount
                        Log.d(TAG, "refreshCommentCountFromDatabase: Got count: $newCount from ${if (isFeedComment) "feed" else "shorts"} endpoint")
                        updateCommentCount(newCount)
                        currentShorts?.let { shorts ->
                            shorts.comments = newCount
                        }
                    }
                } else {
                    Log.e(TAG, "refreshCommentCountFromDatabase: Failed with code: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<CommentsResponse>, t: Throwable) {
                Log.e(TAG, "refreshCommentCountFromDatabase: Network error", t)
            }
        })
    }

    // Format count for display
    @SuppressLint("DefaultLocale")
    private fun formatCount(count: Int): String {
        return when {
            count >= 1_000_000 -> {
                val millions = count / 1_000_000.0
                if (millions == millions.toInt().toDouble()) {
                    "${millions.toInt()}M"
                } else {
                    String.format("%.1fM", millions)
                }
            }
            count >= 1_000 -> {
                val thousands = count / 1_000.0
                if (thousands == thousands.toInt().toDouble()) {
                    "${thousands.toInt()}K"
                } else {
                    String.format("%.1fK", thousands)
                }
            }
            else -> count.toString()
        }
    }

    fun onViewRecycled() {
        stopProgressUpdates()

        thumbnailImageView.animate().cancel()
        thumbnailImageView.visibility = View.GONE
        thumbnailImageView.setImageDrawable(null)
        thumbnailImageView.alpha = 1f
        currentThumbnailUrl = null

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
        exoplayer.removeListener(playerListener)

        // Clear shorts reference
        currentShorts = null
    }

    @OptIn(UnstableApi::class)
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
            Log.d(TAG, "onBind: Opening comments for shorts")
            val userShortsEntity = shortsEntityToUserShortsEntity(data.shortsEntity)

            // Add animation for comment button click
            YoYo.with(Techniques.Tada)
                .duration(700)
                .repeat(1)
                .playOn(commentsCount)

            // Pass the shorts entity with isFeedComment flag set to false for shorts
            commentsClickListener.onCommentsClick(
                bindingAdapterPosition,
                userShortsEntity,
                isFeedComment = false  //Indicate this is a shorts comment
            )
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
                isFollowed = newFollowState
                updateFollowButtonState(newFollowState)

                data.followItemEntity.isFollowing = newFollowState

                val followUnFollowEntity = FollowUnFollowEntity(shortOwnerId, newFollowState)
                EventBus.getDefault().post(ShortsFollowButtonClicked(followUnFollowEntity))

                Log.d(TAG, "Follow button clicked: userId=$shortOwnerId, newState=$newFollowState")
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateFollowButtonState(isFollowing: Boolean) {
        isFollowed = isFollowing
        if (isFollowing) {
            followButton.text = "Following"
            followButton.isAllCaps = false
            followButton.setBackgroundResource(R.drawable.shorts_following_button)
        } else {
            followButton.text = "Follow"
            followButton.setBackgroundResource(R.drawable.shorts_follow_button_border)
        }
    }

    private fun setupContent(shortsEntity: ShortsEntity) {
        val caption = shortsEntity.content.toString()
        if (caption.isNotEmpty()) {
            captionTextView.text = caption
        }

        username.text = shortsEntity.author.account.username
        commentsCount.text = formatCount(totalComments)
        likeCount.text = formatCount(totalLikes)
        favoriteCount.text = formatCount(totalFavorites)
        shareCount.text = formatCount(totalShares)
        downloadCount.text = formatCount(totalDownloads)
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
}



@UnstableApi
@AndroidEntryPoint
class ShotsFragment : Fragment(), OnCommentsClickListener, OnClickListeners {



    companion object {
        const val REQUEST_UPLOAD_SHORTS_ACTIVITY = 123 // You can use any unique value
        private const val FEED_ARG_DATA = "feed_arg_data"
        private const val FEED_POST_POSITION = "feed_post_position"
        private const val FEED_SHORT_BUSINESS_ID = "feed_short_business_id"
        private const val FEED_SHORT_BUSINESS_FILE_ID = "feed_short_business_file_id"

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ShotsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }


        //        fun willReturnToFeedInstance(data: String? = null): ShotsFragment {
        fun willReturnToFeedInstance(
            data: Boolean = false, feedPostPosition: Int = -1,
            feedShortsBusinessId:String, feedShortsBusinessFileId: String
        ): ShotsFragment {
            Log.d("openShortsFragment", "willReturnToFeedInstance: feedPostPosition $feedPostPosition")
            val fragment = ShotsFragment()
            val args = Bundle()
            data.let {
                args.putBoolean(FEED_ARG_DATA, it)
            }
            feedPostPosition.let {
                args.putInt(FEED_POST_POSITION, it)
            }

            feedShortsBusinessId.let {
                args.putString(FEED_SHORT_BUSINESS_ID, feedShortsBusinessId)
            }

            feedShortsBusinessFileId.let {
                args.putString(FEED_SHORT_BUSINESS_FILE_ID, it)
            }

            fragment.arguments = args
            return fragment
        }
    }

    

    // Fragment Parameters
    private var param1: String? = null
    private var param2: String? = null

    // UI Components - ViewPager & Adapter
    private lateinit var viewPager: ViewPager2
    private lateinit var shortsAdapter: ShortsAdapter
    private var shouldFilter = false

    // UI Components - Buttons & Actions
    private lateinit var fabAction: FloatingActionButton
    private lateinit var shortsMenu: ImageView
    private lateinit var cancelShortsUpload: ImageView

    // UI Components - Progress Indicators
    private lateinit var progressBar: ProgressBar
    private lateinit var shortsDownloadProgressBar: ProgressBar
    private lateinit var progressBarLayout: LinearLayout
    private lateinit var downloadProgressBarLayout: LinearLayout
    private lateinit var searchForAllShorts: ImageView

    // UI Components - Media & Controls
    // private lateinit var playerView: PlayerView
    private lateinit var shortSeekBar: SeekBar
    private lateinit var shortsDownloadImageView: ImageView

    // Data Collections
    private var videoShorts = ArrayList<ShortsEntity>()
    private var shortsList = ArrayList<String>()
    private val exoPlayerItems = ArrayList<ExoPlayerItem>()
    val uniqueEntitiesSet = HashSet<ShortsEntity>()

    // Media Player & ExoPlayer
    private var exoPlayer: ExoPlayer? = null
    private var currentPlayerListener: Player.Listener? = null
    //private var isPlayerPreparing = false
    private var isUserSeeking = false

    // ExoPlayer Data Sources
    private lateinit var httpDataSourceFactory: HttpDataSource.Factory
    private lateinit var defaultDataSourceFactory: DefaultDataSourceFactory
    private lateinit var cacheDataSourceFactory: CacheDataSource.Factory
    private val simpleCache: SimpleCache = FlashApplication.cache
    private val playbackStateListener: Player.Listener = playbackStateListener()

    // Media Content & URLs
    private var videoUrl: String? = null
    private var postItem: PostItem? = null

    // UI Animations
    private var wifiAnimation: AnimationDrawable? = null

    // Position & State Management
    private var currentPosition: Int = -1

    // File Picker & Permissions
    private var getContentLauncher: ActivityResultLauncher<String>? = null
    private val PICK_VIDEO_REQUEST = "video/*"
    private val requestCode = 2024
    private val TOTAL_PROGRESS = 200

    private val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 12
    private val MY_MANAGE_EXTERNAL_STORAGE_REQUEST_CODE = 202

    // Dependency Injection
    @Inject
    lateinit var retrofitIns: RetrofitInstance

    private var filterUserId: String? = null
    private var filterUsername: String? = null
    private var filterUserAvatar: String? = null
    private var isUserFiltered = false
    private var isLoadingMore = false
    private var currentPage = 1

    // ViewModels
    private val eventProgressSets = HashMap<String, HashSet<Int>>()
    private val feesShortsSharedViewModel: FeedShortsViewModel by activityViewModels()
    private val progressViewModel: ProgressViewModel by activityViewModels()
    private val likeUnLikeViewModel: LikeUnLikeViewModel by activityViewModels()
    private val shortsViewModel: ShortsViewModel by activityViewModels()
    private val followShortsViewModel: FollowListItemViewModel by viewModels()
    private val followViewModel: FollowViewModel by viewModels()
    private val followUnFollowViewModel: FollowUnfollowViewModel by viewModels()
    private val getOtherUsersProfileViewModel: GetOtherUsersProfileViewModel by viewModels()
    private val userProfileShortsViewModel: UserProfileShortsViewModel by activityViewModels()

    // Repository
    private lateinit var myProfileRepository: ProfileRepository



    // Back Press & Navigation
    private val doubleBackPressThreshold = 3
    private var backPressCount = 0
    private var feedOnBackPressedData: Boolean = false
    private var feedPostPosition: Int = -1

    // Feed Business Data
    private var feedShortsBusinessId = ""
    private var feedShortsBusinessFileId = ""
    val count = 0


    init {
        // Increase cache size for better performance
        simpleCache.release() // Clear if needed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // From the Tapped Files Viewers Post fragment
        arguments?.let { bundle ->
            videoUrl = bundle.getString("video_url")
            postItem = bundle.getParcelable("post_item")
            val postId = bundle.getString("post_id")

            Log.d("ShotsFragment", "Received video URL: $videoUrl")
            Log.d("ShotsFragment", "Received post ID: $postId")
        }

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            feedShortsBusinessId = it.getString(FEED_SHORT_BUSINESS_ID) ?: run {
                Log.d("openShortsFragment", "FEED_SHORT_BUSINESS_ID is not set")
                "default_value"
            }
            feedShortsBusinessFileId = it.getString(FEED_SHORT_BUSINESS_FILE_ID) ?: run {
                Log.d("openShortsFragment", "FEED_SHORT_BUSINESS_ID is not set")
                "default_value_file_id"
            }
        }

        feedOnBackPressedData = arguments?.getBoolean(FEED_ARG_DATA) == true
        feedPostPosition = arguments?.getInt(FEED_POST_POSITION)!!

        // ADD THESE LINES
        shouldFilter = activity?.intent?.getBooleanExtra("should_filter", false) ?: false
        if (shouldFilter) {
            filterUserId = activity?.intent?.getStringExtra("filter_user_id")
            filterUsername = activity?.intent?.getStringExtra("filter_username")
            filterUserAvatar = activity?.intent?.getStringExtra("filter_user_avatar")
            isUserFiltered = true
            Log.d("ShotsFragment", "Filtering enabled for user: $filterUsername (ID: $filterUserId)")
        }

        Log.d("openShortsFragment", "onCreate:feedShortsBusinessId $feedShortsBusinessId")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        videoUrl?.let { url ->
            setupVideoPlaybackInShots(url)
        }




        if (savedInstanceState == null) {

            Log.d("ViewModel", "onViewCreated: view not created")
        } else {
            Log.d("ViewModel", "onViewCreated: view already created")
        }
    }

    @SuppressLint("NotifyDataSetChanged", "MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.Q)
    @OptIn(UnstableApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {

        val TAG = "ShortsFragment onCreateView"
        val view = inflater.inflate(R.layout.shorts_fragment, container, false)
        activity?.window?.statusBarColor = ContextCompat.getColor(requireContext(), R.color.black)

        // Set the navigation bar color dynamically
        activity?.window?.navigationBarColor =
            ContextCompat.getColor(requireContext(), R.color.black)

        myProfileRepository =
            ProfileRepository(ChatDatabase.getInstance(requireActivity()).profileDao())
        progressBar = view.findViewById(R.id.progressBar)
        shortsDownloadProgressBar = view.findViewById(R.id.shortsDownloadProgressBar)
        progressBarLayout = view.findViewById(R.id.progressBarLayout)
        downloadProgressBarLayout = view.findViewById(R.id.downloadProgressBarLayout)
        shortsDownloadImageView = view.findViewById(R.id.shortsDownloadImageView)
        cancelShortsUpload = view.findViewById(R.id.cancelShortsUpload)
        shortSeekBar = view.findViewById(R.id.shortSeekBar)
        shortsMenu = view.findViewById(R.id.shortsMenu)
        searchForAllShorts = view.findViewById(R.id.searchForAllShorts)

        shortsMenu.setOnClickListener {
            val popupMenu = PopupMenu(context, shortsMenu)
            popupMenu.menuInflater.inflate(R.menu.shorts_menu_item, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_setting -> {
                        true
                    }
                    R.id.logout -> {
                        performLogout()
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }



        searchForAllShorts.setOnClickListener {
            val intent = Intent(requireContext(), SearchAllUserNameActivity::class.java)
            startActivity(intent)


            requireActivity().overridePendingTransition(
                R.anim.slide_in_up,
                R.anim.stay
            )
        }

        shortSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    exoPlayer?.seekTo(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = false
            }
        })

        // Initialize the launcher
        getContentLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    openUploadShortsActivity(uri)
                }
            }

        (activity as? MainActivity)?.hideAppBar()

        viewPager = view.findViewById(R.id.shortsViewPager)


        httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(8000)
            .setReadTimeoutMs(8000)



        val dataSourceFactory = DefaultDataSource.Factory(
            requireContext(),
            httpDataSourceFactory
        )

        val mediaSourceFactory: MediaSource.Factory =
            DefaultMediaSourceFactory(requireContext())
                .setDataSourceFactory(dataSourceFactory) // Use direct factory without cache

        exoPlayer = ExoPlayer.Builder(requireActivity())
            .setMediaSourceFactory(mediaSourceFactory)
            .setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
            .setHandleAudioBecomingNoisy(true)
            .setLoadControl(
                DefaultLoadControl.Builder()
                    .setBufferDurationsMs(
                        1500,
                        15000,
                        1000,
                        1000
                    )
                    .setPrioritizeTimeOverSizeThresholds(true)
                    .build()
            )
            .build()

        exoPlayer?.apply {
            repeatMode = Player.REPEAT_MODE_ONE
            playWhenReady = false // Will be set to true in prepareAndPlayVideoImmediately
        }

        val tag = "handleFollowButtonClick"
        val connectivityManager =
            requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkInfo = connectivityManager.activeNetworkInfo
        val isConnected = networkInfo != null && networkInfo.isConnected

        lifecycleScope.launch(Dispatchers.IO) {

            val followEntity = followShortsViewModel.allShortsList

            // Inside the lifecycleScope.launch(Dispatchers.IO) block, after checking shouldFilter
            if (shouldFilter && filterUserId != null) {
                // Clear existing data before loading filtered content
                withContext(Dispatchers.Main) {
                    shortsViewModel.mutableShortsList.clear()
                    shortsViewModel.videoShorts.clear()
                    shortsViewModel.followList.clear()
                    uniqueEntitiesSet.clear()
                    shortsList.clear()
                }
            }

            if (!shortsViewModel.isResuming) {
                // ADD THESE LINES HERE - Clear data if filtering
                if (shouldFilter && filterUserId != null) {
                    shortsViewModel.mutableShortsList.clear()
                    shortsViewModel.videoShorts.clear()
                    shortsViewModel.followList.clear()
                    uniqueEntitiesSet.clear()
                    shortsList.clear()
                    Log.d("ShotsFragment", "Cleared existing data for filtered view")
                }

                loadMoreShortsPage1(1)
                Log.d("Resume", "onCreateView: ! ${!shortsViewModel.isResuming}")
            }

            withContext(Dispatchers.Main) {
                shortsViewModel.allShortsList.observe(viewLifecycleOwner, Observer {
                    for (entity in it) {
                        Log.d("PreLoad", "onCreateView: $it ")
                    }
                })
            }

            if (!shortsViewModel.isResuming) {
                loadMoreShortsPage1(1)
                Log.d("Resume", "onCreateView: ! ${!shortsViewModel.isResuming}")
            }

            shortsAdapter =
                ShortsAdapter(requireActivity() as OnCommentsClickListener, this@ShotsFragment,
                    exoPlayer!!, object :
                        OnVideoPreparedListener {
                        override fun onVideoPrepared(exoPlayerItem: ExoPlayerItem) {
                            exoPlayerItems.add(exoPlayerItem)
                        }
                    }) { id, username, followButton ->
                }

            withContext(Dispatchers.Main) {
                viewPager.adapter = shortsAdapter
                viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL

                if (shortsViewModel.isResuming && shortsViewModel.mutableShortsList.isEmpty()) {
                    loadMoreShortsPage1(1)
                } else if (shortsViewModel.isResuming) {

                    lifecycleScope.launch {
                        Log.d(
                            "Resume",
                            "onCreateView: shorts view model size ${shortsViewModel.followList.size}"
                        )

                        shortsAdapter.addData(shortsViewModel.mutableShortsList)
                        shortsAdapter.addIsFollowingData(shortsViewModel.followList)
                    }

                    currentPosition = shortsViewModel.shortIndex

                    if(feedShortsBusinessId != "default_value" ) {
                        loadMoreShortsByFeedShortsBusinessId(feedShortsBusinessId)

                        val handler = Handler(Looper.getMainLooper())
                        handler.postDelayed({
                            viewPager.setCurrentItem(shortsViewModel.mutableShortsList.size, false)
                            playVideoAtPosition(shortsViewModel.mutableShortsList.size)
                        }, 200)

                        for(i in shortsViewModel.mutableShortsList){
                            Log.d("feedShortsBusinessId", "feedShortsBusinessId(i): ${i.feedShortsBusinessId}")
                        }
                    } else {
                        viewPager.setCurrentItem(currentPosition, false)
                        playVideoAtPosition(currentPosition)
                    }
                }


                viewPager.registerOnPageChangeCallback(object :
                    ViewPager2.OnPageChangeCallback() {

                    override fun onPageSelected(position: Int) {
                        shortsViewModel.shortIndex = position

                        // Stop current video immediately
                        exoPlayer?.let { player ->
                            player.stop()
                            player.clearMediaItems()
                        }

                        // Play next video immediately
                        playVideoAtPosition(position)
                    }

                    override fun onPageScrolled(
                        position: Int,
                        positionOffset: Float,
                        positionOffsetPixels: Int
                    ) {
                        super.onPageScrolled(position, positionOffset, positionOffsetPixels)

                        if (position > shortsViewModel.lastPosition) {
                            EventBus.getDefault().post(HideBottomNav())
                            EventBus.getDefault().post(HideFeedFloatingActionButton())
                        } else if (position < shortsViewModel.lastPosition) {
                            EventBus.getDefault().post(ShowFeedFloatingActionButton(false))
                            EventBus.getDefault().post(ShowBottomNav(false))
                        }

                        if (position > shortsViewModel.lastPosition) {
                            loadMoreVideosIfNeeded(position)
                        }

                        shortsViewModel.lastPosition = position

                        if (positionOffset > 0.5) {
                            currentPosition = position + 1
                        } else if (positionOffset < -0.5) {
                            currentPosition = position - 1
                        } else {
                            currentPosition = position
                        }
                    }

                    override fun onPageScrollStateChanged(state: Int) {
                        super.onPageScrollStateChanged(state)
                        Log.d("onPageScrollStateChanged", "onPageScrollStateChanged: state $state")

                        if (state == ViewPager.SCROLL_STATE_IDLE) {
                            Log.d("onPageScrollStateChanged", "onPageScrollStateChanged: state $state")
                            playVideoAtPosition(currentPosition)
                            backPressCount = 0
                        }
                    }
                })
            }
        }

        // Find FloatingActionButton
        fabAction = view.findViewById(R.id.fabAction)

        // Set up FloatingActionButton click listener
        fabAction.setOnClickListener {
            getContentLauncher?.launch(PICK_VIDEO_REQUEST)
        }

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if(feedOnBackPressedData) {
                    Log.d("handleOnBackPressed", "handleOnBackPressed: feedPostPosition $feedPostPosition")
                    EventBus.getDefault().post(GoToFeedFragment(feedPostPosition))
                } else {
                    if (backPressCount == 0) {
                        EventBus.getDefault().post(ShowBottomNav(false))
                        backPressCount++
                        Log.d("handleOnBackPressed", "handleOnBackPressed: 1 - display bottom nav ")
                    }
                    else if (backPressCount == 1) {
                        backPressCount++
                        Log.d(
                            "handleOnBackPressed",
                            "handleOnBackPressed: current position $currentPosition"
                        )
                        shortsViewModel.lastPosition = currentPosition + 2
                        currentPosition += 1
                        Log.d(
                            "handleOnBackPressed",
                            "handleOnBackPressed: current position $currentPosition"
                        )
                        Log.d(
                            "handleOnBackPressed",
                            "handleOnBackPressed: last/prev position ${shortsViewModel.lastPosition}"
                        )
                        EventBus.getDefault().post(ShowBottomNav(false))
                        viewPager.setCurrentItem(currentPosition, false)
                        playVideoAtPosition(currentPosition)

                        Log.d("handleOnBackPressed", "handleOnBackPressed: 2 - next short")
                    }
                    else if (backPressCount == 2) {
                        Log.d(
                            "handleOnBackPressed",
                            "handleOnBackPressed: last/prev position ${shortsViewModel.lastPosition}"
                        )
                        EventBus.getDefault().post(ShowBottomNav(false))
                        backPressCount++

                        if (backPressCount >= doubleBackPressThreshold) {
                            backPressCount = 0
                            requireActivity().finish()
                        }
                    }
                    else {
                        Log.d("handleOnBackPressed", "handleOnBackPressed: else")
                    }
                }
            }
        }

        // Add the callback to the onBackPressedDispatcher
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )

        // Observe data from ViewModel
        feesShortsSharedViewModel.data.observe(viewLifecycleOwner) { newData ->
            Log.d("feedShortsSharedViewModel", "feedShortsSharedViewModel: in shorts $newData")

            val followListItem: List<ShortsEntityFollowList> = listOf(
                ShortsEntityFollowList(
                    newData.userId, newData.isFollowing
                )
            )

            lifecycleScope.launch(Dispatchers.IO) {
                val uniqueFollowList = removeDuplicateFollowers(followListItem)

                Log.d(
                    "feedShortsSharedViewModel",
                    "feedShortsSharedViewModel: Inserted uniqueFollowList in shorts: $uniqueFollowList"
                )
                delay(100)
                followShortsViewModel.insertFollowListItems(uniqueFollowList)
                shortsViewModel.followList.add(
                    ShortsEntityFollowList(
                        newData.userId,
                        newData.isFollowing
                    )
                )
            }

            lifecycleScope.launch(Dispatchers.Main) {
                followShortsViewModel._followListItems.observe(viewLifecycleOwner) {
                    shortsAdapter.addIsFollowingData(it)
                    shortsAdapter.notifyDataSetChanged()
                    Log.d(
                        "feedShortsSharedViewModel",
                        "feedShortsSharedViewModel: size: ${it.size} in shorts: $it "
                    )
                }
            }
        }

        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    private suspend fun loadMoreShorts(currentPage: Int) {
        try {
            val response = retrofitIns.apiService.getShorts(currentPage.toString())

            if (response.isSuccessful) {
                val responseBody = response.body()
                Log.d(
                    "AllShorts3",
                    "Shorts List in page $currentPage ${responseBody?.data!!.posts}"
                )
                Log.d(
                    "AllShorts3",
                    "loadMoreShorts: followItem:  ${responseBody.data.followList}"
                )

                // CHANGE THIS - Remove var and reassign properly
                var shortsEntity = serverResponseToEntity(responseBody.data.posts.posts)

                // ADD THIS FILTERING LOGIC - Fixed version
                if (isUserFiltered && filterUserId != null) {
                    shortsEntity = shortsEntity.filter { entity ->
                        entity.author.account._id == filterUserId
                    }
                    Log.d("AllShorts3", "Filtered shorts for user $filterUsername: ${shortsEntity.size} videos")
                }

                val followListItem =
                    responseBody.data.followList.let { serverResponseToFollowEntity(it) }

                // Rest of the code remains the same...
                lifecycleScope.launch(Dispatchers.IO) {

                    val uniqueFollowList = removeDuplicateFollowers(followListItem)
                    Log.d(
                        "AllShorts3",
                        "getAllShort3: Inserted uniqueFollowList $uniqueFollowList"
                    )
                    followShortsViewModel.insertFollowListItems(uniqueFollowList)
                    if (uniqueFollowList.isEmpty()) {
                        Log.d("AllShorts3", "loadMoreShorts:uniqueFollowList is empty")

                        withContext(Dispatchers.Main) {
                            followShortsViewModel._followListItems.observe(viewLifecycleOwner) {
                                shortsViewModel.followList.addAll(followListItem)

                                shortsAdapter.addIsFollowingData(followListItem)

                            }
                        }
                    }

                    for (entity in shortsEntity) {
                        // Access the list of images for each entity
                        val images = entity.images

                        if (uniqueEntitiesSet.add(entity)) {
                            Log.d("SHORTS", "Processing entity: $entity")
                            // Add the unique entity to both the set and your ViewModel's list
                            shortsViewModel.videoShorts.add(entity)
                            continue
                        }
                        // Iterate through the list of images
                        for (image in images) {
                            // Access individual image properties or perform any desired actions
                            val imageUrl = image.url

                            shortsList.add(imageUrl)

                        }
                    }

                    startPreLoadingService()
                    withContext(Dispatchers.Main) {
                        if (shortsEntity.isNotEmpty()) {
                            Log.d("AllShorts3", "loadMoreShorts: shorts entity is not empty")

                            shortsViewModel.mutableShortsList.addAll(shortsEntity)
                            shortsAdapter.addData(shortsEntity)
                            shortsViewModel.followList.addAll(followListItem)

                            shortsAdapter.addIsFollowingData(followListItem)

                        } else {
                            Log.d("AllShorts3", "loadMoreShorts:shorts entity is empty")
                        }

                    }

                }


            } else {
                Log.d("AllShorts3", "Error: ${response.message()}")
                requireActivity().runOnUiThread {
                    showToast(response.message())
                }
            }

        } catch (e: HttpException) {
            Log.d("AllShorts", "Http Exception ${e.message}")
            requireActivity().runOnUiThread {
                showToast("Failed to connect try again...")
            }
        } catch (e: IOException) {
            Log.d("AllShorts", "IOException ${e.message}")
            requireActivity().runOnUiThread {
                showToast("Failed to connect try again...")
            }
        }
    }

    private suspend fun loadMoreShortsFromFeed(currentPage: Int) {
        try {
            val response = retrofitIns.apiService.getAllFeed(currentPage.toString())

            if (response.isSuccessful) {
                val responseBody = response.body()

                // CHANGE THIS - Make it var instead of val
                var videoPosts = responseBody?.data?.data?.posts?.filter { post ->
                    post.contentType == "mixed_files" && post.fileTypes.any {
                        // FIX: Add null safety check
                        it.fileType?.contains("video", ignoreCase = true) == true
                    }
                } ?: emptyList()

                // ADD THIS FILTERING LOGIC - Fixed version
                if (isUserFiltered && filterUserId != null) {
                    videoPosts = videoPosts.filter { post ->
                        post.author._id == filterUserId
                    }
                    Log.d("GetAllFeed", "Filtered feed videos for user $filterUsername: ${videoPosts.size} videos")
                }

                if (videoPosts.isNotEmpty()) {
                    val shortsEntity = convertFeedPostsToShortsEntity(videoPosts)

                    lifecycleScope.launch(Dispatchers.IO) {
                        val newFollowData = mutableListOf<ShortsEntityFollowList>()

                        for (entity in shortsEntity) {
                            val videos = entity.images

                            if (uniqueEntitiesSet.add(entity)) {
                                shortsViewModel.videoShorts.add(entity)

                                newFollowData.add(
                                    ShortsEntityFollowList(
                                        followersId = entity.author.account._id,
                                        isFollowing = false
                                    )
                                )

                                for (video in videos) {
                                    shortsList.add(video.url)
                                }
                            }
                        }

                        startPreLoadingService()

                        withContext(Dispatchers.Main) {
                            if (shortsEntity.isNotEmpty()) {
                                shortsViewModel.mutableShortsList.addAll(shortsEntity)
                                shortsAdapter.addData(shortsEntity)
                                shortsAdapter.addIsFollowingData(newFollowData)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("GetAllFeed", "Error loading feed: ${e.message}", e)
        }
    }

    private suspend fun loadMoreShortsPage1(currentPage: Int) {
        try {
            val response = retrofitIns.apiService.getShorts(currentPage.toString())

            if (response.isSuccessful) {
                val responseBody = response.body()
                Log.d(
                    "AllShorts3",
                    "Shorts List in page $currentPage ${responseBody?.data!!.posts}"
                )
                Log.d(
                    "AllShorts3",
                    "loadMoreShorts: followItem:  ${responseBody.data.followList}"
                )

                var shortsEntity = serverResponseToEntity(responseBody.data.posts.posts)

                // ADD THIS FILTERING LOGIC
                if (isUserFiltered && filterUserId != null) {
                    shortsEntity = shortsEntity.filter { entity ->
                        entity.author.account._id == filterUserId
                    }
                    Log.d("AllShorts3", "Filtered page 1 shorts for user $filterUsername: ${shortsEntity.size} videos")
                }

                val followListItem =
                    responseBody.data.followList.let { serverResponseToFollowEntity(it) }


                // Now, insert yourEntity into the Room database
                lifecycleScope.launch(Dispatchers.IO) {

                    val uniqueFollowList = removeDuplicateFollowers(followListItem)
                    Log.d(
                        "AllShorts3",
                        "getAllShort3: Inserted uniqueFollowList $uniqueFollowList"
                    )
                    followShortsViewModel.insertFollowListItems(uniqueFollowList)
                    if (uniqueFollowList.isEmpty()) {
                        Log.d("AllShorts3", "loadMoreShorts:uniqueFollowList is empty")

                        withContext(Dispatchers.Main) {
                            followShortsViewModel._followListItems.observe(viewLifecycleOwner) {
                                shortsViewModel.followList.addAll(it)

                                shortsAdapter.addIsFollowingData(it)

                            }
                        }
                    }


                    for (entity in shortsEntity) {

                        // Access the list of images for each entity
                        val images = entity.images

                        Log.d("ShortsData", "short: $entity")


                        // Add the unique entity to both the set and your ViewModel's list
                        shortsViewModel.videoShorts.add(entity)


                        for (image in images) {
                            // Access individual image properties or perform any desired actions
                            val imageUrl = image.url

                            shortsList.add(imageUrl)
                        }
                    }

                    startPreLoadingService()
                    withContext(Dispatchers.Main) {
                        if (shortsEntity.isNotEmpty()) {
                            Log.d("AllShorts3", "loadMoreShorts: shorts entity is not empty")


                            shortsViewModel.mutableShortsList.addAll(shortsEntity)
                            shortsViewModel.followList.addAll(followListItem)
                            shortsAdapter.addData(shortsEntity)

                            shortsAdapter.addIsFollowingData(followListItem)

                        } else {
                            Log.d("AllShorts3", "loadMoreShorts:shorts entity is empty")
                        }

                    }

                }


            } else {
                Log.d("ErrorInShortsFragment", "Error message: ${response.message()}")
                Log.d("ErrorInShortsFragment", "Error body: ${response.body()}")
                Log.d("ErrorInShortsFragment", "Error error body: ${response.errorBody()}")
                Log.d("ErrorInShortsFragment", "Error response: $response")
                Log.d("ErrorInShortsFragment", "Error response code: ${response.code()}")
                Log.d("ErrorInShortsFragment", "Error response headers: ${response.headers()}")
                Log.d("ErrorInShortsFragment", "Error response raw: ${response.raw()}")

                requireActivity().runOnUiThread {
                    showToast(response.message())
                }
            }

        } catch (e: HttpException) {
            Log.d("AllShorts", "Http Exception ${e.message}")
            requireActivity().runOnUiThread {
                showToast("Failed to connect try again...")
            }
        } catch (e: IOException) {
            Log.d("AllShorts", "IOException ${e.message}")
            requireActivity().runOnUiThread {
                showToast("Failed to connect try again...")
            }
        }
    }

    private fun playVideoAtPosition(position: Int) {
        val videoShorts = shortsViewModel.videoShorts

        if (position < 0 || position >= videoShorts.size) {
            Log.e("playVideoAtPosition", "Invalid position: $position, size: ${videoShorts.size}")
            return
        }

        val shortVideo = videoShorts[position]
        Log.d("playVideoAtPosition", "Playing video for: ${shortVideo.author.account.username}")

        // Load thumbnail for current position ONLY
        val currentHolder = shortsAdapter.getCurrentViewHolder()
        val thumbnailUrl = shortVideo.thumbnail.firstOrNull()?.thumbnailUrl
        currentHolder?.loadThumbnail(thumbnailUrl)

        val rawVideoUrl = shortVideo.images.firstOrNull()?.url

        if (rawVideoUrl.isNullOrEmpty()) {
            Log.e("playVideoAtPosition", "Video URL is null or empty at position $position")
            return
        }

        val finalVideoUrl = when {
            rawVideoUrl.startsWith("http://") || rawVideoUrl.startsWith("https://") -> {
                rawVideoUrl
            }
            rawVideoUrl.contains("mixed_files") || rawVideoUrl.contains("temp") -> {
                val serverBaseUrl = "http://192.168.1.103:8080/feed_mixed_files/"
                serverBaseUrl + rawVideoUrl.trimStart('/')
            }
            else -> {
                val serverBaseUrl = "http://192.168.1.103:8080/"
                if (rawVideoUrl.startsWith("/")) {
                    serverBaseUrl + rawVideoUrl.trimStart('/')
                } else {
                    serverBaseUrl + rawVideoUrl
                }
            }
        }

        Log.d("playVideoAtPosition", "Final video URL: $finalVideoUrl")

        // Play immediately
        prepareAndPlayVideoImmediately(finalVideoUrl, position)
    }

    private fun prepareAndPlayVideoImmediately(videoUrl: String, position: Int) {
        try {
            val videoUri = Uri.parse(videoUrl)
            Log.d("prepareAndPlayVideo", "Preparing video URI: $videoUri")

            // Ensure surface is ready
            val currentHolder = shortsAdapter.getCurrentViewHolder()
            currentHolder?.getSurface()?.let { playerView ->
                playerView.visibility = View.VISIBLE
                playerView.player = exoPlayer
            }

            val mediaItem = MediaItem.Builder()
                .setUri(videoUri)
                .apply {
                    val detectedMimeType = getMimeTypeFromUrl(videoUrl)
                    if (detectedMimeType != MimeTypes.VIDEO_UNKNOWN) {
                        setMimeType(detectedMimeType)
                    }
                }
                .build()

            // Remove old listener
            currentPlayerListener?.let { oldListener ->
                exoPlayer?.removeListener(oldListener)
            }

            currentPlayerListener = createPlayerListener(position)

            exoPlayer?.let { player ->
                // Stop and clear previous media
                player.stop()
                player.clearMediaItems()

                // Add new listener
                player.addListener(currentPlayerListener!!)
                player.repeatMode = Player.REPEAT_MODE_ONE
                player.playWhenReady = true // Set to play immediately

                // Set media and prepare
                player.setMediaItem(mediaItem, true) // Reset position
                player.prepare()

                Log.d("prepareAndPlayVideo", "Video preparation started for position: $position")
            }

        } catch (e: Exception) {
            Log.e("prepareAndPlayVideo", "Error in prepareAndPlayVideo", e)
            handlePlaybackError(position)
        }
    }

    private fun createPlayerListener(position: Int): Player.Listener {
        return object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)

                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        Log.d("PlayerState", "Buffering video at position: $position")
                    }
                    Player.STATE_READY -> {
                        Log.d("PlayerState", "Video ready at position: $position")

                        exoPlayer?.let { player ->
                            if (player.duration != C.TIME_UNSET) {
                                shortSeekBar.max = player.duration.toInt()
                                Log.d("PlayerState", "Duration set: ${player.duration}")
                            }
                            // playWhenReady already handles playback
                        }
                    }
                    Player.STATE_ENDED -> {
                        Log.d("PlayerState", "Video ended at position: $position")
                    }
                    Player.STATE_IDLE -> {
                        Log.d("PlayerState", "Player idle at position: $position")
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)

                Log.e("PlayerError", "Playback error at position $position", error)
                Log.e("PlayerError", "Error code: ${error.errorCode}")

                when (error.errorCode) {
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> {
                        Log.e("PlayerError", "Network error: ${error.message}")
                    }
                    PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED,
                    PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED -> {
                        Log.e("PlayerError", "Format error: ${error.message}")
                    }
                    else -> {
                        Log.e("PlayerError", "Unknown error: ${error.message}")
                    }
                }

                handlePlaybackError(position)
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                super.onPositionDiscontinuity(oldPosition, newPosition, reason)
                updateSeekBar()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                Log.d("PlayerState", "Is playing: $isPlaying at position: $position")
            }
        }
    }
    
    @SuppressLint("NotifyDataSetChanged")
    private suspend fun loadMoreShortsByFeedShortsBusinessId(feedShortsBusinessId: String) {
        try {
            val response = retrofitIns.apiService.getAllShortsByFeedShortBusinessId(feedShortsBusinessId)

            if (response.isSuccessful) {
                val responseBody = response.body()
                Log.d(
                    "loadMoreShortsByFeedShortsBusinessId",
                    "Shorts List in page $feedShortsBusinessId ${responseBody?.data!!.posts}"
                )
                Log.d(
                    "loadMoreShortsByFeedShortsBusinessId",
                    "loadMoreShortsByFeedShortsBusinessId: followItem:  ${responseBody.data.followList}"
                )


                val shortsEntity = serverResponseToEntity(responseBody.data.posts.posts)

                val followListItem =
                    responseBody.data.followList.let { serverResponseToFollowEntity(it) }

                lifecycleScope.launch(Dispatchers.IO) {

                    val uniqueFollowList = removeDuplicateFollowers(followListItem)
                    Log.d(
                        "loadMoreShortsByFeedShortsBusinessId",
                        "loadMoreShortsByFeedShortsBusinessId: Inserted uniqueFollowList $uniqueFollowList"
                    )
                    followShortsViewModel.insertFollowListItems(uniqueFollowList)
                    if (uniqueFollowList.isEmpty()) {
                        Log.d("loadMoreShortsByFeedShortsBusinessId", "loadMoreShorts:uniqueFollowList is empty")

                        withContext(Dispatchers.Main) {
                            followShortsViewModel._followListItems.observe(viewLifecycleOwner) {
                                shortsViewModel.followList.addAll(followListItem)

                                shortsAdapter.addIsFollowingData(followListItem)

                            }
                        }
                    }


                    for (entity in shortsEntity) {
                        // Access the list of images for each entity
                        val images = entity.images

                        if (uniqueEntitiesSet.add(entity) ) {

                            // Add the unique entity to both the set and your ViewModel's list
                            shortsViewModel.videoShorts.add(entity)

                        }else{
                            Log.d("DuplicateEntity", "Duplicate entity found: $entity")
                            shortsViewModel.videoShorts.add(entity)

                        }
                        // Iterate through the list of images
                        for (image in images) {
                            // Access individual image properties or perform any desired actions
                            val imageUrl = image.url
                            Log.d(SHORTS, "imageUrl - $imageUrl")
                            if (imageUrl !in shortsList) {
                                shortsList.add(imageUrl)
                            }
                            shortsList.add(imageUrl)

                        }
                    }

                    startPreLoadingService()
                    withContext(Dispatchers.Main) {
                        if (shortsEntity.isNotEmpty()) {
                            Log.d("loadMoreShortsByFeedShortsBusinessId", "loadMoreShorts: shorts entity is not empty")

                            shortsViewModel.mutableShortsList.addAll(shortsEntity)
                            shortsAdapter.addData(shortsEntity)
                            shortsViewModel.followList.addAll(followListItem)

                            shortsAdapter.addIsFollowingData(followListItem)

                        } else {
                            Log.d("loadMoreShortsByFeedShortsBusinessId", "loadMoreShorts:shorts entity is empty")
                        }

                    }

                }


            } else {
                Log.d("loadMoreShortsByFeedShortsBusinessId", "Error: ${response.message()}")
                requireActivity().runOnUiThread {
                    showToast(response.message())
                }
            }

        } catch (e: HttpException) {
            Log.d("AllShorts", "Http Exception ${e.message}")
            requireActivity().runOnUiThread {
                showToast("Failed to connect try again...")
            }
        } catch (e: IOException) {
            Log.d("AllShorts", "IOException ${e.message}")
            requireActivity().runOnUiThread {
                showToast("Failed to connect try again...")
            }
        }
    }

    private fun markVideoAsFailedToLoad(position: Int) {

        Log.d("ErrorRecovery", "Marking video at position $position as failed to load")
        // Implementation depends on your data structure
    }

    fun loadMoreVideosIfNeeded(position: Int) {

        if (position >= 5 && (position - 5) % 5 == 0) {

            val loadMoreValue = 2 + (position - 5) / 5

            loadMoreVideos(loadMoreValue)
        }
    }

    private fun loadMoreVideos(pageNumber: Int) {
        // Call the function that makes a request to the server for more videos
        lifecycleScope.launch(Dispatchers.IO) {
            // Your code to fetch more videos goes here
            Log.d("TAG", "Call for more videos")

            delay(50)
            loadMoreShorts(pageNumber)
            loadMoreShortsFromFeed(pageNumber)
            shortsViewModel.pageNumber = pageNumber
            lifecycleScope.launch(Dispatchers.Main) {

            }
        }
    }

    private fun setupVideoPlaybackInShots(videoUrl: String) {
        // Implement video playback logic specific to ShotsFragment
        Log.d("ShotsFragment", "Setting up video playback for: $videoUrl")
    }


    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
            Log.d("EventBus", "ShotsFragment registered")
        }
    }

    override fun onStop() {
        super.onStop()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
            Log.d("EventBus", "ShotsFragment unregistered")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Only pause, don't release
        exoPlayer?.pause()

        lifecycleScope.launch {
            shortsViewModel.isResuming = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        try {
            exoPlayer?.apply {
                removeListener(playbackStateListener)
                currentPlayerListener?.let { removeListener(it) }
                stop()
                clearMediaItems()
                release()
            }
            exoPlayer = null
        } catch (e: Exception) {
            Log.e("ShotsFragment", "Error destroying player", e)
        }

        if (exoPlayerItems.isNotEmpty()) {
            for (item in exoPlayerItems) {
                val player = item.exoPlayer
                player.stop()
                player.clearMediaItems()
            }
            exoPlayerItems.clear()
        }

        lifecycleScope.launch {
            shortsViewModel.isResuming = true
        }
    }

    override fun onPause() {
        super.onPause()
        exoPlayer!!.pause()
        val index = exoPlayerItems.indexOfFirst { it.position == viewPager.currentItem }
        if (index != -1) {
            val player = exoPlayerItems[index].exoPlayer
            player.pause()

            player.playWhenReady = false
            player.seekTo(0)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.hideAppBar()
        val vNLayout = activity?.findViewById<ConstraintLayout>(R.id.VNLayout)
        if (vNLayout?.visibility == View.VISIBLE) {
            pauseVideo()
        } else {
            exoPlayer!!.play()
            val index = exoPlayerItems.indexOfFirst { it.position == viewPager.currentItem }
            if (index != -1) {
                val player = exoPlayerItems[index].exoPlayer
                player.playWhenReady = true
                player.play()
                player.seekTo(0)
            }
        }
        updateStatusBar()

    }

    private fun convertFeedPostsToShortsEntity(
        feedPosts: List<com.uyscuti.social.network.api.response.posts.Post>
    ): List<ShortsEntity> {
        return feedPosts.mapNotNull { feedPost ->
            try {
                // Validate required fields exist
                if (feedPost.author == null || feedPost.author.account == null) {
                    Log.e("GetAllFeed", "Skipping post ${feedPost._id}: missing author data")
                    return@mapNotNull null
                }

                // Convert Feed Author to Shorts Author
                val shortsAuthor = com.uyscuti.social.network.api.response.getallshorts.Author(
                    __v = feedPost.author.__v,
                    _id = feedPost.author._id,
                    bio = feedPost.author.bio,
                    countryCode = feedPost.author.countryCode,
                    coverImage = com.uyscuti.social.network.api.response.getallshorts.CoverImage(
                        _id = feedPost.author.coverImage._id,
                        localPath = feedPost.author.coverImage.localPath,
                        url = feedPost.author.coverImage.url
                    ),
                    createdAt = feedPost.author.createdAt,
                    dob = feedPost.author.dob,
                    firstName = feedPost.author.firstName,
                    lastName = feedPost.author.lastName,
                    location = feedPost.author.location,
                    owner = feedPost.author.owner,
                    phoneNumber = feedPost.author.phoneNumber,
                    updatedAt = feedPost.author.updatedAt,
                    account = com.uyscuti.social.network.api.response.getallshorts.Account(
                        _id = feedPost.author.account._id,
                        avatar = com.uyscuti.social.network.api.response.getallshorts.Avatar(
                            _id = feedPost.author.account.avatar._id,
                            localPath = feedPost.author.account.avatar.localPath,
                            url = feedPost.author.account.avatar.url
                        ),
                        email = feedPost.author.account.email,
                        username = feedPost.author.account.username
                    )
                )

                // FIX: Add null safety for fileType
                val shortsImages = feedPost.files.filter { file ->
                    feedPost.fileTypes.any {
                        it.fileId == file.fileId && it.fileType?.contains("video", ignoreCase = true) == true
                    }
                }.map { file ->
                    com.uyscuti.social.network.api.response.getallshorts.Image(
                        _id = file._id,
                        localPath = file.localPath,
                        url = file.url
                    )
                }

                if (shortsImages.isEmpty()) {
                    Log.d("GetAllFeed", "Skipping post ${feedPost._id}: no video files found")
                    return@mapNotNull null
                }

                // FIX: Add null safety for fileType in thumbnails
                val shortsThumbnails = feedPost.thumbnail.mapNotNull { thumb ->
                    try {
                        val isVideoThumbnail = feedPost.fileTypes.any {
                            it.fileId == thumb.fileId && it.fileType?.contains("video", ignoreCase = true) == true
                        }

                        if (isVideoThumbnail) {
                            com.uyscuti.social.network.api.response.getallshorts.Thumbnail(
                                _id = thumb._id,
                                thumbnailLocalPath = thumb.thumbnailLocalPath,
                                thumbnailUrl = thumb.thumbnailUrl
                            )
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        Log.e("GetAllFeed", "Error converting thumbnail: ${e.message}")
                        null
                    }
                }

                // Create ShortsEntity
                ShortsEntity(
                    __v = feedPost.__v,
                    _id = feedPost._id,
                    author = shortsAuthor,
                    comments = feedPost.comments,
                    content = feedPost.content,
                    createdAt = feedPost.createdAt,
                    images = shortsImages,
                    thumbnail = shortsThumbnails,
                    isBookmarked = feedPost.isBookmarked,
                    isLiked = feedPost.isLiked,
                    likes = feedPost.likes,
                    tags = feedPost.tags.filterIsInstance<String>(),
                    updatedAt = feedPost.updatedAt,
                    feedShortsBusinessId = feedPost.feedShortsBusinessId
                )
            } catch (e: Exception) {
                Log.e("GetAllFeed", "Error converting feed post ${feedPost._id} to shorts entity: ${e.message}", e)
                e.printStackTrace()
                null
            }
        }
    }

    private fun serverResponseToEntity(serverResponse: List<Post>): List<ShortsEntity> {
        return serverResponse.map { serverResponseItem ->
            ShortsEntity(
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
                thumbnail = serverResponseItem.thumbnail,
                feedShortsBusinessId = serverResponseItem.feedShortsBusinessId

            )
        }
    }

    private fun serverResponseToFollowEntity(serverResponse: List<FollowListItem>): List<ShortsEntityFollowList> {
        return serverResponse.map { serverResponse ->
            ShortsEntityFollowList(
                followersId = serverResponse.followersId,
                isFollowing = serverResponse.isFollowing
            )
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun successEvent(event: UploadSuccessful) {
        if (event.success) {
            progressBarLayout.visibility = View.GONE
            progressViewModel.totalProgress = 0
        } else {
            progressBarLayout.visibility = View.VISIBLE
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun shortAdapterNotifyDatasetChanged(event: ShortAdapterNotifyDatasetChanged) {
        val TAG = "shortAdapterNotifyDatasetChanged"
        Log.d(
            TAG,
            "shortAdapterNotifyDatasetChanged: in shorts adapter notify adapter: seh data set changed"
        )
        shortsAdapter.notifyDataSetChanged()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun pausePlayEvent(event: PausePlayEvent) {
        Log.d("pausePlayEvent", "pausePlayEvent ${count + 1}")
        if (exoPlayer?.isPlaying == true) {
            pauseVideo()
        } else {
            playVideo()
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun pauseShort(event: PauseShort) {
        if (event.pause) {
            pauseVideo()
        }
    }



    @SuppressLint("SetTextI18n")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handleFollowButtonClick(event: ShortsFollowButtonClicked) {
        val tag = "handleFollowButtonClick"
        Log.d(tag, "Follow state changed to: ${event.followUnFollowEntity.isFollowing}")

        val userId = event.followUnFollowEntity.userId
        val isFollowing = event.followUnFollowEntity.isFollowing

        val connectivityManager =
            requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        val isConnected = networkInfo != null && networkInfo.isConnected

        // Update the adapter's data source silently (without rebinding)
        shortsAdapter.updateFollowState(userId, isFollowing)

        // Update the Room database immediately
        followViewModel.insertOrUpdateFollow(event.followUnFollowEntity)

        if (isConnected) {
            Log.d(tag, "Internet connected, making API call")

            // Make the API call
            followUnFollowViewModel.followUnFollow(userId)

            // Clean up database after API call succeeds
            followUnFollowViewModel.viewModelScope.launch {
                delay(1000) // Wait for API call to complete
                val isDeleted = followViewModel.deleteFollowById(userId)
                if (isDeleted) {
                    Log.d(tag, "Follow record deleted successfully from local DB.")
                } else {
                    Log.d(tag, "Failed to delete follow record from local DB.")
                }
            }
        } else {
            Log.d(tag, "No internet connection, saved locally only")
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onProgressEvent(event: ProgressEvent) {
        Log.d("EventBus", "✓ onProgressEvent CALLED - eventId: ${event.eventId}, progress: ${event.progress}")

        val progressSet = eventProgressSets.getOrPut(event.eventId) { HashSet() }

        if (progressSet.add(event.progress)) {
            progressViewModel.totalProgress += 1
            var overallProgress = (progressViewModel.totalProgress.toDouble() / TOTAL_PROGRESS) * 100

            Log.d("Progress", "Overall Progress: $overallProgress - total progress ${progressViewModel.totalProgress}")

            progressBarLayout.visibility = View.VISIBLE
            progressBar.progress = overallProgress.toInt()

            // Fix: Safe call for animation
            wifiAnimation?.start()

            cancelShortsUpload.setOnClickListener {
                Log.d("Cancel", "Cancel upload clicked")
                EventBus.getDefault().post(CancelShortsUpload(true))
                progressBarLayout.visibility = View.GONE
                wifiAnimation?.stop()
                eventProgressSets.clear()
                progressSet.clear()
                progressViewModel.totalProgress = 0
                overallProgress = 0.0
            }

            if (overallProgress.toInt() >= 100) {
                Log.d("overallProgress", "onProgressEvent: $overallProgress")
                progressViewModel.totalProgress = 0
                overallProgress = 0.0
                wifiAnimation?.stop()
                eventProgressSets.clear()
                progressSet.clear()
                progressBarLayout.visibility = View.GONE
            }
        } else {
            Log.d("Progress", "Event ${event.eventId} - Duplicate progress value: ${event.progress}")
        }
    }

    private fun updateSeekBar() {
        exoPlayer?.let { player ->
            if (!isUserSeeking) {
                val currentPosition = player.currentPosition.toInt()
                shortSeekBar.progress = currentPosition
            }
        }
    }


    private fun getMimeTypeFromUrl(url: String): String {
        val lowerUrl = url.lowercase()
        return when {
            lowerUrl.contains(".mp4") -> MimeTypes.VIDEO_MP4
            lowerUrl.contains(".mov") -> "video/quicktime"
            lowerUrl.contains(".avi") -> "video/x-msvideo"
            lowerUrl.contains(".mkv") -> "video/x-matroska"
            lowerUrl.contains(".webm") -> "video/webm"
            lowerUrl.contains(".3gp") -> "video/3gpp"
            else -> MimeTypes.VIDEO_MP4 // Default fallback
        }
    }

    @OptIn(UnstableApi::class)
    private fun createEnhancedMediaSource(mediaItem: MediaItem, videoUrl: String): MediaSource {
        return try {
            // Create a more robust data source factory
            val dataSourceFactory = createRobustDataSourceFactory()


            ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mediaItem)
        } catch (e: Exception) {
            Log.e("createEnhancedMediaSource", "Error creating enhanced media source", e)
            // Ultimate fallback - basic progressive source with default factory
            val defaultFactory = DefaultDataSource.Factory(requireContext())
            ProgressiveMediaSource.Factory(defaultFactory)
                .createMediaSource(mediaItem)
        }
    }

    @OptIn(UnstableApi::class)
    private fun createRobustDataSourceFactory(): DataSource.Factory {
        return try {
            // Create cache data source factory with fallback
            val cacheFactory = if (::cacheDataSourceFactory.isInitialized) {
                cacheDataSourceFactory
            } else {
                Log.w("createRobustDataSourceFactory", "Cache factory not initialized, using default")
                DefaultDataSource.Factory(requireContext())
            }

            // Wrap with retry mechanism
            cacheFactory
        } catch (e: Exception) {
            Log.e("createRobustDataSourceFactory", "Error creating robust data source factory", e)
            DefaultDataSource.Factory(requireContext())
        }
    }

    private fun handlePlaybackError(position: Int) {
        lifecycleScope.launch {
            delay(1000) // Wait before trying next action

            // Try to skip to next video
            val nextPosition = if (position + 1 < shortsViewModel.videoShorts.size) {
                position + 1
            } else {
                0 // Loop back to first video
            }

            // Also update the current position in your view model or adapter
            // to reflect that this video failed to play
            markVideoAsFailedToLoad(position)

            Log.d("ErrorRecovery", "Trying to play video at position: $nextPosition")
            playVideoAtPosition(nextPosition)
        }
    }

    fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun openUploadShortsActivity(videoUri: Uri) {
        val intent = Intent(requireContext(), UploadShortsActivity::class.java).apply {
            putExtra(UploadShortsActivity.EXTRA_VIDEO_URI, videoUri)
        }
        startActivityForResult(intent, REQUEST_UPLOAD_SHORTS_ACTIVITY)

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_UPLOAD_SHORTS_ACTIVITY) {
            if (resultCode == Activity.RESULT_OK) {


            } else {

            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun informAdapter(event: InformAdapter) {

        lifecycleScope.launch(Dispatchers.Main) {
            followShortsViewModel._followListItems.observe(viewLifecycleOwner) {
                shortsAdapter.addIsFollowingData(it)
                shortsAdapter.notifyDataSetChanged()

            }
        }


    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun feedFavoriteFollowUpdate(event: FeedFavoriteFollowUpdate) {
        val tag = "feedFavoriteFollowUpdate"
        Log.d(tag, "feedFavoriteFollowUpdate: from favorites or all feed in short fragment")
        val followListItem: List<ShortsEntityFollowList> = listOf(
            ShortsEntityFollowList(
                event.userId, event.isFollowing
            )
        )

        lifecycleScope.launch(Dispatchers.IO) {

            val uniqueFollowList = removeDuplicateFollowers(followListItem)

            Log.d(
                "followButtonClicked",
                "followButtonClicked: Inserted uniqueFollowList $uniqueFollowList"
            )
            delay(100)
            followShortsViewModel.insertFollowListItems(uniqueFollowList)

        }

        lifecycleScope.launch(Dispatchers.Main) {

            followShortsViewModel._followListItems.observe(viewLifecycleOwner) {
                shortsAdapter.addIsFollowingData(it)
                shortsAdapter.notifyDataSetChanged()
                Log.d("followButtonClicked", "followButtonClicked:$it ")
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun followButtonClicked(event: ShortsFollowButtonClicked) {
        Log.d("followButtonClicked", "followButtonClicked: ${event.followUnFollowEntity}")


        followUnFollowViewModel.followUnFollow(event.followUnFollowEntity.userId)

        val followListItem: List<ShortsEntityFollowList> = listOf(
            ShortsEntityFollowList(
                event.followUnFollowEntity.userId, event.followUnFollowEntity.isFollowing
            )
        )

        feesShortsSharedViewModel.setData(
            FollowUnFollowEntity(
                event.followUnFollowEntity.userId,
                event.followUnFollowEntity.isFollowing
            )
        )

        lifecycleScope.launch(Dispatchers.IO) {

            val uniqueFollowList = removeDuplicateFollowers(followListItem)

            Log.d(
                "followButtonClicked",
                "followButtonClicked: Inserted uniqueFollowList $uniqueFollowList"
            )
            delay(100)
            followShortsViewModel.insertFollowListItems(uniqueFollowList)

        }

        lifecycleScope.launch(Dispatchers.Main) {

            followShortsViewModel._followListItems.observe(viewLifecycleOwner) {
                shortsAdapter.addIsFollowingData(it)
                shortsAdapter.notifyDataSetChanged()
                Log.d("followButtonClicked", "followButtonClicked:$it ")
            }
        }


    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLikeUnLikeClick(event: ShortsLikeUnLike) {
        Log.d("onLikeUnLikeClick", "onLikeUnLikeClick: ${event.isLiked}")

    }

    @Subscribe(threadMode = ThreadMode.MAIN)

    fun likeButtonClicked(event: ShortsLikeUnLikeButton) {


        var button = event.likeUnLikeButton
        var shortOwnerId = event.shortsEntity.author.account._id
        var postId = event.shortsEntity._id


        shortsViewModel.isLiked = event.shortsEntity.isLiked

        event.likeCount.text = event.shortsEntity.likes.toString()


        if (shortsViewModel.isLiked) {
            button.setImageResource(R.drawable.filled_favorite_like)

            Log.d(
                "likeButtonClicked",
                "likeButtonClicked: event is liked: ${event.shortsEntity.isLiked}"
            )

        } else {
            button.setImageResource(R.drawable.favorite_svgrepo_com)

            Log.d(
                "likeButtonClicked",
                "likeButtonClicked: event is liked: ${event.shortsEntity.isLiked}"
            )

        }

        button.setOnClickListener {

            handleLikeClick(postId, event.likeCount, button, event.shortsEntity)
        }

    }

    private fun handleLikeClick(
        postId: String,
        likeCount: TextView,
        btnLike: ImageButton,
        shortsEntity: ShortsEntity
    ) {

        Log.d("handleLikeClick", "handleLikeClick: before ${shortsViewModel.isLiked}")
        shortsViewModel.isLiked = !shortsViewModel.isLiked
        Log.d("handleLikeClick", "handleLikeClick: after ! ${shortsViewModel.isLiked}")
        EventBus.getDefault().post(ShortsLikeUnLike2(postId))

        if (!shortsEntity.isLiked) {


            shortsEntity.likes += 1
            likeCount.text = shortsEntity.likes.toString()

            btnLike.setImageResource(R.drawable.filled_favorite_like)
            YoYo.with(Techniques.Tada)
                .duration(700)
                .repeat(1)
                .playOn(btnLike)

            shortsEntity.isLiked = true


            val myShorts = userProfileShortsViewModel.mutableShortsList.find { it._id == postId }
            var myFavoriteShorts =
                userProfileShortsViewModel.mutableFavoriteShortsList.find { it._id == postId }

            if (myShorts != null) {
                Log.d("handleLikeClick", "handleLikeClick: short found id: ${myShorts._id}")
                myShorts.isLiked = true
                myShorts.likes += 1
            } else {
                Log.d("handleLikeClick", "handleLikeClick: short not found")
            }
            if (myFavoriteShorts != null) {
                Log.d("handleLikeClick", "handleLikeClick: short found id: ${myFavoriteShorts._id}")
                myFavoriteShorts.isLiked = true
                myFavoriteShorts.likes += 1
            } else {
                Log.d("handleLikeClick", "handleLikeClick: short not found")
            }
            shortsViewModel.isLiked = true
        } else {
            shortsEntity.likes -= 1
            likeCount.text = shortsEntity.likes.toString()
            var myShorts = userProfileShortsViewModel.mutableShortsList.find { it._id == postId }
            var myFavoriteShorts =
                userProfileShortsViewModel.mutableFavoriteShortsList.find { it._id == postId }

            if (myShorts != null) {
                Log.d("handleLikeClick", "handleLikeClick: short found id: ${myShorts._id}")
                myShorts.isLiked = false
                myShorts.likes -= 1
            } else {
                Log.d("handleLikeClick", "handleLikeClick: short not found")
            }
            if (myFavoriteShorts != null) {
                Log.d("handleLikeClick", "handleLikeClick: short found id: ${myFavoriteShorts._id}")
                myFavoriteShorts.isLiked = false
                myFavoriteShorts.likes -= 1
            } else {
                Log.d("handleLikeClick", "handleLikeClick: short not found")
            }
            btnLike.setImageResource(R.drawable.favorite_svgrepo_com)
            shortsEntity.isLiked = false

            shortsViewModel.isLiked = false
            YoYo.with(Techniques.Tada)
                .duration(700)
                .repeat(1)
                .playOn(btnLike)
        }
    }

    private suspend fun likeUnLikeShort(shortOwnerId: String) {
        try {
            val response = retrofitIns.apiService.likeUnLikeShort(shortOwnerId)


            if (response.isSuccessful) {
                val responseBody = response.body()
                Log.d(
                    "likeUnLikeShort",
                    "likeUnLikeShort ${responseBody?.data!!.isLiked}"
                )
            } else {
                Log.d("likeUnLikeShort", "Error: ${response.message()}")
                requireActivity().runOnUiThread {
                    showToast(response.message())
                }
            }

        } catch (e: HttpException) {
            Log.d("likeUnLikeShort", "Http Exception ${e.message}")
            requireActivity().runOnUiThread {
                showToast("Failed to connect try again...")
            }
        } catch (e: IOException) {
            Log.d("likeUnLikeShort", "IOException ${e.message}")
            requireActivity().runOnUiThread {
                showToast("Failed to connect try again...")
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun favoriteButtonClicked(event: ShortsBookmarkButton) {


        val button = event.favoriteButton
        var shortOwnerId = event.shortsEntity.author.account._id
        val postId = event.shortsEntity._id


        shortsViewModel.isFavorite = event.shortsEntity.isBookmarked

        if (shortsViewModel.isFavorite) {
            button.setImageResource(R.drawable.filled_favorite)

        } else {
            button.setImageResource(R.drawable.favorite_svgrepo_com__1_)
        }

        button.setOnClickListener {
            handleFavoriteClick(postId, button, event.shortsEntity)
        }

    }

    private fun handleFavoriteClick(postId: String, button: ImageView, shortsEntity: ShortsEntity) {

        val TAG = "handleFavoriteClick"
        shortsViewModel.isFavorite = !shortsViewModel.isFavorite
        EventBus.getDefault().post(ShortsFavoriteUnFavorite(postId))

        if (!shortsEntity.isBookmarked) {

            button.setImageResource(R.drawable.filled_favorite)
            YoYo.with(Techniques.Tada)
                .duration(700)
                .repeat(1)
                .playOn(button)

            shortsEntity.isBookmarked = true


            shortsViewModel.isFavorite = true

            val myShorts = userProfileShortsViewModel.mutableShortsList.find { it._id == postId }
            val myFavoriteShorts =
                userProfileShortsViewModel.mutableFavoriteShortsList.find { it._id == postId }

            if (myShorts != null) {
                Log.d("handleLikeClick", "handleLikeClick: short found id: ${myShorts._id}")
                myShorts.isBookmarked = true
            } else {
                Log.d("handleLikeClick", "handleLikeClick: short not found")
            }
            if (myFavoriteShorts != null) {
                myFavoriteShorts.isBookmarked = true
            }
            val convertedShort = shortsEntityToUserShortsEntity(shortsEntity)
            userProfileShortsViewModel.mutableFavoriteShortsList.add(0, convertedShort)
        } else {
            button.setImageResource(R.drawable.favorite_svgrepo_com__1_)
            shortsEntity.isBookmarked = false

            shortsViewModel.isFavorite = false

            val myShorts = userProfileShortsViewModel.mutableShortsList.find { it._id == postId }
            val myFavoriteShorts =
                userProfileShortsViewModel.mutableFavoriteShortsList.find { it._id == postId }

            if (myShorts != null) {
                Log.d("handleLikeClick", "handleLikeClick: short found id: ${myShorts._id}")
                myShorts.isBookmarked = false
            } else {
                Log.d("handleLikeClick", "handleLikeClick: short not found")
            }

            if (myFavoriteShorts != null) {
                Log.d("handleLikeClick", "handleLikeClick: short found id: ${myFavoriteShorts._id}")
                myFavoriteShorts.isBookmarked = false

                Log.d(
                    TAG,
                    "handleFavoriteClick: mutableFavoriteShortsList size before ${userProfileShortsViewModel.mutableFavoriteShortsList.size}"
                )

                // Remove the item if it exists in the list
                userProfileShortsViewModel.mutableFavoriteShortsList.removeIf { it._id == postId }

                Log.d(
                    TAG,
                    "handleFavoriteClick: mutableFavoriteShortsList size after ${userProfileShortsViewModel.mutableFavoriteShortsList.size}"
                )

            } else {
                Log.d("handleLikeClick", "handleLikeClick: short not found")
            }

            YoYo.with(Techniques.Tada)
                .duration(700)
                .repeat(1)
                .playOn(button)
        }
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
            thumbnail = serverResponseItem.thumbnail,
            // map other properties...
        )
    }

    private suspend fun favoriteShort(postId: String) {
        try {
            val response = retrofitIns.apiService.favoriteShort(postId)

            if (response.isSuccessful) {
                val responseBody = response.body()
                Log.d(
                    "favoriteButtonClicked",
                    "favoriteButtonClicked ${responseBody?.data!!.isBookmarked}"
                )
            } else {
                Log.d("favoriteButtonClicked", "Error: ${response.message()}")
                requireActivity().runOnUiThread {
                    showToast(response.message())
                }
            }

        } catch (e: HttpException) {
            Log.d("favoriteButtonClicked", "Http Exception ${e.message}")
            requireActivity().runOnUiThread {
                showToast("Failed to connect try again...")
            }
        } catch (e: IOException) {
            Log.d("favoriteButtonClicked", "IOException ${e.message}")
            requireActivity().runOnUiThread {
                showToast("Failed to connect try again...")
            }
        }
    }

    private fun updateStatusBar() {
        val decor: View? = activity?.window?.decorView
        decor?.systemUiVisibility = 0


    }

    override fun onCommentsClick(
        position: Int,
        data: UserShortsEntity,
        isFeedComment: Boolean
    ) {

        showBottomSheet()

    }


    @SuppressLint("InflateParams")
    private fun showBottomSheet() {


    }


    private fun startPreLoadingService() {
        Log.d(SHORTS, "Preloading called")
        val preloadingServiceIntent =
            Intent(requireContext(), VideoPreLoadingService::class.java)
        preloadingServiceIntent.putStringArrayListExtra(Constants.VIDEO_LIST, shortsList)
        requireContext().startService(preloadingServiceIntent)
    }

    override fun onSeekBarChanged(progress: Int) {


        exoPlayer!!.seekTo(progress.toLong())
    }

    private var isPlaying = false
    private fun togglesPausePlay() {
        Log.d("Pause", "togglePausePlay")
        if (isPlaying) {
            pauseVideo()
        } else {
            playVideo()
        }
    }

    private fun playVideo() {
        exoPlayer?.playWhenReady = true
        isPlaying = true
    }

    private fun pauseVideo() {
        exoPlayer?.playWhenReady = false
        isPlaying = false
    }

    private fun playbackStateListener() = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            when (state) {
                ExoPlayer.STATE_ENDED -> {
                    // The video playback ended. Move to the next video if available.
                }
                // Add other cases if needed
                Player.STATE_BUFFERING -> {

                }

                Player.STATE_IDLE -> {
                }

                Player.STATE_READY -> {

                    Log.d("TAG", "STATE_READY")
                    startUpdatingSeekBar()


                }

                else -> {
                    Log.d("TAG", "STOP SEEK BAR")
                    // Stop updating seek bar in other states
                    stopUpdatingSeekBar()
                }
            }
        }

        private var updateSeekBarJob: Job? = null

        private fun startUpdatingSeekBar() {
            updateSeekBarJob = CoroutineScope(Dispatchers.Main).launch {
                while (true) {
                    updateSeekBar()
                    delay(50) // Update seek bar every second (adjust as needed)
                }
            }
        }

        private fun stopUpdatingSeekBar() {
            updateSeekBarJob?.cancel()
        }

        override fun onIsPlayingChanged(isVideoPlaying: Boolean) {


        }

        override fun onEvents(player: Player, events: Player.Events) {

            if (events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED) ||
                events.contains(Player.EVENT_IS_PLAYING_CHANGED)
            ) {

            }

            if (events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION)
            ) {

            }
        }
    }

    override fun onDownloadClick(url: String, fileLocation: String) {
        Log.d(
            "Download",
            "OnDownload $url  \nto path : $fileLocation"
        )

        val permissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(requireActivity(), permissions, requestCode)
        } else {
            // You have permission, proceed with your file operations

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                // Check if the permission is not granted
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // Request the permission
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        WRITE_EXTERNAL_STORAGE_REQUEST_CODE
                    )
                } else {

                    download(url, fileLocation)
                }


            } else {
                download(url, fileLocation)
            }
        }



    }


    @SuppressLint("MissingInflatedId")
    override fun onShareClick(position: Int) {
        val context = requireContext()
        val shortVideo = shortsViewModel.videoShorts.getOrNull(position) ?: return

        Log.d(TAG, "Share clicked for video: ${shortVideo._id}")

        // Make API call to increment share count on server
        RetrofitClient.shareService.incrementShare(shortVideo._id)
            .enqueue(object : Callback<ShareResponse> {
                override fun onResponse(call: Call<ShareResponse>, response: Response<ShareResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { shareResponse ->
                            Log.d(TAG, "Share count updated on server: ${shareResponse.shareCount}")
                        }
                    } else {
                        Log.e(TAG, "Share sync failed: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<ShareResponse>, t: Throwable) {
                    Log.e(TAG, "Share network error - will sync later", t)
                }
            })

        // Show share bottom sheet
        val bottomSheetDialog = BottomSheetDialog(context)
        val shareView = layoutInflater.inflate(R.layout.bottom_dialog_for_share, null)
        bottomSheetDialog.setContentView(shareView)

        // Prepare share content
        val shareText = "Check out this video on Flash!\n" +
                "By: ${shortVideo.author.account.username}\n" +
                "${shortVideo.content}"
        val videoUrl = shortVideo.images.firstOrNull()?.url
        val fullShareText = if (videoUrl != null) "$shareText\n$videoUrl" else shareText

        // Setup share buttons
        shareView.findViewById<ImageButton>(R.id.btnWhatsApp)?.setOnClickListener {
            shareToApp(context, "com.whatsapp", fullShareText)
            bottomSheetDialog.dismiss()
        }

        shareView.findViewById<ImageButton>(R.id.btnSMS)?.setOnClickListener {
            shareViaSMS(context, fullShareText)
            bottomSheetDialog.dismiss()
        }

        shareView.findViewById<ImageButton>(R.id.btnInstagram)?.setOnClickListener {
            shareToApp(context, "com.instagram.android", fullShareText)
            bottomSheetDialog.dismiss()
        }

        shareView.findViewById<ImageButton>(R.id.btnMessenger)?.setOnClickListener {
            shareToApp(context, "com.facebook.orca", fullShareText)
            bottomSheetDialog.dismiss()
        }

        shareView.findViewById<ImageButton>(R.id.btnFacebook)?.setOnClickListener {
            shareToApp(context, "com.facebook.katana", fullShareText)
            bottomSheetDialog.dismiss()
        }

        shareView.findViewById<ImageButton>(R.id.btnTelegram)?.setOnClickListener {
            shareToApp(context, "org.telegram.messenger", fullShareText)
            bottomSheetDialog.dismiss()
        }

        // Setup action buttons
        shareView.findViewById<ImageButton>(R.id.btnReport)?.setOnClickListener {
            // Handle report action
            Toast.makeText(context, "Report functionality", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }

        shareView.findViewById<ImageButton>(R.id.btnNotInterested)?.setOnClickListener {
            // Handle not interested action
            Toast.makeText(context, "Not interested", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }

        shareView.findViewById<ImageButton>(R.id.btnSaveVideo)?.setOnClickListener {
            // Handle save video action
            Toast.makeText(context, "Save video functionality", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }

        shareView.findViewById<ImageButton>(R.id.btnDuet)?.setOnClickListener {
            // Handle duet action
            Toast.makeText(context, "Duet functionality", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }

        shareView.findViewById<ImageButton>(R.id.btnReact)?.setOnClickListener {
            // Handle react action
            Toast.makeText(context, "React functionality", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }

        shareView.findViewById<ImageButton>(R.id.btnAddToFavorites)?.setOnClickListener {
            // Handle add to favorites action
            Toast.makeText(context, "Add to favorites", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }

        // Setup cancel button
        shareView.findViewById<TextView>(R.id.btnCancel)?.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    // Helper function to share to specific app
    private fun shareToApp(context: Context, packageName: String, text: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                setPackage(packageName)
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "App not installed", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Error sharing to $packageName", e)
        }
    }

    // Helper function to share via SMS
    private fun shareViaSMS(context: Context, text: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("sms:")
                putExtra("sms_body", text)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "SMS not available", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Error sharing via SMS", e)
        }
    }

    override fun onUploadCancelClick() {
        TODO("Not yet implemented")
    }

    fun generateUniqueValue(): String {
        val currentTimeMillis = System.currentTimeMillis()

        // You can format the timestamp as needed
        val dateFormat = SimpleDateFormat("yyyy_MM_dd_HHmmss", Locale.getDefault())

        // Create a unique value using the formatted timestamp or customize it as needed

        return dateFormat.format(Date(currentTimeMillis))
    }

    private fun String.replaceLast(oldValue: String, newValue: String): String {
        val lastIndexOf = this.lastIndexOf(oldValue)
        return if (lastIndexOf == -1) this else this.substring(
            0,
            lastIndexOf
        ) + newValue + this.substring(lastIndexOf + oldValue.length)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int,
        fileLocation: String
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create a Notification Channel for Android Oreo and above
        val channelId = "channel_id"
        val channel = NotificationChannel(
            channelId,
            "Channel Name",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = "Channel Description"
        channel.enableLights(true)
        channel.lightColor = Color.BLUE
        notificationManager.createNotificationChannel(channel)

        // Create an Intent to view the video file
        val fileUri = Uri.parse(fileLocation)
        val intent = Intent(Intent.ACTION_VIEW, fileUri)
        intent.setDataAndType(fileUri, "video/*") // Set the MIME type for videos

        // Create a PendingIntent to be triggered when the notification is clicked
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Create the notification
        val builder = NotificationCompat.Builder(context, "channel_id")
            .setSmallIcon(R.drawable.flash21) // Set your notification icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent) // Set the PendingIntent

        // Show the notification with a unique ID
        notificationManager.notify(notificationId, builder.build())
    }

    private fun generateRandomNotificationId(): Int {
        val randomUUID = UUID.randomUUID()
        return abs(randomUUID.hashCode())
    }

    private fun generateUniqueFileName(originalUrl: String): String {
        val timestamp =
            SimpleDateFormat("yyyy_MM_dd_HHmmss", Locale.getDefault()).format(Date())
        val originalFileName = originalUrl.split("/").last()
        val fileExtension = MimeTypeMap.getFileExtensionFromUrl(originalFileName)
        val randomString = UUID.randomUUID().toString().substring(0, 8)
        return "$timestamp-$randomString.$fileExtension"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @kotlin.OptIn(DelicateCoroutinesApi::class)
    private fun download(
        mUrl: String,
        fileLocation: String,
    ) {

        Log.d("Download", "directory path - $fileLocation")

        if (mUrl.startsWith("/storage/") || mUrl.startsWith("/storage/")) {

            Log.d("Download", "Cannot download a local file")
            return
        }

        //STORAGE_FOLDER += fileLocation
        val STORAGE_FOLDER = "/Download/Flash/$fileLocation"

        val fileName = generateUniqueFileName(mUrl)

        val storageDirectory =
            Environment.getExternalStorageDirectory().toString() + STORAGE_FOLDER + "/$fileName"

        Log.d("Download", "directory path - $storageDirectory")
        val file = File(Environment.getExternalStorageDirectory().toString() + STORAGE_FOLDER)
        if (!file.exists()) {
            file.mkdirs()
        }

        GlobalScope.launch(Dispatchers.IO) {
            val url = URL(mUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept-Encoding", "identity")
            connection.connect()

            try {
                if (connection.responseCode in 200..299) {
                    val fileSize = connection.contentLength
                    val inputStream = connection.inputStream
                    val outputStream = FileOutputStream(storageDirectory)

                    var bytesCopied: Long = 0
                    val buffer = ByteArray(1024)
                    var bytes = inputStream.read(buffer)
                    while (bytes >= 0) {
                        bytesCopied += bytes
                        val downloadProgress =
                            (bytesCopied.toFloat() / fileSize.toFloat() * 100).toInt()
                        requireActivity().runOnUiThread {

                            downloadProgressBarLayout.visibility = View.VISIBLE
                            shortsDownloadImageView.setBackgroundResource(R.drawable.shorts_download_animation)
                            wifiAnimation =
                                shortsDownloadImageView.background as AnimationDrawable
                            wifiAnimation!!.start()
                            Log.d("Download", "Progress $downloadProgress")
                            shortsDownloadProgressBar.progress = downloadProgress
                        }
                        outputStream.write(buffer, 0, bytes)
                        bytes = inputStream.read(buffer)
                    }

                    requireActivity().runOnUiThread {

                        Log.d("Download", "File Downloaded : $storageDirectory")
                        downloadProgressBarLayout.visibility = View.GONE

                        wifiAnimation!!.stop()

                        // Show notification
                        showNotification(
                            requireActivity(),
                            "Download Complete",
                            "File downloaded successfully.",
                            generateRandomNotificationId(),
                            storageDirectory
                        )
                        val downloadedFile = File(storageDirectory)

                    }
                    outputStream.close()
                    inputStream.close()
                } else {
                    requireActivity().runOnUiThread {
                        Toast.makeText(
                            requireActivity(),
                            "Not successful",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("DownloadFailed", e.message.toString())

                e.printStackTrace()
                requireActivity().runOnUiThread {

                    downloadProgressBarLayout.visibility = View.GONE
                }
            }
        }
    }

    override fun onGetLayoutInflater(
        savedInstanceState: Bundle?
    ): LayoutInflater {


        return super.onGetLayoutInflater(savedInstanceState).cloneInContext(
            ContextThemeWrapper(
                requireContext(), R.style.DarkTheme
            )
        )
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirm Logout")
        builder.setMessage("Are you sure you want to logout?")
        builder.setPositiveButton("Yes") { dialog, which ->
            // Handle logout here
            performLogout()
        }
        builder.setNegativeButton("No") { dialog, which ->
            // Dismiss the dialog
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun performLogout() {
        CoroutineScope(Dispatchers.IO).launch {

            LocalStorage.getInstance(requireContext()).clear()
            LocalStorage.getInstance(requireContext()).clearToken()

        }

        val intent = Intent(requireActivity(), LoginActivity::class.java)
        requireActivity().finish()
        startActivity(intent)
    }


    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun feedInformShortsFragment(event: InformShortsFragment2) {
        Log.d("feedInformShortsFragment", "InformShortsFragment2: shorts fragment informed")
        val list: MutableList<ShortsEntityFollowList> = mutableListOf()
        list.add(ShortsEntityFollowList(event.userId, event.isFollowing))
        val followListItem: List<ShortsEntityFollowList> = listOf(
            ShortsEntityFollowList(
                event.userId, event.isFollowing
            )
        )
        lifecycleScope.launch(Dispatchers.IO) {

            val uniqueFollowList = removeDuplicateFollowers(followListItem)
            Log.d(
                "feedShortsSharedViewModel",
                "feedShortsSharedViewModel: Inserted uniqueFollowList in shorts: $uniqueFollowList"
            )
            delay(100)
            followShortsViewModel.insertFollowListItems(uniqueFollowList)
            shortsViewModel.followList.add(ShortsEntityFollowList(event.userId, event.isFollowing))
        }
        shortsAdapter.addIsFollowingData(list)
        shortsAdapter.notifyDataSetChanged()
    }



}
