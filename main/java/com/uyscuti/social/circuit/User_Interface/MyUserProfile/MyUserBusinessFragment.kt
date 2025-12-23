package com.uyscuti.social.circuit.User_Interface.MyUserProfile

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
import com.uyscuti.social.circuit.databinding.MyUserBusinessFragmentBinding
import com.uyscuti.social.business.model.Catalogue
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject

@AndroidEntryPoint
class MyUserBusinessFragment : Fragment() {

    companion object {
        private const val TAG = "MyUsersBusinessFragment"
        private const val ARG_USER_ID = "userId"
        private const val ARG_USERNAME = "username"

        // Static cache that survives fragment recreation
        private val businessCache = mutableMapOf<String, CachedBusinessData>()

        // Cache duration: 5 minutes
        private const val CACHE_DURATION_MS = 5 * 60 * 1000L

        data class CachedBusinessData(
            val catalogueList: ArrayList<Catalogue>,
            val timestamp: Long,
            val hasProfile: Boolean
        ) {
            fun isValid(): Boolean {
                return (System.currentTimeMillis() - timestamp) < CACHE_DURATION_MS
            }
        }

        fun newInstance(userId: String, username: String): MyUserBusinessFragment {
            return MyUserBusinessFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_USER_ID, userId)
                    putString(ARG_USERNAME, username)
                }
            }
        }

        fun clearCache(userId: String) {
            businessCache.remove(userId)
            Log.d(TAG, "Cache cleared for userId: $userId")
        }
    }

    private var _binding: MyUserBusinessFragmentBinding? = null
    private val binding get() = _binding!!

    private var userId: String? = null
    private var username: String? = null
    private var isLoading = false

    @Inject
    lateinit var retrofitInstance: RetrofitInstance

    private lateinit var catalogueAdapter: CatalogueAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString(ARG_USER_ID)
            username = it.getString(ARG_USERNAME)
        }
        if (userId.isNullOrEmpty()) {
            throw IllegalArgumentException("userId is required")
        }

        // Load from cache IMMEDIATELY in onCreate
        val cacheKey = getCacheKey()
        businessCache[cacheKey]?.let { cached ->
            if (cached.isValid()) {
                Log.d(TAG, "✓ Cache HIT in onCreate! Loading ${cached.catalogueList.size} items instantly")
            } else {
                Log.d(TAG, "Cache expired, will fetch fresh data")
                businessCache.remove(cacheKey)
            }
        }
    }

    private fun getCacheKey(): String = userId ?: ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MyUserBusinessFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        val cacheKey = getCacheKey()
        val cached = businessCache[cacheKey]

        if (cached != null && cached.isValid()) {
            // INSTANT LOAD from cache
            Log.d(TAG, "🚀 INSTANT LOAD - Displaying ${cached.catalogueList.size} cached items")

            displayCachedData(cached.catalogueList)

            // Optional: Background refresh if needed
            if (shouldBackgroundRefresh(cached)) {
                Log.d(TAG, "Background refresh starting...")
                backgroundRefresh()
            }
        } else {
            // Fresh load
            Log.d(TAG, "Fresh load starting...")
            fetchBusinessData()
        }
    }

    private fun shouldBackgroundRefresh(cached: CachedBusinessData): Boolean {
        // Refresh if cache is older than 2.5 minutes (half of validity)
        val age = System.currentTimeMillis() - cached.timestamp
        return age > (CACHE_DURATION_MS / 2)
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.VERTICAL,
                false
            )

            // RecyclerView optimizations
            setHasFixedSize(true)
            setItemViewCacheSize(20)

            // Create initial adapter
            catalogueAdapter = CatalogueAdapter(requireActivity(), ArrayList())
            adapter = catalogueAdapter
        }

        Log.d(TAG, "RecyclerView setup complete with optimizations")
    }

    private fun displayCachedData(catalogueList: ArrayList<Catalogue>) {
        if (catalogueList.isEmpty()) {
            showEmptyState("No catalogue items found")
        } else {
            showContent()
            catalogueAdapter = CatalogueAdapter(requireActivity(), catalogueList)
            binding.recyclerView.adapter = catalogueAdapter
            Log.d(TAG, "✓ Displayed ${catalogueList.size} cached items")
        }
    }

    private fun backgroundRefresh() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Fetch profile silently
                val profileResponse = retrofitInstance.apiService.getUserBusinessProfile(userId!!)
                val hasProfile = profileResponse.isSuccessful && profileResponse.body() != null

                // Fetch catalogue silently
                val catalogueResponse = retrofitInstance.apiService.getUserBusinessCatalogue(userId!!)

                if (catalogueResponse.isSuccessful) {
                    val businessCatalogue = catalogueResponse.body()?.data
                    val catalogueList = ArrayList<Catalogue>()

                    businessCatalogue?.products?.forEach { product ->
                        val catalogue = Catalogue(
                            product._id,
                            product.itemName,
                            product.description,
                            product.price,
                            product.images ?: emptyList()
                        )
                        catalogueList.add(catalogue)
                    }

                    // Update cache silently
                    val cacheKey = getCacheKey()
                    businessCache[cacheKey] = CachedBusinessData(
                        catalogueList = catalogueList,
                        timestamp = System.currentTimeMillis(),
                        hasProfile = hasProfile
                    )

                    Log.d(TAG, "✓ Background refresh complete: ${catalogueList.size} items")

                    // Only update UI if data changed significantly
                    withContext(Dispatchers.Main) {
                        val currentAdapter = binding.recyclerView.adapter as? CatalogueAdapter
                        if (currentAdapter?.itemCount != catalogueList.size) {
                            displayCachedData(catalogueList)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Background refresh error: ${e.message}")
                // Silently fail - user still has cached data
            }
        }
    }

    private fun fetchBusinessData() {
        if (isLoading) {
            Log.d(TAG, "Already loading, skipping...")
            return
        }

        isLoading = true
        Log.d(TAG, "Fetching business data for $userId")

        showLoading()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Fetch profile
                val profileResponse = retrofitInstance.apiService.getUserBusinessProfile(userId!!)
                val hasProfile = profileResponse.isSuccessful && profileResponse.body() != null

                if (!hasProfile) {
                    withContext(Dispatchers.Main) {
                        val errorMsg = parseErrorMessage(profileResponse)
                        Log.e(TAG, "Failed to load user profile: $errorMsg")
                        handleProfileError(errorMsg)
                        hideLoading()
                        isLoading = false
                    }
                    return@launch
                }

                // Fetch catalogue
                val catalogueResponse = retrofitInstance.apiService.getUserBusinessCatalogue(userId!!)

                withContext(Dispatchers.Main) {
                    if (catalogueResponse.isSuccessful) {
                        val businessCatalogue = catalogueResponse.body()?.data
                        val catalogueList = ArrayList<Catalogue>()

                        businessCatalogue?.products?.forEach { product ->
                            val catalogue = Catalogue(
                                product._id,
                                product.itemName,
                                product.description,
                                product.price,
                                product.images ?: emptyList()
                            )
                            catalogueList.add(catalogue)
                        }

                        Log.d(TAG, "✓ Loaded ${catalogueList.size} catalogue items")

                        // Cache the results
                        val cacheKey = getCacheKey()
                        businessCache[cacheKey] = CachedBusinessData(
                            catalogueList = catalogueList,
                            timestamp = System.currentTimeMillis(),
                            hasProfile = hasProfile
                        )

                        displayCachedData(catalogueList)
                    } else {
                        val errorMsg = parseErrorMessage(catalogueResponse)
                        Log.e(TAG, "Failed to load catalogue: $errorMsg")
                        handleCatalogueError(errorMsg)
                    }

                    hideLoading()
                    isLoading = false
                }

            } catch (e: HttpException) {
                Log.e(TAG, "HTTP error fetching data: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    handleCatalogueError("Server error: ${e.message}")
                    hideLoading()
                    isLoading = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching data", e)
                withContext(Dispatchers.Main) {
                    handleCatalogueError("Error: ${e.localizedMessage}")
                    hideLoading()
                    isLoading = false
                }
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
        binding.progressBar.visibility = View.GONE
        binding.emptyView.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
    }

    private fun showEmptyState(message: String? = null) {
        binding.progressBar.visibility = View.GONE
        binding.emptyView.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE

        catalogueAdapter = CatalogueAdapter(requireActivity(), ArrayList())
        binding.recyclerView.adapter = catalogueAdapter
    }

    private fun parseErrorMessage(response: retrofit2.Response<*>?): String {
        return response?.let {
            try {
                val errorBody = it.errorBody()?.string()
                if (!errorBody.isNullOrEmpty()) {
                    org.json.JSONObject(errorBody).optString("message")
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
        // Don't clear cache - it's static and should persist
    }
}