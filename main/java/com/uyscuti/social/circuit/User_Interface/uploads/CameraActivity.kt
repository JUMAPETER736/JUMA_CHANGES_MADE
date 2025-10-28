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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.ActivityCameraBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding

    private val cameraPermission = Manifest.permission.CAMERA
    private val cameraPermissionRequestCode = 101

    private var takenPicturePath = ""

    private lateinit var currentPhotoPath: String

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { result: Bitmap? ->
            // Handle the result (captured image)
            if (result != null) {
                // Do something with the captured image (e.g., display in an ImageView)
//                imageView.setImageBitmap(result)
                binding.image.setImageBitmap(result)
            }
        }

    private val takePictureLauncherM: ActivityResultLauncher<Uri> =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                // Do something with the taken picture path
                binding.image.setImageURI(Uri.parse(currentPhotoPath))
            } else {
                // Handle the case where the picture was not taken successfully
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)


        binding.toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24)



        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }



        // Check camera permission
        if (checkCameraPermission()) {
            // Permission is already granted, enable the button
            setupCameraButton()
        } else {
            // Request camera permission
            requestCameraPermission()
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            cameraPermission
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.confirm, menu)
        return true
    }


    private fun createImageFile(): Uri {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File.createTempFile(
            "FLASH_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
        currentPhotoPath = imageFile.absolutePath
        return FileProvider.getUriForFile(
            this,
            "com.uyscut.provider",
            imageFile
        )
    }


    private fun confirmImage() {
        // Return the image path to the calling activity
        val resultIntent = Intent()
        resultIntent.putExtra("image_url", currentPhotoPath)
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.menu_confirm -> {

                confirmImage()
//                return true
            }
            else -> {

            }
        }
        return true
    }

    private fun setupCameraButton() {
        takePictureLauncherM.launch(createImageFile())
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(cameraPermission),
            cameraPermissionRequestCode
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == cameraPermissionRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, enable the button
                setupCameraButton()
            } else {
                // Permission denied, handle accordingly (e.g., show a message)
                // You may want to inform the user why the permission is required
            }
        }
    }
}
