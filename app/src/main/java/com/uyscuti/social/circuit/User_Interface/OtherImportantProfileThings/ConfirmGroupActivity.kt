package com.uyscuti.social.circuit.User_Interface.OtherImportantProfileThings

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.adapter.AddParticipantsAdapter
import com.uyscuti.social.circuit.data.model.User
import com.uyscuti.social.circuit.databinding.ActivityConfirmGroupBinding
import com.uyscuti.social.circuit.presentation.DialogViewModel
import com.uyscuti.social.circuit.presentation.GroupDialogViewModel
import com.uyscuti.social.circuit.presentation.UsersViewModel
import com.uyscuti.social.core.common.data.room.entity.GroupDialogEntity
import com.uyscuti.social.core.common.data.room.entity.MessageEntity
import com.uyscuti.social.core.common.data.room.entity.UserEntity
import com.uyscuti.social.network.api.request.group.RequestGroupChat
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
    private var participantsIds: ArrayList<String> = arrayListOf()
    private var participants: ArrayList<User> = arrayListOf()

    //    private lateinit var groupRepository: GroupCreateRepository
    private var dialog: Dialog? = null
    private var chatParticipant: ArrayList<UserEntity> = arrayListOf()
//    private lateinit var userChatRepository: UserChatDialogRepository


    private val usersViewModel: UsersViewModel by viewModels()

    private val dialogViewModel: DialogViewModel by viewModels()

    private val groupDialogViewModel: GroupDialogViewModel by viewModels()





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfirmGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)


        horizontalRecyclerView = binding.horizontalRecyclerView

        val horizontalLayoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        horizontalRecyclerView.layoutManager = horizontalLayoutManager

        selectedUsersAdapter = AddParticipantsAdapter(this) {

        }


        val receivedParticipants = intent.getParcelableArrayListExtra<User>("participantList")

        Log.d("GroupChats", "Received Participant List: $receivedParticipants")

        binding.toolbar.setNavigationIcon(R.drawable.back_svgrepo_com)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.groupNameET.requestFocus()


        // Show the keyboard
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

        if (receivedParticipants != null) {
            for (participant in receivedParticipants){
                val id = participant.id
                participantsIds.add(id)
            }
        }

        binding.confirmFab.setOnClickListener {
            val groupName = binding.groupNameET.text.toString()

            val data = RequestGroupChat(
                groupName,
                participantsIds
            )

            createGroupChat(data)
        }
    }


//    private fun convertToDBUserList(participants: List<User>): List<DBUserList> {
//        val avatar = Avatar(
//            _id = "",
//            localPath = "",
//            url = ""
//        )
//
//        return participants.map {
//            DBUserList(
//                it._id,
//                avatar,
//                it.username,
//                it.email,
//                it.createdAt,
//                it.updatedAt
//            )
//        }
//    }

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
//                Log.d("RetrofitActivity", "Http : ${e.message}")
                return@launch
            }catch (e: IOException) {
//                Log.d("RetrofitActivity", "IOException : ${e.message}")
                return@launch
            }

            if (response.isSuccessful) {
                // User chat created successfully, you can handle this as needed
//                Log.d("UserDialog", "Group chat created successfully: id ${response.body()!!.data._id}")
//                        callBack?.onChatCreated()
                val chatId = response.body()!!.data._id
                val chatName = response.body()!!.data.name
                val adminId = response.body()!!.data.admin

                response.body()!!.data.participants.map {
                    participants.add(it.toUser())
                }

                val admin = response.body()!!.data.participants.first { it._id == adminId }.toUser()


                val dialog = com.uyscuti.social.circuit.data.model.Dialog(
                    chatId,
                    chatName,
                    response.body()!!.data.participants[0].avatar.url,
                    participants,
                    null,
                    0
                )
//                response.body()!!.data.participants.map {
//                    participants.add(it.toUser())
//                }

                participants.map {
                    chatParticipant.add(it.toUserEntity())
                }
//                chatParticipant.add(participants.map { it.toUserEntity() })


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

//                        allUsers.add(dialogEntity)
                insertDialog(dialogEntity)

                dismissLoadingDialog()

                MessagesActivity.Companion.open(
                    this@ConfirmGroupActivity, "",
                    dialog, false
                )
//                        openChatActivity(chatId,chatName)
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
//        Log.d("GroupChat", "Added Group to local -  $dialog")
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
    //    private fun addGroupToRoomDb(groupUsers: GroupChatEntity) {
//        CoroutineScope(Dispatchers.IO).launch {
//            groupRepository.addGroupUsers(groupUsers)
//        }
//    }
    private fun showLoadingDialog() {
        // Create a dialog with the loading layout
        dialog = Dialog(this)
        dialog?.setContentView(R.layout.loading_dialog)
        dialog?.setCancelable(false) // Prevent dismissing by tapping outside

        // Show the dialog
        dialog?.show()
    }

    private fun dismissLoadingDialog() {
        dialog?.dismiss()
    }

}