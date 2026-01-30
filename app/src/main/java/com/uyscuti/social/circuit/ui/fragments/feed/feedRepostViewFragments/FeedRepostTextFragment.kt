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



private const val TAG = "FeedTextViewFragment"

class FeedRepostTextFragment : Fragment() {


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
            data = (it.getSerializable("data") as  com.uyscuti.social.network.api.response.allFeedRepostsPost.OriginalPost?)!! //
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

           // Create an Intent with action Intent.ACTION_SEND
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
            // Update UI with new count value

        }
        binding.commentButtonIcon.setOnClickListener {

        }
        binding.favoriteSection.setOnClickListener {
//            data.isBookmarked = !data.isBookmarked
//            feedTextViewFragmentInterface?.onFeedFavoriteClickFromFeedTextViewFragment(position, data)
//            if(data.isBookmarked) {
//                binding.fav.setImageResource(R.drawable.filled_favorite)
//                YoYo.with(Techniques.Tada)
//                    .duration(700)
//                    .repeat(1)
//                    .playOn(binding.fav)
//            } else {
//                binding.fav.setImageResource(R.drawable.favorite_svgrepo_com__1_)
//                YoYo.with(Techniques.Tada)
//                    .duration(700)
//                    .repeat(1)
//                    .playOn(binding.fav)
//            }
        }

        binding.re.setOnClickListener {
//            data.isReposted = !data.isReposted
//            if (data.isReposted) {
//                Log.d(TAG, "reposted: data likes ${data.likes}")
//                binding.retweetCount.text = data.likes.toString()
//
//            }else {
//                Log.d(TAG, "reposted: data likes ${data.likes}")
//                binding.retweetCount.text = data.isReposted.toString()
//            }
        }
        return binding.root

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FeedRepostTextFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FeedRepostTextFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onResume() {
        Log.d(TAG, "onResume: ")
        super.onResume()
        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle back press
//                navigateBack()
                if (feedTextViewFragmentInterface != null) {
                    feedTextViewFragmentInterface?.backPressedFromFeedTextViewFragment()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }
    override fun onDetach() {
        super.onDetach()
        Log.d(TAG, "onDetach: ")
    }
    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: ")
    }
    override fun onPause() {
        super.onPause()
        // Remove the callback to prevent leaks
        Log.d(TAG, "onPause: ")
        backPressedCallback.remove()
    }
    fun setListener(listener: FeedTextViewFragmentInterface) {
        feedTextViewFragmentInterface = listener
    }
    @SuppressLint("SetTextI18n")
    fun setFeedCommentsCount() {
//        Log.d(TAG, "setFeedCommentsCount: data.comments+1: ${data.comments+1} ")
//        binding.feedCommentsCount.text = "${data.comments +1}"
    }

}