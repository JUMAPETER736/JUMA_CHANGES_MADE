package com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments

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
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.transition.TransitionInflater
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.uyscuti.sharedmodule.interfaces.feedinterfaces.FeedTextViewFragmentInterface
import com.uyscuti.sharedmodule.R
import com.uyscuti.sharedmodule.databinding.FragmentFeedTextViewBinding
import com.uyscuti.sharedmodule.model.FeedCommentClicked
import com.uyscuti.sharedmodule.viewmodels.feed.FeedLiveDataViewModel
import org.greenrobot.eventbus.EventBus
import com.uyscuti.social.network.api.response.posts.Post

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FeedTextViewFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

private const val TAG = "FeedTextViewFragment"

class FeedTextViewFragment : Fragment() {

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FeedTextViewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)

                }
            }
    }


    private var param1: String? = null
    private var param2: String? = null
    private lateinit var data: Post
    private var position = 0
    private lateinit var backPressedCallback: OnBackPressedCallback
    private var feedTextViewFragmentInterface: FeedTextViewFragmentInterface? = null
    private val feedLiveDataViewModel: FeedLiveDataViewModel by activityViewModels()

    private lateinit var binding: FragmentFeedTextViewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = TransitionInflater.from(requireContext())

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            position = it.getInt("position")
            data = (it.getSerializable("data") as Post?)!! // Adjust type if needed
        }

    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentFeedTextViewBinding.inflate(inflater, container, false)

        activity?.window?.navigationBarColor =
            ContextCompat.getColor(requireContext(), R.color.black)
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
        binding.toolbar.username.text = data.author!!.account.username
        Glide.with(this)
            .load(data.author!!.account.avatar.url)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .placeholder(R.drawable.profilepic2)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.toolbar.feedProfilePic)

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

                val totalComments = data.comments
                Log.d(TAG, "onCreateView: data . comments ${data.comments} total comments $totalComments")
                binding.feedCommentsCount.text = "$totalComments"

                feedLiveDataViewModel.setBoolean(false)
            }

        }

        binding.share.setOnClickListener {

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
            binding.feedCommentsCount.text = "${data.comments+count}"

        }
        binding.comment.setOnClickListener {
            Log.d(TAG, "Comments: comment clicked")
            feedTextViewFragmentInterface?.onCommentClickFromFeedTextViewFragment(position, data)
            binding.feedCommentsCount.text = (data.comments + 1).toString()
            EventBus.getDefault().post(FeedCommentClicked(position, data))
        }
        binding.toolbar.username.text = data.author!!.account.username
        if (data.likes <= 0) {
            binding.likesCount.text = "0"
        }else {
            binding.likesCount.text = data.likes.toString()
        }

        if (data.isLiked) {
            binding.like.setImageResource(R.drawable.filled_favorite_like)
        } else {
            binding.like.setImageResource(R.drawable.like_svgrepo_com)
        }
        binding.like.setOnClickListener {
            data.isLiked = !data.isLiked
            if (data.isLiked) {
                Log.d(TAG, "onCreateView: data likes ${data.likes}")
                binding.likesCount.text = data.likes.toString()
                if (data.likes < 0) {
                    binding.likesCount.text = "0"
                } else {
                    binding.likesCount.text = (data.likes + 1).toString()
                }
                binding.like.setImageResource(R.drawable.filled_favorite_like)
                YoYo.with(Techniques.Tada)
                    .duration(700)
                    .repeat(1)
                    .playOn(binding.like)
            } else {
                Log.d(TAG, "onCreateView: data likes ${data.likes}")
                binding.likesCount.text = data.likes.toString()
                if (data.likes <= 0) {
                    binding.likesCount.text = "0"
                } else {
                    binding.likesCount.text = (data.likes - 1).toString()
                }
                binding.like.setImageResource(R.drawable.like_svgrepo_com)
                YoYo.with(Techniques.Tada)
                    .duration(700)
                    .repeat(1)
                    .playOn(binding.like)
            }
            feedTextViewFragmentInterface?.onLikeUnLikeFeedFromFeedTextViewFragment(position, data)
        }
        if (data.isBookmarked) {
            binding.fav.setImageResource(R.drawable.filled_favorite)
        } else {
            binding.fav.setImageResource(R.drawable.favorite_svgrepo_com__1_)
        }
        binding.moreOptions.setOnClickListener {
            feedTextViewFragmentInterface?.onMoreOptionsClickFromFeedTextViewFragment(
                position,
                data)

        }
        binding.fav.setOnClickListener {
            data.isBookmarked = !data.isBookmarked
            feedTextViewFragmentInterface?.onFeedFavoriteClickFromFeedTextViewFragment(position, data)
            if(data.isBookmarked) {
                binding.fav.setImageResource(R.drawable.filled_favorite)
                YoYo.with(Techniques.Tada)
                    .duration(700)
                    .repeat(1)
                    .playOn(binding.fav)
            } else {
                binding.fav.setImageResource(R.drawable.favorite_svgrepo_com__1_)
                YoYo.with(Techniques.Tada)
                    .duration(700)
                    .repeat(1)
                    .playOn(binding.fav)
            }
        }

        binding.re.setOnClickListener {
            data.isReposted = !data.isReposted
            if (data.isReposted) {
                Log.d(TAG, "reposted: data likes ${data.likes}")
                binding.retweetCount.text = data.likes.toString()

                }else {
                    Log.d(TAG, "reposted: data likes ${data.likes}")
                    binding.retweetCount.text = data.isReposted.toString()
            }
        }
        return binding.root
    }


    private fun navigateBack() {
        requireActivity().supportFragmentManager.popBackStack() // Pops the back stack
    }
    override fun onResume() {
        Log.d(TAG, "onResume: ")
        super.onResume()
        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle back press

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

}

