package com.uyscuti.social.circuit.ui.fragments.chat

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.media3.common.util.UnstableApi
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.uyscuti.sharedmodule.media.ViewImagesActivity
import com.uyscuti.sharedmodule.model.ProfileImageEvent
import com.uyscuti.sharedmodule.model.ShowBottomNav
import com.uyscuti.sharedmodule.model.User
import com.uyscuti.sharedmodule.ui.UserProfileEditActivity
import com.uyscuti.sharedmodule.viewmodels.GetShortsByUsernameViewModel
import com.uyscuti.sharedmodule.viewmodels.feed.GetFeedViewModel
import com.uyscuti.social.circuit.MainActivity
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.adapter.UserProfileTabsAdapter
import com.uyscuti.social.circuit.databinding.FeedRetweetPostBinding
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.util.Date
import javax.inject.Inject

private const val TAG = "UserProfileFragment"

@AndroidEntryPoint
class UserProfileFragment : Fragment() {

    private lateinit var  binding : FeedRetweetPostBinding

    private lateinit var settings: SharedPreferences
    private val PREFS_NAME = "LocalSettings" // Change this to a unique name for your app
    private lateinit var username: String
    private lateinit var avatar: String
    private lateinit var userId : String
    lateinit var bio: String

    private val shortsViewModel: GetShortsByUsernameViewModel by activityViewModels()
    private val getFeedViewModel: GetFeedViewModel by activityViewModels()

    @Inject
    lateinit var retrofitInterface: RetrofitInstance
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FeedRetweetPostBinding.inflate(inflater,container, false)

        activity?.window?.statusBarColor = ContextCompat.getColor(requireContext(), R.color.white)
        // Set the navigation bar color dynamically
        activity?.window?.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.white)

        return binding.root
    }

    @OptIn(UnstableApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val TAG = "onViewCreated"

        EventBus.getDefault().post(ShowBottomNav(false))

        (activity as? MainActivity)?.showAppBar()


        settings = requireActivity().getSharedPreferences(PREFS_NAME, 0)
        username = settings.getString("username", "").toString()

        shortsViewModel.getUserProfile()
        shortsViewModel.getOtherUsersProfileShorts(username)
        shortsViewModel.getUserProfileShortsObserver().observe(
            viewLifecycleOwner
        ) {
                userShortsData ->
            shortsViewModel.postCount = userShortsData.totalPosts
            binding.postsCount.text = shortsViewModel.postCount.toString()
        }


        shortsViewModel.isRefreshPostCount.observe(viewLifecycleOwner) { isDataAvailable ->
            // Handle the updated value of isResuming here
            if (isDataAvailable) {
                Log.d(TAG, "onCreateView: data decremented(deleted)")
                binding.postsCount.text = shortsViewModel.postCount.toString()
            } else {
                // Do something when isResuming is false
                Log.d(TAG, "onCreateView: data not decremented(deleted)")
            }
        }

        shortsViewModel.getOnErrorFeedBackObserver().observe(viewLifecycleOwner) {
                errorFeedback ->
            MotionToast.Companion.createToast(requireActivity(),
                "Failed To Retrieve Data☹️",
                errorFeedback,
                MotionToastStyle.ERROR,
                MotionToast.Companion.GRAVITY_BOTTOM,
                MotionToast.Companion.LONG_DURATION,
                ResourcesCompat.getFont(requireActivity(),R.font.helvetica_regular))
        }
        shortsViewModel.getFollowersCount().observe(viewLifecycleOwner) {
            shortsViewModel.followersCount = it.followersCount.toInt()
            shortsViewModel.followingCount = it.followingCount.toInt()

            binding.followersCount.text = shortsViewModel.followersCount.toString()
            binding.followingCount.text = shortsViewModel.followingCount.toString()
        }

        bio = settings.getString("bio", "bio").toString()



        binding.editButton.setOnClickListener {
            val intent = Intent(requireContext(), UserProfileEditActivity::class.java)
            startActivity(intent)
        }

        binding.username.text = username
        binding.userBioText.text = bio

        avatar = settings.getString("avatar", "avatar").toString()
        userId = settings.getString("_id", "").toString()
        Log.d("PREFS_NAME", "PREFS_NAME: userid $userId")

        val user = User(
            _id = userId,
            avatar = avatar,
            email = "email",
            isEmailVerified = false,
            role = "user",
            username = username,
            lastseen = Date()
        )


        val tabsAdapter = UserProfileTabsAdapter(requireContext(), childFragmentManager, user)

        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = tabsAdapter
        viewPager.offscreenPageLimit = 5

        val tabs: TabLayout = binding.tabLayout
        tabs.setupWithViewPager(viewPager)
        for (i in 0 until tabsAdapter.count) {
            tabs.getTabAt(i)?.icon = tabsAdapter.getIcon(i)
        }

        binding.userAvatar.setOnClickListener {
            if (avatar != null) {
                viewImage(avatar, "Profile Picture")
            }
        }


    }

    override fun onResume() {
        super.onResume()
        avatar = settings.getString("avatar", "avatar").toString()
        username = settings.getString("username", "").toString()
        bio = settings.getString("bio", "bio").toString()
        userId = settings.getString("_id", "").toString()


        binding.userBioText.text = bio
        binding.username.text = username
        Glide.with(this)
            .load(avatar)
            .apply(RequestOptions.placeholderOf(R.drawable.flash21))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.headerImage)


        Glide.with(this)
            .load(avatar)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .apply(RequestOptions.placeholderOf(R.drawable.flash21))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.userAvatar)
        updateStatusBar()

        val event = ProfileImageEvent(avatar)
        EventBus.getDefault().post(event)
        // Access the BottomNavigationView from the hosting activity
        val bottomNavigationView: com.uyscuti.sharedmodule.bottomSheet.BottomNavigationView? = activity?.findViewById(R.id.bottomNavigationView)

        // Check if the BottomNavigationView is found and make it visible
        bottomNavigationView?.visibility = View.VISIBLE
    }



    private fun updateStatusBar() {
        val decor: View? = activity?.window?.decorView

        // Your logic to determine the status bar appearance based on the fragment's theme

        decor?.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

    }

    private fun viewImage(url: String, name: String) {
        val intent = Intent(requireContext(), ViewImagesActivity::class.java)
        intent.putExtra("imageUrl", url)
        intent.putExtra("owner", name)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.user_profile_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_setting -> {
                val intent = Intent(requireContext(), UserProfileEditActivity::class.java)
                startActivity(intent)
                return true
            }
            // Add other cases for different menu items if needed
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onGetLayoutInflater(
        savedInstanceState: Bundle?
    ): LayoutInflater {
        // Use a custom theme for the fragment layout


        return super.onGetLayoutInflater(savedInstanceState).cloneInContext(
            ContextThemeWrapper(
                requireContext(), R.style.Base_Theme_FlashDesign
            )
        )
    }

}