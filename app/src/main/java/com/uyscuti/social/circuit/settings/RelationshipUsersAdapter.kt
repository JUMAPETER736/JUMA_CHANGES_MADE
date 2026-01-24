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
import com.uyscuti.social.network.api.response.posts.Avatar
import java.text.SimpleDateFormat
import java.util.Locale

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
        private val avatarImageView: ImageView = itemView.findViewById(R.id.avatarImageView)
        private val fullNameTextView: TextView = itemView.findViewById(R.id.fullNameTextView)
        private val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        private val actionButton: Button = itemView.findViewById(R.id.actionButton)

        fun bind(item: UserRelationshipItem) {
            // Load avatar
            val avatarUrl = item.avatar?.url
            Glide.with(avatarImageView.context)
                .load(avatarUrl)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .placeholder(R.drawable.flash21)
                .error(R.drawable.flash21)
                .into(avatarImageView)

            // Set full name (or username as fallback)
            val fullName = buildString {
                item.firstName?.let { append(it) }
                if (item.firstName != null && item.lastName != null) append(" ")
                item.lastName?.let { append(it) }
            }

            fullNameTextView.text = fullName.ifEmpty { item.username }

            // Set username
            usernameTextView.text = "@${item.username}"

            // Format and set date
            val formattedDate = formatDate(item.actionDate)
            val datePrefix = when (relationshipType) {
                RelationshipType.BLOCKED -> "Blocked"
                RelationshipType.MUTED_POSTS -> "Muted"
                RelationshipType.MUTED_STORIES -> "Muted"
                RelationshipType.CLOSE_FRIENDS -> "Added"
                RelationshipType.FAVORITES -> "Added"
                RelationshipType.RESTRICTED -> "Restricted"
            }
            dateTextView.text = "$datePrefix $formattedDate"

            // Set action button text and styling
            val (buttonText, buttonColor, textColor) = when (relationshipType) {
                RelationshipType.BLOCKED -> Triple("Unblock", R.color.redBlocked, android.R.color.white)
                RelationshipType.MUTED_POSTS -> Triple("Unmute", R.color.gray_dark_transparent, android.R.color.white)
                RelationshipType.MUTED_STORIES -> Triple("Unmute", R.color.app_secondary_variant, android.R.color.white)
                RelationshipType.CLOSE_FRIENDS -> Triple("Remove", R.color.redBlocked, android.R.color.white)
                RelationshipType.FAVORITES -> Triple("Remove", R.color.redBlocked, android.R.color.white)
                RelationshipType.RESTRICTED -> Triple("Unrestrict", R.color.dark_gray, android.R.color.white)
            }

            actionButton.text = buttonText
            actionButton.setTextColor(context.getColor(textColor))
            actionButton.backgroundTintList = android.content.res.ColorStateList.valueOf(context.getColor(buttonColor))

            // Set click listeners
            actionButton.setOnClickListener {
                onActionClick(item.userId, item)
            }

            itemView.setOnClickListener {

                // val intent = Intent(context, UserProfileActivity::class.java)
                // intent.putExtra("userId", item.userId)
                // context.startActivity(intent)
            }
        }

        private fun formatDate(dateString: String): String {
            return try {
                // Parse the ISO date string
                val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                isoFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
                val date = isoFormat.parse(dateString)

                // Format to "24 Jan 2026" (shorter format)
                val displayFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
                date?.let { displayFormat.format(it) } ?: dateString
            } catch (e: Exception) {
                // If parsing fails, try alternate format without milliseconds
                try {
                    val alternateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                    alternateFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
                    val date = alternateFormat.parse(dateString)

                    val displayFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
                    date?.let { displayFormat.format(it) } ?: dateString
                } catch (e2: Exception) {
                    // If all parsing fails, return as is
                    dateString
                }
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
    val actionDate: String // blockedAt, mutedAt, addedAt, restrictedAt (ISO date string)
)