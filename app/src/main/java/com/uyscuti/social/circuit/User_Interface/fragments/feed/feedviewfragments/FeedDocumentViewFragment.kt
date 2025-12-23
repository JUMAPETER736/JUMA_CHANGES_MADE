package com.uyscuti.social.circuit.User_Interface.fragments.feed.feedviewfragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.uyscuti.social.circuit.adapter.feed.OnFeedClickListener
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.adapter.feed.MultipleFeedImagesAdapter
import com.uyscuti.social.circuit.adapter.feed.multiple_files.FeedDocumentsViewAdapter
import com.uyscuti.social.circuit.databinding.FragmentFeedDocViewBinding
import com.uyscuti.social.circuit.interfaces.feedinterfaces.FeedTextViewFragmentInterface
import com.uyscuti.social.circuit.model.FeedCommentClicked
import com.uyscuti.social.core.common.data.room.entity.FollowUnFollowEntity
import com.uyscuti.social.network.api.response.posts.Post
import org.greenrobot.eventbus.EventBus

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FeedDocumentViewFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
private const val TAG = "FeedDocViewFragment"
class FeedDocumentViewFragment : Fragment(), OnFeedClickListener {
    // TODO: Rename and change types of parameters

    private var param1: String? = null
    private var param2: String? = null
    private lateinit var data: com.uyscuti.social.network.api.response.posts.Post
    private var position = 0
    private lateinit var backPressedCallback: OnBackPressedCallback
    private var feedTextViewFragmentInterface: FeedTextViewFragmentInterface? = null
    private lateinit var binding: FragmentFeedDocViewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            position = it.getInt("position")
            data = (it.getSerializable("data") as  com.uyscuti.social.network.api.response.posts.Post?)!!
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =  FragmentFeedDocViewBinding.inflate(layoutInflater, container, false)
        activity?.window?.navigationBarColor =
            ContextCompat.getColor(requireContext(), R.color.black)
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
            .load(data.author!!.account.avatar.url)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .placeholder(R.drawable.profilepic2)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.toolbar.feedProfilePic)

        binding.toolbar.backIcon.setOnClickListener {
//            feedTextViewFragmentInterface.onBackPressed()
//            navigateBack()
            if (feedTextViewFragmentInterface != null) {
                feedTextViewFragmentInterface?.backPressedFromFeedTextViewFragment()
            }
        }
        binding.commentButtonIcon.setOnClickListener {
//            feedTextViewFragmentInterface?.onCommentClickFromFeedTextViewFragment(position, data)
//            binding.feedCommentsCount.text = (data.comments + 1).toString()
            EventBus.getDefault().post(FeedCommentClicked(position, data))
        }



        binding.toolbar.username.text = data.author!!.account.username
        if (data.likes <= 0) {
            binding.likesCount.text = "0"
        } else {
            binding.likesCount.text = data.likes.toString()
        }
