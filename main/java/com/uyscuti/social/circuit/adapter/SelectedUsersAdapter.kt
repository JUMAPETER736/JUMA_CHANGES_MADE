package com.uyscuti.social.circuit.adapter

import android.content.Context
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
import com.uyscuti.social.circuit.data.model.User

class SelectedUsersAdapter(private val context: Context, private val listener: (User) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>()  {
    private var selectedUserList: MutableList<User> = mutableListOf()


    fun addUser(user: User) {
        this.selectedUserList.add(user)
        // Notify adapter about the new item
        notifyItemInserted(selectedUserList.size - 1)
        // Scroll to the last item
    }

    fun removeUser(user: User) {
        val position = selectedUserList.indexOf(user)
        if (position != -1) {
            selectedUserList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.selected_user_layout, parent, false)
        return UserViewHolder(view)
    }

    override fun getItemCount(): Int {
        return selectedUserList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is UserViewHolder) {
            holder.bind(selectedUserList[position], listener)
        }
    }

    private class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val selectedUserAvatar: ImageView = itemView.findViewById(R.id.selected_user_avatar)
        private val selectedUserName: TextView = itemView.findViewById(R.id.selected_user_name)

        fun bind(user: User, listener: (User) -> Unit) {
            Glide.with(itemView.context)
                .load(user.avatar)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .into(selectedUserAvatar)
            selectedUserName.text = user.name

            itemView.setOnClickListener {
                listener.invoke(user)
            }
        }
    }
}