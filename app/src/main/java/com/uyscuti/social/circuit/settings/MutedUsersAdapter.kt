package com.uyscuti.social.circuit.settings

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.uyscuti.social.circuit.R
import java.text.SimpleDateFormat
import java.util.*

class MutedUsersAdapter(
    private val context: Context,
    private val onActionClick: (String, UserRelationshipItem) -> Unit
) : RecyclerView.Adapter<MutedUsersAdapter.ViewHolder>() {

    private val items = mutableListOf<UserRelationshipItem>()
    private val dateFormatter = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())

    fun updateList(newItems: List<UserRelationshipItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        if (position in items.indices) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_muted_users, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatarImageView: ImageView = itemView.findViewById(R.id.avatarImageView)
        private val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        private val fullNameTextView: TextView = itemView.findViewById(R.id.fullNameTextView)
        private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        private val actionButton: Button = itemView.findViewById(R.id.actionButton)

        fun bind(item: UserRelationshipItem) {
            // Set username
            usernameTextView.text = "@${item.username}"

            // Set full name
            val fullName = buildString {
                item.firstName?.let { append(it) }
                if (item.firstName != null && item.lastName != null) append(" ")
                item.lastName?.let { append(it) }
            }
            fullNameTextView.text = fullName.ifEmpty { item.username }

            // Set date with "Muted" prefix
            dateTextView.text = try {
                val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(item.actionDate)
                date?.let { "Muted ${dateFormatter.format(it)}" } ?: "Muted ${item.actionDate}"
            } catch (e: Exception) {
                "Muted ${item.actionDate}"
            }

            // Load avatar
            Glide.with(context)
                .load(item.avatar?.url)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .placeholder(R.drawable.round_user)
                .error(R.drawable.round_user)
                .into(avatarImageView)

            // Set button text and action
            actionButton.text = "Unmute"
            actionButton.setOnClickListener {
                onActionClick(item.userId, item)
            }
        }
    }
}