package com.uyscuti.social.business.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.annotation.RequiresExtension
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator
import com.uyscuti.sharedmodule.adapter.MediaPagerAdapter
import com.uyscuti.social.business.R
import com.uyscuti.social.business.room.database.BusinessDatabase
import com.uyscuti.social.business.room.entity.BusinessCatalogueEntity
import com.uyscuti.social.business.room.entity.BusinessEntity
import com.uyscuti.social.business.room.repository.BusinessRepository
import com.uyscuti.social.business.util.ImagePicker
import com.uyscuti.social.network.api.request.business.create.BackgroundPhoto
import com.uyscuti.social.network.api.request.business.create.BusinessCatalogue
import com.uyscuti.social.network.api.request.business.create.BusinessLocation
import com.uyscuti.social.network.api.request.business.create.Contact
import com.uyscuti.social.network.api.request.business.create.CreateBusinessProfile
import com.uyscuti.social.network.api.request.business.create.Location
import com.uyscuti.social.network.api.request.business.create.LocationInformation
import com.uyscuti.social.network.api.request.business.create.WalkingBillboard
import com.uyscuti.social.network.api.response.business.response.background.BackgroundVideoResponse
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.uyscuti.sharedmodule.model.ShowBottomNav
import com.uyscuti.sharedmodule.utils.LocationServiceUtil
import com.uyscuti.sharedmodule.utils.NetworkUtil
import com.uyscuti.social.business.util.LocationDialog
import com.uyscuti.social.network.api.response.business.response.background.BackgroundVideo
import org.greenrobot.eventbus.EventBus

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment1.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class ProfileFragment1 : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    val TAG = "ProfileFragment1"

    private lateinit var uploadButton: AppCompatButton

    private lateinit var viewPager: ViewPager2

    private lateinit var dotsIndicator: WormDotsIndicator

    private lateinit var businessNameEditText: EditText

    private lateinit var descriptionEditText: EditText

    private lateinit var businessTypeEditText: EditText

    private lateinit var emailEditText: EditText

    private lateinit var businessPhoneNumberEditText: EditText

    private lateinit var addressEditText: EditText

    private lateinit var businessLocationSwitcher: SwitchCompat

    private lateinit var currentLocationSwitcher: SwitchCompat

    private lateinit var saveProfileButton: AppCompatButton

    private var checkBox: CheckBox? = null
    private var checkBox2: CheckBox? = null


    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    // Create location request for continuous updates
    val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        5000L // Update interval: 5 seconds
    ).apply {
        setMinUpdateIntervalMillis(2000L) // Fastest interval: 2 seconds
        setMaxUpdateDelayMillis(10000L)
    }.build()

    private val REQUEST_LOCATION_PERMISSION = 1


    private var latitude: Double? = null
    private var longitude: Double? = null
    private var accuracy: Float? = null
    private var locationRange: Int? = null
    private var locationEnabled: Boolean = false

    private var isBillBoardLiveLocationChecked: Boolean = false
    private var billboardLatitude: Double? = null
    private var billboardLongitude: Double? = null
    private var billboardAccuracy: Float? = null
    private var billboardLocationEnabled: Boolean = false
    private var billboardLocationRange: Int? = null

    private var businessName: String? = null
    private var businessType: String? = null
    private var businessLocation: BusinessLocation? = null
    private var contact: Contact? = null
    private var location: Location? = null
    private var description: String? = null
    private var email: String? = null
    private var phoneNumber: String? = null
    private var address: String? = null
    private lateinit var fullScreenPagerAdapter: MediaPagerAdapter

    private var imageUri: Uri? = null
    private var videoUri: Uri? = null

    private var profile: BusinessEntity? = null

    private var nameError: Boolean = false
    private var typeError: Boolean = false
    private var locationError: Boolean = false
    private var descriptionError: Boolean = false
    private var emailError: Boolean = false
    private var phoneNumberError: Boolean = false
    private var backgroundImageError: Boolean = false
    private var addressError: Boolean = false

    private var backgroundPhoto:  BackgroundPhoto? = null

    private val contactBooleanArray: BooleanArray = BooleanArray(size = 3) { false }

    val mediaUrls = arrayListOf<String>()

    private val API_TAG = "ApiService"

    private var businessId: String? = null

    private var hasBusinessProfile = false

    @Inject
    lateinit var retrofitInterface: RetrofitInstance

    private lateinit var businessDatabase: BusinessDatabase
    private lateinit var businessRepository: BusinessRepository
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var progressBar: ProgressBar

    private lateinit var locationDialog: LocationDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.profile_fragment, container, false)

        sharedPreferences =
            requireContext().getSharedPreferences("BusinessProfile", Context.MODE_PRIVATE)

        businessId = sharedPreferences.getString("businessId", null)

        Log.d(API_TAG, "onCreateView: business id $businessId")

        businessDatabase = BusinessDatabase.getInstance(requireContext())
        businessRepository = BusinessRepository(businessDatabase.businessDao())

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        initViews(view)

        setupEditTextViews()


        handleOnClickListener()

        checkForBusinessProfileAndLoadProfile()


        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileFragment1.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment1().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun checkForBusinessProfileAndLoadProfile() {

        CoroutineScope(Dispatchers.IO).launch {

            if(NetworkUtil.isConnected(requireActivity())) {

                val getBusinessProfile = retrofitInterface.apiService.getBusinessProfile()

                if(getBusinessProfile.isSuccessful) {
                    hasBusinessProfile = true
                    val businessProfile = getBusinessProfile.body()!!


                    val _id = businessProfile._id
                    val businessName = businessProfile.businessName
                    val businessDescription = businessProfile.businessDescription
                    val businessType = businessProfile.businessType
                    val owner = businessProfile.owner
                    val contact = businessProfile.contact
                    val __v = businessProfile.__v
                    val backgroundPhoto = businessProfile.backgroundPhoto!!.url
                    val backgroundVideo = businessProfile.backgroundVideo?.url
                    val videoThumbnail = businessProfile.backgroundVideo?.thumbnail
                    val createdAt = businessProfile.createdAt
                    val updatedAt = businessProfile.updatedAt
                    val location = businessProfile.location

                    val editor = sharedPreferences.edit()

                    editor.putString("businessId", businessProfile._id)
                    editor.putString("businessName", businessProfile.businessName)
                    editor.putString("businessDescription", businessProfile.businessDescription)
                    editor.putString("businessType", businessProfile.businessType)
                    editor.putString("businessOwner", businessProfile.owner)
                    editor.putString("backgroundPhoto", businessProfile.backgroundPhoto!!.url)
                    editor.putString("businessEmail", businessProfile.contact.email)
                    editor.putString("businessPhone", businessProfile.contact.phoneNumber)
                    editor.putString("businessAddress", businessProfile.contact.address)

                    editor.apply()

                    val business = BusinessEntity(
                        _id,
                        __v,
                        backgroundPhoto,
                        backgroundVideo,
                        videoThumbnail,
                        listOf<BusinessCatalogueEntity>(),
                        businessDescription,
                        businessName,
                        businessType,
                        Contact(
                            contact.address,
                            contact.email,
                            contact.phoneNumber,
                            contact.website
                        ),
                        createdAt,
                        com.uyscuti.social.network.api.response.business.response.profile.Location(
                            BusinessLocation(
                                location.businessLocation.enabled,
                                location.businessLocation.locationInfo
                            ),
                            WalkingBillboard(
                                location.walkingBillboard.enabled,
                                location.walkingBillboard.liveLocationInfo
                            )
                        ),
                        owner,
                        updatedAt
                    )
                    insertBusiness(business)

                    withContext(Dispatchers.Main) {
                        currentLocationSwitcher.isChecked = business.location.walkingBillboard.enabled
                        businessLocationSwitcher.isChecked = business.location.businessLocation.enabled
                        if(business.location.businessLocation.enabled) {
                            billboardLocationRange = business.location.businessLocation.locationInfo?.range?.toInt()
                        } else {
                            billboardLocationRange = 0
                        }


                        this@ProfileFragment1.businessNameEditText.setText(business.businessName)
                        this@ProfileFragment1.businessTypeEditText.setText(business.businessType)
                        this@ProfileFragment1.descriptionEditText.setText(business.businessDescription)
                        this@ProfileFragment1.addressEditText.setText(business.contact.address)
                        this@ProfileFragment1.emailEditText.setText(business.contact.email)
                        this@ProfileFragment1.businessPhoneNumberEditText.setText(business.contact.phoneNumber)
                        this@ProfileFragment1.fullScreenPagerAdapter.addImage(business.backgroundPhoto.toUri())
                        this@ProfileFragment1.fullScreenPagerAdapter.addVideo(Uri.parse(business.backgroundVideo.toString()))
                    }
                } else {
                    hasBusinessProfile = false
                    Log.d(API_TAG, "$getBusinessProfile")
                }


            } else {

                val businessProfile = getBusiness()

                if(businessProfile == null) {
                    return@launch
                }

                hasBusinessProfile = true
                withContext(Dispatchers.Main) {
                    businessLocationSwitcher.isChecked = businessProfile.location.businessLocation.enabled
                    currentLocationSwitcher.isChecked = businessProfile.location.walkingBillboard.enabled
                    if(businessProfile.location.businessLocation.enabled) {
                        billboardLocationRange = businessProfile.location.businessLocation.locationInfo?.range?.toInt()
                    } else {
                        billboardLocationRange = 0
                    }

                    this@ProfileFragment1.businessNameEditText.setText(businessProfile.businessName)
                    this@ProfileFragment1.businessTypeEditText.setText(businessProfile.businessType)
                    this@ProfileFragment1.descriptionEditText.setText(businessProfile.businessDescription)
                    this@ProfileFragment1.addressEditText.setText(businessProfile.contact.address)
                    this@ProfileFragment1.emailEditText.setText(businessProfile.contact.email)
                    this@ProfileFragment1.businessPhoneNumberEditText.setText(businessProfile.contact.phoneNumber)
                    this@ProfileFragment1.fullScreenPagerAdapter.addImage(businessProfile.backgroundPhoto.toUri())
                    this@ProfileFragment1.fullScreenPagerAdapter.addVideo(Uri.parse(businessProfile.backgroundVideo.toString()))
                }



            }
        }
    }

    private fun initViews(view: View) {
        uploadButton = view.findViewById(R.id.upload_button)
        viewPager = view.findViewById(R.id.viewPager)
        dotsIndicator = view.findViewById(R.id.worm_dots_indicator)

        businessNameEditText = view.findViewById(R.id.business_name)

        descriptionEditText = view.findViewById(R.id.description_edit)

        businessTypeEditText = view.findViewById(R.id.business_type)

        emailEditText = view.findViewById(R.id.email)

        addressEditText = view.findViewById(R.id.address)

        saveProfileButton = view.findViewById(R.id.save_btn)

        businessPhoneNumberEditText = view.findViewById(R.id.phonenumber_edit_text)

        businessLocationSwitcher = view.findViewById(R.id.business_location_toggle)

        currentLocationSwitcher = view.findViewById(R.id.current_location_toggle)

        progressBar = view.findViewById(R.id.business_progress)

        locationDialog = LocationDialog(requireActivity())

        fullScreenPagerAdapter = MediaPagerAdapter(mediaUrls, requireActivity())

        viewPager.adapter = fullScreenPagerAdapter

        viewPager.offscreenPageLimit = 4

        attachDotsIndicator()

        viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){

            override fun onPageSelected(position: Int) {
                if (position == 0) {
                    fullScreenPagerAdapter.resumePlayer()
                } else {
                    fullScreenPagerAdapter.pausePlayer()
                }
            }

        })


    }

    private fun setupEditTextViews() {

        if (nameError) {
            businessNameEditText.error = "Required"
        }

        businessNameEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val businessNameTextView = businessNameEditText
                val businessName = businessNameTextView.text.toString()
                if (businessName.isEmpty()) {

                    businessNameTextView.error = "Required"

                } else {

                    return@setOnFocusChangeListener
                }
            }
        }

        if (descriptionError) {
            descriptionEditText.error = "Required"
        }

        descriptionEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val descriptionTextView = descriptionEditText
                val description = descriptionTextView.text.toString()
                if (description.isEmpty()) {
                    descriptionTextView.error = "Required"
                }
            }
        }


        if (typeError) {
            businessTypeEditText.error = "Required"
        }

        businessTypeEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val businessTypeTextView = businessTypeEditText
                val businessType = businessTypeTextView.text.toString()
                if (businessType.isEmpty()) {
                    businessTypeTextView.error = "Required"
                } else {
                    return@setOnFocusChangeListener
                }
            }

        }


        contactBooleanArray.forEachIndexed { index, b ->
            if (index == 0 && b) {
                emailEditText.error = "Required"
            } else if (index == 1 && b) {
                businessPhoneNumberEditText.error = "Required"
            } else if (index == 2 && b) {
                addressEditText.error = "Required"
            }
        }

        emailEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                // When focus is lost, validate the email
                val emailTextView = emailEditText
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

        businessPhoneNumberEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val businessPhoneNumberTextView = businessPhoneNumberEditText
                val businessPhoneNumber = businessPhoneNumberTextView.text.toString()

                if (businessPhoneNumber.isEmpty()) {
                    businessPhoneNumberTextView.error = "Required"
                } else {
                    return@setOnFocusChangeListener
                }
            }
        }

        addressEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val addressTextView = addressEditText
                val address = addressTextView.text.toString()
                if (address.isEmpty()) {
                    addressTextView.error = "Required"
                } else {
                    return@setOnFocusChangeListener
                }
            }
        }

    }

    private fun pickImage() {
        ImagePicker.pickMedia(requireActivity())
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private fun handleOnClickListener() {
        uploadButton.setOnClickListener { pickImage() }

        businessLocationSwitcher.setOnClickListener {

            if(businessLocationSwitcher.isChecked) {
                val dialog = Dialog(requireActivity())
                dialog.setContentView(R.layout.business_tag_popup)
                val cancelButton: AppCompatButton = dialog.findViewById(R.id.cancel_Btn)
                val confirmButton: AppCompatButton = dialog.findViewById(R.id.proceedBtn)

                cancelButton.setOnClickListener {
                    businessLocationSwitcher.isChecked = false
                    dialog.dismiss()
                }

                confirmButton.setOnClickListener @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION]) {
                    val rangeRadioGroup =
                        dialog.findViewById<RadioGroup>(R.id.rangeRadioGroup)

                    val selectedRadioButtonId = rangeRadioGroup.checkedRadioButtonId

                    val range = dialog.findViewById<RadioButton>(R.id.range_500_Metres)
                    val range2 = dialog.findViewById<RadioButton>(R.id.range_1kilometer)
                    val range3 = dialog.findViewById<RadioButton>(R.id.range_2kilometre)
                    val range4 = dialog.findViewById<RadioButton>(R.id.range_3kilometre)

                    // Check if a radio button is selected
                    if (selectedRadioButtonId != -1) {
                        businessLocationSwitcher.isChecked = true
                        locationDialog.show()
                        // At least one radio button is selected
                        val selectedRadioButton =
                            dialog.findViewById<RadioButton>(selectedRadioButtonId)

                        when (selectedRadioButton) {
                            range -> {
                                locationRange = 500
                                billboardLocationRange = 500
                            }
                            range2 -> {
                                locationRange = 1000
                                billboardLocationRange = 1000
                            }
                            range3 -> {
                                locationRange = 2000
                                billboardLocationRange = 2000
                            }
                            range4 -> {
                                locationRange = 3000
                                billboardLocationRange = 3000
                            }
                        }

                        locationEnabled = true

                        if (ActivityCompat.checkSelfPermission(
                                requireActivity(),
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                requireActivity(),
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {

                        }
                        if (!getLocation()) {
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

            } else {
                latitude = 0.0
                longitude = 0.0
                locationRange = 0
                accuracy = 0.0f
                locationEnabled = false

                if(hasBusinessProfile) {
                    progressBar.visibility = View.VISIBLE
                    CoroutineScope(Dispatchers.IO).launch {

                        val enabled = RequestBody.create("text/plain".toMediaTypeOrNull(), locationEnabled.toString())
                        val businessLatitude = RequestBody.create("text/plain".toMediaTypeOrNull(), latitude.toString())
                        val businessLongitude = RequestBody.create("text/plain".toMediaTypeOrNull(), longitude.toString())
                        val businessAccuracy = RequestBody.create("text/plain".toMediaTypeOrNull(), accuracy.toString())
                        val businessRange = RequestBody.create("text/plain".toMediaTypeOrNull(), locationRange.toString())


                        // update business location
                        val businessLocation = retrofitInterface.apiService.updateBusinessLocation(
                            enabled,
                            businessLatitude,
                            businessLongitude,
                            businessAccuracy,
                            businessRange
                        )

                        if (businessLocation.isSuccessful) {
                            withContext(Dispatchers.Main) {
                                progressBar.visibility = View.GONE
                                businessLocationSwitcher.isChecked = false
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    requireActivity(),
                                    "Something went wrong. Try again",
                                    Toast.LENGTH_SHORT
                                ).show()
                                businessLocationSwitcher.isChecked = true
                            }
                        }
                    }

                } else {
                    businessLocationSwitcher.isChecked = false
                }
            }
        }

        currentLocationSwitcher.setOnClickListener {

            if (currentLocationSwitcher.isChecked) {

                val dialog = Dialog(requireActivity())
                dialog.setContentView(R.layout.bill_board_popup)
                val cancelButton: AppCompatButton = dialog.findViewById(R.id.cancelBtn)
                val confirmButton: AppCompatButton = dialog.findViewById(R.id.confirmBtn)

                cancelButton.setOnClickListener {
                    isBillBoardLiveLocationChecked = false
                    currentLocationSwitcher.isChecked = false
                    dialog.dismiss()

                }

                confirmButton.setOnClickListener {
                    val acknowledgementDialog = Dialog(requireActivity())
                    acknowledgementDialog.setContentView(R.layout.acknowledge_popup)

                    val disagreeButton: AppCompatButton = acknowledgementDialog.findViewById(R.id.disagree_button)

                    val acknowledgeButton: AppCompatButton = acknowledgementDialog.findViewById(R.id.agree_button)

                    checkBox = acknowledgementDialog.findViewById(R.id.agree_checkbox)
                    checkBox2 = acknowledgementDialog.findViewById(R.id.flash_checkbox)

                    disagreeButton.setOnClickListener {

                        checkBox?.isChecked = false
                        checkBox2?.isChecked = false
                        currentLocationSwitcher.isChecked = false
                        acknowledgementDialog.dismiss()
//                                dialog.setContentView(R.layout.bill_board_popup)

                    }
                    acknowledgeButton.setOnClickListener @RequiresPermission(
                        allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION]
                    ) {
                        locationDialog.show()
                        currentLocationSwitcher.isChecked = true
                        isBillBoardLiveLocationChecked = true

                        if(billboardLocationRange == null || billboardLocationRange == 0)
                            billboardLocationRange = 100 // default bill board range
                         else
                            billboardLocationRange = billboardLocationRange

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

            } else {
                if (hasBusinessProfile) {
                    progressBar.visibility = View.VISIBLE

                    billboardLatitude = 0.0
                    billboardLongitude = 0.0
                    billboardAccuracy = 0.0f
                    billboardLocationRange = 0
                    isBillBoardLiveLocationChecked = false

                    val businessBillBoardLatitude = RequestBody.create("text/plain".toMediaTypeOrNull(), billboardLatitude.toString())
                    val businessBillBoardLongitude = RequestBody.create("text/plain".toMediaTypeOrNull(), billboardLongitude.toString())
                    val businessBillBoardAccuracy = RequestBody.create("text/plain".toMediaTypeOrNull(), billboardAccuracy.toString())
                    val businessBillBoardRange = RequestBody.create("text/plain".toMediaTypeOrNull(), billboardLocationRange.toString())
                    val enabled = RequestBody.create("text/plain".toMediaTypeOrNull(), isBillBoardLiveLocationChecked.toString())

                    CoroutineScope(Dispatchers.IO).launch {
                        val billBoardInfo = retrofitInterface.apiService.updateLiveLocation(
                            enabled,
                            businessBillBoardLatitude,
                            businessBillBoardLongitude,
                            businessBillBoardAccuracy,
                            businessBillBoardRange
                        )

                        if (billBoardInfo.isSuccessful) {
                            withContext(Dispatchers.Main) {
                                currentLocationSwitcher.isChecked = false
                                progressBar.visibility = View.GONE
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(requireActivity()
                                    ,"Something went wrong. Try again",
                                    Toast.LENGTH_SHORT )
                                    .show()
                                currentLocationSwitcher.isChecked = true
                            }
                        }
                    }
                }
            }
        }

        saveProfileButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    validateData()
                }
            }
        }
    }

    private fun attachDotsIndicator() {
        dotsIndicator.attachTo(viewPager)
    }

    fun setProfileImage(imageUri: Uri) {
        this.imageUri = imageUri
        fullScreenPagerAdapter.addImage(imageUri)
    }

    fun setProfileVideo(videoUri: Uri) {
        val getVideoDuration = getVideoDuration(videoUri)
        val maxVideoDuration = 30 * 1000

        if (getVideoDuration > maxVideoDuration) {
            Toast.makeText(
                context,
                "Video duration must be less than 30 seconds",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            this.videoUri = videoUri
            fullScreenPagerAdapter.addVideo(videoUri)
        }


    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().post(ShowBottomNav(false))
    }

    private fun getVideoDuration(videoUri: Uri): Long {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, videoUri)
        val durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        return durationString?.toLongOrNull() ?: 0
    }

    fun getRealPathFromUri(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = requireActivity().contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                return it.getString(columnIndex)
            }
        }
        return null
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private suspend fun validateData() {
        businessName = businessNameEditText.text.toString()
        businessType = businessTypeEditText.text.toString()
        description = descriptionEditText.text.toString()

        email = emailEditText.text.toString()
        phoneNumber = businessPhoneNumberEditText.text.toString()
        address = addressEditText.text.toString()

        contactBooleanArray[0] = email.isNullOrEmpty()
        contactBooleanArray[1] = phoneNumber.isNullOrEmpty()
        contactBooleanArray[2] = address.isNullOrEmpty()

        nameError = businessName.isNullOrEmpty()
        typeError = businessType.isNullOrEmpty()
        locationError = location == null
        descriptionError = description.isNullOrEmpty()
        emailError = email.isNullOrEmpty()
        phoneNumberError = phoneNumber.isNullOrEmpty()
        addressError = address.isNullOrEmpty()


        if(!nameError && !typeError && !emailError && !descriptionError && !addressError && !phoneNumberError ) {

            val email = emailEditText.text.toString()

            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                withContext(Dispatchers.Main) {
                    emailEditText.error = "Invalid email format"
                }
            } else {
                createProfile()
            }

        } else {
            withContext(Dispatchers.Main) {
                if(nameError) {
                    businessNameEditText.error = "Required"
                }
                if (typeError) {
                    businessTypeEditText.error = "Required"
                }
                if(descriptionError) {
                    descriptionEditText.error = "Required"
                }

                if(emailError) {
                    emailEditText.error = "Required"
                }

                if(phoneNumberError) {
                    businessPhoneNumberEditText.error = "Required"
                }
                if(addressError) {
                    addressEditText.error = "Required"
                }
            }

        }
    }

    private fun String.startsWithHttpOrHttps(): Boolean {
        return this.matches(Regex("^(?i)(http|https)://.*"))
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private suspend fun createProfile() {

        Log.d(API_TAG,"CreatedProfile: called")


        try {
            if(mediaUrls.isNotEmpty()) {

                for(uri in mediaUrls) {

                    if(!fullScreenPagerAdapter.isVideoUrl(uri)) {
                        if (uri.startsWithHttpOrHttps()) {
                            backgroundPhoto =  BackgroundPhoto(uri)
                        } else {
                            backgroundPhoto =  BackgroundPhoto("https://www.nla.gov.au/sites/default/files/pic-1.jpg")
                        }
                    }
                }
            }

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
                    range = billboardLocationRange.toString()
                )

                businessLocation = BusinessLocation(locationEnabled, locationInformation)
                val walkingBillboard = WalkingBillboard(isBillBoardLiveLocationChecked, liveLocationInformation)
                val location = Location(businessLocation!!, walkingBillboard)

                val contact = Contact(address!!, email!!, phoneNumber!!, "www.business.com")
                val catalogue = listOf<BusinessCatalogue>()

            if(backgroundPhoto == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireActivity(),"Upload Business Profile Photo", Toast.LENGTH_SHORT).show()
                }
            } else {
                val createBusinessProfile = CreateBusinessProfile(
                    backgroundPhoto!!,
                    catalogue,
                    description!!,
                    businessName!!,
                    businessType!!,
                    contact,
                    location
                )

                insertBusinessProfile(createBusinessProfile)
            }


        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private suspend fun insertBusinessProfile(profile: CreateBusinessProfile) {

        Log.d(API_TAG,"InsertBusinessProfile: called")

            try {
                val response = retrofitInterface.apiService.createBusinessProfile(profile)

                Log.d(API_TAG,"CallingCreateBusinessProfile: called")

                if (response.isSuccessful) {
                    val createdProfile = response.body()!!.business
                    Log.d(API_TAG, "Profile created successfully")
                    Log.d(API_TAG, "Response: ${response.body()}")

                    val video: com.uyscuti.social.network.api.response.business.response.get.BackgroundVideo? = createdProfile.backgroundVideo

                    val _id = createdProfile._id
                    val businessName = createdProfile.businessName
                    val businessDescription = createdProfile.businessDescription
                    val businessType = createdProfile.businessType
                    val owner = createdProfile.owner
                    val contact = createdProfile.contact
                    val __v = createdProfile.__v
                    val backgroundPhoto = createdProfile.backgroundPhoto.url
                    val backgroundVideo = video?.url
                    val videoThumbnail = video?.thumbnail
                    val createdAt = createdProfile.createdAt
                    val updatedAt = createdProfile.updatedAt
                    val location = createdProfile.location

                    val editor = sharedPreferences.edit()
                    editor.putString("businessId", createdProfile._id)
                    editor.putString("businessName", createdProfile.businessName)
                    editor.putString("businessDescription", createdProfile.businessDescription)
                    editor.putString("businessType", createdProfile.businessType)
                    editor.putString("businessOwner", createdProfile.owner)
                    editor.putString("backgroundPhoto", createdProfile.backgroundPhoto.url)
                    editor.putString("businessEmail", createdProfile.contact.email)
                    editor.putString("businessPhone", createdProfile.contact.phoneNumber)
                    editor.putString("businessAddress", createdProfile.contact.address)

                    editor.apply()


                    val business = BusinessEntity(
                        _id,
                        __v,
                        backgroundPhoto,
                        backgroundVideo,
                        videoThumbnail,
                        listOf<BusinessCatalogueEntity>(),
                        businessDescription,
                        businessName,
                        businessType,
                        Contact(
                            contact.address,
                            contact.email,
                            contact.phoneNumber,
                            contact.website
                        ),
                        createdAt,
                        com.uyscuti.social.network.api.response.business.response.profile.Location(
                            com.uyscuti.social.network.api.request.business.create.BusinessLocation(
                                location.businessLocation.enabled,
                                location.businessLocation.locationInfo
                            ),
                            com.uyscuti.social.network.api.request.business.create.WalkingBillboard(
                                location.walkingBillboard.enabled,
                                location.walkingBillboard.liveLocationInfo
                            )

                        ),
                        owner,
                        updatedAt
                    )

                    insertBusiness(business)
                    Log.d(API_TAG, "background imageUri: ${this@ProfileFragment1.imageUri}")
                    Log.d(API_TAG, "background videoUri: ${this@ProfileFragment1.videoUri}")



                    if (this@ProfileFragment1.imageUri != null) {
                        val imagePath = getRealPathFromUri(this@ProfileFragment1.imageUri!!)
                        Log.d(API_TAG, "imagePath: $imagePath")
                        Log.d(API_TAG, "background imageUri: ${this@ProfileFragment1.imageUri}")

                        updateBusinessProfileImage(imagePath!!)
                    }

                    if (this@ProfileFragment1.videoUri != null) {
                        val videoPath = this@ProfileFragment1.videoUri!!.path

                        val videoFile = File(videoPath!!)
                        val thumbnailFile = File(extractThumbnail(this@ProfileFragment1.videoUri!!))

                        Log.d(API_TAG, "videoPath: $videoPath")
                        Log.d(API_TAG, "videoFile: $videoFile")
                        Log.d(API_TAG, "thumbnailFile: $thumbnailFile")

                        updateBusinessProfileVideo(videoFile, thumbnailFile)
                    }

                } else {
                    Log.d(API_TAG,"Failed to create business Profile: called")
                    Log.d(API_TAG,"response ${response}")
                    Log.d(API_TAG,"Background Photo $backgroundPhoto")
                }

            }catch (e:HttpException){
                Log.d(API_TAG, "Error: $e")
                Log.d(API_TAG, "Error: ${e.response()?.errorBody()?.string()}")
            }catch (e:IOException){
                Log.d(API_TAG, "Error: $e")
            }


    }

    @SuppressLint("SupportAnnotationUsage")
    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun getLocation(): Boolean {

        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION

            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
            return false
        }

        if(LocationServiceUtil.isLocationEnabled(requireActivity())) {

            if(!hasBusinessProfile) {
                    locationDialog.dismiss()
                    showProgressDialog(
                        "Business Profile",
                        "To enable this feature you need to have a business profile."
                    )
                    businessLocationSwitcher.isChecked = false

            } else {

                // Create location callback
                locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        val location = locationResult.lastLocation

                        if (location != null) {
                            val accuracy = location.accuracy

                            // Check if accuracy is acceptable
                            if (accuracy < 11f) {
                                // Good accuracy - stop updates and use this location
                                stopLocationUpdates()
                                locationDialog.dismiss()

                                latitude = location.latitude
                                longitude = location.longitude

                                // Use the location
                                CoroutineScope(Dispatchers.IO).launch {
                                        // creating request body for the end point
                                        val locationEnabled = RequestBody.create(
                                            "text/plain".toMediaTypeOrNull(),
                                            locationEnabled.toString()
                                        )
                                        val businessLatitude = RequestBody.create(
                                            "text/plain".toMediaTypeOrNull(),
                                            latitude.toString()
                                        )
                                        val businessLongitude = RequestBody.create(
                                            "text/plain".toMediaTypeOrNull(),
                                            longitude.toString()
                                        )
                                        val businessAccuracy = RequestBody.create(
                                            "text/plain".toMediaTypeOrNull(),
                                            accuracy.toString()
                                        )
                                        val businessRange = RequestBody.create(
                                            "text/plain".toMediaTypeOrNull(),
                                            locationRange.toString()
                                        )


                                        // update business location
                                        val businessLocation =
                                            retrofitInterface.apiService.updateBusinessLocation(
                                                locationEnabled,
                                                businessLatitude,
                                                businessLongitude,
                                                businessAccuracy,
                                                businessRange
                                            )

                                        if (businessLocation.isSuccessful) {
                                            // update the switch and progress bar visibility
                                            withContext(Dispatchers.Main) {
                                                locationDialog.dismiss()
                                                showProgressDialog(
                                                    "Location service",
                                                    "Business location captured\nNOTE: Please disable location service."
                                                )
                                                locationDialog.updateMessageStatus(
                                                    resources.getString(R.string.getting_your_current_location_nplease_wait)
                                                )
                                            }
                                        } else {
                                            withContext(Dispatchers.Main) {
                                                locationDialog.dismiss()
                                                businessLocationSwitcher.isChecked = false

                                                Toast.makeText(
                                                    requireActivity(),
                                                    "Something went wrong. Try again",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                }
                            }
                        }
                    }
                }

                // Request location updates
                try {
                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper()
                    )

                    // Set a timeout to prevent infinite waiting
                    Handler(Looper.getMainLooper()).postDelayed({
                        locationDialog.updateMessageStatus(
                            "Please move around, it is getting too long, to get your current location."
                        )
                    }, 30000L) // 30 seconds timeout

                } catch (e: SecurityException) {
                    locationDialog.dismiss()
                }
            }

        } else {
            businessLocationSwitcher.isChecked = false
            locationDialog.dismiss()
            showDialogToEnableLocationService(
                "Please enable location service to capture your business location.\nNOTE: Do this in your place of business!"
            )
        }

        return true
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

        @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun getBillBoardLocation(): Boolean {

        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION

            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
            return false
        }

        if(LocationServiceUtil.isLocationEnabled(requireActivity())) {
            if(!hasBusinessProfile) {
                locationDialog.dismiss()
                showProgressDialog(
                    "Business Profile",
                    "To enable this feature you need to have a business profile."
                )
                currentLocationSwitcher.isChecked = false
            } else {

                locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        val location = locationResult.lastLocation

                        if (location != null) {
                            val accuracy = location.accuracy

                            // Check if accuracy is acceptable
                            if (accuracy < 11f) {
                                // Good accuracy - stop updates and use this location
                                stopLocationUpdates()
                                locationDialog.dismiss()

                                // Use the location
                                billboardLatitude = location.latitude
                                billboardLongitude = location.longitude
                                billboardAccuracy = location.accuracy

                                val businessBillBoardLatitude = RequestBody.create(
                                    "text/plain".toMediaTypeOrNull(),
                                    billboardLatitude.toString()
                                )
                                val businessBillBoardLongitude = RequestBody.create(
                                    "text/plain".toMediaTypeOrNull(),
                                    billboardLongitude.toString()
                                )
                                val businessBillBoardAccuracy = RequestBody.create(
                                    "text/plain".toMediaTypeOrNull(),
                                    billboardAccuracy.toString()
                                )
                                val businessBillBoardRange = RequestBody.create(
                                    "text/plain".toMediaTypeOrNull(),
                                    billboardLocationRange.toString()
                                )
                                val enabled = RequestBody.create(
                                    "text/plain".toMediaTypeOrNull(),
                                    isBillBoardLiveLocationChecked.toString()
                                )

                                CoroutineScope(Dispatchers.IO).launch {
                                    val billBoardInfo =
                                        retrofitInterface.apiService.updateLiveLocation(
                                            enabled,
                                            businessBillBoardLatitude,
                                            businessBillBoardLongitude,
                                            businessBillBoardAccuracy,
                                            businessBillBoardRange
                                        )

                                    if (billBoardInfo.isSuccessful) {
                                        withContext(Dispatchers.Main) {
                                            locationDialog.dismiss()
                                            showProgressDialog(
                                                "Location service",
                                                "Bill board live location captured\n" +
                                                        "To advertise your business with bill board live location while moving around enable location service while using flash.\n" +
                                                        "NOTE: Please disable location service when not using flash."
                                            )
                                            locationDialog.updateMessageStatus(
                                                resources.getString(R.string.getting_your_current_location_nplease_wait)
                                            )
                                        }
                                    } else {
                                        withContext(Dispatchers.Main) {
                                            locationDialog.dismiss()
                                            currentLocationSwitcher.isChecked = false

                                            Toast.makeText(
                                                requireActivity(),
                                                "Something went wrong. Try again",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                try {
                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper()
                    )

                    // Set a timeout to prevent infinite waiting
                    Handler(Looper.getMainLooper()).postDelayed({
                        locationDialog.updateMessageStatus(
                            "Please move around, it is getting too long, to get your current location."
                        )
                    }, 30000L) // 30 seconds timeout

                } catch (e: SecurityException) {
                    locationDialog.dismiss()
                }
            }
        } else {
            locationDialog.dismiss()
            currentLocationSwitcher.isChecked = false
            showDialogToEnableLocationService(
                "This feature requires location services.\nPlease enable device location service"
            )
        }


        return true
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private suspend fun updateBusinessProfileImage(imageUri: String) {
        val imageFile = File(imageUri)
        val mediaType = "image/*".toMediaTypeOrNull()
        val requestBody = RequestBody.create(mediaType, imageFile)

        Log.d(API_TAG, "background imageUri: $imageUri")


        // Create a RequestBody from the file
//        val requestFile = RequestBody.create(mediaType, avatarFile)

        // Create a MultipartBody.Part from the RequestBody
        val avatarPart = MultipartBody.Part.createFormData("background", imageFile.name, requestBody)

//
        try {

            val response = retrofitInterface.apiService.updateBackground(avatarPart)

            if (response.isSuccessful) {

                val updatedBusinessProfile = response.body()!!.updatedBusinessProfile

                Log.d(API_TAG, "Background image updated successfully")

                val video = updatedBusinessProfile.backgroundVideo?: null

                val _id = updatedBusinessProfile._id
                val businessName = updatedBusinessProfile.businessName
                val businessDescription = updatedBusinessProfile.businessDescription
                val businessType = updatedBusinessProfile.businessType
                val owner = updatedBusinessProfile.owner
                val contact = updatedBusinessProfile.contact
                val __v = updatedBusinessProfile.__v
                val backgroundPhoto = updatedBusinessProfile.backgroundPhoto.url
                val backgroundVideo = video?.url
                val videoThumbnail = video?.thumbnail
                val createdAt = updatedBusinessProfile.createdAt
                val updatedAt = updatedBusinessProfile.updatedAt
                val location = updatedBusinessProfile.location


                val editor = sharedPreferences.edit()
                editor.putString("businessId", updatedBusinessProfile._id)
                editor.putString("businessName", updatedBusinessProfile.businessName)
                editor.putString("businessDescription", updatedBusinessProfile.businessDescription)
                editor.putString("businessType", updatedBusinessProfile.businessType)
                editor.putString("businessOwner", updatedBusinessProfile.owner)
                editor.putString("backgroundPhoto", updatedBusinessProfile.backgroundPhoto.url)
                editor.putString("businessEmail", updatedBusinessProfile.contact.email)
                editor.putString("businessPhone", updatedBusinessProfile.contact.phoneNumber)
                editor.putString("businessAddress", updatedBusinessProfile.contact.address)

                editor.apply()


                val business = BusinessEntity(
                    _id,
                    __v,
                    backgroundPhoto,
                    backgroundVideo,
                    videoThumbnail,
                    listOf<BusinessCatalogueEntity>(),
                    businessDescription,
                    businessName,
                    businessType,
                    Contact(
                        contact.address,
                        contact.email,
                        contact.phoneNumber,
                        contact.website
                    ),
                    createdAt,
                    com.uyscuti.social.network.api.response.business.response.profile.Location(
                        com.uyscuti.social.network.api.request.business.create.BusinessLocation(
                            location.businessLocation.enabled,
                            location.businessLocation.locationInfo
                        ),
                        com.uyscuti.social.network.api.request.business.create.WalkingBillboard(
                            location.walkingBillboard.enabled,
                            location.walkingBillboard.liveLocationInfo
                        )

                    ),
                    owner,
                    updatedAt
                )

                insertBusiness(business)

            } else {
                Log.d(API_TAG, "Error: ${response.errorBody()?.string()}")
            }

        }catch (e:HttpException){
            Log.d(API_TAG, "update background image error: $e")
        }catch (e:IOException){
            Log.d(API_TAG, "update background image error: $e")
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private suspend fun updateBusinessProfileVideo(videoFile: File, thumbnailFile: File){

        try {
            val response = createBackgroundVideo(videoFile, thumbnailFile)

            if (response.isSuccessful) {
                val updatedBusinessProfile = response.body()!!.updatedBusinessProfile

                Log.d(API_TAG, "Background video updated successfully")
                Log.d(API_TAG, "Response: $updatedBusinessProfile")

                val _id = updatedBusinessProfile._id
                val businessName = updatedBusinessProfile.businessName
                val businessDescription = updatedBusinessProfile.businessDescription
                val businessType = updatedBusinessProfile.businessType
                val owner = updatedBusinessProfile.owner
                val contact = updatedBusinessProfile.contact
                val __v = updatedBusinessProfile.__v
                val backgroundPhoto = updatedBusinessProfile.backgroundPhoto.url
                val backgroundVideo = updatedBusinessProfile.backgroundVideo.url
                val videoThumbnail = updatedBusinessProfile.backgroundVideo.thumbnail
                val createdAt = updatedBusinessProfile.createdAt
                val updatedAt = updatedBusinessProfile.updatedAt
                val location = updatedBusinessProfile.location

                val editor = sharedPreferences.edit()
                editor.putString("businessId", updatedBusinessProfile._id)
                editor.putString("businessName", updatedBusinessProfile.businessName)
                editor.putString("businessDescription", updatedBusinessProfile.businessDescription)
                editor.putString("businessType", updatedBusinessProfile.businessType)
                editor.putString("businessOwner", updatedBusinessProfile.owner)
                editor.putString("backgroundPhoto", updatedBusinessProfile.backgroundPhoto.url)
                editor.putString("businessEmail", updatedBusinessProfile.contact.email)
                editor.putString("businessPhone", updatedBusinessProfile.contact.phoneNumber)
                editor.putString("businessAddress", updatedBusinessProfile.contact.address)

                editor.apply()

                val business = BusinessEntity(
                    _id,
                    __v,
                    backgroundPhoto,
                    backgroundVideo,
                    videoThumbnail,

                    listOf<BusinessCatalogueEntity>(),
                    businessDescription,
                    businessName,
                    businessType,
                    Contact(
                        contact.address,
                        contact.email,
                        contact.phoneNumber,
                        contact.website
                    ),
                    createdAt,
                    com.uyscuti.social.network.api.response.business.response.profile.Location(
                        com.uyscuti.social.network.api.request.business.create.BusinessLocation(
                            location.businessLocation.enabled,
                            location.businessLocation.locationInfo
                        ),
                        com.uyscuti.social.network.api.request.business.create.WalkingBillboard(
                            location.walkingBillboard.enabled,
                            location.walkingBillboard.liveLocationInfo
                        )

                    ),
                    owner,
                    updatedAt
                )

                insertBusiness(business)
            } else {
                Log.d(API_TAG, "Error: ${response.errorBody()?.string()}")
            }

        }catch (e:HttpException){
            Log.d(API_TAG, "update background video error: $e")
        }catch (e: IOException){
            Log.d(API_TAG, "update background video error: $e")
        }

    }

    private fun extractThumbnail(videoUri: Uri): String {
        // Create the thumbs directory if it doesn't exist
        val thumbsDir= File(requireActivity().externalCacheDir, "thumbs")
        if (!thumbsDir.exists()) {
            Log.d(API_TAG, "thumbsDir: $thumbsDir Does not Exist, creating........")
            thumbsDir.mkdirs()
        } else{
            Log.d(API_TAG, "thumbsDir: $thumbsDir Exists")
        }

        // Create a File object for the thumbnail
        val thumbnailFile = File(thumbsDir, "${System.currentTimeMillis()}.jpg")

        // Create a MediaMetadataRetriever object
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, videoUri)

        // Extract the thumbnail frame
        val bitmap = retriever.getFrameAtTime(0)

        // Save the bitmap to the thumbnail file
        FileOutputStream(thumbnailFile).use {
            bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }

        // Return the real path of the thumbnail file
        return thumbnailFile.absolutePath
    }

    private suspend fun createBackgroundVideo(videoFile: File, thumbnailFile: File): Response<BackgroundVideoResponse>{
        val videoPart =  RequestBody.create("video/*".toMediaTypeOrNull(), videoFile)
        val thumbnailPart = RequestBody.create("image/*".toMediaTypeOrNull(), thumbnailFile)

        val videoPartBody = MultipartBody.Part.createFormData("b_vid", videoFile.name, videoPart)
        val thumbnailPartBody = MultipartBody.Part.createFormData("b_thumb", thumbnailFile.name, thumbnailPart)

        return retrofitInterface.apiService.updateBackgroundVideo(videoPartBody, thumbnailPartBody)
    }

    private suspend fun insertBusiness(business: BusinessEntity) {
        businessRepository.insertBusiness(business)
    }

    private suspend fun getBusiness(): BusinessEntity? {
        return businessRepository.getBusiness()
    }

    private fun showDialogToEnableLocationService(message: String) {
        AlertDialog.Builder(requireActivity())
            .setTitle("Location Service")
            .setMessage(message)
            .setPositiveButton("Open Settings") {dialog,_ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                dialog.dismiss()
                startActivity(intent)
            }
            .setNegativeButton("Not now"){dialog,_ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()

    }

    private fun showProgressDialog(title: String, message: String) {
        AlertDialog.Builder(requireActivity())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") {dialog,_ ->
                dialog.dismiss()
            }
            .show()
    }
}