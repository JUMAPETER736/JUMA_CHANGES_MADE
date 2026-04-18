package com.uyscuti.social.circuit.ui.fragments.feed.feedRepostViewFragmentsimport
import com.uyscuti.social.circuit.databinding.FragmentFeedRepostMultipleImageBinding


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.uyscuti.sharedmodule.adapter.feed.feed.MultipleFeedImagesAdapter
import com.uyscuti.sharedmodule.adapter.feed.feed.MultipleImagesListener
import com.uyscuti.social.circuit.R
import com.uyscuti.sharedmodule.interfaces.feedinterfaces.FeedTextViewFragmentInterface

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FeedRepostMultipleImageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
private const val TAG = "FeedMultipleImageViewFragment"
class FeedRepostMultipleImageFragment : Fragment() , MultipleImagesListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var data: com.uyscuti.social.network.api.response.allFeedRepostsPost.OriginalPost
    private var position = 0
    private lateinit var backPressedCallback: OnBackPressedCallback
    private var feedTextViewFragmentInterface: FeedTextViewFragmentInterface? = null

    private lateinit var binding: FragmentFeedRepostMultipleImageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            position = it.getInt("position")
            data =
                (it.getSerializable("data") as com.uyscuti.social.network.api.response.allFeedRepostsPost.OriginalPost?)!! // Adjust
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFeedRepostMultipleImageBinding.inflate(inflater, container, false)

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
            .placeholder(com.uyscuti.sharedmodule.R.drawable.profilepic2)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.toolbar.feedProfilePic)

        binding.toolbar.backIcon.setOnClickListener {

            if (feedTextViewFragmentInterface != null) {
                feedTextViewFragmentInterface?.backPressedFromFeedTextViewFragment()
            }
        }
        binding.commentButtonIcon.setOnClickListener {

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

        binding.viewPager.adapter = MultipleFeedImagesAdapter(requireContext(), imageList, this)
        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL


        binding.circleIndicator.setViewPager(binding.viewPager)
        binding.toolbar.username.text = data.author[0].account.username


        return binding.root
    }


}