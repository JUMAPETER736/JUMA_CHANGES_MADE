package com.uyscuti.social.circuit.adapter.feed

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.circuit.R

class ShareVideoAdapter(
    private val appsList: List<ResolveInfo>,
    private val context: Context,
    position: Int,
) : RecyclerView.Adapter<ShareVideoAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appIcon: ImageView = view.findViewById(R.id.app_icon)
        val appName: TextView = view.findViewById(R.id.app_name)
        val shareItem: LinearLayout = view.findViewById(R.id.shareItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_share_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val resolveInfo = appsList[position]
        val packageManager = context.packageManager

        // Set app name and icon
        holder.appName.text = resolveInfo.loadLabel(packageManager)
        holder.appIcon.setImageDrawable(resolveInfo.loadIcon(packageManager))

        // Handle click to share
        holder.shareItem.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT,position)
                setPackage(resolveInfo.activityInfo.packageName) // Open specific app
            }
            context.startActivity(shareIntent)
        }
    }

    override fun getItemCount() = appsList.size
}