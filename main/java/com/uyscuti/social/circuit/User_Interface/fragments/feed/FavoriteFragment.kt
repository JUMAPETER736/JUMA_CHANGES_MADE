package com.uyscuti.social.circuit.User_Interface.fragments.feed


import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.AnimationDrawable
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.uyscuti.social.circuit.adapter.feed.FeedAdapter
import com.uyscuti.social.circuit.adapter.feed.OnFeedClickListener
import com.uyscuti.social.circuit.adapter.feed.ShareFeedPostAdapter
import com.uyscuti.social.circuit.eventbus.FeedFavoriteClick
import com.uyscuti.social.circuit.eventbus.FeedFavoriteFollowUpdate
import com.uyscuti.social.circuit.eventbus.FeedLikeClick
import com.uyscuti.social.circuit.eventbus.FromFavoriteFragmentFeedFavoriteClick
import com.uyscuti.social.circuit.eventbus.FromFavoriteFragmentFeedLikeClick
import com.uyscuti.social.circuit.eventbus.HideFeedFloatingActionButton
import com.uyscuti.social.circuit.eventbus.ShowFeedFloatingActionButton
import com.uyscuti.social.circuit.interfaces.feedinterfaces.FeedTextViewFragmentInterface
import com.uyscuti.social.circuit.model.ContentType
import com.uyscuti.social.circuit.model.FeedAdapterNotifyDatasetChanged
import com.uyscuti.social.circuit.model.FeedCommentClicked
import com.uyscuti.social.circuit.model.HideAppBar
import com.uyscuti.social.circuit.model.HideBottomNav
import com.uyscuti.social.circuit.model.ShowAppBar
import com.uyscuti.social.circuit.model.ShowBottomNav
import com.uyscuti.social.circuit.User_Interface.feedactivities.FeedVideoViewFragment
import com.uyscuti.social.circuit.User_Interface.feedactivities.ReportNotificationActivity2
import com.uyscut.flashdesign.ui.fragments.feed.feedRepostViewFragments.FeedRepostAudioViewFragment
import com.uyscuti.social.circuit.User_Interface.fragments.feed.feedRepostViewFragments.FeedRepostDocFragment
import com.uyscuti.social.circuit.User_Interface.fragments.feed.feedRepostViewFragments.FeedRepostImageFragment
import com.uyscuti.social.circuit.User_Interface.fragments.feed.feedRepostViewFragments.FeedRepostTextFragment
import com.uyscuti.social.circuit.User_Interface.fragments.feed.feedRepostViewFragments.FeedRepostVideoViewFragment
import com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.FeedAudioViewFragment
import com.uyscuti.social.circuit.User_Interface.fragments.feed.feedviewfragments.FeedMixedFilesViewFragment
import com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.FeedMultipleImageViewFragment
import com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.FeedTextViewFragment
import com.uyscuti.social.circuit.User_Interface.fragments.feed.feedviewfragments.NewRepostedPostFragment
import com.uyscut.flashdesign.ui.fragments.feed.feedviewfragments.Fragment_Original_Post_With_Repost_Inside
import com.uyscuti.social.circuit.utils.removeDuplicateFollowers
import com.uyscuti.social.circuit.viewmodels.FeedShortsViewModel
import com.uyscuti.social.circuit.viewmodels.FollowUnfollowViewModel
import com.uyscuti.social.circuit.viewmodels.GetShortsByUsernameViewModel
import com.uyscuti.social.circuit.viewmodels.feed.FeedUploadViewModel
import com.uyscuti.social.circuit.viewmodels.feed.GetFeedViewModel
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.adapter.feed.FeedPaginatedAdapter
import com.uyscuti.social.circuit.model.feed.multiple_files.MixedFeedUploadDataClass
import com.uyscuti.social.circuit.User_Interface.fragments.feed.feedRepostViewFragmentsimport.FeedRepostMultipleImageFragment
import com.uyscuti.social.core.common.data.room.entity.FollowUnFollowEntity
import com.uyscuti.social.core.common.data.room.entity.ShortsEntityFollowList
import com.uyscuti.social.network.api.response.posts.Post
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject


// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


private const val TAG = "FavoriteFragment"
private const val REQUEST_REPOST_FEED_ACTIVITY = 1020

