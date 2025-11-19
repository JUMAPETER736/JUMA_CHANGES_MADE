package com.uyscuti.social.circuit.User_Interface.fragments.feed.feedviewfragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.TappedPostedFilesViewersBinding


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PostRepostedFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
private const val  TAG = "PostRepostedFragment"
class  PostRepostedFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var position = 0
    private lateinit var data:  com.uyscuti.social.network.api.response.getrepostsPostsoriginal.Post

    private lateinit var binding: TappedPostedFilesViewersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            position = it.getInt("position")
            activity?.window?.navigationBarColor =
                ContextCompat.getColor(requireContext(), R.color.black)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
       //return inflater.inflate(R.layout.tapped_images_posted_files_viewers, container, false)
        binding = TappedPostedFilesViewersBinding.inflate(layoutInflater, container, false)

        return binding.root

    }

    companion object {

        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PostRepostedFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}