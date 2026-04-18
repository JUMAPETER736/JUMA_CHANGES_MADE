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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFeedRepostTextBinding.inflate(inflater, container, false)
        binding.feedTextContent.text = data.content
        if (data.tags.isEmpty()) {
            binding.tags.visibility = View.GONE
        } else {
            binding.tags.visibility = View.VISIBLE
            val formattedTags = data.tags.joinToString(" ") { "#$it" }
            binding.tags.text = formattedTags
        }
        if (data.content == "") {
            binding.feedTextContent.text = ""
        } else {
            binding.feedTextContent.text = data.content
        }

        if (data.author.isEmpty()){
            binding.toolbar.username.text = "Unknown"
        }else {
            binding.toolbar.username.text = data.author[0].account.username
        }

        binding.toolbar.backIcon.setOnClickListener {

            if (feedTextViewFragmentInterface != null) {
                feedTextViewFragmentInterface?.backPressedFromFeedTextViewFragment()
            }
        }
        feedLiveDataViewModel.booleanValue.observe(viewLifecycleOwner) { value ->
            // Update UI based on the boolean value
            Log.d(TAG, "onCreateView: value $value")

            if (value) {
                // Handle true state

                val totalComments = data.commentCount.toString()

                binding.feedCommentsCount.text = "$totalComments"

                feedLiveDataViewModel.setBoolean(false)
            }

        }

        binding.shareButtonIcon.setOnClickListener {

            Toast.makeText(requireContext(), "share clicked", Toast.LENGTH_SHORT).show()
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, binding.feedTextContent.text.toString())
                type = "text/plain"
            }

            //  Verify that the Intent will resolve to an activity
            if (sendIntent.resolveActivity(requireContext().packageManager) != null) {
                // Start the activity to share the text
                startActivity(Intent.createChooser(sendIntent, "Share via"))
            }
        }
        feedLiveDataViewModel.counter.observe(viewLifecycleOwner) { count ->

        }
        binding.commentButtonIcon.setOnClickListener {

        }

        binding.favoriteSection.setOnClickListener {

        }

        binding.re.setOnClickListener {

        }
        return binding.root

    }



}