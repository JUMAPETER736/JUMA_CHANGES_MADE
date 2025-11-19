package com.uyscuti.social.business.retro

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresExtension
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.business.ProfileActivity
import com.uyscuti.social.business.R

import com.uyscuti.social.business.adapter.CreateProductAdapter
import com.uyscuti.social.business.adapter.ImageAdapter
import com.uyscuti.social.business.model.Catalogue

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException


class CreateCatalogueActivity : AppCompatActivity() {
    private val context: Context = this
    private lateinit var recyclerView: RecyclerView
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var toolbar: Toolbar
    private lateinit var createProductAdapter: CreateProductAdapter

    private val videoUri : String? = null


    private val PERMISSION_REQUEST_CODE = 1001
    private val REQUEST_CODE_VIDEO_PICKER = 158


    @RequiresExtension(extension = Build.VERSION_CODES.R, version = 2)
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("WrongViewCast", "MissingInflatedId", "SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_catalogue)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Set up the toolbar

        toolbar = findViewById(R.id.toolbar)
        toolbar.title = "Create Catalogue Item"
        toolbar.setNavigationIcon(R.drawable.baseline_chevron_left_24)

        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        createProductAdapter = CreateProductAdapter(this)
        createProductAdapter.onInvokeCatalogue = { catalogue ->

            val resultIntent = Intent()


            val images = arrayListOf<String>()

            for (image in catalogue.images) {
                val file = File(image)
                val destinationFileName = "${System.currentTimeMillis()}.${file.extension}"
                val imageUri = copyFileToInternalStorage(this, image, destinationFileName)
                if (imageUri != null) {
                    images.add(imageUri.absolutePath)
                }
            }

            if (images.isEmpty()) {
                finish()
            }

            val product = Catalogue(
                catalogue.id,
                catalogue.name,
                catalogue.description,
                catalogue.price,
                images
            )

            resultIntent.putExtra("resultKey", product)

            // Set the result to indicate success and pass the resultIntent
            setResult(Activity.RESULT_OK, resultIntent)

            // Finish the current activity and navigate back to the previous one (catalogue)
            finish()
        }

        recyclerView = findViewById(R.id.image_recycler_view)
        recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        recyclerView.adapter = createProductAdapter

    }

