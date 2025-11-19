package com.uyscuti.social.business.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uyscuti.social.business.R
import com.uyscuti.social.business.retro.model.User


class UsersAdapter(
    private val users: ArrayList<com.uyscuti.social.business.model.User>,
    private val onItemClick: (User) -> Unit
) : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.textViewUserName.text = user.username
        Glide.with(holder.itemView.context)
            .load(user.avatar)
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(holder.imageViewAvatar)

        holder.itemView.setOnClickListener {
            onItemClick(user)
        }
    }

    override fun getItemCount(): Int = users.size

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewAvatar: ImageView = itemView.findViewById(R.id.imageViewAvatar)
        val textViewUserName: TextView = itemView.findViewById(R.id.textViewUserName)
    }
}

private fun UsersAdapter.onItemClick(user: com.uyscuti.social.business.model.User) {}

