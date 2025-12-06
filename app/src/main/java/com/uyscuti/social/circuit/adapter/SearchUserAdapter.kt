package com.uyscuti.social.circuit.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.data.model.User

class SearchUserAdapter(

    private val context: Context,
    private val listener: (User) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var recentUserList: MutableList<User> = mutableListOf()
    private var searchUserList: MutableList<User> = mutableListOf()
    private var displayRecentUsers = true
    private var isLoading = false
    private var noResults = false
    private var isGroup = false

    private val SHIMMER_ITEM_COUNT = 10 // Set the desired number of shimmer items

    // View types
    private val TYPE_RECENT_HEADER = 0
    private val TYPE_SEARCH_HEADER = 1
    private val TYPE_USER = 2
    private val TYPE_LOADING = 3
    private val TYPE_NO_RESULTS = 4

    @SuppressLint("NotifyDataSetChanged")
    fun setRecentUsers(recentUsers: List<User>) {
        this.recentUserList = recentUsers.toMutableList()
        if (displayRecentUsers) {
            notifyDataSetChanged()
        } else {
            // If recent users were not displayed, update the flag and notify data change
            displayRecentUsers = true
            notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSearchUsers(searchUsers: List<User>) {
        this.searchUserList = searchUsers.toMutableList()
        isLoading = false
        displayRecentUsers = false
        noResults = false

        notifyDataSetChanged()
    }

    // Add this function to toggle the loading state
    fun setLoading(isLoading: Boolean) {
        this.isLoading = isLoading
        displayRecentUsers = false

        notifyDataSetChanged()
    }

    fun update(user: User) {
        val searchPosition = searchUserList.indexOf(user)
        val recentPosition = recentUserList.indexOf(user)

        if (searchPosition != -1) {

        } else if (recentPosition != -1) {
            // User is in recentUserList
            // Update the user at its current position
            recentUserList[recentPosition] = user
            // Notify the adapter that the item has changed
            notifyItemChanged(recentPosition)
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    fun setNoResults(){
        this.noResults = true
        notifyDataSetChanged()
    }

    fun setGroupSearch(isGroup: Boolean){
        this.isGroup = isGroup
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)

        return when (viewType) {
            TYPE_RECENT_HEADER -> {
                val view = inflater.inflate(
                    R.layout.recent_users_header, parent, false)
                HeaderViewHolder(view)
            }
            TYPE_SEARCH_HEADER -> {
                val view = inflater.inflate(
                    R.layout.search_results_header, parent, false)
                HeaderViewHolder(view)
            }
            TYPE_USER -> {
                val view = inflater.inflate(
                    R.layout.search_user_item, parent, false)

                UserViewHolder(view,isGroup)
            }
            TYPE_LOADING -> {
                val view = inflater.inflate(
                    R.layout.shimmer_search_user, parent, false)
                LoadingViewHolder(view)
            }
            TYPE_NO_RESULTS -> {
                val view = inflater.inflate(
                    R.layout.no_results_item, parent, false)
                NoResultsViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }




    override fun getItemCount(): Int {
        return when {
            displayRecentUsers -> recentUserList.size + 1
            isLoading -> SHIMMER_ITEM_COUNT  // Display a fixed number of shimmers
            noResults -> 1
            else -> searchUserList.size + 1
        }
    }



    override fun getItemViewType(position: Int): Int {
        return when {
            displayRecentUsers && position == 0 -> TYPE_RECENT_HEADER
            !displayRecentUsers && position == 0 -> TYPE_SEARCH_HEADER
            isLoading -> TYPE_LOADING
            noResults -> TYPE_NO_RESULTS
            else -> TYPE_USER
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                // Bind header data if needed
            }
            is UserViewHolder -> {
                val userPosition = if (displayRecentUsers) position - 1 else position - 1
                holder.bind(if (displayRecentUsers) recentUserList[userPosition] else searchUserList[userPosition], listener)
            }
            is LoadingViewHolder -> {
                if (isLoading) {
                    holder.showLoading()
                } else {
                    holder.hideLoading()
                }
            }

            is NoResultsViewHolder -> {

            }
        }
    }

    // ViewHolder for header
    private class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // You can customize this ViewHolder for the header if needed
    }

    // ViewHolder for user items
    private class UserViewHolder(itemView: View, isGroup: Boolean) : RecyclerView.ViewHolder(itemView) {
        private val avatarImageView: ImageView = itemView.findViewById(R.id.avatar)
        private val userNameTextView: TextView = itemView.findViewById(R.id.name)
        private val selected: ImageView = itemView.findViewById(R.id.selected)
        private val groupSearch = isGroup

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
                if (groupSearch){
                    selected.visibility = if (selected.isVisible) View.GONE else View.VISIBLE
                }
            }

        }
    }


    // ViewHolder for loading
    private class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        private val shimmerView: View = itemView.findViewById(R.id.shimmer_view)
        private val shimmerView2: View = itemView.findViewById(R.id.shimmer_view2)

        init {
           showLoading()
        }

        fun showLoading() {
            // Show the shimmering loading views
            shimmerView.visibility = View.VISIBLE
            shimmerView2.visibility = View.VISIBLE

            // Start shimmer effect
            startShimmerEffect()
        }

        fun hideLoading() {
            // Hide the shimmering loading views
            shimmerView.visibility = View.GONE
            shimmerView2.visibility = View.GONE

            // Stop shimmer effect
            stopShimmerEffect()
        }

        private fun startShimmerEffect() {
            val handler = Handler()
            val shimmerRunnable = object : Runnable {
                override fun run() {
                    // Update alpha (transparency) of shimmer views
                    shimmerView.alpha = 0.7f
                    shimmerView2.alpha = 0.7f

                    // Delay and update alpha back to create shimmer effect
                    handler.postDelayed({
                        shimmerView.alpha = 1f
                        shimmerView2.alpha = 1f
                    }, 500)

                    // Repeat the process for continuous shimmering
                    handler.postDelayed(this, 600)
                }
            }

            // Start shimmer effect
            handler.post(shimmerRunnable)
        }

        private fun stopShimmerEffect() {
            // Stop the shimmer effect
            // You may need to clean up any resources or animations here
        }

    }

    // ViewHolder for no results
    private class NoResultsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // You can customize this ViewHolder for the no results message if needed
    }
}

