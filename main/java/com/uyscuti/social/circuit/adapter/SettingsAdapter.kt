package com.uyscuti.social.circuit.adapter

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
import com.uyscuti.social.circuit.model.SettingsModel
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
    private val PREFS_NAME = "LocalSettings" // Change this to a unique name for your app

    private var settings: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        return if (viewType == USER_VIEW) {
            val firstItemView = layoutInflater.inflate(
                R.layout.item_list_user_redesign_item, parent, false)
            FirstItemViewHolder(firstItemView)
        }
        else if (viewType == PRIVACY_VIEW) {
            val firstItemView = layoutInflater.inflate(
                R.layout.privacy_item_view, parent, false)
            PrivacyItemViewHolder(firstItemView)
        }
        else if (viewType == CHAT_VIEW) {
            val firstItemView = layoutInflater.inflate(
                R.layout.chats_item_layout, parent, false)
            ChatItemViewHolder(firstItemView)
        }
        else if (viewType == NOTIFICATIONS_VIEW) {
            val firstItemView = layoutInflater.inflate(
                R.layout.notifications_item_view, parent, false)
            NotificationsItemViewHolder(firstItemView)
        }
        else if (viewType == STORAGE_VIEW) {
            val firstItemView = layoutInflater.inflate(
                R.layout.storage_item_view, parent, false)
            StorageItemViewHolder(firstItemView)
        }
        else if (viewType == INVITE_VIEW) {
            val firstItemView = layoutInflater.inflate(
                R.layout.invite_item_view, parent, false)
            InviteItemViewHolder(firstItemView)
        }
        else if (viewType == HELP_VIEW) {
            val normalItemView = layoutInflater.inflate(
                R.layout.help_item_view, parent, false)
            HelpItemViewHolder(normalItemView)
        }

        else {
            val normalItemView = layoutInflater.inflate(
                R.layout.item_list, parent, false)
            NormalItemViewHolder(normalItemView)
        }
    }

    override fun getItemCount(): Int {
        return settingsList.size
    }

    override fun getItemViewType(position: Int): Int {
        if (position < 0 || position >= settingsList.size) {
            return super.getItemViewType(position)
        }

        val setting = settingsList[position]

        return if (setting.title == "Username")
            USER_VIEW
        else if (setting.title == "Privacy")
            PRIVACY_VIEW
        else if (setting.title == "Chats")
            CHAT_VIEW
        else if (setting.title == "Notifications")
            NOTIFICATIONS_VIEW
        else if (setting.title == "Storage")
            STORAGE_VIEW
        else if (setting.title == "Invite")
            INVITE_VIEW
        else if (setting.title == "Help")
            HELP_VIEW
        else HELP_VIEW

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val setting = settingsList[position]
        when (holder) {
            is FirstItemViewHolder -> {
                holder.bind(setting, clickListener)
            }
            is PrivacyItemViewHolder -> {
                holder.bind(setting, clickListener)
            }
            is ChatItemViewHolder -> {
                holder.bind(setting, clickListener)
            }
            is NotificationsItemViewHolder -> {
                holder.bind(setting, clickListener)
            }
            is StorageItemViewHolder -> {
                holder.bind(setting, clickListener)
            }
            is InviteItemViewHolder -> {
                holder.bind(setting, clickListener)
            }
            is HelpItemViewHolder -> {
                holder.bind(setting, clickListener)
            }
            is NormalItemViewHolder -> {
                holder.bind(setting, clickListener)
            }
        }
    }

    inner class FirstItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Bind views for the first item layout here

        private val title: TextView = itemView.findViewById(R.id.titleTextView)
        private val subTitle: TextView = itemView.findViewById(R.id.subtitleTextView)
        private val image: ImageView = itemView.findViewById(R.id.settingsImageView)
        private val avatar = settings.getString("avatar", "avatar")

        init {
            val selectableItemBackground = TypedValue()
            itemView.context.theme.resolveAttribute(android.R.attr.selectableItemBackground, selectableItemBackground, true)
            itemView.setBackgroundResource(selectableItemBackground.resourceId)
        }

        fun bind(setting: SettingsModel, clickListener: (SettingsModel) -> Unit) {
            // Bind data for the first item layout here
            // Bind data for the normal item layout here
            title.text = settings.getString("username", "login")
            subTitle.text = setting.subTitle

            subTitle.visibility = View.GONE
            Glide.with(image.context)
                .load(avatar)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .apply(RequestOptions.placeholderOf(R.drawable.google))
                .error(setting.imageBitmap)
                .into(image);



            itemView.setOnClickListener {
                clickListener(setting)
            }
        }
    }
    inner class PrivacyItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Bind views for the first item layout here

        private val title: TextView = itemView.findViewById(R.id.titleTextView)
        private val subTitle: TextView = itemView.findViewById(R.id.subtitleTextView)
        private val image: ImageView = itemView.findViewById(R.id.settingsImageView)

        init {
            val selectableItemBackground = TypedValue()
            itemView.context.theme.resolveAttribute(android.R.attr.selectableItemBackground, selectableItemBackground, true)
            itemView.setBackgroundResource(selectableItemBackground.resourceId)
        }

        fun bind(setting: SettingsModel, clickListener: (SettingsModel) -> Unit) {
            // Bind data for the first item layout here
            // Bind data for the normal item layout here
            title.text = setting.title
            subTitle.text = setting.subTitle


            itemView.setOnClickListener {
                clickListener(setting)
            }
        }
    }

    inner class ChatItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Bind views for the first item layout here

        private val title: TextView = itemView.findViewById(R.id.titleTextView)
        private val subTitle: TextView = itemView.findViewById(R.id.subtitleTextView)
        private val image: ImageView = itemView.findViewById(R.id.settingsImageView)

        init {
            val selectableItemBackground = TypedValue()
            itemView.context.theme.resolveAttribute(android.R.attr.selectableItemBackground, selectableItemBackground, true)
            itemView.setBackgroundResource(selectableItemBackground.resourceId)
        }

        fun bind(setting: SettingsModel, clickListener: (SettingsModel) -> Unit) {
            // Bind data for the first item layout here
            // Bind data for the normal item layout here
            title.text = setting.title
            subTitle.text = setting.subTitle


            itemView.setOnClickListener {
                clickListener(setting)
            }
        }
    }
    inner class NotificationsItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Bind views for the first item layout here

        private val title: TextView = itemView.findViewById(R.id.titleTextView)
        private val subTitle: TextView = itemView.findViewById(R.id.subtitleTextView)
        private val image: ImageView = itemView.findViewById(R.id.settingsImageView)

        init {
            val selectableItemBackground = TypedValue()
            itemView.context.theme.resolveAttribute(android.R.attr.selectableItemBackground, selectableItemBackground, true)
            itemView.setBackgroundResource(selectableItemBackground.resourceId)
        }

        fun bind(setting: SettingsModel, clickListener: (SettingsModel) -> Unit) {
            // Bind data for the first item layout here
            // Bind data for the normal item layout here
            title.text = setting.title
            subTitle.text = setting.subTitle


            itemView.setOnClickListener {
                clickListener(setting)
            }
        }
    }
    inner class StorageItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Bind views for the first item layout here

        private val title: TextView = itemView.findViewById(R.id.titleTextView)
        private val subTitle: TextView = itemView.findViewById(R.id.subtitleTextView)
        private val image: ImageView = itemView.findViewById(R.id.settingsImageView)

        init {
            val selectableItemBackground = TypedValue()
            itemView.context.theme.resolveAttribute(android.R.attr.selectableItemBackground, selectableItemBackground, true)
            itemView.setBackgroundResource(selectableItemBackground.resourceId)
        }

        fun bind(setting: SettingsModel, clickListener: (SettingsModel) -> Unit) {
            // Bind data for the first item layout here
            // Bind data for the normal item layout here
            title.text = setting.title
            subTitle.text = setting.subTitle


            itemView.setOnClickListener {
                clickListener(setting)
            }
        }
    }
    inner class DefaultItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Bind views for the first item layout here

        private val title: TextView = itemView.findViewById(R.id.titleTextView)
        private val subTitle: TextView = itemView.findViewById(R.id.subtitleTextView)
        private val image: ImageView = itemView.findViewById(R.id.settingsImageView)

        init {
            val selectableItemBackground = TypedValue()
            itemView.context.theme.resolveAttribute(android.R.attr.selectableItemBackground, selectableItemBackground, true)
            itemView.setBackgroundResource(selectableItemBackground.resourceId)
        }

        fun bind(setting: SettingsModel, clickListener: (SettingsModel) -> Unit) {
            // Bind data for the first item layout here
            // Bind data for the normal item layout here
            title.text = setting.title
            subTitle.text = setting.subTitle


            itemView.setOnClickListener {
                clickListener(setting)
            }
        }
    }
    inner class InviteItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Bind views for the first item layout here

        private val title: TextView = itemView.findViewById(R.id.titleTextView)
        private val subTitle: TextView = itemView.findViewById(R.id.subtitleTextView)
        private val image: ImageView = itemView.findViewById(R.id.settingsImageView)

        init {
            val selectableItemBackground = TypedValue()
            itemView.context.theme.resolveAttribute(android.R.attr.selectableItemBackground, selectableItemBackground, true)
            itemView.setBackgroundResource(selectableItemBackground.resourceId)
        }

        fun bind(setting: SettingsModel, clickListener: (SettingsModel) -> Unit) {
            // Bind data for the first item layout here
            // Bind data for the normal item layout here
            title.text = setting.title
            subTitle.text = setting.subTitle


            itemView.setOnClickListener {
                clickListener(setting)
            }
        }
    }
    inner class HelpItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Bind views for the first item layout here

        private val title: TextView = itemView.findViewById(R.id.titleTextView)
        private val subTitle: TextView = itemView.findViewById(R.id.subtitleTextView)
        private val image: ImageView = itemView.findViewById(R.id.settingsImageView)

        init {
            val selectableItemBackground = TypedValue()
            itemView.context.theme.resolveAttribute(android.R.attr.selectableItemBackground, selectableItemBackground, true)
            itemView.setBackgroundResource(selectableItemBackground.resourceId)
        }

        fun bind(setting: SettingsModel, clickListener: (SettingsModel) -> Unit) {
            // Bind data for the first item layout here
            // Bind data for the normal item layout here
            title.text = setting.title
            subTitle.text = setting.subTitle


            itemView.setOnClickListener {
                clickListener(setting)
            }
        }
    }

    inner class NormalItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Bind views for the normal item layout here
        private val title: TextView = view.findViewById(R.id.titleTextView)
        private val subTitle: TextView = view.findViewById(R.id.subtitleTextView)
        private val image: ImageView = view.findViewById(R.id.settingsImageView)

        init {
            val selectableItemBackground = TypedValue()
            itemView.context.theme.resolveAttribute(android.R.attr.selectableItemBackground, selectableItemBackground, true)
            itemView.setBackgroundResource(selectableItemBackground.resourceId)
        }


        fun bind(setting: SettingsModel, clickListener: (SettingsModel) -> Unit) {
            // Bind data for the normal item layout here
            title.text = setting.title
            subTitle.text = setting.subTitle
            Glide.with(image.context)
                .load(setting.imageBitmap)
                .error(setting.imageBitmap)
                .placeholder(R.drawable.google)
                .into(image);


            itemView.setOnClickListener {
                clickListener(setting)
            }
        }
    }
}
