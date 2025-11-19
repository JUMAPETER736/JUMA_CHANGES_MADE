package com.uyscuti.social.business.adapter

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator
import com.uyscuti.social.business.model.Catalogue
import com.uyscuti.social.business.R
import com.uyscuti.social.network.api.request.business.users.GetBusinessProfileById


class SellerFullAdapter(private val context: Activity, private val catalogue: Catalogue) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {



    private var images = arrayListOf<String>()
    private var businessProfile: GetBusinessProfileById? = null

    private var pagerPosition = 0
    private var showIndicator = false

    private var userAvatar: String? = null
    private var username: String? = null

    private var priceText: String? = null
    private var descriptionText: String? = null

    private var spinner: Spinner? = null


    companion object {
        private const val SELLER_IMAGE_VIEW_TYPE = 1
        private const val SELLER_DETAILS_VIEW_TYPE = 2
        private const val SELLER_MESSAGE_VIEW_TYPE = 3
        private const val SELLER_INFORMATION_VIEW_TYPE = 4

    }

    init {
        images = catalogue.images.map {it} as ArrayList<String>
        priceText = catalogue.price
        descriptionText = catalogue.description

    }
    fun setNameAndAvatar(username: String, userAvatar: String) {
        this.username = username
        this.userAvatar = userAvatar
        notifyItemChanged(3)




    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            SELLER_IMAGE_VIEW_TYPE -> {
                val view =
                    LayoutInflater.from(context).inflate(R.layout.image_viewer, parent, false)
                SellerImageViewHolder(view)
            }

            SELLER_DETAILS_VIEW_TYPE -> {
                val view =
                    LayoutInflater.from(context).inflate(R.layout.seller_details, parent, false)
                SellerDetailsViewHolder(view)
            }

            SELLER_MESSAGE_VIEW_TYPE -> {
                val view =
                    LayoutInflater.from(context).inflate(R.layout.message_seller, parent, false)
                SellerMessageViewHolder(view)
            }

            SELLER_INFORMATION_VIEW_TYPE -> {
                val view =
                    LayoutInflater.from(context).inflate(R.layout.seller_information, parent, false)
                SellerInformationViewHolder(view)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    inner class SellerInformationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val followButton: Button = itemView.findViewById(R.id.btn_follow)
        val username: TextView = itemView.findViewById(R.id.name_user)
        val avatar: ImageView = itemView.findViewById(R.id.round_placeholder)

    }

    inner class SellerMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun showKeyboard(messageText : EditText) {
            showKeyboard(messageText)
        }

        val messageSpinner: Spinner = itemView.findViewById(R.id.spinner)
        val messageText: EditText = itemView.findViewById(R.id.message_input)
        val sendButton: ImageView = itemView.findViewById(R.id.send_button)

        val suggestions = arrayOf(
            "Select suggestions!",
            "I am interested in this item?",
            "Is this item still available?",
            "How much does it cost?",
            "Can I get a discount?",
            "Is this item in stock?",
            "Where can I get this item?",
            // Add more suggestions as needed
        )


    }

