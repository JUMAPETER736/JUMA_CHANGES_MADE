package com.uyscuti.social.circuit.adapter

import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.media3.common.util.UnstableApi
import com.uyscuti.social.business.forapp.fragment.BusinessProfileEditFragment
import com.uyscuti.social.circuit.tabs.Calls
import com.uyscuti.social.circuit.tabs.GroupChats
import com.uyscuti.social.circuit.tabs.PersonalChats

class ChatPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    private var unreadCounts = mutableListOf(0, 0, 0, 0) // Initialize the unread counts for each tab



    // Define a method to update the unread count for a specific tab.
    fun updateUnreadCount(tabPosition: Int, count: Int) {
        if (tabPosition >= 0 && tabPosition < unreadCounts.size) {
            unreadCounts[tabPosition] = count
            notifyDataSetChanged() // Notify the adapter that the data has changed
        }
    }

    @OptIn(UnstableApi::class)

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> PersonalChats.newInstance("personal","")
            1 -> GroupChats.newInstance("","")
            2 -> Calls.newInstance("","")
            3 -> BusinessProfileEditFragment.newInstance("","")
            else -> throw IllegalArgumentException("Invalid tab position: $position")
        }
    }

    override fun getCount(): Int {
        return 4 // Number of tabs
    }

    override fun getPageTitle(position: Int): CharSequence {
        val title = when (position) {
            0 -> "Chats"
            1 -> "Groups"
            2 -> "Calls"
            3 -> "Business"
            else -> ""
        }


        val unreadCount = unreadCounts.getOrNull(position) ?: 0
        return if (unreadCount > 0) {
            "$title ($unreadCount)"
        } else {
            title
        }
    }



}