@AndroidEntryPoint
 class FavoriteFragment : Fragment(), OnFeedClickListener, FeedTextViewFragmentInterface {

    private var param1: String? = null
    private var param2: String? = null
    private lateinit var myFeedAdapter: FeedAdapter
    private lateinit var frameLayout: FrameLayout
    private val requestCode = 2024
    private val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 12
    private var wifiAnimation: AnimationDrawable? = null
    private val shortsViewModel: GetShortsByUsernameViewModel by activityViewModels()
    private val getFeedViewModel: GetFeedViewModel by activityViewModels()
    private val feedUploadViewModel: FeedUploadViewModel by activityViewModels()
    private val followUnFollowViewModel: FollowUnfollowViewModel by viewModels()
    private var feedVideoViewFragment: FeedVideoViewFragment? = null
    private var feedTextViewFragment: FeedTextViewFragment?= null
    private var feedAudioViewFragment: FeedAudioViewFragment? = null
    private var feedMultipleImageViewFragment: FeedMultipleImageViewFragment? = null
    private var feedMixedFilesViewFragment: FeedMixedFilesViewFragment? = null
    private lateinit var allFeedAdapterRecyclerView: RecyclerView
    private lateinit var favoriteFeedAdapter: FeedAdapter
    private lateinit var progressBar: ProgressBar
    private val feesShortsSharedViewModel: FeedShortsViewModel by activityViewModels()
    private var fragmentOriginalPostWithRepostInside: Fragment_Original_Post_With_Repost_Inside? = null
    private var feedRepostDocFragment : FeedRepostDocFragment? = null
    private var feedRepostTextFragment : FeedRepostTextFragment? = null
    private var feedRepostVideoViewFragment : FeedRepostVideoViewFragment? = null
    private var feedRepostAudioViewFragment : FeedRepostAudioViewFragment? = null
    private var feedRepostImageFragment : FeedRepostImageFragment? = null
    private var feedRepostMultipleImageFragment: FeedRepostMultipleImageFragment? = null

    private lateinit var feedListView: RecyclerView

    @Inject
    lateinit var retrofitInstance: RetrofitInstance
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        EventBus.getDefault().register(this)
    }
    @SuppressLint("CutPasteId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_favorite, container, false)
        allFeedAdapterRecyclerView = view.findViewById(R.id.rv)
        feedListView = view.findViewById(R.id.rv)

        frameLayout = view.findViewById(R.id.feed_text_view)
//        allFeedAdapterRecyclerView.adapter = favoriteFeedAdapter  // or allFeedAdapter depending on your needs
        favoriteFeedAdapter = FeedAdapter(
            requireActivity(),
            this
        )
        var isScrollingDown = false
        val scrollThreshold = 5 // M
        Log.d("RecyclerViewTwo", "Adapter set: $favoriteFeedAdapter")
        feedListView.adapter = favoriteFeedAdapter

        feedListView.layoutManager = LinearLayoutManager(requireContext())
        allFeedAdapterRecyclerView.itemAnimator = null
        allFeedAdapterRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        favoriteFeedAdapter.setOnPaginationListener(object :
            FeedPaginatedAdapter.OnPaginationListener {
            override fun onCurrentPage(page: Int) {
//                Toast.makeText(requireContext(), "Page $page loaded!", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "currentPage: page number $page")

            }
            override fun onNextPage(page: Int) {
                lifecycleScope.launch(Dispatchers.Main) {
//                    loadMoreShorts(page)
                    Log.d(TAG, "onNextPage: page number $page")
                    getAllFeed(page)
                }
            }

            override fun onFinish() {
                Log.d(TAG, "finished: page number")
            }
        })


        feedListView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            private var totalDy = 0 // Track accumulated scroll distance

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                // You can handle scroll state changes here if needed
            }
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                getFeedViewModel.allFeedDataLastViewPosition = firstVisibleItemPosition + 1
                getFeedViewModel.allFeedDataLastViewPosition = lastVisibleItemPosition + 1
                // Show/hide FAB based on scroll direction
                totalDy += dy
                if (totalDy > scrollThreshold && !isScrollingDown) {
                    // Scrolling down → Hide FAB
                    isScrollingDown = true
                    EventBus.getDefault().post(HideFeedFloatingActionButton())
                    totalDy = 0 // Reset after action
                } else if (totalDy < -scrollThreshold && isScrollingDown) {
                    // Scrolling up → Show FAB
                    isScrollingDown = false
                    EventBus.getDefault().post(ShowFeedFloatingActionButton(false))
                    totalDy = 0 // Reset after action
                }
            }
        })
        favoriteFeedAdapter.recyclerView = allFeedAdapterRecyclerView


        lifecycleScope.launch(Dispatchers.Main)
        {
            Log.d(
                TAG,
                "onCreateView: getAllFavoriteFeedData size: ${getFeedViewModel.getAllFavoriteFeedData().size}"
            )
            if (getFeedViewModel.getAllFavoriteFeedData().isEmpty()) {
                Log.d(TAG, "onCreateView: get all feed data is empty")
                getAllFeed(favoriteFeedAdapter.startPage)
            } else {
                Log.d(TAG, "onCreateView: get all feed data is not empty")
            }

            Log.d(
                TAG,
                "onCreateView: isFavoritesFeedDataAvailable ${getFeedViewModel.isFavoritesFeedDataAvailable}"
            )

            getFeedViewModel.isFavoritesFeedDataAvailable.observe(viewLifecycleOwner) { isDataAvailable ->
                // Handle the updated value of isResuming here
                if (isDataAvailable) {
                    // Do something when isResuming is true
                    Log.d(
                        TAG,
                        "onCreateView: data is available and size is ${getFeedViewModel.getAllFavoriteFeedData().size}"
                    )
//                    allFeedAdapter.item
                    favoriteFeedAdapter.submitItems(getFeedViewModel.getAllFavoriteFeedData())
                    getFeedViewModel.setIsDataAvailable(false)

                } else {
                    // Do something when isResuming is false
                    Log.d(TAG, "onCreateView: data not added")

                }
            }
        }

        return view
    }

    private fun getAllFeed(page: Int) {
        Log.d(
            TAG,
            "getAllFeed: page number $page feed data empty?: ${
                getFeedViewModel.getAllFavoriteFeedData().isEmpty()
            }"
        )
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = retrofitInstance.apiService.getFavoriteFeed(
                    page.toString()
                )
                val responseBody = response.body()
                Log.d(TAG, "feed: response $response")
                Log.d(TAG, "feed: response body message ${responseBody!!.message}")
                Log.d(TAG, "getAllFeed: size ${responseBody.data.totalBookmarkedPosts}")
                val data = responseBody.data
                Log.d(TAG, "getAllFeed: ${data.bookmarkedPosts.toMutableList()}")

                // Cast to the correct type
                val bookmarkedPosts = data.bookmarkedPosts.toMutableList() as MutableList<com.uyscuti.social.network.api.response.posts.Post>
                getFeedViewModel.addAllFavoriteFeedData(bookmarkedPosts)

                withContext(Dispatchers.Main) {
                    // Add any UI updates here if needed
                }
                Log.d(TAG, "text comment data response: $data")
            } catch (e: Exception) {
                Log.e(TAG, "comment: $e")
                Log.e(TAG, "comment: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FavoriteFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
        }


    override fun likeUnLikeFeed(position: Int, data: Post) {
        try {
            Log.d("likeUnLikeFeed", "likeUnLikeFeed: received like toggle for post ${data._id}")
            Log.d("likeUnLikeFeed", "likeUnLikeFeed: current state - isLiked: ${data.isLiked}, likes: ${data.likes}")

            // Create updated comment with the current data (ViewHolder already updated the counts)
            val updatedComment = data.copy(
                content = data.content ?: "",
                repostedByUserId = data.repostedByUserId ?: "",
//                url = data.url ?: "",
//                profilePicUrl = data.profilePicUrl ?: "",
//                username = data.username ?: "",
//                fullName = data.fullName ?: "",
//                description = data.description ?: "",
//                location = data.location ?: ""
            )

            // Make API call to sync with server
            lifecycleScope.launch {
                try {
                    feedUploadViewModel.likeUnLikeFeed(data._id)
                } catch (e: Exception) {
                    Log.e("likeUnLikeFeed", "API call failed: ${e.message}", e)
                }
            }

            Log.d("likeUnLikeFeed", "likeUnLikeFeed: final likes count is ${updatedComment.likes}")

            // Update all related data sources with the already-updated data
            updateFavoriteFeedData(updatedComment)
            updateMyFeedData(updatedComment)

            // Notify other fragments about the change
            EventBus.getDefault().post(FromFavoriteFragmentFeedLikeClick(position, updatedComment))

            // Update adapter if this is in FavoriteFragment
            if (this::favoriteFeedAdapter.isInitialized) {
                val favoritePosition = favoriteFeedAdapter.getPositionById(data._id)
                if (favoritePosition != -1) {
                    favoriteFeedAdapter.updateItem(favoritePosition, updatedComment)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "likeUnLikeFeed error: ${e.message}", e)
            e.printStackTrace()
        }
    }

    override fun feedFavoriteClick(position: Int, data: Post) {
        try {
            Log.d(TAG, "feedFavoriteClick: received bookmark toggle for post ${data._id}")
            Log.d(TAG, "feedFavoriteClick: current state - isBookmarked: ${data.isBookmarked}, bookmarkCount: ${data.bookmarkCount}")

            // Create safe copy of the data
            val safeData = data.copy(
                content = data.content ?: "",
                repostedByUserId = data.repostedByUserId ?: "",
//                url = data.url ?: "",
//                profilePicUrl = data.profilePicUrl ?: "",
//                username = data.username ?: "",
//                fullName = data.fullName ?: "",
//                description = data.description ?: "",
//                location = data.location ?: ""
            )

            // Update MyFeed data if it exists
            updateMyFeedBookmarkData(safeData)

            // Handle favorites list updates
            if (safeData.isBookmarked == true) {
                // Adding to favorites
                handleAddToFavorites(safeData)
            } else {
                // Removing from favorites
                handleRemoveFromFavorites(position, safeData)
            }

            // Notify other components
            EventBus.getDefault().post(FromFavoriteFragmentFeedFavoriteClick(position, safeData))

            // Make API call
            lifecycleScope.launch {
                try {
                    feedUploadViewModel.favoriteFeed(safeData._id)
                } catch (e: Exception) {
                    Log.e(TAG, "feedFavoriteClick API error: ${e.message}", e)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "feedFavoriteClick error: ${e.message}", e)
            e.printStackTrace()
        }
    }

    private fun updateFavoriteFeedData(updatedPost: com.uyscuti.social.network.api.response.posts.Post) {
        try {
            val updatedItems = getFeedViewModel.getAllFavoriteFeedData()
            val itemToUpdate = updatedItems.find { it._id == updatedPost._id }

            itemToUpdate?.let { item ->
                item.isLiked = updatedPost.isLiked
                item.likes = updatedPost.likes
                Log.d(TAG, "updateFavoriteFeedData: updated favorite item ${item._id} - likes: ${item.likes}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateFavoriteFeedData error: ${e.message}", e)
        }
    }

    private fun updateMyFeedData(updatedPost: com.uyscuti.social.network.api.response.posts.Post) {
        try {
            if (getFeedViewModel.getMyFeedData().isNotEmpty()) {
                val myFeedData = getFeedViewModel.getMyFeedData()
                val feedToUpdate = myFeedData.find { feed -> feed._id == updatedPost._id }

                feedToUpdate?.let { feed ->
                    feed.isLiked = updatedPost.isLiked
                    feed.likes = updatedPost.likes

                    val myFeedDataPosition = getFeedViewModel.getMyFeedPositionById(feed._id)
                    if (myFeedDataPosition != -1) {
                        getFeedViewModel.updateMyFeedData(myFeedDataPosition, feed)
                        Log.d(TAG, "updateMyFeedData: updated my feed item at position $myFeedDataPosition")
                    }
                } ?: run {
                    Log.d(TAG, "updateMyFeedData: feed not found in my feed data")
                }
            } else {
                Log.i(TAG, "updateMyFeedData: my feed data is empty")
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateMyFeedData error: ${e.message}", e)
        }
    }

    private fun updateMyFeedBookmarkData(updatedPost: com.uyscuti.social.network.api.response.posts.Post) {
        try {
            if (getFeedViewModel.getMyFeedData().isNotEmpty()) {
                val myFeedData = getFeedViewModel.getMyFeedData()
                val feedToUpdate = myFeedData.find { feed -> feed._id == updatedPost._id }

                feedToUpdate?.let { feed ->
                    feed.isBookmarked = updatedPost.isBookmarked
                    feed.bookmarkCount = updatedPost.bookmarkCount

                    val myFeedDataPosition = getFeedViewModel.getMyFeedPositionById(feed._id)
                    if (myFeedDataPosition != -1) {
                        getFeedViewModel.updateMyFeedData(myFeedDataPosition, feed)
                        Log.d(TAG, "updateMyFeedBookmarkData: updated bookmark in my feed at position $myFeedDataPosition")
                    }
                } ?: run {
                    Log.d(TAG, "updateMyFeedBookmarkData: feed not found in my feed data")
                }
            } else {
                Log.i(TAG, "updateMyFeedBookmarkData: my feed data is empty")
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateMyFeedBookmarkData error: ${e.message}", e)
        }
    }

    private fun handleAddToFavorites(data: com.uyscuti.social.network.api.response.posts.Post) {
        try {
            // Check if the feed already exists in the viewModel
            val existingFeedPosition = getFeedViewModel.getPositionById(data._id)
            Log.d(TAG, "handleAddToFavorites: existing feed position $existingFeedPosition")

            if (existingFeedPosition == -1) {
                // Add to favorites list
                getFeedViewModel.addFavoriteFeed(0, data)

                if (this::favoriteFeedAdapter.isInitialized) {
                    favoriteFeedAdapter.addFollowList(getFeedViewModel.getFollowList())
                    favoriteFeedAdapter.submitItem(data, 0)
                    Log.d(TAG, "handleAddToFavorites: added new favorite item")
                }
            } else {
                Log.d(TAG, "handleAddToFavorites: feed already exists in favorites")
            }
        } catch (e: Exception) {
            Log.e(TAG, "handleAddToFavorites error: ${e.message}", e)
        }
    }

    private fun handleRemoveFromFavorites(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {
        try {
            // Find position in ViewModel
            val existingFeedPosition = getFeedViewModel.getPositionById(data._id)
            Log.d(TAG, "handleRemoveFromFavorites: existingFeedPosition $existingFeedPosition")

            if (existingFeedPosition != -1) {
                getFeedViewModel.removeFavoriteFeed(existingFeedPosition)
                Log.d(TAG, "handleRemoveFromFavorites: removed from viewModel at position $existingFeedPosition")
            } else {
                Log.e(TAG, "handleRemoveFromFavorites: feed not found in viewModel")
            }

            // Find position in adapter and remove
            if (this::favoriteFeedAdapter.isInitialized) {
                val adapterPosition = favoriteFeedAdapter.getPositionById(data._id)
                Log.d(TAG, "handleRemoveFromFavorites: adapter position $adapterPosition")

                if (adapterPosition != -1 && adapterPosition < favoriteFeedAdapter.itemCount) {
                    favoriteFeedAdapter.removeItem(adapterPosition)
                    Log.d(TAG, "handleRemoveFromFavorites: removed from adapter at position $adapterPosition")
                } else {
                    Log.e(TAG, "handleRemoveFromFavorites: invalid adapter position $adapterPosition")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "handleRemoveFromFavorites error: ${e.message}", e)
        }
    }

    override fun feedCommentClicked(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {
        try {
            EventBus.getDefault().post(FeedCommentClicked(position, data))
        } catch (e: Exception) {
            Log.e(TAG, "feedCommentClicked error: ${e.message}", e)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun feedAdapterNotifyDatasetChanged(event: FeedAdapterNotifyDatasetChanged) {
        try {
            Log.d(TAG, "feedAdapterNotifyDatasetChanged: refreshing adapter")
            if (this::favoriteFeedAdapter.isInitialized) {
                favoriteFeedAdapter.notifyDataSetChanged()
            }
        } catch (e: Exception) {
            Log.e(TAG, "feedAdapterNotifyDatasetChanged error: ${e.message}", e)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun favoriteFeedClick(event: FeedFavoriteClick) {
        try {
            Log.d(TAG, "favoriteFeedClick: processing EventBus event for ${event.data._id}")
            Log.d(TAG, "favoriteFeedClick: isBookmarked=${event.data.isBookmarked}")

            if (event.data.isBookmarked == true) {
                // Adding to favorites
                val existingFeed = getFeedViewModel.getPositionById(event.data._id)
                Log.d(TAG, "favoriteFeedClick: existing feed position $existingFeed")

                if (existingFeed == -1) {
                    getFeedViewModel.addFavoriteFeed(0, event.data)
                    if (this::favoriteFeedAdapter.isInitialized) {
                        favoriteFeedAdapter.addFollowList(getFeedViewModel.getFollowList())
                        favoriteFeedAdapter.submitItem(event.data, 0)
                        Log.d(TAG, "favoriteFeedClick: added to favorites")
                    }
                } else {
                    Log.d(TAG, "favoriteFeedClick: feed already exists in favorites")
                }
            } else {
                // Removing from favorites
                val existingFeedPosition = getFeedViewModel.getPositionById(event.data._id)
                Log.d(TAG, "favoriteFeedClick: removing from position $existingFeedPosition")

                if (existingFeedPosition != -1) {
                    getFeedViewModel.removeFavoriteFeed(existingFeedPosition)
                }

                if (this::favoriteFeedAdapter.isInitialized) {
                    val adapterPosition = favoriteFeedAdapter.getPositionById(event.data._id)
                    if (adapterPosition != -1 && adapterPosition < favoriteFeedAdapter.itemCount) {
                        favoriteFeedAdapter.removeItem(adapterPosition)
                        Log.d(TAG, "favoriteFeedClick: removed from adapter")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "favoriteFeedClick error: ${e.message}", e)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun likeFeedClick(event: FeedLikeClick) {
        try {
            Log.d(TAG, "likeFeedClick: processing EventBus event for ${event.data._id}")
            Log.d(TAG, "likeFeedClick: likes=${event.data.likes}, isLiked=${event.data.isLiked}")

            if (this::favoriteFeedAdapter.isInitialized) {
                val feedPosition = favoriteFeedAdapter.getPositionById(event.data._id)
                if (feedPosition != -1) {
                    favoriteFeedAdapter.updateItem(feedPosition, event.data)
                    getFeedViewModel.updateForFavoriteFragment(feedPosition, event.data)
                    Log.d(TAG, "likeFeedClick: updated item at position $feedPosition")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "likeFeedClick error: ${e.message}", e)
        }
    }








//    override fun likeUnLikeFeed(position: Int, data: com.uyscuti.social.network.api.response.allFeedRepostsPost.Post) {
//        try {
//            val updatedComment = if (data.isLiked) {
//                data.copy(
//                    content = data.content?:"",
//                    likes = data.likes + 1,
//                    repostedByUserId = data.repostedByUserId?:"",
//                    isLiked = true
//                )
//            } else {
//                data.copy(
//                    content = data.content?:"",
//                    likes = data.likes - 1,
//                    repostedByUserId = data.repostedByUserId?:"",
//                    isLiked = false
//                )
//            }
//            lifecycleScope.launch {
//                feedUploadViewModel.likeUnLikeFeed(data._id)
//            }
//            Log.d("likeUnLikeFeed", "likeUnLikeFeed: likes count is ${data.likes}")
//            val updatedItems = getFeedViewModel.getAllFavoriteFeedData()
//            for (updatedItem in updatedItems) {
//                if (updatedItem._id == data._id) {
//                    if (data.isLiked) {
//                        updatedItem.likes += 1
//                    } else {
//                        updatedItem.likes -= 1
//                    }
//                }
//            }
//            EventBus.getDefault().post(FromFavoriteFragmentFeedLikeClick(position, updatedComment))
//            favoriteFeedAdapter.updateItem(position, updatedComment)
//            val isMyFeedEmpty = getFeedViewModel.getMyFeedData().isEmpty()
//            if (!isMyFeedEmpty) {
//                val myFeedData = getFeedViewModel.getMyFeedData()
//                val feedToUpdate = myFeedData.find { feed -> feed._id == data._id }
//                if (feedToUpdate != null) {
//                    feedToUpdate.isLiked = data.isLiked
//                    feedToUpdate.likes = data.likes
//                    val myFeedDataPosition =
//                        getFeedViewModel.getMyFeedPositionById(feedToUpdate._id)
//                    getFeedViewModel.updateMyFeedData(myFeedDataPosition, feedToUpdate)
//                } else {
//                    Log.d(TAG, "likeUnLikeFeed: feed to update is not available in the list")
//                }
//            } else {
//                Log.i(TAG, "likeUnLikeFeed: my feed data is empty")
//            }
//            Log.d(TAG, "likeUnLikeFeed: ")
//        } catch (e: Exception) {
//            Log.e(TAG, "likeUnLikeFeed: ${e.message}")
//            e.printStackTrace()
//        }
//    }
//
//    override fun feedCommentClicked(position: Int, data: com.uyscuti.social.network.api.response.allFeedRepostsPost.Post) {
//        EventBus.getDefault().post(FeedCommentClicked(position, data))
//    }
//
//    override fun feedFavoriteClick(position: Int, data: com.uyscuti.social.network.api.response.allFeedRepostsPost.Post) {
//        EventBus.getDefault().post(FromFavoriteFragmentFeedFavoriteClick(position, data))
//        val isMyFeedEmpty = getFeedViewModel.getMyFeedData().isEmpty()
//        if (!isMyFeedEmpty) {
//            val myFeedData = getFeedViewModel.getMyFeedData()
//            val feedToUpdate = myFeedData.find { feed -> feed._id == data._id }
//            if (feedToUpdate != null) {
//                feedToUpdate.isBookmarked = data.isBookmarked
//                val myFeedDataPosition = getFeedViewModel.getMyFeedPositionById(feedToUpdate._id)
//                getFeedViewModel.updateMyFeedData(myFeedDataPosition, feedToUpdate)
//            } else {
//                Log.d(TAG, "feedFavoriteClick: feed to update is not available in the list")
//            }
//        } else {
//            Log.i(TAG, "feedFavoriteClick: my feed data is empty")
//        }
//        if (!data.isBookmarked) {
//            favoriteFeedAdapter.removeItem(position)
//            getFeedViewModel.removeFavoriteFeed(position)
//        }
//        lifecycleScope.launch {
//            feedUploadViewModel.favoriteFeed(data._id)
//        }
//    }
//
//    @SuppressLint("NotifyDataSetChanged")
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun feedAdapterNotifyDatasetChanged(event: FeedAdapterNotifyDatasetChanged) {
//        Log.d(
//            TAG,
//            "FeedAdapterNotifyDatasetChanged: in feed adapter notify adapter: seh data set changed"
//        )
//        favoriteFeedAdapter.notifyDataSetChanged()
//
//    }
//
//    @SuppressLint("NotifyDataSetChanged")
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun favoriteFeedClick(event: FeedFavoriteClick) {
//
//        Log.d(TAG, "favoriteFeedClick: ${getFeedViewModel.getFollowList()}")
//        if (event.data.isBookmarked) {
//            // Check if the feed already exists in the viewModel
//            val existingFeed = getFeedViewModel.getPositionById(event.data._id)
//            Log.d(TAG, "favoriteFeedClick: existing feed $existingFeed")
//            if (existingFeed == -1) {
//                getFeedViewModel.addFavoriteFeed(0, event.data)
//                favoriteFeedAdapter.addFollowList(getFeedViewModel.getFollowList())
//                favoriteFeedAdapter.notifyDataSetChanged()
//            } else {
//                Log.e(TAG, "favoriteFeedClick: feed already exists")
//            }
//
//            favoriteFeedAdapter.submitItem(event.data, 0)
//
//        } else {
//            val existingFeedPosition = getFeedViewModel.getPositionById(event.data._id)
//            Log.d(TAG, "favoriteFeedClick: existingFeedPosition $existingFeedPosition")
//            if (existingFeedPosition != -1) {
//
//                getFeedViewModel.removeFavoriteFeed(existingFeedPosition)
//            } else {
//                Log.e(
//                    TAG,
//                    "favoriteFeedClick: you can't delete if there is no existing feed position"
//                )
//            }
//            val feedPosition = favoriteFeedAdapter.getPositionById(event.data._id)
//
//            Log.d(
//                TAG,
//                "favoriteFeedClick: item to remove on position $feedPosition"
//            )
//            favoriteFeedAdapter.removeItem(feedPosition)
//        }
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun likeFeedClick(event: FeedLikeClick) {
//        Log.d(
//            TAG,
//            "likeFeedClick: event bus position ${event.position} is bookmarked ${event.data.likes}"
//        )
//        val feedPosition = favoriteFeedAdapter.getPositionById(event.data._id)
//        favoriteFeedAdapter.updateItem(feedPosition, event.data)
//        getFeedViewModel.updateForFavoriteFragment(feedPosition, event.data)
//    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun feedFavoriteFollowUpdate(event: FeedFavoriteFollowUpdate) {

    }


    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: called")
        EventBus.getDefault().unregister(this)
    }
    fun updateLayoutVisibility(contentType: ContentType, downloadFeedLayout: View, followUnfollowLayout: View, muteUser: View, hideFavorite: View) {
        when (contentType) {
            ContentType.TEXT -> {
                downloadFeedLayout.visibility = View.GONE
                followUnfollowLayout.visibility = View.GONE
                muteUser.visibility = View.VISIBLE // Or handle it accordingly
                hideFavorite.visibility = View.VISIBLE
            }
            ContentType.VIDEO -> {
                downloadFeedLayout.visibility = View.VISIBLE
                followUnfollowLayout.visibility = View.VISIBLE
                muteUser.visibility = View.GONE
                hideFavorite.visibility = View.GONE
            }
            ContentType.IMAGE -> {
                downloadFeedLayout.visibility = View.VISIBLE
                followUnfollowLayout.visibility = View.VISIBLE
                muteUser.visibility = View.VISIBLE
                hideFavorite.visibility = View.VISIBLE
            }
            ContentType.AUDIO -> {
                downloadFeedLayout.visibility = View.VISIBLE
                followUnfollowLayout.visibility = View.VISIBLE
                muteUser.visibility = View.VISIBLE
                hideFavorite.visibility = View.VISIBLE
            }
        }
    }


    fun onDownloadClick(url: String, fileLocation: String) {
        Log.d(
            "Download",
            "OnDownload $url  \nto path : $fileLocation"
        )

        val permissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(requireActivity(), permissions, requestCode)
        } else {
            // You have permission, proceed with your file operations

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                // Check if the permission is not granted
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // Request the permission
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        WRITE_EXTERNAL_STORAGE_REQUEST_CODE
                    )
                } else {

                    download(url, fileLocation)
                }


            } else {
                download(url, fileLocation)
            }
        }




    }


    @OptIn(DelicateCoroutinesApi::class)
    private fun download(
        mUrl: String,
        fileLocation: String,
    ) {
        //STORAGE_FOLDER += fileLocation
        Log.d("Download", "directory path - $fileLocation")

        if (mUrl.startsWith("/storage/") || mUrl.startsWith("/storage/")) {

            Log.d("Download", "Cannot download a local file")
            return
        }

        //STORAGE_FOLDER += fileLocation
        val STORAGE_FOLDER = "/Download/Flash/$fileLocation"

        val fileName = generateUniqueFileName(mUrl)

        val storageDirectory =
            Environment.getExternalStorageDirectory().toString() + STORAGE_FOLDER + "/$fileName"

        Log.d("Download", "directory path - $storageDirectory")
        val file = File(Environment.getExternalStorageDirectory().toString() + STORAGE_FOLDER)
        if (!file.exists()) {
            file.mkdirs()
        }

        GlobalScope.launch(Dispatchers.IO) {
            val url = URL(mUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept-Encoding", "identity")
            connection.connect()

            try {
                if (connection.responseCode in 200..299) {
                    val fileSize = connection.contentLength
                    val inputStream = connection.inputStream
                    val outputStream = FileOutputStream(storageDirectory)

                    var bytesCopied: Long = 0
                    val buffer = ByteArray(1024)
                    var bytes = inputStream.read(buffer)
                    while (bytes >= 0) {
                        bytesCopied += bytes
                        val downloadProgress =
                            (bytesCopied.toFloat() / fileSize.toFloat() * 100).toInt()
                        requireActivity().runOnUiThread {
//
                        }
                        outputStream.write(buffer, 0, bytes)
                        bytes = inputStream.read(buffer)
                    }

                    requireActivity().runOnUiThread {

                        Log.d("Download", "File Downloaded : $storageDirectory")

                        val downloadedFile = File(storageDirectory)

                    }
                    outputStream.close()
                    inputStream.close()
                } else {
                    requireActivity().runOnUiThread {
                        Toast.makeText(
                            requireActivity(),
                            "Not successful",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("DownloadFailed", e.message.toString())

                e.printStackTrace()
                requireActivity().runOnUiThread {

                }
            }
        }
    }
    private fun generateUniqueFileName(originalUrl: String): String {
        val timestamp =
            SimpleDateFormat("yyyy_MM_dd_HHmmss", Locale.getDefault()).format(Date())
        val originalFileName = originalUrl.split("/").last()
        val fileExtension = MimeTypeMap.getFileExtensionFromUrl(originalFileName)
        val randomString = UUID.randomUUID().toString().substring(0, 8)
        return "$timestamp-$randomString.$fileExtension"
    }

    @SuppressLint("InflateParams", "MissingInflatedId")
    override fun moreOptionsClick(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {
        Log.d(TAG, "moreOptionsClick: More options clicked")
        val view: View = layoutInflater.inflate(R.layout.feed_more_options_layout, null)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(view)
        dialog.show()
        val downloadFiles: View = view.findViewById(R.id.downloadFeedLayout)
        val followUnfollowLayout: View = view.findViewById(R.id.followUnfollowLayout)
        val reportUser: View = view.findViewById(R.id.reportOptionLayout)
        val hidePostLayout: View = view.findViewById(R.id.hidePostLayout)
        val copyLink: View = view.findViewById(R.id.copyLinkLayout)
        val muteOptionLayout: View = view.findViewById(R.id.muteOptionLayout)
        val QuoteFeedLayout: View = view.findViewById(R.id.rePostFeedLayout)

        downloadFiles.setOnClickListener {
            Log.d("DownloadButton", "Data: $data")

            onDownloadClick(data.files[0].url, "FlashShorts")
            dialog.dismiss()
        }

        muteOptionLayout.setOnClickListener {
            Log.d("MuteButton", "Data: $data")
        }
        followUnfollowLayout.visibility = View.GONE
        QuoteFeedLayout.setOnClickListener {
            Log.d("QuoteButton", "Data: $data")
            val fragment = NewRepostedPostFragment(data )
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, fragment) // Ensure fragment_container is correct
            transaction.addToBackStack(null)
            transaction.commit()
            dialog.dismiss()

        }
        copyLink.setOnClickListener {
            val postId = data._id // Adjust this based on your actual Post class property name
            val linkToCopy =
                "https:/circuitSocial.app/post/$postId" // Replace with your actual link
            val clipboard =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = android.content.ClipData.newPlainText("Copied Link", linkToCopy)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(requireContext(), "Link copied to clipboard/$postId", Toast.LENGTH_SHORT)
                .show()
            dialog.dismiss()
        }

        val notInterested: View = view.findViewById(R.id.notInterestedLayout)
        notInterested.setOnClickListener {
            handleNotInterested(data)
            dialog.dismiss()

        }

        hidePostLayout.setOnClickListener {
            Log.d(TAG, "hidePostLayout: hide post clicked")
            hideSinglePost(position, data)
            dialog.dismiss()

        }

        val downloadOption: View = view.findViewById(R.id.downloadFeedLayout)
        if (data.isBookmarked) {
            data.isBookmarked = true
        }
        if (data.contentType == "text") {
            downloadOption.visibility = View.GONE
        }

        reportUser.setOnClickListener {
            Log.d("reportUser", "has been clicked")
            val intent = Intent(requireActivity(), ReportNotificationActivity2::class.java)
            startActivityForResult(intent, REQUEST_REPOST_FEED_ACTIVITY)
            dialog.dismiss()
        }

        downloadOption.setOnClickListener {
            Log.d(TAG, "Download option clicked for post: $data")
            Toast.makeText(requireContext(), "download clicked", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
    }

    private fun handleNotInterested(data: com.uyscuti.social.network.api.response.posts.Post) {

        val sharedPrefs = requireContext().getSharedPreferences("NotInterestedPosts", Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putBoolean(data._id.toString(), true)
            apply()
        }


        Toast.makeText(requireContext(), "We'll show you less content like this", Toast.LENGTH_SHORT).show()
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun hideSinglePost(position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {
        Log.d(TAG, "hideSinglePost: Hiding post at position: $position, PostId: ${data._id}")
        try {
            if (::favoriteFeedAdapter.isInitialized) {


                favoriteFeedAdapter.removeItem(position)
                favoriteFeedAdapter.notifyItemRemoved(position)
//                allFeedAdapter.notifyItemChanged(position)
                // Optional: Add fade-out animation
                val viewHolder = feedListView.findViewHolderForAdapterPosition(position)
                if (viewHolder != null) {
                    viewHolder.itemView.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction {
                            favoriteFeedAdapter.notifyItemRemoved(position)
                        }
                        .start()
                } else {
                    Log.w(TAG, "ViewHolder at position $position is null, notifying removal directly")
                    favoriteFeedAdapter.notifyItemRemoved(position) // Fallback for off-screen items
                }

                // Show Snackbar with Undo button
                Snackbar.make(feedListView, "Post hidden", Snackbar.LENGTH_LONG)
                    .setAction("Undo") {
                        // Restore the post

                        favoriteFeedAdapter.notifyItemInserted(position)
                    }
                    .show()
                return
            }

            val sharedPrefs = requireContext().getSharedPreferences("HiddenPosts", Context.MODE_PRIVATE)
            with(sharedPrefs.edit()) {
                putBoolean(data._id, true)
                apply()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error hiding post: ${e.message}")
            Toast.makeText(requireContext(), "Failed to hide post", Toast.LENGTH_SHORT).show()
        }
    }




    private fun muteUserOption(userId: String) {
        Log.d(TAG, "muteUserOption: $userId")
        val sharedPreferences = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val mutedUsersSet = sharedPreferences.getStringSet("muted_users", mutableSetOf()) ?: mutableSetOf()
        mutedUsersSet.add(userId)
        editor.putStringSet("muted_users", mutedUsersSet)
        editor.apply()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun downloadMediaFile(fileUrl: String, fileName: String, fileType: String) {
        // Check for permissions (write external storage)
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), WRITE_EXTERNAL_STORAGE_REQUEST_CODE)
            return
        }
        // Determine directory based on file type
        val directoryType = when (fileType) {
            "audio" -> Environment.DIRECTORY_MUSIC
            "image" -> Environment.DIRECTORY_PICTURES
            "video" -> Environment.DIRECTORY_MOVIES
            else -> Environment.DIRECTORY_DOWNLOADS
        }
        val file = File(requireContext().getExternalFilesDir(directoryType), fileName)
        // Start download in a coroutine to avoid blocking the main thread
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = URL(fileUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()
                if (connection.responseCode in 200..299) {
                    val inputStream = connection.inputStream
                    val outputStream = FileOutputStream(file)

                    inputStream.use { input ->
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }
                    // Notify the user on download completion
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Download completed: ${file.absolutePath}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Download failed: ${connection.responseMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("Download", "Error downloading file", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @SuppressLint("ServiceCast")



    private fun showDeleteConfirmationDialog(feedId: String, position: Int) {
        val inflater = LayoutInflater.from(requireContext())
        val customTitleView: View = inflater.inflate(R.layout.delete_title_custom_layout, null)
        val builder = AlertDialog.Builder(requireContext())

        builder.setCustomTitle(customTitleView)
        builder.setMessage("Are you sure you want to delete this feed?")

        // Positive Button
        builder.setPositiveButton("Delete") { dialog, which ->
            // Handle delete action

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


                shortsViewModel.postCount -= 1
                shortsViewModel.setIsRefreshPostCount(true)

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

                            Log.d("feedResponse", "handleDeleteAction: 4 ${feedToUpdate._id}")

                        }catch (e: Exception) {
                            Log.e(TAG, "handleDeleteAction: error on bookmark delete ${e.message}")
                            e.printStackTrace()
                        }

                    }else {
                        Log.e("feedResponse", "handleDeleteAction: feed to un-favorite not available")
                    }
                }

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

                    }else {
                        Log.d(TAG, "handleDeleteAction: feed data not found for all fragment")
                    }
                }else {
                    Log.i(TAG, "handleDeleteAction: all feed data is empty")
                }

                if(!isFavoriteFeedDataEmpty) {
                    val favoriteFeedData = getFeedViewModel.getAllFavoriteFeedData()
                    val feedToUpdate = favoriteFeedData.find { feed -> feed._id == feedId }
                    if (feedToUpdate != null) {
                        Log.d(TAG, "handleDeleteAction: feed data found for favorite")
                        getFeedViewModel.setRefreshMyData(position, true)
                    }else {
                        Log.d(TAG, "handleDeleteAction: feed data not found for favorite")
                    }
                }else {
                    Log.i(TAG, "handleDeleteAction: favorite feed data is empty")
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

    fun forShow() {
        Log.d("forShow", "forShow: is called")
    }


    override fun feedFileClicked(
        position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {
        val contentType = data.contentType
        if (contentType.isNullOrEmpty()) {
            Log.e("FavoriteFragment", "Invalid or null contentType for data: $data at position: $position")
            Toast.makeText(requireContext(), "Unsupported content type", Toast.LENGTH_SHORT).show()
            return
        }
            when (data.contentType){
            "text" -> {
                EventBus.getDefault().post(HideBottomNav())
                EventBus.getDefault().post(HideAppBar())
                EventBus.getDefault().post(HideFeedFloatingActionButton())
                frameLayout.visibility = View.VISIBLE
                allFeedAdapterRecyclerView.visibility = View.GONE
                val args = Bundle().apply {
                    putInt("position", position)
                    putSerializable("data", data) // Adjust type if needed
                }
                feedTextViewFragment = FeedTextViewFragment()
                feedTextViewFragment?.setListener(this)
                feedTextViewFragment?.arguments = args

                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.feed_text_view_fragment,
                        feedTextViewFragment!!
                    ) // Use the correct container ID
                    .addToBackStack(null) // Optional, to add to back stack
                    .commit()
            }

            "video"-> {
                EventBus.getDefault().post(HideBottomNav())
                EventBus.getDefault().post(HideAppBar())
                EventBus.getDefault().post(HideFeedFloatingActionButton())
                frameLayout.visibility = View.VISIBLE
                allFeedAdapterRecyclerView.visibility = View.GONE
                val args = Bundle().apply {
                    putInt("position", position)
                    putSerializable("data", data) // Adjust type if needed
                }
                feedVideoViewFragment = FeedVideoViewFragment()
                feedVideoViewFragment?.setListener(this)
                feedVideoViewFragment?.arguments = args
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.feed_text_view,
                        feedVideoViewFragment!!
                    ) // Use the correct container ID
                    .addToBackStack(null) // Optional, to add to back stack
                    .commit()
            }

            "mixed_files"-> {

                EventBus.getDefault().post(HideBottomNav())
                EventBus.getDefault().post(HideAppBar())
                EventBus.getDefault().post(HideFeedFloatingActionButton())
                frameLayout.visibility = View.VISIBLE
                allFeedAdapterRecyclerView.visibility = View.GONE


                feedMixedFilesViewFragment = FeedMixedFilesViewFragment()
                feedMixedFilesViewFragment?.setListener(this)

                val args = Bundle().apply {
                    putInt("position", position)
                    putSerializable("data", data) // Adjust type if needed
                }
                feedMixedFilesViewFragment?.arguments = args
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.feed_text_view,
                        feedMixedFilesViewFragment!!
                    ) // Use the correct container ID
                    .addToBackStack(null) // Optional, to add to back stack
                    .commit()

            }

            "audio" -> {
                EventBus.getDefault().post(HideBottomNav())
                EventBus.getDefault().post(HideAppBar())
                EventBus.getDefault().post(HideFeedFloatingActionButton())
                frameLayout.visibility = View.VISIBLE
                allFeedAdapterRecyclerView.visibility = View.GONE
                val args = Bundle().apply {
                    putInt("position", position)
                    putSerializable("data", data) // Adjust type if needed
                }
                feedAudioViewFragment = FeedAudioViewFragment()
                feedAudioViewFragment?.setListener(this)
                feedAudioViewFragment?.arguments = args
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.feed_text_view,
                        feedAudioViewFragment!!
                    ) // Use the correct container ID
                    .addToBackStack(null) // Optional, to add to back stack
                    .commit()
            }

            "multiple_images" -> {
                EventBus.getDefault().post(HideBottomNav())
                EventBus.getDefault().post(HideAppBar())
                EventBus.getDefault().post(HideFeedFloatingActionButton())
                frameLayout.visibility = View.VISIBLE
                allFeedAdapterRecyclerView.visibility = View.GONE
                val args = Bundle().apply {
                    putInt("position", position)
                    putSerializable("data", data) // Adjust type if needed
                }
                feedMultipleImageViewFragment = FeedMultipleImageViewFragment()
                feedMultipleImageViewFragment?.setListener(this)
                feedMultipleImageViewFragment?.arguments = args
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.feed_text_view,
                        feedMultipleImageViewFragment!!
                    ) // Use the correct container ID
                    .addToBackStack(null) // Optional, to add to back stack
                    .commit()
            }

        }

    }



    override fun feedRepostFileClicked(
        position: Int,data: com.uyscuti.social.network.api.response.posts.OriginalPost
    ) {
        when (data.contentType){
            "mixed_files" -> {

                EventBus.getDefault().post(HideBottomNav())
                EventBus.getDefault().post(HideAppBar())
                EventBus.getDefault().post(HideFeedFloatingActionButton())
                frameLayout.visibility = View.VISIBLE
                feedListView.visibility = View.GONE
                feedMixedFilesViewFragment?.setListener(this)
                val args = Bundle().apply {
                    putInt("position", position)
                    putSerializable("data", data) // Adjust type if needed
                }
                fragmentOriginalPostWithRepostInside?.arguments = args
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.feed_text_view_fragment,
                        fragmentOriginalPostWithRepostInside!!
                    ) // Use the correct container ID
                    .addToBackStack(null) // Optional, to add to back stack
                    .commit()
            }
            "multiple_images" -> {

                EventBus.getDefault().post(HideBottomNav())
                EventBus.getDefault().post(HideAppBar())
                EventBus.getDefault().post(HideFeedFloatingActionButton())
                frameLayout.visibility = View.VISIBLE
                feedListView.visibility = View.GONE
                val args = Bundle().apply {
                    putInt("position", position)
                    putSerializable("data", data) // Adjust type if needed
                }
                feedRepostMultipleImageFragment = FeedRepostMultipleImageFragment()
                feedRepostMultipleImageFragment?.setListener(this)
                feedRepostMultipleImageFragment?.arguments = args
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.feed_text_view_fragment,
                        feedRepostMultipleImageFragment!!
                    ) // Use the correct container ID
                    .addToBackStack(null) // Optional, to add to back stack
                    .commit()

            }

            "audio", "vn" -> {
                EventBus.getDefault().post(HideBottomNav())
                EventBus.getDefault().post(HideAppBar())
                EventBus.getDefault().post(HideFeedFloatingActionButton())
                frameLayout.visibility = View.VISIBLE
                feedListView.visibility = View.GONE
                val args = Bundle().apply {
                    putInt("position", position)
                    putSerializable("data", data) // Adjust type if needed
                }
                feedRepostAudioViewFragment = FeedRepostAudioViewFragment()
                feedRepostAudioViewFragment?.setListener(this)
                feedRepostAudioViewFragment?.arguments = args
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.feed_text_view_fragment,
                        feedRepostAudioViewFragment!!
                    ) // Use the correct container ID
                    .addToBackStack(null) // Optional, to add to back stack
                    .commit()

            }
            "image" -> {

                EventBus.getDefault().post(HideBottomNav())
                EventBus.getDefault().post(HideAppBar())
                EventBus.getDefault().post(HideFeedFloatingActionButton())
                frameLayout.visibility = View.VISIBLE
                feedListView.visibility = View.GONE
                val args = Bundle().apply {
                    putInt("position", position)
                    putSerializable("data", data) // Adjust type if needed
                }
                feedRepostImageFragment = FeedRepostImageFragment()
                feedRepostImageFragment?.setListener(this)
                feedRepostImageFragment?.arguments = args
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.feed_text_view_fragment,
                        feedRepostImageFragment!!
                    ) // Use the correct container ID
                    .addToBackStack(null) // Optional, to add to back stack
                    .commit()
            }

            "text" -> {
                EventBus.getDefault().post(HideBottomNav())
                EventBus.getDefault().post(HideAppBar())
                EventBus.getDefault().post(HideFeedFloatingActionButton())
                frameLayout.visibility = View.VISIBLE
                feedListView.visibility = View.GONE
                val args = Bundle().apply {
                    putInt("position", position)
                    putSerializable("data", data) // Adjust type if needed
                }
                feedRepostTextFragment = FeedRepostTextFragment()
                feedRepostTextFragment?.setListener(this)
                feedRepostTextFragment?.arguments = args
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.feed_text_view_fragment,
                        feedRepostTextFragment!!
                    ) // Use the correct container ID
                    .addToBackStack(null) // Optional, to add to back stack
                    .commit()
            }

            "docs" -> {

                EventBus.getDefault().post(HideBottomNav())
                EventBus.getDefault().post(HideAppBar())
                EventBus.getDefault().post(HideFeedFloatingActionButton())
                frameLayout.visibility = View.VISIBLE
                feedListView.visibility = View.GONE
                val args = Bundle().apply {
                    putInt("position", position)
                    putSerializable("data", data) // Adjust type if needed
                }
                feedRepostDocFragment = FeedRepostDocFragment()
                feedRepostDocFragment?.setListener(this)
                feedRepostDocFragment?.arguments = args
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.feed_text_view_fragment,
                        feedRepostDocFragment!!
                    ) // Use the correct container ID
                    .addToBackStack(null) // Optional, to add to back stack
                    .commit()

            }

            "video" -> {

                EventBus.getDefault().post(HideBottomNav())
                EventBus.getDefault().post(HideAppBar())
                EventBus.getDefault().post(HideFeedFloatingActionButton())
                frameLayout.visibility = View.VISIBLE
                feedListView.visibility = View.GONE
                val args = Bundle().apply {
                    putInt("position", position)
                    putSerializable("data", data) // Adjust type if needed
                }
                feedRepostVideoViewFragment = FeedRepostVideoViewFragment()
                feedRepostVideoViewFragment?.setListener(this)
                feedRepostVideoViewFragment?.arguments = args
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.feed_text_view_fragment,
                        feedRepostVideoViewFragment!!
                    ) // Use the correct container ID
                    .addToBackStack(null) // Optional, to add to back stack
                    .commit()

            }
        }
    }

    private fun  shareTextFeed(data: com.uyscuti.social.network.api.response.posts.Post) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, data.content)
            type = "text/plain"
        }
        // Verify that the Intent will resolve to an activity
        if (sendIntent.resolveActivity(requireContext().packageManager) != null) {
        // Start the activity to share the text
            startActivity(Intent.createChooser(sendIntent, "Share via"))
        }

    }

    private fun replaceFragment(fragment: Fragment) {
        val supportFragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }



    @SuppressLint("MissingInflatedId", "ServiceCast", "InflateParams")// Suppresses lint warning for missing inflated ID check
    override fun feedShareClicked
                (position: Int, data: com.uyscuti.social.network.api.response.posts.Post)
    {
        val context = requireContext()

        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val shareView = layoutInflater.inflate(R.layout.bottom_dialog_for_share, null)
        val close_button = shareView.findViewById<ImageButton>(R.id.close_button)
        val recyclerView = shareView.findViewById<RecyclerView>(R.id.apps_recycler_view)

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
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = resolveInfoList?.let { ShareFeedPostAdapter(it, context, data) }


    }

    override fun followButtonClicked(
        followUnFollowEntity: FollowUnFollowEntity,
        followButton: AppCompatButton
    ) {

        EventBus.getDefault().post(
            FeedFavoriteFollowUpdate(
                followUnFollowEntity.userId,
                followUnFollowEntity.isFollowing
            )
        )

        feesShortsSharedViewModel.setData(
            FollowUnFollowEntity(
            followUnFollowEntity.userId,
            followUnFollowEntity.isFollowing
        )
        )

        followClicked(followUnFollowEntity)

    }

    override fun feedRepostPost(position: Int, data: Post) {
        val view: View = layoutInflater.inflate(R.layout.feed_moreoptions_bottomsheet_layout, null)
        val reportUser : MaterialCardView = view.findViewById(R.id.reportOptionLayout)
        val quoteButton: MaterialCardView = view.findViewById(R.id.rePostFeedLayout)
        val repostButton: MaterialCardView = view.findViewById(R.id.shareFeedLayout)
        val download: MaterialCardView = view.findViewById(R.id.downloadFeedLayout)
        download.visibility = View.GONE
        repostButton.visibility = View.VISIBLE

        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(view)
        dialog.show()


        repostButton.setOnClickListener {
            if (data.isReposted) {
                data.repostedUsers.size > 1 // Example, add current user to reposted users+= "currentUserId" // Example, append current user to reposted users


            } else {
                data.repostedUsers.size < 0

            }
        }

        quoteButton.setOnClickListener {
            dialog.dismiss()

            val fragment = NewRepostedPostFragment(data)
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, fragment) // Ensure fragment_container is correct
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    override fun feedRepostPostClicked(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.Post
    ) {

    }

    override fun feedClickedToOriginalPost(position: Int, originalPostId: String) {

    }

    override fun onImageClick() {

    }

    private fun followClicked(followUnFollowEntity: FollowUnFollowEntity) {
        Log.d("followButtonClicked", "followButtonClicked: $followUnFollowEntity")
        val followListItem: List<ShortsEntityFollowList> = listOf(
            ShortsEntityFollowList(
                followUnFollowEntity.userId, followUnFollowEntity.isFollowing
            )
        )
        lifecycleScope.launch(Dispatchers.IO) {

            val uniqueFollowList = removeDuplicateFollowers(followListItem)

            followUnFollowViewModel.followUnFollow(followUnFollowEntity.userId)
            Log.d(
                "followButtonClicked",
                "followButtonClicked: Inserted uniqueFollowList $uniqueFollowList"
            )
            delay(100)
        }
    }



    override fun backPressedFromFeedTextViewFragment() {
        Log.d(TAG, "backPressedFromFeedTextViewFragment: listening back pressed ")
        allFeedAdapterRecyclerView.visibility = View.VISIBLE
        frameLayout.visibility = View.GONE
        EventBus.getDefault().post(ShowBottomNav(false))
        EventBus.getDefault().post(ShowAppBar(false))
        EventBus.getDefault().post(ShowFeedFloatingActionButton(false))
    }
    override fun onCommentClickFromFeedTextViewFragment(
        position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {
        EventBus.getDefault().post(FeedCommentClicked(position, data))
    }
    override fun onLikeUnLikeFeedFromFeedTextViewFragment(
        position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {
        try {

            val updatedComment = if (data.likes>1) {
                data.copy(

                    likes = data.likes + 1,
                    repostedByUserId = data.repostedByUserId?:"",
                )
            } else {
                data.copy(
                    likes = data.likes - 1,
                    repostedByUserId = data.repostedByUserId?:"",
                )
            }
            lifecycleScope.launch {
                feedUploadViewModel.likeUnLikeFeed(data._id)
            }
            Log.d("likeUnLikeFeed", "likeUnLikeFeed: likes count is ${data.likes}")
            val updatedItems = getFeedViewModel.getAllFeedData()
            for (updatedItem in updatedItems) {
                if (updatedItem._id == data._id) {
                    updatedItem.likes = data.likes
                    if (data.likes > 1) {
                        updatedItem.likes +=1
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
                    EventBus.getDefault().post(FeedLikeClick(position, updatedComment))
                    Log.d("likeUnLikeFeed", "likeUnLikeFeed: remove feed from favorite fragment")
                } else {
                    Log.d("likeUnLikeFeed", "likeUnLikeFeed: add feed to favorite fragment")
                }
            } else {
                Log.i("likeUnLikeFeed", "likeUnLikeFeed: my feed data is empty")
            }
            val isMyFeedEmpty = getFeedViewModel.getMyFeedData().isEmpty()
            if (!isMyFeedEmpty) {
                val myFeedData = getFeedViewModel.getMyFeedData()
                val feedToUpdate = myFeedData.find { feed -> feed._id == data._id }
                if (feedToUpdate != null) {

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

    override fun onFeedFavoriteClickFromFeedTextViewFragment(

        position: Int, data:com.uyscuti.social.network.api.response.posts.Post) {
        EventBus.getDefault().post(FeedFavoriteClick(position, data))

        val isMyFeedEmpty = getFeedViewModel.getMyFeedData().isEmpty()
        if (!isMyFeedEmpty) {
            val myFeedData = getFeedViewModel.getMyFeedData()
            val feedToUpdate = myFeedData.find { feed -> feed._id == data._id }
            if (feedToUpdate != null) {
                feedToUpdate.isBookmarked = data.isBookmarked
                val myFeedDataPosition = getFeedViewModel.getMyFeedPositionById(feedToUpdate._id)
                getFeedViewModel.updateMyFeedData(myFeedDataPosition, feedToUpdate)
            } else {
                Log.d(TAG, "feedFavoriteClick: feed to update is not available in the list")
            }
        } else {
            Log.i(TAG, "feedFavoriteClick: my feed data is empty")
        }

        val allFeed = getFeedViewModel.getAllFeedData().isEmpty()
        if (!allFeed) {
            Log.i(TAG, "onFeedFavoriteClickFromFeedTextViewFragment: allFeed is not empty")
            val allFeedPost = getFeedViewModel.getAllFeedData().find { it._id == data._id }
            if (allFeedPost != null) {
                Log.i(TAG, "onFeedFavoriteClickFromFeedTextViewFragment: allFeedPost is not null")
                val allFeedPosition = getFeedViewModel.getAllFeedDataPositionById(allFeedPost._id)
                getFeedViewModel.updateForAllFeedFragment(allFeedPosition, data)
            } else {
                Log.i(TAG, "onFeedFavoriteClickFromFeedTextViewFragment: allFeedPost is null")
            }
        } else {
            Log.e(TAG, "onFeedFavoriteClickFromFeedTextViewFragment: allFeed is empty")
        }
        lifecycleScope.launch {
            feedUploadViewModel.favoriteFeed(data._id)
        }
    }

    override fun onMoreOptionsClickFromFeedTextViewFragment(
        position: Int, data: com.uyscuti.social.network.api.response.posts.Post) {
        val view: View = layoutInflater.inflate(R.layout.feed_moreoptions_bottomsheet_layout, null)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(view)
        dialog.show()
        val reportUser : MaterialCardView = view.findViewById(R.id.reportOptionLayout)
        val quoteButton: MaterialCardView = view.findViewById(R.id.rePostFeedLayout)
        val repostButton: MaterialCardView = view.findViewById(R.id.shareFeedLayout)
        val download: MaterialCardView = view.findViewById(R.id.downloadFeedLayout)
        download.setOnClickListener {

            dialog.dismiss()
        }

    }

    override fun finishedPlayingVideo(position: Int) {

    }

    override fun onRePostClickFromFeedTextViewFragment(
        position: Int,
        data: com.uyscuti.social.network.api.response.posts.Post
    ) {

    }

    override fun onFullScreenClicked(data: MixedFeedUploadDataClass) {

    }

    override fun onMediaClick(data: MixedFeedUploadDataClass) {

    }

    override fun onMediaPrepared(mp: MediaPlayer) {

    }

    override fun onMediaError() {

    }


    private fun initiateDownload(post: com.uyscuti.social.network.api.response.getfeedandresposts.Post) {
        post.files.forEachIndexed { index, file ->
            val fileName = post.fileNames.getOrNull(index)?.fileName ?: "default_file_name"
            val fileType = post.fileTypes.getOrNull(index)?.fileType ?: post.contentType
            val fileUrl = file.url // Assuming `File` class has a `url` attribute

            downloadMediaFile(fileUrl, fileName, fileType)
        }
    }



}

