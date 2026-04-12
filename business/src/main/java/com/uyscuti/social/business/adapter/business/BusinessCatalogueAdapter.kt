package com.uyscuti.social.business.adapter.business

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.core.models.data.User
import com.uyscuti.social.business.R
import com.uyscuti.social.business.adapter.business.postViewHolder.BusinessPostViewHolder
import com.uyscuti.social.network.api.response.business.response.post.Post
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage

class BusinessCatalogueAdapter(
    private val context: Activity,
    private val retrofitInterface: RetrofitInstance,
    private val localStorage: LocalStorage,
    private val onItemClick: (Post) -> Unit = {},
    private var businessClickedListener: OnBusinessClickedListener,
    private val onBookmarkClick: (Post) -> Unit = { _ -> },
    private val onFollowClick: (Post) -> Unit = { _ -> },
    private val onMessageClick: (User, Post) -> Unit = { _ , _-> },
    private val onSendOfferClicked: (Double, String, Post) -> Unit = { _, _, _->},
    private val fragmentManager: FragmentManager,
    private val onLoadMore: (() -> Unit)? = null
): ListAdapter<Post, RecyclerView.ViewHolder>(POST_COMPARATOR) {

    companion object {
        private const val VIEW_TYPE_POST = 0
        private const val VIEW_TYPE_LOADING = 1

        private val POST_COMPARATOR = object : DiffUtil.ItemCallback<Post>() {
            override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
                return oldItem._id == newItem._id
            }

            override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
                return oldItem.itemName == newItem.itemName &&
                        oldItem.description == newItem.description &&
                        oldItem.comments == newItem.comments &&
                        oldItem.likes == newItem.likes &&
                        oldItem.isLiked == newItem.isLiked &&
                        oldItem.isBookmarked == newItem.isBookmarked
            }

            override fun getChangePayload(oldItem: Post, newItem: Post): Any? {
                val changes = mutableMapOf<String, Any>()

                if (oldItem.itemName != newItem.itemName) {
                    changes["name"] = newItem.itemName
                }
                if (oldItem.description != newItem.description) {
                    changes["description"] = newItem.description
                }
                if (oldItem.comments != newItem.comments) {
                    changes["comments"] = newItem.comments
                }
                if (oldItem.likes != newItem.likes) {
                    changes["likes"] = newItem.likes
                }
                if (oldItem.isLiked != newItem.isLiked) {
                    changes["isLiked"] = newItem.isLiked
                }
                if (oldItem.isBookmarked != newItem.isBookmarked) {
                    changes["isBookmarked"] = newItem.isBookmarked
                }

                return if (changes.isNotEmpty()) changes else null
            }
        }
    }

    private var isLoadingMore = false
    private var hasMoreData = true

    /**
     * ✅ NEW: Clear list immediately without animations
     * Use this when switching between browse/search modes
     */
    fun clearCatalogue() {
        submitList(null)
    }

    /**
     * Regular update with DiffUtil animations (for same mode updates)
     */
    fun updateCatalogue(newCatalogue: List<Post>) {
        submitList(newCatalogue.toList())
    }

    /**
     * Append new items for pagination (within same mode)
     */
    fun appendCatalogue(newPosts: List<Post>) {
        if (newPosts.isEmpty()) {
            hasMoreData = false
            setLoadingMore(false)
            return
        }

        val currentList = currentList.toMutableList()
        currentList.addAll(newPosts)
        submitList(currentList)
        setLoadingMore(false)
    }

    /**
     * Add single item to top (e.g., new post created)
     */
    fun addCatalogue(post: Post) {
        val currentList = currentList.toMutableList()
        currentList.add(0, post)
        submitList(currentList)
    }

    /**
     * Update single post (e.g., after like/bookmark)
     */
    fun updatePost(postId: String, updater: (Post) -> Post) {
        val currentList = currentList.toMutableList()
        val index = currentList.indexOfFirst { it._id == postId }
        if (index != -1) {
            currentList[index] = updater(currentList[index])
            submitList(currentList)
        }
    }

    /**
     * Remove post
     */
    fun removePost(postId: String) {
        val currentList = currentList.toMutableList()
        currentList.removeAll { it._id == postId }
        submitList(currentList)
    }

    fun setLoadingMore(loading: Boolean) {
        val wasLoading = isLoadingMore
        isLoadingMore = loading

        if (wasLoading && !loading) {
            notifyItemRemoved(currentList.size)
        } else if (!wasLoading && loading) {
            notifyItemInserted(currentList.size)
        }
    }

    fun setHasMoreData(hasMore: Boolean) {
        hasMoreData = hasMore
    }

    fun resetPagination() {
        hasMoreData = true
        isLoadingMore = false
    }



    fun updateCommentCount(position: Int) {
        if (position >= 0 && position < currentList.size) {
            val currentList = currentList.toMutableList()
            val post = currentList[position]
            currentList[position] = post.copy(comments = post.comments + 1)
            submitList(currentList)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < currentList.size) VIEW_TYPE_POST else VIEW_TYPE_LOADING
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_POST -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.business_post_layout, parent, false)
                BusinessPostViewHolder(
                    view, context, businessClickedListener, retrofitInterface,
                    localStorage, onItemClick, onBookmarkClick, onFollowClick,
                    onMessageClick, onSendOfferClicked, fragmentManager
                )
            }
            VIEW_TYPE_LOADING -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_loading, parent, false)
                LoadingViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is BusinessPostViewHolder -> {
                if (position < currentList.size) {
                    holder.bind(getItem(position), position)

                    // Trigger load more when near end of list
                    if (position == currentList.size - 3 && !isLoadingMore && hasMoreData) {
                        onLoadMore?.invoke()
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return currentList.size + if (isLoadingMore) 1 else 0
    }

    inner class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}

interface OnBusinessClickedListener {
    fun businessCommentClickedListener(
        position: Int,
        post: Post
    )
}