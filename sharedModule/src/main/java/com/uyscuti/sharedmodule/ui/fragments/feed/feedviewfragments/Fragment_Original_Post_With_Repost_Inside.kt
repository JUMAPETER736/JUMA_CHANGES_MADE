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
import android.widget.Toolbar
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
import androidx.paging.PagingData
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


