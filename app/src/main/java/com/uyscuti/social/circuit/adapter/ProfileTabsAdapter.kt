package com.uyscuti.social.circuit.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.media3.common.util.UnstableApi
import com.uyscuti.social.business.forapp.fragment.ProfileViewFragment
import com.uyscuti.social.business.model.User
import com.uyscuti.social.circuit.interfaces.feedinterfaces.OnShortThumbnailClickListener
import com.uyscuti.social.circuit.User_Interface.fragments.forshorts.OtherUsersFeedProfileFragment
import com.uyscuti.social.circuit.User_Interface.fragments.forshorts.OtherUsersShortsProfileFragment
import com.uyscuti.social.circuit.R


private val TAB_ICONS = arrayOf(
    R.drawable.play_svgrepo_com,
    R.drawable.scroll_text_line_svgrepo_com,
    R.drawable.business_bag_svgrepo_com,
)

class ProfileTabsAdapter @OptIn(UnstableApi::class) constructor(
    private val context: Context,
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val user: User?
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    val TAG = "ProfileTabsAdapter"

    private var onShortThumbnailClickListener: OnShortThumbnailClickListener? = null
    private var username: String? = null

    fun setListener(listener: OnShortThumbnailClickListener) {
        this.onShortThumbnailClickListener = listener
    }

    fun setUsername(username: String?) {
        this.username = username
        Log.d(TAG, "username content: $username")
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {


            0 -> {
                // Use the newInstance method instead of direct instantiation
                username?.let { validUsername ->
                    val shortFragment = OtherUsersShortsProfileFragment.newInstance(validUsername)
                    onShortThumbnailClickListener?.let { shortFragment.setListener(it) }
                    shortFragment
                } ?: run {
                    Log.e("ProfileTabsAdapter", "Username is null when creating OtherUsersShortsProfileFragment")
                    Fragment()
                }
            }

            1 -> {
                // Add null check for username before creating fragment
                username?.let { validUsername ->
                    OtherUsersFeedProfileFragment.newInstance(validUsername)
                } ?: run {
                    // Log error and return empty fragment
                    Log.e("ProfileTabsAdapter", "Username is null when creating OtherUsersFeedProfileFragment")
                    Fragment()
                }
            }

            2 -> {
                user?.let { validUser ->
                    ProfileViewFragment.newInstance(validUser) // ← Pass entire User object
                } ?: Fragment()
            }

            else -> throw IllegalArgumentException("Invalid tab position: $position")
        }

    }


    fun getIcon(position: Int): Drawable? {
        return ContextCompat.getDrawable(context, TAB_ICONS[position])
    }

    override fun getItemCount(): Int {
        return TAB_ICONS.size
    }
}