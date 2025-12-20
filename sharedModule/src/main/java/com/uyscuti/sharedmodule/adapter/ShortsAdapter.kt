package com.uyscuti.sharedmodule.adapter

import android.annotation.SuppressLint
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
import com.uyscuti.sharedmodule.R
import com.uyscuti.sharedmodule.data.model.shortsmodels.OtherUsersProfile
import com.uyscuti.sharedmodule.User_Interfaces.OtherUserProfile.OtherUserProfileAccount
import com.uyscuti.sharedmodule.model.GoToUserProfileFragment
import com.uyscuti.sharedmodule.model.PausePlayEvent
import com.uyscuti.sharedmodule.model.ShortsBookmarkButton
import com.uyscuti.sharedmodule.model.ShortsFollowButtonClicked
import com.uyscuti.sharedmodule.model.ShortsLikeUnLike
import com.uyscuti.sharedmodule.model.ShortsLikeUnLikeButton
import com.uyscuti.sharedmodule.shorts.ExoPlayerItem
import com.uyscuti.social.chatsuit.commons.ViewHolder
import com.uyscuti.social.core.common.data.room.entity.FollowUnFollowEntity
import com.uyscuti.social.core.common.data.room.entity.ShortsEntity
import com.uyscuti.social.core.common.data.room.entity.ShortsEntityFollowList
import com.uyscuti.social.core.common.data.room.entity.UserShortsEntity
import com.uyscuti.social.network.utils.LocalStorage
import org.greenrobot.eventbus.EventBus


private const val TAG = "PlayerActivity"
private const val TAG2 = "MyData"

