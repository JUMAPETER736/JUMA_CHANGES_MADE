package com.uyscuti.social.circuit

import android.content.Intent
import android.content.IntentSender
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.uyscuti.social.network.api.request.googlelogin.GoogleLoginRequest
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class GoogleOAuth : AppCompatActivity() {
    var gso: GoogleSignInOptions? = null
    var gsc: GoogleSignInClient? = null
    private val AUTH_URL = "https://freeapi-app-production-4811.up.railway.app/api/v1/users/google"

    @Inject
    lateinit var retrofitInterface: RetrofitInstance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_google_oauth)

        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestProfile()
            .requestEmail().build()

        gsc = GoogleSignIn.getClient(this, gso!!)

//        val acct = GoogleSignIn.getLastSignedInAccount(this)
//        if (acct != null) {
////            navigateToSecondActivity()
//            // Before sending the ID token to the server
//            Log.d("TAG", "Sending ID token to server: ${acct.idToken}")
//            val username = acct.displayName
//            val email = acct.email
//            Log.d("TAG", "Account: ${acct.account}")
//            Log.d("TAG", "ServerAuthCode: ${acct.serverAuthCode}")
////            authenticateWithServer(acct.idToken)
////            val registerData = RegisterRequest("google@gmail.com", "password", "username")
////            registerUserWithServer(registerData)
//        }

        // Launch Chrome Custom Tab when the user wants to authenticate
//        launchCustomTabForGoogleAuth()

        val google = findViewById<ImageView>(R.id.google_btn)

        google.setOnClickListener {
            signIn()
        }

//        val handler = Handler()
//        handler.postDelayed({
//            signIn()
//        }, 1000)
    }

    private fun signIn() {
        val signInIntent = gsc!!.signInIntent
        startActivityForResult(signInIntent, 1000)
    }


    fun new(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                Log.d("TAG", "Google Sign-In successful. ID Token: ${account?.idToken}")
                Log.d("TAG", "account id:  ${account?.id}")
//                Log.d("TAG", "account id:  $account")
                Log.d("TAG", "Account: ${account.account}")
                Log.d("TAG", "ServerAuthCode: ${account.serverAuthCode}")


                if (idToken != null) {
                    authenticateWithServer(idToken)
                }

//                authenticateWithServer(account?.idToken)
//                val registerData = RegisterRequest("google@gmail.com", "password", "username")
//                registerUserWithServer(registerData)`
//                navigateToSecondActivity()
            } catch (e: ApiException) {
                Log.e("TAG", "Google Sign-In failed. Status code: ${e.statusCode}")
                Log.e("TAG", "Google Sign-In failed. Resolution: ${e.status.resolution}")
                Log.e("TAG", "Google Sign-In failed. Message: ${e.message}")
                Log.e("TAG", "Google Sign-In failed. Status: ${e.status}")
                if (e.status.hasResolution()) {
                    try {
                        e.status.startResolutionForResult(this, 1001)
                    } catch (e: IntentSender.SendIntentException) {
                        Log.e("TAG", "Google Sign-In failed. Start resolution failed: ${e.message}")
                    }
                } else {
                    Log.e("TAG", "Google Sign-In failed. No resolution available.")
                    Toast.makeText(
                        applicationContext,
                        "Google Sign-In failed. No resolution available.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }


//        if (requestCode == 1000) {
//            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
//            try {
//                task.getResult(ApiException::class.java)
//                navigateToSecondActivity()
//            } catch (e: ApiException) {
//                Toast.makeText(applicationContext, "Something went wrong", Toast.LENGTH_SHORT)
//                    .show()
//            }
//        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                // Handle the successful sign-in

                Log.d("GoogleLogin", "Google Login Success")
//                if (idToken != null) {
//                    authenticateWithServer(idToken)
//                }
            } catch (e: ApiException) {
                // Handle sign-in failure
                Log.e("TAG", "Google Sign-In failed. Error code: ${e.statusCode}")
                Log.e("TAG", "Google Sign-In failed. : ${e.message}")
                Toast.makeText(applicationContext, "Google Sign-In failed", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun authenticateWithServer(idToken: String) {
        val loginData = GoogleLoginRequest(
            idToken = idToken
        )


        CoroutineScope(Dispatchers.IO).launch {

            try {

                val response = retrofitInterface.apiService.googleLogin(loginData)

                if (response.isSuccessful) {
                    Log.d("GoogleLogin", "Google Login Success")
                    Log.d("GoogleLogin", "Google Login response : ${response.body()}")
                    Toast.makeText(this@GoogleOAuth, "Google Login Success", Toast.LENGTH_SHORT)
                        .show()
                }

            } catch (e: Exception) {
                Log.d("GoogleErr", "Google Login Failed : ${e.message}")
                e.printStackTrace()
            }

        }

    }


    private fun navigateToSecondActivity() {
        finish()
//        val intent = Intent(this@MainActivity, SecondActivity::class.java)
//        startActivity(intent)
    }

//    private fun launchCustomTabForGoogleAuth() {
//        val customTabsIntent = CustomTabsIntent.Builder().build()
//        customTabsIntent.launchUrl(this, Uri.parse(AUTH_URL))
//    }
}