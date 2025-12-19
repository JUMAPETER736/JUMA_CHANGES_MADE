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

@UnstableApi
class ChatPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    private var unreadCounts = mutableListOf(0, 0, 0, 0) // Initialize the unread counts for each tab

    // Store fragment instances to ensure single instance
    private var personalChatsFragment: PersonalChats? = null
    private var groupChatsFragment: GroupChats? = null
    private var callsFragment: Calls? = null



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
            0 -> {
                if (personalChatsFragment == null) {
                    personalChatsFragment = PersonalChats.newInstance("personal", "")
                }
                personalChatsFragment!!
            }
            1 -> {
                if (groupChatsFragment == null) {
                    groupChatsFragment = GroupChats.newInstance("", "")
                }
                groupChatsFragment!!
            }
            2 -> {
                if (callsFragment == null) {
                    callsFragment = Calls.newInstance("", "")
                }
                callsFragment!!
            }
            else -> throw IllegalArgumentException("Invalid tab position: $position")
        }
    }

    override fun getCount(): Int {
        return 3 // Number of tabs
    }

    override fun getPageTitle(position: Int): CharSequence {
        val title = when (position) {
            0 -> "Chats"
            1 -> "Groups"
            2 -> "Calls"
            else -> ""
        }


        // Append the unread count to the title if it's greater than 0
        val unreadCount = unreadCounts.getOrNull(position) ?: 0
        return if (unreadCount > 0) {
            "$title ($unreadCount)"
        } else {
            title
        }
    }

    // Methods to get specific fragment instances
    fun getPersonalChatsFragment(): PersonalChats? = personalChatsFragment
    fun getGroupChatsFragment(): GroupChats? = groupChatsFragment
    fun getCallsFragment(): Calls? = callsFragment

}
