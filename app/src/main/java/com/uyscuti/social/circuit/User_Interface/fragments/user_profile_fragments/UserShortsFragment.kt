package com.uyscuti.social.circuit.User_Interface.fragments.user_profile_fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
//import com.softrunapps.paginatedrecyclerview.PaginatedAdapter.OnPaginationListener
import com.uyscuti.social.circuit.adapter.ShortsUserProfileAdapter
import com.uyscuti.social.circuit.model.GoToUserProfileShortsPlayerFragment
import com.uyscuti.social.circuit.model.UserProfileShortsStartGet
import com.uyscuti.social.circuit.model.UserProfileShortsViewModel
import com.uyscuti.social.circuit.service.VideoPreLoadingService
import com.uyscuti.social.circuit.User_Interface.fragments.SHORTS
import com.uyscuti.social.circuit.utils.Constants
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.adapter.PaginatedAdapter
import com.uyscuti.social.core.common.data.room.entity.UserShortsEntity
import com.uyscuti.social.network.api.response.getallshorts.Post
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UserShortsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class UserShortsFragment : Fragment(), ShortsUserProfileAdapter.ThumbnailClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    @Inject
    lateinit var retrofitIns: RetrofitInstance
    private lateinit var shortsAdapter: ShortsUserProfileAdapter
    private lateinit var recyclerViewAdapter: RecyclerViewAdapter
    private var shortsList = ArrayList<String>()

    private var shortsProfile = ArrayList<UserShortsEntity>()
    private lateinit var profileShortsAdapterRecyclerView: RecyclerView

    private val viewModel: UserProfileShortsViewModel by activityViewModels()

    private var storedShortsList: List<UserShortsEntity> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        EventBus.getDefault().register(this)

    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_user_shorts, container, false)


        profileShortsAdapterRecyclerView = view.findViewById(R.id.userShortsRecyclerView)

//        initializeShortsViewModel()


        shortsAdapter = ShortsUserProfileAdapter(this)
        shortsAdapter.recyclerView = profileShortsAdapterRecyclerView
        profileShortsAdapterRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
//        var pages = 0
        shortsAdapter.setOnPaginationListener(object : PaginatedAdapter.OnPaginationListener {
            override fun onCurrentPage(page: Int) {
//                Toast.makeText(requireContext(), "Page $page loaded!", Toast.LENGTH_SHORT).show()
//                Log.d(TAG, "onCurrentPage: $page")
//                pages = page + 1
            }

            override fun onNextPage(page: Int) {
                lifecycleScope.launch(Dispatchers.Main) {
//                    loadMoreShorts(page)
//                    if(!viewModel.isResuming) {
////                        userShorts(page)
//                        getUserProfileShorts(page)
//                    }
                    getUserProfileShorts(page)

//                    Log.d(TAG, "onNextPage: $page")
                }
            }

            override fun onFinish() {
//                Log.d(TAG, "onFinish: last page")
//                Log.d(TAG, "onFinish: last pages $pages")
//                getUserProfileShorts(pages)
//                onNextPage(pages)

//                Toast.makeText(requireContext(), "finish", Toast.LENGTH_SHORT).show()
            }
        })


        lifecycleScope.launch(Dispatchers.Main) {
            if (!viewModel.isResuming) {
                getUserProfileShorts(shortsAdapter.startPage)
            }else if(viewModel.isResuming && viewModel.mutableShortsList.isEmpty()) {
                getUserProfileShorts(shortsAdapter.startPage)
            }
            else {
                Log.d(TAG, "onResume: is resuming ${viewModel.isResuming}")
                shortsAdapter.submitItems(viewModel.mutableShortsList)
                shortsProfile.addAll(viewModel.mutableShortsList)
                Log.d(
                    TAG,
                    "onResume: view model mutable shorts list size: ${viewModel.mutableShortsList.size}"
                )
            }
        }
        return view
    }

//    @SuppressLint("NotifyDataSetChanged")
//    private fun initializeShortsViewModel() {
//        Log.d(TAG, "initializeShortsViewModel: is not resuming")
//        viewModel.getUserProfileShortsObserver().observe(
//            viewLifecycleOwner
//        ) { shortsList ->
//            if (shortsList != null) {
//
//
//                // Remove duplicates from the new data
//                val uniqueShortsList = shortsList.distinct()
//
//                // Check if the new data contains items already in the existing list
//                val filteredNewItems = uniqueShortsList.filter { !viewModel.mutableShortsList.contains(it) }
//
//                // Add the new and unique items to the existing list
//                viewModel.mutableShortsList.addAll(filteredNewItems)
//
//                // Submit the updated list to the adapter
//                shortsAdapter.clear()
//                shortsAdapter.submitItems(viewModel.mutableShortsList)
//
//                // If you still want to maintain a separate list (shortsProfile), update it accordingly
//                shortsProfile.clear()
//                shortsProfile.addAll(viewModel.mutableShortsList)
//
//                Log.d(TAG, "initializeShortsViewModel: shorts list size: ${shortsList.size}")
//                Log.d(TAG, "initializeShortsViewModel: mutable shorts list size: ${viewModel.mutableShortsList.size}")
//                Log.d(TAG, "initializeShortsViewModel: shorts profile size after add: ${shortsProfile.size}")
//
//            }
//            Log.d(
//                TAG,
//                "initializeShortsViewModel: shorts profile size after add: ${shortsProfile.size}"
//            )
//
//        }
//        viewModel.getOnErrorFeedBackObserver().observe(viewLifecycleOwner) { onErrorFeedback ->
//            MotionToast.createToast(
//                requireActivity(),
//                "Failed To Retrieve Data☹️",
//                onErrorFeedback,
//                MotionToastStyle.ERROR,
//                MotionToast.GRAVITY_BOTTOM,
//                MotionToast.LONG_DURATION,
//                ResourcesCompat.getFont(requireActivity(), R.font.helvetica_regular)
//            )
//        }
//
//
//    }