    private fun copyFileToInternalStorage(
        context: Context,
        sourceFilePath: String,
        destinationFileName: String
    ): File? {
        val externalCacheDir = context.externalCacheDir
        val productImagesDir = File(externalCacheDir, "product/images")


        // Create the product/images directory if it doesn't exist
        productImagesDir.mkdirs()

        // Create the destination file
        val destinationFile = File(productImagesDir, destinationFileName)

        Log.d("ApiService", "Source File Path: $sourceFilePath")
        Log.d("ApiService", "Destination File Path: ${destinationFile.absolutePath}")
        Log.d("ApiService", "Destination File name: $destinationFileName")

        try {
            // Open the source and destination streams
            FileInputStream(File(sourceFilePath)).use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    // Copy the file content
                    val buffer = ByteArray(4 * 1024)
                    var read: Int
                    while (inputStream.read(buffer).also { read = it } != -1) {
                        outputStream.write(buffer, 0, read)
                    }
                    outputStream.flush()
                }
            }
            return destinationFile
        } catch (e: IOException) {
            Log.e("ApiService", "File Operation Error : ${e.message}")
            e.printStackTrace()
        }

        return null
    }

    // Handle permission request result
    @RequiresExtension(extension = Build.VERSION_CODES.R, version = 2)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with your operations
                pickImage()

            } else {
                // Permission denied, handle accordingly (e.g., show a message to the user)
                Toast.makeText(this, "Please allow permission to proceed", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun resolveContentUri(context: Context, contentUri: Uri): String? {
        val contentResolver: ContentResolver = context.contentResolver

        // Specify the columns you want to retrieve
        val projection = arrayOf(MediaStore.MediaColumns.DATA)

        // Perform a query to retrieve information about the media file
        contentResolver.query(contentUri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                // Get the file path from the cursor
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                return cursor.getString(columnIndex)
            }
        }
        // Return null if the query didn't return any results
        return null
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

    @SuppressLint("SuspiciousIndentation")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val imageUris = mutableListOf<Uri>()
            var image: String? = null

            if (data.clipData != null) {
                // Multiple images are selected
                val clipData = data.clipData
                for (i in 0 until clipData!!.itemCount) {
                    val uri = clipData.getItemAt(i).uri
                    val resolvedUri = getRealPathFromUri(uri)

                    Log.d("ImagePicker", "Content URI: $uri")
                    if (resolvedUri != null) {
                        Log.d("ImagePicker", "Resolved URI: $resolvedUri")
                        imageUris.add(Uri.parse(resolvedUri))
                    } else {
                        imageUris.add(uri)
                    }
                }
            } else if (data.data != null) {
                // Single image is selected
                val uri = data.data

                val resolvedUri = getRealPathFromUri(Uri.parse(uri.toString()))
                if (resolvedUri != null) {
                    Log.d("MediaPicker", "Resolved URI: $resolvedUri")
                    imageUris.add(Uri.parse(resolvedUri))

                    image = resolvedUri

                    createProductAdapter.addImage(resolvedUri)

                } else {
                    imageUris.add(uri!!)
                }
            }
//            if (image != null) {
//                createProductAdapter.addImage(image)
//
//            }
        } else if (requestCode == REQUEST_CODE_VIDEO_PICKER && resultCode == Activity.RESULT_OK && data != null) {
            val video: String? = null
            if (data.data != null) {
                val uri = data.data
                val resolvedUri = getRealPathFromUri(Uri.parse(uri.toString()))
                if (resolvedUri != null) {
                    Log.d("MediaPicker", "Resolved URI: $resolvedUri")
                    val duration = getVideoDuration(Uri.parse(resolvedUri)) as Long
                    Log.d("MediaPicker", "Video Duration: $duration")
                    // Check if the video duration is within the allowed range


                    val maxVideoDuration = 30 * 1000
                    if (duration > maxVideoDuration) {
                        val videoThumbnail= videoUri.toString()
                        Toast.makeText(this, "Video duration must be less than 30 seconds", Toast.LENGTH_SHORT).show()
                        return
                    }

                    createProductAdapter.addVideo(resolvedUri)




                }

            }

        }

    }

    private fun getVideoDuration(videoUri: Uri): Long {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, videoUri)
        val durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        return durationString?.toLongOrNull() ?: 0
    }



    //    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        val PICK_IMAGE_REQUEST = 3
//        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
//            val imageUri = data.data
//            // Update the ImageView with the selected image
////            val productImageView= findViewById<ImageView>(R.id.productImageView)
////            productImageView.setImageURI(imageUri)
//        }
//    }
//    private fun pickImage(context: Context) {
//        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//        val PICK_IMAGE_REQUEST = 3
//        startActivityForResult(intent, PICK_IMAGE_REQUEST)
//    }
    private fun saveData() {
        // Function to save data
        // You can implement your logic here
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private val PICK_IMAGE_REQUEST = 525 // Move this line outside of the function
//    private fun pickImage() {
//        val intent = Intent(Intent.ACTION_GET_CONTENT)
//        intent.type = "image/*"
//        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
//        startActivityForResult(Intent.createChooser(intent, "Select Images"), PICK_IMAGE_REQUEST)
//    }


    @RequiresExtension(extension = Build.VERSION_CODES.R, version = 2)
    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*" // Set the MIME type for images
        intent.putExtra(
            MediaStore.EXTRA_PICK_IMAGES_MAX,
            8
        ) // Set the maximum number of images to pickstartActivityForResult(intent, PICK_IMAGE_REQUEST)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)

    }
}



