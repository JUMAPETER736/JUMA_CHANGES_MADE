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

