package com.uyscuti.social.circuit.adapter.notifications

import android.annotation.SuppressLint
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.widget.PopupMenu
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.uyscuti.social.circuit.data.model.shortsmodels.OtherUsersProfile
import com.uyscuti.social.circuit.model.notificatioRead.NotificationRead
import com.uyscuti.social.circuit.model.notifications_data_class.INotification
import com.uyscuti.social.circuit.presentation.MainViewModel
import com.uyscuti.social.circuit.User_Interface.feedactivities.ReportNotificationActivity2
import com.uyscuti.social.circuit.viewmodels.notificationViewModel.NotificationViewModel
import com.uyscuti.social.circuit.R
import com.uyscuti.social.core.common.data.room.entity.ShortsEntityFollowList
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import org.greenrobot.eventbus.EventBus
import java.util.Date

private const val NOTIFICATION_TYPE_WITH_TEXT = 1
private const val NOTIFICATION_TYPE_WITH_IMAGE = 2
private const val NOTIFICATION_TYPE_WITH_VIDEO = 3
private const val NOTIFICATION_TYPE_WITH_FOLLOW = 4
private const val NOTIFICATION_TYPE_WITH_UNFOLLOW = 5
private const val NOTIFICATION_TYPE_REPLY_COMMENT = 6
private const val NOTIFICATION_TYPE_FAVORITE = 7
private const val NOTIFICATION_TYPE_LIKE = 8
private const val NOTIFICATION_TYPE_COMMENT = 9
private const val NOTIFICATION_TYPE_FRIEND_SUGGESTION = 10



