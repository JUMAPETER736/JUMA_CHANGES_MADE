package com.uyscuti.social.call

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.permissionx.guolindev.PermissionX
import com.uyscuti.social.call.databinding.ActivityMainCallBinding
import com.uyscuti.social.call.models.DataModel
import com.uyscuti.social.call.models.DataModelType
import com.uyscuti.social.call.repository.MainRepository
import com.uyscuti.social.call.service.MainServiceRepository
import com.uyscuti.social.call.ui.CallActivity

import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val TAG = "CallMainActivity"

@AndroidEntryPoint

class CallMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainCallBinding
    @Inject
    lateinit var mainRepository: MainRepository

    @Inject
    lateinit var mainServiceRepository: MainServiceRepository

    private var username: String? = null
    private var friendUsername: String? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        username = intent.getStringExtra("username")
        startMyService()
       // val perm = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
       // startActivity(perm)

        binding.callBtn.setOnClickListener {
            Log.d(TAG, "onCreate: call button clicked")
            PermissionX.init(this)
                .permissions(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA
                ).request { allGranted, _, _ ->
                    if (allGranted) {
                        mainRepository.sendConnectionRequest(
                            DataModel(
                                DataModelType.StartVideoCall,username!!,binding.username.text.toString(),null
                            )
                        ){
                            if (it) {
                                //we have to start video call
                                //we wanna create an intent to move to call activity
                                startActivity(Intent(this, CallActivity::class.java).apply {
                                    putExtra("target", binding.username.text.toString())
                                    putExtra("isVideoCall", true)
                                    putExtra("isCaller", true)
                                })

                            }
                        }

                    } else {
                        Toast.makeText(this, "you should accept all permissions", Toast.LENGTH_LONG)
                            .show()
                    }
                }

        }

    }
    private fun startMyService() {

        mainServiceRepository.startService(username!!)
        mainRepository.setUserName(username!!)
    }

}