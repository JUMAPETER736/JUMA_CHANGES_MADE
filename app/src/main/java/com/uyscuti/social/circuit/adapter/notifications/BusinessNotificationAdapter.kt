package com.uyscuti.social.circuit.adapter.notifications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.circuit.model.notifications_data_class.Notification
import com.uyscuti.social.circuit.R

private const val BUSINESS_NOTIFICATION_COMMENT = 1
private const val  BUSINESS_NOTIFICATION_VIEW= 2
private const val  BUSINESS_NOTIFICATION_LIKE = 3

class BusinessNotificationAdapter(private val notifications:List<Notification>
): RecyclerView.Adapter<RecyclerView.ViewHolder> (){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            BUSINESS_NOTIFICATION_COMMENT -> {
                val view =
                    LayoutInflater.from(parent.context).inflate(R.layout.business_notification_comment, parent, false)
                NotificationCommentViewHolder(view)

            }
            BUSINESS_NOTIFICATION_VIEW -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.business_notification_viewed, parent, false)
                NotificationCheckedViewHolder(view)

            }
            BUSINESS_NOTIFICATION_LIKE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.business_notification_like, parent, false)
                NotificationLikedViewHolder(view)
            }


            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val notification = notifications[position]
        when (holder) {
            is NotificationCommentViewHolder -> {


            }
            is NotificationCheckedViewHolder -> {

            }
            is NotificationLikedViewHolder -> {

            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val notification = notifications[position]
        return when (notification.link) {
            "catalog" -> BUSINESS_NOTIFICATION_COMMENT
            "like" -> BUSINESS_NOTIFICATION_VIEW
            "viewed" -> BUSINESS_NOTIFICATION_LIKE

            else -> BUSINESS_NOTIFICATION_COMMENT
        }
    }

    override fun getItemCount() = notifications.size

    ///new code
    private var currentItemDisplayPosition = -1
    fun getCurrentItemDisplayPosition(): Int {
        return currentItemDisplayPosition
    }

    // View Holder for Notification Comment
    class NotificationCommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val businessPic : ImageView = itemView.findViewById(R.id.profilePic1)
        val notificationTitle : TextView = itemView.findViewById(R.id.commentTitle)

    }
    // View Holder for Notification Checked
    class NotificationCheckedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val businessPic: ImageView = itemView.findViewById(R.id.profilePic1)
        val notificationTitle : TextView = itemView.findViewById(R.id.checkedviewedTitle)

    }
    class NotificationLikedViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val businessPic : ImageView = itemView.findViewById(R.id.profilePic1)
        val businessTitle : TextView = itemView.findViewById(R.id.notificationTitle)
    }

}