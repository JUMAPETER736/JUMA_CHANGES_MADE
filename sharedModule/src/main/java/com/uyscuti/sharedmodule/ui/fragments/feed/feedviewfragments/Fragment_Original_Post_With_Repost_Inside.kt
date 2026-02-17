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

        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = feedAdapter
            setHasFixedSize(true)
        }
    }

    private fun loadPostData() {
        postData?.let { post ->
            // Since FeedAdapter extends a paginated adapter, we need to check its implementation
            // If it has a method to load single posts, use it
            // Otherwise, trigger a manual data load

            // Option 1: If you have access to the data source
            lifecycleScope.launch {
                try {
                    // Fetch the post again to ensure fresh data
                    val response = retrofitInstance.apiService.getPostById(post._id)
                    if (response.isSuccessful && response.body() != null) {
                        val freshPost = response.body()!!
                        // Now you need to submit this to the adapter
                        // Since it's a PagingDataAdapter, you'll need to create PagingData
                        submitSinglePost(freshPost)
                    }
                } catch (e: Exception) {
                    Log.e("Fragment", "Error loading post", e)
                    // Fallback to using the post we already have
                    submitSinglePost(post)
                }
            }
        }
    }

    private fun submitSinglePost(post: Post) {
        // Since your FeedAdapter likely extends PagingDataAdapter or similar,
        // you need to create a way to submit single posts
        // Add this method to your FeedAdapter:

        lifecycleScope.launch {
            // Create a simple PagingData with single item
            val pagingData = PagingData.from(listOf(post))
            feedAdapter.submitData(pagingData)
        }
    }

    private fun getFollowingUserIds(): Set<String> {
        val followingManager = FollowingManager(requireContext())
        return followingManager.getFollowingList().toSet()
    }

    private fun createFeedClickListener(): OnFeedClickListener {
        return object : OnFeedClickListener {

            override fun likeUnLikeFeed(position: Int, data: Post) {
                Log.d("FragmentRepost", "Like clicked - ViewHolder handles API call")
                // ViewHolder already handles the like API call and UI update
                // Just update local reference if needed
                postData = data
            }

            override fun feedCommentClicked(position: Int, data: Post) {
                Log.d("FragmentRepost", "Comment clicked on post: ${data._id}")
                navigateToComments(data)
            }

            override fun feedFavoriteClick(position: Int, data: Post) {
                Log.d("FragmentRepost", "Bookmark clicked - ViewHolder handles API call")
                // ViewHolder already handles the bookmark API call and UI update
                postData = data
            }

            override fun feedRepostPost(position: Int, data: Post) {
                Log.d("FragmentRepost", "Repost button clicked")
                // Navigate to edit repost fragment
                navigateToEditRepost(data)
            }

            override fun feedShareClicked(position: Int, data: Post) {
                Log.d("FragmentRepost", "Share clicked - ViewHolder shows bottom sheet")
                // ViewHolder already shows share bottom sheet and handles increment
            }

            override fun feedFileClicked(position: Int, data: Post) {
                Log.d("FragmentRepost", "File clicked - ViewHolder handles navigation")
                // ViewHolder already navigates to file viewer
            }

            override fun feedRepostFileClicked(position: Int, data: OriginalPost) {
                Log.d("FragmentRepost", "Original post file clicked - ViewHolder handles navigation")
                // ViewHolder already navigates to file viewer
            }

            override fun followButtonClicked(
                followUnFollowEntity: FollowUnFollowEntity,
                followButton: AppCompatButton
            ) {
                Log.d("FragmentRepost", "Follow button clicked for: ${followUnFollowEntity.userId}")
                // ViewHolder already handles follow/unfollow API call and cache updates
                // Just make the API call here for consistency
                handleFollowAction(followUnFollowEntity, followButton)
            }

            override fun moreOptionsClick(position: Int, data: Post) {
                Log.d("FragmentRepost", "More options clicked")
                showMoreOptionsBottomSheet(data)
            }

            override fun feedRepostPostClicked(position: Int, data: Post) {
                Log.d("FragmentRepost", "Repost card clicked")
                // Already viewing the repost, do nothing or refresh
            }

            override fun feedClickedToOriginalPost(position: Int, originalPostId: String) {
                Log.d("FragmentRepost", "Navigate to original post: $originalPostId")
                navigateToOriginalPostWithoutRepost(originalPostId)
            }

            override fun onImageClick() {
                Log.d("FragmentRepost", "Image clicked")
            }
        }
    }

    private fun handleFollowAction(
        followEntity: FollowUnFollowEntity,
        followButton: AppCompatButton
    ) {
        lifecycleScope.launch {
            try {
                val response = retrofitInstance.apiService.followUnFollowUser(
                    followEntity.userId,
                    followEntity.isFollow
                )

                if (response.isSuccessful) {
                    val followingManager = FollowingManager(requireContext())
                    if (followEntity.isFollow) {
                        followingManager.addToFollowing(followEntity.userId)
                        FeedAdapter.addToFollowingCache(followEntity.userId)
                        Toast.makeText(requireContext(), "Following", Toast.LENGTH_SHORT).show()
                    } else {
                        followingManager.removeFromFollowing(followEntity.userId)
                        FeedAdapter.removeFromFollowingCache(followEntity.userId)
                        Toast.makeText(requireContext(), "Unfollowed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to update", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("FragmentRepost", "Error following user", e)
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToComments(post: Post) {
        val commentFragment = Fragment_Post_Comment_Section.newInstance(post)
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .replace(android.R.id.content, commentFragment)
            .addToBackStack("comments")
            .commit()
    }

    private fun navigateToEditRepost(post: Post) {
        val editRepostFragment = Fragment_Edit_Post_To_Repost(post).apply {
            arguments = Bundle().apply {
                putString("post_data", Gson().toJson(post))
                putString("post_id", post._id)
                post.originalPost?.firstOrNull()?.let { originalPost ->
                    putString("original_post_data", Gson().toJson(originalPost))
                    putString("original_post_id", originalPost._id)
                    putString("original_content", originalPost.content)
                }
                putString("repost_type", "quote_repost")
                putString("existing_comment", post.content)
                putBoolean("is_editing_existing_repost", post.isReposted)
                putString("navigation_source", "repost_button_click")
            }
        }

        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .replace(android.R.id.content, editRepostFragment)
            .addToBackStack("edit_repost")
            .commit()
    }

    private fun navigateToOriginalPostWithoutRepost(originalPostId: String) {
        lifecycleScope.launch {
            try {
                val response = retrofitInstance.apiService.getPostById(originalPostId)
                if (response.isSuccessful && response.body() != null) {
                    val originalPost = response.body()!!
                    val fragment = Fragment_Original_Post_Without_Repost_Inside().apply {
                        arguments = Bundle().apply {
                            putString(Fragment_Original_Post_Without_Repost_Inside.ARG_ORIGINAL_POST, Gson().toJson(originalPost))
                            putString("post_id", originalPost._id)
                            putString("navigation_source", "quoted_post_card_click")
                        }
                    }

                    parentFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left,
                            R.anim.slide_in_left,
                            R.anim.slide_out_right
                        )
                        .replace(android.R.id.content, fragment)
                        .addToBackStack("original_post")
                        .commit()
                }
            } catch (e: Exception) {
                Log.e("FragmentRepost", "Error loading original post", e)
                Toast.makeText(requireContext(), "Failed to load post", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showMoreOptionsBottomSheet(data: Post) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val binding = BottomSheetMoreOptionsBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(binding.root)

        val currentUserId = LocalStorage.getInstance(requireContext()).getUserId()
        val isOwnPost = data.author?.account?._id == currentUserId

        if (isOwnPost) {
            binding.btnDelete.visibility = View.VISIBLE
            binding.btnEdit.visibility = View.VISIBLE
            binding.btnReport.visibility = View.GONE
            binding.btnBlock.visibility = View.GONE
            binding.btnMute.visibility = View.GONE
        } else {
            binding.btnDelete.visibility = View.GONE
            binding.btnEdit.visibility = View.GONE
            binding.btnReport.visibility = View.VISIBLE
            binding.btnBlock.visibility = View.VISIBLE
            binding.btnMute.visibility = View.VISIBLE
        }

        binding.btnDelete.setOnClickListener {
            deletePost(data)
            bottomSheetDialog.dismiss()
        }

        binding.btnEdit.setOnClickListener {
            editPost(data)
            bottomSheetDialog.dismiss()
        }

        binding.btnReport.setOnClickListener {
            reportPost(data)
            bottomSheetDialog.dismiss()
        }

        binding.btnBlock.setOnClickListener {
            blockUser(data)
            bottomSheetDialog.dismiss()
        }

        binding.btnMute.setOnClickListener {
            muteUser(data)
            bottomSheetDialog.dismiss()
        }

        binding.btnHide.setOnClickListener {
            hidePost(data)
            bottomSheetDialog.dismiss()
        }

        binding.btnCancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun deletePost(data: Post) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Post")
            .setMessage("Are you sure you want to delete this post?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    try {
                        val response = retrofitInstance.apiService.deletePost(data._id)
                        if (response.isSuccessful) {
                            Toast.makeText(requireContext(), "Post deleted", Toast.LENGTH_SHORT).show()
                            requireActivity().onBackPressed()
                        } else {
                            Toast.makeText(requireContext(), "Failed to delete", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("FragmentRepost", "Error deleting post", e)
                        Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun editPost(data: Post) {
        Toast.makeText(requireContext(), "Edit post - navigate to edit screen", Toast.LENGTH_SHORT).show()
    }

    private fun reportPost(data: Post) {
        val reportReasons = arrayOf(
            "Spam",
            "Harassment or bullying",
            "Inappropriate content",
            "False information",
            "Hate speech",
            "Violence",
            "Other"
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Report Post")
            .setItems(reportReasons) { _, which ->
                val reason = reportReasons[which]
                submitReport(data._id, reason)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun submitReport(postId: String, reason: String) {
        lifecycleScope.launch {
            try {
                val response = retrofitInstance.apiService.reportPost(postId, reason)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Post reported. Thank you.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to report post", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("FragmentRepost", "Error reporting post", e)
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun blockUser(data: Post) {
        val authorId = data.author?.account?._id ?: return
        val authorName = data.author?.account?.username ?: "this user"

        AlertDialog.Builder(requireContext())
            .setTitle("Block User")
            .setMessage("Are you sure you want to block @$authorName? You won't see their posts anymore.")
            .setPositiveButton("Block") { _, _ ->
                lifecycleScope.launch {
                    try {
                        val response = retrofitInstance.apiService.blockUser(authorId)
                        if (response.isSuccessful) {
                            Toast.makeText(requireContext(), "@$authorName blocked", Toast.LENGTH_SHORT).show()
                            requireActivity().onBackPressed()
                        } else {
                            Toast.makeText(requireContext(), "Failed to block user", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("FragmentRepost", "Error blocking user", e)
                        Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun muteUser(data: Post) {
        val authorId = data.author?.account?._id ?: return
        val authorName = data.author?.account?.username ?: "this user"

        AlertDialog.Builder(requireContext())
            .setTitle("Mute User")
            .setMessage("Mute posts from @$authorName? You can unmute them later from their profile.")
            .setPositiveButton("Mute") { _, _ ->
                FeedAdapter.addToMutedPostsCache(authorId)
                Toast.makeText(requireContext(), "@$authorName muted", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressed()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun hidePost(data: Post) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hide Post")
            .setMessage("Hide this post from your feed?")
            .setPositiveButton("Hide") { _, _ ->
                FeedAdapter.addToHiddenPostsCache(data._id)
                Toast.makeText(requireContext(), "Post hidden", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressed()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}



