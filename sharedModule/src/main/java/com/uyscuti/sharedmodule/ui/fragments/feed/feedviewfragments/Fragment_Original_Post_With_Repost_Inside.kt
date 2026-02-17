package com.uyscuti.sharedmodule.ui.fragments.feed.feedviewfragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toolbar
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.uyscuti.sharedmodule.R
import com.uyscuti.sharedmodule.adapter.feed.FeedAdapter
import com.uyscuti.sharedmodule.adapter.feed.OnFeedClickListener
import com.uyscuti.sharedmodule.utils.FollowingManager
import com.uyscuti.social.network.api.response.posts.OriginalPost
import com.uyscuti.social.network.api.response.posts.Post
import com.uyscuti.social.core.common.data.room.entity.FollowUnFollowEntity
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject



@AndroidEntryPoint
class Fragment_Original_Post_With_Repost_Inside : Fragment() {

    @Inject
    lateinit var retrofitInstance: RetrofitInstance

    private lateinit var feedAdapter: FeedAdapter
    private lateinit var recyclerView: RecyclerView
    private var postData: Post? = null

    companion object {
        private const val ARG_POST_DATA = "post_data"
        const val ARG_ORIGINAL_POST = "original_post"

        fun newInstance(post: Post): Fragment_Original_Post_With_Repost_Inside {
            return Fragment_Original_Post_With_Repost_Inside().apply {
                arguments = Bundle().apply {
                    putString(ARG_POST_DATA, Gson().toJson(post))
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getString(ARG_POST_DATA)?.let { jsonData ->
            postData = Gson().fromJson(jsonData, Post::class.java)
        } ?: arguments?.getString(ARG_ORIGINAL_POST)?.let { jsonData ->
            postData = Gson().fromJson(jsonData, Post::class.java)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_original_post_with_repost_inside, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar(view)
        setupRecyclerView(view)
        loadPostData()
    }

    private fun setupToolbar(view: View) {
        view.findViewById<Toolbar>(R.id.toolbar)?.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)

        feedAdapter = FeedAdapter(
            context = requireContext(),
            retrofitInterface = retrofitInstance,
            feedClickListener = createFeedClickListener(),
            fragmentManager = parentFragmentManager,
            followingUserIds = getFollowingUserIds()
        )

        // Set the RecyclerView to the adapter (this initializes pagination)
        feedAdapter.recyclerView = recyclerView
    }

    private fun loadPostData() {
        postData?.let { post ->
            // Use submitItem() method from FeedPaginatedAdapter
            feedAdapter.submitItem(post)
        }
    }

    private fun getFollowingUserIds(): Set<String> {
        return FollowingManager(requireContext()).getFollowingList().toSet()
    }

    private fun createFeedClickListener(): OnFeedClickListener {
        return object : OnFeedClickListener {

            override fun likeUnLikeFeed(position: Int, data: Post) {
                // FeedRepostViewHolder already handles everything
            }

            override fun feedCommentClicked(position: Int, data: Post) {
                // FeedRepostViewHolder already handles everything
            }

            override fun feedFavoriteClick(position: Int, data: Post) {
                // FeedRepostViewHolder already handles everything
            }

            override fun feedRepostPost(position: Int, data: Post) {
                // FeedRepostViewHolder already handles everything
            }

            override fun feedShareClicked(position: Int, data: Post) {
                // FeedRepostViewHolder already handles everything
            }

            override fun feedFileClicked(position: Int, data: Post) {
                // FeedMixedFilesViewAdapter already handles everything
            }

            override fun feedRepostFileClicked(position: Int, data: OriginalPost) {
                // FeedMixedFilesViewAdapter already handles everything
            }

            override fun followButtonClicked(
                followUnFollowEntity: FollowUnFollowEntity,
                followButton: AppCompatButton
            ) {
                // FeedRepostViewHolder already handles everything
            }

            override fun moreOptionsClick(position: Int, data: Post) {
                // FeedRepostViewHolder already handles everything
            }

            override fun feedRepostPostClicked(position: Int, data: Post) {
                // Already viewing the repost
            }

            override fun feedClickedToOriginalPost(position: Int, originalPostId: String) {
                // FeedRepostViewHolder already handles everything
            }

            override fun onImageClick() {
                // FeedMixedFilesViewAdapter already handles everything
            }
        }
    }
}


