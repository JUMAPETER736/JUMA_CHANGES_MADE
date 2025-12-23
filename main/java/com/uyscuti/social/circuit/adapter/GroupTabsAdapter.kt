package com.uyscuti.social.circuit.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.uyscuti.social.circuit.User_Interface.fragments.GroupParticipantsFragment
import com.uyscuti.social.circuit.User_Interface.fragments.GroupPlaceholderFragment
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.data.model.Dialog

private val TAB_ICONS = arrayOf(
    R.drawable.baseline_groups_24, // Replace with your icon resource for the first tab
    R.drawable.play_svgrepo_com, // Replace with your icon resource for the first tab
    R.drawable.scroll_text_line_svgrepo_com,  // Replace with your icon resource for the second tab
    R.drawable.business_bag_svgrepo_com,
    R.drawable.files_folder_svgrepo_com2,  // Replace with your icon resource for the second tab
    R.drawable.analytics_svgrepo_com,  // Replace with your icon resource for the second tab
)

class GroupTabsAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private var unreadCounts = mutableListOf(0, 0, 0,0,0,0) // Initialize the unread counts for each tab
    private var dialog: Dialog? = null
    private var adminId: String? = null


    fun setDialog(dialog: Dialog) {
        this.dialog = dialog
        Log.d("GroupTabsAdapter", "Dialog content: ${dialog.users}")
    }
    fun setAdminId(adminId: String) {
        this.adminId = adminId
        Log.d("GroupTabsAdapter", "admin id: $adminId")
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> GroupParticipantsFragment.newInstance(dialog!!,adminId!!, "")
            1 -> GroupPlaceholderFragment.newInstance("","")
            2 -> GroupPlaceholderFragment.newInstance("","")
            3 -> GroupPlaceholderFragment.newInstance("","")
            4 -> GroupPlaceholderFragment.newInstance("","")
            5 -> GroupPlaceholderFragment.newInstance("","")
            else -> throw IllegalArgumentException("Invalid tab position: $position")
        }
    }



    override fun getPageTitle(position: Int): CharSequence? {
        // Return null to indicate that you want to use icons instead of text for tabs
        return null
    }

    fun getIcon(position: Int): Drawable? {
        // Return the icon for the specified position
        return ContextCompat.getDrawable(context, TAB_ICONS[position])
    }

    override fun getCount(): Int {
        return TAB_ICONS.size
    }
}