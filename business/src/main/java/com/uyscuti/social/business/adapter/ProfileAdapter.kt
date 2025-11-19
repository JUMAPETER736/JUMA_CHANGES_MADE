package com.uyscuti.social.business.adapter

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator
import com.uyscuti.social.business.model.Catalogue
import com.uyscuti.social.network.api.request.business.create.BackgroundPhoto
import com.uyscuti.social.network.api.request.business.create.BusinessCatalogue
import com.uyscuti.social.network.api.request.business.create.BusinessLocation
import com.uyscuti.social.network.api.request.business.create.CreateBusinessProfile
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.uyscuti.social.network.api.request.business.create.LocationInformation
import com.uyscuti.social.business.room.entity.BusinessEntity
import com.uyscuti.social.business.util.ImagePicker
import com.uyscuti.social.business.R
import com.uyscuti.social.network.api.request.business.create.Contact
import com.uyscuti.social.network.api.request.business.create.Location
import com.uyscuti.social.network.api.request.business.create.WalkingBillboard

class ProfileAdapter(
    val context: Activity,
    private var businessEntity: BusinessEntity?,
    private val onCreateProfile: (CreateBusinessProfile) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // Add necessary view holder classes for each section (BusinessViewHolder, TypeViewHolder, etc.)
    private val sections: ArrayList<String> = arrayListOf()

    var onItemSelectedListener: ((Boolean) -> Unit)? = null

    private lateinit var fullScreenPagerAdapter: MediaPagerAdapter
    private lateinit var geoTagAdapter: GeoTagAdapter


    private var latitude: Double? = null
    private var longitude: Double? = null
    private var accuracy: Float? = null
    private var locationRange: Int? = null
    private var locationEnabled: Boolean = false


    private var billboardLatitude: Double? = null
    private var billboardLongitude: Double? = null
    private var billboardAccuracy: Float? = null
    private var billboardLocationEnabled: Boolean = false
    private var billboardLocationRange: Int? = null

    private var checkBox: CheckBox? = null
    private var checkBox2: CheckBox? = null

    private var termsError: Boolean = false
    private var policyError: Boolean = false
    private var isLocationChecked: Boolean = false

    private var isBillBoardLiveLocationChecked: Boolean = false

    private var profile: BusinessEntity? = null
    private val BUSINESS_SECTION = 0
    private val TYPE_SECTION = 1
    private val LOCATION_SECTION = 2
    private val DESCRIPTION_SECTION = 3
    private val BACKGROUNDIMAGE_SAECTION = 4
    private val SAVEPROFILE_SECTION = 5
    private val REQUEST_LOCATION_PERMISSION = 1

    //    private val CATALOGUE_SECTION = 6
    private val CONTACT_SECTION = 7
    private val NEWCATALOGUE = 8

    private var imageUri: Uri? = null
    private var videoUri: Uri? = null
    private val describeProduct: EditText? = null
    private val price: EditText? = null
    private val catalogueImages: ArrayList<Uri> = arrayListOf()
    private val catalogueItems: ArrayList<Catalogue> = arrayListOf()

    private var catalogueAdapter: CatalogueAdapter? = null

    private var businessName: String? = null
    private var businessType: String? = null
    private var businessLocation: BusinessLocation? = null
    private var contact: Contact? = null
    private var location: Location? = null
    private var description: String? = null
    private var email: String? = null
    private var phoneNumber: String? = null
    private var address: String? = null

    private val contactBooleanArray: BooleanArray = BooleanArray(size = 3) { false }

    private var nameError: Boolean = false
    private var typeError: Boolean = false
    private var locationError: Boolean = false
    private var descriptionError: Boolean = false
    private var emailError: Boolean = false
    private var phoneNumberError: Boolean = false
    private var backgroundImageError: Boolean = false

    // Edit Texts
    private var businessNameEditText: EditText? = null
    private var businessTypeEditText: EditText? = null
    private var descriptionEditText: EditText? = null
    private var businessEmailEditText: EditText? = null
    private var phoneNumberEditText: EditText? = null
    private var addressEditText: EditText? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient


    init {

        profile = businessEntity
        sections.add("Background Image")
        sections.add("Business Name")
        sections.add("Business Type")
        sections.add("Description")
        sections.add("business contact")
        sections.add("Location")
//        sections.add("upload catalogue")
        sections.add("New Catalogue")
        sections.add("Save Profile")

        catalogueItems.add(Catalogue(0.toString(), "add", "", "0.0", listOf()))

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)



        checkBox?.setOnCheckedChangeListener { _, isChecked ->
            handleCheckboxChange(isChecked)
        }

        checkBox2?.setOnCheckedChangeListener { _, isChecked ->
            handleCheckboxChange(isChecked)
        }
    }

    private fun getVideoDuration(videoUri: Uri): Long {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, videoUri)
        val durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        return durationString?.toLongOrNull() ?: 0
    }

    private fun handleCheckboxChange(checked: Boolean) {
        if (checked) {
            checkBox?.isChecked = true
            checkBox2?.isChecked = true
        } else {
            checkBox?.isChecked = false
        }
    }


    fun setProfile(businessEntity: BusinessEntity) {
        profile = businessEntity
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            BUSINESS_SECTION -> BusinessViewHolder(
                inflater.inflate(
                    R.layout.business_name,
                    parent,
                    false
                )
            )

            TYPE_SECTION -> TypeViewHolder(
                inflater.inflate(
                    R.layout.business_type,
                    parent,
                    false
                )
            )

            LOCATION_SECTION -> LocationViewHolder(
                inflater.inflate(
                    R.layout.location_business,
                    parent,
                    false
                )
            )

            DESCRIPTION_SECTION -> DescriptionViewHolder(
                inflater.inflate(
                    R.layout.describe_business,
                    parent,
                    false
                )
            )

            BACKGROUNDIMAGE_SAECTION -> BackgroundImageViewHolder(
                inflater.inflate(
                    R.layout.backgroud_profilephoto,
                    parent,
                    false
                )
            )

            SAVEPROFILE_SECTION -> SaveProfileViewHolder(
                inflater.inflate(
                    R.layout.save_button,
                    parent,
                    false
                )
            )

            CONTACT_SECTION -> BusinessContactDetailsViewHolder(
                inflater.inflate(
                    R.layout.business_contact_details,
                    parent,
                    false
                )
            )

            NEWCATALOGUE -> CatalogueViewHolder(
                inflater.inflate(
                    R.layout.new_cat,
                    parent,
                    false
                )
            )

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    fun getRealPathFromUri(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                return it.getString(columnIndex)
            }
        }
        return null
    }

    override fun getItemCount(): Int {
        return sections.size
    }

    private fun pickImage() {
        ImagePicker.pickMedia(context)
    }

    fun setBackgroundImage(imageUri: Uri) {
        this.imageUri = imageUri
        Log.d("ProfileAdapter", "setBackgroundImage: $imageUri")
        notifyItemChanged(0)
    }

    fun setBackgroundVideo(imageUri: Uri) {
        this.videoUri = imageUri
        notifyItemChanged(0)
    }

    fun setCatalogueImage(imageUri: Uri) {
        this.catalogueImages.add(imageUri)
        notifyItemChanged(6)
    }

    fun addCatalogueItem(catalogueItem: Catalogue) {
        try {
            if (catalogueItems.contains(catalogueItem)) {
                return
            }
            catalogueItems.add(catalogueItem)
            notifyItemChanged(6)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteSelectedItems() {
        catalogueAdapter!!.deleteSelectedItems()
    }

    fun getSelectedProductsIds(): List<String> {
        return catalogueAdapter!!.getSelectedProductsIds()

    }

    fun releasePlayer() {
        fullScreenPagerAdapter.releasePlayer()
    }

    fun pausePlayer() {
        fullScreenPagerAdapter.pausePlayer()
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        if (holder is BackgroundImageViewHolder) {
            releasePlayer()
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is BusinessViewHolder -> {

                // Bind data for business section
                val businessViewHolder = holder as BusinessViewHolder

                businessNameEditText = businessViewHolder.businessNameEditText

                if (nameError) {
                    businessViewHolder.businessNameEditText.error = "Required"
                }

                if (profile != null) {
                    businessViewHolder.businessNameEditText.setText(profile!!.businessName)
                }

                businessViewHolder.businessNameEditText.setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) {
                        val businessNameTextView = businessViewHolder.businessNameEditText
                        val businessName = businessNameTextView.text.toString()
                        if (businessName.isEmpty()) {

                            businessNameTextView.error = "Required"

                        } else {

                            return@setOnFocusChangeListener
                        }
                    }
                }
            }

            is BusinessContactDetailsViewHolder -> {
                val businessContactDetailsViewHolder = holder as BusinessContactDetailsViewHolder
                val emailEditText =
                    businessContactDetailsViewHolder.itemView.findViewById<EditText>(R.id.email)

                phoneNumberEditText = businessContactDetailsViewHolder.businessPhoneNumberTextView

                businessEmailEditText = emailEditText

                addressEditText = businessContactDetailsViewHolder.addressTextView

                if (profile != null) {
                    emailEditText.setText(profile!!.contact.email)
                    businessContactDetailsViewHolder.businessPhoneNumberTextView.setText(profile!!.contact.phoneNumber)
                    businessContactDetailsViewHolder.addressTextView.setText(profile!!.contact.address)
                }

                contactBooleanArray.forEachIndexed { index, b ->
                    if (index == 0 && b) {
                        emailEditText.error = "Required"
                    } else if (index == 1 && b) {
                        businessContactDetailsViewHolder.businessPhoneNumberTextView.error =
                            "Required"
                    } else if (index == 2 && b) {
                        businessContactDetailsViewHolder.addressTextView.error =
                            "Required"
                    }
                }

                emailEditText.setOnClickListener {
                    // Handle click event for the email EditText
                }
                businessContactDetailsViewHolder.emailTextView.setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) {
                        // When focus is lost, validate the email
                        val emailTextView = businessContactDetailsViewHolder.emailTextView
                        val email = emailTextView.text.toString()

                        if (email.isEmpty()) {
                            emailTextView.error = "Required"

                        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            emailEditText.error = "Invalid email format"
                        } else {
                            return@setOnFocusChangeListener
                        }
                    }
                }
                businessContactDetailsViewHolder.businessPhoneNumberTextView.setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) {
                        val businessPhoneNumberTextView =
                            businessContactDetailsViewHolder.businessPhoneNumberTextView
                        val businessPhoneNumber = businessPhoneNumberTextView.text.toString()

                        if (businessPhoneNumber.isEmpty()) {
                            businessPhoneNumberTextView.error = "Required"
                        } else {
                            return@setOnFocusChangeListener
                        }
                    }
                }
                businessContactDetailsViewHolder.addressTextView.setOnFocusChangeListener() { _, hasFocus ->
                    if (!hasFocus) {
                        val addressTextView = businessContactDetailsViewHolder.addressTextView
                        val address = addressTextView.text.toString()
                        if (address.isEmpty()) {
                            addressTextView.error = "Required"
                        } else {
                            return@setOnFocusChangeListener
                        }
                    }
                }
            }

            is TypeViewHolder -> {
                // Bind data for type section
                val typeViewHolder = holder as TypeViewHolder

                if (typeError) {
                    typeViewHolder.businessTypeTextView.error = "Required"
                }

                businessTypeEditText = typeViewHolder.businessTypeTextView

                if (profile != null) {
                    typeViewHolder.businessTypeTextView.setText(profile!!.businessType)
                }

                typeViewHolder.businessTypeTextView.setOnFocusChangeListener() { _, hasFocus ->
                    if (!hasFocus) {
                        val businessTypeTextView = typeViewHolder.businessTypeTextView
                        val businessType = businessTypeTextView.text.toString()
                        if (businessType.isEmpty()) {
                            businessTypeTextView.error = "Required"
                        } else {
                            return@setOnFocusChangeListener
                        }
                    }

                }
            }


            is LocationViewHolder -> {
                // Bind data for location section
                val locationViewHolder = holder as LocationViewHolder

                locationViewHolder.businessLocationSwitcher.setOnCheckedChangeListener { _, isChecked ->
                    //bind data for business location
                    if (isChecked) {
                        val dialog = Dialog(locationViewHolder.itemView.context)
                        dialog.setContentView(R.layout.business_tag_popup)
                        val cancelButton: AppCompatButton = dialog.findViewById(R.id.cancel_Btn)
                        val confirmButton: AppCompatButton = dialog.findViewById(R.id.proceedBtn)

//                        locationViewHolder.businessLocationSwitcher.isChecked = businessEntity!!.location.businessLocation.enabled

                        cancelButton.setOnClickListener {
                            locationViewHolder.businessLocationSwitcher.isChecked = false
                            dialog.dismiss()
                        }

                        confirmButton.setOnClickListener {
                            locationViewHolder.businessLocationSwitcher.isChecked = true
                            val rangeRadioGroup =
                                dialog.findViewById<RadioGroup>(R.id.rangeRadioGroup)

                            val selectedRadioButtonId = rangeRadioGroup.checkedRadioButtonId
                            val range = dialog.findViewById<RadioButton>(R.id.range_500_Metres)
                            val range2 = dialog.findViewById<RadioButton>(R.id.range_1kilometer)
                            val range3 = dialog.findViewById<RadioButton>(R.id.range_2kilometre)
                            val range4 = dialog.findViewById<RadioButton>(R.id.range_3kilometre)

                            // Check if a radio button is selected
                            if (selectedRadioButtonId != -1) {
                                // At least one radio button is selected
                                val selectedRadioButton =
                                    dialog.findViewById<RadioButton>(selectedRadioButtonId)

                                if (selectedRadioButton == range) {
                                    locationRange = 500
                                    billboardLocationRange = 500

                                } else if (selectedRadioButton == range2) {
                                    locationRange = 1000
                                    billboardLocationRange = 1000
                                } else if (selectedRadioButton == range3) {
                                    locationRange = 2000
                                    billboardLocationRange = 2000
                                } else if (selectedRadioButton == range4) {
                                    locationRange = 3000
                                    billboardLocationRange = 3000
                                }

                                locationEnabled = true
                                billboardLocationEnabled = true

                                if (!getLocation()) {
                                    Toast.makeText(
                                        context,
                                        "Please enable location",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@setOnClickListener
                                }

                                dialog.dismiss()
                                // Do something with the selected radio button
                            } else {
                                // No radio button is selected
                                Toast.makeText(
                                    dialog.context,
                                    "Please select a range",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        dialog.setCancelable(false)

                        dialog.show()
                    }
                }

                locationViewHolder.currentLocationSwitcher.setOnCheckedChangeListener { _, isChecked ->
                    //bind data for location section
                    if (isChecked) {
                        val dialog = Dialog(locationViewHolder.itemView.context)
                        dialog.setContentView(R.layout.bill_board_popup)
                        val cancelButton: AppCompatButton = dialog.findViewById(R.id.cancelBtn)
                        val confirmButton: AppCompatButton = dialog.findViewById(R.id.confirmBtn)

                        cancelButton.setOnClickListener {
                            isBillBoardLiveLocationChecked = false
                            locationViewHolder.currentLocationSwitcher.isChecked = false
                            dialog.dismiss()

                        }

                        confirmButton.setOnClickListener {
                            locationViewHolder.currentLocationSwitcher.isChecked = true
                            isBillBoardLiveLocationChecked = true

                            val acknowledgementDialog = Dialog(locationViewHolder.itemView.context)
                            acknowledgementDialog.setContentView(R.layout.acknowledge_popup)

                            val disagreeButton: AppCompatButton = acknowledgementDialog.findViewById(R.id.disagree_button)

                            val acknowledgeButton: AppCompatButton = acknowledgementDialog.findViewById(R.id.agree_button)

                            checkBox = acknowledgementDialog.findViewById(R.id.agree_checkbox)
                            checkBox2 = acknowledgementDialog.findViewById(R.id.flash_checkbox)

                            disagreeButton.setOnClickListener {

                                checkBox?.isChecked = false
                                checkBox2?.isChecked = false
                                locationViewHolder.currentLocationSwitcher.isChecked = false
                                acknowledgementDialog.dismiss()
//                                dialog.setContentView(R.layout.bill_board_popup)

                            }
                            acknowledgeButton.setOnClickListener {


                                if (!checkBox!!.isChecked || !checkBox2!!.isChecked) {

                                    if (!checkBox2!!.isChecked) {
                                        checkBox2!!.error = "Required"


                                    } else {
                                        checkBox2!!.error = null
                                    }

                                    if (!checkBox!!.isChecked) {
                                        checkBox!!.error = "Required"
                                    } else {
                                        checkBox!!.error = null
                                    }
                                    return@setOnClickListener
                                }


                                if (!getBillBoardLocation()) {

                                    Toast.makeText(
                                        context,
                                        "Please enable location",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@setOnClickListener

                                }
                                acknowledgementDialog.dismiss()

                            }

                            acknowledgementDialog.setCancelable(false)
                            acknowledgementDialog.show()
                            dialog.dismiss()
                        }
                        dialog.setCancelable(false)
                        dialog.show()
                    }
                }
            }

            is DescriptionViewHolder -> {
                // Bind data for description section
                val descriptionViewHolder = holder as DescriptionViewHolder

                if (descriptionError) {
                    descriptionViewHolder.descriptionTextView.error = "Required"
                }

                descriptionEditText = descriptionViewHolder.descriptionTextView

                if (profile != null) {
                    descriptionViewHolder.descriptionTextView.setText(profile!!.businessDescription)
                }

                descriptionViewHolder.descriptionTextView.setOnFocusChangeListener() { _, hasFocus ->
                    if (!hasFocus) {
                        val descriptionTextView = descriptionViewHolder.descriptionTextView
                        val description = descriptionTextView.text.toString()
                        if (description.isEmpty()) {
                            descriptionTextView.error = "Required"
                        }
                    }
                }
            }

            is SaveProfileViewHolder -> {
                // Bind data for save profile section
                val saveProfileViewHolder = holder as SaveProfileViewHolder
                saveProfileViewHolder.saveProfileButton.setOnClickListener {

                    // Save profile
                    Toast.makeText(context, "Profile saved", Toast.LENGTH_SHORT).show()
                    validateData()
                    createProfile()


//                    val dialog = Dialog(saveProfileViewHolder.itemView.context)
//                    if (imageUri == null) {
//                        Toast.makeText(
//                            context,
//                            "Please upload a profile photo",
//                            Toast.LENGTH_SHORT
//                        )
//                            .show()
//                        return@setOnClickListener
//                    }

//                    dialog.setContentView(R.layout.popup_confirmation)
//                    dialog.show()
//                    dialog.setCancelable(false)
//                    val cancelButton: AppCompatButton = dialog.findViewById(R.id.cancelButton)
//                    val confirmButton: AppCompatButton = dialog.findViewById(R.id.nextButton)
//                    cancelButton.setOnClickListener {
//                        dialog.dismiss()
//                    }
//
//                    confirmButton.setOnClickListener {
//                        dialog.dismiss()
//
//                    }
                }
            }

            is BackgroundImageViewHolder -> {
                val backgroundImageViewHolder = holder as BackgroundImageViewHolder
                backgroundImageViewHolder.setIsRecyclable(false)

                backgroundImageViewHolder.uploadButton.setOnClickListener {
                    pickImage()
                }

                val mediaUrls = arrayListOf<String>()
                var videoThumbnail: String? = null

                if (profile != null && profile!!.backgroundVideo != null && videoUri == null) {
                    mediaUrls.add(0, profile!!.backgroundVideo!!)
                    videoThumbnail = profile!!.videoThumbnail
                }

                if (videoUri != null) {

                    val getVideoDuration = getVideoDuration(videoUri!!)
                    val maxVideoDuration = 30 * 1000
                    if (getVideoDuration > maxVideoDuration) {
                        videoThumbnail = videoUri.toString()
                        Toast.makeText(
                            context,
                            "Video duration must be less than 30 seconds",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {

                        mediaUrls.add(0, videoUri.toString())
                        videoThumbnail = videoUri.toString()
                    }

                }



                if (profile != null && imageUri == null) {
                    mediaUrls.add(profile!!.backgroundPhoto)
                }

                if (imageUri != null) {

                    if (mediaUrls.size > 0) {
                        mediaUrls.add(mediaUrls.size - 1, imageUri.toString())
                    } else {
                        mediaUrls.add(imageUri.toString())
                    }
                }

                fullScreenPagerAdapter = MediaPagerAdapter(mediaUrls, context, videoThumbnail)

                backgroundImageViewHolder.viewPager.adapter = fullScreenPagerAdapter
                backgroundImageViewHolder.viewPager.offscreenPageLimit = 10

                backgroundImageViewHolder.dotsIndicator.attachTo(backgroundImageViewHolder.viewPager)

                backgroundImageViewHolder.viewPager.registerOnPageChangeCallback(object :
                    ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {

                        if (position == 0) {
                            fullScreenPagerAdapter.resumePlayer()
                        } else {
                            fullScreenPagerAdapter.pausePlayer()
                        }
                    }
                })


//                if (backgroundImageError){
//                    backgroundImageViewHolder.uploadButton.error = "Please upload a profile photo"
//                }

//                if (profile != null && imageUri == null) {
////                    val imageUri = Uri.parse(profile!!.backgroundPhoto)
////                    backgroundImageViewHolder.backgroundImageView.setImageURI(imageUri)
//
//                    Glide.with(context)
//                        .load(profile!!.backgroundPhoto)
//                        .centerCrop()
//                        .into(backgroundImageViewHolder.backgroundImageView)
//
//
////                    backgroundImageViewHolder.backgroundImageView.setOnClickListener {
////                        val intent = Intent(context, FullScreenImageActivity::class.java).apply {
//////                            putExtra("imageUrl", profile!!.backgroundPhoto)
////
////                            putStringArrayListExtra("imageUrls",arrayListOf(profile!!.backgroundPhoto))
////
////                        }
////                        context.startActivity(intent)
////                    }
//                } else if (imageUri != null){
//                    Glide.with(context)
//                        .load(imageUri)
//                        .centerCrop()
//                        .into(backgroundImageViewHolder.backgroundImageView)
//
//                    backgroundImageViewHolder.backgroundImageView.setOnClickListener {
//                        val intent = Intent(context, FullScreenImageActivity::class.java).apply {
////                            putExtra("imageUrl", imageUri)
//                            putStringArrayListExtra("imageUrls",arrayListOf(imageUri.toString()))
//
//                        }
//                        context.startActivity(intent)
//                    }
//                }


//                if (imageUri != null) {
//                    val imageView: ImageView =
//                        backgroundImageViewHolder.itemView.findViewById(R.id.photo_preview)
//                    imageView.setImageURI(imageUri)
//                }
            }

            is CatalogueViewHolder -> {
                val catalogueViewHolder = holder as CatalogueViewHolder
                val catalogueRecyclerView = catalogueViewHolder.catalogueRecyclerView
                catalogueRecyclerView.setHasFixedSize(true)
                catalogueRecyclerView.isNestedScrollingEnabled = false
                catalogueRecyclerView.isFocusable = false
                catalogueRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

                catalogueAdapter = CatalogueAdapter(context, catalogueItems)

                catalogueAdapter?.onItemSelectedListener = {
                    onItemSelectedListener?.invoke(it)
                }
                catalogueRecyclerView.adapter = catalogueAdapter
                catalogueViewHolder.itemView.findViewById<ImageView>(R.id.add_item7)
            }

        }

    }

    fun getLocation(): Boolean {

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION

            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
            return false
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: android.location.Location? ->
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                    accuracy = location.accuracy


                    Log.d("GeoTagActivity", "latitude: $latitude")
                    Log.d("GeoTagActivity", "longitude: $longitude")
                    Log.d("GeoTagActivity", "accuracy: $accuracy")

                }
            }
        return true
    }

    fun getBillBoardLocation(): Boolean {

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION

            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
            return false
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: android.location.Location? ->
                if (location != null) {
                    billboardLatitude = location.latitude
                    billboardLongitude = location.longitude
                    billboardAccuracy = location.accuracy

                    Log.d("GeoTagActivity", "billboardLatitude: $billboardLatitude")
                    Log.d("GeoTagActivity", "billboardLongitude: $billboardLongitude")
                    Log.d("GeoTagActivity", "billboardAccuracy: $billboardAccuracy")

                }
            }
        return true
    }

    private fun createProfile() {
        if (contactBooleanArray.any() || nameError || typeError || locationError || descriptionError || emailError || phoneNumberError) {
            notifyDataSetChanged()
        }
        try {
//            val imagePath = getRealPathFromUri(imageUri!!)
            val backgroundPhoto =
                BackgroundPhoto("https://www.nla.gov.au/sites/default/files/pic-1.jpg")

            val locationInformation = LocationInformation(
                latitude = latitude.toString(),
                longitude = longitude.toString(),
                accuracy = accuracy.toString(),
                range = locationRange.toString()
            )

            val liveLocationInformation = LocationInformation (
                latitude = billboardLatitude.toString(),
                longitude = billboardLongitude.toString(),
                accuracy = billboardAccuracy.toString(),
                range = billboardLocationRange.toString()?: 500.toString()
            )

            businessLocation = BusinessLocation(locationEnabled, locationInformation)
            val walkingBillboard = WalkingBillboard(isBillBoardLiveLocationChecked, liveLocationInformation)
            val location = Location(businessLocation!!, walkingBillboard)

            val contact = Contact(address!!, email!!, phoneNumber!!, "www.business.com")
            val catalogue = listOf<BusinessCatalogue>()

            val createBusinessProfile = CreateBusinessProfile(
                backgroundPhoto,
                catalogue,
                description!!,
                businessName!!,
                businessType!!,
                contact,
                location
            )

            onCreateProfile.invoke(createBusinessProfile)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun validateData() {
        businessName = businessNameEditText?.text.toString()
        businessType = businessTypeEditText?.text.toString()
        description = descriptionEditText?.text.toString()

        email = businessEmailEditText?.text.toString()
        phoneNumber = phoneNumberEditText?.text.toString()
        address = addressEditText?.text.toString()

        contactBooleanArray[0] = email.isNullOrEmpty()
        contactBooleanArray[1] = phoneNumber.isNullOrEmpty()
        contactBooleanArray[2] = address.isNullOrEmpty()

        nameError = businessName.isNullOrEmpty()
        typeError = businessType.isNullOrEmpty()
        locationError = location == null
        descriptionError = description.isNullOrEmpty()
        emailError = email.isNullOrEmpty()

        backgroundImageError = imageUri == null
    }


    override fun getItemViewType(position: Int): Int {
        return when (sections[position]) {
            "Business Name" -> BUSINESS_SECTION
            "Business Type" -> TYPE_SECTION
            "Location" -> LOCATION_SECTION
            "Description" -> DESCRIPTION_SECTION
            "Background Image" -> BACKGROUNDIMAGE_SAECTION
//            "upload catalogue" -> CATALOGUE_SECTION
            "Save Profile" -> SAVEPROFILE_SECTION
            "business contact" -> CONTACT_SECTION
            "New Catalogue" -> NEWCATALOGUE
            else -> throw IllegalArgumentException("Invalid section type")
        }
    }

    // LocationViewHolder.java
    class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val locationTextView: TextView = itemView.findViewById<TextView>(R.id.section_label)
        val businessLocationSwitcher: SwitchCompat =
            itemView.findViewById(R.id.business_location_toggle)

        // Initialize views for BusinessLocation if needed
        val currentLocationSwitcher: SwitchCompat =
            itemView.findViewById(R.id.current_location_toggle)

    }

    // DescriptionViewHolder.java
    class DescriptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val descriptionTextView: EditText = itemView.findViewById(R.id.description_edit)
    }

    class BackgroundImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val uploadButton: AppCompatButton = itemView.findViewById(R.id.upload_button)
//        val backgroundImageView: ImageView = itemView.findViewById(R.id.photo_preview)

        // Initialize views for ImagesHolder if needed
        val viewPager: ViewPager2 = itemView.findViewById(R.id.viewPager)
        val dotsIndicator: WormDotsIndicator = itemView.findViewById(R.id.worm_dots_indicator)
    }

    class SaveProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val saveProfileButton: AppCompatButton = itemView.findViewById(R.id.save_btn)
    }

    class BusinessViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val businessNameEditText: EditText = itemView.findViewById(R.id.business_name)
    }

    class CatalogueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val catalogueRecyclerView: RecyclerView = itemView.findViewById(R.id.recyclerViewer)
    }

    class BusinessContactDetailsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val emailTextView: EditText = itemView.findViewById(R.id.email)
        val businessPhoneNumberTextView: EditText =
            itemView.findViewById(R.id.phonenumber_edit_text)
        val addressTextView: EditText = itemView.findViewById(R.id.address)
    }

    // TypeViewHolder.java
    class TypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val businessTypeTextView: EditText = itemView.findViewById(R.id.business_type)
    }
}

//





