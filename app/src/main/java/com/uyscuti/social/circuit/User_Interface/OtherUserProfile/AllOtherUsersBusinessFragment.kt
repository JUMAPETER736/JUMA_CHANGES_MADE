package com.uyscuti.social.circuit.User_Interface.OtherUserProfile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.business.adapter.CatalogueAdapter
import com.uyscuti.social.circuit.databinding.AllOtherUsersBusinessFragmentBinding
import com.uyscuti.social.business.model.Catalogue
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject

@AndroidEntryPoint
class AllOtherUsersBusinessFragment : Fragment() {

    private var _binding: AllOtherUsersBusinessFragmentBinding? = null
    private val binding get() = _binding!!

    private var userId: String? = null
    private var username: String? = null

    @Inject
    lateinit var retrofitInstance: RetrofitInstance

    private lateinit var catalogueAdapter: CatalogueAdapter

    // Cache management
    private var cachedCatalogue: List<Catalogue>? = null
    private var cacheTimestamp: Long = 0L
    private val CACHE_DURATION_MS = 5 * 60 * 1000L // 5 minutes

    // Loading state
    private var isLoading = false
    private var hasLoadedOnce = false

    companion object {
        private const val TAG = "AllOtherUsersBusinessFragment"
        private const val ARG_USER_ID = "userId"
        private const val ARG_USERNAME = "username"

        // Static cache shared across all instances (optional - for cross-fragment caching)
        private val staticCache = mutableMapOf<String, Pair<List<Catalogue>, Long>>()

        fun newInstance(userId: String, username: String): AllOtherUsersBusinessFragment {
            return AllOtherUsersBusinessFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_USER_ID, userId)
                    putString(ARG_USERNAME, username)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString(ARG_USER_ID)
            username = it.getString(ARG_USERNAME)
        }
        // Validate required args
        if (userId.isNullOrEmpty()) {
            throw IllegalArgumentException("userId is required")
        }

        // 🚀 SPEED BOOST: Check static cache immediately
        userId?.let { id ->
            staticCache[id]?.let { (catalogue, timestamp) ->
                if ((System.currentTimeMillis() - timestamp) < CACHE_DURATION_MS) {
                    cachedCatalogue = catalogue
                    cacheTimestamp = timestamp
                    hasLoadedOnce = true
                    Log.d(TAG, "⚡ Loaded from static cache in onCreate!")
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AllOtherUsersBusinessFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🚀 SPEED BOOST: Setup RecyclerView with optimizations
        setupRecyclerViewOptimized()

        // 🚀 SPEED BOOST: Show cached data IMMEDIATELY if available
        if (cachedCatalogue != null && isCacheValid()) {
            Log.d(TAG, "⚡ Instant display from cache!")
            displayCachedData()
            // Still refresh in background if cache is getting old
            if ((System.currentTimeMillis() - cacheTimestamp) > (CACHE_DURATION_MS / 2)) {
                Log.d(TAG, "🔄 Refreshing stale cache in background")
                fetchDataInBackground()
            }
        } else {
            // No cache, load fresh data
            fetchDataParallel()
        }
    }

    private fun setupRecyclerViewOptimized() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

            // 🚀 PERFORMANCE OPTIMIZATIONS
            setHasFixedSize(true) // Size won't change
            setItemViewCacheSize(20) // Cache more views

            // Optimize nested scrolling
            isNestedScrollingEnabled = true

            // RecycledViewPool for better recycling
            val viewPool = RecyclerView.RecycledViewPool()
            viewPool.setMaxRecycledViews(0, 15)
            setRecycledViewPool(viewPool)
        }

        catalogueAdapter = CatalogueAdapter(requireActivity(), ArrayList())
        binding.recyclerView.adapter = catalogueAdapter
        Log.d(TAG, "RecyclerView setup with optimizations")
    }

    private fun isCacheValid(): Boolean {
        val isValid = cachedCatalogue != null &&
                (System.currentTimeMillis() - cacheTimestamp) < CACHE_DURATION_MS
        return isValid
    }

    private fun displayCachedData() {
        cachedCatalogue?.let { catalogue ->
            if (catalogue.isEmpty()) {
                showEmptyState("No catalogue items found")
            } else {
                showContent()
                catalogueAdapter = CatalogueAdapter(requireActivity(), ArrayList(catalogue))
                binding.recyclerView.adapter = catalogueAdapter
                Log.d(TAG, "⚡ Displayed ${catalogue.size} cached items instantly")
            }
        }
    }

