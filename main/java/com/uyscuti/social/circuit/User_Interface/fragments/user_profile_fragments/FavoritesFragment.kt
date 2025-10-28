package com.uyscuti.social.circuit.User_Interface.fragments.user_profile_fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.adapter.PaginatedAdapter
import com.uyscuti.social.circuit.adapter.ShortsUserProfileAdapter
import com.uyscuti.social.circuit.model.GoToUserProfileShortsPlayerFragment
import com.uyscuti.social.circuit.model.UserProfileShortsViewModel
import com.uyscuti.social.core.common.data.room.entity.UserShortsEntity
import com.uyscuti.social.core.models.BookmarkedShortsEntity
import com.uyscuti.social.network.api.response.getfavoriteshorts.BookmarkedPost
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject


private const val TAG = "FavoritesFragment"
@AndroidEntryPoint
class FavoritesFragment : Fragment(), ShortsUserProfileAdapter.ThumbnailClickListener {

    @Inject
    lateinit var retrofitIns: RetrofitInstance

    private lateinit var shortsAdapter: ShortsUserProfileAdapter
    private lateinit var recyclerViewAdapter: RecyclerViewAdapter
    private var shortsList = ArrayList<String>()

    private var shortsProfile = ArrayList<UserShortsEntity>()
    private lateinit var profileShortsAdapterRecyclerView: RecyclerView

    private val viewModel: UserProfileShortsViewModel by activityViewModels()
    private var storedShortsList: List<UserShortsEntity> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_favorites, container, false)

        profileShortsAdapterRecyclerView = view.findViewById(R.id.userShortsRecyclerView)

//        initializeShortsViewModel()

        lifecycleScope.launch {
//            val lastPage = getLastPage(1)
//            Log.d(TAG, "onCreateView: last page $lastPage")
        }



        shortsAdapter = ShortsUserProfileAdapter(this)
        shortsAdapter.recyclerView = profileShortsAdapterRecyclerView
        profileShortsAdapterRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)

        shortsAdapter.setOnPaginationListener(object : PaginatedAdapter.OnPaginationListener {
            override fun onCurrentPage(page: Int) {
//                Toast.makeText(requireContext(), "Page $page loaded!", Toast.LENGTH_SHORT).show()
            }

            override fun onNextPage(page: Int) {
                lifecycleScope.launch(Dispatchers.Main) {
//                    loadMoreShorts(page)
//                    userShorts(page)
                    getUserProfileFavoriteShorts(page)
//                    Log.d(TAG, "onNextPage: $page")
                }
            }

            override fun onFinish() {
//                Toast.makeText(requireContext(), "finish", Toast.LENGTH_SHORT).show()
            }
        })

        lifecycleScope.launch(Dispatchers.Main) {
            if (!viewModel.isResuming) {
//                Log.d(TAG, "onResume: is not resuming")
                getUserProfileFavoriteShorts(shortsAdapter.startPage)
            } else if(viewModel.isResuming && viewModel.mutableFavoriteShortsList.isEmpty()) {
                getUserProfileFavoriteShorts(shortsAdapter.startPage)
            }else {
//                Log.d(TAG, "onResume: is resuming ${viewModel.isResuming}")
                if(viewModel.shortsToRemove.isNotEmpty()) {
                    Log.d(TAG, "onCreateView: Remove un favorite shorts")
                    // Remove shorts specified in shortsToRemove list
                    viewModel.mutableFavoriteShortsList.removeAll(viewModel.shortsToRemove)

                    viewModel.shortsToRemove.clear()
                    // Update the adapter and profile list
                    shortsAdapter.submitItems(viewModel.mutableFavoriteShortsList)
                    shortsProfile.addAll(viewModel.mutableFavoriteShortsList)
                }else {
                    Log.d(TAG, "onCreateView: all favorite shorts")
                    shortsAdapter.submitItems(viewModel.mutableFavoriteShortsList)
                    shortsProfile.addAll(viewModel.mutableFavoriteShortsList)
                }

                Log.d(
                    TAG,
                    "onResume: view model mutable mutableFavoriteShortsList list size: ${viewModel.mutableFavoriteShortsList.size}"
                )
            }
        }
        

        return view

    }
