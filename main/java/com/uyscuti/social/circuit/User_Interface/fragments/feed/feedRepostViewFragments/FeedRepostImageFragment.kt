package com.uyscuti.social.circuit.User_Interface.fragments.feed.feedRepostViewFragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.FragmentFeedRepostImageBinding
import com.uyscuti.social.circuit.interfaces.feedinterfaces.FeedTextViewFragmentInterface

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FeedRepostImageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
private const val TAG = "FeedRepostImageFragment"
class FeedRepostImageFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var data: com.uyscuti.social.network.api.response.allFeedRepostsPost.OriginalPost
    private var position = 0
    private lateinit var backPressedCallback: OnBackPressedCallback
    private var feedTextViewFragmentInterface: FeedTextViewFragmentInterface? = null

    private lateinit var binding : FragmentFeedRepostImageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            position = it.getInt("position")
            data = (it.getSerializable("data") as com.uyscuti.social.network.api.response.allFeedRepostsPost. OriginalPost)// Adjust
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  FragmentFeedRepostImageBinding.inflate(inflater, container, false)
        if (data.content == "") {
            binding.feedTextContent.text = ""
        } else {
            binding.feedTextContent.text = data.content
        }


        if (data.tags.isEmpty()) {
            binding.tags.visibility = View.GONE
        } else {
            binding.tags.visibility = View.VISIBLE
            val formattedTags = data.tags.joinToString(" ") { "#$it" }

            binding.tags.text = formattedTags
        }
        Glide.with(this)
            .load(data.files[0].url)
            .placeholder(R.drawable.profilepic2)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.feedImage)

        if (data.author.isEmpty()){
            Glide.with(this)
                .load(data.author[0].account.avatar.url)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .placeholder(R.drawable.profilepic2)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.toolbar.feedProfilePic)
        }else{
            Glide.with(this)
                .load(R.drawable.profilepic2)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .placeholder(R.drawable.profilepic2)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.toolbar.feedProfilePic)
        }


        binding.toolbar.backIcon.setOnClickListener {
//            feedTextViewFragmentInterface.onBackPressed()
//            navigateBack()
            if (feedTextViewFragmentInterface != null) {
                feedTextViewFragmentInterface?.backPressedFromFeedTextViewFragment()
            }else{
                Log.d(TAG, "onCreateView: feedTextViewFragmentInterface is null")
            }
        }
        binding.commentButtonIcon.setOnClickListener {
//            feedTextViewFragmentInterface?.onCommentClickFromFeedTextViewFragment(position, data)
//            binding.feedCommentsCount.text = (data.comments + 1).toString()
//            EventBus.getDefault().post(FeedCommentClicked(position, data))
        }


//        binding.toolbar.username.text = data.author!!.account.username
//        if (data.likes <= 0) {
//            binding.likesCount.text = "0"
//        } else {
//            binding.likesCount.text = data.likes.toString()
//        }
//        binding.feedCommentsCount .text ="${data.comments.size}"
//        binding.feedCommentsCount.text = data.comments.size.toString()
//        if (data.isLiked) {
//            binding.like.setImageResource(R.drawable.filled_favorite_like)
//        } else {
//            binding.like.setImageResource(R.drawable.like_svgrepo_com)
//        }
//        binding.like.setOnClickListener {
//            data.isLiked = !data.isLiked
//            if (data.isLiked) {
//                Log.d(com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.TAG, "onCreateView: data likes ${data.likes}")
//                binding.likesCount.text = data.likes.toString()
//                if (data.likes < 0) {
//                    binding.likesCount.text = "0"
//                } else {
//                    binding.likesCount.text = (data.likes + 1).toString()
//                }
//                binding.like.setImageResource(R.drawable.filled_favorite_like)
//                YoYo.with(Techniques.Tada)
//                    .duration(700)
//                    .repeat(1)
//                    .playOn(binding.like)
//            } else {
//                Log.d(com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.TAG, "onCreateView: data likes ${data.likes}")
//                binding.likesCount.text = data.likes.toString()
//                if (data.likes <= 0) {
//                    binding.likesCount.text = "0"
//                } else {
//                    binding.likesCount.text = (data.likes - 1).toString()
//                }
//                binding.like.setImageResource(R.drawable.like_svgrepo_com)
//                YoYo.with(Techniques.Tada)
//                    .duration(700)
//                    .repeat(1)
//                    .playOn(binding.like)
//            }
//            feedTextViewFragmentInterface?.onLikeUnLikeFeedFromFeedTextViewFragment(position, data)
//
//        }
//        if (data.isBookmarked) {
//            binding.fav.setImageResource(R.drawable.filled_favorite)
//        } else {
//            binding.fav.setImageResource(R.drawable.favorite_svgrepo_com__1_)
//        }
//
//        binding.moreOptions.setOnClickListener {
//            feedTextViewFragmentInterface?.onMoreOptionsClickFromFeedTextViewFragment(
//                position,
//                data
//            )
//        }
//        binding.fav.setOnClickListener {
//            data.isBookmarked = !data.isBookmarked
//            feedTextViewFragmentInterface?.onFeedFavoriteClickFromFeedTextViewFragment(
//                position,
//                data
//            )
//            if (data.isBookmarked) {
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
//        }
        return binding.root

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FeedRepostImageFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FeedRepostImageFragment().apply {
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

    fun setListener(listener: FeedTextViewFragmentInterface) {
        feedTextViewFragmentInterface = listener
    }
}