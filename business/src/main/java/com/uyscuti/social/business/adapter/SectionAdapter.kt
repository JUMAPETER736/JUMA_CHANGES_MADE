package com.uyscuti.social.business.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.uyscuti.social.business.model.Section
import com.uyscuti.social.business.R


class SectionAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var sections: ArrayList<Section> = arrayListOf()

    init {
        val audience = Section(
            title = "Audience",
            icon = R.drawable.auience
        )
        sections.add(audience)

        val assets = Section(
            title = "Assets",
            icon = R.drawable.assets
        )
        sections.add(assets)

        val ads = Section(
            title = "Ads",
            icon = R.drawable.mega
        )
        sections.add(ads)

        val analytics = Section(
            title = "Analytics",
            icon = R.drawable.analystics
        )
        sections.add(analytics)

        val manager = Section(
            title = "Manager",
            icon = R.drawable.manager
        )
//        sections.add(manager)

        val settings = Section(
            title = "Settings",
            icon = R.drawable.settings
        )
        sections.add(settings)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.section_item, parent, false)
        return SectionViewHolder(view)
    }

    override fun getItemCount(): Int {
        return sections.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val section = sections[position]
        if (holder is SectionViewHolder) {
            holder.bind(section)
        }
    }

    class SectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.section_label)
        private val sectionIcon: ImageView = itemView.findViewById(R.id.section_icon)

        fun bind(section: Section) {
            titleTextView.text = section.title
            sectionIcon.setImageResource(section.icon)
        }
    }
}
