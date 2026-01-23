package com.uyscuti.social.circuit.settings

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.uyscuti.social.circuit.R
import com.uyscuti.social.network.api.response.posts.Avatar

/**
 * Adapter for displaying relationship-based user lists
 * Used for: Blocked Users, Muted Posts, Muted Stories, Close Friends, Favorites, Restricted
 */
class RelationshipUsersAdapter(
    private val context: Context,
    private val relationshipType: RelationshipType,
    private val onActionClick: (String, UserRelationshipItem) -> Unit
) : RecyclerView.Adapter<RelationshipUsersAdapter.UserViewHolder>() {

    private var userList = mutableListOf<UserRelationshipItem>()

    enum class RelationshipType {
        BLOCKED,
        MUTED_POSTS,
        MUTED_STORIES,
        CLOSE_FRIENDS,
        FAVORITES,
        RESTRICTED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.relationship_user_item, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(userList[position])
    }

    override fun getItemCount(): Int = userList.size

    fun updateList(newList: List<UserRelationshipItem>) {
        userList.clear()
        userList.addAll(newList)
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        if (position >= 0 && position < userList.size) {
            userList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, userList.size)
        }
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatar: ImageView = itemView.findViewById(R.id.userAvatar)
        private val username: TextView = itemView.findViewById(R.id.usernameText)
        private val fullName: TextView = itemView.findViewById(R.id.fullNameText)
        private val actionButton: TextView = itemView.findViewById(R.id.actionButton)
        private val addedAtText: TextView = itemView.findViewById(R.id.addedAtText)

        init {
            val selectableItemBackground = TypedValue()
            itemView.context.theme.resolveAttribute(
                android.R.attr.selectableItemBackground,
                selectableItemBackground,
                true
            )
            itemView.setBackgroundResource(selectableItemBackground.resourceId)
        }

        fun bind(item: UserRelationshipItem) {
            // Set username
            username.text = "@${item.username}"

            // Set full name
            val fullName = buildString {
                item.firstName?.let { append(it) }
                if (item.firstName != null && item.lastName != null) append(" ")
                item.lastName?.let { append(it) }
            }
            this.fullName.text = fullName.ifEmpty { item.username }

            // Load avatar
            val avatarUrl = item.avatar?.url
            Glide.with(avatar.context)
                .load(avatarUrl)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .placeholder(R.drawable.round_user)
                .error(R.drawable.round_user)
                .into(avatar)

            // Set added/action date text
            val dateText = when (relationshipType) {
                RelationshipType.BLOCKED -> "Blocked ${item.actionDate}"
                RelationshipType.MUTED_POSTS -> "Muted ${item.actionDate}"
                RelationshipType.MUTED_STORIES -> "Muted ${item.actionDate}"
                RelationshipType.CLOSE_FRIENDS -> "Added ${item.actionDate}"
                RelationshipType.FAVORITES -> "Added ${item.actionDate}"
                RelationshipType.RESTRICTED -> "Restricted ${item.actionDate}"
            }
            addedAtText.text = dateText

            // Set action button text and color
            val (buttonText, buttonColor) = when (relationshipType) {
                RelationshipType.BLOCKED -> "Unblock" to R.color.redBlocked
                RelationshipType.MUTED_POSTS -> "Unmute" to R.color.gray_dark_transparent
                RelationshipType.MUTED_STORIES -> "Unmute" to R.color.app_secondary_variant
                RelationshipType.CLOSE_FRIENDS -> "Remove" to R.color.redBlocked
                RelationshipType.FAVORITES -> "Remove" to R.color.redBlocked
                RelationshipType.RESTRICTED -> "Unrestrict" to R.color.dark_gray
            }
            actionButton.text = buttonText
            actionButton.setTextColor(context.getColor(buttonColor))

            // Set click listener
            actionButton.setOnClickListener {
                onActionClick(item.userId, item)
            }

            // Navigate to profile on item click
            itemView.setOnClickListener {
                // TODO: Navigate to user profile
                // val intent = Intent(context, UserProfileActivity::class.java)
                // intent.putExtra("userId", item.userId)
                // context.startActivity(intent)
            }
        }
    }
}

/**
 * Universal data class for relationship items
 */
data class UserRelationshipItem(
    val userId: String,
    val username: String,
    val email: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val avatar: Avatar? = null,
    val actionDate: String // blockedAt, mutedAt, addedAt, restrictedAt
)