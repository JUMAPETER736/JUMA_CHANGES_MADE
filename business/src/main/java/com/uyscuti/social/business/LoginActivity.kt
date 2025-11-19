package com.uyscuti.social.business

//import android.content.Intent
//import android.content.SharedPreferences
//import android.os.Bundle
//import android.util.Log
//import android.view.View
//import android.view.ViewTreeObserver
//import android.widget.Toast
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import com.example.mylibrary.databinding.ActivityLoginBinding
////import com.example.mylibrary.retro.RetrofitClient
//import com.uyscuti.social.network.api.request.business.create.BusinessLocation
//import com.uyscuti.social.network.api.request.business.create.Contact
//import com.uyscut.network.api.request.business.create.Location
//import com.uyscut.network.api.request.business.create.LocationInformation
//import com.uyscut.network.api.request.business.create.WalkingBillboard
//import com.uyscuti.social.business.room.database.BusinessDatabase
//import com.uyscuti.social.business.room.entity.BusinessCatalogueEntity
//import com.uyscuti.social.business.room.entity.BusinessEntity
//import com.uyscuti.social.business.room.entity.MyProductEntity
//import com.uyscuti.social.business.room.repository.BusinessRepository
//
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import retrofit2.HttpException



//class LoginActivity : AppCompatActivity() {
//    private lateinit var binding: ActivityLoginBinding
////    private val apiService = RetrofitClient.instance
//    private lateinit var sharedPreferences: SharedPreferences
//
//    private lateinit var businessDatabase: BusinessDatabase
//    private lateinit var businessRepository: BusinessRepository
//
//    private var viewIsReady = false
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//
//        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE)
//
//        binding = ActivityLoginBinding.inflate(layoutInflater)
//        enableEdgeToEdge()
//        setContentView(binding.root)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//
//        businessDatabase = BusinessDatabase.getInstance(this)
//        businessRepository = BusinessRepository(businessDatabase.businessDao())
//
//        installSplashScreen()
//
//
//        // Set up an OnPreDrawListener to the root view.
//        val content: View = findViewById(android.R.id.content)
//        content.viewTreeObserver.addOnPreDrawListener(
//            object : ViewTreeObserver.OnPreDrawListener {
//                override fun onPreDraw(): Boolean {
//                    // Check whether the initial data is ready.
//                    return if (viewIsReady) {
//                        // The content is ready. Start drawing.
//                        content.viewTreeObserver.removeOnPreDrawListener(this)
//                        true
//                    } else {
//                        // The content isn't ready. Suspend.
//                        false
//                    }
//                }
//            }
//        )
//
//
//        val token = RetrofitClient.sharedPreferences.getString("token", null)
//
//        Log.d("ApiService", "Token: $token")
//
//        if (token != null) {
//            CoroutineScope(Dispatchers.IO).launch {
//                try {
//                    val response = apiService.getBusinessProfile()
//
//                    if (response.isSuccessful) {
//                        val businessProfile = response.body()!!
//
//                        Log.d("ApiService", "Business profile: $businessProfile")
//
//                        val editor = getSharedPreferences("ProfilePrefs", MODE_PRIVATE).edit()
//
//                        editor.putString("backgroundPhoto", businessProfile.backgroundPhoto.url)
//
//                        editor.apply()
//
//                        val video = businessProfile.backgroundVideo ?: null
//
//                        val _id = businessProfile._id
//                        val businessName = businessProfile.businessName
//                        val businessDescription = businessProfile.businessDescription
//                        val businessType = businessProfile.businessType
//                        val owner = businessProfile.owner
//                        val contact = businessProfile.contact
//                        val __v = businessProfile.__v
//                        val backgroundPhoto = businessProfile.backgroundPhoto.url
//                        val backgroundVideo = video?.url
//                        val videoThumbnail = video?.thumbnail
//                        val createdAt = businessProfile.createdAt
//                        val updatedAt = businessProfile.updatedAt
//                        val location = businessProfile.location
//
//
//                        val business = BusinessEntity(
//                            _id,
//                            __v,
//                            backgroundPhoto,
//                            backgroundVideo,
//                            videoThumbnail,
//                            listOf<BusinessCatalogueEntity>(),
//                            businessDescription,
//                            businessName,
//                            businessType,
//                            Contact(
//                                contact.address,
//                                contact.email,
//                                contact.phoneNumber,
//                                contact.website
//                            ),
//                            createdAt,
//                            Location(
//                                BusinessLocation(
//                                    location.businessLocation.enabled,
//                                    LocationInformation(location.businessLocation.locationInfo.latitude, location.businessLocation.locationInfo.longitude,location.businessLocation.locationInfo.accuracy,location.businessLocation.locationInfo.range)
//                                ),
//                                WalkingBillboard(
//                                    location.walkingBillboard.enabled,
//                                    LocationInformation(location.walkingBillboard.liveLocationInfo.latitude, location.walkingBillboard.liveLocationInfo.longitude,location.walkingBillboard.liveLocationInfo.accuracy,location.walkingBillboard.liveLocationInfo.range)
//                                )
//                            ),
//                            owner,
//                            updatedAt
//                        )
//
//                        insertBusiness(business)
//
//                    } else {
//                        Log.e("ApiService", "Failed to get business profile: ${response.message()}")
//                    }
//                }catch (e: HttpException) {
//                    Log.e("ApiService", "Failed to get business profile: ${e.message}", e)
//                }catch (e: Throwable) {
//                    Log.e("ApiService", "Failed to get business profile: ${e.message}", e)
//                } finally {
//                    withContext(Dispatchers.Main){
//                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
//                        startActivity(intent)
//                        finish()
//                    }
//                }
//            }
//
//
//        } else{
//            viewIsReady = true
//        }
//
//        binding.buttonLogin.setOnClickListener {
//            val username = binding.editTextUsername.text.toString()
//            val password = binding.editTextPassword.text.toString()
//
//            CoroutineScope(Dispatchers.IO).launch {
//                try {
//                    val response = apiService.login(username, password)
//                    // Handle successful login, response contains token
//
//                    val token = response.data.accessToken
//
//                    // Save token to SharedPreferences
//                    RetrofitClient.sharedPreferences.edit().putString("token", token).apply()
//
//                    Log.d("Login", "Login successful with token: ${response.data.accessToken}")
//                    Log.d("Login", "Login successful with message: ${response.message}")
//                    Log.d("Login", "Login successful with user: ${response.data.user}")
//                    Log.d("Login", "Login successful with code: ${response.statusCode}")
//                    withContext(Dispatchers.Main) {
//                        Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()
//                    }
//
//                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
//                    startActivity(intent)
//                } catch (e: HttpException) {
//
//                    Log.e("Login", "Login failed: ${e.message}", e)
//                    e.printStackTrace()
//                    // Handle error response (e.g., incorrect credentials)
//                    withContext(Dispatchers.Main) {
//                        // Show error message to user
//                        // For example:
//                         Toast.makeText(this@LoginActivity, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
//                    }
//                } catch (e: Throwable) {
//
//                    Log.e("Login", "Login failed: ${e.message}",e)
//                    e.printStackTrace()
//                    // Handle network or unexpected errors
//                    withContext(Dispatchers.Main) {
//                        // Show error message to user
//                        Toast.makeText(this@LoginActivity, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
//
//                    }
//                }
//            }
//
//        }
//
//        binding.SignIn.setOnClickListener {
//            val intent = Intent(this, RegisterActivity::class.java)
//            startActivity(intent)
//        }
//    }
//
//
//    private suspend fun insertBusiness(business: BusinessEntity) {
//        businessRepository.insertBusiness(business)
//    }
//
//    private suspend fun getBusiness(): BusinessEntity? {
//        return businessRepository.getBusiness()
//    }
//
//    private suspend fun insertMyProduct(productEntity: MyProductEntity){
//        businessRepository.insertMyProduct(productEntity)
//    }
//}