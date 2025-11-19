package com.uyscuti.social.business

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.uyscuti.social.business.adapter.EditCatalogueAdapter
import com.uyscuti.social.business.model.Catalogue
import com.uyscuti.social.business.retro.CreateCatalogueActivity


class EditCatalogueActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var viewPager: ViewPager2
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EditCatalogueAdapter
    private lateinit var viewName: TextView

    private var videoUri: Uri? = null

    private val REQUEST_CODE_IMAGE_PICKER = 525
    private val REQUEST_CODE_VIDEO_PICKER = 158


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_catalogue)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        toolbar = findViewById(R.id.toolbar)
        toolbar.title = "Edit Product"
        toolbar.setNavigationIcon(R.drawable.baseline_chevron_left_24)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        val catalogue: Catalogue? = intent.getSerializableExtra("catalogue") as Catalogue?

        if (catalogue != null) {
            adapter = EditCatalogueAdapter(this,catalogue)
            adapter.onInvokeCatalogue = { updated->
                val resultIntent = Intent()

                resultIntent.putExtra("updated_catalogue", updated)
                // Set the result to indicate success and pass the resultIntent
                setResult(Activity.RESULT_OK, resultIntent)

                // Finish the current activity and navigate back to the previous one (catalogue)
                finish()
            }

            recyclerView = findViewById(R.id.image_recycler_edit)

            recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            recyclerView.adapter = adapter

        } else{
            val intent = Intent(this, CreateCatalogueActivity::class.java)
            startActivity(intent)
        }
//        viewPager = findViewById(R.id.viewPager)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_IMAGE_PICKER && resultCode == RESULT_OK) {
            val selectedImageUri = data?.data
            // Handle the selected image URI
            val selectedImagePath = getRealPathFromUri(selectedImageUri!!)
            // Do something with the selected image path
            if (selectedImagePath != null) {
                adapter.replaceImage(selectedImagePath)
            }


        } else if (requestCode == REQUEST_CODE_VIDEO_PICKER && resultCode == RESULT_OK) {
            val selectedVideoUri = data?.data
            // Handle the selected video URI
            val selectedVideoPath = getRealPathFromUri(selectedVideoUri!!)
            // Do something with the selected video path
            if (selectedVideoPath != null) {
                val duration = getVideoDuration(selectedVideoPath)
                val maxVideoDuration = 30*1000 // 5 minutes in milliseconds

                if (duration > maxVideoDuration) {
                    val videoThumNail = videoUri.toString()
                    Toast.makeText(this, "Video must be less than 5 minutes", Toast.LENGTH_SHORT).show()
                    return

                }
                adapter.replaceVideo(selectedVideoPath)
            }
        }
    }
    private fun getVideoDuration(videoPath: String): Int {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(videoPath)
        val durationString = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        return durationString?.toInt() ?: 0
    }

    private fun getRealPathFromUri(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                return it.getString(columnIndex)
            }
        }
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.releasePlayer()
    }

    override fun onPause() {
        super.onPause()
        adapter.pausePlayer()
    }
}