    inner class SellerDetailsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.view_name_product)
        val priceText: TextView = itemView.findViewById(R.id.view_price_product)
        val descriptionText: TextView = itemView.findViewById(R.id.view_description_product)

        val likeButton: ImageView = itemView.findViewById(R.id.tv_like)
        val unlikeButton: ImageView = itemView.findViewById(R.id.tv_unlike)
        val shareButton: ImageView = itemView.findViewById(R.id.tv_share)
        val heartLoveButton: ImageView = itemView.findViewById(R.id.tv_heart)

    }

    inner class SellerImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val viewPager: ViewPager2 = itemView.findViewById(R.id.viewPager)
        val wormDotsIndicator: WormDotsIndicator = itemView.findViewById(R.id.worm_dots_indicator)

    }

    inner class Seller

    override fun getItemCount(): Int {
        return 4
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {

            0 -> SELLER_IMAGE_VIEW_TYPE
            1 -> SELLER_DETAILS_VIEW_TYPE
            2 -> SELLER_MESSAGE_VIEW_TYPE
            3 -> SELLER_INFORMATION_VIEW_TYPE
            else -> throw IllegalArgumentException("Invalid position")
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is SellerImageViewHolder) {
            val viewPager = holder.viewPager
            val wormDotsIndicator = holder.wormDotsIndicator

            val adapter = MediaPagerAdapter(images, context)
            viewPager.adapter = adapter

            wormDotsIndicator.attachTo(viewPager)
        }

        else if (holder is SellerDetailsViewHolder) {
            val holder = holder as SellerDetailsViewHolder
            holder.nameText.text = catalogue.name
            holder.descriptionText.text = catalogue.description
            holder.priceText.text = "MK " + catalogue.price.toString()

            holder.likeButton.setOnClickListener {
                Toast.makeText(context, "Liked", Toast.LENGTH_SHORT).show()
                shakeView(holder.likeButton)
            }

            holder.unlikeButton.setOnClickListener {
                Toast.makeText(context, "Unliked", Toast.LENGTH_SHORT).show()
                shakeView(holder.unlikeButton)

            }

            holder.heartLoveButton.setOnClickListener {
                Toast.makeText(context, "Loved", Toast.LENGTH_SHORT).show()
                shakeView(holder.heartLoveButton)

            }


            holder.shareButton.setOnClickListener {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Your share text here")

                val chooserIntent = Intent.createChooser(shareIntent, "Share with")

                val resInfoList = context.packageManager.queryIntentActivities(
                    chooserIntent,
                    PackageManager.MATCH_DEFAULT_ONLY
                )

                for (resolveInfo in resInfoList) {
                    val packageName = resolveInfo.activityInfo.packageName
                    val label = resolveInfo.loadLabel(context.packageManager)

                    if (packageName.contains("com.facebook.katana") || // Facebook
                        packageName.contains("com.lenovo.anyshare") || // Shareit
                        packageName.contains("com.twitter.android") || // Twitter
                        packageName.contains("com.whatsapp") || // WhatsApp
                        packageName.contains("com.google.android.apps.plus") || // Google+
                        packageName.contains("com.instagram.android") || // Instagram
                        packageName.contains("com.pinterest.android") || // Pinterest
                        packageName.contains("com.snapchat.android") || // Snapchat
                        packageName.contains("com.tumblr")
                    ) { // Tumblr
                        val intent = Intent()
                        intent.setComponent(
                            ComponentName(
                                packageName,
                                resolveInfo.activityInfo.name
                            )
                        )
                        intent.setAction(Intent.ACTION_SEND)
                        intent.setType("text/plain")
                        intent.putExtra(Intent.EXTRA_TEXT, "Your share text here")
                        context.startActivity(intent)
                    }
                }
     context.startActivity(chooserIntent)
            }

        }
        else if (holder is SellerMessageViewHolder) {

            val holder = holder as SellerMessageViewHolder
            holder.sendButton.setOnClickListener {

                if (holder.messageText.text.toString().isNotEmpty()) {
                    Toast.makeText(context, "Message Sent", Toast.LENGTH_SHORT).show()
                    holder.messageText.text.clear()

                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(holder.messageText.windowToken, 0)





                } else {
                    Toast.makeText(context, "Please enter a message", Toast.LENGTH_SHORT).show()

                }

            }

            val suggestions = arrayOf(
                "Select suggestions!",
                "I am interested in this item?",
                "Is this item still available?",
                "How much does it cost?",
                "Can I get a discount?",
                "Is this item in stock?",
                "Where can I get this item?",
                "Katundu uyu akanali bwana?"
                // Add more suggestions as needed
            )

            val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, suggestions)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            holder.messageSpinner.adapter = adapter

            holder.messageText.setOnClickListener {
                holder.messageSpinner.performClick()
                holder.messageSpinner.visibility = View.VISIBLE


            }

                // Hide the spinner and set the text when an item is selected
                holder.messageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                        if (position > 0) {

                            holder.messageText.setText(suggestions[position])
                            holder.messageText.setSelection(holder.messageText.text.length)  // Set cursor to the end
                            holder.messageSpinner.visibility = View.GONE

                        }
                    }
                    override fun onNothingSelected(parent: AdapterView<*>) {
                        holder .messageSpinner.visibility = View.GONE
                    }
                }

            holder.messageText.hint = "Enter message here..."
            holder.messageText.requestFocus()
//                Toast.makeText(context, "Message Sent", Toast.LENGTH_SHORT).show()
//                holder.messageText.text.clear()
        }
//            holder.messageText.hint = "Type your message here..."
//            holder.messageText.requestFocus()
        if (holder is SellerInformationViewHolder) {
            val holder = holder as SellerInformationViewHolder

            holder.username.text = username

            Glide.with(context).load(userAvatar).into(holder.avatar)

            holder.followButton.setOnClickListener {
                holder.followButton.text = "Following"

            }
        }
    }

    fun shakeView(view: View) {
        val shake = ObjectAnimator.ofFloat(
            view,
            "translationX",
            0f,
            25f,
            -25f,
            25f,
            -25f,
            15f,
            -15f,
            6f,
            -6f,
            0f
        )
        shake.duration = 100
        shake.start()
    }


}

