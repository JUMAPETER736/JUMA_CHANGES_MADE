package com.uyscuti.sharedmodule.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.sharedmodule.GroupProfileViewModel
import com.uyscuti.sharedmodule.data.model.Dialog
import com.uyscuti.sharedmodule.data.model.User
import com.uyscuti.sharedmodule.R
import com.uyscuti.sharedmodule.adapter.GroupParticipantAdapter
import com.uyscuti.social.core.common.data.room.repository.MessageRepository
import com.uyscuti.social.network.api.request.group.GroupMember
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.getValue


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


    private lateinit var recyclerView:       RecyclerView
    private lateinit var participantsNumber: TextView
    private lateinit var adapter:            GroupMembersAdapter
    private lateinit var adminId:            String
    private lateinit var myRole:             String

    private val viewModel: GroupProfileViewModel by activityViewModels()

    @Inject lateinit var retrofitInstance: RetrofitInstance
    @Inject lateinit var localStorage: LocalStorage

    private lateinit var messageRepository: MessageRepository

    private var myUserId:   String = ""
    private var myUsername: String = ""
    private var myAvatar:   String = ""

    // Cache the last known member list so we can still show it
    // even after the current user has been removed from the group
    private var cachedMembers: List<GroupMember> = emptyList()

    // Track whether the current user has been removed
    private var iWasRemoved: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adminId = arguments?.getString(ARG_ADMIN_ID) ?: ""
        myRole  = arguments?.getString(ARG_MY_ROLE)  ?: "member"
    }



}