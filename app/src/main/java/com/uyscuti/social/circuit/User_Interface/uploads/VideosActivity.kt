package com.uyscuti.social.circuit.User_Interface.uploads

import android.Manifest
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.ActivityVideosBinding
import com.uyscuti.social.circuit.User_Interface.uploads.adapter.VideoAdapter

class VideosActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVideosBinding
    private val REQUEST_PERMISSIONS_CODE = 123
    private val imagesList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24_black)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // Set slide-up animation
//        overridePendingTransition(R.anim.slide_up, R.anim.stay)

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_PERMISSIONS_CODE
            )
        } else {
            loadAndDisplayVideos()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadAndDisplayVideos()
            } else {
                // Handle permission denied
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    lateinit var vUri: Uri
    private fun loadAndDisplayVideos() {
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DATE_ADDED
        )

        val cursor = contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            "${MediaStore.Video.Media.DATE_ADDED} DESC"
        )

        val videosUri = MediaStore.Video.Media.getContentUri("external")
        cursor?.use {
            if (it.moveToFirst()) {
                val videoList = mutableListOf<String>()
                val uriList = mutableListOf<Uri>()
                val dataIndex = it.getColumnIndex(MediaStore.Video.Media.DATA)

                do {
                    val videoPath = it.getString(dataIndex)
                    if (!videoPath.isNullOrBlank()) {
                        videoList.add(videoPath)
                        val uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, it.getLong(0))
                        uriList.add(uri)
//                        vUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, it.getLong(0))
//                        uriList.add(ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, it.getLong(0)))
                    }
                } while (it.moveToNext())

                val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
                val layoutManager = GridLayoutManager(this, 3)
                recyclerView.layoutManager = layoutManager
                val adapter = VideoAdapter(videoList) { selectedVideoPath ->
                    // Handle the click event
                    val selectedVideoIndex = videoList.indexOf(selectedVideoPath)
                    val selectedVideoUri = uriList[selectedVideoIndex]
                    val resultIntent = Intent()
                    resultIntent.putExtra("video_url", selectedVideoPath)
                    resultIntent.putExtra("vUri", selectedVideoUri.toString())
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
                recyclerView.adapter = adapter
            } else {
                // Handle the case where there are no videos
                Toast.makeText(this, "No videos found", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun loadImage() {
        val projection = arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA)
        val sortOrder = MediaStore.Video.Media.DATE_ADDED + " DESC"

        val cursor = contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )

        cursor?.use { rs ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)

            while (cursor.moveToNext()) {
                val imageId = cursor.getLong(idColumn)
                val imagePath = cursor.getString(dataColumn)
                val imageUri = Uri.withAppendedPath(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    imageId.toString()
                )

                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    imageId
                )
                val selectedImagePath =
                    contentUri.toString() // Get the selected image URI as a string
                imagesList.add(selectedImagePath)
            }

            val adapter = VideoAdapter(imagesList) {

            }
            binding.recyclerView.layoutManager =
                GridLayoutManager(this, 3) // Grid layout with 3 columns
            binding.recyclerView.adapter = adapter
        }
    }

}