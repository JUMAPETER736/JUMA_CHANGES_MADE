package com.uyscuti.social.business.forapp.fragment

import android.Manifest
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
import android.widget.Toast
import androidx.annotation.RequiresExtension
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.uyscuti.social.business.forapp.viewmodel.BusinessViewModel
import com.uyscuti.social.business.fragment.ProfileFragment
import com.uyscuti.social.business.room.database.BusinessDatabase
import com.uyscuti.social.business.room.repository.BusinessRepository
import com.uyscuti.social.business.R
import com.uyscuti.social.business.adapter.BusinessAdapter
import com.uyscuti.social.business.interfaces.BottomNavController
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
class BusinessProfileEditFragment : Fragment(), BottomNavController {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var adapter: BusinessAdapter


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

        val view = inflater.inflate(R.layout.activity_profile, container, false)
        viewModel = ViewModelProvider(this)[BusinessViewModel::class.java]


        tabLayout = view.findViewById(R.id.tab_layout)
        viewPager = view.findViewById(R.id.viewPager)

        adapter = BusinessAdapter(childFragmentManager)

        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 1


        tabLayout.setupWithViewPager(viewPager)


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = requireContext().getSharedPreferences("LoginPrefs", MODE_PRIVATE)


        Log.d("onViewCreated", "onViewCreated: ")

        setHasOptionsMenu(true)



    }

    override fun onResume() {
        super.onResume()
        Log.d("onResume", "switchSelection ${viewModel.getValue()}")

    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
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


        val selectedIds = emptyList<String>()

        if (selectedIds != null) {

            CoroutineScope(Dispatchers.IO).launch {
                deleteProductsRequest(selectedIds)

                for (id in selectedIds) {

                    profileFragment?.deleteMyProduct(id)

                }

            }
        }
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

    override fun navigateToChildFragments(childFragmentPosition: Int) {
        tabLayout.getTabAt(childFragmentPosition)?.select()
    }

    fun getProfileFragment() = adapter.getProfileFragment()

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