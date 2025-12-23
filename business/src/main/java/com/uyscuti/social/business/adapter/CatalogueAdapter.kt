package com.uyscuti.social.business.adapter

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uyscuti.social.business.model.Catalogue
import com.uyscuti.social.business.retro.CreateCatalogueActivity
import com.uyscuti.social.business.retro.ImageViewActivity
import com.uyscuti.social.business.R


class CatalogueAdapter(val context: Activity, private val items: ArrayList<Catalogue>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var imageUri: Uri? = null
    private val catalogueImages: ArrayList<Uri> = arrayListOf()
    var adapterPosition = -1
    private var addCatalogueListener: AddCatalogueListener? = null

    var selectedItems = mutableListOf<Catalogue>()

    var onItemSelectedListener: ((Boolean) -> Unit)? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ADD_ITEM) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.add_catalogue_item, parent, false)
            AddItemViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.new_catelogue, parent, false)
            CatalogueItemViewHolder(view)
        }
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

    fun addCatalogue(catalogue: Catalogue) {
        items.add(catalogue)
//        notifyItemInserted(items.size - 1)
//        addCatalogueListener?.onAddCatalogue(items)
        notifyDataSetChanged()
    }

    fun setAddCatalogueListener(listener: AddCatalogueListener) {
        addCatalogueListener = listener

    }

    fun getCatalogueImages(): List<Uri> {
        return catalogueImages
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        adapterPosition = position

        if (getItemViewType(position) == VIEW_TYPE_ADD_ITEM) {
            // Handle logic for add item view holder
//            val deleteButton = (holder as com.example.payment.adapter.addItemViewHolder)
            val addItemHolder = holder as AddItemViewHolder
//             Implement logic specific to the com.example.payment.adapter.AddItemViewHolder if needed
//            deleteButton.itemView.setOnClickListener {
//                if (position != RecyclerView.NO_POSITION ) {
//                    items.removeAt(position)
//                    notifyItemRemoved(position)
//                }
//            }

            addItemHolder.itemView.setOnClickListener {
//                addCatalogue(Catalogue(items.size + 1, "New Catalogue", "", 10.0, listOf()))

                val intent = Intent(context, CreateCatalogueActivity::class.java)
                context.startActivityForResult(intent,111)

            }

        } else if (getItemViewType(position) == VIEW_TYPE_CATALOGUE_ITEM) {

            if (position == items.size){
                notifyDataSetChanged()
                return
            }
            // Handle logic for catalogue item view holder
            val catalogueItemHolder = holder as CatalogueItemViewHolder
            val item = items[position]

            catalogueItemHolder.productName.text = item.name
            catalogueItemHolder.product_description.text=item.description
            val itemPrice = item.price.toString() // Convert the price to string
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
//                catalogueItemHolder.productImage.setImageURI(Uri.parse(item.images[0]))
                catalogueItemHolder.addImage.visibility = View.GONE
                catalogueItemHolder.cardView.visibility = View.VISIBLE
            }

            catalogueItemHolder.itemContainer.setOnClickListener {

                if (selectedItems.isNotEmpty()) {
                    toggleSelection(item)

                    return@setOnClickListener
                }
                val intent = Intent(context, ImageViewActivity::class.java)
                intent.putExtra("catalogue", item)
                context.startActivity(intent)
            }

            catalogueItemHolder.itemContainer.setOnLongClickListener {
                toggleSelection(item)

                true
            }
            catalogueItemHolder.deleteButton.setOnClickListener {

                if (position != items.size || position < items.size){
                    if (position != RecyclerView.NO_POSITION) {
                        items.removeAt(position)
                        notifyItemRemoved(position)
                    }
                }else{
                    try {
                        notifyItemRemoved(position)
                    }catch (e:Exception){
                        notifyDataSetChanged()
                        e.printStackTrace()
                    }
                }
            }

            // Implement other logic or event handling for com.example.payment.adapter.CatalogueItemViewHolder
        }
    }

    fun deleteSelectedItems() {
        for (item in selectedItems) {
            items.remove(item)
        }
        selectedItems.clear()
        notifyDataSetChanged()
    }

    fun getSelectedProductsIds(): List<String>{
        return selectedItems.map { it.id }
    }


    override fun getItemCount(): Int {
        // Add 1 for the add item view holder
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            VIEW_TYPE_ADD_ITEM
        } else {
            VIEW_TYPE_CATALOGUE_ITEM
        }
    }

    fun setCatalogueImage(imageUri: Uri) {
        this.catalogueImages.add(imageUri)
        notifyItemChanged(adapterPosition)
    }

    fun addCatalogImage(imageUri: Uri) {
        setCatalogueImage(imageUri)
    }

    fun removeCatalogImage(position: Int) {
        catalogueImages.removeAt(position)
        notifyItemChanged(adapterPosition)
    }

    class addItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


    }

    private val VIEW_TYPE_ADD_ITEM = 0
    private val VIEW_TYPE_CATALOGUE_ITEM = 1


    class AddItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val addItem: LinearLayout = itemView.findViewById(R.id.add_item)


    }

    class deleteItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deleteButton: ImageView = itemView.findViewById(R.id.delete_button)

    }

    class CatalogueItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deleteButton: ImageView = itemView.findViewById(R.id.delete_button)
        val productName: TextView = itemView.findViewById(R.id.product_name)
        val product_description:TextView = itemView.findViewById(R.id.product_description)
        val price_text: TextView = itemView.findViewById(R.id.price_text)
        val productImage: ImageView = itemView.findViewById(R.id.firstImage)
        val cardView: CardView = itemView.findViewById(R.id.card_view)
        val addImage: ImageView = itemView.findViewById(R.id.add_item7)


        val itemContainer: LinearLayout = itemView.findViewById(R.id.item_container)

    }

    interface AddCatalogueListener {
        fun onAddCatalogue(catalogues: ArrayList<Catalogue>)
    }

}







    

