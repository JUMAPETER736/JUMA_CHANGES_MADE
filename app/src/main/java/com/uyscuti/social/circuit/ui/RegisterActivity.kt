package com.uyscuti.social.circuit.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.http.HttpException
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresExtension
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.uyscuti.social.core.common.data.room.repository.DialogRepository
import com.uyscuti.social.core.common.data.room.repository.MessageRepository
import com.uyscuti.social.circuit.MainActivity
import com.uyscuti.social.circuit.R
import com.uyscuti.social.network.api.request.googlelogin.GoogleLoginRequest
import com.uyscuti.social.network.api.request.register.RegisterRequest
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject


@AndroidEntryPoint
class RegisterActivity : AppCompatActivity(), TextWatcher {
    private lateinit var registerButton: Button
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var userNameEditText: EditText
    private lateinit var googleBtn: ImageView
    private lateinit var loginLink: TextView
    private var isPasswordVisible = false

    private val PREFS_NAME = "LocalSettings" // Change this to a unique name for your app
    private lateinit var settings: SharedPreferences

    private lateinit var googleSignInClient: GoogleSignInClient

    private val RC_SIGN_IN = 123

    private var dialog: Dialog? = null

    var password: String? = null
    var passwordCon: String? = null

    var username: String? = null
    var email: String? = null

    @Inject
    lateinit var retrofitInstance: RetrofitInstance

    @Inject
    lateinit var localStorage: LocalStorage

    @Inject
    lateinit var dialogRepository: DialogRepository

    @Inject
    lateinit var messageRepository: MessageRepository

    private var chatIdList = ArrayList<String>()


    var gso: GoogleSignInOptions? = null
    var gsc: GoogleSignInClient? = null


    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        registerButton = findViewById(R.id.register_button)
        emailEditText = findViewById(R.id.email)
        passwordEditText = findViewById(R.id.password)
        confirmPassword = findViewById(R.id.confirmPass)
        userNameEditText = findViewById(R.id.username)
        loginLink = findViewById(R.id.login_link)
        googleBtn = findViewById(R.id.google_btn)
        setEditTextChangeListeners()

        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                changeRegisterButton();
//            }
//        }, 2000);

        confirmPassword.addTextChangedListener { changeRegisterButton() }

        registerButton.setOnClickListener {
            if(it.isEnabled){
                password = passwordEditText.text.toString()
                passwordCon = confirmPassword.text.toString()

                if (checkPassword()){
//                    val intent = Intent(this@RegisterActivity, MainActivity::class.java)
//                    startActivity(intent)

                    val email = emailEditText.text.toString()
                    val username = userNameEditText.text.toString()

//                    simulate()

                    registerUser(username, email, password!!)
                } else {
                    passwordEditText.text.clear()
                    confirmPassword.text.clear()
                    Toast.makeText(this@RegisterActivity, "Your Password does not match", Toast.LENGTH_SHORT).show()
                }
            }
        }

