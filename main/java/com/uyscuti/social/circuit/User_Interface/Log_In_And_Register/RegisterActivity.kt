package com.uyscuti.social.circuit.User_Interface.Log_In_And_Register

import android.app.Dialog
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresExtension
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.ActivityRegisterBinding
import com.uyscuti.social.network.api.request.googlelogin.GoogleLoginRequest
import com.uyscuti.social.network.api.request.register.RegisterRequest
import com.uyscuti.social.network.api.request.FaceBookLogIn.FacebookLoginRequest
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.GraphRequest
import org.json.JSONException

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager
    private val RC_SIGN_IN = 123

    private var dialog: Dialog? = null

    @Inject
    lateinit var retrofitInstance: RetrofitInstance

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Facebook SDK
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(application)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        setupTextWatchers()
        initGoogleSignIn()
        initFacebookSignIn()
    }

    private fun initFacebookSignIn() {
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    Log.d("FacebookRegister", "Registration Success")
                    handleFacebookAccessToken(loginResult.accessToken)
                }

                override fun onCancel() {
                    Log.d("FacebookRegister", "Registration Cancelled")
                    Toast.makeText(this@RegisterActivity, "Facebook registration cancelled", Toast.LENGTH_SHORT).show()
                }

                override fun onError(exception: FacebookException) {
                    Log.e("FacebookRegister", "Registration Error: ${exception.message}")
                    Toast.makeText(this@RegisterActivity, "Facebook registration failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d("FacebookRegister", "Access Token: ${token.token}")

        showLoadingDialog()
        val request = GraphRequest.newMeRequest(token) { jsonObject, response ->
            try {
                val id = jsonObject?.getString("id")
                val name = jsonObject?.getString("name")
                val email = jsonObject?.getString("email")
                val pictureUrl = jsonObject?.getJSONObject("picture")
                    ?.getJSONObject("data")?.getString("url")

                Log.d("FacebookRegister", "User ID: $id")
                Log.d("FacebookRegister", "Name: $name")
                Log.d("FacebookRegister", "Email: $email")
                Log.d("FacebookRegister", "Picture URL: $pictureUrl")

                registerWithFacebook(token.token, id, name, email, pictureUrl)
            } catch (e: JSONException) {
                Log.e("FacebookRegister", "JSON Error: ${e.message}")
                runOnUiThread {
                    dismissLoadingDialog()
                    Toast.makeText(this@RegisterActivity, "Failed to get user information", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val parameters = Bundle()
        parameters.putString("fields", "id,name,email,picture.type(large)")
        request.parameters = parameters
        request.executeAsync()
    }

    private fun registerWithFacebook(
        accessToken: String,
        facebookId: String?,
        name: String?,
        email: String?,
        pictureUrl: String?
    ) {
        val registerData = FacebookLoginRequest(
            accessToken = accessToken,
            facebookId = facebookId,
            name = name,
            email = email,
            pictureUrl = pictureUrl
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = retrofitInstance.apiService.facebookLogin(registerData)
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d("FacebookRegister", "Facebook Registration Success")
                    Log.d("FacebookRegister", "Response: $responseBody")

                    withContext(Dispatchers.Main) {
                        dismissLoadingDialog()
                        Toast.makeText(this@RegisterActivity, "Registration Successful! Please log in.", Toast.LENGTH_SHORT).show()
                        navigateToLogin()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        dismissLoadingDialog()
                        Toast.makeText(this@RegisterActivity, "Facebook registration failed. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("FacebookRegisterErr", "Registration Failed: ${e.message}")
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    dismissLoadingDialog()
                    Toast.makeText(this@RegisterActivity, "Network error during registration", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private fun setupListeners() {
        // Register button
        binding.registerButton.setOnClickListener {
            if (validateForm()) {
                val fullName = binding.signUpFullName.text.toString().trim()
                val username = binding.signUpUsername.text.toString().trim()
                val email = binding.email.text.toString().trim()
                val password = binding.signUpPassword.text.toString()

                registerUser(fullName, username, email, password)
            }
        }

        // Navigate to login
        binding.LogInText.setOnClickListener {
            navigateToLogin()
        }

        // Google sign-up button
        binding.btnSignInWithGoogle.setOnClickListener {
            registerWithGoogle()
        }

        // Facebook sign-up button
        binding.btnSignInWithFacebook.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(
                this,
                listOf("email", "public_profile")
            )
        }

        // Password visibility toggles
        binding.ivPasswordToggle.setOnClickListener {
            togglePasswordVisibility()
        }

        binding.ivConfirmPasswordToggle.setOnClickListener {
            toggleConfirmPasswordVisibility()
        }
    }

    private fun setupTextWatchers() {
        binding.signUpFullName.addTextChangedListener { updateRegisterButton() }
        binding.signUpUsername.addTextChangedListener { updateRegisterButton() }
        binding.email.addTextChangedListener { updateRegisterButton() }
        binding.signUpPassword.addTextChangedListener { updateRegisterButton() }
        binding.confirmPass.addTextChangedListener { updateRegisterButton() }
    }

    private fun validateForm(): Boolean {
        val fullName = binding.signUpFullName.text.toString().trim()
        val username = binding.signUpUsername.text.toString().trim()
        val email = binding.email.text.toString().trim()
        val password = binding.signUpPassword.text.toString()
        val confirmPassword = binding.confirmPass.text.toString()

        when {
            fullName.isEmpty() -> {
                binding.signUpFullName.error = "Full name is required"
                binding.signUpFullName.requestFocus()
                return false
            }
            fullName.length < 2 -> {
                binding.signUpFullName.error = "Full name must be at least 2 characters"
                binding.signUpFullName.requestFocus()
                return false
            }
            username.isEmpty() -> {
                binding.signUpUsername.error = "Username is required"
                binding.signUpUsername.requestFocus()
                return false
            }
            username.length < 3 -> {
                binding.signUpUsername.error = "Username must be at least 3 characters"
                binding.signUpUsername.requestFocus()
                return false
            }
            username.contains(" ") -> {
                binding.signUpUsername.error = "Username cannot contain spaces"
                binding.signUpUsername.requestFocus()
                return false
            }
            !username.matches(Regex("^[a-zA-Z0-9_]+$")) -> {
                binding.signUpUsername.error = "Username can only contain letters, numbers, and underscores"
                binding.signUpUsername.requestFocus()
                return false
            }
            email.isEmpty() -> {
                binding.email.error = "Email is required"
                binding.email.requestFocus()
                return false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.email.error = "Please enter a valid email address"
                binding.email.requestFocus()
                return false
            }
            password.isEmpty() -> {
                binding.signUpPassword.error = "Password is required"
                binding.signUpPassword.requestFocus()
                return false
            }
            password.length < 6 -> {
                binding.signUpPassword.error = "Password must be at least 6 characters"
                binding.signUpPassword.requestFocus()
                return false
            }
            confirmPassword.isEmpty() -> {
                binding.confirmPass.error = "Please confirm your password"
                binding.confirmPass.requestFocus()
                return false
            }
            password != confirmPassword -> {
                binding.confirmPass.error = "Passwords do not match"
                binding.confirmPass.requestFocus()
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return false
            }
        }

        return true
    }

    private fun updateRegisterButton() {
        val fullName = binding.signUpFullName.text.toString().trim()
        val username = binding.signUpUsername.text.toString().trim()
        val email = binding.email.text.toString().trim()
        val password = binding.signUpPassword.text.toString()
        val confirmPassword = binding.confirmPass.text.toString()

        val isEnabled = fullName.isNotEmpty() && username.isNotEmpty() && email.isNotEmpty() &&
                password.isNotEmpty() && confirmPassword.isNotEmpty()

        binding.registerButton.isEnabled = isEnabled
        if (isEnabled) {
            binding.registerButton.setBackgroundColor(resources.getColor(R.color.bluejeans))
        } else {
            binding.registerButton.setBackgroundColor(resources.getColor(R.color.fade))
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private fun registerUser(fullName: String, username: String, email: String, password: String) {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        val isConnected = networkInfo != null && networkInfo.isConnected

        if (!isConnected) {
            Toast.makeText(this, "No internet connection. Please connect to the internet and try again.", Toast.LENGTH_SHORT).show()
            return
        }

        // Additional validation for username
        if (username.isEmpty() || username.length < 3) {
            Toast.makeText(this, "Username must be at least 3 characters long.", Toast.LENGTH_SHORT).show()
            return
        }

        if (username.contains(" ")) {
            Toast.makeText(this, "Username cannot contain spaces.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!username.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            Toast.makeText(this, "Username can only contain letters, numbers, and underscores.", Toast.LENGTH_SHORT).show()
            return
        }

        showLoadingDialog()
        binding.registerButton.visibility = View.INVISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            val response = try {
                val data = RegisterRequest(email, password, username)
                Log.d("RegisterActivity", "Registration data: $data")
                retrofitInstance.apiService.registerUsers(data)
            } catch (e: HttpException) {
                Log.e("RegisterActivity", "HTTP error: ${e.message}")
                withContext(Dispatchers.Main) {
                    dismissLoadingDialog()
                    binding.registerButton.visibility = View.VISIBLE
                    Toast.makeText(this@RegisterActivity, "HTTP error. Please try again.", Toast.LENGTH_SHORT).show()
                }
                return@launch
            } catch (e: IOException) {
                Log.e("RegisterActivity", "Network error: ${e.message}")
                withContext(Dispatchers.Main) {
                    dismissLoadingDialog()
                    binding.registerButton.visibility = View.VISIBLE
                    Toast.makeText(this@RegisterActivity, "Network error. Please try again.", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            withContext(Dispatchers.Main) {
                dismissLoadingDialog()
                binding.registerButton.visibility = View.VISIBLE
            }

            if (response.isSuccessful && response.body() != null) {
                withContext(Dispatchers.Main) {
                    showSuccess()
                    navigateToLogin()
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RegisterActivity, "User already exists. Please try with different credentials.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        if (isPasswordVisible) {
            binding.signUpPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.ivPasswordToggle.setImageResource(R.drawable.ic_eye_open)
        } else {
            binding.signUpPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.ivPasswordToggle.setImageResource(R.drawable.ic_eye_close)
        }
        binding.signUpPassword.setSelection(binding.signUpPassword.text.length)
    }

    private fun toggleConfirmPasswordVisibility() {
        isConfirmPasswordVisible = !isConfirmPasswordVisible
        if (isConfirmPasswordVisible) {
            binding.confirmPass.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.ivConfirmPasswordToggle.setImageResource(R.drawable.ic_eye_open)
        } else {
            binding.confirmPass.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.ivConfirmPasswordToggle.setImageResource(R.drawable.ic_eye_close)
        }
        binding.confirmPass.setSelection(binding.confirmPass.text.length)
    }

    private fun initGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun registerWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Handle Facebook registration result
        callbackManager.onActivityResult(requestCode, resultCode, data)

        // Handle Google registration result
        if (requestCode == RC_SIGN_IN) {
            showLoadingDialog()
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                Log.d("GoogleRegister", "Google Sign-In successful. ID Token: ${account?.idToken}")

                if (idToken != null) {
                    completeGoogleRegistration(idToken)
                }
            } catch (e: ApiException) {
                Log.e("GoogleRegister", "Google Sign-In failed. Error code: ${e.statusCode}")
                dismissLoadingDialog()
                Toast.makeText(this, "Google registration failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun completeGoogleRegistration(idToken: String) {
        val registerData = GoogleLoginRequest(idToken = idToken)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = retrofitInstance.apiService.googleLogin(registerData)

                if (response.isSuccessful) {
                    Log.d("GoogleRegister", "Google Registration Success")
                    Log.d("GoogleRegister", "Response: ${response.body()}")

                    withContext(Dispatchers.Main) {
                        dismissLoadingDialog()
                        Toast.makeText(this@RegisterActivity, "Registration Successful! Please log in.", Toast.LENGTH_SHORT).show()
                        navigateToLogin()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        dismissLoadingDialog()
                        Toast.makeText(this@RegisterActivity, "Google registration failed. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("GoogleRegisterErr", "Registration Failed: ${e.message}")
                withContext(Dispatchers.Main) {
                    dismissLoadingDialog()
                    Toast.makeText(this@RegisterActivity, "Network error during registration", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showLoadingDialog() {
        dialog = Dialog(this)
        dialog?.setContentView(R.layout.loading_dialog)
        dialog?.setCancelable(false)
        dialog?.show()
    }

    private fun dismissLoadingDialog() {
        dialog?.dismiss()
    }

    private fun showSuccess() {
        Toast.makeText(this, "Registration Successful! Please log in to continue.", Toast.LENGTH_SHORT).show()
    }
}