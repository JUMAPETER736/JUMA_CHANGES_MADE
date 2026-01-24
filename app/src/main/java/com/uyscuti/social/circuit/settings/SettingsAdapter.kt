package com.uyscuti.social.circuit.settings

import android.content.Context
import android.content.SharedPreferences
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
import com.uyscuti.sharedmodule.model.SettingsModel
import com.uyscuti.social.circuit.R

class SettingsAdapter(
    private val context: Context,
    private val settingsList: List<SettingsModel>,
    private val clickListener: (SettingsModel) -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val USER_VIEW = 0
    private val PRIVACY_VIEW = 1
    private val CHAT_VIEW = 2
    private val NOTIFICATIONS_VIEW = 3
    private val STORAGE_VIEW = 4
    private val INVITE_VIEW = 5
    private val HELP_VIEW = 6
    private val DEFAULT_VIEW = 7

    // New view types for relationship management
    private val BLOCKED_USERS_VIEW = 8
    private val MUTED_POSTS_VIEW = 9
    private val MUTED_STORIES_VIEW = 10
    private val CLOSE_FRIENDS_VIEW = 11
    private val FAVORITES_VIEW = 12
    private val RESTRICTED_VIEW = 13
    private val HIDDEN_POSTS_VIEW = 14

    private val PREFS_NAME = "LocalSettings"
    private var settings: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            USER_VIEW -> {
                val view = layoutInflater.inflate(R.layout.item_list_user_redesign_item, parent, false)
                FirstItemViewHolder(view)
            }
            PRIVACY_VIEW -> {
                val view = layoutInflater.inflate(R.layout.privacy_item_view, parent, false)
                PrivacyItemViewHolder(view)
            }
            CHAT_VIEW -> {
                val view = layoutInflater.inflate(R.layout.chats_item_layout, parent, false)
                ChatItemViewHolder(view)
            }
            NOTIFICATIONS_VIEW -> {
                val view = layoutInflater.inflate(R.layout.notifications_item_view, parent, false)
                NotificationsItemViewHolder(view)
            }
            STORAGE_VIEW -> {
                val view = layoutInflater.inflate(R.layout.storage_item_view, parent, false)
                StorageItemViewHolder(view)
            }
            INVITE_VIEW -> {
                val view = layoutInflater.inflate(R.layout.invite_item_view, parent, false)
                InviteItemViewHolder(view)
            }
            HELP_VIEW -> {
                val view = layoutInflater.inflate(R.layout.help_item_view, parent, false)
                HelpItemViewHolder(view)
            }

            // New relationship management views
            BLOCKED_USERS_VIEW -> {
                val view = layoutInflater.inflate(R.layout.relationship_item_view, parent, false)
                RelationshipItemViewHolder(view)
            }
            MUTED_POSTS_VIEW -> {
                val view = layoutInflater.inflate(R.layout.relationship_item_view, parent, false)
                RelationshipItemViewHolder(view)
            }
            MUTED_STORIES_VIEW -> {
                val view = layoutInflater.inflate(R.layout.relationship_item_view, parent, false)
                RelationshipItemViewHolder(view)
            }
            CLOSE_FRIENDS_VIEW -> {
                val view = layoutInflater.inflate(R.layout.relationship_item_view, parent, false)
                RelationshipItemViewHolder(view)
            }
            FAVORITES_VIEW -> {
                val view = layoutInflater.inflate(R.layout.relationship_item_view, parent, false)
                RelationshipItemViewHolder(view)
            }
            RESTRICTED_VIEW -> {
                val view = layoutInflater.inflate(R.layout.relationship_item_view, parent, false)
                RelationshipItemViewHolder(view)
            }
            HIDDEN_POSTS_VIEW -> {
                val view = layoutInflater.inflate(R.layout.relationship_item_view, parent, false)
                RelationshipItemViewHolder(view)
            }

            else -> {
                val view = layoutInflater.inflate(R.layout.item_list, parent, false)
                NormalItemViewHolder(view)
            }
        }
    }

    override fun getItemCount(): Int = settingsList.size

    override fun getItemViewType(position: Int): Int {
        if (position < 0 || position >= settingsList.size) {
            return super.getItemViewType(position)
        }

        return when (settingsList[position].title) {
            "Username" -> USER_VIEW
            "Privacy" -> PRIVACY_VIEW
            "Chats" -> CHAT_VIEW
            "Notifications" -> NOTIFICATIONS_VIEW
            "Storage" -> STORAGE_VIEW
            "Invite" -> INVITE_VIEW
            "Help" -> HELP_VIEW

            // New relationship management items
            "Blocked Users" -> BLOCKED_USERS_VIEW
            "Muted Posts" -> MUTED_POSTS_VIEW
            "Muted Stories" -> MUTED_STORIES_VIEW
            "Close Friends" -> CLOSE_FRIENDS_VIEW
            "Favorites" -> FAVORITES_VIEW
            "Restricted Accounts" -> RESTRICTED_VIEW
            "Hidden Posts" -> HIDDEN_POSTS_VIEW

            else -> DEFAULT_VIEW
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val setting = settingsList[position]
        when (holder) {
            is FirstItemViewHolder -> holder.bind(setting, clickListener)
            is PrivacyItemViewHolder -> holder.bind(setting, clickListener)
            is ChatItemViewHolder -> holder.bind(setting, clickListener)
            is NotificationsItemViewHolder -> holder.bind(setting, clickListener)
            is StorageItemViewHolder -> holder.bind(setting, clickListener)
            is InviteItemViewHolder -> holder.bind(setting, clickListener)
            is HelpItemViewHolder -> holder.bind(setting, clickListener)
            is RelationshipItemViewHolder -> holder.bind(setting, clickListener)
            is NormalItemViewHolder -> holder.bind(setting, clickListener)
        }
    }

    // ==================== EXISTING VIEW HOLDERS ====================

    inner class FirstItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.titleTextView)
        private val subTitle: TextView = itemView.findViewById(R.id.subtitleTextView)
        private val image: ImageView = itemView.findViewById(R.id.settingsImageView)

        init {
            val selectableItemBackground = TypedValue()
            itemView.context.theme.resolveAttribute(android.R.attr.selectableItemBackground, selectableItemBackground, true)
            itemView.setBackgroundResource(selectableItemBackground.resourceId)
        }

        fun bind(setting: SettingsModel, clickListener: (SettingsModel) -> Unit) {
            // Get user data from SharedPreferences
            val avatar = settings.getString("avatar", null)
            val firstName = settings.getString("firstName", null)
            val lastName = settings.getString("lastName", null)
            val username = settings.getString("username", "login")

            // Build full name
            val fullName = buildString {
                if (!firstName.isNullOrEmpty()) append(firstName)
                if (!firstName.isNullOrEmpty() && !lastName.isNullOrEmpty()) append(" ")
                if (!lastName.isNullOrEmpty()) append(lastName)
            }

            // Set full name (or username if no full name available)
            title.text = fullName.ifEmpty { username }

            // Set username with @ prefix
            subTitle.text = "@$username"
            subTitle.visibility = View.VISIBLE

            Glide.with(image.context)
                .load(avatar)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .apply(RequestOptions.placeholderOf(R.drawable.google))
                .error(setting.imageBitmap)
                .into(image)

            itemView.setOnClickListener { clickListener(setting) }
        }
    }

    inner class PrivacyItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.titleTextView)
        private val subTitle: TextView = itemView.findViewById(R.id.subtitleTextView)

        init {
            val selectableItemBackground = TypedValue()
            itemView.context.theme.resolveAttribute(android.R.attr.selectableItemBackground, selectableItemBackground, true)
            itemView.setBackgroundResource(selectableItemBackground.resourceId)
        }

        fun bind(setting: SettingsModel, clickListener: (SettingsModel) -> Unit) {
            title.text = setting.title
            subTitle.text = setting.subTitle
            itemView.setOnClickListener { clickListener(setting) }
        }
    }

    inner class ChatItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.fullName)
        private val subTitle: TextView = itemView.findViewById(R.id.userName)

        init {
            val selectableItemBackground = TypedValue()
            itemView.context.theme.resolveAttribute(android.R.attr.selectableItemBackground, selectableItemBackground, true)
            itemView.setBackgroundResource(selectableItemBackground.resourceId)
        }

        fun bind(setting: SettingsModel, clickListener: (SettingsModel) -> Unit) {
            title.text = setting.title
            subTitle.text = setting.subTitle
            itemView.setOnClickListener { clickListener(setting) }
        }
    }

    inner class NotificationsItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.titleTextView)
        private val subTitle: TextView = itemView.findViewById(R.id.subtitleTextView)

        init {
            val selectableItemBackground = TypedValue()
            itemView.context.theme.resolveAttribute(android.R.attr.selectableItemBackground, selectableItemBackground, true)
            itemView.setBackgroundResource(selectableItemBackground.resourceId)
        }

        fun bind(setting: SettingsModel, clickListener: (SettingsModel) -> Unit) {
            title.text = setting.title
            subTitle.text = setting.subTitle
            itemView.setOnClickListener { clickListener(setting) }
        }
    }

    inner class StorageItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.titleTextView)
        private val subTitle: TextView = itemView.findViewById(R.id.subtitleTextView)

        init {
            val selectableItemBackground = TypedValue()
            itemView.context.theme.resolveAttribute(android.R.attr.selectableItemBackground, selectableItemBackground, true)
            itemView.setBackgroundResource(selectableItemBackground.resourceId)
        }

        fun bind(setting: SettingsModel, clickListener: (SettingsModel) -> Unit) {
            title.text = setting.title
            subTitle.text = setting.subTitle
            itemView.setOnClickListener { clickListener(setting) }
        }
    }

    inner class InviteItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.titleTextView)
        private val subTitle: TextView = itemView.findViewById(R.id.subtitleTextView)

        init {
            val selectableItemBackground = TypedValue()
            itemView.context.theme.resolveAttribute(android.R.attr.selectableItemBackground, selectableItemBackground, true)
            itemView.setBackgroundResource(selectableItemBackground.resourceId)
        }

        fun bind(setting: SettingsModel, clickListener: (SettingsModel) -> Unit) {
            title.text = setting.title
            subTitle.text = setting.subTitle
            itemView.setOnClickListener { clickListener(setting) }
        }
    }

    inner class HelpItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.titleTextView)
        private val subTitle: TextView = itemView.findViewById(R.id.subtitleTextView)

        init {
            val selectableItemBackground = TypedValue()
            itemView.context.theme.resolveAttribute(android.R.attr.selectableItemBackground, selectableItemBackground, true)
            itemView.setBackgroundResource(selectableItemBackground.resourceId)
        }

        fun bind(setting: SettingsModel, clickListener: (SettingsModel) -> Unit) {
            title.text = setting.title
            subTitle.text = setting.subTitle
            itemView.setOnClickListener { clickListener(setting) }
        }
    }

    inner class NormalItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.titleTextView)
        private val subTitle: TextView = view.findViewById(R.id.subtitleTextView)
        private val image: ImageView = view.findViewById(R.id.settingsImageView)

        init {
            val selectableItemBackground = TypedValue()
            itemView.context.theme.resolveAttribute(android.R.attr.selectableItemBackground, selectableItemBackground, true)
            itemView.setBackgroundResource(selectableItemBackground.resourceId)
        }

        fun bind(setting: SettingsModel, clickListener: (SettingsModel) -> Unit) {
            title.text = setting.title
            subTitle.text = setting.subTitle
            Glide.with(image.context)
                .load(setting.imageBitmap)
                .error(setting.imageBitmap)
                .placeholder(R.drawable.google)
                .into(image)

            itemView.setOnClickListener { clickListener(setting) }
        }
    }

    // ==================== NEW RELATIONSHIP MANAGEMENT VIEW HOLDER ====================

    inner class RelationshipItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.titleTextView)
        private val subTitle: TextView = itemView.findViewById(R.id.subtitleTextView)
        private val icon: ImageView = itemView.findViewById(R.id.settingsImageView)

        init {
            val selectableItemBackground = TypedValue()
            itemView.context.theme.resolveAttribute(android.R.attr.selectableItemBackground, selectableItemBackground, true)
            itemView.setBackgroundResource(selectableItemBackground.resourceId)
        }

        fun bind(setting: SettingsModel, clickListener: (SettingsModel) -> Unit) {
            title.text = setting.title
            subTitle.text = setting.subTitle

            // Set appropriate icon based on the setting type
            val iconRes = when (setting.title) {
                "Blocked Users" -> R.drawable.block_user
                "Muted Posts" -> R.drawable.ic_mute_post
                "Muted Stories" -> R.drawable.ic_mute_stories
                "Close Friends" -> R.drawable.ic_close_friends
                "Favorites" -> R.drawable.ic_favorite
                "Restricted Accounts" -> R.drawable.ic_restrict
                "Hidden Posts" -> R.drawable.hide_svgrepo_com
                else -> R.drawable.google
            }

            icon.setImageResource(iconRes)
            itemView.setOnClickListener { clickListener(setting) }
        }
    }
}