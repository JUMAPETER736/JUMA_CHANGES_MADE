package com.uyscuti.social.circuit.adapter.feed

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.uyscuti.social.circuit.User_Interface.fragments.NotificationsFragment
import com.uyscuti.social.circuit.User_Interface.fragments.feed.feedviewfragments.BusinessNotificationsFragment

class NotificationsHostPagerAdapter (fragmentManager: FragmentManager) :FragmentPagerAdapter(fragmentManager) {
    private var unreadCounts = mutableListOf(0, 0) // Initialize the unread counts for each tab


    fun updateUnreadCount(tabPosition: Int, count: Int) {
        if (tabPosition >= 0 && tabPosition < unreadCounts.size) {
            unreadCounts[tabPosition] = count
            notifyDataSetChanged() // Notify the adapter that the data has changed
        }
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> NotificationsFragment()
            1 -> BusinessNotificationsFragment()
            else -> throw IllegalArgumentException("Invalid tab position: $position")

        }
    }
    override fun getPageTitle(position: Int): CharSequence {
        val title = when (position) {
            0 -> "All"
            1 -> "Business"
            else -> ""
        }


        // Append the unread count to the title if it's greater than 0
        val unreadCount = unreadCounts.getOrNull(position) ?: 0

        return if (position == 1) {
            "$title (99)"
        } else {
            title
        }

    }
}