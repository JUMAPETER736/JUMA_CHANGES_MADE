package com.uyscuti.sharedmodule.adapter.feed.feed.multiple_files

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.sharedmodule.R

private const val TAG = ""

class MultipleFeedDocAdapter(
    private var context: Context,
    private var documentList: List<Uri>,
    private var documentListenerInterface: DocumentListenerInterface

) :
    RecyclerView.Adapter<MultipleFeedDocAdapter.Pager2ViewHolder>() {
    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Pager2ViewHolder {
        return Pager2ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.feed_multiple_documents_view_item, parent, false)
        )
    }

    override fun onBindViewHolder(
        holder: Pager2ViewHolder,
        position: Int
    ) {
       holder.itemView.setOnClickListener {
           documentListenerInterface.onDocumentClickListener()
       }
    }

    fun getDocumentUri(position: Int): Uri {
        return documentList[position]
    }
    override fun getItemCount(): Int {
        return documentList.size
    }
}

interface DocumentListenerInterface {
    fun onDocumentClickListener()
}