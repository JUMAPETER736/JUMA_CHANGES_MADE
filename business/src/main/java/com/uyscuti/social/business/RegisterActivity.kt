package com.uyscuti.social.business

import android.net.http.HttpException
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresExtension
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.uyscuti.social.business.databinding.ActivityRegisterBinding
//import com.example.mylibrary.retro.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
//import retrofit2.HttpException


//private val apiService = RetrofitClient.instance

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

      binding.buttonRegister.setOnClickListener {
          val username = binding.textUsername.text.toString()
          val email = binding.emailRegister.text.toString()
          val password = binding.pass.text.toString()

          CoroutineScope(Dispatchers.IO).launch {
              try {
//                  val response = apiService.register(username, email, password)

                  // Handle successful registration, response contains message
              } catch (e: HttpException) {
                  // Handle error response (e.g., username or email already taken)
                  withContext(Dispatchers.Main) {
                      // Show error message to user
                  }
              } catch (e: Throwable) {
                  // Handle network or unexpected errors
                  withContext(Dispatchers.Main) {
                      // Show error message to user
                  }
              }
          }
      }
    }
}