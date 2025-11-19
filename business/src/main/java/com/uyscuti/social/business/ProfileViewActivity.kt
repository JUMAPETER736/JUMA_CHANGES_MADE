package com.uyscuti.social.business

import android.net.http.HttpException
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresExtension
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.business.adapter.MediaPagerAdapter
import com.uyscuti.social.business.adapter.ProfileViewAdapter
import com.uyscuti.social.business.model.Catalogue
//import com.example.mylibrary.retro.RetrofitClient
import com.uyscuti.social.network.api.request.business.users.GetBusinessProfileById
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.AndroidEntryPoint

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

//import retrofit2.HttpException

@AndroidEntryPoint
class ProfileViewActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var profileViewAdapter: ProfileViewAdapter
    private lateinit var mediaViewAdapter: MediaPagerAdapter

//    private val apiService = RetrofitClient.instance
    @Inject
    lateinit var retrofitInterface: RetrofitInstance

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_view)

        val userId = intent.getStringExtra("userId")
        val userName = intent.getStringExtra("userName")
        val avatar = intent.getStringExtra( "userAvatar")

        if (userId == null) {
            // Handle the case where userId is null
            finish()
            return
        }

        var userBusinessProfile: GetBusinessProfileById? = null
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Fetch business profile
                val profileResponse = retrofitInterface.apiService.getUserBusinessProfile(userId)

                if (profileResponse.isSuccessful) {
                    val businessProfile = profileResponse.body()
                    if (businessProfile != null) {
                        // Update UI with the business profile
                        withContext(Dispatchers.Main) {
                            userBusinessProfile = businessProfile
                            profileViewAdapter.setBusinessProfile(userBusinessProfile!!)
                        }
                        Log.d("ApiService", "Business profile: $businessProfile")
                    } else {
                        Log.e("ApiService", "No business profile found")
                        // Show toast message on main thread
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@ProfileViewActivity,
                                "No business profile found",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    // Fetch business catalogue
                    val catalogueResponse = retrofitInterface.apiService.getUserBusinessCatalogue(userId)

                    if (catalogueResponse.isSuccessful) {
                        val businessCatalogue = catalogueResponse.body()?.data
                        val catalogueList = arrayListOf<Catalogue>()

                        businessCatalogue?.let {
                            if (it.products.isNotEmpty()) {
                                for (product in it.products) {
                                    val catalogue = Catalogue(
                                        product._id,
                                        product.itemName,
                                        product.description,
                                        product.price,
                                        product.images
                                    )
                                    catalogueList.add(catalogue)
                                }
                            }
                        }

                        // Update UI with catalogue list
                        withContext(Dispatchers.Main) {
                            if (catalogueList.isNotEmpty()) {
                                profileViewAdapter.setNameAndAvatar(userName ?: "", avatar ?: "")
                                profileViewAdapter.setCatalogueList(catalogueList)
                            }
                        }
                    } else {
                        Log.e("ApiService", "Failed to get business catalogue: ${catalogueResponse.message()}")
                        // Show toast message on main thread
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@ProfileViewActivity,
                                "Failed to get business catalogue",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Log.e("ApiService", "Failed to get business profile: ${profileResponse.message()}")
                    // Show toast message on main thread
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ProfileViewActivity,
                            "Failed to get business profile",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: HttpException) {
                Log.e("ApiService", "Failed to fetch data: ${e.message}", e)
                // Show toast message on main thread
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ProfileViewActivity,
                        "Failed to fetch data: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Throwable) {
                Log.e("ApiService", "Network error: ${e.message}", e)
                // Show toast message on main thread
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ProfileViewActivity,
                        "Network error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }


//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val response = retrofitInterface.apiService.getUserBusinessProfile(userId)
//
//                if (response.isSuccessful) {
//                    val businessProfile = response.body()
//                    if (businessProfile != null) {
//                        // Update UI with the business profile
//                        userBusinessProfile = businessProfile
//                    }
//
//                    Log.d("ApiService", "Business profile: $businessProfile")
//                } else {
//                    Log.e("ApiService", "Failed to get business profile")
//                }
//            } catch (e: HttpException) {
//                Log.e("ApiService", "Failed to get other users: ${e.message}", e)
//            } catch (e: Throwable) {
//                Log.e("ApiService", "Failed to get other users: ${e.message}", e)
//            } finally {
//                withContext(Dispatchers.Main) {
//                    // Initialize RecyclerView
//                    recyclerView = findViewById(R.id.image_recycler_view)
//                    recyclerView.layoutManager = LinearLayoutManager(
//                        this@ProfileViewActivity,
//                        LinearLayoutManager.VERTICAL,
//                        false
//                    )
//
//                    // Initialize and set the adapter
//                    profileViewAdapter = ProfileViewAdapter(this@ProfileViewActivity)
//                    profileViewAdapter.setBusinessProfile(userBusinessProfile!!)
//
//
//                    recyclerView.adapter = profileViewAdapter
//
//
//
//                    if (userBusinessProfile != null) {
//                        profileViewAdapter.setBusinessProfile(userBusinessProfile!!)
//                    }
//
//                    val catalogueList = arrayListOf<Catalogue>()
//
//                    val userBusinessCatalogue: GetCatalogueByUserId? = null
//                    CoroutineScope(Dispatchers.IO).launch {
//
//                        try {
//
//                            val response = retrofitInterface.apiService.getUserBusinessCatalogue(userId)
//
//
//                            if (response.isSuccessful) {
//                                val businessCatalogue = response.body()!!.data
//                                if (businessCatalogue.products.isNotEmpty()) {
//                                    for (product in businessCatalogue.products) {
//                                        val catalogue = Catalogue(product._id,product.itemName,product.description,product.price,product.images)
//                                        catalogueList.add(catalogue)
//                                    }
//                                }
//                            }
//
//                        } catch (e: HttpException) {
//                            Log.e("ApiService", "Failed to get business catalogue: ${e.message}", e)
//                        } catch (e: Throwable) {
//                            Log.e(
//                                "ApiService",
//                                "Failed to get  business catalogue: ${e.message}",
//                                e
//                            )
//                        } finally {
//                            withContext(Dispatchers.Main) {
//                                // Initialize RecyclerView
//
//                               if (catalogueList.isNotEmpty()){
//
//                                   profileViewAdapter.setNameAndAvatar(userName?:"",avatar?:"")
//                                   profileViewAdapter.setCatalogueList(catalogueList)
//                               }
//                            }
//
//                        }
//                    }
//                }
//            }
//
//        }
    }


}