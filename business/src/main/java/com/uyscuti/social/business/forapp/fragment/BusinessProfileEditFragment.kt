package com.uyscuti.social.business.forapp.fragment

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.net.http.HttpException
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
//import android.widget.Toolbar
import androidx.annotation.RequiresExtension
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.business.adapter.ProfileAdapter
import com.uyscuti.social.business.forapp.viewmodel.BusinessViewModel
import com.uyscuti.social.business.fragment.CategoryFragment
import com.uyscuti.social.business.fragment.ProfileFragment
import com.uyscuti.social.business.model.Catalogue
import com.uyscuti.social.business.room.database.BusinessDatabase
import com.uyscuti.social.business.room.repository.BusinessRepository
import com.uyscuti.social.business.util.ImagePicker
import com.uyscuti.social.business.R
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


@AndroidEntryPoint
class BusinessProfileEditFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

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

    @Inject
    lateinit var retrofitInterface: RetrofitInstance

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var businessDatabase: BusinessDatabase
    private lateinit var businessRepository: BusinessRepository

    private val API_TAG = "ApiService"
    private lateinit var viewModel: BusinessViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private var opened = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_business_profile_edit, container, false)
        val view = inflater.inflate(R.layout.activity_profile, container, false)
        viewModel = ViewModelProvider(this)[BusinessViewModel::class.java]
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = requireContext().getSharedPreferences("LoginPrefs", MODE_PRIVATE)
        // Initialize views and set up listeners here

//        toolbar = view.findViewById(R.id.toolbar)
//        val activity = requireActivity() as AppCompatActivity
//        activity.setSupportActionBar(toolbar)
//        toolbar.title = "Business Profile"
//        toolbar.setNavigationIcon(R.drawable.baseline_chevron_left_24)
//        setSupportActionBar(toolbar)
        // Initialize other views similarly

        // Set up toolbar and other UI components as in your ProfileActivity
        Log.d("onViewCreated", "onViewCreated: ")
        fragmentContainerView = view.findViewById(R.id.fragment_container)

        profileTabFrame = view.findViewById(R.id.profileTab)
        profileTabFrame.isSelected = true

        interestTabFrame = view.findViewById(R.id.interestsTab)
        interestTabFrame.isSelected = false

        profileTextView = view.findViewById(R.id.profileText)
        interestTextView = view.findViewById(R.id.interestsText)
        setHasOptionsMenu(true)
        switchSelection(0)

        profileTextView.setOnClickListener {
            viewModel.setValue(true)
            switchSelection(0)
        }

        interestTextView.setOnClickListener {
            switchSelection(1)
        }

        // Set up RecyclerView, adapters, and other UI components as needed

    }

    override fun onResume() {
        super.onResume()
        Log.d("onResume", "switchSelection ${viewModel.getValue()}")
        if (viewModel.getValue()) {
            switchSelection(0)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {

            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                val profileFragment =
                    requireActivity().supportFragmentManager.findFragmentByTag("ProfileFragment") as? ProfileFragment
                profileFragment?.getCoordinates()
            } else {
                Toast.makeText(requireActivity(), "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Deprecated("Deprecated in Java")
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
                        requireActivity().supportFragmentManager.findFragmentByTag("ProfileFragment") as? ProfileFragment

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
                    requireActivity().supportFragmentManager.findFragmentByTag("ProfileFragment") as? ProfileFragment


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
        val cursor = requireActivity().contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                return it.getString(columnIndex)
            }
        }
        return null
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.catalogue_menu, menu)
//        menu.findItem(R.id.action_delete).isVisible = false
//        return true
//    }

    //    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            R.id.action_delete -> {
//                deleteSelectedItems()
//                true
//            }
//
//            else -> super.onOptionsItemSelected(item)
//        }
//    }
    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.catalogue_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java")
    override fun onPrepareOptionsMenu(menu: Menu) {
        // Hide or show menu items based on your logic
        menu.findItem(R.id.action_delete).isVisible = false
        super.onPrepareOptionsMenu(menu)
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    @Deprecated("Deprecated in Java")
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
            requireActivity().supportFragmentManager.findFragmentByTag("ProfileFragment") as? ProfileFragment


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

    private fun switchSelection(position: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            profileTabFrame.isSelected = position == 0
            interestTabFrame.isSelected = position == 1

            Log.d("ProfileActivity", "switchSelection: $position")

            if (position == 0) {
                profileTextView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.blueJeans
                    )
                )
                interestTextView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )

//                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, ProfileFragment()).commit()

                var profileFragment =
                    requireActivity().supportFragmentManager.findFragmentByTag("ProfileFragment") as? ProfileFragment

                if (profileFragment == null) {
                    profileFragment = ProfileFragment()
                }

                profileFragment.onItemSelectedListener = { hasSelectedItems ->
//                    toolbar.menu.findItem(R.id.action_delete).isVisible = hasSelectedItems
                }

                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                transaction.replace(R.id.fragment_container, profileFragment, "ProfileFragment")
                transaction.commit()

            } else {
                profileTextView.setTextColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.black
                    )
                )
                interestTextView.setTextColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.blueJeans
                    )
                )
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, CategoryFragment()).commit()
            }
        }
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

    companion object {

        private const val REQUEST_LOCATION_PERMISSION = 1

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BusinessProfileEditFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BusinessProfileEditFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}