package com.uyscuti.social.circuit.User_Interface.Log_In_And_Register

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.media3.common.util.UnstableApi
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.uyscuti.social.network.api.request.FaceBookLogIn.FacebookLoginRequest
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.GraphRequest
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.uyscuti.social.core.common.data.room.repository.DialogRepository
import com.uyscuti.social.core.common.data.room.repository.GroupDialogRepository
import com.uyscuti.social.core.common.data.room.repository.MessageRepository
import com.uyscuti.social.circuit.MainActivity
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.ActivityLoginBinding
import com.uyscuti.social.network.api.request.googlelogin.GoogleLoginRequest
import com.uyscuti.social.network.api.request.login.LoginRequest
import com.uyscuti.social.network.api.response.login.LoginResponse
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import org.json.JSONException

@AndroidEntryPoint

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private var isPasswordVisible = false

    private var dialog: Dialog? = null

    private val PREFS_NAME = "LocalSettings"

    private lateinit var settings: SharedPreferences

    private lateinit var googleSignInClient: GoogleSignInClient

    // Facebook login variables

    private lateinit var callbackManager: CallbackManager

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


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)



        FacebookSdk.sdkInitialize(applicationContext)

        AppEventsLogger.activateApp(application)

        binding = ActivityLoginBinding.inflate(layoutInflater)

        setContentView(binding.root)

        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        setUpTextListeners()

        setUpListeners()

        initGoogleSignIn()

        initFacebookSignIn()

    }

    private fun initFacebookSignIn() {

        callbackManager = CallbackManager.Factory.create()

        LoginManager.getInstance().registerCallback(callbackManager,

            object : FacebookCallback<LoginResult> {

                override fun onSuccess(loginResult: LoginResult) {

                    Log.d("FacebookLogin", "Login Success")

                    handleFacebookAccessToken(loginResult.accessToken)

                }

                override fun onCancel() {

                    Log.d("FacebookLogin", "Login Cancelled")

                    Toast.makeText(this@LoginActivity, "Facebook login cancelled", Toast.LENGTH_SHORT).show()

                }

                override fun onError(exception: FacebookException) {

                    Log.e("FacebookLogin", "Login Error: ${exception.message}")

                    Toast.makeText(this@LoginActivity, "Facebook login failed: ${exception.message}", Toast.LENGTH_SHORT).show()

                }

            }

        )

    }

    private fun handleFacebookAccessToken(token: AccessToken) {

        Log.d("FacebookLogin", "Access Token: ${token.token}")

        // Get user information from Facebook

        val request = GraphRequest.newMeRequest(token) { jsonObject, response ->

            try {

                val id = jsonObject?.getString("id")

                val name = jsonObject?.getString("name")

                val email = jsonObject?.getString("email")

                val pictureUrl = jsonObject?.getJSONObject("picture")

                    ?.getJSONObject("data")?.getString("url")

                Log.d("FacebookLogin", "User ID: $id")

                Log.d("FacebookLogin", "Name: $name")

                Log.d("FacebookLogin", "Email: $email")

                Log.d("FacebookLogin", "Picture URL: $pictureUrl")

                // Send token to your server for authentication

                authenticateWithServerFacebook(token.token, id, name, email, pictureUrl)

            } catch (e: JSONException) {

                Log.e("FacebookLogin", "JSON Error: ${e.message}")

                Toast.makeText(this@LoginActivity, "Failed to get user information", Toast.LENGTH_SHORT).show()

            }

        }

        val parameters = Bundle()

        parameters.putString("fields", "id,name,email,picture.type(large)")

        request.parameters = parameters

        request.executeAsync()

    }

    private fun showFacebookSuccess() {

        openMainActivity()



    }

    private fun setUpListeners() {

        // Register link

        binding.registerLink.setOnClickListener {

            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)

            startActivity(intent)

        }

        // Login button

        binding.loginButton.setOnClickListener {



            val username = binding.usernameEditText.text.toString().trim()

            val password = binding.passwordEditText.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {

                Toast.makeText(this, "Please enter your username and password", Toast.LENGTH_SHORT).show()

            } else {

                loginUser(password, username)

            }

        }

        // Google sign-in button

        binding.googleBtn.setOnClickListener {

            if (!binding.checkboxTermsConditions.isChecked) {

                Toast.makeText(this, "Please accept terms and conditions to continue", Toast.LENGTH_SHORT).show()

                return@setOnClickListener

            }

            signInWithGoogle()

        }

        // Facebook sign-in button

        binding.btnFacebook.setOnClickListener {

            if (!binding.checkboxTermsConditions.isChecked) {

                Toast.makeText(this, "Please accept terms and conditions to continue", Toast.LENGTH_SHORT).show()

                return@setOnClickListener

            }

            signInWithFacebook()

        }

        // Password visibility toggle

        binding.ivPasswordToggle.setOnClickListener {

            togglePasswordVisibility()

        }

        // Forgot password link

        binding.tvForgotPassword.setOnClickListener {

            val intent = Intent(this@LoginActivity, Forgot_Password::class.java)

            startActivity(intent)

        }

        // Terms and conditions checkbox

        binding.checkboxTermsConditions.setOnCheckedChangeListener { _, isChecked ->

            updateLoginButton()

            if (isChecked) {

                // You can add a dialog to show terms and conditions here

            }

        }

    }

    private fun signInWithFacebook() {

        LoginManager.getInstance().logInWithReadPermissions(

            this,

            listOf("email", "public_profile")

        )

    }

    private fun togglePasswordVisibility() {

        isPasswordVisible = !isPasswordVisible

        if (isPasswordVisible) {

            binding.passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

            binding.ivPasswordToggle.setImageResource(R.drawable.ic_eye_open)

        } else {

            binding.passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

            binding.ivPasswordToggle.setImageResource(R.drawable.ic_eye_close)

        }

        binding.passwordEditText.setSelection(binding.passwordEditText.text.length)

    }

    private fun setUpTextListeners() {

        binding.passwordEditText.addTextChangedListener { updateLoginButton() }

        binding.usernameEditText.addTextChangedListener { updateLoginButton() }

    }

    private fun initGoogleSignIn() {

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)

            .requestIdToken(getString(R.string.default_web_client_id))

            .requestEmail()

            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        // Handle Facebook login result

        callbackManager.onActivityResult(requestCode, resultCode, data)

        // Handle Google login result

        if (requestCode == RC_SIGN_IN) {

            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {

                val account = task.getResult(ApiException::class.java)

                val idToken = account.idToken

                Log.d("TAG", "Google Sign-In successful. ID Token: ${account?.idToken}")

                if (idToken != null) {

                    authenticateWithServer(idToken)

                }

            } catch (e: ApiException) {

                Log.e("TAG", "Google Sign-In failed. Error code: ${e.statusCode}")

            }

        }

    }

    private fun signInWithGoogle() {

        val signInIntent = googleSignInClient.signInIntent

        startActivityForResult(signInIntent, RC_SIGN_IN)

    }

    private fun showGoogleSuccess() {

        openMainActivity()


    }



    @OptIn(DelicateCoroutinesApi::class)

    private fun loginUser(password: String, username: String) {

        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkInfo = connectivityManager.activeNetworkInfo

        val isConnected = networkInfo != null && networkInfo.isConnected

        if (!isConnected) {

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

                            // ✅ SAVE USER DATA USING UserStorageHelper (persists forever)

                            val saveSuccess = UserStorageHelper.saveUserData(this@LoginActivity, responseBody)

                            if (saveSuccess) {

                                Log.d("LOGIN", "✅ User data saved with UserStorageHelper")

                            } else {

                                Log.e("LOGIN", "❌ Failed to save user data with UserStorageHelper")

                            }

                            // Keep existing SharedPreferences for backward compatibility

                            val editor = settings.edit()

                            editor.putString("username", username)

                            editor.putString("token", responseBody.data.accessToken)

                            editor.putString("_id", responseBody.data.user._id)

                            editor.putString("email", responseBody.data.user.email)

                            editor.putString("profile_pic", responseBody.data.user.avatar.url)

                            editor.putBoolean("logged", true)

                            editor.apply()

                            // Keep existing LocalStorage for backward compatibility

                            localStorage.setUserId(responseBody.data.user._id)

                            localStorage.setUser(responseBody.data.user.username)

                            localStorage.setToken(responseBody.data.accessToken)

                            localStorage.setUserName(username)

                            dismissLoadingDialog()

                           // showSuccess()

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

    private fun authenticateWithServer(idToken: String) {

        val loginData = GoogleLoginRequest(idToken = idToken)

        showLoadingDialog()

        CoroutineScope(Dispatchers.IO).launch {

            try {

                val response = retrofitInstance.apiService.googleLogin(loginData)

                if (response.isSuccessful) {

                    Log.d("GoogleLogin", "Google Login Success")

                    Log.d("GoogleLogin", "Google Login response: ${response.body()}")

                    val responseBody = response.body()

                    if (responseBody?.user != null) {

                        withContext(Dispatchers.Main) {

                            // ✅ SAVE USER DATA USING UserStorageHelper

                            val saveSuccess = UserStorageHelper.saveUserDataFromGoogle(this@LoginActivity, responseBody)

                            if (saveSuccess) {

                                Log.d("GOOGLE_LOGIN", "✅ User data saved with UserStorageHelper")

                            } else {

                                Log.e("GOOGLE_LOGIN", "❌ Failed to save user data with UserStorageHelper")

                            }

                            // Keep existing storage for backward compatibility

                            val editor = settings.edit()

                            editor.putString("username", responseBody.user!!.username)

                            editor.putString("token", responseBody.accessToken)

                            editor.putString("_id", responseBody.user!!._id)

                            editor.putString("email", responseBody.user!!.email)

                            editor.putString("profile_pic", responseBody.user!!.avatar?.url)

                            editor.putBoolean("logged", true)

                            editor.apply()

                            localStorage.setUserId(responseBody.user!!._id)

                            localStorage.setUser(responseBody.user!!.username)

                            responseBody.accessToken?.let { localStorage.setToken(it) }

                            localStorage.setUserName(responseBody.user!!.username)

                            dismissLoadingDialog()

                            Toast.makeText(this@LoginActivity, "Google Login Success", Toast.LENGTH_SHORT).show()

                            showGoogleSuccess()

                        }

                    }

                } else {

                    withContext(Dispatchers.Main) {

                        dismissLoadingDialog()

                        Toast.makeText(this@LoginActivity, "Google Login Failed", Toast.LENGTH_SHORT).show()

                    }

                }

            } catch (e: Exception) {

                Log.d("GoogleErr", "Google Login Failed: ${e.message}")

                withContext(Dispatchers.Main) {

                    dismissLoadingDialog()

                    Toast.makeText(this@LoginActivity, "Google Login Failed", Toast.LENGTH_SHORT).show()

                }

                e.printStackTrace()

            }

        }

    }

    private fun authenticateWithServerFacebook(

        accessToken: String,

        facebookId: String?,

        name: String?,

        email: String?,

        pictureUrl: String?

    ) {

        val loginData = FacebookLoginRequest(

            accessToken = accessToken,

            facebookId = facebookId,

            name = name,

            email = email,

            pictureUrl = pictureUrl

        )

        showLoadingDialog()

        CoroutineScope(Dispatchers.IO).launch {

            try {

                val response = retrofitInstance.apiService.facebookLogin(loginData)

                if (response.isSuccessful) {

                    val responseBody = response.body()

                    Log.d("FacebookLogin", "Facebook Login Success")

                    Log.d("FacebookLogin", "Facebook Login response: $responseBody")

                    if (responseBody != null) {

                        withContext(Dispatchers.Main) {

                            // ✅ SAVE USER DATA USING UserStorageHelper (persists forever)

                            val saveSuccess = UserStorageHelper.saveUserDataFromFacebook(this@LoginActivity, responseBody)

                            if (saveSuccess) {

                                Log.d("FACEBOOK_LOGIN", "✅ User data saved with UserStorageHelper")

                            } else {

                                Log.e("FACEBOOK_LOGIN", "❌ Failed to save user data with UserStorageHelper")

                            }

                            // Keep existing storage methods for backward compatibility

                            val editor = settings.edit()


                            Toast.makeText(this@LoginActivity, "Facebook Login Success", Toast.LENGTH_SHORT).show()

                            showFacebookSuccess()

                        }

                    } else {

                        withContext(Dispatchers.Main) {

                            dismissLoadingDialog()

                            Toast.makeText(

                                this@LoginActivity,

                                "Facebook login failed - no response data",

                                Toast.LENGTH_SHORT

                            ).show()

                        }

                    }

                } else {

                    withContext(Dispatchers.Main) {

                        dismissLoadingDialog()

                        Toast.makeText(

                            this@LoginActivity,

                            "Facebook login failed",

                            Toast.LENGTH_SHORT

                        ).show()

                    }

                }

            } catch (e: Exception) {

                Log.e("FacebookErr", "Facebook Login Failed: ${e.message}")

                e.printStackTrace()

                withContext(Dispatchers.Main) {

                    dismissLoadingDialog()

                    Toast.makeText(

                        this@LoginActivity,

                        "Network error during Facebook login",

                        Toast.LENGTH_SHORT

                    ).show()

                }

            }

        }

    }

    // ✅ USER STORAGE HELPER - Persistent data storage

    object UserStorageHelper {

        private const val PREF_NAME = "app_prefs"

        private const val KEY_USER_ID = "user_id"

        private const val KEY_USERNAME = "username"

        private const val KEY_EMAIL = "email"

        private const val KEY_AVATAR_URL = "avatar_url"

        private const val KEY_AVATAR_LOCAL_PATH = "avatar_local_path"

        private const val KEY_ACCESS_TOKEN = "access_token"

        private const val KEY_REFRESH_TOKEN = "refresh_token"

        private const val KEY_IS_EMAIL_VERIFIED = "is_email_verified"

        private const val KEY_USER_ROLE = "user_role"

        // ✅ Save data from regular login

        fun saveUserData(context: Context, loginResponse: LoginResponse): Boolean {

            return try {

                val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

                val user = loginResponse.data.user

                sharedPrefs.edit().apply {

                    putString(KEY_USER_ID, user._id)

                    putString(KEY_USERNAME, user.username)

                    putString(KEY_EMAIL, user.email)

                    putString(KEY_AVATAR_URL, user.avatar.url)

                    putString(KEY_AVATAR_LOCAL_PATH, user.avatar.localPath)

                    putString(KEY_ACCESS_TOKEN, loginResponse.data.accessToken)

                    putString(KEY_REFRESH_TOKEN, loginResponse.data.refreshToken)

                    putBoolean(KEY_IS_EMAIL_VERIFIED, user.isEmailVerified)

                    putString(KEY_USER_ROLE, user.role)

                    apply()

                }

                Log.d("USER_STORAGE", "User data saved successfully: ${user.username}")

                true

            } catch (e: Exception) {

                Log.e("USER_STORAGE", "Error saving user data: ${e.message}", e)

                false

            }

        }

        // ✅ Save data from Google login

        fun saveUserDataFromGoogle(context: Context, googleResponse: Any): Boolean {

            return try {

                val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

                // Use reflection to get fields (adjust based on your GoogleLoginResponse structure)

                val userField = googleResponse.javaClass.getDeclaredField("user")

                userField.isAccessible = true

                val user = userField.get(googleResponse)

                val accessTokenField = googleResponse.javaClass.getDeclaredField("accessToken")

                accessTokenField.isAccessible = true

                val accessToken = accessTokenField.get(googleResponse) as? String

                val idField = user?.javaClass?.getDeclaredField("_id")

                idField?.isAccessible = true

                val userId = idField?.get(user) as? String

                val usernameField = user?.javaClass?.getDeclaredField("username")

                usernameField?.isAccessible = true

                val username = usernameField?.get(user) as? String

                val emailField = user?.javaClass?.getDeclaredField("email")

                emailField?.isAccessible = true

                val email = emailField?.get(user) as? String

                val avatarField = user?.javaClass?.getDeclaredField("avatar")

                avatarField?.isAccessible = true

                val avatar = avatarField?.get(user)

                val avatarUrlField = avatar?.javaClass?.getDeclaredField("url")

                avatarUrlField?.isAccessible = true

                val avatarUrl = avatarUrlField?.get(avatar) as? String

                sharedPrefs.edit().apply {

                    putString(KEY_USER_ID, userId ?: "")

                    putString(KEY_USERNAME, username ?: "")

                    putString(KEY_EMAIL, email ?: "")

                    putString(KEY_AVATAR_URL, avatarUrl ?: "")

                    putString(KEY_ACCESS_TOKEN, accessToken ?: "")

                    putBoolean(KEY_IS_EMAIL_VERIFIED, true)

                    putString(KEY_USER_ROLE, "user")

                    apply()

                }

                Log.d("USER_STORAGE", "Google user data saved successfully: $username")

                true

            } catch (e: Exception) {

                Log.e("USER_STORAGE", "Error saving Google user data: ${e.message}", e)

                false

            }

        }

        // ✅ Save data from Facebook login

        fun saveUserDataFromFacebook(context: Context, facebookResponse: Any): Boolean {

            return try {

                val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

                // Use reflection to get fields (adjust based on your FacebookLoginResponse structure)

                val userField = facebookResponse.javaClass.getDeclaredField("user")

                userField.isAccessible = true

                val user = userField.get(facebookResponse)

                val accessTokenField = facebookResponse.javaClass.getDeclaredField("accessToken")

                accessTokenField.isAccessible = true

                val accessToken = accessTokenField.get(facebookResponse) as? String

                val idField = user?.javaClass?.getDeclaredField("_id")

                idField?.isAccessible = true

                val userId = idField?.get(user) as? String

                val usernameField = user?.javaClass?.getDeclaredField("username")

                usernameField?.isAccessible = true

                val username = usernameField?.get(user) as? String

                val emailField = user?.javaClass?.getDeclaredField("email")

                emailField?.isAccessible = true

                val email = emailField?.get(user) as? String

                val avatarField = user?.javaClass?.getDeclaredField("avatar")

                avatarField?.isAccessible = true

                val avatar = avatarField?.get(user)

                val avatarUrlField = avatar?.javaClass?.getDeclaredField("url")

                avatarUrlField?.isAccessible = true

                val avatarUrl = avatarUrlField?.get(avatar) as? String

                sharedPrefs.edit().apply {

                    putString(KEY_USER_ID, userId ?: "")

                    putString(KEY_USERNAME, username ?: "")

                    putString(KEY_EMAIL, email ?: "")

                    putString(KEY_AVATAR_URL, avatarUrl ?: "")

                    putString(KEY_ACCESS_TOKEN, accessToken ?: "")

                    putBoolean(KEY_IS_EMAIL_VERIFIED, true)

                    putString(KEY_USER_ROLE, "user")

                    apply()

                }

                Log.d("USER_STORAGE", "Facebook user data saved successfully: $username")

                true

            } catch (e: Exception) {

                Log.e("USER_STORAGE", "Error saving Facebook user data: ${e.message}", e)

                false

            }

        }

        fun getUserId(context: Context): String {

            val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

            return sharedPrefs.getString(KEY_USER_ID, "") ?: ""

        }

        fun getUsername(context: Context): String {

            val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

            return sharedPrefs.getString(KEY_USERNAME, "") ?: ""

        }

        fun getEmail(context: Context): String {

            val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

            return sharedPrefs.getString(KEY_EMAIL, "") ?: ""

        }

        fun getAvatarUrl(context: Context): String {

            val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

            return sharedPrefs.getString(KEY_AVATAR_URL, "") ?: ""

        }

        fun getAccessToken(context: Context): String {

            val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

            return sharedPrefs.getString(KEY_ACCESS_TOKEN, "") ?: ""

        }








    }



    @androidx.annotation.OptIn(UnstableApi::class)
    private fun openMainActivity() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun updateLoginButton() {
        val username = binding.usernameEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()
        val isTermsAccepted = binding.checkboxTermsConditions.isChecked

        binding.loginButton.isEnabled = username.isNotEmpty() &&
                password.isNotEmpty() &&
                isTermsAccepted
    }

    private fun showLoadingDialog() {
        if (dialog == null) {
            dialog = Dialog(this)
            dialog?.setContentView(R.layout.loading_dialog)
            dialog?.setCancelable(false)
            dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
        dialog?.show()
    }

    private fun dismissLoadingDialog() {
        dialog?.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        dialog?.dismiss()
        dialog = null
    }
}
