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
import com.uyscuti.sharedmodule.fragments.MyUserBusinessProfileFragment
import com.uyscuti.sharedmodule.ui.fragments.FragmentFactoryRegistry


private val TAB_ICONS = arrayOf(

    R.drawable.play_svgrepo_com,
    R.drawable.scroll_text_line_svgrepo_com,
    R.drawable.business_bag_svgrepo_com,
)




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

