package com.uyscuti.social.circuit.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.uyscuti.social.circuit.MainActivity
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.adapter.AddParticipantsAdapter
import com.uyscuti.social.circuit.databinding.ActivityConfirmGroupBinding
import com.uyscuti.sharedmodule.presentation.DialogViewModel
import com.uyscuti.sharedmodule.presentation.GroupDialogViewModel
import com.uyscuti.sharedmodule.presentation.UsersViewModel
import com.uyscuti.social.core.common.data.room.entity.GroupDialogEntity
import com.uyscuti.social.core.common.data.room.entity.MessageEntity
import com.uyscuti.social.core.common.data.room.entity.UserEntity
import com.uyscuti.social.core.models.data.User
import com.uyscuti.social.network.api.request.group.RequestGroupChat
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import javax.inject.Inject

@AndroidEntryPoint
class ConfirmGroupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfirmGroupBinding
    private lateinit var horizontalRecyclerView: RecyclerView
    private lateinit var selectedUsersAdapter: AddParticipantsAdapter

    @Inject
    lateinit var retrofitInterface: RetrofitInstance

    private var participantsIds: ArrayList<String>    = arrayListOf()
    private var participants: ArrayList<User>          = arrayListOf()
    private var loadingDialog: android.app.Dialog?     = null
    private var chatParticipant: ArrayList<UserEntity> = arrayListOf()

    private val usersViewModel:       UsersViewModel       by viewModels()
    private val dialogViewModel:      DialogViewModel       by viewModels()
    private val groupDialogViewModel: GroupDialogViewModel  by viewModels()

    private val gson = Gson()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfirmGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        horizontalRecyclerView = binding.horizontalRecyclerView
        horizontalRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        selectedUsersAdapter = AddParticipantsAdapter(this) {}

        val receivedParticipants = intent.getParcelableArrayListExtra<User>("participantList")

        binding.toolbar.setNavigationIcon(R.drawable.back_svgrepo_com)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        binding.groupNameET.requestFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)

        if (receivedParticipants != null) {
            for (selectedUser in receivedParticipants) {
                selectedUsersAdapter.addUser(selectedUser)
            }
        }

        val lastPosition = selectedUsersAdapter.itemCount - 1
        horizontalRecyclerView.adapter = selectedUsersAdapter
        horizontalRecyclerView.smoothScrollToPosition(lastPosition)
        selectedUsersAdapter.notifyDataSetChanged()

        val myUserId = getSharedPreferences("LocalSettings", 0).getString("_id", "") ?: ""

        if (receivedParticipants != null) {
            for (participant in receivedParticipants) {
                if (participant.id != myUserId) {
                    participantsIds.add(participant.id)
                }
            }
        }

        binding.confirmFab.setOnClickListener {
            val groupName = binding.groupNameET.text.toString().trim()
            if (groupName.isEmpty()) {
                binding.groupNameET.error = "Please enter a group name"
                return@setOnClickListener
            }
            createGroupChat(RequestGroupChat(groupName, participantsIds))
        }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // MAIN: Create group chat
    // ─────────────────────────────────────────────────────────────────────────

    private fun createGroupChat(data: RequestGroupChat) {
        showLoadingDialog()
        participants.clear()
        chatParticipant.clear()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Log.d("CreateGroup", "Sending: name=${data.name}, participants=${data.participants}")

                val response = retrofitInterface.apiService.createGroupChat(data)

                if (response.isSuccessful) {
                    val rawString = gson.toJson(response.body())
                    Log.d("CreateGroup", "Raw response: $rawString")

                    val rootObj = gson.fromJson(rawString, JsonObject::class.java)
                    val dataEl  = rootObj?.get("data")?.takeIf { !it.isJsonNull }
                    val isEmpty = dataEl == null || !dataEl.isJsonObject
                            || dataEl.asJsonObject.size() == 0

                    if (isEmpty) {
                        // Server returned data:{} due to serialization bug —
                        // group WAS created (HTTP 201). Fetch the list silently.
                        Log.w("CreateGroup", "data:{} received — using getAllGroupChats fallback")
                        handleEmptyDataFallback(data.name)
                        return@launch
                    }

                    // Happy path — parse and navigate
                    val chat: GroupChatDetail = gson.fromJson(dataEl, GroupChatDetail::class.java)
                    Log.d("CreateGroup", "Parsed: _id=${chat._id}  name=${chat.name}")
                    saveAndNavigate(chat, data.name)

                } else {
                    // Real HTTP error — group was NOT created
                    val errorCode = response.code()
                    val errorBody = response.errorBody()?.string() ?: "No error body"
                    Log.e("CreateGroup", "HTTP $errorCode – $errorBody")
                    withContext(Dispatchers.Main) {
                        dismissLoadingDialog()
                        Toast.makeText(
                            this@ConfirmGroupActivity,
                            "Failed to create group ($errorCode). Please try again.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

            } catch (e: HttpException) {
                Log.e("CreateGroup", "HttpException: ${e.message}")
                withContext(Dispatchers.Main) {
                    dismissLoadingDialog()
                    Toast.makeText(
                        this@ConfirmGroupActivity,
                        "Network error. Please check your connection and try again.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: IOException) {
                Log.e("CreateGroup", "IOException: ${e.message}")
                withContext(Dispatchers.Main) {
                    dismissLoadingDialog()
                    Toast.makeText(
                        this@ConfirmGroupActivity,
                        "Connection lost. Please check your internet and try again.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("CreateGroup", "Unexpected: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    dismissLoadingDialog()
                    Toast.makeText(
                        this@ConfirmGroupActivity,
                        "Something went wrong. Please try again.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // FALLBACK: data:{} came back — fetch group list, open the newest group
    // ─────────────────────────────────────────────────────────────────────────

    private suspend fun handleEmptyDataFallback(groupName: String) {
        try {
            val groupsResp = retrofitInterface.apiService.getAllGroupChats()

            if (groupsResp.isSuccessful) {
                val groups = groupsResp.body()?.data
                val newest = groups?.maxByOrNull { it.createdAt ?: "" }

                if (newest != null) {
                    Log.d("CreateGroup", "Fallback found: ${newest._id} – ${newest.name}")
                    saveAndNavigate(newest, groupName)
                    return
                }
            }

            // If even the fallback failed, just go to the Groups tab
            Log.e("CreateGroup", "Fallback fetch failed or returned empty list")
            withContext(Dispatchers.Main) {
                dismissLoadingDialog()
                goToGroupsTab()
            }

        } catch (e: Exception) {
            Log.e("CreateGroup", "Fallback exception: ${e.message}")
            withContext(Dispatchers.Main) {
                dismissLoadingDialog()
                goToGroupsTab()
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SAVE: persist to Room and generate invite link, then go to Groups tab.
    // Does NOT open MessagesActivity — that caused the black screen.
    // The user lands on the Groups tab and taps the group to open it naturally.
    // ─────────────────────────────────────────────────────────────────────────

    private suspend fun saveAndNavigate(chat: GroupChatDetail, fallbackName: String) {
        val chatId   = chat._id
        val chatName = chat.name?.takeIf { it.isNotBlank() } ?: fallbackName
        val adminId  = chat.admin ?: ""

        // Prefer role-aware members list; fall back to participants array
        val memberUsers: List<GroupMemberUser> = when {
            !chat.members.isNullOrEmpty()      -> chat.members.map { it.user }
            !chat.participants.isNullOrEmpty() -> chat.participants
            else                               -> emptyList()
        }

        val participantEntities = arrayListOf<UserEntity>()
        memberUsers.forEach {
            participantEntities.add(it.toAppUser().toUserEntity())
        }

        val adminMemberUser = memberUsers.firstOrNull { it._id == adminId }

        val lastMessage = createDefaultMessageEntity(chat.createdAt ?: "")
        val createdAt   = convertIso8601ToUnixTimestamp(chat.createdAt ?: "")
        val updatedAt   = convertIso8601ToUnixTimestamp(chat.updatedAt ?: "")

        val dialogEntity = GroupDialogEntity(
            id          = chatId,
            adminId     = adminMemberUser?._id ?: adminId,
            adminName   = adminMemberUser?.fullName?.takeIf { it.isNotBlank() }
                ?: adminMemberUser?.username ?: "",
            dialogPhoto = "",       // no photo for new groups
            dialogName  = chatName,
            users       = participantEntities,
            lastMessage = lastMessage,
            unreadCount = 0,
            updatedAt   = updatedAt,
            createdAt   = createdAt
        )

        // Save to Room
        CoroutineScope(Dispatchers.IO).launch {
            groupDialogViewModel.insertGroupDialog(dialogEntity)
        }

        // Generate invite link — non-fatal
        try {
            val linkResp = retrofitInterface.apiService.generateGroupLink(chatId)
            if (linkResp.isSuccessful) {
                Log.d("CreateGroup", "Invite link: ${linkResp.body()?.data?.inviteLink}")
            } else {
                Log.w("CreateGroup",
                    "Link gen failed ${linkResp.code()}: ${linkResp.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.w("CreateGroup", "Link generation non-fatal: ${e.message}")
        }

        withContext(Dispatchers.Main) {
            dismissLoadingDialog()
            if (chatId.isNotBlank()) {
                goToGroupsTab()
            } else {
                Toast.makeText(
                    this@ConfirmGroupActivity,
                    "Group created! Find it in your Groups tab.",
                    Toast.LENGTH_SHORT
                ).show()
                goToGroupsTab()
            }
        }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // NAVIGATE: Go back to MainActivity on the Groups tab.
    // Uses FLAG_ACTIVITY_CLEAR_TOP so if MainActivity is already running,
    // onNewIntent fires and switchToGroupsTab() handles it cleanly.
    // Back button works correctly because the stack is just MainActivity.
    // ─────────────────────────────────────────────────────────────────────────

    @OptIn(UnstableApi::class)
    private fun goToGroupsTab() {
        val intent = Intent(this@ConfirmGroupActivity, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("openGroupsTab", true)
        }
        startActivity(intent)
        finish()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private fun GroupMemberUser.toAppUser(): User = User(
        _id,
        fullName?.takeIf { it.isNotBlank() } ?: username ?: "",
        avatar?.url ?: "",
        false,
        null
    )

    private fun User.toUserEntity(): UserEntity = UserEntity(id, name, avatar, Date(), true)

    private fun convertIso8601ToUnixTimestamp(iso8601Date: String): Long {
        if (iso8601Date.isBlank()) return 0L
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'"
        )
        for (fmt in formats) {
            try {
                val sdf = SimpleDateFormat(fmt)
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                return sdf.parse(iso8601Date)?.time ?: 0L
            } catch (_: Exception) {}
        }
        return 0L
    }


}