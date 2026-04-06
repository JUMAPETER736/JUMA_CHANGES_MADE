package com.uyscuti.sharedmodule.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.uyscuti.sharedmodule.R
import com.uyscuti.sharedmodule.data.model.Dialog
import com.uyscuti.sharedmodule.fragments.GroupParticipantsFragment
import com.uyscuti.sharedmodule.fragments.GroupPlaceholderFragment

private val TAB_ICONS = arrayOf(
    R.drawable.baseline_groups_24,
    R.drawable.play_svgrepo_com,
    R.drawable.scroll_text_line_svgrepo_com,
    R.drawable.business_bag_svgrepo_com,
    R.drawable.files_folder_svgrepo_com2,
    R.drawable.analytics_svgrepo_com,
)

class GroupTabsAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private var unreadCounts = mutableListOf(0, 0, 0, 0, 0, 0)
    private var dialog: com.uyscuti.social.core.models.data.Dialog? = null
    private var adminId: String? = null
    private var myRole: String = "member"

    // Keep a reference to the participants fragment so we can
    // notify it of role changes (e.g. when the user gets removed)
    private var participantsFragment: GroupParticipantsFragment? = null


    fun setDialog(dialog: com.uyscuti.social.core.models.data.Dialog) {
        this.dialog = dialog
        Log.d("GroupTabsAdapter", "Dialog content: ${dialog.users}")
    }

    fun setAdminId(adminId: String) {
        this.adminId = adminId
        Log.d("GroupTabsAdapter", "admin id: $adminId")
    }

    fun setMyRole(role: String) {
        this.myRole = role
        Log.d("GroupTabsAdapter", "my role: $role")
    }

    //  Call this from GroupProfileActivity when the user is removed
    // so the participants tab switches to read-only mode immediately
    fun notifyUserRemoved() {
        participantsFragment?.onCurrentUserRemoved()
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                val fragment = GroupParticipantsFragment.newInstance(
                    chatId  = dialog?.id ?: "",
                    adminId = adminId ?: "",
                    myRole  = myRole
                )
                //  Save reference so we can call notifyUserRemoved() later
                participantsFragment = fragment
                fragment
            }
            1 -> GroupPlaceholderFragment.newInstance("", "")
            2 -> GroupPlaceholderFragment.newInstance("", "")
            3 -> GroupPlaceholderFragment.newInstance("", "")
            4 -> GroupPlaceholderFragment.newInstance("", "")
            5 -> GroupPlaceholderFragment.newInstance("", "")
            else -> throw IllegalArgumentException("Invalid tab position: $position")
        }
    }


    override fun getPageTitle(position: Int): CharSequence? {
        return null
    }

    fun getIcon(position: Int): Drawable? {
        return ContextCompat.getDrawable(context, TAB_ICONS[position])
    }

    override fun getCount(): Int {
        return TAB_ICONS.size
    }
}