package com.uyscuti.social.circuit.adapter


import android.annotation.SuppressLint
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
import androidx.appcompat.widget.AppCompatButton
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.uyscuti.social.circuit.model.PlayPauseEvent
import com.uyscuti.social.circuit.model.ShortsBookmarkButton2
import com.uyscuti.social.circuit.model.ShortsCommentButtonClicked
import com.uyscuti.social.circuit.model.ShortsLikeUnLikeButton2
import com.uyscuti.social.chatsuit.commons.ViewHolder
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.data.model.Dialog
import com.uyscuti.social.core.common.data.room.entity.UserShortsEntity
import org.greenrobot.eventbus.EventBus


private const val TAG = "UserProfileShortsAdapter"


class UserProfileShortsAdapter(
    private val commentsClickListener: OnCommentsClickListener,
    private var clickListeners: OnClickListeners,
) : RecyclerView.Adapter<UserProfileShortsViewHolder>() {
    private val viewHolderList = mutableListOf<UserProfileShortsViewHolder>()
    private val shortsList: MutableList<UserShortsEntity> = mutableListOf()

    // Keep track of the current active view holder
    private var currentViewHolder: UserProfileShortsViewHolder? = null
    private var surfaceList: ArrayList<PlayerView> = arrayListOf()

    fun addData(newData: List<UserShortsEntity>) {
        // Determine the position where the new data will be inserted
        val startPosition = shortsList.size

        // Add the new data to the existing list
        shortsList.addAll(newData)

        // Notify the adapter that new items have been inserted
        if (startPosition == 0) {
            // If the adapter was empty, use notifyDataSetChanged
            notifyDataSetChanged()
        } else {
            // Otherwise, use notifyItemRangeInserted
            notifyItemRangeInserted(startPosition, newData.size)

        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserProfileShortsViewHolder {


        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.shorts_view_pager_item, parent, false)

        val viewHolder = UserProfileShortsViewHolder(view, commentsClickListener, clickListeners)
        viewHolderList.add(viewHolder)
        return viewHolder
    }
    override fun onBindViewHolder(holder: UserProfileShortsViewHolder, position: Int) {
        currentViewHolder = holder
        val data = shortsList[position]
        holder.onBind(data)
        val surface = holder.getSurface()
        val isAdded = surfaceList.contains(surface)
        if (!isAdded) {
            try {
                surfaceList.add(position, surface)

            }catch (e: IndexOutOfBoundsException) {
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

class UserProfileShortsViewHolder(
    itemView: View,
    private val commentsClickListener: OnCommentsClickListener,
    private var onClickListeners: OnClickListeners,
) : ViewHolder<UserShortsEntity>(itemView) {

    private val videoView: PlayerView = itemView.findViewById(R.id.video_view)
    private val captionTextView: TextView = itemView.findViewById(R.id.tvReadMoreLess)
    private val btnLike: ImageButton = itemView.findViewById(R.id.btnLike)
    private val likeCount: TextView = itemView.findViewById(R.id.likeCount)
    private val commentsCount: TextView = itemView.findViewById(R.id.commentsCount)

    private val favorite: ImageView = itemView.findViewById(R.id.favorite)
    private val commentsParentLayout: LinearLayout = itemView.findViewById(R.id.commentsParentLayout)
    private val downloadBtn: ImageButton = itemView.findViewById(R.id.downloadBtn)
    private val profileImageView: ImageView = itemView.findViewById(R.id.profileImageView)
    private val shortsSeekBar: SeekBar = itemView.findViewById(R.id.shortsSeekBar)
    private val shortsViewPager: FrameLayout = itemView.findViewById(R.id.shortsViewPager)
    private val shortUsername: TextView = itemView.findViewById(R.id.shortUsername)
    private val followButton: AppCompatButton = itemView.findViewById(R.id.followButton)



    fun getSurface(): PlayerView {
        return videoView
    }

    private var isLiked = false
    private var isFavorite = false

    private var isUserSeeking = false

    init {

        btnLike.setOnClickListener {
            handleLikeClick()
        }
        favorite.setOnClickListener {
            handleFavoriteClick()
        }


        followButton.visibility = View.INVISIBLE


        shortsViewPager.setOnClickListener {
            EventBus.getDefault().post(PlayPauseEvent(true))
        }
        shortsSeekBar.setOnSeekBarChangeListener(

            object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Update playback position when user drags the SeekBar
                if (fromUser) {
                    Log.d(TAG, "From user seek bar")
                    onClickListeners.onSeekBarChanged(progress)
                }else {
                    Log.d(TAG, "Not From user seek bar")
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // User starts seeking
                isUserSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // User stops seeking
                isUserSeeking = false
            }
        })
    }


    @SuppressLint("SetTextI18n")
    override fun onBind(data: UserShortsEntity?) {
        // Set the video URL to the VideoView
        Log.d("Shorts", "data in view holder: $data")

        val url = data?.images?.get(0)?.url
        val shortOwnerDate = data!!.author.createdAt
        val shortOwnerUsername = data.author.account.username
        val shortOwnerName = "${data.author.firstName} ${data.author.lastName}"
        val shortOwnerProfilePic = data.author.account.avatar.url


        EventBus.getDefault().post(ShortsLikeUnLikeButton2(data, btnLike, isLiked, likeCount))
        EventBus.getDefault().post(ShortsBookmarkButton2(data, favorite))

        commentsParentLayout.setOnClickListener {
            EventBus.getDefault().post(
                ShortsCommentButtonClicked(position = absoluteAdapterPosition, data))

        }

        downloadBtn.setOnClickListener {
            if (url != null) {
                onClickListeners.onDownloadClick(url, "FlashShorts")
            }
        }

        Glide.with(itemView.context)
            .load(data.author.account.avatar.url)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .apply(RequestOptions.placeholderOf(R.drawable.google))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(profileImageView)

        profileImageView.setOnClickListener {
            val shortOwnerId = data.author.account._id

            var dialog: Dialog? = null

        }
        val caption = data.content
        if(caption.isNotEmpty()) {
            captionTextView.text = caption
        }
        else {
            Log.d("Caption", "Caption empty")
        }

        shortUsername.text = shortOwnerUsername
        commentsCount.text = data.comments.toString()
    }

    private fun handleLikeClick() {
        isLiked = !isLiked
        if (isLiked) {
            // Set the liked drawable
            btnLike.setImageResource(R.drawable.filled_favorite_like)
            YoYo.with(Techniques.Tada)
                .duration(700)
                .repeat(1)
                .playOn(btnLike)
        } else {
            // Set the unliked drawable
            btnLike.setImageResource(R.drawable.favorite_svgrepo_com)
            YoYo.with(Techniques.Tada)
                .duration(700)
                .repeat(1)
                .playOn(btnLike)
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