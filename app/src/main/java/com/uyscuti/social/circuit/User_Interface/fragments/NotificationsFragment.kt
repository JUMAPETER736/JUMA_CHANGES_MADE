package com.uyscuti.social.circuit.User_Interface.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room

import com.uyscuti.social.circuit.MainActivity
import com.uyscuti.social.circuit.PostDetailsActivity2
//import com.uyscuti.social.circuit.PostDetailsActivity2
import com.uyscuti.social.circuit.adapter.feed.NotificationViewPagerAdapter
import com.uyscuti.social.circuit.adapter.notifications.NotificationDayAdapter
import com.uyscuti.social.circuit.data.model.shortsmodels.OtherUsersProfile
import com.uyscuti.social.circuit.model.ShortsFollowButtonClicked
import com.uyscuti.social.circuit.model.notificatioRead.NotificationRead
import com.uyscuti.social.circuit.model.notifications_data_class.CommentNotification
import com.uyscuti.social.circuit.model.notifications_data_class.FavoriteBookMarkNotification
import com.uyscuti.social.circuit.model.notifications_data_class.FollowNotification
import com.uyscuti.social.circuit.model.notifications_data_class.FriendSuggestionReq
import com.uyscuti.social.circuit.model.notifications_data_class.INotification
import com.uyscuti.social.circuit.model.notifications_data_class.LikeNotification
import com.uyscuti.social.circuit.model.notifications_data_class.Notification
import com.uyscuti.social.circuit.model.notifications_data_class.NotificationByDay
import com.uyscuti.social.circuit.model.notifications_data_class.ReplyCommentNotification
import com.uyscuti.social.circuit.model.notifications_data_class.UnfollowNotification
import com.uyscuti.social.circuit.viewmodels.FollowUnfollowViewModel
import com.uyscuti.social.circuit.viewmodels.notificationViewModel.NotificationViewModel
import com.uyscuti.social.chatsuit.utils.DateFormatter
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.adapter.notifications.NotificationsAdapter
import com.uyscuti.social.core.common.data.room.database.NotificationDataBase
import com.uyscuti.social.core.common.data.room.entity.NotificationEntity
import com.uyscuti.social.core.pushnotifications.socket.chatsocket.social.FlashNotificationsEvents
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage

import dagger.hilt.android.AndroidEntryPoint
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.IOException
import java.net.URISyntaxException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import javax.inject.Inject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [NotificationsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

@AndroidEntryPoint
class NotificationsFragment : Fragment(), NotificationsAdapter.NotificationActionListener {
    val adapter: NotificationViewPagerAdapter by lazy {
        NotificationViewPagerAdapter(childFragmentManager, lifecycle)
    }
    private var onItemSelectedListener: ((Boolean) -> Unit)? = null

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    //<---------------new code updated----------------------->
    @Inject
    lateinit var retrofitInterface: RetrofitInstance
    lateinit var localStorage: LocalStorage
    private val followUnFollowViewModel: FollowUnfollowViewModel by viewModels()
    private lateinit var dayNotificationsAdapter: NotificationDayAdapter
    private lateinit var nestedRecyclerView: RecyclerView
    private lateinit var notificationDataBase: NotificationDataBase
    private val notificationViewModel: NotificationViewModel by activityViewModels()
    private lateinit var notificationsAdapter: NotificationsAdapter
    private lateinit var socket: Socket

    private val processedNotificationIds = ArrayList<String>()

    //    private val apiService = RetrofitInstance
    private var notifications = mutableListOf(
        NotificationByDay(
            "Today",
            "text",
            arrayListOf(
                Notification("username", "This is a text notification", "text", "1hr", "", "", ""),
                Notification(
                    "username2",
                    "This is a text notification",
                    "text",
                    "2hrs",
                    "",
                    "",
                    ""
                ),
                Notification(
                    "username2, username4, username5",
                    "Started following you",
                    "follow",
                    "2hrs", "", "", ""
                ),
                Notification("username3", "Started following you", "follow", "3hrs", "", "", "")
            )
        ),
        NotificationByDay(
            "Yesterday",
            "video",
            arrayListOf(
                Notification(
                    "username, username4, username5",
                    "Started following you",
                    "image",
                    "", "", "", ""
                ),
                Notification("username", "This is a video notification", "video", "", "", "", ""),
                Notification(
                    "username2, username4, username5",
                    "This is a text notification",
                    "text",
                    "", "", "", ""
                ),
                Notification("username3", "This is a video notification", "video", "", "", "", "")
            )
        ),
        NotificationByDay(
            "7 days ago",
            "image",
            arrayListOf(
                Notification("username", "Started following you", "follow", "", "", "", ""),
                Notification("username2", "This is an image notification", "image", "", "", "", ""),
                Notification(
                    "username2, username4, username5",
                    "Started following you",
                    "follow",
                    "", "", "", ""
                ),
                Notification("username3", "This is text notification", "text", "", "", "", "")
            )
        ),
        NotificationByDay(
            "30 days ago",
            "follow",
            arrayListOf(
                Notification(

                    "username, username4, username5",
                    "This is a follow notification",
                    "follow",
                    "", "", "", ""
                ),
                Notification(

                    "username2, username4, username5",
                    "This is a text notification",
                    "text",
                    "", "", "", ""
                ),
                Notification("username2", " an image notification", "image", "", "", "", ""),
                Notification(
                    "username2",
                    "This is a follow notification",
                    "follow",
                    "",
                    "",
                    "",
                    ""
                ),
                Notification(
                    "username2",
                    "This is a follow notification",
                    "follow",
                    "",
                    "",
                    "",
                    ""
                ),
                Notification("username3", "text notification", "text", "", "", "", ""),
                Notification(
                    "username3, username4, username5",
                    "text notification",
                    "text",
                    "",
                    "",
                    "",
                    ""
                ),
                Notification("username3", "Text notification", "text", "", "", "", ""),
                Notification("username3", "text notification", "text", "", "", "", ""),
                Notification(
                    "username3, username4, username5",
                    "This is an image notification",
                    "text",
                    "", "", "", ""
                ),
                Notification("username3", "This is an image notification", "text", "", "", "", ""),
                Notification("username3", "This is an image notification", "text", "", "", "", ""),
                Notification("username3", "This is an image notification", "text", "", "", "", ""),
                Notification(
                    "username2",
                    "This is a follow notification",
                    "follow",
                    "",
                    "",
                    "",
                    ""
                ),
                Notification(
                    "username4, username5",
                    "This is a follow notification",
                    "text",
                    "",
                    "",
                    "",
                    ""
                ),
                Notification("username2", "This is a follow notification", "image", "", "", "", ""),
                Notification("username2", "This is a follow notification", "image", "", "", "", ""),
                Notification(
                    "username3",
                    "This is a follow notification",
                    "follow",
                    "",
                    "",
                    "",
                    ""
                ),

                Notification("username5", "Recently someone has unfollowed", "", "", "", "", "")
            )
        )
    )

    //<-----------------added a socket function ----------------------->

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            try {
                socket = IO.socket("http://192.168.1.103:8080")

            } catch (e: URISyntaxException) {
                e.printStackTrace()
            }
            socket.connect()
//            fetchNotifications()
        }
//        notificationViewModel = ViewModelProvider(requireActivity())[NotificationViewModel::class.java]


        notificationDataBase = Room.databaseBuilder(
            requireContext().applicationContext,
            NotificationDataBase::class.java, "notifications"
        ).build()
        // Initialize the adapter

//        fun createNotificationChannel() {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                val name = HIGH_CHANNEL_NAME
//                val descriptionText = "Important notifications"
//                val importance = NotificationManager.IMPORTANCE_HIGH
//                val channel = NotificationChannel(HIGH_CHANNEL_ID, name, importance).apply {
//                    description = descriptionText
//                }
//                val notificationManager: NotificationManager =
//                    requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//                notificationManager.createNotificationChannel(channel)
//            }
//        }
    }

    // fragment code added here
    @SuppressLint("MissingInflatedId", "CutPasteId")
    @OptIn(UnstableApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        EventBus.getDefault().register(this)
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_notifications, container, false)
        (activity as? MainActivity)?.showAppBar()
        activity?.window?.statusBarColor = ContextCompat.getColor(requireContext(), R.color.white)
        // Set the navigation bar color dynamically
        activity?.window?.navigationBarColor =
            ContextCompat.getColor(requireContext(), R.color.white)
        notifications.clear()
        nestedRecyclerView = view.findViewById(R.id.nestedRecyclerView)
        nestedRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        dayNotificationsAdapter = NotificationDayAdapter(notifications, this)
        nestedRecyclerView.adapter = dayNotificationsAdapter
        val notifications = notificationViewModel.notifications.value
        if (notifications != null) {
            Log.d("notificationViewModel", "notificationDay itemCount : $notifications")
            val dayNotifications = NotificationByDay("Today", "", notifications.asReversed())
            dayNotificationsAdapter.addNotifications(arrayListOf(dayNotifications))
            dayNotificationsAdapter.addNotification(dayNotifications)
             // Ensure this method initializes the socket
        } else {
            getMyUnifiedNotifications()
        }
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeMainNotificationToDelete()
    }



    @OptIn(UnstableApi::class)
    private fun getAllCommentNotification() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = retrofitInterface.apiService.getCommentNotification()
                if (response.isSuccessful) {
                    val notifications = response.body()?.data ?: emptyList()
                    val notes: ArrayList<INotification> = arrayListOf()
                    val notificationEntities: ArrayList<NotificationEntity> = arrayListOf()
                    val groupedNotification = mutableMapOf<String, MutableList<Notification>>()
                    Log.d("ApiService", "notifications: $notifications")
                    for (notification in notifications) {
//                        val dateLabel = getDateLabel(date = Date.from())
                        val createdAt = convertIso8601ToUnixTimestamp(notification.createdAt)
                        val date = Date(createdAt)
                        val formated = format(date)
//                        when(notification.type){
//                            "comment" -> {
//                            }
//                        }
                        val isLike = notification.message.contains("liked")
                        val isCommented = notification.message.contains("commented")
                        val isCommentReply = notification.message.contains("replied")


                        val isFollowed = notification.message.contains("started")
                        Log.d("CheckFollowed", "you are followed: $isFollowed")
                        val link = "onComment"
//                        val link :String = when{
//                            isCommented -> "onComment"
////                            isCommentReply -> "onCommentReplyComment"
//                            isLike -> "onLike"
//                            else -> ""
//                        }
//                        if (isCommented){
                        val note = CommentNotification(
                            notification.sender.username,
                            notification.message,
                            "onComment",
                            formated,
                            notification.sender.avatar.url,
                            notification._id,
                            notification.sender._id,
                            isRead = notification.read,
                            postId = notification.postId,
                            commentId = notification.commentId ?: ""
                        )
                        notes.add(note)
                        val notificationEntity = NotificationEntity(
                            _id = notification._id,
                            name = notification.sender.username,
                            notificationMessage = notification.message,
                            link = link,
                            notificationTime = formated,
                            avatar = notification.sender.avatar.url,
                            owner = notification.sender._id,
                            isRead = notification.read,
//                            colorState = notification.colorState

                        )
                        notificationEntities.add(notificationEntity)

//                        } else
//                            if (isLike) {
//                                Log.d("isLike","is working:$isLike")
//                                val note = LikeNotification(
//                                    notification.sender.username,
//                                    notification.message,
//                                    link,
//                                    formated,
//                                    notification.sender.avatar.url,
//                                    notification._id,
//                                    notification.sender._id,
//                                    isRead = notification.read,
//                                    likeId = notification.postId
//                                )
//                                notes.add(note)
//                            }
                    }
                    // Insert notifications into the database
                    notificationDataBase.notificationDao().insertNotifications(notificationEntities)
//                    val dayNotifications = NotificationByDay("Today", "follow", notes)
                    val dayNotifications = NotificationByDay("Today", "", notes)

                    Log.d("Api Service", "notificationDay itemCount : $dayNotifications")
                    // Update the UI with the notifications
                    withContext(Dispatchers.Main) {
                        dayNotificationsAdapter.addNotifications(arrayListOf(dayNotifications))
                        dayNotificationsAdapter.addNotification(dayNotifications)
                    }
                } else {
                    Log.e("ApiService", "failed to fetch successfully: ${response.message()}")
                    // Handle error response
                }
            } catch (e: IOException) {
                // Handle network error
                Log.e("ApiServices", "failed to fetch successfully ${e.message}")
            }
        }
    }
    @SuppressLint("Range")
    @OptIn(UnstableApi::class)
    private fun getMyUnifiedNotifications() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = retrofitInterface.apiService.getMyUnifiedNotifications()
                if (response.isSuccessful) {
                    val notifications = response.body()?.data ?: emptyList()
                    Log.d("NotificationApi", "getMyUnifiedNotifications: $notifications")
                    val notificationEntities: ArrayList<NotificationEntity> = arrayListOf()
                    val groupedNotifications = mutableMapOf<String, MutableList<INotification>>()
                    val notificationsByDate = mutableMapOf<String, MutableList<INotification>>()
                    val notes: ArrayList<INotification> = arrayListOf()
                    Log.d("ApiService", "notifications: $notifications")
                    for (notification in notifications) {
                        val createdAt = convertIso8601ToUnixTimestamp(notification.createdAt)
                        val date = Date(createdAt)
                        val formated = format(date)
                        val label = getDateLabel(date)
                        when (notification.type) {
                            "onCommentPost" -> {
                                Log.d("commentNotification", "commentNotification")
                                val note = CommentNotification(
                                    notification.sender.username,
                                    notification.message,
                                    "onComment",
                                    formated,
                                    notification.sender.avatar.url,
                                    notification._id,
                                    notification.sender._id,
                                    isRead = notification.read,
                                    postId = notification.data.postId,
                                    commentId = notification.data.commentId,
                                )
                                notes.add(note)
                            }

                            "postLiked" -> {
                                val note = LikeNotification(
                                    notification.sender.username,
                                    notification.message,
                                    "onLiked",
                                    formated,
                                    notification.sender.avatar.url,
                                    notification._id,
                                    notification.sender._id,
                                    isRead = notification.read,
                                    postId = notification.data.postId,
//                                    commentId = notification.data.commentId
                                )
                                notes.add(note)
                            }

                            "bookMarked" -> {
                                val note = FavoriteBookMarkNotification(
                                    notification.sender.username,
                                    notification.message,
                                    "postBooked",
                                    formated,
                                    notification.sender.avatar.url,
                                    notification._id,
                                    notification.sender._id,
                                    isRead = notification.read,
                                    postId = notification.data.postId,
                                    commentId = notification.data.commentId
                                )
                                notes.add(note)
                            }

                            "reply" -> {
                                val note = ReplyCommentNotification(
                                    notification.sender.username,
                                    notification.message,
                                    "onCommentReply",
                                    formated,
                                    notification.sender.avatar.url,
                                    notification._id,
                                    notification.sender._id,
                                    isRead = notification.read,
                                    replyId = notification.data.commentId,
                                    postId = notification.data.postId,
                                    commentId = notification.data.commentId
                                )
                                notes.add(note)
                            }

                            "followed" -> {

                                val note = FollowNotification(
                                    notification.sender.username,
                                    notification.message,
                                    "follow",
                                    formated,
                                    notification.sender.avatar.url,
                                    notification._id,
                                    notification.sender._id,
                                    isRead = notification.read,
                                    followId = "",
                                )
                                notes.add(note)
                            }

                            "unfollow" -> {
                                val note = UnfollowNotification(
                                    notification.sender.username,
                                    notification.message,
                                    "unfollowed",
                                    formated,
                                    notification.sender.avatar.url,
                                    notification._id,
                                    notification.owner,
                                    isRead = notification.read,
                                    unfollowId = "",
                                )
                                notes.add(note)
                            }

                            "friendSuggestions" -> {
                                val note = FriendSuggestionReq(
                                    ownerId = notification.owner,
                                    suggestedUserId = notification.sender._id,
                                    notification.sender.username,
                                    notification.message,
                                    "friendSuggestion",
                                    formated,
                                    notification.sender.avatar.url,
                                    notification._id,
                                    notification.sender._id,
                                    isRead = notification.read,
                                )
                                notes.add(note)
                            }



                            else -> null
                        }
                        lifecycleScope.launch {
                            notificationViewModel.setNotifications(notes)
                        }
                        val dayNotifications = NotificationByDay("Today", "", notes)
//                      notificationDataBase.notificationDao().insertNotifications(notificationEntities)
                        withContext(Dispatchers.Main) {
                            dayNotificationsAdapter.addNotifications(arrayListOf(dayNotifications))
//                            dayNotificationsAdapter.addNotification(dayNotifications)
                        }
                    }
                } else {
                    Log.e("ApiService", "failed to fetch successfully: ${response.message()}")
                    // Handle error response
                }


            } catch (e: IOException) {
                // Handle network error
                Log.e("ApiServices", "failed to fetch successfully ${e.message}")
            }
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handleFollowButtonClick(event: ShortsFollowButtonClicked) {
        val tag = "handleFollowButtonClick"
        android.util.Log.d(tag, "handleFollowButtonClick: inside")
        val connectivityManager =
            requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        followUnFollowViewModel.followUnFollow(event.followUnFollowEntity.userId)
    }
    //<------------------added a notification read function ------------------------>
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handleNotificationRead(event: NotificationRead) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val readNotificationResponse =
                    retrofitInterface.apiService.markNotificationRead(event.notificationId)
                if (readNotificationResponse.isSuccessful) {
                    // Assuming the response body contains the updated notification item
                    val newNotification = readNotificationResponse.body()
                    android.util.Log.d(
                        "NotificationAdapter",
                        "Mark as read notification: $newNotification"
                    )
                } else {
                    android.util.Log.e(
                        "NotificationAdapter",
                        "Failed to mark notification as read: ${
                            readNotificationResponse.errorBody()?.string()
                        }"
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e(
                    "NotificationAdapter",
                    "Exception while marking notification as read",
                    e
                )
            }
        }
    }

    //        shortsAdapter.updateBtn("")
    @SuppressLint("SimpleDateFormat")
    @OptIn(UnstableApi::class)
    private fun convertIso8601ToUnixTimestamp(iso8601Date: String): Long {
        if (iso8601Date.isEmpty()) {
            return 0L
        }
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val date = sdf.parse(iso8601Date)
            return date?.time ?: 0L
        } catch (e: ParseException) {
            e.printStackTrace()
            return 0L
        }
    }

    //<----------------------added an event bus ----------------------->
    @OptIn(UnstableApi::class)
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handleNewNotification(events: FlashNotificationsEvents) {
        Log.d("handleNewNotification", "notifications no working: ${events.notificationMessage}")
        val notes: ArrayList<INotification> = arrayListOf()
        val createdAt = convertIso8601ToUnixTimestamp(events.notificationTime)
        val date = Date(createdAt)
        val formated = format(date)
        val notificationEntities: ArrayList<NotificationEntity> = arrayListOf()
        val notificationDateLabel = getDateLabel(date)
        /**Check if this notification has already been processed*/
        if (events._id in processedNotificationIds) {
            Log.d("handleNewNotification", "Notification already processed: ${events._id}")
            return
        }
        processedNotificationIds.add(events._id)
        /** Add the notification ID to the set of processed IDs*/
        val type = when {
            events.type.contains("follow", ignoreCase = true) -> "follow"
            events.type.contains("like", ignoreCase = true) -> "like"
            events.type.contains("comment", ignoreCase = true) -> "comment"
            events.type.contains("commentReply", ignoreCase = true) -> "commentReply"
            else -> "unknown"
        }

        val note = when (events.type) {
            "postLiked" -> LikeNotification(
                events.name,
                events.notificationMessage,
                "onLiked",
                formated,
                events.avatar,
                events._id,
                events.owner,
                isRead = events.isRead,
                postId = events.postId,// Assuming link represents postId
//                commentId = events.commentId // Assuming link represents commentId
            )
            "onCommentPost" -> CommentNotification(
                events.name,
                events.notificationMessage,
                "onComment",
                formated,
                events.avatar,
                events._id,
                events.owner,
                isRead = events.isRead,
                postId = events.postId,
                commentId = events.commentId
                /**Assuming link represents commentId*/
                /**Assuming link represents commentId*/
            )
            "reply" -> ReplyCommentNotification(
                events.name,
                events.notificationMessage,
                "onCommentReply",
                formated,
                events.avatar,
                events._id,
                events.owner,
                isRead = events.isRead,
                replyId = "",// Assuming link represents replyId
                commentId = events.commentId,
                postId = events.postId
            )
            "followed" -> FollowNotification(
                events.name,
                events.notificationMessage,
                "follow",
                formated,
                events.avatar,
                events._id,
                events.owner,
                isRead = events.isRead,
                followId = events.link // Assuming link represents followId
            )
            "unfollow" -> UnfollowNotification(
                events.name,
                events.notificationMessage,
                "unfollowed",
                formated,
                events.avatar,
                events._id,
                events.owner,
                isRead = events.isRead,
                unfollowId = events.link // Assuming link represents followId
            )
            "bookMarked" -> FavoriteBookMarkNotification(
                events.name,
                events.notificationMessage,
                "postBooked",
                formated,
                events.avatar,
                events._id,
                events.owner,
                isRead = events.isRead,
                postId = events.postId, // Assuming link represents postId
                commentId = events.commentId // Assuming link represents commentId

            )
            else -> {
                Log.e(
                    "handleNewNotification",
                    "Unknown notification type: ${events.type}"
                )
                null
            }
        }

        if (note != null) {
            notes.add(note)
            val dayNotifications = NotificationByDay("Today", "", notes)
            dayNotificationsAdapter.addNotification(dayNotifications)

            nestedRecyclerView.scrollToPosition(0)
            lifecycleScope.launch {
                notificationViewModel.addNotification(note)
            }
        }
    }
