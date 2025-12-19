package com.uyscuti.sharedmodule.ui.fragments.feed.feedviewfragments.editRepost

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.uyscuti.sharedmodule.R
import com.uyscuti.sharedmodule.adapter.feed.feed.multiple_files.FeedRepostViewFileAdapter
import com.uyscuti.sharedmodule.databinding.FragmentEditPostToRepostBinding
import com.uyscuti.sharedmodule.eventbus.ShowFeedFloatingActionButton
import com.uyscuti.sharedmodule.interfaces.feedinterfaces.FeedTextViewFragmentInterface
import com.uyscuti.sharedmodule.model.ShowAppBar
import com.uyscuti.sharedmodule.model.ShowBottomNav
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Fragment_Edit_Post_To_Repost.newInstance] factory method to
 * create an instance of this fragment.
 */
private const val TAG = "NewRepostedPostFragment"
@AndroidEntryPoint
class Fragment_Edit_Post_To_Repost(val data: com.uyscuti.social.network.api.response.posts.Post) : Fragment(), FeedTextViewFragmentInterface {

    private var param1: String? = null
    private var param2: String? = null
    private lateinit var avatar: String
    private lateinit var context: Context
    private lateinit var binding: FragmentEditPostToRepostBinding
    private lateinit var feedListView: RecyclerView
    private lateinit var frameLayout: FrameLayout
    private lateinit var repostUser: SharedPreferences
    private var position = 0
    private lateinit var backPressedCallback: OnBackPressedCallback
    private val PREFS_NAME = "LocalSettings" // Change this to a unique name for your app
    private var feedTextViewFragmentInterface: FeedTextViewFragmentInterface? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            position = it.getInt("position")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentEditPostToRepostBinding.inflate(inflater, container, false)
        repostUser = requireActivity().getSharedPreferences(PREFS_NAME, 0)
        avatar = repostUser.getString("avatar", "").toString()
        val username = repostUser.getString("username", "").toString()
        val userId = repostUser.getString("userId", "").toString()

        context = requireContext()


