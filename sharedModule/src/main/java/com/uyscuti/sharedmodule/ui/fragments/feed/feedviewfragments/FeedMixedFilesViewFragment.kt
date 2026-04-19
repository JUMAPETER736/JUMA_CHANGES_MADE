package com.uyscuti.sharedmodule.ui.fragments.feed.feedviewfragments

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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
import com.google.android.material.card.MaterialCardView
import com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.FeedTextViewFragment
import com.uyscuti.sharedmodule.eventbus.HideFeedFloatingActionButton
import com.uyscuti.sharedmodule.interfaces.feedinterfaces.FeedTextViewFragmentInterface
import com.uyscuti.sharedmodule.R
import com.uyscuti.sharedmodule.adapter.UserListAdapter
import com.uyscuti.sharedmodule.adapter.feed.feed.ShareFeedPostAdapter
import com.uyscuti.sharedmodule.adapter.feed.feed.multiple_files.FeedMixedFilesViewPagerAdapter
import com.uyscuti.sharedmodule.databinding.FragmentFeedMixedFilesViewBinding
import com.uyscuti.sharedmodule.model.FeedCommentClicked
import com.uyscuti.sharedmodule.viewmodels.feed.FeedUploadViewModel
import org.greenrobot.eventbus.EventBus
import com.uyscuti.social.network.api.response.posts.Post

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FeedMixedFilesViewFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

private const val TAG = "FeedMixedFilesViewFragment"

class FeedMixedFilesViewFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var feedTextViewFragment: FeedTextViewFragment? = null
    private lateinit var frameLayout: FrameLayout
    private lateinit var binding: FragmentFeedMixedFilesViewBinding
    private lateinit var feedUploadViewModel: FeedUploadViewModel
    private lateinit var data:  Post
    private var position = 0
    private lateinit var backPressedCallback: OnBackPressedCallback
    private var feedTextViewFragmentInterface: FeedTextViewFragmentInterface? = null
    private var adapter: FeedMixedFilesViewPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = TransitionInflater.from(requireContext())
//        enterTransition = inflater.inflateTransition(R.transition.feed_slide_from_top)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            position = it.getInt("position")
            data = (it.getSerializable("data") as Post?)!!
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentFeedMixedFilesViewBinding.inflate(layoutInflater, container, false)
        EventBus.getDefault().post(HideFeedFloatingActionButton())
        activity?.window?.navigationBarColor =
            ContextCompat.getColor(requireContext(), R.color.black)
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
        Log.d(TAG, "onCreateView: data content ${data.content}")
        Glide.with(this)
            .load(data.author!!.account.avatar.url)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .placeholder(R.drawable.profilepic2)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.toolbar.feedProfilePic)
        if (data.content == "") {
            binding.caption.visibility = View.GONE
        } else {
            binding.caption.visibility = View.VISIBLE
            binding.caption.text = data.content
        }
        if (data.tags.isEmpty()) {
            binding.tags.visibility = View.GONE
        } else {
            binding.tags.visibility = View.VISIBLE
            val formattedTags = data.tags.joinToString(" ") { "#$it" }
            binding.tags.text = formattedTags
        }
        binding.share.setOnClickListener {
//            val bottomSheet = BottomSheetFragment()
//            bottomSheet.show(requireActivity().supportFragmentManager, "BottomSheetFeedFragment")
//            Toast.makeText(context, "share clicked is here", Toast.LENGTH_SHORT).show()
            ShareClicked()

        }
        binding.comment.setOnClickListener {
            EventBus.getDefault().post(FeedCommentClicked(position, data))
        }

        binding.toolbar.username.text = data.author!!.account.username
        if (data.likes <= 0) {
            binding.likesCount.text = "0"

        } else {
            binding.likesCount.text = data.likes.toString()
        }
//        binding.feedCommentsCount.text = "${data.comments.size}"
//        binding.feedCommentsCount.text = data.comments.toString()
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
                if (data.likes <= 0) {

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
            Log.d(TAG, "onCreateView: data likes ${data.likes}")
            binding.favCount.text = data.bookmarkCount.toString()
            binding.fav.setImageResource(R.drawable.filled_favorite)
        } else {
            binding.fav.setImageResource(R.drawable.favorite_svgrepo_com__1_)
        }

        binding.moreOptions.setOnClickListener {
            feedTextViewFragmentInterface?.onMoreOptionsClickFromFeedTextViewFragment(
                position,
                data
            )
        }
        binding.fav.setOnClickListener {
            data.isBookmarked = !data.isBookmarked
            feedTextViewFragmentInterface?.onFeedFavoriteClickFromFeedTextViewFragment(
                position,
                data
            )
            if (data.isBookmarked) {
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

            repostClicked()

        }

        adapter = FeedMixedFilesViewPagerAdapter(requireActivity(), data)
        adapter!!.setFeedPostPosition(position)

        binding.viewPager.adapter = adapter
        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.circleIndicator.setViewPager(binding.viewPager)

        return binding.root
    }

    binding.re.animate().rotation(360f).setDuration(500).start()
}

private fun replaceFragment (fragment: Fragment){
    val supportFragmentManager = requireActivity().supportFragmentManager
    val fragmentTransaction = supportFragmentManager.beginTransaction()
    fragmentTransaction.add(R.id.fragment_frame, fragment)
    fragmentTransaction.commit()
}

fun setListener(listener: FeedTextViewFragmentInterface) {
    feedTextViewFragmentInterface = listener
}
private fun ShareClicked (){
    val context = requireContext()

    val bottomSheetDialog = BottomSheetDialog(requireContext())
    val shareView = layoutInflater.inflate(R.layout.example, null)
    val close_button = shareView.findViewById<ImageButton>(R.id.close_button)
    val recyclerView = shareView.findViewById<RecyclerView>(R.id.apps_recycler_view)
    val userRecyclerView = shareView.findViewById<RecyclerView>(R.id.users_recycler_view)
    val people_search_container = shareView.findViewById<LinearLayout>(R.id.people_search_container)
    bottomSheetDialog.setContentView(shareView)
    bottomSheetDialog.show()

    recyclerView.visibility = View.GONE
    people_search_container.visibility = View.GONE

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
