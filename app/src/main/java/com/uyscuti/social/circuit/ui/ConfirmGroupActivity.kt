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


    private fun convertIso8601ToUnixTimestamp(iso8601Date: String): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        sdf.timeZone = TimeZone.getTimeZone("UTC")


        val date = sdf.parse(iso8601Date)
        return date?.time ?: 0
    }


    private fun createDefaultMessageEntity(date: String): MessageEntity {
        val createdAt = convertIso8601ToUnixTimestamp(date)
        val lastseen = Date(createdAt)
        val user = UserEntity(
            id = "Flash",
            name = "Flash",
            avatar = "Flash",
            online = true,
            lastSeen = lastseen
        )

        return MessageEntity(
            id = "FirstMessageId",
            chatId = "InitialMessage",
            text = "No messages yet for this group chat, be the first to send a message.",
            userId = "Flash",
            user = user,
            createdAt = createdAt,
            imageUrl = null,
            voiceUrl = null,
            voiceDuration = 0,
            userName = "Flash",
            status = "Received",
            videoUrl = null,
            audioUrl = null,
            docUrl = null,
            fileSize = 0
        )
    }


    private fun createGroupChat(data: RequestGroupChat){
        showLoadingDialog()

        GlobalScope.launch {

            val response = try {
                retrofitInterface.apiService.createGroupChat(data)
            }catch (e: HttpException) {

                return@launch
            }catch (e: IOException) {

                return@launch
            }

            if (response.isSuccessful) {
                // User chat created successfully, you can handle this as needed

                val chatId = response.body()!!.data._id
                val chatName = response.body()!!.data.name
                val adminId = response.body()!!.data.admin

                response.body()!!.data.participants.map {
                    participants.add(it.toUser())
                }

                val admin = response.body()!!.data.participants.first { it._id == adminId }.toUser()


                val dialog = Dialog(
                    chatId,
                    chatName,
                    response.body()!!.data.participants[0].avatar.url,
                    participants,
                    null,
                    0
                )


                participants.map {
                    chatParticipant.add(it.toUserEntity())
                }

                val chatResponse = response.body()!!.data
                val lastMessage = createDefaultMessageEntity(chatResponse.createdAt)
                val createdAt = convertIso8601ToUnixTimestamp(chatResponse.createdAt)
                val updatedAt = convertIso8601ToUnixTimestamp(chatResponse.updatedAt)

                val dialogEntity = GroupDialogEntity(
                    id = chatResponse._id,
                    adminId = admin.id,
                    adminName = admin.name,
                    dialogPhoto = chatResponse.participants[0].avatar.url,
                    dialogName = chatName,
                    users = chatParticipant,
                    lastMessage = lastMessage,
                    unreadCount = 0,
                    updatedAt = updatedAt,
                    createdAt = createdAt
                )

                insertDialog(dialogEntity)

                dismissLoadingDialog()

                MessagesActivity.open(
                    this@ConfirmGroupActivity, "",
                    dialog, false, ""
                )

                finish()
            }else{
                Toast.makeText(this@ConfirmGroupActivity, "Failed To Create Group, Please Try again later", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun insertDialog(dialog: GroupDialogEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            groupDialogViewModel.insertGroupDialog(dialog)
        }

    }

    private fun User.toUserEntity(): UserEntity {

        val lastseen = Date()
        return UserEntity(
            id,
            name,
            avatar,
            lastseen,
            true
        )
    }
    private fun com.uyscuti.social.network.api.models.User.toUser(): User {
        return User(
            _id,
            username,
            avatar.url,
            false,
            lastseen
        )
    }

    private fun showLoadingDialog() {
        // Create a dialog with the loading layout
        dialog = android.app.Dialog(this)
        dialog?.setContentView(R.layout.loading_dialog)
        dialog?.setCancelable(false) // Prevent dismissing by tapping outside

        // Show the dialog
        dialog?.show()
    }

    private fun dismissLoadingDialog() {
        dialog?.dismiss()
    }

}