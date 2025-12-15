package com.uyscuti.social.circuit.ui

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.media3.common.util.UnstableApi
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.uyscuti.social.core.common.data.room.repository.DialogRepository
import com.uyscuti.social.core.common.data.room.repository.GroupDialogRepository
import com.uyscuti.social.core.common.data.room.repository.MessageRepository
import com.uyscuti.social.circuit.MainActivity
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.ActivityLoginBinding
import com.uyscuti.social.network.api.request.googlelogin.GoogleLoginRequest
import com.uyscuti.social.network.api.request.login.LoginRequest
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject


@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private var isPasswordVisible = false
    private var dialog: Dialog? = null

    private val PREFS_NAME = "LocalSettings" // Change this to a unique name for your app
    private lateinit var settings: SharedPreferences

    private lateinit var googleSignInClient: GoogleSignInClient

    private val RC_SIGN_IN = 123

    @Inject
    lateinit var retrofitInstance: RetrofitInstance

    @Inject
    lateinit var dialogRepository: DialogRepository

    @Inject
    lateinit var groupDialogRepository: GroupDialogRepository

    @Inject
    lateinit var messageRepository: MessageRepository


    @Inject
    lateinit var localStorage: LocalStorage


    private var chatIdList = ArrayList<String>()
    private var groupIdList = ArrayList<String>()

//    private lateinit var businessRepository: BusinessRepository
//    private lateinit var businessDatabase: BusinessDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

//        businessDatabase = BusinessDatabase.getInstance(this)
//        businessRepository = BusinessRepository(businessDatabase.businessDao())


        setUpTextListeners()

        setUpListeners()

        initGoogleSignIn()

//        getUserBussinessProfile()
    }


    private fun setUpListeners() {
        binding.registerLink.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.loginButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter your username and password", Toast.LENGTH_SHORT)
                    .show()
            } else {

                loginUser(password, username)

//                simulate()
//                val editor = settings.edit()
//                editor.putString("username", username)
//                editor.putBoolean("logged", true)
//                editor.apply()
            }
        }

        binding.googleBtn.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun setUpTextListeners() {
        binding.passwordEditText.addTextChangedListener { updateLoginButton() }
        binding.usernameEditText.addTextChangedListener {
            updateLoginButton()
        }
    }

    private fun initGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

//        googleSignInClient.signOut()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                Log.d("TAG", "Google Sign-In successful. ID Token: ${account?.idToken}")
                Log.d("TAG", "account id:  ${account?.id}")
//                Log.d("TAG", "account id:  $account")
                Log.d("TAG", "Account: ${account.account}")
                Log.d("TAG", "ServerAuthCode: ${account.serverAuthCode}")

                if (idToken != null) {
                    authenticateWithServer(idToken)
                }

//                Toast.makeText(this, "Login Success", Toast.LENGTH_SHORT).show()

            } catch (e: ApiException) {
                Log.e("TAG", "Google Sign-In failed. Error code: ${e.statusCode}")
//                Toast.makeText(applicationContext, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
            }

