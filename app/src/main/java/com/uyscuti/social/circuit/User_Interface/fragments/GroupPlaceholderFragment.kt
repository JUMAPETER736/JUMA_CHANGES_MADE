package com.uyscuti.social.circuit.User_Interface.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.uyscuti.social.circuit.R

//import com.example.mylibrary.LoginActivity
//import com.example.business.LoginActivity
//import com.uyscuti.buziness.LoginActivity

/**
 * A simple [Fragment] subclass.
 * Use the [GroupPlaceholderFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GroupPlaceholderFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
//            param1 = it.getString(ARG_PARAM1)
//            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_group_placeholder, container, false)

        val button : Button = view.findViewById(R.id.button)

        button.setOnClickListener {
            Log.d("TAG", "onCreateView: button clicked")
//            val intent = Intent(requireActivity(), LoginActivity::class.java)
//            requireContext().startActivity(intent)
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
         * @return A new instance of fragment GroupPlaceholderFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GroupPlaceholderFragment().apply {
                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
                }
            }
    }
}