//        binding.feedCommentsCount.text = data.comments.toString()
        if (data.isLiked) {
            binding.likeButtonIcon.setImageResource(R.drawable.filled_favorite_like)
        } else {
            binding.likeButtonIcon.setImageResource(R.drawable.like_svgrepo_com)
        }
        binding.likeButtonIcon.setOnClickListener {
            data.isLiked = !data.isLiked
            if (data.isLiked) {
                Log.d(TAG, "onCreateView: data likes ${data.likes}")
                binding.likesCount.text = data.likes.toString()
                if (data.likes < 0) {
                    binding.likesCount.text = "0"
                } else {
                    binding.likesCount.text = (data.likes + 1).toString()
                }
                binding.likeButtonIcon.setImageResource(R.drawable.filled_favorite_like)
                YoYo.with(Techniques.Tada)
                    .duration(700)
                    .repeat(1)
                    .playOn(binding.likeButtonIcon)
            } else {
                Log.d(TAG, "onCreateView: data likes ${data.likes}")
                binding.likesCount.text = data.likes.toString()
                if (data.likes <= 0) {
                    binding.likesCount.text = "0"
                } else {
                    binding.likesCount.text = (data.likes - 1).toString()
                }
                binding.likeButtonIcon.setImageResource(R.drawable.like_svgrepo_com)
                YoYo.with(Techniques.Tada)
                    .duration(700)
                    .repeat(1)
                    .playOn(binding.likeButtonIcon)
            }
            feedTextViewFragmentInterface?.onLikeUnLikeFeedFromFeedTextViewFragment(position, data)

        }
        if (data.isBookmarked) {

            binding.favoriteSection.setImageResource(R.drawable.filled_favorite)

        } else {

            binding.favoriteSection.setImageResource(R.drawable.favorite_svgrepo_com__1_)

        }

        binding.moreOptions.setOnClickListener {

            feedTextViewFragmentInterface?.onMoreOptionsClickFromFeedTextViewFragment(
                position,
                data

            )

        }
        binding.favoriteSection.setOnClickListener {
            data.isBookmarked = !data.isBookmarked
            feedTextViewFragmentInterface?.onFeedFavoriteClickFromFeedTextViewFragment(
                position,
                data
            )
            if (data.isBookmarked) {
                binding.favoriteSection.setImageResource(R.drawable.filled_favorite)
                YoYo.with(Techniques.Tada)
                    .duration(700)
                    .repeat(1)
                    .playOn(binding.favoriteSection)
            } else {
                binding.favoriteSection.setImageResource(R.drawable.favorite_svgrepo_com__1_)
                YoYo.with(Techniques.Tada)
                    .duration(700)
                    .repeat(1)
                    .playOn(binding.favoriteSection)
            }
        }
        binding.repost.setOnClickListener {



        }

        val documentList:MutableList<String> = mutableListOf()
        if(data.files.isNotEmpty()) {
            for (document in data.files) {
                Log.d(TAG, "render: document ${document.url}")
                documentList.add(document.url)
            }
        }else {
            Log.d(TAG, "render: data files is empty")
        }

        val imageList: MutableList<String> = mutableListOf()

        val context = requireContext()

        val feedDocumentAdapter = FeedDocumentsViewAdapter(imageList, context)

       feedDocumentAdapter.setDocumentList(documentList)
        feedDocumentAdapter.setData(data)
        context?.let { feedDocumentAdapter.setContext(it) }
        binding.viewPager.adapter = MultipleFeedImagesAdapter(requireContext(), imageList, this)
        binding.viewPager.adapter = feedDocumentAdapter

        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.circleIndicator.setViewPager(binding.viewPager)
        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FeedDocViewFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FeedDocumentViewFragment().apply {
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

    override fun likeUnLikeFeed(
        position: Int,
        data: Post
    ) {
        TODO("Not yet implemented")
    }

    override fun feedCommentClicked(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.Post
    ) {
        TODO("Not yet implemented")
    }

    override fun feedFavoriteClick(
        position: Int,
        data: Post
    ) {
        TODO("Not yet implemented")
    }

    override fun moreOptionsClick(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.Post
    ) {
        TODO("Not yet implemented")
    }

    override fun feedFileClicked(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.Post
    ) {
        TODO("Not yet implemented")
    }

    override fun feedRepostFileClicked(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.OriginalPost
    ) {
        TODO("Not yet implemented")
    }



    override fun feedShareClicked(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.Post
    ) {
        TODO("Not yet implemented")
    }

    override fun followButtonClicked(
        followUnFollowEntity: FollowUnFollowEntity,
        followButton: AppCompatButton
    ) {
        TODO("Not yet implemented")
    }

    override fun feedRepostPost(
        position: Int,
        data: Post
    ) {
        TODO("Not yet implemented")
    }

    override fun feedRepostPostClicked(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.Post
    ) {
        TODO("Not yet implemented")
    }

    override fun feedClickedToOriginalPost(position: Int, originalPostId: String) {
        TODO("Not yet implemented")
    }

    override fun onImageClick() {
        TODO("Not yet implemented")
    }

}