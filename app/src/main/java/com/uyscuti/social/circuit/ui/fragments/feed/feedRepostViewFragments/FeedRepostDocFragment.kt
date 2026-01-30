package com.uyscuti.social.circuit.ui.fragments.feed.feedRepostViewFragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatButton
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.uyscuti.sharedmodule.adapter.feed.OnFeedClickListener
import com.uyscuti.social.circuit.R
//import com.uyscuti.social.circuit.adapter.feed.feedRepostViewAdapter.FeedRepostDocumentViewAdapter
import com.uyscuti.social.circuit.databinding.FragmentFeedRepostDocBinding
import com.uyscuti.sharedmodule.interfaces.feedinterfaces.FeedTextViewFragmentInterface
import com.uyscuti.social.core.common.data.room.entity.FollowUnFollowEntity


// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


private const val TAG = "FeedDocumentViewAdapter"

class FeedRepostDocFragment : Fragment(), OnFeedClickListener {

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FeedRepostDocFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private var currentCommentCount = 0

    // Add this function to update comment count
    fun updateCommentCount(newCount: Int) {
        currentCommentCount = newCount
        binding.feedCommentsCount.text = newCount.toString()
    }



    private var param1: String? = null
    private var param2: String? = null
    private lateinit var data: com.uyscuti.social.network.api.response.allFeedRepostsPost. OriginalPost
    private var position = 0
    private lateinit var backPressedCallback: OnBackPressedCallback
    private var feedTextViewFragmentInterface: FeedTextViewFragmentInterface? = null
    private lateinit var  binding: FragmentFeedRepostDocBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            position = it.getInt("position")
            data = (it.getSerializable("data") as  com.uyscuti.social.network.api.response.allFeedRepostsPost.OriginalPost?)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFeedRepostDocBinding.inflate(layoutInflater, container, false)
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
            .load(data.author[0].account.avatar.url)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .placeholder(R.drawable.profilepic2)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.toolbar.feedProfilePic)

        binding.toolbar.backIcon.setOnClickListener {

            if (feedTextViewFragmentInterface != null) {
                feedTextViewFragmentInterface?.backPressedFromFeedTextViewFragment()
            }
       }


        val documentList:MutableList<String> = mutableListOf()

        if(data.files.isNotEmpty()) {
            for (document in data.files) {
                documentList.add(document.url)
            }

        }else {

        }


        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.circleIndicator.setViewPager(binding.viewPager)
        return binding.root


        // Initialize comment count
        currentCommentCount = data.commentCount ?: 0
        binding.feedCommentsCount.text = currentCommentCount.toString()

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
    fun setListener(listener: FeedTextViewFragmentInterface) {
        feedTextViewFragmentInterface = listener
    }

    override fun likeUnLikeFeed(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.Post
    ) {

    }

    override fun feedCommentClicked(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.Post
    ) {

    }

    override fun feedFavoriteClick(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.Post
    ) {

    }

    override fun moreOptionsClick(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.Post
    ) {

    }

    override fun feedFileClicked(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.Post
    ) {

    }

    override fun feedRepostFileClicked(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.OriginalPost
    ) {

    }



    override fun feedShareClicked(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.Post
    ) {

    }

    override fun followButtonClicked(
        followUnFollowEntity: FollowUnFollowEntity,
        followButton: AppCompatButton
    ) {

    }

    override fun feedRepostPost(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.Post
    ) {

    }

    override fun feedRepostPostClicked(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.Post
    ) {

    }

    override fun feedClickedToOriginalPost(position: Int, originalPostId: String) {

    }

    override fun onImageClick() {

    }
}