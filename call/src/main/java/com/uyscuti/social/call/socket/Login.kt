package com.uyscuti.social.call.socket

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast

import com.uyscuti.social.call.repository.MainRepository
import com.uyscuti.social.call.representation.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.uyscuti.social.call.CallMainActivity
import com.uyscuti.social.call.databinding.ActivityLogin2Binding


@AndroidEntryPoint
class Login : AppCompatActivity() {
    private  lateinit var  binding: ActivityLogin2Binding
    @Inject
    lateinit var repository: MainRepository

    // Define your permissions
    @RequiresApi(Build.VERSION_CODES.P)
    private val permissions = arrayOf(
        Manifest.permission.INTERNET,
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA,
        Manifest.permission.MODIFY_AUDIO_SETTINGS,
        Manifest.permission.FOREGROUND_SERVICE,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.VIBRATE,
        Manifest.permission.SYSTEM_ALERT_WINDOW
    )

    // Initialize the permission request launcher
    private val requestPermissionLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsMap ->
            // Check if all permissions were granted
            if (permissionsMap.all { it.value }) {
                // All permissions granted, continue with your login logic
                launchMainActivity()
            } else {
                // Handle the case where permissions were not granted
                // You may show a message to the user or take appropriate action
            }
        }


    @Inject
    lateinit var mainViewModel: MainViewModel
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogin2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }


    @RequiresApi(Build.VERSION_CODES.P)
    private fun init(){
        binding.apply {
            btn.setOnClickListener {

                if (binding.usernameEt.text.isNullOrEmpty()){
                    Toast.makeText(this@Login, "please fill the username", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
//                repository.init(
//                    usernameEt.text.toString()
//                )

                // Check and request permissions
                checkAndRequestPermissions()

                val intent = Intent(this@Login, CallMainActivity::class.java)
                intent.putExtra("username", usernameEt.text.toString())
                startActivity(intent)
                finish()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        // Check if each permission is granted
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission)
            }
        }

        // If some permissions need to be requested
        if (permissionsToRequest.isNotEmpty()) {
            // Request the permissions
            val permissionsArray = permissionsToRequest.toTypedArray()
            requestPermissionLauncher.launch(permissionsArray)
        } else {
            // All permissions are already granted, continue with your login logic
            launchMainActivity()
        }
    }

    private fun launchMainActivity() {
        // Start the MainActivity
        val intent = Intent(this@Login, CallMainActivity::class.java)
        intent.putExtra("username", binding.usernameEt.text.toString())
        startActivity(intent)
        finish()
    }
}