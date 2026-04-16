package com.uyscuti.sharedmodule.adapter.notifications

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uyscuti.sharedmodule.R
import com.uyscuti.social.network.api.response.getUnifiedNotification.FeedNotification
import java.util.Calendar


class NotificationsAdapter(
    private val onNotificationClick: (FeedNotification, Int) -> Unit,
    private val onLoadMore: () -> Unit,
    private val onMarkAsRead: (FeedNotification, Int) -> Unit,
    private val onDelete: (FeedNotification, Int) -> Unit
):RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<NotificationItem>()
    private var isLoading = false
    private var hasMorePages = true

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_NOTIFICATION = 1
        private const val VIEW_TYPE_LOADING = 2
        private const val VIEW_TYPE_NO_DATA = 3
    }

    // Add more notifications for pagination
    fun addMoreNotifications(newNotifications: List<FeedNotification>) {
        // Remove loading indicator if present
        val loadingIndex = items.indexOfFirst { it is NotificationItem.Loading }
        if (loadingIndex != -1) {
            items.removeAt(loadingIndex)
            notifyItemRemoved(loadingIndex)
        }

        isLoading = false

        if (newNotifications.isEmpty()) {
            hasMorePages = false
            return
        }

        val startPosition = items.size
        val groupedItems = groupNotificationsByDate(newNotifications)

        // Merge with existing items (avoid duplicate headers)
        for (item in groupedItems) {
            if (item is NotificationItem.Header) {
                // Check if this header already exists
                val existingHeader = items.find {
                    it is NotificationItem.Header && it.title == item.title
                }
                if (existingHeader == null) {
                    items.add(item)
                }
            } else {
                items.add(item)
            }
        }

        notifyItemRangeInserted(startPosition, items.size - startPosition)
    }

    // Add new notifications and group them by date
    fun submitList(newNotifications: List<FeedNotification>, hasMorePages: Boolean) {
        items.clear()
        isLoading = false

        if (newNotifications.isEmpty()) {
            notifyDataSetChanged()
            return
        }

        val groupedItems = groupNotificationsByDate(newNotifications)
        items.addAll(groupedItems)
        notifyDataSetChanged()
    }

    // Safe version that posts to avoid layout conflicts
    fun submitListSafe(newItems: List<FeedNotification>, hasMorePages: Boolean, recyclerView: RecyclerView) {
        recyclerView.post {
            submitList(newItems, hasMorePages)
        }
    }

    private fun loadMore() {
        if (isLoading || !hasMorePages || items.isEmpty()) return

        isLoading = true

        // Add loading indicator at the end
        items.add(NotificationItem.Loading)
        val position = items.size - 1
        notifyItemInserted(position)

        // Call the callback
        onLoadMore()
    }

    fun resetLoadingState() {
        isLoading = false
        val loadingIndex = items.indexOfFirst { it is NotificationItem.Loading }
        if (loadingIndex != -1) {
            items.removeAt(loadingIndex)
            notifyItemRemoved(loadingIndex)
        }
    }

    fun setHasMorePages(hasMore: Boolean) {
        hasMorePages = hasMore
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is NotificationItem.Header -> VIEW_TYPE_HEADER
            is NotificationItem.NotificationData -> VIEW_TYPE_NOTIFICATION
            is NotificationItem.Loading -> VIEW_TYPE_LOADING
            else -> VIEW_TYPE_NO_DATA
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val view = inflater.inflate(R.layout.item_notification_header, parent, false)
                HeaderViewHolder(view)
            }
            VIEW_TYPE_LOADING -> {
                val view = inflater.inflate(R.layout.item_loading, parent, false)
                LoadingViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_notification, parent, false)
                NotificationViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                val header = items[position] as NotificationItem.Header
                holder.bind(header)
            }
            is NotificationViewHolder -> {
                val notificationData = items[position] as NotificationItem.NotificationData
                holder.bind(notificationData.notification, position)
            }
            is LoadingViewHolder -> {
                // Loading indicator is already visible
            }
        }

        if (position >= items.size - 3 &&
            position > 0 &&
            items.size > 3 &&
            !isLoading &&
            hasMorePages) {
            holder.itemView.post {
                if (!isLoading && hasMorePages) {
                    loadMore()
                }
            }
        }

    }

    override fun getItemCount(): Int = items.size


    private fun groupNotificationsByDate(notifications: List<FeedNotification>): List<NotificationItem> {
        val result = mutableListOf<NotificationItem>()
        val sortedNotifications = notifications.sortedByDescending { it.getTimestamp() }

        // Get today's start (00:00:00)
        val todayCalendar = Calendar.getInstance()
        todayCalendar.set(Calendar.HOUR_OF_DAY, 0)
        todayCalendar.set(Calendar.MINUTE, 0)
        todayCalendar.set(Calendar.SECOND, 0)
        todayCalendar.set(Calendar.MILLISECOND, 0)
        val today = todayCalendar.timeInMillis

        // Get yesterday's start (00:00:00)
        val yesterdayCalendar = Calendar.getInstance()
        yesterdayCalendar.set(Calendar.HOUR_OF_DAY, 0)
        yesterdayCalendar.set(Calendar.MINUTE, 0)
        yesterdayCalendar.set(Calendar.SECOND, 0)
        yesterdayCalendar.set(Calendar.MILLISECOND, 0)
        yesterdayCalendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = yesterdayCalendar.timeInMillis

        // Get 7 days ago (for "This Week" - anything in the last 7 days)
        val sevenDaysAgoCalendar = Calendar.getInstance()
        sevenDaysAgoCalendar.set(Calendar.HOUR_OF_DAY, 0)
        sevenDaysAgoCalendar.set(Calendar.MINUTE, 0)
        sevenDaysAgoCalendar.set(Calendar.SECOND, 0)
        sevenDaysAgoCalendar.set(Calendar.MILLISECOND, 0)
        sevenDaysAgoCalendar.add(Calendar.DAY_OF_YEAR, -7)
        val sevenDaysAgo = sevenDaysAgoCalendar.timeInMillis

        var currentSection = ""

        for (notification in sortedNotifications) {
            val timestamp = notification.getTimestamp()

            val section = when {
                timestamp >= today -> "Today"
                timestamp >= yesterday -> "Yesterday"
                timestamp >= sevenDaysAgo -> "This Week"
                else -> "Older"
            }

            if (section != currentSection) {
                result.add(NotificationItem.Header(section))
                currentSection = section
            }

            result.add(NotificationItem.NotificationData(notification))
        }

        return result
    }

    // Add a single new notification (e.g., from real-time updates)
    fun addNewNotification(notification: FeedNotification) {
        val timestamp = notification.getTimestamp()

        // Determine which section this notification belongs to
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val today = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.add(Calendar.DAY_OF_YEAR, -6) // 7 days ago total
        val sevenDaysAgo = calendar.timeInMillis

        val section = when {
            timestamp >= today -> "Today"
            timestamp >= yesterday -> "Yesterday"
            timestamp >= sevenDaysAgo -> "This Week"
            else -> "Older"
        }

        // Find the position to insert
        var insertPosition = 0
        var headerExists = false

        // Check if the header for this section already exists
        for (i in items.indices) {
            val item = items[i]
            if (item is NotificationItem.Header && item.title == section) {
                headerExists = true
                insertPosition = i + 1
                break
            }
            if (item is NotificationItem.Header) {
                // If we found a different header, we need to insert before it
                insertPosition = i
                break
            }
        }

        // If no header exists, add it first
        if (!headerExists) {
            items.add(insertPosition, NotificationItem.Header(section))
            notifyItemInserted(insertPosition)
            insertPosition++
        }

        // Find correct position within the section (sorted by timestamp)
        var notificationPosition = insertPosition
        while (notificationPosition < items.size) {
            val item = items[notificationPosition]
            if (item is NotificationItem.Header) {
                // Reached next section, insert before it
                break
            }
            if (item is NotificationItem.NotificationData) {
                if (timestamp > item.notification.getTimestamp()) {
                    // Found the right position
                    break
                }
            }
            notificationPosition++
        }

        // Insert the notification
        items.add(notificationPosition, NotificationItem.NotificationData(notification))
        notifyItemInserted(notificationPosition)

    }

    // Remove a notification by ID
    fun removeNotification(notificationId: String) {
        val position = items.indexOfFirst {
            it is NotificationItem.NotificationData && it.notification._id == notificationId
        }

        if (position != -1) {
            items.removeAt(position)
            notifyItemRemoved(position)

            // Check if the section is now empty and remove header if needed
            removeEmptyHeaders()
        }
    }

    // Update notification read status
    fun updateNotificationReadStatus(notificationId: String, isRead: Boolean) {
        val position = items.indexOfFirst {
            it is NotificationItem.NotificationData && it.notification._id == notificationId
        }

        if (position != -1) {
            val item = items[position] as NotificationItem.NotificationData
            val updatedNotification = item.notification.copy(read = isRead)
            items[position] = NotificationItem.NotificationData(updatedNotification)
            notifyItemChanged(position)
        }
    }

    private fun removeEmptyHeaders() {
        val headersToRemove = mutableListOf<Int>()

        for (i in items.indices) {
            if (items[i] is NotificationItem.Header) {
                // Check if next item is another header or end of list
                val isLastItem = i == items.size - 1
                val nextIsHeader = !isLastItem && items[i + 1] is NotificationItem.Header

                if (isLastItem || nextIsHeader) {
                    headersToRemove.add(i)
                }
            }
        }

        // Remove headers in reverse order to maintain correct indices
        headersToRemove.reversed().forEach { index ->
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    // ViewHolder for Header
    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val headerText: TextView = itemView.findViewById(R.id.headerText)

        fun bind(header: NotificationItem.Header) {
            headerText.text = header.title
        }
    }

    // ViewHolder for Notification
    inner  class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.notificationTitle)
        private val messageText: TextView = itemView.findViewById(R.id.notificationMessage)
        private val timeText: TextView = itemView.findViewById(R.id.notificationTime)
        private val imageView: ImageView = itemView.findViewById(R.id.notificationImage)
        private val optionsMenu: ImageView = itemView.findViewById(R.id.optionsMenu)

        fun bind(notification: FeedNotification, position: Int) {

            setupNotification(notification)
            updateNotificationState(notification)
            itemView.setOnClickListener { onNotificationClick(notification, position) }
            optionsMenu.setOnClickListener { view ->
                showPopupMenu(view, notification,position)
            }
        }

        private fun setupNotification(item: FeedNotification) {
            titleText.text = item.sender.username
            messageText.text = item.message
            timeText.text = getTimeAgo(item.getTimestamp())

            Glide.with(imageView.context)
                .load(item.sender.avatar.url)
                .error(R.drawable.ic_person)
                .circleCrop()
                .into(imageView)
        }

        private fun updateNotificationState(notification: FeedNotification) {
            // Change background for unread notifications
            itemView.setBackgroundColor(
                if (notification.read)
                    Color.TRANSPARENT
                else
                    itemView.context.resources.getColor(R.color.unReadNotification)
            )
        }

        private fun getTimeAgo(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp

            return when {
                diff < 60_000 -> "Just now"
                diff < 3600_000 -> {
                    val minutes = diff / 60_000
                    if (minutes == 1L) "1 minute ago" else "$minutes minutes ago"
                }
                diff < 86400_000 -> {
                    val hours = diff / 3600_000
                    if (hours == 1L) "1 hour ago" else "$hours hours ago"
                }
                diff < 604800_000 -> {
                    val days = diff / 86400_000
                    if (days == 1L) "1 day ago" else "$days days ago"
                }
                else -> {
                    val weeks = diff / 604800_000
                    if (weeks == 1L) "1 week ago" else "$weeks weeks ago"
                }
            }
        }

        private fun showPopupMenu(
            view: View,
            notification: FeedNotification,
            position: Int
        ) {
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.notification_menu_options, popup.menu)

            // Show/hide "Mark as Read" based on current state
            val markAsReadItem = popup.menu.findItem(R.id.action_mark_as_read)
            if (notification.read) {
                markAsReadItem?.title = "Mark as Unread"
            } else {
                markAsReadItem?.title = "Mark as Read"
            }

            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_mark_as_read -> {
                        onMarkAsRead(notification, position)
                        true
                    }
                    R.id.delete -> {
                        onDelete(notification, position)
                        true
                    }
                    else -> false
                }
            }

            popup.show()
        }
    }

    // ViewHolder for Loading
    inner    class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}

// Sealed class for different view types
sealed class NotificationItem {
    data class Header(val title: String) : NotificationItem()
    data class NotificationData(val notification: FeedNotification) : NotificationItem()
    object Loading : NotificationItem()
}

