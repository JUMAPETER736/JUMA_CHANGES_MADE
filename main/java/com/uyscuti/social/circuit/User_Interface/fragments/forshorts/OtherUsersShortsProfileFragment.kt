package com.uyscuti.social.circuit.User_Interface.fragments.forshorts

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.GridLayoutManager
import com.uyscuti.social.circuit.adapter.PaginatedAdapter
import com.uyscuti.social.circuit.adapter.ShortsUserProfileAdapter
import com.uyscuti.social.circuit.interfaces.feedinterfaces.OnShortThumbnailClickListener
import com.uyscuti.social.circuit.User_Interface.fragments.user_profile_fragments.RecyclerViewAdapter
import com.uyscuti.social.circuit.User_Interface.shorts.UserProfileShortsPlayerActivity
import com.uyscuti.social.circuit.viewmodels.GetShortsByUsernameViewModel
import com.uyscuti.social.circuit.viewmodels.otherusersprofile.OtherUsersProfileViewModel
import com.uyscuti.social.circuit.databinding.FragmentOtherUsersShortsProfileBinding
import com.uyscuti.social.core.common.data.room.entity.UserShortsEntity
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "OtherUsersShortsProfileFragment"
@AndroidEntryPoint
class OtherUsersShortsProfileFragment : Fragment(), ShortsUserProfileAdapter.ThumbnailClickListener  {


    @Inject
    lateinit var retrofitIns: RetrofitInstance
    private lateinit var shortsAdapter: ShortsUserProfileAdapter
    private lateinit var recyclerViewAdapter: RecyclerViewAdapter
    private var shortsList = ArrayList<String>()

    private var shortsProfile = ArrayList<UserShortsEntity>()
//    private lateinit var profileShortsAdapterRecyclerView: RecyclerView

//    private val viewModel: UserProfileShortsViewModel by viewModels()
    private val shortsViewModel: GetShortsByUsernameViewModel by viewModels()

    private var storedShortsList: List<UserShortsEntity> = emptyList()

    private lateinit var username: String

    private lateinit var binding: FragmentOtherUsersShortsProfileBinding


//    private var shortPlayerFragment: OtherUserProfileShortsPlayerFragment? = null

    private var onShortThumbnailClickListener: OnShortThumbnailClickListener? = null

    fun setListener(listener: OnShortThumbnailClickListener) {
        Log.d(TAG, "setListener: listener set OtherUsersShortsProfileFragment")
        this.onShortThumbnailClickListener = listener
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            username = it.getString("username").toString()
        }

//        EventBus.getDefault().register(this)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =   FragmentOtherUsersShortsProfileBinding.inflate(layoutInflater, container, false)
//        profileShortsAdapterRecyclerView = view.findViewById(R.id.userShortsRecyclerView)
        Log.d(TAG, "onCreateView username: $username")
//        shortsViewModel.getOtherUsersProfileShorts(username)
        viewModelObserver()

        shortsAdapter = ShortsUserProfileAdapter(this)
        shortsAdapter.recyclerView = binding.userShortsRecyclerView
        binding.userShortsRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)

        shortsAdapter.setOnPaginationListener(object : PaginatedAdapter.OnPaginationListener {
            override fun onCurrentPage(page: Int) {
//                Toast.makeText(requireContext(), "Page $page loaded!", Toast.LENGTH_SHORT).show()
            }

            override fun onNextPage(page: Int) {
                lifecycleScope.launch(Dispatchers.Main) {
//                    loadMoreShorts(page)
                    userShorts(page)
                    Log.d(TAG, "onNextPage: $page")
                }
            }

            override fun onFinish() {
//                Toast.makeText(requireContext(), "finish", Toast.LENGTH_SHORT).show()
            }
        })

        lifecycleScope.launch(Dispatchers.Main) {
            userShorts(shortsAdapter.startPage)
            Log.d(TAG, "onCreateView: calling ${shortsAdapter.startPage}")
        }

        return binding.root
    }

    private fun viewModelObserver() {
        shortsViewModel.getUsersShortsObserver().observe(
            viewLifecycleOwner
        ) { shortsList ->

            shortsAdapter.submitItems(shortsList)
            shortsProfile.addAll(shortsList!!)

            Log.d("TAG", "In getUserProfileShortsObserver")
        }
    }

    private fun userShorts(page: Int) {
        Log.d(TAG, "userShorts:  invoke getUserProfileShorts $page")

        shortsViewModel.getUsersShorts(username, page)
//        sharedViewModel.getShorts(page)
    }

    companion object {
        fun newInstance(username: String): OtherUsersShortsProfileFragment {
            val fragment = OtherUsersShortsProfileFragment()
            val args = Bundle()
            args.putString("username", username)
            fragment.arguments = args
            return fragment
        }
    }

    private val otherUsersProfileViewModel: OtherUsersProfileViewModel by viewModels()
    @OptIn(UnstableApi::class)
    override fun onUserProfileShortClick(shortsEntity: UserShortsEntity) {

        Log.d("onUserProfileShortClick", "onUserProfileShortClick: short thumbnail clicked")



        val intent = Intent(activity, UserProfileShortsPlayerActivity::class.java).apply {
            putExtra(UserProfileShortsPlayerActivity.CLICKED_SHORT, shortsEntity)
            putExtra(UserProfileShortsPlayerActivity.SHORTS_LIST, shortsProfile)
            // Add other extras if needed
        }

        startForResult.launch(intent)

    }

    // Define a contract for the result
    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Handle the result here
            val data: Intent? = result.data
            // Process the data if needed
            Log.d("startForResult", "data $data: ")
        }
    }

}