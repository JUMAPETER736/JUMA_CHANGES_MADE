package com.uyscuti.social.circuit.ui.fragments.chat

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.uyscuti.sharedmodule.presentation.DialogViewModel
import com.uyscuti.sharedmodule.presentation.GroupDialogViewModel
import com.uyscuti.sharedmodule.presentation.MainViewModel
import com.uyscuti.sharedmodule.utils.ChatNavigationController
import com.uyscuti.social.business.model.business.BusinessProfile
import com.uyscuti.social.business.repository.IFlashApiRepositoryImplementation
import com.uyscuti.social.business.retro.CreateCatalogueActivity
import com.uyscuti.social.circuit.MainActivity
import com.uyscuti.social.circuit.MakeCallActivity
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.adapter.ChatPagerAdapter
import com.uyscuti.social.circuit.databinding.FragmentChatBinding
import com.uyscuti.sharedmodule.popupDialog.BusinessProfileDialogFragment
import com.uyscuti.social.circuit.ui.CreateGroupChat
import com.uyscuti.social.circuit.ui.CreateUserChat
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChatFragment.newInstance] factory method to
 * create an instance of this fragment.
 */


@UnstableApi
@AndroidEntryPoint
class ChatFragment : Fragment(), ChatNavigationController {

    private var param1: String? = null
    private var param2: String? = null

    @Inject
    lateinit var retrofitInstance: RetrofitInstance

    private lateinit var binding: FragmentChatBinding
    private lateinit var tabLayout: TabLayout
    lateinit var viewPager: ViewPager           // ← changed from private to internal so MainActivity can reach it if needed
    private lateinit var adapter: ChatPagerAdapter
    private lateinit var fabAction: FloatingActionButton

    private lateinit var repository: IFlashApiRepositoryImplementation

    private val dialogViewModel: DialogViewModel by viewModels()
    private val groupDialogViewModel: GroupDialogViewModel by viewModels()

    private val mainViewModel: MainViewModel by activityViewModels()

    private var businessProfile: Result<BusinessProfile>? = null
    private var profileDeferred: Deferred<Boolean>? = null
    private var hasBusinessProfile: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @OptIn(UnstableApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(inflater)
        (activity as? MainActivity)?.showAppBar()
        activity?.window?.statusBarColor = ContextCompat.getColor(requireContext(), R.color.white)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity?.window?.navigationBarColor =
                ContextCompat.getColor(requireContext(), R.color.white)
        }
        initRepo()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpTabs()
    }

    private fun setUpTabs() {
        tabLayout = binding.tabLayout
        viewPager = binding.viewPager
        adapter = ChatPagerAdapter(childFragmentManager)
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 3
        tabLayout.setupWithViewPager(viewPager)
        setTabListener()
        getUnReads()
    }

    /**
     * Called by MainActivity after a group delete/leave to land directly on the
     * Groups tab (index 1 in ChatPagerAdapter: Chats=0, Groups=1, Calls=2).
     */
    fun navigateToGroupsTab() {
        if (::viewPager.isInitialized) {
            viewPager.currentItem = 1
        }
    }

    private suspend fun setUpBusinessProfile(): Boolean {
        return try {
            businessProfile = repository.getBusinessProfile()
            if (businessProfile?.isSuccess == true) {
                Log.d("ApiService", "${businessProfile.toString()}")
                hasBusinessProfile = true
                true
            } else {
                Log.d("ApiService", "${businessProfile.toString()}")
                hasBusinessProfile = false
                false
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Error getting business profile", e)
            hasBusinessProfile = false
            false
        }
    }

    private fun initRepo() {
        repository = IFlashApiRepositoryImplementation(retrofitInstance)
    }

    private fun getUnReads() {
        lifecycleScope.launch {
            dialogViewModel.allUnreadDialogsCount.observe(viewLifecycleOwner) { unread ->
                Log.d("UnReadCount", "Count : $unread")
                CoroutineScope(Dispatchers.Main).launch {
                    adapter.updateUnreadCount(0, unread)
                }
            }

            groupDialogViewModel.allUnreadGroupDialogsCount.observe(viewLifecycleOwner) { unread ->
                CoroutineScope(Dispatchers.Main).launch {
                    adapter.updateUnreadCount(1, unread)
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChatFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun setTabListener() {
        fabAction = binding.fabAction

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position ?: 0) {
                    0 -> firstTab()
                    1 -> secondTab()
                    2 -> thirdTab()
                    3 -> fourthTab()
                    else -> fabAction.setImageResource(R.drawable.baseline_add_24)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun firstTab() {
        fabAction.visibility = View.VISIBLE
        mainViewModel.resetSelectedDialogsCount()
        fabAction.setImageResource(R.drawable.baseline_add_24)
        fabAction.setOnClickListener {
            val intent = Intent(requireContext(), CreateUserChat::class.java)
            startActivity(intent)
        }
    }

    private fun secondTab() {
        fabAction.visibility = View.VISIBLE
        mainViewModel.resetSelectedDialogsCount()
        fabAction.setImageResource(R.drawable.baseline_add_24)
        fabAction.setOnClickListener {
            val intent = Intent(requireContext(), CreateGroupChat::class.java)
            startActivity(intent)
        }
    }

    private fun thirdTab() {
        fabAction.visibility = View.VISIBLE
        mainViewModel.resetSelectedDialogsCount()
        fabAction.setImageResource(R.drawable.baseline_add_ic_call_24)
        fabAction.setOnClickListener {
            val intent = Intent(requireContext(), MakeCallActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fourthTab() {
        fabAction.visibility = View.INVISIBLE
    }

    override fun openChat(id: Long, prepopulateText: String?) {
        TODO("Not yet implemented")
    }

    override fun currentFragment(id: String) {
        when (id) {
            "chat" -> {
                fabAction.visibility = View.VISIBLE
                binding.fabAction.setImageResource(R.drawable.baseline_add_24)
            }
            "groups" -> {
                fabAction.visibility = View.VISIBLE
                binding.fabAction.setImageResource(R.drawable.baseline_add_24)
            }
            "calls" -> {
                fabAction.visibility = View.VISIBLE
                binding.fabAction.setImageResource(R.drawable.baseline_add_ic_call_24)
            }
            else -> {
                binding.fabAction.setImageResource(R.drawable.baseline_add_24)
            }
        }
    }

    @OptIn(UnstableApi::class)
    override fun unreadCount(id: Int, count: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            adapter.updateUnreadCount(id, count)
        }
    }

    override fun onGetLayoutInflater(savedInstanceState: Bundle?): LayoutInflater {
        return super.onGetLayoutInflater(savedInstanceState).cloneInContext(
            ContextThemeWrapper(requireContext(), R.style.AppThemeWithLightStatusBar)
        )
    }

    override fun onResume() {
        super.onResume()
        updateStatusBar()
    }

    private fun updateStatusBar() {
        val decor: View? = activity?.window?.decorView
        decor?.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }
}