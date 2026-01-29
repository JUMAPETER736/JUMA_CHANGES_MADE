package com.uyscuti.sharedmodule.ui.fragments.forshorts

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
import com.uyscuti.sharedmodule.adapter.PaginatedAdapter
import com.uyscuti.sharedmodule.adapter.RecyclerViewAdapter
import com.uyscuti.sharedmodule.adapter.ShortsUserProfileAdapter
import com.uyscuti.sharedmodule.databinding.FragmentOtherUsersShortsProfileBinding
import com.uyscuti.sharedmodule.interfaces.feedinterfaces.OnShortThumbnailClickListener
import com.uyscuti.sharedmodule.shorts.UserProfileShortsPlayerActivity
import com.uyscuti.sharedmodule.viewmodels.GetShortsByUsernameViewModel
import com.uyscuti.sharedmodule.viewmodels.otherusersprofile.OtherUsersProfileViewModel
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

    private val shortsViewModel: GetShortsByUsernameViewModel by viewModels()
    private var storedShortsList: List<UserShortsEntity> = emptyList()
    private lateinit var username: String
    private lateinit var binding: FragmentOtherUsersShortsProfileBinding
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


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =   FragmentOtherUsersShortsProfileBinding.inflate(layoutInflater, container, false)

        Log.d(TAG, "onCreateView username: $username")

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

//        onShortThumbnailClickListener?.onShortClick(shortsProfile, shortsEntity)
//        binding.userShortsRecyclerView.visibility = View.GONE
//        binding.otherUsersShortsPlayFragment.visibility = View.VISIBLE
//        EventBus.getDefault().post(
//            HideToolBar(shortsProfile, shortsEntity)
//        )
//        shortPlayerFragment = OtherUserProfileShortsPlayerFragment()
//
//        requireActivity().supportFragmentManager.beginTransaction()
//            .replace(
//                R.id.other_users_shorts_play_fragment,
//                shortPlayerFragment!!
//            ) // Use the correct container ID
//            .addToBackStack(null) // Optional, to add to back stack
//            .commit()

//        val intent = Intent(activity, UserProfileShortsPlayerActivity::class.java)
////        intent.putExtra("theClickedShort", clickedShort)
//        intent.putExtra(UserProfileShortsPlayerActivity.CLICKED_SHORT, shortsEntity)
//
//        intent.putExtra(UserProfileShortsPlayerActivity.SHORTS_LIST, shortsProfile)
////        intent.putExtra("userShortsEntity", storedShortsList)
//        startActivity(intent)

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