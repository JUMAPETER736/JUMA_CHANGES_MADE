package com.uyscuti.social.business.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
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
import com.uyscuti.social.business.BusinessCommentsClicked
import com.uyscuti.social.business.FullSellerActivity

import com.uyscuti.social.business.model.Catalogue
import com.uyscuti.social.business.R
import org.greenrobot.eventbus.EventBus


class SellerCatalogueAdapter(private val context: Activity, private val items: ArrayList<Catalogue>, val username: String, val  userAvatar: String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val CATALOGUE_DISPLAY_TYPE_VIEW  = 0


    private val catalogueImages: ArrayList<Uri> = arrayListOf()

    private var adapterPosition = -1



    override fun getItemViewType(position: Int): Int {
        return CATALOGUE_DISPLAY_TYPE_VIEW
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatalogueItemViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.catalog_view_activity, parent, false)
        return CatalogueItemViewHolder(view)
    }

    class CatalogueItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.add_item7)
        val businessComments: ImageView = itemView.findViewById(R.id.businessComments)
        val productName: TextView = itemView.findViewById(R.id.product_name)
        val productDescription: TextView = itemView.findViewById(R.id.product_description)
        val priceText: TextView = itemView.findViewById(R.id.price_text)
        val productImage: ImageView = itemView.findViewById(R.id.firstImage)
        val cardView: CardView = itemView.findViewById(R.id.card_view)
        val addImage : ImageView = itemView.findViewById(R.id.add_item7)


        val itemContainer: LinearLayout = itemView.findViewById(R.id.item_container)


    }

    override fun getItemCount(): Int {
        return items.size

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        adapterPosition = position
        val item = items[position]



        if (holder is CatalogueItemViewHolder){

            Glide.with(context).load(item.images[0])
                .centerCrop()
                .into(holder.productImage)

            holder.productName.text = items[position].name
            holder.productDescription.text = items[position].description
            holder.priceText.text = items[position].price

            holder.addImage.visibility = View.GONE
            holder.cardView.visibility = View.VISIBLE


            holder.itemContainer.setOnClickListener{

                val intent = Intent(context, FullSellerActivity::class.java)
                intent.putExtra("catalogue", item)
                intent.putExtra("username", username)
                intent.putExtra("userAvatar", userAvatar)
                context.startActivity(intent)



            }


            holder.businessComments.setOnClickListener {
// Post an event from Module A
//                EventBus.getDefault().post(new MyEvent("Data from Module A"));
                EventBus.getDefault().post(
                    BusinessCommentsClicked(position, item)
                )
            }

        }
    }

}


