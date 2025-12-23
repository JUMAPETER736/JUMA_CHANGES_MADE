package com.uyscuti.social.circuit.User_Interface.fragments.feed.feedviewfragments

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionInflater
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.adapter.UserListAdapter
import com.uyscuti.social.circuit.adapter.feed.MultipleFeedImagesAdapter
import com.uyscuti.social.circuit.adapter.feed.MultipleImagesListener
import com.uyscuti.social.circuit.adapter.feed.ShareFeedPostAdapter
import com.uyscuti.social.circuit.databinding.FragmentFeedMultipleImageViewBinding
import com.uyscuti.social.circuit.interfaces.feedinterfaces.FeedTextViewFragmentInterface
import com.uyscuti.social.circuit.model.FeedCommentClicked
import org.greenrobot.eventbus.EventBus

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FeedMultipleImageViewFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

private const val TAG = "FeedMultipleImageViewFragment"
class FeedMultipleImageViewFragment : Fragment(), MultipleImagesListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var data: com.uyscuti.social.network.api.response.posts.Post
    private var position = 0
    private lateinit var backPressedCallback: OnBackPressedCallback
    private var feedTextViewFragmentInterface: FeedTextViewFragmentInterface? = null

    private lateinit var binding: FragmentFeedMultipleImageViewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = TransitionInflater.from(requireContext())
//        enterTransition = inflater.inflateTransition(R.transition.feed_slide_from_top)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            position = it.getInt("position")
            data = (it.getSerializable("data") as com.uyscuti.social.network.api.response.posts. Post?)!! // Adjust type if needed
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =  FragmentFeedMultipleImageViewBinding.inflate(layoutInflater, container, false)

        activity?.window?.navigationBarColor =
            ContextCompat.getColor(requireContext(), R.color.black)
        if (data.content == "") {
            binding.feedTextContent.text = ""
        } else {
            binding.feedTextContent.text = data.content
        }

        if(data.tags.isEmpty()) {
            binding.tags.visibility = View.GONE
        }else {
            binding.tags.visibility = View.VISIBLE
        }

        Glide.with(this)
            .load(data.author)
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
        val imageList:MutableList<String> = mutableListOf()
        if(data.files.isNotEmpty()) {
            for (image in data.files) {
                Log.d(TAG, "render: images ${image.url}")
                imageList.add(image.url)
            }
        }else {
            Log.d(TAG, "render: data files is empty")
        }
//      Setup ViewPager2 with the adapter
        binding.viewPager.adapter = MultipleFeedImagesAdapter(requireContext(), imageList, this)
        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        // Setup CircleIndicator for ViewPager2
//      val indicator = findViewById<CircleIndicator3>(R.id.circleIndicator)
        binding.circleIndicator.setViewPager(binding.viewPager)
        binding.toolbar.username.text = data.author!!.account.username
        if (data.likes <= 0) {
            binding.likesCount.text = "0"
        } else {
            binding.likesCount.text = data.likes.toString()
        }
        binding.feedCommentsCount.text = data.comments.toString()
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
        binding.re.setOnClickListener {
            shareClicked()

        }
        return binding.root
    }

    private fun shareClicked (){
        val context = requireContext()

        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val shareView = layoutInflater.inflate(R.layout.bottom_dialog_for_share, null)
        val close_button = shareView.findViewById<ImageButton>(R.id.close_button)
        val recyclerView = shareView.findViewById<RecyclerView>(R.id.apps_recycler_view)
        val userRecyclerView = shareView.findViewById<RecyclerView>(R.id.users_recycler_view)

        bottomSheetDialog.setContentView(shareView)
        bottomSheetDialog.show()

        close_button.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        // Fetch installed apps that support sharing
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain" }
        val resolveInfoList = packageManager?.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = resolveInfoList?.let { ShareFeedPostAdapter(it, context, data) }
        userRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        userRecyclerView.adapter = UserListAdapter(context) { user ->

        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FeedMultipleImageViewFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FeedMultipleImageViewFragment().apply {
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

    override fun onImageClick() {

    }

}