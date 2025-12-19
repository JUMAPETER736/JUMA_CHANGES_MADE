package com.uyscuti.social.business.fragment


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresExtension
import androidx.annotation.RequiresPermission
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.business.adapter.ProfileAdapter
import com.uyscuti.sharedmodule.model.Catalogue
//import com.example.mylibrary.retro.RetrofitClient
import com.uyscuti.social.network.api.request.business.create.BusinessLocation
import com.uyscuti.social.network.api.request.business.create.Contact
import com.uyscuti.social.network.api.request.business.create.Location
import com.uyscuti.social.network.api.request.business.create.WalkingBillboard
import com.uyscuti.social.network.api.response.business.response.background.BackgroundVideoResponse
//import com.uyscuti.social.network.api.request.business.product.AddProductResponse
import com.uyscuti.social.business.room.database.BusinessDatabase
import com.uyscuti.social.business.room.entity.BusinessCatalogueEntity
import com.uyscuti.social.business.room.entity.BusinessEntity
import com.uyscuti.social.business.room.entity.MyProductEntity
import com.uyscuti.social.business.room.repository.BusinessRepository
import com.uyscuti.social.business.util.ImagePicker
import com.uyscuti.social.business.R
import com.uyscuti.social.network.api.request.business.create.LocationInformation
import com.uyscuti.social.network.api.response.business.response.product.AddProductResponse
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


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var profileAdapter: ProfileAdapter
    private val REQUEST_CODE_IMAGE_PICKER = 100
    private var business: BusinessEntity? = null

