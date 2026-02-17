package com.uyscuti.sharedmodule.ui.fragments.feed.feedviewfragments

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Outline
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson
import com.bumptech.glide.request.RequestOptions
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.JsonSyntaxException
import com.google.gson.stream.MalformedJsonException
import com.uyscuti.sharedmodule.R
import com.uyscuti.sharedmodule.ReportNotificationActivity2
import com.uyscuti.sharedmodule.User_Interfaces.OtherUserProfile.OtherUserProfileAccount
import com.uyscuti.sharedmodule.adapter.feed.FeedAdapter
import com.uyscuti.sharedmodule.adapter.feed.OnFeedClickListener
import com.uyscuti.sharedmodule.data.model.shortsmodels.OtherUsersProfile
import com.uyscuti.sharedmodule.databinding.FragmentOriginalPostWithRepostInsideBinding
import com.uyscuti.sharedmodule.model.GoToUserProfileFragment
import com.uyscuti.sharedmodule.model.HideAppBar
import com.uyscuti.sharedmodule.model.HideBottomNav
import com.uyscuti.sharedmodule.model.ShowAppBar
import com.uyscuti.sharedmodule.model.ShowBottomNav
import com.uyscuti.sharedmodule.model.business.User
import com.uyscuti.sharedmodule.ui.fragments.feed.feedviewfragments.editRepost.Fragment_Edit_Post_To_Repost
import com.uyscuti.sharedmodule.ui.fragments.feed.feedviewfragments.feedRepost.PostItem
import com.uyscuti.sharedmodule.ui.fragments.feed.feedviewfragments.feedRepost.Tapped_Files_In_The_Container_View_Fragment
import com.uyscuti.sharedmodule.utils.FollowingManager
import com.uyscuti.sharedmodule.utils.waveformseekbar.WaveformSeekBar
import com.uyscuti.sharedmodule.viewmodels.feed.GetFeedViewModel
import com.uyscuti.sharedmodule.viewmodels.feed.UserRelationshipsViewModel
import com.uyscuti.social.network.api.response.posts.OriginalPost
import com.uyscuti.social.network.api.response.posts.Post
import com.uyscuti.social.network.api.response.posts.File
import com.uyscuti.social.network.api.response.posts.FileType
import java.text.SimpleDateFormat
import java.util.*
import com.uyscuti.social.network.utils.LocalStorage
import retrofit2.Call
import retrofit2.Callback
import kotlin.math.abs
import com.uyscuti.social.core.common.data.room.entity.FollowUnFollowEntity
import com.uyscuti.social.network.api.response.posts.Avatar
import com.uyscuti.social.network.api.response.allFeedRepostsPost.BookmarkRequest
import com.uyscuti.social.network.api.response.allFeedRepostsPost.CommentCountResponse
import com.uyscuti.social.network.api.response.allFeedRepostsPost.CommentsResponse
import com.uyscuti.social.network.api.response.allFeedRepostsPost.RepostResponse
import com.uyscuti.social.network.api.response.allFeedRepostsPost.RetrofitClient
import com.uyscuti.social.network.api.response.allFeedRepostsPost.ShareResponse
import com.uyscuti.social.network.api.response.comment.allcomments.Comment
import com.uyscuti.social.network.api.response.post.Thumbnail
import com.uyscuti.social.network.api.response.posts.Author
import com.uyscuti.social.network.api.response.posts.RepostedUser
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.Response
import javax.inject.Inject
import kotlin.getValue

private const val TAG = "Fragment_Original_Post_With_Repost_Inside"
private const val FRAGMENT_ORIGINAL_POST_WITH_REPOST = 1



@AndroidEntryPoint
class Fragment_Original_Post_With_Repost_Inside : Fragment() {

    // Inject dependencies
    @Inject
    lateinit var retrofitInstance: RetrofitInstance

    private lateinit var feedAdapter: FeedAdapter
    private lateinit var recyclerView: RecyclerView

    // Store the post data
    private var postData: Post? = null

    companion object {
        private const val ARG_POST_DATA = "post_data"

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

        // Get post data from arguments
        arguments?.getString(ARG_POST_DATA)?.let { jsonData ->
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

        setupRecyclerView(view)
        loadPostData()
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView) // Add this to your layout

        // Initialize the adapter with the FeedClickListener
        feedAdapter = FeedAdapter(
            context = requireContext(),
            retrofitInterface = retrofitInstance,
            feedClickListener = createFeedClickListener(),
            fragmentManager = parentFragmentManager,
            followingUserIds = getFollowingUserIds()
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = feedAdapter
            setHasFixedSize(true)
        }
    }

    private fun loadPostData() {
        postData?.let { post ->
            // Submit the single post as a list to the adapter
            feedAdapter.submitList(listOf(post))
        }
    }

    private fun getFollowingUserIds(): Set<String> {
        // Get following list from FollowingManager or LocalStorage
        val followingManager = FollowingManager(requireContext())
        return followingManager.getFollowingList().toSet()
    }

    private fun createFeedClickListener(): OnFeedClickListener {
        return object : OnFeedClickListener {
            override fun likeUnLikeFeed(position: Int, data: Post) {
                Log.d("FragmentRepost", "Like clicked on post: ${data._id}")
                // Handle like action
            }

            override fun feedCommentClicked(position: Int, data: Post) {
                Log.d("FragmentRepost", "Comment clicked on post: ${data._id}")
                // Navigate to comments
                navigateToComments(data)
            }

            override fun feedFavoriteClick(position: Int, data: Post) {
                Log.d("FragmentRepost", "Bookmark clicked on post: ${data._id}")
                // Handle bookmark
            }

            override fun feedRepostPost(position: Int, data: Post) {
                Log.d("FragmentRepost", "Repost clicked on post: ${data._id}")
                // Handle repost
            }

            override fun feedShareClicked(position: Int, data: Post) {
                Log.d("FragmentRepost", "Share clicked on post: ${data._id}")
                // Handle share
            }

            override fun feedRepostFileClicked(position: Int, originalPost: OriginalPost) {
                Log.d("FragmentRepost", "Media clicked on original post")
                // Handle media click
            }

            override fun followButtonClicked(followEntity: FollowUnFollowEntity, button: View) {
                Log.d("FragmentRepost", "Follow button clicked")
                // Handle follow
            }

            override fun moreOptionsClick(position: Int, data: Post) {
                Log.d("FragmentRepost", "More options clicked")
                // Show more options menu
            }
        }
    }

    private fun navigateToComments(post: Post) {
        // Navigate to your comment fragment
       // val commentFragment = Fragment_Post_Comment_Section.newInstance(post)
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
         //   .replace(android.R.id.content, commentFragment)
            .addToBackStack(null)
            .commit()
    }
}



