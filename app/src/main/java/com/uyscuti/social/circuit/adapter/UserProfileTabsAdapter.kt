package com.uyscuti.social.circuit.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.uyscuti.social.business.forapp.fragment.ProfileViewFragment
import com.uyscuti.social.business.model.User
import com.uyscuti.social.circuit.User_Interface.fragments.GroupPlaceholderFragment
import com.uyscuti.social.circuit.User_Interface.fragments.feed.FavoriteFragment
import com.uyscuti.social.circuit.User_Interface.fragments.feed.MyFeedFragment
import com.uyscuti.social.circuit.User_Interface.fragments.user_profile_fragments.UserShortsFragment
import com.uyscuti.social.circuit.R

class UserProfileTabsAdapter(
    private val context: Context, fm: FragmentManager, private val user: User) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val fragmentList = mutableListOf<Fragment>()
    private val fragmentTagList = mutableListOf<String>()

    private val TAB_ICONS = arrayOf(
        R.drawable.play_svgrepo_com,
        R.drawable.scroll_text_line_svgrepo_com,
        R.drawable.favorite_black,
        R.drawable.business_bag_svgrepo_com,
        R.drawable.analytics_svgrepo_com,

    )

    init {
        // Add fragments to the adapter during initialization
        addFragment(UserShortsFragment.newInstance("", ""), "user_shorts_fragment_tag")

        addFragment(MyFeedFragment.newInstance("", ""), "group_placeholder_2_tag")

        addFragment(FavoriteFragment(), "group_placeholder_1_tag")
        addFragment(ProfileViewFragment.newInstance(user), "group_placeholder_3_tag")
        addFragment(GroupPlaceholderFragment.newInstance("", ""), "group_placeholder_4_tag")
    }

    private fun addFragment(fragment: Fragment, tag: String) {
        fragmentList.add(fragment)
        fragmentTagList.add(tag)
    }

    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return null
    }

    override fun getCount(): Int {
        return fragmentList.size
    }
}

