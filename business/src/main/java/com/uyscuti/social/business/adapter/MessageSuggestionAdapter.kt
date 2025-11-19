package com.uyscuti.social.business.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.business.R

class MessageSuggestionsAdapter(
    private val messages: List<String>,
    private val onMessageClick: (String) -> Unit

) : RecyclerView.Adapter<MessageSuggestionsAdapter.MessageViewHolder>() {

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageTextView: TextView = itemView.findViewById(R.id.suggestion_text)

        fun bind(message: String) {
            messageTextView.text = message
            itemView.setOnClickListener {
                onMessageClick(message)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.suggestion_item, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
        holder.itemView.setOnClickListener {
            onMessageClick(messages[position])

        }
        holder.messageTextView.text = messages[position]

    }

    override fun getItemCount(): Int {
        return messages.size
    }
}