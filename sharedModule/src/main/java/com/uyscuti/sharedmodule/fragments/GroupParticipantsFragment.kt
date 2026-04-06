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
import com.uyscuti.social.core.common.data.room.entity.MessageEntity
import com.uyscuti.social.core.common.data.room.entity.UserEntity
import com.uyscuti.social.core.common.data.room.repository.MessageRepository
import com.uyscuti.social.network.api.request.group.GroupMember
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.util.Date
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

    // Show a subtle banner at the top of the participant count label
    private fun showRemovedBanner() {
        participantsNumber.text = "You were removed from this group"
        participantsNumber.setTextColor(
            androidx.core.content.ContextCompat.getColor(
                requireContext(),
                android.R.color.holo_red_dark
            )
        )
    }

    private fun handleMemberAction(chatId: String, member: GroupMember, action: String) {
        // If I was removed, ignore all actions — adapter should not show buttons anyway
        if (iWasRemoved) return

        val name = member.user.username ?: member.user.fullName ?: "this member"

        when (action) {

            "make_moderator" -> {
                val fromAdmin = member.role.name == "admin"

                confirm(
                    title   = "Make Moderator",
                    message = if (fromAdmin)
                        "Demote $name from admin to moderator? They will keep moderator permissions but lose admin privileges."
                    else
                        "Give $name moderator permissions? They can add members, rename the group, and remove regular members.",
                    confirm = "Make Moderator"
                ) { viewModel.changeMemberRole(chatId, member.user._id, "moderator") }
            }

            "make_admin" -> confirm(
                title   = "Make Admin",
                message = "Make $name an admin? Admins have full control over the group including deleting it.",
                confirm = "Make Admin"
            ) { viewModel.changeMemberRole(chatId, member.user._id, "admin") }

            "make_member" -> {
                val isAdmin = member.role.name == "admin"

                confirm(
                    title   = if (isAdmin) "Demote Admin" else "Remove Moderator Role",
                    message = if (isAdmin)
                        "Remove $name's admin privileges? They will become a regular member."
                    else
                        "Remove $name's moderator permissions? They will become a regular member.",
                    confirm = if (isAdmin) "Demote to Member" else "Remove Role"
                ) { viewModel.changeMemberRole(chatId, member.user._id, "member") }
            }

            "mute" -> confirm(
                title   = "Mute $name",
                message = "$name will not be able to send messages in this group.",
                confirm = "Mute"
            ) {
                viewModel.setMemberMuteStatus(
                    chatId,
                    member.user._id,
                    true) }

            "unmute" -> viewModel.setMemberMuteStatus(chatId, member.user._id, false)

            "remove" -> {

                val username = member.user.username ?: member.user.fullName ?: "Someone"
                confirm(

                    title       = "Remove $name?",
                    message     = "$name will be removed from the group and will no longer receive messages.",
                    confirm     = "Remove",
                    isDangerous = true

                ) {
                    insertLocalRemoveSystemMessage(
                        chatId,
                        member.user._id,
                        username)

                    viewModel.removeMember(
                        chatId,
                        member.user._id
                    )
                }
            }
        }
    }


    private fun insertLocalRemoveSystemMessage(
        chatId:          String,
        removedUserId:   String,
        removedUsername: String
    ) {
        val now  = System.currentTimeMillis()
        val text = "You removed @$removedUsername"

        val userEntity = UserEntity(
            id       = myUserId,
            name     = myUsername,
            avatar   = myAvatar,
            online   = true,
            lastSeen = Date(now)
        )

        val entity = MessageEntity(
            id              = "Text_system_remove_${removedUserId}_${myUserId}_${chatId.takeLast(6)}",
            chatId          = chatId,
            text            = text,
            userId          = myUserId,
            user            = userEntity,
            createdAt       = now,
            imageUrl        = null,
            voiceUrl        = null,
            voiceDuration   = 0,
            userName        = myUsername,
            status          = "Received",
            videoUrl        = null,
            audioUrl        = null,
            docUrl          = null,
            fileSize        = 0,
            isSystemMessage = true
        )

        CoroutineScope(Dispatchers.IO).launch {
            messageRepository.insertMessage(entity)
            kotlinx.coroutines.withContext(Dispatchers.Main) {
                EventBus.getDefault().post(entity)
            }
        }
    }



}