//<------------------added another date format ------------------->
    private fun getDateLabel(date: Date): String {
        return when {
            DateFormatter.isToday(date) -> "Today"
            DateFormatter.isYesterday(date) -> "Yesterday"
//            DateFormatter.is (date, 7) -> "Within the last week"
            else -> "Older"
        }
    }
    private fun getType(notifications: ArrayList<Notification>): String {
        return notifications.firstOrNull()?.notificationTime ?: "text"
    }

//<------------------added another date format ------------------->
    fun format(date: Date): String {
        return when {
            DateFormatter.isToday(date) -> DateFormatter.format(date, DateFormatter.Template.TIME)
            DateFormatter.isYesterday(date) -> getString(R.string.date_header_yesterday)
            DateFormatter.isCurrentYear(date) -> DateFormatter.format(
                date,
                DateFormatter.Template.STRING_DAY_MONTH
            )

            else -> DateFormatter.format(date, DateFormatter.Template.STRING_DAY_MONTH_YEAR)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment NotificationsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NotificationsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    override fun onResume() {
        super.onResume()
        updateStatusBar()
    }
    private fun updateStatusBar() {
        val decor: View? = activity?.window?.decorView
        decor?.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

    }

    override fun onGetLayoutInflater(
        savedInstanceState: Bundle?
    ): LayoutInflater {
        // Use a custom theme for the fragment layout
        return super.onGetLayoutInflater(savedInstanceState).cloneInContext(
            ContextThemeWrapper(
                requireContext(), R.style.DarkTheme
            )
        )
    }

    //<------------------added an on going event bus --------------------------->
    override fun onDestroy() {
        super.onDestroy()
        // Check if socket is initialized
        if (::socket.isInitialized) {
            socket.off("notification") // Only call off if socket is initialized
            socket.disconnect() // Only disconnect if socket is initialized
        }

        // Unregister from EventBus
        EventBus.getDefault().unregister(this)
    }

    override fun removeNotification(notification: INotification) {
        notificationViewModel.removeNotification(notification)
    }

    override fun onNotificationLongClick(notification: INotification) {
        notificationViewModel.incrementAndAddToSelectedNotifications(notification)
    }
    @OptIn(UnstableApi::class)
    override fun onClickSingleNotification(notification: INotification) {
        val selected = notificationViewModel.selectedNotificationCount.value ?: 0
        if (selected > 0) {
            notificationViewModel.incrementAndAddToSelectedNotifications(notification)
        } else {
            when (notification) {
                is ReplyCommentNotification -> {
                    val postId = notification.postId
                    val replyId = notification.replyId
                    val commentId = notification.commentId
                    val intent =
                        Intent(requireActivity(), PostDetailsActivity2::class.java).apply {
                            putExtra("replyId", replyId)
                            putExtra("comment_id", commentId)
                            putExtra("post_id", postId)
                        }
                    startActivity(intent)
                }

                is FavoriteBookMarkNotification -> {
                    android.util.Log.d("FavoriteProfile", "listener is working ")
                    val postId = notification.postId
                    val intent =
                        Intent(requireActivity(), PostDetailsActivity2::class.java).apply {
                            putExtra("post_id", postId)
                            putExtra("show_comments", true)
                        }
                    startActivity(intent)
                    EventBus.getDefault().post(NotificationRead(true, notification._id))
                }

                is LikeNotification -> {
                    val postId = notification.postId
                    val intent = Intent(requireActivity(), PostDetailsActivity2::class.java).apply {
                        putExtra("post_id", postId)
                        putExtra("show_comments", true)
                    }
                    EventBus.getDefault().post(NotificationRead(true, notification._id))
                    startActivity(intent)
                }

                is CommentNotification -> {
                    android.util.Log.d("OnCommentNotification", "listener is working ")
                    val postId = notification.postId
                    val commentId = notification.commentId
                    val intent = Intent(requireActivity(), PostDetailsActivity2::class.java).apply {
                        putExtra("post_id", postId)
                        putExtra("comment_id", commentId)
                        putExtra("show_comments", true)
                    }
                    EventBus.getDefault().post(NotificationRead(true, notification._id))
                    startActivity(intent)
                }

                is FollowNotification -> {
                    val otherUsersProfile = OtherUsersProfile(
                        notification.name,
                        notification.name,
                        notification.avatar,
                        notification.owner,
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
                    EventBus.getDefault().post(NotificationRead(true, notification._id))
                  //  UserProfileAccount.openFromShorts(requireContext(), otherUsersProfile)
                }

                is UnfollowNotification -> {
                    val otherUsersProfile = OtherUsersProfile(
                        notification.name,
                        notification.name,
                        notification.avatar,
                        notification.owner,
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
                    EventBus.getDefault().post(NotificationRead(true, notification._id))
                   // UserProfileAccount.openFromShorts(requireContext(), otherUsersProfile)
                }
            }
        }
    }
    override fun onRemoveNotificationClick(notification: INotification) {
        notificationViewModel.decrementAndRemoveFromSelectedNotifications(notification)
    }

    @SuppressLint("Range", "NotifyDataSetChanged")
    @OptIn(UnstableApi::class)
    private fun observeMainNotificationToDelete() {
        notificationViewModel.deleteSelectedList.observe(viewLifecycleOwner) { deletedNotes ->
            adapter.notifyDataSetChanged()
        }
    }
}




