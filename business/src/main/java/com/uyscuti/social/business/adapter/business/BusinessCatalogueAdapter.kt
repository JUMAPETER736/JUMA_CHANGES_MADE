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

    @SuppressLint("NotifyDataSetChanged")
    fun updateCatalogue(newCatalogue: List<Post>) {
        val diffCallBack = CatalogueDiffCallBack(catalogue, newCatalogue)
        val diffResults = DiffUtil.calculateDiff(diffCallBack)

        catalogue.clear()
        catalogue.addAll(newCatalogue)
        diffResults.dispatchUpdatesTo(this)
        notifyDataSetChanged()
    }

    fun addCatalogue(post: Post) {
        catalogue.add(0,post)
        notifyItemInserted(0)
    }

    fun setLoadingMore(loading: Boolean) {
        val wasLoading = isLoadingMore
        isLoadingMore = loading

        if (wasLoading && !loading) {
            notifyItemRemoved(catalogue.size)
        } else if (!wasLoading && loading) {
            notifyItemInserted(catalogue.size)
        }
    }

    fun updateCommentCount(position: Int) {
        var count = catalogue[position].comments
        ++count
        catalogue[position].comments = count
        notifyItemChanged(position)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BusinessPostViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.business_post_layout, parent, false)
        return BusinessPostViewHolder(
            view,
            context,
            businessClickedListener,
            retrofitInterface,
            localStorage,
            onItemClick,
            onBookmarkClick,
            onFollowClick,
            onMessageClick,
            fragmentManager
        )
    }

    override fun onBindViewHolder(
        holder: BusinessPostViewHolder,
        position: Int
    ) {
        if(position < catalogue.size) {
            holder.bind(catalogue[position], position)
        }
    }

    override fun getItemCount(): Int {
        return catalogue.size + if (isLoadingMore) 1 else 0
    }



    inner class CatalogueDiffCallBack(
        private val oldList: List<Post>,
        private val newList: List<Post>
    ): DiffUtil.Callback() {

        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(
            oldItemPosition: Int,
            newItemPosition: Int
        ): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }

        override fun areContentsTheSame(
            oldItemPosition: Int,
            newItemPosition: Int
        ): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]

            // Return a payload indicating what changed for partial updates
            val changes = mutableMapOf<String, Any>()

            if (oldItem.itemName != newItem.itemName) {
                changes["name"] = newItem.itemName
            }
            if (oldItem.description != newItem.description) {
                changes["description"] = newItem.description
            }

            return if (changes.isNotEmpty()) changes else null
        }

    }

}

interface OnBusinessClickedListener {

    fun businessCommentClickedListener(
        position: Int,
        post: Post
    )
}


