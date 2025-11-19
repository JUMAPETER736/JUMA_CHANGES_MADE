package com.uyscuti.social.circuit.calls.adapter

import android.content.Context
import android.graphics.PorterDuff
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.uyscuti.social.circuit.R
import com.uyscuti.social.core.common.data.room.entity.CallLogEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CallLogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val textCallerName: TextView = itemView.findViewById(R.id.textCallerName)
    private val textCallDateTime: TextView = itemView.findViewById(R.id.textCallDateTime)
    private val imageCallTypeIndicator: ImageView = itemView.findViewById(R.id.imageCallTypeIndicator)
    private val imageIsVideo: ImageView = itemView.findViewById(R.id.isVideo)
    private val callerAvatar: ImageView = itemView.findViewById(R.id.imageCallerAvatar)

    fun bind(callLog: CallLogEntity, context: Context) {
        val date = Date(callLog.createdAt)
        val dateTime = checkDate(date)

        val type = callLog.callType
        val status = callLog.callStatus
        val isVideoCall = callLog.isVideoCall
        val avatar = callLog.callerAvatar

        if (isVideoCall){
            imageIsVideo.setImageResource(R.drawable.baseline_videocam_24)
        } else {
            imageIsVideo.setImageResource(R.drawable.baseline_phone_24)
        }

        if (type == "Incoming") {
            if (status == "Answered") {
                // Set the tint to green and change the icon
                imageCallTypeIndicator.setImageResource(R.drawable.baseline_call_received_24)
                imageCallTypeIndicator.setColorFilter(ContextCompat.getColor(context, R.color.green_dark), PorterDuff.Mode.SRC_IN)
                textCallerName.setTextColor(ContextCompat.getColor(context, R.color.black))

            } else {
                // Set the tint to red (or any other color you prefer) and change the icon
                imageCallTypeIndicator.setImageResource(R.drawable.baseline_call_received_24)
                imageCallTypeIndicator.setColorFilter(ContextCompat.getColor(context, R.color.red), PorterDuff.Mode.SRC_IN)
                textCallerName.setTextColor(ContextCompat.getColor(context, R.color.red))
            }
        } else {
            // Set the type to "Outgoing" (or any other type you prefer) and change the icon
            imageCallTypeIndicator.setImageResource(R.drawable.baseline_call_made_24)
            imageCallTypeIndicator.setColorFilter(ContextCompat.getColor(context, R.color.green_dark), PorterDuff.Mode.SRC_IN)
        }

        Glide.with(context).load(avatar).apply(RequestOptions.bitmapTransform(CircleCrop())).into(callerAvatar)

        textCallerName.text = callLog.callerName
        textCallDateTime.text = dateTime
    }

    private fun checkDate(date: Date, locale: Locale = Locale.getDefault()): String {
        val currentDate = Date()
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        calendar.add(Calendar.DAY_OF_YEAR, -1) // Subtract 1 day to get yesterday's date

        val dateFormat = SimpleDateFormat("dd MMMM yyyy", locale)

        return when {
            isSameDay(date, currentDate) -> SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT, locale).format(date)
            isSameDay(date, calendar.time) -> "Yesterday"
            else -> dateFormat.format(date)
        }
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(date1) == dateFormat.format(date2)
    }
}
