package com.uyscuti.social.circuit.adapter.notifications

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.circuit.adapter.notifications.NotificationsAdapter.NotificationActionListener
import com.uyscuti.social.circuit.model.notifications_data_class.INotification
import com.uyscuti.social.circuit.model.notifications_data_class.NotificationByDay
import com.uyscuti.social.circuit.R

class NotificationDayAdapter(

    private val notificationsList: MutableList<NotificationByDay>,
    private val notificationActionListener: NotificationActionListener,

    ) :
    RecyclerView.Adapter<ClassViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.notifications_day_item, parent, false)
        return ClassViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClassViewHolder, position: Int) {
        val notifications = notificationsList[position]
        holder.day.text = notifications.day
        holder.notificationsRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        val notes: ArrayList<INotification> = arrayListOf()
        for (notification in notifications.notification) {
            notes.add(notification)
        }
        holder.notificationsRecyclerView.adapter =
            NotificationsAdapter(notes, notificationActionListener)
    }
    override fun getItemCount(): Int {
        return notificationsList.size
    }
    @SuppressLint("NotifyDataSetChanged")
    fun addNotifications(notificationsList: ArrayList<NotificationByDay>) {
        this.notificationsList.clear()
        this.notificationsList.addAll(notificationsList)
        notifyDataSetChanged()
    }

    fun addNotification(notification: NotificationByDay) {
        val startPosition = notificationsList.size
        notificationsList.add(0, notification)
        notifyItemInserted(0)
    }
}

class ClassViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val day: TextView = itemView.findViewById(R.id.day)
    val notificationsRecyclerView: RecyclerView =
        itemView.findViewById(R.id.notificationsRecyclerView)
}