//    private val apiService = RetrofitClient.instance
    private lateinit var sharedPreferences: SharedPreferences

    private val catalogueList = arrayListOf<Catalogue>()

    @Inject
    lateinit var retrofitInterface: RetrofitInstance

    private lateinit var businessDatabase: BusinessDatabase
    private lateinit var businessRepository: BusinessRepository

    var onItemSelectedListener: ((Boolean) -> Unit)? = null

    private var imageUri: Uri? = null
    private var videoUri: Uri? = null

    private var businessId: String? = null

    private val API_TAG = "ApiService"

    private val roomCatalogueList = arrayListOf<Catalogue>()

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PersonalChats.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)

        sharedPreferences =
            requireContext().getSharedPreferences("BusinessProfile", Context.MODE_PRIVATE)

        businessId = sharedPreferences.getString("businessId", null)

        Log.d(API_TAG, "onCreateView: business id $businessId")
        businessDatabase = BusinessDatabase.getInstance(requireContext())
        businessRepository = BusinessRepository(businessDatabase.businessDao())

        if (businessId != null) {
            Log.d(API_TAG, "businessId: $businessId")
        }

        // getting products from room database
        CoroutineScope(Dispatchers.IO).launch {
            val business = getBusiness()

            if(business == null) {
                val getBusinessProfile = retrofitInterface.apiService.getBusinessProfile()

                if(getBusinessProfile.isSuccessful) {
                    val businessProfile = getBusinessProfile.body()!!


                    val _id = businessProfile._id
                    val businessName = businessProfile.businessName
                    val businessDescription = businessProfile.businessDescription
                    val businessType = businessProfile.businessType
                    val owner = businessProfile.owner
                    val contact = businessProfile.contact
                    val __v = businessProfile.__v
                    val backgroundPhoto = businessProfile.backgroundPhoto!!.url
                    val backgroundVideo = businessProfile.backgroundVideo!!.url
                    val videoThumbnail = businessProfile.backgroundVideo!!.thumbnail
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
                    this@ProfileFragment.business = business
                    insertBusiness(business)

                    withContext(Dispatchers.Main) {
                        this@ProfileFragment.profileAdapter.setProfile(business)
                    }
                }
            } else {
                this@ProfileFragment.business = business

                Log.d(API_TAG, "Found Business from room: $business")

                withContext(Dispatchers.Main) {
                    this@ProfileFragment.profileAdapter.setProfile(business)
                }

            }
        }

        setupRecyclerView()
        return view
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun getCoordinates(){
        profileAdapter.getLocation()
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private suspend fun updateBackgroundVideo(videoFile: File, thumbnailFile: File){

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



    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private suspend fun updateBackgroundImage(imageUri: String) {
        val imageFile = File(imageUri)
        val mediaType = "image/jpeg".toMediaTypeOrNull()
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


    private suspend fun createProduct(
        itemName: String,
        description: String,
        features: String,
        price: String,
        imageFiles: List<File>
    ): Response<AddProductResponse> {
        val itemNamePart = RequestBody.create("text/plain".toMediaTypeOrNull(), itemName)
        val descriptionPart = RequestBody.create("text/plain".toMediaTypeOrNull(), description)
        val featuresPart = RequestBody.create("text/plain".toMediaTypeOrNull(), features)
        val pricePart = RequestBody.create("text/plain".toMediaTypeOrNull(), price)

        val imageParts = imageFiles.map {
            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), it)
            MultipartBody.Part.createFormData("product", it.name, requestFile)
        }

        return retrofitInterface.apiService.addProduct(itemNamePart, descriptionPart, featuresPart, pricePart, imageParts)
    }

    private suspend fun createBackgroundVideo(videoFile: File, thumbnailFile: File): Response<BackgroundVideoResponse>{
        val videoPart =  RequestBody.create("video/*".toMediaTypeOrNull(), videoFile)
        val thumbnailPart = RequestBody.create("image/*".toMediaTypeOrNull(), thumbnailFile)

        val videoPartBody = MultipartBody.Part.createFormData("b_vid", videoFile.name, videoPart)
        val thumbnailPartBody = MultipartBody.Part.createFormData("b_thumb", thumbnailFile.name, thumbnailPart)

        return retrofitInterface.apiService.updateBackgroundVideo(videoPartBody, thumbnailPartBody)
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private fun   setupRecyclerView() {
        val profileList = listOf("Profile Item 1", "Profile Item 2", "Profile Item 3")
        profileAdapter = ProfileAdapter(requireActivity(),business) { profile ->
            Log.d(API_TAG, "Profile clicked: $profile")

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = retrofitInterface.apiService.createBusinessProfile(profile)

                    if (response.isSuccessful) {

                        val createdProfile = response.body()!!.business
                        Log.d(API_TAG, "Profile created successfully")
                        Log.d(API_TAG, "Response: ${response.body()}")

                        val video = createdProfile.backgroundVideo?: null


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

                        if (imageUri != null) {
                            val imagePath = getRealPathFromUri(imageUri!!)
                            Log.d(API_TAG, "imagePath: $imagePath")
                            Log.d(API_TAG, "background imageUri: $imageUri")


                            updateBackgroundImage(imageUri?.path!!)
                        }

                        if (videoUri != null) {
                            val videoPath = videoUri!!.path

                            val videoFile = File(videoPath!!)
                            val thumbnailFile = File(extractThumbnail(videoUri!!))

                            Log.d(API_TAG, "videoPath: $videoPath")
                            Log.d(API_TAG, "videoFile: $videoFile")
                            Log.d(API_TAG, "thumbnailFile: $thumbnailFile")

                            updateBackgroundVideo(videoFile, thumbnailFile)


                        }

                    } else {
                        Log.d(API_TAG, "Error: ${response.errorBody()?.string()}")
                    }
                } catch (e: HttpException) {
                    Log.d(API_TAG, "Error: $e")
                    Log.d(API_TAG, "Error: ${e.response()?.errorBody()?.string()}")
                    e.printStackTrace()
                } catch (e: Throwable) {
                    Log.d(API_TAG, "Error: $e")
                    e.printStackTrace()
                }catch (e:Exception){
                    Log.d(API_TAG, "Error: $e")
                    e.printStackTrace()
                }
            }
        }
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        profileAdapter.onItemSelectedListener = {
            onItemSelectedListener?.invoke(it)
        }

        recyclerView.adapter = profileAdapter

        recyclerView.recycledViewPool.setMaxRecycledViews(0, 0)

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


    private suspend fun insertBusiness(business: BusinessEntity) {
        businessRepository.insertBusiness(business)
    }

    private suspend fun getBusiness(): BusinessEntity? {
        return businessRepository.getBusiness()
    }

    private suspend fun getMyProducts(): List<MyProductEntity> {
        return businessRepository.getMyProducts()
    }

    private suspend fun insertMyProduct(productEntity: MyProductEntity){
        businessRepository.insertMyProduct(productEntity)
    }

    suspend fun deleteMyProduct(productId: String) {
        businessRepository.deleteMyProduct(productId)
    }




    fun pickImage() {
        ImagePicker.pickMedia(requireActivity())
    }

    fun setBackgroundImage(imageUri: Uri) {
        this.imageUri = imageUri
        profileAdapter.setBackgroundImage(imageUri)
    }

    fun setBackgroundVideo(videoUri: Uri) {
        this.videoUri = videoUri
        profileAdapter.setBackgroundVideo(videoUri)
    }

    fun addCatalogImage(imageUri: Uri) {
        profileAdapter.setCatalogueImage(imageUri)
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun addCatalogue(catalogue: Catalogue) {

        catalogueList.add(catalogue)

        val imageFiles = arrayListOf<File>()
        for (image in catalogue.images) {

            Log.d(API_TAG, "image: $image")
            val imageFile = File(image)
            imageFiles.add(imageFile)
        }

        if (businessId != null){
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val createProductResponse = createProduct(
                        catalogue.name,
                        catalogue.description,
                        "none",
                        catalogue.price,
                        imageFiles
                    )

                    if (createProductResponse.isSuccessful) {
                        Log.d(API_TAG, "Product created successfully")
                        Log.d(API_TAG, "Response: ${createProductResponse.body()}")

                        catalogueList.remove(catalogue)

                        val product = createProductResponse.body()!!.data

                        val myProduct = MyProductEntity(
                            product._id,
                            product.__v,
                            product.catalogue,
                            product.createdAt,
                            product.description,
                            product.features,
                            product.images,
                            product.itemName,
                            product.owner,
                            product.price,
                            product.updatedAt
                        )

                        insertMyProduct(myProduct)
                    } else {
                        Log.d(API_TAG, "Error: ${createProductResponse.errorBody()?.string()}")
                    }
                }catch (e:HttpException){
                    Log.d(API_TAG, "Error: $e")
                    Log.d(API_TAG, "Error: ${e.response()?.errorBody()?.string()}")
                }catch (e:IOException){
                    Log.d(API_TAG, "Error: $e")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        profileAdapter.releasePlayer()
    }

    override fun onPause() {
        super.onPause()
        profileAdapter.pausePlayer()
    }


    // Function to update EditText fields with Catalogue data
    fun updateFields(catalogue: Catalogue) {
        // Set the text of the EditText fields to the corresponding values from the Catalogue object

    }

}