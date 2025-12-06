package com.uyscuti.social.circuit.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.data.model.User


private const val VIEW_TYPE_ADMIN = 0
private const val VIEW_TYPE_NORMAL = 1

class GroupParticipantAdapter(private val participantList: List<User>) :
    RecyclerView.Adapter<GroupParticipantAdapter.ViewHolder>() {
    //
    private var adminId: String? = null
    fun setAdminId(adminId: String) {
        this.adminId = adminId
        notifyDataSetChanged() // Notify the adapter that the data has changed
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val image: ImageView = itemView.findViewById(R.id.avatar)
        val nameTv: TextView = itemView.findViewById(R.id.name)
        val linearLayout: LinearLayout = itemView.findViewById(R.id.linear)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutResId = when (viewType) {
            VIEW_TYPE_ADMIN -> R.layout.group_participant_admin_list_item
            VIEW_TYPE_NORMAL -> R.layout.group_participant_list_item
            else -> throw IllegalArgumentException("Invalid view type: $viewType")
        }

        val itemView = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        return ViewHolder(itemView)
    }



    override fun getItemViewType(position: Int): Int {
        val user = participantList[position]
        return if (user.id == adminId) {
            VIEW_TYPE_ADMIN
        } else {
            VIEW_TYPE_NORMAL
        }
    }

    override fun getItemCount(): Int {
        return participantList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = participantList[position]

        // Common bindings
        holder.nameTv.text = currentItem.name

        Glide.with(holder.itemView.context)
            .load(currentItem.avatar)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .into(holder.image)

        when (getItemViewType(position)) {
            VIEW_TYPE_ADMIN -> {

                holder.linearLayout.setOnClickListener {
                    // Handle admin click
                }
            }

            VIEW_TYPE_NORMAL -> {
                // Bindings for normal users
                holder.linearLayout.setOnClickListener {
                    // Handle normal user click
                }
            }
        }
    }


}