class NotificationsAdapter(
    private val notifications: ArrayList<INotification>,
    private val notificationActionListener: NotificationActionListener

) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
private lateinit var retrofitInterface: RetrofitInstance
    private val selectedItems = mutableSetOf<Int>()
    private lateinit var mainViewModel: MainViewModel
    private lateinit var notificationViewModel: NotificationViewModel
    private var selectedNotifications = 0

    /**this is the interface for the NotificationListener*/
    interface NotificationActionListener {
        fun removeNotification(notification: INotification)
        fun onNotificationLongClick(notification: INotification)
        fun onClickSingleNotification(notification: INotification)
        fun onRemoveNotificationClick(notification: INotification)
    }
    interface OnMarkAsUnreadListener {
        fun onMarkAsUnread(notification: INotification)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.d("NotificationsAdapter", "ViewType: $viewType")
        return when (viewType) {
            NOTIFICATION_TYPE_WITH_TEXT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.notification_type_with_text, parent, false)
                TextViewHolder(view)
            }
            NOTIFICATION_TYPE_WITH_IMAGE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.notification_type_with_image, parent, false)
                ImageViewHolder(view)
            }
            NOTIFICATION_TYPE_WITH_UNFOLLOW -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.notification_unfollow, parent, false)
                UnfollowViewHolder(view)
            }
            NOTIFICATION_TYPE_REPLY_COMMENT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.notification_comment_reply, parent, false)
                CommentReplyViewHolder(view)
            }
            NOTIFICATION_TYPE_FAVORITE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.notification_fav_book, parent, false)
                FavoriteViewHolder(view)
            }
            NOTIFICATION_TYPE_LIKE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.notification_like_type, parent, false)
                LikeNotificationViewHolder(view)
            }
            NOTIFICATION_TYPE_COMMENT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.notification_oncomment_type, parent, false)
                OnCommentViewHolder(view)
            }
            NOTIFICATION_TYPE_FRIEND_SUGGESTION -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.friend_suggestions, parent, false)
                FriendSuggestionViewHolder(view)
            }
            NOTIFICATION_TYPE_WITH_FOLLOW ->{
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.notification_type_with_follow, parent, false)
                FollowViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }
    override fun getItemCount(): Int {
        return notifications.size
    }
    @OptIn(UnstableApi::class)
    @SuppressLint("SetTextI18n", "NotifyDataSetChanged", "SuspiciousIndentation")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val notificationItem = notifications[position]
        /**listener for every notification item */
        holder.itemView.setOnLongClickListener { view ->
            /**Scale up the view**/
            view.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction {
                /**Scale back to original size*/
                view.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
            }.start()
            if (selectedItems.contains(position)) {
                holder.itemView.setBackgroundResource(R.color.white)
                notificationActionListener.onRemoveNotificationClick(notificationItem)
            } else {
                selectedItems.add(position)
                holder.itemView.setBackgroundResource(R.color.bluejeans)
                notificationActionListener.onNotificationLongClick(notificationItem)
            }
            true
        }
        holder.itemView.setOnClickListener {
            /**Fade to white background with animation*/
            notificationItem.isRead = true
            holder.itemView.setBackgroundColor(holder.itemView.context.getColor(com.uyscuti.social.chatsuit.R.color.transparent))
            EventBus.getDefault().post(NotificationRead(false, notificationItem._id))
            if (selectedItems.contains(position)) {
                selectedItems.remove(position)
                notificationActionListener.onRemoveNotificationClick(notificationItem)
            } else {
                selectedItems.add(position)
                holder.itemView.setBackgroundResource(R.color.bluejeans)
                notificationActionListener.onClickSingleNotification(notificationItem)
            }
        }

        val isSelected = selectedItems.contains(position)
        if (notificationItem.isRead) {
            holder.itemView.setBackgroundResource(R.color.white)
        } else {
            holder.itemView.setBackgroundResource(R.color.bluejeans)
        }
        when (holder) {
            /**TextViewHolder is used for text notifications*/
            is TextViewHolder -> {
                val fullMessage = notificationItem.notificationMessage
                // Limit the message to 40 characters
                val truncatedMessage = if (fullMessage.length > 40) {
                    "${fullMessage.substring(0, 40)}..."
                } else {
                    fullMessage
                }
                notificationItem.avatar
                holder.notificationMessage.text =
                    truncatedMessage + " ${notificationItem.notificationTime}"
                holder.username.text = notificationItem.name
                Log.d(
                    "NotificationAdapter",
                    "Profile Image URL NotificationAdapter: ${notificationItem.avatar}"
                )

                if (notificationItem.avatar.isNotEmpty()) {
                    Log.w(
                        "NotificationAdapter",
                        "Avatar URL is empty or null for notification: $notificationItem"
                    )
                }

                Log.e("NotificationAdapter", "avatar upload : ${notificationItem.avatar}")
                Glide.with(holder.itemView.context)
                    .load(notificationItem.avatar)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .apply(RequestOptions.placeholderOf(R.drawable.nav_user_svg))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.profilePic)


                holder.itemView.setOnClickListener {
                    if (notificationItem.isRead.not()) {
                        notificationItem.isRead = true
                        holder.itemView.setBackgroundResource(R.color.white)
                        EventBus.getDefault().post(NotificationRead(true, notificationItem._id))
                    }
                }
            }
            /**code not in use*/
            is ImageViewHolder -> {
                /** Set up your image view or other views for image type */
                holder.notificationMessage.text =
                    notificationItem.notificationMessage + " ${notificationItem.notificationTime}"
                holder.username.text = notificationItem.name
                Glide.with(holder.itemView.context)
                    .load(notificationItem.avatar)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .apply(RequestOptions.placeholderOf(R.drawable.nav_user_svg))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.profilePic)
            }
            /**t his is FollowViewHolder */
       /**the is UnfollowViewHolder */
            is UnfollowViewHolder -> {
                val fullMessage = notificationItem.notificationTime
                val truncatedMessage = if (fullMessage.length > 40) {
                    "${fullMessage.substring(0, 40)}..."
                } else {
                    fullMessage
                }
                notificationItem.avatar
                holder.username.text = notificationItem.name
                holder.notificationTime.text = notificationItem.notificationTime
//                <-------user profile image --------->
                Glide.with(holder.itemView.context)
                    .load(notificationItem.avatar)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .apply(RequestOptions.placeholderOf(R.drawable.nav_user_svg))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.profilePic)
                /**this is the listener for profilePic */
                holder.profilePic.setOnClickListener {
                    notificationItem.isRead = true
                    holder.itemView.setBackgroundResource(R.color.white)
                    Log.d(
                        "followButton",
                        "Profile Image URL NotificationAdapter: ${notificationItem.avatar}"
                    )
                    val otherUsersProfile = OtherUsersProfile(
                        notificationItem.name,
                        notificationItem.name,
                        notificationItem.avatar,
                        notificationItem.owner,
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
//                    OtherUserProfile.openFromShorts(holder.itemView.context, otherUsersProfile)
                }
            }
            /**this is the comment REPLY VIEW HOLDER */
            is CommentReplyViewHolder -> {
                val fullMessage = notificationItem.notificationMessage
                // Limit the message to 40 characters
                val truncatedMessage = if (fullMessage.length > 40) {
                    "${fullMessage.substring(0, 40)}..."
                } else {
                    fullMessage
                }
//                holder.notificationMessage.text = truncatedMessage
                holder.notificationTime.text = notificationItem.notificationTime
                holder.username.text = notificationItem.name
                notificationItem.avatar

                if (notificationItem.avatar.isNotEmpty()) {
                    Log.w(
                        "NotificationAdapter",
                        "Avatar URL is empty or null for notification: $notificationItem"
                    )
                }
                /**profile pic here*/
                Glide.with(holder.itemView.context)
                    .load(notificationItem.avatar)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .apply(RequestOptions.placeholderOf(R.drawable.nav_user_svg))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.profilePic)
                /**profile Pic listener is here*/
                holder.profilePic.setOnClickListener {
                    Log.d(
                        "OnCommentProfile",
                        "Profile Image URL OnCommentProfile: ${notificationItem.avatar}"
                    )
                    notificationItem.isRead = true
                    holder.itemView.setBackgroundResource(R.color.white)
                    val otherUsersProfile = OtherUsersProfile(
                        notificationItem.name,
                        notificationItem.name,
                        notificationItem.avatar,
                        notificationItem.owner,
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
                   // UserProfileAccount.openFromShorts(holder.itemView.context, otherUsersProfile)
                }
            }
            /**This is the function for favorite */
            is FavoriteViewHolder -> {
                val fullMessage = notificationItem.notificationTime
                val truncatedMessage = if (fullMessage.length > 40) {
                    "${fullMessage.substring(0, 40)}..."
                } else {
                    fullMessage
                }
                notificationItem.avatar
                holder.notificationMessage.text = notificationItem.notificationMessage
                holder.notificationTime.text = notificationItem.notificationTime
                holder.username.text = notificationItem.name
                Log.d(
                    "FavoriteListener",
                    "Profile Image URL FavoriteListener: ${notificationItem.avatar}"
                )
//                <-------user profile image --------->
                Glide.with(holder.itemView.context)
                    .load(notificationItem.avatar)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .apply(RequestOptions.placeholderOf(R.drawable.nav_user_svg))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.profilePic)
                //click listeners on the notification item
                holder.profilePic.setOnClickListener {
                    notificationItem.isRead = true
                    holder.itemView.setBackgroundResource(R.color.white)
                    Log.d(
                        "FavoriteListener",
                        "Profile Image URL FavoriteListener: ${notificationItem.avatar}"
                    )
                    val otherUsersProfile = OtherUsersProfile(
                        notificationItem.name,
                        notificationItem.name,
                        notificationItem.avatar,
                        notificationItem.owner,
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
                  //  UserProfileAccount.openFromShorts(holder.itemView.context, otherUsersProfile)
                }
            }
            /**This is Like Notification View Holder */
            is LikeNotificationViewHolder -> {
                val fullMessage = notificationItem.notificationTime
                val truncatedMessage = if (fullMessage.length > 40) {
                    "${fullMessage.substring(0, 40)}..."
                } else {
                    fullMessage
                }
                notificationItem.avatar
                holder.username.text = notificationItem.name
                holder.notificationTime.text = notificationItem.notificationTime
                if (notificationItem.avatar.isNotEmpty()) {
                    Log.w(
                        "LikeNotification",
                        "Avatar URL is empty or null for LikeNotification: $notificationItem"
                    )
                }
                /**this is the avatar retriever  */
                Glide.with(holder.itemView.context)
                    .load(notificationItem.avatar)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .apply(RequestOptions.placeholderOf(R.drawable.nav_user_svg))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.profilePic)
                /**this is the profile pic listener */
                holder.profilePic.setOnClickListener {
                    Log.d(
                        "OnCommentProfile",
                        "Profile Image URL OnCommentProfile: ${notificationItem.avatar}"
                    )
                    notificationItem.isRead = true
                    holder.itemView.setBackgroundResource(R.color.white)
                    val otherUsersProfile = OtherUsersProfile(
                        notificationItem.name,
                        notificationItem.name,
                        notificationItem.avatar,
                        notificationItem.owner,
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
                   // UserProfileAccount.openFromShorts(holder.itemView.context, otherUsersProfile)
                }
            }
            /**this is the function for comment view Holder*/
            is OnCommentViewHolder -> {
                val fullMessage = notificationItem.notificationMessage
                val truncatedMessage = if (fullMessage.length > 40) {
                    "${fullMessage.substring(0, 40)}..."
                } else {
                    fullMessage
                }
                holder.notificationMessage.text = truncatedMessage
                notificationItem.avatar
                holder.username.text = notificationItem.name
                holder.notificationMessage.text = truncatedMessage
                holder.notificationTime.text = notificationItem.notificationTime
                holder.username.text = notificationItem.name
                if (notificationItem.avatar.isNotEmpty()) {
                    Log.w(
                        "OnCommentNotification",
                        "Avatar URL is empty or null for OnCommentNotification: $notificationItem"
                    )
                }
                /**this is the avatar retriever */
                Glide.with(holder.itemView.context)
                    .load(notificationItem.avatar)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .apply(RequestOptions.placeholderOf(R.drawable.nav_user_svg))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.profilePic)
                /**this is the profile pic for the listener */
                holder.profilePic.setOnClickListener {
                    notificationItem.isRead = true
                    EventBus.getDefault().post(NotificationRead(true, notificationItem._id))
                    holder.itemView.setBackgroundResource(R.color.white)
                    val otherUsersProfile = OtherUsersProfile(
                        notificationItem.name,
                        notificationItem.name,
                        notificationItem.avatar,
                        notificationItem.owner,
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
                 //   UserProfileAccount.openFromShorts(holder.itemView.context, otherUsersProfile)
                    EventBus.getDefault().post(NotificationRead(true, notificationItem._id))
                }
            }


        }
    }

    private fun toggleSelection(position: Int) {
        if (selectedItems.contains(position)) {
            selectedItems.remove(position)
        } else {
            selectedItems.add(position)
        }
        notifyItemChanged(position)
    }

    /**code not used */

    @SuppressLint("ResourceType")
    private fun showPopupMenu(view: View?, position: Int, notification: INotification) {
        if (view == null) return
        val popupMenu = PopupMenu(view.context, view, Gravity.END)
        popupMenu.inflate(R.menu.notification_menu_options)
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener { item ->

            when (item.itemId) {
                R.id.action_mark_as_read -> {
                    // Handle mark as read action
                    handleMarkAsdRead(position)
                    EventBus.getDefault().post(NotificationRead(true, notifications[position]._id))
                    true
                }

                R.id.action_mark_as_unread -> {
                    // Handle mark as Unread action
                    handleMarkUnread(position)
                    EventBus.getDefault().post(NotificationRead(false, notifications[position]._id))
                    true
                }

                R.id.delete -> {
                    // Handle remove notifications action
//                    notificationActionListener.removeNotification(notification)
                    handleRemoveNotifications(position, notification)
                    true
                }

                R.id.action_report -> {
                    // Handle _report action
                    val intent = Intent(view.context, ReportNotificationActivity2::class.java)
                    intent.putExtra("position", position)
                    view.context.startActivity(intent)
                    Toast.makeText(view.context, "Report has been sent", Toast.LENGTH_SHORT).show()
                    true
                }

                R.id.priority -> {
                    // handle priority action
                    handlePriority(position)
                    true
                }

                else -> {
                    false
                }
            }
        }
    }

    /**code not used */
    private fun handleMarkUnread(position: Int) {
        notifications[position].isRead = false
        notifyItemChanged(position)
    }

    private fun handlePriority(position: Int) {
        val intent = Intent().apply {
            Log.d("handlePriority", "settings ar available $position")
            action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            putExtra(Settings.EXTRA_APP_PACKAGE, "com.uyscut.flashdesign")
            putExtra("EXTRA_NOTIFICATION_POSITION", position)

        }
        startActivity(intent)
    }

    private fun startActivity(intent: Intent) {

    }

    private fun handleReport(position: Int) {
        notifyItemChanged(position)
    }

    private fun handleMarkAsdRead(position: Int) {
        notifications[position].isRead = true
        notifyItemChanged(position)
    }

    /** has been passed to the dayAdapter */
    fun handleRemoveNotifications(position: Int, notification: INotification) {
        notificationActionListener.removeNotification(notification)
        notifications.remove(notification)
        notifyItemRemoved(position)
    }

    /** Determine the view type based on the link type (assuming linkType is a string)*/
    override fun getItemViewType(position: Int): Int {
        val notificationItem = notifications[position]
        return when (notificationItem.link) {
            "text" -> NOTIFICATION_TYPE_WITH_TEXT
            "image" -> NOTIFICATION_TYPE_WITH_IMAGE
            "unfollowed" -> NOTIFICATION_TYPE_WITH_FOLLOW
            "unfollowed" -> NOTIFICATION_TYPE_WITH_UNFOLLOW
            "onCommentReply" -> NOTIFICATION_TYPE_REPLY_COMMENT
            "postBooked" -> NOTIFICATION_TYPE_FAVORITE
            "onLiked" -> NOTIFICATION_TYPE_LIKE
            "onComment" -> NOTIFICATION_TYPE_COMMENT
            "friendSuggestion" -> NOTIFICATION_TYPE_FRIEND_SUGGESTION
            else -> {
                Log.w("NotificationsAdapter", "Unknown link type: ${notificationItem.link}")
                NOTIFICATION_TYPE_WITH_TEXT
            }
        }
    }


    fun addIsFollowingData(isFollowingData: List<ShortsEntityFollowList>) {
//      val startPosition = followingData.size
        val followingData = null
        followingData?.addAll(isFollowingData)
//        android.util.Log(, "addIsFollowingData: $isFollowingData")
    }
}

