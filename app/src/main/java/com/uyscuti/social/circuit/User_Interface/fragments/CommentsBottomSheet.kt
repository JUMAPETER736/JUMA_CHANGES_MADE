package com.uyscuti.social.circuit.User_Interface.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.adapter.CommentAdapter
import com.uyscuti.social.network.api.response.comment.allcomments.Comment



class CommentsBottomSheet(
    private val context: Context,
    private val dataList: ArrayList<Comment>,
    private var commentCount: Int?, // Total number of comments on the server
    private val onCommentCountChanged: ((Int) -> Unit)? = null,
    onCommentDeleted: () -> Unit,
    onCommentAdded: () -> Unit,
    postId: String
) {

    private lateinit var dialog: BottomSheetDialog
    private lateinit var recyclerView: RecyclerView
    private lateinit var itemAdapter: CommentAdapter
    private lateinit var commentCountTextView: TextView

    @SuppressLint("InflateParams")
    fun showBottomSheet() {
        val dialogView = LayoutInflater.from(context).inflate(
            R.layout.activity_bottom_sheet1, null)

        dialog = BottomSheetDialog(context, R.style.BottomSheetDialogTheme)
        dialog.setContentView(dialogView)

        recyclerView = dialogView.findViewById(R.id.recyclerView)
        commentCountTextView = dialogView.findViewById(R.id.tvCommentsTitle)

        itemAdapter = CommentAdapter(dataList)
        recyclerView.adapter = itemAdapter

        updateCommentCount()
        dialog.show()

        // Set height to half of the screen and expand sheet
        val bottomSheet = dialog.findViewById<FrameLayout>(
            com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            val halfHeight = (Resources.getSystem().displayMetrics.heightPixels * 0.5).toInt()
            it.layoutParams.height = halfHeight
            it.requestLayout()

            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
        }
    }

    fun dismissBottomSheet() {
        if (::dialog.isInitialized && dialog.isShowing) {
            dialog.dismiss()
        }
    }

    fun getCommentCount(): Int? = commentCount

    fun getLoadedCommentCount(): Int = dataList.size

    private fun updateCommentCount() {
        val count = getCommentCount()
        if (::commentCountTextView.isInitialized) {
            if (count != null) {
                commentCountTextView.text = if (count > 0) "Comments ($count)" else "Comments"
            }
        }
        onCommentCountChanged?.invoke(count ?: 0)
    }


    fun addComment(comment: Comment) {
        dataList.add(comment)
        commentCount = commentCount!! + 1 // Increment total count when adding a new comment
        if (::itemAdapter.isInitialized) {
            itemAdapter.notifyItemInserted(dataList.size - 1)
        }
        updateCommentCount()
    }

    fun removeComment(position: Int) {
        if (position in dataList.indices) {
            dataList.removeAt(position)
            commentCount = commentCount!! - 1 // Decrement total count when removing a comment
            if (::itemAdapter.isInitialized) {
                itemAdapter.notifyItemRemoved(position)
            }
            updateCommentCount()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refreshComments(newCommentsList: ArrayList<Comment>) {
        dataList.clear()
        dataList.addAll(newCommentsList)
        if (::itemAdapter.isInitialized) {
            itemAdapter.notifyDataSetChanged()
        }
        updateCommentCount()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refreshComments(newCommentsList: ArrayList<Comment>, newTotalCount: Int) {
        dataList.clear()
        dataList.addAll(newCommentsList)
        commentCount = newTotalCount
        if (::itemAdapter.isInitialized) {
            itemAdapter.notifyDataSetChanged()
        }
        updateCommentCount()
    }

    fun updateTotalCommentCount(newTotalCount: Int) {
        commentCount = newTotalCount
        updateCommentCount()
    }

}