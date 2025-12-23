package com.uyscuti.social.business

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.net.http.HttpException
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresExtension
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.business.adapter.ProfileAdapter
import com.uyscuti.social.business.fragment.CategoryFragment
import com.uyscuti.social.business.fragment.ProfileFragment
import com.uyscuti.social.business.model.Catalogue
//import com.example.mylibrary.retro.RetrofitClient
import com.uyscuti.social.business.room.database.BusinessDatabase
import com.uyscuti.social.business.room.repository.BusinessRepository
import com.uyscuti.social.business.util.ImagePicker
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.AndroidEntryPoint

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
//    private lateinit var toolbar: Toolbar
    private lateinit var profileAdapter: ProfileAdapter
    private lateinit var profileTabFrame: FrameLayout
    private lateinit var interestTabFrame: FrameLayout
    private lateinit var profileTextView: TextView
    private lateinit var interestTextView: TextView

    private lateinit var fragmentContainerView: FrameLayout
    private val REQUEST_CODE_IMAGE_PICKER = 100
    private val REQUEST_CODE_IMAGE_PICKER_CATALOGUE = 110
    private val REQUEST_CODE_VIDEO_PICKER = 158


    //    private val apiService = RetrofitClient.instance
    @Inject
    lateinit var retrofitInterface: RetrofitInstance

    private lateinit var sharedPreferences: SharedPreferences


    private lateinit var businessDatabase: BusinessDatabase
    private lateinit var businessRepository: BusinessRepository

    private val API_TAG = "ApiService"

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE)

//
//        businessDatabase = BusinessDatabase.getInstance(this)
//        businessRepository = BusinessRepository(businessDatabase.businessDao())


//        toolbar = findViewById(R.id.toolbar)
//
//        setSupportActionBar(toolbar)
//
//        toolbar.title = "Business Profile"
//        toolbar.setNavigationIcon(R.drawable.baseline_chevron_left_24)

        fragmentContainerView = findViewById(R.id.fragment_container)

        profileTabFrame = findViewById(R.id.profileTab)
        profileTabFrame.isSelected = true

        interestTabFrame = findViewById(R.id.interestsTab)
        interestTabFrame.isSelected = false

        profileTextView = findViewById(R.id.profileText)
        interestTextView = findViewById(R.id.interestsText)


//        recyclerView = findViewById(R.id.recyclerView)

        switchSelection(0)

//        profileAdapter = ProfileAdapter()

        profileTextView.setOnClickListener {
            switchSelection(0)
        }

        interestTextView.setOnClickListener {
            switchSelection(1)
        }

//        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
//        recyclerView.adapter = profileAdapter

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {

            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                val profileFragment =
                    supportFragmentManager.findFragmentByTag("ProfileFragment") as? ProfileFragment
                profileFragment?.getCoordinates()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        Log.d(
            "ProfileFragment",
            "activity onActivityResult: $data  code: $requestCode result: $resultCode"
        )

        if (resultCode == RESULT_OK && requestCode == 111) {

            try {

                // Check if the data Intent is not null and contains the expected key
                data?.getSerializableExtra("resultKey")?.let { catalogue ->
                    // Find the existing instance of the fragment by its tag
                    val profileFragment =
                        supportFragmentManager.findFragmentByTag("ProfileFragment") as? ProfileFragment

                    // Pass the catalogue object to the fragment if it exists
                    profileFragment?.addCatalogue(catalogue as Catalogue)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }


        } else {
            val imageUri = ImagePicker.onActivityResult(requestCode, resultCode, data)
            val code = imageUri.first

            if (imageUri.second != null) {
                // Update the selected image in your data model
                // and notify the adapter about the change
//            profileAdapter.setBackgroundImage(imageUri)

                Log.d("MediaPicker", "onActivityResult: media: $imageUri")

                // Find the existing instance of the fragment by its tag
                val profileFragment =
                    supportFragmentManager.findFragmentByTag("ProfileFragment") as? ProfileFragment


                if (code == REQUEST_CODE_IMAGE_PICKER) {
                    profileFragment?.setBackgroundImage(imageUri.second!!)
                } else if (code == REQUEST_CODE_IMAGE_PICKER_CATALOGUE) {
                    profileFragment?.addCatalogImage(imageUri.second!!)
                } else if (code == REQUEST_CODE_VIDEO_PICKER) {
                    val resolved = getRealPathFromUri(imageUri.second!!)

                    Log.d("MediaPicker", "onActivityResult: video: $imageUri")
                    Log.d("MediaPicker", "onActivityResult: resolved video: $resolved")
                    profileFragment?.setBackgroundVideo(Uri.parse(resolved))
                }
                // Check if the fragment is not null


            }
        }
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


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.catalogue_menu, menu)
        menu.findItem(R.id.action_delete).isVisible = false
        return true
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete -> {
                deleteSelectedItems()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private fun deleteSelectedItems() {

        val profileFragment =
            supportFragmentManager.findFragmentByTag("ProfileFragment") as? ProfileFragment


        val selectedIds = profileFragment?.getSelectedProductsIds()

        if (selectedIds != null) {

            CoroutineScope(Dispatchers.IO).launch {
                deleteProductsRequest(selectedIds)

                for (id in selectedIds) {

                    profileFragment.deleteMyProduct(id)

                }

            }
        }

        profileFragment?.deleteSelectedItems()

//        toolbar.menu.findItem(R.id.action_delete).isVisible = false
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private suspend fun deleteProductsRequest(productIds: List<String>) {
        for (productId in productIds) {

            try {
                val response = retrofitInterface.apiService.deleteProduct(productId)
                if (response.isSuccessful) {
                    Log.d(API_TAG, "Product deleted successfully")
                } else {
                    Log.e(API_TAG, "Failed to delete product: ${response.message()}")
                }
            } catch (e: HttpException) {
                Log.e(API_TAG, "Failed to delete product: ${e.message}")
            } catch (e: IOException) {
                Log.e(API_TAG, "Failed to delete product: ${e.message}")
            }
        }
    }

//    private fun areCheckBoxesChecked() {
//        val checkBox1 = findViewById<CheckBox>(R.id.agree_checkbox)
//        val checkBox2 = findViewById<CheckBox>(R.id.flash_checkbox)
//    }


    private fun switchSelection(position: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            profileTabFrame.isSelected = position == 0
            interestTabFrame.isSelected = position == 1

            Log.d("ProfileActivity", "switchSelection: $position")

            if (position == 0) {
                profileTextView.setTextColor(
                    ContextCompat.getColor(
                        this@ProfileActivity,
                        R.color.blueJeans
                    )
                )
                interestTextView.setTextColor(
                    ContextCompat.getColor(
                        this@ProfileActivity,
                        R.color.black
                    )
                )

//                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, ProfileFragment()).commit()

                var profileFragment =
                    supportFragmentManager.findFragmentByTag("ProfileFragment") as? ProfileFragment

                if (profileFragment == null) {
                    profileFragment = ProfileFragment()
                }

                profileFragment.onItemSelectedListener = { hasSelectedItems ->
//                    toolbar.menu.findItem(R.id.action_delete).isVisible = hasSelectedItems
                }

                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.fragment_container, profileFragment, "ProfileFragment")
                transaction.commit()

            } else {
                profileTextView.setTextColor(
                    ContextCompat.getColor(
                        this@ProfileActivity,
                        R.color.black
                    )
                )
                interestTextView.setTextColor(
                    ContextCompat.getColor(
                        this@ProfileActivity,
                        R.color.blueJeans
                    )
                )
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, CategoryFragment()).commit()
            }
        }
    }
}