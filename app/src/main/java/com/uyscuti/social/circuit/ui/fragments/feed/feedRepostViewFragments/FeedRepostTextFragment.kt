package com.uyscuti.social.circuit.ui.fragments.feed.feedRepostViewFragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import com.uyscuti.sharedmodule.interfaces.feedinterfaces.FeedTextViewFragmentInterface
import com.uyscuti.sharedmodule.viewmodels.feed.FeedLiveDataViewModel
import com.uyscuti.social.circuit.databinding.FragmentFeedRepostTextBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FeedRepostTextFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

private const val TAG = "FeedTextViewFragment"

class FeedRepostTextFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var data: com.uyscuti.social.network.api.response.allFeedRepostsPost.OriginalPost
    private var position = 0
    private lateinit var backPressedCallback: OnBackPressedCallback
    private var feedTextViewFragmentInterface: FeedTextViewFragmentInterface? = null
    private val feedLiveDataViewModel: FeedLiveDataViewModel by activityViewModels()

    private lateinit var binding: FragmentFeedRepostTextBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            position = it.getInt("position")
            data =
                (it.getSerializable("data") as com.uyscuti.social.network.api.response.allFeedRepostsPost.OriginalPost?)!! //
        }
    }



}