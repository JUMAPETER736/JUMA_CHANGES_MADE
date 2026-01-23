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

/**
 * Adapter for displaying hidden posts
 */
class HiddenPostsAdapter(
    private val context: Context,
    private val onUnhideClick: (String, Int) -> Unit
) : RecyclerView.Adapter<HiddenPostsAdapter.HiddenPostViewHolder>() {

    private var hiddenPostsList = mutableListOf<HiddenPostItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HiddenPostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.hidden_post_item, parent, false)
        return HiddenPostViewHolder(view)
    }

    override fun onBindViewHolder(holder: HiddenPostViewHolder, position: Int) {
        holder.bind(hiddenPostsList[position], position)
    }

    override fun getItemCount(): Int = hiddenPostsList.size

    fun updateList(newList: List<HiddenPostItem>) {
        hiddenPostsList.clear()
        hiddenPostsList.addAll(newList)
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        if (position >= 0 && position < hiddenPostsList.size) {
            hiddenPostsList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, hiddenPostsList.size)
        }
    }

    inner class HiddenPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val authorAvatar: ImageView = itemView.findViewById(R.id.authorAvatar)
        private val authorUsername: TextView = itemView.findViewById(R.id.authorUsername)
        private val postContent: TextView = itemView.findViewById(R.id.postContent)
        private val hiddenAtText: TextView = itemView.findViewById(R.id.hiddenAtText)
        private val unhideButton: TextView = itemView.findViewById(R.id.unhideButton)

        init {
            val selectableItemBackground = TypedValue()
            itemView.context.theme.resolveAttribute(
                android.R.attr.selectableItemBackground,
                selectableItemBackground,
                true
            )
            itemView.setBackgroundResource(selectableItemBackground.resourceId)
        }

        fun bind(item: HiddenPostItem, position: Int) {
            // Set author username
            authorUsername.text = "@${item.authorUsername}"

            // Set post content with max 3 lines
            postContent.text = item.content
            postContent.maxLines = 3

            // Set hidden date
            hiddenAtText.text = "Hidden on ${item.hiddenAt}"

            // Load author avatar
            Glide.with(authorAvatar.context)
                .load(item.authorAvatar?.url)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .placeholder(R.drawable.round_user)
                .error(R.drawable.round_user)
                .into(authorAvatar)

            // Unhide button click
            unhideButton.setOnClickListener {
                onUnhideClick(item.postId, position)
            }

            // Navigate to post on item click
            itemView.setOnClickListener {
                // TODO: Navigate to post detail
                // val intent = Intent(context, PostDetailActivity::class.java)
                // intent.putExtra("postId", item.postId)
                // context.startActivity(intent)
            }
        }
    }
}