//            handleGoogleSignInResult(task)
        }
    }


    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }


    private fun authenticateWithServer(idToken: String) {
        val loginData = GoogleLoginRequest(
            idToken = idToken
        )
        CoroutineScope(Dispatchers.IO).launch {

            try {

                val response = retrofitInstance.apiService.googleLogin(loginData)

                if (response.isSuccessful) {
                    Log.d("GoogleLogin", "Google Login Success")
                    Log.d("GoogleLogin", "Google Login response : ${response.body()}")

                    val editor = settings.edit()
                    editor.putString("username", response.body()?.user?.username)
                    editor.putString("token", response.body()?.accessToken)
                    editor.putString("username", response.body()?.user?.username)
                    editor.putString("_id", response.body()?.user?._id)
                    editor.putString("email", response.body()?.user?.email)
                    editor.putString("profile_pic", response.body()?.user?.avatar?.url)
                    editor.putBoolean("logged", true)
                    editor.apply()

                    response.body()?.user?._id?.let { localStorage.setUserId(it) }
                    response.body()?.user?.username?.let { localStorage.setUser(it) }
                    response.body()?.accessToken?.let { localStorage.setToken(it) }
                    response.body()?.user?.username?.let { localStorage.setUserName(it) }

//                    runOnUiThread {
//                        Toast.makeText(this@RegisterActivity, "Google Login Success", Toast.LENGTH_SHORT)
//                            .show()
//                    }
                    showGoogleSuccess()
//                    openMainActivity()
                }

            } catch (e: Exception) {
                Log.d("GoogleErr", "Google Login Failed : ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun showGoogleSuccess() {
//        showToast("Google Login Successful")
        openMainActivity()

        fetchDialogs(
            onSuccess = {
                if (chatIdList.isNotEmpty()) {
                    chatIdList.forEach { dialogId ->
                        fetchMessages(dialogId)
                    }
                }

            },
            onFailure = {
//                openMainActivity()
            }
        )
    }


    @OptIn(DelicateCoroutinesApi::class)
    private fun loginUser(password: String, username: String) {

        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkInfo = connectivityManager.activeNetworkInfo
        val isConnected = networkInfo != null && networkInfo.isConnected

        if (!isConnected) {
            // No internet connection, notify the user
            Toast.makeText(
                this,
                "No internet connection. Please connect to the internet and try again.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        showLoadingDialog()


        GlobalScope.launch {
            val response = try {
                val data = LoginRequest(password, username)
                retrofitInstance.apiService.loginUsers(data)
            } catch (e: HttpException) {
                Log.d("RetrofitActivity", "Http Exception ${e.message}")
                runOnUiThread {
                    Toast.makeText(
                        this@LoginActivity,
                        "HTTP error. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                return@launch
            } catch (e: IOException) {
                Log.d("RetrofitActivity", "IOException ${e.message}")
                runOnUiThread {
                    Toast.makeText(
                        this@LoginActivity,
                        "Network error. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return@launch
            } finally {
                // Ensure the progress bar is hidden in case of an error
                withContext(Dispatchers.Main) {
                    dismissLoadingDialog()
                }
            }

            Log.d("Response", "- ${response.body().toString()}")
            Log.d("Response", "message ${response.message()}")
            Log.d("Response", "successful state ${response.isSuccessful}")

            if (response.isSuccessful) {
                val responseBody = response.body()

                if (responseBody?.data != null) {
                    val accessToken = responseBody.data.accessToken
                    LocalStorage.getInstance(this@LoginActivity).setToken(accessToken)

                    if (accessToken.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            Log.d("RetrofitActivity", "Login Success")
                            Log.d("RetrofitActivity", "Access Token: $accessToken")

                            val editor = settings.edit()
                            editor.putString("username", username)
                            editor.putString("token", responseBody.data.accessToken)
                            editor.putString("username", responseBody.data.user.username)
                            editor.putString("_id", responseBody.data.user._id)
                            editor.putString("email", responseBody.data.user.email)
                            editor.putString("profile_pic", responseBody.data.user.avatar.url)
                            editor.putBoolean("logged", true)
                            editor.apply()

                            localStorage.setUserId(responseBody.data.user._id)
                            localStorage.setUser(responseBody.data.user.username)
                            localStorage.setToken(responseBody.data.accessToken)
//                            responseBody.data.user
                            localStorage.setUserName(username)

                            dismissLoadingDialog()

                            showSuccess()
                            openMainActivity()
                        }
                    } else {
                        Log.d("RetrofitActivity", "Access Token is empty or null")
                    }
                } else {
                    Log.d("RetrofitActivity", "Response body or data is null")
                }
            } else {
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "Invalid Details", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    private fun updateLoginButton() {
        val username = binding.usernameEditText.text.toString()
        val password = binding.passwordEditText.text.toString()

        // Enable the login button if both fields are filled
        val isEnabled = username.isNotEmpty() && password.isNotEmpty()
        binding.loginButton.isEnabled = isEnabled
        if (isEnabled) {
            binding.loginButton.isEnabled = true
            binding.loginButton.setBackgroundColor(resources.getColor(R.color.bluejeans))
        } else {
            binding.loginButton.setBackgroundColor(resources.getColor(R.color.fade))
            binding.loginButton.isEnabled = false
        }
    }

    private fun simulate() {
        showLoadingDialog()


        // Simulate a delay (2 seconds) and then open the login activity
        Handler().postDelayed({
            dismissLoadingDialog()

            showSuccess()

            CoroutineScope(Dispatchers.Main).launch {


                delay(900)
                openMainActivity()
            }

        }, 3000)
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    private fun openMainActivity() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }


    private fun showLoadingDialog() {
        // Create a dialog with the loading layout
        dialog = Dialog(this)
        dialog?.setContentView(R.layout.loading_dialog)
        dialog?.setCancelable(false) // Prevent dismissing by tapping outside

        // Show the dialog
        dialog?.show()
    }

    private fun dismissLoadingDialog() {
        dialog?.dismiss()
    }

    private fun showSuccess() {
        fetchDialogs(
            onSuccess = {
                if (chatIdList.isNotEmpty()) {
                    chatIdList.forEach { dialogId ->
                        fetchMessages(dialogId)
                    }
                }
            },
            onFailure = {}
        )

        fetchGroups(
            onSuccess = {
                if (groupIdList.isNotEmpty()) {
                    groupIdList.forEach { dialogId ->
                        fetchGroupMessages(dialogId)
                    }
                }
            },
            onFailure = {}
        )
        showToast("Login Successful")
    }

    private fun fetchDialogs(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
//                Log.d("FetchedDialogs", "Fetching dialogs from the internet")
                dialogRepository.fetchAndInsertPersonalDialogs() // Initiate fetching and inserting dialogs
//                Log.d("FetchedDialogs", "Fetched dialogs from the internet successfully")

                val list = dialogRepository.dialogIds()
                chatIdList = list as ArrayList<String>
//                Log.d("FetchedDialogs", "The number of chats fetched : ${chatIdList.size}")

                // Call the success callback
                onSuccess.invoke()

            } catch (e: Exception) {
                e.printStackTrace()
                // Call the failure callback with the error message
                onFailure.invoke("Failed to fetch dialogs: ${e.message}")
            }
        }
    }


    private fun fetchMessages(chatId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
//                    Log.d("FetchedMessages", "Fetching messages from the internet")
                messageRepository.getMessagesWithMediaType(chatId)
//                    Log.d("FetchedMessages", "Fetched messages from the internet successfully")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun fetchGroups(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("FetchedGroups", "Fetching groups from the internet")
                groupDialogRepository.fetchAndInsertGroupDialogs() // Initiate fetching and inserting groups
                Log.d("FetchedGroups", "Fetched groups from the internet successfully")

                val list = groupDialogRepository.dialogIds()
                groupIdList = list as ArrayList<String>
                Log.d("FetchedGroups", "The number of groups fetched : ${chatIdList.size}")

                // Call the success callback
                onSuccess.invoke()
            } catch (e: Exception) {
                e.printStackTrace()
                // Call the failure callback with the error message
                onFailure.invoke("Failed to fetch groups: ${e.message}")
            }
        }

    }

    private fun fetchGroupMessages(groupId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("FetchedMessages", "Fetching group messages from the internet")
                messageRepository.getMessagesWithMediaType(groupId)
                Log.d("FetchedMessages", "Fetched group messages from the internet successfully")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

//    private fun getUserBussinessProfile() {
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val response = retrofitInstance.apiService.getBusinessProfile()
//
//                if (response.isSuccessful) {
//                    val businessProfile = response.body()!!
//
//                    Log.d("ApiService", "Business profile: $businessProfile")
//
//                    val editor = getSharedPreferences("ProfilePrefs", MODE_PRIVATE).edit()
//
//                    editor.putString("backgroundPhoto", businessProfile.backgroundPhoto.url)
//
//                    editor.apply()
//
//                    val video = businessProfile.backgroundVideo ?: null
//
//                    val _id = businessProfile._id
//                    val businessName = businessProfile.businessName
//                    val businessDescription = businessProfile.businessDescription
//                    val businessType = businessProfile.businessType
//                    val owner = businessProfile.owner
//                    val contact = businessProfile.contact
//                    val __v = businessProfile.__v
//                    val backgroundPhoto = businessProfile.backgroundPhoto.url
//                    val backgroundVideo = video?.url
//                    val videoThumbnail = video?.thumbnail
//                    val createdAt = businessProfile.createdAt
//                    val updatedAt = businessProfile.updatedAt
//                    val location = businessProfile.location
//
//
//                    val business =
//                        BusinessEntity(
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
//                                    LocationInformation(
//                                        location.businessLocation.locationInfo.latitude,
//                                        location.businessLocation.locationInfo.longitude,
//                                        location.businessLocation.locationInfo.accuracy,
//                                        location.businessLocation.locationInfo.range
//                                    )
//                                ),
//                                WalkingBillboard(
//                                    location.walkingBillboard.enabled,
//                                    LocationInformation(
//                                        location.walkingBillboard.liveLocationInfo.latitude,
//                                        location.walkingBillboard.liveLocationInfo.longitude,
//                                        location.walkingBillboard.liveLocationInfo.accuracy,
//                                        location.walkingBillboard.liveLocationInfo.range
//                                    )
//                                )
//                            ),
//                            owner,
//                            updatedAt
//                        )
//
//                    insertBusiness(business)
//
//                } else {
//                    Log.e("ApiService", "Failed to get business profile: ${response.message()}")
//                }
//            }catch (e: HttpException) {
//                Log.e("ApiService", "Failed to get business profile: ${e.message}", e)
//            }catch (e: Throwable) {
//                Log.e("ApiService", "Failed to get business profile: ${e.message}", e)
//            } finally {
//                withContext(Dispatchers.Main){
////                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
////                    startActivity(intent)
////                    finish()
//                    Log.d("ApiService", "Do nothing")
//                }
//            }
//        }
//    }
//
//    private suspend fun insertBusiness(business: BusinessEntity) {
//        businessRepository.insertBusiness(business)
//    }

}