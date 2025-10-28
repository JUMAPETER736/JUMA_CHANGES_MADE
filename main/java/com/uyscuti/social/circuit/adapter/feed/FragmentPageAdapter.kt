package com.uyscuti.social.circuit.adapter.feed

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.uyscuti.social.circuit.User_Interface.fragments.feed.AllFragment
import com.uyscuti.social.circuit.User_Interface.fragments.feed.FavoriteFragment
import com.uyscuti.social.circuit.User_Interface.fragments.feed.FollowingFragment

class FragmentPageAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {


    private val fragments = listOf<Fragment>(
        AllFragment(),
        FollowingFragment(),
        FavoriteFragment() // Add more fragments as needed
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]

    // Method to get a fragment by position
    fun getFragment(position: Int): Fragment? {
        return if (position in 0 until itemCount) fragments[position] else null
    }
}