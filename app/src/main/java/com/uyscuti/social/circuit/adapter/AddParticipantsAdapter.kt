package com.uyscuti.social.circuit.adapter

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
import com.uyscuti.social.circuit.data.model.User
import com.uyscuti.social.circuit.R

class AddParticipantsAdapter (
    private val context: Context,
    private val listener: (User) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var userList: MutableList<User> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.item_add_participant, parent, false)
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

    fun addUser(user: User) {
        this.userList.add(user)
        // Notify adapter about the new item
        notifyItemInserted(userList.size - 1)
        // Scroll to the last item

    }

    fun removeUser(user: User) {
        val position = userList.indexOf(user)
        if (position != -1) {
            userList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    // ViewHolder for user items
    private class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatarImageView: ImageView = itemView.findViewById(R.id.avatar)
        private val userNameTextView: TextView = itemView.findViewById(R.id.name)

        init {
            val selectableItemBackground = TypedValue()
            itemView.context.theme.resolveAttribute(android.R.attr.selectableItemBackground, selectableItemBackground, true)
            itemView.setBackgroundResource(selectableItemBackground.resourceId)
        }

        fun bind(user: User, listener: (User) -> Unit) {


            Glide.with(itemView.context)
                .load(user.avatar)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .into(avatarImageView)
            userNameTextView.text = user.name

            itemView.setOnClickListener {
                listener.invoke(user)
            }
        }
    }
}
