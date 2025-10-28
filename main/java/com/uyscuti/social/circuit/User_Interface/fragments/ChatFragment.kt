package com.uyscuti.social.circuit.User_Interface.fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.uyscuti.social.circuit.MainActivity
import com.uyscuti.social.circuit.adapter.ChatPagerAdapter
import com.uyscuti.social.circuit.presentation.DialogViewModel
import com.uyscuti.social.circuit.presentation.GroupDialogViewModel
import com.uyscuti.social.circuit.presentation.MainViewModel
import com.uyscuti.social.circuit.User_Interface.OtherImportantProfileThings.CreateGroupChat
import com.uyscuti.social.circuit.User_Interface.OtherImportantProfileThings.CreateUserChat
import com.uyscuti.social.circuit.User_Interface.OtherImportantProfileThings.MakeCallActivity
import com.uyscuti.social.circuit.utils.ChatNavigationController
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.FragmentChatBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChatFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

@AndroidEntryPoint
class ChatFragment : Fragment(), ChatNavigationController {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentChatBinding
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var adapter: ChatPagerAdapter
    private lateinit var fabAction: FloatingActionButton

    private val dialogViewModel: DialogViewModel by viewModels()
    private val groupDialogViewModel: GroupDialogViewModel by viewModels()

    private val mainViewModel: MainViewModel by activityViewModels()


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
        // Inflate the layout for this fragment
        binding = FragmentChatBinding.inflate(inflater)
        (activity as? MainActivity)?.showAppBar()
        activity?.window?.statusBarColor = ContextCompat.getColor(requireContext(), R.color.white)
        // Set the navigation bar color dynamically
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity?.window?.navigationBarColor =
                ContextCompat.getColor(requireContext(), R.color.white)
        }
//        val decor: View? = activity?.window?.decorView
//
//        if(decor!!.systemUiVisibility != View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
//            decor.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
//        else
//            decor.systemUiVisibility = 0

        return binding.root
//        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpTabs()
    }

    private fun setUpTabs() {
        // Initialize TabLayout
        tabLayout = binding.tabLayout

        // Initialize ViewPager
        viewPager = binding.viewPager

        // Create an adapter for ViewPager to manage tab fragments
        adapter = ChatPagerAdapter(childFragmentManager) // Use childFragmentManager
        viewPager.adapter = adapter

        viewPager.offscreenPageLimit = 4
//        viewPager.setCurrentItem(0, true)

        // Connect the TabLayout and ViewPager
        tabLayout.setupWithViewPager(viewPager)

        setTabListener()

        getUnReads()
    }

    private fun getUnReads() {
        lifecycleScope.launch {
            dialogViewModel.allUnreadDialogsCount.observe(viewLifecycleOwner) { unread ->
                Log.d("UnReadCount", "Count : $unread")

                CoroutineScope(Dispatchers.Main).launch {
                    adapter.updateUnreadCount(0, unread)
                }
//               getChatNavigationController()?.unreadCount(0,unread)
            }

            groupDialogViewModel.allUnreadGroupDialogsCount.observe(viewLifecycleOwner) { unread ->
                CoroutineScope(Dispatchers.Main).launch {
                    adapter.updateUnreadCount(1, unread)
                }
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
         * @return A new instance of fragment ChatFragment.
         */
        // TODO: Rename and change types and number of parameters
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
        // Initialize your FAB
        fabAction = binding.fabAction

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                // Get the position of the selected tab

                // Update the FAB icon based on the selected tab
                when (tab?.position ?: 0) {
                    0 -> firstTab()
                    1 -> secondTab()
                    2 -> thirdTab()
                    3 -> fourthTab()
                    // Add cases for other tabs as needed
                    else -> fabAction.setImageResource(R.drawable.baseline_add_24)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Do nothing when a tab is unselected
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Handle reselection of the tab if needed
            }
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
//            Toast.makeText(requireContext(),"Create Group Chat", Toast.LENGTH_LONG).show()
        }
    }

    private fun thirdTab() {
        fabAction.visibility = View.VISIBLE
        mainViewModel.resetSelectedDialogsCount()
        fabAction.setImageResource(R.drawable.baseline_add_ic_call_24)
        fabAction.setOnClickListener {
            val intent = Intent(requireContext(), MakeCallActivity::class.java)
            startActivity(intent)
//            Toast.makeText(requireContext(),"Make A Call", Toast.LENGTH_LONG).show()

        }
    }

    private fun fourthTab() {
//        mainViewModel.resetSelectedDialogsCount()
//        fabAction.setImageResource(R.drawable.baseline_add_24)
//        fabAction.setOnClickListener {
////            val intent = Intent(requireContext(), MakeCallActivity::class.java)
////            startActivity(intent)
////            Toast.makeText(requireContext(),"Make A Call", Toast.LENGTH_LONG).show()
//
//        }

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
                // Set the FAB icon for the "calls" fragment
                binding.fabAction.setImageResource(R.drawable.baseline_add_ic_call_24)
            }

            "business" -> {
               fabAction.visibility = View.INVISIBLE
//                binding.fabAction.setImageResource(R.drawable.baseline_add_24)
            }

            else -> {
                binding.fabAction.setImageResource(R.drawable.baseline_add_24)
            }
        }
    }

    override fun unreadCount(id: Int, count: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            adapter.updateUnreadCount(id, count)
        }
    }

    override fun onGetLayoutInflater(
        savedInstanceState: Bundle?
    ): LayoutInflater {
        // Use a custom theme for the fragment layout
//        val themeId = if (someCondition) {
//            R.style.FragmentLightTheme
//        } else {
//        }

        return super.onGetLayoutInflater(savedInstanceState).cloneInContext(
            ContextThemeWrapper(
                requireContext(), R.style.AppThemeWithLightStatusBar
            )
        )
    }

    override fun onResume() {
        super.onResume()
        updateStatusBar()
    }

    private fun updateStatusBar() {
        val decor: View? = activity?.window?.decorView

        // Your logic to determine the status bar appearance based on the fragment's theme
//        val isLightTheme = // Your logic to determine if the fragment has a light theme
//            decor?.systemUiVisibility = 0
        decor?.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

//            if (isLightTheme) {
//                // Light theme
//            } else {
//                // Dark theme
//                decor?.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
//            }
    }
}