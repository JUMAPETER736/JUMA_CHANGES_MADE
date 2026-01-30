package com.uyscuti.social.circuit.ui.fragments.feed.RepostedRepostFragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import com.uyscuti.sharedmodule.adapter.feed.feed.feedRepostedRepostViewAdapter.FeedRostedViewAdapter
import com.uyscuti.social.circuit.databinding.FragmentFeedRepostedRepostViewBinding
import com.uyscuti.sharedmodule.interfaces.feedinterfaces.FeedTextViewFragmentInterface


// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


private const val TAG = "FeedRepostedRepostViewFragment"

class FeedRepostedRepostViewFragment : Fragment() {

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String, originalPostId: String) =
            FeedRepostedRepostViewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)

                    putString("originalPostId", originalPostId)
                }
            }
    }

    private var param1: String? = null
    private var param2: String? = null
    private var adapter : FeedRostedViewAdapter?= null
    private lateinit var backPressedCallback: OnBackPressedCallback
    private var feedTextViewFragmentInterface: FeedTextViewFragmentInterface? = null
    private lateinit var data : com.uyscuti.social.network.api.response.allFeedRepostsPost.OriginalPost
    private lateinit var binding : FragmentFeedRepostedRepostViewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            data = it.getSerializable("data") as com.uyscuti.social.network.api.response.allFeedRepostsPost.OriginalPost
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentFeedRepostedRepostViewBinding.inflate(inflater, container, false)


        binding.toolbar.backIcon.setOnClickListener {
            if (feedTextViewFragmentInterface != null) {
                feedTextViewFragmentInterface?.backPressedFromFeedTextViewFragment()
                adapter?.backPressedFromFeedTextViewFragment()
            }
        }
        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (feedTextViewFragmentInterface != null) {
                    feedTextViewFragmentInterface?.backPressedFromFeedTextViewFragment()
                }
            }
        }
        return binding.root
    }



    fun setListener(listener: FeedTextViewFragmentInterface) {
        feedTextViewFragmentInterface = listener
    }

    override fun onResume() {
        Log.d(TAG, "onResume: ")
        super.onResume()
        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                if (feedTextViewFragmentInterface != null) {
                    feedTextViewFragmentInterface?.backPressedFromFeedTextViewFragment()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }
}