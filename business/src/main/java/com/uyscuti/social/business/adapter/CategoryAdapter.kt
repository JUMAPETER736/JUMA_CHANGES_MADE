package com.uyscuti.social.business.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.business.model.Section
import com.uyscuti.social.business.R

class CategoryAdapter() : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {


    private var categoryList: ArrayList<Section> = arrayListOf()

    init {
        val audience = Section(
            title = "Women's clothing",
            icon = R.drawable.dress
        )


        categoryList.add(audience)

        val assets = Section(
            title = "Accessories",
            icon = R.drawable.phone
        )
        categoryList.add(assets)

        val ads = Section(
            title = "books",
            icon = R.drawable.mega
        )
        categoryList.add(ads)

        val analytics = Section(
            title = "cars & bikes",
            icon = R.drawable.car
        )
        categoryList.add(analytics)

        val manager = Section(
            title = "farm products",
            icon = R.drawable.fruits
        )
        categoryList.add(manager)
        val media = Section(
            title = "Books",
            icon = R.drawable.books
        )

        categoryList.add(media)
        val mediat = Section(
            title = "DVDs & Videos\n",
            icon = R.drawable.disk
        )
        categoryList.add(mediat)
        val music = Section(
            title = " Music & Sound Recordings\n",
            icon = R.drawable.mega
        )
        categoryList.add(music)
        val software = Section(
            title = "Software > Video Game Software\n",
            icon = R.drawable.software)

        categoryList.add(software)
        val games = Section(
            title="games",
            icon= R.drawable.games)

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.category_item, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categoryList[position]
        holder.bind(category)
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryTextView: TextView = itemView.findViewById(R.id.category_name)
        private val categoryIcon: ImageView = itemView.findViewById(R.id.category_icon)


        fun bind(category: Section) {
            categoryTextView.text = category.title
            categoryIcon.setImageResource(category.icon)
        }
    }
}