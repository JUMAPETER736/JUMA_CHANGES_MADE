package com.uyscuti.sharedmodule.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.sharedmodule.data.model.Dialog
import com.uyscuti.sharedmodule.data.model.User
import com.uyscuti.sharedmodule.R
import com.uyscuti.sharedmodule.adapter.GroupParticipantAdapter
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class GroupParticipantsFragment : Fragment() {

    companion object {
        private const val ARG_ADMIN_ID = "adminId"
        private const val ARG_MY_ROLE = "myRole"
        private const val ARG_CHAT_ID = "chatId"

        fun newInstance(chatId: String, adminId: String, myRole: String) =
            GroupParticipantsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CHAT_ID, chatId)
                    putString(ARG_ADMIN_ID, adminId)
                    putString(ARG_MY_ROLE, myRole)
                }
            }
    }

    


}