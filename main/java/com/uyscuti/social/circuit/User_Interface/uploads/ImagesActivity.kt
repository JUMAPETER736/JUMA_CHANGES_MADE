package com.uyscuti.social.circuit.User_Interface.uploads

import android.Manifest
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.ActivityImagesBinding
import com.uyscuti.social.circuit.User_Interface.uploads.adapter.ImageAdapter

class ImagesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityImagesBinding
    private val imagesList = ArrayList<String>()
    private val REQUEST_PERMISSION = 100
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImagesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24_black)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

//        loadImage()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_PERMISSION)
        } else {
            loadImage()
        }
    }


    private fun loadImages() {
        Log.d("UserProfile", "Loading images....")

        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null)
        cursor?.let {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            while (it.moveToNext()) {
                val imagePath = it.getString(columnIndex)
                imagesList.add(imagePath)
            }
            it.close()

            Log.d("UserProfile", "Images Loaded:  $imagesList")

            val adapter = ImageAdapter(imagesList){}
            binding.recyclerView.layoutManager = LinearLayoutManager(this)
            binding.recyclerView.adapter = adapter
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadImage()
        }
    }


    private fun loadImage() {
        val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA)
        val sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC"

        val cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )

        cursor?.use { rs ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

            while (cursor.moveToNext()) {
                val imageId = cursor.getLong(idColumn)
                val imagePath = cursor.getString(dataColumn)
                val imageUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageId.toString())

                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    imageId
                )
                val selectedImagePath = contentUri.toString() // Get the selected image URI as a string
                imagesList.add(selectedImagePath)
            }

            val adapter = ImageAdapter(imagesList){ imageUrl ->
                // Pass the image URL back to the UserActivity
                val resultIntent = Intent()
                resultIntent.putExtra("image_url", imageUrl)
                setResult(RESULT_OK, resultIntent)
                finish()
            }
            binding.recyclerView.layoutManager = GridLayoutManager(this, 3) // Grid layout with 3 columns
            binding.recyclerView.adapter = adapter
        }
    }
}