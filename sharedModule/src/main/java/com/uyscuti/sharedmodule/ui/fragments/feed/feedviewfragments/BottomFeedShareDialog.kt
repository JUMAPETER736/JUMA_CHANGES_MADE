package com.uyscuti.sharedmodule.ui.fragments.feed.feedviewfragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.uyscuti.sharedmodule.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [BottomFeedShareDialog.newInstance] factory method to
 * create an instance of this fragment.
 */
class BottomFeedShareDialog : Fragment() {

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BottomFeedShareDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    private var param1: String? = null
    private var param2: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_bottom_feed_share_dialog, container, false)
        val copyLinkButton : LinearLayout = view.findViewById(R.id.copy_link)
        val sharePublic : LinearLayout = view.findViewById(R.id.share_public)
        val shareToChat : LinearLayout = view.findViewById(R.id.share_toChat)
        
        sharePublic.setOnClickListener {
            Toast.makeText(requireContext(), "share to public", Toast.LENGTH_SHORT).show()
        }

        shareToChat.setOnClickListener { 
            Toast.makeText(requireContext(),"share to chat", Toast.LENGTH_SHORT).show()
        }
        copyLinkButton.setOnClickListener {
            Toast.makeText(requireContext(), "copy is clicked ", Toast.LENGTH_SHORT).show()
        }
        
        return  view
    } 


    fun show(parentFragmentManager: FragmentManager, s: String) {
        parentFragmentManager.beginTransaction().apply {
            // Use add() if you want to show it as a dialog fragment
            add(this@BottomFeedShareDialog, tag)
            commit()
        }
    }

}