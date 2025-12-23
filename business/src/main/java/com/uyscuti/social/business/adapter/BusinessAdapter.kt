package com.uyscuti.social.business.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

class BusinessAdapter : RecyclerView.Adapter<ViewHolder>() {
    private val sections: ArrayList<String> = arrayListOf()

    init {
        sections.add("Audience")
        sections.add("Daily Budget")
        sections.add("Duration")
        sections.add("Payment Method")
        sections.add("Payment Summary")




    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }
}