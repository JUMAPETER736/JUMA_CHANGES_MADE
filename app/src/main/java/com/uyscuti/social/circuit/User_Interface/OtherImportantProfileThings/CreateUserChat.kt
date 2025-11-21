package com.uyscuti.social.circuit.User_Interface.OtherImportantProfileThings

import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.adapter.SearchUserAdapter
import com.uyscuti.social.circuit.data.model.Dialog
import com.uyscuti.social.circuit.data.model.Message
import com.uyscuti.social.circuit.data.model.User
import com.uyscuti.social.circuit.databinding.ActivityCreateUserChatBinding
import com.uyscuti.social.circuit.presentation.DialogViewModel
import com.uyscuti.social.circuit.presentation.RecentUserViewModel
import com.uyscuti.social.core.common.data.room.entity.DialogEntity
import com.uyscuti.social.core.common.data.room.entity.MessageEntity
import com.uyscuti.social.core.common.data.room.entity.RecentUser
import com.uyscuti.social.core.common.data.room.entity.UserEntity
import com.uyscuti.social.network.api.request.search.SearchUsersRequest
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import retrofit2.HttpException
import java.io.IOException
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class CreateUserChat : AppCompatActivity() {
    private lateinit var binding: ActivityCreateUserChatBinding
    private lateinit var searchEditText: EditText
    private lateinit var userListView: RecyclerView
    private lateinit var userListAdapter: SearchUserAdapter
    private lateinit var originalUserList: List<User>

    private var chatParticipant: ArrayList<UserEntity> = arrayListOf()


    private var participants: ArrayList<User> = arrayListOf()

    @Inject
    lateinit var retrofitInterface: RetrofitInstance


    private val recentUserViewModel: RecentUserViewModel by viewModels()

    @Inject
    lateinit var localStorage: LocalStorage

    private lateinit var myId: String

    private val dialogViewModel: DialogViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateUserChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        searchEditText = binding.searchEditText

        myId = localStorage.getUserId()

        binding.toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        setListeners()
        initUserList()


        CoroutineScope(Dispatchers.IO).launch {
            val recentUsers = recentUserViewModel.getRecentUsers()

            userListAdapter.setRecentUsers(recentUsers.map { it.toUser() })

            Log.d("RecentUsers", "RecentUsers: $recentUsers")
        }



//        CoroutineScope(Dispatchers.Main).launch {
//            recentUserViewModel.recentUsers.observe(this@CreateUserChat, Observer{ users ->
//                Log.d("RecentUsers", "RecentUsers: $users")
//                if (users.isNotEmpty()){
//                    val recentLiveUsers = users.map { it.toUser() }
////                    userListAdapter.setRecentUsers(recentLiveUsers)
//                }
//            })
//        }

//        observeTempDialogs()

    }

    private fun observeTempDialogs() {
        CoroutineScope(Dispatchers.Main).launch {
            dialogViewModel.getTempDialogs().observe(this@CreateUserChat, Observer { temps ->

                Log.d("ChatOperations", "Temp Chats Found : $temps")
                CoroutineScope(Dispatchers.IO).launch {
                    handleTempDialogs(temps, 0)
//                    deleteTempDialogs(temps)
                }
            })
        }
    }


    private fun RecentUser.toUser(): User{
        return User(
            id, name, avatar, online, lastSeen
        )
    }

    private fun setListeners(){
        // Handle the "Search" button on the keyboard
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                userListAdapter.setLoading(true)
                performSearch(searchEditText.text.toString())
                true
            } else {
                false
            }
        }

        // Or use addTextChangedListener to detect text changes in the EditText
