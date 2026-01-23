package com.uyscuti.social.circuit.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.circuit.R

class BlockedUsersAdapter(
    private val blockedUsers: List<BlockedUserItem>,
    private val onUnblockClick: (BlockedUserItem) -> Unit
) : RecyclerView.Adapter<BlockedUsersAdapter.BlockedUserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockedUserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_blocked_user, parent, false)
        return BlockedUserViewHolder(view)
    }

    override fun onBindViewHolder(holder: BlockedUserViewHolder, position: Int) {
        holder.bind(blockedUsers[position])
    }

    override fun getItemCount(): Int = blockedUsers.size

    inner class BlockedUserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatarImageView: ImageView = itemView.findViewById(R.id.avatarImageView)
        private val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        private val fullNameTextView: TextView = itemView.findViewById(R.id.fullNameTextView)
        private val unblockButton: MaterialButton = itemView.findViewById(R.id.unblockButton)

        fun bind(blockedUser: BlockedUserItem) {
            usernameTextView.text = "@${blockedUser.user.username}"
            fullNameTextView.text = "${blockedUser.user.firstName} ${blockedUser.user.lastName}"

            Glide.with(itemView.context)
                .load(blockedUser.user.avatar?.url)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .placeholder(R.drawable.round_user)
                .error(R.drawable.round_user)
                .into(avatarImageView)

            unblockButton.setOnClickListener {
                onUnblockClick(blockedUser)
            }
        }
    }
}