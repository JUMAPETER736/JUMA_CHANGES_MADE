package com.uyscuti.social.circuit.calls.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.circuit.R
import com.uyscuti.social.core.common.data.room.entity.CallLogEntity

class CallLogAdapter(private val context: Context) : RecyclerView.Adapter<CallLogViewHolder>() {
    private var callLogs: MutableList<CallLogEntity> = mutableListOf()
    private lateinit var callLogClickListener: CallLogClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallLogViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.call_log_item, parent, false)

        return CallLogViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CallLogViewHolder, position: Int) {
        val callLog = callLogs[position]


        holder.itemView.setOnClickListener {
            callLogClickListener.onCallLogClick(callLog)
        }

        if (callLog.isSelected){
            val grayColor = ContextCompat.getColor(holder.itemView.context, com.uyscuti.social.chatsuit.R.color.white_three)
            val colorDrawable = ColorDrawable(grayColor)
            holder.itemView.background = colorDrawable
        } else {
            val grayColor = ContextCompat.getColor(holder.itemView.context, android.R.color.white)
            val colorDrawable = ColorDrawable(grayColor)
            holder.itemView.background = colorDrawable
        }



        holder.itemView.setOnLongClickListener {
            // Set the background color to gray


            // Notify the listener about the long click
            callLogClickListener.onCallLogLongClick(callLog)

            // Return true to indicate that the long click is consumed
            true
        }

        holder.bind(callLog, context)
    }

    override fun getItemCount(): Int {
        return callLogs.size
    }

    // Function to update an item by its position
    fun updateItem(updatedCallLog: CallLogEntity) {
        val position = callLogs.indexOfFirst { it.id == updatedCallLog.id }
        if (position != -1) {
            // Update the call log at the found position
            callLogs[position] = updatedCallLog

            // Notify the adapter that the item at the given position has changed
            notifyItemChanged(position)
        }
    }

    fun deselectAllCallLogs() {
        val selectedPositions = mutableListOf<Int>()

        // Find positions of selected call logs
        for (i in callLogs.indices) {
            if (callLogs[i].isSelected) {
                selectedPositions.add(i)
            }
        }

        // Update the selected call logs and notify the adapter
        selectedPositions.forEach { position ->
            callLogs[position].isSelected = false
            notifyItemChanged(position)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addCallLogs(callLogs: List<CallLogEntity>) {
        this.callLogs = callLogs.toMutableList() // Convert the List to a MutableList
        notifyDataSetChanged()
    }


    fun addNewCallLogs(newCallLogs: List<CallLogEntity>) {
        if (newCallLogs.isNotEmpty()) {
            val oldSize = callLogs.size
            callLogs.addAll(newCallLogs)
            notifyItemRangeInserted(oldSize, callLogs.size - oldSize)
        }
    }

    fun setCallLogClickListener(callLogClickListener: CallLogClickListener) {
        this.callLogClickListener = callLogClickListener
    }

    // Method to add a new call log to the adapter
    fun addCallLog(callLog: CallLogEntity) {
        callLogs.add(callLog)
        notifyItemInserted(callLogs.size - 1)
    }

    interface CallLogClickListener {
        fun onCallLogClick(callLog: CallLogEntity)

        fun onCallLogLongClick(callLog: CallLogEntity)
    }
}

