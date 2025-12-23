package com.uyscuti.social.circuit.User_Interface.fragments.forshorts

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog

import com.uyscuti.social.circuit.adapter.feed.FeedAdapter
import com.uyscuti.social.circuit.adapter.feed.FeedPaginatedAdapter
import com.uyscuti.social.circuit.adapter.feed.OnFeedClickListener
import com.uyscuti.social.circuit.eventbus.AllFeedUpdateLike
import com.uyscuti.social.circuit.eventbus.FeedFavoriteFollowUpdate
import com.uyscuti.social.circuit.eventbus.FeedLikeClick
import com.uyscuti.social.circuit.eventbus.FromFavoriteFragmentFeedLikeClick
import com.uyscuti.social.circuit.eventbus.FromOtherUsersFeedCommentClick
import com.uyscuti.social.circuit.eventbus.FromOtherUsersFeedFavoriteClick
import com.uyscuti.social.circuit.eventbus.InformOtherUsersFeedProfileFragment
import com.uyscuti.social.circuit.eventbus.InformShortsFragment2
import com.uyscuti.social.circuit.utils.removeDuplicateFollowers
import com.uyscuti.social.circuit.viewmodels.FeedShortsViewModel
import com.uyscuti.social.circuit.viewmodels.FollowUnfollowViewModel
import com.uyscuti.social.circuit.viewmodels.feed.FeedUploadViewModel
import com.uyscuti.social.circuit.viewmodels.feed.GetFeedViewModel
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.FragmentOtherUsersFeedProfileBinding
import com.uyscuti.social.core.common.data.room.entity.FollowUnFollowEntity
import com.uyscuti.social.core.common.data.room.entity.ShortsEntityFollowList
import com.uyscuti.social.network.api.response.posts.Post
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"



private const val TAG = "OtherUsersFeedProfileFragment"