class StringViewHolder(
    itemView: View,
    private val commentsClickListener: OnCommentsClickListener,
    private var onClickListeners: OnClickListeners,
    private var exoplayer: ExoPlayer,
    private var videoPreparedListener: OnVideoPreparedListener,
    private val onFollow: (String, String, AppCompatButton) -> Unit
) : ViewHolder<MyData>(itemView) {

    val btnLiked = false

    private var player: Player? = null

    private val shareBtn : ImageButton = itemView.findViewById(R.id.shareBtn)
    private val videoView: PlayerView = itemView.findViewById(R.id.video_view)
    //    private val captionTextView: ReadMoreTextView = itemView.findViewById(R.id.tvReadMoreLess)
    private val captionTextView: TextView = itemView.findViewById(R.id.tvReadMoreLess)
    //    private val seeMoreButton: TextView = itemView.findViewById(R.id.seeMoreButton)
    private val btnLike: ImageButton = itemView.findViewById(R.id.btnLike)
    private val favorite: ImageView = itemView.findViewById(R.id.favorite)
    private val comments: ImageView = itemView.findViewById(R.id.comments)
    private val downloadBtn: ImageButton = itemView.findViewById(R.id.downloadBtn)

    private val profileImageView: ImageView = itemView.findViewById(R.id.profileImageView)
   // private val shortsSeekBar: SeekBar = itemView.findViewById(R.id.shortsSeekBar)
    private val shortsViewPager: FrameLayout = itemView.findViewById(R.id.shortsViewPager)
    private val followButton: AppCompatButton = itemView.findViewById(R.id.followButton)
    private val username: TextView = itemView.findViewById(R.id.shortUsername)
    private val likeCount: TextView = itemView.findViewById(R.id.likeCount)
//    private val favoriteCount: TextView = itemView.findViewById(R.id.favoriteCount)
    private val commentsCount: TextView = itemView.findViewById(R.id.commentsCount)
    private val commentsParentLayout: LinearLayout = itemView.findViewById(R.id.commentsParentLayout)
//    private val commentsRecyclerView:RecyclerView = itemView.findViewById(R.id.rv)

    private val mainHandler = Handler(Looper.getMainLooper())
    private var totalLikes = 0
    private var totalComments = 0
    fun getSurface(): PlayerView {
        return videoView
    }

    fun updateSeekBarProgress(progress: Long) {
        Log.d(TAG, "updateSeekBarProgress $progress")
        val progressInSeconds = progress / 1000
       // shortsSeekBar.progress = progressInSeconds.toInt()
//        shortsSeekBar.progress = progress
    }

    fun setSeekBarMaxValue(max: Int) {
        //shortsSeekBar.max = max
    }

    private var isLiked = false
    private var isFavorite = false
    private var isFollowed = false

    private var isUserSeeking = false
    private var isPlaying = false

    init {


        shortsViewPager.setOnClickListener {
            EventBus.getDefault().post(PausePlayEvent(true))
        }


//        shortsSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                // Update playback position when user drags the SeekBar
//                if (fromUser) {
//                    Log.d(TAG, "From user seek bar")
//                    onClickListeners.onSeekBarChanged(progress)
//                } else {
//                    Log.d(TAG, "Not From user seek bar")
//                }
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar?) {
//                // User starts seeking
//                isUserSeeking = true
//            }
//
//            override fun onStopTrackingTouch(seekBar: SeekBar?) {
//                // User stops seeking
//                isUserSeeking = false
//            }
//        })
    }

    private fun updateSeekBar() {
        player?.let { player ->
            if (!isUserSeeking) {
                val currentPosition = player.currentPosition.toInt()
               // shortsSeekBar.progress = currentPosition
            }
        }
    }


    @OptIn(UnstableApi::class)
    @SuppressLint("SetTextI18n")
    override fun onBind(data: MyData) {
        // Set the video URL to the VideoView
        Log.d("Shorts", "data in view holder: $data")
        val url = data.shortsEntity.images[0].url
        val shortOwnerId = data.shortsEntity.author.account._id
        val shortOwnerDate = data.shortsEntity.author.createdAt
        val shortOwnerUsername = data.shortsEntity.author.account.username
        val shortOwnerName =
            "${data.shortsEntity.author.firstName} ${data.shortsEntity.author.lastName}"
        val shortOwnerProfilePic = data.shortsEntity.author.account.avatar.url
//        totalLikes = data.shortsEntity.likes
        totalComments = data.shortsEntity.comments
//        data.author.account.
        EventBus.getDefault().post(ShortsLikeUnLikeButton(data.shortsEntity, btnLike, isLiked, likeCount))
        EventBus.getDefault().post(ShortsBookmarkButton(data.shortsEntity, favorite))
        commentsParentLayout.setOnClickListener {
            Log.d(TAG, "onBind: Posting for main activity to open comments")
            val uSE = shortsEntityToUserShortsEntity(data.shortsEntity)
            commentsClickListener.onCommentsClick(itemView.id, uSE)
//            EventBus.getDefault().post(ShortsCommentButtonClicked(uSE))
        }
        shareBtn.setOnClickListener {
            onClickListeners.onShareClick(position)

        }
        downloadBtn.setOnClickListener {
            onClickListeners.onDownloadClick(url, "FlashShorts")
        }
        if (shortOwnerId == LocalStorage.getInstance(profileImageView.context).getUserId()) {

            followButton.visibility = View.INVISIBLE
            Log.d(TAG, "onBind: short owner id == logged user id")
        }
        else {
            followButton.visibility = View.VISIBLE
//            EventBus.getDefault().post(HandleInShortsFollowButtonClick(followButton, shortOwnerId, shortOwnerUsername))

            Log.d(TAG, "onBind: ${data.followItemEntity.isFollowing}")
            Log.d(TAG, "onBind: ${data.followItemEntity.followersId}")
            if (data.followItemEntity.isFollowing) {
                followButton.text = "Following"
                followButton.isAllCaps = false
                followButton.setBackgroundResource(R.drawable.shorts_following_button)
                isFollowed = true
            } else {
                followButton.text = "Follow"
                followButton.setBackgroundResource(R.drawable.shorts_follow_button_border)
                isFollowed = false
            }
            followButton.setOnClickListener {
//                onFollow.invoke(shortOwnerId, shortOwnerUsername, followButton)

                handleFollowButtonClick(shortOwnerId)
            }
            Log.d(TAG, "onBind: short owner id != logged user id")
        }
        Glide.with(itemView.context)
            .load(data.shortsEntity.author.account.avatar.url)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .apply(RequestOptions.placeholderOf(R.drawable.flash21))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(profileImageView)

        profileImageView.setOnClickListener {
            if (shortOwnerId == LocalStorage.getInstance(profileImageView.context).getUserId()) {
                //open user profile fragment
                EventBus.getDefault().post(GoToUserProfileFragment())
            } else {
                Log.d(TAG, "onBind: Clicked on another users profile")
                val otherUsersProfile = OtherUsersProfile(
                    shortOwnerName, shortOwnerUsername, shortOwnerProfilePic, shortOwnerId, false
                )

                OtherUserProfileAccount.open(
                    profileImageView.context,
                    otherUsersProfile,
                    shortOwnerProfilePic,
                    shortOwnerId
                )
            }
        }
        // You can add other configurations for the VideoView here
        // For example, you may want to set an OnCompletionListener, etc.

        // Start playing the video

        val caption = data.shortsEntity.content.toString()
        if (caption.isNotEmpty()) {
            captionTextView.text = caption

        } else {
            Log.d("Caption", "Caption empty")
        }

        username.text = data.shortsEntity.author.account.username

//        likeCount.text = totalLikes.toString()
        commentsCount.text = totalComments.toString()

        username.setOnClickListener {


            if (shortOwnerId == LocalStorage.getInstance(username.context).getUserId()) {
                //open user profile fragment
                EventBus.getDefault().post(GoToUserProfileFragment())

            } else {
                Log.d(TAG, "onBind: Clicked on another users profile")
                val otherUsersProfile = OtherUsersProfile(
                    shortOwnerName, shortOwnerUsername, shortOwnerProfilePic, shortOwnerId,false
                )
                OtherUserProfileAccount.open(
                    username.context,
                    otherUsersProfile,
                    shortOwnerProfilePic,
                    shortOwnerId
                )
            }


        }

    }
    private fun shareVideoShot () {


    }

    private val list = ArrayList<String>()

    private fun shortsComments() {
        for (i in 1..3) {
            list.add("user $i")
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
            thumbnail = serverResponseItem.thumbnail
            // map other properties...
        )
    }


    private fun handleFollowButtonClick(shortOwnerId: String) {
        Log.d(TAG, "handleFollowButtonClick: is followed value $isFollowed")
        isFollowed = !isFollowed
        if (isFollowed) {
            followButton.text = "Following"

            Log.d(TAG, "handleFollowButtonClick: following $isFollowed")
            val followUnFollowEntity = FollowUnFollowEntity(shortOwnerId, true)
            EventBus.getDefault().post(ShortsFollowButtonClicked(followUnFollowEntity))

        } else {
            Log.d(TAG, "handleFollowButtonClick: follow $isFollowed")

            followButton.text = "Follow"

            val followUnFollowEntity = FollowUnFollowEntity(shortOwnerId, false)
            EventBus.getDefault().post(ShortsFollowButtonClicked(followUnFollowEntity))
//            followButton.context.theme.applyStyle(R.style.Theme_AppCompat_Light, true)
        }

    }

    fun updateButton(newText: String) {
        // Update the button text
        // Note: Ensure that this method is called on the main thread
        followButton.text = newText
    }

    private fun handleLikeClick(shortOwnerId: String) {


        isLiked = !isLiked
        if (isLiked) {
            totalLikes += 1
            likeCount.text = totalLikes.toString()
            // Set the liked drawable
            btnLike.setImageResource(R.drawable.filled_favorite_like)
            YoYo.with(Techniques.Tada)
                .duration(700)
                .repeat(1)
                .playOn(btnLike)
            EventBus.getDefault().post(ShortsLikeUnLike(shortOwnerId, isLiked))
//            isLiked = true

        } else {
            // Set the unliked drawable
            totalLikes -= 1
            likeCount.text = totalLikes.toString()
//            isLiked = false
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
            // Set the liked drawable
            YoYo.with(Techniques.Tada)
                .duration(700)
                .repeat(1)
                .playOn(favorite)
            favorite.setImageResource(R.drawable.filled_favorite)
        } else {
            // Set the unliked drawable
            YoYo.with(Techniques.Tada)
                .duration(700)
                .repeat(1)
                .pivotX(2.6F)
                .playOn(favorite)
            favorite.setImageResource(R.drawable.favorite_svgrepo_com__1_)
        }
    }

}