/**these are the class view holder */
class TextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val notificationMessage: TextView = itemView.findViewById(R.id.notificationMessage)
    val username: TextView = itemView.findViewById(R.id.username)
    val profilePic: ImageView = itemView.findViewById(R.id.profilePic1)
}

class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val notificationMessage: TextView = itemView.findViewById(R.id.notificationMessage)
    val username: TextView = itemView.findViewById(R.id.username)
    val profilePic: ImageView = itemView.findViewById(R.id.profilePic1)
}

class FollowViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val notificationMessage: TextView = itemView.findViewById(R.id.notificationMessage)
    val username: TextView = itemView.findViewById(R.id.username)
    val profilePic: ImageView = itemView.findViewById(R.id.profilePic1)
}

class UnfollowViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val notificationMessage: TextView = itemView.findViewById(R.id.notificationMessageUnfollow)
    val username: TextView = itemView.findViewById(R.id.username)
    val profilePic: ImageView = itemView.findViewById(R.id.profilePic1)
    val notificationTime: TextView = itemView.findViewById(R.id.notificationTime)
}

class CommentReplyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val notificationMessage: TextView = itemView.findViewById(R.id.notificationMessage)
    val profilePic: ImageView = itemView.findViewById(R.id.profilePic1)
    val username: TextView = itemView.findViewById(R.id.username)
    val notificationTime: TextView = itemView.findViewById(R.id.notificationTime)
}

class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val notificationMessage: TextView = itemView.findViewById(R.id.notificationMessage1)
    val profilePic: ImageView = itemView.findViewById(R.id.profilePic1)
    val username: TextView = itemView.findViewById(R.id.username)
    val notificationTime: TextView = itemView.findViewById(R.id.notificationTime)
}

class LikeNotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val notificationMessage: TextView = itemView.findViewById(R.id.notificationMessage)
    val profilePic: ImageView = itemView.findViewById(R.id.profilePic1)
    val username: TextView = itemView.findViewById(R.id.username)
    val notificationTime: TextView = itemView.findViewById(R.id.notificationTime)
}

class OnCommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val notificationMessage: TextView = itemView.findViewById(R.id.notificationMessage)
    val profilePic: ImageView = itemView.findViewById(R.id.profilePic1)
    val username: TextView = itemView.findViewById(R.id.username)
    val notificationTime: TextView = itemView.findViewById(R.id.notificationTime)
}

class FriendSuggestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


}