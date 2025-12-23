package com.uyscuti.social.circuit.User_Interface.feedactivities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import androidx.transition.TransitionInflater
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.adapter.feed.multiple_files.MultipleFeedVideosAdapter
import com.uyscuti.social.circuit.adapter.feed.multiple_files.PlayFeedVideoInterface
import com.uyscuti.social.circuit.databinding.FragmentFeedVideoViewBinding
import com.uyscuti.social.circuit.feed_demo.VideoPagerAdapter
import com.uyscuti.social.circuit.interfaces.feedinterfaces.FeedTextViewFragmentInterface
import com.uyscuti.social.circuit.model.FeedCommentClicked
import org.greenrobot.eventbus.EventBus

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FeedVideoViewFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

private const val TAG = "FeedVideoViewFragment"

class FeedVideoViewFragment : Fragment(), PlayFeedVideoInterface {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var data: com.uyscuti.social.network.api.response.posts.Post
    private var position = 0
    private var videoPlayingPosition = -1;
    private lateinit var backPressedCallback: OnBackPressedCallback
    private var feedTextViewFragmentInterface: FeedTextViewFragmentInterface? = null
    private lateinit var binding: FragmentFeedVideoViewBinding

    var adapter: MultipleFeedVideosAdapter? = null
    private var adapter2: VideoPagerAdapter? = null

    //    private var isPlaying = false
    private var isUserSeeking = false
    private var currentDuration: TextView? = null


    var isPaused = false


    var videoUrl = ""
    var owner = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = TransitionInflater.from(requireContext())

         arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            position = it.getInt("position")
            data = (it.getSerializable("data") as com.uyscuti.social.network.api.response.posts.Post?)!!
        }
    }

    @OptIn(UnstableApi::class)
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        binding = FragmentFeedVideoViewBinding.inflate(layoutInflater, container, false)
        activity?.window?.navigationBarColor =
            ContextCompat.getColor(requireContext(), R.color.black)
        binding.toolbar.backIcon.setOnClickListener {
            if (feedTextViewFragmentInterface != null) {
                feedTextViewFragmentInterface?.backPressedFromFeedTextViewFragment()
                adapter2?.backPressedFromFeedTextViewFragment()

            }
        }
        Log.d(TAG, "onCreateView: data content ${data.content}")
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
        val videoList: MutableList<String> = mutableListOf()
        if (data.files.isNotEmpty()) {
            for (image in data.files) {
                Log.d(TAG, "render: images ${image.url}")
                videoList.add(image.url)
            }
        } else {
            Log.d(TAG, "render: data files is empty")
        }
        videoUrl = data.files[0].url
        var previousPosition: Int = -1

        adapter2 = VideoPagerAdapter(requireActivity(), videoList)
          binding.viewPager.adapter = adapter2

        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.circleIndicator.setViewPager(binding.viewPager)



        Glide.with(this)
            .load(data.author!!.account.avatar.url)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .placeholder(R.drawable.profilepic2)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.toolbar.feedProfilePic)

        binding.commentButtonIcon.setOnClickListener {
            EventBus.getDefault().post(FeedCommentClicked(position, data))
        }

        binding.toolbar.username.text = data.author!!.account.username
        if (data.likes  <= 0) {
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
                if (data.likes  < 0) {
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
//        where i have stopped
        binding.shareButtonIcon.setOnClickListener {
            val postLink = "https://yourwebsite.com/posts/${binding.feedImageCardView.id}" // Replace wi
            Toast.makeText(requireContext(), "share clicked", Toast.LENGTH_SHORT).show()
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT,postLink)
                type = "video"
            }
            if (sendIntent.resolveActivity(requireContext().packageManager) != null){
                startActivity(Intent.createChooser(sendIntent,"share via"))
            }
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
         * @return A new instance of fragment FeedVideoViewFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FeedVideoViewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onPause() {
        super.onPause()


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

    override fun onDestroy() {
        super.onDestroy()

    }

    fun setListener(listener: FeedTextViewFragmentInterface) {
        feedTextViewFragmentInterface = listener
    }

    @OptIn(UnstableApi::class)
    override fun onPlayClickListener(
        videoUrl: String,
        playerView: PlayerView,
        playImageView: ImageView,
        seekBars: SeekBar,
        currentDuration: TextView
    ) {

    }
}