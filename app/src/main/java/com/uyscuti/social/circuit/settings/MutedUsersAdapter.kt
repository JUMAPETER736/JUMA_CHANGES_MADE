package com.uyscuti.social.circuit.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.uyscuti.social.circuit.R

class MutedUsersAdapter<T>(
    private val items: List<T>,
    private val buttonText: String,
    private val onButtonClick: (T) -> Unit
) : RecyclerView.Adapter<MutedUsersAdapter<T>.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_blocked_user, parent, false)
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
        private val actionButton: MaterialButton = itemView.findViewById(R.id.unblockButton)

        fun bind(item: T) {
            actionButton.text = buttonText

            // Use reflection or when expression to handle different types
            when (item) {
                is com.uyscuti.social.network.api.response.profile.followingList.MutedPostsItem -> {
                    usernameTextView.text = "@${item.user?.username}"
                    fullNameTextView.text = "${item.user?.firstName} ${item.user?.lastName}"
                    Glide.with(itemView.context)
                        .load(item.user?.avatar?.url)
                        .apply(RequestOptions.bitmapTransform(CircleCrop()))
                        .placeholder(R.drawable.round_user)
                        .into(avatarImageView)
                }
                // Add other types as needed
            }

            actionButton.setOnClickListener {
                onButtonClick(item)
            }
        }
    }
}