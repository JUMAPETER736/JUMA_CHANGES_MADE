package com.uyscuti.social.circuit.ui.fragments.feed.feedRepostViewFragments

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.uyscuti.sharedmodule.adapter.feed.feed.feedRepostViewAdapter.FeedRepostMultipleAudioAdapter
import com.uyscuti.sharedmodule.adapter.feed.feed.feedRepostViewAdapter.PlayFeedAudioInterface
import com.uyscuti.sharedmodule.interfaces.feedinterfaces.FeedTextViewFragmentInterface
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.FragmentFeedRepostAudioViewBinding
import com.uyscuti.social.network.api.response.allFeedRepostsPost.OriginalPost
import java.io.IOException
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FeedRepostAudioViewFragment.newInstance] factory method to
 * create an instance of this fragment.
 */private const val TAG = "FeedRepostAudioViewFragment"

class FeedRepostAudioViewFragment() : Fragment(), PlayFeedAudioInterface {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var data: OriginalPost
    private var position = 0
    private lateinit var backPressedCallback: OnBackPressedCallback
    private var feedTextViewFragmentInterface: FeedTextViewFragmentInterface? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioPlayingPosition = -1
    private var previousPosition = -1
    private var seekBar: SeekBar? = null
    private var currentDuration: TextView? = null
    private var pausePlayButton: ImageView? = null
    private lateinit var binding: FragmentFeedRepostAudioViewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            data = (it.getSerializable("data") as OriginalPost?)!!
            position = it.getInt("position")

        }
    }


    private var handler: Handler? = null
    private var isPlaying = false
    private lateinit var updateSeekBarRunnable: Runnable
    private var adapter: FeedRepostMultipleAudioAdapter? = null


}