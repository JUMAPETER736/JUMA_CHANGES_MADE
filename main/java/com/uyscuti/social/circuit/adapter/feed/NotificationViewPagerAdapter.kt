package com.uyscuti.social.circuit.adapter.feed

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.uyscuti.social.circuit.User_Interface.fragments.NotificationsFragment
import com.uyscuti.social.circuit.User_Interface.fragments.feed.feedviewfragments.BusinessNotificationsFragment

class NotificationViewPagerAdapter(fragmentManager: FragmentManager,lifecycle: Lifecycle)
    :FragmentStateAdapter(fragmentManager,lifecycle){
    override fun getItemCount(): Int {
        return 2
    }
    override fun createFragment(position: Int): Fragment {
        return if (position==0) {
            NotificationsFragment()  // Replace with your actual fragment class
        }else{
            BusinessNotificationsFragment()  // Replace with your actual fragment class
        }
    }

}