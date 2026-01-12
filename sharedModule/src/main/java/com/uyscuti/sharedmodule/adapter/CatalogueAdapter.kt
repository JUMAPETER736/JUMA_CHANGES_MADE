package com.uyscuti.sharedmodule.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uyscuti.sharedmodule.R
import com.uyscuti.sharedmodule.model.Catalogue
import com.uyscuti.sharedmodule.popupDialog.AdsServiceAgreementDialog

class CatalogueAdapter(val context: Activity, private val items: ArrayList<Catalogue>) :
    RecyclerView.Adapter<CatalogueAdapter.CatalogueItemViewHolder>() {
    var adapterPosition = -1

    private val OPEN_IMAGE_VIEW_ACTIVITY = "com.uyscuti.social.circuit.ImageViewActivity"

    var selectedItems = mutableListOf<Catalogue>()

    var onItemSelectedListener: ((Boolean) -> Unit)? = null

    private var  boostAgreement: AdsServiceAgreementDialog? = null

    fun updateCatalogue(newCatalogue: List<Catalogue>) {
        val diffCallBack = CatalogueDiffCallBack(items, newCatalogue)
        val diffResults = DiffUtil.calculateDiff(diffCallBack)

        items.clear()
        items.addAll(newCatalogue)
        diffResults.dispatchUpdatesTo(this)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatalogueItemViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.new_catelogue, parent, false)
            return CatalogueItemViewHolder(view)
    }



    private fun toggleSelection(item: Catalogue) {
        item.isSelected = !item.isSelected
        if (item.isSelected) {
            selectedItems.add(item)
        } else {
            selectedItems.remove(item)
        }
        notifyDataSetChanged()
        onItemSelectedListener?.invoke(selectedItems.isNotEmpty())
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onBindViewHolder(holder: CatalogueItemViewHolder, position: Int) {
        adapterPosition = position


            if (position == items.size){
                notifyDataSetChanged()
                return
            }
            // Handle logic for catalogue item view holder
            val catalogueItemHolder = holder
            val item = items[position]

            catalogueItemHolder.productName.text = item.name
            catalogueItemHolder.product_description.text=item.description
            val itemPrice = item.price // Convert the price to string
            catalogueItemHolder.price_text.text = itemPrice // Set the price to the TextView
//            catalogueItemHolder.addItem.text = item.name
//
//            // Set the image resource for the ImageView

            holder.itemView.isSelected = item.isSelected

            holder.itemView.setBackgroundColor(if (item.isSelected) Color.LTGRAY else Color.WHITE)

            if (item.images.isNotEmpty()) {

                Glide.with(context).load(item.images[0])
                    .centerCrop()
                    .into(catalogueItemHolder.productImage)
            }

        catalogueItemHolder.boost.setOnClickListener {
            showBoostServiceAgreement(item)
        }

            catalogueItemHolder.itemContainer.setOnClickListener {
//
//                if (selectedItems.isNotEmpty()) {
//                    toggleSelection(item)
//
//                    return@setOnClickListener
//                }
                val intent = Intent(OPEN_IMAGE_VIEW_ACTIVITY)
                intent.putExtra("catalogue", item)
                intent.setPackage(context.packageName)
                context.startActivity(intent)
            }

            catalogueItemHolder.itemContainer.setOnLongClickListener {
                toggleSelection(item)
                true
            }

            // Implement other logic or event handling for com.example.payment.adapter.CatalogueItemViewHolder

    }

    override fun getItemCount(): Int {
        // Add 1 for the add item view holder
        return items.size
    }

    private fun showBoostServiceAgreement(item: Catalogue) {
        boostAgreement = AdsServiceAgreementDialog.Builder(context)
            .setOnAcceptListener { hasAccepted ->  }
            .setDismissListener {  }
            .setOnDeclineListener {
                boostAgreement?.dismiss()
            }
            .setCancelable(false)
            .show()

        Log.d("Dialog", "Inside Boost service agreement")
    }

    class CatalogueItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productName: TextView = itemView.findViewById(R.id.product_name)
        val product_description: TextView = itemView.findViewById(R.id.product_description)
        val price_text: TextView = itemView.findViewById(R.id.price_text)
        val productImage: ImageView = itemView.findViewById(R.id.firstImage)
        val boost: CardView = itemView.findViewById(R.id.boost)


        val itemContainer: CardView = itemView.findViewById(R.id.item_container)

    }

    inner class CatalogueDiffCallBack(
        private val oldList: ArrayList<Catalogue>,
        private val newList: List<Catalogue>
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

            if (oldItem.name != newItem.name) {
                changes["name"] = newItem.name
            }
            if (oldItem.description != newItem.description) {
                changes["description"] = newItem.description
            }

            return changes.ifEmpty { null }
        }

    }


}