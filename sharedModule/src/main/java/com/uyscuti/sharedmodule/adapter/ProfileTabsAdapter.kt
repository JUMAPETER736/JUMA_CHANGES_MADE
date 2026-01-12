package com.uyscuti.sharedmodule.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.uyscuti.sharedmodule.R
import com.uyscuti.sharedmodule.interfaces.feedinterfaces.OnShortThumbnailClickListener
import com.uyscuti.sharedmodule.model.User
import com.uyscuti.sharedmodule.ui.fragments.forshorts.OtherUsersShortsProfileFragment
import com.uyscuti.sharedmodule.fragments.ProfileViewFragment
import com.uyscuti.sharedmodule.ui.fragments.FragmentFactoryRegistry


private val TAB_ICONS = arrayOf(
    R.drawable.play_svgrepo_com,
    R.drawable.scroll_text_line_svgrepo_com,
//    R.drawable.favorite_black,
    R.drawable.business_bag_svgrepo_com,
)
class ProfileTabsAdapter(private val context: Context, fm: FragmentManager, private val user: User) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    val TAG = "ProfileTabsAdapter"

    private var onShortThumbnailClickListener: OnShortThumbnailClickListener? = null

    fun setListener(listener: OnShortThumbnailClickListener) {
        this.onShortThumbnailClickListener = listener
    }

    private lateinit var shortFragment: OtherUsersShortsProfileFragment
    private var username: String? = null

    fun setUsername(username: String?) {
        this.username = username
        Log.d(TAG, "username content: $username")
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 ->{
                shortFragment = OtherUsersShortsProfileFragment.newInstance(this.username!!)
//                val args = Bundle().apply {
//                    putString("username", username)
//                }

                onShortThumbnailClickListener?.let { shortFragment.setListener(it) }
                //  shortFragment.arguments = args
                return shortFragment

            }
            1 ->  FragmentFactoryRegistry.createFragment("profile", username!!) ?: EmptyFragment()
            2 -> ProfileViewFragment.newInstance(user)


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


class EmptyFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return TextView(requireContext()).apply {
            text = ""
            gravity = Gravity.CENTER
        }
    }
}