    // 🚀 PARALLEL LOADING: Fetch profile and catalogue simultaneously
    private fun fetchDataParallel() {
        if (userId.isNullOrEmpty() || isLoading) return

        Log.d(TAG, "⚡ Starting parallel data fetch")
        isLoading = true
        showLoading()

        lifecycleScope.launch {
            try {
                // Launch both requests in parallel
                val profileDeferred = async(Dispatchers.IO) {
                    retrofitInstance.apiService.getUserBusinessProfile(userId!!)
                }

                val catalogueDeferred = async(Dispatchers.IO) {
                    retrofitInstance.apiService.getUserBusinessCatalogue(userId!!)
                }

                // Wait for both to complete
                val profileResponse = profileDeferred.await()
                val catalogueResponse = catalogueDeferred.await()

                // Process profile response
                withContext(Dispatchers.Main) {
                    if (profileResponse.isSuccessful) {
                        val businessProfile = profileResponse.body()
                        if (businessProfile != null) {
                            Log.d(TAG, "✅ Profile loaded")
                        }
                    } else {
                        Log.e(TAG, "Profile error: ${parseErrorMessage(profileResponse)}")
                    }
                }

                // Process catalogue response
                withContext(Dispatchers.Main) {
                    if (catalogueResponse.isSuccessful) {
                        val businessCatalogue = catalogueResponse.body()?.data
                        val catalogueList = ArrayList<Catalogue>()

                        businessCatalogue?.products?.forEach { product ->
                            catalogueList.add(
                                Catalogue(
                                    product._id,
                                    product.itemName,
                                    product.description,
                                    product.price,
                                    product.images ?: emptyList()
                                )
                            )
                        }

                        Log.d(TAG, "✅ Loaded ${catalogueList.size} items in parallel")

                        // Update both instance and static cache
                        cachedCatalogue = catalogueList
                        cacheTimestamp = System.currentTimeMillis()
                        hasLoadedOnce = true

                        userId?.let { id ->
                            staticCache[id] = Pair(catalogueList, cacheTimestamp)
                        }

                        if (catalogueList.isEmpty()) {
                            showEmptyState("No catalogue items found")
                        } else {
                            showContent()
                            catalogueAdapter = CatalogueAdapter(requireActivity(), catalogueList)
                            binding.recyclerView.adapter = catalogueAdapter
                        }
                    } else {
                        val errorMsg = parseErrorMessage(catalogueResponse)
                        Log.e(TAG, "Catalogue error: $errorMsg")
                        handleCatalogueError(errorMsg)
                    }

                    hideLoading()
                    isLoading = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in parallel fetch", e)
                withContext(Dispatchers.Main) {
                    handleCatalogueError("Error: ${e.localizedMessage}")
                    hideLoading()
                    isLoading = false
                }
            }
        }
    }

    // 🚀 BACKGROUND REFRESH: Update cache without showing loading spinner
    private fun fetchDataInBackground() {
        if (userId.isNullOrEmpty() || isLoading) return

        isLoading = true
        Log.d(TAG, "🔄 Background refresh started")

        lifecycleScope.launch {
            try {
                val catalogueResponse = withContext(Dispatchers.IO) {
                    retrofitInstance.apiService.getUserBusinessCatalogue(userId!!)
                }

                withContext(Dispatchers.Main) {
                    if (catalogueResponse.isSuccessful) {
                        val businessCatalogue = catalogueResponse.body()?.data
                        val catalogueList = ArrayList<Catalogue>()

                        businessCatalogue?.products?.forEach { product ->
                            catalogueList.add(
                                Catalogue(
                                    product._id,
                                    product.itemName,
                                    product.description,
                                    product.price,
                                    product.images ?: emptyList()
                                )
                            )
                        }

                        // Silently update cache
                        cachedCatalogue = catalogueList
                        cacheTimestamp = System.currentTimeMillis()

                        userId?.let { id ->
                            staticCache[id] = Pair(catalogueList, cacheTimestamp)
                        }

                        // Update UI only if data changed
                        if (catalogueList != cachedCatalogue) {
                            catalogueAdapter = CatalogueAdapter(requireActivity(), catalogueList)
                            binding.recyclerView.adapter = catalogueAdapter
                            Log.d(TAG, "✅ Background refresh completed - UI updated")
                        } else {
                            Log.d(TAG, "✅ Background refresh completed - no changes")
                        }
                    }
                    isLoading = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Background refresh failed (silent)", e)
                isLoading = false
            }
        }
    }

    private fun handleProfileError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun handleCatalogueError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        showEmptyState(message)
    }

    private fun showLoading() {

        binding.emptyView.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showContent() {
        binding.emptyView.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
    }

    private fun showEmptyState(message: String? = null) {
        binding.emptyView.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.progressBar.visibility = View.GONE

        val emptyList = ArrayList<Catalogue>()
        catalogueAdapter = CatalogueAdapter(requireActivity(), emptyList)
        binding.recyclerView.adapter = catalogueAdapter
    }

    private fun parseErrorMessage(response: retrofit2.Response<*>?): String {
        return response?.let {
            try {
                val errorBody = it.errorBody()?.string()
                if (!errorBody.isNullOrEmpty()) {
                    org.json.JSONObject(errorBody).optString("message", "")
                } else {
                    ""
                }
            } catch (e: Exception) {
                ""
            }
        } ?: ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}