        Glide.with(this)
            .load(avatar)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .placeholder(R.drawable.profilepic2)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.userprofile)

        binding.backButton.setOnClickListener {
            Log.d("button","clicked again")
            if (feedTextViewFragmentInterface != null) {
                feedTextViewFragmentInterface?.backPressedFromFeedTextViewFragment()
            }
        }

        val fileList: MutableList<String> = mutableListOf()
        if (data.files.isNotEmpty()) {
            for (file in data.files) {
                Log.d(TAG, "render: images ${file.url}")
                fileList.add(file.url)
            }
        } else {
            Log.d(TAG, "render: data files is empty")
        }

        try {
            when (data.contentType) {

                "text" -> {
                    if (data.content.isNotEmpty()){
                        Log.d("clicked", "render: original post text")
                        binding.originalFeedTextContent.visibility = View.VISIBLE
                        binding.originalFeedTextContent.text = data.content
                        binding.mixedFilesCardView.visibility = View.GONE
//                nestedOriginalPostSnippet.text = data.content
                    }else {
                        binding.originalFeedTextContent.visibility = View.GONE
                        binding.mixedFilesCardView.visibility = View.VISIBLE
                    }
                }

                "mixed_files" -> {

                    Log.d("clicked", "render: original post mixed files")
                    binding.mixedFilesCardView.visibility = View.VISIBLE
                    if (data.files.isNotEmpty()) {
                        Log.d("clicked", "render: data files are empty")
                        binding.originalFeedTextContent.visibility = View.VISIBLE
                        binding.mixedFilesCardView.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.VISIBLE
                    } else {
                        binding.originalFeedTextContent.visibility = View.GONE
                        binding.mixedFilesCardView.visibility = View.GONE
                    }
                    // Display original post text if availab le
                    if (data.content.isNotEmpty()) {
                        binding.originalFeedTextContent.visibility = View.VISIBLE
                        binding.originalFeedTextContent.text = data.content
                    } else {
                        binding.originalFeedTextContent.visibility = View.GONE
                    }

                    var adapter: FeedRepostViewFileAdapter? = null

                    if (data.originalPost.isNotEmpty()) {
                        val originalPost = data.originalPost[0] // Extract the first OriginalPost
                        val adapter = FeedRepostViewFileAdapter(originalPost) // Pass the correct type
                        binding.recyclerView.adapter = adapter // Set the adapter to the RecyclerView
                    } else {
                        Log.e(TAG, "No OriginalPost available to display")
                        binding.recyclerView.visibility = View.GONE // Hide RecyclerView if no OriginalPost exists
                    }

                    when (fileList.size) {
                        1 -> {
                            binding.recyclerView.layoutManager = GridLayoutManager(
                                requireContext(), 1)
                            binding.recyclerView.setHasFixedSize(true) // Ensures items won't change size
                            binding.recyclerView.adapter = adapter
                        }
                        2 -> {
                            binding.recyclerView.layoutManager =
                                GridLayoutManager(requireContext(), 2) // 2 columns in the grid
                            binding.recyclerView.setHasFixedSize(true) // Ensures items won't change size
                            binding.recyclerView.adapter = adapter // Replace with your adapter
                            // Replace with your adapter
                        }

                        3 -> {
                            // Use a GridLayoutManager with span size 2 for the first row and 1 for the second row
                            val layoutManager = GridLayoutManager(requireContext(), 2)
                            layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                                override fun getSpanSize(position: Int): Int {
                                    return if (position < 2) 1 else 2
                                }
                            }
                            binding.recyclerView.layoutManager = layoutManager
                            binding.recyclerView.setHasFixedSize(true) // Ensures items won't change size
                            binding.recyclerView.adapter = adapter // Replace with your adapter
                        }
                        else -> {
                            val layoutManager = GridLayoutManager(requireContext(), 2)


                            layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                                override fun getSpanSize(position: Int): Int {
                                    return when (position) {
                                        0, 1 -> 1  // First and second items span 2 columns
                                        else -> 1   // All other items span 1 column
                                    }
                                }
                            }
                            binding. recyclerView.layoutManager = layoutManager
                            binding.recyclerView.adapter = adapter
                        }
                    }
                } "image" -> {
                    if (data.files.isNotEmpty()) {
                        Log.d("clicked", "render: image content")
                        binding.originalFeedTextContent.visibility = View.VISIBLE
                        binding.originalFeedTextContent.text = data.originalPost[0].content
                        binding.originalFeedImage.visibility = View.VISIBLE
                        Glide.with(context)
                            .load(data.files[0].url) // Assuming the image URL is in `files[0].url`
                            .placeholder(R.drawable.flash21)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(binding.originalFeedImage)
                    } else {
                        Log.d("clicked", "render: image content")
                        binding.originalFeedTextContent.visibility = View.GONE
                    }
                }

                "video" -> {
                    Log.d("clicked", "render: video content")
                    // Hide other views
                    if (data.files.isNotEmpty()) {
                        binding.originalFeedTextContent.visibility = View.VISIBLE
                        binding.mixedFilesCardView.visibility = View.VISIBLE
                    } else {
                        binding.originalFeedTextContent.visibility = View.GONE
                        binding.mixedFilesCardView.visibility = View.GONE
                    }
                }
                else -> {
                }
            }
        }catch (e: Exception){
            Log.d("Exception", "onCreate: ${e.message}")
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
         * @return A new instance of fragment EditPostToRepost.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String,data: com.uyscuti.social.network.api.response.posts.Post) =
            Fragment_Edit_Post_To_Repost(data).apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
                EventBus.getDefault().register(this)

            }
    }

    override fun backPressedFromFeedTextViewFragment() {
        Log.d("BACKPRESS", "backPressedFromFeedTextViewFragment: listening back pressed ")
        feedListView.visibility = View.VISIBLE
        frameLayout.visibility = View.GONE
        EventBus.getDefault().post(ShowBottomNav())
        EventBus.getDefault().post(ShowAppBar())
        EventBus.getDefault().post(ShowFeedFloatingActionButton(true))
    }

    override fun onCommentClickFromFeedTextViewFragment(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {
        TODO("Not yet implemented")
    }

    override fun onLikeUnLikeFeedFromFeedTextViewFragment(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {
        TODO("Not yet implemented")
    }

    override fun onFeedFavoriteClickFromFeedTextViewFragment(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {
        TODO("Not yet implemented")
    }

    override fun onMoreOptionsClickFromFeedTextViewFragment(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {
        TODO("Not yet implemented")
    }

    override fun finishedPlayingVideo(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onRePostClickFromFeedTextViewFragment(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {
        TODO("Not yet implemented")
    }
    fun onImageClick() {
        TODO("Not yet implemented")
    }
    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onResume() {
        super.onResume()
        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (feedTextViewFragmentInterface != null) {
                    feedTextViewFragmentInterface?.backPressedFromFeedTextViewFragment()
                    backPressedFromFeedTextViewFragment()
                }
            }
        }
    }
}