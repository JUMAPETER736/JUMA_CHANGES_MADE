package com.uyscuti.social.circuit.User_Interface.fragments.feed.feedviewfragments


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.uyscuti.social.circuit.R
import com.uyscuti.social.network.api.response.allFeedRepostsPost.RepostRequest
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject



class BottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var data: com.uyscuti.social.network.api.response.getrepostsPostsoriginal.Post
    @Inject
    lateinit var retrofitInstance: RetrofitInstance



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bottom_sheet_feed_fragments, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        val copyLink: Button = view.findViewById(R.id.copy_link)
        val copyLink : LinearLayout? = view.findViewById(R.id.copy_link)
        copyLink?.setOnClickListener {


        }
        val repostRetweet : LinearLayout? = view.findViewById(R.id.rePostFeedLayout2)
        repostRetweet?.setOnClickListener {
            repostsFeed()

        }
/**
   this is a temporally code to open the chat fragment the goal is make it
   params/
   bring available users so that the user can choose
   **/
        val shareToChat :LinearLayout? = view.findViewById(R.id.share_toChat)
        shareToChat?.setOnClickListener {
//            replaceFragment(ChatFragment.newInstance("", ""))
        }
        val repostFeed : LinearLayout?= view.findViewById(R.id.rePostFeedLayout)
        repostFeed?.setOnClickListener {
//            replaceFragment(NewRepostedPostFragment(data))
            Toast.makeText(requireContext(), "REPOST CLICKED ", Toast.LENGTH_SHORT).show()
        }
    }

    private fun replaceFragment(fragment: NewRepostedPostFragment) {
        val supportFragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.feed_text_view_fragment, fragment)
        fragmentTransaction.commit()
    }

    private fun repostsFeed(){
        CoroutineScope(Dispatchers.IO).launch {
            try {

                val repostRequest = RepostRequest(
                    isReposted = true,
                    comment = "",
                    files = null,
                    tags = null
                )

                val response = retrofitInstance.apiService.repostsFeed(
                    postId = data._id,
                    request = repostRequest
                )

                val responseBody = response.body()
                Log.d("repostsFeed",
                    "Feed Feed repostsFeed feed: response body message" +
                            " ${responseBody!!.message}")
                val responseData = responseBody.data

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful){
                        Toast.makeText(requireContext(),
                            "Reposted", Toast.LENGTH_SHORT).show()
                        requireActivity().onBackPressed()
                    } else {
                        Toast.makeText(requireContext(),
                            "Error", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(),
                        "Network error", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            }
        }
    }



}