//    private fun userShorts(page: Int) {
//        Log.d(TAG, "userShorts:  invoke getUserProfileShorts $page")
//
//        viewModel.getUserProfileShorts(page)
//    }

    private fun serverResponseToUserEntity(serverResponse: List<Post>): List<UserShortsEntity> {
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

    fun getUserProfileShorts(page: Int) {

        Log.d(TAG, "getUserProfileShorts: current page is $page")

        lifecycleScope.launch(Dispatchers.IO) {

            try {
                val response = retrofitIns.apiService.myShorts(page.toString())
                val responseBody = response.body()
                val shortsEntity = responseBody?.data?.posts?.let { serverResponseToUserEntity(it) }

                val hasNextPage = responseBody!!.data.hasNextPage
                Log.d(TAG, "getUserProfileShorts: has next page $hasNextPage")

                if (shortsEntity != null) {
                    withContext(Dispatchers.Main) {

//                        val uniqueShortsList = shortsEntity.distinct()
////
//                        // Check if the new data contains items already in the existing list
//                        val filteredNewItems = uniqueShortsList.filter { !viewModel.mutableShortsList.contains(it) }
//
//                        // Add the new and unique items to the existing list
//                        viewModel.mutableShortsList.addAll(filteredNewItems)
//
                        // Assuming ShortsEntity has an 'id' property
                        val uniqueShortsList = shortsEntity.distinctBy { it._id }

// Check if the new data contains items already in the existing list
                        val filteredNewItems = uniqueShortsList.filter { newItem ->
                            viewModel.mutableShortsList.none { existingItem -> existingItem._id == newItem._id }
                        }
                        viewModel.mutableShortsList.addAll(filteredNewItems)
                        shortsAdapter.submitItems(filteredNewItems)
                        shortsProfile.addAll(filteredNewItems)
                    }

                }

                if (shortsEntity == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "User Data is empty", Toast.LENGTH_LONG)
                            .show()
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Exception: ${e.message}")

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Exception: ${e.message}", Toast.LENGTH_LONG)
                        .show()
                }

                e.printStackTrace()
            }

        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment UserShortsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UserShortsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    private fun startPreLoadingService() {
        Log.d(SHORTS, "Preloading called")
        val preloadingServiceIntent = Intent(requireContext(), VideoPreLoadingService::class.java)
        preloadingServiceIntent.putStringArrayListExtra(Constants.VIDEO_LIST, shortsList)
        requireContext().startService(preloadingServiceIntent)
    }

    val TAG = "UserShortsFragment"
    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        lifecycleScope.launch {
            viewModel.isResuming = true
        }
//        Log.d(TAG, "onDestroy: method")
    }


    override fun onDestroyView() {
        super.onDestroyView()
        lifecycleScope.launch {
            viewModel.isResuming = true
        }
//        Log.d(TAG, "onDestroyView: method")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUserProfileShorts(event: UserProfileShortsStartGet) {
//      loadMoreShorts(1)
        Log.d("onProgressEvent", "onProgressEvent: you can load shorts")
    }

    override fun onUserProfileShortClick(shortsEntity: UserShortsEntity) {

        val clickedShort = shortsEntity.images[0].url
//        Log.d(TAG, "onUserProfileShortClick: ${storedShortsList.size}")
////        shortsEntity.
//        val intent = Intent(activity, UserProfileShortsPlayerActivity::class.java)
////        intent.putExtra("theClickedShort", clickedShort)
//        intent.putExtra(UserProfileShortsPlayerActivity.CLICKED_SHORT, shortsEntity)
//
//        Log.d(TAG, "onUserProfileShortClick: shortsProfile size: ${shortsProfile.size}")
//        intent.putExtra(UserProfileShortsPlayerActivity.SHORTS_LIST, shortsProfile)
////        intent.putExtra("userShortsEntity", storedShortsList)
//        startActivity(intent)

//        EventBus.getDefault().post(UserProfileShortsOnClickEvent(storedShortsList))

//        Log.d(TAG, "onUserProfileShortClick: User thumbnail short clicked")
        EventBus.getDefault().post(GoToUserProfileShortsPlayerFragment(shortsProfile, shortsEntity, false))

    }
}