        loginLink.setOnClickListener {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        setUpShowPassword()


        initGoogleSignIn()

        googleBtn.setOnClickListener{
//            signIn()
//            val intent = Intent(this@RegisterActivity, GoogleSignInActivity::class.java)
//            startActivity(intent)
            signInWithGoogle()
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

                    val responseBody = response.body()

                    if (responseBody?.user != null) {
                        username = response.body()?.user?.username
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

                        localStorage.setUserId(responseBody.user!!._id)
                        localStorage.setUser(responseBody.user!!.username)
                        responseBody.accessToken?.let { localStorage.setToken(it) }
                        username?.let { localStorage.setUserName(it) }

                        dismissLoadingDialog()

                        runOnUiThread {
                            Toast.makeText(this@RegisterActivity, "Google Login Success", Toast.LENGTH_SHORT)
                                .show()
                        }

                        openMainActivity()

                        showGoogleSuccess()
                    }
                }

            } catch (e: Exception) {
                Log.d("GoogleErr", "Google Login Failed : ${e.message}")

                runOnUiThread {
                    Toast.makeText(this@RegisterActivity, "Google Login Failed", Toast.LENGTH_SHORT)
                        .show()
                }

                e.printStackTrace()
                dismissLoadingDialog()
            }
        }
    }

    private fun showGoogleSuccess() {


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
//        showToast("Google Login Successful")
    }


    private fun openMainActivity() {
        val intent = Intent(this@RegisterActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        showLoadingDialog()
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


    private fun simulate(){
        showLoadingDialog()

        // Simulate a delay (2 seconds) and then open the login activity
        Handler().postDelayed({
            dismissLoadingDialog()

            showSuccess()

            CoroutineScope(Dispatchers.Main).launch {


                delay(900)
                openLoginActivity()
            }

        }, 3000)
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private fun registerUser(username: String, email: String, password: String) {

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkInfo = connectivityManager.activeNetworkInfo
        val isConnected = networkInfo != null && networkInfo.isConnected

        if (!isConnected) {
            // No internet connection, notify the user
            Toast.makeText(this, "No internet connection. Please connect to the internet and try again.", Toast.LENGTH_SHORT).show()
            return
        }

        if (username.isEmpty() || username.length < 4) {
            Toast.makeText(this, "Username must be at least 3 characters long.", Toast.LENGTH_SHORT).show()
            return
        }

//        if (password.isEmpty() || password.length < 6) {
//            Toast.makeText(this, "Password must be at least 6 characters long.", Toast.LENGTH_SHORT).show()
//            return
//        }

        // Show the progress bar
        showLoadingDialog()

        registerButton.visibility = View.INVISIBLE
        GlobalScope.launch(Dispatchers.IO) {

            val response = try {
                val data = RegisterRequest(email, password, username)

                Log.d("RetrofitActivity", "$data")
                retrofitInstance.apiService.registerUsers(data)
//                RetrofitInstance.flashService.loginUsers(datae)
            }catch (e: HttpException) {
                Log.d("RetrofitActivity", "Http : ${e.message}")
//                runOnUiThread {
//                    Toast.makeText(this@RegisterActivity, "HTTP error. Please try again.", Toast.LENGTH_SHORT).show()
//                }

                return@launch
            }catch (e: IOException) {
                Log.d("RetrofitActivity", "IOException : ${e.message}")
//                runOnUiThread {
//                    Toast.makeText(this@RegisterActivity, "Network error. Please try again.", Toast.LENGTH_SHORT).show()
//                }
                return@launch
            }finally {
                // Ensure the progress bar is hidden in case of an error
                withContext(Dispatchers.Main) {
                    dismissLoadingDialog()

                    registerButton.visibility = View.VISIBLE

                }
            }

//            Log.d("Response", "- ${response.body().toString()}")
//            Log.d("Response", "message ${response.message()}")
//            Log.d("Response", "successful state ${response.isSuccessful}")
            if(response.isSuccessful || response.body() != null) {
                withContext(Dispatchers.Main) {
                    showSuccess()
                    registerButton.visibility = View.VISIBLE
                    dismissLoadingDialog()
                    openLoginActivity()

                }
            }else {
                runOnUiThread {
                    Toast.makeText(this@RegisterActivity, "User already exist. create an account with new details", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun requestVerification(){

    }


    private fun openLoginActivity(){
        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
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


    @SuppressLint("ClickableViewAccessibility")
    private fun setUpShowPassword() {
      confirmPassword.setOnTouchListener { _, event ->
            // Check if the touch event is within the bounds of the drawableRight
            if (event.action == MotionEvent.ACTION_UP && event.rawX >= (confirmPassword.right - confirmPassword.compoundDrawables[2].bounds.width())) {
                isPasswordVisible = !isPasswordVisible

                // Toggle password visibility
                if (isPasswordVisible) {
                   confirmPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                   confirmPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_24, 0, R.drawable.baseline_remove_red_eye_24, 0)
                } else {
                    confirmPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    confirmPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_24, 0, R.drawable.baseline_visibility_off_24, 0)
                }

                // Move the cursor to the end of the text
                 confirmPassword.setSelection(confirmPassword.text.length)

                // Call performClick to handle the click event
                confirmPassword.performClick()
            }
            false
        }
    }

    private fun setEditTextChangeListeners() {
        emailEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                email = s.toString()
                changeRegisterButton()
            }

            override fun afterTextChanged(s: Editable) {}
        })
        userNameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                username = s.toString()
                changeRegisterButton()
            }

            override fun afterTextChanged(s: Editable) {}
        })
        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                email = s.toString()
                changeRegisterButton()
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun changeRegisterButton() {
        val isEnabled = confirmPassword.text.isNotEmpty() && passwordEditText.text.isNotEmpty() && emailEditText.text.isNotEmpty() && userNameEditText.text.isNotEmpty()
        if (isEnabled) {
            registerButton.isEnabled = true
            registerButton.setBackgroundColor(resources.getColor(R.color.bluejeans))
        } else {
            registerButton.setBackgroundColor(resources.getColor(R.color.fade))
            registerButton.isEnabled = false
        }
    }

    private fun showSuccess(){
        showToast("Registration Successful")
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun checkPassword(): Boolean {
        return password == passwordCon
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        TODO("Not yet implemented")
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        TODO("Not yet implemented")
    }

    override fun afterTextChanged(s: Editable?) {
        TODO("Not yet implemented")
    }
}