@AndroidEntryPoint
class OtherUsersFeedProfileFragment : Fragment(), OnFeedClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    var username = ""

    @Inject
    lateinit var retrofitInstance: RetrofitInstance

    private lateinit var binding: FragmentOtherUsersFeedProfileBinding
    private lateinit var otherUserFeedAdapter: FeedAdapter

    private val getFeedViewModel: GetFeedViewModel by activityViewModels()
    private val feedShortsSharedViewModel: FeedShortsViewModel by activityViewModels()
    private val followUnFollowViewModel: FollowUnfollowViewModel by viewModels()
    private val feedUploadViewModel: FeedUploadViewModel by activityViewModels()

    //    private val feesShortsSharedViewModel: FeedShortsViewModel by activityViewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            username = it.getString("username").toString()
        }

        EventBus.getDefault().register(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentOtherUsersFeedProfileBinding.inflate(layoutInflater, container, false)


        otherUserFeedAdapter = FeedAdapter(
            requireActivity(),
            this
        )
        otherUserFeedAdapter.recyclerView = binding.rv
        binding.rv.itemAnimator = null

        binding.rv.layoutManager = LinearLayoutManager(requireContext())
//        allFeedAdapter.setDefaultRecyclerView(requireActivity(), allFeedAdapterRecyclerView.id)
        otherUserFeedAdapter.setOnPaginationListener(object :
            FeedPaginatedAdapter.OnPaginationListener {
            override fun onCurrentPage(page: Int) {
//                Toast.makeText(requireContext(), "Page $page loaded!", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "currentPage: page number $page")

            }

            override fun onNextPage(page: Int) {
                lifecycleScope.launch(Dispatchers.Main) {
//                    loadMoreShorts(page)
                    Log.d(TAG, "onNextPage: page number $page")
                    getOtherUserFeed(page)
//                    allFeedAdapter.submitItems(getFeedViewModel.getAllFeedData())
                }
            }

            override fun onFinish() {
                Log.d(TAG, "finished: page number")

//                Toast.makeText(requireContext(), "finish", Toast.LENGTH_SHORT).show()
            }
        })

        lifecycleScope.launch(Dispatchers.Main)
        {
//            Log.d(TAG, "onCreateView: ${getFeedViewModel.getAllFeedData()}")
            if (getFeedViewModel.getAllFeedData().isEmpty()) {
//                Log.d(TAG, "onCreateView: get all feed data is empty")
                getOtherUserFeed(otherUserFeedAdapter.startPage)
            } else {
                Log.d(TAG, "onCreateView: get all feed data is not empty")
            }
            getFeedViewModel.isFeedDataAvailable.observe(viewLifecycleOwner) { isDataAvailable ->
                // Handle the updated value of isResuming here
                if (isDataAvailable) {
                    // Do something when isResuming is true
                    Log.d(TAG, "onCreateView: data is available")
//                    allFeedAdapter.item
                    otherUserFeedAdapter.submitItems(getFeedViewModel.getAllFeedData())
                    otherUserFeedAdapter.addFollowList(getFeedViewModel.getFollowList())
//                    getFeedViewModel.setIsDataAvailable(false)

                } else {
                    // Do something when isResuming is false
                    Log.d(TAG, "onCreateView: data not added")

                }
            }
            getFeedViewModel.isSingleFeedAvailable.observe(viewLifecycleOwner) { isDataAvailable ->
                // Handle the updated value of isResuming here
                if (isDataAvailable) {
                    // Do something when isResuming is true
                    Log.d(TAG, "onCreateView: data is available")
//                    allFeedAdapter.item
//                    allFeedAdapter.submitItems(getFeedViewModel.getAllFeedData())
                    otherUserFeedAdapter.submitItem(getFeedViewModel.getSingleAllFeedData(), 0)
//                    allFeedAdapter.addFollowList(getFeedViewModel.getFollowList())
                    binding.rv.smoothScrollToPosition(0)
//                    getFeedViewModel.setIsDataAvailable(false)

                } else {
                    // Do something when isResuming is false
                    Log.d(TAG, "onCreateView: data not added")

                }
            }
        }

        feedShortsSharedViewModel.data.observe(viewLifecycleOwner) { newData ->
            Log.d(
                "feesShortsSharedViewModel",
                "onCreateView: data from all shorts fragment $newData"
            )

//            otherUserFeedAdapter.addSingleFollowList(
//                com.uyscut.network.api.response.getrepostsPostsoriginal.Follow(
//                    newData.userId,
//                    newData.isFollowing
//                )
//            )

//            EventBus.getDefault().post(
//                FeedFavoriteFollowUpdate(
//                    newData.userId,
//                    newData.isFollowing
//                )
//            )
//            getFeedViewModel.addFollowToFollowList(
//                com.uyscut.network.api.response.getrepostsPostsoriginal.Follow(
//                    newData.userId,
//                    newData.isFollowing
//                )
//            )
        }
//        lifecycleScope.launch(Dispatchers.Main)
//        {
//            getOtherUserFeed(otherUserFeedAdapter.startPage)
//        }
        return binding.root
    }

    fun getOtherUserFeed(page: Int) {
        val TAG = "getOtherUserFeed"
        Log.d(
            TAG,
            "getMyFeed: page number $page username $username"
        )
        CoroutineScope(Dispatchers.IO).launch {
            try {

                val response = retrofitInstance.apiService.getOtherUserFeed(
                    username,
                    page.toString()
                )
                val responseBody = response.body()
                Log.d(TAG, "feed: response body message ${responseBody!!.message}")

                Log.d(TAG, "getMyFeed: feedCount ${responseBody.data.posts.posts[0]}")
                val data = responseBody.data
//                getFeedViewModel.addAllFeedData(data.posts.posts)
//                getFeedViewModel.setFollowList(data.followList)
//                getFeedViewModel.addMyFeedData(data.posts.toMutableList())
//                withContext(Dispatchers.Main) {
//                    otherUserFeedAdapter.submitItems(data.posts.posts)
//                    responseBody.data.let { otherUserFeedAdapter.addFollowList(it.followList) }
//                }

            } catch (e: Exception) {
                Log.e(TAG, "getOtherUserFeed: $e")
                Log.e(TAG, "getOtherUserFeed: ${e.message}")
                e.printStackTrace()
            }
        }

    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            OtherUsersFeedProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        fun newInstance(username: String): OtherUsersFeedProfileFragment {
            val fragment = OtherUsersFeedProfileFragment()
            val args = Bundle()
            args.putString("username", username)
            fragment.arguments = args
            return fragment
        }
    }

    override fun likeUnLikeFeed(position: Int, data: Post) {
        try {
            val updatedFeed = if (data.isLiked) {
                data.copy(
                    likes = data.likes + 1,
                    isLiked = data.isLiked,
                    repostedByUserId = data.repostedByUserId ?: ""
                )
            } else {
                data.copy(
                    likes = data.likes - 1,
                    isLiked = data.isLiked,
                    repostedByUserId = data.repostedByUserId ?: ""
                )
            }
            lifecycleScope.launch {
                feedUploadViewModel.likeUnLikeFeed(data._id)
                feedShortsSharedViewModel.data
            }
            EventBus.getDefault().post(
                AllFeedUpdateLike(
                    position, updatedFeed
                )
            )
            EventBus.getDefault().post(FromFavoriteFragmentFeedLikeClick(position, updatedFeed))

            Log.d("likeUnLikeFeed", "likeUnLikeFeed: likes count is ${data.likes}")
            val updatedItems = getFeedViewModel.getAllFeedData()
//            feedShortsSharedViewModel.setAllFeedData(updatedFeed)
            for (updatedItem in updatedItems) {

                if (updatedItem._id == data._id) {
                    updatedItem.isLiked = data.isLiked
                    if (data.isLiked) {
                        updatedItem.likes += 1
                    } else {
                        updatedItem.likes -= 1
                    }
                }
            }


            val isFavoriteFeedDataEmpty = getFeedViewModel.getAllFavoriteFeedData().isEmpty()
            if (!isFavoriteFeedDataEmpty) {
                val favoriteFeedData = getFeedViewModel.getAllFavoriteFeedData()
                val feedToUpdate = favoriteFeedData.find { feed -> feed._id == data._id }
                if (feedToUpdate != null) {
                    EventBus.getDefault().post(FeedLikeClick(position, updatedFeed))
                    Log.d("likeUnLikeFeed", "likeUnLikeFeed: remove feed from favorite fragment")
                } else {
                    Log.d("likeUnLikeFeed", "likeUnLikeFeed: add feed to favorite fragment")
                }
            } else {
                Log.i("likeUnLikeFeed", "likeUnLikeFeed: my feed data is empty")
            }
            otherUserFeedAdapter.updateItem(position, updatedFeed)
            val isMyFeedEmpty = getFeedViewModel.getMyFeedData().isEmpty()
            if (!isMyFeedEmpty) {
                val myFeedData = getFeedViewModel.getMyFeedData()
                val feedToUpdate = myFeedData.find { feed -> feed._id == data._id }
                if (feedToUpdate != null) {
                    feedToUpdate.isLiked = data.isLiked
                    feedToUpdate.likes = data.likes
                    val myFeedDataPosition =
                        getFeedViewModel.getMyFeedPositionById(feedToUpdate._id)
                    getFeedViewModel.updateMyFeedData(myFeedDataPosition, feedToUpdate)
                } else {
                    Log.d(TAG, "likeUnLikeFeed: feed to update is not available in the list")
                }
            } else {
                Log.i(TAG, "likeUnLikeFeed: my feed data is empty")
            }
        } catch (e: Exception) {
            Log.e("likeUnLikeFeed", "likeUnLikeFeed: ${e.message}")
            e.printStackTrace()
        }
    }
    override fun feedCommentClicked(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {

        EventBus.getDefault().post(
            FromOtherUsersFeedCommentClick(
                position,
                data
            )
        )
    }

    override fun feedFavoriteClick(position: Int, data: Post) {
        EventBus.getDefault().post(FromOtherUsersFeedFavoriteClick(position, data))
        lifecycleScope.launch {
            feedUploadViewModel.favoriteFeed(data._id)
        }
//        EventBus.getDefault().post(FromFavoriteFragmentFeedFavoriteClick(position, data))
    }

    private fun followClicked(followUnFollowEntity: FollowUnFollowEntity) {
        Log.d("followButtonClicked", "followButtonClicked: $followUnFollowEntity")


        val followListItem: List<ShortsEntityFollowList> = listOf(
            ShortsEntityFollowList(
                followUnFollowEntity.userId, followUnFollowEntity.isFollowing
            )
        )

        lifecycleScope.launch(Dispatchers.IO) {
//            delay(200)
            val uniqueFollowList = removeDuplicateFollowers(followListItem)

            followUnFollowViewModel.followUnFollow(followUnFollowEntity.userId)
            Log.d(
                "followButtonClicked",
                "followButtonClicked: Inserted uniqueFollowList $uniqueFollowList"
            )
            delay(100)
        }
    }

    @SuppressLint("InflateParams")
    override fun moreOptionsClick(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {
        val view: View = layoutInflater.inflate(R.layout.feed_moreoptions_bottomsheet_layout, null)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(view)
        dialog.show()
    }

    override fun feedFileClicked(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {

    }

    override fun feedRepostFileClicked(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.OriginalPost
    ) {
        TODO("Not yet implemented")
    }

//    override fun feedRepostFileClicked(position: Int, data: OriginalPost) {
//    }

    override fun feedShareClicked(position: Int, data:com.uyscuti.social.network.api.response.posts.Post) {

    }

    override fun followButtonClicked(
        followUnFollowEntity: FollowUnFollowEntity,
        followButton: AppCompatButton
    ) {
        Log.d("followButtonClicked", "followButtonClicked: clicked")
//        EventBus.getDefault().post(ShortsFollowButtonClicked(followUnFollowEntity))
//        val followList = allFeedAdapter.getFollowList()
//        otherUserFeedAdapter.addSingleFollowList(
//            com.uyscuti.social.network.api.response.allFeedRepostsPost.Post(
//                followUnFollowEntity.userId,
//                followUnFollowEntity.isFollowing
//            )
//        )
        EventBus.getDefault().post(
            FeedFavoriteFollowUpdate(
                followUnFollowEntity.userId,
                followUnFollowEntity.isFollowing
            )
        )
        EventBus.getDefault().post(
            InformShortsFragment2(
                followUnFollowEntity.userId,
                followUnFollowEntity.isFollowing
            )
        )
        feedShortsSharedViewModel.setData(
            FollowUnFollowEntity(
                followUnFollowEntity.userId,
                followUnFollowEntity.isFollowing
            )
        )
//        getFeedViewModel.addFollowToFollowList(
//            com.uyscuti.social.network.api.response.getrepostsPostsoriginal.Follow(
//                followUnFollowEntity.userId,
//                followUnFollowEntity.isFollowing
//            )
//        )
        followClicked(followUnFollowEntity)
    }

    override fun feedRepostPost(position: Int, data: Post) {
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun likeFeedClick(event: FromFavoriteFragmentFeedLikeClick) {
        Log.d(
            TAG,
            "likeFeedClick: event bus position ${event.position} is bookmarked ${event.data.isLiked}"
        )
        val feedPosition = otherUserFeedAdapter.getPositionById(event.data._id)
        otherUserFeedAdapter.updateItem(feedPosition, event.data)
        getFeedViewModel.updateForAllFeedFragment(feedPosition, event.data)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun feedInformOtherUsersFeedProfileFragment(event: InformOtherUsersFeedProfileFragment) {
//        Log.d("InformOtherUsersFeedProfileFragment", "InformOtherUsersFeedProfileFragment: feed follow update")
//        otherUserFeedAdapter.addSingleFollowList(
//            com.uyscut.network.api.response.allFeedRepostsPost.Follow(
//                event.userId,
//                event.isFollowing
//            )
//        )
//        getFeedViewModel.addFollowToFollowList(
//            com.uyscut.network.api.response.allFeedRepostsPost.Follow(
//                event.userId,
//                event.isFollowing
//            )
//        )
    }




    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}