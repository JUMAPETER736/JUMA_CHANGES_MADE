package com.uyscuti.social.circuit.adapter

import android.app.Activity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


abstract class AdvancePaginatedAdapter<ITEM, VH : RecyclerView.ViewHolder?> :
    RecyclerView.Adapter<VH>() {
    val mDataSet: MutableList<ITEM> = ArrayList()
    private var mListener: OnPaginationListener? = null
    private var mStartPage = 1
    var currentPage = 1
        private set
    private var mPageSize = 10
    private var mRecyclerView: RecyclerView? = null
    private var loadingNewItems = true
    private var loadMoreListener: LoadMoreListener? = null
    fun setLoadMoreListener(loadMoreListener: LoadMoreListener?) {
        this.loadMoreListener = loadMoreListener
    }

    override fun getItemViewType(position: Int): Int {
        return if (mDataSet.isEmpty()) VIEW_TYPE_EMPTY else VIEW_TYPE_ITEM
    }

    override fun getItemCount(): Int {
        return if (mDataSet.isEmpty()) 1 else mDataSet.size
    }

    fun submitItems(collection: Collection<ITEM>) {
        mDataSet.addAll(collection)
        notifyDataSetChanged()
        if (mListener != null) {
            mListener!!.onCurrentPage(currentPage)
            if (collection.size == mPageSize) {
                loadingNewItems = false
            } else {
                mListener!!.onFinish()
            }
        }
    }

    fun submitItem(item: ITEM) {
        mDataSet.add(item)
        notifyDataSetChanged()
    }

    fun submitItem(item: ITEM, position: Int) {
        mDataSet.add(position, item)
        notifyDataSetChanged()
    }

    fun clear() {
        mDataSet.clear()
        notifyDataSetChanged()
    }

    protected fun getItem(position: Int): ITEM {
        return mDataSet[position]
    }

    var startPage: Int
        get() = mStartPage
        set(mFirstPage) {
            mStartPage = mFirstPage
            currentPage = mFirstPage
        }
    var recyclerView: RecyclerView?
        get() = mRecyclerView
        set(recyclerView) {
            mRecyclerView = recyclerView
            initPaginating()
            setAdapter()
        }

    fun setDefaultRecyclerView(activity: Activity, recyclerViewId: Int) {
        val recyclerView = activity.findViewById<RecyclerView>(recyclerViewId)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setHasFixedSize(true)
        mRecyclerView = recyclerView
        initPaginating()
        setAdapter()
    }

    private fun setAdapter() {
        mRecyclerView!!.adapter = this
    }

    private fun initPaginating() {
        mRecyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
                val totalItemCount = layoutManager!!.itemCount
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                val endHasBeenReached = lastVisible + 2 >= totalItemCount
                if (totalItemCount > 0 && endHasBeenReached) {
                    if (mListener != null) {
                        if (!loadingNewItems) {
                            loadingNewItems = true
                            mListener!!.onNextPage(++currentPage)
                        }
                    }
                }
            }
        })
    }

    fun setOnPaginationListener(onPaginationListener: OnPaginationListener?) {
        mListener = onPaginationListener
    }

    interface OnPaginationListener {
        fun onCurrentPage(page: Int)
        fun onNextPage(page: Int)
        fun onFinish()
    }

    interface LoadMoreListener {
        fun onLoadMore(pageNumber: Int)
    }

    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_EMPTY = 1
    }
}

