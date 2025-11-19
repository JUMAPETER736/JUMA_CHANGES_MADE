package com.uyscuti.social.business.adapter

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator
import com.uyscuti.social.business.model.Catalogue
import com.uyscuti.social.business.R
import com.uyscuti.social.network.api.request.business.catalogue.GetCatalogueByUserId
import com.uyscuti.social.network.api.request.business.users.GetBusinessProfileById


class ProfileViewAdapter(private var context: Activity) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val catalogueItems: ArrayList<Catalogue> = arrayListOf()
    private lateinit var sellerCatalogueAdapter: SellerCatalogueAdapter
    private var businessProfile: GetBusinessProfileById? = null
    private var mediaPagerAdapter: MediaPagerAdapter? = null

    var onInvokeCatalogue: ((Catalogue) -> Unit)? = null

    private var username: String? = null
    private var userAvatar: String? = null

    private var pagerPosition = 0

    private var showIndicator = false

    private var images = arrayListOf<String>()

    private var businessName: String? = null
    private var businessLocation: String? = null
    private var businessContact: String? = null
    private var businessDescription: String? = null
    private var businessEmail: String? = null
    private var businessType: String? = null






    private val image0 = "https://www.nla.gov.au/sites/default/files/pic-1.jpg"


    companion object {
        private const val PROFILE_VIEW_TYPE = 1
        private const val PROFILE_DETAILS_VIEW_TYPE = 2
        private const val PROFILE_CATALOGUE_VIEW_TYPE = 3
    }

    fun setNameAndAvatar(username: String, userAvatar: String) {
        this.username = username
        this.userAvatar = userAvatar
        Log.d("ProfileViewAdapter", "Username: $username, Avatar: $userAvatar")

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            PROFILE_VIEW_TYPE -> {
                val view =
                    LayoutInflater.from(context).inflate(R.layout.image_viewer, parent, false)
                ProfileViewHolder(view)
            }

            PROFILE_DETAILS_VIEW_TYPE -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.profile_details_activity, parent, false)
                ProfileDetailsViewHolder(view)
            }

            PROFILE_CATALOGUE_VIEW_TYPE -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.catalogue_details, parent, false)
                ProfileCatalogueViewHolder(view)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    inner class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val viewPager: ViewPager2 = itemView.findViewById(R.id.viewPager)
        val wormDotsIndicator: WormDotsIndicator = itemView.findViewById(R.id.worm_dots_indicator)
    }

    inner class ProfileDetailsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val businessName: TextView = itemView.findViewById(R.id.tv_name)
        val businessLocation: TextView = itemView.findViewById(R.id.tv_location)
        val businessContact: TextView = itemView.findViewById(R.id.tv_contacts)
        val businessDescription: TextView = itemView.findViewById(R.id.tv_describe)
        val businessEmail: TextView = itemView.findViewById(R.id.tv_email)
        val businessType: TextView = itemView.findViewById(R.id.tv_type)
    }

    inner class ProfileCatalogueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val catalogueRecyclerView: RecyclerView = itemView.findViewById(R.id.recyclerViewer)
    }

    fun setBusinessProfile(businessProfile: GetBusinessProfileById) {
        this.businessProfile = businessProfile
        Log.d("ProfileViewAdapter", businessProfile.toString())
        businessName = businessProfile.businessName
        businessLocation = businessProfile.contact.address
        businessContact = businessProfile.contact.phoneNumber
        businessDescription = businessProfile.businessDescription
        businessEmail = businessProfile.contact.email
        businessType = businessProfile.businessType
        notifyDataSetChanged()

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ProfileViewHolder -> {
                val profileViewHolder = holder as ProfileViewHolder
                val viewPager: ViewPager2 = holder.viewPager
                val wormDotsIndicator: WormDotsIndicator = holder.wormDotsIndicator

//                holder.wormDotsIndicator.visibility = if (showIndicator) View.VISIBLE else View.GONE

                val mediaUrls = arrayListOf<String>()
                var videoThumbnail: String? = null



                if (businessProfile != null) {
                    mediaUrls.add(businessProfile!!.backgroundVideo.url)
                    videoThumbnail = businessProfile!!.backgroundVideo.thumbnail
                }

                if (businessProfile != null) {
                    mediaUrls.add(businessProfile!!.backgroundPhoto.url)
                }

                //            images.add(image0)
                viewPager.adapter = MediaPagerAdapter(mediaUrls, context, videoThumbnail)
                wormDotsIndicator.attachTo(viewPager)


            }

            is ProfileCatalogueViewHolder -> {
                val catalogueViewHolder = holder as ProfileCatalogueViewHolder
                val catalogueRecyclerView = catalogueViewHolder.catalogueRecyclerView

                catalogueRecyclerView.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

                sellerCatalogueAdapter = SellerCatalogueAdapter(context, catalogueItems, username?:"", userAvatar?:"")
                catalogueRecyclerView.adapter = sellerCatalogueAdapter
            }

            is ProfileDetailsViewHolder -> {
                val detailsViewHolder = holder as ProfileDetailsViewHolder

                detailsViewHolder.businessName.text = businessName
                detailsViewHolder.businessLocation.text = businessLocation
                detailsViewHolder.businessContact.text = businessContact
                detailsViewHolder.businessDescription.text = businessDescription
                detailsViewHolder.businessEmail.text = businessEmail
                detailsViewHolder.businessType.text = businessType


            }
        }
    }

    override fun getItemCount(): Int {
        return 3
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> PROFILE_VIEW_TYPE
            1 -> PROFILE_DETAILS_VIEW_TYPE
            2 -> PROFILE_CATALOGUE_VIEW_TYPE
            else -> throw IllegalArgumentException("Invalid position")
        }
    }

    fun setCatalogueList(catalogueList: List<Catalogue>) {
        this.catalogueItems.clear()
        this.catalogueItems.addAll(catalogueList)
        notifyItemChanged(2)
    }

    fun setBusinessCatalogue(userBusinessCatalogue: GetCatalogueByUserId) {
        this.catalogueItems.clear()
        this.catalogueItems.addAll(catalogueItems)
    }
}