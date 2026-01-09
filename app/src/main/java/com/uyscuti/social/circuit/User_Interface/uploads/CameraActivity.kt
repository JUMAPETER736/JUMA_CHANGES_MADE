package com.uyscuti.social.circuit.User_Interface.uploads



import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.journeyapps.barcodescanner.camera.CameraManager
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.ActivityCamera2Binding
import com.uyscuti.social.circuit.databinding.ActivityCameraBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCamera2Binding
    private lateinit var cameraManager: CameraManager

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var isFlashEnabled = false

    companion object {
        const val EXTRA_MEDIA_TYPE = "media_type"
        const val EXTRA_MEDIA_URI = "media_uri"
        const val EXTRA_MEDIA_PATH = "media_path"
        const val MEDIA_TYPE_PHOTO = "photo"
        const val MEDIA_TYPE_VIDEO = "video"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCamera2Binding.inflate(layoutInflater)
        setContentView(binding.root)


        // Initialize camera manager
        cameraManager = CameraManager(this, this, binding.previewView)

        // Setup permission launcher
        permissionLauncher = createPermissionLauncher(
            onGranted = { startCameraPreview() },
            onDenied = {
                Toast.makeText(
                    this,
                    "Camera permissions are required",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        )

        // Request permissions or start camera
        if (cameraManager.hasPermissions()) {
            startCameraPreview()
        } else {
            permissionLauncher.launch(CameraManager.REQUIRED_PERMISSIONS)
        }

        setupClickListeners()
    }

    private fun startCameraPreview() {
        cameraManager.startCamera(object : CameraManager.CameraCallback {
            override fun onPhotoCapture(uri: Uri, file: File?) {
                runOnUiThread {
                    Toast.makeText(
                        this@CameraActivity,
                        "Photo saved successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Return photo result to MainActivity
                    returnPhotoResult(uri, file)
                }
            }

            override fun onPhotoCaptureError(exception: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@CameraActivity,
                        "Error: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onVideoRecordingStart() {
                runOnUiThread {
                    binding.videoButton.text = "Stop Recording"
                    binding.videoButton.setBackgroundColor(getColor(android.R.color.holo_red_dark))
                    binding.captureButton.isEnabled = false
                }
            }

            override fun onVideoRecordingStop(uri: Uri, file: File?) {
                runOnUiThread {
                    binding.videoButton.text = "Start Recording"
                    binding.videoButton.setBackgroundColor(getColor(android.R.color.holo_blue_dark))
                    binding.captureButton.isEnabled = true

                    Toast.makeText(
                        this@CameraActivity,
                        "Video saved successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Return video result to MainActivity
                    returnVideoResult(uri, file)
                }
            }

            override fun onVideoRecordingError(exception: Exception) {
                runOnUiThread {
                    binding.videoButton.text = "Start Recording"
                    binding.videoButton.setBackgroundColor(getColor(android.R.color.holo_blue_dark))
                    binding.captureButton.isEnabled = true

                    Toast.makeText(
                        this@CameraActivity,
                        "Recording error: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    private fun setupClickListeners() {
        // Capture photo
        binding.captureButton.setOnClickListener {
            cameraManager.capturePhoto(object : CameraManager.CameraCallback {
                override fun onPhotoCapture(uri: Uri, file: File?) {
                    runOnUiThread {
                        Toast.makeText(
                            this@CameraActivity,
                            "Photo captured!",
                            Toast.LENGTH_SHORT
                        ).show()
                        returnPhotoResult(uri, file)
                    }
                }

                override fun onPhotoCaptureError(exception: Exception) {
                    runOnUiThread {
                        Toast.makeText(
                            this@CameraActivity,
                            "Capture failed: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onVideoRecordingStart() {}
                override fun onVideoRecordingStop(uri: Uri, file: File?) {}
                override fun onVideoRecordingError(exception: Exception) {}
            })
        }

        // Record video
        binding.videoButton.setOnClickListener {
            if (cameraManager.isRecording()) {
                cameraManager.stopRecording()
            } else {
                cameraManager.startRecording(object : CameraManager.CameraCallback {
                    override fun onPhotoCapture(uri: Uri, file: File?) {}
                    override fun onPhotoCaptureError(exception: Exception) {}

                    override fun onVideoRecordingStart() {
                        runOnUiThread {
                            binding.videoButton.text = "Stop Recording"
                            binding.captureButton.isEnabled = false
                        }
                    }

                    override fun onVideoRecordingStop(uri: Uri, file: File?) {
                        runOnUiThread {
                            binding.videoButton.text = "Start Recording"
                            binding.captureButton.isEnabled = true
                            returnVideoResult(uri, file)
                        }
                    }

                    override fun onVideoRecordingError(exception: Exception) {
                        runOnUiThread {
                            binding.videoButton.text = "Start Recording"
                            binding.captureButton.isEnabled = true
                            Toast.makeText(
                                this@CameraActivity,
                                "Error: ${exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                })
            }
        }

        // Switch camera
        binding.switchCameraButton.setOnClickListener {
            cameraManager.switchCamera()
        }

        // Toggle flash
        binding.flashButton.setOnClickListener {
            if (cameraManager.hasFlash()) {
                isFlashEnabled = !isFlashEnabled
                cameraManager.setFlashMode(isFlashEnabled)
                binding.flashButton.text = if (isFlashEnabled) "Flash: ON" else "Flash: OFF"
            } else {
                Toast.makeText(this, "Flash not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Return photo result to Activity or Fragment
     */
    private fun returnPhotoResult(uri: Uri, file: File?) {
        val resultIntent = Intent().apply {
            putExtra(EXTRA_MEDIA_TYPE, MEDIA_TYPE_PHOTO)
            putExtra(EXTRA_MEDIA_URI, uri.toString())
            file?.let { putExtra(EXTRA_MEDIA_PATH, it.absolutePath) }
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    /**
     * Return video result to MainActivity
     */
    private fun returnVideoResult(uri: Uri, file: File?) {
        val resultIntent = Intent().apply {
            putExtra(EXTRA_MEDIA_TYPE, MEDIA_TYPE_VIDEO)
            putExtra(EXTRA_MEDIA_URI, uri.toString())
            file?.let { putExtra(EXTRA_MEDIA_PATH, it.absolutePath) }
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraManager.shutdown()
    }
}