//        searchEditText.addTextChangedListener { text ->
//            performSearch(text.toString())
//        }

        searchEditText.addTextChangedListener(afterTextChanged = { editable ->
            // This will be called after the text in the EditText has changed
            // and the user has finished typing

            Log.d("FinishedTyping", "FinishedTyping: $editable")

            userListAdapter.setLoading(true)
            val searchText = editable.toString()
            performSearch(searchText)
        })
    }

    @OptIn(UnstableApi::class)
    private fun initUserList() {
        userListView = binding.userList
        userListAdapter = SearchUserAdapter(this) {

            Log.d("RecentUsers", "RecentUsers: $it")
            addUserToRecent(it.toRecentUser())

            CoroutineScope(Dispatchers.Main).launch {
                chatExist(it.name) { exists, dialogEntity ->
                    if (exists && dialogEntity?.id != dialogEntity?.dialogName) {

                        Log.d("UserDialog", "Dialog exists, and you can use dialogEntity")
                        // Dialog exists, and you can use dialogEntity
                        participants.add(it)

                        val lastMessage = dialogEntity?.lastMessage?.toMessage()

                        val dialog = Dialog(
                            dialogEntity?.id,
                            dialogEntity?.dialogName,
                            dialogEntity?.dialogPhoto,
                            participants,
                            lastMessage,
                            0
                        )
                        MessagesActivity.Companion.open(
                            this@CreateUserChat, "",
                            dialog, false
                        )

                        finish()
                    } else {
                        Log.d("UserDialog", "Dialog does not exist")
                        doInBackGround(it)
                    }
                }
            }
        }

        // Set LinearLayoutManager for a vertical list
        userListView.layoutManager = LinearLayoutManager(this)

        // Get the random users and sort them alphabetically
//        val randomUsers = DialogsFixtures.getRandomUsers(15)
//        val sortedUsers = randomUsers.sortedBy { it.name }

//        originalUserList = participants.map { it }.sortedBy { it.name }
        originalUserList = listOf()
//
//        val converted = participants.map { it }
//
//        val sorted = converted.sortedBy { it.name }

//        userListAdapter.setSearchUsers(originalUserList)
        userListView.adapter = userListAdapter
    }

    private fun com.uyscuti.social.network.api.models.User.toUser(): User{
        return User(
            _id,
            username,
            avatar.url,
            false,
            Date()
        )
    }

    private fun User.toRecentUser(): RecentUser {
        return RecentUser(
            id,
            name,
            avatar,
            Date(),
            false,
            Date()
        )
    }

    private fun addUserToRecent(user: RecentUser){
        CoroutineScope(Dispatchers.IO).launch {
            recentUserViewModel.addRecentUser(user)
        }
    }

    private fun performSearch(query: String) {
        userListAdapter.setLoading(true)
        // Implement your logic to search for users based on the query
        // You can make a network request here and update your UI accordingly
        // Example: Call your search function and update the UI with search results
        CoroutineScope(Dispatchers.Main).launch {
            val searchResults = searchUsers(query).sortedBy { it.username }
            // Update UI with searchResults...
//

            // Hide the keyboard and remove focus from searchEditText
            hideKeyboard()
            searchEditText.clearFocus()
//            val
//            originalUserList = searchResults.map { it.toUser() }
            Log.d("SearchResults", "$searchResults")
            Log.d("SearchResults", "${searchResults.size}")

            if (searchResults.isNotEmpty()){
                userListAdapter.setSearchUsers(searchResults.map { it.toUser() })
            } else {
                userListAdapter.setNoResults()
            }
        }
    }

    private suspend fun searchUsers(query: String): List<com.uyscuti.social.network.api.models.User> {
        userListAdapter.setLoading(true)
        return withContext(Dispatchers.IO) {
            val users = mutableListOf<com.uyscuti.social.network.api.models.User>()

            try {
                val request = SearchUsersRequest("")
                val response = retrofitInterface.apiService.searchUsers(query)


                if (response.isSuccessful) {
                    Log.d("SearchUsers", "Success Message: ${response.body()?.message}")
                    Log.d("SearchUsers", "Success Data: ${response.body()?.data}")

                    users.addAll(response.body()?.data as List<com.uyscuti.social.network.api.models.User>)
                } else {
                    Log.e("SearchUsers", "Error: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("SearchUsers", "Exception: ${e.message}")
                e.printStackTrace()
            }

            users
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
    }


    private suspend fun deleteTempDialogs(temps: List<DialogEntity>) {
        // Your logic to delete all temporary dialogs
        // You can use dialogViewModel.deleteDialog(dialog) for each dialog
        val ids = temps.filter { it.id == it.dialogName }.map { it.id }
        dialogViewModel.deleteDialogs(ids)
//        temps.forEach { dialog ->
//            dialogViewModel.deleteDialog(dialog)
//        }
    }


    private suspend fun handleTempDialogs(tempDialogs: List<DialogEntity>, index: Int) {
        if (index < tempDialogs.size) {
            val currentDialog = tempDialogs[index]

            // Perform your specific operation for the current dialog
            val operationResult = performOperationForDialog(currentDialog)

            if (operationResult == ChatOperationResult.SUCCESS) {
                // Proceed to the next dialog after the operation is successful
                handleTempDialogs(tempDialogs, index + 1)
            } else {
                // Handle the failure if needed
//                deleteTempDialogs(tempDialogs)
            }
        } else {
            // All dialogs are successfully processed
            // You can perform any final actions here if needed
//            deleteTempDialogs(tempDialogs)
        }
    }

    private suspend fun performOperationForDialog(dialog: DialogEntity): ChatOperationResult {
        // Your specific operation for the dialog goes here
        var chatId = ""
        var isCodeExecuted = false
        // Return ChatOperationResult.SUCCESS if the operation is successful, ChatOperationResult.FAILURE otherwise
        return try {
            // Your operation logic here
            // Assuming you have access to receiverId and receiver data in your dialog
            val receiverId = dialog.users.first().id
            val receiver = dialog.users.first()
//            val receiver = dialog.receiver
            // Invoke createUserChat and check the result
//            val result = createChat(receiverId, receiver)

            val result = createChat(receiverId, receiver)


            if (result is ChatOperationResult.Return) {
                chatId = result.chatId


                // Do something with the chat ID
                Log.d("ChatOperation", "Chat Created Successfully,  Chat ID: $chatId , ")

                CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                    // Your code here

                    if (!isCodeExecuted){
                        isCodeExecuted = true
                        withContext(NonCancellable) {
                            // Your code inside withContext
                            Log.d("ChatOperation", "Before try block : $dialog")
                            try {
//                            dialog.id = chatId
//                            dialogViewModel.updateDialog(dialog)
//                            updateDialog(dialog, chatId)
//                            deleteTempDialogs(listOf(dialog))
                                Log.d("ChatOperation", "After try block , : $dialog")
//                            val updated = dialogViewModel.getDialog(chatId)

                                val newDialog = dialog
                                newDialog.id = chatId
//
//                                dialogViewModel.replaceDialog(dialog,newDialog)
//
//
                                val updated = dialogViewModel.getDialog(chatId)

                                Log.d("ChatOperation", "First Updated Dialog : $updated")
//
//                                cancel()
//
//                                return@withContext

                                // Check for cancellation and respond appropriately
                                if (!NonCancellable.isActive) {
                                    // Perform cleanup or abort the operation as needed
                                    Log.d(
                                        "ChatOperation",
                                        "Coroutine cancelled. Performing cleanup."
                                    )
                                    yield() // Check for cancellation
                                    return@withContext
                                }

                            } catch (e: Exception) {
                                Log.e("MessageUpdate", "Failure: ${e.message}")

                                e.printStackTrace()
                                return@withContext
                            }
                        }
                    }

                }




                ChatOperationResult.SUCCESS
            } else {
                // Handle the failure case
                Log.d("ChatOperation", "Chat Creation Failed")

                ChatOperationResult.FAILURE

            }

        } catch (e: Exception) {
            // Handle the failure and return ChatOperationResult.FAILURE
            Log.d("ChatOperation", "Chat Creation Failed : ${e.message}")

            ChatOperationResult.FAILURE
        }
    }

    private suspend fun updateDialog(dialog: DialogEntity, id:String){
        Log.d("ChatOperation", "Dialog To Update : $dialog")
        dialog.id = id
        dialogViewModel.updateDialog(dialog)

        Log.d("ChatOperation", "After try block , : $dialog")
        val updated = dialogViewModel.getDialog(id)

        Log.d("ChatOperation", "Updated Dialog : $updated")
    }


    private suspend fun createChat(receiverId: String, receiver: UserEntity): ChatOperationResult {
        return try {
            val response = retrofitInterface.apiService.createUserChat(receiverId)

            if (response.isSuccessful) {
                val chatId = response.body()!!.data._id
                val chatName = response.body()!!.data.name



//                participants.add(receiver)

                val dialog = Dialog(
                    chatId,
                    receiver.name,
                    receiver.avatar,
                    participants,
                    null,
                    0
                )

                chatParticipant.add(receiver)

                val chatResponse = response.body()!!.data
                val dialogEntity = DialogEntity(
                    id = chatResponse._id,
                    dialogPhoto = chatResponse.participants[0].avatar.url,
                    dialogName = chatResponse.participants[0].username,
                    users = chatParticipant,
                    lastMessage = null,
                    unreadCount = 0
                )

                val dialogs = dialogViewModel.getDialogsList()

                val available = dialogs.filter { it.id == it.dialogName }.find { it.dialogName == receiver.name }

                Log.d("Available", "Available Chat : $available")

                if (available != null) {
//                    dialogViewModel.replaceDialog(available,dialogEntity)


                    deleteTempDialogs(listOf(available))
                }

                insertDialog(dialogEntity)

//                MessagesActivity.open(
//                    this@CreateChatActivity, "",
//                    dialog
//                )
//
//                finish()
                ChatOperationResult.Return(chatId)
            } else {
                // Handle the error or show a message to the user
                val errorBody = response.errorBody()?.string()
                Log.d("UserDialog", "Error creating user chat: $errorBody")
                ChatOperationResult.FAILURE
            }
        } catch (e: HttpException) {
            Log.d("UserDialog", "Http Exception In User Dialog is : ${e.message}")
            ChatOperationResult.FAILURE
        } catch (e: IOException) {
            Log.d("UserDialog", "IOException In User Dialog is : ${e.message}")
            ChatOperationResult.FAILURE
        }
    }


    private suspend fun chatExist(name: String, resultHandler: (Boolean, DialogEntity?) -> Unit) {
        val dialogEntityLiveData = dialogViewModel.getDialogByName(name)

        val dialogs = dialogViewModel.getDialogsList()

        val available = dialogs.filter { it.id != it.dialogName }.find { it.dialogName == name }

        val dialogEntity = dialogEntityLiveData.value

        Log.d("Dialog", "Dialog Entity : $dialogEntity")
        Log.d("Dialog", "Available Dialog Entity : $available")

        // Invoke the resultHandler with the information about dialog existence and the entity
        resultHandler(available != null, available)
    }

    private fun insertDialog(dialog: DialogEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            dialogViewModel.insertDialog(dialog)
        }
//        Log.d("OneToChat", "Added single chat to local -  $dialog")

    }


    private fun MessageEntity.toMessage(): Message {

        val userId = if (userId == myId) "0" else "1"
        val status = status
        val user =
            User(
                userId,
                user.name,
                user.avatar,
                user.online,
                user.lastSeen
            )
        val date = Date(createdAt)

        // Check if the text is "None" and imageUrl is not null
        val messageContent = if (imageUrl != null) {
//                        user.id = "0"
            Message(
                id,
                user,
                null,
                date
            ).apply {
                setImage(Message.Image(imageUrl!!))
                setStatus(status)
            }
        } else if (videoUrl != null) {
//                        user.id = "0"
            Message(
                id,
                user,
                null,
                date
            ).apply {
                setVideo(Message.Video(videoUrl!!))
                setStatus(status)
            }
        } else if (audioUrl != null) {
//                        user.id = "0"
            Message(
                id,
                user,
                null,
                date
            ).apply {
                setAudio(
                    Message.Audio(
                        audioUrl!!,
                        0,
                        getNameFromUrl(audioUrl!!)
                    )
                )
                setStatus(status)
            }
        } else if (voiceUrl != null) {
//                        user.id = "0"
            Message(
                id,
                user,
                null,
                date
            ).apply {
                setVoice(Message.Voice(voiceUrl!!, voiceDuration))
                setStatus(status)
            }
        } else if (docUrl != null) {
//                        user.id = "0"
            Message(
                id,
                user,
                null,
                date
            ).apply {


                setStatus(status)
            }
        } else {
            Message(
                id,
                user,
                text,
                date
            ).apply {
                setStatus(status)
            }
        }

        return messageContent
    }

    private fun getNameFromUrl(videoUrl: String): String {
        // Split the URL using '/' as a delimiter and get the last part, which is the video filename
        val parts = videoUrl.split("/")

        // You can further process the filename if needed, such as removing the file extension
        return parts.last()
    }

    private fun doInBackGround(user: User) {
        val singleUserList = arrayListOf(user)


        Log.d("UserList", "Single User List Size : ${singleUserList.size}")

        val tempDialog = Dialog(
            user.name,
            user.name,
            user.avatar,
            singleUserList,
            null,
            0
        )



        val dialogEntity = DialogEntity(
            id = tempDialog.dialogName,
            dialogPhoto = tempDialog.dialogPhoto,
            dialogName = tempDialog.dialogName,
            users = listOf(user.toUserEntity()),
            lastMessage = null,
            unreadCount = 0
        )

        insertDialog(dialogEntity)

//        createUserChat(user.id, user)

        openTempChat(tempDialog)
    }

    @OptIn(UnstableApi::class)
    private fun openTempChat(dialog: Dialog) {
        MessagesActivity.Companion.open(
            this@CreateUserChat, "",
            dialog, true
        )
        finish()
    }

    private fun User.toUserEntity(): UserEntity {
        return UserEntity(
            id,
            name,
            avatar,
            lastSeen = Date(),
            true
        )
    }

}