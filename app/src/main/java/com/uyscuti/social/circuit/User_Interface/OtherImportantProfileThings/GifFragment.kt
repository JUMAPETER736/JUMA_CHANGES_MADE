package com.uyscuti.social.circuit.User_Interface.OtherImportantProfileThings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.uyscuti.social.circuit.databinding.FragmentGifBinding

class GifFragment : Fragment() {

    private lateinit var binding: FragmentGifBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_gif, container, false)

        binding = FragmentGifBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        return view
    }
}