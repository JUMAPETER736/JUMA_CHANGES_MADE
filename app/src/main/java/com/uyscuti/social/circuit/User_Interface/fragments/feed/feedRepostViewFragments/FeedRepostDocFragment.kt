package com.uyscuti.social.circuit.User_Interface.fragments.feed.feedRepostViewFragments

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
import com.uyscuti.social.circuit.adapter.feed.OnFeedClickListener
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.adapter.feed.FeedAdapter
//import com.uyscuti.social.circuit.adapter.feed.feedRepostViewAdapter.FeedRepostDocumentViewAdapter
import com.uyscuti.social.circuit.databinding.FragmentFeedRepostDocBinding
import com.uyscuti.social.circuit.interfaces.feedinterfaces.FeedTextViewFragmentInterface
import com.uyscuti.social.core.common.data.room.entity.FollowUnFollowEntity
import com.uyscuti.social.network.api.response.posts.Post

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private lateinit var feedAdapter: FeedAdapter
/**
 * A simple [Fragment] subclass.
 * Use the [FeedRepostDocFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

private const val TAG = "FeedDocumentViewAdapter"

class FeedRepostDocFragment : Fragment(), OnFeedClickListener {

    private var currentCommentCount = 0

    // Add this function to update comment count
    fun updateCommentCount(newCount: Int) {
        currentCommentCount = newCount
        binding.feedCommentsCount.text = newCount.toString()
    }

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var data: com.uyscuti.social.network.api.response.posts.OriginalPost
    private var position = 0
    private lateinit var backPressedCallback: OnBackPressedCallback
    private var feedTextViewFragmentInterface: FeedTextViewFragmentInterface? = null
    private lateinit var binding: FragmentFeedRepostDocBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            position = it.getInt("position")
            data = (it.getSerializable("data") as com.uyscuti.social.network.api.response.posts.OriginalPost?)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentFeedRepostDocBinding.inflate(inflater, container, false)

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
            .load(data.author.account.avatar.url)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .apply(RequestOptions().transform(CircleCrop()).placeholder(R.drawable.profilepic2))
            .into(binding.toolbar.feedProfilePic)

        binding.toolbar.backIcon.setOnClickListener {
            feedTextViewFragmentInterface?.backPressedFromFeedTextViewFragment()
        }

        binding.commentButtonIcon.setOnClickListener {
            // Comment click implementation
        }

        val documentList: MutableList<String> = mutableListOf()
        if (data.files.isNotEmpty()) {
            for (document in data.files) {
                documentList.add(document.url)
            }
        }

        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.circleIndicator.setViewPager(binding.viewPager)

        // Initialize comment count
        currentCommentCount = data.commentCount ?: 0
        binding.feedCommentsCount.text = currentCommentCount.toString()

        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FeedRepostDocFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FeedRepostDocFragment().apply {
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
                feedTextViewFragmentInterface?.backPressedFromFeedTextViewFragment()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }

    fun setListener(listener: FeedTextViewFragmentInterface) {
        feedTextViewFragmentInterface = listener
    }

    override fun likeUnLikeFeed(position: Int, data: Post) {
        TODO("Not yet implemented")
    }

    override fun feedCommentClicked(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {
        TODO("Not yet implemented")
    }

    override fun feedFavoriteClick(position: Int, data: Post) {
        TODO("Not yet implemented")
    }

    override fun moreOptionsClick(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {
        TODO("Not yet implemented")
    }

    override fun feedFileClicked(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {
        TODO("Not yet implemented")
    }

    override fun feedRepostFileClicked(position: Int, data: com.uyscuti.social.network.api.response.posts.OriginalPost) {
        TODO("Not yet implemented")
    }

    override fun feedShareClicked(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {
        TODO("Not yet implemented")
    }

    override fun followButtonClicked(followUnFollowEntity: FollowUnFollowEntity, followButton: AppCompatButton) {
        TODO("Not yet implemented")
    }

    override fun feedRepostPost(position: Int, data: Post) {
        TODO("Not yet implemented")
    }

    override fun feedRepostPostClicked(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {
        TODO("Not yet implemented")
    }

    override fun feedClickedToOriginalPost(position: Int, originalPostId: String) {
        TODO("Not yet implemented")
    }

    override fun onImageClick() {
        TODO("Not yet implemented")
    }
}