//    private fun userShorts(page: Int) {
//        Log.d(TAG, "userShorts:  invoke getUserProfileShorts $page")
//
//        viewModel.getUserProfileFavoriteShorts(page)
////        sharedViewModel.getShorts(page)
//    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: OnResume")

        Log.d(TAG, "onResume: shorts to remove ${viewModel.shortsToRemove.size}")
        viewModel.mutableFavoriteShortsList.removeAll(viewModel.shortsToRemove)
        viewModel.shortsToRemove.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        lifecycleScope.launch {
            viewModel.isResuming = true
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        lifecycleScope.launch {
            viewModel.isResuming = true
        }
    }
    private fun serverResponseToBookmarkedPost(serverResponse: List<BookmarkedPost>): List<BookmarkedShortsEntity> {
        return serverResponse.map { serverResponseItem ->
            BookmarkedShortsEntity(
                __v = serverResponseItem.__v,
                _id = serverResponseItem._id,
                content = serverResponseItem.content,
                author = serverResponseItem.author,
                comments = serverResponseItem.comments,
                createdAt = serverResponseItem.createdAt,
                images = serverResponseItem.images,
                isBookmarked = serverResponseItem.isBookmarked,
                isLiked = serverResponseItem.isLiked,
                likes = serverResponseItem.likes,
                tags = serverResponseItem.tags,
                updatedAt = serverResponseItem.updatedAt,
                thumbnail = serverResponseItem.thumbnail
                // map other properties...
            )
        }
    }

    fun getUserProfileFavoriteShorts(page: Int) {


        lifecycleScope.launch(Dispatchers.IO) {

            try {
                val response = retrofitIns.apiService.getFavoriteShorts(page.toString())
                val responseBody = response.body()
                val shortsEntity =
                    responseBody?.data?.bookmarkedPosts?.let { serverResponseToUserEntity(it) }

//                val uniqueShortsList = shortsEntity!!.distinct()
//
                // Check if the new data contains items already in the existing list
//                val filteredNewItems = uniqueShortsList.filter { !viewModel.mutableFavoriteShortsList.contains(it) }

                // Add the new and unique items to the existing list

                val uniqueShortsList = shortsEntity!!.distinctBy { it._id }

// Check if the new data contains items already in the existing list
                val filteredNewItems = uniqueShortsList.filter { newItem ->
                    viewModel.mutableFavoriteShortsList.none { existingItem -> existingItem._id == newItem._id }
                }
                withContext(Dispatchers.Main) {
                    viewModel.mutableFavoriteShortsList.addAll(filteredNewItems)
                    shortsAdapter.submitItems(filteredNewItems)
                    shortsProfile.addAll(filteredNewItems)
                }

            } catch (e: Exception) {
                Log.e("UserProfileShortsViewModel", "Exception: ${e.message}")
                lifecycleScope.launch {
                    Toast.makeText(requireContext(), "Error connecting to server.....check internet connection", Toast.LENGTH_LONG).show()
                }
                e.printStackTrace()
            }

        }
    }

    private suspend fun getLastPage(page: Int): Int {
        return withContext(Dispatchers.IO) {
            try {
                val response = retrofitIns.apiService.getFavoriteShorts(page.toString())
                val responseBody = response.body()

                responseBody?.data?.totalPages ?: 1 // Return 1 if totalPages is null
            } catch (e: Exception) {
                Log.e("UserProfileShortsViewModel", "Exception: ${e.message}")
                // Handle the exception (optional)
                1 // Return 1 in case of an error
            }
        }
    }


    private fun serverResponseToUserEntity(serverResponse: List<BookmarkedPost>): List<UserShortsEntity> {
        return serverResponse.map { serverResponseItem ->
            UserShortsEntity(
                __v = serverResponseItem.__v,
                _id = serverResponseItem._id,
                content = serverResponseItem.content,
                author = serverResponseItem.author,
                comments = serverResponseItem.comments,
                createdAt = serverResponseItem.createdAt,
                images = serverResponseItem.images,
                isBookmarked = serverResponseItem.isBookmarked,
                isLiked = serverResponseItem.isLiked,
                likes = serverResponseItem.likes,
                tags = serverResponseItem.tags,
                updatedAt = serverResponseItem.updatedAt,
                thumbnail = serverResponseItem.thumbnail
                // map other properties...
            )
        }
    }



    companion object;

    override fun onUserProfileShortClick(shortsEntity: UserShortsEntity) {

//        val clickedShort = shortsEntity.images[0].url
//        Log.d(TAG, "onUserProfileShortClick: ${storedShortsList.size}")
//        shortsEntity.
//        val intent = Intent(activity, UserProfileShortsPlayerActivity::class.java)
////        intent.putExtra("theClickedShort", clickedShort)
//        intent.putExtra(UserProfileShortsPlayerActivity.CLICKED_SHORT, shortsEntity)
//
//        intent.putExtra(UserProfileShortsPlayerActivity.SHORTS_LIST, shortsProfile)
////        intent.putExtra("userShortsEntity", storedShortsList)
//        startActivity(intent)

//        EventBus.getDefault().post(UserProfileShortsOnClickEvent(storedShortsList))
        EventBus.getDefault().post(GoToUserProfileShortsPlayerFragment(shortsProfile, shortsEntity, true))

    }

}