package com.uyscuti.social.circuit.User_Interface.fragments.notifications.notifications//package com.uyscut.flashdesign.adapter.notifications
//
//import android.annotation.SuppressLint
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.uyscut.flashdesign.R
//import com.uyscut.flashdesign.adapter.notifications.NotificationsAdapter.NotificationActionListener
//import com.uyscuti.social.circuit.model.notifications_data_class.INotification
//import com.uyscuti.social.circuit.model.notifications_data_class.NotificationByDay
//
//class NotificationDayAdapter(
//    private val notificationsList: MutableList<NotificationByDay>,
//    private val notificationActionListener: NotificationActionListener,
//
//) :
//    RecyclerView.Adapter<ClassViewHolder>() {
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.notifications_day_item, parent, false)
//        return ClassViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: ClassViewHolder, position: Int) {
//        val notifications = notificationsList[position]
//        holder.day.text = notifications.day
//        holder.notificationsRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
//        val notes: ArrayList<INotification> = arrayListOf()
//        for (notification in notifications.notification) {
//            notes.add(notification)
//        }
//        holder.notificationsRecyclerView.adapter =
//            NotificationsAdapter(notes, notificationActionListener)
//    }
//    override fun getItemCount(): Int {
//        return notificationsList.size
//    }
//    @SuppressLint("NotifyDataSetChanged")
//    fun addNotifications(notificationsList: ArrayList<NotificationByDay>) {
//        this.notificationsList.clear()
//        this.notificationsList.addAll(notificationsList)
//        notifyDataSetChanged()
//    }
//
//    fun addNotification(notification: NotificationByDay) {
//        val startPosition = notificationsList.size
//        notificationsList.add(0, notification)
//        notifyItemInserted(0)
//    }
//}
//
//class ClassViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//    val day: TextView = itemView.findViewById(R.id.day)
//    val notificationsRecyclerView: RecyclerView =
//        itemView.findViewById(R.id.notificationsRecyclerView)
//}
////class NotificationDayAdapter(private val notificationsList: ArrayList<NotificationByDay>) :
////    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
////
////    companion object {
////        private const val MARK_AS_READ = 0
////        private const val DAYS_TYPE_VIEW = 1
////    }
////
////    override fun getItemViewType(position: Int): Int {
////        return when (position) {
////            0 -> MARK_AS_READ
////            1 -> DAYS_TYPE_VIEW
////
////            else -> {
////               0
////            }
////        }
////    }
////    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
////        return when (viewType) {
////            MARK_AS_READ -> {
////                val view = LayoutInflater.from(parent.context)
////                    .inflate(R.layout.notification_tab_layout, parent, false)
////                NotificaitonChooseViewHolder(view)
////            }
////
////            DAYS_TYPE_VIEW -> {
////                val view = LayoutInflater.from(parent.context)
////                    .inflate(R.layout.notifications_day_item, parent, false)
////                NotificationDayViewHolder(view)
////            }
////
////            else -> throw IllegalArgumentException("Invalid view type")
////        }
////    }
////
////    class NotificationDayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
////        val day: TextView = itemView.findViewById(R.id.day)
////        val notificationsRecyclerView: RecyclerView =
////            itemView.findViewById(R.id.notificationsRecyclerView)
////
////    }
////
////    class NotificationChooseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
////    }
////    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
////        when (holder) {
////            is NotificationDayViewHolder->{
////                val notifications = notificationsList[position]
////                holder.day.text = notifications.day
////                holder . notificationsRecyclerView . layoutManager = LinearLayoutManager (holder.itemView.context)
////                holder . notificationsRecyclerView . adapter = NotificationsAdapter (notifications.notification)
////            }
////        }
////    }
////    override fun getItemCount(): Int {
////        return notificationsList.size  // Adjusting for header and mark as read
////    }
////            fun addNotifications(notificationsList: List<NotificationByDay>) {
////                this.notificationsList.clear()
////                this.notificationsList.addAll(notificationsList)
////                notifyDataSetChanged()
////            }
////
////            fun addNotification(notification: NotificationByDay) {
////                val startPosition = notificationsList.size
////                 notificationsList.add(0,notification)
////                notifyItemInserted(0)
////            }
////        }