interface OnCommentsClickListener {
    fun onCommentsClick(position: Int, data: UserShortsEntity)
}

interface OnClickListeners {
    fun onSeekBarChanged(progress: Int)
    fun onDownloadClick(url: String, fileLocation: String)
    fun onShareClick(position: Int)
//    fun onLikeButtonClick(position: Int, likeButton: View)
}

interface OnVideoPreparedListener {
    fun onVideoPrepared(exoPlayerItem: ExoPlayerItem)
}


class ShortsAdapter(
    private val commentsClickListener: OnCommentsClickListener,
    private var clickListeners: OnClickListeners,
    private var exoplayer: ExoPlayer,
    private var videoPreparedListener: OnVideoPreparedListener,
    private val onFollow: (String, String, AppCompatButton) -> Unit
) : RecyclerView.Adapter<StringViewHolder>() {
    private val viewHolderList = mutableListOf<StringViewHolder>()
    private val shortsList: MutableList<ShortsEntity> = mutableListOf()

    //    private val flowList: MutableList<ShortsEntity> = mutableListOf()
    private val followingData: MutableList<ShortsEntityFollowList> = mutableListOf()

    // Keep track of the current active view holder
    private var currentViewHolder: StringViewHolder? = null
    private var surfaceList: ArrayList<PlayerView> = arrayListOf()


    @SuppressLint("NotifyDataSetChanged")
    fun addData(newData: List<ShortsEntity>) {
        // Determine the position where the new data will be inserted
        val startPosition = shortsList.size

        // Add the new data to the existing list
        shortsList.addAll(newData)

        Log.d(TAG, "addData: shorts list size: ${shortsList.size}")
        // Notify the adapter that new items have been inserted
        if (startPosition == 0) {
            // If the adapter was empty, use notifyDataSetChanged
            notifyDataSetChanged()
        } else {
            // Otherwise, use notifyItemRangeInserted
            notifyItemRangeInserted(startPosition, newData.size)

        }
    }

    fun addIsFollowingData(isFollowingData: List<ShortsEntityFollowList>) {
//        val startPosition = followingData.size
        followingData.addAll(isFollowingData)

//        Log.d(TAG, "addIsFollowingData: $isFollowingData")
    }


    fun updateBtn(text: String) {
        currentViewHolder?.updateButton(text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StringViewHolder {
//        EventBus.getDefault().register(this)

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.shorts_view_pager_item_view, parent, false)

        Log.d("ShortsAdapter", "Inflated view: $view")
        Log.d("ShortsAdapter", "Has shortsSeekBar: ${view.findViewById<SeekBar>(R.id.shortsSeekBar) != null}")

        val viewHolder = StringViewHolder(
            view,
            commentsClickListener,
            clickListeners,
            exoplayer,
            videoPreparedListener
        ) { id, name, followButton ->
            onFollow.invoke(id, name, followButton)
        }

        viewHolderList.add(viewHolder)
        return viewHolder
    }

    private fun getIsFollowingData(): MutableList<ShortsEntityFollowList> {
        return followingData
    }

    override fun onBindViewHolder(holder: StringViewHolder, position: Int) {
        currentViewHolder = holder
        val data = shortsList[position]

        val isFollowingData = followingData.findLast { it.followersId == data.author.account._id }!!

        val myData = MyData(data, isFollowingData)

        Log.d(TAG2, "onBindViewHolder: MyData position $position: follow: ${myData.followItemEntity}: follow size ${followingData.size}")
        holder.onBind(myData)


        val surface = holder.getSurface()
        val isAdded = surfaceList.contains(surface)
        if (!isAdded) {
            try {
                surfaceList.add(position, surface)

            } catch (e: IndexOutOfBoundsException) {
                Log.d(TAG, "IndexOutOfBoundsException: ${e.message}")
            }
            Log.d("Video Adapter", "Added Surface at position $position")
        } else {
            Log.d("Video Adapter", "Surface at position $position already added ")
        }

    }

    override fun getItemCount(): Int {
        return shortsList.size
    }

}


data class MyData(
    val shortsEntity: ShortsEntity,
    val followItemEntity: ShortsEntityFollowList
)


