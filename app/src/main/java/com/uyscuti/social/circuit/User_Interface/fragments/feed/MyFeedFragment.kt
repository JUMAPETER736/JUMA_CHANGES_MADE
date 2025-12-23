package com.uyscuti.social.circuit.User_Interface.fragments.feed

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.uyscuti.social.circuit.adapter.feed.FeedAdapter
import com.uyscuti.social.circuit.adapter.feed.OnFeedClickListener
import com.uyscuti.social.circuit.model.FeedAdapterNotifyDatasetChanged
import com.uyscuti.social.circuit.model.FeedCommentClicked
import com.uyscuti.social.circuit.viewmodels.GetShortsByUsernameViewModel
import com.uyscuti.social.circuit.viewmodels.feed.FeedUploadViewModel
import com.uyscuti.social.circuit.viewmodels.feed.GetFeedViewModel
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.adapter.feed.FeedPaginatedAdapter
import com.uyscuti.social.core.common.data.room.entity.FollowUnFollowEntity
import com.uyscuti.social.network.api.response.posts.Post
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
/**
 * A simple [Fragment] subclass.
 * Use the [MyFeedFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
private const val TAG = "MyFeedFragment"
@AndroidEntryPoint
class MyFeedFragment : Fragment(), OnFeedClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    private val getFeedViewModel: GetFeedViewModel by activityViewModels()
    private val feedUploadViewModel: FeedUploadViewModel by activityViewModels()

    private lateinit var myFeedAdapterRecyclerView: RecyclerView
    private lateinit var myFeedAdapter: FeedAdapter

    @Inject
    lateinit var retrofitInstance: RetrofitInstance
    private val shortsViewModel: GetShortsByUsernameViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        EventBus.getDefault().register(this)
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun feedAdapterNotifyDatasetChanged(event: FeedAdapterNotifyDatasetChanged) {
        Log.d(TAG, "FeedAdapterNotifyDatasetChanged: in feed adapter notify adapter: seh data set changed")
        myFeedAdapter.notifyDataSetChanged()
//        myFeedAdapter.notifyItemChanged(event.position)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_my_feed, container, false)
        myFeedAdapterRecyclerView = view.findViewById(R.id.rv)
        myFeedAdapter = FeedAdapter(
            requireActivity(),
            this
        )
        myFeedAdapter.recyclerView = myFeedAdapterRecyclerView
        myFeedAdapterRecyclerView.itemAnimator = null
        myFeedAdapterRecyclerView.layoutManager = LinearLayoutManager(requireContext())
//        allFeedAdapter.setDefaultRecyclerView(requireActivity(), allFeedAdapterRecyclerView.id)
        myFeedAdapter.setOnPaginationListener(object : FeedPaginatedAdapter.OnPaginationListener {
            override fun onCurrentPage(page: Int) {
//                Toast.makeText(requireContext(), "Page $page loaded!", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "currentPage: page number $page")

            }
            override fun onNextPage(page: Int) {
                lifecycleScope.launch(Dispatchers.Main) {
//                    loadMoreShorts(page)
                    Log.d(TAG, "onNextPage: page number $page")
                    getMyFeed(page)
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
//            Log.d(TAG, "onCreateView: ${getFeedViewModel.getAllFeedData()?.get(0)?.contentType}")
            Log.d(TAG, "onCreateView: ${getFeedViewModel.getMyFeedData()}")
            if (getFeedViewModel.getMyFeedData().isEmpty()) {
                Log.d(TAG, "onCreateView: get all feed data is empty")
                getMyFeed(myFeedAdapter.startPage)
//                getFeedReposts(myFeedAdapter.startPage)
            } else {
                Log.d(TAG, "onCreateView: get all feed data is not empty")
            }

            getFeedViewModel.isMineFeedDataAvailable.observe(viewLifecycleOwner) { isDataAvailable ->
                // Handle the updated value of isResuming here
                if (isDataAvailable) {
                    // Do something when isResuming is true
                    Log.d(TAG, "onCreateView: data is available")
//                    allFeedAdapter.item
                    myFeedAdapter.submitItems(getFeedViewModel.getMyFeedData())

//                    getFeedViewModel.setIsDataAvailable(false)

                } else {
                    // Do something when isResuming is false
                    Log.d(TAG, "onCreateView: data not added")

                }
            }
        }

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MyFeedFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MyFeedFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }



    override fun likeUnLikeFeed(position: Int, data: Post) {
        try {
            val updatedComment = if (data.isLiked) {
                data.copy(
                    likes = data.likes + 1,
                    repostedByUserId = data.repostedByUserId?:"",
                    isLiked = true

                )
            } else {
                data.copy(
                    likes = data.likes - 1,
                    repostedByUserId = data.repostedByUserId?:"",
                    isLiked = false

                )
            }
            lifecycleScope.launch {
                feedUploadViewModel.likeUnLikeFeed(data._id)
            }
            Log.d("likeUnLikeFeed", "likeUnLikeFeed: likes count is ${data.likes}")
            val updatedItems = getFeedViewModel.getMyFeedData()

            for (updatedItem in updatedItems) {

                if (updatedItem._id == data._id) {
                    if (data.isLiked) {
                        updatedItem.likes += 1
                    } else {
                        updatedItem.likes -= 1
                    }
                }
            }

//            EventBus.getDefault().post(FromFavoriteFragmentFeedLikeClick(position, updatedComment))
            myFeedAdapter.updateItem(position, updatedComment)
            val isAllFeedDataEmpty = getFeedViewModel.getAllFeedData().isEmpty()
            val isFavoriteFeedDataEmpty = getFeedViewModel.getAllFavoriteFeedData().isEmpty()

            if(!isAllFeedDataEmpty) {
                val allFeedData = getFeedViewModel.getAllFeedData()
                val feedToUpdate = allFeedData.find { feed -> feed._id == data._id }
                if (feedToUpdate != null) {
                    feedToUpdate.isLiked = data.isLiked
                    feedToUpdate.likes = data.likes

                    val allFeedDataPosition = getFeedViewModel.getAllFeedDataPositionById(feedToUpdate._id)
                    getFeedViewModel.updateForAllFeedFragment(allFeedDataPosition, feedToUpdate)
                }else {
                    Log.d(TAG, "likeUnLikeFeed: feed to update is not available in the list")
                }
            }else {
                Log.i(TAG, "likeUnLikeFeed: my feed data is empty")
            }

            if(!isFavoriteFeedDataEmpty) {
                val favoriteFeedData = getFeedViewModel.getAllFavoriteFeedData()
                val feedToUpdate = favoriteFeedData.find { feed -> feed._id == data._id }
                if (feedToUpdate != null) {
                    val favoriteFeedDataPosition = getFeedViewModel.getPositionById(feedToUpdate._id)
                    feedToUpdate.isLiked = data.isLiked
                    feedToUpdate.likes = data.likes
                    getFeedViewModel.updateForFavoriteFragment(favoriteFeedDataPosition, feedToUpdate)
                    Log.d(TAG, "feedFavoriteClick: remove feed from favorite fragment")
                }else {
                    Log.d(TAG, "feedFavoriteClick: add feed to favorite fragment")
                    getFeedViewModel.addFavoriteFeed(0, data)

                }
            }else {
                Log.i(TAG, "likeUnLikeFeed: my feed data is empty")
            }


        } catch (e: Exception) {
            Log.e(TAG, "likeUnLikeFeed: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun feedCommentClicked(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {
        EventBus.getDefault().post(FeedCommentClicked(position, data))
    }

    override fun feedFavoriteClick(position: Int, data: Post) {

        val isAllFeedDataEmpty = getFeedViewModel.getAllFeedData().isEmpty()
        val isFavoriteFeedDataEmpty = getFeedViewModel.getAllFavoriteFeedData().isEmpty()

        if(!isAllFeedDataEmpty) {
            val allFeedData = getFeedViewModel.getAllFeedData()
            val feedToUpdate = allFeedData.find { feed -> feed._id == data._id }
            if (feedToUpdate != null) {
                feedToUpdate.isBookmarked = data.isBookmarked

                val allFeedDataPosition = getFeedViewModel.getAllFeedDataPositionById(feedToUpdate._id)
                getFeedViewModel.updateForAllFeedFragment(allFeedDataPosition, feedToUpdate)
            }else {
                Log.d(TAG, "feedFavoriteClick: feed to update is not available in the list")
            }
        }else {
            Log.i(TAG, "feedFavoriteClick: my feed data is empty")
        }

        if(!isFavoriteFeedDataEmpty) {
            val favoriteFeedData = getFeedViewModel.getAllFavoriteFeedData()
            val feedToUpdate = favoriteFeedData.find { feed -> feed._id == data._id }
            if (feedToUpdate != null) {
                val favoriteFeedDataPosition = getFeedViewModel.getPositionById(feedToUpdate._id)
                getFeedViewModel.removeFavoriteFeed(favoriteFeedDataPosition)
                Log.d(TAG, "feedFavoriteClick: remove feed from favorite fragment")
            }else {
                Log.d(TAG, "feedFavoriteClick: add feed to favorite fragment")
                getFeedViewModel.addFavoriteFeed(0, data)

            }
        }else {
            Log.i(TAG, "feedFavoriteClick: my feed data is empty")
        }
        lifecycleScope.launch {
            feedUploadViewModel.favoriteFeed(data._id)

        }
    }


    fun getMyFeed(page: Int) {
        val TAG = "AllFeedTag"

        Log.d(
            TAG,
            "getAllFeed: page number $page"
        )
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = retrofitInstance.apiService.getAllFeed(
                    page.toString()
                )
                val responseBody = response.body()
                Log.d(TAG, "Feed Feed getAllFeed feed: response  nse $response")
                Log.d(TAG, "Feed Feed getAllFeed feed: response message ${response.message()}")
                Log.d(TAG, "Feed Feed getAllFeed feed: response message error body ${response.errorBody()}")
                Log.d(TAG, "Feed Feed getAllFeed feed: response body $responseBody")
//                Log.d(TAG, "Feed Feed getAllFeed feed: response body data ${responseBody?.data?.posts?.posts?.get(4)}")
                Log.d("AllFeedTag", "Feed Feed getAllFeed feed: response body message ${responseBody!!.message}")
                val data = responseBody.data


                // Safely access the 4th element if it exists
                val posts = responseBody.data.data.posts

                if (posts.size > 4) {
                    Log.d(TAG, "Feed Feed getAllFeed feed: response body data ${posts[4]}")
                } else {
                    Log.d(TAG, "Feed Feed getAllFeed feed: Not enough posts, size=${posts.size}")
                }
                Log.d(TAG, "Feed Feed getAllFeed feed: response body data ${data.data.posts.size}")
                withContext(Dispatchers.Main) {
                    getFeedViewModel.addAllFeedData(data.data.posts.toMutableList())
                    myFeedAdapter.submitItems(getFeedViewModel.getAllFeedData())
                    val posts = getFeedViewModel.getAllFeedData()
                    Log.d(TAG, "getAllFeed: posts :$posts")

                    myFeedAdapter.submitItems(posts)
                }

            } catch (e: Exception) {
                Log.e(TAG, "comment: $e")
                Log.e(TAG, "comment: ${e.message}")
                e.printStackTrace()
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: called")
        EventBus.getDefault().unregister(this)
    }
    @SuppressLint("InflateParams")
    override fun moreOptionsClick(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {
//        Log.d(TAG, "moreOptionsClick: More options clicked")
        val view: View = layoutInflater.inflate(R.layout.feed_moreoptions_bottomsheet_layout, null)
//        val deleteFeedLayout: LinearLayout = view.findViewById(R.id.deleteFeedLayout)
          val hidePostLayout : MaterialCardView = view.findViewById(R.id.hidePostLayout)
//        deleteFeedLayout.visibility = View.VISIBLE
        hidePostLayout.setOnClickListener {
//            Log.d(TAG,"DELETE LAYOUT HAS BEEN CLICKED")
            showDeleteConfirmationDialog(data._id, position)
        }
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(view)
        dialog.show()
    }

    override fun feedFileClicked(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {

    }



    override fun feedRepostFileClicked(position: Int, data: com.uyscuti.social.network.api.response.posts.OriginalPost) {
        TODO("Not yet implemented")
    }

    override fun feedShareClicked(
        position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {



    }

    override fun followButtonClicked(
        followUnFollowEntity: FollowUnFollowEntity,
        followButton: AppCompatButton
    ) {

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

    @SuppressLint("InflateParams")
    private fun showDeleteConfirmationDialog(feedId: String, position: Int) {
        val inflater = LayoutInflater.from(requireContext())
        val customTitleView: View = inflater.inflate(R.layout.delete_title_custom_layout, null)
        val builder = AlertDialog.Builder(requireContext())
//        builder.setTitle("Delete Feed Confirmation")
        builder.setCustomTitle(customTitleView)
        builder.setMessage("Are you sure you want to delete this feed?")

        // Positive Button
        builder.setPositiveButton("Delete") { dialog, which ->
            // Handle delete action
            dialog.dismiss()
            handleDeleteAction(feedId = feedId, position){ isSuccess, message ->
                if (isSuccess) {
                    Log.d(TAG, "handleDeleteAction $message")
                    dialog.dismiss()
                } else {
                    dialog.dismiss()
                    Log.e(TAG, "handleDeleteAction $message")
                }}
            dialog.dismiss()
        }

        // Negative Button
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss() // Dismiss the dialog
        }

        // Create and show the AlertDialog
        val alertDialog = builder.create()
        alertDialog.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun handleDeleteAction(feedId: String, position: Int, callback: (Boolean, String) -> Unit) {
        // Logic to delete the item
        // e.g., remove it from a list or database
        Log.d(TAG, "handleDeleteAction: remove from database")
        lifecycleScope.launch {
            val response = retrofitInstance.apiService.deleteFeed(feedId)
            Log.d(TAG, "handleDeleteAction: $response")
            Log.d(TAG, "handleDeleteAction body: ${response.body()}")
            Log.d(TAG, "handleDeleteAction isSuccessful: ${response.isSuccessful}")
            if(response.isSuccessful) {
                getFeedViewModel.removeMyFeed(position)
                myFeedAdapter.removeItem(position)
//                shortsViewModel.decrementPostCount()

                shortsViewModel.postCount -= 1
                shortsViewModel.setIsRefreshPostCount(true)
//                myFeedAdapter.notifyItemRemoved(position)
//                myFeedAdapter.notifyDataSetChanged()
                Log.d(TAG, "handleDeleteAction: delete successful")
                showSnackBar("File has been deleted successfully")
                val isAllFeedDataEmpty = getFeedViewModel.getAllFeedData().isEmpty()
                val isFavoriteFeedDataEmpty = getFeedViewModel.getAllFavoriteFeedData().isEmpty()

                if(!isFavoriteFeedDataEmpty) {
                    val favoriteFeed = getFeedViewModel.getAllFavoriteFeedData()
                    val feedToUpdate = favoriteFeed.find { feed-> feed._id == feedId }

                    if(feedToUpdate != null) {

                        Log.d(TAG, "handleDeleteAction: feed to update id ${feedToUpdate._id}")
                        try {
                            Log.d("feedResponse", "handleDeleteAction: 1 ${feedToUpdate._id}")
                            val feedPos = getFeedViewModel.getPositionById(feedId)
                            Log.d("feedResponse", "handleDeleteAction: 2 ${feedToUpdate._id}")
                            getFeedViewModel.removeFavoriteFeed(feedPos)
                            Log.d("feedResponse", "handleDeleteAction: 3 ${feedToUpdate._id}")
//                            val feedResponse = retrofitInstance.apiService.deleteFavoriteFeed(feedToUpdate._id)
                            Log.d("feedResponse", "handleDeleteAction: 4 ${feedToUpdate._id}")
//                            Log.d("feedResponse", "handleDeleteAction: $feedResponse")
//                            Log.d("feedResponse", "handleDeleteAction body: ${feedResponse.body()}")
                        }catch (e: Exception) {
                            Log.e(TAG, "handleDeleteAction: error on bookmark delete ${e.message}")
                            e.printStackTrace()
                        }

                    }else {
                        Log.e("feedResponse", "handleDeleteAction: feed to un-favorite not available")
                    }
                }
//
                if(!isAllFeedDataEmpty) {
                    val allFeedData = getFeedViewModel.getAllFeedData()
                    val feedToUpdate = allFeedData.find { feed -> feed._id == feedId }
                    if (feedToUpdate != null) {
                        Log.d(TAG, "handleDeleteAction: feed data found for all fragment")
                        val pos = getFeedViewModel.getAllFeedDataPositionById(feedToUpdate._id)
                        try{
                            getFeedViewModel.removeAllFeedFragment(pos)
                        }catch (e: Exception) {
                            e.printStackTrace()
                        }
//                        getFeedViewModel.setRefreshMyData(pos, true)
                    }else {
                        Log.d(TAG, "handleDeleteAction: feed data not found for all fragment")
                    }
                }else {

                    Log.i(TAG, "handleDeleteAction: all feed data is empty")
                }

            }else {
                callback(false, "Failed to delete file")
                showSnackBar("Please try again!!!")
            }

        }
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(requireActivity().findViewById(android.R.id.content), message, 1000)
            .setBackgroundTint((ContextCompat.getColor(requireContext(),R.color.green_dark))) // Custom background color
            .setAction("OK") {
                // Handle undo action if needed
            }
            .show()
    }

}