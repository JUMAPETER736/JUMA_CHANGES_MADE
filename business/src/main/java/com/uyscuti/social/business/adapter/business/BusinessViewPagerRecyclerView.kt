package com.uyscuti.social.business.adapter.business

import android.app.Activity
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.business.R

class BusinessViewPagerRecyclerView(
    private val context: Activity,
    private val mediaUrl: List<String>
): RecyclerView.Adapter<BusinessViewPagerRecyclerView.PagerViewHolder>() {

    private var pages = mediaUrl.chunked(4)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PagerViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.view_pager_recycler_view,parent,false)
        return PagerViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: PagerViewHolder,
        position: Int
    ) {
        holder.bind(pages[position])
    }

    override fun getItemCount(): Int {
        return  pages.size
    }


    inner class PagerViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val recyclerView: RecyclerView = itemView.findViewById(R.id.pager_recycler_view)



        fun bind(mediaUrl: List<String>) {
         //   val businessMediaViewPager = BusinessMediaViewPager(context,mediaUrl)

            // Set up RecyclerView with custom layout manager
            recyclerView.layoutManager = GridLayoutManager(context, 2).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return when (mediaUrl.size) {
                            1 -> 2 // Single item takes full width
                            2 -> 1 // Two items, each takes 1 span
                            3 -> if (position < 2) 1 else 2 // First two items 1 span each, third takes full width
                            4 -> 1 // All items take 1 span each (2x2 grid)
                            else -> 1
                        }
                    }
                }
            }

            // Add spacing decoration
            if (recyclerView.itemDecorationCount == 0) {
                recyclerView.addItemDecoration(
                    MediaGridSpacingDecoration(
                        spacing = 2,
                        spanCount = 2,
                        itemCount = mediaUrl.size
                    )
                )
            }


              //  recyclerView.adapter = businessMediaViewPager

        }
    }
}

class MediaGridSpacingDecoration(
    private val spacing: Int,
    private val spanCount: Int,
    private val itemCount: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % spanCount

        when (itemCount) {
            1 -> {
                // Single item - center it
                outRect.left = spacing
                outRect.right = spacing
            }
            2 -> {
                // Two items side by side
                if (column == 0) {
                    outRect.left = spacing
                    outRect.right = spacing / 2
                } else {
                    outRect.left = spacing / 2
                    outRect.right = spacing
                }
            }
            3 -> {
                // Three items - first two on top, third centered below
                when (position) {
                    0 -> {
                        outRect.left = spacing
                        outRect.right = spacing / 2
                    }
                    1 -> {
                        outRect.left = spacing / 2
                        outRect.right = spacing
                    }
                    2 -> {
                        outRect.left = spacing
                        outRect.right = spacing
                    }
                }
            }
            4 -> {
                // Four items in 2x2 grid
                if (column == 0) {
                    outRect.left = spacing
                    outRect.right = spacing / 2
                } else {
                    outRect.left = spacing / 2
                    outRect.right = spacing
                }
            }
        }

        // Vertical spacing
        if (position < spanCount) {
            outRect.top = spacing
        }
        outRect.bottom = spacing
    }
}