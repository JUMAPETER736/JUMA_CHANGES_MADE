package com.uyscuti.social.circuit.adapter

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.data.model.User

class CallUsersListAdapter(private val context: Context, private val listener:(User, Boolean) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var userList: MutableList<User> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.call_user_list_item, parent, false)
        return UserViewHolder(view)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is UserViewHolder) {
            holder.bind(userList[position], listener)
        }
    }

    fun setUserList(userList: List<User>) {
        this.userList = userList.toMutableList()
        notifyDataSetChanged()
    }

    // ViewHolder for user items
    private class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatarImageView: ImageView = itemView.findViewById(R.id.avatar)
        private val userNameTextView: TextView = itemView.findViewById(R.id.name)
        private val video: FrameLayout = itemView.findViewById(R.id.video)
        private val voice: FrameLayout = itemView.findViewById(R.id.voice)

        init {
            val selectableItemBackground = TypedValue()
            itemView.context.theme.resolveAttribute(android.R.attr.selectableItemBackground, selectableItemBackground, true)
            itemView.setBackgroundResource(selectableItemBackground.resourceId)

            voice.context.theme.resolveAttribute(android.R.attr.selectableItemBackground, selectableItemBackground, true)
            voice.setBackgroundResource(selectableItemBackground.resourceId)

            video.context.theme.resolveAttribute(android.R.attr.selectableItemBackground, selectableItemBackground, true)
            video.setBackgroundResource(selectableItemBackground.resourceId)
        }

        fun bind(user: User, listener: (User, Boolean) -> Unit) {
            // Bind data to the views
            // For example:

            Glide.with(itemView.context)
                .load(user.avatar)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .into(avatarImageView)
            userNameTextView.text = user.name

            video.setOnClickListener {
                listener.invoke(user, true)

            }

            voice.setOnClickListener {
                listener.invoke(user, false)
            }
        }
    }
}
