package com.uyscuti.sharedmodule.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.sharedmodule.GroupProfileViewModel
import com.uyscuti.sharedmodule.GroupResult
import com.uyscuti.sharedmodule.data.model.Dialog
import com.uyscuti.sharedmodule.data.model.User
import com.uyscuti.sharedmodule.R
import com.uyscuti.sharedmodule.adapter.GroupParticipantAdapter
import com.uyscuti.social.core.common.data.room.database.ChatDatabase
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_group_participants, container, false)
        recyclerView       = view.findViewById(R.id.participantsListRV)
        participantsNumber = view.findViewById(R.id.participantsNumber)
        recyclerView.layoutManager = LinearLayoutManager(context)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs: SharedPreferences =
            requireContext().getSharedPreferences("LocalSettings", 0)
        myUserId   = prefs.getString("_id", "")      ?: ""
        myUsername = prefs.getString("username", "") ?: ""
        myAvatar   = prefs.getString("avatar", "")  ?: ""

        val chatId = arguments?.getString(ARG_CHAT_ID) ?: ""

        messageRepository = MessageRepository(
            requireContext(),
            ChatDatabase.getInstance(requireContext()).messageDao(),
            retrofitInstance
        )

        adapter = GroupMembersAdapter(
            context  = requireContext(),
            myRole   = myRole,
            myUserId = myUserId,
            onAction = { member, action ->
                handleMemberAction(chatId, member, action)
            }
        )
        recyclerView.adapter = adapter

        viewModel.members.observe(viewLifecycleOwner) { result ->
            when (result) {
                is GroupResult.Success -> {
                    //  Always cache the latest successful member list
                    cachedMembers = result.data

                    // Check if I am still in the list
                    val iAmStillMember = result.data.any { it.user._id == myUserId }

                    if (!iAmStillMember && result.data.isNotEmpty()) {
                        // I was removed — show cached list in read-only mode
                        iWasRemoved = true
                        showRemovedBanner()
                        showMembersReadOnly(cachedMembers)
                        return@observe
                    }

                    // Normal flow — I am still a member
                    iWasRemoved = false

                    val me = result.data.find { it.user._id == myUserId }
                    if (me != null && me.role.name != myRole) {
                        myRole = me.role.name
                        adapter = GroupMembersAdapter(
                            context  = requireContext(),
                            myRole   = myRole,
                            myUserId = myUserId,
                            onAction = { member, action ->
                                handleMemberAction(chatId, member, action)
                            }
                        )
                        recyclerView.adapter = adapter
                    }

                    val sorted = sortMembers(result.data, adminId)
                    adapter.submitList(sorted)
                    participantsNumber.text = "${result.data.size} participants"
                }

                is GroupResult.Error -> {
                    // If the API fails (e.g. because I was removed and no longer
                    // have access), fall back to the cached list in read-only mode
                    if (cachedMembers.isNotEmpty()) {
                        iWasRemoved = true
                        showRemovedBanner()
                        showMembersReadOnly(cachedMembers)
                    } else {
                        participantsNumber.text = "Failed to load members"
                    }
                }

                is GroupResult.Loading -> {
                    // Only show loading text if we have nothing cached yet
                    if (cachedMembers.isEmpty()) {
                        participantsNumber.text = "Loading…"
                    }
                }
            }
        }

        viewModel.roleChange.observe(viewLifecycleOwner) { result ->
            when (result) {
                is GroupResult.Success ->
                    Toast.makeText(requireContext(), "Role updated ✓", Toast.LENGTH_SHORT).show()
                is GroupResult.Error ->
                    Toast.makeText(requireContext(),
                        "Role change failed: ${result.message}", Toast.LENGTH_LONG).show()
                else -> {}
            }
        }

        viewModel.removeMember.observe(viewLifecycleOwner) { result ->
            when (result) {
                is GroupResult.Success ->
                    Toast.makeText(requireContext(), "Member removed", Toast.LENGTH_SHORT).show()
                is GroupResult.Error ->
                    Toast.makeText(requireContext(),
                        "Remove failed: ${result.message}", Toast.LENGTH_LONG).show()
                else -> {}
            }
        }

        viewModel.muteMemberResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is GroupResult.Success ->
                    Toast.makeText(requireContext(), result.data, Toast.LENGTH_SHORT).show()
                is GroupResult.Error ->
                    Toast.makeText(requireContext(),
                        "Mute failed: ${result.message}", Toast.LENGTH_LONG).show()
                else -> {}
            }
        }
    }

    // Show a read-only version of the member list (no action buttons)
    // Used when the current user has been removed from the group
    private fun showMembersReadOnly(members: List<GroupMember>) {
        // Re-create the adapter with myRole = "removed" so no action buttons appear
        adapter = GroupMembersAdapter(
            context  = requireContext(),
            myRole   = "removed",   // ← signals adapter to hide all action buttons
            myUserId = myUserId,
            onAction = { _, _ -> }  // ← no-op, nothing is clickable
        )
        recyclerView.adapter = adapter

        val sorted = sortMembers(members, adminId)
        adapter.submitList(sorted)
        participantsNumber.text = "${members.size} participants (you were removed)"
    }

}