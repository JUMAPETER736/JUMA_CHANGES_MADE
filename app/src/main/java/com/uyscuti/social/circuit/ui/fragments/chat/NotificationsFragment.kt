package com.uyscuti.social.circuit.ui.fragments.chat

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.uyscuti.sharedmodule.adapter.notifications.NotificationsAdapter
import com.uyscuti.sharedmodule.ui.fragments.feed.feedviewfragments.Fragment_Original_Post_With_Repost_Inside
import com.uyscuti.sharedmodule.ui.fragments.feed.feedviewfragments.Fragment_Original_Post_Without_Repost_Inside
import com.uyscuti.sharedmodule.utils.DialogUtils
import com.uyscuti.sharedmodule.viewmodels.feed.FeedNotificationViewModel
import com.uyscuti.sharedmodule.viewmodels.feed.FeedNotificationViewModelFactory
import com.uyscuti.social.circuit.PostDetailsActivity2
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.user_interface.userProfile.MyUserProfileAccount
import com.uyscuti.social.core.pushnotifications.socket.chatsocket.social.FlashNotificationsEvents
import com.uyscuti.social.network.api.response.getUnifiedNotification.Avatar
import com.uyscuti.social.network.api.response.getUnifiedNotification.DataX
import com.uyscuti.social.network.api.response.getUnifiedNotification.FeedNotification
import com.uyscuti.social.network.api.response.getUnifiedNotification.Sender
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage
import com.uyscuti.social.notifications.feed.FeedNotificationImplementation
import com.uyscuti.social.notifications.feed.FeedNotificationRepo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 * Use the [NotificationsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val TAG = "NotificationsFragment"

@AndroidEntryPoint
class NotificationsFragment : Fragment() {


    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    @Inject
    lateinit var retrofitInterface: RetrofitInstance

    @Inject
    lateinit var localStorage: LocalStorage

    private lateinit var notificationsAdapter: NotificationsAdapter
    private lateinit var nestedRecyclerView: RecyclerView
    private lateinit var progressBarLayout: LinearLayout
    private lateinit var feedRepo: FeedNotificationRepo
    private lateinit var feedNotificationViewModel: FeedNotificationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)

        }

        initViewModel()

    }

    private fun initViewModel() {
        feedRepo = FeedNotificationImplementation(retrofitInterface)
        val factory = FeedNotificationViewModelFactory(feedRepo)
        feedNotificationViewModel =
            ViewModelProvider(this, factory)[FeedNotificationViewModel::class.java]

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


        nestedRecyclerView = view.findViewById(R.id.nestedRecyclerView)
        progressBarLayout = view.findViewById(R.id.noteProgress)
        nestedRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        notificationsAdapter = NotificationsAdapter(
            onNotificationClick = { notification, position ->
                if (notification.data.`for` == "feed") {
                    handleFeedNotifications(notification, position)
                } else {
                    handleShortsNotification(notification, position)
                }
            },
            onLoadMore = {
                feedNotificationViewModel.loadNextPage()
            },
            onMarkAsRead = { notification, position ->
                feedNotificationViewModel.toggleReadStatus(notification._id)
                notificationsAdapter.updateNotificationReadStatus(
                    notification._id,
                    !notification.read
                )
            },
            onDelete = { notification, position ->
                DialogUtils.showDeleteConfirmationDialog(
                    requireActivity(),
                    onConfirm = {
                        feedNotificationViewModel.deleteNotification(notification._id)
                        notificationsAdapter.removeNotification(notification._id)
                    }
                )
            }
        )

        nestedRecyclerView.adapter = notificationsAdapter

        observeViewModel()

        return view
    }

    @OptIn(UnstableApi::class)
    private fun goToPostDetailsActivity(
        notification: FeedNotification,
        showComments: Boolean,
        commentId: String? = null
    ) {
        val intent = Intent(requireActivity(), PostDetailsActivity2::class.java).apply {
            if (showComments) {
                putExtra("comment_id", commentId)
            } else {
                putExtra("comment_id", "")
            }
            putExtra("post_id", notification.data.postId)
            putExtra("showComments", showComments)
        }
        startActivity(intent)

    }

    @OptIn(UnstableApi::class)
    private fun handleFeedNotifications(notification: FeedNotification, position: Int) {
        //first change notification state from unread to read
        if (!notification.read) {
            markNotification(notification, position)
        }
        //launch associated activity
        progressBarLayout.isVisible = true
        lifecycleScope.launch {
            val response = retrofitInterface.apiService.getFeedPostById(notification.data.postId)
            if (response.isSuccessful) {
                progressBarLayout.isVisible = false
                val post = response.body()!!.data.data.posts.first()
                Log.d(TAG, "Post: $post")
                when (notification.type) {
                    "postLiked" -> {

                        val isLikedComment = notification.message.contains("comment", true)
                        if (isLikedComment) {
                            val isLikeReply = notification.message.contains("reply", true)

                            if (isLikeReply) {
                                navigateToFeedDetailsFragment(post)
                            } else {
                                navigateToFeedDetailsFragment(post)
                            }
                        } else {
                            navigateToFeedDetailsFragment(post)
                        }
                    }

                    "onCommentPost" -> {
                        val isCommentReply = notification.message.contains("replied", true)
                        if (isCommentReply) {
                            navigateToFeedDetailsFragment(post)
                        } else {
                            navigateToFeedDetailsFragment(post)
                        }
                    }

                    "followed" -> {
                        goUserProfileActivity()
                    }

                    else -> "No such type of notification"
                }
            } else {
                progressBarLayout.isVisible = false
                Toast.makeText(requireActivity(), "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun navigateToFeedDetailsFragment(data: com.uyscuti.social.network.api.response.posts.Post) {
        if (data.isReposted) {
            navigateToOriginalPostWithRepostInside(data)
        } else {
            navigateToOriginalPostWithoutRepostInside(data)
        }
    }

    @OptIn(UnstableApi::class)
    private fun handleShortsNotification(notification: FeedNotification, position: Int) {
        if (!notification.read) {
            markNotification(notification, position)
        }

        when (notification.type) {
            "postLiked" -> {
                val isLikeComment = notification.message.contains("comment", true)
                if (isLikeComment) {
                    val isLikeReply = notification.message.contains("reply", true)
                    if (isLikeReply) {
                        goToPostDetailsActivity(
                            notification,
                            true,
                            notification.data.commentReplyId
                        )
                    } else {
                        goToPostDetailsActivity(notification, true, notification.data.commentId)
                    }
                } else {
                    goToPostDetailsActivity(notification, false)
                }
            }

            "onCommentPost" -> {
                val isCommentReply = notification.message.contains("replied", true)
                if (isCommentReply) {
                    goToPostDetailsActivity(notification, true, notification.data.commentReplyId)
                } else {
                    goToPostDetailsActivity(notification, true, notification.data.commentId)
                }

            }

            "followed" -> {
                goUserProfileActivity()
            }

            else -> "No such type of notification"
        }

    }

    private fun markNotification(notification: FeedNotification, position: Int) {
        feedNotificationViewModel.markAsRead(notification._id)
        notificationsAdapter.notifyItemChanged(position)
    }

    @OptIn(UnstableApi::class)
    private fun goUserProfileActivity() {
        val intent = Intent(requireActivity(), MyUserProfileAccount::class.java)
        requireActivity().startActivity(intent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            feedNotificationViewModel.state.collect { state ->
                when {
                    state.isLoading && state.notifications.isEmpty() -> {
//                        linearLayout.visibility = View.VISIBLE
//                        businessRecyclerView.visibility = View.GONE

                    }

                    state.notifications.isNotEmpty() -> {
//                        linearLayout.visibility = View.GONE
//                        businessRecyclerView.visibility = View.VISIBLE
                        // Update adapter
                        if (state.currentPage == 1) {
                            // First page - submit entire list
                            notificationsAdapter.submitListSafe(
                                state.notifications,
                                state.hasNextPage,
                                nestedRecyclerView
                            )
                            notificationsAdapter.setHasMorePages(state.hasNextPage)
                        } else {
                            // Subsequent pages - this shouldn't be needed
                            // because we're submitting the full list each time
                            notificationsAdapter.submitListSafe(
                                state.notifications,
                                state.hasNextPage,
                                nestedRecyclerView
                            )
                            notificationsAdapter.setHasMorePages(state.hasNextPage)
                        }
                    }

                    state.error != null -> {
                        // Show error
                        //  notificationsAdapter.onLoadFailed()
                    }
                }
            }
        }
    }

    //<----------------------added an event bus ----------------------->
    @OptIn(UnstableApi::class)
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handleNewNotification(events: FlashNotificationsEvents) {

        if (events.noteFor != "business") {
            val avatar = Avatar(
                "",
                "",
                events.avatar
            )

            val sender = Sender(
                "",
                avatar,
                "",
                events.name
            )

            val postId = DataX(
                events.postId,
                events.noteFor
            )

            val notification = FeedNotification(
                events._id,
                events.avatar,
                events.notificationTime,
                postId,
                events.notificationMessage,
                events.owner,
                events.isRead,
                sender,
                events.type
            )

            notificationsAdapter.addNewNotification(notification)
            nestedRecyclerView.scrollToPosition(0)

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
    }

    private fun navigateToOriginalPostWithRepostInside(originalPostData: com.uyscuti.social.network.api.response.posts.Post) {
        try {
            val fragment = Fragment_Original_Post_With_Repost_Inside.newInstance(originalPostData)
            navigateToFragment(fragment, "repost_with_context")
        } catch (e: Exception) {
            Log.e(tag, "Error navigating to repost fragment: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun navigateToOriginalPostWithoutRepostInside(data: com.uyscuti.social.network.api.response.posts.Post) {
        try {
            Log.d(TAG, "Navigating to original Post for Post ID: ${data._id}")

            val firstName = data.author.firstName
            val lastName = data.author.lastName
            val displayName = when {
                firstName.isNotBlank() && lastName.isNotBlank() -> "$firstName $lastName"
                firstName.isNotBlank() -> firstName
                lastName.isNotBlank() -> lastName
                else -> data.author.account.username
            }

            val fragment = Fragment_Original_Post_Without_Repost_Inside().apply {
                arguments = Bundle().apply {
                    putString(
                        Fragment_Original_Post_Without_Repost_Inside.ARG_ORIGINAL_POST,
                        Gson().toJson(data)
                    )
                    putString("post_id", data._id)
                    // putInt("adapter_position", absoluteAdapterPosition)
                    putString("navigation_source", "feed_mixed_files")
                    putLong("navigation_timestamp", System.currentTimeMillis())

                    putString("author_name", displayName)
                    putString("author_username", data.author?.account?.username ?: "unknown_user")
                    putString("author_profile_image_url", data.author?.account?.avatar?.url ?: "")
                    putString("user_id", data.author?._id ?: "")

                    Log.d(
                        TAG,
                        "Author Info - Name: $displayName, Username: ${data.author?.account?.username}, ID: ${data.author?._id}"
                    )
                }
            }

            navigateToFragment(fragment, "original_post_without_repost")

        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to original post fragment: ${e.message}", e)
        }
    }

    private fun navigateToFragment(fragment: Fragment, tag: String) {
        try {
            val activity = requireActivity()
            activity.supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    com.uyscuti.sharedmodule.R.anim.slide_in_right,
                    com.uyscuti.sharedmodule.R.anim.slide_out_left,
                    com.uyscuti.sharedmodule.R.anim.slide_in_left,
                    com.uyscuti.sharedmodule.R.anim.slide_out_right
                )
                .replace(android.R.id.content, fragment, tag)
                .addToBackStack(tag)
                .commit()

            Log.d(TAG, "Successfully navigated to fragment: $tag")
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to fragment: $tag", e)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        // Unregister from EventBus
        EventBus.getDefault().unregister(this)
    }

}