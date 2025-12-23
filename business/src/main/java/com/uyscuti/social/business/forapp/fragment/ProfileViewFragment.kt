package com.uyscuti.social.business.forapp.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.business.adapter.MediaPagerAdapter
import com.uyscuti.social.business.adapter.ProfileViewAdapter
import com.uyscuti.social.business.model.Catalogue
import com.uyscuti.social.business.model.User

import com.uyscuti.social.business.R
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject


@AndroidEntryPoint
class ProfileViewFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var profileViewAdapter: ProfileViewAdapter
    private lateinit var businessProfileNotFound: TextView
    private lateinit var mediaViewAdapter: MediaPagerAdapter

    @Inject
    lateinit var retrofitInterface: RetrofitInstance
    private lateinit var user: User


    companion object {
        fun newInstance(user: User?): ProfileViewFragment {
            val fragment = ProfileViewFragment()
            val args = Bundle()
            args.putSerializable("user", user)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
//            user = it.getParcelable("user")!!
            user = it.getSerializable("user") as User
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.activity_profile_view, container, false)

        recyclerView = view.findViewById(R.id.image_recycler_view)
        businessProfileNotFound = view.findViewById(R.id.businessProfileNotFound)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        profileViewAdapter = ProfileViewAdapter(requireActivity())
        recyclerView.adapter = profileViewAdapter
        Log.d("onViewCreated", "onCreateView called")

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        Log.d("onViewCreated", "onViewCreated called")
        val userId = user._id
        val userName = user.username
        val avatar = user.avatar

        userId

        fetchUserProfile(userId.toString(), userName, avatar.toString())
        fetchUserCatalogue(userId.toString())
    }

    private fun fetchUserProfile(userId: String, userName: String?, avatar: String?) {

        Log.d("onViewCreated", "fetchUserProfile called $userId")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Fetch business profile
                val profileResponse = retrofitInterface.apiService.getUserBusinessProfile(userId)

                withContext(Dispatchers.Main) {
                    if (profileResponse.isSuccessful) {
                        val businessProfile = profileResponse.body()
                        if (businessProfile != null) {
                            // Update UI with the business profile
                            profileViewAdapter.setBusinessProfile(businessProfile)
                            profileViewAdapter.setNameAndAvatar(userName ?: "", avatar ?: "")
                        } else {

                            Log.e("ApiService", "No business profile found")
                            businessProfileNotFound.visibility = View.VISIBLE
                            recyclerView.visibility = View.GONE
                            // Show toast message on main thread
                            Toast.makeText(requireContext(), "No business profile found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e("ApiService", "Failed to get business profile: ${profileResponse.message()}")

                    }
                }
            } catch (e: HttpException) {
                Log.e("ApiService", "Failed to fetch data: ${e.message}", e)

            } catch (e: Throwable) {
                Log.e("ApiService", "Network error: ${e.message}", e)
                // Show toast message on main thread
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun fetchUserCatalogue(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Fetch business catalogue
                val catalogueResponse = retrofitInterface.apiService.getUserBusinessCatalogue(userId)

                withContext(Dispatchers.Main) {
                    if (catalogueResponse.isSuccessful) {
                        val businessCatalogue = catalogueResponse.body()?.data
                        val catalogueList = arrayListOf<Catalogue>()

                        businessCatalogue?.let {
                            if (it.products.isNotEmpty()) {
                                for (product in it.products) {
                                    val catalogue = Catalogue(
                                        product._id,
                                        product.itemName,
                                        product.description,
                                        product.price,
                                        product.images
                                    )
                                    catalogueList.add(catalogue)
                                }
                            }
                        }

                        // Update UI with catalogue list
                        if (catalogueList.isNotEmpty()) {
                            profileViewAdapter.setCatalogueList(catalogueList)
                        }else {

                        }
                    }else
                    {
                        Log.e("ApiService", "Failed to get business catalogue: ${catalogueResponse.message()}")

                    }
                }
            } catch (e: HttpException) {
                Log.e("ApiService", "Failed to fetch data: ${e.message}", e)

            } catch (e: Throwable) {
                Log.e("ApiService", "Network error: ${e.message}", e)
                // Show toast message on main thread
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
