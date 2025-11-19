package com.uyscuti.social.circuit.adapter.feed

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.uyscuti.social.circuit.User_Interface.fragments.feed.FavoriteFragment
import com.uyscuti.social.circuit.User_Interface.fragments.feed.MyFeedFragment

class UserProfileMyFeedFragmentAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return if (position == 0) {
            MyFeedFragment()

        } else{
            FavoriteFragment()
        }

    }
}